package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.AstSpan.TextPos;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Direct tests for {@link AstSpan#sliceSpan}, whose two ends are caller-supplied {@link TextPos}
 * values rather than AST positions: every refusal arm, and the empty-result asymmetry (a zero-width
 * single-line span is {@code ""} while an all-blank multi-line one is {@code null}), is reachable
 * here and was invisible while the method was private to one check.
 */
public class AstSpanTest {
	private static final String FIXTURE = "astspan/InputAstSpan.java";

	@CheckReturnValue
	@Nullable
	private static DetailAST firstOfType(@Nonnull DetailAST from, int type) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(from);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == type)
				return node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static URL fixtureUrl() {
		return requireNonNull(
				AstSpanTest.class.getResource("/com/etk2000/checkstyle/inputs/" + FIXTURE),
				"Test input file not found: " + FIXTURE
		);
	}

	/** The body of the fixture method named {@code name}, so no test hard-codes a fixture line. */
	@CheckReturnValue
	@Nonnull
	private static DetailAST methodBody(@Nonnull String name) throws Exception {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(parseFixture());
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_DEF) {
				final var ident = node.findFirstToken(TokenTypes.IDENT);
				if (ident != null && name.equals(ident.getText()))
					return node;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("fixture has no method named " + name);
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseFixture() throws Exception {
		return JavaParser.parseFile(new File(fixtureUrl().toURI()), JavaParser.Options.WITHOUT_COMMENTS);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> sourceLines() throws Exception {
		return Files.readAllLines(new File(fixtureUrl().toURI()).toPath(), StandardCharsets.UTF_8);
	}

	@Test
	public void sliceNodeJoinsMultiLineOperand() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("multiLineCall"), TokenTypes.METHOD_CALL));
		assertEquals(
				"destinationCollectionWithALongName.addAll(sourceCollectionWithALongName)",
				AstSpan.sliceNode(sourceLines(), call)
		);
	}

	/**
	 * An identifier may legitimately be spelled like a token name, and {@code TokenTypes.IDENT} is
	 * written constantly in this very codebase. Deciding "imaginary" by comparing a node's text to
	 * its own token name misreads that one as structure and truncates the span before it.
	 */
	@Test
	public void sliceNodeKeepsAnIdentifierSpelledLikeItsOwnTokenName() throws Exception {
		final var body = methodBody("identifierSpelledLikeItsOwnTokenName");
		final var dot = requireNonNull(firstOfType(body, TokenTypes.DOT));
		assertEquals("Names.IDENT", AstSpan.sliceNode(sourceLines(), dot));
	}

	/**
	 * The wrapper's token name is a strict PREFIX of the identifier it borrows its position from, so
	 * the source check admits both and only the longer one may measure the span. Taking the first
	 * verified node instead would slice {@code EXPR} out of {@code EXPRESSION}. The traversal starts
	 * at the wrapper, not above it: {@code spanEnd} walks only its own subtree, so rooting at the
	 * {@code DOT} below would never visit the wrapper and the tie would not arise.
	 */
	@Test
	public void sliceNodeOnExprWrapperKeepsAnIdentifierPrefixedByTheWrapperTokenName() throws Exception {
		final var wrapper = requireNonNull(firstOfType(methodBody("expression"), TokenTypes.EXPR));
		assertEquals("EXPR", wrapper.getText());
		assertEquals("EXPRESSION", AstSpan.sliceNode(sourceLines(), wrapper));
	}

	/**
	 * The regression this guards: an {@code EXPR} wrapper ties with the {@code IDENT} it takes its
	 * position from and wins the tie, so measuring the span by its {@code getText()} once yielded
	 * {@code "sour"} - four characters, the length of the token name - instead of the identifier.
	 */
	@Test
	public void sliceNodeOnExprWrapperYieldsTheWholeIdentifier() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		final var elist = requireNonNull(call.findFirstToken(TokenTypes.ELIST));
		final var wrapper = requireNonNull(elist.getFirstChild());
		assertEquals("EXPR", wrapper.getText());
		assertEquals("source", AstSpan.sliceNode(sourceLines(), wrapper));
	}

	@Test
	public void sliceSpanAllBlankMultiLineIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab", "  ", "cd"), new TextPos(0, 2), new TextPos(2, 0)));
	}

	@Test
	public void sliceSpanBackwardsColumnsSameLineIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("abcdef"), new TextPos(0, 5), new TextPos(0, 2)));
	}

	@Test
	public void sliceSpanBackwardsLinesIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab", "cd"), new TextPos(1, 0), new TextPos(0, 0)));
	}

	/** Paired with {@link #sliceSpanIndexPastLineLengthIsNull}: one past the last char is a valid end. */
	@Test
	public void sliceSpanIndexAtLineLengthIsAccepted() {
		assertEquals("abc", AstSpan.sliceSpan(List.of("abc"), new TextPos(0, 0), new TextPos(0, 3)));
	}

	@Test
	public void sliceSpanIndexPastLineLengthIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("abc"), new TextPos(0, 0), new TextPos(0, 4)));
	}

	@Test
	public void sliceSpanJoinsLooseAcrossLines() {
		assertEquals("a + b", AstSpan.sliceSpan(List.of("a +", "b"), new TextPos(0, 0), new TextPos(1, 1)));
	}

	@Test
	public void sliceSpanJoinsTightAcrossLines() {
		assertEquals(
				"list.addAll(src);",
				AstSpan.sliceSpan(List.of("list.addAll(", "src", ");"), new TextPos(0, 0), new TextPos(2, 2))
		);
	}

	/**
	 * The loop tests the carried state at the top of each iteration, so the state the final region
	 * leaves open is computed and discarded. No parse can produce this span (a text block's rightmost
	 * node is its closing delimiter), so the row documents the discard rather than asserting a fix.
	 */
	@Test
	public void sliceSpanLastLineOpeningTextBlockIsNotRefused() {
		assertEquals(
				"a + b \"\"\"",
				AstSpan.sliceSpan(List.of("a +", "b \"\"\""), new TextPos(0, 0), new TextPos(1, 5))
		);
	}

	@Test
	public void sliceSpanLinePastBufferIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab"), new TextPos(0, 0), new TextPos(1, 0)));
	}

	/** Isolatable only across lines: a same-line negative end is swallowed by the backwards guard. */
	@Test
	public void sliceSpanNegativeEndIndexIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab", "cd"), new TextPos(0, 0), new TextPos(1, -1)));
	}

	@Test
	public void sliceSpanNegativeLineIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab"), new TextPos(-1, 0), new TextPos(0, 2)));
	}

	@Test
	public void sliceSpanNegativeStartIndexIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("abc"), new TextPos(0, -1), new TextPos(0, 2)));
	}

	@Test
	public void sliceSpanNullEndIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab"), new TextPos(0, 0), null));
	}

	@Test
	public void sliceSpanNullStartIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("ab"), null, new TextPos(0, 2)));
	}

	@Test
	public void sliceSpanRefusesCommentInRegion() {
		assertNull(AstSpan.sliceSpan(List.of("f(a, // note", "b)"), new TextPos(0, 0), new TextPos(1, 2)));
	}

	@Test
	public void sliceSpanRefusesLineBeginningInTextBlock() {
		assertNull(AstSpan.sliceSpan(List.of("s = \"\"\"", "txt", "\"\"\";"), new TextPos(0, 0), new TextPos(2, 4)));
	}

	@Test
	public void sliceSpanSingleLineIsVerbatim() {
		assertEquals(
				"list.addAll(src)",
				AstSpan.sliceSpan(List.of("\tlist.addAll(src);"), new TextPos(0, 1), new TextPos(0, 17))
		);
	}

	/**
	 * The start index has its own bound: {@link #sliceSpanIndexAtLineLengthIsAccepted} and its
	 * past-end partner pin the END index only, and a start past its own line's length survives every
	 * earlier guard when the span is multi-line.
	 */
	@Test
	public void sliceSpanStartIndexPastLineLengthIsNull() {
		assertNull(AstSpan.sliceSpan(List.of("abc", "de"), new TextPos(0, 4), new TextPos(1, 2)));
	}

	/** Paired with {@link #sliceSpanAllBlankMultiLineIsNull}: both spans are empty, the answers differ. */
	@Test
	public void sliceSpanZeroWidthSingleLineIsEmptyString() {
		assertEquals("", AstSpan.sliceSpan(List.of("abcdef"), new TextPos(0, 3), new TextPos(0, 3)));
	}

	/**
	 * A childless imaginary node is its own whole subtree, so no real token is found anywhere and
	 * the search ends with nothing selected. All four null-returning {@code spanEnd} tests share
	 * that one arm now that the post-selection bounds guards are gone; each reaches it from a
	 * different input class.
	 */
	@Test
	public void spanEndNullForImaginaryNode() throws Exception {
		final var elist = requireNonNull(firstOfType(methodBody("noArgCall"), TokenTypes.ELIST));
		assertEquals("ELIST", elist.getText());
		assertNull(AstSpan.spanEnd(sourceLines(), elist));
	}

	@Test
	public void spanEndNullForMultiLineToken() throws Exception {
		final var content = requireNonNull(firstOfType(methodBody("textBlock"), TokenTypes.TEXT_BLOCK_CONTENT));
		assertNull(AstSpan.spanEnd(sourceLines(), content));
	}

	/** The buffer-LINE-shorter case; {@link #spanEndNullWhenNodeLineBeyondBuffer} is the list-shorter one. */
	@Test
	public void spanEndNullWhenColumnPastLineEnd() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		final var truncated = new ArrayList<>(sourceLines());
		truncated.set(call.getLineNo() - 1, "");
		assertNull(AstSpan.spanEnd(truncated, call));
	}

	/** The buffer-shorter-than-the-AST case a stale same-pass edit would produce. */
	@Test
	public void spanEndNullWhenNodeLineBeyondBuffer() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		assertNull(AstSpan.spanEnd(sourceLines().subList(0, 1), call));
	}

	@Test
	public void spanEndPastLastTokenOnItsLine() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		assertEquals(new TextPos(call.getLineNo() - 1, 28), AstSpan.spanEnd(sourceLines(), call));
	}

	/** The wrapper and its {@code IDENT} share a position; only the identifier's length may measure it. */
	@Test
	public void spanEndSkipsTheImaginaryExprWrapper() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		final var wrapper = requireNonNull(requireNonNull(call.findFirstToken(TokenTypes.ELIST)).getFirstChild());
		assertEquals(new TextPos(call.getLineNo() - 1, 27), AstSpan.spanEnd(sourceLines(), wrapper));
	}

	/**
	 * The astral character earlier on the line makes {@code marker}'s code-point column one less than
	 * its char index, which is the whole reason both ends convert through
	 * {@code LineText.charIndexOfColumn} rather than indexing by column.
	 */
	@Test
	public void spanStartConvertsColumnPastSupplementaryChar() throws Exception {
		final var body = methodBody("supplementaryBeforeOperand");
		final var plus = requireNonNull(firstOfType(body, TokenTypes.PLUS));
		final var marker = requireNonNull(plus.findFirstToken(TokenTypes.IDENT));
		assertEquals("marker", marker.getText());
		final var start = requireNonNull(AstSpan.spanStart(sourceLines(), marker));
		assertEquals(new TextPos(marker.getLineNo() - 1, 25), start);
		assertNotEquals(marker.getColumnNo(), start.index(), "fixture lost its supplementary character");
	}

	@Test
	public void spanStartFindsLeftmostDescendant() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		assertEquals(new TextPos(call.getLineNo() - 1, 2), AstSpan.spanStart(sourceLines(), call));
	}

	/** The sibling of {@link #spanEndNullWhenColumnPastLineEnd}, on the other end of every span. */
	@Test
	public void spanStartNullWhenColumnPastLineEnd() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		final var truncated = new ArrayList<>(sourceLines());
		truncated.set(call.getLineNo() - 1, "");
		assertNull(AstSpan.spanStart(truncated, call));
	}

	@Test
	public void spanStartNullWhenNodeLineBeyondBuffer() throws Exception {
		final var call = requireNonNull(firstOfType(methodBody("singleLineCall"), TokenTypes.METHOD_CALL));
		assertNull(AstSpan.spanStart(sourceLines().subList(0, 1), call));
	}
}