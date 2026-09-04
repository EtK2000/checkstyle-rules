package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;

import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

import java.io.File;
import java.nio.file.Files;
import java.util.TreeSet;

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

	@Test
	public void testBlankLineAfterBreakClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_BLANK_LINE_AFTER_BREAK, DIR + "cases.blanklineafterbreak.clean.java").isEmpty());
	}

	@Test
	public void testBlankLineAfterBreakViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_BLANK_LINE_AFTER_BREAK, DIR + "cases.blanklineafterbreak.in.java");
		assertEquals(2, violations.size());
	}

	@Test
	public void testNoBlankLineAfterClassBraceClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_AFTER_CLASS_BRACE, DIR + "cases.noblanklineafterclassbrace.clean.java").isEmpty());
	}

	@Test
	public void testNoBlankLineAfterClassBraceViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_AFTER_CLASS_BRACE, DIR + "cases.noblanklineafterclassbrace.in.java");
		assertEquals(4, violations.size());
	}

	@Test
	public void testNoBlankLineBeforeClosingBraceClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_BEFORE_CLOSING_BRACE, DIR + "cases.noblanklinebeforeclosingbrace.clean.java").isEmpty());
	}

	@Test
	public void testNoBlankLineBeforeClosingBraceViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_BLANK_LINE_BEFORE_CLOSING_BRACE, DIR + "cases.noblanklinebeforeclosingbrace.in.java");
		assertEquals(1, violations.size());
	}

	@Test
	public void testNoDoubleBlankLinesClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(MULTI, FMT_NO_DOUBLE_BLANK_LINES, DIR + "cases.nodoubleblanklines.clean.java").isEmpty());
	}

	@Test
	public void testNoDoubleBlankLinesViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(MULTI, FMT_NO_DOUBLE_BLANK_LINES, DIR + "cases.nodoubleblanklines.in.java");
		assertEquals(1, violations.size());
	}

	@Test
	public void testNoSpaceIndentClean() throws Exception {
		assertTrue(BaseCheckTest.runRegexCheck(SINGLE, FMT_NO_SPACE_INDENT, DIR + "cases.nospaceindent.clean.java").isEmpty());
	}

	@Test
	public void testNoSpaceIndentViolation() throws Exception {
		final var violations = BaseCheckTest.runRegexCheck(SINGLE, FMT_NO_SPACE_INDENT, DIR + "cases.nospaceindent.in.java");
		assertEquals(2, violations.size());
	}

	// inline strings, since editors add trailing newlines to fixture files

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

	// inline strings, since editors strip trailing whitespace from fixture files

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

	// MessageFormat would eat a quote or brace in a configured message
	@Test
	public void testShippedMessagesRenderVerbatim() throws Exception {
		final var shippedUrl = RegexRulesTest.class.getResource("/com/etk2000/checkstyle/checkstyle.xml");
		assertNotNull(shippedUrl);
		final var shipped = ConfigurationLoader.loadConfiguration(
				new InputSource(shippedUrl.toExternalForm()),
				name -> "",
				ConfigurationLoader.IgnoredModulesOptions.EXECUTE
		);
		final var checkerConfig = new DefaultConfiguration("Checker");
		final var configured = new TreeSet<String>();
		for (var child : shipped.getChildren()) {
			if (child.getName().startsWith("Regexp")) {
				checkerConfig.addChild(child);
				configured.add(child.getProperty("message"));
			}
		}

		final var tempFile = File.createTempFile("checkstyle-regex-messages", ".java");
		try {
			Files.writeString(
					tempFile.toPath(),
					"class A {\n\n\tint x; \n   int y;\n\n\n\tvoid m(int k) {\n\t\tswitch (k) {\n\t\t\tcase 1:\n\t\t\t\tbreak;\n"
							+ "\t\t\tcase 2:\n\t\t\t\tbreak;\n\t\t}\n\n\t}\n}\n"
			);
			final var rendered = new TreeSet<String>();
			for (var event : CheckerCache.process(checkerConfig, Checker.class.getClassLoader(), tempFile))
				rendered.add(event.getMessage());
			assertEquals(configured, rendered);
		}
		finally {
			tempFile.delete();
		}
	}
}