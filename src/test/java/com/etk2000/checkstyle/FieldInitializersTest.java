package com.etk2000.checkstyle;

import static com.etk2000.checkstyle.ScalingAssertions.assertScalesLinearly;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.FieldInitializers.BodyOrder;
import com.etk2000.checkstyle.FieldInitializers.Kind;
import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

@Isolated
public class FieldInitializersTest {
	private static final List<String> PREAMBLE = List.of(
			"class T {",
			"static final int SIZE = 4;",
			"int[] arr;",
			"Integer boxed;",
			"int b;",
			"int count;",
			"boolean flag;",
			"String label;",
			"List<String> names = new ArrayList<>();",
			"T other;",
			"double ratio;",
			"String regex;",
			"ChronoUnit unit;"
	);
	// every token type isLiteral lets through when it has no children
	private static final Set<Integer> WHITELIST = Set.of(
			TokenTypes.ARRAY_DECLARATOR,
			TokenTypes.ARRAY_INIT,
			TokenTypes.BAND,
			TokenTypes.BNOT,
			TokenTypes.BOR,
			TokenTypes.BSR,
			TokenTypes.BXOR,
			TokenTypes.CHAR_LITERAL,
			TokenTypes.COLON,
			TokenTypes.COMMA,
			TokenTypes.DIV,
			TokenTypes.EQUAL,
			TokenTypes.EXPR,
			TokenTypes.GE,
			TokenTypes.GT,
			TokenTypes.LAND,
			TokenTypes.LE,
			TokenTypes.LITERAL_FALSE,
			TokenTypes.LITERAL_NULL,
			TokenTypes.LITERAL_TRUE,
			TokenTypes.LNOT,
			TokenTypes.LOR,
			TokenTypes.LPAREN,
			TokenTypes.LT,
			TokenTypes.MINUS,
			TokenTypes.MOD,
			TokenTypes.NOT_EQUAL,
			TokenTypes.NUM_DOUBLE,
			TokenTypes.NUM_FLOAT,
			TokenTypes.NUM_INT,
			TokenTypes.NUM_LONG,
			TokenTypes.PLUS,
			TokenTypes.QUESTION,
			TokenTypes.RBRACK,
			TokenTypes.RCURLY,
			TokenTypes.RPAREN,
			TokenTypes.SL,
			TokenTypes.SR,
			TokenTypes.STAR,
			TokenTypes.STRING_LITERAL,
			TokenTypes.TEXT_BLOCK_CONTENT,
			TokenTypes.TEXT_BLOCK_LITERAL_BEGIN,
			TokenTypes.TEXT_BLOCK_LITERAL_END,
			TokenTypes.UNARY_MINUS,
			TokenTypes.UNARY_PLUS
	);

	static Stream<Arguments> bareForwardReadsProvider() {
		return Stream.of(
				Arguments.of("later constant read bare", List.of("B(A), A(null);"), Map.of("B", "A")),
				Arguments.of("two later constants read bare", List.of("C(B, A), A(null), B(null);"), Map.of("C", "A")),
				Arguments.of("later constant read bare and qualified", List.of("B(E.A, A), A(null, null);"), Map.of("B", "A")),
				Arguments.of("later constant read qualified", List.of("B(E.A), A(null);"), Map.of()),
				Arguments.of("later constant read in a body", List.of("B(null) {", "Object o = A;", "},", "A(null);"), Map.of()),
				Arguments.of("later constant read by a constructor", List.of("B(null), A(null);", "E(E p, int v) {", "use(A);", "}"), Map.of()),
				Arguments.of("earlier constant read bare", List.of("A(null), B(A);"), Map.of()),
				Arguments.of("later constant named by a case label", List.of("B(null) {", "int m(E e) {", "return switch (e) { case A -> 1; default -> 0; };", "}", "},", "A(null);"), Map.of())
		);
	}

	static Stream<Arguments> constantReadsProvider() {
		return Stream.of(
				Arguments.of("no read", List.of("A(null), B(null);"), Map.of()),
				Arguments.of("earlier constants read", List.of("ROOT(null), ELECTRONICS(ROOT), BOOKS(ROOT);"), Map.of("ELECTRONICS", Set.of("ROOT"), "BOOKS", Set.of("ROOT"))),
				Arguments.of("earlier constant read qualified", List.of("ROOT(null), BOOKS(E.ROOT);"), Map.of("BOOKS", Set.of("ROOT"))),
				Arguments.of("later constant read bare", List.of("B(A), A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("later constant read qualified", List.of("B(E.A), A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("later constant read bare and qualified", List.of("B(A, E.A), A(null, null);"), Map.of("B", Set.of("A"))),
				Arguments.of("later constant read qualified and bare", List.of("B(E.A, A), A(null, null);"), Map.of("B", Set.of("A"))),
				Arguments.of("later constant read inside a call", List.of("B(m(A)), A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("another type's constant read", List.of("B(Other.A), A(null);"), Map.of()),
				Arguments.of("constant read through an object", List.of("B(other.A), A(null);"), Map.of()),
				Arguments.of("constant read through a call's result", List.of("B(m().A), A(null);"), Map.of()),
				Arguments.of("constant qualified by a prefix of no package", List.of("B(x.E.A), A(null);"), Map.of()),
				Arguments.of("own constant read", List.of("A(A);"), Map.of()),
				Arguments.of("later constant read in a body", List.of("B(null) {", "Object o = A;", "},", "A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("earlier constant read in a body", List.of("A(null),", "B(null) {", "Object o = E.A;", "};"), Map.of("B", Set.of("A"))),
				Arguments.of("later constant read in a body's method", List.of("B(null) {", "void m() {", "use(A);", "}", "},", "A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("body field named like a constant", List.of("A(null),", "B(null) {", "int A;", "int x = A;", "};"), Map.of("B", Set.of("A"))),
				Arguments.of("body method parameter named like a constant", List.of("B(null) {", "void m(Object A) {", "use(A);", "}", "},", "A(null);"), Map.of()),
				Arguments.of("lambda parameter named like a constant", List.of("B(A -> A), A(null);"), Map.of()),
				Arguments.of("lambda parameter beside a qualified read of its name", List.of("B(A -> E.A), A(null);"), Map.of("B", Set.of("A"))),
				Arguments.of("constant read by an instance field", List.of("Z(null), A(null), B(null);", "final Object o = E.A;"), Map.of("Z", Set.of("A"), "B", Set.of("A"))),
				Arguments.of("constant read by an instance initializer", List.of("A(null), B(null);", "{", "use(A);", "}"), Map.of("B", Set.of("A"))),
				Arguments.of("constant read by a constructor", List.of("Z(null), A(null);", "E(E p, int v) {", "use(E.A);", "}"), Map.of("Z", Set.of("A"))),
				Arguments.of("constructor parameter named like a constant", List.of("Z(null), A(null);", "E(E A, int v) {", "use(A);", "}"), Map.of()),
				Arguments.of("constant read by a static field", List.of("Z(null), A(null);", "static final Object O = A;"), Map.of()),
				Arguments.of("constant read by a static initializer", List.of("Z(null), A(null);", "static {", "use(A);", "}"), Map.of()),
				Arguments.of("constant read by a method", List.of("Z(null), A(null);", "void m() {", "use(A);", "}"), Map.of()),
				Arguments.of("constant named by a method call", List.of("B(null) {", "Object o = A();", "},", "A(null);"), Map.of()),
				Arguments.of("constant named by a case label", List.of("B(null) {", "int m(E e) {", "return switch (e) { case A -> 1; default -> 0; };", "}", "},", "A(null);"), Map.of()),
				Arguments.of("constant read in a case body", List.of("B(null) {", "Object m(E e) {", "return switch (e) { case B -> A; default -> null; };", "}", "},", "A(null);"), Map.of("B", Set.of("A")))
		);
	}

	@Nonnull
	private static List<DetailAST> constantsOf(@Nonnull List<String> body) throws Exception {
		final var lines = new ArrayList<String>();
		lines.add("enum E {");
		lines.addAll(body);
		lines.add("E(Object... p) {");
		lines.add("}");
		lines.add("}");
		final var constants = new ArrayList<DetailAST>();
		for (var member : membersOf(parse(lines))) {
			if (member.getType() == TokenTypes.ENUM_CONSTANT_DEF)
				constants.add(member);
		}
		return constants;
	}

	static Stream<Arguments> enumConstantProvider() {
		return Stream.of(
				Arguments.of("plain constant", List.of("enum E {", "A, B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("literal argument to a constructor assigning it", List.of("enum E {", "A(1);", "final int v;", "E(int v) {", "this.v = v;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a literal", List.of("enum E {", "A;", "final int v;", "E() {", "v = 3;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a literal expression", List.of("enum E {", "A;", "final int v;", "E() {", "v = (1 + 2) * 3;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a parameter to a bare field", List.of("enum E {", "A(1);", "final int v;", "E(int p) {", "v = p;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a parenthesized parameter", List.of("enum E {", "A(1);", "final int v;", "E(int p) {", "this.v = ((p));", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning to a parameter", List.of("enum E {", "A(1);", "final int v = 0;", "E(int v) {", "v = 2;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a static of its own", List.of("enum E {", "A(1);", "static int last;", "E(int v) {", "last = v;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of(
						"constructor assigning a static of an enclosing class",
						List.of("class O {", "static int last;", "enum E {", "A(1);", "E(int v) {", "last = v;", "}", "}", "}"),
						"A",
						Kind.RUNS_CODE
				),
				Arguments.of(
						"constructor assigning a parenthesized call",
						List.of("enum E {", "A;", "final int id;", "E() {", "this.id = (Ids.next());", "}", "}"),
						"A",
						Kind.RUNS_CODE
				),
				Arguments.of("constructor assigning through another object", List.of("enum E {", "A(1);", "E other;", "E(int v) {", "other.v = v;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("own constant as argument", List.of("enum E {", "A(null), B(A);", "E(E e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant qualified as argument", List.of("enum E {", "A(null), B(E.A);", "E(E e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant package-qualified as argument", List.of("package p;", "enum E {", "A(null), B(p.E.A);", "E(E e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of(
						"own constant outer-qualified as argument",
						List.of("class O {", "enum E {", "A(null), B(O.E.A);", "E(E e) {", "}", "}", "}"),
						"B",
						Kind.RUNS_CODE
				),
				Arguments.of("own constant interface-qualified as argument", List.of("interface O {", "enum E {", "A(null), B(O.E.A);", "E(E e) {", "}", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant record-qualified as argument", List.of("record O() {", "enum E {", "A(null), B(O.E.A);", "E(E e) {", "}", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant annotation-qualified as argument", List.of("@interface O {", "enum E {", "A(null), B(O.E.A);", "E(E e) {", "}", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant in parentheses as argument", List.of("enum E {", "A(null), B(((A)));", "E(E e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("constructor assigning two parameters", List.of("enum E {", "A(1, 2);", "final int v;", "final int w;", "E(int v, int w) {", "this.v = v;", "this.w = w;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of(
						"constructor assigning a parameter then running code",
						List.of("enum E {", "A(1);", "final int v;", "E(int v) {", "this.v = v;", "Registry.add(this);", "}", "}"),
						"A",
						Kind.RUNS_CODE
				),
				Arguments.of("constructor adding a parameter to a field", List.of("enum E {", "A(1);", "int v;", "E(int p) {", "v += p;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning an array element", List.of("enum E {", "A(1);", "final int[] v = new int[1];", "E(int p) {", "v[0] = p;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("method running code", List.of("enum E {", "A;", "void m() {", "Registry.add(this);", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("static field reading values()", List.of("enum E {", "A, B;", "static final E DEFAULT = values()[1];", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("method calling compareTo", List.of("enum E {", "A, B;", "boolean atLeast(E other) {", "return compareTo(other) >= 0;", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("method calling ordinal on another constant", List.of("enum E {", "A, B;", "int rank(E other) {", "return other.ordinal();", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("method referring to ordinal", List.of("enum E {", "A, B;", "static final Object RANK = (ToIntFunction<E>) E::ordinal;", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumSet range", List.of("enum E {", "A, B;", "static final Object LOW = EnumSet.range(A, B);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("qualified EnumSet of all constants", List.of("enum E {", "A, B;", "static final Object ALL = java.util.EnumSet.allOf(E.class);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("a body calling values()", List.of("enum E {", "A {", "Object first() {", "return values()[0];", "}", "},", "B", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("method calling name", List.of("enum E {", "A, B;", "String label() {", "return name();", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("field named values", List.of("enum E {", "A, B;", "static final int values = 1;", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("another set's range", List.of("enum E {", "A, B;", "static final Object R = Ranges.range(A, B);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumSet of constants", List.of("enum E {", "A, B;", "static final Object SOME = EnumSet.of(A, B);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumSet copy", List.of("enum E {", "A, B;", "static final Object SOME = EnumSet.copyOf(List.of(A, B));", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumSet complement", List.of("enum E {", "A, B;", "static final Object REST = EnumSet.complementOf(EnumSet.of(A));", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumSet with type arguments", List.of("enum E {", "A, B;", "static final Object NONE = EnumSet.<E>noneOf(E.class);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("statically imported allOf", List.of("enum E {", "A, B;", "static final Object ALL = allOf(E.class);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("statically imported range", List.of("enum E {", "A, B;", "static final Object LOW = range(A, B);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("another type's allOf", List.of("enum E {", "A, B;", "static final Object ALL = Sets.allOf(E.class);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("a set type ending in EnumSet", List.of("enum E {", "A, B;", "static final Object LOW = MyEnumSet.range(A, B);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("field selected named ordinal", List.of("enum E {", "A, B;", "static final int R = Holder.ordinal;", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("getEnumConstants", List.of("enum E {", "A, B;", "static final E FIRST = E.class.getEnumConstants()[0];", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("natural order", List.of("enum E {", "A, B;", "static final Object ORDER = Comparator.naturalOrder();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("sort", List.of("enum E {", "A, B;", "static List<E> sorted(List<E> all) {", "Collections.sort(all);", "return all;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("sorted stream", List.of("enum E {", "A, B;", "static final Object FIRST = Stream.of(B, A).sorted().findFirst();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("EnumMap created", List.of("enum E {", "A, B;", "static final Object LABELS = new EnumMap<E, String>(E.class);", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("TreeSet created", List.of("enum E {", "A, B;", "static final Object SET = new TreeSet<>(List.of(B, A));", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("qualified TreeMap created", List.of("enum E {", "A, B;", "static final Object MAP = new java.util.TreeMap<E, String>();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("another type created", List.of("enum E {", "A, B;", "static final Object MAP = new HashMap<E, String>();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("own-qualified values", List.of("enum E {", "A, B;", "static final E LAST = E.values()[1];", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("values referred to", List.of("enum E {", "A, B;", "static final Supplier<E[]> ALL = E::values;", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("a map's values", List.of("enum E {", "A, B;", "static final Object V = Map.of(1, 2).values();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("another enum's values", List.of("enum E {", "A, B;", "static final Object V = Other.values();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("ordinal of another receiver", List.of("enum E {", "A, B;", "static final int R = Other.X.ordinal();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body static initializer running code", List.of("enum E {", "A {", "static {", "Registry.add(1);", "}", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("empty body static initializer", List.of("enum E {", "A {", "static {", "}", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body static field running code", List.of("enum E {", "A {", "static final Object TAG = Ids.next();", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body static literal field", List.of("enum E {", "A {", "static int count = 1;", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body static constant", List.of("enum E {", "A {", "static final int COUNT = 1;", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body static field without a value", List.of("enum E {", "A {", "static Object tag;", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("another type's constant as argument", List.of("enum E {", "A(null), B(Other.A);", "E(Object e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("another package's same-named type as argument", List.of("package p;", "enum E {", "A(null), B(q.E.A);", "E(Object e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own static field as argument", List.of("enum E {", "A(null), B(X);", "static final Object X = 1;", "E(Object e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant read through a call as argument", List.of("enum E {", "A(null), B(A.name());", "E(Object e) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("own constant beside a literal as arguments", List.of("enum E {", "A(null, 1), B(A, 2);", "E(E e, int v) {", "}", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("parenthesized call as argument", List.of("enum E {", "A((Ids.next()));", "E(int v) {", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("lambda as argument", List.of("enum E {", "A(() -> 1);", "E(Object v) {", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("class literal as argument", List.of("enum E {", "A(E.class);", "E(Object v) {", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor assigning a field it does not declare", List.of("enum E {", "A(1);", "E(int v) {", "this.w = v;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("argument running code", List.of("enum E {", "A(Ids.next());", "E(int v) {", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor running code", List.of("enum E {", "A(1);", "E(int v) {", "Registry.add(this);", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor computing a field", List.of("enum E {", "A(1);", "final int v;", "E(int v) {", "this.v = v + 1;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor writing another type's field", List.of("enum E {", "A(1);", "E(int v) {", "Other.count = v;", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("constructor delegating", List.of("enum E {", "A;", "E() {", "this(1);", "}", "E(int v) {", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of(
						"constructor registering every constant",
						List.of("enum E {", "AUTHOR, TITLE;", "final int index;", "E() {", "index = Csv.register(this);", "}", "}"),
						"TITLE",
						Kind.RUNS_CODE
				),
				Arguments.of("instance initializer running code", List.of("enum E {", "A;", "{", "Registry.add(this);", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("empty instance initializer", List.of("enum E {", "A;", "{", "}", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("instance field running code", List.of("enum E {", "A;", "final Object o = Ids.next();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("instance field holding a class literal", List.of("enum E {", "A;", "final Object o = E.class;", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("instance field running nothing", List.of("enum E {", "A;", "final int[] o = {1};", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("static field running code", List.of("enum E {", "A;", "static final Object O = Ids.next();", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("body running code", List.of("enum E {", "A {", "int x = Ids.next();", "},", "B", "}"), "A", Kind.RUNS_CODE),
				Arguments.of("sibling of a body running code", List.of("enum E {", "A {", "int x = Ids.next();", "},", "B", "}"), "B", Kind.RUNS_CODE),
				Arguments.of("empty body", List.of("enum E {", "A {", "},", "B", "}"), "A", Kind.RUNS_CODE)
		);
	}

	static Stream<Arguments> enumKeepsOrderProvider() {
		return Stream.of(
				Arguments.of("readers sorted ahead of what they read", "ROOT(null), ELECTRONICS(ROOT), BOOKS(ROOT);", "BOOKS(ROOT), ELECTRONICS(ROOT), ROOT(null);", false),
				Arguments.of("readers sorted after what they read", "ROOT(null), ELECTRONICS(ROOT), BOOKS(ROOT);", "ROOT(null), BOOKS(ROOT), ELECTRONICS(ROOT);", false),
				Arguments.of("qualified readers sorted ahead of what they read", "ROOT(null), ELECTRONICS(E.ROOT), BOOKS(E.ROOT);", "BOOKS(E.ROOT), ELECTRONICS(E.ROOT), ROOT(null);", false),
				Arguments.of("qualified forward reader swapped", "B(E.A), A(null);", "A(null), B(E.A);", false),
				Arguments.of("bare forward reader moved below what it reads", "B(A), A(null);", "A(null), B(A);", false),
				Arguments.of("constants reading nothing swapped", "B(null), A(null);", "A(null), B(null);", false)
		);
	}

	static Stream<Arguments> fieldDependenciesProvider() {
		return Stream.of(
				Arguments.of("bare read of a later field", List.of("int a = b + 1;", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("bare read of an earlier field", List.of("int b = 2;", "int a = b + 1;"), Map.of("a", Set.of("b"))),
				Arguments.of("qualified read", List.of("int a = this.b + 1;", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("read through the own class's name", List.of("static int a = T.b;", "static int b;"), Map.of("a", Set.of("b"))),
				Arguments.of("read through the outer instance", List.of("int a = T.this.b;", "int b;"), Map.of("a", Set.of("b"))),
				Arguments.of("member of another type", List.of("int a = Other.b;", "int b;"), Map.of()),
				Arguments.of("member of the superclass", List.of("int a = super.b;", "int b;"), Map.of()),
				Arguments.of("assignment target", List.of("int a = (b = 5) + 1;", "int b;"), Map.of("a", Set.of("b"))),
				Arguments.of("assigned value", List.of("int a = (c = b) + 1;", "int b = 2;", "int c;"), Map.of("a", Set.of("b", "c"))),
				Arguments.of("anonymous class body", List.of("Object a = new Object() { int c = b; };", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("anonymous class field beside a read", List.of("Object a = List.of(new Object() { int rate = 2; }, rate);", "int rate;"), Map.of("a", Set.of("rate"))),
				Arguments.of("constructor argument", List.of("Object a = new StringBuilder(b);", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("type created named like a field", List.of("Object a = new b();", "int b;"), Map.of()),
				Arguments.of("array dimension", List.of("int[] a = new int[b];", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("array initializer", List.of("int[] a = new int[] {b};", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("call receiver", List.of("Object a = b.trim();", "String b = \"x\";"), Map.of("a", Set.of("b"))),
				Arguments.of("call argument", List.of("Object a = List.of(b);", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("method named like a later field", List.of("int a = b();", "int b;"), Map.of()),
				Arguments.of("argument named like the method called", List.of("int a = b(b);", "int b;"), Map.of("a", Set.of("b"))),
				Arguments.of("method of a field named like a later field", List.of("int a = x.b();", "T x;", "int b;"), Map.of("a", Set.of("x"))),
				Arguments.of("selection from a later field", List.of("int a = x.b;", "T x;", "int b;"), Map.of("a", Set.of("x"))),
				Arguments.of("method reference receiver", List.of("Runnable a = b::run;", "Runnable b = null;"), Map.of("a", Set.of("b"))),
				Arguments.of("method referred to named like a field", List.of("Runnable a = T::b;", "int b;"), Map.of()),
				Arguments.of("lambda body", List.of("Runnable a = () -> System.out.println(b);", "int b = 2;"), Map.of("a", Set.of("b"))),
				Arguments.of("switch selector", List.of("int a = switch (b) { case 1 -> 2; default -> 3; };", "int b = 1;"), Map.of("a", Set.of("b"))),
				Arguments.of("case label named like a field", List.of("int a = switch (c) { case RED -> 1; default -> 0; };", "Object c;", "int RED;"), Map.of("a", Set.of("c"))),
				Arguments.of("case label of a list named like a field", List.of("int a = switch (c) { case BLUE, RED -> 1; default -> 0; };", "Object c;", "int RED;"), Map.of("a", Set.of("c"))),
				Arguments.of("colon case label named like a field", List.of("int a = switch (c) { case RED: yield 1; default: yield 0; };", "Object c;", "int RED;"), Map.of("a", Set.of("c"))),
				Arguments.of("case label naming a constant variable", List.of("int a = switch (c) { case MAX -> 1; default -> 0; };", "int c;", "static final int MAX = 5;"), Map.of("a", Set.of("c", "MAX"))),
				Arguments.of("case label naming a field set at runtime", List.of("int a = switch (c) { case RED -> 1; default -> 0; };", "int c;", "final int RED = Integer.parseInt(\"1\");"), Map.of("a", Set.of("c"))),
				Arguments.of("parenthesized case label", List.of("int a = switch (c) { case (RED) -> 1; default -> 0; };", "int c;", "int RED;"), Map.of("a", Set.of("c", "RED"))),
				Arguments.of("case body read named like a field", List.of("int a = switch (c) { case RED -> RED; default -> 0; };", "Object c;", "int RED;"), Map.of("a", Set.of("c", "RED"))),
				Arguments.of("cast type named like a later field", List.of("Object a = (b) null;", "int b;"), Map.of()),
				Arguments.of("class literal named like a field", List.of("Object a = URL.class;", "String URL;"), Map.of()),
				Arguments.of("annotation element named like a field", List.of("Runnable a = () -> { @Named(name = \"x\") int q = 0; };", "String name;"), Map.of()),
				Arguments.of("label named like a field", List.of("Runnable a = () -> { outer: for (;;) break outer; };", "int outer;"), Map.of()),
				Arguments.of("lambda parameter named like a later field", List.of("DoubleUnaryOperator a = rate -> rate * 0.9;", "double rate = 1;"), Map.of()),
				Arguments.of("lambda parameter named otherwise", List.of("DoubleUnaryOperator a = x -> x * rate;", "double rate = 1;"), Map.of("a", Set.of("rate"))),
				Arguments.of("parenthesized lambda parameter", List.of("DoubleUnaryOperator a = (rate) -> rate * 0.9;", "double rate = 1;"), Map.of()),
				Arguments.of("typed lambda parameter", List.of("DoubleUnaryOperator a = (double rate) -> rate * 0.9;", "double rate = 1;"), Map.of()),
				Arguments.of("second lambda parameter named like a later field", List.of("DoubleBinaryOperator a = (x, rate) -> rate * x;", "double rate = 1;"), Map.of()),
				Arguments.of("outer lambda parameter read in a nested lambda", List.of("Function<Object, Supplier<Object>> a = rate -> () -> rate;", "Object rate;"), Map.of()),
				Arguments.of(
						"field read beside a lambda binding its name",
						List.of("Object a = List.of(rate, (DoubleUnaryOperator) rate -> rate);", "double rate = 1;"),
						Map.of("a", Set.of("rate"))
				),
				Arguments.of(
						"lambda sibling binding the name",
						List.of("Object a = List.of((DoubleUnaryOperator) rate -> rate, rate);", "double rate = 1;"),
						Map.of("a", Set.of("rate"))
				),
				Arguments.of("lambda block local", List.of("Supplier<Object> a = () -> { int rate = 2; return rate; };", "int rate = 1;"), Map.of()),
				Arguments.of("lambda block read ahead of a local", List.of("Supplier<Object> a = () -> { use(rate); int rate = 2; return rate; };", "int rate = 1;"), Map.of("a", Set.of("rate"))),
				Arguments.of("local of a nested block", List.of("Supplier<Object> a = () -> { { int rate = 2; } return rate; };", "int rate = 1;"), Map.of("a", Set.of("rate"))),
				Arguments.of("local class method parameter", List.of("Runnable a = () -> { class R { int m(int rate) { return rate; } } };", "int rate;"), Map.of()),
				Arguments.of(
						"local class reading a field",
						List.of("IntSupplier a = () -> { class Reader { int read() { return zeta; } } return new Reader().read(); };", "int zeta = 5;"),
						Map.of("a", Set.of("zeta"))
				),
				Arguments.of("pattern variable", List.of("boolean a = o instanceof String rate && rate.isEmpty();", "Object o;", "String rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of("pattern variable named otherwise", List.of("boolean a = o instanceof String s && rate.isEmpty();", "Object o;", "String rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of("pattern variable read where its test failed", List.of("boolean a = o instanceof String rate || rate.isEmpty();", "Object o;", "String rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of(
						"pattern variable of an earlier statement",
						List.of("Function<Object, String> a = o -> { if (o instanceof Handler handler) return handler.name(); return handler.name(); };", "Handler handler;"),
						Map.of("a", Set.of("handler"))
				),
				Arguments.of("pattern variable of a ternary's other branch", List.of("Function<Object, String> a = o -> o instanceof String name ? name : name;", "String name;"), Map.of("a", Set.of("name"))),
				Arguments.of("record pattern component", List.of("boolean a = o instanceof R(var rate) && rate > 0;", "Object o;", "int rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of("switch pattern", List.of("int a = switch (o) { case Integer rate -> rate; default -> 0; };", "Object o;", "int rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of("guarded switch pattern", List.of("int a = switch (o) { case Integer rate when rate > 0 -> rate; default -> 0; };", "Object o;", "int rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of("switch pattern of a colon case", List.of("int a = switch (o) { case Integer rate: yield rate; default: yield 0; };", "Object o;", "int rate;"), Map.of("a", Set.of("o", "rate"))),
				Arguments.of(
						"switch pattern of a sibling rule",
						List.of("int a = switch (o) { case Integer zeta -> 1; case String s -> zeta.length(); default -> 0; };", "Object o;", "String zeta;"),
						Map.of("a", Set.of("o", "zeta"))
				),
				Arguments.of("switch arm local", List.of("int a = switch (o) { default -> { int rate = 2; yield rate; } };", "Object o;", "int rate;"), Map.of("a", Set.of("o"))),
				Arguments.of("for-each variable", List.of("Runnable a = () -> { for (int rate : arr) use(rate); };", "int[] arr;", "int rate;"), Map.of("a", Set.of("arr"))),
				Arguments.of("for variable after its loop", List.of("Runnable a = () -> { for (int i = 0; i < 1; ++i) use(i); use(i); };", "int i;"), Map.of("a", Set.of("i"))),
				Arguments.of("catch parameter", List.of("Runnable a = () -> { try { use(); } catch (Exception rate) { use(rate); } };", "int rate;"), Map.of()),
				Arguments.of("resource variable", List.of("Runnable a = () -> { try (AutoCloseable rate = open()) { use(rate); } };", "int rate;"), Map.of()),
				Arguments.of("resource variable read in a catch", List.of("Runnable a = () -> { try (AutoCloseable rate = open()) { } catch (Exception e) { use(rate); } };", "int rate;"), Map.of("a", Set.of("rate"))),
				Arguments.of("existing variable as a resource", List.of("Runnable a = () -> { try (rate) { use(); } };", "AutoCloseable rate;"), Map.of("a", Set.of("rate"))),
				Arguments.of("lambda parameter beside a this-qualified read of its name", List.of("DoubleUnaryOperator a = rate -> this.rate * rate;", "double rate = 1;"), Map.of("a", Set.of("rate"))),
				Arguments.of("for variable read inside its loop", List.of("Runnable a = () -> { for (int i = 0; i < 1; ++i) use(i); };", "int i;"), Map.of()),
				Arguments.of("multi-variable local", List.of("Supplier<Object> a = () -> { int x = 1, rate = 2; return rate; };", "int rate = 1;"), Map.of()),
				Arguments.of("catch parameter's name read in the try block", List.of("Runnable a = () -> { try { use(rate); } catch (Exception rate) { } };", "int rate;"), Map.of("a", Set.of("rate"))),
				Arguments.of(
						"multi-catch parameter",
						List.of("Runnable a = () -> { try { use(); } catch (IllegalStateException | IllegalArgumentException rate) { use(rate); } };", "int rate;"),
						Map.of()
				),
				Arguments.of("for-each variable read after its loop", List.of("Runnable a = () -> { for (int rate : arr) use(rate); use(rate); };", "int[] arr;", "int rate;"), Map.of("a", Set.of("arr", "rate"))),
				Arguments.of("for init variable read by a later one", List.of("Runnable a = () -> { for (int i = 0, rate = i; rate < 1; ++rate) use(rate); };", "int i;", "int rate;"), Map.of()),
				Arguments.of("resource read by a later resource", List.of("Runnable a = () -> { try (AutoCloseable rate = open(); AutoCloseable b = wrap(rate)) { } };", "int rate;"), Map.of()),
				Arguments.of("resource variable read in a finally", List.of("Runnable a = () -> { try (AutoCloseable rate = open()) { } finally { use(rate); } };", "int rate;"), Map.of("a", Set.of("rate"))),
				Arguments.of("label of a continue named like a field", List.of("Runnable a = () -> { outer: for (;;) continue outer; };", "int outer;"), Map.of()),
				Arguments.of("type argument named like a field", List.of("Object a = List.<b>of();", "int b;"), Map.of()),
				Arguments.of("qualified type created named like a field", List.of("Object a = new b.Inner();", "int b;"), Map.of()),
				Arguments.of("annotation value naming a field", List.of("Runnable a = () -> { @SuppressWarnings(b) int q = 0; };", "String b;"), Map.of()),
				Arguments.of("type annotation of a created object named like a field", List.of("Object a = new @b Object();", "int b;"), Map.of()),
				Arguments.of("local class named like a field", List.of("Runnable a = () -> { class b { } };", "int b;"), Map.of()),
				Arguments.of("local class constructor named like a field", List.of("Runnable a = () -> { class b { b() { } } };", "int b;"), Map.of()),
				Arguments.of("local enum named like a field", List.of("Runnable a = () -> { enum b { X } };", "int b;"), Map.of()),
				Arguments.of("local enum constant named like a field", List.of("Runnable a = () -> { enum M { b } };", "int b;"), Map.of()),
				Arguments.of("local interface named like a field", List.of("Runnable a = () -> { interface b { } };", "int b;"), Map.of()),
				Arguments.of("local record named like a field", List.of("Runnable a = () -> { record b() { } };", "int b;"), Map.of()),
				Arguments.of("local record component named like a field", List.of("Runnable a = () -> { record R(int b) { } };", "int b;"), Map.of()),
				Arguments.of("local class method named like a field", List.of("Runnable a = () -> { class R { int b() { return 1; } } };", "int b;"), Map.of()),
				Arguments.of("annotation type and element named like fields", List.of("Runnable a = () -> { class R { @interface b { int c(); } } };", "int b;", "int c;"), Map.of()),
				Arguments.of("pattern variable declared but never read", List.of("boolean a = o instanceof String b;", "Object o;", "int b;"), Map.of("a", Set.of("o"))),
				Arguments.of("method of the outer instance named like a later field", List.of("int a = T.this.b();", "int b;"), Map.of()),
				Arguments.of("member of the outer superclass", List.of("int a = T.super.b;", "int b;"), Map.of()),
				Arguments.of("outer instance of another class", List.of("int a = Other.this.b;", "int b;"), Map.of()),
				Arguments.of("another nested type of the own simple name", List.of("static int a = Tree.T.b;", "static int b;"), Map.of()),
				Arguments.of(
						"sibling anonymous class's field",
						List.of("Object a = List.of(new Object() { int b = 1; }, new Object() { int c = b; });", "int b;"),
						Map.of("a", Set.of("b"))
				),
				Arguments.of("local class field read by its method", List.of("Runnable a = () -> { class R { int b; int m() { return b; } } };", "int b;"), Map.of("a", Set.of("b"))),
				Arguments.of("local record component read by its constructor", List.of("Runnable a = () -> { record R(int b) { R { use(b); } } };", "int b;"), Map.of("a", Set.of("b")))
		);
	}

	@Nonnull
	private static List<DetailAST> fieldsOf(@Nonnull String... lines) throws Exception {
		final var unit = parse(List.of(lines));
		final var classDef = unit.findFirstToken(TokenTypes.CLASS_DEF);
		final var type = classDef != null ? classDef : unit.findFirstToken(TokenTypes.INTERFACE_DEF);
		final var fields = new ArrayList<DetailAST>();
		for (var child = type.findFirstToken(TokenTypes.OBJBLOCK).getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				fields.add(child);
		}
		return fields;
	}

	static Stream<Arguments> initializerProvider() {
		return Stream.of(
				Arguments.of("int a;", Kind.INERT),
				Arguments.of("final int a = 7;", Kind.INERT),
				Arguments.of("final String s = \"a\" + \"b\";", Kind.INERT),
				Arguments.of("final java.lang.String s = \"w\";", Kind.INERT),
				Arguments.of("final String tag = \"session:\" + this;", Kind.RUNS_CODE),
				Arguments.of("final String s = \"a\" + int.class;", Kind.RUNS_CODE),
				Arguments.of("final String s = \"a\" + new int[0];", Kind.LITERAL),
				Arguments.of("int a = 7;", Kind.LITERAL),
				Arguments.of("char c = 'c';", Kind.LITERAL),
				Arguments.of("double d = 1.5;", Kind.LITERAL),
				Arguments.of("float f = 1f;", Kind.LITERAL),
				Arguments.of("long l = 0x1FL;", Kind.LITERAL),
				Arguments.of("boolean f = true;", Kind.LITERAL),
				Arguments.of("boolean f = false;", Kind.LITERAL),
				Arguments.of("final String s = null;", Kind.LITERAL),
				Arguments.of("final String s = \"a\" + null;", Kind.LITERAL),
				Arguments.of("final String s = true ? \"a\" : null;", Kind.LITERAL),
				Arguments.of("final int a = 1 / 2;", Kind.INERT),
				Arguments.of("final int a = 0 / 1;", Kind.INERT),
				Arguments.of("final int a = 1 / 0;", Kind.RUNS_CODE),
				Arguments.of("final int a = 7 % 0;", Kind.RUNS_CODE),
				Arguments.of("long a = 7 % (-0L);", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / (0);", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 0x0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 0_0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / +0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 10;", Kind.LITERAL),
				Arguments.of("int a = 1 / 0b1;", Kind.LITERAL),
				Arguments.of("int a = 1 / -(2);", Kind.LITERAL),
				Arguments.of("int[] a = new int[0];", Kind.LITERAL),
				Arguments.of("int[] a = new int[65536];", Kind.LITERAL),
				Arguments.of("int[] a = new int[65537];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[0x10000];", Kind.LITERAL),
				Arguments.of("int[] a = new int[0x10001];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[0200000];", Kind.LITERAL),
				Arguments.of("int[] a = new int[0200001];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[0b10000000000000000];", Kind.LITERAL),
				Arguments.of("int[] a = new int[0b10000000000000001];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[65_536];", Kind.LITERAL),
				Arguments.of("int[] a = new int[0xFFFFFFFF];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[-1];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[+1];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[(1)];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[1 + 1];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int['a'];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[2][3];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[2][];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[2][65537];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[65537][2];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[2][-1];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[256][256];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[256][257];", Kind.RUNS_CODE),
				Arguments.of("int[][][] a = new int[16][64][64];", Kind.LITERAL),
				Arguments.of("int[][][] a = new int[16][64][65];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[0][65537];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[65537][0];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[65536][0];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[0][0x7FFFFFFF];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[0][0xFFFFFFFF];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[0][037777777777];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[0B1];", Kind.LITERAL),
				Arguments.of("int[][] a = new int[0][99999999999999999999];", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[2][0x7FFFFFFFFFFFFFFF];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[99999999999999999999];", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[] {1, 2};", Kind.LITERAL),
				Arguments.of("int[][] a = new int[][] {{1}, {2}};", Kind.LITERAL),
				Arguments.of("double d = 1.0 / 0.0;", Kind.LITERAL),
				Arguments.of("int a = 1 / 0b0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 0x1;", Kind.LITERAL),
				Arguments.of("int a = 1 / -0;", Kind.RUNS_CODE),
				Arguments.of("long a = 1 / 0L;", Kind.RUNS_CODE),
				Arguments.of("long a = 1 / 0l;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 0X0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 0B0;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / 00;", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / -3;", Kind.LITERAL),
				Arguments.of("int a = 1 / 010;", Kind.LITERAL),
				Arguments.of("float f = 1f / 0f;", Kind.LITERAL),
				Arguments.of("double d = 1 / 2.0;", Kind.LITERAL),
				Arguments.of("int a = 1 / (1 - 1);", Kind.RUNS_CODE),
				Arguments.of("final int a = 1 / (1 - 1);", Kind.RUNS_CODE),
				Arguments.of("final int a = 1 / (2 + 3);", Kind.RUNS_CODE),
				Arguments.of("int a = 1 / '\\0';", Kind.RUNS_CODE),
				Arguments.of("int a = 1 % (int) 0.0;", Kind.RUNS_CODE),
				Arguments.of("double d = 1.0 / (0.5 - 0.5);", Kind.RUNS_CODE),
				Arguments.of("Object[] a = new java.lang.Object[2];", Kind.LITERAL),
				Arguments.of("Object o = null;", Kind.LITERAL),
				Arguments.of("String s = \"a\" + 1;", Kind.LITERAL),
				Arguments.of("int a = -7 * 2 + (3 << 1) % 5 / 1 - (8 >> 1) + (8 >>> 1);", Kind.LITERAL),
				Arguments.of("int a = ~1 & 3 | 4 ^ 5;", Kind.LITERAL),
				Arguments.of("boolean f = !true && 2 > 1 || 1 < 2 && 1 >= 1 && 1 <= 1 && 1 == 1 && 1 != 2;", Kind.LITERAL),
				Arguments.of("int a = true ? +1 : 2;", Kind.LITERAL),
				Arguments.of("long l = (long) 3;", Kind.LITERAL),
				Arguments.of("Object o = (Object) \"x\";", Kind.LITERAL),
				Arguments.of("Object o = ((Object) ((\"x\")));", Kind.LITERAL),
				Arguments.of("Object o = (java.io.Serializable) \"x\";", Kind.LITERAL),
				Arguments.of("Object[] a = new Object[] {\"a\", null};", Kind.LITERAL),
				Arguments.of("String[][] a = {{\"a\"}, {}};", Kind.LITERAL),
				Arguments.of("int a = ((Widget) register(\"clock\", 120)).width;", Kind.RUNS_CODE),
				Arguments.of("int a = (Ids.next());", Kind.RUNS_CODE),
				Arguments.of("int a = ((b));", Kind.RUNS_CODE),
				Arguments.of("int a = 1 + (b);", Kind.RUNS_CODE),
				Arguments.of("int a = switch ((Ids.next())) { default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (1) { default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("boolean f = null instanceof String;", Kind.RUNS_CODE),
				Arguments.of("int a = (new int[] {1})[0];", Kind.RUNS_CODE),
				Arguments.of("int a = new int[1].length;", Kind.RUNS_CODE),
				Arguments.of("Object o = new Object[] {new Object()};", Kind.RUNS_CODE),
				Arguments.of("Object o = (Runnable & java.io.Serializable) null;", Kind.RUNS_CODE),
				Arguments.of("int a = this.new Inner().x;", Kind.RUNS_CODE),
				Arguments.of("int a = RATE;", Kind.RUNS_CODE),
				Arguments.of("final int a = b + 1;", Kind.RUNS_CODE),
				Arguments.of("final Object o = \"w\";", Kind.LITERAL),
				Arguments.of("final int[] a = {1, 2};", Kind.LITERAL),
				Arguments.of("int a = -b * 2 + (b << 1);", Kind.RUNS_CODE),
				Arguments.of("boolean f = !flag && b > 1;", Kind.RUNS_CODE),
				Arguments.of("String s = \"v\" + count;", Kind.RUNS_CODE),
				Arguments.of("int i = (int) ratio;", Kind.RUNS_CODE),
				Arguments.of("int a = flag ? 1 : 2;", Kind.RUNS_CODE),
				Arguments.of("int a = b;", Kind.RUNS_CODE),
				Arguments.of("int a = this.b;", Kind.RUNS_CODE),
				Arguments.of("int a = T.this.b;", Kind.RUNS_CODE),
				Arguments.of("int a = T.b;", Kind.RUNS_CODE),
				Arguments.of("int a = other.b;", Kind.RUNS_CODE),
				Arguments.of("int a = super.b;", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[3];", Kind.LITERAL),
				Arguments.of("int[] a = new int[] {b, 2};", Kind.RUNS_CODE),
				Arguments.of("Class<?> c = String.class;", Kind.RUNS_CODE),
				Arguments.of("Runnable r = () -> {};", Kind.RUNS_CODE),
				Arguments.of("Runnable r = () -> System.out.println(this);", Kind.RUNS_CODE),
				Arguments.of("Runnable r = T::staticRun;", Kind.RUNS_CODE),
				Arguments.of("Object r = (Runnable) () -> {};", Kind.RUNS_CODE),
				Arguments.of("Object r = flag ? (Runnable) () -> {} : null;", Kind.RUNS_CODE),
				Arguments.of("Runnable[] rs = {() -> {}};", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(1);", Kind.RUNS_CODE),
				Arguments.of("Object l = java.util.List.of(1);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(this.count);", Kind.RUNS_CODE),
				Arguments.of("Object s = Set.of(1);", Kind.RUNS_CODE),
				Arguments.of("Object m = Map.of(1, 2);", Kind.RUNS_CODE),
				Arguments.of("Object d = new ArrayDeque<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new java.util.ArrayList<>();", Kind.RUNS_CODE),
				Arguments.of("Object m = new java.util.HashMap<String, Integer>();", Kind.RUNS_CODE),
				Arguments.of("Object m = new HashMap<>(16);", Kind.RUNS_CODE),
				Arguments.of("Object s = new HashSet<>();", Kind.RUNS_CODE),
				Arguments.of("Object m = new LinkedHashMap<>();", Kind.RUNS_CODE),
				Arguments.of("Object s = new LinkedHashSet<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new LinkedList<>();", Kind.RUNS_CODE),
				Arguments.of("Object m = new TreeMap<>();", Kind.RUNS_CODE),
				Arguments.of("Object s = new TreeSet<>();", Kind.RUNS_CODE),
				Arguments.of("Object sb = new StringBuilder(\"x\");", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>(other);", Kind.RUNS_CODE),
				Arguments.of("String s = String.valueOf(3);", Kind.RUNS_CODE),
				Arguments.of("Object v = Boolean.valueOf(true);", Kind.RUNS_CODE),
				Arguments.of("Object v = Byte.valueOf((byte) 1);", Kind.RUNS_CODE),
				Arguments.of("Object v = Character.valueOf('c');", Kind.RUNS_CODE),
				Arguments.of("Object v = Double.valueOf(1.0);", Kind.RUNS_CODE),
				Arguments.of("Object v = Float.valueOf(1f);", Kind.RUNS_CODE),
				Arguments.of("Object v = Integer.valueOf(1);", Kind.RUNS_CODE),
				Arguments.of("Object v = Long.valueOf(1L);", Kind.RUNS_CODE),
				Arguments.of("Object v = Short.valueOf((short) 1);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.max(1, 2);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.abs(b);", Kind.RUNS_CODE),
				Arguments.of("Object p = Pattern.compile(\"a+\");", Kind.RUNS_CODE),
				Arguments.of("Object p = Pattern.compile(regex);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.ofSeconds(5);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.of(5, ChronoUnit.SECONDS);", Kind.RUNS_CODE),
				Arguments.of("Object i = Instant.ofEpochMilli(1);", Kind.RUNS_CODE),
				Arguments.of("Object d = LocalDate.of(2020, 1, 1);", Kind.RUNS_CODE),
				Arguments.of("Object d = LocalDateTime.of(2020, 1, 1, 0, 0);", Kind.RUNS_CODE),
				Arguments.of("Object t = LocalTime.of(1, 2);", Kind.RUNS_CODE),
				Arguments.of("Object p = Period.ofDays(1);", Kind.RUNS_CODE),
				Arguments.of("int a = switch (b) { case 1 -> 3; default -> 4; };", Kind.RUNS_CODE),
				Arguments.of("int a = (b = 5) + 1;", Kind.RUNS_CODE),
				Arguments.of("boolean f = other instanceof T;", Kind.RUNS_CODE),
				Arguments.of("int a = arr[0];", Kind.RUNS_CODE),
				Arguments.of("int a = arr.length;", Kind.RUNS_CODE),
				Arguments.of("double m = Math.ceil(1.5);", Kind.RUNS_CODE),
				Arguments.of("double m = Math.floor(1.5);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.floorDiv(7, 2);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.floorMod(7, 2);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.min(1, 2);", Kind.RUNS_CODE),
				Arguments.of("double m = Math.pow(2, 3);", Kind.RUNS_CODE),
				Arguments.of("long m = Math.round(1.5);", Kind.RUNS_CODE),
				Arguments.of("double m = Math.sqrt(4);", Kind.RUNS_CODE),
				Arguments.of("int m = Math.toIntExact(4L);", Kind.RUNS_CODE),
				Arguments.of("Object d = java.time.Duration.ofSeconds(5);", Kind.RUNS_CODE),
				Arguments.of("String s = \"\"\"\n\t\ttext\n\t\t\"\"\";", Kind.LITERAL),
				Arguments.of("long l = (long) b;", Kind.RUNS_CODE),
				Arguments.of("Object o = (Integer) boxed;", Kind.RUNS_CODE),
				Arguments.of("Object o = (String) label;", Kind.RUNS_CODE),
				Arguments.of("Object o = (java.lang.String) label;", Kind.RUNS_CODE),
				Arguments.of("String s = \"v\" + other;", Kind.RUNS_CODE),
				Arguments.of("int a = T.SIZE;", Kind.RUNS_CODE),
				Arguments.of("Object o = System.out;", Kind.RUNS_CODE),
				Arguments.of("int a = Integer.MAX_VALUE;", Kind.RUNS_CODE),
				Arguments.of("Object u = TimeUnit.SECONDS;", Kind.RUNS_CODE),
				Arguments.of("Object u = java.util.concurrent.TimeUnit.SECONDS;", Kind.RUNS_CODE),
				Arguments.of("int a = other.other.b;", Kind.RUNS_CODE),
				Arguments.of("Object r = other::hashCode;", Kind.RUNS_CODE),
				Arguments.of("Object r = this.label::length;", Kind.RUNS_CODE),
				Arguments.of("Object r = System.out::println;", Kind.RUNS_CODE),
				Arguments.of("Object r = com.x.Foo::m;", Kind.RUNS_CODE),
				Arguments.of("Object r = java.util.List<String>::size;", Kind.RUNS_CODE),
				Arguments.of("Object r = Map.Entry::getKey;", Kind.RUNS_CODE),
				Arguments.of("Object r = ArrayList<String>::new;", Kind.RUNS_CODE),
				Arguments.of("Object[] a = new Object[] {label, null};", Kind.RUNS_CODE),
				Arguments.of("int[][] a = new int[2][b];", Kind.RUNS_CODE),
				Arguments.of("int a = switch (label) { case \"x\" -> 1; default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (this.b) { case 1, 2 -> 1; default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (5) { case SIZE -> 1; default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>(List.of(1));", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>(SIZE);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(count, \"a\" + b, (byte) 1, -ratio);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.of(count, ChronoUnit.SECONDS);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.of(5, java.time.temporal.ChronoUnit.SECONDS);", Kind.RUNS_CODE),
				Arguments.of("Object m = Map.ofEntries(Map.entry(1, 2));", Kind.RUNS_CODE),
				Arguments.of("Object e = Map.entry(\"a\", 1);", Kind.RUNS_CODE),
				Arguments.of("Object p = Path.of(\"a\", \"b\");", Kind.RUNS_CODE),
				Arguments.of("Object p = Paths.get(\"a\");", Kind.RUNS_CODE),
				Arguments.of("Object m = new ConcurrentHashMap<>();", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicInteger();", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicInteger(1);", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicLong(1L);", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicBoolean(true);", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicReference<>(\"x\");", Kind.RUNS_CODE),
				Arguments.of("Object t = new ThreadLocal<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new @Deprecated ArrayList<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new <String> ArrayList<>();", Kind.RUNS_CODE),
				Arguments.of("Object s = new TreeSet<>(List.of(1));", Kind.RUNS_CODE),
				Arguments.of("Object p = Pattern.compile(\"x\", Pattern.DOTALL);", Kind.RUNS_CODE),
				Arguments.of("String s = String.valueOf(Integer.MAX_VALUE);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(java.util.Locale.ROOT, Long.MIN_VALUE);", Kind.RUNS_CODE),
				Arguments.of("final int a = Integer.parseInt(\"7\");", Kind.RUNS_CODE),
				Arguments.of("int i = (int) Ids.next();", Kind.RUNS_CODE),
				Arguments.of("int a = flag ? 1 : Ids.next();", Kind.RUNS_CODE),
				Arguments.of("Object self = this;", Kind.RUNS_CODE),
				Arguments.of("Object self = T.this;", Kind.RUNS_CODE),
				Arguments.of("String s = \"v\" + this;", Kind.RUNS_CODE),
				Arguments.of("int a = super.b();", Kind.RUNS_CODE),
				Arguments.of("int a = other.size();", Kind.RUNS_CODE),
				Arguments.of("int a = this.read();", Kind.RUNS_CODE),
				Arguments.of("int a = T.read();", Kind.RUNS_CODE),
				Arguments.of("int a = read();", Kind.RUNS_CODE),
				Arguments.of("int a = T.this.read();", Kind.RUNS_CODE),
				Arguments.of("int[] a = {Ids.next()};", Kind.RUNS_CODE),
				Arguments.of("Runnable r = this::run;", Kind.RUNS_CODE),
				Arguments.of("Runnable r = T.this::run;", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(() -> {});", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of((Runnable) () -> {});", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(T::f);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(this);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(Ids.next());", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(new Object() {});", Kind.RUNS_CODE),
				Arguments.of("Object l = List.<String>of();", Kind.RUNS_CODE),
				Arguments.of("Object l = com.x.List.of(1);", Kind.RUNS_CODE),
				Arguments.of("Object l = new com.x.ArrayList<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>() {};", Kind.RUNS_CODE),
				Arguments.of("Object o = new Object();", Kind.RUNS_CODE),
				Arguments.of("Object o = new Thread();", Kind.RUNS_CODE),
				Arguments.of("Object o = new T();", Kind.RUNS_CODE),
				Arguments.of("Object o = outer.new Inner();", Kind.RUNS_CODE),
				Arguments.of("Object b = new StringBuilder().append(\"x\");", Kind.RUNS_CODE),
				Arguments.of("int n = List.of(1).size();", Kind.RUNS_CODE),
				Arguments.of("String s = valueOf(3);", Kind.RUNS_CODE),
				Arguments.of("int m = StrictMath.max(1, 2);", Kind.RUNS_CODE),
				Arguments.of("double r = Math.random();", Kind.RUNS_CODE),
				Arguments.of("boolean m = Pattern.matches(\"a\", label);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.parse(\"PT5S\");", Kind.RUNS_CODE),
				Arguments.of("Object d = LocalDate.now();", Kind.RUNS_CODE),
				Arguments.of("Object k = Stream.of(\"a\").map(T::normalize).toList();", Kind.RUNS_CODE),
				Arguments.of("Object s = new TreeSet<>((a, b) -> 0);", Kind.RUNS_CODE),
				Arguments.of("Object s = new TreeSet<>(String::compareTo);", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>(Ids.all());", Kind.RUNS_CODE),
				Arguments.of("int[] a = new int[Ids.next()];", Kind.RUNS_CODE),
				Arguments.of("Object[] a = new Object[] {Ids.next()};", Kind.RUNS_CODE),
				Arguments.of("Iterable<?> r = this.items()::iterator;", Kind.RUNS_CODE),
				Arguments.of("Runnable r = Ids.make()::run;", Kind.RUNS_CODE),
				Arguments.of("Runnable r = new Task()::run;", Kind.RUNS_CODE),
				Arguments.of("Object r = new Object() {}::toString;", Kind.RUNS_CODE),
				Arguments.of("Object r = super::toString;", Kind.RUNS_CODE),
				Arguments.of("Object r = T.super::toString;", Kind.RUNS_CODE),
				Arguments.of("Object r = Ids.FIRST::run;", Kind.RUNS_CODE),
				Arguments.of("int a = ++b;", Kind.RUNS_CODE),
				Arguments.of("int a = b++;", Kind.RUNS_CODE),
				Arguments.of("int a = --b;", Kind.RUNS_CODE),
				Arguments.of("int a = b--;", Kind.RUNS_CODE),
				Arguments.of("int a = b += 1;", Kind.RUNS_CODE),
				Arguments.of("int a = inherited;", Kind.RUNS_CODE),
				Arguments.of("int a = Ids.FIRST;", Kind.RUNS_CODE),
				Arguments.of("int a = com.x.Ids.FIRST;", Kind.RUNS_CODE),
				Arguments.of("boolean f = other instanceof T t;", Kind.RUNS_CODE),
				Arguments.of("boolean f = other instanceof R(var x);", Kind.RUNS_CODE),
				Arguments.of("Object o = (Object) label;", Kind.RUNS_CODE),
				Arguments.of("Object o = (int[]) arr;", Kind.RUNS_CODE),
				Arguments.of("Object o = (Runnable & java.io.Serializable) () -> {};", Kind.RUNS_CODE),
				Arguments.of("Object o = (String) Ids.next();", Kind.RUNS_CODE),
				Arguments.of("int a = switch (other) { default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (boxed) { case 1 -> 1; default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (b) { case 1: yield 3; default: yield 4; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (b) { case 1 -> Ids.next(); default -> 4; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (b) { case 1 -> { yield 3; } default -> 4; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (other) { case T t -> 1; default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("int a = switch (Ids.next()) { default -> 2; };", Kind.RUNS_CODE),
				Arguments.of("Object l = new ArrayList<>(names);", Kind.RUNS_CODE),
				Arguments.of("Object sb = new StringBuilder(label);", Kind.RUNS_CODE),
				Arguments.of("Object s = new TreeSet<>(names);", Kind.RUNS_CODE),
				Arguments.of("Object m = new TreeMap<>(Map.of(1, 2).comparator());", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.of(5, unit);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(other);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(boxed);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(arr);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(T.this.label);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of((Object) 1);", Kind.RUNS_CODE),
				Arguments.of("Object p = Path.of(label);", Kind.RUNS_CODE),
				Arguments.of("Object o = new Object(1);", Kind.RUNS_CODE),
				Arguments.of("Object o = new @Deprecated Object() {};", Kind.RUNS_CODE),
				Arguments.of("Object t = new ThreadLocal<>(1);", Kind.RUNS_CODE),
				Arguments.of("Object t = ThreadLocal.withInitial(() -> 1);", Kind.RUNS_CODE),
				Arguments.of("Object l = Locale.getDefault();", Kind.RUNS_CODE),
				Arguments.of("Object i = new AtomicReference<>(other);", Kind.RUNS_CODE),
				Arguments.of("Object m = new java.util.concurrent.ConcurrentSkipListMap<>();", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(System.out);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(Integer.MaxValue);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(TokenTypes.IDENT);", Kind.RUNS_CODE),
				Arguments.of("Object l = List.of(com.x.Ids.FIRST);", Kind.RUNS_CODE),
				Arguments.of("Object d = Duration.of(5, TimeUnit.SECONDS.toChronoUnit());", Kind.RUNS_CODE)
		);
	}

	static Stream<Arguments> keepsOrderProvider() {
		return Stream.of(
				Arguments.of("pinned pair swapped", List.of("int zeta = Ids.next();", "int alpha = 1;"), List.of("int alpha = 1;", "int zeta = Ids.next();"), false),
				Arguments.of("literal moved below a later field running code", List.of("int alpha = 1;", "int zeta = Ids.next();"), List.of("int zeta = Ids.next();", "int alpha = 1;"), false),
				Arguments.of("instance reader moved above the static field it reads", List.of("static int s = Ids.next();", "int a = s + 1;"), List.of("int a = s + 1;", "static int s = Ids.next();"), true),
				Arguments.of("reader moved above what it reads", List.of("int a = 1;", "int b = a + 1;"), List.of("int b = a + 1;", "int a = 1;"), false),
				Arguments.of("reader moved below what it reads", List.of("int b = a + 1;", "int a = 1;"), List.of("int a = 1;", "int b = a + 1;"), false),
				Arguments.of("reader moved below an inert field it reads", List.of("int b = a + 1;", "int a;"), List.of("int a;", "int b = a + 1;"), true),
				Arguments.of(
						"pinned bare forward reader moved below what it reads",
						List.of("int b = a + Ids.next();", "int a = 1;"),
						List.of("int a = 1;", "int b = a + Ids.next();"),
						false
				),
				Arguments.of(
						"pinned qualified forward reader moved below what it reads",
						List.of("int b = this.a + Ids.next();", "int a = 1;"),
						List.of("int a = 1;", "int b = this.a + Ids.next();"),
						false
				),
				Arguments.of(
						"pinned pair beside a bare forward read swapped",
						List.of("int b = a + 1;", "int z = Ids.next();", "int a = 1;"),
						List.of("int z = Ids.next();", "int a = 1;", "int b = a + 1;"),
						false
				),
				Arguments.of("compiling forward read target moved up", List.of("int total = this.count * 2;", "int count = 3;"), List.of("int count = 3;", "int total = this.count * 2;"), false),
				Arguments.of("constant target moved up", List.of("int total = this.count * 2;", "final int count = 3;"), List.of("final int count = 3;", "int total = this.count * 2;"), true),
				Arguments.of(
						"other object's member target moved up",
						List.of("static T other;", "int total = other.count * 2;", "int count = 3;"),
						List.of("static T other;", "int count = 3;", "int total = other.count * 2;"),
						false
				),
				Arguments.of(
						"other object's member target without an initializer moved up",
						List.of("static T other;", "int total = other.count * 2;", "int count;"),
						List.of("static T other;", "int count;", "int total = other.count * 2;"),
						true
				),
				Arguments.of(
						"literal pair swapped",
						List.of("int zeta = 2;", "Object alpha = new int[] {1};"),
						List.of("Object alpha = new int[] {1};", "int zeta = 2;"),
						true
				),
				Arguments.of(
						"lambda binding a later field's name moved below it",
						List.of("DoubleUnaryOperator discount = rate -> rate * 0.9;", "double rate = initialRate();"),
						List.of("double rate = initialRate();", "DoubleUnaryOperator discount = rate -> rate * 0.9;"),
						false
				),
				Arguments.of(
						"this handed out moved above a field running code",
						List.of("final String user = System.getProperty(\"u\");", "final String tag = \"s\" + this;"),
						List.of("final String tag = \"s\" + this;", "final String user = System.getProperty(\"u\");"),
						false
				),
				Arguments.of("unpinned pair swapped", List.of("int zeta;", "int alpha;"), List.of("int alpha;", "int zeta;"), true),
				Arguments.of(
						"inert pair swapped around a pinned pair kept",
						List.of("int zeta;", "int y = Ids.next();", "int x = 1;", "int alpha;"),
						List.of("int alpha;", "int y = Ids.next();", "int x = 1;", "int zeta;"),
						true
				),
				Arguments.of("pinned pair across groups swapped", List.of("static int s = Ids.next();", "int i = 1;"), List.of("int i = 1;", "static int s = Ids.next();"), true),
				Arguments.of("a field dropped", List.of("int zeta;", "int alpha;"), List.of("int alpha;", "int beta;"), false),
				Arguments.of("a field duplicated", List.of("int zeta;", "int alpha;"), List.of("int alpha;", "int zeta;", "int alpha;"), false)
		);
	}

	@Nonnull
	private static Kind kindOfNamed(@Nonnull List<String> lines, @Nonnull String name) throws Exception {
		for (var member : membersOf(parse(lines))) {
			if (FieldSortingCheck.fieldName(member).equals(name))
				return FieldInitializers.kindOf(member);
		}
		throw new AssertionError("no member " + name);
	}

	/** The fields and enum constants under {@code root}, in tree order. */
	@Nonnull
	private static List<DetailAST> membersOf(@Nonnull DetailAST root) {
		final var members = new ArrayList<DetailAST>();
		final var pending = new ArrayDeque<DetailAST>();
		for (var top = root; top != null; top = top.getNextSibling())
			pending.addLast(top);
		while (!pending.isEmpty()) {
			final var node = pending.removeFirst();
			if (node.getType() == TokenTypes.ENUM_CONSTANT_DEF || node.getType() == TokenTypes.VARIABLE_DEF)
				members.add(node);
			for (var child = node.getLastChild(); child != null; child = child.getPreviousSibling())
				pending.addFirst(child);
		}
		return members;
	}

	@Nonnull
	private static BodyOrder orderOf(@Nonnull List<String> body) throws Exception {
		final var lines = new ArrayList<String>();
		lines.add("class T {");
		lines.addAll(body);
		lines.add("}");
		return FieldInitializers.bodyOrders(parse(lines)).getFirst();
	}

	static Stream<Arguments> outsideWhitelistProvider() {
		return Arrays.stream(TokenUtil.getAllTokenIds())
				.filter(type -> !WHITELIST.contains(type))
				.mapToObj(type -> Arguments.of(TokenUtil.getTokenName(type), type));
	}

	@Nonnull
	private static DetailAST parse(@Nonnull List<String> lines) throws Exception {
		return JavaParser.parse(new FileContents(new FileText(new File("T.java"), lines)));
	}

	@Nonnull
	private static List<List<Integer>> permutations(int size) {
		if (size == 0)
			return List.of(List.of());
		final var orders = new ArrayList<List<Integer>>();
		for (var shorter : permutations(size - 1)) {
			for (var at = 0; at <= shorter.size(); ++at) {
				final var order = new ArrayList<>(shorter);
				order.add(at, size - 1);
				orders.add(order);
			}
		}
		return orders;
	}

	static Stream<Arguments> untrustedNameProvider() {
		return Stream.of(
				Arguments.of("no import", List.of("class T {", "Object x = List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of("single-type import", List.of("import java.util.List;", "class T {", "Object x = List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of("java on-demand import", List.of("import java.util.*;", "class T {", "Object x = List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of("foreign single-type import", List.of("import com.x.List;", "class T {", "Object x = List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"java on-demand import beside a foreign one",
						List.of("import com.x.*;", "import java.util.*;", "class T {", "Object x = List.of(1);", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of("java.lang beside a foreign on-demand import", List.of("import com.x.*;", "class T {", "int x = Math.max(1, 2);", "}"), Kind.RUNS_CODE),
				Arguments.of("on-demand java package without the type", List.of("import java.time.*;", "class T {", "Object x = List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of("fully qualified beside a foreign on-demand import", List.of("import com.x.*;", "class T {", "Object x = java.util.List.of(1);", "}"), Kind.RUNS_CODE),
				Arguments.of("java.lang type declared in the file", List.of("class T {", "int x = Math.max(1, 2);", "}", "class Math {", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"imported type shadowed by a nested type",
						List.of("import java.util.regex.Pattern;", "class T {", "static class Pattern {", "}", "Object x = Pattern.compile(\"a\");", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of("type parameter", List.of("class T<Math> {", "int x = Math.max(1, 2);", "}"), Kind.RUNS_CODE),
				Arguments.of("java.lang constructor", List.of("class T {", "Object x = new StringBuilder();", "}"), Kind.RUNS_CODE),
				Arguments.of("java.util constructor without an import", List.of("class T {", "Object x = new ArrayList<>();", "}"), Kind.RUNS_CODE),
				Arguments.of("java.util constructor imported", List.of("import java.util.ArrayList;", "class T {", "Object x = new ArrayList<>();", "}"), Kind.RUNS_CODE),
				Arguments.of("java.lang constructor declared in the file", List.of("class T {", "Object x = new StringBuilder();", "}", "class StringBuilder {", "}"), Kind.RUNS_CODE),
				Arguments.of("static field of a JDK type", List.of("class T {", "int x = Integer.MAX_VALUE;", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"static field of a declared type named like a JDK one",
						List.of("class T {", "int x = Integer.MAX_VALUE;", "}", "class Integer {", "static int MAX_VALUE;", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of("static field of a nested type", List.of("class T {", "static class Inner {", "static int X = 1;", "}", "int x = Inner.X;", "}"), Kind.RUNS_CODE),
				Arguments.of("constant of a nested type", List.of("class T {", "static class Inner {", "static final int X = 1;", "}", "int x = Inner.X;", "}"), Kind.RUNS_CODE),
				Arguments.of("constant of a type later in the file", List.of("class T {", "int x = Ids.FIRST;", "}", "class Ids {", "static final int FIRST = 1;", "}"), Kind.RUNS_CODE),
				Arguments.of("String constant of a declared type", List.of("class T {", "Object x = Ids.NAME;", "}", "class Ids {", "static final String NAME = \"n\";", "}"), Kind.RUNS_CODE),
				Arguments.of("interface constant of a declared type", List.of("class T {", "int x = Ids.FIRST;", "}", "interface Ids {", "int FIRST = 1;", "}"), Kind.RUNS_CODE),
				Arguments.of("constant of a declared type as an argument", List.of("class T {", "Object x = Math.max(Ids.FIRST, 2);", "}", "class Ids {", "static final int FIRST = 1;", "}"), Kind.RUNS_CODE),
				Arguments.of("non-final static of a declared type", List.of("class T {", "int x = Ids.FIRST;", "}", "class Ids {", "static int FIRST = 1;", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"final static of a declared type set by a call",
						List.of("class T {", "int x = Ids.FIRST;", "}", "class Ids {", "static final int FIRST = Integer.parseInt(\"1\");", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of("final static array of a declared type", List.of("class T {", "Object x = Ids.FIRST;", "}", "class Ids {", "static final int[] FIRST = {1};", "}"), Kind.RUNS_CODE),
				Arguments.of("enum constant of a declared enum", List.of("class T {", "enum Mode {", "FAST", "}", "Object x = Mode.FAST;", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"enum constant of a declared enum as an argument",
						List.of("class T {", "enum Mode {", "FAST", "}", "Object x = List.of(Mode.FAST);", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of(
						"constant of one of two declared types sharing a name",
						List.of("class T {", "static class A {", "static final int X = 1;", "}", "int x = A.X;", "}", "class U {", "static class A {", "static int X = 1;", "}", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of(
						"constant through an outer type the file does not declare",
						List.of("class T {", "int x = Other.Inner.X;", "}", "class Inner {", "static final int X = 1;", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of("constant through a type parameter's name", List.of("class T<Ids> {", "int x = Ids.FIRST;", "}", "class Ids {", "static final int FIRST = 1;", "}"), Kind.RUNS_CODE),
				Arguments.of("static field through the own name", List.of("class T {", "static int s = 1;", "int x = T.s;", "}"), Kind.RUNS_CODE),
				Arguments.of("static field through the package-qualified own name", List.of("package p;", "class T {", "static int s = 1;", "int x = p.T.s;", "}"), Kind.RUNS_CODE),
				Arguments.of("static field through the outer-qualified own name", List.of("class T {", "static class U {", "static int s = 1;", "int x = T.U.s;", "}", "}"), Kind.RUNS_CODE),
				Arguments.of("static field through another package's name", List.of("package p;", "class T {", "static int s = 1;", "int x = q.T.s;", "}"), Kind.RUNS_CODE),
				Arguments.of("cast to a declared String", List.of("class T {", "Object label;", "Object x = (String) label;", "}", "class String {", "}"), Kind.RUNS_CODE),
				Arguments.of(
						"unit constant argument",
						List.of("import java.time.Duration;", "import java.time.temporal.ChronoUnit;", "class T {", "Object x = Duration.of(5, ChronoUnit.SECONDS);", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of(
						"unit constant argument of a declared type",
						List.of("import java.time.Duration;", "class T {", "Object x = Duration.of(5, ChronoUnit.SECONDS);", "}", "enum ChronoUnit {", "SECONDS", "}"),
						Kind.RUNS_CODE
				),
				Arguments.of(
						"unit constant argument through a foreign on-demand import",
						List.of("import com.x.*;", "import java.time.Duration;", "class T {", "Object x = Duration.of(5, ChronoUnit.SECONDS);", "}"),
						Kind.RUNS_CODE
				)
		);
	}

	@MethodSource("bareForwardReadsProvider")
	@ParameterizedTest(name = "{0}")
	public void bareForwardReads(@Nonnull String name, @Nonnull List<String> body, @Nonnull Map<String, String> expected) throws Exception {
		assertEquals(expected, FieldInitializers.bareForwardReads(constantsOf(body)));
	}

	@MethodSource("constantReadsProvider")
	@ParameterizedTest(name = "{0}")
	public void constantReads(@Nonnull String name, @Nonnull List<String> body, @Nonnull Map<String, Set<String>> expected) throws Exception {
		final var reads = new HashMap<String, Set<String>>();
		for (var entry : FieldInitializers.constantReads(constantsOf(body)).entrySet())
			reads.put(entry.getKey(), entry.getValue().keySet());
		assertEquals(expected, reads);
	}

	@MethodSource("enumKeepsOrderProvider")
	@ParameterizedTest(name = "{0}")
	public void enumKeepsOrder(@Nonnull String name, @Nonnull String before, @Nonnull String after, boolean expected) throws Exception {
		final var beforeOrder = FieldInitializers.bodyOrders(parse(List.of("enum E {", before, "E(E p) {", "}", "}"))).getFirst();
		final var afterOrder = FieldInitializers.bodyOrders(parse(List.of("enum E {", after, "E(E p) {", "}", "}"))).getFirst();
		assertEquals(expected, FieldInitializers.keepsOrder(beforeOrder, afterOrder));
	}

	@MethodSource("fieldDependenciesProvider")
	@ParameterizedTest(name = "{0}")
	public void fieldDependencies(@Nonnull String name, @Nonnull List<String> body, @Nonnull Map<String, Set<String>> expected) throws Exception {
		final var lines = new ArrayList<String>();
		lines.add("class T {");
		lines.addAll(body);
		lines.add("}");
		assertEquals(expected, FieldSortingCheck.fieldDependencies(fieldsOf(lines.toArray(String[]::new))));
	}

	@MethodSource("keepsOrderProvider")
	@ParameterizedTest(name = "{0}")
	public void keepsOrder(@Nonnull String name, @Nonnull List<String> before, @Nonnull List<String> after, boolean expected) throws Exception {
		assertEquals(expected, FieldInitializers.keepsOrder(orderOf(before), orderOf(after)));
	}

	@MethodSource("enumConstantProvider")
	@ParameterizedTest(name = "{0}")
	public void kindOfEnumConstant(@Nonnull String name, @Nonnull List<String> lines, @Nonnull String constant, @Nonnull Kind expected) throws Exception {
		assertEquals(expected, kindOfNamed(lines, constant));
	}

	@MethodSource("initializerProvider")
	@ParameterizedTest(name = "{0}")
	public void kindOfInitializer(@Nonnull String declaration, @Nonnull Kind expected) throws Exception {
		final var lines = new ArrayList<>(PREAMBLE);
		lines.addAll(List.of(declaration.split("\n")));
		lines.add("}");
		final var row = PREAMBLE.size() + 1;
		final var member = membersOf(parse(lines)).stream().filter(field -> field.getLineNo() == row).findFirst().orElseThrow();
		assertEquals(expected, FieldInitializers.kindOf(member));
	}

	@MethodSource("untrustedNameProvider")
	@ParameterizedTest(name = "{0}")
	public void kindOfUntrustedName(@Nonnull String name, @Nonnull List<String> lines, @Nonnull Kind expected) throws Exception {
		assertEquals(expected, kindOfNamed(lines, "x"));
	}

	@MethodSource("outsideWhitelistProvider")
	@ParameterizedTest(name = "{0}")
	public void runsCodeOutsideTheWhitelist(@Nonnull String name, int type) throws Exception {
		final var field = fieldsOf("class T {", "int a = 1;", "}").getFirst();
		assertEquals(Kind.LITERAL, FieldInitializers.kindOf(field), "the literal is classified before it changes type");
		final var literal = (DetailAstImpl) field.findFirstToken(TokenTypes.ASSIGN).getFirstChild().getFirstChild();
		literal.setType(type);
		assertEquals(Kind.RUNS_CODE, FieldInitializers.kindOf(field));
	}

	@Test
	public void testAnnotationTypeLiteralConstantIsInertAndStatic() throws Exception {
		final var order = FieldInitializers.bodyOrders(parse(List.of("@interface A {", "int X = 1;", "}"))).getFirst();
		assertEquals(List.of(Kind.INERT), order.kinds());
		assertEquals(List.of(true), order.statics());
	}

	@Test
	public void testAnonymousClassBoundFollowsAReaderCycle() throws Exception {
		final var fields = fieldsOf(
				"class T {",
				"Object anon = new Object() {};",
				"int first = anon.hashCode() + this.second;",
				"int second = first;",
				"int free = 1;",
				"}"
		);
		assertEquals(Set.of("anon", "first", "second"), FieldSortingCheck.anonymousClassBound(fields));
	}

	@Test
	public void testAnonymousClassBoundScalesLinearly() throws Exception {
		assertScalesLinearly(
				500,
				4000,
				size -> {
					final var lines = new ArrayList<>(List.of("class T {", "Object anon = new Object() {};", "int f0 = anon.hashCode();"));
					for (var i = 1; i < size; ++i)
						lines.add("int f" + i + " = f" + (i - 1) + " + 1;");
					lines.add("}");
					return fieldsOf(lines.toArray(String[]::new));
				},
				(size, fields) -> {
					final var start = System.nanoTime();
					assertEquals(size + 1, FieldSortingCheck.anonymousClassBound(fields).size());
					return System.nanoTime() - start;
				}
		);
	}

	@Test
	public void testBodyOrdersAreKeyedByTheirOwners() throws Exception {
		final var orders = FieldInitializers.bodyOrders(parse(List.of(
				"enum Shade {",
				"ZETA { int depth; },",
				"ALPHA { int width; };",
				"final Object pair = List.of(new Object() { int x; }, new Object() { int y; });",
				"void m() {",
				"class Local {",
				"}",
				"}",
				"}"
		)));
		assertEquals(
				List.of("Shade", "Shade/ZETA", "Shade/ALPHA", "Shade/pair/<new>", "Shade/pair/<new>#2", "Shade/m/Local"),
				orders.stream().map(BodyOrder::path).toList()
		);
	}

	@Test
	public void testBodyOrdersKeyNestedBodiesByEveryOwner() throws Exception {
		final var orders = FieldInitializers.bodyOrders(parse(List.of(
				"class T {",
				"interface I {",
				"}",
				"record R() {",
				"}",
				"@interface A {",
				"}",
				"final Object outer = new Object() {",
				"final Object inner = new Object() {",
				"};",
				"};",
				"T() {",
				"Object local = new Object() {",
				"};",
				"}",
				"}"
		)));
		assertEquals(
				List.of("T", "T/I", "T/R", "T/A", "T/outer/<new>", "T/outer/<new>/inner/<new>", "T/T/local/<new>"),
				orders.stream().map(BodyOrder::path).toList()
		);
	}

	@Test
	public void testDependenciesCountAReadThroughTheOuterQualifiedOwnName() throws Exception {
		final var fields = membersOf(parse(List.of("class T {", "static class U {", "static int a = T.U.b;", "static int b;", "}", "}")));
		assertEquals(Map.of("a", Set.of("b")), FieldSortingCheck.fieldDependencies(fields));
	}

	@Test
	public void testDependenciesCountAReadThroughTheOwnInstanceOfANestedClass() throws Exception {
		final var members = membersOf(parse(List.of("class O {", "int count;", "class H {", "int base = H.this.count;", "int count;", "}", "}")));
		assertEquals(Map.of("base", Set.of("count")), FieldSortingCheck.fieldDependencies(members.subList(1, 3)));
	}

	@Test
	public void testDependenciesCountAReadThroughTheQualifiedOwnInstanceOfANestedClass() throws Exception {
		final var members = membersOf(parse(List.of("class O {", "int count;", "class H {", "int base = O.H.this.count;", "int count;", "}", "}")));
		assertEquals(Map.of("base", Set.of("count")), FieldSortingCheck.fieldDependencies(members.subList(1, 3)));
	}

	@Test
	public void testDependenciesOfAWideTableScaleLinearly() throws Exception {
		assertScalesLinearly(
				250,
				2000,
				size -> {
					final var table = new StringBuilder("static final Object[] TABLE = {");
					for (var i = 0; i < size; ++i)
						table.append(i == 0 ? "" : ", ").append("K.A").append(i).append(", a").append(i);
					return fieldsOf("class T {", table.append("};").toString(), "static Object a0;", "}");
				},
				(size, fields) -> {
					final var start = System.nanoTime();
					assertEquals(Map.of("TABLE", Set.of("a0")), FieldSortingCheck.fieldDependencies(fields));
					return System.nanoTime() - start;
				}
		);
	}

	@Test
	public void testDependenciesSkipAReadThroughAnEnclosingInstance() throws Exception {
		final var members = membersOf(parse(List.of("class O {", "int count;", "class H {", "int base = O.this.count;", "int count;", "}", "}")));
		assertEquals(Map.of(), FieldSortingCheck.fieldDependencies(members.subList(1, 3)));
	}

	@Test
	public void testDependenciesSkipAReadThroughTheEnumNameInAConstantBody() throws Exception {
		final var members = membersOf(parse(List.of("enum E {", "A {", "int a = E.b;", "int b;", "};", "static int b;", "}")));
		assertEquals(Map.of(), FieldSortingCheck.fieldDependencies(members.subList(1, 3)));
	}

	/**
	 * Only the classification is timed: checkstyle's own parse of so long a constant list grows
	 * about four times per doubling.
	 */
	@Test
	public void testEnumConstantsClassifyInLinearTime() throws Exception {
		assertScalesLinearly(
				250,
				2000,
				size -> {
					final var lines = new ArrayList<>(List.of("enum Code {"));
					for (var i = 0; i < size; ++i)
						lines.add(String.format(Locale.ROOT, "C%05d(%d)%s", i, i, i == size - 1 ? ";" : ","));
					lines.addAll(List.of("private final int v;", "Code(int v) {", "this.v = v;", "}", "}"));
					return parse(lines);
				},
				(size, root) -> {
					final var constants = membersOf(root).stream().filter(member -> member.getType() == TokenTypes.ENUM_CONSTANT_DEF).toList();
					final var start = System.nanoTime();
					assertEquals(Collections.nCopies(size, Kind.RUNS_CODE), FieldInitializers.kindsOf(constants));
					assertEquals(Map.of(), FieldInitializers.constantReads(constants));
					assertEquals(Map.of(), FieldInitializers.bareForwardReads(constants));
					assertEquals(size + 1, FieldInitializers.bodyOrders(root).getFirst().names().size());
					return System.nanoTime() - start;
				}
		);
	}

	@Test
	public void testEnumConstantsSwappedWhenPinnedIsRefused() throws Exception {
		final var before = FieldInitializers.bodyOrders(parse(List.of("enum E {", "B(Ids.next()), A;", "E() {", "}", "E(int v) {", "}", "}"))).getFirst();
		final var after = FieldInitializers.bodyOrders(parse(List.of("enum E {", "A, B(Ids.next());", "E() {", "}", "E(int v) {", "}", "}"))).getFirst();
		assertFalse(FieldInitializers.keepsOrder(before, after));
	}

	@Test
	public void testEnumConstantsTakePartInTheBodyOrder() throws Exception {
		final var order = FieldInitializers.bodyOrders(parse(List.of("enum E {", "B(Ids.next()), A;", "int x;", "}"))).getFirst();
		assertEquals(List.of("B", "A", "x"), order.names());
		assertEquals(List.of(Kind.RUNS_CODE, Kind.RUNS_CODE, Kind.INERT), order.kinds());
		assertEquals(List.of(true, true, false), order.statics());
	}

	@Test
	public void testInitTimeForwardReadsCountAnAnonymousInitializerRead() throws Exception {
		final var fields = fieldsOf(
				"class T {",
				"final Object holder = new Object() { { System.out.println(label); } };",
				"final String label = String.valueOf(\"w\");",
				"}"
		);
		assertEquals(Map.of("holder", Set.of("label")), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsCountAReadThroughTheQualifiedOwnInstanceOfANestedClass() throws Exception {
		final var members = membersOf(parse(List.of("class O {", "class H {", "int base = O.H.this.count;", "int count = Integer.parseInt(\"1\");", "}", "}")));
		assertEquals(Map.of("base", Set.of("count")), FieldSortingCheck.initTimeForwardReads(members));
	}

	@Test
	public void testInitTimeForwardReadsCountAThisQualifiedReadOfALambdaParameterName() throws Exception {
		final var fields = fieldsOf("class T {", "DoubleUnaryOperator a = rate -> this.rate * rate;", "double rate = Double.parseDouble(\"1\");", "}");
		assertEquals(Map.of("a", Set.of("rate")), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsCountAWrittenField() throws Exception {
		final var fields = fieldsOf("class T {", "Supplier<Object> a = () -> { rate = 2; return null; };", "int rate = Integer.parseInt(\"1\");", "}");
		assertEquals(Map.of("a", Set.of("rate")), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsSkipACaseLabel() throws Exception {
		final var fields = fieldsOf(
				"class T {",
				"final Object holder = new Object() { int q = switch (mode) { case b -> 1; default -> 0; }; };",
				"Mode mode;",
				"int b = Integer.parseInt(\"1\");",
				"}"
		);
		assertEquals(Map.of(), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsSkipAMethodOfTheOuterInstance() throws Exception {
		final var fields = fieldsOf("class T {", "int a = T.this.b();", "int b = Integer.parseInt(\"1\");", "}");
		assertEquals(Map.of(), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsSkipAnAnonymousInitializerLocal() throws Exception {
		final var fields = fieldsOf(
				"class T {",
				"final Object holder = new Object() { { final var label = \"x\"; System.out.println(label); } };",
				"final String label = String.valueOf(\"w\");",
				"}"
		);
		assertEquals(Map.of(), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInitTimeForwardReadsSkipAReadThroughAnEnclosingInstance() throws Exception {
		final var members = membersOf(parse(List.of("class O {", "int count;", "class H {", "int base = O.this.count;", "int count = Integer.parseInt(\"1\");", "}", "}")));
		assertEquals(Map.of(), FieldSortingCheck.initTimeForwardReads(members.subList(1, 3)));
	}

	@Test
	public void testInitTimeForwardReadsSkipAWrittenLambdaLocal() throws Exception {
		final var fields = fieldsOf("class T {", "Supplier<Object> a = () -> { int rate; rate = 2; return rate; };", "int rate = Integer.parseInt(\"1\");", "}");
		assertEquals(Map.of(), FieldSortingCheck.initTimeForwardReads(fields));
	}

	@Test
	public void testInterfaceExplicitStaticAndImplicitPinnedPairSwappedIsRefused() throws Exception {
		final var before = FieldInitializers.bodyOrders(parse(List.of("interface I {", "static int Z = Ids.next();", "Object A = \"a\";", "}"))).getFirst();
		final var after = FieldInitializers.bodyOrders(parse(List.of("interface I {", "Object A = \"a\";", "static int Z = Ids.next();", "}"))).getFirst();
		assertFalse(FieldInitializers.keepsOrder(before, after));
	}

	@Test
	public void testInterfaceFieldPinnedPairSwappedIsRefused() throws Exception {
		final var before = FieldInitializers.bodyOrders(parse(List.of("interface I {", "int Z = Ids.next();", "Object A = \"a\";", "}"))).getFirst();
		final var after = FieldInitializers.bodyOrders(parse(List.of("interface I {", "Object A = \"a\";", "int Z = Ids.next();", "}"))).getFirst();
		assertFalse(FieldInitializers.keepsOrder(before, after));
	}

	@Test
	public void testInterfaceLiteralConstantIsInert() throws Exception {
		assertEquals(Kind.INERT, FieldInitializers.kindOf(fieldsOf("interface I {", "int A = 1;", "}").getFirst()));
	}

	@Test
	public void testInterfaceNonConstantIsNotInert() throws Exception {
		assertEquals(Kind.LITERAL, FieldInitializers.kindOf(fieldsOf("interface I {", "Object A = \"a\";", "}").getFirst()));
	}

	@Test
	public void testKeepsOrderAgreesWithEveryPairwisePin() {
		final var names = List.of("m0", "m1", "m2", "m3");
		final var kinds = Kind.values();
		final var orders = permutations(names.size());
		final var kindLayouts = (int) Math.pow(kinds.length, names.size());
		for (var kindLayout = 0; kindLayout < kindLayouts; ++kindLayout) {
			for (var staticLayout = 0; staticLayout < 1 << names.size(); ++staticLayout) {
				final var memberKinds = new ArrayList<Kind>();
				final var memberStatics = new ArrayList<Boolean>();
				var rest = kindLayout;
				for (var i = 0; i < names.size(); ++i) {
					memberKinds.add(kinds[rest % kinds.length]);
					memberStatics.add((staticLayout >> i & 1) == 1);
					rest /= kinds.length;
				}
				final var before = new BodyOrder("T", names, memberStatics, memberKinds, Map.of(), Map.of());
				for (var order : orders) {
					final var after = new BodyOrder(
							"T",
							order.stream().map(names::get).toList(),
							order.stream().map(memberStatics::get).toList(),
							order.stream().map(memberKinds::get).toList(),
							Map.of(),
							Map.of()
					);
					var kept = true;
					for (var i = 0; i < names.size(); ++i) {
						for (var j = i + 1; j < names.size(); ++j) {
							if (memberStatics.get(i).equals(memberStatics.get(j)) && FieldInitializers.pinned(memberKinds.get(i), memberKinds.get(j)) && order.indexOf(i) > order.indexOf(j))
								kept = false;
						}
					}
					assertEquals(kept, FieldInitializers.keepsOrder(before, after), before + " to " + after);
				}
			}
		}
	}

	@Test
	public void testKeepsOrderScalesLinearly() throws Exception {
		assertScalesLinearly(
				5000,
				40_000,
				size -> {
					final var names = new ArrayList<String>(size);
					final var reads = new HashMap<String, Set<String>>();
					for (var i = 0; i < size; ++i) {
						names.add("m" + i);
						if (i > 0)
							reads.put("m" + i, Set.of("m" + (i - 1)));
					}
					final var statics = Collections.nCopies(size, false);
					final var literals = new BodyOrder("T", names, statics, Collections.nCopies(size, Kind.LITERAL), Map.of(), Map.of());
					final var reversed = new BodyOrder("T", names.reversed(), statics, literals.kinds(), Map.of(), Map.of());
					return List.of(literals, reversed, new BodyOrder("T", names, statics, Collections.nCopies(size, Kind.RUNS_CODE), reads, reads));
				},
				(size, orders) -> {
					final var start = System.nanoTime();
					assertTrue(FieldInitializers.keepsOrder(orders.getFirst(), orders.get(1)));
					assertTrue(FieldInitializers.keepsOrder(orders.getLast(), orders.getLast()));
					return System.nanoTime() - start;
				}
		);
	}

	@Test
	public void testLaterDeclaratorClassifiedOnItsOwn() throws Exception {
		final var fields = fieldsOf("class T {", "int a = 1, c = Ids.next();", "}");
		assertEquals(List.of(Kind.LITERAL, Kind.RUNS_CODE), List.of(FieldInitializers.kindOf(fields.get(0)), FieldInitializers.kindOf(fields.get(1))));
	}

	@Test
	public void testPinnedAfterHoldsAPairOfABareForwardRead() throws Exception {
		final var fields = fieldsOf("class T {", "int b = c + Ids.next();", "int c = 2;", "}");
		assertEquals(Map.of("c", Set.of("b")), FieldInitializers.pinnedAfter(fields));
	}

	@Test
	public void testPinnedAfterHoldsEveryConstantAfterTheOneAheadOfIt() throws Exception {
		assertEquals(
				Map.of("ELECTRONICS", Set.of("ROOT"), "BOOKS", Set.of("ELECTRONICS")),
				FieldInitializers.pinnedAfter(constantsOf(List.of("ROOT(null), ELECTRONICS(ROOT), BOOKS(ROOT);")))
		);
	}

	@Test
	public void testPinnedAfterKeepsABareForwardReadAmongPins() throws Exception {
		final var fields = fieldsOf("class T {", "int a = Ids.next();", "int b = c + 1;", "int c = 2;", "int d = Ids.next();", "}");
		assertEquals(Map.of("b", Set.of("a"), "c", Set.of("b"), "d", Set.of("b", "c")), FieldInitializers.pinnedAfter(fields));
	}

	@Test
	public void testPinnedAfterKeepsTheEdgesThatImplyTheRest() throws Exception {
		final var fields = fieldsOf(
				"class T {",
				"int a = Ids.next();",
				"int b = 1;",
				"int c;",
				"int d = 2;",
				"int e = Ids.next();",
				"int f = 3;",
				"static int g = 4;",
				"static int h = Ids.next();",
				"}"
		);
		assertEquals(
				Map.of("b", Set.of("a"), "d", Set.of("a"), "e", Set.of("a", "b", "d"), "f", Set.of("e"), "h", Set.of("g")),
				FieldInitializers.pinnedAfter(fields)
		);
	}

	@Test
	public void testPinnedOnlyWhenOneRunsCodeAndTheOtherIsNotInert() {
		final var kinds = Kind.values();
		final var pinned = new ArrayList<String>();
		for (var a : kinds) {
			for (var b : kinds) {
				if (FieldInitializers.pinned(a, b))
					pinned.add(a + "/" + b);
			}
		}
		assertEquals(List.of("LITERAL/RUNS_CODE", "RUNS_CODE/LITERAL", "RUNS_CODE/RUNS_CODE"), pinned);
	}

	@Test
	public void testStaticInterfaceFieldsShareTheGroupOfImplicitOnes() throws Exception {
		final var events = BaseCheckTest.runCheckInline(
				FieldSortingCheck.class,
				"interface I {\n\tstatic String LABEL = I.PREFIX + \"x\";\n\tString PREFIX = System.getProperty(\"p\", \"app\");\n}"
		);
		assertEquals(List.of("Field 'LABEL' must appear after 'PREFIX' (dependency order)."), events.stream().map(AuditEvent::getMessage).toList());
		assertEquals(2, events.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel());
	}
}