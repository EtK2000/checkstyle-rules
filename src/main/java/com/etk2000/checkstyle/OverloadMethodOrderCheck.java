package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces overloaded methods are ordered
 * with fewer parameters first.
 */
public class OverloadMethodOrderCheck extends AbstractCheck {
	private static final String MSG_KEY = "overload.method.order";

	@CheckReturnValue
	private static int paramCount(@Nonnull DetailAST methodDef) {
		return methodDef.findFirstToken(TokenTypes.PARAMETERS).getChildCount(TokenTypes.PARAMETER_DEF);
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		String prevName = null;
		var prevParams = 0;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var name = child.findFirstToken(TokenTypes.IDENT).getText();
			final var params = paramCount(child);

			if (name.equals(prevName) && params < prevParams)
				log(child, MSG_KEY, name, params, prevParams);

			prevName = name;
			prevParams = params;
		}
	}
}