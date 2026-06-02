package com.etk2000.checkstyle;

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
 *     <li>{@code assertTrue(!flag)} -> use {@code assertFalse(flag)}</li>
 *     <li>{@code assertFalse(!flag)} -> use {@code assertTrue(flag)}</li>
 * </ul>
 * For pattern-matching {@code instanceof Y y}: the binding is only reachable when the
 * assertion's effective polarity is TRUE (the runtime path that retains the binding).
 * Cases that effectively assert {@code instanceof} TRUE
 * ({@code assertTrue(x instanceof Y y)}, {@code assertFalse(!(x instanceof Y y))}) are
 * skipped to preserve the binding; cases that effectively assert FALSE
 * ({@code assertFalse(x instanceof Y y)}, {@code assertTrue(!(x instanceof Y y))}) drop
 * the unreachable binding and rewrite to {@code assertNotInstanceOf}.
 * Handles bare calls, qualified calls ({@code Assert.assertTrue},
 * {@code Assertions.assertTrue}), fully-qualified type references
 * ({@code instanceof java.io.IOException}), and multi-arg forms (JUnit 4/5).
 *
 * <p>All three forms (comparison, instanceof, negation) require evidence that the
 * call resolves to a JUnit assertion method. Without that evidence the swap is
 * unsafe (e.g. {@code helper.assertTrue(...)} where {@code helper} is some custom
 * object that may not have a matching {@code assertFalse}/{@code assertNotEquals}
 * method). The gate:</p>
 * <ul>
 *     <li>If the call has a receiver whose simple name is {@code Assert} (JUnit 4)
 *         or {@code Assertions} (JUnit 5): fire (subject to the framework-specific
 *         constraint below).</li>
 *     <li>If the call is qualified but the receiver is anything else (custom
 *         helper, chained expression, parenthesized expression): don't fire.</li>
 *     <li>If the call is unqualified: fire iff the file has a static import
 *         bringing {@code Assert} or {@code Assertions} into scope. Only static
 *         imports count for this gate; non-static type imports don't enable
 *         unqualified method resolution, so they're irrelevant.</li>
 * </ul>
 * <p>{@code instanceof}-form violations have an additional framework constraint:
 * JUnit 4's {@code Assert} has no {@code assertInstanceOf}/{@code assertNotInstanceOf}.
 * So {@code instanceof}-form only fires when the receiver simple name is
 * {@code Assertions}, or (unqualified) when only {@code Assertions} is in scope.</p>
 */
public class PreferExactAssertionCheck extends AbstractAstCheck {
	/** JUnit 4's assertion holder class ({@code org.junit.Assert}), by simple name. */
	public static final String ASSERT_CLASS = "Assert";
	/** JUnit 5's assertion holder class ({@code org.junit.jupiter.api.Assertions}), by simple name. */
	public static final String ASSERTIONS_CLASS = "Assertions";
	private static final String MSG_KEY = "prefer.assert";

	/**
	 * Specific JUnit assertion that replaces {@code assertTrue}/{@code assertFalse}
	 * with a comparison operator. For {@code ==}/{@code !=} the choice pairs the
	 * operator's effective equality with the outer method's polarity:
	 * {@code assertTrue(a == b)} and {@code assertFalse(a != b)} both effectively
	 * assert equality, hence {@code assertEquals}; the inverses use
	 * {@code assertNotEquals}. Relational operators ({@code <}, {@code <=},
	 * {@code >}, {@code >=}) don't map to a JUnit assertion of their own, so we
	 * suggest {@code assertEquals} per the class Javadoc ("with an exact
	 * expected value") as the canonical rewrite target.
	 */
	@CheckReturnValue
	@Nonnull
	private static String comparisonReplacement(@Nonnull String methodName, int opType) {
		final var assertTrueCaller = "assertTrue".equals(methodName);
		return switch (opType) {
			case TokenTypes.EQUAL -> assertTrueCaller ? "assertEquals" : "assertNotEquals";
			case TokenTypes.NOT_EQUAL -> assertTrueCaller ? "assertNotEquals" : "assertEquals";
			default -> "assertEquals";
		};
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

	/**
	 * Whether {@code simpleName} is a recognized JUnit assertion holder class
	 * ({@link #ASSERT_CLASS} or {@link #ASSERTIONS_CLASS}). Shared with
	 * {@code PreferExactAssertionFixer} so both apply the same whitelist.
	 */
	@CheckReturnValue
	public static boolean isJunitAssertClass(@Nullable String simpleName) {
		return ASSERT_CLASS.equals(simpleName) || ASSERTIONS_CLASS.equals(simpleName);
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

	/**
	 * Returns whether swapping {@code assertTrue}/{@code assertFalse} to a different
	 * JUnit assertion method is safe for this call site. Safe means we have evidence
	 * the call resolves to a JUnit {@code Assert} or {@code Assertions} class, so
	 * the replacement method ({@code assertFalse}/{@code assertEquals}/...) is
	 * guaranteed to exist on the same receiver. See class Javadoc for the rule.
	 */
	@CheckReturnValue
	private boolean canSwapAssertMethod(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var receiver = getReceiverSimpleName(methodCall);
		if (isJunitAssertClass(receiver))
			return true;
		if (dot != null)
			return false;
		return hasJunit4StaticAssert || hasJunit5StaticAssertions;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.METHOD_CALL, TokenTypes.STATIC_IMPORT};
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
		final var simple = AstUtil.simpleName(classFqn);
		if (ASSERT_CLASS.equals(simple))
			hasJunit4StaticAssert = true;
		else if (ASSERTIONS_CLASS.equals(simple))
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
		if (ASSERTIONS_CLASS.equals(receiver))
			return true;
		if (ASSERT_CLASS.equals(receiver))
			return false;
		return dot == null && hasJunit5StaticAssertions && !hasJunit4StaticAssert;
	}

	private void visitMethodCall(@Nonnull DetailAST ast) {
		final var methodName = AstUtil.getMethodName(ast);
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
			final var exprType = expr.getType();
			if (exprType == TokenTypes.EQUAL || exprType == TokenTypes.GE
					|| exprType == TokenTypes.GT || exprType == TokenTypes.LE
					|| exprType == TokenTypes.LT || exprType == TokenTypes.NOT_EQUAL) {
				if (!canSwapAssertMethod(ast))
					continue;
				final var opText = operatorText(expr.getType());
				log(ast, MSG_KEY, comparisonReplacement(methodName, expr.getType()), methodName, "'" + opText + "'");
				continue;
			}
			final int[] notCount = {0};
			final var instanceofAst = instanceOfThroughNots(expr, notCount);
			var instanceofEmitted = false;
			if (instanceofAst != null && shouldFireInstanceofViolation(ast)) {
				final var negated = (notCount[0] & 1) == 1;
				final var assertTrueCaller = "assertTrue".equals(methodName);
				final var wantsAssertInstanceOf = assertTrueCaller != negated;
				if (instanceofAst.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF) == null
						|| !wantsAssertInstanceOf) {
					final var replacement = wantsAssertInstanceOf
							? "assertInstanceOf"
							: "assertNotInstanceOf";
					log(ast, MSG_KEY, replacement, methodName, "'instanceof'");
					instanceofEmitted = true;
				}
			}
			if (!instanceofEmitted && expr.getType() == TokenTypes.LNOT && canSwapAssertMethod(ast)) {
				final var opposite = "assertTrue".equals(methodName) ? "assertFalse" : "assertTrue";
				log(ast, MSG_KEY, opposite, methodName, "a negated argument");
			}
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