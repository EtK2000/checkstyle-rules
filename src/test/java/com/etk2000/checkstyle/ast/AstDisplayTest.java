package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.assertSameWithAndWithoutComments;
import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseExprFirstChild;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSource;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSourceWithComments;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class AstDisplayTest {
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

	static Stream<Arguments> displayTextCommentProvider() {
		return Stream.of(
				Arguments.of("class T { Object f(Object a) { return a.b; } }", "a.b"),
				Arguments.of("class T { Object f(Object a) { return a./*c*/b; } }", "a.b"),
				Arguments.of("class T { Object f(Object a) { return a.\n// c\nb; } }", "a.b"),
				Arguments.of("class T { Object f(Object a) { return /*c*/ a.b; } }", "a.b"),
				Arguments.of("class T { int f(int x, int y) { return x + /* c */ y; } }", "x + y"),
				Arguments.of("class T { int f(int x, int y) { return x /* c */ + y; } }", "x + y"),
				Arguments.of("class T { int f(int x, int y) { return x // c\n+ y; } }", "x + y"),
				Arguments.of("class T { int f(int[] arr, int i) { return arr[/*c*/ i]; } }", "arr[i]"),
				Arguments.of("class T { int g(int x) { return g(/*c*/ x); } }", "g(x)"),
				Arguments.of("class T { int g(int x, int y) { return g(x /*c*/, y); } }", "g(x, y)"),
				Arguments.of("class T { Object f(Object obj) { return (String) /*c*/ obj; } }", "(String) obj"),
				Arguments.of("class T { Object f(Object obj) { return (/*c*/ String) obj; } }", "(String) obj"),
				Arguments.of("class T { int f(int a, int b) { return a /*c*/ > b ? a : b; } }", "a > b ? a : b"),
				Arguments.of("class T { int f(int a, int b) { return a > b ? /*c*/ a : b; } }", "a > b ? a : b"),
				Arguments.of("class T { int f(int a, int b) { return a > b ? a : /*c*/ b; } }", "a > b ? a : b"),
				Arguments.of("class T { boolean f(boolean a) { return !/*c*/a; } }", "!a"),
				Arguments.of("class T { int f(int a) { return -/*c*/a; } }", "-a")
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

	@MethodSource("displayTextBinaryProvider")
	@ParameterizedTest
	void testDisplayTextBinary(String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a, int b) { var x = a " + op + " b; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals("a " + op + " b", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextChildlessNumericLeaf() throws Exception {
		final var ast = parseSource("class T { void f() { var x = 42; } }");
		final var num = findFirst(ast, TokenTypes.NUM_INT);
		assertEquals("42", AstDisplay.displayText(num));
	}

	@Test
	public void testDisplayTextCommentNodeAlone() throws Exception {
		final var ast = parseSourceWithComments("class T { Object f(Object a) { return a./*c*/b; } }");
		assertEquals("", AstDisplay.displayText(requireNonNull(findFirst(ast, TokenTypes.BLOCK_COMMENT_BEGIN))));
	}

	@MethodSource("displayTextCommentProvider")
	@ParameterizedTest
	void testDisplayTextCommentPositions(String source, String expected) throws Exception {
		assertSameWithAndWithoutComments(
				source,
				expected,
				root -> AstDisplay.displayText(requireNonNull(findFirst(findFirst(root, TokenTypes.LITERAL_RETURN), TokenTypes.EXPR)))
		);
	}

	@Test
	public void testDisplayTextCompoundFallback() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = new Object(); }");
		assertEquals(TokenTypes.LITERAL_NEW, node.getType(), "expected a non-switch compound node for this test to be meaningful");
		assertEquals(AstText.exprText(node), AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextCompoundFallbackInstanceOf() throws Exception {
		final var node = parseExprFirstChild("class T { Object o; boolean x = o instanceof String; }");
		assertEquals(TokenTypes.LITERAL_INSTANCEOF, node.getType(), "expected a non-switch compound node for this test to be meaningful");
		assertEquals(AstText.exprText(node), AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextCompoundFallbackLambda() throws Exception {
		final var node = parseExprFirstChild("class T { Runnable x = () -> {}; }");
		assertEquals(TokenTypes.LAMBDA, node.getType(), "expected a non-switch compound node for this test to be meaningful");
		assertEquals(AstText.exprText(node), AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextCompoundFallbackMethodRef() throws Exception {
		final var node = parseExprFirstChild("class T { Object x = String::valueOf; }");
		assertEquals(TokenTypes.METHOD_REF, node.getType(), "expected a non-switch compound node for this test to be meaningful");
		assertEquals(AstText.exprText(node), AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextDec() throws Exception {
		final var ast = parseSource("class T { void f(int a) { --a; } }");
		final var dec = findFirst(ast, TokenTypes.DEC);
		assertEquals("--a", AstDisplay.displayText(dec));
	}

	@Test
	public void testDisplayTextDot() throws Exception {
		final var node = parseExprFirstChild("class T { int x; void f() { int a = this.x; } }");
		assertEquals("this.x", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextImportWildcardDot() throws Exception {
		final var ast = parseSource("import java.util.*;\nclass T {}");
		final var dot = findFirst(findFirst(ast, TokenTypes.IMPORT), TokenTypes.DOT);
		assertEquals("java.util.*", AstDisplay.displayText(dot));
	}

	@Test
	public void testDisplayTextImportWildcardStar() throws Exception {
		final var ast = parseSource("import java.util.*;\nclass T {}");
		assertEquals("*", AstDisplay.displayText(findFirst(ast, TokenTypes.STAR)));
	}

	@Test
	public void testDisplayTextInc() throws Exception {
		final var ast = parseSource("class T { void f(int a) { ++a; } }");
		final var inc = findFirst(ast, TokenTypes.INC);
		assertEquals("++a", AstDisplay.displayText(inc));
	}

	@Test
	public void testDisplayTextIndexOp() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr) { int a = arr[0]; } }");
		assertEquals("arr[0]", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpNested() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[][] arr) { int a = arr[0][1]; } }");
		assertEquals("arr[0][1]", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpNestedInside() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int[] arr, int[] idx) { int a = arr[idx[0]]; } }");
		assertEquals("arr[idx[0]]", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextIndexOpWithDot() throws Exception {
		final var node = parseExprFirstChild("class T { int[] x; void f() { int a = this.x[0]; } }");
		assertEquals("this.x[0]", AstDisplay.displayText(node));
	}

	@MethodSource("displayTextMethodCallProvider")
	@ParameterizedTest
	void testDisplayTextMethodCall(String source, String expected) throws Exception {
		assertEquals(expected, AstDisplay.displayText(parseExprFirstChild(source)));
	}

	@Test
	public void testDisplayTextPostDec() throws Exception {
		final var ast = parseSource("class T { void f(int a) { a--; } }");
		final var postDec = findFirst(ast, TokenTypes.POST_DEC);
		assertEquals("a--", AstDisplay.displayText(postDec));
	}

	@Test
	public void testDisplayTextPostInc() throws Exception {
		final var ast = parseSource("class T { void f(int a) { a++; } }");
		final var postInc = findFirst(ast, TokenTypes.POST_INC);
		assertEquals("a++", AstDisplay.displayText(postInc));
	}

	@MethodSource("displayTextPrefixUnaryProvider")
	@ParameterizedTest
	void testDisplayTextPrefixUnary(String op, int tokenType) throws Exception {
		final var ast = parseSource("class T { void f(int a) { var x = " + op + "a; } }");
		final var node = findFirst(ast, tokenType);
		assertEquals(op + "a", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextQuestion() throws Exception {
		final var ast = parseSource("class T { int f(boolean c, int a, int b) { return c ? a : b; } }");
		final var node = findFirst(ast, TokenTypes.QUESTION);
		assertEquals("c ? a : b", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextQuestionNested() throws Exception {
		final var ast = parseSource("class T { int f(boolean c, boolean d, int a, int b, int e) { return c ? a : d ? b : e; } }");
		final var node = findFirst(ast, TokenTypes.QUESTION);
		assertEquals("c ? a : d ? b : e", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextTypecast() throws Exception {
		final var ast = parseSource("class T { void f(Object o) { var x = (String) o; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(String) o", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastGeneric() throws Exception {
		final var ast = parseSource("import java.util.List; class T { void f(Object o) { var x = (List<String>) o; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(List<String>) o", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastNested() throws Exception {
		final var ast = parseSource("class T { void f(int x) { var y = (long) (int) x; } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(long) (int) x", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextTypecastParenthesizedOperand() throws Exception {
		final var ast = parseSource("class T { void f(Object o) { var x = (String) (o); } }");
		final var node = findFirst(ast, TokenTypes.TYPECAST);
		assertEquals("(String) (o)", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextUnaryMinus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = -a; } }");
		assertEquals("-a", AstDisplay.displayText(node));
	}

	@Test
	public void testDisplayTextUnaryPlus() throws Exception {
		final var node = parseExprFirstChild("class T { void f(int a) { int b = +a; } }");
		assertEquals("+a", AstDisplay.displayText(node));
	}
}