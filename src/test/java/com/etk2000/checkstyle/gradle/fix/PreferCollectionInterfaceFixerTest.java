package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class PreferCollectionInterfaceFixerTest {
	private static final PreferCollectionInterfaceFixer FIXER = new PreferCollectionInterfaceFixer();

	@Test
	public void testAnnotatedGenericArg() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tvoid f(ArrayList<@SuppressWarnings(\"unused\") String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(List<@SuppressWarnings(\"unused\") String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotatedTypeArg() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tvoid f(List<@SuppressWarnings(\"unused\") ArrayList<String>> items) {}"));
		final var result = FIXER.fix(lines, 1, 41);
		assertEquals(List.of("\tvoid f(List<@SuppressWarnings(\"unused\") List<String>> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayDequeToDeque() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayDeque;", "\tvoid f(ArrayDeque<String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Deque<String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayListToList() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tstatic ArrayList<String> getItems() {"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tstatic List<String> getItems() {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testBoundedTypeParamNotTouched() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tstatic <T extends Comparable<T>> ArrayList<T> sorted(List<T> items) {"));
		final var result = FIXER.fix(lines, 1, 34);
		assertEquals(List.of("\tstatic <T extends Comparable<T>> List<T> sorted(List<T> items) {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tvoid foo() {}"));
		assertNull(FIXER.fix(lines, 1, 100));
	}

	@Test
	public void testConcreteInBoundNotTouched() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tstatic <T extends ArrayList<String>> ArrayList<T> f(List<T> items) {"));
		final var result = FIXER.fix(lines, 1, 38);
		assertEquals(List.of("\tstatic <T extends ArrayList<String>> List<T> f(List<T> items) {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testConstructorParam() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tFoo(ArrayList<String> items) {"));
		final var result = FIXER.fix(lines, 1, 5);
		assertEquals(List.of("\tFoo(List<String> items) {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFqn() {
		final var lines = new ArrayList<>(List.of("\tstatic java.util.ArrayList<String> getItems() {"));
		final var result = FIXER.fix(lines, 0, 18);
		assertEquals(List.of("\tstatic java.util.List<String> getItems() {"), result.replacement());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testHashMapToMap() {
		final var lines = new ArrayList<>(List.of("import java.util.HashMap;", "\tvoid f(HashMap<String, Integer> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Map<String, Integer> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testHashSetToSet() {
		final var lines = new ArrayList<>(List.of("import java.util.HashSet;", "\tvoid f(HashSet<String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Set<String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIntersectionBoundNotTouched() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tstatic <T extends Comparable<T> & java.io.Serializable> ArrayList<T> f(List<T> items) {"));
		final var result = FIXER.fix(lines, 1, 57);
		assertEquals(List.of("\tstatic <T extends Comparable<T> & java.io.Serializable> List<T> f(List<T> items) {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLinkedHashMapToMap() {
		final var lines = new ArrayList<>(List.of("import java.util.LinkedHashMap;", "\tvoid f(LinkedHashMap<K, V> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Map<K, V> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLinkedHashSetToSet() {
		final var lines = new ArrayList<>(List.of("import java.util.LinkedHashSet;", "\tvoid f(LinkedHashSet<String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Set<String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLinkedListSkipped() {
		final var lines = new ArrayList<>(List.of("import java.util.LinkedList;", "\tstatic LinkedList<String> getItems() {"));
		assertNull(FIXER.fix(lines, 1, 8));
	}

	@Test
	public void testMultiLevelNesting() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tMap<String, Map<Integer, ArrayList<String>>> f() {"));
		final var result = FIXER.fix(lines, 1, 26);
		assertEquals(List.of("\tMap<String, Map<Integer, List<String>>> f() {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleParamsFirst() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "import java.util.HashMap;", "\tvoid f(ArrayList<String> a, HashMap<String, Integer> b) {}"));
		final var result = FIXER.fix(lines, 2, 8);
		assertEquals(List.of("\tvoid f(List<String> a, HashMap<String, Integer> b) {}"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultipleParamsSecond() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "import java.util.HashMap;", "\tvoid f(List<String> a, HashMap<String, Integer> b) {}"));
		final var result = FIXER.fix(lines, 2, 24);
		assertEquals(List.of("\tvoid f(List<String> a, Map<String, Integer> b) {}"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNestedGenericParam() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tvoid f(Map<String, ArrayList<Integer>> items) {}"));
		final var result = FIXER.fix(lines, 1, 20);
		assertEquals(List.of("\tvoid f(Map<String, List<Integer>> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNestedGenericReturn() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tMap<String, ArrayList<Integer>> f() {"));
		final var result = FIXER.fix(lines, 1, 13);
		assertEquals(List.of("\tMap<String, List<Integer>> f() {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoMatch() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "\tvoid f(List<String> items) {}"));
		assertNull(FIXER.fix(lines, 1, 8));
	}

	@Test
	public void testPriorityQueueToQueue() {
		final var lines = new ArrayList<>(List.of("import java.util.PriorityQueue;", "\tvoid f(PriorityQueue<String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Queue<String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testRawType() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tArrayList getItems() {"));
		final var result = FIXER.fix(lines, 1, 1);
		assertEquals(List.of("\tList getItems() {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testReturnAndParam() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "import java.util.HashSet;", "\tArrayList<String> f(HashSet<Integer> items) {"));
		final var resultReturn = FIXER.fix(lines, 2, 1);
		assertEquals(List.of("\tList<String> f(HashSet<Integer> items) {"), resultReturn.replacement());
		assertEquals(2, resultReturn.startLine());
		assertEquals(2, resultReturn.endLine());
		assertTrue(resultReturn.importsToAdd().isEmpty());

		final var resultParam = FIXER.fix(lines, 2, 21);
		assertEquals(List.of("\tArrayList<String> f(Set<Integer> items) {"), resultParam.replacement());
		assertEquals(2, resultParam.startLine());
		assertEquals(2, resultParam.endLine());
		assertTrue(resultParam.importsToAdd().isEmpty());
	}

	@Test
	public void testTreeMapToMap() {
		final var lines = new ArrayList<>(List.of("import java.util.TreeMap;", "\tstatic TreeMap<K, V> getItems() {"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tstatic Map<K, V> getItems() {"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTreeSetToSet() {
		final var lines = new ArrayList<>(List.of("import java.util.TreeSet;", "\tvoid f(TreeSet<String> items) {}"));
		final var result = FIXER.fix(lines, 1, 8);
		assertEquals(List.of("\tvoid f(Set<String> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWildcardExtends() {
		final var lines = new ArrayList<>(List.of("import java.util.ArrayList;", "\tvoid f(List<? extends ArrayList<String>> items) {}"));
		final var result = FIXER.fix(lines, 1, 23);
		assertEquals(List.of("\tvoid f(List<? extends List<String>> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWildcardSuper() {
		final var lines = new ArrayList<>(List.of("import java.util.HashSet;", "\tvoid f(Set<? super HashSet<Integer>> items) {}"));
		final var result = FIXER.fix(lines, 1, 20);
		assertEquals(List.of("\tvoid f(Set<? super Set<Integer>> items) {}"), result.replacement());
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}
}