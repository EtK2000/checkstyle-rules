package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
 * Direct tests for {@link RedundantNumericSuffixCheck#suffixedLiteralAt}, the seam
 * {@code RedundantNumericSuffixFixer} dispatches on.
 */
public class RedundantNumericSuffixLocatorTest {
	private static final String FIXTURE = "redundantnumericsuffix/InputLocatorPositions.java";

	/**
	 * Asserts the literal spelled {@code spelling} on {@code field}'s line classifies, and that the
	 * located token ends exactly past its last character, which is where the fixer cuts.
	 */
	private static void assertClassifies(@Nonnull String field, @Nonnull String spelling) throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, field);
		final var text = lines.get(line);
		final var literal = RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line, columnOf(text, spelling));
		assertNotNull(literal);
		assertEquals(spelling, literal.getText());
		final var end = AstSpan.tokenEnd(lines, literal);
		assertNotNull(end);
		assertEquals(text.indexOf(spelling) + spelling.length(), end.index());
	}

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

	/** The 0-based line declaring the field {@code field}. */
	@CheckReturnValue
	private static int lineOf(@Nonnull List<String> lines, @Nonnull String field) {
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).contains(' ' + field + " ="))
				return i;
		}
		throw new IllegalArgumentException("no field [" + field + "] in " + FIXTURE);
	}

	/** What the check classifies where {@code spelling} begins on {@code field}'s line. */
	@CheckReturnValue
	@Nullable
	private static DetailAST locate(@Nonnull String field, @Nonnull String spelling) throws Exception {
		return locateOffset(field, spelling, 0);
	}

	/** What the check classifies on {@code field}'s line at an absolute {@code column}. */
	@CheckReturnValue
	@Nullable
	private static DetailAST locateAbsolute(@Nonnull String field, int column) throws Exception {
		return RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), lineOf(fixtureLines(), field), column);
	}

	/** What the check classifies at {@code line}, with {@code field}'s own column. */
	@CheckReturnValue
	@Nullable
	private static DetailAST locateLine(@Nonnull String field, @Nonnull String spelling, int line) throws Exception {
		final var lines = fixtureLines();
		return RedundantNumericSuffixCheck.suffixedLiteralAt(
				parseFixture(), line, columnOf(lines.get(lineOf(lines, field)), spelling)
		);
	}

	/**
	 * What the check classifies on {@code field}'s line, {@code offset} code points from where
	 * {@code spelling} begins.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST locateOffset(@Nonnull String field, @Nonnull String spelling, int offset) throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, field);
		return RedundantNumericSuffixCheck.suffixedLiteralAt(
				parseFixture(), line, columnOf(lines.get(line), spelling) + offset
		);
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
				RedundantNumericSuffixLocatorTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	@Test
	public void binaryUpperLClassifies() throws Exception {
		assertClassifies("binaryUpperL", "0b1010L");
	}

	@Test
	public void binaryUpperPrefixLClassifies() throws Exception {
		assertClassifies("binaryUpperPrefixL", "0B1010L");
	}

	@Test
	public void columnAtAStringStartFindsNothing() throws Exception {
		// the STRING_LITERAL begins exactly here and its text ends in a suffix letter, so a
		// predicate that read the text instead of the token type would strip the L out of "100L"
		assertNull(locate("stringHoldingSuffix", "\"100L\""));
	}

	@Test
	public void columnInsideACommentFindsNothing() throws Exception {
		assertNull(locateOffset("trailingCommentSuffix", "// 2L", 3));
	}

	@Test
	public void columnInsideAStringFindsNothing() throws Exception {
		assertNull(locateOffset("stringHoldingSuffix", "\"100L\"", 1));
	}

	@Test
	public void columnInsideATextBlockFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, "textBlockHoldingSuffix") + 1;
		assertNull(RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line, columnOf(lines.get(line), "100L")));
	}

	@Test
	public void columnMidLiteralFindsNothing() throws Exception {
		assertNull(locateOffset("hexUpperL", "0xFFL", 1));
	}

	@Test
	public void columnOneBeforeTheLiteralFindsNothing() throws Exception {
		assertNull(locateOffset("decimalUpperL", "1L", -1));
	}

	@Test
	public void columnPastTheLineEndFindsNothing() throws Exception {
		assertNull(locateAbsolute("decimalUpperL", 4096));
	}

	@Test
	public void decimalLowerDClassifies() throws Exception {
		assertClassifies("decimalLowerD", "1.5d");
	}

	@Test
	public void decimalLowerFClassifies() throws Exception {
		assertClassifies("decimalLowerF", "1.5f");
	}

	@Test
	public void decimalLowerLClassifies() throws Exception {
		assertClassifies("decimalLowerL", "1l");
	}

	@Test
	public void decimalUpperDClassifies() throws Exception {
		assertClassifies("decimalUpperD", "1.5D");
	}

	@Test
	public void decimalUpperFClassifies() throws Exception {
		assertClassifies("decimalUpperF", "1.5F");
	}

	@Test
	public void decimalUpperLClassifies() throws Exception {
		assertClassifies("decimalUpperL", "1L");
	}

	@Test
	public void exponentFClassifies() throws Exception {
		assertClassifies("exponentF", "1e2f");
	}

	@Test
	public void exponentMinusUpperDClassifies() throws Exception {
		assertClassifies("exponentMinusUpperD", "1E-2D");
	}

	@Test
	public void exponentMinusUpperFClassifies() throws Exception {
		assertClassifies("exponentMinusUpperF", "1E-2F");
	}

	@Test
	public void exponentPlusDClassifies() throws Exception {
		assertClassifies("exponentPlusD", "1e+2d");
	}

	@Test
	public void exponentPlusFClassifies() throws Exception {
		assertClassifies("exponentPlusF", "1e+2f");
	}

	@Test
	public void hexExponentFClassifies() throws Exception {
		assertClassifies("hexExponentF", "0x1p3f");
	}

	@Test
	public void hexExponentMinusPClassifies() throws Exception {
		assertClassifies("hexExponentMinusP", "0x1p-3d");
	}

	@Test
	public void hexExponentPlusPClassifies() throws Exception {
		assertClassifies("hexExponentPlusP", "0x1p+3d");
	}

	@Test
	public void hexExponentUpperPClassifies() throws Exception {
		assertClassifies("hexExponentUpperP", "0X1P3D");
	}

	@Test
	public void hexIntBeforeOperatorFindsNothing() throws Exception {
		assertNull(locate("hexIntBeforeOperator", "0x1E"));
	}

	@Test
	public void hexLowerLClassifies() throws Exception {
		assertClassifies("hexLowerL", "0xFFl");
	}

	@Test
	public void hexUnderscoreLClassifies() throws Exception {
		assertClassifies("hexUnderscoreL", "0xFF_FFL");
	}

	@Test
	public void hexUpperLClassifies() throws Exception {
		assertClassifies("hexUpperL", "0xFFL");
	}

	@Test
	public void intBeforeOperatorFindsNothing() throws Exception {
		assertNull(locate("intBeforeOperator", "5"));
	}

	@Test
	public void leadingDotDClassifies() throws Exception {
		assertClassifies("leadingDotD", ".5d");
	}

	@Test
	public void leadingDotFClassifies() throws Exception {
		assertClassifies("leadingDotF", ".5f");
	}

	@Test
	public void lineAboveFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, "literalEndingTheLine") + 1;
		assertNull(RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line - 1, columnOf(lines.get(line), "6L")));
	}

	@Test
	public void lineBelowFindsNothing() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, "literalEndingTheLine") + 1;
		assertNull(RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line + 1, columnOf(lines.get(line), "6L")));
	}

	@Test
	public void lineBelowFindsThatLinesOwnLiteral() throws Exception {
		// decimalUpperL and decimalLowerL are named the same length, so their literals share a
		// column: the line is the half of the position that tells the two apart
		final var below = locateLine("decimalUpperL", "1L", lineOf(fixtureLines(), "decimalUpperL") + 1);
		assertNotNull(below);
		assertEquals("1l", below.getText());
	}

	@Test
	public void literalAfterBlockCommentClassifies() throws Exception {
		assertClassifies("afterBlockComment", "1L");
	}

	@Test
	public void literalEndingTheLineSpansToLineEnd() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, "literalEndingTheLine") + 1;
		final var literal = RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line, columnOf(lines.get(line), "6L"));
		assertNotNull(literal);
		final var end = AstSpan.tokenEnd(lines, literal);
		assertNotNull(end);
		assertEquals(lines.get(line).length(), end.index());
	}

	@Test
	public void methodArgumentLiteralClassifies() throws Exception {
		assertClassifies("methodArgument", "7L");
	}

	@Test
	public void noFractionDClassifies() throws Exception {
		assertClassifies("noFractionD", "5d");
	}

	@Test
	public void noFractionFClassifies() throws Exception {
		assertClassifies("noFractionF", "5f");
	}

	@Test
	public void octalUpperLClassifies() throws Exception {
		assertClassifies("octalUpperL", "0777L");
	}

	@Test
	public void plainIntFindsNothing() throws Exception {
		assertNull(locate("plainInt", "5"));
	}

	@Test
	public void spacedNegatedLiteralClassifies() throws Exception {
		assertClassifies("spacedNegatedFloat", "1f");
	}

	@Test
	public void supplementaryCharAheadShiftsTheColumn() throws Exception {
		final var lines = fixtureLines();
		final var line = lineOf(lines, "supplementaryAhead");
		final var text = lines.get(line);
		assertNotNull(RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line, columnOf(text, "5L")));
		assertNotEquals(text.indexOf("5L"), columnOf(text, "5L"));
		assertNull(RedundantNumericSuffixCheck.suffixedLiteralAt(parseFixture(), line, text.indexOf("5L")));
	}

	@Test
	public void trailingDotDClassifies() throws Exception {
		assertClassifies("trailingDotD", "1.d");
	}

	@Test
	public void trailingDotFClassifies() throws Exception {
		assertClassifies("trailingDotF", "1.f");
	}

	@Test
	public void twoLiteralsOnOneLineClassifyIndependently() throws Exception {
		final var first = locate("twoOnOneLine", "3L");
		final var second = locate("twoOnOneLine", "4L");
		assertNotNull(first);
		assertNotNull(second);
		assertEquals("3L", first.getText());
		assertEquals("4L", second.getText());
	}

	@Test
	public void unaryMinusColumnFindsNothing() throws Exception {
		assertNull(locate("negatedFloat", "-1f"));
	}

	@Test
	public void underscoreExponentDClassifies() throws Exception {
		assertClassifies("underscoreExponentD", "1_0.0_1e+1_0d");
	}

	@Test
	public void underscoreLClassifies() throws Exception {
		assertClassifies("underscoreL", "1_000L");
	}

	@Test
	public void unsuffixedDecimalFindsNothing() throws Exception {
		assertNull(locate("unsuffixedDecimal", "1.5"));
	}

	@Test
	public void unsuffixedLeadingDotFindsNothing() throws Exception {
		assertNull(locate("unsuffixedLeadingDot", ".5"));
	}

	@Test
	public void unsuffixedSignedExponentFindsNothing() throws Exception {
		assertNull(locate("unsuffixedSignedExponent", "1e+2"));
	}

	@Test
	public void unsuffixedTrailingDotFindsNothing() throws Exception {
		assertNull(locate("unsuffixedTrailingDot", "1."));
	}
}