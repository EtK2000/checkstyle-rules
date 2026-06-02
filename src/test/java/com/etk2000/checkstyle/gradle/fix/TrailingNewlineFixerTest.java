package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.applyFixResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class TrailingNewlineFixerTest {
	private static final String TOPIC = "trailingnewline";

	private final CheckstyleFixer fixer = new TrailingNewlineFixer();

	@Test
	public void testAllBlankLines() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "all_blank_lines");
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(lines, t.line(), t.column())
		);
		assertTrue(result.importsToAdd().isEmpty());
		applyFixResult(lines, result);
		assertEquals(fx.fixedLines(), lines);
	}

	@Test
	public void testDeleteMultipleTrailingEmptyLines() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "delete_multiple_trailing_empty_lines");
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(lines, t.line(), t.column())
		);
		assertTrue(result.importsToAdd().isEmpty());
		applyFixResult(lines, result);
		assertEquals(fx.fixedLines(), lines);
	}

	@Test
	public void testDeleteSingleTrailingEmptyLine() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "delete_single_trailing_empty_line");
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(lines, t.line(), t.column())
		);
		assertTrue(result.importsToAdd().isEmpty());
		applyFixResult(lines, result);
		assertEquals(fx.fixedLines(), lines);
	}

	@Test
	public void testDeleteTrailingWhitespaceOnlyLine() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "delete_trailing_whitespace_only_line");
		final var t = fx.firstTarget();
		final var lines = new ArrayList<>(fx.inputLines());
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(lines, t.line(), t.column())
		);
		assertTrue(result.importsToAdd().isEmpty());
		applyFixResult(lines, result);
		assertEquals(fx.fixedLines(), lines);
	}

	@Test
	public void testEmptyLineListReturnsNull() {
		assertNull(fixer.fix(new ArrayList<>(), 0, 0));
	}

	@Test
	public void testLastLineHasContent() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "last_line_has_content");
		final var t = fx.firstTarget();
		assertNull(fixer.fix(new ArrayList<>(fx.inputLines()), t.line(), t.column()));
	}
}