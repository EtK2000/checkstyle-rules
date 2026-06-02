package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSimpleFix;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

public class PreferStaticImportConstantFixerTest {
	private static final String FIX_CONTEXT_TOPIC = "preferstaticimportconstantfixcontext";
	private static final String TOPIC = "preferstaticimportconstant";

	private final CheckstyleFixer fixer = new PreferStaticImportConstantFixer();

	@TempDir
	Path tempDir;

	@AfterEach
	public void cleanup() {
		FixContext.clearFilePath();
	}

	@Nonnull
	private List<String> setUpFixContext(
			@Nonnull String targetCase,
			@Nonnull String siblingFileName,
			@Nonnull String siblingCase
	) throws Exception {
		final var siblingLines = TestResources.loadCase(FIX_CONTEXT_TOPIC, siblingCase).inputLines();
		Files.writeString(tempDir.resolve(siblingFileName), String.join("\n", siblingLines));
		final var targetLines = TestResources.loadCase(FIX_CONTEXT_TOPIC, targetCase).inputLines();
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(file.toPath(), String.join("\n", targetLines));
		FixContext.setFilePath(file.getAbsolutePath());
		return new ArrayList<>(targetLines);
	}

	@Test
	public void testAnnotationArgContainingBarePrivateWordDoesNotMisidentifyVisibility() throws Exception {
		assertSkipResult(fixer, TOPIC, "annotation_arg_containing_bare_private_word_does_not_misidentify_visibility", "cannot auto-fix non-private alias: it may be referenced from outside the class");
	}

	@Test
	public void testCinitConflictingTargetFieldReturnsConflictSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "cinit_conflicting_target_field_returns_conflict_skip", "cannot add static import: file already imports a different constant with the same name statically");
	}

	@Test
	public void testCinitFixerWithColumnOffIdentReturnsCinitSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "cinit_fixer_with_column_off_ident_returns_cinit_skip", "could not auto-fix the split-assignment cinit alias (ambiguous match, mismatched qualifier, non-Class.IDENT RHS, or the file did not re-parse cleanly); manually remove both the field and the cinit assignment, then add the static import");
	}

	@Test
	public void testCinitSameLineDeclAndCinitRenamedWithUsageOnEarlierLineIsAutoFixed() throws Exception {
		assertSimpleFix(fixer, TOPIC, "cinit_same_line_decl_and_cinit_renamed_with_usage_on_earlier_line_is_auto_fixed", Set.of("static foo.Foo.X"));
	}

	@Test
	public void testCinitSameLineDeclAndCinitRenamedWithUsageOnOtherLineIsAutoFixed() throws Exception {
		assertSimpleFix(fixer, TOPIC, "cinit_same_line_decl_and_cinit_renamed_with_usage_on_other_line_is_auto_fixed", Set.of("static foo.Foo.X"));
	}

	@Test
	public void testCinitWithNestedSameNameFieldAndNoMatchingCinitReturnsCinitSkip() throws Exception {
		assertSkipResult(
				fixer,
				TOPIC,
				"cinit_with_nested_same_name_field_and_no_matching_cinit_returns_cinit_skip",
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT
		);
	}

	@Test
	public void testConflictingStaticImportWithLeadingBomReturnsConflictSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "conflicting_static_import_with_leading_bom_returns_conflict_skip", "cannot add static import: file already imports a different constant with the same name statically");
	}

	@Test
	public void testConflictScanIgnoresMalformedStaticImport() throws Exception {
		assertSimpleFix(fixer, TOPIC, "conflict_scan_ignores_malformed_static_import", Set.of("static foo.Foo.X"));
	}

	@Test
	public void testDefaultPackageEmitsSimpleNameOnly() throws Exception {
		final var lines = setUpFixContext("default_package_emits_simple_name_only", "Foo.java", "sibling_foo");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static Foo.X"), result.importsToAdd());
	}

	@Test
	public void testDropAllBlankReplacementOnAllBlankListReturnsEmptyList() {
		assertEquals(List.of(), PreferStaticImportConstantFixer.dropAllBlankReplacement(List.of("", "   ", "\t")));
	}

	@Test
	public void testDropAllBlankReplacementOnEmptyListReturnsEmptyList() {
		assertEquals(List.of(), PreferStaticImportConstantFixer.dropAllBlankReplacement(List.of()));
	}

	@Test
	public void testDropAllBlankReplacementOnNonBlankListReturnsInput() {
		final var input = List.of("", "code();", "");
		assertEquals(input, PreferStaticImportConstantFixer.dropAllBlankReplacement(input));
	}

	@Test
	public void testExplicitImportWinsOverSibling() throws Exception {
		final var lines = setUpFixContext("explicit_import_wins_over_sibling", "Foo.java", "sibling_foo");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static other.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFinalizeBodyFixWithDetachedFieldDefReturnsDefaultResult() {
		final var fieldDef = new DetailAstImpl();
		fieldDef.setType(TokenTypes.VARIABLE_DEF);
		final var objBlock = new DetailAstImpl();
		objBlock.setType(TokenTypes.OBJBLOCK);
		objBlock.addChild(fieldDef);
		final var baseReplacement = List.of("// orphan");
		final var importsToAdd = Set.of("static foo.Foo.X");
		final var result = PreferStaticImportConstantFixer.finalizeBodyFix(
				List.of("// orphan"),
				fieldDef,
				null,
				0,
				0,
				baseReplacement,
				importsToAdd
		);
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(baseReplacement, result.replacement());
		assertEquals(importsToAdd, result.importsToAdd());
	}

	@Test
	public void testFindFieldDefForInlineReturnsNullWhenColumnNotOnIdentifierStart() {
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				List.of(" X"),
				0,
				0,
				PreferStaticImportConstantFixer::parseLinesToAst
		));
	}

	@Test
	public void testFindFieldDefForInlineReturnsNullWhenNoVariableDefAtLocation() throws Exception {
		final var lines = TestResources.loadCaseSlice(TOPIC, "int_alias").inputLines();
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				lines,
				0,
				0,
				PreferStaticImportConstantFixer::parseLinesToAst
		));
	}

	@Test
	public void testFindFieldDefForInlineUsingBailsOnAssertionErrorFromParser() {
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				List.of("X"),
				0,
				0,
				ls -> {
					throw new AssertionError("parser bug");
				}
		));
	}

	@Test
	public void testFindFieldDefForInlineUsingBailsOnCheckstyleExceptionFromParser() {
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				List.of("X"),
				0,
				0,
				ls -> {
					throw new CheckstyleException("bad input");
				}
		));
	}

	@Test
	public void testFindFieldDefForInlineUsingBailsOnRuntimeExceptionFromParser() {
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				List.of("X"),
				0,
				0,
				ls -> {
					throw new IllegalStateException("parser internal NPE");
				}
		));
	}

	@Test
	public void testFindFieldDefForInlineUsingBailsOnStackOverflowErrorFromParser() {
		assertNull(PreferStaticImportConstantFixer.findFieldDefForInlineUsing(
				List.of("X"),
				0,
				0,
				ls -> {
					throw new StackOverflowError("deep recursion");
				}
		));
	}

	@Test
	public void testFindShadowKindBailsOnAssertionErrorFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				Set.of(),
				ls -> {
					throw new AssertionError("parser bug");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnCheckstyleExceptionFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				Set.of(),
				ls -> {
					throw new CheckstyleException("bad input");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnRuntimeExceptionFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				Set.of(),
				ls -> {
					throw new IllegalStateException("parser internal NPE");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnStackOverflowErrorFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				Set.of(),
				ls -> {
					throw new StackOverflowError("deep recursion");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindStatementEndBailsOnUnterminatedStringInLine() {
		final var result = PreferStaticImportConstantFixer.findStatementEnd(
				List.of("\"abc"),
				0,
				false,
				false
		);
		assertEquals(-1, result[0]);
		assertEquals(-1, result[1]);
	}

	@Test
	public void testFindStatementEndPropagatesTextBlockStateAcrossLines() {
		final var result = PreferStaticImportConstantFixer.findStatementEnd(
				List.of("a = \"\"\"", "\"\"\";", "b;"),
				0,
				false,
				false
		);
		assertEquals(1, result[0]);
		assertEquals(3, result[1]);
	}

	@Test
	public void testHasCommentMarkerBlockCommentClosingAtEndBeforeRangeReturnsFalse() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("a /*x*/", 5, 7));
	}

	@Test
	public void testHasCommentMarkerDetectsBlockCommentAtUpperBoundMinusOne() {
		assertTrue(PreferStaticImportConstantFixer.hasCommentMarker("ab/*x*/", 0, 3));
	}

	@Test
	public void testHasCommentMarkerDetectsLineCommentAtUpperBoundMinusOne() {
		assertTrue(PreferStaticImportConstantFixer.hasCommentMarker("ab//", 0, 3));
	}

	@Test
	public void testHasCommentMarkerIgnoresBlockCommentAtUpperBound() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("ab/*x*/", 0, 2));
	}

	@Test
	public void testHasCommentMarkerIgnoresLineCommentAtUpperBound() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("ab//", 0, 2));
	}

	@Test
	public void testHasCommentMarkerLineCommentBeforeRangeReturnsFalse() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("a // /*y*/", 5, 9));
	}

	@Test
	public void testHasCommentMarkerSkipsBlockCommentBeforeRangeAndFindsInRangeMarker() {
		assertTrue(PreferStaticImportConstantFixer.hasCommentMarker("/*x*/ ab //", 6, 11));
	}

	@Test
	public void testHasCommentMarkerSkipsBlockCommentBeforeRangeWithNoInRangeMarkerReturnsFalse() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("/*x*/ ab", 6, 8));
	}

	@Test
	public void testHasCommentMarkerUnterminatedBlockCommentBeforeRangeReturnsFalse() {
		assertFalse(PreferStaticImportConstantFixer.hasCommentMarker("/*x ab", 4, 6));
	}

	@Test
	public void testIsLabelOccurrenceBlockCommentBetweenCaseAndIdentReturnsFalse() {
		final var line = "case /* note */ RENAMED:";
		final var matchStart = line.indexOf("RENAMED");
		final var prev = line.charAt(matchStart - 1);
		assertFalse(PreferStaticImportConstantFixer.isLabelOccurrence(line, matchStart, "RENAMED".length(), prev));
	}

	@Test
	public void testIsLabelOccurrenceCaseKeywordReturnsFalse() {
		final var line = "case RENAMED:";
		final var matchStart = "case ".length();
		final var prev = ' ';
		assertFalse(PreferStaticImportConstantFixer.isLabelOccurrence(line, matchStart, "RENAMED".length(), prev));
	}

	@Test
	public void testIsLabelOccurrenceCaseSuffixOfLongerIdentReturnsTrue() {
		final var line = "mycase RENAMED:";
		final var matchStart = "mycase ".length();
		final var prev = ' ';
		assertTrue(PreferStaticImportConstantFixer.isLabelOccurrence(line, matchStart, "RENAMED".length(), prev));
	}

	@Test
	public void testIsLabelOccurrenceUnclosedBlockCommentBeforeIdentBailsLookback() {
		final var line = "*/ RENAMED:";
		final var matchStart = line.indexOf("RENAMED");
		final var prev = line.charAt(matchStart - 1);
		assertTrue(PreferStaticImportConstantFixer.isLabelOccurrence(line, matchStart, "RENAMED".length(), prev));
	}

	@Test
	public void testMalformedImportsAreIgnoredDuringResolution() throws Exception {
		assertSimpleFix(fixer, TOPIC, "malformed_imports_are_ignored_during_resolution", Set.of("static wild.Foo.X"));
	}

	@Test
	public void testMalformedWildcardImportSkipped() throws Exception {
		assertSimpleFix(fixer, TOPIC, "malformed_wildcard_import_skipped", Set.of("static foo.Foo.X"));
	}

	@Test
	public void testMultiVarConflictingTargetAliasReturnsConflictSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_var_conflicting_target_alias_returns_conflict_skip", "cannot add static import: file already imports a different constant with the same name statically");
	}

	@Test
	public void testMultiVarFixerWithColumnOffIdentReturnsMultiVarSkip() throws Exception {
		assertSkipResult(
				fixer,
				TOPIC,
				"multi_var_fixer_with_column_off_ident_returns_multi_var_skip",
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR
		);
	}

	@Test
	public void testMultiVarUnresolvableClassReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_var_unresolvable_class_returns_skip", "initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved");
	}

	@Test
	public void testNoStatementTerminatorAcrossMultipleLinesSkips() throws Exception {
		assertSkip(fixer, TOPIC, "no_statement_terminator_across_multiple_lines_skips");
	}

	@Test
	public void testNoStatementTerminatorSkips() throws Exception {
		assertSkip(fixer, TOPIC, "no_statement_terminator_skips");
	}

	@Test
	public void testPackageLineWithTrailingCommentRecognized() throws Exception {
		final var lines = setUpFixContext("package_line_with_trailing_comment_recognized", "Foo.java", "sibling_foo");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.bar.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testPackageWithInternalWhitespaceIsSanitized() throws Exception {
		final var lines = setUpFixContext("package_with_internal_whitespace_is_sanitized", "Foo.java", "sibling_foo");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.bar.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testParseAliasDotAtEofSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "parse_alias_dot_at_eof_skips", "initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved");
	}

	@Test
	public void testParseAliasEqualsAtEndOfLineSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "parse_alias_equals_at_end_of_line_skips", "initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved");
	}

	@Test
	public void testParseAliasNoEqualsReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "parse_alias_no_equals_returns_skip");
	}

	@Test
	public void testRenamedAliasWithUnparseableFileBailsConservatively() throws Exception {
		assertSkipResult(
				fixer,
				TOPIC,
				"renamed_alias_with_unparseable_file_bails_conservatively",
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("potential shadow (file does not parse cleanly)")
		);
	}

	@Test
	public void testRewriteOutsideLiteralsPropagatesTextBlockStateAcrossLines() {
		final var modified = PreferStaticImportConstantFixer.rewriteOutsideLiterals(
				List.of(
						"String s = \"\"\"",
						"Bar.FOO",
						"\"\"\";",
						"int x = Bar.FOO;"
				),
				-1,
				-1,
				List.of("Bar.FOO"),
				"FOO"
		);
		assertFalse(modified.containsKey(1));
		assertEquals("int x = FOO;", modified.get(3));
	}

	@Test
	public void testRhsDotFollowedByLiteralReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "rhs_dot_followed_by_literal_returns_skip", "initializer is not a SimpleClass.IDENT alias or FQCN cannot be resolved");
	}

	@Test
	public void testRhsExcessClosingParensReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "rhs_excess_closing_parens_returns_null");
	}

	@Test
	public void testRhsMismatchedParensReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "rhs_mismatched_parens_returns_null");
	}

	@Test
	public void testSamePackageResolutionUsesSiblingFile() throws Exception {
		final var lines = setUpFixContext("same_package_resolution_uses_sibling_file", "Helper.java", "same_package_helper");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static x.y.Helper.MAX"), result.importsToAdd());
	}

	@Test
	public void testSamePackageResolutionWithLeadingBomUsesSiblingFile() throws Exception {
		final var lines = setUpFixContext("same_package_resolution_with_leading_bom_uses_sibling_file", "Helper.java", "same_package_helper");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 26));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static x.y.Helper.MAX"), result.importsToAdd());
	}

	@Test
	public void testSiblingWinsOverWildcard() throws Exception {
		final var lines = setUpFixContext("sibling_wins_over_wildcard", "Foo.java", "sibling_foo");
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static x.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testStaticBlockHasCommentsHandlesNegativeEndColPlusOne() {
		assertTrue(PreferStaticImportConstantFixer.staticBlockHasComments(
				List.of("nonblank"),
				0,
				0,
				0,
				-2,
				-1,
				0,
				0
		));
	}

	@Test
	public void testStaticBlockHasCommentsHandlesNegativeStartCol() {
		assertFalse(PreferStaticImportConstantFixer.staticBlockHasComments(
				List.of(""),
				0,
				0,
				-1,
				-1,
				-1,
				0,
				0
		));
	}

	@Test
	public void testWildcardPrefixEndsWithDotSkipped() throws Exception {
		assertSimpleFix(fixer, TOPIC, "wildcard_prefix_ends_with_dot_skipped", Set.of("static good.Foo.X"));
	}

	@Test
	public void testWildcardPrefixStartsWithDotSkipped() throws Exception {
		assertSimpleFix(fixer, TOPIC, "wildcard_prefix_starts_with_dot_skipped", Set.of("static good.Foo.X"));
	}
}