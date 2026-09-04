package com.etk2000.checkstyle;

import static com.etk2000.checkstyle.ScalingAssertions.assertScalesLinearly;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.FieldInitializers.Kind;
import com.etk2000.checkstyle.ast.DocComments;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

@Isolated
public class FieldConsolidationCheckTest {
	record Field(boolean isStatic, @Nonnull Kind kind) {}

	private static final List<String> SPLIT_DECLARATOR = List.of(
			"class InputFieldConsolidationSplitDeclarator {",
			"\tint beta",
			"\t\t\t=",
			"\t\t\t5;",
			"}"
	);
	private static final Map<Kind, String> INITIALIZERS = Map.of(Kind.INERT, "", Kind.LITERAL, " = 1", Kind.RUNS_CODE, " = g()");

	/**
	 * True when a field of {@code end}'s group sits between the first of {@code run} and
	 * {@code end}, outside the run, and is {@linkplain FieldInitializers#pinned pinned} to it.
	 */
	@CheckReturnValue
	private static boolean crossesPin(@Nonnull List<Field> fields, @Nonnull List<Integer> run, int end) {
		for (var between = run.getFirst() + 1; between < end; ++between) {
			final var other = fields.get(between);
			if (!run.contains(between) && other.isStatic() == fields.get(end).isStatic() && FieldInitializers.pinned(fields.get(end).kind(), other.kind()))
				return true;
		}
		return false;
	}

	/** Every sequence of three or four fields, each static or not and of every {@link Kind}. */
	@Nonnull
	private static Stream<List<Field>> fieldSequences() {
		final var choices = new ArrayList<Field>();
		final var kinds = Kind.values();
		for (var isStatic : List.of(false, true)) {
			for (var kind : kinds)
				choices.add(new Field(isStatic, kind));
		}
		var sequences = List.of(List.<Field>of());
		final var lengthsThreeAndFour = new ArrayList<List<Field>>();
		for (var length = 1; length <= 4; ++length) {
			final var longer = new ArrayList<List<Field>>();
			for (var sequence : sequences) {
				for (var choice : choices) {
					final var next = new ArrayList<>(sequence);
					next.add(choice);
					longer.add(next);
				}
			}
			sequences = longer;
			if (length >= 3)
				lengthsThreeAndFour.addAll(sequences);
		}
		return lengthsThreeAndFour.stream();
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST firstField(@Nonnull List<String> source) throws Exception {
		final var root = JavaParser.parseFileText(new FileText(new File("InputFieldConsolidationDeclarator.java"), source), JavaParser.Options.WITHOUT_COMMENTS);
		return requireNonNull(root.findFirstToken(TokenTypes.CLASS_DEF).findFirstToken(TokenTypes.OBJBLOCK).findFirstToken(TokenTypes.VARIABLE_DEF));
	}

	/**
	 * The runs, by the indexes of their fields, that {@link FieldConsolidationCheck#runs} finds among
	 * {@code fields} by the pairwise rule: a field joins the run open for its group unless that
	 * would move it past a field it is {@linkplain #crossesPin pinned} to.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<List<Integer>> pairwiseRuns(@Nonnull List<Field> fields) {
		final var open = new HashMap<Boolean, List<Integer>>();
		final var runs = new ArrayList<List<Integer>>();
		for (var end = 0; end < fields.size(); ++end) {
			final var run = open.get(fields.get(end).isStatic());
			if (run == null || crossesPin(fields, run, end)) {
				final var started = new ArrayList<>(List.of(end));
				open.put(fields.get(end).isStatic(), started);
				runs.add(started);
			}
			else
				run.add(end);
		}
		runs.removeIf(run -> run.size() < 2);
		return runs;
	}

	@Test
	public void testCommentsDoNotLeakBetweenFiles() throws Exception {
		// the first file's directive ends where the second file's plain comment does, right above its
		// first field, and the first file's two fields make the check read its comments, so the second
		// file's pair is only reported when each file reads its own
		final var events = BaseCheckTest.runCheckOnFiles(
				FieldConsolidationCheck.class,
				"fieldconsolidation/InputFieldConsolidationCommentsResetA.java",
				"fieldconsolidation/InputFieldConsolidationCommentsResetB.java"
		);
		assertEquals(1, events.size(), "expected only the second file's violation, got " + events);
		final var event = events.getFirst();
		assertTrue(event.getFileName().endsWith("InputFieldConsolidationCommentsResetB.java"), event.getFileName());
		assertEquals(6, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') must be declared on one line.", event.getMessage());
	}

	@Test
	public void testDocCommentsDoNotLeakBetweenFiles() throws Exception {
		// the first file's class Javadoc sits where the second file's first field starts its line, and
		// the first file's two fields make the check read its doc comments, so the second file's pair
		// is only reported when each file reads its own
		final var events = BaseCheckTest.runCheckOnFiles(
				FieldConsolidationCheck.class,
				"fieldconsolidation/InputFieldConsolidationDocResetA.java",
				"fieldconsolidation/InputFieldConsolidationDocResetB.java"
		);
		assertEquals(1, events.size(), "expected only the second file's violation, got " + events);
		final var event = events.getFirst();
		assertTrue(event.getFileName().endsWith("InputFieldConsolidationDocResetB.java"), event.getFileName());
		assertEquals(5, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') must be declared on one line.", event.getMessage());
	}

	@Test
	public void testFileLinesDoNotLeakBetweenFiles() throws Exception {
		// the second file's initializer sits past the first file's last line, so it only reads as
		// short when each file's lines are read afresh
		final var events = BaseCheckTest.runCheckOnFiles(
				FieldConsolidationCheck.class,
				"fieldconsolidation/InputFieldConsolidationLinesResetA.java",
				"fieldconsolidation/InputFieldConsolidationLinesResetB.java"
		);
		assertEquals(1, events.size(), "expected only the second file's violation, got " + events);
		final var event = events.getFirst();
		assertTrue(event.getFileName().endsWith("InputFieldConsolidationLinesResetB.java"), event.getFileName());
		assertEquals(6, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') must be declared on one line.", event.getMessage());
	}

	@Test
	public void testInterfaceImplicitAndExplicitModifiersNotMerged() throws Exception {
		// inline because RedundantModifier rejects the explicit modifiers in a lint-checked slice
		final var source = """
				interface InputFieldConsolidationCleanImplicitModifiers {
					int ALPHA = 1;
					public static final int BETA = 2;
				}
				""";
		assertEquals(List.of(), BaseCheckTest.runCheckInline(FieldConsolidationCheck.class, source));
	}

	@Test
	public void testOneLineInitializerOnOneLineWithLinesOutOfStep() throws Exception {
		final var source = List.of("class InputFieldConsolidationDeclarator {", "\tint beta = 5;", "}");
		final var declarator = firstField(source);
		assertEquals("5", FieldConsolidationCheck.oneLineInitializer(new LineColumns(source), declarator));
		assertNull(FieldConsolidationCheck.oneLineInitializer(new LineColumns(List.of(source.getFirst(), "\tint beta = ", source.getLast())), declarator));
	}

	/**
	 * Lines out of step with the parse (here one of them shorter than the column of the name, of
	 * the {@code =} or of the {@code ;} on it) leave that token at no position, which reads as no
	 * one-line initializer rather than one sliced from the line's start ({@code x 5}).
	 */
	@CsvSource(value = {
			"-1, , 5",
			"1, abc, NULL",
			"2, x, NULL",
			"3, x, NULL"
	}, nullValues = "NULL")
	@ParameterizedTest
	public void testOneLineInitializerWithLinesOutOfStep(int replaced, String replacement, String expected) throws Exception {
		final var declarator = firstField(SPLIT_DECLARATOR);
		final var lines = new ArrayList<>(SPLIT_DECLARATOR);
		if (replaced >= 0)
			lines.set(replaced, replacement);
		assertEquals(expected, FieldConsolidationCheck.oneLineInitializer(new LineColumns(lines), declarator));
	}

	/** Only the runs are timed: checkstyle's own parse of so long a class grows about four times per doubling. */
	@Test
	public void testRunHoldingARunnerScalesLinearly() throws Exception {
		assertScalesLinearly(
				500,
				4000,
				size -> {
					final var lines = new ArrayList<>(List.of("class T {", "\tint f0 = 0;", "\tint f1 = g();"));
					for (var i = 2; i < size; ++i)
						lines.add("\tint f" + i + " = " + i + ";");
					lines.add("}");
					return Map.entry(lines, JavaParser.parseFileText(new FileText(new File("T.java"), lines), JavaParser.Options.WITHOUT_COMMENTS));
				},
				(size, parsed) -> {
					final var root = parsed.getValue();
					final var docs = new DocComments(DocComments.tokenPositions(root), new TreeSet<>());
					final var objBlock = root.findFirstToken(TokenTypes.CLASS_DEF).findFirstToken(TokenTypes.OBJBLOCK);
					final var start = System.nanoTime();
					assertEquals(size, FieldConsolidationCheck.runs(objBlock, new LineColumns(parsed.getKey()), () -> docs, List::of).getFirst().size());
					return System.nanoTime() - start;
				}
		);
	}

	@MethodSource("fieldSequences")
	@ParameterizedTest
	public void testRunsMatchThePairwisePins(List<Field> fields) throws Exception {
		final var lines = new ArrayList<>(List.of("class T {"));
		for (var i = 0; i < fields.size(); ++i)
			lines.add("\t" + (fields.get(i).isStatic() ? "static " : "") + "int x" + i + INITIALIZERS.get(fields.get(i).kind()) + ";");
		lines.add("}");
		final var root = JavaParser.parseFileText(new FileText(new File("T.java"), lines), JavaParser.Options.WITHOUT_COMMENTS);
		final var docs = new DocComments(DocComments.tokenPositions(root), new TreeSet<>());
		final var objBlock = root.findFirstToken(TokenTypes.CLASS_DEF).findFirstToken(TokenTypes.OBJBLOCK);
		final var runs = new ArrayList<List<Integer>>();
		for (var run : FieldConsolidationCheck.runs(objBlock, new LineColumns(lines), () -> docs, List::of)) {
			final var indexes = new ArrayList<Integer>();
			for (var declaration : run)
				indexes.add(Integer.parseInt(declaration.getFirst().findFirstToken(TokenTypes.IDENT).getText().substring(1)));
			runs.add(indexes);
		}
		assertEquals(pairwiseRuns(fields), runs);
	}
}