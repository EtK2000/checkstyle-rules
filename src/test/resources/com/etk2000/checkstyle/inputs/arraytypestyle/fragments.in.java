// === case: block_comment_after_method_return_brackets_returns_null ===
// target: col=7
int m()[] /* c
{ return null; }
// === end ===

// === case: bracket_at_line_start_returns_null ===
// target: col=0
[] x;
// === end ===

// === case: catch_keyword_not_treated_as_param_list ===
// target: col=12
catch (int x[] = a, b) {}
// === end ===

// === case: do_keyword_not_treated_as_param_list ===
// target: col=9
do (int x[] = a, b);
// === end ===

// === case: escaped_backslash_at_eol_in_string ===
// target: line=2 col=5
String s = "abc\
more";
int x[];
// === end ===

// === case: expression_context_not_method_return_returns_null ===
// target: col=9
x = bar()[]
// === end ===

// === case: fake_ident_after_method_return_brackets_returns_null ===
// target: col=7
int m()[] foo;
// === end ===

// === case: field_access_not_method_return_returns_null ===
// target: col=9
obj.bar()[]
// === end ===

// === case: field_init_call_ends_in_paren_multi_var_returns_null ===
// target: line=1 col=0
int x = compute()
[], y = 1;
// === end ===

// === case: generic_record_multi_component ===
// target: col=17
record R<T>(int x[], String s) {}
// === end ===

// === case: generic_record_multi_type_param ===
// target: col=20
record R<K, V>(int x[], V v) {}
// === end ===

// === case: generic_record_nested_type_bounds ===
// target: col=38
record R<T extends List<String>>(int x[], int y) {}
// === end ===

// === case: if_keyword_not_treated_as_param_list ===
// target: col=9
if (int x[] = a, b) {}
// === end ===

// === case: method_ident_at_line_start_returns_null ===
// target: col=3
m()[];
// === end ===

// === case: method_return_followed_by_partial_throws_returns_null ===
// target: col=7
int m()[] thrown;
// === end ===

// === case: method_return_followed_by_throws_like_ident_returns_null ===
// target: col=7
int m()[] throwsException;
// === end ===

// === case: method_return_with_type_use_annotation_returns_null ===
// target: col=11
int m() @A []
// === end ===

// === case: multi_line_brackets_unclosed_returns_null ===
// target: line=1 col=0
int x
[abc];
// === end ===

// === case: multi_line_decl_ending_at_closing_brace_treated_as_terminator ===
// target: line=1 col=2
int x
		[]
}
// === end ===

// === case: multi_line_eof_without_terminator ===
// target: line=1 col=2
int x
		[]
// === end ===

// === case: multi_line_first_line_returns_null ===
// target: col=2
		[];
// === end ===

// === case: multi_line_prev_line_ends_in_brace_returns_null ===
// target: line=1 col=2
class C {
		[];
// === end ===

// === case: multi_line_prev_line_ends_in_permits_returns_null ===
// target: line=1 col=2
sealed class C permits X
		[] {}
// === end ===

// === case: multi_line_prev_line_ends_in_semicolon_returns_null ===
// target: line=1 col=2
int y = 0;
		[];
// === end ===

// === case: multi_line_prev_line_ends_in_throws_ident ===
// target: line=1 col=2
int m() throws E
		[] { return null; }
// === end ===

// === case: multi_line_super_blacklist_returns_null ===
// target: line=1 col=2
super X
		[];
// === end ===

// === case: multi_line_text_block_on_bracket_line_returns_null ===
// target: line=1 col=2
String x
		[] = """
// === end ===

// === case: multi_line_text_block_on_later_line_returns_null ===
// target: line=1 col=2
int x
		[] = a
"""
// === end ===

// === case: multi_line_unterminated_string_does_not_eat_comma_on_next_line ===
// target: line=1 col=2
int x
		[] = "abc
, y = 0;
// === end ===

// === case: multi_line_with_empty_previous_line_returns_null ===
// target: line=1 col=2

		[];
// === end ===

// === case: multi_line_with_whitespace_only_prev_line_returns_null ===
// target: line=1 col=2
		
		[];
// === end ===

// === case: no_suffix_returns_null ===
// target: col=5
int x[]
// === end ===

// === case: open_paren_at_line_start_treated_as_parens ===
// target: col=6
(int x[])
// === end ===

// === case: orphan_close_paren_ignored ===
// target: col=7
) int x[];
// === end ===

// === case: orphan_close_paren_without_match_returns_null ===
// target: col=2
})[];
// === end ===

// === case: permits_keyword_single_line_returns_null ===
// target: col=24
sealed class C permits X[] {}
// === end ===

// === case: super_keyword_single_line_returns_null ===
// target: col=5
super[] x;
// === end ===

// === case: switch_keyword_not_treated_as_param_list ===
// target: col=13
switch (int x[] = a, b) {}
// === end ===

// === case: synchronized_keyword_not_treated_as_param_list ===
// target: col=19
synchronized (int x[] = a, b) {}
// === end ===

// === case: text_block_after_bracket_same_line ===
// target: col=5
int x[] = """
// === end ===

// === case: text_block_opener_after_brackets ===
// target: col=5
int x[]
= """
// === end ===

// === case: throws_keyword_single_line_returns_null ===
// target: col=16
int m() throws E[];
// === end ===

// === case: try_keyword_not_treated_as_param_list ===
// target: col=10
try (int x[] = a, b) {}
// === end ===

// === case: type_use_annotation_in_parameter_returns_null ===
// target: col=16
void m(int x @A [])
// === end ===

// === case: type_use_annotation_in_record_returns_null ===
// target: col=18
record R(int x @A [])
// === end ===

// === case: unclosed_bracket_returns_null ===
// target: col=5
int x[abc];
// === end ===

// === case: while_keyword_not_treated_as_param_list ===
// target: col=12
while (int x[] = a, b) {}
// === end ===

// === case: with_invalid_suffix_returns_null ===
// target: col=5
int x[]:
// === end ===