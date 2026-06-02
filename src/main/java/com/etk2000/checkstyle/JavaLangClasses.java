package com.etk2000.checkstyle;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Version-aware oracle of the public top-level {@code java.lang} types implicitly imported into every
 * Java compilation unit. {@link #forJavaTarget(int)} returns the set available as standard
 * (non-preview) API at a given source language level.
 *
 * <p>Static-import-constant resolution uses this to short-circuit the wildcard-import fallback: when
 * a class chain's leftmost segment is a known {@code java.lang} type and no explicit import names a
 * class with the same simple name, the chain resolves to {@code java.lang.<Name>} instead of being
 * misrouted to an unrelated wildcard-imported package.</p>
 *
 * <p>The data is generated from {@code $JDK/lib/ct.sym} (the per-release API database
 * {@code --release} uses) and gated on each type's <em>finalization</em> release, not its preview
 * introduction. Preview-only and withdrawn types are excluded. Regenerate when bumping the supported
 * JDK range.</p>
 *
 * <p>Public so {@link com.etk2000.checkstyle.gradle.fix} fixers can share the same data as the check
 * that produces violations.</p>
 */
public final class JavaLangClasses {
	private record RemovedType(@Nonnull String name, int firstRelease, int lastRelease) {}

	private static final List<RemovedType> REMOVED = List.of(new RemovedType("Compiler", 8, 20));
	private static final Map<Integer, Set<String>> ADDED = Map.of(
			9, Set.of(
					"IllegalCallerException",
					"LayerInstantiationException",
					"Module",
					"ModuleLayer",
					"ProcessHandle",
					"StackWalker"
			),
			16, Set.of("Record"),
			19, Set.of("WrongThreadException"),
			21, Set.of("MatchException"),
			25, Set.of("IO", "ScopedValue")
	);
	private static final Map<Integer, Set<String>> CACHE = new ConcurrentHashMap<>();
	private static final Set<String> BASE = Set.of(
			"AbstractMethodError",
			"Appendable",
			"ArithmeticException",
			"ArrayIndexOutOfBoundsException",
			"ArrayStoreException",
			"AssertionError",
			"AutoCloseable",
			"Boolean",
			"BootstrapMethodError",
			"Byte",
			"CharSequence",
			"Character",
			"Class",
			"ClassCastException",
			"ClassCircularityError",
			"ClassFormatError",
			"ClassLoader",
			"ClassNotFoundException",
			"ClassValue",
			"CloneNotSupportedException",
			"Cloneable",
			"Comparable",
			"Deprecated",
			"Double",
			"Enum",
			"EnumConstantNotPresentException",
			"Error",
			"Exception",
			"ExceptionInInitializerError",
			"Float",
			"FunctionalInterface",
			"IllegalAccessError",
			"IllegalAccessException",
			"IllegalArgumentException",
			"IllegalMonitorStateException",
			"IllegalStateException",
			"IllegalThreadStateException",
			"IncompatibleClassChangeError",
			"IndexOutOfBoundsException",
			"InheritableThreadLocal",
			"InstantiationError",
			"InstantiationException",
			"Integer",
			"InternalError",
			"InterruptedException",
			"Iterable",
			"LinkageError",
			"Long",
			"Math",
			"NegativeArraySizeException",
			"NoClassDefFoundError",
			"NoSuchFieldError",
			"NoSuchFieldException",
			"NoSuchMethodError",
			"NoSuchMethodException",
			"NullPointerException",
			"Number",
			"NumberFormatException",
			"Object",
			"OutOfMemoryError",
			"Override",
			"Package",
			"Process",
			"ProcessBuilder",
			"Readable",
			"ReflectiveOperationException",
			"Runnable",
			"Runtime",
			"RuntimeException",
			"RuntimePermission",
			"SafeVarargs",
			"SecurityException",
			"SecurityManager",
			"Short",
			"StackOverflowError",
			"StackTraceElement",
			"StrictMath",
			"String",
			"StringBuffer",
			"StringBuilder",
			"StringIndexOutOfBoundsException",
			"SuppressWarnings",
			"System",
			"Thread",
			"ThreadDeath",
			"ThreadGroup",
			"ThreadLocal",
			"Throwable",
			"TypeNotPresentException",
			"UnknownError",
			"UnsatisfiedLinkError",
			"UnsupportedClassVersionError",
			"UnsupportedOperationException",
			"VerifyError",
			"VirtualMachineError",
			"Void"
	);

	@CheckReturnValue
	@Nonnull
	private static Set<String> computeForJavaTarget(int javaTarget) {
		final var result = new HashSet<>(BASE);
		for (var added : ADDED.entrySet()) {
			if (javaTarget >= added.getKey())
				result.addAll(added.getValue());
		}
		for (var removed : REMOVED) {
			if (removed.firstRelease() <= javaTarget && javaTarget <= removed.lastRelease())
				result.add(removed.name());
		}
		return Set.copyOf(result);
	}

	@CheckReturnValue
	@Nonnull
	public static Set<String> forJavaTarget(int javaTarget) {
		return CACHE.computeIfAbsent(javaTarget, JavaLangClasses::computeForJavaTarget);
	}

	private JavaLangClasses() {
	}
}