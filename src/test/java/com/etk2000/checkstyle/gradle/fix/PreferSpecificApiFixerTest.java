package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferSpecificApiFixerTest {
	private final CheckstyleFixer fixer = new PreferSpecificApiFixer();

	@Test
	public void testArraysAsList() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var list = Arrays.asList(\"a\", \"b\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var list = List.of(\"a\", \"b\");", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testArraysAsListNoArgs() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var list = Arrays.asList();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var list = List.of();", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testAssertEqualsFalseAddsStaticImport() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"\t\tassertEquals(false, result);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals("\t\tassertFalse(result);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.Assert.assertFalse"), result.importsToAdd());
	}

	@Test
	public void testAssertEqualsFalseLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(false, result);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertFalse(result);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsFalseLiteralFirstJunit5Import() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.jupiter.api.Assertions.assertEquals;",
				"\t\tassertEquals(false, result);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals("\t\tassertFalse(result);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertFalse"), result.importsToAdd());
	}

	@Test
	public void testAssertEqualsFalseLiteralFirstNoImportWithWildcard() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.*;",
				"\t\tassertEquals(false, result);"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals("\t\tassertFalse(result);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsFalseLiteralFirstWithTrailingMessage() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(false, result, \"msg\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertFalse(result, \"msg\");", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsNullLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(getValue(), null);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNull(getValue());", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsNullLiteralMiddleJunit4() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(\"msg\", null, obj);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNull(\"msg\", obj);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsNullLiteralMiddleJunit5() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(obj, null, \"msg\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNull(obj, \"msg\");", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsTrueLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(true, value);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertTrue(value);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsTrueLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(value, true);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertTrue(value);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertEqualsTrueLiteralMiddle() {
		final var lines = new ArrayList<>(List.of("\t\tassertEquals(\"msg\", true, value);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertTrue(\"msg\", value);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertNotEqualsFalseLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotEquals(false, x);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertTrue(x);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertNotEqualsNullLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotEquals(null, obj);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNotNull(obj);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertNotSameNullLiteralFirst() {
		final var lines = new ArrayList<>(List.of("\t\tassertNotSame(null, obj);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNotNull(obj);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAssertSameNullLiteralLast() {
		final var lines = new ArrayList<>(List.of("\t\tassertSame(obj, null);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tassertNull(obj);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectionsEmptyList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> list = Collections.emptyList();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tList<String> list = List.of();", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testCollectionsEmptyMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> map = Collections.emptyMap();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tMap<String, String> map = Map.of();", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Map"), result.importsToAdd());
	}

	@Test
	public void testCollectionsEmptySet() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> set = Collections.emptySet();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tSet<String> set = Set.of();", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Set"), result.importsToAdd());
	}

	@Test
	public void testCollectionsSingleton() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> set = Collections.singleton(\"a\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tSet<String> set = Set.of(\"a\");", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Set"), result.importsToAdd());
	}

	@Test
	public void testCollectionsSingletonList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> list = Collections.singletonList(\"a\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tList<String> list = List.of(\"a\");", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testCollectionsSingletonMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> map = Collections.singletonMap(\"k\", \"v\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tMap<String, String> map = Map.of(\"k\", \"v\");", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Map"), result.importsToAdd());
	}

	@Test
	public void testCollectionsSortNoComparator() {
		final var lines = new ArrayList<>(List.of("\t\tCollections.sort(list);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlist.sort(null);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectionsSortNoComparatorNestedArg() {
		final var lines = new ArrayList<>(List.of("\t\tCollections.sort(getList());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tgetList().sort(null);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectionsSortWithComparator() {
		final var lines = new ArrayList<>(List.of("\t\tCollections.sort(list, Comparator.naturalOrder());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlist.sort(Comparator.naturalOrder());", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectionsSortWithLambdaComparator() {
		final var lines = new ArrayList<>(List.of("\t\tCollections.sort(list, (a, b) -> a.compareTo(b));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlist.sort((a, b) -> a.compareTo(b));", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectionsUnmodifiableList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> result = Collections.unmodifiableList(list);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tList<String> result = List.copyOf(list);", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testCollectionsUnmodifiableListAsList() {
		final var lines = new ArrayList<>(List.of("\t\tList<String> list = Collections.unmodifiableList(Arrays.asList(\"a\", \"b\"));"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tList<String> list = List.copyOf(Arrays.asList(\"a\", \"b\"));", result.replacement().getFirst());
		assertEquals(Set.of("java.util.List"), result.importsToAdd());
	}

	@Test
	public void testCollectionsUnmodifiableMap() {
		final var lines = new ArrayList<>(List.of("\t\tMap<String, String> result = Collections.unmodifiableMap(map);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tMap<String, String> result = Map.copyOf(map);", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Map"), result.importsToAdd());
	}

	@Test
	public void testCollectionsUnmodifiableSet() {
		final var lines = new ArrayList<>(List.of("\t\tSet<String> result = Collections.unmodifiableSet(set);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tSet<String> result = Set.copyOf(set);", result.replacement().getFirst());
		assertEquals(Set.of("java.util.Set"), result.importsToAdd());
	}

	@Test
	public void testCollectToList() {
		final var lines = new ArrayList<>(List.of("\t\t\t\t.collect(Collectors.toList());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\t\t\t.toList();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCollectToUnmodifiableList() {
		final var lines = new ArrayList<>(List.of("\t\t\t\t.collect(Collectors.toUnmodifiableList());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\t\t\t.toList();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEqualsEmpty() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.equals(\"\"))"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testGetFirst() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var first = list.get(0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var first = list.getFirst();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIndexOfCharBackslash() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\\\\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\\\\');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharColumnAtLparen() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"x\");"));
		final var col = lines.getFirst().indexOf('(');
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('x');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharDoubleQuote() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\\"\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\"');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharEmptyStringFallsThrough() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, col));
	}

	@Test
	public void testIndexOfCharMultiCharFallsThrough() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"foo\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, col));
	}

	@Test
	public void testIndexOfCharNewline() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\n\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\\n');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharOctalEscape() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\077\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\\077');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharRefusesInvalidEscape() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\z\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, col));
	}

	@Test
	public void testIndexOfCharRefusesInvalidUnicodeEscape() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\uABCG\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, col));
	}

	@Test
	public void testIndexOfCharSimple() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"x\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('x');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharSingleQuoteEscape() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"'\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\\'');", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharTwoArg() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"x\", 5);"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('x', 5);", result.replacement().getFirst());
	}

	@Test
	public void testIndexOfCharUnicodeEscape() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.indexOf(\"\\u00e9\");"));
		final var col = lines.getFirst().indexOf("s.indexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.indexOf('\\u00e9');", result.replacement().getFirst());
	}

	@Test
	public void testKeySetContains() {
		final var lines = new ArrayList<>(List.of("\t\tif (map.keySet().contains(\"key\"))"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (map.containsKey(\"key\"))", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLastIndexOfCharSlash() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var i = s.lastIndexOf(\"/\");"));
		final var col = lines.getFirst().indexOf("s.lastIndexOf");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, col));
		assertEquals("\t\tfinal var i = s.lastIndexOf('/');", result.replacement().getFirst());
	}

	@Test
	public void testLengthIsEmptyAlreadyNegated() {
		final var lines = new ArrayList<>(List.of("\t\tif (!str.length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (str.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLengthIsEmptyComplexReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (getStr().length() > 0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.name.length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!obj.name.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLengthIsEmptyEqualsZeroFollowedByLetter() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.length() == 0xF)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyInCompoundReversedCondition() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 != x && 0 != s.length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (0 != x && !s.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLengthIsEmptyLessThanFollowedByDigit() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.length() < 10)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyLessThanOneFollowedByDecimal() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.length() < 1.5)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyLessThanOneFollowedByUnderscore() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.length() < 1_0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyMultipleOccurrencesFirstRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (a.length() == 0xF || b.length() == 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (a.length() == 0xF || b.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLengthIsEmptyReversedAfterDigitsRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (300 == s.length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testLengthIsEmptyReversedFirstRejectedSecondAccepted() {
		final var lines = new ArrayList<>(List.of("\t\tif (idx10 == 0 && 0 == s.length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (idx10 == 0 && s.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testLengthIsEmptyReversedMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 == foo().length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@CsvSource({
			// .length() positive normal
			"str.length() == 0,    str.isEmpty()",
			"str.length() <= 0,    str.isEmpty()",
			"str.length() < 1,     str.isEmpty()",
			// .length() negated normal
			"str.length() != 0,    !str.isEmpty()",
			"str.length() > 0,     !str.isEmpty()",
			"str.length() >= 1,    !str.isEmpty()",
			// .length() positive reversed
			"0 == str.length(),    str.isEmpty()",
			"0 >= str.length(),    str.isEmpty()",
			"1 > str.length(),     str.isEmpty()",
			// .length() negated reversed
			"0 != str.length(),    !str.isEmpty()",
			"0 < str.length(),     !str.isEmpty()",
			"1 <= str.length(),    !str.isEmpty()",
			// .size() positive normal
			"list.size() == 0,     list.isEmpty()",
			"list.size() <= 0,     list.isEmpty()",
			"list.size() < 1,      list.isEmpty()",
			// .size() negated normal
			"list.size() != 0,     !list.isEmpty()",
			"list.size() > 0,      !list.isEmpty()",
			"list.size() >= 1,     !list.isEmpty()",
			// .size() positive reversed
			"0 == list.size(),     list.isEmpty()",
			"0 >= list.size(),     list.isEmpty()",
			"1 > list.size(),      list.isEmpty()",
			// .size() negated reversed
			"0 != list.size(),     !list.isEmpty()",
			"0 < list.size(),      !list.isEmpty()",
			"1 <= list.size(),     !list.isEmpty()"
	})
	@ParameterizedTest
	public void testLengthOrSizeIsEmpty(String input, String expected) {
		final var lines = new ArrayList<>(List.of("\t\tif (" + input.strip() + ")"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (" + expected.strip() + ")", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testNoMatch() {
		final var lines = new ArrayList<>(List.of("\t\tSystem.out.println(\"hello\");"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testRemoveFirst() {
		final var lines = new ArrayList<>(List.of("\t\tlist.remove(0);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlist.removeFirst();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testReplaceAll() {
		final var lines = new ArrayList<>(List.of("\t\tString result = s.replaceAll(\"foo\", \"bar\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tString result = s.replace(\"foo\", \"bar\");", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStreamCount() {
		final var lines = new ArrayList<>(List.of("\t\tlong count = list.stream().count();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlong count = list.size();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStreamFindFirstIsPresent() {
		final var lines = new ArrayList<>(List.of("\t\tif (list.stream().findFirst().isPresent())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!list.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStreamFindFirstIsPresentAlreadyNegated() {
		final var lines = new ArrayList<>(List.of("\t\tif (!list.stream().findFirst().isPresent())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (list.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStreamFindFirstIsPresentDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.list.stream().findFirst().isPresent())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!obj.list.isEmpty())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStreamFindFirstIsPresentMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (getList().stream().findFirst().isPresent())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStreamForEach() {
		final var lines = new ArrayList<>(List.of("\t\tlist.stream().forEach(System.out::println);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tlist.forEach(System.out::println);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatCharLiteralInArgs() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"%c\", ')');"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"%c\".formatted(')');", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatEscapedQuotes() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"Say \\\"hi\\\"\", name);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"Say \\\"hi\\\"\".formatted(name);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatMultipleArgs() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"Hello %s, age %d\", name, age);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"Hello %s, age %d\".formatted(name, age);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatNestedParens() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"Hello %s\", getName());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"Hello %s\".formatted(getName());", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatNonLiteralMultiArgReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(fmt, name);"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStringFormatOneArg() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"Hello %s\", name);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"Hello %s\".formatted(name);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatSingleArgCastExpression() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format((String) arr[0]);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = (String) arr[0];", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatSingleArgLiteral() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(\"literal\");"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = \"literal\";", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatSingleArgMethodCall() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(a.toString());"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = a.toString();", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStringFormatSingleArgVariable() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var s = String.format(fmt);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var s = fmt;", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripIsBlank() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().isEmpty())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripIsBlankDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.name.strip().isEmpty())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (obj.name.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripIsBlankMethodReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (getText().strip().isEmpty())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (getText().isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthAlreadyNegated() {
		final var lines = new ArrayList<>(List.of("\t\tif (!s.strip().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthEqualsZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() == 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthEqualsZeroFollowedByLetter() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() == 0xF)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthGreaterEqualOne() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() >= 1)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthGreaterThanZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthGreaterThanZeroDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.name.strip().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!obj.name.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthGreaterThanZeroMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (getText().strip().length() > 0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthInCompoundReversedCondition() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 != x && 0 != s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (0 != x && !s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthLessEqualZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() <= 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthLessThanFollowedByDigit() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() < 10)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthLessThanOne() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() < 1)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthLessThanOneFollowedByDecimal() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() < 1.5)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthLessThanOneFollowedByUnderscore() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() < 1_0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthMultipleOccurrencesFirstRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (a.strip().length() == 0xF || b.strip().length() == 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (a.strip().length() == 0xF || b.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthNotEqualsZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.strip().length() != 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedAfterDigitsRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (300 == s.strip().length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthReversedFirstRejectedSecondAccepted() {
		final var lines = new ArrayList<>(List.of("\t\tif (idx10 == 0 && 0 == s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (idx10 == 0 && s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 == foo().strip().length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testStripLengthReversedNegatedLessThan() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 < s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedNegatedNotEquals() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 != s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedNegatedOneLessEqual() {
		final var lines = new ArrayList<>(List.of("\t\tif (1 <= s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedPositiveGreaterEqual() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 >= s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedPositiveOneGreaterThan() {
		final var lines = new ArrayList<>(List.of("\t\tif (1 > s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testStripLengthReversedPositiveZeroEquals() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 == s.strip().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testToArrayMultiDimensionalReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var arr = list.toArray(new String[0][]);"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testToArrayNewZero() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var arr = list.toArray(new String[0]);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tfinal var arr = list.toArray(String[]::new);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testToArrayNonZeroReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var arr = list.toArray(new String[10]);"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimIsBlank() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().isEmpty())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimIsBlankDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.name.trim().isEmpty())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (obj.name.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthAlreadyNegated() {
		final var lines = new ArrayList<>(List.of("\t\tif (!s.trim().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthEqualsZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() == 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthEqualsZeroFollowedByLetter() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() == 0xF)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthEqualsZeroReversed() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 == s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthGreaterEqualOne() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() >= 1)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthGreaterThanZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthGreaterThanZeroDottedReceiver() {
		final var lines = new ArrayList<>(List.of("\t\tif (obj.name.trim().length() > 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!obj.name.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthGreaterThanZeroMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (getText().trim().length() > 0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthInCompoundReversedCondition() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 != x && 0 != s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (0 != x && !s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthLessEqualZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() <= 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthLessThanFollowedByDigit() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() < 10)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthLessThanOne() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() < 1)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthLessThanOneFollowedByDecimal() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() < 1.5)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthLessThanOneFollowedByUnderscore() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() < 1_0)"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthMultipleOccurrencesFirstRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (a.trim().length() == 0xF || b.trim().length() == 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (a.trim().length() == 0xF || b.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthNotEqualsZero() {
		final var lines = new ArrayList<>(List.of("\t\tif (s.trim().length() != 0)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedAfterDigitsRejected() {
		final var lines = new ArrayList<>(List.of("\t\tif (300 == s.trim().length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthReversedFirstRejectedSecondAccepted() {
		final var lines = new ArrayList<>(List.of("\t\tif (idx10 == 0 && 0 == s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (idx10 == 0 && s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedMethodReceiverReturnsSkipResult() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 == foo().trim().length())"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals(SkipMessages.PREFER_API_SKIP, result.reason());
	}

	@Test
	public void testTrimLengthReversedNegated() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 != s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedNegatedLessThan() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 < s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedNegatedOneLessEqual() {
		final var lines = new ArrayList<>(List.of("\t\tif (1 <= s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (!s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedPositive() {
		final var lines = new ArrayList<>(List.of("\t\tif (0 >= s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTrimLengthReversedPositiveOneGreaterThan() {
		final var lines = new ArrayList<>(List.of("\t\tif (1 > s.trim().length())"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (s.isBlank())", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testValuesContains() {
		final var lines = new ArrayList<>(List.of("\t\tif (map.values().contains(\"value\"))"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("\t\tif (map.containsValue(\"value\"))", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}
}