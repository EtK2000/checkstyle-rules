package com.etk2000.checkstyle.gradle;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.BaseCheckTest;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixAction;
import com.etk2000.checkstyle.gradle.fix.CheckstyleFixTask;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.checks.imports.ImportControlCheck;

import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.xml.sax.InputSource;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

@ResourceLock(CheckstylePluginTest.PROJECT_BUILDER)
public class CheckstylePluginTest {
	private static final Pattern ALLOWED_METHODS_PATTERN = Pattern.compile(
			"<property\\s+name=\"allowedMethods\"\\s+value=\"([^\"]+)\""
	);
	private static final Pattern CUSTOM_CHECK_MODULE_PATTERN = Pattern.compile(
			"<module\\s+name=\"(com\\.etk2000\\.checkstyle\\.\\w+Check)\"\\s*(?:/>|>(.*?)</module>)",
			Pattern.DOTALL
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

	/** The resource lock held by every class building a project, as Gradle documents no thread safety for {@link ProjectBuilder}. */
	public static final String PROJECT_BUILDER = "org.gradle.testfixtures.ProjectBuilder";
	private static final String XML_FOOTER = "</checkstyle>";
	private static final String XML_HEADER = """
			<?xml version="1.0" encoding="UTF-8"?>
			<checkstyle version="10.21.4">
			""";

	private static void assertNoCustomCheckDeclaresProperty(String propertyName, String rationale) throws IOException {
		final var xml = shippedConfigXml();

		final var propertyPattern = propertyDeclarationPattern(propertyName);
		final var matcher = CUSTOM_CHECK_MODULE_PATTERN.matcher(xml);
		final var matchedChecks = new HashSet<String>();
		while (matcher.find()) {
			final var name = matcher.group(1);
			matchedChecks.add(name);
			final var body = matcher.group(2);
			if (body != null) {
				assertFalse(
						propertyPattern.matcher(body).find(),
						"Custom check " + name + " sets " + propertyName + " in checkstyle.xml; " + rationale
				);
			}
		}
		assertTrue(
				matchedChecks.containsAll(Set.of(
						"com.etk2000.checkstyle.EmptyBodyCheck",
						"com.etk2000.checkstyle.NoFinalParametersCheck",
						"com.etk2000.checkstyle.PreferImportCheck",
						"com.etk2000.checkstyle.PreferVarCheck"
				)),
				"custom-check module regex did not match the expected checks; matched " + matchedChecks
		);
	}

	private static Pattern propertyDeclarationPattern(String propertyName) {
		return Pattern.compile("name\\s*=\\s*[\"']" + Pattern.quote(propertyName) + "[\"']");
	}

	private static Map<String, String> regexpMessagesById(String xml) {
		final var messages = new HashMap<String, String>();
		final var moduleMatcher = MODULE_PATTERN.matcher(xml);
		while (moduleMatcher.find()) {
			final var body = moduleMatcher.group(1);
			final var idMatcher = ID_PATTERN.matcher(body);
			final var messageMatcher = MSG_PATTERN.matcher(body);
			if (idMatcher.find() && messageMatcher.find())
				messages.put(idMatcher.group(1), messageMatcher.group(1));
		}
		return messages;
	}

	private static String shippedConfigXml() throws IOException {
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
			assertNotNull(in);
			return new String(in.readAllBytes());
		}
	}

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
				message="Line must not end with whitespace."
				source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
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
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsFixableMessagesNotHardcoded() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Line must not end with whitespace."/>
				</file>
				""";
		final var file = writeXml(xml);
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
				<error line="2" severity="error" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="3" severity="error" message="Indentation must use tabs, not spaces." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="4" severity="error" message="Covariant equals." source="com.puppycrawl.tools.checkstyle.checks.coding.CovariantEqualsCheck"/>
				<error line="5" severity="error" message="File must not contain consecutive blank lines." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
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
				<error line="1" severity="error" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
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
				<error line="1" severity="error" message="Indentation must use tabs, not spaces." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
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
		assertArrayEquals(
				new int[]{0, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsNoSourceAttribute() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Line must not end with whitespace."/>
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
				<error line="2" severity="warning" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
		assertArrayEquals(
				new int[]{2, 0},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsRegexpOnly() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="2" severity="error" message="File must not contain consecutive blank lines." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
				<error line="3" severity="error" message="Closing brace must not be preceded by a blank line." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpMultilineCheck"/>
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
				<error line="1" severity="error" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
				<error line="2" severity="warning" message="Line must not end with whitespace." source="com.puppycrawl.tools.checkstyle.checks.regexp.RegexpSinglelineCheck"/>
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
		assertArrayEquals(
				new int[]{2, 1},
				CheckstylePlugin.countViolations(file, FIXABLE_NAMES, FIXABLE_MESSAGES)
		);
	}

	@Test
	public void countViolationsSourceAndMessageBothFixable() throws Exception {
		final var xml = """
				<file name="A.java">
				<error line="1" severity="error" message="Line must not end with whitespace." source="com.etk2000.checkstyle.PreferPrefixIncrementCheck"/>
				</file>
				""";
		final var file = writeXml(xml);
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
	public void customCheckModulesDeclareNoSeverity() throws Exception {
		assertNoCustomCheckDeclaresProperty(
				"severity",
				"severity must live in the check code (AbstractAstCheck.logWarning), not config"
		);
	}

	@Test
	public void customCheckModulesDeclareNoTokens() throws Exception {
		assertNoCustomCheckDeclaresProperty(
				"tokens",
				"custom checks run at their default token set (getDefaultTokens), which is what the tests cover"
		);
	}

	@Test
	public void customCheckPropertyGuardDetectsDeclaredProperty() {
		final var xml = "<module name=\"com.etk2000.checkstyle.FooCheck\">"
				+ "<property name=\"tokens\" value=\"METHOD_DEF\" /></module>";
		final var matcher = CUSTOM_CHECK_MODULE_PATTERN.matcher(xml);
		assertTrue(matcher.find());
		final var body = matcher.group(2);
		assertNotNull(body);
		assertTrue(propertyDeclarationPattern("tokens").matcher(body).find());
		assertFalse(propertyDeclarationPattern("severity").matcher(body).find());
		assertTrue(propertyDeclarationPattern("tokens").matcher("name = 'tokens'").find());
	}

	@Test
	public void fixableCheckNamesSyncWithFixers() {
		assertEquals(FixableCheckNames.all(), CheckstyleFixAction.fixableSourceNames());
	}

	/**
	 * countViolations compares the report's message attribute to these constants verbatim, but the
	 * report holds the text after MessageFormat (quotes, braces) and XML escaping (quotes, ampersand,
	 * angle brackets), so a message carrying any of those would silently stop counting as fixable.
	 */
	@Test
	public void fixableMessagesHaveNoCharactersTheReportRewrites() {
		for (var message : FIXABLE_MESSAGES)
			assertTrue(message.chars().noneMatch(c -> "\"&'<>{}".indexOf(c) >= 0), message);
	}

	@Test
	public void fixableMessagesSyncWithCheckstyleXml() throws Exception {
		final var messagesFromXml = new HashSet<String>();
		for (var entry : regexpMessagesById(shippedConfigXml()).entrySet()) {
			if (FixableCheckNames.MODULE_IDS.contains(entry.getKey()))
				messagesFromXml.add(entry.getValue());
		}

		assertEquals(FixableCheckNames.FIXABLE_MESSAGES, messagesFromXml);
	}

	@Test
	public void fixerAllowedMethodsSyncWithCheckstyleXml() throws Exception {
		final var xml = shippedConfigXml();

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
	public void fixTasksGetTheCheckstyleConfigDirectory() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("configDirectoryProject")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		project.getConfigurations().create("compileOnly");
		new CheckstylePlugin().apply(project);

		final var expected = project.getExtensions().getByType(CheckstyleExtension.class).getConfigDirectory().get().getAsFile();
		for (var name : List.of("checkstyleFix", "checkstyleFixHint", "checkstyleFixTest")) {
			final var task = (CheckstyleFixTask) project.getTasks().findByName(name);
			assertNotNull(task, name);
			assertEquals(expected, task.getConfigDirectory().get().getAsFile(), name);
		}
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

	/**
	 * No file in the tree imports {@code AstResolve} from the fix package, so {@code check} alone
	 * never evaluates the rule and a typo in the subpackage nesting or the class name would leave a
	 * silent no-op behind a green build.
	 */
	@Test
	public void importControlBarsAstResolveFromTheFixPackage() throws Exception {
		final var rule = new File("config/checkstyle/import-control.xml");
		assertTrue(rule.isFile(), "rule file not found at " + rule.getAbsolutePath());

		for (var barred : new String[]{
				"import com.etk2000.checkstyle.ast.AstResolve;",
				"import static com.etk2000.checkstyle.ast.AstResolve.resolveVariableType;"
		}) {
			final var events = BaseCheckTest.runCheckInline(
					ImportControlCheck.class,
					"package com.etk2000.checkstyle.gradle.fix;\n" + barred + "\nclass T {}",
					"file",
					rule.getPath()
			);
			assertEquals(1, events.size(), barred);
			assertTrue(
					events.getFirst().getMessage().contains("AstResolve"),
					"message must name the barred class: " + events.getFirst().getMessage()
			);
		}

		// PreferStaticImportConstantFixer depends on AstText staying reachable from the fix package
		assertTrue(BaseCheckTest.runCheckInline(
				ImportControlCheck.class,
				"package com.etk2000.checkstyle.gradle.fix;\nimport com.etk2000.checkstyle.ast.AstText;\nclass T {}",
				"file",
				rule.getPath()
		).isEmpty());

		assertTrue(BaseCheckTest.runCheckInline(
				ImportControlCheck.class,
				"package com.etk2000.checkstyle;\nimport com.etk2000.checkstyle.ast.AstResolve;\nclass T {}",
				"file",
				rule.getPath()
		).isEmpty());
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

	/**
	 * The plugin extracts only {@code checkstyle.xml} and points {@code config_loc} at the
	 * consumer's own {@code config/checkstyle}, so any {@code ${config_loc}} reference the shipped
	 * config makes must tolerate that directory being empty.
	 */
	@Test
	public void shippedConfigLoadsWithAnEmptyConfigLoc() throws Exception {
		final var emptyConfigLoc = Files.createDirectory(tempDir.resolve("empty-config-loc"));
		final var properties = new Properties();
		properties.setProperty("config_loc", emptyConfigLoc.toString());
		properties.setProperty("minSdk", "35");

		final var shipped = CheckstylePlugin.class.getResource("/com/etk2000/checkstyle/checkstyle.xml");
		assertNotNull(shipped);
		final var config = ConfigurationLoader.loadConfiguration(
				new InputSource(shipped.toExternalForm()),
				new PropertiesExpander(properties),
				ConfigurationLoader.IgnoredModulesOptions.EXECUTE
		);
		assertEquals("Checker", config.getName());

		// configure(), not just loadConfiguration(): a module whose file property points at a
		// missing path parses fine and fails when the module is instantiated
		final var checker = new Checker();
		try {
			checker.setModuleClassLoader(getClass().getClassLoader());
			checker.configure(config);
		}
		finally {
			checker.destroy();
		}
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

	@Test
	public void testResourcesRegexpMessagesSyncWithCheckstyleXml() throws Exception {
		final var shipped = regexpMessagesById(shippedConfigXml());
		assertEquals(
				Set.of(
						"BlankLineAfterBreak",
						"NoBlankLineAfterClassBrace",
						"NoBlankLineBeforeClosingBrace",
						"NoDoubleBlankLines",
						"NoSpaceIndent",
						"NoTrailingNewline",
						"NoTrailingWhitespace"
				),
				shipped.keySet()
		);
		assertEquals(
				shipped,
				regexpMessagesById(Files.readString(Path.of("config", "checkstyle", "checkstyle-test-resources.xml")))
		);
	}

	private File writeXml(String body) throws Exception {
		final var file = tempDir.resolve("report.xml").toFile();
		Files.writeString(file.toPath(), XML_HEADER + body + XML_FOOTER);
		return file;
	}
}