package com.etk2000.checkstyle.gradle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.util.Properties;

public class CheckstylePluginTest {
	@Rule
	public final TemporaryFolder tempDir = new TemporaryFolder();

	@Test
	public void testApplyConfiguresProject() throws Exception {
		final var projectDir = tempDir.newFolder("project");
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
		final var projectDir = tempDir.newFolder("project");
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
}