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

import javax.annotation.Nonnull;

/**
 * Fixer-internal tests for the ternary and argument-list re-layout dispatch. The slice pipeline
 * exercises the reachable {@code applyTernaryReformat}/{@code applyArgListReformat} outcomes (fix,
 * skips); these pin the one arm each cannot reach: mapping the reformatter's defensive {@code STALE}
 * geometry result to {@code MULTILINE_PUT_SKIP_STALE}. {@code fix()} re-parses its lines, so a stale
 * coordinate cannot survive through the public entry point, hence the direct calls.
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

		final var result = MultilineCallFormattingFixer.applyArgListReformat(stale, owner);
		final var skip = assertInstanceOf(SkipResult.class, result);
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

		final var result = MultilineCallFormattingFixer.applyTernaryReformat(stale, question);
		final var skip = assertInstanceOf(SkipResult.class, result);
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_STALE, skip.reason());
	}

	@Test
	public void fixReturnsUnsupportedSkipWhenParseFails() {
		final var unterminated = List.of("class T {", "\tString s = \"\"\"", "\tunterminated;");
		final var result = new MultilineCallFormattingFixer().fix(unterminated, 0, 0);
		final var skip = assertInstanceOf(SkipResult.class, result);
		assertEquals(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED, skip.reason());
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