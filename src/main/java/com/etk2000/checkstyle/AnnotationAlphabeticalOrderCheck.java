package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces annotations are sorted alphabetically
 * (both stacked and inline).
 */
public class AnnotationAlphabeticalOrderCheck extends AbstractCheck {
	private static final String MSG_KEY = "annotation.alphabetical.order";

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.MODIFIERS, TokenTypes.ANNOTATIONS};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		String previousName = null;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				continue;

			final var name = AstUtil.annotationName(child);
			if (previousName != null && name.compareTo(previousName) < 0)
				log(child, MSG_KEY, name, previousName);
			previousName = name;
		}
	}
}