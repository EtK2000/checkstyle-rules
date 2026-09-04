package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags redundant numeric literal suffixes
 * ({@code L}, {@code f}, {@code d}). The {@code d} suffix on a literal with a
 * decimal point or an exponent is always redundant, since such a literal is
 * already a {@code double}. Any other suffix is redundant only when the
 * unsuffixed literal is an {@code int} of the same value and the literal's
 * target is proven ({@link ProvenTypes}) to be a primitive that {@code int}
 * widens to. Boxing has no widening step (JLS 5.2), so a boxed,
 * {@code Object} or other reference target binds the literal's own type:
 * {@code Object o = 1L} holds a {@code Long}, {@code Object o = 1} an
 * {@code Integer}. A target the syntax tree does not prove is never reported.
 */
public class RedundantNumericSuffixCheck extends AbstractResolvingCheck {
	private static final Set<String> NUMERIC_PRIMITIVES = Set.of("byte", "char", "double", "float", "int", "long", "short");
	private static final String FLOAT = "float";
	private static final String MSG_KEY = "redundant.numeric.suffix";

	/**
	 * Whether the suffix is load-bearing once the declaration becomes {@code var}.
	 * A single-variable local must use {@code var} (see {@code PreferVarCheck}), and
	 * {@code var} binds the literal's own type, so dropping a suffix the unsuffixed
	 * literal would not imply silently retypes the variable: {@code long l = 5L} would
	 * become an {@code int}.
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

		// only when the literal is the whole initializer, behind any signs: nested in an
		// expression its type is decided by promotion, and such a declaration is never
		// converted to `var`
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return false;
		var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null
				&& (value.getType() == TokenTypes.BNOT || value.getType() == TokenTypes.UNARY_MINUS
				|| value.getType() == TokenTypes.UNARY_PLUS))
			value = AstQuery.unwrapParensAndExpr(value.getFirstChild());
		return value == literal;
	}

	@CheckReturnValue
	private static boolean isIntegerValue(@Nonnull String text) {
		final var withoutSuffix = text.substring(0, text.length() - 1);
		return !withoutSuffix.contains(".")
				&& !withoutSuffix.contains("e") && !withoutSuffix.contains("E")
				&& !withoutSuffix.contains("p") && !withoutSuffix.contains("P");
	}

	/**
	 * Whether {@code node} hands the literal's value on to its own context with the type
	 * question unchanged: parentheses and signs, a conditional or switch-expression arm, and an
	 * array initializer, whose elements take the array's element type.
	 */
	@CheckReturnValue
	private static boolean isTransparent(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.ANNOTATION_ARRAY_INIT, TokenTypes.ARRAY_INIT, TokenTypes.BNOT,
			     TokenTypes.EXPR, TokenTypes.LITERAL_SWITCH, TokenTypes.QUESTION,
			     TokenTypes.SWITCH_RULE, TokenTypes.UNARY_MINUS, TokenTypes.UNARY_PLUS -> true;
			default -> false;
		};
	}

	/**
	 * Whether the integer-valued {@code f}/{@code d} literal {@code text} is a zero, which a
	 * minus makes a negative zero only while it stays floating point: {@code -0} is the
	 * {@code int} zero, so {@code float x = -0} holds {@code +0.0f}.
	 */
	@CheckReturnValue
	private static boolean isZero(@Nonnull String text) {
		return text.substring(0, text.length() - 1).replace("_", "").chars().allMatch(c -> c == '0');
	}

	/**
	 * Whether dropping the suffix of the integer-valued {@code f}/{@code d} literal {@code text}
	 * leaves an {@code int} literal of the same value. A leading zero would make it octal
	 * ({@code 010d} is 10.0 but {@code 010} is 8, and {@code 09} does not compile), an
	 * {@code int} literal stops at {@link Integer#MAX_VALUE}, and a {@code float} literal has
	 * already rounded its value ({@code 16777217f} is 16777216) where the {@code int} has not.
	 */
	@CheckReturnValue
	private static boolean keepsValueAsInt(@Nonnull String text, boolean isFloat) {
		final var digits = text.substring(0, text.length() - 1).replace("_", "");
		final long value;
		try {
			value = Long.parseLong(digits);
		}
		catch (NumberFormatException ignored) {
			return false;
		}
		return (value == 0 || digits.charAt(0) != '0')
				&& value <= Integer.MAX_VALUE
				&& (!isFloat || (long) (float) value == value);
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

	/**
	 * The rank of the type binary numeric promotion gives a compound assignment's operation
	 * on the primitive {@code key} (JLS 5.6): {@code int} and narrower 0, {@code long} 1,
	 * {@code float} 2, {@code double} 3.
	 */
	@CheckReturnValue
	private static int promotedRank(@Nonnull String key) {
		return switch (key) {
			case FLOAT -> 2;
			case "double" -> 3;
			case "long" -> 1;
			default -> 0;
		};
	}

	/** The element type of the array type {@code key}, or {@code key} itself when it is not one. */
	@CheckReturnValue
	@Nullable
	private static String stripArrays(@Nullable String key) {
		return key == null ? null : key.replace("[]", "");
	}

	/** The {@link #promotedRank} of the literal's own type. */
	@CheckReturnValue
	private static int suffixRank(@Nonnull DetailAST literal) {
		return switch (literal.getType()) {
			case TokenTypes.NUM_DOUBLE -> 3;
			case TokenTypes.NUM_FLOAT -> 2;
			default -> 1;
		};
	}

	@Nonnull
	private ProvenTypes types = new ProvenTypes(this::resolve, null);

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		types = new ProvenTypes(this::resolve, packageName());
	}

	/**
	 * Checkstyle reuses one check instance across files, so {@code types} left populated would pin
	 * the finished file's whole AST through the declarations it has keyed.
	 */
	@Override
	protected void finishFile(@Nullable DetailAST rootAST) {
		types = new ProvenTypes(this::resolve, null);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_LONG};
	}

	/**
	 * Whether {@code literal}'s value lands in a target this file proves to be a numeric
	 * primitive. The walk climbs through {@link #isTransparent} nodes and the first other node
	 * decides. A compound assignment also needs its operation to keep its type, which it does
	 * only when the left side already promotes at least as wide as the literal: {@code i += 1f}
	 * on an {@code int} adds in {@code float}, rounding {@code i}, where {@code i += 1} does not.
	 * An {@code f} literal reached through an arm needs a {@code float} target, since a numeric
	 * conditional takes the arms' promoted type first (JLS 15.25): {@code flag ? 1f : i} rounds
	 * {@code i} through {@code float} before a {@code double} target widens it, and
	 * {@code flag ? 1 : i} does not.
	 *
	 * @param signedZero whether the literal is a zero {@code f}/{@code d} ({@link #isZero}), so a
	 *                   minus on the way up makes the suffix load-bearing
	 */
	@CheckReturnValue
	private boolean hasProvenPrimitiveTarget(@Nonnull DetailAST literal, boolean signedZero) {
		var throughArm = false;
		var node = literal.getParent();
		for (; isTransparent(node); node = node.getParent()) {
			switch (node.getType()) {
				case TokenTypes.LITERAL_SWITCH, TokenTypes.QUESTION, TokenTypes.SWITCH_RULE -> throughArm = true;
				case TokenTypes.UNARY_MINUS -> {
					if (signedZero)
						return false;
				}
				default -> {
				}
			}
		}

		final var armNeedsFloat = throughArm && literal.getType() == TokenTypes.NUM_FLOAT;
		// an annotation element taking a numeric value can only have a primitive type (JLS 9.6.1),
		// but which one is not in the tree
		if (node.getType() == TokenTypes.ANNOTATION || node.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR)
			return !armNeedsFloat;

		final var target = targetKey(node, literal);
		return target != null && NUMERIC_PRIMITIVES.contains(target) && (!armNeedsFloat || FLOAT.equals(target));
	}

	/**
	 * The return type of the method {@code returnNode} returns from, or null when it returns from
	 * a lambda, whose type comes from the functional interface it targets.
	 */
	@CheckReturnValue
	@Nullable
	private String returnKey(@Nonnull DetailAST returnNode) {
		var scope = returnNode.getParent();
		while (scope.getType() != TokenTypes.METHOD_DEF && scope.getType() != TokenTypes.LAMBDA)
			scope = scope.getParent();
		return scope.getType() == TokenTypes.LAMBDA ? null : types.typeKey(scope.findFirstToken(TokenTypes.TYPE));
	}

	/**
	 * The key of the type the deciding node {@code context} gives {@code literal}'s value, or null
	 * when it gives none this file proves, or one the suffix is load-bearing for.
	 */
	@CheckReturnValue
	@Nullable
	private String targetKey(@Nonnull DetailAST context, @Nonnull DetailAST literal) {
		return switch (context.getType()) {
			case TokenTypes.ASSIGN -> {
				final var varDef = context.getParent();
				if (varDef.getType() != TokenTypes.VARIABLE_DEF)
					yield types.keyOf(context.getFirstChild());
				yield establishesTypeUnderVar(literal, varDef)
						? null
						: stripArrays(types.typeKey(varDef.findFirstToken(TokenTypes.TYPE)));
			}
			case TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN, TokenTypes.BSR_ASSIGN,
			     TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN,
			     TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN, TokenTypes.SL_ASSIGN,
			     TokenTypes.SR_ASSIGN, TokenTypes.STAR_ASSIGN -> {
				final var left = types.keyOf(context.getFirstChild());
				yield left == null || !NUMERIC_PRIMITIVES.contains(left) || promotedRank(left) < suffixRank(literal)
						? null
						: left;
			}
			case TokenTypes.LITERAL_DEFAULT ->
					stripArrays(types.typeKey(context.getParent().findFirstToken(TokenTypes.TYPE)));
			case TokenTypes.LITERAL_NEW -> stripArrays(types.newKey(context, false));
			case TokenTypes.LITERAL_RETURN -> returnKey(context);
			case TokenTypes.TYPECAST -> types.keyOf(context);
			default -> null;
		};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		final var text = ast.getText();
		final var suffix = text.substring(text.length() - 1);
		switch (ast.getType()) {
			case TokenTypes.NUM_DOUBLE -> {
				if ((text.endsWith("d") || text.endsWith("D"))
						&& (!isIntegerValue(text) || keepsValueAsInt(text, false) && hasProvenPrimitiveTarget(ast, isZero(text))))
					log(ast, MSG_KEY, suffix);
			}
			// checkstyle also lexes an unsuffixed decimal (1., .5, 1e+2) as NUM_FLOAT
			case TokenTypes.NUM_FLOAT -> {
				if ((text.endsWith("f") || text.endsWith("F"))
						&& isIntegerValue(text) && keepsValueAsInt(text, true) && hasProvenPrimitiveTarget(ast, isZero(text)))
					log(ast, MSG_KEY, suffix);
			}
			case TokenTypes.NUM_LONG -> {
				if (longFitsInInt(text) && hasProvenPrimitiveTarget(ast, false))
					log(ast, MSG_KEY, suffix);
			}
		}
	}
}