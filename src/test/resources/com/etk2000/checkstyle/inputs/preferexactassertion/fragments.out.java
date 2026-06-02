// === case: import_with_trailing_line_comment_recognized ===
import static org.junit.jupiter.api.Assertions.assertTrue; // bootstrap

		assertInstanceOf(String.class, o);
// === end ===

// === case: multi_line_call_open_paren_on_own_line_fixed ===
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertInstanceOf(String.class, o);
// === end ===

// === case: multi_line_call_string_literal_containing_name_ignored ===
import static org.junit.jupiter.api.Assertions.assertTrue;

		String s = "earlier: assertTrue";
		assertInstanceOf(String.class, o);
// === end ===

// === case: multi_line_call_with_line_comment_before_open_paren_fixed ===
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertInstanceOf(String.class, o);
// === end ===

// === case: multi_line_negation_inner_fixed_with_outer_parens_preserved ===
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertFalse((
				o instanceof String
		));
// === end ===

// === case: whitespace_tolerant_import_recognized ===
	import   static  org.junit.jupiter.api.Assertions.assertTrue ;

		assertInstanceOf(String.class, o);
// === end ===
