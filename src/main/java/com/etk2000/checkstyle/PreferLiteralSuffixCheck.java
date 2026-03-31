package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that suggests using a literal suffix instead of a
 * widening cast when the other operand of a binary operator or ternary
 * branch is a numeric literal. For example,
 * {@code (long) x * 100} should be {@code x * 100L}, and
 * {@code flag ? (long) x : 0} should be {@code flag ? x : 0L}.
 */
public class PreferLiteralSuffixCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.literal.suffix";

	/**
	 * Finds the other operand/branch relative to the given cast node.
	 * For arithmetic/bitwise operators, returns the sibling operand.
	 * For ternary, returns the other branch.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findOtherOperand(@Nonnull DetailAST typecast) {
		final var parent = typecast.getParent();
		if (parent == null)
			return null;

		if (isBinaryNumericPromotion(parent.getType())) {
			return parent.getFirstChild() == typecast
					? typecast.getNextSibling()
					: parent.getFirstChild();
		}

		// ternary: QUESTION has children [condition, trueBranch, COLON, falseBranch]
		if (parent.getType() == TokenTypes.QUESTION) {
			final var next = typecast.getNextSibling();
			if (next != null && next.getType() == TokenTypes.COLON)
				return next.getNextSibling();
			final var prev = typecast.getPreviousSibling();
			if (prev != null && prev.getType() == TokenTypes.COLON)
				return prev.getPreviousSibling();
		}

		return null;
	}

	@CheckReturnValue
	private static boolean isBinaryNumericPromotion(int tokenType) {
		return switch (tokenType) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BSR,
			     TokenTypes.BXOR, TokenTypes.DIV, TokenTypes.EQUAL,
			     TokenTypes.GE, TokenTypes.GT, TokenTypes.LE,
			     TokenTypes.LT, TokenTypes.MINUS, TokenTypes.MOD,
			     TokenTypes.NOT_EQUAL, TokenTypes.PLUS, TokenTypes.SL,
			     TokenTypes.SR, TokenTypes.STAR -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String suffixFor(@Nonnull String castType) {
		return switch (castType) {
			case "double" -> "d";
			case "float" -> "f";
			case "long" -> "L";
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String typeName(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild == null)
			return null;

		return switch (firstChild.getType()) {
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			default -> null;
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
		return new int[]{TokenTypes.TYPECAST};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var typeNode = ast.findFirstToken(TokenTypes.TYPE);
		if (typeNode == null)
			return;

		final var castType = typeName(typeNode);
		final var suffix = castType != null ? suffixFor(castType) : null;
		if (suffix == null)
			return;

		final var other = findOtherOperand(ast);

		// only flag when the other operand is an integer literal (no suffix)
		// unwrap UNARY_MINUS/UNARY_PLUS to check the inner node
		if (other == null)
			return;
		final var unwrapped = (other.getType() == TokenTypes.UNARY_MINUS || other.getType() == TokenTypes.UNARY_PLUS)
				? other.getFirstChild() : other;
		if (unwrapped == null || unwrapped.getType() != TokenTypes.NUM_INT)
			return;

		final var literalText = other == unwrapped ? unwrapped.getText() : other.getText() + unwrapped.getText();
		log(ast, MSG_KEY, suffix, literalText);
	}
}