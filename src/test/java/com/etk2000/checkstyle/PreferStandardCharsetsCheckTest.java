package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferStandardCharsetsCheckTest {
	private static final String DIR = "standardcharsets/";
	private static final String GENERIC_MSG = "Use a StandardCharsets constant instead of a String charset name.";
	private static final String VIOLATION_FILE = DIR + "InputStandardCharsetsViolation.java";

	@Test
	public void testBuiltinFunctionViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var builtins = violations.stream().filter(v -> v.getLine() >= 19 && v.getLine() <= 31).toList();
		assertEquals(13, builtins.size());
		for (var v : builtins)
			assertEquals("Use 'StandardCharsets.UTF_8' instead of \"UTF-8\".", v.getMessage());
	}

	@Test
	public void testCleanCode() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, DIR + "InputStandardCharsetsClean.java", "minSdk", "19"
		).isEmpty());
	}

	@Test
	public void testLowMinSdkSuppresses() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "18"
		).isEmpty());
	}

	@Test
	public void testOtherCharsetViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var others = violations.stream().filter(v -> v.getLine() >= 35 && v.getLine() <= 41).toList();
		assertEquals(7, others.size());
		assertEquals("Use 'StandardCharsets.ISO_8859_1' instead of \"ISO-8859-1\".", others.get(0).getMessage());
		assertEquals("Use 'StandardCharsets.ISO_8859_1' instead of \"latin1\".", others.get(1).getMessage());
		assertEquals("Use 'StandardCharsets.US_ASCII' instead of \"US-ASCII\".", others.get(2).getMessage());
		assertEquals("Use 'StandardCharsets.US_ASCII' instead of \"ASCII\".", others.get(3).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_16' instead of \"UTF-16\".", others.get(4).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_16BE' instead of \"UTF-16BE\".", others.get(5).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_16LE' instead of \"UTF-16LE\".", others.get(6).getMessage());
	}

	@Test
	public void testStringField() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var field = violations.stream().filter(v -> v.getLine() >= 45 && v.getLine() <= 56).toList();
		assertEquals(12, field.size());
		for (var v : field)
			assertEquals(GENERIC_MSG, v.getMessage());
	}

	@Test
	public void testStringLocal() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var locals = violations.stream().filter(v -> v.getLine() >= 61 && v.getLine() <= 72).toList();
		assertEquals(12, locals.size());
		for (var v : locals)
			assertEquals(GENERIC_MSG, v.getMessage());
	}

	@Test
	public void testStringParameter() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var params = violations.stream().filter(v -> v.getLine() >= 76 && v.getLine() <= 87).toList();
		assertEquals(12, params.size());
		for (var v : params)
			assertEquals(GENERIC_MSG, v.getMessage());
	}

	@Test
	public void testUtf8Aliases() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferStandardCharsetsCheck.class, VIOLATION_FILE, "minSdk", "19"
		);
		final var utf8 = violations.stream().filter(v -> v.getLine() >= 91).toList();
		assertEquals(5, utf8.size());
		assertEquals("Use 'StandardCharsets.UTF_8' instead of \"UTF-8\".", utf8.get(0).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_8' instead of \"utf-8\".", utf8.get(1).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_8' instead of \"UTF8\".", utf8.get(2).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_8' instead of \"utf8\".", utf8.get(3).getMessage());
		assertEquals("Use 'StandardCharsets.UTF_8' instead of \"Utf-8\".", utf8.get(4).getMessage());
	}
}