package com.etk2000.checkstyle;

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

	private static final Set<String> BOXED_TYPES = Set.of(
			"Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short"
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
	private static final String MSG_LOCAL = "prefer.var.local";
	private static final String MSG_TRY = "prefer.var.try.resource";
	private static final String MSG_TYPE_ARGS = "prefer.var.type.args";
	private static final String MSG_VAR_EXPLICIT_ARRAY = "prefer.var.explicit.array";
	private static final String MSG_VAR_GENERIC = "prefer.var.generic.return";
	private static final String NO_NAMEABLE_TYPE = "";

	private static void addParameterTypeAt(@Nonnull DetailAST defNode, int arity, int index, @Nonnull List<String> found) {
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;

		var declared = 0;
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() == TokenTypes.PARAMETER_DEF)
				++declared;
		}
		if (declared != arity)
			return;

		var position = 0;
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;
			if (position++ != index)
				continue;

			final var paramType = param.findFirstToken(TokenTypes.TYPE);
			if (paramType != null)
				found.add(AstUtil.canonicalType(paramType));
		}
	}

	@CheckReturnValue
	private static boolean bodyDeclaresMethod(
			@Nonnull DetailAST objBlock,
			@Nonnull String methodName,
			@Nonnull Set<DetailAST> visited
	) {
		if (!visited.add(objBlock))
			return false;

		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null && methodName.equals(ident.getText()))
				return true;
		}
		for (var body : AstUtil.supertypeBodies(objBlock)) {
			if (bodyDeclaresMethod(body, methodName, visited))
				return true;
		}
		return false;
	}

	/**
	 * The name of the method a bare or {@code this}/{@code super}-qualified call invokes, or null
	 * when the receiver is something else. A foreign receiver's overloads are not in this file, so
	 * matching a same-named method here would veto a conversion made against an unrelated class.
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
		return receiver != null
				&& (receiver.getType() == TokenTypes.LITERAL_THIS || receiver.getType() == TokenTypes.LITERAL_SUPER)
				? AstUtil.getMethodName(methodCall)
				: null;
	}

	private static void collectParameterTypes(
			@Nonnull DetailAST objBlock,
			@Nonnull String methodName,
			int arity,
			int index,
			@Nonnull Set<DetailAST> visited,
			@Nonnull List<String> found
	) {
		if (!visited.add(objBlock))
			return;

		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null && methodName.equals(ident.getText()))
				addParameterTypeAt(child, arity, index, found);
		}
		for (var body : AstUtil.supertypeBodies(objBlock))
			collectParameterTypes(body, methodName, arity, index, visited, found);
	}

	/**
	 * Adds {@code scope}'s uses to {@code uses}, ignoring names in {@code shadowed}. A nested body's
	 * own members shadow the enclosing local, but a name it does not declare is the captured local
	 * itself, so the body is descended into rather than skipped whole.
	 */
	private static void collectUses(
			@Nonnull DetailAST scope,
			@Nonnull Set<String> shadowed,
			@Nonnull Map<String, List<DetailAST>> uses
	) {
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(scope);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (node != scope && node.getType() == TokenTypes.OBJBLOCK) {
				collectUses(node, union(shadowed, declaredMemberNames(node)), uses);
				continue;
			}

			// a nested body's parameters shadow an enclosing local for that whole body, so an
			// identifier inside it names the parameter rather than the declaration being weighed.
			// Only parameters: a nested local's scope starts at its own declarator, so treating one
			// as shadowing would hide a genuine use written above it
			if (node != scope
					&& (node.getType() == TokenTypes.METHOD_DEF || node.getType() == TokenTypes.CTOR_DEF)) {
				collectUses(node, union(shadowed, AstUtil.collectParameterNames(node)), uses);
				continue;
			}

			if (node.getType() == TokenTypes.IDENT && !shadowed.contains(node.getText()))
				uses.computeIfAbsent(node.getText(), name -> new ArrayList<>()).add(node);

			// pushed in reverse so siblings pop in document order, which is what lets a caller take
			// the uses after a declarator by position in the list
			final var children = new ArrayList<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				children.add(child);
			for (var i = children.size() - 1; i >= 0; --i)
				pending.push(children.get(i));
		}
	}

	/**
	 * Every arm of a conditional or switch-expression initializer, or {@code null} when any arm
	 * is not a {@code new}. Only a diamond can receive the declared type arguments; any other
	 * arm infers on its own, so the variable would bind the arms' least upper bound.
	 */
	@CheckReturnValue
	@Nullable
	private static List<DetailAST> conditionalNewArms(@Nonnull DetailAST value) {
		final var arms = new ArrayList<DetailAST>();
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(value);
		while (!pending.isEmpty()) {
			// EXPR only, deliberately not parens: the fixer cannot reach a diamond nested inside
			// them, so unwrapping here would report a conversion it then half-applies
			final var popped = pending.pop();
			final var arm = popped != null && popped.getType() == TokenTypes.EXPR ? popped.getFirstChild() : popped;
			if (arm == null)
				return null;
			if (arm.getType() == TokenTypes.LITERAL_NEW) {
				arms.add(arm);
				continue;
			}

			if (arm.getType() == TokenTypes.QUESTION) {
				final var condition = arm.getFirstChild();
				final var trueArm = condition == null ? null : condition.getNextSibling();
				final var colon = trueArm == null ? null : trueArm.getNextSibling();
				final var falseArm = colon == null ? null : colon.getNextSibling();
				// a parenthesised condition adds LPAREN/RPAREN siblings, so every position shifts
				// and the node after the true arm is no longer the colon
				if (falseArm == null || colon.getType() != TokenTypes.COLON)
					return null;
				pending.push(trueArm);
				pending.push(falseArm);
				continue;
			}

			// only the arrow form carries its value as the sibling of the '->'; a colon-form
			// switch yields through statements this walk cannot follow, so it is refused
			if (arm.getType() != TokenTypes.LITERAL_SWITCH)
				return null;
			var sawRule = false;
			for (var rule = arm.getFirstChild(); rule != null; rule = rule.getNextSibling()) {
				if (rule.getType() != TokenTypes.SWITCH_RULE)
					continue;
				sawRule = true;
				final var arrow = rule.findFirstToken(TokenTypes.LAMBDA);
				final var body = arrow == null ? null : arrow.getNextSibling();
				if (body == null)
					return null;
				pending.push(body);
			}
			if (!sawRule)
				return null;
		}
		return arms;
	}

	/**
	 * The declared type of parameter {@code index} in every same-file constructor of the class
	 * {@code call} builds, taking {@code arity} arguments. Constructors are declared on that class,
	 * not on any scope enclosing the call, so the class body is resolved rather than walked to.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> constructorParameterTypes(@Nonnull DetailAST call, int arity, int index) {
		final var className = AstUtil.findNewClassName(call);
		final var classDef = className == null ? null : AstUtil.sameFileClassDef(call, className);
		final var body = classDef == null ? null : classDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (body == null)
			return List.of();

		final var found = new ArrayList<String>();
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.CTOR_DEF)
				addParameterTypeAt(child, arity, index, found);
		}
		return found;
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
					final var qualifiedName = AstUtil.typeName(child);
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

	@CheckReturnValue
	private static int countTypeArguments(@Nonnull DetailAST typeArguments) {
		var count = 0;
		for (var arg = typeArguments.getFirstChild(); arg != null; arg = arg.getNextSibling()) {
			if (arg.getType() == TokenTypes.TYPE_ARGUMENT)
				++count;
		}
		return count;
	}

	/**
	 * Whether the declared type arguments at {@code (line, column)} (0-based) belong to a
	 * diamond the fixer may fill. They do only when the initializer is itself a {@code new}
	 * (or a conditional whose every arm is one): in {@code new Foo<>().names()} the diamond
	 * belongs to the chain's receiver, whose type parameter is unrelated to the declared type.
	 */
	@CheckReturnValue
	public static boolean declaredArgumentsMoveToDiamondAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstUtil.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			final var isDeclaration = node.getType() == TokenTypes.VARIABLE_DEF
					|| node.getType() == TokenTypes.RESOURCE;
			if (!isDeclaration)
				continue;
			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				return false;
			final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
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

	@CheckReturnValue
	@Nonnull
	private static Set<String> declaredMemberNames(@Nonnull DetailAST objBlock) {
		final var names = new HashSet<String>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null)
				names.add(ident.getText());
		}
		return names;
	}

	/**
	 * The declared type's arguments, or null when it has none. A qualified type carries them on
	 * its {@code DOT} rather than on the {@code TYPE} node.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST declaredTypeArguments(@Nonnull DetailAST type) {
		final var onType = type.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
		if (onType != null)
			return onType;

		final var first = type.getFirstChild();
		if (first == null || first.getType() != TokenTypes.DOT)
			return null;

		// a qualifier hangs its arguments off the same DOT, so only the last segment's are the type's
		DetailAST lastName = null;
		for (var child = first.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT)
				lastName = child;
		}
		if (lastName == null)
			return null;

		final var next = lastName.getNextSibling();
		return next != null && next.getType() == TokenTypes.TYPE_ARGUMENTS ? next : null;
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
		// a same-file cycle (`class A extends B`, `class B extends A`) does not compile, but the
		// walk runs on whatever parses, so revisiting a body must terminate rather than recurse
		if (!visited.add(objBlock))
			return false;

		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
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

			for (var tp = typeParams.getFirstChild(); tp != null; tp = tp.getNextSibling()) {
				if (tp.getType() != TokenTypes.TYPE_PARAMETER)
					continue;

				final var tpIdent = tp.findFirstToken(TokenTypes.IDENT);
				if (tpIdent == null || !returnIdent.getText().equals(tpIdent.getText()))
					continue;

				final var params = child.findFirstToken(TokenTypes.PARAMETERS);
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
		}

		for (var body : AstUtil.supertypeBodies(objBlock)) {
			if (declaresTargetTypedMethod(body, methodName, visited))
				return true;
		}
		return false;
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
	 * The block a local's uses live in: its own, except for a {@code for}-init declarator, whose
	 * condition, update and body hang off the enclosing {@code for} rather than off {@code FOR_INIT}.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST enclosingScopeOf(@Nonnull DetailAST varDef) {
		final var parent = varDef.getParent();
		return parent != null && parent.getType() == TokenTypes.FOR_INIT ? parent.getParent() : parent;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getInitializerMethodCall(@Nonnull DetailAST assign) {
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
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
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		if (value == null || value.getType() != TokenTypes.LITERAL_NEW)
			return false;

		final var typeArgs = AstUtil.findNewClassTypeArguments(value);
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
		final var methodName = AstUtil.getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.IDENT)
			return false;

		// a bare call resolves against every enclosing class, not only the innermost: a nested,
		// local or anonymous body sits inside the one that declares the method
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

	/**
	 * Every {@code IDENT} in {@code scope}, in document order, grouped by name, minus the names
	 * {@link #collectUses} treats as shadowed. Built once per scope because a block declaring many
	 * locals would otherwise be rescanned for each of them, which is quadratic in the block's size.
	 */
	@CheckReturnValue
	@Nonnull
	private static Map<String, List<DetailAST>> indexUses(@Nonnull DetailAST scope) {
		final var uses = new HashMap<String, List<DetailAST>>();
		collectUses(scope, Set.of(), uses);
		return uses;
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
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
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
		final var token = AstUtil.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		if (token == null)
			return null;
		for (var node = token; node != null; node = node.getParent()) {
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
	 * explicit {@code new T[]{...}} array creation.
	 */
	@CheckReturnValue
	public static boolean isExplicitArrayInitAt(@Nonnull DetailAST root, int line, int column) {
		final var token = AstUtil.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		for (var node = token; node != null; node = node.getParent()) {
			if (node.getType() == TokenTypes.VARIABLE_DEF) {
				final var assign = node.findFirstToken(TokenTypes.ASSIGN);
				return assign != null && isInitializerExplicitArrayInit(assign);
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isInitializerExplicitArrayInit(@Nonnull DetailAST assign) {
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		return value != null
				&& value.getType() == TokenTypes.LITERAL_NEW
				&& value.findFirstToken(TokenTypes.ARRAY_INIT) != null;
	}

	@CheckReturnValue
	private static boolean isInitializerLambdaOrMethodRef(@Nonnull DetailAST assign) {
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		return value != null
				&& (value.getType() == TokenTypes.LAMBDA || value.getType() == TokenTypes.METHOD_REF);
	}

	@CheckReturnValue
	private static boolean isInitializerNull(@Nonnull DetailAST assign) {
		var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null && value.getType() == TokenTypes.TYPECAST) {
			final var rparen = value.findFirstToken(TokenTypes.RPAREN);
			value = AstUtil.unwrapParensAndExpr(rparen != null ? rparen.getNextSibling() : null);
		}
		return value != null && value.getType() == TokenTypes.LITERAL_NULL;
	}

	/**
	 * Checks whether the initializer is an anonymous class with exactly
	 * one method and no extra members (fields, inner types, etc.).
	 */
	@CheckReturnValue
	private static boolean isInitializerSimpleAnonymousClass(@Nonnull DetailAST assign) {
		final var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
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
		final var token = AstUtil.findNodeAt(root, line, column, node -> node.getFirstChild() == null);
		if (token == null)
			return null;
		for (var node = token; node != null; node = node.getParent()) {
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

	/**
	 * The component type of the array {@code iterable} names, or null when it does not resolve to
	 * an array declared in this file.
	 */
	@CheckReturnValue
	@Nullable
	private static String iteratedArrayComponentName(@Nullable DetailAST iterable) {
		final var value = AstUtil.unwrapParensAndExpr(iterable);
		if (value == null || value.getType() != TokenTypes.IDENT)
			return null;

		final var declared = AstUtil.resolveVariableType(value, value.getText());
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
	 * {@code new}, or {@code this}/{@code super}. A chain is followed to its base, since the base
	 * is what has to be resolvable for the whole chain to be.
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

		// a parenthesised cast writes its type at the call site, so the receiver names it outright
		// just as a literal or a `new` does
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
	private static List<String> methodParameterTypes(
			@Nonnull DetailAST call,
			int arity,
			int index
	) {
		final var methodName = calledMethodName(call);
		if (methodName == null)
			return List.of();

		final var found = new ArrayList<String>();
		final var visited = new HashSet<DetailAST>();
		for (var scope = call.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.OBJBLOCK)
				collectParameterTypes(scope, methodName, arity, index, visited, found);
		}
		return found;
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
				&& "valueOf".equals(AstUtil.getMethodName(value))
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
		return receiver != null && receiver.getType() == TokenTypes.DOT && AstUtil.isPureDotChainOrIdent(receiver)
				? AstUtil.dottedName(receiver)
				: null;
	}

	/**
	 * Whether the receiver names a variable declared in this file. Its type is concrete even when
	 * this check cannot spell it, so the call is a member of a known class rather than an unknown
	 * one, and {@code var} binds whatever that member returns.
	 */
	@CheckReturnValue
	private static boolean receiverIsDeclaredVariable(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.getFirstChild();
		if (dot == null || dot.getType() != TokenTypes.DOT)
			return false;

		var receiver = dot.getFirstChild();
		// an array element's type is its array's component type, so the array's own declaration is
		// what has to be resolvable
		if (receiver != null && receiver.getType() == TokenTypes.INDEX_OP)
			receiver = receiver.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return false;

		final var name = receiver.getText();
		for (var scope = methodCall.getParent(); scope != null; scope = scope.getParent()) {
			if (declaresVariableNamed(scope, name))
				return true;
		}
		return false;
	}

	/**
	 * The declared return type of the method {@code methodCall} names, read from this file, or
	 * null when no unambiguous declaration is found. The classpath is deliberately not consulted:
	 * reflection reports the erasure, so {@code List<String>.get} would read as {@code Object} and
	 * every such declaration would look narrowed.
	 */
	@CheckReturnValue
	@Nullable
	private static String resolvedReturnTypeName(@Nonnull DetailAST methodCall, @Nonnull DetailAST scope) {
		final var methodName = AstUtil.getMethodName(methodCall);
		if (methodName == null)
			return null;

		final var arguments = methodCall.findFirstToken(TokenTypes.ELIST);
		return AstUtil.resolveSameFileMethodReturnType(
				scope, methodName, arguments == null ? 0 : AstUtil.countArguments(arguments)
		);
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
		final var name = AstUtil.typeName(nameNode);
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

	@CheckReturnValue
	@Nonnull
	private static Set<String> union(@Nonnull Set<String> first, @Nonnull Set<String> second) {
		if (second.isEmpty())
			return first;

		final var merged = new HashSet<>(first);
		merged.addAll(second);
		return merged;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapInitializerValue(@Nonnull DetailAST assign) {
		var value = AstUtil.unwrapParensAndExpr(assign.getFirstChild());
		while (value != null
				&& (value.getType() == TokenTypes.UNARY_MINUS || value.getType() == TokenTypes.UNARY_PLUS))
			value = AstUtil.unwrapParensAndExpr(value.getFirstChild());
		return value;
	}

	private final Map<DetailAST, Map<String, List<DetailAST>>> scopeUses = new HashMap<>();
	private final Map<String, String> staticImportOwners = new HashMap<>();

	private Set<String> allowedMethods = Set.of();

	/**
	 * Whether {@code use}, read as a call argument, would bind to a different parameter once its
	 * type narrows.
	 */
	@CheckReturnValue
	private boolean argumentReselectsAnOverload(
			@Nonnull DetailAST use,
			@Nonnull String declaredFqcn,
			@Nonnull String constructedFqcn
	) {
		final var expr = use.getParent();
		final var elist = expr == null ? null : expr.getParent();
		if (expr == null || expr.getType() != TokenTypes.EXPR || elist == null || elist.getType() != TokenTypes.ELIST)
			return false;

		final var call = elist.getParent();
		if (call == null)
			return false;

		var index = 0;
		for (var arg = elist.getFirstChild(); arg != null && arg != expr; arg = arg.getNextSibling()) {
			if (arg.getType() != TokenTypes.COMMA)
				++index;
		}

		final var arity = AstUtil.countArguments(elist);
		final var candidates = call.getType() == TokenTypes.LITERAL_NEW
				? constructorParameterTypes(call, arity, index)
				: methodParameterTypes(call, arity, index);
		for (var parameterName : candidates) {
			final var parameterFqcn = resolvedClassName(parameterName);
			if (parameterFqcn != null && ReflectionUtil.parameterSelectsOnlyTheConstructedType(
					parameterFqcn, declaredFqcn, constructedFqcn
			))
				return true;
		}
		return false;
	}

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		scopeUses.clear();
		staticImportOwners.clear();
	}

	/**
	 * Whether the class declaring the called method can be determined here. When it cannot, the
	 * method may be target-typed, and {@code var} would silently rebind the variable to whatever
	 * the call infers on its own.
	 */
	@CheckReturnValue
	private boolean callTargetResolvable(@Nonnull DetailAST methodCall) {
		final var methodName = AstUtil.getMethodName(methodCall);
		if (methodName == null)
			return false;

		if (staticImportOwners.containsKey(methodName) || sameFileDeclaresMethod(methodCall, methodName))
			return true;

		final var receiver = receiverTypeName(methodCall);
		if (receiver != null && resolve(receiver) != null)
			return true;

		final var qualified = qualifiedReceiverName(methodCall);
		if (qualified != null)
			return ReflectionUtil.isResolvableClass(qualified);

		return literalReceiverResolvable(methodCall) || receiverIsDeclaredVariable(methodCall);
	}

	/**
	 * Whether the class constructed by {@code value} cannot take the declared type arguments, so
	 * moving them onto its diamond would not compile.
	 */
	@CheckReturnValue
	private boolean constructedTypeParameterCountDiffers(
			@Nonnull DetailAST type,
			@Nonnull DetailAST declaredArgs,
			@Nonnull DetailAST value
	) {
		final var createdName = AstUtil.findNewClassName(value);
		if (createdName == null)
			return false;
		final var fqcn = resolvedClassName(createdName);
		final var declared = fqcn == null ? -1 : ReflectionUtil.declaredTypeParameterCount(fqcn);
		if (declared < 0) {
			// off the classpath, so the arity cannot be checked: only constructing the declared
			// type itself is safe, since anything else may declare a different arity
			final var declaredName = AstUtil.typeName(type.getFirstChild());
			if (declaredName == null)
				return true;
			if (declaredName.equals(createdName))
				return false;

			// compared in the same form, since either spelling may name a class the other does
			// not: `api.Cache<String, Integer> c = new impl.Cache<>()` is written qualified
			// precisely because the simple names collide
			final var declaredFqcn = resolvedClassName(declaredName);
			if (fqcn != null && declaredFqcn != null)
				return !fqcn.equals(declaredFqcn);

			// neither name is on the classpath, so only the compilation unit is evidence. A subtype
			// may reorder the parameters it passes up (`Swapped<B, A> extends Src<A, B>`), which
			// matching arity cannot tell apart from an identity mapping
			final var createdDef = AstUtil.sameFileClassDef(type, createdName);
			return createdDef == null
					|| createdDef != AstUtil.sameFileClassDef(type, declaredName)
					|| countTypeArguments(declaredArgs) != AstUtil.typeParameterCount(createdDef);
		}

		return countTypeArguments(declaredArgs) != declared;
	}

	/**
	 * Releases the per-scope use index. Checkstyle reuses one check instance across files, so a map
	 * left populated pins the finished file's whole AST through its identifiers' parent pointers.
	 */
	@Override
	public void finishTree(@Nonnull DetailAST rootAST) {
		scopeUses.clear();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.FOR_EACH_CLAUSE,
				TokenTypes.IMPORT,
				TokenTypes.PACKAGE_DEF,
				TokenTypes.RESOURCE,
				TokenTypes.STATIC_IMPORT,
				TokenTypes.VARIABLE_DEF
		};
	}

	@CheckReturnValue
	private boolean hasReflectionGenericReturnType(@Nonnull DetailAST methodCall) {
		final var methodName = AstUtil.getMethodName(methodCall);
		if (methodName == null)
			return false;

		final var receiverTypeName = receiverTypeName(methodCall);
		var fqcn = receiverTypeName == null
				? qualifiedReceiverName(methodCall)
				: resolve(receiverTypeName);
		if (fqcn == null)
			fqcn = staticImportOwners.get(methodName);
		final var args = methodCall.findFirstToken(TokenTypes.ELIST);
		final var argCount = args == null ? 0 : AstUtil.countArguments(args);
		return fqcn != null && ReflectionUtil.hasGenericReturnType(fqcn, methodName, argCount);
	}

	/**
	 * The class the initializer's own type names, or null when it cannot be determined here.
	 */
	@CheckReturnValue
	@Nullable
	private String inferredInitializerName(@Nonnull DetailAST value, @Nonnull DetailAST scope) {
		if (value.getType() == TokenTypes.LITERAL_NEW)
			return AstUtil.findNewClassName(value);

		if (value.getType() == TokenTypes.QUESTION || value.getType() == TokenTypes.LITERAL_SWITCH) {
			final var arms = conditionalNewArms(value);
			if (arms == null || arms.isEmpty())
				return null;

			// arms that all construct one class pin it; differing arms bind their least upper bound,
			// an intersection type no declaration can name, so nothing can be reassigned into it
			final var first = AstUtil.findNewClassName(arms.getFirst());
			if (first == null)
				return null;

			for (var arm : arms) {
				if (!first.equals(AstUtil.findNewClassName(arm)))
					return NO_NAMEABLE_TYPE;
			}
			return first;
		}

		return value.getType() == TokenTypes.METHOD_CALL ? resolvedReturnTypeName(value, scope) : null;
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
	 * Whether two constructor names denote different classes. Both spellings are resolved before
	 * comparing, since {@code api.Cache} and {@code impl.Cache} share a simple name while naming
	 * unrelated classes.
	 */
	@CheckReturnValue
	private boolean namesDifferentClasses(@Nonnull DetailAST scope, @Nonnull String left, @Nonnull String right) {
		if (left.equals(right))
			return false;

		final var leftFqcn = resolvedClassName(left);
		final var rightFqcn = resolvedClassName(right);
		if (leftFqcn != null && rightFqcn != null)
			return !leftFqcn.equals(rightFqcn);

		// a name neither the classpath nor this file declares could denote anything, so the two are
		// reported as different and the declaration stays a warning rather than a conversion
		final var leftDef = AstUtil.sameFileClassDef(scope, left);
		final var rightDef = AstUtil.sameFileClassDef(scope, right);
		return leftDef == null || rightDef == null || leftDef != rightDef;
	}

	/**
	 * The class the initializer binds when the declaration names something else, so {@code var}
	 * would narrow the variable, or null when the two agree or the initializer's type is unknown.
	 */
	@CheckReturnValue
	@Nullable
	private String narrowedInitializerName(@Nonnull DetailAST type, @Nonnull DetailAST assign) {
		final var value = unwrapInitializerValue(assign);
		if (value == null)
			return null;

		final var inferred = inferredInitializerName(value, type);
		final var declaredName = simpleTypeName(type.getFirstChild());
		if (inferred == null || declaredName == null)
			return null;

		return declaredName.equals(AstUtil.simpleName(inferred)) ? null : inferred;
	}

	/**
	 * Whether narrowing this variable to the class its initializer binds would make some call on it
	 * select a different overload. The call binds to the most specific applicable parameter, so a
	 * parameter that accepts the constructed class but not the declared one is one the argument
	 * would move to: {@code pick(List)} beside {@code pick(ArrayList)} silently changes answer.
	 */
	@CheckReturnValue
	private boolean overloadSelectionChanges(@Nonnull DetailAST varDef, @Nonnull DetailAST type, @Nonnull String constructedName) {
		final var nameNode = varDef.findFirstToken(TokenTypes.IDENT);
		final var declaredName = AstUtil.typeName(type.getFirstChild());
		if (nameNode == null || declaredName == null)
			return false;

		final var declaredFqcn = resolvedClassName(declaredName);
		final var constructedFqcn = resolvedClassName(constructedName);
		// an unresolvable pair says nothing either way, and refusing on it would silence every
		// declaration whose classes are off the classpath. An identical pair short-circuits the
		// use walk rather than guarding a distinct answer: no parameter accepts one of the two
		// and not the other, so the walk below would reject it one reflection call later
		if (declaredFqcn == null || constructedFqcn == null || declaredFqcn.equals(constructedFqcn))
			return false;

		for (var node : usesAfter(varDef, nameNode)) {
			if (argumentReselectsAnOverload(node, declaredFqcn, constructedFqcn))
				return true;
		}
		return false;
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

		final var declaredType = getPrimitiveTypeName(type);
		if (declaredType == null)
			return referenceVarChangesType(type, assign) ? PrimitiveVarAction.SKIP : PrimitiveVarAction.ERROR;

		final var castType = initializerCastType(assign);
		if (declaredType.equals(castType))
			return PrimitiveVarAction.ERROR;

		final var value = unwrapInitializerValue(assign);
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
	 * Whether the variable is reassigned to anything other than the very same class. Under
	 * {@code var} it takes the constructed class, so {@code List<String> l = new ArrayList<>()}
	 * followed by {@code l = new LinkedList<>()} stops compiling.
	 */
	@CheckReturnValue
	private boolean reassignedBeyondTheConstructedType(@Nonnull DetailAST varDef, @Nonnull String constructedName) {
		final var nameNode = varDef.findFirstToken(TokenTypes.IDENT);
		if (nameNode == null)
			return true;

		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null && modifiers.findFirstToken(TokenTypes.FINAL) != null)
			return false;

		// only the uses after the declarator are this variable's: one above it or in a sibling block
		// reaches a field, a parameter, or another variable that happens to share the name
		for (var node : usesAfter(varDef, nameNode)) {
			final var parent = node.getParent();
			if (parent == null)
				continue;

			if (parent.getType() == TokenTypes.ASSIGN && parent.getFirstChild() == node) {
				final var reassigned = AstUtil.unwrapParensAndExpr(node.getNextSibling());
				if (reassigned == null)
					return true;

				if (reassigned.getType() == TokenTypes.LITERAL_NULL)
					continue;

				final var reassignedName = reassigned.getType() == TokenTypes.LITERAL_NEW
						? AstUtil.findNewClassName(reassigned)
						: null;

				if (reassignedName == null || namesDifferentClasses(varDef, constructedName, reassignedName))
					return true;
				continue;
			}
		}
		return false;
	}

	@CheckReturnValue
	private boolean referenceVarChangesType(@Nonnull DetailAST type, @Nonnull DetailAST assign) {
		final var value = unwrapInitializerValue(assign);
		if (value == null)
			return false;

		if (hasQualifierTypeArguments(type))
			return true;

		final var declaredArgs = declaredTypeArguments(type);
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
				&& (value.getType() == TokenTypes.QUESTION || value.getType() == TokenTypes.LITERAL_SWITCH)) {
			final var arms = conditionalNewArms(value);
			if (arms == null)
				return true;

			for (var arm : arms) {
				if (constructedTypeParameterCountDiffers(type, declaredArgs, arm))
					return true;
			}
		}

		if (!isNew)
			return false;

		// an anonymous class body var cannot name: only the single-method form is deferred
		// to PreferLambdaCheck, so a multi-member body would bind the non-denotable type
		if (value.findFirstToken(TokenTypes.OBJBLOCK) != null)
			return true;

		// the declared arguments move onto the constructor, so the conversion stays faithful to
		// them, but only when the constructed class takes that many: `Function<String, Integer>
		// f = new MyFunc<>()` with `MyFunc<T> implements Function<T, Integer>` would not compile
		if (declaredArgs != null)
			return constructedTypeParameterCountDiffers(type, declaredArgs, value);

		final var createdName = simpleTypeName(value.getFirstChild());
		return declaredName != null && createdName != null && !declaredName.equals(createdName);
	}

	/**
	 * The qualified form of {@code name}: an already-qualified name as written, a simple name
	 * resolved through the imports, or null when a simple name resolves to nothing.
	 */
	@CheckReturnValue
	@Nullable
	private String resolvedClassName(@Nonnull String name) {
		return name.indexOf('.') >= 0 ? name : resolve(name);
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

	@CheckReturnValue
	@Nonnull
	private List<DetailAST> usesAfter(@Nonnull DetailAST varDef, @Nonnull DetailAST nameNode) {
		final var scope = enclosingScopeOf(varDef);
		if (scope == null)
			return List.of();

		final var all = scopeUses
				.computeIfAbsent(scope, PreferVarCheck::indexUses)
				.getOrDefault(nameNode.getText(), List.of());
		final var declarator = all.indexOf(nameNode);
		return declarator < 0 ? List.of() : all.subList(declarator + 1, all.size());
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.FOR_EACH_CLAUSE -> {
				final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
				if (varDef == null || isVarType(varDef))
					return;

				final var loopType = varDef.findFirstToken(TokenTypes.TYPE);
				if (loopType == null || elementTypeChangesUnderVar(loopType, ast.findFirstToken(TokenTypes.EXPR)))
					return;

				log(loopType, MSG_FOREACH);
			}
			case TokenTypes.RESOURCE -> {
				// Java 9+ "try (existingVar) {}" reference form has no TYPE child;
				// it can't use var either way, so skip rather than log at null.
				final var typeNode = ast.findFirstToken(TokenTypes.TYPE);
				if (typeNode == null || isVarType(ast))
					return;

				final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
				if (assign != null && referenceVarChangesType(typeNode, assign))
					return;

				log(typeNode, MSG_TRY);
			}
			case TokenTypes.STATIC_IMPORT -> {
				// the node leads with the `static` keyword, so the qualified name is the sibling
				// before the semicolon rather than the first child
				final var qualified = ast.getLastChild().getPreviousSibling();
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

				if (isVarType(ast) && hasAllObjectTypeArgs(assign)) {
					log(type, MSG_DIAMOND);
					return;
				}

				if (isMultiVarDeclaration(ast)) {
					if (!isVarType(ast) && isMultiVarFirst(ast))
						logWarning(type, MSG_LOCAL, "should");
					return;
				}

				if (isInitializerNull(assign))
					return;

				final var initValue = assign.getFirstChild();
				if (initValue != null && initValue.getType() == TokenTypes.ARRAY_INIT)
					return;

				if (isInitializerExplicitArrayInit(assign)) {
					log(type, MSG_VAR_EXPLICIT_ARRAY);
					return;
				}

				if (isInitializerLambdaOrMethodRef(assign))
					return;

				if (isInitializerSimpleAnonymousClass(assign))
					return;

				final var primAction = primitiveVarAction(ast, assign);
				if (primAction == PrimitiveVarAction.SKIP)
					return;

				// below the refusals above, so a declaration `var` cannot express at all stays
				// silent instead of being advertised as one it should be used on
				final var narrowedName = isVarType(ast) ? null : narrowedInitializerName(type, assign);
				if (narrowedName != null && (reassignedBeyondTheConstructedType(ast, narrowedName)
						|| overloadSelectionChanges(ast, type, narrowedName))) {
					logWarning(typeStartToken(type), MSG_LOCAL, "should");
					return;
				}

				final var methodCall = getInitializerMethodCall(assign);
				final var methodName = methodCall == null ? null : AstUtil.getMethodName(methodCall);
				final var isGeneric = (methodName != null && allowedMethods.contains(methodName))
						|| (methodCall != null && hasGenericReturnType(methodCall))
						|| (methodCall != null && hasReflectionGenericReturnType(methodCall));

				if (isGeneric && hasTypeArguments(methodCall)) {
					logWarning(type, MSG_TYPE_ARGS, methodName);
					return;
				}

				if (isVarType(ast)) {
					if (isGeneric)
						logWarning(type, MSG_VAR_GENERIC, methodName);
					return;
				}

				if (methodCall != null && !callTargetResolvable(methodCall))
					return;

				if (!isGeneric) {
					if (primAction == PrimitiveVarAction.WARN)
						logWarning(type, MSG_LOCAL, "should");
					else {
						final var typeStart = typeStartToken(type);
						log(typeStart.getLineNo(), typeStart.getColumnNo(), MSG_LOCAL, "must");
					}
				}
			}
		}
	}
}