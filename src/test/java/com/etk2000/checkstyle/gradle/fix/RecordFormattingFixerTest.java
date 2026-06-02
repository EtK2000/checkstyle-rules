package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseFixMultiViolation;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import com.etk2000.checkstyle.RecordFormattingCheck;

import org.junit.jupiter.api.Test;

public class RecordFormattingFixerTest {
	private static final String TOPIC = "recordformatting";

	private final CheckstyleFixer fixer = new RecordFormattingFixer();

	@Test
	public void testFixComponentBitshiftInAnnotation() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_bitshift_in_annotation");
	}

	@Test
	public void testFixComponentBlockCommentWithCommaInAnnotation() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_block_comment_with_comma_in_annotation");
	}

	@Test
	public void testFixComponentCollapseMixedToSingleLine() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_collapse_mixed_to_single_line");
	}

	@Test
	public void testFixComponentCommaInAnnotationParens() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_comma_in_annotation_parens");
	}

	@Test
	public void testFixComponentEmptyLeadingComponentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_empty_leading_component_returns_null");
	}

	@Test
	public void testFixComponentEmptyMiddleComponentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_empty_middle_component_returns_null");
	}

	@Test
	public void testFixComponentExpandWideLineToStyleB() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_expand_wide_line_to_style_b");
	}

	@Test
	public void testFixComponentGreaterEqualInAnnotation() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_greater_equal_in_annotation");
	}

	@Test
	public void testFixComponentGreaterThanInAnnotationInsideGenerics() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_greater_than_in_annotation_inside_generics");
	}

	@Test
	public void testFixComponentLessThanInAnnotation() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_less_than_in_annotation");
	}

	@Test
	public void testFixComponentLineCommentOnRparenLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_line_comment_on_rparen_line_returns_null");
	}

	@Test
	public void testFixComponentNestedGenerics() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_nested_generics");
	}

	@Test
	public void testFixComponentRecordKeywordAfterSupplementaryIdentCharReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_record_keyword_after_supplementary_ident_char_returns_null");
	}

	@Test
	public void testFixComponentRightShiftInAnnotation() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_right_shift_in_annotation");
	}

	@Test
	public void testFixComponentTrailingCommaReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_trailing_comma_returns_null");
	}

	@Test
	public void testFixComponentUnbalancedAngleBracketsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_unbalanced_angle_brackets_returns_null");
	}

	@Test
	public void testFixComponentUnterminatedBlockCommentInHeaderReturnsNull() throws Exception {
		assertSkipResult(fixer, TOPIC, "fix_component_unterminated_block_comment_in_header_returns_null", "cannot reformat a record header that spans a multi-line comment or text block");
	}

	@Test
	public void testFixComponentUnterminatedStringHidingRecordKeyword() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_unterminated_string_hiding_record_keyword");
	}

	@Test
	public void testFixComponentUnterminatedStringInHeaderReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fix_component_unterminated_string_in_header_returns_null");
	}

	@Test
	public void testFixComponentWithAnnotationComparisonInGenericBound() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_annotation_comparison_in_generic_bound");
	}

	@Test
	public void testFixComponentWithAnnotationGreaterThanInGenericBound() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_annotation_greater_than_in_generic_bound");
	}

	@Test
	public void testFixComponentWithBlockCommentInHeader() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_block_comment_in_header");
	}

	@Test
	public void testFixComponentWithBoundedGeneric() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_bounded_generic");
	}

	@Test
	public void testFixComponentWithGenericTypeArg() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_generic_type_arg");
	}

	@Test
	public void testFixComponentWithStringContainingRecordOnPriorLine() throws Exception {
		assertCaseFixMultiViolation(RecordFormattingCheck.class, fixer, TOPIC, "fix_component_with_string_containing_record_on_prior_line");
	}

	@Test
	public void testFixEmptyBodyCloseOnLineAfterUnterminatedString() throws Exception {
		assertSkipResult(fixer, TOPIC, "fix_empty_body_close_on_line_after_unterminated_string", "cannot collapse empty record body without losing surrounding content");
	}

	@Test
	public void testFixOpenBraceAfterUnterminatedBlockComment() throws Exception {
		assertSkipResult(fixer, TOPIC, "fix_open_brace_after_unterminated_block_comment", "anchor line ends in a comment or unterminated literal");
	}

	@Test
	public void testFixOpenBraceAfterUnterminatedCharLiteral() throws Exception {
		assertSkipResult(fixer, TOPIC, "fix_open_brace_after_unterminated_char_literal", "anchor line ends in a comment or unterminated literal");
	}
}