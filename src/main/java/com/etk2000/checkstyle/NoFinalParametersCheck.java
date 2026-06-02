package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags {@code final} on parameters (method, constructor,
 * catch, lambda), for-each iteration variables, and for-loop init variables, all
 * as errors. Replaces the fragile {@code NoFinalParameters} regex rule.
 */
public class NoFinalParametersCheck extends AbstractAstCheck {
	static final String MSG_FOR_INIT = "no.final.for.init";
	static final String MSG_FOREACH = "no.final.foreach";
	static final String MSG_PARAMETER = "no.final.parameters";

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.FOR_EACH_CLAUSE, TokenTypes.FOR_INIT, TokenTypes.PARAMETER_DEF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return new int[0];
	}

	private void visitForEach(@Nonnull DetailAST ast) {
		final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var finalToken = varDef.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.FINAL);
		if (finalToken == null)
			return;
		log(finalToken, MSG_FOREACH, varDef.findFirstToken(TokenTypes.IDENT).getText());
	}

	private void visitForInit(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var finalToken = child.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.FINAL);
			if (finalToken == null)
				continue;
			log(finalToken, MSG_FOR_INIT, child.findFirstToken(TokenTypes.IDENT).getText());
		}
	}

	private void visitParameter(@Nonnull DetailAST ast) {
		final var finalToken = ast.findFirstToken(TokenTypes.MODIFIERS).findFirstToken(TokenTypes.FINAL);
		if (finalToken == null)
			return;
		log(finalToken, MSG_PARAMETER, ast.findFirstToken(TokenTypes.IDENT).getText());
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> visitForEach(ast);
			case TokenTypes.FOR_INIT -> visitForInit(ast);
			case TokenTypes.PARAMETER_DEF -> visitParameter(ast);
		}
	}
}