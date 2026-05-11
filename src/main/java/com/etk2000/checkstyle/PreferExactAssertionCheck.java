package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
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
 *
 * <p>{@code instanceof}-form violations are framework-gated: JUnit 4's {@code Assert}
 * has no {@code assertInstanceOf}/{@code assertNotInstanceOf}, so emitting the
 * violation when the call resolves to JUnit 4 would just yield an unfixable hint.
 * Resolution:</p>
 * <ul>
 *     <li>If the call has a receiver whose simple name is {@code Assertions} (JUnit 5
 *         by simple name): fire.</li>
 *     <li>If the receiver's simple name is {@code Assert} (JUnit 4): don't fire.</li>
 *     <li>Otherwise (unqualified, or some other receiver): fire iff the file has a
 *         static import bringing {@code Assertions} into scope and no static import
 *         bringing {@code Assert} into scope. Only static imports count for this
 *         gate; non-static type imports don't enable unqualified method resolution,
 *         so they're irrelevant.</li>
 * </ul>
 * <p>Comparison-form violations fire regardless of framework: JUnit 4 has
 * {@code assertEquals}/{@code assertSame} and friends.</p>
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
	 * Returns the simple class name of the method-call's receiver, or null if
	 * the call is unqualified. For {@code Assert.assertTrue(...)} returns
	 * {@code "Assert"}; for {@code org.junit.jupiter.api.Assertions.assertTrue(...)}
	 * returns {@code "Assertions"} (the rightmost segment of the FQN qualifier).
	 */
	@CheckReturnValue
	@Nullable
	private static String getReceiverSimpleName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null)
			return null;
		return switch (receiver.getType()) {
			case TokenTypes.DOT -> {
				final var last = receiver.getLastChild();
				yield last != null && last.getType() == TokenTypes.IDENT ? last.getText() : null;
			}
			case TokenTypes.IDENT -> receiver.getText();
			default -> null;
		};
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
		while (cur != null && cur.getType() == TokenTypes.LPAREN)
			cur = cur.getNextSibling();
		while (cur != null && cur.getType() == TokenTypes.EXPR)
			cur = cur.getFirstChild();
		return cur;
	}

	private boolean hasJunit4StaticAssert, hasJunit5StaticAssertions;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		hasJunit4StaticAssert = false;
		hasJunit5StaticAssertions = false;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.METHOD_CALL, TokenTypes.STATIC_IMPORT};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	/**
	 * Records the simple class name of a static import for framework detection.
	 * For {@code import static org.junit.Assert.assertTrue;} the trailing method
	 * name is stripped to leave the class FQN {@code org.junit.Assert}, simple
	 * name {@code Assert}. For {@code import static org.junit.Assert.*;} the
	 * trailing {@code .*} is stripped to the same class FQN.
	 *
	 * <p>Only static imports are tracked: non-static imports only bring types
	 * into scope, which doesn't enable unqualified method calls like
	 * {@code assertTrue(...)}. A file with only {@code import org.junit.Assert;}
	 * and an unqualified {@code assertTrue} wouldn't compile, so it doesn't
	 * matter whether we flag the violation. The qualified-call shape
	 * ({@code Assert.assertTrue(...)}) is handled separately by the receiver
	 * check, which works regardless of import form.</p>
	 */
	private void recordStaticImport(@Nonnull String fqn) {
		final var wildcard = fqn.endsWith(".*");
		var classFqn = wildcard ? fqn.substring(0, fqn.length() - 2) : fqn;
		if (!wildcard) {
			final var lastDot = classFqn.lastIndexOf('.');
			if (lastDot < 0)
				return;
			classFqn = classFqn.substring(0, lastDot);
		}
		final var lastDot = classFqn.lastIndexOf('.');
		final var simple = lastDot < 0 ? classFqn : classFqn.substring(lastDot + 1);
		if ("Assert".equals(simple))
			hasJunit4StaticAssert = true;
		else if ("Assertions".equals(simple))
			hasJunit5StaticAssertions = true;
	}

	/**
	 * Decides whether the {@code instanceof}-form violation should be emitted.
	 * See class Javadoc for the full resolution rule.
	 */
	@CheckReturnValue
	private boolean shouldFireInstanceofViolation(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var receiver = getReceiverSimpleName(methodCall);
		if ("Assertions".equals(receiver))
			return true;
		if ("Assert".equals(receiver))
			return false;
		// qualified call whose receiver isn't a simple Assert/Assertions ident
		// (e.g. `getHelper().assertTrue(...)`): the receiver's runtime type could be
		// either framework, so suppress rather than emit a hint the fixer can't fix.
		if (dot != null)
			return false;
		return hasJunit5StaticAssertions && !hasJunit4StaticAssert;
	}

	private void visitMethodCall(@Nonnull DetailAST ast) {
		final var methodName = getMethodName(ast);
		if (!"assertTrue".equals(methodName) && !"assertFalse".equals(methodName))
			return;

		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;

			final var rawExpr = child.getType() == TokenTypes.EXPR
					? child.getFirstChild()
					: child;
			final var expr = unwrapParens(rawExpr);
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
			if (!shouldFireInstanceofViolation(ast))
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

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.METHOD_CALL -> visitMethodCall(ast);

			case TokenTypes.STATIC_IMPORT -> {
				// STATIC_IMPORT children: LITERAL_STATIC, then the DOT/IDENT path, then SEMI.
				final var first = ast.getFirstChild();
				if (first == null)
					return;
				final var pathNode = first.getNextSibling();
				if (pathNode == null)
					return;
				recordStaticImport(FullIdent.createFullIdent(pathNode).getText());
			}
		}
	}
}