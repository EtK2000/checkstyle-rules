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
		assertEquals(6, violations.size());

		// tier1BodyOnOwnLine
		assertEquals(11, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Simple do-while must be on a single line.", violations.get(0).getMessage());

		// tier1WhileOnNextLine
		assertEquals(17, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Simple do-while must be on a single line.", violations.get(1).getMessage());

		assertEquals(20, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Simple do-while must be on a single line.", violations.get(2).getMessage());

		assertEquals(23, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Simple do-while must be on a single line.", violations.get(3).getMessage());

		// tier2BodyOnOwnLine
		assertEquals(28, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Do-while body must be on the do line.", violations.get(4).getMessage());

		assertEquals(33, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Do-while body must be on the do line.", violations.get(5).getMessage());
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
		assertEquals(14, violations.size());

		// tier 2 while on same line (dotted body + compound while boundary)
		assertEquals(8, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Do-while while clause must be on its own line.", violations.get(0).getMessage());
		assertEquals(9, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Do-while while clause must be on its own line.", violations.get(1).getMessage());
		assertEquals(10, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Do-while while clause must be on its own line.", violations.get(2).getMessage());

		// tier 3 as tier 2 (body on do line, while split)
		assertEquals(15, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(3).getMessage());
		assertEquals(18, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(4).getMessage());

		// tier 3 as tier 1 (all one line: chained, new, complex compound, complex assign)
		assertEquals(24, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(5).getMessage());
		assertEquals(25, violations.get(6).getLine());
		assertEquals(26, violations.get(7).getLine());
		assertEquals(27, violations.get(8).getLine());
		for (var i = 6; i <= 8; ++i) {
			assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
			assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(i).getMessage());
		}

		// else one-liner
		assertEquals(33, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(9).getMessage());

		// if/while/for/foreach one-liners
		assertEquals(37, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(10).getMessage());
		assertEquals(38, violations.get(11).getLine());
		assertEquals(39, violations.get(12).getLine());
		assertEquals(41, violations.get(13).getLine());
		for (var i = 11; i <= 13; ++i) {
			assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
			assertEquals("Control flow body must be on its own line, not a one-liner.", violations.get(i).getMessage());
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