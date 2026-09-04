package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Re-indents a contiguous, already-line-broken Java span to the project's canonical indentation.
 * Callers (the auto-fixers) decide the line breaks (which content sits on which physical line) and
 * hand the resulting lines here to fix only the leading indentation of each.
 *
 * <p>The rule: {@code indent(line) = indent(openerLineOf(enclosingBracket)) + step}, where the enclosing bracket
 * is the innermost bracket still open at the start of the line after that line's own leading closing
 * brackets are applied, and {@code step} is one tab for {@code &#123;} (a block body) or two tabs for
 * {@code (}/{@code [} (a continuation/argument/chain). Multiple brackets opened on one line form a
 * single level (so {@code method(new ArrayList<>(other(} indents its content one level and
 * {@code )))} dedents one level). A method-chain link ({@code .foo()} beginning its own line) instead
 * indents one level in from the line it chains onto (the preceding content line, i.e. the receiver),
 * so a chain on a receiver that sits on its own continuation line steps in from that line rather than
 * from the enclosing bracket; consecutive links at the same depth align with each other.
 *
 * <p>Lines directly inside a {@code &#123;} block (or at the span's top level) are statements, so their
 * indent also follows statement structure rather than brackets alone: a line continuing an unfinished
 * statement sits two tabs in from the statement's first line, a braceless control body
 * ({@code if}/{@code else}/{@code for}/{@code while}/{@code do} with no {@code &#123;}) one tab in from
 * its header, and a colon-form {@code case}/{@code default} body one tab in from its label. An
 * {@code else}, {@code catch}, {@code finally} or do-while {@code while} returns to the statement it
 * belongs to (a dangling {@code else} binds to the innermost {@code if}). A control block's
 * {@code &#123;} anchors to its statement's first line even when it opens on a wrapped header line.
 * Inside an array initializer or enum constant list a {@code ,} ends an element the way {@code ;} ends
 * a statement.
 *
 * <p>Bracket counting runs on {@link JavaLineScanner#stripCommentsAndStrings}-masked text with the
 * {@link JavaLineScanner.LexerState} threaded across lines, so brackets inside string/char literals,
 * {@code //} and {@code /* *}{@code /} comments, and text blocks are ignored. A line that begins
 * inside a text block or a multi-line block comment is emitted verbatim, since its leading whitespace
 * is significant. Output is tabs-only; the first line is forced to {@code baseTabs}; over-closed input
 * clamps at {@code baseTabs} rather than going negative; the transform is idempotent.
 */
public final class JavaSpanReindenter {
	private enum BraceKind {
		BLOCK,
		ENUM,
		EXPRESSION,
		LIST
	}

	private enum HeaderParen {
		CLOSED,
		NONE,
		OPEN,
		WAITING
	}

	/** A braceless control header whose body is still open, or a statement that just completed. */
	private record Level(int indent, @Nonnull String keyword) {}

	/**
	 * {@code anchor} is the indent its contents step in from and its closer aligns with; {@code completes}
	 * says whether closing it ends the enclosing statement (a control/declaration block, not a lambda,
	 * anonymous class or initializer); {@code frame} is non-null exactly for a {@code &#123;}.
	 */
	private record OpenBracket(int anchor, boolean completes, @Nullable Frame frame, int openerLine, int openerColumn) {}

	/** Statement state of one {@code &#123;} block, or of the span's top level. */
	private static final class Frame {
		private final ArrayDeque<Level> levels = new ArrayDeque<>();
		private final boolean list;
		private final int bodyIndent;
		private boolean annotation, caseBody, enumConstants, headerPending, inStatement, keywordOnly;
		private int headerCloseColumn, headerCloseLine, headerParenDepth, segmentLine, statementIndent;
		@Nonnull
		private List<Level> completed = List.of();
		@Nonnull
		private String keyword = "", lastText = "", segmentText = "";
		@Nonnull
		private HeaderParen headerParen = HeaderParen.NONE;

		private Frame(int bodyIndent, @Nonnull BraceKind kind) {
			this.bodyIndent = bodyIndent;
			enumConstants = kind == BraceKind.ENUM;
			list = kind == BraceKind.LIST;
		}

		@CheckReturnValue
		private int baseIndent() {
			return levels.isEmpty() ? bodyIndent + (caseBody ? 1 : 0) : levels.getFirst().indent() + 1;
		}

		/** An enum's constants end at its first {@code ;}, after which its members are ordinary statements. */
		@CheckReturnValue
		private boolean commaCompletes() {
			return list || enumConstants;
		}
	}

	private static final Pattern ANNOTATION_CALL = Pattern.compile("^@[\\w$.]+\\s*\\(");
	private static final Pattern ANNOTATIONS_ONLY = Pattern.compile("^(?:@[\\w$.]++\\s*+(?:\\([^()]*+\\))?+\\s*+)++$");
	private static final Pattern CASE_LABEL = Pattern.compile("^(case\\b|default\\s*(:|->))");
	private static final Pattern DO_WHILE = Pattern.compile("\\bwhile\\s*\\(");
	private static final Pattern ELSE_IF = Pattern.compile("^else\\s+if\\b");
	private static final Pattern ENUM_PREFIX = Pattern.compile("\\benum\\s+[\\w$]+");
	private static final Pattern LEADING_WORD = Pattern.compile("^[A-Za-z_$][\\w$]*");
	private static final Pattern RESUMING_KEYWORD = Pattern.compile("^(catch|else|finally|while)\\b");
	private static final Pattern STATEMENT_LABEL = Pattern.compile("^[A-Za-z_$][\\w$]*\\s*:(?!:)");
	private static final Set<String> DO_OWNERS = Set.of("do");
	private static final Set<String> IF_OWNERS = Set.of("if");
	private static final Set<String> KEYWORD_HEADERS = Set.of("do", "else", "finally", "try");
	private static final Set<String> PAREN_HEADERS = Set.of("catch", "for", "if", "switch", "synchronized", "try", "while");
	private static final Set<String> TRY_OWNERS = Set.of("catch", "try");

	/**
	 * Re-opens the statement an {@code else}/{@code catch}/{@code finally}/do-{@code while} belongs to:
	 * the innermost just-completed level whose keyword is one of {@code owners}, restoring the braceless
	 * levels outside it. Returns that statement's indent, or -1 when nothing in {@code frame} owns it.
	 */
	@CheckReturnValue
	private static int attach(@Nonnull Frame frame, @Nonnull Set<String> owners) {
		final var completed = frame.completed;
		for (var k = 0; k < completed.size(); ++k) {
			if (owners.contains(completed.get(k).keyword())) {
				for (var outer = completed.size() - 1; outer > k; --outer)
					frame.levels.push(completed.get(outer));
				return completed.get(k).indent();
			}
		}
		return -1;
	}

	private static void beginSegment(@Nonnull Frame frame, @Nonnull String text, int indent, int line) {
		final var elseIf = ELSE_IF.matcher(text);
		final var headerText = elseIf.find() ? text.substring(elseIf.end() - 2) : text;
		final var word = LEADING_WORD.matcher(headerText);
		final var keyword = word.find() ? word.group() : "";
		frame.annotation = text.startsWith("@");
		frame.completed = List.of();
		frame.headerParen = PAREN_HEADERS.contains(keyword) && headerText.substring(keyword.length()).strip().startsWith("(")
				|| ANNOTATION_CALL.matcher(text).find()
				? HeaderParen.WAITING
				: HeaderParen.NONE;
		frame.headerPending = false;
		frame.inStatement = true;
		frame.keyword = keyword;
		frame.keywordOnly = KEYWORD_HEADERS.contains(text);
		frame.segmentLine = line;
		frame.segmentText = text;
		frame.statementIndent = indent;
	}

	/**
	 * Classifies a {@code &#123;} by the code before it on its line ({@code prefix}) and, when that ends in a
	 * {@code )}, the code before the matching {@code (} ({@code parenOwner}, wherever that paren opened). A
	 * brace alone on its line takes the class of the line it continues: an Allman brace.
	 */
	@CheckReturnValue
	@Nonnull
	private static BraceKind braceKind(@Nonnull String prefix, @Nonnull String parenOwner, @Nullable Frame frame, int line) {
		if (prefix.endsWith("->"))
			return CASE_LABEL.matcher(prefix).find() ? BraceKind.BLOCK : BraceKind.EXPRESSION;
		if (endsWithNewType(parenOwner))
			return BraceKind.EXPRESSION;
		if (prefix.isEmpty()) {
			if (frame == null || frame.list)
				return BraceKind.LIST;
			if (frame.segmentLine == line)
				return BraceKind.BLOCK;
			return frame.lastText.isEmpty() ? BraceKind.LIST : braceKind(frame.lastText, parenOwnerOf(frame.lastText), frame, line);
		}
		final var last = prefix.charAt(prefix.length() - 1);
		if (last == '=' || last == ',' || last == '(' || last == '{' || last == ']')
			return BraceKind.LIST;
		final var declarationStart = Math.max(prefix.lastIndexOf(';'), Math.max(prefix.lastIndexOf('{'), prefix.lastIndexOf('}'))) + 1;
		return ENUM_PREFIX.matcher(prefix.substring(declarationStart)).find() ? BraceKind.ENUM : BraceKind.BLOCK;
	}

	private static void complete(@Nonnull Frame frame) {
		final var completed = new ArrayList<Level>(frame.levels.size() + 1);
		// a do-while finished on one line owns no later `while`
		final var oneLineDoWhile = frame.keyword.equals("do") && DO_WHILE.matcher(frame.segmentText).find();
		completed.add(new Level(frame.statementIndent, oneLineDoWhile ? "do-while" : frame.keyword));
		while (!frame.levels.isEmpty())
			completed.add(frame.levels.pop());
		frame.completed = completed;
		frame.headerPending = false;
		frame.inStatement = false;
	}

	/**
	 * Whether {@code text} ends with {@code new Type} or {@code new Type<...>}, the head of an anonymous
	 * class creation. Scans backward once, so a line packed with {@code new X<} stays linear where a regex
	 * restarting at every {@code new} would not.
	 */
	@CheckReturnValue
	private static boolean endsWithNewType(@Nonnull String text) {
		var end = text.stripTrailing().length();
		if (end > 0 && text.charAt(end - 1) == '>') {
			var depth = 0;
			do {
				final var c = text.charAt(--end);
				if (c == '>')
					++depth;
				else if (c == '<')
					--depth;
			}
			while (end > 0 && depth > 0);
			if (depth > 0)
				return false;
			end = text.substring(0, end).stripTrailing().length();
		}
		final var typeEnd = end;
		while (end > 0 && (Character.isJavaIdentifierPart(text.charAt(end - 1)) || text.charAt(end - 1) == '.'))
			--end;
		final var keywordEnd = text.substring(0, end).stripTrailing().length();
		if (end == typeEnd || keywordEnd == end || keywordEnd < 3 || !text.startsWith("new", keywordEnd - 3))
			return false;
		return keywordEnd == 3 || !Character.isJavaIdentifierPart(text.charAt(keywordEnd - 4));
	}

	/**
	 * Updates {@code frame}'s statement state from how a line directly inside it ends: a {@code ;} (or a
	 * {@code ,} in a list) or a control block's {@code &#125;} completes the statement, a header whose
	 * {@code (...)} closes the line opens a braceless body, and a colon label or a lone annotation lets
	 * the next line start a statement at the same level.
	 */
	private static void finishLine(@Nonnull Frame frame, @Nonnull String masked, int line, boolean closedBlock) {
		final var text = masked.strip();
		if (text.isEmpty() || !frame.inStatement)
			return;

		final var end = text.charAt(text.length() - 1);
		final var headerClosed = frame.headerParen == HeaderParen.CLOSED && frame.headerCloseLine == line
				&& masked.substring(frame.headerCloseColumn + 1).isBlank();
		frame.lastText = text;
		if (frame.caseBody && end == ':' && (frame.keyword.equals("case") || frame.keyword.equals("default")))
			frame.inStatement = false;
		else if (end == ';' || end == ',' && frame.commaCompletes() || end == '}' && closedBlock)
			complete(frame);
		else if (frame.annotation && (headerClosed || ANNOTATIONS_ONLY.matcher(text).matches()))
			frame.inStatement = false;
		else if (headerClosed || frame.keywordOnly && frame.segmentLine == line) {
			frame.levels.push(new Level(frame.statementIndent, frame.keyword));
			frame.headerPending = true;
			frame.inStatement = false;
		}
	}

	/** The frame a line at this bracket state sits directly in, or null inside a {@code (}/{@code [}. */
	@CheckReturnValue
	@Nullable
	private static Frame frameAt(@Nonnull Deque<OpenBracket> open, @Nonnull Frame root) {
		return open.isEmpty() ? root : open.getFirst().frame();
	}

	@CheckReturnValue
	private static boolean isCloser(char c) {
		return c == ')' || c == ']' || c == '}';
	}

	/**
	 * Whether a case label is the colon form, judged by whichever of {@code :} and {@code ->} comes first
	 * outside brackets, so a lambda in a colon case's body or a ternary in a guard does not mislead it. A
	 * label wrapped before either is treated as the colon form until its next line says otherwise.
	 */
	@CheckReturnValue
	private static boolean isColonLabel(@Nonnull String text) {
		var depth = 0;
		var ternaries = 0;
		for (var k = 0; k < text.length(); ++k) {
			final var c = text.charAt(k);
			if (isOpener(c))
				++depth;
			else if (isCloser(c))
				--depth;
			else if (depth == 0) {
				final var next = k + 1 < text.length() ? text.charAt(k + 1) : ' ';
				if (c == '-' && next == '>')
					return false;
				if (c == '?')
					++ternaries;
				else if (c == ':' && next == ':')
					++k;
				else if (c == ':') {
					if (ternaries == 0)
						return true;
					--ternaries;
				}
			}
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isOpener(char c) {
		return c == '(' || c == '[' || c == '{';
	}

	@CheckReturnValue
	private static int leadingTabs(@Nonnull String line) {
		var n = 0;
		while (n < line.length() && line.charAt(n) == '\t')
			++n;
		return n;
	}

	/** The index of the {@code (} matching {@code text}'s final {@code )}, or -1 if it opens on an earlier line. */
	@CheckReturnValue
	private static int matchingOpenParen(@Nonnull String text) {
		var depth = 0;
		for (var k = text.length() - 1; k >= 0; --k) {
			final var c = text.charAt(k);
			if (c == ')')
				++depth;
			else if (c == '(' && --depth == 0)
				return k;
		}
		return -1;
	}

	/** The code before the {@code (} matching {@code text}'s final {@code )}, or empty when there is none on this line. */
	@CheckReturnValue
	@Nonnull
	private static String parenOwnerOf(@Nonnull String text) {
		final var openParen = text.endsWith(")") ? matchingOpenParen(text) : -1;
		return openParen < 0 ? "" : text.substring(0, openParen);
	}

	/** {@link #reindent(List, int, LexerState)} for a span whose first line begins outside any literal. */
	@CheckReturnValue
	@Nonnull
	public static List<String> reindent(@Nonnull List<String> lines, int baseTabs) {
		return reindent(lines, baseTabs, LexerState.NONE);
	}

	/**
	 * Returns a copy of {@code lines} re-indented per the class contract. {@code baseTabs} is the
	 * indentation (in tabs) of the span's first line; every other line is indented relative to it.
	 * {@code initial} is the lexer state the first line begins in ({@link SpanReformat#lexerStateAt}), so a
	 * span opening on a text block's closing line still knows which lines are literal content, and a first
	 * line that begins inside a literal is emitted as given, so its caller supplies its leading whitespace
	 * ({@link SpanReformat#firstLineIndent}). A span of zero or one line is returned unchanged.
	 *
	 * @throws IllegalArgumentException if the span has a {@link SpanReformat#hasEscapedDelimiter
	 *                                  Unicode-escaped delimiter}: its lines are a caller's re-layout, so
	 *                                  returning them unindented would emit column-0 code
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> reindent(@Nonnull List<String> lines, int baseTabs, @Nonnull LexerState initial) {
		if (SpanReformat.hasEscapedDelimiter(lines, 0, lines.size() - 1))
			throw new IllegalArgumentException("span hides a literal behind a Unicode-escaped delimiter; the caller must decline it");
		if (lines.size() <= 1)
			return lines;

		final var expanded = splitTrailingClosersAfterLiteral(lines, initial);
		final var result = new ArrayList<String>(expanded.size());
		final var chainByLine = new boolean[expanded.size()];
		final var depthByLine = new int[expanded.size()];
		final var indentByLine = new int[expanded.size()];
		final var maskedByLine = new String[expanded.size()];
		final var open = new ArrayDeque<OpenBracket>();
		final var pendingComments = new ArrayList<Integer>();
		final var root = new Frame(baseTabs, BraceKind.BLOCK);
		var pendingFrame = root;
		var state = initial;
		var prevIndent = baseTabs;
		var prevDepth = 0;
		var prevWasChain = false;
		for (var i = 0; i < expanded.size(); ++i) {
			final var raw = expanded.get(i);
			final var beganInBlock = state.inTextBlock() || state.inBlockComment();
			final var masked = JavaLineScanner.stripCommentsAndStrings(raw, state);
			final var content = raw.strip();
			final var maskedContent = masked.strip();
			final var depthAtStart = open.size();
			final var frame = frameAt(open, root);
			var leadingClosers = 0;
			while (leadingClosers < maskedContent.length() && isCloser(maskedContent.charAt(leadingClosers)))
				++leadingClosers;
			final var isChain = !beganInBlock && leadingClosers == 0 && maskedContent.startsWith(".");
			chainByLine[i] = isChain;
			depthByLine[i] = depthAtStart;
			maskedByLine[i] = masked;

			if (beganInBlock) {
				// the line's own whitespace is literal content, so what follows anchors on its structural place
				final int structural;
				if (frame == null)
					structural = open.getFirst().anchor() + 2;
				else
					structural = frame.inStatement ? frame.statementIndent + 2 : frame.baseIndent();
				result.add(raw);
				indentByLine[i] = structural;
				pendingComments.clear();
				prevIndent = structural;
				prevDepth = depthAtStart;
				prevWasChain = false;
			}
			else if (i == 0) {
				indentByLine[i] = baseTabs;
				result.add("\t".repeat(baseTabs) + content);
				if (leadingClosers == 0 && !maskedContent.isEmpty()) {
					startStatement(root, maskedContent, 0);
					root.statementIndent = baseTabs;
				}
				prevIndent = baseTabs;
				prevDepth = depthAtStart;
				prevWasChain = false;
			}
			else if (content.isEmpty()) {
				result.add("");
				indentByLine[i] = baseTabs;
			}
			else {
				final var chainIndent = prevWasChain && prevDepth == depthAtStart ? prevIndent : prevIndent + 2;
				final int indent;
				if (leadingClosers > 0 && !open.isEmpty()) {
					final var pops = Math.min(leadingClosers, open.size());
					final var it = open.iterator();
					var outermostClosed = it.next();
					for (var k = 1; k < pops; ++k)
						outermostClosed = it.next();
					indent = outermostClosed.anchor();
					// a chain link after the closer continues the chain its opener line belonged to
					final var opener = outermostClosed.openerLine();
					prevIndent = indentByLine[opener];
					prevDepth = depthByLine[opener];
					prevWasChain = chainByLine[opener];
				}
				else {
					if (leadingClosers > 0)
						indent = baseTabs;
					else if (frame == null)
						indent = isChain ? chainIndent : open.getFirst().anchor() + 2;
					else if (maskedContent.isEmpty())
						indent = frame.inStatement ? frame.statementIndent + 2 : frame.baseIndent();
					else if (frame.inStatement) {
						if (isChain)
							indent = chainIndent;
						else if (maskedContent.startsWith("{") && braceKind("", "", frame, -1) != BraceKind.LIST)
							indent = frame.statementIndent;
						else
							indent = frame.statementIndent + 2;
					}
					else
						indent = startStatement(frame, maskedContent, i);
					prevIndent = indent;
					prevDepth = depthAtStart;
					prevWasChain = isChain;
				}
				indentByLine[i] = indent;
				result.add("\t".repeat(indent) + content);

				// a comment line between statements takes the indent of the statement it precedes
				if (maskedContent.isEmpty() && frame != null && !frame.inStatement) {
					if (pendingComments.isEmpty())
						pendingFrame = frame;
					pendingComments.add(i);
				}
				else {
					if (frame == pendingFrame && leadingClosers == 0) {
						for (var comment : pendingComments) {
							indentByLine[comment] = indent;
							result.set(comment, "\t".repeat(indent) + expanded.get(comment).strip());
						}
					}
					pendingComments.clear();
				}
			}

			final var leadEnd = beganInBlock || leadingClosers == 0 ? -1 : masked.length() - masked.stripLeading().length() + leadingClosers;
			var closedBlock = false;
			var closedParenColumn = -1;
			var closedParenLine = -1;
			for (var c = 0; c < masked.length(); ++c) {
				if (c == leadEnd) {
					final var resumed = frameAt(open, root);
					final var rest = masked.substring(c).strip();
					if (resumed != null && closedBlock && RESUMING_KEYWORD.matcher(rest).find())
						beginSegment(resumed, rest, indentByLine[i], i);
				}
				final var ch = masked.charAt(c);
				if (isOpener(ch)) {
					final var enclosing = frameAt(open, root);
					if (ch == '{') {
						final var prefix = masked.substring(0, c).strip();
						var parenOwner = parenOwnerOf(prefix);
						if (parenOwner.isEmpty() && prefix.endsWith(")") && closedParenLine >= 0)
							parenOwner = maskedByLine[closedParenLine].substring(0, closedParenColumn);
						final var kind = braceKind(prefix, parenOwner, enclosing, i);
						final var completes = kind == BraceKind.BLOCK || kind == BraceKind.ENUM;
						final var anchor = completes && enclosing != null && enclosing.inStatement ? enclosing.statementIndent : indentByLine[i];
						open.push(new OpenBracket(anchor, completes, new Frame(anchor + 1, kind), i, c));
					}
					else {
						open.push(new OpenBracket(indentByLine[i], false, null, i, c));
						if (enclosing != null && enclosing.headerParen == HeaderParen.WAITING) {
							enclosing.headerParen = HeaderParen.OPEN;
							enclosing.headerParenDepth = open.size();
						}
					}
				}
				else if (isCloser(ch) && !open.isEmpty()) {
					final var closed = open.pop();
					closedBlock = closed.completes();
					final var enclosing = frameAt(open, root);
					if (closed.frame() == null) {
						closedParenColumn = closed.openerColumn();
						closedParenLine = closed.openerLine();
					}
					if (closed.frame() == null && enclosing != null && enclosing.headerParen == HeaderParen.OPEN && open.size() == enclosing.headerParenDepth - 1) {
						enclosing.headerParen = HeaderParen.CLOSED;
						enclosing.headerCloseColumn = c;
						enclosing.headerCloseLine = i;
					}
				}
				else if (ch == ';') {
					final var enclosing = frameAt(open, root);
					if (enclosing != null)
						enclosing.enumConstants = false;
				}
			}
			final var ending = frameAt(open, root);
			if (ending != null)
				finishLine(ending, masked, i, closedBlock);
			state = JavaLineScanner.stateAfter(raw, state);
		}
		return result;
	}

	/**
	 * Splits a line that begins inside a text block / block comment, closes that literal, and carries a
	 * trailing run of closing brackets (e.g. {@code text"""; });}) into two: the literal-closing part
	 * (kept verbatim, with any statement terminator) and the closing-bracket run, so the latter lands on
	 * its own line at its canonical depth. Lines without that exact shape are returned unchanged, so a
	 * verbatim argument ending in {@code ,} or a bare closing delimiter is never touched.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> splitTrailingClosersAfterLiteral(@Nonnull List<String> lines, @Nonnull LexerState initial) {
		final var expanded = new ArrayList<String>(lines.size());
		var state = initial;
		for (var line : lines) {
			final var beganInBlock = state.inTextBlock() || state.inBlockComment();
			final var next = JavaLineScanner.stateAfter(line, state);
			if (beganInBlock) {
				final var closeAt = JavaLineScanner.multilineLiteralCloseIndex(line, state);
				if (closeAt >= 0) {
					final var trailing = line.substring(closeAt);
					final var splitAt = trailingCloserSplit(trailing);
					if (splitAt >= 0) {
						expanded.add((line.substring(0, closeAt) + trailing.substring(0, splitAt)).stripTrailing());
						expanded.add(trailing.substring(splitAt));
						state = next;
						continue;
					}
				}
			}
			expanded.add(line);
			state = next;
		}
		return expanded;
	}

	/**
	 * Begins a statement on a line directly inside {@code frame} and returns its indent: a case label at
	 * the block's body level, an {@code else}/{@code catch}/{@code finally}/do-{@code while} at the
	 * statement it belongs to, an Allman {@code &#123;} at its header's level, anything else at the
	 * current body level. A lone statement label leaves the next line to start the statement.
	 */
	@CheckReturnValue
	private static int startStatement(@Nonnull Frame frame, @Nonnull String text, int line) {
		if (CASE_LABEL.matcher(text).find()) {
			frame.caseBody = isColonLabel(text);
			frame.levels.clear();
			beginSegment(frame, text, frame.bodyIndent, line);
			return frame.bodyIndent;
		}

		final var label = STATEMENT_LABEL.matcher(text);
		final var body = label.find() ? text.substring(label.end()).strip() : text;
		if (body.isEmpty())
			return frame.baseIndent();

		final var word = LEADING_WORD.matcher(body);
		final var owner = switch (word.find() ? word.group() : "") {
			case "catch", "finally" -> attach(frame, TRY_OWNERS);
			case "else" -> attach(frame, IF_OWNERS);
			case "while" -> attach(frame, DO_OWNERS);
			default -> -1;
		};
		if (owner >= 0) {
			beginSegment(frame, body, owner, line);
			return owner;
		}
		if (frame.headerPending && body.startsWith("{")) {
			final var header = frame.levels.pop();
			beginSegment(frame, body, header.indent(), line);
			frame.keyword = header.keyword();
			return header.indent();
		}
		final var indent = frame.baseIndent();
		beginSegment(frame, body, indent, line);
		return indent;
	}

	/**
	 * The index in {@code trailing} at which a pure closing-bracket run begins, or -1 if {@code trailing}
	 * is not {@code [statement terminators]} followed by a run of only {@code }})];,} and whitespace that
	 * contains at least one {@code &#125;}. Requiring a brace keeps this to a block/lambda close (e.g. a
	 * value lambda's {@code });}); a bare call-closing {@code )} after a text-block argument is left inline.
	 */
	@CheckReturnValue
	private static int trailingCloserSplit(@Nonnull String trailing) {
		var closerStart = -1;
		for (var i = 0; i < trailing.length(); ++i) {
			final var c = trailing.charAt(i);
			if (c == '}' || c == ')') {
				closerStart = i;
				break;
			}
			if (c != ';' && c != ',' && !Character.isWhitespace(c))
				return -1;
		}
		if (closerStart < 0)
			return -1;
		var hasBrace = false;
		for (var i = closerStart; i < trailing.length(); ++i) {
			final var c = trailing.charAt(i);
			if (c != '}' && c != ')' && c != ']' && c != ';' && c != ',' && !Character.isWhitespace(c))
				return -1;
			if (c == '}')
				hasBrace = true;
		}
		return hasBrace ? closerStart : -1;
	}

	private JavaSpanReindenter() {
	}
}