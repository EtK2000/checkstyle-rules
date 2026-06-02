package com.etk2000.checkstyle.inputs.fieldconsolidation;

class L<T> {
}

@interface Multi {
	int a() default 0;

	int b() default 0;
}

// === case: after_multi_var ===
class InputFieldConsolidationViolationAfterMultiVar {
	int a, b, c;
}
// === end ===

// === case: annotated_lower_bound_match ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedLowerBoundMatch {
	List<? super @ViolationTypeAnn Number> alpha, beta;
}
// === end ===

// === case: annotated_upper_bound ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedBound {
	List<? extends @ViolationTypeAnn Number> alpha, beta;
}
// === end ===

// === case: annotation_line_trailing_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationAnnotationLineTrailingComment {
	@Deprecated
	int alpha;
	@Deprecated // kept for the legacy wire format
	int beta;
}
// === end ===

// === case: annotation_order_swapped ===
class InputFieldConsolidationViolationAnnotationOrder {
	@Deprecated
	@SuppressWarnings("unused")
	int alpha, beta;
}
// === end ===

// === case: anonymous_class ===
class InputFieldConsolidationViolationAnonymousClass {
	Runnable r = new Runnable() {
		int alpha, beta;

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: array_mixed_styles ===
class InputFieldConsolidationViolationArray {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_both_c_style ===
class InputFieldConsolidationViolationArrayTypeBothCStyle {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_both_c_style_merged_three_fields ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMerged {
	int[] alpha, beta, gamma;
}
// === end ===

// === case: array_type_both_c_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMultidim {
	int[][] alpha, beta;
}
// === end ===

// === case: array_type_both_java_style ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyle {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_both_java_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyleMultidimensional {
	int[][] alpha, beta;
}
// === end ===

// === case: array_type_c_style_curr_only ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnly {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_c_style_curr_only_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnlyMultidim {
	int[][] alpha, beta;
}
// === end ===

// === case: array_type_c_style_curr_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrSpaceBeforeBrackets {
	int[] alpha, beta, gamma;
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurr {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr_multidim ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurrMultidim {
	int[][] alpha, beta;
}
// === end ===

// === case: array_type_c_style_prev_multi_var ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVar {
	int[] alpha, beta, gamma;
}
// === end ===

// === case: array_type_c_style_prev_multi_var_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVarMultidim {
	int[][] alpha, beta, gamma;
}
// === end ===

// === case: array_type_c_style_prev_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStylePrevSpaceBeforeBrackets {
	int[] alpha, beta;
}
// === end ===

// === case: array_type_c_style_prev_with_final_modifier ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithFinal {
	final int[] alpha, beta;
}
// === end ===

// === case: array_type_c_style_prev_with_tab_before_semicolon ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTab {
	int[] alpha	, beta;
}
// === end ===

// === case: array_type_c_style_prev_with_trailing_line_comment ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTrailing {
	int[] alpha, beta; // a note
}
// === end ===

// === case: backward_scan_hits_text_block_annotation ===
// skip-reason: could not locate the preceding field declaration
class InputFieldConsolidationViolationTextBlockAnnotation {
	@SuppressWarnings("""
		x;
		""") String p;
	@SuppressWarnings("""
		x;
		""") String q;
}
// === end ===

// === case: boolean_primitive ===
class InputFieldConsolidationViolationBoolean {
	boolean active, visible;
}
// === end ===

// === case: both_c_style_arrays ===
class InputFieldConsolidationViolationBothCStyle {
	int[] alpha, beta;
}
// === end ===

// === case: both_lines_trailing_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationBothLinesTrailingComment {
	int alpha; // vertical extent
	int beta; // horizontal extent
}
// === end ===

// === case: comma_merge_through_annotation_array_arg ===
class InputFieldConsolidationViolationNamedArrayParam {
	@ArrayParam({1, 2})
	int alpha, beta;
}
// === end ===

// === case: comma_merge_through_annotation_named_params ===
class InputFieldConsolidationViolationNamedParams {
	@Multi(a = 1, b = 2)
	int alpha, beta;
}
// === end ===

// === case: comma_merge_through_explicit_value_array ===
class InputFieldConsolidationViolationExplicitValueArray {
	@ArrayParam(value = {1, 2})
	int alpha, beta;
}
// === end ===

// === case: comma_merge_through_paren_annotation_array_value ===
class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha, beta;
}
// === end ===

// === case: compound_array_match ===
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha, beta;
}
// === end ===

// === case: continuation_inside_carried_block_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationContinuationInsideCarriedBlockComment {
	int prevName;
	int alpha,
			beta, /* note
			more text
			*/ gamma;
}
// === end ===

// === case: continuation_stops_at_block_comment ===
class InputFieldConsolidationViolationContinuationStopsAtBlockComment {
	int prevName, alpha,
			/* single-line block comment */
			beta;
}
// === end ===

// === case: continuation_stops_at_collected_then_comment ===
class InputFieldConsolidationViolationContinuationStopsAtCollectedThenComment {
	int prevName, alpha, beta,
			// comment about gamma
			gamma;
}
// === end ===

// === case: continuation_stops_at_comment ===
class InputFieldConsolidationViolationContinuationStopsAtComment {
	int prevName, alpha,
			// comment about beta
			beta;
}
// === end ===

// === case: continuation_stops_at_javadoc ===
class InputFieldConsolidationViolationContinuationStopsAtJavadoc {
	int prevName, alpha,
			/** Javadoc for beta */
			beta;
}
// === end ===

// === case: continuation_stops_at_multi_line_block_comment ===
class InputFieldConsolidationViolationContinuationStopsAtMultiLineBlockComment {
	int prevName, alpha,
			/*
			 * multi-line comment
			 */
			beta;
}
// === end ===

// === case: enum_with_fields ===
enum InputFieldConsolidationViolationEnum {
	A,
	B;

	int alpha, beta;
}
// === end ===

// === case: final_fields ===
class InputFieldConsolidationViolationFinal {
	final int alpha, beta;

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}
// === end ===

// === case: fqn_annotation ===
class InputFieldConsolidationViolationFqnAnnotation {
	@java.lang.Deprecated
	int alpha, beta;
}
// === end ===

// === case: fqn_type ===
// imports: java.util.List
class InputFieldConsolidationViolationFqnType {
	java.util.List<String> alpha, beta;
}
// === end ===

// === case: fqn_type_no_generics ===
class InputFieldConsolidationViolationFqnTypeNoGenerics {
	java.lang.Object alpha, beta;
}
// === end ===

// === case: generic_type ===
// imports: java.util.List
class InputFieldConsolidationViolationGeneric {
	List<String> names, words;
}
// === end ===

// === case: generic_with_array_type_arg ===
// imports: java.util.List
class InputFieldConsolidationViolationGenericArrayTypeArg {
	List<String[]> alpha, beta;
}
// === end ===

// === case: inner_class ===
class InputFieldConsolidationViolationInnerClass {
	static class Inner {
		int alpha, beta;
	}
}
// === end ===

// === case: long_primitive ===
class InputFieldConsolidationViolationLong {
	long elapsed, remaining;
}
// === end ===

// === case: main ===
class InputFieldConsolidationViolationThree {
	int a, b, c;
}

class InputFieldConsolidationViolationOtherPrimitives {
	byte alphaByte, betaByte;
	char alphaChar, betaChar;
	double alphaDouble, betaDouble;
	float alphaFloat, betaFloat;
	short alphaShort, betaShort;
}

class InputFieldConsolidationViolationNamedArrayParamExplicit {
	@ArrayParam({1, 2})
	int alpha, beta;
}

class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha, beta;
}
// === end ===

// === case: merge_above_mixed_array_declaration ===
class InputFieldConsolidationViolationMergeAboveMixedArray {
	int alpha, gamma;
	int zebra, beta[];
}
// === end ===

// === case: multiline_generic_prev_field_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericPrevWraps {
	Map<String,
			Integer> alpha;
	Map<String, Integer> beta;
}
// === end ===

// === case: multiline_generic_shared_line_violation_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericSharedLine {
	Map<String, Integer> alpha; Map<String,
			Integer> beta;
}
// === end ===

// === case: multiline_generic_violation_field_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericViolationWraps {
	Map<String, Integer> alpha;
	Map<String,
			Integer> beta;
}
// === end ===

// === case: named_nested_annotation ===
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(value = @Inner)
	int alpha, beta;
}
// === end ===

// === case: nested_annotation ===
class InputFieldConsolidationViolationNestedAnnotation {
	@Container(@Inner)
	int alpha, beta;
}
// === end ===

// === case: nested_generic ===
// imports: java.util.List
// imports: java.util.Map
class InputFieldConsolidationViolationNestedGeneric {
	Map<String, List<Integer>> alphaMap, betaMap;
}
// === end ===

// === case: paramless_annotation ===
class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha, beta;
}
// === end ===

// === case: paramless_variant ===
class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha, beta;
}
// === end ===

// === case: prev_field_type_on_earlier_line ===
// skip-reason: cannot consolidate a declaration whose declarators carry different array brackets
class InputFieldConsolidationViolationPrevFieldTypeOnEarlierLine {
	int
			alpha[];
	int beta[];
}
// === end ===

// === case: prev_line_adjacent_block_comments ===
class InputFieldConsolidationViolationPrevLineAdjacentBlockComments {
	int /* ; *//* x */ alpha, beta;
}
// === end ===

// === case: prev_line_annotation_with_semicolon_in_string ===
class InputFieldConsolidationViolationPrevLineAnnotationSemicolonInString {
	@SuppressWarnings("a;b") int alpha, beta;
}
// === end ===

// === case: prev_line_block_comment_closes_and_merges ===
class InputFieldConsolidationViolationPrevLineBlockCommentCloses {
	int /* multi
	line */ alpha, beta;
}
// === end ===

// === case: prev_line_block_comment_spanning_multiple_fields ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationPrevLineBlockCommentSpanning {
	int /* comment */ alpha;
	int /* comment */ beta;
}
// === end ===

// === case: prev_line_block_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineBlockCommentWithSemicolon {
	int /* ; */ alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_escaped_backslash ===
class InputFieldConsolidationViolationPrevLineCharEscapedBackslash {
	@Ann('\\') int alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_escaped_quote ===
class InputFieldConsolidationViolationPrevLineCharEscapedQuote {
	@Ann('\'') int alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_semicolon ===
class InputFieldConsolidationViolationPrevLineCharSemicolon {
	@Ann(';') int alpha, beta;
}
// === end ===

// === case: prev_line_trailing_comment_not_ending_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentNoSemicolon {
	int alpha, beta; // field comment
}
// === end ===

// === case: prev_line_trailing_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentWithSemicolon {
	int alpha, beta; // see init();
}
// === end ===

// === case: primitives_two_int ===
class InputFieldConsolidationViolationPrimitives {
	int alpha, beta;
}
// === end ===

// === case: protected_fields ===
class InputFieldConsolidationViolationProtected {
	protected int alpha, beta;
}
// === end ===

// === case: record_static_fields ===
record InputFieldConsolidationViolationRecord(int x) {
	static String first, second;
}
// === end ===

// === case: references_two_string ===
class InputFieldConsolidationViolationReferences {
	String first, second;
}
// === end ===

// === case: same_annotation_params ===
class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha, beta;
}
// === end ===

// === case: shorthand_vs_explicit_expr ===
class InputFieldConsolidationViolationShorthandVsExplicitExpr {
	@SuppressWarnings("unused")
	int alpha, beta;
}
// === end ===

// === case: shorthand_vs_explicit_nested ===
class InputFieldConsolidationViolationShorthandVsExplicitNested {
	@Container(@Inner)
	int alpha, beta;
}
// === end ===

// === case: simple_primitive_fields ===
class InputFieldConsolidationViolationSimplePrimitive {
	int alpha, beta;
}
// === end ===

// === case: simple_reference_fields ===
class InputFieldConsolidationViolationSimpleReference {
	String first, second;
}
// === end ===

// === case: static_fields ===
class InputFieldConsolidationViolationStatic {
	static int global, shared;
}
// === end ===

// === case: tab_separated_multi_var_on_violation_line ===
class InputFieldConsolidationViolationTabSeparatedMultiVar {
	int a, b, c;
}
// === end ===

// === case: three_fields_bottom_up_first_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpFirstPass {
	int a, b, c;
}
// === end ===

// === case: three_fields_bottom_up_second_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpSecondPass {
	int a, b, c;
}
// === end ===

// === case: type_depth_just_under_cap ===
class InputFieldConsolidationViolationTypeDepthJustUnderCap {
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> alpha, beta;
}
// === end ===

// === case: unbounded_wildcard ===
// imports: java.util.List
class InputFieldConsolidationViolationUnboundedWildcard {
	List<?> alphaUnbounded, betaUnbounded;
}
// === end ===

// === case: violation_column_forward_scan_past_semicolon ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationColumnForwardScanPastSemicolon {
	int alpha;
	int beta; int gamma;
}
// === end ===

// === case: violation_line_block_comment_after_field_name_proceeds ===
class InputFieldConsolidationViolationBlockCommentAfterFieldName {
	int alpha, beta; /* note */
}
// === end ===

// === case: violation_line_block_comment_carry_inner ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentCarryInner {
	int alpha; /* open
	comment */ int beta /* doc */;
}
// === end ===

// === case: violation_line_block_comment_inside_char_literal_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideCharLiteral {
	@Ann('/') int alpha, beta;
}
// === end ===

// === case: violation_line_block_comment_inside_escaped_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideEscapedString {
	@SuppressWarnings("a\"/*b") int alpha, beta;
}
// === end ===

// === case: violation_line_block_comment_inside_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideString {
	@SuppressWarnings("a/*b") int alpha, beta;
}
// === end ===

// === case: violation_line_block_comment_post_name ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentPostName {
	int alpha;
	int beta /* doc */;
}
// === end ===

// === case: violation_line_block_comment_with_semicolon ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentWithSemicolon {
	int alpha;
	int /* ; */ beta;
}
// === end ===

// === case: violation_line_trailing_block_comment_unclosed_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationTrailingBlockCommentUnclosed {
	int alpha;
	int beta; /* note
			continued */
}
// === end ===

// === case: wildcard_extends ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcard {
	List<? extends Number> alphaList, betaList;
}
// === end ===

// === case: wildcard_super ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcardSuper {
	List<? super Integer> alphaSuper, betaSuper;
}
// === end ===

// === case: with_annotations_on_own_line ===
class InputFieldConsolidationViolationWithAnnotationsOnOwnLine {
	@NonNull
	protected Button nextButton, presetsButton;
}
// === end ===

// === case: with_escaped_quote_in_annotation_string ===
class InputFieldConsolidationViolationWithEscapedQuoteInAnn {
	@SuppressWarnings("a\"b") int alpha, beta;
}
// === end ===

// === case: with_multiple_annotations ===
class InputFieldConsolidationViolationWithMultipleAnnotations {
	@CheckResult
	@NonNull
	String alpha, beta;
}
// === end ===

// === case: with_violation_comment ===
class InputFieldConsolidationViolationWithViolationComment {
	int alpha, beta;
}
// === end ===

// === case: wrap_boundary121_wraps ===
class InputFieldConsolidationViolationWrapBoundary121 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: wrap_boundary_exactly120_no_wrap ===
class InputFieldConsolidationViolationWrapBoundary120 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: wrap_c_style_arrays ===
class InputFieldConsolidationViolationWrapCStyleArrays {
	int[] aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: wrap_continuation_comment_preserved ===
class InputFieldConsolidationViolationWrapContinuationCommentPreserved {
	int prevName, alpha, beta; // important
}
// === end ===

// === case: wrap_continuation_from_previous_wrap ===
class InputFieldConsolidationViolationWrapContinuationFromPreviousWrap {
	int prevName, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			cccccccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: wrap_continuation_multiple_lines ===
class InputFieldConsolidationViolationWrapContinuationMultipleLines {
	int prevName, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, cccccccccccccccccccccccccccccc,
			dddddddddddddddddddddddddddddd;
}
// === end ===

// === case: wrap_deep_indent ===
class InputFieldConsolidationViolationWrapDeepIndent {
	static class Outer {
		int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
				bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
	}
}
// === end ===

// === case: wrap_each_name_own_line ===
class InputFieldConsolidationViolationWrapEachNameOwnLine {
	static class Outer {
		static class Inner {
			int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
					bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
					ccccccccccccccccccccccccccccccccccccccccccccccccccc;
		}
	}
}
// === end ===

// === case: wrap_four_fields_two_per_line ===
class InputFieldConsolidationViolationWrapFourFieldsTwoPerLine {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			ccccccccccccccccccccccccccccccccccc, ddddddddddddddddddddddddddddddddddd;
}
// === end ===

// === case: wrap_three_fields_one_plus_two ===
class InputFieldConsolidationViolationWrapThreeFieldsOnePlusTwo {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, cccccccccccccccccccccccccccccc;
}
// === end ===

// === case: wrap_three_fields_two_plus_one ===
class InputFieldConsolidationViolationWrapThreeFieldsTwoPlusOne {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			ccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: wrap_with_modifiers ===
class InputFieldConsolidationViolationWrapWithModifiers {
	private static int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
}
// === end ===

// === case: wrap_with_trailing_comment ===
class InputFieldConsolidationViolationWrapWithTrailingComment {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // see init();
}
// === end ===