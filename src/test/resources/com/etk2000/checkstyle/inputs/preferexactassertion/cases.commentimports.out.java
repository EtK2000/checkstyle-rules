package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: import_line_with_comment_inside_qualified_name_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: import static org.junit.jupiter.api./* v5 */Assertions.assertTrue;
class ImportLineWithCommentInsideQualifiedNameResolvesSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: import_line_with_comment_on_opposite_import_skips_add ===
// imports: import static org.junit.jupiter.api.Assertions.assertFalse; // already in scope
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class ImportLineWithCommentOnOppositeImportSkipsAddSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_with_leading_block_comment_negation_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: /* legacy */ import static org.junit.jupiter.api.Assertions.assertTrue;
class ImportLineWithLeadingBlockCommentNegationResolvesSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_with_line_comment_naming_junit4_import_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: import static org.junit.jupiter.api.Assertions.assertTrue; // was import static org.junit.Assert.*;
class ImportLineWithLineCommentNamingJunit4ImportResolvesSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_with_tab_separators_negation_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertFalse
// imports: import  static		org.junit.jupiter.api.Assertions.assertTrue;
class ImportLineWithTabSeparatorsNegationResolvesSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: import_line_with_trailing_line_comment_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: import static org.junit.jupiter.api.Assertions.assertTrue; // in scope for the suite
class ImportLineWithTrailingLineCommentResolvesSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: import_line_with_unterminated_block_comment_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: import static org.junit.jupiter.api.Assertions.assertTrue; /* note that opens here
/* and closes on this line */
class ImportLineWithUnterminatedBlockCommentResolvesSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: import_line_with_url_in_block_comment_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: import static org.junit.jupiter.api.Assertions.assertTrue; /* see https://example.com */
class ImportLineWithUrlInBlockCommentResolvesSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: import_line_with_whitespace_around_dots_resolves ===
// imports: static org.junit.jupiter.api.Assertions.assertInstanceOf
// imports: import static org . junit . jupiter . api . Assertions . assertTrue ;
class ImportLineWithWhitespaceAroundDotsResolvesSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: junit4_import_line_with_trailing_comment_negation_resolves ===
// imports: static org.junit.Assert.assertFalse
// imports: import static org.junit.Assert.assertTrue; // junit 4 only
class Junit4ImportLineWithTrailingCommentNegationResolvesSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: junit4_wildcard_with_comment_before_specific_import_skips_add ===
// imports: import static org.junit./* junit 4 */Assert.*;
// imports: static org.junit.Assert.assertTrue
class Junit4WildcardWithCommentBeforeSpecificImportSkipsAddSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: junit5_wildcard_with_comment_before_static_assert_import_skips_add ===
// imports: import static org.junit.jupiter.api./* v5 */Assertions.*;
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class Junit5WildcardWithCommentBeforeStaticAssertImportSkipsAddSlice {
	void m(Object o) {
		assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: negated_wildcard_with_comment_before_specific_import_skips_add ===
// imports: import static org.junit.jupiter.api.Assertions.*; /* everything in scope */
// imports: static org.junit.jupiter.api.Assertions.assertTrue
class NegatedWildcardWithCommentBeforeSpecificImportSkipsAddSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: negation_both_frameworks_comment_bearing_imports_ambiguous ===
// skip-reason: cannot rewrite the assertion: both JUnit 4 and JUnit 5 assertions are statically imported
// imports: /* junit 4 */ import static org.junit.Assert.assertTrue;
// imports: import static org.junit.jupiter.api.Assertions.assertFalse; // junit 5
class NegationBothFrameworksCommentBearingImportsAmbiguousSlice {
	void m(boolean flag) {
		assertTrue(!flag);
		// keeps the JUnit 5 import in use, so the unused-import fixer cannot resolve the ambiguity first
		assertFalse(flag);
	}
}
// === end ===