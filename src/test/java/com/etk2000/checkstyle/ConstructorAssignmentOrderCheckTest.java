package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class ConstructorAssignmentOrderCheckTest {
	private static final String DIR = "constructorassign/";

	@Test
	public void testCleanAssignmentOrder() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignClean.java");
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testDependencyViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignDependencyViolation.java");
		assertEquals(2, violations.size());

		// dependency violation in group SIMPLE
		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Assignment 'this.beta' references 'this.alpha' which should be assigned before it.", violations.get(0).getMessage());

		// dependency violation in var sub-group
		assertEquals(18, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' references 'this.beta' which should be assigned before it.", violations.get(1).getMessage());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ConstructorAssignmentOrderCheck.class, DIR + "InputConstructorAssignViolation.java");
		assertEquals(7, violations.size());

		// V1: alphabetical violation in group 1
		assertEquals(9, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).", violations.get(0).getMessage());

		// V2: group 2 before group 1
		assertEquals(25, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Simple assignment 'this.alpha' must appear before multi-line assignments.", violations.get(1).getMessage());

		// V3: alphabetical violation in group 2
		assertEquals(40, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).", violations.get(2).getMessage());

		// V4: group 3 before group 1
		assertEquals(56, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Assignment 'this.beta' must appear before variable-dependent assignments.", violations.get(3).getMessage());

		// V5: group 3 before group 2
		assertEquals(68, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' must appear before variable-dependent assignments.", violations.get(4).getMessage());

		// V6: alphabetical violation within var sub-group
		assertEquals(84, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).", violations.get(5).getMessage());

		// V7: later var sub-group before earlier sub-group
		assertEquals(96, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Assignment 'this.alpha' must appear before 'this.beta' (variable declaration order).", violations.get(6).getMessage());
	}
}