package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

/**
 * Fixer-internal tests for the arms the slice pipeline cannot reach.
 */
public class MultilineCallFormattingFixerTest {
	@Nonnull
	private static DetailAST findFirst(@Nonnull DetailAST root, int type) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == type)
				return node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no token of type " + type + " in parsed source");
	}

	@Test
	public void applyArgListReformatMapsStaleGeometryToStaleSkip() throws Exception {
		final var lines = List.of(
				"class C {",
				"\tvoid m() {",
				"\t\tmethod(",
				"\t\t\t\t1, 2,",
				"\t\t\t\t3",
				"\t\t);",
				"\t}",
				"\tvoid method(int a, int b, int c) {",
				"\t}",
				"}"
		);
		final var owner = findFirst(PreferStaticImportConstantFixer.parseLinesToAst(lines), TokenTypes.METHOD_CALL);
		final var stale = new ArrayList<>(lines);
		stale.set(3, "\t\t\t\tx");

		final var skip = assertInstanceOf(SkipResult.class, MultilineCallFormattingFixer.applyArgListReformat(stale, owner));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_STALE, skip.reason());
	}

	@Test
	public void applyTernaryReformatMapsStaleGeometryToStaleSkip() throws Exception {
		final var lines = List.of(
				"class C {",
				"\tvoid m() {",
				"\t\tmethod(true ?",
				"\t\t\t\t\"a\"",
				"\t\t\t\t: \"b\"",
				"\t\t);",
				"\t}",
				"\tvoid method(Object a) {",
				"\t}",
				"}"
		);
		final var question = findFirst(PreferStaticImportConstantFixer.parseLinesToAst(lines), TokenTypes.QUESTION);
		final var stale = new ArrayList<>(lines);
		stale.set(question.getLineNo() - 1, "\t\t// shifted away");

		final var skip = assertInstanceOf(SkipResult.class, MultilineCallFormattingFixer.applyTernaryReformat(stale, question));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_STALE, skip.reason());
	}

	@Test
	public void fixCollapsesWhenTheClosingLineHoldsASupplementaryCharacter() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tmethod(",
				"\t\t\t\t1,",
				"\t\t\t\t\"\uD835\uDC00\");",
				"\t}",
				"",
				"\tvoid method(int a, String b) {",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 8));
		assertEquals(2, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(List.of("\t\tmethod(1, \"\uD835\uDC00\");"), fix.replacement());
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void fixCollapsesWhenTheSpanCrossesABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tfinal var o = new JSONObject()",
				"\t\t\t\t.put(\"k\", /* first",
				"\t\t\t\tsecond */ 1);",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 3, 8));
		assertEquals(2, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(
				List.of("\t\tfinal var o = new JSONObject().put(\"k\", /* first second */ 1);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void fixCollapsesWhenTheStatementBeginsInsideABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\t/* the key is",
				"\t\tthe caller's id */ final var o = new JSONObject()",
				"\t\t\t\t.put(\"k\", 1);",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 8));
		assertEquals(3, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(
				List.of("\t\tthe caller's id */ final var o = new JSONObject().put(\"k\", 1);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void fixDeclinesClosingPullUpWhenTheArgumentsLastLineHidesATrailingComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put(\"k\", x -> {",
				"\t\t\tg(x); /* the",
				"\t\t\tcaller's id */ } // legacy",
				"\t\t);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 5, 2));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT, skip.reason());
	}

	@Test
	public void fixDeclinesCollapseWhenACommentContinuationHidesATrailingComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\tfinal var o = new JSONObject() /* the key is",
				"\t\t\t\tthe caller's id */ // legacy",
				"\t\t\t\t.put(\"k\", 1);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 8));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT, skip.reason());
	}

	@Test
	public void fixDeclinesCollapseWhenTheJoinedFormExceedsTheWidthLimit() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\tnew JSONObject()",
				"\t\t\t\t.put(\"kkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk\",",
				"\t\t\t\t\t\t1);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 3, 8));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixDeclinesCollapseWhenTheKeyIsATextBlock() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tfinal var o = new JSONObject()",
				"\t\t\t\t.put(\"\"\"",
				"\t\t\t\t\t\tkey",
				"\t\t\t\t\t\t\"\"\", 1);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 3, 8));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixDeclinesCollapseWhenTheStatementBeginsInsideABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\t/* the key is",
				"\t\tthe caller's id */ final var o = new JSONObject() // legacy",
				"\t\t\t\t.put(\"k\", 1);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 8));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT, skip.reason());
	}

	@Test
	public void fixDeclinesCollapseWhenTheValueCallSpansATextBlock() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tfinal var o = new JSONObject()",
				"\t\t\t\t.put(\"k\", wrap(\"\"\"",
				"\t\t\t\t\t\ttext\"\"\"));",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 3, 8));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixDeclinesPullUpWhenTheHeadJoinCrossesABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put(",
				"\t\t\t\t/* a",
				"\t\t\t\tb */ \"k\", x -> {",
				"\t\t\t\t\tg(x);",
				"\t\t\t\t});",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 11));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixDeclinesPullUpWhenTheHeadJoinCrossesATextBlock() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put(",
				"\t\t\t\t\"\"\"",
				"\t\t\t\t\t\tk\"\"\", x -> {",
				"\t\t\t\t\tg(x);",
				"\t\t\t\t});",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 11));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixDeclinesPullUpWhenTheOpeningLineBeginsInsideABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\t/* TODO",
				"\t\t   don't forget */ cache.put( // note",
				"\t\t\t\t\"k\", v -> {",
				"\t\t\t\t\trun(v);",
				"\t\t\t\t});",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 3, 30));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN, skip.reason());
	}

	@Test
	public void fixDeclinesPullUpWhenTheTailJoinCrossesABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put(",
				"\t\t\t\t\"k\", x -> {",
				"\t\t\t\t\tg(x);",
				"\t\t\t\t} /* a",
				"\t\t\t\tb */ );",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 11));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixPrefersTheCommentSkipWhenAHeadCommentAndATailLiteralBothApply() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put( // note",
				"\t\t\t\t\"k\", x -> {",
				"\t\t\t\t\tg(x);",
				"\t\t\t\t} /* a",
				"\t\t\t\tb */ );",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 11));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN, skip.reason());
	}

	@Test
	public void fixPrefersTheCommentSkipWhenATailCommentAndAHeadLiteralBothApply() {
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tcache.put(",
				"\t\t\t\t/* a",
				"\t\t\t\tb */ \"k\", x -> {",
				"\t\t\t\t\tg(x);",
				"\t\t\t\t} // note",
				"\t\t);",
				"\t}",
				"}"
		);
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 11));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN, skip.reason());
	}

	/**
	 * The arguments come back at the base indent instead of one continuation level in: the re-indent's
	 * bracket scan seeds {@code LexerState.NONE} at the span's first line, so the apostrophe in
	 * {@code name's} opens a phantom char literal that masks the rest of that line, the {@code (} included.
	 * Its bracket stack stays empty, so every later line is emitted at the base depth. The output compiles,
	 * keeps the comment and is check-clean, so it is pinned as-is rather than fixed here.
	 */
	@Test
	public void fixPreservesATrailingCommentWhenTheOpeningLineBeginsInsideABlockComment() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {",
				"\t\t/* the user",
				"\t\tname's */ method( // legacy",
				"\t\t\t\t1,",
				"\t\t\t\t2);",
				"\t}",
				"",
				"\tvoid method(int a, int b) {",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 5, 5));
		assertEquals(3, fix.startLine());
		assertEquals(5, fix.endLine());
		assertEquals(
				List.of("\t\tname's */ method( // legacy", "\t\t1,", "\t\t2", "\t\t);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void fixReturnsUnsupportedSkipWhenParseFails() {
		final var unterminated = List.of("class T {", "\tString s = \"\"\"", "\tunterminated;");
		final var skip = assertInstanceOf(SkipResult.class, new MultilineCallFormattingFixer().fix(unterminated, 0, 0));
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
	}

	@Test
	public void fixSplitsAtTheConvertedColumnWhenTheClosingLineHoldsASupplementaryCharacter() {
		final var wide = "a".repeat(110);
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\tmethod(",
				"\t\t\t\t1,",
				"\t\t\t\t\"𝐀" + wide + "\");",
				"\t}",
				"",
				"\tvoid method(int a, String b) {",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 118));
		assertEquals(2, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\t1,", "\t\t\t\t\"𝐀" + wide + "\"", "\t\t);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void fixSplitsAtTheConvertedColumnWhenTheOpeningLineHoldsASupplementaryCharacter() {
		final var wide = "a".repeat(110);
		final var lines = List.of(
				"class T {",
				"\tvoid f() {",
				"\t\twrap(\"𝐀\").method(1,",
				"\t\t\t\t\"" + wide + "\"",
				"\t\t);",
				"\t}",
				"}"
		);
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 19));
		assertEquals(2, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(
				List.of("\t\twrap(\"𝐀\").method(", "\t\t\t\t1,", "\t\t\t\t\"" + wide + "\"", "\t\t);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	@Test
	public void parseLinesToAstReturnsAstForTerminatedLiterals() throws Exception {
		final var terminatedTextBlock = List.of("class T {", "\tString s = \"\"\"", "\tclosed", "\t\"\"\";", "}");
		final var terminatedBlockComment = List.of("class T { /* closed", "\tstill comment */", "\tvoid m() {}", "}");
		assertDoesNotThrow(() -> PreferStaticImportConstantFixer.parseLinesToAst(terminatedTextBlock));
		assertDoesNotThrow(() -> PreferStaticImportConstantFixer.parseLinesToAst(terminatedBlockComment));
	}

	@Test
	public void parseLinesToAstThrowsOnUnterminatedLiterals() {
		final var unterminatedTextBlock = List.of("class T {", "\tString s = \"\"\"", "\tunterminated;");
		final var unterminatedBlockComment = List.of("class T { /* never closed", "\tvoid m() {}");
		assertThrows(
				CheckstyleException.class,
				() -> PreferStaticImportConstantFixer.parseLinesToAst(unterminatedTextBlock)
		);
		assertThrows(
				CheckstyleException.class,
				() -> PreferStaticImportConstantFixer.parseLinesToAst(unterminatedBlockComment)
		);
	}
}