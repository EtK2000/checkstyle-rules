package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that requires {@code var} instead of explicit types
 * in for-each loops, try-with-resources, and local variable declarations
 * (where the type is inferrable from the initializer).
 */
public class PreferVarCheck extends AbstractCheck {
	enum PrimitiveVarAction {
		ERROR,
		SKIP,
		WARN
	}

	private static final String MSG_FOREACH = "prefer.var.foreach";
	private static final String MSG_LOCAL = "prefer.var.local";
	private static final String MSG_TRY = "prefer.var.try.resource";
	private static final String MSG_TYPE_ARGS = "prefer.var.type.args";
	private static final String MSG_VAR_EXPLICIT_ARRAY = "prefer.var.explicit.array";
	private static final String MSG_VAR_GENERIC = "prefer.var.generic.return";

	/**
	 * Checks whether the given type parameter name appears anywhere
	 * in a TYPE subtree (including nested generics like {@code Class<T>}).
	 */
	@CheckReturnValue
	private static boolean containsTypeParamName(@Nonnull DetailAST typeNode, @Nonnull String tpName) {
		for (var child = typeNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT && tpName.equals(child.getText()))
				return true;
			if (containsTypeParamName(child, tpName))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static DetailAST getInitializerMethodCall(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		if (value != null && value.getType() == TokenTypes.METHOD_CALL)
			return value;
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String getMethodName(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return null;

		// bare call: method(...)
		if (firstChild.getType() == TokenTypes.IDENT)
			return firstChild.getText();

		// dotted call: receiver.method(...) or receiver.<Type>method(...)
		// the method name is the last child of DOT
		if (firstChild.getType() == TokenTypes.DOT) {
			var last = firstChild.getFirstChild();
			if (last == null)
				return null;
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			if (last.getType() == TokenTypes.IDENT)
				return last.getText();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String getPrimitiveTypeName(@Nonnull DetailAST type) {
		final var child = type.getFirstChild();
		if (child == null)
			return null;
		return switch (child.getType()) {
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
	private static boolean hasGenericReturnType(@Nonnull DetailAST methodCall) {
		final var methodName = getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var firstChild = methodCall.getFirstChild();
		// only check bare calls (same-class methods)
		if (firstChild == null || firstChild.getType() != TokenTypes.IDENT)
			return false;

		// walk up to the enclosing OBJBLOCK
		var parent = methodCall.getParent();
		while (parent != null && parent.getType() != TokenTypes.OBJBLOCK)
			parent = parent.getParent();
		if (parent == null)
			return false;

		// search for METHOD_DEFs with matching name
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var nameNode = child.findFirstToken(TokenTypes.IDENT);
			if (nameNode == null || !methodName.equals(nameNode.getText()))
				continue;

			final var typeParams = child.findFirstToken(TokenTypes.TYPE_PARAMETERS);
			if (typeParams == null)
				continue;

			final var returnType = child.findFirstToken(TokenTypes.TYPE);
			if (returnType == null)
				continue;

			final var returnIdent = returnType.findFirstToken(TokenTypes.IDENT);
			if (returnIdent == null)
				continue;

			// check if the return type name matches a type parameter
			for (var tp = typeParams.getFirstChild(); tp != null; tp = tp.getNextSibling()) {
				if (tp.getType() != TokenTypes.TYPE_PARAMETER)
					continue;

				final var tpIdent = tp.findFirstToken(TokenTypes.IDENT);
				if (tpIdent == null || !returnIdent.getText().equals(tpIdent.getText()))
					continue;

				// check if this type param also appears in any method parameter type
				final var params = child.findFirstToken(TokenTypes.PARAMETERS);
				if (params == null)
					return true; // no params, needs target type

				var inferableFromArgs = false;
				for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
					if (param.getType() != TokenTypes.PARAMETER_DEF)
						continue;

					final var paramType = param.findFirstToken(TokenTypes.TYPE);
					if (paramType != null && containsTypeParamName(paramType, tpIdent.getText())) {
						inferableFromArgs = true;
						break;
					}
				}
				if (!inferableFromArgs)
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasTypeArguments(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return false;

		if (firstChild.getType() == TokenTypes.DOT)
			return firstChild.findFirstToken(TokenTypes.TYPE_ARGUMENTS) != null;
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static String inferredLiteralType(@Nullable DetailAST value) {
		if (value == null)
			return null;
		return switch (value.getType()) {
			case TokenTypes.CHAR_LITERAL -> "char";
			case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
			case TokenTypes.NUM_DOUBLE -> "double";
			case TokenTypes.NUM_FLOAT -> "float";
			case TokenTypes.NUM_INT -> "int";
			case TokenTypes.NUM_LONG -> "long";
			default -> null;
		};
	}

	/**
	 * Returns the cast target type name if the initializer is a cast
	 * expression, or {@code null} otherwise.
	 */
	@CheckReturnValue
	@Nullable
	private static String initializerCastType(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		if (value == null || value.getType() != TokenTypes.TYPECAST)
			return null;
		final var type = value.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;
		final var prim = getPrimitiveTypeName(type);
		if (prim != null)
			return prim;
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : null;
	}

	@CheckReturnValue
	private static boolean isInitializerArrayInit(@Nonnull DetailAST assign) {
		final var value = assign.getFirstChild();
		return value != null && value.getType() == TokenTypes.ARRAY_INIT;
	}

	@CheckReturnValue
	private static boolean isInitializerExplicitArrayInit(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		return value != null
				&& value.getType() == TokenTypes.LITERAL_NEW
				&& value.findFirstToken(TokenTypes.ARRAY_INIT) != null;
	}

	@CheckReturnValue
	private static boolean isInitializerLambdaOrMethodRef(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		return value != null
				&& (value.getType() == TokenTypes.LAMBDA || value.getType() == TokenTypes.METHOD_REF);
	}

	@CheckReturnValue
	private static boolean isInitializerNull(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		// unwrap EXPR wrapper
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		return value != null && value.getType() == TokenTypes.LITERAL_NULL;
	}

	/**
	 * Checks whether the initializer is an anonymous class with exactly
	 * one method and no extra members (fields, inner types, etc.).
	 * These are candidates for lambda conversion and are skipped by
	 * the var check (handled by {@link PreferLambdaCheck} instead).
	 */
	@CheckReturnValue
	private static boolean isInitializerSimpleAnonymousClass(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		if (value == null || value.getType() != TokenTypes.LITERAL_NEW)
			return false;

		final var objBlock = value.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return false;

		var methodCount = 0;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LCURLY, TokenTypes.RCURLY -> {
				}
				case TokenTypes.METHOD_DEF -> {
					if (++methodCount > 1)
						return false;
				}
				default -> {
					return false;
				}
			}
		}
		return methodCount == 1;
	}

	@CheckReturnValue
	private static boolean isLocalVariable(@Nonnull DetailAST varDef) {
		final var parent = varDef.getParent();
		// local variables live in SLIST (block) or FOR_INIT (traditional for-loop)
		return parent != null
				&& (parent.getType() == TokenTypes.SLIST || parent.getType() == TokenTypes.FOR_INIT);
	}

	/**
	 * Returns whether the variable definition is part of a multi-variable
	 * declaration (e.g. {@code int x = 1, y = 2;}). Multi-var declarations
	 * can't use {@code var}, so they must be downgraded to a warning.
	 * <p>
	 * Detection: multi-var declarations have COMMA siblings between the
	 * VARIABLE_DEF nodes. Separate statements ({@code int a; int b;}) have
	 * SEMI siblings instead.
	 */
	@CheckReturnValue
	private static boolean isMultiVarDeclaration(@Nonnull DetailAST varDef) {
		for (var sibling = varDef.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.COMMA)
				return true;
			if (sibling.getType() == TokenTypes.SEMI)
				return false;
		}
		for (var sibling = varDef.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
			if (sibling.getType() == TokenTypes.COMMA)
				return true;
			if (sibling.getType() == TokenTypes.SEMI)
				return false;
		}
		return false;
	}

	/**
	 * Returns the primitive type returned by a known parse method
	 * (e.g. {@code Integer.parseInt} returns {@code "int"}), or
	 * {@code null} if the method call is not a recognized parse method.
	 */
	@CheckReturnValue
	@Nullable
	private static String knownParseReturnType(@Nonnull DetailAST assign) {
		final var call = getInitializerMethodCall(assign);
		if (call == null)
			return null;
		final var dot = call.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		final var method = dot.getLastChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT
				|| method == null || method.getType() != TokenTypes.IDENT)
			return null;
		return switch (receiver.getText() + "." + method.getText()) {
			case "Boolean.parseBoolean" -> "boolean";
			case "Byte.parseByte" -> "byte";
			case "Double.parseDouble" -> "double";
			case "Float.parseFloat" -> "float";
			case "Integer.parseInt" -> "int";
			case "Long.parseLong" -> "long";
			case "Short.parseShort" -> "short";
			default -> null;
		};
	}

	/**
	 * Checks how a primitive-typed local variable interacts with
	 * {@code var} inference. Returns:
	 * <ul>
	 *   <li>{@code SKIP} — unfixable mismatch (byte, short, int from char)</li>
	 *   <li>{@code WARN} — primitive with non-literal expression (can't verify type)</li>
	 *   <li>{@code ERROR} — safe to flag (literal matches or is fixable with suffix)</li>
	 * </ul>
	 * For non-primitive types, always returns {@code ERROR}.
	 */
	@CheckReturnValue
	@Nonnull
	private static PrimitiveVarAction primitiveVarAction(@Nonnull DetailAST varDef, @Nonnull DetailAST assign) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return PrimitiveVarAction.ERROR;

		final var declaredType = getPrimitiveTypeName(type);
		if (declaredType == null)
			return PrimitiveVarAction.ERROR;

		// cast to declared type: var infers the cast type, safe
		final var castType = initializerCastType(assign);
		if (declaredType.equals(castType))
			return PrimitiveVarAction.ERROR;

		final var value = unwrapInitializerValue(assign);
		final var inferredType = inferredLiteralType(value);

		// non-literal expression on a primitive: can't verify inferred type
		// unless it's a known parse method whose return type matches
		if (inferredType == null) {
			final var parseType = knownParseReturnType(assign);
			if (declaredType.equals(parseType))
				return PrimitiveVarAction.ERROR;
			return PrimitiveVarAction.WARN;
		}

		// literal type matches declared type: safe
		if (inferredType.equals(declaredType))
			return PrimitiveVarAction.ERROR;

		// declared type differs from inferred literal type — only flag if fixable via suffix
		final var fixable = ("int".equals(inferredType) && switch (declaredType) {
			case "double", "float", "long" -> true; // add ./F/L
			default -> false;
		}) || switch (declaredType) {
			case "double" ->
					"float".equals(inferredType) || "long".equals(inferredType); // remove f / change L to .
			case "float" -> "double".equals(inferredType); // add f
			default -> false;
		};
		return fixable ? PrimitiveVarAction.ERROR : PrimitiveVarAction.SKIP;
	}

	/**
	 * Unwraps the initializer value from an ASSIGN node, stripping the EXPR
	 * wrapper and any unary +/- prefix.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapInitializerValue(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		while (value != null
				&& (value.getType() == TokenTypes.UNARY_MINUS || value.getType() == TokenTypes.UNARY_PLUS))
			value = value.getFirstChild();
		return value;
	}

	private final Set<String> imports = new HashSet<>();

	private Set<String> allowedMethods = Set.of();
	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.FOR_EACH_CLAUSE,
				TokenTypes.IMPORT,
				TokenTypes.PACKAGE_DEF,
				TokenTypes.RESOURCE,
				TokenTypes.VARIABLE_DEF
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@CheckReturnValue
	private boolean hasReflectionGenericReturnType(@Nonnull DetailAST methodCall) {
		final var methodName = getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var receiverTypeName = AstUtil.getReceiverTypeName(methodCall, packageName, imports);
		if (receiverTypeName == null)
			return false;

		final var fqcn = ReflectionUtil.resolveClassName(receiverTypeName, packageName, imports);
		if (fqcn == null)
			return false;

		return ReflectionUtil.hasGenericReturnType(fqcn, methodName);
	}

	@CheckReturnValue
	private boolean isVarType(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null && "var".equals(ident.getText());
	}

	private void logWarning(@Nonnull DetailAST ast, @Nonnull String msgKey, @Nonnull Object... args) {
		final var savedSeverity = getSeverity();
		setSeverity(SeverityLevel.WARNING.getName());
		log(ast, msgKey, args);
		setSeverity(savedSeverity);
	}

	/**
	 * Sets method names whose generic return type is inferred from the
	 * left-hand side, so an explicit type is preferred over {@code var}.
	 * For example, {@code findViewById} returns {@code <T extends View> T}.
	 * <p>Called by Checkstyle via reflection when {@code allowedMethods} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setAllowedMethods(@Nonnull String... methods) {
		allowedMethods = Set.of(methods);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> {
				final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
				if (varDef != null && !isVarType(varDef))
					log(varDef, MSG_FOREACH);
			}
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
			case TokenTypes.RESOURCE -> {
				if (!isVarType(ast))
					log(ast, MSG_TRY);
			}
			case TokenTypes.VARIABLE_DEF -> {
				if (!isLocalVariable(ast))
					return;

				// must have an initializer
				final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
				if (assign == null)
					return;

				// multi-var declarations (int x = 1, y = 2;) can't use var
				if (isMultiVarDeclaration(ast)) {
					if (!isVarType(ast))
						logWarning(ast, MSG_LOCAL);
					return;
				}

				// skip null initializers (type can't be inferred)
				if (isInitializerNull(assign))
					return;

				// skip implicit array initializers ({...} without new), var can't be used
				if (isInitializerArrayInit(assign))
					return;

				// prefer implicit array init over explicit new Type[]{...}
				if (isInitializerExplicitArrayInit(assign)) {
					log(ast, MSG_VAR_EXPLICIT_ARRAY);
					return;
				}

				// skip lambdas and method references (var can't infer a target type)
				if (isInitializerLambdaOrMethodRef(assign))
					return;

				// skip simple anonymous classes (PreferLambdaCheck handles these)
				if (isInitializerSimpleAnonymousClass(assign))
					return;

				// check primitive type safety before flagging
				final var primAction = primitiveVarAction(ast, assign);
				if (primAction == PrimitiveVarAction.SKIP)
					return;

				final var methodCall = getInitializerMethodCall(assign);
				final var methodName = methodCall == null ? null : getMethodName(methodCall);
				final var isGeneric = (methodName != null && allowedMethods.contains(methodName))
						|| (methodCall != null && hasGenericReturnType(methodCall))
						|| (methodCall != null && hasReflectionGenericReturnType(methodCall));

				// warn about explicit type arguments (prefer explicit type on the left)
				if (isGeneric && hasTypeArguments(methodCall)) {
					logWarning(ast, MSG_TYPE_ARGS, methodName);
					return;
				}

				if (isVarType(ast)) {
					// warn when var is used with a generic return type method
					if (isGeneric)
						logWarning(ast, MSG_VAR_GENERIC, methodName);
				}
				else if (!isGeneric) {
					// primitive with non-literal expression: warn (can't verify inferred type)
					if (primAction == PrimitiveVarAction.WARN)
						logWarning(ast, MSG_LOCAL);
					else
						log(ast, MSG_LOCAL);
				}
			}
		}
	}
}