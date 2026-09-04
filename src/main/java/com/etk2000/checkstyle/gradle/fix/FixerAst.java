package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shared AST entry point for the fixers. A fixer may parse a candidate it builds to
 * test a rewrite, or a file that never parsed, so a parse failure is an ordinary
 * state rather than an error: every caller needs the same
 * firewall, degrading to "no AST" and its text fallback instead of aborting the
 * fix pass. Reuse this rather than re-deriving the catch list, which has to cover
 * {@code StackOverflowError} and {@code AssertionError} from the parser as well as
 * the declared exception.
 */
final class FixerAst {
	/**
	 * A computation over a parsed buffer that may fail the way checkstyle's own
	 * AST readers can.
	 */
	@FunctionalInterface
	interface AstFunction<T> {
		@Nullable
		T apply(@Nonnull DetailAST root) throws CheckstyleException;
	}

	/**
	 * A check's positional classifier: whatever it recognizes at {@code (line, column)}, or null
	 * when nothing it recognizes sits there. {@code line} is a 0-based line index and {@code column}
	 * a 0-based code-point column, the pair {@code AstQuery.findNodeAt} takes and every locator in
	 * the repo is written against.
	 */
	@FunctionalInterface
	interface Locator<T> {
		@Nullable
		T locate(@Nonnull DetailAST root, int line, int column) throws CheckstyleException;
	}

	/**
	 * A parser that may fail the way checkstyle's does. Exists so the firewall's
	 * error arms can be driven in a test without a buffer that provokes each one
	 * from the real parser.
	 */
	@FunctionalInterface
	interface ThrowingParser {
		@Nonnull
		DetailAST parse(@Nonnull List<String> lines) throws CheckstyleException;
	}

	/**
	 * The classes {@code List.of} and {@code List.copyOf} return, whose contents cannot change under
	 * the same reference. Collected through {@code Set.copyOf} because a JDK may share one class
	 * between sizes.
	 */
	private static final Set<Class<?>> UNMODIFIABLE_LISTS = Set.copyOf(List.of(List.of().getClass(), List.of("").getClass(), List.of("", "", "").getClass()));
	/**
	 * The buffer the cached root was parsed from, held as an immutable copy so a
	 * later in-place edit to the caller's list cannot make it silently match.
	 */
	private static final ThreadLocal<List<String>> CACHED_LINES = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> CACHED_MASK = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> CACHED_MASK_LINES = new ThreadLocal<>();
	// the caller's own list the mask was taken from, trusted without comparing lines when unmodifiable
	private static final ThreadLocal<List<String>> CACHED_MASK_SOURCE = new ThreadLocal<>();
	private static final ThreadLocal<ThrowingParser> CACHED_PARSER = new ThreadLocal<>();
	private static final ThreadLocal<DetailAST> CACHED_ROOT = new ThreadLocal<>();
	// the caller's own list the cached root was parsed from, and its buffer version then
	private static final ThreadLocal<List<String>> CACHED_SOURCE = new ThreadLocal<>();
	// the lexer state each line of the masked buffer begins in, plus the state after the last
	private static final ThreadLocal<LexerState[]> CACHED_STATES = new ThreadLocal<>();
	private static final ThreadLocal<Integer> CACHED_VERSION = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> JUDGED_SOURCE = new ThreadLocal<>();
	private static final ThreadLocal<Boolean> JUDGED_SOURCE_PARSES = new ThreadLocal<>();

	// held as a constant rather than a fresh method reference per call so the cache
	// can compare parser identity without missing on every production lookup
	static final ThrowingParser DEFAULT_PARSER = PreferStaticImportConstantFixer::parseLinesToAst;

	/**
	 * Drops this thread's cached parse. The cache only has to outlive one file's
	 * fix loop; a Gradle worker thread is pooled for the daemon's lifetime, so a
	 * root left behind pins a whole AST and, through it, this class's loader.
	 */
	static void clearCache() {
		CACHED_LINES.remove();
		CACHED_MASK.remove();
		CACHED_MASK_LINES.remove();
		CACHED_MASK_SOURCE.remove();
		CACHED_PARSER.remove();
		CACHED_ROOT.remove();
		CACHED_SOURCE.remove();
		CACHED_STATES.remove();
		CACHED_VERSION.remove();
		JUDGED_SOURCE.remove();
		JUDGED_SOURCE_PARSES.remove();
	}

	/**
	 * The lexer state line {@code index} of {@code lines} begins in, as
	 * {@code SpanReformat.lexerStateAt} folds it and total in {@code index} the same way, served from
	 * the cache {@link #maskAll} fills: folding from the top of the buffer for every line asked about
	 * makes a pass cost O(violations x file size).
	 */
	@CheckReturnValue
	@Nonnull
	static LexerState entryState(@Nonnull List<String> lines, int index) {
		refreshMask(lines);
		final var states = CACHED_STATES.get();
		return states[Math.clamp(index, 0, states.length - 1)];
	}

	/**
	 * {@code root}, unless it is a failed parse of the live buffer after a fix landed in it though
	 * the buffer parsed before the pass. Every fix must leave the file parseable, so an earlier fix
	 * broke it and the run fails. A candidate a fixer builds to test a rewrite, and a buffer that
	 * never parsed, still answer null.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST failIfFixBrokeParse(
			@Nonnull List<String> lines,
			@Nonnull ThrowingParser parser,
			@Nullable DetailAST root
	) {
		if (root != null || parser != DEFAULT_PARSER)
			return root;
		final var source = FixContext.liveSourceOf(lines);
		final var version = FixContext.getBufferVersion();
		if (source == null || version == null || version == 0 || !sourceParses(source))
			return null;
		throw new FixBrokeParseError("The buffer stopped parsing after " + version + " fixes in " + FixContext.getFilePath());
	}

	/**
	 * Whether {@code guard} holds over the parsed buffer, or cannot tell: false only when it answers
	 * false, true when it answers true, throws or answers null, and when the buffer does not parse,
	 * without asking it. For a guard that stops a fix whose premise may be gone, where a classifier
	 * that failed, or a buffer it cannot read, is no evidence the premise still holds
	 * ({@link #withAst} reads all three as null): the fix is skipped.
	 */
	@CheckReturnValue
	static boolean holdsOrFails(@Nonnull List<String> lines, @Nonnull AstFunction<Boolean> guard) {
		return parseOrNull(lines) == null || !Boolean.FALSE.equals(withAst(lines, guard));
	}

	/**
	 * True when {@code lines} is the buffer the cached root was parsed from: the same list at the
	 * same {@linkplain FixContext#getBufferVersion buffer version} inside {@code applyFixes}, which
	 * edits only through applied fixes, else equal line for line. Comparing every line on every
	 * event would make a pass over a file with many refused violations cost O(violations x lines).
	 */
	@CheckReturnValue
	private static boolean isCachedBuffer(@Nonnull List<String> lines) {
		final var version = FixContext.getBufferVersion();
		if (version != null && CACHED_SOURCE.get() == lines && version.equals(CACHED_VERSION.get()))
			return true;
		final var cachedLines = CACHED_LINES.get();
		if (cachedLines == null || !cachedLines.equals(lines))
			return false;
		CACHED_SOURCE.set(lines);
		CACHED_VERSION.set(version);
		return true;
	}

	/**
	 * True when {@code lines} is the buffer the cached mask was taken from: the same unmodifiable
	 * list, else equal line for line. Outside the pipeline there is no buffer version to settle it,
	 * and comparing every line on every call is what the cache exists to avoid.
	 */
	@CheckReturnValue
	private static boolean isMaskedBuffer(@Nonnull List<String> lines) {
		final var cachedLines = CACHED_MASK_LINES.get();
		if (cachedLines == null)
			return false;
		if (CACHED_MASK_SOURCE.get() == lines && UNMODIFIABLE_LISTS.contains(lines.getClass()))
			return true;
		if (!cachedLines.equals(lines))
			return false;
		CACHED_MASK_SOURCE.set(lines);
		return true;
	}

	/**
	 * What {@code locator} recognizes at the violation's position, or null when the position names
	 * no line of {@code lines}, the buffer does not parse, or the locator recognizes nothing there.
	 *
	 * <p>The prologue every fixer dispatching on a check's locator shares. {@code charColumn} arrives
	 * as an index into the line's chars while an AST position counts code points, so the conversion
	 * happens here, exactly once, and the line is handed on 0-based so every locator agrees on the
	 * base. The {@code lineIndex} test comes first because {@code lines.get} would throw ahead of
	 * either.
	 *
	 * <p>A negative column is refused rather than converted, because
	 * {@link LineText#columnOfCharIndex} clamps it to the start of the line and the locator would
	 * then be asked about whatever sits at column 0. That is not a refusal, it is a fix applied to
	 * the wrong construct.
	 */
	@CheckReturnValue
	@Nullable
	static <T> T locate(@Nonnull List<String> lines, int lineIndex, int charColumn, @Nonnull Locator<T> locator) {
		if (lineIndex < 0 || lineIndex >= lines.size() || charColumn < 0)
			return null;
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		return withAst(lines, root -> locator.locate(root, lineIndex, column));
	}

	/**
	 * {@link JavaLineScanner#maskAll} over {@code lines}, served from a cache when the buffer is
	 * unchanged. Cached for the same reason {@link #parseOrNull} caches its AST: the pipeline calls a
	 * fixer once per violation, so re-masking the whole file each time makes a pass cost
	 * O(violations x file size). Keyed on buffer equality rather than an invalidation call, so a
	 * stale mask cannot outlive an edit. The result is immutable: masked lines are read-only to every
	 * caller, and sharing one instance is what makes the cache worth having.
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> maskAll(@Nonnull List<String> lines) {
		refreshMask(lines);
		return CACHED_MASK.get();
	}

	/**
	 * The parsed buffer, or {@code null} when it does not parse. Callers that
	 * locate a node by the reported position must also handle a non-null root that
	 * no longer has their node there, since the position predates any sibling
	 * fix applied to the same line.
	 *
	 * <p>Repeats against an unchanged buffer are served from a cache, because the
	 * pipeline calls a fixer once per violation and re-parsing a whole compilation
	 * unit each time makes a fix pass cost O(violations x file size). The cache is
	 * keyed on buffer equality rather than an invalidation call so a stale AST
	 * cannot outlive an edit, and it is thread-confined to match {@link FixContext}.
	 * Inside the pipeline the buffer version settles it without comparing lines
	 * ({@link #isCachedBuffer}).
	 *
	 * <p>Throws {@link FixBrokeParseError} instead of answering null when an earlier fix in the
	 * pass broke the live buffer ({@link #failIfFixBrokeParse}).
	 */
	@CheckReturnValue
	@Nullable
	static DetailAST parseOrNull(@Nonnull List<String> lines) {
		return parseOrNull(lines, DEFAULT_PARSER);
	}

	@CheckReturnValue
	@Nullable
	static DetailAST parseOrNull(@Nonnull List<String> lines, @Nonnull ThrowingParser parser) {
		// the parser is part of the key: keyed on the buffer alone, an injected parser
		// would be served an AST it never produced, and its result would then be handed
		// back to the production path for the same buffer
		if (CACHED_PARSER.get() == parser && isCachedBuffer(lines))
			return failIfFixBrokeParse(lines, parser, CACHED_ROOT.get());

		final var root = parseUncached(lines, parser);
		// a failed parse is cached too: the pipeline retries every violation in the
		// file, and re-failing costs as much as succeeding
		CACHED_LINES.set(List.copyOf(lines));
		CACHED_PARSER.set(parser);
		CACHED_ROOT.set(root);
		CACHED_SOURCE.set(lines);
		CACHED_VERSION.set(FixContext.getBufferVersion());
		return failIfFixBrokeParse(lines, parser, root);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST parseUncached(@Nonnull List<String> lines, @Nonnull ThrowingParser parser) {
		try {
			return parser.parse(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError | AssertionError ignored) {
			return null;
		}
	}

	private static void refreshMask(@Nonnull List<String> lines) {
		if (isMaskedBuffer(lines))
			return;
		final var states = new LexerState[lines.size() + 1];
		states[0] = LexerState.NONE;
		for (var i = 0; i < lines.size(); ++i)
			states[i + 1] = JavaLineScanner.stateAfter(lines.get(i), states[i]);
		CACHED_MASK.set(List.copyOf(JavaLineScanner.maskAll(lines)));
		CACHED_MASK_LINES.set(List.copyOf(lines));
		CACHED_MASK_SOURCE.set(lines);
		CACHED_STATES.set(states);
	}

	@CheckReturnValue
	private static boolean sourceParses(@Nonnull List<String> source) {
		if (JUDGED_SOURCE.get() != source) {
			// judged before either is set, so an error out of the parse cannot leave a source without a verdict
			final var parses = parseUncached(source, DEFAULT_PARSER) != null;
			JUDGED_SOURCE.set(source);
			JUDGED_SOURCE_PARSES.set(parses);
		}
		return JUDGED_SOURCE_PARSES.get();
	}

	/**
	 * The result of {@code action} over the parsed buffer, or {@code null} when the
	 * buffer does not parse or {@code action} fails.
	 *
	 * <p>Most fixers classify a violation straight off the AST and want one
	 * firewall over both steps: a throw from a check's classifier is no more
	 * recoverable than a parse failure, and both mean the same thing to the caller
	 * ("this violation cannot be classified, leave it alone"). Splitting them would
	 * let a classifier throw abort the violation's whole check rather than moving on
	 * to the next one.
	 */
	@CheckReturnValue
	@Nullable
	static <T> T withAst(@Nonnull List<String> lines, @Nonnull AstFunction<T> action) {
		try {
			final var root = parseOrNull(lines);
			return root == null ? null : action.apply(root);
		}
		// a classifier that throws is a bug, not an ordinary state like a failed
		// parse, and the skip it degrades to is indistinguishable from the most
		// common one ("no node at that position"). Print so it is not invisible
		catch (CheckstyleException | RuntimeException | StackOverflowError | AssertionError e) {
			System.err.println("AST classifier failed for " + FixContext.getFilePath());
			e.printStackTrace();
			return null;
		}
	}

	private FixerAst() {
	}
}