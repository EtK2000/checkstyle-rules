package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that requires for-each loops and try-with-resources
 * to use {@code var} instead of an explicit type.
 */
public class PreferVarCheck extends AbstractCheck {
	private static final String MSG_FOREACH = "prefer.var.foreach";
	private static final String MSG_TRY = "prefer.var.try.resource";

	private void checkVarType(@Nonnull DetailAST varDef, @Nonnull String msgKey) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident == null || !"var".equals(ident.getText()))
			log(varDef, msgKey);
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.FOR_EACH_CLAUSE, TokenTypes.RESOURCE};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.RESOURCE) {
			checkVarType(ast, MSG_TRY);
			return;
		}

		final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef != null)
			checkVarType(varDef, MSG_FOREACH);
	}
}