package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}
@interface V {
	String[] value();
}

// === case: annotation_with_char_literal ===
class InputAnnotationOwnLineAnnotationWithCharLiteralViolation {
	@A('x') @B void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: annotation_with_nested_annotation ===
class InputAnnotationOwnLineAnnotationWithNestedAnnotationViolation {
	@A(@B) @C void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: annotation_with_nested_parens ===
class InputAnnotationOwnLineAnnotationWithNestedParensViolation {
	@A(v = (1 + 2)) @B void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: annotation_with_string_params ===
class InputAnnotationOwnLineAnnotationWithStringParamsViolation {
	@SuppressWarnings("unchecked") @Override void f() {} // violation: Annotation 'SuppressWarnings' must be on its own line. // violation: Annotation 'Override' must appear before 'SuppressWarnings' (alphabetical order).
}
// === end ===

// === case: blank_line_after_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterBlockCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.
	/* block */

	void f() {}
}
// === end ===

// === case: blank_line_after_javadoc_below ===
class InputAnnotationOwnLineBlankLineAfterJavadocBelowViolation {
	@A // violation: No blank line after annotation 'A'.
	/** Javadoc. */

	void f() {}
}
// === end ===

// === case: blank_line_after_line_comment_below ===
class InputAnnotationOwnLineBlankLineAfterLineCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.
	// comment

	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.
	/*
	 * comment
	 */

	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_with_internal_blank_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentWithInternalBlankBelowViolation {
	@A // violation: No blank line after annotation 'A'.
	/*
	 * comment
	 *
	 * more
	 */

	void f() {}
}
// === end ===

// === case: blank_line_before_block_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeBlockCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.

	/* block */
	void f() {}
}
// === end ===

// === case: blank_line_before_javadoc_below ===
class InputAnnotationOwnLineBlankLineBeforeJavadocBelowViolation {
	@A // violation: No blank line after annotation 'A'.

	/** Javadoc. */
	void f() {}
}
// === end ===

// === case: blank_line_before_line_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeLineCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.

	// comment
	void f() {}
}
// === end ===

// === case: blank_line_before_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeMultiLineBlockCommentBelowViolation {
	@A // violation: No blank line after annotation 'A'.

	/*
	 * comment
	 */
	void f() {}
}
// === end ===

// === case: blank_line_below ===
class InputAnnotationOwnLineBlankLineBelowViolation {
	@A // violation: No blank line after annotation 'A'.

	@B
	void f() {}
}
// === end ===

// === case: blank_line_below_multi_line_annotation ===
class InputAnnotationOwnLineBlankLineBelowMultiLineAnnotationViolation {
	@V({
		"a"
	}) // violation: No blank line after annotation 'V'.

	void f() {}
}
// === end ===

// === case: blank_line_inside_annotation ===
class InputAnnotationOwnLineBlankLineInsideAnnotationSliceViolation {
	@V({
// violation: No blank line inside annotation 'V'.
		"a"
	})
	void f() {}
}
// === end ===

// === case: blank_line_inside_block_comment_no_blank_after ===
class InputAnnotationOwnLineBlankLineInsideBlockCommentNoBlankAfterViolation {
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	/*

	 */
	void f() {}
}
// === end ===

// === case: blankline_blank_before_field ===
class InputAnnotationOwnLineBlanklineBlankBeforeFieldViolation {
	@A // violation: No blank line after annotation 'A'.

	int f;
}
// === end ===

// === case: blankline_blank_before_method ===
class InputAnnotationOwnLineBlanklineBlankBeforeMethodViolation {
	@A // violation: No blank line after annotation 'A'.

	void m() {}
}
// === end ===

// === case: blankline_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineBlankBetweenAnnotationsViolation {
	@A // violation: No blank line after annotation 'A'.

	@B
	int f;
}
// === end ===

// === case: blankline_blank_line_after_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterBlockCommentViolation {
	@A // violation: No blank line after annotation 'A'.
	/* block comment */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterJavadocViolation {
	@A // violation: No blank line after annotation 'A'.
	/** Javadoc comment. */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterLineCommentViolation {
	@A // violation: No blank line after annotation 'A'.
	// line comment

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterMultiLineBlockCommentViolation {
	@A // violation: No blank line after annotation 'A'.
	/*
	 * multi-line
	 * block comment
	 */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeBlockCommentViolation {
	@A // violation: No blank line after annotation 'A'.

	/* block comment */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeJavadocViolation {
	@A // violation: No blank line after annotation 'A'.

	/** Javadoc comment. */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeLineCommentViolation {
	@A // violation: No blank line after annotation 'A'.

	// line comment
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeMultiLineBlockCommentViolation {
	@A // violation: No blank line after annotation 'A'.

	/*
	 * multi-line
	 * block comment
	 */
	void m() {}
}
// === end ===

// === case: blankline_multi_line_blank_before_method ===
class InputAnnotationOwnLineBlanklineMultiLineBlankBeforeMethodViolation {
	@V({
		"a"
	}) // violation: No blank line after annotation 'V'.

	void m() {}
}
// === end ===

// === case: blankline_multi_line_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineMultiLineBlankBetweenAnnotationsViolation {
	@A // violation: No blank line after annotation 'A'.

	@V({
		"b"
	})
	void m() {}
}
// === end ===

// === case: embedded_annotation_after_multiple_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterMultipleModifiersViolation {
	private final @A String field; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_after_static_final ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterStaticFinalViolation {
	static final @A int CONST = 1; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_between_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationBetweenModifiersViolation {
	private @A final int x = 1; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_with_array_of_annotations ===
class InputAnnotationOwnLineEmbeddedAnnotationWithArrayOfAnnotationsViolation {
	void m() {
		final @A({@B, @C}) var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_block_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInBlockCommentViolation {
	void m() {
		final @A var x = /* @ignore */ 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_char ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInCharViolation {
	void m() {
		final @A var x = '@'; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_line_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInLineCommentViolation {
	void m() {
		final @A var x = 1; // @ignore // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_string ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInStringViolation {
	void m() {
		final @A var x = "@email"; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_deep_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithDeepNestedAnnotationViolation {
	void m() {
		final @A(value = @B("test")) var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_escaped_quotes ===
class InputAnnotationOwnLineEmbeddedAnnotationWithEscapedQuotesViolation {
	void m() {
		final @A("he said \"hi\"") var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedAnnotationViolation {
	void m() {
		final @A(@B) var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_nested_parens ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedParensViolation {
	void m() {
		final @A(v = (1 + 2)) var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_tab_separator ===
class InputAnnotationOwnLineEmbeddedAnnotationWithTabSeparatorViolation {
	void m() {
		final	@A var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_value ===
class InputAnnotationOwnLineEmbeddedAnnotationWithValueViolation {
	void m() {
		final @SuppressWarnings("unused") var x = 1; // violation: Annotation 'SuppressWarnings' must be on its own line.
	}
}
// === end ===

// === case: embedded_multiple_annotations_after_final ===
class InputAnnotationOwnLineEmbeddedMultipleAnnotationsAfterFinalViolation {
	void m() {
		final @A @B var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: embedded_qualified_annotation ===
class InputAnnotationOwnLineEmbeddedQualifiedAnnotationViolation {
	void m() {
		final @javax.annotation.Nonnull var x = ""; // violation: Annotation 'Nonnull' must be on its own line.
	}
}
// === end ===

// === case: embedded_single_annotation_after_final ===
class InputAnnotationOwnLineEmbeddedSingleAnnotationAfterFinalViolation {
	void m() {
		final @A var x = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: escaped_quote_in_string_param ===
class InputAnnotationOwnLineEscapedQuoteInStringParamViolation {
	@A("he said \"hi\"") @B void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: leading_and_embedded_annotations ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsViolation {
	void m() {
		@C final @A var x = 1; // violation: Annotation 'C' must be on its own line. // violation: Annotation 'A' must appear before 'C' (alphabetical order).
	}
}
// === end ===

// === case: leading_and_embedded_annotations_with_multiple_modifiers ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsWithMultipleModifiersViolation {
	@B private final @A String x; // violation: Annotation 'B' must be on its own line. // violation: Annotation 'A' must appear before 'B' (alphabetical order).
}
// === end ===

// === case: multiple_blank_lines_below ===
class InputAnnotationOwnLineMultipleBlankLinesBelowViolation {
	@A // violation: No blank line after annotation 'A'.


	@B
	void f() {}
}
// === end ===

// === case: multiple_leading_and_embedded_annotations ===
class InputAnnotationOwnLineMultipleLeadingAndEmbeddedAnnotationsViolation {
	@interface D {}

	void m() {
		@D @C final @B @A var x = 1; // violation: Annotation 'D' must be on its own line. // violation: Annotation 'C' must appear before 'D' (alphabetical order). // violation: Annotation 'B' must appear before 'C' (alphabetical order). // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	}
}
// === end ===

// === case: multiple_positions_all_three ===
class InputAnnotationOwnLineMultiplePositionsAllThreeViolation {
	@A static @B final @C int v; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: multiple_positions_between_and_after ===
class InputAnnotationOwnLineMultiplePositionsBetweenAndAfterViolation {
	static @A final @B int v; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: multiple_positions_between_only ===
class InputAnnotationOwnLineMultiplePositionsBetweenOnlyViolation {
	static @A final int v; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: multiple_positions_leading_and_after ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndAfterViolation {
	@A static final @B int v; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: multiple_positions_leading_and_between ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndBetweenViolation {
	@A static @B final int v; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: multiple_spaces_between_annotations ===
class InputAnnotationOwnLineMultipleSpacesBetweenAnnotationsViolation {
	@A    @B void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: order ===
class InputAnnotationOwnLineOrderViolation {
	@C
	@B // violation: Annotation 'B' must appear before 'C' (alphabetical order).
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	void reverseOrder() {}
}
// === end ===

// === case: order_field ===
class InputAnnotationOwnLineOrderFieldViolation {
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	int fieldOrder;
}
// === end ===

// === case: order_method_with_three ===
class InputAnnotationOwnLineOrderMethodWithThreeViolation {
	@C
	@A // violation: Annotation 'A' must appear before 'C' (alphabetical order).
	@B
	void methodOrder() {}
}
// === end ===

// === case: qualified_annotation ===
class InputAnnotationOwnLineQualifiedAnnotationViolation {
	@javax.annotation.Nonnull @Override void f() {} // violation: Annotation 'Nonnull' must be on its own line.
}
// === end ===

// === case: reorder_block ===
class InputAnnotationOwnLineReorderBlockViolation {
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: reorder_block_comment_below_no_blank ===
class InputAnnotationOwnLineReorderBlockCommentBelowNoBlankViolation {
	@B
	@A // violation: Annotation 'A' must appear before 'B' (alphabetical order).
	// comment
	void f() {}
}
// === end ===

// === case: reorder_three_annotations ===
class InputAnnotationOwnLineReorderThreeAnnotationsViolation {
	@C
	@A // violation: Annotation 'A' must appear before 'C' (alphabetical order).
	@B
	void f() {}
}
// === end ===

// === case: sameline_compact_constructor ===
class InputAnnotationOwnLineSamelineCompactConstructorViolation {
	record InlineCompact(int v) {
		@A InlineCompact {} // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_embedded_field_after_final ===
class InputAnnotationOwnLineSamelineEmbeddedFieldAfterFinalViolation {
	static final @A int embeddedField = 1; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_enum_constant ===
class InputAnnotationOwnLineSamelineEnumConstantViolation {
	enum Status {
		@A ACTIVE // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_enum_constant_two_annotations ===
class InputAnnotationOwnLineSamelineEnumConstantTwoAnnotationsViolation {
	enum Status {
		@A @B INACTIVE // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_inline_constructor ===
class InputAnnotationOwnLineSamelineInlineConstructorViolation {
	@A InputAnnotationOwnLineSamelineInlineConstructorViolation() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_enum ===
class InputAnnotationOwnLineSamelineInlineEnumViolation {
	@A enum InlineEnum {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_field ===
class InputAnnotationOwnLineSamelineInlineFieldViolation {
	@A int inlineField; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_inner_annotation_method ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationMethodViolation {
	@interface InlineMeta {
		@A String value(); // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_inline_inner_annotation_type ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationTypeViolation {
	@A @interface InlineMeta {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_inner_interface ===
class InputAnnotationOwnLineSamelineInlineInnerInterfaceViolation {
	@A interface InlineInner {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_method ===
class InputAnnotationOwnLineSamelineInlineMethodViolation {
	@A void inlineMethod() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_inline_record ===
class InputAnnotationOwnLineSamelineInlineRecordViolation {
	@A @B record InlineRec(int x) {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_local ===
class InputAnnotationOwnLineSamelineLocalViolation {
	void locals() {
		@A final var x = "test"; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_after_final ===
class InputAnnotationOwnLineSamelineLocalAfterFinalViolation {
	void locals() {
		final @A var afterFinal = 1; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_after_final_two_annotations ===
class InputAnnotationOwnLineSamelineLocalAfterFinalTwoAnnotationsViolation {
	void locals() {
		final @A @B var afterFinalMultiple = 2; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_two_annotations ===
class InputAnnotationOwnLineSamelineLocalTwoAnnotationsViolation {
	void locals() {
		@A @B final var y = 42; // violation: Annotation 'A' must be on its own line.
	}
}
// === end ===

// === case: sameline_three_annotation_field ===
class InputAnnotationOwnLineSamelineThreeAnnotationFieldViolation {
	@A @B @C int allInOne; // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sameline_three_annotation_method ===
class InputAnnotationOwnLineSamelineThreeAnnotationMethodViolation {
	@A @B @C // violation: Annotation 'A' must be on its own line.
	void threeAnnotationsMethod() {}
}
// === end ===

// === case: sameline_top_level_class ===
@A @B // violation: Annotation 'A' must be on its own line.
class InputAnnotationOwnLineSamelineTopLevelClassViolation {}
// === end ===

// === case: sameline_two_annotation_field ===
class InputAnnotationOwnLineSamelineTwoAnnotationFieldViolation {
	@A @B // violation: Annotation 'A' must be on its own line.
	int twoAnnotationsField;
}
// === end ===

// === case: sameline_two_annotation_method ===
class InputAnnotationOwnLineSamelineTwoAnnotationMethodViolation {
	@A @B // violation: Annotation 'A' must be on its own line.
	void twoAnnotationsMethod() {}
}
// === end ===

// === case: single_annotation_with_declaration ===
class InputAnnotationOwnLineSingleAnnotationWithDeclarationViolation {
	@A void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationOwnLineSortsAlphabeticallyViolation {
	@C @A @B void f() {} // violation: Annotation 'C' must be on its own line. // violation: Annotation 'A' must appear before 'C' (alphabetical order).
}
// === end ===

// === case: split_annotation_and_declaration ===
class InputAnnotationOwnLineSplitSliceViolation {
	@interface A {}

	@A void foo() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: split_multiple_annotations_and_declaration ===
class InputAnnotationOwnLineSplitMultipleAnnotationsAndDeclarationViolation {
	@A @B void foo() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===

// === case: tab_between_leading_annotations ===
class InputAnnotationOwnLineTabBetweenLeadingAnnotationsViolation {
	@A	@B void f() {} // violation: Annotation 'A' must be on its own line.
}
// === end ===