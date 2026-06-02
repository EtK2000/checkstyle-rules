package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class PreferStandardCharsetsFixerTest {
	private static final String TOPIC = "preferstandardcharsets";

	private final CheckstyleFixer fixer = new PreferStandardCharsetsFixer();

	@Test
	public void testUnclosedQuoteReturnsNull() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "unclosed_quote");
		final var t = fx.firstTarget();
		assertNull(fixer.fix(new ArrayList<>(fx.inputLines()), t.line(), t.column()));
	}
}