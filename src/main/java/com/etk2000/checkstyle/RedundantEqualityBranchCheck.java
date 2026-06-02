package com.etk2000.checkstyle;

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
public class RedundantEqualityBranchCheck extends AbstractAstCheck {
	private record BranchInfo(@Nonnull BranchKind kind, @Nullable DetailAST target, @Nonnull DetailAST value) {}

	public enum BranchKind {
		ASSIGN,
		RETURN
	}

	/**
	 * The classification of a redundant equality if-else. Lines are 0-based and inclusive.
	 * {@code target} is the assignment target text for {@link BranchKind#ASSIGN} and
	 * {@code null} for a return. When an uninitialized {@code T target;} declaration sits immediately
	 * above the assign form and a bare {@code return target;} immediately below, {@code declLine} and
	 * {@code collapseReturnLine} carry their 0-based lines (both {@code -1} otherwise).
	 */
	public record Redundancy(
			@Nonnull BranchKind kind,
			@Nullable String target,
			@Nonnull String hint,
			int startLine,
			int endLine,
			int declLine,
			int collapseReturnLine
	) {}

	private static final String MSG_KEY = "redundant.equality.branch";

	/**
	 * Classifies {@code ifAst} as a redundant equality if-else, returning the collapse or {@code null}
	 * when it does not fire.
	 */
	@CheckReturnValue
	@Nullable
	public static Redundancy classify(@Nonnull DetailAST ifAst) {
		if (ifAst.getType() != TokenTypes.LITERAL_IF)
			return null;
		final var lparen = ifAst.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return null;
		final var condExpr = lparen.getNextSibling();
		if (condExpr == null || condExpr.getType() != TokenTypes.EXPR)
			return null;
		final var condition = condExpr.getFirstChild();
		if (condition == null
				|| (condition.getType() != TokenTypes.EQUAL && condition.getType() != TokenTypes.NOT_EQUAL))
			return null;

		final var leftOp = condition.getFirstChild();
		final var rightOp = leftOp != null ? leftOp.getNextSibling() : null;
		if (leftOp == null || rightOp == null)
			return null;
		if (!AstUtil.isPureExpression(leftOp) || !AstUtil.isPureExpression(rightOp))
			return null;

		final var leftText = AstUtil.displayText(leftOp);
		final var rightText = AstUtil.displayText(rightOp);

		final var rparen = ifAst.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return null;

		final var thenBranch = extractBranch(thenBody);
		if (thenBranch == null)
			return null;

		final BranchInfo elseBranch;
		final int endLine;
		final var elseAst = ifAst.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return null;
			elseBranch = extractBranch(elseBody);
			if (elseBranch == null)
				return null;
			endLine = AstUtil.lastLine(ifAst) - 1;
		}
		else {
			if (thenBranch.kind() != BranchKind.RETURN)
				return null;
			final var nextStmt = ifAst.getNextSibling();
			if (nextStmt == null || nextStmt.getType() != TokenTypes.LITERAL_RETURN)
				return null;
			final var nextExpr = nextStmt.findFirstToken(TokenTypes.EXPR);
			if (nextExpr == null)
				return null;
			final var nextValue = nextExpr.getFirstChild();
			if (nextValue == null)
				return null;
			elseBranch = new BranchInfo(BranchKind.RETURN, null, nextValue);
			endLine = AstUtil.lastLine(nextStmt) - 1;
		}

		if (thenBranch.kind() != elseBranch.kind())
			return null;
		if (thenBranch.kind() == BranchKind.ASSIGN
				&& !AstUtil.displayText(thenBranch.target()).equals(AstUtil.displayText(elseBranch.target())))
			return null;

		if (!AstUtil.isPureExpression(thenBranch.value()) || !AstUtil.isPureExpression(elseBranch.value()))
			return null;

		final var thenValueText = AstUtil.displayText(thenBranch.value());
		final var elseValueText = AstUtil.displayText(elseBranch.value());
		if (!leftText.equals(thenValueText) && !rightText.equals(thenValueText))
			return null;
		if (!leftText.equals(elseValueText) && !rightText.equals(elseValueText))
			return null;

		final var hint = condition.getType() == TokenTypes.EQUAL ? elseValueText : thenValueText;
		final var kind = thenBranch.kind();
		final var target = kind == BranchKind.ASSIGN ? AstUtil.displayText(thenBranch.target()) : null;
		final var startLine = ifAst.getLineNo() - 1;

		var declLine = -1;
		var collapseReturnLine = -1;
		if (kind == BranchKind.ASSIGN && elseAst != null) {
			final var decl = previousStatement(ifAst);
			final var next = ifAst.getNextSibling();
			if (decl != null && decl.getType() == TokenTypes.VARIABLE_DEF
					&& next != null && next.getType() == TokenTypes.LITERAL_RETURN
					&& isUninitializedDeclOf(decl, target)
					&& returnsBareName(next, target)) {
				declLine = decl.getLineNo() - 1;
				collapseReturnLine = AstUtil.lastLine(next) - 1;
			}
		}

		return new Redundancy(kind, target, hint, startLine, endLine, declLine, collapseReturnLine);
	}

	/**
	 * Locates the {@code LITERAL_IF} the check reported at {@code (line, column)} (0-based) in
	 * {@code root} and classifies it, or {@code null} when no such if-node exists (the fixer's
	 * parse produced no node at the reported site).
	 */
	@CheckReturnValue
	@Nullable
	public static Redundancy classifyAt(@Nonnull DetailAST root, int line, int column) {
		final var ifAst = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.LITERAL_IF);
		return ifAst == null ? null : classify(ifAst);
	}

	@CheckReturnValue
	@Nullable
	private static BranchInfo extractBranch(@Nonnull DetailAST body) {
		final var stmt = AstUtil.unwrapSingleStatementBlock(body);
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
	private static boolean isUninitializedDeclOf(@Nonnull DetailAST varDef, @Nonnull String name) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		return ident != null && ident.getText().equals(name) && varDef.findFirstToken(TokenTypes.ASSIGN) == null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST previousStatement(@Nonnull DetailAST node) {
		var prev = node.getPreviousSibling();
		while (prev != null && prev.getType() == TokenTypes.SEMI)
			prev = prev.getPreviousSibling();
		return prev;
	}

	@CheckReturnValue
	private static boolean returnsBareName(@Nonnull DetailAST returnStmt, @Nonnull String name) {
		final var expr = returnStmt.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return false;
		final var value = expr.getFirstChild();
		return value != null && value.getType() == TokenTypes.IDENT
				&& value.getNextSibling() == null && value.getText().equals(name);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var redundancy = classify(ast);
		if (redundancy != null)
			log(ast, MSG_KEY, redundancy.hint());
	}
}