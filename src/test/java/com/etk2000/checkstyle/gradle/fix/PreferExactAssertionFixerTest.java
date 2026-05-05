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
		// default fixture: a JUnit 5 Assertions non-static import is in scope
		return List.of("import org.junit.jupiter.api.Assertions;", "", body);
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
		// AssertJ's `assertThat` starts with "assert" but is not in the JUnit set;
		// fixer must not pick its FQN prefix when adding the assertInstanceOf import.
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
		// IMPORT_PATTERN admits `$` for inner-class FQNs; the parser must accept such
		// import lines and not bail out as if they were non-import text.
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
		// !!(x instanceof Y) cancels — assertTrue stays positive (assertInstanceOf)
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
		// !!(x instanceof Y) cancels — assertFalse stays negative (assertNotInstanceOf)
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
	public void instanceofInsideParensInOtherArgIgnored() {
		// the FIRST arg contains " instanceof " but inside parens; the SECOND arg has the
		// top-level instanceof. The fixer must pick arg 1, not arg 0.
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
		// In a mixed-imports file, a QUALIFIED `Assertions.assertTrue` is unambiguous —
		// the qualifier names JUnit 5 explicitly, so the rewrite is safe.
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import static org.junit.Assert.assertTrue;",
				"import org.junit.jupiter.api.Assertions;"
		);
		final var call = "\t\tAssertions.assertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals("\t\tAssertions.assertInstanceOf(String.class, o);", result.replacement().getFirst());
	}

	@Test
	public void junit4And5MixedImportsSkipUnqualified() {
		// JUnit 4 + JUnit 5 in same file: the unqualified `assertTrue` resolves through
		// JUnit 4 today; rewriting would silently swap frameworks, so we skip.
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
		// JUnit 4's Assert class has no assertInstanceOf — fixer must skip rather than
		// emit a rewrite that won't compile.
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
	public void junit4NonStaticAndJunit5StaticMixedSkipsUnqualified() {
		// Pin conservative behavior: a non-static `import org.junit.Assert;` plus a JUnit 5
		// static import skips the unqualified rewrite. The skip is over-conservative
		// (the unqualified call resolves through JUnit 5's static import, so a rewrite
		// would be safe), but the simple-name match in `hasJunit4AssertImport` errs on
		// the side of caution rather than silently swapping frameworks on a bug.
		final var fixer = new PreferExactAssertionFixer();
		final var imports = List.of(
				"import org.junit.Assert;",
				"import static org.junit.jupiter.api.Assertions.assertTrue;"
		);
		final var call = "\t\tassertTrue(o instanceof String);";
		final var attempt = fixer.fix(toLines(imports, call), imports.size() + 1, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
	}

	@Test
	public void junit4PackageWildcardWithJunit5RewritesUnqualified() {
		// `import org.junit.*;` is a TYPE wildcard — it brings `Assert` (and `Test` etc.)
		// into scope as types but does NOT statically import `assertTrue`. So the
		// unqualified call resolves through the JUnit 5 static import. The rewrite is
		// safe, and `hasJunit4AssertImport` correctly returns false (the wildcard's
		// stripped FQN is `org.junit`, simple name `junit`, not `Assert`).
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
		// Ordering: a JUnit 5 wildcard `Assertions.*` followed by a JUnit 5 single static
		// import. addAssertImport must short-circuit on the wildcard before reaching the
		// single import; the wildcard already covers the new method, so no add needed.
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
	public void multiLineCallSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var lines = List.of(
				"\t\tassertTrue(",
				"\t\t\t\to instanceof String",
				"\t\t);"
		);
		final var attempt = fixer.fix(lines, 0, 0);
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
		// A string literal containing "Assertions.assertInstanceOf" must not trick the
		// qualified-detection into skipping the import-presence guard. With no Assertions
		// import in scope, this should skip rather than emit non-compiling code.
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
	public void stringLiteralContainingAssertTrueIgnored() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tString s = \"call assertTrue(here)\"; assertTrue(o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		final var result = assertInstanceOf(FixResult.class, attempt);
		assertEquals(
				"\t\tString s = \"call assertTrue(here)\"; assertInstanceOf(String.class, o);",
				result.replacement().getFirst()
		);
	}

	@Test
	public void textBlockArgSkipped() {
		final var fixer = new PreferExactAssertionFixer();
		final var line = "\t\tassertTrue(\"\"\"some text\"\"\", o instanceof String);";
		final var attempt = fixer.fix(withJunit5(line), 2, 0);
		assertNotNull(attempt);
		assertInstanceOf(SkipResult.class, attempt);
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
		// !!!(x instanceof Y) — odd parity, assertTrue flips to assertNotInstanceOf
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
		// A wildcard import for an unrelated class must not short-circuit the scan;
		// the subsequent JUnit 5 single import must still be found.
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
		// Imports with non-canonical whitespace (extra spaces, leading tab, space before `;`)
		// must still be parsed and recognized as JUnit 5 Assertions imports.
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