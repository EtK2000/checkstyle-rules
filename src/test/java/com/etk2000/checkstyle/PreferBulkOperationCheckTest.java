package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class PreferBulkOperationCheckTest {
	private static final String DIR = "preferbulkoperation/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferBulkOperationCheck.class, DIR + "InputPreferBulkOperationClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferBulkOperationCheck.class, DIR + "InputPreferBulkOperationViolation.java");
		assertEquals(15, violations.size());
		var i = 0;

		assertEquals(8, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.addAll(source)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.addAll(source)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.addAll(source)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(24, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.", violations.get(i++).getMessage());

		assertEquals(29, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.", violations.get(i++).getMessage());

		assertEquals(34, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Arrays.fill(arr, -a[b[0]])' instead of a loop that assigns a constant.", violations.get(i++).getMessage());

		assertEquals(39, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Arrays.fill(arr, +other[0])' instead of a loop that assigns a constant.", violations.get(i++).getMessage());

		assertEquals(44, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'other.addAll(list)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(48, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'other.addAll(list)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(54, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'other.addAll(list)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.putAll(source)' instead of a loop that puts entries one at a time.", violations.get(i++).getMessage());

		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.putAll(source)' instead of a loop that puts entries one at a time.", violations.get(i++).getMessage());

		assertEquals(68, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'other.addAll(list)' instead of a loop that adds elements one at a time.", violations.get(i++).getMessage());

		assertEquals(72, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.putAll(source)' instead of a loop that puts entries one at a time.", violations.get(i++).getMessage());

		assertEquals(76, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'target.putAll(source)' instead of a loop that puts entries one at a time.", violations.get(i++).getMessage());
	}
}