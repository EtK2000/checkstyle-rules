package com.etk2000.checkstyle.inputs.preferspecificapi;

// === case: as_list_multiple_args ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListAsListMultipleArgsSliceViolation {
	void asListMultipleArgs() {
		final var list = List.of("a", "b", "c");
	}
}
// === end ===

// === case: as_list_no_args ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListAsListNoArgsSliceViolation {
	void asListNoArgs() {
		final var list = List.of();
	}
}
// === end ===

// === case: as_list_pattern_in_string_not_anchored ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListAsListPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		final var list = "Arrays.asList(" + List.of(s);
	}
}
// === end ===

// === case: as_list_single_arg ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListAsListSingleArgSliceViolation {
	void asListSingleArg(String s) {
		final var list = List.of(s);
	}
}
// === end ===

// === case: as_list_text_block_continuation ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListAsListTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + List.of("a").toString();
	}
}
// === end ===

// === case: assert_comment_before_kept_argument_rejected ===
// skip-reason: cannot rewrite: unrecognized API pattern
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertCommentBeforeKeptArgumentRejectedSliceViolation {
	void assertCommentBeforeKeptArgumentRejected(Object x) {
		assertEquals(null, /* note */x);
	}
}
// === end ===

// === case: assert_comment_before_lparen_rejected ===
// skip-reason: cannot rewrite: unrecognized API pattern
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertCommentBeforeLparenRejectedSliceViolation {
	void assertCommentBeforeLparenRejected(Object x) {
		assertEquals/* note */(null, x);
	}
}
// === end ===

// === case: assert_comment_inside_qualified_receiver ===
class InputSpecificApiAssertCommentInsideQualifiedReceiverSliceViolation {
	void assertCommentInsideQualifiedReceiver(Object x) {
		org.junit./* note */Assert.assertNull(x);
	}
}
// === end ===

// === case: assert_donor_after_unrelated_wildcard ===
// imports: static java.util.Map.*
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertDonorAfterUnrelatedWildcardSliceViolation {
	void assertDonorAfterUnrelatedWildcard(Object value) {
		assertNull(value);
	}
}
// === end ===

// === case: assert_donor_ignores_foreign_assertion_import ===
// imports: static org.assertj.core.api.Assertions.assertThat
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertDonorIgnoresForeignAssertionImportSliceViolation {
	void assertDonorIgnoresForeignAssertionImport(Object result) {
		assertThat(result);
		assertNull(result);
	}
}
// === end ===

// === case: assert_donor_scan_ignores_text_block ===
// imports: static org.junit.Assert.*
class InputSpecificApiAssertDonorScanIgnoresTextBlockSliceViolation {
	static final String SNIPPET = """
			import static com.evil.Fake.assertEquals;
			""";

	void assertDonorScanIgnoresTextBlock(Object x) {
		assertNull(x);
	}
}
// === end ===

// === case: assert_equals_false ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertEqualsFalseSliceViolation {
	void assertEqualsFalse() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: assert_equals_false_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertEqualsFalseReversedSliceViolation {
	void assertEqualsFalseReversed() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: assert_equals_false_wildcard_import ===
// imports: static org.junit.Assert.*
class InputSpecificApiAssertAssertEqualsFalseWildcardImportSliceViolation {
	void assertEqualsFalseWildcardImport(boolean result) {
		assertFalse(result);
	}
}
// === end ===

// === case: assert_equals_null ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertEqualsNullSliceViolation {
	void assertEqualsNull() {
		assertNull(new Object());
	}
}
// === end ===

// === case: assert_equals_null_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertEqualsNullReversedSliceViolation {
	void assertEqualsNullReversed() {
		assertNull(new Object());
	}
}
// === end ===

// === case: assert_equals_true ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertEqualsTrueSliceViolation {
	void assertEqualsTrue() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_equals_true_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertEqualsTrueReversedSliceViolation {
	void assertEqualsTrueReversed() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_equals_with_message_false ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertEqualsWithMessageFalseSliceViolation {
	void assertEqualsWithMessageFalse() {
		assertFalse("msg", 1 == 2);
	}
}
// === end ===

// === case: assert_equals_with_message_false_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertEqualsWithMessageFalseReversedSliceViolation {
	void assertEqualsWithMessageFalseReversed() {
		assertFalse("msg", 1 == 2);
	}
}
// === end ===

// === case: assert_equals_with_message_null ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertEqualsWithMessageNullSliceViolation {
	void assertEqualsWithMessageNull() {
		assertNull("msg", new Object());
	}
}
// === end ===

// === case: assert_equals_with_message_null_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertEqualsWithMessageNullReversedSliceViolation {
	void assertEqualsWithMessageNullReversed() {
		assertNull("msg", new Object());
	}
}
// === end ===

// === case: assert_equals_with_message_true ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertEqualsWithMessageTrueSliceViolation {
	void assertEqualsWithMessageTrue() {
		assertTrue("msg", 1 == 1);
	}
}
// === end ===

// === case: assert_equals_with_message_true_reversed ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertEqualsWithMessageTrueReversedSliceViolation {
	void assertEqualsWithMessageTrueReversed() {
		assertTrue("msg", 1 == 1);
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_false ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertEqualsWithTrailingMessageFalseSliceViolation {
	void assertEqualsWithTrailingMessageFalse() {
		assertFalse(1 == 2, "msg");
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_null ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertEqualsWithTrailingMessageNullSliceViolation {
	void assertEqualsWithTrailingMessageNull() {
		assertNull(new Object(), "msg");
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_true ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertEqualsWithTrailingMessageTrueSliceViolation {
	void assertEqualsWithTrailingMessageTrue() {
		assertTrue(1 == 1, "msg");
	}
}
// === end ===

// === case: assert_literal_first_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertLiteralFirstPatternInCommentNotAnchoredSliceViolation {
	void m() {
		/* assertEquals(true, x) */ assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_literal_last_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertLiteralLastPatternInCommentNotAnchoredSliceViolation {
	void m(boolean b) {
		/* assertEquals(x, true) */ assertTrue(b);
	}
}
// === end ===

// === case: assert_literal_middle_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertLiteralMiddlePatternInCommentNotAnchoredSliceViolation {
	void m() {
		/* assertEquals(a, null, b) */ assertNull("msg", new Object());
	}
}
// === end ===

// === case: assert_not_equals_false ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertNotEqualsFalseSliceViolation {
	void assertNotEqualsFalse() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_not_equals_false_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertNotEqualsFalseReversedSliceViolation {
	void assertNotEqualsFalseReversed() {
		assertTrue(1 == 1);
	}
}
// === end ===

// === case: assert_not_equals_null ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotEqualsNullSliceViolation {
	void assertNotEqualsNull() {
		assertNotNull(new Object());
	}
}
// === end ===

// === case: assert_not_equals_null_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotEqualsNullReversedSliceViolation {
	void assertNotEqualsNullReversed() {
		assertNotNull(new Object());
	}
}
// === end ===

// === case: assert_not_equals_true ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertNotEqualsTrueSliceViolation {
	void assertNotEqualsTrue() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: assert_not_equals_true_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertNotEqualsTrueReversedSliceViolation {
	void assertNotEqualsTrueReversed() {
		assertFalse(1 == 2);
	}
}
// === end ===

// === case: assert_not_equals_with_message_false ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertNotEqualsWithMessageFalseSliceViolation {
	void assertNotEqualsWithMessageFalse() {
		assertTrue("msg", 1 == 1);
	}
}
// === end ===

// === case: assert_not_equals_with_message_false_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertNotEqualsWithMessageFalseReversedSliceViolation {
	void assertNotEqualsWithMessageFalseReversed() {
		assertTrue("msg", 1 == 1);
	}
}
// === end ===

// === case: assert_not_equals_with_message_null ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotEqualsWithMessageNullSliceViolation {
	void assertNotEqualsWithMessageNull() {
		assertNotNull("msg", new Object());
	}
}
// === end ===

// === case: assert_not_equals_with_message_null_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotEqualsWithMessageNullReversedSliceViolation {
	void assertNotEqualsWithMessageNullReversed() {
		assertNotNull("msg", new Object());
	}
}
// === end ===

// === case: assert_not_equals_with_message_true ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertNotEqualsWithMessageTrueSliceViolation {
	void assertNotEqualsWithMessageTrue() {
		assertFalse("msg", 1 == 2);
	}
}
// === end ===

// === case: assert_not_equals_with_message_true_reversed ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertNotEqualsWithMessageTrueReversedSliceViolation {
	void assertNotEqualsWithMessageTrueReversed() {
		assertFalse("msg", 1 == 2);
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_false ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertTrue
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageFalseSliceViolation {
	void assertNotEqualsWithTrailingMessageFalse() {
		assertTrue(1 == 1, "msg");
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_null ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageNullSliceViolation {
	void assertNotEqualsWithTrailingMessageNull() {
		assertNotNull(new Object(), "msg");
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_true ===
// imports: static org.junit.Assert.assertNotEquals
// imports: static org.junit.Assert.assertFalse
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageTrueSliceViolation {
	void assertNotEqualsWithTrailingMessageTrue() {
		assertFalse(1 == 2, "msg");
	}
}
// === end ===

// === case: assert_not_same_null ===
// imports: static org.junit.Assert.assertNotSame
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotSameNullSliceViolation {
	void assertNotSameNull() {
		assertNotNull(new Object());
	}
}
// === end ===

// === case: assert_not_same_null_reversed ===
// imports: static org.junit.Assert.assertNotSame
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotSameNullReversedSliceViolation {
	void assertNotSameNullReversed() {
		assertNotNull(new Object());
	}
}
// === end ===

// === case: assert_not_same_with_message ===
// imports: static org.junit.Assert.assertNotSame
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotSameWithMessageSliceViolation {
	void assertNotSameWithMessage() {
		assertNotNull("msg", new Object());
	}
}
// === end ===

// === case: assert_not_same_with_message_reversed ===
// imports: static org.junit.Assert.assertNotSame
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotSameWithMessageReversedSliceViolation {
	void assertNotSameWithMessageReversed() {
		assertNotNull("msg", new Object());
	}
}
// === end ===

// === case: assert_not_same_with_trailing_message ===
// imports: static org.junit.Assert.assertNotSame
// imports: static org.junit.Assert.assertNotNull
class InputSpecificApiAssertAssertNotSameWithTrailingMessageSliceViolation {
	void assertNotSameWithTrailingMessage() {
		assertNotNull(new Object(), "msg");
	}
}
// === end ===

// === case: assert_same_null ===
// imports: static org.junit.Assert.assertSame
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertSameNullSliceViolation {
	void assertSameNull() {
		assertNull(new Object());
	}
}
// === end ===

// === case: assert_same_null_reversed ===
// imports: static org.junit.Assert.assertSame
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertSameNullReversedSliceViolation {
	void assertSameNullReversed() {
		assertNull(new Object());
	}
}
// === end ===

// === case: assert_same_with_message ===
// imports: static org.junit.Assert.assertSame
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertSameWithMessageSliceViolation {
	void assertSameWithMessage() {
		assertNull("msg", new Object());
	}
}
// === end ===

// === case: assert_same_with_message_reversed ===
// imports: static org.junit.Assert.assertSame
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertSameWithMessageReversedSliceViolation {
	void assertSameWithMessageReversed() {
		assertNull("msg", new Object());
	}
}
// === end ===

// === case: assert_same_with_trailing_message ===
// imports: static org.junit.Assert.assertSame
// imports: static org.junit.Assert.assertNull
class InputSpecificApiAssertAssertSameWithTrailingMessageSliceViolation {
	void assertSameWithTrailingMessage() {
		assertNull(new Object(), "msg");
	}
}
// === end ===

// === case: assert_two_calls_in_lambdas ===
// imports: static org.junit.jupiter.api.Assertions.assertAll
// imports: static org.junit.jupiter.api.Assertions.assertEquals
// imports: static org.junit.jupiter.api.Assertions.assertNull
class InputSpecificApiAssertTwoCallsInLambdasSliceViolation {
	void assertTwoCallsInLambdas(Object a, Object b, Object c) {
		assertAll(() -> assertEquals(a, b), () -> assertNull(c));
	}
}
// === end ===

// === case: chained_call_get_zero ===
class InputSpecificApiReflectionChainedCallGetZeroSliceViolation {
	void chainedCallGetZero() {
		System.out.println(getList().getFirst());
	}
}
// === end ===

// === case: chained_call_index_of_contains ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfChainedCallIndexOfContainsSliceViolation {
	void chainedCallIndexOfContains() {
		if (getName().indexOf("foo") != -1)
			System.out.println("found");
	}
}
// === end ===

// === case: chained_call_resolved_get_zero ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiReflectionChainedCallResolvedGetZeroSliceViolation {
	void chainedCallResolvedGetZero(List<String> list) {
		System.out.println(Collections.synchronizedList(list).getFirst());
	}
}
// === end ===

// === case: chained_call_resolved_index_of_char ===
class InputSpecificApiIndexOfCharChainedCallResolvedIndexOfCharSliceViolation {
	void chainedCallResolvedIndexOfChar(Object obj) {
		final var i = obj.toString().indexOf('x');
		System.out.println(i);
	}
}
// === end ===

// === case: chained_call_resolved_remove_first ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiReflectionChainedCallResolvedRemoveFirstSliceViolation {
	void chainedCallResolvedRemoveFirst(List<String> list) {
		Collections.synchronizedList(list).removeFirst();
	}
}
// === end ===

// === case: char_sequence_length_equals_zero ===
class InputSpecificApiIsEmptyCharSequenceLengthEqualsZeroSliceViolation {
	void m(CharSequence cs) {
		if (cs.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: clean_char_arg ===
class InputSpecificApiIndexOfCharCleanCharArgSliceViolation {
	void cleanCharArg(String s) {
		final var i = s.indexOf('x');
		System.out.println(i);
	}
}
// === end ===

// === case: clean_empty_string ===
class InputSpecificApiIndexOfCharCleanEmptyStringSliceViolation {
	void cleanEmptyString(String s) {
		// empty string has no char-literal equivalent
		final var i = s.indexOf("");
		System.out.println(i);
	}
}
// === end ===

// === case: clean_multi_char_string ===
class InputSpecificApiIndexOfCharCleanMultiCharStringSliceViolation {
	void cleanMultiCharString(String s) {
		final var i = s.indexOf("xy");
		System.out.println(i);
	}
}
// === end ===

// === case: clean_variable_arg ===
class InputSpecificApiIndexOfCharCleanVariableArgSliceViolation {
	void cleanVariableArg(String s, String needle) {
		final var i = s.indexOf(needle);
		System.out.println(i);
	}
}
// === end ===

// === case: collect_pattern_in_string_not_anchored ===
// imports: java.util.List
// imports: java.util.stream.Collectors
class InputSpecificApiToListCollectPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var result = ".collect(Collectors.toList())" + list.stream().toList();
	}
}
// === end ===

// === case: collect_to_list ===
// imports: java.util.List
// imports: java.util.stream.Collectors
class InputSpecificApiToListCollectToListSliceViolation {
	void collectToList(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
// === end ===

// === case: collect_to_unmodifiable_list ===
// imports: java.util.List
// imports: java.util.stream.Collectors
class InputSpecificApiToListCollectToUnmodifiableListSliceViolation {
	void collectToUnmodifiableList(List<String> list) {
		final var result = list.stream()
				.filter(s -> !s.isEmpty())
				.toList();
	}
}
// === end ===

// === case: compact_constructor_scope ===
// skip-reason: cannot rewrite: unrecognized API pattern
record InputSpecificApiIndexOfCompactConstructorScopeSliceViolation(String value) {
	InputSpecificApiIndexOfCompactConstructorScopeSliceViolation {
		if (value.indexOf("foo") != -1)
			throw new IllegalArgumentException();
	}
}
// === end ===

// === case: constructor_scope ===
class InputSpecificApiIsEmptyConstructorScopeSliceViolation {
	InputSpecificApiIsEmptyConstructorScopeSliceViolation(String value) {
		if (value.isEmpty())
			throw new IllegalArgumentException();
	}
}
// === end ===

// === case: double_quote_escape ===
class InputSpecificApiIndexOfCharDoubleQuoteEscapeSliceViolation {
	void doubleQuoteEscape(String s) {
		final var i = s.indexOf('"');
		System.out.println(i);
	}
}
// === end ===

// === case: empty_list ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsEmptyEmptyListSliceViolation {
	void emptyList() {
		final var list = List.of();
	}
}
// === end ===

// === case: empty_list_pattern_in_string_not_anchored ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsEmptyEmptyListPatternInStringNotAnchoredSliceViolation {
	void m() {
		final var x = "Collections.emptyList()" + List.of();
	}
}
// === end ===

// === case: empty_map ===
// imports: java.util.Collections
// imports: java.util.Map
class InputSpecificApiCollectionsEmptyEmptyMapSliceViolation {
	void emptyMap() {
		final var map = Map.of();
	}
}
// === end ===

// === case: empty_set ===
// imports: java.util.Collections
// imports: java.util.Set
class InputSpecificApiCollectionsEmptyEmptySetSliceViolation {
	void emptySet() {
		final var set = Set.of();
	}
}
// === end ===

// === case: equals_empty ===
class InputSpecificApiStringMethodEqualsEmptySliceViolation {
	void equalsEmpty(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: equals_empty_field_read_through_a_same_file_variable ===
class InputSpecificApiStringMethodEqualsEmptyFieldReadThroughASameFileVariableSliceViolation {
	String name = "";

	void m(InputSpecificApiStringMethodEqualsEmptyFieldReadThroughASameFileVariableSliceViolation other) {
		System.out.println(other.name.isEmpty());
	}
}
// === end ===

// === case: equals_empty_foreign_receiver_rejected ===
class InputSpecificApiStringMethodEqualsEmptyForeignReceiverRejectedSliceViolation {
	void m(Object o, String s) {
		if (o.equals("") || s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: equals_empty_package_private_member_string_other_package ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathPackagePrivateString
class InputSpecificApiStringMethodEqualsEmptyPackagePrivateMemberStringOtherPackageSliceViolation extends ClasspathPackagePrivateString {
	boolean empty(String s) {
		return s.isEmpty();
	}
}
// === end ===

// === case: equals_empty_package_private_member_string_same_package ===
// package: com.etk2000.checkstyle.testhelpers
class InputSpecificApiStringMethodEqualsEmptyPackagePrivateMemberStringSamePackageSliceViolation extends ClasspathPackagePrivateString {
	boolean empty(String s) {
		return s.equals("");
	}

	boolean emptyOf(Object o) {
		return o.toString().isEmpty();
	}
}
// === end ===

// === case: equals_empty_pattern_in_comment_not_anchored ===
class InputSpecificApiStringMethodEqualsEmptyPatternInCommentNotAnchoredSliceViolation {
	void m(String s) {
		if (/* .equals("") */ s.isEmpty())
			System.out.println("x");
	}
}
// === end ===

// === case: equals_empty_qualified_string_parameter ===
class InputSpecificApiStringMethodEqualsEmptyQualifiedStringParameterSliceViolation {
	void m(java.lang.String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: equals_empty_same_file_call_returning_string ===
class InputSpecificApiStringMethodEqualsEmptySameFileCallReturningStringSliceViolation {
	String getName() {
		return "";
	}

	void m() {
		System.out.println(getName().isEmpty());
	}
}
// === end ===

// === case: equals_empty_string_builder_to_string ===
class InputSpecificApiStringMethodEqualsEmptyStringBuilderToStringSliceViolation {
	void m(StringBuilder sb) {
		if (sb.toString().isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: equals_empty_this_qualified_string_field ===
class InputSpecificApiStringMethodEqualsEmptyThisQualifiedStringFieldSliceViolation {
	String name = "";

	void m(String name) {
		if (this.name.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: escape_backslash ===
class InputSpecificApiIndexOfCharEscapeBackslashSliceViolation {
	void escapeBackslash(String s) {
		final var i = s.indexOf('\\');
		System.out.println(i);
	}
}
// === end ===

// === case: escape_newline ===
class InputSpecificApiIndexOfCharEscapeNewlineSliceViolation {
	void escapeNewline(String s) {
		final var i = s.indexOf('\n');
		System.out.println(i);
	}
}
// === end ===

// === case: format_block_comment ===
class InputSpecificApiStringFormatFormatBlockCommentSliceViolation {
	void formatBlockComment(String name) {
		final var s = "Hi %s".formatted(name /* ) */);
	}
}
// === end ===

// === case: format_char_literal_in_args ===
class InputSpecificApiStringFormatFormatCharLiteralInArgsSliceViolation {
	void formatCharLiteralInArgs() {
		final var s = "%c".formatted(')');
	}
}
// === end ===

// === case: format_escaped_quotes ===
class InputSpecificApiStringFormatFormatEscapedQuotesSliceViolation {
	void formatEscapedQuotes(String name) {
		final var s = "Say \"hi\"".formatted(name);
	}
}
// === end ===

// === case: format_nested_parens ===
class InputSpecificApiStringFormatFormatNestedParensSliceViolation {
	void formatNestedParens() {
		final var s = "Hello %s".formatted(getName());
	}

	String getName() {
		return "x";
	}
}
// === end ===

// === case: format_non_literal_first_rejected ===
class InputSpecificApiStringFormatNonLiteralFirstRejectedSliceViolation {
	void m(String fmt, String a) {
		final var s = String.format(fmt, a) + "%s".formatted(a);
	}
}
// === end ===

// === case: format_one_arg ===
class InputSpecificApiStringFormatFormatOneArgSliceViolation {
	void formatOneArg(String name) {
		final var s = "Hello %s".formatted(name);
	}
}
// === end ===

// === case: format_pattern_in_string_not_anchored ===
class InputSpecificApiStringFormatFormatPatternInStringNotAnchoredSliceViolation {
	void m(int n) {
		final var s = "String.format(" + "%d".formatted(n);
	}
}
// === end ===

// === case: format_single_literal ===
class InputSpecificApiStringFormatFormatSingleLiteralSliceViolation {
	void formatSingleLiteral() {
		final var s = "literal";
	}
}
// === end ===

// === case: format_string_literal_paren ===
class InputSpecificApiStringFormatFormatStringLiteralParenSliceViolation {
	void formatStringLiteralParen(String name) {
		final var s = "Result (%s)".formatted(name);
	}
}
// === end ===

// === case: format_strip_empty_literal ===
class InputSpecificApiStringFormatFormatStripEmptyLiteralSliceViolation {
	void formatStripEmptyLiteral() {
		final var s = "";
	}
}
// === end ===

// === case: format_strip_escape_backslash ===
class InputSpecificApiStringFormatFormatStripEscapeBackslashSliceViolation {
	void formatStripEscapeBackslash() {
		final var s = "dir\\";
	}
}
// === end ===

// === case: format_strip_escape_newline ===
class InputSpecificApiStringFormatFormatStripEscapeNewlineSliceViolation {
	void formatStripEscapeNewline() {
		final var s = "a\nb";
	}
}
// === end ===

// === case: format_strip_escape_space ===
class InputSpecificApiStringFormatFormatStripEscapeSpaceSliceViolation {
	void formatStripEscapeSpace() {
		final var s = "\s";
	}
}
// === end ===

// === case: format_strip_escaped_apostrophe ===
class InputSpecificApiStringFormatFormatStripEscapedApostropheSliceViolation {
	void formatStripEscapedApostrophe() {
		final var s = "it\'s";
	}
}
// === end ===

// === case: format_strip_escaped_quotes ===
class InputSpecificApiStringFormatFormatStripEscapedQuotesSliceViolation {
	void formatStripEscapedQuotes() {
		final var s = "say \"hi\"";
	}
}
// === end ===

// === case: format_strip_octal_dollar ===
class InputSpecificApiStringFormatFormatStripOctalDollarSliceViolation {
	void formatStripOctalDollar() {
		final var s = "\044";
	}
}
// === end ===

// === case: format_strip_unicode_dollar ===
class InputSpecificApiStringFormatFormatStripUnicodeDollarSliceViolation {
	void formatStripUnicodeDollar() {
		final var s = "\u0024";
	}
}
// === end ===

// === case: format_strip_unicode_escaped_backslash_newline ===
class InputSpecificApiStringFormatFormatStripUnicodeEscapedBackslashNewlineSliceViolation {
	void m() {
		final var s = "\u005cn";
	}
}
// === end ===

// === case: format_trailing_backslash_in_literal ===
class InputSpecificApiStringFormatFormatTrailingBackslashInLiteralSliceViolation {
	void formatTrailingBackslashInLiteral(String path) {
		final var s = "dir\\".formatted(path);
	}
}
// === end ===

// === case: format_two_args ===
class InputSpecificApiStringFormatFormatTwoArgsSliceViolation {
	void formatTwoArgs(String name, int age) {
		final var s = "Hello %s, age %d".formatted(name, age);
	}
}
// === end ===

// === case: get_last_foreign_receiver_rejected ===
// imports: java.util.List
// imports: java.util.Map
class InputSpecificApiGetLastForeignReceiverRejectedSliceViolation {
	Map<Integer, String> map;

	boolean getLastForeignReceiverRejected(List<String> list) {
		return map.get(map.size() - 1) == list.getLast();
	}
}
// === end ===

// === case: get_last_side_effect_receiver ===
// imports: java.util.List
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiGetLastSideEffectReceiverSliceViolation {
	String getLastSideEffectReceiver() {
		return getList().get(getList().size() - 1);
	}

	List<String> getList() {
		return List.of("a");
	}
}
// === end ===

// === case: get_size_minus_one ===
// imports: java.util.List
class InputSpecificApiGetSizeMinusOneSliceViolation {
	void getSizeMinusOne(List<String> list) {
		System.out.println(list.getLast());
	}
}
// === end ===

// === case: get_size_minus_one_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiGetSizeMinusOnePatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = "list.get(list.size() - 1)" + list.getLast();
	}
}
// === end ===

// === case: get_zero ===
// imports: java.util.List
class InputSpecificApiGetZeroSliceViolation {
	void getZero(List<String> list) {
		System.out.println(list.getFirst());
	}
}
// === end ===

// === case: get_zero_foreign_receiver_rejected ===
// imports: java.util.List
// imports: java.util.Map
class InputSpecificApiGetZeroForeignReceiverRejectedSliceViolation {
	String m(List<String> list, Map<Integer, String> map) {
		return map.get(0) + list.getFirst();
	}
}
// === end ===

// === case: get_zero_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiGetZeroPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".get(0)" + list.getFirst();
	}
}
// === end ===

// === case: index_of_char_octal_apostrophe ===
class InputSpecificApiIndexOfCharOctalApostropheSliceViolation {
	void octalApostrophe(String s) {
		final var i = s.indexOf('\47');
		System.out.println(i);
	}
}
// === end ===

// === case: index_of_char_refuses_unicode_apostrophe ===
class InputSpecificApiIndexOfCharRefusesUnicodeApostropheSliceViolation {
	void refusesUnicodeApostrophe(String s) {
		final var i = s.indexOf("\u0027");
		System.out.println(i);
	}
}
// === end ===

// === case: index_of_char_unicode_escape ===
class InputSpecificApiIndexOfCharUnicodeEscapeSliceViolation {
	void unicodeEscape(String s) {
		final var i = s.indexOf('\u0041');
		System.out.println(i);
	}
}
// === end ===

// === case: index_of_equal_neg_one ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfEqualNegOneSliceViolation {
	void indexOfEqualNegOne(String s) {
		if (s.indexOf("baz") == -1)
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_from_index ===
class InputSpecificApiIndexOfCharIndexOfFromIndexSliceViolation {
	void indexOfFromIndex(String s) {
		final var i = s.indexOf('x', 5);
		System.out.println(i);
	}
}
// === end ===

// === case: index_of_greater_equal_zero ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterEqualZeroSliceViolation {
	void indexOfGreaterEqualZero(String s) {
		if (s.indexOf("bar") >= 0)
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_greater_neg_one ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterNegOneSliceViolation {
	void indexOfGreaterNegOne(String s) {
		if (s.indexOf("ee") > -1)
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_less_equal_neg_one ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessEqualNegOneSliceViolation {
	void indexOfLessEqualNegOne(String s) {
		if (s.indexOf("ff") <= -1)
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_less_than_zero ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessThanZeroSliceViolation {
	void indexOfLessThanZero(String s) {
		if (s.indexOf("qux") < 0)
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_not_equal_neg_one ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfNotEqualNegOneSliceViolation {
	void indexOfNotEqualNegOne(String s) {
		if (s.indexOf("foo") != -1)
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_single_char ===
class InputSpecificApiIndexOfCharIndexOfSingleCharSliceViolation {
	void indexOfSingleChar(String s) {
		final var i = s.indexOf('x');
		System.out.println(i);
	}
}
// === end ===

// === case: inherited_list_field_get_zero ===
// imports: java.util.List
class InputSpecificApiReflectionInheritedListFieldGetZeroSliceViolation extends InputSpecificApiInheritedListBase {
	void inheritedListFieldGetZero() {
		System.out.println(inheritedList.getFirst());
	}
}

class InputSpecificApiInheritedListBase {
	List<String> inheritedList = List.of("a");
}
// === end ===

// === case: instance_init_scope ===
class InputSpecificApiIsEmptyInstanceInitScopeSliceViolation {
	private final String value = "x";

	{
		if (value.isEmpty())
			throw new IllegalArgumentException();
	}
}
// === end ===

// === case: key_set_contains ===
// imports: java.util.Map
class InputSpecificApiMapChainKeySetContainsSliceViolation {
	void keySetContains(Map<String, String> map) {
		if (map.containsKey("key"))
			System.out.println("found");
	}
}
// === end ===

// === case: key_set_contains_pattern_in_string_not_anchored ===
// imports: java.util.Map
class InputSpecificApiMapChainKeySetContainsPatternInStringNotAnchoredSliceViolation {
	void m(Map<String, String> map) {
		if (".keySet().contains(".isEmpty() || map.containsKey("key"))
			System.out.println("x");
	}
}
// === end ===

// === case: last_index_of_single_char ===
class InputSpecificApiIndexOfCharLastIndexOfSingleCharSliceViolation {
	void lastIndexOfSingleChar(String s) {
		final var i = s.lastIndexOf('/');
		System.out.println(i);
	}
}
// === end ===

// === case: length_equals_zero ===
class InputSpecificApiIsEmptyLengthEqualsZeroSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: length_equals_zero_foreign_receiver_rejected ===
// imports: java.io.File
// imports: java.util.List
class InputSpecificApiIsEmptyLengthForeignReceiverRejectedSliceViolation {
	boolean m(File file, List<String> list) {
		return file.length() == 0 || list.isEmpty();
	}
}
// === end ===

// === case: length_greater_than_or_equal_one ===
class InputSpecificApiIsEmptyLengthGreaterThanOrEqualOneSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_greater_than_zero ===
class InputSpecificApiIsEmptyLengthGreaterThanZeroSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_in_compound_reversed_condition ===
class InputSpecificApiIsEmptyLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && !s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_is_empty_multiple_occurrences_first_rejected ===
class InputSpecificApiLengthIsEmptyMultipleOccurrencesFirstRejectedSliceViolation {
	void lengthIsEmptyMultipleOccurrencesFirstRejected(String a, String b) {
		if (a.length() == 0xF || b.isEmpty())
			return;
	}
}
// === end ===

// === case: length_less_than_one ===
class InputSpecificApiIsEmptyLengthLessThanOneSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: length_less_than_or_equal_zero ===
class InputSpecificApiIsEmptyLengthLessThanOrEqualZeroSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: length_not_equals_zero ===
class InputSpecificApiIsEmptyLengthNotEqualsZeroSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_not_equals_zero_already_negated ===
class InputSpecificApiIsEmptyLengthNotEqualsZeroAlreadyNegatedSliceViolation {
	boolean m(String s) {
		return !(!s.isEmpty());
	}
}
// === end ===

// === case: length_pattern_in_string_not_anchored ===
class InputSpecificApiIsEmptyLengthPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".length() == 0".isEmpty() || s.isEmpty())
			System.out.println("x");
	}
}
// === end ===

// === case: length_reversed_first_rejected_second_accepted ===
class InputSpecificApiIsEmptyLengthReversedFirstRejectedSecondAcceptedSliceViolation {
	void m(String s, int idx10) {
		if (idx10 == 0 && s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: list_local_get_zero ===
// imports: java.util.List
class InputSpecificApiReflectionListLocalGetZeroSliceViolation {
	void listLocalGetZero() {
		final var list = List.of("a");
		System.out.println(list.getFirst());
	}
}
// === end ===

// === case: list_param_get_zero ===
// imports: java.util.List
class InputSpecificApiReflectionListParamGetZeroSliceViolation {
	void listParamGetZero(List<String> list) {
		System.out.println(list.getFirst());
	}
}
// === end ===

// === case: neg_one_equal_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfNegOneEqualIndexOfSliceViolation {
	void negOneEqualIndexOf(String s) {
		if (-1 == s.indexOf("bb"))
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_greater_equal_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfNegOneGreaterEqualIndexOfSliceViolation {
	void negOneGreaterEqualIndexOf(String s) {
		if (-1 >= s.indexOf("hh"))
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_less_than_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfNegOneLessThanIndexOfSliceViolation {
	void negOneLessThanIndexOf(String s) {
		if (-1 < s.indexOf("gg"))
			System.out.println("found");
	}
}
// === end ===

// === case: neg_one_not_equal_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfNegOneNotEqualIndexOfSliceViolation {
	void negOneNotEqualIndexOf(String s) {
		if (-1 != s.indexOf("aa"))
			System.out.println("found");
	}
}
// === end ===

// === case: octal_escape ===
class InputSpecificApiIndexOfCharOctalEscapeSliceViolation {
	void octalEscape(String s) {
		final var i = s.indexOf('\077');
		System.out.println(i);
	}
}
// === end ===

// === case: one_greater_than_length ===
class InputSpecificApiIsEmptyOneGreaterThanLengthSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: one_greater_than_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyOneGreaterThanSizeSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: one_greater_than_strip_length ===
class InputSpecificApiStripIsBlankOneGreaterThanStripLengthSliceViolation {
	void oneGreaterThanStripLength(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: one_greater_than_trim_length ===
class InputSpecificApiTrimIsBlankOneGreaterThanTrimLengthSliceViolation {
	void oneGreaterThanTrimLength(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: one_less_equal_strip_length ===
class InputSpecificApiStripIsBlankOneLessEqualStripLengthSliceViolation {
	void oneLessEqualStripLength(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: one_less_equal_trim_length ===
class InputSpecificApiTrimIsBlankOneLessEqualTrimLengthSliceViolation {
	void oneLessEqualTrimLength(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: one_less_than_or_equal_length ===
class InputSpecificApiIsEmptyOneLessThanOrEqualLengthSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: one_less_than_or_equal_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyOneLessThanOrEqualSizeSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: paren_receiver_stream_find_first_rejected ===
// skip-reason: cannot rewrite: unrecognized API pattern
// imports: java.util.List
class InputSpecificApiParenReceiverStreamFindFirstRejectedSliceViolation {
	boolean parenReceiverStreamFindFirstRejected(Object o) {
		return ((List<?>) o).stream().findFirst().isPresent();
	}
}
// === end ===

// === case: qualified_assert_equals ===
class InputSpecificApiAssertQualifiedAssertEqualsSliceViolation {
	void qualifiedAssertEquals() {
		org.junit.Assert.assertTrue(1 == 1);
	}
}
// === end ===

// === case: qualified_assert_equals_literal_last ===
class InputSpecificApiAssertQualifiedAssertEqualsLiteralLastSliceViolation {
	void qualifiedAssertEqualsLiteralLast() {
		org.junit.Assert.assertTrue(1 == 1);
	}
}
// === end ===

// === case: qualified_assert_equals_literal_middle ===
class InputSpecificApiAssertQualifiedAssertEqualsLiteralMiddleSliceViolation {
	void qualifiedAssertEqualsLiteralMiddle() {
		org.junit.Assert.assertNull("msg", new Object());
	}
}
// === end ===

// === case: qualified_assertions_assert_equals ===
// imports: org.junit.jupiter.api.Assertions
class InputSpecificApiAssertQualifiedAssertionsAssertEqualsSliceViolation {
	void qualifiedAssertionsAssertEquals() {
		Assertions.assertTrue(1 == 1);
	}
}
// === end ===

// === case: remove_first ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstSliceViolation {
	void removeFirst(List<String> list) {
		list.removeFirst();
	}
}
// === end ===

// === case: remove_first_field_shadowed_by_later_local ===
// imports: java.awt.Container
// imports: java.util.ArrayList
class InputSpecificApiRemoveRemoveFirstFieldShadowedByLaterLocalSliceViolation {
	private final Container items = new Container();

	void removeFirstFieldShadowedByLaterLocal() {
		items.remove(0);
		final var items = new ArrayList<String>();
		System.out.println(items);
	}
}
// === end ===

// === case: remove_first_later_local_does_not_hide_list_field ===
// imports: java.awt.Container
// imports: java.util.ArrayList
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstLaterLocalDoesNotHideListFieldSliceViolation {
	private final List<String> items = new ArrayList<>();

	void removeFirstLaterLocalDoesNotHideListField() {
		items.removeFirst();
		final var items = new Container();
		System.out.println(items);
	}
}
// === end ===

// === case: remove_first_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".remove(0)" + list.removeFirst();
	}
}
// === end ===

// === case: remove_first_uppercase_container_parameter ===
// imports: java.awt.Container
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstUppercaseContainerParameterSliceViolation {
	void removeFirstUppercaseContainerParameter(Container List) {
		List.remove(0);
	}

	int size(List<String> list) {
		return list.size();
	}
}
// === end ===

// === case: remove_first_uppercase_list_parameter ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstUppercaseListParameterSliceViolation {
	void removeFirstUppercaseListParameter(List<String> List) {
		List.removeFirst();
	}
}
// === end ===

// === case: remove_first_uppercase_pattern_variable ===
// imports: java.awt.Container
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstUppercasePatternVariableSliceViolation {
	void removeFirstUppercasePatternVariable(Object o) {
		if (o instanceof Container List)
			List.remove(0);
	}

	int size(List<String> list) {
		return list.size();
	}
}
// === end ===

// === case: remove_last ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveLastSliceViolation {
	void removeLast(List<String> list) {
		list.removeLast();
	}
}
// === end ===

// === case: remove_last_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveLastPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = "list.remove(list.size() - 1)" + list.removeLast();
	}
}
// === end ===

// === case: replace_all_bare_call_beside_a_classpath_overload_of_another_arity ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDocSource
class InputSpecificApiReplaceAllBareCallBesideAClasspathOverloadOfAnotherAritySliceViolation extends ClasspathDocSource {
	String m() {
		return text("a", "b").replace("x", "y");
	}

	String text(String first, String second) {
		return first + second;
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_classpath_supertype_returning_a_type_variable ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathValueSource
class InputSpecificApiReplaceAllBareCallInheritedFromAClasspathSupertypeReturningATypeVariableSliceViolation extends ClasspathValueSource<String> {
	String m() {
		return value().replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_classpath_supertype_returning_string ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDocSource
class InputSpecificApiReplaceAllBareCallInheritedFromAClasspathSupertypeReturningStringSliceViolation extends ClasspathDocSource {
	String m() {
		return label().replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_jdk_supertype_returning_its_annotated_string_type_argument ===
// imports: java.lang.annotation.ElementType
// imports: java.lang.annotation.Target
// imports: java.util.ArrayList
class InputSpecificApiReplaceAllBareCallInheritedFromAJdkSupertypeReturningItsAnnotatedStringTypeArgumentSliceViolation {
	@Target(ElementType.TYPE_USE)
	@interface Mark {}

	static class Names extends ArrayList<@Mark String> {
		String m() {
			return get(0).replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_jdk_supertype_returning_its_second_string_type_argument ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDoc
// imports: java.util.HashMap
class InputSpecificApiReplaceAllBareCallInheritedFromAJdkSupertypeReturningItsSecondStringTypeArgumentSliceViolation extends HashMap<ClasspathDoc, String> {
	String m(ClasspathDoc key) {
		return get(key).replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_jdk_supertype_returning_its_string_type_argument ===
// imports: java.util.ArrayList
class InputSpecificApiReplaceAllBareCallInheritedFromAJdkSupertypeReturningItsStringTypeArgumentSliceViolation extends ArrayList<String> {
	String m() {
		return get(0).replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_qualified_jdk_supertype_returning_its_string_type_argument ===
class InputSpecificApiReplaceAllBareCallInheritedFromAQualifiedJdkSupertypeReturningItsStringTypeArgumentSliceViolation extends java.util.ArrayList<String> {
	String m() {
		return get(0).replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_a_static_method_of_a_same_file_superclass_returning_string ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDoc
class InputSpecificApiReplaceAllBareCallInheritedFromAStaticMethodOfASameFileSuperclassReturningStringSliceViolation {
	static class Base {
		static String make() {
			return "";
		}
	}

	class Inner extends Base {
		String use() {
			return make().replace("x", "y");
		}
	}

	ClasspathDoc make() {
		return new ClasspathDoc();
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_an_anonymous_jdk_supertype_returning_its_string_type_argument ===
// imports: java.util.ArrayList
class InputSpecificApiReplaceAllBareCallInheritedFromAnAnonymousJdkSupertypeReturningItsStringTypeArgumentSliceViolation {
	Object m() {
		return new ArrayList<String>() {
			String first() {
				return get(0).replace("x", "y");
			}
		};
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_from_an_anonymous_qualified_jdk_supertype_returning_its_string_type_argument ===
class InputSpecificApiReplaceAllBareCallInheritedFromAnAnonymousQualifiedJdkSupertypeReturningItsStringTypeArgumentSliceViolation {
	Object m() {
		return new java.util.ArrayList<String>() {
			String first() {
				return get(0).replace("x", "y");
			}
		};
	}
}
// === end ===

// === case: replace_all_bare_call_inherited_through_a_same_file_supertype_binding_a_jdk_type_argument_to_string ===
// imports: java.util.ArrayList
class InputSpecificApiReplaceAllBareCallInheritedThroughASameFileSupertypeBindingAJdkTypeArgumentToStringSliceViolation {
	static class Base<T> extends ArrayList<T> {
	}

	static class Derived extends Base<String> {
		String m() {
			return get(0).replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_bare_call_on_a_classpath_override_narrowed_to_string ===
class InputSpecificApiReplaceAllBareCallOnAClasspathOverrideNarrowedToStringSliceViolation {
	abstract static class Narrowed implements CharSequence {
		String m() {
			return subSequence(0, 1).replace("x", "y");
		}

		@Override
		public String subSequence(int start, int end) {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_bare_call_past_a_private_method_of_a_same_file_superclass_to_an_enclosing_method_returning_string ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDoc
class InputSpecificApiReplaceAllBareCallPastAPrivateMethodOfASameFileSuperclassToAnEnclosingMethodReturningStringSliceViolation {
	static class Base {
		private ClasspathDoc make() {
			return new ClasspathDoc();
		}
	}

	class Inner extends Base {
		String use() {
			return make().replace("x", "y");
		}
	}

	String make() {
		return "";
	}
}
// === end ===

// === case: replace_all_bare_call_past_a_static_method_of_a_same_file_superinterface_to_an_enclosing_method_returning_string ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDoc
class InputSpecificApiReplaceAllBareCallPastAStaticMethodOfASameFileSuperinterfaceToAnEnclosingMethodReturningStringSliceViolation {
	class Inner implements Maker {
		String use() {
			return make().replace("x", "y");
		}
	}

	interface Maker {
		static ClasspathDoc make() {
			return new ClasspathDoc();
		}
	}

	String make() {
		return "";
	}
}
// === end ===

// === case: replace_all_bare_call_static_imported_returning_string ===
// imports: static java.lang.String.valueOf
class InputSpecificApiReplaceAllBareCallStaticImportedReturningStringSliceViolation {
	String m(int value) {
		return valueOf(value).replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_to_a_same_file_method_shadowing_a_static_import ===
// imports: static java.util.Objects.requireNonNull
class InputSpecificApiReplaceAllBareCallToASameFileMethodShadowingAStaticImportSliceViolation {
	String m(Object value) {
		return requireNonNull(value).replace("x", "y");
	}

	String requireNonNull(Object value) {
		return String.valueOf(value);
	}
}
// === end ===

// === case: replace_all_bare_call_to_an_own_private_method_returning_string ===
class InputSpecificApiReplaceAllBareCallToAnOwnPrivateMethodReturningStringSliceViolation {
	private String make() {
		return "";
	}

	String use() {
		return make().replace("x", "y");
	}
}
// === end ===

// === case: replace_all_bare_call_to_an_own_static_interface_method_returning_string ===
interface InputSpecificApiReplaceAllBareCallToAnOwnStaticInterfaceMethodReturningStringSliceViolation {
	static String make() {
		return "";
	}

	default String use() {
		return make().replace("x", "y");
	}
}
// === end ===

// === case: replace_all_keeps_comment_in_arguments ===
class InputSpecificApiReplaceAllKeepsCommentInArgumentsSliceViolation {
	String replaceAllKeepsCommentInArguments(String s) {
		return s.replace(/*c*/"x", "y");
	}
}
// === end ===

// === case: replace_all_literal ===
class InputSpecificApiStringMethodReplaceAllLiteralSliceViolation {
	void replaceAllLiteral(String s) {
		final var result = s.replace("foo", "bar");
	}
}
// === end ===

// === case: replace_all_non_literal_first_rejected ===
class InputSpecificApiStringMethodReplaceAllNonLiteralFirstRejectedSliceViolation {
	String m(String a, String b, String p) {
		return a.replaceAll(p, b) + b.replace("y", "z");
	}
}
// === end ===

// === case: replace_all_pattern_in_string_not_anchored ===
class InputSpecificApiStringMethodReplaceAllPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		final var r = ".replaceAll(" + s.replace("foo", "bar");
	}
}
// === end ===

// === case: replace_all_record_accessor_beside_a_varargs_overload_returning_string ===
class InputSpecificApiReplaceAllRecordAccessorBesideAVarargsOverloadReturningStringSliceViolation {
	record Named(String text) {
		String m() {
			return text().replace("x", "y");
		}

		String text(String... parts) {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_replacement_comment_before_respelled_literal ===
class InputSpecificApiStringMethodReplaceAllReplacementCommentBeforeRespelledLiteralSliceViolation {
	String m(String s) {
		return s.replace("x", /* c */ "a\\b");
	}
}
// === end ===

// === case: replace_all_replacement_concatenation_plain ===
class InputSpecificApiStringMethodReplaceAllReplacementConcatenationPlainSliceViolation {
	String m(String s) {
		return s.replace("x", "a" + "b");
	}
}
// === end ===

// === case: replace_all_replacement_empty ===
class InputSpecificApiStringMethodReplaceAllReplacementEmptySliceViolation {
	String m(String s) {
		return s.replace("x", "");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_backslash ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedBackslashSliceViolation {
	String m(String s) {
		return s.replace("x", "a\\b");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_dollar ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedDollarSliceViolation {
	String m(String s) {
		return s.replace("x", "$");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_dollar_digit ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedDollarDigitSliceViolation {
	String m(String s) {
		return s.replace("x", "$1");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_letter ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedLetterSliceViolation {
	String m(String s) {
		return s.replace("x", "ab");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_n ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedNSliceViolation {
	String m(String s) {
		return s.replace("x", "n");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_quote ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedQuoteSliceViolation {
	String m(String s) {
		return s.replace("x", "\"");
	}
}
// === end ===

// === case: replace_all_replacement_escaped_u_text ===
class InputSpecificApiStringMethodReplaceAllReplacementEscapedUTextSliceViolation {
	String m(String s) {
		return s.replace("x", "u0024");
	}
}
// === end ===

// === case: replace_all_replacement_java_escape_only ===
class InputSpecificApiStringMethodReplaceAllReplacementJavaEscapeOnlySliceViolation {
	String m(String s) {
		return s.replace("x", "a\nb");
	}
}
// === end ===

// === case: replace_all_replacement_literal_text_elsewhere_on_line ===
class InputSpecificApiStringMethodReplaceAllReplacementLiteralTextElsewhereOnLineSliceViolation {
	String m(String s) {
		return "a\\\\b" + s.replace("x", "a\\b");
	}
}
// === end ===

// === case: replace_all_replacement_mixed_escapes ===
class InputSpecificApiStringMethodReplaceAllReplacementMixedEscapesSliceViolation {
	String m(String s) {
		return s.replace("foo", "\\foo$");
	}
}
// === end ===

// === case: replace_all_replacement_octal_dollar_zero ===
class InputSpecificApiStringMethodReplaceAllReplacementOctalDollarZeroSliceViolation {
	String m(String s) {
		return s.replace("foo", "foo");
	}
}
// === end ===

// === case: replace_all_replacement_octal_escaped_backslash ===
class InputSpecificApiStringMethodReplaceAllReplacementOctalEscapedBackslashSliceViolation {
	String m(String s) {
		return s.replace("x", "\134");
	}
}
// === end ===

// === case: replace_all_replacement_on_continuation_line ===
class InputSpecificApiStringMethodReplaceAllReplacementOnContinuationLineSliceViolation {
	String m(String s) {
		return s.replace(
				"x",
				"a\\b"
		);
	}
}
// === end ===

// === case: replace_all_replacement_text_block_plain ===
class InputSpecificApiStringMethodReplaceAllReplacementTextBlockPlainSliceViolation {
	String m(String s) {
		return s.replace(
				"x",
				"""
				y"""
		);
	}
}
// === end ===

// === case: replace_all_replacement_unicode_dollar_zero ===
class InputSpecificApiStringMethodReplaceAllReplacementUnicodeDollarZeroSliceViolation {
	String m(String s) {
		return s.replace("foo", "foo");
	}
}
// === end ===

// === case: replace_all_replacement_unicode_escaped_backslash_pair ===
class InputSpecificApiStringMethodReplaceAllReplacementUnicodeEscapedBackslashPairSliceViolation {
	String m(String s) {
		return s.replace("x", "\u005c\u005c");
	}
}
// === end ===

// === case: replace_all_replacement_unicode_escaped_newline ===
class InputSpecificApiStringMethodReplaceAllReplacementUnicodeEscapedNewlineSliceViolation {
	String m(String s) {
		return s.replace("x", "\u005cn");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_double_zero ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchDoubleZeroSliceViolation {
	String m(String s) {
		return s.replace("foo", "foo");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_empty_pattern ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchEmptyPatternSliceViolation {
	String m(String s) {
		return s.replace("", "[]");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_mid ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchMidSliceViolation {
	String m(String s) {
		return s.replace("foo", "[foo]");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_only ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchOnlySliceViolation {
	String m(String s) {
		return s.replace("foo", "foo");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_repeated ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchRepeatedSliceViolation {
	String m(String s) {
		return s.replace("foo", "foofoo");
	}
}
// === end ===

// === case: replace_all_replacement_whole_match_then_digit ===
class InputSpecificApiStringMethodReplaceAllReplacementWholeMatchThenDigitSliceViolation {
	String m(String s) {
		return s.replace("foo", "foo1");
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_no_argument_override_beside_a_classpath_supertype_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantNoArgumentOverrideBesideAClasspathSupertypeReturningStringSliceViolation {
	static class Base {
		CharSequence source() {
			return "";
		}
	}

	static class Derived extends Base implements Runnable {
		String m() {
			return source().replace("x", "y");
		}

		@Override
		public void run() {
		}

		@Override
		String source() {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_beside_a_classpath_interface_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideBesideAClasspathInterfaceReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base implements Runnable {
		String m() {
			return source("k").replace("x", "y");
		}

		@Override
		public void run() {
		}

		@Override
		String source(String key) {
			return key;
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_in_an_anonymous_same_file_subclass_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideInAnAnonymousSameFileSubclassReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	Base derived() {
		return new Base() {
			String m() {
				return source("k").replace("x", "y");
			}

			@Override
			String source(String key) {
				return key;
			}
		};
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_of_a_primitive_beside_a_classpath_supertype_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideOfAPrimitiveBesideAClasspathSupertypeReturningStringSliceViolation {
	static class Base {
		CharSequence source(int key) {
			return "";
		}
	}

	static class Derived extends Base implements Runnable {
		String m() {
			return source(1).replace("x", "y");
		}

		@Override
		public void run() {
		}

		@Override
		String source(int key) {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_of_a_same_file_parameter_type_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideOfASameFileParameterTypeReturningStringSliceViolation {
	static class Base {
		CharSequence source(Key key) {
			return "";
		}
	}

	static class Derived extends Base {
		@Override
		String source(Key key) {
			return "";
		}
	}

	static class Key {
	}

	static class Leaf extends Derived {
		String m(Key key) {
			return source(key).replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_of_an_enclosing_type_variable_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideOfAnEnclosingTypeVariableReturningStringSliceViolation<T> {
	class Base {
		CharSequence source(T key) {
			return "";
		}
	}

	class Derived extends Base {
		@Override
		String source(T key) {
			return "";
		}
	}

	class Leaf extends Derived {
		String m(T key) {
			return source(key).replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_a_covariant_override_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnACovariantOverrideReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base {
		@Override
		String source(String key) {
			return key;
		}
	}

	static class Leaf extends Derived {
		String m() {
			return source("k").replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_on_an_override_of_a_class_type_variable_returning_string ===
class InputSpecificApiReplaceAllSameFileCallOnAnOverrideOfAClassTypeVariableReturningStringSliceViolation {
	static class Base<T> {
		CharSequence source(T key) {
			return "";
		}
	}

	static class Derived<T> extends Base<T> {
		String m(T key) {
			return source(key).replace("x", "y");
		}

		@Override
		String source(T key) {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_same_file_call_returning_string ===
class InputSpecificApiReplaceAllSameFileCallReturningStringSliceViolation {
	String m() {
		return source().replace("x", "y");
	}

	String source() {
		return "";
	}
}
// === end ===

// === case: replace_all_same_file_call_with_every_level_returning_string ===
class InputSpecificApiReplaceAllSameFileCallWithEveryLevelReturningStringSliceViolation {
	static class Base {
		String source(String key) {
			return key;
		}
	}

	static class Derived extends Base {
		String source(int index) {
			return "";
		}
	}

	static class Leaf extends Derived {
		String m() {
			return source("k").replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_same_file_receiver_parameter_call_returning_string ===
class InputSpecificApiReplaceAllSameFileReceiverParameterCallReturningStringSliceViolation {
	static class Row {
		String m() {
			return source().replace("x", "y");
		}

		String source(Row this) {
			return "";
		}
	}
}
// === end ===

// === case: replace_all_same_file_varargs_call_returning_string ===
class InputSpecificApiReplaceAllSameFileVarargsCallReturningStringSliceViolation {
	String join(String... parts) {
		return String.join("", parts);
	}

	void m() {
		System.out.println(join().replace("x", "y"));
		System.out.println(join("a").replace("x", "y"));
		System.out.println(join("a", "b").replace("x", "y"));
	}
}
// === end ===

// === case: replace_all_string_literal_receiver ===
class InputSpecificApiStringMethodReplaceAllStringLiteralReceiverSliceViolation {
	String m() {
		return "abc".replace("b", "x");
	}
}
// === end ===

// === case: replace_all_super_call_inherited_from_a_jdk_supertype_returning_its_string_type_argument ===
// imports: java.util.ArrayList
class InputSpecificApiReplaceAllSuperCallInheritedFromAJdkSupertypeReturningItsStringTypeArgumentSliceViolation extends ArrayList<String> {
	String m() {
		return super.get(0).replace("x", "y");
	}
}
// === end ===

// === case: replace_all_super_call_on_a_covariant_override_returning_string ===
class InputSpecificApiReplaceAllSuperCallOnACovariantOverrideReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base {
		@Override
		String source(String key) {
			return key;
		}
	}

	static class Leaf extends Derived {
		String m() {
			return super.source("k").replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_super_call_returning_string ===
class InputSpecificApiReplaceAllSuperCallReturningStringSliceViolation {
	static class Base {
		String source() {
			return "";
		}
	}

	static class Derived extends Base {
		String m() {
			return super.source().replace("x", "y");
		}
	}
}
// === end ===

// === case: replace_all_super_call_without_a_same_file_declaration ===
class InputSpecificApiReplaceAllSuperCallWithoutASameFileDeclarationSliceViolation {
	String m() {
		return super.toString().replace("x", "y");
	}
}
// === end ===

// === case: replace_all_two_respelled_calls_on_one_line ===
class InputSpecificApiStringMethodReplaceAllTwoRespelledCallsOnOneLineSliceViolation {
	String m(String s) {
		return s.replace("x", "$").replace("y", "[y]");
	}
}
// === end ===

// === case: replace_all_var_local_with_a_literal_initializer ===
class InputSpecificApiReplaceAllVarLocalWithALiteralInitializerSliceViolation {
	String m() {
		final var text = "  ";
		return text.replace("x", "y");
	}
}
// === end ===

// === case: replace_all_var_local_with_a_proven_call_initializer ===
class InputSpecificApiReplaceAllVarLocalWithAProvenCallInitializerSliceViolation {
	String m(int value) {
		final var text = String.valueOf(value);
		return text.replace("x", "y");
	}
}
// === end ===

// === case: shadowing_inverse_get_zero ===
// imports: java.util.List
// imports: java.util.Map
class InputSpecificApiReflectionShadowingInverseGetZeroSliceViolation {
	private final Map<Integer, String> shared = Map.of();

	void shadowingInverseGetZero(List<List<String>> lists) {
		lists.forEach((List<String> shared) -> System.out.println(shared.getFirst()));
	}
}
// === end ===

// === case: single_quote ===
class InputSpecificApiIndexOfCharSingleQuoteSliceViolation {
	void singleQuote(String s) {
		final var i = s.indexOf('\'');
		System.out.println(i);
	}
}
// === end ===

// === case: singleton ===
// imports: java.util.Collections
// imports: java.util.Set
class InputSpecificApiCollectionsEmptySingletonSliceViolation {
	void singleton() {
		final var set = Set.of("a");
	}
}
// === end ===

// === case: singleton_list ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsEmptySingletonListSliceViolation {
	void singletonList() {
		final var list = List.of("a");
	}
}
// === end ===

// === case: singleton_map ===
// imports: java.util.Collections
// imports: java.util.Map
class InputSpecificApiCollectionsEmptySingletonMapSliceViolation {
	void singletonMap() {
		final var map = Map.of("k", "v");
	}
}
// === end ===

// === case: size_equals_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeEqualsZeroSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: size_greater_than_or_equal_one ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeGreaterThanOrEqualOneSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: size_greater_than_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeGreaterThanZeroSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: size_less_than_one ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeLessThanOneSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: size_less_than_or_equal_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeLessThanOrEqualZeroSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: size_not_equals_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeNotEqualsZeroSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: size_zero_space_before_dot ===
// imports: java.util.List
class InputSpecificApiSizeZeroSpaceBeforeDotSliceViolation {
	boolean sizeZeroSpaceBeforeDot(List<String> list) {
		return list.isEmpty();
	}
}
// === end ===

// === case: sort_cast_argument_parenthesized ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiSortCastArgumentParenthesizedSliceViolation {
	void sortCastArgumentParenthesized(Object o) {
		((List<String>) o).sort(null);
	}
}
// === end ===

// === case: sort_no_comparator ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortNoComparatorSliceViolation {
	void sortNoComparator(List<String> list) {
		list.sort(null);
	}
}
// === end ===

// === case: sort_no_comparator_nested_arg ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortNoComparatorNestedArgSliceViolation {
	List<String> getList() {
		return List.of();
	}

	void sortNoComparatorNestedArg() {
		getList().sort(null);
	}
}
// === end ===

// === case: sort_pattern_in_comment_not_anchored ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortPatternInCommentNotAnchoredSliceViolation {
	void m(List<String> list) {
		/* Collections.sort( */ list.sort(null);
	}
}
// === end ===

// === case: sort_with_block_comment ===
// imports: java.util.Collections
// imports: java.util.Comparator
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithBlockCommentSliceViolation {
	void sortWithBlockComment(List<String> list, Comparator<String> cmp) {
		list.sort(cmp /* ) */);
	}
}
// === end ===

// === case: sort_with_comparator ===
// imports: java.util.Collections
// imports: java.util.Comparator
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithComparatorSliceViolation {
	void sortWithComparator(List<String> list) {
		list.sort(Comparator.naturalOrder());
	}
}
// === end ===

// === case: sort_with_comparator_char_literal ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithComparatorCharLiteralSliceViolation {
	void sortWithComparatorCharLiteral(List<String> list) {
		list.sort((a, b) -> Character.compare(a.charAt(0), '('));
	}
}
// === end ===

// === case: sort_with_lambda_comparator ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithLambdaComparatorSliceViolation {
	void sortWithLambdaComparator(List<String> list) {
		list.sort((a, b) -> a.compareTo(b));
	}
}
// === end ===

// === case: sort_with_string_literal_paren ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithStringLiteralParenSliceViolation {
	void sortWithStringLiteralParen(List<String> list) {
		list.sort((a, b) -> a.replace("(", ")").compareTo(b));
	}
}
// === end ===

// === case: static_init_scope ===
// imports: java.util.Arrays
// imports: java.util.List
class InputSpecificApiArraysAsListStaticInitScopeSliceViolation {
	static {
		final var list = List.of("a");
	}
}
// === end ===

// === case: stream_count ===
// imports: java.util.List
class InputSpecificApiStreamStreamCountSliceViolation {
	void streamCount(List<String> list) {
		final var count = list.size();
	}
}
// === end ===

// === case: stream_count_discarded_comment_rejected ===
// skip-reason: cannot rewrite: unrecognized API pattern
// imports: java.util.List
class InputSpecificApiStreamCountDiscardedCommentRejectedSliceViolation {
	long streamCountDiscardedCommentRejected(List<String> list) {
		return list.stream()/*c*/.count();
	}
}
// === end ===

// === case: stream_count_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamCountPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var c = ".stream().count()" + list.size();
	}
}
// === end ===

// === case: stream_find_first_is_present ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentSliceViolation {
	void streamFindFirstIsPresent(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_already_negated ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentAlreadyNegatedSliceViolation {
	void streamFindFirstIsPresentAlreadyNegated(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_dotted_receiver ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentDottedReceiverSliceViolation {
	List<String> list;

	void streamFindFirstIsPresentDottedReceiver(InputSpecificApiStreamStreamFindFirstIsPresentDottedReceiverSliceViolation obj) {
		if (!obj.list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_method_receiver ===
// imports: java.util.List
class InputSpecificApiStreamFindFirstIsPresentMethodReceiverSliceViolation {
	List<String> getList() {
		return List.of();
	}

	void streamFindFirstIsPresentMethodReceiver() {
		if (!getList().isEmpty())
			return;
	}
}
// === end ===

// === case: stream_find_first_is_present_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		if (".stream().findFirst().isPresent()".isEmpty() || !list.isEmpty())
			System.out.println("x");
	}
}
// === end ===

// === case: stream_for_each ===
// imports: java.util.List
class InputSpecificApiStreamStreamForEachSliceViolation {
	void streamForEach(List<String> list) {
		list.forEach(System.out::println);
	}
}
// === end ===

// === case: stream_for_each_pattern_in_comment_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamForEachPatternInCommentNotAnchoredSliceViolation {
	void m(List<String> list) {
		/* .stream().forEach( */ list.forEach(System.out::println);
	}
}
// === end ===

// === case: strip_is_blank_array_element_receiver ===
class InputSpecificApiStripIsBlankArrayElementReceiverSliceViolation {
	boolean m(String[] names) {
		return names[0].isBlank();
	}
}
// === end ===

// === case: strip_is_blank_bare_call_beside_a_classpath_overload_of_another_arity ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDocSource
class InputSpecificApiStripIsBlankBareCallBesideAClasspathOverloadOfAnotherAritySliceViolation extends ClasspathDocSource {
	boolean m() {
		return text("a", "b").isBlank();
	}

	String text(String first, String second) {
		return first + second;
	}
}
// === end ===

// === case: strip_is_blank_bare_call_inherited_from_a_classpath_supertype_returning_a_type_variable ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathValueSource
class InputSpecificApiStripIsBlankBareCallInheritedFromAClasspathSupertypeReturningATypeVariableSliceViolation extends ClasspathValueSource<String> {
	boolean m() {
		return value().isBlank();
	}
}
// === end ===

// === case: strip_is_blank_bare_call_inherited_from_a_classpath_supertype_returning_string ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDocSource
class InputSpecificApiStripIsBlankBareCallInheritedFromAClasspathSupertypeReturningStringSliceViolation extends ClasspathDocSource {
	boolean m() {
		return label().isBlank();
	}
}
// === end ===

// === case: strip_is_blank_bare_call_inherited_from_a_jdk_supertype_returning_its_string_type_argument ===
// imports: java.util.ArrayList
class InputSpecificApiStripIsBlankBareCallInheritedFromAJdkSupertypeReturningItsStringTypeArgumentSliceViolation extends ArrayList<String> {
	boolean m() {
		return get(0).isBlank();
	}
}
// === end ===

// === case: strip_is_blank_bare_call_inherited_from_a_jdk_supertype_returning_string ===
class InputSpecificApiStripIsBlankBareCallInheritedFromAJdkSupertypeReturningStringSliceViolation extends Thread {
	boolean m() {
		return getName().isBlank();
	}
}
// === end ===

// === case: strip_is_blank_bare_call_on_a_classpath_override_narrowed_to_string ===
class InputSpecificApiStripIsBlankBareCallOnAClasspathOverrideNarrowedToStringSliceViolation {
	abstract static class Narrowed implements CharSequence {
		boolean m() {
			return subSequence(0, 1).isBlank();
		}

		@Override
		public String subSequence(int start, int end) {
			return "";
		}
	}
}
// === end ===

// === case: strip_is_blank_cast_receiver ===
class InputSpecificApiStripIsBlankCastReceiverSliceViolation {
	boolean m(Object value) {
		return ((String) value).isBlank();
	}
}
// === end ===

// === case: strip_is_blank_field_read_through_a_same_file_variable ===
class InputSpecificApiStripIsBlankFieldReadThroughASameFileVariableSliceViolation {
	static class Holder {
		String name;
	}

	boolean m(Holder holder) {
		return holder.name.isBlank();
	}
}
// === end ===

// === case: strip_is_blank_inherited_same_file_call_returning_string ===
class InputSpecificApiStripIsBlankInheritedSameFileCallReturningStringSliceViolation {
	static class Base {
		String source() {
			return "";
		}
	}

	static class Derived extends Base {
		boolean m() {
			return source().isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_method_receiver ===
class InputSpecificApiStripIsBlankMethodReceiverSliceViolation {
	String getText() {
		return "";
	}

	void stripIsBlankMethodReceiver() {
		if (getText().isBlank())
			return;
	}
}
// === end ===

// === case: strip_is_blank_new_string_receiver ===
class InputSpecificApiStripIsBlankNewStringReceiverSliceViolation {
	boolean m(char[] chars) {
		return new String(chars).isBlank();
	}
}
// === end ===

// === case: strip_is_blank_outer_field_read_in_a_classpath_subclass_of_another_field ===
// imports: com.etk2000.checkstyle.testhelpers.ClasspathDocField
class InputSpecificApiStripIsBlankOuterFieldReadInAClasspathSubclassOfAnotherFieldSliceViolation {
	class Inner extends ClasspathDocField {
		boolean blank() {
			return label.isBlank();
		}
	}

	String label = "";
}
// === end ===

// === case: strip_is_blank_pattern_variable_receiver ===
class InputSpecificApiStripIsBlankPatternVariableReceiverSliceViolation {
	boolean m(Object value) {
		return value instanceof String text && text.isBlank();
	}
}
// === end ===

// === case: strip_is_blank_qualified_string_parameter ===
class InputSpecificApiStripIsBlankQualifiedStringParameterSliceViolation {
	boolean m(java.lang.String s) {
		return s.isBlank();
	}
}
// === end ===

// === case: strip_is_blank_record_component_accessor_returning_string ===
class InputSpecificApiStripIsBlankRecordComponentAccessorReturningStringSliceViolation {
	record Named(String name) {
		boolean m() {
			return name().isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_beside_an_overload_of_another_arity ===
class InputSpecificApiStripIsBlankSameFileCallBesideAnOverloadOfAnotherAritySliceViolation {
	static class Text {
		boolean isEmpty() {
			return true;
		}

		Text strip() {
			return this;
		}
	}

	boolean m() {
		return source().isBlank();
	}

	String source() {
		return "";
	}

	Text source(int index) {
		return new Text();
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_no_argument_override_beside_a_classpath_supertype_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantNoArgumentOverrideBesideAClasspathSupertypeReturningStringSliceViolation {
	static class Base {
		CharSequence source() {
			return "";
		}
	}

	static class Derived extends Base implements Runnable {
		boolean m() {
			return source().isBlank();
		}

		@Override
		public void run() {
		}

		@Override
		String source() {
			return "";
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_beside_a_classpath_interface_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideBesideAClasspathInterfaceReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base implements Runnable {
		boolean m() {
			return source("k").isBlank();
		}

		@Override
		public void run() {
		}

		@Override
		String source(String key) {
			return key;
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_in_an_anonymous_same_file_subclass_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideInAnAnonymousSameFileSubclassReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	Base derived() {
		return new Base() {
			boolean m() {
				return source("k").isBlank();
			}

			@Override
			String source(String key) {
				return key;
			}
		};
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_of_a_primitive_beside_a_classpath_supertype_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideOfAPrimitiveBesideAClasspathSupertypeReturningStringSliceViolation {
	static class Base {
		CharSequence source(int key) {
			return "";
		}
	}

	static class Derived extends Base implements Runnable {
		boolean m() {
			return source(1).isBlank();
		}

		@Override
		public void run() {
		}

		@Override
		String source(int key) {
			return "";
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_of_a_same_file_parameter_type_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideOfASameFileParameterTypeReturningStringSliceViolation {
	static class Base {
		CharSequence source(Key key) {
			return "";
		}
	}

	static class Derived extends Base {
		@Override
		String source(Key key) {
			return "";
		}
	}

	static class Key {
	}

	static class Leaf extends Derived {
		boolean m(Key key) {
			return source(key).isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_of_an_enclosing_type_variable_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideOfAnEnclosingTypeVariableReturningStringSliceViolation<T> {
	class Base {
		CharSequence source(T key) {
			return "";
		}
	}

	class Derived extends Base {
		@Override
		String source(T key) {
			return "";
		}
	}

	class Leaf extends Derived {
		boolean m(T key) {
			return source(key).isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_a_covariant_override_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnACovariantOverrideReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base {
		@Override
		String source(String key) {
			return key;
		}
	}

	static class Leaf extends Derived {
		boolean m() {
			return source("k").isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_on_an_override_of_a_class_type_variable_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallOnAnOverrideOfAClassTypeVariableReturningStringSliceViolation {
	static class Base<T> {
		CharSequence source(T key) {
			return "";
		}
	}

	static class Derived<T> extends Base<T> {
		boolean m(T key) {
			return source(key).isBlank();
		}

		@Override
		String source(T key) {
			return "";
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_call_returning_string ===
class InputSpecificApiStripIsBlankSameFileCallReturningStringSliceViolation {
	boolean m() {
		return source().isBlank();
	}

	String source() {
		return "";
	}
}
// === end ===

// === case: strip_is_blank_same_file_overloads_all_returning_string ===
class InputSpecificApiStripIsBlankSameFileOverloadsAllReturningStringSliceViolation {
	boolean m() {
		return text(1).isBlank();
	}

	String text(int index) {
		return "";
	}

	String text(String value) {
		return value;
	}
}
// === end ===

// === case: strip_is_blank_same_file_receiver_parameter_call_returning_string ===
class InputSpecificApiStripIsBlankSameFileReceiverParameterCallReturningStringSliceViolation {
	static class Row {
		boolean m() {
			return source().isBlank();
		}

		String source(Row this) {
			return "";
		}
	}
}
// === end ===

// === case: strip_is_blank_same_file_varargs_call_returning_string ===
class InputSpecificApiStripIsBlankSameFileVarargsCallReturningStringSliceViolation {
	String join(String... parts) {
		return String.join("", parts);
	}

	void m() {
		System.out.println(join().isBlank());
		System.out.println(join("a").isBlank());
		System.out.println(join("a", "b").isBlank());
	}
}
// === end ===

// === case: strip_is_blank_static_factory_chain_receiver ===
class InputSpecificApiStripIsBlankStaticFactoryChainReceiverSliceViolation {
	boolean m(int value) {
		return String.valueOf(value).isBlank();
	}
}
// === end ===

// === case: strip_is_blank_string_literal_receiver ===
class InputSpecificApiStripIsBlankStringLiteralReceiverSliceViolation {
	boolean m() {
		return "  ".isBlank();
	}
}
// === end ===

// === case: strip_is_blank_super_call_on_a_classpath_supertype ===
class InputSpecificApiStripIsBlankSuperCallOnAClasspathSupertypeSliceViolation extends Thread {
	boolean m() {
		return super.getName().isBlank();
	}
}
// === end ===

// === case: strip_is_blank_super_call_on_a_covariant_override_returning_string ===
class InputSpecificApiStripIsBlankSuperCallOnACovariantOverrideReturningStringSliceViolation {
	static class Base {
		CharSequence source(String key) {
			return key;
		}
	}

	static class Derived extends Base {
		@Override
		String source(String key) {
			return key;
		}
	}

	static class Leaf extends Derived {
		boolean m() {
			return super.source("k").isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_super_call_returning_string ===
class InputSpecificApiStripIsBlankSuperCallReturningStringSliceViolation {
	static class Base {
		String source() {
			return "";
		}
	}

	static class Derived extends Base {
		boolean m() {
			return super.source().isBlank();
		}
	}
}
// === end ===

// === case: strip_is_blank_super_call_without_a_same_file_declaration ===
class InputSpecificApiStripIsBlankSuperCallWithoutASameFileDeclarationSliceViolation {
	boolean m() {
		return super.toString().isBlank();
	}
}
// === end ===

// === case: strip_is_blank_this_qualified_same_file_call_returning_string ===
class InputSpecificApiStripIsBlankThisQualifiedSameFileCallReturningStringSliceViolation {
	boolean m(String source) {
		return this.source().isBlank();
	}

	String source() {
		return "";
	}
}
// === end ===

// === case: strip_is_blank_this_qualified_string_field ===
class InputSpecificApiStripIsBlankThisQualifiedStringFieldSliceViolation {
	String name = "";

	boolean m(String name) {
		return this.name.isBlank();
	}
}
// === end ===

// === case: strip_is_blank_var_local_receiver ===
class InputSpecificApiStripIsBlankVarLocalReceiverSliceViolation {
	boolean m() {
		final var text = source();
		return text.isBlank();
	}

	String source() {
		return "";
	}
}
// === end ===

// === case: strip_is_blank_var_local_with_a_literal_initializer ===
class InputSpecificApiStripIsBlankVarLocalWithALiteralInitializerSliceViolation {
	boolean m() {
		final var text = "  ";
		return text.isBlank();
	}
}
// === end ===

// === case: strip_is_blank_var_local_with_a_proven_call_initializer ===
class InputSpecificApiStripIsBlankVarLocalWithAProvenCallInitializerSliceViolation {
	boolean m(int value) {
		final var text = String.valueOf(value);
		return text.isBlank();
	}
}
// === end ===

// === case: strip_is_empty ===
class InputSpecificApiStripIsBlankStripIsEmptySliceViolation {
	void stripIsEmpty(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_is_empty_already_negated ===
class InputSpecificApiStripIsEmptyAlreadyNegatedSliceViolation {
	boolean m(String s) {
		return !s.isBlank();
	}
}
// === end ===

// === case: strip_is_empty_pattern_in_string_not_anchored ===
class InputSpecificApiStripIsBlankStripIsEmptyPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".strip().isEmpty()".isEmpty() || s.isBlank())
			System.out.println("x");
	}
}
// === end ===

// === case: strip_length_equals_zero ===
class InputSpecificApiStripIsBlankStripLengthEqualsZeroSliceViolation {
	void stripLengthEqualsZero(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_greater_equal_one ===
class InputSpecificApiStripIsBlankStripLengthGreaterEqualOneSliceViolation {
	void stripLengthGreaterEqualOne(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_greater_than_zero ===
class InputSpecificApiStripIsBlankStripLengthGreaterThanZeroSliceViolation {
	void stripLengthGreaterThanZero(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_greater_than_zero_method_receiver ===
class InputSpecificApiStripLengthGreaterThanZeroMethodReceiverSliceViolation {
	String getText() {
		return "";
	}

	void stripLengthGreaterThanZeroMethodReceiver() {
		if (!getText().isBlank())
			return;
	}
}
// === end ===

// === case: strip_length_greater_than_zero_on_a_field_read_through_a_same_file_variable ===
class InputSpecificApiStripLengthGreaterThanZeroOnAFieldReadThroughASameFileVariableSliceViolation {
	static class Holder {
		String name;
	}

	boolean m(Holder holder) {
		return !holder.name.isBlank();
	}
}
// === end ===

// === case: strip_length_in_compound_reversed_condition ===
class InputSpecificApiStripIsBlankStripLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && !s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_less_equal_zero ===
class InputSpecificApiStripIsBlankStripLengthLessEqualZeroSliceViolation {
	void stripLengthLessEqualZero(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_less_than_one ===
class InputSpecificApiStripIsBlankStripLengthLessThanOneSliceViolation {
	void stripLengthLessThanOne(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_multiple_occurrences_first_rejected ===
class InputSpecificApiStripLengthMultipleOccurrencesFirstRejectedSliceViolation {
	void stripLengthMultipleOccurrencesFirstRejected(String a, String b) {
		if (a.strip().length() == 0xF || b.isBlank())
			return;
	}
}
// === end ===

// === case: strip_length_not_equals_zero ===
class InputSpecificApiStripIsBlankStripLengthNotEqualsZeroSliceViolation {
	void stripLengthNotEqualsZero(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_reversed_first_rejected_second_accepted ===
class InputSpecificApiStripLengthReversedFirstRejectedSecondAcceptedSliceViolation {
	void stripLengthReversedFirstRejectedSecondAccepted(String s, int idx10) {
		if (idx10 == 0 && s.isBlank())
			return;
	}
}
// === end ===

// === case: strip_length_reversed_method_receiver ===
class InputSpecificApiStripLengthReversedMethodReceiverSliceViolation {
	String getText() {
		return "";
	}

	void stripLengthReversedMethodReceiver() {
		if (getText().isBlank())
			return;
	}
}
// === end ===

// === case: strip_then_trim_length_equals_zero ===
class InputSpecificApiTrimLengthStripThenTrimLengthEqualsZeroSliceViolation {
	boolean m(String s) {
		return s.strip().trim().isEmpty();
	}
}
// === end ===

// === case: this_qualified_field_get_zero ===
// imports: java.util.List
// imports: java.util.Map
class InputSpecificApiReflectionThisQualifiedFieldGetZeroSliceViolation {
	private final List<String> items = List.of("a");

	void thisQualifiedFieldGetZero(Map<Integer, String> items) {
		System.out.println(this.items.getFirst());
	}
}
// === end ===

// === case: this_qualified_field_size_zero ===
// imports: java.util.Map
class InputSpecificApiIsEmptyThisQualifiedFieldSizeZeroSliceViolation {
	private final Map<Integer, String> byId = Map.of();

	void thisQualifiedFieldSizeZero(String byId) {
		if (this.byId.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: to_array_integer ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayIntegerSliceViolation {
	void toArrayInteger(List<Integer> list) {
		final var arr = list.toArray(Integer[]::new);
	}
}
// === end ===

// === case: to_array_method_receiver ===
class InputSpecificApiToArrayToArrayMethodReceiverSliceViolation {
	void toArrayMethodReceiver() {
		final var arr = getList().toArray(String[]::new);
	}
}
// === end ===

// === case: to_array_non_zero_first_rejected ===
// imports: java.util.List
class InputSpecificApiToArrayNonZeroFirstRejectedSliceViolation {
	int m(List<String> a, List<String> b) {
		return a.toArray(new String[5]).length + b.toArray(String[]::new).length;
	}
}
// === end ===

// === case: to_array_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".toArray(new String[0])" + list.toArray(String[]::new);
	}
}
// === end ===

// === case: to_array_qualified ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayQualifiedSliceViolation {
	void toArrayQualified(List<String> list) {
		final var arr = list.toArray(java.lang.String[]::new);
	}
}
// === end ===

// === case: to_array_string ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayStringSliceViolation {
	void toArrayString(List<String> list) {
		final var arr = list.toArray(String[]::new);
	}
}
// === end ===

// === case: trim_length_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthEqualsZeroSliceViolation {
	void trimLengthEqualsZero(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_equals_zero_chained_resolved_receiver ===
class InputSpecificApiTrimLengthEqualsZeroChainedResolvedReceiverSliceViolation {
	boolean m(Object obj) {
		return obj.toString().trim().isEmpty();
	}
}
// === end ===

// === case: trim_length_equals_zero_this_qualified_receiver ===
class InputSpecificApiTrimLengthEqualsZeroThisQualifiedReceiverSliceViolation {
	String name = "";

	boolean m(String name) {
		return this.name.trim().isEmpty();
	}
}
// === end ===

// === case: trim_length_greater_equal_one ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterEqualOneSliceViolation {
	void trimLengthGreaterEqualOne(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_greater_than_zero ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterThanZeroSliceViolation {
	void trimLengthGreaterThanZero(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_in_compound_reversed_condition ===
class InputSpecificApiTrimIsBlankTrimLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && !s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_less_equal_zero ===
class InputSpecificApiTrimIsBlankTrimLengthLessEqualZeroSliceViolation {
	void trimLengthLessEqualZero(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_less_than_one ===
class InputSpecificApiTrimIsBlankTrimLengthLessThanOneSliceViolation {
	void trimLengthLessThanOne(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_multiple_occurrences_first_rejected ===
class InputSpecificApiTrimLengthMultipleOccurrencesFirstRejectedSliceViolation {
	void trimLengthMultipleOccurrencesFirstRejected(String a, String b) {
		if (a.trim().length() == 0xF || b.trim().isEmpty())
			return;
	}
}
// === end ===

// === case: trim_length_not_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthNotEqualsZeroSliceViolation {
	void trimLengthNotEqualsZero(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_not_equals_zero_already_negated ===
class InputSpecificApiTrimLengthNotEqualsZeroAlreadyNegatedSliceViolation {
	boolean m(String s) {
		return !(!s.trim().isEmpty());
	}
}
// === end ===

// === case: trim_length_reversed_first_rejected_second_accepted ===
class InputSpecificApiTrimLengthReversedFirstRejectedSecondAcceptedSliceViolation {
	void trimLengthReversedFirstRejectedSecondAccepted(String s, int idx10) {
		if (idx10 == 0 && s.trim().isEmpty())
			return;
	}
}
// === end ===

// === case: trim_then_strip_length_equals_zero ===
class InputSpecificApiStripLengthTrimThenStripLengthEqualsZeroSliceViolation {
	boolean m(String s) {
		return s.trim().isBlank();
	}
}
// === end ===

// === case: unicode_escape ===
class InputSpecificApiIndexOfCharUnicodeEscapeSliceViolation {
	void unicodeEscape(String s) {
		final var i = s.indexOf('é');
		System.out.println(i);
	}
}
// === end ===

// === case: unmodifiable_as_list ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListSliceViolation {
	void unmodifiableAsList() {
		final var list = List.of("a", "b");
	}
}
// === end ===

// === case: unmodifiable_as_list_block_comment ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListBlockCommentSliceViolation {
	void unmodifiableAsListBlockComment() {
		final var list = List.of("a" /* ) */, "b");
	}
}
// === end ===

// === case: unmodifiable_as_list_nested_call ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListNestedCallSliceViolation {
	void unmodifiableAsListNestedCall() {
		final var list = List.of(String.valueOf(1), String.valueOf(2));
	}
}
// === end ===

// === case: unmodifiable_as_list_paren_in_char ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListParenInCharSliceViolation {
	void unmodifiableAsListParenInChar() {
		final var list = List.of(')', '(');
	}
}
// === end ===

// === case: unmodifiable_as_list_paren_in_string ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListParenInStringSliceViolation {
	void unmodifiableAsListParenInString() {
		final var list = List.of("(", ")");
	}
}
// === end ===

// === case: unmodifiable_as_list_plain_chars ===
// imports: java.util.Arrays
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableAsListPlainCharsSliceViolation {
	void unmodifiableAsListPlainChars() {
		final var list = List.of('a', 'b');
	}
}
// === end ===

// === case: unmodifiable_list ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableListSliceViolation {
	void unmodifiableList(List<String> list) {
		final var result = List.copyOf(list);
	}
}
// === end ===

// === case: unmodifiable_map ===
// imports: java.util.Collections
// imports: java.util.Map
class InputSpecificApiCopyOfUnmodifiableMapSliceViolation {
	void unmodifiableMap(Map<String, String> map) {
		final var result = Map.copyOf(map);
	}
}
// === end ===

// === case: unmodifiable_set ===
// imports: java.util.Collections
// imports: java.util.Set
class InputSpecificApiCopyOfUnmodifiableSetSliceViolation {
	void unmodifiableSet(Set<String> set) {
		final var result = Set.copyOf(set);
	}
}
// === end ===

// === case: unresolvable_resource_receiver_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfUnresolvableResourceReceiverIndexOfSliceViolation {
	void unresolvableResourceReceiverIndexOf() throws Exception {
		try (var buffer = Factory.make()) {
			if (buffer.indexOf("xy") >= 0)
				System.out.println("found");
		}
	}
}
// === end ===

// === case: values_contains ===
// imports: java.util.Map
class InputSpecificApiMapChainValuesContainsSliceViolation {
	void valuesContains(Map<String, String> map) {
		if (map.containsValue("value"))
			System.out.println("found");
	}
}
// === end ===

// === case: values_contains_pattern_in_string_not_anchored ===
// imports: java.util.Map
class InputSpecificApiMapChainValuesContainsPatternInStringNotAnchoredSliceViolation {
	void m(Map<String, String> map) {
		if (".values().contains(".isEmpty() || map.containsValue("value"))
			System.out.println("x");
	}
}
// === end ===

// === case: var_local_get_size_minus_one ===
// imports: java.util.Map
class InputSpecificApiReflectionVarLocalGetSizeMinusOneSliceViolation {
	void varLocalGetSizeMinusOne() {
		final var map = Map.of(0, "a", 1, "b");
		System.out.println(map.getLast());
	}
}
// === end ===

// === case: zero_equals_length ===
class InputSpecificApiIsEmptyZeroEqualsLengthSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_equals_length_pattern_in_string_not_anchored ===
class InputSpecificApiIsEmptyZeroEqualsLengthPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if ("0 == s.length()".isEmpty() || s.isEmpty())
			System.out.println("x");
	}
}
// === end ===

// === case: zero_equals_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroEqualsSizeSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_equals_strip_length ===
class InputSpecificApiStripIsBlankZeroEqualsStripLengthSliceViolation {
	void zeroEqualsStripLength(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_equals_strip_length_string_literal_receiver ===
class InputSpecificApiZeroEqualsStripLengthStringLiteralReceiverSliceViolation {
	boolean m() {
		return " x ".isBlank();
	}
}
// === end ===

// === case: zero_equals_trim_length ===
class InputSpecificApiTrimIsBlankZeroEqualsTrimLengthSliceViolation {
	void zeroEqualsTrimLength(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_equal_strip_length ===
class InputSpecificApiStripIsBlankZeroGreaterEqualStripLengthSliceViolation {
	void zeroGreaterEqualStripLength(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_equal_trim_length ===
class InputSpecificApiTrimIsBlankZeroGreaterEqualTrimLengthSliceViolation {
	void zeroGreaterEqualTrimLength(String s) {
		if (s.trim().isEmpty())
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfZeroGreaterIndexOfSliceViolation {
	void zeroGreaterIndexOf(String s) {
		if (0 > s.indexOf("dd"))
			System.out.println("not found");
	}
}
// === end ===

// === case: zero_greater_than_or_equal_length ===
class InputSpecificApiIsEmptyZeroGreaterThanOrEqualLengthSliceViolation {
	void m(String s) {
		if (s.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_greater_than_or_equal_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroGreaterThanOrEqualSizeSliceViolation {
	void m(List<String> list) {
		if (list.isEmpty())
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_less_equal_index_of ===
// skip-reason: cannot rewrite: unrecognized API pattern
class InputSpecificApiIndexOfZeroLessEqualIndexOfSliceViolation {
	void zeroLessEqualIndexOf(String s) {
		if (0 <= s.indexOf("cc"))
			System.out.println("found");
	}
}
// === end ===

// === case: zero_less_than_length ===
class InputSpecificApiIsEmptyZeroLessThanLengthSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_less_than_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroLessThanSizeSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_less_than_strip_length ===
class InputSpecificApiStripIsBlankZeroLessThanStripLengthSliceViolation {
	void zeroLessThanStripLength(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_less_than_trim_length ===
class InputSpecificApiTrimIsBlankZeroLessThanTrimLengthSliceViolation {
	void zeroLessThanTrimLength(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_not_equals_length ===
class InputSpecificApiIsEmptyZeroNotEqualsLengthSliceViolation {
	void m(String s) {
		if (!s.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_not_equals_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroNotEqualsSizeSliceViolation {
	void m(List<String> list) {
		if (!list.isEmpty())
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_not_equals_strip_length ===
class InputSpecificApiStripIsBlankZeroNotEqualsStripLengthSliceViolation {
	void zeroNotEqualsStripLength(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_not_equals_trim_length ===
class InputSpecificApiTrimIsBlankZeroNotEqualsTrimLengthSliceViolation {
	void zeroNotEqualsTrimLength(String s) {
		if (!s.trim().isEmpty())
			System.out.println("not blank");
	}
}
// === end ===