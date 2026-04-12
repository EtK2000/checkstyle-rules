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

public class PreferStandardCharsetsFixerTest {
	private final CheckstyleFixer fixer = new PreferStandardCharsetsFixer();

	@CsvSource({
			"UTF-8,       UTF_8",
			"utf-8,       UTF_8",
			"UTF8,        UTF_8",
			"utf8,        UTF_8",
			"Utf-8,       UTF_8",
			"ISO-8859-1,  ISO_8859_1",
			"latin1,      ISO_8859_1",
			"US-ASCII,    US_ASCII",
			"ASCII,       US_ASCII",
			"UTF-16,      UTF_16",
			"UTF-16BE,    UTF_16BE",
			"UTF-16LE,    UTF_16LE"
	})
	@ParameterizedTest
	public void testCharsetReplacement(String charsetName, String constant) {
		final var input = "\t\tfinal var bytes = s.getBytes(\"" + charsetName + "\");";
		final var lines = new ArrayList<>(List.of(input));
		final var column = input.indexOf('"');
		final var result = fixer.fix(lines, 0, column);
		assertNotNull(result);
		assertEquals(
				"\t\tfinal var bytes = s.getBytes(StandardCharsets." + constant.strip() + ");",
				result.replacement().getFirst()
		);
		assertEquals(Set.of("java.nio.charset.StandardCharsets"), result.importsToAdd());
	}

	@Test
	public void testColumnNotOnQuoteReturnsNull() {
		final var lines = new ArrayList<>(List.of("\t\tfinal var bytes = s.getBytes(charset);"));
		assertNull(fixer.fix(lines, 0, 31));
	}

	@Test
	public void testUnknownCharsetReturnsNull() {
		final var input = "\t\tfinal var bytes = s.getBytes(\"Windows-1252\");";
		final var lines = new ArrayList<>(List.of(input));
		assertNull(fixer.fix(lines, 0, input.indexOf('"')));
	}
}