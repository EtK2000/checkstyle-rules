package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.format.SpanReformat;
import com.etk2000.checkstyle.gradle.fix.FixerAst.AstFunction;
import com.etk2000.checkstyle.gradle.fix.FixerAst.Locator;
import com.etk2000.checkstyle.gradle.fix.FixerAst.ThrowingParser;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Only the {@code CheckstyleException} arm is reachable from a real buffer, so the rest are driven
 * through the injected-parser overload.
 */
public class FixerAstTest {
	private static final List<String> UNPARSEABLE = List.of("+");
	private static final List<String> VALID = List.of("class T {", "\tint x;", "}");

	/**
	 * Char columns against {@code VALID}'s second line and the code-point column each must become.
	 * The line is pure ASCII, so every pair but the clamped ones is the identity.
	 */
	@Nonnull
	private static Stream<Arguments> clampedColumns() {
		final var length = VALID.get(1).length();
		return Stream.of(
				Arguments.of(0, 0),
				Arguments.of(length, length),
				Arguments.of(10_000, length)
		);
	}

	/** A locator that finds nothing and records that it was asked. */
	@Nonnull
	private static Locator<String> counting(@Nonnull AtomicInteger calls) {
		return (root, line, column) -> {
			calls.incrementAndGet();
			return null;
		};
	}

	/** Hands {@code live} to FixerAst as applyFixes' buffer, {@code source} as it stood before the pass. */
	private static void enterPass(@Nonnull List<String> live, @Nonnull List<String> source, int version) {
		FixContext.setLiveBuffer(live, source);
		FixContext.setBufferVersion(version);
	}

	/** Name of the single field declared by {@link #VALID}. */
	@Nonnull
	private static String fieldName(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.VARIABLE_DEF)
				return node.findFirstToken(TokenTypes.IDENT).getText();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no field in the parsed buffer");
	}

	/** Guards {@link FixerAst#holdsOrFails} reads as holding on {@code VALID}: a true answer, and every way of giving none. */
	@Nonnull
	private static Stream<Arguments> holdingGuards() {
		final AstFunction<Boolean> answersNull = root -> null;
		final AstFunction<Boolean> answersTrue = root -> true;
		final AstFunction<Boolean> overflows = root -> {
			throw new StackOverflowError();
		};
		final AstFunction<Boolean> throwsRuntime = root -> {
			throw new IllegalStateException("classifier blew up");
		};
		return Stream.of(
				Arguments.of("null", answersNull),
				Arguments.of("overflowing", overflows),
				Arguments.of("throwing", throwsRuntime),
				Arguments.of("true", answersTrue)
		);
	}

	/** Every entry point that parses the buffer it is handed, run on that buffer. */
	@Nonnull
	private static Stream<Arguments> liveBufferEntryPoints() {
		return Stream.of(
				Arguments.of("holdsOrFails", (Function<List<String>, Object>) lines -> FixerAst.holdsOrFails(lines, root -> true)),
				Arguments.of("locate", (Function<List<String>, Object>) lines -> FixerAst.locate(lines, 1, 1, (root, line, column) -> "found")),
				Arguments.of("parseOrNull", (Function<List<String>, Object>) FixerAst::parseOrNull),
				Arguments.of("withAst", (Function<List<String>, Object>) lines -> FixerAst.withAst(lines, root -> "found"))
		);
	}

	/** A locator that finds nothing and records the column it was handed. */
	@Nonnull
	private static Locator<String> recordingColumn(@Nonnull AtomicInteger seen) {
		return (root, line, column) -> {
			seen.set(column);
			return null;
		};
	}

	/** A locator that finds nothing and records the line it was handed. */
	@Nonnull
	private static Locator<String> recordingLine(@Nonnull AtomicInteger seen) {
		return (root, line, column) -> {
			seen.set(line);
			return null;
		};
	}

	/**
	 * The cache is per-thread and outlives a test, so every case starts cold and
	 * leaves none behind: the injected-parser overload writes its result into the
	 * same cache the production one-arg path reads.
	 */
	@AfterEach
	@BeforeEach
	public void clearCache() {
		FixContext.clearBufferVersion();
		FixContext.clearLiveBuffer();
		FixerAst.clearCache();
	}

	@Test
	public void testActionThrowOnTheLiveBufferAfterAFixAnswersNull() {
		final AstFunction<String> throwing = root -> {
			throw new IllegalStateException("classifier blew up");
		};
		final var live = new ArrayList<>(VALID);
		enterPass(live, VALID, 1);
		assertNull(FixerAst.withAst(live, throwing));
	}

	@Test
	public void testAssertionErrorYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new AssertionError("grammar assertion");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testBufferVersionBumpedAfterAnEditReparses() {
		final var lines = new ArrayList<>(VALID);
		try {
			FixContext.setBufferVersion(0);
			final var first = FixerAst.parseOrNull(lines);
			lines.set(1, "\tint y;");
			FixContext.setBufferVersion(1);
			final var second = FixerAst.parseOrNull(lines);
			assertNotNull(second);
			assertNotSame(first, second);
			assertEquals("y", fieldName(second));
		}
		finally {
			FixContext.clearBufferVersion();
		}
	}

	@Test
	public void testBufferVersionBumpedWithoutAnEditReuses() {
		final var lines = new ArrayList<>(VALID);
		try {
			FixContext.setBufferVersion(0);
			final var first = FixerAst.parseOrNull(lines);
			FixContext.setBufferVersion(1);
			assertSame(first, FixerAst.parseOrNull(lines));
		}
		finally {
			FixContext.clearBufferVersion();
		}
	}

	@Test
	public void testBufferVersionEqualCopyAtTheSameVersionReuses() {
		try {
			FixContext.setBufferVersion(0);
			final var first = FixerAst.parseOrNull(new ArrayList<>(VALID));
			assertSame(first, FixerAst.parseOrNull(new ArrayList<>(VALID)));
		}
		finally {
			FixContext.clearBufferVersion();
		}
	}

	@Test
	public void testBufferVersionSameListAtTheSameVersionSettlesWithoutComparing() {
		final var lines = new ArrayList<>(VALID);
		try {
			FixContext.setBufferVersion(3);
			final var first = FixerAst.parseOrNull(lines);
			lines.set(1, "\tint y;");
			assertSame(first, FixerAst.parseOrNull(lines));
		}
		finally {
			FixContext.clearBufferVersion();
		}
	}

	@Test
	public void testCachedRootNotReusedAfterInPlaceEdit() {
		final var lines = new ArrayList<>(VALID);
		final var first = FixerAst.parseOrNull(lines);
		assertNotNull(first);
		lines.set(1, "\tint y;");
		final var second = FixerAst.parseOrNull(lines);
		assertNotNull(second);
		assertNotSame(first, second);
		assertEquals("y", fieldName(second));
		assertEquals("x", fieldName(first));
	}

	@Test
	public void testCachedRootNotReusedForDifferentParser() {
		final var lines = new ArrayList<>(VALID);
		assertNotNull(FixerAst.parseOrNull(lines));
		final ThrowingParser failing = ignored -> {
			throw new CheckstyleException("injected");
		};
		assertNull(FixerAst.parseOrNull(lines, failing));
		assertNotNull(FixerAst.parseOrNull(lines), "the injected parser's failure must not poison the production path");
	}

	@Test
	public void testCachedRootReusedForUnchangedBuffer() {
		final var lines = new ArrayList<>(VALID);
		final var first = FixerAst.parseOrNull(lines);
		assertNotNull(first);
		assertSame(first, FixerAst.parseOrNull(lines));
	}

	@Test
	public void testCacheIsThreadConfined() throws Exception {
		final var mine = FixerAst.parseOrNull(VALID);
		assertNotNull(mine);
		final var theirs = new AtomicReference<>();
		final var thread = new Thread(() -> theirs.set(FixerAst.parseOrNull(VALID)));
		thread.start();
		thread.join();
		assertNotNull(theirs.get());
		assertNotSame(mine, theirs.get());
	}

	@Test
	public void testCandidateBufferAfterAFixAnswersNull() {
		final var live = new ArrayList<>(VALID);
		live.set(1, "\tint x = (;");
		enterPass(live, VALID, 1);
		assertNull(FixerAst.parseOrNull(new ArrayList<>(live)));
	}

	@Test
	public void testCheckstyleExceptionYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new CheckstyleException("unparseable");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testClearCacheForcesReparse() {
		final var first = FixerAst.parseOrNull(VALID);
		assertNotNull(first);
		FixerAst.clearCache();
		assertNotSame(first, FixerAst.parseOrNull(VALID));
	}

	@Test
	public void testEntryStateMatchesTheFoldFromTheTop() {
		final var lines = List.of("/* a", "b */ int x;", "String s = \"\"\"", "\tt", "\t\"\"\";", "int y;");
		for (var i = -1; i <= lines.size() + 1; ++i)
			assertEquals(SpanReformat.lexerStateAt(lines, i), FixerAst.entryState(lines, i), "line " + i);
	}

	@Test
	public void testEntryStateNotReusedAfterInPlaceEdit() {
		final var lines = new ArrayList<>(List.of("int x;", "int y;"));
		assertEquals(LexerState.NONE, FixerAst.entryState(lines, 1));
		lines.set(0, "/* open");
		assertEquals(new LexerState(true, false), FixerAst.entryState(lines, 1));
	}

	@Test
	public void testFailedParseIsCached() {
		final var calls = new AtomicInteger();
		final ThrowingParser counting = lines -> {
			calls.incrementAndGet();
			throw new CheckstyleException("unparseable");
		};
		assertNull(FixerAst.parseOrNull(List.copyOf(UNPARSEABLE), counting));
		assertNull(FixerAst.parseOrNull(List.copyOf(UNPARSEABLE), counting));
		assertEquals(1, calls.get());
	}

	@Test
	public void testHoldsOrFailsFalseAnswerDoesNotHold() {
		assertFalse(FixerAst.holdsOrFails(VALID, root -> false));
	}

	@MethodSource("holdingGuards")
	@ParameterizedTest(name = "{0}")
	public void testHoldsOrFailsOnAParsedBufferHolds(@Nonnull String name, @Nonnull AstFunction<Boolean> guard) {
		assertTrue(FixerAst.holdsOrFails(VALID, guard));
	}

	@Test
	public void testHoldsOrFailsUnparseableBufferHoldsWithoutAskingTheGuard() {
		final var ran = new AtomicInteger();
		final AstFunction<Boolean> guard = root -> {
			ran.incrementAndGet();
			return false;
		};
		assertTrue(FixerAst.holdsOrFails(UNPARSEABLE, guard));
		assertEquals(0, ran.get());
	}

	@Test
	public void testInjectedParserFailureOnTheLiveBufferAfterAFixAnswersNull() {
		final ThrowingParser failing = lines -> {
			throw new CheckstyleException("injected");
		};
		final var live = new ArrayList<>(VALID);
		enterPass(live, VALID, 1);
		assertNull(FixerAst.parseOrNull(live, failing));
	}

	@MethodSource("liveBufferEntryPoints")
	@ParameterizedTest
	public void testLiveBufferBrokenAfterAFixThrows(@Nonnull String name, @Nonnull Function<List<String>, Object> entryPoint) {
		final var live = new ArrayList<>(VALID);
		live.set(1, "\tint x = (;");
		enterPass(live, VALID, 1);
		final var error = assertThrows(FixBrokeParseError.class, () -> entryPoint.apply(live));
		assertEquals("The buffer stopped parsing after 1 fixes in null", error.getMessage());
	}

	@Test
	public void testLiveBufferBrokenAfterAnEqualCandidateCachedItsFailureThrows() {
		final var live = new ArrayList<>(UNPARSEABLE);
		enterPass(live, VALID, 1);
		assertNull(FixerAst.parseOrNull(new ArrayList<>(live)));
		assertThrows(FixBrokeParseError.class, () -> FixerAst.parseOrNull(live));
	}

	@Test
	public void testLiveBufferBrokenAtVersionZeroAnswersNull() {
		final var live = new ArrayList<>(UNPARSEABLE);
		enterPass(live, VALID, 0);
		assertNull(FixerAst.parseOrNull(live));
	}

	@ParameterizedTest
	@ValueSource(strings = {"+", "class T {\n\tint x;", "class T {\n\t/* open\n}", "class T {\n\tString s = \"\"\"\n\t\tx\n}"})
	public void testLiveBufferBrokenIntoAnyShapeThrows(@Nonnull String shape) {
		final var live = new ArrayList<>(List.of(shape.split("\n")));
		enterPass(live, VALID, 1);
		assertThrows(FixBrokeParseError.class, () -> FixerAst.parseOrNull(live));
	}

	@Test
	public void testLiveBufferBrokenWithoutAVersionAnswersNull() {
		final var live = new ArrayList<>(UNPARSEABLE);
		FixContext.setLiveBuffer(live, VALID);
		assertNull(FixerAst.parseOrNull(live));
	}

	@Test
	public void testLiveBufferCachedFailureThrowsAgain() {
		final var live = new ArrayList<>(UNPARSEABLE);
		enterPass(live, VALID, 1);
		assertThrows(FixBrokeParseError.class, () -> FixerAst.parseOrNull(live));
		assertThrows(FixBrokeParseError.class, () -> FixerAst.parseOrNull(live));
	}

	@Test
	public void testLiveBufferParsingAfterAFixAnswersRoot() {
		final var live = new ArrayList<>(VALID);
		live.set(1, "\tint y;");
		enterPass(live, VALID, 1);
		assertEquals("y", fieldName(FixerAst.parseOrNull(live)));
	}

	@Test
	public void testLiveBufferUnparseableFromTheStartAnswersNull() {
		final var live = new ArrayList<>(UNPARSEABLE);
		enterPass(live, UNPARSEABLE, 1);
		assertNull(FixerAst.parseOrNull(live));
		assertNull(FixerAst.parseOrNull(live));
	}

	@Test
	public void testLocateCatchesLocatorCheckstyleException() {
		final Locator<String> throwing = (root, line, column) -> {
			throw new CheckstyleException("locator rejected the tree");
		};
		assertNull(FixerAst.locate(VALID, 1, 0, throwing));
	}

	@MethodSource("clampedColumns")
	@ParameterizedTest
	public void testLocateClampsColumnToTheLine(int charColumn, int expectedColumn) {
		final var seen = new AtomicInteger(-1);
		FixerAst.locate(VALID, 1, charColumn, recordingColumn(seen));
		assertEquals(expectedColumn, seen.get());
	}

	@Test
	public void testLocateConvertsCharIndexToCodePointColumn() {
		final var pair = new String(Character.toChars(0x1D465));
		final var line = "class T { String s = \"" + pair + pair + "\"; }";
		final var charColumn = line.lastIndexOf('}');
		final var seen = new AtomicInteger(-1);
		FixerAst.locate(List.of(line), 0, charColumn, recordingColumn(seen));
		// two surrogate pairs ahead of it are four chars but two code points
		assertEquals(charColumn - 2, seen.get());
	}

	@Test
	public void testLocateEmptyLinesSkipsLocator() {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(List.of(), 0, 0, counting(calls)));
		assertEquals(0, calls.get());
	}

	@ParameterizedTest
	@ValueSource(ints = {0, 1, 2})
	public void testLocateInRangeLineIndexReachesLocatorZeroBased(int lineIndex) {
		final var seen = new AtomicInteger(-1);
		FixerAst.locate(VALID, lineIndex, 0, recordingLine(seen));
		assertEquals(lineIndex, seen.get());
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
	public void testLocateNegativeColumnSkipsLocator(int charColumn) {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(VALID, 1, charColumn, counting(calls)));
		assertEquals(0, calls.get());
	}

	@Test
	public void testLocateNullLocatorResult() {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(VALID, 1, 1, counting(calls)));
		assertEquals(1, calls.get());
	}

	@ParameterizedTest
	@ValueSource(ints = {-1, -100, Integer.MIN_VALUE, 3, 4, 10_000})
	public void testLocateOutOfRangeLineIndexSkipsLocator(int lineIndex) {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(VALID, lineIndex, 0, counting(calls)));
		assertEquals(0, calls.get());
	}

	@Test
	public void testLocateReturnsLocatorResult() {
		assertEquals("ok", FixerAst.locate(VALID, 1, 1, (root, line, column) -> "ok"));
	}

	@Test
	public void testLocateSingleLineBufferIsInRange() {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(List.of("class T {}"), 0, 0, counting(calls)));
		assertEquals(1, calls.get());
	}

	@Test
	public void testLocateTwoColumnsOnOneLineReachTheLocatorDistinctly() {
		final var line = "class T { int a, b; }";
		final var seen = new ArrayList<Integer>();
		final var recorded = new AtomicInteger(-1);
		for (var charColumn : new int[]{line.indexOf('a'), line.indexOf('b')}) {
			FixerAst.locate(List.of(line), 0, charColumn, recordingColumn(recorded));
			seen.add(recorded.get());
		}
		assertEquals(List.of(line.indexOf('a'), line.indexOf('b')), seen);
	}

	@Test
	public void testLocateUnparseableBufferSkipsLocator() {
		final var calls = new AtomicInteger();
		assertNull(FixerAst.locate(UNPARSEABLE, 0, 0, counting(calls)));
		assertEquals(0, calls.get());
	}

	@Test
	public void testMaskCachedForSameUnmodifiableList() {
		final var lines = List.copyOf(VALID);
		final var first = FixerAst.maskAll(lines);
		assertSame(first, FixerAst.maskAll(lines));
	}

	@Test
	public void testMaskCachedForUnchangedBuffer() {
		final var lines = new ArrayList<>(VALID);
		final var first = FixerAst.maskAll(lines);
		assertSame(first, FixerAst.maskAll(lines));
	}

	@Test
	public void testMaskClearedByClearCache() {
		final var first = FixerAst.maskAll(VALID);
		FixerAst.clearCache();
		assertNotSame(first, FixerAst.maskAll(VALID));
	}

	@Test
	public void testMaskIsImmutable() {
		assertThrows(UnsupportedOperationException.class, () -> FixerAst.maskAll(VALID).set(0, "x"));
	}

	@Test
	public void testMaskIsThreadConfined() throws Exception {
		final var mine = FixerAst.maskAll(VALID);
		final var theirs = new AtomicReference<List<String>>();
		final var thread = new Thread(() -> theirs.set(FixerAst.maskAll(VALID)));
		thread.start();
		thread.join();
		assertNotNull(theirs.get());
		assertNotSame(mine, theirs.get());
	}

	@Test
	public void testMaskNotReusedAfterInPlaceEdit() {
		final var lines = new ArrayList<>(List.of("var s = \"ab\";"));
		assertEquals(List.of("var s = \"  \";"), FixerAst.maskAll(lines));
		lines.set(0, "var s = \"abcd\";");
		assertEquals(List.of("var s = \"    \";"), FixerAst.maskAll(lines));
	}

	@Test
	public void testRuntimeExceptionYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new IllegalStateException("recognition failure");
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testStackOverflowErrorYieldsNull() {
		final ThrowingParser parser = lines -> {
			throw new StackOverflowError();
		};
		assertNull(FixerAst.parseOrNull(VALID, parser));
	}

	@Test
	public void testUnparseableBufferYieldsNull() {
		assertNull(FixerAst.parseOrNull(UNPARSEABLE));
	}

	@Test
	public void testWithAstCatchesActionAssertionError() {
		final AstFunction<String> action = root -> {
			throw new AssertionError("classifier assertion");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionCheckstyleException() {
		final AstFunction<String> action = root -> {
			throw new CheckstyleException("classifier rejected the tree");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionFailure() {
		final AstFunction<String> action = root -> {
			throw new IllegalStateException("classifier blew up");
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstCatchesActionStackOverflowError() {
		final AstFunction<String> action = root -> {
			throw new StackOverflowError();
		};
		assertNull(FixerAst.withAst(VALID, action));
	}

	@Test
	public void testWithAstNullActionResult() {
		final var ran = new AtomicInteger();
		final AstFunction<String> action = root -> {
			ran.incrementAndGet();
			return null;
		};
		assertNull(FixerAst.withAst(VALID, action));
		assertEquals(1, ran.get());
	}

	@Test
	public void testWithAstReturnsActionResult() {
		assertEquals("ok", FixerAst.withAst(VALID, root -> "ok"));
	}

	@Test
	public void testWithAstSkipsActionWhenUnparseable() {
		assertNull(FixerAst.withAst(UNPARSEABLE, root -> "should not run"));
	}
}