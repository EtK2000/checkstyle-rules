package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.Test;

public class SwitchCaseOrderCheckTest {
	private static final String DIR = "switchorder/";

	@Test
	public void testCharLiteralViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderCharLiteralViolation.java");
		assertEquals(5, violations.size());
		assertEquals("Case 'CHAR_CONST' must appear before '0'.", violations.get(0).getMessage());
		assertEquals("Case 'A' must appear before 'b'.", violations.get(1).getMessage());
		assertEquals("Case 'a' must appear before 'z'.", violations.get(2).getMessage());
		assertEquals("Case '0' must appear before 'a'.", violations.get(3).getMessage());
		assertEquals("Case '0' must appear before 'A'.", violations.get(4).getMessage());
	}

	@Test
	public void testCleanSwitchOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderClean.java").isEmpty());
	}

	@Test
	public void testDefaultNotLast() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderDefaultNotLast.java");
		assertEquals(2, violations.size());
		assertEquals("Case 'default' must appear before '1'.", violations.get(0).getMessage());
		assertEquals("Case 'default' must appear before '1'.", violations.get(1).getMessage());
	}

	@Test
	public void testInternalOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderInternalViolation.java");
		assertEquals(5, violations.size());
		assertEquals("Label '2' must appear before '3'.", violations.get(0).getMessage());
		assertEquals("Label 'delta' must appear before 'gamma'.", violations.get(1).getMessage());
		assertEquals("Label '0' must appear before 'a'.", violations.get(2).getMessage());
		assertEquals("Label 'ALPHA' must appear before '100'.", violations.get(3).getMessage());
		assertEquals("Label '1' must appear before '3'.", violations.get(4).getMessage());
	}

	@Test
	public void testNumericEdgeCaseViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderNumericEdgeCases.java");
		assertEquals(6, violations.size());

		assertEquals(8, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Case '0b0001' must appear before '0b1010'.", violations.get(0).getMessage());

		assertEquals(17, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Case 'a' must appear before 'z'.", violations.get(1).getMessage());

		assertEquals(26, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Case '0x0A' must appear before '0xFF'.", violations.get(2).getMessage());

		assertEquals(35, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Case '10L' must appear before '100L'.", violations.get(3).getMessage());

		assertEquals(44, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Case '010' must appear before '017'.", violations.get(4).getMessage());

		assertEquals(53, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Case '999' must appear before '1_000'.", violations.get(5).getMessage());
	}

	@Test
	public void testSwitchOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(SwitchCaseOrderCheck.class, DIR + "InputSwitchOrderViolation.java");
		assertEquals(5, violations.size());
		assertEquals("Case 'alpha' must appear before 'beta'.", violations.get(0).getMessage());
		assertEquals("Case 'default' must appear before '1'.", violations.get(1).getMessage());
		assertEquals("Case '0123' must appear before 'abc'.", violations.get(2).getMessage());
		assertEquals("Case 'ALPHA' must appear before '100'.", violations.get(3).getMessage());
		assertEquals("Case '2' must appear before '10'.", violations.get(4).getMessage());
	}
}