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
		// first case: anon class not on opening AND closing brace not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(10, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing brace not on closing paren line
		assertEquals(18, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
	}

	@Test
	public void testChainedConstructorViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallChainedConstructorViolation.java");
		assertEquals(2, violations.size());
		// first case: chained constructor not on opening paren line
		assertEquals(7, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		// second case: closing paren on chain end line
		assertEquals(17, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing"));
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
		assertEquals(11, violations.get(1).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("closing"));
	}

	@Test
	public void testConstructorViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallConstructorViolation.java");
		assertEquals(3, violations.size());
		// first case: constructor not on opening AND closing paren not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(9, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing paren not on closing paren line
		assertEquals(16, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
	}

	@Test
	public void testDefinitionViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallDefinition.java");
		assertEquals(4, violations.size());
		assertEquals(10, violations.get(0).getLine());
		assertEquals(17, violations.get(1).getLine());
		assertEquals(20, violations.get(2).getLine());
		assertEquals(27, violations.get(3).getLine());
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
		// first case: lambda not on opening AND closing brace not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(9, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing brace not on closing paren line
		assertEquals(16, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
		// third case: braceless lambda closing paren on body line
		assertEquals(21, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("closing"));
	}

	@Test
	public void testOpeningParenViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallOpeningViolation.java");
		assertEquals(2, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertEquals(10, violations.get(1).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("opening"));
	}

	@Test
	public void testPostDelayedViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallPostDelayedViolation.java");
		assertEquals(4, violations.size());
		// first case: lambda not on opening AND delay not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(10, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: delay not on closing paren line
		assertEquals(18, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
		// third case: lambda not on opening only (delay IS on closing)
		assertEquals(22, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("opening paren"));
	}

	@Test
	public void testResourceIdInlineBlockViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallResourceIdViolation.java");
		assertEquals(6, violations.size());
		// first case: R.xxx+lambda not on opening AND closing brace not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(9, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing brace not on closing paren line
		assertEquals(16, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
		// third case: braceless lambda closing paren on body line
		assertEquals(21, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("closing"));
		// fourth case: android.R.xxx+lambda not on opening AND closing brace not on closing
		assertEquals(25, violations.get(4).getLine());
		assertTrue(violations.get(4).getMessage().contains("opening paren"));
		assertEquals(29, violations.get(5).getLine());
		assertTrue(violations.get(5).getMessage().contains("closing paren"));
	}

	@Test
	public void testSharedLineViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSharedLineViolation.java");
		assertEquals(4, violations.size());
		assertEquals(6, violations.get(0).getLine());
		assertEquals(14, violations.get(1).getLine());
		// definition shared-line violations
		assertEquals(19, violations.get(2).getLine());
		assertEquals(26, violations.get(3).getLine());
		for (var v : violations)
			assertTrue(v.getMessage().contains("own line"));
	}

	@Test
	public void testSpecialMethodViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSpecialMethodViolation.java");
		assertEquals(3, violations.size());
		// first case: List.of not on opening AND closing paren not on closing
		assertEquals(7, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(11, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing paren not on closing paren line
		assertEquals(18, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
	}

	@Test
	public void testSuperViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallSuperViolation.java");
		assertEquals(3, violations.size());
		// closing violation
		assertEquals(35, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("closing"));
		// opening violation
		assertEquals(41, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("opening"));
		// shared-line violation
		assertEquals(51, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("own line"));
	}

	@Test
	public void testTernaryPositionViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallTernaryPositionViolation.java");
		assertEquals(4, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("?"));
		assertEquals(14, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("?"));
		assertEquals(21, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains(":"));
		assertEquals(29, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains(":"));
	}

	@Test
	public void testTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallTernaryViolation.java");
		assertEquals(3, violations.size());
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("Ternary"));
		assertEquals(15, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing"));
		assertEquals(20, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing"));
	}

	@Test
	public void testThisInlineBlockViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallThisViolation.java");
		assertEquals(4, violations.size());
		// first case: this+lambda not on opening AND closing brace not on closing
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("opening paren"));
		assertEquals(9, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing paren"));
		// second case: closing brace not on closing paren line
		assertEquals(16, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing paren"));
		// third case: braceless lambda closing paren on body line
		assertEquals(21, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("closing"));
	}

	@Test
	public void testThisTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(MultilineCallFormattingCheck.class, DIR + "InputMultilineCallThisTernaryViolation.java");
		assertEquals(4, violations.size());
		// first case: this + ternary not on opening
		assertEquals(5, violations.get(0).getLine());
		assertTrue(violations.get(0).getMessage().contains("Ternary"));
		// second case: this + ternary on closing
		assertEquals(15, violations.get(1).getLine());
		assertTrue(violations.get(1).getMessage().contains("closing"));
		// third case: this + single-line ternary wrong close
		assertEquals(20, violations.get(2).getLine());
		assertTrue(violations.get(2).getMessage().contains("closing"));
		// fourth case: R.xxx + ternary not on opening
		assertEquals(24, violations.get(3).getLine());
		assertTrue(violations.get(3).getMessage().contains("Ternary"));
	}
}