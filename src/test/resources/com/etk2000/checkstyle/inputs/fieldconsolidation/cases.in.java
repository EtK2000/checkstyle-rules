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
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}
// === end ===

// === case: annotated_lower_bound_match ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedLowerBoundMatch {
	List<? super @ViolationTypeAnn Number> alpha;
	List<? super @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? super @ViolationTypeAnn Number>') should be declared on one line.
}
// === end ===

// === case: annotated_upper_bound ===
// imports: java.util.List
class InputFieldConsolidationViolationAnnotatedBound {
	List<? extends @ViolationTypeAnn Number> alpha;
	List<? extends @ViolationTypeAnn Number> beta; // violation: Fields 'beta' and 'alpha' (type 'List<? extends @ViolationTypeAnn Number>') should be declared on one line.
}
// === end ===

// === case: annotation_line_trailing_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationAnnotationLineTrailingComment {
	@Deprecated
	int alpha;
	@Deprecated // kept for the legacy wire format
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: annotation_order_swapped ===
class InputFieldConsolidationViolationAnnotationOrder {
	@Deprecated
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: anonymous_class ===
class InputFieldConsolidationViolationAnonymousClass {
	Runnable r = new Runnable() {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

		@Override
		public void run() {
		}
	};
}
// === end ===

// === case: array_mixed_styles ===
class InputFieldConsolidationViolationArray {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_both_c_style ===
class InputFieldConsolidationViolationArrayTypeBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_both_c_style_merged_three_fields ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMerged {
	int alpha[];
	int beta[], gamma[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_both_c_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothCStyleMultidim {
	int alpha[][];
	int beta[][]; // violation: Fields 'beta' and 'alpha' (type 'int[][]') should be declared on one line.
}
// === end ===

// === case: array_type_both_java_style ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyle {
	int[] alpha;
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_both_java_style_multidimensional ===
class InputFieldConsolidationViolationArrayTypeBothJavaStyleMultidimensional {
	int[][] alpha;
	int[][] beta; // violation: Fields 'beta' and 'alpha' (type 'int[][]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_only ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnly {
	int[] alpha;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_only_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrOnlyMultidim {
	int[][] alpha;
	int beta[][]; // violation: Fields 'beta' and 'alpha' (type 'int[][]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_curr_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStyleCurrSpaceBeforeBrackets {
	int alpha[];
	int beta [], gamma[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurr {
	int alpha[];
	int[] beta; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_java_style_curr_multidim ===
class InputFieldConsolidationViolationArrayTypeCStylePrevJavaStyleCurrMultidim {
	int alpha[][];
	int[][] beta; // violation: Fields 'beta' and 'alpha' (type 'int[][]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_multi_var ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVar {
	int alpha[], beta[];
	int gamma[]; // violation: Fields 'gamma' and 'beta' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_multi_var_multidimensional ===
class InputFieldConsolidationViolationArrayTypeCStylePrevMultiVarMultidim {
	int alpha[][], beta[][];
	int[][] gamma; // violation: Fields 'gamma' and 'beta' (type 'int[][]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_space_before_brackets ===
class InputFieldConsolidationViolationArrayTypeCStylePrevSpaceBeforeBrackets {
	int alpha [];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_final_modifier ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithFinal {
	final int alpha[];
	final int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_tab_before_semicolon ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTab {
	int alpha[]	;
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: array_type_c_style_prev_with_trailing_line_comment ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithTrailing {
	int alpha[]; // a note
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
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
		""") String q; // violation: Fields 'q' and 'p' (type 'String') should be declared on one line.
}
// === end ===

// === case: boolean_primitive ===
class InputFieldConsolidationViolationBoolean {
	boolean active;
	boolean visible; // violation: Fields 'visible' and 'active' (type 'boolean') should be declared on one line.
}
// === end ===

// === case: both_c_style_arrays ===
class InputFieldConsolidationViolationBothCStyle {
	int alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: both_lines_trailing_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationBothLinesTrailingComment {
	int alpha; // vertical extent
	int beta; // horizontal extent // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: comma_merge_through_annotation_array_arg ===
class InputFieldConsolidationViolationNamedArrayParam {
	@ArrayParam({1, 2})
	int alpha;
	@ArrayParam({1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: comma_merge_through_annotation_named_params ===
class InputFieldConsolidationViolationNamedParams {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(a = 1, b = 2)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: comma_merge_through_explicit_value_array ===
class InputFieldConsolidationViolationExplicitValueArray {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: comma_merge_through_paren_annotation_array_value ===
class InputFieldConsolidationViolationComplexParam {
	@SuppressWarnings({"unused", "all"})
	int alpha;
	@SuppressWarnings({"unused", "all"})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: compound_array_match ===
class InputFieldConsolidationViolationCompoundArray {
	String[][] alpha;
	String[] beta[]; // violation: Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.
}
// === end ===

// === case: continuation_inside_carried_block_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationContinuationInsideCarriedBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			beta, /* note
			more text
			*/ gamma;
}
// === end ===

// === case: continuation_stops_at_block_comment ===
class InputFieldConsolidationViolationContinuationStopsAtBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			/* single-line block comment */
			beta;
}
// === end ===

// === case: continuation_stops_at_collected_then_comment ===
class InputFieldConsolidationViolationContinuationStopsAtCollectedThenComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			beta,
			// comment about gamma
			gamma;
}
// === end ===

// === case: continuation_stops_at_comment ===
class InputFieldConsolidationViolationContinuationStopsAtComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			// comment about beta
			beta;
}
// === end ===

// === case: continuation_stops_at_javadoc ===
class InputFieldConsolidationViolationContinuationStopsAtJavadoc {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			/** Javadoc for beta */
			beta;
}
// === end ===

// === case: continuation_stops_at_multi_line_block_comment ===
class InputFieldConsolidationViolationContinuationStopsAtMultiLineBlockComment {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
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

	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: final_fields ===
class InputFieldConsolidationViolationFinal {
	final int alpha;
	final int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.

	InputFieldConsolidationViolationFinal(int alpha, int beta) {
		this.alpha = alpha;
		this.beta = beta;
	}
}
// === end ===

// === case: fqn_annotation ===
class InputFieldConsolidationViolationFqnAnnotation {
	@java.lang.Deprecated
	int alpha;
	@java.lang.Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: fqn_type ===
// imports: java.util.List
class InputFieldConsolidationViolationFqnType {
	java.util.List<String> alpha;
	java.util.List<String> beta; // violation: Fields 'beta' and 'alpha' (type 'java.util.List<String>') should be declared on one line.
}
// === end ===

// === case: fqn_type_no_generics ===
class InputFieldConsolidationViolationFqnTypeNoGenerics {
	java.lang.Object alpha;
	java.lang.Object beta; // violation: Fields 'beta' and 'alpha' (type 'java.lang.Object') should be declared on one line.
}
// === end ===

// === case: generic_type ===
// imports: java.util.List
class InputFieldConsolidationViolationGeneric {
	List<String> names;
	List<String> words; // violation: Fields 'words' and 'names' (type 'List<String>') should be declared on one line.
}
// === end ===

// === case: generic_with_array_type_arg ===
// imports: java.util.List
class InputFieldConsolidationViolationGenericArrayTypeArg {
	List<String[]> alpha;
	List<String[]> beta; // violation: Fields 'beta' and 'alpha' (type 'List<String[]>') should be declared on one line.
}
// === end ===

// === case: inner_class ===
class InputFieldConsolidationViolationInnerClass {
	static class Inner {
		int alpha;
		int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
	}
}
// === end ===

// === case: long_primitive ===
class InputFieldConsolidationViolationLong {
	long elapsed;
	long remaining; // violation: Fields 'remaining' and 'elapsed' (type 'long') should be declared on one line.
}
// === end ===

// === case: main ===
class InputFieldConsolidationViolationThree {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationOtherPrimitives {
	byte alphaByte;
	byte betaByte; // violation: Fields 'betaByte' and 'alphaByte' (type 'byte') should be declared on one line.
	char alphaChar;
	char betaChar; // violation: Fields 'betaChar' and 'alphaChar' (type 'char') should be declared on one line.
	double alphaDouble;
	double betaDouble; // violation: Fields 'betaDouble' and 'alphaDouble' (type 'double') should be declared on one line.
	float alphaFloat;
	float betaFloat; // violation: Fields 'betaFloat' and 'alphaFloat' (type 'float') should be declared on one line.
	short alphaShort;
	short betaShort; // violation: Fields 'betaShort' and 'alphaShort' (type 'short') should be declared on one line.
}

class InputFieldConsolidationViolationNamedArrayParamExplicit {
	@ArrayParam(value = {1, 2})
	int alpha;
	@ArrayParam(value = {1, 2})
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}

class InputFieldConsolidationViolationParamOrder {
	@Multi(a = 1, b = 2)
	int alpha;
	@Multi(b = 2, a = 1)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: merge_above_mixed_array_declaration ===
class InputFieldConsolidationViolationMergeAboveMixedArray {
	int alpha;
	int gamma; // violation: Fields 'gamma' and 'alpha' (type 'int') should be declared on one line.
	int zebra, beta[];
}
// === end ===

// === case: multiline_generic_prev_field_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericPrevWraps {
	Map<String,
			Integer> alpha;
	Map<String, Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'Map<String,Integer>') should be declared on one line.
}
// === end ===

// === case: multiline_generic_shared_line_violation_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericSharedLine {
	Map<String, Integer> alpha; Map<String,
			Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'Map<String,Integer>') should be declared on one line.
}
// === end ===

// === case: multiline_generic_violation_field_wraps ===
// imports: java.util.Map
// skip-reason: cannot consolidate a field whose declaration spans multiple lines
class InputFieldConsolidationViolationMultilineGenericViolationWraps {
	Map<String, Integer> alpha;
	Map<String,
			Integer> beta; // violation: Fields 'beta' and 'alpha' (type 'Map<String,Integer>') should be declared on one line.
}
// === end ===

// === case: named_nested_annotation ===
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(value = @Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: nested_annotation ===
class InputFieldConsolidationViolationNestedAnnotation {
	@Container(@Inner)
	int alpha;
	@Container(@Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: nested_generic ===
// imports: java.util.List
// imports: java.util.Map
class InputFieldConsolidationViolationNestedGeneric {
	Map<String, List<Integer>> alphaMap;
	Map<String, List<Integer>> betaMap; // violation: Fields 'betaMap' and 'alphaMap' (type 'Map<String,List<Integer>>') should be declared on one line.
}
// === end ===

// === case: paramless_annotation ===
class InputFieldConsolidationViolationParamlessAnnotation {
	@Deprecated
	int alpha;
	@Deprecated
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: paramless_variant ===
class InputFieldConsolidationViolationParamlessVariant {
	@Deprecated
	int alpha;
	@Deprecated()
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_field_type_on_earlier_line ===
// skip-reason: cannot consolidate a declaration whose declarators carry different array brackets
class InputFieldConsolidationViolationPrevFieldTypeOnEarlierLine {
	int
			alpha[];
	int beta[]; // violation: Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: prev_line_adjacent_block_comments ===
class InputFieldConsolidationViolationPrevLineAdjacentBlockComments {
	int /* ; *//* x */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_annotation_with_semicolon_in_string ===
class InputFieldConsolidationViolationPrevLineAnnotationSemicolonInString {
	@SuppressWarnings("a;b") int alpha;
	@SuppressWarnings("a;b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_closes_and_merges ===
class InputFieldConsolidationViolationPrevLineBlockCommentCloses {
	int /* multi
	line */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_spanning_multiple_fields ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationPrevLineBlockCommentSpanning {
	int /* comment */ alpha;
	int /* comment */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_block_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineBlockCommentWithSemicolon {
	int /* ; */ alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_escaped_backslash ===
class InputFieldConsolidationViolationPrevLineCharEscapedBackslash {
	@Ann('\\') int alpha;
	@Ann('\\') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_escaped_quote ===
class InputFieldConsolidationViolationPrevLineCharEscapedQuote {
	@Ann('\'') int alpha;
	@Ann('\'') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_char_literal_with_semicolon ===
class InputFieldConsolidationViolationPrevLineCharSemicolon {
	@Ann(';') int alpha;
	@Ann(';') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_trailing_comment_not_ending_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentNoSemicolon {
	int alpha; // field comment
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: prev_line_trailing_comment_with_semicolon ===
class InputFieldConsolidationViolationPrevLineTrailingCommentWithSemicolon {
	int alpha; // see init();
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: primitives_two_int ===
class InputFieldConsolidationViolationPrimitives {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: protected_fields ===
class InputFieldConsolidationViolationProtected {
	protected int alpha;
	protected int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: record_static_fields ===
record InputFieldConsolidationViolationRecord(int x) {
	static String first;
	static String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}
// === end ===

// === case: references_two_string ===
class InputFieldConsolidationViolationReferences {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}
// === end ===

// === case: same_annotation_params ===
class InputFieldConsolidationViolationSameParams {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings("unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: shorthand_vs_explicit_expr ===
class InputFieldConsolidationViolationShorthandVsExplicitExpr {
	@SuppressWarnings("unused")
	int alpha;
	@SuppressWarnings(value = "unused")
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: shorthand_vs_explicit_nested ===
class InputFieldConsolidationViolationShorthandVsExplicitNested {
	@Container(@Inner)
	int alpha;
	@Container(value = @Inner)
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: simple_primitive_fields ===
class InputFieldConsolidationViolationSimplePrimitive {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: simple_reference_fields ===
class InputFieldConsolidationViolationSimpleReference {
	String first;
	String second; // violation: Fields 'second' and 'first' (type 'String') should be declared on one line.
}
// === end ===

// === case: static_fields ===
class InputFieldConsolidationViolationStatic {
	static int global;
	static int shared; // violation: Fields 'shared' and 'global' (type 'int') should be declared on one line.
}
// === end ===

// === case: tab_separated_multi_var_on_violation_line ===
class InputFieldConsolidationViolationTabSeparatedMultiVar {
	int a;
	int b,	c; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
}
// === end ===

// === case: three_fields_bottom_up_first_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpFirstPass {
	int a;
	int b; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
	int c; // violation: Fields 'c' and 'b' (type 'int') should be declared on one line.
}
// === end ===

// === case: three_fields_bottom_up_second_pass ===
class InputFieldConsolidationViolationThreeFieldsBottomUpSecondPass {
	int a;
	int b, c; // violation: Fields 'b' and 'a' (type 'int') should be declared on one line.
}
// === end ===

// === case: type_depth_just_under_cap ===
class InputFieldConsolidationViolationTypeDepthJustUnderCap {
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> alpha;
	L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> beta; // violation: Fields 'beta' and 'alpha' (type 'L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<L<String>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>') should be declared on one line.
}
// === end ===

// === case: unbounded_wildcard ===
// imports: java.util.List
class InputFieldConsolidationViolationUnboundedWildcard {
	List<?> alphaUnbounded;
	List<?> betaUnbounded; // violation: Fields 'betaUnbounded' and 'alphaUnbounded' (type 'List<?>') should be declared on one line.
}
// === end ===

// === case: violation_column_forward_scan_past_semicolon ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationColumnForwardScanPastSemicolon {
	int alpha;
	int beta; int gamma; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_after_field_name_proceeds ===
class InputFieldConsolidationViolationBlockCommentAfterFieldName {
	int alpha;
	int beta; /* note */ // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_carry_inner ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentCarryInner {
	int alpha; /* open
	comment */ int beta /* doc */; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_char_literal_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideCharLiteral {
	@Ann('/') int alpha;
	@Ann('/') int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_escaped_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideEscapedString {
	@SuppressWarnings("a\"/*b") int alpha;
	@SuppressWarnings("a\"/*b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_inside_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideString {
	@SuppressWarnings("a/*b") int alpha;
	@SuppressWarnings("a/*b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_post_name ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentPostName {
	int alpha;
	int beta /* doc */; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_block_comment_with_semicolon ===
// skip-reason: block comment on the field declaration line
class InputFieldConsolidationViolationBlockCommentWithSemicolon {
	int alpha;
	int /* ; */ beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: violation_line_trailing_block_comment_unclosed_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationTrailingBlockCommentUnclosed {
	int alpha;
	int beta; /* note // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
			continued */
}
// === end ===

// === case: wildcard_extends ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcard {
	List<? extends Number> alphaList;
	List<? extends Number> betaList; // violation: Fields 'betaList' and 'alphaList' (type 'List<? extends Number>') should be declared on one line.
}
// === end ===

// === case: wildcard_super ===
// imports: java.util.List
class InputFieldConsolidationViolationWildcardSuper {
	List<? super Integer> alphaSuper;
	List<? super Integer> betaSuper; // violation: Fields 'betaSuper' and 'alphaSuper' (type 'List<? super Integer>') should be declared on one line.
}
// === end ===

// === case: with_annotations_on_own_line ===
class InputFieldConsolidationViolationWithAnnotationsOnOwnLine {
	@NonNull
	protected Button nextButton;
	@NonNull
	protected Button presetsButton; // violation: Fields 'presetsButton' and 'nextButton' (type 'Button') should be declared on one line.
}
// === end ===

// === case: with_escaped_quote_in_annotation_string ===
class InputFieldConsolidationViolationWithEscapedQuoteInAnn {
	@SuppressWarnings("a\"b") int alpha;
	@SuppressWarnings("a\"b") int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: with_multiple_annotations ===
class InputFieldConsolidationViolationWithMultipleAnnotations {
	@CheckResult
	@NonNull
	String alpha;
	@CheckResult
	@NonNull
	String beta; // violation: Fields 'beta' and 'alpha' (type 'String') should be declared on one line.
}
// === end ===

// === case: with_violation_comment ===
class InputFieldConsolidationViolationWithViolationComment {
	int alpha;
	int beta; // violation: Fields 'beta' and 'alpha' (type 'int') should be declared on one line.
}
// === end ===

// === case: wrap_boundary121_wraps ===
class InputFieldConsolidationViolationWrapBoundary121 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
}
// === end ===

// === case: wrap_boundary_exactly120_no_wrap ===
class InputFieldConsolidationViolationWrapBoundary120 {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
}
// === end ===

// === case: wrap_c_style_arrays ===
class InputFieldConsolidationViolationWrapCStyleArrays {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa[];
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb[]; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int[]') should be declared on one line.
}
// === end ===

// === case: wrap_continuation_comment_preserved ===
class InputFieldConsolidationViolationWrapContinuationCommentPreserved {
	int prevName;
	int alpha, // violation: Fields 'alpha' and 'prevName' (type 'int') should be declared on one line.
			beta; // important
}
// === end ===

// === case: wrap_continuation_from_previous_wrap ===
class InputFieldConsolidationViolationWrapContinuationFromPreviousWrap {
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, // violation: Fields 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' and 'prevName' (type 'int') should be declared on one line.
			cccccccccccccccccccccccccccccccccccccccc;
}
// === end ===

// === case: wrap_continuation_multiple_lines ===
class InputFieldConsolidationViolationWrapContinuationMultipleLines {
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, // violation: Fields 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' and 'prevName' (type 'int') should be declared on one line.
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			cccccccccccccccccccccccccccccc,
			dddddddddddddddddddddddddddddd;
}
// === end ===

// === case: wrap_deep_indent ===
class InputFieldConsolidationViolationWrapDeepIndent {
	static class Outer {
		int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
		int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
	}
}
// === end ===

// === case: wrap_each_name_own_line ===
class InputFieldConsolidationViolationWrapEachNameOwnLine {
	static class Outer {
		static class Inner {
			int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
			int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
		}
	}
}
// === end ===

// === case: wrap_four_fields_two_per_line ===
class InputFieldConsolidationViolationWrapFourFieldsTwoPerLine {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccc, ddddddddddddddddddddddddddddddddddd; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') should be declared on one line.
}
// === end ===

// === case: wrap_three_fields_one_plus_two ===
class InputFieldConsolidationViolationWrapThreeFieldsOnePlusTwo {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, cccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') should be declared on one line.
}
// === end ===

// === case: wrap_three_fields_two_plus_one ===
class InputFieldConsolidationViolationWrapThreeFieldsTwoPlusOne {
	boolean aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	boolean bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, ccccccccccccccccccccccccccccccccccc; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'boolean') should be declared on one line.
}
// === end ===

// === case: wrap_with_modifiers ===
class InputFieldConsolidationViolationWrapWithModifiers {
	private static int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	private static int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
}
// === end ===

// === case: wrap_with_trailing_comment ===
class InputFieldConsolidationViolationWrapWithTrailingComment {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa; // see init();
	int bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb; // violation: Fields 'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb' and 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa' (type 'int') should be declared on one line.
}
// === end ===