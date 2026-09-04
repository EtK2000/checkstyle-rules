package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck.AnnotationRedundancy;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck.AnnotationSyntaxTarget;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link RedundantAnnotationSyntaxCheck#redundantSyntaxAt}, the seam
 * {@code RedundantAnnotationSyntaxFixer} dispatches on. The fixer deletes a span bounded by the
 * descriptor's own nodes, so the rows that matter most are the ones asserting where those nodes
 * stand, and the ones asserting that a position the check never reports classifies as nothing
 * rather than handing back a neighbouring annotation's span.
 */
public class RedundantAnnotationSyntaxLocatorTest {
	private static final String FIXTURE = "redundantannotationsyntax/InputLocatorPositions.java";

	/** An AST column counts code points, so a char index into the line is converted. */
	@CheckReturnValue
	private static int columnOf(@Nonnull String line, @Nonnull String needle) {
		final var index = line.indexOf(needle);
		if (index < 0)
			throw new IllegalArgumentException("no [" + needle + "] in [" + line + "]");
		return line.codePointCount(0, index);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return Files.readAllLines(Path.of(resource().toURI()), StandardCharsets.UTF_8);
	}

	/**
	 * The first 0-based line below {@code method}'s declaration carrying {@code needle}. Anchoring
	 * inside one method keeps a needle such as {@code @}, which every row shares, pointing at the one
	 * the test is about.
	 */
	@CheckReturnValue
	private static int lineIn(@Nonnull List<String> lines, @Nonnull String method, @Nonnull String needle) {
		for (var i = lineWith(lines, ' ' + method + "(") + 1; i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] under [" + method + "] in " + FIXTURE);
	}

	/** The first 0-based line of the fixture carrying {@code needle}. */
	@CheckReturnValue
	private static int lineWith(@Nonnull List<String> lines, @Nonnull String needle) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] in " + FIXTURE);
	}

	/** What the check classifies where {@code needle} begins, inside {@code method}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationSyntaxTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationSyntaxTarget locateAbsolute(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return RedundantAnnotationSyntaxCheck.redundantSyntaxAt(
				parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column
		);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationSyntaxTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return RedundantAnnotationSyntaxCheck.redundantSyntaxAt(
				parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	/** What the check classifies at {@code column} on the 0-based {@code line}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationSyntaxTarget locateLine(int line, int column) throws Exception {
		return RedundantAnnotationSyntaxCheck.redundantSyntaxAt(parseFixture(), line, column);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var file = new File(resource().toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource() {
		return requireNonNull(
				RedundantAnnotationSyntaxLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void annotationInsideArrayInitClassifies() throws Exception {
		final var target = locateColumn("annotationInsideArrayInit", "@LocatorMarkers", "@LocatorMarker(", 0);
		assertNotNull(target);
		assertEquals("LocatorMarker", target.targetName());
	}

	@Test
	public void columnOneLeftFindsNothing() throws Exception {
		assertNull(locateColumn("exactColumnEmptyParens", "@LocatorMarker()", "@LocatorMarker()", -1));
	}

	@Test
	public void columnOneRightFindsNothing() throws Exception {
		assertNull(locateColumn("exactColumnEmptyParens", "@LocatorMarker()", "@LocatorMarker()", 1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("exactColumnEmptyParens", "@LocatorMarker()", 4096));
	}

	/**
	 * The parse carries no comment nodes, so a comment between the {@code @} and the name is
	 * invisible to the locator and the name node simply starts past it. That is what the text
	 * scanner could not do: its {@code @\w} match failed on the {@code /}.
	 */
	@Test
	public void commentAfterAtClassifies() throws Exception {
		final var target = locate("commentAfterAt", "@");
		assertNotNull(target);
		assertEquals("LocatorMarker", target.targetName());
	}

	@Test
	public void decoyInsideCommentFindsNothing() throws Exception {
		assertNull(locate("decoyInsideComment", "@LocatorMarker()"));
	}

	@Test
	public void decoyInsideStringFindsNothing() throws Exception {
		assertNull(locate("decoyInsideString", "@LocatorMarker()"));
	}

	@Test
	public void decoyInsideTextBlockFindsNothing() throws Exception {
		assertNull(locate("decoyInsideTextBlock", "@LocatorMarker()"));
	}

	@Test
	public void emptyArrayInitFindsNothing() throws Exception {
		assertNull(locate("emptyArrayInit", "@LocatorStrings"));
	}

	/**
	 * The two positions the empty-parentheses fix deletes between. Nothing else decides how much
	 * text goes, so a wrong answer here is a wrong edit rather than a missed one.
	 */
	@Test
	public void emptyParensSpanEdgesAreNameEndAndRparenEnd() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumnEmptyParens", "@LocatorMarker()");
		final var target = locate("exactColumnEmptyParens", "@LocatorMarker()");
		assertNotNull(target);
		final var text = lines.get(line);
		assertEquals(columnOf(text, "LocatorMarker"), target.name().getColumnNo());
		assertEquals(columnOf(text, ")"), target.rparen().getColumnNo());
	}

	@Test
	public void exactColumnEmptyParensClassifies() throws Exception {
		final var target = locate("exactColumnEmptyParens", "@LocatorMarker()");
		assertNotNull(target);
		assertEquals(AnnotationRedundancy.EMPTY_PARENS, target.redundancy());
		assertEquals("LocatorMarker", target.targetName());
		assertNull(target.key());
		assertNull(target.value());
	}

	@Test
	public void exactColumnExplicitValueClassifies() throws Exception {
		final var target = locate("exactColumnExplicitValue", "@LocatorNum");
		assertNotNull(target);
		assertEquals(AnnotationRedundancy.EXPLICIT_VALUE, target.redundancy());
		assertEquals("LocatorNum", target.targetName());
		assertNotNull(target.key());
		assertNotNull(target.value());
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumnEmptyParens", "@LocatorMarker()");
		assertNull(locateLine(line - 1, columnOf(lines.get(line), "@LocatorMarker()")));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumnEmptyParens", "@LocatorMarker()");
		assertNull(locateLine(line + 1, columnOf(lines.get(line), "@LocatorMarker()")));
	}

	@Test
	public void lineBeyondBufferFindsNothing() throws Exception {
		assertNull(locateLine(fixtureLines().size() + 10, 0));
	}

	@Test
	public void markerAnnotationFindsNothing() throws Exception {
		assertNull(locate("markerAnnotation", "@LocatorMarker"));
	}

	@Test
	public void multipleMembersFindsNothing() throws Exception {
		assertNull(locate("multipleMembers", "@LocatorMulti"));
	}

	@Test
	public void negativeColumnFindsNothing() throws Exception {
		assertNull(locateAbsolute("exactColumnEmptyParens", "@LocatorMarker()", -1));
	}

	/**
	 * Two reports on one line, of different kinds. The fixer dispatches on the descriptor's kind, so
	 * the outer position answering the inner annotation's kind would delete the wrong text.
	 */
	@Test
	public void nestedAnnotationsClassifyIndependently() throws Exception {
		final var outer = locate("nestedAnnotations", "@LocatorNested");
		final var inner = locateColumn("nestedAnnotations", "@LocatorNested", "@LocatorMarker()", 0);
		assertNotNull(outer);
		assertNotNull(inner);
		assertEquals(AnnotationRedundancy.EXPLICIT_VALUE, outer.redundancy());
		assertEquals(AnnotationRedundancy.EMPTY_PARENS, inner.redundancy());
		assertNotEquals(outer.annotation(), inner.annotation());
	}

	@Test
	public void nonAsciiNameClassifies() throws Exception {
		final var target = locate("nonAsciiName", "@LocatorÄ()");
		assertNotNull(target);
		assertEquals("LocatorÄ", target.targetName());
	}

	@Test
	public void nonValueKeyFindsNothing() throws Exception {
		assertNull(locate("nonValueKey", "@LocatorKey"));
	}

	/** The two positions the {@code value =} fix deletes between. */
	@Test
	public void pairKeyAndValuePositions() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumnExplicitValue", "@LocatorNum");
		final var target = locate("exactColumnExplicitValue", "@LocatorNum");
		assertNotNull(target);
		final var text = lines.get(line);
		assertEquals(columnOf(text, "value"), target.key().getColumnNo());
		assertEquals(columnOf(text, "1"), target.value().getColumnNo());
	}

	/**
	 * The name node covers the whole chain, so its end is past the last identifier rather than past
	 * the first. Slicing from the wrong one would leave {@code .Qualified()} behind.
	 */
	@Test
	public void qualifiedNameEndsAtTheLastIdent() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "qualifiedName", "@com.example.Qualified()");
		final var target = locate("qualifiedName", "@com.example.Qualified()");
		assertNotNull(target);
		final var end = AstSpan.spanEnd(lines, target.name());
		assertNotNull(end);
		assertEquals(line, end.line());
		assertEquals(lines.get(line).indexOf("()"), end.index());
	}

	@Test
	public void shorthandValueFindsNothing() throws Exception {
		assertNull(locate("shorthandValue", "@LocatorStrings"));
	}

	@Test
	public void spaceAfterAtClassifies() throws Exception {
		final var target = locate("spaceAfterAt", "@ LocatorMarker()");
		assertNotNull(target);
		assertEquals("LocatorMarker", target.targetName());
	}

	/** The reported {@code @} and the name node that bounds the deletion sit on different lines. */
	@Test
	public void splitQualifiedNameClassifies() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "splitQualifiedName", "@com.example");
		final var target = locate("splitQualifiedName", "@com.example");
		assertNotNull(target);
		final var end = AstSpan.spanEnd(lines, target.name());
		assertNotNull(end);
		assertEquals(line + 1, end.line());
	}

	/**
	 * The column is counted in code points, so the astral character ahead of the annotation shifts
	 * it one below the char index. Looking it up at the char index finds nothing, which is what
	 * makes the conversion load-bearing rather than cosmetic.
	 */
	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryAhead", "@LocatorMarker()");
		final var text = lines.get(line);
		assertNotNull(locateLine(line, text.codePointCount(0, text.indexOf("@LocatorMarker()"))));
		assertNull(locateLine(line, text.indexOf("@LocatorMarker()")));
	}

	@Test
	public void twoAnnotationsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("twoAnnotationsOnOneLine", "@LocatorMarker()");
		final var other = locateColumn("twoAnnotationsOnOneLine", "@LocatorMarker()", "@LocatorStrings", 0);
		assertNotNull(first);
		assertNull(other);
		assertEquals(AnnotationRedundancy.EMPTY_PARENS, first.redundancy());
	}

	@Test
	public void typeUseAnnotationClassifies() throws Exception {
		final var target = locate("typeUseAnnotation", "@LocatorMarker()");
		assertNotNull(target);
		assertEquals(AnnotationRedundancy.EMPTY_PARENS, target.redundancy());
	}

	/**
	 * The value node starts at the sign, not at the digits. Slicing from the digits would leave the
	 * {@code -} on the wrong side of the deletion and flip the value.
	 */
	@Test
	public void unaryMinusValueStartsAtTheSign() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "unaryMinusValue", "@LocatorNum");
		final var target = locate("unaryMinusValue", "@LocatorNum");
		assertNotNull(target);
		final var start = AstSpan.spanStart(lines, target.value());
		assertNotNull(start);
		assertEquals(lines.get(line).indexOf('-'), start.index());
	}

	@Test
	public void valueStatementFindsNothing() throws Exception {
		assertNull(locate("valueStatement", "value"));
	}
}