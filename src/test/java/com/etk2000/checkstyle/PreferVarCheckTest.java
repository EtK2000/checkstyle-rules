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
	public void testExplicitTypeLiteralMismatchViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarLiteralMismatchViolation.java");
		assertEquals(65, violations.size());

		final var msg = "Local variable must use 'var' instead of an explicit type.";
		var i = 0;

		// same-type primitive literals (boolean, char, double, float, int, long)
		for (final var line : new int[]{6, 7, 8, 9, 10, 11}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// cast to matching type
		for (final var line : new int[]{16, 17, 18}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// double from float literal
		for (final var line : new int[]{22, 23, 24, 25}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// double from int literal
		for (final var line : new int[]{29, 30, 31, 32, 33, 34}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// double from long literal
		for (final var line : new int[]{38, 39, 40}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// float from double literal
		for (final var line : new int[]{44, 45, 46, 47}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// float from int literal
		for (final var line : new int[]{51, 52, 53, 54, 55, 56}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// long from int literal
		for (final var line : new int[]{60, 61, 62, 63, 64, 65}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		}

		// Boolean.parseBoolean — matching (error)
		assertEquals(69, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());

		// Byte.parseByte — matching (error), then widening to double/float/int/long/short (warning)
		assertEquals(74, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		for (final var line : new int[]{76, 77, 78, 79, 80}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());
		}

		// Double.parseDouble — matching (error)
		assertEquals(84, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());

		// Float.parseFloat — matching (error), widening to double (warning)
		assertEquals(89, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		assertEquals(91, violations.get(i).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());

		// Integer.parseInt — matching (error), widening to double/float/long (warning)
		assertEquals(96, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		for (final var line : new int[]{98, 99, 100}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());
		}

		// Long.parseLong — matching (error), widening to double/float (warning)
		assertEquals(105, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		for (final var line : new int[]{107, 108}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());
		}

		// Short.parseShort — matching (error), widening to double/float/int/long (warning)
		assertEquals(113, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i++).getSeverityLevel());
		for (final var line : new int[]{115, 116, 117, 118}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());
		}

		// primitive with non-literal expression (warning): binary op, cast mismatch, shift, ternary
		for (final var line : new int[]{123, 124, 125, 126, 127}) {
			assertEquals(line, violations.get(i).getLine());
			assertEquals(SeverityLevel.WARNING, violations.get(i++).getSeverityLevel());
		}

		// verify all violations have the same message
		for (var v : violations)
			assertEquals(msg, v.getMessage());
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