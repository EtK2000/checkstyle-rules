package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.ScalingAssertions.assertScalesLinearly;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.FieldConsolidationCheck;
import com.etk2000.checkstyle.LineColumns;
import com.etk2000.checkstyle.SourceComments;
import com.etk2000.checkstyle.ast.DocComments;
import com.etk2000.checkstyle.gradle.fix.FixerAst.AstFunction;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Isolated
public class FieldConsolidationFixerTest {
	private static final int LONG_RUN = 2000, MAX_WIDTH = 120;
	private static final String DECLARATION_START = "private int ";
	private static final String DIRECTIVE_REASON = "cannot consolidate: a comment that has to move above the merged declaration is a tool directive acting on its own line";
	private static final String STALE_REASON = "cannot consolidate: the declaration was already merged earlier in this pass";
	private static final String TOPIC = "fieldconsolidation";
	private static final String UNVERIFIED_REASON = "cannot consolidate: the merge would change a field, a comment or the code around them";
	private static final String WIDENED_REASON = "cannot consolidate: a tool directive acting on its own line would end up on a line holding names it was not written for";

	/**
	 * The names a merged {@code private int} declaration declares, in order, after checking that
	 * none of its lines is wider than the limit with tabs at four columns.
	 */
	private static List<String> declaredNames(List<String> replacement) {
		assertEquals(List.of(), replacement.stream().filter(line -> line.replace("\t", "    ").length() > MAX_WIDTH).toList());
		final var joined = String.join(" ", replacement).strip();
		assertTrue(joined.startsWith(DECLARATION_START) && joined.endsWith(";"), joined);
		final var names = new ArrayList<String>();
		for (var declarator : joined.substring(DECLARATION_START.length(), joined.length() - 1).split(","))
			names.add(declarator.strip().split(" ")[0]);
		return names;
	}

	private static List<String> inClass(String... body) {
		final var lines = new ArrayList<String>();
		lines.add("class T {");
		for (var line : body)
			lines.add("\t" + line);
		lines.add("}");
		return lines;
	}

	// continuation lines are space-indented to keep two tabs out of an inline string
	static Stream<Arguments> unparseableProvider() {
		return Stream.of(
				Arguments.of(List.of("\tint alpha;", "\t/* separator */", "\tint beta;"), 2, 5),
				Arguments.of(List.of("\tint alpha;", "\t// separator comment", "\tint beta;"), 2, 5),
				Arguments.of(List.of("\tint alpha;", "\t/** Javadoc for beta */", "\tint beta;"), 2, 5),
				Arguments.of(List.of("\tint alpha;", "\t/**", "\t * Javadoc for beta.", "\t */", "\tint beta;"), 4, 5),
				Arguments.of(List.of("\tint alpha[];", "\tint[] beta;"), 1, 4),
				Arguments.of(List.of("\tint alpha;", "\tint beta;"), 1, 10),
				Arguments.of(List.of("\tint alpha;", "\tint beta;"), 1, 50),
				Arguments.of(List.of("\tint alpha;", "\tint beta;"), 1, 5),
				Arguments.of(List.of("\tint alpha,", "\tbeta,", "\tgamma;"), 1, 1),
				Arguments.of(List.of("\tint /* , */ alpha,", "        beta;"), 1, 8),
				Arguments.of(List.of("\tint alpha;", "\tint beta, gamma", "        = 10;"), 1, 5),
				Arguments.of(List.of("int[] alpha[];", "int[][] beta;"), 1, 8),
				Arguments.of(List.of("\tint alpha;", "\tint beta, gamma = 10;"), 1, 5),
				Arguments.of(List.of("\tint alpha;", "\tint beta, gamma = 10,", "        delta;"), 1, 5),
				Arguments.of(List.of("\tint alpha;", "\tint beta,", "        gamma = 10;"), 1, 5),
				Arguments.of(List.of("\tint alpha;", "\tint beta,", "        gamma = 10,", "        delta;"), 1, 5),
				Arguments.of(List.of("\tint alpha;", "\tint beta, gamma", "        = alpha;"), 1, 5),
				Arguments.of(List.of("\tint alpha[] /* legacy */,", "\tbeta[];"), 1, 1),
				Arguments.of(List.of("\tint alpha, beta[];", "\tint[] zebra;"), 1, 7),
				Arguments.of(List.of("\tint alpha[], beta[][];", "\tint[][] zebra;"), 1, 9),
				Arguments.of(List.of("\tint alpha @A [];", "\tint @A [] beta;"), 1, 11),
				Arguments.of(List.of("\tint @A [] alpha[];", "\tint @A [][] beta;"), 1, 13),
				Arguments.of(List.of("\tint [] @A(x = 1) [] alpha[];", "\tint [] @A(x = 1) [][] beta;"), 1, 23),
				Arguments.of(List.of("\tint @ A [] alpha[];", "\tint @ A [][] beta;"), 1, 14),
				Arguments.of(List.of("\tint @a.A [] alpha[];", "\tint @a.A [][] beta;"), 1, 15),
				Arguments.of(List.of("// foo;", "int beta;"), 1, 4),
				Arguments.of(List.of("\tint alpha;", "\tint beta; int gamma;"), 1, 9)
		);
	}

	private final CheckstyleFixer fixer = new FieldConsolidationFixer();

	@Test
	public void testBomAboveTheRunKeptAhead() {
		final var lines = List.of((char) 0xFEFF + "class T { }", "class U {", "\tint alpha;", "\tint beta;", "}");
		assertEquals(new FixResult(2, 3, List.of("\tint alpha, beta;")), fixer.fix(lines, 3, 5));
	}

	@Test
	public void testBomBufferMerges() {
		final var lines = inClass("int alpha;", "int beta;");
		lines.set(0, (char) 0xFEFF + lines.getFirst());
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(lines, 2, 5));
	}

	@Test
	public void testColumnMidIdentifierFindsTheDeclarator() {
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(inClass("int alpha;", "int beta;"), 2, 6));
	}

	@Test
	public void testColumnOnLaterNameOfLaterDeclarationFindsTheRun() {
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta, gamma;")), fixer.fix(inClass("int alpha;", "int beta, gamma;"), 2, 11));
	}

	@Test
	public void testColumnOutOfRangeFindsTheLinesDeclarator() {
		final var lines = inClass("int alpha;", "int beta;");
		final var expected = new FixResult(1, 2, List.of("\tint alpha, beta;"));
		assertEquals(expected, fixer.fix(lines, 2, -1));
		assertEquals(expected, fixer.fix(lines, 2, -100));
		assertEquals(expected, fixer.fix(lines, 2, 10_000));
	}

	@Test
	public void testColumnShiftedBySiblingFix() {
		// ArrayTypeStyle rewrote `int beta[];` on the event's line first, so the name moved two columns on
		assertEquals(new FixResult(1, 2, List.of("\tint[] alpha, beta;")), fixer.fix(inClass("int[] alpha;", "int[] beta;"), 2, 5));
	}

	@Test
	public void testDirectiveAtColumnZeroAheadOfFirstDeclarationIsWidened() {
		assertEquals(new SkipResult(WIDENED_REASON), fixer.fix(List.of("class T {", "/* NOSONAR */ int alpha;", "int beta;", "}"), 2, 4));
	}

	@Test
	public void testEventOnConstantIsStale() {
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(inClass("static int alpha;", "static final int LIMIT = 5;", "static int gamma;"), 2, 18));
	}

	@Test
	public void testEventOnFirstOfRunIsStale() {
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(inClass("int alpha;", "int beta;"), 1, 5));
	}

	@Test
	public void testEventOnLineWithoutFieldIsNotFixable() {
		assertNull(fixer.fix(inClass("int alpha;", "int beta;", "void m() {", "}"), 3, 1));
	}

	@Test
	public void testEventOnLongDeclarationIsStale() {
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(inClass("int alpha;", "int beta = 1_000_000 + 2_000_000 + 3_000_000;", "int gamma;"), 2, 5));
	}

	@Test
	public void testEventOnMergedDeclarationIsStale() {
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(inClass("int alpha, beta, gamma;"), 1, 12));
	}

	@Test
	public void testFieldLinesOfAnotherBufferAreFoundAfresh() {
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(inClass("int alpha;", "int beta;"), 2, 5));
		assertEquals(new FixResult(1, 2, List.of("\tlong delta, gamma;")), fixer.fix(inClass("long gamma;", "long delta;"), 2, 6));
	}

	@Test
	public void testFixOnOwnOutputIsStale() {
		final var merged = inClass("int alpha, beta, gamma;");
		for (var column = 0; column < merged.get(1).length(); ++column)
			assertEquals(new SkipResult(STALE_REASON), fixer.fix(merged, 1, column));
	}

	@Test
	public void testFixOnOwnOutputWithLongSiblingIsStale() {
		final var merged = inClass("int alpha, gamma;", "int beta = 1_000_000 + 2_000_000 + 3_000_000;");
		for (var line = 1; line <= 2; ++line) {
			for (var column = 0; column < merged.get(line).length(); ++column)
				assertEquals(new SkipResult(STALE_REASON), fixer.fix(merged, line, column));
		}
	}

	@Test
	public void testFoldStartsAtEarlierRun() {
		assertEquals(
				new FixResult(1, 4, List.of("\tString first, second;", "\tint alpha, beta;")),
				fixer.fix(inClass("String first;", "int alpha;", "String second;", "int beta;"), 4, 5)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"array_type_both_c_style_no_semicolon_on_violation",
			"comma_merge_last_field",
			"comma_merge_no_terminator_on_violation",
			"comma_merge_through_annotation",
			"comma_merge_through_annotation_with_inner_comma",
			"comma_merge_with_line_comment",
			"comma_merge_wraps",
			"continuation_loop_exhausts_lines",
			"continuation_stops_at_comment_no_comma_on_violation",
			"line_index_zero",
			"merge_ignores_trailing_comma_before_semicolon",
			"no_previous_semicolon",
			"prev_line_block_comment_unclosed",
			"violation_column_forward_scan_exhausts_line",
			"violation_line_block_comment_post_name_no_semicolon",
			"violation_line_block_comment_unclosed",
			"violation_line_without_semicolon",
			"wrap_continuation_breaks_at_no_ident_line",
			"wrap_continuation_breaks_at_same_indent"
	})
	void testFragmentNotFixable(String fragment) throws Exception {
		assertSkip(fixer, TOPIC, fragment);
	}

	@Test
	public void testInterleavedDirectiveRunStaysAsWritten() {
		assertEquals(
				new FixResult(1, 3, List.of("\tint alpha, beta;", "\tString first;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta;", "String second; // NOSONAR"), 3, 5)
		);
	}

	@Test
	public void testInterleavedRefusedRunStaysAsWritten() {
		assertEquals(
				new FixResult(1, 3, List.of("\tint alpha, beta;", "\tString first;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta;", "String second; /* spans", "two lines */"), 3, 5)
		);
	}

	@Test
	public void testInterleavedRunsFoldFromLaterEvent() {
		assertEquals(
				new FixResult(1, 4, List.of("\tint alpha, beta;", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta;", "String second;"), 3, 5)
		);
	}

	@Test
	public void testInterleavedRunsFoldFromOtherRunsEvent() {
		assertEquals(
				new FixResult(1, 4, List.of("\tint alpha, beta;", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta;", "String second;"), 4, 8)
		);
	}

	@Test
	public void testInterleavedRunsFoldWithLongInSpanFromOtherRunsEvent() {
		assertEquals(
				new FixResult(1, 5, List.of("\tint alpha, gamma;", "\tString first, second;", "\tint beta = 1_000_000 + 2_000_000 + 3_000_000;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta = 1_000_000 + 2_000_000 + 3_000_000;", "int gamma;", "String second;"), 5, 8)
		);
	}

	@Test
	public void testInterleavedRunsWithBlankLines() {
		assertEquals(
				new FixResult(1, 6, List.of("\tint alpha, beta;", "\t", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "", "String first;", "int beta;", "", "String second;"), 4, 5)
		);
	}

	@Test
	public void testInterleavedRunsWithCarriedBlockComment() {
		final var lines = inClass("/* legacy", " */ int alpha;", "String first;", "int /* m */ beta;", "String second;");
		final var expected = new FixResult(1, 5, List.of("\t// m", "\t/* legacy", "\t */ int alpha, beta;", "\tString first, second;"));
		assertEquals(expected, fixer.fix(lines, 4, 12));
		assertEquals(expected, fixer.fix(lines, 5, 8));
	}

	@Test
	public void testInterleavedRunsWithMovedComments() {
		assertEquals(
				new FixResult(1, 4, List.of("\t// b", "\tint alpha, beta;", "\t// s", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta; // b", "String second; // s"), 3, 5)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"", " = 1"})
	public void testInterleavedUnverifiedRunsAnswerEveryEventInLinearTime(String initializer) throws Exception {
		assertScalesLinearly(
				250,
				2000,
				size -> {
					final var body = new ArrayList<>(List.of("int a0" + initializer + "; // \\u000a", "String s0" + initializer.replace("1", "\"1\"") + ";"));
					for (var i = 1; i < size; ++i) {
						body.add("int a" + i + initializer + ";");
						body.add("String s" + i + initializer.replace("1", "\"1\"") + ";");
					}
					return inClass(body.toArray(String[]::new));
				},
				(size, lines) -> timeEveryEvent(lines, 3, line -> line % 2 == 1 ? 5 : 8)
		);
	}

	@Test
	public void testLineIndexOutsideTheBufferIsNotFixable() {
		final var lines = inClass("int alpha;", "int beta;");
		assertNull(fixer.fix(lines, -1, 5));
		assertNull(fixer.fix(lines, lines.size(), 5));
	}

	@Test
	public void testLongCommentedRunIsOneRewrite() {
		final var body = new String[LONG_RUN];
		for (var i = 0; i < LONG_RUN; ++i)
			body[i] = "private int f" + i + "; // c" + i;
		final var result = assertInstanceOf(FixResult.class, assertTimeoutPreemptively(Duration.ofSeconds(2), () -> fixer.fix(inClass(body), LONG_RUN, 13)));
		assertEquals(1, result.startLine());
		assertEquals(LONG_RUN, result.endLine());
		final var comments = new ArrayList<String>();
		for (var i = 1; i < LONG_RUN; ++i)
			comments.add("c" + i);
		assertEquals("\t// " + String.join(" ", comments), result.replacement().getFirst());
		final var declaration = new ArrayList<>(result.replacement().subList(1, result.replacement().size()));
		final var last = declaration.removeLast();
		assertTrue(last.endsWith("; // c0"), last);
		declaration.add(last.substring(0, last.length() - " // c0".length()));
		final var names = new ArrayList<String>();
		for (var i = 0; i < LONG_RUN; ++i)
			names.add("f" + i);
		names.sort(null);
		assertEquals(names, declaredNames(declaration));
	}

	@Test
	public void testLongDeclarationWithCommentBeforeSplitSemicolonStaysAsWritten() {
		assertEquals(
				new FixResult(1, 4, List.of("\tint alpha, beta;", "\tString first = \"x\" // note", "\t\t\t;")),
				fixer.fix(inClass("int alpha;", "String first = \"x\" // note", "\t\t;", "int beta;", "String second;"), 4, 5)
		);
	}

	@Test
	public void testLongDependentRunIsOneRewrite() {
		final var body = new String[LONG_RUN];
		body[0] = "private int f0;";
		for (var i = 1; i < LONG_RUN; ++i)
			body[i] = "private int f" + i + " = f" + (i - 1) + ";";
		final var result = assertInstanceOf(FixResult.class, assertTimeoutPreemptively(Duration.ofSeconds(2), () -> fixer.fix(inClass(body), LONG_RUN, 13)));
		assertEquals(1, result.startLine());
		assertEquals(LONG_RUN, result.endLine());
		final var expected = new ArrayList<String>();
		for (var i = 0; i < LONG_RUN; ++i)
			expected.add("f" + i);
		assertEquals(expected, declaredNames(result.replacement()));
	}

	@Test
	public void testLongRefusedRunAnswersEveryEventInLinearTime() {
		final var body = new String[LONG_RUN];
		for (var i = 0; i < LONG_RUN; ++i)
			body[i] = "public static T g" + i + " = new T(" + i + "); // NOSONAR";
		final var lines = inClass(body);
		final Executable everyEvent = () -> {
			for (var line = 2; line <= LONG_RUN; ++line)
				assertEquals(new SkipResult(WIDENED_REASON), fixer.fix(lines, line, 17));
		};
		assertTimeoutPreemptively(Duration.ofSeconds(5), everyEvent);
	}

	@Test
	public void testLongRunIsOneRewrite() {
		final var body = new String[LONG_RUN];
		for (var i = 0; i < LONG_RUN; ++i)
			body[i] = "private int f" + i + ";";
		final var result = assertInstanceOf(FixResult.class, assertTimeoutPreemptively(Duration.ofSeconds(2), () -> fixer.fix(inClass(body), LONG_RUN, 13)));
		assertEquals(1, result.startLine());
		assertEquals(LONG_RUN, result.endLine());
		final var names = declaredNames(result.replacement());
		assertEquals(LONG_RUN, names.size());
		assertEquals(Arrays.stream(body).map(line -> line.substring(DECLARATION_START.length(), line.length() - 1)).collect(Collectors.toSet()), new HashSet<>(names));
	}

	@Test
	public void testLongUnverifiedRunAnswersEveryEventInLinearTime() throws Exception {
		assertScalesLinearly(
				500,
				4000,
				size -> {
					final var body = new String[size];
					body[0] = "int a0; // \\u000a";
					for (var i = 1; i < size; ++i)
						body[i] = "int a" + i + ";";
					return inClass(body);
				},
				(size, lines) -> timeEveryEvent(lines, 2, line -> 5)
		);
	}

	/**
	 * Every declaration on one line, which UTF-16 text makes slow to index by column: a char past
	 * Latin-1 has the line held as UTF-16, and a supplementary one also sets its columns apart from
	 * its chars. Only the runs are timed, since the parse of so long a line is slow on its own.
	 */
	@ParameterizedTest
	@ValueSource(ints = {0x100, 0x1D465})
	public void testManyDeclarationsOnOneUtf16LineFindRunsInLinearTime(int codePoint) throws Exception {
		assertScalesLinearly(
				250,
				2000,
				size -> {
					final var line = new StringBuilder("\t");
					for (var i = 0; i < size; ++i)
						line.append("int f").append(i).append(" = ").append(i).append("; ");
					final var lines = List.of("class T {", line.append("// ").appendCodePoint(codePoint).toString(), "}");
					return Map.entry(lines, requireNonNull(FixerAst.withAst(lines, DocComments::tokenPositions)));
				},
				(size, input) -> {
					final var lines = input.getKey();
					// withAst turns a throw into a null, so the measurement comes out to be asserted on
					final AstFunction<Entry<List<List<List<DetailAST>>>, Long>> timedRuns = root -> {
						final var objBlock = root.getFirstChild().findFirstToken(TokenTypes.OBJBLOCK);
						final var docs = new DocComments(input.getValue(), new TreeSet<>());
						final var comments = SourceComments.scan(lines);
						final var start = System.nanoTime();
						final var runs = FieldConsolidationCheck.runs(objBlock, new LineColumns(lines), () -> docs, () -> comments);
						return Map.entry(runs, System.nanoTime() - start);
					};
					final var measured = requireNonNull(FixerAst.withAst(lines, timedRuns));
					assertEquals(List.of(), measured.getKey());
					return measured.getValue();
				}
		);
	}

	@Test
	public void testNestedRunFolds() {
		assertEquals(
				new FixResult(1, 4, List.of("\tint alpha, beta;", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "String first;", "String second;", "int beta;"), 4, 5)
		);
	}

	@Test
	public void testRefusalOfOneRunLeavesAnotherRunMergeable() {
		final var lines = inClass("int alpha;", "int beta; // NOSONAR", "void m() {", "}", "long gamma;", "long delta;");
		assertEquals(new SkipResult(DIRECTIVE_REASON), fixer.fix(lines, 2, 5));
		assertEquals(new FixResult(5, 6, List.of("\tlong delta, gamma;")), fixer.fix(lines, 6, 6));
	}

	@Test
	public void testRefusalsOfAnotherBufferAreFoundAfresh() {
		assertEquals(new SkipResult(DIRECTIVE_REASON), fixer.fix(inClass("int alpha;", "int beta; // NOSONAR"), 2, 5));
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(inClass("int alpha;", "int beta;"), 2, 5));
	}

	@Test
	public void testRefusalsOfTheSameListAreFoundAfreshAfterAnInPlaceEdit() {
		final var lines = inClass("int alpha;", "int beta; // NOSONAR");
		assertEquals(new SkipResult(DIRECTIVE_REASON), fixer.fix(lines, 2, 5));
		lines.set(2, "\tint beta;");
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(lines, 2, 5));
	}

	@Test
	public void testRunsOfAnotherClassBodyAreFoundAfresh() {
		final var lines = List.of("class T {", "\tint alpha;", "\tint beta;", "}", "class U {", "\tlong gamma;", "\tlong delta;", "}");
		assertEquals(new FixResult(1, 2, List.of("\tint alpha, beta;")), fixer.fix(lines, 2, 5));
		assertEquals(new FixResult(5, 6, List.of("\tlong delta, gamma;")), fixer.fix(lines, 6, 6));
	}

	/**
	 * Only the fix pass is timed, over a parse made beforehand: checkstyle's own parse of so long a
	 * class grows about four times per doubling.
	 */
	@Test
	public void testRunsRefusedByLineCountsScaleLinearly() throws Exception {
		assertScalesLinearly(
				125,
				1000,
				size -> {
					final var lines = new ArrayList<>(List.of("class T {"));
					for (var i = 0; i < size; ++i) {
						final var n = String.format(Locale.ROOT, "%06d", i);
						lines.addAll(List.of("\tint m" + n + "() {", "\t\treturn 0;", "\t} // CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES", "\tint f" + n + "a;", "\tint f" + n + "b;"));
					}
					lines.add("}");
					return Map.entry(lines, BaseCheckTest.runCheckInline(FieldConsolidationCheck.class, String.join("\n", lines)));
				},
				(size, checked) -> {
					final var lines = checked.getKey();
					final var events = checked.getValue();
					assertNotNull(FixerAst.parseOrNull(lines));
					final var start = System.nanoTime();
					final var result = CheckstyleFixAction.applyFixes(lines, new ArrayList<>(events), Map.of(FieldConsolidationCheck.class.getName(), fixer), Map.of());
					final var took = System.nanoTime() - start;
					assertEquals(0, result.fixCount());
					assertEquals(size, result.skippedReasons().get("FieldConsolidationCheck").size());
					return took;
				}
		);
	}

	@Test
	public void testSameRunFromEveryEventPosition() {
		final var lines = inClass("int alpha;", "int beta;", "int gamma;");
		final var expected = new FixResult(1, 3, List.of("\tint alpha, beta, gamma;"));
		assertEquals(expected, fixer.fix(lines, 2, 5));
		assertEquals(expected, fixer.fix(lines, 3, 5));
	}

	@Test
	public void testSeparateStretchRunNotFolded() {
		assertEquals(
				new FixResult(1, 4, List.of("\tint alpha, beta;", "\tString first, second;")),
				fixer.fix(inClass("int alpha;", "String first;", "int beta;", "String second;", "void m() {", "}", "int gamma;", "int delta;"), 3, 5)
		);
	}

	@Test
	public void testSplitCallArgumentsJoinOntoOneLine() {
		assertEquals(
				new FixResult(1, 3, List.of("\tString alpha, beta = String.valueOf(12);")),
				fixer.fix(inClass("String alpha;", "String beta = String.valueOf(", "\t\t12);"), 2, 8)
		);
	}

	@Test
	public void testStaleEventsAfterFold() {
		final var folded = inClass("int alpha, beta;", "String first, second;");
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(folded, 2, 8));
		assertNull(fixer.fix(folded, 3, 5));
	}

	@Test
	public void testTabInsideValueCountsOneCodePoint() {
		assertEquals(
				new FixResult(1, 2, List.of("\tint alpha, n123456789012345678901234 = 1\t+\t2;")),
				fixer.fix(inClass("int alpha;", "int n123456789012345678901234 = 1\t+\t2;"), 2, 5)
		);
	}

	@Test
	public void testTabInsideValuePastLimitIsStale() {
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(inClass("int alpha;", "int n1234567890123456789012345 = 1\t+\t2;"), 2, 5));
	}

	@Test
	public void testTargetColumnCountsCodePointsPastSupplementaryCharacters() {
		final var wide = new String(Character.toChars(0x1D465)).repeat(3);
		final var lines = inClass("Object alpha = new Object() {};", "/* " + wide + " */ Object beta = new Object() { int x; };");
		assertEquals(new FixResult(1, 2, List.of("\t// " + wide, "\tObject alpha = new Object() {}, beta = new Object() { int x; };")), fixer.fix(lines, 2, 33));
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(lines, 2, 34));
	}

	@Test
	public void testTargetEquidistantNamesPickTheOneAtOrPastTheColumn() {
		final var lines = inClass("Object alpha = new Object() {};", "Object beta = new Object() { int x; };");
		assertEquals(new FixResult(1, 2, List.of("\tObject alpha = new Object() {}, beta = new Object() { int x; };")), fixer.fix(lines, 2, 20));
		assertEquals(new SkipResult(STALE_REASON), fixer.fix(lines, 2, 21));
	}

	@Test
	public void testTargetRunDirectiveSkipsWithoutFold() {
		assertEquals(
				new SkipResult(DIRECTIVE_REASON),
				fixer.fix(inClass("int alpha;", "String first;", "int beta; // NOSONAR", "String second;"), 3, 5)
		);
	}

	@Test
	public void testThreeInterleavedRunsFold() {
		final var lines = inClass("int a1;", "long b1;", "String c1;", "int a2;", "long b2;", "String c2;");
		final var expected = new FixResult(1, 6, List.of("\tint a1, a2;", "\tlong b1, b2;", "\tString c1, c2;"));
		assertEquals(expected, fixer.fix(lines, 4, 5));
		assertEquals(expected, fixer.fix(lines, 5, 6));
		assertEquals(expected, fixer.fix(lines, 6, 8));
	}

	@Test
	public void testTouchingRunsNotFolded() {
		assertEquals(
				new FixResult(1, 2, List.of("\tint alpha, beta;")),
				fixer.fix(inClass("int alpha;", "int beta;", "String first;", "String second;"), 2, 5)
		);
	}

	@Test
	public void testTrailingWhitespaceOnSplitValueLineDropped() {
		assertEquals(new FixResult(1, 3, List.of("\tint alpha, beta = 1 + 2;")), fixer.fix(inClass("int alpha;", "int beta = 1 +   ", "\t\t2;"), 2, 5));
	}

	@Test
	public void testTransitiveChainFolds() {
		assertEquals(
				new FixResult(1, 6, List.of("\tint a1, a2;", "\tString s1, s2;", "\tlong l1, l2;")),
				fixer.fix(inClass("int a1;", "String s1;", "int a2;", "long l1;", "String s2;", "long l2;"), 3, 5)
		);
	}

	@Test
	public void testUnicodeEscapeAnywhereIsUnverified() {
		assertEquals(new SkipResult(UNVERIFIED_REASON), fixer.fix(inClass("int alpha; // \\u000a", "int beta;"), 2, 5));
	}

	@MethodSource("unparseableProvider")
	@ParameterizedTest
	void testUnparseableBufferNotFixable(List<String> lines, int lineIndex, int column) {
		assertNull(fixer.fix(lines, lineIndex, column));
	}

	/**
	 * Answers the event on every line from {@code firstLine} to the class body's last, each refused
	 * as unverified, timing the answers alone: the parse is made beforehand and the memos of any
	 * earlier answer are dropped, so each call times one cold pass. The buffer version is set as
	 * {@code applyFixes} sets it, without which every answer compares the whole buffer to the parsed one.
	 */
	private long timeEveryEvent(List<String> lines, int firstLine, IntUnaryOperator column) {
		FieldConsolidationFixer.clearCache();
		FieldRewriteVerifier.clearCache();
		assertNotNull(FixerAst.parseOrNull(lines));
		FixContext.setBufferVersion(0);
		try {
			final var start = System.nanoTime();
			for (var line = firstLine; line < lines.size() - 1; ++line)
				assertEquals(new SkipResult(UNVERIFIED_REASON), fixer.fix(lines, line, column.applyAsInt(line)));
			return System.nanoTime() - start;
		}
		finally {
			FixContext.clearBufferVersion();
		}
	}
}