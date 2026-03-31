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
		assertTrue(violations.getFirst().getMessage().contains("toList"));
	}

	@Test
	public void testGetFirstAndGetLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("getLast"));
		assertEquals(11, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("getFirst"));
	}

	@Test
	public void testIsEmptyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiIsEmptyViolation.java");
		assertEquals(11, violations.size());
		for (var v : violations)
			assertTrue(v.getMessage().contains("isEmpty"));
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
		assertTrue(violations.getFirst().getMessage().contains("getFirst"));

		// chained call resolved via reflection: Collections.unmodifiableList returns List
		assertEquals(13, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("getFirst"));

		// list local: List has getFirst
		assertEquals(22, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("getFirst"));

		// list param: List has getFirst
		assertEquals(26, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("getFirst"));

		// var-typed local: unresolvable, best-effort flags getLast
		assertEquals(31, violations.get(4).getLine());
		assertTrue(violations.get(4).getMessage().contains("getLast"));
	}

	@Test
	public void testRemoveFirstAndRemoveLastViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferSpecificApiCheck.class, DIR + "InputSpecificApiRemoveViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("removeFirst"));
		assertEquals(11, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("removeLast"));
	}
}