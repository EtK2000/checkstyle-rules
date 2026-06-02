package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}
@interface V {
	String[] value();
}

// === case: annotation_with_char_literal ===
class InputAnnotationOwnLineAnnotationWithCharLiteralViolation {
	@A('x')
	@B
	void f() {}
}
// === end ===

// === case: annotation_with_nested_annotation ===
class InputAnnotationOwnLineAnnotationWithNestedAnnotationViolation {
	@A(@B)
	@C
	void f() {}
}
// === end ===

// === case: annotation_with_nested_parens ===
class InputAnnotationOwnLineAnnotationWithNestedParensViolation {
	@A(v = (1 + 2))
	@B
	void f() {}
}
// === end ===

// === case: annotation_with_string_params ===
class InputAnnotationOwnLineAnnotationWithStringParamsViolation {
	@Override
	@SuppressWarnings("unchecked")
	void f() {}
}
// === end ===

// === case: blank_line_after_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterBlockCommentBelowViolation {
	@A
	/* block */
	void f() {}
}
// === end ===

// === case: blank_line_after_javadoc_below ===
class InputAnnotationOwnLineBlankLineAfterJavadocBelowViolation {
	@A
	/** Javadoc. */
	void f() {}
}
// === end ===

// === case: blank_line_after_line_comment_below ===
class InputAnnotationOwnLineBlankLineAfterLineCommentBelowViolation {
	@A
	// comment
	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentBelowViolation {
	@A
	/*
	 * comment
	 */
	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_with_internal_blank_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentWithInternalBlankBelowViolation {
	@A
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
	@A
	/* block */
	void f() {}
}
// === end ===

// === case: blank_line_before_javadoc_below ===
class InputAnnotationOwnLineBlankLineBeforeJavadocBelowViolation {
	@A
	/** Javadoc. */
	void f() {}
}
// === end ===

// === case: blank_line_before_line_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeLineCommentBelowViolation {
	@A
	// comment
	void f() {}
}
// === end ===

// === case: blank_line_before_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeMultiLineBlockCommentBelowViolation {
	@A
	/*
	 * comment
	 */
	void f() {}
}
// === end ===

// === case: blank_line_below ===
class InputAnnotationOwnLineBlankLineBelowViolation {
	@A
	@B
	void f() {}
}
// === end ===

// === case: blank_line_below_multi_line_annotation ===
class InputAnnotationOwnLineBlankLineBelowMultiLineAnnotationViolation {
	@V({
		"a"
	})
	void f() {}
}
// === end ===

// === case: blank_line_inside_annotation ===
class InputAnnotationOwnLineBlankLineInsideAnnotationSliceViolation {
	@V({
		"a"
	})
	void f() {}
}
// === end ===

// === case: blank_line_inside_block_comment_no_blank_after ===
class InputAnnotationOwnLineBlankLineInsideBlockCommentNoBlankAfterViolation {
	@A
	@B
	/*

	 */
	void f() {}
}
// === end ===

// === case: blankline_blank_before_field ===
class InputAnnotationOwnLineBlanklineBlankBeforeFieldViolation {
	@A
	int f;
}
// === end ===

// === case: blankline_blank_before_method ===
class InputAnnotationOwnLineBlanklineBlankBeforeMethodViolation {
	@A
	void m() {}
}
// === end ===

// === case: blankline_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineBlankBetweenAnnotationsViolation {
	@A
	@B
	int f;
}
// === end ===

// === case: blankline_blank_line_after_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterBlockCommentViolation {
	@A
	/* block comment */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterJavadocViolation {
	@A
	/** Javadoc comment. */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterLineCommentViolation {
	@A
	// line comment
	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterMultiLineBlockCommentViolation {
	@A
	/*
	 * multi-line
	 * block comment
	 */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeBlockCommentViolation {
	@A
	/* block comment */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeJavadocViolation {
	@A
	/** Javadoc comment. */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeLineCommentViolation {
	@A
	// line comment
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeMultiLineBlockCommentViolation {
	@A
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
	})
	void m() {}
}
// === end ===

// === case: blankline_multi_line_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineMultiLineBlankBetweenAnnotationsViolation {
	@A
	@V({
		"b"
	})
	void m() {}
}
// === end ===

// === case: embedded_annotation_after_multiple_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterMultipleModifiersViolation {
	@A
	private final String field;
}
// === end ===

// === case: embedded_annotation_after_static_final ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterStaticFinalViolation {
	@A
	static final int CONST = 1;
}
// === end ===

// === case: embedded_annotation_between_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationBetweenModifiersViolation {
	@A
	private final int x = 1;
}
// === end ===

// === case: embedded_annotation_with_array_of_annotations ===
class InputAnnotationOwnLineEmbeddedAnnotationWithArrayOfAnnotationsViolation {
	void m() {
		@A({@B, @C})
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_block_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInBlockCommentViolation {
	void m() {
		@A
		final var x = /* @ignore */ 1;
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_char ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInCharViolation {
	void m() {
		@A
		final var x = '@';
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_line_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInLineCommentViolation {
	void m() {
		@A
		final var x = 1; // @ignore
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_string ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInStringViolation {
	void m() {
		@A
		final var x = "@email";
	}
}
// === end ===

// === case: embedded_annotation_with_deep_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithDeepNestedAnnotationViolation {
	void m() {
		@A(value = @B("test"))
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_escaped_quotes ===
class InputAnnotationOwnLineEmbeddedAnnotationWithEscapedQuotesViolation {
	void m() {
		@A("he said \"hi\"")
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedAnnotationViolation {
	void m() {
		@A(@B)
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_nested_parens ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedParensViolation {
	void m() {
		@A(v = (1 + 2))
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_tab_separator ===
class InputAnnotationOwnLineEmbeddedAnnotationWithTabSeparatorViolation {
	void m() {
		@A
		final var x = 1;
	}
}
// === end ===

// === case: embedded_annotation_with_value ===
class InputAnnotationOwnLineEmbeddedAnnotationWithValueViolation {
	void m() {
		@SuppressWarnings("unused")
		final var x = 1;
	}
}
// === end ===

// === case: embedded_multiple_annotations_after_final ===
class InputAnnotationOwnLineEmbeddedMultipleAnnotationsAfterFinalViolation {
	void m() {
		@A
		@B
		final var x = 1;
	}
}
// === end ===

// === case: embedded_qualified_annotation ===
class InputAnnotationOwnLineEmbeddedQualifiedAnnotationViolation {
	void m() {
		@javax.annotation.Nonnull
		final var x = "";
	}
}
// === end ===

// === case: embedded_single_annotation_after_final ===
class InputAnnotationOwnLineEmbeddedSingleAnnotationAfterFinalViolation {
	void m() {
		@A
		final var x = 1;
	}
}
// === end ===

// === case: escaped_quote_in_string_param ===
class InputAnnotationOwnLineEscapedQuoteInStringParamViolation {
	@A("he said \"hi\"")
	@B
	void f() {}
}
// === end ===

// === case: leading_and_embedded_annotations ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsViolation {
	void m() {
		@A
		@C
		final var x = 1;
	}
}
// === end ===

// === case: leading_and_embedded_annotations_with_multiple_modifiers ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsWithMultipleModifiersViolation {
	@A
	@B
	private final String x;
}
// === end ===

// === case: multiple_blank_lines_below ===
class InputAnnotationOwnLineMultipleBlankLinesBelowViolation {
	@A
	@B
	void f() {}
}
// === end ===

// === case: multiple_leading_and_embedded_annotations ===
class InputAnnotationOwnLineMultipleLeadingAndEmbeddedAnnotationsViolation {
	@interface D {}

	void m() {
		@A
		@B
		@C
		@D
		final var x = 1;
	}
}
// === end ===

// === case: multiple_positions_all_three ===
class InputAnnotationOwnLineMultiplePositionsAllThreeViolation {
	@A
	@B
	@C
	static final int v;
}
// === end ===

// === case: multiple_positions_between_and_after ===
class InputAnnotationOwnLineMultiplePositionsBetweenAndAfterViolation {
	@A
	@B
	static final int v;
}
// === end ===

// === case: multiple_positions_between_only ===
class InputAnnotationOwnLineMultiplePositionsBetweenOnlyViolation {
	@A
	static final int v;
}
// === end ===

// === case: multiple_positions_leading_and_after ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndAfterViolation {
	@A
	@B
	static final int v;
}
// === end ===

// === case: multiple_positions_leading_and_between ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndBetweenViolation {
	@A
	@B
	static final int v;
}
// === end ===

// === case: multiple_spaces_between_annotations ===
class InputAnnotationOwnLineMultipleSpacesBetweenAnnotationsViolation {
	@A
	@B
	void f() {}
}
// === end ===

// === case: order ===
class InputAnnotationOwnLineOrderViolation {
	@A
	@B
	@C
	void reverseOrder() {}
}
// === end ===

// === case: order_field ===
class InputAnnotationOwnLineOrderFieldViolation {
	@A
	@B
	int fieldOrder;
}
// === end ===

// === case: order_method_with_three ===
class InputAnnotationOwnLineOrderMethodWithThreeViolation {
	@A
	@B
	@C
	void methodOrder() {}
}
// === end ===

// === case: qualified_annotation ===
class InputAnnotationOwnLineQualifiedAnnotationViolation {
	@javax.annotation.Nonnull
	@Override
	void f() {}
}
// === end ===

// === case: reorder_block ===
class InputAnnotationOwnLineReorderBlockViolation {
	@A
	@B
	void f() {}
}
// === end ===

// === case: reorder_block_comment_below_no_blank ===
class InputAnnotationOwnLineReorderBlockCommentBelowNoBlankViolation {
	@A
	@B
	// comment
	void f() {}
}
// === end ===

// === case: reorder_three_annotations ===
class InputAnnotationOwnLineReorderThreeAnnotationsViolation {
	@A
	@B
	@C
	void f() {}
}
// === end ===

// === case: sameline_compact_constructor ===
class InputAnnotationOwnLineSamelineCompactConstructorViolation {
	record InlineCompact(int v) {
		@A
		InlineCompact {}
	}
}
// === end ===

// === case: sameline_embedded_field_after_final ===
class InputAnnotationOwnLineSamelineEmbeddedFieldAfterFinalViolation {
	@A
	static final int embeddedField = 1;
}
// === end ===

// === case: sameline_enum_constant ===
class InputAnnotationOwnLineSamelineEnumConstantViolation {
	enum Status {
		@A
		ACTIVE
	}
}
// === end ===

// === case: sameline_enum_constant_two_annotations ===
class InputAnnotationOwnLineSamelineEnumConstantTwoAnnotationsViolation {
	enum Status {
		@A
		@B
		INACTIVE
	}
}
// === end ===

// === case: sameline_inline_constructor ===
class InputAnnotationOwnLineSamelineInlineConstructorViolation {
	@A
	InputAnnotationOwnLineSamelineInlineConstructorViolation() {}
}
// === end ===

// === case: sameline_inline_enum ===
class InputAnnotationOwnLineSamelineInlineEnumViolation {
	@A
	enum InlineEnum {}
}
// === end ===

// === case: sameline_inline_field ===
class InputAnnotationOwnLineSamelineInlineFieldViolation {
	@A
	int inlineField;
}
// === end ===

// === case: sameline_inline_inner_annotation_method ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationMethodViolation {
	@interface InlineMeta {
		@A
		String value();
	}
}
// === end ===

// === case: sameline_inline_inner_annotation_type ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationTypeViolation {
	@A
	@interface
	InlineMeta {}
}
// === end ===

// === case: sameline_inline_inner_interface ===
class InputAnnotationOwnLineSamelineInlineInnerInterfaceViolation {
	@A
	interface InlineInner {}
}
// === end ===

// === case: sameline_inline_method ===
class InputAnnotationOwnLineSamelineInlineMethodViolation {
	@A
	void inlineMethod() {}
}
// === end ===

// === case: sameline_inline_record ===
class InputAnnotationOwnLineSamelineInlineRecordViolation {
	@A
	@B
	record InlineRec(int x) {}
}
// === end ===

// === case: sameline_local ===
class InputAnnotationOwnLineSamelineLocalViolation {
	void locals() {
		@A
		final var x = "test";
	}
}
// === end ===

// === case: sameline_local_after_final ===
class InputAnnotationOwnLineSamelineLocalAfterFinalViolation {
	void locals() {
		@A
		final var afterFinal = 1;
	}
}
// === end ===

// === case: sameline_local_after_final_two_annotations ===
class InputAnnotationOwnLineSamelineLocalAfterFinalTwoAnnotationsViolation {
	void locals() {
		@A
		@B
		final var afterFinalMultiple = 2;
	}
}
// === end ===

// === case: sameline_local_two_annotations ===
class InputAnnotationOwnLineSamelineLocalTwoAnnotationsViolation {
	void locals() {
		@A
		@B
		final var y = 42;
	}
}
// === end ===

// === case: sameline_three_annotation_field ===
class InputAnnotationOwnLineSamelineThreeAnnotationFieldViolation {
	@A
	@B
	@C
	int allInOne;
}
// === end ===

// === case: sameline_three_annotation_method ===
class InputAnnotationOwnLineSamelineThreeAnnotationMethodViolation {
	@A
	@B
	@C
	void threeAnnotationsMethod() {}
}
// === end ===

// === case: sameline_top_level_class ===
@A
@B
class InputAnnotationOwnLineSamelineTopLevelClassViolation {}
// === end ===

// === case: sameline_two_annotation_field ===
class InputAnnotationOwnLineSamelineTwoAnnotationFieldViolation {
	@A
	@B
	int twoAnnotationsField;
}
// === end ===

// === case: sameline_two_annotation_method ===
class InputAnnotationOwnLineSamelineTwoAnnotationMethodViolation {
	@A
	@B
	void twoAnnotationsMethod() {}
}
// === end ===

// === case: single_annotation_with_declaration ===
class InputAnnotationOwnLineSingleAnnotationWithDeclarationViolation {
	@A
	void f() {}
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationOwnLineSortsAlphabeticallyViolation {
	@A
	@B
	@C
	void f() {}
}
// === end ===

// === case: split_annotation_and_declaration ===
class InputAnnotationOwnLineSplitSliceViolation {
	@interface A {}

	@A
	void foo() {}
}
// === end ===

// === case: split_multiple_annotations_and_declaration ===
class InputAnnotationOwnLineSplitMultipleAnnotationsAndDeclarationViolation {
	@A
	@B
	void foo() {}
}
// === end ===

// === case: tab_between_leading_annotations ===
class InputAnnotationOwnLineTabBetweenLeadingAnnotationsViolation {
	@A
	@B
	void f() {}
}
// === end ===