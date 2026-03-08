package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags unnecessary braces in case/default blocks.
 * Braces are only allowed when a variable is defined in the case's direct scope.
 */
public class NoCaseBracesCheck extends AbstractCheck {
	private static final String MSG_MISSING = "case.braces.missing";
	private static final String MSG_UNNECESSARY = "no.case.braces";

	@CheckReturnValue
	private static boolean hasVarDef(@Nonnull DetailAST parent) {
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.CASE_GROUP};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var slist = ast.findFirstToken(TokenTypes.SLIST);
		if (slist == null)
			return;

		// look for a block (SLIST starting with LCURLY) as a direct child
		var hasBlock = false;
		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.SLIST)
				continue;

			hasBlock = true;
			// braces without a variable definition → unnecessary
			if (!hasVarDef(child))
				log(child, MSG_UNNECESSARY);
		}

		// no braces but the case body defines a variable → missing required braces
		if (!hasBlock && hasVarDef(slist))
			log(slist, MSG_MISSING);
	}
}