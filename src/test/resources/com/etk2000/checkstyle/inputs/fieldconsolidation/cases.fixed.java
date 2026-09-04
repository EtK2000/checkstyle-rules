// === case: array_type_c_style_prev_with_modifier_annotation_type_dims_merges ===
class InputFieldConsolidationViolationArrayTypeCStylePrevWithModifierAnnotationTypeDims {
	@Deprecated
	int[][] alpha, beta;
}
// === end ===

// === case: comma_merge_through_explicit_value_array ===
class InputFieldConsolidationViolationExplicitValueArray {
	@ArrayParam({1, 2})
	int alpha, beta;
}
// === end ===

// === case: constant_between_blank_lines_merges_past ===
class InputFieldConsolidationViolationConstantBetweenBlankLinesMergesPast {
	static final int LIMIT = 5;

	static int alpha, gamma;
}
// === end ===

// === case: constant_between_blank_static_finals_merges_past ===
class InputFieldConsolidationViolationConstantBetweenBlankStaticFinalsMergesPast {
	static final int LIMIT = 5;

	static final int ALPHA, GAMMA;

	static {
		ALPHA = 1;
		GAMMA = 2;
	}
}
// === end ===

// === case: constant_between_statics_merges_past ===
class InputFieldConsolidationViolationConstantBetweenStaticsMergesPast {
	static final int LIMIT = 5;

	static int alpha, gamma;
}
// === end ===

// === case: constant_interleaved_with_run_stays_apart ===
class InputFieldConsolidationViolationConstantInterleavedWithRunStaysApart {
	static final int A = 1;
	static final int B = 2;

	static int alpha, beta;
}
// === end ===

// === case: constant_split_between_statics_merges_past ===
class InputFieldConsolidationViolationConstantSplitBetweenStaticsMergesPast {
	static final int LIMIT =
			5;

	static int alpha, gamma;
}
// === end ===

// === case: constant_with_trailing_directive_between_merges_past ===
class InputFieldConsolidationViolationConstantWithTrailingDirectiveBetweenMergesPast {
	static final int LIMIT = 5; // NOSONAR

	static int alpha, gamma;
}
// === end ===

// === case: continuation_inside_carried_block_comment_bails ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationContinuationInsideCarriedBlockComment {
	int alpha,
			beta, /* note
			more text
			*/ gamma, prevName;
}
// === end ===

// === case: continuation_stops_at_multi_line_block_comment ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration spans several lines
class InputFieldConsolidationViolationContinuationStopsAtMultiLineBlockComment {
	int alpha,
			/*
			 * multi-line comment
			 */
			beta, prevName;
}
// === end ===

// === case: directive_ahead_of_rebuilt_wrapped_first_declaration_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveAheadOfRebuiltWrappedFirstDeclarationSkips {
	int alpha;
	/* NOSONAR */ int zeta,
			beta;
}
// === end ===

// === case: directive_inside_rebuilt_first_initializer_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideRebuiltFirstInitializerSkips {
	int alpha;
	int zeta = /* NOSONAR */ 1;
}
// === end ===

// === case: directive_inside_rebuilt_first_initializer_sorted_after_initialized_skips ===
// skip-reason: cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for
class InputFieldConsolidationViolationDirectiveInsideRebuiltFirstInitializerSortedAfterInitializedSkips {
	int alpha = 2;
	int zeta = /* NOSONAR */ 1;
}
// === end ===

// === case: directive_on_rebuilt_first_declaration_skips ===
// skip-reason: cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line
class InputFieldConsolidationViolationDirectiveOnRebuiltFirstDeclarationSkips {
	int alpha;
	int zeta /* NOSONAR */;
}
// === end ===

// === case: enum_trailing_comma_before_fields ===
enum InputFieldConsolidationViolationEnumTrailingComma {
	A,
	B;

	int alpha, beta;
}
// === end ===

// === case: fqn_annotation ===
class InputFieldConsolidationViolationFqnAnnotation {
	@Deprecated
	int alpha, beta;
}
// === end ===

// === case: fqn_type ===
// imports: java.util.List
class InputFieldConsolidationViolationFqnType {
	List<String> alpha, beta;
}
// === end ===

// === case: fqn_type_no_generics ===
class InputFieldConsolidationViolationFqnTypeNoGenerics {
	Object alpha, beta;
}
// === end ===

// === case: later_reader_of_moved_declaration_merges ===
class InputFieldConsolidationViolationLaterReaderOfMovedDeclarationMerges {
	int alpha, beta = 2;
	long copy = beta;
	String label;
}
// === end ===

// === case: long_initializer_interleaved_runs_fold ===
class InputFieldConsolidationViolationLongInitializerInterleavedRunsFold {
	int alpha, gamma;
	int beta = 1_000_000 + 2_000_000 + 3_000_000;
	String first, second;
}
// === end ===

// === case: long_initializer_middle_name_of_declaration_between ===
class InputFieldConsolidationViolationLongInitializerMiddleNameOfDeclarationBetween {
	int alpha, delta;
	int beta, epsilon, gamma = 1_000_000 + 2_000_000 + 3_000_000;
}
// === end ===

// === case: named_nested_annotation ===
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(@Inner)
	int alpha, beta;
}
// === end ===

// === case: prev_line_annotation_with_semicolon_in_string ===
class InputFieldConsolidationViolationPrevLineAnnotationSemicolonInString {
	@SuppressWarnings("a;b")
	int alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_escaped_backslash ===
class InputFieldConsolidationViolationPrevLineCharEscapedBackslash {
	@Ann('\\')
	int alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_escaped_quote ===
class InputFieldConsolidationViolationPrevLineCharEscapedQuote {
	@Ann('\'')
	int alpha, beta;
}
// === end ===

// === case: prev_line_char_literal_with_semicolon ===
class InputFieldConsolidationViolationPrevLineCharSemicolon {
	@Ann(';')
	int alpha, beta;
}
// === end ===

// === case: sorting_suppressed_explicit_value ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedExplicitValue {
	int beta, alpha;
}
// === end ===

// === case: sorting_suppressed_explicit_value_array ===
@SuppressWarnings({"FieldSorting"})
class InputFieldConsolidationViolationSortingSuppressedExplicitValueArray {
	int beta, alpha;
}
// === end ===

// === case: sorting_suppressed_qualified_annotation ===
@SuppressWarnings("FieldSorting")
class InputFieldConsolidationViolationSortingSuppressedQualifiedAnnotation {
	int beta, alpha;
}
// === end ===

// === case: violation_line_block_comment_inside_char_literal_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideCharLiteral {
	@Ann('/')
	int alpha, beta;
}
// === end ===

// === case: violation_line_block_comment_inside_escaped_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideEscapedString {
	@SuppressWarnings("a\"/*b")
	int alpha, beta;
}
// === end ===

// === case: violation_line_block_comment_inside_string_proceeds ===
class InputFieldConsolidationViolationBlockCommentInsideString {
	@SuppressWarnings("a/*b")
	int alpha, beta;
}
// === end ===

// === case: with_escaped_quote_in_annotation_string ===
class InputFieldConsolidationViolationWithEscapedQuoteInAnn {
	@SuppressWarnings("a\"b")
	int alpha, beta;
}
// === end ===

// === case: wrapped_annotation_array_closes_on_violation_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationArrayClosesOnViolationLineMerges {
	@SuppressWarnings({"a", "b"})
	int alpha, beta;
}
// === end ===

// === case: wrapped_annotation_closes_on_stacked_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationClosesOnStackedLineMerges {
	@Outer({@Element(1), @Element(2), @Element(3)})
	int alpha, beta;
}
// === end ===

// === case: wrapped_annotation_closes_on_violation_line_merges ===
class InputFieldConsolidationViolationWrappedAnnotationClosesOnViolationLineMerges {
	@Size(min = 1, max = 10, message = "m")
	String alpha, beta;
}
// === end ===

// === case: wrapped_annotation_two_lines_merges ===
class InputFieldConsolidationViolationWrappedAnnotationTwoLinesMerges {
	@Size(min = 1, max = 10)
	String alpha, beta;
}
// === end ===