package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;

public class FieldSortingFixerTest {
	private static final String TOPIC = "fieldsorting";

	private final CheckstyleFixer fixer = new FieldSortingFixer();

	@Test
	public void testByteOrderMarkBufferNotFixable() {
		final var body = List.of("\tint zeta;", "\tint alpha;", "}");
		final var plain = new ArrayList<>(List.of("class T {"));
		plain.addAll(body);
		final var marked = new ArrayList<>(List.of((char) 0xFEFF + "class T {"));
		marked.addAll(body);
		assertInstanceOf(FixResult.class, fixer.fix(plain, 2, 1));
		assertNull(fixer.fix(marked, 2, 1));
	}

	@Test
	public void testFieldUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "field_unparseable_buffer");
	}

	@Test
	public void testInterfaceExplicitStaticFieldSortsWithImplicitOnes() {
		final var lines = List.of("interface I {", "\tstatic int ZETA = 2;", "\tint ALPHA = 1;", "}");
		assertEquals(new FixResult(1, 2, List.of("\tint ALPHA = 1;", "\tstatic int ZETA = 2;")), fixer.fix(lines, 2, 1));
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