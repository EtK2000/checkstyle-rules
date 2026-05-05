package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags {@code assertTrue}/{@code assertFalse} calls
 * whose argument is a comparison operator or {@code instanceof} expression.
 * There is always a more specific assertion method available:
 * <ul>
 *     <li>{@code assertTrue(a > b)} / {@code >=} / {@code <} / {@code <=} ->
 *         use {@code assertEquals} with an exact expected value</li>
 *     <li>{@code assertTrue(a == b)} -> use {@code assertEquals} or {@code assertSame}</li>
 *     <li>{@code assertTrue(a != b)} -> use {@code assertNotEquals} or {@code assertNotSame}</li>
 *     <li>{@code assertTrue(x instanceof Y)} -> use {@code assertInstanceOf(Y.class, x)}</li>
 *     <li>{@code assertFalse(x instanceof Y)} -> use {@code assertNotInstanceOf(Y.class, x)}</li>
 *     <li>{@code assertTrue(!(x instanceof Y))} -> use {@code assertNotInstanceOf(Y.class, x)}</li>
 *     <li>{@code assertFalse(!(x instanceof Y))} -> use {@code assertInstanceOf(Y.class, x)}</li>
 * </ul>
 * Skips pattern-matching {@code instanceof Y y} (the binding can't be preserved).
 * Handles bare calls, qualified calls ({@code Assert.assertTrue},
 * {@code Assertions.assertTrue}), fully-qualified type references
 * ({@code instanceof java.io.IOException}), and multi-arg forms (JUnit 4/5).
 */
public class PreferExactAssertionCheck extends AbstractCheck {
	private static final String MSG_COMPARISON = "prefer.assert.comparison";
	private static final String MSG_INSTANCEOF = "prefer.assert.instanceof";

	@CheckReturnValue
	@Nullable
	private static String getMethodName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			var last = dot.getFirstChild();
			if (last == null)
				return null;
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getText();
		}

		final var ident = methodCall.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : null;
	}

	/**
	 * Returns the inner {@code LITERAL_INSTANCEOF} token if {@code expr} is an
	 * {@code instanceof} or a chain of {@code !} operators wrapping one. Returns
	 * the count of {@code !} unwraps encountered as the second array element. The
	 * count's parity tells the caller whether to flip the assertion polarity.
	 * Returns {@code null} if no instanceof is reachable through pure-{@code !}
	 * unwrapping.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST instanceOfThroughNots(@Nonnull DetailAST expr, @Nonnull int[] notCount) {
		var cur = expr;
		while (cur != null && cur.getType() == TokenTypes.LNOT) {
			++notCount[0];
			cur = unwrapParens(cur.getFirstChild());
		}
		return cur != null && cur.getType() == TokenTypes.LITERAL_INSTANCEOF ? cur : null;
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

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapParens(@Nullable DetailAST node) {
		var cur = node;
		// skip LPAREN siblings; descend through any EXPR wrappers introduced by parentheses
		while (cur != null && cur.getType() == TokenTypes.LPAREN)
			cur = cur.getNextSibling();
		while (cur != null && cur.getType() == TokenTypes.EXPR)
			cur = cur.getFirstChild();
		return cur;
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
			if (expr == null)
				continue;
			if (isComparisonOperator(expr.getType())) {
				log(ast, MSG_COMPARISON, methodName, operatorText(expr.getType()));
				continue;
			}
			final int[] notCount = {0};
			final var instanceofAst = instanceOfThroughNots(expr, notCount);
			if (instanceofAst == null)
				continue;
			if (instanceofAst.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF) != null)
				continue;
			final var negated = (notCount[0] & 1) == 1;
			final var assertTrueCaller = "assertTrue".equals(methodName);
			// effective polarity: assertTrue(!x) is the same as assertFalse(x); odd `!`s flip it
			final var wantsAssertInstanceOf = assertTrueCaller != negated;
			final var replacement = wantsAssertInstanceOf
					? "assertInstanceOf"
					: "assertNotInstanceOf";
			log(ast, MSG_INSTANCEOF, replacement, methodName);
		}
	}
}