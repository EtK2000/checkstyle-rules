package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ControlFlowBracesCheckTest {
	private static final String DIR = "controlflow/";

	@Test
	public void testCleanBraceUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowClean.java").isEmpty());
	}

	@Test
	public void testMissingBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowMissingBraces.java");
		assertEquals(6, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals(15, violations.get(1).getLine());
		assertEquals(19, violations.get(2).getLine());
		assertEquals(23, violations.get(3).getLine());
		assertEquals(28, violations.get(4).getLine());
		assertEquals(32, violations.get(5).getLine());
		for (var v : violations)
			assertEquals("Braceless control flow has multi-line body, add braces.", v.getMessage());
	}

	@Test
	public void testNestedIndependentLevels() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowNested.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals("Braceless control flow has multi-line body, add braces.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Braceless control flow has multi-line body, add braces.", violations.get(1).getMessage());
	}

	@Test
	public void testOneLiners() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowOneLiner.java");
		assertEquals(6, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(14, violations.get(2).getLine());
		assertEquals(15, violations.get(3).getLine());
		assertEquals(17, violations.get(4).getLine());
		assertEquals(18, violations.get(5).getLine());
		for (var v : violations)
			assertEquals("Control flow body must be on its own line, not a one-liner.", v.getMessage());
	}

	@Test
	public void testUnnecessaryBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowUnnecessaryBraces.java");
		assertEquals(7, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals(11, violations.get(1).getLine());
		assertEquals(15, violations.get(2).getLine());
		assertEquals(20, violations.get(3).getLine());
		assertEquals(24, violations.get(4).getLine());
		assertEquals(28, violations.get(5).getLine());
		assertEquals(31, violations.get(6).getLine());
		for (var v : violations)
			assertEquals("Single-line control flow body has unnecessary braces.", v.getMessage());
	}
}