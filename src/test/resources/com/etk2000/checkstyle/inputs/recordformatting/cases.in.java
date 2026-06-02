package com.etk2000.checkstyle.inputs.recordformatting;

interface Foo {}

// === case: braces_brace_next_line_body ===
class InputRecordFormattingBracesBraceNextLineBodySliceViolation {
	record BraceNextLineBody(int a)
	{ // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
		void m() {}
	}
}
// === end ===

// === case: braces_brace_next_line_empty ===
class InputRecordFormattingBracesBraceNextLineEmptySliceViolation {
	record BraceNextLineEmpty(int a)
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: braces_empty_body_split ===
class InputRecordFormattingBracesEmptyBodySplitSliceViolation {
	record EmptyBodySplit(int a) {
	} // violation: Empty record body must be '{}' on one line.
}
// === end ===

// === case: braces_implements_brace_next_line ===
class InputRecordFormattingBracesImplementsBraceNextLineSliceViolation {
	record ImplementsBraceNextLine(int a) implements Foo
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: braces_implements_multi_line ===
class InputRecordFormattingBracesImplementsMultiLineSliceViolation {
	record ImplementsMultiLine(int a) implements
			Foo
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: braces_implements_no_space ===
class InputRecordFormattingBracesImplementsNoSpaceSliceViolation {
	record ImplementsNoSpace(int a) implements Foo{} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: braces_no_space_body ===
class InputRecordFormattingBracesNoSpaceBodySliceViolation {
	record NoSpaceBody(int a){ // violation: Record opening brace must have exactly one space before it.
		void m() {}
	}
}
// === end ===

// === case: braces_no_space_empty ===
class InputRecordFormattingBracesNoSpaceEmptySliceViolation {
	record NoSpaceEmpty(int a){} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: braces_non_empty_body_same_line ===
class InputRecordFormattingBracesNonEmptyBodySameLineSliceViolation {
	record NonEmptyBodySameLine(int a) { void m() {} } // violation: Non-empty record body must place '}' on its own line.
}
// === end ===

// === case: braces_tab_between_empty ===
class InputRecordFormattingBracesTabBetweenEmptySliceViolation {
	record TabBetweenEmpty(int a)	{} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: braces_two_spaces_empty ===
class InputRecordFormattingBracesTwoSpacesEmptySliceViolation {
	record TwoSpacesEmpty(int a)  {} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: components ===
class InputRecordFormattingComponentsViolation {
	record Mixed2(int a, // violation: First record component must not share the line with the opening paren.
			int b) {} // violation: Last record component must not share the line with the closing paren.

	record MixedGeneric<T>(T a, // violation: First record component must not share the line with the opening paren.
			T b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: components_all_on_one_line ===
class InputRecordFormattingComponentsAllOnOneLineSliceViolation {
	record AllOnMiddleLine(
			int a, int b, int c // violation: Each record component must be on its own line. // violation: Each record component must be on its own line.
	) {}
}
// === end ===

// === case: components_closing_shared_3 ===
class InputRecordFormattingComponentsClosingShared3SliceViolation {
	record ClosingShared3(
			int a,
			int b,
			int c) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: components_multi_per_line_middle ===
class InputRecordFormattingComponentsMultiPerLineMiddleSliceViolation {
	record MultiPerLineMiddle(
			int a,
			int b, int c, // violation: Each record component must be on its own line.
			int d
	) {}
}
// === end ===

// === case: components_opening_shared_3 ===
class InputRecordFormattingComponentsOpeningShared3SliceViolation {
	record OpeningShared3(int a, // violation: First record component must not share the line with the opening paren.
			int b,
			int c
	) {}
}
// === end ===

// === case: fix_brace_newline_empty ===
record InputRecordFormattingBracesNewlineEmptySliceViolation(int a)
{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
// === end ===

// === case: fix_brace_newline_with_body ===
class InputRecordFormattingFixBraceNewlineWithBodySliceViolation {
	record R(int a)
	{ // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
		void m() {}
	}
}
// === end ===

// === case: fix_component_bitshift_in_annotation ===
class InputRecordFormattingFixComponentBitshiftInAnnotationSliceViolation {
	@interface A { int value(); }

	record R(@A(1 << 4) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_block_comment_with_comma_in_annotation ===
class InputRecordFormattingFixComponentBlockCommentWithCommaInAnnotationSliceViolation {
	@interface A { int value(); }

	record R(@A(/* a, b */ 1) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_boundary_collapse_at_max ===
class InputRecordFormattingFixComponentBoundaryCollapseAtMaxSliceViolation {
	record Boundary120(int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa // violation: First record component must not share the line with the opening paren.
	) {}
}
// === end ===

// === case: fix_component_boundary_expand_over_max ===
class InputRecordFormattingFixComponentBoundaryExpandOverMaxSliceViolation {
	record Boundary121(int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa // violation: First record component must not share the line with the opening paren.
	) {}
}
// === end ===

// === case: fix_component_collapse_mixed_to_single_line ===
class InputRecordFormattingFixComponentCollapseMixedToSingleLineSliceViolation {
	record R(int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_comma_in_annotation_parens ===
class InputRecordFormattingFixComponentCommaInAnnotationParensSliceViolation {
	@interface A {
		int b() default 0;

		int c() default 0;
	}

	record R(@A(b = 1, c = 2) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_expand_wide_line_to_style_b ===
class InputRecordFormattingFixComponentExpandWideLineToStyleBSliceViolation {
	record WideRecord(int aaaaaaaa, // violation: First record component must not share the line with the opening paren.
			int bbbbbbbb, int cccccccc, int dddddddd, int eeeeeeee, int ffffffff, int gggggggg) {} // violation: Each record component must be on its own line. // violation: Each record component must be on its own line. // violation: Each record component must be on its own line. // violation: Each record component must be on its own line. // violation: Each record component must be on its own line. // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_greater_equal_in_annotation ===
class InputRecordFormattingFixComponentGreaterEqualInAnnotationSliceViolation {
	@interface A {
		int value();
	}

	record R(@A(X >= 1) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.

	static final int X = 0;
}
// === end ===

// === case: fix_component_greater_than_in_annotation_inside_generics ===
class InputRecordFormattingFixComponentGreaterThanInAnnotationInsideGenericsSliceViolation {
	@interface A { int value(); }

	record R(java.util.List<@A(5 > 0) Integer> m, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_less_than_in_annotation ===
class InputRecordFormattingFixComponentLessThanInAnnotationSliceViolation {
	@interface A { int value(); }

	record R(@A(1 < 5) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_middle_line_comment_in_header_skipped ===
// skip-reason: cannot reformat a record header that contains a line comment
class InputRecordFormattingFixComponentMiddleLineCommentInHeaderSliceViolation {
	record R(
		int a, // note
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_multi_per_line_collapses ===
class InputRecordFormattingFixComponentMultiPerLineCollapsesSliceViolation {
	record R(
			int a, int b, // violation: Each record component must be on its own line.
			int c
	) {}
}
// === end ===

// === case: fix_component_nested_generics ===
class InputRecordFormattingFixComponentNestedGenericsSliceViolation {
	record R(java.util.Map<String, java.util.List<Integer>> m, // violation: First record component must not share the line with the opening paren.
		int x) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_opening_line_comment_in_header_skipped ===
// skip-reason: cannot reformat a record header that contains a line comment
class InputRecordFormattingFixComponentOpeningLineCommentInHeaderSliceViolation {
	record R( // note
		int a,
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_right_shift_in_annotation ===
class InputRecordFormattingFixComponentRightShiftInAnnotationSliceViolation {
	@interface A { int value(); }

	record R(@A(64 >> 2) int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_text_block_in_annotation_spans_lines_skipped ===
// skip-reason: cannot reformat a record header that spans a multi-line comment or text block
class InputRecordFormattingFixComponentTextBlockInAnnotationSliceViolation {
	@interface A { String value(); }

	record R(
		@A("""
			x""") int a,
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_annotation_comparison_in_generic_bound ===
class InputRecordFormattingFixComponentWithAnnotationComparisonInGenericBoundSliceViolation {
	@interface A { int value(); }

	record R<T extends @A(1 < 2) Object>(T a, // violation: First record component must not share the line with the opening paren.
		T b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_annotation_greater_than_in_generic_bound ===
class InputRecordFormattingFixComponentWithAnnotationGreaterThanInGenericBoundSliceViolation {
	@interface A { int value(); }

	record R<T extends @A(5 > 0) Object>(T a, // violation: First record component must not share the line with the opening paren.
		T b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_block_comment_in_header ===
class InputRecordFormattingFixComponentWithBlockCommentInHeaderSliceViolation {
	record R(int a, // violation: First record component must not share the line with the opening paren.
	/* note */
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_bounded_generic ===
class InputRecordFormattingFixComponentWithBoundedGenericSliceViolation {
	record R<T extends java.util.Map<K, V>>(T a, // violation: First record component must not share the line with the opening paren.
		T b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_generic_type_arg ===
class InputRecordFormattingFixComponentWithGenericTypeArgSliceViolation {
	record R(java.util.Map<String, Integer> m, // violation: First record component must not share the line with the opening paren.
		int x) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_component_with_string_containing_record_on_prior_line ===
class InputRecordFormattingFixComponentWithStringContainingRecordOnPriorLineSliceViolation {
	String s = "record FAKE(int x)";

	record Real(int a, // violation: First record component must not share the line with the opening paren.
		int b) {} // violation: Last record component must not share the line with the closing paren.
}
// === end ===

// === case: fix_empty_body_braces_split ===
class InputRecordFormattingFixEmptyBodyBracesSplitSliceViolation {
	record R(int a) {
	} // violation: Empty record body must be '{}' on one line.
}
// === end ===

// === case: fix_empty_body_braces_split_open_line_has_block_comment ===
class InputRecordFormattingFixEmptyBodyBracesSplitOpenLineHasBlockCommentSliceViolation {
	record R(int a) /* { */ {
	} // violation: Empty record body must be '{}' on one line.
}
// === end ===

// === case: fix_empty_body_braces_split_open_line_has_line_comment ===
// skip-reason: cannot collapse empty record body without losing surrounding content
class InputRecordFormattingFixEmptyBodyBracesSplitOpenLineHasLineCommentSliceViolation {
	record R(int a) { // note
	} // violation: Empty record body must be '{}' on one line.
}
// === end ===

// === case: fix_empty_body_braces_split_trailing_content ===
// skip-reason: cannot collapse empty record body without losing surrounding content
class InputRecordFormattingFixEmptyBodyBracesSplitTrailingContentSliceViolation {
	record R(int a) {
	} // note // violation: Empty record body must be '{}' on one line.
}
// === end ===

// === case: fix_implements_multi_line ===
interface InputRecordFormattingFixImplementsMultiLineFoo {}

class InputRecordFormattingFixImplementsMultiLineSliceViolation {
	record R(int a) implements
			InputRecordFormattingFixImplementsMultiLineFoo
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: fix_implements_no_space ===
interface InputRecordFormattingFixImplementsNoSpaceFoo {}

class InputRecordFormattingFixImplementsNoSpaceSliceViolation {
	record R(int a) implements InputRecordFormattingFixImplementsNoSpaceFoo{} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: fix_no_space_before_brace ===
class InputRecordFormattingFixNoSpaceBeforeBraceSliceViolation {
	record R(int a){} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: fix_non_empty_body_block_comment_containing_brace ===
class InputRecordFormattingFixNonEmptyBodyBlockCommentContainingBraceSliceViolation {
	record R(int a) { /* } { */ int x = 1; } // violation: Non-empty record body must place '}' on its own line.
}
// === end ===

// === case: fix_non_empty_body_char_literal_containing_brace ===
class InputRecordFormattingFixNonEmptyBodyCharLiteralContainingBraceSliceViolation {
	record R(int a) { char c = '}'; } // violation: Non-empty record body must place '}' on its own line.
}
// === end ===

// === case: fix_non_empty_body_same_line ===
class InputRecordFormattingFixNonEmptyBodySameLineSliceViolation {
	record R(int a) { void m() {} } // violation: Non-empty record body must place '}' on its own line.
}
// === end ===

// === case: fix_non_empty_body_string_containing_brace ===
class InputRecordFormattingFixNonEmptyBodyStringContainingBraceSliceViolation {
	record R(int a) { String s = "{"; } // violation: Non-empty record body must place '}' on its own line.
}
// === end ===

// === case: fix_open_brace_after_line_comment ===
// skip-reason: anchor line ends in a comment or unterminated literal
class InputRecordFormattingFixOpenBraceAfterLineCommentSliceViolation {
	record R(int a) // note
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: fix_open_brace_after_terminated_block_comment ===
class InputRecordFormattingFixOpenBraceAfterTerminatedBlockCommentSliceViolation {
	record R(int a) /* note */
	{} // violation: Record opening brace must be on the same line as the closing paren (or implements clause).
}
// === end ===

// === case: fix_tab_before_brace ===
class InputRecordFormattingFixTabBeforeBraceSliceViolation {
	record R(int a)	{} // violation: Record opening brace must have exactly one space before it.
}
// === end ===

// === case: fix_two_spaces_before_brace ===
class InputRecordFormattingFixTwoSpacesBeforeBraceSliceViolation {
	record R(int a)  {} // violation: Record opening brace must have exactly one space before it.
}
// === end ===