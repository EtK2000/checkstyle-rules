package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class PreferMathMethodFixerTest {
	private static final String TOPIC = "prefermathmethod";

	private final CheckstyleFixer fixer = new PreferMathMethodFixer();

	@Test
	public void testIfNoBodyLineReturnsSkip() throws Exception {
		assertSkipResult(fixer, TOPIC, "if_no_body_line_returns_skip", "if-else not auto-fixable");
	}

	@Test
	public void testIfPlainAssignAtFileStartFallsBackToBare() {
		// An if-else at lineIndex 0 has no line above, so the fix must not index
		// lines.get(-1). The trailing `return r;` keeps trailingReturnIndex in
		// bounds, so `declIndex >= 0` is the only guard stopping the decl+return
		// collapse from reading lines.get(-1). The check never reports at line 0
		// (an if-else is always nested), so this path is only reached by direct
		// invocation.
		final var lines = List.of(
				"if (a > b)",
				"\tr = a;",
				"else",
				"\tr = b;",
				"return r;"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(List.of("r = Math.max(a, b);"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testIfPlainAssignNoElseAtFileStartSkipsWithoutIndexingAbove() {
		// At lineIndex 0 with no else, tryPlainAssignShape returns null, so
		// fixIfShape's `lineIndex >= 1` guard must skip tryInitOverwriteShape,
		// which would otherwise read lines.get(-1) for the decl line.
		final var lines = List.of(
				"if (a > b)",
				"\tr = a;"
		);
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 0));
		assertEquals("if-else not auto-fixable", result.reason());
	}

	@Test
	public void testNoMatchClampUnbalancedParens() throws Exception {
		assertSkipResult(fixer, TOPIC, "no_match_clamp_unbalanced_parens", "parenthesized or multiline ternary");
	}

	@ParameterizedTest
	@ValueSource(strings = {"assert", "break", "continue", "return", "throw", "yield"})
	public void testNonDeclKeywordAboveDoesNotTriggerTrailingReturnCollapse(String keyword) {
		// DECL_LINE_PATTERN's lookahead must reject lines starting with a
		// control-flow keyword so `tryPlainAssignShape`'s decl+return collapse
		// doesn't silently delete a `<keyword> r;` statement above the if-else.
		// Synthetic input: the corruption scenario isn't reachable from
		// compilable Java (would require <keyword> r; AND the same name `r` as
		// the if-else target), so direct fixer invocation is the right level
		// for this regression test.
		final var lines = List.of(
				keyword + " r;",
				"if (a > b)",
				"\tr = a;",
				"else",
				"\tr = b;",
				"return r;"
		);
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 0));
		assertEquals(1, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("r = Math.max(a, b);"), result.replacement());
	}
}