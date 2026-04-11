package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferMathMethodCheckTest {
	private static final String DIR = "mathmethod/";

	@Test
	public void testClampGatedByMinSdk() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodClampViolation.java",
				"minSdk",
				"34"
		).isEmpty());
	}

	@Test
	public void testClampViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodClampViolation.java",
				"minSdk",
				"35"
		);
		assertEquals(4, violations.size());

		assertEquals(5, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(lo, Math.min(hi, value))'.", violations.get(0).getMessage());

		assertEquals(9, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(Math.min(hi, value), lo)'.", violations.get(1).getMessage());

		assertEquals(13, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(hi, Math.max(lo, value))'.", violations.get(2).getMessage());

		assertEquals(17, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(Math.max(lo, value), hi)'.", violations.get(3).getMessage());
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferMathMethodCheck.class, DIR + "InputPreferMathMethodClean.java").isEmpty());
	}

	@Test
	public void testTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodTernaryViolation.java"
		);
		assertEquals(23, violations.size());
		var i = 0;

		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a >= 0 ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(11, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 >= a ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a > 0 ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 > a ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a <= 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 <= a ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(31, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 < a ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(39, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(arr[0])' instead of 'arr[0] < 0 ? -arr[0] : arr[0]'.", violations.get(i++).getMessage());

		assertEquals(43, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(47, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a >= b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(51, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a <= b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(59, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a < b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(63, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(arr[0], arr[1])' instead of 'arr[0] > arr[1] ? arr[0] : arr[1]'.", violations.get(i++).getMessage());

		assertEquals(67, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a.x, b.x)' instead of 'a.x > b.x ? a.x : b.x'.", violations.get(i++).getMessage());

		assertEquals(71, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, 5)' instead of 'a > 5 ? a : 5'.", violations.get(i++).getMessage());

		assertEquals(75, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(--a, b)' instead of '--a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(79, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(++a, b)' instead of '++a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(83, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a >= b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(87, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a > b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(91, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a <= b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(95, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a < b ? a : b'.", violations.get(i++).getMessage());
	}
}