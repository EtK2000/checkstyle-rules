package com.etk2000.checkstyle.inputs.specificapi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class InputSpecificApiClean {
	void assertEqualsWithTwoNonLiterals() {
		assertEquals("a", "b");
	}

	void assertFalseAlready() {
		assertFalse(1 == 2);
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

	void assertTrueAlready() {
		assertTrue(1 == 1);
	}

	void collectToSet(List<String> list) {
		Set<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toSet());
	}

	void collectToUnmodifiableList(List<String> list) {
		List<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toUnmodifiableList());
	}

	void collectWithCustomCollector(List<String> list) {
		String result = list.stream()
				.collect(Collectors.joining(", "));
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

	void lengthComparisonNotEmpty(StringBuilder sb) {
		if (sb.length() > 1)
			System.out.println("more than one char");
	}

	void mapRemoveZero(Map<Integer, String> map) {
		map.remove(0);
	}

	void removeFirst(List<String> list) {
		list.removeFirst();
	}

	void removeLast(List<String> list) {
		list.removeLast();
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

	void toListDirect(List<String> list) {
		List<String> result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}
}