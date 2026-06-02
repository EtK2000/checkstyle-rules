package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags unnecessary trailing semicolons in enum
 * definitions. A semicolon after the last enum constant is only needed
 * when the enum has body declarations (methods, fields, constructors,
 * inner types, or initializer blocks) after it.
 */
public class NoEnumTrailingSemicolonCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "no.enum.trailing.semicolon";

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ENUM_DEF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return;

		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var next = child.getNextSibling();
			if (child.getType() == TokenTypes.SEMI && next != null && next.getType() == TokenTypes.RCURLY)
				log(child, MSG_KEY);
		}
	}
}