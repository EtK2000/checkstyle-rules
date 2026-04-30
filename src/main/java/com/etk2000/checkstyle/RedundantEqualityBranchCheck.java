package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags if-else chains whose condition is an equality
 * comparison ({@code X == Y} or {@code X != Y}) and whose branches each use
 * one of the equality operands. Inside the true branch of {@code ==} (or false
 * branch of {@code !=}), X and Y are interchangeable, so the if-else collapses
 * to a single statement.
 *
 * <p>For {@code ==}, the surviving value is the else-branch value; for
 * {@code !=}, the surviving value is the then-branch value.
 *
 * <p>Examples that fire:
 * <ul>
 *     <li>{@code if (a == b) r = a; else r = b;} -> {@code r = b;}</li>
 *     <li>{@code if (a == b) return a; else return b;} -> {@code return b;}</li>
 *     <li>{@code if (a == b) return a; return b;} -> {@code return b;}</li>
 *     <li>{@code if (a != b) r = a; else r = b;} -> {@code r = a;}</li>
 *     <li>{@code if (a != b) return a; return b;} -> {@code return a;}</li>
 * </ul>
 * Operands and branch values must all be pure (no side effects).
 */
public class RedundantEqualityBranchCheck extends AbstractCheck {
	private record BranchInfo(@Nonnull BranchKind kind, @Nullable DetailAST target, @Nonnull DetailAST value) {}

	private enum BranchKind {
		ASSIGN,
		RETURN
	}

	private static final String MSG_KEY = "redundant.equality.branch";

	@CheckReturnValue
	@Nullable
	private static BranchInfo extractBranch(@Nonnull DetailAST body) {
		final var stmt = unwrapSingleStatementBlock(body);
		if (stmt == null)
			return null;

		if (stmt.getType() == TokenTypes.EXPR) {
			final var inner = stmt.getFirstChild();
			if (inner == null || inner.getType() != TokenTypes.ASSIGN)
				return null;
			final var lhs = inner.getFirstChild();
			final var rhs = lhs != null ? lhs.getNextSibling() : null;
			if (lhs == null || rhs == null)
				return null;
			return new BranchInfo(BranchKind.ASSIGN, lhs, rhs);
		}

		if (stmt.getType() == TokenTypes.LITERAL_RETURN) {
			final var expr = stmt.findFirstToken(TokenTypes.EXPR);
			if (expr == null)
				return null;
			final var value = expr.getFirstChild();
			if (value == null)
				return null;
			return new BranchInfo(BranchKind.RETURN, null, value);
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapSingleStatementBlock(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.SLIST)
			return body;
		DetailAST single = null;
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.SEMI || child.getType() == TokenTypes.RCURLY)
				continue;
			if (single != null)
				return null;
			single = child;
		}
		return single;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return;
		final var condExpr = lparen.getNextSibling();
		if (condExpr == null || condExpr.getType() != TokenTypes.EXPR)
			return;
		final var condition = condExpr.getFirstChild();
		if (condition == null
				|| (condition.getType() != TokenTypes.EQUAL && condition.getType() != TokenTypes.NOT_EQUAL))
			return;

		final var leftOp = condition.getFirstChild();
		final var rightOp = leftOp != null ? leftOp.getNextSibling() : null;
		if (leftOp == null || rightOp == null)
			return;
		if (!AstUtil.isPureExpression(leftOp) || !AstUtil.isPureExpression(rightOp))
			return;

		final var leftText = AstUtil.displayText(leftOp);
		final var rightText = AstUtil.displayText(rightOp);

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return;

		final var thenBranch = extractBranch(thenBody);
		if (thenBranch == null)
			return;

		final BranchInfo elseBranch;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return;
			elseBranch = extractBranch(elseBody);
			if (elseBranch == null)
				return;
		}
		else {
			if (thenBranch.kind() != BranchKind.RETURN)
				return;
			final var nextStmt = ast.getNextSibling();
			if (nextStmt == null || nextStmt.getType() != TokenTypes.LITERAL_RETURN)
				return;
			final var nextExpr = nextStmt.findFirstToken(TokenTypes.EXPR);
			if (nextExpr == null)
				return;
			final var nextValue = nextExpr.getFirstChild();
			if (nextValue == null)
				return;
			elseBranch = new BranchInfo(BranchKind.RETURN, null, nextValue);
		}

		if (thenBranch.kind() != elseBranch.kind())
			return;
		if (thenBranch.kind() == BranchKind.ASSIGN
				&& !AstUtil.displayText(thenBranch.target()).equals(AstUtil.displayText(elseBranch.target())))
			return;

		if (!AstUtil.isPureExpression(thenBranch.value()) || !AstUtil.isPureExpression(elseBranch.value()))
			return;

		final var thenValueText = AstUtil.displayText(thenBranch.value());
		final var elseValueText = AstUtil.displayText(elseBranch.value());
		if (!leftText.equals(thenValueText) && !rightText.equals(thenValueText))
			return;
		if (!leftText.equals(elseValueText) && !rightText.equals(elseValueText))
			return;

		final var hint = condition.getType() == TokenTypes.EQUAL ? elseValueText : thenValueText;
		log(ast, MSG_KEY, hint);
	}
}