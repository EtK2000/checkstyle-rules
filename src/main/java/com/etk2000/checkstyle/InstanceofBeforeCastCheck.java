package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags two patterns involving {@code instanceof}
 * and casts to the same type:
 * <ol>
 *     <li>In {@code &&} conditions, a cast appears before the
 *         {@code instanceof} check (the cast should come after).</li>
 *     <li>A cast appears in a branch where {@code instanceof} is
 *         guaranteed false (ternary false branch, else after
 *         {@code if (instanceof)}, or then after
 *         {@code if (!(instanceof))}), which will always throw
 *         {@code ClassCastException}.</li>
 * </ol>
 */
public class InstanceofBeforeCastCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "instanceof.before.cast";
	private static final String MSG_WRONG_BRANCH = "instanceof.cast.wrong.branch";

	/**
	 * Finds the LITERAL_INSTANCEOF node inside an expression, if any.
	 * Stops at SLIST/OBJBLOCK boundaries (doesn't descend into blocks).
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findInstanceof(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_INSTANCEOF)
			return ast;
		if (ast.getType() == TokenTypes.SLIST || ast.getType() == TokenTypes.OBJBLOCK)
			return null;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var result = findInstanceof(child);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Gets the condition expression from an if statement. The condition
	 * is inside the EXPR between LPAREN and RPAREN.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST getIfCondition(@Nonnull DetailAST ifAst) {
		final var lparen = ifAst.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return null;
		final var expr = lparen.getNextSibling();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return null;
		return expr.getFirstChild();
	}

	/**
	 * Checks whether the given AST is a LNOT wrapping an instanceof expression.
	 * Returns the instanceof node if so, null otherwise.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST getInstanceofUnderNot(@Nonnull DetailAST condition) {
		if (condition.getType() != TokenTypes.LNOT)
			return null;
		// search all children (LNOT may contain LPAREN, EXPR, RPAREN)
		for (var child = condition.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var result = findInstanceof(child);
			if (result != null)
				return result;
		}
		return null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_INSTANCEOF};
	}

	private void visitIfWrongBranch(
			@Nonnull DetailAST ifAst,
			@Nonnull DetailAST instanceofAst,
			@Nonnull String typeName,
			@Nonnull String exprStr
	) {
		final var condition = getIfCondition(ifAst);
		if (condition == null)
			return;

		// positive instanceof (not negated) — check else block
		final var elseAst = ifAst.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null && condition.getType() != TokenTypes.LNOT) {
			final var instanceofInCondition = findInstanceof(condition);
			if (instanceofInCondition == instanceofAst) {
				for (var child = elseAst.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (AstUtil.containsCastTo(child, typeName, exprStr)) {
						log(child, MSG_WRONG_BRANCH, typeName);
						return;
					}
				}
			}
		}

		// negated instanceof — check then block
		final var negatedInstanceof = getInstanceofUnderNot(condition);
		if (negatedInstanceof == instanceofAst) {
			final var rparen = ifAst.findFirstToken(TokenTypes.RPAREN);
			if (rparen == null)
				return;
			final var thenBody = rparen.getNextSibling();
			if (thenBody != null && AstUtil.containsCastTo(thenBody, typeName, exprStr))
				log(thenBody, MSG_WRONG_BRANCH, typeName);
		}
	}

	private boolean visitLandChain(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprStr) {
		var node = ast;
		var parent = ast.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.LAND) {
				if (parent.getFirstChild() != node) {
					if (AstUtil.containsCastTo(parent.getFirstChild(), typeName, exprStr)) {
						log(ast, MSG_KEY, typeName);
						return true;
					}
				}
			}
			else if (parent.getType() != TokenTypes.EXPR)
				break;
			node = parent;
			parent = parent.getParent();
		}
		return false;
	}

	private void visitTernaryWrongBranch(
			@Nonnull DetailAST question,
			@Nonnull String typeName,
			@Nonnull String exprStr
	) {
		final var colon = question.findFirstToken(TokenTypes.COLON);
		if (colon == null)
			return;
		final var falseBranch = colon.getNextSibling();
		if (falseBranch == null)
			return;
		if (AstUtil.containsCastTo(falseBranch, typeName, exprStr))
			log(falseBranch, MSG_WRONG_BRANCH, typeName);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF) != null)
			return;

		final var expr = ast.getFirstChild();
		final var type = ast.findFirstToken(TokenTypes.TYPE);
		if (expr == null || type == null)
			return;

		final var typeName = AstUtil.typeText(type);
		final var exprStr = AstUtil.exprText(expr);
		if (typeName.isEmpty())
			return;

		if (visitLandChain(ast, typeName, exprStr))
			return;

		visitWrongBranch(ast, typeName, exprStr);
	}

	private void visitWrongBranch(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprStr) {
		var parent = ast.getParent();
		while (parent != null) {
			switch (parent.getType()) {
				case TokenTypes.EXPR, TokenTypes.LAND, TokenTypes.LNOT, TokenTypes.LOR -> {}
				case TokenTypes.LITERAL_IF -> {
					visitIfWrongBranch(parent, ast, typeName, exprStr);
					return;
				}
				case TokenTypes.QUESTION -> {
					visitTernaryWrongBranch(parent, typeName, exprStr);
					return;
				}
				default -> {
					return;
				}
			}
			parent = parent.getParent();
		}
	}
}