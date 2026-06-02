package com.etk2000.checkstyle.inputs.unusedimports;

// === case: deletes_comment_matched_does_not_keep_import ===
// imports: import java.util.List; // violation: Unused import - java.util.List.
class InputUnusedImportsCommentMatched {
	// once used a List here
}
// === end ===

// === case: deletes_fqn_usage_does_not_keep_static_import ===
// imports: import static org.junit.jupiter.api.Assertions.assertTrue; // violation: Unused import - org.junit.jupiter.api.Assertions.assertTrue.
class InputUnusedImportsFqnUsageStaticImport {
	void m(boolean f) { org.junit.jupiter.api.Assertions.assertTrue(f); }
}
// === end ===

// === case: deletes_java_lang_import_even_when_simple_name_used ===
// imports: import java.lang.String; // violation: Unused import - java.lang.String.
class InputUnusedImportsJavaLangSimpleNameUsed { String s; }
// === end ===

// === case: deletes_string_literal_matched_does_not_keep_import ===
// imports: import java.util.List; // violation: Unused import - java.util.List.
class InputUnusedImportsStringLiteralMatched {
	String doc = "List of things";
}
// === end ===

// === case: deletes_unused_regular_import ===
// imports: import java.util.List; // violation: Unused import - java.util.List.
class InputUnusedImportsUnusedRegular {
	void m() {}
}
// === end ===

// === case: deletes_unused_static_import ===
// imports: import static java.util.Collections.emptyList; // violation: Unused import - java.util.Collections.emptyList.
class InputUnusedImportsUnusedStatic {
	void m() {}
}
// === end ===

// === case: deletes_when_package_qualifier_contains_simple_name ===
// package: com.List.x
// imports: import java.util.List; // violation: Unused import - java.util.List.
class InputUnusedImportsPackageQualifier {
	void m() {}
}
// === end ===

// === case: deletes_whitespace_around_dot_in_fqn ===
// imports: import java . util . List ; // violation: Unused import - java.util.List.
class InputUnusedImportsWhitespaceDot {
	void m() {}
}
// === end ===