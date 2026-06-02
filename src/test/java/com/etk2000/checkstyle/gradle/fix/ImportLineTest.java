package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.stream.Stream;

public class ImportLineTest {
	static Stream<Arguments> parseProvider() {
		return Stream.of(
				Arguments.of("import java.util.List;", "java.util.List", false, false),
				Arguments.of("import static org.junit.Assert.assertTrue;", "org.junit.Assert.assertTrue", true, false),
				Arguments.of("import java.util.*;", "java.util", false, true),
				Arguments.of("import static org.junit.Assert.*;", "org.junit.Assert", true, true),
				Arguments.of("import java . util . * ;", "java.util", false, true),
				Arguments.of("import static a . b . C . * ;", "a.b.C", true, true),
				Arguments.of("\timport  java.util.List ;", "java.util.List", false, false),
				Arguments.of("import java . util . List ;", "java.util.List", false, false),
				Arguments.of("import static a . b . C . m ;", "a.b.C.m", true, false),
				Arguments.of("import a.staticfoo.List;", "a.staticfoo.List", false, false),
				Arguments.of("import café.Foo;", "café.Foo", false, false),
				Arguments.of("import a.B$Inner;", "a.B$Inner", false, false),
				Arguments.of("import a.B", null, false, false),
				Arguments.of("import a.B; x", null, false, false),
				Arguments.of("package a.B;", null, false, false),
				Arguments.of("import ;", null, false, false),
				Arguments.of("import a;b;", null, false, false),
				Arguments.of("importjava.util.List;", null, false, false)
		);
	}

	@MethodSource("parseProvider")
	@ParameterizedTest
	void testParse(String line, String expectedFqn, boolean expectedStatic, boolean expectedWildcard) {
		final var parsed = ImportLine.parse(line);
		if (expectedFqn == null) {
			assertNull(parsed);
			return;
		}
		assertNotNull(parsed);
		assertEquals(expectedFqn, parsed.fqn());
		assertEquals(expectedStatic, parsed.staticImport());
		assertEquals(expectedWildcard, parsed.wildcard());
	}

	@Test
	void testParseDoesNotBacktrackOnWhitespaceRun() {
		final var line = "import " + " ".repeat(200_000);
		assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertNull(ImportLine.parse(line)));
	}
}