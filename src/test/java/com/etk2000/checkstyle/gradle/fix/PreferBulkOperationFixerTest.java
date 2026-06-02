package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class PreferBulkOperationFixerTest {
	private static final String TOPIC = "preferbulkoperation";

	private final CheckstyleFixer fixer = new PreferBulkOperationFixer();

	@Test
	public void testArrayCopyIdentSuffixedRhsRejected() throws Exception {
		assertSkip(fixer, TOPIC, "array_copy_ident_suffixed_rhs_rejected");
	}

	@Test
	public void testArrayCopyUnmatchedCloseBracketRejected() throws Exception {
		assertSkip(fixer, TOPIC, "array_copy_unmatched_close_bracket_rejected");
	}

	@Test
	public void testArrayFillEmptyValueReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "array_fill_empty_value_returns_null");
	}

	@Test
	public void testForEachAddAllBracelessMissingCloseParenReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_add_all_braceless_missing_close_paren_returns_null");
	}

	@Test
	public void testForEachAddAllBracelessMissingTargetReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_add_all_braceless_missing_target_returns_null");
	}

	@Test
	public void testForEachLambdaBailsOnEmptyBlockBodyBeforePut() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_lambda_bails_on_empty_block_body_before_put");
	}

	@Test
	public void testForEachLambdaEmptySourceReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_lambda_empty_source_returns_null");
	}

	@Test
	public void testForEachLambdaReturnNullMultiLineUnclosed() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_lambda_return_null_multi_line_unclosed");
	}

	@Test
	public void testForEachLambdaSourceEndsWithDotReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_lambda_source_ends_with_dot_returns_null");
	}

	@Test
	public void testForEachLambdaSourceStartsWithDotReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_lambda_source_starts_with_dot_returns_null");
	}

	@Test
	public void testForEachMethodRefEmptyTargetReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_method_ref_empty_target_returns_null");
	}

	@Test
	public void testForEachMethodRefNoCloseParenReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "for_each_method_ref_no_close_paren_returns_null");
	}

	@Test
	public void testGuardBracedUnclosed() throws Exception {
		assertSkip(fixer, TOPIC, "guard_braced_unclosed");
	}

	@Test
	public void testGuardBracelessNoSemicolon() throws Exception {
		assertSkip(fixer, TOPIC, "guard_braceless_no_semicolon");
	}

	@Test
	public void testIndexedAddAllMissingTargetReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "indexed_add_all_missing_target_returns_null");
	}

	@Test
	public void testPutAllEntrySetMissingTargetReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "put_all_entry_set_missing_target_returns_null");
	}
}