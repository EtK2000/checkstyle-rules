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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class CheckstyleFixNoFixTest {
	record FixOutput(@Nonnull String content, @Nonnull ApplyFixesResult result) {
	}

	@TempDir
	Path tempDir;

	@Test
	public void collectJavaFilesEmptyDir() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("empty"));
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertTrue(files.isEmpty());
	}

	@Test
	public void collectJavaFilesFiltersNonJava() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("src"));
		Files.writeString(dir.resolve("A.java"), "class A {}");
		Files.writeString(dir.resolve("B.txt"), "not java");
		Files.writeString(dir.resolve("C.java"), "class C {}");
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertEquals(2, files.size());
		assertTrue(files.stream().allMatch(f -> f.getName().endsWith(".java")));
	}

	@Test
	public void collectJavaFilesNonExistentDir() throws Exception {
		final var dir = tempDir.resolve("does-not-exist");
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertTrue(files.isEmpty());
	}

	@Test
	public void collectJavaFilesRecursive() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("src"));
		final var sub = Files.createDirectory(dir.resolve("sub"));
		Files.writeString(dir.resolve("A.java"), "class A {}");
		Files.writeString(sub.resolve("B.java"), "class B {}");
		final var files = new ArrayList<File>();
		CheckstyleFixAction.collectJavaFiles(dir, files);
		assertEquals(2, files.size());
	}

	@Test
	public void doExecuteDryRunDoesNotModifyFile() throws Exception {
		final var file = tempDir.resolve("DryExec.java").toFile();
		final var original = "class T {\n\tint[] a = {1, 2,};\n}";
		Files.writeString(file.toPath(), original);

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		assertEquals(original, Files.readString(file.toPath()));
	}

	@Test
	public void doExecuteDryRunFixableButAllSkipped() throws Exception {
		final var file = tempDir.resolve("DrySkip.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(boolean x) {\n\t\tif (x) return;\n\t}\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
	}

	@Test
	public void doExecuteDryRunFixableCountMatchesViolations() throws Exception {
		final var file = tempDir.resolve("DryAnnot.java").toFile();
		Files.writeString(
				file.toPath(),
				"class T {\n\t@SuppressWarnings(\"unused\")\n\tString c;\n\t@Deprecated\n\tString b;\n\tint x = 0;\n}"
		);

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(2, result[1]);
	}

	@Test
	public void doExecuteDryRunReturnsCorrectCount() throws Exception {
		final var file = tempDir.resolve("DryCount.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint b, a;\n\tint[] c = {1, 2,};\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(2, result[1]);
	}

	@Test
	public void doExecuteDryRunSecondPassFlag() throws Exception {
		final var file = tempDir.resolve("DryPass.java").toFile();
		Files.writeString(file.toPath(), "import java.nio.charset.Charset;\nclass T {\n\tCharset c = Charset.forName(\"UTF-8\");\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(1, result[0]);
		assertEquals(1, result[1]);
	}

	@Test
	public void doExecuteDryRunSuppressesSummaryOutput() throws Exception {
		final var file = tempDir.resolve("DrySilent.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1, 2,};\n}");

		final var origOut = System.out;
		final var captured = new ByteArrayOutputStream();
		System.setOut(new PrintStream(captured));
		try {
			final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
			CheckstyleFixAction.doExecute(config, true, List.of(file));
		}
		finally {
			System.setOut(origOut);
		}
		assertFalse(captured.toString().contains("Fixed"));
	}

	@Test
	public void doExecuteZeroViolationsReturnsZeros() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid m() {}\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));
		assertEquals(0, result[0]);
		assertEquals(0, result[1]);
	}

	@Test
	public void e2eMixedFixableAndUnfixableViolations() throws Exception {
		final var file = tempDir.resolve("E2EMixed.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tint[] c = {1, 2,};\n\tvoid f() {\n\t\tint a, b;\n\t}\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(file));

		assertEquals(2, result[1]);
	}

	@Test
	public void e2eMultipleFilesAggregatesCount() throws Exception {
		final var dir = Files.createDirectory(tempDir.resolve("multi"));
		final var f1 = dir.resolve("A.java").toFile();
		final var f2 = dir.resolve("B.java").toFile();
		Files.writeString(f1.toPath(), "class A {\n\tint x = 0;\n}");
		Files.writeString(f2.toPath(), "class B {\n\tint[] a = {1,};\n}");

		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE));
		final var result = CheckstyleFixAction.doExecute(config, true, List.of(f1, f2));
		assertEquals(2, result[1]);
	}

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file) throws Exception {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", "8");
		for (var checkName : CheckstyleFixAction.FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.endsWith("FinalLocalVariableCheck"))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
			if (checkName.endsWith("PreferMathMethodCheck")
					|| checkName.endsWith("PreferSpecificApiCheck")
					|| checkName.endsWith("PreferStandardCharsetsCheck")
					|| checkName.endsWith("PreferStaticImportCheck"))
				checkConfig.addProperty("minSdk", String.valueOf(Integer.MAX_VALUE));
			if (checkName.endsWith("PreferVarCheck"))
				checkConfig.addProperty("allowedMethods", CheckstyleFixAction.fixerAllowedMethods());
			treeWalkerConfig.addChild(checkConfig);
		}

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

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
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var result = CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
		return new FixOutput(String.join("\n", lines), result);
	}

	@Nonnull
	private ApplyFixesResult runFixPipeline(@Nonnull File file) throws Exception {
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		return CheckstyleFixAction.applyFixes(lines, violations, CheckstyleFixAction.FIXERS, CheckstyleFixAction.MODULE_ID_FIXERS);
	}

	@Test
	public void testAllSkippedHasReasons() throws Exception {
		final var file = tempDir.resolve("AllSkipped.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int x) {\n\t\tif (x > 0) --x;\n\t}\n}");

		final var output = runFixAndGetResult(file);
		assertEquals(0, output.result().fixCount());
		assertTrue(output.result().skippedReasons().containsKey("ControlFlowBracesCheck"));
		final var reasons = output.result().skippedReasons().get("ControlFlowBracesCheck");
		assertFalse(reasons.isEmpty());
		assertEquals(SkipMessages.CONTROL_FLOW_SKIP, reasons.getFirst());
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
	public void testArrayTypeStyleMultiVarWithInitializerSkipped() throws Exception {
		final var file = tempDir.resolve("ArrTypeStyleMultiVarInit.java").toFile();
		final var input = "class T {\n\tvoid m() {\n\t\tfinal int gamma[] = {1}, delta = 0;\n\t\tgamma[0] = delta;\n\t}\n}";
		Files.writeString(file.toPath(), input);

		final var output = runFixAndGetResult(file);
		assertEquals(input, output.content());
		assertEquals(0, output.result().fixCount());
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
	public void testFieldConsolidationBlockCommentBeforeFieldNameSkipped() throws Exception {
		final var file = tempDir.resolve("FieldConsBlockComment.java").toFile();
		final var content = "class T {\n\tint /* note */ alpha;\n\tint /* note */ beta;\n}";
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
	}

	@Test
	public void testFieldConsolidationBlockCommentPostNameSkipped() throws Exception {
		final var file = tempDir.resolve("FieldConsBlockCommentPost.java").toFile();
		final var content = "class T {\n\tint alpha;\n\tint beta /* doc */;\n}";
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
	}

	@Test
	public void testFieldConsolidationWrappingPreExistingMultiLineNotFlagged() throws Exception {
		final var file = tempDir.resolve("FieldConsPreWrap.java").toFile();
		final var content = "class T {\n"
				+ "\tprivate boolean areInvestmentFundsTreatedAsPensionLiquidity,\n"
				+ "\t\t\tarePensionsTreatedAsSeparateLiquidity,\n"
				+ "\t\t\tareUnvestedRsusExcludedFromSum,\n"
				+ "\t\t\tareUnvestedRsusTreatedAsSeparateLiquidity;\n"
				+ "}";
		Files.writeString(file.toPath(), content);

		final var output = runFixAndGetResult(file);
		assertEquals(content, output.content());
		assertEquals(0, output.result().fixCount());
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
	public void testFieldSortingFieldViolationNowFixed() throws Exception {
		final var file = tempDir.resolve("FieldOrder.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tstatic final String Z = \"z\";\n\tstatic final int A = 0;\n}");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {\n\tstatic final int A = 0;\n\tstatic final String Z = \"z\";\n}", output.content());
		assertEquals(1, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}

	@Test
	public void testNoViolations() throws Exception {
		final var file = tempDir.resolve("Clean.java").toFile();
		final var input = "class Clean {\n\tint x = 100;\n\tint[] a = {1, 2};\n}";
		Files.writeString(file.toPath(), input);

		final var result = runFixPipeline(file);
		assertEquals(0, result.fixCount());
		assertFalse(result.needsSecondPass());
	}

	@Test
	public void testPreferMathMethodSkipsMultilineTernary() throws Exception {
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
	public void testPreferVarWarningNotFixed() throws Exception {
		final var file = tempDir.resolve("VarWarn.java").toFile();
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int a, int b) {\n\t\tfinal float x = a + b;\n\t}\n}");

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
	public void testTrailingNewline() throws Exception {
		final var file = tempDir.resolve("TrailNl.java").toFile();
		Files.writeString(file.toPath(), "class T {}\n");

		final var output = runFixAndGetResult(file);
		assertEquals("class T {}", output.content());
		assertEquals(0, output.result().fixCount());
		assertFalse(output.result().needsSecondPass());
	}
}