package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferSpecificApiCheckTest {
	private static final String DIR = "specificapi/";

	@Test
	public void testAssertViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiAssertViolation.java");
		assertEquals(33, violations.size());

		var i = 0;

		// assertEquals 2-arg
		assertEquals(10, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(14, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(18, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(26, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(30, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());

		// assertEquals 3-arg
		assertEquals(34, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(38, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(42, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(46, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(50, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(54, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());

		// assertNotEquals 2-arg
		assertEquals(58, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(62, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(66, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(70, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(74, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(78, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());

		// assertNotEquals 3-arg
		assertEquals(82, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(86, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.", violations.get(i++).getMessage());
		assertEquals(90, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(94, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(98, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());
		assertEquals(102, violations.get(i).getLine());
		assertEquals("Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.", violations.get(i++).getMessage());

		// assertNotSame 2-arg + 3-arg
		assertEquals(106, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(110, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(114, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(118, violations.get(i).getLine());
		assertEquals("Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.", violations.get(i++).getMessage());

		// assertSame 2-arg + 3-arg
		assertEquals(122, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(126, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(130, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());
		assertEquals(134, violations.get(i).getLine());
		assertEquals("Use 'assertNull' instead of 'assertSame' with a 'null' literal.", violations.get(i++).getMessage());

		// qualified
		assertEquals(138, violations.get(i).getLine());
		assertEquals("Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.", violations.get(i++).getMessage());
	}

	@Test
	public void testCleanApiUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiClean.java").isEmpty());
	}

	@Test
	public void testCollectToListViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiToListViolation.java");
		assertEquals(1, violations.size());
		assertEquals("Use '.toList()' instead of '.collect(Collectors.toList())'.", violations.getFirst().getMessage());
	}

	@Test
	public void testGetFirstAndGetLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java");
		assertEquals(2, violations.size());

		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Use '.getLast()' instead of '.get(size() - 1)'.", violations.getFirst().getMessage());

		assertEquals(11, violations.get(1).getLine());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(1).getMessage());
	}

	@Test
	public void testIsEmptyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiIsEmptyViolation.java");
		assertEquals(13, violations.size());

		var i = 0;
		assertEquals("Use '.isEmpty()' instead of '.length() == 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '.length() > 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '1 > .size()'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '1 <= .size()'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '.size() == 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '.size() >= 1'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '.size() > 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '.size() < 1'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '.size() <= 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '.size() != 0'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '0 == .size()'.", violations.get(i++).getMessage());
		assertEquals("Use '.isEmpty()' instead of '0 >= .size()'.", violations.get(i++).getMessage());
		assertEquals("Use '.!isEmpty()' instead of '0 < .size()'.", violations.get(i++).getMessage());
	}

	@Test
	public void testMinSdkSuppressesCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java", "minSdk", "34"
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
	public void testReflectionClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiReflectionClean.java").isEmpty());
	}

	@Test
	public void testReflectionViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiReflectionViolation.java");
		assertEquals(5, violations.size());

		// bare chained call: receiver unresolvable, best-effort flags it
		assertEquals(9, violations.getFirst().getLine());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.getFirst().getMessage());

		// chained call resolved via reflection: Collections.unmodifiableList returns List
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(1).getMessage());

		// list local: List has getFirst
		assertEquals(22, violations.get(2).getLine());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(2).getMessage());

		// list param: List has getFirst
		assertEquals(26, violations.get(3).getLine());
		assertEquals("Use '.getFirst()' instead of '.get(0)'.", violations.get(3).getMessage());

		// var-typed local: unresolvable, best-effort flags getLast
		assertEquals(31, violations.get(4).getLine());
		assertEquals("Use '.getLast()' instead of '.get(size() - 1)'.", violations.get(4).getMessage());
	}

	@Test
	public void testRemoveFirstAndRemoveLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiRemoveViolation.java");
		assertEquals(2, violations.size());

		assertEquals(7, violations.getFirst().getLine());
		assertEquals("Use '.removeFirst()' instead of '.remove(0)'.", violations.getFirst().getMessage());

		assertEquals(11, violations.get(1).getLine());
		assertEquals("Use '.removeLast()' instead of '.remove(size() - 1)'.", violations.get(1).getMessage());
	}
}