package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.tools.ToolProvider;

@Isolated
public class ReflectionUtilTest {
	public static class ConflictingVarargs {
		public int m(int... values) {
			return values.length;
		}

		public int m(String... values) {
			return values.length;
		}
	}

	public static class VarargsArity {
		public <T> T pick() {
			return null;
		}

		public <T> T pick(T seed, Object... rest) {
			return seed;
		}
	}

	public static class PackagePrivateMethodHolder {
		int packaged() {
			return 0;
		}
	}

	public static class PrivateMethodHolder {
		private int hidden(String value) {
			return value.length();
		}

		public int size(String value) {
			return hidden(value);
		}
	}

	public interface InheritedVarargs {
		default int join(String... parts) {
			return parts.length;
		}
	}

	public static class InheritedVarargsUser implements InheritedVarargs {}

	public static class ProtectedVarargsHolder {
		protected int gather(String first, Object... rest) {
			return rest.length;
		}
	}

	public static class ArrayNames<T> extends ArrayList<T[]> {}

	public static class BoundedTriple<N extends Number, C extends Comparable<C>, U extends N> {}

	public static class ConcreteNames extends ArrayList<String> {}

	public static class RawNames extends ArrayList {}

	public static class StringArrays extends ArrayList<String[]> {}

	public static class StringBag extends AbstractCollection<String> {
		@Override
		public Iterator<String> iterator() {
			return null;
		}

		@Override
		public int size() {
			return 0;
		}
	}

	public static class TwoLevelOuter {
		public static class TwoLevelMid {
			public static class TwoLevelLeaf {
				public String getValue() {
					return "";
				}
			}
		}
	}

	public static class OuterBox<T> {
		public class Item {
			public class Part {}

			public T value() {
				return null;
			}
		}

		public class NamedItem extends Item {}
	}

	public class PlainInner {}

	public static class StaticMethodHolder {
		private static String hidden(int value) {
			return String.valueOf(value);
		}

		static int packaged(int value) {
			return hidden(value).length();
		}
	}

	private static final char CONSTANT_UTF8 = 1;
	private static final Path LINKAGE_PROBE_SOURCES = Path.of("src", "test", "resources", "com", "etk2000", "checkstyle", "inputs", "reflectionutil");
	private static final String JAVAC_TYPE_PARAMETER_SIGNATURE = "<T:Ljava/lang/Object;>Ljava/lang/Object;";
	private static final String MALFORMED_SIGNATURE = "<T:Ljava/lang/Object;>X";
	private static boolean linkageProbesDefined;

	static Stream<Arguments> applyJavaNamingHeuristicProvider() {
		return Stream.of(
				Arguments.of("", ""),
				Arguments.of(".", "."),
				Arguments.of(".A", ".A"),
				Arguments.of("A.", "A."),
				Arguments.of("A..B", "A..B"),
				Arguments.of("Foo", "Foo"),
				Arguments.of("foo", "foo"),
				Arguments.of("Foo.", "Foo."),
				Arguments.of("foo.", "foo."),
				Arguments.of("a.B", "a.B"),
				Arguments.of("a.B.C", "a.B$C"),
				Arguments.of("A.B", "A$B"),
				Arguments.of("A.B.C", "A$B$C"),
				Arguments.of("com.foo.Bar", "com.foo.Bar"),
				Arguments.of("java.util.Map.Entry", "java.util.Map$Entry"),
				Arguments.of("java.util.AbstractMap.SimpleEntry", "java.util.AbstractMap$SimpleEntry"),
				Arguments.of("com.Foo.bar.Baz", "com.Foo.bar.Baz"),
				Arguments.of("pkg.Outer.lower.Inner", "pkg.Outer.lower.Inner"),
				Arguments.of("Foo.bar", "Foo.bar"),
				Arguments.of("Foo.bar.Baz", "Foo.bar.Baz"),
				Arguments.of("Outer.lower.Inner", "Outer.lower.Inner"),
				Arguments.of("Outer.lower.A.B.C", "Outer.lower.A.B.C"),
				Arguments.of("Outer.A.lower.Inner", "Outer.A.lower.Inner"),
				Arguments.of("Foo.0Bad", "Foo.0Bad"),
				Arguments.of("java.util._NoSuch", "java.util._NoSuch"),
				Arguments.of("java.util.$NoSuch", "java.util.$NoSuch"),
				Arguments.of("java.util.Map$Entry", "java.util.Map$Entry")
		);
	}

	static Stream<Arguments> collectionInterfaceFqcnTypes() {
		return Stream.of(
				Arguments.of("java.util.List", "java.util.List"),
				Arguments.of("java.util.Map", "java.util.Map"),
				Arguments.of("java.util.ArrayDeque", "java.util.Deque"),
				Arguments.of("java.util.ArrayList", "java.util.List"),
				Arguments.of("java.util.HashMap", "java.util.Map"),
				Arguments.of("java.util.HashSet", "java.util.Set"),
				Arguments.of("java.util.PriorityQueue", "java.util.Queue"),
				Arguments.of("java.util.TreeMap", "java.util.Map"),
				Arguments.of("java.lang.String", null),
				Arguments.of("com.nonexistent.FakeClass", null),
				Arguments.of("com.etk2000.checkstyle.ReflectionUtilTest$StringBag", "java.util.Collection")
		);
	}

	/**
	 * Compiles the probe classes into {@code dir} and defines the field-type holder and the outer
	 * class without the types they name, once per JVM: defining a class a second time fails.
	 */
	private static synchronized void defineLinkageProbes(@Nonnull Path dir) throws Exception {
		if (linkageProbesDefined)
			return;
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeFieldTypeHolder.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeOuter.java").toString()
		);
		assertEquals(0, compiled);
		final var lookup = MethodHandles.lookup();
		lookup.defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeFieldTypeHolder.class")));
		lookup.defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeOuter.class")));
		linkageProbesDefined = true;
	}

	static Stream<Arguments> findCollectionInterfaceConcreteTypes() {
		return Stream.of(
				Arguments.of("java.util.ArrayDeque", "Deque"),
				Arguments.of("java.util.ArrayList", "List"),
				Arguments.of("java.util.HashMap", "Map"),
				Arguments.of("java.util.HashSet", "Set"),
				Arguments.of("java.util.Hashtable", "Map"),
				Arguments.of("java.util.IdentityHashMap", "Map"),
				Arguments.of("java.util.LinkedHashMap", "Map"),
				Arguments.of("java.util.LinkedHashSet", "Set"),
				Arguments.of("java.util.PriorityQueue", "Queue"),
				Arguments.of("java.util.Stack", "List"),
				Arguments.of("java.util.TreeMap", "Map"),
				Arguments.of("java.util.TreeSet", "Set"),
				Arguments.of("java.util.Vector", "List"),
				Arguments.of("java.util.WeakHashMap", "Map"),
				Arguments.of("java.util.concurrent.ArrayBlockingQueue", "Queue"),
				Arguments.of("java.util.concurrent.ConcurrentHashMap", "Map"),
				Arguments.of("java.util.concurrent.ConcurrentLinkedDeque", "Deque"),
				Arguments.of("java.util.concurrent.ConcurrentLinkedQueue", "Queue"),
				Arguments.of("java.util.concurrent.ConcurrentSkipListMap", "Map"),
				Arguments.of("java.util.concurrent.ConcurrentSkipListSet", "Set"),
				Arguments.of("java.util.concurrent.CopyOnWriteArrayList", "List"),
				Arguments.of("java.util.concurrent.CopyOnWriteArraySet", "Set"),
				Arguments.of("java.util.concurrent.DelayQueue", "Queue"),
				Arguments.of("java.util.concurrent.LinkedBlockingDeque", "Deque"),
				Arguments.of("java.util.concurrent.LinkedBlockingQueue", "Queue"),
				Arguments.of("java.util.concurrent.LinkedTransferQueue", "Queue"),
				Arguments.of("java.util.concurrent.PriorityBlockingQueue", "Queue"),
				Arguments.of("java.util.concurrent.SynchronousQueue", "Queue"),
				Arguments.of("java.util.EnumMap", "Map"),
				Arguments.of("com.etk2000.checkstyle.ReflectionUtilTest$StringBag", "Collection")
		);
	}

	static Stream<Arguments> inheritedReturnTypeNamesCases() {
		return Stream.of(
				Arguments.of("java.lang.Thread", "getName", 0, List.of(), Set.of("java.lang.String")),
				Arguments.of("java.lang.Thread", "getName", 1, List.of(), Set.of()),
				Arguments.of("java.lang.Thread", "currentThread", 0, List.of(), Set.of("java.lang.Thread")),
				Arguments.of("java.lang.StringBuilder", "append", 1, List.of(), Set.of("java.lang.StringBuilder")),
				Arguments.of("java.lang.ThreadLocal", "get", 0, List.of("java.lang.String"), Set.of("java.lang.String")),
				Arguments.of("java.lang.ref.WeakReference", "get", 0, List.of("java.lang.String"), Set.of("java.lang.String")),
				Arguments.of("java.util.ArrayList", "get", 1, List.of(), Set.of("java.lang.Object")),
				Arguments.of("java.util.ArrayList", "get", 1, List.of("java.lang.String"), Set.of("java.lang.String")),
				Arguments.of("java.util.ArrayList", "get", 1, List.of("java.lang.Thread"), Set.of("java.lang.Thread")),
				Arguments.of("java.util.ArrayList", "get", 1, List.of(ReflectionUtil.TYPE_VARIABLE_KEY), Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of("java.util.ArrayList", "get", 1, List.of("java.lang.String", "java.lang.String"), Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of("java.util.ArrayList", "get", 1, null, Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of("java.util.ArrayList", "subList", 2, List.of("java.lang.String"), Set.of("java.util.List")),
				Arguments.of("java.util.ArrayList", "toArray", 1, List.of("java.lang.String"), Set.of("[Ljava.lang.Object;")),
				Arguments.of("java.util.HashMap", "get", 1, List.of("java.lang.Integer", "java.lang.String"), Set.of("java.lang.String")),
				Arguments.of("java.util.List", "of", 0, List.of(), Set.of()),
				Arguments.of("java.util.Properties", "get", 1, List.of(), Set.of("java.lang.Object")),
				Arguments.of("java.util.concurrent.AbstractExecutorService", "invokeAny", 1, List.of(), Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of(ArrayNames.class.getName(), "get", 1, List.of("java.lang.String"), Set.of("java.lang.String[]")),
				Arguments.of(ConcreteNames.class.getName(), "get", 1, List.of(), Set.of("java.lang.String")),
				Arguments.of(InheritedVarargsUser.class.getName(), "join", 2, List.of(), Set.of("int")),
				Arguments.of("com.etk2000.checkstyle.ReflectionUtilTest.OuterBox.NamedItem", "value", 0, List.of(), Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of(PackagePrivateMethodHolder.class.getName(), "packaged", 0, List.of(), Set.of()),
				Arguments.of(PrivateMethodHolder.class.getName(), "clone", 0, List.of(), Set.of("java.lang.Object")),
				Arguments.of(PrivateMethodHolder.class.getName(), "hidden", 1, List.of(), Set.of()),
				Arguments.of(PrivateMethodHolder.class.getName(), "size", 1, List.of(), Set.of("int")),
				Arguments.of(ProtectedVarargsHolder.class.getName(), "gather", 0, List.of(), Set.of()),
				Arguments.of(ProtectedVarargsHolder.class.getName(), "gather", 1, List.of(), Set.of("int")),
				Arguments.of(ProtectedVarargsHolder.class.getName(), "gather", 3, List.of(), Set.of("int")),
				Arguments.of(RawNames.class.getName(), "get", 1, List.of(), Set.of("java.lang.Object")),
				Arguments.of(VarargsArity.class.getName(), "pick", 0, List.of(), Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of("com.nonexistent.FakeClass", "text", 0, List.of(), Set.of())
		);
	}

	/**
	 * Recurses until the stack overflows, then on the way back up asks for {@code name} at every
	 * frame, each with a little more stack than the one below it, so some ask overflows inside the
	 * class lookup itself. {@code outcomes[0]} counts the overflows that reached here, and
	 * {@code outcomes[1]} the asks answered with nothing.
	 */
	private static void loadOnTheWayBack(@Nonnull String name, @Nonnull int[] outcomes) {
		try {
			loadOnTheWayBack(name, outcomes);
		}
		catch (StackOverflowError e) {
			++outcomes[0];
		}
		try {
			if (ReflectionUtil.binaryName(name) == null)
				++outcomes[1];
		}
		catch (StackOverflowError e) {
			++outcomes[0];
		}
	}

	static Stream<Arguments> staticReturnTypeNamesCases() {
		return Stream.of(
				Arguments.of("java.lang.Integer", "hashCode", 0, Set.of()),
				Arguments.of("java.lang.Integer", "hashCode", 1, Set.of("int")),
				Arguments.of("java.lang.String", "valueOf", 1, Set.of("java.lang.String")),
				Arguments.of("java.util.ArrayList", "of", 0, Set.of()),
				Arguments.of("java.util.GregorianCalendar", "getInstance", 0, Set.of("java.util.Calendar")),
				Arguments.of("java.util.List", "of", 0, Set.of("java.util.List")),
				Arguments.of("java.util.Objects", "requireNonNull", 1, Set.of(ReflectionUtil.TYPE_VARIABLE_KEY)),
				Arguments.of("java.util.Objects", "requireNonNull", 3, Set.of()),
				Arguments.of("java.util.Objects", "toString", 1, Set.of("java.lang.String")),
				Arguments.of(StaticMethodHolder.class.getName(), "hidden", 1, Set.of()),
				Arguments.of(StaticMethodHolder.class.getName(), "packaged", 1, Set.of("int")),
				Arguments.of("com.nonexistent.FakeClass", "text", 0, Set.of())
		);
	}

	/** A class file's {@code CONSTANT_Utf8_info} entry holding {@code text}, which is ASCII. */
	@CheckReturnValue
	@Nonnull
	private static String utf8Constant(@Nonnull String text) {
		return new String(new char[]{CONSTANT_UTF8, (char) (text.length() >> Byte.SIZE), (char) (text.length() & 0xFF)}) + text;
	}

	@Test
	public void testAcceptsValueOfType() {
		assertTrue(ReflectionUtil.acceptsValueOfType("java.util.List", "java.util.ArrayList"));
		assertTrue(ReflectionUtil.acceptsValueOfType("java.lang.Object", "java.util.List"));
		assertTrue(ReflectionUtil.acceptsValueOfType("java.util.List", "java.util.List"));
		assertFalse(ReflectionUtil.acceptsValueOfType("java.util.ArrayList", "java.util.List"));
		assertFalse(ReflectionUtil.acceptsValueOfType("java.util.List", "java.util.Set"));
	}

	@Test
	public void testAcceptsValueOfTypeUnknownClass() {
		assertFalse(ReflectionUtil.acceptsValueOfType("does.not.Exist", "java.util.List"));
		assertFalse(ReflectionUtil.acceptsValueOfType("java.util.List", "does.not.Exist"));
	}

	@MethodSource("applyJavaNamingHeuristicProvider")
	@ParameterizedTest
	public void testApplyJavaNamingHeuristic(@Nonnull String input, @Nonnull String expected) {
		assertEquals(expected, ReflectionUtil.applyJavaNamingHeuristic(input));
	}

	@Test
	public void testBinaryName() {
		assertEquals("java.util.List", ReflectionUtil.binaryName("java.util.List"));
		assertEquals("java.util.Map$Entry", ReflectionUtil.binaryName("java.util.Map.Entry"));
		assertEquals("java.util.Map$Entry", ReflectionUtil.binaryName("java.util.Map$Entry"));
	}

	@Test
	public void testBinaryNameUnknownClass() {
		assertNull(ReflectionUtil.binaryName("does.not.Exist"));
	}

	@Test
	public void testClearCacheDoesNotResetCounter() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "size"));
		final var afterCall = ReflectionUtil.classForNameCallCount.get();
		assertNotEquals(0, afterCall);
		ReflectionUtil.clearCache();
		assertEquals(afterCall, ReflectionUtil.classForNameCallCount.get());
	}

	@MethodSource("collectionInterfaceFqcnTypes")
	@ParameterizedTest
	public void testCollectionInterfaceFqcn(String fqcn, String expected) {
		assertEquals(expected, ReflectionUtil.collectionInterfaceFqcn(fqcn));
	}

	@Test
	public void testDeclaredTypeParameterCount() {
		assertEquals(1, ReflectionUtil.declaredTypeParameterCount("java.util.List"));
		assertEquals(2, ReflectionUtil.declaredTypeParameterCount("java.util.Map"));
		assertEquals(0, ReflectionUtil.declaredTypeParameterCount("java.lang.String"));
	}

	@Test
	public void testDeclaredTypeParameterCountUnknownClass() {
		assertEquals(-1, ReflectionUtil.declaredTypeParameterCount("does.not.Exist"));
	}

	@Test
	public void testDeclaresFieldNamed() {
		assertTrue(ReflectionUtil.declaresFieldNamed("java.io.FilterInputStream", "in"));
		assertTrue(ReflectionUtil.declaresFieldNamed("java.io.BufferedInputStream", "in"));
		assertTrue(ReflectionUtil.declaresFieldNamed("java.lang.Integer", "MAX_VALUE"));
		assertFalse(ReflectionUtil.declaresFieldNamed("java.lang.Integer", "value"));
		assertFalse(ReflectionUtil.declaresFieldNamed("java.io.FilterInputStream", "stream"));
	}

	@Test
	public void testDeclaresFieldNamedUnknownClass() {
		assertNull(ReflectionUtil.declaresFieldNamed("does.not.Exist", "in"));
	}

	@Test
	public void testDeclaresMethodErasureArityMismatch() {
		assertFalse(ReflectionUtil.declaresMethodErasure("java.util.AbstractMap", "putAll", List.of()));
	}

	@Test
	public void testDeclaresMethodErasureArrayParameterType() {
		assertTrue(ReflectionUtil.declaresMethodErasure("java.lang.String", "valueOf", List.of("char[]")));
	}

	@Test
	public void testDeclaresMethodErasureBridgeMethodExcluded() {
		assertFalse(ReflectionUtil.declaresMethodErasure("java.lang.String", "compareTo", List.of("java.lang.Object")));
		assertTrue(ReflectionUtil.declaresMethodErasure("java.lang.String", "compareTo", List.of("java.lang.String")));
	}

	@Test
	public void testDeclaresMethodErasureInheritedFromSupertype() {
		assertTrue(ReflectionUtil.declaresMethodErasure("java.util.EnumMap", "putAll", List.of("java.util.Map")));
	}

	@Test
	public void testDeclaresMethodErasureMatchesMultipleParameters() {
		assertTrue(ReflectionUtil.declaresMethodErasure(
				"java.lang.String", "regionMatches", List.of("int", "java.lang.String", "int", "int")
		));
		assertFalse(ReflectionUtil.declaresMethodErasure(
				"java.lang.String", "regionMatches", List.of("java.lang.String", "int", "int", "int")
		));
	}

	@Test
	public void testDeclaresMethodErasureMatchesParameterType() {
		assertTrue(ReflectionUtil.declaresMethodErasure("java.util.AbstractMap", "putAll", List.of("java.util.Map")));
	}

	@Test
	public void testDeclaresMethodErasurePrivateMethodExcluded() {
		final var fqcn = "com.etk2000.checkstyle.ReflectionUtilTest$PrivateMethodHolder";
		assertFalse(ReflectionUtil.declaresMethodErasure(fqcn, "hidden", List.of("java.lang.String")));
		assertTrue(ReflectionUtil.declaresMethodErasure(fqcn, "size", List.of("java.lang.String")));
	}

	@Test
	public void testDeclaresMethodErasureProtectedOnTheDeclaringClass() {
		assertTrue(ReflectionUtil.declaresMethodErasure("java.util.AbstractList", "removeRange", List.of("int", "int")));
	}

	@Test
	public void testDeclaresMethodErasureStaticInterfaceMethodExcluded() {
		assertFalse(ReflectionUtil.declaresMethodErasure("java.util.List", "of", List.of()));
		assertTrue(ReflectionUtil.declaresMethodErasure("java.util.List", "clear", List.of()));
	}

	@Test
	public void testDeclaresMethodErasureUnknownClass() {
		assertFalse(ReflectionUtil.declaresMethodErasure("com.nonexistent.FakeClass", "putAll", List.of("java.util.Map")));
	}

	@Test
	public void testDeclaresMethodErasureUnknownMethodName() {
		assertFalse(ReflectionUtil.declaresMethodErasure("java.util.AbstractMap", "storeAll", List.of("java.util.Map")));
	}

	@Test
	public void testDeclaresMethodErasureWrongParameterType() {
		assertFalse(ReflectionUtil.declaresMethodErasure("java.util.AbstractMap", "putAll", List.of("java.util.List")));
	}

	@Test
	public void testDeclaresMethodNamedAndExactMethodsReadAProtectedOverload() {
		assertTrue(ReflectionUtil.declaresMethodNamed("java.util.AbstractList", "removeRange"));
		final var methods = ReflectionUtil.exactMethods("java.util.AbstractList", "removeRange", 2, List.of("java.lang.String"), false, true);
		assertEquals(List.of(new ExactMethod(List.of("int", "int"), "void", false)), methods);
	}

	@Test
	public void testDeclaresMethodNamedAndExactMethodsWithAnUnreadableDeclaredMethod(@TempDir Path dir) throws Exception {
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeProtectedOverloadApi.java").toString()
		);
		assertEquals(0, compiled);

		final var api = MethodHandles.lookup().defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeProtectedOverloadApi.class")));
		assertFalse(ReflectionUtil.hasUnreadableMembers(api.getName()), "getMethods() must still read the probe");
		assertNull(ReflectionUtil.declaresMethodNamed(api.getName(), "pick"));
		assertNull(ReflectionUtil.exactMethods(api.getName(), "pick", 1, null, false, true));
		assertNull(ReflectionUtil.exactMethods(api.getName(), "pick", 1, null, false, false));
	}

	@Test
	public void testDeclaresOverloadAtChainCrossPackagePackagePrivateExcluded() {
		assertFalse(ReflectionUtil.declaresOverloadAt("java.io.IOException", "setCause", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt("java.lang.Exception", "setCause", 1));
	}

	@Test
	public void testDeclaresOverloadAtChainPrivateExcluded() {
		final var fqcn = "com.etk2000.checkstyle.ReflectionUtilTest$PrivateMethodHolder";
		assertFalse(ReflectionUtil.declaresOverloadAt(fqcn, "hidden", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt(fqcn, "size", 1));
	}

	@Test
	public void testDeclaresOverloadAtChainProtectedSupertypeMethod() {
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.Stack", "removeRange", 2));
		assertFalse(ReflectionUtil.declaresOverloadAt("java.util.Stack", "removeRange", 1));
	}

	@Test
	public void testDeclaresOverloadAtChainVarargs() {
		final var fqcn = "com.etk2000.checkstyle.ReflectionUtilTest$ProtectedVarargsHolder";
		assertTrue(ReflectionUtil.declaresOverloadAt(fqcn, "gather", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt(fqcn, "gather", 5));
		assertFalse(ReflectionUtil.declaresOverloadAt(fqcn, "gather", 0));
	}

	@Test
	public void testDeclaresOverloadAtExactArity() {
		assertTrue(ReflectionUtil.declaresOverloadAt("java.lang.String", "substring", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt("java.lang.String", "substring", 2));
	}

	@Test
	public void testDeclaresOverloadAtInheritedInterfaceDefault() {
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.ArrayList", "stream", 0));
	}

	@Test
	public void testDeclaresOverloadAtInheritedInterfaceVarargs() {
		final var fqcn = "com.etk2000.checkstyle.ReflectionUtilTest$InheritedVarargsUser";
		assertTrue(ReflectionUtil.declaresOverloadAt(fqcn, "join", 0));
		assertTrue(ReflectionUtil.declaresOverloadAt(fqcn, "join", 3));
	}

	@Test
	public void testDeclaresOverloadAtNoSuchArity() {
		assertFalse(ReflectionUtil.declaresOverloadAt("java.lang.String", "substring", 0));
		assertFalse(ReflectionUtil.declaresOverloadAt("java.lang.String", "substring", 3));
	}

	@Test
	public void testDeclaresOverloadAtStaticInterfaceMethodExcluded() {
		assertFalse(ReflectionUtil.declaresOverloadAt("java.util.Comparator", "comparing", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.Comparator", "compare", 2));
	}

	@Test
	public void testDeclaresOverloadAtUnknownClass() {
		assertFalse(ReflectionUtil.declaresOverloadAt("com.nonexistent.FakeClass", "size", 0));
	}

	@Test
	public void testDeclaresOverloadAtUnknownMethodName() {
		assertFalse(ReflectionUtil.declaresOverloadAt("java.lang.String", "storeAll", 1));
	}

	@Test
	public void testDeclaresOverloadAtVarargs() {
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.Arrays", "asList", 0));
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.Arrays", "asList", 1));
		assertTrue(ReflectionUtil.declaresOverloadAt("java.util.Arrays", "asList", 12));
	}

	@Test
	public void testFieldTypeName() {
		assertEquals("java.io.PrintStream", ReflectionUtil.fieldTypeName("java.lang.System", "out"));
		assertEquals("int", ReflectionUtil.fieldTypeName("java.lang.Integer", "MAX_VALUE"));
	}

	@Test
	public void testFieldTypeNameNoSuchField() {
		assertNull(ReflectionUtil.fieldTypeName("java.lang.System", "notAField"));
	}

	@Test
	public void testFieldTypeNameUnknownClass() {
		assertNull(ReflectionUtil.fieldTypeName("does.not.Exist", "out"));
	}

	@Test
	public void testFindCharsetParameterConstructorOwnerIsItsClass() {
		assertEquals(new CharsetParameter(1, "java.util.Scanner"), ReflectionUtil.findCharsetParameter("java.util.Scanner", "new", 2));
	}

	@Test
	public void testFindCharsetParameterConstructors() {
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.lang.String", "new", 2).index());
		assertEquals(3, ReflectionUtil.findCharsetParameter("java.lang.String", "new", 4).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.io.InputStreamReader", "new", 2).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.io.OutputStreamWriter", "new", 2).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.io.PrintStream", "new", 2).index());
		assertEquals(2, ReflectionUtil.findCharsetParameter("java.io.PrintStream", "new", 3).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.io.PrintWriter", "new", 2).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.util.Scanner", "new", 2).index());
	}

	@Test
	public void testFindCharsetParameterInheritedOverloadOwnerIsTheDeclaringSuperclass() {
		assertEquals(
				new CharsetParameter(2, "java.util.Properties"),
				ReflectionUtil.findCharsetParameter("java.security.Provider", "storeToXML", 3)
		);
	}

	@Test
	public void testFindCharsetParameterMethods() {
		assertEquals(0, ReflectionUtil.findCharsetParameter("java.lang.String", "getBytes", 1).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.net.URLEncoder", "encode", 2).index());
		assertEquals(1, ReflectionUtil.findCharsetParameter("java.net.URLDecoder", "decode", 2).index());
		assertEquals(0, ReflectionUtil.findCharsetParameter("java.io.ByteArrayOutputStream", "toString", 1).index());
	}

	@Test
	public void testFindCharsetParameterNoCharsetOverload() {
		assertNull(ReflectionUtil.findCharsetParameter("java.nio.charset.Charset", "forName", 1));
		assertNull(ReflectionUtil.findCharsetParameter("java.lang.String", "valueOf", 1));
	}

	@Test
	public void testFindCharsetParameterNoConstructorWithThatCount() {
		assertNull(ReflectionUtil.findCharsetParameter("java.io.PrintWriter", "new", 3));
	}

	@Test
	public void testFindCharsetParameterUnknownClass() {
		assertNull(ReflectionUtil.findCharsetParameter("com.nonexistent.FakeClass", "method", 1));
	}

	@Test
	public void testFindCharsetParameterWrongArgCount() {
		assertNull(ReflectionUtil.findCharsetParameter("java.lang.String", "getBytes", 3));
		assertNull(ReflectionUtil.findCharsetParameter("java.lang.String", "new", 5));
	}

	@MethodSource("findCollectionInterfaceConcreteTypes")
	@ParameterizedTest
	public void testFindCollectionInterfaceConcrete(String fqcn, String expected) {
		assertEquals(expected, ReflectionUtil.findCollectionInterface(fqcn));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"com.nonexistent.FakeClass",
			"java.lang.Integer",
			"java.lang.String",
			"java.util.AbstractList",
			"java.util.EnumSet",
			"java.util.LinkedList",
			"java.util.AbstractMap",
			"java.util.AbstractSet",
			"java.util.List",
			"java.util.Map",
			"java.util.Set"
	})
	public void testFindCollectionInterfaceNull(String fqcn) {
		assertNull(ReflectionUtil.findCollectionInterface(fqcn));
	}

	@Test
	public void testGetMethodReturnTypeName() {
		assertEquals("java.util.List", ReflectionUtil.getMethodReturnTypeName("java.util.Collections", "unmodifiableList", 1));
		assertEquals("java.util.Map", ReflectionUtil.getMethodReturnTypeName("java.util.Collections", "unmodifiableMap", 1));
		assertEquals("int", ReflectionUtil.getMethodReturnTypeName("java.util.List", "size", 0));
	}

	@Test
	public void testGetMethodReturnTypeNameArityDistinguishesOverloads() {
		assertEquals(
				"java.util.concurrent.ConcurrentHashMap$KeySetView",
				ReflectionUtil.getMethodReturnTypeName("java.util.concurrent.ConcurrentHashMap", "keySet", 0)
		);
		assertEquals(
				"java.util.concurrent.ConcurrentHashMap$KeySetView",
				ReflectionUtil.getMethodReturnTypeName("java.util.concurrent.ConcurrentHashMap", "keySet", 1)
		);
	}

	@Test
	public void testGetMethodReturnTypeNameArrayReturn() {
		assertEquals("[C", ReflectionUtil.getMethodReturnTypeName("java.lang.String", "toCharArray", 0));
		assertEquals("[Ljava.lang.String;", ReflectionUtil.getMethodReturnTypeName("java.lang.String", "split", 1));
	}

	@Test
	public void testGetMethodReturnTypeNameSkipsBridgeMethod() {
		assertEquals(
				"java.lang.StringBuilder",
				ReflectionUtil.getMethodReturnTypeName("java.lang.StringBuilder", "append", 1)
		);
	}

	@Test
	public void testGetMethodReturnTypeNameUnknownClass() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("com.nonexistent.FakeClass", "method", 0));
	}

	@Test
	public void testGetMethodReturnTypeNameUnknownMethod() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("java.lang.String", "nonexistentMethod", 0));
	}

	@Test
	public void testGetVarArgsComponentTypeConflictingVarargsReturnsNull() {
		assertNull(ReflectionUtil.getVarArgsComponentType("com.etk2000.checkstyle.ReflectionUtilTest$ConflictingVarargs", "m", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeConstructor() {
		assertEquals(String.class, ReflectionUtil.getVarArgsComponentType("java.lang.ProcessBuilder", "new", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeNegativeArgCount() {
		assertNull(ReflectionUtil.getVarArgsComponentType("java.util.Arrays", "asList", -1));
	}

	@Test
	public void testGetVarArgsComponentTypeNonVarargsArrayOverloadBlocks() {
		assertNull(ReflectionUtil.getVarArgsComponentType("java.util.Arrays", "sort", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeNonVarargsOverloadBlocks() {
		assertNull(ReflectionUtil.getVarArgsComponentType("java.util.List", "of", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeNonVarargsOverloadNonArrayDoesNotBlock() {
		assertEquals(CharSequence.class, ReflectionUtil.getVarArgsComponentType("java.lang.String", "join", 2));
	}

	@Test
	public void testGetVarArgsComponentTypeUnknownClass() {
		assertNull(ReflectionUtil.getVarArgsComponentType("com.nonexistent.FakeClass", "method", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeVarargs() {
		assertEquals(Object.class, ReflectionUtil.getVarArgsComponentType("java.util.Arrays", "asList", 1));
	}

	@Test
	public void testGetVarArgsComponentTypeWrongArgCount() {
		assertNull(ReflectionUtil.getVarArgsComponentType("java.util.Arrays", "asList", 5));
	}

	@Test
	public void testGetVarArgsComponentTypeZeroArgCount() {
		assertNull(ReflectionUtil.getVarArgsComponentType("java.util.Arrays", "asList", 0));
	}

	@Test
	public void testHasGenericReturnTypeAritySelectsOverload() {
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.List", "of", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "of", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "of", 2));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Map", "of", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Map", "of", 2));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Set", "of", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Set", "of", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "of", 11));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Arrays", "asList", 2));
	}

	@Test
	public void testHasGenericReturnTypeClassLevelTypeParam() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "get", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "iterator", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Map", "get", 1));
	}

	@Test
	public void testHasGenericReturnTypeInferableFromArgs() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Collections", "min", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.Class", "cast", 1));
	}

	@Test
	public void testHasGenericReturnTypeNeedsTargetType() {
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Collections", "emptyList", 0));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Collections", "emptySet", 0));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Optional", "empty", 0));
	}

	@Test
	public void testHasGenericReturnTypeNonGenericMethod() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "valueOf", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "length", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "size", 0));
	}

	@Test
	public void testHasGenericReturnTypeUnknownArity() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Collections", "emptyList", 3));
	}

	@Test
	public void testHasGenericReturnTypeUnknownClass() {
		assertFalse(ReflectionUtil.hasGenericReturnType("com.nonexistent.FakeClass", "method", 0));
	}

	@Test
	public void testHasGenericReturnTypeUnknownMethod() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "nonexistentMethod", 0));
	}

	@Test
	public void testHasGenericReturnTypeVarargsArityBounds() {
		final var fqcn = "com.etk2000.checkstyle.ReflectionUtilTest$VarargsArity";
		assertTrue(ReflectionUtil.hasGenericReturnType(fqcn, "pick", 0));
		assertFalse(ReflectionUtil.hasGenericReturnType(fqcn, "pick", 1));
		assertFalse(ReflectionUtil.hasGenericReturnType(fqcn, "pick", 5));
	}

	@Test
	public void testHasMethodCachedSuccess() {
		for (var i = 0; i < 5; ++i)
			assertTrue(ReflectionUtil.hasMethod("java.util.ArrayList", "add"));
	}

	@Test
	public void testHasMethodCountInnerClassResolvesInOneCall() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertTrue(ReflectionUtil.hasMethod("java.util.Map.Entry", "getKey"));
		assertEquals(1, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodCountMitigationFallsBackToSlowPath() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertFalse(ReflectionUtil.hasMethod("pkg.Outer.lower.Inner", "x"));
		assertEquals(2, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodCountTopLevelClassResolvesInOneCall() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "size"));
		assertEquals(1, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodCountTwoLevelInnerResolvesInOneCall() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertTrue(ReflectionUtil.hasMethod(
				"com.etk2000.checkstyle.ReflectionUtilTest.TwoLevelOuter.TwoLevelMid.TwoLevelLeaf",
				"getValue"
		));
		assertEquals(1, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodCountUnresolvableCachedSecondCallZero() {
		ReflectionUtil.clearCache();
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		ReflectionUtil.classForNameCallCount.set(0);
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		assertEquals(0, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodCountUnresolvableTopLevelOneCall() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		assertEquals(1, ReflectionUtil.classForNameCallCount.get());
	}

	@Test
	public void testHasMethodDepthCapExhausted() {
		assertFalse(ReflectionUtil.hasMethod("A.B.C.D.E.F.G.H.I.J.K.L", "x"));
	}

	@Test
	public void testHasMethodDigitSegmentNotSubstituted() {
		assertFalse(ReflectionUtil.hasMethod("java.util.0NoSuch", "x"));
	}

	@Test
	public void testHasMethodDollarSegmentNotSubstituted() {
		assertFalse(ReflectionUtil.hasMethod("java.util.$NoSuch", "x"));
	}

	@Test
	public void testHasMethodExists() {
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "getFirst"));
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "getLast"));
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "get"));
	}

	@Test
	public void testHasMethodInnerClassDollarForm() {
		assertTrue(ReflectionUtil.hasMethod("java.util.Map$Entry", "getKey"));
	}

	@Test
	public void testHasMethodInnerClassDottedName() {
		assertTrue(ReflectionUtil.hasMethod("java.util.AbstractMap.SimpleEntry", "getValue"));
		assertTrue(ReflectionUtil.hasMethod("java.util.AbstractMap.SimpleEntry", "getKey"));
		assertTrue(ReflectionUtil.hasMethod("java.util.Map.Entry", "getKey"));
	}

	@Test
	public void testHasMethodInnerClassNonexistent() {
		assertFalse(ReflectionUtil.hasMethod("java.util.HashMap.NoSuchInner", "anything"));
	}

	@Test
	public void testHasMethodLowercaseSegmentNoSubstitution() {
		assertFalse(ReflectionUtil.hasMethod("java.util.nonexistent", "x"));
	}

	@Test
	public void testHasMethodMissing() {
		assertFalse(ReflectionUtil.hasMethod("java.util.Map", "getFirst"));
		assertFalse(ReflectionUtil.hasMethod("java.util.Map", "getLast"));
	}

	@Test
	public void testHasMethodMixedCaseLowercaseInMiddle() {
		assertFalse(ReflectionUtil.hasMethod("pkg.Outer.lower.Inner", "x"));
	}

	@Test
	public void testHasMethodNoDotsBareName() {
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
	}

	@Test
	public void testHasMethodTrailingDot() {
		assertFalse(ReflectionUtil.hasMethod("java.util.", "x"));
	}

	@Test
	public void testHasMethodTwoLevelInnerClass() {
		assertTrue(ReflectionUtil.hasMethod(
				"com.etk2000.checkstyle.ReflectionUtilTest.TwoLevelOuter.TwoLevelMid.TwoLevelLeaf",
				"getValue"
		));
	}

	@Test
	public void testHasMethodUnderscoreSegmentNotSubstituted() {
		assertFalse(ReflectionUtil.hasMethod("java.util._NoSuch", "x"));
	}

	@Test
	public void testHasMethodUnknownClass() {
		assertFalse(ReflectionUtil.hasMethod("com.nonexistent.FakeClass", "method"));
	}

	@Test
	public void testHasMethodUnknownClassMultiDot() {
		assertFalse(ReflectionUtil.hasMethod("java.util.NoSuch.AlsoNoSuch.AlsoMissing", "x"));
	}

	@Test
	public void testHasMethodUnknownClassRepeatedCallsCached() {
		for (var i = 0; i < 5; ++i)
			assertFalse(ReflectionUtil.hasMethod("com.nonexistent.RepeatedFake", "method"));
	}

	@Test
	public void testHasUnreadableMembers() {
		assertFalse(ReflectionUtil.hasUnreadableMembers("java.util.List"));
		assertFalse(ReflectionUtil.hasUnreadableMembers("java.lang.String"));
	}

	@Test
	public void testHasUnreadableMembersUnknownClass() {
		assertFalse(ReflectionUtil.hasUnreadableMembers("does.not.Exist"));
	}

	/**
	 * Member types pass down through superclasses and interfaces, one reached along two paths
	 * counting once, when the subclass's package may access them: public ones, non-private ones of
	 * its own package, and protected ones by inheritance but not by static import. A declaration of
	 * the name, accessible or not, hides its class's supertypes' ones. A static import brings in only
	 * the static ones. An empty package is the default package.
	 */
	@CsvSource({
			"java.util.concurrent.FutureTask, State, false, , java.util.concurrent.Future$State",
			"java.lang.Thread, State, false, , java.lang.Thread$State",
			"java.util.AbstractMap, Entry, false, , java.util.Map$Entry",
			"java.util.HashMap, SimpleEntry, false, , java.util.AbstractMap$SimpleEntry",
			"java.util.ArrayList, State, false, , ",
			"java.util.ArrayList, Itr, false, , ",
			"java.util.HashMap, Node, false, , ",
			"java.lang.Thread, State, true, , java.lang.Thread$State",
			"java.util.AbstractMap, Entry, true, , java.util.Map$Entry",
			"java.util.concurrent.RunnableScheduledFuture, State, false, , java.util.concurrent.Future$State",
			"java.util.HashMap, Entry, false, , java.util.Map$Entry",
			"java.awt.Component, AccessibleAWTComponent, false, , java.awt.Component$AccessibleAWTComponent",
			"java.awt.Component, AccessibleAWTComponent, true, , ",
			"java.awt.geom.Point2D, Double, true, , java.awt.geom.Point2D$Double",
			"com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture, State, false, , com.etk2000.checkstyle.testhelpers.ClasspathHidingFuture$State",
			"com.etk2000.checkstyle.testhelpers.ClasspathInnerDoublePoint, Double, false, , com.etk2000.checkstyle.testhelpers.ClasspathInnerDoublePoint$Double",
			"com.etk2000.checkstyle.testhelpers.ClasspathInnerDoublePoint, Double, true, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Integer, true, , com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Integer",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Deprecated, true, , com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Deprecated",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Short, true, com.etk2000.checkstyle.testhelpers, com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Short",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Byte, true, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Character, true, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Double, true, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Double, false, , com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Double",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Short, true, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Short, true, com.etk2000.checkstyle.inputs.preferimport, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Short, false, com.etk2000.checkstyle.inputs.preferimport, com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Short",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Byte, true, com.etk2000.checkstyle.testhelpers, com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Byte",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Byte, false, com.etk2000.checkstyle.testhelpers, com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Byte",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Byte, false, com.etk2000.checkstyle.inputs.preferimport, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Character, false, com.etk2000.checkstyle.testhelpers, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Character, true, com.etk2000.checkstyle.testhelpers, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes, Integer, true, com.etk2000.checkstyle.inputs.preferimport, com.etk2000.checkstyle.testhelpers.ClasspathMemberTypes$Integer",
			"java.util.HashMap, Node, false, java.util, java.util.HashMap$Node",
			"java.util.HashMap, KeySet, false, java.util, java.util.HashMap$KeySet",
			"java.util.HashMap, KeySet, true, java.util, ",
			"java.util.LinkedHashMap, Entry, false, , ",
			"java.util.LinkedHashMap, Entry, false, java.util, java.util.LinkedHashMap$Entry",
			"java.util.LinkedHashMap, Entry, true, java.util, java.util.LinkedHashMap$Entry",
			"java.util.WeakHashMap, Entry, false, java.util, ",
			"java.util.Properties, Entry, false, , ",
			"com.etk2000.checkstyle.testhelpers.ClasspathLinkedEntryMap, Entry, false, com.etk2000.checkstyle.testhelpers, java.util.Map$Entry"
	})
	@ParameterizedTest
	public void testInheritedMemberClass(
			@Nonnull String fqcn,
			@Nonnull String simpleName,
			boolean staticOnly,
			@Nullable String fromPackage,
			@Nullable String expected
	) {
		assertEquals(expected, ReflectionUtil.inheritedMemberClass(fqcn, simpleName, staticOnly, fromPackage));
	}

	/**
	 * Two different member types of the name pass down, at one depth or at two, or along two paths
	 * one of which a member type the package may access hides.
	 */
	@CsvSource({
			"com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture, State, false, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathThreadFuture, State, true, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathThreadRunnableFuture, State, false, ",
			"com.etk2000.checkstyle.testhelpers.ClasspathLinkedEntryMap, Entry, false, java.util"
	})
	@ParameterizedTest
	public void testInheritedMemberClassAmbiguous(@Nonnull String fqcn, @Nonnull String simpleName, boolean staticOnly, @Nullable String fromPackage) {
		assertEquals(ReflectionUtil.AMBIGUOUS_TYPE_KEY, ReflectionUtil.inheritedMemberClass(fqcn, simpleName, staticOnly, fromPackage));
	}

	@Test
	public void testInheritedMemberClassUnloadableIsUnknown() {
		assertEquals(ReflectionUtil.UNKNOWN_TYPE_KEY, ReflectionUtil.inheritedMemberClass("com.unknown.Missing", "State", false, null));
	}

	@Test
	public void testInheritedMemberClassWithAnUnreadableMemberType(@TempDir Path dir) throws Exception {
		defineLinkageProbes(dir);
		assertEquals(ReflectionUtil.UNKNOWN_TYPE_KEY, ReflectionUtil.inheritedMemberClass("com.etk2000.checkstyle.InputLinkageProbeOuter", "Anything", false, null));
	}

	@MethodSource("inheritedReturnTypeNamesCases")
	@ParameterizedTest
	public void testInheritedReturnTypeNames(
			@Nonnull String fqcn,
			@Nonnull String methodName,
			int argCount,
			@Nullable List<String> typeArguments,
			@Nonnull Set<String> expected
	) {
		assertEquals(expected, Set.copyOf(ReflectionUtil.inheritedReturnTypeNames(fqcn, methodName, argCount, typeArguments)));
	}

	@Test
	public void testInheritedReturnTypeNamesWithAbsentTypeArgument(@TempDir Path dir) throws Exception {
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeGenericReturnApi.java").toString()
		);
		assertEquals(0, compiled);

		final var api = MethodHandles.lookup().defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeGenericReturnApi.class")));
		assertEquals(List.of("java.util.List"), ReflectionUtil.inheritedReturnTypeNames(api.getName(), "items", 0, List.of()));
	}

	@Test
	public void testInheritedReturnTypeNamesWithAbsentTypeInTheSupertype(@TempDir Path dir) throws Exception {
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeMissing.java").toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeTypeVariableView.java").toString()
		);
		assertEquals(0, compiled);

		final var view = MethodHandles.lookup().defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeTypeVariableView.class")));
		assertEquals(List.of(ReflectionUtil.TYPE_VARIABLE_KEY), ReflectionUtil.inheritedReturnTypeNames(view.getName(), "get", 0, List.of()));
	}

	@Test
	public void testInheritedReturnTypeNamesWithMalformedSignature(@TempDir Path dir) throws Exception {
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputMalformedSignatureApi.java").toString()
		);
		assertEquals(0, compiled);

		final var text = new String(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputMalformedSignatureApi.class")), StandardCharsets.ISO_8859_1);
		final var written = utf8Constant(JAVAC_TYPE_PARAMETER_SIGNATURE);
		assertTrue(text.contains(written), "javac no longer writes the signature this rewrites");

		final var api = MethodHandles.lookup().defineClass(text.replace(written, utf8Constant(MALFORMED_SIGNATURE)).getBytes(StandardCharsets.ISO_8859_1));
		assertEquals(List.of("java.lang.Object"), ReflectionUtil.inheritedReturnTypeNames(api.getName(), "value", 0, List.of("java.lang.String")));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"java.lang.String",
			"java.lang.Object",
			"java.io.File",
			"java.util.ArrayList",
			"com.nonexistent.FakeClass"
	})
	public void testIsCharSequenceNotStringFalse(@Nonnull String fqcn) {
		assertFalse(ReflectionUtil.isCharSequenceNotString(fqcn));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"java.lang.CharSequence",
			"java.lang.StringBuffer",
			"java.lang.StringBuilder",
			"java.nio.CharBuffer",
			"javax.lang.model.element.Name",
			"javax.swing.text.Segment"
	})
	public void testIsCharSequenceNotStringTrue(@Nonnull String fqcn) {
		assertTrue(ReflectionUtil.isCharSequenceNotString(fqcn));
	}

	@Test
	public void testIsFunctionalInterfaceFalseMultipleMethods() {
		assertFalse(ReflectionUtil.isFunctionalInterface("java.util.List"));
	}

	@Test
	public void testIsFunctionalInterfaceFalseNoAbstractMethods() {
		assertFalse(ReflectionUtil.isFunctionalInterface("java.io.Serializable"));
	}

	@Test
	public void testIsFunctionalInterfaceFalseNotInterface() {
		assertFalse(ReflectionUtil.isFunctionalInterface("java.lang.String"));
	}

	@Test
	public void testIsFunctionalInterfaceFalseUnknownClass() {
		assertFalse(ReflectionUtil.isFunctionalInterface("com.nonexistent.FakeClass"));
	}

	@Test
	public void testIsFunctionalInterfaceTrue() {
		assertTrue(ReflectionUtil.isFunctionalInterface("java.lang.Runnable"));
		assertTrue(ReflectionUtil.isFunctionalInterface("java.util.Comparator"));
		assertTrue(ReflectionUtil.isFunctionalInterface("java.util.function.Function"));
		assertTrue(ReflectionUtil.isFunctionalInterface("java.util.function.Supplier"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"com.etk2000.checkstyle.ReflectionUtilTest$PlainInner",
			"com.etk2000.checkstyle.ReflectionUtilTest$TwoLevelOuter$TwoLevelMid$TwoLevelLeaf",
			"java.util.HashMap$Node",
			"java.util.Map$Entry",
			"java.lang.String",
			"com.nonexistent.FakeClass"
	})
	public void testIsInnerClassOfGenericFalse(@Nonnull String fqcn) {
		assertFalse(ReflectionUtil.isInnerClassOfGeneric(fqcn));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"com.etk2000.checkstyle.ReflectionUtilTest$OuterBox$Item",
			"com.etk2000.checkstyle.ReflectionUtilTest$OuterBox$Item$Part",
			"java.util.ArrayList$Itr"
	})
	public void testIsInnerClassOfGenericTrue(@Nonnull String fqcn) {
		assertTrue(ReflectionUtil.isInnerClassOfGeneric(fqcn));
	}

	@Test
	public void testIsInnerClassOfGenericWithAnAbsentEnclosingClass(@TempDir Path dir) throws Exception {
		final var compiled = ToolProvider.getSystemJavaCompiler().run(
				null,
				null,
				null,
				"-d",
				dir.toString(),
				LINKAGE_PROBE_SOURCES.resolve("InputLinkageProbeGenericOuter.java").toString()
		);
		assertEquals(0, compiled);

		final var inner = MethodHandles.lookup().defineClass(Files.readAllBytes(dir.resolve("com/etk2000/checkstyle/InputLinkageProbeGenericOuter$Inner.class")));
		assertTrue(ReflectionUtil.isInnerClassOfGeneric(inner.getName()));
	}

	@Test
	public void testIsResolvableClassResolvable() {
		assertTrue(ReflectionUtil.isResolvableClass("java.lang.String"));
		assertTrue(ReflectionUtil.isResolvableClass("java.util.Map"));
	}

	@Test
	public void testIsResolvableClassUnresolvable() {
		assertFalse(ReflectionUtil.isResolvableClass("com.etk2000.checkstyle.NoSuchClass"));
		assertFalse(ReflectionUtil.isResolvableClass("totally.bogus.Type"));
	}

	/**
	 * The sibling class warms the class-loading path up at full stack, so the dive only cuts the
	 * lookup itself short.
	 */
	@Test
	public void testLoadClassOverflowLeavesNoCacheEntry() {
		ReflectionUtil.clearCache();
		assertEquals("javax.swing.text.html.parser.Element", ReflectionUtil.binaryName("javax.swing.text.html.parser.Element"));

		final var name = "javax.swing.text.html.parser.DTD";
		final var outcomes = new int[2];
		loadOnTheWayBack(name, outcomes);
		assertNotEquals(0, outcomes[0], "the dive must overflow the stack");
		assertEquals(name, ReflectionUtil.binaryName(name));
	}

	@Test
	public void testParameterAcceptsTypeCandidateRejects() {
		assertTrue(ReflectionUtil.parameterAcceptsType(
				"java.io.PrintStream", "println", 1, 0, "java.util.ArrayList", "java.util.List"
		));
	}

	@Test
	public void testParameterAcceptsTypeConstructor() {
		assertTrue(ReflectionUtil.parameterAcceptsType(
				"java.util.ArrayList", "new", 1, 0, "java.util.ArrayList", "java.util.List"
		));
	}

	@Test
	public void testParameterAcceptsTypeIndexPastArity() {
		assertFalse(ReflectionUtil.parameterAcceptsType(
				"java.io.PrintStream", "println", 1, 3, "java.util.ArrayList", "java.util.List"
		));
	}

	@Test
	public void testParameterAcceptsTypeNoCandidate() {
		assertFalse(ReflectionUtil.parameterAcceptsType(
				"java.io.PrintStream", "notAMethod", 1, 0, "java.util.ArrayList", "java.util.List"
		));
	}

	@Test
	public void testParameterAcceptsTypeUnknownClass() {
		assertFalse(ReflectionUtil.parameterAcceptsType(
				"does.not.Exist", "println", 1, 0, "java.util.ArrayList", "java.util.List"
		));
		assertFalse(ReflectionUtil.parameterAcceptsType(
				"java.io.PrintStream", "println", 1, 0, "does.not.Exist", "java.util.List"
		));
	}

	@Test
	public void testParameterAcceptsTypeWidenedRejected() {
		assertFalse(ReflectionUtil.parameterAcceptsType(
				"java.util.Collections", "unmodifiableSortedSet", 1, 0, "java.util.TreeSet", "java.util.Set"
		));
	}

	@Test
	public void testResolveClassNameAlreadyQualified() {
		assertEquals(
				"java.util.List",
				ReflectionUtil.resolveClassName("java.util.List", null, Set.of())
		);
	}

	@Test
	public void testResolveClassNameEmpty() {
		assertNull(ReflectionUtil.resolveClassName("", null, Set.of()));
	}

	@Test
	public void testResolveClassNameExplicitImport() {
		assertEquals(
				"java.util.Collections",
				ReflectionUtil.resolveClassName("Collections", null, Set.of("java.util.Collections"))
		);
	}

	@Test
	public void testResolveClassNameJavaLangFallback() {
		assertEquals("java.lang.String", ReflectionUtil.resolveClassName("String", null, Set.of()));
		assertEquals("java.lang.Integer", ReflectionUtil.resolveClassName("Integer", null, Set.of()));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"String[]",
			"String[][]",
			"int[]",
			"int[][]",
			"java.lang.String[]",
			"java.util.List[][]"
	})
	public void testResolveClassNameRejectsArrayTypes(@Nonnull String typeName) {
		assertNull(ReflectionUtil.resolveClassName(typeName, null, Set.of()));
	}

	@Test
	public void testResolveClassNameSamePackage() {
		assertEquals(
				"java.util.List",
				ReflectionUtil.resolveClassName("List", "java.util", Set.of())
		);
	}

	@Test
	public void testResolveClassNameUnresolvable() {
		assertNull(ReflectionUtil.resolveClassName("NonexistentClass", null, Set.of()));
	}

	@Test
	public void testResolveClassNameWildcardImport() {
		assertEquals(
				"java.util.Collections",
				ReflectionUtil.resolveClassName("Collections", null, Set.of("java.util.*"))
		);
	}

	@Test
	public void testResolveClassNameWildcardImportMissesFallsThroughToJavaLang() {
		assertEquals(
				"java.lang.String",
				ReflectionUtil.resolveClassName("String", null, Set.of("com.bogus.*"))
		);
	}

	@Test
	public void testReturnsTheSameTypeAbsentOnTheInterface() {
		assertFalse(ReflectionUtil.returnsTheSameType(
				"java.util.Vector", "java.util.List", "indexOf", 2
		));
	}

	@Test
	public void testReturnsTheSameTypeCovariantReturnDiffers() {
		assertFalse(ReflectionUtil.returnsTheSameType(
				"java.util.concurrent.ConcurrentHashMap", "java.util.Map", "keySet", 0
		));
	}

	@Test
	public void testReturnsTheSameTypeMatchesPerOverload() {
		assertTrue(ReflectionUtil.returnsTheSameType(
				"java.util.ArrayList", "java.util.List", "remove", 1
		));
	}

	@Test
	public void testReturnsTheSameTypeNoSuchMethod() {
		assertFalse(ReflectionUtil.returnsTheSameType(
				"java.util.ArrayList", "java.util.List", "ensureCapacity", 1
		));
	}

	@Test
	public void testReturnsTheSameTypeSimpleMatch() {
		assertTrue(ReflectionUtil.returnsTheSameType("java.util.ArrayList", "java.util.List", "add", 1));
		assertTrue(ReflectionUtil.returnsTheSameType("java.util.ArrayList", "java.util.List", "size", 0));
	}

	@Test
	public void testReturnsTheSameTypeUnknownClass() {
		assertFalse(ReflectionUtil.returnsTheSameType("does.not.Exist", "java.util.List", "add", 1));
		assertFalse(ReflectionUtil.returnsTheSameType("java.util.ArrayList", "does.not.Exist", "add", 1));
	}

	/**
	 * {@code ClasspathScopeBase} declares one field per visibility, static and instance; the other
	 * package is the preferimport fixtures', and an empty package is the default one.
	 */
	@CsvSource({
			"ClasspathScopeBase, Float, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeBase, Float, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Float, , false, true",
			"ClasspathScopeBase, Integer, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeBase, Integer, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Integer, , false, true",
			"ClasspathScopeBase, Short, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeBase, Short, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Short, , false, false",
			"ClasspathScopeBase, Double, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeBase, Double, com.etk2000.checkstyle.testhelpers, false, false",
			"ClasspathScopeBase, Double, , false, false",
			"ClasspathScopeBase, Boolean, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeBase, Boolean, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Boolean, , false, true",
			"ClasspathScopeBase, Long, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeBase, Long, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Long, , false, true",
			"ClasspathScopeBase, Byte, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeBase, Byte, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeBase, Byte, , false, false",
			"ClasspathScopeBase, Character, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeBase, Character, com.etk2000.checkstyle.testhelpers, false, false",
			"ClasspathScopeBase, Character, , false, false",
			"ClasspathScopeBase, Float, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Float, com.etk2000.checkstyle.testhelpers, true, false",
			"ClasspathScopeBase, Float, , true, false",
			"ClasspathScopeBase, Integer, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Integer, com.etk2000.checkstyle.testhelpers, true, false",
			"ClasspathScopeBase, Integer, , true, false",
			"ClasspathScopeBase, Short, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Short, com.etk2000.checkstyle.testhelpers, true, false",
			"ClasspathScopeBase, Short, , true, false",
			"ClasspathScopeBase, Double, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Double, com.etk2000.checkstyle.testhelpers, true, false",
			"ClasspathScopeBase, Double, , true, false",
			"ClasspathScopeBase, Boolean, com.etk2000.checkstyle.inputs.preferimport, true, true",
			"ClasspathScopeBase, Boolean, com.etk2000.checkstyle.testhelpers, true, true",
			"ClasspathScopeBase, Boolean, , true, true",
			"ClasspathScopeBase, Long, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Long, com.etk2000.checkstyle.testhelpers, true, true",
			"ClasspathScopeBase, Long, , true, false",
			"ClasspathScopeBase, Byte, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Byte, com.etk2000.checkstyle.testhelpers, true, true",
			"ClasspathScopeBase, Byte, , true, false",
			"ClasspathScopeBase, Character, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeBase, Character, com.etk2000.checkstyle.testhelpers, true, false",
			"ClasspathScopeBase, Character, , true, false",
			"ClasspathScopeBase, Thread, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeBase, Thread, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeMiddle, Integer, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeMiddle, Short, com.etk2000.checkstyle.testhelpers, false, true",
			"ClasspathScopeMiddle, Short, com.etk2000.checkstyle.inputs.preferimport, false, false",
			"ClasspathScopeMiddle, Double, com.etk2000.checkstyle.testhelpers, false, false",
			"ClasspathScopeMiddle, Boolean, com.etk2000.checkstyle.inputs.preferimport, true, true",
			"ClasspathScopeMiddle, Long, com.etk2000.checkstyle.testhelpers, true, true",
			"ClasspathScopeMiddle, Long, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeMiddle, Byte, com.etk2000.checkstyle.testhelpers, true, true",
			"ClasspathScopeConstants, System, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeConstants, System, com.etk2000.checkstyle.inputs.preferimport, true, true",
			"ClasspathScopeGenericBase, String, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeOuter.Inner, Thread, com.etk2000.checkstyle.inputs.preferimport, false, true",
			"ClasspathScopeOuter.Inner, Thread, com.etk2000.checkstyle.inputs.preferimport, true, false",
			"ClasspathScopeOuter.Inner, Runtime, com.etk2000.checkstyle.inputs.preferimport, true, true"
	})
	@ParameterizedTest
	public void testSeesFieldOfTestHelper(
			@Nonnull String simpleName,
			@Nonnull String fieldName,
			@Nullable String fromPackage,
			boolean staticImport,
			boolean expected
	) {
		assertEquals(expected, ReflectionUtil.seesField("com.etk2000.checkstyle.testhelpers." + simpleName, fieldName, fromPackage, staticImport));
	}

	@CsvSource({
			"java.util.ArrayList, modCount, false, true",
			"java.lang.Integer, value, false, false",
			"java.lang.Integer, MAX_VALUE, true, true",
			"javax.swing.text.StyleConstants, TabSet, true, true",
			"java.lang.Math, Integer, true, false"
	})
	@ParameterizedTest
	public void testSeesFieldOnJdkClasses(@Nonnull String fqcn, @Nonnull String fieldName, boolean staticImport, boolean expected) {
		assertEquals(expected, ReflectionUtil.seesField(fqcn, fieldName, "com.etk2000.checkstyle.inputs.preferimport", staticImport));
	}

	/** The public fields read even when another field's type is missing, so a public match is found before the failure. */
	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testSeesFieldReadsAPublicFieldBeforeAnUnreadableOne(boolean staticImport, @TempDir Path dir) throws Exception {
		defineLinkageProbes(dir);
		assertEquals(Boolean.TRUE, ReflectionUtil.seesField("com.etk2000.checkstyle.InputLinkageProbeFieldTypeHolder", "Integer", "com.etk2000.checkstyle.inputs.preferimport", staticImport));
	}

	@ParameterizedTest
	@ValueSource(booleans = {false, true})
	public void testSeesFieldUnloadableClassIsUnknown(boolean staticImport) {
		assertNull(ReflectionUtil.seesField("com.unknown.Missing", "Integer", "com.etk2000.checkstyle.inputs.preferimport", staticImport));
	}

	@CsvSource({
			"UUID, false",
			"UUID, true",
			"Absent, false",
			"Absent, true"
	})
	@ParameterizedTest
	public void testSeesFieldWithAnUnreadableFieldType(@Nonnull String fieldName, boolean staticImport, @TempDir Path dir) throws Exception {
		defineLinkageProbes(dir);
		assertNull(ReflectionUtil.seesField("com.etk2000.checkstyle.InputLinkageProbeFieldTypeHolder", fieldName, "com.etk2000.checkstyle.inputs.preferimport", staticImport));
	}

	@MethodSource("staticReturnTypeNamesCases")
	@ParameterizedTest
	public void testStaticReturnTypeNames(@Nonnull String fqcn, @Nonnull String methodName, int argCount, @Nonnull Set<String> expected) {
		assertEquals(expected, Set.copyOf(ReflectionUtil.staticReturnTypeNames(fqcn, methodName, argCount)));
	}
}