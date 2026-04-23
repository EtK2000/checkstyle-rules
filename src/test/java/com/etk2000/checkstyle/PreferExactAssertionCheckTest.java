package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.junit.jupiter.api.Test;

public class PreferExactAssertionCheckTest {
	private static final String DIR = "exactassertion/";
	private static final String MSG_PREFIX = "Use a dedicated assertion (e.g. 'assertEquals') instead of '";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferExactAssertionCheck.class, DIR + "InputPreferExactAssertionClean.java").isEmpty());
	}

	@Test
	public void testSpecificApiCleanCrossCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				"specificapi/InputSpecificApiClean.java"
		);
		assertEquals(2, violations.size());
		assertEquals(33, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '=='.", violations.get(0).getMessage());
		assertEquals(77, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '=='.", violations.get(1).getMessage());
	}

	@Test
	public void testSpecificApiViolationCrossCheck() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferExactAssertionCheck.class,
				"specificapi/InputSpecificApiAssertViolation.java"
		).isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferExactAssertionCheck.class, DIR + "InputPreferExactAssertionViolation.java");
		assertEquals(20, violations.size());

		var i = 0;

		assertEquals(9, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '=='.", violations.get(i++).getMessage());

		assertEquals(13, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(21, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '<='.", violations.get(i++).getMessage());

		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '<'.", violations.get(i++).getMessage());

		assertEquals(29, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '!='.", violations.get(i++).getMessage());

		assertEquals(33, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '=='.", violations.get(i++).getMessage());

		assertEquals(37, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>='.", violations.get(i++).getMessage());

		assertEquals(41, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '<='.", violations.get(i++).getMessage());

		assertEquals(49, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '<'.", violations.get(i++).getMessage());

		assertEquals(53, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '!='.", violations.get(i++).getMessage());

		assertEquals(57, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(61, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(65, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(69, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(73, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(77, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(81, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(85, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i).getMessage());
	}
}