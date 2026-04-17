package com.etk2000.checkstyle.gradle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction;

import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

public class CheckstylePluginTest {
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
	public void countViolationsEmptyXml() throws Exception {
		final var file = writeXml("");
		assertArrayEquals(
				new int[]{0, 0},
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
		// NoDoubleBlankLines (message), PreferVarCheck (source, regardless of warning severity)
		// not fixable: NoSpaceIndent (message not fixable), CovariantEquals (not fixable check)
		assertArrayEquals(
				new int[]{6, 4},
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
	public void countViolationsOnlyWarnings() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="warning" message="prefer.var.local" source="com.etk2000.checkstyle.PreferVarCheck"/>
				<error line="2" severity="warning" message="No trailing whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// both are from fixable checks; severity no longer affects fixable count
		assertArrayEquals(
				new int[]{2, 2},
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
	public void countViolationsSameSourceMixedSeverity() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				<error line="2" severity="warning" message="msg" source="com.etk2000.checkstyle.PreferVarCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		// both should be counted: total=2, fixable=2 (severity does not affect fixable count)
		assertArrayEquals(
				new int[]{2, 2},
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
		final var idPattern = Pattern.compile(
				"<property\\s+name=\"id\"\\s+value=\"([^\"]+)\"",
				Pattern.MULTILINE
		);
		final var modulePattern = Pattern.compile(
				"<module\\s+name=\"Regexp(?:Multiline|Singleline)\">(.*?)</module>",
				Pattern.DOTALL
		);
		final var msgPattern = Pattern.compile(
				"<property\\s+name=\"message\"[^>]*value=\"([^\"]+)\"",
				Pattern.DOTALL
		);

		final var messagesFromXml = new HashSet<String>();
		final var moduleMatcher = modulePattern.matcher(xml);
		while (moduleMatcher.find()) {
			final var body = moduleMatcher.group(1);
			final var idMatcher = idPattern.matcher(body);
			final var messageMatcher = msgPattern.matcher(body);
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

		final var pattern = Pattern.compile(
				"<module\\s+name=\"com\\.etk2000\\.checkstyle\\.PreferVarCheck\">"
						+ "(.*?)</module>",
				Pattern.DOTALL
		);
		final var propPattern = Pattern.compile(
				"<property\\s+name=\"allowedMethods\"\\s+value=\"([^\"]+)\""
		);

		final var moduleMatcher = pattern.matcher(xml);
		assertTrue(moduleMatcher.find(), "PreferVarCheck module not found in checkstyle.xml");

		final var propMatcher = propPattern.matcher(moduleMatcher.group(1));
		assertTrue(propMatcher.find(), "allowedMethods property not found on PreferVarCheck");
		assertEquals(CheckstyleFixAction.fixerAllowedMethods(), propMatcher.group(1));
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
		task.get().extract();

		assertTrue(outputFile.exists());

		final var content = Files.readString(outputFile.toPath());
		assertTrue(content.startsWith("<?xml"));
		assertTrue(content.contains("<module name=\"Checker\">"));
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