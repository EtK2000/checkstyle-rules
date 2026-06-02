package com.etk2000.checkstyle.inputs.preferbulkoperation;

// === case: add_all_indexed ===
// imports: java.util.List
class InputPreferBulkOperationAddAllIndexedSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: add_all_indexed_post_inc ===
// imports: java.util.List
class InputPreferBulkOperationAddAllIndexedPostIncSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: array_copy ===
class InputPreferBulkOperationArrayCopySliceViolation {
	void m(int[] dst, int[] src) {
		System.arraycopy(src, 0, dst, 0, src.length);
	}
}
// === end ===

// === case: array_copy_braced ===
class InputPreferBulkOperationArrayCopyBracedSliceViolation {
	void m(int[] dst, int[] src) {
		System.arraycopy(src, 0, dst, 0, src.length);
	}
}
// === end ===

// === case: array_copy_post_inc ===
class InputPreferBulkOperationArrayCopyPostIncSliceViolation {
	void m(int[] dst, int[] src) {
		System.arraycopy(src, 0, dst, 0, src.length);
	}
}
// === end ===

// === case: array_fill ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillSliceViolation {
	void m(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: array_fill_braced ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillBracedSliceViolation {
	void m(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: array_fill_char_literal_close_brace ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillCharLiteralCloseBraceSliceViolation {
	void m(char[] arr) {
		Arrays.fill(arr, '}');
	}
}
// === end ===

// === case: array_fill_multi_line_close_bracket ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillMultiLineCloseBracketSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		Arrays.fill(arr, -a[b[0]]);
	}
}
// === end ===

// === case: array_fill_multi_line_unary_split ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillMultiLineUnarySplitSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		Arrays.fill(arr, - a[b[0]]);
	}
}
// === end ===

// === case: array_fill_multi_line_value ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillMultiLineValueSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		Arrays.fill(arr, -a[b[0]]);
	}
}
// === end ===

// === case: array_fill_non_zero_value ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillNonZeroValueSliceViolation {
	void m(int[] arr) {
		Arrays.fill(arr, -1);
	}
}
// === end ===

// === case: array_fill_post_inc ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillPostIncSliceViolation {
	void m(int[] arr) {
		Arrays.fill(arr, 0);
	}
}
// === end ===

// === case: array_fill_source_has_field_name_starting_with_length ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillSourceHasFieldNameStartingWithLengthSliceViolation {
	static class Holder {
		int[] lengthArray;
	}

	void m(Holder obj) {
		Arrays.fill(obj.lengthArray, 0);
	}
}
// === end ===

// === case: array_fill_source_name_has_length_prefix ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillSourceNameHasLengthPrefixSliceViolation {
	void m(int[] lengthValues) {
		Arrays.fill(lengthValues, 0);
	}
}
// === end ===

// === case: array_fill_string_literal_close_brace ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillStringLiteralCloseBraceSliceViolation {
	void m(String[] arr) {
		Arrays.fill(arr, "}");
	}
}
// === end ===

// === case: array_fill_string_literal_open_brace ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillStringLiteralOpenBraceSliceViolation {
	void m(String[] arr) {
		Arrays.fill(arr, "{");
	}
}
// === end ===

// === case: array_fill_string_value_with_escaped_quote ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillStringValueWithEscapedQuoteSliceViolation {
	void m(String[] arr) {
		Arrays.fill(arr, "a\"b");
	}
}
// === end ===

// === case: array_fill_string_value_with_semicolon ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillStringValueWithSemicolonSliceViolation {
	void m(String[] arr) {
		Arrays.fill(arr, "a;b");
	}
}
// === end ===

// === case: array_fill_string_value_with_slashes ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillStringValueWithSlashesSliceViolation {
	void m(String[] arr) {
		Arrays.fill(arr, "http://x");
	}
}
// === end ===

// === case: array_fill_unary_plus_constant ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillUnaryPlusConstantSliceViolation {
	void m(int[] arr, int[] other) {
		Arrays.fill(arr, +other[0]);
	}
}
// === end ===

// === case: array_fill_value_contains_bracket_not_arraycopy ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillValueContainsBracketNotArraycopySliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		Arrays.fill(arr, -a[b[0]]);
	}
}
// === end ===

// === case: array_fill_value_contains_unary_plus_and_bracket ===
// imports: java.util.Arrays
class InputPreferBulkOperationArrayFillValueContainsUnaryPlusAndBracketSliceViolation {
	void m(int[] arr, int[] otherArr) {
		Arrays.fill(arr, +otherArr[0]);
	}
}
// === end ===

// === case: for_each_add_all_braced ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracedSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: for_each_add_all_braceless ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracelessSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: for_each_add_all_braceless_with_trailing_comment ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracelessWithTrailingCommentSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: for_each_add_all_method_call_source ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferBulkOperationForEachAddAllMethodCallSourceSliceViolation {
	void m(List<String> target, Map<String, String> map) {
		target.addAll(map.values());
	}
}
// === end ===

// === case: for_each_add_all_multi_line_dotted_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllMultiLineDottedSourceSliceViolation {
	void m(List<String> target, List<String> src) {
		target.addAll(src.subList(0, 1));
	}
}
// === end ===

// === case: for_each_add_all_multi_line_paren_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllMultiLineParenSourceSliceViolation {
	void m(List<String> target, int a, int b) {
		target.addAll(getList(a, b));
	}
}
// === end ===

// === case: for_each_add_all_nested_paren_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllNestedParenSourceSliceViolation {
	void m(List<String> target, int a, int b) {
		target.addAll(getList(a, b));
	}
}
// === end ===

// === case: for_each_lambda_add_all ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_block_body_multi_line ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllBlockBodyMultiLineSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetSliceViolation {
	void m(List<String> list, Object o) {
		((List) o).addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target_generic ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetGenericSliceViolation {
	void m(List<String> list, Object o) {
		((List<String>) o).addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target_parenthesized_operand ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetParenthesizedOperandSliceViolation {
	void m(List<String> list, Object o) {
		((List) (o)).addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_complex_ternary_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllComplexTernaryTargetSliceViolation {
	void m(List<String> list, List<String> a, List<String> b, boolean cond) {
		(cond ? a : b).addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_dotted_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllDottedTargetSliceViolation {
	List<String> other;

	void m(List<String> list) {
		this.other.addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_add_all_parenthesized ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllParenthesizedSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_lambda_block_body_multi_line_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyMultiLinePutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_multiple_comment_lines ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyMultipleCommentLinesSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_containing_put ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentContainingPutSliceViolation {
	void m(Map<String, String> source, Map<String, String> real) {
		real.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_no_star_prefix ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentNoStarPrefixSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_star_prefix ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentStarPrefixSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_line_comment ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithLineCommentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_whitespace_lines ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithWhitespaceLinesSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_nested_for_each_inner_fixed ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaNestedForEachInnerFixedSliceViolation {
	void m(Map<String, List<String>> map, List<String> target) {
		map.forEach((k, v) -> target.addAll(v));
	}
}
// === end ===

// === case: for_each_lambda_preserves_leading_if_statement ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPreservesLeadingIfStatementSliceViolation {
	void m(boolean flag, Map<String, String> source, Map<String, String> target) {
		if (flag) target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_put_all_complex_ternary_target ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllComplexTernaryTargetSliceViolation {
	void m(Map<String, String> source, Map<String, String> a, Map<String, String> b, boolean cond) {
		(cond ? a : b).putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_put_all_dotted_target ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllDottedTargetSliceViolation {
	Map<String, String> target;

	void m(Map<String, String> source) {
		this.target.putAll(source);
	}
}
// === end ===

// === case: for_each_lambda_put_all_preserves_trailing_content ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllPreservesTrailingContentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source); done();
	}
}
// === end ===

// === case: for_each_lambda_single_line_block_body_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaSingleLineBlockBodyPutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_method_ref_add ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_method_ref_add_dotted_receiver ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddDottedReceiverSliceViolation {
	List<String> other;

	void m(List<String> list) {
		this.other.addAll(list);
	}
}
// === end ===

// === case: for_each_method_ref_add_parenthesized_qualifier ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddParenthesizedQualifierSliceViolation {
	void m(List<String> list, List<String> a, List<String> b, boolean cond) {
		(cond ? a : b).addAll(list);
	}
}
// === end ===

// === case: for_each_method_ref_add_type_witness ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddTypeWitnessSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_method_ref_multi_line ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefMultiLineSliceViolation {
	void m(List<String> list, List<String> other) {
		other.addAll(list);
	}
}
// === end ===

// === case: for_each_method_ref_put ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_method_ref_put_as_method_argument ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutAsMethodArgumentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		consume(target.putAll(source));
	}
}
// === end ===

// === case: for_each_method_ref_put_block_comment_before_receiver ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutBlockCommentBeforeReceiverSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_method_ref_put_inside_synchronized_block ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutInsideSynchronizedBlockSliceViolation {
	void m(Map<String, String> source, Map<String, String> target, Object lock) {
		synchronized (lock) { target.putAll(source); }
	}
}
// === end ===

// === case: for_each_method_ref_put_multi_line_receiver ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutMultiLineReceiverSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		target.putAll(source);
	}
}
// === end ===

// === case: for_each_method_ref_put_ternary_source ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutTernarySourceSliceViolation {
	void m(Map<String, String> a, Map<String, String> b, boolean cond, Map<String, String> target) {
		target.putAll((cond ? a : b));
	}
}
// === end ===

// === case: indexed_add_all_braced ===
// imports: java.util.List
class InputPreferBulkOperationIndexedAddAllBracedSliceViolation {
	void m(List<String> target, List<String> source) {
		target.addAll(source);
	}
}
// === end ===

// === case: put_all_entry_set ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===

// === case: put_all_entry_set_block_comment_with_fake_brace ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetBlockCommentWithFakeBraceSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===

// === case: put_all_entry_set_braced ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetBracedSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		target.putAll(source);
	}
}
// === end ===