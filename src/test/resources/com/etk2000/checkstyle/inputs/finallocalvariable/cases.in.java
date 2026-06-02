package com.etk2000.checkstyle.inputs.finallocalvariable;

// === case: add_final_after_two_tabs ===
class InputAddFinalAfterTwoTabsViolation {
	int method() {
		var x = 5; // violation: Variable 'x' should be declared final.
		return x;
	}
}
// === end ===

// === case: add_final_annotation_single_line ===
// imports: javax.annotation.Nonnull
class InputFinalLocalAddFinalAnnotationSingleLineViolation {
	void m() {
		@Nonnull Runnable r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: multi_var_declaration ===
class InputFinalLocalMultiVarDeclarationViolation {
	int method() {
		int x, y; // violation: Variable 'x' should be declared final. // violation: Variable 'y' should be declared final.
		x = 5;
		y = 6;
		return x + y;
	}
}
// === end ===

// === case: split_annotation_on_type_line ===
// imports: javax.annotation.Nonnull
class InputFinalLocalSplitAnnotationOnTypeLineViolation {
	void m() {
		@Nonnull Runnable
				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: split_blank_line_between ===
class InputFinalLocalSplitBlankLineBetweenViolation {
	void m() {
		Runnable

				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: split_comment_between ===
// skip-reason: no declaration type line precedes the variable name
class InputFinalLocalSplitCommentBetweenViolation {
	void m() {
		Runnable
				// note
				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: split_multi_var_continuation ===
// skip-reason: multi-variable declaration
class InputFinalLocalSplitMultiVarContinuationViolation {
	void m() {
		Runnable s,
				other = () -> {}; // violation: Variable 'other' should be declared final.
		s = () -> {};
		s = () -> {};
		s.run();
		other.run();
	}
}
// === end ===

// === case: split_multi_var_trailing_comment ===
// skip-reason: multi-variable declaration
class InputFinalLocalSplitMultiVarTrailingCommentViolation {
	void m() {
		Runnable s, // note
				other = () -> {}; // violation: Variable 'other' should be declared final.
		s = () -> {};
		s = () -> {};
		s.run();
		other.run();
	}
}
// === end ===

// === case: split_two_blank_lines_between ===
class InputFinalLocalSplitTwoBlankLinesBetweenViolation {
	void m() {
		Runnable


				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: split_type_comment_comma ===
class InputFinalLocalSplitTypeCommentCommaViolation {
	void m() {
		Runnable // a, b
				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===

// === case: split_type_on_own_line ===
class InputFinalLocalSplitTypeOnOwnLineViolation {
	void m() {
		Runnable
				r = () -> {}; // violation: Variable 'r' should be declared final.
		r.run();
	}
}
// === end ===