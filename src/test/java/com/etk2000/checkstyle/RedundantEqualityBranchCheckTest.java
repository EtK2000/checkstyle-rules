package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class RedundantEqualityBranchCheckTest {
	private static final String DIR = "redundantequality/";

	private static Stream<Arguments> violationProvider() {
		return Stream.of(
				Arguments.of(6, "Redundant equality if-else, use 'b' directly."),
				Arguments.of(15, "Redundant equality if-else, use 'a' directly."),
				Arguments.of(23, "Redundant equality if-else, use 'b' directly."),
				Arguments.of(30, "Redundant equality if-else, use 'a' directly."),
				Arguments.of(38, "Redundant equality if-else, use 'a' directly."),
				Arguments.of(46, "Redundant equality if-else, use 'a' directly."),
				Arguments.of(54, "Redundant equality if-else, use 'b' directly."),
				Arguments.of(62, "Redundant equality if-else, use 'a' directly."),
				Arguments.of(68, "Redundant equality if-else, use 'b' directly."),
				Arguments.of(74, "Redundant equality if-else, use 'a' directly.")
		);
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputRedundantEqualityBranchClean.java"
		).isEmpty());
	}

	@Test
	public void testPreferMathMethodCheckDoesNotFireOnEqualityResources() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputRedundantEqualityBranchViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputRedundantEqualityBranchClean.java"
		).isEmpty());
	}

	@MethodSource("violationProvider")
	@ParameterizedTest
	public void testViolations(int expectedLine, String expectedMessage) throws Exception {
		final var violations = BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputRedundantEqualityBranchViolation.java"
		);
		final var match = violations.stream()
				.filter(v -> v.getLine() == expectedLine)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No violation at line " + expectedLine));
		assertEquals(SeverityLevel.ERROR, match.getSeverityLevel());
		assertEquals(expectedMessage, match.getMessage());
	}

	@Test
	public void testViolationsCount() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputRedundantEqualityBranchViolation.java"
		);
		assertEquals(10, violations.size());
	}
}