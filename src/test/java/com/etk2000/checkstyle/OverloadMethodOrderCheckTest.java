package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class OverloadMethodOrderCheckTest {
	private static final String DIR = "overload/";

	@Test
	public void testCleanOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadClean.java").isEmpty());
	}

	@Test
	public void testTypeOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadTypeViolation.java");
		assertEquals(8, violations.size());

		assertEquals(6, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Overload 'arr(int)' must appear before 'arr(int[])'.", violations.get(0).getMessage());

		assertEquals(10, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Overload 'dim(int[])' must appear before 'dim(int[][])'.", violations.get(1).getMessage());

		assertEquals(14, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Overload 'mix(int[])' must appear before 'mix(String)'.", violations.get(2).getMessage());

		assertEquals(18, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Overload 'method(int)' must appear before 'method(String)'.", violations.get(3).getMessage());

		assertEquals(22, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Overload 'obj(List)' must appear before 'obj(String)'.", violations.get(4).getMessage());

		assertEquals(26, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Overload 'prim(char)' must appear before 'prim(int)'.", violations.get(5).getMessage());

		assertEquals(30, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Overload 'secondParam(int, char)' must appear before 'secondParam(int, int)'.", violations.get(6).getMessage());

		assertEquals(34, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals("Overload 'vararg(int)' must appear before 'vararg(int...)'.", violations.get(7).getMessage());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Overload 'method' with 1 parameters must appear before overload with 2 parameters.", violations.getFirst().getMessage());
	}
}