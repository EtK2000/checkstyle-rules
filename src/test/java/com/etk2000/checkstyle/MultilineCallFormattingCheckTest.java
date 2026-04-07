package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MultilineCallFormattingCheckTest {
	private static final String DIR = "multilinecall/";

	@Test
	public void testAnonClassViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallAnonClassViolation.java");
		assertEquals(3, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(1).getMessage());
		assertEquals(18, violations.get(2).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(2).getMessage());
	}

	@Test
	public void testChainedConstructorViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallChainedConstructorViolation.java");
		assertEquals(2, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(1).getMessage());
	}

	@Test
	public void testCleanCalls() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallClean.java").isEmpty());
	}

	@Test
	public void testClosingParenViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallClosingViolation.java");
		assertEquals(2, violations.size());
		assertEquals(8, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(1).getMessage());
	}

	@Test
	public void testConstructorViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallConstructorViolation.java");
		assertEquals(3, violations.size());
		assertEquals(12, violations.get(0).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(0).getMessage());
		assertEquals(16, violations.get(1).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(1).getMessage());
		assertEquals(20, violations.get(2).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(2).getMessage());
	}

	@Test
	public void testDefinitionViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallDefinition.java");
		assertEquals(4, violations.size());
		assertEquals(4, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(1).getMessage());
		assertEquals(22, violations.get(2).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(2).getMessage());
		assertEquals(25, violations.get(3).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(3).getMessage());
	}

	@Test
	public void testGetQuantityStringNotContextIsNotInlineBlock() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallGetQuantityStringNotContext.java").isEmpty());
	}

	@Test
	public void testGetStringNotContextIsNotInlineBlock() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallGetStringNotContext.java").isEmpty());
	}

	@Test
	public void testLambdaViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallLambdaViolation.java");
		assertEquals(4, violations.size());
		assertEquals(8, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(0).getMessage());
		assertEquals(15, violations.get(1).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(1).getMessage());
		assertEquals(19, violations.get(2).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(2).getMessage());
		assertEquals(23, violations.get(3).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(3).getMessage());
	}

	@Test
	public void testMethodCallArgViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallMethodCallViolation.java");
		assertEquals(14, violations.size());
		assertEquals(12, violations.get(0).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(0).getMessage());
		assertEquals(19, violations.get(1).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(1).getMessage());
		assertEquals(36, violations.get(2).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(2).getMessage());
		assertEquals(44, violations.get(3).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(3).getMessage());
		assertEquals(48, violations.get(4).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(4).getMessage());
		assertEquals(56, violations.get(5).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(5).getMessage());
		assertEquals(59, violations.get(6).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(6).getMessage());
		assertEquals(66, violations.get(7).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(7).getMessage());
		assertEquals(73, violations.get(8).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(8).getMessage());
		assertEquals(81, violations.get(9).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(9).getMessage());
		assertEquals(85, violations.get(10).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(10).getMessage());
		assertEquals(93, violations.get(11).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(11).getMessage());
		assertEquals(96, violations.get(12).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(12).getMessage());
		assertEquals(103, violations.get(13).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(13).getMessage());
	}

	@Test
	public void testOpeningParenViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallOpeningViolation.java");
		assertEquals(2, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(1).getMessage());
	}

	@Test
	public void testPostDelayedViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallPostDelayedViolation.java");
		assertEquals(4, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(1).getMessage());
		assertEquals(20, violations.get(2).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(2).getMessage());
		assertEquals(25, violations.get(3).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(3).getMessage());
	}

	@Test
	public void testResourceIdInlineBlockViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallResourceIdViolation.java");
		assertEquals(6, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(0).getMessage());
		assertEquals(11, violations.get(1).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(1).getMessage());
		assertEquals(19, violations.get(2).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(2).getMessage());
		assertEquals(26, violations.get(3).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(3).getMessage());
		assertEquals(30, violations.get(4).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(4).getMessage());
		assertEquals(34, violations.get(5).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(5).getMessage());
	}

	@Test
	public void testSharedLineViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSharedLineViolation.java");
		assertEquals(4, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(12, violations.get(1).getLine());
		assertEquals(18, violations.get(2).getLine());
		assertEquals(26, violations.get(3).getLine());
		for (var v : violations)
			assertEquals("In multiline calls/signatures, each argument must be on its own line.", v.getMessage());
	}

	@Test
	public void testSpecialMethodViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSpecialMethodViolation.java");
		assertEquals(5, violations.size());
		assertEquals(10, violations.get(0).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(0).getMessage());
		assertEquals(14, violations.get(1).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(1).getMessage());
		assertEquals(18, violations.get(2).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(2).getMessage());
		assertEquals(22, violations.get(3).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(3).getMessage());
		assertEquals(26, violations.get(4).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(4).getMessage());
	}

	@Test
	public void testSuperViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSuperViolation.java");
		assertEquals(3, violations.size());
		assertEquals(35, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(0).getMessage());
		assertEquals(41, violations.get(1).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the opening paren line.", violations.get(1).getMessage());
		assertEquals(51, violations.get(2).getLine());
		assertEquals("In multiline calls/signatures, each argument must be on its own line.", violations.get(2).getMessage());
	}

	@Test
	public void testTernaryPositionViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallTernaryPositionViolation.java");
		assertEquals(4, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals("Ternary ':' must be on the line immediately after the true branch.", violations.get(0).getMessage());
		assertEquals(14, violations.get(1).getLine());
		assertEquals("Ternary ':' must be on the line immediately after the true branch.", violations.get(1).getMessage());
		assertEquals(22, violations.get(2).getLine());
		assertEquals("Ternary '?' must be on the line immediately after the condition.", violations.get(2).getMessage());
		assertEquals(31, violations.get(3).getLine());
		assertEquals("Ternary '?' must be on the line immediately after the condition.", violations.get(3).getMessage());
	}

	@Test
	public void testTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallTernaryViolation.java");
		assertEquals(3, violations.size());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Single-line ternary argument: closing paren must be on the same line.", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals("Ternary argument: condition must be on the opening paren line.", violations.get(1).getMessage());
		assertEquals(23, violations.get(2).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(2).getMessage());
	}

	@Test
	public void testThisInlineBlockViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallThisViolation.java");
		assertEquals(4, violations.size());
		assertEquals(11, violations.get(0).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(0).getMessage());
		assertEquals(18, violations.get(1).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(1).getMessage());
		assertEquals(22, violations.get(2).getLine());
		assertEquals("Inline block argument: must be on the opening paren line.", violations.get(2).getMessage());
		assertEquals(26, violations.get(3).getLine());
		assertEquals("Inline block argument: closing brace/paren must be on the closing paren line.", violations.get(3).getMessage());
	}

	@Test
	public void testThisTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallThisTernaryViolation.java");
		assertEquals(4, violations.size());
		assertEquals(8, violations.get(0).getLine());
		assertEquals("Ternary argument: condition must be on the opening paren line.", violations.get(0).getMessage());
		assertEquals(17, violations.get(1).getLine());
		assertEquals("Single-line ternary argument: closing paren must be on the same line.", violations.get(1).getMessage());
		assertEquals(21, violations.get(2).getLine());
		assertEquals("Ternary argument: condition must be on the opening paren line.", violations.get(2).getMessage());
		assertEquals(31, violations.get(3).getLine());
		assertEquals("In multiline calls/signatures, no arguments on the closing paren line.", violations.get(3).getMessage());
	}
}