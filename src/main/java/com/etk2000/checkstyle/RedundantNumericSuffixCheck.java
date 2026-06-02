package com.etk2000.checkstyle;

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
public class RedundantNumericSuffixCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "redundant.numeric.suffix";

	/**
	 * Whether the suffix is load-bearing once the declaration becomes {@code var}.
	 * A single-variable local must use {@code var} (see {@code PreferVarCheck}), and
	 * {@code var} binds the literal's own type, so dropping a suffix the unsuffixed
	 * literal would not imply silently retypes the variable: {@code long l = 5L} would
	 * become an {@code int}. Only {@code d} on a literal that already has a decimal
	 * point or an exponent is safe, because such a literal is a {@code double} without
	 * it.
	 */
	@CheckReturnValue
	private static boolean establishesTypeUnderVar(@Nonnull DetailAST literal, @Nonnull DetailAST varDef) {
		// mirrors PreferVarCheck.isLocalVariable: a for-init declaration is converted too,
		// so `for (long i = 0L; ...)` would otherwise become an int-typed loop counter
		final var parent = varDef.getParent();
		if (parent == null
				|| (parent.getType() != TokenTypes.SLIST && parent.getType() != TokenTypes.FOR_INIT))
			return false;

		// a shared declaration is never converted, so nothing rebinds the literal and the
		// suffix is as redundant as it would be on a field
		if (PreferVarCheck.isMultiVarDeclaration(varDef))
			return false;

		// only when the literal is the whole initializer: nested in an expression its type
		// is decided by promotion, and such a declaration is never converted to `var`
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return false;
		var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null
				&& (value.getType() == TokenTypes.UNARY_MINUS || value.getType() == TokenTypes.UNARY_PLUS))
			value = AstUtil.unwrapParensAndExpr(value.getFirstChild());
		if (value != literal)
			return false;

		final var text = literal.getText();
		final var suffix = text.charAt(text.length() - 1);
		return isIntegerValue(text) || (suffix != 'd' && suffix != 'D');
	}

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
		// IDENT means var, wrapper, or other reference type
		return type == null || type.findFirstToken(TokenTypes.IDENT) != null;
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
					return isNonPrimitiveType(node) || establishesTypeUnderVar(literal, node);
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
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_LONG};
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