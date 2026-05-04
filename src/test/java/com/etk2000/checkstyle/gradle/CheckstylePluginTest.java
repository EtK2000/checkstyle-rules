package com.etk2000.checkstyle.gradle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixTask;

import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class CheckstylePluginTest {
	private static final Pattern ALLOWED_METHODS_PATTERN = Pattern.compile(
			"<property\\s+name=\"allowedMethods\"\\s+value=\"([^\"]+)\""
	);
	private static final Pattern ID_PATTERN = Pattern.compile(
			"<property\\s+name=\"id\"\\s+value=\"([^\"]+)\"",
			Pattern.MULTILINE
	);
	private static final Pattern MODULE_PATTERN = Pattern.compile(
			"<module\\s+name=\"Regexp(?:Multiline|Singleline)\">(.*?)</module>",
			Pattern.DOTALL
	);
	private static final Pattern MSG_PATTERN = Pattern.compile(
			"<property\\s+name=\"message\"[^>]*value=\"([^\"]+)\"",
			Pattern.DOTALL
	);
	private static final Pattern PREFER_VAR_MODULE_PATTERN = Pattern.compile(
			"<module\\s+name=\"com\\.etk2000\\.checkstyle\\.PreferVarCheck\">"
					+ "(.*?)</module>",
			Pattern.DOTALL
	);
	private static final Set<String> FIXABLE_MESSAGES = FixableCheckNames.FIXABLE_MESSAGES;
	private static final Set<String> FIXABLE_NAMES = FixableCheckNames.all();

	private static final String XML_FOOTER = "</checkstyle>";
	private static final String XML_HEADER = """
			<?xml version="1.0" encoding="UTF-8"?>
			<checkstyle version="10.21.4">
			""";

	@TempDir
	Path tempDir;

	@Test
	public void countViolationsAttributeOrderIrrelevant() throws Exception {
		final var xml = """
				<file name="A.java">
				<error source="com.etk2000.checkstyle.PreferPrefixIncrementCheck" line="1" message="msg" severity="error"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsAttributeSpacesAroundEquals() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity = "error" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// severity = "error" (with spaces) doesn't match severity="([^"]+)" regex
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsEmptyAttributeValues() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="" message="" source=""/>
				</file>
				""";
		final var file = writeXml(xml);
		// severity="" doesn't match regex [^"]+ so treated as missing -> not fixable
		// source="" and message="" also don't match their [^"]+ regexes
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsEmptyFile() throws Exception {
		final var file = tempDir.resolve("empty.xml").toFile();
		Files.writeString(file.toPath(), "");
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsEmptyXml() throws Exception {
		final var file = writeXml("");
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsErrorElementMultiline() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1"
				severity="error"
				message="No trailing whitespace."
				source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// [^>] character class matches newlines, so multi-line elements are matched
		assertArrayEquals(
				new int[]{1, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsErrorSeverityNoAttributes() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsErrorSeverityUppercase() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="Error" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsErrorTagWordBoundary() throws Exception {
		final var xml = """
				<file name="A.java">
				<errorSummary line="1" severity="error" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// \b in <error\b prevents matching <errorSummary>
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsFixableMessagesNotHardcoded() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="No trailing whitespace."/>
				</file>
				""";
		final var file = writeXml(xml);
		// message is normally fixable, but passing empty set proves the parameter is used
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, Set.of(), Set.of())
		);
	}

	@Test
	public void countViolationsFixableNamesNotHardcoded() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="msg" source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// source is normally fixable, but passing empty set proves the parameter is used
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, Set.of(), Set.of())
		);
	}

	@Test
	public void countViolationsIgnoreSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="ignore" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsInfoSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="info" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsIoException() {
		final var dir = tempDir.toFile();
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(dir, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsMissingSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsMixed() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Use prefix increment." source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				<error line="2" severity="error" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="3" severity="error" message="Use TABs for indentation, not spaces." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="4" severity="error" message="Covariant equals." source="com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck"/>
				<error line="5" severity="error" message="No double blank lines." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
				<error line="6" severity="warning" message="prefer.var.local" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// fixable: PreferPrefixIncrementCheck (source), NoTrailingWhitespace (message),
		// NoDoubleBlankLines (message)
		// not fixable: NoSpaceIndent (message not fixable), CovariantEquals (not fixable check),
		// PreferVarCheck (warning severity)
		assertArrayEquals(
				new int[]{6, 3},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsMultipleElementsSameLine() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/><error line="2" severity="error" source="com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{2, 2},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsMultipleFiles() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Use prefix increment." source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				<error line="2" severity="warning" message="prefer.var.local" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				<file name="B.java">
				<error line="1" severity="error" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{3, 2},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNoMessageAttribute() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" source="com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNonExistentFile() {
		final var file = new File(tempDir.toFile(), "does-not-exist.xml");
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNonFixableRegexp() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Use TABs for indentation, not spaces." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNonFixableTreeWalker() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Covariant equals." source="com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNonSelfClosingElementIgnored() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" source="com.etk2000.checkstyle.PreferVarCheck">text</error>
				</file>
				""";
		final var file = writeXml(xml);
		// regex requires /> (self-closing), so <error ...> is not matched
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNoSourceAttribute() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="No trailing whitespace."/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNoSourceNonFixableMessage() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Some non-fixable message."/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{1, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsOnlyWarnings() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="warning" message="prefer.var.local" source="com.etk2000.checkstyle.PreferVarCheck"/>
				<error line="2" severity="warning" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// both from fixable checks but warning severity, so not counted as fixable
		assertArrayEquals(
				new int[]{2, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsRegexpOnly() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="2" severity="error" message="No double blank lines." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
				<error line="3" severity="error" message="No blank line before closing brace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{3, 3},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsSameMessageMixedSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="2" severity="warning" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{2, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsSameSourceMixedSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				<error line="2" severity="warning" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// only the error-severity violation is fixable
		assertArrayEquals(
				new int[]{2, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsSourceAndMessageBothFixable() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="No trailing whitespace." source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// source matches fixable names, so fixable is incremented once via source;
		// message also matches fixable messages but the else-branch is not reached
		assertArrayEquals(
				new int[]{1, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsTreeWalkerOnly() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Use prefix increment." source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				<error line="2" severity="error" message="Redundant import." source="com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{2, 2},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void fixableCheckNamesSyncWithFixers() {
		assertEquals(FixableCheckNames.all(), CheckstyleFixAction.fixableSourceNames());
	}

	/**
	 * Verifies that every fixable regexp module ID in {@link FixableCheckNames#MODULE_IDS}
	 * has its message in {@link FixableCheckNames#FIXABLE_MESSAGES}, by parsing checkstyle.xml.
	 */
	@Test
	public void fixableMessagesSyncWithCheckstyleXml() throws Exception {
		final String xml;
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
			assertNotNull(in);
			xml = new String(in.readAllBytes());
		}

		// extract id/message pairs from regexp modules in checkstyle.xml
		final var messagesFromXml = new HashSet<String>();
		final var moduleMatcher = MODULE_PATTERN.matcher(xml);
		while (moduleMatcher.find()) {
			final var body = moduleMatcher.group(1);
			final var idMatcher = ID_PATTERN.matcher(body);
			final var messageMatcher = MSG_PATTERN.matcher(body);
			if (idMatcher.find() && messageMatcher.find()) {
				final var id = idMatcher.group(1);
				if (FixableCheckNames.MODULE_IDS.contains(id))
					messagesFromXml.add(messageMatcher.group(1));
			}
		}

		assertEquals(FixableCheckNames.FIXABLE_MESSAGES, messagesFromXml);
	}

	/**
	 * Verifies that the fixer's allowedMethods for PreferVarCheck matches checkstyle.xml.
	 */
	@Test
	public void fixerAllowedMethodsSyncWithCheckstyleXml() throws Exception {
		final String xml;
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
			assertNotNull(in);
			xml = new String(in.readAllBytes());
		}

		final var moduleMatcher = PREFER_VAR_MODULE_PATTERN.matcher(xml);
		assertTrue(moduleMatcher.find(), "PreferVarCheck module not found in checkstyle.xml");

		final var propMatcher = ALLOWED_METHODS_PATTERN.matcher(moduleMatcher.group(1));
		assertTrue(propMatcher.find(), "allowedMethods property not found on PreferVarCheck");
		assertEquals(CheckstyleFixAction.fixerAllowedMethods(), propMatcher.group(1));
	}

	@Test
	public void fixTaskDryRunNotPresent() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("fixProject")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		project.getConfigurations().create("compileOnly");
		new CheckstylePlugin().apply(project);

		final var fixTask = (CheckstyleFixTask) project.getTasks().findByName("checkstyleFix");
		assertNotNull(fixTask);
		assertFalse(fixTask.getDryRun().isPresent());
	}

	/**
	 * CheckstyleFixTask must have zero checkstyle imports so Gradle can
	 * decorate it on the buildscript classpath (which lacks checkstyle).
	 * All checkstyle-dependent logic lives in CheckstyleFixAction, which
	 * runs inside an isolated classloader via the Worker API.
	 */
	@Test
	public void fixTaskHasNoCheckstyleImports() throws Exception {
		final var source = Path.of("src/main/java/com/etk2000/checkstyle/gradle/fix/CheckstyleFixTask.java");
		final var content = Files.readString(source);
		assertFalse(
				content.contains("import com.puppycrawl"),
				"CheckstyleFixTask must not import checkstyle classes"
		);
	}

	@Test
	public void hintTaskRegisteredAsDryRun() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("hintProject")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		project.getConfigurations().create("compileOnly");
		new CheckstylePlugin().apply(project);

		final var task = (CheckstyleFixTask) project.getTasks().findByName("checkstyleFixHint");
		assertNotNull(task);
		assertTrue(task.getDryRun().get());
		assertFalse(task.getTestSource().isPresent());
	}

	@Test
	public void hintTaskTestSourceSetWhenDirExists() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("hintTestProject")).toFile();
		Files.createDirectories(new File(projectDir, "src/test/java").toPath());
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		project.getConfigurations().create("compileOnly");
		new CheckstylePlugin().apply(project);

		final var task = (CheckstyleFixTask) project.getTasks().findByName("checkstyleFixHint");
		assertNotNull(task);
		assertTrue(task.getTestSource().isPresent());
	}

	@Test
	public void selectFixTaskNameBothFixable() {
		assertEquals("checkstyleFixAll", CheckstylePlugin.selectFixTaskName(3, 2));
	}

	@Test
	public void selectFixTaskNameMainOnly() {
		assertEquals("checkstyleFix", CheckstylePlugin.selectFixTaskName(5, 0));
	}

	@Test
	public void selectFixTaskNameNegativeInputs() {
		assertEquals("checkstyleFixAll", CheckstylePlugin.selectFixTaskName(-1, 0));
		assertEquals("checkstyleFixAll", CheckstylePlugin.selectFixTaskName(0, -1));
		assertEquals("checkstyleFixAll", CheckstylePlugin.selectFixTaskName(-1, -1));
	}

	@Test
	public void selectFixTaskNameNeitherFixable() {
		assertEquals("checkstyleFixAll", CheckstylePlugin.selectFixTaskName(0, 0));
	}

	@Test
	public void selectFixTaskNameTestOnly() {
		assertEquals("checkstyleFixTest", CheckstylePlugin.selectFixTaskName(0, 3));
	}

	@Test
	public void testApplyConfiguresProject() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("project")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		project.getConfigurations().create("compileOnly");
		new CheckstylePlugin().apply(project);

		assertNotNull(project.getTasks().findByName("checkstyleFix"));
		assertNotNull(project.getTasks().findByName("checkstyleFixAll"));
		assertNotNull(project.getTasks().findByName("checkstyleFixHint"));
		assertNotNull(project.getTasks().findByName("checkstyleFixTest"));
		assertNotNull(project.getTasks().findByName("checkstyleMain"));
		assertNotNull(project.getTasks().findByName("checkstyleTest"));
		assertNotNull(project.getTasks().findByName("extractCheckstyleConfig"));

		final var ext = project.getExtensions().getByType(CheckstyleExtension.class);
		assertNotNull(ext.getToolVersion());
		assertTrue(ext.getToolVersion().matches("\\d+\\.\\d+(\\.\\d+)?"));
	}

	@Test
	public void testExtractCheckstyleConfig() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("project")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		final var outputFile = new File(projectDir, "build/checkstyle.xml");
		final var task = project.getTasks().register(
				"extractConfig",
				CheckstylePlugin.ExtractCheckstyleConfig.class,
				t -> t.getOutputFile().set(outputFile)
		);

		assertFalse(outputFile.getParentFile().exists());
		task.get().extract();
		assertTrue(outputFile.exists());

		final byte[] expectedBytes;
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
			assertNotNull(in);
			expectedBytes = in.readAllBytes();
		}
		assertArrayEquals(expectedBytes, Files.readAllBytes(outputFile.toPath()));
	}

	@Test
	public void testExtractCheckstyleConfigDirCreateFailure() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("dirCreateFail")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		final var blocker = new File(projectDir, "blocker");
		Files.writeString(blocker.toPath(), "");
		final var outputFile = new File(blocker, "sub/checkstyle.xml");
		final var task = project.getTasks().register(
				"extractConfigDirFail",
				CheckstylePlugin.ExtractCheckstyleConfig.class,
				t -> t.getOutputFile().set(outputFile)
		);

		final var ex = assertThrows(RuntimeException.class, () -> task.get().extract());
		assertEquals("Failed to extract checkstyle.xml", ex.getMessage());
		assertInstanceOf(IOException.class, ex.getCause());
		assertTrue(ex.getCause().getMessage().contains(blocker.toString()));
		assertFalse(outputFile.exists());
		assertFalse(outputFile.getParentFile().exists());
		assertTrue(blocker.isFile());
	}

	@Test
	public void testExtractCheckstyleConfigIdempotent() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("idempotent")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		final var outputFile = new File(projectDir, "build/checkstyle.xml");
		final var task = project.getTasks().register(
				"extractConfigIdempotent",
				CheckstylePlugin.ExtractCheckstyleConfig.class,
				t -> t.getOutputFile().set(outputFile)
		);
		task.get().extract();
		final var firstContent = Files.readString(outputFile.toPath());
		Files.writeString(outputFile.toPath(), "GARBAGE");
		task.get().extract();

		assertTrue(outputFile.exists());
		assertEquals(firstContent, Files.readString(outputFile.toPath()));
	}

	@Test
	public void testExtractCheckstyleConfigOutputFileIsDirectory() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("outputIsDir")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		final var outputFile = new File(projectDir, "build/checkstyle.xml");
		Files.createDirectories(outputFile.toPath());
		final var task = project.getTasks().register(
				"extractConfigOutputIsDir",
				CheckstylePlugin.ExtractCheckstyleConfig.class,
				t -> t.getOutputFile().set(outputFile)
		);

		final var ex = assertThrows(RuntimeException.class, () -> task.get().extract());
		assertEquals("Failed to extract checkstyle.xml", ex.getMessage());
		assertInstanceOf(IOException.class, ex.getCause());
		assertTrue(ex.getCause().getMessage().contains(outputFile.toString()));
		assertTrue(outputFile.isDirectory());
	}

	@Test
	public void testPluginPropertiesVersion() throws Exception {
		final var props = new Properties();
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/plugin.properties")) {
			assertNotNull(in);
			props.load(in);
		}

		final var version = props.getProperty("checkstyle.version");
		assertNotNull(version);
		assertTrue(version.matches("\\d+\\.\\d+(\\.\\d+)?"));
	}

	private File writeXml(String body) throws Exception {
		final var file = tempDir.resolve("report.xml").toFile();
		Files.writeString(file.toPath(), XML_HEADER + body + XML_FOOTER);
		return file;
	}
}