package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

import java.util.Set;

public class PreferSpecificApiFixerTest {
	private static final String TOPIC = "preferspecificapi";

	private final CheckstyleFixer fixer = new PreferSpecificApiFixer();

	// can't migrate: probes addAssertImport against a degenerate (whitespace-collapsed) existing static import, which the `// imports:` slice directive can't express (it materializes a canonical import line)
	@Test
	public void testAssertImportWhitespaceTolerant() throws Exception {
		assertSimpleFix(fixer, TOPIC, "assert_import_whitespace_tolerant", Set.of("static org.junit.Assert.assertFalse"));
	}

	@Test
	public void testIndexOfCharRefusesInvalidEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_escape", "unrecognized API pattern");
	}

	@Test
	public void testIndexOfCharRefusesInvalidUnicodeEscape() throws Exception {
		assertSkipResult(fixer, TOPIC, "index_of_char_refuses_invalid_unicode_escape", "unrecognized API pattern");
	}

	// can't migrate: snippet `if (!str.length() > 0)` is not parseable as a class member (boolean negation of int is invalid Java)
	@Test
	public void testLengthIsEmptyAlreadyNegated() throws Exception {
		assertSimpleFix(fixer, TOPIC, "length_is_empty_already_negated");
	}

	@Test
	public void testLengthIsEmptyComplexReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_complex_receiver_returns_skip_result", "unrecognized API pattern");
	}

	// can't migrate: check's receiverHasMethodStrict requires AstUtil type resolution of obj.name field; nested field access type chain isn't resolved by check, so wrapped slice produces zero violations
	@Test
	public void testLengthIsEmptyDottedReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "length_is_empty_dotted_receiver");
	}

	@Test
	public void testLengthIsEmptyEqualsZeroFollowedByLetter() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_equals_zero_followed_by_letter", "unrecognized API pattern");
	}

	@Test
	public void testLengthIsEmptyLessThanFollowedByDigit() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_less_than_followed_by_digit", "unrecognized API pattern");
	}

	@Test
	public void testLengthIsEmptyLessThanOneFollowedByDecimal() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_less_than_one_followed_by_decimal", "unrecognized API pattern");
	}

	@Test
	public void testLengthIsEmptyLessThanOneFollowedByUnderscore() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_less_than_one_followed_by_underscore", "unrecognized API pattern");
	}

	// can't migrate: snippet has two patterns on one line, only second is fixable; single-violation assertCaseFix can't represent the rejected first pattern coexisting with the accepted second
	@Test
	public void testLengthIsEmptyMultipleOccurrencesFirstRejected() throws Exception {
		assertSimpleFix(fixer, TOPIC, "length_is_empty_multiple_occurrences_first_rejected");
	}

	@Test
	public void testLengthIsEmptyReversedAfterDigitsRejected() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_reversed_after_digits_rejected", "unrecognized API pattern");
	}

	@Test
	public void testLengthIsEmptyReversedMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "length_is_empty_reversed_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	@Test
	public void testStreamFindFirstIsPresentMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "stream_find_first_is_present_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	// can't migrate: dotted-receiver pattern; check's type resolution doesn't fire on nested field access
	@Test
	public void testStripIsBlankDottedReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_is_blank_dotted_receiver");
	}

	// can't migrate: method-receiver pattern; check's type resolution doesn't fire on method-call receiver
	@Test
	public void testStripIsBlankMethodReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_is_blank_method_receiver");
	}

	// can't migrate: snippet `if (!s.strip().length() > 0)` is not parseable Java (boolean negation of int)
	@Test
	public void testStripLengthAlreadyNegated() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_length_already_negated");
	}

	@Test
	public void testStripLengthEqualsZeroFollowedByLetter() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_equals_zero_followed_by_letter", "unrecognized API pattern");
	}

	// can't migrate: dotted-receiver pattern; check's type resolution doesn't fire on nested field access
	@Test
	public void testStripLengthGreaterThanZeroDottedReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_length_greater_than_zero_dotted_receiver");
	}

	@Test
	public void testStripLengthGreaterThanZeroMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_greater_than_zero_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	@Test
	public void testStripLengthLessThanFollowedByDigit() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_less_than_followed_by_digit", "unrecognized API pattern");
	}

	@Test
	public void testStripLengthLessThanOneFollowedByDecimal() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_less_than_one_followed_by_decimal", "unrecognized API pattern");
	}

	@Test
	public void testStripLengthLessThanOneFollowedByUnderscore() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_less_than_one_followed_by_underscore", "unrecognized API pattern");
	}

	// can't migrate: snippet has two patterns on one line, only second is fixable; single-violation assertCaseFix can't represent
	@Test
	public void testStripLengthMultipleOccurrencesFirstRejected() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_length_multiple_occurrences_first_rejected");
	}

	@Test
	public void testStripLengthReversedAfterDigitsRejected() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_reversed_after_digits_rejected", "unrecognized API pattern");
	}

	// can't migrate: snippet has two patterns on one line, only second is fixable; single-violation assertCaseFix can't represent
	@Test
	public void testStripLengthReversedFirstRejectedSecondAccepted() throws Exception {
		assertSimpleFix(fixer, TOPIC, "strip_length_reversed_first_rejected_second_accepted");
	}

	@Test
	public void testStripLengthReversedMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "strip_length_reversed_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	// can't migrate: dotted-receiver pattern; check's type resolution doesn't fire on nested field access
	@Test
	public void testTrimIsBlankDottedReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "trim_is_blank_dotted_receiver");
	}

	// can't migrate: snippet `if (!s.trim().length() > 0)` is not parseable Java (boolean negation of int)
	@Test
	public void testTrimLengthAlreadyNegated() throws Exception {
		assertSimpleFix(fixer, TOPIC, "trim_length_already_negated");
	}

	@Test
	public void testTrimLengthEqualsZeroFollowedByLetter() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_equals_zero_followed_by_letter", "unrecognized API pattern");
	}

	// can't migrate: dotted-receiver pattern; check's type resolution doesn't fire on nested field access
	@Test
	public void testTrimLengthGreaterThanZeroDottedReceiver() throws Exception {
		assertSimpleFix(fixer, TOPIC, "trim_length_greater_than_zero_dotted_receiver");
	}

	@Test
	public void testTrimLengthGreaterThanZeroMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_greater_than_zero_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	@Test
	public void testTrimLengthLessThanFollowedByDigit() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_less_than_followed_by_digit", "unrecognized API pattern");
	}

	@Test
	public void testTrimLengthLessThanOneFollowedByDecimal() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_less_than_one_followed_by_decimal", "unrecognized API pattern");
	}

	@Test
	public void testTrimLengthLessThanOneFollowedByUnderscore() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_less_than_one_followed_by_underscore", "unrecognized API pattern");
	}

	// can't migrate: snippet has two patterns on one line, only second is fixable; single-violation assertCaseFix can't represent
	@Test
	public void testTrimLengthMultipleOccurrencesFirstRejected() throws Exception {
		assertSimpleFix(fixer, TOPIC, "trim_length_multiple_occurrences_first_rejected");
	}

	@Test
	public void testTrimLengthReversedAfterDigitsRejected() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_reversed_after_digits_rejected", "unrecognized API pattern");
	}

	// can't migrate: snippet has two patterns on one line, only second is fixable; single-violation assertCaseFix can't represent
	@Test
	public void testTrimLengthReversedFirstRejectedSecondAccepted() throws Exception {
		assertSimpleFix(fixer, TOPIC, "trim_length_reversed_first_rejected_second_accepted");
	}

	@Test
	public void testTrimLengthReversedMethodReceiverReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "trim_length_reversed_method_receiver_returns_skip_result", "unrecognized API pattern");
	}

	// fragment: unbalanced/multiline collapse input, not compilable as a class member
	@Test
	public void testUnmodifiableAsListUnbalanced() throws Exception {
		assertSimpleFix(fixer, TOPIC, "unmodifiable_as_list_unbalanced", Set.of("java.util.List"));
	}
}