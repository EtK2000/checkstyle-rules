package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class NoBlankLineBetweenSingleCasesFixerTest {
	private static final String TOPIC = "noblanklinebetweensinglecases";

	private final CheckstyleFixer fixer = new NoBlankLineBetweenSingleCasesFixer();

	@Test
	public void testBlankLinesWithWhitespace() throws Exception {
		// can't migrate: probes fixer's isBlank() handling of whitespace-only lines; class-wrapped slice would contain whitespace-only lines that violate the project's NoTrailingWhitespace lint, which scans cases.in.java
		assertSimpleFix(fixer, TOPIC, "blank_lines_with_whitespace");
	}

	@Test
	public void testCommentOnlyGapIsStale() throws Exception {
		assertSkipResult(fixer, TOPIC, "comment_only_gap_is_stale", SkipMessages.NO_BLANK_CASES_SKIP_STALE);
	}

	@Test
	public void testFirstLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "first_line", SkipMessages.NO_BLANK_CASES_SKIP_STALE);
	}

	@Test
	public void testLineIndexOutsideTheBufferIsOutOfBounds() {
		final var lines = List.of("}", "", "");
		for (var lineIndex : new int[]{-1, lines.size(), lines.size() + 1})
			assertEquals(new SkipResult(SkipMessages.FIX_BOUNDS), fixer.fix(lines, lineIndex, 0), "lineIndex " + lineIndex);
	}

	@Test
	public void testMixedBlankAndWhitespaceLines() throws Exception {
		// can't migrate: combines whitespace-only lines (NoTrailingWhitespace lint violation) and multiple consecutive blank lines (NoDoubleBlankLines lint violation) in cases.in.java
		assertSimpleFix(fixer, TOPIC, "mixed_blank_and_whitespace_lines");
	}

	@Test
	public void testNoBlankLinesAbove() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_blank_lines_above", SkipMessages.NO_BLANK_CASES_SKIP_STALE);
	}

	@Test
	public void testRemoveMultipleBlankLines() throws Exception {
		// can't migrate: requires multiple consecutive blank lines in cases.in.java, which violates the project's NoDoubleBlankLines lint
		assertSimpleFix(fixer, TOPIC, "remove_multiple_blank_lines");
	}
}