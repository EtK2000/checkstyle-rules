package com.etk2000.checkstyle.ast;

import static com.etk2000.checkstyle.ast.AstTestSupport.findFirst;
import static com.etk2000.checkstyle.ast.AstTestSupport.findMethod;
import static com.etk2000.checkstyle.ast.AstTestSupport.parseSource;
import static com.etk2000.checkstyle.ast.AstTestSupport.root;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AstResolveTest {
	static Stream<Arguments> expressionTypeNameProvider() {
		return Stream.of(
				Arguments.of("parameter", "void m(List<String> s) { probe(s); }", "List"),
				Arguments.of("local with explicit type", "void m() { final List<String> s = null; probe(s); }", "List"),
				Arguments.of("local inferred from diamond", "void m() { final var s = new ArrayList<String>(); probe(s); }", "ArrayList"),
				Arguments.of("object array parameter", "void m(String[] s) { probe(s); }", "String[]"),
				Arguments.of("primitive array parameter", "void m(int[] s) { probe(s); }", "int[]"),
				Arguments.of("two-dimensional array parameter", "void m(String[][] s) { probe(s); }", "String[][]"),
				Arguments.of("C-style array parameter", "void m(String s[]) { probe(s); }", "String[]"),
				Arguments.of("varargs parameter keeps its array-ness", "void m(String... s) { probe(s); }", "String[]"),
				Arguments.of("primitive varargs parameter", "void m(char... s) { probe(s); }", "char[]"),
				Arguments.of("varargs of an array type", "void m(String[]... s) { probe(s); }", "String[][]"),
				Arguments.of("array access strips one dimension", "void m(char[][] g) { probe(g[0]); }", "char[]"),
				Arguments.of("array access to a scalar", "void m(char[][] g) { probe(g[0][0]); }", "char"),
				Arguments.of("array access on a reference array", "void m(String[] s) { probe(s[0]); }", "String"),
				Arguments.of("array access with nothing left to strip", "void m(String s) { probe(s[0]); }", null),
				Arguments.of("for-each var over an array", "void m(char[][] g) { for (var r : g) probe(r); }", "char[]"),
				Arguments.of("for-each var over a collection", "void m(List<String> l) { for (var e : l) probe(e); }", null),
				Arguments.of("for-each explicit type over an array", "void m(char[][] g) { for (char[] r : g) probe(r); }", "char[]"),
				Arguments.of("a binding declared in a for-init", "void m() { for (char[] row = null; row != null; row = null) probe(row); }", "char[]"),
				Arguments.of("pattern variable", "void m(Object o) { if (o instanceof char[] a) probe(a); }", "char[]"),
				Arguments.of("pattern variable of a reference type", "void m(Object o) { if (o instanceof String a) probe(a); }", "String"),
				Arguments.of("a governing pattern outranks a shadowed field", "char[] chars; void m(Object o) { if (o instanceof String chars) probe(chars); }", "String"),
				Arguments.of("a negated pattern does not govern the then-branch", "char[] chars; void m(Object o) { if (!(o instanceof String chars)) probe(chars); }", "char[]"),
				Arguments.of("a pattern does not govern the else-branch", "char[] chars; void m(Object o) { if (o instanceof String chars) z(); else probe(chars); }", "char[]"),
				Arguments.of("var inferred from a call", "void m(String n) { final var c = n.toCharArray(); probe(c); }", "[C"),
				Arguments.of("var inferred from an array access", "void m(char[][] g) { final var r = g[0]; probe(r); }", "char[]"),
				Arguments.of("var with no initializer", "void m() { var s; probe(s); }", null),
				Arguments.of("non-collection parameter", "void m(Iterable<String> s) { probe(s); }", "Iterable"),
				Arguments.of("unresolvable type", "void m(Mystery s) { probe(s); }", "Mystery"),
				Arguments.of("catch parameter", "void m() { try { z(); } catch (RuntimeException e) { probe(e); } }", "RuntimeException"),
				Arguments.of("raw cast", "void m(Object o) { probe((List) o); }", "List"),
				Arguments.of("generic cast", "void m(Object o) { probe((List<String>) o); }", "List"),
				Arguments.of("parenthesized", "void m(List<String> s) { probe((s)); }", "List"),
				Arguments.of("doubly parenthesized", "void m(List<String> s) { probe(((s))); }", "List"),
				Arguments.of("ternary with matching branches", "void m(boolean c, List<String> a, List<String> b) { probe(c ? a : b); }", "List"),
				Arguments.of("ternary with divergent branches", "void m(boolean c, List<String> a, Iterable<String> b) { probe(c ? a : b); }", null),
				Arguments.of("nested ternary", "void m(boolean c, List<String> a, List<String> b, List<String> d) { probe(c ? (c ? a : b) : d); }", "List"),
				Arguments.of("ternary nested in the false branch", "void m(boolean c, List<String> a, List<String> b, List<String> d) { probe(c ? a : (c ? b : d)); }", "List"),
				Arguments.of("ternary with a parenthesized true branch", "void m(boolean c, List<String> a, List<String> b) { probe(c ? (a) : b); }", "List"),
				Arguments.of("ternary with a parenthesized false branch", "void m(boolean c, List<String> a, List<String> b) { probe(c ? a : (b)); }", "List"),
				Arguments.of("ternary with a parenthesized condition", "void m(int n, List<String> a, List<String> b) { probe((n > 0) ? a : b); }", "List"),
				Arguments.of("call on a resolvable receiver", "void m(Map<String, String> x) { probe(x.values()); }", "java.util.Collection"),
				Arguments.of("chained call", "void m(List<String> s) { probe(s.subList(0, 1).subList(0, 1)); }", "java.util.List"),
				Arguments.of("call returning an array", "void m(String s) { probe(s.split(\",\")); }", "[Ljava.lang.String;"),
				Arguments.of("bare same-file call", "List<String> mk() { return null; } void m() { probe(mk()); }", "List"),
				Arguments.of("bare same-file call with no such method", "void m() { probe(nope(1)); }", null),
				Arguments.of("fully qualified static call", "void m() { probe(java.time.DayOfWeek.values()); }", null),
				Arguments.of("record accessor by its bare name", "record R(char[] data) { void g() { probe(data()); } }", "char[]"),
				Arguments.of("record accessor through this", "record R(char[] data) { void g() { probe(this.data()); } }", "char[]"),
				Arguments.of("record accessor at a non-zero arity", "record R(char[] data) { void g() { probe(data(1)); } }", null),
				Arguments.of("an explicit accessor outranks the component", "record R(char[] data) { String data() { return null; } void g() { probe(data()); } }", "String"),
				Arguments.of("a varargs component is not re-dimensioned", "record R(char... cs) { void g() { probe(cs); } }", "char[]"),
				Arguments.of("a varargs component read through this", "record R(char... cs) { void g() { probe(this.cs); } }", "char[]"),
				Arguments.of("a varargs component read by its accessor", "record R(char... cs) { void g() { probe(cs()); } }", "char[]"),
				Arguments.of("a varargs component keeps its own declared dimension", "record R(char[]... rows) { void g() { probe(rows); } }", "char[][]"),
				Arguments.of("a varargs component's own dimension through this", "record R(char[]... rows) { void g() { probe(this.rows); } }", "char[][]"),
				Arguments.of("a varargs component's own dimension by accessor", "record R(char[]... rows) { void g() { probe(rows()); } }", "char[][]"),
				Arguments.of("an element of an array-typed varargs component", "record R(char[]... rows) { void g() { probe(rows[0]); } }", "char[]"),
				Arguments.of("a varargs parameter keeps its own declared dimension", "void m(char[]... rows) { probe(rows); }", "char[][]"),
				Arguments.of("a varargs component with a qualified element", "record R(java.util.List... xs) { void g() { probe(xs); } }", "java.util.List[]"),
				Arguments.of("a varargs component with a nested-type element", "static class Outer { static class Inner {} } record R(Outer.Inner... xs) { void g() { probe(xs); } }", "Outer.Inner[]"),
				Arguments.of("a varargs component with a generic element", "record R(java.util.List<String>... xs) { void g() { probe(xs); } }", "java.util.List[]"),
				Arguments.of("a varargs component with a type-variable element", "record R<E>(E... xs) { void g() { probe(xs); } }", "E[]"),
				Arguments.of("an element of a type-variable varargs component", "record R<E>(E... xs) { void g() { probe(xs[0]); } }", "E"),
				Arguments.of("an annotated dimension on a varargs component", "@interface A {} record R(char @A []... rows) { void g() { probe(rows); } }", "char[][]"),
				Arguments.of("an annotated dimension on an array parameter", "@interface A {} void m(char @A [] cs) { probe(cs); }", "char[]"),
				Arguments.of("a scalar primitive parameter names no type", "void m(char c) { probe(c); }", null),
				Arguments.of("a record component through a typed receiver", "record R(char[] data) {} void m(R r) { probe(r.data()); }", "char[]"),
				Arguments.of("a method the anonymous body itself declares", "char[] pick() { return null; } Object g() { return new Object() { void h() { probe(pick()); } }; }", "char[]"),
				Arguments.of("a method the anonymous body inherits", "static class Base { String pick() { return null; } } char[] pick() { return null; } Object g() { return new Base() { void h() { probe(this.pick()); } }; }", "String"),
				Arguments.of("an anonymous body with no such method falls through", "static class Base {} char[] pick() { return null; } Object g() { return new Base() { void h() { probe(pick()); } }; }", "char[]"),
				Arguments.of("super from a plain same-file subclass", "static class Base { char[] pick() { return null; } } static class Sub extends Base { void h() { probe(super.pick()); } }", "char[]"),
				Arguments.of("a method an enum constant body itself declares", "enum E { A { char[] pick() { return null; } void h() { probe(pick()); } }; String pick() { return null; } }", "char[]"),
				Arguments.of("super in an enum constant body", "enum E { A { void h() { probe(super.pick()); } }; char[] pick() { return null; } }", "char[]"),
				Arguments.of("a method inherited two links up", "static class Top { char[] pick() { return null; } } static class Mid extends Top {} static class Sub extends Mid { void h() { probe(pick()); } }", "char[]"),
				Arguments.of("a method inherited through implements", "interface Face { char[] pick(); } abstract static class Sub implements Face { void h() { probe(pick()); } }", "char[]"),
				Arguments.of("an interface constant inherited through implements", "interface Face { char[] CHARS = null; } static class Sub implements Face { void h() { probe(CHARS); } }", "char[]"),
				Arguments.of("a method on the second name of one clause", "interface A {} interface B { char[] pick(); } abstract static class Sub implements A, B { void h() { probe(pick()); } }", "char[]"),
				Arguments.of("a field inherited through the second of two clauses", "static class Base {} interface Face { char[] CHARS = null; } abstract static class Sub extends Base implements Face { void h() { probe(CHARS); } }", "char[]"),
				Arguments.of("a constant inherited from a same-file annotation type", "@interface Ann { char[] CHARS = {}; } abstract static class Sub implements Ann { void h() { probe(CHARS); } }", "char[]"),
				Arguments.of("a supertype that is a member type inherited from an interface", "interface Face { class Deep { char[] chars; } } static class Holder implements Face { static class Inner extends Deep { void h() { probe(chars); } } }", "char[]"),
				Arguments.of("a method lookup through an inheritance cycle terminates", "static class A extends B { void h() { probe(pick()); } } static class B extends A {}", null),
				Arguments.of("a field lookup through an inheritance cycle terminates", "static class A extends B { void h() { probe(chars); } } static class B extends A {}", null),
				Arguments.of("a for-each whose iterable the loop variable shadows terminates", "char[][] items; void m() { for (var items : items) probe(items); }", null),
				Arguments.of("Outer.this on a call is not resolved", "char[] pick() { return null; } class Inner { void h() { probe(T.this.pick()); } }", null),
				Arguments.of("Outer.this on a field is resolved", "char[] chars = null; class Inner { void h() { probe(T.this.chars); } }", "char[]"),
				Arguments.of("literal", "void m() { probe(1); }", null)
		);
	}

	@Nullable
	private static DetailAST findProbeCall(@Nonnull DetailAST node) {
		if (node.getType() == TokenTypes.METHOD_CALL) {
			final var callee = node.getFirstChild();
			if (callee != null && callee.getType() == TokenTypes.IDENT && "probe".equals(callee.getText()))
				return node;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var found = findProbeCall(child);
			if (found != null)
				return found;
		}
		return null;
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

	/**
	 * The type {@code AstResolve} gives the single argument of the {@code probe(...)} call in
	 * {@code classBody}, which is how each case marks the expression under test.
	 */
	@Nullable
	private static String typeOfProbeArgument(@Nonnull String classBody) throws Exception {
		final var root = parseSource(
				"import java.util.ArrayList; import java.util.List; import java.util.Map;\n"
						+ "class T { static void probe(Object o) {} static void z() {} " + classBody + " }"
		);

		final var probeCall = findProbeCall(requireNonNull(root));
		final var arguments = requireNonNull(probeCall).findFirstToken(TokenTypes.ELIST);
		return AstResolve.expressionTypeName(
				requireNonNull(arguments).getFirstChild(),
				null,
				Set.of("java.util.ArrayList", "java.util.List", "java.util.Map")
		);
	}

	private static int typeParameterCountOf(@Nonnull DetailAST scope, @Nonnull String className) {
		return AstQuery.typeParameterCount(
				requireNonNull(AstResolve.sameFileClassDef(scope, className), "no such type: " + className)
		);
	}

	@MethodSource("expressionTypeNameProvider")
	@ParameterizedTest(name = "{0}")
	public void testExpressionTypeName(
			@Nonnull String description,
			@Nonnull String classBody,
			@Nullable String expected
	) throws Exception {
		assertEquals(expected, typeOfProbeArgument(classBody));
	}

	@Test
	public void testExpressionTypeNameFieldShapes() throws Exception {
		assertEquals("List", typeOfProbeArgument("List<String> f; void m() { probe(f); }"), "bare field");
		assertEquals("List", typeOfProbeArgument("List<String> f; void m() { probe(this.f); }"), "this-qualified field");
		assertEquals("String[]", typeOfProbeArgument("static String[] A; void m() { probe(T.A); }"), "static field via type name");
		assertNull(typeOfProbeArgument("void m(String s) { probe(s.CASE_INSENSITIVE_ORDER); }"), "field on a classpath receiver");
	}

	@Test
	public void testExpressionTypeNameSameFileTypesAreRefused() throws Exception {
		assertNull(
				typeOfProbeArgument("enum E { A } void m() { probe(E.values()); }"),
				"a same-file enum's values() names a type the classpath cannot confirm"
		);
		assertEquals(
				"Bag",
				typeOfProbeArgument("static class Bag {} void m(Bag b) { probe(b); }"),
				"the name still resolves; refusing it is the caller's job"
		);
	}

	@Test
	public void testGetReceiverTypeNameBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameChainedCall() throws Exception {
		final var ast = parseSource("class T { String foo() { return \"\"; } void f() { foo().trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameFieldReceiver() throws Exception {
		final var ast = parseSource("class T { String str = \"hello\"; void f() { str.length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameFullyQualifiedStatic() throws Exception {
		final var ast = parseSource("class T { void f() { java.lang.Math.max(1, 2); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameLocalVariable() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; str.length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameNewExpressionReceiver() throws Exception {
		final var ast = parseSource("class T { void f() { new String(\"x\").trim(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameStaticCall() throws Exception {
		final var ast = parseSource("class T { void f() { String.valueOf(0); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameSuperCall() throws Exception {
		final var ast = parseSource("class T { void f() { super.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameThisCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { this.foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameUnknownVariable() throws Exception {
		final var ast = parseSource("class T { void f() { unknown.foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameUppercaseVariable() throws Exception {
		final var ast = parseSource("class T { void f() { Object Foo = new Object(); Foo.toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("Foo", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameVariable() throws Exception {
		final var ast = parseSource("import java.util.List; class T { void f(List list) { list.size(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("List", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameVarReceiver() throws Exception {
		final var ast = parseSource("class T { void f() { var sb = new StringBuilder(); sb.append(\"x\"); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("StringBuilder", AstResolve.getReceiverTypeName(methodCall));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsBareCall() throws Exception {
		final var ast = parseSource("class T { void foo() {} void f() { foo(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsBareInnerCall() throws Exception {
		final var ast = parseSource("class T { Object requireView() { return null; } void f() { requireView().toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainMethodNotFound() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; str.fakeMethod().other(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainResolved() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; var x = str.trim().length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.lang.String", AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsChainUsesImports() throws Exception {
		final var ast = parseSource("class T { void f() { ArrayList list = null; list.stream().count(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.util.stream.Stream", AstResolve.getReceiverTypeName(methodCall, null, Set.of("java.util.ArrayList")));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsDeepChain() throws Exception {
		final var ast = parseSource("class T { void f() { String str = \"hello\"; var x = str.trim().substring(0).length(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.lang.String", AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsDelegatesToSimple() throws Exception {
		final var ast = parseSource("class T { void f() { String.valueOf(0); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("String", AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsNonMethodReceiver() throws Exception {
		final var ast = parseSource("class T { void f(Object[] arr) { arr[0].toString(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsPackageResolution() throws Exception {
		final var ast = parseSource("class T { void f() { ArrayList list = null; list.iterator().next(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertEquals("java.util.Iterator", AstResolve.getReceiverTypeName(methodCall, "java.util", Set.of()));
	}

	@Test
	public void testGetReceiverTypeNameWithImportsUnresolvableType() throws Exception {
		final var ast = parseSource("class T { void f() { Xyz custom = null; custom.method().other(); } }");
		final var methodCall = findFirst(ast, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.getReceiverTypeName(methodCall, null, Set.of()));
	}

	@Test
	public void testResolveVariableTypeConstructorParameter() {
		final var objBlock = findFirst(root(), TokenTypes.OBJBLOCK);
		final var ctor = findFirst(objBlock, TokenTypes.CTOR_DEF);
		final var slist = ctor.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstResolve.resolveVariableType(slist, "ctorParam"));
	}

	@Test
	public void testResolveVariableTypeExplicitDeepQualifiedArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.concurrent.atomic.AtomicInteger[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.concurrent.atomic.AtomicInteger[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitGenericArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List<String>[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitGenericMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List<String>[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitPrimitiveArray() throws Exception {
		final var ast = parseSource("class T { void f() { int[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayDeclarationProvider")
	@ParameterizedTest
	void testResolveVariableTypeExplicitPrimitiveArrayTypes(String type) throws Exception {
		final var ast = parseSource("class T { void f() { " + type + "[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitPrimitiveMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { int[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedMultiDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.util.List[][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.List[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitQualifiedStringArray() throws Exception {
		final var ast = parseSource("class T { void f() { java.lang.String[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitStringArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeExplicitTripleDimArray() throws Exception {
		final var ast = parseSource("class T { void f() { String[][][] x = null; x.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeField() {
		final var method = findMethod(root(), "emptyBlock");
		assertEquals("java.util.List", AstResolve.resolveVariableType(method, "qualifiedField"));
	}

	@Test
	public void testResolveVariableTypeImplicitLambdaParametersBindWithoutType() throws Exception {
		final var ast = parseSource("class T { String a = \"\"; void f(java.util.Map<String, String> m) { m.forEach((a, b) -> a.length()); } }");
		final var lambda = findFirst(ast, TokenTypes.LAMBDA);
		assertNull(AstResolve.resolveVariableType(lambda.getLastChild(), "a"));
	}

	@Test
	public void testResolveVariableTypeLocalVariable() {
		final var method = findMethod(root(), "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		final var exprNode = slist.getFirstChild().getNextSibling();
		assertEquals("String", AstResolve.resolveVariableType(exprNode, "s"));
	}

	@Test
	public void testResolveVariableTypeNakedLambdaParameterBindsWithoutType() throws Exception {
		final var ast = parseSource("class T { String item = \"\"; void f(java.util.List<java.nio.file.Path> ps) { ps.forEach(item -> item.toString()); } }");
		final var lambda = findFirst(ast, TokenTypes.LAMBDA);
		assertNull(AstResolve.resolveVariableType(lambda.getLastChild(), "item"));
	}

	@Test
	public void testResolveVariableTypeParameter() {
		final var method = findMethod(root(), "castAndResolve");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Object", AstResolve.resolveVariableType(slist, "obj"));
	}

	@Test
	public void testResolveVariableTypePrimitive() {
		final var method = findMethod(root(), "primitiveLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveExplicitTypeProvider")
	@ParameterizedTest
	void testResolveVariableTypePrimitiveTypes(String declaration) throws Exception {
		final var ast = parseSource("class T { void f() { " + declaration + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeResourceExistingVariableReference() throws Exception {
		final var ast = parseSource("class T { java.io.InputStream s; void f() throws Exception { try (s) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.InputStream", AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeResourceInTryBody() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (java.io.ByteArrayInputStream s = null) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeResourceNotVisibleFromCatch() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (java.io.ByteArrayInputStream s = null) { s.read(); } catch (Exception e) { s.length(); } } }");
		final var caught = findFirst(ast, TokenTypes.LITERAL_CATCH).findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstResolve.resolveVariableType(caught, "s"));
	}

	@Test
	public void testResolveVariableTypeResourceNotVisibleFromFinally() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (java.io.ByteArrayInputStream s = null) { s.read(); } finally { s.length(); } } }");
		final var last = findFirst(ast, TokenTypes.LITERAL_FINALLY).findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstResolve.resolveVariableType(last, "s"));
	}

	@Test
	public void testResolveVariableTypeResourceSecondInList() throws Exception {
		final var ast = parseSource("class T { void f() throws Exception { try (java.io.ByteArrayInputStream a = null; java.io.CharArrayReader b = null) { a.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.CharArrayReader", AstResolve.resolveVariableType(body, "b"));
	}

	@Test
	public void testResolveVariableTypeResourceVisibleFromLaterResource() throws Exception {
		final var ast = parseSource("class T { String a = \"\"; void f() throws Exception { try (var a = new java.io.ByteArrayInputStream(new byte[0]); var b = a) { } } }");
		final var second = findFirst(ast, TokenTypes.RESOURCES).getLastChild();
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(second, "a"));
	}

	@Test
	public void testResolveVariableTypeResourceVisibleFromNestedTryBody() throws Exception {
		final var ast = parseSource("class T { String a = \"\"; void f() throws Exception { try (var a = new java.io.ByteArrayInputStream(new byte[0])) { try (var b = new java.io.CharArrayReader(new char[0])) { a.read(); } } } }");
		final var inner = findFirst(findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST), TokenTypes.LITERAL_TRY);
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(inner.findFirstToken(TokenTypes.SLIST), "a"));
	}

	@Test
	public void testResolveVariableTypeUninitializedPrimitiveBindsWithoutType() throws Exception {
		final var ast = parseSource("class T { String x = \"\"; void f() { int x; x = 1; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeUnknown() {
		final var method = findMethod(root(), "castAndResolve");
		assertNull(AstResolve.resolveVariableType(method, "nonexistent"));
	}

	@Test
	public void testResolveVariableTypeVar() {
		final var method = findMethod(root(), "varLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarAnonymousClass() {
		final var method = findMethod(root(), "varAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Thread", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarCharLiteral() {
		final var method = findMethod(root(), "varCharLiteralLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarForEachBindsWithoutType() throws Exception {
		final var ast = parseSource("class T { String name = \"\"; void f(java.util.List<java.nio.file.Path> ps) { for (var name : ps) name.toString(); } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_FOR).getLastChild();
		assertNull(AstResolve.resolveVariableType(body, "name"));
	}

	@Test
	public void testResolveVariableTypeVarGenericAnonymousClass() {
		final var method = findMethod(root(), "varGenericAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("ArrayList", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarLambdaParameter() {
		final var method = findMethod(root(), "varLambdaParameterLocal");
		final var methodCall = findFirst(method, TokenTypes.METHOD_CALL);
		assertNull(AstResolve.resolveVariableType(methodCall, "s"));
	}

	@Test
	public void testResolveVariableTypeVarLocalUninferableBindsWithoutType() throws Exception {
		final var ast = parseSource("class T { String v = \"\"; void f(java.util.List<java.nio.file.Path> ps) { var v = ps.get(0); v.toString(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "v"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallDottedReceiver() {
		final var method = findMethod(root(), "varMethodCallInitDottedReceiver");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallInit() {
		final var method = findMethod(root(), "varMethodCallInitLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("Object", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallOverloadDisambiguated() {
		final var method = findMethod(root(), "varMethodCallOverloadAmbiguousLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarMethodCallOverloadZeroArg() {
		final var method = findMethod(root(), "varMethodCallOverloadZeroArgLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArray() {
		final var method = findMethod(root(), "varNewArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArrayInitializer() {
		final var method = findMethod(root(), "varNewArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewArrayInitializerRef() {
		final var method = findMethod(root(), "varNewArrayInitializerRefLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewBothAnnotatedArray() {
		final var method = findMethod(root(), "varNewBothAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgs() throws Exception {
		final var ast = parseSource("class T { <U> T(U arg) {} void f() { var x = new <String>T(\"a\"); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("T", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgsBothLevels() throws Exception {
		final var ast = parseSource(
				"import java.util.ArrayList;\nclass T { void f() { var x = new <String>ArrayList<Object>(); } }"
		);
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("ArrayList", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewConstructorTypeArgsQualified() throws Exception {
		final var ast = parseSource("class T { void f() { var x = new <String>java.util.ArrayList<>(); } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.ArrayList", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDeeplyQualified() {
		final var method = findMethod(root(), "varNewDeeplyQualifiedLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.concurrent.atomic.AtomicInteger", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDimAnnotatedArray() {
		final var method = findMethod(root(), "varNewDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewDimAnnotatedMultiDimArray() {
		final var method = findMethod(root(), "varNewDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewGeneric() {
		final var method = findMethod(root(), "varNewGenericLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.HashMap", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewMultiDimArray() {
		final var method = findMethod(root(), "varNewMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayInitializerProvider")
	@ParameterizedTest
	void testResolveVariableTypeVarNewPrimitiveArrayInitializerTypes(String type, String expr) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + expr + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@MethodSource("primitiveArrayProvider")
	@ParameterizedTest
	void testResolveVariableTypeVarNewPrimitiveArrayTypes(String type, String expr) throws Exception {
		final var ast = parseSource("class T { void f() { var x = " + expr + "; } }");
		final var slist = findFirst(ast, TokenTypes.METHOD_DEF).findFirstToken(TokenTypes.SLIST);
		assertEquals(type + "[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveBothAnnotatedArray() {
		final var method = findMethod(root(), "varNewPrimitiveBothAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveDimAnnotatedArray() {
		final var method = findMethod(root(), "varNewPrimitiveDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveDimAnnotatedMultiDimArray() {
		final var method = findMethod(root(), "varNewPrimitiveDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveMultiDimArray() {
		final var method = findMethod(root(), "varNewPrimitiveMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveMultiDimArrayInitializer() {
		final var method = findMethod(root(), "varNewPrimitiveMultiDimArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveSizedArray() {
		final var method = findMethod(root(), "varNewPrimitiveSizedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewPrimitiveTypeAnnotatedArray() {
		final var method = findMethod(root(), "varNewPrimitiveTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("int[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualified() {
		final var method = findMethod(root(), "varNewQualifiedLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.Object", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedAnonymousClass() {
		final var method = findMethod(root(), "varNewQualifiedAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.Thread", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedArray() {
		final var method = findMethod(root(), "varNewQualifiedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedArrayInitializer() {
		final var method = findMethod(root(), "varNewQualifiedArrayInitializerLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDiamond() {
		final var method = findMethod(root(), "varNewQualifiedDiamondLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.HashMap", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDimAnnotatedArray() {
		final var method = findMethod(root(), "varNewQualifiedDimAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedDimAnnotatedMultiDimArray() {
		final var method = findMethod(root(), "varNewQualifiedDimAnnotatedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedGenericAnonymousClass() {
		final var method = findMethod(root(), "varNewQualifiedGenericAnonymousClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.ArrayList", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedInnerClass() {
		final var method = findMethod(root(), "varNewQualifiedInnerClassLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.util.AbstractMap.SimpleEntry", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedMultiDimArray() {
		final var method = findMethod(root(), "varNewQualifiedMultiDimArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[][]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewQualifiedTypeAnnotatedArray() {
		final var method = findMethod(root(), "varNewQualifiedTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("java.lang.String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewSimple() {
		final var method = findMethod(root(), "varNewLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("StringBuilder", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNewTypeAnnotatedArray() {
		final var method = findMethod(root(), "varNewTypeAnnotatedArrayLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertEquals("String[]", AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarNullLiteral() {
		final var method = findMethod(root(), "varNullLiteralLocal");
		final var slist = method.findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(slist.getLastChild(), "x"));
	}

	@Test
	public void testResolveVariableTypeVarResourceBindsWithoutType() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (var s = java.nio.file.Files.newInputStream(null)) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeVarResourceFinalModifier() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (final var s = new java.io.ByteArrayInputStream(new byte[0])) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeVarResourceIdentInitializer() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f(AutoCloseable c) throws Exception { try (var s = c) { s.close(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertNull(AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeVarResourceNewQualified() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; void f() throws Exception { try (var s = new java.io.ByteArrayInputStream(new byte[0])) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testResolveVariableTypeVarResourceSameFileMethodCall() throws Exception {
		final var ast = parseSource("class T { String s = \"\"; java.io.ByteArrayInputStream mk() { return null; } void f() throws Exception { try (var s = mk()) { s.read(); } } }");
		final var body = findFirst(ast, TokenTypes.LITERAL_TRY).findFirstToken(TokenTypes.SLIST);
		assertEquals("java.io.ByteArrayInputStream", AstResolve.resolveVariableType(body, "s"));
	}

	@Test
	public void testSameFileClassDefAndTypeParameterCount() throws Exception {
		final var source = "class T { static class Box<V> {} enum Kind { A } interface Face"
				+ " { class Deep<Y> {} } record Rec<A, B>() {} static class Holder { static class Mid"
				+ " { static class Leaf<X> {} } static class Plain {} } @interface Ann {}"
				+ " void m() { Object x; } }";
		final var ast = parseSource(source);
		final var scope = requireNonNull(findFirst(ast, TokenTypes.VARIABLE_DEF), "no declaration");

		assertEquals(1, typeParameterCountOf(scope, "Box"));
		assertEquals(0, typeParameterCountOf(scope, "Kind"));
		assertEquals(2, typeParameterCountOf(scope, "Rec"));
		assertEquals(1, typeParameterCountOf(scope, "Face.Deep"));
		assertEquals(0, typeParameterCountOf(scope, "Holder.Plain"));
		assertEquals(1, typeParameterCountOf(scope, "Holder.Mid.Leaf"));

		assertNull(AstResolve.sameFileClassDef(scope, "Absent"));
		assertNull(AstResolve.sameFileClassDef(scope, "Holder.Missing"));
		assertNull(AstResolve.sameFileClassDef(scope, "Holder.Mid.Missing"));
		assertNull(AstResolve.sameFileClassDef(scope, "."));
		assertNull(AstResolve.sameFileClassDef(scope, "Ann"));
		assertSame(AstResolve.sameFileClassDef(scope, "Box"), AstResolve.sameFileClassDef(scope, "Box"));
	}

	@Test
	public void testVariableIsVarargs() throws Exception {
		final var reference = parseSource("class T { void m(String... s) { s.toString(); } }");
		final var referenceScope = requireNonNull(findFirst(reference, TokenTypes.SLIST));
		assertTrue(AstResolve.variableIsVarargs(referenceScope, "s"));
		assertEquals("String", AstResolve.resolveVariableType(referenceScope, "s"));

		final var component = parseSource("record R(char... cs) { void g() { toString(); } }");
		final var componentScope = requireNonNull(findFirst(component, TokenTypes.SLIST));
		assertTrue(AstResolve.variableIsVarargs(componentScope, "cs"));
		assertEquals(
				"char[]",
				AstResolve.resolveVariableType(componentScope, "cs"),
				"a record component binds the whole array, where a parameter binds the element"
		);

		final var array = parseSource("class T { void m(String[] s) { s.toString(); } }");
		final var arrayScope = requireNonNull(findFirst(array, TokenTypes.SLIST));
		assertFalse(AstResolve.variableIsVarargs(arrayScope, "s"));
		assertFalse(AstResolve.variableIsVarargs(arrayScope, "nosuch"));
	}

	@Test
	public void testVariableTypeArgumentName() throws Exception {
		final var root = requireNonNull(parseSource(
				"import java.util.List; import java.util.Map;\n"
						+ "class T { void m(Map<String, List<String>> src) { src.size(); } }"
		));
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));

		assertEquals("String", AstResolve.variableTypeArgumentName(call, "src", 0));
		assertEquals("List", AstResolve.variableTypeArgumentName(call, "src", 1));
		assertNull(AstResolve.variableTypeArgumentName(call, "src", 2), "past the last argument");
		assertNull(AstResolve.variableTypeArgumentName(call, "nosuch", 0), "no such variable");
	}

	@Test
	public void testVariableTypeArgumentNameWithoutArguments() throws Exception {
		final var root = requireNonNull(parseSource(
				"import java.util.Map;\nclass T { void m(Map src) { src.size(); } }"
		));
		final var call = requireNonNull(findFirst(root, TokenTypes.METHOD_CALL));

		assertNull(AstResolve.variableTypeArgumentName(call, "src", 0));
	}
}