package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static java.util.Objects.requireNonNull;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AstUtilTest {
	private static DetailAST root;

	static Stream<Arguments> canonicalTypeGenericProvider() {
		return Stream.of(
				Arguments.of("import java.util.List; class T { List<String> x; }", "List"),
				Arguments.of("class T { java.util.List<String> x; }", "java.util.List"),
				Arguments.of("class T { java.util.Map<String, Integer> x; }", "java.util.Map"),
				Arguments.of("class T { java.util.List<String>[] x; }", "java.util.List[]"),
				Arguments.of("class T { @Deprecated java.util.List<String> x; }", "java.util.List"),
				Arguments.of("class T { @Deprecated java.util.Map<String, Integer> x; }", "java.util.Map"),
				Arguments.of("class T { @Deprecated java.util.List<String>[] x; }", "java.util.List[]"),
				Arguments.of("@interface A {} class T { java.util.List<@A String> x; }", "java.util.List"),
				Arguments.of("@interface A {} class T { java.util.Map<@A String, @A Integer> x; }", "java.util.Map"),
				Arguments.of("@interface A {} class T { java.util.List<@A String>[] x; }", "java.util.List[]")
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

	static Stream<Arguments> displayTextBinaryProvider() {
		return Stream.of(
				Arguments.of("&", TokenTypes.BAND),
				Arguments.of("|", TokenTypes.BOR),
				Arguments.of(">>>", TokenTypes.BSR),
				Arguments.of("^", TokenTypes.BXOR),
				Arguments.of("/", TokenTypes.DIV),
				Arguments.of("==", TokenTypes.EQUAL),
				Arguments.of(">=", TokenTypes.GE),
				Arguments.of(">", TokenTypes.GT),
				Arguments.of("&&", TokenTypes.LAND),
				Arguments.of("<=", TokenTypes.LE),
				Arguments.of("||", TokenTypes.LOR),
				Arguments.of("<", TokenTypes.LT),
				Arguments.of("-", TokenTypes.MINUS),
				Arguments.of("%", TokenTypes.MOD),
				Arguments.of("!=", TokenTypes.NOT_EQUAL),
				Arguments.of("+", TokenTypes.PLUS),
				Arguments.of("<<", TokenTypes.SL),
				Arguments.of(">>", TokenTypes.SR),
				Arguments.of("*", TokenTypes.STAR)
		);
	}

	static Stream<Arguments> displayTextMethodCallProvider() {
		return Stream.of(
				Arguments.of("class T { Object x = foo(); }", "foo()"),
				Arguments.of("class T { Object x = foo(a); }", "foo(a)"),
				Arguments.of("class T { Object x = foo( a ,b ); }", "foo(a, b)"),
				Arguments.of("class T { Object x = foo(a, b, c); }", "foo(a, b, c)"),
				Arguments.of("class T { Object x = getList(a, b); }", "getList(a, b)"),
				Arguments.of("class T { Object x = Math.min(a, b); }", "Math.min(a, b)"),
				Arguments.of("class T { Object x = map.values(); }", "map.values()"),
				Arguments.of("class T { Object x = this.foo(); }", "this.foo()"),
				Arguments.of("class T { Object x = super.toString(); }", "super.toString()"),
				Arguments.of("class T { Object x = a.b().c(); }", "a.b().c()"),
				Arguments.of("class T { Object x = arr[0].toString(); }", "arr[0].toString()"),
				Arguments.of("class T { Object x = f(g(x)); }", "f(g(x))"),
				Arguments.of("class T { Object x = Math.min(hi, foo(a, b)); }", "Math.min(hi, foo(a, b))"),
				Arguments.of("class T { Object x = a + foo(b); }", "a + foo(b)"),
				Arguments.of("class T { Object x = arr[foo(i)]; }", "arr[foo(i)]"),
				Arguments.of("class T { Object x = !foo(); }", "!foo()"),
				Arguments.of("class T { Object x = foo(\"a\", \"b\"); }", "foo(\"a\", \"b\")"),
				Arguments.of("class T { Object x = foo(1, 2); }", "foo(1, 2)"),
				Arguments.of("class T { Object x = foo(a + b); }", "foo(a + b)"),
				Arguments.of("class T { Object x = foo(a, b + c); }", "foo(a, b + c)"),
				Arguments.of("class T { Object x = f(g()); }", "f(g())")
		);
	}

	static Stream<Arguments> displayTextPrefixUnaryProvider() {
		return Stream.of(
				Arguments.of("~", TokenTypes.BNOT),
				Arguments.of("!", TokenTypes.LNOT)
		);
	}

	static Stream<Arguments> dottedNameProvider() {
		return Stream.of(
				Arguments.of("class T { a.B x; }", "a.B"),
				Arguments.of("class T { a.b.C x; }", "a.b.C"),
				Arguments.of("class T { a.b.c.D x; }", "a.b.c.D"),
				Arguments.of("class T { a.b.c.d.E x; }", "a.b.c.d.E"),
				Arguments.of("class T { a.b.c.d.e.F x; }", "a.b.c.d.e.F"),
				Arguments.of("class T { a.b.C<String> x; }", "a.b.C"),
				Arguments.of("class T { a.b.C<@Deprecated String> x; }", "a.b.C"),
				Arguments.of("class T { @Deprecated a.b.C x; }", "a.b.C"),
				Arguments.of("class T { @Deprecated a.b.C<@Deprecated String> x; }", "a.b.C"),
				Arguments.of("class T { Outer<String>.Inner<Integer> x; }", "Outer.Inner"),
				Arguments.of("class T { Outer<A>.Mid<B>.Deep<C> x; }", "Outer.Mid.Deep"),
				Arguments.of("class T { Outer<String>.Inner x; }", "Outer.Inner")
		);
	}

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

	@Nullable
	private static DetailAST findMethod(@Nonnull DetailAST node, @Nonnull String name) {
		if (node.getType() == TokenTypes.METHOD_DEF) {
			final var ident = node.findFirstToken(TokenTypes.IDENT);
			if (ident != null && name.equals(ident.getText()))
				return node;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findMethod(child, name);
			if (found != null)
				return found;
		}
		return null;
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

	private static boolean isZeroLiteral(@Nonnull String literal) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + literal + "; } }");
		for (var type : new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG}) {
			final var num = findFirst(ast, type);
			if (num != null)
				return AstUtil.isZeroLiteral(num);
		}
		throw new AssertionError("No numeric literal found in: " + literal);
	}

	@Nonnull
	private static DetailAST parse(@Nonnull String inputPath) throws Exception {
		final var url = AstUtilTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		requireNonNull(url, "Test input file not found: " + inputPath);
		return JavaParser.parseFile(new File(url.toURI()), JavaParser.Options.WITH_COMMENTS);
	}

	@Nonnull
	private static DetailAST parseExprFirstChild(@Nonnull String source) throws Exception {
		final var ast = parseSource(source);
		final var assign = findFirst(ast, TokenTypes.ASSIGN);
		requireNonNull(assign, "No ASSIGN found");
		final var expr = assign.getFirstChild();
		requireNonNull(expr, "No child of ASSIGN");
		return expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
	}

	@Nonnull
	private static DetailAST parseSource(@Nonnull String source) throws Exception {
		final var tmp = File.createTempFile("test", ".java");
		try {
			Files.writeString(tmp.toPath(), source);
			return JavaParser.parse(new FileContents(new FileText(tmp, StandardCharsets.UTF_8.name())));
		}
		finally {
			tmp.delete();
		}
	}

	static Stream<Arguments> primitiveArrayDeclarationProvider() {
		return Stream.of(
				Arguments.of("boolean"),
				Arguments.of("byte"),
				Arguments.of("char"),
				Arguments.of("double"),
				Arguments.of("float"),
				Arguments.of("int"),
				Arguments.of("long"),
				Arguments.of("short")
		);
	}

	static Stream<Arguments> primitiveArrayInitializerProvider() {
		return Stream.of(
				Arguments.of("boolean", "new boolean[]{true}"),
				Arguments.of("byte", "new byte[]{1}"),
				Arguments.of("char", "new char[]{'a'}"),
				Arguments.of("double", "new double[]{1.0}"),
				Arguments.of("float", "new float[]{1.0f}"),
				Arguments.of("int", "new int[]{1}"),
				Arguments.of("long", "new long[]{1L}"),
				Arguments.of("short", "new short[]{1}")
		);
	}

	static Stream<Arguments> primitiveArrayProvider() {
		return Stream.of(
				Arguments.of("boolean", "new boolean[10]"),
				Arguments.of("byte", "new byte[10]"),
				Arguments.of("char", "new char[10]"),
				Arguments.of("double", "new double[10]"),
				Arguments.of("float", "new float[10]"),
				Arguments.of("int", "new int[10]"),
				Arguments.of("long", "new long[10]"),
				Arguments.of("short", "new short[10]")
		);
	}

	static Stream<Arguments> primitiveExplicitTypeProvider() {
		return Stream.of(
				Arguments.of("boolean x = false"),
				Arguments.of("byte x = 0"),
				Arguments.of("char x = 0"),
				Arguments.of("double x = 0"),
				Arguments.of("float x = 0"),
				Arguments.of("int x = 0"),
				Arguments.of("long x = 0"),
				Arguments.of("short x = 0")
		);
	}

	@BeforeAll
	public static void setUp() throws Exception {
		root = parse("astutil/InputAstUtil.java");
	}

	static Stream<Arguments> simpleNameProvider() {
		return Stream.of(
				Arguments.of("java.util.List", "List"),
				Arguments.of("a.b.c.D", "D"),
				Arguments.of("a.b.Outer.Inner", "Inner"),
				Arguments.of("List", "List"),
				Arguments.of("T", "T"),
				Arguments.of("java.util.", ""),
				Arguments.of("", "")
		);
	}

	private static int typeParameterCountOf(@Nonnull DetailAST scope, @Nonnull String className) {
		return AstUtil.typeParameterCount(
				requireNonNull(AstUtil.sameFileClassDef(scope, className), "no such type: " + className)
		);
	}

	@Test
	public void testAnnotationNameQualified() {
		final var classAnnotation = findFirst(root, TokenTypes.ANNOTATION);
		assertEquals("CheckReturnValue", AstUtil.annotationName(classAnnotation));
	}

	@Test
	public void testAnnotationNameSimple() {
		final var objBlock = findFirst(root, TokenTypes.OBJBLOCK);
		final var fieldAnnotation = findFirst(objBlock, TokenTypes.ANNOTATION);
		assertEquals("Nonnull", AstUtil.annotationName(fieldAnnotation));
	}

	@Test
	public void testAstStructuralEqualsChildCountMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = f(a); }");
		final var b = parseExprFirstChild("class T { Object x = f(a, b); }");
		assertFalse(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsDottedChain() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a.b.c.d.e.f.g.h.i.j; }");
		final var b = parseExprFirstChild("class T { Object x = a.b.c.d.e.f.g.h.i.j; }");
		assertTrue(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsIdentical() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = ++i; }");
		final var b = parseExprFirstChild("class T { Object x = ++i; }");
		assertTrue(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsSiblingOrder() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a + b; }");
		final var b = parseExprFirstChild("class T { Object x = b + a; }");
		assertFalse(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsTextMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = a; }");
		final var b = parseExprFirstChild("class T { Object x = b; }");
		assertFalse(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testAstStructuralEqualsTypeMismatch() throws Exception {
		final var a = parseExprFirstChild("class T { Object x = ++i; }");
		final var b = parseExprFirstChild("class T { Object x = --i; }");
		assertFalse(AstUtil.astStructuralEquals(a, b));
	}

	@Test
	public void testCanonicalAnnotationEmptyParens() throws Exception {
		final var ast = parseSource("@Deprecated() class T {}");
		final var annotation = findFirst(ast, TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationExplicitValue() throws Exception {
		final var ast = parseSource("class T { @SuppressWarnings(value = \"unused\") int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("SuppressWarnings(value=\"unused\")", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationMarker() throws Exception {
		final var ast = parseSource("class T { @Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationMaxDepthZero() throws Exception {
		final var ast = parseSource("class T { @Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("", AstUtil.canonicalAnnotation(annotation, 0));
	}

	@Test
	public void testCanonicalAnnotationMultiParam() throws Exception {
		final var ast = parseSource("@interface M { int b() default 0; int a() default 0; }\nclass T { @M(b = 2, a = 1) int x; }");
		final var varDef = findFirst(ast, TokenTypes.VARIABLE_DEF);
		final var annotation = findFirst(varDef, TokenTypes.ANNOTATION);
		assertEquals("M(a=1,b=2)", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationQualified() throws Exception {
		final var ast = parseSource("class T { @java.lang.Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationSingleValue() throws Exception {
		final var ast = parseSource("class T { @SuppressWarnings(\"unused\") int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("SuppressWarnings(value=\"unused\")", AstUtil.canonicalAnnotation(annotation, 50));
	}

	@ParameterizedTest
	@ValueSource(strings = {"boolean", "byte", "char", "double", "float", "int", "int[]",
			"int[][]", "java.util.List", "java.util.List[]", "long", "short", "String",
			"String[]"})
	void testCanonicalTypeAnnotatedField(String type) throws Exception {
		final var ast = parseSource("class T { @Deprecated " + type + " x; }");
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(type, AstUtil.canonicalType(typeNode));
	}

	@ParameterizedTest
	@ValueSource(strings = {"boolean", "byte", "char", "double", "float", "int", "int[]",
			"int[][]", "java.util.List", "java.util.List[]", "long", "short", "String",
			"String[]"})
	void testCanonicalTypeField(String type) throws Exception {
		final var ast = parseSource("class T { " + type + " x; }");
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(type, AstUtil.canonicalType(typeNode));
	}

	@MethodSource("canonicalTypeGenericProvider")
	@ParameterizedTest
	void testCanonicalTypeGeneric(String source, String expected) throws Exception {
		final var ast = parseSource(source);
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(expected, AstUtil.canonicalType(typeNode));
	}

	@Test
	public void testCanonicalTypeVoid() throws Exception {
		final var ast = parseSource("class T { void f() {} }");
		final var method = findFirst(ast, TokenTypes.METHOD_DEF);
		final var type = method.findFirstToken(TokenTypes.TYPE);
		assertEquals("void", AstUtil.canonicalType(type));
	}

	@Test
	public void testCollectAnnotationsMultiple() throws Exception {
		final var ast = parseSource("class T { void f(@Deprecated @Override String p) {} }");
		final var param = findFirst(ast, TokenTypes.PARAMETER_DEF);
		final var modifiers = param.findFirstToken(TokenTypes.MODIFIERS);
		assertEquals(2, AstUtil.collectAnnotations(modifiers).size());
	}

	@Test
	public void testCollectAnnotationsNone() {
		final var method = findMethod(root, "emptyBlock");
		final var params = method.findFirstToken(TokenTypes.PARAMETERS);
		assertTrue(AstUtil.collectAnnotations(params).isEmpty());
	}

	@MethodSource("collectInstanceFieldTypesProvider")
	@ParameterizedTest
	void testCollectInstanceFieldTypes(String source, List<String> expected) throws Exception {
		final var ast = parseSource(source);
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals(expected, AstUtil.collectInstanceFieldTypes(objBlock));
	}

	@Test
	public void testCollectMatchingNoMatch() throws Exception {
		final var ast = parseSource("class T { void f() { a(); } }");
		assertTrue(AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.LITERAL_TRY).isEmpty());
	}

	@Test
	public void testCollectMatchingPreOrder() throws Exception {
		final var ast = parseSource("class T { void f() { a(); b(c()); } }");
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL);
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
		assertEquals(expected, AstUtil.collectParameterNames(def));
	}

	@MethodSource("collectParameterTypesProvider")
	@ParameterizedTest
	void testCollectParameterTypes(String source, int tokenType, List<String> expected) throws Exception {
		final var ast = parseSource(source);
		final var def = findFirst(ast, tokenType);
		assertEquals(expected, AstUtil.collectParameterTypes(def));
	}

	@Test
	public void testContainsCastToFalse() {
		final var method = findMethod(root, "emptyBlock");
		assertFalse(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToTrue() {
		final var method = findMethod(root, "castAndResolve");
		assertTrue(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToWrongExpr() {
		final var method = findMethod(root, "castWrongExpr");
		assertFalse(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@Test
	public void testContainsCastToWrongType() {
		final var method = findMethod(root, "castWrongType");
		assertFalse(AstUtil.containsCastTo(method, "String", "obj"));
	}

	@MethodSource("countArgumentsProvider")
	@ParameterizedTest
	void testCountArguments(String source, int expected) throws Exception {
		final var elist = findFirst(parseSource(source), TokenTypes.ELIST);
		assertEquals(expected, AstUtil.countArguments(elist));
	}

	@MethodSource("displayTextBinaryProvider")
	@ParameterizedTest
	void testDisplayTextBinary(String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a, int b) { var x = a " + op + " b; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals("a " + op + " b", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextCompoundFallback() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = new Object(); }");
		assertEquals(TokenTypes.LITERAL_NEW, node.getType(), "expected a non-switch compound node for this test to be meaningful");
		assertEquals(AstUtil.exprText(node), AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextDec() throws Exception {
		final var ast = parseSource("class T { void f(int a) { --a; } }");
		final var dec = findFirst(ast, TokenTypes.DEC);
		assertEquals("--a", AstUtil.displayText(dec));
	}

	@Test
	public void testDisplayTextDefault() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 42; } }");
		final var num = findFirst(ast, TokenTypes.NUM_INT);
		assertEquals("42", AstUtil.displayText(num));
	}

	@Test
	public void testDisplayTextDot() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = this.x; } }");
		assertEquals("this.x", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextInc() throws Exception {
		final var ast = parseSource("class T { void f(int a) { ++a; } }");
		final var inc = findFirst(ast, TokenTypes.INC);
		assertEquals("++a", AstUtil.displayText(inc));
	}

	@Test
	public void testDisplayTextIndexOp() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr) { int a = arr[0]; } }");
		assertEquals("arr[0]", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpNested() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[][] arr) { int a = arr[0][1]; } }");
		assertEquals("arr[0][1]", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpNestedInside() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr, int[] idx) { int a = arr[idx[0]]; } }");
		assertEquals("arr[idx[0]]", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpWithDot() throws Exception {
		final var node = parseExprFirstChild("class T { int[] x; void f() { int a = this.x[0]; } }");
		assertEquals("this.x[0]", AstUtil.displayText(node));
	}

	@MethodSource("displayTextMethodCallProvider")
	@ParameterizedTest
	void testDisplayTextMethodCall(String source, String expected) throws Exception {
		assertEquals(expected, AstUtil.displayText(parseExprFirstChild(source)));
	}

	@Test
	public void testDisplayTextPostDec() throws Exception {
		final var ast = parseSource("class T { void f(int a) { a--; } }");
		final var postDec = findFirst(ast, TokenTypes.POST_DEC);
		assertEquals("a--", AstUtil.displayText(postDec));
	}

	@Test
	public void testDisplayTextPostInc() throws Exception {
		final var ast = parseSource("class T { void f(int a) { a++; } }");
		final var postInc = findFirst(ast, TokenTypes.POST_INC);
		assertEquals("a++", AstUtil.displayText(postInc));
	}

	@MethodSource("displayTextPrefixUnaryProvider")
	@ParameterizedTest
	void testDisplayTextPrefixUnary(String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a) { var x = " + op + "a; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals(op + "a", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextQuestion() throws Exception {
		final var ast = parseSource("class T { int f(boolean c, int a, int b) { return c ? a : b; } }");
		final var node = findFirst(ast, TokenTypes.QUESTION);
		assertEquals("c ? a : b", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextQuestionNested() throws Exception {
		final var ast = parseSource("class T { int f(boolean c, boolean d, int a, int b, int e) { return c ? a : d ? b : e; } }");
		final var node = findFirst(ast, TokenTypes.QUESTION);
		assertEquals("c ? a : d ? b : e", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextTypecast() throws Exception {
		final var ast = parseSource("class T { void f(Object o) { var x = (String) o; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(String) o", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastGeneric() throws Exception {
		final var ast = parseSource("import java.util.List; class T { void f(Object o) { var x = (List<String>) o; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(List<String>) o", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastNested() throws Exception {
		final var ast = parseSource("class T { void f(int x) { var y = (long) (int) x; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(long) (int) x", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastParenthesizedOperand() throws Exception {
		final var ast = parseSource("class T { void f(Object o) { var x = (String) (o); } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(String) (o)", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextUnaryMinus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = -a; } }");
		assertEquals("-a", AstUtil.displayText(node));
	}

	@Test
	public void testDisplayTextUnaryPlus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = +a; } }");
		assertEquals("+a", AstUtil.displayText(node));
	}

	@MethodSource("dottedNameProvider")
	@ParameterizedTest
	void testDottedName(String source, String expected) throws Exception {
		final var ast = parseSource(source);
		final var dot = findFirst(ast, TokenTypes.DOT);
		assertEquals(expected, AstUtil.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionContext() throws Exception {
		final var ast = parseSource("class T { Object a; void f() { var x = a.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var dot = methodCall.getFirstChild();
		assertEquals("a.toString", AstUtil.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionIndexOp() throws Exception {
		final var ast = parseSource("class T { Object[] a; void f() { var x = a[0].toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var dot = methodCall.getFirstChild();
		assertEquals("[.toString", AstUtil.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionLiteralThis() throws Exception {
		final var ast = parseSource("class T { int a; void f() { var x = this.a; } }");
		final var dot = findFirst(ast, TokenTypes.DOT);
		assertEquals("this.a", AstUtil.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionNestedChain() throws Exception {
		final var ast = parseSource("class T { String a; void f() { var x = a.toString().length(); } }");
		final var outerCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var outerDot = outerCall.getFirstChild();
		assertEquals("(.length", AstUtil.dottedName(outerDot));
	}

	@Test
	public void testExprText() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		final var expr = assign.getFirstChild();
		assertEquals("String)obj", AstUtil.exprText(expr));
	}

	@Test
	public void testExprTextSimpleIdent() {
		final var method = findMethod(root, "castAndResolve");
		final var params = method.findFirstToken(TokenTypes.PARAMETERS);
		final var param = params.findFirstToken(TokenTypes.PARAMETER_DEF);
		final var ident = param.findFirstToken(TokenTypes.IDENT);
		assertEquals("obj", AstUtil.exprText(ident));
	}

	@Test
	public void testFindNewClassNameAnnotated() throws Exception {
		final var ast = parseSource("@interface Ann {}\nclass T { void f() { var x = new @Ann Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Object", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameAnonymousClass() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Thread() { @Override public void run() {} }; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Thread", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameBothTypeArgLevels() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { <U> T(U arg) {} void f() { var x = new <String>ArrayList<Object>(); } }"
		);
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("ArrayList", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameConstructorTypeArgsQualified() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>java.util.ArrayList<>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.ArrayList", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameConstructorTypeArgsSimple() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("T", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameDeeplyQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.concurrent.atomic.AtomicInteger(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.concurrent.atomic.AtomicInteger", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameInnerClass() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.AbstractMap.SimpleEntry<>(\"a\", \"b\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.AbstractMap.SimpleEntry", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNamePrimitiveArray() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new int[10]; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.lang.Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.lang.Object", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameQualifiedWithTypeArgs() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<String>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.ArrayList", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameReferenceArray() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new String[10]; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("String", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameSimple() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Object", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameSimpleWithTypeArgs() throws Exception {
		final var ast = parseSource("import java.util.ArrayList;\nclass T { void f() { var x = new ArrayList<String>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("ArrayList", AstUtil.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsConstructorLevelSkipped() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstUtil.findNewClassTypeArguments(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsConstructorLevelWithClassLevel() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { <U> T(U arg) {} void f() { var x = new <String>ArrayList<Object>(\"a\"); } }"
		);
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstUtil.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
		final var typeArg = typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT);
		final var ident = typeArg.findFirstToken(TokenTypes.IDENT);
		assertEquals("Object", ident.getText());
	}

	@Test
	public void testFindNewClassTypeArgumentsDiamond() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstUtil.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs == null || typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) == null);
	}

	@Test
	public void testFindNewClassTypeArgumentsNoTypeArgs() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstUtil.findNewClassTypeArguments(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsQualifiedName() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<Object>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstUtil.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
	}

	@Test
	public void testFindNewClassTypeArgumentsSimpleName() throws Exception {
		final var ast = parseSource("import java.util.ArrayList;\nclass T { void f() { var x = new ArrayList<Object>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		final var typeArgs = AstUtil.findNewClassTypeArguments(literalNew);
		assertTrue(typeArgs != null && typeArgs.findFirstToken(TokenTypes.TYPE_ARGUMENT) != null);
	}

	@Test
	public void testFindNodeAtAscendsFromNonRoot() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		final var deepChild = requireNonNull(findFirst(root, TokenTypes.SLIST));
		final var found = AstUtil.findNodeAt(
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
		final var found = AstUtil.findNodeAt(
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
		final var found = AstUtil.findNodeAt(
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
		assertNull(AstUtil.findNodeAt(
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
		assertNull(AstUtil.findNodeAt(root, 0, 0, n -> n.getType() == TokenTypes.LITERAL_TRY));
	}

	@Test
	public void testFindNodeAtDisambiguatesByPredicate() throws Exception {
		final var root = parseSource("class T { void f() { a(); } }");
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));
		final var expr = requireNonNull(findFirst(root, TokenTypes.EXPR));
		assertEquals(expr.getColumnNo(), call.getColumnNo(), "EXPR and its METHOD_CALL child must share a column for this test");
		final var found = AstUtil.findNodeAt(
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
		assertNull(AstUtil.findNodeAt(
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
		assertNull(AstUtil.findNodeAt(
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
		assertNull(AstUtil.findNodeAt(
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
		final var found = AstUtil.findNodeAt(
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
		final var found = AstUtil.findNodeAt(
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
		assertEquals(assign.getFirstChild().getColumnNo(), AstUtil.firstColumn(expr));
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
		assertEquals(prefix.length(), AstUtil.firstColumn(expr));
	}

	@Test
	public void testFirstColumnFirstLineFromDescendant() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f(int x) {\n\t\tx\n\t\t\t\t= 5;\n\t}\n}");
		final var assign = requireNonNull(findFirst(ast, TokenTypes.ASSIGN));
		final var expr = assign.getParent();
		assertEquals(4, expr.getLineNo(), "EXPR must sit on the '=' line for this test to be meaningful");
		assertEquals(2, AstUtil.firstColumn(expr));
	}

	@Test
	public void testFirstColumnIgnoresEarlierColumnsOnLaterLines() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n1\n\t\t);\n\t}\n\tvoid g(int a) {}\n}");
		final var call = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(2, AstUtil.firstColumn(call));
	}

	@Test
	public void testFirstLineAnnotationOnEarlierLine() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated\n\tint x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstUtil.firstLine(varDef));
	}

	@Test
	public void testFirstLineAnnotationSameLine() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated int x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstUtil.firstLine(varDef));
	}

	@Test
	public void testFirstLineDeepTree() throws Exception {
		final var sb = new StringBuilder("class T { int f() { return 0");
		for (var i = 0; i < 500; ++i)
			sb.append("\n\t\t\t+ ").append(i);
		sb.append("; } }");
		final var ast = parseSource(sb.toString());
		final var exprStart = requireNonNull(findFirst(ast, TokenTypes.LITERAL_RETURN));
		assertEquals(1, AstUtil.firstLine(exprStart));
	}

	@Test
	public void testFirstLineGrandchildEarliest() throws Exception {
		final var ast = parseSource("@Deprecated\nclass T {\n\tvoid f() {}\n}");
		final var classDef = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		assertEquals(1, AstUtil.firstLine(classDef));
	}

	@Test
	public void testFirstLineMultiLineMethodCall() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n\t\t\t1,\n\t\t\t2\n\t\t);\n\t}\n\tvoid g(int a, int b) {}\n}");
		final var methodCall = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(3, AstUtil.firstLine(methodCall));
	}

	@Test
	public void testFirstLineMultipleStackedAnnotations() throws Exception {
		final var ast = parseSource("class T {\n\t@Deprecated\n\t@SuppressWarnings(\"x\")\n\tint x;\n}");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(2, AstUtil.firstLine(varDef));
	}

	@Test
	public void testFirstLineSameLine() throws Exception {
		final var ast = parseSource("class T { int x; }");
		final var varDef = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF));
		assertEquals(1, AstUtil.firstLine(varDef));
	}

	@Test
	public void testFirstLineSingleLeaf() throws Exception {
		final var ast = parseSource("class T { int x; }");
		final var ident = requireNonNull(findFirst(ast, TokenTypes.IDENT));
		assertEquals(ident.getLineNo(), AstUtil.firstLine(ident));
	}

	@Test
	public void testGetEnclosingTypeNameAnnotationType() throws Exception {
		final var ast = parseSource("@interface Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstUtil.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameAnonymousClass() throws Exception {
		final var ast = parseSource("class T { Object o = new Object() {}; }");
		final var anonBlock = findFirst(ast, TokenTypes.LITERAL_NEW).findFirstToken(TokenTypes.OBJBLOCK);
		assertNull(AstUtil.getEnclosingTypeName(anonBlock));
	}

	@Test
	public void testGetEnclosingTypeNameClass() throws Exception {
		final var ast = parseSource("class Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstUtil.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameEnum() throws Exception {
		final var ast = parseSource("enum Foo { A }");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstUtil.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameInterface() throws Exception {
		final var ast = parseSource("interface Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstUtil.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameRecord() throws Exception {
		final var ast = parseSource("record Foo(int x) {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstUtil.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetMethodNameBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("foo", AstUtil.getMethodName(methodCall));
	}

	@Test
	public void testGetMethodNameDottedCall() throws Exception {
		final var ast = parseSource("class T { void f(String s) { s.trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("trim", AstUtil.getMethodName(methodCall));
	}

	@Test
	public void testGetPackageNameDefaultPackage() throws Exception {
		final var ast = parseSource("class T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertNull(AstUtil.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameDotted() throws Exception {
		final var ast = parseSource("package a.b.c;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("a.b.c", AstUtil.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameSingleSegment() throws Exception {
		final var ast = parseSource("package foo;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("foo", AstUtil.getPackageName(objBlock));
	}

	@Test
	public void testGetReceiverTypeNameBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameChainedCall() throws Exception {
		final var ast = parseSource("class T { String foo() { return \"\"; } void f() { foo().trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameFieldReceiver() throws Exception {
		final var ast = parseSource("class T { String str = \"hello\"; void f() { str.length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameFullyQualifiedStatic() throws Exception {
		final var ast = parseSource("class T { void f() { java.lang.Math.max(1, 2); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameLocalVariable() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; str.length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameNewExpressionReceiver() throws Exception {
		final var ast = parseSource("class T { void f() { new String(\"x\").trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameStaticCall() throws Exception {
		final var ast = parseSource("class T { void f() { String.valueOf(0); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameSuperCall() throws Exception {
		final var ast = parseSource("class T { void f() { super.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameThisCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { this.foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameUnknownVariable() throws Exception {
		final var ast = parseSource("class T { void f() { unknown.foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameUppercaseVariable() throws Exception {
		final var ast = parseSource("class T { void f() { Object Foo = new Object(); Foo.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("Foo", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameVariable() throws Exception {
		final var ast = parseSource("import java.util.List; class T { void f(List list) { list.size(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("List", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameVarReceiver() throws Exception {
		final var ast = parseSource("class T { void f() { var sb = new StringBuilder(); sb.append(\"x\"); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("StringBuilder", AstUtil.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsBareInnerCall() throws Exception {
		final var ast = parseSource("class T { Object requireView() { return null; } void f() { requireView().toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainMethodNotFound() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; str.fakeMethod().other(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainResolved() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; var x = str.trim().length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.lang.String", AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainUsesImports() throws Exception {
		final var ast = parseSource("class T { void f() { ArrayList list = null; list.stream().count(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.util.stream.Stream", AstUtil.getReceiverTypeName(methodCall, null, Set.of("java.util.ArrayList")));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsDeepChain() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; var x = str.trim().substring(0).length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.lang.String", AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsDelegatesToSimple() throws Exception {
		final var ast = parseSource("class T { void f() { String.valueOf(0); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsNonMethodReceiver() throws Exception {
		final var ast = parseSource("class T { void f(Object[] arr) { arr[0].toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsPackageResolution() throws Exception {
		final var ast = parseSource("class T { void f() { ArrayList list = null; list.iterator().next(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.util.Iterator", AstUtil.getReceiverTypeName(methodCall, "java.util", Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsUnresolvableType() throws Exception {
		final var ast = parseSource("class T { void f() { Xyz custom = null; custom.method().other(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@MethodSource("hasSuppressWarningsProvider")
	@ParameterizedTest
	void testHasSuppressWarnings(String source, String key, boolean expected) throws Exception {
		final var ast = parseSource(source);
		final var classDef = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		final var modifiers = classDef.findFirstToken(TokenTypes.MODIFIERS);
		assertEquals(expected, AstUtil.hasSuppressWarnings(modifiers, key));
	}

	@ParameterizedTest
	@ValueSource(ints = {TokenTypes.EQUAL, TokenTypes.NOT_EQUAL, TokenTypes.LE, TokenTypes.GE,
			TokenTypes.METHOD_CALL, TokenTypes.INC, TokenTypes.DEC, TokenTypes.POST_INC, TokenTypes.POST_DEC,
			TokenTypes.LAND, TokenTypes.LITERAL_NEW, TokenTypes.IDENT})
	void testIsAssignmentOperatorFalse(int tokenType) {
		assertFalse(AstUtil.isAssignmentOperator(tokenType));
	}

	@ParameterizedTest
	@ValueSource(ints = {TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN,
			TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN, TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN,
			TokenTypes.STAR_ASSIGN})
	void testIsAssignmentOperatorTrue(int tokenType) {
		assertTrue(AstUtil.isAssignmentOperator(tokenType));
	}

	@Test
	public void testIsEmptyBodyBlock() {
		final var method = findMethod(root, "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertTrue(AstUtil.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyDefaultToken() throws Exception {
		final var ast = parseSource("class T { void f() { int x = 1; } }");
		final var varDef = findFirst(ast, TokenTypes.VARIABLE_DEF);
		assertFalse(AstUtil.isEmptyBody(varDef));
	}

	@Test
	public void testIsEmptyBodyNonEmpty() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertFalse(AstUtil.isEmptyBody(slist));
	}

	@Test
	public void testIsEmptyBodyStatement() {
		final var method = findMethod(root, "emptyStatement");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var ifNode = slist.findFirstToken(TokenTypes.LITERAL_IF);
		final var rparen = ifNode.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen.getNextSibling();
		assertTrue(AstUtil.isEmptyBody(body));
	}

	@Test
	public void testIsPureDotChainOrIdentBareIdent() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = x; } }");
		assertTrue(AstUtil.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentClassLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = String.class; }");
		assertFalse(AstUtil.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentDeepChain() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a.b.c.d; }");
		assertTrue(AstUtil.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentNonDotNonIdent() throws Exception {
		final var node = parseExprFirstChild("class T { void g() {} void f() { Object a = g(); } }");
		assertFalse(AstUtil.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureDotChainOrIdentSimpleDot() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = a.b; }");
		assertTrue(AstUtil.isPureDotChainOrIdent(node));
	}

	@Test
	public void testIsPureExpressionAssign() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x = 1; } }");
		final var assign = findFirst(ast, TokenTypes.ASSIGN);
		assertFalse(AstUtil.isPureExpression(assign));
	}

	@Test
	public void testIsPureExpressionCharLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { char a = 'x'; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionDot() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = this.x; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionIdent() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = 1; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionIndexOp() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr) { int a = arr[0]; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionLiteralFalse() throws Exception {
		final var ast = parseSource("class T { void f() { var x = false; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_FALSE);
		assertTrue(AstUtil.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionLiteralNull() throws Exception {
		final var ast = parseSource("class T { void f() { Object x = null; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_NULL);
		assertTrue(AstUtil.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionLiteralTrue() throws Exception {
		final var ast = parseSource("class T { void f() { var x = true; } }");
		final var lit = findFirst(ast, TokenTypes.LITERAL_TRUE);
		assertTrue(AstUtil.isPureExpression(lit));
	}

	@Test
	public void testIsPureExpressionMethodCall() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = foo(); } int foo() { return 0; } }");
		assertFalse(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNewObject() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { Object a = new Object(); } }");
		assertFalse(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNumDouble() throws Exception {
		final var ast = parseSource("class T { void f() { double x = 1.0d; } }");
		final var num = findFirst(ast, TokenTypes.NUM_DOUBLE);
		assertTrue(AstUtil.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionNumFloat() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 1.0f; } }");
		final var num = findFirst(ast, TokenTypes.NUM_FLOAT);
		assertTrue(AstUtil.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionNumInt() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { int a = 42; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionNumLong() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 1L; } }");
		final var num = findFirst(ast, TokenTypes.NUM_LONG);
		assertTrue(AstUtil.isPureExpression(num));
	}

	@Test
	public void testIsPureExpressionPostDecrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x--; } }");
		final var postDec = findFirst(ast, TokenTypes.POST_DEC);
		assertFalse(AstUtil.isPureExpression(postDec));
	}

	@Test
	public void testIsPureExpressionPostIncrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { x++; } }");
		final var postInc = findFirst(ast, TokenTypes.POST_INC);
		assertFalse(AstUtil.isPureExpression(postInc));
	}

	@Test
	public void testIsPureExpressionPreDecrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { --x; } }");
		final var preDec = findFirst(ast, TokenTypes.DEC);
		assertFalse(AstUtil.isPureExpression(preDec));
	}

	@Test
	public void testIsPureExpressionPreIncrement() throws Exception {
		final var ast = parseSource("class T { void f(int x) { ++x; } }");
		final var preInc = findFirst(ast, TokenTypes.INC);
		assertFalse(AstUtil.isPureExpression(preInc));
	}

	@Test
	public void testIsPureExpressionStringLiteral() throws Exception {
		final var node = parseExprFirstChild("class T { void f() { String a = \"hello\"; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionThis() throws Exception {
		final var ast = parseSource("class T { void f() { Object a = this; } }");
		final var literalThis = findFirst(ast, TokenTypes.LITERAL_THIS);
		assertTrue(AstUtil.isPureExpression(literalThis));
	}

	@Test
	public void testIsPureExpressionUnaryMinus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = -a; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@Test
	public void testIsPureExpressionUnaryPlus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = +a; } }");
		assertTrue(AstUtil.isPureExpression(node));
	}

	@ParameterizedTest
	@ValueSource(strings = {"foo()", "a && foo()", "new boolean[]{foo()}[0]", "new Object() instanceof String", "++i > 0", "i++ > 0", "i-- > 0", "--i > 0", "flag = other", "flag &= other", "flag |= other", "flag ^= other", "(x += 1) > 0", "(x -= 1) > 0", "(x *= 2) > 0", "(x /= 2) > 0", "(x %= 2) > 0", "(x <<= 1) > 0", "(x >>= 1) > 0", "(x >>>= 1) > 0", "arr[i]++ > 0", "(new boolean[size()])[0]", "func(new boolean[]{a})", "(new String[]{s})[0].isEmpty()"})
	void testIsSideEffectFreeFalse(String cond) throws Exception {
		final var ast = parseSource("class T { void f(int i, int x, int n, int[] arr, boolean a, boolean b, boolean c, boolean flag, boolean other, Object obj, String s) { if (" + cond + ") {} } }");
		assertFalse(AstUtil.isSideEffectFree(findFirst(ast, TokenTypes.EXPR)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"a", "a && b", "x > 0", "i % x > 0", "arr.length > 0", "(boolean) obj", "!flag", "(a || b) && c", "new boolean[]{a}[0]", "obj instanceof String p", "a ? b : c", "(new int[]{x})[0]++ > 0", "--(new int[]{x})[0] > 0", "++(new int[]{x})[0] > 0", "(new int[]{x})[0]-- > 0", "(new boolean[n])[0]"})
	void testIsSideEffectFreeTrue(String cond) throws Exception {
		final var ast = parseSource("class T { void f(int i, int x, int n, int[] arr, boolean a, boolean b, boolean c, boolean flag, boolean other, Object obj, String s) { if (" + cond + ") {} } }");
		assertTrue(AstUtil.isSideEffectFree(findFirst(ast, TokenTypes.EXPR)));
	}

	@ParameterizedTest
	@ValueSource(strings = {"1", "1L", "1.0", "1.0f", "0x1", "0b1", ".1", "0.0e1"})
	void testIsZeroLiteralFalse(String literal) throws Exception {
		assertFalse(isZeroLiteral(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {"-0", "-0L", "-0.0", "-0.0f", "-0f", "-0.0d", "-0.", "-.0", "-0x0",
			"-0X0", "-0x0L", "-0b0", "-0B0", "-0b0L", "-0_0", "-0.0e0", "-0.0e+0", "-0.0e-0"})
	void testIsZeroLiteralNegativeZero(String literal) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + literal + "; } }");
		final var unaryMinus = findFirst(ast, TokenTypes.UNARY_MINUS);
		assertFalse(AstUtil.isZeroLiteral(unaryMinus));
		for (var type : new int[]{TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG}) {
			final var num = findFirst(unaryMinus, type);
			if (num != null) {
				assertTrue(AstUtil.isZeroLiteral(num));
				return;
			}
		}
		throw new AssertionError("No numeric literal found in: " + literal);
	}

	@Test
	public void testIsZeroLiteralNonNumericToken() throws Exception {
		final var ast = parseSource("class T { void f() { var x = true; } }");
		final var literalTrue = findFirst(ast, TokenTypes.LITERAL_TRUE);
		assertFalse(AstUtil.isZeroLiteral(literalTrue));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "0L", "0.0", "0.0f", "0f", "0.0d", "0.", ".0", "0x0", "0X0",
			"0x0L", "0b0", "0B0", "0b0L", "0_0", "0.0e0", "0.0e+0", "0.0e-0"})
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
		assertEquals(501, AstUtil.lastLine(ret));
	}

	@Test
	public void testLastLineMultiLine() {
		final var method = findMethod(root, "multiLine");
		assertEquals(42, AstUtil.lastLine(method));
	}

	@Test
	public void testLastLineMultiLineAnnotation() throws Exception {
		final var ast = parseSource("class T {\n\t@SuppressWarnings(\n\t\t\t\"x\"\n\t)\n\tint y;\n}");
		final var annotation = requireNonNull(findFirst(ast, TokenTypes.ANNOTATION));
		assertEquals(4, AstUtil.lastLine(annotation));
	}

	@Test
	public void testLastLineMultiLineMethodCall() throws Exception {
		final var ast = parseSource("class T {\n\tvoid f() {\n\t\tg(\n\t\t\t1,\n\t\t\t2\n\t\t);\n\t}\n\tvoid g(int a, int b) {}\n}");
		final var methodCall = requireNonNull(findFirst(ast, TokenTypes.METHOD_CALL));
		assertEquals(6, AstUtil.lastLine(methodCall));
	}

	@Test
	public void testLastLineNestedClass() throws Exception {
		final var ast = parseSource("class T {\n\tclass U {\n\t\tint a;\n\t\tint b;\n\t}\n}");
		final var outer = requireNonNull(findFirst(ast, TokenTypes.CLASS_DEF));
		assertEquals(6, AstUtil.lastLine(outer));
	}

	@Test
	public void testLastLineSameLineTie() throws Exception {
		final var ast = parseSource("class T { void f() { int x; } }");
		final var method = requireNonNull(findFirst(ast, TokenTypes.METHOD_DEF));
		assertEquals(1, AstUtil.lastLine(method));
	}

	@Test
	public void testLastLineSingleLine() {
		final var method = findMethod(root, "emptyBlock");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var rcurly = slist.findFirstToken(TokenTypes.RCURLY);
		assertEquals(rcurly.getLineNo(), AstUtil.lastLine(slist));
	}

	@Test
	public void testResolveVariableTypeConstructorParameter() {
		final var objBlock = findFirst(root, TokenTypes.OBJBLOCK);
		final var ctor = findFirst(objBlock, TokenTypes.CTOR_DEF);
		final var slist = ctor.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstUtil.resolveVariableType(slist, "ctorParam"));
	}

	@Test
	public void testResolveVariableTypeExplicitDeepQualifiedArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.concurrent.atomic.AtomicInteger[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.concurrent.atomic.AtomicInteger[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitGenericArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List<String>[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitGenericMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List<String>[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitPrimitiveArray() throws Exception {
		final var ast = parseSource("class T { void f() { int[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayDeclarationProvider")
	@ParameterizedTest
	void testResolveVariableTypeExplicitPrimitiveArrayTypes(String type) throws Exception {
		final var ast = parseSource("class T { void f() { " + type + "[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitPrimitiveMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { int[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedStringArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.lang.String[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitStringArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitTripleDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[][][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeField() {
		final var method = findMethod(root, "emptyBlock");
		assertEquals("java.util.List", AstUtil.resolveVariableType(method, "qualifiedField"));
	}

	@Test
	public void testResolveVariableTypeLocalVariable() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var exprNode = slist.getFirstChild().getNextSibling();
		assertEquals("String", AstUtil.resolveVariableType(exprNode, "s"));
	}

	@Test
	public void testResolveVariableTypeParameter() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Object", AstUtil.resolveVariableType(slist, "obj"));
	}

	@Test
	public void testResolveVariableTypePrimitive() {
		final var method = findMethod(root, "primitiveLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveExplicitTypeProvider")
	@ParameterizedTest
	void testResolveVariableTypePrimitiveTypes(String declaration) throws Exception {
		final var ast = parseSource("class T { void f() { " + declaration + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeUnknown() {
		final var method = findMethod(root, "castAndResolve");
		assertNull(AstUtil.resolveVariableType(method, "nonexistent"));
	}

	@Test
	public void testResolveVariableTypeVar() {
		final var method = findMethod(root, "varLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarAnonymousClass() {
		final var method = findMethod(root, "varAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Thread", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarCharLiteral() {
		final var method = findMethod(root, "varCharLiteralLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarGenericAnonymousClass() {
		final var method = findMethod(root, "varGenericAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("ArrayList", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarLambdaParameter() {
		final var method = findMethod(root, "varLambdaParameterLocal");
		final var methodCall = findFirst(method, TokenTypes.METHOD_CALL);
		assertNull(AstUtil.resolveVariableType(methodCall, "s"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallDottedReceiver() {
		final var method = findMethod(root, "varMethodCallInitDottedReceiver");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallInit() {
		final var method = findMethod(root, "varMethodCallInitLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Object", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallOverloadDisambiguated() {
		final var method = findMethod(root, "varMethodCallOverloadAmbiguousLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallOverloadZeroArg() {
		final var method = findMethod(root, "varMethodCallOverloadZeroArgLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArray() {
		final var method = findMethod(root, "varNewArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArrayInitializer() {
		final var method = findMethod(root, "varNewArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArrayInitializerRef() {
		final var method = findMethod(root, "varNewArrayInitializerRefLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewBothAnnotatedArray() {
		final var method = findMethod(root, "varNewBothAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgs() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("T", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgsBothLevels() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { void f() { var x = new <String>ArrayList<Object>(); } }"
		);
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("ArrayList", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgsQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new <String>java.util.ArrayList<>(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.ArrayList", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDeeplyQualified() {
		final var method = findMethod(root, "varNewDeeplyQualifiedLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.concurrent.atomic.AtomicInteger", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDimAnnotatedArray() {
		final var method = findMethod(root, "varNewDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDimAnnotatedMultiDimArray() {
		final var method = findMethod(root, "varNewDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewGeneric() {
		final var method = findMethod(root, "varNewGenericLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.HashMap", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewMultiDimArray() {
		final var method = findMethod(root, "varNewMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayInitializerProvider")
	@ParameterizedTest
	void testResolveVariableTypeVarNewPrimitiveArrayInitializerTypes(String type, String expr) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + expr + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayProvider")
	@ParameterizedTest
	void testResolveVariableTypeVarNewPrimitiveArrayTypes(String type, String expr) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + expr + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveBothAnnotatedArray() {
		final var method = findMethod(root, "varNewPrimitiveBothAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveDimAnnotatedArray() {
		final var method = findMethod(root, "varNewPrimitiveDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveDimAnnotatedMultiDimArray() {
		final var method = findMethod(root, "varNewPrimitiveDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveMultiDimArray() {
		final var method = findMethod(root, "varNewPrimitiveMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveMultiDimArrayInitializer() {
		final var method = findMethod(root, "varNewPrimitiveMultiDimArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveSizedArray() {
		final var method = findMethod(root, "varNewPrimitiveSizedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveTypeAnnotatedArray() {
		final var method = findMethod(root, "varNewPrimitiveTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualified() {
		final var method = findMethod(root, "varNewQualifiedLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.Object", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedAnonymousClass() {
		final var method = findMethod(root, "varNewQualifiedAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.Thread", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedArray() {
		final var method = findMethod(root, "varNewQualifiedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedArrayInitializer() {
		final var method = findMethod(root, "varNewQualifiedArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDiamond() {
		final var method = findMethod(root, "varNewQualifiedDiamondLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.HashMap", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDimAnnotatedArray() {
		final var method = findMethod(root, "varNewQualifiedDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDimAnnotatedMultiDimArray() {
		final var method = findMethod(root, "varNewQualifiedDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedGenericAnonymousClass() {
		final var method = findMethod(root, "varNewQualifiedGenericAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.ArrayList", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedInnerClass() {
		final var method = findMethod(root, "varNewQualifiedInnerClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.AbstractMap.SimpleEntry", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedMultiDimArray() {
		final var method = findMethod(root, "varNewQualifiedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[][]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedTypeAnnotatedArray() {
		final var method = findMethod(root, "varNewQualifiedTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewSimple() {
		final var method = findMethod(root, "varNewLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("StringBuilder", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewTypeAnnotatedArray() {
		final var method = findMethod(root, "varNewTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNullLiteral() {
		final var method = findMethod(root, "varNullLiteralLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testSameFileClassDefAndTypeParameterCount() throws Exception {
		final var source = "class T { static class Box<V> {} enum Kind { A } interface Face"
				+ " { class Deep<Y> {} } record Rec<A, B>() {} static class Holder { static class Mid"
				+ " { static class Leaf<X> {} } static class Plain {} } void m() { Object x; } }";
		final var ast = parseSource(source);
		final var scope = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF), "no declaration");

		assertEquals(1, typeParameterCountOf(scope, "Box"));
		assertEquals(0, typeParameterCountOf(scope, "Kind"));
		assertEquals(2, typeParameterCountOf(scope, "Rec"));
		assertEquals(1, typeParameterCountOf(scope, "Face.Deep"));
		assertEquals(0, typeParameterCountOf(scope, "Holder.Plain"));
		assertEquals(1, typeParameterCountOf(scope, "Holder.Mid.Leaf"));

		assertNull(AstUtil.sameFileClassDef(scope, "Absent"));
		assertNull(AstUtil.sameFileClassDef(scope, "Holder.Missing"));
		assertNull(AstUtil.sameFileClassDef(scope, "Holder.Mid.Missing"));
		assertNull(AstUtil.sameFileClassDef(scope, "."));
		assertSame(AstUtil.sameFileClassDef(scope, "Box"), AstUtil.sameFileClassDef(scope, "Box"));
	}

	@MethodSource("simpleNameProvider")
	@ParameterizedTest
	void testSimpleName(String input, String expected) {
		assertEquals(expected, AstUtil.simpleName(input));
	}

	@Test
	public void testSingleExpressionStatementBody() throws Exception {
		final var exprBody = findFirst(parseSource("class C { Runnable r = () -> { foo(); }; }"), TokenTypes.SLIST);
		assertEquals(TokenTypes.EXPR, AstUtil.singleExpressionStatementBody(exprBody).getType());
		final var returnBody = findFirst(parseSource("class C { Runnable r = () -> { return; }; }"), TokenTypes.SLIST);
		assertNull(AstUtil.singleExpressionStatementBody(returnBody));
		final var multiBody = findFirst(parseSource("class C { Runnable r = () -> { a(); b(); }; }"), TokenTypes.SLIST);
		assertNull(AstUtil.singleExpressionStatementBody(multiBody));
	}

	@Test
	public void testTypeTextPrimitive() {
		final var method = findMethod(root, "primitiveLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("", AstUtil.typeText(type));
	}

	@Test
	public void testTypeTextQualified() {
		final var objBlock = findFirst(root, TokenTypes.OBJBLOCK);
		var varDef = objBlock.findFirstToken(TokenTypes.VARIABLE_DEF);
		// skip to qualifiedField (4th VARIABLE_DEF: noAnnotationField, primitiveField, field, qualifiedField)
		for (var i = 0; i < 3; ++i) {
			do varDef = varDef.getNextSibling();
			while (varDef != null && varDef.getType() != TokenTypes.VARIABLE_DEF);
		}
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("javautilList", AstUtil.typeText(type));
	}

	@Test
	public void testTypeTextSimple() {
		final var method = findMethod(root, "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("String", AstUtil.typeText(type));
	}

	@Test
	public void testUnwrapParensAndExprExprWrapper() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = a; }"), TokenTypes.ASSIGN);
		final var unwrapped = AstUtil.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprFromEndNestedParens() throws Exception {
		final var land = findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && (((b))); } }"), TokenTypes.LAND);
		final var unwrapped = AstUtil.unwrapParensAndExprFromEnd(requireNonNull(land).getLastChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("b", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprFromEndNonWrapper() throws Exception {
		final var operand = requireNonNull(findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && b; } }"), TokenTypes.LAND)).getLastChild();
		assertSame(operand, AstUtil.unwrapParensAndExprFromEnd(operand));
	}

	@Test
	public void testUnwrapParensAndExprFromEndNull() {
		assertNull(AstUtil.unwrapParensAndExprFromEnd(null));
	}

	@Test
	public void testUnwrapParensAndExprFromEndParenOperand() throws Exception {
		final var land = findFirst(parseSource("class T { boolean f(boolean a, boolean b) { return a && (b); } }"), TokenTypes.LAND);
		final var unwrapped = AstUtil.unwrapParensAndExprFromEnd(requireNonNull(land).getLastChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("b", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprNestedParens() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = (((a))); }"), TokenTypes.ASSIGN);
		final var unwrapped = AstUtil.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@Test
	public void testUnwrapParensAndExprNonWrapper() throws Exception {
		final var ident = findFirst(parseSource("class T { Object x = a; }"), TokenTypes.IDENT);
		assertEquals(ident, AstUtil.unwrapParensAndExpr(ident));
	}

	@Test
	public void testUnwrapParensAndExprNull() {
		assertNull(AstUtil.unwrapParensAndExpr(null));
	}

	@Test
	public void testUnwrapParensAndExprParen() throws Exception {
		final var assign = findFirst(parseSource("class T { Object x = (a); }"), TokenTypes.ASSIGN);
		final var unwrapped = AstUtil.unwrapParensAndExpr(requireNonNull(assign).getFirstChild());
		assertEquals(TokenTypes.IDENT, requireNonNull(unwrapped).getType());
		assertEquals("a", unwrapped.getText());
	}

	@Test
	public void testUnwrapSingleStatementBlockEmpty() throws Exception {
		final var ast = parseSource("class T { void f() {} }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.unwrapSingleStatementBlock(slist));
	}

	@Test
	public void testUnwrapSingleStatementBlockMultiStatement() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); g(); } }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.unwrapSingleStatementBlock(slist));
	}

	@Test
	public void testUnwrapSingleStatementBlockNonSlist() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); } }");
		final var expr = findFirst(findMethod(ast, "f"), TokenTypes.EXPR);
		assertEquals(expr, AstUtil.unwrapSingleStatementBlock(expr));
	}

	@Test
	public void testUnwrapSingleStatementBlockSingleStatement() throws Exception {
		final var ast = parseSource("class T { void g() {} void f() { g(); } }");
		final var slist = findMethod(ast, "f").findFirstToken(TokenTypes.SLIST);
		final var unwrapped = AstUtil.unwrapSingleStatementBlock(slist);
		assertEquals(TokenTypes.EXPR, requireNonNull(unwrapped).getType());
	}
}