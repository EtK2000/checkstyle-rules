// === case: array_init_before_reported_declaration ===
// target: col=25
	int[] a = new int[]{1}; String s = ""
// === end ===

// === case: at_prefixed_open_paren_does_not_join ===
// target: line=1 col=1
	@Foo(
	String s = "";
// === end ===

// === case: chain_receiver_diamond_without_an_ast ===
// target: col=8
		final java.util.List<String> values = new Holder<>().names();
	} } }
// === end ===

// === case: column_at_exact_end ===
// target: col=11
	int x = 5;
// === end ===

// === case: column_at_non_identifier ===
// target: col=7
	int x = 5;
// === end ===

// === case: column_mid_identifier ===
// target: col=2
	int x = 5;
// === end ===

// === case: declaration_type_inside_string_literal ===
// target: col=13
	String s = "int x = 5";
// === end ===

// === case: diamond_no_new_keyword ===
// target: col=1
	var x = factory<Object>();
// === end ===

// === case: diamond_unbalanced_angle_brackets ===
// target: col=1
	var x = new ArrayList<Object();
// === end ===

// === case: double_equals_after_name ===
// target: col=1
	int b == true;
// === end ===

// === case: equals_at_line_end ===
// target: col=1
	int[] a =
// === end ===

// === case: equals_only_double_equals ===
// target: col=1
	var b == true;
// === end ===

// === case: explicit_array_init_no_brace ===
// target: col=1
	int[] a = new int[]
// === end ===

// === case: explicit_array_init_no_brace_var ===
// target: col=1
	final var a = new String[]
// === end ===

// === case: explicit_array_init_unbalanced_angle_brackets ===
// target: col=1
	var a = new List<String[]{"a"};
// === end ===

// === case: explicit_array_init_unbalanced_paren ===
// target: col=1
	var x) = new String[]{"a"};
// === end ===

// === case: multi_var_text_fallback ===
// target: col=1
	int a = 1, b = 2
// === end ===

// === case: unterminated_generic_declaration ===
// target: col=1
	List<String> values = build(
// === end ===

// === case: var_declaration_after_reported_declaration ===
// target: col=1
	int a = 1; var b = new ArrayList<Object>()
// === end ===