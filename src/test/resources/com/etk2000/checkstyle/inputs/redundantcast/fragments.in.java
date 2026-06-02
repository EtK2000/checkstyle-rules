// === case: afterOuter_walks_past_eol ===
// target: col=8
return ((String) s)
// === end ===

// === case: array_access_before_outer_paren ===
// target: col=7
arr[0]((String) s).length();
// === end ===

// === case: block_comment_unterminated ===
// target: col=8
return ((String) s /* unterminated
// === end ===

// === case: chained_call_before_outer_paren ===
// target: col=6
f.g()((String) s).length();
// === end ===

// === case: char_literal_unterminated ===
// target: col=8
return ((Object) 'a
// === end ===

// === case: column_not_open_paren ===
// target: col=10
final String s = (String) null;
// === end ===

// === case: line_comment_in_receiver_wrap ===
// target: col=8
return ((String) s //x).length();
// === end ===

// === case: malformed_no_expression ===
// target: col=17
final String s = (String)
// === end ===

// === case: multi_line_block_comment_prior_doesNotStrip ===
// target: line=3 col=1
foo /*
 multi line
 */
((String) s).length();
// === end ===

// === case: multi_line_cast ===
// target: col=17
final String s = (String
// === end ===

// === case: outer_open_no_close_same_line ===
// target: col=12
takesString((String) s,
// === end ===

// === case: path_a_prior_line_rbracket_rejected ===
// target: line=1 col=1
arr[0]
((String) s).length();
// === end ===

// === case: path_a_prior_line_rparen_rejected ===
// target: line=1 col=1
foo()
((String) s).length();
// === end ===

// === case: string_literal_unterminated ===
// target: col=8
return ((String) "unterminated
// === end ===

// === case: text_block_body_with_escape_doesNotStrip ===
// target: line=3 col=1
String x = """
\""";
// foo
((String) s).length();
// === end ===

// === case: text_block_close_then_outer_open_no_close_plain_strips ===
// target: line=2 col=9
String x = """
hi
"""; foo((String) s,
// === end ===

// === case: text_block_closes_at_line_end_strips ===
// target: line=1 col=8
String x = """abc"""
return ((String) s).length();
// === end ===

// === case: text_block_unterminated ===
// target: col=8
return ((String) """abc
// === end ===