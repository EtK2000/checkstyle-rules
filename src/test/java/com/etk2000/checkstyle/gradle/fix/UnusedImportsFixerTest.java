package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;

import org.junit.jupiter.api.Test;

public class UnusedImportsFixerTest {
	private static final String FIX_CONTEXT_TOPIC = "unusedimportsfixcontext";
	private static final String TOPIC = "unusedimports";

	private final UnusedImportsFixer fixer = new UnusedImportsFixer();

	@Test
	public void testDeletesEmptyLineFromCascade() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_empty_line_from_cascade");
	}

	@Test
	public void testDeletesIdentifierContainingSimpleNameAsPrefix() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_identifier_containing_simple_name_as_prefix");
	}

	@Test
	public void testDeletesIdentifierContainingSimpleNameAsSuffix() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_identifier_containing_simple_name_as_suffix");
	}

	@Test
	public void testDeletesImportWithBlockCommentInFqn() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_import_with_block_comment_in_fqn");
	}

	@Test
	public void testDeletesImportWithTrailingLineComment() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_import_with_trailing_line_comment");
	}

	@Test
	public void testDeletesSiblingImportLineContainingSimpleName() throws Exception {
		assertSimpleFix(fixer, TOPIC, "deletes_sibling_import_line_containing_simple_name");
	}

	@Test
	public void testDeletesStaticWildcardImportWithoutReverify() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_static_wildcard_import_without_reverify");
	}

	@Test
	public void testDeletesWildcardImportWithComment() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_wildcard_import_with_comment");
	}

	@Test
	public void testDeletesWildcardImportWithoutReverify() throws Exception {
		assertSimpleFix(fixer, FIX_CONTEXT_TOPIC, "deletes_wildcard_import_without_reverify");
	}

	@Test
	public void testSkipsBlankWhitespaceLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "skips_blank_whitespace_line", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsCommentOnlyLine() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_comment_only_line", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsImportNoDot() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_import_no_dot", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsImportTrailingDot() throws Exception {
		assertSkipResult(fixer, TOPIC, "skips_import_trailing_dot", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsImportWithCommentNowUsed() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_import_with_comment_now_used", SkipMessages.UNUSED_IMPORTS_NOW_USED);
	}

	@Test
	public void testSkipsImportWithUnterminatedBlockComment() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_import_with_unterminated_block_comment", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsMalformedImportLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "skips_malformed_import_line", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsMultiStatementImportLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "skips_multi_statement_import_line", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsStaticImportNoDot() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_static_import_no_dot", SkipMessages.UNUSED_IMPORTS_MALFORMED);
	}

	@Test
	public void testSkipsTabAroundDotInFqn() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_tab_around_dot_in_fqn", SkipMessages.UNUSED_IMPORTS_NOW_USED);
	}

	@Test
	public void testSkipsWhenJavaLangSubpackageSimpleNameNowUsed() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_when_java_lang_subpackage_simple_name_now_used", SkipMessages.UNUSED_IMPORTS_NOW_USED);
	}

	@Test
	public void testSkipsWhenSimpleNameNowUsed() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_when_simple_name_now_used", SkipMessages.UNUSED_IMPORTS_NOW_USED);
	}

	@Test
	public void testSkipsWhitespaceAroundDotInFqn() throws Exception {
		assertSkipResult(fixer, FIX_CONTEXT_TOPIC, "skips_whitespace_around_dot_in_fqn", SkipMessages.UNUSED_IMPORTS_NOW_USED);
	}
}