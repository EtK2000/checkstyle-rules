package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import org.junit.jupiter.api.Test;

import java.util.List;

public class FieldConsolidationFixerTest {
	private static final LexerState IN_BLOCK_COMMENT = new LexerState(true, false);
	private static final LexerState IN_TEXT_BLOCK = new LexerState(false, true);
	private static final String BLOCK_COMMENT_REASON = "block comment on the field declaration line";
	private static final String MIXED_BRACKETS_REASON = "cannot consolidate a declaration whose declarators carry different array brackets";
	private static final String PREV_FIELD_REASON = "could not locate the preceding field declaration";
	private static final String TOPIC = "fieldconsolidation";

	private final CheckstyleFixer fixer = new FieldConsolidationFixer();

	@Test
	public void testArrayTypeBothCStyleNoSemicolonOnViolation() throws Exception {
		// Cannot migrate to assertCaseFix: snippet ends with `int beta[]` (no semicolon),
		// not a parseable class body.
		assertSimpleFix(fixer, TOPIC, "array_type_both_c_style_no_semicolon_on_violation");
	}

	@Test
	public void testBackwardScanHitsBlockComment() throws Exception {
		assertSkipResult(fixer, TOPIC, "backward_scan_hits_block_comment", PREV_FIELD_REASON);
	}

	@Test
	public void testBackwardScanHitsCommentLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "backward_scan_hits_comment_line", PREV_FIELD_REASON);
	}

	@Test
	public void testBackwardScanHitsJavadoc() throws Exception {
		assertSkipResult(fixer, TOPIC, "backward_scan_hits_javadoc", PREV_FIELD_REASON);
	}

	@Test
	public void testBackwardScanHitsMultiLineJavadoc() throws Exception {
		assertSkipResult(fixer, TOPIC, "backward_scan_hits_multi_line_javadoc", PREV_FIELD_REASON);
	}

	@Test
	public void testColumnAtBracketAfterPriorFix() {
		// Not reachable via a slice: assertCaseFix always dispatches at the
		// check-reported column, which is the field name.
		final var result = assertInstanceOf(
				FixResult.class,
				fixer.fix(List.of("\tint alpha[];", "\tint[] beta;"), 1, 4)
		);
		assertEquals(0, result.startLine());
		assertEquals(1, result.endLine());
		assertEquals(List.of("\tint[] alpha, beta;"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaMergeLastField() throws Exception {
		assertSimpleFix(fixer, TOPIC, "comma_merge_last_field");
	}

	@Test
	public void testCommaMergeNoTerminatorOnViolation() throws Exception {
		assertSimpleFix(fixer, TOPIC, "comma_merge_no_terminator_on_violation");
	}

	@Test
	public void testCommaMergeThroughAnnotation() throws Exception {
		// can't migrate: single-decl continuation case with type-use @Deprecated between commas;
		// check requires two separate decls with matching annotation sets to fire.
		assertSimpleFix(fixer, TOPIC, "comma_merge_through_annotation");
	}

	@Test
	public void testCommaMergeThroughAnnotationWithInnerComma() throws Exception {
		// can't migrate: single-decl continuation with annotation containing inner commas;
		// check requires two separate decls with matching annotation sets to fire.
		assertSimpleFix(fixer, TOPIC, "comma_merge_through_annotation_with_inner_comma");
	}

	@Test
	public void testCommaMergeWithBlockComment() throws Exception {
		// can't migrate: single-decl continuation case; check requires two separate decls to fire.
		assertSimpleFix(fixer, TOPIC, "comma_merge_with_block_comment");
	}

	@Test
	public void testCommaMergeWithLineComment() throws Exception {
		// can't migrate: single-decl continuation case; check requires two separate decls to fire.
		assertSimpleFix(fixer, TOPIC, "comma_merge_with_line_comment");
	}

	@Test
	public void testCommaMergeWraps() throws Exception {
		// can't migrate: single-decl continuation case; check requires two separate decls to fire.
		assertSimpleFix(fixer, TOPIC, "comma_merge_wraps");
	}

	@Test
	public void testContinuationLoopExhaustsLines() throws Exception {
		assertSimpleFix(fixer, TOPIC, "continuation_loop_exhausts_lines");
	}

	@Test
	public void testContinuationStopsAtCommentNoCommaOnViolation() throws Exception {
		assertSimpleFix(fixer, TOPIC, "continuation_stops_at_comment_no_comma_on_violation");
	}

	@Test
	public void testEntryStatesPropagatesMultiLineCarry() {
		final var states = FieldConsolidationFixer.entryStates(List.of("int /* a", "b */ c;", "String s = \"\"\"", "x"));
		assertEquals(LexerState.NONE, states.get(0));
		assertEquals(IN_BLOCK_COMMENT, states.get(1));
		assertEquals(LexerState.NONE, states.get(2));
		assertEquals(IN_TEXT_BLOCK, states.get(3));
	}

	@Test
	public void testFindFieldSemicolonBlockCommentStateMasksContentButFindsPostCloseSemi() {
		assertEquals(-1, FieldConsolidationFixer.findFieldSemicolon("x;", IN_BLOCK_COMMENT));
		assertEquals(1, FieldConsolidationFixer.findFieldSemicolon("x;", LexerState.NONE));
		assertEquals(11, FieldConsolidationFixer.findFieldSemicolon("*/ String q;", IN_BLOCK_COMMENT));
	}

	@Test
	public void testFindFieldSemicolonIgnoresBlockCommentContent() {
		assertEquals(10, FieldConsolidationFixer.findFieldSemicolon("a /* ; */ ;", LexerState.NONE));
	}

	@Test
	public void testFindFieldSemicolonIgnoresCharLiteralContent() {
		assertEquals(7, FieldConsolidationFixer.findFieldSemicolon("a = ';';", LexerState.NONE));
		assertEquals(8, FieldConsolidationFixer.findFieldSemicolon("a = '\\\\';", LexerState.NONE)); // char literal '\\' (escaped backslash, even run; closing quote lands identically with or without the escape skip)
		assertEquals(8, FieldConsolidationFixer.findFieldSemicolon("a = '\\'';", LexerState.NONE)); // char literal '\'' (escaped quote); -1 without the escape skip
	}

	@Test
	public void testFindFieldSemicolonIgnoresLineCommentContent() {
		assertEquals(-1, FieldConsolidationFixer.findFieldSemicolon("a // ; b", LexerState.NONE));
		assertEquals(1, FieldConsolidationFixer.findFieldSemicolon("a; // ; b", LexerState.NONE));
	}

	@Test
	public void testFindFieldSemicolonIgnoresStringLiteralContent() {
		assertEquals(7, FieldConsolidationFixer.findFieldSemicolon("a = \";\";", LexerState.NONE));
		assertEquals(10, FieldConsolidationFixer.findFieldSemicolon("a = \"x\\\"y\";", LexerState.NONE)); // string "x\"y" with an escaped quote; real ; at end; -1 without the escape skip
	}

	@Test
	public void testFindFieldSemicolonTextBlockStateMasksContentButFindsPostCloseSemi() {
		assertEquals(-1, FieldConsolidationFixer.findFieldSemicolon("x;", IN_TEXT_BLOCK));
		assertEquals(1, FieldConsolidationFixer.findFieldSemicolon("x;", LexerState.NONE));
		assertEquals(13, FieldConsolidationFixer.findFieldSemicolon("\"\"\") String q;", IN_TEXT_BLOCK));
		assertEquals(-1, FieldConsolidationFixer.findFieldSemicolon("\"\"\") String q;", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaBlockCommentStateMasksContent() {
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("y,", IN_BLOCK_COMMENT));
		assertEquals(1, FieldConsolidationFixer.findTrailingComma("y,", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaIgnoresBlockCommentContent() {
		assertEquals(10, FieldConsolidationFixer.findTrailingComma("a /* , */ ,", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaIgnoresCharLiteralContent() {
		assertEquals(7, FieldConsolidationFixer.findTrailingComma("a = ';',", LexerState.NONE));
		assertEquals(8, FieldConsolidationFixer.findTrailingComma("a = '\\\\',", LexerState.NONE)); // char literal '\\' (escaped backslash, even run; closing quote lands identically with or without the escape skip)
		assertEquals(8, FieldConsolidationFixer.findTrailingComma("a = '\\'',", LexerState.NONE)); // char literal '\'' (escaped quote); -1 without the escape skip
	}

	@Test
	public void testFindTrailingCommaIgnoresLineCommentContent() {
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("a // , b", LexerState.NONE));
		assertEquals(1, FieldConsolidationFixer.findTrailingComma("a, // , b", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaIgnoresStringLiteralContent() {
		assertEquals(7, FieldConsolidationFixer.findTrailingComma("a = \",\",", LexerState.NONE));
		assertEquals(10, FieldConsolidationFixer.findTrailingComma("a = \"x\\\"y\",", LexerState.NONE)); // string "x\"y" with an escaped quote; real , at end; -1 without the escape skip
	}

	@Test
	public void testFindTrailingCommaParenAndBraceDepth() {
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("f(0, 1)", LexerState.NONE));
		assertEquals(7, FieldConsolidationFixer.findTrailingComma("f(0, 1),", LexerState.NONE));
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("{0, 1}", LexerState.NONE));
		assertEquals(6, FieldConsolidationFixer.findTrailingComma("{0, 1},", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaSquareBracketDepth() {
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("a[0, 1]", LexerState.NONE));
		assertEquals(7, FieldConsolidationFixer.findTrailingComma("a[0, 1],", LexerState.NONE));
		assertEquals(3, FieldConsolidationFixer.findTrailingComma("a[],", LexerState.NONE));
	}

	@Test
	public void testFindTrailingCommaTextBlockStateMasksContent() {
		assertEquals(-1, FieldConsolidationFixer.findTrailingComma("y,", IN_TEXT_BLOCK));
		assertEquals(1, FieldConsolidationFixer.findTrailingComma("y,", LexerState.NONE));
	}

	@Test
	public void testHasBlockCommentBeforeBlockCommentStateReadsLineStart() {
		assertTrue(FieldConsolidationFixer.hasBlockCommentBefore("** still comment", 5, IN_BLOCK_COMMENT));
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("x still comment", 5, IN_BLOCK_COMMENT));
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("** still comment", 5, LexerState.NONE));
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("*", 5, IN_BLOCK_COMMENT)); // 1-char carried-comment line
	}

	@Test
	public void testHasBlockCommentBeforeIgnoresLiteralContent() {
		assertTrue(FieldConsolidationFixer.hasBlockCommentBefore("x /* y", 6, LexerState.NONE));
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("\"/*\" x", 6, LexerState.NONE)); // /* inside a string literal is not a block comment
		assertTrue(FieldConsolidationFixer.hasBlockCommentBefore("'\\'' /* x", 9, LexerState.NONE)); // escaped-quote char literal '\'' kept open, real /* after still found; false without the escape skip
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("\"a\\\"/*\" x", 9, LexerState.NONE)); // /* inside a string with an escaped quote stays hidden; true without the escape skip
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("a // b /* x", 11, LexerState.NONE)); // /* inside a // line comment is not a block comment
	}

	@Test
	public void testHasBlockCommentBeforeTextBlockStateMasksContent() {
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("/* x", 4, IN_TEXT_BLOCK));
		assertTrue(FieldConsolidationFixer.hasBlockCommentBefore("/* x", 4, LexerState.NONE));
		assertTrue(FieldConsolidationFixer.hasBlockCommentBefore("\"\"\") x /* y", 9, IN_TEXT_BLOCK));
	}

	@Test
	public void testHasBlockCommentBeforeToleratesOversizedEnd() {
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("ab", 5, LexerState.NONE));
		assertFalse(FieldConsolidationFixer.hasBlockCommentBefore("a/", 5, LexerState.NONE));
	}

	@Test
	public void testLineIndexZero() throws Exception {
		assertSkip(fixer, TOPIC, "line_index_zero");
	}

	@Test
	public void testMergeIgnoresTrailingCommaBeforeSemicolon() throws Exception {
		assertSimpleFix(fixer, TOPIC, "merge_ignores_trailing_comma_before_semicolon");
	}

	@Test
	public void testNoPreviousSemicolon() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_previous_semicolon", PREV_FIELD_REASON);
	}

	@Test
	public void testPrevDeclaratorsWithMixedArrayBrackets() {
		// Not reachable via a slice: FieldConsolidationCheck only reports a pair whose
		// declarations are uniformly typed, so it never points at a declarator list
		// carrying different brackets. Reachable in the pipeline only from a buffer a
		// sibling fixer reshaped after the violation was recorded.
		assertEquals(
				new SkipResult(MIXED_BRACKETS_REASON),
				fixer.fix(List.of("\tint alpha, beta[];", "\tint[] zebra;"), 1, 7)
		);
		assertEquals(
				new SkipResult(MIXED_BRACKETS_REASON),
				fixer.fix(List.of("\tint alpha[], beta[][];", "\tint[][] zebra;"), 1, 9)
		);
	}

	@Test
	public void testPrevLineAllSemicolonsInComments() throws Exception {
		assertSkipResult(fixer, TOPIC, "prev_line_all_semicolons_in_comments", PREV_FIELD_REASON);
	}

	@Test
	public void testPrevLineBlockCommentUnclosed() throws Exception {
		assertSkipResult(fixer, TOPIC, "prev_line_block_comment_unclosed", PREV_FIELD_REASON);
	}

	@Test
	public void testViolationColumnForwardScanExhaustsLine() throws Exception {
		assertSkip(fixer, TOPIC, "violation_column_forward_scan_exhausts_line");
	}

	@Test
	public void testViolationColumnMidIdentifier() throws Exception {
		assertSkip(fixer, TOPIC, "violation_column_mid_identifier");
	}

	@Test
	public void testViolationLineBlockCommentPostNameNoSemicolon() throws Exception {
		assertSkipResult(fixer, TOPIC, "violation_line_block_comment_post_name_no_semicolon", BLOCK_COMMENT_REASON);
	}

	@Test
	public void testViolationLineBlockCommentUnclosed() throws Exception {
		assertSkipResult(fixer, TOPIC, "violation_line_block_comment_unclosed", BLOCK_COMMENT_REASON);
	}

	@Test
	public void testViolationLineWithoutSemicolon() throws Exception {
		assertSimpleFix(fixer, TOPIC, "violation_line_without_semicolon");
	}

	@Test
	public void testWrapContinuationBreaksAtNoIdentLine() throws Exception {
		assertSimpleFix(fixer, TOPIC, "wrap_continuation_breaks_at_no_ident_line");
	}

	@Test
	public void testWrapContinuationBreaksAtSameIndent() throws Exception {
		assertSimpleFix(fixer, TOPIC, "wrap_continuation_breaks_at_same_indent");
	}

	@Test
	public void testWrapContinuationBreaksAtSameIndentMixedTabsSpaces() throws Exception {
		// can't migrate: fragment exercises the fixer's continuation-break logic on a mixed-indent
		// follower line (`beta;`). Wrapped in a class body, `beta;` parses as a third sibling
		// VARIABLE_DEF, so post-fix the check would still flag beta vs alpha (residual violation),
		// failing assertCaseFix's zero-residual guard.
		assertSimpleFix(fixer, TOPIC, "wrap_continuation_breaks_at_same_indent_mixed_tabs_spaces");
	}
}