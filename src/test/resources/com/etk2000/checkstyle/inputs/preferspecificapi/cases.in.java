package com.etk2000.checkstyle.inputs.preferspecificapi;

// === case: as_list_multiple_args ===
// imports: java.util.Arrays
class InputSpecificApiArraysAsListAsListMultipleArgsSliceViolation {
	void asListMultipleArgs() {
		final var list = Arrays.asList("a", "b", "c"); // violation [minSdk>=30]: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}
}
// === end ===

// === case: as_list_no_args ===
// imports: java.util.Arrays
class InputSpecificApiArraysAsListAsListNoArgsSliceViolation {
	void asListNoArgs() {
		final var list = Arrays.asList(); // violation [minSdk>=30]: Use 'List.of()' instead of 'Arrays.asList()'.
	}
}
// === end ===

// === case: as_list_pattern_in_string_not_anchored ===
// imports: java.util.Arrays
class InputSpecificApiArraysAsListAsListPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		final var list = "Arrays.asList(" + Arrays.asList(s); // violation [minSdk>=30]: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}
}
// === end ===

// === case: as_list_single_arg ===
// imports: java.util.Arrays
class InputSpecificApiArraysAsListAsListSingleArgSliceViolation {
	void asListSingleArg(String s) {
		final var list = Arrays.asList(s); // violation [minSdk>=30]: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}
}
// === end ===

// === case: as_list_text_block_continuation ===
// imports: java.util.Arrays
class InputSpecificApiArraysAsListAsListTextBlockContinuationSliceViolation {
	String m() {
		return """
				""" + Arrays.asList("a").toString(); // violation [minSdk>=30]: Use 'List.of(...)' instead of 'Arrays.asList(...)'.
	}
}
// === end ===

// === case: assert_equals_false ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsFalseSliceViolation {
	void assertEqualsFalse() {
		assertEquals(false, 1 == 2); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_false_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsFalseReversedSliceViolation {
	void assertEqualsFalseReversed() {
		assertEquals(1 == 2, false); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_false_wildcard_import ===
// imports: static org.junit.Assert.*
class InputSpecificApiAssertAssertEqualsFalseWildcardImportSliceViolation {
	void assertEqualsFalseWildcardImport(boolean result) {
		assertEquals(false, result); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_null ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsNullSliceViolation {
	void assertEqualsNull() {
		assertEquals(null, new Object()); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_equals_null_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsNullReversedSliceViolation {
	void assertEqualsNullReversed() {
		assertEquals(new Object(), null); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_equals_true ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsTrueSliceViolation {
	void assertEqualsTrue() {
		assertEquals(true, 1 == 1); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_equals_true_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsTrueReversedSliceViolation {
	void assertEqualsTrueReversed() {
		assertEquals(1 == 1, true); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_false ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageFalseSliceViolation {
	void assertEqualsWithMessageFalse() {
		assertEquals("msg", false, 1 == 2); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_false_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageFalseReversedSliceViolation {
	void assertEqualsWithMessageFalseReversed() {
		assertEquals("msg", 1 == 2, false); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_null ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageNullSliceViolation {
	void assertEqualsWithMessageNull() {
		assertEquals("msg", null, new Object()); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_null_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageNullReversedSliceViolation {
	void assertEqualsWithMessageNullReversed() {
		assertEquals("msg", new Object(), null); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_true ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageTrueSliceViolation {
	void assertEqualsWithMessageTrue() {
		assertEquals("msg", true, 1 == 1); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_equals_with_message_true_reversed ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithMessageTrueReversedSliceViolation {
	void assertEqualsWithMessageTrueReversed() {
		assertEquals("msg", 1 == 1, true); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_false ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithTrailingMessageFalseSliceViolation {
	void assertEqualsWithTrailingMessageFalse() {
		assertEquals(false, 1 == 2, "msg"); // violation: Use 'assertFalse' instead of 'assertEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_null ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithTrailingMessageNullSliceViolation {
	void assertEqualsWithTrailingMessageNull() {
		assertEquals(null, new Object(), "msg"); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_equals_with_trailing_message_true ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertEqualsWithTrailingMessageTrueSliceViolation {
	void assertEqualsWithTrailingMessageTrue() {
		assertEquals(true, 1 == 1, "msg"); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_literal_first_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertLiteralFirstPatternInCommentNotAnchoredSliceViolation {
	void m() {
		/* assertEquals(true, x) */ assertEquals(true, 1 == 1); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_literal_last_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertLiteralLastPatternInCommentNotAnchoredSliceViolation {
	void m(boolean b) {
		/* assertEquals(x, true) */ assertEquals(b, true); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_literal_middle_pattern_in_comment_not_anchored ===
// imports: static org.junit.Assert.assertEquals
class InputSpecificApiAssertAssertLiteralMiddlePatternInCommentNotAnchoredSliceViolation {
	void m() {
		/* assertEquals(a, null, b) */ assertEquals("msg", null, new Object()); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_false ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsFalseSliceViolation {
	void assertNotEqualsFalse() {
		assertNotEquals(false, 1 == 1); // violation: Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_not_equals_false_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsFalseReversedSliceViolation {
	void assertNotEqualsFalseReversed() {
		assertNotEquals(1 == 1, false); // violation: Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_not_equals_null ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsNullSliceViolation {
	void assertNotEqualsNull() {
		assertNotEquals(null, new Object()); // violation: Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_null_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsNullReversedSliceViolation {
	void assertNotEqualsNullReversed() {
		assertNotEquals(new Object(), null); // violation: Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_true ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsTrueSliceViolation {
	void assertNotEqualsTrue() {
		assertNotEquals(true, 1 == 2); // violation: Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_not_equals_true_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsTrueReversedSliceViolation {
	void assertNotEqualsTrueReversed() {
		assertNotEquals(1 == 2, true); // violation: Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_false ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageFalseSliceViolation {
	void assertNotEqualsWithMessageFalse() {
		assertNotEquals("msg", false, 1 == 1); // violation: Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_false_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageFalseReversedSliceViolation {
	void assertNotEqualsWithMessageFalseReversed() {
		assertNotEquals("msg", 1 == 1, false); // violation: Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_null ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageNullSliceViolation {
	void assertNotEqualsWithMessageNull() {
		assertNotEquals("msg", null, new Object()); // violation: Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_null_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageNullReversedSliceViolation {
	void assertNotEqualsWithMessageNullReversed() {
		assertNotEquals("msg", new Object(), null); // violation: Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_true ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageTrueSliceViolation {
	void assertNotEqualsWithMessageTrue() {
		assertNotEquals("msg", true, 1 == 2); // violation: Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_message_true_reversed ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithMessageTrueReversedSliceViolation {
	void assertNotEqualsWithMessageTrueReversed() {
		assertNotEquals("msg", 1 == 2, true); // violation: Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_false ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageFalseSliceViolation {
	void assertNotEqualsWithTrailingMessageFalse() {
		assertNotEquals(false, 1 == 1, "msg"); // violation: Use 'assertTrue' instead of 'assertNotEquals' with a 'false' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_null ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageNullSliceViolation {
	void assertNotEqualsWithTrailingMessageNull() {
		assertNotEquals(null, new Object(), "msg"); // violation: Use 'assertNotNull' instead of 'assertNotEquals' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_equals_with_trailing_message_true ===
// imports: static org.junit.Assert.assertNotEquals
class InputSpecificApiAssertAssertNotEqualsWithTrailingMessageTrueSliceViolation {
	void assertNotEqualsWithTrailingMessageTrue() {
		assertNotEquals(true, 1 == 2, "msg"); // violation: Use 'assertFalse' instead of 'assertNotEquals' with a 'true' literal.
	}
}
// === end ===

// === case: assert_not_same_null ===
// imports: static org.junit.Assert.assertNotSame
class InputSpecificApiAssertAssertNotSameNullSliceViolation {
	void assertNotSameNull() {
		assertNotSame(null, new Object()); // violation: Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_same_null_reversed ===
// imports: static org.junit.Assert.assertNotSame
class InputSpecificApiAssertAssertNotSameNullReversedSliceViolation {
	void assertNotSameNullReversed() {
		assertNotSame(new Object(), null); // violation: Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_same_with_message ===
// imports: static org.junit.Assert.assertNotSame
class InputSpecificApiAssertAssertNotSameWithMessageSliceViolation {
	void assertNotSameWithMessage() {
		assertNotSame("msg", null, new Object()); // violation: Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_same_with_message_reversed ===
// imports: static org.junit.Assert.assertNotSame
class InputSpecificApiAssertAssertNotSameWithMessageReversedSliceViolation {
	void assertNotSameWithMessageReversed() {
		assertNotSame("msg", new Object(), null); // violation: Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_not_same_with_trailing_message ===
// imports: static org.junit.Assert.assertNotSame
class InputSpecificApiAssertAssertNotSameWithTrailingMessageSliceViolation {
	void assertNotSameWithTrailingMessage() {
		assertNotSame(null, new Object(), "msg"); // violation: Use 'assertNotNull' instead of 'assertNotSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_same_null ===
// imports: static org.junit.Assert.assertSame
class InputSpecificApiAssertAssertSameNullSliceViolation {
	void assertSameNull() {
		assertSame(null, new Object()); // violation: Use 'assertNull' instead of 'assertSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_same_null_reversed ===
// imports: static org.junit.Assert.assertSame
class InputSpecificApiAssertAssertSameNullReversedSliceViolation {
	void assertSameNullReversed() {
		assertSame(new Object(), null); // violation: Use 'assertNull' instead of 'assertSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_same_with_message ===
// imports: static org.junit.Assert.assertSame
class InputSpecificApiAssertAssertSameWithMessageSliceViolation {
	void assertSameWithMessage() {
		assertSame("msg", null, new Object()); // violation: Use 'assertNull' instead of 'assertSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_same_with_message_reversed ===
// imports: static org.junit.Assert.assertSame
class InputSpecificApiAssertAssertSameWithMessageReversedSliceViolation {
	void assertSameWithMessageReversed() {
		assertSame("msg", new Object(), null); // violation: Use 'assertNull' instead of 'assertSame' with a 'null' literal.
	}
}
// === end ===

// === case: assert_same_with_trailing_message ===
// imports: static org.junit.Assert.assertSame
class InputSpecificApiAssertAssertSameWithTrailingMessageSliceViolation {
	void assertSameWithTrailingMessage() {
		assertSame(null, new Object(), "msg"); // violation: Use 'assertNull' instead of 'assertSame' with a 'null' literal.
	}
}
// === end ===

// === case: chained_call_get_zero ===
class InputSpecificApiReflectionChainedCallGetZeroSliceViolation {
	void chainedCallGetZero() {
		System.out.println(getList().get(0)); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: chained_call_resolved_get_zero ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiReflectionChainedCallResolvedGetZeroSliceViolation {
	void chainedCallResolvedGetZero(List<String> list) {
		System.out.println(Collections.synchronizedList(list).get(0)); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: char_sequence_length_equals_zero ===
class InputSpecificApiIsEmptyCharSequenceLengthEqualsZeroSliceViolation {
	void m(CharSequence cs) {
		if (cs.length() == 0) // violation [minSdk>=35]: Use '.isEmpty()' instead of '.length() == 0'.
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
		final var result = ".collect(Collectors.toList())" + list.stream().collect(Collectors.toList()); // violation: Use '.toList()' instead of '.collect(Collectors.toList())'.
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
				.collect(Collectors.toList()); // violation: Use '.toList()' instead of '.collect(Collectors.toList())'.
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
				.collect(Collectors.toUnmodifiableList()); // violation: Use '.toList()' instead of '.collect(Collectors.toUnmodifiableList())'.
	}
}
// === end ===

// === case: double_quote_escape ===
class InputSpecificApiIndexOfCharDoubleQuoteEscapeSliceViolation {
	void doubleQuoteEscape(String s) {
		final var i = s.indexOf("\""); // violation: Use 'indexOf('"')' instead of 'indexOf("\"")'.
		System.out.println(i);
	}
}
// === end ===

// === case: empty_list ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptyEmptyListSliceViolation {
	void emptyList() {
		final var list = Collections.emptyList(); // violation [minSdk>=30]: Use 'List.of()' instead of 'Collections.emptyList()'.
	}
}
// === end ===

// === case: empty_list_pattern_in_string_not_anchored ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptyEmptyListPatternInStringNotAnchoredSliceViolation {
	void m() {
		final var x = "Collections.emptyList()" + Collections.emptyList(); // violation [minSdk>=30]: Use 'List.of()' instead of 'Collections.emptyList()'.
	}
}
// === end ===

// === case: empty_map ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptyEmptyMapSliceViolation {
	void emptyMap() {
		final var map = Collections.emptyMap(); // violation [minSdk>=30]: Use 'Map.of()' instead of 'Collections.emptyMap()'.
	}
}
// === end ===

// === case: empty_set ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptyEmptySetSliceViolation {
	void emptySet() {
		final var set = Collections.emptySet(); // violation [minSdk>=30]: Use 'Set.of()' instead of 'Collections.emptySet()'.
	}
}
// === end ===

// === case: equals_empty ===
class InputSpecificApiStringMethodEqualsEmptySliceViolation {
	void equalsEmpty(String s) {
		if (s.equals("")) // violation: Use '.isEmpty()' instead of '.equals("")'.
			System.out.println("empty");
	}
}
// === end ===

// === case: equals_empty_pattern_in_comment_not_anchored ===
class InputSpecificApiStringMethodEqualsEmptyPatternInCommentNotAnchoredSliceViolation {
	void m(String s) {
		if (/* .equals("") */ s.equals("")) // violation: Use '.isEmpty()' instead of '.equals("")'.
			System.out.println("x");
	}
}
// === end ===

// === case: escape_backslash ===
class InputSpecificApiIndexOfCharEscapeBackslashSliceViolation {
	void escapeBackslash(String s) {
		final var i = s.indexOf("\\"); // violation: Use 'indexOf('\\')' instead of 'indexOf("\\")'.
		System.out.println(i);
	}
}
// === end ===

// === case: escape_newline ===
class InputSpecificApiIndexOfCharEscapeNewlineSliceViolation {
	void escapeNewline(String s) {
		final var i = s.indexOf("\n"); // violation: Use 'indexOf('\n')' instead of 'indexOf("\n")'.
		System.out.println(i);
	}
}
// === end ===

// === case: format_block_comment ===
class InputSpecificApiStringFormatFormatBlockCommentSliceViolation {
	void formatBlockComment(String name) {
		final var s = String.format("Hi %s", name /* ) */); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_char_literal_in_args ===
class InputSpecificApiStringFormatFormatCharLiteralInArgsSliceViolation {
	void formatCharLiteralInArgs() {
		final var s = String.format("%c", ')'); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_escaped_quotes ===
class InputSpecificApiStringFormatFormatEscapedQuotesSliceViolation {
	void formatEscapedQuotes(String name) {
		final var s = String.format("Say \"hi\"", name); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_nested_parens ===
class InputSpecificApiStringFormatFormatNestedParensSliceViolation {
	void formatNestedParens() {
		final var s = String.format("Hello %s", getName()); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}

	String getName() {
		return "x";
	}
}
// === end ===

// === case: format_one_arg ===
class InputSpecificApiStringFormatFormatOneArgSliceViolation {
	void formatOneArg(String name) {
		final var s = String.format("Hello %s", name); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_pattern_in_string_not_anchored ===
class InputSpecificApiStringFormatFormatPatternInStringNotAnchoredSliceViolation {
	void m(int n) {
		final var s = "String.format(" + String.format("%d", n); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_single_cast ===
class InputSpecificApiStringFormatFormatSingleCastSliceViolation {
	void formatSingleCast(Object obj) {
		final var s = String.format((String) obj); // violation [minSdk>=34]: Use 'the value directly' instead of 'String.format(value)'.
	}
}
// === end ===

// === case: format_single_literal ===
class InputSpecificApiStringFormatFormatSingleLiteralSliceViolation {
	void formatSingleLiteral() {
		final var s = String.format("literal"); // violation [minSdk>=34]: Use 'the value directly' instead of 'String.format(value)'.
	}
}
// === end ===

// === case: format_single_method_call ===
class InputSpecificApiStringFormatFormatSingleMethodCallSliceViolation {
	void formatSingleMethodCall(Object obj) {
		final var s = String.format(obj.toString()); // violation [minSdk>=34]: Use 'the value directly' instead of 'String.format(value)'.
	}
}
// === end ===

// === case: format_single_variable ===
class InputSpecificApiStringFormatFormatSingleVariableSliceViolation {
	void formatSingleVariable(String fmt) {
		final var s = String.format(fmt); // violation [minSdk>=34]: Use 'the value directly' instead of 'String.format(value)'.
	}
}
// === end ===

// === case: format_string_literal_paren ===
class InputSpecificApiStringFormatFormatStringLiteralParenSliceViolation {
	void formatStringLiteralParen(String name) {
		final var s = String.format("Result (%s)", name); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_trailing_backslash_in_literal ===
class InputSpecificApiStringFormatFormatTrailingBackslashInLiteralSliceViolation {
	void formatTrailingBackslashInLiteral(String path) {
		final var s = String.format("dir\\", path); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: format_two_args ===
class InputSpecificApiStringFormatFormatTwoArgsSliceViolation {
	void formatTwoArgs(String name, int age) {
		final var s = String.format("Hello %s, age %d", name, age); // violation [minSdk>=34]: Use '.formatted(...)' instead of 'String.format(...)'.
	}
}
// === end ===

// === case: get_size_minus_one ===
// imports: java.util.List
class InputSpecificApiGetSizeMinusOneSliceViolation {
	void getSizeMinusOne(List<String> list) {
		System.out.println(list.get(list.size() - 1)); // violation [minSdk>=35]: Use '.getLast()' instead of '.get(size() - 1)'.
	}
}
// === end ===

// === case: get_size_minus_one_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiGetSizeMinusOnePatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = "list.get(list.size() - 1)" + list.get(list.size() - 1); // violation [minSdk>=35]: Use '.getLast()' instead of '.get(size() - 1)'.
	}
}
// === end ===

// === case: get_zero ===
// imports: java.util.List
class InputSpecificApiGetZeroSliceViolation {
	void getZero(List<String> list) {
		System.out.println(list.get(0)); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: get_zero_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiGetZeroPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".get(0)" + list.get(0); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: index_of_equal_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfEqualNegOneSliceViolation {
	void indexOfEqualNegOne(String s) {
		if (s.indexOf("baz") == -1) // violation: Use '!.contains(...)' instead of '.indexOf(...) == -1'.
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_from_index ===
class InputSpecificApiIndexOfCharIndexOfFromIndexSliceViolation {
	void indexOfFromIndex(String s) {
		final var i = s.indexOf("x", 5); // violation: Use 'indexOf('x')' instead of 'indexOf("x")'.
		System.out.println(i);
	}
}
// === end ===

// === case: index_of_greater_equal_zero ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterEqualZeroSliceViolation {
	void indexOfGreaterEqualZero(String s) {
		if (s.indexOf("bar") >= 0) // violation: Use '.contains(...)' instead of '.indexOf(...) >= 0'.
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_greater_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfGreaterNegOneSliceViolation {
	void indexOfGreaterNegOne(String s) {
		if (s.indexOf("ee") > -1) // violation: Use '.contains(...)' instead of '.indexOf(...) > -1'.
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_less_equal_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessEqualNegOneSliceViolation {
	void indexOfLessEqualNegOne(String s) {
		if (s.indexOf("ff") <= -1) // violation: Use '!.contains(...)' instead of '.indexOf(...) <= -1'.
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_less_than_zero ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfLessThanZeroSliceViolation {
	void indexOfLessThanZero(String s) {
		if (s.indexOf("qux") < 0) // violation: Use '!.contains(...)' instead of '.indexOf(...) < 0'.
			System.out.println("not found");
	}
}
// === end ===

// === case: index_of_not_equal_neg_one ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfIndexOfNotEqualNegOneSliceViolation {
	void indexOfNotEqualNegOne(String s) {
		if (s.indexOf("foo") != -1) // violation: Use '.contains(...)' instead of '.indexOf(...) != -1'.
			System.out.println("found");
	}
}
// === end ===

// === case: index_of_single_char ===
class InputSpecificApiIndexOfCharIndexOfSingleCharSliceViolation {
	void indexOfSingleChar(String s) {
		final var i = s.indexOf("x"); // violation: Use 'indexOf('x')' instead of 'indexOf("x")'.
		System.out.println(i);
	}
}
// === end ===

// === case: key_set_contains ===
// imports: java.util.Map
class InputSpecificApiMapChainKeySetContainsSliceViolation {
	void keySetContains(Map<String, String> map) {
		if (map.keySet().contains("key")) // violation: Use '.containsKey(...)' instead of '.keySet().contains(...)'.
			System.out.println("found");
	}
}
// === end ===

// === case: key_set_contains_pattern_in_string_not_anchored ===
// imports: java.util.Map
class InputSpecificApiMapChainKeySetContainsPatternInStringNotAnchoredSliceViolation {
	void m(Map<String, String> map) {
		if (".keySet().contains(".isEmpty() || map.keySet().contains("key")) // violation: Use '.containsKey(...)' instead of '.keySet().contains(...)'.
			System.out.println("x");
	}
}
// === end ===

// === case: last_index_of_single_char ===
class InputSpecificApiIndexOfCharLastIndexOfSingleCharSliceViolation {
	void lastIndexOfSingleChar(String s) {
		final var i = s.lastIndexOf("/"); // violation: Use 'lastIndexOf('/')' instead of 'lastIndexOf("/")'.
		System.out.println(i);
	}
}
// === end ===

// === case: length_equals_zero ===
class InputSpecificApiIsEmptyLengthEqualsZeroSliceViolation {
	void m(String s) {
		if (s.length() == 0) // violation: Use '.isEmpty()' instead of '.length() == 0'.
			System.out.println("empty");
	}
}
// === end ===

// === case: length_greater_than_or_equal_one ===
class InputSpecificApiIsEmptyLengthGreaterThanOrEqualOneSliceViolation {
	void m(String s) {
		if (s.length() >= 1) // violation: Use '.!isEmpty()' instead of '.length() >= 1'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_greater_than_zero ===
class InputSpecificApiIsEmptyLengthGreaterThanZeroSliceViolation {
	void m(String s) {
		if (s.length() > 0) // violation: Use '.!isEmpty()' instead of '.length() > 0'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_in_compound_reversed_condition ===
class InputSpecificApiIsEmptyLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && 0 != s.length()) // violation: Use '.!isEmpty()' instead of '0 != .length()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_less_than_one ===
class InputSpecificApiIsEmptyLengthLessThanOneSliceViolation {
	void m(String s) {
		if (s.length() < 1) // violation: Use '.isEmpty()' instead of '.length() < 1'.
			System.out.println("empty");
	}
}
// === end ===

// === case: length_less_than_or_equal_zero ===
class InputSpecificApiIsEmptyLengthLessThanOrEqualZeroSliceViolation {
	void m(String s) {
		if (s.length() <= 0) // violation: Use '.isEmpty()' instead of '.length() <= 0'.
			System.out.println("empty");
	}
}
// === end ===

// === case: length_not_equals_zero ===
class InputSpecificApiIsEmptyLengthNotEqualsZeroSliceViolation {
	void m(String s) {
		if (s.length() != 0) // violation: Use '.!isEmpty()' instead of '.length() != 0'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: length_pattern_in_string_not_anchored ===
class InputSpecificApiIsEmptyLengthPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".length() == 0".isEmpty() || s.length() == 0) // violation: Use '.isEmpty()' instead of '.length() == 0'.
			System.out.println("x");
	}
}
// === end ===

// === case: length_reversed_first_rejected_second_accepted ===
class InputSpecificApiIsEmptyLengthReversedFirstRejectedSecondAcceptedSliceViolation {
	void m(String s, int idx10) {
		if (idx10 == 0 && 0 == s.length()) // violation: Use '.isEmpty()' instead of '0 == .length()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: list_local_get_zero ===
// imports: java.util.List
class InputSpecificApiReflectionListLocalGetZeroSliceViolation {
	void listLocalGetZero() {
		final var list = List.of("a");
		System.out.println(list.get(0)); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: list_param_get_zero ===
// imports: java.util.List
class InputSpecificApiReflectionListParamGetZeroSliceViolation {
	void listParamGetZero(List<String> list) {
		System.out.println(list.get(0)); // violation [minSdk>=35]: Use '.getFirst()' instead of '.get(0)'.
	}
}
// === end ===

// === case: neg_one_equal_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneEqualIndexOfSliceViolation {
	void negOneEqualIndexOf(String s) {
		if (-1 == s.indexOf("bb")) // violation: Use '!.contains(...)' instead of '-1 == .indexOf(...)'.
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_greater_equal_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneGreaterEqualIndexOfSliceViolation {
	void negOneGreaterEqualIndexOf(String s) {
		if (-1 >= s.indexOf("hh")) // violation: Use '!.contains(...)' instead of '-1 >= .indexOf(...)'.
			System.out.println("not found");
	}
}
// === end ===

// === case: neg_one_less_than_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneLessThanIndexOfSliceViolation {
	void negOneLessThanIndexOf(String s) {
		if (-1 < s.indexOf("gg")) // violation: Use '.contains(...)' instead of '-1 < .indexOf(...)'.
			System.out.println("found");
	}
}
// === end ===

// === case: neg_one_not_equal_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfNegOneNotEqualIndexOfSliceViolation {
	void negOneNotEqualIndexOf(String s) {
		if (-1 != s.indexOf("aa")) // violation: Use '.contains(...)' instead of '-1 != .indexOf(...)'.
			System.out.println("found");
	}
}
// === end ===

// === case: octal_escape ===
class InputSpecificApiIndexOfCharOctalEscapeSliceViolation {
	void octalEscape(String s) {
		final var i = s.indexOf("\077"); // violation: Use 'indexOf('\077')' instead of 'indexOf("\077")'.
		System.out.println(i);
	}
}
// === end ===

// === case: one_greater_than_length ===
class InputSpecificApiIsEmptyOneGreaterThanLengthSliceViolation {
	void m(String s) {
		if (1 > s.length()) // violation: Use '.isEmpty()' instead of '1 > .length()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: one_greater_than_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyOneGreaterThanSizeSliceViolation {
	void m(List<String> list) {
		if (1 > list.size()) // violation: Use '.isEmpty()' instead of '1 > .size()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: one_greater_than_strip_length ===
class InputSpecificApiStripIsBlankOneGreaterThanStripLengthSliceViolation {
	void oneGreaterThanStripLength(String s) {
		if (1 > s.strip().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '1 > .strip().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: one_greater_than_trim_length ===
class InputSpecificApiTrimIsBlankOneGreaterThanTrimLengthSliceViolation {
	void oneGreaterThanTrimLength(String s) {
		if (1 > s.trim().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '1 > .trim().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: one_less_equal_strip_length ===
class InputSpecificApiStripIsBlankOneLessEqualStripLengthSliceViolation {
	void oneLessEqualStripLength(String s) {
		if (1 <= s.strip().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '1 <= .strip().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: one_less_equal_trim_length ===
class InputSpecificApiTrimIsBlankOneLessEqualTrimLengthSliceViolation {
	void oneLessEqualTrimLength(String s) {
		if (1 <= s.trim().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '1 <= .trim().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: one_less_than_or_equal_length ===
class InputSpecificApiIsEmptyOneLessThanOrEqualLengthSliceViolation {
	void m(String s) {
		if (1 <= s.length()) // violation: Use '.!isEmpty()' instead of '1 <= .length()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: one_less_than_or_equal_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyOneLessThanOrEqualSizeSliceViolation {
	void m(List<String> list) {
		if (1 <= list.size()) // violation: Use '.!isEmpty()' instead of '1 <= .size()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: qualified_assert_equals ===
class InputSpecificApiAssertQualifiedAssertEqualsSliceViolation {
	void qualifiedAssertEquals() {
		org.junit.Assert.assertEquals(true, 1 == 1); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: qualified_assert_equals_literal_last ===
class InputSpecificApiAssertQualifiedAssertEqualsLiteralLastSliceViolation {
	void qualifiedAssertEqualsLiteralLast() {
		org.junit.Assert.assertEquals(1 == 1, true); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: qualified_assert_equals_literal_middle ===
class InputSpecificApiAssertQualifiedAssertEqualsLiteralMiddleSliceViolation {
	void qualifiedAssertEqualsLiteralMiddle() {
		org.junit.Assert.assertEquals("msg", null, new Object()); // violation: Use 'assertNull' instead of 'assertEquals' with a 'null' literal.
	}
}
// === end ===

// === case: qualified_assertions_assert_equals ===
// imports: org.junit.jupiter.api.Assertions
class InputSpecificApiAssertQualifiedAssertionsAssertEqualsSliceViolation {
	void qualifiedAssertionsAssertEquals() {
		Assertions.assertEquals(true, 1 == 1); // violation: Use 'assertTrue' instead of 'assertEquals' with a 'true' literal.
	}
}
// === end ===

// === case: remove_first ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstSliceViolation {
	void removeFirst(List<String> list) {
		list.remove(0); // violation [minSdk>=35]: Use '.removeFirst()' instead of '.remove(0)'.
	}
}
// === end ===

// === case: remove_first_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveFirstPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".remove(0)" + list.remove(0); // violation [minSdk>=35]: Use '.removeFirst()' instead of '.remove(0)'.
	}
}
// === end ===

// === case: remove_last ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveLastSliceViolation {
	void removeLast(List<String> list) {
		list.remove(list.size() - 1); // violation [minSdk>=35]: Use '.removeLast()' instead of '.remove(size() - 1)'.
	}
}
// === end ===

// === case: remove_last_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiRemoveRemoveLastPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = "list.remove(list.size() - 1)" + list.remove(list.size() - 1); // violation [minSdk>=35]: Use '.removeLast()' instead of '.remove(size() - 1)'.
	}
}
// === end ===

// === case: replace_all_literal ===
class InputSpecificApiStringMethodReplaceAllLiteralSliceViolation {
	void replaceAllLiteral(String s) {
		final var result = s.replaceAll("foo", "bar"); // violation: Use '.replace(...)' instead of '.replaceAll(...)'.
	}
}
// === end ===

// === case: replace_all_pattern_in_string_not_anchored ===
class InputSpecificApiStringMethodReplaceAllPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		final var r = ".replaceAll(" + s.replaceAll("foo", "bar"); // violation: Use '.replace(...)' instead of '.replaceAll(...)'.
	}
}
// === end ===

// === case: single_quote ===
class InputSpecificApiIndexOfCharSingleQuoteSliceViolation {
	void singleQuote(String s) {
		final var i = s.indexOf("'"); // violation: Use 'indexOf('\'')' instead of 'indexOf("'")'.
		System.out.println(i);
	}
}
// === end ===

// === case: singleton ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptySingletonSliceViolation {
	void singleton() {
		final var set = Collections.singleton("a"); // violation [minSdk>=30]: Use 'Set.of(...)' instead of 'Collections.singleton(...)'.
	}
}
// === end ===

// === case: singleton_list ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptySingletonListSliceViolation {
	void singletonList() {
		final var list = Collections.singletonList("a"); // violation [minSdk>=30]: Use 'List.of(...)' instead of 'Collections.singletonList(...)'.
	}
}
// === end ===

// === case: singleton_map ===
// imports: java.util.Collections
class InputSpecificApiCollectionsEmptySingletonMapSliceViolation {
	void singletonMap() {
		final var map = Collections.singletonMap("k", "v"); // violation [minSdk>=30]: Use 'Map.of(...)' instead of 'Collections.singletonMap(...)'.
	}
}
// === end ===

// === case: size_equals_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeEqualsZeroSliceViolation {
	void m(List<String> list) {
		if (list.size() == 0) // violation: Use '.isEmpty()' instead of '.size() == 0'.
			System.out.println("empty");
	}
}
// === end ===

// === case: size_greater_than_or_equal_one ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeGreaterThanOrEqualOneSliceViolation {
	void m(List<String> list) {
		if (list.size() >= 1) // violation: Use '.!isEmpty()' instead of '.size() >= 1'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: size_greater_than_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeGreaterThanZeroSliceViolation {
	void m(List<String> list) {
		if (list.size() > 0) // violation: Use '.!isEmpty()' instead of '.size() > 0'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: size_less_than_one ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeLessThanOneSliceViolation {
	void m(List<String> list) {
		if (list.size() < 1) // violation: Use '.isEmpty()' instead of '.size() < 1'.
			System.out.println("empty");
	}
}
// === end ===

// === case: size_less_than_or_equal_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeLessThanOrEqualZeroSliceViolation {
	void m(List<String> list) {
		if (list.size() <= 0) // violation: Use '.isEmpty()' instead of '.size() <= 0'.
			System.out.println("empty");
	}
}
// === end ===

// === case: size_not_equals_zero ===
// imports: java.util.List
class InputSpecificApiIsEmptySizeNotEqualsZeroSliceViolation {
	void m(List<String> list) {
		if (list.size() != 0) // violation: Use '.!isEmpty()' instead of '.size() != 0'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: sort_no_comparator ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortNoComparatorSliceViolation {
	void sortNoComparator(List<String> list) {
		Collections.sort(list); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
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
		Collections.sort(getList()); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_pattern_in_comment_not_anchored ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortPatternInCommentNotAnchoredSliceViolation {
	void m(List<String> list) {
		/* Collections.sort( */ Collections.sort(list); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_with_block_comment ===
// imports: java.util.Collections
// imports: java.util.Comparator
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithBlockCommentSliceViolation {
	void sortWithBlockComment(List<String> list, Comparator<String> cmp) {
		Collections.sort(list, cmp /* ) */); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_with_comparator ===
// imports: java.util.Collections
// imports: java.util.Comparator
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithComparatorSliceViolation {
	void sortWithComparator(List<String> list) {
		Collections.sort(list, Comparator.naturalOrder()); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_with_comparator_char_literal ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithComparatorCharLiteralSliceViolation {
	void sortWithComparatorCharLiteral(List<String> list) {
		Collections.sort(list, (a, b) -> Character.compare(a.charAt(0), '(')); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_with_lambda_comparator ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithLambdaComparatorSliceViolation {
	void sortWithLambdaComparator(List<String> list) {
		Collections.sort(list, (a, b) -> a.compareTo(b)); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: sort_with_string_literal_paren ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCollectionsSortSortWithStringLiteralParenSliceViolation {
	void sortWithStringLiteralParen(List<String> list) {
		Collections.sort(list, (a, b) -> a.replace("(", ")").compareTo(b)); // violation [minSdk>=24]: Use '.sort(...)' instead of 'Collections.sort(...)'.
	}
}
// === end ===

// === case: stream_count ===
// imports: java.util.List
class InputSpecificApiStreamStreamCountSliceViolation {
	void streamCount(List<String> list) {
		final var count = list.stream().count(); // violation: Use '.size()' instead of '.stream().count()'.
	}
}
// === end ===

// === case: stream_count_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamCountPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var c = ".stream().count()" + list.stream().count(); // violation: Use '.size()' instead of '.stream().count()'.
	}
}
// === end ===

// === case: stream_find_first_is_present ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentSliceViolation {
	void streamFindFirstIsPresent(List<String> list) {
		if (list.stream().findFirst().isPresent()) // violation: Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_already_negated ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentAlreadyNegatedSliceViolation {
	void streamFindFirstIsPresentAlreadyNegated(List<String> list) {
		if (!list.stream().findFirst().isPresent()) // violation: Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_dotted_receiver ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentDottedReceiverSliceViolation {
	List<String> list;

	void streamFindFirstIsPresentDottedReceiver(InputSpecificApiStreamStreamFindFirstIsPresentDottedReceiverSliceViolation obj) {
		if (obj.list.stream().findFirst().isPresent()) // violation: Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: stream_find_first_is_present_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamFindFirstIsPresentPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		if (".stream().findFirst().isPresent()".isEmpty() || list.stream().findFirst().isPresent()) // violation: Use '!.isEmpty()' instead of '.stream().findFirst().isPresent()'.
			System.out.println("x");
	}
}
// === end ===

// === case: stream_for_each ===
// imports: java.util.List
class InputSpecificApiStreamStreamForEachSliceViolation {
	void streamForEach(List<String> list) {
		list.stream().forEach(System.out::println); // violation [minSdk>=24]: Use '.forEach(...)' instead of '.stream().forEach(...)'.
	}
}
// === end ===

// === case: stream_for_each_pattern_in_comment_not_anchored ===
// imports: java.util.List
class InputSpecificApiStreamStreamForEachPatternInCommentNotAnchoredSliceViolation {
	void m(List<String> list) {
		/* .stream().forEach( */ list.stream().forEach(System.out::println); // violation [minSdk>=24]: Use '.forEach(...)' instead of '.stream().forEach(...)'.
	}
}
// === end ===

// === case: strip_is_empty ===
class InputSpecificApiStripIsBlankStripIsEmptySliceViolation {
	void stripIsEmpty(String s) {
		if (s.strip().isEmpty()) // violation [minSdk>=33]: Use '.isBlank()' instead of '.strip().isEmpty()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_is_empty_pattern_in_string_not_anchored ===
class InputSpecificApiStripIsBlankStripIsEmptyPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".strip().isEmpty()".isEmpty() || s.strip().isEmpty()) // violation [minSdk>=33]: Use '.isBlank()' instead of '.strip().isEmpty()'.
			System.out.println("x");
	}
}
// === end ===

// === case: strip_length_equals_zero ===
class InputSpecificApiStripIsBlankStripLengthEqualsZeroSliceViolation {
	void stripLengthEqualsZero(String s) {
		if (s.strip().length() == 0) // violation [minSdk>=33]: Use '.isBlank()' instead of '.strip().length() == 0'.
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_greater_equal_one ===
class InputSpecificApiStripIsBlankStripLengthGreaterEqualOneSliceViolation {
	void stripLengthGreaterEqualOne(String s) {
		if (s.strip().length() >= 1) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.strip().length() >= 1'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_greater_than_zero ===
class InputSpecificApiStripIsBlankStripLengthGreaterThanZeroSliceViolation {
	void stripLengthGreaterThanZero(String s) {
		if (s.strip().length() > 0) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.strip().length() > 0'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_in_compound_reversed_condition ===
class InputSpecificApiStripIsBlankStripLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && 0 != s.strip().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 != .strip().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: strip_length_less_equal_zero ===
class InputSpecificApiStripIsBlankStripLengthLessEqualZeroSliceViolation {
	void stripLengthLessEqualZero(String s) {
		if (s.strip().length() <= 0) // violation [minSdk>=33]: Use '.isBlank()' instead of '.strip().length() <= 0'.
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_less_than_one ===
class InputSpecificApiStripIsBlankStripLengthLessThanOneSliceViolation {
	void stripLengthLessThanOne(String s) {
		if (s.strip().length() < 1) // violation [minSdk>=33]: Use '.isBlank()' instead of '.strip().length() < 1'.
			System.out.println("blank");
	}
}
// === end ===

// === case: strip_length_not_equals_zero ===
class InputSpecificApiStripIsBlankStripLengthNotEqualsZeroSliceViolation {
	void stripLengthNotEqualsZero(String s) {
		if (s.strip().length() != 0) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.strip().length() != 0'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: to_array_integer ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayIntegerSliceViolation {
	void toArrayInteger(List<Integer> list) {
		final var arr = list.toArray(new Integer[0]); // violation [minSdk>=33]: Use 'Integer[]::new' instead of 'new Integer[0]'.
	}
}
// === end ===

// === case: to_array_method_receiver ===
class InputSpecificApiToArrayToArrayMethodReceiverSliceViolation {
	void toArrayMethodReceiver() {
		final var arr = getList().toArray(new String[0]); // violation [minSdk>=33]: Use 'String[]::new' instead of 'new String[0]'.
	}
}
// === end ===

// === case: to_array_pattern_in_string_not_anchored ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayPatternInStringNotAnchoredSliceViolation {
	void m(List<String> list) {
		final var s = ".toArray(new String[0])" + list.toArray(new String[0]); // violation [minSdk>=33]: Use 'String[]::new' instead of 'new String[0]'.
	}
}
// === end ===

// === case: to_array_qualified ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayQualifiedSliceViolation {
	void toArrayQualified(List<String> list) {
		final var arr = list.toArray(new java.lang.String[0]); // violation [minSdk>=33]: Use 'java.lang.String[]::new' instead of 'new java.lang.String[0]'.
	}
}
// === end ===

// === case: to_array_string ===
// imports: java.util.List
class InputSpecificApiToArrayToArrayStringSliceViolation {
	void toArrayString(List<String> list) {
		final var arr = list.toArray(new String[0]); // violation [minSdk>=33]: Use 'String[]::new' instead of 'new String[0]'.
	}
}
// === end ===

// === case: trim_is_empty ===
class InputSpecificApiTrimIsBlankTrimIsEmptySliceViolation {
	void trimIsEmpty(String s) {
		if (s.trim().isEmpty()) // violation [minSdk>=33]: Use '.isBlank()' instead of '.trim().isEmpty()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_is_empty_pattern_in_string_not_anchored ===
class InputSpecificApiTrimIsBlankTrimIsEmptyPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if (".trim().isEmpty()".isEmpty() || s.trim().isEmpty()) // violation [minSdk>=33]: Use '.isBlank()' instead of '.trim().isEmpty()'.
			System.out.println("x");
	}
}
// === end ===

// === case: trim_length_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthEqualsZeroSliceViolation {
	void trimLengthEqualsZero(String s) {
		if (s.trim().length() == 0) // violation [minSdk>=33]: Use '.isBlank()' instead of '.trim().length() == 0'.
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_greater_equal_one ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterEqualOneSliceViolation {
	void trimLengthGreaterEqualOne(String s) {
		if (s.trim().length() >= 1) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.trim().length() >= 1'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_greater_than_zero ===
class InputSpecificApiTrimIsBlankTrimLengthGreaterThanZeroSliceViolation {
	void trimLengthGreaterThanZero(String s) {
		if (s.trim().length() > 0) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.trim().length() > 0'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_in_compound_reversed_condition ===
class InputSpecificApiTrimIsBlankTrimLengthInCompoundReversedConditionSliceViolation {
	void m(String s, int x) {
		if (0 != x && 0 != s.trim().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 != .trim().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: trim_length_less_equal_zero ===
class InputSpecificApiTrimIsBlankTrimLengthLessEqualZeroSliceViolation {
	void trimLengthLessEqualZero(String s) {
		if (s.trim().length() <= 0) // violation [minSdk>=33]: Use '.isBlank()' instead of '.trim().length() <= 0'.
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_less_than_one ===
class InputSpecificApiTrimIsBlankTrimLengthLessThanOneSliceViolation {
	void trimLengthLessThanOne(String s) {
		if (s.trim().length() < 1) // violation [minSdk>=33]: Use '.isBlank()' instead of '.trim().length() < 1'.
			System.out.println("blank");
	}
}
// === end ===

// === case: trim_length_not_equals_zero ===
class InputSpecificApiTrimIsBlankTrimLengthNotEqualsZeroSliceViolation {
	void trimLengthNotEqualsZero(String s) {
		if (s.trim().length() != 0) // violation [minSdk>=33]: Use '!.isBlank()' instead of '.trim().length() != 0'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: unicode_escape ===
class InputSpecificApiIndexOfCharUnicodeEscapeSliceViolation {
	void unicodeEscape(String s) {
		final var i = s.indexOf("é"); // violation: Use 'indexOf('é')' instead of 'indexOf("é")'.
		System.out.println(i);
	}
}
// === end ===

// === case: unmodifiable_as_list ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListSliceViolation {
	void unmodifiableAsList() {
		final var list = Collections.unmodifiableList(Arrays.asList("a", "b")); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_as_list_block_comment ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListBlockCommentSliceViolation {
	void unmodifiableAsListBlockComment() {
		final var list = Collections.unmodifiableList(Arrays.asList("a" /* ) */, "b")); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_as_list_nested_call ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListNestedCallSliceViolation {
	void unmodifiableAsListNestedCall() {
		final var list = Collections.unmodifiableList(Arrays.asList(String.valueOf(1), String.valueOf(2))); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_as_list_paren_in_char ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListParenInCharSliceViolation {
	void unmodifiableAsListParenInChar() {
		final var list = Collections.unmodifiableList(Arrays.asList(')', '(')); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_as_list_paren_in_string ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListParenInStringSliceViolation {
	void unmodifiableAsListParenInString() {
		final var list = Collections.unmodifiableList(Arrays.asList("(", ")")); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_as_list_plain_chars ===
// imports: java.util.Arrays
// imports: java.util.Collections
class InputSpecificApiCopyOfUnmodifiableAsListPlainCharsSliceViolation {
	void unmodifiableAsListPlainChars() {
		final var list = Collections.unmodifiableList(Arrays.asList('a', 'b')); // violation [minSdk>=31]: Use 'List.of(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_list ===
// imports: java.util.Collections
// imports: java.util.List
class InputSpecificApiCopyOfUnmodifiableListSliceViolation {
	void unmodifiableList(List<String> list) {
		final var result = Collections.unmodifiableList(list); // violation [minSdk>=31]: Use 'List.copyOf(...)' instead of 'Collections.unmodifiableList(...)'.
	}
}
// === end ===

// === case: unmodifiable_map ===
// imports: java.util.Collections
// imports: java.util.Map
class InputSpecificApiCopyOfUnmodifiableMapSliceViolation {
	void unmodifiableMap(Map<String, String> map) {
		final var result = Collections.unmodifiableMap(map); // violation [minSdk>=31]: Use 'Map.copyOf(...)' instead of 'Collections.unmodifiableMap(...)'.
	}
}
// === end ===

// === case: unmodifiable_set ===
// imports: java.util.Collections
// imports: java.util.Set
class InputSpecificApiCopyOfUnmodifiableSetSliceViolation {
	void unmodifiableSet(Set<String> set) {
		final var result = Collections.unmodifiableSet(set); // violation [minSdk>=31]: Use 'Set.copyOf(...)' instead of 'Collections.unmodifiableSet(...)'.
	}
}
// === end ===

// === case: values_contains ===
// imports: java.util.Map
class InputSpecificApiMapChainValuesContainsSliceViolation {
	void valuesContains(Map<String, String> map) {
		if (map.values().contains("value")) // violation: Use '.containsValue(...)' instead of '.values().contains(...)'.
			System.out.println("found");
	}
}
// === end ===

// === case: values_contains_pattern_in_string_not_anchored ===
// imports: java.util.Map
class InputSpecificApiMapChainValuesContainsPatternInStringNotAnchoredSliceViolation {
	void m(Map<String, String> map) {
		if (".values().contains(".isEmpty() || map.values().contains("value")) // violation: Use '.containsValue(...)' instead of '.values().contains(...)'.
			System.out.println("x");
	}
}
// === end ===

// === case: var_local_get_size_minus_one ===
// imports: java.util.Map
class InputSpecificApiReflectionVarLocalGetSizeMinusOneSliceViolation {
	void varLocalGetSizeMinusOne() {
		final var map = Map.of(0, "a", 1, "b");
		System.out.println(map.get(map.size() - 1)); // violation [minSdk>=35]: Use '.getLast()' instead of '.get(size() - 1)'.
	}
}
// === end ===

// === case: zero_equals_length ===
class InputSpecificApiIsEmptyZeroEqualsLengthSliceViolation {
	void m(String s) {
		if (0 == s.length()) // violation: Use '.isEmpty()' instead of '0 == .length()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_equals_length_pattern_in_string_not_anchored ===
class InputSpecificApiIsEmptyZeroEqualsLengthPatternInStringNotAnchoredSliceViolation {
	void m(String s) {
		if ("0 == s.length()".isEmpty() || 0 == s.length()) // violation: Use '.isEmpty()' instead of '0 == .length()'.
			System.out.println("x");
	}
}
// === end ===

// === case: zero_equals_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroEqualsSizeSliceViolation {
	void m(List<String> list) {
		if (0 == list.size()) // violation: Use '.isEmpty()' instead of '0 == .size()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_equals_strip_length ===
class InputSpecificApiStripIsBlankZeroEqualsStripLengthSliceViolation {
	void zeroEqualsStripLength(String s) {
		if (0 == s.strip().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '0 == .strip().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_equals_trim_length ===
class InputSpecificApiTrimIsBlankZeroEqualsTrimLengthSliceViolation {
	void zeroEqualsTrimLength(String s) {
		if (0 == s.trim().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '0 == .trim().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_equal_strip_length ===
class InputSpecificApiStripIsBlankZeroGreaterEqualStripLengthSliceViolation {
	void zeroGreaterEqualStripLength(String s) {
		if (0 >= s.strip().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '0 >= .strip().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_equal_trim_length ===
class InputSpecificApiTrimIsBlankZeroGreaterEqualTrimLengthSliceViolation {
	void zeroGreaterEqualTrimLength(String s) {
		if (0 >= s.trim().length()) // violation [minSdk>=33]: Use '.isBlank()' instead of '0 >= .trim().length()'.
			System.out.println("blank");
	}
}
// === end ===

// === case: zero_greater_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfZeroGreaterIndexOfSliceViolation {
	void zeroGreaterIndexOf(String s) {
		if (0 > s.indexOf("dd")) // violation: Use '!.contains(...)' instead of '0 > .indexOf(...)'.
			System.out.println("not found");
	}
}
// === end ===

// === case: zero_greater_than_or_equal_length ===
class InputSpecificApiIsEmptyZeroGreaterThanOrEqualLengthSliceViolation {
	void m(String s) {
		if (0 >= s.length()) // violation: Use '.isEmpty()' instead of '0 >= .length()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_greater_than_or_equal_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroGreaterThanOrEqualSizeSliceViolation {
	void m(List<String> list) {
		if (0 >= list.size()) // violation: Use '.isEmpty()' instead of '0 >= .size()'.
			System.out.println("empty");
	}
}
// === end ===

// === case: zero_less_equal_index_of ===
// skip-reason: unrecognized API pattern
class InputSpecificApiIndexOfZeroLessEqualIndexOfSliceViolation {
	void zeroLessEqualIndexOf(String s) {
		if (0 <= s.indexOf("cc")) // violation: Use '.contains(...)' instead of '0 <= .indexOf(...)'.
			System.out.println("found");
	}
}
// === end ===

// === case: zero_less_than_length ===
class InputSpecificApiIsEmptyZeroLessThanLengthSliceViolation {
	void m(String s) {
		if (0 < s.length()) // violation: Use '.!isEmpty()' instead of '0 < .length()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_less_than_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroLessThanSizeSliceViolation {
	void m(List<String> list) {
		if (0 < list.size()) // violation: Use '.!isEmpty()' instead of '0 < .size()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_less_than_strip_length ===
class InputSpecificApiStripIsBlankZeroLessThanStripLengthSliceViolation {
	void zeroLessThanStripLength(String s) {
		if (0 < s.strip().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 < .strip().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_less_than_trim_length ===
class InputSpecificApiTrimIsBlankZeroLessThanTrimLengthSliceViolation {
	void zeroLessThanTrimLength(String s) {
		if (0 < s.trim().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 < .trim().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_not_equals_length ===
class InputSpecificApiIsEmptyZeroNotEqualsLengthSliceViolation {
	void m(String s) {
		if (0 != s.length()) // violation: Use '.!isEmpty()' instead of '0 != .length()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_not_equals_size ===
// imports: java.util.List
class InputSpecificApiIsEmptyZeroNotEqualsSizeSliceViolation {
	void m(List<String> list) {
		if (0 != list.size()) // violation: Use '.!isEmpty()' instead of '0 != .size()'.
			System.out.println("not empty");
	}
}
// === end ===

// === case: zero_not_equals_strip_length ===
class InputSpecificApiStripIsBlankZeroNotEqualsStripLengthSliceViolation {
	void zeroNotEqualsStripLength(String s) {
		if (0 != s.strip().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 != .strip().length()'.
			System.out.println("not blank");
	}
}
// === end ===

// === case: zero_not_equals_trim_length ===
class InputSpecificApiTrimIsBlankZeroNotEqualsTrimLengthSliceViolation {
	void zeroNotEqualsTrimLength(String s) {
		if (0 != s.trim().length()) // violation [minSdk>=33]: Use '!.isBlank()' instead of '0 != .trim().length()'.
			System.out.println("not blank");
	}
}
// === end ===