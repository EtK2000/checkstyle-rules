package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class FullPipelineRunnerTest {
	@Test
	public void stripViolationCommentsLineWithoutMarkerUnchanged() {
		assertEquals("class T {}", FullPipelineRunner.stripViolationComments("class T {}"));
	}

	@Test
	public void stripViolationCommentsMarkerAtLineStartFullyStrips() {
		assertEquals("", FullPipelineRunner.stripViolationComments("\t\t// violation: msg"));
	}

	@Test
	public void stripViolationCommentsMixedLinesStripsOnlyMarkerLines() {
		final var input = "class T {\n\tint x; // violation: msg\n\tint y;\n}";
		final var expected = "class T {\n\tint x;\n\tint y;\n}";
		assertEquals(expected, FullPipelineRunner.stripViolationComments(input));
	}

	@Test
	public void stripViolationCommentsMultipleMarkersOnOneLineStripsAll() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x; // violation: a // violation: b"));
	}

	@Test
	public void stripViolationCommentsOpenerMarker() {
		assertEquals("\t\t\"\"\";", FullPipelineRunner.stripViolationComments("\t\t\"\"\"; // violation@opener: msg"));
	}

	@Test
	public void stripViolationCommentsPlainMarker() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x; // violation: msg"));
	}

	@Test
	public void stripViolationCommentsPredicateMarker() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x; // violation [minSdk>=35]: msg"));
	}

	@Test
	public void stripViolationCommentsTrailingWhitespaceBeforeMarkerRemoved() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x;   \t// violation: msg"));
	}

	@Test
	public void stripViolationCommentsWarningQualifier() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x; // violation (warning): msg"));
	}

	@Test
	public void stripViolationCommentsWarningWithPredicate() {
		assertEquals("x;", FullPipelineRunner.stripViolationComments("x; // violation (warning) [minSdk>=35]: msg"));
	}
}