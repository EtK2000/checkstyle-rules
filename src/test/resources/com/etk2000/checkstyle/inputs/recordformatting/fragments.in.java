// === case: fix_component_empty_leading_component_returns_null ===
// target: col=10
	record R(, int a,
		int b) {}
// === end ===

// === case: fix_component_empty_middle_component_returns_null ===
// target: col=10
	record R(int a, ,
		int b) {}
// === end ===

// === case: fix_component_line_comment_on_rparen_line_returns_null ===
// target: line=1 col=5
	record R(
			int a,
	// note ) {}
// === end ===

// === case: fix_component_record_keyword_after_supplementary_ident_char_returns_null ===
// target: col=5
	foo(x𝐀record(a,
		b)) {}
// === end ===

// === case: fix_component_trailing_comma_returns_null ===
// target: col=10
	record R(int a, int b,
		int c, ) {}
// === end ===

// === case: fix_component_unbalanced_angle_brackets_returns_null ===
// target: col=10
	record R(List<String x, int y)
	{}
// === end ===

// === case: fix_component_unterminated_block_comment_in_header_returns_null ===
// target: col=10
	record R(int a, /* unterminated
		int b) {}
// === end ===

// === case: fix_component_unterminated_string_hiding_record_keyword ===
// target: col=5
	String s = "unterminated record R(int a,
		int b) {}
// === end ===

// === case: fix_component_unterminated_string_in_header_returns_null ===
// target: col=10
	record R(@A("unterminated) int a,
		int b) {}
// === end ===

// === case: fix_empty_body_close_on_line_after_unterminated_string ===
// target: line=1 col=1
	String s = "unterminated
	}
// === end ===

// === case: fix_open_brace_after_unterminated_block_comment ===
// target: line=1 col=1
	record R(int a) /* unterminated
	{}
// === end ===

// === case: fix_open_brace_after_unterminated_char_literal ===
// target: line=1 col=1
	record R(int a) /* */ 'unterminated
	{}
// === end ===