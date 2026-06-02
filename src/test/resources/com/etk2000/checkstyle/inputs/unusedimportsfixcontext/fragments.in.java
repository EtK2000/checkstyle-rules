// === case: deletes_empty_line_from_cascade ===
// target: line=2 col=0
package x;


class X {
	void m() {}
}
// === end ===

// === case: deletes_identifier_containing_simple_name_as_prefix ===
// target: line=2 col=0
package x;

import static foo.bar.foo;
class X {
	void m() { fooBar(); }
}
// === end ===

// === case: deletes_identifier_containing_simple_name_as_suffix ===
// target: line=2 col=0
package x;

import static foo.bar.foo;
class X {
	void m() { barfoo(); }
}
// === end ===

// === case: deletes_import_with_block_comment_in_fqn ===
// target: line=2 col=0
package x;

import java.util. /*c*/ List;
class X {
	void m() {}
}
// === end ===

// === case: deletes_import_with_trailing_line_comment ===
// target: line=2 col=0
package x;

import java.util.List; // once used a List
class X {
	void m() {}
}
// === end ===

// === case: deletes_static_wildcard_import_without_reverify ===
// target: line=2 col=0
package x;

import static java.util.Map.*;
class X { Entry<String, String> e; }
// === end ===

// === case: deletes_wildcard_import_with_comment ===
// target: line=2 col=0
package x;

import java.util.*; // c
class X { List<String> l; }
// === end ===

// === case: deletes_wildcard_import_without_reverify ===
// target: line=2 col=0
package x;

import java.util.*;
class X { List<String> l; }
// === end ===

// === case: skips_comment_only_line ===
// target: line=2 col=0
package x;

/* import a.b.C; */
class X {}
// === end ===

// === case: skips_import_no_dot ===
// target: line=2 col=0
package x;

import Foo;
class X { Foo f; }
// === end ===

// === case: skips_import_with_comment_now_used ===
// target: line=2 col=0
package x;

import java.util.List; // note
class X { List l; }
// === end ===

// === case: skips_import_with_unterminated_block_comment ===
// target: line=2 col=0
package x;

import java.util.List; /* note
 */
class X {
	void m() {}
}
// === end ===

// === case: skips_static_import_no_dot ===
// target: line=2 col=0
package x;

import static Foo;
class X { Foo f; }
// === end ===

// === case: skips_tab_around_dot_in_fqn ===
// target: line=2 col=0
package x;

import foo	.	Bar;
class X { Bar b; }
// === end ===

// === case: skips_when_java_lang_subpackage_simple_name_now_used ===
// target: line=2 col=0
package x;

import java.lang.invoke.MethodHandle;
class X { MethodHandle h; }
// === end ===

// === case: skips_when_simple_name_now_used ===
// target: line=2 col=0
package x;

import a.b.Foo;
class X { Foo f; }
// === end ===

// === case: skips_whitespace_around_dot_in_fqn ===
// target: line=2 col=0
package x;

import foo . Bar ;
class X { Bar b; }
// === end ===
