package com.etk2000.checkstyle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Structural tripwire for the permanent comment-node opt-out. The slices in
 * {@code inputs/infiniteemptyloop/} already fail if the check opts in, but they report it as a
 * pile of missing violations; this names the cause directly, and also fires if the opt-in arrives
 * through {@code AbstractAstCheck} rather than through this check.
 */
public class InfiniteEmptyLoopCheckTest {
	@Test
	public void testDoesNotRequireCommentNodes() {
		assertFalse(
				new InfiniteEmptyLoopCheck().isCommentNodesRequired(),
				"comment nodes silence a guaranteed hang, see the class javadoc"
		);
	}
}