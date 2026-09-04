package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Multi-file runs proving {@link MultilineCallFormattingCheck#beginTree} resets the two pieces of
 * per-file state the check carries. Every other harness entry point runs one file per checker, so
 * these are the only tests that can observe a leak.
 */
public class MultilineCallFormattingStateTest {
	private static final int LINES_LEAK_VIOLATION_LINE = 19;
	private static final String LINES_LEAK_A = "multilinecallformatting/InputMultilineCallLinesLeakA.java";

	@CheckReturnValue
	private static int lineCount(@Nonnull String inputPath) throws Exception {
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		return Files.readAllLines(Path.of(requireNonNull(url, inputPath).toURI())).size();
	}

	@Test
	public void contextReceiverNamesDoNotLeakBetweenFiles() throws Exception {
		// B uses the name `ctx` for a String receiver in the layout that is exempt only when the call
		// counts as a special inline block, so a leaked name turns B's clean call into an opening- and
		// closing-paren violation
		final var events = BaseCheckTest.runCheckOnFiles(
				MultilineCallFormattingCheck.class,
				"multilinecallformatting/InputMultilineCallContextLeakA.java",
				"multilinecallformatting/InputMultilineCallContextLeakB.java"
		);

		assertEquals(0, events.size(), "expected no violations, got " + events);
	}

	@Test
	public void sourceLinesDoNotLeakBetweenFiles() throws Exception {
		// asserted as a clamp rather than an equality so a grown A cannot be repaired by bumping a
		// constant, which is the very edit that would destroy the invariant
		final var linesLeakALineCount = lineCount(LINES_LEAK_A);
		assertEquals(
				linesLeakALineCount,
				Math.min(linesLeakALineCount, LINES_LEAK_VIOLATION_LINE - 1),
				"A must stay shorter than B's violation line " + LINES_LEAK_VIOLATION_LINE
						+ ", or a leaked array still resolves B's span and the leak goes unnoticed"
		);
		final var events = BaseCheckTest.runCheckOnFiles(
				MultilineCallFormattingCheck.class,
				LINES_LEAK_A,
				"multilinecallformatting/InputMultilineCallLinesLeakB.java"
		);

		assertEquals(1, events.size(), "expected only B's violation, got " + events);
		final var event = events.getFirst();
		assertTrue(event.getFileName().endsWith("InputMultilineCallLinesLeakB.java"), event.getFileName());
		assertEquals(LINES_LEAK_VIOLATION_LINE, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals(
				"A simple new JSONObject().put(...) fits on one line; do not split it across lines.",
				event.getMessage()
		);
	}
}