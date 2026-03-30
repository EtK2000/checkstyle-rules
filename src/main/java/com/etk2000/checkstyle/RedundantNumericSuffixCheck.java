package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags redundant numeric literal suffixes
 * ({@code L}, {@code f}, {@code d}). If a literal's value fits in
 * {@code int}, the suffix is unnecessary because {@code int} widens
 * to {@code long}, {@code float}, and {@code double} automatically.
 * The {@code d} suffix on decimal literals (e.g. {@code 0.0d}) is
 * always redundant since the literal is already {@code double}.
 */
public class RedundantNumericSuffixCheck extends AbstractCheck {
	private static final String MSG_KEY = "redundant.numeric.suffix";

	@CheckReturnValue
	private static boolean isIntegerValue(@Nonnull String text) {
		final var withoutSuffix = text.substring(0, text.length() - 1);
		return !withoutSuffix.contains(".")
				&& !withoutSuffix.contains("e") && !withoutSuffix.contains("E")
				&& !withoutSuffix.contains("p") && !withoutSuffix.contains("P");
	}

	@CheckReturnValue
	private static boolean isNonPrimitiveType(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return true;
		// IDENT means var, wrapper, or other reference type
		return type.findFirstToken(TokenTypes.IDENT) != null;
	}

	@CheckReturnValue
	private static boolean isPrimitiveMethodReturn(@Nonnull DetailAST returnNode) {
		var parent = returnNode.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.METHOD_DEF) {
				final var type = parent.findFirstToken(TokenTypes.TYPE);
				if (type == null)
					return false;
				final var child = type.getFirstChild();
				return child != null && child.getType() != TokenTypes.IDENT;
			}
			if (parent.getType() == TokenTypes.CLASS_DEF
					|| parent.getType() == TokenTypes.LAMBDA
					|| parent.getType() == TokenTypes.RECORD_DEF)
				return false;
			parent = parent.getParent();
		}
		return false;
	}

	/**
	 * Checks if the literal is in a context where the suffix determines
	 * the type and cannot be safely removed.
	 */
	@CheckReturnValue
	private static boolean isSuffixRequired(@Nonnull DetailAST literal) {
		var node = literal.getParent();
		while (node != null) {
			switch (node.getType()) {
				case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BSR,
				     TokenTypes.BXOR, TokenTypes.DIV, TokenTypes.ELIST,
				     TokenTypes.LAMBDA, TokenTypes.MINUS, TokenTypes.MOD,
				     TokenTypes.PLUS, TokenTypes.SL, TokenTypes.SR,
				     TokenTypes.STAR -> {
					return true;
				}
				case TokenTypes.CLASS_DEF, TokenTypes.COMPACT_CTOR_DEF,
				     TokenTypes.CTOR_DEF, TokenTypes.ENUM_DEF,
				     TokenTypes.METHOD_DEF, TokenTypes.RECORD_DEF -> {
					return false;
				}
				case TokenTypes.LITERAL_RETURN -> {
					return !isPrimitiveMethodReturn(node);
				}
				case TokenTypes.VARIABLE_DEF -> {
					return isNonPrimitiveType(node);
				}
			}
			node = node.getParent();
		}
		return false;
	}

	@CheckReturnValue
	private static boolean longFitsInInt(@Nonnull String text) {
		final var clean = text.substring(0, text.length() - 1).replace("_", "");
		try {
			final long value;
			if (clean.startsWith("0b") || clean.startsWith("0B"))
				value = Long.parseLong(clean.substring(2), 2);
			else
				value = Long.decode(clean);
			return value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE;
		}
		catch (NumberFormatException ignored) {
			return false;
		}
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_LONG};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var text = ast.getText();
		switch (ast.getType()) {
			case TokenTypes.NUM_DOUBLE -> {
				if ((text.endsWith("d") || text.endsWith("D"))
						&& (!isIntegerValue(text) || !isSuffixRequired(ast)))
					log(ast, MSG_KEY, text.substring(text.length() - 1));
			}
			case TokenTypes.NUM_FLOAT -> {
				if (isIntegerValue(text) && !isSuffixRequired(ast))
					log(ast, MSG_KEY, text.substring(text.length() - 1));
			}
			case TokenTypes.NUM_LONG -> {
				if (longFitsInInt(text) && !isSuffixRequired(ast))
					log(ast, MSG_KEY, text.substring(text.length() - 1));
			}
		}
	}
}