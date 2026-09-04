package com.etk2000.checkstyle;

import static com.etk2000.checkstyle.ReflectionUtil.AMBIGUOUS_TYPE_KEY;
import static com.etk2000.checkstyle.ReflectionUtil.UNKNOWN_TYPE_KEY;

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
import java.util.StringJoiner;
import java.util.function.Function;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The exact static type of an expression, as far as one file's syntax tree and the classpath prove
 * it and no further: what a rewrite that depends on that type, as {@code var} does, may rest on. A
 * type is answered as a key:
 * <ul>
 *     <li>a class on the classpath is its binary name ({@code java.util.Map$Entry}), a type this
 *     file declares is {@code #} and the position of its body, and a type variable this file declares
 *     is {@code '} and the position of its name;</li>
 *     <li>type arguments follow between angle brackets, separated by commas, and every array
 *     dimension adds a {@code []};</li>
 *     <li>{@link ReflectionUtil#UNKNOWN_TYPE_KEY} stands for any part that is not proven, as a
 *     diamond's arguments or a method's own type variable are.</li>
 * </ul>
 * A key is exact when no part of it is unknown. A name nothing resolves has no key at all.
 *
 * <p>A call proves its type only through a receiver whose own type is proven, a written type, a
 * class name, {@code this}, {@code super} or a {@code new}, and only when every method it may bind to
 * returns that one type. One with a poly expression for an argument is refused. One instance serves
 * one file: the positions it hands out name that file's declarations.
 */
final class ProvenTypes {
	/**
	 * The return types of the methods of one name and arity a call in a hierarchy may bind to,
	 * whether any method of the name was seen at all, and whether part of the hierarchy could not be
	 * read, which makes the rest no proof.
	 */
	private record Candidates(@Nonnull List<String> keys, boolean named, boolean unknown) {}

	/** One method {@link #candidates} found, and how many supertype levels down. */
	private record Found(@Nonnull ExactMethod method, int depth) {}

	/** A body {@link #candidates} still has to read, with what its type parameters are bound to. */
	private record Level(@Nonnull DetailAST body, @Nonnull Map<String, String> bindings, int depth) {}

	/** What a call's or a field access's qualifier denotes, and whether that is a type rather than a value. */
	private record Qualifier(@Nonnull String key, boolean isType) {}

	/**
	 * How deep {@link #keyOf} recurses before it answers nothing: a chain of that many links
	 * ({@code a.b().c()...}, {@code var a1 = a0; var a2 = a1; ...}) proves nothing a shorter one
	 * would not, and a deeper one would overflow the stack.
	 */
	static final int MAX_DEPTH = 256;
	/**
	 * The longest key worth carrying. A type argument that doubles at every link
	 * ({@code C1<T> extends C0<Map<T, T>>}) grows a key exponentially, so a longer one reads as
	 * {@link ReflectionUtil#UNKNOWN_TYPE_KEY} instead.
	 */
	static final int MAX_KEY_LENGTH = 4096;
	private static final Set<String> PRIMITIVES = Set.of(
			"boolean", "byte", "char", "double", "float", "int", "long", "short", "void"
	);
	private static final String ITERATOR_FQCN = "java.util.Iterator";
	private static final String OBJECT_FQCN = "java.lang.Object";
	static final String STRING_FQCN = "java.lang.String";

	/** The single key every one of {@code keys} is, or null when there are none or they differ. */
	@CheckReturnValue
	@Nullable
	private static String agree(@Nullable List<String> keys) {
		if (keys == null || keys.isEmpty())
			return null;

		final var first = keys.getFirst();
		for (var key : keys) {
			if (!first.equals(key))
				return null;
		}
		return first;
	}

	/** The type arguments {@code key} writes on its outermost type, in order, or null when it writes none. */
	@CheckReturnValue
	@Nullable
	static List<String> arguments(@Nonnull String key) {
		final var open = key.indexOf('<');
		if (open < 0)
			return null;

		final var found = new ArrayList<String>();
		var depth = 0;
		var start = open + 1;
		for (var i = start; i < key.length(); ++i) {
			switch (key.charAt(i)) {
				case ',' -> {
					if (depth == 0) {
						found.add(key.substring(start, i));
						start = i + 1;
					}
				}
				case '<' -> ++depth;
				case '>' -> {
					if (depth == 0) {
						found.add(key.substring(start, i));
						return found;
					}
					--depth;
				}
				default -> {
				}
			}
		}
		return found;
	}

	/** The class or type variable {@code key} names, without its type arguments or dimensions. */
	@CheckReturnValue
	@Nonnull
	private static String base(@Nonnull String key) {
		var end = 0;
		while (end < key.length() && key.charAt(end) != '<' && key.charAt(end) != '[')
			++end;
		return key.substring(0, end);
	}

	/**
	 * The type a call of {@code methodName} with {@code arity} arguments returns through the
	 * classpath class {@code fqcn} written with {@code typeArguments}, or null unless every method
	 * it may bind to returns one type.
	 */
	@CheckReturnValue
	@Nullable
	private static String classpathCallKey(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int arity,
			@Nullable List<String> typeArguments,
			boolean statics
	) {
		final var methods = ReflectionUtil.exactMethods(fqcn, methodName, arity, typeArguments, statics, false);
		return methods == null ? null : agree(methods.stream().map(ExactMethod::returnType).toList());
	}

	/**
	 * Every arm of a conditional or switch-expression {@code value}, nested ones flattened, or null
	 * when one cannot be read: a parenthesized arm, which a rewrite of the arms cannot reach, or a
	 * colon-form switch, which yields through statements.
	 */
	@CheckReturnValue
	@Nullable
	static List<DetailAST> conditionalArms(@Nonnull DetailAST value) {
		final var arms = new ArrayList<DetailAST>();
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(value);
		while (!pending.isEmpty()) {
			final var popped = pending.pop();
			final var arm = popped.getType() == TokenTypes.EXPR ? popped.getFirstChild() : popped;
			if (arm == null || arm.getType() == TokenTypes.LPAREN)
				return null;

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
			if (arm.getType() != TokenTypes.LITERAL_SWITCH) {
				arms.add(arm);
				continue;
			}

			// only the arrow form carries its value as the sibling of the '->'
			var sawRule = false;
			for (var rule = arm.getFirstChild(); rule != null; rule = rule.getNextSibling()) {
				if (rule.getType() != TokenTypes.SWITCH_RULE)
					continue;
				sawRule = true;
				final var arrow = rule.findFirstToken(TokenTypes.LAMBDA);
				final var body = arrow == null ? null : arrow.getNextSibling();
				if (body == null || body.getType() == TokenTypes.SLIST)
					return null;
				pending.push(body);
			}
			if (!sawRule)
				return null;
		}
		return arms;
	}

	/** The body of the type named {@code name} that {@code frame}, a block, a body or the file, itself declares, or null. */
	@CheckReturnValue
	@Nullable
	private static DetailAST declaredMemberType(@Nonnull DetailAST frame, @Nonnull String name) {
		for (var child = frame.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			final var isTypeDef = type == TokenTypes.ANNOTATION_DEF || type == TokenTypes.CLASS_DEF || type == TokenTypes.ENUM_DEF
					|| type == TokenTypes.INTERFACE_DEF || type == TokenTypes.RECORD_DEF;
			final var ident = isTypeDef ? child.findFirstToken(TokenTypes.IDENT) : null;
			if (ident != null && name.equals(ident.getText()))
				return child.findFirstToken(TokenTypes.OBJBLOCK);
		}
		return null;
	}

	/** Whether {@code node} sits inside {@code outer}. */
	@CheckReturnValue
	private static boolean encloses(@Nonnull DetailAST outer, @Nonnull DetailAST node) {
		for (var parent = node.getParent(); parent != null; parent = parent.getParent()) {
			if (parent == outer)
				return true;
		}
		return false;
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

	/** {@code key} without its outermost type arguments: {@code java.util.List<java.lang.String>[]} is {@code java.util.List[]}. */
	@CheckReturnValue
	@Nonnull
	static String erasure(@Nonnull String key) {
		final var open = key.indexOf('<');
		if (open < 0)
			return key;

		var depth = 0;
		for (var i = open; i < key.length(); ++i) {
			if (key.charAt(i) == '<')
				++depth;
			else if (key.charAt(i) == '>') {
				--depth;
				if (depth == 0)
					return key.substring(0, open) + key.substring(i + 1);
			}
		}
		return key.substring(0, open);
	}

	/**
	 * A resolver over {@code root}'s own import and package declarations, for a caller that has a
	 * parsed file but no check instance. {@link AbstractResolvingCheck} builds the same scope and
	 * memoizes it across a whole file's tokens, so a check with a per-file lifecycle should use that
	 * instead; this exists for a {@code static} classifier, which is handed a root and nothing else.
	 */
	@CheckReturnValue
	@Nonnull
	static ProvenTypes forRoot(@Nonnull DetailAST root) {
		final var imports = new HashSet<String>();
		String packageName = null;
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.IMPORT -> {
					final var imported = FullIdent.createFullIdentBelow(child).getText();
					if (!imported.isEmpty())
						imports.add(imported);
				}
				case TokenTypes.PACKAGE_DEF -> packageName = AstText.getPackageName(child);
				default -> {
				}
			}
		}
		final var scope = packageName;
		return new ProvenTypes(name -> ReflectionUtil.resolveClassName(name, scope, imports), scope);
	}

	/** Whether {@code key} is a key with no unknown part. */
	@CheckReturnValue
	static boolean isExact(@Nullable String key) {
		return key != null && !key.contains(UNKNOWN_TYPE_KEY);
	}

	/**
	 * Whether the type whose body is {@code body} is an inner class (JLS 8.1.3) of a generic class: a
	 * member class that is not static, enclosed by a generic class directly or through further inner
	 * classes. A member of an interface is static, as is every enum, record and interface, and a
	 * local or anonymous class is no member at all.
	 */
	@CheckReturnValue
	private static boolean isInnerClassOfGeneric(@Nonnull DetailAST body) {
		var owner = body.getParent();
		while (owner.getType() == TokenTypes.CLASS_DEF) {
			final var modifiers = owner.findFirstToken(TokenTypes.MODIFIERS);
			final var enclosingBody = owner.getParent();
			if (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null
					|| enclosingBody == null || enclosingBody.getType() != TokenTypes.OBJBLOCK)
				return false;

			final var enclosing = enclosingBody.getParent();
			if (enclosing.getType() == TokenTypes.ANNOTATION_DEF || enclosing.getType() == TokenTypes.INTERFACE_DEF)
				return false;
			if (enclosing.findFirstToken(TokenTypes.TYPE_PARAMETERS) != null)
				return true;
			owner = enclosing;
		}
		return false;
	}

	/**
	 * Whether {@code cast} names more than one bound ({@code (Runnable & Serializable) () -> {}}): its
	 * type is the intersection, which no one key carries, so the first bound proves nothing.
	 */
	@CheckReturnValue
	static boolean isIntersectionCast(@Nonnull DetailAST cast) {
		return cast.findFirstToken(TokenTypes.TYPE_EXTENSION_AND) != null;
	}

	/** Whether a nearer method among {@code found} overrides {@code candidate}: the same exact parameter types. */
	@CheckReturnValue
	private static boolean isOverridden(@Nonnull Found candidate, @Nonnull List<Found> found) {
		final var parameters = candidate.method().parameters();
		if (!parameters.stream().allMatch(ProvenTypes::isExact))
			return false;

		for (var nearer : found) {
			if (nearer.depth() < candidate.depth() && nearer.method().parameters().equals(parameters))
				return true;
		}
		return false;
	}

	/**
	 * Whether a pattern of a method, constructor, initializer, field initializer or enum constant's
	 * arguments enclosing {@code at} declares a variable named {@code name},
	 * {@code patternsOf} giving the names a member's
	 * patterns declare ({@link #patternNames}). Only an if-then branch and an {@code &&}'s right
	 * operand are modelled ({@link AstResolve#patternVariableInScope}); anywhere else (after a negated
	 * {@code if} that returns, in its {@code else}, a {@code case}, a conditional's arm, an
	 * {@code ||}'s right operand) such a pattern may be the variable in scope, ahead of any field of
	 * the name.
	 */
	@CheckReturnValue
	static boolean mayBindPattern(
			@Nonnull DetailAST at,
			@Nonnull String name,
			@Nonnull Function<DetailAST, Set<String>> patternsOf
	) {
		for (var node = at; node.getParent() != null; node = node.getParent()) {
			final var type = node.getType();
			final var isMember = node.getParent().getType() == TokenTypes.OBJBLOCK && type != TokenTypes.ANNOTATION_DEF
					&& type != TokenTypes.CLASS_DEF && type != TokenTypes.ENUM_DEF
					&& type != TokenTypes.INTERFACE_DEF && type != TokenTypes.RECORD_DEF;
			// an enum constant's body methods are members of their own, so only its arguments count here
			final var scope = type == TokenTypes.ENUM_CONSTANT_DEF ? node.findFirstToken(TokenTypes.ELIST) : node;
			if (isMember && scope != null && patternsOf.apply(scope).contains(name))
				return true;
		}
		return false;
	}

	/** The names every {@code instanceof} or record pattern inside {@code scope} declares. */
	@CheckReturnValue
	@Nonnull
	static Set<String> patternNames(@Nonnull DetailAST scope) {
		final var names = new HashSet<String>();
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(scope);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			final var ident = node.getType() == TokenTypes.PATTERN_VARIABLE_DEF ? node.findFirstToken(TokenTypes.IDENT) : null;
			if (ident != null)
				names.add(ident.getText());
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
		return names;
	}

	/**
	 * {@code key} with every type variable read through {@code bindings}. One {@code bindings} does
	 * not bind is unknown: a member read through a receiver only knows the type variables the
	 * receiver's type binds.
	 */
	@CheckReturnValue
	@Nonnull
	private static String substitute(@Nonnull String key, @Nonnull Map<String, String> bindings) {
		if (key.indexOf('\'') < 0)
			return key;

		final var substituted = new StringBuilder();
		var i = 0;
		while (i < key.length()) {
			if (key.charAt(i) != '\'') {
				substituted.append(key.charAt(i));
				++i;
				continue;
			}

			var end = i + 1;
			while (end < key.length() && "<>,[".indexOf(key.charAt(end)) < 0)
				++end;
			substituted.append(bindings.getOrDefault(key.substring(i, end), UNKNOWN_TYPE_KEY));
			if (substituted.length() > MAX_KEY_LENGTH)
				return UNKNOWN_TYPE_KEY;
			i = end;
		}
		return substituted.toString();
	}

	/**
	 * Whether the method {@code methodDef} takes a call of {@code arity} arguments. A receiver
	 * parameter ({@code Foo this}) takes none.
	 */
	@CheckReturnValue
	private static boolean takesArity(@Nonnull DetailAST methodDef, int arity) {
		final var parameters = methodDef.findFirstToken(TokenTypes.PARAMETERS);
		var count = 0;
		var varargs = false;
		for (var parameter = parameters == null ? null : parameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
			if (parameter.getType() == TokenTypes.PARAMETER_DEF && parameter.findFirstToken(TokenTypes.IDENT) != null) {
				++count;
				varargs = parameter.findFirstToken(TokenTypes.ELLIPSIS) != null;
			}
		}
		return varargs ? arity >= count - 1 : arity == count;
	}

	private final Map<DetailAST, Set<String>> memberPatterns = new HashMap<>();
	private final Map<List<DetailAST>, Boolean> inheritsOtherwise = new HashMap<>();
	private final Map<String, Boolean> classpathFields = new HashMap<>();
	private final Map<String, DetailAST> declarations = new HashMap<>();
	private final Map<String, String> memberClasses = new HashMap<>();
	private final Set<DetailAST> inProgress = new HashSet<>();
	private final Set<DetailAST> readingInheritance = new HashSet<>();
	private final Set<DetailAST> resolvingMembers = new HashSet<>();

	@Nonnull
	private final Function<String, String> resolver;
	@Nullable
	private final String packageName;

	private int depth;

	/**
	 * @param resolver    the qualified name a simple class name resolves to in the file, or null
	 * @param packageName the file's package, which decides the package-private member types a
	 *                    classpath supertype passes down; null for the default package
	 */
	ProvenTypes(@Nonnull Function<String, String> resolver, @Nullable String packageName) {
		this.packageName = packageName;
		this.resolver = resolver;
	}

	@CheckReturnValue
	@Nullable
	private String armsKey(@Nonnull DetailAST conditional) {
		final var arms = conditionalArms(conditional);
		if (arms == null)
			return null;

		final var keys = new ArrayList<String>();
		for (var arm : arms)
			keys.add(keyOf(arm));
		return keys.contains(null) ? null : agree(keys);
	}

	/**
	 * The type a bare call binds: what the methods of the innermost enclosing class with a method
	 * of that name return (JLS 15.12.1), or failing any, the ones the static imports bring in.
	 */
	@CheckReturnValue
	@Nullable
	private String bareCallKey(@Nonnull DetailAST call, @Nonnull String methodName, int arity) {
		for (var body = enclosingBody(call); body != null; body = enclosingBody(body)) {
			final var found = candidates(body, ownBindings(body), methodName, arity, false, false);
			if (found.unknown())
				return null;
			if (found.named())
				return agree(found.keys());
		}

		final var singles = new ArrayList<String>();
		final var onDemand = new ArrayList<String>();
		for (var imported : PreferExactAssertionCheck.staticImportFqns(call)) {
			final var owner = PreferExactAssertionCheck.staticImportClassFqn(imported);
			final var member = PreferExactAssertionCheck.staticImportMember(imported);
			if (owner == null)
				continue;
			if (member == null)
				onDemand.add(owner);
			else if (member.equals(methodName))
				singles.add(owner);
		}

		final var keys = new ArrayList<String>();
		for (var owner : singles.isEmpty() ? onDemand : singles) {
			final var found = ReflectionUtil.exactMethods(owner, methodName, arity, null, true, false);
			if (found == null)
				return null;
			found.forEach(method -> keys.add(method.returnType()));
		}
		return agree(keys);
	}

	/**
	 * What the type parameters of the type whose body is {@code body} read as through a type
	 * written with {@code arguments}: each the argument at its index, or unknown when the counts
	 * differ, as for a raw type or a diamond. A type parameter of a declaration enclosing the type
	 * is the same one inside it, so it reads as itself.
	 */
	@CheckReturnValue
	@Nonnull
	private Map<String, String> bindings(@Nonnull DetailAST body, @Nullable List<String> arguments) {
		final var parameters = typeParameterTokens(body);
		final var bindings = new HashMap<String, String>();
		for (var scope = body.getParent().getParent(); scope != null; scope = scope.getParent()) {
			final var enclosing = scope.findFirstToken(TokenTypes.TYPE_PARAMETERS);
			for (var parameter = enclosing == null ? null : enclosing.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
				final var ident = parameter.getType() == TokenTypes.TYPE_PARAMETER ? parameter.findFirstToken(TokenTypes.IDENT) : null;
				if (ident != null)
					bindings.putIfAbsent(position("'", ident), position("'", ident));
			}
		}
		for (var i = 0; i < parameters.size(); ++i) {
			final var bound = arguments != null && arguments.size() == parameters.size() ? arguments.get(i) : UNKNOWN_TYPE_KEY;
			bindings.put(parameters.get(i), bound);
		}
		return bindings;
	}

	@CheckReturnValue
	@Nullable
	private String callKey(@Nonnull DetailAST call) {
		final var methodName = AstQuery.getMethodName(call);
		final var arguments = call.findFirstToken(TokenTypes.ELIST);
		if (methodName == null || arguments == null || hasPolyArgument(arguments))
			return null;

		final var arity = AstQuery.countArguments(arguments);
		final var callee = call.getFirstChild();
		if (callee.getType() == TokenTypes.IDENT)
			return bareCallKey(call, methodName, arity);
		// explicit type arguments pick the method's own type variables, which no key here carries
		if (callee.getType() != TokenTypes.DOT || callee.findFirstToken(TokenTypes.TYPE_ARGUMENTS) != null)
			return null;

		final var receiver = callee.getFirstChild();
		if (receiver.getType() == TokenTypes.LITERAL_SUPER) {
			final var body = enclosingBody(call);
			final var found = body == null ? null : candidates(body, ownBindings(body), methodName, arity, false, true);
			return found == null || found.unknown() ? null : agree(found.keys());
		}

		final var qualifier = qualifier(receiver, call);
		return qualifier == null ? null : memberCallKey(qualifier.key(), methodName, arity, qualifier.isType());
	}

	/**
	 * What a call of {@code methodName} with {@code arity} arguments may return when it binds in the
	 * type whose body is {@code body}, its type parameters read through {@code bindings}: the
	 * methods it declares, a record component's implicit accessor, an enum's implicit
	 * {@code values()} and {@code valueOf(String)}, and what it inherits, through same-file
	 * supertypes and classpath ones alike, {@code Object} included. Only static ones when
	 * {@code statics}, and only inherited ones when {@code skipOwn}, as for {@code super.m()}. A
	 * method a nearer one overrides, the same exact parameter types, is no candidate, so a covariant
	 * override is read at its own return type. A call without arguments binds a fixed-arity method
	 * whenever there is one (JLS 15.12.2.2), so a varargs one is then no candidate either. A supertype
	 * nothing resolves makes the answer unknown, since it may declare one.
	 */
	@CheckReturnValue
	@Nonnull
	private Candidates candidates(
			@Nonnull DetailAST body,
			@Nonnull Map<String, String> bindings,
			@Nonnull String methodName,
			int arity,
			boolean statics,
			boolean skipOwn
	) {
		final var found = new ArrayList<Found>();
		var named = false;
		final var visited = new HashSet<DetailAST>();
		final var pending = new ArrayDeque<Level>();
		pending.add(new Level(body, bindings, 0));
		while (!pending.isEmpty()) {
			final var level = pending.poll();
			if (!visited.add(level.body()))
				continue;

			if (level.depth() > 0 || !skipOwn)
				named |= ownCandidates(level, methodName, arity, statics, found);

			for (var supertype : supertypeKeys(level.body())) {
				if (supertype == null)
					return new Candidates(List.of(), named, true);

				final var bound = substitute(supertype, level.bindings());
				final var base = base(bound);
				final var superBody = base.startsWith("#") ? declarations.get(base) : null;
				if (superBody != null) {
					pending.add(new Level(superBody, bindings(superBody, arguments(bound)), level.depth() + 1));
					continue;
				}

				final var methods = ReflectionUtil.exactMethods(base, methodName, arity, arguments(bound), statics, true);
				final var declares = ReflectionUtil.declaresMethodNamed(base, methodName);
				if (methods == null || declares == null)
					return new Candidates(List.of(), named, true);
				named |= declares;
				for (var method : methods)
					found.add(new Found(method, level.depth() + 1));
			}
		}

		final var kept = found.stream().filter(candidate -> !isOverridden(candidate, found)).toList();
		final var fixedArity = arity == 0 && kept.stream().anyMatch(candidate -> !candidate.method().varargs());
		final var keys = kept.stream()
				.filter(candidate -> !fixedArity || !candidate.method().varargs())
				.map(candidate -> candidate.method().returnType())
				.toList();
		return new Candidates(keys, named, false);
	}

	/** The type {@code cast} names ({@link #typeKey}), or null for an intersection ({@link #isIntersectionCast}). */
	@CheckReturnValue
	@Nullable
	private String castKey(@Nonnull DetailAST cast) {
		final var type = cast.findFirstToken(TokenTypes.TYPE);
		return type == null || isIntersectionCast(cast) ? null : typeKey(type);
	}

	/**
	 * Whether the classpath class {@code fqcn} or a supertype of it has a non-private field named
	 * {@code name}, or cannot be read, so may have one.
	 */
	@CheckReturnValue
	private boolean classpathDeclaresField(@Nonnull String fqcn, @Nonnull String name) {
		return classpathFields.computeIfAbsent(fqcn + '#' + name, key -> !Boolean.FALSE.equals(ReflectionUtil.declaresFieldNamed(fqcn, name)));
	}

	/**
	 * Whether a field this file cannot see may bind {@code name} at {@code at} ahead of
	 * {@code declarator}, the declaration the file shows binding it (null when it shows none). A body
	 * the walk out from {@code at} leaves before it reaches the declarator's inherits the non-private
	 * fields of its classpath supertypes, and such a field shadows any outer variable of the name
	 * (JLS 6.4.1); a supertype nothing reads may declare one too. Same-file supertypes need no look:
	 * the walk that found {@code declarator} already read their fields.
	 */
	@CheckReturnValue
	private boolean classpathFieldMayBind(@Nonnull DetailAST at, @Nonnull String name, @Nullable DetailAST declarator) {
		for (var body = enclosingBody(at); body != null; body = enclosingBody(body)) {
			if (declarator != null && (encloses(body, declarator) || AstResolve.fieldDeclaratorIn(body, name) == declarator))
				return false;

			final var visited = new HashSet<DetailAST>();
			final var pending = new ArrayDeque<DetailAST>();
			pending.add(body);
			while (!pending.isEmpty()) {
				final var level = pending.poll();
				if (!visited.add(level))
					continue;

				for (var supertype : supertypeKeys(level)) {
					final var base = supertype == null ? UNKNOWN_TYPE_KEY : base(supertype);
					final var superBody = base.startsWith("#") ? declarations.get(base) : null;
					if (superBody != null)
						pending.add(superBody);
					else if (UNKNOWN_TYPE_KEY.equals(base) || base.startsWith("'") || classpathDeclaresField(base, name))
						return true;
				}
			}
		}
		return false;
	}

	/**
	 * The type a declarator gives its name: the written type, an array one for a varargs parameter,
	 * and for {@code var} its initializer's, or an array for-each's element.
	 */
	@CheckReturnValue
	@Nullable
	private String declaredKey(@Nonnull DetailAST declarator) {
		final var type = declarator.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		final var written = type.getFirstChild();
		final var isVar = written != null && written.getType() == TokenTypes.IDENT && "var".equals(written.getText())
				&& written.getNextSibling() == null;
		if (!isVar) {
			final var key = typeKey(type);
			return key == null || declarator.findFirstToken(TokenTypes.ELLIPSIS) == null ? key : key + "[]";
		}

		final var parent = declarator.getParent();
		if (parent != null && parent.getType() == TokenTypes.FOR_EACH_CLAUSE) {
			final var iterable = parent.findFirstToken(TokenTypes.EXPR);
			final var iterated = iterable == null ? null : keyOf(iterable);
			return iterated != null && iterated.endsWith("[]") ? iterated.substring(0, iterated.length() - 2) : null;
		}

		final var assign = declarator.findFirstToken(TokenTypes.ASSIGN);
		return assign == null || assign.getFirstChild() == null ? null : keyOf(assign.getFirstChild());
	}

	@CheckReturnValue
	@Nullable
	private String elementKey(@Nonnull DetailAST indexOp) {
		final var array = indexOp.getFirstChild();
		final var arrayKey = array == null ? null : keyOf(array);
		return arrayKey != null && arrayKey.endsWith("[]") ? arrayKey.substring(0, arrayKey.length() - 2) : null;
	}

	/**
	 * The method {@code methodDef} declares, its types read through {@code bindings}: a type
	 * variable they leave unbound, the method's own included, is unknown.
	 */
	@CheckReturnValue
	@Nonnull
	private ExactMethod exactMethod(@Nonnull DetailAST methodDef, @Nonnull Map<String, String> bindings) {
		final var parameters = new ArrayList<String>();
		var varargs = false;
		final var declared = methodDef.findFirstToken(TokenTypes.PARAMETERS);
		for (var parameter = declared == null ? null : declared.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
			if (parameter.getType() != TokenTypes.PARAMETER_DEF || parameter.findFirstToken(TokenTypes.IDENT) == null)
				continue;

			final var key = declaredKey(parameter);
			parameters.add(key == null ? UNKNOWN_TYPE_KEY : substitute(key, bindings));
			varargs = parameter.findFirstToken(TokenTypes.ELLIPSIS) != null;
		}

		final var type = methodDef.findFirstToken(TokenTypes.TYPE);
		final var returnType = type == null ? null : typeKey(type);
		return new ExactMethod(parameters, returnType == null ? UNKNOWN_TYPE_KEY : readThrough(returnType, bindings), varargs);
	}

	@CheckReturnValue
	@Nullable
	private String fieldKey(@Nonnull DetailAST dot) {
		final var qualifier = dot.getFirstChild();
		final var member = dot.getLastChild();
		if (qualifier == null || member == qualifier || member.getType() != TokenTypes.IDENT)
			return null;

		final var owner = qualifier(qualifier, dot);
		return owner == null ? null : memberFieldKey(owner.key(), member.getText());
	}

	/**
	 * Whether a call's arguments hold a poly expression, whose type comes from the parameter it is
	 * passed to: a lambda, a method reference, {@code null}, a diamond, or a call, conditional or
	 * switch whose own type is not proven, which a generic method's is not, and neither is a
	 * conditional's whose arms do not all prove one type.
	 */
	@CheckReturnValue
	boolean hasPolyArgument(@Nonnull DetailAST arguments) {
		for (var child = arguments.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var argument = AstQuery.unwrapParensAndExpr(child);
			if (argument == null)
				continue;

			switch (argument.getType()) {
				case TokenTypes.LAMBDA, TokenTypes.LITERAL_NULL, TokenTypes.METHOD_REF -> {
					return true;
				}
				case TokenTypes.LITERAL_NEW -> {
					final var typeArguments = AstQuery.findNewClassTypeArguments(argument);
					if (typeArguments != null && typeArguments.findFirstToken(TokenTypes.TYPE_ARGUMENT) == null)
						return true;
				}
				case TokenTypes.LITERAL_SWITCH, TokenTypes.METHOD_CALL, TokenTypes.QUESTION -> {
					if (!isExact(keyOf(argument)))
						return true;
				}
				default -> {
				}
			}
		}
		return false;
	}

	/**
	 * The type a bare name reads as: an {@code instanceof} pattern governing it, or the declarator
	 * binding it ({@link #declaredKey}). An implicitly typed lambda parameter has none, and neither
	 * does a name a pattern of the enclosing member may bind where its scope is not modelled
	 * ({@link #mayBindPattern}), or a classpath field may ({@link #classpathFieldMayBind}).
	 */
	@CheckReturnValue
	@Nullable
	private String identKey(@Nonnull DetailAST ident) {
		final var name = ident.getText();
		final var pattern = AstResolve.patternVariableInScope(ident, name);
		if (pattern != null)
			return typeKey(pattern.findFirstToken(TokenTypes.TYPE));
		if (mayBindPattern(ident, name))
			return null;

		final var declarator = AstResolve.variableDeclarator(ident, name);
		return declarator == null || classpathFieldMayBind(ident, name, declarator) ? null : declaredKey(declarator);
	}

	/**
	 * The member type named {@code name} that the type whose body is {@code body} inherits (JLS
	 * 8.5): one a same-file supertype declares, or one a classpath supertype declares or inherits.
	 * Null when none does, and unknown when a supertype cannot be read or passes down two different
	 * ones (which javac rejects), or the walk comes back to a body it is already resolving, which only
	 * a cyclic hierarchy does.
	 */
	@CheckReturnValue
	@Nullable
	private String inheritedMemberType(@Nonnull DetailAST body, @Nonnull String name) {
		if (!resolvingMembers.add(body))
			return UNKNOWN_TYPE_KEY;

		try {
			final var visited = new HashSet<DetailAST>();
			final var pending = new ArrayDeque<DetailAST>();
			pending.add(body);
			while (!pending.isEmpty()) {
				final var level = pending.poll();
				if (!visited.add(level))
					continue;

				final var declared = level == body ? null : declaredMemberType(level, name);
				if (declared != null)
					return position("#", declared);

				for (var supertype : supertypeKeys(level)) {
					final var base = supertype == null ? UNKNOWN_TYPE_KEY : base(supertype);
					final var superBody = base.startsWith("#") ? declarations.get(base) : null;
					if (superBody != null) {
						pending.add(superBody);
						continue;
					}
					if (UNKNOWN_TYPE_KEY.equals(base) || base.startsWith("'"))
						return UNKNOWN_TYPE_KEY;

					final var cacheKey = base + '#' + name;
					if (!memberClasses.containsKey(cacheKey))
						memberClasses.put(cacheKey, ReflectionUtil.inheritedMemberClass(base, name, false, packageName));
					final var member = memberClasses.get(cacheKey);
					if (member != null)
						return AMBIGUOUS_TYPE_KEY.equals(member) ? UNKNOWN_TYPE_KEY : member;
				}
			}
			return null;
		}
		finally {
			resolvingMembers.remove(body);
		}
	}

	/**
	 * Whether the type whose body is {@code body}, inside the class whose body is {@code declaring},
	 * inherits that class other than as its own instance type, which makes a member class of it,
	 * named simply inside {@code body}, the member of another instance type: raw
	 * ({@code new Outer() { Inner copy; }} names the raw {@code Outer.Inner}), with other type
	 * arguments, or through another class, whose arguments are not followed. A supertype nothing
	 * resolves may be any of these, and so may a supertype name that leads back to {@code body}
	 * while it is still being read, which only a cyclic hierarchy does.
	 *
	 * <p>Answers are kept per pair, since {@link #writesRaw} asks for every body between a name and
	 * its declaring class, and reading a supertype name there asks again for the bodies around it:
	 * uncached, k nested classes each extending an inner class of a generic class cost 3^k. An
	 * answer given while a cyclic hierarchy was being read can only be a refusal too many.
	 */
	@CheckReturnValue
	private boolean inheritsDeclaringClassOtherwise(@Nonnull DetailAST body, @Nonnull DetailAST declaring) {
		final var pair = List.of(body, declaring);
		final var known = inheritsOtherwise.get(pair);
		if (known != null)
			return known;
		if (!readingInheritance.add(body))
			return true;

		try {
			final var declaringKey = position("#", declaring);
			final var self = selfKey(declaring);
			final var visited = new HashSet<DetailAST>();
			final var pending = new ArrayDeque<DetailAST>();
			pending.add(body);
			var otherwise = false;
			while (!otherwise && !pending.isEmpty()) {
				final var level = pending.poll();
				if (!visited.add(level))
					continue;

				for (var supertype : supertypeKeys(level)) {
					final var base = supertype == null ? UNKNOWN_TYPE_KEY : base(supertype);
					if (UNKNOWN_TYPE_KEY.equals(base) || base.equals(declaringKey) && (level != body || !supertype.equals(self))) {
						otherwise = true;
						break;
					}

					final var superBody = base.startsWith("#") ? declarations.get(base) : null;
					if (superBody != null)
						pending.add(superBody);
				}
			}
			inheritsOtherwise.put(pair, otherwise);
			return otherwise;
		}
		finally {
			readingInheritance.remove(body);
		}
	}

	/**
	 * The type a for-each over {@code iterable} gives its variable: an array's element type, or the
	 * one argument of the {@code java.util.Iterator} its {@code iterator()} returns, as read through
	 * the iterable's type arguments. Null when neither is proven.
	 */
	@CheckReturnValue
	@Nullable
	String iteratedKey(@Nonnull DetailAST iterable) {
		final var key = keyOf(iterable);
		if (key == null)
			return null;
		if (key.endsWith("[]"))
			return key.substring(0, key.length() - 2);

		final var iterator = memberCallKey(key, "iterator", 0, false);
		final var arguments = iterator == null || !ITERATOR_FQCN.equals(base(iterator)) ? null : arguments(iterator);
		return arguments == null || arguments.size() != 1 ? null : arguments.getFirst();
	}

	/** The key of the type {@code expression} evaluates to, or null when nothing here proves one. */
	@CheckReturnValue
	@Nullable
	String keyOf(@Nonnull DetailAST expression) {
		final var node = AstQuery.unwrapParensAndExpr(expression);
		// a name can resolve through itself, as `for (var items : items)` does
		if (node == null || depth >= MAX_DEPTH || !inProgress.add(node))
			return null;

		++depth;
		try {
			final var key = switch (node.getType()) {
				case TokenTypes.CHAR_LITERAL -> "char";
				case TokenTypes.DOT -> fieldKey(node);
				case TokenTypes.IDENT -> identKey(node);
				case TokenTypes.INDEX_OP -> elementKey(node);
				case TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_TRUE -> "boolean";
				case TokenTypes.LITERAL_NEW -> newKey(node, false);
				case TokenTypes.LITERAL_SWITCH, TokenTypes.QUESTION -> armsKey(node);
				case TokenTypes.LITERAL_THIS -> thisKey(node);
				case TokenTypes.METHOD_CALL -> callKey(node);
				case TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT ->
						node.getText().endsWith("f") || node.getText().endsWith("F") ? "float" : "double";
				case TokenTypes.NUM_INT -> "int";
				case TokenTypes.NUM_LONG -> "long";
				case TokenTypes.STRING_LITERAL, TokenTypes.TEXT_BLOCK_LITERAL_BEGIN -> STRING_FQCN;
				case TokenTypes.TYPECAST -> castKey(node);
				default -> null;
			};
			return key == null || key.length() <= MAX_KEY_LENGTH ? key : UNKNOWN_TYPE_KEY;
		}
		finally {
			--depth;
			inProgress.remove(node);
		}
	}

	/** {@link #mayBindPattern(DetailAST, String, Function)}, reading each member's patterns once. */
	@CheckReturnValue
	private boolean mayBindPattern(@Nonnull DetailAST at, @Nonnull String name) {
		return mayBindPattern(at, name, member -> memberPatterns.computeIfAbsent(member, ProvenTypes::patternNames));
	}

	/**
	 * The type a call of {@code methodName} with {@code arity} arguments returns on a value or, with
	 * {@code statics}, a type named {@code owner}. A type variable's members are its bound's.
	 */
	@CheckReturnValue
	@Nullable
	private String memberCallKey(@Nonnull String owner, @Nonnull String methodName, int arity, boolean statics) {
		var receiver = owner;
		final var seen = new HashSet<String>();
		while (base(receiver).startsWith("'")) {
			if (!seen.add(receiver))
				return null;
			receiver = typeVariableBound(base(receiver));
			if (receiver == null)
				return null;
		}

		if (receiver.endsWith("[]"))
			return "clone".equals(methodName) && arity == 0 ? receiver : classpathCallKey(OBJECT_FQCN, methodName, arity, null, false);

		final var base = base(receiver);
		if (UNKNOWN_TYPE_KEY.equals(base) || PRIMITIVES.contains(base))
			return null;

		final var body = base.startsWith("#") ? declarations.get(base) : null;
		if (body == null)
			return classpathCallKey(base, methodName, arity, arguments(receiver), statics);

		final var found = candidates(body, bindings(body, arguments(receiver)), methodName, arity, statics, false);
		return found.unknown() ? null : agree(found.keys());
	}

	/** The type of the field {@code fieldName} read off a value or a type named {@code owner}. */
	@CheckReturnValue
	@Nullable
	private String memberFieldKey(@Nonnull String owner, @Nonnull String fieldName) {
		if (owner.endsWith("[]"))
			return "length".equals(fieldName) ? "int" : null;

		final var base = base(owner);
		if (UNKNOWN_TYPE_KEY.equals(base) || PRIMITIVES.contains(base) || base.startsWith("'"))
			return null;

		final var body = base.startsWith("#") ? declarations.get(base) : null;
		if (body == null)
			return ReflectionUtil.exactFieldType(base, fieldName, arguments(owner));

		final var declarator = AstResolve.fieldDeclaratorIn(body, fieldName);
		final var key = declarator == null ? null : declaredKey(declarator);
		return key == null ? null : readThrough(key, bindings(body, arguments(owner)));
	}

	/**
	 * The key of the type {@code name} names, a class name or a primitive keyword, resolved where it
	 * is written, or spelled as written when {@code spelled}, with the type arguments written on it.
	 * Arguments written on a qualifier ({@code Outer<String>.Inner}) belong to an enclosing
	 * instance no key carries, so such a name has none, and an inner class of a generic class
	 * written raw ({@link #writesRaw}) is unknown. An empty argument list, a diamond, is unknown.
	 */
	@CheckReturnValue
	@Nullable
	private String nameKey(@Nonnull DetailAST name, @Nullable DetailAST typeArguments, boolean spelled) {
		if (PRIMITIVES.contains(name.getText()) && name.getType() != TokenTypes.IDENT && name.getType() != TokenTypes.DOT)
			return name.getText();

		var arguments = typeArguments;
		if (name.getType() == TokenTypes.DOT) {
			for (var segment = name; segment != null && segment.getType() == TokenTypes.DOT; segment = segment.getFirstChild()) {
				final var own = segment.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
				if (own != null && segment != name)
					return null;
				if (own != null)
					arguments = own;
			}
		}
		else if (name.getType() != TokenTypes.IDENT)
			return null;

		final var text = AstText.typeName(name);
		final var resolved = resolvedName(text, name);
		final var base = spelled ? text : resolved;
		if (base == null)
			return null;
		if (resolved != null && writesRaw(name, resolved))
			return UNKNOWN_TYPE_KEY;
		if (arguments == null)
			return base;

		final var keys = new StringJoiner(",", "<", ">");
		var count = 0;
		for (var argument = arguments.getFirstChild(); argument != null; argument = argument.getNextSibling()) {
			if (argument.getType() != TokenTypes.TYPE_ARGUMENT)
				continue;
			final var key = typeKey(argument, spelled);
			if (key == null)
				return null;
			keys.add(key);
			++count;
		}
		return count == 0 ? base + "<" + UNKNOWN_TYPE_KEY + ">" : base + keys;
	}

	/**
	 * Whether {@code key}, a resolved class name, names an inner class of a generic class, in this
	 * file ({@link #isInnerClassOfGeneric(DetailAST)}) or on the classpath
	 * ({@link ReflectionUtil#isInnerClassOfGeneric}).
	 */
	@CheckReturnValue
	private boolean namesInnerClassOfGeneric(@Nonnull String key) {
		if (key.startsWith("#")) {
			final var body = declarations.get(key);
			return body != null && isInnerClassOfGeneric(body);
		}
		return !key.startsWith("'") && !UNKNOWN_TYPE_KEY.equals(key) && ReflectionUtil.isInnerClassOfGeneric(key);
	}

	/**
	 * The key of the object or array {@code literalNew} creates, resolved or, with {@code spelled},
	 * as written. An anonymous class's type has no name, so it has no key.
	 */
	@CheckReturnValue
	@Nullable
	String newKey(@Nonnull DetailAST literalNew, boolean spelled) {
		if (literalNew.findFirstToken(TokenTypes.OBJBLOCK) != null)
			return null;

		DetailAST name = null;
		var dimensions = 0;
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				++dimensions;
			else if (name == null && (child.getType() == TokenTypes.IDENT || child.getType() == TokenTypes.DOT
					|| PRIMITIVES.contains(child.getText())))
				name = child;
		}
		if (name == null)
			return null;

		final var arguments = name.getType() == TokenTypes.IDENT ? AstQuery.findNewClassTypeArguments(literalNew) : null;
		final var key = nameKey(name, arguments, spelled);
		return key == null ? null : key + "[]".repeat(dimensions);
	}

	/**
	 * The type parameters of the type whose body is {@code body}, each bound to itself, as a call
	 * made inside that type sees them.
	 */
	@CheckReturnValue
	@Nonnull
	private Map<String, String> ownBindings(@Nonnull DetailAST body) {
		return bindings(body, typeParameterTokens(body));
	}

	/**
	 * Adds to {@code found} what {@code level}'s body itself offers a call of {@code methodName}
	 * with {@code arity} arguments ({@link #candidates}), and answers whether it has a method of
	 * that name a call from below can reach at all. A supertype's private methods and an
	 * interface's static ones are not inherited (JLS 8.4.8), so they count for neither. A record
	 * component's accessor is implicit unless a method without parameters declares it.
	 */
	private boolean ownCandidates(
			@Nonnull Level level,
			@Nonnull String methodName,
			int arity,
			boolean statics,
			@Nonnull List<Found> found
	) {
		final var body = level.body();
		final var owner = body.getParent();
		var named = false;
		var accessor = owner.getType() == TokenTypes.RECORD_DEF;
		for (var member = body.getFirstChild(); member != null; member = member.getNextSibling()) {
			final var ident = member.getType() == TokenTypes.METHOD_DEF ? member.findFirstToken(TokenTypes.IDENT) : null;
			if (ident == null || !methodName.equals(ident.getText()))
				continue;

			final var modifiers = member.findFirstToken(TokenTypes.MODIFIERS);
			final var isStatic = modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
			final var isPrivate = modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null;
			if (level.depth() > 0 && (isPrivate || (isStatic && owner.getType() == TokenTypes.INTERFACE_DEF)))
				continue;
			named = true;

			final var method = exactMethod(member, level.bindings());
			if (method.parameters().isEmpty() && !method.varargs())
				accessor = false;
			if (takesArity(member, arity) && (!statics || isStatic))
				found.add(new Found(method, level.depth()));
		}

		if (accessor && arity == 0 && !statics) {
			final var component = AstResolve.fieldDeclaratorIn(body, methodName);
			final var key = component == null || component.getType() != TokenTypes.RECORD_COMPONENT_DEF ? null : declaredKey(component);
			if (key != null) {
				named = true;
				found.add(new Found(new ExactMethod(List.of(), readThrough(key, level.bindings()), false), level.depth()));
			}
		}

		if (owner.getType() == TokenTypes.ENUM_DEF && ("values".equals(methodName) || "valueOf".equals(methodName))) {
			named = true;
			final var self = position("#", body);
			if ("values".equals(methodName) && arity == 0)
				found.add(new Found(new ExactMethod(List.of(), self + "[]", false), level.depth()));
			else if ("valueOf".equals(methodName) && arity == 1)
				found.add(new Found(new ExactMethod(List.of(STRING_FQCN), self, false), level.depth()));
		}
		return named;
	}

	/** The token naming the same-file declaration {@code node}, registered so the key leads back to it. */
	@Nonnull
	private String position(@Nonnull String prefix, @Nonnull DetailAST node) {
		final var token = prefix + node.getLineNo() + ":" + node.getColumnNo();
		declarations.put(token, node);
		return token;
	}

	/**
	 * What {@code qualifier}, written before a member at {@code at}, denotes: the type of
	 * {@code this} or {@code Outer.this}, a type it names, or the type of the value it is. A
	 * variable obscures a type of the same name (JLS 6.4.2), so a name in scope as one is a value.
	 */
	@CheckReturnValue
	@Nullable
	private Qualifier qualifier(@Nonnull DetailAST qualifier, @Nonnull DetailAST at) {
		if (qualifier.getType() == TokenTypes.LITERAL_THIS) {
			final var body = enclosingBody(at);
			return body == null ? null : new Qualifier(selfKey(body), false);
		}
		if (qualifier.getType() == TokenTypes.DOT && qualifier.getLastChild().getType() == TokenTypes.LITERAL_THIS) {
			final var outer = AstText.typeName(qualifier.getFirstChild());
			final var classDef = outer == null ? null : AstResolve.sameFileClassDef(at, outer);
			final var body = classDef == null ? null : classDef.findFirstToken(TokenTypes.OBJBLOCK);
			return body == null ? null : new Qualifier(selfKey(body), false);
		}

		if (AstQuery.isPureDotChainOrIdent(qualifier)) {
			var head = qualifier;
			while (head.getType() == TokenTypes.DOT)
				head = head.getFirstChild();
			if (!AstResolve.variableIsBound(at, head.getText())) {
				// a variable obscures the type, and one the file does not show may still be in scope
				if (mayBindPattern(head, head.getText()) || classpathFieldMayBind(head, head.getText(), null))
					return null;

				final var typeName = AstText.typeName(qualifier);
				final var type = typeName == null ? null : resolvedName(typeName, at);
				if (type != null && !type.startsWith("'"))
					return new Qualifier(type, true);
				if (qualifier.getType() == TokenTypes.IDENT)
					return null;
			}
		}

		final var key = keyOf(qualifier);
		return key == null ? null : new Qualifier(key, false);
	}

	/**
	 * {@code key}, the type a member declares, read through {@code bindings}, or unknown when it
	 * names a same-file inner class of a generic class ({@link #isInnerClassOfGeneric(DetailAST)})
	 * and {@code bindings} bind a type parameter of an enclosing class to anything but itself. The
	 * key carries no arguments for the enclosing instance, so it is that type only read through the
	 * declaring instance itself: {@code strings.inner()} on an {@code Outer<String>} is an
	 * {@code Outer<String>.Inner}, and on a raw {@code Outer} a raw one.
	 */
	@CheckReturnValue
	@Nonnull
	private String readThrough(@Nonnull String key, @Nonnull Map<String, String> bindings) {
		for (var start = key.indexOf('#'); start >= 0; start = key.indexOf('#', start + 1)) {
			var end = start + 1;
			while (end < key.length() && "<>,[".indexOf(key.charAt(end)) < 0)
				++end;
			final var body = declarations.get(key.substring(start, end));
			if (body != null && isInnerClassOfGeneric(body) && rebindsEnclosingTypeParameters(body, bindings))
				return UNKNOWN_TYPE_KEY;
		}
		return substitute(key, bindings);
	}

	/** Whether {@code bindings} bind a type parameter declared around the type whose body is {@code body} to anything but itself. */
	@CheckReturnValue
	private boolean rebindsEnclosingTypeParameters(@Nonnull DetailAST body, @Nonnull Map<String, String> bindings) {
		for (var scope = body.getParent().getParent(); scope != null; scope = scope.getParent()) {
			final var parameters = scope.findFirstToken(TokenTypes.TYPE_PARAMETERS);
			for (var parameter = parameters == null ? null : parameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
				final var ident = parameter.getType() == TokenTypes.TYPE_PARAMETER ? parameter.findFirstToken(TokenTypes.IDENT) : null;
				final var token = ident == null ? null : position("'", ident);
				if (token != null && !token.equals(bindings.get(token)))
					return true;
			}
		}
		return false;
	}

	/**
	 * The key of the class or type variable {@code name} denotes at {@code scope}: its leading
	 * name as the scope names it ({@link #scopedName}), or else as the imports resolve it, and any
	 * further names as members of that one. A qualified name nothing in scope or the imports leads
	 * is taken as fully qualified. Null when it names nothing.
	 */
	@CheckReturnValue
	@Nullable
	private String resolvedName(@Nonnull String name, @Nonnull DetailAST scope) {
		final var dot = name.indexOf('.');
		final var head = dot < 0 ? name : name.substring(0, dot);
		final var scoped = scopedName(head, scope);
		if (scoped != null && (dot < 0 || UNKNOWN_TYPE_KEY.equals(scoped)))
			return scoped;
		if (scoped != null && scoped.startsWith("'"))
			return null;
		if (scoped != null && scoped.startsWith("#")) {
			final var classDef = AstResolve.sameFileClassDef(scope, name);
			return classDef == null ? null : position("#", classDef.findFirstToken(TokenTypes.OBJBLOCK));
		}
		if (scoped != null)
			return ReflectionUtil.binaryName(scoped + name.substring(dot));

		final var resolvedHead = resolver.apply(head);
		if (resolvedHead == null && dot < 0)
			return null;
		return ReflectionUtil.binaryName(resolvedHead == null ? name : resolvedHead + name.substring(head.length()));
	}

	/**
	 * What the simple name {@code name} denotes as a type at {@code scope} ahead of the imports
	 * (JLS 6.4.1), walking outwards: a type parameter, a type a block, a body or the file declares,
	 * or a member type a body inherits, from this file or the classpath alike. Null when nothing in
	 * scope declares one, and unknown when a supertype on the way cannot be read, since it may.
	 */
	@CheckReturnValue
	@Nullable
	private String scopedName(@Nonnull String name, @Nonnull DetailAST scope) {
		for (var frame = scope; frame != null; frame = frame.getParent()) {
			final var parameters = frame.findFirstToken(TokenTypes.TYPE_PARAMETERS);
			for (var parameter = parameters == null ? null : parameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
				final var ident = parameter.getType() == TokenTypes.TYPE_PARAMETER ? parameter.findFirstToken(TokenTypes.IDENT) : null;
				if (ident != null && name.equals(ident.getText()))
					return position("'", ident);
			}

			final var type = frame.getType();
			if (type != TokenTypes.COMPILATION_UNIT && type != TokenTypes.OBJBLOCK && type != TokenTypes.SLIST)
				continue;
			final var declared = declaredMemberType(frame, name);
			if (declared != null)
				return position("#", declared);
			if (type == TokenTypes.OBJBLOCK) {
				final var inherited = inheritedMemberType(frame, name);
				if (inherited != null)
					return inherited;
			}
		}
		return null;
	}

	/** The type of {@code this} in the body {@code body}: its own type parameters as its arguments. */
	@CheckReturnValue
	@Nonnull
	private String selfKey(@Nonnull DetailAST body) {
		final var self = position("#", body);
		final var parameters = typeParameterTokens(body);
		return parameters.isEmpty() ? self : self + "<" + String.join(",", parameters) + ">";
	}

	/** The key of the type a {@code TYPE} node writes, spelled as written ({@link #typeKey(DetailAST, boolean)}). */
	@CheckReturnValue
	@Nullable
	String spelling(@Nonnull DetailAST type) {
		return typeKey(type, true);
	}

	/**
	 * The direct supertypes of the type whose body is {@code body}, each written with the type
	 * arguments its declaration gives it, null where a name resolves to nothing: the extends and
	 * implements clauses, the type an anonymous class's {@code new} names, the enum an enum
	 * constant's body extends, and the implicit {@code Object}, {@code Enum} or {@code Record}. An
	 * interface lists {@code Object} too, whose public methods it has (JLS 9.2).
	 */
	@CheckReturnValue
	@Nonnull
	private List<String> supertypeKeys(@Nonnull DetailAST body) {
		final var owner = body.getParent();
		final var supertypes = new ArrayList<String>();
		switch (owner.getType()) {
			case TokenTypes.ENUM_CONSTANT_DEF -> {
				final var enumBody = owner.getParent();
				supertypes.add(enumBody == null ? null : selfKey(enumBody));
				return supertypes;
			}
			case TokenTypes.ENUM_DEF -> supertypes.add("java.lang.Enum<" + position("#", body) + ">");
			case TokenTypes.LITERAL_NEW -> {
				var name = owner.getFirstChild();
				while (name != null && name.getType() != TokenTypes.IDENT && name.getType() != TokenTypes.DOT)
					name = name.getNextSibling();
				supertypes.add(name == null ? null : nameKey(name, name.getType() == TokenTypes.IDENT ? AstQuery.findNewClassTypeArguments(owner) : null, false));
				supertypes.add(OBJECT_FQCN);
				return supertypes;
			}
			case TokenTypes.RECORD_DEF -> supertypes.add("java.lang.Record");
			default -> {
				if (owner.getType() == TokenTypes.INTERFACE_DEF || owner.findFirstToken(TokenTypes.EXTENDS_CLAUSE) == null)
					supertypes.add(OBJECT_FQCN);
			}
		}

		for (var clause = owner.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var name = clause.getFirstChild(); name != null; name = name.getNextSibling()) {
				if (name.getType() != TokenTypes.IDENT && name.getType() != TokenTypes.DOT)
					continue;
				final var arguments = name.getNextSibling();
				final var written = arguments != null && arguments.getType() == TokenTypes.TYPE_ARGUMENTS ? arguments : null;
				supertypes.add(nameKey(name, written, false));
			}
		}
		return supertypes;
	}

	/**
	 * The type of {@code self}, a {@code this}, inside an enum constant's body: the constant's own
	 * class, a subclass of the enum that no declaration can name. Anywhere else nothing here proves
	 * it, so null.
	 */
	@CheckReturnValue
	@Nullable
	private String thisKey(@Nonnull DetailAST self) {
		final var body = enclosingBody(self);
		return body == null || body.getParent().getType() != TokenTypes.ENUM_CONSTANT_DEF ? null : selfKey(body);
	}

	/** The key of the type a {@code TYPE} or {@code TYPE_ARGUMENT} node writes, resolved where it is written. */
	@CheckReturnValue
	@Nullable
	String typeKey(@Nonnull DetailAST type) {
		return typeKey(type, false);
	}

	/**
	 * The key of the type a {@code TYPE} or {@code TYPE_ARGUMENT} node writes, its names resolved
	 * where they are written or, with {@code spelled}, as written. Annotations name no type, and a
	 * wildcard is unknown.
	 */
	@CheckReturnValue
	@Nullable
	private String typeKey(@Nonnull DetailAST type, boolean spelled) {
		DetailAST name = null;
		DetailAST arguments = null;
		var dimensions = 0;
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.ANNOTATION, TokenTypes.ANNOTATIONS, TokenTypes.BLOCK_COMMENT_BEGIN,
				     TokenTypes.SINGLE_LINE_COMMENT -> {
				}
				case TokenTypes.ARRAY_DECLARATOR -> ++dimensions;
				case TokenTypes.TYPE_ARGUMENTS -> arguments = child;
				case TokenTypes.WILDCARD_TYPE -> {
					return UNKNOWN_TYPE_KEY;
				}
				default -> {
					if (name == null)
						name = child;
				}
			}
		}
		if (name == null)
			return null;

		final var element = nameKey(name, arguments, spelled);
		return element == null ? null : element + "[]".repeat(dimensions);
	}

	/** The tokens of the type parameters the type whose body is {@code body} declares, in order. */
	@CheckReturnValue
	@Nonnull
	private List<String> typeParameterTokens(@Nonnull DetailAST body) {
		final var parameters = body.getParent().findFirstToken(TokenTypes.TYPE_PARAMETERS);
		final var tokens = new ArrayList<String>();
		for (var parameter = parameters == null ? null : parameters.getFirstChild(); parameter != null; parameter = parameter.getNextSibling()) {
			final var ident = parameter.getType() == TokenTypes.TYPE_PARAMETER ? parameter.findFirstToken(TokenTypes.IDENT) : null;
			if (ident != null)
				tokens.add(position("'", ident));
		}
		return tokens;
	}

	/**
	 * The type a type variable's members are read off: its bound, or {@code Object} when it has
	 * none. Null for an intersection of bounds, whose members no one key carries.
	 */
	@CheckReturnValue
	@Nullable
	private String typeVariableBound(@Nonnull String token) {
		final var ident = declarations.get(token);
		final var bounds = ident == null ? null : ident.getParent().findFirstToken(TokenTypes.TYPE_UPPER_BOUNDS);
		if (bounds == null)
			return ident == null ? null : OBJECT_FQCN;
		if (bounds.findFirstToken(TokenTypes.TYPE_EXTENSION_AND) != null)
			return null;

		var name = bounds.getFirstChild();
		while (name != null && name.getType() != TokenTypes.IDENT && name.getType() != TokenTypes.DOT)
			name = name.getNextSibling();
		if (name == null)
			return null;

		final var arguments = name.getNextSibling();
		return nameKey(name, arguments != null && arguments.getType() == TokenTypes.TYPE_ARGUMENTS ? arguments : null, false);
	}

	/**
	 * Whether {@code name}, which resolves to {@code key}, writes an inner class of a generic class
	 * ({@link #namesInnerClassOfGeneric}) without the enclosing instance's type arguments. Only a
	 * simple name written inside the body of the class declaring it reads as that body's own
	 * instance ({@code Inner} inside {@code Outer<T>} is {@code Outer<T>.Inner}), and only where no
	 * class in between inherits it otherwise ({@link #inheritsDeclaringClassOtherwise}). A qualified
	 * name ({@code Outer.Inner}) is raw (JLS 4.8), and an imported, inherited or classpath one is
	 * raw or takes arguments from a supertype, which the key cannot carry either.
	 */
	@CheckReturnValue
	private boolean writesRaw(@Nonnull DetailAST name, @Nonnull String key) {
		if (!namesInnerClassOfGeneric(key))
			return false;

		final var body = declarations.get(key);
		if (body == null || name.getType() != TokenTypes.IDENT)
			return true;

		final var declaring = body.getParent().getParent();
		if (!encloses(declaring, name))
			return true;
		for (var between = enclosingBody(name); between != null && between != declaring; between = enclosingBody(between)) {
			if (inheritsDeclaringClassOtherwise(between, declaring))
				return true;
		}
		return false;
	}
}