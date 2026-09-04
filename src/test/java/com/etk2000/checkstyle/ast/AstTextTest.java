package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.assertSameWithAndWithoutComments;
import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.findMethod;
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

import java.util.stream.Stream;

public class AstTextTest {
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

	static Stream<Arguments> commentTokenProvider() {
		return Stream.of(
				Arguments.of(TokenTypes.BLOCK_COMMENT_BEGIN),
				Arguments.of(TokenTypes.BLOCK_COMMENT_END),
				Arguments.of(TokenTypes.COMMENT_CONTENT),
				Arguments.of(TokenTypes.SINGLE_LINE_COMMENT)
		);
	}

	static Stream<Arguments> dottedNameCommentProvider() {
		return Stream.of(
				Arguments.of("class T { a.b.C x; }", "a.b.C"),
				Arguments.of("class T { /*c*/ a.b.C x; }", "a.b.C"),
				Arguments.of("class T { a./*c*/b.C x; }", "a.b.C"),
				Arguments.of("class T { a.b./*c*/C x; }", "a.b.C"),
				Arguments.of("class T {\n// c\na.b.C x; }", "a.b.C"),
				Arguments.of("class T { a.\n// c\nb.C x; }", "a.b.C"),
				Arguments.of("class T { /*c*/ Outer<String>.Inner x; }", "Outer.Inner")
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

	static Stream<Arguments> exprTextCommentProvider() {
		return Stream.of(
				Arguments.of("class T { Object f(Object a) { return a.b; } }", "ab"),
				Arguments.of("class T { Object f(Object a) { return a./*c*/b; } }", "ab"),
				Arguments.of("class T { Object f(Object a) { return a.\n// c\nb; } }", "ab"),
				Arguments.of("class T { Object f(Object a) { return /*c*/ a.b; } }", "ab"),
				Arguments.of("class T { int f(int x, int y) { return x + /* c */ y; } }", "xy"),
				Arguments.of("class T { int f(int x, int y) { return x /* c */ + y; } }", "xy"),
				Arguments.of("class T { int f(int x, int y) { return x // c\n+ y; } }", "xy"),
				Arguments.of("class T { int f(int[] arr, int i) { return arr[/*c*/ i]; } }", "arri]"),
				Arguments.of("class T { int f(int[] arr, int i) { return arr[i /*c*/]; } }", "arri]"),
				Arguments.of("class T { int f(int a, int b) { return (a /*c*/ > b) ? a : b; } }", "(ab)a:b"),
				Arguments.of("class T { int g(int x) { return g(/*c*/ x); } }", "gx)"),
				Arguments.of("class T { Object f(Object obj) { return (String) /*c*/ obj; } }", "String)obj")
		);
	}

	static Stream<Arguments> getPackageNameCommentProvider() {
		return Stream.of(
				Arguments.of("package a.b;\nclass T {}", "a.b"),
				Arguments.of("package /*p*/ a.b;\nclass T {}", "a.b"),
				Arguments.of("package a./*p*/b;\nclass T {}", "a.b"),
				Arguments.of("package // p\na.b;\nclass T {}", "a.b"),
				Arguments.of("package /*p*/ a;\nclass T {}", "a"),
				Arguments.of("package // p\na;\nclass T {}", "a"),
				Arguments.of("package a.b.c.d;\nclass T {}", "a.b.c.d"),
				Arguments.of("/*header*/\npackage a.b;\nclass T {}", "a.b")
		);
	}

	static Stream<Arguments> nonCommentTokenProvider() {
		return Stream.of(
				Arguments.of(TokenTypes.DOT),
				Arguments.of(TokenTypes.EXPR),
				Arguments.of(TokenTypes.IDENT),
				Arguments.of(TokenTypes.RCURLY),
				Arguments.of(TokenTypes.SLIST),
				Arguments.of(TokenTypes.STRING_LITERAL)
		);
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

	static Stream<Arguments> typeTextCommentProvider() {
		return Stream.of(
				Arguments.of("class T { java.util.List f; }", "javautilList"),
				Arguments.of("class T { java./*c*/util.List f; }", "javautilList"),
				Arguments.of("class T { /*lead*/ java.util.List f; }", "javautilList"),
				Arguments.of("class T { java.util./*c*/List f; }", "javautilList"),
				Arguments.of("class T {\n// lead\njava.util.List f; }", "javautilList"),
				Arguments.of("class T { /*lead*/ String f; }", "String")
		);
	}

	@Test
	public void testAnnotationNameQualified() {
		final var classAnnotation = findFirst(root(), TokenTypes.ANNOTATION);
		assertEquals("CheckReturnValue", AstText.annotationName(classAnnotation));
	}

	@Test
	public void testAnnotationNameSimple() {
		final var objBlock = findFirst(root(), TokenTypes.OBJBLOCK);
		final var fieldAnnotation = findFirst(objBlock, TokenTypes.ANNOTATION);
		// the fixture's `@Nonnull int field` is redundant by this project's own convention, so it
		// reads as removable; it is the only unqualified annotation in the file. The fixture cannot
		// say so itself: it is parsed WITH_COMMENTS, and a trailing comment there attaches into the
		// following declaration's subtree and corrupts testTypeTextQualified.
		assertEquals(
				"field",
				fieldAnnotation.getParent().getParent().findFirstToken(TokenTypes.IDENT).getText(),
				"expected the annotation on 'field' for this test to be meaningful"
		);
		assertEquals("Nonnull", AstText.annotationName(fieldAnnotation));
	}

	@Test
	public void testCanonicalAnnotationEmptyParens() throws Exception {
		final var ast = parseSource("@Deprecated() class T {}");
		final var annotation = findFirst(ast, TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstText.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationExplicitValue() throws Exception {
		final var ast = parseSource("class T { @SuppressWarnings(value = \"unused\") int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("SuppressWarnings(value=\"unused\")", AstText.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationMarker() throws Exception {
		final var ast = parseSource("class T { @Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstText.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationMaxDepthZero() throws Exception {
		final var ast = parseSource("class T { @Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("", AstText.canonicalAnnotation(annotation, 0));
	}

	@Test
	public void testCanonicalAnnotationMultiParam() throws Exception {
		final var ast = parseSource("@interface M { int b() default 0; int a() default 0; }\nclass T { @M(b = 2, a = 1) int x; }");
		final var varDef = findFirst(ast, TokenTypes.VARIABLE_DEF);
		final var annotation = findFirst(varDef, TokenTypes.ANNOTATION);
		assertEquals("M(a=1,b=2)", AstText.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationQualified() throws Exception {
		final var ast = parseSource("class T { @java.lang.Deprecated int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("Deprecated", AstText.canonicalAnnotation(annotation, 50));
	}

	@Test
	public void testCanonicalAnnotationSingleValue() throws Exception {
		final var ast = parseSource("class T { @SuppressWarnings(\"unused\") int x; }");
		final var annotation = findFirst(findFirst(ast, TokenTypes.OBJBLOCK), TokenTypes.ANNOTATION);
		assertEquals("SuppressWarnings(value=\"unused\")", AstText.canonicalAnnotation(annotation, 50));
	}

	@ParameterizedTest
	@ValueSource(strings = {"boolean", "byte", "char", "double", "float", "int", "int[]",
			"int[][]", "java.util.List", "java.util.List[]", "long", "short", "String",
			"String[]"})
	void testCanonicalTypeAnnotatedField(String type) throws Exception {
		final var ast = parseSource("class T { @Deprecated " + type + " x; }");
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(type, AstText.canonicalType(typeNode));
	}

	@ParameterizedTest
	@ValueSource(strings = {"boolean", "byte", "char", "double", "float", "int", "int[]",
			"int[][]", "java.util.List", "java.util.List[]", "long", "short", "String",
			"String[]"})
	void testCanonicalTypeField(String type) throws Exception {
		final var ast = parseSource("class T { " + type + " x; }");
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(type, AstText.canonicalType(typeNode));
	}

	@MethodSource("canonicalTypeGenericProvider")
	@ParameterizedTest
	void testCanonicalTypeGeneric(String source, String expected) throws Exception {
		final var ast = parseSource(source);
		final var typeNode = findFirst(ast, TokenTypes.VARIABLE_DEF).findFirstToken(TokenTypes.TYPE);
		assertEquals(expected, AstText.canonicalType(typeNode));
	}

	@Test
	public void testCanonicalTypeVoid() throws Exception {
		final var ast = parseSource("class T { void f() {} }");
		final var method = findFirst(ast, TokenTypes.METHOD_DEF);
		final var type = method.findFirstToken(TokenTypes.TYPE);
		assertEquals("void", AstText.canonicalType(type));
	}

	@MethodSource("dottedNameProvider")
	@ParameterizedTest
	void testDottedName(String source, String expected) throws Exception {
		final var ast = parseSource(source);
		final var dot = findFirst(ast, TokenTypes.DOT);
		assertEquals(expected, AstText.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionContext() throws Exception {
		final var ast = parseSource("class T { Object a; void f() { var x = a.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var dot = methodCall.getFirstChild();
		assertEquals("a.toString", AstText.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionIndexOp() throws Exception {
		final var ast = parseSource("class T { Object[] a; void f() { var x = a[0].toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var dot = methodCall.getFirstChild();
		assertEquals("[.toString", AstText.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionLiteralThis() throws Exception {
		final var ast = parseSource("class T { int a; void f() { var x = this.a; } }");
		final var dot = findFirst(ast, TokenTypes.DOT);
		assertEquals("this.a", AstText.dottedName(dot));
	}

	@Test
	public void testDottedNameExpressionNestedChain() throws Exception {
		final var ast = parseSource("class T { String a; void f() { var x = a.toString().length(); } }");
		final var outerCall = findFirst(ast, TokenTypes.METHOD_CALL);
		final var outerDot = outerCall.getFirstChild();
		assertEquals("(.length", AstText.dottedName(outerDot));
	}

	@MethodSource("dottedNameCommentProvider")
	@ParameterizedTest
	void testDottedNameWithComments(String source, String expected) throws Exception {
		assertSameWithAndWithoutComments(
				source,
				expected,
				root -> AstText.dottedName(requireNonNull(findFirst(findFirst(root, TokenTypes.TYPE), TokenTypes.DOT)))
		);
	}

	@Test
	public void testExprText() {
		final var method = findMethod(root(), "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		final var expr = assign.getFirstChild();
		assertEquals("String)obj", AstText.exprText(expr));
	}

	@Test
	public void testExprTextCommentNodeAlone() throws Exception {
		final var block = parseSourceWithComments("class T { Object f(Object a) { return a./*c*/b; } }");
		assertEquals("", AstText.exprText(requireNonNull(findFirst(block, TokenTypes.BLOCK_COMMENT_BEGIN))));

		final var line = parseSourceWithComments("class T {\n// c\nvoid f() {}\n}");
		assertEquals("", AstText.exprText(requireNonNull(findFirst(line, TokenTypes.SINGLE_LINE_COMMENT))));
	}

	@Test
	public void testExprTextSimpleIdent() {
		final var method = findMethod(root(), "castAndResolve");
		final var params = method.findFirstToken(TokenTypes.PARAMETERS);
		final var paramDef = params.findFirstToken(TokenTypes.PARAMETER_DEF);
		final var ident = paramDef.findFirstToken(TokenTypes.IDENT);
		assertEquals("obj", AstText.exprText(ident));
	}

	@MethodSource("exprTextCommentProvider")
	@ParameterizedTest
	void testExprTextWithComments(String source, String expected) throws Exception {
		assertSameWithAndWithoutComments(
				source,
				expected,
				root -> AstText.exprText(requireNonNull(findFirst(findFirst(root, TokenTypes.LITERAL_RETURN), TokenTypes.EXPR)))
		);
	}

	@Test
	public void testFindNewClassNameAnnotated() throws Exception {
		final var ast = parseSource("@interface Ann {}\nclass T { void f() { var x = new @Ann Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Object", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameAnonymousClass() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Thread() { @Override public void run() {} }; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Thread", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameBothTypeArgLevels() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { <U> T(U arg) {} void f() { var x = new <String>ArrayList<Object>(); } }"
		);
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("ArrayList", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameConstructorTypeArgsQualified() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>java.util.ArrayList<>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.ArrayList", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameConstructorTypeArgsSimple() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("T", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameDeeplyQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.concurrent.atomic.AtomicInteger(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.concurrent.atomic.AtomicInteger", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameInnerClass() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.AbstractMap.SimpleEntry<>(\"a\", \"b\"); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.AbstractMap.SimpleEntry", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNamePrimitiveArray() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new int[10]; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertNull(AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.lang.Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.lang.Object", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameQualifiedWithTypeArgs() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new java.util.ArrayList<String>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("java.util.ArrayList", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameReferenceArray() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new String[10]; } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("String", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameSimple() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new Object(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("Object", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFindNewClassNameSimpleWithTypeArgs() throws Exception {
		final var ast = parseSource("import java.util.ArrayList;\nclass T { void f() { var x = new ArrayList<String>(); } }");
		final var literalNew = requireNonNull(findFirst(ast, TokenTypes.LITERAL_NEW));
		assertEquals("ArrayList", AstText.findNewClassName(literalNew));
	}

	@Test
	public void testFirstRealChildNullWhenEveryChildIsAComment() throws Exception {
		final var ast = parseSourceWithComments("class T { public /*c*/ static int x; }");
		final var comment = requireNonNull(findFirst(ast, TokenTypes.BLOCK_COMMENT_BEGIN));
		assertEquals(2, comment.getChildCount(), "a block comment carries COMMENT_CONTENT and BLOCK_COMMENT_END");
		assertNull(AstText.firstRealChild(comment));
	}

	@Test
	public void testFirstRealChildSkipsALeadingComment() throws Exception {
		final var ast = parseSourceWithComments("class T { Object f(Object o) { return /*c*/ o.b; } }");
		final var dot = requireNonNull(findFirst(ast, TokenTypes.DOT));
		assertEquals(TokenTypes.BLOCK_COMMENT_BEGIN, dot.getFirstChild().getType(), "the comment must really be in the way");
		final var first = requireNonNull(AstText.firstRealChild(dot));
		assertEquals(TokenTypes.IDENT, first.getType());
		assertEquals("o", first.getText());
	}

	@Test
	public void testFirstRealChildWithoutChildren() throws Exception {
		final var ast = parseSource("class T { Object f(Object o) { return o.b; } }");
		assertNull(AstText.firstRealChild(requireNonNull(findFirst(ast, TokenTypes.IDENT))));
	}

	@Test
	public void testFirstRealChildWithoutComments() throws Exception {
		final var ast = parseSource("class T { Object f(Object o) { return o.b; } }");
		final var dot = requireNonNull(findFirst(ast, TokenTypes.DOT));
		assertSame(dot.getFirstChild(), AstText.firstRealChild(dot));
	}

	@Test
	public void testGetEnclosingTypeNameAnnotationType() throws Exception {
		final var ast = parseSource("@interface Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstText.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameAnonymousClass() throws Exception {
		final var ast = parseSource("class T { Object o = new Object() {}; }");
		final var anonBlock = findFirst(ast, TokenTypes.LITERAL_NEW).findFirstToken(TokenTypes.OBJBLOCK);
		assertNull(AstText.getEnclosingTypeName(anonBlock));
	}

	@Test
	public void testGetEnclosingTypeNameClass() throws Exception {
		final var ast = parseSource("class Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstText.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameEnum() throws Exception {
		final var ast = parseSource("enum Foo { A }");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstText.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameInterface() throws Exception {
		final var ast = parseSource("interface Foo {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstText.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetEnclosingTypeNameRecord() throws Exception {
		final var ast = parseSource("record Foo(int x) {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("Foo", AstText.getEnclosingTypeName(objBlock));
	}

	@Test
	public void testGetPackageNameAnnotatedDeclaration() throws Exception {
		final var ast = parseSource("@SuppressWarnings(\"x\")\npackage a.b;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("a.b", AstText.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameDefaultPackage() throws Exception {
		final var ast = parseSource("class T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertNull(AstText.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameDotted() throws Exception {
		final var ast = parseSource("package a.b.c;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("a.b.c", AstText.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameOnAnnotatedDeclarationNode() throws Exception {
		final var ast = parseSource("@java.lang.SuppressWarnings(\"x\")\npackage a.b;\nclass T {}");
		final var declaration = findFirst(ast, TokenTypes.PACKAGE_DEF);
		assertEquals("a.b", AstText.getPackageName(declaration));
	}

	@Test
	public void testGetPackageNameOnDeclarationNode() throws Exception {
		final var ast = parseSource("package a.b;\nclass T {}");
		final var declaration = findFirst(ast, TokenTypes.PACKAGE_DEF);
		assertEquals("a.b", AstText.getPackageName(declaration));
	}

	@Test
	public void testGetPackageNameQualifiedAnnotationDeclaration() throws Exception {
		final var ast = parseSource("@java.lang.SuppressWarnings(\"x\")\npackage a.b;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("a.b", AstText.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameSingleSegment() throws Exception {
		final var ast = parseSource("package foo;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("foo", AstText.getPackageName(objBlock));
	}

	@Test
	public void testGetPackageNameSingleSegmentQualifiedAnnotation() throws Exception {
		final var ast = parseSource("@java.lang.SuppressWarnings(\"x\")\npackage foo;\nclass T {}");
		final var objBlock = findFirst(ast, TokenTypes.OBJBLOCK);
		assertEquals("foo", AstText.getPackageName(objBlock));
	}

	@MethodSource("getPackageNameCommentProvider")
	@ParameterizedTest
	void testGetPackageNameWithComments(String source, String expected) throws Exception {
		assertSameWithAndWithoutComments(source, expected, AstText::getPackageName);
	}

	@MethodSource("commentTokenProvider")
	@ParameterizedTest
	void testIsCommentTokenAcceptsEveryCommentToken(int tokenType) {
		assertTrue(AstText.isCommentToken(tokenType));
	}

	@MethodSource("nonCommentTokenProvider")
	@ParameterizedTest
	void testIsCommentTokenRejectsRealTokens(int tokenType) {
		assertFalse(AstText.isCommentToken(tokenType));
	}

	@Test
	public void testLastIdentClassLiteral() throws Exception {
		final var ast = parseSource("class T { Object f() { return String.class; } }");
		assertNull(AstText.lastIdent(requireNonNull(findFirst(ast, TokenTypes.DOT))));
	}

	@Test
	public void testLastIdentDottedChain() throws Exception {
		final var ast = parseSource("class T { void f() { a.b.c(); } }");
		assertEquals("c", AstText.lastIdent(requireNonNull(findFirst(ast, TokenTypes.DOT))));
	}

	@Test
	public void testLastIdentQualifiedGenericTypeArguments() throws Exception {
		final var ast = parseSource("class T { java.util.List<String> f() { return null; } }");
		final var type = requireNonNull(findFirst(ast, TokenTypes.TYPE));
		assertNull(AstText.lastIdent(requireNonNull(findFirst(type, TokenTypes.DOT))));
	}

	@Test
	public void testLastIdentQualifiedSuper() throws Exception {
		final var ast = parseSource("class T { class Inner { String f() { return T.super.toString(); } } }");
		final var outer = requireNonNull(findFirst(ast, TokenTypes.DOT));
		assertNull(AstText.lastIdent(requireNonNull(outer.getFirstChild())));
	}

	@Test
	public void testNextRealSiblingNullForNullNode() {
		assertNull(AstText.nextRealSibling(null));
	}

	@Test
	public void testNextRealSiblingNullWhenNothingFollows() throws Exception {
		final var ast = parseSourceWithComments("class T { void m(Object o) { o.p(\"k\" /*c*/, 1); } }");
		final var elist = requireNonNull(findFirst(ast, TokenTypes.ELIST));
		var last = elist.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		assertEquals(TokenTypes.EXPR, last.getType(), "a comment is never a parent's last child; it is inserted before a real token");
		assertNull(AstText.nextRealSibling(last));
	}

	@Test
	public void testNextRealSiblingSkipsAdjacentComments() throws Exception {
		final var ast = parseSourceWithComments("class T { Object f(Object a) { return a./*c*//*d*/b; } }");
		final var dot = requireNonNull(findFirst(ast, TokenTypes.DOT));
		final var left = dot.getFirstChild();
		assertEquals(4, dot.getChildCount(), "two IDENTs and two comment nodes");
		final var right = requireNonNull(AstText.nextRealSibling(left));
		assertEquals("b", right.getText());
	}

	@Test
	public void testNextRealSiblingSkipsAnInterveningComment() throws Exception {
		final var ast = parseSourceWithComments("class T { Object f(Object a) { return a./*c*/b; } }");
		final var dot = requireNonNull(findFirst(ast, TokenTypes.DOT));
		final var left = dot.getFirstChild();
		assertEquals(TokenTypes.BLOCK_COMMENT_BEGIN, left.getNextSibling().getType(), "the comment must really be in the way");
		final var right = requireNonNull(AstText.nextRealSibling(left));
		assertEquals(TokenTypes.IDENT, right.getType());
		assertEquals("b", right.getText());
	}

	@Test
	public void testNextRealSiblingWithoutComments() throws Exception {
		final var ast = parseSource("class T { Object f(Object a) { return a.b; } }");
		final var left = requireNonNull(findFirst(ast, TokenTypes.DOT)).getFirstChild();
		assertSame(left.getNextSibling(), AstText.nextRealSibling(left));
	}

	@MethodSource("simpleNameProvider")
	@ParameterizedTest
	void testSimpleName(String input, String expected) {
		assertEquals(expected, AstText.simpleName(input));
	}

	@Test
	public void testTypeTextPrimitive() {
		final var method = findMethod(root(), "primitiveLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("", AstText.typeText(type));
	}

	@Test
	public void testTypeTextQualified() {
		final var objBlock = findFirst(root(), TokenTypes.OBJBLOCK);
		var varDef = objBlock.findFirstToken(TokenTypes.VARIABLE_DEF);
		// skip to qualifiedField (4th VARIABLE_DEF: noAnnotationField, primitiveField, field, qualifiedField)
		for (var i = 0; i < 3; ++i) {
			do varDef = varDef.getNextSibling();
			while (varDef != null && varDef.getType() != TokenTypes.VARIABLE_DEF);
		}
		assertEquals(
				"qualifiedField",
				varDef.findFirstToken(TokenTypes.IDENT).getText(),
				"the sibling walk must land on qualifiedField for this test to be meaningful"
		);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("javautilList", AstText.typeText(type));
	}

	@Test
	public void testTypeTextSimple() {
		final var method = findMethod(root(), "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var varDef = slist.findFirstToken(TokenTypes.VARIABLE_DEF);
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		assertEquals("String", AstText.typeText(type));
	}

	@MethodSource("typeTextCommentProvider")
	@ParameterizedTest
	void testTypeTextWithComments(String source, String expected) throws Exception {
		assertSameWithAndWithoutComments(
				source,
				expected,
				root -> AstText.typeText(requireNonNull(findFirst(root, TokenTypes.TYPE)))
		);
	}
}