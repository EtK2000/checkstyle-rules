package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.junit.jupiter.api.Test;

public class PreferExactAssertionCheckTest {
	private static final String DIR = "exactassertion/";
	private static final String MSG_PREFIX = "Use a dedicated assertion (e.g. 'assertEquals') instead of '";

	private static String instanceOfMsg(String original, String replacement) {
		return "Use '" + replacement + "' instead of '" + original + "' with 'instanceof'.";
	}

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
		assertEquals(33, violations.size());

		var i = 0;

		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '=='.", violations.get(i++).getMessage());

		assertEquals(21, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(25, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertFalse", "assertNotInstanceOf"), violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertFalse", "assertNotInstanceOf"), violations.get(i++).getMessage());

		assertEquals(40, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertFalse", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(45, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertFalse", "assertNotInstanceOf"), violations.get(i++).getMessage());

		assertEquals(50, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertFalse", "assertNotInstanceOf"), violations.get(i++).getMessage());

		assertEquals(54, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '<='.", violations.get(i++).getMessage());

		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '<'.", violations.get(i++).getMessage());

		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '!='.", violations.get(i++).getMessage());

		assertEquals(67, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(71, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '=='.", violations.get(i++).getMessage());

		assertEquals(75, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>='.", violations.get(i++).getMessage());

		assertEquals(79, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(84, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(89, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(94, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertNotInstanceOf"), violations.get(i++).getMessage());

		assertEquals(99, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(104, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(108, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '<='.", violations.get(i++).getMessage());

		assertEquals(112, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '<'.", violations.get(i++).getMessage());

		assertEquals(116, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '!='.", violations.get(i++).getMessage());

		assertEquals(120, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(124, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(128, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>'.", violations.get(i++).getMessage());

		assertEquals(132, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(136, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(140, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(145, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i++).getMessage());

		assertEquals(149, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertFalse' with '>='.", violations.get(i++).getMessage());

		assertEquals(153, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(MSG_PREFIX + "assertTrue' with '>'.", violations.get(i++).getMessage());

		assertEquals(158, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(instanceOfMsg("assertTrue", "assertInstanceOf"), violations.get(i).getMessage());
	}
}