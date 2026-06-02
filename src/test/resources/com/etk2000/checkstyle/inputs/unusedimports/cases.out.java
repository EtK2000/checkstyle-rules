package com.etk2000.checkstyle.inputs.unusedimports;

// === case: deletes_comment_matched_does_not_keep_import ===
class InputUnusedImportsCommentMatched {
	// once used a List here
}
// === end ===

// === case: deletes_fqn_usage_does_not_keep_static_import ===
class InputUnusedImportsFqnUsageStaticImport {
	void m(boolean f) { org.junit.jupiter.api.Assertions.assertTrue(f); }
}
// === end ===

// === case: deletes_java_lang_import_even_when_simple_name_used ===
class InputUnusedImportsJavaLangSimpleNameUsed { String s; }
// === end ===

// === case: deletes_string_literal_matched_does_not_keep_import ===
class InputUnusedImportsStringLiteralMatched {
	String doc = "List of things";
}
// === end ===

// === case: deletes_unused_regular_import ===
class InputUnusedImportsUnusedRegular {
	void m() {}
}
// === end ===

// === case: deletes_unused_static_import ===
class InputUnusedImportsUnusedStatic {
	void m() {}
}
// === end ===

// === case: deletes_when_package_qualifier_contains_simple_name ===
// package: com.List.x
class InputUnusedImportsPackageQualifier {
	void m() {}
}
// === end ===

// === case: deletes_whitespace_around_dot_in_fqn ===
class InputUnusedImportsWhitespaceDot {
	void m() {}
}
// === end ===