// === case: cinit_same_line_decl_and_cinit_renamed_with_usage_on_earlier_line_is_auto_fixed ===
package x;
import foo.Foo;
class T {
	int use() { return X; }
	 
}
// === end ===

// === case: cinit_same_line_decl_and_cinit_renamed_with_usage_on_other_line_is_auto_fixed ===
package x;
import foo.Foo;
class T {  
	int use() { return X; }
}
// === end ===

// === case: conflict_scan_ignores_malformed_static_import ===
package x;
import foo.Foo;
import static .X;
import static foo.X.;
class T {
}
// === end ===

// === case: malformed_imports_are_ignored_during_resolution ===
package x;
import .Foo;
import foo.;
import wild.*;
class T {
}
// === end ===

// === case: malformed_wildcard_import_skipped ===
package x;
import .*;
import foo.*;
class T {
}
// === end ===

// === case: wildcard_prefix_ends_with_dot_skipped ===
package x;
import foo..*;
import good.*;
class T {
}
// === end ===

// === case: wildcard_prefix_starts_with_dot_skipped ===
package x;
import ..foo.*;
import good.*;
class T {
}
// === end ===