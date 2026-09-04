package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.etk2000.checkstyle.JavaLineScanner;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shared AST entry point for the fixers. A fixer parses the <em>live</em> buffer,
 * which an earlier fixer in the same pass may already have edited, so a parse
 * failure is an ordinary state rather than an error: every caller needs the same
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
	 * The buffer the cached root was parsed from, held as an immutable copy so a
	 * later in-place edit to the caller's list cannot make it silently match.
	 */
	private static final ThreadLocal<List<String>> CACHED_LINES = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> CACHED_MASK = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> CACHED_MASK_LINES = new ThreadLocal<>();
	private static final ThreadLocal<ThrowingParser> CACHED_PARSER = new ThreadLocal<>();
	private static final ThreadLocal<DetailAST> CACHED_ROOT = new ThreadLocal<>();

	// held as a constant rather than a fresh method reference per call so the cache
	// can compare parser identity without missing on every production lookup
	private static final ThrowingParser DEFAULT_PARSER = PreferStaticImportConstantFixer::parseLinesToAst;

	/**
	 * Drops this thread's cached parse. The cache only has to outlive one file's
	 * fix loop; a Gradle worker thread is pooled for the daemon's lifetime, so a
	 * root left behind pins a whole AST and, through it, this class's loader.
	 */
	static void clearCache() {
		CACHED_LINES.remove();
		CACHED_MASK.remove();
		CACHED_MASK_LINES.remove();
		CACHED_PARSER.remove();
		CACHED_ROOT.remove();
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
	 * Equality is cheap despite comparing every line: the pipeline reuses the same
	 * {@code String} objects for lines it did not touch, and {@code String.equals}
	 * short-circuits on identity.
	 */
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
		final var cachedLines = CACHED_MASK_LINES.get();
		final var cachedMask = CACHED_MASK.get();
		if (cachedLines != null && cachedMask != null && cachedLines.equals(lines))
			return cachedMask;

		final var masked = List.copyOf(JavaLineScanner.maskAll(lines));
		CACHED_MASK_LINES.set(List.copyOf(lines));
		CACHED_MASK.set(masked);
		return masked;
	}

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
		final var cachedLines = CACHED_LINES.get();
		if (cachedLines != null && cachedLines.equals(lines) && CACHED_PARSER.get() == parser)
			return CACHED_ROOT.get();

		DetailAST root;
		try {
			root = parser.parse(lines);
		}
		catch (CheckstyleException | RuntimeException | StackOverflowError | AssertionError ignored) {
			root = null;
		}
		// a failed parse is cached too: the pipeline retries every violation in the
		// file, and re-failing costs as much as succeeding
		CACHED_LINES.set(List.copyOf(lines));
		CACHED_PARSER.set(parser);
		CACHED_ROOT.set(root);
		return root;
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