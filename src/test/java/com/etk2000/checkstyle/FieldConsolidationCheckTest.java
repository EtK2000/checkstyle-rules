package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class FieldConsolidationCheckTest {
	private static final String DIR = "fieldconsolidation/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, DIR + "InputFieldConsolidationClean.java").isEmpty());
	}

	// Cross-check: FieldSortingCheck must not fire on our clean file
	@Test
	public void testCrossCheckOurCleanWithFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldConsolidationClean.java").isEmpty());
	}

	// Cross-check: FieldSortingCheck must not fire on our violation file
	@Test
	public void testCrossCheckOurViolationWithFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldConsolidationViolation.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldConsolidationCheck.class, DIR + "InputFieldConsolidationViolation.java");
		assertEquals(14, violations.size());
		var i = 0;

		assertEquals(13, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'second' and 'first' (type 'String') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'b' and 'a' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'c' and 'b' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		// @Deprecated vs @Deprecated() (paramless variants)
		assertEquals(34, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(42, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		// Same annotation with complex array param
		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		// Same annotation with named params in different order
		assertEquals(66, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(72, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'shared' and 'global' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		// Array type match (Java-style and C-style)
		assertEquals(78, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.", violations.get(i++).getMessage());

		// Compound array (Type[] name[]) matches Type[][]
		assertEquals(84, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(90, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'words' and 'names' (type 'List<String>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(96, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());
	}
}