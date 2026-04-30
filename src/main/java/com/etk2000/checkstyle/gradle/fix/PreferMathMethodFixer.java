package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferMathMethodFixer implements CheckstyleFixer {
	private static final Pattern ASSIGN_BODY_PATTERN = Pattern.compile(
			"^\\s*([\\w.\\[\\]]+)\\s*=\\s*(.+?)\\s*;\\s*$"
	);
	private static final Pattern COMPOUND_ASSIGN_BODY_PATTERN = Pattern.compile(
			"^\\s*([\\w.\\[\\]]+)\\s*([+\\-*/%&|^]=|<<=|>>>?=)\\s*(.+?)\\s*;\\s*$"
	);
	private static final Pattern DECL_LINE_PATTERN = Pattern.compile(
			"^\\s*(?:final\\s+)?\\S+\\s+(\\w+)\\s*;\\s*$"
	);
	private static final Pattern ELSE_LINE_PATTERN = Pattern.compile("^\\s*else\\s*$");
	private static final Pattern IF_COMPARISON_PATTERN = Pattern.compile(
			"^(\\s*)if\\s*\\(\\s*(.+?)\\s*(>=?|<=?)\\s*(.+?)\\s*\\)\\s*$"
	);
	private static final Pattern IF_LINE_PATTERN = Pattern.compile(
			"^\\s*(?:\\}\\s*)?(?:else\\s+)?if\\s*\\("
	);
	private static final Pattern RETURN_BODY_PATTERN = Pattern.compile("^\\s*return\\s+(.+?)\\s*;\\s*$");
	private static final Pattern RETURN_VAR_PATTERN = Pattern.compile("^\\s*return\\s+(\\w+)\\s*;\\s*$");
	private static final Pattern TERNARY_PATTERN = Pattern.compile(
			"((?:\\+\\+|--)?(?:[+\\-]\\s*)?[\\w.\\[\\]]+)\\s*(>=?|<=?)\\s*((?:\\+\\+|--)?(?:[+\\-]\\s*)?[\\w.\\[\\]]+)\\s*\\?\\s*((?:[+\\-]\\s*)?[\\w.\\[\\]]+)\\s*:\\s*((?:[+\\-]\\s*)?[\\w.\\[\\]]+)"
	);
	// init capture excludes ',' to reject multi-decls like `int r = a, s = b;`
	private static final Pattern VAR_DECL_INIT_PATTERN = Pattern.compile(
			"^(\\s*)(?:final\\s+)?(?:\\w+(?:\\s*\\[\\s*\\])*\\s+)(\\w+)\\s*=\\s*([^,]+?)\\s*;\\s*$"
	);

	@CheckReturnValue
	@Nonnull
	private static String buildClamp(
			@Nonnull String line,
			int outerStart,
			int outerClose,
			@Nonnull String outerArg,
			@Nonnull String innerArg1,
			@Nonnull String innerArg2,
			boolean isOuterMax
	) {
		final String replacement;
		if (isOuterMax)
			replacement = "Math.clamp(" + innerArg2 + ", " + outerArg + ", " + innerArg1 + ")";
		else
			replacement = "Math.clamp(" + innerArg2 + ", " + innerArg1 + ", " + outerArg + ")";
		return line.substring(0, outerStart) + replacement + line.substring(outerClose + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String computeMathExpr(
			@Nonnull String left,
			@Nonnull String op,
			@Nonnull String right,
			@Nonnull String thenValue,
			@Nonnull String elseValue
	) {
		if (isZero(right)) {
			final var abs = tryFixAbs(left, op, thenValue, elseValue);
			if (abs != null)
				return abs;
		}
		if (isZero(left)) {
			final var abs = tryFixAbsZeroLeft(right, op, thenValue, elseValue);
			if (abs != null)
				return abs;
		}
		return tryFixMaxMin(left, op, right, thenValue, elseValue);
	}

	/**
	 * Finds the index of {@code target} at paren nesting depth 0, starting from {@code from}.
	 * Returns -1 if not found before the end of the string or before depth goes negative.
	 */
	@CheckReturnValue
	private static int findAtDepthZero(@Nonnull String s, int from, char target) {
		var depth = 0;
		for (var i = from; i < s.length(); ++i) {
			final var c = s.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')') {
				if (depth == 0)
					return -1;
				--depth;
			}
			else if (c == target && depth == 0)
				return i;
		}
		return -1;
	}

	/**
	 * Finds the matching close paren for an open paren at {@code openIndex}.
	 * {@code from} should point to the char AFTER the opening '('.
	 */
	@CheckReturnValue
	private static int findMatchingCloseParen(@Nonnull String s, int from) {
		var depth = 1;
		for (var i = from; i < s.length(); ++i) {
			final var c = s.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')') {
				--depth;
				if (depth == 0)
					return i;
			}
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static String fixClamp(@Nonnull String line) {
		final var result = tryFixClampOuter(line, "Math.max(", "Math.min(", true);
		if (result != null)
			return result;

		return tryFixClampOuter(line, "Math.min(", "Math.max(", false);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixIfShape(@Nonnull List<String> lines, int lineIndex) {
		final var ifMatch = IF_COMPARISON_PATTERN.matcher(lines.get(lineIndex));
		if (!ifMatch.matches())
			return null;
		final var indent = ifMatch.group(1);
		final var leftOp = ifMatch.group(2).strip();
		final var op = ifMatch.group(3);
		final var rightOp = ifMatch.group(4).strip();

		if (lineIndex + 1 >= lines.size())
			return null;
		final var thenLine = lines.get(lineIndex + 1);

		final var thenCompound = COMPOUND_ASSIGN_BODY_PATTERN.matcher(thenLine);
		if (thenCompound.matches()) {
			return tryCompoundAssignShape(
					lines,
					lineIndex,
					indent,
					leftOp,
					op,
					rightOp,
					thenCompound.group(1),
					thenCompound.group(2),
					thenCompound.group(3).strip()
			);
		}

		final var thenAssign = ASSIGN_BODY_PATTERN.matcher(thenLine);
		if (thenAssign.matches()) {
			final var plainResult = tryPlainAssignShape(
					lines,
					lineIndex,
					indent,
					leftOp,
					op,
					rightOp,
					thenAssign.group(1),
					thenAssign.group(2).strip()
			);
			if (plainResult != null)
				return plainResult;
			if (lineIndex >= 1) {
				return tryInitOverwriteShape(
						lines,
						lineIndex,
						indent,
						leftOp,
						op,
						rightOp,
						thenAssign.group(1),
						thenAssign.group(2).strip()
				);
			}
		}

		final var thenReturn = RETURN_BODY_PATTERN.matcher(thenLine);
		if (thenReturn.matches()) {
			return tryReturnShape(
					lines,
					lineIndex,
					indent,
					leftOp,
					op,
					rightOp,
					thenReturn.group(1).strip()
			);
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixTernary(@Nonnull String line, int column) {
		final var m = TERNARY_PATTERN.matcher(line);
		// when the violation column is known (> 0), pick the regex match whose
		// [start, end) contains it so multi-ternary lines pick the right one;
		// when column == 0 (unit-test default), fall back to the first match
		final boolean found;
		if (column <= 0)
			found = m.find();
		else {
			var matchContainingColumn = false;
			while (m.find()) {
				if (m.start() <= column && column < m.end()) {
					matchContainingColumn = true;
					break;
				}
			}
			found = matchContainingColumn;
		}
		if (!found)
			return null;

		final var left = m.group(1).strip();
		final var op = m.group(2);
		final var right = m.group(3).strip();
		final var trueBranch = m.group(4).strip();
		final var falseBranch = m.group(5).strip();

		if (isZero(right)) {
			final var absResult = tryFixAbs(left, op, trueBranch, falseBranch);
			if (absResult != null)
				return line.substring(0, m.start()) + absResult + line.substring(m.end());
		}
		if (isZero(left)) {
			final var absResult = tryFixAbsZeroLeft(right, op, trueBranch, falseBranch);
			if (absResult != null)
				return line.substring(0, m.start()) + absResult + line.substring(m.end());
		}

		final var maxMinResult = tryFixMaxMin(left, op, right, trueBranch, falseBranch);
		if (maxMinResult != null)
			return line.substring(0, m.start()) + maxMinResult + line.substring(m.end());

		return null;
	}

	@CheckReturnValue
	private static boolean isNegation(@Nonnull String expr, @Nonnull String variable) {
		if (!expr.startsWith("-"))
			return false;
		return expr.substring(1).strip().equals(variable);
	}

	@CheckReturnValue
	private static boolean isZero(@Nonnull String text) {
		return "0".equals(text);
	}

	/**
	 * Splits a call like "Math.min(arg1, arg2)" into its two arguments,
	 * using paren-balancing to find the correct comma.
	 * Returns [arg1, arg2] or null if the structure doesn't match.
	 */
	@CheckReturnValue
	@Nullable
	private static String[] splitInnerArgs(@Nonnull String call, @Nonnull String prefix) {
		if (!call.endsWith(")"))
			return null;

		final var argsStart = prefix.length();
		final var argsEnd = call.length() - 1;

		final var commaIdx = findAtDepthZero(call, argsStart, ',');
		if (commaIdx < 0 || commaIdx >= argsEnd)
			return null;

		return new String[]{
				call.substring(argsStart, commaIdx).strip(),
				call.substring(commaIdx + 1, argsEnd).strip()
		};
	}

	@CheckReturnValue
	@Nonnull
	private static String stripPrefixMutation(@Nonnull String operand) {
		if (operand.startsWith("++") || operand.startsWith("--"))
			return operand.substring(2);
		return operand;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryCompoundAssignShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenAssignOp,
			@Nonnull String thenValue
	) {
		if (lineIndex + 3 >= lines.size())
			return null;
		if (!ELSE_LINE_PATTERN.matcher(lines.get(lineIndex + 2)).matches())
			return null;
		final var elseMatch = COMPOUND_ASSIGN_BODY_PATTERN.matcher(lines.get(lineIndex + 3));
		if (!elseMatch.matches())
			return null;
		if (!thenTarget.equals(elseMatch.group(1)) || !thenAssignOp.equals(elseMatch.group(2)))
			return null;
		final var elseValue = elseMatch.group(3).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		return new FixResult(
				lineIndex,
				lineIndex + 3,
				List.of(indent + thenTarget + " " + thenAssignOp + " " + math + ";")
		);
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixAbs(
			@Nonnull String variable,
			@Nonnull String op,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var stripped = stripPrefixMutation(variable);
		if (">".equals(op) || ">=".equals(op)) {
			if (trueBranch.equals(stripped) && isNegation(falseBranch, stripped))
				return "Math.abs(" + variable + ")";
		}
		if ("<".equals(op) || "<=".equals(op)) {
			if (isNegation(trueBranch, stripped) && falseBranch.equals(stripped))
				return "Math.abs(" + variable + ")";
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixAbsZeroLeft(
			@Nonnull String variable,
			@Nonnull String op,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var stripped = stripPrefixMutation(variable);
		if (">".equals(op) || ">=".equals(op)) {
			if (isNegation(trueBranch, stripped) && falseBranch.equals(stripped))
				return "Math.abs(" + variable + ")";
		}
		if ("<".equals(op) || "<=".equals(op)) {
			if (trueBranch.equals(stripped) && isNegation(falseBranch, stripped))
				return "Math.abs(" + variable + ")";
		}
		return null;
	}

	/**
	 * Tries to parse and fix a clamp pattern starting with {@code outerPrefix}
	 * (e.g. "Math.max(") containing {@code innerPrefix} (e.g. "Math.min(").
	 * Uses paren-balancing to correctly split arguments even when they contain
	 * nested calls, casts, or other parenthesized expressions.
	 */
	@CheckReturnValue
	@Nullable
	private static String tryFixClampOuter(
			@Nonnull String line,
			@Nonnull String outerPrefix,
			@Nonnull String innerPrefix,
			boolean isOuterMax
	) {
		final var outerStart = line.indexOf(outerPrefix);
		if (outerStart < 0)
			return null;

		final var argsStart = outerStart + outerPrefix.length();
		final var outerClose = findMatchingCloseParen(line, argsStart);
		if (outerClose < 0)
			return null;

		final var commaIdx = findAtDepthZero(line, argsStart, ',');
		if (commaIdx < 0 || commaIdx >= outerClose)
			return null;

		final var firstArg = line.substring(argsStart, commaIdx).strip();
		final var secondArg = line.substring(commaIdx + 1, outerClose).strip();

		if (secondArg.startsWith(innerPrefix)) {
			final var innerArgs = splitInnerArgs(secondArg, innerPrefix);
			if (innerArgs != null)
				return buildClamp(line, outerStart, outerClose, firstArg, innerArgs[0], innerArgs[1], isOuterMax);
		}

		if (firstArg.startsWith(innerPrefix)) {
			final var innerArgs = splitInnerArgs(firstArg, innerPrefix);
			if (innerArgs != null)
				return buildClamp(line, outerStart, outerClose, secondArg, innerArgs[0], innerArgs[1], isOuterMax);
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixMaxMin(
			@Nonnull String left,
			@Nonnull String op,
			@Nonnull String right,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var strippedLeft = stripPrefixMutation(left);
		final var strippedRight = stripPrefixMutation(right);
		final var trueIsLeft = trueBranch.equals(strippedLeft) && falseBranch.equals(strippedRight);
		final var trueIsRight = trueBranch.equals(strippedRight) && falseBranch.equals(strippedLeft);
		if (!trueIsLeft && !trueIsRight)
			return null;

		final boolean isMax;
		if (">".equals(op) || ">=".equals(op))
			isMax = trueIsLeft;
		else
			isMax = trueIsRight;

		return (isMax ? "Math.max(" : "Math.min(") + left + ", " + right + ")";
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryInitOverwriteShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenValue
	) {
		final var declMatch = VAR_DECL_INIT_PATTERN.matcher(lines.get(lineIndex - 1));
		if (!declMatch.matches() || !thenTarget.equals(declMatch.group(2)))
			return null;
		final var elseValue = declMatch.group(3).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		if (lineIndex + 2 < lines.size()) {
			final var trailingReturn = RETURN_VAR_PATTERN.matcher(lines.get(lineIndex + 2));
			if (trailingReturn.matches() && thenTarget.equals(trailingReturn.group(1))) {
				return new FixResult(
						lineIndex - 1,
						lineIndex + 2,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		final var declLine = lines.get(lineIndex - 1);
		final var newDecl = declLine.substring(0, declLine.indexOf('=')) + "= " + math + ";";
		return new FixResult(lineIndex - 1, lineIndex + 1, List.of(newDecl));
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryPlainAssignShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenValue
	) {
		if (lineIndex + 3 >= lines.size())
			return null;
		if (!ELSE_LINE_PATTERN.matcher(lines.get(lineIndex + 2)).matches())
			return null;
		final var elseMatch = ASSIGN_BODY_PATTERN.matcher(lines.get(lineIndex + 3));
		if (!elseMatch.matches())
			return null;
		if (!thenTarget.equals(elseMatch.group(1)))
			return null;
		final var elseValue = elseMatch.group(2).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		final var declIndex = lineIndex - 1;
		final var trailingReturnIndex = lineIndex + 4;
		if (declIndex >= 0 && trailingReturnIndex < lines.size()) {
			final var declMatch = DECL_LINE_PATTERN.matcher(lines.get(declIndex));
			final var returnVarMatch = RETURN_VAR_PATTERN.matcher(lines.get(trailingReturnIndex));
			if (declMatch.matches()
					&& returnVarMatch.matches()
					&& thenTarget.equals(declMatch.group(1))
					&& thenTarget.equals(returnVarMatch.group(1))) {
				return new FixResult(
						declIndex,
						trailingReturnIndex,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		return new FixResult(
				lineIndex,
				lineIndex + 3,
				List.of(indent + thenTarget + " = " + math + ";")
		);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryReturnShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenValue
	) {
		if (lineIndex + 3 < lines.size()
				&& ELSE_LINE_PATTERN.matcher(lines.get(lineIndex + 2)).matches()) {
			final var elseReturnMatch = RETURN_BODY_PATTERN.matcher(lines.get(lineIndex + 3));
			if (elseReturnMatch.matches()) {
				final var elseValue = elseReturnMatch.group(1).strip();
				final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
				if (math == null)
					return null;
				return new FixResult(
						lineIndex,
						lineIndex + 3,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		if (lineIndex + 2 < lines.size()) {
			final var trailingReturnMatch = RETURN_BODY_PATTERN.matcher(lines.get(lineIndex + 2));
			if (trailingReturnMatch.matches()) {
				final var elseValue = trailingReturnMatch.group(1).strip();
				final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
				if (math == null)
					return null;
				return new FixResult(
						lineIndex,
						lineIndex + 2,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		return null;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		var result = fixClamp(line);
		if (result == null)
			result = fixTernary(line, column);
		if (result != null)
			return new FixResult(lineIndex, lineIndex, List.of(result));

		final var ifShapeResult = fixIfShape(lines, lineIndex);
		if (ifShapeResult != null)
			return ifShapeResult;

		if (IF_LINE_PATTERN.matcher(line).find())
			return new SkipResult(SkipMessages.MATH_METHOD_SKIP_IF);
		return new SkipResult(SkipMessages.MATH_METHOD_SKIP);
	}
}