// === case: empty_parens_among_modifiers_embedded ===
class InputRedundantAnnotationSyntaxEmptyParensAmongModifiersEmbeddedSliceViolation {
	@B
	public static int field;
}
// === end ===

// === case: empty_parens_before_declaration ===
class InputRedundantAnnotationSyntaxEmptyParensBeforeDeclarationSliceViolation {
	@Override
	void f() {}
}
// === end ===

// === case: empty_parens_on_enum_and_constant ===
@B
enum InputRedundantAnnotationSyntaxEmptyParensOnEnumAndConstantSliceViolation {
	@B
	ALPHA
}
// === end ===

// === case: empty_parens_tail_block_comment ===
class InputRedundantAnnotationSyntaxEmptyParensTailBlockCommentSliceViolation {
	@B
	/* c */int field;
}
// === end ===

// === case: explicit_value_char_literal_decoys ===
class InputRedundantAnnotationSyntaxExplicitValueCharLiteralDecoysSliceViolation {
	@C('"')
	int quote;
	@C('(')
	int open;
	@C('/')
	int slash;
	@C('\'')
	int tick;
}
// === end ===

// === case: explicit_value_numeric_edge_values ===
class InputRedundantAnnotationSyntaxExplicitValueNumericEdgeValuesSliceViolation {
	@C(-1)
	int unaryMinus;
	@C(Integer.MIN_VALUE)
	int smallestInt;
	@Dbl(-0.0)
	int negativeZero;
	@Dbl(Double.NaN)
	int notANumber;
}
// === end ===

// === case: explicit_value_numeric_literal_forms ===
class InputRedundantAnnotationSyntaxExplicitValueNumericLiteralFormsSliceViolation {
	@C(0b1010)
	int binary;
	@C(0xFF)
	int hex;
	@C(1_000)
	int grouped;
}
// === end ===

// === case: multi_line_empty_parens_code_after_close ===
class InputRedundantAnnotationSyntaxMultiLineEmptyParensCodeAfterCloseSliceViolation {
	@B
	int field;
}
// === end ===