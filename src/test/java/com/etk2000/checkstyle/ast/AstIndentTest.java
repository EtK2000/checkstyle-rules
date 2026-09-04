package com.etk2000.checkstyle.ast;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Tests for {@link AstIndent#tabsFor}. Every snippet is wrapped in a host class and method, so a
 * statement written directly in the body answers 2 (one level for the class body, one for the
 * method body) and each row's expectation reads as "2 plus the levels the construct opens".
 *
 * <p>The snippets are deliberately written with no indentation of their own. {@code tabsFor} reads
 * no line text, so their layout cannot influence the answer, and writing them flat keeps that
 * honest: a row that only passed because its source happened to be indented correctly would be
 * indistinguishable from one that measured the tree.
 */
public class AstIndentTest {
	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> bodyShapes() {
		return Stream.of(
				Arguments.of("if", "if (c)\nmark();", "if (c) {\nmark();\nmore();\n}", 3),
				Arguments.of("else", "if (c)\nmore();\nelse\nmark();", "if (c) {\nmore();\n}\nelse {\nmark();\n}", 3),
				Arguments.of("while", "while (c)\nmark();", "while (c) {\nmark();\nmore();\n}", 3),
				Arguments.of("for", "for (var i = 0; i < n; ++i)\nmark();", "for (var i = 0; i < n; ++i) {\nmark();\n}", 3),
				Arguments.of("for-each", "for (var v : list)\nmark();", "for (var v : list) {\nmark();\n}", 3),
				Arguments.of("do", "do\nmark();\nwhile (c);", "do {\nmark();\nmore();\n}\nwhile (c);", 3),
				Arguments.of("if on one line", "if (c) mark();", "if (c) { mark(); more(); }", 3),
				Arguments.of("do on one line", "do mark();\nwhile (c);", "do { mark(); more(); }\nwhile (c);", 3)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> bracedCaseShapes() {
		return Stream.of(
				Arguments.of("sole statement", "switch (x) {\ncase 1: {\nint v = 1;\nmark(v);\nbreak;\n}\n}", 4),
				Arguments.of("trailing sibling", "switch (x) {\ncase 1: {\nint v = 1;\nmark(v);\n}\nmore();\nbreak;\n}", 4),
				Arguments.of("block later in the body", "switch (x) {\ncase 1:\nmore();\n{\nint v = 1;\nmark(v);\n}\nbreak;\n}", 5),
				Arguments.of("unbraced group", "switch (x) {\ncase 1:\nmark();\nbreak;\n}", 4)
		);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findIdent(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() == TokenTypes.IDENT && name.equals(node.getText()))
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findIdent(child, name);
			if (found != null)
				return found;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static String host(@Nonnull String body) {
		return "import java.util.List;\nclass T {\nvoid m(boolean c, boolean d, int n, int x, Object o, List<String> list) {\n"
				+ body + "\n}\n}";
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> nestedBlocks() {
		return Stream.of(
				Arguments.of("bare block", "{\nmark();\n}", 3),
				Arguments.of("two bare blocks", "{\n{\nmark();\n}\n}", 4),
				Arguments.of("three bare blocks", "{\n{\n{\nmark();\n}\n}\n}", 5),
				Arguments.of("lambda block", "list.forEach(v -> {\nmark();\n});", 3),
				Arguments.of("nested lambda blocks", "list.forEach(a -> {\nlist.forEach(b -> {\nmark();\n});\n});", 4),
				Arguments.of("local class", "class L {\nvoid f() {\nmark();\n}\n}", 4),
				Arguments.of("anonymous class", "Thread t = new Thread() {\npublic void run() {\nmark();\n}\n};", 4),
				Arguments.of("if inside while, braceless", "while (c)\nif (d)\nmark();", 4),
				Arguments.of("if inside while, braced", "while (c) {\nif (d) {\nmark();\nmore();\n}\n}", 4),
				Arguments.of("try inside try", "try {\ntry {\nmark();\n}\nfinally {\nmore();\n}\n}\nfinally {\nmore();\n}", 4),
				Arguments.of("switch inside if", "if (c) {\nswitch (x) {\ncase 1:\nmark();\nbreak;\n}\n}", 5)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> nonBodyChildren() {
		return Stream.of(
				Arguments.of("if condition", "if (mark())\nmore();", 2),
				Arguments.of("while condition", "while (mark())\nmore();", 2),
				Arguments.of("do condition", "do\nmore();\nwhile (mark());", 2),
				Arguments.of("for init", "for (var i = mark(); i < n; ++i)\nmore();", 2),
				Arguments.of("for condition", "for (var i = 0; mark(); ++i)\nmore();", 2),
				Arguments.of("for update", "for (var i = 0; i < n; mark())\nmore();", 2),
				Arguments.of("for-each iterable", "for (var v : mark())\nmore();", 2),
				Arguments.of("switch selector", "switch (mark()) {\ncase 1:\nmore();\nbreak;\n}", 2),
				Arguments.of("synchronized lock", "synchronized (mark()) {\nmore();\n}", 2),
				Arguments.of("try resource", "try (var r = mark()) {\nmore();\n}", 2),
				Arguments.of("anonymous class ctor argument", "Thread t = new Thread(mark()) {\npublic void run() {\n}\n};", 2)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> sourceTextVariants() {
		return Stream.of(
				Arguments.of("canonical tabs", "class T {\n\tvoid m(boolean c) {\n\t\tif (c) {\n\t\t\tmark();\n\t\t\tmore();\n\t\t}\n\t}\n}"),
				Arguments.of("four-space indent", "class T {\n    void m(boolean c) {\n        if (c) {\n            mark();\n            more();\n        }\n    }\n}"),
				Arguments.of("no indent at all", "class T {\nvoid m(boolean c) {\nif (c) {\nmark();\nmore();\n}\n}\n}"),
				Arguments.of("over-indented body", "class T {\n\tvoid m(boolean c) {\n\t\tif (c) {\n\t\t\t\t\t\t\tmark();\n\t\t\t\t\t\t\tmore();\n\t\t}\n\t}\n}"),
				Arguments.of("under-indented body", "class T {\n\t\t\t\t\tvoid m(boolean c) {\n\tif (c) {\nmark();\nmore();\n}\n}\n}"),
				Arguments.of("CRLF endings", "class T {\r\n\tvoid m(boolean c) {\r\n\t\tif (c) {\r\n\t\t\tmark();\r\n\t\t\tmore();\r\n\t\t}\r\n\t}\r\n}")
		);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> switchShapes() {
		return Stream.of(
				Arguments.of("colon case body", "switch (x) {\ncase 1:\nmark();\nbreak;\n}", 4),
				Arguments.of("colon default body", "switch (x) {\ndefault:\nmark();\n}", 4),
				Arguments.of("fall-through labels", "switch (x) {\ncase 1:\ncase 2:\nmark();\nbreak;\n}", 4),
				Arguments.of("arrow single statement", "switch (x) {\ncase 1 -> mark();\ndefault -> more();\n}", 3),
				Arguments.of("arrow block", "switch (x) {\ncase 1 -> {\nmark();\n}\ndefault -> more();\n}", 4),
				Arguments.of("arrow default", "switch (x) {\ncase 1 -> more();\ndefault -> mark();\n}", 3),
				Arguments.of("comma labels", "switch (x) {\ncase 1, 2 -> mark();\ndefault -> more();\n}", 3),
				Arguments.of("pattern label", "switch (o) {\ncase Integer i -> mark();\ndefault -> more();\n}", 3),
				Arguments.of("guarded pattern", "switch (o) {\ncase String s when s.isEmpty() -> mark();\ndefault -> more();\n}", 3),
				Arguments.of("switch expression rule", "var y = switch (x) {\ncase 1 -> mark();\ndefault -> 2;\n};", 3),
				Arguments.of("nested switch", "switch (x) {\ncase 1:\nswitch (n) {\ncase 2:\nmark();\nbreak;\n}\nbreak;\n}", 6)
		);
	}

	@CheckReturnValue
	private static int tabsForMark(@Nonnull String body) throws Exception {
		return tabsForMarkIn(host(body));
	}

	@CheckReturnValue
	private static int tabsForMarkIn(@Nonnull String source) throws Exception {
		final var mark = findIdent(AstTestSupport.parseSource(source), "mark");
		assertNotNull(mark, "no mark() in the snippet");
		return AstIndent.tabsFor(mark);
	}

	@CheckReturnValue
	@Nonnull
	private static Stream<Arguments> zeroContributors() {
		return Stream.of(
				Arguments.of("method call chain", "o.toString().equals(mark());", 2),
				Arguments.of("cast", "((String) mark()).length();", 2),
				Arguments.of("ternary branch", "var y = c ? mark() : null;", 2),
				Arguments.of("array index", "list.get(mark()).length();", 2),
				Arguments.of("declaration initializer", "var v = mark();", 2),
				Arguments.of("expression lambda body", "list.forEach(v -> mark());", 2),
				Arguments.of("label over a statement", "lbl:\nmark();", 2),
				Arguments.of("label over a block", "lbl:\n{\nmark();\n}", 3),
				Arguments.of("label over a loop", "lbl:\nwhile (c) {\nmark();\n}", 3),
				Arguments.of("generic type argument", "List<String> v = mark();", 2),
				Arguments.of("instanceof pattern in a condition", "if (o instanceof String s && s.equals(mark()))\nmore();", 2),
				Arguments.of("new expression argument", "var v = new StringBuilder(mark());", 2)
		);
	}

	@MethodSource("bodyShapes")
	@ParameterizedTest(name = "{0}")
	public void bodyAddsOneLevelWhetherBracedOrNot(
			@Nonnull String name, @Nonnull String braceless, @Nonnull String braced, int expected
	) throws Exception {
		assertEquals(expected, tabsForMark(braceless), "braceless");
		assertEquals(expected, tabsForMark(braced), "braced");
	}

	@MethodSource("bracedCaseShapes")
	@ParameterizedTest(name = "{0}")
	public void bracedCaseBlockSharesTheLabelLevel(
			@Nonnull String name, @Nonnull String body, int expected
	) throws Exception {
		assertEquals(expected, tabsForMark(body));
	}

	@Test
	public void closingBraceSitsAtItsOpenersLevel() throws Exception {
		final var ast = AstTestSupport.parseSource(host("if (c) {\nmark();\nmore();\n}"));
		final var ifAst = AstTestSupport.findFirst(ast, TokenTypes.LITERAL_IF);
		assertNotNull(ifAst);
		final var block = ifAst.findFirstToken(TokenTypes.SLIST);
		assertNotNull(block);
		final var rcurly = block.findFirstToken(TokenTypes.RCURLY);
		assertNotNull(rcurly);
		assertEquals(2, AstIndent.tabsFor(rcurly));
		assertEquals(3, tabsForMark("if (c) {\nmark();\nmore();\n}"));
	}

	@Test
	public void commentNodesDoNotShiftTheAnswer() throws Exception {
		AstTestSupport.assertSameWithAndWithoutComments(
				host("if (c) {\n// leading\nmark();\nmore();\n}"),
				3,
				ast -> AstIndent.tabsFor(requireNonNull(findIdent(ast, "mark")))
		);
	}

	@Test
	public void deepNestingDoesNotOverflowTheStack() throws Exception {
		final var depth = 200;
		assertEquals(2 + depth, tabsForMark("{\n".repeat(depth) + "mark();\n" + "}\n".repeat(depth)));
	}

	@Test
	public void elseIfChainStaysFlatHoweverLongItGrows() throws Exception {
		assertEquals(3, tabsForMark("if (c)\nmore();\nelse if (d)\nmark();"));
		assertEquals(3, tabsForMark("if (c)\nmore();\nelse if (d)\nmore();\nelse if (x > 0)\nmark();"));
		assertEquals(3, tabsForMark("if (c)\nmore();\nelse if (d)\nmore();\nelse\nmark();"));
		assertEquals(3, tabsForMark("if (c)\nmore();\nelse if (d)\nmore();\nelse if (x > 0)\nmore();\nelse\nmark();"));
		assertEquals(3, tabsForMark("if (c) {\nmore();\n}\nelse if (d) {\nmark();\nmore();\n}"));
		// an else whose body is an if parses identically however it was written, so the collapsed
		// spelling is the only one tabsFor can answer for
		assertEquals(3, tabsForMark("if (c)\nmore();\nelse\nif (d)\nmark();"));
	}

	@MethodSource("zeroContributors")
	@ParameterizedTest(name = "{0}")
	public void interiorNodeAnswersItsStatementsIndent(
			@Nonnull String name, @Nonnull String body, int expected
	) throws Exception {
		assertEquals(expected, tabsForMark(body));
	}

	@MethodSource("nestedBlocks")
	@ParameterizedTest(name = "{0}")
	public void nestingComposesAcrossConstructs(
			@Nonnull String name, @Nonnull String body, int expected
	) throws Exception {
		assertEquals(expected, tabsForMark(body));
	}

	@MethodSource("nonBodyChildren")
	@ParameterizedTest(name = "{0}")
	public void nonBodyChildrenAddNothing(
			@Nonnull String name, @Nonnull String body, int expected
	) throws Exception {
		assertEquals(expected, tabsForMark(body));
	}

	@MethodSource("sourceTextVariants")
	@ParameterizedTest(name = "{0}")
	public void sourceTextDoesNotChangeTheAnswer(@Nonnull String name, @Nonnull String source) throws Exception {
		assertEquals(3, tabsForMarkIn(source));
	}

	@MethodSource("switchShapes")
	@ParameterizedTest(name = "{0}")
	public void switchShapeLevels(@Nonnull String name, @Nonnull String body, int expected) throws Exception {
		assertEquals(expected, tabsForMark(body));
	}

	@Test
	public void topLevelPositionsAreZero() throws Exception {
		final var ast = AstTestSupport.parseSource("import java.util.List;\nclass T {\nint f;\n}");
		assertEquals(0, AstIndent.tabsFor(ast));
		final var imported = AstTestSupport.findFirst(ast, TokenTypes.IMPORT);
		assertNotNull(imported);
		assertEquals(0, AstIndent.tabsFor(imported));
		final var classDef = AstTestSupport.findFirst(ast, TokenTypes.CLASS_DEF);
		assertNotNull(classDef);
		assertEquals(0, AstIndent.tabsFor(classDef));
		final var field = AstTestSupport.findFirst(ast, TokenTypes.VARIABLE_DEF);
		assertNotNull(field);
		assertEquals(1, AstIndent.tabsFor(field));
	}
}