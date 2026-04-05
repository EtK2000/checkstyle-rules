package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferSpecificApiFixerTest {
	private final CheckstyleFixer fixer = new PreferSpecificApiFixer();

	@Test
	public void testAssertEqualsFalseLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(false, result);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertFalse(result);", result.replacement().getFirst());
	}

	@Test
	public void testAssertEqualsNullLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(getValue(), null);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertNull(getValue());", result.replacement().getFirst());
	}

	@Test
	public void testAssertEqualsTrueLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(true, value);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertTrue(value);", result.replacement().getFirst());
	}

	@Test
	public void testAssertEqualsTrueLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(value, true);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertTrue(value);", result.replacement().getFirst());
	}

	@Test
	public void testAssertNotEqualsFalseLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotEquals(false, x);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertTrue(x);", result.replacement().getFirst());
	}

	@Test
	public void testAssertNotEqualsNullLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotEquals(null, obj);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertNotNull(obj);", result.replacement().getFirst());
	}

	@Test
	public void testAssertNotSameNullLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotSame(null, obj);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertNotNull(obj);", result.replacement().getFirst());
	}

	@Test
	public void testAssertSameNullLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertSame(obj, null);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tassertNull(obj);", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsEmptyList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> list = Collections.emptyList();"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tList<String> list = List.of();", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsEmptyMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> map = Collections.emptyMap();"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tMap<String, String> map = Map.of();", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsEmptySet() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> set = Collections.emptySet();"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tSet<String> set = Set.of();", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsSingleton() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> set = Collections.singleton(\"a\");"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tSet<String> set = Set.of(\"a\");", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsSingletonList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> list = Collections.singletonList(\"a\");"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tList<String> list = List.of(\"a\");", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsSingletonMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> map = Collections.singletonMap(\"k\", \"v\");"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tMap<String, String> map = Map.of(\"k\", \"v\");", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsUnmodifiableList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> result = Collections.unmodifiableList(list);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tList<String> result = List.copyOf(list);", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsUnmodifiableMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> result = Collections.unmodifiableMap(map);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tMap<String, String> result = Map.copyOf(map);", result.replacement().getFirst());
	}

	@Test
	public void testCollectionsUnmodifiableSet() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> result = Collections.unmodifiableSet(set);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tSet<String> result = Set.copyOf(set);", result.replacement().getFirst());
	}

	@Test
	public void testCollectToList() {
		final var lines = new ArrayList<>(List.of("\t\t\t\t.collect(Collectors.toList());"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\t\t\t.toList();", result.replacement().getFirst());
	}

	@Test
	public void testCollectToUnmodifiableList() {
		final var lines = new ArrayList<>(List.of("\t\t\t\t.collect(Collectors.toUnmodifiableList());"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\t\t\t.toList();", result.replacement().getFirst());
	}

	@Test
	public void testEqualsEmpty() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.equals(\"\"))"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tif (s.isEmpty())", result.replacement().getFirst());
	}

	@Test
	public void testNoMatch() {
		final var lines = new ArrayList<>(List.of("\t\tSystem.out.println(\"hello\");"));
		assertNull(fixer.fix(lines, 0, 0));
	}

	@Test
	public void testStreamForEach() {
		final var lines = new ArrayList<>(List.of("\t\tlist.stream().forEach(System.out::println);"));
		final var result = fixer.fix(lines, 0, 0);
		assertNotNull(result);
		assertEquals("\t\tlist.forEach(System.out::println);", result.replacement().getFirst());
	}
}