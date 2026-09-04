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
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Queue;
import java.util.Set;
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

	@CheckReturnValue
	private static boolean acceptsArgCount(@Nonnull Method method, int argCount) {
		final var declared = method.getParameterCount();
		return method.isVarArgs() ? argCount >= declared - 1 : argCount == declared;
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

	@CheckReturnValue
	static int findCharsetStringArgIndex(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return -1;

		final var methods = executablesOf(clazz, "new".equals(methodName));

		for (var method : methods) {
			if (!"new".equals(methodName) && !method.getName().equals(methodName))
				continue;
			final var params = method.getParameterTypes();
			if (params.length != argCount)
				continue;

			for (var i = 0; i < params.length; ++i) {
				if (params[i] == String.class && hasCharsetOverload(methods, method, i))
					return i;
			}
		}
		return -1;
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
	 * Returns the return type name of the first public method with the
	 * specified name on the given class, or {@code null} if not found.
	 */
	@CheckReturnValue
	@Nullable
	static String getMethodReturnTypeName(@Nonnull String fqcn, @Nonnull String methodName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return null;

		for (var method : methodsOf(clazz)) {
			if (method.getName().equals(methodName))
				return method.getReturnType().getName();
		}
		return null;
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
	 * Checks whether there is a sibling overload of the given method that
	 * replaces the parameter at {@code stringIndex} with {@code Charset},
	 * keeping all other parameters the same.
	 */
	@CheckReturnValue
	private static boolean hasCharsetOverload(@Nonnull Executable[] methods, @Nonnull Executable method, int stringIndex) {
		final var params = method.getParameterTypes();
		for (var candidate : methods) {
			if (candidate == method)
				continue;
			// findCharsetStringArgIndex always passes a single-kind array (all
			// methods or all constructors), so this cross-kind skip is defensive
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
				return true;
		}
		return false;
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
	 * Checks whether the given class is or extends {@link CharSequence}
	 * but is NOT {@link String}. {@code CharSequence.isEmpty()} is a
	 * default method added in Java 15 (Android API 35), while
	 * {@code String.isEmpty()} has been available since Java 6 (API 1).
	 */
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

	@CheckReturnValue
	public static boolean isResolvableClass(@Nonnull String fqn) {
		return loadClass(fqn) != null;
	}

	@CheckReturnValue
	@Nullable
	private static Class<?> loadClass(@Nonnull String fqcn) {
		return CLASS_CACHE.computeIfAbsent(fqcn, ReflectionUtil::resolveAndLoad).orElse(null);
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
	 * Whether a parameter of type {@code parameterFqcn} accepts {@code constructedFqcn} but not
	 * {@code declaredFqcn}. Such a parameter is the one an argument would bind to once its
	 * declared type is narrowed to the constructed one, so the call would select a different
	 * overload than it does today.
	 */
	@CheckReturnValue
	static boolean parameterSelectsOnlyTheConstructedType(
			@Nonnull String parameterFqcn,
			@Nonnull String declaredFqcn,
			@Nonnull String constructedFqcn
	) {
		final var parameter = loadClass(parameterFqcn);
		final var declared = loadClass(declaredFqcn);
		final var constructed = loadClass(constructedFqcn);
		if (parameter == null || declared == null || constructed == null)
			return false;

		try {
			return parameter.isAssignableFrom(constructed) && !parameter.isAssignableFrom(declared);
		}
		catch (LinkageError e) {
			// an unresolvable hierarchy cannot show the parameter selects only the constructed type
			return false;
		}
	}

	/**
	 * Resolves an FQN to a {@link Class} accounting for inner-class names that
	 * use {@code .} in source but {@code $} in JVM form.
	 */
	@Nonnull
	private static Optional<Class<?>> resolveAndLoad(@Nonnull String fqcn) {
		final var heuristic = applyJavaNamingHeuristic(fqcn);
		if (!heuristic.equals(fqcn)) {
			final var loaded = tryLoadClass(heuristic);
			if (loaded.isPresent())
				return loaded;
		}

		var name = fqcn;
		for (var attempt = 0; attempt <= MAX_INNER_CLASS_DEPTH; ++attempt) {
			final var loaded = tryLoadClass(name);
			if (loaded.isPresent())
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
	 * Whether {@code method} is one a call of {@code methodName} with {@code argCount} arguments
	 * could bind to. A bridge restates a signature the real method already carries, and a static
	 * member is never selected through an instance receiver, so neither is a candidate.
	 */
	@CheckReturnValue
	private static boolean selectableAt(@Nonnull Method method, @Nonnull String methodName, int argCount) {
		return !method.isBridge() && !Modifier.isStatic(method.getModifiers())
				&& method.getName().equals(methodName) && acceptsArgCount(method, argCount);
	}

	@CheckReturnValue
	@Nonnull
	private static Optional<Class<?>> tryLoadClass(@Nonnull String name) {
		classForNameCallCount.incrementAndGet();
		try {
			return Optional.of(Class.forName(name, false, ReflectionUtil.class.getClassLoader()));
		}
		catch (ClassNotFoundException | LinkageError | StackOverflowError e) {
			return Optional.empty();
		}
	}

	private ReflectionUtil() {
	}
}