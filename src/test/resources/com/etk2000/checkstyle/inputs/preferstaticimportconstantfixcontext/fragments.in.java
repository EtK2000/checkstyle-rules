// === case: default_package_emits_simple_name_only ===
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: explicit_import_wins_over_sibling ===
package x;
import other.Foo;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: package_line_with_trailing_comment_recognized ===
package foo.bar; // a trailing comment
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: package_with_internal_whitespace_is_sanitized ===
package foo . bar;
class T {
	private static final int X = Foo.X;
}
// === end ===

// === case: same_package_helper ===
package x.y; class Helper { static final int MAX = 100; }
// === end ===

// === case: same_package_resolution_uses_sibling_file ===
package x.y;
class T {
	private static final int MAX = Helper.MAX;
}
// === end ===

// === case: same_package_resolution_with_leading_bom_uses_sibling_file ===
﻿package x.y;
class T {
	private static final int MAX = Helper.MAX;
}
// === end ===

// === case: sibling_foo ===
class Foo {}
// === end ===

// === case: sibling_wins_over_wildcard ===
package x;
import wild.*;
class T {
	private static final int X = Foo.X;
}
// === end ===
