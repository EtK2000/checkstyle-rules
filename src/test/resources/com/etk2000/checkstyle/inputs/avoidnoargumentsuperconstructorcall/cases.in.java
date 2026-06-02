package com.etk2000.checkstyle.inputs.avoidnoargumentsuperconstructorcall;

// === case: delete_super_call ===
class InputAvoidNoArgumentSuperCallSliceViolation {
	InputAvoidNoArgumentSuperCallSliceViolation() {
		super(); // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: delete_super_call_no_indent ===
class InputAvoidNoArgumentSuperCallNoIndentSliceViolation {
InputAvoidNoArgumentSuperCallNoIndentSliceViolation() {
super(); // violation: Unnecessary call to superclass constructor with no arguments.
}
}
// === end ===

// === case: fix_super_after_multiline_comment ===
class InputAvoidNoArgumentSuperCallMultilineCommentSliceViolation {
	InputAvoidNoArgumentSuperCallMultilineCommentSliceViolation() {
		/* super();
		super(); */ super(); // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_comment_decoy ===
class InputAvoidNoArgumentSuperCallCommentDecoySliceViolation {
	InputAvoidNoArgumentSuperCallCommentDecoySliceViolation() { /* super(); */ super(); } // violation: Unnecessary call to superclass constructor with no arguments.
}
// === end ===

// === case: fix_super_embedded ===
class InputAvoidNoArgumentSuperCallEmbeddedSliceViolation {
	InputAvoidNoArgumentSuperCallEmbeddedSliceViolation() { super(); } // violation: Unnecessary call to superclass constructor with no arguments.
}
// === end ===

// === case: fix_super_space_before_paren ===
class InputAvoidNoArgumentSuperCallSpaceBeforeParenSliceViolation {
	InputAvoidNoArgumentSuperCallSpaceBeforeParenSliceViolation() {
		super (); // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_space_before_semicolon ===
class InputAvoidNoArgumentSuperCallSpaceBeforeSemicolonSliceViolation {
	InputAvoidNoArgumentSuperCallSpaceBeforeSemicolonSliceViolation() {
		super() ; // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_spaced_parens ===
class InputAvoidNoArgumentSuperCallSpacedParensSliceViolation {
	InputAvoidNoArgumentSuperCallSpacedParensSliceViolation() {
		super( ); // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_with_block_comment ===
class InputAvoidNoArgumentSuperCallBlockCommentSliceViolation {
	InputAvoidNoArgumentSuperCallBlockCommentSliceViolation() {
		super(); /* needed */ // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_with_comment ===
class InputAvoidNoArgumentSuperCallCommentSliceViolation {
	InputAvoidNoArgumentSuperCallCommentSliceViolation() {
		super(); // needed // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: fix_super_with_comment_no_indent ===
class InputAvoidNoArgumentSuperCallCommentNoIndentSliceViolation {
InputAvoidNoArgumentSuperCallCommentNoIndentSliceViolation() {
super(); // needed // violation: Unnecessary call to superclass constructor with no arguments.
}
}
// === end ===

// === case: fix_super_with_extra_code ===
class InputAvoidNoArgumentSuperCallExtraCodeSliceViolation {
	int x;

	InputAvoidNoArgumentSuperCallExtraCodeSliceViolation() {
		super(); x = 1; // violation: Unnecessary call to superclass constructor with no arguments.
	}
}
// === end ===

// === case: skip_super_multiline ===
// skip-reason: super() call spans multiple lines
class InputAvoidNoArgumentSuperCallMultilineSliceViolation {
	InputAvoidNoArgumentSuperCallMultilineSliceViolation() {
		super( // violation: Unnecessary call to superclass constructor with no arguments.
		);
	}
}
// === end ===