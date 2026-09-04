// === case: method_at_file_start_with_leading_dot_unparseable ===
// target: line=0 col=11
.assertTrue(!flag);
// === end ===

// === case: multi_line_call_no_close_paren_unparseable ===
// target: col=12
		assertTrue(o instanceof String
// === end ===

// === case: multi_line_call_no_semicolon_unparseable ===
// target: col=24
		boolean b = assertTrue(o instanceof String)
// === end ===

// === case: negation_empty_inner_unparseable ===
// target: line=2 col=12
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!());
// === end ===

// === case: negation_not_equal_malformed_unparseable ===
// target: line=2 col=12
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!= flag);
// === end ===

// === case: unicode_escape_in_string_arg_unparseable ===
// target: line=2 col=12
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(o instanceof String, "prefix \u002F\u002F suffix");
// === end ===

// === case: unicode_escape_outside_literal_unparseable ===
// target: line=2 col=12
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(o instanceof String /* \u002F\u002F */);
// === end ===

// === case: walkback_line_with_unicode_escape_unparseable ===
// target: line=2 col=13
helper
String banner = "x\u0041y";
		.assertTrue(!flag);
// === end ===
