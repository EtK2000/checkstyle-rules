package com.etk2000.checkstyle.inputs.specificapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class InputSpecificApiClean {
	void assertEqualsWithThreeNonLiterals() {
		assertEquals("a", "b", "msg");
	}

	void assertEqualsWithTwoNonLiterals() {
		assertEquals("a", "b");
	}

	void assertFalseAlready() {
		assertFalse(1 == 2);
	}

	void assertNotEqualsWithThreeNonLiterals() {
		assertNotEquals("a", "b", "msg");
	}

	void assertNotEqualsWithTwoNonLiterals() {
		assertNotEquals("a", "b");
	}

	void assertNotNullAlready() {
		assertNotNull(new Object());
	}

	void assertNotSameNonNull() {
		assertNotSame("a", "b");
	}

	void assertNotSameWithTrailingMessageTrue() {
		assertNotSame(true, Boolean.TRUE, "msg");
	}

	void assertNotSameWithTrue() {
		assertNotSame(true, Boolean.TRUE);
	}

	void assertNullAlready() {
		assertNull(null);
	}

	void assertSameNonNull() {
		assertSame("a", "a");
	}

	void assertSameWithFalse() {
		assertSame(false, Boolean.FALSE);
	}

	void assertSameWithTrailingMessageFalse() {
		assertSame(false, Boolean.FALSE, "msg");
	}

	void assertTrueAlready() {
		assertTrue(1 == 1);
	}

	void collectionsCopyOfAlreadyClean(List<String> list) {
		final var result = List.copyOf(list);
	}

	void collectionsEmptyListAlreadyClean() {
		final var list = List.of();
	}

	void collectionsSynchronizedList(List<String> list) {
		final var result = Collections.synchronizedList(list);
	}

	void collectToSet(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	void collectToUnmodifiableSet(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableSet());
	}

	void collectWithCustomCollector(List<String> list) {
		final var result = list.stream()
				.collect(Collectors.joining(", "));
	}

	void containsAlready(String s) {
		if (s.contains("foo"))
			System.out.println("found");
	}

	void containsKeyAlready(Map<String, String> map) {
		if (map.containsKey("key"))
			System.out.println("found");
	}

	void containsValueAlready(Map<String, String> map) {
		if (map.containsValue("value"))
			System.out.println("found");
	}

	void entrySetContains(Map<String, String> map) {
		if (map.entrySet().contains(Map.entry("k", "v")))
			System.out.println("found");
	}

	void equalsNonEmpty(String s) {
		if (s.equals("hello"))
			System.out.println("hello");
	}

	void equalsNonLiteral(String s, String other) {
		if (s.equals(other))
			System.out.println("equal");
	}

	void equalsNullSafe(String s) {
		if ("".equals(s))
			System.out.println("empty");
	}

	void forEachAlready(List<String> list) {
		list.forEach(System.out::println);
	}

	void getFirst(List<String> list) {
		System.out.println(list.getFirst());
	}

	void getLast(List<String> list) {
		System.out.println(list.getLast());
	}

	void getNonZeroIndex(List<String> list) {
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void indexOfCharVariant(String s) {
		if (s.indexOf('c') != -1)
			System.out.println("found char");
	}

	void indexOfEqualsZero(String s) {
		if (s.indexOf("foo") == 0)
			System.out.println("starts with");
	}

	void indexOfGreaterThanZero(String s) {
		if (s.indexOf("foo") > 0)
			System.out.println("found after first char");
	}

	void indexOfLessEqualZero(String s) {
		if (s.indexOf("foo") <= 0)
			System.out.println("at start or not found");
	}

	void indexOfNonLiteralArg(String s, String target) {
		if (s.indexOf(target) != -1)
			System.out.println("found variable");
	}

	void indexOfNotEqualZero(String s) {
		if (s.indexOf("foo") != 0)
			System.out.println("not at start");
	}

	void indexOfTwoArgs(String s) {
		if (s.indexOf("foo", 5) != -1)
			System.out.println("found after index 5");
	}

	void isEmptyAlready(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}

	void keySetSize(Map<String, String> map) {
		System.out.println(map.keySet().size());
	}

	void lengthComparisonNotEmpty(StringBuilder sb) {
		if (sb.length() > 1)
			System.out.println("more than one char");
	}

	void listSortAlready(List<String> list) {
		list.sort(Comparator.naturalOrder());
	}

	void mapRemoveZero(Map<Integer, String> map) {
		map.remove(0);
	}

	void parallelStreamCount(List<String> list) {
		final var count = list.parallelStream().count();
	}

	void parallelStreamForEach(List<String> list) {
		list.parallelStream().forEach(System.out::println);
	}

	void removeFirst(List<String> list) {
		list.removeFirst();
	}

	void removeLast(List<String> list) {
		list.removeLast();
	}

	void replaceAllWithRegex(String s) {
		final var result = s.replaceAll("foo.*bar", "baz");
	}

	void replaceAllWithVariable(String s, String pattern) {
		final var result = s.replaceAll(pattern, "baz");
	}

	void replaceAlready(String s) {
		final var result = s.replace("foo", "bar");
	}

	void sequentialAccess(List<String> list) {
		System.out.println(list.get(0));
		System.out.println(list.get(1));
		System.out.println(list.get(2));
	}

	void sequentialAccessFromEnd(List<String> list) {
		System.out.println(list.get(list.size() - 1));
		System.out.println(list.get(list.size() - 2));
	}

	void sequentialRemove(List<String> list) {
		list.remove(0);
		list.remove(1);
	}

	void sequentialRemoveFromEnd(List<String> list) {
		list.remove(list.size() - 1);
		list.remove(list.size() - 2);
	}

	void sizeComparisonNotEmpty(List<String> list) {
		if (list.size() > 1)
			System.out.println("more than one");
	}

	void sizeEqualsTwo(List<String> list) {
		if (list.size() == 2)
			System.out.println("pair");
	}

	void streamFilterCount(List<String> list) {
		final var count = list.stream().filter(s -> !s.isEmpty()).count();
	}

	void streamFilterFindFirstIsPresent(List<String> list) {
		if (list.stream().filter(s -> !s.isEmpty()).findFirst().isPresent())
			System.out.println("found");
	}

	void streamFilterForEach(List<String> list) {
		list.stream().filter(s -> !s.isEmpty()).forEach(System.out::println);
	}

	void toListDirect(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}
}