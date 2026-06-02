package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Direct unit tests for the package-private helpers of
 * {@link FullPipelineRegressionTest}: {@code findEndMarker},
 * {@code findExactLine}, {@code stripImportsDirectives}.
 */
public class FullPipelineRegressionTestHelpersTest {
	@Test
	public void findEndMarker_leadingWhitespaceMatches() {
		final var lines = List.of(
				"// === case: foo ===",
				"body",
				"\t  // === end ==="
		);
		assertEquals(2, FullPipelineRegressionTest.findEndMarker(lines, 1));
	}

	@Test
	public void findEndMarker_noTerminator_returnsMinusOne() {
		final var lines = List.of(
				"// === case: foo ===",
				"first body line",
				"second body line"
		);
		assertEquals(-1, FullPipelineRegressionTest.findEndMarker(lines, 1));
	}

	@Test
	public void findEndMarker_returnsIndexOfTerminator() {
		final var lines = List.of(
				"// === case: foo ===",
				"body",
				"// === end ===",
				"// extra trailing content"
		);
		assertEquals(2, FullPipelineRegressionTest.findEndMarker(lines, 1));
	}

	@Test
	public void findExactLine_leadingWhitespaceLineMatches() {
		final var lines = List.of(
				"line 0",
				"\t  // === case: foo ===",
				"line 2"
		);
		assertEquals(1, FullPipelineRegressionTest.findExactLine(lines, "// === case: foo ==="));
	}

	@Test
	public void findExactLine_returnsIndexOfExactMatch() {
		final var lines = List.of(
				"line 0",
				"// === case: foo ===",
				"line 2"
		);
		assertEquals(1, FullPipelineRegressionTest.findExactLine(lines, "// === case: foo ==="));
	}

	@Test
	public void findExactLine_substringOnly_returnsMinusOne() {
		final var lines = List.of(
				"line 0",
				"// reference: // === case: foo === inside a comment",
				"line 2"
		);
		assertEquals(-1, FullPipelineRegressionTest.findExactLine(lines, "// === case: foo ==="));
	}

	@Test
	public void stripImportsDirectives_dropsMatchingLines() {
		final var content = String.join(
				"\n",
				"line 0",
				"",
				"// imports: a.b.C",
				"line 3"
		);
		final var expected = String.join("\n", "line 0", "", "line 3");
		assertEquals(expected, FullPipelineRegressionTest.stripImportsDirectives(content));
	}

	@Test
	public void stripImportsDirectives_emptyContentReturnsEmpty() {
		assertEquals("", FullPipelineRegressionTest.stripImportsDirectives(""));
	}

	@Test
	public void stripImportsDirectives_leadingWhitespaceMatches() {
		final var content = String.join(
				"\n",
				"line 0",
				"\t  // imports: a.b.C",
				"line 2"
		);
		final var expected = String.join("\n", "line 0", "line 2");
		assertEquals(expected, FullPipelineRegressionTest.stripImportsDirectives(content));
	}

	@Test
	public void stripImportsDirectives_noDirectivesReturnsIdentical() {
		final var content = String.join(
				"\n",
				"line 0",
				"line 1",
				"",
				"line 3"
		);
		assertEquals(content, FullPipelineRegressionTest.stripImportsDirectives(content));
	}

	@Test
	public void stripImportsDirectives_preservesAdjacentBlankLines() {
		final var content = String.join(
				"\n",
				"line 0",
				"",
				"// imports: a.b.C",
				"",
				"line 4"
		);
		final var expected = String.join("\n", "line 0", "", "", "line 4");
		assertEquals(expected, FullPipelineRegressionTest.stripImportsDirectives(content));
	}

	@Test
	public void stripImportsDirectives_substringOnlyKept() {
		final var content = String.join(
				"\n",
				"trailing text containing // imports: foo not at column 0",
				"// reference to // imports: bar inside another comment",
				"line 2"
		);
		assertEquals(content, FullPipelineRegressionTest.stripImportsDirectives(content));
	}
}