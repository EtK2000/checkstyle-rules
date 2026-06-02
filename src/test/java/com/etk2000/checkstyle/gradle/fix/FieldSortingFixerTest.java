package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FieldSortingFixerTest {
	private static final String TOPIC = "fieldsorting";

	private final CheckstyleFixer fixer = new FieldSortingFixer();

	@Test
	public void testFieldUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "field_unparseable_buffer");
	}

	@CsvSource(delimiter = '|', quoteCharacter = '#', value = {
			"B(1, 2)|-1",
			"B(1, 2),|7",
			"),|1",
			"ALPHA;|5",
			"INSTANCE { x, y }|-1",
			"B(\"x, y\"),|9",
			"ALPHA, // x, y|5",
			"A /* , */;|9",
			"A, /*|1",
			"B(','),|6",
			"A = \"\\\"\", B;|11",
			"A = '\\'', B;|11",
			"}, x;|4",
			"A \",\"|-1",
			"A ','|-1",
			"A /* , */|-1"
	})
	@ParameterizedTest
	public void testLastTerminalSepIndex(String input, int expected) {
		assertEquals(expected, FieldSortingFixer.lastTerminalSepIndex(input));
	}

	@Test
	public void testStripCommentsAndStringsBlockComment() {
		assertEquals("     ", JavaLineScanner.stripCommentsAndStrings("/*x*/", JavaLineScanner.LexerState.NONE));
	}

	@Test
	public void testStripCommentsAndStringsIncomingBlockCommentState() {
		assertEquals("     end", JavaLineScanner.stripCommentsAndStrings("abc*/end", new LexerState(true, false)));
	}

	@Test
	public void testStripCommentsAndStringsIncomingTextBlockState() {
		assertEquals("      end", JavaLineScanner.stripCommentsAndStrings("abc\"\"\"end", new LexerState(false, true)));
	}

	@Test
	public void testStripCommentsAndStringsLineComment() {
		assertEquals("a   ", JavaLineScanner.stripCommentsAndStrings("a//x", JavaLineScanner.LexerState.NONE));
	}

	@Test
	public void testStripCommentsAndStringsStringLiteral() {
		assertEquals("\"   \"", JavaLineScanner.stripCommentsAndStrings("\"abc\"", JavaLineScanner.LexerState.NONE));
	}
}