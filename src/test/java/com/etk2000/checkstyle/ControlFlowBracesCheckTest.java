package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class ControlFlowBracesCheckTest {
	private static final String DIR = "controlflow/";

	@Test
	public void testCleanBraceUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowClean.java").isEmpty());
	}

	@Test
	public void testDoWhileTierViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowDoWhileTier.java");
		assertEquals(5, violations.size());

		final int[] lines = {11, 15, 19, 23, 28};
		for (var i = 0; i < lines.length; ++i) {
			assertEquals(lines[i], violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
			assertEquals("Do-while body must be on the do line.", violations.get(i).getMessage());
		}
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
		for (var v : violations) {
			assertEquals(SeverityLevel.ERROR, v.getSeverityLevel());
			assertEquals("Braceless control flow has multi-line body, add braces.", v.getMessage());
		}
	}

	@Test
	public void testNestedIndependentLevels() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowNested.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Braceless control flow has multi-line body, add braces.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Braceless control flow has multi-line body, add braces.", violations.get(1).getMessage());
	}

	@Test
	public void testOneLiners() throws Exception {
		final var violations = BaseCheckTest.runCheck(ControlFlowBracesCheck.class, DIR + "InputControlFlowOneLiner.java");
		assertEquals(15, violations.size());

		// tier 2 while on same line: simple body + simple while, dotted body, system.out, compound while
		final int[] tier2WhileSameLine = {8, 9, 10, 11};
		for (var i = 0; i < tier2WhileSameLine.length; ++i) {
			assertEquals(tier2WhileSameLine[i], violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
			assertEquals("Do-while while clause must be on its own line.", violations.get(i).getMessage());
		}

		// tier 3 as tier 2 (body on do line, while split)
		assertEquals(16, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(4).getMessage());
		assertEquals(19, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(5).getMessage());

		// tier 3 one-liner (body and while on do line: chained, new, complex compound, complex assign)
		final int[] tier3OneLiner = {25, 26, 27, 28};
		for (var i = 0; i < tier3OneLiner.length; ++i) {
			assertEquals(tier3OneLiner[i], violations.get(6 + i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(6 + i).getSeverityLevel());
			assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(6 + i).getMessage());
		}

		// else one-liner
		assertEquals(34, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(10).getMessage());

		// if/while/for/foreach one-liners
		final int[] otherOneLiners = {38, 39, 40, 42};
		for (var i = 0; i < otherOneLiners.length; ++i) {
			assertEquals(otherOneLiners[i], violations.get(11 + i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(11 + i).getSeverityLevel());
			assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(11 + i).getMessage());
		}
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
		for (var v : violations) {
			assertEquals(SeverityLevel.ERROR, v.getSeverityLevel());
			assertEquals("Single-line control flow body has unnecessary braces.", v.getMessage());
		}
	}
}