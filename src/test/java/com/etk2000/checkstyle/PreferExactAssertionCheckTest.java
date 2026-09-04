package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.util.Objects.requireNonNull;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class PreferExactAssertionCheckTest {
	private record Expected(String file, int line, SeverityLevel severity, String message) {}

	private static final String DIR = "preferexactassertion/";

	private static void assertViolations(List<AuditEvent> got, List<Expected> expected) {
		assertEquals(expected.size(), got.size());
		for (var i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i).line, got.get(i).getLine(), "line " + i);
			assertEquals(expected.get(i).severity, got.get(i).getSeverityLevel(), "severity " + i);
			assertEquals(expected.get(i).message, got.get(i).getMessage(), "message " + i);
			// a directive-bearing fixture is checked from a translated temp copy, so only the name survives
			assertTrue(
					got.get(i).getFileName().endsWith(expected.get(i).file.substring(DIR.length())),
					"file " + i + ": expected " + expected.get(i).file + ", got " + got.get(i).getFileName()
			);
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

	@CheckReturnValue
	@Nonnull
	private static List<Expected> markersOf(@Nonnull String... files) throws Exception {
		final var expected = new ArrayList<Expected>();
		for (var file : files) {
			final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + file);
			requireNonNull(url, "Test input file not found: " + file);
			for (var marker : BaseCheckTest.parseViolationMarkers(Files.readAllLines(Path.of(url.toURI()))))
				expected.add(new Expected(file, marker.line(), marker.severity(), marker.message()));
		}
		return expected;
	}

	@MethodSource("isJunitAssertClassProvider")
	@ParameterizedTest
	void testIsJunitAssertClass(String simpleName, boolean expected) {
		assertEquals(expected, PreferExactAssertionCheck.isJunitAssertClass(simpleName));
	}

	@Test
	public void testStateResetBetweenFiles() throws Exception {
		final var first = DIR + "cases.junit4wildcard.in.java";
		final var second = DIR + "cases.junit5wildcard.in.java";
		final var expected = markersOf(first, second);
		assertEquals(4, expected.size(), "fixtures lost their violation markers, the assertion would be vacuous");
		assertViolations(
				BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, first, second),
				expected
		);
	}
}