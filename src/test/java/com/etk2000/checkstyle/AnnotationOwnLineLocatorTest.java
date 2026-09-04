package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.etk2000.checkstyle.AnnotationOwnLineCheck.AnnotationLayout;
import com.etk2000.checkstyle.AnnotationOwnLineCheck.AnnotationLayoutTarget;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link AnnotationOwnLineCheck#layoutAt}, the seam
 * {@code AnnotationOwnLineFixer} dispatches on. Two of the four layouts are logged away from the
 * annotation's own token, so the rows that matter most are the ones asserting that a position
 * holding no annotation still resolves to the right holder, and that a position the check never
 * reports classifies as nothing rather than handing back a neighbouring group.
 */
public class AnnotationOwnLineLocatorTest {
	private static final String FIXTURE = "annotationownline/InputOwnLineLocatorPositions.java";

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
	 * The first 0-based line from {@code member}'s declaration on carrying {@code needle}. Anchoring
	 * inside one member keeps a needle such as {@code @LocatorA}, which most rows share, pointing at
	 * the one the row is about.
	 */
	@CheckReturnValue
	private static int lineIn(@Nonnull List<String> lines, @Nonnull String member, @Nonnull String needle) {
		for (var i = lineWith(lines, member); i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] under [" + member + "] in " + FIXTURE);
	}

	/**
	 * The first 0-based line at or above {@code member}'s declaration carrying {@code needle}. The
	 * group of a stacked declaration is written above it, so a row about the group searches upwards.
	 */
	@CheckReturnValue
	private static int lineOver(@Nonnull List<String> lines, @Nonnull String member, @Nonnull String needle) {
		for (var i = lineWith(lines, member); i >= 0; --i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] over [" + member + "] in " + FIXTURE);
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

	/** What the check classifies where {@code needle} begins on the line above {@code member}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationLayoutTarget locate(@Nonnull String member, @Nonnull String needle) throws Exception {
		return locateOffset(member, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out above {@code member}, at an
	 * absolute {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationLayoutTarget locateAbsolute(
			@Nonnull String member, @Nonnull String lineNeedle, int column
	) throws Exception {
		final var lines = fixtureLines();
		return AnnotationOwnLineCheck.layoutAt(
				parseFixture(), lines, lineOver(lines, member, lineNeedle), column
		);
	}

	@CheckReturnValue
	@Nullable
	private static AnnotationLayoutTarget locateLine(int line, int column) throws Exception {
		final var lines = fixtureLines();
		return AnnotationOwnLineCheck.layoutAt(parseFixture(), lines, line, column);
	}

	/** {@link #locate} shifted by {@code offset} code points. */
	@CheckReturnValue
	@Nullable
	private static AnnotationLayoutTarget locateOffset(
			@Nonnull String member, @Nonnull String needle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineOver(lines, member, needle);
		return AnnotationOwnLineCheck.layoutAt(
				parseFixture(), lines, line, columnOf(lines.get(line), needle) + offset
		);
	}

	/** What the check classifies where {@code needle} begins on a line inside {@code member}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationLayoutTarget locateUnder(
			@Nonnull String member, @Nonnull String needle
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, member, needle);
		return AnnotationOwnLineCheck.layoutAt(
				parseFixture(), lines, line, columnOf(lines.get(line), needle)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> namesOf(@Nonnull AnnotationLayoutTarget target) {
		final var names = new ArrayList<String>(target.annotations().size());
		for (var annotation : target.annotations())
			names.add(AstText.annotationName(annotation));
		return names;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		final var file = new File(resource().toURI());
		return JavaParser.parseFileText(
				new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

	/** The length of the line {@code target} is reported on, which its column is clamped to. */
	@CheckReturnValue
	private static int reportedLineLength(@Nonnull AnnotationLayoutTarget target) throws Exception {
		return fixtureLines().get(target.reportedLine() - 1).length();
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource() {
		return requireNonNull(
				AnnotationOwnLineLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void anchorIsTheDeclarationLine() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("void stackedOutOfOrder()", "@LocatorA");
		assertNotNull(target);
		// AST lines are 1-based, the fixture index is not
		assertEquals(lineWith(lines, "void stackedOutOfOrder()") + 1, target.anchor().getLineNo());
	}

	@Test
	public void annotationsListsEveryMemberInSourceOrder() throws Exception {
		final var target = locate("void stackedOutOfOrder()", "@LocatorA");
		assertNotNull(target);
		assertEquals(List.of("LocatorC", "LocatorA", "LocatorB"), namesOf(target));
	}

	@Test
	public void blankAfterColumnPastTheLineEndStillResolves() throws Exception {
		final var lines = fixtureLines();
		// the annotation is indented past the end of its own last line, so the check cannot report
		// at its column: reading that line up to it is what expands the tabs ahead of a logged column
		final var reported = lineOver(lines, "void blankAfterColumnPastEnd()", "1)");
		final var target = locateLine(reported, lines.get(reported).length());
		assertNotNull(target);
		assertEquals(AnnotationLayout.BLANK_AFTER, target.layout());
		assertEquals("LocatorNum", target.targetName());
		// clamped to the line it is reported on, which is shorter than the annotation's own column
		assertEquals(reportedLineLength(target), target.reportedColumn());
	}

	@Test
	public void blankAfterNestedAnnotationDecoyResolvesToTheHolder() throws Exception {
		final var target = locate("void blankAfterNestedDecoy()", "@LocatorA)");
		assertNotNull(target);
		assertEquals(AnnotationLayout.BLANK_AFTER, target.layout());
		// a real ANNOTATION node sits exactly here; the holder is the one the check reported
		assertEquals("LocatorNest", target.targetName());
	}

	@Test
	public void blankAfterOnAMultiLineAnnotationResolvesFromItsLastLine() throws Exception {
		final var target = locate("void blankAfterMultiLine()", "@LocatorNum(");
		assertNull(target, "the group's first line is not where a BLANK_AFTER is reported");

		final var resolved = locateAbsolute("void blankAfterMultiLine()", "1)", 1);
		assertNotNull(resolved);
		assertEquals(AnnotationLayout.BLANK_AFTER, resolved.layout());
		assertEquals("LocatorNum", resolved.targetName());
	}

	@Test
	public void blankAfterSimpleClassifiesAtTheAnnotation() throws Exception {
		final var target = locate("void blankAfterSimple()", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationLayout.BLANK_AFTER, target.layout());
		assertEquals("LocatorA", target.targetName());
		assertNull(target.previousName());
	}

	@Test
	public void catchParameterFindsNothing() throws Exception {
		assertNull(locateUnder("void catchParameter()", "@LocatorA"));
	}

	@Test
	public void columnOneLeftFindsNothing() throws Exception {
		assertNull(locateOffset("void stackedOutOfOrder()", "@LocatorA", -1));
	}

	@Test
	public void columnOneRightFindsNothing() throws Exception {
		assertNull(locateOffset("void stackedOutOfOrder()", "@LocatorA", 1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("void stackedOutOfOrder()", "@LocatorA", 4096));
	}

	@Test
	public void decoyInsideACommentFindsNothing() throws Exception {
		assertNull(locateUnder("void decoys()", "// @LocatorB"));
	}

	@Test
	public void decoyInsideAStringFindsNothing() throws Exception {
		assertNull(locateUnder("void decoys()", "\"@LocatorB"));
	}

	@Test
	public void decoyInsideATextBlockFindsNothing() throws Exception {
		assertNull(locateUnder("void decoys()", "@LocatorB decoy"));
	}

	@Test
	public void enumConstantClassifies() throws Exception {
		final var target = locateUnder("enum Constants", "@LocatorB");
		assertNotNull(target);
		assertEquals(AnnotationLayout.OWN_LINE, target.layout());
		assertEquals("LocatorB", target.targetName());
	}

	@Test
	public void forEachAndForInitVariablesFindNothing() throws Exception {
		assertNull(locateUnder("void forEachAndForInit(", "@LocatorA var row"));
		assertNull(locateUnder("void forEachAndForInit(", "@LocatorA var i"));
	}

	@Test
	public void holderIsTheGroupTheAnnotationBelongsTo() throws Exception {
		final var target = locate("void stackedOutOfOrder()", "@LocatorA");
		assertNotNull(target);
		assertSame(target.reported().getParent(), target.holder());
	}

	@Test
	public void internalBlankClassifiesAtColumnZero() throws Exception {
		final var lines = fixtureLines();
		final var blank = lineOver(lines, "void internalBlank()", "@LocatorNum({") + 2;
		assertEquals("", lines.get(blank).strip(), "the fixture's blank line moved");

		final var target = locateLine(blank, 0);
		assertNotNull(target);
		assertEquals(AnnotationLayout.BLANK_INTERNAL, target.layout());
		assertEquals("LocatorNum", target.targetName());
		assertEquals(blank + 1, target.blankLine());
	}

	@Test
	public void internalBlankColumnOneRightFindsNothing() throws Exception {
		final var lines = fixtureLines();
		assertNull(locateLine(lineOver(lines, "void internalBlank()", "@LocatorNum({") + 2, 1));
	}

	@Test
	public void internalBlankInsideATextBlockFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var blank = lineOver(lines, "void internalBlankInTextBlock()", "@LocatorVal(\"\"\"") + 2;
		assertEquals("", lines.get(blank).strip(), "the fixture's blank line moved");
		assertNull(locateLine(blank, 0));
	}

	@Test
	public void lambdaParameterFindsNothing() throws Exception {
		assertNull(locateUnder("void lambdaParameter()", "@LocatorA"));
	}

	@Test
	public void lineBeyondTheBufferFindsNothing() throws Exception {
		assertNull(locateLine(fixtureLines().size() + 10, 0));
	}

	@Test
	public void methodParameterFindsNothing() throws Exception {
		assertNull(locateUnder("void methodParameter(", "@LocatorA"));
	}

	@Test
	public void nestedAnnotationValueFindsNothing() throws Exception {
		assertNull(locateUnder("void nestedAnnotationValue(", "@LocatorA"));
	}

	@Test
	public void orderCarriesThePreviousName() throws Exception {
		final var target = locate("void stackedOutOfOrder()", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationLayout.ALPHABETICAL_ORDER, target.layout());
		assertEquals("LocatorA", target.targetName());
		assertEquals("LocatorC", target.previousName());
	}

	/**
	 * The subsumption rule: re-emitting the group spans the gap after it, so an answer whose fix is
	 * a re-emission replaces a {@link AnnotationLayout#BLANK_AFTER} found first at the same position.
	 * A single-line annotation reports its blank-after at its own line and column, which is exactly
	 * where its order violation is reported, so the two coincide.
	 */
	@Test
	public void orderSubsumesTheBlankAfterAtTheSamePosition() throws Exception {
		final var target = locate("void orderThenBlankAfter()", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationLayout.ALPHABETICAL_ORDER, target.layout());
		assertEquals("LocatorA", target.targetName());
		assertEquals("LocatorB", target.previousName());
	}

	@Test
	public void ownLineClassifiesAtTheFirstAnnotationOfASharedLine() throws Exception {
		final var target = locate("void bothOnOneLine()", "@LocatorB");
		assertNotNull(target);
		assertEquals(AnnotationLayout.OWN_LINE, target.layout());
		assertEquals("LocatorB", target.targetName());
	}

	@Test
	public void patternVariableFindsNothing() throws Exception {
		assertNull(locateUnder("void patternVariable(", "@LocatorA"));
	}

	@Test
	public void receiverParameterFindsNothing() throws Exception {
		assertNull(locateUnder("void receiverParameter(", "@LocatorA"));
	}

	@Test
	public void recordComponentFindsNothing() throws Exception {
		assertNull(locateUnder("Component(", "@LocatorA"));
	}

	@Test
	public void splitQualifiedTypeReportsTheAnnotationSharingItsLine() throws Exception {
		final var target = locate("splitQualifiedType()", "@LocatorA java.util");
		assertNotNull(target);
		assertEquals(AnnotationLayout.OWN_LINE, target.layout());
		assertEquals("LocatorA", target.targetName());
	}

	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOver(lines, "void supplementaryNameAhead()", "@LocatorA");
		final var charIndex = lines.get(line).indexOf("@LocatorA");
		final var column = columnOf(lines.get(line), "@LocatorA");
		assertNotEquals(charIndex, column, "fixture lost its supplementary character");

		assertNotNull(locateLine(line, column));
		assertNull(locateLine(line, charIndex), "a char index is not a code-point column");
	}

	@Test
	public void tryWithResourcesVariableFindsNothing() throws Exception {
		assertNull(locateUnder("void tryWithResources()", "@LocatorA"));
	}

	@Test
	public void typeUsePositionsFindNothing() throws Exception {
		assertNull(locateUnder("void typeUse(", "@LocatorA T>"));
		assertNull(locateUnder("void typeUse(", "@LocatorA String>"));
		assertNull(locateUnder("void typeUse(", "@LocatorA []"));
	}
}