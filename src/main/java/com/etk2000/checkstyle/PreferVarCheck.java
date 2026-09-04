package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that requires {@code var} instead of explicit types
 * in for-each loops, try-with-resources, and local variable declarations
 * (where the type is inferrable from the initializer).
 */
public class PreferVarCheck extends AbstractResolvingCheck {
	enum PrimitiveVarAction {
		ERROR,
		SKIP,
		WARN
	}

	private static final Set<Integer> BOOLEAN_OPERATORS = Set.of(
			TokenTypes.EQUAL, TokenTypes.GE, TokenTypes.GT, TokenTypes.LAND, TokenTypes.LE,
			TokenTypes.LITERAL_INSTANCEOF, TokenTypes.LNOT, TokenTypes.LOR, TokenTypes.LT, TokenTypes.NOT_EQUAL
	);
	private static final Set<String> BOXED_TYPES = Set.of(
			"Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short"
	);
	private static final Set<String> PRIMITIVE_TYPES = Set.of(
			"boolean", "byte", "char", "double", "float", "int", "long", "short"
	);
	private static final Set<String> WIDENING_SUPERTYPE_FQCNS = Set.of(
			"java.io.Serializable", "java.lang.CharSequence", "java.lang.Comparable",
			"java.lang.Number", "java.lang.Object"
	);
	private static final Set<String> WIDENING_SUPERTYPES = Set.of(
			"CharSequence", "Comparable", "Number", "Object", "Serializable"
	);
	private static final String MSG_DIAMOND = "prefer.var.diamond";
	private static final String MSG_FOREACH = "prefer.var.foreach";
	private static final String MSG_FOREACH_WARNING = "prefer.var.foreach.warning";
	private static final String MSG_LOCAL = "prefer.var.local";
	private static final String MSG_LOCAL_WARNING = "prefer.var.local.warning";
	private static final String MSG_TRY = "prefer.var.try.resource";
	private static final String MSG_TRY_WARNING = "prefer.var.try.resource.warning";
	private static final String MSG_TYPE_ARGS = "prefer.var.type.args";
	public static final String MSG_VAR_EXPLICIT_ARRAY = "prefer.var.explicit.array";
	private static final String MSG_VAR_GENERIC = "prefer.var.generic.return";
	private static final String OBJECT_FQCN = "java.lang.Object";

	@CheckReturnValue
	private static boolean bodyDeclaresMethod(
			@Nonnull DetailAST objBlock,
			@Nonnull String methodName,
			@Nonnull Set<DetailAST> visited
	) {
		final Function<DetailAST, DetailAST> declaring = body -> methodsNamed(body, methodName).isEmpty() ? null : body;
		return AstResolve.firstInHierarchy(List.of(objBlock), AstResolve::inheritedBodies, visited, declaring) != null;
	}

	/**
	 * The name of the method a call invokes on an enclosing body: a bare call, one qualified by
	 * {@code this}/{@code super} or their {@code Outer.this}/{@code Iface.super} forms, or one
	 * qualified by the name of an enclosing type. Null when the receiver is something else. A
	 * foreign receiver's methods are not in this file, so matching a same-named method here would
	 * judge the call by an unrelated declaration.
	 */
	@CheckReturnValue
	@Nullable
	private static String calledMethodName(@Nonnull DetailAST methodCall) {
		if (methodCall.getType() != TokenTypes.METHOD_CALL)
			return null;

		final var first = methodCall.getFirstChild();
		if (first == null)
			return null;
		if (first.getType() == TokenTypes.IDENT)
			return first.getText();

		if (first.getType() != TokenTypes.DOT)
			return null;

		final var receiver = first.getFirstChild();
		if (receiver == null)
			return null;

		final var self = receiver.getType() == TokenTypes.DOT ? receiver.getLastChild() : receiver;
		return self.getType() == TokenTypes.LITERAL_THIS || self.getType() == TokenTypes.LITERAL_SUPER
				|| namesEnclosingType(methodCall, receiver)
				? AstQuery.getMethodName(methodCall)
				: null;
	}

	/**
	 * Every arm of a conditional or switch-expression initializer, or {@code null} when any arm
	 * is not a {@code new}. Only a diamond can receive the declared type arguments; any other
	 * arm infers on its own, so the variable would bind the arms' least upper bound.
	 */
	@CheckReturnValue
	@Nullable
	private static List<DetailAST> conditionalNewArms(@Nonnull DetailAST value) {
		final var arms = ProvenTypes.conditionalArms(value);
		return arms == null || arms.stream().anyMatch(arm -> arm.getType() != TokenTypes.LITERAL_NEW) ? null : arms;
	}

	@CheckReturnValue
	private static boolean containsTypeParamName(@Nonnull DetailAST typeNode, @Nonnull String tpName) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(typeNode);
		while (!stack.isEmpty()) {
			for (var child = stack.pop().getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.IDENT && tpName.equals(child.getText()))
					return true;
				stack.push(child);
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsWideningTypeArg(@Nonnull DetailAST typeArguments) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(typeArguments);
		while (!stack.isEmpty()) {
			for (var child = stack.pop().getFirstChild(); child != null; child = child.getNextSibling()) {
				// a DOT's children are the name's own segments, so descending would match
				// `com.example.Number` on its trailing `Number`; only its arguments are types
				if (child.getType() == TokenTypes.DOT) {
					final var qualifiedName = AstText.typeName(child);
					if (qualifiedName != null && WIDENING_SUPERTYPE_FQCNS.contains(qualifiedName))
						return true;
					// an earlier segment's own arguments hang off the inner DOT it closed
					for (var segment = child; segment != null && segment.getType() == TokenTypes.DOT;
							segment = segment.getFirstChild()) {
						for (var nested = segment.getFirstChild(); nested != null; nested = nested.getNextSibling()) {
							if (nested.getType() == TokenTypes.TYPE_ARGUMENTS)
								stack.push(nested);
						}
					}
					continue;
				}

				if (child.getType() == TokenTypes.IDENT && WIDENING_SUPERTYPES.contains(child.getText()))
					return true;
				stack.push(child);
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsWildcard(@Nonnull DetailAST typeArguments) {
		// iterative: a pathological nesting depth must not overflow the stack inside a check,
		// where the error would abort the whole run rather than fail one file
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(typeArguments);
		while (!stack.isEmpty()) {
			for (var child = stack.pop().getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.WILDCARD_TYPE)
					return true;
				stack.push(child);
			}
		}
		return false;
	}

	/**
	 * Whether the declared type arguments at {@code (line, column)} (0-based) belong to a
	 * diamond the fixer may fill. They do only when the initializer is itself a {@code new}
	 * (or a conditional whose every arm is one): in {@code new Foo<>().names()} the diamond
	 * belongs to the chain's receiver, whose type parameter is unrelated to the declared type.
	 */
	@CheckReturnValue
	public static boolean declaredArgumentsMoveToDiamondAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return false;
			final var isDeclaration = node.getType() == TokenTypes.VARIABLE_DEF
					|| node.getType() == TokenTypes.RESOURCE;
			if (!isDeclaration)
				continue;
			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				return false;
			final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
			if (value == null)
				return false;
			if (value.getType() == TokenTypes.LITERAL_NEW)
				return true;
			final var conditional = value.getType() == TokenTypes.QUESTION
					|| value.getType() == TokenTypes.LITERAL_SWITCH;
			return conditional && conditionalNewArms(value) != null;
		}
		return false;
	}

	/**
	 * Whether {@code objBlock} or a same-file supertype declares {@code methodName} returning a
	 * type only the assignment target can infer.
	 */
	@CheckReturnValue
	private static boolean declaresTargetTypedMethod(
			@Nonnull DetailAST objBlock,
			@Nonnull String methodName,
			@Nonnull Set<DetailAST> visited
	) {
		final Function<DetailAST, DetailAST> declaring =
				body -> methodsNamed(body, methodName).stream().anyMatch(PreferVarCheck::isTargetTypedMethod) ? body : null;
		return AstResolve.firstInHierarchy(List.of(objBlock), AstResolve::inheritedBodies, visited, declaring) != null;
	}

	@CheckReturnValue
	private static boolean declaresVariableNamed(@Nonnull DetailAST scope, @Nonnull String name) {
		for (var node = scope.getFirstChild(); node != null; node = node.getNextSibling()) {
			if (node.getType() == TokenTypes.VARIABLE_DEF || node.getType() == TokenTypes.PARAMETER_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				if (ident != null && name.equals(ident.getText()))
					return true;
				continue;
			}

			// a method, constructor or typed lambda holds its parameters one level down, and a
			// for-each holds its loop variable there
			if ((node.getType() == TokenTypes.PARAMETERS || node.getType() == TokenTypes.FOR_EACH_CLAUSE)
					&& declaresVariableNamed(node, name))
				return true;

			// an untyped single-parameter lambda names it with a bare IDENT instead
			if (scope.getType() == TokenTypes.LAMBDA && node.getType() == TokenTypes.IDENT
					&& name.equals(node.getText()))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean elementTypeChangesUnderVar(@Nonnull DetailAST loopType, @Nullable DetailAST iterable) {
		// an array is its own type rather than a conversion of its elements, so `int[]` neither
		// boxes nor unboxes the way a bare `int` does
		final var isArray = loopType.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null;
		final var primitive = getPrimitiveTypeName(loopType);
		if (!isArray && (primitive != null || isBoxedType(loopType))) {
			// boxing and unboxing both retype the variable, so the declaration survives only when the
			// iterable is an array of exactly this type. `List<Integer>` iterated as `int` rebinds to
			// Integer, where `==` compares references and arithmetic runs in int space
			final var declaredName = primitive != null ? primitive : simpleTypeName(loopType.getFirstChild());
			return !declaredName.equals(iteratedArrayComponentName(iterable));
		}

		// a reference array still widens: `Object[]` over a `String[]` element rebinds to `String[]`
		final var loopTypeName = simpleTypeName(loopType.getFirstChild());
		return loopTypeName != null && WIDENING_SUPERTYPES.contains(loopTypeName);
	}

	/**
	 * Whether the climb from a reported position to its declaration has to stop at {@code node}, so
	 * the position names no declaration: an {@code ASSIGN} means the position sits inside an
	 * initializer or an assignment, never on a declared type, and climbing on would read the
	 * declaration that initializer belongs to.
	 */
	@CheckReturnValue
	private static boolean endsDeclarationClimb(@Nonnull DetailAST node) {
		return node.getType() == TokenTypes.ASSIGN;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getInitializerMethodCall(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		if (value != null && value.getType() == TokenTypes.METHOD_CALL)
			return value;
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

	/**
	 * Checks whether the initializer is a {@code new XXX<Object[, Object]...>()}
	 * constructor call where all class-level type arguments are bare
	 * {@code Object} or {@code java.lang.Object}. Constructor-level type
	 * arguments ({@code new <T>Foo()}) are ignored.
	 */
	@CheckReturnValue
	private static boolean hasAllObjectTypeArgs(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		if (value == null || value.getType() != TokenTypes.LITERAL_NEW)
			return false;

		final var typeArgs = AstQuery.findNewClassTypeArguments(value);
		if (typeArgs == null)
			return false;

		// `<Object>` is only redundant when the diamond would infer Object on its own: a
		// constructor argument mentioning the type variable pins it instead, so
		// `new ArrayList<>(strings)` infers ArrayList<String> and the arguments are load-bearing
		final var constructorArgs = value.findFirstToken(TokenTypes.ELIST);
		if (constructorArgs == null || constructorArgs.getFirstChild() != null)
			return false;

		var hasTypeArg = false;
		for (var child = typeArgs.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.TYPE_ARGUMENT)
				continue;
			hasTypeArg = true;

			if (child.getChildCount() != 1)
				return false;
			final var firstChild = child.getFirstChild();
			if (firstChild.getType() == TokenTypes.IDENT) {
				if (!"Object".equals(firstChild.getText()))
					return false;
			}
			else if (firstChild.getType() == TokenTypes.DOT) {
				if (!"java.lang.Object".equals(FullIdent.createFullIdent(firstChild).getText()))
					return false;
			}
			else
				return false;
		}
		return hasTypeArg;
	}

	@CheckReturnValue
	private static boolean hasGenericReturnType(@Nonnull DetailAST methodCall) {
		final var methodName = calledMethodName(methodCall);
		if (methodName == null)
			return false;

		// a bare call resolves against every enclosing class and a qualified one names one of them,
		// so walking them all can only over-refuse
		final var visited = new HashSet<DetailAST>();
		for (var scope = methodCall.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.OBJBLOCK && declaresTargetTypedMethod(scope, methodName, visited))
				return true;
		}
		return false;
	}

	/**
	 * Whether a qualifier segment of a qualified type carries its own type arguments, as in
	 * {@code Outer<String>.Inner}, which belong to the enclosing instance rather than to the
	 * declared type.
	 */
	@CheckReturnValue
	private static boolean hasQualifierTypeArguments(@Nonnull DetailAST type) {
		final var first = type.getFirstChild();
		if (first == null || first.getType() != TokenTypes.DOT)
			return false;

		// a qualified name nests left-to-right, so anything deeper than the outermost level's
		// trailing arguments hangs off an inner DOT and belongs to a qualifier
		for (var segment = first; segment != null && segment.getType() == TokenTypes.DOT;
				segment = segment.getFirstChild()) {
			for (var child = segment.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.TYPE_ARGUMENTS
						&& (segment != first || child.getNextSibling() != null))
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

		return firstChild.getType() == TokenTypes.DOT && firstChild.findFirstToken(TokenTypes.TYPE_ARGUMENTS) != null;
	}

	@CheckReturnValue
	@Nullable
	private static String inferredLiteralType(@Nullable DetailAST value) {
		if (value == null)
			return null;
		return switch (value.getType()) {
			case TokenTypes.CHAR_LITERAL -> "char";
			case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
			// the token type does not decide float vs double: an unsuffixed `5.0` is a
			// double in Java whatever checkstyle tags it, so the suffix is the authority
			case TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT ->
					value.getText().endsWith("f") || value.getText().endsWith("F") ? "float" : "double";
			case TokenTypes.NUM_INT -> "int";
			case TokenTypes.NUM_LONG -> "long";
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String initializerCastType(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
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
	private static boolean isBoxedType(@Nonnull DetailAST type) {
		final var name = simpleTypeName(type.getFirstChild());
		return name != null && BOXED_TYPES.contains(name);
	}

	/**
	 * Whether the position reported at {@code (line, column)} (0-based) is a declaration this
	 * check converts: a local, a {@code for}-init, a for-each variable, or a try-with-resources
	 * resource. A field or a bare assignment is not, and {@code var} is illegal in both.
	 * Returns {@code null} when no token resolves there, so a caller can tell "not convertible"
	 * apart from "could not tell".
	 */
	@CheckReturnValue
	@Nullable
	public static Boolean isConvertibleDeclarationAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		if (token == null)
			return null;
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return false;
			if (node.getType() == TokenTypes.RESOURCE)
				return true;
			if (node.getType() == TokenTypes.VARIABLE_DEF) {
				final var parent = node.getParent();
				return parent != null
						&& (parent.getType() == TokenTypes.SLIST
						|| parent.getType() == TokenTypes.FOR_INIT
						|| parent.getType() == TokenTypes.FOR_EACH_CLAUSE);
			}
		}
		return false;
	}

	/**
	 * Whether the declaration reported at {@code (line, column)} (0-based) initializes from an
	 * explicit {@code new T[]{...}} array creation that the {@code {...}} shorthand can replace
	 * ({@link #takesArrayShorthand}). The proof is re-read with the file's own imports.
	 */
	@CheckReturnValue
	public static boolean isExplicitArrayInitAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return false;
			if (node.getType() != TokenTypes.VARIABLE_DEF)
				continue;

			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			final var type = node.findFirstToken(TokenTypes.TYPE);
			if (assign == null || type == null)
				return false;

			final var check = new PreferVarCheck();
			check.beginTree(root);
			return check.takesArrayShorthand(node, type, assign);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isInitializerExplicitArrayInit(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		return value != null
				&& value.getType() == TokenTypes.LITERAL_NEW
				&& value.findFirstToken(TokenTypes.ARRAY_INIT) != null;
	}

	@CheckReturnValue
	private static boolean isInitializerLambdaOrMethodRef(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		return value != null
				&& (value.getType() == TokenTypes.LAMBDA || value.getType() == TokenTypes.METHOD_REF);
	}

	@CheckReturnValue
	private static boolean isInitializerNull(@Nonnull DetailAST assign) {
		var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null && value.getType() == TokenTypes.TYPECAST) {
			final var rparen = value.findFirstToken(TokenTypes.RPAREN);
			value = AstQuery.unwrapParensAndExpr(rparen != null ? rparen.getNextSibling() : null);
		}
		return value != null && value.getType() == TokenTypes.LITERAL_NULL;
	}

	/**
	 * Checks whether the initializer is an anonymous class with exactly
	 * one method and no extra members (fields, inner types, etc.).
	 */
	@CheckReturnValue
	private static boolean isInitializerSimpleAnonymousClass(@Nonnull DetailAST assign) {
		final var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
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
		return parent != null
				&& (parent.getType() == TokenTypes.SLIST || parent.getType() == TokenTypes.FOR_INIT);
	}

	@CheckReturnValue
	static boolean isMultiVarDeclaration(@Nonnull DetailAST varDef) {
		for (var sibling = varDef.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.COMMA || sibling.getType() == TokenTypes.VARIABLE_DEF)
				return true;
			if (sibling.getType() == TokenTypes.SEMI)
				break;
		}
		for (var sibling = varDef.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
			if (sibling.getType() == TokenTypes.COMMA || sibling.getType() == TokenTypes.VARIABLE_DEF)
				return true;
			if (sibling.getType() == TokenTypes.SEMI)
				break;
		}
		return false;
	}

	/**
	 * Whether the declaration reported at {@code (line, column)} (0-based) shares
	 * its declaration with another variable. Returns {@code null} when no declaration
	 * resolves there, so a caller can tell "not multi-var" apart from "could not tell".
	 */
	@CheckReturnValue
	@Nullable
	public static Boolean isMultiVarDeclarationAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		if (token == null)
			return null;
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return null;
			// a resource is ';'-separated and can never share a declarator list, so this is a
			// definite no rather than "could not tell"
			if (node.getType() == TokenTypes.RESOURCE)
				return Boolean.FALSE;
			if (node.getType() == TokenTypes.VARIABLE_DEF)
				return isMultiVarDeclaration(node);
		}
		return null;
	}

	@CheckReturnValue
	private static boolean isMultiVarFirst(@Nonnull DetailAST varDef) {
		var hasNext = false;
		for (var sibling = varDef.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.COMMA || sibling.getType() == TokenTypes.VARIABLE_DEF) {
				hasNext = true;
				break;
			}
			if (sibling.getType() == TokenTypes.SEMI)
				break;
		}
		if (!hasNext)
			return false;
		for (var sibling = varDef.getPreviousSibling(); sibling != null; sibling = sibling.getPreviousSibling()) {
			if (sibling.getType() == TokenTypes.COMMA || sibling.getType() == TokenTypes.VARIABLE_DEF)
				return false;
			if (sibling.getType() == TokenTypes.SEMI)
				break;
		}
		return true;
	}

	/** Whether {@code type} declares a reference type, a primitive array included. */
	@CheckReturnValue
	private static boolean isReferenceType(@Nonnull DetailAST type) {
		return getPrimitiveTypeName(type) == null || type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null;
	}

	/**
	 * Whether {@code methodDef}'s return type mentions a type variable it declares that none of its
	 * parameters mentions, which only the assignment target can infer.
	 */
	@CheckReturnValue
	private static boolean isTargetTypedMethod(@Nonnull DetailAST methodDef) {
		final var typeParams = methodDef.findFirstToken(TokenTypes.TYPE_PARAMETERS);
		final var returnType = methodDef.findFirstToken(TokenTypes.TYPE);
		if (typeParams == null || returnType == null)
			return false;

		for (var tp = typeParams.getFirstChild(); tp != null; tp = tp.getNextSibling()) {
			if (tp.getType() != TokenTypes.TYPE_PARAMETER)
				continue;

			final var tpIdent = tp.findFirstToken(TokenTypes.IDENT);
			if (tpIdent == null || !containsTypeParamName(returnType, tpIdent.getText()))
				continue;

			final var params = methodDef.findFirstToken(TokenTypes.PARAMETERS);
			if (params == null)
				return true;

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
		return false;
	}

	/**
	 * The component type of the array {@code iterable} names, or null when it does not resolve to
	 * an array declared in this file.
	 */
	@CheckReturnValue
	@Nullable
	private static String iteratedArrayComponentName(@Nullable DetailAST iterable) {
		final var value = AstQuery.unwrapParensAndExpr(iterable);
		if (value == null || value.getType() != TokenTypes.IDENT)
			return null;

		final var declared = AstResolve.resolveVariableType(value, value.getText());
		return declared != null && declared.endsWith("[]")
				? declared.substring(0, declared.length() - 2)
				: null;
	}

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
	 * Whether the call's receiver names its own type outright: a string or text block, a
	 * {@code new}, a parenthesised cast, or {@code this}/{@code super}. A chain is followed to its
	 * base, since the base is what has to be resolvable for the whole chain to be.
	 */
	@CheckReturnValue
	private static boolean literalReceiverResolvable(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return false;

		var receiver = dot.getFirstChild();
		while (receiver != null && receiver.getType() == TokenTypes.METHOD_CALL) {
			final var innerDot = receiver.getFirstChild();
			receiver = innerDot == null || innerDot.getType() != TokenTypes.DOT ? null : innerDot.getFirstChild();
		}
		if (receiver == null)
			return false;

		if (receiver.getType() == TokenTypes.LPAREN) {
			final var cast = receiver.getNextSibling();
			return cast != null && cast.getType() == TokenTypes.TYPECAST;
		}

		return switch (receiver.getType()) {
			case TokenTypes.LITERAL_NEW, TokenTypes.LITERAL_SUPER, TokenTypes.LITERAL_THIS,
			     TokenTypes.STRING_LITERAL, TokenTypes.TEXT_BLOCK_LITERAL_BEGIN -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> methodsNamed(@Nonnull DetailAST objBlock, @Nonnull String methodName) {
		final var methods = new ArrayList<DetailAST>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var ident = child.getType() == TokenTypes.METHOD_DEF ? child.findFirstToken(TokenTypes.IDENT) : null;
			if (ident != null && methodName.equals(ident.getText()))
				methods.add(child);
		}
		return methods;
	}

	@CheckReturnValue
	@Nullable
	private static String methodTypeVariableErasure(@Nonnull DetailAST defNode, @Nonnull String name) {
		final var typeParameters = defNode.findFirstToken(TokenTypes.TYPE_PARAMETERS);
		return typeParameters == null ? null : typeVariableErasure(typeParameters, name, new HashSet<>());
	}

	/**
	 * Whether {@code receiver}, the receiver of {@code methodCall}, names a same-file type whose body
	 * encloses the call, so the call reaches the declarations a bare call would. A variable of that
	 * name obscures the type (JLS 6.4.2), so a declared one is not taken for it.
	 */
	@CheckReturnValue
	private static boolean namesEnclosingType(@Nonnull DetailAST methodCall, @Nonnull DetailAST receiver) {
		if (!AstQuery.isPureDotChainOrIdent(receiver) || receiverIsDeclaredVariable(methodCall))
			return false;

		final var name = receiver.getType() == TokenTypes.IDENT ? receiver.getText() : AstText.dottedName(receiver);
		final var classDef = AstResolve.sameFileClassDef(methodCall, name);
		final var body = classDef == null ? null : classDef.findFirstToken(TokenTypes.OBJBLOCK);
		for (var node = methodCall.getParent(); body != null && node != null; node = node.getParent()) {
			if (node == body)
				return true;
		}
		return false;
	}

	/**
	 * How {@code var} inference meets a primitive-typed declaration ({@link #primitiveVarAction}),
	 * or null when {@code type} is not primitive. A primitive array is a reference type: no other
	 * array type converts to it, so {@code var} keeps it whatever initializer is legal there.
	 */
	@CheckReturnValue
	@Nullable
	private static PrimitiveVarAction primitiveAction(@Nonnull DetailAST type, @Nonnull DetailAST assign) {
		final var declaredType = getPrimitiveTypeName(type);
		if (declaredType == null || type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null)
			return null;

		final var castType = initializerCastType(assign);
		if (declaredType.equals(castType))
			return PrimitiveVarAction.ERROR;

		final var value = unwrapInitializerValue(assign);
		if (value != null && BOOLEAN_OPERATORS.contains(value.getType()) && "boolean".equals(declaredType))
			return PrimitiveVarAction.ERROR;
		final var inferredType = inferredLiteralType(value);

		if (inferredType == null) {
			final var parseType = knownParseReturnType(assign);
			if (declaredType.equals(parseType))
				return PrimitiveVarAction.ERROR;
			return PrimitiveVarAction.WARN;
		}

		if (inferredType.equals(declaredType))
			return PrimitiveVarAction.ERROR;

		return PrimitiveVarAction.SKIP;
	}

	/**
	 * Whether the declaration reported at {@code (line, column)} (0-based) has a primitive type
	 * its initializer no longer pins, so {@code var} would retype it ({@code long y = x}).
	 */
	@CheckReturnValue
	public static boolean primitiveTypeUnpinnedAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return false;
			if (node.getType() == TokenTypes.VARIABLE_DEF) {
				final var assign = node.findFirstToken(TokenTypes.ASSIGN);
				if (assign == null)
					return false;
				final var action = primitiveAction(node.findFirstToken(TokenTypes.TYPE), assign);
				return action != null && action != PrimitiveVarAction.ERROR;
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean producesDeclaredBox(@Nullable String declaredName, @Nonnull DetailAST value, boolean isNew) {
		if (declaredName == null)
			return false;
		if (isNew)
			return declaredName.equals(simpleTypeName(value.getFirstChild()));
		if (value.getType() != TokenTypes.METHOD_CALL)
			return false;
		final var dot = value.getFirstChild();
		return dot != null && dot.getType() == TokenTypes.DOT
				&& "valueOf".equals(AstQuery.getMethodName(value))
				&& declaredName.equals(simpleTypeName(dot.getFirstChild()));
	}

	/**
	 * The receiver of a fully-qualified static call ({@code java.util.Collections.emptyList()}),
	 * which names its class outright, or null when the receiver is not a plain qualified name.
	 */
	@CheckReturnValue
	@Nullable
	private static String qualifiedReceiverName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return null;

		final var receiver = dot.getFirstChild();
		return receiver != null && receiver.getType() == TokenTypes.DOT && AstQuery.isPureDotChainOrIdent(receiver)
				? AstText.dottedName(receiver)
				: null;
	}

	@CheckReturnValue
	private static boolean receiverIsDeclaredVariable(@Nonnull DetailAST methodCall) {
		final var name = receiverVariableName(methodCall);
		if (name == null)
			return false;

		for (var scope = methodCall.getParent(); scope != null; scope = scope.getParent()) {
			if (declaresVariableNamed(scope, name))
				return true;
		}
		return false;
	}

	/**
	 * The name the receiver of {@code methodCall} reads as a variable, or null when it is not a bare
	 * name. An indexed receiver answers with its array's name.
	 */
	@CheckReturnValue
	@Nullable
	private static String receiverVariableName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return null;

		var receiver = dot.getFirstChild();
		if (receiver != null && receiver.getType() == TokenTypes.INDEX_OP)
			receiver = receiver.getFirstChild();
		return receiver == null || receiver.getType() != TokenTypes.IDENT ? null : receiver.getText();
	}

	/**
	 * Whether the reference-typed declaration reported at {@code (line, column)} (0-based) has an
	 * initializer that no longer proves the declared type ({@link #bindsDeclaredType}), so
	 * {@code var} could retype it ({@code final List raw = typed}). The proof is re-read with the
	 * file's own imports.
	 */
	@CheckReturnValue
	public static boolean referenceTypeUnprovenAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstQuery.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			if (endsDeclarationClimb(node))
				return false;
			if (node.getType() != TokenTypes.VARIABLE_DEF && node.getType() != TokenTypes.RESOURCE)
				continue;

			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			final var type = node.findFirstToken(TokenTypes.TYPE);
			if (assign == null || type == null || !isReferenceType(type))
				return false;

			final var check = new PreferVarCheck();
			check.beginTree(root);
			return !check.isVarType(node) && !check.bindsDeclaredType(type, assign);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean sameFileDeclaresMethod(@Nonnull DetailAST from, @Nonnull String methodName) {
		final var visited = new HashSet<DetailAST>();
		for (var scope = from.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.OBJBLOCK && bodyDeclaresMethod(scope, methodName, visited))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static String simpleTypeName(@Nullable DetailAST nameNode) {
		final var name = AstText.typeName(nameNode);
		return name == null ? null : name.substring(name.lastIndexOf('.') + 1);
	}

	/**
	 * Returns the type's leftmost leaf token. For a qualified type
	 * ({@code java.util.List}) the {@code TYPE} node's own column falls on an
	 * inner {@code .} rather than the type's first token, so reporting it
	 * directly would point the fixer (and any IDE squiggle) into the middle of
	 * the name.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST typeStartToken(@Nonnull DetailAST type) {
		var node = type;
		while (node.getFirstChild() != null)
			node = node.getFirstChild();
		return node;
	}

	/**
	 * The erasure of the type variable {@code name} among {@code typeParameters} (JLS 4.6): its first
	 * bound as written, followed through a bound that is another of them, or {@code Object} when it
	 * has none. Null when none of them is named {@code name}. Bounds that loop back, which do not
	 * compile, stop at the type variable they loop back to.
	 */
	@CheckReturnValue
	@Nullable
	private static String typeVariableErasure(@Nonnull DetailAST typeParameters, @Nonnull String name, @Nonnull Set<String> visited) {
		if (!visited.add(name))
			return null;

		for (var parameter = typeParameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
			final var ident = parameter.getType() == TokenTypes.TYPE_PARAMETER ? parameter.findFirstToken(TokenTypes.IDENT) : null;
			if (ident == null || !name.equals(ident.getText()))
				continue;

			final var bounds = parameter.findFirstToken(TokenTypes.TYPE_UPPER_BOUNDS);
			var bound = bounds == null ? null : bounds.getFirstChild();
			while (bound != null && bound.getType() != TokenTypes.IDENT && bound.getType() != TokenTypes.DOT)
				bound = bound.getNextSibling();
			final var boundName = AstText.typeName(bound);
			if (boundName == null)
				return OBJECT_FQCN;

			final var deeper = typeVariableErasure(typeParameters, boundName, visited);
			return deeper == null ? boundName : deeper;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapInitializerValue(@Nonnull DetailAST assign) {
		var value = AstQuery.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null
				&& (value.getType() == TokenTypes.UNARY_MINUS || value.getType() == TokenTypes.UNARY_PLUS))
			value = AstQuery.unwrapParensAndExpr(value.getFirstChild());
		return value;
	}

	private final Map<String, String> staticImportOwners = new HashMap<>();

	private Set<String> allowedMethods = Set.of();
	@Nonnull
	private ProvenTypes types = new ProvenTypes(this::resolve, null);

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		staticImportOwners.clear();
		types = new ProvenTypes(this::resolve, packageName());
	}

	/**
	 * Whether {@code var} binds exactly the reference type {@code type} declares to the initializer
	 * of {@code assign}. Only the syntax tree is evidence ({@link ProvenTypes}), so a final declared
	 * class takes the same proof as any other.
	 */
	@CheckReturnValue
	private boolean bindsDeclaredType(@Nonnull DetailAST type, @Nonnull DetailAST assign) {
		final var value = unwrapInitializerValue(assign);
		return value != null && provesDeclaredType(type, types.typeKey(type), value);
	}

	/**
	 * Whether the class declaring the called method can be determined here. When it cannot, the
	 * method may be target-typed, and {@code var} would silently rebind the variable to whatever
	 * the call infers on its own.
	 */
	@CheckReturnValue
	private boolean callTargetResolvable(@Nonnull DetailAST methodCall) {
		final var methodName = AstQuery.getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var first = methodCall.getFirstChild();
		final var bare = first != null && first.getType() == TokenTypes.IDENT;
		if (bare && staticImportOwners.containsKey(methodName)
				|| calledMethodName(methodCall) != null && sameFileDeclaresMethod(methodCall, methodName))
			return true;

		// an import names a class without proving the classpath holds it, and a class that cannot be
		// read may declare the method target-typed, the same as a qualified name nothing loads
		final var receiver = receiverTypeName(methodCall);
		final var receiverFqcn = receiver == null ? null : resolve(receiver);
		if (receiverFqcn != null && resolvesToType(methodCall, receiverFqcn))
			return true;

		final var qualified = qualifiedReceiverName(methodCall);
		if (qualified != null)
			return ReflectionUtil.isResolvableClass(qualified);

		return literalReceiverResolvable(methodCall) || receiverVariableTypeResolvable(methodCall);
	}

	/**
	 * Whether {@code literalNew} creates exactly the type {@code type} declares, whose key is
	 * {@code declared}: the same class written the same way or resolving to the same one, with the
	 * same type arguments, or with a diamond that receives the declared ones. A raw declaration's
	 * diamond infers arguments the declaration does not have.
	 */
	@CheckReturnValue
	private boolean constructsDeclaredType(@Nonnull DetailAST type, @Nullable String declared, @Nonnull DetailAST literalNew) {
		final var created = types.newKey(literalNew, false);
		final var createdSpelling = types.newKey(literalNew, true);
		final var declaredSpelling = types.spelling(type);
		if (createdSpelling == null || declaredSpelling == null)
			return false;

		final var typeArguments = AstQuery.findNewClassTypeArguments(literalNew);
		if (typeArguments == null || typeArguments.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null) {
			return ProvenTypes.isExact(createdSpelling) && createdSpelling.equals(declaredSpelling)
					|| ProvenTypes.isExact(created) && created.equals(declared);
		}

		if (ProvenTypes.arguments(declaredSpelling) == null)
			return false;
		return ProvenTypes.erasure(createdSpelling).equals(ProvenTypes.erasure(declaredSpelling))
				|| created != null && declared != null && ProvenTypes.isExact(ProvenTypes.erasure(declared))
				&& ProvenTypes.erasure(created).equals(ProvenTypes.erasure(declared));
	}

	/**
	 * Checkstyle reuses one check instance across files, so {@code staticImportOwners} left populated
	 * would resolve the next file's bare calls against this one's static imports, and {@code types}
	 * would pin the finished file's whole AST through the declarations it has keyed.
	 */
	@Override
	protected void finishFile(@Nullable DetailAST rootAST) {
		staticImportOwners.clear();
		types = new ProvenTypes(this::resolve, null);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.FOR_EACH_CLAUSE,
				TokenTypes.RESOURCE,
				TokenTypes.STATIC_IMPORT,
				TokenTypes.VARIABLE_DEF
		};
	}

	@CheckReturnValue
	private boolean hasReflectionGenericReturnType(@Nonnull DetailAST methodCall) {
		final var methodName = AstQuery.getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var receiverTypeName = receiverTypeName(methodCall);
		var fqcn = receiverTypeName == null
				? qualifiedReceiverName(methodCall)
				: resolve(receiverTypeName);
		if (fqcn == null)
			fqcn = staticImportOwners.get(methodName);
		final var args = methodCall.findFirstToken(TokenTypes.ELIST);
		final var argCount = args == null ? 0 : AstQuery.countArguments(args);
		return fqcn != null && ReflectionUtil.hasGenericReturnType(fqcn, methodName, argCount);
	}

	/**
	 * Whether {@code methodCall} may infer its return type from the declaration it initializes, so
	 * {@code var} would bind whatever the call infers on its own instead.
	 */
	@CheckReturnValue
	private boolean isTargetTypedCall(@Nonnull DetailAST methodCall) {
		final var methodName = AstQuery.getMethodName(methodCall);
		return (methodName != null && allowedMethods.contains(methodName))
				|| hasGenericReturnType(methodCall)
				|| hasReflectionGenericReturnType(methodCall);
	}

	@CheckReturnValue
	private boolean isVarType(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null && "var".equals(ident.getText());
	}

	/**
	 * Checks how a primitive-typed local variable interacts with
	 * {@code var} inference. Returns:
	 * <ul>
	 *   <li>{@code SKIP}: the literal's own type differs from the declared one, so {@code var}
	 *       would retype the variable ({@code long x = 1}, {@code double d = 5f})</li>
	 *   <li>{@code WARN}: a non-literal initializer whose type cannot be verified here, unless a
	 *       known parse method already pins it ({@code int i = Integer.parseInt(s)} is
	 *       {@code ERROR})</li>
	 *   <li>{@code ERROR}: safe to flag (the initializer's own type is the declared type)</li>
	 * </ul>
	 * For non-primitive types, returns {@code SKIP} when {@code var} would bind a
	 * different type than the declaration states, and {@code ERROR} otherwise.
	 */
	@CheckReturnValue
	@Nonnull
	private PrimitiveVarAction primitiveVarAction(@Nonnull DetailAST varDef, @Nonnull DetailAST assign) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return PrimitiveVarAction.ERROR;

		final var primitive = primitiveAction(type, assign);
		if (primitive != null)
			return primitive;
		return referenceVarChangesType(type, assign) ? PrimitiveVarAction.SKIP : PrimitiveVarAction.ERROR;
	}

	/**
	 * Whether {@code value}, a declaration's initializer, is proven to have exactly the type
	 * {@code type} declares, whose key is {@code declared} ({@link #bindsDeclaredType}). A cast names
	 * its type where the declaration does, so the same spelling there is the same type even when
	 * nothing resolves it.
	 */
	@CheckReturnValue
	private boolean provesDeclaredType(@Nonnull DetailAST type, @Nullable String declared, @Nonnull DetailAST value) {
		return switch (value.getType()) {
			case TokenTypes.LITERAL_NEW -> constructsDeclaredType(type, declared, value);
			case TokenTypes.LITERAL_SWITCH, TokenTypes.QUESTION -> {
				final var arms = ProvenTypes.conditionalArms(value);
				if (arms == null)
					yield false;

				var proven = true;
				for (var arm : arms) {
					final var unwrapped = AstQuery.unwrapParensAndExpr(arm);
					proven &= unwrapped != null && provesDeclaredType(type, declared, unwrapped);
				}
				yield proven;
			}
			case TokenTypes.TYPECAST -> {
				if (ProvenTypes.isIntersectionCast(value))
					yield false;

				final var cast = value.findFirstToken(TokenTypes.TYPE);
				final var castSpelling = cast == null ? null : types.spelling(cast);
				final var castKey = cast == null ? null : types.typeKey(cast);
				yield ProvenTypes.isExact(castSpelling) && castSpelling.equals(types.spelling(type))
						|| ProvenTypes.isExact(castKey) && castKey.equals(declared);
			}
			default -> {
				final var actual = types.keyOf(value);
				yield ProvenTypes.isExact(actual) && actual.equals(declared);
			}
		};
	}

	/**
	 * Whether the receiver ({@link #receiverVariableName}) names a variable whose type is declared in
	 * this file or held by the classpath. A type variable's members are its erasure's.
	 */
	@CheckReturnValue
	private boolean receiverVariableTypeResolvable(@Nonnull DetailAST methodCall) {
		final var name = receiverVariableName(methodCall);
		final var typeName = name == null ? null : AstResolve.resolveVariableType(methodCall, name);
		if (typeName == null)
			return false;

		// a primitive array's members are the ones every array has
		final var element = typeName.replace("[]", "");
		if (element.length() < typeName.length() && PRIMITIVE_TYPES.contains(element))
			return true;

		for (var scope = methodCall.getParent(); scope != null; scope = scope.getParent()) {
			final var erasure = methodTypeVariableErasure(scope, element);
			if (erasure != null)
				return resolvesToType(methodCall, erasure);
		}
		return resolvesToType(methodCall, typeName);
	}

	@CheckReturnValue
	private boolean referenceVarChangesType(@Nonnull DetailAST type, @Nonnull DetailAST assign) {
		final var value = unwrapInitializerValue(assign);
		if (value == null)
			return false;

		if (hasQualifierTypeArguments(type))
			return true;

		final var declaredArgs = AstQuery.declaredTypeArguments(type);
		// `List<?> w = ...`: var binds the inferred arguments, so a wildcard is lost
		if (declaredArgs != null && containsWildcard(declaredArgs))
			return true;

		final var isNew = value.getType() == TokenTypes.LITERAL_NEW;
		final var declaredName = simpleTypeName(type.getFirstChild());

		// a boxed declaration unboxes under `var` unless the initializer is itself the box:
		// `Byte b = 0` and `Integer i = Integer.parseInt(s)` both bind a primitive
		if (isBoxedType(type))
			return !producesDeclaredBox(declaredName, value, isNew);

		// `Object o = "x"`: var binds the value's own type, so only constructing the declared
		// class preserves it. `Object[]` is its own type, not a widening, so arrays are exempt
		if (declaredName != null && WIDENING_SUPERTYPES.contains(declaredName)
				&& type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null)
			return !(isNew && declaredName.equals(simpleTypeName(value.getFirstChild())));

		// `Map<String, Object> m = Map.of(...)`: the arguments are inferred from the call and
		// the deliberate widening is lost. A `new` is exempt because its diamond receives them
		if (declaredArgs != null && !isNew && containsWideningTypeArg(declaredArgs))
			return true;

		// under `var` a conditional or switch initializer is standalone, so its arms no longer
		// share the declaration as a target type and each infers on its own
		if (declaredArgs != null && !isNew
				&& (value.getType() == TokenTypes.QUESTION || value.getType() == TokenTypes.LITERAL_SWITCH)
				&& conditionalNewArms(value) == null)
			return true;

		if (!isNew)
			return false;

		// an anonymous class body var cannot name: only the single-method form is deferred
		// to PreferLambdaCheck, so a multi-member body would bind the non-denotable type
		if (value.findFirstToken(TokenTypes.OBJBLOCK) != null)
			return true;

		// an array of another element type is left alone, as `Object[] a = new String[0]` is; any
		// other class constructed into a supertype is a narrowing, which bindsDeclaredType tells
		final var createdName = simpleTypeName(value.getFirstChild());
		return value.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null
				&& declaredName != null && createdName != null && !declaredName.equals(createdName);
	}

	/**
	 * Whether the element type of {@code name} names a type from {@code scope} that this file
	 * declares or the classpath holds.
	 */
	@CheckReturnValue
	private boolean resolvesToType(@Nonnull DetailAST scope, @Nonnull String name) {
		var element = name;
		while (element.endsWith("[]"))
			element = element.substring(0, element.length() - 2);
		final var typeArguments = element.indexOf('<');
		final var erased = typeArguments < 0 ? element : element.substring(0, typeArguments);
		if (erased.indexOf('[') >= 0)
			return false;

		final var qualified = erased.indexOf('.') >= 0;
		final var classDef = qualified ? AstResolve.sameFileClassDef(scope, erased) : null;
		final var body = qualified
				? (classDef == null ? null : classDef.findFirstToken(TokenTypes.OBJBLOCK))
				: AstResolve.sameFileTypeBody(scope, erased);
		if (body != null)
			return true;

		final var fqcn = qualified ? erased : resolve(erased);
		return fqcn != null && ReflectionUtil.isResolvableClass(fqcn);
	}

	/**
	 * Sets method names whose generic return type is inferred from the
	 * left-hand side, so an explicit type is preferred over {@code var}.
	 * For example, {@code findViewById} returns {@code <T extends View> T}.
	 * <p>Called by Checkstyle via reflection when {@code allowedMethods} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setAllowedMethods(@Nonnull String... methods) {
		allowedMethods = Set.copyOf(List.of(methods));
	}

	/**
	 * Whether the {@code {...}} shorthand can replace the initializer of {@code varDef}: an explicit
	 * {@code new T[]{...}} that creates exactly the array type the declaration names, or any under
	 * {@code var}. An array initializer creates an array of the declared type (JLS 10.6), so
	 * {@code Object[] a = new String[]{"a"}} would quietly become an {@code Object[]} and
	 * {@code Object[] a = new int[][]{{1}}} would not compile.
	 */
	@CheckReturnValue
	private boolean takesArrayShorthand(@Nonnull DetailAST varDef, @Nonnull DetailAST type, @Nonnull DetailAST assign) {
		return isInitializerExplicitArrayInit(assign) && (isVarType(varDef) || bindsDeclaredType(type, assign));
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> {
				final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
				if (varDef == null || isVarType(varDef))
					return;

				final var loopType = varDef.findFirstToken(TokenTypes.TYPE);
				final var iterable = ast.findFirstToken(TokenTypes.EXPR);
				if (loopType == null || elementTypeChangesUnderVar(loopType, iterable))
					return;

				// `var` takes the element type, so only an element proven to be the declared type keeps it
				final var element = iterable == null ? null : types.iteratedKey(iterable);
				if (ProvenTypes.isExact(element) && element.equals(types.typeKey(loopType)))
					log(typeStartToken(loopType), MSG_FOREACH);
				else
					logWarning(typeStartToken(loopType), MSG_FOREACH_WARNING);
			}
			case TokenTypes.RESOURCE -> {
				// Java 9+ "try (existingVar) {}" reference form has no TYPE child;
				// it can't use var either way.
				final var typeNode = ast.findFirstToken(TokenTypes.TYPE);
				if (typeNode == null || isVarType(ast))
					return;

				final var typeStart = typeStartToken(typeNode);
				final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
				if (assign == null) {
					log(typeStart, MSG_TRY);
					return;
				}
				if (isInitializerNull(assign) || referenceVarChangesType(typeNode, assign))
					return;

				final var methodCall = getInitializerMethodCall(assign);
				if (methodCall != null && isTargetTypedCall(methodCall)) {
					if (hasTypeArguments(methodCall))
						logWarning(typeStart, MSG_TYPE_ARGS, AstQuery.getMethodName(methodCall));
					return;
				}
				if (methodCall != null && !callTargetResolvable(methodCall))
					return;

				if (bindsDeclaredType(typeNode, assign))
					log(typeStart, MSG_TRY);
				else
					logWarning(typeStart, MSG_TRY_WARNING);
			}
			case TokenTypes.STATIC_IMPORT -> {
				// the node leads with the `static` keyword, so the qualified name is the sibling
				// before the semicolon rather than the first child
				final var last = ast.getLastChild();
				final var qualified = last == null ? null : last.getPreviousSibling();
				final var imported = qualified == null ? "" : FullIdent.createFullIdent(qualified).getText();
				final var lastDot = imported.lastIndexOf('.');
				if (lastDot > 0)
					staticImportOwners.put(imported.substring(lastDot + 1), imported.substring(0, lastDot));
			}
			case TokenTypes.VARIABLE_DEF -> {
				if (!isLocalVariable(ast))
					return;

				final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
				if (assign == null)
					return;

				final var type = ast.findFirstToken(TokenTypes.TYPE);
				if (type == null)
					return;

				if (isVarType(ast) && hasAllObjectTypeArgs(assign)) {
					log(type, MSG_DIAMOND);
					return;
				}

				if (isMultiVarDeclaration(ast)) {
					if (!isVarType(ast) && isMultiVarFirst(ast))
						logWarning(typeStartToken(type), MSG_LOCAL_WARNING);
					return;
				}

				if (isInitializerNull(assign))
					return;

				final var initValue = assign.getFirstChild();
				if (initValue != null && initValue.getType() == TokenTypes.ARRAY_INIT)
					return;

				if (takesArrayShorthand(ast, type, assign)) {
					log(typeStartToken(type), MSG_VAR_EXPLICIT_ARRAY);
					return;
				}

				if (isInitializerLambdaOrMethodRef(assign))
					return;

				if (isInitializerSimpleAnonymousClass(assign))
					return;

				final var primAction = primitiveVarAction(ast, assign);
				if (primAction == PrimitiveVarAction.SKIP)
					return;

				final var methodCall = getInitializerMethodCall(assign);
				final var methodName = methodCall == null ? null : AstQuery.getMethodName(methodCall);
				final var isGeneric = methodCall != null && isTargetTypedCall(methodCall);

				if (isGeneric && hasTypeArguments(methodCall)) {
					logWarning(typeStartToken(type), MSG_TYPE_ARGS, methodName);
					return;
				}

				if (isVarType(ast)) {
					if (isGeneric)
						logWarning(type, MSG_VAR_GENERIC, methodName);
					return;
				}

				if (methodCall != null && !callTargetResolvable(methodCall))
					return;

				if (isGeneric)
					return;

				if (primAction == PrimitiveVarAction.WARN)
					logWarning(type, MSG_LOCAL_WARNING);
				// a reference type is only proven when `var` binds exactly the declared one: a narrower
				// one rebinds every use of the variable, which nothing here can see
				else if (isReferenceType(type) && !bindsDeclaredType(type, assign))
					logWarning(typeStartToken(type), MSG_LOCAL_WARNING);
				else {
					final var typeStart = typeStartToken(type);
					log(typeStart.getLineNo(), typeStart.getColumnNo(), MSG_LOCAL);
				}
			}
		}
	}
}