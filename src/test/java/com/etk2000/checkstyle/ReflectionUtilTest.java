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

import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class ReflectionUtilTest {
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
				Arguments.of("java.util.EnumMap", "Map")
		);
	}

	@MethodSource("applyJavaNamingHeuristicProvider")
	@ParameterizedTest
	public void testApplyJavaNamingHeuristic(@Nonnull String input, @Nonnull String expected) {
		assertEquals(expected, ReflectionUtil.applyJavaNamingHeuristic(input));
	}

	@Test
	public void testClearCacheDoesNotResetCounter() {
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "size"));
		final var afterCall = ReflectionUtil.classForNameCallCount;
		assertNotEquals(0, afterCall);
		ReflectionUtil.clearCache();
		assertEquals(afterCall, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testFindCharsetStringArgIndexConstructors() {
		// String(byte[], String charsetName) -> String(byte[], Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "new", 2));
		// String(byte[], int, int, String) -> String(byte[], int, int, Charset)
		assertEquals(3, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "new", 4));
		// InputStreamReader(InputStream, String) -> InputStreamReader(InputStream, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.InputStreamReader", "new", 2));
		// OutputStreamWriter(OutputStream, String) -> OutputStreamWriter(OutputStream, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.OutputStreamWriter", "new", 2));
		// PrintStream(File, String) -> PrintStream(File, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintStream", "new", 2));
		// PrintStream(OutputStream, boolean, String) -> PrintStream(OutputStream, boolean, Charset)
		assertEquals(2, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintStream", "new", 3));
		// PrintWriter(File, String) -> PrintWriter(File, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.io.PrintWriter", "new", 2));
		// Scanner(InputStream, String) -> Scanner(InputStream, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.util.Scanner", "new", 2));
	}

	@Test
	public void testFindCharsetStringArgIndexMethods() {
		// String.getBytes(String) -> String.getBytes(Charset)
		assertEquals(0, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "getBytes", 1));
		// URLEncoder.encode(String, String) -> URLEncoder.encode(String, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.net.URLEncoder", "encode", 2));
		// URLDecoder.decode(String, String) -> URLDecoder.decode(String, Charset)
		assertEquals(1, ReflectionUtil.findCharsetStringArgIndex("java.net.URLDecoder", "decode", 2));
		// ByteArrayOutputStream.toString(String) -> ByteArrayOutputStream.toString(Charset)
		assertEquals(0, ReflectionUtil.findCharsetStringArgIndex("java.io.ByteArrayOutputStream", "toString", 1));
	}

	@Test
	public void testFindCharsetStringArgIndexNoCharsetOverload() {
		// Charset.forName(String) has no Charset.forName(Charset) overload
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.nio.charset.Charset", "forName", 1));
		// String.valueOf(Object) has no Charset variant
		assertEquals(-1, ReflectionUtil.findCharsetStringArgIndex("java.lang.String", "valueOf", 1));
	}

	@Test
	public void testFindCharsetStringArgIndexNoConstructorWithThatCount() {
		// PrintWriter has no 3-arg String constructor (only Charset)
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
	public void testGetMethodReturnTypeNameUnknownClass() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("com.nonexistent.FakeClass", "method"));
	}

	@Test
	public void testGetMethodReturnTypeNameUnknownMethod() {
		assertNull(ReflectionUtil.getMethodReturnTypeName("java.lang.String", "nonexistentMethod"));
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
		// Arrays.sort(int[]) is non-varargs with array last param; no competing varargs at argCount=1
		// This tests the isArray() branch: int[].isArray() is true, so it would block if a varargs overload existed
		// Since no varargs overload exists at argCount=1, componentType stays null and returns null
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
	public void testHasGenericReturnTypeClassLevelTypeParam() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "get"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "iterator"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Map", "get"));
	}

	@Test
	public void testHasGenericReturnTypeInferableFromArgs() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.Collections", "min"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "of"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.Class", "cast"));
	}

	@Test
	public void testHasGenericReturnTypeNeedsTargetType() {
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Collections", "emptyList"));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Collections", "emptySet"));
		assertTrue(ReflectionUtil.hasGenericReturnType("java.util.Optional", "empty"));
	}

	@Test
	public void testHasGenericReturnTypeNonGenericMethod() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "valueOf"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "length"));
		assertFalse(ReflectionUtil.hasGenericReturnType("java.util.List", "size"));
	}

	@Test
	public void testHasGenericReturnTypeUnknownClass() {
		assertFalse(ReflectionUtil.hasGenericReturnType("com.nonexistent.FakeClass", "method"));
	}

	@Test
	public void testHasGenericReturnTypeUnknownMethod() {
		assertFalse(ReflectionUtil.hasGenericReturnType("java.lang.String", "nonexistentMethod"));
	}

	@Test
	public void testHasMethodCachedSuccess() {
		for (var i = 0; i < 5; ++i)
			assertTrue(ReflectionUtil.hasMethod("java.util.ArrayList", "add"));
	}

	@Test
	public void testHasMethodCountInnerClassResolvesInOneCall() {
		// heuristic differs from input: java.util.Map$Entry resolves on the
		// fast path in exactly one Class.forName invocation
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertTrue(ReflectionUtil.hasMethod("java.util.Map.Entry", "getKey"));
		assertEquals(1, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testHasMethodCountMitigationFallsBackToSlowPath() {
		// mitigation triggers (lowercase mid-segment after first uppercase),
		// heuristic returns input unchanged, slow path runs:
		// 1) try original (fail) 2) substitute Inner (fail) 3) lowercase guard stops
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertFalse(ReflectionUtil.hasMethod("pkg.Outer.lower.Inner", "x"));
		assertEquals(2, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testHasMethodCountTopLevelClassResolvesInOneCall() {
		// top-level class: heuristic equals input, fast path skipped, slow
		// path resolves on the first attempt
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "size"));
		assertEquals(1, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testHasMethodCountTwoLevelInnerResolvesInOneCall() {
		// 3-level deep nesting: heuristic produces the full $-form in one
		// shot, no slow-path iterations needed
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertTrue(ReflectionUtil.hasMethod(
				"com.etk2000.checkstyle.ReflectionUtilTest.TwoLevelOuter.TwoLevelMid.TwoLevelLeaf",
				"getValue"
		));
		assertEquals(1, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testHasMethodCountUnresolvableCachedSecondCallZero() {
		// failed lookups are cached as Optional.empty(); a second call with
		// the same FQN must hit the cache and not invoke Class.forName again
		ReflectionUtil.clearCache();
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		ReflectionUtil.classForNameCallCount = 0;
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		assertEquals(0, ReflectionUtil.classForNameCallCount);
	}

	@Test
	public void testHasMethodCountUnresolvableTopLevelOneCall() {
		// pin that the counter increments even on Class.forName failure:
		// no-dot input runs slow path once (fail), then exits on lastDot < 0
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount = 0;
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
		assertEquals(1, ReflectionUtil.classForNameCallCount);
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
		// FQN already in JVM '$' form: heuristic returns same string, fast
		// path skipped, slow path resolves directly. Verifies cache key is
		// the original input form (not the heuristic).
		assertTrue(ReflectionUtil.hasMethod("java.util.Map$Entry", "getKey"));
	}

	@Test
	public void testHasMethodInnerClassDottedName() {
		// Java FQN uses '.', JVM expects '$'; loadClass should retry with substitution
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
		// uppercase segment followed by lowercase segment after first class:
		// heuristic mitigation returns input unchanged so we never load a
		// substituted form the slow path's lowercase guard would have rejected
		assertFalse(ReflectionUtil.hasMethod("pkg.Outer.lower.Inner", "x"));
	}

	@Test
	public void testHasMethodNoDotsBareName() {
		// no-dot input: substitution loop has no '.' to replace, returns null without retry
		assertFalse(ReflectionUtil.hasMethod("Bareword", "method"));
	}

	@Test
	public void testHasMethodTrailingDot() {
		// rightmost char is '.': lastDot+1 == length, guard returns null without
		// attempting charAt on out-of-bounds index
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
}