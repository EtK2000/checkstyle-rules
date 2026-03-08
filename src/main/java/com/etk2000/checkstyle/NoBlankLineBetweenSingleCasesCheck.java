package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags blank lines between consecutive single-line switch cases
 * (case + one statement like return/throw/yield).
 */
public class NoBlankLineBetweenSingleCasesCheck extends AbstractCheck {
	private static final String MSG_KEY = "no.blank.line.between.single.cases";

	@CheckReturnValue
	private static boolean isSingleLineCase(@Nonnull DetailAST caseGroup) {
		// a case is "single-line" if the case label + body spans exactly 2 lines
		// (one for the case label, one for the return/throw/yield statement)
		final var startLine = caseGroup.getLineNo();
		final var endLine = AstUtil.lastLine(caseGroup);
		if (endLine - startLine != 1)
			return false;

		final var slist = caseGroup.findFirstToken(TokenTypes.SLIST);
		if (slist == null) {
			for (var child = caseGroup.getFirstChild(); child != null; child = child.getNextSibling()) {
				switch (child.getType()) {
					case TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW,
					     TokenTypes.LITERAL_YIELD:
						return true;

					case TokenTypes.LITERAL_CASE, TokenTypes.LITERAL_DEFAULT:
						break;

					default:
						return false;
				}
			}
			return false;
		}

		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW,
				     TokenTypes.LITERAL_YIELD:
					return true;

				case TokenTypes.SEMI, TokenTypes.RCURLY:
					break;

				default:
					return false;
			}
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
		return new int[]{TokenTypes.LITERAL_SWITCH};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		DetailAST prevSingleCase = null;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.CASE_GROUP) {
				prevSingleCase = null;
				continue;
			}

			if (isSingleLineCase(child)) {
				if (prevSingleCase != null) {
					final var prevEnd = AstUtil.lastLine(prevSingleCase);
					final var currStart = child.getLineNo();
					if (currStart - prevEnd > 1)
						log(child, MSG_KEY);
				}
				prevSingleCase = child;
			}
			else
				prevSingleCase = null;
		}
	}
}