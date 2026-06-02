// === case: assert_import_whitespace_tolerant ===
// target: line=2 col=2
import static org . junit . Assert . assertEquals ;

		assertEquals(false, result);
// === end ===

// === case: index_of_char_refuses_invalid_escape ===
// target: col=16
		final var i = s.indexOf("\z");
// === end ===

// === case: index_of_char_refuses_invalid_unicode_escape ===
// target: col=16
		final var i = s.indexOf("\uABCG");
// === end ===

// === case: length_is_empty_already_negated ===
// target: col=0
		if (!str.length() > 0)
// === end ===

// === case: length_is_empty_complex_receiver_returns_skip_result ===
// target: col=0
		if (getStr().length() > 0)
// === end ===

// === case: length_is_empty_dotted_receiver ===
// target: col=0
		if (obj.name.length() > 0)
// === end ===

// === case: length_is_empty_equals_zero_followed_by_letter ===
// target: col=0
		if (s.length() == 0xF)
// === end ===

// === case: length_is_empty_less_than_followed_by_digit ===
// target: col=0
		if (s.length() < 10)
// === end ===

// === case: length_is_empty_less_than_one_followed_by_decimal ===
// target: col=0
		if (s.length() < 1.5)
// === end ===

// === case: length_is_empty_less_than_one_followed_by_underscore ===
// target: col=0
		if (s.length() < 1_0)
// === end ===

// === case: length_is_empty_multiple_occurrences_first_rejected ===
// target: col=0
		if (a.length() == 0xF || b.length() == 0)
// === end ===

// === case: length_is_empty_reversed_after_digits_rejected ===
// target: col=0
		if (300 == s.length())
// === end ===

// === case: length_is_empty_reversed_method_receiver_returns_skip_result ===
// target: col=0
		if (0 == foo().length())
// === end ===

// === case: stream_find_first_is_present_method_receiver_returns_skip_result ===
// target: col=0
		if (getList().stream().findFirst().isPresent())
// === end ===

// === case: strip_is_blank_dotted_receiver ===
// target: col=0
		if (obj.name.strip().isEmpty())
// === end ===

// === case: strip_is_blank_method_receiver ===
// target: col=0
		if (getText().strip().isEmpty())
// === end ===

// === case: strip_length_already_negated ===
// target: col=0
		if (!s.strip().length() > 0)
// === end ===

// === case: strip_length_equals_zero_followed_by_letter ===
// target: col=0
		if (s.strip().length() == 0xF)
// === end ===

// === case: strip_length_greater_than_zero_dotted_receiver ===
// target: col=0
		if (obj.name.strip().length() > 0)
// === end ===

// === case: strip_length_greater_than_zero_method_receiver_returns_skip_result ===
// target: col=0
		if (getText().strip().length() > 0)
// === end ===

// === case: strip_length_less_than_followed_by_digit ===
// target: col=0
		if (s.strip().length() < 10)
// === end ===

// === case: strip_length_less_than_one_followed_by_decimal ===
// target: col=0
		if (s.strip().length() < 1.5)
// === end ===

// === case: strip_length_less_than_one_followed_by_underscore ===
// target: col=0
		if (s.strip().length() < 1_0)
// === end ===

// === case: strip_length_multiple_occurrences_first_rejected ===
// target: col=0
		if (a.strip().length() == 0xF || b.strip().length() == 0)
// === end ===

// === case: strip_length_reversed_after_digits_rejected ===
// target: col=0
		if (300 == s.strip().length())
// === end ===

// === case: strip_length_reversed_first_rejected_second_accepted ===
// target: col=0
		if (idx10 == 0 && 0 == s.strip().length())
// === end ===

// === case: strip_length_reversed_method_receiver_returns_skip_result ===
// target: col=0
		if (0 == foo().strip().length())
// === end ===

// === case: trim_is_blank_dotted_receiver ===
// target: col=0
		if (obj.name.trim().isEmpty())
// === end ===

// === case: trim_length_already_negated ===
// target: col=0
		if (!s.trim().length() > 0)
// === end ===

// === case: trim_length_equals_zero_followed_by_letter ===
// target: col=0
		if (s.trim().length() == 0xF)
// === end ===

// === case: trim_length_greater_than_zero_dotted_receiver ===
// target: col=0
		if (obj.name.trim().length() > 0)
// === end ===

// === case: trim_length_greater_than_zero_method_receiver_returns_skip_result ===
// target: col=0
		if (getText().trim().length() > 0)
// === end ===

// === case: trim_length_less_than_followed_by_digit ===
// target: col=0
		if (s.trim().length() < 10)
// === end ===

// === case: trim_length_less_than_one_followed_by_decimal ===
// target: col=0
		if (s.trim().length() < 1.5)
// === end ===

// === case: trim_length_less_than_one_followed_by_underscore ===
// target: col=0
		if (s.trim().length() < 1_0)
// === end ===

// === case: trim_length_multiple_occurrences_first_rejected ===
// target: col=0
		if (a.trim().length() == 0xF || b.trim().length() == 0)
// === end ===

// === case: trim_length_reversed_after_digits_rejected ===
// target: col=0
		if (300 == s.trim().length())
// === end ===

// === case: trim_length_reversed_first_rejected_second_accepted ===
// target: col=0
		if (idx10 == 0 && 0 == s.trim().length())
// === end ===

// === case: trim_length_reversed_method_receiver_returns_skip_result ===
// target: col=0
		if (0 == foo().trim().length())
// === end ===

// === case: unmodifiable_as_list_unbalanced ===
// target: col=0
		List<String> result = Collections.unmodifiableList(Arrays.asList(list);
// === end ===