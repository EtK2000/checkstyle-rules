package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests for the regex-based checkstyle rules defined in checkstyle.xml
 * (RegexpSingleline and RegexpMultiline modules).
 */
public class RegexRulesTest {
	private static final String DIR = "regex/";
	private static final String FMT_BLANK_LINE_AFTER_BREAK = "break\\s*;\\n[^\\S\\n]*(case |default[\\s:])";
	private static final String FMT_NO_BLANK_LINE_AFTER_CLASS_BRACE = "(class|interface|enum|record)\\s+\\w[^{]*\\{\\s*\\n\\s*\\n";
	private static final String FMT_NO_BLANK_LINE_BEFORE_CLOSING_BRACE = "\\n[^\\S\\n]*\\n[^\\S\\n]*\\}";
	private static final String FMT_NO_DOUBLE_BLANK_LINES = "\\n\\s*\\n\\s*\\n";
	private static final String FMT_NO_SPACE_INDENT = "^ (?!\\*)(?!@)";
	private static final String FMT_NO_TRAILING_NEWLINE = "\\n\\z";
	private static final String FMT_NO_TRAILING_WHITESPACE = "[ \\t]+$";
	private static final String MULTI = "RegexpMultiline";
	private static final String SINGLE = "RegexpSingleline";

	// --- BlankLineAfterBreak ---

	@Test
	public void testBlankLineAfterBreakClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_BLANK_LINE_AFTER_BREAK, DIR + "cases.blanklineafterbreak.clean.java").isEmpty());
	}

	@Test
	public void testBlankLineAfterBreakViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_BLANK_LINE_AFTER_BREAK, DIR + "cases.blanklineafterbreak.in.java");
		assertEquals(2, violations.size());
	}

	// --- NoBlankLineAfterClassBrace ---

	@Test
	public void testNoBlankLineAfterClassBraceClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_AFTER_CLASS_BRACE, DIR + "cases.noblanklineafterclassbrace.clean.java").isEmpty());
	}

	@Test
	public void testNoBlankLineAfterClassBraceViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_AFTER_CLASS_BRACE, DIR + "cases.noblanklineafterclassbrace.in.java");
		assertEquals(4, violations.size());
	}

	// --- NoBlankLineBeforeClosingBrace ---

	@Test
	public void testNoBlankLineBeforeClosingBraceClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_BEFORE_CLOSING_BRACE, DIR + "cases.noblanklinebeforeclosingbrace.clean.java").isEmpty());
	}

	@Test
	public void testNoBlankLineBeforeClosingBraceViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_BEFORE_CLOSING_BRACE, DIR + "cases.noblanklinebeforeclosingbrace.in.java");
		assertEquals(1, violations.size());
	}

	// --- NoDoubleBlankLines ---

	@Test
	public void testNoDoubleBlankLinesClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_DOUBLE_BLANK_LINES, DIR + "cases.nodoubleblanklines.clean.java").isEmpty());
	}

	@Test
	public void testNoDoubleBlankLinesViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_DOUBLE_BLANK_LINES, DIR + "cases.nodoubleblanklines.in.java");
		assertEquals(1, violations.size());
	}

	// --- NoSpaceIndent ---

	@Test
	public void testNoSpaceIndentClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(SINGLE, FMT_NO_SPACE_INDENT, DIR + "cases.nospaceindent.clean.java").isEmpty());
	}

	@Test
	public void testNoSpaceIndentViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(SINGLE, FMT_NO_SPACE_INDENT, DIR + "cases.nospaceindent.in.java");
		assertEquals(2, violations.size());
	}

	// --- NoTrailingNewline (inline strings — editors add trailing newlines) ---

	@Test
	public void testNoTrailingNewlineClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheckInline(MULTI, FMT_NO_TRAILING_NEWLINE, "class Foo {}").isEmpty());
	}

	@Test
	public void testNoTrailingNewlineViolation() throws Exception {
		assertEquals(1, BaseCheckTest.runRegexCheckInline(MULTI, FMT_NO_TRAILING_NEWLINE, "class Foo {}\n").size());
	}

	@Test
	public void testNoTrailingNewlineViolationMultiple() throws Exception {
		assertEquals(1, BaseCheckTest.runRegexCheckInline(MULTI, FMT_NO_TRAILING_NEWLINE, "class Foo {}\n\n").size());
	}

	// --- NoTrailingWhitespace (inline strings — editors strip trailing whitespace) ---

	@Test
	public void testNoTrailingWhitespaceClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheckInline(SINGLE, FMT_NO_TRAILING_WHITESPACE, "int x;").isEmpty());
	}

	@Test
	public void testNoTrailingWhitespaceViolationSpace() throws Exception {
		assertEquals(1, BaseCheckTest.runRegexCheckInline(SINGLE, FMT_NO_TRAILING_WHITESPACE, "int x; ").size());
	}

	@Test
	public void testNoTrailingWhitespaceViolationTab() throws Exception {
		assertEquals(1, BaseCheckTest.runRegexCheckInline(SINGLE, FMT_NO_TRAILING_WHITESPACE, "int x;\t").size());
	}
}