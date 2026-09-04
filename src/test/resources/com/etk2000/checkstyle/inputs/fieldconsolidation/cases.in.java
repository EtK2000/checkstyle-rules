package com.etk2000.checkstyle.inputs.fieldconsolidation;

class L<T> {
}

@interface Multi {
	int a() default 0;

	int b() default 0;
}

// === case: after_multi_var ===
class InputFieldConsolidationViolationAfterMultiVar {
	int a, b;
	int c; // violation: Fields 'c' and 'b' (type 'int') must be declared on one line.
}
// === end ===

// === case: annotated_dimension_after_generic_match ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedDimensionAfterGenericMatch {
	List<String> @ViolationTypeAnn [] alpha;
	List<String> @ViolationTypeAnn [] beta; // violation: Fields 'beta' and 'alpha' (type 'List<String> @ViolationTypeAnn []') must be declared on one line.
}
// === end ===

// === case: annotated_dimension_match ===
class InputFieldConsolidationViolationAnnotatedDimensionMatch {
	String @ViolationTypeAnn [] alpha;
	String @ViolationTypeAnn [] beta; // violation: Fields 'beta' and 'alpha' (type 'String @ViolationTypeAnn []') must be declared on one line.
}
// === end ===

// === case: annotated_dimension_param_order_match ===
class InputFieldConsolidationViolationAnnotatedDimensionParamOrderMatch {
	String @Multi(b = 2, a = 1) [] alpha;
	String @Multi(a = 1, b = 2) [] beta; // violation: Fields 'beta' and 'alpha' (type 'String @Multi(a=1,b=2) []') must be declared on one line.
}
// === end ===

// === case: annotated_lower_bound_match ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedLowerBoundMatch {
	List<? super @ViolationTypeAnn Number> alpha;
	List<? super @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? super @ViolationTypeAnn Number>') must be declared on one line.
}
// === end ===

// === case: annotated_multi_dimension_match ===
class InputFieldConsolidationViolationAnnotatedMultiDimensionMatch {
	String @ViolationTypeAnn [] @ViolationTypeAnn [] alpha;
	String @ViolationTypeAnn [] @ViolationTypeAnn [] beta; // violation: Fields 'beta' and 'alpha' (type 'String @ViolationTypeAnn [] @ViolationTypeAnn []') must be declared on one line.
}
// === end ===

// === case: annotated_upper_bound ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedBound {
	List<? extends @ViolationTypeAnn Number> alpha;
	List<? extends @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? extends @ViolationTypeAnn Number>') must be declared on one line.
}
// === end ===

// === case: annotation_at_depth_cap_match ===
class InputFieldConsolidationViolationAnnotationAtDepthCapMatch {
	@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W)))))))))))))))))))))))))))))))))))))))))))))))))
	int alpha;
	@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W)))))))))))))))))))))))))))))))))))))))))))))))))
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: annotation_line_trailing_comment_merges ===
class InputFieldConsolidationViolationAnnotationLineTrailingComment {
	@Deprecated
	int alpha;
	@Deprecated // kept for the legacy wire format
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: annotation_order_swapped ===
class InputFieldConsolidationViolationAnnotationOrder {
	@Deprecated
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: annotation_value_whitespace_variant_match ===
class InputFieldConsolidationViolationAnnotationValueWhitespaceVariantMatch {
	@Multi(a = -1)
	int alpha;
	@Multi(a = - 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: anonymous_class ===
class InputFieldConsolidationViolationAnonymousClass {
	Runnable r = new Runnable() {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: anonymous_class_initialized_fields ===
class InputFieldConsolidationViolationAnonymousClassInitializedFields {
	Runnable r = new Runnable() {
		int alpha = 1;
		int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.

		@Override
		public void run() {}
	};
}
// === end ===

// === case: array_mixed_styles ===
class InputFieldConsolidationViolationArray {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_both_c_style ===
class InputFieldConsolidationViolationArrayTypeBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_both_c_style_merged_three_fields ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMerged {
	int alpha[];
	int beta[], gamma[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_both_c_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMultidim {
	int alpha[][];
	int beta[][]; // violation: Fields 'beta' and 'alpha' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_both_java_style ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyle {
	int[] alpha;
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_both_java_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyleMultidimensional {
	int[][] alpha;
	int[][] beta; // violation: Fields 'beta' and 'alpha' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_only ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnly {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_only_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnlyMultidim {
	int[][] alpha;
	int beta[][]; // violation: Fields 'beta' and 'alpha' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrSpaceBeforeBrackets {
	int alpha[];
	int beta [], gamma[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_generic_array_type_merges ===
// imports: java.util.List
class InputFieldConsolidationViolationArrayTypeCStylePrevGenericArrayType {
	List<String>[] alpha[];
	List<String>[][] beta; // violation: Fields 'beta' and 'alpha' (type 'List<String>[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_generic_type_merges ===
// imports: java.util.List
class InputFieldConsolidationViolationArrayTypeCStylePrevGenericType {
	List<String> alpha[];
	List<String>[] beta; // violation: Fields 'beta' and 'alpha' (type 'List<String>[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurr {
	int alpha[];
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr_multidim ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurrMultidim {
	int alpha[][];
	int[][] beta; // violation: Fields 'beta' and 'alpha' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_multi_var ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVar {
	int alpha[], beta[];
	int gamma[]; // violation: Fields 'gamma' and 'beta' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_multi_var_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVarMultidim {
	int alpha[][], beta[][];
	int[][] gamma; // violation: Fields 'gamma' and 'beta' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_qualified_type_annotation_merges ===
class InputFieldConsolidationViolationArrayTypeCStylePrevQualifiedTypeAnnotation {
	java.lang.@ViolationTypeAnn String alpha[];
	java.lang.@ViolationTypeAnn String[] beta; // violation: Fields 'beta' and 'alpha' (type 'java.lang.@ViolationTypeAnn String[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStylePrevSpaceBeforeBrackets {
	int alpha [];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_type_argument_annotation_merges ===
// imports: java.util.List
class InputFieldConsolidationViolationArrayTypeCStylePrevTypeArgumentAnnotation {
	List<@ViolationTypeAnn String> alpha[];
	List<@ViolationTypeAnn String>[] beta; // violation: Fields 'beta' and 'alpha' (type 'List<@ViolationTypeAnn String>[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_final_modifier ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithFinal {
	final int alpha[];
	final int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_modifier_annotation_type_dims_merges ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithModifierAnnotationTypeDims {
	@Deprecated int[] alpha[];
	@Deprecated int[][] beta; // violation: Fields 'beta' and 'alpha' (type 'int[][]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_tab_before_semicolon ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTab {
	int alpha[]	;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_trailing_line_comment ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTrailing {
	int alpha[]; // a note
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: backward_scan_hits_text_block_annotation ===
class InputFieldConsolidationViolationTextBlockAnnotation {
	@SuppressWarnings("""
		x;
		""") String p;
	@SuppressWarnings("""
		x;
		""") String q; // violation: Fields 'q' and 'p' (type 'String') must be declared on one line.
}
// === end ===

// === case: blank_line_between_merges ===
class InputFieldConsolidationViolationBlankLineBetweenMerges {
	int alpha;

	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: blank_lines_around_other_type_field_kept ===
class InputFieldConsolidationViolationBlankLinesAroundOtherTypeFieldKept {
	int alpha;

	String label;

	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: boolean_primitive ===
class InputFieldConsolidationViolationBoolean {
	boolean active;
	boolean visible; // violation: Fields 'visible' and 'active' (type 'boolean') must be declared on one line.
}
// === end ===

// === case: both_c_style_arrays ===
class InputFieldConsolidationViolationBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: both_lines_trailing_comment_merges ===
class InputFieldConsolidationViolationBothLinesTrailingComment {
	int alpha; // vertical extent
	int beta; // horizontal extent // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: c_style_brackets_on_line_after_name_merges ===
class InputFieldConsolidationViolationCStyleBracketsOnLineAfterNameMerges {
	int alpha
			[];
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_chain_moves_above_first_line ===
class InputFieldConsolidationViolationCarriedBlockCommentChainMovesAboveFirstLine {
	/* first
	 */ /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_code_ahead_nothing_to_move_merges ===
class InputFieldConsolidationViolationCarriedBlockCommentCodeAheadNothingToMoveMerges {
	static class Item {
	}

	char zulu = 'z'; /* legacy
	 */ Item alpha;
	Item beta; // violation: Fields 'beta' and 'alpha' (type 'Item') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_code_ahead_of_chain_skips ===
// skip-reason: cannot consolidate: the merged declaration opens inside a block comment that starts after other code, leaving the comments that have to move no line of their own above it
class InputFieldConsolidationViolationCarriedBlockCommentCodeAheadOfChainSkips {
	char zulu = 'z'; /* first
	 */ /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_code_ahead_skips ===
// skip-reason: cannot consolidate: the merged declaration opens inside a block comment that starts after other code, leaving the comments that have to move no line of their own above it
class InputFieldConsolidationViolationCarriedBlockCommentCodeAheadSkips {
	char zulu = 'z'; /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_code_inside_chain_skips ===
// skip-reason: cannot consolidate: the merged declaration opens inside a block comment that starts after other code, leaving the comments that have to move no line of their own above it
class InputFieldConsolidationViolationCarriedBlockCommentCodeInsideChainSkips {
	/* first
	 */ char zulu = 'z'; /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_comment_ahead_moves_above_line ===
class InputFieldConsolidationViolationCarriedBlockCommentCommentAheadMovesAboveLine {
	/* note */ /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_moves_above ===
class InputFieldConsolidationViolationCarriedBlockCommentMovesAbove {
	/* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_rebuilt_moves_above ===
class InputFieldConsolidationViolationCarriedBlockCommentRebuiltMovesAbove {
	/* legacy
	 */ int alpha[];
	int[] /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_supplementary_char_comment_ahead_moves_above_line ===
class InputFieldConsolidationViolationCarriedBlockCommentSupplementaryCharCommentAheadMovesAboveLine {
	/* 𝑥 */ /* legacy
	 */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: carried_block_comment_with_comment_after_moves_above ===
class InputFieldConsolidationViolationCarriedBlockCommentWithCommentAfterMovesAbove {
	/* legacy
	 */ /* kept */ int alpha;
	int /* moved */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: collapse_anonymous_class_one_line_body ===
class InputFieldConsolidationViolationCollapseAnonymousClassOneLineBody {
	Object alpha = new Object() {};
	Object beta = // violation: Fields 'beta' and 'alpha' (type 'Object') must be declared on one line.
			new Object() { int c; };
}
// === end ===

// === case: collapse_array_initializer ===
class InputFieldConsolidationViolationCollapseArrayInitializer {
	int[] alpha;
	int[] beta = { // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
			1, 2
	};
}
// === end ===

// === case: collapse_binary_operator ===
class InputFieldConsolidationViolationCollapseBinaryOperator {
	int alpha;
	int beta = 1 // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			+ 2;
}
// === end ===

// === case: collapse_binary_operator_break_after ===
class InputFieldConsolidationViolationCollapseBinaryOperatorBreakAfter {
	int alpha;
	int beta = 1 + // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			2;
}
// === end ===

// === case: collapse_block_lambda_on_one_line ===
class InputFieldConsolidationViolationCollapseBlockLambdaOnOneLine {
	Runnable alpha;
	Runnable beta = // violation: Fields 'beta' and 'alpha' (type 'Runnable') must be declared on one line.
			() -> { System.gc(); };
}
// === end ===

// === case: collapse_both_declarations ===
class InputFieldConsolidationViolationCollapseBothDeclarations {
	int alpha =
			1;
	int beta = // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			2;
}
// === end ===

// === case: collapse_c_style_brackets_first_declaration ===
class InputFieldConsolidationViolationCollapseCStyleBracketsFirstDeclaration {
	int alpha[] =
			{1};
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: collapse_chained_call ===
class InputFieldConsolidationViolationCollapseChainedCall {
	String alpha;
	String beta = "60" // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
			.trim();
}
// === end ===

// === case: collapse_empty_lambda_block ===
class InputFieldConsolidationViolationCollapseEmptyLambdaBlock {
	Runnable alpha;
	Runnable beta = () -> { // violation: Fields 'beta' and 'alpha' (type 'Runnable') must be declared on one line.
	};
}
// === end ===

// === case: collapse_first_declaration_sorted_after_later ===
class InputFieldConsolidationViolationCollapseFirstDeclarationSortedAfterLater {
	int zeta =
			1;
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: collapse_later_declaration_sorted_first ===
class InputFieldConsolidationViolationCollapseLaterDeclarationSortedFirst {
	int zeta;
	int alpha = // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
			1;
}
// === end ===

// === case: collapse_split_after_equals_first_declaration ===
class InputFieldConsolidationViolationCollapseSplitAfterEqualsFirstDeclaration {
	int alpha =
			12;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: collapse_split_after_equals_later_declaration ===
class InputFieldConsolidationViolationCollapseSplitAfterEqualsLaterDeclaration {
	int alpha;
	int beta = // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			12;
}
// === end ===

// === case: collapse_supplementary_char_name_split_after_equals ===
class InputFieldConsolidationViolationCollapseSupplementaryCharNameSplitAfterEquals {
	int alpha;
	int b𝑥 = // violation: Fields 'b𝑥' and 'alpha' (type 'int') must be declared on one line.
			5;
}
// === end ===

// === case: collapse_switch_on_one_line ===
class InputFieldConsolidationViolationCollapseSwitchOnOneLine {
	int a;
	int b = // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
			switch (a) { default -> 1; };
}
// === end ===

// === case: collapse_ternary ===
class InputFieldConsolidationViolationCollapseTernary {
	int alpha;
	int beta = 1 > 0 // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			? 1
			: 2;
}
// === end ===

// === case: comma_merge_through_annotation_array_arg ===
class InputFieldConsolidationViolationNamedArrayParam {
	@ArrayParam({1, 2})
	int alpha;
	@ArrayParam({1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: comma_merge_through_annotation_named_params ===
class InputFieldConsolidationViolationNamedParams {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(a = 1, b = 2)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: comma_merge_through_explicit_value_array ===
class InputFieldConsolidationViolationExplicitValueArray {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: comma_merge_through_paren_annotation_array_value ===
class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha;
	@SuppressWarnings({"unused", "all"})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: comment_inside_rebuilt_first_initializer_sorted_after_initialized_stays_in_place ===
class InputFieldConsolidationViolationCommentInsideRebuiltFirstInitializerSortedAfterInitializedStaysInPlace {
	int zeta = /* note */ 1;
	int alpha = 2; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: comment_line_inside_later_declaration_moves ===
class InputFieldConsolidationViolationCommentLineInsideLaterDeclarationMoves {
	@Deprecated
	int alpha;
	@Deprecated
	// why
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: compound_array_match ===
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha;
	String[] beta[]; // violation: Fields 'beta' and 'alpha' (type 'String[][]') must be declared on one line.
}
// === end ===

// === case: constant_between_blank_lines_merges_past ===
class InputFieldConsolidationViolationConstantBetweenBlankLinesMergesPast {
	static int alpha;

	static final int LIMIT = 5;

	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: constant_between_blank_static_finals_merges_past ===
class InputFieldConsolidationViolationConstantBetweenBlankStaticFinalsMergesPast {
	static final int ALPHA;
	static final int LIMIT = 5;
	static final int GAMMA; // violation: Fields 'GAMMA' and 'ALPHA' (type 'int') must be declared on one line.

	static {
		ALPHA = 1;
		GAMMA = 2;
	}
}
// === end ===

// === case: constant_between_statics_merges_past ===
class InputFieldConsolidationViolationConstantBetweenStaticsMergesPast {
	static int alpha;
	static final int LIMIT = 5;
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: constant_interleaved_with_run_stays_apart ===
class InputFieldConsolidationViolationConstantInterleavedWithRunStaysApart {
	static int alpha;
	static final int A = 1;
	static int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	static final int B = 2;
}
// === end ===

// === case: constant_reading_earlier_run_member_merges_past ===
class InputFieldConsolidationViolationConstantReadingEarlierRunMemberMergesPast {
	static int alpha = 1;
	static final int LIMIT = alpha + 1;
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: constant_running_code_inert_neighbour_merges_past ===
class InputFieldConsolidationViolationConstantRunningCodeInertNeighbourMergesPast {
	static int alpha = 1;
	static final int LIMIT = Integer.parseInt("5");
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: constant_split_between_statics_merges_past ===
class InputFieldConsolidationViolationConstantSplitBetweenStaticsMergesPast {
	static int alpha;
	static final int LIMIT =
			5;
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: constant_with_trailing_directive_between_merges_past ===
class InputFieldConsolidationViolationConstantWithTrailingDirectiveBetweenMergesPast {
	static int alpha;
	static final int LIMIT = 5; // NOSONAR
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: continuation_at_declaration_indent_merges ===
class InputFieldConsolidationViolationContinuationAtDeclarationIndentMerges {
	int alpha;
	int beta, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	gamma;
}
// === end ===

// === case: continuation_comment_between_names_no_semicolon_merges ===
class InputFieldConsolidationViolationContinuationCommentBetweenNamesNoSemicolonMerges {
	int alpha;
	int beta, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma, /* x */ delta,
			epsilon;
}
// === end ===

// === case: continuation_inside_carried_block_comment_bails ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationContinuationInsideCarriedBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			beta, /* note
			more text
			*/ gamma;
}
// === end ===

// === case: continuation_line_comment_after_trailing_comma_merges ===
class InputFieldConsolidationViolationContinuationLineCommentAfterTrailingCommaMerges {
	int alpha;
	int beta, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma, // x
			delta;
}
// === end ===

// === case: continuation_line_comment_before_last_name ===
class InputFieldConsolidationViolationContinuationLineCommentBeforeLastName {
	int a;
	int b, // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
			c, /* x */ d;
}
// === end ===

// === case: continuation_line_comment_before_semicolon ===
class InputFieldConsolidationViolationContinuationLineCommentBeforeSemicolon {
	int a;
	int b, // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
			c /* x */;
}
// === end ===

// === case: continuation_space_indent_one_past_declaration_merges ===
class InputFieldConsolidationViolationContinuationSpaceIndentOnePastDeclarationMerges {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
     beta;
}
// === end ===

// === case: continuation_space_indent_ties_declaration_merges ===
class InputFieldConsolidationViolationContinuationSpaceIndentTiesDeclarationMerges {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
    beta;
}
// === end ===

// === case: continuation_stops_at_block_comment ===
class InputFieldConsolidationViolationContinuationStopsAtBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			/* single-line block comment */
			beta;
}
// === end ===

// === case: continuation_stops_at_collected_then_comment ===
class InputFieldConsolidationViolationContinuationStopsAtCollectedThenComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			beta,
			// comment about gamma
			gamma;
}
// === end ===

// === case: continuation_stops_at_comment ===
class InputFieldConsolidationViolationContinuationStopsAtComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			// comment about beta
			beta;
}
// === end ===

// === case: continuation_stops_at_javadoc ===
class InputFieldConsolidationViolationContinuationStopsAtJavadoc {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			/** Javadoc for beta */
			beta;
}
// === end ===

// === case: continuation_stops_at_multi_line_block_comment ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationContinuationStopsAtMultiLineBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			/*
			 * multi-line comment
			 */
			beta;
}
// === end ===

// === case: dependency_ahead_of_run_start_merges ===
class InputFieldConsolidationViolationDependencyAheadOfRunStartMerges {
	String label = "v";
	int base;
	int max = label.length(); // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: dependency_crossing_starts_new_run ===
class InputFieldConsolidationViolationDependencyCrossingStartsNewRun {
	int alpha = 1;
	String label = "v" + alpha;
	int beta = label.length();
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: dependency_on_merged_member_between_merges ===
class InputFieldConsolidationViolationDependencyOnMergedMemberBetweenMerges {
	int alpha;
	int beta = 1; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	String label;
	int gamma = beta; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_ahead_of_first_type_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveAheadOfFirstTypeSkips {
	/* NOSONAR */ int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_ahead_of_rebuilt_wrapped_first_declaration_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveAheadOfRebuiltWrappedFirstDeclarationSkips {
	/* NOSONAR */ int zeta,
			beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_ahead_of_split_first_declaration_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveAheadOfSplitFirstDeclarationSkips {
	/* NOSONAR */ int alpha
			;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_ahead_of_wrapped_first_declaration_in_order_merges ===
class InputFieldConsolidationViolationDirectiveAheadOfWrappedFirstDeclarationInOrderMerges {
	/* NOSONAR */ int alpha,
			beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_among_first_declarators_in_order_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveAmongFirstDeclaratorsInOrderSkips {
	int alpha, /* NOSONAR */ beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_among_later_declarators_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveAmongLaterDeclaratorsSkips {
	int alpha;
	int beta, /* NOSONAR */ gamma; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_before_later_semicolon_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveBeforeLaterSemicolonSkips {
	int alpha;
	int beta /* NOSONAR */; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_block_ahead_of_type_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveBlockAheadOfTypeSkips {
	int alpha;
	/* NOSONAR */ int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_block_ending_on_join_line_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveBlockEndingOnJoinLineSkips {
	int alpha, /* NOSONAR
			*/ beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_checkstyle_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveCheckstyleSkips {
	int alpha;
	int beta; // CHECKSTYLE IGNORE MagicNumber // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_formatter_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationDirectiveFormatterSkips {
	int alpha;
	int beta; // @formatter:off // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_inside_first_initializer_in_order_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideFirstInitializerInOrderSkips {
	int alpha = /* NOSONAR */ 1;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_inside_later_initializer_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideLaterInitializerSkips {
	int alpha;
	int beta = /* NOSONAR */ 1; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_inside_rebuilt_first_initializer_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideRebuiltFirstInitializerSkips {
	int zeta = /* NOSONAR */ 1;
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_inside_rebuilt_first_initializer_sorted_after_initialized_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideRebuiltFirstInitializerSortedAfterInitializedSkips {
	int zeta = /* NOSONAR */ 1;
	int alpha = 2; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_leading_an_earlier_field_merges ===
class InputFieldConsolidationViolationDirectiveLeadingAnEarlierFieldMerges {
	//noinspection unused
	boolean label;
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_led_declaration_left_between_merges ===
class InputFieldConsolidationViolationDirectiveLedDeclarationLeftBetweenMerges {
	int alpha;
	String label; //noinspection unused
	int beta;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_led_declaration_skipped_later_ones_merge ===
class InputFieldConsolidationViolationDirectiveLedDeclarationSkippedLaterOnesMerge {
	//noinspection unused
	int alpha;
	int beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_middle_of_three_skips_whole_run ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveMiddleOfThreeSkipsWholeRun {
	int alpha;
	int beta; // NOSONAR // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_formatter_without_colon_merges ===
class InputFieldConsolidationViolationDirectiveNearMissFormatterWithoutColonMerges {
	int alpha;
	int beta; // @formatter off // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_language_capitalised_above_base_merges ===
class InputFieldConsolidationViolationDirectiveNearMissLanguageCapitalisedAboveBaseMerges {
	// Language=SQL
	private String query = "select 1";
	private String label = "x"; // violation: Fields 'label' and 'query' (type 'String') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_lowercase_checkstyle_merges ===
class InputFieldConsolidationViolationDirectiveNearMissLowercaseCheckstyleMerges {
	int alpha;
	int beta; // checkstyle:off // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_lowercase_nosonar_merges ===
class InputFieldConsolidationViolationDirectiveNearMissLowercaseNosonarMerges {
	int alpha;
	int beta; // nosonar // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_noinspection_capitalised_merges ===
class InputFieldConsolidationViolationDirectiveNearMissNoinspectionCapitalisedMerges {
	int alpha;
	int beta; // NoInspection // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_non_nls_without_dollar_merges ===
class InputFieldConsolidationViolationDirectiveNearMissNonNlsWithoutDollarMerges {
	int alpha;
	int beta; // NON-NLS-1 // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_nopm_merges ===
class InputFieldConsolidationViolationDirectiveNearMissNopmMerges {
	int alpha;
	int beta; // NOPM // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_nosona_merges ===
class InputFieldConsolidationViolationDirectiveNearMissNosonaMerges {
	int alpha;
	int beta; // NOSONA // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_plain_comment_between_annotation_and_type_merges ===
class InputFieldConsolidationViolationDirectiveNearMissPlainCommentBetweenAnnotationAndTypeMerges {
	@Deprecated
	// plain
	private String alpha;
	@Deprecated
	private String beta; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: directive_near_miss_spotless_without_colon_merges ===
class InputFieldConsolidationViolationDirectiveNearMissSpotlessWithoutColonMerges {
	int alpha;
	int beta; // spotless off // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_noinspection_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveNoinspectionSkips {
	int alpha;
	int beta; //noinspection unused // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_non_nls_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveNonNlsSkips {
	String alpha;
	String beta = "b"; //$NON-NLS-1$ // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: directive_nopmd_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveNopmdSkips {
	int alpha;
	int beta; // NOPMD // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_nosonar_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveNosonarSkips {
	int alpha;
	int beta; // NOSONAR // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_on_annotation_line_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveOnAnnotationLineSkips {
	@Deprecated
	int alpha;
	@Deprecated // NOSONAR
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_on_field_outside_run_merges ===
class InputFieldConsolidationViolationDirectiveOnFieldOutsideRunMerges {
	int alpha;
	String label; // NOSONAR
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_on_first_annotation_line_kept ===
class InputFieldConsolidationViolationDirectiveOnFirstAnnotationLineKept {
	@Deprecated // NOSONAR
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_on_rebuilt_first_declaration_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveOnRebuiltFirstDeclarationSkips {
	int zeta /* NOSONAR */;
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_on_wrapped_continuation_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveOnWrappedContinuationSkips {
	int alpha;
	int beta, // NOSONAR // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma;
}
// === end ===

// === case: directive_repeating_kept_trailing_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveRepeatingKeptTrailingSkips {
	int alpha; // NOSONAR
	int beta; // NOSONAR // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_spotless_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationDirectiveSpotlessSkips {
	int alpha;
	int beta; // spotless:off // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_statement_scoped_near_miss_capitalised_merges ===
class InputFieldConsolidationViolationDirectiveStatementScopedNearMissCapitalisedMerges {
	// NoInspection unused
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_text_in_string_merges ===
class InputFieldConsolidationViolationDirectiveTextInStringMerges {
	String alpha;
	String beta = "// NOSONAR"; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: directive_trailing_first_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveTrailingFirstSkips {
	int alpha; // NOSONAR
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_trailing_line_above_not_statement_merges ===
class InputFieldConsolidationViolationDirectiveTrailingLineAboveNotStatementMerges {
	boolean label; // NOSONAR
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: directive_with_plain_comment_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveWithPlainCommentSkips {
	int alpha;
	int beta; // why // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma; // NOSONAR // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: enum_constant_body_fields ===
enum InputFieldConsolidationViolationEnumConstantBody {
	A {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: enum_trailing_comma_before_fields ===
enum InputFieldConsolidationViolationEnumTrailingComma {
	A,
	B,;

	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: enum_with_fields ===
enum InputFieldConsolidationViolationEnum {
	A,
	B;

	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: final_fields ===
class InputFieldConsolidationViolationFinal {
	final int alpha;
	final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}
// === end ===

// === case: final_split_two_runs_merge ===
class InputFieldConsolidationViolationFinalSplitTwoRunsMerge {
	final int alpha = 1;
	final int beta;
	final int gamma = 2; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
	final int delta; // violation: Fields 'delta' and 'beta' (type 'int') must be declared on one line.

	InputFieldConsolidationViolationFinalSplitTwoRunsMerge() {
		beta = 0;
		delta = 0;
	}
}
// === end ===

// === case: fqn_annotation ===
class InputFieldConsolidationViolationFqnAnnotation {
	@java.lang.Deprecated
	int alpha;
	@java.lang.Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: fqn_type ===
// imports: java.util.List
class InputFieldConsolidationViolationFqnType {
	java.util.List<String> alpha;
	java.util.List<String> beta; // violation: Fields 'beta' and 'alpha' (type 'java.util.List<String>') must be declared on one line.
}
// === end ===

// === case: fqn_type_no_generics ===
class InputFieldConsolidationViolationFqnTypeNoGenerics {
	java.lang.Object alpha;
	java.lang.Object beta; // violation: Fields 'beta' and 'alpha' (type 'java.lang.Object') must be declared on one line.
}
// === end ===

// === case: generic_type ===
// imports: java.util.List
class InputFieldConsolidationViolationGeneric {
	List<String> names;
	List<String> words; // violation: Fields 'words' and 'names' (type 'List<String>') must be declared on one line.
}
// === end ===

// === case: generic_with_array_type_arg ===
// imports: java.util.List
class InputFieldConsolidationViolationGenericArrayTypeArg {
	List<String[]> alpha;
	List<String[]> beta; // violation: Fields 'beta' and 'alpha' (type 'List<String[]>') must be declared on one line.
}
// === end ===

// === case: hoisted_block_comment_joins ===
class InputFieldConsolidationViolationHoistedBlockCommentJoins {
	int alpha;
	int beta; /* why beta */ // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: hoisted_comment_holding_block_close_joins ===
class InputFieldConsolidationViolationHoistedCommentHoldingBlockCloseJoins {
	int alpha;
	int beta; // ends */ here // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: hoisted_comments_join_into_one_line ===
class InputFieldConsolidationViolationHoistedCommentsJoinIntoOneLine {
	int alpha;
	int beta; // why beta // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma; /* why gamma */ // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: identical_moved_comments_join_once ===
class InputFieldConsolidationViolationIdenticalMovedCommentsJoinOnce {
	int alpha;
	int beta; // px // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma; // px // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: identical_trailing_and_moved_comment_kept_once ===
class InputFieldConsolidationViolationIdenticalTrailingAndMovedCommentKeptOnce {
	int alpha; // px
	int beta; // px // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_c_style_prev ===
class InputFieldConsolidationViolationInitCStylePrev {
	int alpha[] = {1}, beta[];
	int[] gamma; // violation: Fields 'gamma' and 'beta' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: init_continuation_before_trailing_comma ===
class InputFieldConsolidationViolationInitContinuationBeforeTrailingComma {
	int alpha;
	int beta, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma = 10,
			delta;
}
// === end ===

// === case: init_continuation_line_opens_with_equals ===
class InputFieldConsolidationViolationInitContinuationLineOpensWithEquals {
	int alpha;
	int beta, gamma // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			= alpha;
}
// === end ===

// === case: init_continuation_name_with_initializer ===
class InputFieldConsolidationViolationInitContinuationNameWithInitializer {
	int alpha;
	int beta, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma = 10;
}
// === end ===

// === case: init_continuation_without_name ===
class InputFieldConsolidationViolationInitContinuationWithoutName {
	int alpha;
	int beta, gamma // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			= 10;
}
// === end ===

// === case: init_f_f ===
class InputFieldConsolidationViolationInitFF {
	int first = 1;
	int second = 2; // violation: Fields 'second' and 'first' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_f_u ===
class InputFieldConsolidationViolationInitFU {
	int active = 1;
	int passive; // violation: Fields 'passive' and 'active' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_forward_read_reader_inside_run_keeps_read_field_out ===
class InputFieldConsolidationViolationInitForwardReadReaderInsideRunKeepsReadFieldOut {
	private int alpha;
	private int total = this.count * 2; // violation: Fields 'total' and 'alpha' (type 'int') must be declared on one line.
	private int count = 3;
}
// === end ===

// === case: init_forward_read_run_starting_after_reader_merges ===
class InputFieldConsolidationViolationInitForwardReadRunStartingAfterReaderMerges {
	private long total = this.count * 2L;
	private int base;
	private int count = 3; // violation: Fields 'count' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_on_violation_line ===
class InputFieldConsolidationViolationInitOnViolationLine {
	int alpha;
	int beta, gamma = 10; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_on_violation_line_without_semicolon ===
class InputFieldConsolidationViolationInitOnViolationLineWithoutSemicolon {
	int alpha;
	int beta, gamma = 10, // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			delta;
}
// === end ===

// === case: init_prev_first ===
class InputFieldConsolidationViolationInitPrevFirst {
	int alpha = 1, beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_prev_last ===
class InputFieldConsolidationViolationInitPrevLast {
	int alpha, beta = 1;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_prev_middle ===
class InputFieldConsolidationViolationInitPrevMiddle {
	int alpha, beta = 1, gamma;
	int delta; // violation: Fields 'delta' and 'gamma' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_qualified_read_of_other_object_merges ===
class InputFieldConsolidationViolationInitQualifiedReadOfOtherObjectMerges {
	private static InputFieldConsolidationViolationInitQualifiedReadOfOtherObjectMerges other;
	private int total = other.count * 2;
	private int count = 3; // violation: Fields 'count' and 'total' (type 'int') must be declared on one line.
}
// === end ===

// === case: init_u_f ===
class InputFieldConsolidationViolationInitUF {
	int alpha;
	int beta = 1; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: inner_class ===
class InputFieldConsolidationViolationInnerClass {
	static class Inner {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: later_doc_comment_after_annotation_moves ===
class InputFieldConsolidationViolationLaterDocCommentAfterAnnotationMoves {
	@Deprecated
	int alpha;
	@Deprecated /** d */ int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: later_reader_of_moved_declaration_merges ===
class InputFieldConsolidationViolationLaterReaderOfMovedDeclarationMerges {
	int alpha;
	String label;
	int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	long copy = beta;
}
// === end ===

// === case: length_boundary_array_initializer ===
class InputFieldConsolidationViolationLengthBoundaryArrayInitializer {
	int[] alpha;
	int[] abc = {1, 2, 3, 4, 5, 6, 7, 8, 9}; // violation: Fields 'abc' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: length_boundary_c_style_brackets_not_counted ===
class InputFieldConsolidationViolationLengthBoundaryCStyleBracketsNotCounted {
	int[] alpha;
	int n12345678901234567890123456[] = {1}; // violation: Fields 'n12345678901234567890123456' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: length_boundary_cast ===
class InputFieldConsolidationViolationLengthBoundaryCast {
	int alpha;
	int n123456789012 = (int) 123456789.0; // violation: Fields 'n123456789012' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_char_escape ===
class InputFieldConsolidationViolationLengthBoundaryCharEscape {
	char alpha;
	char n12345678901234567890123456 = 'a'; // violation: Fields 'n12345678901234567890123456' and 'alpha' (type 'char') must be declared on one line.
}
// === end ===

// === case: length_boundary_comment_counted ===
class InputFieldConsolidationViolationLengthBoundaryCommentCounted {
	int alpha;
	int n1234567890123456 = 1 /* c */ + 2; // violation: Fields 'n1234567890123456' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_diamond ===
// imports: java.util.ArrayList
// imports: java.util.List
class InputFieldConsolidationViolationLengthBoundaryDiamond {
	List<String> first;
	List<String> n123456789012 = new ArrayList<>(); // violation: Fields 'n123456789012' and 'first' (type 'List<String>') must be declared on one line.
}
// === end ===

// === case: length_boundary_first_declaration ===
class InputFieldConsolidationViolationLengthBoundaryFirstDeclaration {
	String s = "123456789012345678901234567";
	String t; // violation: Fields 't' and 's' (type 'String') must be declared on one line.
}
// === end ===

// === case: length_boundary_hex_literal ===
class InputFieldConsolidationViolationLengthBoundaryHexLiteral {
	int alpha;
	int n1234567890123456789 = 0xFF_FF_FF; // violation: Fields 'n1234567890123456789' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_lambda ===
class InputFieldConsolidationViolationLengthBoundaryLambda {
	Runnable first;
	Runnable r = () -> System.out.println("h"); // violation: Fields 'r' and 'first' (type 'Runnable') must be declared on one line.
}
// === end ===

// === case: length_boundary_leading_comment_counted ===
class InputFieldConsolidationViolationLengthBoundaryLeadingCommentCounted {
	int alpha;
	int n12345678901234567890 = /* c */ 1; // violation: Fields 'n12345678901234567890' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_long_name ===
class InputFieldConsolidationViolationLengthBoundaryLongName {
	int alpha;
	int n1234567890123456789012345678 = 1; // violation: Fields 'n1234567890123456789012345678' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_method_call ===
class InputFieldConsolidationViolationLengthBoundaryMethodCall {
	String first;
	String s = String.valueOf(123456789012L); // violation: Fields 's' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: length_boundary_method_reference ===
class InputFieldConsolidationViolationLengthBoundaryMethodReference {
	Runnable first;
	Runnable printerRefs = System.out::println; // violation: Fields 'printerRefs' and 'first' (type 'Runnable') must be declared on one line.
}
// === end ===

// === case: length_boundary_new_with_arguments ===
class InputFieldConsolidationViolationLengthBoundaryNewWithArguments {
	Object first;
	Object o = new StringBuilder("12345678"); // violation: Fields 'o' and 'first' (type 'Object') must be declared on one line.
}
// === end ===

// === case: length_boundary_no_initializer_long_name ===
class InputFieldConsolidationViolationLengthBoundaryNoInitializerLongName {
	int alpha;
	int n123456789012345678901234567890; // violation: Fields 'n123456789012345678901234567890' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_numeric_suffix ===
class InputFieldConsolidationViolationLengthBoundaryNumericSuffix {
	long alpha;
	long n123456789012345 = 9_999_999_999L; // violation: Fields 'n123456789012345' and 'alpha' (type 'long') must be declared on one line.
}
// === end ===

// === case: length_boundary_one_char_name ===
class InputFieldConsolidationViolationLengthBoundaryOneCharName {
	String first;
	String s = "123456789012345678901234567"; // violation: Fields 's' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: length_boundary_qualified_constant ===
class InputFieldConsolidationViolationLengthBoundaryQualifiedConstant {
	int alpha;
	int n1234567890 = Thread.MAX_PRIORITY; // violation: Fields 'n1234567890' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_single_line_anonymous_class ===
class InputFieldConsolidationViolationLengthBoundarySingleLineAnonymousClass {
	final Object first = new Object() {};
	final Object n12345678901234 = new Object() {}; // violation: Fields 'n12345678901234' and 'first' (type 'Object') must be declared on one line.
}
// === end ===

// === case: length_boundary_split_value_joined ===
class InputFieldConsolidationViolationLengthBoundarySplitValueJoined {
	String first;
	String s = "123456789012345678901" // violation: Fields 's' and 'first' (type 'String') must be declared on one line.
			+ "x";
}
// === end ===

// === case: length_boundary_split_value_tight_join ===
class InputFieldConsolidationViolationLengthBoundarySplitValueTightJoin {
	String first;
	String b = "12345678901234567890" // violation: Fields 'b' and 'first' (type 'String') must be declared on one line.
			.trim();
}
// === end ===

// === case: length_boundary_string_escapes ===
class InputFieldConsolidationViolationLengthBoundaryStringEscapes {
	String first;
	String s = "\n\n\n\n\n\n\n\n\n\n\n\n\nx"; // violation: Fields 's' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: length_boundary_supplementary_code_points ===
class InputFieldConsolidationViolationLengthBoundarySupplementaryCodePoints {
	String first;
	String s = "𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥𝑥"; // violation: Fields 's' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: length_boundary_ternary ===
class InputFieldConsolidationViolationLengthBoundaryTernary {
	int alpha;
	int n12 = 1 > 0 ? 1_000_000 : 999_999; // violation: Fields 'n12' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_type_witness ===
// imports: java.util.List
class InputFieldConsolidationViolationLengthBoundaryTypeWitness {
	List<Integer> first;
	List<Integer> n1234567 = List.<Integer>of(1, 2); // violation: Fields 'n1234567' and 'first' (type 'List<Integer>') must be declared on one line.
}
// === end ===

// === case: length_boundary_underscores ===
class InputFieldConsolidationViolationLengthBoundaryUnderscores {
	int alpha;
	int n12345678901234 = 100_000 + 1_000; // violation: Fields 'n12345678901234' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: length_boundary_whitespace_around_equals ===
class InputFieldConsolidationViolationLengthBoundaryWhitespaceAroundEquals {
	int alpha;
	int n1234567890123456789012345678   =   1; // violation: Fields 'n1234567890123456789012345678' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_one_line_merges ===
class InputFieldConsolidationViolationLineCountDirectiveOneLineMerges {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	int alpha = 1;
	int beta = 2;
	int gamma = 3; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_window_ending_at_first_of_run_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveWindowEndingAtFirstOfRunSkips {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES
	int alpha = 1;
	int beta = 2;
	int gamma = 3; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_window_from_below_misses_run_merges ===
class InputFieldConsolidationViolationLineCountDirectiveWindowFromBelowMissesRunMerges {
	int alpha = 1;
	int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	long other;
	String note; // CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
}
// === end ===

// === case: line_count_directive_window_from_below_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveWindowFromBelowSkips {
	int alpha = 1;
	int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	long other; // CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
}
// === end ===

// === case: line_count_directive_window_on_semicolon_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveWindowOnSemicolonLineSkips {
	int alpha = 1;
	int beta = // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			2;
	long other; // CHECKSTYLE IGNORE MagicNumber FOR -1 LINES
}
// === end ===

// === case: line_count_directive_window_over_run_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveWindowOverRunSkips {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 3 LINES
	int alpha = 1;
	int beta = 2;
	int gamma = 3; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_window_reaching_last_of_run_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveWindowReachingLastOfRunSkips {
	int beta = 2;
	long other; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES
	int gamma = 3; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_zero_count_above_run_merges ===
class InputFieldConsolidationViolationLineCountDirectiveZeroCountAboveRunMerges {
	// CHECKSTYLE IGNORE MagicNumber FOR NEXT 0 LINES
	long other;
	int alpha = 1;
	int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: line_count_directive_zero_count_on_run_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationLineCountDirectiveZeroCountOnRunLineSkips {
	int alpha = 1;
	int beta = 2; // CHECKSTYLE IGNORE MagicNumber FOR NEXT 0 LINES // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: local_class_fields_merge ===
class InputFieldConsolidationViolationLocalClassFieldsMerge {
	void m() {
		class Local {
			int alpha;
			int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
		}

		new Local();
	}
}
// === end ===

// === case: long_initializer_anonymous_still_binds_readers ===
class InputFieldConsolidationViolationLongInitializerAnonymousStillBindsReaders {
	final Object holder = new Object() {
		int counter;
	};
	final Object r1 = holder;
	final Object r2 = holder; // violation: Fields 'r2' and 'r1' (type 'Object') must be declared on one line.
}
// === end ===

// === case: long_initializer_between_blank_lines ===
class InputFieldConsolidationViolationLongInitializerBetweenBlankLines {
	int alpha;

	int beta = 1_000_000 + 2_000_000 + 3_000_000;

	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_between_pairs_merges_into_one ===
class InputFieldConsolidationViolationLongInitializerBetweenPairsMergesIntoOne {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma = 1_000_000 + 2_000_000 + 3_000_000;
	int delta; // violation: Fields 'delta' and 'beta' (type 'int') must be declared on one line.
	int epsilon; // violation: Fields 'epsilon' and 'delta' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_between_shorts_merges_past ===
class InputFieldConsolidationViolationLongInitializerBetweenShortsMergesPast {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_block_lambda_between ===
class InputFieldConsolidationViolationLongInitializerBlockLambdaBetween {
	Runnable alpha;
	Runnable beta = () -> {
		System.gc();
	};
	Runnable gamma; // violation: Fields 'gamma' and 'alpha' (type 'Runnable') must be declared on one line.
}
// === end ===

// === case: long_initializer_every_name_of_declaration_between ===
class InputFieldConsolidationViolationLongInitializerEveryNameOfDeclarationBetween {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000, gamma = 1_000_000 + 2_000_000 + 3_000_000;
	int delta; // violation: Fields 'delta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_final_value_between ===
class InputFieldConsolidationViolationLongInitializerFinalValueBetween {
	final int alpha = 1;
	final int beta = 1_000_000 + 2_000_000 + 3_000_000;
	final int gamma = 2; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_final_without_value_long_name ===
class InputFieldConsolidationViolationLongInitializerFinalWithoutValueLongName {
	final int alpha;
	final int n12345678901234567890123456789012345678; // violation: Fields 'n12345678901234567890123456789012345678' and 'alpha' (type 'int') must be declared on one line.

	InputFieldConsolidationViolationLongInitializerFinalWithoutValueLongName() {
		alpha = 1;
		n12345678901234567890123456789012345678 = 2;
	}
}
// === end ===

// === case: long_initializer_first_name_of_declaration_between ===
class InputFieldConsolidationViolationLongInitializerFirstNameOfDeclarationBetween {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000, gamma;
	int delta; // violation: Fields 'delta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_first_of_stretch ===
class InputFieldConsolidationViolationLongInitializerFirstOfStretch {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000;
	int beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_in_anonymous_class_body ===
class InputFieldConsolidationViolationLongInitializerInAnonymousClassBody {
	final Object holder = new Object() {
		int alpha;
		int beta = 1_000_000 + 2_000_000 + 3_000_000;
		int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
	};
}
// === end ===

// === case: long_initializer_in_enum_constant_body ===
enum InputFieldConsolidationViolationLongInitializerInEnumConstantBody {
	ONE {
		int alpha;
		int beta = 1_000_000 + 2_000_000 + 3_000_000;
		int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: long_initializer_in_local_class ===
class InputFieldConsolidationViolationLongInitializerInLocalClass {
	void method() {
		class Local {
			int alpha;
			int beta = 1_000_000 + 2_000_000 + 3_000_000;
			int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
		}
	}
}
// === end ===

// === case: long_initializer_in_record ===
record InputFieldConsolidationViolationLongInitializerInRecord(int x) {
	static int alpha;
	static int beta = 1_000_000 + 2_000_000 + 3_000_000;
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_interleaved_runs_fold ===
class InputFieldConsolidationViolationLongInitializerInterleavedRunsFold {
	int alpha;
	String first;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
	String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: long_initializer_last_name_of_declaration_between ===
class InputFieldConsolidationViolationLongInitializerLastNameOfDeclarationBetween {
	int alpha;
	int beta, gamma = 1_000_000 + 2_000_000 + 3_000_000;
	int delta; // violation: Fields 'delta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_last_of_stretch ===
class InputFieldConsolidationViolationLongInitializerLastOfStretch {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma = 1_000_000 + 2_000_000 + 3_000_000;
}
// === end ===

// === case: long_initializer_middle_name_of_declaration_between ===
class InputFieldConsolidationViolationLongInitializerMiddleNameOfDeclarationBetween {
	int alpha;
	int beta, gamma = 1_000_000 + 2_000_000 + 3_000_000, epsilon;
	int delta; // violation: Fields 'delta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_on_annotated_fields ===
class InputFieldConsolidationViolationLongInitializerOnAnnotatedFields {
	@Deprecated
	int alpha;
	@Deprecated
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	@Deprecated
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_reads_run_start ===
class InputFieldConsolidationViolationLongInitializerReadsRunStart {
	int alpha = 1;
	int beta = alpha + 1_000_000 + 2_000_000;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_runs_code_between_inert ===
class InputFieldConsolidationViolationLongInitializerRunsCodeBetweenInert {
	private int base;
	private int mid = Integer.parseInt("1234567890");
	private int max; // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_sorting_suppressed_between_pairs ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationLongInitializerSortingSuppressedBetweenPairs {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma = 1_000_000 + 2_000_000 + 3_000_000;
	int delta;
	int epsilon; // violation: Fields 'epsilon' and 'delta' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_sorting_suppressed_first ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationLongInitializerSortingSuppressedFirst {
	int alpha = 1_000_000 + 2_000_000 + 3_000_000;
	int beta;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_static_between ===
class InputFieldConsolidationViolationLongInitializerStaticBetween {
	static int alpha;
	static int beta = 1_000_000 + 2_000_000 + 3_000_000;
	static int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_switch_between ===
class InputFieldConsolidationViolationLongInitializerSwitchBetween {
	int alpha;
	int beta = switch (alpha) {
		default -> 1;
	};
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_text_block_between ===
class InputFieldConsolidationViolationLongInitializerTextBlockBetween {
	String alpha;
	String beta = """
			text
			""";
	String gamma; // violation: Fields 'gamma' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: long_initializer_tool_directive_stays ===
class InputFieldConsolidationViolationLongInitializerToolDirectiveStays {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000; // NOSONAR
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_trailing_comment_stays ===
class InputFieldConsolidationViolationLongInitializerTrailingCommentStays {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000; // keep
	int gamma; // why // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_two_between ===
class InputFieldConsolidationViolationLongInitializerTwoBetween {
	int alpha;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	int gamma = 1_000_000 + 2_000_000 + 3_000_000;
	int delta; // violation: Fields 'delta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_initializer_wrapped_with_comment_between ===
class InputFieldConsolidationViolationLongInitializerWrappedWithCommentBetween {
	int alpha;
	int beta = 1 // c
			+ 2;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: long_primitive ===
class InputFieldConsolidationViolationLong {
	long elapsed;
	long remaining; // violation: Fields 'remaining' and 'elapsed' (type 'long') must be declared on one line.
}
// === end ===

// === case: main ===
class InputFieldConsolidationViolationThree {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') must be declared on one line.
}

class InputFieldConsolidationViolationOtherPrimitives {
	byte alphaByte;
	byte betaByte; // violation: Fields 'betaByte' and 'alphaByte' (type 'byte') must be declared on one line.
	char alphaChar;
	char betaChar; // violation: Fields 'betaChar' and 'alphaChar' (type 'char') must be declared on one line.
	double alphaDouble;
	double betaDouble; // violation: Fields 'betaDouble' and 'alphaDouble' (type 'double') must be declared on one line.
	float alphaFloat;
	float betaFloat; // violation: Fields 'betaFloat' and 'alphaFloat' (type 'float') must be declared on one line.
	short alphaShort;
	short betaShort; // violation: Fields 'betaShort' and 'alphaShort' (type 'short') must be declared on one line.
}

class InputFieldConsolidationViolationNamedArrayParamExplicit {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}

class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(b = 2, a = 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: merge_above_mixed_array_declaration ===
class InputFieldConsolidationViolationMergeAboveMixedArray {
	int alpha;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
	int zebra, beta[];
}
// === end ===

// === case: modifier_order_variant_match ===
class InputFieldConsolidationViolationModifierOrderVariantMatch {
	private static int alpha;
	static private int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: moved_comment_from_other_field_closing_on_later_line_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationMovedCommentFromOtherFieldClosingOnLaterLineSkips {
	int alpha;
	long zulu; /* c
	*/ int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: moved_comment_repeated_inside_later_initializer_kept_once ===
class InputFieldConsolidationViolationMovedCommentRepeatedInsideLaterInitializerKeptOnce {
	int alpha;
	int beta; /* x */ // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	int gamma = /* x */ 1; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: multiline_generic_prev_field_wraps ===
// imports: java.util.Map
class InputFieldConsolidationViolationMultilineGenericPrevWraps {
	Map<String,
			Integer> alpha;
	Map<String, Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'Map<String,Integer>') must be declared on one line.
}
// === end ===

// === case: multiline_generic_violation_field_wraps ===
// imports: java.util.Map
class InputFieldConsolidationViolationMultilineGenericViolationWraps {
	Map<String, Integer> alpha;
	Map<String,
			Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'Map<String,Integer>') must be declared on one line.
}
// === end ===

// === case: named_nested_annotation ===
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(value = @Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: nested_annotation ===
class InputFieldConsolidationViolationNestedAnnotation {
	@Container(@Inner)
	int alpha;
	@Container(@Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: nested_generic ===
// imports: java.util.List
// imports: java.util.Map
class InputFieldConsolidationViolationNestedGeneric {
	Map<String, List<Integer>> alphaMap;
	Map<String, List<Integer>> betaMap; // violation: Fields 'betaMap' and 'alphaMap' (type 'Map<String,List<Integer>>') must be declared on one line.
}
// === end ===

// === case: nested_qualified_annotation_match ===
class InputFieldConsolidationViolationNestedQualifiedAnnotationMatch {
	@Container(@first.Inner)
	int alpha;
	@Container(@first.Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_adjacent_across_other_type_merges ===
class InputFieldConsolidationViolationNonAdjacentAcrossOtherTypeMerges {
	int alpha;
	String label;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_adjacent_across_other_visibility_merges ===
class InputFieldConsolidationViolationNonAdjacentAcrossOtherVisibilityMerges {
	private int alpha;
	public int beta;
	private int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_adjacent_across_volatile_merges ===
class InputFieldConsolidationViolationNonAdjacentAcrossVolatileMerges {
	int alpha;
	volatile int beta;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_constant_anonymous_body_in_interface_fields_merge ===
interface InputFieldConsolidationViolationNonConstantAnonymousBodyInInterfaceFieldsMerge {
	Object HOLDER = new Object() {
		final int alpha = 1;
		final int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	};
}
// === end ===

// === case: non_constant_blank_static_final_multi_name_merges ===
class InputFieldConsolidationViolationNonConstantBlankStaticFinalMultiNameMerges {
	static final int beta, delta;
	static final int gamma; // violation: Fields 'gamma' and 'delta' (type 'int') must be declared on one line.

	static {
		beta = 1;
		delta = 2;
		gamma = 3;
	}
}
// === end ===

// === case: non_constant_class_fields_like_interface_constants_merge ===
class InputFieldConsolidationViolationNonConstantClassFieldsLikeInterfaceConstantsMerge {
	int ALPHA = 1;
	int BETA = 2; // violation: Fields 'BETA' and 'ALPHA' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_constant_class_nested_in_interface_fields_merge ===
interface InputFieldConsolidationViolationNonConstantClassNestedInInterfaceFieldsMerge {
	class Impl {
		int alpha = 1;
		int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: non_constant_enum_instance_finals_merge ===
enum InputFieldConsolidationViolationNonConstantEnumInstanceFinalsMerge {
	ONE;

	final int alpha = 1;
	final int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_constant_record_static_with_values_merge ===
record InputFieldConsolidationViolationNonConstantRecordStaticWithValuesMerge(int x) {
	static int alpha = 1;
	static int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_constant_static_final_without_value_merges ===
class InputFieldConsolidationViolationNonConstantStaticFinalWithoutValueMerges {
	static final int alpha;
	static final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.

	static {
		alpha = 1;
		beta = 2;
	}
}
// === end ===

// === case: non_constant_static_objects_merge ===
class InputFieldConsolidationViolationNonConstantStaticObjectsMerge {
	static Object lock = new Object();
	static Object other = new Object(); // violation: Fields 'other' and 'lock' (type 'Object') must be declared on one line.
}
// === end ===

// === case: non_constant_static_transient_with_values_merge ===
class InputFieldConsolidationViolationNonConstantStaticTransientWithValuesMerge {
	private static transient int alpha = 1;
	private static transient int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: non_constant_static_with_values_merge ===
class InputFieldConsolidationViolationNonConstantStaticWithValuesMerge {
	static int alpha = 1;
	static int beta = 2; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: parameterized_qualifier_match ===
class InputFieldConsolidationViolationParameterizedQualifierMatch {
	L<String>.Inner<Integer> alpha;
	L<String>.Inner<Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'L<String>.Inner<Integer>') must be declared on one line.
}
// === end ===

// === case: parameterized_qualifier_nested_match ===
class InputFieldConsolidationViolationParameterizedQualifierNestedMatch {
	L<String>.Inner<Integer>.Leaf alpha;
	L<String>.Inner<Integer>.Leaf beta; // violation: Fields 'beta' and 'alpha' (type 'L<String>.Inner<Integer>.Leaf') must be declared on one line.
}
// === end ===

// === case: paramless_annotation ===
class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: paramless_variant ===
class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha;
	@Deprecated()
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_cycle_with_dependency_keeps_source_order ===
class InputFieldConsolidationViolationPinCycleWithDependencyKeepsSourceOrder {
	private int total = size();
	private int size = 3; // violation: Fields 'size' and 'total' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_inert_past_runs_merges ===
class InputFieldConsolidationViolationPinInertPastRunsMerges {
	private int base;
	private String report = String.join("..", "a", "b");
	private int max; // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_inert_runs_sorts ===
class InputFieldConsolidationViolationPinInertRunsSorts {
	private int zeta;
	private int alpha = Integer.parseInt("1"); // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_literal_between_merges ===
class InputFieldConsolidationViolationPinLiteralBetweenMerges {
	private int base = 1;
	private String label = "none";
	private int max = 3; // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_literal_runs_merges_in_source_order ===
class InputFieldConsolidationViolationPinLiteralRunsMergesInSourceOrder {
	private int zeta = 2;
	private int alpha = Integer.parseInt("1"); // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_other_group_runs_between_merges ===
class InputFieldConsolidationViolationPinOtherGroupRunsBetweenMerges {
	private int base = 1;
	private static int counter = Integer.parseInt("1");
	private int max = 2; // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_post_increment_merges_in_source_order ===
class InputFieldConsolidationViolationPinPostIncrementMergesInSourceOrder {
	private static int next;
	static int typeText = next++;
	static int typeImage = next++; // violation: Fields 'typeImage' and 'typeText' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_prefix_increment_merges_in_source_order ===
class InputFieldConsolidationViolationPinPrefixIncrementMergesInSourceOrder {
	private static int issued;

	private final int number = ++issued;
	private final int checksum = issued * 31; // violation: Fields 'checksum' and 'number' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_runs_ahead_of_run_start_merges ===
class InputFieldConsolidationViolationPinRunsAheadOfRunStartMerges {
	private String report = String.join("..", "a");
	private int base = 1;
	private int max = 2; // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_runs_literal_merges_in_source_order ===
class InputFieldConsolidationViolationPinRunsLiteralMergesInSourceOrder {
	private int zeta = Integer.parseInt("1");
	private int alpha = 2; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_runs_own_member_then_literal_between_merges ===
class InputFieldConsolidationViolationPinRunsOwnMemberThenLiteralBetweenMerges {
	private int zeta = Integer.parseInt("1");
	private int mid = Integer.parseInt("2"); // violation: Fields 'mid' and 'zeta' (type 'int') must be declared on one line.
	private String label = "x";
	private int alpha = Integer.parseInt("3");
}
// === end ===

// === case: pin_runs_past_inert_merges_in_source_order ===
class InputFieldConsolidationViolationPinRunsPastInertMergesInSourceOrder {
	private int base = Integer.parseInt("1");
	private String label;
	private int max = Integer.parseInt("2"); // violation: Fields 'max' and 'base' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_runs_runs_merges_in_source_order ===
class InputFieldConsolidationViolationPinRunsRunsMergesInSourceOrder {
	private int zeta = Integer.parseInt("1");
	private int alpha = Integer.parseInt("2"); // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_runs_three_merges_in_source_order ===
class InputFieldConsolidationViolationPinRunsThreeMergesInSourceOrder {
	private int zeta = Integer.parseInt("1");
	private int mid = Integer.parseInt("2"); // violation: Fields 'mid' and 'zeta' (type 'int') must be declared on one line.
	private int alpha = Integer.parseInt("3"); // violation: Fields 'alpha' and 'mid' (type 'int') must be declared on one line.
}
// === end ===

// === case: pin_sort_partial_pins_merges ===
class InputFieldConsolidationViolationPinSortPartialPinsMerges {
	private int zeta = Integer.parseInt("1");
	private int mid; // violation: Fields 'mid' and 'zeta' (type 'int') must be declared on one line.
	private int alpha = 2; // violation: Fields 'alpha' and 'mid' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_c_style_comment_among_declarators_merges ===
class InputFieldConsolidationViolationPrevCStyleCommentAmongDeclaratorsMerges {
	int alpha[], /* legacy */ beta[];
	int[] gamma; // violation: Fields 'gamma' and 'beta' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: prev_c_style_comment_before_semicolon_merges ===
class InputFieldConsolidationViolationPrevCStyleCommentBeforeSemicolonMerges {
	int alpha[] /* legacy */;
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: prev_c_style_comment_before_type_merges ===
class InputFieldConsolidationViolationPrevCStyleCommentBeforeTypeMerges {
	/* legacy */ int alpha[];
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: prev_c_style_line_closes_carried_block_comment_merges ===
class InputFieldConsolidationViolationPrevCStyleLineClosesCarriedBlockCommentMerges {
	/* legacy
	 */ int alpha[];
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: prev_comment_holding_brackets_merges ===
class InputFieldConsolidationViolationPrevCommentHoldingBracketsMerges {
	int alpha /* [] */;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_doc_comment_after_first_token_merges ===
class InputFieldConsolidationViolationPrevDocCommentAfterFirstTokenMerges {
	private /** d */ int alpha;
	private int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_doc_comment_before_semicolon_merges ===
class InputFieldConsolidationViolationPrevDocCommentBeforeSemicolonMerges {
	private int alpha /** d */;
	private int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_empty_block_comment_line_above_merges ===
class InputFieldConsolidationViolationPrevEmptyBlockCommentLineAboveMerges {
	/**/
	private int alpha;
	private int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_empty_block_comment_merges ===
class InputFieldConsolidationViolationPrevEmptyBlockCommentMerges {
	/**/ int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_field_type_on_earlier_line ===
class InputFieldConsolidationViolationPrevFieldTypeOnEarlierLine {
	int
			alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: prev_follows_documented_field_line_merges ===
class InputFieldConsolidationViolationPrevFollowsDocumentedFieldLineMerges {
	/** A. */ private int alpha;
	private int beta;
	private int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_leading_block_comment_merges ===
class InputFieldConsolidationViolationPrevLeadingBlockCommentMerges {
	/* in pixels */
	private int height;
	private int width; // violation: Fields 'width' and 'height' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_leading_line_comment_merges ===
class InputFieldConsolidationViolationPrevLeadingLineCommentMerges {
	// in pixels
	private int height;
	private int width; // violation: Fields 'width' and 'height' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_adjacent_block_comments ===
class InputFieldConsolidationViolationPrevLineAdjacentBlockComments {
	int /* ; *//* x */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_annotation_with_semicolon_in_string ===
class InputFieldConsolidationViolationPrevLineAnnotationSemicolonInString {
	@SuppressWarnings("a;b") int alpha;
	@SuppressWarnings("a;b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_closes_and_merges ===
class InputFieldConsolidationViolationPrevLineBlockCommentCloses {
	int /* multi
	line */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_spanning_multiple_fields ===
class InputFieldConsolidationViolationPrevLineBlockCommentSpanning {
	int /* comment */ alpha;
	int /* comment */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineBlockCommentWithSemicolon {
	int /* ; */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_escaped_backslash ===
class InputFieldConsolidationViolationPrevLineCharEscapedBackslash {
	@Ann('\\') int alpha;
	@Ann('\\') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_escaped_quote ===
class InputFieldConsolidationViolationPrevLineCharEscapedQuote {
	@Ann('\'') int alpha;
	@Ann('\'') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_semicolon ===
class InputFieldConsolidationViolationPrevLineCharSemicolon {
	@Ann(';') int alpha;
	@Ann(';') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_trailing_comment_not_ending_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentNoSemicolon {
	int alpha; // field comment
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_line_trailing_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentWithSemicolon {
	int alpha; // see init();
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_same_line_block_comment_merges ===
class InputFieldConsolidationViolationPrevSameLineBlockCommentMerges {
	private int height; /* in pixels */
	private int width; // violation: Fields 'width' and 'height' (type 'int') must be declared on one line.
}
// === end ===

// === case: prev_split_semicolon_comment_merges ===
class InputFieldConsolidationViolationPrevSplitSemicolonCommentMerges {
	String first // note
			;
	String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: prev_split_semicolon_comment_multi_name_merges ===
class InputFieldConsolidationViolationPrevSplitSemicolonCommentMultiNameMerges {
	int alpha, beta // note
			;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: primitives_two_int ===
class InputFieldConsolidationViolationPrimitives {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: protected_fields ===
class InputFieldConsolidationViolationProtected {
	protected int alpha;
	protected int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: qualified_type_annotation_match ===
class InputFieldConsolidationViolationQualifiedTypeAnnotationMatch {
	Outer.@ViolationTypeAnn Inner alpha;
	Outer.@ViolationTypeAnn Inner beta; // violation: Fields 'beta' and 'alpha' (type 'Outer.@ViolationTypeAnn Inner') must be declared on one line.
}
// === end ===

// === case: qualified_type_argument_annotation_match ===
// imports: java.util.List
class InputFieldConsolidationViolationQualifiedTypeArgumentAnnotationMatch {
	List<Outer.@ViolationTypeAnn Inner> alpha;
	List<Outer.@ViolationTypeAnn Inner> beta; // violation: Fields 'beta' and 'alpha' (type 'List<Outer.@ViolationTypeAnn Inner>') must be declared on one line.
}
// === end ===

// === case: range_directive_formatter_trailing_non_run_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationRangeDirectiveFormatterTrailingNonRunLineSkips {
	String header; // @formatter:off
	int alpha = 7;
	long footer; // @formatter:on
	int beta = 42; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: range_directive_off_trailing_non_run_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationRangeDirectiveOffTrailingNonRunLineSkips {
	String header; // CHECKSTYLE:OFF
	int alpha = 7;
	long footer; // CHECKSTYLE:ON
	int beta = 42; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: range_directive_on_alone_trailing_non_run_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationRangeDirectiveOnAloneTrailingNonRunLineSkips {
	int alpha = 7;
	long footer; // CHECKSTYLE:ON
	int beta = 42; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: range_directive_past_run_merges ===
class InputFieldConsolidationViolationRangeDirectivePastRunMerges {
	int alpha = 7;
	int beta = 42; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	long footer; // CHECKSTYLE:OFF
}
// === end ===

// === case: range_directive_region_trailing_non_run_line_skips ===
// skip-reason: cannot consolidate: a comment around the fields switches a tool off or on for the lines after it or counts lines, and the merge would move names into or out of its reach
class InputFieldConsolidationViolationRangeDirectiveRegionTrailingNonRunLineSkips {
	String header; //region Tuning
	int alpha = 7;
	long footer; //endregion
	int beta = 42; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: record_static_fields ===
record InputFieldConsolidationViolationRecord(int x) {
	static String first;
	static String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: references_two_string ===
class InputFieldConsolidationViolationReferences {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: reformat_first_break_after_name ===
class InputFieldConsolidationViolationReformatFirstBreakAfterName {
	int alpha
			= 5;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_break_after_type ===
class InputFieldConsolidationViolationReformatFirstBreakAfterType {
	int
			alpha = 5;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_break_before_semicolon ===
class InputFieldConsolidationViolationReformatFirstBreakBeforeSemicolon {
	int alpha = 5
			;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_break_everywhere ===
class InputFieldConsolidationViolationReformatFirstBreakEverywhere {
	int
			alpha
			=
			5
			;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_break_inside_value ===
class InputFieldConsolidationViolationReformatFirstBreakInsideValue {
	int alpha = 1
			+ 2;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_no_space_around_equals ===
class InputFieldConsolidationViolationReformatFirstNoSpaceAroundEquals {
	int alpha=5;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_no_value_break_before_semicolon ===
class InputFieldConsolidationViolationReformatFirstNoValueBreakBeforeSemicolon {
	int alpha
			;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_space_before_semicolon ===
class InputFieldConsolidationViolationReformatFirstSpaceBeforeSemicolon {
	int alpha = 5 ;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_spaces_around_equals ===
class InputFieldConsolidationViolationReformatFirstSpacesAroundEquals {
	int alpha  =  5;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_first_tabs_around_equals ===
class InputFieldConsolidationViolationReformatFirstTabsAroundEquals {
	int alpha	=	5;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_later_break_after_name ===
class InputFieldConsolidationViolationReformatLaterBreakAfterName {
	int alpha;
	int beta // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			= 5;
}
// === end ===

// === case: reformat_later_break_after_type ===
class InputFieldConsolidationViolationReformatLaterBreakAfterType {
	int alpha;
	int
			beta = 5; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_later_break_before_semicolon ===
class InputFieldConsolidationViolationReformatLaterBreakBeforeSemicolon {
	int alpha;
	int beta = 5 // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			;
}
// === end ===

// === case: reformat_later_break_everywhere ===
class InputFieldConsolidationViolationReformatLaterBreakEverywhere {
	int alpha;
	int
			beta // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			=
			5
			;
}
// === end ===

// === case: reformat_later_no_space_around_equals ===
class InputFieldConsolidationViolationReformatLaterNoSpaceAroundEquals {
	int alpha;
	int beta=5; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_later_no_value_break_before_semicolon ===
class InputFieldConsolidationViolationReformatLaterNoValueBreakBeforeSemicolon {
	int alpha;
	int beta // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			;
}
// === end ===

// === case: reformat_later_space_before_semicolon ===
class InputFieldConsolidationViolationReformatLaterSpaceBeforeSemicolon {
	int alpha;
	int beta = 5 ; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_later_tabs_around_equals ===
class InputFieldConsolidationViolationReformatLaterTabsAroundEquals {
	int alpha;
	int beta	=	5; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_multi_name_first_break_after_comma ===
class InputFieldConsolidationViolationReformatMultiNameFirstBreakAfterComma {
	int alpha = 1,
			beta = 2;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_multi_name_first_break_before_comma ===
class InputFieldConsolidationViolationReformatMultiNameFirstBreakBeforeComma {
	int alpha = 1
			, beta = 2;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_multi_name_first_later_name_split ===
class InputFieldConsolidationViolationReformatMultiNameFirstLaterNameSplit {
	int alpha, beta =
			2;
	int gamma; // violation: Fields 'gamma' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_multi_name_first_middle_break_before_comma ===
class InputFieldConsolidationViolationReformatMultiNameFirstMiddleBreakBeforeComma {
	int alpha, beta
			, gamma;
	int delta; // violation: Fields 'delta' and 'gamma' (type 'int') must be declared on one line.
}
// === end ===

// === case: reformat_multi_name_later_break_before_comma ===
class InputFieldConsolidationViolationReformatMultiNameLaterBreakBeforeComma {
	int alpha;
	int beta = 1 // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			, gamma = 2;
}
// === end ===

// === case: repeated_annotation_same_order_match ===
class InputFieldConsolidationViolationRepeatedAnnotationSameOrderMatch {
	@Tag("x")
	@Tag("y")
	int alpha;
	@Tag("x")
	@Tag("y")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: repeated_annotation_with_distinct_swapped_match ===
class InputFieldConsolidationViolationRepeatedAnnotationWithDistinctSwappedMatch {
	@Deprecated
	@Tag("x")
	@Tag("y")
	int alpha;
	@Tag("x")
	@Deprecated
	@Tag("y")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: same_annotation_params ===
class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: shorthand_vs_explicit_expr ===
class InputFieldConsolidationViolationShorthandVsExplicitExpr {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings(value = "unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: shorthand_vs_explicit_nested ===
class InputFieldConsolidationViolationShorthandVsExplicitNested {
	@Container(@Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: simple_primitive_fields ===
class InputFieldConsolidationViolationSimplePrimitive {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: simple_reference_fields ===
class InputFieldConsolidationViolationSimpleReference {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: sort_and_c_style_hoist_merges ===
class InputFieldConsolidationViolationSortAndCStyleHoistMerges {
	int zeta[];
	int[] alpha; // violation: Fields 'alpha' and 'zeta' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: sort_anonymous_class_initializers ===
class InputFieldConsolidationViolationSortAnonymousClassInitializers {
	final Object zeta = new Object() {
	};
	final Object alpha = new Object() { // violation: Fields 'alpha' and 'zeta' (type 'Object') must be declared on one line.
	};
}
// === end ===

// === case: sort_cycle_keeps_source_order ===
class InputFieldConsolidationViolationSortCycleKeepsSourceOrder {
	int zeta = alpha;
	int alpha = zeta; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sort_first_declaration_comment_between_names_moves ===
class InputFieldConsolidationViolationSortFirstDeclarationCommentBetweenNamesMoves {
	int zeta, // why zeta
			beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sort_first_trailing_comment_stays ===
class InputFieldConsolidationViolationSortFirstTrailingCommentStays {
	int zeta; // why zeta
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sort_respects_dependency ===
class InputFieldConsolidationViolationSortRespectsDependency {
	int zeta = 1;
	int mid; // violation: Fields 'mid' and 'zeta' (type 'int') must be declared on one line.
	int alpha = zeta; // violation: Fields 'alpha' and 'mid' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_not_suppressed_annotation_on_field ===
class InputFieldConsolidationViolationSortingNotSuppressedAnnotationOnField {
	@SuppressWarnings("FieldSorting")
	int beta;
	@SuppressWarnings("FieldSorting")
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_not_suppressed_anonymous_body ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingNotSuppressedAnonymousBody {
	final Object holder = new Object() {
		int beta;
		int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
	};
}
// === end ===

// === case: sorting_not_suppressed_lowercase_value ===
@SuppressWarnings("fieldsorting")
class InputFieldConsolidationViolationSortingNotSuppressedLowercaseValue {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_not_suppressed_other_value ===
@SuppressWarnings("unused")
class InputFieldConsolidationViolationSortingNotSuppressedOtherValue {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_not_suppressed_prefixed_value ===
@SuppressWarnings("checkstyle:FieldSorting")
class InputFieldConsolidationViolationSortingNotSuppressedPrefixedValue {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_adjacent_unsorted_keeps_source_order ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedAdjacentUnsortedKeepsSourceOrder {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_array_value_first ===
@SuppressWarnings({"FieldSorting", "x"})
class InputFieldConsolidationViolationSortingSuppressedArrayValueFirst {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_array_value_second ===
@SuppressWarnings({"unused", "FieldSorting"})
class InputFieldConsolidationViolationSortingSuppressedArrayValueSecond {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_blank_line_between_adjacent ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedBlankLineBetweenAdjacent {
	int beta;

	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_c_style_rebuilt_in_source_order ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedCStyleRebuiltInSourceOrder {
	int beta[];
	int alpha[]; // violation: Fields 'alpha' and 'beta' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_chain_broken_by_other_type ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedChainBrokenByOtherType {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
	String s;
	int c;
	int d; // violation: Fields 'd' and 'c' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_collapse_source_order ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedCollapseSourceOrder {
	int zeta =
			1;
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_enclosing_only_nested_sorted ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedEnclosingOnlyNestedSorted {
	class Inner {
		int beta;
		String label;
		int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: sorting_suppressed_enum_constant_body ===
enum InputFieldConsolidationViolationSortingSuppressedEnumConstantBody {
	@SuppressWarnings("FieldSorting")
	INSTANCE {
		int beta;
		int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: sorting_suppressed_enum_fields ===
@SuppressWarnings("FieldSorting")
enum InputFieldConsolidationViolationSortingSuppressedEnumFields {
	ALPHA;

	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_explicit_value ===
@SuppressWarnings(value = "FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedExplicitValue {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_explicit_value_array ===
@SuppressWarnings(value = {"FieldSorting"})
class InputFieldConsolidationViolationSortingSuppressedExplicitValueArray {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_first_trailing_comment_stays ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedFirstTrailingCommentStays {
	int beta; // b
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_local_class ===
class InputFieldConsolidationViolationSortingSuppressedLocalClass {
	void m() {
		@SuppressWarnings("FieldSorting")
		class Local {
			int beta;
			int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
		}

		new Local();
	}
}
// === end ===

// === case: sorting_suppressed_moved_comment_hoists ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedMovedCommentHoists {
	int beta;
	int alpha; // px // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_multi_declarator_previous ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedMultiDeclaratorPrevious {
	int beta, zeta;
	int alpha; // violation: Fields 'alpha' and 'zeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_nested_only_outer_sorted ===
class InputFieldConsolidationViolationSortingSuppressedNestedOnlyOuterSorted {
	int beta;
	String label;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.

	@SuppressWarnings("FieldSorting")
	class Inner {
		int beta;
		int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: sorting_suppressed_qualified_annotation ===
@java.lang.SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedQualifiedAnnotation {
	int beta;
	int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_record_static_fields ===
@SuppressWarnings("FieldSorting")
record InputFieldConsolidationViolationSortingSuppressedRecordStaticFields(int x) {
	static int beta;
	static int alpha; // violation: Fields 'alpha' and 'beta' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_three_adjacent_chain ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedThreeAdjacentChain {
	int c;
	int a; // violation: Fields 'a' and 'c' (type 'int') must be declared on one line.
	int b; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
}
// === end ===

// === case: sorting_suppressed_wrap_source_order ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedWrapSourceOrder {
	int zetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZeta;
	int alphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlpha; // violation: Fields 'alphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlphaAlpha' and 'zetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZetaZeta' (type 'int') must be declared on one line.
}
// === end ===

// === case: stacked_dimension_annotation_same_order_match ===
class InputFieldConsolidationViolationStackedDimensionAnnotationSameOrderMatch {
	String @ViolationTypeAnn @Inner [] alpha;
	String @ViolationTypeAnn @Inner [] beta; // violation: Fields 'beta' and 'alpha' (type 'String @ViolationTypeAnn @Inner []') must be declared on one line.
}
// === end ===

// === case: static_fields ===
class InputFieldConsolidationViolationStatic {
	static int global;
	static int shared; // violation: Fields 'shared' and 'global' (type 'int') must be declared on one line.
}
// === end ===

// === case: supplementary_char_ahead_of_rebuilt_first_declaration_merges ===
class InputFieldConsolidationViolationSupplementaryCharAheadOfRebuiltFirstDeclarationMerges {
	/* 𝑥 */ String zeta;
	String alpha; // violation: Fields 'alpha' and 'zeta' (type 'String') must be declared on one line.
}
// === end ===

// === case: supplementary_char_first_declaration_kept_as_written_merges ===
class InputFieldConsolidationViolationSupplementaryCharFirstDeclarationKeptAsWrittenMerges {
	String alpha = "𝑥"; /* 𝑥 */
	String beta; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: supplementary_identifier_merges ===
class InputFieldConsolidationViolationSupplementaryIdentifierMerges {
	int alpha;
	int b𝑥; // violation: Fields 'b𝑥' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: tab_separated_multi_var_on_violation_line ===
class InputFieldConsolidationViolationTabSeparatedMultiVar {
	int a;
	int b,	c; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
}
// === end ===

// === case: three_fields_bottom_up_first_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpFirstPass {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') must be declared on one line.
}
// === end ===

// === case: three_fields_bottom_up_second_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpSecondPass {
	int a;
	int b, c; // violation: Fields 'b' and 'a' (type 'int') must be declared on one line.
}
// === end ===

// === case: two_interleaved_runs_merge ===
class InputFieldConsolidationViolationTwoInterleavedRunsMerge {
	int alpha;
	String first;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
	String second; // violation: Fields 'second' and 'first' (type 'String') must be declared on one line.
}
// === end ===

// === case: type_argument_annotation_match ===
// imports: java.util.List
class InputFieldConsolidationViolationTypeArgumentAnnotationMatch {
	List<@ViolationTypeAnn String> alpha;
	List<@ViolationTypeAnn String> beta; // violation: Fields 'beta' and 'alpha' (type 'List<@ViolationTypeAnn String>') must be declared on one line.
}
// === end ===

// === case: type_depth_just_under_cap ===
class InputFieldConsolidationViolationTypeDepthJustUnderCap {
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> alpha;
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> beta; // violation: Fields 'beta' and 'alpha' (type 'L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>') must be declared on one line.
}
// === end ===

// === case: type_use_annotation_at_depth_cap_match ===
// imports: java.util.List
class InputFieldConsolidationViolationTypeUseAnnotationAtDepthCapMatch {
	List<@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W))))))))))))))))))))))))))))))))))))))))))))))))) String> alpha;
	List<@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W(@W))))))))))))))))))))))))))))))))))))))))))))))))) String> beta; // violation: Fields 'beta' and 'alpha' (type 'List<@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W(value=@W))))))))))))))))))))))))))))))))))))))))))))))))) String>') must be declared on one line.
}
// === end ===

// === case: unbounded_wildcard ===
// imports: java.util.List
class InputFieldConsolidationViolationUnboundedWildcard {
	List<?> alphaUnbounded;
	List<?> betaUnbounded; // violation: Fields 'betaUnbounded' and 'alphaUnbounded' (type 'List<?>') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_after_field_name_proceeds ===
class InputFieldConsolidationViolationBlockCommentAfterFieldName {
	int alpha;
	int beta; /* note */ // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_carry_inner ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationBlockCommentCarryInner {
	int alpha; /* open
	comment */ int beta /* doc */; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_char_literal_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideCharLiteral {
	@Ann('/') int alpha;
	@Ann('/') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_escaped_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideEscapedString {
	@SuppressWarnings("a\"/*b") int alpha;
	@SuppressWarnings("a\"/*b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideString {
	@SuppressWarnings("a/*b") int alpha;
	@SuppressWarnings("a/*b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_post_name ===
class InputFieldConsolidationViolationBlockCommentPostName {
	int alpha;
	int beta /* doc */; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_with_semicolon ===
class InputFieldConsolidationViolationBlockCommentWithSemicolon {
	int alpha;
	int /* ; */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_comment_above_continuation_merges ===
class InputFieldConsolidationViolationLineCommentAboveContinuationMerges {
	int alpha;
	int beta, // note // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			gamma;
}
// === end ===

// === case: violation_line_starts_inside_block_comment ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationStartsInsideBlockComment {
	int alpha; /* open
	comment */ int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: violation_line_trailing_block_comment_unclosed_bails ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationTrailingBlockCommentUnclosed {
	int alpha;
	int beta; /* note // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
			continued */
}
// === end ===

// === case: wildcard_extends ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcard {
	List<? extends Number> alphaList;
	List<? extends Number> betaList; // violation: Fields 'betaList' and 'alphaList' (type 'List<? extends Number>') must be declared on one line.
}
// === end ===

// === case: wildcard_super ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcardSuper {
	List<? super Integer> alphaSuper;
	List<? super Integer> betaSuper; // violation: Fields 'betaSuper' and 'alphaSuper' (type 'List<? super Integer>') must be declared on one line.
}
// === end ===

// === case: with_annotations_on_own_line ===
class InputFieldConsolidationViolationWithAnnotationsOnOwnLine {
	@NonNull
	protected Button nextButton;
	@NonNull
	protected Button presetsButton; // violation: Fields 'presetsButton' and 'nextButton' (type 'Button') must be declared on one line.
}
// === end ===

// === case: with_escaped_quote_in_annotation_string ===
class InputFieldConsolidationViolationWithEscapedQuoteInAnn {
	@SuppressWarnings("a\"b") int alpha;
	@SuppressWarnings("a\"b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: with_multiple_annotations ===
class InputFieldConsolidationViolationWithMultipleAnnotations {
	@CheckResult
	@NonNull
	String alpha;
	@CheckResult
	@NonNull
	String beta; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: with_violation_comment ===
class InputFieldConsolidationViolationWithViolationComment {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_boundary121_wraps ===
class InputFieldConsolidationViolationWrapBoundary121 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_boundary_exactly120_no_wrap ===
class InputFieldConsolidationViolationWrapBoundary120 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_c_style_arrays ===
class InputFieldConsolidationViolationWrapCStyleArrays {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa[];
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb[]; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int[]') must be declared on one line.
}
// === end ===

// === case: wrap_continuation_comment_preserved ===
class InputFieldConsolidationViolationWrapContinuationCommentPreserved {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') must be declared on one line.
			beta; // important
}
// === end ===

// === case: wrap_continuation_from_previous_wrap ===
class InputFieldConsolidationViolationWrapContinuationFromPreviousWrap {
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, // violation: Fields 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' and 'prevName' (type 'int') must be declared on one line.
			cccccccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: wrap_continuation_multiple_lines ===
class InputFieldConsolidationViolationWrapContinuationMultipleLines {
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, // violation: Fields 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' and 'prevName' (type 'int') must be declared on one line.
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			cccccccccccccccccccccccccccccc,
			dddddddddddddddddddddddddddddd;
}
// === end ===

// === case: wrap_deep_indent ===
class InputFieldConsolidationViolationWrapDeepIndent {
	static class Outer {
		int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
		int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
	}
}
// === end ===

// === case: wrap_each_name_own_line ===
class InputFieldConsolidationViolationWrapEachNameOwnLine {
	static class Outer {
		static class Inner {
			int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
			int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
		}
	}
}
// === end ===

// === case: wrap_four_fields_two_per_line ===
class InputFieldConsolidationViolationWrapFourFieldsTwoPerLine {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccc, ddddddddddddddddddddddddddddddddddd; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_collapsed_core_exactly_120 ===
class InputFieldConsolidationViolationWrapShortInitializersCollapsedCoreExactly120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05 = // violation: Fields 'v05' and 'v04' (type 'int') must be declared on one line.
			1_000_000_005;
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_collapsed_core_past_120 ===
class InputFieldConsolidationViolationWrapShortInitializersCollapsedCorePast120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05x = // violation: Fields 'v05x' and 'v04' (type 'int') must be declared on one line.
			1_000_000_005;
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05x' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_continuation_exactly_120 ===
class InputFieldConsolidationViolationWrapShortInitializersContinuationExactly120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05 = 1_000_000_005; // violation: Fields 'v05' and 'v04' (type 'int') must be declared on one line.
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05' (type 'int') must be declared on one line.
	private int v07 = 1_000_000_007; // violation: Fields 'v07' and 'v06' (type 'int') must be declared on one line.
	private int v08 = 1_000_000_008; // violation: Fields 'v08' and 'v07' (type 'int') must be declared on one line.
	private int v09 = 1_000_000_009; // violation: Fields 'v09' and 'v08' (type 'int') must be declared on one line.
	private int v10 = 1_000_000_010 + 1; // violation: Fields 'v10' and 'v09' (type 'int') must be declared on one line.
	private int v11 = 1_000_000_011; // violation: Fields 'v11' and 'v10' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_continuation_past_120 ===
class InputFieldConsolidationViolationWrapShortInitializersContinuationPast120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05 = 1_000_000_005; // violation: Fields 'v05' and 'v04' (type 'int') must be declared on one line.
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05' (type 'int') must be declared on one line.
	private int v07 = 1_000_000_007; // violation: Fields 'v07' and 'v06' (type 'int') must be declared on one line.
	private int v08 = 1_000_000_008; // violation: Fields 'v08' and 'v07' (type 'int') must be declared on one line.
	private int v09 = 1_000_000_009; // violation: Fields 'v09' and 'v08' (type 'int') must be declared on one line.
	private int v10 = 1_000_000_010 + 10; // violation: Fields 'v10' and 'v09' (type 'int') must be declared on one line.
	private int v11 = 1_000_000_011; // violation: Fields 'v11' and 'v10' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_first_line_exactly_120 ===
class InputFieldConsolidationViolationWrapShortInitializersFirstLineExactly120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05 = 1_000_000_005; // violation: Fields 'v05' and 'v04' (type 'int') must be declared on one line.
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05' (type 'int') must be declared on one line.
	private int v07 = 1_000_000_007; // violation: Fields 'v07' and 'v06' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_first_line_past_120 ===
class InputFieldConsolidationViolationWrapShortInitializersFirstLinePast120 {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05x = 1_000_000_005; // violation: Fields 'v05x' and 'v04' (type 'int') must be declared on one line.
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05x' (type 'int') must be declared on one line.
	private int v07 = 1_000_000_007; // violation: Fields 'v07' and 'v06' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_short_initializers_long_between ===
class InputFieldConsolidationViolationWrapShortInitializersLongBetween {
	private int v01 = 1_000_000_001;
	private int v02 = 1_000_000_002; // violation: Fields 'v02' and 'v01' (type 'int') must be declared on one line.
	private int v03 = 1_000_000_003; // violation: Fields 'v03' and 'v02' (type 'int') must be declared on one line.
	private int zz = 1_000_000 + 2_000_000 + 3_000_000;
	private int v04 = 1_000_000_004; // violation: Fields 'v04' and 'v03' (type 'int') must be declared on one line.
	private int v05 = 1_000_000_005; // violation: Fields 'v05' and 'v04' (type 'int') must be declared on one line.
	private int v06 = 1_000_000_006; // violation: Fields 'v06' and 'v05' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_three_fields_one_plus_two ===
class InputFieldConsolidationViolationWrapThreeFieldsOnePlusTwo {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, cccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') must be declared on one line.
}
// === end ===

// === case: wrap_three_fields_two_plus_one ===
class InputFieldConsolidationViolationWrapThreeFieldsTwoPlusOne {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') must be declared on one line.
}
// === end ===

// === case: wrap_with_modifiers ===
class InputFieldConsolidationViolationWrapWithModifiers {
	private static int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	private static int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrap_with_trailing_comment ===
class InputFieldConsolidationViolationWrapWithTrailingComment {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa; // see init();
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrapped_annotation_array_closes_on_violation_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationArrayClosesOnViolationLineMerges {
	@SuppressWarnings({"a", "b"}) int alpha;
	@SuppressWarnings({
			"a",
			"b"}) int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrapped_annotation_closes_on_stacked_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationClosesOnStackedLineMerges {
	@Outer({@Element(1), @Element(2), @Element(3)}) int alpha;
	@Outer({@Element(1),
			@Element(2),
			@Element(3)})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') must be declared on one line.
}
// === end ===

// === case: wrapped_annotation_closes_on_violation_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationClosesOnViolationLineMerges {
	@Size(min = 1, max = 10, message = "m") String alpha;
	@Size(min = 1,
			max = 10,
			message = "m") String beta; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===

// === case: wrapped_annotation_two_lines_merges ===
class InputFieldConsolidationViolationWrappedAnnotationTwoLinesMerges {
	@Size(min = 1, max = 10) String alpha;
	@Size(min = 1,
			max = 10) String beta; // violation: Fields 'beta' and 'alpha' (type 'String') must be declared on one line.
}
// === end ===