package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ArrayTypeStyleFixer implements CheckstyleFixer {
	/**
	 * Java keywords that introduce a parenthesized clause that is NOT a parameter
	 * list (so its commas are not parameter separators). For-loop init in particular
	 * uses comma to separate multi-variable declarations sharing a single type, which
	 * means {@code int x[] = a, y = 1} must NOT be treated as a parameter list.
	 */
	private static final Set<String> NON_PARAM_LIST_KEYWORDS = Set.of(
			"catch", "do", "for", "if", "switch", "synchronized", "try", "while"
	);

	/**
	 * Java keywords that can appear immediately before a type-name-like identifier but
	 * are NOT themselves types. If walking back from a candidate IDENT lands on one of
	 * these, the declaration shape is malformed (or the IDENT is actually an exception
	 * name in a {@code throws} clause, etc.) and the fixer must bail.
	 */
	private static final Set<String> NON_TYPE_KEYWORDS = Set.of(
			"extends", "implements", "permits", "super", "throws"
	);

	private static final String THROWS_KEYWORD = "throws";

	/**
	 * Inserts {@code brackets} into {@code rawDeclLine} at the type-end position computed
	 * from {@code blankedDeclLine} ending at {@code prevPreBracket}. Returns null if the
	 * declaration shape is not recognized (multi-var, annotation-prefixed, etc.).
	 */
	@CheckReturnValue
	@Nullable
	private static String applyBracketsToDeclaration(
			@Nonnull String rawDeclLine,
			@Nonnull String blankedDeclLine,
			int prevPreBracket,
			@Nonnull String brackets
	) {
		final int middleEnd;
		final int typeEndExclusive;
		if (blankedDeclLine.charAt(prevPreBracket) == ')') {
			final var openParen = findMatchingOpenParen(blankedDeclLine, prevPreBracket);
			if (openParen < 0)
				return null;
			final var beforeParen = skipWhitespaceBackward(blankedDeclLine, openParen - 1);
			if (beforeParen < 0)
				return null;
			if (!Character.isJavaIdentifierPart(blankedDeclLine.charAt(beforeParen)))
				return null;
			var methodIdentStart = beforeParen;
			while (methodIdentStart > 0
					&& Character.isJavaIdentifierPart(blankedDeclLine.charAt(methodIdentStart - 1)))
				--methodIdentStart;
			if (!Character.isJavaIdentifierStart(blankedDeclLine.charAt(methodIdentStart)))
				return null;
			final var beforeMethodIdent = skipWhitespaceBackward(blankedDeclLine, methodIdentStart - 1);
			if (beforeMethodIdent < 0)
				return null;
			final var typeEndChar = blankedDeclLine.charAt(beforeMethodIdent);
			if (!Character.isJavaIdentifierPart(typeEndChar) && typeEndChar != ']' && typeEndChar != '>')
				return null;
			middleEnd = prevPreBracket + 1;
			typeEndExclusive = beforeMethodIdent + 1;
		}
		else {
			if (!Character.isJavaIdentifierPart(blankedDeclLine.charAt(prevPreBracket)))
				return null;
			var identStart = prevPreBracket;
			while (identStart > 0 && Character.isJavaIdentifierPart(blankedDeclLine.charAt(identStart - 1)))
				--identStart;
			if (!Character.isJavaIdentifierStart(blankedDeclLine.charAt(identStart)))
				return null;
			final var preIdent = skipWhitespaceBackward(blankedDeclLine, identStart - 1);
			if (preIdent < 0)
				return null;
			final var preIdentChar = blankedDeclLine.charAt(preIdent);
			if (preIdentChar == ',' || preIdentChar == '@')
				return null;
			if (Character.isJavaIdentifierPart(preIdentChar)) {
				var typeWordStart = preIdent;
				while (typeWordStart > 0
						&& Character.isJavaIdentifierPart(blankedDeclLine.charAt(typeWordStart - 1)))
					--typeWordStart;
				if (NON_TYPE_KEYWORDS.contains(blankedDeclLine.substring(typeWordStart, preIdent + 1)))
					return null;
			}
			middleEnd = prevPreBracket + 1;
			typeEndExclusive = preIdent + 1;
		}

		return rawDeclLine.substring(0, typeEndExclusive)
				+ brackets
				+ rawDeclLine.substring(typeEndExclusive, middleEnd)
				+ rawDeclLine.substring(middleEnd);
	}

	/**
	 * Replaces every {@code /* ... *}{@code /} and {@code //} region in {@code s} with
	 * same-length spaces (preserving column indices). Used so the fixer's textual
	 * scanners ({@link #findEnclosingOpenParen}, {@link #hasCommaBeforeTerminator},
	 * {@link #skipWhitespaceBackward}, etc.) don't misread punctuation that lives
	 * inside comments.
	 */
	@CheckReturnValue
	@Nonnull
	private static String blankComments(@Nonnull String s) {
		final var sb = new StringBuilder(s);
		var inChar = false;
		var inString = false;
		var i = 0;
		while (i < sb.length()) {
			final var c = sb.charAt(i);
			if (inString) {
				if (c == '\\') {
					i += 2;
					continue;
				}
				if (c == '"')
					inString = false;
				++i;
				continue;
			}
			if (inChar) {
				if (c == '\\') {
					i += 2;
					continue;
				}
				if (c == '\'')
					inChar = false;
				++i;
				continue;
			}
			if (c == '"') {
				inString = true;
				++i;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				++i;
				continue;
			}
			if (c == '/' && i + 1 < sb.length()) {
				final var next = sb.charAt(i + 1);
				if (next == '/') {
					while (i < sb.length())
						sb.setCharAt(i++, ' ');
					break;
				}
				if (next == '*') {
					sb.setCharAt(i, ' ');
					sb.setCharAt(i + 1, ' ');
					i += 2;
					while (i < sb.length()) {
						if (i + 1 < sb.length() && sb.charAt(i) == '*' && sb.charAt(i + 1) == '/') {
							sb.setCharAt(i, ' ');
							sb.setCharAt(i + 1, ' ');
							i += 2;
							break;
						}
						sb.setCharAt(i++, ' ');
					}
					continue;
				}
			}
			++i;
		}
		return sb.toString();
	}

	@CheckReturnValue
	private static int findBracketsEnd(@Nonnull String line, int start) {
		var pos = start;
		var lastBracketEnd = start;
		while (pos < line.length()) {
			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;
			if (pos >= line.length() || line.charAt(pos) != '[')
				break;
			++pos;
			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;
			if (pos >= line.length() || line.charAt(pos) != ']')
				return -1;
			++pos;
			lastBracketEnd = pos;
		}
		return lastBracketEnd;
	}

	@CheckReturnValue
	private static int findEnclosingOpenParen(@Nonnull String line, int upTo) {
		final var stack = new ArrayDeque<Integer>();
		var inChar = false;
		var inString = false;
		final var bound = Math.min(upTo, line.length());
		for (var i = 0; i < bound; ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '\\')
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(')
				stack.push(i);
			else if (c == ')' && !stack.isEmpty())
				stack.pop();
		}
		return stack.isEmpty() ? -1 : stack.peek();
	}

	@CheckReturnValue
	private static int findMatchingOpenParen(@Nonnull String line, int closeParenIdx) {
		final var stack = new ArrayDeque<Integer>();
		var inChar = false;
		var inString = false;
		final var bound = Math.min(closeParenIdx, line.length() - 1);
		for (var i = 0; i <= bound; ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '\\')
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\')
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(')
				stack.push(i);
			else if (c == ')') {
				if (stack.isEmpty())
					return -1;
				final var match = stack.pop();
				if (i == closeParenIdx)
					return match;
			}
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixMultiLine(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String rawLine,
			@Nonnull String line
	) {
		if (lineIndex == 0)
			return null;
		final var rawPrevLine = lines.get(lineIndex - 1);
		final var prevLine = blankComments(rawPrevLine);
		final var prevPreBracket = skipWhitespaceBackward(prevLine, prevLine.length() - 1);
		if (prevPreBracket < 0)
			return null;

		final var bracketsEnd = findBracketsEnd(line, column);
		if (bracketsEnd < 0)
			return null;

		final var prevIsMethodReturn = isMethodReturnPrev(prevLine, prevPreBracket);
		if (!prevIsMethodReturn && hasCommaBeforeTerminator(lines, lineIndex, bracketsEnd))
			return null;

		final var fixedPrev = applyBracketsToDeclaration(
				rawPrevLine,
				prevLine,
				prevPreBracket,
				rawLine.substring(column, bracketsEnd)
		);
		if (fixedPrev == null)
			return null;

		final var leftover = rawLine.substring(0, column) + rawLine.substring(bracketsEnd);
		final var replacement = new ArrayList<String>(2);
		replacement.add(fixedPrev);
		if (!leftover.isBlank())
			replacement.add(leftover);
		return new FixResult(lineIndex - 1, lineIndex, replacement);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixSingleLine(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String rawLine,
			@Nonnull String line,
			int preBracket
	) {
		final var isMethodReturn = line.charAt(preBracket) == ')';
		final int middleEnd;
		final int typeEndExclusive;
		if (isMethodReturn) {
			final var openParen = findMatchingOpenParen(line, preBracket);
			if (openParen < 0)
				return null;
			final var beforeParen = skipWhitespaceBackward(line, openParen - 1);
			if (beforeParen < 0)
				return null;
			if (!Character.isJavaIdentifierPart(line.charAt(beforeParen)))
				return null;
			var methodIdentStart = beforeParen;
			while (methodIdentStart > 0 && Character.isJavaIdentifierPart(line.charAt(methodIdentStart - 1)))
				--methodIdentStart;
			if (!Character.isJavaIdentifierStart(line.charAt(methodIdentStart)))
				return null;
			final var beforeMethodIdent = skipWhitespaceBackward(line, methodIdentStart - 1);
			if (beforeMethodIdent < 0)
				return null;
			final var typeEndChar = line.charAt(beforeMethodIdent);
			if (!Character.isJavaIdentifierPart(typeEndChar) && typeEndChar != ']' && typeEndChar != '>')
				return null;
			middleEnd = preBracket + 1;
			typeEndExclusive = beforeMethodIdent + 1;
		}
		else {
			final var identEndExclusive = preBracket + 1;
			if (!Character.isJavaIdentifierPart(line.charAt(identEndExclusive - 1)))
				return null;
			var identStart = identEndExclusive - 1;
			while (identStart > 0 && Character.isJavaIdentifierPart(line.charAt(identStart - 1)))
				--identStart;
			if (!Character.isJavaIdentifierStart(line.charAt(identStart)))
				return null;
			final var preIdent = skipWhitespaceBackward(line, identStart - 1);
			if (preIdent < 0)
				return null;
			final var preIdentChar = line.charAt(preIdent);
			if (preIdentChar == ',' || preIdentChar == '@')
				return null;
			if (Character.isJavaIdentifierPart(preIdentChar)) {
				var typeWordStart = preIdent;
				while (typeWordStart > 0 && Character.isJavaIdentifierPart(line.charAt(typeWordStart - 1)))
					--typeWordStart;
				if (NON_TYPE_KEYWORDS.contains(line.substring(typeWordStart, preIdent + 1)))
					return null;
			}
			middleEnd = identEndExclusive;
			typeEndExclusive = preIdent + 1;
		}

		final var bracketsEnd = findBracketsEnd(line, column);
		if (bracketsEnd < 0)
			return null;

		final var afterBrackets = skipWhitespaceForward(line, bracketsEnd);
		if (afterBrackets >= line.length())
			return null;
		final var nextChar = line.charAt(afterBrackets);
		final var inParens = isInsideMethodLikeParens(line, column);
		if (isMethodReturn) {
			if (nextChar != '{' && nextChar != ';') {
				final var len = THROWS_KEYWORD.length();
				if (afterBrackets + len > line.length()
						|| !line.regionMatches(afterBrackets, THROWS_KEYWORD, 0, len)
						|| (afterBrackets + len < line.length()
								&& Character.isJavaIdentifierPart(line.charAt(afterBrackets + len))))
					return null;
			}
		}
		else if (nextChar == ',') {
			if (!inParens)
				return null;
		}
		else if (nextChar != '=' && nextChar != ';' && nextChar != ')')
			return null;

		if (!isMethodReturn && !inParens && hasCommaBeforeTerminator(lines, lineIndex, bracketsEnd))
			return null;

		if (!rawLine.substring(middleEnd, column).equals(line.substring(middleEnd, column)))
			return null;

		final var brackets = rawLine.substring(column, bracketsEnd);
		final var fixed = rawLine.substring(0, typeEndExclusive)
				+ brackets
				+ rawLine.substring(typeEndExclusive, middleEnd)
				+ rawLine.substring(bracketsEnd);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	/**
	 * Forward-scans across {@code lines} starting at {@code (startLineIdx, startColumn)},
	 * tracking comments, string/char literals, and paren/bracket/brace depth, until a
	 * depth-0 {@code ,} (returns true) or {@code ;} (returns false) is reached.
	 * Block-comment state persists across line boundaries (so block comments spanning
	 * multiple lines are correctly skipped); string, char, and line-comment states reset
	 * per line, since Java strings and chars cannot span lines. Text blocks ({@code """})
	 * cause an immediate {@code true} return (conservative bail; caller will skip the fix).
	 */
	@CheckReturnValue
	private static boolean hasCommaBeforeTerminator(
			@Nonnull List<String> lines,
			int startLineIdx,
			int startColumn
	) {
		var depth = 0;
		var inBlockComment = false;
		for (var lineIdx = startLineIdx; lineIdx < lines.size(); ++lineIdx) {
			final var ln = lines.get(lineIdx);
			if (ln.contains("\"\"\""))
				return true;
			var inChar = false;
			var inLineComment = false;
			var inString = false;
			final var from = lineIdx == startLineIdx ? startColumn : 0;
			for (var i = from; i < ln.length(); ++i) {
				final var c = ln.charAt(i);
				if (inLineComment)
					continue;
				if (inBlockComment) {
					if (c == '*' && i + 1 < ln.length() && ln.charAt(i + 1) == '/') {
						inBlockComment = false;
						++i;
					}
					continue;
				}
				if (inString) {
					if (c == '\\')
						++i;
					else if (c == '"')
						inString = false;
					continue;
				}
				if (inChar) {
					if (c == '\\')
						++i;
					else if (c == '\'')
						inChar = false;
					continue;
				}
				if (c == '/' && i + 1 < ln.length()) {
					final var n = ln.charAt(i + 1);
					if (n == '/') {
						inLineComment = true;
						++i;
						continue;
					}
					if (n == '*') {
						inBlockComment = true;
						++i;
						continue;
					}
				}
				if (c == '"')
					inString = true;
				else if (c == '\'')
					inChar = true;
				else if (c == '(' || c == '[' || c == '{')
					++depth;
				else if (c == ')' || c == ']' || c == '}') {
					if (depth == 0)
						return false;
					--depth;
				}
				else if (depth == 0) {
					if (c == ',')
						return true;
					if (c == ';')
						return false;
				}
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isInsideMethodLikeParens(@Nonnull String line, int upTo) {
		final var openParen = findEnclosingOpenParen(line, upTo);
		if (openParen < 0)
			return false;

		var pos = skipWhitespaceBackward(line, openParen - 1);
		if (pos >= 0 && line.charAt(pos) == '>') {
			var depth = 0;
			while (pos >= 0) {
				final var c = line.charAt(pos);
				if (c == '>')
					++depth;
				else if (c == '<') {
					--depth;
					if (depth == 0) {
						--pos;
						break;
					}
				}
				--pos;
			}
			pos = skipWhitespaceBackward(line, pos);
		}

		if (pos < 0)
			return true;
		if (!Character.isJavaIdentifierPart(line.charAt(pos)))
			return true;

		var identStart = pos;
		while (identStart > 0 && Character.isJavaIdentifierPart(line.charAt(identStart - 1)))
			--identStart;
		final var ident = line.substring(identStart, pos + 1);
		if (NON_PARAM_LIST_KEYWORDS.contains(ident))
			return false;

		final var beforeIdent = skipWhitespaceBackward(line, identStart - 1);
		return beforeIdent < 0 || line.charAt(beforeIdent) != '@';
	}

	/**
	 * Returns true only if the {@code )} at {@code prevPreBracket} closes a method
	 * declaration's parameter list (not, e.g., a field initializer ending in a method
	 * call). Mirrors the type-end validation done by
	 * {@link #applyBracketsToDeclaration}.
	 */
	@CheckReturnValue
	private static boolean isMethodReturnPrev(@Nonnull String blankedDeclLine, int prevPreBracket) {
		if (blankedDeclLine.charAt(prevPreBracket) != ')')
			return false;
		final var openParen = findMatchingOpenParen(blankedDeclLine, prevPreBracket);
		if (openParen < 0)
			return false;
		final var beforeParen = skipWhitespaceBackward(blankedDeclLine, openParen - 1);
		if (beforeParen < 0)
			return false;
		if (!Character.isJavaIdentifierPart(blankedDeclLine.charAt(beforeParen)))
			return false;
		var methodIdentStart = beforeParen;
		while (methodIdentStart > 0
				&& Character.isJavaIdentifierPart(blankedDeclLine.charAt(methodIdentStart - 1)))
			--methodIdentStart;
		final var beforeMethodIdent = skipWhitespaceBackward(blankedDeclLine, methodIdentStart - 1);
		if (beforeMethodIdent < 0)
			return false;
		final var typeEndChar = blankedDeclLine.charAt(beforeMethodIdent);
		return Character.isJavaIdentifierPart(typeEndChar) || typeEndChar == ']' || typeEndChar == '>';
	}

	@CheckReturnValue
	private static int skipWhitespaceBackward(@Nonnull String line, int from) {
		var pos = from;
		while (pos >= 0 && Character.isWhitespace(line.charAt(pos)))
			--pos;
		return pos;
	}

	@CheckReturnValue
	private static int skipWhitespaceForward(@Nonnull String line, int from) {
		var pos = from;
		while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
			++pos;
		return pos;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var rawLine = lines.get(lineIndex);
		if (column < 0 || column >= rawLine.length() || rawLine.charAt(column) != '[')
			return null;

		final var line = blankComments(rawLine);
		final var preBracket = skipWhitespaceBackward(line, column - 1);
		if (preBracket < 0)
			return fixMultiLine(lines, lineIndex, column, rawLine, line);

		return fixSingleLine(lines, lineIndex, column, rawLine, line, preBracket);
	}
}