package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.gradle.fix.LineLength;

import org.junit.jupiter.api.Test;

/**
 * Direct tests for {@link LineCollapse}, whose line array is a caller-supplied parameter: the bounds
 * guards and the tight-versus-loose join divergence are not reachable from the AST-derived spans the
 * check and the move planners pass.
 */
public class LineCollapseTest {
	private static final String[] LINES = {"alpha", "beta", "gamma", "delta"};

	@Test
	public void fitsOnOneLineAcceptsToLineAtEnd() {
		assertTrue(LineCollapse.fitsOnOneLine(LINES, 1, LINES.length));
	}

	/**
	 * The joiners emit the raw lines, comments included, so a comment has to count against the limit
	 * here. Measured with comments masked, the two spans below are indistinguishable and the second one
	 * is accepted, then comes back over the limit once the comment is re-emitted.
	 */
	@Test
	public void fitsOnOneLineCountsCommentWidth() {
		final var head = "alpha";
		final var code = "x".repeat(LineLength.MAX_LINE_LENGTH - head.length() - 1);
		assertTrue(LineCollapse.fitsOnOneLine(new String[]{head, code}, 1, 2));
		assertFalse(LineCollapse.fitsOnOneLine(new String[]{head, code + " // and now it is too wide"}, 1, 2));
	}

	@Test
	public void fitsOnOneLineRejectsFromLineAfterToLine() {
		assertFalse(LineCollapse.fitsOnOneLine(LINES, 3, 2));
	}

	@Test
	public void fitsOnOneLineRejectsFromLineBelowOne() {
		assertFalse(LineCollapse.fitsOnOneLine(LINES, 0, 2));
	}

	@Test
	public void fitsOnOneLineRejectsToLinePastEnd() {
		assertFalse(LineCollapse.fitsOnOneLine(LINES, 1, LINES.length + 1));
	}

	@Test
	public void fitsOnOneLineSingleLineSpanFits() {
		assertTrue(LineCollapse.fitsOnOneLine(LINES, 1, 1));
	}

	@Test
	public void tightModeSkipsBlankLineLooseModeSpendsColumns() {
		// tight skips the blank line entirely while loose spends a column on it, so a span sized to
		// exactly MAX_LINE_LENGTH under the tight join is one column over under the loose one
		final var head = "alpha";
		final var pad = "x".repeat(LineLength.MAX_LINE_LENGTH - head.length() - 1);
		final String[] lines = {head, "", pad};
		assertTrue(LineCollapse.fitsOnOneLine(lines, 1, 3, true));
		assertFalse(LineCollapse.fitsOnOneLine(lines, 1, 3));
	}
}