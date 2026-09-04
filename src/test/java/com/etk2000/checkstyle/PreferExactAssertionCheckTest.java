package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.util.Objects.requireNonNull;

import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionRule;
import com.etk2000.checkstyle.PreferExactAssertionCheck.AssertionTarget;
import com.etk2000.checkstyle.PreferExactAssertionCheck.JunitScope;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.JavaParser.Options;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PreferExactAssertionCheckTest {
	private record Expected(String file, int line, SeverityLevel severity, String message) {}

	private static final String DIR = "preferexactassertion/";
	private static final String DOUBLE_NEGATION =
			"class T { void m(boolean flag) { Assertions.assertTrue(!!flag); } }";
	private static final String MESSAGE_THEN_INSTANCEOF =
			"class T { void m(Object o) { Assertions.assertTrue(\"msg\", o instanceof String); } }";
	private static final String NEGATION_AND_COMPARISON =
			"class T { void m(boolean flag, int a, int b) { Assertions.assertTrue(!flag, a == b); } }";
	private static final String NEGATION_AND_COMPARISON_COMMENTED =
			"class T { void m(boolean flag, int a, int b) { Assertions.assertTrue(!flag /* c */, a == b); } }";
	private static final String QUALIFIED_JUNIT_RECEIVER =
			"class T { void m(Object o) { org.junit.jupiter.api.Assertions.assertTrue(o instanceof String); } }";
	private static final String QUALIFIED_LOOKALIKE_RECEIVER =
			"class T { void m(Object o) { com.example.Assertions.assertTrue(o instanceof String); } }";
	// `this.` on a call cannot live in a lint-gated fixture, since NoUnnecessaryThis reports it there
	private static final String QUALIFIED_THIS_RECEIVER =
			"class T { void assertTrue(boolean b) {} void m(boolean flag) { this.assertTrue(!flag); } }";

	private static void assertViolations(List<AuditEvent> got, List<Expected> expected) {
		assertEquals(expected.size(), got.size());
		for (var i = 0; i < expected.size(); ++i) {
			assertEquals(expected.get(i).line, got.get(i).getLine(), "line " + i);
			assertEquals(expected.get(i).severity, got.get(i).getSeverityLevel(), "severity " + i);
			assertEquals(expected.get(i).message, got.get(i).getMessage(), "message " + i);
			// a directive-bearing fixture is checked from a translated temp copy, so only the name survives
			assertTrue(
					got.get(i).getFileName().endsWith(expected.get(i).file.substring(DIR.length())),
					"file " + i + ": expected " + expected.get(i).file + ", got " + got.get(i).getFileName()
			);
		}
	}

	/**
	 * The targets {@link PreferExactAssertionCheck#classify} gives the first call in {@code root}, which
	 * {@code source} was parsed from: the classifier measures the rewrite it would produce against that
	 * same text.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<AssertionTarget> classifyAssertCall(@Nonnull DetailAST root, @Nonnull String source) {
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL), "no METHOD_CALL in the source");
		return PreferExactAssertionCheck.classify(
				call,
				List.of(source.split("\n", -1)),
				PreferExactAssertionCheck.junitScopeOf(root)
		);
	}

	@CheckReturnValue
	private static int commentChildCount(@Nonnull DetailAST node) {
		var count = 0;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (AstText.isCommentToken(child.getType()))
				++count;
		}
		return count;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findFirst(@Nonnull DetailAST node, int tokenType) {
		if (node.getType() == tokenType)
			return node;
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findFirst(child, tokenType);
			if (found != null)
				return found;
		}
		return null;
	}

	static Stream<Arguments> isJunitAssertClassProvider() {
		return Stream.of(
				Arguments.of("Assert", true),
				Arguments.of("Assertions", true),
				Arguments.of("Assertion", false),
				Arguments.of("assert", false),
				Arguments.of("Helper", false),
				Arguments.of(null, false)
		);
	}

	static Stream<Arguments> isJunitAssertFqcnProvider() {
		return Stream.of(
				Arguments.of("org.junit.Assert", true),
				Arguments.of("org.junit.jupiter.api.Assertions", true),
				Arguments.of("Assert", false),
				Arguments.of("Assertions", false),
				Arguments.of("com.example.Assertions", false),
				Arguments.of("com.foo.Assert", false),
				Arguments.of("my.org.junit.Assert", false),
				Arguments.of("org.junit.Assert.Inner", false),
				Arguments.of("org.junit.assert", false),
				Arguments.of("", false),
				Arguments.of(null, false)
		);
	}

	static Stream<Arguments> junitScopeProvider() {
		return Stream.of(
				Arguments.of("import static org.junit.jupiter.api.Assertions.*;", false, true),
				Arguments.of("import static org.junit.jupiter.api.Assertions.assertTrue;", false, true),
				Arguments.of("import static org.junit.Assert.assertTrue;", true, false),
				Arguments.of("import static org.junit.Assert.*;", true, false),
				Arguments.of("import static org . junit . Assert . assertTrue ;", true, false),
				Arguments.of("import static java.util.Objects.requireNonNull;", false, false),
				Arguments.of(
						"import static org.junit.Assert.assertTrue;\nimport static org.junit.jupiter.api.Assertions.*;",
						true,
						true
				),
				Arguments.of("import java.util.List;", false, false),
				Arguments.of("", false, false),
				Arguments.of("import static Helper.assertTrue;", false, false),
				Arguments.of("import static com.example.Assertions.assertTrue;", false, false),
				Arguments.of("import static com.example.Assertions.*;", false, false),
				Arguments.of("import static com.foo.Assert.assertTrue;", false, false),
				Arguments.of("import static com.foo.Assert.*;", false, false),
				Arguments.of("import static Assertions.assertTrue;", false, false),
				Arguments.of("import static Assert.*;", false, false),
				Arguments.of("import static my.org.junit.Assert.assertTrue;", false, false),
				Arguments.of("import static org.junit.Assert.Inner.assertTrue;", false, false),
				Arguments.of("import static org.junit.jupiter.Assertions.assertTrue;", false, false),
				Arguments.of("import static org.junit.jupiter.api.AssertionsX.assertTrue;", false, false),
				Arguments.of("import static org.junit.Assertions.assertTrue;", false, false),
				Arguments.of("import static org.junit.jupiter.api.Assert.assertTrue;", false, false)
		);
	}

	@CheckReturnValue
	@Nonnull
	private static List<Expected> markersOf(@Nonnull String... files) throws Exception {
		final var expected = new ArrayList<Expected>();
		for (var file : files) {
			final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + file);
			requireNonNull(url, "Test input file not found: " + file);
			for (var marker : BaseCheckTest.parseViolationMarkers(Files.readAllLines(Path.of(url.toURI()))))
				expected.add(new Expected(file, marker.line(), marker.severity(), marker.message()));
		}
		return expected;
	}

	@CheckReturnValue
	@Nonnull
	private static DetailAST parseSource(@Nonnull String source, @Nonnull Options options) throws Exception {
		final var sourceFile = File.createTempFile("preferexactassertion", ".java");
		try {
			Files.writeString(sourceFile.toPath(), source);
			return JavaParser.parseFileText(new FileText(sourceFile, StandardCharsets.UTF_8.name()), options);
		}
		finally {
			sourceFile.delete();
		}
	}

	@CheckReturnValue
	@Nonnull
	static Stream<Arguments> staticImportClassFqnProvider() {
		return Stream.of(
				Arguments.of("org.junit.Assert.assertTrue", "org.junit.Assert"),
				Arguments.of("org.junit.Assert.*", "org.junit.Assert"),
				Arguments.of("a.b.C.D.m", "a.b.C.D"),
				Arguments.of("Assert.*", "Assert"),
				Arguments.of("Assert", null)
		);
	}

	static Stream<Arguments> staticImportFqnsProvider() {
		return Stream.of(
				Arguments.of("", List.of()),
				Arguments.of(
						"package a.b;\nimport static org.junit.Assert.assertTrue;",
						List.of("org.junit.Assert.assertTrue")
				),
				Arguments.of("import static org.junit.Assert.assertTrue;", List.of("org.junit.Assert.assertTrue")),
				Arguments.of("import static org.junit.Assert.*;", List.of("org.junit.Assert.*")),
				Arguments.of("import static a.b.C.D.m;", List.of("a.b.C.D.m")),
				Arguments.of("import org.junit.Assert;", List.of()),
				Arguments.of("import java.util.*;", List.of()),
				Arguments.of("import static\n\t\torg.junit.Assert.assertTrue;", List.of("org.junit.Assert.assertTrue")),
				Arguments.of("import static org.junit.\n\t\tAssert.assertTrue;", List.of("org.junit.Assert.assertTrue")),
				Arguments.of("import static org.junit.Assert.assertTrue\n\t\t;", List.of("org.junit.Assert.assertTrue")),
				Arguments.of(
						"import java.util.List;\nimport static org.junit.Assert.assertTrue;\nimport static a.B.c;",
						List.of("org.junit.Assert.assertTrue", "a.B.c")
				)
		);
	}

	static Stream<Arguments> staticImportMemberProvider() {
		return Stream.of(
				Arguments.of("org.junit.Assert.assertTrue", "assertTrue"),
				Arguments.of("a.b.C.D.m", "m"),
				Arguments.of("org.junit.Assert.*", null),
				Arguments.of("Assert.*", null),
				Arguments.of("Assert", null)
		);
	}

	private static DetailAstImpl syntheticAst(int tokenType, @Nonnull String text) {
		final var ast = new DetailAstImpl();
		ast.setType(tokenType);
		ast.setText(text);
		return ast;
	}

	/** {@code segments} as the left-nested DOT chain checkstyle builds for a qualified name. */
	@Nonnull
	private static DetailAstImpl syntheticDottedPath(@Nonnull String... segments) {
		var path = syntheticAst(TokenTypes.IDENT, segments[0]);
		for (var i = 1; i < segments.length; ++i) {
			final var dot = syntheticAst(TokenTypes.DOT, ".");
			dot.addChild(path);
			dot.addChild(syntheticAst(TokenTypes.IDENT, segments[i]));
			path = dot;
		}
		return path;
	}

	@Test
	public void testClassifyArgIndexSkipsMessageArgument() throws Exception {
		final var root = parseSource(MESSAGE_THEN_INSTANCEOF, Options.WITHOUT_COMMENTS);
		final var targets = classifyAssertCall(root, MESSAGE_THEN_INSTANCEOF);
		assertEquals(1, targets.size());
		final var target = targets.getFirst();
		assertEquals(AssertionRule.INSTANCEOF, target.rule());
		assertEquals(1, target.argIndex());
		assertEquals("assertInstanceOf", target.replacementMethod());
	}

	@Test
	public void testClassifyDoubleNegationKeepsOneStripSemantics() throws Exception {
		final var root = parseSource(DOUBLE_NEGATION, Options.WITHOUT_COMMENTS);
		final var targets = classifyAssertCall(root, DOUBLE_NEGATION);
		assertEquals(1, targets.size());
		final var target = targets.getFirst();
		assertEquals(AssertionRule.NEGATION, target.rule());
		assertEquals(2, target.notCount());
		assertEquals("assertFalse", target.replacementMethod());
	}

	@Test
	public void testClassifyReportsEveryOffendingArgument() throws Exception {
		final var root = parseSource(NEGATION_AND_COMPARISON, Options.WITHOUT_COMMENTS);
		final var targets = classifyAssertCall(root, NEGATION_AND_COMPARISON);
		assertEquals(2, targets.size());
		assertEquals(AssertionRule.NEGATION, targets.getFirst().rule());
		assertEquals(0, targets.getFirst().argIndex());
		assertEquals("assertFalse", targets.getFirst().replacementMethod());
		assertEquals(AssertionRule.COMPARISON, targets.get(1).rule());
		assertEquals(1, targets.get(1).argIndex());
		assertEquals("assertEquals", targets.get(1).replacementMethod());
	}

	@Test
	public void testClassifyWithCommentNodesInArgumentList() throws Exception {
		final var root = parseSource(NEGATION_AND_COMPARISON_COMMENTED, Options.WITH_COMMENTS);
		final var elist = requireNonNull(findFirst(root, TokenTypes.ELIST), "no ELIST in the source");
		// pins the premise: with no comment among the ELIST's children the walk has nothing to step
		// over, and the argument indices below would hold under a raw getNextSibling walk too
		assertEquals(1, commentChildCount(elist));
		final var targets = classifyAssertCall(root, NEGATION_AND_COMPARISON_COMMENTED);
		assertEquals(2, targets.size());
		assertEquals(AssertionRule.NEGATION, targets.getFirst().rule());
		assertEquals(0, targets.getFirst().argIndex());
		assertEquals(AssertionRule.COMPARISON, targets.get(1).rule());
		assertEquals(1, targets.get(1).argIndex());
	}

	@MethodSource("isJunitAssertClassProvider")
	@ParameterizedTest
	void testIsJunitAssertClass(String simpleName, boolean expected) {
		assertEquals(expected, PreferExactAssertionCheck.isJunitAssertClass(simpleName));
	}

	@MethodSource("isJunitAssertFqcnProvider")
	@ParameterizedTest
	void testIsJunitAssertFqcn(String fqcn, boolean expected) {
		assertEquals(expected, PreferExactAssertionCheck.isJunitAssertFqcn(fqcn));
	}

	@MethodSource("junitScopeProvider")
	@ParameterizedTest
	void testJunitScopeOf(String imports, boolean junit4, boolean junit5) throws Exception {
		final var root = parseSource(imports + "\nclass T {}\n", Options.WITHOUT_COMMENTS);
		assertEquals(new JunitScope(junit4, junit5), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testJunitScopeOfChildlessStaticImport() {
		// checkstyle never emits a childless STATIC_IMPORT, but the path guard must hold if one is built
		final var root = syntheticAst(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT");
		root.addChild(syntheticAst(TokenTypes.STATIC_IMPORT, "import"));
		assertEquals(new JunitScope(false, false), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testJunitScopeOfDotlessStaticImportPath() {
		// `import static Helper.assertTrue;` still parses to a DOT, so only a hand-built single-IDENT
		// path reaches the no-dot guard, which must not read the path itself as the imported class
		final var root = syntheticAst(TokenTypes.STATIC_IMPORT, "import");
		root.addChild(syntheticAst(TokenTypes.LITERAL_STATIC, "static"));
		root.addChild(syntheticAst(TokenTypes.IDENT, "Assert"));
		assertEquals(new JunitScope(false, false), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testJunitScopeOfNonCompilationUnitRoot() {
		final var root = syntheticAst(TokenTypes.STATIC_IMPORT, "import");
		root.addChild(syntheticAst(TokenTypes.LITERAL_STATIC, "static"));
		root.addChild(syntheticDottedPath("org", "junit", "jupiter", "api", "Assertions", "assertTrue"));
		assertEquals(new JunitScope(false, true), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testJunitScopeOfNonCompilationUnitRootDefaultPackageClass() {
		final var root = syntheticAst(TokenTypes.STATIC_IMPORT, "import");
		root.addChild(syntheticAst(TokenTypes.LITERAL_STATIC, "static"));
		root.addChild(syntheticDottedPath("Assertions", "assertTrue"));
		assertEquals(new JunitScope(false, false), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testJunitScopeOfPathlessStaticImport() {
		final var staticImport = syntheticAst(TokenTypes.STATIC_IMPORT, "import");
		staticImport.addChild(syntheticAst(TokenTypes.LITERAL_STATIC, "static"));
		final var root = syntheticAst(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT");
		root.addChild(staticImport);
		assertEquals(new JunitScope(false, false), PreferExactAssertionCheck.junitScopeOf(root));
	}

	@Test
	public void testQualifiedLookalikeReceiverNotReported() throws Exception {
		final var root = parseSource(QUALIFIED_LOOKALIKE_RECEIVER, Options.WITHOUT_COMMENTS);
		assertEquals(List.of(), classifyAssertCall(root, QUALIFIED_LOOKALIKE_RECEIVER));
	}

	@Test
	public void testQualifiedReceiverWithJunitFullNameReported() throws Exception {
		final var root = parseSource(QUALIFIED_JUNIT_RECEIVER, Options.WITHOUT_COMMENTS);
		final var targets = classifyAssertCall(root, QUALIFIED_JUNIT_RECEIVER);
		assertEquals(1, targets.size());
		assertEquals(AssertionRule.INSTANCEOF, targets.getFirst().rule());
		assertEquals("assertInstanceOf", targets.getFirst().replacementMethod());
		assertEquals("Assertions", targets.getFirst().qualifierSimpleName());
	}

	@Test
	public void testQualifiedThisReceiverNotReported() throws Exception {
		final var root = parseSource(QUALIFIED_THIS_RECEIVER, Options.WITHOUT_COMMENTS);
		assertEquals(List.of(), classifyAssertCall(root, QUALIFIED_THIS_RECEIVER));
	}

	@Test
	public void testStateResetBetweenFiles() throws Exception {
		final var first = DIR + "cases.junit4wildcard.in.java";
		final var second = DIR + "cases.junit5wildcard.in.java";
		final var expected = markersOf(first, second);
		assertEquals(4, expected.size(), "fixtures lost their violation markers, the assertion would be vacuous");
		assertViolations(
				BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, first, second),
				expected
		);
	}

	@Test
	public void testStateResetJunit5ScopeDoesNotBleed() throws Exception {
		// the other direction: the no-import file's unqualified assert calls resolve to an
		// inherited helper, so a leftover JUnit 5 scope would make all ten of them fire
		final var first = DIR + "cases.junit5wildcard.in.java";
		final var second = DIR + "cases.nostaticimport.clean.java";
		final var expected = markersOf(first, second);
		assertEquals(2, expected.size(), "fixtures lost their violation markers, the assertion would be vacuous");
		assertViolations(
				BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, first, second),
				expected
		);
	}

	@MethodSource("staticImportClassFqnProvider")
	@ParameterizedTest
	void testStaticImportClassFqn(String fqn, String expected) {
		assertEquals(expected, PreferExactAssertionCheck.staticImportClassFqn(fqn));
	}

	@MethodSource("staticImportFqnsProvider")
	@ParameterizedTest
	void testStaticImportFqns(String imports, List<String> expected) throws Exception {
		final var root = parseSource(imports + "\nclass T {}\n", Options.WITHOUT_COMMENTS);
		assertEquals(expected, PreferExactAssertionCheck.staticImportFqns(root));
	}

	@Test
	public void testStaticImportFqnsFromNonRootNode() {
		final var path = syntheticAst(TokenTypes.DOT, ".");
		path.addChild(syntheticAst(TokenTypes.IDENT, "Assertions"));
		path.addChild(syntheticAst(TokenTypes.IDENT, "assertTrue"));
		final var staticImport = syntheticAst(TokenTypes.STATIC_IMPORT, "import");
		staticImport.addChild(syntheticAst(TokenTypes.LITERAL_STATIC, "static"));
		staticImport.addChild(path);
		final var root = syntheticAst(TokenTypes.COMPILATION_UNIT, "COMPILATION_UNIT");
		root.addChild(staticImport);
		assertEquals(List.of("Assertions.assertTrue"), PreferExactAssertionCheck.staticImportFqns(staticImport));
	}

	@Test
	public void testStaticImportFqnsWithCommentNodesInTheTree() throws Exception {
		// nothing passes a comment-bearing tree today: this check does not require comment nodes, so the
		// TreeWalker runs it over the comment-free tree, and the fixer parses WITHOUT_COMMENTS. A comment
		// written INSIDE the imported name is left out on purpose, because FullIdent swallows it; see
		// docs/todo/TODO-prefer-exact-assertion-ast-migration.md
		final var root = parseSource(
				"/* legacy */ import static org.junit.jupiter.api.Assertions.assertTrue; // note\nclass T {}\n",
				Options.WITH_COMMENTS
		);
		assertEquals(
				List.of("org.junit.jupiter.api.Assertions.assertTrue"),
				PreferExactAssertionCheck.staticImportFqns(root)
		);
	}

	@MethodSource("staticImportMemberProvider")
	@ParameterizedTest
	void testStaticImportMember(String fqn, String expected) {
		assertEquals(expected, PreferExactAssertionCheck.staticImportMember(fqn));
	}
}