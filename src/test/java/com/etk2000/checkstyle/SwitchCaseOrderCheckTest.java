package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SwitchCaseOrderCheckTest {
	private static final String DIR = "switchorder/";

	@Test
	public void testCharLiteralViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderCharLiteralViolation.java");
		assertEquals(5, violations.size());
	}

	@Test
	public void testCleanSwitchOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderClean.java").isEmpty());
	}

	@Test
	public void testDefaultNotLast() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderDefaultNotLast.java");
		assertEquals(2, violations.size());
		for (var violation : violations)
			assertTrue(violation.getMessage().contains("default"));
	}

	@Test
	public void testInternalOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderInternalViolation.java");
		assertEquals(5, violations.size());
	}

	@Test
	public void testNumericEdgeCaseViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderNumericEdgeCases.java");
		assertEquals(5, violations.size());
	}

	@Test
	public void testSwitchOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderViolation.java");
		assertEquals(5, violations.size());
	}
}