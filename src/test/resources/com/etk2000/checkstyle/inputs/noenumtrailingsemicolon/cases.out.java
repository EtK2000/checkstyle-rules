package com.etk2000.checkstyle.inputs.noenumtrailingsemicolon;

// === case: annotated_constant ===
enum ViolationAnnotatedConstant {
	@Deprecated
	X
}
// === end ===

// === case: annotated_enum ===
@Deprecated
enum ViolationAnnotated {
	X
}
// === end ===

// === case: block_comment_before_closing_brace ===
enum ViolationBlockCommentBeforeBrace {
	X
	/* this comment does not make the semicolon necessary */
}
// === end ===

// === case: constructor_args ===
enum ViolationConstructorArgs {
	X(1),
	Y(2)
}
// === end ===

// === case: implements_interface ===
// imports: java.io.Serializable
enum ViolationWithInterface implements Serializable {
	X
}
// === end ===

// === case: inner_enum_in_class ===
class ViolationOuter {
	enum Inner {
		X
	}
}
// === end ===

// === case: inner_enum_in_enum ===
enum ViolationEnumInEnum {
	X;

	enum InnerViolation {
		Y
	}
}
// === end ===

// === case: multiline_multiple_constants ===
enum ViolationMultipleConstants {
	A,
	B,
	C
}
// === end ===

// === case: multiline_single_constant ===
enum InputEnumSemicolonViolation {
	X
}
// === end ===

// === case: remove_semicolon_after_brace ===
enum ViolationConstantWithBody {
	X {
		@Override
		public String toString() {
			return "x";
		}
	}
}
// === end ===

// === case: remove_semicolon_before_comment ===
enum InputEnumSemicolonBeforeCommentSliceViolation {
	X // some comment
}
// === end ===

// === case: remove_semicolon_collapses_double_space ===
enum InputEnumSemicolonCollapsesDoubleSpaceSliceViolation { }
// === end ===

// === case: remove_semicolon_collapses_space_after_only ===
enum InputEnumSemicolonCollapsesSpaceAfterOnlySliceViolation { }
// === end ===

// === case: remove_semicolon_collapses_space_before_only ===
enum InputEnumSemicolonCollapsesSpaceBeforeOnlySliceViolation { }
// === end ===

// === case: remove_semicolon_inline ===
enum InputEnumSemicolonInlineSliceViolation { X }
// === end ===

// === case: remove_semicolon_on_constant_line ===
enum ViolationMultiline {
	ALPHA,
	BETA,
	GAMMA
}
// === end ===

// === case: remove_semicolon_only_on_line ===
enum ViolationNoConstants {
}
// === end ===

// === case: semicolon_column_zero ===
enum ViolationSemicolonColumnZero {
	X
}
// === end ===

// === case: semicolon_on_own_line ===
enum ViolationSemicolonOwnLine {
	X
}
// === end ===