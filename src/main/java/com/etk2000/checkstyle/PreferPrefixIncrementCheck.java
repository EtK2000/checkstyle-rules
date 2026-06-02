package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags postfix increment/decrement,
 * preferring prefix form (++i, --i). Only flags when the
 * return value is unused, since changing {@code return i++} to
 * {@code return ++i} would alter behavior.
 */
public class PreferPrefixIncrementCheck extends AbstractAstCheck {
	/**
	 * The span of a postfix increment/decrement: where its operand begins, where
	 * the operator sits, and which operator it is. Lines and columns are
	 * zero-based.
	 */
	public record PostfixSpan(int operandLine, int operandColumn, int operatorLine, int operatorColumn, boolean increment) {}

	private static final String MSG_DEC = "prefer.prefix.decrement";
	private static final String MSG_INC = "prefer.prefix.increment";

	@CheckReturnValue
	private static boolean isPostfix(@Nonnull DetailAST node) {
		return node.getType() == TokenTypes.POST_INC || node.getType() == TokenTypes.POST_DEC;
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
		// braceless control flow body: EXPR follows RPAREN in if/while/for
		final var prevSibling = parent.getPreviousSibling();
		if (prevSibling != null && prevSibling.getType() == TokenTypes.RPAREN) {
			return switch (grandparent.getType()) {
				case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE -> true;
				default -> false;
			};
		}
		// braceless do-while body: EXPR is first child of LITERAL_DO (before RPAREN)
		if (grandparent.getType() == TokenTypes.LITERAL_DO && grandparent.getFirstChild() == parent)
			return true;
		// A SWITCH_RULE body is deliberately absent: `case 1 -> i++;` discards the
		// value in a switch statement but IS the value in a switch expression
		// (`var x = switch (k) { case 1 -> i++; };`), where rewriting to `++i`
		// changes what the switch yields. The colon form has no such ambiguity and
		// is covered by the SLIST branch above. A LABELED_STAT body is absent for a
		// different reason: it is unambiguous but reaches EXPR through the label,
		// which none of these branches match.

		// braceless else body: EXPR -> LITERAL_ELSE (no RPAREN)
		return grandparent.getType() == TokenTypes.LITERAL_ELSE;
	}

	/**
	 * The postfix increment/decrement reported at {@code line}/{@code column}
	 * (both zero-based), or {@code null} when there is none.
	 *
	 * <p>Only nodes this check would actually flag are considered, so a caller
	 * cannot be handed the inner {@code j++} of {@code arr[j++]++}, whose value is
	 * consumed and which must not be rewritten.
	 *
	 * <p>A reported column is accepted at either end of the span, because the
	 * position carried by the logged node is the operator's while a caller working
	 * from line text may instead have the operand's.
	 */
	@CheckReturnValue
	@Nullable
	public static PostfixSpan postfixSpanAt(@Nonnull DetailAST root, int line, int column) {
		var top = root;
		while (top.getParent() != null)
			top = top.getParent();
		PostfixSpan byOperand = null;
		for (var node : AstUtil.collectMatching(top, PreferPrefixIncrementCheck::isPostfix)) {
			final var operand = node.getFirstChild();
			if (operand == null || node.getLineNo() != line + 1 || !isValueDiscarded(node))
				continue;
			final var operandLine = AstUtil.firstLine(operand) - 1;
			final var operandColumn = AstUtil.firstColumn(operand);
			final var increment = node.getType() == TokenTypes.POST_INC;
			if (node.getColumnNo() == column)
				return new PostfixSpan(operandLine, operandColumn, line, node.getColumnNo(), increment);
			// first match wins, though no valid Java reaches the tie: two postfix nodes
			// share an operand start column only when one nests in the other, and the
			// inner one's value is consumed, so isValueDiscarded already dropped it
			if (operandLine == line && operandColumn == column && byOperand == null)
				byOperand = new PostfixSpan(operandLine, operandColumn, line, node.getColumnNo(), increment);
		}
		return byOperand;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.POST_INC, TokenTypes.POST_DEC};
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