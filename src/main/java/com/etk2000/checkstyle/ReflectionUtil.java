package com.etk2000.checkstyle;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
	// plain HashMap is fine: TreeWalker is single-threaded, append-only cache
	private static final Map<String, Class<?>> CLASS_CACHE = new HashMap<>();

	@CheckReturnValue
	private static void collectMethodLevelTypeVars(@Nonnull Type type, @Nonnull Set<String> result) {
		switch (type) {
			case TypeVariable<?> tv when tv.getGenericDeclaration() instanceof Method ->
					result.add(tv.getName());
			case ParameterizedType pt -> {
				for (final var arg : pt.getActualTypeArguments())
					collectMethodLevelTypeVars(arg, result);
			}
			case WildcardType wt -> {
				for (final var bound : wt.getUpperBounds())
					collectMethodLevelTypeVars(bound, result);
				for (final var bound : wt.getLowerBounds())
					collectMethodLevelTypeVars(bound, result);
			}
			default -> {
			}
		}
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

		for (final var method : clazz.getMethods()) {
			if (method.getName().equals(methodName))
				return method.getReturnType().getName();
		}
		return null;
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
		for (final var method : clazz.getMethods()) {
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

		for (final var method : clazz.getMethods()) {
			if (method.getName().equals(methodName))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static Class<?> loadClass(@Nonnull String fqcn) {
		return CLASS_CACHE.computeIfAbsent(
				fqcn,
				name -> {
					try {
						return Class.forName(name, false, ReflectionUtil.class.getClassLoader());
					}
					catch (ClassNotFoundException e) {
						return null;
					}
				}
		);
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
		for (final var paramType : method.getGenericParameterTypes()) {
			final var paramTypeVars = new HashSet<String>();
			collectMethodLevelTypeVars(paramType, paramTypeVars);
			returnTypeVars.removeAll(paramTypeVars);
		}
		return !returnTypeVars.isEmpty();
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
		// already fully qualified
		if (simpleName.contains("."))
			return simpleName;

		// check explicit imports
		for (final var imp : imports) {
			if (imp.endsWith("." + simpleName))
				return imp;
		}

		// check wildcard imports
		for (final var imp : imports) {
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

	private ReflectionUtil() {
	}
}