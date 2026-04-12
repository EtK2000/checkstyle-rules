package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
	public void testHasMethodExists() {
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "getFirst"));
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "getLast"));
		assertTrue(ReflectionUtil.hasMethod("java.util.List", "get"));
	}

	@Test
	public void testHasMethodMissing() {
		assertFalse(ReflectionUtil.hasMethod("java.util.Map", "getFirst"));
		assertFalse(ReflectionUtil.hasMethod("java.util.Map", "getLast"));
	}

	@Test
	public void testHasMethodUnknownClass() {
		assertFalse(ReflectionUtil.hasMethod("com.nonexistent.FakeClass", "method"));
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