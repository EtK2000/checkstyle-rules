package com.etk2000.checkstyle.inputs.jitinefficiency;

// === case: allocation_append_concat_and_empty_string_concat_on_one_line ===
class InputJitInefficiencyAllocationAppendConcatAndEmptyStringConcatOnOneLineSliceViolation {
	void m(StringBuilder sb, int a) {
		sb.append("" + a); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'. // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
	}
}
// === end ===

// === case: allocation_append_concat_bracket_index_arithmetic ===
class InputJitInefficiencyAllocationAppendConcatBracketIndexArithmeticSliceViolation {
	void m(StringBuilder sb, String[] data, int n) {
		for (var i = 0; i < n; ++i)
			sb.append("row " + data[i + 1]); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_cast_receiver_bails ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatCastReceiverBailsSliceViolation {
	void m(Object o, String b) {
		((StringBuilder) o).append("a" + b); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_leading_char_literal ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatLeadingCharLiteralSliceViolation {
	void m(StringBuilder sb) {
		sb.append('a' + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_leading_dot_float ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatLeadingDotFloatSliceViolation {
	void m(StringBuilder sb) {
		sb.append(.5 + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_leading_signed_dot_float ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatLeadingSignedDotFloatSliceViolation {
	void m(StringBuilder sb) {
		sb.append(-.5 + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_leading_signed_number ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatLeadingSignedNumberSliceViolation {
	void m(StringBuilder sb) {
		sb.append(-1 + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_lhs_literal ===
class InputJitInefficiencyAllocationAppendConcatLhsLiteralSliceViolation {
	void m(StringBuilder sb, String value) {
		sb.append("key=" + value); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_lhs_literal_three_operands ===
class InputJitInefficiencyAllocationAppendConcatLhsLiteralThreeOperandsSliceViolation {
	void m(StringBuilder sb, String b, String c) {
		sb.append("a" + b + c); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_lhs_literal_with_paren ===
class InputJitInefficiencyAllocationAppendConcatLhsLiteralWithParenSliceViolation {
	void m(StringBuilder sb, String b) {
		sb.append("x.append(" + b); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_lhs_second_of_chain ===
class InputJitInefficiencyAllocationAppendConcatLhsSecondOfChainSliceViolation {
	void m(StringBuilder sb, String x, String z) {
		sb.append(x).append("y" + z); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_lhs_var ===
class InputJitInefficiencyAllocationAppendConcatLhsVarSliceViolation {
	void m(StringBuilder sb, String value) {
		sb.append(value + "=tail"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_no_literal_operand_bails ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatNoLiteralOperandBailsSliceViolation {
	void m(StringBuilder sb, String a, String b) {
		sb.append(a + b); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_numeric_leading_chain ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatNumericLeadingChainSliceViolation {
	void m(StringBuilder sb) {
		sb.append(1 + 2 + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_receiver_after_dot_operand ===
class InputJitInefficiencyAllocationAppendConcatReceiverAfterDotOperandSliceViolation {
	static class Holder {
		String sb = "h";
	}

	void m(StringBuilder sb, Holder foo) {
		sb.append("a" + foo.sb + "b"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_receiver_self_reference ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatReceiverSelfReferenceSliceViolation {
	void m(StringBuilder sb) {
		sb.append("a" + sb.length() + "b"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_receiver_self_reference_leading ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatReceiverSelfReferenceLeadingSliceViolation {
	void m(StringBuilder sb) {
		sb.append(sb.length() + "x"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_receiver_self_reference_var_leading ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatReceiverSelfReferenceVarLeadingSliceViolation {
	void m(StringBuilder sb, String value) {
		sb.append(value + sb.length() + "tail"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_receiver_substring_operand ===
class InputJitInefficiencyAllocationAppendConcatReceiverSubstringOperandSliceViolation {
	void m(StringBuilder sb, String sbExtra) {
		sb.append("a" + sbExtra + "b"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_string_buffer_receiver ===
class InputJitInefficiencyAllocationAppendConcatStringBufferReceiverSliceViolation {
	void m(StringBuffer buf, String value) {
		buf.append("key=" + value); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_append_concat_unanalysable_receiver_bails ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatUnanalysableReceiverBailsSliceViolation {
	void m(String z) {
		buf().append("len=" + buf().length()); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}

	private StringBuilder buf() {
		return new StringBuilder();
	}
}
// === end ===

// === case: allocation_append_concat_unary_increment_operand_bails ===
// skip-reason: string concatenation inside append()
class InputJitInefficiencyAllocationAppendConcatUnaryIncrementOperandBailsSliceViolation {
	void m(StringBuilder sb, int i) {
		sb.append("a" + ++i); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}
}
// === end ===

// === case: allocation_boxed_constructor_space_before_paren_bails ===
// skip-reason: qualified boxed constructor
class InputJitInefficiencyAllocationBoxedConstructorSpaceBeforeParenBailsSliceViolation {
	void m() {
		final var n = new Integer (42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		System.out.println(n);
	}
}
// === end ===

// === case: allocation_empty_string_concat_array_access ===
class InputJitInefficiencyAllocationEmptyStringConcatArrayAccessSliceViolation {
	void m(int[] arr, int idx) {
		final var s = "" + arr[idx]; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_block_comment_in_args ===
class InputJitInefficiencyAllocationEmptyStringConcatBlockCommentInArgsSliceViolation {
	void m(Object x) {
		final var s = "" + f(x /*,*/); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(Object o) {
		return o;
	}
}
// === end ===

// === case: allocation_empty_string_concat_call_arg_comparison_before_comma ===
class InputJitInefficiencyAllocationEmptyStringConcatCallArgComparisonBeforeCommaSliceViolation {
	void m(int a, int b, int c) {
		final var s = "" + f(a > b, c); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(boolean flag, int c) {
		return c;
	}
}
// === end ===

// === case: allocation_empty_string_concat_call_arg_comparison_ternary ===
class InputJitInefficiencyAllocationEmptyStringConcatCallArgComparisonTernarySliceViolation {
	void m(int x, int y, int p, int q) {
		final var s = "" + Math.max(x > y ? p : q, 0); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_chain ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyAllocationEmptyStringConcatChainSliceViolation {
	void m(int a, int b) {
		final var s = "" + a + b; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_chain_reversed ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyAllocationEmptyStringConcatChainReversedSliceViolation {
	void m(Object a, Object b) {
		final var s = a + b + ""; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_char_literal ===
class InputJitInefficiencyAllocationEmptyStringConcatCharLiteralSliceViolation {
	void m() {
		final var s = "" + 'x'; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_escaped_quote_char_literal ===
class InputJitInefficiencyAllocationEmptyStringConcatEscapedQuoteCharLiteralSliceViolation {
	void m() {
		final var s = "" + f('\''); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(char c) {
		return c;
	}
}
// === end ===

// === case: allocation_empty_string_concat_generic_witness ===
class InputJitInefficiencyAllocationEmptyStringConcatGenericWitnessSliceViolation {
	interface Holder {
		<T> T get();
	}

	void m(Holder obj) {
		final var s = "" + obj.<String>get(); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_index_comparison_ternary ===
class InputJitInefficiencyAllocationEmptyStringConcatIndexComparisonTernarySliceViolation {
	void m(int[] arr, int i, int j, int k) {
		final var s = "" + arr[i > 0 ? j : k]; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_inside_call_arg ===
class InputJitInefficiencyAllocationEmptyStringConcatInsideCallArgSliceViolation {
	void m(int x, int y) {
		g("" + x, y); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
	}

	private void g(String a, int b) {
	}
}
// === end ===

// === case: allocation_empty_string_concat_inside_println ===
class InputJitInefficiencyAllocationEmptyStringConcatInsidePrintlnSliceViolation {
	void m(int x) {
		System.out.println("" + x); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
	}
}
// === end ===

// === case: allocation_empty_string_concat_left ===
class InputJitInefficiencyAllocationEmptyStringConcatLeftSliceViolation {
	void m(int x) {
		final var s = "" + x; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_left_decoy_in_comment ===
class InputJitInefficiencyAllocationEmptyStringConcatLeftDecoyInCommentSliceViolation {
	void m(int x) {
		final var s = /* "" + */ "" + x; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_less_than_in_call_arg ===
class InputJitInefficiencyAllocationEmptyStringConcatLessThanInCallArgSliceViolation {
	void m(int a, int b) {
		final var s = "" + f(a < b); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(boolean flag) {
		return flag;
	}
}
// === end ===

// === case: allocation_empty_string_concat_multi_arg_generic_witness ===
class InputJitInefficiencyAllocationEmptyStringConcatMultiArgGenericWitnessSliceViolation {
	interface Maker {
		<A, B> A make();
	}

	void m(Maker obj) {
		final var s = "" + obj.<Integer, String>make(); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_multiline_continuation ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyAllocationEmptyStringConcatMultilineContinuationSliceViolation {
	void m(Object foo, Object bar) {
		final var s = "" + foo // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
				+ bar;
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_nested_generic_witness ===
class InputJitInefficiencyAllocationEmptyStringConcatNestedGenericWitnessSliceViolation {
	interface Holder {
		<T> T get();
	}

	void m(Holder obj) {
		final var s = "" + obj.<java.util.List<String>>get(); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_nested_plus_in_call ===
class InputJitInefficiencyAllocationEmptyStringConcatNestedPlusInCallSliceViolation {
	void m(int a, int b) {
		final var s = "" + f(a + b); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(int v) {
		return v;
	}
}
// === end ===

// === case: allocation_empty_string_concat_parenthesized_comparison ===
class InputJitInefficiencyAllocationEmptyStringConcatParenthesizedComparisonSliceViolation {
	void m(int a, int b) {
		final var s = "" + (a > b); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_plus_inside_char_literal ===
class InputJitInefficiencyAllocationEmptyStringConcatPlusInsideCharLiteralSliceViolation {
	void m() {
		final var s = "" + f('+'); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(char c) {
		return c;
	}
}
// === end ===

// === case: allocation_empty_string_concat_right ===
class InputJitInefficiencyAllocationEmptyStringConcatRightSliceViolation {
	void m(String name) {
		final var t = name + ""; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(t);
	}
}
// === end ===

// === case: allocation_empty_string_concat_shift_in_call_arg ===
class InputJitInefficiencyAllocationEmptyStringConcatShiftInCallArgSliceViolation {
	void m(int a, int b) {
		final var s = "" + f(a >> b); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(int v) {
		return v;
	}
}
// === end ===

// === case: allocation_empty_string_concat_string_literal_containing_stop_chars ===
class InputJitInefficiencyAllocationEmptyStringConcatStringLiteralContainingStopCharsSliceViolation {
	void m() {
		final var s = "" + f(",;)//"); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f(String v) {
		return v;
	}
}
// === end ===

// === case: allocation_empty_string_concat_ternary_true_branch ===
class InputJitInefficiencyAllocationEmptyStringConcatTernaryTrueBranchSliceViolation {
	void m(boolean cond, String a, String b) {
		final var s = cond ? "" + a : b; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_trailing_block_comment ===
class InputJitInefficiencyAllocationEmptyStringConcatTrailingBlockCommentSliceViolation {
	void m(Object foo) {
		final var s = "" + foo /* note */; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: allocation_empty_string_concat_two_on_one_line ===
class InputJitInefficiencyAllocationEmptyStringConcatTwoOnOneLineSliceViolation {
	void m(int x, int y) {
		g("" + x, "" + y); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string. // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
	}

	private void g(String a, String b) {
	}
}
// === end ===

// === case: allocation_new_string_comment_in_arg ===
// skip-reason: redundant new String(...) wrapper
class InputJitInefficiencyAllocationNewStringCommentInArgSliceViolation {
	void m(String existing) {
		final var b = new String(/* c */ existing); // violation: Use the String variable directly instead of wrapping in 'new String(...)'.
		System.out.println(b);
	}
}
// === end ===

// === case: allocation_new_string_fqn_bails ===
// skip-reason: redundant new String(...) wrapper
class InputJitInefficiencyAllocationNewStringFqnBailsSliceViolation {
	void m() {
		final var a = new java.lang.String("hello"); // violation: Use the string literal directly instead of wrapping in 'new String(...)'.
		System.out.println(a);
	}
}
// === end ===

// === case: allocation_new_string_literal ===
class InputJitInefficiencyAllocationNewStringLiteralSliceViolation {
	void m() {
		final var a = new String("hello"); // violation: Use the string literal directly instead of wrapping in 'new String(...)'.
		System.out.println(a);
	}
}
// === end ===

// === case: allocation_new_string_literal_escaped_quote ===
class InputJitInefficiencyAllocationNewStringLiteralEscapedQuoteSliceViolation {
	void m() {
		final var a = new String("a\"b"); // violation: Use the string literal directly instead of wrapping in 'new String(...)'.
		System.out.println(a);
	}
}
// === end ===

// === case: allocation_new_string_literal_with_paren ===
class InputJitInefficiencyAllocationNewStringLiteralWithParenSliceViolation {
	void m() {
		final var a = new String("a)b("); // violation: Use the string literal directly instead of wrapping in 'new String(...)'.
		System.out.println(a);
	}
}
// === end ===

// === case: allocation_new_string_var ===
class InputJitInefficiencyAllocationNewStringVarSliceViolation {
	void m(String existing) {
		final var b = new String(existing); // violation: Use the String variable directly instead of wrapping in 'new String(...)'.
		System.out.println(b);
	}
}
// === end ===

// === case: allocation_string_buffer_fqn_constructor ===
// skip-reason: local StringBuffer
class InputJitInefficiencyAllocationStringBufferFqnConstructorSliceViolation {
	void m() {
		final var sb = new java.lang.StringBuffer(); // violation: Use 'StringBuilder' instead of 'StringBuffer' for non-shared local builders.
		System.out.println(sb);
	}
}
// === end ===

// === case: allocation_string_buffer_local ===
class InputJitInefficiencyAllocationStringBufferLocalSliceViolation {
	void m() {
		final var sb = new StringBuffer(); // violation: Use 'StringBuilder' instead of 'StringBuffer' for non-shared local builders.
		sb.append("x");
		System.out.println(sb);
	}
}
// === end ===

// === case: allocation_string_buffer_local_with_constructor_arg ===
class InputJitInefficiencyAllocationStringBufferLocalWithConstructorArgSliceViolation {
	void m() {
		final var sb = new StringBuffer("hi"); // violation: Use 'StringBuilder' instead of 'StringBuffer' for non-shared local builders.
		System.out.println(sb);
	}
}
// === end ===

// === case: allocation_to_array_sized_annotated_type ===
// skip-reason: presized toArray(...)
// imports: java.util.List
// imports: javax.annotation.Nullable
class InputJitInefficiencyAllocationToArraySizedAnnotatedTypeSliceViolation {
	void m(List<String> list) {
		final var a = list.toArray(new @Nullable String[5]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_bare_ident_size ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedBareIdentSizeSliceViolation {
	void m(List<String> list, int n) {
		final var a = list.toArray(new String[n]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_length_suffix_size ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedLengthSuffixSizeSliceViolation {
	void m(List<String> list, String key) {
		final var a = list.toArray(new String[key.length()]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_literal ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedLiteralSliceViolation {
	void m(List<String> list) {
		final var a = list.toArray(new String[5]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_qualified_type ===
// imports: java.util.Date
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedQualifiedTypeSliceViolation {
	void m(List<Date> list) {
		final var a = list.toArray(new java.util.Date[5]); // violation: Use 'toArray(new java.util.Date[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_side_effecting_size ===
// skip-reason: presized toArray(...)
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedSideEffectingSizeSliceViolation {
	void m(List<String> list, int x) {
		final var a = list.toArray(new String[x++]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(a.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_size_expr ===
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedSizeExprSliceViolation {
	void m(List<String> list) {
		final var b = list.toArray(new String[list.size()]); // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
		System.out.println(b.length);
	}
}
// === end ===

// === case: allocation_to_array_sized_wrapped_call_bails ===
// skip-reason: presized toArray(...)
// imports: java.util.List
class InputJitInefficiencyAllocationToArraySizedWrappedCallBailsSliceViolation {
	void m(List<String> list, int n) {
		final var a = list.toArray(new String[ // violation: Use 'toArray(new String[0])' instead of a sized array (faster on modern JVMs).
				n]);
		System.out.println(a.length);
	}
}
// === end ===

// === case: array_lhs_array_mentioned_in_block_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsArrayMentionedInBlockCommentPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			/* arr is debug-only */
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_array_mentioned_in_char_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsArrayMentionedInCharLiteralPassesSliceViolation {
	void m(int k, List<String> list) {
		final var a = new String[10];
		a[k] = "";
		for (var x : list) {
			log('a');
			a[k] = a[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private void log(char c) {
	}
}
// === end ===

// === case: array_lhs_array_mentioned_in_line_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsArrayMentionedInLineCommentPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			// arr is dead
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_array_mentioned_in_string_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsArrayMentionedInStringLiteralPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			log("debug arr");
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private void log(String s) {
	}
}
// === end ===

// === case: array_lhs_array_mentioned_in_text_block_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsArrayMentionedInTextBlockBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			log("""arr referenced here: arr[k]""");
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private void log(String s) {
	}
}
// === end ===

// === case: array_lhs_body_line_packs_chain_prefix_compound_assign_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksChainPrefixCompoundAssignBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list)
			obj.f[k] = obj.f[k] + x; obj += newObjFlag(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	Obj newObjFlag() {
		return new Obj();
	}
}
// === end ===

// === case: array_lhs_body_line_packs_chain_prefix_cousin_name_not_match_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksChainPrefixCousinNameNotMatchPassesSliceViolation {
	static class Obj {
		String[] f;
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list, String myObj) {
		obj.f[k] = "";
		for (var x : list)
			obj.f[k] = obj.f[k] + myObj.length(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_body_line_packs_chain_prefix_simple_assign_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksChainPrefixSimpleAssignBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list)
			obj.f[k] = obj.f[k] + x; obj = newObj(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	Obj newObj() {
		return new Obj();
	}
}
// === end ===

// === case: array_lhs_body_line_packs_chain_prefix_string_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksChainPrefixStringLiteralPassesSliceViolation {
	static class Obj {
		String[] f;
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list)
			obj.f[k] = obj.f[k] + "obj = newObj()"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_body_line_packs_full_chain_reassigned_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksFullChainReassignedBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list)
			obj.f[k] = obj.f[k] + x; obj.f = newArr(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	String[] newArr() {
		return new String[10];
	}
}
// === end ===

// === case: array_lhs_body_line_packs_this_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsBodyLinePacksThisChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		this.matrix.cells[i][j] = "";
		for (var x : list)
			this.matrix.cells[i][j] = this.matrix.cells[i][j] + x; this.matrix = pickNew(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: array_lhs_chain_prefix_behind_carried_comment_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsChainPrefixBehindCarriedCommentBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list) {
			/* note:
			   don't */ obj = newObj();
			obj.f[k] = obj.f[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	Obj newObj() {
		return new Obj();
	}
}
// === end ===

// === case: array_lhs_chain_prefix_embedded_in_longer_name_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsChainPrefixEmbeddedInLongerNamePassesSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list, Obj myObj, Obj objTail) {
		obj.f[k] = "";
		for (var x : list) {
			myObj.f[k] = "left-embed";
			objTail.f[k] = "right-embed";
			obj.f[k] = obj.f[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_chain_prefix_in_carried_block_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsChainPrefixInCarriedBlockCommentPassesSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list) {
			/* note:
			   obj is stale */
			obj.f[k] = obj.f[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_for_header_array_length_passes ===
class InputJitInefficiencyArrayLhsForHeaderArrayLengthPassesSliceViolation {
	void m(int k) {
		final var arr = new String[10];
		for (var j = 0; j < arr.length; ++j)
			arr[k] = arr[k] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_array_reassigned_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderArrayReassignedBailsSliceViolation {
	static final int CONSTANT = 0;

	void m() {
		var arr = new String[10];
		int j;
		for (arr = newArr(), j = 0; j < arr.length; ++j)
			arr[CONSTANT] = arr[CONSTANT] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	String[] newArr() {
		return new String[10];
	}
}
// === end ===

// === case: array_lhs_for_header_array_reassigned_compound_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderArrayReassignedCompoundBailsSliceViolation {
	static final int CONSTANT = 0;

	void m() {
		var arr = new String[10];
		int j;
		for (arr += newArr(), j = 0; j < arr.length; ++j)
			arr[CONSTANT] = arr[CONSTANT] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	String[] newArr() {
		return new String[10];
	}
}
// === end ===

// === case: array_lhs_for_header_block_comment_binding_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderBlockCommentBindingBailsSliceViolation {
	void m(List<String> list) {
		final var arr = new String[10];
		for (/* note */ var x : list)
			arr[x] = arr[x] + "!"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_chain_prefix_mentioned_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderChainPrefixMentionedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int k, int j) {
		for (var entry : this.matrix.entrySet())
			this.matrix.cells[k][j] = this.matrix.cells[k][j] + entry; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int a, int b, int n) {
		int j;
		for (this.matrix = pickNew(), j = 0; j < n; ++j)
			this.matrix.cells[a][b] = this.matrix.cells[a][b] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: array_lhs_for_header_classic_multiline_comment_mentions_index_passes ===
class InputJitInefficiencyArrayLhsForHeaderClassicMultilineCommentMentionsIndexPassesSliceViolation {
	void m(int k, int n) {
		final var arr = new String[10];
		for (var j = 0; /* advance
				using k */ j < n; ++j)
			arr[k] = arr[k] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_foreach_multiline_comment_index_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderForeachMultilineCommentIndexBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[10];
		for (var i : /* over
				all */ indices)
			arr[i] = arr[i] + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_line_comment_skip_passes ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderLineCommentSkipPassesSliceViolation {
	void m(int[] indices) {
		final var arr = new String[10];
		for ( // trailing
				var i : indices)
			arr[i] = arr[i] + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_multiline_comment_index_in_header_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsForHeaderMultilineCommentIndexInHeaderBailsSliceViolation {
	void m() {
		final var arr = new String[10];
		for (var j = 0; /* count
				down */ j < arr.length; ++j)
			arr[j] = arr[j] + j; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_multiline_comment_mentions_index_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderMultilineCommentMentionsIndexPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		for (var x : /* pick the
				source using k */ list)
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_multiline_comment_nested_open_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderMultilineCommentNestedOpenPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		for (var x : /* outer /* still
				open */ list)
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_multiline_comment_no_index_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderMultilineCommentNoIndexPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		for (var x : /* pick
				the source */ list)
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_multiline_comment_spans_rparen_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderMultilineCommentSpansRparenPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		for (var x : list /* trailing
				note */)
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_string_literal_with_paren_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderStringLiteralWithParenPassesSliceViolation {
	void m(int k) {
		final var arr = new String[10];
		for (var x : List.of("("))
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_for_header_text_block_in_parens_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsForHeaderTextBlockInParensBailsSliceViolation {
	void m(int k) {
		final var arr = new String[10];
		for (var x : List.of("""
				src""".lines().toList()))
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_index_mutation_in_string_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsIndexMutationInStringLiteralPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			log("reset ++k");
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private void log(String s) {
	}
}
// === end ===

// === case: array_lhs_integer_literal_index ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsIntegerLiteralIndexSliceViolation {
	void m(List<String> list) {
		final var arr = new String[10];
		for (var x : list)
			arr[0] = arr[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_longer_name_not_substring_match_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsLongerNameNotSubstringMatchPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			final var myArr = compute();
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private String compute() {
		return "x";
	}
}
// === end ===

// === case: array_lhs_loop_after_statement_label_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsLoopAfterStatementLabelBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		outer:
		for (var x : list)
			obj.f[k] = obj.f[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: array_lhs_member_k_does_not_count_as_mutation ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsMemberKDoesNotCountAsMutationSliceViolation {
	static class Obj {
		int k;
	}

	final Obj obj = new Obj();

	void m(int k, List<String> list) {
		final var arr = new String[10];
		for (var x : list) {
			obj.k = 5;
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_nested_loop_inner_var_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyArrayLhsNestedLoopInnerVarBailsSliceViolation {
	void m(int m, int n) {
		final var arr = new String[10];
		for (var k = 0; k < m; ++k) {
			for (var i = 0; i < n; ++i)
				arr[i] = arr[i] + k; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_prefix_name_not_substring_match_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsPrefixNameNotSubstringMatchPassesSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			final var arrayList = newList();
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private String newList() {
		return "x";
	}
}
// === end ===

// === case: array_lhs_right_shift_compound_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsRightShiftCompoundIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k >>>= 1;
		}
	}
}
// === end ===

// === case: array_lhs_right_shift_regular_compound_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsRightShiftRegularCompoundIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k >>= 1;
		}
	}
}
// === end ===

// === case: array_lhs_root_identifier_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsRootIdentifierMutatedBailsSliceViolation {
	static class Obj {
		String[] f = new String[10];
	}

	Obj obj = new Obj();

	void m(int k, List<String> list) {
		obj.f[k] = "";
		for (var x : list) {
			obj = newObj();
			obj.f[k] = obj.f[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	Obj newObj() {
		return new Obj();
	}
}
// === end ===

// === case: array_lhs_shift_compound_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsShiftCompoundIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k <<= 1;
		}
	}
}
// === end ===

// === case: array_lhs_this_chain_in_block_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInBlockCommentPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		this.matrix.cells[i][j] = "";
		for (var x : list) {
			/* this.matrix is fine */
			this.matrix.cells[i][j] = this.matrix.cells[i][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_this_chain_in_comment_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInCommentPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		this.matrix.cells[i][j] = "";
		for (var x : list) {
			// this.matrix is fine
			this.matrix.cells[i][j] = this.matrix.cells[i][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: array_lhs_this_chain_in_string_literal_passes ===
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainInStringLiteralPassesSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	final Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		this.matrix.cells[i][j] = "";
		for (var x : list) {
			log("this.matrix is fine");
			this.matrix.cells[i][j] = this.matrix.cells[i][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private void log(String s) {
	}
}
// === end ===

// === case: array_lhs_this_chain_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainMutatedBailsSliceViolation {
	static class Inner {
		String[] b;
	}

	final Inner a = new Inner();

	void m(List<String> list) {
		this.a.b[0] = "";
		for (var x : list) {
			this.a.b = newArr();
			this.a.b[0] = this.a.b[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	String[] newArr() {
		return new String[10];
	}
}
// === end ===

// === case: array_lhs_this_chain_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyArrayLhsThisChainPrefixMutatedBailsSliceViolation {
	static class Matrix {
		String[][] cells;
	}

	Matrix matrix = new Matrix();

	void m(int i, int j, List<String> list) {
		this.matrix.cells[i][j] = "";
		for (var x : list) {
			this.matrix = pickNew();
			this.matrix.cells[i][j] = this.matrix.cells[i][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	Matrix pickNew() {
		return new Matrix();
	}
}
// === end ===

// === case: boxed_constructor_boolean_false_literal ===
class InputJitInefficiencyBoxedConstructorBooleanFalseLiteralSliceViolation {
	void m() {
		final var c = new Boolean(false); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(c);
	}
}
// === end ===

// === case: boxed_constructor_boolean_literal ===
class InputJitInefficiencyBoxedConstructorBooleanLiteralSliceViolation {
	void m() {
		final var c = new Boolean(true); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(c);
	}
}
// === end ===

// === case: boxed_constructor_boolean_literal_with_padding ===
class InputJitInefficiencyBoxedConstructorBooleanLiteralWithPaddingSliceViolation {
	void m() {
		final var c = new Boolean( true ); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(c);
	}
}
// === end ===

// === case: boxed_constructor_boolean_string_arg_with_paren ===
class InputJitInefficiencyBoxedConstructorBooleanStringArgWithParenSliceViolation {
	void m() {
		final var c = new Boolean("true)"); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(c);
	}
}
// === end ===

// === case: boxed_constructor_boolean_variable ===
class InputJitInefficiencyBoxedConstructorBooleanVariableSliceViolation {
	void m(boolean flag) {
		final var d = new Boolean(flag); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(d);
	}
}
// === end ===

// === case: boxed_constructor_byte_constructor ===
class InputJitInefficiencyBoxedConstructorByteConstructorSliceViolation {
	void m() {
		final var h = new Byte((byte) 1); // violation: Use 'Byte.valueOf(...)' instead of 'new Byte(...)'.
		System.out.println(h);
	}
}
// === end ===

// === case: boxed_constructor_character_constructor ===
class InputJitInefficiencyBoxedConstructorCharacterConstructorSliceViolation {
	void m() {
		final var i = new Character('c'); // violation: Use 'Character.valueOf(...)' instead of 'new Character(...)'.
		System.out.println(i);
	}
}
// === end ===

// === case: boxed_constructor_double_constructor ===
class InputJitInefficiencyBoxedConstructorDoubleConstructorSliceViolation {
	void m() {
		final var e = new Double(3.14); // violation: Use 'Double.valueOf(...)' instead of 'new Double(...)'.
		System.out.println(e);
	}
}
// === end ===

// === case: boxed_constructor_float_constructor ===
class InputJitInefficiencyBoxedConstructorFloatConstructorSliceViolation {
	void m() {
		final var f = new Float(1.5f); // violation: Use 'Float.valueOf(...)' instead of 'new Float(...)'.
		System.out.println(f);
	}
}
// === end ===

// === case: boxed_constructor_foreign_qualifier ===
// skip-reason: qualified boxed constructor
class InputJitInefficiencyBoxedConstructorForeignQualifierSliceViolation {
	void m() {
		final var x = new com.example.Integer(42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		System.out.println(x);
	}
}
// === end ===

// === case: boxed_constructor_integer_constructor ===
class InputJitInefficiencyBoxedConstructorIntegerConstructorSliceViolation {
	void m() {
		final var a = new Integer(42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		System.out.println(a);
	}
}
// === end ===

// === case: boxed_constructor_long_constructor ===
class InputJitInefficiencyBoxedConstructorLongConstructorSliceViolation {
	void m() {
		final var b = new Long(100L); // violation: Use 'Long.valueOf(...)' instead of 'new Long(...)'.
		System.out.println(b);
	}
}
// === end ===

// === case: boxed_constructor_short_constructor ===
class InputJitInefficiencyBoxedConstructorShortConstructorSliceViolation {
	void m() {
		final var g = new Short((short) 1); // violation: Use 'Short.valueOf(...)' instead of 'new Short(...)'.
		System.out.println(g);
	}
}
// === end ===

// === case: empty_string_concat_call_receiver_bails ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyEmptyStringConcatCallReceiverBailsSliceViolation {
	void m() {
		final var s = f() + ""; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}

	private Object f() {
		return "x";
	}
}
// === end ===

// === case: empty_string_concat_carried_block_comment_bails ===
// skip-reason: reusable object creation
// imports: java.util.regex.Pattern
class InputJitInefficiencyEmptyStringConcatCarriedBlockCommentBailsSliceViolation {
	void m() {
		/* note
		   "" + tag */ final var p = Pattern.compile("a"); // violation: 'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(p);
	}
}
// === end ===

// === case: empty_string_concat_diamond_operand_bails ===
// skip-reason: empty-string concatenation the fixer cannot simplify
// imports: java.util.ArrayList
class InputJitInefficiencyEmptyStringConcatDiamondOperandBailsSliceViolation {
	void m() {
		final String s = "" + new ArrayList<>(); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: empty_string_concat_new_array_argument ===
class InputJitInefficiencyEmptyStringConcatNewArrayArgumentSliceViolation {
	void m(int id, String[] actual) {
		compare(new String[]{"" + id}, actual); // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
	}

	void compare(String[] a, String[] b) {
	}
}
// === end ===

// === case: empty_string_concat_switch_expression ===
class InputJitInefficiencyEmptyStringConcatSwitchExpressionSliceViolation {
	String m(int kind) {
		final String label = "" + switch (kind) { case 1 -> "a"; default -> "b"; }; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		return label;
	}
}
// === end ===

// === case: empty_string_concat_switch_expression_operand_plus_bails ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyEmptyStringConcatSwitchExpressionOperandPlusBailsSliceViolation {
	String m(int kind, String a, String b) {
		final String label = "" + switch (kind) { case 1 -> a + b; default -> "c"; }; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		return label;
	}
}
// === end ===

// === case: empty_string_concat_text_block_bail ===
// skip-reason: empty-string concatenation the fixer cannot simplify
class InputJitInefficiencyEmptyStringConcatTextBlockBailSliceViolation {
	void m() {
		final var s = """
				body
				""" + ""; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s);
	}
}
// === end ===

// === case: explicit_form_array_lhs_chained_index ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsChainedIndexSliceViolation {
	void m(int k, int j, List<String> list) {
		final var matrix = new String[10][10];
		matrix[k][j] = "";
		for (var x : list)
			matrix[k][j] = matrix[k][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_array_lhs_classic_for ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyExplicitFormArrayLhsClassicForSliceViolation {
	void m(int n) {
		final var arr = new String[n];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i] + "!"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_array_lhs_external_index ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsExternalIndexSliceViolation {
	void m(int k, List<String> list) {
		final var local = new String[10];
		local[k] = "";
		for (var x : list)
			local[k] = local[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_array_lhs_this_array ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsThisArraySliceViolation {
	String[] arr;

	void m(int k, List<String> list) {
		this.arr[k] = "";
		for (var x : list)
			this.arr[k] = this.arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_array_lhs_this_chained_index ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormArrayLhsThisChainedIndexSliceViolation {
	String[][] grid;

	void m(int k, int j, List<String> list) {
		this.grid[k][j] = "";
		for (var x : list)
			this.grid[k][j] = this.grid[k][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_chained ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormChainedSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + ", " + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_classic_for ===
class InputJitInefficiencyExplicitFormClassicForSliceViolation {
	void m(int n) {
		String result = "";
		for (var i = 0; i < n; ++i)
			result = result + i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_deep_chain ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormDeepChainSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + x + "," + " " + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_deep_nested_field ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormDeepNestedFieldSliceViolation {
	static class Holder {
		Holder next;
		String value;
	}

	final Holder holder = new Holder();

	void m(List<String> list) {
		this.holder.next.value = "";
		for (var x : list)
			this.holder.next.value = this.holder.next.value + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_do_while_tier2 ===
class InputJitInefficiencyExplicitFormDoWhileTier2SliceViolation {
	void m() {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: explicit_form_do_while_tier3 ===
class InputJitInefficiencyExplicitFormDoWhileTier3SliceViolation {
	void m() {
		String s = "";
		do
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: explicit_form_field_on_obj ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormFieldOnObjSliceViolation {
	String f;

	void m(InputJitInefficiencyExplicitFormFieldOnObjSliceViolation obj, List<String> list) {
		obj.f = "";
		for (var x : list)
			obj.f = obj.f + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_field_this ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormFieldThisSliceViolation {
	String f;

	void m(List<String> list, String f) {
		this.f = f;
		for (var x : list)
			this.f = this.f + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(this.f);
	}
}
// === end ===

// === case: explicit_form_for_each ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormForEachSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_fqn_array_lhs ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormFqnArrayLhsSliceViolation {
	void m(int k, List<String> list) {
		final java.lang.String[] local = new java.lang.String[10];
		local[k] = "";
		for (var x : list)
			local[k] = local[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_fqn_type ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormFqnTypeSliceViolation {
	void m(List<String> list) {
		java.lang.String result = "";
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_in_for_iterator ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyExplicitFormInForIteratorSliceViolation {
	void m(int n) {
		String s = "";
		for (var i = 0; i < n; s = s + i) // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(i);
	}
}
// === end ===

// === case: explicit_form_middle ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormMiddleSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result = ">>" + result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_nested_loop_inner_index ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyExplicitFormNestedLoopInnerIndexSliceViolation {
	void m(int m, int n) {
		final var local = new String[n];
		for (var k = 0; k < m; ++k) {
			for (var i = 0; i < n; ++i)
				local[i] = local[i] + k; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: explicit_form_nested_loop_outer_index ===
class InputJitInefficiencyExplicitFormNestedLoopOuterIndexSliceViolation {
	void m(int m, int n) {
		final var local = new String[m];
		for (var k = 0; k < m; ++k) {
			local[k] = "";
			for (var i = 0; i < n; ++i)
				local[k] = local[k] + i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: explicit_form_reversed ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormReversedSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result = x + result; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: explicit_form_this_dot_nested ===
// imports: java.util.List
class InputJitInefficiencyExplicitFormThisDotNestedSliceViolation {
	static class Holder {
		Holder next;
		String value;
	}

	final Holder holder = new Holder();

	void m(List<String> list) {
		this.holder.value = "";
		for (var x : list)
			this.holder.value = this.holder.value + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: explicit_form_var_from_method ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyExplicitFormVarFromMethodSliceViolation {
	void m(List<String> list) {
		var result = compute();
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	private String compute() {
		return "x";
	}
}
// === end ===

// === case: explicit_form_while_loop ===
class InputJitInefficiencyExplicitFormWhileLoopSliceViolation {
	void m(boolean cond) {
		String result = "";
		while (cond) {
			result = result + getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			cond = result.length() < 5;
		}
		System.out.println(result);
	}

	private String getNext() {
		return "x";
	}
}
// === end ===

// === case: field_lhs_tier2_do_while_after_statement_label_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyFieldLhsTier2DoWhileAfterStatementLabelBailsSliceViolation {
	private String out = "";

	void m(String out, String x, boolean c) {
		this.out = out;
		outer:
		do this.out = this.out + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (c);
	}
}
// === end ===

// === case: loop_boxed_accumulator_byte ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorByteSliceViolation {
	void m(int n) {
		Byte total = Byte.valueOf((byte) 0); // violation: Boxed accumulator 'total' (type 'Byte') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			total += 1;
		System.out.println(total);
	}
}
// === end ===

// === case: loop_boxed_accumulator_classic_for ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorClassicForSliceViolation {
	void m(int n) {
		Double total = 0.0; // violation: Boxed accumulator 'total' (type 'Double') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			total += i;
		System.out.println(total);
	}
}
// === end ===

// === case: loop_boxed_accumulator_div_assign ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorDivAssignSliceViolation {
	void m() {
		Long n = 1024L; // violation: Boxed accumulator 'n' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		while (n > 1)
			n /= 2;
		System.out.println(n);
	}
}
// === end ===

// === case: loop_boxed_accumulator_do_while ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorDoWhileSliceViolation {
	void m() {
		Float fSum = 0f; // violation: Boxed accumulator 'fSum' (type 'Float') is autoboxed in a loop, prefer the primitive type.
		do fSum += 1f;
		while (fSum < 10f);
		System.out.println(fSum);
	}
}
// === end ===

// === case: loop_boxed_accumulator_for ===
// skip-reason: boxed accumulator in a loop
// imports: java.util.List
class InputJitInefficiencyLoopBoxedAccumulatorForSliceViolation {
	void m(List<Long> nums) {
		Long sum = 0L; // violation: Boxed accumulator 'sum' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var v : nums)
			sum += v;
		System.out.println(sum);
	}
}
// === end ===

// === case: loop_boxed_accumulator_minus_assign ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorMinusAssignSliceViolation {
	void m() {
		Long count = 100L; // violation: Boxed accumulator 'count' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < 10; ++i)
			count -= 1;
		System.out.println(count);
	}
}
// === end ===

// === case: loop_boxed_accumulator_mod_assign ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorModAssignSliceViolation {
	void m() {
		Long m = 1000L; // violation: Boxed accumulator 'm' (type 'Long') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < 5; ++i)
			m %= 7;
		System.out.println(m);
	}
}
// === end ===

// === case: loop_boxed_accumulator_self_read ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorSelfReadSliceViolation {
	void m(int n) {
		Integer count = 0; // violation: Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			count = count + 1;
		System.out.println(count);
	}
}
// === end ===

// === case: loop_boxed_accumulator_short ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorShortSliceViolation {
	void m(int n) {
		Short total = Short.valueOf((short) 0); // violation: Boxed accumulator 'total' (type 'Short') is autoboxed in a loop, prefer the primitive type.
		for (var i = 0; i < n; ++i)
			total += 1;
		System.out.println(total);
	}
}
// === end ===

// === case: loop_boxed_accumulator_star_assign ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorStarAssignSliceViolation {
	void m() {
		Integer prod = 1; // violation: Boxed accumulator 'prod' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		while (prod < 1000)
			prod *= 2;
		System.out.println(prod);
	}
}
// === end ===

// === case: loop_boxed_accumulator_while ===
// skip-reason: boxed accumulator in a loop
class InputJitInefficiencyLoopBoxedAccumulatorWhileSliceViolation {
	void m(boolean cond) {
		Integer count = 0; // violation: Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.
		while (cond) {
			count += 1;
			cond = count < 10;
		}
		System.out.println(count);
	}
}
// === end ===

// === case: loop_boxed_fqn_constructor ===
class InputJitInefficiencyLoopBoxedFqnConstructorSliceViolation {
	void m() {
		final var x = new java.lang.Integer(42); // violation: Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.
		System.out.println(x);
	}
}
// === end ===

// === case: loop_boxed_fqn_constructor_boolean_true ===
class InputJitInefficiencyLoopBoxedFqnConstructorBooleanTrueSliceViolation {
	void m() {
		final var b = new java.lang.Boolean(true); // violation: Use 'Boolean.valueOf(...)' instead of 'new Boolean(...)'.
		System.out.println(b);
	}
}
// === end ===

// === case: loop_enum_values_do_while ===
// skip-reason: Enum.values() in a loop
class InputJitInefficiencyLoopEnumValuesDoWhileSliceViolation {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	void m() {
		var i = 0;
		do {
			final var arr = Color.values(); // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
			i += arr.length;
		}
		while (i < 10);
	}
}
// === end ===

// === case: loop_enum_values_in_for_each ===
// skip-reason: Enum.values() in a loop
class InputJitInefficiencyLoopEnumValuesInForEachSliceViolation {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	void m() {
		for (var i = 0; i < 100; ++i) {
			for (var c : Color.values()) // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
				System.out.println(c);
		}
	}
}
// === end ===

// === case: loop_enum_values_in_while ===
// skip-reason: Enum.values() in a loop
class InputJitInefficiencyLoopEnumValuesInWhileSliceViolation {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	void m(boolean cond) {
		while (cond) {
			final var arr = Color.values(); // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
			cond = arr.length > 0;
		}
	}
}
// === end ===

// === case: loop_enum_values_second_top_level_type ===
// skip-reason: Enum.values() in a loop
class InputJitInefficiencyLoopEnumValuesSecondTopLevelTypeFirstSlice {
}

class InputJitInefficiencyLoopEnumValuesSecondTopLevelTypeSliceViolation {
	enum Color {
		BLUE,
		GREEN,
		RED
	}

	void m() {
		var i = 0;
		do {
			final var arr = Color.values(); // violation: 'Color.values()' allocates a new array each call; cache to a static final field outside the loop.
			i += arr.length;
		}
		while (i < 10);
	}
}
// === end ===

// === case: loop_iterator_while_no_remove ===
// skip-reason: explicit Iterator loop
// imports: java.util.List
class InputJitInefficiencyLoopIteratorWhileNoRemoveSliceViolation {
	void m(List<String> list) {
		final var it = list.iterator();
		while (it.hasNext()) { // violation: Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.
			final var x = it.next();
			System.out.println(x);
		}
	}
}
// === end ===

// === case: loop_map_key_set_get ===
// skip-reason: keySet() with get(key)
// imports: java.util.Map
class InputJitInefficiencyLoopMapKeySetGetSliceViolation {
	void m(Map<String, Integer> map) {
		for (var key : map.keySet()) { // violation: Iterate '.entrySet()' instead of '.keySet()' + '.get(...)' (avoids double lookup).
			final var value = map.get(key);
			System.out.println(key + value);
		}
	}
}
// === end ===

// === case: loop_matches_in_do_while ===
// skip-reason: regex method in a loop
class InputJitInefficiencyLoopMatchesInDoWhileSliceViolation {
	void m(String s) {
		var i = 0;
		do {
			if (s.matches("\\d+")) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
				++i;
		}
		while (i < 5);
	}
}
// === end ===

// === case: loop_regex_in_for_condition ===
// skip-reason: regex method in a loop
class InputJitInefficiencyLoopRegexInForConditionSliceViolation {
	void m(String s) {
		for (var i = 0; s.matches("\\d+"); ++i) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(i);
	}
}
// === end ===

// === case: loop_regex_in_for_iterator ===
// skip-reason: regex method in a loop
class InputJitInefficiencyLoopRegexInForIteratorSliceViolation {
	void m(String s) {
		for (var i = 0; i < 10; s = s.replaceAll("a", "b")) // violation: '.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(i);
	}
}
// === end ===

// === case: loop_regex_matches_in_for_each ===
// skip-reason: regex method in a loop
// imports: java.util.List
class InputJitInefficiencyLoopRegexMatchesInForEachSliceViolation {
	void m(List<String> lines) {
		for (var line : lines) {
			if (line.matches("\\d+")) // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
				System.out.println(line);
		}
	}
}
// === end ===

// === case: loop_regex_replace_all_in_for ===
// skip-reason: regex method in a loop
// imports: java.util.List
class InputJitInefficiencyLoopRegexReplaceAllInForSliceViolation {
	void m(List<String> lines) {
		for (var i = 0; i < lines.size(); ++i) {
			final var s = lines.get(i);
			final var t = s.replaceAll("foo.*", "bar"); // violation: '.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(t);
		}
	}
}
// === end ===

// === case: loop_regex_split_in_while ===
// imports: java.util.Iterator
class InputJitInefficiencyLoopRegexSplitInWhileSliceViolation {
	void m(Iterator<String> it) {
		while (it.hasNext()) { // violation: Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.
			final var line = it.next();
			final var parts = line.split(","); // violation: '.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(parts.length);
		}
	}
}
// === end ===

// === case: loop_regex_string_literal_receiver ===
// skip-reason: regex method in a loop
class InputJitInefficiencyLoopRegexStringLiteralReceiverSliceViolation {
	void m(int n) {
		for (var i = 0; i < n; ++i)
			System.out.println("abc".matches("\\d+")); // violation: '.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
	}
}
// === end ===

// === case: loop_split_in_classic_for ===
// skip-reason: regex method in a loop
class InputJitInefficiencyLoopSplitInClassicForSliceViolation {
	void m(String s) {
		for (var i = 0; i < 10; ++i) {
			final var parts = s.split(","); // violation: '.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.
			System.out.println(parts.length);
		}
	}
}
// === end ===

// === case: loop_string_concat_in_classic_for ===
class InputJitInefficiencyLoopStringConcatInClassicForSliceViolation {
	void m(int n) {
		String result = "";
		for (var i = 0; i < n; ++i)
			result += i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: loop_string_concat_in_do_while ===
class InputJitInefficiencyLoopStringConcatInDoWhileSliceViolation {
	void m() {
		String s = "";
		do s += "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: loop_string_concat_in_for_each ===
// imports: java.util.List
class InputJitInefficiencyLoopStringConcatInForEachSliceViolation {
	void m(List<String> list) {
		String result = "";
		for (var x : list)
			result += x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}
}
// === end ===

// === case: loop_string_concat_in_while ===
class InputJitInefficiencyLoopStringConcatInWhileSliceViolation {
	void m(boolean cond) {
		String result = "";
		while (cond) {
			result += getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			cond = result.length() < 5;
		}
		System.out.println(result);
	}

	private String getNext() {
		return "x";
	}
}
// === end ===

// === case: string_concat_accumulator_name_in_literal_operand ===
// imports: java.util.List
class InputJitInefficiencyStringConcatAccumulatorNameInLiteralOperandSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			s = s + x + "s"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_array_lhs_array_mutated_by_method_call_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.Arrays
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsArrayMutatedByMethodCallBailsSliceViolation {
	void m(List<String> list) {
		final var arr = new String[3];
		arr[0] = "";
		for (var x : list) {
			Arrays.fill(arr, "");
			arr[0] = arr[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_array_var_reassigned_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsArrayVarReassignedBailsSliceViolation {
	void m(List<String> list) {
		var arr = new String[3];
		arr[0] = "";
		for (var x : list) {
			arr = newArr();
			arr[0] = arr[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private String[] newArr() {
		return new String[3];
	}
}
// === end ===

// === case: string_concat_array_lhs_body_line_mutates_index_external_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsBodyLineMutatesIndexExternalBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list)
			arr[k] = arr[k] + (x + ++k); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_chained_index_inner_var_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatArrayLhsChainedIndexInnerVarBailsSliceViolation {
	void m(int k) {
		final var matrix = new String[3][3];
		for (var i = 0; i < 3; ++i)
			matrix[k][i] = matrix[k][i] + "!"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_compound_assign_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsCompoundAssignIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k += 1;
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_dotted_prefix_reassigned_unindented_below_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsDottedPrefixReassignedUnindentedBelowBailsSliceViolation {
	static class Obj {
		String[] f = new String[3];
	}

	Obj obj = new Obj();

	void m(List<String> list) {
		obj.f[0] = "";
		for (var x : list) {
			obj.f[0] = obj.f[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
obj = newObj();
		}
	}

	private Obj newObj() {
		return new Obj();
	}
}
// === end ===

// === case: string_concat_array_lhs_for_each_iter_var_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatArrayLhsForEachIterVarBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[3];
		for (var x : indices)
			arr[x] = arr[x] + "!"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_multi_line_for_each_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatArrayLhsMultiLineForEachBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[3];
		for (var i :
				indices)
			arr[i] = arr[i] + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_multi_line_for_each_continuation_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatArrayLhsMultiLineForEachContinuationLineBailsSliceViolation {
	void m(int[] indices) {
		final var arr = new String[3];
		for (
				int i : indices)
			arr[i] = arr[i] + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_mutated_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsMutatedIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			++k;
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_negative_literal_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsNegativeLiteralIndexBailsSliceViolation {
	void m(List<String> list) {
		final var arr = new String[3];
		arr[2] = "";
		for (var x : list)
			arr[-1] = arr[-1] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_non_simple_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsNonSimpleIndexBailsSliceViolation {
	static class Index {
		int field;
	}

	void m(Index k, List<String> list) {
		final var arr = new String[10];
		arr[k.field] = "";
		for (var x : list)
			arr[k.field] = arr[k.field] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_array_lhs_post_decrement_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsPostDecrementIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k--;
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_post_increment_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsPostIncrementIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k++;
		}
	}
}
// === end ===

// === case: string_concat_array_lhs_reassigned_unindented_below_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsReassignedUnindentedBelowBailsSliceViolation {
	void m(List<String> list) {
		var arr = new String[3];
		arr[0] = "";
		for (var x : list) {
			arr[0] = arr[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
arr = newArr();
		}
	}

	private String[] newArr() {
		return new String[3];
	}
}
// === end ===

// === case: string_concat_array_lhs_simple_assign_index_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsSimpleAssignIndexBailsSliceViolation {
	void m(int k, List<String> list) {
		final var arr = new String[10];
		arr[k] = "";
		for (var x : list) {
			arr[k] = arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			k = otherK();
		}
	}

	private int otherK() {
		return 0;
	}
}
// === end ===

// === case: string_concat_array_lhs_this_qualified_prefix_mutated_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatArrayLhsThisQualifiedPrefixMutatedBailsSliceViolation {
	static class Obj {
		String[] f = new String[3];
	}

	Obj obj = new Obj();

	void m(List<String> list) {
		obj.f[0] = "";
		for (var x : list) {
			this.obj = newObj();
			obj.f[0] = obj.f[0] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private Obj newObj() {
		return new Obj();
	}
}
// === end ===

// === case: string_concat_array_lhs_wrapped_for_header_braced ===
class InputJitInefficiencyStringConcatArrayLhsWrappedForHeaderBracedSliceViolation {
	void m(int n) {
		final var arr = new String[3];
		arr[0] = "";
		for (var i = 0;
				i < n; ++i) {
			arr[0] = arr[0] + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(i);
		}
		System.out.println(arr[0]);
	}
}
// === end ===

// === case: string_concat_block_comment_containing_brace_finds_real_close ===
// imports: java.util.List
class InputJitInefficiencyStringConcatBlockCommentContainingBraceFindsRealCloseSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			/* old code:
		}
		*/
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_block_comment_in_gap_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBlockCommentInGapBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		/* note: } unrelated brace */
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_block_comment_on_body_line_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBlockCommentOnBodyLineBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s = s + /* a + b */ x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_body_line_packs_second_statement_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBodyLinePacksSecondStatementBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s = s + x; log(x); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private void log(String x) {
	}
}
// === end ===

// === case: string_concat_body_line_text_block_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBodyLineTextBlockBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + """
					tail
					"""; // violation@opener: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_close_line_packs_safe_use_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatBracedCloseLinePacksSafeUsePassesSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		} log(s.length());
		System.out.println(s);
	}

	private void log(int n) {
	}
}
// === end ===

// === case: string_concat_braced_close_line_packs_unsafe_use_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBracedCloseLinePacksUnsafeUseBailsSliceViolation {
	void m(List<String> list, String target) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		} log(s.equals(target));
		System.out.println(s);
	}

	private void log(boolean b) {
	}
}
// === end ===

// === case: string_concat_braced_do_while ===
class InputJitInefficiencyStringConcatBracedDoWhileSliceViolation {
	void m() {
		String s = "";
		do {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_condition_wraps_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatBracedDoWhileConditionWrapsBailsSliceViolation {
	void m(boolean flag) {
		String s = "";
		do {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		while (s.length() < 5
				&& flag);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_cuddled_terminator ===
class InputJitInefficiencyStringConcatBracedDoWhileCuddledTerminatorSliceViolation {
	void m() {
		String s = "";
		do {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		} while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_do_while_sibling_unsafe_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatBracedDoWhileSiblingUnsafeBailsSliceViolation {
	void m(String target) {
		var s = "a";
		do {
			log(s.equals(target));
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		while (s.length() < 5);
		System.out.println(s);
	}

	private void log(boolean b) {
	}
}
// === end ===

// === case: string_concat_braced_do_while_unsafe_method_in_while_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatBracedDoWhileUnsafeMethodInWhileBailsSliceViolation {
	void m(String target) {
		var s = "";
		do {
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_braced_single_if_body ===
// imports: java.util.List
class InputJitInefficiencyStringConcatBracedSingleIfBodySliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			if (x != null)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_bracket_index_arithmetic ===
class InputJitInefficiencyStringConcatBracketIndexArithmeticSliceViolation {
	String m(String[] data, int n) {
		String s = "";
		for (var i = 0; i < n; ++i)
			s = s + data[i + 1]; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_buried_assign_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatBuriedAssignBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s.trim();
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_buried_in_if_with_braced_loop ===
// imports: java.util.List
class InputJitInefficiencyStringConcatBuriedInIfWithBracedLoopSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			log(x);
			if (x != null)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			other();
		}
		System.out.println(s);
	}

	private void log(String x) {
	}

	private void other() {
	}
}
// === end ===

// === case: string_concat_commented_out_decl_above_is_skipped ===
// imports: java.util.List
class InputJitInefficiencyStringConcatCommentedOutDeclAboveIsSkippedSliceViolation {
	void m(List<String> list) {
		/* legacy:
		String s = "old";
		*/
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_cuddled_else_below_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatCuddledElseBelowBodyBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null) {
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			} else {
				log("skip");
			}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_cuddled_else_no_space_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatCuddledElseNoSpaceBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null) {
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			}else {
				log("skip");
			}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_decl_with_gap ===
// imports: java.util.List
class InputJitInefficiencyStringConcatDeclWithGapSliceViolation {
	void m(List<String> list) {
		String s = "";
		final int x = compute();
		log("start");
		for (var v : list)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private int compute() {
		return 0;
	}

	private void log(String x) {
	}
}
// === end ===

// === case: string_concat_decl_with_gap_brace_in_string_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatDeclWithGapBraceInStringPassesSliceViolation {
	void m(List<String> list) {
		String s = "";
		log("open brace: {");
		for (var v : list)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private void log(String x) {
	}
}
// === end ===

// === case: string_concat_decl_with_gap_mentions_var_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatDeclWithGapMentionsVarBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		log(s);
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_decl_with_gap_string_mentions_var_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatDeclWithGapStringMentionsVarPassesSliceViolation {
	void m(List<String> list) {
		String s = "";
		final int x = compute();
		log("track s now");
		for (var v : list)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private int compute() {
		return 0;
	}

	private void log(String x) {
	}
}
// === end ===

// === case: string_concat_do_while_close_comment_shares_while_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatDoWhileCloseCommentSharesWhileLineBailsSliceViolation {
	void m() {
		var s = "";
		do
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		/* note
		*/ while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_do_while_close_comment_shares_while_line_no_space_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatDoWhileCloseCommentSharesWhileLineNoSpaceBailsSliceViolation {
	void m() {
		var s = "";
		do
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		/* note
		*/ while(s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_do_while_cuddled_terminator_decoy_while_below ===
class InputJitInefficiencyStringConcatDoWhileCuddledTerminatorDecoyWhileBelowSliceViolation {
	private String f = "";

	void m(boolean c, int n, String f) {
		this.f = f;
		do
			if (c) {
				this.f = this.f + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			} while (n < 3);
		System.out.println(this.f);
		do
			n = next(n);
		while (n < 9);
	}

	private int next(int x) {
		return x + 1;
	}
}
// === end ===

// === case: string_concat_do_while_decoy_while_in_block_comment_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatDoWhileDecoyWhileInBlockCommentBailsSliceViolation {
	void m(String target) {
		var s = "";
		do {
		/*
		while (decoy);
		*/
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_do_while_decoy_while_in_text_block_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatDoWhileDecoyWhileInTextBlockBailsSliceViolation {
	void m(String target) {
		var s = "";
		do {
			final var tb = """
		while (decoy);
			""";
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(tb);
		}
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_do_while_tier3_while_no_space_fixed ===
class InputJitInefficiencyStringConcatDoWhileTier3WhileNoSpaceFixedSliceViolation {
	void m() {
		var s = "";
		do
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while(s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_else_brace_no_space_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatElseBraceNoSpaceBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			else{
				log("skip");
			}
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_else_branch_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatElseBranchBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			else
				log("skip");
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_else_word_in_comment_continuation_still_fixes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatElseWordInCommentContinuationStillFixesSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			/* note:
			else nope */
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_explicit_braced_body ===
// imports: java.util.List
class InputJitInefficiencyStringConcatExplicitBracedBodySliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_explicit_double_lhs_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatExplicitDoubleLhsBailsSliceViolation {
	void m(List<String> list) {
		var s = "a";
		for (var x : list)
			s = s + s; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_explicit_reverse_multi_prepend ===
// imports: java.util.List
class InputJitInefficiencyStringConcatExplicitReverseMultiPrependSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			s = "<" + x + s; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_explicit_while ===
class InputJitInefficiencyStringConcatExplicitWhileSliceViolation {
	void m(boolean cond) {
		String s = "";
		while (cond)
			s = s + getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private String getNext() {
		return "x";
	}
}
// === end ===

// === case: string_concat_field_lhs_sb_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatFieldLhsSbTakenSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	String f;

	void m(List<String> list) {
		this.f = "";
		for (var x : list)
			this.f = this.f + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		sb.append(this.f);
	}
}
// === end ===

// === case: string_concat_for_header_array_initializer_braced ===
class InputJitInefficiencyStringConcatForHeaderArrayInitializerBracedSliceViolation {
	void m() {
		String s = "";
		for (var x : new String[]{"a", "b"}) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_for_header_no_space ===
// imports: java.util.List
class InputJitInefficiencyStringConcatForHeaderNoSpaceSliceViolation {
	void m(List<String> list) {
		String s = "";
		for(var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_for_header_trailing_comment_braced ===
// imports: java.util.List
class InputJitInefficiencyStringConcatForHeaderTrailingCommentBracedSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) { // accumulate
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_generic_type_args_init_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.HashMap
// imports: java.util.List
class InputJitInefficiencyStringConcatGenericTypeArgsInitBailsSliceViolation {
	void m(List<String> values) {
		String s = new HashMap<String, Integer>().toString();
		for (var v : values)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_if_else_above_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatIfElseAboveBodyBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x == null)
				log("skip");
			else
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_index_of_char_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatIndexOfCharBailsSliceViolation {
	void m(List<String> list) {
		var s = "a";
		for (var x : list) {
			if (s.indexOf('x') < 0)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_init_block_lambda_semicolon_passes ===
// imports: java.util.List
// imports: java.util.function.Supplier
class InputJitInefficiencyStringConcatInitBlockLambdaSemicolonPassesSliceViolation {
	void m(List<String> list) {
		String s = supply(() -> { return "seed"; });
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private String supply(Supplier<String> f) {
		return f.get();
	}
}
// === end ===

// === case: string_concat_init_call_arg_comma_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatInitCallArgCommaPassesSliceViolation {
	void m(List<String> list, String a, String b) {
		String s = String.join("-", a, b);
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_init_literal_comma_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatInitLiteralCommaPassesSliceViolation {
	void m(List<String> list) {
		String s = "a,b";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_last_index_of_char_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatLastIndexOfCharBailsSliceViolation {
	void m(List<String> list) {
		var s = "a";
		for (var x : list) {
			if (s.lastIndexOf('x') < 0)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_later_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatLaterAppendReadsAccumulatorBailsSliceViolation {
	String f() {
		String s = "";
		for (int i = 0; i < 3; ++i)
			s = s + "-" + s.length(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_loop_close_line_has_closed_block_comment ===
// imports: java.util.List
class InputJitInefficiencyStringConcatLoopCloseLineHasClosedBlockCommentSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		} /* trailing */
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_loop_close_line_opens_block_comment_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatLoopCloseLineOpensBlockCommentBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		} /* trailing
		*/
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_char_at ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadCharAtSliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.charAt(0) + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_chars ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadCharsSliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.chars() + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_code_point_at ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadCodePointAtSliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.codePointAt(0) + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_code_points ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadCodePointsSliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.codePoints() + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_is_empty ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadIsEmptySliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.isEmpty() + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_length_in_if_cond ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadLengthInIfCondSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			if (s.length() < 100)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_read_sub_sequence ===
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopReadSubSequenceSliceViolation {
	void m(List<String> list) {
		String s = "z";
		for (var x : list)
			s = s + s.subSequence(0, 1) + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_mid_loop_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatMidLoopUnsafeMethodBailsSliceViolation {
	void m(String target, List<String> list) {
		var s = "abc";
		for (var x : list) {
			if (s.equals(target))
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_middle_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatMiddleAppendReadsAccumulatorBailsSliceViolation {
	String f(String x) {
		String s = "";
		for (int i = 0; i < 3; ++i)
			s = s + "-" + s.length() + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_multi_var_decl_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatMultiVarDeclBailsSliceViolation {
	void m(List<String> list) {
		String s = "", t = "x";
		for (var v : list)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		t = s;
		System.out.println(t);
	}
}
// === end ===

// === case: string_concat_multivar_relational_rebalanced_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatMultivarRelationalRebalancedBailsSliceViolation {
	String f(int n, int mm, int j, int k) {
		String s = n < mm ? "x" : "y", t = j > k ? "p" : "q";
		for (var i = 0; i < 3; ++i)
			s += i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		t = t.trim();
		return s + t;
	}
}
// === end ===

// === case: string_concat_nested_if_body ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNestedIfBodySliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null)
				if (!x.isEmpty())
					s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_non_empty_init_literal ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNonEmptyInitLiteralSliceViolation {
	void m(List<String> list) {
		String s = "prefix";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_non_empty_init_method_call ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNonEmptyInitMethodCallSliceViolation {
	void m(List<String> list) {
		String s = compute();
		for (var x : list)
			s += x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private String compute() {
		return "x";
	}
}
// === end ===

// === case: string_concat_non_empty_init_sb_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNonEmptyInitSbTakenSliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder("seed");
		String s = "prefix";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sb);
	}
}
// === end ===

// === case: string_concat_non_empty_init_var ===
// imports: java.util.List
class InputJitInefficiencyStringConcatNonEmptyInitVarSliceViolation {
	void m(List<String> list, String otherVar) {
		String s = otherVar;
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_operand_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatOperandUnsafeMethodBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s = s + s.replace('x', 'y'); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_packed_second_statement_in_decl_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatPackedSecondStatementInDeclBailsSliceViolation {
	void m(List<String> xs) {
		String s = ""; final boolean first = true;
		for (var x : xs)
			s += x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		use(s, first);
	}

	void use(String a, boolean b) {
	}
}
// === end ===

// === case: string_concat_plus_assign_accepts_tab_between_lhs_and_op ===
// imports: java.util.List
class InputJitInefficiencyStringConcatPlusAssignAcceptsTabBetweenLhsAndOpSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			s	+=	x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_plus_assign_do_while_tier3 ===
class InputJitInefficiencyStringConcatPlusAssignDoWhileTier3SliceViolation {
	void m() {
		String s = "";
		do
			s += "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_plus_assign_unsafe_method_rhs_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatPlusAssignUnsafeMethodRhsBailsSliceViolation {
	void m(List<String> list) {
		var s = "abc";
		for (var x : list)
			s += s.replace('x', 'y'); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_plus_assign_while ===
class InputJitInefficiencyStringConcatPlusAssignWhileSliceViolation {
	void m(boolean cond) {
		String s = "";
		while (cond)
			s += getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private String getNext() {
		return "x";
	}
}
// === end ===

// === case: string_concat_post_loop_write_behind_carried_comment_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatPostLoopWriteBehindCarriedCommentBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		/* note:
		   don't */ s = s.trim();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_prepend_insert_sb_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatPrependInsertSbTakenSliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder("seed");
		String s = "";
		for (var x : list)
			s = ">>" + s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sb);
	}
}
// === end ===

// === case: string_concat_prepend_operand_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatPrependOperandUnsafeMethodBailsSliceViolation {
	void m(List<String> list) {
		var s = "a";
		for (var x : list)
			s = s.replace('x', 'y') + s; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_prepend_then_append_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatPrependThenAppendReadsAccumulatorBailsSliceViolation {
	String f(String p) {
		String s = "";
		for (int i = 0; i < 3; ++i)
			s = p + s + s.length(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_reassigned_after_loop_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatReassignedAfterLoopBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		s = s.trim();
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_relational_gt_init_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatRelationalGtInitPassesSliceViolation {
	String m(int n, int mm, List<String> values) {
		String s = n > mm ? "x" : "y";
		for (var v : values)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_relational_lt_single_declarator_passes ===
// imports: java.util.List
class InputJitInefficiencyStringConcatRelationalLtSingleDeclaratorPassesSliceViolation {
	String m(int n, int mm, List<String> values) {
		String s = n < mm ? "x" : "y";
		for (var v : values)
			s = s + v; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_same_line_else_body_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatSameLineElseBodyBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list)
			if (x != null)
				s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			else log("skip");
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_sb_and_stringbuilder_and_sb2_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbAndStringbuilderAndSb2TakenSliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder("a");
		final var stringBuilder = new StringBuilder("b");
		final var sb2 = new StringBuilder("c");
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sb + stringBuilder + sb2);
	}
}
// === end ===

// === case: string_concat_sb_and_stringbuilder_taken ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbAndStringbuilderTakenSliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder("a");
		final var stringBuilder = new StringBuilder("b");
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sb + stringBuilder);
	}
}
// === end ===

// === case: string_concat_sb_free_in_block_comment ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbFreeInBlockCommentSliceViolation {
	void m(List<String> list) {
		/* sb */
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_sb_free_in_line_comment ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbFreeInLineCommentSliceViolation {
	void m(List<String> list) {
		// sb is not used here
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_sb_free_in_string_literal ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbFreeInStringLiteralSliceViolation {
	void m(List<String> list) {
		System.out.println("sb");
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_sb_free_in_text_block ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbFreeInTextBlockSliceViolation {
	void m(List<String> list) {
		final var tb = """
				sb
				""";
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + tb);
	}
}
// === end ===

// === case: string_concat_sb_substring_identifier_free ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbSubstringIdentifierFreeSliceViolation {
	void m(List<String> list) {
		final var sbCount = list.size();
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sbCount);
	}
}
// === end ===

// === case: string_concat_sb_taken_by_field ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbTakenByFieldSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	void m(List<String> list) {
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		sb.append(s);
	}
}
// === end ===

// === case: string_concat_sb_taken_by_local ===
// imports: java.util.List
class InputJitInefficiencyStringConcatSbTakenByLocalSliceViolation {
	void m(List<String> list) {
		final var sb = new StringBuilder("seed");
		String s = "";
		for (var x : list)
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s + sb);
	}
}
// === end ===

// === case: string_concat_single_append_reads_accumulator ===
class InputJitInefficiencyStringConcatSingleAppendReadsAccumulatorSliceViolation {
	String f() {
		String s = "";
		for (int i = 0; i < 3; ++i)
			s = s + s.length(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_text_block_above_body_line ===
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockAboveBodyLineSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			final var note = """
					note
					""";
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(note);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_text_block_below_loop_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockBelowLoopBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(x);
		}
		final var tb = """
				note
				""";
		System.out.println(s + tb);
	}
}
// === end ===

// === case: string_concat_text_block_close_line_reads_accumulator_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockCloseLineReadsAccumulatorBailsSliceViolation {
	void m(List<String> list) {
		String s = "";
		for (var x : list) {
			final var note = """
					note""" + s;
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(note);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_text_block_containing_brace_bails ===
// skip-reason: string concatenation in a loop
// imports: java.util.List
class InputJitInefficiencyStringConcatTextBlockContainingBraceBailsSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list) {
			s = s + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			final var tb = """
}
	""";
			System.out.println(tb);
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_body_call_with_paren_argument_stays_tier2 ===
// imports: java.util.List
class InputJitInefficiencyStringConcatTier2BodyCallWithParenArgumentStaysTier2SliceViolation {
	void m(List<String> list, int i) {
		var s = "";
		do s += list.get(i); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (--i > 0);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_accepts_tab_separator ===
class InputJitInefficiencyStringConcatTier2DoWhileAcceptsTabSeparatorSliceViolation {
	void m() {
		String s = "";
		do	s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_array_lhs_mutated_index_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileArrayLhsMutatedIndexBailsSliceViolation {
	void m(String[] arr, int k) {
		do arr[k] = arr[k] + (++k); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (arr[k] != null);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_block_comment_in_do_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileBlockCommentInDoLineBailsSliceViolation {
	void m() {
		String s = "";
		do /* note */ s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_block_comment_in_while_line_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileBlockCommentInWhileLineBailsSliceViolation {
	void m() {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (/* note */ s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_chained_expands_to_tier3 ===
class InputJitInefficiencyStringConcatTier2DoWhileChainedExpandsToTier3SliceViolation {
	void m(String x) {
		String s = "";
		do s = s + ", " + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_char_literal_paren_stays_tier2 ===
class InputJitInefficiencyStringConcatTier2DoWhileCharLiteralParenStaysTier2SliceViolation {
	void m() {
		String s = "";
		do s += '('; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_field_lhs_sb_taken ===
class InputJitInefficiencyStringConcatTier2DoWhileFieldLhsSbTakenSliceViolation {
	private final StringBuilder sb = new StringBuilder();

	String f;

	void m() {
		this.f = "";
		do this.f = this.f + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (this.f.length() < 5);
		sb.append(this.f);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_gap_mentions_var_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileGapMentionsVarBailsSliceViolation {
	void m() {
		String s = "";
		log(s);
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}

	private void log(String s) {
	}
}
// === end ===

// === case: string_concat_tier2_do_while_mismatched_while_indent_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileMismatchedWhileIndentBailsSliceViolation {
	void m() {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_no_space_terminator ===
class InputJitInefficiencyStringConcatTier2DoWhileNoSpaceTerminatorSliceViolation {
	void m() {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while(s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_paren_in_literal_stays_tier2 ===
class InputJitInefficiencyStringConcatTier2DoWhileParenInLiteralStaysTier2SliceViolation {
	void m() {
		String s = "";
		do s += "("; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_reassigned_after_loop_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileReassignedAfterLoopBailsSliceViolation {
	void m(int n) {
		String result = "";
		var x = n;
		do result = result + "a"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (--x > 0);
		result = result.trim();
		System.out.println(result);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_sb_taken ===
class InputJitInefficiencyStringConcatTier2DoWhileSbTakenSliceViolation {
	void m() {
		final var sb = new StringBuilder("seed");
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s + sb);
	}
}
// === end ===

// === case: string_concat_tier2_do_while_unsafe_method_in_while_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier2DoWhileUnsafeMethodInWhileBailsSliceViolation {
	void m(String target) {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_tier3_do_while_unsafe_method_in_while_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatTier3DoWhileUnsafeMethodInWhileBailsSliceViolation {
	void m(String target) {
		var s = "";
		do
			s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.equals(target));
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_unary_increment_operand_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatUnaryIncrementOperandBailsSliceViolation {
	int count;

	String f(int n) {
		String s = "";
		for (int i = 0; i < n; ++i)
			s = s + ++count; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_unary_plus_operand_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatUnaryPlusOperandBailsSliceViolation {
	String f(int n, int b) {
		String s = "";
		for (int i = 0; i < n; ++i)
			s = s + +b; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s;
	}
}
// === end ===

// === case: string_concat_var_decl ===
// imports: java.util.List
class InputJitInefficiencyStringConcatVarDeclSliceViolation {
	void m(List<String> list) {
		var s = "";
		for (var x : list)
			s += x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_after_closed_block_comment ===
class InputJitInefficiencyStringConcatWhileHeaderAfterClosedBlockCommentSliceViolation {
	String f;

	void m(boolean cond, String f) {
		this.f = f;
		/* note
		foo( */
		while (cond)
			this.f = this.f + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_while_header_in_carried_block_comment_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderInCarriedBlockCommentBailsSliceViolation {
	String f;

	void m(boolean cond, String f) {
		this.f = f;
		/* note
		foo( */ while (cond)
			this.f = this.f + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_while_header_in_carried_comment_no_decoy_paren_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderInCarriedCommentNoDecoyParenBailsSliceViolation {
	String f;

	void m(String f) {
		this.f = f;
		/* note
		*/ for (var i = 0; i < 3; ++i)
			this.f = this.f + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}
}
// === end ===

// === case: string_concat_while_header_no_space ===
class InputJitInefficiencyStringConcatWhileHeaderNoSpaceSliceViolation {
	void m(boolean cond) {
		String s = "";
		while(cond)
			s = s + getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}

	private String getNext() {
		return "x";
	}
}
// === end ===

// === case: string_concat_while_header_packed_statement_braced ===
class InputJitInefficiencyStringConcatWhileHeaderPackedStatementBracedSliceViolation {
	void m(int n) {
		String s = "";
		while (n > 0) { --n;
			s += "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_reads_length ===
class InputJitInefficiencyStringConcatWhileHeaderReadsLengthSliceViolation {
	void m() {
		String s = "";
		while (s.length() < 10)
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderUnsafeMethodBailsSliceViolation {
	void m(String target) {
		String s = "";
		while (!s.equals(target))
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_wraps_braced ===
class InputJitInefficiencyStringConcatWhileHeaderWrapsBracedSliceViolation {
	void m(int n) {
		String s = "";
		while (n > 0
				&& n < 100) {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			--n;
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_wraps_reads_length ===
class InputJitInefficiencyStringConcatWhileHeaderWrapsReadsLengthSliceViolation {
	void m(int n) {
		String s = "";
		while (n > 0
				&& s.length() < 10) {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			--n;
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_while_header_wraps_unsafe_method_bails ===
// skip-reason: string concatenation in a loop
class InputJitInefficiencyStringConcatWhileHeaderWrapsUnsafeMethodBailsSliceViolation {
	void m(int n, String target) {
		String s = "";
		while (n > 0
				&& !s.equals(target)) {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			--n;
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_wrapped_for_header_braced ===
class InputJitInefficiencyStringConcatWrappedForHeaderBracedSliceViolation {
	void m(int n) {
		String s = "";
		for (var i = 0;
				i < n; ++i) {
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
		System.out.println(s);
	}
}
// === end ===

// === case: string_concat_wrapped_while_header_reads_length_sb_taken ===
class InputJitInefficiencyStringConcatWrappedWhileHeaderReadsLengthSbTakenSliceViolation {
	String f() {
		final var sb = new StringBuilder("seed");
		String s = "";
		while (s.length()
				< 10)
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		return s + sb;
	}
}
// === end ===

// === case: structural_double_brace_fqn ===
// skip-reason: double-brace initialization
class InputJitInefficiencyStructuralDoubleBraceFqnSliceViolation {
	void m() {
		final var list = new java.util.ArrayList<String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			add("a");
		}};
		System.out.println(list);
	}
}
// === end ===

// === case: structural_double_brace_list ===
// skip-reason: double-brace initialization
// imports: java.util.ArrayList
class InputJitInefficiencyStructuralDoubleBraceListSliceViolation {
	void m() {
		final var list = new ArrayList<String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			add("a");
			add("b");
		}};
		System.out.println(list);
	}
}
// === end ===

// === case: structural_double_brace_map ===
// skip-reason: double-brace initialization
// imports: java.util.HashMap
class InputJitInefficiencyStructuralDoubleBraceMapSliceViolation {
	void m() {
		final var map = new HashMap<String, String>() {{ // violation: Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.
			put("k", "v");
		}};
		System.out.println(map);
	}
}
// === end ===

// === case: structural_pattern_compile_in_constructor ===
// skip-reason: reusable object creation
// imports: java.util.regex.Pattern
class InputJitInefficiencyStructuralPatternCompileInConstructorSliceViolation {
	private final Pattern instancePattern;

	InputJitInefficiencyStructuralPatternCompileInConstructorSliceViolation(String s) {
		final var p = Pattern.compile("\\d+"); // violation: 'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.
		this.instancePattern = p;
		System.out.println(s);
	}
}
// === end ===

// === case: structural_reusable_date_time_formatter ===
// skip-reason: reusable object creation
// imports: java.time.format.DateTimeFormatter
// imports: java.util.Date
class InputJitInefficiencyStructuralReusableDateTimeFormatterSliceViolation {
	void m(Date d) {
		final var fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // violation: 'DateTimeFormatter.ofPattern(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt + " " + d);
	}
}
// === end ===

// === case: structural_reusable_decimal_format ===
// skip-reason: reusable object creation
// imports: java.text.DecimalFormat
class InputJitInefficiencyStructuralReusableDecimalFormatSliceViolation {
	void m(double n) {
		final var fmt = new DecimalFormat("#,##0.00"); // violation: 'new DecimalFormat(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt.format(n));
	}
}
// === end ===

// === case: structural_reusable_pattern_compile ===
// skip-reason: reusable object creation
// imports: java.util.regex.Pattern
class InputJitInefficiencyStructuralReusablePatternCompileSliceViolation {
	void m(String s) {
		final var matched = Pattern.compile("\\d+").matcher(s).matches(); // violation: 'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(matched);
	}
}
// === end ===

// === case: structural_reusable_simple_date_format ===
// skip-reason: reusable object creation
// imports: java.text.SimpleDateFormat
// imports: java.util.Date
class InputJitInefficiencyStructuralReusableSimpleDateFormatSliceViolation {
	void m(Date d) {
		final var fmt = new SimpleDateFormat("yyyy-MM-dd"); // violation: 'new SimpleDateFormat(...)' creates a reusable object on every call; hoist to a static final field.
		System.out.println(fmt.format(d));
	}
}
// === end ===

// === case: tier2_do_while_array_lhs_external_index ===
class InputJitInefficiencyTier2DoWhileArrayLhsExternalIndexSliceViolation {
	void m(int k) {
		final var arr = new String[10];
		do arr[k] = arr[k] + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (arr[k].length() < 5);
	}
}
// === end ===

// === case: tier2_do_while_chained_index ===
class InputJitInefficiencyTier2DoWhileChainedIndexSliceViolation {
	void m(int k, int j) {
		final var matrix = new String[10][10];
		do matrix[k][j] = matrix[k][j] + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (matrix[k][j].length() < 5);
	}
}
// === end ===

// === case: tier2_do_while_field_this ===
class InputJitInefficiencyTier2DoWhileFieldThisSliceViolation {
	String f;

	void m() {
		do this.f = this.f + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (this.f.length() < 5);
	}
}
// === end ===