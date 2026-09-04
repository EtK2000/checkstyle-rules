package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.PreferLiteralSuffixCheck.LiteralSuffixTarget;
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
 * Direct tests for {@link PreferLiteralSuffixCheck#targetAt}, the seam
 * {@code PreferLiteralSuffixFixer} dispatches on. The fixer deletes the cast it is handed, so the
 * rows that matter most are the ones asserting a position does <em>not</em> classify: each is a
 * shape the fixer would otherwise delete a cast from.
 */
public class PreferLiteralSuffixLocatorTest {
	private static final String FIXTURE = "preferliteralsuffix/InputLocatorPositions.java";

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
	 * inside one method keeps a needle such as {@code (long)}, which every one of them shares,
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
	private static LiteralSuffixTarget locate(@Nonnull String method, @Nonnull String needle) throws Exception {
		return locateColumn(method, needle, needle, 0);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at an absolute
	 * {@code column} rather than one derived from the text sitting there.
	 */
	@CheckReturnValue
	@Nullable
	private static LiteralSuffixTarget locateAt(
			@Nonnull String method, @Nonnull String lineNeedle, int column
	) throws Exception {
		return PreferLiteralSuffixCheck.targetAt(
				parseFixture(JavaParser.Options.WITHOUT_COMMENTS), lineIn(fixtureLines(), method, lineNeedle), column
		);
	}

	/**
	 * What the check classifies on the line {@code lineNeedle} picks out, at the column
	 * {@code columnNeedle} begins at plus {@code offset} code points.
	 */
	@CheckReturnValue
	@Nullable
	private static LiteralSuffixTarget locateColumn(
			@Nonnull String method, @Nonnull String lineNeedle, @Nonnull String columnNeedle, int offset
	) throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, method, lineNeedle);
		return PreferLiteralSuffixCheck.targetAt(
				parseFixture(JavaParser.Options.WITHOUT_COMMENTS),
				line,
				columnOf(lines.get(line), columnNeedle) + offset
		);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture(@Nonnull JavaParser.Options options) throws Exception {
		final var file = new File(resource().toURI());
		return JavaParser.parseFileText(new FileText(file, StandardCharsets.UTF_8.name()), options);
	}

	@CheckReturnValue
	@Nonnull
	private static URL resource() {
		return requireNonNull(
				PreferLiteralSuffixLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void bitThirtyOneLiteralFindsNothing() throws Exception {
		assertNull(locate("bitThirtyOneLiteral", "(long)"));
	}

	@Test
	public void castAtColumnZeroClassifies() throws Exception {
		final var target = locate("castAtColumnZero", "(long)");
		assertNotNull(target);
		assertEquals(0, target.typecast().getColumnNo());
	}

	@Test
	public void castBesideACommentedCastClassifies() throws Exception {
		final var lines = fixtureLines();
		final var target = locate("castInsideACommentAndAString", "(long) i");
		assertNotNull(target);
		assertEquals(
				lineIn(lines, "castInsideACommentAndAString", "(long) i") + 1,
				target.typecast().getLineNo()
		);
	}

	@Test
	public void castColumnZeroDoesNotFallBackToTheFirstOnTheLine() throws Exception {
		assertNull(locateAt("castWithLiteralSibling", "(long)", 0));
	}

	@Test
	public void castInsideACommentOnTheSameLineFindsNothing() throws Exception {
		assertNull(locateColumn("castInsideACommentAndAString", "(long) i", "(long) j", 0));
	}

	@Test
	public void castInsideAStringOnTheSameLineFindsNothing() throws Exception {
		assertNull(locate("castInsideACommentAndAString", "(long) k"));
	}

	@Test
	public void castOnePastTheParenFindsNothing() throws Exception {
		assertNull(locateColumn("castWithLiteralSibling", "(long)", "(long)", 1));
	}

	@Test
	public void castOnTheLineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "castWithLiteralSibling", "(long)");
		assertNull(PreferLiteralSuffixCheck.targetAt(
				parseFixture(JavaParser.Options.WITHOUT_COMMENTS), line - 1, columnOf(lines.get(line), "(long)")
		));
	}

	@Test
	public void castToIntFindsNothing() throws Exception {
		assertNull(locate("castToInt", "(int)"));
	}

	@Test
	public void castWithLiteralSiblingClassifies() throws Exception {
		final var target = locate("castWithLiteralSibling", "(long)");
		assertNotNull(target);
		assertEquals(TokenTypes.TYPECAST, target.typecast().getType());
		assertEquals(TokenTypes.IDENT, target.subject().getType());
		assertEquals(TokenTypes.NUM_INT, target.literal().getType());
		assertEquals("100", target.literalText());
		assertEquals("L", target.suffix());
	}

	@Test
	public void castWithNonLiteralSiblingFindsNothing() throws Exception {
		assertNull(locate("castWithNonLiteralSibling", "(long)"));
	}

	@Test
	public void literalIsTheNumIntNotTheUnaryWrapper() throws Exception {
		final var target = locate("castWithNegatedLiteral", "(long)");
		assertNotNull(target);
		assertEquals(TokenTypes.NUM_INT, target.literal().getType());
		assertEquals("100", target.literal().getText());
		assertEquals("-100", target.literalText());
	}

	@Test
	public void nestedInnerCastFindsNothing() throws Exception {
		assertNull(locate("nestedCastPair", "(int)"));
	}

	@Test
	public void nestedOuterCastClassifies() throws Exception {
		final var target = locate("nestedCastPair", "(long)");
		assertNotNull(target);
		assertEquals(TokenTypes.TYPECAST, target.subject().getType());
	}

	@Test
	public void sameColumnCastPairBothClassify() throws Exception {
		final var lines = fixtureLines();
		final var first = locate("sameColumnCastPair", "(long) i");
		final var second = locate("sameColumnCastPair", "(long) j");
		assertNotNull(first);
		assertNotNull(second);
		assertEquals(lineIn(lines, "sameColumnCastPair", "(long) i") + 1, first.typecast().getLineNo());
		assertEquals(lineIn(lines, "sameColumnCastPair", "(long) j") + 1, second.typecast().getLineNo());
		assertEquals(first.typecast().getColumnNo(), second.typecast().getColumnNo());
		assertEquals("100", first.literalText());
		assertEquals("200", second.literalText());
	}

	@Test
	public void shiftCastFindsNothing() throws Exception {
		assertNull(locate("shiftCast", "(long)"));
	}

	/**
	 * A {@code targetAt} column counts code points, so a supplementary character ahead of the cast
	 * puts the char index and the column at different numbers. Only the column may classify; the char
	 * index addresses a position inside the preceding text.
	 */
	@Test
	public void theCodePointColumnLocatesWhereTheCharIndexMisses() throws Exception {
		final var lines = fixtureLines();
		final var line = lineIn(lines, "supplementaryBeforeCast", "(long)");
		final var text = lines.get(line);
		final var charIndex = text.indexOf("(long)");
		final var codePointColumn = text.codePointCount(0, charIndex);

		assertNotEquals(charIndex, codePointColumn, "the fixture line must carry a supplementary character");
		final var target = PreferLiteralSuffixCheck.targetAt(
				parseFixture(JavaParser.Options.WITHOUT_COMMENTS), line, codePointColumn
		);
		assertNotNull(target);
		assertEquals(line + 1, target.typecast().getLineNo());
		assertNull(PreferLiteralSuffixCheck.targetAt(
				parseFixture(JavaParser.Options.WITHOUT_COMMENTS), line, charIndex
		));
	}

	@Test
	public void twoCastsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("twoCastsOnOneLine", "(long) a");
		final var second = locate("twoCastsOnOneLine", "(long) b");
		assertNotNull(first);
		assertNotNull(second);
		assertEquals("100", first.literalText());
		assertEquals("200", second.literalText());
		assertNotEquals(first.typecast().getColumnNo(), second.typecast().getColumnNo());
	}

	/**
	 * A {@code WITH_COMMENTS} parse weaves comment nodes in as operand siblings, so a comment ahead of
	 * an operand becomes a sibling the walk has to step over. Each method below puts one in the way of
	 * a different walk: the operator's first child, the ternary's forward walk to its {@code COLON},
	 * and the two backward walks. Asserting equality rather than non-nullity is what makes this pin
	 * the fields the fixer consumes, not merely that something was found.
	 */
	@Test
	public void withCommentsParseClassifiesTheSameTargets() throws Exception {
		final var lines = fixtureLines();
		final var methods = List.of(
				"supplementaryBeforeCast",
				"commentBeforeLiteralOperand",
				"commentBeforeTernaryColon",
				"commentBeforeTernaryFalseBranch",
				"commentBeforeTernaryTrueBranchLiteral"
		);
		for (var method : methods) {
			final var line = lineIn(lines, method, "(long)");
			final var column = columnOf(lines.get(line), "(long)");
			final var without = PreferLiteralSuffixCheck.targetAt(
					parseFixture(JavaParser.Options.WITHOUT_COMMENTS), line, column
			);
			final var with = PreferLiteralSuffixCheck.targetAt(
					parseFixture(JavaParser.Options.WITH_COMMENTS), line, column
			);
			assertNotNull(without, method + " must classify under a WITHOUT_COMMENTS parse");
			assertNotNull(with, method + " must classify under a WITH_COMMENTS parse");
			assertEquals(without.literalText(), with.literalText(), method + ": literalText");
			assertEquals(without.suffix(), with.suffix(), method + ": suffix");
			assertEquals(without.literal().getText(), with.literal().getText(), method + ": literal");
			assertEquals(without.subject().getType(), with.subject().getType(), method + ": subject token");
			assertEquals(
					List.of(without.typecast().getLineNo(), without.typecast().getColumnNo()),
					List.of(with.typecast().getLineNo(), with.typecast().getColumnNo()),
					method + ": typecast position"
			);
		}
	}
}