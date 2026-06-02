// === case: comma_merge_through_explicit_value_array ===
class InputFieldConsolidationViolationExplicitValueArray {
	@ArrayParam({1, 2})
	int alpha, beta;
}
// === end ===

// === case: continuation_inside_carried_block_comment_bails ===
// skip-reason: cannot consolidate when a declaration line carries content after its terminator
class InputFieldConsolidationViolationContinuationInsideCarriedBlockComment {
	int alpha,
			beta, /* note
			more text
			*/ gamma, prevName;
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

// === case: named_nested_annotation ===
class InputFieldConsolidationViolationNamedNestedAnnotation {
	@Container(@Inner)
	int alpha, beta;
}
// === end ===

// === case: prev_field_type_on_earlier_line ===
// skip-reason: cannot consolidate a declaration whose declarators carry different array brackets
class InputFieldConsolidationViolationPrevFieldTypeOnEarlierLine {
	int
			alpha[];
	int[] beta;
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

// === case: wrap_continuation_comment_preserved ===
class InputFieldConsolidationViolationWrapContinuationCommentPreserved {
	int alpha, beta, prevName; // important
}
// === end ===

// === case: wrap_continuation_from_previous_wrap ===
class InputFieldConsolidationViolationWrapContinuationFromPreviousWrap {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb,
			cccccccccccccccccccccccccccccccccccccccc, prevName;
}
// === end ===

// === case: wrap_continuation_multiple_lines ===
class InputFieldConsolidationViolationWrapContinuationMultipleLines {
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa, bbbbbbbbbbbbbbbbbbbbbbbbbbbbbb, cccccccccccccccccccccccccccccc,
			dddddddddddddddddddddddddddddd, prevName;
}
// === end ===