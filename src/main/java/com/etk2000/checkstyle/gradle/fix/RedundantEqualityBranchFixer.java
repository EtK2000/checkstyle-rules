package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantEqualityBranchFixer implements CheckstyleFixer {
	private static final Pattern ASSIGN_BODY = Pattern.compile("^\\s*(.+?)\\s*=\\s*(.+?)\\s*;\\s*$");
	private static final Pattern DECL_LINE = Pattern.compile("^\\s*(?:final\\s+)?\\S+\\s+(\\w+)\\s*;\\s*$");
	private static final Pattern ELSE_LINE = Pattern.compile("^\\s*else\\s*$");
	private static final Pattern IF_LINE = Pattern.compile(
			"^(\\s*)if\\s*\\(\\s*(.+?)\\s*(==|!=)\\s*(.+?)\\s*\\)\\s*$"
	);
	private static final Pattern RETURN_BODY = Pattern.compile("^\\s*return\\s+(.+?)\\s*;\\s*$");
	private static final Pattern RETURN_VAR_LINE = Pattern.compile("^\\s*return\\s+(\\w+)\\s*;\\s*$");

	@CheckReturnValue
	@Nullable
	private static String hintValue(
			@Nonnull String op,
			@Nonnull String thenValue,
			@Nonnull String elseValue,
			@Nonnull String left,
			@Nonnull String right
	) {
		if (!matchesOperand(thenValue, left, right) || !matchesOperand(elseValue, left, right))
			return null;
		return "==".equals(op) ? elseValue : thenValue;
	}

	@CheckReturnValue
	private static boolean matchesOperand(@Nonnull String value, @Nonnull String left, @Nonnull String right) {
		return left.equals(value) || right.equals(value);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryAssignShape(
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
		if (!ELSE_LINE.matcher(lines.get(lineIndex + 2)).matches())
			return null;
		final var elseBodyMatch = ASSIGN_BODY.matcher(lines.get(lineIndex + 3));
		if (!elseBodyMatch.matches())
			return null;
		final var elseTarget = elseBodyMatch.group(1).strip();
		final var elseValue = elseBodyMatch.group(2).strip();
		if (!thenTarget.equals(elseTarget))
			return null;

		final var hint = hintValue(op, thenValue, elseValue, leftOp, rightOp);
		if (hint == null)
			return null;

		final var declIndex = lineIndex - 1;
		final var trailingReturnIndex = lineIndex + 4;
		if (declIndex >= 0 && trailingReturnIndex < lines.size()) {
			final var declMatch = DECL_LINE.matcher(lines.get(declIndex));
			final var returnVarMatch = RETURN_VAR_LINE.matcher(lines.get(trailingReturnIndex));
			if (declMatch.matches()
					&& returnVarMatch.matches()
					&& thenTarget.equals(declMatch.group(1))
					&& thenTarget.equals(returnVarMatch.group(1))) {
				return new FixResult(
						declIndex,
						trailingReturnIndex,
						List.of(indent + "return " + hint + ";")
				);
			}
		}

		return new FixResult(
				lineIndex,
				lineIndex + 3,
				List.of(indent + thenTarget + " = " + hint + ";")
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
				&& ELSE_LINE.matcher(lines.get(lineIndex + 2)).matches()) {
			final var elseReturnMatch = RETURN_BODY.matcher(lines.get(lineIndex + 3));
			if (elseReturnMatch.matches()) {
				final var elseValue = elseReturnMatch.group(1).strip();
				final var hint = hintValue(op, thenValue, elseValue, leftOp, rightOp);
				if (hint == null)
					return null;
				return new FixResult(
						lineIndex,
						lineIndex + 3,
						List.of(indent + "return " + hint + ";")
				);
			}
		}

		if (lineIndex + 2 < lines.size()) {
			final var trailingReturnMatch = RETURN_BODY.matcher(lines.get(lineIndex + 2));
			if (trailingReturnMatch.matches()) {
				final var elseValue = trailingReturnMatch.group(1).strip();
				final var hint = hintValue(op, thenValue, elseValue, leftOp, rightOp);
				if (hint == null)
					return null;
				return new FixResult(
						lineIndex,
						lineIndex + 2,
						List.of(indent + "return " + hint + ";")
				);
			}
		}

		return null;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var ifLine = lines.get(lineIndex);
		final var ifMatch = IF_LINE.matcher(ifLine);
		if (!ifMatch.matches())
			return null;

		final var indent = ifMatch.group(1);
		final var leftOp = ifMatch.group(2);
		final var op = ifMatch.group(3);
		final var rightOp = ifMatch.group(4);

		if (lineIndex + 1 >= lines.size())
			return null;
		final var thenLine = lines.get(lineIndex + 1);

		final var thenAssign = ASSIGN_BODY.matcher(thenLine);
		final var thenReturn = RETURN_BODY.matcher(thenLine);

		if (thenAssign.matches()) {
			return tryAssignShape(
					lines,
					lineIndex,
					indent,
					leftOp,
					op,
					rightOp,
					thenAssign.group(1).strip(),
					thenAssign.group(2).strip()
			);
		}
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
}