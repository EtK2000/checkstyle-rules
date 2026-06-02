// === case: chained_receiver_negated_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		getHelper().assertTrue(!flag);
// === end ===

// === case: explicit_type_arg_in_args_skipped ===
// target: line=3 col=0
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(Map.<String, Integer>of() instanceof Map);
// === end ===

// === case: helper_dot_on_previous_line_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.
		assertTrue(!flag);
// === end ===

// === case: helper_qualified_block_comment_before_dot_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper. /* explanatory note */
		assertTrue(!flag);
// === end ===

// === case: helper_qualified_char_literal_walkback_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		char slash = '/'; helper
				.assertTrue(!flag);
// === end ===

// === case: helper_qualified_comment_line_intermediary_skipped ===
// target: line=4 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper
		// inline note
				.assertTrue(!flag);
// === end ===

// === case: helper_qualified_comparison_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.assertTrue(a == b);
// === end ===

// === case: helper_qualified_line_comment_before_dot_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper. // trailing note
		assertTrue(!flag);
// === end ===

// === case: helper_qualified_negated_instance_of_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.assertTrue(!(o instanceof String));
// === end ===

// === case: helper_qualified_negated_instance_of_skipped_assert_false ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.assertFalse(!(o instanceof String));
// === end ===

// === case: helper_qualified_plain_instance_of_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.assertTrue(o instanceof String);
// === end ===

// === case: helper_qualified_plain_negation_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper.assertTrue(!flag);
// === end ===

// === case: helper_qualified_string_literal_walkback_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		String url = "scheme://x"; helper
				.assertTrue(!flag);
// === end ===

// === case: helper_qualified_trailing_block_comment_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper /* see Assertions */
				.assertTrue(!flag);
// === end ===

// === case: helper_qualified_trailing_line_comment_skipped ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		helper // refactored from Assert
				.assertTrue(!flag);
// === end ===

// === case: import_with_trailing_line_comment_recognized ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue; // bootstrap

		assertTrue(o instanceof String);
// === end ===

// === case: junit4_and_5_mixed_imports_skip_unqualified ===
// target: line=3 col=0
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

		assertTrue(o instanceof String);
// === end ===

// === case: junit4_import_only_skipped ===
// target: line=2 col=0
import static org.junit.Assert.assertTrue;

		assertTrue(o instanceof String);
// === end ===

// === case: junit4_wildcard_import_only_skipped ===
// target: line=2 col=0
import static org.junit.Assert.*;

		assertTrue(o instanceof String);
// === end ===

// === case: method_at_file_start_with_leading_dot_skipped ===
// target: line=0 col=0
.assertTrue(!flag);
// === end ===

// === case: multi_line_call_no_close_paren_skipped ===
// target: col=0
		assertTrue(o instanceof String
// === end ===

// === case: multi_line_call_no_semicolon_skipped ===
// target: col=0
		boolean b = assertTrue(o instanceof String)
// === end ===

// === case: multi_line_call_open_paren_on_own_line_fixed ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue
		(
				o instanceof String
		);
// === end ===

// === case: multi_line_call_string_literal_containing_name_ignored ===
// target: line=3 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		String s = "earlier: assertTrue";
		assertTrue
		(
			o instanceof String
		);
// === end ===

// === case: multi_line_call_with_line_comment_before_open_paren_fixed ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue // note
		(o instanceof String);
// === end ===

// === case: multi_line_negation_inner_fixed_with_outer_parens_preserved ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!(
				o instanceof String
		));
// === end ===

// === case: multi_line_outer_paren_arg_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue((
				o instanceof String
		));
// === end ===

// === case: negation_both_frameworks_skipped ===
// target: line=3 col=0
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!flag);
// === end ===

// === case: negation_empty_inner_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!());
// === end ===

// === case: negation_no_negation_no_instanceof_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(flag);
// === end ===

// === case: negation_not_equal_malformed_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!= flag);
// === end ===

// === case: paren_receiver_negated_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		(helper).assertTrue(!flag);
// === end ===

// === case: qualified_heuristic_not_spoofed_by_string_literal ===
// target: line=2 col=0
import static org.junit.Assert.assertTrue;

		assertTrue("Assertions.assertInstanceOf".equals(s) && o instanceof String);
// === end ===

// === case: structural_line_comment_in_args_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(!flag // dangerous
);
// === end ===

// === case: unicode_escape_in_string_arg_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(o instanceof String, "prefix \u002F\u002F suffix");
// === end ===

// === case: unicode_escape_outside_literal_skipped ===
// target: line=2 col=0
import static org.junit.jupiter.api.Assertions.assertTrue;

		assertTrue(o instanceof String /* \u002F\u002F */);
// === end ===

// === case: walkback_line_in_block_comment_middle_skipped ===
// target: line=2 col=0
/* opens earlier
   continues here */ helper
		.assertTrue(!flag);
// === end ===

// === case: walkback_line_with_unicode_escape_skipped ===
// target: line=2 col=0
helper
String banner = "x\u0041y";
		.assertTrue(!flag);
// === end ===

// === case: whitespace_tolerant_import_recognized ===
// target: line=2 col=0
	import   static  org.junit.jupiter.api.Assertions.assertTrue ;

		assertTrue(o instanceof String);
// === end ===
