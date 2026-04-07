package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags postfix increment/decrement,
 * preferring prefix form (++i, --i). Only flags when the
 * return value is unused (standalone statement or for-loop
 * update), since changing {@code return i++} to {@code return ++i}
 * would alter behavior.
 */
public class PreferPrefixIncrementCheck extends AbstractCheck {
	private static final String MSG_DEC = "prefer.prefix.decrement";
	private static final String MSG_INC = "prefer.prefix.increment";

	/**
	 * Returns true when the postfix result value is discarded, meaning
	 * prefix form is a safe drop-in replacement.
	 */
	@CheckReturnValue
	private static boolean isAfterRparen(@Nonnull DetailAST ast) {
		final var prev = ast.getPreviousSibling();
		return prev != null && prev.getType() == TokenTypes.RPAREN;
	}

	@CheckReturnValue
	private static boolean isValueDiscarded(@Nonnull DetailAST postfix) {
		final var parent = postfix.getParent();
		if (parent == null || parent.getType() != TokenTypes.EXPR)
			return false;
		final var grandparent = parent.getParent();
		if (grandparent == null)
			return false;
		// standalone statement: EXPR -> SLIST
		if (grandparent.getType() == TokenTypes.SLIST)
			return true;
		// for-loop update: EXPR -> ELIST -> FOR_ITERATOR
		if (grandparent.getType() == TokenTypes.ELIST) {
			final var greatGrandparent = grandparent.getParent();
			return greatGrandparent != null && greatGrandparent.getType() == TokenTypes.FOR_ITERATOR;
		}
		// braceless control flow body: EXPR follows RPAREN in if/while/for/do
		if (isAfterRparen(parent)) {
			return switch (grandparent.getType()) {
				case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_FOR,
				     TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE -> true;
				default -> false;
			};
		}
		// braceless do-while body: EXPR is first child of LITERAL_DO (before RPAREN)
		if (grandparent.getType() == TokenTypes.LITERAL_DO && grandparent.getFirstChild() == parent)
			return true;
		// braceless else body: EXPR -> LITERAL_ELSE (no RPAREN)
		if (grandparent.getType() == TokenTypes.LITERAL_ELSE)
			return true;
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
		return new int[]{TokenTypes.POST_INC, TokenTypes.POST_DEC};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (!isValueDiscarded(ast))
			return;
		if (ast.getType() == TokenTypes.POST_INC)
			log(ast, MSG_INC);
		else
			log(ast, MSG_DEC);
	}
}