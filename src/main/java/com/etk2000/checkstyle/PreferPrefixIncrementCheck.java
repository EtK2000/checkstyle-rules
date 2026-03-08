package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags postfix increment/decrement,
 * preferring prefix form (++i, --i).
 */
public class PreferPrefixIncrementCheck extends AbstractCheck {
	private static final String MSG_DEC = "prefer.prefix.decrement";
	private static final String MSG_INC = "prefer.prefix.increment";

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.POST_INC, TokenTypes.POST_DEC};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.POST_INC)
			log(ast, MSG_INC);
		else
			log(ast, MSG_DEC);
	}
}