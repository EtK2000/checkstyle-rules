package com.etk2000.checkstyle;

import org.jetbrains.annotations.TestOnly;

import java.lang.reflect.Executable;
import java.lang.reflect.GenericArrayType;
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

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility for resolving class names and inspecting method signatures
 * via reflection. Designed for use by checkstyle checks that need
 * type information beyond what the AST provides.
 */
public final class ReflectionUtil {
	// caps the right-to-left dot-to-$ substitution loop in resolveAndLoad so
	// pathologically deep FQNs don't pay O(d) Class.forName failures.
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

	/**
	 * Whether {@code method} could be the one invoked with {@code argCount} arguments.
	 * Overloads of other arities are irrelevant to the call being classified: without
	 * this, {@code List.of()} is judged by {@code List.of(E)}, which infers its type
	 * from an argument the no-arg call does not have.
	 */
	@CheckReturnValue
	private static boolean acceptsArgCount(@Nonnull Method method, int argCount) {
		final var declared = method.getParameterCount();
		return method.isVarArgs() ? argCount >= declared - 1 : argCount == declared;
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

	@TestOnly
	static void clearCache() {
		CLASS_CACHE.clear();
	}

	/**
	 * The fully qualified name of the collection interface {@code fqcn} is best declared as: the
	 * interface itself when it already is one, or the one {@link #findCollectionInterface} picks.
	 * Callers comparing two spellings need a single canonical form, which a simple name cannot give
	 * ({@code List} and {@code java.util.List} name one type but differ as strings).
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

	private static void collectMethodLevelTypeVars(@Nonnull Type type, @Nonnull Set<String> result) {
		switch (type) {
			case TypeVariable<?> tv when tv.getGenericDeclaration() instanceof Method ->
					result.add(tv.getName());
			case ParameterizedType pt -> {
				for (var arg : pt.getActualTypeArguments())
					collectMethodLevelTypeVars(arg, result);
			}
			case WildcardType wt -> {
				for (var bound : wt.getUpperBounds())
					collectMethodLevelTypeVars(bound, result);
				for (var bound : wt.getLowerBounds())
					collectMethodLevelTypeVars(bound, result);
			}
			// a varargs parameter is an array of the type variable, so without this the
			// element type is never collected and the method looks target-typed
			case GenericArrayType ga -> collectMethodLevelTypeVars(ga.getGenericComponentType(), result);
			default -> {
			}
		}
	}

	@CheckReturnValue
	static int declaredTypeParameterCount(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		return clazz == null ? -1 : clazz.getTypeParameters().length;
	}

	@CheckReturnValue
	static int findCharsetStringArgIndex(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return -1;

		final var methods = "new".equals(methodName)
				? clazz.getConstructors()
				: clazz.getMethods();

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
	 * Only flags concrete (non-abstract, non-interface) classes.
	 * Priority: Deque > List > Map > Queue > Set > Collection.
	 */
	@CheckReturnValue
	@Nullable
	public static String findCollectionInterface(@Nonnull String fqcn) {
		final var clazz = loadClass(fqcn);
		if (clazz == null || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
			return null;

		// skip types that implement multiple collection interfaces
		// (e.g. LinkedList implements both List and Deque)
		if (List.class.isAssignableFrom(clazz) && Deque.class.isAssignableFrom(clazz))
			return null;

		// alphabetical, except Collection last (matches everything)
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

		for (var method : clazz.getMethods()) {
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

		final var methods = "new".equals(methodName)
				? clazz.getConstructors()
				: clazz.getMethods();

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
	 * Checks whether any overload of the named method on the given class
	 * has a method-level generic return type (i.e. returns a {@link TypeVariable}
	 * declared on the method itself, not the class). Class-level type parameters
	 * like {@code List<E>.get()} are excluded because {@code var} correctly
	 * infers the type from the receiver.
	 */
	@CheckReturnValue
	static boolean hasGenericReturnType(@Nonnull String fqcn, @Nonnull String methodName, int argCount) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		var hasAnyOverload = false;
		for (var method : clazz.getMethods()) {
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

		for (var method : clazz.getMethods()) {
			if (method.getName().equals(methodName))
				return true;
		}
		return false;
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
		return clazz != null && CharSequence.class.isAssignableFrom(clazz);
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
		for (var method : clazz.getMethods()) {
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
	 * Whether {@code fqn} names a type loadable from the current classpath.
	 */
	@CheckReturnValue
	public static boolean isResolvableClass(@Nonnull String fqn) {
		return loadClass(fqn) != null;
	}

	@CheckReturnValue
	@Nullable
	private static Class<?> loadClass(@Nonnull String fqcn) {
		return CLASS_CACHE.computeIfAbsent(fqcn, ReflectionUtil::resolveAndLoad).orElse(null);
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
		return parameter != null && declared != null && constructed != null
				&& parameter.isAssignableFrom(constructed) && !parameter.isAssignableFrom(declared);
	}

	/**
	 * Resolves an FQN to a {@link Class} accounting for inner-class names that
	 * use {@code .} in source but {@code $} in JVM form.
	 * <p>
	 * Fast path: assume Java naming convention (lowercase segments are
	 * packages, uppercase segments start classes) and rewrite all dots between
	 * adjacent uppercase-starting segments to {@code $} in one pass. One
	 * {@link Class#forName(String)} call covers most top-level and inner-class FQNs.
	 * <p>
	 * Slow path: if the heuristic was wrong (rare: class name violates
	 * convention, or the heuristic substitution doesn't match the actual
	 * binary name), fall back to right-to-left dot-to-{@code $} substitution
	 * starting from the original name. Bounded by {@link #MAX_INNER_CLASS_DEPTH}.
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

		for (var imp : imports) {
			if (imp.endsWith(".*")) {
				final var candidate = imp.substring(0, imp.length() - 1) + simpleName;
				if (loadClass(candidate) != null)
					return candidate;
			}
		}

		if (packageName != null) {
			final var candidate = packageName + "." + simpleName;
			if (loadClass(candidate) != null)
				return candidate;
		}

		final var candidate = "java.lang." + simpleName;
		if (loadClass(candidate) != null)
			return candidate;

		return null;
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