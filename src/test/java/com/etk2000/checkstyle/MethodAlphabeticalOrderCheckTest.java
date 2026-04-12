package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import org.junit.jupiter.api.Test;

public class MethodAlphabeticalOrderCheckTest {
	private static final String DIR = "methodorder/";

	@Test
	public void testCleanOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderClean.java").isEmpty());
	}

	@Test
	public void testInstanceMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderViolation.java");
		assertEquals(2, violations.size());
		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Method 'alpha' must appear before 'zeta' (alphabetical order).", violations.get(0).getMessage());
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Method 'beta' must appear before 'gamma' (alphabetical order).", violations.get(1).getMessage());
	}

	@Test
	public void testOverloadsSkipped() throws Exception {
		assertTrue(BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderOverloads.java").isEmpty());
	}

	@Test
	public void testStaticMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(MethodAlphabeticalOrderCheck.class, DIR + "InputMethodOrderStaticViolation.java");
		assertEquals(1, violations.size());
		assertEquals(7, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Method 'alpha' must appear before 'beta' (alphabetical order).", violations.getFirst().getMessage());
	}
}