package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FieldSortingCheckTest {
	private static final String DIR = "fieldsorting/";

	@Test
	public void testArrayAndMultidimensionalViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingArrayViolation.java");
		assertEquals(4, violations.size());
		assertEquals(6, violations.get(0).getLine());  // int plain after String
		assertEquals(11, violations.get(1).getLine()); // int[] after int[][]
		assertEquals(12, violations.get(2).getLine()); // int after int[]
		assertEquals(17, violations.get(3).getLine()); // char after double[]
	}

	@Test
	public void testChunkViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingChunkViolation.java");
		assertEquals(2, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(6, violations.get(1).getLine());
	}

	@Test
	public void testCleanFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingClean.java").isEmpty());
	}

	@Test
	public void testNameViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingNameViolation.java");
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("alpha"));
	}

	@Test
	public void testTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingTypeViolation.java");
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
	}
}