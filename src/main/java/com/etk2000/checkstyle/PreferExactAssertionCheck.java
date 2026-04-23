package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags {@code assertTrue}/{@code assertFalse} calls
 * whose argument is a comparison operator. There is always a more specific
 * assertion method available:
 * <ul>
 *     <li>{@code assertTrue(a > b)} / {@code >=} / {@code <} / {@code <=} ->
 *         use {@code assertEquals} with an exact expected value</li>
 *     <li>{@code assertTrue(a == b)} -> use {@code assertEquals} or {@code assertSame}</li>
 *     <li>{@code assertTrue(a != b)} -> use {@code assertNotEquals} or {@code assertNotSame}</li>
 * </ul>
 * Handles bare calls, qualified calls ({@code Assert.assertTrue},
 * {@code Assertions.assertTrue}), and multi-arg forms (JUnit 4/5).
 */
public class PreferExactAssertionCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.assert.comparison";

	@CheckReturnValue
	@Nullable
	private static String getMethodName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			var last = dot.getFirstChild();
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getText();
		}

		final var ident = methodCall.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : null;
	}

	private static boolean isComparisonOperator(int tokenType) {
		return tokenType == TokenTypes.EQUAL
				|| tokenType == TokenTypes.GE
				|| tokenType == TokenTypes.GT
				|| tokenType == TokenTypes.LE
				|| tokenType == TokenTypes.LT
				|| tokenType == TokenTypes.NOT_EQUAL;
	}

	@CheckReturnValue
	@Nonnull
	private static String operatorText(int tokenType) {
		return switch (tokenType) {
			case TokenTypes.EQUAL -> "==";
			case TokenTypes.GE -> ">=";
			case TokenTypes.GT -> ">";
			case TokenTypes.LE -> "<=";
			case TokenTypes.LT -> "<";
			case TokenTypes.NOT_EQUAL -> "!=";
			default -> "?";
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
		return new int[]{TokenTypes.METHOD_CALL};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var methodName = getMethodName(ast);
		if (!"assertTrue".equals(methodName) && !"assertFalse".equals(methodName))
			return;

		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;

			final var expr = child.getType() == TokenTypes.EXPR
					? child.getFirstChild()
					: child;
			if (expr != null && isComparisonOperator(expr.getType()))
				log(ast, MSG_KEY, methodName, operatorText(expr.getType()));
		}
	}
}