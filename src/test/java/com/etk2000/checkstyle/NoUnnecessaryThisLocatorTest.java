package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.NoUnnecessaryThisCheck.ThisTarget;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

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
 * Direct tests for {@link NoUnnecessaryThisCheck#thisAt}, the seam {@code NoUnnecessaryThisFixer}
 * dispatches on. The fixer deletes everything between the two nodes the target names, so the rows
 * that matter most are the ones asserting a position does <em>not</em> classify: the reported
 * column is the dot's, and a column naming anything else must find nothing rather than delete text
 * the check never looked at.
 */
public class NoUnnecessaryThisLocatorTest {
	private static final String FIXTURE = "nounnecessarythis/InputLocatorPositions.java";

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
	 * inside one method keeps a needle such as {@code .field}, which most of them share, pointing at
	 * the one the test is about.
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
	private static ThisTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static ThisTarget locateAbsolute(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return NoUnnecessaryThisCheck.thisAt(parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static ThisTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return NoUnnecessaryThisCheck.thisAt(parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset);
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
				NoUnnecessaryThisLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void arrayAccessMemberClassifies() throws Exception {
		final var target = locate("arrayAccessMember", ".values");
		assertNotNull(target);
		assertEquals("values", target.ident().getText());
	}

	@Test
	public void arrayElementAssignmentTargetClassifies() throws Exception {
		final var target = locate("arrayElementAssignmentTarget", ".values");
		assertNotNull(target);
	}

	@Test
	public void assignmentTargetFindsNothing() throws Exception {
		assertNull(locate("assignmentTarget", ".field"));
	}

	@Test
	public void bindingInALocalClassClassifies() throws Exception {
		assertNotNull(locate("bindingInALocalClass", ".field"));
	}

	@Test
	public void bindingInAnAnonymousBodyFindsNothing() throws Exception {
		assertNull(locate("bindingInAnAnonymousBody", ".field"));
	}

	@Test
	public void castReceiverFindsNothing() throws Exception {
		assertNull(locate("castReceiver", ".hashCode"));
	}

	@Test
	public void columnInsideAStringFindsNothing() throws Exception {
		assertNull(locateColumn("stringHoldingThisDot", "\"this.field\"", "\"this.field\"", 5));
	}

	@Test
	public void columnInsideATextBlockFindsNothing() throws Exception {
		assertNull(locate("textBlockHoldingThisDot", ".field"));
	}

	@Test
	public void columnOneBeforeTheDotFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", ".field", ".field", -1));
	}

	@Test
	public void columnOnePastTheDotFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", ".field", ".field", 1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("tightDot", ".field", 4096));
	}

	@Test
	public void commentAfterDotClassifies() throws Exception {
		final var target = locate("commentAfterDot", "./* c */field");
		assertNotNull(target);
		assertEquals("field", target.ident().getText());
	}

	/** The dot sits past the comment, so a classifier anchored on {@code this} would miss it. */
	@Test
	public void commentBeforeDotClassifies() throws Exception {
		final var target = locate("commentBeforeDot", ".field");
		assertNotNull(target);
		assertNotEquals(target.literalThis().getColumnNo() + 4, target.ident().getColumnNo());
	}

	@Test
	public void compoundAssignmentTargetClassifies() throws Exception {
		assertNotNull(locate("compoundAssignmentTarget", ".field"));
	}

	@Test
	public void dotOnTheNextLineClassifies() throws Exception {
		final var target = locate("dotOnTheNextLine", ".field");
		assertNotNull(target);
		assertEquals(target.literalThis().getLineNo() + 1, target.ident().getLineNo());
	}

	@Test
	public void identifierEndingInThisFindsNothing() throws Exception {
		assertNull(locate("identifierEndingInThis", ".field"));
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "tightDot", ".field");
		assertNull(NoUnnecessaryThisCheck.thisAt(parseFixture(), line - 1, columnOf(lines.get(line), ".field")));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "tightDot", ".field");
		assertNull(NoUnnecessaryThisCheck.thisAt(parseFixture(), line + 1, columnOf(lines.get(line), ".field")));
	}

	@Test
	public void negativeColumnFindsNothing() throws Exception {
		assertNull(locateAbsolute("tightDot", ".field", -1));
	}

	@Test
	public void nestedDotOuterFindsNothing() throws Exception {
		assertNull(locateColumn("nestedDotReceiver", ".inner", ".field", 0));
	}

	@Test
	public void nestedDotReceiverClassifies() throws Exception {
		final var target = locate("nestedDotReceiver", ".inner");
		assertNotNull(target);
		assertEquals("inner", target.ident().getText());
	}

	@Test
	public void parenthesizedThisFindsNothing() throws Exception {
		assertNull(locate("parenthesizedThis", ".field"));
	}

	@Test
	public void qualifiedNewFindsNothing() throws Exception {
		assertNull(locate("qualifiedNew", ".new"));
	}

	@Test
	public void shadowedNameFindsNothing() throws Exception {
		assertNull(locate("shadowedName", ".field"));
	}

	@Test
	public void spaceAfterDotClassifies() throws Exception {
		final var target = locate("spaceAfterDot", ". field");
		assertNotNull(target);
		assertEquals(target.ident().getColumnNo(), target.literalThis().getColumnNo() + 6);
	}

	@Test
	public void spaceBeforeDotClassifies() throws Exception {
		final var target = locateColumn("spaceBeforeDot", " .field", " .field", 1);
		assertNotNull(target);
		assertEquals(target.ident().getColumnNo(), target.literalThis().getColumnNo() + 6);
	}

	@Test
	public void spacedDotClassifies() throws Exception {
		final var target = locateColumn("spacedDot", " . field", " . field", 1);
		assertNotNull(target);
		assertEquals(target.ident().getColumnNo(), target.literalThis().getColumnNo() + 7);
	}

	/**
	 * The column is a code-point column, so a supplementary char ahead of the dot shifts it away
	 * from the char index the same text would give.
	 */
	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryAhead", ".text");
		final var text = lines.get(line);
		final var charIndex = text.indexOf(".text");
		final var codePointColumn = text.codePointCount(0, charIndex);
		assertNotEquals(charIndex, codePointColumn);
		assertNotNull(NoUnnecessaryThisCheck.thisAt(parseFixture(), line, codePointColumn));
		assertNull(NoUnnecessaryThisCheck.thisAt(parseFixture(), line, charIndex));
	}

	@Test
	public void tightDotClassifies() throws Exception {
		final var target = locate("tightDot", ".field");
		assertNotNull(target);
		assertEquals(TokenTypes.LITERAL_THIS, target.literalThis().getType());
		assertEquals(TokenTypes.IDENT, target.ident().getType());
		assertEquals("field", target.ident().getText());
	}

	@Test
	public void twoThisOnOneLineClassifyIndependently() throws Exception {
		final var first = locateColumn("twoThisOnOneLine", ".alpha", ".alpha", 0);
		final var second = locateColumn("twoThisOnOneLine", ".beta", ".beta", 0);
		assertNotNull(first);
		assertNotNull(second);
		assertEquals("alpha", first.ident().getText());
		assertEquals("beta", second.ident().getText());
		assertNotEquals(first.literalThis().getColumnNo(), second.literalThis().getColumnNo());
	}

	/** A type witness puts {@code TYPE_ARGUMENTS} where the member name would be. */
	@Test
	public void typeWitnessFindsNothing() throws Exception {
		assertNull(locate("typeWitness", ".<X>pick"));
	}
}