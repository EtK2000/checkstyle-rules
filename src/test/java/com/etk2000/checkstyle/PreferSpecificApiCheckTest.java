package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import javax.annotation.Nonnull;

public class PreferSpecificApiCheckTest {
	/**
	 * The unicode escapes whose char literal javac destroys before it reads it, in the
	 * one-or-more-{@code u} spelling JLS 3.3 allows and in both hex cases.
	 *
	 * <p>Only the apostrophe's escape is reachable from a fixture. A string literal holding
	 * nothing but the backslash's own unicode escape is unterminated once JLS 3.3 has run, so
	 * javac rejects the input before any of this applies and no compilable slice can carry it;
	 * this seam is where those rows are driven from.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"\\u0027", "\\u005C", "\\u005c", "\\uu0027", "\\uuuu005C"})
	public void breakingUnicodeEscapesAreRefused(@Nonnull String content) {
		assertTrue(PreferSpecificApiCheck.unicodeEscapeBreaksCharLiteral(content));
	}

	/**
	 * This lives outside the topic's own fixtures because the project style forbids an
	 * explicitly typed for-each variable.
	 */
	@Test
	public void loopVariableReceiverTypeIsResolved() throws Exception {
		final var events = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class,
				"prefervar/InputForEachReceiverScope.java",
				"minSdk",
				"35"
		);
		assertEquals(List.of(), events.stream().map(e -> e.getLine() + ":" + e.getMessage()).toList());
	}

	/**
	 * The boundary partners: a unicode escape for anything else, the octal escapes for the same two
	 * characters (decoded with the token, not ahead of it, so they survive), the characters written
	 * plainly, and the shapes that are not a unicode escape at all.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"\\u0041", "\\u0022", "\\u0026", "\\u0028", "\\u005B", "\\u005D",
			"\\47", "\\134", "\\\\", "\\'", "\\n", "a", "", "\\", "\\u", "\\u002",
			"\\u00270", "u0027", "\\0027"
	})
	public void nonBreakingEscapesAreAccepted(@Nonnull String content) {
		assertFalse(PreferSpecificApiCheck.unicodeEscapeBreaksCharLiteral(content));
	}
}