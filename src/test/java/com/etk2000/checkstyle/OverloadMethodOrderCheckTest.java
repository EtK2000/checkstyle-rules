package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OverloadMethodOrderCheckTest {
	private static final String DIR = "overload/";

	@Test
	public void testCleanOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadClean.java").isEmpty());
	}

	@Test
	public void testTypeOrderViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadTypeViolation.java");
		assertEquals(6, violations.size());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(OverloadMethodOrderCheck.class, DIR + "InputOverloadViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("method"));
	}
}