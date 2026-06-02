// === case: deletes_sibling_import_line_containing_simple_name ===
// target: line=2 col=0
package x;

import static a.b.foo;
import com.foo;
class X {
	void m() {}
}
// === end ===

// === case: skips_blank_whitespace_line ===
// target: line=1 col=0
package x;
	
class X {}
// === end ===

// === case: skips_import_trailing_dot ===
// target: line=2 col=0
package x;

import foo.;
class X {}
// === end ===

// === case: skips_malformed_import_line ===
// target: line=2 col=0
package x;

int notAnImport = 5;
class X {}
// === end ===

// === case: skips_multi_statement_import_line ===
// target: line=2 col=0
package x;

import a.b;c.d;
class X {}
// === end ===
