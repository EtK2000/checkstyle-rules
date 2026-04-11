package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Tests for {@link ControlFlowBracesCheck} tier determination and
 * expression classification using direct AST inspection.
 */
public class ControlFlowBracesCheckTierTest {
	@Nonnull
	private static DetailAST findDoBody(@Nonnull String doWhileCode) throws Exception {
		final var source = "class T { void f(int x, int y) { " + doWhileCode + " } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		if (doAst == null)
			throw new AssertionError("No LITERAL_DO found in: " + doWhileCode);
		return doAst.getFirstChild();
	}

	@Nonnull
	private static DetailAST findDoNode(@Nonnull String doWhileCode) throws Exception {
		final var source = "class T { void f(int x, int y) { " + doWhileCode + " } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		if (doAst == null)
			throw new AssertionError("No LITERAL_DO found in: " + doWhileCode);
		return doAst;
	}

	private static DetailAST findFirst(@Nonnull DetailAST root, int tokenType) {
		for (var node = root; node != null; node = node.getNextSibling()) {
			if (node.getType() == tokenType)
				return node;
			final var child = findFirst(node.getFirstChild(), tokenType);
			if (child != null)
				return child;
		}
		return null;
	}

	static Stream<Arguments> tierProvider() {
		return Stream.of(
				Arguments.of("do --x; while (x > 0);", ControlFlowBracesCheck.TIER_1),
				Arguments.of("do x--; while (x > 0);", ControlFlowBracesCheck.TIER_1),
				Arguments.of("do x++; while (x > 0);", ControlFlowBracesCheck.TIER_1),
				Arguments.of("do --x; while (x > 0 && x < 100);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do --x; while (x > 0 || x < 100);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do System.out.println(x); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = System.out.hashCode(); while (x > 0);", ControlFlowBracesCheck.TIER_2),
				Arguments.of("do x = x + y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do x += 5 * y; while (x > 0);", ControlFlowBracesCheck.TIER_3),
				Arguments.of("do new Object(); while (x > 0);", ControlFlowBracesCheck.TIER_3)
		);
	}

	@ParameterizedTest
	@ValueSource(strings = {"do x.a().b(); while (x > 0);", "do new Object(); while (x > 0);"})
	void testIsSimpleFalse(String doWhileCode) throws Exception {
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(findDoBody(doWhileCode)));
	}

	@Test
	public void testIsSimpleNonExprReturnsFalse() throws Exception {
		final var source = "class T { void f(int x) { do { --x; } while (x > 0); } }";
		final var tmp = File.createTempFile("tier", ".java");
		tmp.deleteOnExit();
		Files.writeString(tmp.toPath(), source);
		final var ast = JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		final var doAst = findFirst(ast, TokenTypes.LITERAL_DO);
		assertFalse(ControlFlowBracesCheck.isSimpleExpression(doAst.getFirstChild()));
	}

	@ParameterizedTest
	@ValueSource(strings = {"do x = 0; while (x > 0);", "do x &= 1; while (x > 0);",
			"do x |= 1; while (x > 0);", "do x >>>= 1; while (x > 0);",
			"do x ^= 1; while (x > 0);", "do x /= 2; while (x > 0);",
			"do x -= 1; while (x > 0);", "do x %= 3; while (x > 0);",
			"do x += 5; while (x > 0);", "do x <<= 1; while (x > 0);",
			"do x >>= 1; while (x > 0);", "do x *= 2; while (x > 0);",
			"do --x; while (x > 0);", "do ++x; while (x > 0);",
			"do f(x, y); while (x > 0);", "do System.out.println(x); while (x > 0);",
			"do x--; while (x > 0);", "do x++; while (x > 0);"})
	void testIsSimpleTrue(String doWhileCode) throws Exception {
		assertTrue(ControlFlowBracesCheck.isSimpleExpression(findDoBody(doWhileCode)));
	}

	@MethodSource("tierProvider")
	@ParameterizedTest
	void testTierClassification(String doWhileCode, int expectedTier) throws Exception {
		final var doAst = findDoNode(doWhileCode);
		assertEquals(expectedTier, ControlFlowBracesCheck.determineTier(doAst.getFirstChild(), doAst));
	}
}