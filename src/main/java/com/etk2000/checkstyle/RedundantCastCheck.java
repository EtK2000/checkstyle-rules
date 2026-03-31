package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags redundant type casts: same-type casts,
 * casts to the same primitive type as the assignment/return target,
 * null casts matching the target type, and widening primitive casts
 * that Java performs implicitly.
 */
public class RedundantCastCheck extends AbstractCheck {
	private static final String MSG_KEY = "redundant.cast";

	@CheckReturnValue
	@Nullable
	private static String castTypeName(@Nonnull DetailAST typecast) {
		final var type = typecast.findFirstToken(TokenTypes.TYPE);
		return type != null ? typeName(type) : null;
	}

	/**
	 * Returns the target type from context if the cast is in a variable
	 * declaration assignment or a return statement with a known return
	 * type. Returns {@code null} if the context target type cannot be
	 * determined.
	 */
	@CheckReturnValue
	@Nullable
	private static String contextTargetType(@Nonnull DetailAST typecast) {
		var parent = typecast.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR)
			parent = parent.getParent();
		if (parent == null)
			return null;

		if (parent.getType() == TokenTypes.ASSIGN) {
			final var grandparent = parent.getParent();
			if (grandparent != null && grandparent.getType() == TokenTypes.VARIABLE_DEF)
				return variableDefType(grandparent);
		}

		if (parent.getType() == TokenTypes.LITERAL_RETURN)
			return methodReturnType(parent);

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String enclosingClassName(@Nonnull DetailAST ast) {
		var node = ast.getParent();
		while (node != null) {
			if (node.getType() == TokenTypes.CLASS_DEF
					|| node.getType() == TokenTypes.ENUM_DEF
					|| node.getType() == TokenTypes.INTERFACE_DEF
					|| node.getType() == TokenTypes.RECORD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				return ident != null ? ident.getText() : null;
			}
			node = node.getParent();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String expressionType(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.CHAR_LITERAL -> "char";
			case TokenTypes.EXPR -> {
				final var child = expr.getFirstChild();
				yield child != null ? expressionType(child) : null;
			}
			case TokenTypes.IDENT -> lookupVariableType(expr);
			case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
			case TokenTypes.LITERAL_NEW -> {
				final var ident = expr.findFirstToken(TokenTypes.IDENT);
				yield ident != null ? ident.getText() : null;
			}
			case TokenTypes.LITERAL_THIS -> enclosingClassName(expr);
			case TokenTypes.NUM_DOUBLE -> "double";
			case TokenTypes.NUM_FLOAT -> {
				// NUM_FLOAT is any floating-point literal; check suffix
				// to distinguish Java float (f/F) from double (default)
				final var text = expr.getText();
				yield text.endsWith("f") || text.endsWith("F") ? "float" : "double";
			}
			case TokenTypes.NUM_INT -> "int";
			case TokenTypes.NUM_LONG -> "long";
			case TokenTypes.STRING_LITERAL -> "String";
			case TokenTypes.TYPECAST -> castTypeName(expr);
			default -> null;
		};
	}

	/**
	 * Checks if a widening cast is inside a compound assignment
	 * ({@code +=}, {@code -=}, etc.) where it is always redundant.
	 */
	@CheckReturnValue
	private static boolean isCompoundAssignment(@Nonnull DetailAST typecast) {
		var parent = typecast.getParent();
		while (parent != null && parent.getType() == TokenTypes.EXPR)
			parent = parent.getParent();
		if (parent == null)
			return false;

		return switch (parent.getType()) {
			case TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			     TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN,
			     TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN,
			     TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN,
			     TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN,
			     TokenTypes.STAR_ASSIGN -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isPrimitive(@Nonnull String type) {
		return switch (type) {
			case "boolean", "byte", "char", "double", "float",
			     "int", "long", "short" -> true;
			default -> false;
		};
	}

	/**
	 * Checks if a widening cast is redundant because the sibling operand
	 * in a binary operator already has the cast type or wider, making
	 * the binary numeric promotion handle the widening automatically.
	 * For example, {@code (long) x * 100L} is redundant because
	 * {@code 100L} already promotes the multiplication to {@code long}.
	 * Excludes shift operators where only the left operand determines
	 * the result type.
	 */
	@CheckReturnValue
	private static boolean isSiblingAlreadyWiderOrEqual(@Nonnull DetailAST typecast, @Nonnull String castType) {
		final var parent = typecast.getParent();
		if (parent == null)
			return false;

		return switch (parent.getType()) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BXOR,
			     TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE,
			     TokenTypes.GT, TokenTypes.LE, TokenTypes.LT,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL,
			     TokenTypes.PLUS, TokenTypes.STAR -> {
				final var sibling = parent.getFirstChild() == typecast
						? typecast.getNextSibling()
						: parent.getFirstChild();
				if (sibling == null)
					yield false;
				final var siblingType = expressionType(sibling);
				yield castType.equals(siblingType)
						|| (siblingType != null && isWideningPrimitive(castType, siblingType));
			}
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isWideningPrimitive(@Nonnull String fromType, @Nonnull String toType) {
		return switch (fromType) {
			case "byte" -> switch (toType) {
				case "double", "float", "int", "long", "short" -> true;
				default -> false;
			};
			case "char" -> switch (toType) {
				case "double", "float", "int", "long" -> true;
				default -> false;
			};
			case "float" -> "double".equals(toType);
			case "int" -> switch (toType) {
				case "double", "float", "long" -> true;
				default -> false;
			};
			case "long" -> "double".equals(toType) || "float".equals(toType);
			case "short" -> switch (toType) {
				case "double", "float", "int", "long" -> true;
				default -> false;
			};
			default -> false;
		};
	}

	/**
	 * Checks if a widening cast is redundant in its context. Only flags
	 * when the cast is in a simple assignment to a primitive variable or
	 * a return in a method with a primitive return type. Avoids flagging
	 * in arithmetic, method arguments, or ternary contexts where the
	 * widening may change semantics.
	 */
	@CheckReturnValue
	private static boolean isWideningRedundantInContext(@Nonnull DetailAST typecast) {
		final var targetType = contextTargetType(typecast);
		return targetType != null && isPrimitive(targetType);
	}

	@CheckReturnValue
	@Nullable
	private static String lookupVariableType(@Nonnull DetailAST ident) {
		final var name = ident.getText();
		var scope = ident.getParent();
		while (scope != null) {
			final var type = searchScope(scope, name);
			if (type != null)
				return type;
			scope = scope.getParent();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String methodReturnType(@Nonnull DetailAST returnNode) {
		var parent = returnNode.getParent();
		while (parent != null) {
			if (parent.getType() == TokenTypes.METHOD_DEF) {
				final var type = parent.findFirstToken(TokenTypes.TYPE);
				return type != null ? typeName(type) : null;
			}
			if (parent.getType() == TokenTypes.CLASS_DEF
					|| parent.getType() == TokenTypes.LAMBDA
					|| parent.getType() == TokenTypes.RECORD_DEF)
				return null;
			parent = parent.getParent();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String searchScope(@Nonnull DetailAST scope, @Nonnull String name) {
		for (var child = scope.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.PARAMETER_DEF
					|| child.getType() == TokenTypes.VARIABLE_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null && name.equals(ident.getText()))
					return variableDefType(child);
			}
			if (child.getType() == TokenTypes.FOR_EACH_CLAUSE
					|| child.getType() == TokenTypes.FOR_INIT
					|| child.getType() == TokenTypes.PARAMETERS) {
				final var type = searchScope(child, name);
				if (type != null)
					return type;
			}
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String typeName(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild == null)
			return null;

		return switch (firstChild.getType()) {
			case TokenTypes.IDENT -> firstChild.getText();
			case TokenTypes.LITERAL_BOOLEAN -> "boolean";
			case TokenTypes.LITERAL_BYTE -> "byte";
			case TokenTypes.LITERAL_CHAR -> "char";
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			case TokenTypes.LITERAL_SHORT -> "short";
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String variableDefType(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		return type != null ? typeName(type) : null;
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
		final var castType = castTypeName(ast);
		if (castType == null)
			return;

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		final var expr = rparen != null ? rparen.getNextSibling() : null;
		if (expr == null)
			return;

		// null is assignable to any reference type; (Type) null is only
		// meaningful in method/constructor args (overload resolution) or
		// var declarations (type inference), so flag it everywhere else
		if (expr.getType() == TokenTypes.LITERAL_NULL) {
			final var targetType = contextTargetType(ast);
			if (targetType != null && !"var".equals(targetType)) {
				log(ast, MSG_KEY, castType, "null");
				return;
			}
		}

		final var exprType = expressionType(expr);
		if (exprType == null)
			return;

		// same-type cast is always redundant
		if (castType.equals(exprType)) {
			log(ast, MSG_KEY, castType, exprType);
			return;
		}

		// widening primitive cast: redundant in assignment/return, compound assignment, or when sibling is already wider
		if (isWideningPrimitive(exprType, castType)
				&& (isCompoundAssignment(ast) || isWideningRedundantInContext(ast) || isSiblingAlreadyWiderOrEqual(ast, castType)))
			log(ast, MSG_KEY, castType, exprType);
	}
}