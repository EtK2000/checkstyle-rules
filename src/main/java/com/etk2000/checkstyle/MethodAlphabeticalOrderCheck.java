package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces methods within each section (static, instance)
 * are sorted alphabetically by name. Overloads (consecutive methods with the same
 * name) are skipped — OverloadMethodOrderCheck handles those.
 */
public class MethodAlphabeticalOrderCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "method.alphabetical.order";

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		String prevInstanceName = null;
		String prevStaticName = null;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var name = child.findFirstToken(TokenTypes.IDENT).getText();

			final var modifiers = child.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null) {
				if (prevStaticName != null && !name.equals(prevStaticName)
						&& name.compareToIgnoreCase(prevStaticName) < 0)
					log(child, MSG_KEY, name, prevStaticName);
				prevStaticName = name;
			}
			else {
				if (prevInstanceName != null && !name.equals(prevInstanceName)
						&& name.compareToIgnoreCase(prevInstanceName) < 0)
					log(child, MSG_KEY, name, prevInstanceName);
				prevInstanceName = name;
			}
		}
	}
}