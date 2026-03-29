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
	}

	@Test
	public void testQualifiedNameViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferImportCheck.class, DIR + "InputPreferImportViolation.java");
		assertEquals(12, violations.size());
		assertEquals(3, violations.get(0).getLine());  // annotation
		assertEquals(5, violations.get(1).getLine());  // extends
		assertEquals(6, violations.get(2).getLine());  // implements
		assertEquals(7, violations.get(3).getLine());  // field type
		assertEquals(10, violations.get(4).getLine()); // instanceof
		assertEquals(11, violations.get(5).getLine()); // cast
		assertEquals(14, violations.get(6).getLine()); // return type
		assertEquals(14, violations.get(7).getLine()); // param type
		assertEquals(15, violations.get(8).getLine()); // throws
		assertEquals(16, violations.get(9).getLine()); // local type
		assertEquals(20, violations.get(10).getLine()); // nested generic return type
		assertEquals(20, violations.get(11).getLine()); // nested generic type arg
	}
}