package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class TestResourcesTest {
	@Test
	public void testCaseNamesReturnsAlphabeticalOrder() {
		final var names = TestResources.caseNames("arraytypestyle");
		final var sorted = names.stream().sorted().toList();
		assertEquals(sorted, names);
	}

	@Test
	public void testCaseNamesThrowsForUnknownTopic() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.caseNames("nonexistent_topic_xyz")
		);
		assertTrue(ex.getMessage().contains("nonexistent_topic_xyz"));
	}

	@Test
	public void testFindOrphanOutputCasesDetectsOrphan() {
		assertEquals(
				List.of("only_in_output"),
				TestResources.findOrphanOutputCases(
						Map.of("shared", List.of()),
						Map.of("shared", List.of(), "only_in_output", List.of())
				)
		);
	}

	@Test
	public void testFindOrphanOutputCasesEmptyWhenInputsCoverOutputs() {
		assertTrue(TestResources.findOrphanOutputCases(
				Map.of("a", List.of(), "b", List.of()),
				Map.of("a", List.of())
		).isEmpty());
	}

	@Test
	public void testFindOrphanOutputCasesReportsAllWhenInputsEmpty() {
		final var orphans = TestResources.findOrphanOutputCases(
				Map.of(),
				Map.of("a", List.of(), "b", List.of())
		);
		assertEquals(2, orphans.size());
		assertTrue(orphans.contains("a"));
		assertTrue(orphans.contains("b"));
	}

	@Test
	public void testLoadCaseSliceVariantEmptyUsesBaseOutput() throws Exception {
		final var slice = TestResources.loadCaseSlice("preferstandardcharsets", "builtin_functions", "");
		assertTrue(
				slice.fixedLines().stream().anyMatch(l -> l.contains("StandardCharsets.UTF_8")),
				"empty variant suffix should resolve to cases.out.java (minSdk>=19 content)"
		);
	}

	@Test
	public void testLoadCaseSliceVariantExistsPrefersVariant() throws Exception {
		final var slice = TestResources.loadCaseSlice("preferstandardcharsets", "builtin_functions", "minSdk-18");
		assertTrue(
				slice.fixedLines().stream().noneMatch(l -> l.contains("StandardCharsets.UTF_8")),
				"minSdk-18 variant (check gated off) should NOT have StandardCharsets.UTF_8"
		);
		assertTrue(
				slice.fixedLines().stream().anyMatch(l -> l.contains("\"UTF-8\"")),
				"minSdk-18 variant should retain raw \"UTF-8\" literals"
		);
	}

	@Test
	public void testLoadCaseSliceVariantMissingFallsBackToBase() throws Exception {
		final var slice = TestResources.loadCaseSlice("preferstandardcharsets", "builtin_functions", "minSdk-99");
		assertTrue(
				slice.fixedLines().stream().anyMatch(l -> l.contains("StandardCharsets.UTF_8")),
				"missing variant should fall back to cases.out.java"
		);
	}

	@Test
	public void testTranslateDirectivesEmitsCanonicalImportLine() {
		assertEquals(
				List.of("import java.util.List;", "import static foo.Bar.X;", "body"),
				TestResources.translateDirectives(List.of(
						"\t// imports: java.util.List",
						"// imports:   static foo.Bar.X  ",
						"body"
				))
		);
	}

	@Test
	public void testTranslateDirectivesEmptyFqcnNoTrailingSpaceThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.translateDirectives(List.of("// imports:"))
		);
		assertTrue(ex.getMessage().contains("empty FQCN"));
	}

	@Test
	public void testTranslateDirectivesEmptyFqcnThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.translateDirectives(List.of("// imports: "))
		);
		assertTrue(ex.getMessage().contains("empty FQCN"));
	}

	@Test
	public void testTranslateDirectivesEmptyPackageThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.translateDirectives(List.of("// package: "))
		);
		assertTrue(ex.getMessage().contains("empty package name"));
	}

	@Test
	public void testTranslateDirectivesMalformedPackageThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.translateDirectives(List.of("// package:x"))
		);
		assertTrue(ex.getMessage().contains("malformed directive"));
	}

	@Test
	public void testTranslateDirectivesMissingSpaceAfterColonThrows() {
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> TestResources.translateDirectives(List.of("// imports:java.util.List"))
		);
		assertTrue(ex.getMessage().contains("malformed directive"));
	}

	@Test
	public void testTranslateDirectivesPackageDirectiveEmitsPackageLine() {
		assertEquals(
				List.of("package x;", "import foo.Foo;", "body"),
				TestResources.translateDirectives(List.of(
						"\t// package: x",
						"// imports: foo.Foo",
						"body"
				))
		);
	}

	@Test
	public void testTranslateDirectivesVerbatimImportLineWithComment() {
		assertEquals(
				List.of("import foo.Foo; // historical note", "/* legacy */ import foo.Foo;", "body"),
				TestResources.translateDirectives(List.of(
						"// imports: import foo.Foo; // historical note",
						"// imports: /* legacy */ import foo.Foo;",
						"body"
				))
		);
	}
}