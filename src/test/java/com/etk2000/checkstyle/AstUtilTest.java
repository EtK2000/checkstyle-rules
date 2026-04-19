package com.etk2000.checkstyle;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

	private static void assertDisplayBinary(@Nonnull String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a, int b) { var x = a " + op + " b; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals("a " + op + " b", AstUtil.displayText(node));
	}

	private static void assertDisplayPrefixUnary(@Nonnull String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a) { var x = " + op + "a; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals(op + "a", AstUtil.displayText(node));
	}

	private static void assertNegativeZero(@Nonnull String literal) throws Exception {
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

	static Stream<Arguments> collectParameterTypesProvider() {
		return Stream.of(
				Arguments.of("class T { T() {} }", TokenTypes.CTOR_DEF, List.of()),
				Arguments.of("class T { T(int x) {} }", TokenTypes.CTOR_DEF, List.of("int")),
				Arguments.of("class T { T(String a, int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { void f(String a, int b) {} }", TokenTypes.METHOD_DEF, List.of("String", "int")),
				Arguments.of("class T { T(@Deprecated String a, @Deprecated int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { T(@Deprecated String a, int b) {} }", TokenTypes.CTOR_DEF, List.of("String", "int")),
				Arguments.of("class T { void f(@Deprecated String a, @Deprecated int[] b) {} }", TokenTypes.METHOD_DEF, List.of("String", "int[]"))
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
				Arguments.of("class T { @Deprecated a.b.C<@Deprecated String> x; }", "a.b.C")
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

	@BeforeAll
	public static void setUp() throws Exception {
		root = parse("astutil/InputAstUtil.java");
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

	@MethodSource("displayTextBinaryProvider")
	@ParameterizedTest
	void testDisplayTextBinary(String op, int tokenType) throws Exception {
		assertDisplayBinary(op, tokenType);
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
		assertDisplayPrefixUnary(op, tokenType);
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
	public void testFindNewClassTypeArgumentsConstructorLevelSkipped() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		// constructor-level <String> should NOT be returned
		assertNull(AstUtil.findNewClassTypeArguments(literalNew));
	}

	@Test
	public void testFindNewClassTypeArgumentsConstructorLevelWithClassLevel() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { <U> T(U arg) {} void f() { var x = new <String>ArrayList<Object>(\"a\"); } }"
		);
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		// should return the class-level <Object>, not the constructor-level <String>
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
		// diamond <> has TYPE_ARGUMENTS but no TYPE_ARGUMENT children
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
	@ValueSource(strings = {"1", "1L", "1.0", "1.0f", "0x1", "0b1", ".1", "0.0e1"})
	void testIsZeroLiteralFalse(String literal) throws Exception {
		assertFalse(isZeroLiteral(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {"-0", "-0L", "-0.0", "-0.0f", "-0f", "-0.0d", "-0.", "-.0", "-0x0",
			"-0X0", "-0x0L", "-0b0", "-0B0", "-0b0L", "-0_0", "-0.0e0", "-0.0e+0", "-0.0e-0"})
	void testIsZeroLiteralNegativeZero(String literal) throws Exception {
		assertNegativeZero(literal);
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
	public void testLastLineMultiLine() {
		final var method = findMethod(root, "multiLine");
		assertTrue(AstUtil.lastLine(method) > method.getLineNo());
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

	@Test
	public void testResolveVariableTypeUnknown() {
		final var method = findMethod(root, "castAndResolve");
		assertNull(AstUtil.resolveVariableType(method, "nonexistent"));
	}

	@Test
	public void testResolveVariableTypeVar() {
		final var method = findMethod(root, "varLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarAnonymousClass() {
		final var method = findMethod(root, "varAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArray() {
		final var method = findMethod(root, "varNewArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArrayInitializer() {
		// `var x = new int[]{...}` also produces an ARRAY_DECLARATOR child; the guard
		// must bail on this form too, not just `new X[N]`.
		final var method = findMethod(root, "varNewArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewGeneric() {
		// Qualified constructors use a DOT subtree rather than a bare IDENT child,
		// so the simple-name inference bails. Documented limitation.
		final var method = findMethod(root, "varNewGenericLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstUtil.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewSimple() {
		final var method = findMethod(root, "varNewLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("StringBuilder", AstUtil.resolveVariableType(slist.getLastChild(), "x"));
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
		// skip to qualifiedField (4th VARIABLE_DEF: field, noAnnotationField, primitiveField, qualifiedField)
		for (var i = 0; i < 3; ++i) {
			varDef = varDef.getNextSibling();
			while (varDef != null && varDef.getType() != TokenTypes.VARIABLE_DEF)
				varDef = varDef.getNextSibling();
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
}