package com.etk2000.checkstyle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ForEachVarCheckTest {
	private static final String DIR = "foreachvar/";

	@Test
	public void testCleanVarUsage() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ForEachVarCheck.class, DIR + "InputForEachVarClean.java").isEmpty());
	}

	@Test
	public void testExplicitTypeViolation() throws Exception {
		final var violations = BaseCheckTest.runCheck(ForEachVarCheck.class, DIR + "InputForEachVarViolation.java");
		assertEquals(2, violations.size());
		assertEquals(8, violations.getFirst().getLine());
		assertEquals(11, violations.get(1).getLine());
	}
}