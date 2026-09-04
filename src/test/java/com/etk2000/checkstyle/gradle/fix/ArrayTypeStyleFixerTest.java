package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertEachEventFixesCase;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertEachEventFixesOnlyItsOwnLine;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertEachEventSkips;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.ArrayTypeStyleCheck;
import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class ArrayTypeStyleFixerTest {
	private static final String BS = "\\";
	private static final String POSITIONS = "InputLocatorPositions.java";
	private static final String SKIP_ANNOTATION = "cannot move the brackets: a type-use annotation precedes them";
	private static final String SKIP_MULTI_VAR = "cannot move the brackets: multi-variable declaration";
	private static final String SKIP_NOT_DECLARATOR = "cannot move the brackets: the reported column is not on a declaration's brackets";
	private static final String SKIP_TEXT_BLOCK = "cannot move the brackets: a possible text block prevents scanning the declaration";
	private static final String SKIP_UNICODE = "cannot move the brackets: the name is spelled with a unicode escape";
	private static final String SKIP_UNPARSEABLE = "cannot move the brackets: the file could not be parsed to confirm the declaration";
	private static final String TOPIC = "arraytypestyle";

	/** Slices whose name, respelled as a unicode escape, the fixer refuses, with the reason. */
	@Nonnull
	private static Stream<Arguments> escapedNames() {
		return Stream.of(
				Arguments.of("annotated_base_dimension_field", "x", SKIP_ANNOTATION),
				Arguments.of("comma_after_brackets_in_variable_decl", "x", SKIP_MULTI_VAR),
				Arguments.of("generic_method_multi_param_lambda", "a", SKIP_UNICODE),
				Arguments.of("local_simple", "lb", SKIP_UNICODE),
				Arguments.of("multi_line_simple_var", "x", SKIP_UNICODE),
				Arguments.of("multi_var_mixed_depth_in_call_argument_lambda", "alpha", SKIP_UNICODE),
				Arguments.of("parameter_c_style", "x", SKIP_UNICODE),
				Arguments.of("record_component_c_style", "x", SKIP_UNICODE),
				Arguments.of("simple_c_style", "field", SKIP_UNICODE)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> linesOf(@Nonnull String source) throws Exception {
		return source.equals(POSITIONS)
				? TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + TOPIC + "/" + POSITIONS)
				: strippedSlice(source);
	}

	/** Brackets the check never reports, each named by the start of its line and the text ending at the {@code [}. */
	@Nonnull
	private static Stream<Arguments> nonDeclaratorBrackets() {
		return Stream.of(
				Arguments.of(POSITIONS, "Class<?> type", "int["),
				Arguments.of(POSITIONS, "IntFunction<int[]> factory", "<int["),
				Arguments.of(POSITIONS, "IntFunction<int[]> factory", "= int["),
				Arguments.of(POSITIONS, "Map<String, int[]>", "int["),
				Arguments.of(POSITIONS, "String literal", "\"int a["),
				Arguments.of(POSITIONS, "final Byte bytes[]", "(Byte["),
				Arguments.of(POSITIONS, "int a[] = x;", "a["),
				Arguments.of(POSITIONS, "int[]", "int["),
				Arguments.of(POSITIONS, "return o instanceof", "int["),
				Arguments.of(POSITIONS, "void varargs(", "int["),
				Arguments.of(POSITIONS, "// int a[]", "a["),
				Arguments.of("compound_c_style", "int x[][]", "x[]["),
				Arguments.of("mixed_java_and_c_style", "int[] x[]", "int["),
				Arguments.of("multi_var_same_depth_two_dim", "int alpha[][]", "alpha[][")
		);
	}

	@Nonnull
	private static Stream<Arguments> refusedGroups() {
		return Stream.of(
				Arguments.of("main", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_base_type_annotated_brackets", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_base_type_brackets_first_only", List.of(SKIP_MULTI_VAR)),
				Arguments.of("multi_var_base_type_brackets_last_only", List.of(SKIP_MULTI_VAR)),
				Arguments.of("multi_var_base_type_brackets_same_depth", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_gap_annotation_between_sibling_bracket_pairs", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_gap_annotation_on_own_brackets", List.of(SKIP_ANNOTATION, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_gap_annotation_on_sibling_brackets", List.of(SKIP_MULTI_VAR, SKIP_ANNOTATION)),
				Arguments.of("multi_var_gap_comment_inside_brackets", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_gap_comment_own", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_gap_comment_sibling", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_for_init", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_in_call_argument_lambda", List.of(SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_none_then_two", List.of(SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_one_then_two", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_three_first", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_three_last", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_three_middle", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_three_none_in_middle", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_two_then_none", List.of(SKIP_MULTI_VAR)),
				Arguments.of("multi_var_mixed_depth_two_then_one", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_own_brackets_on_next_line", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_own_last_bracket_pair_on_next_line", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR)),
				Arguments.of("multi_var_sibling_brackets_on_next_line", List.of(SKIP_MULTI_VAR, SKIP_MULTI_VAR))
		);
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> strippedSlice(@Nonnull String caseName) throws Exception {
		final var input = String.join("\n", TestResources.loadCaseSlice(TOPIC, caseName).inputLines());
		return List.of(FullPipelineRunner.stripViolationComments(input).split("\n", -1));
	}

	/** {@code lines} with the first whole-word {@code name} respelled as the unicode escape of {@code a}. */
	@CheckReturnValue
	@Nonnull
	private static List<String> withEscapedName(@Nonnull List<String> lines, @Nonnull String name) {
		final var respelled = String.join("\n", lines).replaceFirst("\\b" + name + "\\b", Matcher.quoteReplacement(BS + "u0061"));
		return List.of(respelled.split("\n", -1));
	}

	private final CheckstyleFixer fixer = new ArrayTypeStyleFixer();

	@Test
	public void testAnnotatedBaseDimensionUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "annotated_base_dimension_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testBaseTypeBracketsOnAnEarlierLineDoNotFix() throws Exception {
		final var lines = TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + TOPIC + "/InputLocatorPositions.java");
		final var line = lines.stream().filter(text -> text.contains("alpha[")).findFirst().map(lines::indexOf).orElseThrow();
		assertNull(fixer.fix(lines, line, lines.get(line).indexOf('[')));
		assertEquals(new SkipResult(SKIP_MULTI_VAR), fixer.fix(lines, line, lines.get(line).lastIndexOf('[')));
	}

	@Test
	public void testBlockCommentAfterMethodReturnBracketsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "block_comment_after_method_return_brackets_returns_null");
	}

	@Test
	public void testBracketAtLineStartReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "bracket_at_line_start_returns_null");
	}

	@Test
	public void testCallArgumentLambdaInitializedMultiVarUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_initialized_multi_var_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCallArgumentLambdaInitializerClosesBlockUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_initializer_closes_block_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCallArgumentLambdaMultiVarUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_multi_var_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCallArgumentLambdaSameDepthUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_same_depth_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCallArgumentLambdaSingleVarUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_single_var_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCallArgumentLambdaThreeDeclaratorsUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "call_argument_lambda_three_declarators_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCastLineDeclaratorStillFixes() throws Exception {
		final var lines = linesOf(POSITIONS);
		final var line = lines.stream().filter(text -> text.strip().startsWith("final Byte bytes[]")).findFirst().map(lines::indexOf).orElseThrow();
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, line, lines.get(line).indexOf("bytes[") + "bytes".length()));
		assertEquals(List.of(lines.get(line).replace("Byte bytes[]", "Byte[] bytes")), result.replacement());
	}

	@Test
	public void testCastUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "cast_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testCatchKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "catch_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testDoKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "do_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"multi_var_same_depth_anonymous_class_field",
			"multi_var_same_depth_array_valued_annotation",
			"multi_var_same_depth_block_comment_spans_lines",
			"multi_var_same_depth_brace_init_with_commas",
			"multi_var_same_depth_call_init_with_commas",
			"multi_var_same_depth_cast_and_ternary_init",
			"multi_var_same_depth_char_init_with_comma",
			"multi_var_same_depth_comma_on_next_line",
			"multi_var_same_depth_comment_after_comma",
			"multi_var_same_depth_comment_after_type",
			"multi_var_same_depth_comment_between_declarators",
			"multi_var_same_depth_declaration_annotation",
			"multi_var_same_depth_declarators_on_two_lines",
			"multi_var_same_depth_enum_field",
			"multi_var_same_depth_field",
			"multi_var_same_depth_for_init",
			"multi_var_same_depth_for_init_call_with_commas",
			"multi_var_same_depth_generic",
			"multi_var_same_depth_in_call_argument_lambda",
			"multi_var_same_depth_index_init",
			"multi_var_same_depth_initializer_spans_lines",
			"multi_var_same_depth_interface_constant",
			"multi_var_same_depth_lambda_body_local",
			"multi_var_same_depth_local_array_init",
			"multi_var_same_depth_method_ref_init",
			"multi_var_same_depth_mixed_init",
			"multi_var_same_depth_nested_generic",
			"multi_var_same_depth_qualified_generic",
			"multi_var_same_depth_record_static_field",
			"multi_var_same_depth_space_between_bracket_pairs",
			"multi_var_same_depth_static_final_constant",
			"multi_var_same_depth_static_initializer_local",
			"multi_var_same_depth_string_init_with_comma",
			"multi_var_same_depth_supplementary_char_before_type",
			"multi_var_same_depth_switch_rule_local",
			"multi_var_same_depth_tab_between_type_and_name",
			"multi_var_same_depth_text_block_in_first_init",
			"multi_var_same_depth_text_block_in_last_init",
			"multi_var_same_depth_three_declarators",
			"multi_var_same_depth_trailing_line_comment",
			"multi_var_same_depth_two_dim",
			"multi_var_same_depth_two_dim_nested_brace_init",
			"multi_var_same_depth_type_args_with_brackets",
			"multi_var_same_depth_type_on_previous_line",
			"multi_var_same_depth_type_use_annotation_in_type_args",
			"multi_var_same_depth_whitespace_before_brackets",
			"multi_var_same_depth_whitespace_inside_first_brackets",
			"multi_var_same_depth_whitespace_inside_later_brackets",
			"multi_var_same_depth_wildcard_generic"
	})
	public void testEachEventFixesTheWholeGroup(@Nonnull String caseName) throws Exception {
		assertEachEventFixesCase(ArrayTypeStyleCheck.class, fixer, TOPIC, caseName);
	}

	@Test
	public void testEachEventOfAdjacentDeclarationsFixesOnlyItsOwnLine() throws Exception {
		assertEachEventFixesOnlyItsOwnLine(ArrayTypeStyleCheck.class, fixer, TOPIC, "multi_var_same_depth_adjacent_declarations");
	}

	@MethodSource("refusedGroups")
	@ParameterizedTest
	public void testEachEventOfARefusedGroupSkips(@Nonnull String caseName, @Nonnull List<String> reasons) throws Exception {
		assertEachEventSkips(ArrayTypeStyleCheck.class, fixer, TOPIC, caseName, reasons);
	}

	@Test
	public void testEscapedBackslashAtEolInStringSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "escaped_backslash_at_eol_in_string_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testEscapedNameInAUniformGroupFixes() throws Exception {
		final var lines = withEscapedName(strippedSlice("multi_var_same_depth_field"), "alpha");
		final var expected = withEscapedName(TestResources.loadCaseSlice(TOPIC, "multi_var_same_depth_field").fixedLines(), "alpha");
		final var events = BaseCheckTest.runCheckInline(ArrayTypeStyleCheck.class, String.join("\n", lines));
		assertEquals(2, events.size());
		for (var event : events) {
			final var buffer = new ArrayList<>(lines);
			final var lineIndex = event.getLine() - 1;
			final var column = CheckstyleFixAction.eventCharIndex(buffer.get(lineIndex), event);
			FixerTestUtil.applyFixResult(buffer, assertInstanceOf(FixResult.class, fixer.fix(buffer, lineIndex, column)));
			assertEquals(expected, buffer);
		}
	}

	@MethodSource("escapedNames")
	@ParameterizedTest
	public void testEscapedNameSkips(@Nonnull String caseName, @Nonnull String name, @Nonnull String reason) throws Exception {
		final var lines = withEscapedName(strippedSlice(caseName), name);
		final var event = BaseCheckTest.runCheckInline(ArrayTypeStyleCheck.class, String.join("\n", lines)).getFirst();
		final var lineIndex = event.getLine() - 1;
		final var column = CheckstyleFixAction.eventCharIndex(lines.get(lineIndex), event);
		assertEquals(new SkipResult(reason), fixer.fix(new ArrayList<>(lines), lineIndex, column));
	}

	@Test
	public void testExpressionContextNotMethodReturnReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "expression_context_not_method_return_returns_null");
	}

	@Test
	public void testFakeIdentAfterMethodReturnBracketsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "fake_ident_after_method_return_brackets_returns_null");
	}

	@Test
	public void testFieldAccessNotMethodReturnReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "field_access_not_method_return_returns_null");
	}

	@Test
	public void testFieldInitCallEndsInParenMultiVarSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "field_init_call_ends_in_paren_multi_var_skips", SKIP_MULTI_VAR);
	}

	@Test
	public void testGenericRecordMultiComponentSkips() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSkipResult(fixer, TOPIC, "generic_record_multi_component_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testGenericRecordMultiTypeParamSkips() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSkipResult(fixer, TOPIC, "generic_record_multi_type_param_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testGenericRecordNestedTypeBoundsSkips() throws Exception {
		// can't migrate: Checkstyle parser rejects C-style array brackets in record components when the record has type parameters
		assertSkipResult(fixer, TOPIC, "generic_record_nested_type_bounds_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testIfKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "if_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testMethodIdentAtLineStartReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_ident_at_line_start_returns_null");
	}

	@Test
	public void testMethodReturnFollowedByPartialThrowsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_return_followed_by_partial_throws_returns_null");
	}

	@Test
	public void testMethodReturnFollowedByThrowsLikeIdentReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "method_return_followed_by_throws_like_ident_returns_null");
	}

	@Test
	public void testMethodReturnWithTypeUseAnnotationSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "method_return_with_type_use_annotation_skips", SKIP_ANNOTATION);
	}

	@Test
	public void testMultiLineBracketsUnclosedReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_brackets_unclosed_returns_null");
	}

	@Test
	public void testMultiLineDeclEndingAtClosingBraceUnparseableSkips() throws Exception {
		// can't migrate: the declaration is terminated by a bare '}' with no ';', so class-wrapping yields a field declaration missing its semicolon (unparseable)
		assertSkipResult(fixer, TOPIC, "multi_line_decl_ending_at_closing_brace_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testMultiLineEofWithoutTerminatorSkips() throws Exception {
		// can't migrate: the declaration has no terminator (ends at EOF after '[]'), so class-wrapping yields a field declaration missing its semicolon (unparseable)
		assertSkipResult(fixer, TOPIC, "multi_line_eof_without_terminator_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testMultiLineFirstLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_first_line_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInBraceReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_brace_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInPermitsReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_permits_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInSemicolonReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_semicolon_returns_null");
	}

	@Test
	public void testMultiLinePrevLineEndsInThrowsIdent() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_prev_line_ends_in_throws_ident");
	}

	@Test
	public void testMultiLineSuperBlacklistReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_super_blacklist_returns_null");
	}

	@Test
	public void testMultiLineTextBlockOnBracketLineSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_text_block_on_bracket_line_skips", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testMultiLineTextBlockOnLaterLineSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_text_block_on_later_line_skips", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testMultiLineUnterminatedStringDoesNotEatCommaOnNextLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_line_unterminated_string_does_not_eat_comma_on_next_line", SKIP_MULTI_VAR);
	}

	@Test
	public void testMultiLineWithEmptyPreviousLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_with_empty_previous_line_returns_null");
	}

	@Test
	public void testMultiLineWithWhitespaceOnlyPrevLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "multi_line_with_whitespace_only_prev_line_returns_null");
	}

	@Test
	public void testMultiVarSameDepthUnparseableSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "multi_var_same_depth_unparseable_skips", SKIP_MULTI_VAR);
	}

	@MethodSource("nonDeclaratorBrackets")
	@ParameterizedTest
	public void testNonDeclaratorBracketSkips(@Nonnull String source, @Nonnull String lineStart, @Nonnull String needle) throws Exception {
		final var lines = linesOf(source);
		final var line = lines.stream().filter(text -> text.strip().startsWith(lineStart)).findFirst().map(lines::indexOf).orElseThrow();
		assertEquals(new SkipResult(SKIP_NOT_DECLARATOR), fixer.fix(lines, line, lines.get(line).indexOf(needle) + needle.length() - 1));
	}

	@Test
	public void testNoSuffixReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "no_suffix_returns_null");
	}

	@Test
	public void testOpenParenAtLineStartUnparseableSkips() throws Exception {
		// can't migrate: snippet `(int x[])` is not parseable Java when class-wrapped
		assertSkipResult(fixer, TOPIC, "open_paren_at_line_start_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testOrphanCloseParenUnparseableSkips() throws Exception {
		// can't migrate: snippet `) int x[];` starts with an orphan close paren and is not parseable Java when class-wrapped
		assertSkipResult(fixer, TOPIC, "orphan_close_paren_unparseable_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testOrphanCloseParenWithoutMatchReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "orphan_close_paren_without_match_returns_null");
	}

	@Test
	public void testParamListUnparseableAnnotatedNextParamSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "param_list_unparseable_annotated_next_param_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testParamListUnparseableCommaAtLineEndSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "param_list_unparseable_comma_at_line_end_skips", SKIP_UNPARSEABLE);
	}

	@Test
	public void testPermitsKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "permits_keyword_single_line_returns_null");
	}

	/**
	 * A tree cached for an earlier text of the same buffer at the same version, which
	 * {@link CheckstyleFixAction#applyFixes} never produces but a caller could: the group's later
	 * declarator then sits past the end of the shortened line, where its position resolves to no char.
	 */
	@Test
	public void testStaleTreeWithADeclaratorPastTheLineEndSkips() throws Exception {
		final var lines = new ArrayList<>(strippedSlice("multi_var_same_depth_field"));
		final var shorter = strippedSlice("simple_c_style").stream().filter(text -> text.contains("field[")).findFirst().orElseThrow();
		final var line = lines.stream().filter(text -> text.contains("alpha[")).findFirst().map(lines::indexOf).orElseThrow();
		FixContext.setBufferVersion(1);
		try {
			assertInstanceOf(FixResult.class, fixer.fix(lines, line, lines.get(line).indexOf('[')));
			lines.set(line, shorter);
			assertEquals(new SkipResult(SKIP_MULTI_VAR), fixer.fix(lines, line, shorter.indexOf('[')));
		}
		finally {
			FixContext.clearBufferVersion();
			FixerAst.clearCache();
		}
	}

	@Test
	public void testSuperKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "super_keyword_single_line_returns_null");
	}

	@Test
	public void testSwitchKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "switch_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testSynchronizedKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "synchronized_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testTextBlockAfterBracketSameLine() throws Exception {
		assertSkipResult(fixer, TOPIC, "text_block_after_bracket_same_line", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testTextBlockOpenerAfterBrackets() throws Exception {
		assertSkipResult(fixer, TOPIC, "text_block_opener_after_brackets", SKIP_TEXT_BLOCK);
	}

	@Test
	public void testThrowsKeywordSingleLineReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "throws_keyword_single_line_returns_null");
	}

	@Test
	public void testTryKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "try_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testTypeUseAnnotationInParameterSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "type_use_annotation_in_parameter_skips", SKIP_ANNOTATION);
	}

	@Test
	public void testTypeUseAnnotationInRecordSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "type_use_annotation_in_record_skips", SKIP_ANNOTATION);
	}

	@Test
	public void testUnclosedBracketReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "unclosed_bracket_returns_null");
	}

	@Test
	public void testWhileKeywordNotTreatedAsParamList() throws Exception {
		assertSkipResult(fixer, TOPIC, "while_keyword_not_treated_as_param_list", SKIP_MULTI_VAR);
	}

	@Test
	public void testWithInvalidSuffixReturnsNull() throws Exception {
		assertSkip(fixer, TOPIC, "with_invalid_suffix_returns_null");
	}
}