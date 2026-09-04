package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Direct tests for {@link JitInefficiencyCheck#categorizeAt}, whose {@code null} return is
 * unreachable from the fixer's own fixtures: every slice reports a real violation, and every
 * fragment fails to parse before {@code categorizeAt} is called at all.
 *
 * <p>Also covers {@link JitInefficiencyCheck#splitLiteralTakesFastPath} char by char, for values a
 * fixture cannot spell: a lone surrogate has no UTF-8 encoding, and a unicode escape cannot be
 * authored into one. Unicode escapes are spelled {@code BS + "u002c"} here so javac does not
 * translate them in this source first.
 */
public class JitInefficiencyCheckTest {
	private static final String BS = "\\";
	private static final String CLEAN_FIXTURE = "jitinefficiency/cases.clean.java";
	private static final String VIOLATION_FIXTURE = "jitinefficiency/cases.in.java";

	/** Literal texts whose escapes decide the outcome: the raw spelling alone would classify them the other way. */
	@Nonnull
	private static Stream<Arguments> decodedSplitLiterals() {
		return Stream.of(
				Arguments.of("\"" + BS + "t\"", true),
				Arguments.of("\"" + BS + BS + "t\"", false),
				Arguments.of("\"" + BS + "54\"", true),
				Arguments.of("\"" + BS + "174\"", false),
				Arguments.of("\"" + BS + "u002c\"", true),
				Arguments.of("\"" + BS + "u007c\"", false),
				Arguments.of("\"" + BS + "u005c" + BS + "u005c.\"", true),
				Arguments.of("\"" + BS + "uD7FF\"", true),
				Arguments.of("\"" + BS + "uD800\"", false),
				Arguments.of("\"" + BS + "uDFFF\"", false),
				Arguments.of("\"" + BS + "uE000\"", true),
				Arguments.of("\"" + BS + "u0022\"", false),
				Arguments.of("\"" + BS + "q\"", false),
				Arguments.of(",", false),
				Arguments.of("\",", false)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<String> eachChar(@Nonnull String chars) {
		return chars.chars().mapToObj(c -> String.valueOf((char) c));
	}

	/** Values every runtime splits on without a {@code Pattern}, including both edges of each excluded range. */
	@Nonnull
	private static Stream<String> fastSplitValues() {
		final var nonAscii = Stream.of(
				String.valueOf((char) 0x7F),
				String.valueOf((char) 0xE9),
				String.valueOf((char) (Character.MIN_HIGH_SURROGATE - 1)),
				String.valueOf((char) (Character.MAX_LOW_SURROGATE + 1)),
				String.valueOf(Character.MAX_VALUE)
		);
		final var escaped = eachChar("\\?*+[](){}^$.|").map(c -> BS + c);
		return Stream.of(eachChar(",- \t\0\"'#!%&=<>_~/:@`09azAZ"), nonAscii, escaped).flatMap(values -> values);
	}

	/** {@code value} spelled as a string literal, escaping only what a literal cannot hold raw. */
	@CheckReturnValue
	@Nonnull
	private static String literalOf(@Nonnull String value) {
		return '"' + value.replace(BS, BS + BS).replace("\"", BS + "\"") + '"';
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture(@Nonnull String name) throws Exception {
		final var url = requireNonNull(
				JitInefficiencyCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + name),
				"Test input file not found: " + name
		);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	/** Values at least one runtime compiles a {@code Pattern} for, each beside a fast neighbour above. */
	@Nonnull
	private static Stream<String> slowSplitValues() {
		final var surrogates = Stream.of(
				String.valueOf(Character.MIN_HIGH_SURROGATE),
				String.valueOf(Character.MAX_HIGH_SURROGATE),
				String.valueOf(Character.MIN_LOW_SURROGATE),
				String.valueOf(Character.MAX_LOW_SURROGATE)
		);
		final var escaped = Stream.concat(
				eachChar("09azAZd/:@`,- ~#\"t"),
				Stream.of(String.valueOf((char) 0xE9), String.valueOf(Character.MIN_HIGH_SURROGATE))
		).map(c -> BS + c);
		final var otherLengths = Stream.of(
				"",
				"ab",
				",,",
				", ",
				"." + BS,
				"a" + BS,
				"abc",
				BS + ".x",
				"x" + BS + ".",
				BS + BS + BS,
				Character.toString(0x1D49C)
		);
		return Stream.of(eachChar(".$|()[]{}^?*+\\"), surrogates, escaped, otherLengths).flatMap(values -> values);
	}

	/**
	 * The positive control for {@link #categorizeAtReturnsNullWhereNoNodeIsDetectable}: without it a
	 * {@code findNodeAt} regression that answered null everywhere would pass that test.
	 */
	@Test
	public void categorizeAtFindsACategorySomewhereInTheViolationFixture() throws Exception {
		final var root = parseFixture(VIOLATION_FIXTURE);
		JitInefficiencyCategory found = null;
		for (var line = 0; line < 200 && found == null; ++line) {
			for (var column = 0; column < 120 && found == null; ++column)
				found = JitInefficiencyCheck.categorizeAt(root, line, column);
		}
		assertNotNull(found);
	}

	/**
	 * A sweep rather than one position: probing {@code (0, 0)} in a clean file lands on the
	 * {@code package} line, which answers null for the same reason it does in a file full of
	 * violations, so it would not distinguish the two fixtures at all.
	 */
	@Test
	public void categorizeAtReturnsNullEverywhereOnACleanFixture() throws Exception {
		final var root = parseFixture(CLEAN_FIXTURE);
		for (var line = 0; line < 200; ++line) {
			for (var column = 0; column < 120; ++column)
				assertNull(JitInefficiencyCheck.categorizeAt(root, line, column));
		}
	}

	@Test
	public void categorizeAtReturnsNullWhereNoNodeIsDetectable() throws Exception {
		assertNull(JitInefficiencyCheck.categorizeAt(parseFixture(VIOLATION_FIXTURE), 0, 0));
	}

	@MethodSource("decodedSplitLiterals")
	@ParameterizedTest
	public void splitLiteralClassifiesTheDecodedValue(@Nonnull String literalText, boolean expected) {
		assertEquals(expected, JitInefficiencyCheck.splitLiteralTakesFastPath(literalText));
	}

	@MethodSource("fastSplitValues")
	@ParameterizedTest
	public void splitLiteralTakesFastPathOnBothRuntimes(@Nonnull String value) {
		assertTrue(JitInefficiencyCheck.splitLiteralTakesFastPath(literalOf(value)));
	}

	@MethodSource("slowSplitValues")
	@ParameterizedTest
	public void splitLiteralTakesNoFastPathOnSomeRuntime(@Nonnull String value) {
		assertFalse(JitInefficiencyCheck.splitLiteralTakesFastPath(literalOf(value)));
	}
}