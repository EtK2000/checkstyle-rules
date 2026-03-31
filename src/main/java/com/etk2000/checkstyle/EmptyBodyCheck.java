package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags empty if/else-if/else bodies (error) and
 * empty while/for/do-while bodies (warning, since spin-waits are a valid use case).
 * When removing, preserve any side effects in the condition.
 *
 * @see InfiniteEmptyLoopCheck for infinite empty loops at error severity
 */
public class EmptyBodyCheck extends AbstractCheck {
	private static final String MSG_DO = "empty.do";
	private static final String MSG_ELSE = "empty.else";
	private static final String MSG_FOR = "empty.for";
	private static final String MSG_IF = "empty.if";
	private static final String MSG_WHILE = "empty.while";

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
		return new int[0];
	}

	private void visitDo(@Nonnull DetailAST ast) {
		final var body = ast.getFirstChild();
		if (body != null && AstUtil.isEmptyBody(body))
			log(ast, MSG_DO);
	}

	private void visitIf(@Nonnull DetailAST ast) {
		// check the "then" body (child after LPAREN, EXPR, RPAREN)
		final var thenBody = ast.findFirstToken(TokenTypes.SLIST);
		if (thenBody != null && AstUtil.isEmptyBody(thenBody))
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
		if (elseBody != null && AstUtil.isEmptyBody(elseBody))
			log(elseAst, MSG_ELSE);
	}

	private void visitLoop(@Nonnull DetailAST ast, @Nonnull String msgKey) {
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var body = rparen.getNextSibling();
		if (body != null && AstUtil.isEmptyBody(body))
			log(ast, msgKey);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.LITERAL_DO -> visitDo(ast);
			case TokenTypes.LITERAL_FOR -> visitLoop(ast, MSG_FOR);
			case TokenTypes.LITERAL_IF -> visitIf(ast);
			case TokenTypes.LITERAL_WHILE -> visitLoop(ast, MSG_WHILE);
		}
	}
}