package com.etk2000.checkstyle.inputs.finallocalvariable;

// === case: add_final_after_two_tabs ===
class InputAddFinalAfterTwoTabsViolation {
	int method() {
		final var x = 5;
		return x;
	}
}
// === end ===

// === case: add_final_annotation_single_line ===
// imports: javax.annotation.Nonnull
class InputFinalLocalAddFinalAnnotationSingleLineViolation {
	void m() {
		final @Nonnull Runnable r = () -> {};
		r.run();
	}
}
// === end ===

// === case: multi_var_declaration ===
class InputFinalLocalMultiVarDeclarationViolation {
	int method() {
		final int x, y;
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
		final @Nonnull Runnable
				r = () -> {};
		r.run();
	}
}
// === end ===

// === case: split_blank_line_between ===
class InputFinalLocalSplitBlankLineBetweenViolation {
	void m() {
		final Runnable

				r = () -> {};
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
				r = () -> {};
		r.run();
	}
}
// === end ===

// === case: split_multi_var_continuation ===
// skip-reason: multi-variable declaration
class InputFinalLocalSplitMultiVarContinuationViolation {
	void m() {
		Runnable s,
				other = () -> {};
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
				other = () -> {};
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
		final Runnable


				r = () -> {};
		r.run();
	}
}
// === end ===

// === case: split_type_comment_comma ===
class InputFinalLocalSplitTypeCommentCommaViolation {
	void m() {
		final Runnable // a, b
				r = () -> {};
		r.run();
	}
}
// === end ===

// === case: split_type_on_own_line ===
class InputFinalLocalSplitTypeOnOwnLineViolation {
	void m() {
		final Runnable
				r = () -> {};
		r.run();
	}
}
// === end ===