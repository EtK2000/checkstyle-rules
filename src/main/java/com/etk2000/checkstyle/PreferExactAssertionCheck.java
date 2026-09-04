package com.etk2000.checkstyle;

import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.etk2000.checkstyle.format.SpanEdges;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

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
 *     <li>If the call has a receiver naming {@code Assert} (JUnit 4) or
 *         {@code Assertions} (JUnit 5), written as a bare name or as the full
 *         {@code org.junit.Assert}/{@code org.junit.jupiter.api.Assertions}: fire
 *         (subject to the framework-specific constraint below).</li>
 *     <li>If the call is qualified but the receiver is anything else (custom
 *         helper, chained expression, parenthesized expression): don't fire.</li>
 *     <li>If the call is unqualified: fire iff the file has a static import from
 *         {@code org.junit.Assert} or {@code org.junit.jupiter.api.Assertions}.
 *         Only static imports count for this gate; non-static type imports don't
 *         enable unqualified method resolution, so they're irrelevant.</li>
 * </ul>
 * <p>{@code instanceof}-form violations have an additional framework constraint:
 * JUnit 4's {@code Assert} has no {@code assertInstanceOf}/{@code assertNotInstanceOf}.
 * So {@code instanceof}-form only fires when the receiver names {@code Assertions},
 * or (unqualified) when only {@code Assertions} is in scope.</p>
 */
public class PreferExactAssertionCheck extends AbstractAstCheck {
	/** Why {@link #rewriteOf} refuses a reported call. */
	public enum AssertionRefusal {
		/** {@code instanceof @A Foo}: the annotation has no place on the {@code .class} literal. */
		ANNOTATED_TYPE,
		/** More arguments than a condition and a message, so no replacement overload is known to exist. */
		ARITY,
		/**
		 * A comparison operator. No rewrite is equivalent without the operand types:
		 * {@code assertTrue(Double.NaN == x)} fails where {@code assertEquals(Double.NaN, x)} passes,
		 * and {@code assertTrue(0.0 == -0.0)} passes where {@code assertEquals(0.0, -0.0)} fails.
		 */
		COMPARISON,
		/** {@code instanceof List<String>}: {@code List<String>.class} is not Java. */
		GENERIC_TYPE,
		/** {@code instanceof Pt(int x, int y)}: dropping the pattern would drop bindings still in use. */
		RECORD_PATTERN,
		/** The call's source span does not resolve against the buffer the rewrite would be spliced into. */
		UNSLICEABLE
	}

	/** Which of the three rewrites a reported call qualifies for. */
	public enum AssertionRule {
		COMPARISON,
		INSTANCEOF,
		NEGATION
	}

	/**
	 * One violation the check reports. {@code argIndex} is the position among the {@code ELIST}'s
	 * value arguments.
	 *
	 * <p>{@code notCount} is how many {@code !} the classifier unwrapped while looking for an
	 * {@code instanceof}, so it counts the whole chain rather than the one the rewrite consumes.
	 * Its parity is authoritative for polarity on {@link AssertionRule#INSTANCEOF} targets only,
	 * where the chain is what flips {@code assertInstanceOf} to {@code assertNotInstanceOf}. A
	 * {@link AssertionRule#NEGATION} rewrite instead strips exactly one {@code !} and takes its
	 * polarity from {@link #replacementMethod()}: {@code assertTrue(!!flag)} reports
	 * {@code notCount = 2} alongside {@code replacementMethod = "assertFalse"}, so a consumer
	 * reading {@code notCount % 2} would emit the inverse assertion.
	 *
	 * @see #locateAt
	 */
	public record AssertionTarget(
			@Nonnull AssertionRule rule,
			@Nonnull DetailAST methodCall,
			@Nullable String qualifierSimpleName,
			@Nonnull String callerMethod,
			@Nonnull String replacementMethod,
			@Nonnull String operandDescription,
			int argIndex,
			@Nonnull DetailAST argExpr,
			@Nullable DetailAST instanceofAst,
			@Nullable DetailAST patternBinding,
			int notCount,
			@Nullable DetailAST comparisonOp
	) {}

	/** A reported call the rewrite refuses, naming why. */
	public record CannotRewrite(@Nonnull AssertionRefusal reason) implements Rewrite {}

	/**
	 * Which JUnit assertion holders a file's static imports bring into scope, which is what decides
	 * whether an <em>unqualified</em> {@code assertTrue} resolves to one of them.
	 */
	public record JunitScope(boolean junit4Static, boolean junit5Static) {}

	/**
	 * The replacement text for the one argument the rewrite changes, and the source regions it was taken
	 * from: text copied from a kept region carries any comment inside it, so those regions are what
	 * {@link SpanComments#relocate} must leave alone.
	 */
	private record Pieces(@Nonnull List<String> texts, @Nonnull List<TextSpan> kept) {}

	/** What the fixer does with a reported call: see {@link #rewriteOf}. */
	public sealed interface Rewrite permits Rewritten, CannotRewrite {}

	/**
	 * The call text that replaces the span {@link #spanOf} describes, and the comments the rewrite could
	 * not leave where they were, merged into one trailing {@code //} comment (empty when none had to
	 * move). {@code call} may span lines, because an argument carried over verbatim may itself be a text
	 * block whose line breaks are part of its value.
	 */
	public record Rewritten(@Nonnull String call, @Nonnull String comment) implements Rewrite {}

	/** JUnit 4's assertion holder class ({@code org.junit.Assert}), by simple name. */
	public static final String ASSERT_CLASS = "Assert";
	/** JUnit 4's assertion holder class in full. */
	public static final String ASSERT_FQCN = "org.junit.Assert";
	/** JUnit 5's assertion holder class ({@code org.junit.jupiter.api.Assertions}), by simple name. */
	public static final String ASSERTIONS_CLASS = "Assertions";
	/** JUnit 5's assertion holder class in full. */
	public static final String ASSERTIONS_FQCN = "org.junit.jupiter.api.Assertions";
	private static final String MSG_KEY = "prefer.assert";

	/**
	 * The {@code Type.class} and operand texts that replace an {@code instanceof} argument, in the order
	 * the replacement assertion takes them. Both are sliced from the source rather than rebuilt from the
	 * type's tokens, so a fully-qualified name keeps its spelling and a comment beside either operand
	 * stays beside it.
	 */
	@CheckReturnValue
	@Nullable
	private static Pieces argPiecesForInstanceof(
			@Nonnull List<String> lines, @Nonnull AssertionTarget target, @Nonnull TextSpan region
	) {
		final var instanceofAst = target.instanceofAst();
		final var type = instanceofAst == null ? null : typeNodeOf(instanceofAst);
		final var inner = innerRegion(lines, target.argExpr(), region, target.notCount());
		if (type == null || inner == null)
			return null;
		final var keywordStart = AstSpan.tokenStart(lines, instanceofAst);
		final var keywordEnd = AstSpan.tokenEnd(lines, instanceofAst);
		final var typeEnd = AstSpan.spanEnd(lines, type);
		if (keywordStart == null || keywordEnd == null || typeEnd == null)
			return null;
		final var operandRegion = new TextSpan(inner.start(), keywordStart);
		final var typeRegion = new TextSpan(keywordEnd, typeEnd);
		final var operand = pieceText(lines, operandRegion);
		final var typeText = pieceText(lines, typeRegion);
		if (operand == null || operand.isEmpty() || typeText == null || typeText.isEmpty())
			return null;
		return new Pieces(List.of(typeText + ".class", operand), List.of(operandRegion, typeRegion));
	}

	/**
	 * The text that replaces a negated argument: the argument with exactly one {@code !} and the
	 * parentheses that {@code !} wrapped removed. One rather than the whole chain, because
	 * {@link AssertionTarget#replacementMethod} flipped the assertion once; a longer chain is reported
	 * again on the rewritten call and unwinds one level per pass.
	 */
	@CheckReturnValue
	@Nullable
	private static Pieces argPiecesForNegation(
			@Nonnull List<String> lines, @Nonnull AssertionTarget target, @Nonnull TextSpan region
	) {
		final var inner = innerRegion(lines, target.argExpr(), region, 1);
		final var text = inner == null ? null : pieceText(lines, inner);
		return text == null || text.isEmpty() ? null : new Pieces(List.of(text), List.of(inner));
	}

	/**
	 * The call's value arguments as source regions, in the order {@link #classify} indexes them. A region
	 * runs from just past the delimiter that opens it to the one that closes it, so it is wider than the
	 * argument's own node span: a comment written just inside a parenthesis or beside a comma belongs to
	 * the argument's text, not to the structure around it. Null when a delimiter does not resolve.
	 */
	@CheckReturnValue
	@Nullable
	private static List<TextSpan> argRegions(@Nonnull List<String> lines, @Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		final var rparen = methodCall.findFirstToken(TokenTypes.RPAREN);
		if (elist == null || rparen == null)
			return null;
		// the METHOD_CALL token is the `(` itself, so the call node opens the first region
		final var bounds = new ArrayList<DetailAST>();
		bounds.add(methodCall);
		for (var child = AstText.firstRealChild(elist); child != null; child = AstText.nextRealSibling(child)) {
			if (child.getType() == TokenTypes.COMMA)
				bounds.add(child);
		}
		bounds.add(rparen);

		final var regions = new ArrayList<TextSpan>(bounds.size() - 1);
		for (var i = 0; i + 1 < bounds.size(); ++i) {
			final var start = AstSpan.tokenEnd(lines, bounds.get(i));
			final var end = AstSpan.tokenStart(lines, bounds.get(i + 1));
			if (start == null || end == null)
				return null;
			regions.add(new TextSpan(start, end));
		}
		return regions;
	}

	/**
	 * Returns whether swapping {@code assertTrue}/{@code assertFalse} to a different JUnit assertion
	 * method is safe for this call site. Safe means we have evidence the call resolves to a JUnit
	 * {@code Assert} or {@code Assertions} class, so the replacement method is guaranteed to exist on
	 * the same receiver. See class Javadoc for the rule.
	 */
	@CheckReturnValue
	private static boolean canSwapAssertMethod(@Nonnull DetailAST methodCall, @Nonnull JunitScope scope) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (junitHolderOf(methodCall) != null)
			return true;
		if (dot != null)
			return false;
		return scope.junit4Static() || scope.junit5Static();
	}

	/**
	 * Every violation {@code methodCall} warrants, in the order {@link #visitToken} logs them: one per
	 * offending {@code ELIST} argument. Empty when the call is not a recognized assertion, or when no
	 * argument offends, or when the framework gate refuses.
	 *
	 * <p>This is the single decision both the check and the fixer run on, so neither re-derives it.
	 *
	 * <p>{@code lines} is the buffer the rewrite would be spliced into, which the line-length gate
	 * measures: a call whose replacement would not fit is left unreported rather than reported and then
	 * refused.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<AssertionTarget> classify(
			@Nonnull DetailAST methodCall, @Nonnull List<String> lines, @Nonnull JunitScope scope
	) {
		final var methodName = AstQuery.getMethodName(methodCall);
		if (!"assertTrue".equals(methodName) && !"assertFalse".equals(methodName))
			return List.of();

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return List.of();

		final var qualifier = getReceiverSimpleName(methodCall);
		final var targets = new ArrayList<AssertionTarget>();
		var argIndex = 0;
		for (var child = AstText.firstRealChild(elist); child != null; child = AstText.nextRealSibling(child)) {
			if (child.getType() == TokenTypes.COMMA)
				continue;

			final var rawExpr = child.getType() == TokenTypes.EXPR ? AstText.firstRealChild(child) : child;
			final var expr = unwrapParens(rawExpr);
			++argIndex;
			if (expr == null)
				continue;
			final var exprType = expr.getType();
			if (exprType == TokenTypes.EQUAL || exprType == TokenTypes.GE
					|| exprType == TokenTypes.GT || exprType == TokenTypes.LE
					|| exprType == TokenTypes.LT || exprType == TokenTypes.NOT_EQUAL) {
				if (canSwapAssertMethod(methodCall, scope)) {
					targets.add(new AssertionTarget(
							AssertionRule.COMPARISON,
							methodCall,
							qualifier,
							methodName,
							comparisonReplacement(methodName, exprType),
							"'" + operatorText(exprType) + "'",
							argIndex - 1,
							child,
							null,
							null,
							0,
							expr
					));
				}
				continue;
			}
			final int[] notCount = {0};
			final var instanceofAst = instanceOfThroughNots(expr, notCount);
			var instanceofEmitted = false;
			if (instanceofAst != null && shouldFireInstanceofViolation(methodCall, scope)) {
				final var negated = (notCount[0] & 1) == 1;
				final var wantsAssertInstanceOf = "assertTrue".equals(methodName) != negated;
				final var binding = instanceofAst.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF);
				if (binding == null || !wantsAssertInstanceOf) {
					final var target = new AssertionTarget(
							AssertionRule.INSTANCEOF,
							methodCall,
							qualifier,
							methodName,
							wantsAssertInstanceOf ? "assertInstanceOf" : "assertNotInstanceOf",
							"'instanceof'",
							argIndex - 1,
							child,
							instanceofAst,
							binding,
							notCount[0],
							null
					);
					// the shape decides which rule applies, so an over-long rewrite drops the report
					// rather than falling through to the negation rule and reporting a different one
					instanceofEmitted = true;
					if (fitsLineBudget(lines, target))
						targets.add(target);
				}
			}
			if (!instanceofEmitted && exprType == TokenTypes.LNOT && canSwapAssertMethod(methodCall, scope)) {
				final var target = new AssertionTarget(
						AssertionRule.NEGATION,
						methodCall,
						qualifier,
						methodName,
						"assertTrue".equals(methodName) ? "assertFalse" : "assertTrue",
						"a negated argument",
						argIndex - 1,
						child,
						null,
						null,
						notCount[0],
						null
				);
				if (fitsLineBudget(lines, target))
					targets.add(target);
			}
		}
		return targets;
	}

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
	 * Whether the lines the rewrite composes stay within the line-length budget. The rewrite folds a
	 * multi-line call onto the line its span starts on, and {@code assertInstanceOf(Y.class, x)} is longer
	 * than the {@code assertTrue(x instanceof Y)} it replaces, so the check has to measure what the fixer
	 * would emit rather than what is written.
	 *
	 * <p>Only the first and last emitted lines are measured, because those are the only two the rewrite
	 * composes: a line between them is an argument's own content (a text block's body) carried over
	 * unchanged, already on disk at that width.
	 *
	 * <p>The relocated trailing comment is deliberately excluded, because a long comment would otherwise
	 * suppress the violation outright.
	 */
	@CheckReturnValue
	private static boolean fitsLineBudget(@Nonnull List<String> lines, @Nonnull AssertionTarget target) {
		final var span = spanOf(lines, target);
		// a shape with no rewrite has no projected line to measure; the check reports it and the fixer
		// names the refusal
		if (span == null || !(rewriteOf(lines, target) instanceof Rewritten rewritten))
			return true;
		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var emitted = rewritten.call().split("\n", -1);
		final var last = emitted.length - 1;
		// the same lines the fixer composes: text it cannot hoist stays ahead of the call, and a
		// trailing sibling statement leaves the call's line for one of its own. That trailing line is
		// not measured, because moving it to the shared indent can only make it narrower than it was
		final var hoists = hoistsLeadingText(target);
		final var tail = hoists && !edges.suffix().isBlank() ? "" : edges.suffix();
		final var head = (hoists ? edges.indent() : edges.prefix()) + emitted[0] + (last == 0 ? tail : "");
		if (LineLength.tabExpandedLength(head) > LineLength.MAX_LINE_LENGTH)
			return false;
		return last == 0
				|| LineLength.tabExpandedLength(emitted[last] + tail) <= LineLength.MAX_LINE_LENGTH;
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
		final var receiver = AstText.firstRealChild(dot);
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
	 * Whether text sharing the call's line ahead of it may be moved onto its own line. True only when
	 * the assertion is a statement in a statement list, so whatever precedes it there is a sibling
	 * statement or a comment.
	 *
	 * <p>False for an assertion that is a control-flow body ({@code if (flag) assertTrue(...);}) or a
	 * lambda body ({@code assertAll(() -> assertTrue(...))}), where the text ahead of it is a header
	 * that the statement belongs to rather than one that precedes it. Hoisting there emits the body at
	 * the header's own indent, which compiles and asserts the same thing but ships mis-indented source
	 * for the project's own formatting checks to flag.
	 *
	 * <p>False too when the statement list's own {@code &#123;} sits on the call's line
	 * ({@code void t() &#123; assertTrue(...); &#125;}). Being in a statement list is not enough: that
	 * brace precedes the call and is a header of exactly the same kind, so hoisting past it lands the
	 * body one level short.
	 */
	@CheckReturnValue
	public static boolean hoistsLeadingText(@Nonnull AssertionTarget target) {
		final var expr = target.methodCall().getParent();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return false;
		final var slist = expr.getParent();
		return slist != null && slist.getType() == TokenTypes.SLIST
				&& slist.getLineNo() != target.methodCall().getLineNo();
	}

	/**
	 * {@code region} narrowed past everything the rewrite drops ahead of the expression it keeps: the
	 * parentheses grouping the whole argument, then {@code nots} levels of {@code !} with the parentheses
	 * each one wrapped. What is left starts at the kept expression, so a comment written between the
	 * argument's opening delimiter and that expression is still inside the region and travels with its
	 * text instead of being relocated.
	 */
	@CheckReturnValue
	@Nullable
	private static TextSpan innerRegion(
			@Nonnull List<String> lines, @Nonnull DetailAST argExpr, @Nonnull TextSpan region, int nots
	) {
		final var holder = argExpr.getType() == TokenTypes.EXPR ? AstText.firstRealChild(argExpr) : argExpr;
		var narrowed = narrowPastParens(lines, argExpr, region);
		var cur = unwrapParens(holder);
		for (var i = 0; i < nots && narrowed != null && cur != null && cur.getType() == TokenTypes.LNOT; ++i) {
			final var afterNot = AstSpan.tokenEnd(lines, cur);
			narrowed = afterNot == null
					? null
					: narrowPastParens(lines, cur, new TextSpan(afterNot, narrowed.end()));
			cur = unwrapParens(AstText.firstRealChild(cur));
		}
		return narrowed;
	}

	/**
	 * Returns the inner {@code LITERAL_INSTANCEOF} token if {@code expr} is an
	 * {@code instanceof} or a chain of {@code !} operators wrapping one. Writes
	 * the count of {@code !} unwraps encountered into {@code notCount[0]}.
	 * Returns {@code null} if no instanceof is reachable through pure-{@code !}
	 * unwrapping.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST instanceOfThroughNots(@Nonnull DetailAST expr, @Nonnull int[] notCount) {
		var cur = expr;
		while (cur != null && cur.getType() == TokenTypes.LNOT) {
			++notCount[0];
			cur = unwrapParens(AstText.firstRealChild(cur));
		}
		return cur != null && cur.getType() == TokenTypes.LITERAL_INSTANCEOF ? cur : null;
	}

	/**
	 * Whether {@code simpleName} is a recognized JUnit assertion holder class
	 * ({@link #ASSERT_CLASS} or {@link #ASSERTIONS_CLASS}). Only for a receiver written as a bare
	 * name, which carries no package to judge; anything that names its package is matched in full by
	 * {@link #isJunitAssertFqcn}.
	 */
	@CheckReturnValue
	public static boolean isJunitAssertClass(@Nullable String simpleName) {
		return ASSERT_CLASS.equals(simpleName) || ASSERTIONS_CLASS.equals(simpleName);
	}

	/**
	 * Whether {@code fqcn} is a recognized JUnit assertion holder class, named in full
	 * ({@link #ASSERT_FQCN} or {@link #ASSERTIONS_FQCN}).
	 *
	 * <p>The test is equality, not a suffix: a class called {@code Assertions} in some other package
	 * is a different class that need not declare the same methods, so rewriting a call to it, or
	 * drawing the replacement's static import from it, would emit a call to a method that may not
	 * exist.
	 */
	@CheckReturnValue
	public static boolean isJunitAssertFqcn(@Nullable String fqcn) {
		return ASSERT_FQCN.equals(fqcn) || ASSERTIONS_FQCN.equals(fqcn);
	}

	/**
	 * The JUnit assertion holder {@code methodCall}'s receiver names, by simple name, or null when it
	 * names none and for an unqualified call. A receiver written with its package is matched in full,
	 * so {@code com.example.Assertions} is not one; a receiver written as a bare name carries no
	 * package, so its simple name is all there is to go on.
	 */
	@CheckReturnValue
	@Nullable
	private static String junitHolderOf(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var receiver = dot == null ? null : AstText.firstRealChild(dot);
		if (receiver == null)
			return null;
		final var name = FullIdent.createFullIdent(receiver).getText();
		if (name.indexOf('.') < 0)
			return isJunitAssertClass(name) ? name : null;
		return isJunitAssertFqcn(name) ? AstText.simpleName(name) : null;
	}

	/** The JUnit assertion holders a file's static imports bring into scope. */
	@CheckReturnValue
	@Nonnull
	public static JunitScope junitScopeOf(@Nonnull DetailAST root) {
		var junit4 = false;
		var junit5 = false;
		for (var fqn : staticImportFqns(root)) {
			final var classFqn = staticImportClassFqn(fqn);
			junit4 |= ASSERT_FQCN.equals(classFqn);
			junit5 |= ASSERTIONS_FQCN.equals(classFqn);
		}
		return new JunitScope(junit4, junit5);
	}

	/**
	 * The leftmost position {@code subtree} occupies on {@code lineNo} (1-based, as a {@link DetailAST}
	 * reports it), or null when nothing there resolves against {@code lines}. Stops at the line boundary,
	 * unlike {@link AstSpan#spanStart}, which is what keeps a receiver written on an earlier line out of
	 * the rewritten span: {@code org.junit.Assert} above {@code .assertTrue(...)} stays where the author
	 * put it, and the {@code .} that starts the call's own line opens the replacement.
	 */
	@CheckReturnValue
	@Nullable
	private static TextPos leftmostOnLine(@Nonnull List<String> lines, @Nonnull DetailAST subtree, int lineNo) {
		var best = -1;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(subtree);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			// both sides are AST columns, so this comparison stays in code-point space
			if (node.getLineNo() == lineNo && (best < 0 || node.getColumnNo() < best))
				best = node.getColumnNo();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		if (best < 0 || lineNo - 1 < 0 || lineNo - 1 >= lines.size())
			return null;
		final var index = LineText.charIndexOfColumn(lines.get(lineNo - 1), best);
		return index < 0 ? null : new TextPos(lineNo - 1, index);
	}

	/**
	 * Every violation the check would report at {@code (line, column)} (0-based line index, 0-based
	 * code-point column, matching {@code AbstractCheck.log}'s {@code getLineNo()}/{@code getColumnNo()}),
	 * in the order it logs them.
	 *
	 * <p>A list rather than a single target because the check logs once per offending argument and
	 * anchors every one of them on the {@code METHOD_CALL}, so one position can carry more than one
	 * rule. Real JUnit cannot get there: every parameter after the condition is a {@code String} or
	 * {@code Supplier<String>} message, so only one argument is ever a boolean expression. It takes a
	 * user-defined class named {@code Assert} or {@code Assertions}, called by a bare qualifier,
	 * declaring an overload with two boolean parameters, which the bare-name gate accepts by design.
	 * Keeping the anchor on the call is deliberate: moving it onto the argument would restore
	 * one-position-one-rule but shift every column this check reports.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<AssertionTarget> locateAt(
			@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column
	) {
		// computed once rather than inside the predicate, which findNodeAt evaluates per candidate node
		final var scope = junitScopeOf(root);
		final var node = AstQuery.findNodeAt(
				root,
				line,
				column,
				n -> n.getType() == TokenTypes.METHOD_CALL && !classify(n, lines, scope).isEmpty()
		);
		return node == null ? List.of() : classify(node, lines, scope);
	}

	/**
	 * The {@code IDENT} holding the called method's name: the call's first child when the call is
	 * unqualified, the last segment of the receiver chain when it is qualified.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST methodNameIdent(@Nonnull DetailAST methodCall) {
		final var first = AstText.firstRealChild(methodCall);
		if (first == null)
			return null;
		if (first.getType() == TokenTypes.IDENT)
			return first;
		if (first.getType() != TokenTypes.DOT)
			return null;
		final var last = first.getLastChild();
		return last != null && last.getType() == TokenTypes.IDENT ? last : null;
	}

	/**
	 * {@code region} narrowed past the parentheses {@code holder} carries as leading children, and past
	 * the matching closers. Checkstyle hangs grouping parentheses off the expression they group as
	 * siblings of it, so they are tokens to measure rather than text to scan for.
	 */
	@CheckReturnValue
	@Nullable
	private static TextSpan narrowPastParens(
			@Nonnull List<String> lines, @Nonnull DetailAST holder, @Nonnull TextSpan region
	) {
		var start = region.start();
		var child = AstText.firstRealChild(holder);
		var opened = false;
		while (child != null && child.getType() == TokenTypes.LPAREN) {
			final var open = AstSpan.tokenEnd(lines, child);
			if (open == null)
				return null;
			start = open;
			opened = true;
			child = AstText.nextRealSibling(child);
		}
		if (!opened)
			return region;
		for (var after = AstText.nextRealSibling(child); after != null; after = AstText.nextRealSibling(after)) {
			if (after.getType() != TokenTypes.RPAREN)
				continue;
			final var close = AstSpan.tokenStart(lines, after);
			return close == null ? null : new TextSpan(start, close);
		}
		return new TextSpan(start, region.end());
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

	/**
	 * The source text of {@code region} as the rewrite re-emits it: verbatim, so a text block keeps the
	 * line breaks its value depends on and a block comment keeps its place, but with each line's trailing
	 * {@code //} comment cut off. Those cannot survive the re-emit, because the rewrite closes the call
	 * after the argument and a {@code //} would comment the closer out;
	 * {@link SpanComments#relocate} picks every one of them up instead.
	 */
	@CheckReturnValue
	@Nullable
	private static String pieceText(@Nonnull List<String> lines, @Nonnull TextSpan region) {
		final var raw = AstSpan.sliceVerbatim(lines, region.start(), region.end());
		if (raw == null)
			return null;
		final var parts = raw.split("\n", -1);
		final var text = new StringBuilder();
		var state = LexerState.NONE;
		for (var i = 0; i < parts.length; ++i) {
			if (i > 0)
				text.append('\n');
			final var comment = JavaLineScanner.firstLineComment(parts[i], state);
			text.append(comment < 0 ? parts[i] : parts[i].substring(0, comment));
			state = JavaLineScanner.stateAfter(parts[i], state);
		}
		return text.toString().strip();
	}

	/**
	 * Why {@code target}'s own shape refuses a rewrite, or null when nothing in it does. Decided from the
	 * tree alone, so the answer does not depend on the buffer: a span that will not slice is
	 * {@link AssertionRefusal#UNSLICEABLE}, which {@link #rewriteOf} raises instead.
	 */
	@CheckReturnValue
	@Nullable
	private static AssertionRefusal refusalFor(@Nonnull AssertionTarget target) {
		if (target.rule() == AssertionRule.COMPARISON)
			return AssertionRefusal.COMPARISON;
		final var elist = target.methodCall().findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) > 2)
			return AssertionRefusal.ARITY;
		final var instanceofAst = target.instanceofAst();
		if (instanceofAst == null)
			return null;
		final var pattern = AstText.nextRealSibling(AstText.firstRealChild(instanceofAst));
		if (pattern != null && pattern.getType() == TokenTypes.RECORD_PATTERN_DEF)
			return AssertionRefusal.RECORD_PATTERN;
		final var type = typeNodeOf(instanceofAst);
		if (type == null)
			return AssertionRefusal.UNSLICEABLE;
		if (AstQuery.containsType(type, TokenTypes.TYPE_ARGUMENTS))
			return AssertionRefusal.GENERIC_TYPE;
		return AstQuery.containsType(type, TokenTypes.ANNOTATION) ? AssertionRefusal.ANNOTATED_TYPE : null;
	}

	/**
	 * What the fixer does with {@code target}: the call text that replaces the span {@link #spanOf}
	 * describes, or the named reason there is none. Every argument is sliced from {@code lines} rather
	 * than rebuilt from its tokens, so a fully-qualified type keeps its spelling, a text block keeps its
	 * line breaks and a comment beside an argument stays beside it. Only the structure the rewrite drops
	 * (the {@code instanceof} keyword, an {@code !} chain, grouping parentheses, the call's old name) can
	 * strand a comment, and those are what {@link Rewritten#comment} carries out.
	 */
	@CheckReturnValue
	@Nonnull
	public static Rewrite rewriteOf(@Nonnull List<String> lines, @Nonnull AssertionTarget target) {
		final var refusal = refusalFor(target);
		if (refusal != null)
			return new CannotRewrite(refusal);

		final var methodCall = target.methodCall();
		final var nameIdent = methodNameIdent(methodCall);
		final var regions = argRegions(lines, methodCall);
		final var span = spanOf(lines, target);
		if (nameIdent == null || regions == null || span == null || target.argIndex() >= regions.size())
			return new CannotRewrite(AssertionRefusal.UNSLICEABLE);
		final var nameStart = AstSpan.tokenStart(lines, nameIdent);
		// the receiver as written, up to the name being replaced; empty for an unqualified call, and for
		// one whose receiver and `.` sit on an earlier line, which the span deliberately leaves alone
		final var receiver = AstSpan.sliceVerbatim(lines, span.start(), nameStart);
		final var rewritten = target.rule() == AssertionRule.INSTANCEOF
				? argPiecesForInstanceof(lines, target, regions.get(target.argIndex()))
				: argPiecesForNegation(lines, target, regions.get(target.argIndex()));
		if (nameStart == null || receiver == null || rewritten == null)
			return new CannotRewrite(AssertionRefusal.UNSLICEABLE);

		final var kept = new ArrayList<>(rewritten.kept());
		final var pieces = new ArrayList<String>();
		for (var i = 0; i < regions.size(); ++i) {
			if (i == target.argIndex())
				continue;
			final var text = pieceText(lines, regions.get(i));
			if (text == null || text.isEmpty())
				return new CannotRewrite(AssertionRefusal.UNSLICEABLE);
			kept.add(regions.get(i));
			pieces.add(text);
		}
		// an `instanceof` rewrite re-orders: the type and operand lead, any message follows. A negation
		// rewrite edits one argument in place, so it keeps the position it was written in
		pieces.addAll(
				target.rule() == AssertionRule.INSTANCEOF ? 0 : Math.min(target.argIndex(), pieces.size()),
				rewritten.texts()
		);

		final var semi = semiOf(methodCall);
		return new Rewritten(
				receiver.stripTrailing() + target.replacementMethod()
						+ '(' + String.join(", ", pieces) + ')' + (semi == null ? "" : ";"),
				SpanComments.relocate(lines, span, kept)
		);
	}

	/**
	 * The {@code ;} that ends the statement {@code methodCall} is, or null when the call is not a
	 * statement of its own. A {@code void} assertion can still be an expression lambda's body
	 * ({@code () -> assertTrue(x instanceof Y)}), which has no semicolon to replace.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST semiOf(@Nonnull DetailAST methodCall) {
		final var expr = methodCall.getParent();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return null;
		final var semi = AstText.nextRealSibling(expr);
		return semi != null && semi.getType() == TokenTypes.SEMI ? semi : null;
	}

	@CheckReturnValue
	private static boolean shouldFireInstanceofViolation(@Nonnull DetailAST methodCall, @Nonnull JunitScope scope) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var holder = junitHolderOf(methodCall);
		if (ASSERTIONS_CLASS.equals(holder))
			return true;
		if (ASSERT_CLASS.equals(holder))
			return false;
		return dot == null && scope.junit5Static() && !scope.junit4Static();
	}

	/**
	 * The source span the rewrite replaces: from the start of the call as written on the line its name
	 * sits on, to just past the {@code ;} that ends it (or past the closing {@code )} when the call is an
	 * expression lambda's body and has no {@code ;}).
	 */
	@CheckReturnValue
	@Nullable
	public static TextSpan spanOf(@Nonnull List<String> lines, @Nonnull AssertionTarget target) {
		final var methodCall = target.methodCall();
		final var nameIdent = methodNameIdent(methodCall);
		if (nameIdent == null)
			return null;
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var start = dot == null
				? AstSpan.tokenStart(lines, nameIdent)
				: leftmostOnLine(lines, dot, nameIdent.getLineNo());
		final var semi = semiOf(methodCall);
		final var end = AstSpan.spanEnd(lines, semi == null ? methodCall : semi);
		if (start == null || end == null)
			return null;
		return AstSpan.extendedOverTrailingComment(lines, start, end);
	}

	/**
	 * The class FQN a static import names, or null when it names no class. A single-member import and
	 * a wildcard strip to the same thing: {@code org.junit.Assert} for either
	 * {@code org.junit.Assert.assertTrue} or {@code org.junit.Assert.*}.
	 */
	@CheckReturnValue
	@Nullable
	public static String staticImportClassFqn(@Nonnull String fqn) {
		if (fqn.endsWith(".*"))
			return fqn.substring(0, fqn.length() - 2);
		final var lastDot = fqn.lastIndexOf('.');
		return lastDot < 0 ? null : fqn.substring(0, lastDot);
	}

	/**
	 * The fully qualified name each {@code static} import brings into scope, in source order, a
	 * wildcard keeping its trailing {@code .*}.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> staticImportFqns(@Nonnull DetailAST root) {
		var top = root;
		while (top.getParent() != null)
			top = top.getParent();
		while (top.getPreviousSibling() != null)
			top = top.getPreviousSibling();
		// imports are children of the COMPILATION_UNIT, not siblings of it
		final var first = top.getType() == TokenTypes.COMPILATION_UNIT ? AstText.firstRealChild(top) : top;
		final var fqns = new ArrayList<String>();
		for (var node = first; node != null; node = AstText.nextRealSibling(node)) {
			if (node.getType() != TokenTypes.STATIC_IMPORT)
				continue;
			// STATIC_IMPORT children: LITERAL_STATIC, then the DOT/IDENT path, then SEMI.
			final var pathNode = AstText.nextRealSibling(AstText.firstRealChild(node));
			if (pathNode != null)
				fqns.add(FullIdent.createFullIdent(pathNode).getText());
		}
		return fqns;
	}

	/**
	 * The member name a single-member static import brings into scope, or null for a wildcard and for
	 * an import naming no class.
	 */
	@CheckReturnValue
	@Nullable
	public static String staticImportMember(@Nonnull String fqn) {
		if (fqn.endsWith(".*"))
			return null;
		final var lastDot = fqn.lastIndexOf('.');
		return lastDot < 0 ? null : fqn.substring(lastDot + 1);
	}

	/**
	 * The {@code TYPE} the {@code instanceof} tests against, reached through a pattern's declaration when
	 * the source writes one ({@code o instanceof Y y} declares {@code Y} under a
	 * {@code PATTERN_VARIABLE_DEF}). Null when the operand carries no type at all.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST typeNodeOf(@Nonnull DetailAST instanceofAst) {
		final var operand = AstText.nextRealSibling(AstText.firstRealChild(instanceofAst));
		if (operand == null)
			return null;
		return operand.getType() == TokenTypes.TYPE ? operand : operand.findFirstToken(TokenTypes.TYPE);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapParens(@Nullable DetailAST node) {
		var cur = node;
		while (cur != null && cur.getType() == TokenTypes.LPAREN)
			cur = AstText.nextRealSibling(cur);
		while (cur != null && cur.getType() == TokenTypes.EXPR)
			cur = AstText.firstRealChild(cur);
		return cur;
	}

	@Nonnull
	private List<String> sourceLines = List.of();
	@Nonnull
	private JunitScope scope = new JunitScope(false, false);

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		// a comments-only or empty file has no compilation unit, so checkstyle passes a null root
		scope = new JunitScope(false, false);
		sourceLines = List.of(getLines());
		if (rootAST != null)
			scope = junitScopeOf(rootAST);
	}

	@Override
	public void finishTree(@Nullable DetailAST rootAST) {
		// a TreeWalker instance outlives the file, so holding the buffer here would pin every line of
		// the last file checked for as long as the check lives
		sourceLines = List.of();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.METHOD_CALL};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		for (var target : classify(ast, sourceLines, scope))
			log(ast, MSG_KEY, target.replacementMethod(), target.callerMethod(), target.operandDescription());
	}
}