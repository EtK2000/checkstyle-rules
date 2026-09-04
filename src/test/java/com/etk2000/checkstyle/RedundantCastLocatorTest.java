package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.RedundantCastCheck.RedundantCastTarget;
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
 * Direct tests for {@link RedundantCastCheck#targetAt}, the seam {@code RedundantCastFixer}
 * dispatches on. The fixer deletes the text the target spans, so the rows that matter most are the
 * ones asserting a position does <em>not</em> classify, and the ones asserting which parentheses the
 * target names: a parenthesis wrongly named as a grouping one is deleted out of a call, a switch
 * selector or a lock.
 */
public class RedundantCastLocatorTest {
	private static final String FIXTURE = "redundantcast/InputLocatorPositions.java";

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
	 * inside one method keeps a needle such as {@code (String)}, which most of them share, pointing at
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
	private static RedundantCastTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static RedundantCastTarget locateAt(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return RedundantCastCheck.targetAt(parseFixture(), lineIn(fixtureLines(), method, lineNeedle), column);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static RedundantCastTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return RedundantCastCheck.targetAt(parseFixture(), line, columnOf(lines.get(line), columnNeedle) + offset);
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
				RedundantCastLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void argumentParenIsNotAWrap() throws Exception {
		final var target = locate("argumentParen", "(String)");
		assertNotNull(target);
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void arrayDepthTwoNamesBothPairsOfBrackets() throws Exception {
		final var target = locate("arrayDepthTwo", "(int[][])");
		assertNotNull(target);
		assertEquals("int[][]", target.castType());
		assertEquals("int[][]", target.subjectType());
		assertNotNull(target.wrapOpen());
	}

	@Test
	public void bareWrapUnderReturnClassifies() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "bareWrap", "(String)");
		final var target = locate("bareWrap", "(String)");
		assertNotNull(target);
		assertNotNull(target.wrapOpen());
		assertEquals(columnOf(lines.get(line), "(("), target.wrapOpen().getColumnNo());
	}

	@Test
	public void castAtColumnZeroClassifies() throws Exception {
		final var target = locateAt("castAtColumnZero", "((String)", 1);
		assertNotNull(target);
		assertEquals(1, target.typecast().getColumnNo());
		assertNotNull(target.wrapOpen());
		assertEquals(0, target.wrapOpen().getColumnNo());
	}

	@Test
	public void castInsideACommentFindsNothing() throws Exception {
		assertNull(locateColumn("castInsideACommentAndAString", "(String) s", "(String) t", 0));
	}

	@Test
	public void castInsideAStringFindsNothing() throws Exception {
		assertNull(locate("castInsideACommentAndAString", "(String) u"));
	}

	@Test
	public void castOnePastTheParenFindsNothing() throws Exception {
		assertNull(locateColumn("receiverWrap", "(String)", "(String)", 1));
	}

	@Test
	public void castOnTheLineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "receiverWrap", "(String)");
		assertNull(RedundantCastCheck.targetAt(parseFixture(), line - 1, columnOf(lines.get(line), "(String)")));
	}

	@Test
	public void castOnTheLineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "receiverWrap", "(String)");
		assertNull(RedundantCastCheck.targetAt(parseFixture(), line + 1, columnOf(lines.get(line), "(String)")));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAt("receiverWrap", "(String)", 4096));
	}

	@Test
	public void constructorArgumentParenIsNotAWrap() throws Exception {
		final var target = locate("constructorArgumentParen", "(String)");
		assertNotNull(target);
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void doubleWrapFollowsOutToTheOuterPair() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "doubleWrap", "(((String)");
		final var target = locateColumn("doubleWrap", "(((String)", "(String)", 0);
		assertNotNull(target);
		assertNotNull(target.wrapOpen());
		assertNotNull(target.wrapClose());
		assertEquals(columnOf(lines.get(line), "((("), target.wrapOpen().getColumnNo());
		assertEquals(columnOf(lines.get(line), ").length"), target.wrapClose().getColumnNo());
	}

	@Test
	public void ifConditionParenIsNotAWrap() throws Exception {
		final var target = locate("ifConditionParenIsNotAWrap", "(boolean)");
		assertNotNull(target);
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void intersectionCastFindsNothing() throws Exception {
		assertNull(locate("intersectionCast", "(Runnable"));
	}

	@Test
	public void methodReferenceReceiverWraps() throws Exception {
		final var target = locate("methodReferenceReceiver", "(String)");
		assertNotNull(target);
		assertNotNull(target.wrapOpen());
		assertNotNull(target.wrapClose());
		assertEquals(TokenTypes.METHOD_REF, target.typecast().getParent().getType());
	}

	/**
	 * The subject of the outer cast is itself a cast, which binds looser than the {@code .} the wrap
	 * stands to the left of, so the wrap stays even though the sibling test finds it. Asserting the
	 * sibling is what keeps this from passing for the wrong reason.
	 */
	@Test
	public void nestedOuterCastRefusesTheWrapItsSiblingsOffer() throws Exception {
		final var target = locate("nestedCastKeepsItsWrap", "(String) (String)");
		assertNotNull(target);
		assertEquals(TokenTypes.TYPECAST, target.subject().getType());
		assertEquals(TokenTypes.LPAREN, target.typecast().getPreviousSibling().getType());
		assertEquals(TokenTypes.RPAREN, target.typecast().getNextSibling().getType());
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void qualifiedCastTypeFindsNothing() throws Exception {
		assertNull(locate("qualifiedCastType", "(java.util.List)"));
	}

	@Test
	public void receiverWrapClassifies() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "receiverWrap", "(String)");
		final var target = locate("receiverWrap", "(String)");
		assertNotNull(target);
		assertEquals("String", target.castType());
		assertEquals("String", target.subjectType());
		assertEquals(TokenTypes.IDENT, target.subject().getType());
		assertNotNull(target.wrapOpen());
		assertNotNull(target.wrapClose());
		assertEquals(columnOf(lines.get(line), "(("), target.wrapOpen().getColumnNo());
		assertEquals(columnOf(lines.get(line), ").length"), target.wrapClose().getColumnNo());
	}

	@Test
	public void receiverWrapSpanningLinesClassifies() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "receiverWrapSpanningLines", "(String)");
		final var target = locate("receiverWrapSpanningLines", "(String)");
		assertNotNull(target);
		assertNotNull(target.wrapClose());
		assertEquals(line + 1, target.wrapClose().getLineNo());
		assertEquals(line + 2, target.typecast().getParent().findFirstToken(TokenTypes.IDENT).getLineNo());
	}

	@Test
	public void switchSelectorParenIsNotAWrap() throws Exception {
		final var target = locate("switchSelector", "(char)");
		assertNotNull(target);
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void synchronizedLockParenIsNotAWrap() throws Exception {
		final var target = locate("synchronizedLock", "(Object)");
		assertNotNull(target);
		assertNull(target.wrapOpen());
		assertNull(target.wrapClose());
	}

	@Test
	public void twoCastsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("twoCastsOnOneLine", "(String) a");
		final var second = locate("twoCastsOnOneLine", "(String) b");
		assertNotNull(first);
		assertNotNull(second);
		assertNotNull(first.wrapOpen());
		assertNotNull(second.wrapOpen());
		assertNotEquals(first.typecast().getColumnNo(), second.typecast().getColumnNo());
		assertNotEquals(first.wrapOpen().getColumnNo(), second.wrapOpen().getColumnNo());
	}

	@Test
	public void typeArgumentsAreInTheName() throws Exception {
		final var target = locate("typeArgumentsInTheName", "(List<String>)");
		assertNotNull(target);
		assertEquals("List<String>", target.castType());
		assertEquals("List<String>", target.subjectType());
	}
}