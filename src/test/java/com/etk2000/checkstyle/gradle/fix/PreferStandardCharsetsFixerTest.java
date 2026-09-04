package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferStandardCharsetsCheck;
import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct calls pairing a violation key with text the check never reports it on, the shape a stale
 * buffer hands the fixer. The fixed pairings live in the topic's slices.
 */
public class PreferStandardCharsetsFixerTest {
	private static final String TOPIC = "preferstandardcharsets";

	@CheckReturnValue
	@Nonnull
	private static List<String> wrapped(@Nonnull String statement) {
		return List.of("class T {", "\tObject m(String s) throws Exception {", statement, "\t}", "}");
	}

	private final CheckstyleFixer fixer = new PreferStandardCharsetsFixer();

	private void assertSkip(@Nonnull String expectedReason, @Nullable FixAttempt attempt) {
		assertEquals(expectedReason, assertInstanceOf(SkipResult.class, attempt).reason());
	}

	@Nullable
	private FixAttempt fixUnderKey(@Nonnull String key, @Nonnull List<String> lines, int lineIndex, int column) {
		FixContext.setViolation(new Violation(1, "bundle", key, new Object[0], null, getClass(), key));
		try {
			return fixer.fix(lines, lineIndex, column);
		}
		finally {
			FixContext.clearViolation();
		}
	}

	/** No lint-clean slice can leave a single-argument call's closing paren alone on the next line. */
	@Test
	public void testForNameKeyClosingParenOnTheNextLineSkipsMultiline() {
		final var statement = "\t\treturn Charset.forName(\"UTF-8\"";
		final var lines = List.of("class T {", "\tObject m() {", statement, "\t\t);", "\t}", "}");
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_FOR_NAME_MULTILINE,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, lines, 2, statement.indexOf('"'))
		);
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 9})
	public void testForNameKeyColumnOffTheLineSkipsUnproven(int column) {
		final var lines = List.of("class T {", "\tObject m() {", "\t\treturn Charset.forName(", "\"UTF-8\");", "\t}", "}");
		assertEquals(9, lines.get(3).length());
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, lines, 3, column)
		);
	}

	@Test
	public void testForNameKeyColumnOnTheReceiverSkipsUnproven() {
		final var statement = "\t\treturn Charset.forName(\"UTF-8\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped(statement), 2, statement.indexOf('C'))
		);
	}

	@Test
	public void testForNameKeyLineIndexOffTheBufferSkipsUnproven() {
		final var lines = wrapped("\t\treturn Charset.forName(\"UTF-8\");");
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, lines, lines.size(), 0)
		);
	}

	@Test
	public void testForNameKeyNegativeLineIndexSkipsUnproven() {
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped("\t\treturn Charset.forName(\"UTF-8\");"), -1, 0)
		);
	}

	@Test
	public void testForNameKeyOnALiteralOpeningItsLineSkipsMultiline() {
		final var lines = List.of("class T {", "\tObject m() {", "\t\treturn Charset.forName(", "\"UTF-8\");", "\t}", "}");
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_FOR_NAME_MULTILINE,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, lines, 3, 0)
		);
	}

	@Test
	public void testForNameKeyOnALiteralOutsideAForNameCallSkipsUnproven() {
		final var statement = "\t\treturn s.getBytes(\"UTF-8\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"\t\treturn \"UTF-8\";", "\t\treturn Charset.forName(\"x\" + \"UTF-8\");"})
	public void testForNameKeyOnALiteralThatIsNotAWholeArgumentSkipsUnproven(@Nonnull String statement) {
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped(statement), 2, statement.lastIndexOf("\"UTF-8\""))
		);
	}

	@Test
	public void testForNameKeyOnAnUnknownCharsetNameSkipsUnproven() {
		final var statement = "\t\treturn Charset.forName(\"windows-1252\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testForNameKeyOnAnUnparseableBufferSkipsUnproven() {
		final var statement = "\t\treturn Charset.forName(\"UTF-8\";";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_FOR_NAME, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testLiteralKeySkipsUnproven() {
		final var statement = "\t\treturn s.equals(\"UTF-8\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_LITERAL, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testNoKeyOnAForNameCallSkipsUnproven() {
		final var statement = "\t\treturn Charset.forName(\"UTF-8\");";
		assertSkip(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN, fixer.fix(wrapped(statement), 2, statement.indexOf('"')));
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, 29})
	public void testSpecificKeyColumnOffTheLineReturnsNull(int column) {
		final var statement = "\t\treturn s.getBytes(\"UTF-8\");";
		assertEquals(29, statement.length());
		assertNull(fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, column));
	}

	@Test
	public void testSpecificKeyOnAForNameArgumentSkipsUnproven() {
		final var statement = "\t\treturn Charset.forName(\"UTF-8\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testSpecificKeyOnAnEmptyCharsetNameSkips() {
		final var statement = "\t\treturn s.getBytes(\"\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testSpecificKeyOnAnIdentifierSkipsUnproven() {
		final var statement = "\t\treturn s.getBytes(name);";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf("name"))
		);
	}

	@Test
	public void testSpecificKeyOnAnUnknownCharsetNameSkips() {
		final var statement = "\t\treturn s.getBytes(\"windows-1252\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"\t\treturn s.getBytes(\"UTF-8\";", "\t\treturn s.getBytes(name); // \"UTF-8\""})
	public void testSpecificKeyOnAQuoteOutsideAnyLiteralSkipsUnproven(@Nonnull String statement) {
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testSpecificKeyUnclosedQuoteReturnsNull() {
		final var statement = "\t\treturn s.getBytes(\"UTF-8";
		assertNull(fixUnderKey(PreferStandardCharsetsCheck.MSG_SPECIFIC, wrapped(statement), 2, statement.indexOf('"')));
	}

	@Test
	public void testStringKeyOnALiteralSkipsVariable() {
		final var statement = "\t\treturn s.getBytes(\"UTF-8\");";
		assertSkip(
				SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_VARIABLE,
				fixUnderKey(PreferStandardCharsetsCheck.MSG_GENERIC, wrapped(statement), 2, statement.indexOf('"'))
		);
	}

	@Test
	public void testUnclosedQuoteWithoutAViolationKeySkipsUnproven() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "unclosed_quote");
		final var t = fx.firstTarget();
		assertSkip(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN, fixer.fix(new ArrayList<>(fx.inputLines()), t.line(), t.column()));
	}
}