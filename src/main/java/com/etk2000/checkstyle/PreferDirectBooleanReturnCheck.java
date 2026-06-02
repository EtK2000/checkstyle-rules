package com.etk2000.checkstyle;

import com.etk2000.checkstyle.gradle.fix.LineLength;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags an {@code if} whose body and paired {@code return} (the {@code else}
 * branch or the immediately following sibling statement) are each a single
 * {@code return <expr>;}, where the pair collapses to one {@code return} without
 * changing behavior:
 *
 * <ul>
 *     <li><b>Opposite literals</b> {@code if (C) return true; return false;} -> {@code return C;}
 *         (and {@code return false;}/{@code return true;} -> {@code return !C;}).</li>
 *     <li><b>Same literal</b> {@code if (C) return true; return true;} -> {@code return true;}:
 *         {@code C} is dropped when it is side-effect-free (a pointless {@code if}, dropped even if the
 *         condition could throw); when {@code C} has a hoistable side effect that effect is extracted ahead
 *         of the return (e.g. {@code if (compute()) ...} -> {@code compute();} then {@code return true;}).
 *         Otherwise not flagged.</li>
 *     <li><b>One literal (mixed)</b> collapses via short-circuit: {@code if (C) return X; return false;}
 *         -> {@code return C && X;}; {@code return true;} -> {@code return !C || X;};
 *         {@code if (C) return true; return X;} -> {@code return C || X;};
 *         {@code return false;} -> {@code return !C && X;}.</li>
 * </ul>
 *
 * <p>When both returns are non-literal expressions the pair is a ternary
 * ({@code C ? X : Y}) and is left alone. A mixed collapse is also left alone when the non-literal return
 * spans more than one line (a genuine guarded body), when it would mix {@code &&} with {@code ||} or use
 * three or more boolean operators, or when the resulting one-line return would exceed the line-length
 * budget.
 */
public class PreferDirectBooleanReturnCheck extends AbstractAstCheck {
	private static final String MSG_COMBINE = "prefer.direct.boolean.return.combine";
	private static final String MSG_OPPOSITE = "prefer.direct.boolean.return";
	private static final String MSG_REDUNDANT = "prefer.direct.boolean.return.redundant";

	@CheckReturnValue
	private static int countType(@Nonnull DetailAST root, int tokenType) {
		var count = 0;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == tokenType)
				++count;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return count;
	}

	/**
	 * True when a same-literal condition's side effect can be hoisted into a preceding statement so the
	 * {@code if} collapses without dropping the effect. Bounded to three shapes: the whole condition is a
	 * statement-expression ({@code compute()}, {@code flag = other}, {@code ++i}); a comparison wrapping one
	 * statement-expression ({@code ++i > 0} -> {@code ++i;}); or {@code pure && sideEffect()} -> a guarded
	 * {@code if (pure) sideEffect();}. Anything else is left alone. A leading cast is transparent: the
	 * operand it wraps is classified instead ({@code (boolean) box()} -> {@code box()}), so the cast is
	 * dropped and its side effect hoisted (matching {@code same_literal_drop_cast}, which likewise discards a
	 * pure cast's {@code ClassCastException} risk).
	 */
	@CheckReturnValue
	private static boolean extractableSameLiteral(@Nonnull DetailAST conditionExpr) {
		var top = AstUtil.unwrapParensAndExpr(conditionExpr);
		while (top != null && top.getType() == TokenTypes.TYPECAST) {
			final var rparen = top.findFirstToken(TokenTypes.RPAREN);
			top = rparen == null ? null : AstUtil.unwrapParensAndExpr(rparen.getNextSibling());
		}
		if (top == null)
			return false;
		if (isStatementExpr(top))
			return true;
		final var left = AstUtil.unwrapParensAndExpr(top.getFirstChild());
		final var right = AstUtil.unwrapParensAndExprFromEnd(top.getLastChild());
		if (left == null || right == null)
			return false;
		return switch (top.getType()) {
			case TokenTypes.EQUAL, TokenTypes.GE, TokenTypes.GT, TokenTypes.LE, TokenTypes.LT, TokenTypes.NOT_EQUAL ->
					(isStatementExpr(left) && AstUtil.isSideEffectFree(right))
							|| (isStatementExpr(right) && AstUtil.isSideEffectFree(left));
			case TokenTypes.LAND -> isStatementExpr(right)
					|| (isStatementExpr(left) && AstUtil.isSideEffectFree(right));
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isStatementExpr(@Nonnull DetailAST node) {
		final var type = node.getType();
		return AstUtil.isAssignmentOperator(type)
				|| type == TokenTypes.DEC || type == TokenTypes.INC
				|| type == TokenTypes.METHOD_CALL
				|| type == TokenTypes.POST_DEC || type == TokenTypes.POST_INC;
	}

	@CheckReturnValue
	private static boolean isValuedReturn(@Nonnull DetailAST stmt) {
		return stmt.getType() == TokenTypes.LITERAL_RETURN && stmt.findFirstToken(TokenTypes.EXPR) != null;
	}

	/**
	 * The boolean literal value of a {@code return true;}/{@code return false;} (unwrapping redundant
	 * parentheses, so {@code return (true);} counts), or {@code null} when the return value is a
	 * non-literal expression. Assumes {@code stmt} is a valued return (see {@link #isValuedReturn}).
	 */
	@CheckReturnValue
	@Nullable
	private static Boolean returnLiteralValue(@Nonnull DetailAST stmt) {
		final var expr = stmt.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return null;
		final var value = AstUtil.unwrapParensAndExpr(expr);
		if (value == null)
			return null;
		return switch (value.getType()) {
			case TokenTypes.LITERAL_FALSE -> Boolean.FALSE;
			case TokenTypes.LITERAL_TRUE -> Boolean.TRUE;
			default -> null;
		};
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return;

		final var thenStmt = AstUtil.unwrapSingleStatementBlock(thenBody);
		if (thenStmt == null || !isValuedReturn(thenStmt))
			return;

		final DetailAST elseStmt;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return;
			elseStmt = AstUtil.unwrapSingleStatementBlock(elseBody);
		}
		else
			elseStmt = ast.getNextSibling();
		if (elseStmt == null || !isValuedReturn(elseStmt))
			return;

		final var thenValue = returnLiteralValue(thenStmt);
		final var elseValue = returnLiteralValue(elseStmt);

		if (thenValue == null && elseValue == null)
			return;

		if (thenValue == null || elseValue == null) {
			final var conditionExpr = ast.findFirstToken(TokenTypes.EXPR);
			final var xInBody = thenValue == null;
			final var valueExpr = (xInBody ? thenStmt : elseStmt).findFirstToken(TokenTypes.EXPR);
			if (conditionExpr == null || valueExpr == null)
				return;
			if (AstUtil.lastLine(valueExpr) > AstUtil.firstLine(valueExpr))
				return;
			final var literalTrue = xInBody ? elseValue : thenValue;
			final var ands = countType(conditionExpr, TokenTypes.LAND)
					+ countType(valueExpr, TokenTypes.LAND) + (literalTrue ? 0 : 1);
			final var ors = countType(conditionExpr, TokenTypes.LOR)
					+ countType(valueExpr, TokenTypes.LOR) + (literalTrue ? 1 : 0);
			if ((ands > 0 && ors > 0) || ands + ors >= 3)
				return;
			final var negateCondition = xInBody == literalTrue;
			final var ifLine = getLine(ast.getLineNo() - 1);
			final var indentEnd = Math.min(ast.getColumnNo(), ifLine.length());
			final var collapsedLength = LineLength.tabExpandedLength(ifLine.substring(0, indentEnd))
					+ "return ".length() + (negateCondition ? "!".length() : 0)
					+ AstUtil.displayText(conditionExpr).length() + " && ".length()
					+ AstUtil.displayText(valueExpr).length() + ";".length();
			if (collapsedLength > LineLength.MAX_LINE_LENGTH)
				return;
			log(ast, MSG_COMBINE);
			return;
		}

		if (!thenValue.equals(elseValue)) {
			log(ast, MSG_OPPOSITE);
			return;
		}

		final var condition = ast.findFirstToken(TokenTypes.EXPR);
		if (condition != null && (AstUtil.isSideEffectFree(condition) || extractableSameLiteral(condition)))
			log(ast, MSG_REDUNDANT);
	}
}