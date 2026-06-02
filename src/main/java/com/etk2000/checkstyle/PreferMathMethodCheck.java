package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

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
public class PreferMathMethodCheck extends AbstractAstCheck {
	private record BranchInfo(@Nonnull BranchKind kind, @Nullable DetailAST target, @Nonnull DetailAST value, int assignType) {}

	private enum BranchKind {
		ASSIGN,
		RETURN
	}

	private static final int MIN_SDK_CLAMP = 35;
	private static final String MSG_METHOD = "prefer.replacement";
	private static final String MSG_METHOD_IF = "prefer.math.method.if";

	/**
	 * Returns the display-text of a node, stripping a top-level prefix
	 * increment/decrement so the post-mutation operand text matches the
	 * post-mutation form used in branches.
	 *
	 * <p>Used for structural identity checks instead of
	 * {@link AstUtil#exprText} to avoid leaf-concatenation collisions
	 * (e.g. {@code DOT{a,x}} would otherwise compare equal to
	 * {@code IDENT[ax]}).
	 */
	@CheckReturnValue
	@Nonnull
	private static String branchText(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.INC || ast.getType() == TokenTypes.DEC) {
			final var child = ast.getFirstChild();
			return child != null ? AstUtil.displayText(child) : ast.getText();
		}
		return AstUtil.displayText(ast);
	}

	@CheckReturnValue
	@Nullable
	private static String checkAbs(@Nonnull DetailAST condition, @Nonnull DetailAST trueBranch, @Nonnull DetailAST falseBranch) {
		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return null;

		final var op = condition.getType();

		final var leftIsZero = AstUtil.isZeroLiteral(condLeft);
		final var rightIsZero = AstUtil.isZeroLiteral(condRight);
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

		return "Math.abs(" + AstUtil.displayText(variable) + ")";
	}

	/**
	 * Checks if the given METHOD_CALL is {@code Math.max(a, Math.min(b, c))}
	 * or {@code Math.min(a, Math.max(b, c))} (clamp pattern).
	 * Returns [replacement, original] or null.
	 */
	@CheckReturnValue
	@Nullable
	private static String[] checkClamp(@Nonnull DetailAST methodCall) {
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

			final var outerArgText = AstUtil.displayText(args[1 - i]);
			final var innerArg1Text = AstUtil.displayText(innerArgs[0]);
			final var innerArg2Text = AstUtil.displayText(innerArgs[1]);

			final var original = "Math." + outerName + "("
					+ (i == 0 ? "Math." + expectedInner + "(" + innerArg1Text + ", " + innerArg2Text + "), " + outerArgText
					: outerArgText + ", Math." + expectedInner + "(" + innerArg1Text + ", " + innerArg2Text + ")")
					+ ")";

			final String replacement;
			if (isOuterMax)
				replacement = "Math.clamp(" + innerArg2Text + ", " + outerArgText + ", " + innerArg1Text + ")";
			else
				replacement = "Math.clamp(" + innerArg2Text + ", " + innerArg1Text + ", " + outerArgText + ")";
			return new String[]{replacement, original};
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String checkMaxMin(@Nonnull DetailAST condition, @Nonnull DetailAST trueBranch, @Nonnull DetailAST falseBranch) {
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

		final var method = isMax ? "Math.max" : "Math.min";
		return method + "(" + AstUtil.displayText(condLeft) + ", " + AstUtil.displayText(condRight) + ")";
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
		final var stmt = AstUtil.unwrapSingleStatementBlock(body);
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
	 * sibling and, if it is a {@code VARIABLE_DEF} declaring {@code varName}
	 * with an initializer, returns the initializer value AST.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findVariableDefInit(@Nonnull DetailAST ast, @Nonnull String varName) {
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
		if (ident == null || !varName.equals(ident.getText()))
			return null;
		final var assign = prev.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return null;
		final var initChild = assign.getFirstChild();
		if (initChild == null)
			return null;
		// VARIABLE_DEF wraps the init in EXPR for some shapes but not others
		return initChild.getType() == TokenTypes.EXPR ? initChild.getFirstChild() : initChild;
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
	 * Like {@link AstUtil#isPureExpression} but also allows prefix
	 * increment/decrement ({@code ++x}, {@code --x}) at the top level.
	 * The mutation happens before the ternary branches evaluate, so the
	 * branches see the post-mutation value, making the transformation safe.
	 * Post-increment/decrement is NOT allowed because the branch would
	 * see the new value but the condition used the old value.
	 */
	@CheckReturnValue
	private static boolean isPureOrPrefixMutated(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.DEC || ast.getType() == TokenTypes.INC) {
			final var child = ast.getFirstChild();
			return child != null && AstUtil.isPureExpression(child);
		}
		return AstUtil.isPureExpression(ast);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST unwrapExpr(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.EXPR && ast.getChildCount() == 1)
			return ast.getFirstChild();
		return ast;
	}

	private int minSdk = Integer.MAX_VALUE;

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF, TokenTypes.METHOD_CALL, TokenTypes.QUESTION};
	}

	/**
	 * Sets the minimum SDK version for the target platform.
	 * {@code Math.clamp} requires Android API 35+.
	 * <p>Called by Checkstyle via reflection when {@code minSdk} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}

	private void visitIf(@Nonnull DetailAST ast) {
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return;
		final var condExpr = lparen.getNextSibling();
		if (condExpr == null || condExpr.getType() != TokenTypes.EXPR)
			return;
		final var condition = condExpr.getFirstChild();
		if (condition == null)
			return;

		final var op = condition.getType();
		if (op != TokenTypes.GT && op != TokenTypes.GE && op != TokenTypes.LT && op != TokenTypes.LE)
			return;

		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return;
		if (!isPureOrPrefixMutated(condLeft) || !isPureOrPrefixMutated(condRight))
			return;

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return;

		final var thenBranch = extractBranch(thenBody);
		if (thenBranch == null)
			return;

		final BranchInfo elseBranch;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return;
			elseBranch = extractBranch(elseBody);
			if (elseBranch == null)
				return;
		}
		else if (thenBranch.kind() == BranchKind.RETURN) {
			final var nextStmt = ast.getNextSibling();
			if (nextStmt == null || nextStmt.getType() != TokenTypes.LITERAL_RETURN)
				return;
			final var nextExpr = nextStmt.findFirstToken(TokenTypes.EXPR);
			if (nextExpr == null)
				return;
			final var nextValue = nextExpr.getFirstChild();
			if (nextValue == null)
				return;
			elseBranch = new BranchInfo(BranchKind.RETURN, null, nextValue, 0);
		}
		else {
			if (thenBranch.assignType() != TokenTypes.ASSIGN)
				return;
			if (thenBranch.target().getType() != TokenTypes.IDENT)
				return;
			final var initValue = findVariableDefInit(ast, thenBranch.target().getText());
			if (initValue == null)
				return;
			elseBranch = new BranchInfo(BranchKind.ASSIGN, thenBranch.target(), initValue, TokenTypes.ASSIGN);
		}

		if (thenBranch.kind() != elseBranch.kind())
			return;

		if (thenBranch.kind() == BranchKind.ASSIGN
				&& (thenBranch.assignType() != elseBranch.assignType()
						|| !branchText(thenBranch.target()).equals(branchText(elseBranch.target()))))
			return;

		if (!AstUtil.isPureExpression(thenBranch.value()) || !AstUtil.isPureExpression(elseBranch.value()))
			return;

		var replacement = checkAbs(condition, thenBranch.value(), elseBranch.value());
		if (replacement == null)
			replacement = checkMaxMin(condition, thenBranch.value(), elseBranch.value());
		if (replacement == null)
			return;

		log(ast, MSG_METHOD_IF, replacement);
	}

	private void visitMethodCall(@Nonnull DetailAST ast) {
		if (minSdk < MIN_SDK_CLAMP)
			return;

		final var result = checkClamp(ast);
		if (result != null)
			log(ast, MSG_METHOD, result[0], result[1]);
	}

	private void visitTernary(@Nonnull DetailAST ast) {
		final var condition = ast.getFirstChild();
		if (condition == null)
			return;

		final var op = condition.getType();
		if (op != TokenTypes.GT && op != TokenTypes.GE && op != TokenTypes.LT && op != TokenTypes.LE)
			return;

		final var trueBranch = condition.getNextSibling();
		if (trueBranch == null)
			return;
		final var colon = trueBranch.getNextSibling();
		if (colon == null || colon.getType() != TokenTypes.COLON)
			return;
		final var falseBranch = colon.getNextSibling();
		if (falseBranch == null)
			return;

		final var condLeft = condition.getFirstChild();
		final var condRight = condLeft != null ? condLeft.getNextSibling() : null;
		if (condLeft == null || condRight == null)
			return;
		// condition operands allow prefix ++/-- (the mutation happens before branches evaluate)
		// but branches must be strictly pure
		if (!isPureOrPrefixMutated(condLeft) || !isPureOrPrefixMutated(condRight)
				|| !AstUtil.isPureExpression(trueBranch) || !AstUtil.isPureExpression(falseBranch))
			return;

		var replacement = checkAbs(condition, trueBranch, falseBranch);
		if (replacement == null)
			replacement = checkMaxMin(condition, trueBranch, falseBranch);
		if (replacement == null)
			return;

		final var original = AstUtil.displayText(condition)
				+ " ? " + AstUtil.displayText(trueBranch) + " : " + AstUtil.displayText(falseBranch);
		log(ast, MSG_METHOD, replacement, original);
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