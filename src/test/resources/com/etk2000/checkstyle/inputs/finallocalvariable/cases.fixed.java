// === case: add_final_annotation_single_line ===
// imports: javax.annotation.Nonnull
class InputFinalLocalAddFinalAnnotationSingleLineViolation {
	void m() {
		@Nonnull
		final Runnable r = () -> {};
		r.run();
	}
}
// === end ===

// === case: split_annotation_on_type_line ===
// imports: javax.annotation.Nonnull
class InputFinalLocalSplitAnnotationOnTypeLineViolation {
	void m() {
		@Nonnull
		final Runnable
				r = () -> {};
		r.run();
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