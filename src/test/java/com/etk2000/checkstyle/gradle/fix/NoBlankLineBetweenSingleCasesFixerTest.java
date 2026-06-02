package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class NoBlankLineBetweenSingleCasesFixerTest {
	private static final String TOPIC = "noblanklinebetweensinglecases";

	private final CheckstyleFixer fixer = new NoBlankLineBetweenSingleCasesFixer();

	@Test
	public void testBlankLinesWithWhitespace() throws Exception {
		// can't migrate: probes fixer's isBlank() handling of whitespace-only lines; class-wrapped slice would contain whitespace-only lines that violate the project's NoTrailingWhitespace lint, which scans cases.in.java
		assertSimpleFix(fixer, TOPIC, "blank_lines_with_whitespace");
	}

	@Test
	public void testFirstLine() throws Exception {
		assertSkip(fixer, TOPIC, "first_line");
	}

	@Test
	public void testMixedBlankAndWhitespaceLines() throws Exception {
		// can't migrate: combines whitespace-only lines (NoTrailingWhitespace lint violation) and multiple consecutive blank lines (NoDoubleBlankLines lint violation) in cases.in.java
		assertSimpleFix(fixer, TOPIC, "mixed_blank_and_whitespace_lines");
	}

	@Test
	public void testNoBlankLinesAbove() throws Exception {
		assertSkip(fixer, TOPIC, "no_blank_lines_above");
	}

	@Test
	public void testRemoveMultipleBlankLines() throws Exception {
		// can't migrate: requires multiple consecutive blank lines in cases.in.java, which violates the project's NoDoubleBlankLines lint
		assertSimpleFix(fixer, TOPIC, "remove_multiple_blank_lines");
	}
}