// === case: extract_indent_space_value_line ===
// target: line=0 col=1
	@A(
    value = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
// === end ===

// === case: multi_line_empty_parens_eof_no_close ===
// target: col=1
	@B(
// === end ===

// === case: rule1_multiline_no_at_sign ===
// target: col=1
	foo(
	)
// === end ===
