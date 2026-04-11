package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class InstanceofBeforeCastCheckTest {
	private static final String DIR = "instanceofbeforecast/";

	@Test
	public void testCleanInput() throws Exception {
		assertTrue(BaseCheckTest.runCheck(InstanceofBeforeCastCheck.class, DIR + "InputInstanceofBeforeCastClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(InstanceofBeforeCastCheck.class, DIR + "InputInstanceofBeforeCastViolation.java");
		assertEquals(6, violations.size());
		var i = 0;

		assertEquals(5, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		assertEquals(10, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());

		assertEquals(28, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());

		assertEquals(32, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());
	}
}