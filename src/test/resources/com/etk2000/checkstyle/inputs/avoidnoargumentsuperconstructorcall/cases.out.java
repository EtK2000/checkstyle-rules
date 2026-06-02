package com.etk2000.checkstyle.inputs.avoidnoargumentsuperconstructorcall;

// === case: delete_super_call ===
class InputAvoidNoArgumentSuperCallSliceViolation {
	InputAvoidNoArgumentSuperCallSliceViolation() {
	}
}
// === end ===

// === case: delete_super_call_no_indent ===
class InputAvoidNoArgumentSuperCallNoIndentSliceViolation {
InputAvoidNoArgumentSuperCallNoIndentSliceViolation() {
}
}
// === end ===

// === case: fix_super_after_multiline_comment ===
class InputAvoidNoArgumentSuperCallMultilineCommentSliceViolation {
	InputAvoidNoArgumentSuperCallMultilineCommentSliceViolation() {
		/* super();
		super(); */
	}
}
// === end ===

// === case: fix_super_comment_decoy ===
class InputAvoidNoArgumentSuperCallCommentDecoySliceViolation {
	InputAvoidNoArgumentSuperCallCommentDecoySliceViolation() { /* super(); */ }
}
// === end ===

// === case: fix_super_embedded ===
class InputAvoidNoArgumentSuperCallEmbeddedSliceViolation {
	InputAvoidNoArgumentSuperCallEmbeddedSliceViolation() { }
}
// === end ===

// === case: fix_super_space_before_paren ===
class InputAvoidNoArgumentSuperCallSpaceBeforeParenSliceViolation {
	InputAvoidNoArgumentSuperCallSpaceBeforeParenSliceViolation() {
	}
}
// === end ===

// === case: fix_super_space_before_semicolon ===
class InputAvoidNoArgumentSuperCallSpaceBeforeSemicolonSliceViolation {
	InputAvoidNoArgumentSuperCallSpaceBeforeSemicolonSliceViolation() {
	}
}
// === end ===

// === case: fix_super_spaced_parens ===
class InputAvoidNoArgumentSuperCallSpacedParensSliceViolation {
	InputAvoidNoArgumentSuperCallSpacedParensSliceViolation() {
	}
}
// === end ===

// === case: fix_super_with_block_comment ===
class InputAvoidNoArgumentSuperCallBlockCommentSliceViolation {
	InputAvoidNoArgumentSuperCallBlockCommentSliceViolation() {
		/* needed */
	}
}
// === end ===

// === case: fix_super_with_comment ===
class InputAvoidNoArgumentSuperCallCommentSliceViolation {
	InputAvoidNoArgumentSuperCallCommentSliceViolation() {
		// needed
	}
}
// === end ===

// === case: fix_super_with_comment_no_indent ===
class InputAvoidNoArgumentSuperCallCommentNoIndentSliceViolation {
InputAvoidNoArgumentSuperCallCommentNoIndentSliceViolation() {
// needed
}
}
// === end ===

// === case: fix_super_with_extra_code ===
class InputAvoidNoArgumentSuperCallExtraCodeSliceViolation {
	int x;

	InputAvoidNoArgumentSuperCallExtraCodeSliceViolation() {
		x = 1;
	}
}
// === end ===

// === case: skip_super_multiline ===
// skip-reason: super() call spans multiple lines
class InputAvoidNoArgumentSuperCallMultilineSliceViolation {
	InputAvoidNoArgumentSuperCallMultilineSliceViolation() {
		super(
		);
	}
}
// === end ===