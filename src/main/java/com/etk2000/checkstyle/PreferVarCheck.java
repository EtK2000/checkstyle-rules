package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
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
	private static boolean isInitializerAnonymousClass(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		return value != null
				&& value.getType() == TokenTypes.LITERAL_NEW
				&& value.findFirstToken(TokenTypes.OBJBLOCK) != null;
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

	@CheckReturnValue
	private static boolean isLocalVariable(@Nonnull DetailAST varDef) {
		final var parent = varDef.getParent();
		// local variables live in SLIST (block), not OBJBLOCK (class body)
		return parent != null && parent.getType() == TokenTypes.SLIST;
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
		setSeverity("warning");
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

				// skip anonymous classes (var would infer the anonymous type, not the declared type)
				if (isInitializerAnonymousClass(assign))
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
				else if (!isGeneric)
					log(ast, MSG_LOCAL);
			}
		}
	}
}