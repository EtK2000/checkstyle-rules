package com.etk2000.checkstyle.gradle;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Files;

import javax.annotation.Nonnull;

public class CheckstylePlugin implements Plugin<Project> {
	public static abstract class ExtractCheckstyleConfig extends DefaultTask {
		@OutputFile
		public abstract RegularFileProperty getOutputFile();

		@TaskAction
		public void extract() {
			var outputFile = getOutputFile().get().getAsFile();
			outputFile.getParentFile().mkdirs();
			try (InputStream in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
				if (in == null)
					throw new IllegalStateException("Bundled checkstyle.xml not found in plugin JAR");

				try (OutputStream out = Files.newOutputStream(outputFile.toPath())) {
					in.transferTo(out);
				}
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to extract checkstyle.xml", e);
			}
		}
	}

	private static final String CHECKSTYLE_VERSION = "10.22.0";

	@Override
	public void apply(@Nonnull Project project) {
		project.getPluginManager().apply("checkstyle");

		var configFile = new File(project.getLayout().getBuildDirectory().getAsFile().get(), "checkstyle/checkstyle.xml");

		var extractTask = project.getTasks().register("extractCheckstyleConfig", ExtractCheckstyleConfig.class, task ->
				task.getOutputFile().set(configFile)
		);

		var ext = project.getExtensions().getByType(CheckstyleExtension.class);
		ext.setToolVersion(CHECKSTYLE_VERSION);
		ext.setConfigFile(configFile);
		ext.getConfigDirectory().set(project.file("config/checkstyle"));

		var checkstyleConfig = project.getConfigurations().getByName("checkstyle");
		addDependencies(project, checkstyleConfig);

		registerTasks(project, extractTask.getName());
	}

	private static void addDependencies(@Nonnull Project project, @Nonnull Configuration checkstyleConfig) {
		checkstyleConfig.getDependencies().add(
				project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + CHECKSTYLE_VERSION)
		);

		// Add this plugin's own JAR to the checkstyle classpath so custom check classes are available
		URI pluginJar = null;
		try {
			pluginJar = CheckstylePlugin.class.getProtectionDomain().getCodeSource().getLocation().toURI();
		}
		catch (Exception ignored) {
		}
		if (pluginJar != null)
			checkstyleConfig.getDependencies().add(project.getDependencies().create(project.files(new File(pluginJar))));
	}

	private static void registerTasks(@Nonnull Project project, @Nonnull String extractTaskName) {
		project.getTasks().register("checkstyleMain", Checkstyle.class, task -> {
			task.dependsOn(extractTaskName);
			task.setSource(project.fileTree("src/main/java"));
			task.setClasspath(project.files());
			task.include("**/*.java");
		});

		var testDir = project.file("src/test/java");
		project.getTasks().register("checkstyleTest", Checkstyle.class, task -> {
			task.dependsOn(extractTaskName);
			task.setSource(project.fileTree("src/test/java"));
			task.setClasspath(project.files());
			task.include("**/*.java");
			task.onlyIf(t -> testDir.exists());
		});

		project.afterEvaluate(p -> {
			var checkTask = p.getTasks().findByName("check");
			if (checkTask != null)
				checkTask.dependsOn("checkstyleMain", "checkstyleTest");
		});
	}
}