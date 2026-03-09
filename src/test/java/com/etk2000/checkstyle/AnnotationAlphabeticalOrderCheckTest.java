package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AnnotationAlphabeticalOrderCheckTest {
	private static final String DIR = "annotation/";

	@Test
	public void testCleanAlphabeticalOrder() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationAlphabeticalOrderCheck.class, DIR + "InputAnnotationClean.java").isEmpty());
	}

	@Test
	public void testImportedAnnotationViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationAlphabeticalOrderCheck.class, DIR + "InputAnnotationQualifiedViolation.java");
		assertEquals(1, violations.size());
		assertEquals(9, violations.getFirst().getLine());
	}

	@Test
	public void testQualifiedNameViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationAlphabeticalOrderCheck.class, DIR + "InputAnnotationQualifiedNameViolation.java");
		assertEquals(1, violations.size());
		assertEquals(5, violations.getFirst().getLine());
	}

	@Test
	public void testSingleAnnotation() throws Exception {
		assertTrue(BaseCheckTest.runCheck(AnnotationAlphabeticalOrderCheck.class, DIR + "InputAnnotationSingleAnnotation.java").isEmpty());
	}

	@Test
	public void testViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(AnnotationAlphabeticalOrderCheck.class, DIR + "InputAnnotationViolation.java");
		assertEquals(2, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertTrue(violations.getFirst().getMessage().contains("CheckResult"));
	}
}