package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Cross-checks between {@link BraceOwnLineCheck} and the checks sharing its brace positions.
 */
public class BraceOwnLineCheckTest {
	private static final List<String> CONTROL_FLOW_ELSE_IF_ALLMAN_CASES = List.of(
			"else_if_split_cuddled_comment_header_opens_block_comment",
			"else_if_split_inner_allman"
	);
	private static final List<String> CONTROL_FLOW_FALLBACK_CASES = List.of(
			"brace_on_own_line_do_body_on_brace_line",
			"brace_on_own_line_do_comment_on_brace_line",
			"brace_on_own_line_do_with_comment_on_do_and_body",
			"brace_on_own_line_do_with_comment_on_do_and_brace",
			"brace_on_own_line_if_close_brace_after_text_block",
			"brace_on_own_line_if_comment_before_close_brace",
			"brace_on_own_line_if_comment_on_brace_line",
			"brace_on_own_line_with_block_comment_on_brace"
	);
	// split so NoInlineJavaSourceTest does not read the marker as inline fixture source
	private static final String VIOLATION_MARKER = "// " + "violation:";

	@CheckReturnValue
	@Nonnull
	private static Set<Integer> firedLines(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String path
	) throws Exception {
		final var lines = new TreeSet<Integer>();
		for (var event : BaseCheckTest.runCheck(checkClass, path))
			lines.add(event.getLine());
		return lines;
	}

	/**
	 * The 1-based line of the first violation marker in each named case of {@code path}.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<Integer> markerLines(@Nonnull String path, @Nonnull List<String> caseNames) throws Exception {
		final var lines = resourceLines(path);
		final var markers = new TreeSet<Integer>();
		for (var name : caseNames) {
			var i = lines.indexOf("// === case: " + name + " ===");
			if (i < 0)
				throw new IllegalArgumentException("no case " + name + " in " + path);
			while (!lines.get(i).contains(VIOLATION_MARKER))
				++i;
			markers.add(i + 1);
		}
		return markers;
	}

	/**
	 * The 1-based line of each header inside the named method of {@code path} that an own-line brace
	 * follows, past any comment lines between the two.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<Integer> ownLineBraceHeaders(@Nonnull String path, @Nonnull String method) throws Exception {
		final var lines = resourceLines(path);
		final var headers = new TreeSet<Integer>();
		var i = 0;
		while (!lines.get(i).contains(" " + method + "("))
			++i;
		for (++i; !lines.get(i).equals("\t}"); ++i) {
			if (!lines.get(i + 1).strip().startsWith("{"))
				continue;
			var header = i;
			while (lines.get(header).strip().startsWith("//"))
				--header;
			headers.add(header + 1);
		}
		return headers;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> resourceLines(@Nonnull String path) throws Exception {
		final var url = requireNonNull(BraceOwnLineCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path));
		return Files.readAllLines(Path.of(url.toURI()));
	}

	@Test
	public void testBeginTreeDropsABufferFinishTreeNeverCleared() throws Exception {
		final var check = new BraceOwnLineCheck();
		final var lines = BraceOwnLineCheck.class.getDeclaredField("lines");
		lines.setAccessible(true);
		lines.set(check, List.of("left over from the previous file"));
		check.beginTree(new DetailAstImpl());
		assertNull(lines.get(check));
	}

	@Test
	public void testBraceOwnLineFiresOnlyOnControlFlowBracesAllmanCleanSnippets() throws Exception {
		final var path = "controlflowbraces/cases.clean.java";
		final var expected = new TreeSet<Integer>();
		expected.addAll(ownLineBraceHeaders(path, "blockWithStatementOnBraceLine"));
		expected.addAll(ownLineBraceHeaders(path, "bracedBodyWithOwnMultiLineBraces"));
		expected.addAll(ownLineBraceHeaders(path, "bracedDeclarationBody"));
		assertEquals(3, expected.size());
		assertEquals(expected, firedLines(BraceOwnLineCheck.class, path));
	}

	/**
	 * Besides the fallback slices, the two else-if slices whose inner {@code if} has an own-line brace,
	 * reported at that {@code if}, the line after the {@code else} the slice's marker is on.
	 */
	@Test
	public void testBraceOwnLineFiresOnlyOnControlFlowBracesOwnLineBraceSlices() throws Exception {
		final var path = "controlflowbraces/cases.in.java";
		final var expected = new TreeSet<>(markerLines(path, CONTROL_FLOW_FALLBACK_CASES));
		for (var elseLine : markerLines(path, CONTROL_FLOW_ELSE_IF_ALLMAN_CASES))
			expected.add(elseLine + 1);
		assertEquals(expected, firedLines(BraceOwnLineCheck.class, path));
	}

	@Test
	public void testBraceOwnLineResourcesProduceNoMultilineCallFormatting() throws Exception {
		BaseCheckTest.assertNoViolations(
				MultilineCallFormattingCheck.class,
				"braceownline/cases.clean.java",
				"braceownline/cases.in.java",
				"braceownline/cases.out.java"
		);
	}

	@Test
	public void testBraceOwnLineResourcesProduceNoNoCaseBraces() throws Exception {
		BaseCheckTest.assertNoViolations(
				NoCaseBracesCheck.class,
				"braceownline/cases.clean.java",
				"braceownline/cases.in.java",
				"braceownline/cases.out.java"
		);
	}

	@Test
	public void testBraceOwnLineResourcesProduceNoRecordFormatting() throws Exception {
		BaseCheckTest.assertNoViolations(
				RecordFormattingCheck.class,
				"braceownline/cases.clean.java",
				"braceownline/cases.in.java",
				"braceownline/cases.out.java"
		);
	}

	@Test
	public void testControlFlowBracesFiresOnlyOnBraceOwnLineFallbackSlices() throws Exception {
		final var path = "braceownline/cases.in.java";
		final var fallbackCases = resourceLines(path).stream()
				.filter(line -> line.startsWith("// === case: fallback_"))
				.map(line -> line.substring("// === case: ".length(), line.length() - " ===".length()))
				.toList();
		assertEquals(16, fallbackCases.size());
		assertEquals(markerLines(path, fallbackCases), firedLines(ControlFlowBracesCheck.class, path));
	}

	@Test
	public void testControlFlowBracesFiresOnlyOnBraceOwnLineUnwrapWinsCleanMethods() throws Exception {
		final var path = "braceownline/cases.clean.java";
		final var expected = new TreeSet<Integer>();
		expected.addAll(ownLineBraceHeaders(path, "singleStatementCommentBeforeBraceUnwrapWins"));
		expected.addAll(ownLineBraceHeaders(path, "singleStatementDoUnwrapWins"));
		expected.addAll(ownLineBraceHeaders(path, "singleStatementUnwrapWins"));
		assertEquals(expected, firedLines(ControlFlowBracesCheck.class, path));
	}

	@Test
	public void testEmptyBodyAndEmptySwitchResourcesProduceNoBraceOwnLine() throws Exception {
		BaseCheckTest.assertNoViolations(
				BraceOwnLineCheck.class,
				"emptybody/cases.clean.java",
				"emptybody/cases.in.java",
				"emptybody/cases.out.java",
				"emptyswitch/cases.clean.java",
				"emptyswitch/cases.in.java"
		);
	}

	/**
	 * The shorter file goes first, so a buffer kept past it would be indexed beyond its end at the
	 * second file's first unwrap site.
	 */
	@Test
	public void testLinesDoNotLeakBetweenFiles() throws Exception {
		final var expected = BaseCheckTest.runCheck(BraceOwnLineCheck.class, "braceownline/cases.clean.java").size()
				+ BaseCheckTest.runCheck(BraceOwnLineCheck.class, "braceownline/cases.in.java").size();
		final var events = BaseCheckTest.runCheckOnFiles(
				BraceOwnLineCheck.class,
				"braceownline/cases.clean.java",
				"braceownline/cases.in.java"
		);
		assertEquals(expected, events.size());
	}

	@Test
	public void testMultilineCallFormattingResourcesProduceNoBraceOwnLine() throws Exception {
		BaseCheckTest.assertNoViolations(
				BraceOwnLineCheck.class,
				"multilinecallformatting/cases.clean.java",
				"multilinecallformatting/cases.in.java",
				"multilinecallformatting/cases.out.java",
				"multilinecallformatting/cases.fixed.java"
		);
	}

	@Test
	public void testNoCaseBracesResourcesProduceNoBraceOwnLine() throws Exception {
		BaseCheckTest.assertNoViolations(
				BraceOwnLineCheck.class,
				"nocasebraces/cases.clean.java",
				"nocasebraces/cases.in.java",
				"nocasebraces/cases.out.java"
		);
	}

	@Test
	public void testNoHeaderBraceCorpusProducesNoBraceOwnLine() throws Exception {
		BaseCheckTest.assertNoViolations(
				BraceOwnLineCheck.class,
				"constructorassignmentorder/cases.clean.java",
				"constructorassignmentorder/cases.in.java",
				"constructorassignmentorder/cases.out.java",
				"fieldconsolidation/cases.clean.java",
				"jitinefficiency/cases.clean.java",
				"noenumtrailingsemicolon/cases.clean.java",
				"nounnecessarythis/cases.clean.java",
				"nounnecessarythis/cases.in.java",
				"nounnecessarythis/cases.out.java",
				"preferdowhile/cases.clean.java",
				"preferimport/cases.clean.java",
				"preferprefixincrement/cases.in.java",
				"preferprefixincrement/cases.out.java",
				"preferrecord/cases.clean.java",
				"preferspecificapi/cases.in.java",
				"preferspecificapi/cases.out.java",
				"redundantarraycreation/cases.in.java",
				"redundantnumericsuffix/cases.clean.java",
				"redundantnumericsuffix/cases.in.java",
				"redundantnumericsuffix/cases.out.java"
		);
	}

	@Test
	public void testRecordFormattingResourcesProduceNoBraceOwnLine() throws Exception {
		BaseCheckTest.assertNoViolations(
				BraceOwnLineCheck.class,
				"recordformatting/cases.clean.java",
				"recordformatting/cases.in.java",
				"recordformatting/cases.out.java"
		);
	}
}