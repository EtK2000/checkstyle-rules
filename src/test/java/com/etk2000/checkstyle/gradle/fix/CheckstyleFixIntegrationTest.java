package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction.ApplyFixesResult;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CheckstyleFixIntegrationTest {
	record FixOutput(@Nonnull String content, @Nonnull ApplyFixesResult result) {
	}

	@Nonnull
	private static AuditEvent createEvent(
			int line,
			@Nonnull SeverityLevel severity,
			@Nullable String moduleId,
			@Nonnull Class<?> sourceClass
	) {
		final var violation = new Violation(
				line, 0, "", "", null, severity, moduleId, sourceClass, "test"
		);
		return new AuditEvent(new Object(), "Test.java", violation);
	}

	@TempDir
	Path tempDir;

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file) throws Exception {
		return runChecks(file, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		for (var checkName : CheckstyleFixAction.FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.endsWith("FinalLocalVariableCheck"))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
			if (checkName.endsWith("PreferMathMethodCheck")
					|| checkName.endsWith("PreferSpecificApiCheck")
					|| checkName.endsWith("PreferStandardCharsetsCheck")
					|| checkName.endsWith("PreferStaticImportCheck"))
				checkConfig.addProperty("minSdk", minSdk);
			treeWalkerConfig.addChild(checkConfig);
		}

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		// Checker-level regex modules
		final var blankAfterBreakConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterBreakConfig.addProperty("id", "BlankLineAfterBreak");
		blankAfterBreakConfig.addProperty("format", "break\\s*;\\n[^\\S\\n]*(case |default[\\s:])");
		blankAfterBreakConfig.addProperty("message", "Add a blank line after break; before the next case/default.");
		checkerConfig.addChild(blankAfterBreakConfig);

		final var blankAfterClassBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterClassBraceConfig.addProperty("id", "NoBlankLineAfterClassBrace");
		blankAfterClassBraceConfig.addProperty("format", "(class|interface|enum|record)\\s+\\w[^{]*\\{\\s*\\n\\s*\\n");
		blankAfterClassBraceConfig.addProperty("message", "No blank line at start of a class/interface/enum/record.");
		checkerConfig.addChild(blankAfterClassBraceConfig);

		final var blankBeforeCloseBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankBeforeCloseBraceConfig.addProperty("id", "NoBlankLineBeforeClosingBrace");
		blankBeforeCloseBraceConfig.addProperty("format", "\\n[^\\S\\n]*\\n[^\\S\\n]*\\}");
		blankBeforeCloseBraceConfig.addProperty("message", "No blank line before closing brace.");
		checkerConfig.addChild(blankBeforeCloseBraceConfig);

		final var doubleBlankConfig = new DefaultConfiguration("RegexpMultiline");
		doubleBlankConfig.addProperty("id", "NoDoubleBlankLines");
		doubleBlankConfig.addProperty("format", "\\n\\s*\\n\\s*\\n");
		doubleBlankConfig.addProperty("message", "No double blank lines.");
		checkerConfig.addChild(doubleBlankConfig);

		final var trailingNewlineConfig = new DefaultConfiguration("RegexpMultiline");
		trailingNewlineConfig.addProperty("id", "NoTrailingNewline");
		trailingNewlineConfig.addProperty("format", "\\n\\z");
		trailingNewlineConfig.addProperty("message", "File must not end with a trailing newline.");
		checkerConfig.addChild(trailingNewlineConfig);

		final var trailingWsConfig = new DefaultConfiguration("RegexpSingleline");
		trailingWsConfig.addProperty("id", "NoTrailingWhitespace");
		trailingWsConfig.addProperty("format", "[ \\t]+$");
		trailingWsConfig.addProperty("message", "No trailing whitespace.");
		checkerConfig.addChild(trailingWsConfig);

		final var checker = new Checker();
		final var violations = new ArrayList<AuditEvent>();
		try {
			checker.setModuleClassLoader(getClass().getClassLoader());
			checker.configure(checkerConfig);

			checker.addListener(new AuditListener() {
				@Override
				public void addError(@Nonnull AuditEvent event) {
					violations.add(event);
				}

				@Override
				public void addException(@Nonnull AuditEvent event, @Nonnull Throwable throwable) {
				}

				@Override
				public void auditFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void auditStarted(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileFinished(@Nonnull AuditEvent event) {
				}

				@Override
				public void fileStarted(@Nonnull AuditEvent event) {
				}
			});

			checker.process(List.of(file));
		}
		finally {
			checker.destroy();
		}
		return violations;
	}

	@Nonnull
	private FixOutput runFixAndGetResult(@Nonnull File file) throws Exception {
		return runFixAndGetResult(file, String.valueOf(Integer.MAX_VALUE));
	}

	@Nonnull
	private FixOutput runFixAndGetResult(@Nonnull File file, @Nonnull String minSdk) throws Exception {
		final var violations = runChecks(file, minSdk);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		return new FixOutput(String.join("\n", lines), result);
	}

	@Nonnull
	private String runFixMultiPass(@Nonnull File file) throws Exception {
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		for (var pass = 0; pass < 2; ++pass) {
			Files.writeString(file.toPath(), String.join("\n", lines));
			final var violations = runChecks(file);
			if (CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS).fixCount() == 0)
				break;
		}
		return String.join("\n", lines);
	}

	@Nonnull
	private ApplyFixesResult runFixPipeline(@Nonnull File file) throws Exception {
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		return CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
	}

	@Test
	public void testAllFixedNoSkipReasons() throws Exception {
		final var file = tempDir.resolve("AllFixed.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tlong x = 3000000000l;\n\tlong y = 4000000000l;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().skippedReasons().isEmpty());
	}

	@Test
	public void testAllSkippedHasReasons() throws Exception {
		final var file = tempDir.resolve("AllSkipped.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint b;\n\tint a;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("FieldSortingCheck"));
		final var reasons = output.result().skippedReasons().get("FieldSortingCheck");
		assertFalse(reasons.isEmpty());
		assertEquals(SkipMessages.FIELD_SORT_SKIP, reasons.getFirst());
	}

	@Test
	public void testAnnotationOwnLineBlank() throws Exception {
		final var file = tempDir.resolve("AnnBlank.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankAfterBlockComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankBlock.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\t/* block */\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/* block */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankAfterJavadoc() throws Exception {
		final var file = tempDir.resolve("AnnBlankJavadoc.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\t/** Javadoc. */\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/** Javadoc. */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankAfterLineComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankComment.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\t// comment\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t// comment\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankAfterMultiLineBlockComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankMultiBlock.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\t/*\n\t * comment\n\t */\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/*\n\t * comment\n\t */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankAfterMultiLineBlockCommentWithInternalBlank() throws Exception {
		final var file = tempDir.resolve("AnnBlankAfterMultiBlockInternal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\t/*\n\t * comment\n\t *\n\t * more\n\t */\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/*\n\t * comment\n\t *\n\t * more\n\t */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankBeforeBlockComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankBeforeBlock.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\t/* block */\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/* block */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankBeforeJavadoc() throws Exception {
		final var file = tempDir.resolve("AnnBlankBeforeJavadoc.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\t/** Javadoc. */\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/** Javadoc. */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankBeforeLineComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankBeforeLine.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\t// comment\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t// comment\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankBeforeMultiLineBlockComment() throws Exception {
		final var file = tempDir.resolve("AnnBlankBeforeMultiBlock.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\t/*\n\t * comment\n\t */\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t/*\n\t * comment\n\t */\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineBlankMultiLine() throws Exception {
		final var file = tempDir.resolve("AnnBlankMulti.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@SuppressWarnings({\n\t\t\"unchecked\",\n\t\t\"rawtypes\"\n\t})\n\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@SuppressWarnings({\n\t\t\"unchecked\",\n\t\t\"rawtypes\"\n\t})\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineReorder() throws Exception {
		final var file = tempDir.resolve("AnnReorder.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Override\n\t@Deprecated\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t@Override\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationOwnLineSplit() throws Exception {
		final var file = tempDir.resolve("AnnOwn.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Override @Deprecated void method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\t@Override\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationSameLineInlineReorder() throws Exception {
		final var file = tempDir.resolve("AnnReord.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid method(@Override @Deprecated String param) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid method(@Deprecated @Override String param) {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationSameLineJoin() throws Exception {
		final var file = tempDir.resolve("AnnSame.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid method(\n\t\t\t@Deprecated\n\t\t\tString param\n\t) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid method(\n\t\t\t@Deprecated String param\n\t) {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationSyntaxEmptyParens() throws Exception {
		final var file = tempDir.resolve("AnnSynEmpty.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated()\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@Deprecated\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testAnnotationSyntaxExplicitValue() throws Exception {
		final var file = tempDir.resolve("AnnSynValue.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\t@SuppressWarnings(value = \"unchecked\")\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\t@SuppressWarnings(\"unchecked\")\n\tvoid method() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testApplyFixesSkipsUnknownViolations() throws Exception {
		final var file = tempDir.resolve("Unknown.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1, 2,};\n}");

		final var violations = runChecks(file);
		assertFalse(violations.isEmpty());

		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, Map.of(), Map.of());
		assertEquals(0, result.fixCount());
		assertFalse(result.skippedReasons().isEmpty());
		assertTrue(
				result.skippedReasons().values().stream()
						.anyMatch(reasons -> reasons.contains(SkipMessages.FIX_NO_FIXER))
		);
	}

	@Test
	public void testArrayTrailingComma() throws Exception {
		final var file = tempDir.resolve("Arr.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1, 2,};\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint[] a = {1, 2};\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineAfterBreak() throws Exception {
		final var file = tempDir.resolve("Break.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\t\t\tcase 2:\n\t\t\t\tbreak;\n\t\t\tdefault:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\n\t\t\tcase 2:\n\t\t\t\tbreak;\n\n\t\t\tdefault:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineAfterBreakFallThrough() throws Exception {
		final var file = tempDir.resolve("BreakFall.java").toFile();
		// fall-through cases (no break between case 1 and case 2) should be untouched
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\tcase 2:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\t\t\tcase 3:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\tcase 2:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\n\t\t\tcase 3:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineAfterClassBrace() throws Exception {
		final var file = tempDir.resolve("ClassBrace.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\n\tint x;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineAfterClassBraceCombinedWithBeforeClose() throws Exception {
		final var file = tempDir.resolve("ClassBraceBoth.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\n\tint x;\n\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineAfterClassBraceMultiLine() throws Exception {
		final var file = tempDir.resolve("ClassBraceMulti.java").toFile();
		Files.writeString(file.toPath(), "class T\n\t\textends Base {\n\n\tint x;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T\n\t\textends Base {\n\tint x;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineBeforeClosingBrace() throws Exception {
		final var file = tempDir.resolve("CloseBrace.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineBetweenSingleCases() throws Exception {
		final var file = tempDir.resolve("Switch.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testBlankLineBetweenSingleCasesMultipleBlankLines() throws Exception {
		final var file = tempDir.resolve("SwitchMulti.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\n\n\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testCleanFileNoViolationsNoReasons() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid method() {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().isEmpty());
	}

	@Test
	public void testDoubleBlankLines() throws Exception {
		final var file = tempDir.resolve("Dbl.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n\n\tint y;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n\n\tint y;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoubleBlankLinesTriple() throws Exception {
		final var file = tempDir.resolve("Dbl3.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n\n\n\tint y;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n\n\tint y;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileBracedTier1() throws Exception {
		final var file = tempDir.resolve("DoTier1.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\t--x;\n\t\t} while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileBracedTier2() throws Exception {
		final var file = tempDir.resolve("DoTier2.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\tSystem.out.println(x);\n\t\t} while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileBracedTier3() throws Exception {
		final var file = tempDir.resolve("DoTier3.java").toFile();
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo {\n\t\t\tlist.subList(0, 1).clear();\n\t\t} while (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileBracedTier3ComplexRhs() throws Exception {
		final var file = tempDir.resolve("DoTier3Rhs.java").toFile();
		final var input = "class T {\n\tvoid f(int x, int y) {\n\t\tdo {\n\t\t\tx += 5 * y;\n\t\t} while (x < 100);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x, int y) {\n\t\tdo\n\t\t\tx += 5 * y;\n\t\twhile (x < 100);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileMissingBraces() throws Exception {
		final var file = tempDir.resolve("DoMissing.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\tif (x > 0)\n\t\t\t\t--x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\tif (x > 0)\n\t\t\t\t--x;\n\t\t} while (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileOwnLineTier1() throws Exception {
		final var file = tempDir.resolve("DoOwn1.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\t--x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileOwnLineTier2() throws Exception {
		final var file = tempDir.resolve("DoOwn2.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\tSystem.out.println(x);\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileTier1SplitJoins() throws Exception {
		final var file = tempDir.resolve("DoT1Split.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo --x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileTier2WhileOnSameLine() throws Exception {
		final var file = tempDir.resolve("DoSplit.java").toFile();
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x); while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileTier3AsTier2() throws Exception {
		final var file = tempDir.resolve("DoT3T2.java").toFile();
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo list.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testDoWhileTier3OneLiner() throws Exception {
		final var file = tempDir.resolve("DoT3One.java").toFile();
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo list.subList(0, 1).clear(); while (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingComma() throws Exception {
		final var file = tempDir.resolve("Color.java").toFile();
		Files.writeString(file.toPath(), "enum Color {\n\tRED,\n\tGREEN,\n}");

		final var output = runFixAndGetResult(file);
		// FieldSortingFixer sorts GREEN before RED; trailing comma fixer removes the comma
		assertEquals("enum Color {\n\tGREEN,\n\tRED\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolon() throws Exception {
		final var file = tempDir.resolve("Semi.java").toFile();
		Files.writeString(file.toPath(), "enum Semi {\n\tA,\n\tB;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum Semi {\n\tA,\n\tB\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonConstantBody() throws Exception {
		final var file = tempDir.resolve("SemiBody.java").toFile();
		Files.writeString(file.toPath(), "enum SemiBody {\n\tX {\n\t\t@Override\n\t\tpublic String toString() {\n\t\t\treturn \"x\";\n\t\t}\n\t};\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum SemiBody {\n\tX {\n\t\t@Override\n\t\tpublic String toString() {\n\t\t\treturn \"x\";\n\t\t}\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonDeepTab() throws Exception {
		final var file = tempDir.resolve("SemiTab.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tenum E {\n\t\tX;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tenum E {\n\t\tX\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonDeleteLine() throws Exception {
		final var file = tempDir.resolve("SemiEmpty.java").toFile();
		Files.writeString(file.toPath(), "enum SemiEmpty {\n\t;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum SemiEmpty {\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonDeleteOwnLine() throws Exception {
		final var file = tempDir.resolve("SemiOwn.java").toFile();
		Files.writeString(file.toPath(), "enum SemiOwn {\n\tX\n\t;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum SemiOwn {\n\tX\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonInline() throws Exception {
		final var file = tempDir.resolve("SemiInline.java").toFile();
		Files.writeString(file.toPath(), "enum SemiInline { X; }");

		final var output = runFixAndGetResult(file);
		assertEquals("enum SemiInline { X }", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testEnumTrailingSemicolonWithComment() throws Exception {
		final var file = tempDir.resolve("SemiComment.java").toFile();
		Files.writeString(file.toPath(), "enum SemiComment {\n\tX; // remark\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum SemiComment {\n\tX // remark\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testExplicitInitialization() throws Exception {
		final var file = tempDir.resolve("Init.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint x = 0;\n\tObject o = null;\n\tboolean b = false;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n\tObject o;\n\tboolean b;\n}", output.content());
		assertEquals(3, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testExplicitInitializationMultiDeclaration() throws Exception {
		final var file = tempDir.resolve("InitMulti.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint a = 0, b;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint a, b;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testExtractCheckShortNameModuleIdPreferred() {
		final var event = createEvent(1, SeverityLevel.ERROR, "NoDoubleBlankLines", Object.class);
		final var lines = new ArrayList<>(List.of("line1"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of(), Map.of("NoDoubleBlankLines", nullFixer)
		);
		assertTrue(result.skippedReasons().containsKey("NoDoubleBlankLines"));
		assertTrue(result.skippedReasons().get("NoDoubleBlankLines").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testExtractCheckShortNameSourceNameFallback() {
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("line1"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event)),
				Map.of("java.lang.Object", nullFixer),
				Map.of()
		);
		assertTrue(result.skippedReasons().containsKey("Object"));
		assertTrue(result.skippedReasons().get("Object").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testFieldSortingEnumAlreadySorted() throws Exception {
		final var file = tempDir.resolve("SortedEnum.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tALPHA,\n\tBETA\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\tBETA\n}", output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumInnerClass() throws Exception {
		final var file = tempDir.resolve("InnerEnum.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tenum E {\n\t\tBETA,\n\t\tALPHA\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tenum E {\n\t\tALPHA,\n\t\tBETA\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumReorder() throws Exception {
		final var file = tempDir.resolve("EnumReorder.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tBETA,\n\tALPHA\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\tBETA\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumSameLine() throws Exception {
		final var file = tempDir.resolve("EnumSameLine.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tALPHA, BETA\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\tBETA\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumSameLineAndReorder() throws Exception {
		final var file = tempDir.resolve("EnumSameLineReorder.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tZEBRA, ALPHA\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\tZEBRA\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumSemicolon() throws Exception {
		final var file = tempDir.resolve("EnumSemi.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tBETA,\n\tALPHA;\n\tint x;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\tBETA;\n\tint x;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumWithAnnotations() throws Exception {
		final var file = tempDir.resolve("EnumAnnot.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\t@Deprecated\n\tBETA,\n\tALPHA\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA,\n\t@Deprecated\n\tBETA\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumWithArgs() throws Exception {
		final var file = tempDir.resolve("EnumArgs.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tCHERRY(\"r\"),\n\tAPPLE(\"g\")\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tAPPLE(\"g\"),\n\tCHERRY(\"r\")\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumWithBodies() throws Exception {
		final var file = tempDir.resolve("EnumBodies.java").toFile();
		final var input = "enum T {\n\tSUB {\n\t\tint v() {\n\t\t\treturn 1;\n\t\t}\n\t},\n"
				+ "\tADD {\n\t\tint v() {\n\t\t\treturn 0;\n\t\t}\n\t};\n"
				+ "\tabstract int v();\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		final var expectedOutput = "enum T {\n\tADD {\n\t\tint v() {\n\t\t\treturn 0;\n\t\t}\n\t},\n"
				+ "\tSUB {\n\t\tint v() {\n\t\t\treturn 1;\n\t\t}\n\t};\n"
				+ "\tabstract int v();\n}";
		assertEquals(
				expectedOutput,
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingEnumWithTrailingComments() throws Exception {
		final var file = tempDir.resolve("EnumComments.java").toFile();
		Files.writeString(file.toPath(), "enum T {\n\tBETA, // b\n\tALPHA // a\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum T {\n\tALPHA, // a\n\tBETA // b\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFieldSortingFieldViolationNotFixed() throws Exception {
		final var file = tempDir.resolve("FieldOrder.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tstatic final String Z = \"z\";\n\tstatic final int A = 0;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tstatic final String Z = \"z\";\n\tstatic final int A = 0;\n}", output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFinalLocalVariable() throws Exception {
		final var file = tempDir.resolve("Final.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x = 5;\n\t\tvar y = \"hello\";\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t\tfinal var y = \"hello\";\n\t}\n}",
				output.content()
		);
		assertEquals(3, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFinalLocalVariableTabIndented() throws Exception {
		final var file = tempDir.resolve("FinalTab.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tif (true) {\n\t\t\tint x = 5;\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tif (true) {\n\t\t\tfinal var x = 5;\n\t\t}\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFixerReturnsNullForDuplicateOnSameLine() throws Exception {
		final var file = tempDir.resolve("Multi.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x, y;\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);

		assertEquals("class T {\n\tvoid f() {\n\t\tfinal int x, y;\n\t}\n}", String.join("\n", lines));
		assertEquals(1, result.fixCount());
	}

	@Test
	public void testFixLambdaParamRemoveParens() throws Exception {
		final var file = tempDir.resolve("LamParen.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((x) -> System.out.println(x));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach(x -> System.out.println(x));\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFixLambdaParamRemoveType() throws Exception {
		final var file = tempDir.resolve("LamType.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((String x) -> System.out.println(x));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach(x -> System.out.println(x));\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFixLambdaParamReplaceTypeWithVar() throws Exception {
		final var file = tempDir.resolve("LamVar.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((@A String x) -> System.out.println(x));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((@A var x) -> System.out.println(x));\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFixLambdaParamReplaceTypeWithVarMultiParam() throws Exception {
		final var file = tempDir.resolve("LamVarMulti.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.sort((@A String x, String y) -> x.compareTo(y));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.sort((@A var x, var y) -> x.compareTo(y));\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testFixOrderBottomToTop() throws Exception {
		final var file = tempDir.resolve("Order.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1,};\n\tint[] b = {2,};\n\tint[] c = {3,};\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS);

		assertEquals("class T {\n\tint[] a = {1};\n\tint[] b = {2};\n\tint[] c = {3};\n}", String.join("\n", lines));
		assertEquals(3, result.fixCount());
	}

	@Test
	public void testMinSdkGatesCollectionsSort() throws Exception {
		final var file = tempDir.resolve("MinSort.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tCollections.sort(list);\n\t}\n}");

		// minSdk 23: below API 24 threshold, sort should NOT be fixed
		final var below = runFixAndGetResult(file, "23");
		assertEquals(0, below.result().fixCount());

		// minSdk 24: at threshold, sort should be fixed
		final var at = runFixAndGetResult(file, "24");
		assertEquals(1, at.result().fixCount());
	}

	@Test
	public void testMinSdkGatesStringFormat() throws Exception {
		final var file = tempDir.resolve("MinFmt.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString run(String name) {\n\t\treturn String.format(\"Hello %s\", name);\n\t}\n}");

		// minSdk 33: below API 34 threshold, formatted should NOT be fixed
		final var below = runFixAndGetResult(file, "33");
		assertEquals(0, below.result().fixCount());

		// minSdk 34: at threshold, formatted should be fixed
		final var at = runFixAndGetResult(file, "34");
		assertEquals(1, at.result().fixCount());
	}

	@Test
	public void testMinSdkGatesToArray() throws Exception {
		final var file = tempDir.resolve("MinArr.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tString[] run(List<String> list) {\n\t\treturn list.toArray(new String[0]);\n\t}\n}");

		// minSdk 32: below API 33 threshold, toArray should NOT be fixed
		final var below = runFixAndGetResult(file, "32");
		assertEquals(0, below.result().fixCount());

		// minSdk 33: at threshold, toArray should be fixed
		final var at = runFixAndGetResult(file, "33");
		assertEquals(1, at.result().fixCount());
	}

	@Test
	public void testMixedFixAndSkipFromSameCheck() throws Exception {
		final var file = tempDir.resolve("MixedSameCheck.java").toFile();
		Files.writeString(
				file.toPath(),
				"class T {\n\tenum E {\n\t\tB, A\n\t}\n\tint b;\n\tint a;\n}"
		);

		final var output = runFixAndGetResult(file);
		assertTrue(output.result().fixCount() > 0);
		assertTrue(output.result().skippedReasons().containsKey("FieldSortingCheck"));
	}

	@Test
	public void testMultipleChecksSkipReasons() throws Exception {
		final var file = tempDir.resolve("MultiCheck.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint b;\n\tint a;\n\tint x = 0;\n}");

		final var output = runFixAndGetResult(file);
		assertTrue(output.result().fixCount() > 0);
		assertTrue(output.result().skippedReasons().containsKey("FieldSortingCheck"));
		assertEquals(
				SkipMessages.FIELD_SORT_SKIP,
				output.result().skippedReasons().get("FieldSortingCheck").getFirst()
		);
	}

	@Test
	public void testMultipleSkipReasonsPerCheckAccumulated() {
		final var event1 = createEvent(1, SeverityLevel.WARNING, null, Object.class);
		final var event2 = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("only line"));
		final CheckstyleFixer dummyFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines,
				new ArrayList<>(List.of(event1, event2)),
				Map.of("java.lang.Object", dummyFixer),
				Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().containsKey("Object"));
		final var reasons = result.skippedReasons().get("Object");
		assertEquals(2, reasons.size());
		assertTrue(reasons.contains(SkipMessages.FIX_SEVERITY));
		assertTrue(reasons.contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testMultipleViolationsSameFile() throws Exception {
		final var file = tempDir.resolve("Multi2.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1,};\n\tlong x = 100L;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint[] a = {1};\n\tlong x = 100;\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoFinalParametersCatch() throws Exception {
		final var file = tempDir.resolve("CatchFinal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\ttry {\n\t\t\tSystem.out.println();\n\t\t}\n\t\tcatch (final Exception e) {\n\t\t\tSystem.out.println(e);\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\ttry {\n\t\t\tSystem.out.println();\n\t\t}\n\t\tcatch (Exception e) {\n\t\t\tSystem.out.println(e);\n\t\t}\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoFinalParametersConstructor() throws Exception {
		final var file = tempDir.resolve("CtorFinal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tT(final int x) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tT(int x) {}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoFinalParametersForEach() throws Exception {
		final var file = tempDir.resolve("ForEachFinal.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tfor (final var item : list)\n\t\t\tSystem.out.println(item);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tfor (var item : list)\n\t\t\tSystem.out.println(item);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoFinalParametersMethod() throws Exception {
		final var file = tempDir.resolve("ParamFinal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(final int x, final String y) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tvoid f(int x, String y) {}\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoFinalParametersSecondParam() throws Exception {
		final var file = tempDir.resolve("SecondFinal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int x, final String y) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tvoid f(int x, String y) {}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoViolations() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		final var input = "class Clean {\n\tint[] a = {1, 2};\n\tint x = 100;\n}";
		Files.writeString(file.toPath(), input);

		final var result = runFixPipeline(file);
		assertEquals(0, result.fixCount());
		assertFalse(result.needsSecondPass());
	}

	@Test
	public void testNullFixerReturnTracksNotFixable() {
		final var event = createEvent(1, SeverityLevel.ERROR, null, Object.class);
		final var lines = new ArrayList<>(List.of("content"));
		final CheckstyleFixer nullFixer = (l, i, c) -> null;
		final var result = CheckstyleFixAction.applyFixes(
				lines, new ArrayList<>(List.of(event)), Map.of("java.lang.Object", nullFixer), Map.of()
		);
		assertEquals(0, result.fixCount());
		assertTrue(result.skippedReasons().containsKey("Object"));
		assertTrue(result.skippedReasons().get("Object").contains(SkipMessages.FIX_NOT_FIXABLE));
	}

	@Test
	public void testPostfixDecrement() throws Exception {
		final var file = tempDir.resolve("Decr.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tint i = 5;\n\t\ti--;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid run() {\n\t\tvar i = 5;\n\t\t--i;\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPostfixIncrement() throws Exception {
		final var file = tempDir.resolve("Incr.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tint i = 0;\n\t\ti++;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid run() {\n\t\tvar i = 0;\n\t\t++i;\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPostfixIncrementForLoop() throws Exception {
		final var file = tempDir.resolve("IncrFor.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tfor (var i = 0; i < 10; i++)\n\t\t\tSystem.out.println(i);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid run() {\n\t\tfor (var i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationAddAll() throws Exception {
		final var file = tempDir.resolve("BulkAdd.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\tfor (var item : source)\n\t\t\ttarget.add(item);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\ttarget.addAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationArrayFill() throws Exception {
		final var file = tempDir.resolve("BulkFill.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] arr) {\n\t\tfor (var i = 0; i < arr.length; ++i)\n\t\t\tarr[i] = 0;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nclass T {\n\tvoid f(int[] arr) {\n\t\tArrays.fill(arr, 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationArrayFillBraced() throws Exception {
		final var file = tempDir.resolve("BulkFillBraced.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] arr) {\n\t\tfor (var i = 0; i < arr.length; ++i) {\n\t\t\tarr[i] = 0;\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nclass T {\n\tvoid f(int[] arr) {\n\t\tArrays.fill(arr, 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationArrayFillSourceNameStartsWithLength() throws Exception {
		// End-to-end: source array named `lengthValues`. The `.length` substring matcher
		// must not match inside the identifier `lengthValues`.
		final var file = tempDir.resolve("BulkFillLengthName.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] lengthValues) {\n\t\tfor (var i = 0; i < lengthValues.length; ++i)\n\t\t\tlengthValues[i] = 0;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nclass T {\n\tvoid f(int[] lengthValues) {\n\t\tArrays.fill(lengthValues, 0);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationArrayFillUnaryPlusValueContainsBracket() throws Exception {
		// End-to-end: UNARY_PLUS wrapping an INDEX_OP is a pure expression, but the top
		// token isn't INDEX_OP so the fill branch runs. Fixer dispatch must route to fill.
		final var file = tempDir.resolve("BulkFillUnaryPlusBracket.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] arr, int[] other) {\n\t\tfor (var i = 0; i < arr.length; ++i)\n\t\t\tarr[i] = +other[0];\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nclass T {\n\tvoid f(int[] arr, int[] other) {\n\t\tArrays.fill(arr, +other[0]);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationArrayFillValueContainsBracket() throws Exception {
		// End-to-end guard: a fill value containing `[` must NOT be misdispatched to
		// `fixArrayCopy` (which would produce a no-op self-copy). Regression for the
		// dispatch bug exposed by `arrayFillDeeplyNestedConstant`.
		final var file = tempDir.resolve("BulkFillBracket.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] arr, int[] a, int[] b) {\n\t\tfor (var i = 0; i < arr.length; ++i)\n\t\t\tarr[i] = -a[b[0]];\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nclass T {\n\tvoid f(int[] arr, int[] a, int[] b) {\n\t\tArrays.fill(arr, -a[b[0]]);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationEntrySetPutAll() throws Exception {
		final var file = tempDir.resolve("BulkEntrySet.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> target, Map<String, String> source) {\n\t\tfor (var entry : source.entrySet())\n\t\t\ttarget.put(entry.getKey(), entry.getValue());\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> target, Map<String, String> source) {\n\t\ttarget.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationEntrySetPutAllBraced() throws Exception {
		final var file = tempDir.resolve("BulkEntrySetBraced.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> target, Map<String, String> source) {\n\t\tfor (var entry : source.entrySet()) {\n\t\t\ttarget.put(entry.getKey(), entry.getValue());\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> target, Map<String, String> source) {\n\t\ttarget.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaAddAll() throws Exception {
		final var file = tempDir.resolve("BulkLambdaAdd.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tlist.forEach(item -> other.add(item));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tother.addAll(list);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaBlockBodyAddAll() throws Exception {
		final var file = tempDir.resolve("BulkBlockAdd.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tlist.forEach(item -> {\n\t\t\tother.add(item);\n\t\t});\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tother.addAll(list);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaBlockBodyBlockCommentWrongTarget() throws Exception {
		// End-to-end guard for Gap #10: a multi-line block comment containing a
		// misleading `.put(` on a different target must not confuse the fixer. The
		// real body uses `real.put`; the comment mentions `target.put`. Output must
		// be `real.putAll(source);`.
		final var file = tempDir.resolve("BulkBlockCommentWrongTarget.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> real) {\n\t\tsource.forEach((k, v) -> {\n\t\t\t/* future cleanup:\n\t\t\t   target.put(k, v);\n\t\t\t*/\n\t\t\treal.put(k, v);\n\t\t});\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> real) {\n\t\treal.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaBlockBodyPutAll() throws Exception {
		final var file = tempDir.resolve("BulkBlockPut.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\tsource.forEach((k, v) -> {\n\t\t\ttarget.put(k, v);\n\t\t});\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\ttarget.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaPreservesLeadingIfStatement() throws Exception {
		final var file = tempDir.resolve("BulkLeadingIf.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(boolean flag, Map<String, String> source, Map<String, String> target) {\n\t\tif (flag) source.forEach((k, v) -> target.put(k, v));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(boolean flag, Map<String, String> source, Map<String, String> target) {\n\t\tif (flag) target.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachLambdaPutAll() throws Exception {
		final var file = tempDir.resolve("BulkPut.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\tsource.forEach((k, v) -> target.put(k, v));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\ttarget.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachMethodRefAdd() throws Exception {
		final var file = tempDir.resolve("BulkRefAdd.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tlist.forEach(other::add);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tother.addAll(list);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachMethodRefMultiLine() throws Exception {
		final var file = tempDir.resolve("BulkRefMulti.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tlist.forEach(\n\t\t\t\tother::add\n\t\t);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list, List<String> other) {\n\t\tother.addAll(list);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationForEachMethodRefPut() throws Exception {
		final var file = tempDir.resolve("BulkRefPut.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\tsource.forEach(target::put);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid f(Map<String, String> source, Map<String, String> target) {\n\t\ttarget.putAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationIndexedAddAll() throws Exception {
		final var file = tempDir.resolve("BulkIdxAdd.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\tfor (var i = 0; i < source.size(); ++i)\n\t\t\ttarget.add(source.get(i));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\ttarget.addAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationIndexedAddAllBraced() throws Exception {
		final var file = tempDir.resolve("BulkIdxAddBraced.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\tfor (var i = 0; i < source.size(); ++i) {\n\t\t\ttarget.add(source.get(i));\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> target, List<String> source) {\n\t\ttarget.addAll(source);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationSystemArraycopy() throws Exception {
		final var file = tempDir.resolve("BulkCopy.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] dst, int[] src) {\n\t\tfor (var i = 0; i < src.length; ++i)\n\t\t\tdst[i] = src[i];\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int[] dst, int[] src) {\n\t\tSystem.arraycopy(src, 0, dst, 0, src.length);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferBulkOperationSystemArraycopyBraced() throws Exception {
		final var file = tempDir.resolve("BulkCopyBraced.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int[] dst, int[] src) {\n\t\tfor (var i = 0; i < src.length; ++i) {\n\t\t\tdst[i] = src[i];\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int[] dst, int[] src) {\n\t\tSystem.arraycopy(src, 0, dst, 0, src.length);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferCollectionInterfaceMultiSameLine() throws Exception {
		final var file = tempDir.resolve("ColMulti.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nimport java.util.HashMap;\nclass T {\n\tvoid f(ArrayList<String> a, HashMap<String, Integer> b) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.ArrayList;\nimport java.util.HashMap;\nimport java.util.List;\nimport java.util.Map;\nclass T {\n\tvoid f(List<String> a, Map<String, Integer> b) {}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferCollectionInterfaceParam() throws Exception {
		final var file = tempDir.resolve("ColParam.java").toFile();
		Files.writeString(file.toPath(), "import java.util.HashSet;\nclass T {\n\tvoid f(HashSet<String> s) {}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.HashSet;\nimport java.util.Set;\nclass T {\n\tvoid f(Set<String> s) {}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferCollectionInterfaceReturn() throws Exception {
		final var file = tempDir.resolve("ColReturn.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nclass T {\n\tArrayList<String> f() {\n\t\treturn new ArrayList<>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.ArrayList;\nimport java.util.List;\nclass T {\n\tList<String> f() {\n\t\treturn new ArrayList<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferCollectionInterfaceReturnImportAlreadyPresent() throws Exception {
		final var file = tempDir.resolve("ColReturnImp.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nimport java.util.List;\nclass T {\n\tArrayList<String> f() {\n\t\treturn new ArrayList<>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		// type replaced, List import already exists so no duplicate
		assertTrue(output.content().contains("List<String> f()"));
		assertFalse(output.content().contains("import java.util.List;\nimport java.util.List;"));
	}

	@Test
	public void testPreferMathMethodAbs() throws Exception {
		final var file = tempDir.resolve("MathAbs.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint f(int a) {\n\t\treturn a < 0 ? -a : a;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint f(int a) {\n\t\treturn Math.abs(a);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferMathMethodClamp() throws Exception {
		final var file = tempDir.resolve("MathClamp.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint f(int v, int lo, int hi) {\n\t\treturn Math.max(lo, Math.min(hi, v));\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint f(int v, int lo, int hi) {\n\t\treturn Math.clamp(v, lo, hi);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferMathMethodMax() throws Exception {
		final var file = tempDir.resolve("MathMax.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint f(int a, int b) {\n\t\treturn a > b ? a : b;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint f(int a, int b) {\n\t\treturn Math.max(a, b);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferMathMethodMaxPreDecrement() throws Exception {
		final var file = tempDir.resolve("MathMaxDec.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint f(int a, int b) {\n\t\treturn --a > b ? a : b;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint f(int a, int b) {\n\t\treturn Math.max(--a, b);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferMathMethodMin() throws Exception {
		final var file = tempDir.resolve("MathMin.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint f(int a, int b) {\n\t\treturn a < b ? a : b;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint f(int a, int b) {\n\t\treturn Math.min(a, b);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferMathMethodSkipsMultilineTernary() throws Exception {
		// check fires on the QUESTION token line, fixer's regex only matches single-line ternaries
		final var file = tempDir.resolve("MathMulti.java").toFile();
		Files.writeString(
				file.toPath(),
				"class T {\n\tint f(int a, int b) {\n\t\treturn a > b\n\t\t\t? a : b;\n\t}\n}"
		);

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("PreferMathMethodCheck"));
		assertTrue(
				output.result().skippedReasons().get("PreferMathMethodCheck")
						.contains(SkipMessages.MATH_METHOD_SKIP)
		);
	}

	@Test
	public void testPreferSpecificApiArraysAsList() throws Exception {
		final var file = tempDir.resolve("AsList.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Arrays;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn Arrays.asList(\"a\", \"b\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Arrays;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn List.of(\"a\", \"b\");\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiAssertJunit4() throws Exception {
		final var file = tempDir.resolve("AssertJ4.java").toFile();
		Files.writeString(file.toPath(), "import static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertEquals(true, 1 == 1);\n\t\tassertEquals(new Object(), null);\n\t\tassertEquals(\"msg\", null, new Object());\n\t\tassertNotEquals(\"msg\", false, 1 == 1);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nimport static org.junit.jupiter.api.Assertions.assertNull;\nimport static org.junit.jupiter.api.Assertions.assertTrue;\nclass T {\n\tvoid run() {\n\t\tassertTrue(1 == 1);\n\t\tassertNull(new Object());\n\t\tassertNull(\"msg\", new Object());\n\t\tassertTrue(\"msg\", 1 == 1);\n\t}\n}",
				output.content()
		);
		assertEquals(4, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiAssertJunit5() throws Exception {
		final var file = tempDir.resolve("AssertJ5.java").toFile();
		Files.writeString(file.toPath(), "import static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertEquals(true, 1 == 1);\n\t\tassertEquals(new Object(), null);\n\t\tassertEquals(null, new Object(), \"msg\");\n\t\tassertNotEquals(false, 1 == 1, \"msg\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static org.junit.jupiter.api.Assertions.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nimport static org.junit.jupiter.api.Assertions.assertNull;\nimport static org.junit.jupiter.api.Assertions.assertTrue;\nclass T {\n\tvoid run() {\n\t\tassertTrue(1 == 1);\n\t\tassertNull(new Object());\n\t\tassertNull(new Object(), \"msg\");\n\t\tassertTrue(1 == 1, \"msg\");\n\t}\n}",
				output.content()
		);
		assertEquals(4, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactory() throws Exception {
		final var file = tempDir.resolve("CollFactory.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn Collections.singletonList(\"a\");\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn List.of(\"a\");\n\t}\n}", String.join("\n", lines));
		assertEquals(1, result.fixCount());
		assertFalse(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryAddsImport() throws Exception {
		final var file = tempDir.resolve("CollFactoryImp.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nclass T {\n\tObject run() {\n\t\treturn Collections.emptyList();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nclass T {\n\tObject run() {\n\t\treturn List.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryImportAlreadyPresent() throws Exception {
		final var file = tempDir.resolve("CollFactoryPresent.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn Collections.emptyList();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn List.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(1, result.fixCount());
		assertFalse(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryImportBetweenGroups() throws Exception {
		final var file = tempDir.resolve("CollFactoryGroups.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\n\nimport javax.annotation.Nonnull;\nclass T {\n\t@Nonnull\n\tObject run() {\n\t\treturn Collections.emptyList();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\n\nimport javax.annotation.Nonnull;\nclass T {\n\t@Nonnull\n\tObject run() {\n\t\treturn List.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(1, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryMultipleImports() throws Exception {
		final var file = tempDir.resolve("CollFactoryMulti.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nclass T {\n\tObject a() {\n\t\treturn Collections.emptyList();\n\t}\n\tObject b() {\n\t\treturn Collections.emptyMap();\n\t}\n\tObject c() {\n\t\treturn Collections.emptySet();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nimport java.util.Map;\nimport java.util.Set;\nclass T {\n\tObject a() {\n\t\treturn List.of();\n\t}\n\tObject b() {\n\t\treturn Map.of();\n\t}\n\tObject c() {\n\t\treturn Set.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(3, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryMultipleImportsWithGroupSeparator() throws Exception {
		final var file = tempDir.resolve("CollFactoryMultiGrp.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\n\nimport javax.annotation.Nonnull;\nclass T {\n\t@Nonnull\n\tObject a() {\n\t\treturn Collections.emptyList();\n\t}\n\t@Nonnull\n\tObject b() {\n\t\treturn Collections.emptySet();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nimport java.util.Set;\n\nimport javax.annotation.Nonnull;\nclass T {\n\t@Nonnull\n\tObject a() {\n\t\treturn List.of();\n\t}\n\t@Nonnull\n\tObject b() {\n\t\treturn Set.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(2, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryNeedsSecondPass() throws Exception {
		final var file = tempDir.resolve("CollFactoryFlag.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nclass T {\n\tObject run() {\n\t\treturn Collections.emptyList();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(1, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryPartialImport() throws Exception {
		final var file = tempDir.resolve("CollFactoryPartial.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> a() {\n\t\treturn Collections.emptyList();\n\t}\n\tObject b() {\n\t\treturn Collections.emptySet();\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals("import java.util.Collections;\nimport java.util.List;\nimport java.util.Set;\nclass T {\n\tList<String> a() {\n\t\treturn List.of();\n\t}\n\tObject b() {\n\t\treturn Set.of();\n\t}\n}", String.join("\n", lines));
		assertEquals(2, result.fixCount());
		assertTrue(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryRemovesUnusedImport() throws Exception {
		final var file = tempDir.resolve("CollFactoryUnused.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nclass T {\n\tObject run() {\n\t\treturn Collections.emptyList();\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tObject run() {\n\t\treturn List.of();\n\t}\n}",
				runFixMultiPass(file)
		);
	}

	@Test
	public void testPreferSpecificApiCollectionsFactoryRetainsUsedImport() throws Exception {
		final var file = tempDir.resolve("CollFactoryUsed.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> a() {\n\t\treturn Collections.singletonList(\"a\");\n\t}\n\tvoid b(List<String> list) {\n\t\tCollections.sort(list);\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tList<String> a() {\n\t\treturn List.of(\"a\");\n\t}\n\tvoid b(List<String> list) {\n\t\tlist.sort(null);\n\t}\n}",
				runFixMultiPass(file)
		);
	}

	@Test
	public void testPreferSpecificApiCollectionsSortNoComparator() throws Exception {
		final var file = tempDir.resolve("CollSort.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tCollections.sort(list);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Collections;\nimport java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.sort(null);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiCollectionsSortWithComparator() throws Exception {
		final var file = tempDir.resolve("CollSortCmp.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.Comparator;\nimport java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tCollections.sort(list, Comparator.naturalOrder());\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Collections;\nimport java.util.Comparator;\nimport java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.sort(Comparator.naturalOrder());\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiEqualsEmpty() throws Exception {
		final var file = tempDir.resolve("EqEmpty.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid run(String s) {\n\t\tif (s.equals(\"\"))\n\t\t\treturn;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid run(String s) {\n\t\tif (s.isEmpty())\n\t\t\treturn;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiGetFirst() throws Exception {
		final var file = tempDir.resolve("GetFirst.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tString run(List<String> list) {\n\t\treturn list.get(0);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tString run(List<String> list) {\n\t\treturn list.getFirst();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiLengthIsEmpty() throws Exception {
		final var file = tempDir.resolve("LenEmpty.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tboolean run(String s) {\n\t\treturn s.length() == 0;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tboolean run(String s) {\n\t\treturn s.isEmpty();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiLengthIsEmptyNegated() throws Exception {
		final var file = tempDir.resolve("LenNotEmpty.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid run(String s) {\n\t\tif (s.length() > 0)\n\t\t\treturn;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid run(String s) {\n\t\tif (!s.isEmpty())\n\t\t\treturn;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiMapChain() throws Exception {
		final var file = tempDir.resolve("MapChain.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid run(Map<String, String> map) {\n\t\tif (map.keySet().contains(\"k\"))\n\t\t\treturn;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid run(Map<String, String> map) {\n\t\tif (map.containsKey(\"k\"))\n\t\t\treturn;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiNoSecondPassForNonImportFix() throws Exception {
		final var file = tempDir.resolve("NoImpFix.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString run(String s) {\n\t\treturn s.replaceAll(\"foo\", \"bar\");\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		assertEquals(1, result.fixCount());
		assertFalse(result.needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiRemoveFirst() throws Exception {
		final var file = tempDir.resolve("RemFirst.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.remove(0);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.removeFirst();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiReplaceAll() throws Exception {
		final var file = tempDir.resolve("ReplAll.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString run(String s) {\n\t\treturn s.replaceAll(\"foo\", \"bar\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tString run(String s) {\n\t\treturn s.replace(\"foo\", \"bar\");\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiSizeIsEmpty() throws Exception {
		final var file = tempDir.resolve("SizeEmpty.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tboolean run(List<String> list) {\n\t\treturn list.size() == 0;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tboolean run(List<String> list) {\n\t\treturn list.isEmpty();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiSizeIsEmptyReversed() throws Exception {
		final var file = tempDir.resolve("SizeEmptyRev.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tif (0 < list.size())\n\t\t\treturn;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tif (!list.isEmpty())\n\t\t\treturn;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiStreamCount() throws Exception {
		final var file = tempDir.resolve("StreamCnt.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tlong run(List<String> list) {\n\t\treturn list.stream().count();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tlong run(List<String> list) {\n\t\treturn list.size();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiStreamFindFirstIsPresent() throws Exception {
		final var file = tempDir.resolve("StreamFFIP.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tboolean run(List<String> list) {\n\t\treturn list.stream().findFirst().isPresent();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tboolean run(List<String> list) {\n\t\treturn !list.isEmpty();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiStreamForEach() throws Exception {
		final var file = tempDir.resolve("StreamFE.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.stream().forEach(System.out::println);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.forEach(System.out::println);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiStringFormat() throws Exception {
		final var file = tempDir.resolve("StrFmt.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString run(String name) {\n\t\treturn String.format(\"Hello %s\", name);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tString run(String name) {\n\t\treturn \"Hello %s\".formatted(name);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiStringFormatSingleArg() throws Exception {
		final var file = tempDir.resolve("StrFmt1.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString run() {\n\t\treturn String.format(\"literal\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tString run() {\n\t\treturn \"literal\";\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiToArrayNewZero() throws Exception {
		final var file = tempDir.resolve("ToArr.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tString[] run(List<String> list) {\n\t\treturn list.toArray(new String[0]);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tString[] run(List<String> list) {\n\t\treturn list.toArray(String[]::new);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiTrimIsBlank() throws Exception {
		final var file = tempDir.resolve("TrimBlank.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tboolean run(String s) {\n\t\treturn s.trim().isEmpty();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tboolean run(String s) {\n\t\treturn s.isBlank();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferSpecificApiTrimIsBlankNegated() throws Exception {
		final var file = tempDir.resolve("TrimNotBlank.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tboolean run(String s) {\n\t\treturn s.trim().length() != 0;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tboolean run(String s) {\n\t\treturn !s.isBlank();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStandardCharsets() throws Exception {
		final var file = tempDir.resolve("StdCharset.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn s.getBytes(\"UTF-8\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.nio.charset.StandardCharsets;\nclass T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn s.getBytes(StandardCharsets.UTF_8);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStandardCharsetsAddsRegularAfterExistingStatic() throws Exception {
		final var file = tempDir.resolve("StdCharsetStatic.java").toFile();
		Files.writeString(
				file.toPath(),
				"import static java.util.Objects.requireNonNull;\n\nclass T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn requireNonNull(s).getBytes(\"UTF-8\");\n\t}\n}"
		);

		// fixer adds java.nio.charset.StandardCharsets (regular) to a file with
		// only static imports; the regular should go after the static group
		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static java.util.Objects.requireNonNull;\n\nimport java.nio.charset.StandardCharsets;\nclass T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn requireNonNull(s).getBytes(StandardCharsets.UTF_8);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStandardCharsetsImportAlreadyPresent() throws Exception {
		final var file = tempDir.resolve("StdCharsetImp.java").toFile();
		Files.writeString(file.toPath(), "import java.nio.charset.StandardCharsets;\n\nclass T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn s.getBytes(\"UTF-8\");\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.nio.charset.StandardCharsets;\n\nclass T {\n\tbyte[] run(String s) throws Exception {\n\t\treturn s.getBytes(StandardCharsets.UTF_8);\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStaticImportChainedCalls() throws Exception {
		// 4 violations on a single line (`Predicate.not(Objects.requireNonNull(...))` x2);
		// fixer must strip both `Predicate.` and `Objects.` correctly via column-descending order.
		final var file = tempDir.resolve("StaticImpChained.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nimport java.util.Objects;\nimport java.util.function.Predicate;\n\nclass T {\n\tList<String> f(List<String> list, String p, String s) {\n\t\treturn list.stream().filter(Predicate.not(Objects.requireNonNull(p)::startsWith)).filter(Predicate.not(Objects.requireNonNull(s)::endsWith)).toList();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static java.util.Objects.requireNonNull;\nimport static java.util.function.Predicate.not;\n\nimport java.util.List;\nimport java.util.Objects;\nimport java.util.function.Predicate;\n\nclass T {\n\tList<String> f(List<String> list, String p, String s) {\n\t\treturn list.stream().filter(not(requireNonNull(p)::startsWith)).filter(not(requireNonNull(s)::endsWith)).toList();\n\t}\n}",
				output.content()
		);
		assertEquals(4, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStaticImportCollectorsToSet() throws Exception {
		final var file = tempDir.resolve("StaticImpCollectors.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Set;\nimport java.util.stream.Collectors;\nimport java.util.stream.Stream;\n\nclass T {\n\tSet<String> a(Stream<String> s) {\n\t\treturn s.collect(Collectors.toSet());\n\t}\n\tSet<String> b(Stream<String> s) {\n\t\treturn s.collect(Collectors.toSet());\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static java.util.stream.Collectors.toSet;\n\nimport java.util.Set;\nimport java.util.stream.Collectors;\nimport java.util.stream.Stream;\n\nclass T {\n\tSet<String> a(Stream<String> s) {\n\t\treturn s.collect(toSet());\n\t}\n\tSet<String> b(Stream<String> s) {\n\t\treturn s.collect(toSet());\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStaticImportObjectsRequireNonNull() throws Exception {
		final var file = tempDir.resolve("StaticImpObjects.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Objects;\n\nclass T {\n\tObject f(Object a, Object b) {\n\t\tfinal var x = Objects.requireNonNull(a);\n\t\tfinal var y = Objects.requireNonNull(b);\n\t\treturn x.toString() + y.toString();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static java.util.Objects.requireNonNull;\n\nimport java.util.Objects;\n\nclass T {\n\tObject f(Object a, Object b) {\n\t\tfinal var x = requireNonNull(a);\n\t\tfinal var y = requireNonNull(b);\n\t\treturn x.toString() + y.toString();\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferStaticImportObjectsRequireNonNullRemovesUnusedImport() throws Exception {
		final var file = tempDir.resolve("StaticImpObjUnused.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Objects;\n\nclass T {\n\tObject f(Object a, Object b) {\n\t\tfinal var x = Objects.requireNonNull(a);\n\t\tfinal var y = Objects.requireNonNull(b);\n\t\treturn x.toString() + y.toString();\n\t}\n}");

		assertEquals(
				"import static java.util.Objects.requireNonNull;\n\nclass T {\n\tObject f(Object a, Object b) {\n\t\tfinal var x = requireNonNull(a);\n\t\tfinal var y = requireNonNull(b);\n\t\treturn x.toString() + y.toString();\n\t}\n}",
				runFixMultiPass(file)
		);
	}

	@Test
	public void testPreferStaticImportPredicateNot() throws Exception {
		final var file = tempDir.resolve("StaticImpPredicate.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nimport java.util.function.Predicate;\n\nclass T {\n\tList<String> f(List<String> list) {\n\t\treturn list.stream().filter(Predicate.not(String::isEmpty)).filter(Predicate.not(String::isBlank)).toList();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import static java.util.function.Predicate.not;\n\nimport java.util.List;\nimport java.util.function.Predicate;\n\nclass T {\n\tList<String> f(List<String> list) {\n\t\treturn list.stream().filter(not(String::isEmpty)).filter(not(String::isBlank)).toList();\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertTrue(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamond() throws Exception {
		final var file = tempDir.resolve("VarDiam.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<Object>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondAnonymousClass() throws Exception {
		final var file = tempDir.resolve("VarDiamAnon.java").toFile();
		Files.writeString(file.toPath(), "import java.util.Comparator;\nclass T {\n\tvoid f() {\n\t\tfinal var cmp = new Comparator<Object>() {\n\t\t\t@Override\n\t\t\tpublic int compare(Object a, Object b) {\n\t\t\t\treturn 0;\n\t\t\t}\n\t\t};\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.Comparator;\nclass T {\n\tvoid f() {\n\t\tfinal var cmp = new Comparator<>() {\n\t\t\t@Override\n\t\t\tpublic int compare(Object a, Object b) {\n\t\t\t\treturn 0;\n\t\t\t}\n\t\t};\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondConstructorArgs() throws Exception {
		final var file = tempDir.resolve("VarDiamCtor.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<Object>(16);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<>(16);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondFqConstructorName() throws Exception {
		final var file = tempDir.resolve("VarDiamFqCtor.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal var l = new java.util.ArrayList<Object>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var l = new java.util.ArrayList<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondFqn() throws Exception {
		final var file = tempDir.resolve("VarDiamFqn.java").toFile();
		Files.writeString(file.toPath(), "import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<java.lang.Object>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.ArrayList;\nclass T {\n\tvoid f() {\n\t\tfinal var l = new ArrayList<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondMixedQualifiedAndBare() throws Exception {
		final var file = tempDir.resolve("VarDiamMix.java").toFile();
		Files.writeString(file.toPath(), "import java.util.HashMap;\nclass T {\n\tvoid f() {\n\t\tfinal var m = new HashMap<Object, java.lang.Object>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.HashMap;\nclass T {\n\tvoid f() {\n\t\tfinal var m = new HashMap<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarDiamondMultipleArgs() throws Exception {
		final var file = tempDir.resolve("VarDiamMulti.java").toFile();
		Files.writeString(file.toPath(), "import java.util.HashMap;\nclass T {\n\tvoid f() {\n\t\tfinal var m = new HashMap<Object, Object>();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.HashMap;\nclass T {\n\tvoid f() {\n\t\tfinal var m = new HashMap<>();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarExplicitArrayInit() throws Exception {
		final var file = tempDir.resolve("VarArr.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal var a = new String[]{\"a\"};\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal String[] a = {\"a\"};\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarExplicitArrayInitMethodCallArg() throws Exception {
		final var file = tempDir.resolve("VarArrMeth.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tString result = String.join(\",\", new String[]{\"a\", \"b\"});\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var result = String.join(\",\", new String[]{\"a\", \"b\"});\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarExplicitArrayInitTyped() throws Exception {
		final var file = tempDir.resolve("VarArrTyped.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal String[] a = new String[]{\"a\"};\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal String[] a = {\"a\"};\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarFinalLocalInteraction() throws Exception {
		final var file = tempDir.resolve("VarFinal.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x = 5;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t}\n}",
				output.content()
		);
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarForEach() throws Exception {
		final var file = tempDir.resolve("VarFE.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfor (String item : List.of(\"a\"))\n\t\t\tSystem.out.println(item);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfor (var item : List.of(\"a\"))\n\t\t\tSystem.out.println(item);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarForInit() throws Exception {
		final var file = tempDir.resolve("VarFor.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfor (int i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfor (var i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarGenericType() throws Exception {
		final var file = tempDir.resolve("VarGen.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfinal List<String> l = List.of();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfinal var l = List.of();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarLocalString() throws Exception {
		final var file = tempDir.resolve("VarStr.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal String s = \"hi\";\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var s = \"hi\";\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarLocalWithFinal() throws Exception {
		final var file = tempDir.resolve("VarInt.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal int x = 5;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarTabIndented() throws Exception {
		final var file = tempDir.resolve("VarTab.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\t\tfinal int x = 5;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f() {\n\t\t\tfinal var x = 5;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarTryWithResources() throws Exception {
		final var file = tempDir.resolve("VarTry.java").toFile();
		Files.writeString(file.toPath(), "import java.io.ByteArrayInputStream;\nclass T {\n\tvoid f() throws Exception {\n\t\ttry (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) {\n\t\t\tin.read();\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.io.ByteArrayInputStream;\nclass T {\n\tvoid f() throws Exception {\n\t\ttry (var in = new ByteArrayInputStream(new byte[0])) {\n\t\t\tin.read();\n\t\t}\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testPreferVarWarningNotFixed() throws Exception {
		// float f = a + b with int params: var would infer int, so it's a WARNING
		final var file = tempDir.resolve("VarWarn.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int a, int b) {\n\t\tfinal float x = a + b;\n\t}\n}");

		// WARNING should not be fixed — line stays unchanged
		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tvoid f(int a, int b) {\n\t\tfinal float x = a + b;\n\t}\n}",
				output.content()
		);
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
		assertTrue(output.result().skippedReasons().containsKey("PreferVarCheck"));
		assertTrue(
				output.result().skippedReasons().get("PreferVarCheck").contains(SkipMessages.FIX_SEVERITY)
		);
	}

	@Test
	public void testRedundantImport() throws Exception {
		final var file = tempDir.resolve("Imp.java").toFile();
		Files.writeString(file.toPath(), "import java.lang.String;\n\nclass T {\n\tString s;\n}");

		// both RedundantImport and UnusedImports fire on java.lang.String, so
		// the import line is deleted twice — the second delete removes the blank line
		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tString s;\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantImportContiguousSuppressesDuplicate() throws Exception {
		final var file = tempDir.resolve("RedImpContig.java").toFile();
		Files.writeString(file.toPath(), "import java.lang.String;\nimport java.util.List;\n\nclass T {\n\tList<String> s;\n}");

		// both RedundantImport and UnusedImports fire on java.lang.String;
		// the second same-line violation is suppressed so import java.util.List is preserved
		final var output = runFixAndGetResult(file);
		assertEquals("import java.util.List;\n\nclass T {\n\tList<String> s;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
		assertFalse(output.result().skippedReasons().isEmpty());
		assertTrue(
				output.result().skippedReasons().values().stream()
						.anyMatch(reasons -> reasons.contains(SkipMessages.FIX_SUPPRESSED))
		);
	}

	@Test
	public void testRedundantImportOrphanedSuppressesDuplicate() throws Exception {
		final var file = tempDir.resolve("RedImpOrphan.java").toFile();
		Files.writeString(
				file.toPath(),
				"import java.io.File;\n\nimport java.lang.String;\n\nimport javax.annotation.Nonnull;\n\nclass T {\n\t@Nonnull\n\tFile f;\n\tString s;\n}"
		);

		// both RedundantImport and UnusedImports fire on java.lang.String;
		// first delete collapses the blank below, second is suppressed
		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.io.File;\n\nimport javax.annotation.Nonnull;\n\nclass T {\n\t@Nonnull\n\tFile f;\n\tString s;\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantModifier() throws Exception {
		final var file = tempDir.resolve("Iface.java").toFile();
		Files.writeString(file.toPath(), "interface T {\n\tpublic void method();\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("interface T {\n\tvoid method();\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantModifierPrivateEnumConstructor() throws Exception {
		final var file = tempDir.resolve("EnumCtor.java").toFile();
		Files.writeString(file.toPath(), "enum Color {\n\tRED(1);\n\n\tprivate Color(int code) {\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("enum Color {\n\tRED(1);\n\n\tColor(int code) {\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantModifierStaticInterfaceField() throws Exception {
		final var file = tempDir.resolve("IfaceField.java").toFile();
		Files.writeString(file.toPath(), "interface T {\n\tstatic int VALUE = 5;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("interface T {\n\tint VALUE = 5;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantNumericSuffix() throws Exception {
		final var file = tempDir.resolve("Suffix.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tlong x = 100L;\n\tdouble d = 1.0d;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tlong x = 100;\n\tdouble d = 1.0;\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testRedundantNumericSuffixHexAndBinaryAndFloat() throws Exception {
		final var file = tempDir.resolve("SuffixHex.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tlong a = 0xFFL;\n\tfloat b = 100F;\n\tlong c = 0b1010L;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tlong a = 0xFF;\n\tfloat b = 100;\n\tlong c = 0b1010;\n}", output.content());
		assertEquals(3, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testSuperCall() throws Exception {
		final var file = tempDir.resolve("Child.java").toFile();
		Files.writeString(file.toPath(), "class Child extends Object {\n\tChild() {\n\t\tsuper();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class Child extends Object {\n\tChild() {\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testSuperCallTabIndented() throws Exception {
		final var file = tempDir.resolve("ChildTab.java").toFile();
		Files.writeString(file.toPath(), "class Outer {\n\tclass Inner extends Object {\n\t\tInner() {\n\t\t\tsuper();\n\t\t}\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class Outer {\n\tclass Inner extends Object {\n\t\tInner() {\n\t\t}\n\t}\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testTabColumnConversion() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("hello", 0));
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 5));
		assertEquals(1, CheckstyleFixAction.tabColumnToCharIndex("\thello", 8));
		assertEquals(2, CheckstyleFixAction.tabColumnToCharIndex("\t\thello", 16));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("\thello world", 13));
	}

	@Test
	public void testTabColumnConversionBeyondLine() {
		assertEquals(5, CheckstyleFixAction.tabColumnToCharIndex("hello", 10));
	}

	@Test
	public void testTabColumnConversionMidLineTab() {
		// "ab\tcd" - a=0, b=1, \t=2 (expands from col 2 to col 8), c=3 at col 8, d=4 at col 9
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 8));
		assertEquals(4, CheckstyleFixAction.tabColumnToCharIndex("ab\tcd", 9));
	}

	@Test
	public void testTabColumnConversionNoTabs() {
		assertEquals(0, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 0));
		assertEquals(3, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 3));
		assertEquals(6, CheckstyleFixAction.tabColumnToCharIndex("abcdef", 6));
	}

	@Test
	public void testTrailingNewline() throws Exception {
		final var file = tempDir.resolve("TrailNl.java").toFile();
		Files.writeString(file.toPath(), "class T {}\n");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {}", output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testTrailingNewlineDouble() throws Exception {
		final var file = tempDir.resolve("TrailNl2.java").toFile();
		Files.writeString(file.toPath(), "class T {}\n\n");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testTrailingWhitespace() throws Exception {
		final var file = tempDir.resolve("Trail.java").toFile();
		Files.writeString(file.toPath(), "class T {   \n\tint x;\t\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n}", output.content());
		assertEquals(2, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testTrailingWhitespaceTabOnly() throws Exception {
		final var file = tempDir.resolve("TrailTab.java").toFile();
		Files.writeString(file.toPath(), "class T {\t\t\n\tint x;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tint x;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUnnecessaryThis() throws Exception {
		final var file = tempDir.resolve("This.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint value;\n\tint get() {\n\t\treturn this.value;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tint value;\n\tint get() {\n\t\treturn value;\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUnnecessaryThisChained() throws Exception {
		final var file = tempDir.resolve("ThisChain.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tString value;\n\tint get() {\n\t\treturn this.value.length();\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(
				"class T {\n\tString value;\n\tint get() {\n\t\treturn value.length();\n\t}\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUnusedImport() throws Exception {
		final var file = tempDir.resolve("Unused.java").toFile();
		Files.writeString(file.toPath(), "import java.util.List;\n\nclass T {\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("\nclass T {\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUnusedImportOrphanedInGroupRemovesBlankLine() throws Exception {
		final var file = tempDir.resolve("UnusedOrphan.java").toFile();
		Files.writeString(file.toPath(), "package p;\n\nimport java.util.List;\n\nclass T {\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("package p;\n\nclass T {\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUnusedImportOrphanedMiddleGroupRemovesBlankLine() throws Exception {
		final var file = tempDir.resolve("UnusedMiddle.java").toFile();
		Files.writeString(
				file.toPath(),
				"import java.io.File;\n\nimport java.util.List;\n\nimport javax.annotation.Nonnull;\n\nclass T {\n\t@Nonnull\n\tFile f;\n}"
		);

		final var output = runFixAndGetResult(file);
		assertEquals(
				"import java.io.File;\n\nimport javax.annotation.Nonnull;\n\nclass T {\n\t@Nonnull\n\tFile f;\n}",
				output.content()
		);
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUpperEll() throws Exception {
		// use a value that doesn't fit in int, so RedundantNumericSuffix doesn't also fire
		final var file = tempDir.resolve("Ell.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tlong x = 3000000000l;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tlong x = 3000000000L;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testUpperEllHex() throws Exception {
		// hex value exceeding int range to avoid RedundantNumericSuffix interference
		final var file = tempDir.resolve("EllHex.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tlong x = 0xB00000000l;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tlong x = 0xB00000000L;\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}
}