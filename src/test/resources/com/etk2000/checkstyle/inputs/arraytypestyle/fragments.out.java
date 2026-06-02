// === case: escaped_backslash_at_eol_in_string ===
String s = "abc\
more";
int[] x;
// === end ===

// === case: generic_record_multi_component ===
record R<T>(int[] x, String s) {}
// === end ===

// === case: generic_record_multi_type_param ===
record R<K, V>(int[] x, V v) {}
// === end ===

// === case: generic_record_nested_type_bounds ===
record R<T extends List<String>>(int[] x, int y) {}
// === end ===

// === case: multi_line_decl_ending_at_closing_brace_treated_as_terminator ===
int[] x
}
// === end ===

// === case: multi_line_eof_without_terminator ===
int[] x
// === end ===

// === case: open_paren_at_line_start_treated_as_parens ===
(int[] x)
// === end ===

// === case: orphan_close_paren_ignored ===
) int[] x;
// === end ===