package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.etk2000.checkstyle.AnnotationSameLineCheck.AnnotationPlacement;
import com.etk2000.checkstyle.AnnotationSameLineCheck.AnnotationPlacementTarget;
import com.etk2000.checkstyle.ast.AstText;
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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link AnnotationSameLineCheck#placementAt}, the seam
 * {@code AnnotationSameLineFixer} dispatches on. The fixer rewrites a span bounded by the
 * descriptor's own nodes, so the rows that matter most are the ones asserting which nodes the
 * descriptor carries, and the ones asserting that a position the check never reports classifies as
 * nothing rather than handing back a neighbouring group.
 */
public class AnnotationSameLineLocatorTest {
	private static final String FIXTURE = "annotationsameline/InputLocatorPositions.java";

	/** An AST column counts code points, so a char index into the line is converted. */
	@CheckReturnValue
	private static int columnOf(@Nonnull String line, @Nonnull String needle) {
		final var index = line.indexOf(needle);
		if (index < 0)
			throw new IllegalArgumentException("no [" + needle + "] in [" + line + "]");
		return line.codePointCount(0, index);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST firstOfType(@Nonnull DetailAST node, int tokenType) {
		if (node.getType() == tokenType)
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = firstOfType(child, tokenType);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return Files.readAllLines(Path.of(resource().toURI()), StandardCharsets.UTF_8);
	}

	/**
	 * The first 0-based line from {@code member}'s declaration on carrying {@code needle}. Anchoring
	 * inside one member keeps a needle such as {@code @LocatorA}, which most rows share, pointing at
	 * the one the test is about. The search starts on the declaration itself, since an inline group
	 * is written there.
	 */
	@CheckReturnValue
	private static int lineIn(@Nonnull List<String> lines, @Nonnull String member, @Nonnull String needle) {
		for (var i = lineWith(lines, member); i < lines.size(); ++i) {
			if (lines.get(i).contains(needle))
				return i;
		}
		throw new IllegalArgumentException("no [" + needle + "] under [" + member + "] in " + FIXTURE);
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

	/** What the check classifies where {@code needle} begins, inside {@code member}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationPlacementTarget locate(@Nonnull String member, @Nonnull String needle) throws Exception {
		return locateColumn(member, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationPlacementTarget locateAbsolute(
			@Nonnull String member, @Nonnull String lineNeedle, int column
	) throws Exception {
		return AnnotationSameLineCheck.placementAt(
				parseFixture(), lineIn(fixtureLines(), member, lineNeedle), column
		);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static AnnotationPlacementTarget locateColumn(
			@Nonnull String member, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, member, lineNeedle);
		return AnnotationSameLineCheck.placementAt(
				parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	/** What the check classifies at {@code column} on the 0-based {@code line}. */
	@CheckReturnValue
	@Nullable
	private static AnnotationPlacementTarget locateLine(int line, int column) throws Exception {
		return AnnotationSameLineCheck.placementAt(parseFixture(), line, column);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST methodNamed(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() == TokenTypes.METHOD_DEF) {
			final var ident = node.findFirstToken(TokenTypes.IDENT);
			if (ident != null && name.equals(ident.getText()))
				return node;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = methodNamed(child, name);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> namesOf(@Nonnull AnnotationPlacementTarget target) {
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

	@CheckReturnValue
	@Nonnull
	private static URL resource() {
		return requireNonNull(
				AnnotationSameLineLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void anchorIsTheDeclarationLine() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("void exactColumnSameLine(", "@LocatorA");
		assertNotNull(target);
		// AST lines are 1-based, the fixture index is not
		assertEquals(
				lineIn(lines, "void exactColumnSameLine(", "String param") + 1,
				target.anchor().getLineNo()
		);
	}

	@Test
	public void annotationsListsEveryMemberInSourceOrder() throws Exception {
		final var target = locate("void siblingInOrder(", "@LocatorA");
		assertNotNull(target);
		assertEquals(List.of("LocatorC", "LocatorA", "LocatorB"), namesOf(target));
	}

	@Test
	public void arrayLevelAnnotationFindsNothing() throws Exception {
		assertNull(locate("void arrayLevelAnnotation(", "@LocatorA"));
	}

	@Test
	public void catchParameterClassifies() throws Exception {
		final var target = locate("void catchParameter(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	@Test
	public void columnOneLeftFindsNothing() throws Exception {
		assertNull(locateColumn("void exactColumnOrder(", "@LocatorA", "@LocatorA", -1));
	}

	@Test
	public void columnOneRightFindsNothing() throws Exception {
		assertNull(locateColumn("void exactColumnOrder(", "@LocatorA", "@LocatorA", 1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("void exactColumnOrder(", "@LocatorA", 4096));
	}

	@Test
	public void commentAfterAtClassifies() throws Exception {
		final var target = locate("void commentAfterAt(", "@LocatorA");
		assertNotNull(target);
		assertEquals("LocatorA", target.targetName());
		assertEquals("LocatorB", target.previousName());
	}

	@Test
	public void constructorParameterClassifies() throws Exception {
		final var target = locate("class ConstructorParameter", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	@Test
	public void decoyInsideCommentFindsNothing() throws Exception {
		assertNull(locate("void decoyInComment(", "@LocatorA"));
	}

	@Test
	public void decoyInsideStringFindsNothing() throws Exception {
		assertNull(locate("void decoyInString(", "@LocatorA"));
	}

	@Test
	public void decoyInsideTextBlockFindsNothing() throws Exception {
		assertNull(locate("void decoyInTextBlock(", "@LocatorA"));
	}

	@Test
	public void enumConstantAnnotationFindsNothing() throws Exception {
		assertNull(locate("enum EnumConstant", "@LocatorA"));
	}

	@Test
	public void exactColumnOrderClassifies() throws Exception {
		final var target = locate("void exactColumnOrder(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.ALPHABETICAL_ORDER, target.placement());
		assertEquals("LocatorA", target.targetName());
		assertEquals("LocatorB", target.previousName());
	}

	@Test
	public void exactColumnSameLineClassifies() throws Exception {
		final var target = locate("void exactColumnSameLine(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
		assertEquals("LocatorA", target.targetName());
		assertNull(target.previousName());
	}

	@Test
	public void fieldAnnotationFindsNothing() throws Exception {
		assertNull(locate("int fieldAnnotation;", "@LocatorA"));
	}

	@Test
	public void forEachVariableClassifies() throws Exception {
		final var target = locate("void forEachVariable(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	@Test
	public void forInitVariableClassifies() throws Exception {
		final var target = locate("void forInitVariable(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	@Test
	public void lambdaParameterClassifies() throws Exception {
		final var target = locate("void lambdaParameter(", "@LocatorA");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "void exactColumnOrder(", "@LocatorA");
		assertNull(locateLine(line - 1, columnOf(lines.get(line), "@LocatorA")));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "void exactColumnOrder(", "@LocatorA");
		assertNull(locateLine(line + 1, columnOf(lines.get(line), "@LocatorA")));
	}

	@Test
	public void lineBeyondBufferFindsNothing() throws Exception {
		assertNull(locateLine(fixtureLines().size() + 10, 0));
	}

	@Test
	public void localVariableAnnotationFindsNothing() throws Exception {
		assertNull(locate("void localVariableAnnotation(", "@LocatorA"));
	}

	@Test
	public void methodAnnotationFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, "void methodAnnotation(") - 1;
		assertNull(locateLine(line, columnOf(lines.get(line), "@LocatorA")));
	}

	@Test
	public void negativeColumnFindsNothing() throws Exception {
		assertNull(locateAbsolute("void exactColumnOrder(", "@LocatorA", -1));
	}

	@Test
	public void nestedAnnotationValueFindsNothing() throws Exception {
		assertNull(locateColumn("void nestedAnnotationValue(", "@LocatorNest", "@LocatorA", 0));
	}

	@Test
	public void parameterOwnerIsModifiers() throws Exception {
		final var target = locate("void exactColumnSameLine(", "@LocatorA");
		assertNotNull(target);
		assertEquals(TokenTypes.MODIFIERS, target.owner().getType());
	}

	@Test
	public void recordComponentOwnerIsAnnotations() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("RecordComponent(", "@LocatorA");
		assertNotNull(target);
		assertEquals(TokenTypes.ANNOTATIONS, target.owner().getType());
		assertEquals(
				lineIn(lines, "RecordComponent(", "String name") + 1,
				target.anchor().getLineNo()
		);
	}

	@Test
	public void resourceAnnotationFindsNothing() throws Exception {
		assertNull(locate("void resourceAnnotation(", "@LocatorA"));
	}

	@Test
	public void siblingInOrderAnnotationFindsNothing() throws Exception {
		assertNull(locateColumn("void siblingInOrder(", "@LocatorA", "@LocatorB", 0));
	}

	@Test
	public void spaceAfterAtClassifies() throws Exception {
		final var target = locate("void spaceAfterAt(", "@LocatorA");
		assertNotNull(target);
		assertEquals("LocatorA", target.targetName());
		assertEquals("LocatorB", target.previousName());
	}

	@Test
	public void spanningAnnotationReportsSameLineNotOrder() throws Exception {
		final var target = locate("void spanningAnnotation(", "@LocatorNum(");
		assertNotNull(target);
		assertEquals(AnnotationPlacement.SAME_LINE, target.placement());
	}

	/**
	 * The branch {@code startToken} was extracted for. A {@code TYPE} over a qualified name is
	 * positioned on its outer dot, so its own line is later than the line its text starts on, and an
	 * anchor taken from the node rather than from this token reports a violation no fix can clear.
	 */
	@Test
	public void startTokenCrossesToTheEarlierLine() throws Exception {
		final var method = requireNonNull(methodNamed(parseFixture(), "splitQualifiedType"));
		final var parameter = requireNonNull(firstOfType(method, TokenTypes.PARAMETER_DEF));
		final var type = requireNonNull(parameter.findFirstToken(TokenTypes.TYPE));
		final var first = AstSpan.startToken(type);
		assertEquals("java", first.getText());
		assertEquals(type.getLineNo() - 1, first.getLineNo());
	}

	@Test
	public void startTokenOnALeafIsTheLeafItself() throws Exception {
		final var method = requireNonNull(methodNamed(parseFixture(), "splitQualifiedType"));
		final var parameter = requireNonNull(firstOfType(method, TokenTypes.PARAMETER_DEF));
		final var ident = requireNonNull(parameter.findFirstToken(TokenTypes.IDENT));
		assertSame(ident, AstSpan.startToken(ident));
	}

	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "void supplementaryAhead(", "@LocatorA");
		assertNotNull(locateLine(line, columnOf(lines.get(line), "@LocatorA")));
		assertNull(locateLine(line, lines.get(line).indexOf("@LocatorA")));
	}

	@Test
	public void twoGroupsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("void twoGroupsOnOneLine(", "@LocatorA");
		final var second = locateColumn("void twoGroupsOnOneLine(", "@LocatorA", "@LocatorC", 0);
		assertNotNull(first);
		assertNotNull(second);
		assertNotSame(first.owner(), second.owner());
		assertEquals(List.of("LocatorB", "LocatorA"), namesOf(first));
		assertEquals(List.of("LocatorÄ", "LocatorC"), namesOf(second));
	}

	@Test
	public void typeParameterAnnotationFindsNothing() throws Exception {
		assertNull(locateColumn("void typeParameterAnnotation(", "@LocatorA", "@LocatorA", 0));
	}

	@Test
	public void typeUseAnnotationFindsNothing() throws Exception {
		assertNull(locate("void typeUseAnnotation(", "@LocatorA"));
	}

	@Test
	public void unannotatedParameterFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, "void unannotatedParameter(");
		assertNull(locateLine(line, columnOf(lines.get(line), "String param")));
	}
}