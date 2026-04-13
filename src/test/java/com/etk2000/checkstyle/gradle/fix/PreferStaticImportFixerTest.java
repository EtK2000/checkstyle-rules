package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferStaticImportFixerTest {
	private final CheckstyleFixer fixer = new PreferStaticImportFixer();

	@CsvSource({
			"Predicate, not, java.util.function.Predicate.not",
			"Objects, requireNonNull, java.util.Objects.requireNonNull",
			"Objects, isNull, java.util.Objects.isNull",
			"Objects, nonNull, java.util.Objects.nonNull",
			"Objects, requireNonNullElse, java.util.Objects.requireNonNullElse",
			"Objects, requireNonNullElseGet, java.util.Objects.requireNonNullElseGet",
			"Collectors, toSet, java.util.stream.Collectors.toSet",
			"Collectors, groupingBy, java.util.stream.Collectors.groupingBy",
			"Collectors, joining, java.util.stream.Collectors.joining"
	})
	@ParameterizedTest
	public void testCandidateRewriteAddsStaticImport(String simpleClass, String simpleMethod, String expectedStaticImport) {
		final var prefix = "\t\tfinal var x = ";
		final var suffix = "(arg);";
		final var input = prefix + simpleClass + "." + simpleMethod + suffix;
		final var lines = new ArrayList<>(List.of(input));
		final var column = prefix.length();
		final var result = fixer.fix(lines, 0, column);
		assertNotNull(result);
		assertEquals(prefix + simpleMethod + suffix, result.replacement().getFirst());
		assertEquals(Set.of("static " + expectedStaticImport), result.importsToAdd());
	}

	@Test
	public void testColumnPastEolReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\tObjects.requireNonNull(x);"));
		assertNull(fixer.fix(lines, 0, 999));
	}

	@Test
	public void testColumnTargetsCorrectOccurrenceOnSameLine() {
		final var input = "\t\tObjects.requireNonNull(a); Objects.requireNonNull(b);";
		final var lines = new ArrayList<>(List.of(input));
		final var firstColumn = input.indexOf("Objects");
		final var secondColumn = input.indexOf("Objects", firstColumn + 1);
		final var result = fixer.fix(lines, 0, secondColumn);
		assertNotNull(result);
		assertEquals("\t\tObjects.requireNonNull(a); requireNonNull(b);", result.replacement().getFirst());
	}

	@Test
	public void testDotWithNoMethodIdentReturnsNull() {
		final var input = "\t\tObjects.(arg);";
		final var lines = new ArrayList<>(List.of(input));
		assertNull(fixer.fix(lines, 0, input.indexOf("Objects")));
	}

	@Test
	public void testNoDotAfterIdentReturnsNull() {
		final var input = "\t\treturn Objects;";
		final var lines = new ArrayList<>(List.of(input));
		assertNull(fixer.fix(lines, 0, input.indexOf("Objects")));
	}

	@Test
	public void testNoIdentAtColumnReturnsNull() {
		final var input = "\t\treturn 42;";
		final var lines = new ArrayList<>(List.of(input));
		assertNull(fixer.fix(lines, 0, input.indexOf("42")));
	}

	@Test
	public void testTabIndentedLine() {
		final var input = "\t\t\t\t.filter(Predicate.not(String::isEmpty))";
		final var lines = new ArrayList<>(List.of(input));
		final var column = input.indexOf("Predicate");
		final var result = fixer.fix(lines, 0, column);
		assertNotNull(result);
		assertEquals("\t\t\t\t.filter(not(String::isEmpty))", result.replacement().getFirst());
		assertEquals(Set.of("static java.util.function.Predicate.not"), result.importsToAdd());
	}

	@Test
	public void testUnknownClassReturnsNull() {
		final var input = "\t\tString.valueOf(x);";
		final var lines = new ArrayList<>(List.of(input));
		assertNull(fixer.fix(lines, 0, input.indexOf("String")));
	}
}