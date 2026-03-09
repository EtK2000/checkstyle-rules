package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags traditional switch statements that could use enhanced (arrow) syntax.
 * A switch is convertible when every case group has a single statement (with break), return, throw, or yield,
 * or is a fall-through label (no body).
 */
public class PreferEnhancedSwitchCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.enhanced.switch";

	@CheckReturnValue
	private static boolean isConvertible(@Nonnull DetailAST caseGroup) {
		final var slist = caseGroup.findFirstToken(TokenTypes.SLIST);

		// no SLIST means fall-through label (no body), which is fine
		if (slist == null)
			return true;

		// count real statements (exclude RCURLY and SEMI)
		var statementCount = 0;
		var hasTerminator = false;
		var hasCodeBeforeTerminator = false;

		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_BREAK:
					hasTerminator = true;
					if (statementCount > 1)
						return false;
					break;

				case TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW,
				     TokenTypes.LITERAL_YIELD:
					hasTerminator = true;
					++statementCount;
					if (hasCodeBeforeTerminator)
						return false;
					break;

				case TokenTypes.RCURLY, TokenTypes.SEMI:
					break;

				default:
					++statementCount;
					if (statementCount > 1)
						return false;
					hasCodeBeforeTerminator = true;
					break;
			}
		}

		// must have a terminator (otherwise it's actual fall-through with code)
		if (!hasTerminator)
			return statementCount == 0;

		// single statement + break, or single return/throw/yield, or just break
		return statementCount <= 1;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_SWITCH};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// if already enhanced syntax, skip
		if (ast.findFirstToken(TokenTypes.SWITCH_RULE) != null)
			return;

		var hasCaseGroup = false;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.CASE_GROUP)
				continue;

			hasCaseGroup = true;
			if (!isConvertible(child))
				return;
		}

		// empty switch → no violation
		if (hasCaseGroup)
			log(ast, MSG_KEY);
	}
}