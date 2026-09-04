package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags trailing commas in array initializers, in both the
 * expression form ({@code int[] x = {1, 2,};}, an {@code ARRAY_INIT}) and the
 * annotation form ({@code @Anno({1, 2,})}, an {@code ANNOTATION_ARRAY_INIT}).
 * Both hang their elements the same way, so one visitor serves both.
 */
public class NoArrayTrailingCommaCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "no.array.trailing.comma";

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATION_ARRAY_INIT, TokenTypes.ARRAY_INIT};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
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