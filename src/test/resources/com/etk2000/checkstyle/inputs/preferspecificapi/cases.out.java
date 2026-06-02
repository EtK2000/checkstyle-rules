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

// === case: chained_call_get_zero ===
class InputSpecificApiReflectionChainedCallGetZeroSliceViolation {
	void chainedCallGetZero() {
		System.out.println(getList().getFirst());
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

// === case: equals_empty_pattern_in_comment_not_anchored ===
class InputSpecificApiStringMethodEqualsEmptyPatternInCommentNotAnchoredSliceViolation {
	void m(String s) {
		if (/* .equals("") */ s.isEmpty())
			System.out.println("x");
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

// === case: format_single_cast ===
class InputSpecificApiStringFormatFormatSingleCastSliceViolation {
	void formatSingleCast(Object obj) {
		final var s = (String) obj;
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

// === case: format_single_method_call ===
class InputSpecificApiStringFormatFormatSingleMethodCallSliceViolation {
	void formatSingleMethodCall(Object obj) {
		final var s = obj.toString();
	}
}
// === end ===

// === case: format_single_variable ===
class InputSpecificApiStringFormatFormatSingleVariableSliceViolation {
	void formatSingleVariable(String fmt) {
		final var s = fmt;
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

// === case: get_zero_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiGetZeroPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".get(0)" + list.getFirst();
	}
}
// === end ===

// === case: index_of_equal_neg_one ===
// skip-reason: unrecognized API pattern
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
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterEqualZeroSliceViolation {
	void indexOfGreaterEqualZero(String s) {
		if (s.indexOf("bar") >= 0)
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_greater_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterNegOneSliceViolation {
	void indexOfGreaterNegOne(String s) {
		if (s.indexOf("ee") > -1)
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_less_equal_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessEqualNegOneSliceViolation {
	void indexOfLessEqualNegOne(String s) {
		if (s.indexOf("ff") <= -1)
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_less_than_zero ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessThanZeroSliceViolation {
	void indexOfLessThanZero(String s) {
		if (s.indexOf("qux") < 0)
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_not_equal_neg_one ===
// skip-reason: unrecognized API pattern
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
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneEqualIndexOfSliceViolation {
	void negOneEqualIndexOf(String s) {
		if (-1 == s.indexOf("bb"))
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_greater_equal_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneGreaterEqualIndexOfSliceViolation {
	void negOneGreaterEqualIndexOf(String s) {
		if (-1 >= s.indexOf("hh"))
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_less_than_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneLessThanIndexOfSliceViolation {
	void negOneLessThanIndexOf(String s) {
		if (-1 < s.indexOf("gg"))
			System.out.println("found");
	}
}
// === end ===

// === case: neg_one_not_equal_index_of ===
// skip-reason: unrecognized API pattern
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
		if (s.isBlank())
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
		if (!s.isBlank())
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

// === case: remove_first_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".remove(0)" + list.removeFirst();
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

// === case: replace_all_literal ===
class InputSpecificApiStringMethodReplaceAllLiteralSliceViolation {
	void replaceAllLiteral(String s) {
		final var result = s.replace("foo", "bar");
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

// === case: stream_count ===
// imports: java.util.List
class InputSpecificApiStreamStreamCountSliceViolation {
	void streamCount(List<String> list) {
		final var count = list.size();
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

// === case: strip_is_empty ===
class InputSpecificApiStripIsBlankStripIsEmptySliceViolation {
	void stripIsEmpty(String s) {
		if (s.isBlank())
			System.out.println("blank");
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

// === case: strip_length_not_equals_zero ===
class InputSpecificApiStripIsBlankStripLengthNotEqualsZeroSliceViolation {
	void stripLengthNotEqualsZero(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
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

// === case: trim_is_empty ===
class InputSpecificApiTrimIsBlankTrimIsEmptySliceViolation {
	void trimIsEmpty(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_is_empty_pattern_in_string_not_anchored ===
class InputSpecificApiTrimIsBlankTrimIsEmptyPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".trim().isEmpty()".isEmpty() || s.isBlank())
			System.out.println("x");
	}
}
// === end ===

// === case: trim_length_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthEqualsZeroSliceViolation {
	void trimLengthEqualsZero(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_greater_equal_one ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterEqualOneSliceViolation {
	void trimLengthGreaterEqualOne(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_greater_than_zero ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterThanZeroSliceViolation {
	void trimLengthGreaterThanZero(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_in_compound_reversed_condition ===
class InputSpecificApiTrimIsBlankTrimLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && !s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_less_equal_zero ===
class InputSpecificApiTrimIsBlankTrimLengthLessEqualZeroSliceViolation {
	void trimLengthLessEqualZero(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_less_than_one ===
class InputSpecificApiTrimIsBlankTrimLengthLessThanOneSliceViolation {
	void trimLengthLessThanOne(String s) {
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_not_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthNotEqualsZeroSliceViolation {
	void trimLengthNotEqualsZero(String s) {
		if (!s.isBlank())
			System.out.println("not blank");
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

// === case: zero_equals_trim_length ===
class InputSpecificApiTrimIsBlankZeroEqualsTrimLengthSliceViolation {
	void zeroEqualsTrimLength(String s) {
		if (s.isBlank())
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
		if (s.isBlank())
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_index_of ===
// skip-reason: unrecognized API pattern
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
// skip-reason: unrecognized API pattern
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
		if (!s.isBlank())
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
		if (!s.isBlank())
			System.out.println("not blank");
	}
}
// === end ===