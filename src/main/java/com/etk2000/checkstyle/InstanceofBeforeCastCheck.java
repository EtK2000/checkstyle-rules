package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags {@code &&} conditions where a cast to a
 * type appears before an {@code instanceof} check for the same type on
 * the same expression. The {@code instanceof} should come first to
 * enable pattern matching and prevent {@code ClassCastException}.
 */
public class InstanceofBeforeCastCheck extends AbstractCheck {
	private static final String MSG_KEY = "instanceof.before.cast";

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_INSTANCEOF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// pattern matching already used, no cast to find
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

		// walk up through LAND ancestors
		var node = ast;
		var parent = ast.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.LAND) {
				// if we came from the right subtree, search the left for a preceding cast
				if (parent.getFirstChild() != node) {
					if (AstUtil.containsCastTo(parent.getFirstChild(), typeName, exprStr)) {
						log(ast, MSG_KEY, typeName);
						return;
					}
				}
			}
			else if (parent.getType() != TokenTypes.EXPR)
				break;
			node = parent;
			parent = parent.getParent();
		}
	}
}