package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class LineColumnsTest {
	private static final List<String> LINES = List.of("abc", "x𝐀c", "", "𝐀𝐀b");

	@CheckReturnValue
	@Nonnull
	private static DetailAstImpl at(int lineNo, int column) {
		final var node = new DetailAstImpl();
		node.setLineNo(lineNo);
		node.setColumnNo(column);
		return node;
	}

	@CsvSource({
			"1, 0, 0",
			"1, 2, 2",
			"1, 3, 3",
			"1, 4, -1",
			"1, -1, -1",
			"2, 1, 1",
			"2, 2, 3",
			"2, 3, 4",
			"2, 4, -1",
			"3, 0, 0",
			"3, 1, -1",
			"4, 0, 0",
			"4, 1, 2",
			"4, 2, 4",
			"4, 3, 5",
			"4, 4, -1",
			"0, 0, -1",
			"5, 0, -1"
	})
	@ParameterizedTest
	public void testCharIndex(int lineNo, int column, int expected) {
		assertEquals(expected, new LineColumns(LINES).charIndex(at(lineNo, column)));
	}

	@Test
	public void testCharIndexAgreesWithLineTextOnEveryColumnOnceMapped() {
		final var columns = new LineColumns(LINES);
		for (var lineNo = 1; lineNo <= LINES.size(); ++lineNo) {
			final var line = LINES.get(lineNo - 1);
			for (var pass = 0; pass < 2; ++pass) {
				for (var column = 0; column <= line.codePointCount(0, line.length()) + 1; ++column)
					assertEquals(LineText.charIndexOfColumn(line, column), columns.charIndex(at(lineNo, column)), "line " + lineNo + " column " + column);
			}
		}
	}

	@Test
	public void testLines() {
		assertSame(LINES, new LineColumns(LINES).lines());
	}
}