// === case: array_init_before_reported_declaration ===
	int[] a = new int[]{1}; var s = ""
// === end ===

// === case: at_prefixed_open_paren_does_not_join ===
	@Foo(
	var s = "";
// === end ===

// === case: equals_at_line_end ===
	var a =
// === end ===

// === case: explicit_array_init_no_brace ===
	var a = new int[]
// === end ===

// === case: var_declaration_after_reported_declaration ===
	var a = 1; var b = new ArrayList<Object>()
// === end ===