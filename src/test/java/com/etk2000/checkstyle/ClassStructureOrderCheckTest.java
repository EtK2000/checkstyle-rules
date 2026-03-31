package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ClassStructureOrderCheckTest {
	private static final String DIR = "classstructure/";

	@Test
	public void testCleanAllSections() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureClean.java").isEmpty());
	}

	@Test
	public void testConstructorThenStaticMethodIsViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureConstructorThenStatic.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'staticMethod' (static method) must appear before constructor/instance initializer section.", violations.getFirst().getMessage());
	}

	@Test
	public void testEnumStaticAfterInstance() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureEnum.java");
		assertEquals(1, violations.size());
		assertEquals(8, violations.getFirst().getLine());
		assertEquals("'staticMethod' (static method) must appear before instance method section.", violations.getFirst().getMessage());
	}

	@Test
	public void testFieldAfterMethod() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureFieldAfterMethod.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'field' (instance field) must appear before instance method section.", violations.getFirst().getMessage());
	}

	@Test
	public void testInnerTypeAfterMethod() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureInnerTypeAfterMethod.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'Inner' (inner type) must appear before instance method section.", violations.getFirst().getMessage());
	}

	@Test
	public void testInstanceInitAfterMethodViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureInstanceInitViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'<instance init>' (constructor/instance initializer) must appear before instance method section.", violations.getFirst().getMessage());
	}

	@Test
	public void testMultipleViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureMultipleViolations.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'static1' (static method) must appear before instance method section.", violations.get(0).getMessage());
		assertEquals(10, violations.get(1).getLine());
		assertEquals("'static2' (static method) must appear before instance method section.", violations.get(1).getMessage());
	}

	@Test
	public void testNestedClassScopesAreIndependent() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureNestedScopes.java");
		assertEquals(2, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'Inner' (inner type) must appear before instance method section.", violations.get(0).getMessage());
		assertEquals(12, violations.get(1).getLine());
		assertEquals("'outerStatic' (static method) must appear before instance method section.", violations.get(1).getMessage());
	}

	@Test
	public void testOnlyInstance() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureOnlyInstance.java").isEmpty());
	}

	@Test
	public void testOnlyStatic() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureOnlyStatic.java").isEmpty());
	}

	@Test
	public void testStaticAfterInstanceViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ClassStructureOrderCheck.class, DIR + "InputClassStructureViolation.java");
		assertEquals(1, violations.size());
		assertEquals(6, violations.getFirst().getLine());
		assertEquals("'staticMethod' (static method) must appear before instance method section.", violations.getFirst().getMessage());
	}
}