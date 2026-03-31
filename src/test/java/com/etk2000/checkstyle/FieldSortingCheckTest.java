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
		assertEquals(6, violations.get(0).getLine());
		assertEquals("Field 'plain' (type 'int') must appear before 'name' (type 'String').", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals("Field 'vector' (type 'int[]') must appear before 'matrix' (type 'int[][]').", violations.get(1).getMessage());
		assertEquals(12, violations.get(2).getLine());
		assertEquals("Field 'scalar' (type 'int') must appear before 'vector' (type 'int[]').", violations.get(2).getMessage());
		assertEquals(17, violations.get(3).getLine());
		assertEquals("Field 'letter' (type 'char') must appear before 'values' (type 'double[]').", violations.get(3).getMessage());
	}

	@Test
	public void testChunkViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingChunkViolation.java");
		assertEquals(2, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals("Field 'finalNoValue' (final without inline value) must appear before non-final fields.", violations.get(0).getMessage());
		assertEquals(6, violations.get(1).getLine());
		assertEquals("Field 'finalWithValue' (final with inline value) must appear before final without inline value fields.", violations.get(1).getMessage());
	}

	@Test
	public void testCleanFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingClean.java").isEmpty());
	}

	@Test
	public void testDependencyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingDependencyViolation.java");
		assertEquals(2, violations.size());
		// ALPHA should come before DERIVED (normal name violation, no dependency)
		assertEquals(7, violations.get(0).getLine());
		assertEquals("Field 'ALPHA' must appear before 'DERIVED' (alphabetical order, same type).", violations.get(0).getMessage());
		// beta references alpha via this.alpha, but alpha is declared after beta
		assertEquals(12, violations.get(1).getLine());
		assertEquals("Field 'beta' references 'alpha' which should be declared before it.", violations.get(1).getMessage());
	}

	@Test
	public void testNameViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingNameViolation.java");
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
		assertEquals("Field 'alpha' must appear before 'zebra' (alphabetical order, same type).", violations.getFirst().getMessage());
	}

	@Test
	public void testTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldSortingTypeViolation.java");
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
		assertEquals("Field 'count' (type 'int') must appear before 'name' (type 'String').", violations.getFirst().getMessage());
	}
}