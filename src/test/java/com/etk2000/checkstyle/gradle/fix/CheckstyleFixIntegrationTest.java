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
		for (final var checkName : CheckstyleFixTask.FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.endsWith("FinalLocalVariableCheck"))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
			treeWalkerConfig.addChild(checkConfig);
		}

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		// Checker-level regex modules
		final var doubleBlankConfig = new DefaultConfiguration("RegexpMultiline");
		doubleBlankConfig.addProperty("id", "NoDoubleBlankLines");
		doubleBlankConfig.addProperty("format", "\\n\\s*\\n\\s*\\n");
		doubleBlankConfig.addProperty("message", "No double blank lines.");
		checkerConfig.addChild(doubleBlankConfig);

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
	public void testDoubleBlankLines() throws Exception {
		final var file = tempDir.newFile("Dbl.java");
		Files.writeString(file.toPath(), "class T {\n\tint x;\n\n\n\tint y;\n}");

		assertEquals("class T {\n\tint x;\n\n\tint y;\n}", runFixAndGetResult(file));
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
	public void testFinalLocalVariable() throws Exception {
		final var file = tempDir.newFile("Final.java");
		Files.writeString(file.toPath(), "class T {\n\tvoid f() {\n\t\tint x = 5;\n\t\tvar y = \"hello\";\n\t}\n}");

		assertEquals(
				"class T {\n\tvoid f() {\n\t\tfinal var x = 5;\n\t\tfinal var y = \"hello\";\n\t}\n}",
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
	public void testNoViolations() throws Exception {
		final var file = tempDir.newFile("Clean.java");
		final var input = "class Clean {\n\tint[] a = {1, 2};\n\tint x = 100;\n}";
		Files.writeString(file.toPath(), input);

		assertEquals(0, runFixPipeline(file));
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
	public void testRedundantNumericSuffix() throws Exception {
		final var file = tempDir.newFile("Suffix.java");
		Files.writeString(file.toPath(), "class T {\n\tlong x = 100L;\n\tdouble d = 1.0d;\n}");

		assertEquals("class T {\n\tlong x = 100;\n\tdouble d = 1.0;\n}", runFixAndGetResult(file));
	}

	@Test
	public void testSuperCall() throws Exception {
		final var file = tempDir.newFile("Child.java");
		Files.writeString(file.toPath(), "class Child extends Object {\n\tChild() {\n\t\tsuper();\n\t}\n}");

		assertEquals("class Child extends Object {\n\tChild() {\n\t}\n}", runFixAndGetResult(file));
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
	public void testTrailingWhitespace() throws Exception {
		final var file = tempDir.newFile("Trail.java");
		Files.writeString(file.toPath(), "class T {   \n\tint x;\t\n}");

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
}