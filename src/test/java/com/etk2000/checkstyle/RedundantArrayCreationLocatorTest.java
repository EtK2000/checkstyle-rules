package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.RedundantArrayCreationCheck.ArrayArgTarget;
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
 * Direct tests for {@link RedundantArrayCreationCheck#redundantArrayAt}, the seam
 * {@code RedundantArrayCreationFixer} dispatches on. The fixer deletes the array wrapper and
 * re-emits the elements, so the rows that matter most are the ones asserting a position does
 * <em>not</em> classify: a creation that is not a varargs argument must find nothing rather than
 * have its braces spliced into text the check never looked at.
 */
public class RedundantArrayCreationLocatorTest {
	private static final String FIXTURE = "redundantarraycreation/InputLocatorPositions.java";

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
	 * inside one method keeps a needle such as {@code new}, which every row shares, pointing at the
	 * one the test is about.
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
	private static ArrayArgTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static ArrayArgTarget locateAbsolute(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return RedundantArrayCreationCheck.redundantArrayAt(
				parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column
		);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static ArrayArgTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return RedundantArrayCreationCheck.redundantArrayAt(
				parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	/** What the check classifies at {@code column} on the 0-based {@code line}. */
	@CheckReturnValue
	@Nullable
	private static ArrayArgTarget locateLine(int line, int column) throws Exception {
		return RedundantArrayCreationCheck.redundantArrayAt(parseFixture(), line, column);
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
				RedundantArrayCreationLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	/**
	 * The whole empty-initializer branch rests on these two positions: the {@code ARRAY_INIT} node
	 * stands on its own {@code &#123;} and its {@code RCURLY} child on the {@code &#125;}, so the
	 * text between them is exactly what was written inside the braces.
	 */
	@Test
	public void arrayInitPositionsAreTheBraces() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumn", "new Object[]");
		final var target = locate("exactColumn", "new Object[]");
		assertNotNull(target);
		final var text = lines.get(line);
		assertEquals(columnOf(text, "{"), target.arrayInit().getColumnNo());
		assertEquals(columnOf(text, "}"), target.arrayInit().findFirstToken(TokenTypes.RCURLY).getColumnNo());
	}

	@Test
	public void castWrappedCreationFindsNothing() throws Exception {
		assertNull(locate("castWrappedCreation", "new String[]"));
	}

	@Test
	public void chainedReceiverFindsNothing() throws Exception {
		assertNull(locate("chainedReceiver", "new Object[]"));
	}

	@Test
	public void columnOneLeftFindsNothing() throws Exception {
		assertNull(locateColumn("exactColumn", "new Object[]", "new Object[]", -1));
	}

	@Test
	public void columnOneRightFindsNothing() throws Exception {
		assertNull(locateColumn("exactColumn", "new Object[]", "new Object[]", 1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("exactColumn", "new Object[]", 4096));
	}

	/**
	 * Only the last argument can be the varargs one, so a creation earlier in the same argument
	 * list answers null. Without the identity check in {@code redundantArrayAt} the locator would
	 * hand back the <em>last</em> creation's target for this column, and the fixer would delete an
	 * array the caller never addressed.
	 */
	@Test
	public void earlierCreationInTheSameArgumentListFindsNothing() throws Exception {
		assertNull(locate("twoCreationsInOneArgumentList", "new Object[]"));
	}

	@Test
	public void exactColumnClassifies() throws Exception {
		final var target = locate("exactColumn", "new Object[]");
		assertNotNull(target);
		assertEquals("asList", target.targetName());
	}

	@Test
	public void explicitSizeCreationFindsNothing() throws Exception {
		assertNull(locate("explicitSizeCreation", "new Object[5]"));
	}

	@Test
	public void lastCreationInTheSameArgumentListClassifies() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "twoCreationsInOneArgumentList", "new Object[]");
		final var text = lines.get(line);
		final var second = text.indexOf("new Object[]", text.indexOf("new Object[]") + 1);
		final var target = locateLine(line, text.codePointCount(0, second));
		assertNotNull(target);
		assertEquals("format", target.targetName());
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumn", "new Object[]");
		assertNull(locateLine(line - 1, columnOf(lines.get(line), "new Object[]")));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "exactColumn", "new Object[]");
		assertNull(locateLine(line + 1, columnOf(lines.get(line), "new Object[]")));
	}

	@Test
	public void lineInAnEmptyBufferFindsNothing() throws Exception {
		assertNull(locateLine(fixtureLines().size() + 10, 0));
	}

	@Test
	public void negativeColumnFindsNothing() throws Exception {
		assertNull(locateAbsolute("exactColumn", "new Object[]", -1));
	}

	@Test
	public void nestedArrayCreationInsideAnElementFindsNothing() throws Exception {
		assertNull(locateColumn("nestedCreationInsideAnElement", "new Object[]{new", "new Object[]{new", 13));
	}

	@Test
	public void nestedOwnerCallsClassifyIndependently() throws Exception {
		final var outer = locate("nestedOwnerCalls", "new Object[]");
		final var inner = locate("nestedOwnerCalls", "new CharSequence[]");
		assertNotNull(outer);
		assertNotNull(inner);
		assertEquals("asList", outer.targetName());
		assertEquals("join", inner.targetName());
		assertNotEquals(outer.literalNew(), inner.literalNew());
	}

	@Test
	public void newThatIsNotAnArrayCreationFindsNothing() throws Exception {
		assertNull(locate("chainedReceiver", "new StringBuilder"));
	}

	@Test
	public void nonVarargsOverloadFindsNothing() throws Exception {
		assertNull(locate("nonVarargsOverload", "new Object[]"));
	}

	@Test
	public void notAnArgumentFindsNothing() throws Exception {
		assertNull(locate("notAnArgument", "new String[]"));
	}

	@Test
	public void notTheLastArgumentFindsNothing() throws Exception {
		assertNull(locate("notTheLastArgument", "new Object[]"));
	}

	@Test
	public void primitiveArrayToPrimitiveVarargsClassifies() throws Exception {
		assertNotNull(locate("primitiveToPrimitiveVarargs", "new int[]"));
	}

	@Test
	public void primitiveArrayToReferenceVarargsFindsNothing() throws Exception {
		assertNull(locate("primitiveToReferenceVarargs", "new int[]"));
	}

	/**
	 * The column is counted in code points, so the astral character ahead of the call shifts it one
	 * below the char index. Looking the creation up at the char index finds nothing, which is what
	 * makes the conversion load-bearing rather than cosmetic.
	 */
	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryAhead", "new CharSequence[]");
		final var text = lines.get(line);
		assertNotNull(locateLine(line, text.codePointCount(0, text.indexOf("new CharSequence[]"))));
		assertNull(locateLine(line, text.indexOf("new CharSequence[]")));
	}

	@Test
	public void twoCreationsOnOneLineClassifyIndependently() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "twoCreationsOnOneLine", "new CharSequence[]");
		final var text = lines.get(line);
		final var second = text.indexOf("new CharSequence[]", text.indexOf("new CharSequence[]") + 1);
		final var first = locateLine(line, text.codePointCount(0, text.indexOf("new CharSequence[]")));
		final var other = locateLine(line, text.codePointCount(0, second));
		assertNotNull(first);
		assertNotNull(other);
		assertEquals("join", first.targetName());
		assertEquals("join", other.targetName());
		assertNotEquals(first.literalNew(), other.literalNew());
	}

	@Test
	public void unqualifiedCallFindsNothing() throws Exception {
		assertNull(locate("unqualifiedCall", "new String[]"));
	}

	@Test
	public void variableReceiverClassifies() throws Exception {
		final var target = locate("variableReceiver", "new Object[]");
		assertNotNull(target);
		assertEquals("format", target.targetName());
	}
}