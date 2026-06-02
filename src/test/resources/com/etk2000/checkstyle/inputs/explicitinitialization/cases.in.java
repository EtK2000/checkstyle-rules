package com.etk2000.checkstyle.inputs.explicitinitialization;

// === case: entry_state_block_comment_stray_quote ===
class InputExplicitInitEntryStateBlockCommentStrayQuoteSliceViolation {
	/* open
	stray " */ int x = 0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: multi_declaration_first_var ===
class InputExplicitInitMultiDeclarationFirstVarSliceViolation {
	int x = 0, y; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: multi_declaration_first_var_comma_in_comment ===
class InputExplicitInitMultiDeclarationFirstVarCommaInCommentSliceViolation {
	int x = 0 /* , */, y; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: multi_declaration_second_var ===
class InputExplicitInitMultiDeclarationSecondVarSliceViolation {
	int x, y = 0; // violation: Variable 'y' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_binary_zero ===
class InputExplicitInitRemoveBinaryZeroSliceViolation {
	int x = 0b0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_binary_zero_uppercase_prefix ===
class InputExplicitInitRemoveBinaryZeroUppercasePrefixSliceViolation {
	int x = 0B0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_boolean_comment_between_name_and_eq ===
class InputExplicitInitRemoveBooleanCommentBetweenNameAndEqSliceViolation {
	boolean b /* x = y */ = false; // violation: Variable 'b' explicitly initialized to 'false' (default value for its type).
}
// === end ===

// === case: remove_boolean_false ===
class InputExplicitInitRemoveBooleanFalseSliceViolation {
	boolean b = false; // violation: Variable 'b' explicitly initialized to 'false' (default value for its type).
}
// === end ===

// === case: remove_char_null ===
class InputExplicitInitRemoveCharNullSliceViolation {
	char c = '\0'; // violation: Variable 'c' explicitly initialized to '\0' (default value for its type).
}
// === end ===

// === case: remove_char_null_comment_after_value ===
class InputExplicitInitRemoveCharNullCommentAfterValueSliceViolation {
	char c = '\0' /* z */; // violation: Variable 'c' explicitly initialized to '\0' (default value for its type).
}
// === end ===

// === case: remove_char_null_comment_both_sides ===
class InputExplicitInitRemoveCharNullCommentBothSidesSliceViolation {
	char c = /* a */ '\0' /* b */; // violation: Variable 'c' explicitly initialized to '\0' (default value for its type).
}
// === end ===

// === case: remove_double_underflow_to_zero ===
class InputExplicitInitRemoveDoubleUnderflowToZeroSliceViolation {
	double d = 1.0e-999; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero ===
class InputExplicitInitRemoveDoubleZeroSliceViolation {
	double d = 0.0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_exponent ===
class InputExplicitInitRemoveDoubleZeroExponentSliceViolation {
	double d = 0.0e0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_exponent_underscore ===
class InputExplicitInitRemoveDoubleZeroExponentUnderscoreSliceViolation {
	double d = 0.0e1_0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroExponentMinusSliceViolation {
	double d = 0.0e-0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroExponentPlusSliceViolation {
	double d = 0.0e+0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_leading_dot ===
class InputExplicitInitRemoveDoubleZeroLeadingDotSliceViolation {
	double d = .0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_multi_digit_exponent ===
class InputExplicitInitRemoveDoubleZeroMultiDigitExponentSliceViolation {
	double d = 0.0e12; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_multiple_decimals ===
class InputExplicitInitRemoveDoubleZeroMultipleDecimalsSliceViolation {
	double d = 0.000; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_exponent ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentSliceViolation {
	double d = 0.0e1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentMinusSliceViolation {
	double d = 0.0e-1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroExponentPlusSliceViolation {
	double d = 0.0e+1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentSliceViolation {
	double d = 0.0E1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent_with_minus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentMinusSliceViolation {
	double d = 0.0E-1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_nonzero_uppercase_exponent_with_plus_sign ===
class InputExplicitInitRemoveDoubleZeroNonzeroUppercaseExponentPlusSliceViolation {
	double d = 0.0E+1; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_trailing_dot ===
class InputExplicitInitRemoveDoubleZeroTrailingDotSliceViolation {
	double d = 0.; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_uppercase_exponent ===
class InputExplicitInitRemoveDoubleZeroUppercaseExponentSliceViolation {
	double d = 0.0E0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_uppercase_suffix ===
class InputExplicitInitRemoveDoubleZeroUppercaseSuffixSliceViolation {
	double d = 0.0D; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_double_zero_with_suffix ===
class InputExplicitInitRemoveDoubleZeroWithSuffixSliceViolation {
	double d = 0.0d; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_float_zero ===
class InputExplicitInitRemoveFloatZeroSliceViolation {
	float f = 0.0f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_float_zero_trailing_dot ===
class InputExplicitInitRemoveFloatZeroTrailingDotSliceViolation {
	float f = 0.f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_float_zero_with_underscores ===
class InputExplicitInitRemoveFloatZeroWithUnderscoresSliceViolation {
	float f = 0_0.0_0f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_float_zero_without_decimal ===
class InputExplicitInitRemoveFloatZeroWithoutDecimalSliceViolation {
	float f = 0F; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero ===
class InputExplicitInitRemoveHexZeroSliceViolation {
	int x = 0x0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_double_p_exponent ===
class InputExplicitInitRemoveHexZeroDoublePExponentSliceViolation {
	double d = 0x0.0p0; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_nonzero_uppercase_p_exponent ===
class InputExplicitInitRemoveHexZeroNonzeroUppercasePExponentSliceViolation {
	float f = 0x0.0P1f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_double_suffix ===
class InputExplicitInitRemoveHexZeroPExponentDoubleSuffixSliceViolation {
	double d = 0x0.0p0d; // violation: Variable 'd' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_minus_sign ===
class InputExplicitInitRemoveHexZeroPExponentMinusSliceViolation {
	float f = 0x0.0p-1f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_p_exponent_with_plus_sign ===
class InputExplicitInitRemoveHexZeroPExponentPlusSliceViolation {
	float f = 0x0.0p+1f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_uppercase_prefix ===
class InputExplicitInitRemoveHexZeroUppercasePrefixSliceViolation {
	int x = 0X0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_with_long_suffix ===
class InputExplicitInitRemoveHexZeroWithLongSuffixSliceViolation {
	long x = 0x0L; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_with_long_suffix_comment_after ===
class InputExplicitInitRemoveHexZeroWithLongSuffixCommentAfterSliceViolation {
	long x = 0x0L /* hex */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_with_nonzero_p_exponent ===
class InputExplicitInitRemoveHexZeroWithNonzeroPExponentSliceViolation {
	float f = 0x0.0p1f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_with_p_exponent ===
class InputExplicitInitRemoveHexZeroWithPExponentSliceViolation {
	float f = 0x0.0p0f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_hex_zero_with_uppercase_p_exponent ===
class InputExplicitInitRemoveHexZeroWithUppercasePExponentSliceViolation {
	float f = 0x0.0P0f; // violation: Variable 'f' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero ===
class InputExplicitInitRemoveIntZeroSliceViolation {
	int x = 0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_adjacent_no_space ===
class InputExplicitInitRemoveIntZeroCommentAdjacentNoSpaceSliceViolation {
	int x =/* c */0/* d */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_after_value ===
class InputExplicitInitRemoveIntZeroCommentAfterValueSliceViolation {
	int x = 0 /* c */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_before_value ===
class InputExplicitInitRemoveIntZeroCommentBeforeValueSliceViolation {
	int x = /* c */ 0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_both_sides ===
class InputExplicitInitRemoveIntZeroCommentBothSidesSliceViolation {
	int x = /* a */ 0 /* b */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_default_text ===
class InputExplicitInitRemoveIntZeroCommentDefaultTextSliceViolation {
	int x = 0 /* 0 */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_multiple_same_side ===
class InputExplicitInitRemoveIntZeroCommentMultipleSameSideSliceViolation {
	int x = 0 /* a */ /* b */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_with_stray_quote ===
class InputExplicitInitRemoveIntZeroCommentWithStrayQuoteSliceViolation {
	int x = 0 /* he said " */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_comment_with_structural_chars ===
class InputExplicitInitRemoveIntZeroCommentWithStructuralCharsSliceViolation {
	int x = 0 /* a;b,c=d */; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_doc_comment_before_value ===
class InputExplicitInitRemoveIntZeroDocCommentBeforeValueSliceViolation {
	int x = /** d */ 0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_tab_between_eq_and_value ===
class InputExplicitInitRemoveIntZeroTabBetweenEqAndValueSliceViolation {
	int x =	0; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_int_zero_trailing_line_comment ===
class InputExplicitInitRemoveIntZeroTrailingLineCommentSliceViolation {
	int x = 0; // keep me // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_long_zero ===
class InputExplicitInitRemoveLongZeroSliceViolation {
	long x = 0L; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_long_zero_lowercase_suffix ===
class InputExplicitInitRemoveLongZeroLowercaseSuffixSliceViolation {
	long x = 0l; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===

// === case: remove_null ===
class InputExplicitInitRemoveNullSliceViolation {
	Object o = null; // violation: Variable 'o' explicitly initialized to 'null' (default value for its type).
}
// === end ===

// === case: remove_null_generic_type ===
// imports: java.util.Map
class InputExplicitInitRemoveNullGenericTypeSliceViolation {
	Map<String, Integer> m = null; // violation: Variable 'm' explicitly initialized to 'null' (default value for its type).
}
// === end ===

// === case: remove_octal_zero ===
class InputExplicitInitRemoveOctalZeroSliceViolation {
	int x = 00; // violation: Variable 'x' explicitly initialized to '0' (default value for its type).
}
// === end ===