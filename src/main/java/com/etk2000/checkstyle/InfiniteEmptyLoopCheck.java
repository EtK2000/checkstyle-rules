package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags explicit infinite loops with empty bodies:
 * {@code for(;;);}, {@code while(true);}, and {@code do; while(true);}.
 * These are almost certainly bugs (the program will hang).
 */
public class InfiniteEmptyLoopCheck extends AbstractAstCheck {
	private static final String MSG_DO = "empty.infinite.do";
	private static final String MSG_FOR = "empty.infinite.for";
	private static final String MSG_WHILE = "empty.infinite.while";

	/**
	 * Checks if a do-while has a {@code true} condition: {@code do ... while(true)}.
	 */
	@CheckReturnValue
	private static boolean isInfiniteDoWhile(@Nonnull DetailAST doAst) {
		final var rparen = doAst.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return false;
		final var expr = rparen.getPreviousSibling();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return false;
		final var child = expr.getFirstChild();
		return child != null && child.getType() == TokenTypes.LITERAL_TRUE;
	}

	/**
	 * Checks if a for loop is infinite: {@code for(;;)} or {@code for(;true;)}.
	 * Requires empty init and iterator; condition must be either empty or {@code true}.
	 */
	@CheckReturnValue
	private static boolean isInfiniteFor(@Nonnull DetailAST forAst) {
		final var cond = forAst.findFirstToken(TokenTypes.FOR_CONDITION);
		final var init = forAst.findFirstToken(TokenTypes.FOR_INIT);
		final var iter = forAst.findFirstToken(TokenTypes.FOR_ITERATOR);
		if (init == null || init.getChildCount() != 0 || iter == null || iter.getChildCount() != 0)
			return false;
		if (cond == null)
			return false;
		// for(;;) — empty condition
		if (cond.getChildCount() == 0)
			return true;
		// for(;true;) — literal true condition
		final var expr = cond.getFirstChild();
		if (expr != null && expr.getType() == TokenTypes.EXPR) {
			final var child = expr.getFirstChild();
			return child != null && child.getType() == TokenTypes.LITERAL_TRUE;
		}
		return false;
	}

	/**
	 * Checks if a while loop has a {@code true} condition: {@code while(true)}.
	 */
	@CheckReturnValue
	private static boolean isInfiniteWhile(@Nonnull DetailAST whileAst) {
		final var expr = whileAst.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return false;
		final var child = expr.getFirstChild();
		return child != null && child.getType() == TokenTypes.LITERAL_TRUE;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.LITERAL_DO,
				TokenTypes.LITERAL_FOR,
				TokenTypes.LITERAL_WHILE
		};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.LITERAL_DO -> {
				final var body = ast.getFirstChild();
				if (body != null && AstUtil.isEmptyBody(body) && isInfiniteDoWhile(ast))
					log(ast, MSG_DO);
			}
			case TokenTypes.LITERAL_FOR -> {
				final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
				if (rparen != null) {
					final var body = rparen.getNextSibling();
					if (body != null && AstUtil.isEmptyBody(body) && isInfiniteFor(ast))
						log(ast, MSG_FOR);
				}
			}
			case TokenTypes.LITERAL_WHILE -> {
				final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
				if (rparen != null) {
					final var body = rparen.getNextSibling();
					if (body != null && AstUtil.isEmptyBody(body) && isInfiniteWhile(ast))
						log(ast, MSG_WHILE);
				}
			}
		}
	}
}