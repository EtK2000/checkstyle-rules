package com.etk2000.checkstyle;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Utility for resolving class names and inspecting method signatures
 * via reflection. Designed for use by checkstyle checks that need
 * type information beyond what the AST provides.
 * <p>
 * All methods are static. The class cache uses a plain {@link HashMap}
 * because checkstyle's TreeWalker processes files sequentially on a
 * single thread, so no synchronization is needed.
 */
class ReflectionUtil {
	// caps the right-to-left dot-to-$ substitution loop in resolveAndLoad so
	// pathologically deep FQNs don't pay O(d) Class.forName failures.
	private static final int MAX_INNER_CLASS_DEPTH = 10;

	// plain HashMap is fine: TreeWalker is single-threaded, append-only cache.
	// Optional.empty() represents a known-absent class so retries don't repeat
	// Class.forName scans for unresolvable names.
	private static final Map<String, Optional<Class<?>>> CLASS_CACHE = new HashMap<>();

	// test instrumentation: counts Class.forName invocations so tests can
	// verify the heuristic short-circuits on common cases. Tests reset this
	// directly; clearCache only wipes the resolution cache.
	static int classForNameCallCount;

	/**
	 * Returns {@code fqcn} with dots between adjacent uppercase-starting
	 * segments (after the first uppercase segment) rewritten to {@code $}.
	 * Returns {@code fqcn} unchanged if any segment after the first uppercase
	 * one starts non-uppercase, so the resulting name never reaches outside
	 * the substitution space the slow-path lowercase guard would consider.
	 */
	@CheckReturnValue
	@Nonnull
	static String applyJavaNamingHeuristic(@Nonnull String fqcn) {
		// Pass 1: detect the mitigation trigger. Bail out without rewriting if
		// any segment after the first uppercase segment starts non-uppercase;
		// otherwise pass 2's substitutions are safe (within slow-path's space).
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

		// Pass 2: emit substitutions. All post-first-class segments are now
		// known to start uppercase, so every '.' followed by another segment
		// becomes '$'.
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
	 * Test instrumentation: clears the resolution cache so a subsequent
	 * {@link #classForNameCallCount} measurement reflects fresh lookups.
	 */
	static void clearCache() {
		CLASS_CACHE.clear();
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
			default -> {
			}
		}
	}

	/**
	 * Finds a {@code String} parameter index in a method (or constructor)
	 * with {@code argCount} parameters where a sibling overload exists
	 * that replaces that {@code String} with {@code Charset}.
	 *
	 * @return the 0-based index of the charset {@code String} parameter,
	 * or -1 if no such overload pair exists
	 */
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
	 * Priority: List > Set > Map > Deque > Queue > Collection.
	 */
	@CheckReturnValue
	@Nullable
	static String findCollectionInterface(@Nonnull String fqcn) {
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
	static boolean hasGenericReturnType(@Nonnull String fqcn, @Nonnull String methodName) {
		final var clazz = loadClass(fqcn);
		if (clazz == null)
			return false;

		var hasAnyOverload = false;
		for (var method : clazz.getMethods()) {
			if (!method.getName().equals(methodName))
				continue;

			if (!needsTargetTypeInference(method))
				return false; // at least one overload is inferable from args

			hasAnyOverload = true;
		}
		return hasAnyOverload;
	}

	/**
	 * Checks whether the given class has a public method with the specified name.
	 */
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
			// Object methods don't count for functional interface definition
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
	private static boolean needsTargetTypeInference(@Nonnull Method method) {
		final var returnTypeVars = new HashSet<String>();
		collectMethodLevelTypeVars(method.getGenericReturnType(), returnTypeVars);
		if (returnTypeVars.isEmpty())
			return false;

		// remove type vars that appear in parameter types (inferable from args)
		for (var paramType : method.getGenericParameterTypes()) {
			final var paramTypeVars = new HashSet<String>();
			collectMethodLevelTypeVars(paramType, paramTypeVars);
			returnTypeVars.removeAll(paramTypeVars);
		}
		return !returnTypeVars.isEmpty();
	}

	/**
	 * Resolves an FQN to a {@link Class} accounting for inner-class names that
	 * use {@code .} in source but {@code $} in JVM form.
	 * <p>
	 * Fast path: assume Java naming convention (lowercase segments are
	 * packages, uppercase segments start classes) and rewrite all dots between
	 * adjacent uppercase-starting segments to {@code $} in one pass. One
	 * {@link Class#forName} call covers most top-level and inner-class FQNs.
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
	static String resolveClassName(@Nonnull String simpleName, @Nullable String packageName, @Nonnull Set<String> imports) {
		if (simpleName.isEmpty())
			return null;

		// arrays don't have method overloads our checks care about, and JVM array
		// notation differs from Java's (`[Ljava.lang.String;` vs `String[]`),
		// so reject them early instead of letting Class.forName fail.
		if (simpleName.endsWith("]"))
			return null;

		// already fully qualified
		if (simpleName.contains("."))
			return simpleName;

		// check explicit imports
		for (var imp : imports) {
			if (imp.endsWith("." + simpleName))
				return imp;
		}

		// check wildcard imports
		for (var imp : imports) {
			if (imp.endsWith(".*")) {
				final var candidate = imp.substring(0, imp.length() - 1) + simpleName;
				if (loadClass(candidate) != null)
					return candidate;
			}
		}

		// try same package
		if (packageName != null) {
			final var candidate = packageName + "." + simpleName;
			if (loadClass(candidate) != null)
				return candidate;
		}

		// try java.lang
		final var candidate = "java.lang." + simpleName;
		if (loadClass(candidate) != null)
			return candidate;

		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static Optional<Class<?>> tryLoadClass(@Nonnull String name) {
		++classForNameCallCount;
		try {
			return Optional.of(Class.forName(name, false, ReflectionUtil.class.getClassLoader()));
		}
		catch (ClassNotFoundException | NoClassDefFoundError e) {
			return Optional.empty();
		}
	}

	private ReflectionUtil() {
	}
}