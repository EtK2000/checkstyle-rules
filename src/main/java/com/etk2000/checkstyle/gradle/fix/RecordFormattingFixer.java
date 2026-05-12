package com.etk2000.checkstyle.gradle.fix;

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
	@CheckReturnValue
	@Nullable
	private static FixAttempt collapseEmptyBodyClose(@Nonnull List<String> lines, int lineIndex, int column) {
		final var rcurlyLine = lines.get(lineIndex);
		if (!stripTrailingWhitespace(rcurlyLine.substring(0, column)).isEmpty()
				|| !rcurlyLine.substring(column + 1).isBlank())
			return null;

		var lcurlyLineIdx = -1;
		var lcurlyCol = -1;
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var prev = lines.get(i);
			final var col = findLastOpenBrace(prev);
			if (col >= 0) {
				if (!prev.substring(col + 1).isBlank())
					return null;
				lcurlyLineIdx = i;
				lcurlyCol = col;
				break;
			}
			if (!prev.isBlank())
				return null;
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
		var i = from;
		while (i < to) {
			final var skipped = skipNonCode(line, i);
			if (skipped > i) {
				if (i + 1 < line.length() && line.charAt(i) == '/' && line.charAt(i + 1) == '/')
					return true;
				i = skipped;
				continue;
			}
			++i;
		}
		return false;
	}

	/** True if the line ends inside a {@code //} or unterminated {@code /*} or text block. */
	@CheckReturnValue
	private static boolean endsInComment(@Nonnull String line) {
		var i = 0;
		while (i < line.length()) {
			final var c = line.charAt(i);
			if (c == '"' && isTextBlockStart(line, i)) {
				final var end = skipTextBlock(line, i);
				if (end < 0)
					return true;
				i = end;
			}
			else if (c == '"' || c == '\'') {
				final var end = skipLiteral(line, i, c);
				if (end < 0)
					return true;
				i = end;
			}
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = findBlockCommentEnd(line, i + 2);
				if (end < 0)
					return true;
				i = end;
			}
			else
				++i;
		}
		return false;
	}

	@CheckReturnValue
	private static int findBlockCommentEnd(@Nonnull String line, int from) {
		for (var i = from; i + 1 < line.length(); ++i) {
			if (line.charAt(i) == '*' && line.charAt(i + 1) == '/')
				return i + 2;
		}
		return -1;
	}

	/** Column of the last {@code &#123;} not inside a string/char/comment/text-block, or -1. */
	@CheckReturnValue
	private static int findLastOpenBrace(@Nonnull String line) {
		var last = -1;
		var i = 0;
		while (i < line.length()) {
			final var c = line.charAt(i);
			if (c == '"' && isTextBlockStart(line, i)) {
				final var end = skipTextBlock(line, i);
				if (end < 0)
					return last;
				i = end;
			}
			else if (c == '"' || c == '\'') {
				final var end = skipLiteral(line, i, c);
				if (end < 0)
					return last;
				i = end;
			}
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return last;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = findBlockCommentEnd(line, i + 2);
				if (end < 0)
					return last;
				i = end;
			}
			else if (c == '{') {
				last = i;
				++i;
			}
			else
				++i;
		}
		return last;
	}

	/**
	 * Column of the {@code &#123;} matching the {@code &#125;} at {@code closeCol}, scanning
	 * forward with awareness of string/char literals, text blocks, and comments. Returns -1
	 * if no match (also when an unterminated literal/comment prevents safe matching).
	 */
	@CheckReturnValue
	private static int findMatchingOpenBrace(@Nonnull String line, int closeCol) {
		final var stack = new ArrayDeque<Integer>();
		var i = 0;
		while (i < closeCol) {
			final var c = line.charAt(i);
			if (c == '"' && isTextBlockStart(line, i)) {
				final var end = skipTextBlock(line, i);
				if (end < 0)
					return -1;
				i = end;
			}
			else if (c == '"' || c == '\'') {
				final var end = skipLiteral(line, i, c);
				if (end < 0)
					return -1;
				i = end;
			}
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return -1;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = findBlockCommentEnd(line, i + 2);
				if (end < 0)
					return -1;
				i = end;
			}
			else if (c == '{') {
				stack.push(i);
				++i;
			}
			else if (c == '}') {
				if (!stack.isEmpty())
					stack.pop();
				++i;
			}
			else
				++i;
		}
		return stack.isEmpty() ? -1 : stack.pop();
	}

	/**
	 * Finds the matching {@code )} for the {@code (} at {@code (lparenLineIdx, lparenCol)},
	 * with awareness of literals and comments. Bails (returns null) when a text block spans
	 * multiple lines, since cross-line text-block state can't be safely tracked here.
	 */
	@CheckReturnValue
	@Nullable
	private static int[] findMatchingRparen(@Nonnull List<String> lines, int lparenLineIdx, int lparenCol) {
		var depth = 1;
		for (var lineIdx = lparenLineIdx; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			var i = lineIdx == lparenLineIdx ? lparenCol + 1 : 0;
			while (i < line.length()) {
				final var c = line.charAt(i);
				if (c == '"' && isTextBlockStart(line, i)) {
					final var end = skipTextBlock(line, i);
					if (end < 0)
						return null;
					i = end;
				}
				else if (c == '"' || c == '\'') {
					final var end = skipLiteral(line, i, c);
					if (end < 0)
						return null;
					i = end;
				}
				else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
					break;
				else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
					final var end = findBlockCommentEnd(line, i + 2);
					if (end < 0)
						return null;
					i = end;
				}
				else if (c == '(') {
					++depth;
					++i;
				}
				else if (c == ')') {
					--depth;
					if (depth == 0)
						return new int[]{lineIdx, i};
					++i;
				}
				else
					++i;
			}
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
		var i = 0;
		while (i < line.length()) {
			final var skipped = skipNonCode(line, i);
			if (skipped > i) {
				i = skipped;
				continue;
			}
			if (i + 6 <= line.length() && line.startsWith("record", i)
					&& (i == 0 || !Character.isJavaIdentifierPart(Character.codePointBefore(line, i)))
					&& (i + 6 == line.length() || Character.isWhitespace(line.charAt(i + 6))))
				return i;
			++i;
		}
		return -1;
	}

	/**
	 * Column of {@code (} that opens the record-component list, scanned from {@code from},
	 * tracking generic angle-bracket depth and skipping literals/comments. Returns -1 if not found.
	 */
	@CheckReturnValue
	private static int findRecordLparen(@Nonnull String line, int from) {
		var parenDepth = 0;
		var angleDepth = 0;
		var i = from;
		while (i < line.length()) {
			final var skipped = skipNonCode(line, i);
			if (skipped > i) {
				i = skipped;
				continue;
			}
			final var c = line.charAt(i);
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
			++i;
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

		final var rparenPos = findMatchingRparen(lines, recordLineIdx, lparenCol);
		if (rparenPos == null)
			return null;
		final var rparenLineIdx = rparenPos[0];
		final var rparenCol = rparenPos[1];

		// Bail if any line in the header range contains a `//` line comment, since collapsing
		// would either swallow code into the comment or silently drop the comment.
		if (recordLineIdx != rparenLineIdx) {
			if (containsLineComment(recordLine, lparenCol + 1, recordLine.length()))
				return null;
			for (var i = recordLineIdx + 1; i < rparenLineIdx; ++i) {
				if (containsLineComment(lines.get(i), 0, lines.get(i).length()))
					return null;
			}
			if (containsLineComment(lines.get(rparenLineIdx), 0, rparenCol))
				return null;
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
		final var indent = leadingWhitespace(recordLine);
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
		// Refuse to append ` {` to a line that ends inside a comment; the `{` would be swallowed.
		if (endsInComment(trailing))
			return null;

		final var afterBrace = line.substring(column);
		final var newAnchorLine = trailing + " " + afterBrace;
		if (anchorLineIdx == lineIndex && newAnchorLine.equals(line))
			return null;
		final var replacement = new ArrayList<String>();
		replacement.add(newAnchorLine);
		return new FixResult(anchorLineIdx, lineIndex, replacement);
	}

	@CheckReturnValue
	private static boolean isTextBlockStart(@Nonnull String line, int from) {
		return from + 2 < line.length()
				&& line.charAt(from) == '"' && line.charAt(from + 1) == '"' && line.charAt(from + 2) == '"';
	}

	@CheckReturnValue
	@Nonnull
	private static String leadingWhitespace(@Nonnull String s) {
		var i = 0;
		while (i < s.length() && Character.isWhitespace(s.charAt(i)))
			++i;
		return s.substring(0, i);
	}

	/**
	 * Skips a string or char literal starting at {@code start}. Returns the index past the closing
	 * quote, or -1 if the literal does not terminate on this line.
	 */
	@CheckReturnValue
	private static int skipLiteral(@Nonnull String line, int start, char quote) {
		var i = start + 1;
		while (i < line.length() && line.charAt(i) != quote) {
			if (line.charAt(i) == '\\' && i + 1 < line.length())
				++i;
			++i;
		}
		return i < line.length() ? i + 1 : -1;
	}

	/**
	 * If {@code line[i]} opens a literal or comment, returns the index past its close (or
	 * {@code line.length()} if unterminated). Otherwise returns {@code i}, signaling the caller
	 * to process the character itself.
	 */
	@CheckReturnValue
	private static int skipNonCode(@Nonnull String line, int i) {
		final var c = line.charAt(i);
		if (c == '"' && isTextBlockStart(line, i)) {
			final var end = skipTextBlock(line, i);
			return end < 0 ? line.length() : end;
		}
		if (c == '"' || c == '\'') {
			final var end = skipLiteral(line, i, c);
			return end < 0 ? line.length() : end;
		}
		if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
			return line.length();
		if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
			final var end = findBlockCommentEnd(line, i + 2);
			return end < 0 ? line.length() : end;
		}
		return i;
	}

	/**
	 * Skips a text block ({@code """..."""}) starting at {@code start}. Returns the index after
	 * the closing {@code """}, or -1 if the text block does not terminate on this line.
	 */
	@CheckReturnValue
	private static int skipTextBlock(@Nonnull String line, int start) {
		var i = start + 3;
		while (i + 2 < line.length()) {
			if (line.charAt(i) == '\\' && i + 1 < line.length()) {
				i += 2;
				continue;
			}
			if (line.charAt(i) == '"' && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"')
				return i + 3;
			++i;
		}
		return -1;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> splitComponents(@Nonnull String inner) {
		final var parts = new ArrayList<String>();
		var parenDepth = 0;
		var angleDepth = 0;
		var start = 0;
		var i = 0;
		while (i < inner.length()) {
			final var c = inner.charAt(i);
			if (c == '"' && isTextBlockStart(inner, i)) {
				final var end = skipTextBlock(inner, i);
				if (end < 0)
					return List.of();
				i = end;
				continue;
			}
			if (c == '"' || c == '\'') {
				final var end = skipLiteral(inner, i, c);
				if (end < 0)
					return List.of();
				i = end;
				continue;
			}
			if (c == '/' && i + 1 < inner.length() && inner.charAt(i + 1) == '/')
				return List.of();
			if (c == '/' && i + 1 < inner.length() && inner.charAt(i + 1) == '*') {
				final var end = findBlockCommentEnd(inner, i + 2);
				if (end < 0)
					return List.of();
				i = end;
				continue;
			}
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
			++i;
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

		final var indent = leadingWhitespace(beforeOpen);
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
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
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