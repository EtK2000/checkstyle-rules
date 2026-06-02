package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

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
 * Direct-AST tests for {@link JavaPostDelayedReformatter}: the one-line unwrap of a single-statement
 * {@code postDelayed} lambda that fits, and the {@code }, delay);} fallback when it does not (a
 * multi-statement body). The slice pipeline exercises these end-to-end; these pin the two branches in
 * isolation.
 */
public class JavaPostDelayedReformatterTest {
	@Nonnull
	private static DetailAST findPostDelayed(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_CALL && ArgLayoutClassifier.isPostDelayedWithInlineBlock(node))
				return node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no postDelayed(bracedLambda, delay) call in parsed source");
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("postdelayed", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	@Test
	public void reformatDeclinesLineCommentInBody() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tdoThing(); // note\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		assertEquals(SpanReformat.Reason.COMMENT_ON_JOINED_LINE, assertInstanceOf(SpanReformat.CannotReformat.class, result).reason());
	}

	@Test
	public void reformatDeclinesShiftedColumn() throws Exception {
		// the block's `{` no longer sits at the reported column (a same-pass edit shifted the char): STALE
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tSystem.out.println(\"x\");\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var call = findPostDelayed(parse(source));
		final var lines = new ArrayList<>(List.of(source.split("\n", -1)));
		lines.set(2, "\t\thandler.postDelayed(() -> x");
		final var result = JavaPostDelayedReformatter.reformat(lines, call, 120, 4);
		assertEquals(SpanReformat.Reason.STALE, assertInstanceOf(SpanReformat.CannotReformat.class, result).reason());
	}

	@Test
	public void reformatDeclinesStaleCoordinates() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tSystem.out.println(\"x\");\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var call = findPostDelayed(parse(source));
		final var truncated = new ArrayList<>(List.of(source.split("\n", -1)).subList(0, 4));
		final var result = JavaPostDelayedReformatter.reformat(truncated, call, 120, 4);
		assertEquals(SpanReformat.Reason.STALE, assertInstanceOf(SpanReformat.CannotReformat.class, result).reason());
	}

	@Test
	public void reformatDeclinesTextBlockInBody() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tlog(\"\"\"\n\t\t\ttext\"\"\");\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		assertEquals(SpanReformat.Reason.MULTILINE_LITERAL, assertInstanceOf(SpanReformat.CannotReformat.class, result).reason());
	}

	@Test
	public void reformatKeepsAnonClassWithStackedClose() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(\n\t\t\t\tnew Runnable() {\n\t\t\t\t\tpublic void run() {\n\t\t\t\t\t\tdoThing();\n\t\t\t\t\t}\n\t\t\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(
				List.of("\t\thandler.postDelayed(new Runnable() {", "\t\t\tpublic void run() {", "\t\t\t\tdoThing();", "\t\t\t}", "\t\t}, 1000);"),
				reformatted.lines()
		);
	}

	@Test
	public void reformatKeepsMultiStatementBodyWithStackedClose() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tSystem.out.println(\"a\");\n\t\t\tSystem.out.println(\"b\");\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(
				List.of("\t\thandler.postDelayed(() -> {", "\t\t\tSystem.out.println(\"a\");", "\t\t\tSystem.out.println(\"b\");", "\t\t}, 1000);"),
				reformatted.lines()
		);
	}

	@Test
	public void reformatKeepsNonExpressionBodyWithStackedClose() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\treturn;\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\thandler.postDelayed(() -> {", "\t\t\treturn;", "\t\t}, 1000);"), reformatted.lines());
	}

	@Test
	public void reformatUnwrapsSingleStatementBodyToOneLine() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tSystem.out.println(\"x\");\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaPostDelayedReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findPostDelayed(root), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\thandler.postDelayed(() -> System.out.println(\"x\"), 1000);"), reformatted.lines());
	}

	@Test
	public void resolvablePostDelayedFindsCallFromMethodCallAndRparen() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> {\n\t\t\tdoThing();\n\t\t},\n\t\t\t\t1000\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var call = findPostDelayed(root);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		assertSame(call, MultilineCallFormattingCheck.resolvablePostDelayed(root, call.getLineNo() - 1, call.getColumnNo()));
		assertSame(call, MultilineCallFormattingCheck.resolvablePostDelayed(root, rparen.getLineNo() - 1, rparen.getColumnNo()));
	}

	@Test
	public void resolvablePostDelayedReturnsNullForSingleLine() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\thandler.postDelayed(() -> { doThing(); }, 1000);\n\t}\n}";
		final var root = parse(source);
		final var call = findPostDelayed(root);
		assertNull(MultilineCallFormattingCheck.resolvablePostDelayed(root, call.getLineNo() - 1, call.getColumnNo()));
	}
}