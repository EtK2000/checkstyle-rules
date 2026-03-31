package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.Test;

public class PreferVarCheckTest {
	private static final String DIR = "prefervar/";

	@Test
	public void testAllowedMethodsClean() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputPreferVarAllowedMethodClean.java",
				"allowedMethods",
				"genericMethod"
		);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testAllowedMethodsViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferVarCheck.class,
				DIR + "InputPreferVarAllowedMethodViolation.java",
				"allowedMethods",
				"genericMethod"
		);
		assertEquals(4, violations.size());

		// type arguments with explicit type
		assertEquals(9, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Prefer explicit type over type arguments on 'genericMethod'.", violations.getFirst().getMessage());

		// type arguments with var
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals("Prefer explicit type over type arguments on 'genericMethod'.", violations.get(1).getMessage());

		// var with generic return type
		assertEquals(17, violations.get(2).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(2).getSeverityLevel());
		assertEquals("Using 'var' with 'genericMethod' loses generic type information, consider using an explicit type.", violations.get(2).getMessage());

		// non-allowed method error
		assertEquals(21, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(3).getMessage());
	}

	@Test
	public void testChainClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarChainClean.java").isEmpty());
	}

	@Test
	public void testChainViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarChainViolation.java");
		assertEquals(1, violations.size());

		// var with chained generic return: GenericReturnHelper.create().find(1)
		assertEquals(11, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Using 'var' with 'find' loses generic type information, consider using an explicit type.", violations.getFirst().getMessage());
	}

	@Test
	public void testCleanVarUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarClean.java").isEmpty());
	}

	@Test
	public void testExplicitTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarViolation.java");
		assertEquals(10, violations.size());

		// for-each with explicit type
		assertEquals(9, violations.getFirst().getLine());
		assertEquals("For-each loop must use 'var' instead of an explicit type.", violations.getFirst().getMessage());

		// for-each with explicit type
		assertEquals(12, violations.get(1).getLine());
		assertEquals("For-each loop must use 'var' instead of an explicit type.", violations.get(1).getMessage());

		// local with explicit type
		assertEquals(17, violations.get(2).getLine());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(2).getMessage());

		assertEquals(18, violations.get(3).getLine());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(3).getMessage());

		assertEquals(19, violations.get(4).getLine());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(4).getMessage());

		// explicit array initializer: var names = new String[]{"a", "b"}
		assertEquals(20, violations.get(5).getLine());
		assertEquals("Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.", violations.get(5).getMessage());

		// explicit array initializer: String[] numbers = new String[]{"1"}
		assertEquals(21, violations.get(6).getLine());
		assertEquals("Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.", violations.get(6).getMessage());

		// explicit array initializer: int[][] matrix = new int[][]{{1}, {2}}
		assertEquals(22, violations.get(7).getLine());
		assertEquals("Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.", violations.get(7).getMessage());

		// complex anonymous class with explicit type
		assertEquals(23, violations.get(8).getLine());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(8).getMessage());

		// try-with-resources with explicit type
		assertEquals(34, violations.get(9).getLine());
		assertEquals("Try-with-resources must use 'var' instead of an explicit type.", violations.get(9).getMessage());
	}

	@Test
	public void testGenericReturnAutoDetectedClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarGenericReturnClean.java").isEmpty());
	}

	@Test
	public void testGenericReturnAutoDetectedViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarGenericReturnViolation.java");
		assertEquals(2, violations.size());

		// var with auto-detected generic return type
		assertEquals(13, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Using 'var' with 'cast' loses generic type information, consider using an explicit type.", violations.getFirst().getMessage());

		// non-generic method with explicit type
		assertEquals(17, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(1).getMessage());
	}

	@Test
	public void testReflectionClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarReflectionClean.java").isEmpty());
	}

	@Test
	public void testReflectionViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarReflectionViolation.java");
		assertEquals(4, violations.size());

		// var with Collections.emptyList()
		assertEquals(8, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Using 'var' with 'emptyList' loses generic type information, consider using an explicit type.", violations.getFirst().getMessage());

		// var with Optional.empty()
		assertEquals(9, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals("Using 'var' with 'empty' loses generic type information, consider using an explicit type.", violations.get(1).getMessage());

		// non-generic String.valueOf
		assertEquals(13, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Local variable must use 'var' instead of an explicit type.", violations.get(2).getMessage());

		// type arguments on reflection-detected generic
		assertEquals(17, violations.get(3).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(3).getSeverityLevel());
		assertEquals("Prefer explicit type over type arguments on 'emptyList'.", violations.get(3).getMessage());
	}
}