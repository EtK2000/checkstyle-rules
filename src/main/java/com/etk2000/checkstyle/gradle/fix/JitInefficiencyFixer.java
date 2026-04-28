package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class JitInefficiencyFixer implements CheckstyleFixer {
	private static final Set<String> BOXED_PRIMITIVE_TYPES = Set.of(
			"Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short"
	);

	@CheckReturnValue
	private static boolean containsTopLevelPlus(@Nonnull String s) {
		var depth = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"' || ch == '\'') {
				final var quote = ch;
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if (c == quote) {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '(' || ch == '[')
				++depth;
			else if (ch == ')' || ch == ']') {
				if (depth > 0)
					--depth;
			}
			else if (ch == '+' && depth == 0)
				return true;
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static int findClosingParen(@Nonnull String line, int openParen) {
		var depth = 0;
		var i = openParen;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
						i += 2;
						continue;
					}
					if (c == '"') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
						i += 2;
						continue;
					}
					if (c == '\'') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					return i;
			}
			++i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findExprEnd(@Nonnull String line, int from) {
		var depth = 0;
		var i = from;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				final var quote = ch;
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
						i += 2;
						continue;
					}
					if (c == quote) {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '(' || ch == '[')
				++depth;
			else if (ch == ')' || ch == ']') {
				if (depth == 0)
					return i;
				--depth;
			}
			else if (depth == 0) {
				if (ch == ',' || ch == ';')
					return i;
				// stop at lower-precedence operators that bind weaker than `+`:
				// `==`, `!=`, `<=`, `>=`, `<`, `>`, `&&`, `||`, `?`, `:`
				if (ch == '?' || ch == ':')
					return i;
				if ((ch == '=' || ch == '!') && i + 1 < line.length() && line.charAt(i + 1) == '=')
					return i;
				if ((ch == '<' || ch == '>') && (i + 1 >= line.length() || line.charAt(i + 1) != ch))
					return i;
				if ((ch == '&' || ch == '|') && i + 1 < line.length() && line.charAt(i + 1) == ch)
					return i;
			}
			++i;
		}
		return i;
	}

	@CheckReturnValue
	private static int findIdentifierStart(@Nonnull String line, int from) {
		var pos = from - 1;
		while (pos >= 0) {
			final var ch = line.charAt(pos);
			if (Character.isJavaIdentifierPart(ch) || ch == '.')
				--pos;
			else
				break;
		}
		return pos + 1;
	}

	@CheckReturnValue
	@Nullable
	private static String fixAppendConcat(@Nonnull String line, int column) {
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		final var appendIdx = line.lastIndexOf(".append(", column);
		if (appendIdx < 0)
			return null;
		final var openParen = appendIdx + ".append".length();
		final var closeParen = findClosingParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argsStart = openParen + 1;
		final var arg = line.substring(argsStart, closeParen);
		final var parts = splitTopLevelPlus(arg);
		if (parts == null || parts.size() < 2)
			return null;
		// the FIRST operand must contain a String literal. Otherwise the leading
		// operands may be a numeric-add chain (e.g. `1 + 2 + "x"` evaluates as `"3x"`,
		// but splitting yields `.append(1).append(2).append("x")` = `"12x"`).
		if (!parts.getFirst().contains("\""))
			return null;
		final var sb = new StringBuilder();
		sb.append(line, 0, appendIdx);
		for (var p : parts)
			sb.append(".append(").append(p.strip()).append(')');
		sb.append(line, closeParen + 1, line.length());
		return sb.toString();
	}

	@CheckReturnValue
	@Nullable
	private static String fixBoxedConstructor(@Nonnull String line, int column) {
		if (column >= line.length() || !line.startsWith("new ", column))
			return null;
		final var typeStart = column + "new ".length();
		var typeEnd = typeStart;
		while (typeEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(typeEnd)))
			++typeEnd;
		final var typeName = line.substring(typeStart, typeEnd);
		if (!BOXED_PRIMITIVE_TYPES.contains(typeName))
			return null;
		if (typeEnd >= line.length() || line.charAt(typeEnd) != '(')
			return null;
		final var openParen = typeEnd;
		final var closeParen = findClosingParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argText = line.substring(openParen + 1, closeParen).strip();
		if ("Boolean".equals(typeName)) {
			if ("true".equals(argText))
				return line.substring(0, column) + "Boolean.TRUE" + line.substring(closeParen + 1);
			if ("false".equals(argText))
				return line.substring(0, column) + "Boolean.FALSE" + line.substring(closeParen + 1);
		}
		return line.substring(0, column) + typeName + ".valueOf(" + argText + ")"
				+ line.substring(closeParen + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String fixEmptyStringConcat(@Nonnull String line, int column) {
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		// `"" + expr` form
		final var leftIdx = line.indexOf("\"\" + ");
		if (leftIdx >= 0) {
			final var rhsStart = leftIdx + 5;
			final var rhsEnd = findExprEnd(line, rhsStart);
			// bail on multiline: if findExprEnd consumed all the way to end-of-line,
			// the expression continues on the next line and we can't safely capture it.
			if (rhsEnd < 0 || rhsEnd >= line.length())
				return null;
			final var rhs = line.substring(rhsStart, rhsEnd);
			// reject if rhs has a top-level `+` (chain like `"" + a + b` would change semantics)
			if (containsTopLevelPlus(rhs))
				return null;
			return line.substring(0, leftIdx) + "String.valueOf(" + rhs + ")" + line.substring(rhsEnd);
		}
		// `expr + ""` form
		final var rightIdx = line.indexOf(" + \"\"");
		if (rightIdx >= 0) {
			final var lhsStart = findIdentifierStart(line, rightIdx);
			if (lhsStart < 0 || lhsStart == rightIdx)
				return null;
			final var lhs = line.substring(lhsStart, rightIdx);
			// reject chain: scan everything before the LHS for a top-level `+` (symmetric
			// with the rhs branch). Any `+` to the left at depth 0 means we're in the
			// middle of a longer concat chain.
			if (containsTopLevelPlus(line.substring(0, lhsStart)))
				return null;
			final var afterEmpty = rightIdx + " + \"\"".length();
			return line.substring(0, lhsStart) + "String.valueOf(" + lhs + ")"
					+ line.substring(afterEmpty);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixNewString(@Nonnull String line, int column) {
		if (column >= line.length() || !line.startsWith("new String(", column))
			return null;
		final var openParen = column + "new String".length();
		final var closeParen = findClosingParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argText = line.substring(openParen + 1, closeParen).strip();
		if (argText.isEmpty())
			return null;
		if (!isSimpleIdentifier(argText) && !isSingleStringLiteral(argText))
			return null;
		return line.substring(0, column) + argText + line.substring(closeParen + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String fixStringBuffer(@Nonnull String line, int column) {
		final var prefix = "new StringBuffer";
		if (!line.startsWith(prefix, column))
			return null;
		// guard against `new StringBufferInputStream` (legacy java.io class): the
		// next char must be `(`, `<`, or whitespace, not an identifier continuation.
		final var afterPrefix = column + prefix.length();
		if (afterPrefix < line.length()) {
			final var nextChar = line.charAt(afterPrefix);
			if (Character.isJavaIdentifierPart(nextChar))
				return null;
		}
		return line.substring(0, column) + "new StringBuilder" + line.substring(afterPrefix);
	}

	@CheckReturnValue
	@Nullable
	private static String fixToArraySized(@Nonnull String line, int column) {
		// the violation column may point at the receiver or the LPAREN; the prefix
		// `.toArray(new ` may lie before or after `column`. Try lastIndexOf first
		// (matches LPAREN-anchored case) and fall back to indexOf (receiver-anchored).
		final var prefix = ".toArray(new ";
		var idx = line.lastIndexOf(prefix, Math.max(0, column));
		if (idx < 0)
			idx = line.indexOf(prefix, Math.max(0, column));
		if (idx < 0)
			return null;
		final var typeStart = idx + prefix.length();
		final var bracketIdx = line.indexOf('[', typeStart);
		if (bracketIdx < 0)
			return null;
		final var typeText = line.substring(typeStart, bracketIdx);
		if (typeText.contains("@"))
			return null;
		// require a single-dim closing ']' before ')'. Multi-dim has another '[' before ')'.
		final var sizeStart = bracketIdx + 1;
		final var sizeEnd = line.indexOf(']', sizeStart);
		if (sizeEnd < 0)
			return null;
		final var afterClose = sizeEnd + 1;
		if (afterClose >= line.length())
			return null;
		if (line.charAt(afterClose) != ')')
			return null;
		// refuse unless the size expression is known-pure (whitelist), otherwise dropping
		// it would silently lose side effects.
		final var sizeText = line.substring(sizeStart, sizeEnd);
		if (!sizeExpressionIsKnownPure(sizeText))
			return null;
		return line.substring(0, bracketIdx + 1) + "0" + line.substring(sizeEnd);
	}

	@CheckReturnValue
	private static boolean isSimpleIdentifier(@Nonnull String s) {
		if (s.isEmpty())
			return false;
		if (!Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (var i = 1; i < s.length(); ++i) {
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isSingleStringLiteral(@Nonnull String s) {
		if (s.length() < 2 || s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"')
			return false;
		var i = 1;
		while (i < s.length() - 1) {
			final var ch = s.charAt(i);
			if (ch == '\\' && i + 1 < s.length()) {
				i += 2;
				continue;
			}
			if (ch == '"')
				return false;
			++i;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean sizeExpressionIsKnownPure(@Nonnull String size) {
		// Whitelist of known-pure size shapes (values can be safely dropped):
		// - integer literal (decimal, hex, binary, optionally underscore-separated)
		// - simple identifier or dotted-name (e.g. `n`, `THIS.field`)
		// - dotted access ending in `.size()` or `.length()` (idempotent on collections/arrays/strings)
		final var trimmed = size.strip();
		if (trimmed.isEmpty())
			return false;
		if (Character.isDigit(trimmed.charAt(0))) {
			for (var i = 0; i < trimmed.length(); ++i) {
				final var ch = trimmed.charAt(i);
				if (!Character.isLetterOrDigit(ch) && ch != '_')
					return false;
			}
			return true;
		}
		final var sizeSuffix = ".size()";
		final var lengthSuffix = ".length()";
		var prefix = trimmed;
		if (trimmed.endsWith(sizeSuffix))
			prefix = trimmed.substring(0, trimmed.length() - sizeSuffix.length());
		else if (trimmed.endsWith(lengthSuffix))
			prefix = trimmed.substring(0, trimmed.length() - lengthSuffix.length());
		if (prefix.isEmpty())
			return false;
		for (var i = 0; i < prefix.length(); ++i) {
			final var ch = prefix.charAt(i);
			if (!Character.isJavaIdentifierPart(ch) && ch != '.')
				return false;
		}
		return Character.isJavaIdentifierStart(prefix.charAt(0));
	}

	@CheckReturnValue
	@Nullable
	private static List<String> splitTopLevelPlus(@Nonnull String s) {
		final var parts = new java.util.ArrayList<String>();
		var depth = 0;
		var lastSplit = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if (c == '"') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if (c == '\'') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				if (depth == 0)
					return null;
				--depth;
			}
			else if (ch == '+' && depth == 0) {
				parts.add(s.substring(lastSplit, i));
				lastSplit = i + 1;
			}
			++i;
		}
		if (depth != 0)
			return null;
		parts.add(s.substring(lastSplit));
		return parts;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		var result = fixBoxedConstructor(line, column);
		if (result == null)
			result = fixNewString(line, column);
		if (result == null)
			result = fixStringBuffer(line, column);
		if (result == null)
			result = fixToArraySized(line, column);
		if (result == null)
			result = fixEmptyStringConcat(line, column);
		if (result == null)
			result = fixAppendConcat(line, column);

		if (result == null)
			return null;
		return new FixResult(lineIndex, lineIndex, List.of(result));
	}
}