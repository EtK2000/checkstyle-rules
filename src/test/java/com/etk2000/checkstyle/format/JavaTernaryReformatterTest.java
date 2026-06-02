package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.etk2000.checkstyle.MultilineCallFormattingCheck;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Direct-AST tests for {@link JavaTernaryReformatter} and
 * {@link MultilineCallFormattingCheck#resolvableTernaryLayoutQuestion} that drive guards the
 * slice-based topic pipeline cannot reach: the {@code STALE} column-mismatch bail, a ternary nested
 * inside a larger expression (not a bare call argument), and a ternary that is one of several plain
 * arguments (a surviving non-ternary violation the re-emission would not resolve).
 */
public class JavaTernaryReformatterTest {
	@Nonnull
	private static DetailAST findQuestion(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.QUESTION)
				return node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no QUESTION token in parsed source");
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("ternary", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	@Nonnull
	private static DetailAST resolvableQuestion(@Nonnull String source) throws Exception {
		final var root = parse(source);
		final var question = findQuestion(root);
		return MultilineCallFormattingCheck.resolvableTernaryLayoutQuestion(
				root, List.of(source.split("\n", -1)), question.getLineNo() - 1, question.getColumnNo()
		);
	}

	@Test
	public void reformatKeepsBlockCommentBranchVerbatim() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true\n\t\t\t\t? /* c1\n\t\t\t\tc2 */ \"a\" : \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaTernaryReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findQuestion(root));
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(true", "\t\t\t\t? /* c1", "\t\t\t\tc2 */ \"a\"", "\t\t\t\t: \"b\"", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatKeepsTextBlockBranchVerbatim() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true\n\t\t\t\t? \"\"\"\n\t\t\t\tabc\"\"\" : \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaTernaryReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findQuestion(root));
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(true", "\t\t\t\t? \"\"\"", "\t\t\t\tabc\"\"\"", "\t\t\t\t: \"b\"", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatKeepsTextBlockConditionVerbatim() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\"\"\"\n\t\t\t\tx\"\"\".isEmpty() ?\n\t\t\t\t\"a\"\n\t\t\t\t: \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaTernaryReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findQuestion(root));
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(\"\"\"", "\t\t\t\tx\"\"\".isEmpty()", "\t\t\t\t? \"a\"", "\t\t\t\t: \"b\"", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatKeepsTextBlockFalseBranchVerbatim() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true\n\t\t\t\t? \"a\" : \"\"\"\n\t\t\t\tabc\"\"\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaTernaryReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findQuestion(root));
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(true", "\t\t\t\t? \"a\"", "\t\t\t\t: \"\"\"", "\t\t\t\tabc\"\"\"", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatProducesCanonicalSpan() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true ?\n\t\t\t\t\"a\"\n\t\t\t\t: \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaTernaryReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findQuestion(root));
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(2, reformatted.fromIndex());
		assertEquals(5, reformatted.toIndex());
		assertEquals(List.of("\t\tmethod(true", "\t\t\t\t? \"a\"", "\t\t\t\t: \"b\"", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatReturnsStaleWhenColumnNoLongerHoldsTheQuestionToken() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true ?\n\t\t\t\t\"a\"\n\t\t\t\t: \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var question = findQuestion(root);
		final var mutated = new ArrayList<>(List.of(source.split("\n", -1)));
		mutated.set(question.getLineNo() - 1, "\t\t// shifted away");
		final var result = JavaTernaryReformatter.reformat(mutated, question);
		final var cannot = assertInstanceOf(SpanReformat.CannotReformat.class, result);
		assertEquals(SpanReformat.Reason.STALE, cannot.reason());
	}

	@Test
	public void resolvableIsNullForTernaryAmongMultiplePlainArgs() throws Exception {
		// two-arg call: not a single/compact-first ternary, so a whole-ternary re-emission cannot resolve
		// the shared-line violation the layout carries; the classifier must defer
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(cond ?\n\t\t\t\t\"a\" : \"b\", other);\n\t}\n\tvoid method(Object x, Object y) {\n\t}\n}";
		assertNull(resolvableQuestion(source));
	}

	@Test
	public void resolvableIsNullForTernaryNestedInLargerExpression() throws Exception {
		// the ternary is not a bare call argument (it sits inside a parenthesized additive expression),
		// so its parent is not the EXPR of an ELIST and the classifier returns null
		final var source = "class C {\n\tvoid m() {\n\t\tmethod((cond ? 1 : 2) + 3);\n\t}\n\tvoid method(int a) {\n\t}\n}";
		assertNull(resolvableQuestion(source));
	}

	@Test
	public void resolvableReturnsQuestionForTernaryLayoutViolation() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true ?\n\t\t\t\t\"a\"\n\t\t\t\t: \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var question = resolvableQuestion(source);
		assertNotNull(question);
		assertEquals(TokenTypes.QUESTION, question.getType());
	}
}