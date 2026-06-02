package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class ControlFlowBracesFixerTest {
	private static final String TOPIC = "controlflowbraces";

	private final CheckstyleFixer fixer = new ControlFlowBracesFixer();

	@Test
	public void testCuddledElseUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "cuddled_else_unparseable_buffer_refused", "cannot locate the control-flow body without a parse");
	}

	@Test
	public void testForKeywordUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "for_keyword_unparseable_buffer_refused", "cannot locate the control-flow body without a parse");
	}

	@Test
	public void testKeywordPrefixedIdentifiersRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "keyword_prefixed_identifiers_refused", "no control-flow keyword at the violation site");
	}

	@Test
	public void testNoControlFlowKeywordRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_control_flow_keyword_refused", "no control-flow keyword at the violation site");
	}

	@Test
	public void testNonDoWhileStalePositionRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "non_do_while_stale_position_refused", "no control-flow body at the reported position; an earlier fix in this pass moved it");
	}

	@Test
	public void testNonDoWhileUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "non_do_while_unparseable_buffer_refused", "cannot locate the control-flow body without a parse");
	}

	@Test
	public void testUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "unparseable_buffer_refused", "cannot tell which do-while form the body needs without a parse");
	}

	@Test
	public void testWhileKeywordUnparseableBufferRefused() throws Exception {
		assertSkipResult(fixer, TOPIC, "while_keyword_unparseable_buffer_refused", "cannot locate the control-flow body without a parse");
	}
}