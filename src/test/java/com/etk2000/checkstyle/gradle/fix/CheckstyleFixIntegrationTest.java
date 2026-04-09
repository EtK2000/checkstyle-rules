package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class CheckstyleFixIntegrationTest {
	@Rule
	public final TemporaryFolder tempDir = new TemporaryFolder();

	@Nonnull
	private List<AuditEvent> runChecks(@Nonnull File file) throws Exception {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		for (var checkName : CheckstyleFixTask.FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.endsWith("FinalLocalVariableCheck"))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
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
		checker.setModuleClassLoader(getClass().getClassLoader());
		checker.configure(checkerConfig);

		final var violations = new ArrayList<AuditEvent>();
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
		checker.destroy();
		return violations;
	}

	@Nonnull
	private String runFixAndGetResult(@Nonnull File file) throws Exception {
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		CheckstyleFixTask.applyFixes(lines, violations, CheckstyleFixTask.FIXERS, CheckstyleFixTask.MODULE_ID_FIXERS);
		return String.join("\n", lines);
	}

	private int runFixPipeline(@Nonnull File file) throws Exception {
		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		return CheckstyleFixTask.applyFixes(lines, violations, CheckstyleFixTask.FIXERS, CheckstyleFixTask.MODULE_ID_FIXERS);
	}

	@Test
	public void testAnnotationOwnLineBlank() throws Exception {
		final var file = tempDir.newFile("AnnBlank.java");
		Files.writeString(file.toPath(), "class T {\n\t@Deprecated\n\n\tvoid method() {}\n}");

		assertEquals(
				"class T {\n\t@Deprecated\n\tvoid method() {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testAnnotationOwnLineReorder() throws Exception {
		final var file = tempDir.newFile("AnnReorder.java");
		Files.writeString(file.toPath(), "class T {\n\t@Override\n\t@Deprecated\n\tvoid method() {}\n}");

		assertEquals(
				"class T {\n\t@Deprecated\n\t@Override\n\tvoid method() {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testAnnotationOwnLineSplit() throws Exception {
		final var file = tempDir.newFile("AnnOwn.java");
		Files.writeString(file.toPath(), "class T {\n\t@Override @Deprecated void method() {}\n}");

		assertEquals(
				"class T {\n\t@Deprecated\n\t@Override\n\tvoid method() {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testAnnotationSameLineInlineReorder() throws Exception {
		final var file = tempDir.newFile("AnnReord.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid method(@Override @Deprecated String param) {}\n}");

		assertEquals(
				"class T {\n\tvoid method(@Deprecated @Override String param) {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testAnnotationSameLineJoin() throws Exception {
		final var file = tempDir.newFile("AnnSame.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid method(\n\t\t\t@Deprecated\n\t\t\tString param\n\t) {}\n}");

		assertEquals(
				"class T {\n\tvoid method(\n\t\t\t@Deprecated String param\n\t) {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testApplyFixesSkipsUnknownViolations() throws Exception {
		final var file = tempDir.newFile("Unknown.java");
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1, 2,};\n}");

		final var violations = runChecks(file);
		assertFalse(violations.isEmpty());

		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var fixed = CheckstyleFixTask.applyFixes(lines, violations, Map.of(), Map.of());
		assertEquals(0, fixed);
	}

	@Test
	public void testArrayTrailingComma() throws Exception {
		final var file = tempDir.newFile("Arr.java");
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1, 2,};\n}");

		assertEquals("class T {\n\tint[] a = {1, 2};\n}", runFixAndGetResult(file));
	}

	@Test
	public void testBlankLineAfterBreak() throws Exception {
		final var file = tempDir.newFile("Break.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\t\t\tcase 2:\n\t\t\t\tbreak;\n\t\t\tdefault:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\n\t\t\tcase 2:\n\t\t\t\tbreak;\n\n\t\t\tdefault:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testBlankLineAfterBreakFallThrough() throws Exception {
		final var file = tempDir.newFile("BreakFall.java");
		// fall-through cases (no break between case 1 and case 2) should be untouched
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\tcase 2:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\t\t\tcase 3:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\tcase 2:\n\t\t\t\tdoSomething();\n\t\t\t\tbreak;\n\n\t\t\tcase 3:\n\t\t\t\tbreak;\n\t\t}\n\t}\n\tvoid doSomething() {}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testBlankLineAfterClassBrace() throws Exception {
		final var file = tempDir.newFile("ClassBrace.java");
		Files.writeString(file.toPath(), "class T {\n\n\tint x;\n}");

		assertEquals("class T {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testBlankLineAfterClassBraceCombinedWithBeforeClose() throws Exception {
		final var file = tempDir.newFile("ClassBraceBoth.java");
		Files.writeString(file.toPath(), "class T {\n\n\tint x;\n\n}");

		assertEquals("class T {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testBlankLineAfterClassBraceMultiLine() throws Exception {
		final var file = tempDir.newFile("ClassBraceMulti.java");
		Files.writeString(file.toPath(), "class T\n\t\textends Base {\n\n\tint x;\n}");

		assertEquals("class T\n\t\textends Base {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testBlankLineBeforeClosingBrace() throws Exception {
		final var file = tempDir.newFile("CloseBrace.java");
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n}");

		assertEquals("class T {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testBlankLineBetweenSingleCases() throws Exception {
		final var file = tempDir.newFile("Switch.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testBlankLineBetweenSingleCasesMultipleBlankLines() throws Exception {
		final var file = tempDir.newFile("SwitchMulti.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\n\n\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tswitch (x) {\n\t\t\tcase 1:\n\t\t\t\treturn;\n\t\t\tcase 2:\n\t\t\t\treturn;\n\t\t}\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoubleBlankLines() throws Exception {
		final var file = tempDir.newFile("Dbl.java");
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n\n\tint y;\n}");

		assertEquals("class T {\n\tint x;\n\n\tint y;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testDoubleBlankLinesTriple() throws Exception {
		final var file = tempDir.newFile("Dbl3.java");
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n\n\n\tint y;\n}");

		assertEquals("class T {\n\tint x;\n\n\tint y;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testDoWhileBracedTier1() throws Exception {
		final var file = tempDir.newFile("DoTier1.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\t--x;\n\t\t} while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileBracedTier2() throws Exception {
		final var file = tempDir.newFile("DoTier2.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\tSystem.out.println(x);\n\t\t} while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileBracedTier3() throws Exception {
		final var file = tempDir.newFile("DoTier3.java");
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo {\n\t\t\tlist.subList(0, 1).clear();\n\t\t} while (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileBracedTier3ComplexRhs() throws Exception {
		final var file = tempDir.newFile("DoTier3Rhs.java");
		final var input = "class T {\n\tvoid f(int x, int y) {\n\t\tdo {\n\t\t\tx += 5 * y;\n\t\t} while (x < 100);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x, int y) {\n\t\tdo\n\t\t\tx += 5 * y;\n\t\twhile (x < 100);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileMissingBraces() throws Exception {
		final var file = tempDir.newFile("DoMissing.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\tif (x > 0)\n\t\t\t\t--x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo {\n\t\t\tif (x > 0)\n\t\t\t\t--x;\n\t\t} while (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileOwnLineTier1() throws Exception {
		final var file = tempDir.newFile("DoOwn1.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\t--x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileOwnLineTier2() throws Exception {
		final var file = tempDir.newFile("DoOwn2.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo\n\t\t\tSystem.out.println(x);\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileTier1SplitJoins() throws Exception {
		final var file = tempDir.newFile("DoT1Split.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo --x;\n\t\twhile (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo --x; while (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileTier2WhileOnSameLine() throws Exception {
		final var file = tempDir.newFile("DoSplit.java");
		final var input = "class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x); while (x > 0);\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(int x) {\n\t\tdo System.out.println(x);\n\t\twhile (x > 0);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileTier3AsTier2() throws Exception {
		final var file = tempDir.newFile("DoT3T2.java");
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo list.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testDoWhileTier3OneLiner() throws Exception {
		final var file = tempDir.newFile("DoT3One.java");
		final var input = "class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo list.subList(0, 1).clear(); while (!list.isEmpty());\n\t}\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(
				"class T {\n\tvoid f(java.util.List<String> list) {\n\t\tdo\n\t\t\tlist.subList(0, 1).clear();\n\t\twhile (!list.isEmpty());\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testEnumTrailingComma() throws Exception {
		final var file = tempDir.newFile("Color.java");
		Files.writeString(file.toPath(), "enum Color {\n\tRED,\n\tGREEN,\n}");

		assertEquals("enum Color {\n\tRED,\n\tGREEN\n}", runFixAndGetResult(file));
	}

	@Test
	public void testExplicitInitialization() throws Exception {
		final var file = tempDir.newFile("Init.java");
		Files.writeString(file.toPath(), "class T {\n\tint x = 0;\n\tObject o = null;\n\tboolean b = false;\n}");

		assertEquals("class T {\n\tint x;\n\tObject o;\n\tboolean b;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testExplicitInitializationMultiDeclaration() throws Exception {
		final var file = tempDir.newFile("InitMulti.java");
		Files.writeString(file.toPath(), "class T {\n\tint a = 0, b;\n}");

		assertEquals("class T {\n\tint a, b;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testFinalLocalVariable() throws Exception {
		final var file = tempDir.newFile("Final.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x = 5;\n\t\tvar y = \"hello\";\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t\tfinal var y = \"hello\";\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFinalLocalVariableTabIndented() throws Exception {
		final var file = tempDir.newFile("FinalTab.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tif (true) {\n\t\t\tint x = 5;\n\t\t}\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tif (true) {\n\t\t\tfinal var x = 5;\n\t\t}\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFixerReturnsNullForDuplicateOnSameLine() throws Exception {
		final var file = tempDir.newFile("Multi.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x, y;\n\t}\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var fixed = CheckstyleFixTask.applyFixes(lines, violations, CheckstyleFixTask.FIXERS, CheckstyleFixTask.MODULE_ID_FIXERS);

		assertEquals("class T {\n\tvoid f() {\n\t\tfinal int x, y;\n\t}\n}", String.join("\n", lines));
		assertEquals(1, fixed);
	}

	@Test
	public void testFixLambdaParamRemoveParens() throws Exception {
		final var file = tempDir.newFile("LamParen.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((x) -> System.out.println(x));\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach(x -> System.out.println(x));\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFixLambdaParamRemoveType() throws Exception {
		final var file = tempDir.newFile("LamType.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((String x) -> System.out.println(x));\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach(x -> System.out.println(x));\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFixLambdaParamReplaceTypeWithVar() throws Exception {
		final var file = tempDir.newFile("LamVar.java");
		Files.writeString(file.toPath(), "import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((@A String x) -> System.out.println(x));\n\t}\n}");

		assertEquals(
				"import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.forEach((@A var x) -> System.out.println(x));\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFixLambdaParamReplaceTypeWithVarMultiParam() throws Exception {
		final var file = tempDir.newFile("LamVarMulti.java");
		Files.writeString(file.toPath(), "import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.sort((@A String x, String y) -> x.compareTo(y));\n\t}\n}");

		assertEquals(
				"import java.util.List;\n@interface A {}\nclass T {\n\tvoid f(List<String> list) {\n\t\tlist.sort((@A var x, var y) -> x.compareTo(y));\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testFixOrderBottomToTop() throws Exception {
		final var file = tempDir.newFile("Order.java");
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1,};\n\tint[] b = {2,};\n\tint[] c = {3,};\n}");

		final var violations = runChecks(file);
		final var lines = new ArrayList<>(Files.readAllLines(file.toPath()));
		final var fixed = CheckstyleFixTask.applyFixes(lines, violations, CheckstyleFixTask.FIXERS);

		assertEquals("class T {\n\tint[] a = {1};\n\tint[] b = {2};\n\tint[] c = {3};\n}", String.join("\n", lines));
		assertEquals(3, fixed);
	}

	@Test
	public void testMultipleViolationsSameFile() throws Exception {
		final var file = tempDir.newFile("Multi2.java");
		Files.writeString(file.toPath(), "class T {\n\tint[] a = {1,};\n\tlong x = 100L;\n}");

		assertEquals("class T {\n\tint[] a = {1};\n\tlong x = 100;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testNoFinalParametersCatch() throws Exception {
		final var file = tempDir.newFile("CatchFinal.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\ttry {\n\t\t\tSystem.out.println();\n\t\t}\n\t\tcatch (final Exception e) {\n\t\t\tSystem.out.println(e);\n\t\t}\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\ttry {\n\t\t\tSystem.out.println();\n\t\t}\n\t\tcatch (Exception e) {\n\t\t\tSystem.out.println(e);\n\t\t}\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testNoFinalParametersConstructor() throws Exception {
		final var file = tempDir.newFile("CtorFinal.java");
		Files.writeString(file.toPath(), "class T {\n\tT(final int x) {}\n}");

		assertEquals("class T {\n\tT(int x) {}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testNoFinalParametersForEach() throws Exception {
		final var file = tempDir.newFile("ForEachFinal.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tfor (final var item : list)\n\t\t\tSystem.out.println(item);\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f(List<String> list) {\n\t\tfor (var item : list)\n\t\t\tSystem.out.println(item);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testNoFinalParametersMethod() throws Exception {
		final var file = tempDir.newFile("ParamFinal.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f(final int x, final String y) {}\n}");

		assertEquals("class T {\n\tvoid f(int x, String y) {}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testNoFinalParametersSecondParam() throws Exception {
		final var file = tempDir.newFile("SecondFinal.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int x, final String y) {}\n}");

		assertEquals("class T {\n\tvoid f(int x, String y) {}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testNoViolations() throws Exception {
		final var file = tempDir.newFile("Clean.java");
		final var input = "class Clean {\n\tint[] a = {1, 2};\n\tint x = 100;\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(0, runFixPipeline(file));
	}

	@Test
	public void testPostfixDecrement() throws Exception {
		final var file = tempDir.newFile("Decr.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tint i = 5;\n\t\ti--;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid run() {\n\t\tvar i = 5;\n\t\t--i;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPostfixIncrement() throws Exception {
		final var file = tempDir.newFile("Incr.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tint i = 0;\n\t\ti++;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid run() {\n\t\tvar i = 0;\n\t\t++i;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPostfixIncrementForLoop() throws Exception {
		final var file = tempDir.newFile("IncrFor.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid run() {\n\t\tfor (var i = 0; i < 10; i++)\n\t\t\tSystem.out.println(i);\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid run() {\n\t\tfor (var i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiAssertJunit4() throws Exception {
		final var file = tempDir.newFile("AssertJ4.java");
		Files.writeString(file.toPath(), "import static org.junit.Assert.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertEquals(true, 1 == 1);\n\t\tassertEquals(new Object(), null);\n\t\tassertEquals(\"msg\", null, new Object());\n\t\tassertNotEquals(\"msg\", false, 1 == 1);\n\t}\n}");

		assertEquals(
				"import static org.junit.Assert.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertTrue(1 == 1);\n\t\tassertNull(new Object());\n\t\tassertNull(\"msg\", new Object());\n\t\tassertTrue(\"msg\", 1 == 1);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiAssertJunit5() throws Exception {
		final var file = tempDir.newFile("AssertJ5.java");
		Files.writeString(file.toPath(), "import static org.junit.Assert.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertEquals(true, 1 == 1);\n\t\tassertEquals(new Object(), null);\n\t\tassertEquals(null, new Object(), \"msg\");\n\t\tassertNotEquals(false, 1 == 1, \"msg\");\n\t}\n}");

		assertEquals(
				"import static org.junit.Assert.assertEquals;\nimport static org.junit.Assert.assertNotEquals;\nclass T {\n\tvoid run() {\n\t\tassertTrue(1 == 1);\n\t\tassertNull(new Object());\n\t\tassertNull(new Object(), \"msg\");\n\t\tassertTrue(1 == 1, \"msg\");\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiCollectionsFactory() throws Exception {
		final var file = tempDir.newFile("CollFactory.java");
		Files.writeString(file.toPath(), "import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn Collections.singletonList(\"a\");\n\t}\n}");

		assertEquals(
				"import java.util.Collections;\nimport java.util.List;\nclass T {\n\tList<String> run() {\n\t\treturn List.of(\"a\");\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiEqualsEmpty() throws Exception {
		final var file = tempDir.newFile("EqEmpty.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid run(String s) {\n\t\tif (s.equals(\"\"))\n\t\t\treturn;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid run(String s) {\n\t\tif (s.isEmpty())\n\t\t\treturn;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiMapChain() throws Exception {
		final var file = tempDir.newFile("MapChain.java");
		Files.writeString(file.toPath(), "import java.util.Map;\nclass T {\n\tvoid run(Map<String, String> map) {\n\t\tif (map.keySet().contains(\"k\"))\n\t\t\treturn;\n\t}\n}");

		assertEquals(
				"import java.util.Map;\nclass T {\n\tvoid run(Map<String, String> map) {\n\t\tif (map.containsKey(\"k\"))\n\t\t\treturn;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiReplaceAll() throws Exception {
		final var file = tempDir.newFile("ReplAll.java");
		Files.writeString(file.toPath(), "class T {\n\tString run(String s) {\n\t\treturn s.replaceAll(\"foo\", \"bar\");\n\t}\n}");

		assertEquals(
				"class T {\n\tString run(String s) {\n\t\treturn s.replace(\"foo\", \"bar\");\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiStreamCount() throws Exception {
		final var file = tempDir.newFile("StreamCnt.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tlong run(List<String> list) {\n\t\treturn list.stream().count();\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tlong run(List<String> list) {\n\t\treturn list.size();\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferSpecificApiStreamForEach() throws Exception {
		final var file = tempDir.newFile("StreamFE.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.stream().forEach(System.out::println);\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid run(List<String> list) {\n\t\tlist.forEach(System.out::println);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarExplicitArrayInit() throws Exception {
		final var file = tempDir.newFile("VarArr.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal var a = new String[]{\"a\"};\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal String[] a = {\"a\"};\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarFinalLocalInteraction() throws Exception {
		final var file = tempDir.newFile("VarFinal.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x = 5;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarForEach() throws Exception {
		final var file = tempDir.newFile("VarFE.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfor (String item : List.of(\"a\"))\n\t\t\tSystem.out.println(item);\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfor (var item : List.of(\"a\"))\n\t\t\tSystem.out.println(item);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarForInit() throws Exception {
		final var file = tempDir.newFile("VarFor.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfor (int i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfor (var i = 0; i < 10; ++i)\n\t\t\tSystem.out.println(i);\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarGenericType() throws Exception {
		final var file = tempDir.newFile("VarGen.java");
		Files.writeString(file.toPath(), "import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfinal List<String> l = List.of();\n\t}\n}");

		assertEquals(
				"import java.util.List;\nclass T {\n\tvoid f() {\n\t\tfinal var l = List.of();\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarLocalString() throws Exception {
		final var file = tempDir.newFile("VarStr.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal String s = \"hi\";\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var s = \"hi\";\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarLocalWithFinal() throws Exception {
		final var file = tempDir.newFile("VarInt.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tfinal int x = 5;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarTabIndented() throws Exception {
		final var file = tempDir.newFile("VarTab.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\t\tfinal int x = 5;\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\t\tfinal var x = 5;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarTryWithResources() throws Exception {
		final var file = tempDir.newFile("VarTry.java");
		Files.writeString(file.toPath(), "import java.io.ByteArrayInputStream;\nclass T {\n\tvoid f() throws Exception {\n\t\ttry (ByteArrayInputStream in = new ByteArrayInputStream(new byte[0])) {\n\t\t\tin.read();\n\t\t}\n\t}\n}");

		assertEquals(
				"import java.io.ByteArrayInputStream;\nclass T {\n\tvoid f() throws Exception {\n\t\ttry (var in = new ByteArrayInputStream(new byte[0])) {\n\t\t\tin.read();\n\t\t}\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testPreferVarWarningNotFixed() throws Exception {
		// float f = a + b with int params: var would infer int, so it's a WARNING
		final var file = tempDir.newFile("VarWarn.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f(int a, int b) {\n\t\tfinal float x = a + b;\n\t}\n}");

		// WARNING should not be fixed — line stays unchanged
		assertEquals(
				"class T {\n\tvoid f(int a, int b) {\n\t\tfinal float x = a + b;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testRedundantImport() throws Exception {
		final var file = tempDir.newFile("Imp.java");
		Files.writeString(file.toPath(), "import java.lang.String;\n\nclass T {\n\tString s;\n}");

		// both RedundantImport and UnusedImports fire on java.lang.String, so
		// the import line is deleted twice — the second delete removes the blank line
		assertEquals("class T {\n\tString s;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testRedundantModifier() throws Exception {
		final var file = tempDir.newFile("Iface.java");
		Files.writeString(file.toPath(), "interface T {\n\tpublic void method();\n}");

		assertEquals("interface T {\n\tvoid method();\n}", runFixAndGetResult(file));
	}

	@Test
	public void testRedundantModifierPrivateEnumConstructor() throws Exception {
		final var file = tempDir.newFile("EnumCtor.java");
		Files.writeString(file.toPath(), "enum Color {\n\tRED(1);\n\n\tprivate Color(int code) {\n\t}\n}");

		assertEquals("enum Color {\n\tRED(1);\n\n\tColor(int code) {\n\t}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testRedundantModifierStaticInterfaceField() throws Exception {
		final var file = tempDir.newFile("IfaceField.java");
		Files.writeString(file.toPath(), "interface T {\n\tstatic int VALUE = 5;\n}");

		assertEquals("interface T {\n\tint VALUE = 5;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testRedundantNumericSuffix() throws Exception {
		final var file = tempDir.newFile("Suffix.java");
		Files.writeString(file.toPath(), "class T {\n\tlong x = 100L;\n\tdouble d = 1.0d;\n}");

		assertEquals("class T {\n\tlong x = 100;\n\tdouble d = 1.0;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testRedundantNumericSuffixHexAndBinaryAndFloat() throws Exception {
		final var file = tempDir.newFile("SuffixHex.java");
		Files.writeString(file.toPath(), "class T {\n\tlong a = 0xFFL;\n\tfloat b = 100F;\n\tlong c = 0b1010L;\n}");

		assertEquals("class T {\n\tlong a = 0xFF;\n\tfloat b = 100;\n\tlong c = 0b1010;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testSuperCall() throws Exception {
		final var file = tempDir.newFile("Child.java");
		Files.writeString(file.toPath(), "class Child extends Object {\n\tChild() {\n\t\tsuper();\n\t}\n}");

		assertEquals("class Child extends Object {\n\tChild() {\n\t}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testSuperCallTabIndented() throws Exception {
		final var file = tempDir.newFile("ChildTab.java");
		Files.writeString(file.toPath(), "class Outer {\n\tclass Inner extends Object {\n\t\tInner() {\n\t\t\tsuper();\n\t\t}\n\t}\n}");

		assertEquals("class Outer {\n\tclass Inner extends Object {\n\t\tInner() {\n\t\t}\n\t}\n}", runFixAndGetResult(file));
	}

	@Test
	public void testTabColumnConversion() {
		assertEquals(0, CheckstyleFixTask.tabColumnToCharIndex("hello", 0));
		assertEquals(5, CheckstyleFixTask.tabColumnToCharIndex("hello", 5));
		assertEquals(1, CheckstyleFixTask.tabColumnToCharIndex("\thello", 8));
		assertEquals(2, CheckstyleFixTask.tabColumnToCharIndex("\t\thello", 16));
		assertEquals(6, CheckstyleFixTask.tabColumnToCharIndex("\thello world", 13));
	}

	@Test
	public void testTabColumnConversionBeyondLine() {
		assertEquals(5, CheckstyleFixTask.tabColumnToCharIndex("hello", 10));
	}

	@Test
	public void testTabColumnConversionMidLineTab() {
		// "ab\tcd" - a=0, b=1, \t=2 (expands from col 2 to col 8), c=3 at col 8, d=4 at col 9
		assertEquals(3, CheckstyleFixTask.tabColumnToCharIndex("ab\tcd", 8));
		assertEquals(4, CheckstyleFixTask.tabColumnToCharIndex("ab\tcd", 9));
	}

	@Test
	public void testTabColumnConversionNoTabs() {
		assertEquals(0, CheckstyleFixTask.tabColumnToCharIndex("abcdef", 0));
		assertEquals(3, CheckstyleFixTask.tabColumnToCharIndex("abcdef", 3));
		assertEquals(6, CheckstyleFixTask.tabColumnToCharIndex("abcdef", 6));
	}

	@Test
	public void testTrailingNewline() throws Exception {
		final var file = tempDir.newFile("TrailNl.java");
		Files.writeString(file.toPath(), "class T {}\n");

		assertEquals("class T {}", runFixAndGetResult(file));
	}

	@Test
	public void testTrailingNewlineDouble() throws Exception {
		final var file = tempDir.newFile("TrailNl2.java");
		Files.writeString(file.toPath(), "class T {}\n\n");

		assertEquals("class T {}", runFixAndGetResult(file));
	}

	@Test
	public void testTrailingWhitespace() throws Exception {
		final var file = tempDir.newFile("Trail.java");
		Files.writeString(file.toPath(), "class T {   \n\tint x;\t\n}");

		assertEquals("class T {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testTrailingWhitespaceTabOnly() throws Exception {
		final var file = tempDir.newFile("TrailTab.java");
		Files.writeString(file.toPath(), "class T {\t\t\n\tint x;\n}");

		assertEquals("class T {\n\tint x;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testUnnecessaryThis() throws Exception {
		final var file = tempDir.newFile("This.java");
		Files.writeString(file.toPath(), "class T {\n\tint value;\n\tint get() {\n\t\treturn this.value;\n\t}\n}");

		assertEquals(
				"class T {\n\tint value;\n\tint get() {\n\t\treturn value;\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testUnnecessaryThisChained() throws Exception {
		final var file = tempDir.newFile("ThisChain.java");
		Files.writeString(file.toPath(), "class T {\n\tString value;\n\tint get() {\n\t\treturn this.value.length();\n\t}\n}");

		assertEquals(
				"class T {\n\tString value;\n\tint get() {\n\t\treturn value.length();\n\t}\n}",
				runFixAndGetResult(file)
		);
	}

	@Test
	public void testUnusedImport() throws Exception {
		final var file = tempDir.newFile("Unused.java");
		Files.writeString(file.toPath(), "import java.util.List;\n\nclass T {\n}");

		assertEquals("\nclass T {\n}", runFixAndGetResult(file));
	}

	@Test
	public void testUpperEll() throws Exception {
		// use a value that doesn't fit in int, so RedundantNumericSuffix doesn't also fire
		final var file = tempDir.newFile("Ell.java");
		Files.writeString(file.toPath(), "class T {\n\tlong x = 3000000000l;\n}");

		assertEquals("class T {\n\tlong x = 3000000000L;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testUpperEllHex() throws Exception {
		// hex value exceeding int range to avoid RedundantNumericSuffix interference
		final var file = tempDir.newFile("EllHex.java");
		Files.writeString(file.toPath(), "class T {\n\tlong x = 0xB00000000l;\n}");

		assertEquals("class T {\n\tlong x = 0xB00000000L;\n}", runFixAndGetResult(file));
	}
}