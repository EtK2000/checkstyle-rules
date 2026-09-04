package com.etk2000.checkstyle.inputs.annotationownline;

@interface A {}
@interface B {}
@interface C {}
@interface V {
	String[] value();
}
@interface Ä {}

// === case: annotation_with_char_literal ===
class InputAnnotationOwnLineAnnotationWithCharLiteralViolation {
	@A('x') @B void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: annotation_with_nested_annotation ===
class InputAnnotationOwnLineAnnotationWithNestedAnnotationViolation {
	@A(@B) @C void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: annotation_with_nested_parens ===
class InputAnnotationOwnLineAnnotationWithNestedParensViolation {
	@A(v = (1 + 2)) @B void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: annotation_with_string_params ===
class InputAnnotationOwnLineAnnotationWithStringParamsViolation {
	@SuppressWarnings("unchecked") @Override void f() {} // violation: Annotation '@SuppressWarnings' must be on its own line. // violation: Annotation '@Override' must appear before '@SuppressWarnings' (alphabetical order).
}
// === end ===

// === case: blank_after_column_past_last_line_end ===
class InputAnnotationOwnLineBlankAfterColumnPastLastLineEndViolation {
				@V(
"a") // violation: Annotation '@V' must not be followed by a blank line.

	void f() {}
}
// === end ===

// === case: blank_after_nested_annotation_decoy ===
class InputAnnotationOwnLineBlankAfterNestedAnnotationDecoyViolation {
	@V(
	@A) // violation: Annotation '@V' must not be followed by a blank line.

	void f() {}
}
// === end ===

// === case: blank_line_after_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterBlockCommentBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/* block */

	void f() {}
}
// === end ===

// === case: blank_line_after_javadoc_below ===
class InputAnnotationOwnLineBlankLineAfterJavadocBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/** Javadoc. */

	void f() {}
}
// === end ===

// === case: blank_line_after_line_comment_below ===
class InputAnnotationOwnLineBlankLineAfterLineCommentBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	// comment

	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/*
	 * comment
	 */

	void f() {}
}
// === end ===

// === case: blank_line_after_multi_line_block_comment_with_internal_blank_below ===
class InputAnnotationOwnLineBlankLineAfterMultiLineBlockCommentWithInternalBlankBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
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
	@A // violation: Annotation '@A' must not be followed by a blank line.

	/* block */
	void f() {}
}
// === end ===

// === case: blank_line_before_javadoc_below ===
class InputAnnotationOwnLineBlankLineBeforeJavadocBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	/** Javadoc. */
	void f() {}
}
// === end ===

// === case: blank_line_before_line_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeLineCommentBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	// comment
	void f() {}
}
// === end ===

// === case: blank_line_before_multi_line_block_comment_below ===
class InputAnnotationOwnLineBlankLineBeforeMultiLineBlockCommentBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	/*
	 * comment
	 */
	void f() {}
}
// === end ===

// === case: blank_line_below ===
class InputAnnotationOwnLineBlankLineBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	@B
	void f() {}
}
// === end ===

// === case: blank_line_below_multi_line_annotation ===
class InputAnnotationOwnLineBlankLineBelowMultiLineAnnotationViolation {
	@V({
		"a"
	}) // violation: Annotation '@V' must not be followed by a blank line.

	void f() {}
}
// === end ===

// === case: blank_line_inside_annotation ===
class InputAnnotationOwnLineBlankLineInsideAnnotationSliceViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	void f() {}
}
// === end ===

// === case: blank_line_inside_block_comment_no_blank_after ===
class InputAnnotationOwnLineBlankLineInsideBlockCommentNoBlankAfterViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	/*

	 */
	void f() {}
}
// === end ===

// === case: blankline_blank_before_annotation_field ===
@interface InputAnnotationOwnLineBlanklineBlankBeforeAnnotationFieldViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	int priority();
}
// === end ===

// === case: blankline_blank_before_annotation_type ===
class InputAnnotationOwnLineBlanklineBlankBeforeAnnotationTypeViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	@interface Meta {}
}
// === end ===

// === case: blankline_blank_before_class ===
class InputAnnotationOwnLineBlanklineBlankBeforeClassViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	static class Inner {}
}
// === end ===

// === case: blankline_blank_before_compact_ctor ===
record InputAnnotationOwnLineBlanklineBlankBeforeCompactCtorViolation(int value) {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	InputAnnotationOwnLineBlanklineBlankBeforeCompactCtorViolation {}
}
// === end ===

// === case: blankline_blank_before_ctor ===
class InputAnnotationOwnLineBlanklineBlankBeforeCtorViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	InputAnnotationOwnLineBlanklineBlankBeforeCtorViolation() {}
}
// === end ===

// === case: blankline_blank_before_enum ===
class InputAnnotationOwnLineBlanklineBlankBeforeEnumViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	enum Inner {
		X
	}
}
// === end ===

// === case: blankline_blank_before_enum_constant ===
enum InputAnnotationOwnLineBlanklineBlankBeforeEnumConstantViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	RED
}
// === end ===

// === case: blankline_blank_before_field ===
class InputAnnotationOwnLineBlanklineBlankBeforeFieldViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	int f;
}
// === end ===

// === case: blankline_blank_before_interface ===
class InputAnnotationOwnLineBlanklineBlankBeforeInterfaceViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	interface Inner {}
}
// === end ===

// === case: blankline_blank_before_local ===
class InputAnnotationOwnLineBlanklineBlankBeforeLocalViolation {
	void f() {
		@A // violation: Annotation '@A' must not be followed by a blank line.

		final var local = 1;
		assert local > 0;
	}
}
// === end ===

// === case: blankline_blank_before_method ===
class InputAnnotationOwnLineBlanklineBlankBeforeMethodViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	void m() {}
}
// === end ===

// === case: blankline_blank_before_record ===
class InputAnnotationOwnLineBlanklineBlankBeforeRecordViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	record Inner(int x) {}
}
// === end ===

// === case: blankline_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineBlankBetweenAnnotationsViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	@B
	int f;
}
// === end ===

// === case: blankline_blank_line_after_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterBlockCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/* block comment */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterJavadocViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/** Javadoc comment. */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterLineCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	// line comment

	void m() {}
}
// === end ===

// === case: blankline_blank_line_after_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineAfterMultiLineBlockCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.
	/*
	 * multi-line
	 * block comment
	 */

	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeBlockCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	/* block comment */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_javadoc_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeJavadocViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	/** Javadoc comment. */
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_line_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeLineCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	// line comment
	void m() {}
}
// === end ===

// === case: blankline_blank_line_before_multi_line_block_comment_between_annotation_and_decl ===
class InputAnnotationOwnLineBlanklineBeforeMultiLineBlockCommentViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

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
	}) // violation: Annotation '@V' must not be followed by a blank line.

	void m() {}
}
// === end ===

// === case: blankline_multi_line_blank_between_annotations ===
class InputAnnotationOwnLineBlanklineMultiLineBlankBetweenAnnotationsViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.

	@V({
		"b"
	})
	void m() {}
}
// === end ===

// === case: embedded_annotation_after_multiple_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterMultipleModifiersViolation {
	private final @A String field; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_after_static_final ===
class InputAnnotationOwnLineEmbeddedAnnotationAfterStaticFinalViolation {
	static final @A int CONST = 1; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_between_modifiers ===
class InputAnnotationOwnLineEmbeddedAnnotationBetweenModifiersViolation {
	private @A final int x = 1; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: embedded_annotation_with_array_of_annotations ===
class InputAnnotationOwnLineEmbeddedAnnotationWithArrayOfAnnotationsViolation {
	void m() {
		final @A({@B, @C}) var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_block_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInBlockCommentViolation {
	void m() {
		final @A var x = /* @ignore */ 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_char ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInCharViolation {
	void m() {
		final @A var x = '@'; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_line_comment ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInLineCommentViolation {
	void m() {
		final @A var x = 1; // @ignore // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_at_in_string ===
class InputAnnotationOwnLineEmbeddedAnnotationWithAtInStringViolation {
	void m() {
		final @A var x = "@email"; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_deep_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithDeepNestedAnnotationViolation {
	void m() {
		final @A(value = @B("test")) var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_escaped_quotes ===
class InputAnnotationOwnLineEmbeddedAnnotationWithEscapedQuotesViolation {
	void m() {
		final @A("he said \"hi\"") var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_nested_annotation ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedAnnotationViolation {
	void m() {
		final @A(@B) var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_nested_parens ===
class InputAnnotationOwnLineEmbeddedAnnotationWithNestedParensViolation {
	void m() {
		final @A(v = (1 + 2)) var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_tab_separator ===
class InputAnnotationOwnLineEmbeddedAnnotationWithTabSeparatorViolation {
	void m() {
		final	@A var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_annotation_with_value ===
class InputAnnotationOwnLineEmbeddedAnnotationWithValueViolation {
	void m() {
		final @SuppressWarnings("unused") var x = 1; // violation: Annotation '@SuppressWarnings' must be on its own line.
	}
}
// === end ===

// === case: embedded_multiple_annotations_after_final ===
class InputAnnotationOwnLineEmbeddedMultipleAnnotationsAfterFinalViolation {
	void m() {
		final @A @B var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: embedded_qualified_annotation ===
class InputAnnotationOwnLineEmbeddedQualifiedAnnotationViolation {
	void m() {
		final @javax.annotation.Nonnull var x = ""; // violation: Annotation '@Nonnull' must be on its own line.
	}
}
// === end ===

// === case: embedded_single_annotation_after_final ===
class InputAnnotationOwnLineEmbeddedSingleAnnotationAfterFinalViolation {
	void m() {
		final @A var x = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: escaped_quote_in_string_param ===
class InputAnnotationOwnLineEscapedQuoteInStringParamViolation {
	@A("he said \"hi\"") @B void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: internal_blank_annotation_field ===
@interface InputAnnotationOwnLineInternalBlankAnnotationFieldViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	int priority();
}
// === end ===

// === case: internal_blank_annotation_type ===
class InputAnnotationOwnLineInternalBlankAnnotationTypeViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	@interface Meta {}
}
// === end ===

// === case: internal_blank_before_text_block ===
class InputAnnotationOwnLineInternalBlankBeforeTextBlockViolation {
	@V(
// violation: Annotation '@V' must not contain a blank line.
			"""
			a""")
	void f() {}
}
// === end ===

// === case: internal_blank_class ===
class InputAnnotationOwnLineInternalBlankClassViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	static class Inner {}
}
// === end ===

// === case: internal_blank_compact_ctor ===
record InputAnnotationOwnLineInternalBlankCompactCtorViolation(int value) {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	InputAnnotationOwnLineInternalBlankCompactCtorViolation {}
}
// === end ===

// === case: internal_blank_ctor ===
class InputAnnotationOwnLineInternalBlankCtorViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	InputAnnotationOwnLineInternalBlankCtorViolation() {}
}
// === end ===

// === case: internal_blank_enum ===
class InputAnnotationOwnLineInternalBlankEnumViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	enum Inner {
		X
	}
}
// === end ===

// === case: internal_blank_enum_constant ===
enum InputAnnotationOwnLineInternalBlankEnumConstantViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	RED
}
// === end ===

// === case: internal_blank_field ===
class InputAnnotationOwnLineInternalBlankFieldViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	int field;
}
// === end ===

// === case: internal_blank_inside_block_comment ===
class InputAnnotationOwnLineInternalBlankInsideBlockCommentViolation {
	@V({ /* x
// violation: Annotation '@V' must not contain a blank line.
			y */ "a"
	})
	void f() {}
}
// === end ===

// === case: internal_blank_interface ===
class InputAnnotationOwnLineInternalBlankInterfaceViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	interface Inner {}
}
// === end ===

// === case: internal_blank_local ===
class InputAnnotationOwnLineInternalBlankLocalViolation {
	void f() {
		@V({
// violation: Annotation '@V' must not contain a blank line.
			"a"
		})
		final var local = 1;
		assert local > 0;
	}
}
// === end ===

// === case: internal_blank_record ===
class InputAnnotationOwnLineInternalBlankRecordViolation {
	@V({
// violation: Annotation '@V' must not contain a blank line.
		"a"
	})
	record Inner(int x) {}
}
// === end ===

// === case: leading_and_embedded_annotations ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsViolation {
	void m() {
		@C final @A var x = 1; // violation: Annotation '@C' must be on its own line. // violation: Annotation '@A' must appear before '@C' (alphabetical order).
	}
}
// === end ===

// === case: leading_and_embedded_annotations_with_multiple_modifiers ===
class InputAnnotationOwnLineLeadingAndEmbeddedAnnotationsWithMultipleModifiersViolation {
	@B private final @A String x; // violation: Annotation '@B' must be on its own line. // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: multiple_blank_lines_below ===
class InputAnnotationOwnLineMultipleBlankLinesBelowViolation {
	@A // violation: Annotation '@A' must not be followed by a blank line.


	@B
	void f() {}
}
// === end ===

// === case: multiple_leading_and_embedded_annotations ===
class InputAnnotationOwnLineMultipleLeadingAndEmbeddedAnnotationsViolation {
	@interface D {}

	void m() {
		@D @C final @B @A var x = 1; // violation: Annotation '@D' must be on its own line. // violation: Annotation '@C' must appear before '@D' (alphabetical order). // violation: Annotation '@B' must appear before '@C' (alphabetical order). // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	}
}
// === end ===

// === case: multiple_positions_all_three ===
class InputAnnotationOwnLineMultiplePositionsAllThreeViolation {
	@A static @B final @C int v; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: multiple_positions_between_and_after ===
class InputAnnotationOwnLineMultiplePositionsBetweenAndAfterViolation {
	static @A final @B int v; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: multiple_positions_between_only ===
class InputAnnotationOwnLineMultiplePositionsBetweenOnlyViolation {
	static @A final int v; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: multiple_positions_leading_and_after ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndAfterViolation {
	@A static final @B int v; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: multiple_positions_leading_and_between ===
class InputAnnotationOwnLineMultiplePositionsLeadingAndBetweenViolation {
	@A static @B final int v; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: multiple_spaces_between_annotations ===
class InputAnnotationOwnLineMultipleSpacesBetweenAnnotationsViolation {
	@A    @B void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: order ===
// multi-fix-expected
class InputAnnotationOwnLineOrderViolation {
	@C
	@B // violation: Annotation '@B' must appear before '@C' (alphabetical order).
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void reverseOrder() {}
}
// === end ===

// === case: order_annotation_field ===
@interface InputAnnotationOwnLineOrderAnnotationFieldViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	int priority();
}
// === end ===

// === case: order_annotation_type ===
class InputAnnotationOwnLineOrderAnnotationTypeViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	@interface Meta {}
}
// === end ===

// === case: order_argument_bearing ===
class InputAnnotationOwnLineOrderArgumentBearingViolation {
	@B
	@A({"x"}) // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_class ===
class InputAnnotationOwnLineOrderClassViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	static class Inner {}
}
// === end ===

// === case: order_comment_line_between_annotations ===
class InputAnnotationOwnLineOrderCommentLineBetweenAnnotationsViolation {
	@B
	/* why A */
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_compact_ctor ===
record InputAnnotationOwnLineOrderCompactCtorViolation(int value) {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	InputAnnotationOwnLineOrderCompactCtorViolation {}
}
// === end ===

// === case: order_ctor ===
class InputAnnotationOwnLineOrderCtorViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	InputAnnotationOwnLineOrderCtorViolation() {}
}
// === end ===

// === case: order_enum ===
class InputAnnotationOwnLineOrderEnumViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	enum Inner {
		X
	}
}
// === end ===

// === case: order_enum_constant ===
enum InputAnnotationOwnLineOrderEnumConstantViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	RED
}
// === end ===

// === case: order_field ===
class InputAnnotationOwnLineOrderFieldViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	int fieldOrder;
}
// === end ===

// === case: order_interface ===
class InputAnnotationOwnLineOrderInterfaceViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	interface Inner {}
}
// === end ===

// === case: order_local ===
class InputAnnotationOwnLineOrderLocalViolation {
	void f() {
		@B
		@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
		final var local = 1;
		assert local > 0;
	}
}
// === end ===

// === case: order_method_with_three ===
class InputAnnotationOwnLineOrderMethodWithThreeViolation {
	@C
	@A // violation: Annotation '@A' must appear before '@C' (alphabetical order).
	@B
	void methodOrder() {}
}
// === end ===

// === case: order_multi_declarator ===
class InputAnnotationOwnLineOrderMultiDeclaratorViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	int alpha, beta;
}
// === end ===

// === case: order_multi_line_annotation_with_comment ===
class InputAnnotationOwnLineOrderMultiLineAnnotationWithCommentViolation {
	@V({
			/* why */ "a"})
	@B // violation: Annotation '@B' must appear before '@V' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_non_ascii_name ===
class InputAnnotationOwnLineOrderNonAsciiNameViolation {
	@Ä
	@B // violation: Annotation '@B' must appear before '@Ä' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_record ===
class InputAnnotationOwnLineOrderRecordViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	record Inner(int x) {}
}
// === end ===

// === case: order_space_after_at ===
class InputAnnotationOwnLineOrderSpaceAfterAtViolation {
	@B
	@ A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_split_argument ===
class InputAnnotationOwnLineOrderSplitArgumentViolation {
	@B
	@A({ // violation: Annotation '@A' must appear before '@B' (alphabetical order).
			"x"})
	void f() {}
}
// === end ===

// === case: order_split_qualified_name ===
class InputAnnotationOwnLineOrderSplitQualifiedNameViolation {
	@java.lang
			.Deprecated
	@B // violation: Annotation '@B' must appear before '@Deprecated' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_text_block_argument ===
class InputAnnotationOwnLineOrderTextBlockArgumentViolation {
	@V("""
			a""")
	@B // violation: Annotation '@B' must appear before '@V' (alphabetical order).
	void f() {}
}
// === end ===

// === case: order_trailing_line_comment ===
class InputAnnotationOwnLineOrderTrailingLineCommentViolation {
	@B // why B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: own_line_and_order_split_argument ===
class InputAnnotationOwnLineOwnLineAndOrderSplitArgumentViolation {
	@C @A({ // violation: Annotation '@C' must be on its own line. // violation: Annotation '@A' must appear before '@C' (alphabetical order).
			"x"})
	void f() {}
}
// === end ===

// === case: own_line_multi_declarator ===
class InputAnnotationOwnLineOwnLineMultiDeclaratorViolation {
	@A @B int alpha, beta; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: own_line_split_qualified_name ===
class InputAnnotationOwnLineOwnLineSplitQualifiedNameViolation {
	@A java.util // violation: Annotation '@A' must be on its own line.
			.List<String> field;
}
// === end ===

// === case: qualified_annotation ===
class InputAnnotationOwnLineQualifiedAnnotationViolation {
	@javax.annotation.Nonnull @Override void f() {} // violation: Annotation '@Nonnull' must be on its own line.
}
// === end ===

// === case: reorder_block ===
class InputAnnotationOwnLineReorderBlockViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: reorder_block_comment_below_no_blank ===
class InputAnnotationOwnLineReorderBlockCommentBelowNoBlankViolation {
	@B
	@A // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	// comment
	void f() {}
}
// === end ===

// === case: reorder_three_annotations ===
class InputAnnotationOwnLineReorderThreeAnnotationsViolation {
	@C
	@A // violation: Annotation '@A' must appear before '@C' (alphabetical order).
	@B
	void f() {}
}
// === end ===

// === case: sameline_compact_constructor ===
class InputAnnotationOwnLineSamelineCompactConstructorViolation {
	record InlineCompact(int v) {
		@A InlineCompact {} // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_embedded_field_after_final ===
class InputAnnotationOwnLineSamelineEmbeddedFieldAfterFinalViolation {
	static final @A int embeddedField = 1; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_enum_constant ===
class InputAnnotationOwnLineSamelineEnumConstantViolation {
	enum Status {
		@A ACTIVE // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_enum_constant_two_annotations ===
class InputAnnotationOwnLineSamelineEnumConstantTwoAnnotationsViolation {
	enum Status {
		@A @B INACTIVE // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_group_after_block_comment_tail ===
class InputAnnotationOwnLineSamelineGroupAfterBlockCommentTailViolation {
	/* note
	can't */ @B @A void f() {} // violation: Annotation '@B' must be on its own line. // violation: Annotation '@A' must appear before '@B' (alphabetical order).
}
// === end ===

// === case: sameline_inline_constructor ===
class InputAnnotationOwnLineSamelineInlineConstructorViolation {
	@A InputAnnotationOwnLineSamelineInlineConstructorViolation() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_enum ===
class InputAnnotationOwnLineSamelineInlineEnumViolation {
	@A enum InlineEnum {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_field ===
class InputAnnotationOwnLineSamelineInlineFieldViolation {
	@A int inlineField; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_inner_annotation_method ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationMethodViolation {
	@interface InlineMeta {
		@A String value(); // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_inline_inner_annotation_type ===
class InputAnnotationOwnLineSamelineInlineInnerAnnotationTypeViolation {
	@A @interface InlineMeta {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_inner_interface ===
class InputAnnotationOwnLineSamelineInlineInnerInterfaceViolation {
	@A interface InlineInner {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_method ===
class InputAnnotationOwnLineSamelineInlineMethodViolation {
	@A void inlineMethod() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_inline_record ===
class InputAnnotationOwnLineSamelineInlineRecordViolation {
	@A @B record InlineRec(int x) {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_interior_opener_closed_before_declaration ===
class InputAnnotationOwnLineSamelineInteriorOpenerClosedBeforeDeclarationViolation {
	@B public // violation: Annotation '@B' must be on its own line.
	/* TODO: make this
	package-private */ void f() {}
}
// === end ===

// === case: sameline_interior_opener_closed_before_modifier ===
class InputAnnotationOwnLineSamelineInteriorOpenerClosedBeforeModifierViolation {
	@B public // violation: Annotation '@B' must be on its own line.
	/* note
	*/ static void f() {}
}
// === end ===

// === case: sameline_line_comment_on_modifier ===
class InputAnnotationOwnLineSamelineLineCommentOnModifierViolation {
	@B @A public static // TODO: narrow // violation: Annotation '@B' must be on its own line. // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	void f() {}
}
// === end ===

// === case: sameline_local ===
class InputAnnotationOwnLineSamelineLocalViolation {
	void locals() {
		@A final var x = "test"; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_after_final ===
class InputAnnotationOwnLineSamelineLocalAfterFinalViolation {
	void locals() {
		final @A var afterFinal = 1; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_after_final_two_annotations ===
class InputAnnotationOwnLineSamelineLocalAfterFinalTwoAnnotationsViolation {
	void locals() {
		final @A @B var afterFinalMultiple = 2; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_local_two_annotations ===
class InputAnnotationOwnLineSamelineLocalTwoAnnotationsViolation {
	void locals() {
		@A @B final var y = 42; // violation: Annotation '@A' must be on its own line.
	}
}
// === end ===

// === case: sameline_opened_gap_leading_fragment ===
class InputAnnotationOwnLineSamelineOpenedGapLeadingFragmentViolation {
	@B @A /* TODO: fix // violation: Annotation '@B' must be on its own line. // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	this later */ void f() {}
}
// === end ===

// === case: sameline_three_annotation_field ===
class InputAnnotationOwnLineSamelineThreeAnnotationFieldViolation {
	@A @B @C int allInOne; // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sameline_three_annotation_method ===
class InputAnnotationOwnLineSamelineThreeAnnotationMethodViolation {
	@A @B @C // violation: Annotation '@A' must be on its own line.
	void threeAnnotationsMethod() {}
}
// === end ===

// === case: sameline_top_level_class ===
@A @B // violation: Annotation '@A' must be on its own line.
class InputAnnotationOwnLineSamelineTopLevelClassViolation {}
// === end ===

// === case: sameline_trailing_block_comment_opener ===
class InputAnnotationOwnLineSamelineTrailingBlockCommentOpenerViolation {
	@B @A /* TODO: fix // violation: Annotation '@B' must be on its own line. // violation: Annotation '@A' must appear before '@B' (alphabetical order).
	this later */
	void f() {}
}
// === end ===

// === case: sameline_two_annotation_field ===
class InputAnnotationOwnLineSamelineTwoAnnotationFieldViolation {
	@A @B // violation: Annotation '@A' must be on its own line.
	int twoAnnotationsField;
}
// === end ===

// === case: sameline_two_annotation_method ===
class InputAnnotationOwnLineSamelineTwoAnnotationMethodViolation {
	@A @B // violation: Annotation '@A' must be on its own line.
	void twoAnnotationsMethod() {}
}
// === end ===

// === case: single_annotation_with_declaration ===
class InputAnnotationOwnLineSingleAnnotationWithDeclarationViolation {
	@A void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: sorts_alphabetically ===
class InputAnnotationOwnLineSortsAlphabeticallyViolation {
	@C @A @B void f() {} // violation: Annotation '@C' must be on its own line. // violation: Annotation '@A' must appear before '@C' (alphabetical order).
}
// === end ===

// === case: split_annotation_and_declaration ===
class InputAnnotationOwnLineSplitSliceViolation {
	@interface A {}

	@A void foo() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: split_multiple_annotations_and_declaration ===
class InputAnnotationOwnLineSplitMultipleAnnotationsAndDeclarationViolation {
	@A @B void foo() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===

// === case: tab_between_leading_annotations ===
class InputAnnotationOwnLineTabBetweenLeadingAnnotationsViolation {
	@A	@B void f() {} // violation: Annotation '@A' must be on its own line.
}
// === end ===