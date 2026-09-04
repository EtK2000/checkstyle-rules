package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags {@code instanceof} checks where the checked type
 * is subsequently cast to, preferring pattern matching
 * ({@code x instanceof Foo f}) instead.
 */
public class PreferPatternMatchingInstanceofCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "prefer.pattern.instanceof";

	/**
	 * {@code parent}'s first operand. Parentheses around an operand are children of
	 * the operator node, so {@code (o instanceof String) && x} gives {@code LAND} an
	 * {@code LPAREN} first child and the operand only comes after it.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST firstOperand(@Nonnull DetailAST parent) {
		var child = parent.getFirstChild();
		while (child != null && child.getType() == TokenTypes.LPAREN)
			child = child.getNextSibling();
		return child;
	}

	@CheckReturnValue
	private static boolean hasCastInThenBranch(
			@Nonnull DetailAST instanceofAst,
			@Nonnull String typeName,
			@Nonnull String exprStr
	) {
		var parent = instanceofAst.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.LITERAL_IF) {
				final var slist = parent.findFirstToken(TokenTypes.SLIST);
				return slist != null && AstQuery.containsCastTo(slist, typeName, exprStr);
			}
			// &&: right operand only executes when instanceof is true
			if (parent.getType() == TokenTypes.LAND
					&& isInFirstOperand(parent, instanceofAst)) {
				final var rightOperand = nextOperand(firstOperand(parent));
				if (rightOperand != null && AstQuery.containsCastTo(rightOperand, typeName, exprStr))
					return true;
				// continue walking up to check if-body or outer &&
			}
			if (parent.getType() == TokenTypes.QUESTION
					&& isInFirstOperand(parent, instanceofAst)) {
				for (var child = nextOperand(firstOperand(parent));
				     child != null && child.getType() != TokenTypes.COLON;
				     child = child.getNextSibling()) {
					if (AstQuery.containsCastTo(child, typeName, exprStr))
						return true;
				}
				return false;
			}
			parent = parent.getParent();
		}
		return false;
	}

	/** Whether {@code target} sits inside {@code parent}'s first operand. */
	@CheckReturnValue
	private static boolean isInFirstOperand(@Nonnull DetailAST parent, @Nonnull DetailAST target) {
		var node = target;
		while (node != null && node.getParent() != parent)
			node = node.getParent();
		return node != null && node == firstOperand(parent);
	}

	/**
	 * The operand following {@code node} among its siblings, skipping the
	 * parentheses that wrap either operand.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST nextOperand(@Nullable DetailAST node) {
		var sibling = node == null ? null : node.getNextSibling();
		while (sibling != null
				&& (sibling.getType() == TokenTypes.LPAREN || sibling.getType() == TokenTypes.RPAREN))
			sibling = sibling.getNextSibling();
		return sibling;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_INSTANCEOF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF) != null)
			return;

		final var expr = ast.getFirstChild();
		final var type = ast.findFirstToken(TokenTypes.TYPE);
		if (expr == null || type == null)
			return;

		final var typeName = AstText.typeText(type);
		final var exprStr = AstText.exprText(expr);
		if (typeName.isEmpty())
			return;

		if (hasCastInThenBranch(ast, typeName, exprStr))
			log(ast, MSG_KEY, typeName);
	}
}