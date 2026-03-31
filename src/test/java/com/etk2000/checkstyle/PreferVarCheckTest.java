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
		assertTrue(violations.getFirst().getMessage().contains("type arguments"));

		// type arguments with var
		assertEquals(13, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertTrue(violations.get(1).getMessage().contains("type arguments"));

		// var with generic return type
		assertEquals(17, violations.get(2).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(2).getSeverityLevel());
		assertTrue(violations.get(2).getMessage().contains("var"));

		// non-allowed method error
		assertEquals(21, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
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
		assertTrue(violations.getFirst().getMessage().contains("find"));
	}

	@Test
	public void testCleanVarUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarClean.java").isEmpty());
	}

	@Test
	public void testExplicitTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferVarCheck.class, DIR + "InputPreferVarViolation.java");
		assertEquals(6, violations.size());
		assertEquals(9, violations.getFirst().getLine());
		assertEquals(12, violations.get(1).getLine());
		assertEquals(17, violations.get(2).getLine());
		assertEquals(18, violations.get(3).getLine());
		assertEquals(19, violations.get(4).getLine());
		assertEquals(23, violations.get(5).getLine());
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

		// non-generic method with explicit type
		assertEquals(17, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
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
		assertTrue(violations.getFirst().getMessage().contains("emptyList"));

		// var with Optional.empty()
		assertEquals(9, violations.get(1).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(1).getSeverityLevel());
		assertTrue(violations.get(1).getMessage().contains("empty"));

		// non-generic String.valueOf
		assertEquals(13, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());

		// type arguments on reflection-detected generic
		assertEquals(17, violations.get(3).getLine());
		assertEquals(SeverityLevel.WARNING, violations.get(3).getSeverityLevel());
		assertTrue(violations.get(3).getMessage().contains("type arguments"));
	}
}