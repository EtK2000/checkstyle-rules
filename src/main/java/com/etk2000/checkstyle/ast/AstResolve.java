package com.etk2000.checkstyle.ast;

import com.etk2000.checkstyle.ReflectionUtil;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Resolves a name to a type, from the same file where possible and by reflection otherwise.
 *
 * @apiNote These answers are a best effort, not the JLS: same-file overloads are picked by arity,
 *          the scope ascent is name-blind past an ancestor homonym, and a type parameter can
 *          resolve to an unrelated imported class of the same name. A {@code null} means
 *          "unknown", never "no such type". So an answer from here must not on its own gate a
 *          rewrite of user source, which {@code config/checkstyle/import-control.xml} enforces by
 *          barring the fix package from importing this class.
 */
public final class AstResolve {
	/**
	 * A name that some scope binds. A null {@code typeName} means the binder is there but its
	 * type cannot be spelled.
	 */
	private record Binding(@Nullable String typeName, @Nullable DetailAST declarator) {}

	/**
	 * The binding of {@code varName} among {@code scope}'s direct children, or null when nothing
	 * there binds it.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding bindingAmong(@Nullable DetailAST scope, @Nonnull String varName) {
		if (scope == null)
			return null;

		for (var child = scope.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (bindsName(child, varName))
				return new Binding(variableTypeName(child, varName), child);
		}
		return null;
	}

	/**
	 * Whether {@code node} is a declarator introducing {@code varName}. The {@code TYPE} test
	 * separates a declaring resource from a try-with-resources reference to an existing variable,
	 * which carries the same identifier without binding it.
	 */
	@CheckReturnValue
	private static boolean bindsName(@Nonnull DetailAST node, @Nonnull String varName) {
		if (node.getType() != TokenTypes.VARIABLE_DEF && node.getType() != TokenTypes.PARAMETER_DEF
				&& node.getType() != TokenTypes.RESOURCE)
			return false;
		if (node.findFirstToken(TokenTypes.TYPE) == null)
			return false;

		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && varName.equals(ident.getText());
	}

	/**
	 * The return type of {@code call}, resolved either as a same-file method or by reflecting on
	 * the receiver's type. A receiver that resolves to a same-file type yields null unless the call
	 * reads a record component: the classpath cannot confirm its other members.
	 */
	@CheckReturnValue
	@Nullable
	private static String callReturnTypeName(
			@Nonnull DetailAST call,
			@Nullable String packageName,
			@Nonnull Set<String> imports
	) {
		final var first = call.getFirstChild();
		if (first == null)
			return null;

		final var elist = call.findFirstToken(TokenTypes.ELIST);
		final var arity = elist == null ? 0 : AstQuery.countArguments(elist);

		if (first.getType() == TokenTypes.IDENT)
			return resolveSameFileMethodReturnType(call, first.getText(), arity);

		if (first.getType() != TokenTypes.DOT)
			return null;

		final var methodName = AstText.lastIdent(first);
		final var qualifier = first.getFirstChild();
		if (methodName != null && qualifier != null) {
			// the receiver walk below has an arm for neither qualifier, and would answer unknown for
			// a method this file can read
			if (qualifier.getType() == TokenTypes.LITERAL_THIS)
				return resolveSameFileMethodReturnType(call, methodName, arity);

			if (qualifier.getType() == TokenTypes.LITERAL_SUPER) {
				final var body = enclosingBody(call);
				return body == null
						? null
						: inheritedMethodReturnType(body, methodName, arity, new HashSet<>());
			}
		}

		final var receiverType = getReceiverTypeName(call, packageName, imports);
		if (methodName == null || receiverType == null)
			return null;

		final var fqcn = ReflectionUtil.resolveClassName(receiverType, packageName, imports);
		if (fqcn != null)
			return ReflectionUtil.getMethodReturnTypeName(fqcn, methodName, arity);

		return arity == 0 ? recordComponentTypeName(call, receiverType, methodName) : null;
	}

	@CheckReturnValue
	private static int countParameters(@Nonnull DetailAST parameters) {
		var count = 0;
		for (var child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.PARAMETER_DEF)
				++count;
		}
		return count;
	}

	/**
	 * The return type the body {@code objBlock} declares for {@code methodName} at {@code arity}.
	 * Null when it declares no such overload, when the one it declares spells a return type
	 * {@link #getTypeName} cannot name, or when two overloads at that arity disagree.
	 */
	@CheckReturnValue
	@Nullable
	private static String declaredMethodReturnTypeIn(@Nonnull DetailAST objBlock, @Nonnull String methodName, int arity) {
		String matched = null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident == null || !methodName.equals(ident.getText()))
				continue;
			final var params = child.findFirstToken(TokenTypes.PARAMETERS);
			if ((params == null ? 0 : countParameters(params)) != arity)
				continue;
			final var typeNode = child.findFirstToken(TokenTypes.TYPE);
			final var typeName = typeNode == null ? null : getTypeName(typeNode);
			if (typeName == null)
				continue;
			if (matched != null && !matched.equals(typeName))
				return null;
			matched = typeName;
		}
		return matched;
	}

	/** The body of a type {@code name} names by being declared in an enclosing scope of {@code scope}. */
	@CheckReturnValue
	@Nullable
	private static DetailAST declaredTypeBody(@Nonnull DetailAST scope, @Nonnull String name) {
		for (var frame = scope; frame != null; frame = frame.getParent()) {
			for (var child = frame.getFirstChild(); child != null; child = child.getNextSibling()) {
				final var type = child.getType();
				final var isTypeDef = type == TokenTypes.CLASS_DEF || type == TokenTypes.ENUM_DEF
						|| type == TokenTypes.INTERFACE_DEF || type == TokenTypes.RECORD_DEF
						|| type == TokenTypes.ANNOTATION_DEF;
				final var ident = isTypeDef ? child.findFirstToken(TokenTypes.IDENT) : null;
				if (ident != null && name.equals(ident.getText()))
					return child.findFirstToken(TokenTypes.OBJBLOCK);
			}
		}

		final var def = sameFileClassDef(scope, name);
		return def == null ? null : def.findFirstToken(TokenTypes.OBJBLOCK);
	}

	/**
	 * The type of the value an array access reads: the array expression's type with one dimension
	 * stripped. {@code char[][] grid} gives {@code char[]} for {@code grid[0]} and {@code char} for
	 * {@code grid[0][0]}. Null when the array expression does not resolve, or resolves to something
	 * with no dimension left to strip.
	 */
	@CheckReturnValue
	@Nullable
	private static String elementTypeName(
			@Nonnull DetailAST indexOp,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		final var array = indexOp.getFirstChild();
		if (array == null)
			return null;

		final var arrayType = expressionTypeName(array, packageName, imports, memo);
		return arrayType != null && arrayType.endsWith("[]")
				? arrayType.substring(0, arrayType.length() - 2)
				: null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST enclosingBody(@Nonnull DetailAST node) {
		for (var frame = node.getParent(); frame != null; frame = frame.getParent()) {
			if (frame.getType() == TokenTypes.OBJBLOCK)
				return frame;
		}
		return null;
	}

	/**
	 * The type name of the value {@code expr} evaluates to, or null when this file's scope cannot
	 * determine it. The answer is spelled the way {@link ReflectionUtil#resolveClassName} accepts:
	 * either a simple name the file's imports resolve, or an already-qualified one. An array reads
	 * as {@code Foo[]} when it comes from a declaration, but reflection spells a method's array
	 * return in JVM form ({@code [Ljava.lang.String;}), so a caller testing for arrays must accept
	 * both.
	 */
	@CheckReturnValue
	@Nullable
	public static String expressionTypeName(
			@Nonnull DetailAST expr,
			@Nullable String packageName,
			@Nonnull Set<String> imports
	) {
		return expressionTypeName(expr, packageName, imports, new IdentityHashMap<>());
	}

	/**
	 * The memoized body of {@link #expressionTypeName}. {@code memo} is both the cycle guard and
	 * the work cache, and it has to be both:
	 *
	 * <ul>
	 *     <li>a name can resolve through itself, as in {@code for (var items : items)} where the
	 *     iterable is a field the loop variable shadows. The scope walk is position-blind, so it
	 *     hands back the loop's own declarator and the resolution re-enters on the same node;</li>
	 *     <li>{@link #ternaryTypeName} resolves both branches, so a chain of
	 *     {@code var v = c ? u : u;} doubles the work per level and costs 2^n without a cache.</li>
	 * </ul>
	 *
	 * <p>A node already in progress is held as a null value, and answers null: this class's
	 * "unknown".
	 */
	@CheckReturnValue
	@Nullable
	private static String expressionTypeName(
			@Nonnull DetailAST expr,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		final var node = AstQuery.unwrapParensAndExpr(expr);
		if (node == null)
			return null;
		if (memo.containsKey(node)) {
			final var cached = memo.get(node);
			return cached == null ? null : cached.orElse(null);
		}

		memo.put(node, null);
		final var resolved = switch (node.getType()) {
			case TokenTypes.DOT -> fieldAccessTypeName(node, packageName, imports);
			case TokenTypes.IDENT -> identTypeName(node, packageName, imports, memo);
			case TokenTypes.INDEX_OP -> elementTypeName(node, packageName, imports, memo);
			case TokenTypes.METHOD_CALL -> callReturnTypeName(node, packageName, imports);
			case TokenTypes.QUESTION -> ternaryTypeName(node, packageName, imports, memo);
			case TokenTypes.TYPECAST -> {
				final var type = node.findFirstToken(TokenTypes.TYPE);
				yield type == null ? null : getTypeName(type);
			}
			default -> null;
		};
		memo.put(node, Optional.ofNullable(resolved));
		return resolved;
	}

	/**
	 * The declared type of the field {@code dot} reads, for the qualifier shapes whose owning type
	 * this file can name: {@code this.f}, {@code Outer.this.f}, and {@code Type.F} or
	 * {@code variable.f} where the owner is declared in this file.
	 */
	@CheckReturnValue
	@Nullable
	private static String fieldAccessTypeName(
			@Nonnull DetailAST dot,
			@Nullable String packageName,
			@Nonnull Set<String> imports
	) {
		final var qualifier = dot.getFirstChild();
		final var field = dot.getLastChild();
		if (qualifier == null || field == null || qualifier == field || field.getType() != TokenTypes.IDENT)
			return null;
		final var fieldName = field.getText();

		if (qualifier.getType() == TokenTypes.LITERAL_THIS)
			return resolveSameFileFieldType(dot, null, fieldName);

		// Outer.this.f: the inner DOT holds the outer type name and the `this`
		if (qualifier.getType() == TokenTypes.DOT) {
			final var outer = qualifier.getFirstChild();
			return qualifier.getLastChild().getType() == TokenTypes.LITERAL_THIS
					&& outer != null && outer.getType() == TokenTypes.IDENT
					? resolveSameFileFieldType(dot, outer.getText(), fieldName)
					: null;
		}

		if (qualifier.getType() != TokenTypes.IDENT)
			return null;

		// JLS 6.4.2 obscuring: a variable hides a type of the same name, so the binding is asked
		// first even for a capitalised qualifier, which otherwise reads as a type name
		final var qualifierName = qualifier.getText();
		final var ownerType = resolveVariableType(dot, qualifierName);
		if (ownerType != null)
			return resolveSameFileFieldType(dot, ownerType, fieldName);

		// with no binding, only a type name is left to try; reading a lowercase qualifier as one
		// would make resolveSameFileFieldType answer off the *enclosing* class instead
		return !qualifierName.isEmpty() && Character.isUpperCase(qualifierName.charAt(0))
				? resolveSameFileFieldType(dot, qualifierName, fieldName)
				: null;
	}

	/**
	 * The binding of {@code fieldName} on the type whose body is {@code objBlock}, whether declared
	 * there, declared as one of a record's components, or inherited from a same-file supertype.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding fieldBindingIn(@Nonnull DetailAST objBlock, @Nonnull String fieldName) {
		final var own = bindingAmong(objBlock, fieldName);
		if (own != null)
			return own;

		final var component = recordComponentBinding(objBlock.getParent(), fieldName);
		return component != null ? component : inheritedFieldBinding(objBlock, fieldName);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findEnclosingClassDef(@Nonnull DetailAST node) {
		for (var p = node.getParent(); p != null; p = p.getParent()) {
			final var t = p.getType();
			if (t == TokenTypes.CLASS_DEF || t == TokenTypes.INTERFACE_DEF
					|| t == TokenTypes.ENUM_DEF || t == TokenTypes.RECORD_DEF)
				return p;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findInnerClassDef(@Nonnull DetailAST classDef, @Nonnull String className) {
		final var objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var t = child.getType();
			if (t != TokenTypes.CLASS_DEF && t != TokenTypes.INTERFACE_DEF
					&& t != TokenTypes.ENUM_DEF && t != TokenTypes.RECORD_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null && className.equals(ident.getText()))
				return child;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findSameFileClassDef(@Nonnull DetailAST node, @Nonnull String className) {
		for (var enclosing = findEnclosingClassDef(node); enclosing != null; enclosing = findEnclosingClassDef(enclosing)) {
			final var found = findInnerClassDef(enclosing, className);
			if (found != null)
				return found;
		}
		var root = node;
		while (root.getParent() != null)
			root = root.getParent();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var t = child.getType();
			if (t == TokenTypes.CLASS_DEF || t == TokenTypes.INTERFACE_DEF
					|| t == TokenTypes.ENUM_DEF || t == TokenTypes.RECORD_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null && className.equals(ident.getText()))
					return child;
			}
		}
		return null;
	}

	/**
	 * The element type a for-each binding takes, for the case its own declaration cannot spell:
	 * {@code for (var row : grid)} over a {@code char[][]} binds {@code row} to {@code char[]}.
	 * Only an array iterable answers; a {@code Collection}'s element type lives in a type argument
	 * the loop header does not carry.
	 */
	@CheckReturnValue
	@Nullable
	private static String forEachElementTypeName(
			@Nonnull DetailAST declarator,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		final var clause = declarator.getParent();
		if (clause == null || clause.getType() != TokenTypes.FOR_EACH_CLAUSE)
			return null;

		// a COLON sits between the declarator and the iterable, so the EXPR is not the next sibling
		var iterable = declarator.getNextSibling();
		while (iterable != null && iterable.getType() != TokenTypes.EXPR)
			iterable = iterable.getNextSibling();
		if (iterable == null)
			return null;

		final var iterableType = expressionTypeName(iterable, packageName, imports, memo);
		return iterableType != null && iterableType.endsWith("[]")
				? iterableType.substring(0, iterableType.length() - 2)
				: null;
	}

	/**
	 * Finds the type name of the receiver in a dotted method call.
	 * For {@code obj.method()}, finds the declared type of {@code obj}
	 * (field, parameter, or local variable).
	 * For {@code Type.method()}, returns {@code Type} directly (static call).
	 * For chained calls, returns {@code null};
	 * use {@link #getReceiverTypeName(DetailAST, String, Set)} for chain resolution.
	 *
	 * <p>Deliberately package-private: it is the base case the 3-argument overload loops on, and
	 * its {@code null} for a chain reads as "unknown", which several detectors treat as "fire".
	 * A caller outside this package that reached for the obvious single-argument form would get
	 * the fail-open answer.
	 */
	@CheckReturnValue
	@Nullable
	static String getReceiverTypeName(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return null;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null)
			return null;

		if (receiver.getType() == TokenTypes.DOT) {
			final var qualifier = receiver.getFirstChild();
			if (qualifier == null || qualifier.getType() != TokenTypes.LITERAL_THIS)
				return null;

			// resolved as a field, not through resolveVariableType: `this.` is required
			// precisely when a local or parameter shadows the field, and that shadow must
			// not be what answers here
			final var fieldName = qualifier.getNextSibling();
			return fieldName == null || fieldName.getType() != TokenTypes.IDENT
					? null
					: resolveSameFileFieldType(methodCall, null, fieldName.getText());
		}

		if (receiver.getType() != TokenTypes.IDENT)
			return null;

		final var receiverName = receiver.getText();
		if (!receiverName.isEmpty() && Character.isUpperCase(receiverName.charAt(0)))
			return receiverName;

		return resolveVariableType(methodCall, receiverName);
	}

	/**
	 * Like {@link #getReceiverTypeName(DetailAST)} but also resolves
	 * chained method calls (e.g. {@code fragment.requireView().findViewById()})
	 * by walking the chain and using reflection to resolve intermediate return types.
	 */
	@CheckReturnValue
	@Nullable
	public static String getReceiverTypeName(
			@Nonnull DetailAST methodCall,
			@Nullable String packageName,
			@Nonnull Set<String> imports
	) {
		// iterative rather than recursive, so a very long chain cannot overflow the stack
		final var methodNames = new ArrayDeque<String>();
		final var argCounts = new ArrayDeque<Integer>();
		var current = methodCall;
		String baseType;
		while (true) {
			final var simple = getReceiverTypeName(current);
			if (simple != null) {
				baseType = simple;
				break;
			}
			final var firstChild = current.getFirstChild();
			if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
				return null;
			final var receiver = firstChild.getFirstChild();
			if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
				return null;
			final var innerDot = receiver.getFirstChild();
			if (innerDot == null || innerDot.getType() != TokenTypes.DOT)
				return null;
			final var innerMethodName = AstText.lastIdent(innerDot);
			if (innerMethodName == null)
				return null;
			methodNames.push(innerMethodName);
			final var innerArgs = receiver.findFirstToken(TokenTypes.ELIST);
			argCounts.push(innerArgs == null ? 0 : AstQuery.countArguments(innerArgs));
			current = receiver;
		}

		var type = baseType;
		while (!methodNames.isEmpty()) {
			final var fqcn = ReflectionUtil.resolveClassName(type, packageName, imports);
			if (fqcn == null)
				return null;
			type = ReflectionUtil.getMethodReturnTypeName(fqcn, methodNames.pop(), argCounts.pop());
			if (type == null)
				return null;
		}
		return type;
	}

	@CheckReturnValue
	@Nullable
	private static String getTypeName(@Nonnull DetailAST typeNode) {
		// descend through nested ARRAY_DECLARATORs; some AST shapes nest them
		// around the base type, others leave them as siblings of the base.
		var dimensions = 0;
		var inner = typeNode.getFirstChild();
		while (inner != null && inner.getType() == TokenTypes.ARRAY_DECLARATOR) {
			++dimensions;
			inner = inner.getFirstChild();
		}
		if (inner == null)
			return null;

		for (var sib = inner.getNextSibling(); sib != null; sib = sib.getNextSibling()) {
			if (sib.getType() == TokenTypes.ARRAY_DECLARATOR)
				++dimensions;
		}

		final var arraySuffix = "[]".repeat(dimensions);
		return switch (inner.getType()) {
			// dottedName walks the DOT chain only; FullIdent would also consume
			// sibling ARRAY_DECLARATOR/TYPE_ARGUMENTS and double-count brackets.
			case TokenTypes.DOT -> AstText.dottedName(inner) + arraySuffix;
			case TokenTypes.IDENT -> "var".equals(inner.getText()) ? null : inner.getText() + arraySuffix;
			// primitives have no methods, but primitive arrays are objects
			case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
			     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
			     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
			     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT ->
					dimensions == 0 ? null : inner.getText() + arraySuffix;
			default -> null;
		};
	}

	/** The type a bare name reads as. */
	@CheckReturnValue
	@Nullable
	private static String identTypeName(
			@Nonnull DetailAST ident,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		final var varName = ident.getText();

		final var inScopePattern = patternVariableInScopeTypeName(ident, varName);
		if (inScopePattern != null)
			return inScopePattern;

		final var binding = variableBinding(ident, varName);
		if (binding == null)
			return patternVariableTypeName(ident, varName);

		final var declarator = binding.declarator();
		final var declared = binding.typeName();
		if (declared != null) {
			// the ELLIPSIS sits beside the TYPE, so the declared name is the element type. A record
			// component is the exception: recordComponentBinding already applied the dimension
			return declarator != null && declarator.getType() != TokenTypes.RECORD_COMPONENT_DEF
					&& declarator.findFirstToken(TokenTypes.ELLIPSIS) != null
					? declared + "[]"
					: declared;
		}
		if (declarator == null)
			return null;

		final var varargs = varargsTypeName(declarator);
		if (varargs != null)
			return varargs;

		final var inferred = inferredVarTypeName(declarator, packageName, imports, memo);
		return inferred != null ? inferred : forEachElementTypeName(declarator, packageName, imports, memo);
	}

	@CheckReturnValue
	@Nullable
	private static String inferredVarTypeName(
			@Nonnull DetailAST declarator,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		final var assign = declarator.findFirstToken(TokenTypes.ASSIGN);
		final var initializer = assign == null ? null : assign.getFirstChild();
		return initializer == null ? null : expressionTypeName(initializer, packageName, imports, memo);
	}

	/**
	 * The same-file bodies {@code objBlock} inherits members from. Two owners name their supertype
	 * somewhere other than an extends or implements clause, so {@link #supertypeBodies} finds nothing
	 * for either: an anonymous class's body hangs off a {@code LITERAL_NEW} and inherits the type the
	 * {@code new} names, and an enum constant's body subclasses the enum whose body encloses it.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> inheritedBodies(@Nonnull DetailAST objBlock) {
		final var owner = objBlock.getParent();
		if (owner == null)
			return List.of();

		return switch (owner.getType()) {
			case TokenTypes.ENUM_CONSTANT_DEF -> {
				final var enumBody = owner.getParent();
				yield enumBody == null || enumBody.getType() != TokenTypes.OBJBLOCK
						? List.of()
						: List.of(enumBody);
			}
			case TokenTypes.LITERAL_NEW -> {
				final var body = sameFileTypeBody(owner, AstText.findNewClassName(owner));
				yield body == null ? List.of() : List.of(body);
			}
			default -> supertypeBodies(objBlock);
		};
	}

	/**
	 * The binding of a field named {@code varName} on a same-file supertype of the class whose body
	 * is {@code objBlock}, at any depth, or null.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding inheritedFieldBinding(@Nonnull DetailAST objBlock, @Nonnull String varName) {
		return inheritedFieldBinding(objBlock, varName, new HashSet<>());
	}

	/**
	 * A body {@code visited} refuses was already searched for this same name, so skipping it cannot
	 * lose an answer.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding inheritedFieldBinding(
			@Nonnull DetailAST objBlock,
			@Nonnull String varName,
			@Nonnull Set<DetailAST> visited
	) {
		for (var superBlock : inheritedBodies(objBlock)) {
			if (!visited.add(superBlock))
				continue;

			final var binding = bindingAmong(superBlock, varName);
			if (binding != null)
				return binding;

			final var deeper = inheritedFieldBinding(superBlock, varName, visited);
			if (deeper != null)
				return deeper;
		}
		return null;
	}

	/**
	 * The body of a member type named {@code name} that {@code objBlock} inherits. Supertype names
	 * are resolved with {@link #declaredTypeBody} rather than {@link #supertypeBodies}, because the
	 * latter resolves through {@link #sameFileTypeBody} and would re-enter this walk with a fresh
	 * visited set, which a same-file inheritance cycle turns into unbounded recursion.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST inheritedMemberType(
			@Nonnull DetailAST objBlock,
			@Nonnull String name,
			@Nonnull Set<DetailAST> visited
	) {
		final var typeDef = objBlock.getParent();
		for (var clause = typeDef == null ? null : typeDef.getFirstChild();
				clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var superName = clause.getFirstChild(); superName != null; superName = superName.getNextSibling()) {
				final var written = AstText.typeName(superName);
				final var superBody = written == null ? null : declaredTypeBody(typeDef, written);
				if (superBody == null || !visited.add(superBody))
					continue;

				for (var member = superBody.getFirstChild(); member != null; member = member.getNextSibling()) {
					final var ident = member.findFirstToken(TokenTypes.IDENT);
					if (ident != null && name.equals(ident.getText())) {
						final var body = member.findFirstToken(TokenTypes.OBJBLOCK);
						if (body != null)
							return body;
					}
				}

				final var deeper = inheritedMemberType(superBody, name, visited);
				if (deeper != null)
					return deeper;
			}
		}
		return null;
	}

	/**
	 * The declared return type of {@code methodName} at {@code arity} on a same-file supertype of
	 * the type whose body is {@code objBlock}, at any depth, or null.
	 */
	@CheckReturnValue
	@Nullable
	private static String inheritedMethodReturnType(
			@Nonnull DetailAST objBlock,
			@Nonnull String methodName,
			int arity,
			@Nonnull Set<DetailAST> visited
	) {
		for (var superBlock : inheritedBodies(objBlock)) {
			if (!visited.add(superBlock))
				continue;

			final var found = declaredMethodReturnTypeIn(superBlock, methodName, arity);
			if (found != null)
				return found;

			final var deeper = inheritedMethodReturnType(superBlock, methodName, arity, visited);
			if (deeper != null)
				return deeper;
		}
		return null;
	}

	/**
	 * The binding of {@code varName} among a lambda's parameters. A single implicitly typed
	 * parameter is a bare {@code IDENT} child with no {@code PARAMETERS} node at all.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding lambdaBinding(@Nonnull DetailAST lambda, @Nonnull String varName) {
		final var params = lambda.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null)
			return bindingAmong(params, varName);

		final var naked = lambda.getFirstChild();
		return naked != null && naked.getType() == TokenTypes.IDENT && varName.equals(naked.getText())
				? new Binding(null, null)
				: null;
	}

	/**
	 * The return type of {@code methodName} at {@code arity} on the type whose body is
	 * {@code objBlock}, whether declared there, implicit as a record component's accessor, or
	 * inherited from a same-file supertype. The method-side twin of {@link #fieldBindingIn}.
	 */
	@CheckReturnValue
	@Nullable
	private static String methodReturnTypeIn(@Nonnull DetailAST objBlock, @Nonnull String methodName, int arity) {
		final var own = declaredMethodReturnTypeIn(objBlock, methodName, arity);
		if (own != null)
			return own;

		final var component = arity == 0 ? recordComponentBinding(objBlock.getParent(), methodName) : null;
		return component != null
				? component.typeName()
				: inheritedMethodReturnType(objBlock, methodName, arity, new HashSet<>());
	}

	/**
	 * The type an {@code instanceof} pattern binds {@code varName} to at {@code from}, looking only
	 * at the conditions that govern it: the one an {@code if} tested to reach the branch {@code from}
	 * sits in, and the left operand of an {@code &&} whose right operand holds it.
	 *
	 * <p>Narrow on purpose, unlike {@link #patternVariableTypeName}. This answer outranks a field of
	 * the same name, so a pattern from an unrelated branch answering here would mistype every later
	 * read of that field.
	 */
	@CheckReturnValue
	@Nullable
	private static String patternVariableInScopeTypeName(@Nonnull DetailAST from, @Nonnull String varName) {
		var previous = from;
		for (var scope = from.getParent(); scope != null; previous = scope, scope = scope.getParent()) {
			final var matched = switch (scope.getType()) {
				// a parenthesized right operand is not the last child; the RPAREN is
				case TokenTypes.LAND -> previous == AstQuery.unwrapParensAndExprFromEnd(scope.getLastChild())
						? scope.getFirstChild()
						: null;

				case TokenTypes.LITERAL_IF -> {
					// the condition governs the taken branch, never itself and never the else. The
					// condition is identified by node, not by token type: a braceless body that is
					// an expression statement is an EXPR too, and must not read as the condition
					final var condition = scope.findFirstToken(TokenTypes.EXPR);
					yield previous == condition || previous.getType() == TokenTypes.LITERAL_ELSE
							? null
							: condition;
				}
				default -> null;
			};

			final var found = matched == null ? null : patternVariableTypeWhenTrue(matched, varName);
			if (found != null)
				return found;
		}
		return null;
	}

	/** The type a pattern declared anywhere inside {@code scope} binds {@code varName} to. */
	@CheckReturnValue
	@Nullable
	private static String patternVariableTypeIn(@Nonnull DetailAST scope, @Nonnull String varName) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(scope);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.PATTERN_VARIABLE_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				final var type = node.findFirstToken(TokenTypes.TYPE);
				if (ident != null && varName.equals(ident.getText()) && type != null)
					return getTypeName(type);
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return null;
	}

	/**
	 * The type an {@code instanceof} pattern binds {@code varName} to. Pattern variables are not
	 * declared in any enclosing scope's child list, so {@link #variableBinding} never sees them;
	 * the declaration hangs off the {@code instanceof} itself.
	 *
	 * <p>Scoping is not modelled: a name matching any enclosing pattern answers, even where Java
	 * would consider it out of scope. Over-answering is safe for callers that use the type to
	 * refuse a rewrite, and the alternative is re-implementing definite assignment.
	 */
	@CheckReturnValue
	@Nullable
	private static String patternVariableTypeName(@Nonnull DetailAST from, @Nonnull String varName) {
		for (var scope = from.getParent(); scope != null; scope = scope.getParent()) {
			final var found = patternVariableTypeIn(scope, varName);
			if (found != null)
				return found;
		}
		return null;
	}

	/**
	 * The type a pattern inside {@code condition} binds {@code varName} to where
	 * {@code condition} holding means the match was made. Only {@code &&} is followed, since it
	 * alone carries the match into its other operand; a single {@code &} does not, and leaves the
	 * name bound to a field instead.
	 */
	@CheckReturnValue
	@Nullable
	private static String patternVariableTypeWhenTrue(@Nonnull DetailAST condition, @Nonnull String varName) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(condition);
		while (!stack.isEmpty()) {
			final var node = AstQuery.unwrapParensAndExpr(stack.pop());
			if (node == null)
				continue;

			if (node.getType() == TokenTypes.LAND) {
				for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
					stack.push(child);
				continue;
			}
			if (node.getType() != TokenTypes.LITERAL_INSTANCEOF)
				continue;

			final var pattern = node.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF);
			final var ident = pattern == null ? null : pattern.findFirstToken(TokenTypes.IDENT);
			final var type = pattern == null ? null : pattern.findFirstToken(TokenTypes.TYPE);
			if (ident != null && varName.equals(ident.getText()) && type != null)
				return getTypeName(type);
		}
		return null;
	}

	/**
	 * The binding a record's component declaration gives {@code componentName}, or null when
	 * {@code recordDef} is not a record or declares no such component. The components hang off the
	 * {@code RECORD_DEF} rather than off its body, so {@link #bindingAmong} never reaches them.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding recordComponentBinding(@Nullable DetailAST recordDef, @Nonnull String componentName) {
		final var components = recordDef == null || recordDef.getType() != TokenTypes.RECORD_DEF
				? null
				: recordDef.findFirstToken(TokenTypes.RECORD_COMPONENTS);
		if (components == null)
			return null;

		for (var child = components.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.RECORD_COMPONENT_DEF)
				continue;

			final var ident = child.findFirstToken(TokenTypes.IDENT);
			final var type = child.findFirstToken(TokenTypes.TYPE);
			if (ident != null && componentName.equals(ident.getText()) && type != null) {
				// varargsTypeName appends one dimension to the element it reads, so it undercounts a
				// component that declares its own, as `char[]... rows` does. Ask getTypeName first,
				// which counts them all, and keep varargsTypeName for the scalar element it cannot name
				final var declared = getTypeName(type);
				if (declared == null)
					return new Binding(varargsTypeName(child), child);

				return new Binding(
						child.findFirstToken(TokenTypes.ELLIPSIS) == null ? declared : declared + "[]",
						child
				);
			}
		}
		return null;
	}

	/**
	 * The type of the record component {@code componentName} on the same-file record
	 * {@code recordType}, which is also the return type of its implicit accessor. Null when the
	 * type is not a record declared here, declares no such component, or spells the component with
	 * a type {@link #recordComponentBinding} cannot name.
	 *
	 * <p>An explicitly written accessor is not handled here: it is a {@code METHOD_DEF} like any
	 * other, so {@link #resolveSameFileMethodReturnType} already answers for it.
	 */
	@CheckReturnValue
	@Nullable
	private static String recordComponentTypeName(
			@Nonnull DetailAST scope,
			@Nonnull String recordType,
			@Nonnull String componentName
	) {
		final var binding = recordComponentBinding(sameFileClassDef(scope, recordType), componentName);
		return binding == null ? null : binding.typeName();
	}

	/**
	 * Resolve the type of a field declared on a same-file class definition.
	 * If {@code className} is null, the field is read off the body {@code this} names at
	 * {@code scope}; otherwise locates the named type within the same compilation unit and
	 * resolves the field there.
	 *
	 * <p>Only the nearest body answers, because that is the one {@code this} names. A binding it
	 * supplies stands even when the type is unspellable: looking further out would report a different
	 * field, with an {@code int total} hiding a {@code String total} reading back as a String.
	 */
	@CheckReturnValue
	@Nullable
	public static String resolveSameFileFieldType(@Nonnull DetailAST scope, @Nullable String className, @Nonnull String fieldName) {
		if (className != null) {
			// the owner may be written qualified, as `Outer.Inner`, which only this form resolves
			final var classDef = sameFileClassDef(scope, className);
			final var named = classDef == null ? null : classDef.findFirstToken(TokenTypes.OBJBLOCK);
			final var namedBinding = named == null ? null : fieldBindingIn(named, fieldName);
			return namedBinding == null ? null : namedBinding.typeName();
		}

		final var body = enclosingBody(scope);
		final var binding = body == null ? null : fieldBindingIn(body, fieldName);
		return binding == null ? null : binding.typeName();
	}

	/**
	 * Resolve the declared return type of a method named {@code methodName}
	 * with parameter count {@code arity} on any enclosing same-file class, or on a same-file
	 * supertype of one.
	 * Returns null when no overload at that arity has a spellable return type, or
	 * when the overloads at that arity return different types, to avoid corrupting
	 * downstream type inference.
	 *
	 * <p>The frames are class bodies rather than type definitions, which is what brings an anonymous
	 * class's and an enum constant's own bodies into the walk: each declares members that hide a
	 * same-named one on the class enclosing it, yet neither hangs off a type definition.
	 */
	@CheckReturnValue
	@Nullable
	public static String resolveSameFileMethodReturnType(@Nonnull DetailAST scope, @Nonnull String methodName, int arity) {
		for (var body = enclosingBody(scope); body != null; body = enclosingBody(body)) {
			final var found = methodReturnTypeIn(body, methodName, arity);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	public static String resolveVariableType(@Nonnull DetailAST from, @Nonnull String varName) {
		final var binding = variableBinding(from, varName);
		return binding == null ? null : binding.typeName();
	}

	/**
	 * The same-file type {@code className} resolves to from {@code scope}, or null when the
	 * compilation unit declares no such type. The name may be qualified ({@code Outer.Box}), in
	 * which case each segment after the first is looked up inside the previous one.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST sameFileClassDef(@Nonnull DetailAST scope, @Nonnull String className) {
		final var segments = className.split("\\.");
		if (segments.length == 0)
			return null;

		var classDef = findSameFileClassDef(scope, segments[0]);
		for (var i = 1; i < segments.length && classDef != null; ++i)
			classDef = findInnerClassDef(classDef, segments[i]);
		return classDef;
	}

	/**
	 * The body of the same-file type {@code name} names from {@code scope}, resolved the way Java
	 * scoping does: the innermost enclosing scope that declares the name wins, so a local class
	 * shadows a same-named type declared further out. A name no enclosing scope declares may still
	 * be a member type inherited from a supertype, which is in scope without appearing in any frame.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST sameFileTypeBody(@Nonnull DetailAST scope, @Nullable String name) {
		if (name == null)
			return null;

		final var declared = declaredTypeBody(scope, name);
		if (declared != null)
			return declared;

		for (var frame = scope; frame != null; frame = frame.getParent()) {
			final var inherited = frame.getType() == TokenTypes.OBJBLOCK
					? inheritedMemberType(frame, name, new HashSet<>())
					: null;
			if (inherited != null)
				return inherited;
		}
		return null;
	}

	/**
	 * The bodies of {@code objBlock}'s direct supertypes that are declared in the same file.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<DetailAST> supertypeBodies(@Nonnull DetailAST objBlock) {
		final var typeDef = objBlock.getParent();
		if (typeDef == null)
			return List.of();

		final var bodies = new ArrayList<DetailAST>();
		for (var clause = typeDef.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var name = clause.getFirstChild(); name != null; name = name.getNextSibling()) {
				final var superBlock = sameFileTypeBody(typeDef, AstText.typeName(name));
				if (superBlock != null)
					bodies.add(superBlock);
			}
		}
		return bodies;
	}

	/**
	 * The type both branches of {@code question} evaluate to, or null when either is unknown or
	 * they disagree. Answering with one branch's type would describe a value the other branch can
	 * also produce, so a disagreement is "unknown" rather than a pick.
	 */
	@CheckReturnValue
	@Nullable
	private static String ternaryTypeName(
			@Nonnull DetailAST question,
			@Nullable String packageName,
			@Nonnull Set<String> imports,
			@Nonnull Map<DetailAST, Optional<String>> memo
	) {
		// a grouping paren is a sibling rather than a wrapper, so a parenthesized condition or branch
		// occupies the slot its operand would; the COLON is the one landmark neither can displace
		final var colon = question.findFirstToken(TokenTypes.COLON);
		final var whenTrue = colon == null
				? null
				: AstQuery.unwrapParensAndExprFromEnd(colon.getPreviousSibling());
		final var whenFalse = AstQuery.unwrapParensAndExprFromEnd(question.getLastChild());
		if (whenTrue == null || whenFalse == null || whenFalse == whenTrue)
			return null;

		final var trueType = expressionTypeName(whenTrue, packageName, imports, memo);
		return trueType != null && trueType.equals(expressionTypeName(whenFalse, packageName, imports, memo))
				? trueType
				: null;
	}

	/**
	 * The array type a varargs declaration whose element is a <em>scalar primitive</em> takes:
	 * {@code char... cs} is a {@code char[]}, but a bare primitive carries no dimension, so
	 * {@link #getTypeName} answers null and the {@code ELLIPSIS} is the only evidence left. Null for
	 * every other element, an array one included: one {@code []} is all this appends, so
	 * {@code char[]... rows} would read back a dimension short. Null when {@code declarator} is not
	 * varargs.
	 */
	@CheckReturnValue
	@Nullable
	private static String varargsTypeName(@Nonnull DetailAST declarator) {
		if (declarator.findFirstToken(TokenTypes.ELLIPSIS) == null)
			return null;

		final var type = declarator.findFirstToken(TokenTypes.TYPE);
		final var element = type == null ? null : type.getFirstChild();
		if (element == null)
			return null;

		return switch (element.getType()) {
			case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
			     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
			     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
			     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT -> element.getText() + "[]";
			default -> null;
		};
	}

	/**
	 * The binding {@code varName} has where {@code from} sits, walking outwards through the
	 * enclosing scopes, or null when nothing binds it.
	 */
	@CheckReturnValue
	@Nullable
	private static Binding variableBinding(@Nonnull DetailAST from, @Nonnull String varName) {
		var previous = from;
		for (var scope = from.getParent(); scope != null; previous = scope, scope = scope.getParent()) {
			final var binding = switch (scope.getType()) {
				case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF ->
						bindingAmong(scope.findFirstToken(TokenTypes.PARAMETERS), varName);

				case TokenTypes.LAMBDA -> lambdaBinding(scope, varName);
				case TokenTypes.LITERAL_CATCH, TokenTypes.SLIST -> bindingAmong(scope, varName);

				case TokenTypes.LITERAL_FOR -> {
					final var each = bindingAmong(scope.findFirstToken(TokenTypes.FOR_EACH_CLAUSE), varName);
					yield each != null ? each : bindingAmong(scope.findFirstToken(TokenTypes.FOR_INIT), varName);
				}
				case TokenTypes.LITERAL_TRY -> {
					// a resource is in scope only inside the try block and the specification
					final var inResourceScope = previous.getType() == TokenTypes.SLIST
							|| previous.getType() == TokenTypes.RESOURCE_SPECIFICATION;
					final var spec = inResourceScope ? scope.findFirstToken(TokenTypes.RESOURCE_SPECIFICATION) : null;
					yield spec == null ? null : bindingAmong(spec.findFirstToken(TokenTypes.RESOURCES), varName);
				}
				case TokenTypes.OBJBLOCK -> fieldBindingIn(scope, varName);
				default -> null;
			};

			if (binding != null)
				return binding;
		}
		return null;
	}

	/**
	 * Whether {@code varName} resolves to a declarator in scope at {@code from}. A non-null
	 * {@link #resolveVariableType} is not the same question: that answers null both when nothing
	 * binds the name and when a binding exists whose declarator spells no readable type, as
	 * {@code var} does. A caller that must not act on {@link #expressionTypeName}'s scope-blind
	 * pattern fallback wants this one, or it refuses every {@code var} as well.
	 */
	@CheckReturnValue
	public static boolean variableIsBound(@Nonnull DetailAST from, @Nonnull String varName) {
		return variableBinding(from, varName) != null;
	}

	/**
	 * Whether {@code varName} is declared as a varargs parameter. The {@code ELLIPSIS} sits beside
	 * the {@code TYPE} rather than inside it, so {@link #resolveVariableType} reports {@code String}
	 * for {@code String... s} and a caller needing the array-ness has to ask here.
	 */
	@CheckReturnValue
	public static boolean variableIsVarargs(@Nonnull DetailAST from, @Nonnull String varName) {
		final var binding = variableBinding(from, varName);
		return binding != null && binding.declarator() != null
				&& binding.declarator().findFirstToken(TokenTypes.ELLIPSIS) != null;
	}

	/**
	 * The type name of the {@code index}-th type argument of {@code varName}'s declared type, or
	 * null when the declaration spells no such argument. {@code Map<String, List<String>> m} gives
	 * {@code List} at index 1.
	 */
	@CheckReturnValue
	@Nullable
	public static String variableTypeArgumentName(
			@Nonnull DetailAST from,
			@Nonnull String varName,
			int index
	) {
		final var binding = variableBinding(from, varName);
		if (binding == null || binding.declarator() == null)
			return null;

		final var type = binding.declarator().findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		// a qualified type parks its arguments on the DOT rather than the TYPE
		final var base = type.findFirstToken(TokenTypes.DOT);
		final var owner = base == null ? type : base;
		final var typeArguments = owner.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
		if (typeArguments == null)
			return null;

		var seen = 0;
		for (var arg = typeArguments.getFirstChild(); arg != null; arg = arg.getNextSibling()) {
			if (arg.getType() != TokenTypes.TYPE_ARGUMENT)
				continue;
			if (seen++ == index)
				return getTypeName(arg);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String variableTypeName(@Nonnull DetailAST node, @Nonnull String varName) {
		if (node.getType() != TokenTypes.VARIABLE_DEF && node.getType() != TokenTypes.PARAMETER_DEF
				&& node.getType() != TokenTypes.RESOURCE)
			return null;

		final var ident = node.findFirstToken(TokenTypes.IDENT);
		if (ident == null || !varName.equals(ident.getText()))
			return null;

		final var type = node.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		final var typeName = getTypeName(type);
		if (typeName != null)
			return typeName;

		if (node.getType() == TokenTypes.VARIABLE_DEF || node.getType() == TokenTypes.RESOURCE) {
			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				return null;
			final var assignChild = assign.getFirstChild();
			if (assignChild == null)
				return null;
			final var init = assignChild.getType() == TokenTypes.EXPR ? assignChild.getFirstChild() : assignChild;
			if (init == null)
				return null;
			if (init.getType() == TokenTypes.STRING_LITERAL)
				return "String";
			if (init.getType() == TokenTypes.METHOD_CALL) {
				final var receiver = init.getFirstChild();
				if (receiver != null && receiver.getType() == TokenTypes.IDENT) {
					final var elist = init.findFirstToken(TokenTypes.ELIST);
					var arity = 0;
					if (elist != null) {
						for (var c = elist.getFirstChild(); c != null; c = c.getNextSibling()) {
							if (c.getType() != TokenTypes.COMMA)
								++arity;
						}
					}
					return resolveSameFileMethodReturnType(node, receiver.getText(), arity);
				}
				return null;
			}
			if (init.getType() != TokenTypes.LITERAL_NEW)
				return null;

			var dimensions = 0;
			for (var child = init.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
					++dimensions;
			}

			final var className = AstText.findNewClassName(init);
			if (className != null)
				return className + "[]".repeat(dimensions);
			for (var child = init.getFirstChild(); child != null; child = child.getNextSibling()) {
				switch (child.getType()) {
					case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
					     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
					     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
					     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT -> {
						return child.getText() + "[]".repeat(dimensions);
					}
					default -> {
					}
				}
			}
			return null;
		}
		return null;
	}

	private AstResolve() {
	}
}