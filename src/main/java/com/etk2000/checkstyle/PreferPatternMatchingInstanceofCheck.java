package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags {@code instanceof} checks where the checked type
 * is subsequently cast to, preferring pattern matching
 * ({@code x instanceof Foo f}) instead.
 */
public class PreferPatternMatchingInstanceofCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "prefer.pattern.instanceof";

	@CheckReturnValue
	private static boolean hasCastInThenBranch(
			@Nonnull DetailAST instanceofAst,
			@Nonnull String typeName,
			@Nonnull String exprStr
	) {
		var parent = instanceofAst.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.LITERAL_IF) {
				final var slist = parent.findFirstToken(TokenTypes.SLIST);
				return slist != null && AstUtil.containsCastTo(slist, typeName, exprStr);
			}
			// &&: right operand only executes when instanceof is true
			if (parent.getType() == TokenTypes.LAND
					&& isInFirstChild(parent, instanceofAst)) {
				final var rightOperand = parent.getFirstChild().getNextSibling();
				if (rightOperand != null && AstUtil.containsCastTo(rightOperand, typeName, exprStr))
					return true;
				// continue walking up to check if-body or outer &&
			}
			if (parent.getType() == TokenTypes.QUESTION
					&& isInFirstChild(parent, instanceofAst)) {
				// search true-branch: children between condition and COLON
				for (var child = parent.getFirstChild().getNextSibling();
				     child != null && child.getType() != TokenTypes.COLON;
				     child = child.getNextSibling()) {
					if (AstUtil.containsCastTo(child, typeName, exprStr))
						return true;
				}
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isInFirstChild(@Nonnull DetailAST parent, @Nonnull DetailAST target) {
		var node = target;
		while (node != null && node.getParent() != parent)
			node = node.getParent();
		return node != null && node == parent.getFirstChild();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_INSTANCEOF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// already using pattern matching
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

		if (hasCastInThenBranch(ast, typeName, exprStr))
			log(ast, MSG_KEY, typeName);
	}
}