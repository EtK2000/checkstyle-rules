package com.etk2000.checkstyle.inputs.explicitinitialization;

// === case: entry_state_block_comment_stray_quote ===
class InputExplicitInitEntryStateBlockCommentStrayQuoteSliceViolation {
	/* open
	stray " */ int x;
}
// === end ===

// === case: multi_declaration_first_var ===
class InputExplicitInitMultiDeclarationFirstVarSliceViolation {
	int x, y;
}
// === end ===

// === case: multi_declaration_first_var_comma_in_comment ===
class InputExplicitInitMultiDeclarationFirstVarCommaInCommentSliceViolation {
	int x /* , */, y;
}
// === end ===

// === case: multi_declaration_second_var ===
class InputExplicitInitMultiDeclarationSecondVarSliceViolation {
	int x, y;
}
// === end ===

// === case: remove_binary_zero ===
class InputExplicitInitRemoveBinaryZeroSliceViolation {
	int x;
}
// === end ===

// === case: remove_binary_zero_uppercase_prefix ===
class InputExplicitInitRemoveBinaryZeroUppercasePrefixSliceViolation {
	int x;
}
// === end ===

// === case: remove_boolean_comment_between_name_and_eq ===
class InputExplicitInitRemoveBooleanCommentBetweenNameAndEqSliceViolation {
	boolean b /* x = y */;
}
// === end ===

// === case: remove_boolean_false ===
class InputExplicitInitRemoveBooleanFalseSliceViolation {
	boolean b;
}
// === end ===

// === case: remove_char_null ===
class InputExplicitInitRemoveCharNullSliceViolation {
	char c;
}
// === end ===

// === case: remove_char_null_comment_after_value ===
class InputExplicitInitRemoveCharNullCommentAfterValueSliceViolation {
	char c /* z */;
}
// === end ===

// === case: remove_char_null_comment_both_sides ===
class InputExplicitInitRemoveCharNullCommentBothSidesSliceViolation {
	char c /* a */ /* b */;
}
// === end ===

// === case: remove_double_underflow_to_zero ===
class InputExplicitInitRemoveDoubleUnderflowToZeroSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero ===
class InputExplicitInitRemoveDoubleZeroSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_exponent ===
class InputExplicitInitRemoveDoubleZeroExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_exponent_underscore ===
class InputExplicitInitRemoveDoubleZeroExponentUnderscoreSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroExponentMinusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroExponentPlusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_leading_dot ===
class InputExplicitInitRemoveDoubleZeroLeadingDotSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_multi_digit_exponent ===
class InputExplicitInitRemoveDoubleZeroMultiDigitExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_multiple_decimals ===
class InputExplicitInitRemoveDoubleZeroMultipleDecimalsSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_exponent ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentMinusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentPlusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentMinusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentPlusSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_trailing_dot ===
class InputExplicitInitRemoveDoubleZeroTrailingDotSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_uppercase_exponent ===
class InputExplicitInitRemoveDoubleZeroUppercaseExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_uppercase_suffix ===
class InputExplicitInitRemoveDoubleZeroUppercaseSuffixSliceViolation {
	double d;
}
// === end ===

// === case: remove_double_zero_with_suffix ===
class InputExplicitInitRemoveDoubleZeroWithSuffixSliceViolation {
	double d;
}
// === end ===

// === case: remove_float_zero ===
class InputExplicitInitRemoveFloatZeroSliceViolation {
	float f;
}
// === end ===

// === case: remove_float_zero_trailing_dot ===
class InputExplicitInitRemoveFloatZeroTrailingDotSliceViolation {
	float f;
}
// === end ===

// === case: remove_float_zero_with_underscores ===
class InputExplicitInitRemoveFloatZeroWithUnderscoresSliceViolation {
	float f;
}
// === end ===

// === case: remove_float_zero_without_decimal ===
class InputExplicitInitRemoveFloatZeroWithoutDecimalSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero ===
class InputExplicitInitRemoveHexZeroSliceViolation {
	int x;
}
// === end ===

// === case: remove_hex_zero_double_p_exponent ===
class InputExplicitInitRemoveHexZeroDoublePExponentSliceViolation {
	double d;
}
// === end ===

// === case: remove_hex_zero_nonzero_uppercase_p_exponent ===
class InputExplicitInitRemoveHexZeroNonzeroUppercasePExponentSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_double_suffix ===
class InputExplicitInitRemoveHexZeroPExponentDoubleSuffixSliceViolation {
	double d;
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_minus_sign ===
class InputExplicitInitRemoveHexZeroPExponentMinusSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_plus_sign ===
class InputExplicitInitRemoveHexZeroPExponentPlusSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero_uppercase_prefix ===
class InputExplicitInitRemoveHexZeroUppercasePrefixSliceViolation {
	int x;
}
// === end ===

// === case: remove_hex_zero_with_long_suffix ===
class InputExplicitInitRemoveHexZeroWithLongSuffixSliceViolation {
	long x;
}
// === end ===

// === case: remove_hex_zero_with_long_suffix_comment_after ===
class InputExplicitInitRemoveHexZeroWithLongSuffixCommentAfterSliceViolation {
	long x /* hex */;
}
// === end ===

// === case: remove_hex_zero_with_nonzero_p_exponent ===
class InputExplicitInitRemoveHexZeroWithNonzeroPExponentSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero_with_p_exponent ===
class InputExplicitInitRemoveHexZeroWithPExponentSliceViolation {
	float f;
}
// === end ===

// === case: remove_hex_zero_with_uppercase_p_exponent ===
class InputExplicitInitRemoveHexZeroWithUppercasePExponentSliceViolation {
	float f;
}
// === end ===

// === case: remove_int_zero ===
class InputExplicitInitRemoveIntZeroSliceViolation {
	int x;
}
// === end ===

// === case: remove_int_zero_comment_adjacent_no_space ===
class InputExplicitInitRemoveIntZeroCommentAdjacentNoSpaceSliceViolation {
	int x /* c */ /* d */;
}
// === end ===

// === case: remove_int_zero_comment_after_value ===
class InputExplicitInitRemoveIntZeroCommentAfterValueSliceViolation {
	int x /* c */;
}
// === end ===

// === case: remove_int_zero_comment_before_value ===
class InputExplicitInitRemoveIntZeroCommentBeforeValueSliceViolation {
	int x /* c */;
}
// === end ===

// === case: remove_int_zero_comment_both_sides ===
class InputExplicitInitRemoveIntZeroCommentBothSidesSliceViolation {
	int x /* a */ /* b */;
}
// === end ===

// === case: remove_int_zero_comment_default_text ===
class InputExplicitInitRemoveIntZeroCommentDefaultTextSliceViolation {
	int x /* 0 */;
}
// === end ===

// === case: remove_int_zero_comment_multiple_same_side ===
class InputExplicitInitRemoveIntZeroCommentMultipleSameSideSliceViolation {
	int x /* a */ /* b */;
}
// === end ===

// === case: remove_int_zero_comment_with_stray_quote ===
class InputExplicitInitRemoveIntZeroCommentWithStrayQuoteSliceViolation {
	int x /* he said " */;
}
// === end ===

// === case: remove_int_zero_comment_with_structural_chars ===
class InputExplicitInitRemoveIntZeroCommentWithStructuralCharsSliceViolation {
	int x /* a;b,c=d */;
}
// === end ===

// === case: remove_int_zero_doc_comment_before_value ===
class InputExplicitInitRemoveIntZeroDocCommentBeforeValueSliceViolation {
	int x /** d */;
}
// === end ===

// === case: remove_int_zero_tab_between_eq_and_value ===
class InputExplicitInitRemoveIntZeroTabBetweenEqAndValueSliceViolation {
	int x;
}
// === end ===

// === case: remove_int_zero_trailing_line_comment ===
class InputExplicitInitRemoveIntZeroTrailingLineCommentSliceViolation {
	int x; // keep me
}
// === end ===

// === case: remove_long_zero ===
class InputExplicitInitRemoveLongZeroSliceViolation {
	long x;
}
// === end ===

// === case: remove_long_zero_lowercase_suffix ===
class InputExplicitInitRemoveLongZeroLowercaseSuffixSliceViolation {
	long x;
}
// === end ===

// === case: remove_null ===
class InputExplicitInitRemoveNullSliceViolation {
	Object o;
}
// === end ===

// === case: remove_null_generic_type ===
// imports: java.util.Map
class InputExplicitInitRemoveNullGenericTypeSliceViolation {
	Map<String, Integer> m;
}
// === end ===

// === case: remove_octal_zero ===
class InputExplicitInitRemoveOctalZeroSliceViolation {
	int x;
}
// === end ===