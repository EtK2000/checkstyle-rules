package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags blank lines between consecutive single-line switch cases
 * (case + one statement like return/throw/yield).
 *
 * <p>Covers both switch syntaxes: a traditional {@code case X:} group is a
 * {@code CASE_GROUP} spanning the label line plus its statement line, while an
 * arrow case ({@code case X -> ...}) is a {@code SWITCH_RULE} that fits on one
 * line. {@code CLAUDE.md} prefers the arrow form, so leaving it out would exempt
 * the syntax the project writes most.
 */
public class NoBlankLineBetweenSingleCasesCheck extends AbstractAstCheck {
	private static final String MSG_BRACED = "no.blank.line.after.braced.case";
	private static final String MSG_KEY = "no.blank.line.between.single.cases";

	@CheckReturnValue
	private static boolean isBracedCase(@Nonnull DetailAST caseNode) {
		final var slist = caseNode.findFirstToken(TokenTypes.SLIST);
		if (slist == null)
			return false;
		// an arrow case's own body block is that SLIST; a colon group's statements
		// already live in one, so its block is a second SLIST nested inside
		if (caseNode.getType() == TokenTypes.SWITCH_RULE)
			return true;
		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.SLIST)
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isCaseNode(@Nonnull DetailAST node) {
		return node.getType() == TokenTypes.CASE_GROUP || node.getType() == TokenTypes.SWITCH_RULE;
	}

	@CheckReturnValue
	private static boolean isSingleLineCase(@Nonnull DetailAST caseNode) {
		final var startLine = caseNode.getLineNo();
		final var endLine = AstQuery.lastLine(caseNode);

		// an arrow case carries its body on the label line, so "single-line" is
		// literal; a braced body is excluded because its closing brace already
		// separates it from the next case
		if (caseNode.getType() == TokenTypes.SWITCH_RULE)
			return endLine == startLine && !isBracedCase(caseNode);

		// a colon case is "single-line" if the case label + body spans exactly 2 lines
		// (one for the case label, one for the return/throw/yield statement)
		if (endLine - startLine != 1)
			return false;

		final var slist = caseNode.findFirstToken(TokenTypes.SLIST);
		if (slist == null) {
			for (var child = caseNode.getFirstChild(); child != null; child = child.getNextSibling()) {
				switch (child.getType()) {
					case TokenTypes.LITERAL_CASE, TokenTypes.LITERAL_DEFAULT -> {}
					case TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW,
					     TokenTypes.LITERAL_YIELD -> {
						return true;
					}
					default -> {
						return false;
					}
				}
			}
			return false;
		}

		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_RETURN, TokenTypes.LITERAL_THROW,
				     TokenTypes.LITERAL_YIELD -> {
					return true;
				}
				case TokenTypes.RCURLY, TokenTypes.SEMI -> {}
				default -> {
					return false;
				}
			}
		}
		return false;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_SWITCH};
	}

	/**
	 * Whether a blank line sits strictly between 1-based lines {@code prevEnd} and
	 * {@code currStart}. The gap's line count cannot answer this: a comment between
	 * two cases widens it just as a blank line does, and reporting that is a false
	 * positive the fixer cannot resolve, since deleting the comment is not what the
	 * rule asks for.
	 */
	@CheckReturnValue
	private boolean hasBlankLineBetween(int prevEnd, int currStart) {
		final var contents = getFileContents();
		for (var lineNo = prevEnd + 1; lineNo < currStart; ++lineNo) {
			if (contents.lineIsBlank(lineNo - 1))
				return true;
		}
		return false;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		DetailAST prevCase = null;
		var prevBraced = false;
		DetailAST prevSingleCase = null;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!isCaseNode(child)) {
				prevCase = null;
				prevBraced = false;
				prevSingleCase = null;
				continue;
			}

			if (prevBraced && prevCase != null
					&& hasBlankLineBetween(AstQuery.lastLine(prevCase), child.getLineNo()))
				log(child, MSG_BRACED);

			if (isSingleLineCase(child)) {
				if (prevSingleCase != null
						&& hasBlankLineBetween(AstQuery.lastLine(prevSingleCase), child.getLineNo()))
					log(child, MSG_KEY);
				prevSingleCase = child;
			}
			else
				prevSingleCase = null;

			prevBraced = isBracedCase(child);
			prevCase = child;
		}
	}
}