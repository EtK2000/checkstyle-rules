package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.SourceComments;
import com.etk2000.checkstyle.ast.AstText;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class FieldRewriteVerifierTest {
	private static final String AT_DEPTH_CAP = nestedAnnotation(AstText.MAX_ANNOTATION_DEPTH);
	private static final String PAST_DEPTH_CAP = nestedAnnotation(AstText.MAX_ANNOTATION_DEPTH + 1);

	static Stream<Arguments> acceptedProvider() {
		return Stream.of(
				Arguments.of("field annotation at the depth cap", List.of(AT_DEPTH_CAP + " int alpha;", AT_DEPTH_CAP + " int beta;"), List.of(AT_DEPTH_CAP + " int alpha, beta;")),
				Arguments.of("member annotation at the depth cap", List.of(AT_DEPTH_CAP + " void m() {", "}", "int alpha;", "int beta;"), List.of(AT_DEPTH_CAP + " void m() {", "}", "int alpha, beta;")),
				Arguments.of("member annotation param order", List.of("@M(b = 2, a = 1) void m() {", "}"), List.of("@M(a = 1, b = 2) void m() {", "}")),
				Arguments.of("modifier order", List.of("private static int alpha;", "static private int beta;"), List.of("private static int alpha, beta;")),
				Arguments.of("unnormalizable type brackets rejoined from the next line", List.of("int @A [] alpha", "[];"), List.of("int @A [] alpha[];")),
				Arguments.of("prev trailing comment carried", List.of("int alpha; // why", "int beta;"), List.of("int alpha, beta; // why")),
				Arguments.of("leading line comment kept on the merged declaration", List.of("// in pixels", "int height;", "int width;"), List.of("// in pixels", "int height, width;")),
				Arguments.of("Javadoc between declarators", List.of("int alpha;", "int beta,", "/** doc */", "gamma;"), List.of("int alpha, beta,", "/** doc */", "gamma;")),
				Arguments.of("Javadoc moved with its field", List.of("int zeta;", "/** doc */", "int alpha;"), List.of("/** doc */", "int alpha;", "int zeta;")),
				Arguments.of(
						"enum constants reordered with their comments",
						List.of("enum E {", "ZETA, // z", "/* a */ ALPHA(1) {", "}", "}"),
						List.of("enum E {", "/* a */ ALPHA(1) {", "},", "ZETA // z", "}")
				),
				Arguments.of("enum terminator moved past a comment", List.of("enum E {", "ZETA,", "ALPHA,", "// note", ";", "int x;", "}"), List.of("enum E {", "ALPHA,", "ZETA;", "// note", "int x;", "}")),
				Arguments.of("plain merge", List.of("int alpha;", "int beta;"), List.of("int alpha, beta;")),
				Arguments.of("C-style hoist", List.of("int alpha[];", "int[] beta;"), List.of("int[] alpha, beta;")),
				Arguments.of(
						"generic base hoist",
						List.of("java.util.List<String>[] alpha[];", "java.util.List<String>[][] beta;"),
						List.of("java.util.List<String>[][] alpha, beta;")
				),
				Arguments.of("wrapped merge", List.of("int alpha;", "int beta;"), List.of("int alpha,", "    beta;")),
				Arguments.of("carried trailing comment", List.of("int alpha;", "int beta; /* note */"), List.of("int alpha, beta; /* note */")),
				Arguments.of("reorder and merge", List.of("int beta;", "int alpha;"), List.of("int alpha, beta;")),
				Arguments.of("reorder with initializers", List.of("int beta = 2;", "int alpha = 1;"), List.of("int alpha = 1;", "int beta = 2;")),
				Arguments.of("duplicate comment dedup", List.of("int alpha; // px", "int beta; // px"), List.of("int alpha, beta; // px")),
				Arguments.of("distinct annotation order", List.of("@A @B int alpha;", "@B @A int beta;"), List.of("@A @B int alpha, beta;")),
				Arguments.of("annotation param order", List.of("@M(b = 2, a = 1) int alpha;", "@M(a = 1, b = 2) int beta;"), List.of("@M(b = 2, a = 1) int alpha, beta;")),
				Arguments.of("shorthand vs explicit value", List.of("@S(\"u\") int alpha;", "@S(value = \"u\") int beta;"), List.of("@S(\"u\") int alpha, beta;")),
				Arguments.of("initializer whitespace", List.of("int alpha = 1+2;"), List.of("int alpha = 1 + 2;")),
				Arguments.of("stray semicolon dropped", List.of("int alpha;;", "int beta;"), List.of("int alpha, beta;")),
				Arguments.of("unnormalizable type moved whole", List.of("int @A [] beta[];", "int alpha;"), List.of("int alpha;", "int @A [] beta[];")),
				Arguments.of(
						"merge inside an anonymous class",
						List.of("Runnable r = new Runnable() {", "int alpha;", "int beta;", "public void run() {", "}", "};"),
						List.of("Runnable r = new Runnable() {", "int alpha, beta;", "public void run() {", "}", "};")
				),
				Arguments.of("no-op", List.of("int alpha;", "int beta;"), List.of("int alpha;", "int beta;")),
				Arguments.of("end-line code wins over start-line code", List.of("enum E {", "ZETA, /* a */ ALPHA", "}"), List.of("enum E {", "/* a */ ALPHA,", "ZETA", "}")),
				Arguments.of("multi-line comment binds by the code after its last line", List.of("enum E {", "ZETA, /* a", "b */ ALPHA", "}"), List.of("enum E {", "/* a", "b */ ALPHA,", "ZETA", "}")),
				Arguments.of("comment ahead of code outside every run stays loose", List.of("int alpha; /* c */ void m() {", "}"), List.of("int alpha;", "/* c */ void m() {", "}")),
				Arguments.of("comment trailing code outside every run stays loose", List.of("int alpha;", "int beta;", "void m() {", "} // done"), List.of("int alpha, beta;", "void m() {", "} // done")),
				Arguments.of("empty block comment is no Javadoc", List.of("/**/ int alpha;", "int beta;"), List.of("/**/ int alpha, beta;")),
				Arguments.of("Javadoc kept on a declaration whose names are reordered", List.of("/** doc */", "int zebra, alpha;"), List.of("/** doc */", "int alpha, zebra;")),
				Arguments.of("comment holding a unicode escape that breaks nothing", List.of("int alpha; // caf\\u00e9", "int beta;"), List.of("int alpha, beta; // caf\\u00e9")),
				Arguments.of("escaped backslash ahead of a would-be escape", List.of("String zeta = \"\\\\u0022\";", "String alpha;"), List.of("String alpha;", "String zeta = \"\\\\u0022\";")),
				Arguments.of("escape cut short by the end of the line", List.of("int alpha; // \\u002", "int beta;"), List.of("int alpha, beta; // \\u002")),
				Arguments.of("backslash u with no digits", List.of("int alpha; // \\u", "int beta;"), List.of("int alpha, beta; // \\u")),
				Arguments.of("backslash u with digits that are not hex", List.of("int alpha; // \\u00zz", "int beta;"), List.of("int alpha, beta; // \\u00zz")),
				Arguments.of("upper-case U is no escape", List.of("int alpha; // \\U0022", "int beta;"), List.of("int alpha, beta; // \\U0022")),
				Arguments.of("harmless escapes in a char and a string", List.of("char zeta = '\\u0041';", "String alpha = \"\\u00e9\";"), List.of("String alpha = \"\\u00e9\";", "char zeta = '\\u0041';")),
				Arguments.of("block comment joined as a line comment", List.of("int alpha;", "int beta; /* why */"), List.of("// why", "int alpha, beta;")),
				Arguments.of("two comments joined in order", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// b g", "int alpha, beta, gamma;")),
				Arguments.of("comments moved apart without a join", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// b", "/* g */", "int alpha, beta, gamma;")),
				Arguments.of(
						"block comment before an enum terminator moved past it",
						List.of("enum E {", "GREEN,", "BLUE", "/* Shared helper. */", ";", "void helper() {", "}", "}"),
						List.of("enum E {", "BLUE,", "GREEN;", "/* Shared helper. */", "void helper() {", "}", "}")
				)
		);
	}

	static Stream<Arguments> initializationOrderProvider() {
		return Stream.of(
				Arguments.of("pinned pair swapped", List.of("int zeta = Ids.next();", "int alpha = 1;"), List.of("int alpha = 1;", "int zeta = Ids.next();"), false),
				Arguments.of("pinned pair merged in source order", List.of("int zeta = Ids.next();", "int alpha = 1;"), List.of("int zeta = Ids.next(), alpha = 1;"), true),
				Arguments.of("pinned pair merged and swapped", List.of("int zeta = Ids.next();", "int alpha = 1;"), List.of("int alpha = 1, zeta = Ids.next();"), false),
				Arguments.of(
						"pinned pair swapped in a nested class",
						List.of("int beta;", "class U {", "int zeta = Ids.next();", "int alpha = 1;", "}"),
						List.of("int beta;", "class U {", "int alpha = 1;", "int zeta = Ids.next();", "}"),
						false
				),
				Arguments.of("unpinned pair swapped", List.of("int zeta;", "int alpha = 1;"), List.of("int alpha = 1;", "int zeta;"), true),
				Arguments.of("type added", List.of("int alpha;"), List.of("int alpha;", "class X {", "}"), false),
				Arguments.of("anonymous body moved to another field", List.of("Object a = new Object() {};", "Object b = null;"), List.of("Object a = null;", "Object b = new Object() {};"), false),
				Arguments.of(
						"second anonymous body of one owner matched to its own rewrite",
						List.of("Object pair = List.of(new Object() { int zeta = Ids.next(); int alpha = 1; }, new Object() { int yew; int beta; });"),
						List.of("Object pair = List.of(new Object() { int zeta = Ids.next(); int alpha = 1; }, new Object() { int beta; int yew; });"),
						true
				),
				Arguments.of("no longer parses", List.of("int alpha;"), List.of("int alpha"), false)
		);
	}

	private static boolean keepsInitializationOrder(List<String> body, List<String> replacement) {
		final var before = new ArrayList<String>();
		before.add("class T {");
		before.addAll(body);
		before.add("}");
		return FieldRewriteVerifier.keepsInitializationOrder(before, new FixResult(1, body.size(), replacement));
	}

	private static String nestedAnnotation(int count) {
		return "@W(".repeat(count - 1) + "@W" + ")".repeat(count - 1);
	}

	private static boolean preserves(List<String> body, List<String> replacement) {
		final var before = new ArrayList<String>();
		before.add("class T {");
		before.addAll(body);
		before.add("}");
		return FieldRewriteVerifier.preserves(before, new FixResult(1, body.size(), replacement));
	}

	static Stream<Arguments> rejectedProvider() {
		return Stream.of(
				Arguments.of("unparseable output", List.of("int alpha;", "int beta;"), List.of("int alpha,, beta;")),
				Arguments.of("dropped field", List.of("int alpha;", "int beta;"), List.of("int alpha;")),
				Arguments.of("renamed field", List.of("int alpha;", "int beta;"), List.of("int alpha, betx;")),
				Arguments.of("added declarator", List.of("int alpha;", "int beta;"), List.of("int alpha, beta;", "int gamma;")),
				Arguments.of("collapsed duplicate declarator", List.of("int alpha;", "int alpha;"), List.of("int alpha;")),
				Arguments.of("member annotation past the depth cap", List.of(PAST_DEPTH_CAP + " void m() {", "}"), List.of(PAST_DEPTH_CAP + " void m() {", "}")),
				Arguments.of("field annotation past the depth cap", List.of(PAST_DEPTH_CAP + " int alpha;"), List.of(PAST_DEPTH_CAP + " int alpha;")),
				Arguments.of("type-use annotation past the depth cap", List.of("java.util.List<" + PAST_DEPTH_CAP + " String> alpha;"), List.of("java.util.List<" + PAST_DEPTH_CAP + " String> alpha;")),
				Arguments.of("initializer annotation past the depth cap", List.of("Object o = new Object() {", PAST_DEPTH_CAP + " int x;", "};"), List.of("Object o = new Object() {", PAST_DEPTH_CAP + " int x;", "};")),
				Arguments.of("stray semicolon after a method dropped", List.of("void m() {", "};", "int alpha;"), List.of("void m() {", "}", "int alpha;")),
				Arguments.of("written modifier dropped", List.of("final int @A [] alpha[];"), List.of("int @A [] alpha[];")),
				Arguments.of("comment deduplicated across the file", List.of("// TODO remove", "int zeta;", "void m() {", "// TODO remove", "}"), List.of("int zeta;", "void m() {", "// TODO remove", "}")),
				Arguments.of("comment moved out of the run", List.of("int zeta; // note", "void m() {", "}"), List.of("int zeta;", "void m() {", "// note", "}")),
				Arguments.of("Javadoc reattached", List.of("int zeta;", "/** doc */", "int beta;"), List.of("/** doc */", "int zeta;", "int beta;")),
				Arguments.of("Javadoc stretched over a merged name", List.of("/** doc */", "int alpha;", "int beta;"), List.of("/** doc */", "int alpha, beta;")),
				Arguments.of("Javadoc on a later declarator moved out of the run", List.of("int zeta, /** doc */ beta;"), List.of("int zeta, beta;", "/** doc */")),
				Arguments.of(
						"suppression comments swapped",
						List.of("// CHECKSTYLE:OFF", "void m() {", "}", "// CHECKSTYLE:ON", "int alpha;"),
						List.of("// CHECKSTYLE:ON", "void m() {", "}", "// CHECKSTYLE:OFF", "int alpha;")
				),
				Arguments.of("escaped line terminator in a comment", List.of("int zeta = 1; // \\u000a { alpha = 9; }", "int alpha = 3;"), List.of("int alpha = 3;", "int zeta = 1; // \\u000a { alpha = 9; }")),
				Arguments.of("escaped comment close", List.of("int zeta; /* \\u002a/ int x; /* */", "int alpha;"), List.of("int alpha;", "int zeta; /* \\u002a/ int x; /* */")),
				Arguments.of("enum constant argument changed", List.of("enum E {", "ALPHA(1), BETA", "}"), List.of("enum E {", "ALPHA(2), BETA", "}")),
				Arguments.of("enum constant body changed", List.of("enum E {", "ALPHA {", "}", "}"), List.of("enum E {", "ALPHA {", "int x;", "}", "}")),
				Arguments.of("enum constant dropped", List.of("enum E {", "ALPHA, BETA", "}"), List.of("enum E {", "ALPHA", "}")),
				Arguments.of("enum constant comment reattached", List.of("enum E {", "ALPHA, // a", "BETA", "}"), List.of("enum E {", "ALPHA,", "BETA // a", "}")),
				Arguments.of("dropped initializer", List.of("int alpha = 1;", "int beta;"), List.of("int alpha, beta;")),
				Arguments.of("changed initializer", List.of("int alpha = 1;"), List.of("int alpha = 2;")),
				Arguments.of(
						"retyped by qualifier",
						List.of("@Nullable String alpha;", "@javax.annotation.Nullable String beta;"),
						List.of("@Nullable String alpha, beta;")
				),
				Arguments.of(
						"repeated annotation reordered",
						List.of("@Tag(\"x\") @Tag(\"y\") int alpha;", "@Tag(\"y\") @Tag(\"x\") int beta;"),
						List.of("@Tag(\"x\") @Tag(\"y\") int alpha, beta;")
				),
				Arguments.of("retyped by type", List.of("int alpha;", "long beta;"), List.of("int alpha, beta;")),
				Arguments.of(
						"retyped type-use qualifier",
						List.of("java.util.List<@a.Ann String> alpha;", "java.util.List<@b.Ann String> beta;"),
						List.of("java.util.List<@a.Ann String> alpha, beta;")
				),
				Arguments.of("annotated dimension naive fold", List.of("int @A [] alpha[];"), List.of("int @A [][] alpha;")),
				// even the fold JLS 10.2 allows is refused; no field fixer performs it
				Arguments.of("annotated dimension correct fold", List.of("int @A [] alpha[];"), List.of("int[] @A [] alpha;")),
				Arguments.of("stacked type-use reordered", List.of("String @A @B [] alpha;", "String @B @A [] beta;"), List.of("String @A @B [] alpha, beta;")),
				Arguments.of("modifier changed", List.of("final int alpha;", "int beta;"), List.of("final int alpha, beta;")),
				Arguments.of("dropped comment", List.of("int alpha; // a", "int beta; // b"), List.of("int alpha, beta; // a")),
				Arguments.of("changed comment", List.of("int alpha; // px"), List.of("int alpha; // pix")),
				Arguments.of("added comment", List.of("int alpha;"), List.of("int alpha; // new")),
				Arguments.of("dropped Javadoc", List.of("/** doc */", "int alpha;"), List.of("int alpha;")),
				Arguments.of("changed method body", List.of("int f() {", "return 1;", "}"), List.of("int f() {", "return 2;", "}")),
				Arguments.of("merged locals", List.of("void m() {", "int alpha;", "int beta;", "}"), List.of("void m() {", "int alpha, beta;", "}")),
				Arguments.of(
						"field moved across a method",
						List.of("int beta;", "void m() {", "}", "int alpha;"),
						List.of("int alpha;", "void m() {", "}", "int beta;")
				),
				Arguments.of("changed annotation value", List.of("@S(\"a\") int alpha;"), List.of("@S(\"b\") int alpha;")),
				Arguments.of(
						"trailing Javadocs swapped onto other fields",
						List.of("private int zeta; /**< the zeta */", "private int alpha; /**< the alpha */"),
						List.of("private int alpha; /**< the alpha */", "private int zeta; /**< the zeta */")
				),
				Arguments.of("trailing Javadoc left behind by a merge", List.of("int alpha; /** px */", "int beta;"), List.of("int alpha, beta; /** px */")),
				Arguments.of("enum constant Javadoc after a comma moved", List.of("enum E {", "ZETA, /** doc */", "ALPHA", "}"), List.of("enum E {", "ALPHA,", "ZETA /** doc */", "}")),
				Arguments.of("duplicate Javadoc collapsed", List.of("/** doc */", "/** doc */", "int alpha;"), List.of("/** doc */", "int alpha;")),
				Arguments.of("comment duplicated", List.of("int alpha; /* px */", "int beta;"), List.of("int alpha, beta; /* px */ /* px */")),
				Arguments.of(
						"comment moved between fields of an anonymous class",
						List.of("Runnable r = new Runnable() {", "int alpha; // a", "int beta;", "public void run() {", "}", "};"),
						List.of("Runnable r = new Runnable() {", "int alpha;", "int beta; // a", "public void run() {", "}", "};")
				),
				Arguments.of("enum constant annotation moved to another constant", List.of("enum E {", "@Deprecated ALPHA, BETA", "}"), List.of("enum E {", "ALPHA,", "@Deprecated BETA", "}")),
				Arguments.of("enum constant annotation past the depth cap", List.of("enum E {", PAST_DEPTH_CAP + " ALPHA", "}"), List.of("enum E {", PAST_DEPTH_CAP + " ALPHA", "}")),
				Arguments.of("escaped carriage return in a comment", List.of("int zeta = 1; // \\u000d { alpha = 9; }", "int alpha = 3;"), List.of("int alpha = 3;", "int zeta = 1; // \\u000d { alpha = 9; }")),
				Arguments.of("escaped slash closing a comment", List.of("int zeta; /* *\\u002f int x; /* */", "int alpha;"), List.of("int alpha;", "int zeta; /* *\\u002f int x; /* */")),
				Arguments.of("escaped line terminator in upper-case hex", List.of("int zeta = 1; // \\u000A { alpha = 9; }", "int alpha = 3;"), List.of("int alpha = 3;", "int zeta = 1; // \\u000A { alpha = 9; }")),
				Arguments.of("escaped line terminator with a repeated u", List.of("int zeta = 1; // \\uu000a { alpha = 9; }", "int alpha = 3;"), List.of("int alpha = 3;", "int zeta = 1; // \\uu000a { alpha = 9; }")),
				Arguments.of("escaped quote in a string", List.of("String zeta = \"a\\u0022 + \\u0022b\";", "String alpha;"), List.of("String alpha;", "String zeta = \"a\\u0022 + \\u0022b\";")),
				Arguments.of("escaped apostrophe in a char", List.of("char zeta = '\\u0027';", "char alpha;"), List.of("char alpha;", "char zeta = '\\u0027';")),
				Arguments.of("escaped backslash in a string", List.of("String zeta = \"\\u005c\";", "String alpha;"), List.of("String alpha;", "String zeta = \"\\u005c\";")),
				Arguments.of("escape after an escaped backslash", List.of("String zeta = \"\\\\\\u0022\";", "String alpha;"), List.of("String alpha;", "String zeta = \"\\\\\\u0022\";")),
				Arguments.of("escape inside a text block", List.of("String zeta = \"\"\"", "a\\u0022", "\"\"\";", "String alpha;"), List.of("String alpha;", "String zeta = \"\"\"", "a\\u0022", "\"\"\";")),
				Arguments.of("starred Javadoc stretched over a merged name", List.of("/***/", "int alpha;", "int beta;"), List.of("/***/", "int alpha, beta;")),
				Arguments.of("markdown doc comment stretched over a merged name", List.of("/// doc", "int alpha;", "int beta;"), List.of("/// doc", "int alpha, beta;")),
				Arguments.of("four-slash doc comment stretched over a merged name", List.of("//// doc", "int alpha;", "int beta;"), List.of("//// doc", "int alpha, beta;")),
				Arguments.of("joined comments out of order", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// g b", "int alpha, beta, gamma;")),
				Arguments.of("joined comment with a word added", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// b g x", "int alpha, beta, gamma;")),
				Arguments.of("joined comment with a doubled space", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// b  g", "int alpha, beta, gamma;")),
				Arguments.of("joined comment missing one", List.of("int alpha;", "int beta; // b", "int gamma; /* g */"), List.of("// b", "int alpha, beta, gamma;")),
				Arguments.of("comment joined with one from outside the run", List.of("int alpha;", "int beta; // b", "void m() {", "// m", "}"), List.of("// b m", "int alpha, beta;", "void m() {", "}")),
				Arguments.of(
						"Javadoc before an enum terminator attached to a field",
						List.of("enum E {", "BETA, ALPHA, ZETA /** @deprecated */;", "int legacy;", "}"),
						List.of("enum E {", "ALPHA,", "BETA,", "ZETA; /** @deprecated */", "int legacy;", "}")
				),
				Arguments.of(
						"Javadoc before an enum terminator attached to a method",
						List.of("enum E {", "GREEN,", "BLUE", "/** Shared helper. */", ";", "void helper() {", "}", "}"),
						List.of("enum E {", "BLUE,", "GREEN;", "/** Shared helper. */", "void helper() {", "}", "}")
				)
		);
	}

	@MethodSource("acceptedProvider")
	@ParameterizedTest(name = "{0}")
	void testAccepts(String description, List<String> body, List<String> replacement) {
		assertTrue(preserves(body, replacement));
	}

	@Test
	public void testAcceptsBomBuffer() {
		assertTrue(FieldRewriteVerifier.preserves(List.of((char) 0xFEFF + "class T {", "int alpha;", "int beta;", "}"), new FixResult(1, 2, List.of("int alpha, beta;"))));
	}

	@Test
	public void testAcceptsCommentsOutsideEveryToken() {
		assertTrue(FieldRewriteVerifier.preserves(List.of("// header", "class T {", "int alpha;", "int beta;", "}", "// trailer"), new FixResult(2, 3, List.of("int alpha, beta;"))));
	}

	@Test
	public void testAcceptsJavadocWithNoTokenAfterIt() {
		assertTrue(FieldRewriteVerifier.preserves(List.of("class T {", "int alpha;", "int beta;", "}", "/** trailer */"), new FixResult(1, 2, List.of("int alpha, beta;"))));
	}

	@Test
	public void testAcceptsPureInsertionAtEnd() {
		assertTrue(FieldRewriteVerifier.preserves(List.of("class T {", "int alpha;", "}"), new FixResult(3, 2, List.of(""))));
	}

	@Test
	public void testAcceptsRangeAfterABlankFirstLine() {
		assertTrue(FieldRewriteVerifier.preserves(List.of("", "class T {", "int alpha;", "int beta;", "}"), new FixResult(2, 3, List.of("int alpha, beta;"))));
	}

	@Test
	public void testAcceptsRangeEndingOnLastLine() {
		assertTrue(FieldRewriteVerifier.preserves(List.of("class T {", "int alpha;", "}"), new FixResult(2, 2, List.of("}"))));
	}

	@MethodSource("initializationOrderProvider")
	@ParameterizedTest(name = "{0}")
	void testInitializationOrder(String description, List<String> body, List<String> replacement, boolean expected) {
		assertEquals(expected, keepsInitializationOrder(body, replacement));
	}

	@Test
	public void testInitializationOrderOutOfRangeFixResult() {
		assertFalse(FieldRewriteVerifier.keepsInitializationOrder(List.of("class T {", "}"), new FixResult(1, 2, List.of("}"))));
	}

	@Test
	public void testInitializationOrderUnparseableBeforeParseableAfter() {
		assertFalse(FieldRewriteVerifier.keepsInitializationOrder(List.of("class T {", "int alpha", "}"), new FixResult(1, 1, List.of("int alpha;"))));
	}

	@Test
	public void testInitializationOrderUnparseableInput() {
		assertFalse(FieldRewriteVerifier.keepsInitializationOrder(List.of("int alpha;", "int beta;"), new FixResult(0, 1, List.of("int alpha, beta;"))));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"// NOSONAR",
			"// NOPMD",
			"//$NON-NLS-1$",
			"// CHECKSTYLE:OFF",
			"//noinspection unchecked",
			"// @formatter:off",
			"// spotless:off",
			"/* NOSONAR */",
			"// keep NOSONAR here",
			"// XNOSONARX",
			"// CHECKSTYLE IGNORE MagicNumber FOR NEXT 1 LINES",
			"// language=SQL",
			"/* language=JSON */",
			"//noinspection unused",
			"// noinspection unused",
			"//\tnoinspection",
			"//noinspection",
			"/* noinspection unused */",
			"// keep noinspection unused",
			"/// noinspection unused"
	})
	void testIsDirective(String comment) {
		assertTrue(SourceComments.isDirective(comment));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"// NOSONA",
			"// nosonar",
			"// NOPM",
			"// nopmd",
			"// NON-NLS-1",
			"// $non-nls-1$",
			"// checkstyle:off",
			"// CHECKSTYL",
			"// NoInspection",
			"// NoInspection unused",
			"// noinspectio",
			"// noinspectio unused",
			"// Language=SQL",
			"// language = SQL",
			"// @formatter off",
			"// formatter:off",
			"// @Formatter:off",
			"// spotless off",
			"// Spotless:off",
			"// plain",
			"//",
			"/**/"
	})
	void testIsNotDirective(String comment) {
		assertFalse(SourceComments.isDirective(comment));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"// NoInspection unused",
			"// noinspectio unused",
			"/* noinspection unused */",
			"// keep noinspection unused",
			"// NOSONAR",
			"// @formatter:off",
			"/// noinspection unused",
			"// language=SQL"
	})
	void testIsNotStatementDirective(String comment) {
		assertFalse(SourceComments.isStatementDirective(comment));
	}

	@ParameterizedTest
	@ValueSource(strings = {"//noinspection unused", "// noinspection unused", "//\tnoinspection", "//noinspection"})
	void testIsStatementDirective(String comment) {
		assertTrue(SourceComments.isStatementDirective(comment));
	}

	@Test
	public void testMemoIgnoresALaterEditOfTheBuffer() {
		final var lines = new ArrayList<>(List.of("class T {", "int alpha;", "int beta;", "}"));
		assertTrue(FieldRewriteVerifier.preserves(lines, new FixResult(1, 2, List.of("int alpha, beta;"))));
		lines.set(2, "long beta;");
		assertFalse(FieldRewriteVerifier.preserves(lines, new FixResult(1, 2, List.of("int alpha, beta;"))));
	}

	@Test
	public void testMemoKeepsFieldOrdersOfTheBufferAsEdited() {
		final var lines = new ArrayList<>(List.of("class T {", "int zeta;", "int alpha = 1;", "}"));
		assertTrue(FieldRewriteVerifier.keepsInitializationOrder(lines, new FixResult(1, 2, List.of("int alpha = 1;", "int zeta;"))));
		lines.set(1, "int zeta = Ids.next();");
		assertFalse(FieldRewriteVerifier.keepsInitializationOrder(lines, new FixResult(1, 2, List.of("int alpha = 1;", "int zeta = Ids.next();"))));
	}

	@MethodSource("rejectedProvider")
	@ParameterizedTest(name = "{0}")
	void testRejects(String description, List<String> body, List<String> replacement) {
		assertFalse(preserves(body, replacement));
	}

	@Test
	public void testRejectsClassHeaderJavadocMovedToAnotherField() {
		final var before = List.of("class T { /** The zeta. */", "int zeta;", "int alpha;", "}");
		assertFalse(FieldRewriteVerifier.preserves(before, new FixResult(1, 2, List.of("int alpha;", "int zeta;"))));
		assertFalse(FieldRewriteVerifier.preserves(before, new FixResult(1, 2, List.of("int alpha, zeta;"))));
	}

	@Test
	public void testRejectsOutOfRangeFixResult() {
		assertFalse(FieldRewriteVerifier.preserves(List.of("class T {", "}"), new FixResult(1, 2, List.of("}"))));
		assertFalse(FieldRewriteVerifier.preserves(List.of("class T {", "}"), new FixResult(-1, 0, List.of("class T {"))));
		assertFalse(FieldRewriteVerifier.preserves(List.of("class T {", "}"), new FixResult(3, 2, List.of(""))));
	}

	@Test
	public void testRejectsUnparseableInput() {
		assertFalse(FieldRewriteVerifier.preserves(List.of("int alpha;", "int beta;"), new FixResult(0, 1, List.of("int alpha, beta;"))));
	}
}