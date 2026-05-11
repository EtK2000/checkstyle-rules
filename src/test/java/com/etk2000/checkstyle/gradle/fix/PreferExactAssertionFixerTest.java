package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferExactAssertionFixerTest {
	private static List<String> toLines(List<String> headerLines, String body) {
		final var all = new ArrayList<>(headerLines);
		all.add("");
		all.add(body);
		return all;
	}

	private static List<String> withJunit5(String body) {
		return List.of("import static org.junit.jupiter.api.Assertions.*;", "", body);
	}

	@Test
	public void assertFalseInstanceOfBecomesAssertNotInstanceOf() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertFalse;");
		final var call = "\t\tassertFalse(o instanceof Integer);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertNotInstanceOf(Integer.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertNotInstanceOf"), result.importsToAdd());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void assertJStaticImportNotMistakenForJunit() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.assertj.core.api.Assertions.assertThat;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void assertTrueInstanceOfBecomesAssertInstanceOf() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void complexLhsPreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(ex.getCause() instanceof IOException);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(IOException.class, ex.getCause());", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void compoundInstanceOfSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof String && !o.toString().isEmpty());";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void customAssertTrueNotMatched() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tcustomAssertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void dollarInImportFqnParsed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import com.foo.Outer$Inner;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
	}

	@Test
	public void doubleNegationCancelsToPositive() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var call = "\t\tassertTrue(!!(o instanceof String));";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void doubleNegationOnAssertFalseCancelsToNegative() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertFalse;");
		final var call = "\t\tassertFalse(!!(o instanceof Integer));";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertNotInstanceOf(Integer.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertNotInstanceOf"), result.importsToAdd());
	}

	@Test
	public void emptyArgsSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue();";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void findMethodCallOpenSkipsCustomThenMatchesReal() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tcustomAssertTrue(x); assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tcustomAssertTrue(x); assertInstanceOf(String.class, o);",
				result.replacement().getFirst()
		);
	}

	@Test
	public void fullyQualifiedAssertionsCallPreservesFqnPrefix() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\torg.junit.jupiter.api.Assertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\torg.junit.jupiter.api.Assertions.assertInstanceOf(String.class, o);",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void fullyQualifiedTypePreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(ex.getCause() instanceof java.io.IOException);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(java.io.IOException.class, ex.getCause());",
				result.replacement().getFirst()
		);
	}

	@Test
	public void genericTypeSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof java.util.List<String>);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void importWithTrailingLineCommentRecognized() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.junit.jupiter.api.Assertions.assertTrue; // bootstrap"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void instanceofInsideParensInOtherArgIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"got: \" + (x instanceof Y), x instanceof Y);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(Y.class, x, \"got: \" + (x instanceof Y));",
				result.replacement().getFirst()
		);
	}

	@Test
	public void junit4And5MixedImportsRewriteQualified() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.junit.Assert.assertTrue;",
				"import org.junit.jupiter.api.Assertions;"
		);
		final var call = "\t\tAssertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tAssertions.assertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void junit4And5MixedImportsSkipUnqualified() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.junit.Assert.assertTrue;",
				"import static org.junit.jupiter.api.Assertions.assertNotNull;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void junit4ImportOnlySkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.Assert.assertTrue;");
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void junit4MessageFirstShape() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"should be a string\", o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(String.class, o, \"should be a string\");",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void junit4NonStaticAndJunit5StaticMixedRewritesUnqualified() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import org.junit.Assert;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void junit4PackageWildcardWithJunit5RewritesUnqualified() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import org.junit.*;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(imports.size() + 1, result.startLine());
		assertEquals(imports.size() + 1, result.endLine());
	}

	@Test
	public void junit4WildcardImportOnlySkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.Assert.*;");
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void junit5MessageLastShape() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof String, \"should be a string\");";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(String.class, o, \"should be a string\");",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void junit5WildcardFollowedByStaticAssertImportSkipsAdd() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.junit.jupiter.api.Assertions.*;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void lhsWithCharCommaPreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(processChar(',') instanceof Foo);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(Foo.class, processChar(','));",
				result.replacement().getFirst()
		);
	}

	@Test
	public void lhsWithNestedCallPreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(transform(a, b, c) instanceof Foo);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(Foo.class, transform(a, b, c));",
				result.replacement().getFirst()
		);
	}

	@Test
	public void messageWithCommaPreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"foo, bar\", o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(String.class, o, \"foo, bar\");",
				result.replacement().getFirst()
		);
	}

	@Test
	public void multiLineCallAmbiguousMultiplePerLineSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue /* legacy */ (foo instanceof Object); assertTrue");
		lines.add("\t\t(o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallBlockCommentContainingNameIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\t/* assertTrue(stale) */ assertTrue(o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				List.of("\t\t/* assertTrue(stale) */ assertInstanceOf(String.class, o);"),
				result.replacement()
		);
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void multiLineCallFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\to instanceof String");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallJunit5MessageLastFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\to instanceof String,");
		lines.add("\t\t\t\t\"msg\"");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o, \"msg\");"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallNegatedFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\t!(o instanceof String)");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertNotInstanceOf(String.class, o);"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertNotInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallNoCloseParenReturnsSkipResult() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(o instanceof String");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallNoOpenParenReturnsSkipResult() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue x;");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallNoSemicolonReturnsSkipResult() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tboolean b = assertTrue(o instanceof String)");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallOpenParenOnOwnLineFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue");
		lines.add("\t\t(");
		lines.add("\t\t\t\to instanceof String");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallSemiOnOwnLineFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\to instanceof String");
		lines.add("\t\t)");
		lines.add("\t\t;");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallSpaceBeforeOpenParenFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue (o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void multiLineCallStringLiteralContainingNameIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tString s = \"earlier: assertTrue\";");
		lines.add("\t\tassertTrue");
		lines.add("\t\t(");
		lines.add("\t\t\to instanceof String");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 3, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
	}

	@Test
	public void multiLineCallSuffixIdentNotMatched() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrueX");
		lines.add("\t\t(o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallWithBlockCommentBeforeOpenParenFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue /* note */ (o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void multiLineCallWithCommentBetweenCloseAndSemiSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(o instanceof String) /* note */ ;");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineCallWithLineCommentBeforeOpenParenFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue // note");
		lines.add("\t\t(o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(List.of("\t\tassertInstanceOf(String.class, o);"), result.replacement());
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void multiLineCallWithLineCommentBetweenCloseAndSemiSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(o instanceof String) // note");
		lines.add("\t\t;");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLineGenericTypeSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\to instanceof java.util.List<String>");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void multiLinePatternBindingSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(");
		lines.add("\t\t\t\to instanceof String s");
		lines.add("\t\t);");
		final var attempt = fixer.fix(lines, 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void negatedAssertFalseFlipsToAssertInstanceOf() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertFalse;");
		final var call = "\t\tassertFalse(!(o instanceof Integer));";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(Integer.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void negatedAssertTrueFlipsToAssertNotInstanceOf() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var call = "\t\tassertTrue(!(o instanceof String));";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertNotInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertNotInstanceOf"), result.importsToAdd());
	}

	@Test
	public void nestedTypeNamePreserved() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof java.util.Map.Entry);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(java.util.Map.Entry.class, o);", result.replacement().getFirst());
	}

	@Test
	public void nonAssertStaticImportLeavesImportsEmpty() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static java.util.Objects.requireNonNull;");
		final var call = "\t\tAssertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tAssertions.assertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void noStaticImportLeavesImportsEmpty() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tAssertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tAssertions.assertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void parensAroundNegatedInstanceOfFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue((!(o instanceof String)));";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertNotInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void parenthesizedInstanceOfArgDoubleParenFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(((o instanceof String)));";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void parenthesizedInstanceOfArgFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue((o instanceof String));";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void parenthesizedInstanceOfArgWithJunit4MessageFirst() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"msg\", (o instanceof String));";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o, \"msg\");", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void parenthesizedInstanceOfArgWithJunit5MessageLast() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue((o instanceof String), \"msg\");";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o, \"msg\");", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void patternBindingSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof String s);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void patternBindingSkippedJunit4MessageFirst() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"msg\", o instanceof String s);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void qualifiedHeuristicNotSpoofedByStringLiteral() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.Assert.assertTrue;");
		final var call = "\t\tassertTrue(\"Assertions.assertInstanceOf\".equals(s) && o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void qualifiedJunit5CallPreservesPrefix() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tAssertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tAssertions.assertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void sameLineEarlierIdentWithMatchingNameIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tString msg = assertTrue + \" x\"; Assertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tString msg = assertTrue + \" x\"; Assertions.assertInstanceOf(String.class, o);",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void stringLiteralContainingAssertTrueIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tString s = \"call assertTrue(here)\"; assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tString s = \"call assertTrue(here)\"; assertInstanceOf(String.class, o);",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void textBlockMessageMultiLineFixed() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var lines = new ArrayList<>(imports);
		lines.add("");
		lines.add("\t\tassertTrue(\"\"\"");
		lines.add("\t\t\tshould be a string\"\"\", o instanceof String);");
		final var attempt = fixer.fix(lines, 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				List.of(
						"\t\tassertInstanceOf(String.class, o, \"\"\"",
						"\t\t\tshould be a string\"\"\");"
				),
				result.replacement()
		);
		assertEquals(2, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void textBlockSingleLineArgFixed() {
		// synthetic single-line `"""..."""` (not valid Java text-block syntax) is now
		// rewritten now; the splitter's quote-state tracker handles the alternation.
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"\"\"some text\"\"\", o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tassertInstanceOf(String.class, o, \"\"\"some text\"\"\");",
				result.replacement().getFirst()
		);
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
	}

	@Test
	public void threeArgsSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(o instanceof String, \"msg\", \"extra\");";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void tripleNegationFlipsLikeSingle() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.assertTrue;");
		final var call = "\t\tassertTrue(!!!(o instanceof String));";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertNotInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertNotInstanceOf"), result.importsToAdd());
	}

	@Test
	public void unrelatedAssertionSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertEquals(5, 3 + 2);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void unrelatedWildcardDoesNotBlockJunit5Scan() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static java.util.Arrays.*;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void whitespaceOnlyArgsSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(   );";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void whitespaceTolerantImportRecognized() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"\timport   static  org.junit.jupiter.api.Assertions.assertTrue ;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertEquals(Set.of("static org.junit.jupiter.api.Assertions.assertInstanceOf"), result.importsToAdd());
	}

	@Test
	public void wildcardStaticImportSkipsAddingImport() {
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of("import static org.junit.jupiter.api.Assertions.*;");
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tassertInstanceOf(String.class, o);", result.replacement().getFirst());
		assertTrue(result.importsToAdd().isEmpty());
	}
}