package com.etk2000.checkstyle.inputs.preferexactassertion;

// === case: junit4_assert_false_equal ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertFalse
class Junit4AssertFalseEqualSlice {
	void m() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: junit4_assert_false_gt ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertFalse
class Junit4AssertFalseGtSlice {
	void m() {
		assertFalse(1 > 0);
	}
}
// === end ===

// === case: junit4_assert_false_negated_unqualified ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class Junit4AssertFalseNegatedUnqualifiedSlice {
	void m(boolean flag) {
		assertTrue(flag);
	}
}
// === end ===

// === case: junit4_assert_true_equal ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class Junit4AssertTrueEqualSlice {
	void m() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: junit4_assert_true_gt ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class Junit4AssertTrueGtSlice {
	void m() {
		assertTrue(1 > 0);
	}
}
// === end ===

// === case: junit4_assert_true_lt ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class Junit4AssertTrueLtSlice {
	void m() {
		assertTrue(0 < 1);
	}
}
// === end ===

// === case: junit4_assert_true_negated_unqualified ===
// imports: static org.junit.Assert.assertFalse
// imports: static org.junit.Assert.assertTrue
class Junit4AssertTrueNegatedUnqualifiedSlice {
	void m(boolean flag) {
		assertFalse(flag);
	}
}
// === end ===

// === case: junit4_assert_true_not_equal ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class Junit4AssertTrueNotEqualSlice {
	void m() {
		assertTrue(1 != 2);
	}
}
// === end ===

// === case: junit4_message_first_assert_false_comparison_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertFalse
class Junit4MessageFirstAssertFalseComparisonFiresSlice {
	void m() {
		assertFalse("should be negative", 1 >= 0);
	}
}
// === end ===

// === case: junit4_message_first_comparison_fires ===
// skip-reason: complex assertion form
// imports: static org.junit.Assert.assertTrue
class Junit4MessageFirstComparisonFiresSlice {
	void m() {
		assertTrue("should be positive", 1 > 0);
	}
}
// === end ===

// === case: junit4_qualified_junit5_assertions_assert_false_instanceof_still_fires ===
class Junit4QualifiedJunit5AssertionsAssertFalseInstanceofStillFiresSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertNotInstanceOf(Integer.class, o);
	}
}
// === end ===

// === case: junit4_qualified_junit5_assertions_instanceof_still_fires_under_j4_imports ===
class Junit4QualifiedJunit5AssertionsInstanceofStillFiresUnderJ4ImportsSlice {
	void m(Object o) {
		org.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===

// === case: junit4_qualified_junit5_assertions_simple_name_instanceof_still_fires ===
// imports: org.junit.jupiter.api.Assertions
class Junit4QualifiedJunit5AssertionsSimpleNameInstanceofStillFiresSlice {
	void m(Object o) {
		Assertions.assertInstanceOf(String.class, o);
	}
}
// === end ===