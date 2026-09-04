package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.JavaStringLiterals.Unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Unicode escapes are spelled {@code BS + "u0041"} throughout: written out, javac would translate
 * them in this source before the decoder ever saw them.
 */
public class JavaStringLiteralsTest {
	private static final String BS = "\\";

	@Nonnull
	private static Stream<Arguments> decodedContents() {
		return Stream.of(
				Arguments.of("abc", "abc"),
				Arguments.of("", ""),
				Arguments.of(BS + "b", "\b"),
				Arguments.of(BS + "f", "\f"),
				Arguments.of(BS + "n", "\n"),
				Arguments.of(BS + "r", "\r"),
				Arguments.of(BS + "s", " "),
				Arguments.of(BS + "t", "\t"),
				Arguments.of(BS + "\"", "\""),
				Arguments.of(BS + "'", "'"),
				Arguments.of(BS + BS, BS),
				Arguments.of(BS + "0", String.valueOf((char) 0)),
				Arguments.of(BS + "7", String.valueOf((char) 7)),
				Arguments.of(BS + "77", String.valueOf((char) 63)),
				Arguments.of(BS + "377", String.valueOf((char) 255)),
				Arguments.of(BS + "400", " 0"),
				Arguments.of(BS + "477", "'7"),
				Arguments.of(BS + "0000", String.valueOf((char) 0) + "0"),
				Arguments.of(BS + "8", null),
				Arguments.of(BS + "u0041", "A"),
				Arguments.of(BS + "uuu0041", "A"),
				Arguments.of(BS + "u00e9", String.valueOf((char) 0xE9)),
				Arguments.of(BS + "u00E9", String.valueOf((char) 0xE9)),
				Arguments.of(BS + "u005cn", "\n"),
				Arguments.of(BS + "u005c" + BS + "u005c", BS),
				Arguments.of(BS + "u005c" + BS + "u0027", "'"),
				Arguments.of(BS + BS + "u0041", BS + "u0041"),
				Arguments.of(BS + BS + BS + "u0041", BS + "A"),
				Arguments.of(BS, null),
				Arguments.of("a" + BS, null),
				Arguments.of(BS + "q", null),
				Arguments.of(BS + "u004", null),
				Arguments.of(BS + "u", null),
				Arguments.of(BS + "u00G1", null),
				Arguments.of(BS + "u004" + (char) 0xFF11, null),
				Arguments.of(BS + "u0022", null),
				Arguments.of(BS + "u000a", null),
				Arguments.of(BS + "u000D", null),
				Arguments.of(BS + "u005c", null)
		);
	}

	@MethodSource("decodedContents")
	@ParameterizedTest
	public void decodeReadsTheValue(@Nonnull String content, @Nullable String expected) {
		final var units = JavaStringLiterals.decode(content);
		if (expected == null)
			assertNull(units);
		else {
			final var value = new StringBuilder();
			for (var unit : units)
				value.append(unit.value());
			assertEquals(expected, value.toString());
		}
	}

	@Test
	public void decodeSpansCoverEachSpelling() {
		assertEquals(
				List.of(new Unit('a', 0, 1), new Unit('\n', 1, 3), new Unit('A', 3, 9), new Unit('A', 9, 13), new Unit('b', 13, 14)),
				JavaStringLiterals.decode("a" + BS + "n" + BS + "u0041" + BS + "101b")
		);
		assertEquals(List.of(new Unit('\n', 0, 7)), JavaStringLiterals.decode(BS + "u005cn"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"ab", "\"", "\"a", "a\"", "'a'"})
	public void valueRefusesTextThatIsNotAStringLiteral(@Nonnull String text) {
		assertNull(JavaStringLiterals.value(text));
	}

	@Test
	public void valueStripsTheQuotesAndDecodes() {
		assertEquals("", JavaStringLiterals.value("\"\""));
		assertEquals("a\nb", JavaStringLiterals.value("\"a" + BS + "nb\""));
		assertNull(JavaStringLiterals.value("\"a" + BS + "\""));
	}
}