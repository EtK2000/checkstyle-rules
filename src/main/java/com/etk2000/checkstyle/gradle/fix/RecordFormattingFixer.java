package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes record formatting violations:
 * <ul>
 *   <li>Brace formatting: collapses whitespace/newlines between the anchor token (closing paren
 *       or end of implements clause) and the opening brace to exactly one space; joins empty-body
 *       braces; splits non-empty single-line bodies.</li>
 *   <li>Component layout: rebuilds the record header to either single-line form (if it fits in
 *       {@link LineLength#MAX_LINE_LENGTH} columns) or multi-line form with each component on
 *       its own line.</li>
 * </ul>
 */
class RecordFormattingFixer implements CheckstyleFixer {
	/**
	 * Outcome of {@link #findMatchingRparen}: the matched {@code )} position, or (when
	 * {@code multilineSpan} is set) the signal that a block comment or text block spans past a
	 * header line, which makes cross-line paren matching unsafe.
	 */
	private record RparenScan(int lineIdx, int col, boolean multilineSpan) {}

	// Appended to a line to probe its end-of-line lexer state in endsInComment. Must be a character
	// that cannot open a string, char, comment, or text block (i.e. not '"' or '/'), so it is blanked
	// by the mask only when the line already ends inside a literal or comment.
	private static final char PROBE_SENTINEL = 'X';

	private static final LexerState IN_BLOCK_COMMENT = new LexerState(true, false);

	private static final String SKIP_EMPTY_BODY_CONTENT = "cannot collapse empty record body without losing surrounding content";
	private static final String SKIP_HEADER_LINE_COMMENT = "cannot reformat a record header that contains a line comment";
	private static final String SKIP_HEADER_MULTILINE_SPAN = "cannot reformat a record header that spans a multi-line comment or text block";
	private static final String SKIP_OPEN_BRACE_ANCHOR_COMMENT = "anchor line ends in a comment or unterminated literal";

	@CheckReturnValue
	@Nullable
	private static FixAttempt collapseEmptyBodyClose(@Nonnull List<String> lines, int lineIndex, int column) {
		final var rcurlyLine = lines.get(lineIndex);
		if (!stripTrailingWhitespace(rcurlyLine.substring(0, column)).isEmpty()
				|| !rcurlyLine.substring(column + 1).isBlank())
			return new SkipResult(SKIP_EMPTY_BODY_CONTENT);

		// Mask the whole file once so the backward brace scan reads each line with its true cross-line
		// lexer state: a `{` on a line interior to a multi-line block comment or text block is blanked,
		// not mistaken for the record's opening brace.
		final var maskedLines = JavaLineScanner.maskAll(lines);
		var lcurlyLineIdx = -1;
		var lcurlyCol = -1;
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var prev = lines.get(i);
			final var col = maskedLines.get(i).lastIndexOf('{');
			if (col >= 0) {
				if (!prev.substring(col + 1).isBlank())
					return new SkipResult(SKIP_EMPTY_BODY_CONTENT);
				lcurlyLineIdx = i;
				lcurlyCol = col;
				break;
			}
			if (!prev.isBlank())
				return new SkipResult(SKIP_EMPTY_BODY_CONTENT);
		}
		if (lcurlyLineIdx < 0)
			return null;

		final var lcurlyLine = lines.get(lcurlyLineIdx);
		final var newLine = lcurlyLine.substring(0, lcurlyCol) + "{}";
		return new FixResult(lcurlyLineIdx, lineIndex, List.of(newLine));
	}

	/** True if {@code line[from..to)} contains an unenclosed {@code //} (not inside literal/comment). */
	@CheckReturnValue
	private static boolean containsLineComment(@Nonnull String line, int from, int to) {
		var offset = from;
		while (offset < to) {
			final var rel = JavaLineScanner.firstCommentMarker(line.substring(offset), LexerState.NONE);
			if (rel < 0)
				return false;
			final var col = offset + rel;
			if (col >= to)
				return false;
			if (line.charAt(col + 1) == '/')
				return true;
			final var closeRel = JavaLineScanner.multilineLiteralCloseIndex(line.substring(col + 2), IN_BLOCK_COMMENT);
			if (closeRel < 0)
				return false;
			offset = col + 2 + closeRel;
		}
		return false;
	}

	/**
	 * True if the line ends inside a {@code //}, or an unterminated {@code /*}, string, char, or
	 * text block. The {@link #PROBE_SENTINEL} appended to the line is blanked by
	 * {@link JavaLineScanner#stripCommentsAndStrings} exactly when the end-of-line lexer position
	 * is inside a literal or comment, and survives when it is code.
	 */
	@CheckReturnValue
	private static boolean endsInComment(@Nonnull String line) {
		return JavaLineScanner.stripCommentsAndStrings(line + PROBE_SENTINEL, LexerState.NONE).charAt(line.length()) == ' ';
	}

	/**
	 * Column of the {@code &#123;} matching the {@code &#125;} at {@code closeCol}, scanning
	 * forward with awareness of string/char literals, text blocks, and comments. Returns -1
	 * if no match (also when an unterminated literal/comment prevents safe matching).
	 */
	@CheckReturnValue
	private static int findMatchingOpenBrace(@Nonnull String line, int closeCol) {
		final var prefix = line.substring(0, closeCol);
		// An unterminated literal/comment or a `//` before the `}` swallows it: the group can't be
		// matched safely.
		if (endsInComment(prefix))
			return -1;
		final var masked = JavaLineScanner.stripCommentsAndStrings(prefix, LexerState.NONE);
		final var stack = new ArrayDeque<Integer>();
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '{')
				stack.push(i);
			else if (c == '}' && !stack.isEmpty())
				stack.pop();
		}
		return stack.isEmpty() ? -1 : stack.pop();
	}

	/**
	 * Finds the matching {@code )} for the {@code (} at {@code (lparenLineIdx, lparenCol)},
	 * with awareness of literals and comments. Signals {@link RparenScan#multilineSpan()} when a
	 * block comment or text block spills to the next line, since cross-line state can't be safely
	 * tracked here; returns null when no match is found at all (also on an unterminated string/char).
	 */
	@CheckReturnValue
	@Nullable
	private static RparenScan findMatchingRparen(@Nonnull List<String> lines, int lparenLineIdx, int lparenCol) {
		var depth = 1;
		for (var lineIdx = lparenLineIdx; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			final var start = lineIdx == lparenLineIdx ? lparenCol + 1 : 0;
			final var masked = JavaLineScanner.stripCommentsAndStrings(line, LexerState.NONE);
			for (var i = start; i < masked.length(); ++i) {
				final var c = masked.charAt(i);
				if (c == '(')
					++depth;
				else if (c == ')') {
					--depth;
					if (depth == 0)
						return new RparenScan(lineIdx, i, false);
				}
			}
			// Closing paren wasn't on this line. Each line is scanned fresh (no cross-line literal
			// state), so a block comment or text block that spills to the next line can't be tracked:
			// signal the span. An unterminated string/char bails to null; a trailing `//` is not a
			// spill (it just ends the line), so continue past it.
			final var end = JavaLineScanner.stateAfter(line, LexerState.NONE);
			if (end.inBlockComment() || end.inTextBlock())
				return new RparenScan(-1, -1, true);
			if (endsInComment(line) && !containsLineComment(line, start, line.length()))
				return null;
		}
		return null;
	}

	/**
	 * Column where {@code record} begins on this line (only if it's the keyword: preceded by
	 * non-identifier or line start, followed by whitespace or end of line, and not inside a
	 * literal/comment). Returns -1 if not found. Uses {@link Character#codePointBefore} so a
	 * supplementary identifier character (surrogate pair) preceding {@code record} is correctly
	 * recognized as an identifier part rather than its low surrogate alone.
	 */
	@CheckReturnValue
	private static int findRecordKeywordColumn(@Nonnull String line) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, LexerState.NONE);
		for (var i = 0; i + 6 <= masked.length(); ++i) {
			if (masked.startsWith("record", i)
					&& (i == 0 || !Character.isJavaIdentifierPart(Character.codePointBefore(masked, i)))
					&& (i + 6 == masked.length() || Character.isWhitespace(masked.charAt(i + 6))))
				return i;
		}
		return -1;
	}

	/**
	 * Column of {@code (} that opens the record-component list, scanned from {@code from},
	 * tracking generic angle-bracket depth and skipping literals/comments. Returns -1 if not found.
	 */
	@CheckReturnValue
	private static int findRecordLparen(@Nonnull String line, int from) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, LexerState.NONE);
		var parenDepth = 0;
		var angleDepth = 0;
		for (var i = from; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' && parenDepth == 0 && angleDepth == 0)
				return i;
			if (c == '(')
				++parenDepth;
			else if (c == ')' && parenDepth > 0)
				--parenDepth;
			else if (c == '<' && parenDepth == 0)
				++angleDepth;
			else if (c == '>' && parenDepth == 0 && angleDepth > 0)
				--angleDepth;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixCloseBrace(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column >= line.length() || line.charAt(column) != '}')
			return null;

		final var openOnSameLine = findMatchingOpenBrace(line, column);
		if (openOnSameLine >= 0)
			return splitSameLineBody(lines, lineIndex, openOnSameLine, column);

		return collapseEmptyBodyClose(lines, lineIndex, column);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixComponentLayout(@Nonnull List<String> lines, int lineIndex) {
		var recordLineIdx = -1;
		var recordCol = -1;
		for (var i = lineIndex; i >= 0; --i) {
			final var col = findRecordKeywordColumn(lines.get(i));
			if (col >= 0) {
				recordLineIdx = i;
				recordCol = col;
				break;
			}
		}
		if (recordLineIdx < 0)
			return null;

		final var recordLine = lines.get(recordLineIdx);
		final var lparenCol = findRecordLparen(recordLine, recordCol + 6);
		if (lparenCol < 0)
			return null;

		final var rparen = findMatchingRparen(lines, recordLineIdx, lparenCol);
		if (rparen == null)
			return null;
		if (rparen.multilineSpan())
			return new SkipResult(SKIP_HEADER_MULTILINE_SPAN);
		final var rparenLineIdx = rparen.lineIdx();
		final var rparenCol = rparen.col();

		// Bail if any line in the header range contains a `//` line comment, since collapsing would
		// either swallow code into the comment or silently drop the comment. findMatchingRparen
		// deliberately scans past a trailing `//` (it is not a multi-line spill), so this is the only
		// guard that stops a header line comment from being collapsed away.
		if (recordLineIdx != rparenLineIdx) {
			if (containsLineComment(recordLine, lparenCol + 1, recordLine.length()))
				return new SkipResult(SKIP_HEADER_LINE_COMMENT);
			for (var i = recordLineIdx + 1; i < rparenLineIdx; ++i) {
				if (containsLineComment(lines.get(i), 0, lines.get(i).length()))
					return new SkipResult(SKIP_HEADER_LINE_COMMENT);
			}
			if (containsLineComment(lines.get(rparenLineIdx), 0, rparenCol))
				return new SkipResult(SKIP_HEADER_LINE_COMMENT);
		}

		final var inner = new StringBuilder();
		if (recordLineIdx == rparenLineIdx)
			inner.append(recordLine, lparenCol + 1, rparenCol);
		else {
			inner.append(recordLine.substring(lparenCol + 1).trim());
			for (var i = recordLineIdx + 1; i < rparenLineIdx; ++i) {
				if (!inner.isEmpty())
					inner.append(' ');
				inner.append(lines.get(i).trim());
			}
			final var lastPart = lines.get(rparenLineIdx).substring(0, rparenCol).trim();
			if (!lastPart.isEmpty()) {
				if (!inner.isEmpty())
					inner.append(' ');
				inner.append(lastPart);
			}
		}

		final var components = splitComponents(inner.toString().trim());
		if (components.isEmpty())
			return null;

		final var prefix = recordLine.substring(0, lparenCol);
		final var suffix = lines.get(rparenLineIdx).substring(rparenCol + 1);
		final var indent = LineText.extractIndent(recordLine);
		final var original = lines.subList(recordLineIdx, rparenLineIdx + 1);

		final var collapsed = prefix + "(" + String.join(", ", components) + ")" + suffix;
		if (LineLength.tabExpandedLength(collapsed) <= LineLength.MAX_LINE_LENGTH) {
			final var single = List.of(collapsed);
			return single.equals(original) ? null : new FixResult(recordLineIdx, rparenLineIdx, single);
		}

		final var compIndent = indent + "\t\t";
		final var result = new ArrayList<String>();
		result.add(prefix + "(");
		for (var i = 0; i < components.size(); ++i)
			result.add(compIndent + components.get(i) + (i < components.size() - 1 ? "," : ""));
		result.add(indent + ")" + suffix);
		return result.equals(original) ? null : new FixResult(recordLineIdx, rparenLineIdx, result);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixOpenBrace(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column >= line.length() || line.charAt(column) != '{')
			return null;

		var anchorLineIdx = lineIndex;
		var anchorPrefix = line.substring(0, column);
		var trailing = stripTrailingWhitespace(anchorPrefix);
		while (trailing.isEmpty() && anchorLineIdx > 0) {
			--anchorLineIdx;
			anchorPrefix = lines.get(anchorLineIdx);
			trailing = stripTrailingWhitespace(anchorPrefix);
		}
		if (trailing.isEmpty())
			return null;
		// Refuse to append ` {` to a line that ends inside a comment or unterminated literal; the `{`
		// would be swallowed.
		if (endsInComment(trailing))
			return new SkipResult(SKIP_OPEN_BRACE_ANCHOR_COMMENT);

		final var afterBrace = line.substring(column);
		final var newAnchorLine = trailing + " " + afterBrace;
		if (anchorLineIdx == lineIndex && newAnchorLine.equals(line))
			return null;
		final var replacement = new ArrayList<String>();
		replacement.add(newAnchorLine);
		return new FixResult(anchorLineIdx, lineIndex, replacement);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> splitComponents(@Nonnull String inner) {
		// An unterminated string/char/comment/text-block or a `//` leaves the header in a state we
		// can't split safely.
		if (endsInComment(inner))
			return List.of();
		// Split at commas located in the masked text (literal/comment commas blanked), but slice the
		// components from the original so their literal/comment content survives verbatim.
		final var masked = JavaLineScanner.stripCommentsAndStrings(inner, LexerState.NONE);
		final var parts = new ArrayList<String>();
		var parenDepth = 0;
		var angleDepth = 0;
		var start = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			// Inside annotation parens, ignore `<`/`>` (they may be comparison/shift operators).
			// Outside parens, `<`/`>` are generic type-arg brackets.
			if (c == '(')
				++parenDepth;
			else if (c == ')' && parenDepth > 0)
				--parenDepth;
			else if (c == '<' && parenDepth == 0)
				++angleDepth;
			else if (c == '>' && parenDepth == 0 && angleDepth > 0)
				--angleDepth;
			else if (c == ',' && parenDepth == 0 && angleDepth == 0) {
				parts.add(inner.substring(start, i).trim());
				start = i + 1;
			}
		}
		// `angleDepth != 0` is reachable (e.g. `List<String x`); `parenDepth != 0` is retained as
		// defense in depth in case a future caller bypasses `findMatchingRparen`'s paren matching.
		if (parenDepth != 0 || angleDepth != 0)
			return List.of();
		final var last = inner.substring(start).trim();
		if (last.isEmpty() && !parts.isEmpty())
			return List.of();
		if (!last.isEmpty())
			parts.add(last);
		// Leading (`,a,b`) or middle (`a,,b`) commas produce empty fragments before the trailing
		// one. The trailing check above doesn't see them; bail on any empty part.
		for (var part : parts) {
			if (part.isEmpty())
				return List.of();
		}
		return parts;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt splitSameLineBody(@Nonnull List<String> lines, int lineIndex, int openCol, int closeCol) {
		final var line = lines.get(lineIndex);
		final var beforeOpen = line.substring(0, openCol);
		final var body = line.substring(openCol + 1, closeCol).trim();
		final var afterClose = line.substring(closeCol + 1);

		final var indent = LineText.extractIndent(beforeOpen);
		final var bodyIndent = indent + "\t";

		final var replacement = new ArrayList<String>();
		replacement.add(beforeOpen + "{");
		if (!body.isEmpty())
			replacement.add(bodyIndent + body);
		replacement.add(indent + "}" + afterClose);
		return new FixResult(lineIndex, lineIndex, replacement);
	}

	@CheckReturnValue
	@Nonnull
	private static String stripTrailingWhitespace(@Nonnull String s) {
		var end = s.length();
		while (end > 0 && Character.isWhitespace(s.charAt(end - 1)))
			--end;
		return s.substring(0, end);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var c = line.charAt(column);
		if (c == '{')
			return fixOpenBrace(lines, lineIndex, column);
		if (c == '}')
			return fixCloseBrace(lines, lineIndex, column);
		return fixComponentLayout(lines, lineIndex);
	}
}