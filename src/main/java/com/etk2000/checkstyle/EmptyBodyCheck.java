package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags empty if/else-if/else bodies.
 * When removing, preserve any side effects in the condition.
 */
public class EmptyBodyCheck extends AbstractCheck {
	private static final String MSG_ELSE = "empty.else";
	private static final String MSG_IF = "empty.if";

	@CheckReturnValue
	private static boolean isEmptyBody(@Nonnull DetailAST body) {
		return switch (body.getType()) {
			// empty statement: if (x);
			case TokenTypes.EMPTY_STAT -> true;
			// empty block: if (x) {}
			case TokenTypes.SLIST -> body.getChildCount() == 1
					&& body.getFirstChild().getType() == TokenTypes.RCURLY;
			default -> false;
		};
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// check the "then" body (child after LPAREN, EXPR, RPAREN)
		final var thenBody = ast.findFirstToken(TokenTypes.SLIST);
		if (thenBody != null && isEmptyBody(thenBody))
			log(ast, MSG_IF);
		else if (thenBody == null) {
			// no SLIST means single statement or empty statement
			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.EMPTY_STAT) {
					log(ast, MSG_IF);
					break;
				}
			}
		}

		// check else branch
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst == null)
			return;

		// else body is the first child of LITERAL_ELSE
		final var elseBody = elseAst.getFirstChild();
		if (elseBody != null && isEmptyBody(elseBody))
			log(elseAst, MSG_ELSE);
	}
}