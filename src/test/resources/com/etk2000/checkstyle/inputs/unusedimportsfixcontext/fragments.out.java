// === case: deletes_empty_line_from_cascade ===
package x;

class X {
	void m() {}
}
// === end ===

// === case: deletes_identifier_containing_simple_name_as_prefix ===
package x;

class X {
	void m() { fooBar(); }
}
// === end ===

// === case: deletes_identifier_containing_simple_name_as_suffix ===
package x;

class X {
	void m() { barfoo(); }
}
// === end ===

// === case: deletes_import_with_block_comment_in_fqn ===
package x;

class X {
	void m() {}
}
// === end ===

// === case: deletes_import_with_trailing_line_comment ===
package x;

class X {
	void m() {}
}
// === end ===

// === case: deletes_static_wildcard_import_without_reverify ===
package x;

class X { Entry<String, String> e; }
// === end ===

// === case: deletes_wildcard_import_with_comment ===
package x;

class X { List<String> l; }
// === end ===

// === case: deletes_wildcard_import_without_reverify ===
package x;

class X { List<String> l; }
// === end ===
