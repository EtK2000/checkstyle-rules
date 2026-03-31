package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ThreadAnnotationCheckTest {
	private static final String DIR = "thread/";

	@Test
	public void testAllAnnotationVariantsClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadAllAnnotationsClean.java").isEmpty());
	}

	@Test
	public void testCleanWithAnnotation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadClean.java").isEmpty());
	}

	@Test
	public void testEnumInterfaceRecordClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadEnumInterfaceRecordClean.java").isEmpty());
	}

	@Test
	public void testEnumInterfaceRecordViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadEnumInterfaceRecordViolation.java");
		assertEquals(3, violations.size());
		assertEquals(3, violations.get(0).getLine());
		assertEquals("Class 'InputThreadEnumViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).", violations.get(0).getMessage());
		assertEquals(6, violations.get(1).getLine());
		assertEquals("Class 'InputThreadInterfaceViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).", violations.get(1).getMessage());
		assertEquals(9, violations.get(2).getLine());
		assertEquals("Class 'InputThreadRecordViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).", violations.get(2).getMessage());
	}

	@Test
	public void testInnerClassesSkipped() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadInnerClassSkipped.java").isEmpty());
	}

	@Test
	public void testMissingAnnotation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ThreadAnnotationCheck.class, DIR + "InputThreadViolation.java");
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals("Class 'InputThreadViolation' must have a thread annotation (@AnyThread, @MainThread, etc.).", violations.getFirst().getMessage());
	}
}