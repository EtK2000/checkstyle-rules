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
		assertEquals(7, violations.get(0).getLine());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(17, violations.get(2).getLine());
		assertEquals(21, violations.get(3).getLine());
		assertEquals(26, violations.get(4).getLine());
		assertEquals(30, violations.get(5).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("add braces"));
	}

	@Test
	public void testNestedIndependentLevels() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowNested.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals(13, violations.get(1).getLine());
	}

	@Test
	public void testOneLiners() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowOneLiner.java");
		assertEquals(6, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals(11, violations.get(1).getLine());
		assertEquals(12, violations.get(2).getLine());
		assertEquals(13, violations.get(3).getLine());
		assertEquals(15, violations.get(4).getLine());
		assertEquals(16, violations.get(5).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("on its own line"));
	}

	@Test
	public void testUnnecessaryBraces() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowUnnecessaryBraces.java");
		assertEquals(7, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(9, violations.get(1).getLine());
		assertEquals(13, violations.get(2).getLine());
		assertEquals(18, violations.get(3).getLine());
		assertEquals(22, violations.get(4).getLine());
		assertEquals(26, violations.get(5).getLine());
		assertEquals(29, violations.get(6).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("unnecessary braces"));
	}
}