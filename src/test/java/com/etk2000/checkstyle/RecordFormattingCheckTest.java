package com.etk2000.checkstyle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Structural tripwire for the comment-node opt-in. The slices in {@code inputs/recordformatting/}
 * already fail if the check opts out, but they report it as a pile of marker mismatches spread over
 * the violation and clean files; this names the cause directly.
 */
public class RecordFormattingCheckTest {
	@Test
	public void testRequiresCommentNodes() {
		assertTrue(
				new RecordFormattingCheck().isCommentNodesRequired(),
				"a comment-only body must read as non-empty, see the class javadoc"
		);
	}
}