package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

public class PreferExactAssertionCheckTest {
	private record Expected(int line, String message) {}

	private static final String DIR = "preferexactassertion/";

	private static void assertViolations(List<AuditEvent> got, Expected... expected) {
		assertEquals(expected.length, got.size());
		for (var i = 0; i < expected.length; ++i) {
			assertEquals(expected[i].line, got.get(i).getLine(), "line " + i);
			assertEquals(SeverityLevel.ERROR, got.get(i).getSeverityLevel(), "severity " + i);
			assertEquals(expected[i].message, got.get(i).getMessage(), "message " + i);
		}
	}

	static Stream<Arguments> isJunitAssertClassProvider() {
		return Stream.of(
				Arguments.of("Assert", true),
				Arguments.of("Assertions", true),
				Arguments.of("Assertion", false),
				Arguments.of("assert", false),
				Arguments.of("Helper", false),
				Arguments.of(null, false)
		);
	}

	@MethodSource("isJunitAssertClassProvider")
	@ParameterizedTest
	void testIsJunitAssertClass(String simpleName, boolean expected) {
		assertEquals(expected, PreferExactAssertionCheck.isJunitAssertClass(simpleName));
	}

	@Test
	public void testStateResetBetweenFiles() throws Exception {
		final var violations = BaseCheckTest.runCheckOnFiles(
				PreferExactAssertionCheck.class,
				DIR + "cases.junit4wildcard.in.java",
				DIR + "cases.junit5wildcard.in.java"
		);
		assertViolations(
				violations,
				new Expected(10, "Use 'assertEquals' instead of 'assertTrue' with '>'."),
				new Expected(19, "Use 'assertFalse' instead of 'assertTrue' with a negated argument."),
				new Expected(10, "Use 'assertEquals' instead of 'assertTrue' with '>'."),
				new Expected(19, "Use 'assertInstanceOf' instead of 'assertTrue' with 'instanceof'.")
		);
	}
}