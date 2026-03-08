package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces consistent brace usage for control flow statements.
 * <ul>
 *     <li>No one-liners: body must be on its own line</li>
 *     <li>No unnecessary braces: single-line body must not be wrapped in braces</li>
 *     <li>Missing braces: braceless body spanning multiple lines must have braces</li>
 * </ul>
 * Each nesting level is evaluated independently.
 */
public class ControlFlowBracesCheck extends AbstractCheck {
	private static final String MSG_MISSING_BRACES = "control.flow.missing.braces";
	private static final String MSG_ONE_LINER = "control.flow.one.liner";
	private static final String MSG_UNNECESSARY_BRACES = "control.flow.unnecessary.braces";

	@CheckReturnValue
	private static int bodyLineCount(@Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.SLIST)
			return bodyLineCountOfBlock(body);
		return lastLine(body) - body.getLineNo() + 1;
	}

	@CheckReturnValue
	private static int bodyLineCountOfBlock(@Nonnull DetailAST slist) {
		// count lines between the braces (exclusive)
		final var open = slist.getLineNo();
		final var close = slist.findFirstToken(TokenTypes.RCURLY).getLineNo();
		return close - open - 1;
	}

	@CheckReturnValue
	private static DetailAST getBody(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.LITERAL_DO:
				return ast.getFirstChild();

			case TokenTypes.LITERAL_FOR:
			case TokenTypes.LITERAL_IF:
			case TokenTypes.LITERAL_WHILE:
				return ast.findFirstToken(TokenTypes.RPAREN).getNextSibling();

			default:
				throw new IllegalArgumentException("Unexpected token: " + ast);
		}
	}

	@CheckReturnValue
	private static boolean isOneLiner(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.SLIST)
			return false;
		return body.getLineNo() == keyword.getLineNo();
	}

	@CheckReturnValue
	private static int lastLine(@Nonnull DetailAST ast) {
		var last = ast.getLineNo();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childLast = lastLine(child);
			if (childLast > last)
				last = childLast;
		}
		return last;
	}

	private void checkBody(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.EMPTY_STAT)
			return;

		// one-liner check
		if (isOneLiner(keyword, body)) {
			log(keyword, MSG_ONE_LINER);
			return;
		}

		final var lines = bodyLineCount(body);

		if (body.getType() == TokenTypes.SLIST) {
			// braced body — flag if body is single-line
			if (lines == 1)
				log(keyword, MSG_UNNECESSARY_BRACES);
		}
		else if (lines > 1) {
			// braceless body spanning multiple lines
			log(keyword, MSG_MISSING_BRACES);
		}
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.LITERAL_DO,
				TokenTypes.LITERAL_FOR,
				TokenTypes.LITERAL_IF,
				TokenTypes.LITERAL_WHILE
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var body = getBody(ast);
		if (body == null)
			return;

		checkBody(ast, body);

		// check else/else-if
		if (ast.getType() != TokenTypes.LITERAL_IF)
			return;

		final var elseKeyword = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseKeyword == null)
			return;

		final var elseBody = elseKeyword.getFirstChild();
		if (elseBody == null)
			return;

		// else-if: skip missing-braces check (else-if is one construct)
		if (elseBody.getType() == TokenTypes.LITERAL_IF)
			return;

		checkBody(elseKeyword, elseBody);
	}
}