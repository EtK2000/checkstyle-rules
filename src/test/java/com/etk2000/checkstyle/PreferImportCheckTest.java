package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PreferImportCheckTest {
	private static final String DIR = "preferimport/";

	@Test
	public void testCleanImports() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferImportCheck.class, DIR + "InputPreferImportClean.java").isEmpty());
	}

	@Test
	public void testGenericTypeArgViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferImportCheck.class, DIR + "InputPreferImportGenericViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.getFirst().getMessage());
	}

	@Test
	public void testQualifiedNameViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferImportCheck.class, DIR + "InputPreferImportViolation.java");
		assertEquals(12, violations.size());

		assertEquals(3, violations.get(0).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.lang.SuppressWarnings'.", violations.get(0).getMessage());

		assertEquals(5, violations.get(1).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.ArrayList'.", violations.get(1).getMessage());

		assertEquals(6, violations.get(2).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.io.Serializable'.", violations.get(2).getMessage());

		assertEquals(7, violations.get(3).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.get(3).getMessage());

		assertEquals(10, violations.get(4).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(4).getMessage());

		assertEquals(11, violations.get(5).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(5).getMessage());

		assertEquals(14, violations.get(6).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(6).getMessage());

		assertEquals(14, violations.get(7).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.Set'.", violations.get(7).getMessage());

		assertEquals(15, violations.get(8).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.io.IOException'.", violations.get(8).getMessage());

		assertEquals(16, violations.get(9).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(9).getMessage());

		assertEquals(20, violations.get(10).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(10).getMessage());

		assertEquals(20, violations.get(11).getLine());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.get(11).getMessage());
	}
}