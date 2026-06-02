package com.etk2000.checkstyle.inputs.fieldsorting;

// === case: annotation_explicit_value_keyword_normalization ===
class InputFieldSortingAnnotationExplicitValueKeywordNormalizationSliceViolation {
	@SuppressWarnings("unused")
	int alpha = 2;
	@SuppressWarnings("unused")
	int beta = 1;
}
// === end ===

// === case: annotation_qualified ===
class InputFieldSortingAnnotationQualifiedSliceViolation {
	@Deprecated
	int alpha;
	@SuppressWarnings("unused")
	int beta;
}
// === end ===

// === case: array_c_style_mixed_style_reorders ===
class InputFieldSortingArrayCStyleMixedStyleReordersSliceViolation {
	int[] alpha, zebra;
}
// === end ===

// === case: array_c_style_plain_sibling_reorders ===
class InputFieldSortingArrayCStylePlainSiblingReordersSliceViolation {
	int codes;
	int[] elements;
}
// === end ===

// === case: array_c_style_reorders ===
class InputFieldSortingArrayCStyleReordersSliceViolation {
	int[] alpha, zebra;
}
// === end ===

// === case: array_c_style_two_dimensional_reorders ===
class InputFieldSortingArrayCStyleTwoDimensionalReordersSliceViolation {
	int[][] alpha, zebra;
}
// === end ===

// === case: array_c_style_whitespace_inside_brackets ===
class InputFieldSortingArrayCStyleWhitespaceInsideBracketsSliceViolation {
	int alpha;
	int[ ] zebra;
}
// === end ===

// === case: array_c_style_with_string_group_consolidation ===
class InputFieldSortingArrayCStyleWithStringGroupConsolidationSliceViolation {
	int[] elements;
	String alpha, zebra;
}
// === end ===

// === case: array_mixed_bracket_style_single_declarator ===
class InputFieldSortingArrayMixedBracketStyleSingleDeclaratorSliceViolation {
	String[][] alpha, zebra;
}
// === end ===

// === case: dependency_forward_ref ===
class InputFieldSortingDependencyForwardRefSliceViolation {
	int alpha = 10;
	int beta = alpha + 1;
}
// === end ===

// === case: enumconstant_wrong_key_explicit ===
@SuppressWarnings("unused")
enum InputFieldSortingEnumConstantWrongKeyExplicitSliceViolation {
	ALPHA,
	ZEBRA
}
// === end ===

// === case: enumspan_annotation_arg_empty_parens ===
enum InputFieldSortingEnumSpanAnnotationArgEmptyParensSliceViolation {
	@Deprecated
	ALPHA,
	ZETA
}
// === end ===

// === case: enumspan_annotation_arg_member_value_pair ===
enum InputFieldSortingEnumSpanAnnotationArgMemberValuePairSliceViolation {
	@SuppressWarnings("unused")
	ALPHA,
	ZETA
}
// === end ===

// === case: field_annotation_consolidation_skips_trailing_comment ===
class InputFieldSortingFieldAnnotationConsolidationSkipsTrailingCommentSliceViolation {
	int height, width; // in pixels
}
// === end ===

// === case: field_annotation_empty_parens_normalization ===
class InputFieldSortingFieldAnnotationEmptyParensNormalizationSliceViolation {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_block_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInBlockCommentSliceViolation {
	String /* @Nullable */ alpha, beta;
}
// === end ===

// === case: field_annotation_ignores_at_in_line_comment ===
class InputFieldSortingFieldAnnotationIgnoresAtInLineCommentSliceViolation {
	String alpha, beta; // @Deprecated docs
}
// === end ===

// === case: field_annotation_order_qualified ===
class InputFieldSortingFieldAnnotationOrderQualifiedSliceViolation {
	@Deprecated
	String alpha;
	@SuppressWarnings("unused")
	String beta;
}
// === end ===

// === case: field_dependency_initializer_on_continuation_line ===
class InputFieldSortingFieldDependencyInitializerOnContinuationLineSliceViolation {
	int alpha = 10;
	int beta = 1
			+ alpha;
}
// === end ===

// === case: field_depth_tracked_allfieldnames_excludes_nested_local ===
class InputFieldSortingFieldDepthTrackedAllfieldnamesExcludesNestedLocalSliceViolation {
	int alpha = 2;
	int beta = 1;

	void m() {
		final var charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: field_duplicate_text_below_does_not_steal_endidx ===
@SuppressWarnings("unused")
class InputFieldSortingFieldDuplicateTextBelowDoesNotStealEndIdxSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method() {
		final var alpha = 2;
		System.out.println(alpha);
	}
}
// === end ===

// === case: field_interleaved_dependency_bail ===
// skip-reason: cannot reorder fields across an interleaved static or instance field
class InputFieldSortingFieldInterleavedDependencyBailSliceViolation {
	int zebra = alpha + 1;
	static int s;
	int alpha;
}
// === end ===

// === case: field_local_in_method_body_not_treated_as_field ===
@SuppressWarnings("unused")
class InputFieldSortingFieldLocalInMethodBodyNotTreatedAsFieldSliceViolation {
	int alpha = 2;
	int beta = 1;

	void method() {
		final var charlie = 3;
		System.out.println(charlie);
	}
}
// === end ===

// === case: name_within_decl_inline_annotation ===
class InputFieldSortingNameWithinDeclInlineAnnotationSliceViolation {
	@Deprecated
	private int x, y;
}
// === end ===

// === case: name_within_decl_inline_annotation_c_style_skips ===
// skip-reason: a declarator carries its own C-style array brackets
class InputFieldSortingNameWithinDeclInlineAnnotationCStyleSkipsSliceViolation {
	@Deprecated
	private int y[], x;
}
// === end ===

// === case: name_within_decl_inline_annotation_interior_comment_skips ===
// skip-reason: cannot reorder names in a multi-variable declaration containing a comment
class InputFieldSortingNameWithinDeclInlineAnnotationInteriorCommentSkipsSliceViolation {
	@Deprecated
	private int zebra, /* keep me */ alpha;
}
// === end ===

// === case: name_within_decl_inline_annotation_wrapped ===
class InputFieldSortingNameWithinDeclInlineAnnotationWrappedSliceViolation {
	@Deprecated
	private int x, y;
}
// === end ===

// === case: typeargannotation_empty_parens ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationEmptyParensSliceViolation {
	List<@TAnnA String> aField;
	List<@TAnnB String> bField;
}
// === end ===

// === case: typeargannotation_explicit_value_keyword ===
// imports: java.util.List
class InputFieldSortingTypeArgAnnotationExplicitValueKeywordSliceViolation {
	List<@TAnnParam(1) String> lower;
	List<@TAnnParam(2) String> higher;
}
// === end ===