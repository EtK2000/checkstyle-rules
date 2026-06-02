package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code PreferLiteralSuffixCheck}. Removes a widening cast
 * ({@code (long)}, {@code (float)}, {@code (double)}) and appends the
 * corresponding type suffix ({@code L}, {@code f}, {@code d}) to the
 * integer literal that the check identified as the other operand of the
 * binary expression or ternary branch.
 *
 * <p>Handles four operand-position patterns:
 * <ul>
 *   <li>{@code (Type) X OP LIT}, cast LHS, literal RHS</li>
 *   <li>{@code LIT OP (Type) X}, literal LHS, cast RHS</li>
 *   <li>{@code cond ? (Type) X : LIT}, ternary cast in true branch</li>
 *   <li>{@code cond ? LIT : (Type) X}, ternary cast in false branch</li>
 * </ul>
 * Literals may carry a leading unary {@code -}/{@code +}. Decimal and
 * hex ({@code 0x...}) literals are supported; underscore separators
 * inside the literal are preserved.
 */
class PreferLiteralSuffixFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static int findIdentifierEnd(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length()) {
			final var cp = line.codePointAt(i);
			if (!Character.isJavaIdentifierPart(cp))
				break;
			i += Character.charCount(cp);
		}
		return i;
	}

	@CheckReturnValue
	private static int findLiteralEndForward(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length() && Character.isWhitespace(line.charAt(i)))
			++i;
		if (i < line.length() && (line.charAt(i) == '-' || line.charAt(i) == '+'))
			++i;
		while (i < line.length() && Character.isWhitespace(line.charAt(i)))
			++i;
		if (i >= line.length() || line.charAt(i) < '0' || line.charAt(i) > '9')
			return -1;
		while (i < line.length() && isLiteralPart(line.charAt(i)))
			++i;
		return i;
	}

	@CheckReturnValue
	private static int findLiteralStartBackward(@Nonnull String line, int endExclusive) {
		var i = endExclusive - 1;
		while (i >= 0 && Character.isWhitespace(line.charAt(i)))
			--i;
		if (i < 0 || !isLiteralPart(line.charAt(i)))
			return -1;
		while (i >= 0 && isLiteralPart(line.charAt(i)))
			--i;
		return i + 1;
	}

	@CheckReturnValue
	private static int findLiteralValueEnd(@Nonnull String line, int literalStart) {
		var i = literalStart;
		if (i < line.length() && (line.charAt(i) == '-' || line.charAt(i) == '+'))
			++i;
		while (i < line.length() && Character.isWhitespace(line.charAt(i)))
			++i;
		final var digitsStart = i;
		while (i < line.length() && isLiteralPart(line.charAt(i)))
			++i;
		return i > digitsStart ? i : -1;
	}

	@CheckReturnValue
	private static boolean isBinaryOperatorChar(char c) {
		return c == '+' || c == '-' || c == '*' || c == '/' || c == '%'
				|| c == '&' || c == '|' || c == '^'
				|| c == '<' || c == '>' || c == '=' || c == '!';
	}

	@CheckReturnValue
	private static boolean isLiteralPart(char c) {
		return (c >= '0' && c <= '9')
				|| (c >= 'a' && c <= 'f')
				|| (c >= 'A' && c <= 'F')
				|| c == 'x' || c == 'X' || c == 'b' || c == 'B' || c == '_';
	}

	@CheckReturnValue
	private static int skipBinaryOperator(@Nonnull String line, int start) {
		var i = start;
		while (i < line.length() && isBinaryOperatorChar(line.charAt(i)))
			++i;
		return i;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryBackwardLiteral(
			@Nonnull String line, int lineIndex, int castStart, int subjectStart, @Nonnull String suffix
	) {
		var beforeCast = castStart - 1;
		while (beforeCast >= 0 && Character.isWhitespace(line.charAt(beforeCast)))
			--beforeCast;
		if (beforeCast < 0)
			return null;
		final var opChar = line.charAt(beforeCast);
		final int opStart;
		if (opChar == ':')
			opStart = beforeCast;
		else if (isBinaryOperatorChar(opChar)) {
			var i = beforeCast;
			while (i > 0 && isBinaryOperatorChar(line.charAt(i - 1)))
				--i;
			opStart = i;
		}
		else
			return null;

		final var literalStart = findLiteralStartBackward(line, opStart);
		if (literalStart < 0)
			return null;
		final var literalEnd = findLiteralValueEnd(line, literalStart);
		if (literalEnd < 0)
			return null;

		final var fixed = line.substring(0, literalEnd)
				+ suffix
				+ line.substring(literalEnd, castStart)
				+ line.substring(subjectStart);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryForwardLiteral(
			@Nonnull String line, int lineIndex, int castStart, int subjectStart, int subjectEnd, @Nonnull String suffix
	) {
		var afterSubject = subjectEnd;
		while (afterSubject < line.length() && Character.isWhitespace(line.charAt(afterSubject)))
			++afterSubject;
		if (afterSubject >= line.length())
			return null;
		final var opChar = line.charAt(afterSubject);
		final int literalSearchStart;
		if (opChar == ':')
			literalSearchStart = afterSubject + 1;
		else if (isBinaryOperatorChar(opChar))
			literalSearchStart = skipBinaryOperator(line, afterSubject);
		else
			return null;

		final var literalEnd = findLiteralEndForward(line, literalSearchStart);
		if (literalEnd < 0)
			return null;

		final var fixed = line.substring(0, castStart)
				+ line.substring(subjectStart, literalEnd)
				+ suffix
				+ line.substring(literalEnd);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length() || line.charAt(column) != '(')
			return null;

		final var castClose = line.indexOf(')', column + 1);
		if (castClose < 0)
			return new SkipResult("multi-line-cast");

		final var castType = line.substring(column + 1, castClose).trim();
		final var suffix = switch (castType) {
			case "double" -> "d";
			case "float" -> "f";
			case "long" -> "L";
			default -> null;
		};
		if (suffix == null)
			return null;

		var subjectStart = castClose + 1;
		while (subjectStart < line.length() && Character.isWhitespace(line.charAt(subjectStart)))
			++subjectStart;
		if (subjectStart >= line.length())
			return new SkipResult("malformed-cast-no-expression");
		final var subjectEnd = findIdentifierEnd(line, subjectStart);
		if (subjectEnd == subjectStart)
			return new SkipResult("non-identifier-cast-subject");

		final var forwardFix = tryForwardLiteral(line, lineIndex, column, subjectStart, subjectEnd, suffix);
		if (forwardFix != null)
			return forwardFix;

		final var backwardFix = tryBackwardLiteral(line, lineIndex, column, subjectStart, suffix);
		if (backwardFix != null)
			return backwardFix;

		// Subject parses as an identifier prefix but is followed by a non-operator char
		// (e.g. '.', '(', '['). The check fired on a compound subject the fixer can't
		// disambiguate without paren/bracket-balanced parsing.
		return new SkipResult("complex-cast-subject");
	}
}