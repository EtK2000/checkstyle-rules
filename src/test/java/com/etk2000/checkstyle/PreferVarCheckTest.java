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
		assertEquals(23, violations.size());

		final var localMsg = "Local variable must use 'var' instead of an explicit type.";
		final var forEachMsg = "For-each loop must use 'var' instead of an explicit type.";
		final var tryMsg = "Try-with-resources must use 'var' instead of an explicit type.";
		final var arrayMsg = "Use implicit array initializer ('Type[] x = {...}') instead of 'new Type[]{...}'.";
		var i = 0;

		// annotated local variable (@Nonnull on own line, position at annotation)
		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// constructor calls (Object, HashMap<>)
		assertEquals(20, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(21, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// for-each (String, Integer)
		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(forEachMsg, violations.get(i++).getMessage());
		assertEquals(29, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(forEachMsg, violations.get(i++).getMessage());

		// for-each annotated (@Nonnull String)
		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(forEachMsg, violations.get(i++).getMessage());

		// for-each generic type (Entry<String, Integer>)
		assertEquals(41, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(forEachMsg, violations.get(i++).getMessage());

		// for-loop init (int)
		assertEquals(46, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// for-loop init (Iterator<String>)
		assertEquals(52, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// local variables (int, String, List<Integer>)
		assertEquals(57, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(58, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(59, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// explicit array initializers (var + new String[], String[] + new String[], int[][] + new int[][])
		assertEquals(60, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(arrayMsg, violations.get(i++).getMessage());
		assertEquals(61, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(arrayMsg, violations.get(i++).getMessage());
		assertEquals(62, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(arrayMsg, violations.get(i++).getMessage());

		// complex anonymous class
		assertEquals(63, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// method call (String.valueOf) and chain (.trim().toLowerCase())
		assertEquals(74, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(75, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// wildcard (List<?>), nested generic (Map<String, List<Integer>>), concrete (ArrayList<String>)
		assertEquals(79, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(80, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());
		assertEquals(81, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(localMsg, violations.get(i++).getMessage());

		// try-with-resources
		assertEquals(85, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(tryMsg, violations.get(i++).getMessage());

		// try-with-resources annotated
		assertEquals(91, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals(tryMsg, violations.get(i++).getMessage());
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
	public void testMultiVarWarning() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarMultiVarViolation.java");
		assertEquals(7, violations.size());

		final var msg = "Local variable must use 'var' instead of an explicit type.";

		// multi-var annotated (@Nonnull on own line, position at annotation)
		assertEquals(7, violations.get(0).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(0).getSeverityLevel());
		assertEquals(msg, violations.get(0).getMessage());

		// multi-var for-init
		assertEquals(12, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertEquals(msg, violations.get(1).getMessage());

		// multi-var for-init annotated (@Nonnull inline)
		assertEquals(17, violations.get(2).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(2).getSeverityLevel());
		assertEquals(msg, violations.get(2).getMessage());

		// multi-var local: final int x = 1, y = 2;
		assertEquals(22, violations.get(3).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(3).getSeverityLevel());
		assertEquals(msg, violations.get(3).getMessage());

		// multi-var local: final String a = "a", b = "b";
		assertEquals(23, violations.get(4).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(4).getSeverityLevel());
		assertEquals(msg, violations.get(4).getMessage());

		// multi-var with method call + literal
		assertEquals(27, violations.get(5).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(5).getSeverityLevel());
		assertEquals(msg, violations.get(5).getMessage());

		// multi-var partial init
		assertEquals(31, violations.get(6).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(6).getSeverityLevel());
		assertEquals(msg, violations.get(6).getMessage());
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