package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that requires for-each loops to use {@code var}
 * instead of an explicit type.
 */
public class ForEachVarCheck extends AbstractCheck {
	private static final String MSG_KEY = "foreach.var";

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.FOR_EACH_CLAUSE};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef == null)
			return;

		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident == null || !"var".equals(ident.getText()))
			log(varDef, MSG_KEY);
	}
}