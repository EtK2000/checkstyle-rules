package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class BlankLineAfterClassBraceFixerTest {
	private static final String TOPIC = "blanklineafterclassbrace";

	private final CheckstyleFixer fixer = new BlankLineAfterClassBraceFixer();

	@Test
	public void testDeleteMixedWhitespaceBlanks() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_mixed_whitespace_blanks");
	}

	@Test
	public void testDeleteMultipleBlanksAfterClassBrace() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_multiple_blanks_after_class_brace");
	}

	@Test
	public void testDeleteSingleBlankAfterClassBrace() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_single_blank_after_class_brace");
	}

	@Test
	public void testDeleteWhitespaceOnlyBlank() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "delete_whitespace_only_blank");
	}

	@Test
	public void testEnumKeyword() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "enum_keyword");
	}

	@Test
	public void testInterfaceKeyword() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "interface_keyword");
	}

	@Test
	public void testMultiLineDeclaration() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "multi_line_declaration");
	}

	@Test
	public void testNoBlankAfterBrace() throws Exception {
		assertSkip(fixer, TOPIC, "no_blank_after_brace");
	}

	@Test
	public void testNoBraceFound() throws Exception {
		assertSkip(fixer, TOPIC, "no_brace_found");
	}

	@Test
	public void testRecordKeyword() throws Exception {
		// can't migrate: NoBlankLineAfterClassBrace is a RegexpMultiline check, no AbstractCheck class for assertCaseFix
		assertSimpleFix(fixer, TOPIC, "record_keyword");
	}
}