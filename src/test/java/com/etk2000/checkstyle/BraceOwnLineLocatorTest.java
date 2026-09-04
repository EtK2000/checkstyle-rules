package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.BraceOwnLineCheck.BraceSite;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link BraceOwnLineCheck#siteAt}, the seam {@code BraceOwnLineFixer} dispatches
 * on: which header line the brace joins, which token the site reports at, and which positions hold
 * no site at all.
 */
public class BraceOwnLineLocatorTest {
	private static final String FIXTURE = "/com/etk2000/checkstyle/inputs/braceownline/InputLocatorPositions.java";

	@CheckReturnValue
	@Nonnull
	private static List<String> fixtureLines() throws Exception {
		return Files.readAllLines(Path.of(requireNonNull(BraceOwnLineLocatorTest.class.getResource(FIXTURE)).toURI()));
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

	/** The site reported where {@code columnNeedle} begins on the first line carrying {@code lineNeedle}. */
	@CheckReturnValue
	@Nullable
	private static BraceSite locate(@Nonnull String lineNeedle, @Nonnull String columnNeedle) throws Exception {
		final var lines = fixtureLines();
		final var line = lineWith(lines, lineNeedle);
		final var text = lines.get(line);
		final var file = new File(requireNonNull(BraceOwnLineLocatorTest.class.getResource(FIXTURE)).toURI());
		final var root = JavaParser.parseFileText(new FileText(file, StandardCharsets.UTF_8.name()), JavaParser.Options.WITHOUT_COMMENTS);
		return BraceOwnLineCheck.siteAt(root, line, text.codePointCount(0, text.indexOf(columnNeedle)));
	}

	@Test
	public void testHeaderEndOfMultiLineControlHeaderIsCloseParenLine() throws Exception {
		final var site = requireNonNull(locate("if (a", "if"));
		final var lines = fixtureLines();
		assertEquals(TokenTypes.LITERAL_IF, site.report().getType());
		assertEquals(lineWith(lines, "&& b)") + 1, site.headerEndLine());
		assertEquals(lineWith(lines, "&& b)") + 2, site.brace().getLineNo());
		assertTrue(site.ownLine());
	}

	@Test
	public void testHeaderEndOfThrowsClauseIsThrowsLine() throws Exception {
		final var site = requireNonNull(locate("void throwsNextLine()", "throwsNextLine"));
		assertEquals(TokenTypes.IDENT, site.report().getType());
		assertEquals(TokenTypes.METHOD_DEF, site.owner().getType());
		assertEquals(lineWith(fixtureLines(), "throws IOException") + 1, site.headerEndLine());
	}

	@Test
	public void testInstanceInitializerIsNotASite() throws Exception {
		assertNull(locate("\t{", "{"));
	}

	@Test
	public void testLambdaReportsAtArrow() throws Exception {
		final var site = requireNonNull(locate("final Runnable r", "->"));
		assertEquals(TokenTypes.LAMBDA, site.report().getType());
		assertEquals(lineWith(fixtureLines(), "final Runnable r") + 1, site.headerEndLine());
	}

	@Test
	public void testNonReportTokenOfASiteIsNotASite() throws Exception {
		assertNull(locate("if (a", "("));
	}

	@Test
	public void testRecordIsNotASite() throws Exception {
		assertNull(locate("record R", "R("));
	}
}