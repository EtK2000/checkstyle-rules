package com.etk2000.checkstyle;

import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.MalformedParameterizedTypeException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Queue;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility for resolving class names and inspecting method signatures
 * via reflection. Designed for use by checkstyle checks that need
 * type information beyond what the AST provides.
 */
public final class ReflectionUtil {
	private static final int MAX_INNER_CLASS_DEPTH = 10;

	/**
	 * Atomic because the same daemon runs several checkstyle tasks against one
	 * loaded copy of this class, and lost increments make the assertions flaky.
	 */
	@TestOnly
	static final AtomicInteger classForNameCallCount = new AtomicInteger();

	// concurrent because Gradle runs checkstyleMain/checkstyleTest/checkstyleTestResources
	// in one daemon against one loaded copy of this class, and a plain HashMap can lose
	// entries or throw under that. Optional.empty() represents a known-absent class so
	// retries don't repeat Class.forName scans for unresolvable names.
	private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new ConcurrentHashMap<>();

	/** What {@link #inheritedMemberClass} answers when two different member types of the name reach the class. */
	static final String AMBIGUOUS_TYPE_KEY = "<ambiguous>";
	/** A parameter key position a class's type variable stands at, before its array suffix. */
	static final String TYPE_VARIABLE_KEY = "?";
	/** The part of an exact type key ({@link #exactMethods}) that cannot be told. */
	static final String UNKNOWN_TYPE_KEY = "*";

	@CheckReturnValue
	private static boolean acceptsArgCount(@Nonnull Method method, int argCount) {
		final var declared = method.getParameterCount();
		return method.isVarArgs() ? argCount >= declared - 1 : argCount == declared;
	}

	/**
	 * Whether every parameter but the one at {@code skippedIndex} accepts its argument's type, an
	 * unknown type ({@code null}) accepted by any. The check is assignment without conversions: a
	 * primitive must match by name, and {@code "null"} needs a reference parameter.
	 */
	@CheckReturnValue
	private static boolean acceptsArguments(@Nonnull Class<?>[] params, int skippedIndex, @Nonnull List<String> argumentTypes) {
		for (var i = 0; i < params.length; ++i) {
			final var argumentType = argumentTypes.get(i);
			if (i == skippedIndex || argumentType == null)
				continue;
			if ("null".equals(argumentType)) {
				if (params[i].isPrimitive())
					return false;
				continue;
			}
			if (params[i].isPrimitive()) {
				if (!params[i].getName().equals(argumentType))
					return false;
				continue;
			}
			final var argumentClass = loadClass(argumentType);
			if (argumentClass != null && !params[i].isAssignableFrom(argumentClass))
				return false;
		}
		return true;
	}

	/**
	 * Whether a target declared as {@code targetFqcn} accepts a value of {@code valueFqcn}. Either
	 * name being unloadable answers no, so a caller deciding whether a rewrite keeps compiling
	 * refuses rather than guesses.
	 */
	@CheckReturnValue
	static boolean acceptsValueOfType(@Nonnull String targetFqcn, @Nonnull String valueFqcn) {
		final var target = loadClass(targetFqcn);
		final var value = loadClass(valueFqcn);
		if (target == null || value == null)
			return false;

		try {
			return target.isAssignableFrom(value);
		}
		catch (LinkageError e) {
			return false;
		}
	}

	/**
	 * Returns {@code fqcn} with dots between adjacent uppercase-starting
	 * segments (after the first uppercase segment) rewritten to {@code $}.
	 * Returns {@code fqcn} unchanged if any segment after the first uppercase
	 * one starts non-uppercase.
	 */
	@CheckReturnValue
	@Nonnull
	static String applyJavaNamingHeuristic(@Nonnull String fqcn) {
		var pastFirstClass = false;
		for (var i = 0; i < fqcn.length(); ++i) {
			if (i == 0 || fqcn.charAt(i - 1) == '.') {
				final var c = fqcn.charAt(i);
				if (Character.isUpperCase(c))
					pastFirstClass = true;
				else if (pastFirstClass)
					return fqcn;
			}
		}

		final var sb = new StringBuilder(fqcn.length());
		var seenFirstClass = false;
		for (var i = 0; i < fqcn.length(); ++i) {
			final var c = fqcn.charAt(i);
			if ((i == 0 || fqcn.charAt(i - 1) == '.') && Character.isUpperCase(c))
				seenFirstClass = true;
			if (c == '.' && seenFirstClass && i + 1 < fqcn.length()
					&& Character.isUpperCase(fqcn.charAt(i + 1)))
				sb.append('$');
			else
				sb.append(c);
		}
		return sb.toString();
	}

	/** The key position of {@code argument}, a type argument a supertype is written with. */
	@CheckReturnValue
	@Nonnull
	private static String argumentKey(@Nonnull Type argument, @Nonnull Map<TypeVariable<?>, String> bindings) {
		return switch (argument) {
			case GenericArrayType array -> argumentKey(array.getGenericComponentType(), bindings) + "[]";
			case TypeVariable<?> variable -> bindings.getOrDefault(variable, TYPE_VARIABLE_KEY);
			default -> keyPosition(erasureOf(argument));
		};
	}

	/**
	 * The binary name {@code fqcn} loads as, or null when nothing loads. One nested type has several
	 * source spellings ({@code java.util.Map.Entry}, {@code java.util.Map$Entry}), and a caller
	 * comparing two of them as strings needs the single form the JVM gives back.
	 */
	@CheckReturnValue
	@Nullable
	static String binaryName(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		return clazz == null ? null : clazz.getName();
	}

	/**
	 * The methods a call through {@code clazz} may bind to. With {@code statics}, its static methods
	 * and those it inherits from a superclass. Otherwise its public methods, plus a superclass's
	 * protected ones when {@code inherited}, which a call made from inside a subclass reaches. A
	 * superinterface's static method is no member of {@code clazz} (JLS 8.4.8), and a bridge or
	 * synthetic method restates a signature the real one carries, so neither is listed.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<Method> callableMethods(@Nonnull Class<?> clazz, boolean statics, boolean inherited) {
		final var methods = new ArrayList<Method>();
		if (!statics)
			methods.addAll(List.of(methodsOf(clazz)));
		for (var owner = clazz; owner != null; owner = owner.getSuperclass()) {
			for (var method : declaredMethodsOf(owner)) {
				final var modifiers = method.getModifiers();
				final var wanted = statics
						? Modifier.isStatic(modifiers) && !Modifier.isPrivate(modifiers)
						: inherited && Modifier.isProtected(modifiers);
				if (wanted)
					methods.add(method);
			}
		}
		final var callable = new ArrayList<Method>();
		for (var method : methods) {
			final var interfaceStatic = Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().isInterface()
					&& method.getDeclaringClass() != clazz;
			if (!method.isBridge() && !method.isSynthetic() && !interfaceStatic)
				callable.add(method);
		}
		return callable;
	}

	/**
	 * The sibling overload of the given method that replaces the parameter at
	 * {@code stringIndex} with {@code Charset}, keeping all other parameters the
	 * same, or null when there is none.
	 */
	@CheckReturnValue
	@Nullable
	private static Executable charsetOverload(@Nonnull Executable[] methods, @Nonnull Executable method, int stringIndex) {
		final var params = method.getParameterTypes();
		for (var candidate : methods) {
			if (candidate == method)
				continue;
			// every caller passes a single-kind array (all methods or all
			// constructors), so this cross-kind skip is defensive
			if (!candidate.getClass().equals(method.getClass()))
				continue;
			if (candidate instanceof Method m && !m.getName().equals(((Method) method).getName()))
				continue;

			final var candidateParams = candidate.getParameterTypes();
			if (candidateParams.length != params.length)
				continue;

			var match = true;
			for (var i = 0; i < params.length; ++i) {
				if (i == stringIndex) {
					if (candidateParams[i] != Charset.class) {
						match = false;
						break;
					}
				}
				else if (candidateParams[i] != params[i]) {
					match = false;
					break;
				}
			}
			if (match)
				return candidate;
		}
		return null;
	}

	/**
	 * Whether replacing the charset name with a {@code Charset} could add a checked exception the
	 * call did not throw before: some {@code String} charset overload of {@code methodName} on
	 * {@code fqcn} ({@code new} for the constructors) that the known argument types allow has a
	 * {@code Charset} sibling declaring a checked exception its own throws clause does not cover
	 * ({@code PrintStream(File, Charset)} throws {@code IOException} where
	 * {@code PrintStream(File, String)} throws {@code FileNotFoundException}).
	 *
	 * @param argumentTypes per argument, the fully qualified type, the primitive's name, {@code "null"}
	 *                      for a null literal, or null when unknown, which every parameter accepts
	 */
	@CheckReturnValue
	static boolean charsetOverloadWidensCheckedExceptions(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int charsetIndex,
			@Nonnull List<String> argumentTypes
	) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		final var methods = executablesOf(clazz, "new".equals(methodName));
		for (var method : methods) {
			if (!"new".equals(methodName) && !method.getName().equals(methodName))
				continue;
			final var params = method.getParameterTypes();
			if (params.length != argumentTypes.size() || params[charsetIndex] != String.class)
				continue;
			final var overload = charsetOverload(methods, method, charsetIndex);
			if (overload == null || !acceptsArguments(params, charsetIndex, argumentTypes))
				continue;
			for (var thrown : overload.getExceptionTypes()) {
				if (isChecked(thrown) && Arrays.stream(method.getExceptionTypes()).noneMatch(covered -> covered.isAssignableFrom(thrown)))
					return true;
			}
		}
		return false;
	}

	@TestOnly
	static void clearCache() {
		CLASS_CACHE.clear();
	}

	/**
	 * The fully qualified name of the collection interface {@code fqcn} is best declared as: the
	 * interface itself when it already is one, or the one {@link #findCollectionInterface} picks.
	 */
	@CheckReturnValue
	@Nullable
	static String collectionInterfaceFqcn(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;
		if (clazz.isInterface())
			return clazz.getName();

		final var iface = findCollectionInterface(fqcn);
		if (iface == null)
			return null;

		return switch (iface) {
			case "Collection" -> Collection.class.getName();
			case "Deque" -> Deque.class.getName();
			case "List" -> List.class.getName();
			case "Map" -> Map.class.getName();
			case "Queue" -> Queue.class.getName();
			case "Set" -> Set.class.getName();
			default -> null;
		};
	}

	private static void collectMethodLevelTypeVars(@Nonnull Type type, @Nonnull Set<String> typeVars) {
		switch (type) {
			case TypeVariable<?> tv when tv.getGenericDeclaration() instanceof Method ->
					typeVars.add(tv.getName());
			case ParameterizedType pt -> {
				for (var arg : pt.getActualTypeArguments())
					collectMethodLevelTypeVars(arg, typeVars);
			}
			case WildcardType wt -> {
				for (var bound : wt.getUpperBounds())
					collectMethodLevelTypeVars(bound, typeVars);
				for (var bound : wt.getLowerBounds())
					collectMethodLevelTypeVars(bound, typeVars);
			}
			// a varargs parameter is an array of the type variable, so without this the
			// element type is never collected and the method looks target-typed
			case GenericArrayType ga -> collectMethodLevelTypeVars(ga.getGenericComponentType(), typeVars);
			default -> {
			}
		}
	}

	/**
	 * {@code clazz.getDeclaredMethods()}, or empty when a type named in a method descriptor is
	 * absent. See {@link #methodsOf} for why that is not the same question as loading the class.
	 */
	@CheckReturnValue
	@Nonnull
	private static Method[] declaredMethodsOf(@Nonnull Class<?> clazz) {
		try {
			return clazz.getDeclaredMethods();
		}
		catch (LinkageError e) {
			return new Method[0];
		}
	}

	/**
	 * Whether {@code getDeclaredMethods()} fails on {@code clazz} or a superclass, which
	 * {@link #declaredMethodsOf} reads as having none. {@code getMethods()} links only public
	 * signatures, so a private method naming an absent type leaves it working while the protected
	 * overloads {@link #callableMethods} reads alongside it go unseen.
	 */
	@CheckReturnValue
	private static boolean declaredMethodsUnreadable(@Nonnull Class<?> clazz) {
		try {
			for (var owner = clazz; owner != null; owner = owner.getSuperclass())
				owner.getDeclaredMethods();
			return false;
		}
		catch (LinkageError e) {
			return true;
		}
	}

	@CheckReturnValue
	static int declaredTypeParameterCount(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return -1;

		try {
			return clazz.getTypeParameters().length;
		}
		catch (LinkageError | TypeNotPresentException | MalformedParameterizedTypeException e) {
			// a generic signature naming an absent type answers nothing about the arity
			return -1;
		}
	}

	/**
	 * Whether {@code fqcn} or a supertype of it has a field named {@code fieldName} that is not
	 * private: a public one of a class or an interface, or a protected or package-private one of a
	 * superclass, which a subclass in its package inherits. Null when the class does not load or its
	 * fields cannot be read, since an unread field may be that one.
	 */
	@CheckReturnValue
	@Nullable
	static Boolean declaresFieldNamed(@Nonnull String fqcn, @Nonnull String fieldName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		try {
			for (var field : clazz.getFields()) {
				if (field.getName().equals(fieldName))
					return true;
			}
			for (var owner = clazz; owner != null; owner = owner.getSuperclass()) {
				for (var field : owner.getDeclaredFields()) {
					if (field.getName().equals(fieldName) && !Modifier.isPrivate(field.getModifiers()))
						return true;
				}
			}
			return false;
		}
		catch (LinkageError e) {
			return null;
		}
	}

	/**
	 * Whether {@code fqcn} or a supertype of it declares a method named {@code methodName} whose
	 * erased parameter types are exactly {@code parameterFqcns}. Parameter types are compared as
	 * canonical names, so an array reads as {@code Foo[]} rather than the JVM's {@code [LFoo;},
	 * matching the check's own spelling.
	 *
	 * <p>Private supertype methods are excluded: they are not inherited and so cannot collide.
	 */
	@CheckReturnValue
	static boolean declaresMethodErasure(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			@Nonnull List<String> parameterFqcns
	) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		for (var method : methodsOf(clazz)) {
			if (matchesErasure(method, methodName, parameterFqcns))
				return true;
		}
		for (var current = clazz; current != null; current = current.getSuperclass()) {
			for (var method : declaredMethodsOf(current)) {
				if (!Modifier.isPrivate(method.getModifiers())
						&& matchesErasure(method, methodName, parameterFqcns))
					return true;
			}
		}
		return false;
	}

	/**
	 * Whether a subclass of {@code fqcn} has a method named {@code methodName} of any arity among
	 * the ones it inherits ({@link #callableMethods}), which is what makes the class the one a bare
	 * call of that name binds in (JLS 15.12.1). Null when the class does not load or its members
	 * cannot be read, since an unread member may be that one.
	 */
	@CheckReturnValue
	@Nullable
	static Boolean declaresMethodNamed(@Nonnull String fqcn, @Nonnull String methodName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null || hasUnreadableMembers(fqcn) || declaredMethodsUnreadable(clazz))
			return null;

		for (var method : callableMethods(clazz, false, true)) {
			if (method.getName().equals(methodName))
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code fqcn} or a supertype of it declares a method named {@code methodName} that a
	 * call of {@code argCount} arguments could select. A variable-arity method is applicable to every count
	 * from one below its declared one upward, so the counts need not match for one overload to take
	 * a call from another.
	 */
	@CheckReturnValue
	static boolean declaresOverloadAt(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		for (var method : methodsOf(clazz)) {
			// an implementor does not inherit an interface's static method, so it is not in the
			// overload set a call through this type resolves against
			if (!method.getName().equals(methodName)
					|| (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().isInterface()))
				continue;

			final var declared = method.getParameterCount();
			if (declared == argCount || (method.isVarArgs() && argCount >= declared - 1))
				return true;
		}

		// getMethods sees only public members, so a protected or package-private supertype overload
		// is invisible there while still being in the overload set a call resolves against
		for (var current = clazz; current != null; current = current.getSuperclass()) {
			for (var method : declaredMethodsOf(current)) {
				if (Modifier.isPrivate(method.getModifiers()) || !method.getName().equals(methodName)
						|| (Modifier.isStatic(method.getModifiers()) && current.isInterface()))
					continue;

				// a package-private member of a supertype in another package is not inherited, so it
				// is not in the overload set either
				if (!Modifier.isPublic(method.getModifiers()) && !Modifier.isProtected(method.getModifiers())
						&& !current.isInterface()
						&& !current.getPackageName().equals(clazz.getPackageName()))
					continue;

				final var declared = method.getParameterCount();
				if (declared == argCount || (method.isVarArgs() && argCount >= declared - 1))
					return true;
			}
		}
		return false;
	}

	/**
	 * The class {@code type} erases to (JLS 4.6), a type variable by its first bound. Only a class, a
	 * parameterized type or a type variable stands where a supertype, its type argument or a bound
	 * is read, so anything else erases to {@code Object}. So does a type variable whose bounds loop
	 * back to it ({@code <T extends U, U extends T>}), which javac rejects but other bytecode may
	 * carry.
	 */
	@CheckReturnValue
	@Nonnull
	private static Class<?> erasureOf(@Nonnull Type type) {
		final var visited = new HashSet<TypeVariable<?>>();
		var current = type;
		while (current instanceof TypeVariable<?> variable && visited.add(variable))
			current = variable.getBounds()[0];
		return switch (current) {
			case Class<?> clazz -> clazz;
			case ParameterizedType parameterized -> erasureOf(parameterized.getRawType());
			default -> Object.class;
		};
	}

	/**
	 * The exact type of the public field {@code fieldName} read through {@code fqcn}, as an
	 * {@link #exactMethods} key, or null when the class does not load or has no such field. An
	 * enum constant's type is its enum.
	 */
	@CheckReturnValue
	@Nullable
	static String exactFieldType(@Nonnull String fqcn, @Nonnull String fieldName, @Nullable List<String> typeArguments) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		try {
			for (var field : clazz.getFields()) {
				if (!field.getName().equals(fieldName))
					continue;
				if (Modifier.isStatic(field.getModifiers()))
					return exactKey(field.getGenericType(), Map.of());
				return typeArguments == null && clazz.getTypeParameters().length > 0
						? exactKey(field.getType(), Map.of())
						: exactKey(field.getGenericType(), memberBindings(clazz, field.getDeclaringClass(), typeArguments));
			}
		}
		catch (LinkageError | MalformedParameterizedTypeException | TypeNotPresentException e) {
			return null;
		}
		return null;
	}

	/**
	 * {@code type} as an {@link #exactMethods} key, a type variable read through
	 * {@code bindings}. A wildcard, a type variable {@code bindings} lacks, and a member type of a
	 * parameterized type, whose arguments its own key cannot carry, read as
	 * {@link #UNKNOWN_TYPE_KEY}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String exactKey(@Nonnull Type type, @Nonnull Map<TypeVariable<?>, String> bindings) {
		return switch (type) {
			case Class<?> clazz -> clazz.isArray() ? exactKey(clazz.getComponentType(), bindings) + "[]" : clazz.getName();
			case GenericArrayType array -> exactKey(array.getGenericComponentType(), bindings) + "[]";
			case ParameterizedType parameterized -> {
				if (parameterized.getOwnerType() instanceof ParameterizedType)
					yield UNKNOWN_TYPE_KEY;

				final var arguments = new StringJoiner(",", "<", ">");
				for (var argument : parameterized.getActualTypeArguments())
					arguments.add(exactKey(argument, bindings));
				yield exactKey(parameterized.getRawType(), bindings) + arguments;
			}
			case TypeVariable<?> variable -> bindings.getOrDefault(variable, UNKNOWN_TYPE_KEY);
			default -> UNKNOWN_TYPE_KEY;
		};
	}

	/**
	 * Each method named {@code methodName} that a call of {@code argCount} arguments through
	 * {@code fqcn} may select ({@link #callableMethods}), its types read as keys: the binary name,
	 * then any type arguments between angle brackets, then a {@code []} per array dimension. A class
	 * type variable reads what {@code typeArguments}, in order, binds {@code fqcn}'s type parameters
	 * to, carried up through the supertypes to the class declaring the method; anything else that
	 * cannot be told, a method's own type variable and a wildcard included, reads as
	 * {@link #UNKNOWN_TYPE_KEY}. Null {@code typeArguments} on a generic class is a raw receiver,
	 * whose members are erased (JLS 4.8). Null when the class does not load or its members cannot be
	 * read, so an unread overload is never taken to be absent.
	 */
	@CheckReturnValue
	@Nullable
	static List<ExactMethod> exactMethods(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int argCount,
			@Nullable List<String> typeArguments,
			boolean statics,
			boolean inherited
	) {
		final var clazz = loadClass(fqcn);
		if (clazz == null || hasUnreadableMembers(fqcn) || declaredMethodsUnreadable(clazz))
			return null;

		try {
			final var raw = typeArguments == null && !statics && clazz.getTypeParameters().length > 0;
			final var methods = new ArrayList<ExactMethod>();
			for (var method : nearestOverloads(callableMethods(clazz, statics, inherited), methodName, argCount)) {
				final Map<TypeVariable<?>, String> bindings = raw ? Map.of() : memberBindings(clazz, method.getDeclaringClass(), typeArguments);
				final var parameters = new ArrayList<String>();
				for (var i = 0; i < method.getParameterCount(); ++i) {
					parameters.add(raw
							? exactKey(method.getParameterTypes()[i], bindings)
							: exactKey(method.getGenericParameterTypes()[i], bindings)
					);
				}
				final var returnType = raw ? exactKey(method.getReturnType(), bindings) : exactKey(method.getGenericReturnType(), bindings);
				methods.add(new ExactMethod(parameters, returnType, method.isVarArgs()));
			}
			return methods;
		}
		catch (LinkageError | MalformedParameterizedTypeException | TypeNotPresentException e) {
			return null;
		}
	}

	/**
	 * What {@code target}'s type parameters are bound to as {@link #exactKey} keys, seen from
	 * {@code current}, a subtype of it whose own are bound by {@code bindings}: each supertype on the
	 * way passes its type arguments down, and a raw one binds nothing. Null when no supertype of
	 * {@code current} leads to {@code target}.
	 */
	@CheckReturnValue
	@Nullable
	private static Map<TypeVariable<?>, String> exactSupertypeBindings(
			@Nonnull Class<?> current,
			@Nonnull Class<?> target,
			@Nonnull Map<TypeVariable<?>, String> bindings
	) {
		if (current == target)
			return bindings;

		final var supertypes = new ArrayList<Type>(List.of(current.getGenericInterfaces()));
		if (current.getGenericSuperclass() != null)
			supertypes.add(current.getGenericSuperclass());
		for (var supertype : supertypes) {
			final var raw = erasureOf(supertype);
			if (!target.isAssignableFrom(raw))
				continue;

			final var next = new HashMap<TypeVariable<?>, String>();
			if (supertype instanceof ParameterizedType parameterized) {
				final var parameters = raw.getTypeParameters();
				final var arguments = parameterized.getActualTypeArguments();
				for (var i = 0; i < parameters.length && i < arguments.length; ++i)
					next.put(parameters[i], exactKey(arguments[i], bindings));
			}
			return exactSupertypeBindings(raw, target, next);
		}
		return null;
	}

	/**
	 * {@code clazz.getConstructors()} or {@code getMethods()}, or empty when a descriptor type is
	 * absent. See {@link #methodsOf}.
	 */
	@CheckReturnValue
	@Nonnull
	private static Executable[] executablesOf(@Nonnull Class<?> clazz, boolean constructors) {
		try {
			return constructors ? clazz.getConstructors() : clazz.getMethods();
		}
		catch (LinkageError e) {
			return new Executable[0];
		}
	}

	/**
	 * The declared type of the public field {@code fieldName} on {@code fqcn}, or null when there is
	 * no such field.
	 */
	@CheckReturnValue
	@Nullable
	static String fieldTypeName(@Nonnull String fqcn, @Nonnull String fieldName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		try {
			for (var field : clazz.getFields()) {
				if (field.getName().equals(fieldName))
					return field.getType().getName();
			}
		}
		catch (LinkageError e) {
			return null;
		}
		return null;
	}

	/**
	 * The {@code String} parameter of a {@code methodName} overload on {@code fqcn} taking
	 * {@code argCount} arguments that has a {@code Charset} sibling overload, with the class
	 * declaring that sibling, or null when there is none. {@code "new"} names the constructors.
	 */
	@CheckReturnValue
	@Nullable
	static CharsetParameter findCharsetParameter(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		final var methods = executablesOf(clazz, "new".equals(methodName));

		for (var method : methods) {
			if (!"new".equals(methodName) && !method.getName().equals(methodName))
				continue;
			final var params = method.getParameterTypes();
			if (params.length != argCount)
				continue;

			for (var i = 0; i < params.length; ++i) {
				if (params[i] != String.class)
					continue;
				final var overload = charsetOverload(methods, method, i);
				if (overload != null)
					return new CharsetParameter(i, overload.getDeclaringClass().getName());
			}
		}
		return null;
	}

	/**
	 * Given a FQCN, returns the simple name of the most specific collection
	 * interface the class implements (List, Set, Map, Deque, Queue, Collection),
	 * or {@code null} if the class is not a concrete collection type.
	 */
	@CheckReturnValue
	@Nullable
	public static String findCollectionInterface(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
			return null;

		try {
			if (List.class.isAssignableFrom(clazz) && Deque.class.isAssignableFrom(clazz))
				return null;

			if (Deque.class.isAssignableFrom(clazz))
				return "Deque";
			if (List.class.isAssignableFrom(clazz))
				return "List";
			if (Map.class.isAssignableFrom(clazz))
				return "Map";
			if (Queue.class.isAssignableFrom(clazz))
				return "Queue";
			if (Set.class.isAssignableFrom(clazz))
				return "Set";
			if (Collection.class.isAssignableFrom(clazz))
				return "Collection";
		}
		catch (LinkageError e) {
			// a supertype missing from the classpath makes isAssignableFrom throw rather than
			// answer, and an Error escaping a check aborts the run instead of failing one file
			return null;
		}
		return null;
	}

	/**
	 * The return type name of the public method on {@code fqcn} named {@code methodName}
	 * that accepts {@code argCount} arguments, or null when there is no such method or the
	 * surviving overloads disagree on the return type.
	 *
	 * <p>Bridge and synthetic methods are skipped: {@code getMethods()} lists the bridge
	 * for a covariant override first, so reading the first name match answers
	 * {@code AbstractStringBuilder} for {@code StringBuilder.append}.
	 *
	 * @return the JVM <em>binary</em> name, so an array comes back as {@code [C} or
	 *         {@code [Ljava.lang.String;} and a nested type as {@code Foo$Bar}. Never text to emit
	 *         into source.
	 */
	@CheckReturnValue
	@Nullable
	public static String getMethodReturnTypeName(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		String found = null;
		for (var method : methodsOf(clazz)) {
			if (method.isBridge() || method.isSynthetic())
				continue;
			if (!method.getName().equals(methodName) || !acceptsArgCount(method, argCount))
				continue;

			final var returnType = method.getReturnType().getName();
			if (found == null)
				found = returnType;
			else if (!found.equals(returnType))
				return null;
		}
		return found;
	}

	/**
	 * If at least one public method (or constructor when {@code methodName}
	 * is {@code "new"}) with the given name and parameter count is varargs,
	 * and no competing non-varargs overload exists, returns the component
	 * type of the varargs array parameter. Otherwise returns {@code null}.
	 *
	 * <p>A non-varargs overload "competes" only when its last parameter
	 * can accept an array argument (e.g. {@code Object} or {@code Object[]}).
	 * Non-varargs overloads whose last parameter cannot accept arrays
	 * (e.g. {@code Iterable}, {@code List}) are ignored because the
	 * compiler cannot select them when the call site passes an explicit
	 * array.
	 */
	@CheckReturnValue
	@Nullable
	static Class<?> getVarArgsComponentType(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		if (argCount <= 0)
			return null;

		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		final var methods = executablesOf(clazz, "new".equals(methodName));

		Class<?> componentType = null;
		for (var method : methods) {
			if (!"new".equals(methodName) && !method.getName().equals(methodName))
				continue;
			if (method.getParameterCount() != argCount)
				continue;

			final var params = method.getParameterTypes();
			final var lastParam = params[params.length - 1];
			if (method.isVarArgs()) {
				final var lastParamComponent = lastParam.getComponentType();
				if (componentType == null)
					componentType = lastParamComponent;
				else if (componentType != lastParamComponent)
					return null;
			}
			else if (lastParam.isArray() || lastParam.isAssignableFrom(Object[].class))
				return null;
		}
		return componentType;
	}

	/**
	 * Checks whether every overload of the named method on the given class
	 * accepting {@code argCount} arguments returns a method-level
	 * {@link TypeVariable} (one declared on the method itself, not the class)
	 * that none of its parameters supply. Class-level type parameters
	 * like {@code List<E>.get()} are excluded because {@code var} correctly
	 * infers the type from the receiver.
	 */
	@CheckReturnValue
	static boolean hasGenericReturnType(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		var hasAnyOverload = false;
		for (var method : methodsOf(clazz)) {
			if (!method.getName().equals(methodName) || !acceptsArgCount(method, argCount))
				continue;

			if (!needsTargetTypeInference(method, argCount))
				return false;

			hasAnyOverload = true;
		}
		return hasAnyOverload;
	}

	@CheckReturnValue
	static boolean hasMethod(@Nonnull String fqcn, @Nonnull String methodName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		for (var method : methodsOf(clazz)) {
			if (method.getName().equals(methodName))
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code fqcn} loads but its members cannot be read, because a type named in a method
	 * descriptor is absent. {@link #methodsOf} folds that into an empty array, which is the right
	 * answer for "does this declare X" and the wrong one for "is it safe to rewrite against this",
	 * where an unreadable type has to count as unknown rather than as declaring nothing.
	 */
	@CheckReturnValue
	static boolean hasUnreadableMembers(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		try {
			clazz.getMethods();
			return false;
		}
		catch (LinkageError e) {
			return true;
		}
	}

	/**
	 * The binary name of the member class or interface named {@code simpleName} that a subclass of
	 * {@code fqcn} in {@code fromPackage} (null for the default package) inherits (JLS 8.5), or, with
	 * {@code staticOnly}, that a static import of {@code fqcn} there brings in (JLS 7.5.3), along every
	 * path through its superclasses and superinterfaces. One counts when code in {@code fromPackage}
	 * may access it: a public one, a non-private one declared in {@code fromPackage}, or, by
	 * inheritance only, a protected one; with {@code staticOnly} only a static one. Any declaration of
	 * the name, counted or not, hides its declaring class's supertypes' member types of the name, so
	 * the path stops there. Null when there is none, {@link #AMBIGUOUS_TYPE_KEY} when two different
	 * ones remain, and {@link #UNKNOWN_TYPE_KEY} when the class does not load or its members cannot be
	 * read, since an unread member type may be that one.
	 */
	@CheckReturnValue
	@Nullable
	static String inheritedMemberClass(@Nonnull String fqcn, @Nonnull String simpleName, boolean staticOnly, @Nullable String fromPackage) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return UNKNOWN_TYPE_KEY;

		final var packageName = fromPackage == null ? "" : fromPackage;
		final var found = new HashSet<String>();
		try {
			final var pending = new ArrayDeque<Class<?>>();
			final var visited = new HashSet<Class<?>>();
			pending.add(clazz);
			while (!pending.isEmpty()) {
				final var owner = pending.poll();
				if (!visited.add(owner))
					continue;

				var hides = false;
				for (var member : owner.getDeclaredClasses()) {
					if (!member.getSimpleName().equals(simpleName))
						continue;
					hides = true;
					final var modifiers = member.getModifiers();
					if (Modifier.isPrivate(modifiers) || staticOnly && !Modifier.isStatic(modifiers))
						continue;
					if (Modifier.isPublic(modifiers) || owner.getPackageName().equals(packageName) || !staticOnly && Modifier.isProtected(modifiers))
						found.add(member.getName());
				}
				if (hides)
					continue;
				if (owner.getSuperclass() != null)
					pending.add(owner.getSuperclass());
				pending.addAll(List.of(owner.getInterfaces()));
			}
		}
		catch (LinkageError | SecurityException e) {
			return UNKNOWN_TYPE_KEY;
		}
		if (found.size() > 1)
			return AMBIGUOUS_TYPE_KEY;
		return found.isEmpty() ? null : found.iterator().next();
	}

	/**
	 * The return types of the methods named {@code methodName} that a call with {@code argCount}
	 * arguments made from inside a subclass of {@code fqcn} may bind to: the public and protected
	 * ones it inherits, read at the nearest declaration of each signature, so an override is read at
	 * its own return type. A superinterface's static methods are not inherited, so they are left out.
	 * Empty when the class does not load or none matches.
	 *
	 * <p>{@code typeArguments} are the key positions the subclass writes {@code fqcn}'s type
	 * arguments as, in order, and a class's type variable a method returns is read through them.
	 * Null when they cannot be read, which binds nothing, as do arguments that do not match
	 * {@code fqcn}'s type parameters. Empty on a generic {@code fqcn} is a raw
	 * supertype, whose members are erased (JLS 4.8), so each method answers its erasure.
	 *
	 * @return JVM binary names, as {@link #getMethodReturnTypeName} gives them, but a type variable's
	 *         binding as the key position it is bound to, or {@link #TYPE_VARIABLE_KEY}
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> inheritedReturnTypeNames(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int argCount,
			@Nullable List<String> typeArguments
	) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return List.of();

		final var candidates = new ArrayList<Method>();
		for (var owner = clazz; owner != null; owner = owner.getSuperclass()) {
			for (var method : declaredMethodsOf(owner)) {
				if (Modifier.isProtected(method.getModifiers()) || Modifier.isPublic(method.getModifiers()))
					candidates.add(method);
			}
		}
		candidates.addAll(List.of(methodsOf(clazz)));
		candidates.removeIf(method -> Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().isInterface());

		TypeVariable<?>[] parameters;
		try {
			parameters = clazz.getTypeParameters();
		}
		catch (LinkageError e) {
			// a malformed generic signature shows no type parameter
			parameters = new TypeVariable<?>[0];
		}

		final var bindings = new HashMap<TypeVariable<?>, String>();
		if (typeArguments != null && typeArguments.size() == parameters.length) {
			for (var i = 0; i < parameters.length; ++i)
				bindings.put(parameters[i], typeArguments.get(i));
		}

		final var raw = typeArguments != null && typeArguments.isEmpty() && parameters.length > 0;
		final var names = new ArrayList<String>();
		for (var method : nearestOverloads(candidates, methodName, argCount))
			names.add(raw ? method.getReturnType().getName() : returnTypeKey(method, clazz, bindings));
		return names;
	}

	@CheckReturnValue
	static boolean isCharSequenceNotString(@Nonnull String fqcn) {
		if ("java.lang.String".equals(fqcn))
			return false;

		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		try {
			return CharSequence.class.isAssignableFrom(clazz);
		}
		catch (LinkageError e) {
			return false;
		}
	}

	@CheckReturnValue
	private static boolean isChecked(@Nonnull Class<?> thrown) {
		return Exception.class.isAssignableFrom(thrown) && !RuntimeException.class.isAssignableFrom(thrown);
	}

	/**
	 * Checks whether the given class is a functional interface
	 * (an interface with exactly one abstract method, excluding
	 * methods inherited from {@link Object}).
	 */
	@CheckReturnValue
	static boolean isFunctionalInterface(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null || !clazz.isInterface())
			return false;

		var abstractCount = 0;
		for (var method : methodsOf(clazz)) {
			if (!Modifier.isAbstract(method.getModifiers()))
				continue;
			try {
				Object.class.getMethod(method.getName(), method.getParameterTypes());
				continue;
			}
			catch (NoSuchMethodException ignored) {
			}
			if (++abstractCount > 1)
				return false;
		}
		return abstractCount == 1;
	}

	/**
	 * Whether {@code fqcn} loads as an inner class (JLS 8.1.3) of a generic class: a member class that
	 * is not static, enclosed by a generic class directly or through further inner classes. Its type
	 * takes the type arguments of that enclosing instance, which its name does not carry. True as
	 * well when an enclosing class cannot be read, since it may be generic.
	 */
	@CheckReturnValue
	static boolean isInnerClassOfGeneric(@Nonnull String fqcn) {
		try {
			var clazz = loadClass(fqcn);
			while (clazz != null && clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers())) {
				final var enclosing = clazz.getEnclosingClass();
				if (enclosing.getTypeParameters().length > 0)
					return true;
				clazz = enclosing;
			}
			return false;
		}
		catch (LinkageError e) {
			return true;
		}
	}

	@CheckReturnValue
	public static boolean isResolvableClass(@Nonnull String fqn) {
		return loadClass(fqn) != null;
	}

	/**
	 * {@code type}'s position in a {@link #parameterKey}: its canonical name, or its binary name
	 * when it has none. Throws {@link LinkageError} when its enclosing class is absent.
	 */
	@CheckReturnValue
	@Nonnull
	private static String keyPosition(@Nonnull Class<?> type) {
		final var canonical = type.getCanonicalName();
		return canonical == null ? type.getName() : canonical;
	}

	/**
	 * The class {@code fqcn} names ({@link #resolveAndLoad}), cached for the run. A lookup that an
	 * overflow cut short is no answer, so it is not cached: the name may load fine with stack to
	 * spare. The cache is filled with a plain {@code putIfAbsent} rather than {@code computeIfAbsent},
	 * since an overflow inside the latter can leave its bin reserved for good, so every later lookup
	 * landing there throws.
	 */
	@CheckReturnValue
	@Nullable
	private static Class<?> loadClass(@Nonnull String fqcn) {
		final var cached = CLASS_CACHE.get(fqcn);
		if (cached != null)
			return cached.orElse(null);

		final var loaded = resolveAndLoad(fqcn);
		if (loaded == null)
			return null;
		CLASS_CACHE.putIfAbsent(fqcn, loaded);
		return loaded.orElse(null);
	}

	@CheckReturnValue
	private static boolean matchesErasure(
			@Nonnull Method method,
			@Nonnull String methodName,
			@Nonnull List<String> parameterFqcns
	) {
		// a bridge or synthetic method restates a signature the declared one already carries, and an
		// implementor does not inherit an interface's static method, so neither can collide
		if (method.isBridge() || method.isSynthetic()
				|| (Modifier.isStatic(method.getModifiers()) && method.getDeclaringClass().isInterface()))
			return false;

		if (!method.getName().equals(methodName))
			return false;

		final var params = method.getParameterTypes();
		if (params.length != parameterFqcns.size())
			return false;

		try {
			for (var i = 0; i < params.length; ++i) {
				final var canonical = params[i].getCanonicalName();
				if (canonical == null || !canonical.equals(parameterFqcns.get(i)))
					return false;
			}
		}
		catch (LinkageError e) {
			// a parameter type whose enclosing class is absent cannot show the erasures match
			return false;
		}
		return true;
	}

	/**
	 * What a member {@code declaring} declares reads its class's type variables as through a
	 * receiver of {@code clazz} written with {@code typeArguments}: {@code clazz}'s own bound to them
	 * in order, none when the counts differ, then carried up ({@link #exactSupertypeBindings}).
	 */
	@CheckReturnValue
	@Nonnull
	private static Map<TypeVariable<?>, String> memberBindings(
			@Nonnull Class<?> clazz,
			@Nonnull Class<?> declaring,
			@Nullable List<String> typeArguments
	) {
		final var parameters = clazz.getTypeParameters();
		final var bindings = new HashMap<TypeVariable<?>, String>();
		if (typeArguments != null && typeArguments.size() == parameters.length) {
			for (var i = 0; i < parameters.length; ++i)
				bindings.put(parameters[i], typeArguments.get(i));
		}

		final var declared = exactSupertypeBindings(clazz, declaring, bindings);
		return declared == null ? Map.of() : declared;
	}

	/**
	 * {@code clazz.getMethods()}, or empty when a type named in a method descriptor is missing from
	 * the classpath. Loading a class does not resolve its members' descriptor types, so a class that
	 * loaded cleanly still throws here, and an {@code Error} escaping a check aborts the whole
	 * checkstyle run rather than failing one file. Empty is the right answer either way: an absent
	 * descriptor type says nothing about the question being asked.
	 */
	@CheckReturnValue
	@Nonnull
	private static Method[] methodsOf(@Nonnull Class<?> clazz) {
		try {
			return clazz.getMethods();
		}
		catch (LinkageError e) {
			return new Method[0];
		}
	}

	/**
	 * The methods among {@code candidates} named {@code methodName} that a call with
	 * {@code argCount} arguments may select, each signature read at its first candidate, which a
	 * caller lists nearest first.
	 */
	@CheckReturnValue
	@Nonnull
	private static Collection<Method> nearestOverloads(@Nonnull List<Method> candidates, @Nonnull String methodName, int argCount) {
		final var nearest = new LinkedHashMap<List<Class<?>>, Method>();
		for (var method : candidates) {
			if (!method.isBridge() && !method.isSynthetic() && method.getName().equals(methodName) && acceptsArgCount(method, argCount))
				nearest.putIfAbsent(List.of(method.getParameterTypes()), method);
		}
		return nearest.values();
	}

	/**
	 * Checks whether the method has method-level type variables in its
	 * return type that do NOT appear in any of its parameter types.
	 * Such methods require target type inference from the left-hand side,
	 * meaning {@code var} would lose type information.
	 */
	@CheckReturnValue
	private static boolean needsTargetTypeInference(@Nonnull Method method, int argCount) {
		final var returnTypeVars = new HashSet<String>();
		try {
			collectMethodLevelTypeVars(method.getGenericReturnType(), returnTypeVars);
			if (returnTypeVars.isEmpty())
				return false;

			// a varargs parameter given no variadic argument supplies nothing to infer from,
			// so `List.of()` still needs its target type even though `of(E...)` mentions E
			final var params = method.getGenericParameterTypes();
			final var inferable = method.isVarArgs() && argCount < params.length ? params.length - 1 : params.length;
			for (var i = 0; i < inferable; ++i) {
				final var paramTypeVars = new HashSet<String>();
				collectMethodLevelTypeVars(params[i], paramTypeVars);
				returnTypeVars.removeAll(paramTypeVars);
			}
			return !returnTypeVars.isEmpty();
		}
		catch (LinkageError | TypeNotPresentException | MalformedParameterizedTypeException
				| StackOverflowError e) {
			// a generic signature naming an absent or pathologically nested type cannot show the
			// return needs inference.
			// The bound walks throw it too, so they sit inside the guard rather than after it
			return false;
		}
	}

	/**
	 * Whether widening the argument at {@code index} from {@code concreteFqcn} to {@code valueFqcn}
	 * leaves a call to {@code methodName} on {@code fqcn} binding as it does today. Every overload
	 * that could take the concrete type there must also take the widened one: those are exactly the
	 * candidates the call selects among now, so if any stops accepting, the selection changes or the
	 * call fails to bind at all. {@code "new"} asks about constructors.
	 */
	@CheckReturnValue
	static boolean parameterAcceptsType(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int argCount,
			int index,
			@Nonnull String concreteFqcn,
			@Nonnull String valueFqcn
	) {
		final var clazz = loadClass(fqcn);
		final var concrete = loadClass(concreteFqcn);
		final var value = loadClass(valueFqcn);
		if (clazz == null || concrete == null || value == null)
			return false;

		final var constructors = "new".equals(methodName);
		var candidates = 0;
		for (var candidate : executablesOf(clazz, constructors)) {
			if (!constructors && !candidate.getName().equals(methodName))
				continue;

			final var params = candidate.getParameterTypes();
			if (index >= params.length || (params.length != argCount && !candidate.isVarArgs()))
				continue;

			try {
				if (!params[index].isAssignableFrom(concrete))
					continue;
				if (!params[index].isAssignableFrom(value))
					return false;

				++candidates;
			}
			catch (LinkageError e) {
				// an unresolvable parameter type cannot show the call still binds
				return false;
			}
		}
		return candidates > 0;
	}

	/**
	 * Resolves an FQN to a {@link Class} accounting for inner-class names that
	 * use {@code .} in source but {@code $} in JVM form. Null when an attempt overflowed the
	 * stack ({@link #tryLoadClass}), so the answer is not known.
	 */
	@Nullable
	private static Optional<Class<?>> resolveAndLoad(@Nonnull String fqcn) {
		final var heuristic = applyJavaNamingHeuristic(fqcn);
		if (!heuristic.equals(fqcn)) {
			final var loaded = tryLoadClass(heuristic);
			if (loaded == null || loaded.isPresent())
				return loaded;
		}

		var name = fqcn;
		for (var attempt = 0; attempt <= MAX_INNER_CLASS_DEPTH; ++attempt) {
			final var loaded = tryLoadClass(name);
			if (loaded == null || loaded.isPresent())
				return loaded;
			final var lastDot = name.lastIndexOf('.');
			if (lastDot < 0 || lastDot + 1 >= name.length()
					|| !Character.isUpperCase(name.charAt(lastDot + 1)))
				return Optional.empty();
			name = name.substring(0, lastDot) + '$' + name.substring(lastDot + 1);
		}
		return Optional.empty();
	}

	/**
	 * Resolves a simple class name to a fully qualified class name using
	 * the provided imports, package name, and java.lang fallback.
	 *
	 * @return the FQCN, or {@code null} if it cannot be resolved
	 */
	@CheckReturnValue
	@Nullable
	public static String resolveClassName(@Nonnull String simpleName, @Nullable String packageName, @Nonnull Set<String> imports) {
		if (simpleName.isEmpty())
			return null;

		// arrays don't have method overloads our checks care about, and JVM array
		// notation differs from Java's (`[Ljava.lang.String;` vs `String[]`),
		// so reject them early instead of letting Class.forName fail.
		if (simpleName.endsWith("]"))
			return null;

		if (simpleName.contains("."))
			return simpleName;

		for (var imp : imports) {
			if (imp.endsWith("." + simpleName))
				return imp;
		}

		// a type in the current package outranks any import-on-demand (JLS 6.5.5), so it is tried
		// before the wildcards rather than after them
		if (packageName != null) {
			final var candidate = packageName + "." + simpleName;
			if (loadClass(candidate) != null)
				return candidate;
		}

		// sorted so two wildcards offering the same simple name resolve the same way every run;
		// that input is a compile error either way, but an arbitrary answer is not worth having
		for (var imp : new TreeSet<>(imports)) {
			if (imp.endsWith(".*")) {
				final var candidate = imp.substring(0, imp.length() - 1) + simpleName;
				if (loadClass(candidate) != null)
					return candidate;
			}
		}

		final var candidate = "java.lang." + simpleName;
		if (loadClass(candidate) != null)
			return candidate;

		return null;
	}

	/**
	 * Whether every overload a call of {@code methodName} with {@code argCount} arguments could
	 * select on {@code concreteFqcn} also exists on {@code ifaceFqcn} with the same erased parameter
	 * types AND the same erased return type. Both halves matter: an overload the interface lacks
	 * makes the call stop binding, and a covariant override changes what it yields, so
	 * {@code ConcurrentHashMap.keySet()} hands back a {@code KeySetView} where {@code Map.keySet()}
	 * hands back a {@code Set} and whatever stored the result stops compiling.
	 *
	 * <p>Matched per overload rather than by collapsing the whole name and arity into one answer,
	 * so a concrete type declaring {@code remove(int)} beside {@code remove(Object)} is decided
	 * pairwise instead of the two poisoning each other.
	 */
	@CheckReturnValue
	static boolean returnsTheSameType(
			@Nonnull String concreteFqcn,
			@Nonnull String ifaceFqcn,
			@Nonnull String methodName,
			int argCount
	) {
		final var concrete = loadClass(concreteFqcn);
		final var iface = loadClass(ifaceFqcn);
		if (concrete == null || iface == null)
			return false;

		final var ifaceMethods = methodsOf(iface);
		var matched = 0;
		for (var candidate : methodsOf(concrete)) {
			if (!selectableAt(candidate, methodName, argCount))
				continue;

			var found = false;
			for (var declared : ifaceMethods) {
				if (!selectableAt(declared, methodName, argCount)
						|| !sameErasedOverload(declared, candidate))
					continue;

				if (!declared.getReturnType().equals(candidate.getReturnType()))
					return false;

				found = true;
				break;
			}
			if (!found)
				return false;

			++matched;
		}
		return matched > 0;
	}

	/**
	 * The return type of {@code method} called on {@code view}, whose type parameters
	 * {@code bindings} binds as key positions: a class's type variable is what the supertypes from
	 * the view up to the class declaring it bind it to, a method's own one or one nothing binds is
	 * {@link #TYPE_VARIABLE_KEY}, and anything else is its erasure, as a binary name.
	 */
	@CheckReturnValue
	@Nonnull
	private static String returnTypeKey(@Nonnull Method method, @Nonnull Class<?> view, @Nonnull Map<TypeVariable<?>, String> bindings) {
		final Type generic;
		try {
			generic = method.getGenericReturnType();
		}
		catch (LinkageError | MalformedParameterizedTypeException | TypeNotPresentException e) {
			// a signature naming an absent type spells a parameterized or array type, never a bare
			// type variable, so the erasure is still the type the call returns
			return method.getReturnType().getName();
		}
		if (!(generic instanceof TypeVariable<?> variable))
			return method.getReturnType().getName();
		if (!(variable.getGenericDeclaration() instanceof Class<?> declaring))
			return TYPE_VARIABLE_KEY;

		try {
			final var declared = supertypeBindings(view, declaring, bindings);
			return declared == null ? TYPE_VARIABLE_KEY : declared.getOrDefault(variable, TYPE_VARIABLE_KEY);
		}
		catch (LinkageError | MalformedParameterizedTypeException | TypeNotPresentException e) {
			// a supertype naming an absent type cannot show what it binds the variable to
			return TYPE_VARIABLE_KEY;
		}
	}

	/**
	 * Whether {@code declared} and {@code candidate} are the same overload once the receiver widens.
	 * Positions are compared by assignability rather than identity because a bounded type variable
	 * erases more narrowly on the concrete type than on the interface: {@code EnumMap.put} erases to
	 * {@code put(Enum, Object)} where {@code Map.put} erases to {@code put(Object, Object)}, and
	 * those are still the one overload a call selects.
	 */
	@CheckReturnValue
	private static boolean sameErasedOverload(@Nonnull Method declared, @Nonnull Method candidate) {
		final var declaredParams = declared.getParameterTypes();
		final var candidateParams = candidate.getParameterTypes();
		if (declaredParams.length != candidateParams.length)
			return false;

		for (var i = 0; i < declaredParams.length; ++i) {
			if (!declaredParams[i].isAssignableFrom(candidateParams[i]))
				return false;
		}
		return true;
	}

	/**
	 * Whether code in {@code fromPackage} (null for the default package) sees a field named
	 * {@code fieldName} that {@code fqcn} has, so that the name reads as that field. By inheritance
	 * ({@code staticImport} false): a public field of the class, its superclasses or interfaces, a
	 * protected one of a superclass, or a package-private one declared in {@code fromPackage}. Through
	 * a static on-demand import of the class: its static fields only, public ones or ones declared in
	 * {@code fromPackage}, since a protected one of another package is not accessible there. Never a
	 * private one. Null when the class does not load or its fields cannot be read.
	 */
	@CheckReturnValue
	@Nullable
	static Boolean seesField(@Nonnull String fqcn, @Nonnull String fieldName, @Nullable String fromPackage, boolean staticImport) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		final var packageName = fromPackage == null ? "" : fromPackage;
		try {
			for (var field : clazz.getFields()) {
				if (field.getName().equals(fieldName) && (!staticImport || Modifier.isStatic(field.getModifiers())))
					return true;
			}
			for (var owner = clazz; owner != null; owner = owner.getSuperclass()) {
				for (var field : owner.getDeclaredFields()) {
					final var modifiers = field.getModifiers();
					if (!field.getName().equals(fieldName) || Modifier.isPrivate(modifiers) || staticImport && !Modifier.isStatic(modifiers))
						continue;
					if (owner.getPackageName().equals(packageName) || !staticImport && Modifier.isProtected(modifiers))
						return true;
				}
			}
			return false;
		}
		catch (LinkageError e) {
			return null;
		}
	}

	/**
	 * Whether {@code method} is one a call of {@code methodName} with {@code argCount} arguments
	 * could bind to. A bridge restates a signature the real method already carries, and a static
	 * member is never selected through an instance receiver, so neither is a candidate.
	 */
	@CheckReturnValue
	private static boolean selectableAt(@Nonnull Method method, @Nonnull String methodName, int argCount) {
		return !method.isBridge() && !Modifier.isStatic(method.getModifiers())
				&& method.getName().equals(methodName) && acceptsArgCount(method, argCount);
	}

	/**
	 * The return types of the static methods named {@code methodName} that a call with
	 * {@code argCount} arguments may bind to through a static import from {@code fqcn}: those it
	 * declares or inherits from a superclass, read at the nearest declaration of each signature, a
	 * private one left out. A superinterface's static methods are no members of it (JLS 8.4.8), so
	 * they are not read. A method's own type variable answers {@link #TYPE_VARIABLE_KEY}, as the
	 * call's arguments or target infer it. Empty when the class does not load or none matches.
	 *
	 * @return JVM binary names, as {@link #getMethodReturnTypeName} gives them
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> staticReturnTypeNames(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return List.of();

		final var candidates = new ArrayList<Method>();
		for (var owner = clazz; owner != null; owner = owner.getSuperclass()) {
			for (var method : declaredMethodsOf(owner)) {
				if (Modifier.isStatic(method.getModifiers()) && !Modifier.isPrivate(method.getModifiers()))
					candidates.add(method);
			}
		}

		final var names = new ArrayList<String>();
		for (var method : nearestOverloads(candidates, methodName, argCount))
			names.add(returnTypeKey(method, clazz, Map.of()));
		return names;
	}

	/**
	 * What {@code target}'s type parameters are bound to as key positions, seen from {@code current},
	 * a subtype of it whose own are bound by {@code bindings}: each supertype on the way passes its
	 * type arguments down, and a raw one binds its type parameters to their erasures (JLS 4.8). Null
	 * when no supertype of {@code current} leads to {@code target}.
	 */
	@CheckReturnValue
	@Nullable
	private static Map<TypeVariable<?>, String> supertypeBindings(
			@Nonnull Class<?> current,
			@Nonnull Class<?> target,
			@Nonnull Map<TypeVariable<?>, String> bindings
	) {
		if (current == target)
			return bindings;
		// an interface lists no Object supertype, and a type without type parameters binds none
		if (target.getTypeParameters().length == 0)
			return target.isAssignableFrom(current) ? Map.of() : null;

		final var supertypes = new ArrayList<Type>(List.of(current.getGenericInterfaces()));
		if (current.getGenericSuperclass() != null)
			supertypes.add(current.getGenericSuperclass());
		for (var supertype : supertypes) {
			final var raw = erasureOf(supertype);
			if (!target.isAssignableFrom(raw))
				continue;

			final var parameters = raw.getTypeParameters();
			final var next = new HashMap<TypeVariable<?>, String>();
			if (supertype instanceof ParameterizedType parameterized) {
				final var arguments = parameterized.getActualTypeArguments();
				for (var i = 0; i < parameters.length; ++i)
					next.put(parameters[i], argumentKey(arguments[i], bindings));
			}
			else {
				for (var parameter : parameters)
					next.put(parameter, keyPosition(erasureOf(parameter)));
			}
			return supertypeBindings(raw, target, next);
		}
		return null;
	}

	/**
	 * The class {@code name} loads as, empty when it does not, or null when the attempt overflowed
	 * the stack, which says nothing about the name.
	 */
	@CheckReturnValue
	@Nullable
	private static Optional<Class<?>> tryLoadClass(@Nonnull String name) {
		classForNameCallCount.incrementAndGet();
		try {
			return Optional.of(Class.forName(name, false, ReflectionUtil.class.getClassLoader()));
		}
		catch (ClassNotFoundException | LinkageError e) {
			return Optional.empty();
		}
		catch (StackOverflowError e) {
			return null;
		}
	}

	private ReflectionUtil() {
	}
}