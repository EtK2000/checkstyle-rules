package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

import java.util.Set;

public class PreferExactAssertionFixerTest {
	private static final String TOPIC = "preferexactassertion";

	private final CheckstyleFixer fixer = new PreferExactAssertionFixer();

	@Test
	public void chainedReceiverNegatedSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "chained_receiver_negated_skipped");
	}

	@Test
	public void explicitTypeArgInArgsSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "explicit_type_arg_in_args_skipped");
	}

	@Test
	public void helperDotOnPreviousLineSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_dot_on_previous_line_skipped");
	}

	@Test
	public void helperQualifiedBlockCommentBeforeDotSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_block_comment_before_dot_skipped");
	}

	@Test
	public void helperQualifiedCharLiteralWalkbackSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_char_literal_walkback_skipped");
	}

	@Test
	public void helperQualifiedCommentLineIntermediarySkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_comment_line_intermediary_skipped");
	}

	@Test
	public void helperQualifiedComparisonNoFixableShape() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_comparison_skipped");
	}

	@Test
	public void helperQualifiedLineCommentBeforeDotSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_line_comment_before_dot_skipped");
	}

	@Test
	public void helperQualifiedNegatedInstanceOfSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_negated_instance_of_skipped");
	}

	@Test
	public void helperQualifiedNegatedInstanceOfSkippedAssertFalse() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_negated_instance_of_skipped_assert_false");
	}

	@Test
	public void helperQualifiedPlainInstanceOfSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_plain_instance_of_skipped");
	}

	@Test
	public void helperQualifiedPlainNegationSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_plain_negation_skipped");
	}

	@Test
	public void helperQualifiedStringLiteralWalkbackSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_string_literal_walkback_skipped");
	}

	@Test
	public void helperQualifiedTrailingBlockCommentSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_trailing_block_comment_skipped");
	}

	@Test
	public void helperQualifiedTrailingLineCommentSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "helper_qualified_trailing_line_comment_skipped");
	}

	@Test
	public void importWithTrailingLineCommentRecognized() throws Exception {
		// Cannot migrate: the snippet tests an import line with a trailing `//`
		// comment (`import ...; // bootstrap`); the `// imports:` slice directive
		// can't express that, and a literal `import` mid-file is invalid Java.
		assertSimpleFix(
				fixer,
				TOPIC,
				"import_with_trailing_line_comment_recognized",
				Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
	}

	@Test
	public void junit4And5MixedImportsSkipUnqualified() throws Exception {
		assertSkipResult(fixer, TOPIC, "junit4_and_5_mixed_imports_skip_unqualified");
	}

	@Test
	public void junit4ImportOnlySkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "junit4_import_only_skipped");
	}

	@Test
	public void junit4WildcardImportOnlySkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "junit4_wildcard_import_only_skipped");
	}

	@Test
	public void methodAtFileStartWithLeadingDotSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "method_at_file_start_with_leading_dot_skipped");
	}

	@Test
	public void multiLineCallNoCloseParenReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_call_no_close_paren_skipped");
	}

	@Test
	public void multiLineCallNoSemicolonReturnsSkipResult() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_call_no_semicolon_skipped");
	}

	@Test
	public void multiLineCallOpenParenOnOwnLineFixed() throws Exception {
		// Cannot migrate: check reports at the inner instanceof line, but the fixer
		// needs to be invoked at the assertTrue token line; assertCaseFix would
		// dispatch at the wrong column.
		assertSimpleFix(
				fixer,
				TOPIC,
				"multi_line_call_open_paren_on_own_line_fixed",
				Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
	}

	@Test
	public void multiLineCallStringLiteralContainingNameIgnored() throws Exception {
		// Cannot migrate: check reports at the inner instanceof line, but the fixer
		// needs to be invoked at the assertTrue token line; assertCaseFix would
		// dispatch at the wrong column.
		assertSimpleFix(
				fixer,
				TOPIC,
				"multi_line_call_string_literal_containing_name_ignored",
				Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
	}

	@Test
	public void multiLineCallWithLineCommentBeforeOpenParenFixed() throws Exception {
		// Cannot migrate: check reports at the inner instanceof line, but the fixer
		// needs to be invoked at the assertTrue token line; assertCaseFix would
		// dispatch at the wrong column.
		assertSimpleFix(
				fixer,
				TOPIC,
				"multi_line_call_with_line_comment_before_open_paren_fixed",
				Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
	}

	@Test
	public void multiLineNegationInnerFixedWithOuterParensPreserved() throws Exception {
		// Cannot migrate: check reports 'Use assertNotInstanceOf' (canceling the outer
		// parens around the negated instanceof) but the fixer keeps the outer parens
		// and emits assertFalse((...)), so assertCaseFix sees an import-set mismatch.
		assertSimpleFix(
				fixer,
				TOPIC,
				"multi_line_negation_inner_fixed_with_outer_parens_preserved",
				Set.of("static org.junit.jupiter.api.Assertions.assertFalse")
		);
	}

	@Test
	public void multiLineOuterParenArgSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_outer_paren_arg_skipped");
	}

	@Test
	public void negationBothFrameworksSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "negation_both_frameworks_skipped");
	}

	@Test
	public void negationEmptyInnerSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "negation_empty_inner_skipped");
	}

	@Test
	public void negationNoNegationNoInstanceofSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "negation_no_negation_no_instanceof_skipped");
	}

	@Test
	public void negationNotEqualMalformedSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "negation_not_equal_malformed_skipped");
	}

	@Test
	public void parenReceiverNegatedSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "paren_receiver_negated_skipped");
	}

	@Test
	public void qualifiedHeuristicNotSpoofedByStringLiteral() throws Exception {
		assertSkipResult(fixer, TOPIC, "qualified_heuristic_not_spoofed_by_string_literal");
	}

	@Test
	public void structuralLineCommentInArgsSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "structural_line_comment_in_args_skipped");
	}

	@Test
	public void unicodeEscapeInStringArgSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "unicode_escape_in_string_arg_skipped");
	}

	@Test
	public void unicodeEscapeOutsideLiteralSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "unicode_escape_outside_literal_skipped");
	}

	@Test
	public void walkbackLineInBlockCommentMiddleSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "walkback_line_in_block_comment_middle_skipped");
	}

	@Test
	public void walkbackLineWithUnicodeEscapeSkipped() throws Exception {
		assertSkipResult(fixer, TOPIC, "walkback_line_with_unicode_escape_skipped");
	}

	@Test
	public void whitespaceTolerantImportRecognized() throws Exception {
		// Cannot migrate: the snippet tests an `import` line with non-standard
		// whitespace (extra spaces, leading tab, space before `;`); the `// imports:`
		// slice directive translates to a canonical `import <fqcn>;` and a literal
		// `import` line mid-file is invalid Java when the slice sits in cases.in.java.
		assertSimpleFix(
				fixer,
				TOPIC,
				"whitespace_tolerant_import_recognized",
				Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
	}
}