package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PreferDirectBooleanReturnCheckTest {
	private static final String DIR = "directbooleanreturn/";
	private static final String EXPECTED_MESSAGE = "Redundant if returning a boolean literal, return the condition directly.";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferDirectBooleanReturnCheck.class,
				DIR + "InputPreferDirectBooleanReturnClean.java"
		).isEmpty());
	}

	@Test
	public void testRedundantEqualityBranchCheckDoesNotFireOnDirectBooleanReturnResources() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferDirectBooleanReturnClean.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferDirectBooleanReturnViolation.java"
		).isEmpty());
	}

	@ParameterizedTest
	@ValueSource(ints = {5, 14, 21, 30, 36, 42, 48, 54, 60, 66, 72, 78, 85})
	public void testViolations(int expectedLine) throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferDirectBooleanReturnCheck.class,
				DIR + "InputPreferDirectBooleanReturnViolation.java"
		);
		final var match = violations.stream()
				.filter(v -> v.getLine() == expectedLine)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No violation at line " + expectedLine));
		assertEquals(SeverityLevel.ERROR, match.getSeverityLevel());
		assertEquals(EXPECTED_MESSAGE, match.getMessage());
	}

	@Test
	public void testViolationsCount() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferDirectBooleanReturnCheck.class,
				DIR + "InputPreferDirectBooleanReturnViolation.java"
		);
		assertEquals(13, violations.size());
	}
}