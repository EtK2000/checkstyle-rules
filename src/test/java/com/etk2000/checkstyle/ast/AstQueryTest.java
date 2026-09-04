package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.assertSameWithAndWithoutComments;
import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.findMethod;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseExprFirstChild;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSource;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSourceWithComments;
import static com.etk2000.checkstyle.ast.AstTestSupport.root;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class AstQueryTest {
	static Stream<Arguments> astStructuralEqualsCommentProvider() {
		return Stream.of(
				Arguments.of("i = i + 1; i = i + 1;", true),
				Arguments.of("i = i + 1; i = /*c*/ i + 1;", true),
				Arguments.of("i = /*c*/ i + 1; i = /*d*/ i + 1;", true),
				Arguments.of("i = i + 1; i = i /*c*/ + 1;", true),
				Arguments.of("i = i + 1; i = i + /*c*/ 1;", true),
				Arguments.of("i = i + 1; i = i + 2;", false),
				Arguments.of("i = i + 1; i = /*c*/ i + 2;", false),
				Arguments.of("i = i + 1; i = i + 1 + 1;", false)
		);
	}

	static Stream<Arguments> collectInstanceFieldTypesProvider() {
		return Stream.of(
				Arguments.of("class T { void f() {} }", List.of()),
				Arguments.of("class T { static int x; static String y; }", List.of()),
				Arguments.of("class T { int x, y; }", List.of("int", "int")),
				Arguments.of("class T { String b; int a; }", List.of("String", "int")),
				Arguments.of("class T { static int s; int a; String b; }", List.of("String", "int")),
				Arguments.of("class T { @Deprecated int a; @Deprecated String b; }", List.of("String", "int")),
				Arguments.of("class T { @Deprecated int a; String b; }", List.of("String", "int")),
				Arguments.of("class T { @Deprecated int[] a; @Deprecated String b; }", List.of("String", "int[]"))
		);
	}

	static Stream<Arguments> collectParameterNamesProvider() {
		return Stream.of(
				Arguments.of("class T { T() {} }", TokenTypes.CTOR_DEF, Set.of()),
				Arguments.of("class T { T(int x) {} }", TokenTypes.CTOR_DEF, Set.of("x")),
				Arguments.of("class T { T(String a, int b) {} }", TokenTypes.CTOR_DEF, Set.of("a", "b")),
				Arguments.of("class T { void f(String a, int b) {} }", TokenTypes.METHOD_DEF, Set.of("a", "b")),
				Arguments.of("class T { T(@Deprecated String a, @Deprecated int b) {} }", TokenTypes.CTOR_DEF, Set.of("a", "b")),
				Arguments.of("class T { T(String... args) {} }", TokenTypes.CTOR_DEF, Set.of("args")),
				Arguments.of("class T { void f(String... args) {} }", TokenTypes.METHOD_DEF, Set.of("args")),
				Arguments.of("class T { void f(int a, String... rest) {} }", TokenTypes.METHOD_DEF, Set.of("a", "rest"))
		);
	}

	static Stream<Arguments> collectParameterTypesProvider() {
		return Stream.of(
				Arguments.of("class T { T() {} }", TokenTypes.CTOR_DEF, List.of()),
				Arguments.of("class T { T(int x) {} }", TokenTypes.CTOR_DEF, List.of("int")),
				Arguments.of("class T { T(String a, int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { void f(String a, int b) {} }", TokenTypes.METHOD_DEF, List.of("String", "int")),
				Arguments.of("class T { T(@Deprecated String a, @Deprecated int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { T(@Deprecated String a, int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { void f(@Deprecated String a, @Deprecated int[] b) {} }", TokenTypes.METHOD_DEF, List.of("String", "int[]")),
				Arguments.of("class T { void f(String[] args) {} }", TokenTypes.METHOD_DEF, List.of("String[]")),
				Arguments.of("class T { void f(String... args) {} }", TokenTypes.METHOD_DEF, List.of("String")),
				Arguments.of("class T { void f(int a, String... rest) {} }", TokenTypes.METHOD_DEF, List.of("String", "int")),
				Arguments.of("class T { T(String... args) {} }", TokenTypes.CTOR_DEF, List.of("String")),
				Arguments.of("class T { T(int a, String... rest) {} }", TokenTypes.CTOR_DEF, List.of("String", "int"))
		);
	}

	static Stream<Arguments> countArgumentsProvider() {
		return Stream.of(
				Arguments.of("class T { void f() { g(); } }", 0),
				Arguments.of("class T { void f(int a) { g(a); } }", 1),
				Arguments.of("class T { void f(int a, int b) { g(a, b); } }", 2),
				Arguments.of("class T { void f(int a, int b, int c) { g(a, b, c); } }", 3),
				Arguments.of("class T { void f(int a, int b) { g(a + b); } }", 1),
				Arguments.of("class T { void f(int x, int y) { g(h(x, y)); } }", 1),
				Arguments.of("class T { void f() { g(() -> 1); } }", 1),
				Arguments.of("class T { void f() { g(() -> 1, () -> 2); } }", 2),
				Arguments.of("class T { void f(int a) { g(() -> 1, a); } }", 2),
				Arguments.of("class T { void f(int a) { g(a, () -> 1); } }", 2),
				Arguments.of("class T { void f(int a, int b) { g(a, () -> 1, b); } }", 3),
				Arguments.of("class T { void f() { g(() -> { return 1; }); } }", 1),
				Arguments.of("class T { void f() { g((String s) -> 1); } }", 1),
				Arguments.of("class T { void f() { g(T::new); } }", 1),
				Arguments.of("class T { void f(int k) { g(switch (k) { default -> 1; }); } }", 1),
				Arguments.of("class T { void f() { g(this::h); } }", 1)
		);
	}

	static Stream<Arguments> endsWithDanglingIfProvider() {
		return Stream.of(
				Arguments.of("{ if (c) x(); }", true),
				Arguments.of("{ while (c) if (c) x(); }", true),
				Arguments.of("{ for (int i = 0; i < 3; ++i) if (c) x(); }", true),
				Arguments.of("{ for (;;) if (c) x(); }", true),
				Arguments.of("{ for (var s : list) if (c) x(); }", true),
				Arguments.of("{ one: two: if (c) x(); }", true),
				Arguments.of("{ if (c) x(); else if (c) y(); }", true),
				Arguments.of("{ if (c) x(); else y(); }", false),
				Arguments.of("{ while (c) x(); }", false),
				Arguments.of("{ while (c); }", false),
				Arguments.of("{ for (int i = 0; i < 3; ++i) x(); }", false),
				Arguments.of("{ do x(); while (c); }", false),
				Arguments.of("{ x(); }", false),
				Arguments.of("{ while (c) /*n*/ if (c) x(); }", true),
				Arguments.of("{ for (int i = 0; i < 3; ++i) /*n*/ if (c) x(); }", true),
				Arguments.of("{ one: /*n*/ if (c) x(); }", true),
				Arguments.of("{ if (c) x(); else /*n*/ if (c) y(); }", true)
		);
	}

	static Stream<Arguments> hasSuppressWarningsProvider() {
		return Stream.of(
				Arguments.of("@SuppressWarnings(\"Foo\") class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings(\"Bar\") class T {}", "Foo", false),
				Arguments.of("@SuppressWarnings({\"Foo\"}) class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings({\"Bar\", \"Foo\"}) class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings({\"Foo\", \"Bar\"}) class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings({\"Bar\"}) class T {}", "Foo", false),
				Arguments.of("@SuppressWarnings(value = \"Foo\") class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings(value = \"Bar\") class T {}", "Foo", false),
				Arguments.of("@SuppressWarnings(value = {\"Foo\", \"Bar\"}) class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings(value = {\"Bar\", \"Foo\"}) class T {}", "Foo", true),
				Arguments.of("@SuppressWarnings(value = {\"Bar\"}) class T {}", "Foo", false),
				Arguments.of("@SuppressWarnings(value = {}) class T {}", "Foo", false),
				Arguments.of("@SuppressWarnings({}) class T {}", "Foo", false),
				Arguments.of("@java.lang.SuppressWarnings(\"Foo\") class T {}", "Foo", true),
				Arguments.of("class T {}", "Foo", false),
				Arguments.of("@Deprecated class T {}", "Foo", false)
		);
	}

	static Stream<Arguments> isPureExpressionCommentProvider() {
		return Stream.of(
				Arguments.of("class T { Object f(Object o) { return o.b; } }", true),
				Arguments.of("class T { Object f(Object o) { return o./*c*/b; } }", true),
				Arguments.of("class T { Object f(Object o) { return /*c*/ o.b; } }", true),
				Arguments.of("class T { Object f(Object o) { return o.\n// c\nb; } }", true),
				Arguments.of("class T { int f(int[] arr, int i) { return arr[/*c*/ i]; } }", true),
				Arguments.of("class T { int f(int[] arr, int i) { return arr[i /*c*/]; } }", true),
				Arguments.of("class T { int f(int i) { return -/*c*/i; } }", true),
				Arguments.of("class T { Object f(Object o) { return o.b(); } }", false),
				Arguments.of("class T { Object f(Object o) { return /*c*/ o.b(); } }", false),
				Arguments.of("class T { int f(int i) { return i++; } }", false),
				Arguments.of("class T { int f(int i) { return /*c*/ i++; } }", false)
		);
	}

	private static boolean isZeroLiteral(@Nonnull String literal) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + literal + "; } }");
		for (var type : new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG}) {
			final var num = findFirst(ast, type);
			if (num != null)
				return AstQuery.isZeroLiteral(num);
		}
		throw new AssertionError("No numeric literal found in: " + literal);
	}

	static Stream<Arguments> rebindsAFollowingElseProvider() {
		return Stream.of(
				Arguments.of("{ x(); }", true),
				Arguments.of("do { x(); } while (c);", true),
				Arguments.of("for (int i = 0; i < 3; ++i) { x(); }", true),
				Arguments.of("while (c) { x(); }", true),
				Arguments.of("lbl: { x(); }", true),
				Arguments.of("switch (1) { default -> { x(); } }", false)
		);
	}

	static Stream<Arguments> unwrapSingleStatementBlockCommentProvider() {
		return Stream.of(
				Arguments.of("{ x = 1; }", true),
				Arguments.of("{ x = 1; /*after*/ }", true),
				Arguments.of("{ x = 1; // after\n}", true),
				Arguments.of("{ /*before*/ x = 1; }", true),
				Arguments.of("{ /*a*/ x = 1; /*b*/ }", true),
				Arguments.of("{ /*only*/ }", false),
				Arguments.of("{ // only\n}", false),
				Arguments.of("{ }", false),
				Arguments.of("{ x = 1; x = 2; }", false),
				Arguments.of("{ x = 1; /*c*/ x = 2; }", false)
		);
	}

	@Test
	public void testAstStructuralEqualsChildCountMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = f(a); }");
		final var b = parseExprFirstChild("class T { Object x = f(a, b); }");
		assertFalse(AstQuery.astStructuralEquals(a, b));
	}

	@MethodSource("astStructuralEqualsCommentProvider")
	@ParameterizedTest
	void testAstStructuralEqualsComments(String statements, boolean expected) throws Exception {
		assertSameWithAndWithoutComments(
				"class T { void m(int i) { " + statements + " } }",
				expected,
				root -> {
					final var slist = requireNonNull(findFirst(root, TokenTypes.SLIST));
					final var firstExpr = requireNonNull(findFirst(slist, TokenTypes.EXPR));
					var secondExpr = firstExpr.getNextSibling();
					while (secondExpr != null && secondExpr.getType() != TokenTypes.EXPR)
						secondExpr = secondExpr.getNextSibling();
					return AstQuery.astStructuralEquals(firstExpr, requireNonNull(secondExpr));
				}
		);
	}

	@Test
	public void testAstStructuralEqualsDeepTree() throws Exception {
		final var source = "class T { Object x = a" + ".b".repeat(500) + "; }";
		assertTrue(AstQuery.astStructuralEquals(parseExprFirstChild(source), parseExprFirstChild(source)));
	}

	@Test
	public void testAstStructuralEqualsDottedChain() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a.b.c.d.e.f.g.h.i.j; }");
		final var b = parseExprFirstChild("class T { Object x = a.b.c.d.e.f.g.h.i.j; }");
		assertTrue(AstQuery.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsIdentical() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = ++i; }");
		final var b = parseExprFirstChild("class T { Object x = ++i; }");
		assertTrue(AstQuery.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsSiblingOrder() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a + b; }");
		final var b = parseExprFirstChild("class T { Object x = b + a; }");
		assertFalse(AstQuery.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsTextMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a; }");
		final var b = parseExprFirstChild("class T { Object x = b; }");
		assertFalse(AstQuery.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsTypeMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = ++i; }");
		final var b = parseExprFirstChild("class T { Object x = --i; }");
		assertFalse(AstQuery.astStructuralEquals(a, b));
	}

	@Test
	public void testCollectAnnotationsMultiple() throws Exception {
		final var ast = parseSource("class T { void f(@Deprecated @Override String p) {} }");
		final var paramDef = findFirst(ast, TokenTypes.PARAMETER_DEF);
		final var modifiers = paramDef.findFirstToken(TokenTypes.MODIFIERS);
		assertEquals(2, AstQuery.collectAnnotations(modifiers).size());
	}

	@Test
	public void testCollectAnnotationsNone() {
		final var method = findMethod(root(), "emptyBlock");
		final var params = method.findFirstToken(TokenTypes.PARAMETERS);
		assertTrue(AstQuery.collectAnnotations(params).isEmpty());
	}

	@MethodSource("collectInstanceFieldTypesProvider")
	@ParameterizedTest
	void testCollectInstanceFieldTypes(String source, List<String> expected) throws Exception {
		final var ast = parseSource(source);
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals(expected, AstQuery.collectInstanceFieldTypes(objBlock));
	}

	@Test
	public void testCollectMatchingDeepTree() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a" + ".b".repeat(500) + "; }");
		assertEquals(501, AstQuery.collectMatching(node, n -> n.getType() == TokenTypes.IDENT).size());
	}

	@Test
	public void testCollectMatchingNoMatch() throws Exception {
		final var ast = parseSource("class T { void f() { a(); } }");
		assertTrue(AstQuery.collectMatching(ast, n -> n.getType() == TokenTypes.LITERAL_TRY).isEmpty());
	}

	@Test
	public void testCollectMatchingPreOrder() throws Exception {
		final var ast = parseSource("class T { void f() { a(); b(c()); } }");
		final var calls = AstQuery.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL);
		assertEquals(3, calls.size());
		assertEquals("a", calls.get(0).getFirstChild().getText());
		assertEquals("b", calls.get(1).getFirstChild().getText());
		assertEquals("c", calls.get(2).getFirstChild().getText());
	}

	@MethodSource("collectParameterNamesProvider")
	@ParameterizedTest
	void testCollectParameterNames(String source, int tokenType, Set<String> expected) throws Exception {
		final var ast = parseSource(source);
		final var def = findFirst(ast, tokenType);
		assertEquals(expected, AstQuery.collectParameterNames(def));
	}

	@MethodSource("collectParameterTypesProvider")
	@ParameterizedTest
	void testCollectParameterTypes(String source, int tokenType, List<String> expected) throws Exception {
		final var ast = parseSource(source);
		final var def = findFirst(ast, tokenType);
		assertEquals(expected, AstQuery.collectParameterTypes(def));
	}

	@Test
	public void testContainsCastToCommentBeforeOperand() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object obj) { return (String) /*c*/ obj; } }",
				true,
				root -> AstQuery.containsCastTo(root, "String", "obj")
		);
	}

	@Test
	public void testContainsCastToCommentBeforeTypeName() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object obj) { return (/*c*/ String) obj; } }",
				true,
				root -> AstQuery.containsCastTo(root, "String", "obj")
		);
	}

	@Test
	public void testContainsCastToCommentDoesNotMakeAWrongExprMatch() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object other) { return (String) /*c*/ other; } }",
				false,
				root -> AstQuery.containsCastTo(root, "String", "obj")
		);
	}

	@Test
	public void testContainsCastToDeepTree() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a" + ".b".repeat(500) + "; }");
		assertFalse(AstQuery.containsCastTo(node, "String", "obj"));
	}

	@Test
	public void testContainsCastToFalse() {
		final var method = findMethod(root(), "emptyBlock");
		assertFalse(AstQuery.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToTrue() {
		final var method = findMethod(root(), "castAndResolve");
		assertTrue(AstQuery.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToWrongExpr() {
		final var method = findMethod(root(), "castWrongExpr");
		assertFalse(AstQuery.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToWrongType() {
		final var method = findMethod(root(), "castWrongType");
		assertFalse(AstQuery.containsCastTo(method, "String", "obj"));
	}

	@MethodSource("countArgumentsProvider")
	@ParameterizedTest
	void testCountArguments(String source, int expected) throws Exception {
		final var elist = findFirst(parseSource(source), TokenTypes.ELIST);
		assertEquals(expected, AstQuery.countArguments(elist));
	}

	@MethodSource("endsWithDanglingIfProvider")
	@ParameterizedTest
	void testEndsWithDanglingIf(String body, boolean expected) throws Exception {
		assertSameWithAndWithoutComments(
				"class T { void m(boolean a, boolean c, int[] list) { if (a) " + body + " } }",
				expected,
				root -> {
					final var block = requireNonNull(
							findFirst(requireNonNull(findFirst(root, TokenTypes.LITERAL_IF)), TokenTypes.SLIST)
					);
					return AstQuery.endsWithDanglingIf(requireNonNull(AstQuery.unwrapSingleStatementBlock(block)));
				}
		);
	}

	@Test
	public void testFindNewClassTypeArgumentsConstructorLevelSkipped() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstQuery.findNewClassTypeArguments(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsConstructorLevelWithClassLevel() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { <U> T(U arg) {} void f() { var x = new <String>ArrayList<Object>(\"a\"); } }"
		);
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstQuery.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
		final var typeArg = typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT);
		final var ident = typeArg.findFirstToken(TokenTypes.IDENT);
		assertEquals("Object", ident.getText());
	}

	@Test
	public void testFindNewClassTypeArgumentsDiamond() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstQuery.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs == null || typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) == null);
	}

	@Test
	public void testFindNewClassTypeArgumentsNoTypeArgs() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstQuery.findNewClassTypeArguments(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsQualifiedName() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<Object>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstQuery.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
	}

	@Test
	public void testFindNewClassTypeArgumentsSimpleName() throws Exception {
		final var ast = parseSource("import java.util.ArrayList;\nclass T { void f() { var x = new ArrayList<Object>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstQuery.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
	}

	@Test
	public void testFindNodeAtAscendsFromNonRoot() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		final var deepChild = requireNonNull(findFirst(root, TokenTypes.SLIST));
		final var found = AstQuery.findNodeAt(
				deepChild,
				call.getLineNo() - 1,
				call.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_CALL
		);
		assertSame(call, found);
	}

	@Test
	public void testFindNodeAtAscendsThenCrossesToEarlierSibling() throws Exception {
		final var root = parseSource("class A { void g() {} }\nclass B { void h() {} }");
		final var methodG = requireNonNull(findMethod(root, "g"));
		final var deepInB = requireNonNull(findFirst(requireNonNull(findMethod(root, "h")), TokenTypes.SLIST));
		final var found = AstQuery.findNodeAt(
				deepInB,
				methodG.getLineNo() - 1,
				methodG.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_DEF
		);
		assertSame(methodG, found);
	}

	@Test
	public void testFindNodeAtAscendsThenCrossesToLaterSibling() throws Exception {
		final var root = parseSource("class A { void g() {} }\nclass B { void h() {} }");
		final var methodH = requireNonNull(findMethod(root, "h"));
		final var deepInA = requireNonNull(findFirst(requireNonNull(findMethod(root, "g")), TokenTypes.SLIST));
		final var found = AstQuery.findNodeAt(
				deepInA,
				methodH.getLineNo() - 1,
				methodH.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_DEF
		);
		assertSame(methodH, found);
	}

	@Test
	public void testFindNodeAtColumnOffByOneReturnsNull() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		assertNull(AstQuery.findNodeAt(
				root,
				call.getLineNo() - 1,
				call.getColumnNo() + 1,
				n -> n.getType() == TokenTypes.METHOD_CALL
		));
	}

	@Test
	public void testFindNodeAtDeepTree() throws Exception {
		final var sb = new StringBuilder("class T { int f() { return 0");
		for (var i = 0; i < 500; ++i)
			sb.append("\n\t\t\t+ ").append(i);
		sb.append("; } }");
		final var root = parseSource(sb.toString());
		assertNull(AstQuery.findNodeAt(root, 0, 0, n -> n.getType() == TokenTypes.LITERAL_TRY));
	}

	@Test
	public void testFindNodeAtDisambiguatesByPredicate() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		final var expr = requireNonNull(findFirst(root, TokenTypes.EXPR));
		assertEquals(expr.getColumnNo(), call.getColumnNo(), "EXPR and its METHOD_CALL child must share a column for this test");
		final var found = AstQuery.findNodeAt(
				root,
				call.getLineNo() - 1,
				call.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_CALL
		);
		assertSame(call, found);
	}

	@Test
	public void testFindNodeAtLineOffByOneReturnsNull() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		assertNull(AstQuery.findNodeAt(
				root,
				call.getLineNo(),
				call.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_CALL
		));
	}

	@Test
	public void testFindNodeAtNoMatch() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		assertNull(AstQuery.findNodeAt(
				root,
				call.getLineNo() - 1,
				call.getColumnNo(),
				n -> n.getType() == TokenTypes.LITERAL_TRY
		));
	}

	@Test
	public void testFindNodeAtPredicateMatchesElsewhereReturnsNull() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var expr = requireNonNull(findFirst(root, TokenTypes.EXPR));
		assertNull(AstQuery.findNodeAt(
				root,
				expr.getLineNo() - 1,
				expr.getColumnNo(),
				n -> n.getType() == TokenTypes.SLIST
		));
	}

	@Test
	public void testFindNodeAtReturnsShallowestWhenMultipleMatch() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var expr = requireNonNull(findFirst(root, TokenTypes.EXPR));
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		assertEquals(expr.getColumnNo(), call.getColumnNo(), "EXPR and its METHOD_CALL child must share a column for this test");
		final var found = AstQuery.findNodeAt(
				root,
				expr.getLineNo() - 1,
				expr.getColumnNo(),
				n -> n.getType() == TokenTypes.EXPR || n.getType() == TokenTypes.METHOD_CALL
		);
		assertSame(expr, found);
	}

	@Test
	public void testFindNodeAtSecondTopLevelClass() throws Exception {
		final var root = parseSource("class A { void g() {} }\nclass B { void h() {} }");
		final var method = requireNonNull(findMethod(root, "h"));
		final var found = AstQuery.findNodeAt(
				root,
				method.getLineNo() - 1,
				method.getColumnNo(),
				n -> n.getType() == TokenTypes.METHOD_DEF
		);
		assertSame(method, found);
	}

	@Test
	public void testFirstColumnAssignmentExprStartsAtLeftOperand() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f(int x) {\n\t\tx = 5;\n\t}\n}");
		final var assign = requireNonNull(findFirst(ast, TokenTypes.ASSIGN));
		final var expr = assign.getParent();
		assertEquals(assign.getColumnNo(), expr.getColumnNo(), "EXPR must sit at the '=' for this test to be meaningful");
		assertEquals(assign.getFirstChild().getColumnNo(), AstQuery.firstColumn(expr));
	}

	@Test
	public void testFirstColumnCommentToTheLeftOfTheDeclaration() throws Exception {
		assertSameWithAndWithoutComments(
				"class T {\n/*c*/\n\tvoid m() {\n\t}\n}",
				1,
				root -> AstQuery.firstColumn(requireNonNull(findFirst(root, TokenTypes.METHOD_DEF)))
		);
	}

	@Test
	public void testFirstColumnDeepTree() throws Exception {
		final var prefix = "class T { int f() { return ";
		final var sb = new StringBuilder(prefix).append('0');
		for (var i = 0; i < 500; ++i)
			sb.append("\n\t\t\t+ ").append(i);
		sb.append("; } }");
		final var ast = parseSource(sb.toString());
		final var expr = requireNonNull(findFirst(ast, TokenTypes.EXPR));
		assertEquals(prefix.length(), AstQuery.firstColumn(expr));
	}

	@Test
	public void testFirstColumnFirstLineFromDescendant() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f(int x) {\n\t\tx\n\t\t\t\t= 5;\n\t}\n}");
		final var assign = requireNonNull(findFirst(ast, TokenTypes.ASSIGN));
		final var expr = assign.getParent();
		assertEquals(4, expr.getLineNo(), "EXPR must sit on the '=' line for this test to be meaningful");
		assertEquals(2, AstQuery.firstColumn(expr));
	}

	@Test
	public void testFirstColumnIgnoresEarlierColumnsOnLaterLines() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n1\n\t\t);\n\t}\n\tvoid g(int a) {}\n}");
		final var call = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(2, AstQuery.firstColumn(call));
	}

	@Test
	public void testFirstLineAnnotationOnEarlierLine() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated\n\tint x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstQuery.firstLine(varDef));
	}

	@Test
	public void testFirstLineAnnotationSameLine() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated int x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstQuery.firstLine(varDef));
	}

	@Test
	public void testFirstLineBlockCommentBeforeDeclaration() throws Exception {
		assertSameWithAndWithoutComments(
				"class T {\n\t/* leading */\n\tvoid m() {\n\t}\n}",
				3,
				root -> AstQuery.firstLine(requireNonNull(findFirst(root, TokenTypes.METHOD_DEF)))
		);
	}

	@Test
	public void testFirstLineCommentBetweenModifiersAndType() throws Exception {
		assertSameWithAndWithoutComments(
				"class T {\n\tpublic\n\t/*c*/\n\tstatic int x;\n}",
				2,
				root -> AstQuery.firstLine(requireNonNull(findFirst(root, TokenTypes.VARIABLE_DEF)))
		);
	}

	@Test
	public void testFirstLineDeepTree() throws Exception {
		final var sb = new StringBuilder("class T { int f() { return 0");
		for (var i = 0; i < 500; ++i)
			sb.append("\n\t\t\t+ ").append(i);
		sb.append("; } }");
		final var ast = parseSource(sb.toString());
		final var exprStart = requireNonNull(findFirst(ast, TokenTypes.LITERAL_RETURN));
		assertEquals(1, AstQuery.firstLine(exprStart));
	}

	@Test
	public void testFirstLineGrandchildEarliest() throws Exception {
		final var ast = parseSource("@Deprecated\nclass T {\n\tvoid f() {}\n}");
		final var classDef = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		assertEquals(1, AstQuery.firstLine(classDef));
	}

	@Test
	public void testFirstLineMultiLineMethodCall() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n\t\t\t1,\n\t\t\t2\n\t\t);\n\t}\n\tvoid g(int a, int b) {}\n}");
		final var methodCall = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(3, AstQuery.firstLine(methodCall));
	}

	@Test
	public void testFirstLineMultipleStackedAnnotations() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated\n\t@SuppressWarnings(\"x\")\n\tint x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstQuery.firstLine(varDef));
	}

	@Test
	public void testFirstLineSameLine() throws Exception {
		final var ast = parseSource("class T { int x; }");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(1, AstQuery.firstLine(varDef));
	}

	@Test
	public void testFirstLineSingleLeaf() throws Exception {
		final var ast = parseSource("class T { int x; }");
		final var ident = requireNonNull(findFirst(ast, TokenTypes.IDENT));
		assertEquals(ident.getLineNo(), AstQuery.firstLine(ident));
	}

	@Test
	public void testFirstLineSingleLineCommentBeforeDeclaration() throws Exception {
		assertSameWithAndWithoutComments(
				"class T {\n\t// leading\n\tvoid m() {\n\t}\n}",
				3,
				root -> AstQuery.firstLine(requireNonNull(findFirst(root, TokenTypes.METHOD_DEF)))
		);
	}

	@Test
	public void testGetMethodNameBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("foo", AstQuery.getMethodName(methodCall));
	}

	@Test
	public void testGetMethodNameDottedCall() throws Exception {
		final var ast = parseSource("class T { void f(String s) { s.trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("trim", AstQuery.getMethodName(methodCall));
	}

	@Test
	public void testHasModifierAbsent() throws Exception {
		final var varDef = requireNonNull(findFirst(parseSource("class T { int x; }"), TokenTypes.VARIABLE_DEF));
		assertFalse(AstQuery.hasModifier(varDef, TokenTypes.LITERAL_STATIC));
	}

	@Test
	public void testHasModifierPresent() throws Exception {
		final var varDef = requireNonNull(findFirst(parseSource("class T { static int x; }"), TokenTypes.VARIABLE_DEF));
		assertTrue(AstQuery.hasModifier(varDef, TokenTypes.LITERAL_STATIC));
	}

	@Test
	public void testHasModifierWithoutModifiersChild() throws Exception {
		final var ident = requireNonNull(findFirst(parseSource("class T { int x; }"), TokenTypes.IDENT));
		assertFalse(AstQuery.hasModifier(ident, TokenTypes.LITERAL_STATIC));
	}

	@MethodSource("hasSuppressWarningsProvider")
	@ParameterizedTest
	void testHasSuppressWarnings(String source, String key, boolean expected) throws Exception {
		final var ast = parseSource(source);
		final var classDef = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		final var modifiers = classDef.findFirstToken(TokenTypes.MODIFIERS);
		assertEquals(expected, AstQuery.hasSuppressWarnings(modifiers, key));
	}

	@ParameterizedTest
	@ValueSource(ints = {TokenTypes.EQUAL, TokenTypes.NOT_EQUAL, TokenTypes.LE, TokenTypes.GE,
			TokenTypes.METHOD_CALL, TokenTypes.INC, TokenTypes.DEC, TokenTypes.POST_INC, TokenTypes.POST_DEC,
			TokenTypes.LAND, TokenTypes.LITERAL_NEW, TokenTypes.IDENT})
	void testIsAssignmentOperatorFalse(int tokenType) {
		assertFalse(AstQuery.isAssignmentOperator(tokenType));
	}

	@ParameterizedTest
	@ValueSource(ints = {TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN,
			TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN, TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN,
			TokenTypes.STAR_ASSIGN})
	void testIsAssignmentOperatorTrue(int tokenType) {
		assertTrue(AstQuery.isAssignmentOperator(tokenType));
	}

	@Test
	public void testIsEmptyBodyBlock() {
		final var method = findMethod(root(), "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertTrue(AstQuery.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyCommentOnlyBlockIsNotHardened() throws Exception {
		// Pinned, not fixed: whether a comment-only block still counts as empty is a product
		// question owned by the per-check comment opt-in, not by this hardening pass. The
		// assertion records the measured divergence so the decision cannot be made by accident.
		final var source = "class T { void m(int x) { if (x > 0) { /*only*/ } } }";
		assertTrue(AstQuery.isEmptyBody(requireNonNull(findFirst(findFirst(parseSource(source), TokenTypes.LITERAL_IF), TokenTypes.SLIST))));
		assertFalse(AstQuery.isEmptyBody(requireNonNull(findFirst(findFirst(parseSourceWithComments(source), TokenTypes.LITERAL_IF), TokenTypes.SLIST))));
	}

	@Test
	public void testIsEmptyBodyDefaultToken() throws Exception {
		final var ast = parseSource("class T { void f() { int x = 1; } }");
		final var varDef = findFirst(ast, TokenTypes.VARIABLE_DEF);
		assertFalse(AstQuery.isEmptyBody(varDef));
	}

	@Test
	public void testIsEmptyBodyNonEmpty() {
		final var method = findMethod(root(), "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertFalse(AstQuery.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyStatement() {
		final var method = findMethod(root(), "emptyStatement");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var ifNode = slist.findFirstToken(TokenTypes.LITERAL_IF);
		final var rparen = ifNode.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen.getNextSibling();
		assertTrue(AstQuery.isEmptyBody(body));
	}

	@Test
	public void testIsPureDotChainOrIdentBareIdent() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = x; } }");
		assertTrue(AstQuery.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentChainWithComments() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object a) { return a./*c*/b.c; } }",
				true,
				root -> AstQuery.isPureDotChainOrIdent(requireNonNull(findFirst(root, TokenTypes.DOT)))
		);
	}

	@Test
	public void testIsPureDotChainOrIdentClassLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = String.class; }");
		assertFalse(AstQuery.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentCommentBeforeTheChain() throws Exception {
		assertSameWithAndWithoutComments(
				"class T { Object f(Object a) { return /*c*/ a.b; } }",
				true,
				root -> AstQuery.isPureDotChainOrIdent(requireNonNull(findFirst(root, TokenTypes.DOT)))
		);
	}

	@Test
	public void testIsPureDotChainOrIdentCommentNodeItself() throws Exception {
		final var ast = parseSourceWithComments("class T { Object f(Object a) { return a./*c*/b; } }");
		assertFalse(AstQuery.isPureDotChainOrIdent(requireNonNull(findFirst(ast, TokenTypes.BLOCK_COMMENT_BEGIN))));
	}

	@Test
	public void testIsPureDotChainOrIdentDeepChain() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a.b.c.d; }");
		assertTrue(AstQuery.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentNonDotNonIdent() throws Exception {
		final var node = parseExprFirstChild("class T { void g() {} void f() { Object a = g(); } }");
		assertFalse(AstQuery.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentSimpleDot() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a.b; }");
		assertTrue(AstQuery.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureExpressionAssign() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x = 1; } }");
		final var assign = findFirst(ast, TokenTypes.ASSIGN);
		assertFalse(AstQuery.isPureExpression(assign));
	}

	@Test
	public void testIsPureExpressionCharLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { char a = 'x'; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@MethodSource("isPureExpressionCommentProvider")
	@ParameterizedTest
	void testIsPureExpressionComments(String source, boolean expected) throws Exception {
		assertSameWithAndWithoutComments(
				source,
				expected,
				root -> AstQuery.isPureExpression(requireNonNull(findFirst(findFirst(root, TokenTypes.LITERAL_RETURN), TokenTypes.EXPR)))
		);
	}

	@Test
	public void testIsPureExpressionDeepTree() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a" + ".b".repeat(500) + "; }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionDot() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = this.x; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionIdent() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = 1; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionIndexOp() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr) { int a = arr[0]; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionLiteralFalse() throws Exception {
		final var ast = parseSource("class T { void f() { var x = false; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_FALSE);
		assertTrue(AstQuery.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionLiteralNull() throws Exception {
		final var ast = parseSource("class T { void f() { Object x = null; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_NULL);
		assertTrue(AstQuery.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionLiteralTrue() throws Exception {
		final var ast = parseSource("class T { void f() { var x = true; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_TRUE);
		assertTrue(AstQuery.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionMethodCall() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = foo(); } int foo() { return 0; } }");
		assertFalse(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNewObject() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { Object a = new Object(); } }");
		assertFalse(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNumDouble() throws Exception {
		final var ast = parseSource("class T { void f() { double x = 1.0d; } }");
		final var num = findFirst(ast, TokenTypes.NUM_DOUBLE);
		assertTrue(AstQuery.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionNumFloat() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 1.0f; } }");
		final var num = findFirst(ast, TokenTypes.NUM_FLOAT);
		assertTrue(AstQuery.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionNumInt() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = 42; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNumLong() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 1L; } }");
		final var num = findFirst(ast, TokenTypes.NUM_LONG);
		assertTrue(AstQuery.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionPostDecrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x--; } }");
		final var postDec = findFirst(ast, TokenTypes.POST_DEC);
		assertFalse(AstQuery.isPureExpression(postDec));
	}

	@Test
	public void testIsPureExpressionPostIncrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x++; } }");
		final var postInc = findFirst(ast, TokenTypes.POST_INC);
		assertFalse(AstQuery.isPureExpression(postInc));
	}

	@Test
	public void testIsPureExpressionPreDecrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { --x; } }");
		final var preDec = findFirst(ast, TokenTypes.DEC);
		assertFalse(AstQuery.isPureExpression(preDec));
	}

	@Test
	public void testIsPureExpressionPreIncrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { ++x; } }");
		final var preInc = findFirst(ast, TokenTypes.INC);
		assertFalse(AstQuery.isPureExpression(preInc));
	}

	@Test
	public void testIsPureExpressionStringLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { String a = \"hello\"; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionThis() throws Exception {
		final var ast = parseSource("class T { void f() { Object a = this; } }");
		final var literalThis = findFirst(ast, TokenTypes.LITERAL_THIS);
		assertTrue(AstQuery.isPureExpression(literalThis));
	}

	@Test
	public void testIsPureExpressionUnaryMinus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = -a; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionUnaryPlus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = +a; } }");
		assertTrue(AstQuery.isPureExpression(node));
	}

	@Test
	public void testIsSideEffectFreeDeepTree() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a" + ".b".repeat(500) + "; }");
		assertTrue(AstQuery.isSideEffectFree(node));
	}

	@ParameterizedTest
	@ValueSource(strings = {"foo()", "a && foo()", "new boolean[]{foo()}[0]", "new Object() instanceof String", "++i > 0", "i++ > 0", "i-- > 0", "--i > 0", "flag = other", "flag &= other", "flag |= other", "flag ^= other", "(x += 1) > 0", "(x -= 1) > 0", "(x *= 2) > 0", "(x /= 2) > 0", "(x %= 2) > 0", "(x <<= 1) > 0", "(x >>= 1) > 0", "(x >>>= 1) > 0", "arr[i]++ > 0", "(new boolean[size()])[0]", "func(new boolean[]{a})", "(new String[]{s})[0].isEmpty()"})
	void testIsSideEffectFreeFalse(String cond) throws Exception {
		final var ast = parseSource("class T { void f(int i, int x, int n, int[] arr, boolean a, boolean b, boolean c, boolean flag, boolean other, Object obj, String s) { if (" + cond + ") {} } }");
		assertFalse(AstQuery.isSideEffectFree(findFirst(ast, TokenTypes.EXPR)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"a", "a && b", "x > 0", "i % x > 0", "arr.length > 0", "(boolean) obj", "!flag", "(a || b) && c", "new boolean[]{a}[0]", "obj instanceof String p", "a ? b : c", "(new int[]{x})[0]++ > 0", "--(new int[]{x})[0] > 0", "++(new int[]{x})[0] > 0", "(new int[]{x})[0]-- > 0", "(new boolean[n])[0]"})
	void testIsSideEffectFreeTrue(String cond) throws Exception {
		final var ast = parseSource("class T { void f(int i, int x, int n, int[] arr, boolean a, boolean b, boolean c, boolean flag, boolean other, Object obj, String s) { if (" + cond + ") {} } }");
		assertTrue(AstQuery.isSideEffectFree(findFirst(ast, TokenTypes.EXPR)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"1", "1L", "1.0", "1.0f", "0x1", "0b1", ".1", "0.0e1", "0.0E1", "0x1p0", "0x0p1"})
	void testIsZeroLiteralFalse(String literal) throws Exception {
		assertFalse(isZeroLiteral(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {"-0", "-0L", "-0.0", "-0.0f", "-0f", "-0.0d", "-0.", "-.0", "-0x0",
			"-0X0", "-0x0L", "-0b0", "-0B0", "-0b0L", "-0_0", "-0.0e0", "-0.0e+0", "-0.0e-0"})
	void testIsZeroLiteralNegativeZero(String literal) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + literal + "; } }");
		final var unaryMinus = findFirst(ast, TokenTypes.UNARY_MINUS);
		assertFalse(AstQuery.isZeroLiteral(unaryMinus));
		for (var type : new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG}) {
			final var num = findFirst(unaryMinus, type);
			if (num != null) {
				assertTrue(AstQuery.isZeroLiteral(num));
				return;
			}
		}
		throw new AssertionError("No numeric literal found in: " + literal);
	}

	@Test
	public void testIsZeroLiteralNonNumericToken() throws Exception {
		final var ast = parseSource("class T { void f() { var x = true; } }");
		final var literalTrue = findFirst(ast, TokenTypes.LITERAL_TRUE);
		assertFalse(AstQuery.isZeroLiteral(literalTrue));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "0L", "0.0", "0.0f", "0f", "0.0d", "0.", ".0", "0x0", "0X0",
			"0x0L", "0b0", "0B0", "0b0L", "0_0", "0.0e0", "0.0e+0", "0.0e-0", "0.0E0", "0x0p0",
			"0X0P0", "0x0.0p0f"})
	void testIsZeroLiteralTrue(String literal) throws Exception {
		assertTrue(isZeroLiteral(literal));
	}

	@Test
	public void testLastLineDeepTree() throws Exception {
		final var sb = new StringBuilder("class T { int f() { return 0");
		for (var i = 0; i < 500; ++i)
			sb.append("\n\t\t\t+ ").append(i);
		sb.append("; } }");
		final var ast = parseSource(sb.toString());
		final var ret = requireNonNull(findFirst(ast, TokenTypes.LITERAL_RETURN));
		assertEquals(501, AstQuery.lastLine(ret));
	}

	@Test
	public void testLastLineIsUnaffectedByComments() throws Exception {
		// max-over-descendants stays monotone: a comment is inserted before a real token in the
		// same parent, so some real sibling always sits at or past the comment's line
		assertSameWithAndWithoutComments(
				"class T {\n\tvoid m() {\n\t\t/*c*/\n\t}\n}",
				4,
				root -> AstQuery.lastLine(requireNonNull(findFirst(root, TokenTypes.METHOD_DEF)))
		);
	}

	@Test
	public void testLastLineMultiLine() {
		final var method = findMethod(root(), "multiLine");
		assertEquals(42, AstQuery.lastLine(method));
	}

	@Test
	public void testLastLineMultiLineAnnotation() throws Exception {
		final var ast = parseSource("class T {\n\t@SuppressWarnings(\n\t\t\t\"x\"\n\t)\n\tint y;\n}");
		final var annotation = requireNonNull(findFirst(ast, TokenTypes.ANNOTATION));
		assertEquals(4, AstQuery.lastLine(annotation));
	}

	@Test
	public void testLastLineMultiLineMethodCall() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n\t\t\t1,\n\t\t\t2\n\t\t);\n\t}\n\tvoid g(int a, int b) {}\n}");
		final var methodCall = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(6, AstQuery.lastLine(methodCall));
	}

	@Test
	public void testLastLineNestedClass() throws Exception {
		final var ast = parseSource("class T {\n\tclass U {\n\t\tint a;\n\t\tint b;\n\t}\n}");
		final var outer = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		assertEquals(6, AstQuery.lastLine(outer));
	}

	@Test
	public void testLastLineSameLineTie() throws Exception {
		final var ast = parseSource("class T { void f() { int x; } }");
		final var method = requireNonNull(findFirst(ast, TokenTypes.METHOD_DEF));
		assertEquals(1, AstQuery.lastLine(method));
	}

	@Test
	public void testLastLineSingleLine() {
		final var method = findMethod(root(), "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var rcurly = slist.findFirstToken(TokenTypes.RCURLY);
		assertEquals(rcurly.getLineNo(), AstQuery.lastLine(slist));
	}

	@MethodSource("rebindsAFollowingElseProvider")
	@ParameterizedTest
	void testRebindsAFollowingElse(String statement, boolean expected) throws Exception {
		assertSameWithAndWithoutComments(
				"class T { void m(boolean a, boolean c) { if (a) " + statement + " else y(); } }",
				expected,
				root -> AstQuery.rebindsAFollowingElse(requireNonNull(
						findFirst(requireNonNull(findFirst(root, TokenTypes.LITERAL_IF)), TokenTypes.SLIST)
				))
		);
	}

	@Test
	public void testRebindsAFollowingElseAtRootReturnsFalse() throws Exception {
		final var root = parseSource("class T { void m(boolean a) { if (a) x(); else y(); } }");
		assertNull(root.getParent(), "the parsed root must have no parent for this test to be meaningful");
		assertFalse(AstQuery.rebindsAFollowingElse(root));
	}

	@Test
	public void testSingleExpressionStatementBody() throws Exception {
		final var exprBody = findFirst(parseSource("class C { Runnable r = () -> { foo(); }; }"), TokenTypes.SLIST);
		assertEquals(TokenTypes.EXPR, AstQuery.singleExpressionStatementBody(exprBody).getType());
		final var returnBody = findFirst(parseSource("class C { Runnable r = () -> { return; }; }"), TokenTypes.SLIST);
		assertNull(AstQuery.singleExpressionStatementBody(returnBody));
		final var multiBody = findFirst(parseSource("class C { Runnable r = () -> { a(); b(); }; }"), TokenTypes.SLIST);
		assertNull(AstQuery.singleExpressionStatementBody(multiBody));
	}

	@MethodSource("unwrapSingleStatementBlockCommentProvider")
	@ParameterizedTest
	void testSingleExpressionStatementBodyComments(String body, boolean unwrappable) throws Exception {
		assertSameWithAndWithoutComments(
				"class T { void m(int x) { if (x > 0) " + body + " } }",
				// ASSIGN has no arm in displayText's switch, so it renders through the exprText
				// fallback: leaf text only, no `=`
				unwrappable ? "x1" : null,
				root -> {
					final var single = AstQuery.singleExpressionStatementBody(
							requireNonNull(findFirst(requireNonNull(findFirst(root, TokenTypes.LITERAL_IF)), TokenTypes.SLIST))
					);
					return single == null ? null : AstDisplay.displayText(single);
				}
		);
	}

	@Test
	public void testTypeParameterCountFromDirectClassDef() throws Exception {
		final var generic = requireNonNull(findFirst(parseSource("class T<A, B> {}"), TokenTypes.CLASS_DEF));
		assertEquals(2, AstQuery.typeParameterCount(generic));
		final var plain = requireNonNull(findFirst(parseSource("class T {}"), TokenTypes.CLASS_DEF));
		assertEquals(0, AstQuery.typeParameterCount(plain));
	}

	@Test
	public void testUnwrapParensAndExprExprWrapper() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = a; }"), TokenTypes.ASSIGN);
		final var unwrapped = AstQuery.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprFromEndNestedParens() throws Exception {
		final var land = findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && (((b))); } }"), TokenTypes.LAND);
		final var unwrapped = AstQuery.unwrapParensAndExprFromEnd(requireNonNull(land).getLastChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("b", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprFromEndNonWrapper() throws Exception {
		final var operand = requireNonNull(findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && b; } }"), TokenTypes.LAND)).getLastChild();
		assertSame(operand, AstQuery.unwrapParensAndExprFromEnd(operand));
	}

	@Test
	public void testUnwrapParensAndExprFromEndNull() {
		assertNull(AstQuery.unwrapParensAndExprFromEnd(null));
	}

	@Test
	public void testUnwrapParensAndExprFromEndParenOperand() throws Exception {
		final var land = findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && (b); } }"), TokenTypes.LAND);
		final var unwrapped = AstQuery.unwrapParensAndExprFromEnd(requireNonNull(land).getLastChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("b", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprNestedParens() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = (((a))); }"), TokenTypes.ASSIGN);
		final var unwrapped = AstQuery.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprNonWrapper() throws Exception {
		final var ident = findFirst(parseSource("class T { Object x = a; }"), TokenTypes.IDENT);
		assertEquals(ident, AstQuery.unwrapParensAndExpr(ident));
	}

	@Test
	public void testUnwrapParensAndExprNull() {
		assertNull(AstQuery.unwrapParensAndExpr(null));
	}

	@Test
	public void testUnwrapParensAndExprParen() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = (a); }"), TokenTypes.ASSIGN);
		final var unwrapped = AstQuery.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@MethodSource("unwrapSingleStatementBlockCommentProvider")
	@ParameterizedTest
	void testUnwrapSingleStatementBlockComments(String body, boolean unwrappable) throws Exception {
		assertSameWithAndWithoutComments(
				"class T { void m(int x) { if (x > 0) " + body + " } }",
				unwrappable ? TokenTypes.EXPR : null,
				root -> {
					final var single = AstQuery.unwrapSingleStatementBlock(
							requireNonNull(findFirst(requireNonNull(findFirst(root, TokenTypes.LITERAL_IF)), TokenTypes.SLIST))
					);
					return single == null ? null : single.getType();
				}
		);
	}

	@Test
	public void testUnwrapSingleStatementBlockEmpty() throws Exception {
		final var ast = parseSource("class T { void f() {} }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		assertNull(AstQuery.unwrapSingleStatementBlock(slist));
	}

	@Test
	public void testUnwrapSingleStatementBlockMultiStatement() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); g(); } }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		assertNull(AstQuery.unwrapSingleStatementBlock(slist));
	}

	@Test
	public void testUnwrapSingleStatementBlockNonSlist() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); } }");
		final var expr = findFirst(findMethod(ast, "f"), TokenTypes.EXPR);
		assertEquals(expr, AstQuery.unwrapSingleStatementBlock(expr));
	}

	@Test
	public void testUnwrapSingleStatementBlockSingleStatement() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); } }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		final var unwrapped = AstQuery.unwrapSingleStatementBlock(slist);
		assertEquals(TokenTypes.EXPR, requireNonNull(unwrapped).getType());
	}
}