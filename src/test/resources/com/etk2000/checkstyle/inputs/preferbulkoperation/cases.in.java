package com.etk2000.checkstyle.inputs.preferbulkoperation;

// === case: add_all_indexed ===
// imports: java.util.List
class InputPreferBulkOperationAddAllIndexedSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i) // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(source.get(i));
	}
}
// === end ===

// === case: add_all_indexed_post_inc ===
// imports: java.util.List
class InputPreferBulkOperationAddAllIndexedPostIncSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); i++) // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(source.get(i));
	}
}
// === end ===

// === case: array_copy ===
class InputPreferBulkOperationArrayCopySliceViolation {
	void m(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i) // violation: Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.
			dst[i] = src[i];
	}
}
// === end ===

// === case: array_copy_braced ===
class InputPreferBulkOperationArrayCopyBracedSliceViolation {
	void m(int[] dst, int[] src) {
		for (var i = 0; i < src.length; ++i) { // violation: Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.
			dst[i] = src[i];
		}
	}
}
// === end ===

// === case: array_copy_post_inc ===
class InputPreferBulkOperationArrayCopyPostIncSliceViolation {
	void m(int[] dst, int[] src) {
		for (var i = 0; i < src.length; i++) // violation: Use 'System.arraycopy(src, 0, dst, 0, src.length)' instead of a loop that copies elements one at a time.
			dst[i] = src[i];
	}
}
// === end ===

// === case: array_fill ===
class InputPreferBulkOperationArrayFillSliceViolation {
	void m(int[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.
			arr[i] = 0;
	}
}
// === end ===

// === case: array_fill_braced ===
class InputPreferBulkOperationArrayFillBracedSliceViolation {
	void m(int[] arr) {
		for (var i = 0; i < arr.length; ++i) { // violation: Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.
			arr[i] = 0;
		}
	}
}
// === end ===

// === case: array_fill_char_literal_close_brace ===
class InputPreferBulkOperationArrayFillCharLiteralCloseBraceSliceViolation {
	void m(char[] arr) {
		for (var i = 0; i < arr.length; ++i) { // violation: Use 'Arrays.fill(arr, '}')' instead of a loop that assigns a constant.
			arr[i] = '}';
		}
	}
}
// === end ===

// === case: array_fill_multi_line_close_bracket ===
class InputPreferBulkOperationArrayFillMultiLineCloseBracketSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, -a[b[0]])' instead of a loop that assigns a constant.
			arr[i] = -a[b[0
					]];
	}
}
// === end ===

// === case: array_fill_multi_line_unary_split ===
class InputPreferBulkOperationArrayFillMultiLineUnarySplitSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, - a[b[0]])' instead of a loop that assigns a constant.
			arr[i] = -
					a[b[0]];
	}
}
// === end ===

// === case: array_fill_multi_line_value ===
class InputPreferBulkOperationArrayFillMultiLineValueSliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, -a[b[0]])' instead of a loop that assigns a constant.
			arr[i] = -a[b[
					0]];
	}
}
// === end ===

// === case: array_fill_non_zero_value ===
class InputPreferBulkOperationArrayFillNonZeroValueSliceViolation {
	void m(int[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, -1)' instead of a loop that assigns a constant.
			arr[i] = -1;
	}
}
// === end ===

// === case: array_fill_post_inc ===
class InputPreferBulkOperationArrayFillPostIncSliceViolation {
	void m(int[] arr) {
		for (var i = 0; i < arr.length; i++) // violation: Use 'Arrays.fill(arr, 0)' instead of a loop that assigns a constant.
			arr[i] = 0;
	}
}
// === end ===

// === case: array_fill_source_has_field_name_starting_with_length ===
class InputPreferBulkOperationArrayFillSourceHasFieldNameStartingWithLengthSliceViolation {
	static class Holder {
		int[] lengthArray;
	}

	void m(Holder obj) {
		for (var i = 0; i < obj.lengthArray.length; ++i) // violation: Use 'Arrays.fill(obj.lengthArray, 0)' instead of a loop that assigns a constant.
			obj.lengthArray[i] = 0;
	}
}
// === end ===

// === case: array_fill_source_name_has_length_prefix ===
class InputPreferBulkOperationArrayFillSourceNameHasLengthPrefixSliceViolation {
	void m(int[] lengthValues) {
		for (var i = 0; i < lengthValues.length; ++i) // violation: Use 'Arrays.fill(lengthValues, 0)' instead of a loop that assigns a constant.
			lengthValues[i] = 0;
	}
}
// === end ===

// === case: array_fill_string_literal_close_brace ===
class InputPreferBulkOperationArrayFillStringLiteralCloseBraceSliceViolation {
	void m(String[] arr) {
		for (var i = 0; i < arr.length; ++i) { // violation: Use 'Arrays.fill(arr, "}")' instead of a loop that assigns a constant.
			arr[i] = "}";
		}
	}
}
// === end ===

// === case: array_fill_string_literal_open_brace ===
class InputPreferBulkOperationArrayFillStringLiteralOpenBraceSliceViolation {
	void m(String[] arr) {
		for (var i = 0; i < arr.length; ++i) { // violation: Use 'Arrays.fill(arr, "{")' instead of a loop that assigns a constant.
			arr[i] = "{";
		}
	}
}
// === end ===

// === case: array_fill_string_value_with_escaped_quote ===
class InputPreferBulkOperationArrayFillStringValueWithEscapedQuoteSliceViolation {
	void m(String[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, "a\"b")' instead of a loop that assigns a constant.
			arr[i] = "a\"b";
	}
}
// === end ===

// === case: array_fill_string_value_with_semicolon ===
class InputPreferBulkOperationArrayFillStringValueWithSemicolonSliceViolation {
	void m(String[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, "a;b")' instead of a loop that assigns a constant.
			arr[i] = "a;b";
	}
}
// === end ===

// === case: array_fill_string_value_with_slashes ===
class InputPreferBulkOperationArrayFillStringValueWithSlashesSliceViolation {
	void m(String[] arr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, "http://x")' instead of a loop that assigns a constant.
			arr[i] = "http://x";
	}
}
// === end ===

// === case: array_fill_unary_plus_constant ===
class InputPreferBulkOperationArrayFillUnaryPlusConstantSliceViolation {
	void m(int[] arr, int[] other) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, +other[0])' instead of a loop that assigns a constant.
			arr[i] = +other[0];
	}
}
// === end ===

// === case: array_fill_value_contains_bracket_not_arraycopy ===
class InputPreferBulkOperationArrayFillValueContainsBracketNotArraycopySliceViolation {
	void m(int[] arr, int[] a, int[] b) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, -a[b[0]])' instead of a loop that assigns a constant.
			arr[i] = -a[b[0]];
	}
}
// === end ===

// === case: array_fill_value_contains_unary_plus_and_bracket ===
class InputPreferBulkOperationArrayFillValueContainsUnaryPlusAndBracketSliceViolation {
	void m(int[] arr, int[] otherArr) {
		for (var i = 0; i < arr.length; ++i) // violation: Use 'Arrays.fill(arr, +otherArr[0])' instead of a loop that assigns a constant.
			arr[i] = +otherArr[0];
	}
}
// === end ===

// === case: for_each_add_all_braced ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracedSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var item : source) { // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(item);
		}
	}
}
// === end ===

// === case: for_each_add_all_braceless ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracelessSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var item : source) // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(item);
	}
}
// === end ===

// === case: for_each_add_all_braceless_with_trailing_comment ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllBracelessWithTrailingCommentSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var item : source) // some message // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(item);
	}
}
// === end ===

// === case: for_each_add_all_method_call_source ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferBulkOperationForEachAddAllMethodCallSourceSliceViolation {
	void m(List<String> target, Map<String, String> map) {
		for (var item : map.values()) // violation: Use 'target.addAll(map.values())' instead of a loop that adds elements one at a time.
			target.add(item);
	}
}
// === end ===

// === case: for_each_add_all_multi_line_dotted_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllMultiLineDottedSourceSliceViolation {
	void m(List<String> target, List<String> src) {
		for (var item : src // violation: Use 'target.addAll(src.subList(0, 1))' instead of a loop that adds elements one at a time.
				.subList(0, 1))
			target.add(item);
	}
}
// === end ===

// === case: for_each_add_all_multi_line_paren_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllMultiLineParenSourceSliceViolation {
	void m(List<String> target, int a, int b) {
		for (var item : getList( // violation: Use 'target.addAll(getList(a, b))' instead of a loop that adds elements one at a time.
				a,
				b
		))
			target.add(item);
	}
}
// === end ===

// === case: for_each_add_all_nested_paren_source ===
// imports: java.util.List
class InputPreferBulkOperationForEachAddAllNestedParenSourceSliceViolation {
	void m(List<String> target, int a, int b) {
		for (var item : getList(a, b)) // violation: Use 'target.addAll(getList(a, b))' instead of a loop that adds elements one at a time.
			target.add(item);
	}
}
// === end ===

// === case: for_each_lambda_add_all ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach(item -> other.add(item)); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_block_body_multi_line ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllBlockBodyMultiLineSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach(item -> { // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
			other.add(item);
		});
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetSliceViolation {
	void m(List<String> list, Object o) {
		list.forEach(item -> ((List) o).add(item)); // violation: Use '((List) o).addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target_generic ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetGenericSliceViolation {
	void m(List<String> list, Object o) {
		list.forEach(item -> ((List<String>) o).add(item)); // violation: Use '((List<String>) o).addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_cast_target_parenthesized_operand ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllCastTargetParenthesizedOperandSliceViolation {
	void m(List<String> list, Object o) {
		list.forEach(item -> ((List) (o)).add(item)); // violation: Use '((List) (o)).addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_complex_ternary_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllComplexTernaryTargetSliceViolation {
	void m(List<String> list, List<String> a, List<String> b, boolean cond) {
		list.forEach(item -> (cond ? a : b).add(item)); // violation: Use '(cond ? a : b).addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_dotted_target ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllDottedTargetSliceViolation {
	List<String> other;

	void m(List<String> list) {
		list.forEach(item -> this.other.add(item)); // violation: Use 'this.other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_add_all_parenthesized ===
// imports: java.util.List
class InputPreferBulkOperationForEachLambdaAddAllParenthesizedSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach((item) -> other.add(item)); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_block_body_multi_line_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyMultiLinePutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_multiple_comment_lines ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyMultipleCommentLinesSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			// first comment
			// second comment
			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_containing_put ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentContainingPutSliceViolation {
	void m(Map<String, String> source, Map<String, String> real) {
		source.forEach((k, v) -> { // violation: Use 'real.putAll(source)' instead of a loop that puts entries one at a time.
			/* future cleanup:
			   target.put(k, v);
			*/
			real.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_no_star_prefix ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentNoStarPrefixSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			/* this is a
			   multi-line comment */
			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_block_comment_star_prefix ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithBlockCommentStarPrefixSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			/*
			 * multi-line
			 */
			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_line_comment ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithLineCommentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			// this key/value mapping is noteworthy
			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_block_body_with_whitespace_lines ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaBlockBodyWithWhitespaceLinesSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.


			target.put(k, v);
		});
	}
}
// === end ===

// === case: for_each_lambda_nested_for_each_inner_fixed ===
// imports: java.util.List
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaNestedForEachInnerFixedSliceViolation {
	void m(Map<String, List<String>> map, List<String> target) {
		map.forEach((k, v) -> v.forEach(item -> target.add(item))); // violation: Use 'target.addAll(v)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_lambda_preserves_leading_if_statement ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPreservesLeadingIfStatementSliceViolation {
	void m(boolean flag, Map<String, String> source, Map<String, String> target) {
		if (flag) source.forEach((k, v) -> target.put(k, v)); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_lambda_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(k, v)); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_lambda_put_all_complex_ternary_target ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllComplexTernaryTargetSliceViolation {
	void m(Map<String, String> source, Map<String, String> a, Map<String, String> b, boolean cond) {
		source.forEach((k, v) -> (cond ? a : b).put(k, v)); // violation: Use '(cond ? a : b).putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_lambda_put_all_dotted_target ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllDottedTargetSliceViolation {
	Map<String, String> target;

	void m(Map<String, String> source) {
		source.forEach((k, v) -> this.target.put(k, v)); // violation: Use 'this.target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_lambda_put_all_preserves_trailing_content ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaPutAllPreservesTrailingContentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> target.put(k, v)); done(); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_lambda_single_line_block_body_put_all ===
// imports: java.util.Map
class InputPreferBulkOperationForEachLambdaSingleLineBlockBodyPutAllSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach((k, v) -> { target.put(k, v); }); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_add ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach(other::add); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_add_dotted_receiver ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddDottedReceiverSliceViolation {
	List<String> other;

	void m(List<String> list) {
		list.forEach(this.other::add); // violation: Use 'this.other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_add_parenthesized_qualifier ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddParenthesizedQualifierSliceViolation {
	void m(List<String> list, List<String> a, List<String> b, boolean cond) {
		list.forEach((cond ? a : b)::add); // violation: Use '(cond ? a : b).addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_add_type_witness ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefAddTypeWitnessSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach(other::<String>add); // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_multi_line ===
// imports: java.util.List
class InputPreferBulkOperationForEachMethodRefMultiLineSliceViolation {
	void m(List<String> list, List<String> other) {
		list.forEach( // violation: Use 'other.addAll(list)' instead of a loop that adds elements one at a time.
			other::add
		);
	}
}
// === end ===

// === case: for_each_method_ref_put ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source.forEach(target::put); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_put_as_method_argument ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutAsMethodArgumentSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		consume(source.forEach(target::put)); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_put_block_comment_before_receiver ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutBlockCommentBeforeReceiverSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source/* copy */.forEach(target::put); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_put_inside_synchronized_block ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutInsideSynchronizedBlockSliceViolation {
	void m(Map<String, String> source, Map<String, String> target, Object lock) {
		synchronized (lock) { source.forEach(target::put); } // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_put_multi_line_receiver ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutMultiLineReceiverSliceViolation {
	void m(Map<String, String> source, Map<String, String> target) {
		source
				.forEach(target::put); // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: for_each_method_ref_put_ternary_source ===
// imports: java.util.Map
class InputPreferBulkOperationForEachMethodRefPutTernarySourceSliceViolation {
	void m(Map<String, String> a, Map<String, String> b, boolean cond, Map<String, String> target) {
		(cond ? a : b).forEach(target::put); // violation: Use 'target.putAll((cond ? a : b))' instead of a loop that puts entries one at a time.
	}
}
// === end ===

// === case: indexed_add_all_braced ===
// imports: java.util.List
class InputPreferBulkOperationIndexedAddAllBracedSliceViolation {
	void m(List<String> target, List<String> source) {
		for (var i = 0; i < source.size(); ++i) { // violation: Use 'target.addAll(source)' instead of a loop that adds elements one at a time.
			target.add(source.get(i));
		}
	}
}
// === end ===

// === case: put_all_entry_set ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet()) // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			target.put(entry.getKey(), entry.getValue());
	}
}
// === end ===

// === case: put_all_entry_set_block_comment_with_fake_brace ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetBlockCommentWithFakeBraceSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet()) { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			/* fake } brace
			   inside comment */
			target.put(entry.getKey(), entry.getValue());
		}
	}
}
// === end ===

// === case: put_all_entry_set_braced ===
// imports: java.util.Map
class InputPreferBulkOperationPutAllEntrySetBracedSliceViolation {
	void m(Map<String, String> target, Map<String, String> source) {
		for (var entry : source.entrySet()) { // violation: Use 'target.putAll(source)' instead of a loop that puts entries one at a time.
			target.put(entry.getKey(), entry.getValue());
		}
	}
}
// === end ===