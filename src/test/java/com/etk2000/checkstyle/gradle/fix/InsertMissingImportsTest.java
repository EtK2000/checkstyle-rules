package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class InsertMissingImportsTest {
	private static final Set<String> REGULAR = Set.of("java.util.List");
	private static final Set<String> STATIC = Set.of("static java.util.Objects.requireNonNull");
	private static final String LIST = "import java.util.List;";
	private static final String REQUIRE = "import static java.util.Objects.requireNonNull;";

	/**
	 * A package line followed on its own line by code or a block comment left open, which moves below
	 * the imports, or by a comment that closes there, which stays; and a package statement wrapped
	 * across lines. Each as (name, input, requested imports, expected count, expected lines).
	 */
	@Nonnull
	private static Stream<Arguments> packageLineCases() {
		final var bom = String.valueOf((char) 0xFEFF);
		final var both = Set.of("java.util.List", "static java.util.Objects.requireNonNull");
		return Stream.of(
				Arguments.of("open comment closed on a code line", List.of("package p; /* note", "*/ class C {", "\tint a;", "}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* note", "*/ class C {", "\tint a;", "}")),
				Arguments.of("open comment over whole lines", List.of("package p; /* note", " * more", "", "*/ class C {", "}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* note", " * more", "", "*/ class C {", "}")),
				Arguments.of("class", List.of("package p; class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "class C {}")),
				Arguments.of("annotated class", List.of("package p; @Deprecated class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "@Deprecated class C {}")),
				Arguments.of("interface", List.of("package p; interface I {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "interface I {}")),
				Arguments.of("record", List.of("package p; record R() {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "record R() {}")),
				Arguments.of("enum", List.of("package p; enum E { A }"), REGULAR, 1, List.of("package p;", "", LIST, "", "enum E { A }")),
				Arguments.of("annotation type", List.of("package p; @interface A {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "@interface A {}")),
				Arguments.of("import joins the block", List.of("package p; import a.B;", "class C {}"), REGULAR, 1, List.of("package p;", "import a.B;", LIST, "class C {}")),
				Arguments.of("import already present", List.of("package p; import java.util.List;", "class C {}"), REGULAR, 0, List.of("package p;", LIST, "class C {}")),
				Arguments.of("line comment stays", List.of("package p; // note", "class C {}"), REGULAR, 1, List.of("package p; // note", "", LIST, "class C {}")),
				Arguments.of("closed block comment stays", List.of("package p; /* x */", "class C {}"), REGULAR, 1, List.of("package p; /* x */", "", LIST, "class C {}")),
				Arguments.of("closed block and line comment stay", List.of("package p; /* x */ // y", "class C {}"), REGULAR, 1, List.of("package p; /* x */ // y", "", LIST, "class C {}")),
				Arguments.of("line comment holding a block opener stays", List.of("package p; // see /* old", "class C {}"), REGULAR, 1, List.of("package p; // see /* old", "", LIST, "class C {}")),
				Arguments.of("block comment holding a line opener", List.of("package p; /* see // old", "*/ class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* see // old", "*/ class C {}")),
				Arguments.of("closed then open comment", List.of("package p; /* x */ /* open", "*/ class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* x */ /* open", "*/ class C {}")),
				Arguments.of("code after a closed comment", List.of("package p; /* a */ class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* a */ class C {}")),
				Arguments.of("text block opened", List.of("package p; class C { String s = \"\"\"", "\t\tx", "\t\t\"\"\"; }"), REGULAR, 1, List.of("package p;", "", LIST, "", "class C { String s = \"\"\"", "\t\tx", "\t\t\"\"\"; }")),
				Arguments.of("string holding a comment opener", List.of("package p; class C { String s = \"/*\"; }", "class D {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "class C { String s = \"/*\"; }", "class D {}")),
				Arguments.of("semicolon behind a comment holding one", List.of("package p /* ; */; class C {}"), REGULAR, 1, List.of("package p /* ; */;", "", LIST, "", "class C {}")),
				Arguments.of("tabs before the remainder", List.of("package p;\t\t/* note", "*/ class C {}"), REGULAR, 1, List.of("package p;", "", LIST, "", "/* note", "*/ class C {}")),
				Arguments.of("tab-separated keyword", List.of("package\tp; /* note", "*/ class C {}"), REGULAR, 1, List.of("package\tp;", "", LIST, "", "/* note", "*/ class C {}")),
				Arguments.of("space before the semicolon", List.of("package p ; class C {}"), REGULAR, 1, List.of("package p ;", "", LIST, "", "class C {}")),
				Arguments.of("trailing whitespace stays", List.of("package p;  ", "class C {}"), REGULAR, 1, List.of("package p;  ", "", LIST, "class C {}")),
				Arguments.of("comment before the package", List.of("/* a */ package p; /* note", "*/ class C {}"), REGULAR, 1, List.of("/* a */ package p;", "", LIST, "", "/* note", "*/ class C {}")),
				Arguments.of("package on a comment-closing line", List.of("/* x", "*/ package p; /* note", "*/ class C {}"), REGULAR, 1, List.of("/* x", "*/ package p;", "", LIST, "", "/* note", "*/ class C {}")),
				Arguments.of("byte-order mark stays first", List.of(bom + "package p; /* note", "*/ class C {}"), REGULAR, 1, List.of(bom + "package p;", "", LIST, "", "/* note", "*/ class C {}")),
				Arguments.of("byte-order mark, line comment stays", List.of(bom + "package p; // n", "class C {}"), REGULAR, 1, List.of(bom + "package p; // n", "", LIST, "class C {}")),
				Arguments.of("static, open comment", List.of("package p; /* note", "*/ class C {", "\tint a;", "}"), STATIC, 1, List.of("package p;", "", REQUIRE, "", "/* note", "*/ class C {", "\tint a;", "}")),
				Arguments.of("static, class", List.of("package p; class C {}"), STATIC, 1, List.of("package p;", "", REQUIRE, "", "class C {}")),
				Arguments.of("static, line comment stays", List.of("package p; // note", "class C {}"), STATIC, 1, List.of("package p; // note", "", REQUIRE, "", "class C {}")),
				Arguments.of("static, closed block comment stays", List.of("package p; /* x */", "class C {}"), STATIC, 1, List.of("package p; /* x */", "", REQUIRE, "", "class C {}")),
				Arguments.of("both, open comment", List.of("package p; /* note", "*/ class C {", "\tint a;", "}"), both, 2, List.of("package p;", "", REQUIRE, "", LIST, "", "/* note", "*/ class C {", "\tint a;", "}")),
				Arguments.of("both, class", List.of("package p; class C {}"), both, 2, List.of("package p;", "", REQUIRE, "", LIST, "", "class C {}")),
				Arguments.of("both, line comment stays", List.of("package p; // note", "class C {}"), both, 2, List.of("package p; // note", "", REQUIRE, "", LIST, "class C {}")),
				Arguments.of("several imports", List.of("package p; /* note", "*/ class C {", "\tint a;", "}"), Set.of("a.B", "a.C"), 2, List.of("package p;", "", "import a.B;", "import a.C;", "", "/* note", "*/ class C {", "\tint a;", "}")),
				Arguments.of("anchored on an import the moved comment closes onto", List.of("package p; /* note", "*/ import a.B;", "class C {}"), Set.of("a.C"), 1, List.of("package p;", "/* note", "*/ import a.B;", "import a.C;", "class C {}")),
				Arguments.of("static above an import the moved comment closes onto", List.of("package p; /* note", "*/ import a.B;", "class C {}"), Set.of("static a.D.e"), 1, List.of("package p;", "", "import static a.D.e;", "", "/* note", "*/ import a.B;", "class C {}")),
				Arguments.of("wrapped package statement", List.of("package", "\tp;", "class C {}"), REGULAR, 1, List.of("package", "\tp;", "", LIST, "class C {}")),
				Arguments.of("wrapped package statement, static", List.of("package", "\tp;", "class C {}"), STATIC, 1, List.of("package", "\tp;", "", REQUIRE, "", "class C {}")),
				Arguments.of("wrapped package statement with a class after it", List.of("package", "\tp; class C {}"), REGULAR, 1, List.of("package", "\tp;", "", LIST, "", "class C {}")),
				Arguments.of("no package, comment closed on a code line", List.of("/* note", "*/ class C {}"), REGULAR, 1, List.of(LIST, "/* note", "*/ class C {}")),
				Arguments.of("no package, static", List.of("/* note", "*/ class C {}"), STATIC, 1, List.of(REQUIRE, "", "/* note", "*/ class C {}")),
				Arguments.of("no package, both", List.of("/* note", "*/ class C {}"), both, 2, List.of(REQUIRE, "", LIST, "/* note", "*/ class C {}")),
				Arguments.of("import line with trailing code", List.of("package p;", "import a.B; class C {}"), Set.of("a.C"), 1, List.of("package p;", "", "import a.C;", "import a.B; class C {}"))
		);
	}

	@Test
	public void testBlockCommentImportDoesNotDeduplicate() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import java.util.List;",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"/*",
				"import java.util.List;",
				"*/",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testBomOnImportLineAnchorsAndDeduplicates() {
		final var bom = (char) 0xFEFF;
		final var lines = new ArrayList<>(List.of(bom + "import a.B;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B", "a.C"));
		assertEquals(1, count);
		assertEquals(List.of(bom + "import a.B;", "import a.C;", "class T {}"), lines);
	}

	@Test
	public void testBomOnlyBufferKeepsMarkFirst() {
		final var bom = (char) 0xFEFF;
		final var lines = new ArrayList<>(List.of(String.valueOf(bom)));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of(bom + "import java.util.List;", ""), lines);
	}

	@Test
	public void testBomOnPackageLineStillAnchors() {
		final var bom = (char) 0xFEFF;
		final var lines = new ArrayList<>(List.of(bom + "package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(bom + "package com.example;", "", "import java.util.List;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testBomOnTypeDeclarationLineKeepsMarkFirst() {
		final var bom = (char) 0xFEFF;
		final var lines = new ArrayList<>(List.of(bom + "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of(bom + "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testBomOnTypeDeclarationLineKeepsMarkFirstWithStaticImport() {
		final var bom = (char) 0xFEFF;
		final var lines = new ArrayList<>(List.of(bom + "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(1, count);
		assertEquals(List.of(bom + "import static a.B.c;", "", "class T {}"), lines);
	}

	@Test
	public void testDotlessImportNotSortedIntoStaticGroup() {
		final var lines = new ArrayList<>(List.of(
				"import static a.B.c;",
				"",
				"import java.util.Map;",
				"",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("Foo"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static a.B.c;",
				"",
				"import Foo;",
				"import java.util.Map;",
				"",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testDoubledSemicolonImportAnchorsWithoutDeduplicating() {
		final var lines = new ArrayList<>(List.of("import a.B;;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(1, count);
		assertEquals(List.of("import a.B;", "import a.B;;", "class T {}"), lines);
	}

	@Test
	public void testDoubledSemicolonStaticImportAnchorsWithoutDeduplicating() {
		final var lines = new ArrayList<>(List.of("import static a.B.c;;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(1, count);
		assertEquals(List.of("import static a.B.c;", "import static a.B.c;;", "class T {}"), lines);
	}

	@Test
	public void testEmptyFileGetsImportAtTopOfFile() {
		final var lines = new ArrayList<String>();
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("import java.util.List;"), lines);
	}

	@Test
	public void testEmptyFileGetsStaticImportAtTopOfFile() {
		final var lines = new ArrayList<String>();
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(1, count);
		assertEquals(List.of("import static a.B.c;", ""), lines);
	}

	@Test
	public void testImportAddedAfterAnchorOnFinalLine() {
		final var lines = new ArrayList<>(List.of("import a.B;"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.C"));
		assertEquals(1, count);
		assertEquals(List.of("import a.B;", "import a.C;"), lines);
	}

	@Test
	public void testImportAddedAfterLastImportWhenSortsLast() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedAtEndWhenTailFullyMasked() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedBeforeFirstImport() {
		final var lines = new ArrayList<>(List.of("import java.util.Map;", "import java.util.Set;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedBetweenPackageGroupsNoMatchingGroup() {
		final var lines = new ArrayList<>(List.of("import com.example.Foo;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import com.example.Foo;", "", "import java.util.List;", "import javax.annotation.Nonnull;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSamePackageGroup() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "", "import javax.annotation.Nonnull;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSamePackageGroupBeforeExisting() {
		final var lines = new ArrayList<>(List.of("import com.example.Foo;", "", "import java.util.Map;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"import com.example.Foo;", "", "import java.util.List;", "import java.util.Map;", "", "import javax.annotation.Nonnull;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAddedInSortedPosition() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAfterTextBlockCloseOnSameLineIsNotRecognized() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\t\"\"\"; import a.B;",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import a.B;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\t\"\"\"; import a.B;",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportAlreadyPresent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testImportCoveredByPackageWildcard() {
		final var lines = new ArrayList<>(List.of("import java.nio.charset.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.nio.charset.StandardCharsets"));
		assertEquals(0, count);
		assertEquals(List.of("import java.nio.charset.*;", "class T {}"), lines);
	}

	@Test
	public void testImportInSubPackageNotCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import java.nio.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.nio.charset.StandardCharsets"));
		assertEquals(1, count);
		assertEquals(
				List.of("import java.nio.*;", "import java.nio.charset.StandardCharsets;", "class T {}"),
				lines
		);
	}

	@Test
	public void testImportNotInsertedInsideBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"package old.pkg;",
				"import java.util.Map;",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"/*",
				"package old.pkg;",
				"import java.util.Map;",
				"*/",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideBlockCommentedOutImportGroup() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import static com.zzz.Old.OLD;",
				"*/",
				"import static com.foo.Bar.AAA;",
				"",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static com.foo.Bar.ZZZ"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import static com.zzz.Old.OLD;",
				"*/",
				"import static com.foo.Bar.AAA;",
				"import static com.foo.Bar.ZZZ;",
				"",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideCommentOpenedByAnchorImport() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsidePackageOpenedComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"",
				"/*",
				"trailing comment",
				"*/",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideTextBlock() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.Map;",
				"\t\t\"\"\";",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.Map;",
				"\t\t\"\"\";",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportNotInsertedInsideTextBlockWhenNoRegularImports() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport static com.evil.Y;",
				"\t\t\"\"\";",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X;",
				"",
				"import java.util.List;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport static com.evil.Y;",
				"\t\t\"\"\";",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testImportsAllAlreadyPresent() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map", "java.util.Set"));
		assertEquals(0, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testImportWithBlockCommentAfterKeywordDeduplicates() {
		final var lines = new ArrayList<>(List.of("import /* note */ java.util.List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import /* note */ java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testImportWithBlockCommentInsideQualifiedNameDeduplicates() {
		final var lines = new ArrayList<>(List.of("import java.util./* note */List;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util./* note */List;", "class T {}"), lines);
	}

	@Test
	public void testImportWithJlsWhitespaceDeduplicates() {
		final var lines = new ArrayList<>(List.of("import java . util . List ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java . util . List ;", "class T {}"), lines);
	}

	@Test
	public void testImportWithSpaceBeforeSemicolonDeduplicates() {
		final var lines = new ArrayList<>(List.of("import java.util.List ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util.List ;", "class T {}"), lines);
	}

	@Test
	public void testImportWithTrailingBlockCommentAnchorsAndDeduplicates() {
		final var lines = new ArrayList<>(List.of("import java.util.List; /* note */", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("java.util.List", "java.util.Map")
		);
		assertEquals(1, count);
		assertEquals(List.of("import java.util.List; /* note */", "import java.util.Map;", "class T {}"), lines);
	}

	@Test
	public void testImportWithTrailingLineCommentAnchorsAndDeduplicates() {
		final var lines = new ArrayList<>(List.of("import java.util.List; // note", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("java.util.List", "java.util.Map")
		);
		assertEquals(1, count);
		assertEquals(List.of("import java.util.List; // note", "import java.util.Map;", "class T {}"), lines);
	}

	@Test
	public void testMaskAlignmentHoldsAcrossMultipleRegularInsertions() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.TreeMap;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("java.util.HashMap", "java.util.LinkedList")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.HashMap;",
				"import java.util.LinkedList;",
				"import java.util.TreeMap;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskAlignmentHoldsAcrossMultipleStaticInsertions() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static a.B.aaa;",
				"/*",
				"import static a.B.zzz;",
				"*/",
				"import static a.B.mmm;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static a.B.bbb", "static a.B.nnn")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static a.B.aaa;",
				"/*",
				"import static a.B.zzz;",
				"*/",
				"import static a.B.bbb;",
				"import static a.B.mmm;",
				"import static a.B.nnn;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskAlignmentHoldsAcrossStaticThenRegularInsertion() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static a.B.aaa;",
				"",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.Map;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static a.B.zzz", "java.util.ArrayList")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static a.B.aaa;",
				"import static a.B.zzz;",
				"",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.ArrayList;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInFallbackScanIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import zzz.First;",
				"*/",
				"import java.util.Map;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("com.other.Thing"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import zzz.First;",
				"*/",
				"import com.other.Thing;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInGroupScanIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"/*",
				"import java.util.Map;",
				"*/",
				"import com.other.Thing;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"/*",
				"import java.util.Map;",
				"*/",
				"import com.other.Thing;",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMaskedRegularImportInsideSamePackageGroupIgnored() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.TreeMap;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.HashMap"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.ArrayList;",
				"/*",
				"import java.util.Zzz;",
				"*/",
				"import java.util.HashMap;",
				"import java.util.TreeMap;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsAdded() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of("import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsAddedInSamePackageGroupWithSeparator() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "", "import javax.annotation.Nonnull;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"import java.util.Collections;", "import java.util.List;", "import java.util.Set;", "", "import javax.annotation.Nonnull;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsInterleavedWithExisting() {
		final var lines = new ArrayList<>(List.of("import java.util.Collections;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"import java.util.Collections;", "import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsNotInsertedInsideCommentOpenedByAnchorImport() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Set", "java.util.TreeMap"));
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.Map; /*",
				"trailing comment",
				"*/",
				"import java.util.Set;",
				"import java.util.TreeMap;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleImportsWithPackageNoExistingImports() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Map"));
		assertEquals(2, count);
		final var expected = List.of("package com.example;", "", "import java.util.List;", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsAddedToExistingGroup() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static org.junit.Assert.assertNotNull", "static org.junit.Assert.assertTrue")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNotNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsAddedToNewGroup() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "static java.util.function.Predicate.not")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"import static java.util.function.Predicate.not;",
				"",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testMultipleStaticImportsNotInsertedInsideCommentOpenedByAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static com.foo.Bar.MMM", "static com.foo.Bar.ZZZ")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"import static com.foo.Bar.MMM;",
				"import static com.foo.Bar.ZZZ;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testNoImportsWithPackage() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("package com.example;", "", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testNoImportsWithPackageAndExistingBlankLine() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("package com.example;", "", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testNoImportsWithPackageOnly() {
		final var lines = new ArrayList<>(List.of("package com.example;"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("package com.example;", "", "import java.util.List;"), lines);
	}

	@Test
	public void testNoPackageNoImports() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testOnlyStaticImportsNoPackage() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(
				List.of("import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"),
				lines
		);
	}

	@Test
	public void testOnlyStaticImportsWithBlankAfterNoPackage() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(
				List.of("import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"),
				lines
		);
	}

	@Test
	public void testOnlyStaticImportsWithPackage() {
		final var lines = new ArrayList<>(List.of("package com.example;", "import static org.junit.Assert.assertEquals;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;", "import static org.junit.Assert.assertEquals;", "", "import java.util.List;", "class T {}"
		);
		assertEquals(expected, lines);
	}

	@MethodSource("packageLineCases")
	@ParameterizedTest
	public void testPackageLine(
			@Nonnull String name,
			@Nonnull List<String> input,
			@Nonnull Set<String> fqns,
			int expectedCount,
			@Nonnull List<String> expected
	) {
		final var lines = new ArrayList<>(input);
		assertEquals(expectedCount, CheckstyleFixAction.insertMissingImports(lines, fqns));
		assertEquals(expected, lines);
		assertNull(CheckstyleFixAction.parseBreak(List.of("class T {}"), lines));
	}

	@Test
	public void testPackageOnCommentClosingLineAnchorsRegularFallback() {
		final var lines = new ArrayList<>(List.of("/* note", "*/ package p;", "class C {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(1, count);
		assertEquals(List.of("/* note", "*/ package p;", "", "import a.B;", "class C {}"), lines);
	}

	@Test
	public void testPackageOnCommentClosingLineAnchorsStaticFallback() {
		final var lines = new ArrayList<>(List.of("/* note", "*/ package p;", "class C {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(1, count);
		assertEquals(List.of("/* note", "*/ package p;", "", "import static a.B.c;", "", "class C {}"), lines);
	}

	@Test
	public void testPartialDuplicate() {
		final var lines = new ArrayList<>(List.of("import java.util.List;", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List", "java.util.Set"));
		assertEquals(1, count);
		final var expected = List.of("import java.util.List;", "import java.util.Map;", "import java.util.Set;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportAnchorsOnStaticImportOnCommentClosingLine() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportAnchorsOnStaticImportOnFinalLine() {
		final var lines = new ArrayList<>(List.of("import static a.B.c;"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("x.Y"));
		assertEquals(1, count);
		assertEquals(List.of("import static a.B.c;", "", "import x.Y;"), lines);
	}

	@Test
	public void testRegularImportCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import java.util.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		assertEquals(List.of("import java.util.*;", "class T {}"), lines);
	}

	@Test
	public void testRegularImportNotInsertedInsideCommentOpenedByStaticAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.X; /*",
				"trailing comment",
				"*/",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportOnCommentClosingLineAnchors() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Map"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.util.List;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportOnCommentClosingLineDeduplicates() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(0, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportPartialWildcardNoMatch() {
		final var lines = new ArrayList<>(List.of("import java.io.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		assertEquals(List.of("import java.io.*;", "import java.util.List;", "class T {}"), lines);
	}

	@Test
	public void testRegularImportSkipsUnanchorableStaticAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import java.util.List;",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportSortedBeforeCommentClosingAnchorIsPushedPastIt() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* disabled",
				"*/ import java.util.Zzz;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.Aaa"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* disabled",
				"*/ import java.util.Zzz;",
				"import java.util.Aaa;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularImportSortedBeforeLeadingCommentImport() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* legacy */ import java.util.Map;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.ArrayList"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import java.util.ArrayList;",
				"/* legacy */ import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testRegularWildcardOnCommentClosingLineDoesNotCoverSubPackage() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.nio.*;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.nio.charset.StandardCharsets"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import java.nio.*;",
				"import java.nio.charset.StandardCharsets;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoExistingImports() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "java.util.List")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoExistingStaticGroup() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull", "java.util.Set"));
		assertEquals(2, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.Map;",
				"import java.util.Set;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticAndRegularAddedTogetherNoPackageNoExistingImports() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static java.util.Objects.requireNonNull", "java.util.List")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBeforeExistingStatics() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertEquals"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBeforeExistingStaticsWithRegulars() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static org.junit.Assert.assertEquals", "java.util.Map")
		);
		assertEquals(2, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedBetweenExistingStatics() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertNotNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNotNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedInExistingGroupWithRegularsBelow() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertEquals"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertTrue;",
				"",
				"import java.util.List;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedInSortedPosition() {
		final var lines = new ArrayList<>(List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertTrue"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static org.junit.Assert.assertEquals;",
				"import static org.junit.Assert.assertNull;",
				"import static org.junit.Assert.assertTrue;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageNoBlank() {
		final var lines = new ArrayList<>(List.of("package com.example;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageOnly() {
		final var lines = new ArrayList<>(List.of("package com.example;"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupPackageWithBlank() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("package com.example;", "", "import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoExistingGroupWithRegulars() {
		final var lines = new ArrayList<>(List.of("package com.example;", "", "import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"import java.util.Map;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageLeadingBlankNoDuplicateBlank() {
		final var lines = new ArrayList<>(List.of("", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageNoExistingStatic() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportAddedNoPackageRegularImportsOnly() {
		final var lines = new ArrayList<>(List.of("import java.util.Map;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of("import static java.util.Objects.requireNonNull;", "", "import java.util.Map;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static org.junit.Assert.assertTrue"));
		assertEquals(0, count);
		assertEquals(List.of("import static org.junit.Assert.*;", "class T {}"), lines);
	}

	@Test
	public void testStaticImportNestedClassNotCoveredByWildcard() {
		final var lines = new ArrayList<>(List.of("import static a.B.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.C.m"));
		assertEquals(1, count);
		assertEquals(
				List.of("import static a.B.*;", "import static a.B.C.m;", "class T {}"),
				lines
		);
	}

	@Test
	public void testStaticImportNotInsertedInsideCommentOpenedByAnchorStatic() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static com.foo.Bar.ZZZ"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static com.foo.Bar.AAA; /*",
				"trailing comment",
				"*/",
				"import static com.foo.Bar.ZZZ;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportNotInsertedInsidePackageOpenedComment() {
		final var lines = new ArrayList<>(List.of(
				"package com.example; /*",
				"trailing comment",
				"*/",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static java.util.Objects.requireNonNull"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import static java.util.Objects.requireNonNull;",
				"",
				"/*",
				"trailing comment",
				"*/",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportOnCommentClosingLineAnchors() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.z"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"import static a.B.z;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportOnCommentClosingLineDeduplicates() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.c;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportsNotConfused() {
		final var lines = new ArrayList<>(List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of("import static org.junit.Assert.assertEquals;", "import java.util.Collections;", "import java.util.List;", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportSortedBeforeCommentClosingAnchorIsPushedPastIt() {
		final var lines = new ArrayList<>(List.of(
				"/* disabled",
				"*/ import static org.junit.jupiter.api.Assertions.assertTrue;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(
				lines, Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf")
		);
		assertEquals(1, count);
		final var expected = List.of(
				"/* disabled",
				"*/ import static org.junit.jupiter.api.Assertions.assertTrue;",
				"import static org.junit.jupiter.api.Assertions.assertInstanceOf;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportSortedBeforeLeadingCommentImport() {
		final var lines = new ArrayList<>(List.of(
				"import static a.B.a;",
				"/* legacy */ import static a.B.c;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.b"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static a.B.a;",
				"import static a.B.b;",
				"/* legacy */ import static a.B.c;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticImportWithJlsWhitespaceDeduplicates() {
		final var lines = new ArrayList<>(List.of("import static a . B . c ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		assertEquals(List.of("import static a . B . c ;", "class T {}"), lines);
	}

	@Test
	public void testStaticImportWithSpaceBeforeSemicolonDeduplicates() {
		final var lines = new ArrayList<>(List.of("import static a.B.c ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		assertEquals(List.of("import static a.B.c ;", "class T {}"), lines);
	}

	@Test
	public void testStaticWildcardOnCommentClosingLineCoversMember() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.*;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.*;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticWildcardOnCommentClosingLineDoesNotCoverNestedClass() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.*;",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.C.m"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"/* note",
				"*/ import static a.B.*;",
				"import static a.B.C.m;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testStaticWildcardWithJlsWhitespaceCoversMember() {
		final var lines = new ArrayList<>(List.of("import static a . B . * ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		assertEquals(List.of("import static a . B . * ;", "class T {}"), lines);
	}

	@Test
	public void testStaticWildcardWithJlsWhitespaceDoesNotCoverNestedClass() {
		final var lines = new ArrayList<>(List.of("import static a . B . * ;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.C.m"));
		assertEquals(1, count);
		assertEquals(List.of("import static a . B . * ;", "import static a.B.C.m;", "class T {}"), lines);
	}

	@Test
	public void testTabSeparatedImportDeduplicates() {
		final var lines = new ArrayList<>(List.of("package p;", "", "import\ta.B;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(0, count);
		assertEquals(List.of("package p;", "", "import\ta.B;", "", "class T {}"), lines);
	}

	@Test
	public void testTabSeparatedPackageAnchors() {
		final var lines = new ArrayList<>(List.of("package\tp;", "", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(1, count);
		assertEquals(List.of("package\tp;", "", "import a.B;", "class T {}"), lines);
	}

	@Test
	public void testTabSeparatedStaticImportDeduplicates() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import\tstatic\ta.B.c;",
				"",
				"class T {}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(0, count);
		final var expected = List.of("package p;", "", "import\tstatic\ta.B.c;", "", "class T {}");
		assertEquals(expected, lines);
	}

	@Test
	public void testTextBlockImportDoesNotDeduplicate() {
		final var lines = new ArrayList<>(List.of(
				"package com.example;",
				"",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.List;",
				"\t\t\"\"\";",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("java.util.List"));
		assertEquals(1, count);
		final var expected = List.of(
				"package com.example;",
				"",
				"import java.util.List;",
				"class T {",
				"\tString s = \"\"\"",
				"\t\timport java.util.List;",
				"\t\t\"\"\";",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testTwoImportsOnOneLineAnchorWithoutDeduplicating() {
		final var lines = new ArrayList<>(List.of("import a.B; import a.C;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.C"));
		assertEquals(1, count);
		assertEquals(List.of("import a.B; import a.C;", "import a.C;", "class T {}"), lines);
	}

	@Test
	public void testTwoStaticImportsOnOneLineAnchorWithoutDeduplicating() {
		final var lines = new ArrayList<>(List.of("import static a.B.c; import static a.B.d;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.d"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static a.B.c; import static a.B.d;",
				"import static a.B.d;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableImportFallsBackToEarlierAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import a.A;",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.C"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import a.A;",
				"import a.C;",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableImportFallsBackToPackage() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("x.Y"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import x.Y;",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableImportStillDeduplicates() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(0, count);
		final var expected = List.of(
				"package p;",
				"",
				"import a.B; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableImportWildcardStillCovers() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import a.*; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.B"));
		assertEquals(0, count);
		final var expected = List.of(
				"package p;",
				"",
				"import a.*; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableSoleImportFallsBackToTopOfFile() {
		final var lines = new ArrayList<>(List.of("import a.B; /* note", "*/ class C {", "}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("x.Y"));
		assertEquals(1, count);
		assertEquals(List.of("import x.Y;", "import a.B; /* note", "*/ class C {", "}"), lines);
	}

	@Test
	public void testUnanchorableSoleStaticImportFallsBackToTopOfFile() {
		final var lines = new ArrayList<>(List.of("import static a.B.c; /* note", "*/ class C {", "}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.z"));
		assertEquals(1, count);
		final var expected = List.of(
				"import static a.B.z;",
				"",
				"import static a.B.c; /* note",
				"*/ class C {",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableStaticImportFallsBackToEarlierAnchor() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import static a.B.a;",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.b"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import static a.B.a;",
				"import static a.B.b;",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testUnanchorableStaticImportFallsBackToPackage() {
		final var lines = new ArrayList<>(List.of(
				"package p;",
				"",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.z"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import static a.B.z;",
				"",
				"import static a.B.c; /* note",
				"*/ class C {",
				"\tvoid m() {}",
				"}"
		);
		assertEquals(expected, lines);
	}

	@Test
	public void testWildcardImportRequestCoveredByExistingWildcard() {
		final var lines = new ArrayList<>(List.of("import a.*;", "import static a.B.*;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.*", "static a.B.*"));
		assertEquals(0, count);
		assertEquals(List.of("import a.*;", "import static a.B.*;", "class T {}"), lines);
	}

	@Test
	public void testWrappedImportNeitherAnchorsNorDeduplicates() {
		final var lines = new ArrayList<>(List.of("package p;", "", "import", "\t\ta.B;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("a.C"));
		assertEquals(1, count);
		assertEquals(List.of("package p;", "", "import a.C;", "import", "\t\ta.B;", "class T {}"), lines);
	}

	@Test
	public void testWrappedStaticImportNeitherAnchorsNorDeduplicates() {
		final var lines = new ArrayList<>(List.of("package p;", "", "import static", "\t\ta.B.c;", "class T {}"));
		final var count = CheckstyleFixAction.insertMissingImports(lines, Set.of("static a.B.c"));
		assertEquals(1, count);
		final var expected = List.of(
				"package p;",
				"",
				"import static a.B.c;",
				"",
				"import static",
				"\t\ta.B.c;",
				"class T {}"
		);
		assertEquals(expected, lines);
	}
}