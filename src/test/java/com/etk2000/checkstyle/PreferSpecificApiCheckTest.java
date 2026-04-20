package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferSpecificApiCheckTest {
	private static final String DIR = "specificapi/";

	@Test
	public void testAssertViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiAssertViolation.java");
		assertEquals(41, violations.size());

		var i = 0;

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(18, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(34, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(38, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(42, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(46, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(54, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(66, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(70, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(74, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(78, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(82, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(86, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(90, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(94, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(98, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(102, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(106, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(110, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(114, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(118, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(122, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(126, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());

		assertEquals(130, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(134, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(138, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(142, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(146, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());

		assertEquals(150, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(154, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(158, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(162, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(166, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());

		assertEquals(170, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
	}

	@Test
	public void testCleanApiUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiClean.java").isEmpty());
	}

	@Test
	public void testCollectionsCopyOfNoDoubleFireWithArraysAsList() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiCopyOfViolation.java");
		// must be exactly 4 copyOf violations, no extra Arrays.asList violation
		for (var v : violations)
			assertEquals(SeverityLevel.ERROR, v.getSeverityLevel());
		assertEquals(4, violations.size());
	}

	@Test
	public void testCollectionsCopyOfViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiCopyOfViolation.java");
		assertEquals(4, violations.size());

		var i = 0;
		assertEquals(11, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.", violations.get(i++).getMessage());
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.copyOf(...)' instead of 'Collections.unmodifiableList(...)'.", violations.get(i++).getMessage());
		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.copyOf(...)' instead of 'Collections.unmodifiableMap(...)'.", violations.get(i++).getMessage());
		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.copyOf(...)' instead of 'Collections.unmodifiableSet(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testCollectionsFactoryViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsEmptyViolation.java");
		assertEquals(6, violations.size());

		var i = 0;
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of()' instead of 'Collections.emptyList()'.", violations.get(i++).getMessage());
		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.of()' instead of 'Collections.emptyMap()'.", violations.get(i++).getMessage());
		assertEquals(18, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.of()' instead of 'Collections.emptySet()'.", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.of(...)' instead of 'Collections.singleton(...)'.", violations.get(i++).getMessage());
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Collections.singletonList(...)'.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.of(...)' instead of 'Collections.singletonMap(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testCollectionsSortViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsSortViolation.java");
		assertEquals(2, violations.size());

		assertEquals(9, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.sort(...)' instead of 'Collections.sort(...)'.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.sort(...)' instead of 'Collections.sort(...)'.", violations.get(1).getMessage());
	}

	@Test
	public void testCollectToListViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiToListViolation.java");
		assertEquals(2, violations.size());

		assertEquals(10, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.toList()' instead of '.collect(Collectors.toList())'.", violations.get(0).getMessage());
		assertEquals(16, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.toList()' instead of '.collect(Collectors.toUnmodifiableList())'.", violations.get(1).getMessage());
	}

	@Test
	public void testGetFirstAndGetLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java");
		assertEquals(2, violations.size());

		assertEquals(7, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use '.getLast()' instead of '.get(size() - 1)'.", violations.getFirst().getMessage());

		assertEquals(11, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(1).getMessage());
	}

	@Test
	public void testIndexOfContainsViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiIndexOfViolation.java");
		assertEquals(12, violations.size());

		var i = 0;
		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '.indexOf(...) == -1'.", violations.get(i++).getMessage());
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '.indexOf(...) >= 0'.", violations.get(i++).getMessage());
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '.indexOf(...) > -1'.", violations.get(i++).getMessage());
		assertEquals(20, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '.indexOf(...) <= -1'.", violations.get(i++).getMessage());
		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '.indexOf(...) < 0'.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '.indexOf(...) != -1'.", violations.get(i++).getMessage());
		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '-1 == .indexOf(...)'.", violations.get(i++).getMessage());
		assertEquals(40, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '-1 >= .indexOf(...)'.", violations.get(i++).getMessage());
		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '-1 < .indexOf(...)'.", violations.get(i++).getMessage());
		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '-1 != .indexOf(...)'.", violations.get(i++).getMessage());
		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.contains(...)' instead of '0 > .indexOf(...)'.", violations.get(i++).getMessage());
		assertEquals(60, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.contains(...)' instead of '0 <= .indexOf(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testIsEmptyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiIsEmptyViolation.java");
		assertEquals(15, violations.size());

		var i = 0;
		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.length() == 0'.", violations.get(i++).getMessage());
		assertEquals(12, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.length() == 0'.", violations.get(i++).getMessage());
		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '.length() > 0'.", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '1 > .size()'.", violations.get(i++).getMessage());
		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '1 <= .size()'.", violations.get(i++).getMessage());
		assertEquals(32, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.size() == 0'.", violations.get(i++).getMessage());
		assertEquals(37, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '.size() >= 1'.", violations.get(i++).getMessage());
		assertEquals(42, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '.size() > 0'.", violations.get(i++).getMessage());
		assertEquals(47, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.size() < 1'.", violations.get(i++).getMessage());
		assertEquals(52, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.size() <= 0'.", violations.get(i++).getMessage());
		assertEquals(57, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '.size() != 0'.", violations.get(i++).getMessage());
		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '0 == .size()'.", violations.get(i++).getMessage());
		assertEquals(67, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '0 >= .size()'.", violations.get(i++).getMessage());
		assertEquals(72, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '0 < .size()'.", violations.get(i++).getMessage());
		assertEquals(77, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.!isEmpty()' instead of '0 != .size()'.", violations.get(i++).getMessage());
	}

	@Test
	public void testIsEmptyViolationMinSdk24SkipsCharSequence() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiIsEmptyViolation.java", "minSdk", "24"
		);
		assertEquals(14, violations.size());
		assertEquals(12, violations.getFirst().getLine());
	}

	@Test
	public void testMapChainViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiMapChainViolation.java");
		assertEquals(2, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.containsKey(...)' instead of '.keySet().contains(...)'.", violations.get(0).getMessage());
		assertEquals(12, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.containsValue(...)' instead of '.values().contains(...)'.", violations.get(1).getMessage());
	}

	@Test
	public void testMinSdkAllowsArraysAsList() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiArraysAsListViolation.java", "minSdk", "30"
		);
		assertEquals(3, violations.size());
	}

	@Test
	public void testMinSdkAllowsCollectionsCopyOf() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCopyOfViolation.java", "minSdk", "31"
		);
		assertEquals(4, violations.size());

		var i = 0;
		assertEquals(11, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.", violations.get(i++).getMessage());
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.copyOf(...)' instead of 'Collections.unmodifiableList(...)'.", violations.get(i++).getMessage());
		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.copyOf(...)' instead of 'Collections.unmodifiableMap(...)'.", violations.get(i++).getMessage());
		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.copyOf(...)' instead of 'Collections.unmodifiableSet(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testMinSdkAllowsCollectionsFactory() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsEmptyViolation.java", "minSdk", "30"
		);
		assertEquals(6, violations.size());

		var i = 0;
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of()' instead of 'Collections.emptyList()'.", violations.get(i++).getMessage());
		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.of()' instead of 'Collections.emptyMap()'.", violations.get(i++).getMessage());
		assertEquals(18, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.of()' instead of 'Collections.emptySet()'.", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Set.of(...)' instead of 'Collections.singleton(...)'.", violations.get(i++).getMessage());
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Collections.singletonList(...)'.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Map.of(...)' instead of 'Collections.singletonMap(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testMinSdkAllowsCollectionsSort() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsSortViolation.java", "minSdk", "24"
		);
		assertEquals(2, violations.size());

		assertEquals(9, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.sort(...)' instead of 'Collections.sort(...)'.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.sort(...)' instead of 'Collections.sort(...)'.", violations.get(1).getMessage());
	}

	@Test
	public void testMinSdkAllowsFormatted() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiStringFormatViolation.java", "minSdk", "34"
		);
		assertEquals(6, violations.size());
	}

	@Test
	public void testMinSdkAllowsGeneratorToArray() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiToArrayViolation.java", "minSdk", "33"
		);
		assertEquals(4, violations.size());
	}

	@Test
	public void testMinSdkAllowsIsBlank() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiTrimIsBlankViolation.java", "minSdk", "33"
		);
		assertEquals(13, violations.size());

		var i = 0;
		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '1 > .trim().length()'.", violations.get(i++).getMessage());
		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '1 <= .trim().length()'.", violations.get(i++).getMessage());
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '.trim().isEmpty()'.", violations.get(i++).getMessage());
		assertEquals(20, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '.trim().length() == 0'.", violations.get(i++).getMessage());
		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '.trim().length() >= 1'.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '.trim().length() > 0'.", violations.get(i++).getMessage());
		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '.trim().length() <= 0'.", violations.get(i++).getMessage());
		assertEquals(40, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '.trim().length() < 1'.", violations.get(i++).getMessage());
		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '.trim().length() != 0'.", violations.get(i++).getMessage());
		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '0 == .trim().length()'.", violations.get(i++).getMessage());
		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.isBlank()' instead of '0 >= .trim().length()'.", violations.get(i++).getMessage());
		assertEquals(60, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '0 < .trim().length()'.", violations.get(i++).getMessage());
		assertEquals(65, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '!.isBlank()' instead of '0 != .trim().length()'.", violations.get(i++).getMessage());
	}

	@Test
	public void testMinSdkAllowsStreamForEach() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiStreamViolation.java", "minSdk", "24"
		);
		assertEquals(3, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.size()' instead of '.stream().count()'.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.", violations.get(1).getMessage());
		assertEquals(16, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use '.forEach(...)' instead of '.stream().forEach(...)'.", violations.get(2).getMessage());
	}

	@Test
	public void testMinSdkSuppressesArraysAsList() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiArraysAsListViolation.java", "minSdk", "29"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java", "minSdk", "34"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesCollectionsCopyOf() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCopyOfViolation.java", "minSdk", "30"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesCollectionsFactory() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsEmptyViolation.java", "minSdk", "29"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesCollectionsSort() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiCollectionsSortViolation.java", "minSdk", "23"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesFormatted() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiStringFormatViolation.java", "minSdk", "33"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesGeneratorToArray() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiToArrayViolation.java", "minSdk", "32"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesIsBlank() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiTrimIsBlankViolation.java", "minSdk", "32"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesRemoveCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiRemoveViolation.java", "minSdk", "34"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testMinSdkSuppressesStreamForEach() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiStreamViolation.java", "minSdk", "23"
		);
		// stream().count() and stream().findFirst().isPresent() have no minSdk gate, only forEach does
		assertEquals(2, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.size()' instead of '.stream().count()'.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.", violations.get(1).getMessage());
	}

	@Test
	public void testReflectionClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiReflectionClean.java").isEmpty());
	}

	@Test
	public void testReflectionViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiReflectionViolation.java");
		assertEquals(5, violations.size());

		// bare chained call: receiver unresolvable, best-effort flags it
		assertEquals(9, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.getFirst().getMessage());

		// chained call resolved via reflection: Collections.synchronizedList returns List
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(1).getMessage());

		assertEquals(22, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(2).getMessage());

		assertEquals(26, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(3).getMessage());

		// var-typed local: unresolvable, best-effort flags getLast
		assertEquals(31, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Use '.getLast()' instead of '.get(size() - 1)'.", violations.get(4).getMessage());
	}

	@Test
	public void testRemoveFirstAndRemoveLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiRemoveViolation.java");
		assertEquals(2, violations.size());

		assertEquals(7, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use '.removeFirst()' instead of '.remove(0)'.", violations.getFirst().getMessage());

		assertEquals(11, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.removeLast()' instead of '.remove(size() - 1)'.", violations.get(1).getMessage());
	}

	@Test
	public void testStandaloneArraysAsListViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiArraysAsListViolation.java");
		assertEquals(3, violations.size());

		var i = 0;
		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Arrays.asList(...)'.", violations.get(i++).getMessage());
		assertEquals(11, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of()' instead of 'Arrays.asList()'.", violations.get(i++).getMessage());
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'List.of(...)' instead of 'Arrays.asList(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testStreamViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiStreamViolation.java");
		assertEquals(3, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.size()' instead of '.stream().count()'.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.", violations.get(1).getMessage());
		assertEquals(16, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use '.forEach(...)' instead of '.stream().forEach(...)'.", violations.get(2).getMessage());
	}

	@Test
	public void testStringFormatViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiStringFormatViolation.java");
		assertEquals(6, violations.size());

		var i = 0;
		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.formatted(...)' instead of 'String.format(...)'.", violations.get(i++).getMessage());
		assertEquals(9, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'the value directly' instead of 'String.format(value)'.", violations.get(i++).getMessage());
		assertEquals(13, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'the value directly' instead of 'String.format(value)'.", violations.get(i++).getMessage());
		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'the value directly' instead of 'String.format(value)'.", violations.get(i++).getMessage());
		assertEquals(21, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'the value directly' instead of 'String.format(value)'.", violations.get(i++).getMessage());
		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use '.formatted(...)' instead of 'String.format(...)'.", violations.get(i++).getMessage());
	}

	@Test
	public void testStringMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiStringMethodViolation.java");
		assertEquals(2, violations.size());

		assertEquals(5, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use '.isEmpty()' instead of '.equals(\"\")'.", violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use '.replace(...)' instead of '.replaceAll(...)'.", violations.get(1).getMessage());
	}

	@Test
	public void testToArrayNewZeroViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiToArrayViolation.java");
		assertEquals(4, violations.size());

		var i = 0;
		assertEquals(9, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Integer[]::new' instead of 'new Integer[0]'.", violations.get(i++).getMessage());
		assertEquals(13, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'String[]::new' instead of 'new String[0]'.", violations.get(i++).getMessage());
		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'java.lang.String[]::new' instead of 'new java.lang.String[0]'.", violations.get(i++).getMessage());
		assertEquals(21, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'String[]::new' instead of 'new String[0]'.", violations.get(i++).getMessage());
	}
}