package com.etk2000.checkstyle.format;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.JavaParser.Options;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

/**
 * Direct-AST tests for {@link ArgLayoutClassifier#isSpecialLayoutConfiguration}: a call whose argument
 * list must keep its opening-line shape (a lambda/anonymous-class/ternary/new configuration, or the
 * name-based {@code postDelayed}/{@code computeIfAbsent}/{@code put} inline-block configs) versus a
 * plain flat list the reformatter may re-lay-out. The discriminating case is a special call among
 * MULTIPLE arguments: that is a plain list (the special call is fine on its own line), not a special
 * configuration. The Context-dependent {@code getString}/{@code getQuantityString} configs are gated
 * by the check, not here, so the tests pass an empty context-special predicate.
 */
public class ArgLayoutClassifierTest {
	private static Stream<Arguments> bracedBlocks() {
		return Stream.of(
				Arguments.of("multi-line braced lambda", "x -> {\n\t\t\t\tuse(x);\n\t\t\t\t}", true),
				Arguments.of("one-line braced lambda", "x -> { use(x); }", false),
				Arguments.of("multi-line anonymous class", "new Runnable() {\n\t\t\t\tpublic void run() {}\n\t\t\t\t}", true),
				Arguments.of("one-line anonymous class", "new Runnable() { public void run() {} }", false),
				Arguments.of("multi-line arrow switch", "switch (x) {\n\t\t\t\tcase 1 -> a;\n\t\t\t\tdefault -> b;\n\t\t\t\t}", true),
				Arguments.of("one-line arrow switch", "switch (x) { case 1 -> a; default -> b; }", false),
				Arguments.of("multi-line colon switch (braceless case SLIST skipped)", "switch (x) {\n\t\t\t\tcase 1:\n\t\t\t\t\ta();\n\t\t\t\t\tbreak;\n\t\t\t\tdefault:\n\t\t\t\t\tb();\n\t\t\t\t}", true),
				Arguments.of("multi-line block nested in a call", "wrap(x -> {\n\t\t\t\ta();\n\t\t\t\t})", true),
				Arguments.of("braceless lambda", "x -> a(x)", false),
				Arguments.of("plain literal", "1", false),
				Arguments.of("one-line array initializer", "new int[]{1, 2}", false),
				Arguments.of("multi-line array initializer", "new int[]{\n\t\t\t\t1,\n\t\t\t\t2\n\t\t\t\t}", false)
		);
	}

	@Nonnull
	private static DetailAST callNamed(@Nonnull DetailAST root, @Nonnull String name) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if ((node.getType() == TokenTypes.METHOD_CALL || node.getType() == TokenTypes.LITERAL_NEW)
					&& ArgLayoutClassifier.isMethodCallNamed(node, name))
				return node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		throw new AssertionError("no call named " + name + " in parsed source");
	}

	/**
	 * One case per navigation site a comment node can derail, plus the boundary cases that must
	 * stay a plain list. Each is asserted against both parses, so the expectation is pinned to the
	 * behaviour that ships today rather than to whatever the comment-bearing tree happens to give.
	 */
	private static Stream<Arguments> commentPositions() {
		return Stream.of(
				Arguments.of("block comment before the comma", "target", "target(this /*c*/, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("line comment before the comma", "target", "target(this // c\n\t\t\t\t, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("comment before a this first arg", "target", "target(/*c*/ this, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("comment before a resource-id first arg", "target", "target(/*c*/ R.string.name, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("comment inside a resource-id first arg", "target", "target(R./*c*/string.name, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("comment before a special inline receiver", "target", "target(/*c*/ List.of(\n\t\t\t\t1, 2, 3\n\t\t\t\t))", true),
				Arguments.of("comment before a special inline method name", "target", "target(List./*c*/of(\n\t\t\t\t1, 2, 3\n\t\t\t\t))", true),
				Arguments.of("comment before a sole ternary", "target", "target(/*c*/ cond\n\t\t\t\t? a\n\t\t\t\t: b)", true),
				Arguments.of("comment inside a sole ternary", "target", "target(cond /*c*/\n\t\t\t\t? a\n\t\t\t\t: b)", true),
				Arguments.of("comment before a sole anonymous class", "target", "target(/*c*/ new Runnable() {\n\t\t\t\tpublic void run() {}\n\t\t\t\t})", true),
				Arguments.of("postDelayed comment before the comma", "postDelayed", "postDelayed(x -> {\n\t\t\t\tuse(x);\n\t\t\t\t} /*c*/, 1000)", true),
				Arguments.of("computeIfAbsent comment before the comma", "computeIfAbsent", "computeIfAbsent(k /*c*/, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("put comment before the comma", "put", "put(\"k\" /*c*/, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("comment does not make a plain multi-arg special", "target", "target(a /*c*/, b,\n\t\t\t\tc)", false),
				Arguments.of("comment does not make a plain nested call special", "target", "target(/*c*/ other(\n\t\t\t\targ))", false),
				Arguments.of("comment does not rescue a special call among plain args", "target", "target(List.of(1, 2), other /*c*/,\n\t\t\t\tz)", false)
		);
	}

	private static Stream<Arguments> configurations() {
		return Stream.of(
				Arguments.of("single braced lambda", "target", "target(x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("single braceless lambda", "target", "target(x ->\n\t\t\t\tuse(x))", true),
				Arguments.of("single anonymous class", "target", "target(new Runnable() {\n\t\t\t\tpublic void run() {}\n\t\t\t\t})", true),
				Arguments.of("single ternary", "target", "target(cond\n\t\t\t\t? a\n\t\t\t\t: b)", true),
				Arguments.of("this and ternary", "target", "target(this, cond\n\t\t\t\t? a\n\t\t\t\t: b)", true),
				Arguments.of("this and inline block", "target", "target(this, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("single static special inline call", "target", "target(List.of(\n\t\t\t\t1, 2, 3\n\t\t\t\t))", true),
				Arguments.of("computeIfAbsent braced lambda", "computeIfAbsent", "computeIfAbsent(k, x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("postDelayed braced lambda", "postDelayed", "postDelayed(x -> {\n\t\t\t\tuse(x);\n\t\t\t\t}, 1000)", true),
				Arguments.of("postDelayed anonymous class", "postDelayed", "postDelayed(new Runnable() {\n\t\t\t\tpublic void run() {}\n\t\t\t\t}, 1000)", true),
				Arguments.of("put inline-block value", "put", "put(\"k\", x -> {\n\t\t\t\tuse(x);\n\t\t\t\t})", true),
				Arguments.of("single plain nested call", "target", "target(other(\n\t\t\t\targ))", false),
				Arguments.of("single plain literal", "target", "target(\n\t\t\t\t1)", false),
				Arguments.of("plain multi-arg", "target", "target(a, b,\n\t\t\t\tc)", false),
				Arguments.of("special call among plain args", "target", "target(List.of(1, 2), other,\n\t\t\t\tz)", false),
				Arguments.of("postDelayed method ref not config", "postDelayed", "postDelayed(this::doThing,\n\t\t\t\t1000)", false),
				Arguments.of("plain new among plain args", "target", "target(new Foo(1), other,\n\t\t\t\tz)", false)
		);
	}

	@Nonnull
	private static DetailAST firstArg(@Nonnull DetailAST root) {
		final var elist = callNamed(root, "target").findFirstToken(TokenTypes.ELIST);
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				return child;
		}
		throw new AssertionError("no argument in target(...)");
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source) throws Exception {
		return parse(source, Options.WITHOUT_COMMENTS);
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String source, @Nonnull Options options) throws Exception {
		final var tmp = File.createTempFile("classifier", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parseFileText(new FileText(tmp, StandardCharsets.UTF_8.name()), options);
		}
		finally {
			tmp.delete();
		}
	}

	/** The parse the shipped code does not do today; see {@code AstText.isCommentToken}. */
	@Nonnull
	private static DetailAST parseWithComments(@Nonnull String source) throws Exception {
		return parse(source, Options.WITH_COMMENTS);
	}

	@MethodSource("bracedBlocks")
	@ParameterizedTest(name = "{0}")
	public void containsMultilineBracedBlock(@Nonnull String label, @Nonnull String arg, boolean expected) throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\ttarget(" + arg + ", dummy);\n\t}\n}";
		final var root = parse(source);
		assertEquals(expected, ArgLayoutClassifier.containsMultilineBracedBlock(firstArg(root)));
	}

	@MethodSource("configurations")
	@ParameterizedTest(name = "{0}")
	public void isSpecialLayoutConfiguration(@Nonnull String label, @Nonnull String callName, @Nonnull String call, boolean expected) throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\t" + call + ";\n\t}\n}";
		final var root = parse(source);
		assertEquals(expected, ArgLayoutClassifier.isSpecialLayoutConfiguration(callNamed(root, callName), node -> false));
	}

	@MethodSource("commentPositions")
	@ParameterizedTest(name = "{0}")
	public void isSpecialLayoutConfigurationWithComments(@Nonnull String label, @Nonnull String callName, @Nonnull String call, boolean expected) throws Exception {
		final var source = "class C {\n\tvoid m() {\n\t\t" + call + ";\n\t}\n}";
		assertEquals(
				expected,
				ArgLayoutClassifier.isSpecialLayoutConfiguration(callNamed(parse(source), callName), node -> false),
				"WITHOUT_COMMENTS"
		);
		assertEquals(
				expected,
				ArgLayoutClassifier.isSpecialLayoutConfiguration(callNamed(parseWithComments(source), callName), node -> false),
				"WITH_COMMENTS"
		);
	}
}