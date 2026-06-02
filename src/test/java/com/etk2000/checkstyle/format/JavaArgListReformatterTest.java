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
 * Direct-AST tests for {@link JavaArgListReformatter} and
 * {@link MultilineCallFormattingCheck#resolvableSharedLineArgs} covering paths the slice pipeline
 * cannot reach: the {@code STALE} guard when a boundary token no longer sits at its reported column,
 * and the classifier returning {@code null} for a special-inline receiver ({@code List.of}) that is
 * exempt from the shared-line rule. Positive controls exercise the {@code METHOD_CALL} and
 * {@code METHOD_DEF} owner arms.
 */
public class JavaArgListReformatterTest {
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

	@Nonnull
	private static DetailAST firstArg(@Nonnull DetailAST root) {
		final var elist = findFirst(root, TokenTypes.ELIST);
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				return child;
		}
		throw new AssertionError("no argument in the first ELIST");
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("arglist", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	@Test
	public void reformatAttachesLeakedCommentToVerbatimTextBlockArgument() throws Exception {
		// the `// leaked` trails the text-block argument's `,`, so it leads arg2's slice; it is lifted back
		// onto the text block's last verbatim line (after the hoisted `,`) while arg2 splits onto its own line
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t\"\"\"\n\t\t\t\ttb\"\"\", // leaked\n\t\t\t\tb\n\t\t);\n\t}\n\tvoid method(String a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t\"\"\"", "\t\t\t\ttb\"\"\", // leaked", "\t\t\t\tb", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatCollapsesArrayInitArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tnew int[]{1, 2}, other\n\t\t);\n\t}\n\tvoid method(int[] a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(List.of("\t\tmethod(new int[]{1, 2}, other);"), assertInstanceOf(SpanReformat.Reformatted.class, result).lines());
	}

	@Test
	public void reformatCollapsesAtExactWidthButSplitsOneColumnOver() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}";
		final var root = parse(source);
		final var lines = List.of(source.split("\n", -1));
		// the collapsed one-liner "\t\tmethod(1, 2, 3);" is exactly 24 columns (2 tabs expanded to 8 + 16 chars)
		final var atWidth = JavaArgListReformatter.reformat(new ArrayList<>(lines), findFirst(root, TokenTypes.METHOD_CALL), 24, 4);
		assertEquals(List.of("\t\tmethod(1, 2, 3);"), assertInstanceOf(SpanReformat.Reformatted.class, atWidth).lines());
		final var oneOver = JavaArgListReformatter.reformat(new ArrayList<>(lines), findFirst(root, TokenTypes.METHOD_CALL), 23, 4);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1,", "\t\t\t\t2,", "\t\t\t\t3", "\t\t);"), assertInstanceOf(SpanReformat.Reformatted.class, oneOver).lines());
	}

	@Test
	public void reformatCollapsesBracelessLambdaArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tx -> a(x), other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(List.of("\t\tmethod(x -> a(x), other);"), assertInstanceOf(SpanReformat.Reformatted.class, result).lines());
	}

	@Test
	public void reformatCollapsesNestedMultilinePlainArgOntoOneLine() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1,\n\t\t\t\tother(\n\t\t\t\t\t\t2\n\t\t\t\t)\n\t\t);\n\t}\n\tvoid method(int a, Object b) {\n\t}\n\tObject other(int x) {\n\t\treturn x;\n\t}\n}";
		final var root = parse(source);
		// a tiny width budget forces the split branch; the nested other(...) must still collapse to one line
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 10, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1,", "\t\t\t\tother(2)", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatCollapsesOneLineAnonClassArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tnew Runnable() { public void run() {} }, other\n\t\t);\n\t}\n\tvoid method(Runnable a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(List.of("\t\tmethod(new Runnable() { public void run() {} }, other);"), assertInstanceOf(SpanReformat.Reformatted.class, result).lines());
	}

	@Test
	public void reformatCollapsesOneLineBracedLambdaArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tx -> { a(); }, other\n\t\t);\n\t}\n\tvoid method(Runnable a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(List.of("\t\tmethod(x -> { a(); }, other);"), assertInstanceOf(SpanReformat.Reformatted.class, result).lines());
	}

	@Test
	public void reformatCollapsesOneLineSwitchArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tswitch (x) { case 1 -> a; default -> b; }, other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(List.of("\t\tmethod(switch (x) { case 1 -> a; default -> b; }, other);"), assertInstanceOf(SpanReformat.Reformatted.class, result).lines());
	}

	@Test
	public void reformatCollapsesSingleArgumentCallThatFits() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1\n\t\t);\n\t}\n\tvoid method(int a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(1);"), reformatted.lines());
	}

	@Test
	public void reformatCtorDefCollapsesToOneLineWhenItFits() throws Exception {
		final var source = "class Foo {\n\tFoo(\n\t\t\tint a, int b,\n\t\t\tint c\n\t) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.CTOR_DEF), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\tFoo(int a, int b, int c) {"), reformatted.lines());
	}

	@Test
	public void reformatDeclinesBracedLambdaConfigAsSpecialArg() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(x ->\n\t\t\t\t{\n\t\t\t\t\tuse(x);\n\t\t\t\t});\n\t}\n\tvoid method(Consumer c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var cannot = assertInstanceOf(SpanReformat.CannotReformat.class, result);
		assertEquals(SpanReformat.Reason.SPECIAL_ARG, cannot.reason());
	}

	@Test
	public void reformatDeclinesTernaryConfigAsSpecialArg() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(true\n\t\t\t\t? \"a\"\n\t\t\t\t: \"b\");\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var cannot = assertInstanceOf(SpanReformat.CannotReformat.class, result);
		assertEquals(SpanReformat.Reason.SPECIAL_ARG, cannot.reason());
	}

	@Test
	public void reformatEmitsCommentSwallowedSegmentVerbatim() throws Exception {
		// arg1 is `1 // note` + `+ 2`: collapsing it would pull `+ 2` inline behind the comment, so it is
		// emitted verbatim (its own lines) while arg2 still splits onto its own line around it
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1 // note\n\t\t\t\t\t\t+ 2,\n\t\t\t\t3\n\t\t);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1 // note", "\t\t\t\t+ 2,", "\t\t\t\t3", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatHoistsCommaBeforeNonLastArgTrailingComment() throws Exception {
		// a trailing `//` on a non-last arg blocks the one-liner but not the split: the `,` is hoisted ahead
		// of the comment so the argument keeps its comment on its own line
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1 // note\n\t\t\t\t, 2\n\t\t);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1, // note", "\t\t\t\t2", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatKeepsAnonClassArgumentVerbatimInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tnew Runnable() {\n\t\t\t\t\tpublic void run() {\n\t\t\t\t\t\tstep1();\n\t\t\t\t\t\tstep2();\n\t\t\t\t\t}\n\t\t\t\t}, other\n\t\t);\n\t}\n\tvoid method(Runnable a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tnew Runnable() {", "\t\t\t\t\tpublic void run() {", "\t\t\t\t\t\tstep1();", "\t\t\t\t\t\tstep2();", "\t\t\t\t\t}", "\t\t\t\t},", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsAnonClassWithFieldVerbatimInSplit() throws Exception {
		// the anon class's multi-line span comes from a field, not a method; it is still emitted verbatim
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tnew Object() {\n\t\t\t\t\tint n = 1;\n\t\t\t\t}, other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tnew Object() {", "\t\t\t\t\tint n = 1;", "\t\t\t\t},", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsBlockCommentArgumentVerbatimInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t/* c1\n\t\t\t\tc2 */ first,\n\t\t\t\t2\n\t\t);\n\t}\n\tvoid method(Object a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t/* c1", "\t\t\t\tc2 */ first,", "\t\t\t\t2", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatKeepsBracedLambdaArgumentVerbatimInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tother, x -> {\n\t\t\t\t\ta();\n\t\t\t\t\tb();\n\t\t\t\t}, z\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b, Object c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tother,", "\t\t\t\tx -> {", "\t\t\t\t\ta();", "\t\t\t\t\tb();", "\t\t\t\t},", "\t\t\t\tz", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsBracedLambdaArgumentVerbatimInSplitLastPosition() throws Exception {
		// the braced lambda is the LAST argument, so no trailing comma is appended after its closing brace
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tother, x -> {\n\t\t\t\t\ta();\n\t\t\t\t\tb();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tother,", "\t\t\t\tx -> {", "\t\t\t\t\ta();", "\t\t\t\t\tb();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsBracedLambdaOnOwnLineAfterCommaInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta,", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsColonSyntaxSwitchArgumentVerbatimInSplit() throws Exception {
		// a colon-syntax switch (whose case groups are braceless SLISTs with no RCURLY child) is kept verbatim
		// via the switch's own multi-line braces; the braceless case-group SLISTs are skipped by the predicate
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tswitch (x) {\n\t\t\t\t\tcase 1:\n\t\t\t\t\t\ta();\n\t\t\t\t\t\tbreak;\n\t\t\t\t\tdefault:\n\t\t\t\t\t\tb();\n\t\t\t\t}, other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tswitch (x) {", "\t\t\t\t\tcase 1:", "\t\t\t\t\ta();", "\t\t\t\t\tbreak;", "\t\t\t\t\tdefault:", "\t\t\t\t\tb();", "\t\t\t\t},", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsNestedCallWithBracedBlockVerbatimInSplit() throws Exception {
		// the multi-line braced lambda is nested inside a call argument; the predicate descends into it, so
		// the whole wrap(...) argument is emitted verbatim rather than crammed
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\twrap(x -> {\n\t\t\t\t\ta();\n\t\t\t\t\tb();\n\t\t\t\t}), other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\twrap(x -> {", "\t\t\t\t\ta();", "\t\t\t\t\tb();", "\t\t\t\t}),", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsSwitchArgumentVerbatimInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tswitch (x) {\n\t\t\t\t\tcase 1 -> a;\n\t\t\t\t\tdefault -> b;\n\t\t\t\t}, other\n\t\t);\n\t}\n\tvoid method(Object a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tswitch (x) {", "\t\t\t\t\tcase 1 -> a;", "\t\t\t\t\tdefault -> b;", "\t\t\t\t},", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsTextBlockAndBracedLambdaArgumentsVerbatim() throws Exception {
		// two independent verbatim triggers coexist: a text-block argument and a braced-lambda argument are
		// both emitted verbatim, while the plain third argument still splits around them
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t\"\"\"\n\t\t\t\ttb\"\"\", x -> {\n\t\t\t\t\ta();\n\t\t\t\t}, plain\n\t\t);\n\t}\n\tvoid method(String a, Runnable b, Object c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\t\"\"\"", "\t\t\t\ttb\"\"\",", "\t\t\t\tx -> {", "\t\t\t\t\ta();", "\t\t\t\t},", "\t\t\t\tplain", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatKeepsTextBlockArgumentVerbatimInSplit() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t\"\"\"\n\t\t\t\ttext\n\t\t\t\t\"\"\",\n\t\t\t\t2\n\t\t);\n\t}\n\tvoid method(Object a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t\"\"\"", "\t\t\t\ttext", "\t\t\t\t\"\"\",", "\t\t\t\t2", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatLiftsLeadingCommentFromBracedBlockArgument() throws Exception {
		// a `//` comment that leaked past the previous `,` and leads a braced-block (verbatim) argument is
		// lifted onto the previous argument's line, not re-emitted on its own line above the block
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta, // leaked\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // leaked", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsLeadingCommentOnFirstSegmentToOpeningParenHead() throws Exception {
		// the `// note` trails the `(` (leads the first segment), so there is no previous argument: it stays
		// on the `(` head line (arguments on later lines never join back onto it) while they split
		final var source = "class C {\n\tvoid m() {\n\t\tmethod( // note\n\t\t\t\ta, b\n\t\t);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod( // note", "\t\t\t\ta,", "\t\t\t\tb", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatLiftsLeadingCommentToPreviousArgument() throws Exception {
		// the `// note` trails arg1's `,`, so it leads arg2's slice; it is lifted back onto arg1's line and
		// arg2 splits onto its own line (same canonical as a comment written before the comma)
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1, // note\n\t\t\t\t2\n\t\t);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1, // note", "\t\t\t\t2", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatLiftsOnlyFirstOfTwoOwnLineCommentsBeforeBracedBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note1\n\t\t\t\t// note2\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note1", "\t\t\t\t// note2", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOnlyLiftedCommentWhenInteriorHasIdenticalText() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(x, // shared\n\t\t\t\t// shared\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object x, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\tx, // shared", "\t\t\t\t// shared", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentBeforeBracedBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentBeforeFirstVerbatimTextBlockArgumentToOpeningParenHead() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t// note\n\t\t\t\t\"\"\"\n\t\t\t\ttext\"\"\",\n\t\t\t\tother\n\t\t);\n\t}\n\tvoid method(String a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod( // note", "\t\t\t\t\"\"\"", "\t\t\t\ttext\"\"\",", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentBeforeLastVerbatimTextBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note\n\t\t\t\t\"\"\"\n\t\t\t\ttext\"\"\"\n\t\t);\n\t}\n\tvoid method(Object a, String b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t\"\"\"", "\t\t\t\ttext\"\"\"", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentBeforeVerbatimTextBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note\n\t\t\t\t\"\"\"\n\t\t\t\ttext\"\"\",\n\t\t\t\tother\n\t\t);\n\t}\n\tvoid method(Object a, String b, Object c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t\"\"\"", "\t\t\t\ttext\"\"\",", "\t\t\t\tother", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentSeparatedByBlankBeforeBracedBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note\n\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentWithLeadingBlankBeforeBracedBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\n\t\t\t\t// note\n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiftsOwnLineCommentWithTrailingWhitespaceBeforeBracedBlockArgument() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta,\n\t\t\t\t// note  \n\t\t\t\t() -> {\n\t\t\t\t\tbody();\n\t\t\t\t}\n\t\t);\n\t}\n\tvoid method(Object a, Runnable b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\ta, // note", "\t\t\t\t() -> {", "\t\t\t\t\tbody();", "\t\t\t\t}", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void reformatLiteralNewCollapsesToOneLineWhenItFits() throws Exception {
		final var source = "class C {\n\tstatic class Foo {\n\t\tFoo(int a, int b, int c) {\n\t\t}\n\t}\n\tvoid m() {\n\t\tnew Foo(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.LITERAL_NEW), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tnew Foo(1, 2, 3);"), reformatted.lines());
	}

	@Test
	public void reformatMethodCallCollapsesToOneLineWhenItFits() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(2, reformatted.fromIndex());
		assertEquals(5, reformatted.toIndex());
		assertEquals(List.of("\t\tmethod(1, 2, 3);"), reformatted.lines());
	}

	@Test
	public void reformatMethodDefCollapsesToOneLineWhenItFits() throws Exception {
		final var source = "class C {\n\tvoid m(\n\t\t\tint a, int b,\n\t\t\tint c\n\t) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_DEF), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\tvoid m(int a, int b, int c) {"), reformatted.lines());
	}

	@Test
	public void reformatReturnsStaleWhenBoundaryColumnNoLongerHoldsAComma() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}";
		final var root = parse(source);
		final var owner = findFirst(root, TokenTypes.METHOD_CALL);
		final var mutated = new ArrayList<>(List.of(source.split("\n", -1)));
		mutated.set(3, "\t\t\t\tx");
		final var result = JavaArgListReformatter.reformat(mutated, owner, 120, 4);
		final var cannot = assertInstanceOf(SpanReformat.CannotReformat.class, result);
		assertEquals(SpanReformat.Reason.STALE, cannot.reason());
	}

	@Test
	public void reformatSkipsLiftedCommentLineOnVerbatimSegment() throws Exception {
		// arg2 both leads with a leaked `// leaked` (lifted onto arg1's line) AND has a mid-argument `// mid`
		// that forces it verbatim; the lifted-comment line is not re-emitted in the verbatim block
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta, // leaked\n\t\t\t\tb // mid\n\t\t\t\t\t\t+ c,\n\t\t\t\td\n\t\t);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tmethod(", "\t\t\t\ta, // leaked", "\t\t\t\tb // mid", "\t\t\t\t+ c,", "\t\t\t\td", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatSplitsEachArgumentWhenTheOneLineFormExceedsTheWidth() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n\tvoid method(int a, int b, int c) {\n\t}\n}";
		final var root = parse(source);
		// a tiny width budget forces the split branch even though the one-line form is short
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 10, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		// the split-path return spans the same `method(`..`);` line range as the collapse path
		assertEquals(2, reformatted.fromIndex());
		assertEquals(5, reformatted.toIndex());
		assertEquals(List.of("\t\tmethod(", "\t\t\t\t1,", "\t\t\t\t2,", "\t\t\t\t3", "\t\t);"), reformatted.lines());
	}

	@Test
	public void reformatSuperCtorCallCollapsesToOneLineWhenItFits() throws Exception {
		final var source = "class C extends B {\n\tC() {\n\t\tsuper(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.SUPER_CTOR_CALL), 120, 4);
		final var reformatted = assertInstanceOf(SpanReformat.Reformatted.class, result);
		assertEquals(List.of("\t\tsuper(1, 2, 3);"), reformatted.lines());
	}

	@Test
	public void reformatTrimsBlankLineBetweenVerbatimArgumentAndSeparator() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\t() -> {\n\t\t\t\t\tb();\n\t\t\t\t}\n\n\t\t\t\t, y\n\t\t);\n\t}\n\tvoid method(Runnable a, Object b) {\n\t}\n}";
		final var root = parse(source);
		final var result = JavaArgListReformatter.reformat(new ArrayList<>(List.of(source.split("\n", -1))), findFirst(root, TokenTypes.METHOD_CALL), 120, 4);
		assertEquals(
				List.of("\t\tmethod(", "\t\t\t\t() -> {", "\t\t\t\t\tb();", "\t\t\t\t},", "\t\t\t\ty", "\t\t);"),
				assertInstanceOf(SpanReformat.Reformatted.class, result).lines()
		);
	}

	@Test
	public void resolvableArgListOwnerDefersForInlineBlockArgument() throws Exception {
		// a lambda argument off the ( line is an inline-block move (MSG_LAMBDA_NOT_ON_OPENING), not a plain one
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\tx -> {\n\t\t\t\t\tuse(x);\n\t\t\t\t});\n\t}\n\tvoid method(Consumer c) {\n\t}\n}";
		final var root = parse(source);
		final var arg = firstArg(root);
		assertNull(MultilineCallFormattingCheck.resolvableArgListOwner(root, List.of(source.split("\n", -1)), arg.getLineNo() - 1, arg.getColumnNo()));
	}

	@Test
	public void resolvableArgListOwnerDefersForTernaryArgument() throws Exception {
		// the condition off the ( line makes this a ternary-internal move (MSG_TERNARY_NOT_ON_OPENING), not a plain one
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ttrue\n\t\t\t\t\t\t? \"a\"\n\t\t\t\t\t\t: \"b\"\n\t\t);\n\t}\n\tvoid method(Object a) {\n\t}\n}";
		final var root = parse(source);
		final var arg = firstArg(root);
		assertNull(MultilineCallFormattingCheck.resolvableArgListOwner(root, List.of(source.split("\n", -1)), arg.getLineNo() - 1, arg.getColumnNo()));
	}

	@Test
	public void resolvableArgListOwnerReturnsOwnerForPlainOpeningViolation() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(1,\n\t\t\t\t2\n\t\t);\n\t}\n\tvoid method(int a, int b) {\n\t}\n}";
		final var root = parse(source);
		final var arg = firstArg(root);
		final var owner = MultilineCallFormattingCheck.resolvableArgListOwner(root, List.of(source.split("\n", -1)), arg.getLineNo() - 1, arg.getColumnNo());
		assertNotNull(owner);
		assertEquals(TokenTypes.METHOD_CALL, owner.getType());
	}

	@Test
	public void resolvableSharedLineArgsIsNullForSpecialInlineReceiver() throws Exception {
		// List.of(...) is exempt from the shared-line rule, so the classifier must not offer it for reformat
		final var source = "class C {\n\tvoid m() {\n\t\tList.of(\n\t\t\t\t1, 2,\n\t\t\t\t3\n\t\t);\n\t}\n}";
		final var root = parse(source);
		final var elist = findFirst(root, TokenTypes.ELIST);
		DetailAST secondArg = null;
		var seen = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA && ++seen == 2) {
				secondArg = child;
				break;
			}
		}
		assertNull(MultilineCallFormattingCheck.resolvableSharedLineArgs(
				root, List.of(source.split("\n", -1)), secondArg.getLineNo() - 1, secondArg.getColumnNo()
		));
	}

	@Test
	public void resolvableSharedLineArgsReturnsOwnerForSharedLineViolation() throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\tmethod(\n\t\t\t\ta, b,\n\t\t\t\tc\n\t\t);\n\t}\n\tvoid method(Object a, Object b, Object c) {\n\t}\n}";
		final var root = parse(source);
		final var arg = firstArg(root);
		final var owner = MultilineCallFormattingCheck.resolvableSharedLineArgs(root, List.of(source.split("\n", -1)), arg.getLineNo() - 1, arg.getColumnNo());
		assertNotNull(owner);
		assertEquals(TokenTypes.METHOD_CALL, owner.getType());
	}
}