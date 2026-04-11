package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
		assertEquals("Overload 'arr(int)' must appear before 'arr(int[])'.", violations.get(0).getMessage());
		assertEquals("Overload 'dim(int[])' must appear before 'dim(int[][])'.", violations.get(1).getMessage());
		assertEquals("Overload 'mix(int[])' must appear before 'mix(String)'.", violations.get(2).getMessage());
		assertEquals("Overload 'method(int)' must appear before 'method(String)'.", violations.get(3).getMessage());
		assertEquals("Overload 'obj(List)' must appear before 'obj(String)'.", violations.get(4).getMessage());
		assertEquals("Overload 'prim(char)' must appear before 'prim(int)'.", violations.get(5).getMessage());
		assertEquals("Overload 'secondParam(int, char)' must appear before 'secondParam(int, int)'.", violations.get(6).getMessage());
		assertEquals("Overload 'vararg(int)' must appear before 'vararg(int...)'.", violations.get(7).getMessage());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("Overload 'method' with 1 parameters must appear before overload with 2 parameters.", violations.getFirst().getMessage());
	}
}