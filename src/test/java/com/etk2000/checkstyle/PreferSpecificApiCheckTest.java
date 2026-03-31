package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferSpecificApiCheckTest {
	private static final String DIR = "specificapi/";

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
		assertEquals(11, violations.size());
		for (var v : violations)
			assertEquals("Use '.isEmpty()' instead of '.size() == 0' (or '!.isEmpty()' for '!= 0').", v.getMessage());
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