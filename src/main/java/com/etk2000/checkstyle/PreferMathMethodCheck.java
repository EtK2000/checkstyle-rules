package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstDisplay;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags ternary expressions, if-else assignment/return
 * chains, and nested Math calls that can be replaced with {@code Math}
 * utility methods:
 * <ul>
 *     <li>{@code a > b ? a : b} (and variants) -> {@code Math.max(a, b)}</li>
 *     <li>{@code a < b ? a : b} (and variants) -> {@code Math.min(a, b)}</li>
 *     <li>{@code a < 0 ? -a : a} (and variants) -> {@code Math.abs(a)}</li>
 *     <li>if-else with both branches assigning the same target, or both
 *     returning, or {@code if-return} followed by a trailing {@code return}</li>
 *     <li>{@code Math.max(lo, Math.min(hi, v))} -> {@code Math.clamp(v, lo, hi)} (API 35+)</li>
 * </ul>
 * Only flags when all operands are pure (no side effects).
 * Skips method calls, constructors, increment/decrement, and assignments.
 */
public class PreferMathMethodCheck extends AbstractMinSdkCheck {
	private record BranchInfo(@Nonnull BranchKind kind, @Nullable DetailAST target, @Nonnull DetailAST value, int assignType) {}

	private enum BranchKind {
		ASSIGN,
		RETURN
	}

	/**
	 * The if-else statement found at a located position: the {@code Math} call replacing it, and the
	 * nodes a rewrite has to consume around it.
	 *
	 * <p>{@code math.node()} is the {@code LITERAL_IF}. {@code assign} is the then-branch's
	 * assignment, null for the return form. {@code declaration} is the variable declaration above the
	 * {@code if} whose initializer supplied the else value, and {@code trailingReturn} the
	 * {@code return} below it that did; at most one of the two is ever set, since each stands in for
	 * an {@code else} the statement does not have.
	 */
	public record IfTarget(
			@Nonnull MathTarget math,
			@Nullable DetailAST assign,
			@Nullable DetailAST declaration,
			@Nullable DetailAST trailingReturn
	) {}

	/**
	 * The {@code Math} call that replaces the construct found at a located position: the method's
	 * bare name and the nodes whose text becomes its arguments, in the order they are written.
	 *
	 * <p>Carries nodes rather than rendered text so a fixer can re-emit each argument from the
	 * source it was parsed from. {@link AstDisplay#displayText} normalises spacing and drops a
	 * comment written inside an argument, which is what a violation message wants and the opposite
	 * of what a replacement written back to the file wants.
	 *
	 * <p>{@code zeroLiteral} is the compared-against zero of an {@code abs} rewrite, null for every
	 * other method. A fixer needs it because {@code Math.abs} and the ternary it replaces disagree
	 * on {@code -0.0}; see {@code docs/todo/float-nan-math-methods.md}.
	 */
	public record MathTarget(
			@Nonnull DetailAST node,
			@Nonnull String method,
			@Nonnull List<DetailAST> arguments,
			@Nullable DetailAST zeroLiteral
	) {}

	private static final int MIN_SDK_CLAMP = 35;
	private static final String MSG_METHOD = "prefer.replacement";
	/**
	 * Message key for the if-else form. Public so {@code PreferMathMethodFixer}
	 * can tell that shape apart from the expression forms.
	 */
	public static final String MSG_METHOD_IF = "prefer.math.method.if";

	/**
	 * Returns the display-text of a node, stripping a top-level prefix
	 * increment/decrement so the post-mutation operand text matches the
	 * post-mutation form used in branches.
	 *
	 * <p>Used for structural identity checks instead of
	 * {@link AstText#exprText} to avoid leaf-concatenation collisions
	 * (e.g. {@code DOT{a,x}} would otherwise compare equal to
	 * {@code IDENT[ax]}).
	 */
	@CheckReturnValue
	@Nonnull
	private static String branchText(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.INC || ast.getType() == TokenTypes.DEC) {
			final var child = ast.getFirstChild();
			return child != null ? AstDisplay.displayText(child) : ast.getText();
		}
		return AstDisplay.displayText(ast);
	}

	@CheckReturnValue
	@Nullable
	private static MathTarget checkAbs(@Nonnull DetailAST node, @Nonnull DetailAST condition, @Nonnull DetailAST trueBranch, @Nonnull DetailAST falseBranch) {
		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return null;

		final var op = condition.getType();

		final var leftIsZero = AstQuery.isZeroLiteral(condLeft);
		final var rightIsZero = AstQuery.isZeroLiteral(condRight);
		if (!leftIsZero && !rightIsZero)
			return null;
		if (leftIsZero && rightIsZero)
			return null;

		final var variable = leftIsZero ? condRight : condLeft;
		final var varText = branchText(variable);

		final boolean vPositiveInTrueBranch;
		if (leftIsZero) {
			// 0 op V: GT/GE means V <= 0 in true branch, so false is positive
			// LT/LE means V >= 0 in true branch, so true is positive
			vPositiveInTrueBranch = op == TokenTypes.LT || op == TokenTypes.LE;
		}
		else {
			// V op 0: GT/GE means V >= 0 in true branch, so true is positive
			// LT/LE means V <= 0 in true branch, so false is positive
			vPositiveInTrueBranch = op == TokenTypes.GT || op == TokenTypes.GE;
		}

		final var positiveBranch = vPositiveInTrueBranch ? trueBranch : falseBranch;
		final var negativeBranch = vPositiveInTrueBranch ? falseBranch : trueBranch;

		if (!varText.equals(branchText(positiveBranch)))
			return null;
		if (!isNegationOf(negativeBranch, varText))
			return null;

		return new MathTarget(node, "abs", List.of(variable), leftIsZero ? condLeft : condRight);
	}

	/**
	 * Checks if the given METHOD_CALL is {@code Math.max(a, Math.min(b, c))}
	 * or {@code Math.min(a, Math.max(b, c))} (clamp pattern).
	 */
	@CheckReturnValue
	@Nullable
	private static MathTarget checkClamp(@Nonnull DetailAST methodCall) {
		final var outerName = getMathMethodName(methodCall);
		if (outerName == null)
			return null;

		final var isOuterMax = "max".equals(outerName);
		final var isOuterMin = "min".equals(outerName);
		if (!isOuterMax && !isOuterMin)
			return null;

		final var expectedInner = isOuterMax ? "min" : "max";

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;

		final var args = collectArgs(elist);
		if (args.length != 2)
			return null;

		for (var i = 0; i < 2; ++i) {
			final var arg = unwrapExpr(args[i]);
			if (arg.getType() != TokenTypes.METHOD_CALL)
				continue;

			final var innerName = getMathMethodName(arg);
			if (!expectedInner.equals(innerName))
				continue;

			final var innerElist = arg.findFirstToken(TokenTypes.ELIST);
			if (innerElist == null)
				continue;

			final var innerArgs = collectArgs(innerElist);
			if (innerArgs.length != 2)
				continue;

			final var outerArg = args[1 - i];
			final var clampArgs = isOuterMax
					? List.of(innerArgs[1], outerArg, innerArgs[0])
					: List.of(innerArgs[1], innerArgs[0], outerArg);
			return new MathTarget(methodCall, "clamp", clampArgs, null);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static MathTarget checkMaxMin(@Nonnull DetailAST node, @Nonnull DetailAST condition, @Nonnull DetailAST trueBranch, @Nonnull DetailAST falseBranch) {
		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return null;

		final var trueText = branchText(trueBranch);
		final var falseText = branchText(falseBranch);
		final var leftText = branchText(condLeft);
		final var rightText = branchText(condRight);

		final var op = condition.getType();

		final var trueIsLeft = trueText.equals(leftText) && falseText.equals(rightText);
		final var trueIsRight = trueText.equals(rightText) && falseText.equals(leftText);

		if (!trueIsLeft && !trueIsRight)
			return null;

		final boolean isMax;
		if (op == TokenTypes.GT || op == TokenTypes.GE)
			isMax = trueIsLeft;
		else
			isMax = trueIsRight;

		return new MathTarget(node, isMax ? "max" : "min", List.of(condLeft, condRight), null);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST[] collectArgs(@Nonnull DetailAST elist) {
		var count = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				++count;
		}
		final var args = new DetailAST[count];
		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				args[i++] = child;
		}
		return args;
	}

	/**
	 * Extracts the assignment lvalue/rvalue or return value from a single
	 * if/else branch body. Accepts:
	 * <ul>
	 *     <li>bare statement: {@code lhs = rhs;} or {@code return value;}</li>
	 *     <li>SLIST containing exactly one such statement</li>
	 * </ul>
	 * Returns {@code null} for any other shape (multiple statements, throw,
	 * block-without-statement, nested if, etc.).
	 */
	@CheckReturnValue
	@Nullable
	private static BranchInfo extractBranch(@Nonnull DetailAST body) {
		final var stmt = AstQuery.unwrapSingleStatementBlock(body);
		if (stmt == null)
			return null;

		if (stmt.getType() == TokenTypes.EXPR) {
			final var inner = stmt.getFirstChild();
			if (inner == null || !isAssignOrCompoundAssign(inner.getType()))
				return null;
			final var lhs = inner.getFirstChild();
			final var rhs = lhs != null ? lhs.getNextSibling() : null;
			if (lhs == null || rhs == null)
				return null;
			return new BranchInfo(BranchKind.ASSIGN, lhs, rhs, inner.getType());
		}

		if (stmt.getType() == TokenTypes.LITERAL_RETURN) {
			final var expr = stmt.findFirstToken(TokenTypes.EXPR);
			if (expr == null)
				return null;
			final var value = expr.getFirstChild();
			if (value == null)
				return null;
			return new BranchInfo(BranchKind.RETURN, null, value, 0);
		}

		return null;
	}

	/**
	 * Walks backward from {@code ast} to find the immediately preceding
	 * sibling and, if it is a {@code VARIABLE_DEF} declaring {@code varName},
	 * returns it.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findVariableDef(@Nonnull DetailAST ast, @Nonnull String varName) {
		final var parent = ast.getParent();
		if (parent == null)
			return null;
		DetailAST prev = null;
		for (var child = parent.getFirstChild(); child != null && child != ast; child = child.getNextSibling()) {
			// SEMI nodes can appear as siblings between statements; skip them
			if (child.getType() != TokenTypes.SEMI)
				prev = child;
		}
		if (prev == null || prev.getType() != TokenTypes.VARIABLE_DEF)
			return null;
		final var ident = prev.findFirstToken(TokenTypes.IDENT);
		return ident != null && varName.equals(ident.getText()) ? prev : null;
	}

	@CheckReturnValue
	@Nullable
	private static String getMathMethodName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return null;

		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT || !"Math".equals(receiver.getText()))
			return null;

		final var method = receiver.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT)
			return null;

		return method.getText();
	}

	/**
	 * The if-else statement at {@code ast} rewritten as a {@code Math} call, or null when its shape,
	 * its operands or its branches rule the rewrite out.
	 *
	 * <p>A statement with no {@code else} is still accepted when something adjacent supplies the
	 * value the missing branch would have: the {@code return} after an {@code if} that returns, or
	 * the initializer of a declaration directly above an {@code if} that assigns. Both are reported
	 * in the result, because a rewrite has to consume whichever one it read.
	 */
	@CheckReturnValue
	@Nullable
	private static IfTarget ifTarget(@Nonnull DetailAST ast) {
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return null;
		final var condExpr = lparen.getNextSibling();
		if (condExpr == null || condExpr.getType() != TokenTypes.EXPR)
			return null;
		final var condition = condExpr.getFirstChild();
		if (condition == null)
			return null;

		final var op = condition.getType();
		if (op != TokenTypes.GT && op != TokenTypes.GE && op != TokenTypes.LT && op != TokenTypes.LE)
			return null;

		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return null;
		if (!isPureOrPrefixMutated(condLeft) || !isPureOrPrefixMutated(condRight))
			return null;

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return null;

		final var thenBranch = extractBranch(thenBody);
		if (thenBranch == null)
			return null;

		DetailAST declaration = null;
		DetailAST trailingReturn = null;
		final BranchInfo elseBranch;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return null;
			elseBranch = extractBranch(elseBody);
			if (elseBranch == null)
				return null;
		}
		else if (thenBranch.kind() == BranchKind.RETURN) {
			final var nextStmt = ast.getNextSibling();
			if (nextStmt == null || nextStmt.getType() != TokenTypes.LITERAL_RETURN)
				return null;
			final var nextExpr = nextStmt.findFirstToken(TokenTypes.EXPR);
			if (nextExpr == null)
				return null;
			final var nextValue = nextExpr.getFirstChild();
			if (nextValue == null)
				return null;
			trailingReturn = nextStmt;
			elseBranch = new BranchInfo(BranchKind.RETURN, null, nextValue, 0);
		}
		else {
			if (thenBranch.assignType() != TokenTypes.ASSIGN)
				return null;
			if (thenBranch.target().getType() != TokenTypes.IDENT)
				return null;
			final var def = findVariableDef(ast, thenBranch.target().getText());
			final var initValue = def == null ? null : initValueOf(def);
			if (initValue == null)
				return null;
			declaration = def;
			elseBranch = new BranchInfo(BranchKind.ASSIGN, thenBranch.target(), initValue, TokenTypes.ASSIGN);
		}

		if (thenBranch.kind() != elseBranch.kind())
			return null;

		if (thenBranch.kind() == BranchKind.ASSIGN
				&& (thenBranch.assignType() != elseBranch.assignType()
						|| !branchText(thenBranch.target()).equals(branchText(elseBranch.target()))))
			return null;

		if (!AstQuery.isPureExpression(thenBranch.value()) || !AstQuery.isPureExpression(elseBranch.value()))
			return null;

		var math = checkAbs(ast, condition, thenBranch.value(), elseBranch.value());
		if (math == null)
			math = checkMaxMin(ast, condition, thenBranch.value(), elseBranch.value());
		if (math == null)
			return null;

		final var assign = thenBranch.kind() == BranchKind.ASSIGN ? thenBranch.target().getParent() : null;
		return new IfTarget(math, assign, declaration, trailingReturn);
	}

	/**
	 * What this check reports at {@code (line, column)} on its if-else form, or null when it reports
	 * no if-else there. The companion to {@link #mathTargetAt}, which answers for the expression
	 * forms; neither answers for the other's constructs.
	 *
	 * <p>{@code line} is 0-based and {@code column} counts code points, per the convention in
	 * {@code docs/adding-a-fixer.md}. The position must be the {@code if} keyword, which is where
	 * {@link #visitIf} logs.
	 */
	@CheckReturnValue
	@Nullable
	public static IfTarget ifTargetAt(@Nonnull DetailAST root, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, ast -> ast.getType() == TokenTypes.LITERAL_IF);
		return node == null ? null : ifTarget(node);
	}

	/** The value {@code def}'s initializer assigns, or null when it has no initializer. */
	@CheckReturnValue
	@Nullable
	private static DetailAST initValueOf(@Nonnull DetailAST def) {
		final var assign = def.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return null;
		final var initChild = assign.getFirstChild();
		if (initChild == null)
			return null;
		// VARIABLE_DEF wraps the init in EXPR for some shapes but not others
		return initChild.getType() == TokenTypes.EXPR ? initChild.getFirstChild() : initChild;
	}

	@CheckReturnValue
	private static boolean isAssignOrCompoundAssign(int type) {
		return type == TokenTypes.ASSIGN || type == TokenTypes.PLUS_ASSIGN
				|| type == TokenTypes.MINUS_ASSIGN || type == TokenTypes.STAR_ASSIGN
				|| type == TokenTypes.DIV_ASSIGN || type == TokenTypes.MOD_ASSIGN
				|| type == TokenTypes.BAND_ASSIGN || type == TokenTypes.BOR_ASSIGN
				|| type == TokenTypes.BXOR_ASSIGN || type == TokenTypes.SL_ASSIGN
				|| type == TokenTypes.SR_ASSIGN || type == TokenTypes.BSR_ASSIGN;
	}

	@CheckReturnValue
	private static boolean isNegationOf(@Nonnull DetailAST node, @Nonnull String varText) {
		if (node.getType() == TokenTypes.UNARY_MINUS) {
			final var child = node.getFirstChild();
			return child != null && varText.equals(branchText(child));
		}
		return false;
	}

	/**
	 * Like {@link AstQuery#isPureExpression} but also allows prefix
	 * increment/decrement ({@code ++x}, {@code --x}) at the top level.
	 * The mutation happens before the branches evaluate, so the
	 * branches see the post-mutation value, making the transformation safe.
	 * Post-increment/decrement is NOT allowed because the branch would
	 * see the new value but the condition used the old value.
	 */
	@CheckReturnValue
	private static boolean isPureOrPrefixMutated(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.DEC || ast.getType() == TokenTypes.INC) {
			final var child = ast.getFirstChild();
			return child != null && AstQuery.isPureExpression(child);
		}
		return AstQuery.isPureExpression(ast);
	}

	/**
	 * What this check reports at {@code (line, column)}, or null when it reports nothing there.
	 * The positional classifier {@code PreferMathMethodFixer} dispatches on, so the fixer accepts
	 * exactly the constructs the check flags.
	 *
	 * <p>{@code line} is 0-based and {@code column} counts code points, per the convention in
	 * {@code docs/adding-a-fixer.md}. The position must be the construct's own start: a ternary's
	 * {@code ?} or a call's name, which is where this check logs. The clamp shape is classified
	 * without consulting {@code minSdk}, because the gate belongs to {@link #visitMethodCall},
	 * which decides whether to report at all, and a fixer only runs on a reported position.
	 *
	 * <p>The if-else shape is absent here because it rewrites statements rather than an expression;
	 * {@link #ifTargetAt} answers for it.
	 */
	@CheckReturnValue
	@Nullable
	public static MathTarget mathTargetAt(@Nonnull DetailAST root, int line, int column) {
		final var node = AstQuery.findNodeAt(
				root,
				line,
				column,
				ast -> ast.getType() == TokenTypes.QUESTION || ast.getType() == TokenTypes.METHOD_CALL
		);
		if (node == null)
			return null;
		return node.getType() == TokenTypes.QUESTION ? ternaryTarget(node) : checkClamp(node);
	}

	/** The call {@code target} describes, with each argument rendered for a violation message. */
	@CheckReturnValue
	@Nonnull
	private static String replacementText(@Nonnull MathTarget target) {
		final var text = new StringBuilder("Math.").append(target.method()).append('(');
		final var arguments = target.arguments();
		for (var i = 0; i < arguments.size(); ++i) {
			if (i > 0)
				text.append(", ");
			text.append(AstDisplay.displayText(arguments.get(i)));
		}
		return text.append(')').toString();
	}

	/**
	 * The ternary as written, for the message's "instead of" half. Reads the children positionally
	 * because {@link #ternaryTarget} has already accepted the shape.
	 */
	@CheckReturnValue
	@Nonnull
	private static String ternaryOriginalText(@Nonnull DetailAST question) {
		final var condition = question.getFirstChild();
		final var trueBranch = condition.getNextSibling();
		final var falseBranch = trueBranch.getNextSibling().getNextSibling();
		return AstDisplay.displayText(condition)
				+ " ? " + AstDisplay.displayText(trueBranch) + " : " + AstDisplay.displayText(falseBranch);
	}

	/**
	 * The {@code Math} call replacing the ternary at {@code question}, or null when its shape or
	 * its operands rule the rewrite out.
	 */
	@CheckReturnValue
	@Nullable
	private static MathTarget ternaryTarget(@Nonnull DetailAST question) {
		final var condition = question.getFirstChild();
		if (condition == null)
			return null;

		final var op = condition.getType();
		if (op != TokenTypes.GT && op != TokenTypes.GE && op != TokenTypes.LT && op != TokenTypes.LE)
			return null;

		final var trueBranch = condition.getNextSibling();
		if (trueBranch == null)
			return null;
		final var colon = trueBranch.getNextSibling();
		if (colon == null || colon.getType() != TokenTypes.COLON)
			return null;
		final var falseBranch = colon.getNextSibling();
		if (falseBranch == null)
			return null;

		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return null;
		if (!isPureOrPrefixMutated(condLeft) || !isPureOrPrefixMutated(condRight)
				|| !AstQuery.isPureExpression(trueBranch) || !AstQuery.isPureExpression(falseBranch))
			return null;

		final var absTarget = checkAbs(question, condition, trueBranch, falseBranch);
		return absTarget != null ? absTarget : checkMaxMin(question, condition, trueBranch, falseBranch);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST unwrapExpr(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.EXPR && ast.getChildCount() == 1)
			return ast.getFirstChild();
		return ast;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF, TokenTypes.METHOD_CALL, TokenTypes.QUESTION};
	}

	private void visitIf(@Nonnull DetailAST ast) {
		final var target = ifTarget(ast);
		if (target != null)
			log(ast, MSG_METHOD_IF, replacementText(target.math()));
	}

	private void visitMethodCall(@Nonnull DetailAST ast) {
		if (!minSdkAtLeast(MIN_SDK_CLAMP))
			return;

		final var target = checkClamp(ast);
		if (target != null)
			log(ast, MSG_METHOD, replacementText(target), AstDisplay.displayText(ast));
	}

	private void visitTernary(@Nonnull DetailAST ast) {
		final var target = ternaryTarget(ast);
		if (target != null)
			log(ast, MSG_METHOD, replacementText(target), ternaryOriginalText(ast));
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.LITERAL_IF -> visitIf(ast);
			case TokenTypes.METHOD_CALL -> visitMethodCall(ast);
			case TokenTypes.QUESTION -> visitTernary(ast);
		}
	}
}