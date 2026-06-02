// === case: array_type_both_c_style_no_semicolon_on_violation ===
// target: line=1 col=5
	int alpha[];
	int beta[]
// === end ===

// === case: backward_scan_hits_block_comment ===
// target: line=2 col=5
	int alpha;
	/* separator */
	int beta;
// === end ===

// === case: backward_scan_hits_comment_line ===
// target: line=2 col=5
	int alpha;
	// separator comment
	int beta;
// === end ===

// === case: backward_scan_hits_javadoc ===
// target: line=2 col=5
	int alpha;
	/** Javadoc for beta */
	int beta;
// === end ===

// === case: backward_scan_hits_multi_line_javadoc ===
// target: line=4 col=5
	int alpha;
	/**
	 * Javadoc for beta.
	 */
	int beta;
// === end ===

// === case: comma_merge_last_field ===
// target: line=1 col=3
			alpha,
			beta;
// === end ===

// === case: comma_merge_no_terminator_on_violation ===
// target: line=1 col=3
	int alpha,
			beta
// === end ===

// === case: comma_merge_through_annotation ===
// target: line=2 col=3
	boolean alpha,
	@Deprecated
			beta;
// === end ===

// === case: comma_merge_through_annotation_with_inner_comma ===
// target: line=2 col=3
	boolean alpha,
	@SuppressWarnings({"unused", "all"})
			beta;
// === end ===

// === case: comma_merge_with_block_comment ===
// target: line=1 col=3
	int /* , */ alpha,
			beta;
// === end ===

// === case: comma_merge_with_line_comment ===
// target: line=1 col=3
	int alpha, // trailing,
			beta;
// === end ===

// === case: comma_merge_wraps ===
// target: line=1 col=3
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb;
// === end ===

// === case: continuation_loop_exhausts_lines ===
// target: line=1 col=5
	int prevName;
	int alpha,
			beta,
			gamma,
// === end ===

// === case: continuation_stops_at_comment_no_comma_on_violation ===
// target: line=1 col=5
	int prevName;
	int alpha
			// comment
			beta;
// === end ===

// === case: line_index_zero ===
// target: col=4
int beta;
// === end ===

// === case: merge_ignores_trailing_comma_before_semicolon ===
// target: line=1 col=4
int alpha;
int beta, ;
// === end ===

// === case: no_previous_semicolon ===
// target: line=1 col=5
class Foo {
	int beta;
// === end ===

// === case: prev_line_all_semicolons_in_comments ===
// target: line=1 col=4
// foo;
int beta;
// === end ===

// === case: prev_line_block_comment_unclosed ===
// target: line=1 col=5
	int /* unclosed alpha;
	int beta;
// === end ===

// === case: violation_column_forward_scan_exhausts_line ===
// target: line=1 col=8
int a;
int beta,
// === end ===

// === case: violation_column_mid_identifier ===
// target: line=1 col=6
int alpha;
int beta;
// === end ===

// === case: violation_line_block_comment_post_name_no_semicolon ===
// target: line=1 col=5
	int alpha;
	int beta /* doc */
// === end ===

// === case: violation_line_block_comment_unclosed ===
// target: line=1 col=5
	int alpha;
	int beta /* unclosed
// === end ===

// === case: violation_line_without_semicolon ===
// target: line=1 col=5
	int alpha;
	int beta
// === end ===

// === case: wrap_continuation_breaks_at_no_ident_line ===
// target: line=1 col=5
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
			}
// === end ===

// === case: wrap_continuation_breaks_at_same_indent ===
// target: line=1 col=5
	int prevName;
	int aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,
	int anotherField;
// === end ===

// === case: wrap_continuation_breaks_at_same_indent_mixed_tabs_spaces ===
// target: line=1 col=5
	int prevName;
	int alpha,
    beta;
// === end ===