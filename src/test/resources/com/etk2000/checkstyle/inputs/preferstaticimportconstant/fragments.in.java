// === case: annotation_arg_containing_bare_private_word_does_not_misidentify_visibility ===
// target: line=3 col=1
package x;
import foo.Foo;
class T {
	@MyAnnotation( private ) static final int X = Foo.X;
}
// === end ===

// === case: cinit_conflicting_target_field_returns_conflict_skip ===
// target: line=4 col=26
package x;
import foo.Foo;
import static other.Bar.X;
class T {
	private static final int X;
	private static final int Y = Foo.Y;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_fixer_with_column_off_ident_returns_cinit_skip ===
// target: line=3 col=0
package x;
import foo.Foo;
class T {
	private static final int X;

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_usage_on_earlier_line_is_auto_fixed ===
// target: line=4 col=26
package x;
import foo.Foo;
class T {
	int use() { return RENAMED; }
	private static final int RENAMED; static { RENAMED = Foo.X; }
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_usage_on_other_line_is_auto_fixed ===
// target: line=2 col=35
package x;
import foo.Foo;
class T { private static final int RENAMED; static { RENAMED = Foo.X; }
	int use() { return RENAMED; }
}
// === end ===

// === case: cinit_with_nested_same_name_field_and_no_matching_cinit_returns_cinit_skip ===
// target: line=5 col=27
package x;
import foo.Foo;
class T {
	private static final int X;
	static class Inner {
		private static final int X;
	}

	static {
		X = Foo.X;
	}
}
// === end ===

// === case: conflict_scan_ignores_malformed_static_import ===
// target: line=5 col=1
package x;
import foo.Foo;
import static .X;
import static foo.X.;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: conflicting_static_import_with_leading_bom_returns_conflict_skip ===
// target: line=3 col=1
﻿import static other.Bar.X;
import foo.Foo;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: malformed_imports_are_ignored_during_resolution ===
// target: line=5 col=1
package x;
import .Foo;
import foo.;
import wild.*;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: malformed_wildcard_import_skipped ===
// target: line=4 col=1
package x;
import .*;
import foo.*;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: multi_var_conflicting_target_alias_returns_conflict_skip ===
// target: line=4 col=26
import foo.Foo;
import foo.Bar;
import static other.Other.X;
class T {
	private static final int X = Foo.X, Y = Bar.Y;
}
// === end ===

// === case: multi_var_fixer_with_column_off_ident_returns_multi_var_skip ===
// target: line=3 col=0
package x;
import foo.Foo;
class T {
	private static final int X = Foo.X, Y = Foo.X;
}
// === end ===

// === case: multi_var_unresolvable_class_returns_skip ===
// target: line=3 col=26
package x;
import foo.Foo;
class T {
	private static final int X = NotImported.X, Y = Foo.Y;
}
// === end ===

// === case: no_statement_terminator_across_multiple_lines_skips ===
// target: line=2 col=1
package x;
class T {
	private static final int X =
			Foo.X
// === end ===

// === case: no_statement_terminator_skips ===
// target: line=2 col=1
package x;
class T {
	private static final int X = Foo.X
// === end ===

// === case: parse_alias_dot_at_eof_skips ===
// target: line=2 col=1
package x;
class T {
	private static final int X = Foo.;
}
// === end ===

// === case: parse_alias_equals_at_end_of_line_skips ===
// target: line=2 col=1
package x;
class T {
	private static final int X =;
}
// === end ===

// === case: parse_alias_no_equals_returns_skip ===
// target: line=3 col=1
package x;
import foo.Foo;
class T {
	private static final int X /* no = */ Foo.X;
// === end ===

// === case: renamed_alias_with_unparseable_file_bails_conservatively ===
// target: line=3 col=1
package x;
import foo.Foo;
class T {
	private static final int RENAMED = Foo.X;

	int use() {
		return ((;
	}
}
// === end ===

// === case: rhs_dot_followed_by_literal_returns_skip ===
// target: line=2 col=1
package x;
class T {
	private static final int X = Foo.42;
}
// === end ===

// === case: rhs_excess_closing_parens_returns_null ===
// target: line=3 col=1
package x;
import foo.Foo;
class T {
	private static final int X = Foo.X);
}
// === end ===

// === case: rhs_mismatched_parens_returns_null ===
// target: line=3 col=1
package x;
import foo.Foo;
class T {
	private static final int X = (Foo.X;
}
// === end ===

// === case: wildcard_prefix_ends_with_dot_skipped ===
// target: line=4 col=1
package x;
import foo..*;
import good.*;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: wildcard_prefix_starts_with_dot_skipped ===
// target: line=4 col=1
package x;
import ..foo.*;
import good.*;
class T {
	private static final int X = Foo.X;
}
// === end ===