package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferStaticImportCheck.StaticCallTarget;
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
 * Direct tests for {@link PreferStaticImportCheck#staticCallAt}, the seam
 * {@code PreferStaticImportFixer} dispatches on. The fixer deletes everything from the receiver up
 * to the method name, so the rows that matter most are the ones asserting a position does
 * <em>not</em> classify: a receiver that is only part of a longer name would have the rest of that
 * name deleted with it.
 */
public class PreferStaticImportLocatorTest {
	private static final String FIXTURE = "preferstaticimport/InputLocatorPositions.java";
	private static final String OBJECTS = "java.util.Objects";

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
	 * inside one method keeps a needle such as {@code Objects.isNull}, which most of them share,
	 * pointing at the one the test is about.
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
	private static StaticCallTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static StaticCallTarget locateAbsolute(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return PreferStaticImportCheck.staticCallAt(
				parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column
		);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static StaticCallTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return PreferStaticImportCheck.staticCallAt(
				parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	/**
	 * What the check classifies at the right column but {@code lineOffset} lines away, which is what
	 * a buffer edited above the violation hands the fixer.
	 */
	@CheckReturnValue
	@Nullable
	private static StaticCallTarget locateLine(
			@Nonnull String method, @Nonnull String needle, int lineOffset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, needle);
		return PreferStaticImportCheck.staticCallAt(
				parseFixture(), line + lineOffset, columnOf(lines.get(line), needle)
		);
	}

	/** What the check classifies where {@code columnNeedle} begins on a line outside any method. */
	@CheckReturnValue
	@Nullable
	private static StaticCallTarget locateTopLevel(
			@Nonnull String lineNeedle, @Nonnull String columnNeedle
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, lineNeedle);
		return PreferStaticImportCheck.staticCallAt(parseFixture(), line, columnOf(lines.get(line), columnNeedle));
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
				PreferStaticImportLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void arrayAccessReceiverFindsNothing() throws Exception {
		assertNull(locate("arrayAccessReceiver", "holders[0]"));
	}

	@Test
	public void blockCommentAfterDotClassifies() throws Exception {
		final var target = locate("blockCommentAfterDot", "Objects./* c */isNull");
		assertNotNull(target);
		assertEquals("isNull", target.simpleMethod());
	}

	@Test
	public void blockCommentBeforeDotClassifies() throws Exception {
		final var target = locate("blockCommentBeforeDot", "Objects/* c */.isNull");
		assertNotNull(target);
		assertEquals("isNull", target.simpleMethod());
	}

	@Test
	public void callReceiverFindsNothing() throws Exception {
		assertNull(locate("callReceiver", "supplier()"));
	}

	@Test
	public void classLiteralReceiverFindsNothing() throws Exception {
		assertNull(locate("classLiteralReceiver", "Objects.class"));
	}

	@Test
	public void columnInsideTheReceiverFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", "Objects.isNull", "Objects.isNull", 1));
	}

	@Test
	public void columnOneBeforeTheReceiverFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", "Objects.isNull", "Objects.isNull", -1));
	}

	@Test
	public void columnOnTheDotFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", "Objects.isNull", "Objects.isNull", 7));
	}

	@Test
	public void columnOnTheMethodIdentFindsNothing() throws Exception {
		assertNull(locateColumn("tightDot", "Objects.isNull", "Objects.isNull", 8));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("tightDot", "Objects.isNull", 4096));
	}

	@Test
	public void dotOnTheNextLineClassifies() throws Exception {
		final var target = locate("dotOnTheNextLine", "Objects");
		assertNotNull(target);
		assertEquals(target.receiver().getLineNo() + 1, target.methodIdent().getLineNo());
	}

	/**
	 * The outer name of a qualified receiver. Classifying here would delete {@code java.util.} and
	 * leave {@code Objects.isNull(text)} behind, which still compiles and still reports.
	 */
	@Test
	public void fullyQualifiedOuterIdentFindsNothing() throws Exception {
		assertNull(locate("fullyQualifiedReceiver", OBJECTS));
	}

	/**
	 * The candidate's own simple name inside a qualified receiver. This is the highest mangle risk in
	 * the classifier: a walk that took the last IDENT of the dot chain would emit
	 * {@code java.util.isNull(text)}.
	 */
	@Test
	public void fullyQualifiedReceiverFindsNothing() throws Exception {
		assertNull(locateColumn("fullyQualifiedReceiver", OBJECTS, "Objects.isNull", 0));
	}

	@Test
	public void importDeclarationFindsNothing() throws Exception {
		assertNull(locateTopLevel("function.Predicate;", "Predicate;"));
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		assertNull(locateLine("tightDot", "Objects.isNull", -1));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		assertNull(locateLine("tightDot", "Objects.isNull", 1));
	}

	@Test
	public void methodOnTheNextLineClassifies() throws Exception {
		final var target = locate("methodOnTheNextLine", "Objects.");
		assertNotNull(target);
		assertEquals(target.receiver().getLineNo() + 1, target.methodIdent().getLineNo());
	}

	@Test
	public void methodReferenceFindsNothing() throws Exception {
		assertNull(locate("methodReference", "Objects::isNull"));
	}

	/**
	 * {@code FixerAst.locate} clamps a column below zero to the line's first, so this also records
	 * what a negative column does once clamped: the fixture's receivers are all indented.
	 */
	@Test
	public void negativeColumnFindsNothing() throws Exception {
		assertNull(locateAbsolute("tightDot", "Objects.isNull", -1));
	}

	@Test
	public void nestedCallsClassifyIndependently() throws Exception {
		final var outer = locate("nestedCalls", "Objects.requireNonNull(Objects");
		final var inner = locate("nestedCalls", "Objects.requireNonNull(text)");
		assertNotNull(outer);
		assertNotNull(inner);
		assertNotEquals(outer.receiver().getColumnNo(), inner.receiver().getColumnNo());
	}

	/** {@code hash} is not in the candidate table, so the call has no static import to be rewritten to. */
	@Test
	public void nonCandidateMethodFindsNothing() throws Exception {
		assertNull(locate("nonCandidateMethod", "Objects.hash"));
	}

	@Test
	public void nonCandidateReceiverFindsNothing() throws Exception {
		assertNull(locate("nonCandidateReceiver", "Integer.parseInt"));
	}

	@Test
	public void predicateReceiverClassifies() throws Exception {
		final var target = locate("predicateReceiver", "Predicate.not");
		assertNotNull(target);
		assertEquals("java.util.function.Predicate", target.fqcn());
		assertEquals("not", target.simpleMethod());
	}

	/**
	 * The classifier reports the call's shape, not the decision to flag it: the file declares a nested
	 * {@code Objects} type and this method takes a parameter of that name, either of which stops the
	 * check reporting. Those gates belong to the check instance, which holds the whole file's state,
	 * and the fixer is only ever handed a position the check already decided to report.
	 */
	@Test
	public void shadowedReceiverStillClassifies() throws Exception {
		assertNotNull(locate("shadowedReceiver", "Objects.isNull"));
	}

	@Test
	public void spaceAfterDotClassifies() throws Exception {
		assertNotNull(locate("spaceAfterDot", "Objects. isNull"));
	}

	@Test
	public void spaceBeforeDotClassifies() throws Exception {
		assertNotNull(locate("spaceBeforeDot", "Objects .isNull"));
	}

	@Test
	public void spacedDotClassifies() throws Exception {
		assertNotNull(locate("spacedDot", "Objects . isNull"));
	}

	/** The fixed form of every other row, which must not be reported a second time. */
	@Test
	public void staticallyImportedCallFindsNothing() throws Exception {
		assertNull(locate("staticallyImportedCall", "requireNonNull(text)"));
	}

	@Test
	public void staticImportDeclarationFindsNothing() throws Exception {
		assertNull(locateTopLevel("Objects.requireNonNull;", "Objects.requireNonNull;"));
	}

	@Test
	public void stringLiteralFindsNothing() throws Exception {
		assertNull(locate("stringLiteralSpelling", "Objects.isNull"));
	}

	/**
	 * The line carries a supplementary character ahead of the receiver, so its char index and its
	 * code-point column differ. Only the code-point column names a node.
	 */
	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryCharAhead", "Objects.isNull");
		final var text = lines.get(line);
		final var charIndex = text.indexOf("Objects.isNull");
		assertNotEquals(charIndex, columnOf(text, "Objects.isNull"));
		assertNotNull(PreferStaticImportCheck.staticCallAt(parseFixture(), line, columnOf(text, "Objects.isNull")));
		assertNull(PreferStaticImportCheck.staticCallAt(parseFixture(), line, charIndex));
	}

	@Test
	public void textBlockFindsNothing() throws Exception {
		assertNull(locate("textBlockSpelling", "Objects.isNull"));
	}

	@Test
	public void tightDotClassifies() throws Exception {
		final var target = locate("tightDot", "Objects.isNull");
		assertNotNull(target);
		assertEquals(OBJECTS, target.fqcn());
		assertEquals("isNull", target.simpleMethod());
		assertEquals(TokenTypes.IDENT, target.receiver().getType());
		assertEquals(TokenTypes.IDENT, target.methodIdent().getType());
	}

	@Test
	public void twoCallsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("twoCallsOnOneLine", "Objects.isNull(text)");
		final var second = locate("twoCallsOnOneLine", "Objects.isNull(other)");
		assertNotNull(first);
		assertNotNull(second);
		assertNotEquals(first.receiver().getColumnNo(), second.receiver().getColumnNo());
	}

	/**
	 * A type witness puts {@code TYPE_ARGUMENTS} between the two names. An unqualified invocation has
	 * nowhere to write one, so there is no static-import form of this call to rewrite it to.
	 */
	@Test
	public void typeWitnessFindsNothing() throws Exception {
		assertNull(locate("typeWitness", "Collectors."));
	}
}