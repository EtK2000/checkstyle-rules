// === case: array_type_both_c_style_no_semicolon_on_violation ===
	int[] alpha, beta;
// === end ===

// === case: comma_merge_last_field ===
			alpha, beta;
// === end ===

// === case: comma_merge_no_terminator_on_violation ===
	int alpha, beta,
// === end ===

// === case: comma_merge_through_annotation ===
	boolean alpha, beta;
// === end ===

// === case: comma_merge_through_annotation_with_inner_comma ===
	boolean alpha, beta;
// === end ===

// === case: comma_merge_with_block_comment ===
	int /* , */ alpha, beta;
// === end ===

// === case: comma_merge_with_line_comment ===
	int alpha, beta; // trailing,
// === end ===

// === case: comma_merge_wraps ===
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
// === end ===

// === case: continuation_loop_exhausts_lines ===
	int prevName, alpha, beta, gamma;
// === end ===

// === case: continuation_stops_at_comment_no_comma_on_violation ===
	int prevName, alpha;
			// comment
			beta;
// === end ===

// === case: merge_ignores_trailing_comma_before_semicolon ===
int alpha, beta;
// === end ===

// === case: violation_line_without_semicolon ===
	int alpha, beta;
// === end ===

// === case: wrap_continuation_breaks_at_no_ident_line ===
	int prevName, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
			}
// === end ===

// === case: wrap_continuation_breaks_at_same_indent ===
	int prevName, aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa;
	int anotherField;
// === end ===

// === case: wrap_continuation_breaks_at_same_indent_mixed_tabs_spaces ===
	int prevName, alpha;
    beta;
// === end ===