package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.Test;

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

		// cast before instanceof in &&
		assertEquals(6, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		// cast buried deeper in left operand
		assertEquals(12, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		// cast in variable assignment before instanceof
		assertEquals(18, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Move 'instanceof String' before the cast to 'String'.", violations.get(i++).getMessage());

		// cast in else block after positive instanceof
		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());

		// cast in then block after negated instanceof
		assertEquals(33, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());

		// cast in ternary false branch
		assertEquals(38, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Cast to 'String' is in a branch where 'instanceof String' is false, this will throw ClassCastException.", violations.get(i++).getMessage());
	}
}