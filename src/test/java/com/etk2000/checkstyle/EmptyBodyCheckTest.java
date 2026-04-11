package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class EmptyBodyCheckTest {
	private static final String DIR = "emptybody/";

	@Test
	public void testCleanBodies() throws Exception {
		assertTrue(BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyClean.java").isEmpty());
	}

	@Test
	public void testEmptyBodyViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyBodyViolation.java");
		assertEquals(10, violations.size());

		assertEquals(7, violations.get(0).getLine());
		assertEquals("Empty else body, remove it.", violations.get(0).getMessage());
		assertEquals(14, violations.get(1).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(1).getMessage());
		assertEquals(21, violations.get(2).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(2).getMessage());
		assertEquals(27, violations.get(3).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(3).getMessage());
		assertEquals(34, violations.get(4).getLine());
		assertEquals("Empty else body, remove it.", violations.get(4).getMessage());
		assertEquals(40, violations.get(5).getLine());
		assertEquals("Empty else body, remove it.", violations.get(5).getMessage());

		assertEquals(45, violations.get(6).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(6).getMessage());
		assertEquals(50, violations.get(7).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(7).getMessage());
		assertEquals(54, violations.get(8).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(8).getMessage());
		assertEquals(59, violations.get(9).getLine());
		assertEquals("Empty if body, remove it (preserve any side effects in the condition).", violations.get(9).getMessage());
	}

	@Test
	public void testEmptyInitializerViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyInitializerViolation.java");
		assertEquals(4, violations.size());

		assertEquals(4, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Empty static initializer block, remove it.", violations.get(0).getMessage());

		assertEquals(6, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Empty static initializer block, remove it.", violations.get(1).getMessage());

		assertEquals(9, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Empty instance initializer block, remove it.", violations.get(2).getMessage());

		assertEquals(11, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Empty instance initializer block, remove it.", violations.get(3).getMessage());
	}

	@Test
	public void testEmptyLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(EmptyBodyCheck.class, DIR + "InputEmptyLoopViolation.java");
		assertEquals(7, violations.size());
		var i = 0;

		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty do-while body, remove it (preserve any side effects in the condition).", violations.get(i++).getMessage());
		assertEquals(12, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty do-while body, remove it (preserve any side effects in the condition).", violations.get(i++).getMessage());

		assertEquals(17, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty for body, remove it (preserve any side effects in the condition/update).", violations.get(i++).getMessage());
		assertEquals(22, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty for body, remove it (preserve any side effects in the condition/update).", violations.get(i++).getMessage());
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty for body, remove it (preserve any side effects in the condition/update).", violations.get(i++).getMessage());

		assertEquals(30, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty while body, remove it (preserve any side effects in the condition).", violations.get(i++).getMessage());
		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Empty while body, remove it (preserve any side effects in the condition).", violations.get(i++).getMessage());
	}
}