package com.etk2000.checkstyle.inputs.noenumtrailingsemicolon;

// === case: annotated_constant ===
enum ViolationAnnotatedConstant {
	@Deprecated
	X; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: annotated_enum ===
@Deprecated
enum ViolationAnnotated {
	X; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: block_comment_before_closing_brace ===
enum ViolationBlockCommentBeforeBrace {
	X; // violation: Remove trailing semicolon from enum (no body declarations follow).
	/* this comment does not make the semicolon necessary */
}
// === end ===

// === case: constructor_args ===
enum ViolationConstructorArgs {
	X(1),
	Y(2); // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: implements_interface ===
// imports: java.io.Serializable
enum ViolationWithInterface implements Serializable {
	X; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: inner_enum_in_class ===
class ViolationOuter {
	enum Inner {
		X; // violation: Remove trailing semicolon from enum (no body declarations follow).
	}
}
// === end ===

// === case: inner_enum_in_enum ===
enum ViolationEnumInEnum {
	X;

	enum InnerViolation {
		Y; // violation: Remove trailing semicolon from enum (no body declarations follow).
	}
}
// === end ===

// === case: multiline_multiple_constants ===
enum ViolationMultipleConstants {
	A,
	B,
	C; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: multiline_single_constant ===
enum InputEnumSemicolonViolation {
	X; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: remove_semicolon_after_brace ===
enum ViolationConstantWithBody {
	X {
		@Override
		public String toString() {
			return "x";
		}
	}; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: remove_semicolon_before_comment ===
enum InputEnumSemicolonBeforeCommentSliceViolation {
	X; // some comment // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: remove_semicolon_collapses_double_space ===
enum InputEnumSemicolonCollapsesDoubleSpaceSliceViolation { ; } // violation: Remove trailing semicolon from enum (no body declarations follow).
// === end ===

// === case: remove_semicolon_collapses_space_after_only ===
enum InputEnumSemicolonCollapsesSpaceAfterOnlySliceViolation {; } // violation: Remove trailing semicolon from enum (no body declarations follow).
// === end ===

// === case: remove_semicolon_collapses_space_before_only ===
enum InputEnumSemicolonCollapsesSpaceBeforeOnlySliceViolation { ;} // violation: Remove trailing semicolon from enum (no body declarations follow).
// === end ===

// === case: remove_semicolon_inline ===
enum InputEnumSemicolonInlineSliceViolation { X; } // violation: Remove trailing semicolon from enum (no body declarations follow).
// === end ===

// === case: remove_semicolon_on_constant_line ===
enum ViolationMultiline {
	ALPHA,
	BETA,
	GAMMA; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: remove_semicolon_only_on_line ===
enum ViolationNoConstants {
	; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: semicolon_column_zero ===
enum ViolationSemicolonColumnZero {
	X
; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===

// === case: semicolon_on_own_line ===
enum ViolationSemicolonOwnLine {
	X
	; // violation: Remove trailing semicolon from enum (no body declarations follow).
}
// === end ===