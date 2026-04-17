package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.junit.jupiter.api.Test;

public class NoEnumTrailingSemicolonCheckTest {
	private static final String DIR = "enumsemicolon/";
	private static final String MSG = "No trailing semicolon in enum without body declarations.";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, DIR + "InputEnumSemicolonClean.java").isEmpty());
	}

	@Test
	public void testCrossCheckAnnotationOwnLine() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "annotationownline/InputAnnotationOwnLineClean.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "annotationownline/InputAnnotationOwnLineViolation.java").isEmpty());
	}

	@Test
	public void testCrossCheckClassStructure() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "classstructure/InputClassStructureEnum.java").isEmpty());
	}

	@Test
	public void testCrossCheckFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "fieldsorting/InputFieldSortingClean.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "fieldsorting/InputFieldSortingEnumConstantViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, "fieldsorting/InputFieldSortingEnumSameLineViolation.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(NoEnumTrailingSemicolonCheck.class, DIR + "InputEnumSemicolonViolation.java");
		assertEquals(13, violations.size());

		// V1: single constant
		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(MSG, violations.get(0).getMessage());

		// V2: multiple constants
		assertEquals(14, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(MSG, violations.get(1).getMessage());

		// V3: no constants
		assertEquals(19, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals(MSG, violations.get(2).getMessage());

		// V4: constant with body
		assertEquals(29, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals(MSG, violations.get(3).getMessage());

		// V5: nested enum
		assertEquals(35, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals(MSG, violations.get(4).getMessage());

		// V6: multiline
		assertEquals(43, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals(MSG, violations.get(5).getMessage());

		// V7: constructor args, no body
		assertEquals(49, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals(MSG, violations.get(6).getMessage());

		// V8: implements interface
		assertEquals(54, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals(MSG, violations.get(7).getMessage());

		// V9: annotated enum
		assertEquals(60, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals(MSG, violations.get(8).getMessage());

		// V10: semicolon on own line
		assertEquals(66, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals(MSG, violations.get(9).getMessage());

		// V11: block comment between semicolon and brace
		assertEquals(71, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals(MSG, violations.get(10).getMessage());

		// V12: enum-in-enum, inner violation
		assertEquals(80, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals(MSG, violations.get(11).getMessage());

		// V13: annotated constant, no body
		assertEquals(87, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals(MSG, violations.get(12).getMessage());
	}
}