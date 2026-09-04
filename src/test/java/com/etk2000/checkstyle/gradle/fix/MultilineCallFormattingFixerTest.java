package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 7));
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

	/**
	 * Accept-side partner to {@link #fixDeclinesCollapseWhenTheStatementBeginsInsideABlockComment}: the
	 * same shape without the trailing {@code //} must still collapse, proving the seeded lexer refuses a
	 * real comment rather than every span whose first line begins inside one.
	 */
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

	/**
	 * The closing pull-up's comment scan must seed its lexer from the start of the file. Seeded at
	 * {@code argLastLine}, that line is lexed as code even though it begins inside a block comment, the
	 * apostrophe in the comment's prose masks the rest of it, and the real trailing {@code //} goes
	 * unseen. The pull-up then joins {@code );} onto the end of that line, behind the comment.
	 */
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

	/**
	 * The comment scan must thread the lexer state across the span. Seeded per line, the block
	 * comment's continuation is lexed as code, the apostrophe in its prose opens a char literal that
	 * masks the rest of the line, and the real trailing {@code //} goes unseen. Joining then buries
	 * the statement inside that comment, which does not compile.
	 */
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

	/**
	 * The fixer is registered per check class, so it runs for every message the check emits and a call
	 * carrying an unrelated violation still reaches the collapse entry point. Here the check refused
	 * the collapse on width and reported the opening/closing rules instead; without its own width gate
	 * the collapse would run anyway and emit a line far past the limit.
	 *
	 * <p>The asserted reason is {@code fix}'s terminal fallthrough rather than a width-specific one: once
	 * the collapse span is refused, this call carries both an opening and a closing violation, and each
	 * paren move requires its own violation to be the call's sole one, so no arm claims it.
	 */
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

	/**
	 * The collapse's comment scan must seed its lexer from the start of the file. Seeded at the span, the
	 * statement's own first line is lexed as code even though it begins inside a block comment, the
	 * apostrophe in the prose masks the rest of the line, and the real trailing {@code //} goes unseen.
	 * The join then buries the {@code .put} call inside that comment.
	 */
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

	/**
	 * The comment scan must seed its lexer from the start of the file, not from the span. Seeded at the
	 * span, the {@code (} line is lexed as code even though it begins inside a block comment, the
	 * apostrophe in the comment's prose opens a char literal that masks the rest of the line, and the real
	 * trailing {@code //} goes unseen. The pull-up then lands the argument after that {@code //}, which
	 * does not compile.
	 */
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

	/**
	 * Both a head-join {@code //} and a tail-join literal refuse this pull-up. The comment scans run
	 * first, so the comment reason is the one reported. Pinned because both outcomes are refusals: a
	 * reorder would silently change the hint text a user sees and nothing else would catch it.
	 */
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

	/**
	 * The mirror of {@link #fixPrefersTheCommentSkipWhenAHeadCommentAndATailLiteralBothApply}: a tail-join
	 * {@code //} against a head-join literal. Both comment scans run before both literal scans, so the
	 * comment reason wins in this pairing too. The other two cells are not constructible: a line carrying
	 * a real trailing {@code //} cannot also leave a block comment open.
	 */
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
	 * End-to-end guard on the nastiest shape this fixer accepts: the {@code (} line begins inside a block
	 * comment whose prose carries an apostrophe, and a real {@code //} follows the code. Whatever path
	 * takes it (today {@code JavaArgListReformatter}, not the collapse), the trailing comment must stay
	 * last on its own line rather than being joined ahead of the arguments.
	 *
	 * <p>The arguments come back at the base indent instead of one continuation level in: the re-indent's
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

	/**
	 * The collapse declines on width here, so the split path runs and the reported code-point column must
	 * be converted before it indexes the line. Read unconverted it lands one char early, on the closing
	 * quote, and the {@code )} guard turns a fixable violation into a stale skip.
	 */
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
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 4, 117));
		assertEquals(2, fix.startLine());
		assertEquals(4, fix.endLine());
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\t1,", "\t\t\t\t\"𝐀" + wide + "\"", "\t\t);"),
				fix.replacement()
		);
		assertEquals(Set.of(), fix.importsToAdd());
	}

	/**
	 * The opening half of the same conversion: the supplementary character sits in a receiver argument
	 * ahead of the {@code (}, so an unconverted column indexes one char early and the {@code (} guard
	 * refuses a fix that is perfectly applicable.
	 */
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
		final var fix = assertInstanceOf(FixResult.class, new MultilineCallFormattingFixer().fix(lines, 2, 18));
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
		assertNotNull(PreferStaticImportConstantFixer.parseLinesToAst(terminatedTextBlock));
		assertNotNull(PreferStaticImportConstantFixer.parseLinesToAst(terminatedBlockComment));
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