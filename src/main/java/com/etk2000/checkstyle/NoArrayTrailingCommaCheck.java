package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags trailing commas in array initializers.
 */
public class NoArrayTrailingCommaCheck extends AbstractCheck {
	private static final String MSG_KEY = "no.array.trailing.comma";

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ARRAY_INIT};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// find the last child before RCURLY
		DetailAST lastBeforeRCurly = null;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.RCURLY)
				break;
			lastBeforeRCurly = child;
		}

		if (lastBeforeRCurly != null && lastBeforeRCurly.getType() == TokenTypes.COMMA)
			log(lastBeforeRCurly, MSG_KEY);
	}
}