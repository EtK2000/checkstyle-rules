// === case: append_concat_inside_block_comment_bails ===
// target: line=1 col=16
		/* dangling
		sb.append("x" + b);
// === end ===

// === case: append_concat_unclosed_paren ===
// target: col=14
		sb.append(a + b
// === end ===

// === case: boxed_constructor_refuses_unclosed_paren ===
// target: col=16
		final var x = new Integer(42
// === end ===

// === case: empty_string_concat_line_comment_before_end_bails ===
// target: col=19
		final var s = "" + foo // trailing
// === end ===

// === case: empty_string_concat_unterminated_block_comment_bails ===
// target: col=19
		final var s = "" + foo /* dangling
// === end ===

// === case: new_string_refuses_unclosed_paren ===
// target: col=16
		final var s = new String(existing
// === end ===

// === case: string_concat_array_lhs_classic_for_unparseable_header_bails ===
// target: line=3 col=2
	arr[k] = "";
	for (/* unclosed
		j = 0; j < n; ++j)
		arr[k] = arr[k] + j;
// === end ===

// === case: string_concat_gap_cross_scope_clean_bails ===
// target: line=5 col=3
	void g() {
		String s = "in g";
	}
	void f() {
		for (var x : list)
			s = s + x;
// === end ===

// === case: string_concat_gap_nested_scope_bails ===
// target: line=3 col=3
	String s = "";
	if (flag) {
		for (var x : list)
			s = s + x;
// === end ===

// === case: string_concat_gap_text_block_bails ===
// target: line=3 col=2
	String s = "";
	var t = """;
	for (var x : list)
		s = s + x;
// === end ===

// === case: string_concat_in_loop_inside_block_comment_bails ===
// target: line=4 col=3
		var s = "";
		var t = "";
		for (var i = 0; i < 3; ++i) {
			/* commented out
			s += "x";
			*/
			t += "y";
		}
// === end ===

// === case: string_concat_lhs_malformed_bails ===
// target: line=2 col=2
	String s = "";
	for (var x : list)
		.bad = s + x;
// === end ===

// === case: string_concat_tier2_do_while_last_line_bails ===
// target: line=1 col=2
	String s = "";
	do s = s + "y";
// === end ===

// === case: string_concat_tier2_do_while_missing_semicolon_bails ===
// target: line=1 col=2
	String s = "";
	do s = s + "y";
	while (s.length() < 5) {
// === end ===

// === case: string_concat_tier2_do_while_nested_scope_bails ===
// target: line=2 col=2
	String s = "";
	if (flag) {
	do s = s + "y";
	while (s.length() < 5);
// === end ===

// === case: string_concat_tier2_do_while_no_matching_while_bails ===
// target: line=1 col=2
	String s = "";
	do s = s + "y";
	notAWhile();
// === end ===