package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.SourceComments.Comment;
import com.etk2000.checkstyle.ast.DocComments;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class SourceCommentsTest {
	static Stream<Arguments> bodyProvider() {
		return Stream.of(
				Arguments.of("line comment", "// region Tuning ", "region Tuning"),
				Arguments.of("empty line comment", "//", ""),
				Arguments.of("markdown doc comment", "/// region", "/ region"),
				Arguments.of("block comment", "/* region */", "region"),
				Arguments.of("doc comment", "/** region */", "region"),
				Arguments.of("starred block comment", "/***/", ""),
				Arguments.of("empty block comment", "/**/", "")
		);
	}

	static Stream<Arguments> isRangeDirectiveProvider() {
		return Stream.of(
				Arguments.of("// @formatter:off", true),
				Arguments.of("// @formatter:on", true),
				Arguments.of("// CHECKSTYLE.OFF: MagicNumber", true),
				Arguments.of("// CHECKSTYLE.ON: MagicNumber", true),
				Arguments.of("// CHECKSTYLE:OFF", true),
				Arguments.of("// CHECKSTYLE:ON", true),
				Arguments.of("// spotless:off", true),
				Arguments.of("// spotless:on", true),
				Arguments.of("// checkstyle:off", false),
				Arguments.of("//region Tuning", true),
				Arguments.of("// region", true),
				Arguments.of("// endregion", true),
				Arguments.of("//endregion Tuning", true),
				Arguments.of("//#region Tuning", true),
				Arguments.of("// #region", true),
				Arguments.of("// #endregion", true),
				Arguments.of("//#endregion Tuning", true),
				Arguments.of("/* region Tuning */", true),
				Arguments.of("// <editor-fold desc=\"x\">", true),
				Arguments.of("// </editor-fold>", true),
				Arguments.of("// regional default", false),
				Arguments.of("// #regional default", false),
				Arguments.of("// #endregions", false),
				Arguments.of("// the region it ends", false),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES", true),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINE", true),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber for next 3 lines", true),
				Arguments.of("// CHECKSTYLE SUPPRESS MagicNumber FOR +2 LINES", true),
				Arguments.of("// CHECKSTYLE SUPPRESS MagicNumber FOR -2 LINES", true),
				Arguments.of("/* CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINES */", true),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber", false),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber FOR NEXT LINES", false),
				Arguments.of("// CHECKSTYLE IGNORE MagicNumber FOR NEXT 2 LINESX", false),
				Arguments.of("// IGNORE MagicNumber FOR NEXT 2 LINES", false),
				Arguments.of("// checkstyle IGNORE MagicNumber FOR NEXT 2 LINES", false),
				Arguments.of("// NOSONAR", false)
		);
	}

	static Stream<Arguments> leadingDirectivesProvider() {
		return Stream.of(
				Arguments.of("statement directive on its own line above", List.of("//noinspection unused", "int alpha;"), List.of("//noinspection unused")),
				Arguments.of("statement directive trailing the line above", List.of("int label; //noinspection unused", "int alpha;"), List.of("//noinspection unused")),
				Arguments.of("tool directive trailing the line above", List.of("int label; // NOSONAR", "int alpha;"), List.of()),
				Arguments.of("tool directive on its own line above", List.of("// language=SQL", "String alpha;"), List.of("// language=SQL")),
				Arguments.of("directive above a blank line", List.of("//noinspection unused", "", "int alpha;"), List.of("//noinspection unused")),
				Arguments.of("directive above code", List.of("//noinspection unused", "int label;", "int alpha;"), List.of()),
				Arguments.of("directive above another comment", List.of("//noinspection unused", "// plain", "int alpha;"), List.of("//noinspection unused")),
				Arguments.of("two directives above", List.of("// NOSONAR", "//noinspection unused", "int alpha;"), List.of("// NOSONAR", "//noinspection unused")),
				Arguments.of("statement directive on an annotation line", List.of("@Deprecated //noinspection unused", "int alpha;"), List.of("//noinspection unused")),
				Arguments.of("tool directive on an annotation line", List.of("@Deprecated // NOSONAR", "int alpha;"), List.of()),
				Arguments.of("tool directive between the annotation and the type", List.of("@Deprecated", "// NOSONAR", "int alpha;"), List.of("// NOSONAR")),
				Arguments.of(
						"statement directive between two annotations",
						List.of("@Deprecated", "//noinspection unused", "@SuppressWarnings(\"x\")", "int alpha;"),
						List.of("//noinspection unused")
				),
				Arguments.of("block directive ahead of the type", List.of("/* NOSONAR */ int alpha;"), List.of()),
				Arguments.of("block directive ahead of an annotation on its line", List.of("/* NOSONAR */ @Deprecated", "int alpha;"), List.of()),
				Arguments.of("block directive closing on the name's line", List.of("/* NOSONAR", "*/ int alpha;"), List.of("/* NOSONAR\n*/")),
				Arguments.of("block directive spanning lines above", List.of("/* NOSONAR", "*/", "int alpha;"), List.of("/* NOSONAR\n*/")),
				Arguments.of("block directive spanning lines, closing ahead of an annotation", List.of("/* NOSONAR", "*/ @Deprecated", "int alpha;"), List.of("/* NOSONAR\n*/")),
				Arguments.of("block directive opening after code above, closing on the name's line", List.of("int label; /* NOSONAR", "*/ int alpha;"), List.of()),
				Arguments.of("block directive on its own line above", List.of("/* NOSONAR */", "int alpha;"), List.of("/* NOSONAR */")),
				Arguments.of("block directive followed by whitespace above", List.of("/* NOSONAR */\t ", "int alpha;"), List.of("/* NOSONAR */")),
				Arguments.of("block statement directive trailing the line above", List.of("int label; /* noinspection unused */", "int alpha;"), List.of()),
				Arguments.of("tool directive between the type and the name", List.of("int", "// NOSONAR", "alpha;"), List.of("// NOSONAR")),
				Arguments.of("doc comment holding a directive above", List.of("/** NOSONAR */", "int alpha;"), List.of("/** NOSONAR */")),
				Arguments.of("plain comment above", List.of("// plain", "int alpha;"), List.of()),
				Arguments.of("capitalised near miss above", List.of("// NoInspection unused", "int alpha;"), List.of()),
				Arguments.of("directive past the name", List.of("int alpha; //noinspection unused"), List.of()),
				Arguments.of("directive text in a string above", List.of("String label = \"//noinspection unused\";", "int alpha;"), List.of())
		);
	}

	static Stream<Arguments> lineCountWindowsProvider() {
		return Stream.of(
				Arguments.of("line comment counting the lines after it", List.of("// CHECKSTYLE IGNORE X FOR NEXT 2 LINES"), List.of(List.of(2L, 3L))),
				Arguments.of("block comment counting from its last line", List.of("/* CHECKSTYLE IGNORE X FOR NEXT 1 LINES", "*/"), List.of(List.of(3L, 3L))),
				Arguments.of("count signed plus", List.of("// CHECKSTYLE SUPPRESS X FOR +1 LINES"), List.of(List.of(2L, 2L))),
				Arguments.of("count signed minus", List.of("int a;", "int b;", "/* CHECKSTYLE SUPPRESS X FOR -2 LINES", "*/"), List.of(List.of(1L, 2L))),
				Arguments.of("count of nine digits", List.of("// CHECKSTYLE IGNORE X FOR NEXT 999999999 LINES"), List.of(List.of(2L, 1_000_000_000L))),
				Arguments.of("count of ten digits", List.of("// CHECKSTYLE IGNORE X FOR NEXT 1000000000 LINES"), List.of(List.of(2L, 1L + Integer.MAX_VALUE))),
				Arguments.of("count of zero lines", List.of("// CHECKSTYLE IGNORE X FOR NEXT 0 LINES"), List.of()),
				Arguments.of("count of zero lines signed minus", List.of("int a;", "// CHECKSTYLE SUPPRESS X FOR -0 LINES"), List.of()),
				Arguments.of("count of zero lines signed plus", List.of("// CHECKSTYLE SUPPRESS X FOR +0 LINES"), List.of()),
				Arguments.of("two directives", List.of("// CHECKSTYLE IGNORE X FOR NEXT 1 LINES", "int a;", "// CHECKSTYLE IGNORE Y FOR NEXT 1 LINES"), List.of(List.of(2L, 2L), List.of(4L, 4L))),
				Arguments.of("range directive without a count", List.of("// CHECKSTYLE:OFF"), List.of()),
				Arguments.of("count in a string", List.of("String s = \"// CHECKSTYLE IGNORE X FOR NEXT 2 LINES\";"), List.of())
		);
	}

	static Stream<Arguments> rangeDirectiveOnProvider() {
		return Stream.of(
				Arguments.of("on the first line", List.of("int a;", "// CHECKSTYLE:OFF", "int b;"), 2, 3, true),
				Arguments.of("on the last line", List.of("int a;", "// CHECKSTYLE:OFF", "int b;"), 1, 2, true),
				Arguments.of("above the range", List.of("int a;", "// CHECKSTYLE:OFF", "int b;"), 3, 3, false),
				Arguments.of("below the range", List.of("int a;", "// CHECKSTYLE:OFF", "int b;"), 1, 1, false),
				Arguments.of("block comment reaching into the range", List.of("/* @formatter:off", "*/ int a;", "int b;"), 2, 3, true),
				Arguments.of("block comment closing above the range", List.of("/* @formatter:off", "*/", "int a;"), 3, 3, false),
				Arguments.of("block comment spanning the range", List.of("/* @formatter:off", "int a;", "*/"), 2, 2, true),
				Arguments.of("plain directive", List.of("// NOSONAR", "int a;"), 1, 2, false),
				Arguments.of("range directive after a plain comment on the line", List.of("int a; /* x */ // CHECKSTYLE:OFF"), 1, 1, true),
				Arguments.of("directive counting lines", List.of("// CHECKSTYLE IGNORE X FOR NEXT 1 LINES", "int a;"), 1, 1, true)
		);
	}

	@MethodSource("bodyProvider")
	@ParameterizedTest(name = "{0}")
	public void body(@Nonnull String name, @Nonnull String comment, @Nonnull String expected) {
		assertEquals(expected, SourceComments.body(comment));
	}

	@MethodSource("isRangeDirectiveProvider")
	@ParameterizedTest(name = "{0}")
	public void isRangeDirective(@Nonnull String comment, boolean expected) {
		assertEquals(expected, SourceComments.isRangeDirective(comment));
	}

	@MethodSource("leadingDirectivesProvider")
	@ParameterizedTest(name = "{0}")
	public void leadingDirectives(@Nonnull String name, @Nonnull List<String> body, @Nonnull List<String> expected) throws Exception {
		final var lines = new ArrayList<String>();
		lines.add("class T {");
		lines.addAll(body);
		lines.add("}");
		final var root = JavaParser.parse(new FileContents(new FileText(new File("T.java"), lines)));
		final var objBlock = root.findFirstToken(TokenTypes.CLASS_DEF).findFirstToken(TokenTypes.OBJBLOCK);
		var alpha = objBlock.getFirstChild();
		while (alpha.getType() != TokenTypes.VARIABLE_DEF || !"alpha".equals(FieldSortingCheck.fieldName(alpha)))
			alpha = alpha.getNextSibling();
		final var directives = SourceComments.leadingDirectives(new LineColumns(lines), SourceComments.scan(lines), alpha);
		assertEquals(expected, directives.stream().map(Comment::text).toList());
	}

	@MethodSource("lineCountWindowsProvider")
	@ParameterizedTest(name = "{0}")
	public void lineCountWindows(@Nonnull String name, @Nonnull List<String> lines, @Nonnull List<List<Long>> expected) {
		final var windows = SourceComments.lineCountWindows(SourceComments.scan(lines));
		assertEquals(expected, windows.windows().stream().map(window -> List.of(window[0], window[1])).toList());
	}

	@MethodSource("rangeDirectiveOnProvider")
	@ParameterizedTest(name = "{0}")
	public void rangeDirectiveOn(@Nonnull String name, @Nonnull List<String> lines, int fromLine, int toLine, boolean expected) {
		assertEquals(expected, SourceComments.rangeDirectiveOn(SourceComments.scan(lines), fromLine, toLine));
	}

	@Test
	public void testCommentsFromCountsACommentStartingAtThePosition() {
		final var comments = List.of(
				new Comment(DocComments.position(1, 0), DocComments.position(1, 4), "/**/"),
				new Comment(DocComments.position(2, 0), DocComments.position(2, 4), "/**/"),
				new Comment(DocComments.position(3, 0), DocComments.position(3, 4), "/**/")
		);
		assertEquals(
				List.of(0, 1, 2, 3),
				List.of(
						SourceComments.commentsFrom(comments, DocComments.position(1, 0)),
						SourceComments.commentsFrom(comments, DocComments.position(2, 0)),
						SourceComments.commentsFrom(comments, DocComments.position(2, 1)),
						SourceComments.commentsFrom(comments, DocComments.position(3, 5))
				)
		);
	}

	@Test
	public void testLineCountReachesAShortWindowPastALongEarlierOne() {
		final var windows = SourceComments.lineCountWindows(SourceComments.scan(List.of(
				"// CHECKSTYLE IGNORE X FOR NEXT 9 LINES",
				"// CHECKSTYLE IGNORE Y FOR NEXT 1 LINES"
		)));
		assertEquals(List.of(List.of(2L, 10L), List.of(3L, 3L)), windows.windows().stream().map(window -> List.of(window[0], window[1])).toList());
		assertEquals(List.of(true, false), List.of(windows.reaches(8, 9), windows.reaches(11, 20)));
	}

	@Test
	public void testLineCountReachesTheLinesOfAWindow() {
		final var windows = SourceComments.lineCountWindows(SourceComments.scan(List.of("int a;", "// CHECKSTYLE IGNORE X FOR NEXT 2 LINES")));
		assertEquals(
				List.of(false, true, true, true, false, false),
				List.of(
						windows.reaches(1, 2),
						windows.reaches(2, 3),
						windows.reaches(3, 4),
						windows.reaches(4, 5),
						windows.reaches(5, 6),
						SourceComments.lineCountWindows(List.of()).reaches(1, 9)
				)
		);
	}

	@Test
	public void testLineCountWindowsAreOrderedByTheirFirstLine() {
		final var windows = SourceComments.lineCountWindows(SourceComments.scan(List.of(
				"// CHECKSTYLE IGNORE Y FOR NEXT 1 LINES",
				"int a;",
				"int b;",
				"int c;",
				"// CHECKSTYLE IGNORE X FOR -4 LINES"
		)));
		assertEquals(List.of(List.of(1L, 4L), List.of(2L, 2L)), windows.windows().stream().map(window -> List.of(window[0], window[1])).toList());
		assertEquals(List.of(true, true, false), List.of(windows.reaches(1, 1), windows.reaches(4, 4), windows.reaches(5, 9)));
	}
}