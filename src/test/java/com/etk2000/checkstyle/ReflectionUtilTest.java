package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.AbstractCollection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

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
	public void testFindCharsetStringArgIndexConstructors() {
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "new", 2));
		assertEquals(3, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "new", 4));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.InputStreamReader", "new", 2));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.OutputStreamWriter", "new", 2));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintStream", "new", 2));
		assertEquals(2, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintStream", "new", 3));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintWriter", "new", 2));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.util.Scanner", "new", 2));
	}

	@Test
	public void testFindCharsetStringArgIndexMethods() {
		assertEquals(0, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "getBytes", 1));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.net.URLEncoder", "encode", 2));
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.net.URLDecoder", "decode", 2));
		assertEquals(0, ReflectionUtil.findCharsetStringArgIndex("java.io.ByteArrayOutputStream", "toString", 1));
	}

	@Test
	public void testFindCharsetStringArgIndexNoCharsetOverload() {
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.nio.charset.Charset", "forName", 1));
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "valueOf", 1));
	}

	@Test
	public void testFindCharsetStringArgIndexNoConstructorWithThatCount() {
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintWriter", "new", 3));
	}

	@Test
	public void testFindCharsetStringArgIndexUnknownClass() {
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("com.nonexistent.FakeClass", "method", 1));
	}

	@Test
	public void testFindCharsetStringArgIndexWrongArgCount() {
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "getBytes", 3));
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "new", 5));
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
		assertEquals("java.util.List", ReflectionUtil.getMethodReturnTypeName("java.util.Collections", "unmodifiableList"));
		assertEquals("java.util.Map", ReflectionUtil.getMethodReturnTypeName("java.util.Collections", "unmodifiableMap"));
		assertEquals("int", ReflectionUtil.getMethodReturnTypeName("java.util.List", "size"));
	}

	@Test
	public void testGetMethodReturnTypeNameArrayReturn() {
		assertEquals("[C", ReflectionUtil.getMethodReturnTypeName("java.lang.String", "toCharArray"));
	}

	@Test
	public void testGetMethodReturnTypeNameUnknownClass() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("com.nonexistent.FakeClass", "method"));
	}

	@Test
	public void testGetMethodReturnTypeNameUnknownMethod() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("java.lang.String", "nonexistentMethod"));
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
	public void testParameterSelectsOnlyTheConstructedTypeAcceptsBoth() {
		assertFalse(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"java.util.List", "java.util.List", "java.util.ArrayList"
		));
	}

	@Test
	public void testParameterSelectsOnlyTheConstructedTypeAcceptsNeither() {
		assertFalse(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"java.lang.String", "java.util.List", "java.util.ArrayList"
		));
	}

	@Test
	public void testParameterSelectsOnlyTheConstructedTypeConstructedUnresolvable() {
		assertFalse(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"java.util.ArrayList", "java.util.List", "totally.bogus.Type"
		));
	}

	@Test
	public void testParameterSelectsOnlyTheConstructedTypeDeclaredUnresolvable() {
		assertFalse(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"java.util.ArrayList", "totally.bogus.Type", "java.util.ArrayList"
		));
	}

	@Test
	public void testParameterSelectsOnlyTheConstructedTypeNarrows() {
		assertTrue(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"java.util.ArrayList", "java.util.List", "java.util.ArrayList"
		));
	}

	@Test
	public void testParameterSelectsOnlyTheConstructedTypeParameterUnresolvable() {
		assertFalse(ReflectionUtil.parameterSelectsOnlyTheConstructedType(
				"totally.bogus.Type", "java.util.List", "java.util.ArrayList"
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
}