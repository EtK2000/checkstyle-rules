package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

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
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.getFirst().getMessage());
	}

	@Test
	public void testQualifiedNameViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferImportCheck.class, DIR + "InputPreferImportViolation.java");
		assertEquals(13, violations.size());

		assertEquals(3, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.lang.SuppressWarnings'.", violations.get(0).getMessage());

		assertEquals(5, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.ArrayList'.", violations.get(1).getMessage());

		assertEquals(6, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.io.Serializable'.", violations.get(2).getMessage());

		assertEquals(7, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.get(3).getMessage());

		assertEquals(10, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(4).getMessage());

		assertEquals(11, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(5).getMessage());

		assertEquals(14, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(6).getMessage());

		assertEquals(14, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Set'.", violations.get(7).getMessage());

		assertEquals(15, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.io.IOException'.", violations.get(8).getMessage());

		assertEquals(16, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(9).getMessage());

		assertEquals(20, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.List'.", violations.get(10).getMessage());

		assertEquals(20, violations.get(11).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(11).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map'.", violations.get(11).getMessage());

		assertEquals(25, violations.get(12).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(12).getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.ArrayList'.", violations.get(12).getMessage());
	}
}