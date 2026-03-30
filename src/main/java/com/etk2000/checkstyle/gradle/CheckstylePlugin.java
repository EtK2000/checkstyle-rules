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
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class CheckstylePlugin implements Plugin<Project> {
	public static abstract class ExtractCheckstyleConfig extends DefaultTask {
		@TaskAction
		public void extract() {
			final var outputFile = getOutputFile().get().getAsFile();
			outputFile.getParentFile().mkdirs();
			try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/checkstyle.xml")) {
				if (in == null)
					throw new IllegalStateException("Bundled checkstyle.xml not found in plugin JAR");

				try (var out = Files.newOutputStream(outputFile.toPath())) {
					in.transferTo(out);
				}
			}
			catch (IOException e) {
				throw new RuntimeException("Failed to extract checkstyle.xml", e);
			}
		}

		@OutputFile
		public abstract RegularFileProperty getOutputFile();
	}

	private static final Pattern MANIFEST_MIN_SDK = Pattern.compile(
			"android:minSdkVersion\\s*=\\s*\"(\\d+)\""
	);
	private static final String CHECKSTYLE_VERSION;

	static {
		final var props = new Properties();
		try (var in = CheckstylePlugin.class.getResourceAsStream("/com/etk2000/checkstyle/plugin.properties")) {
			if (in == null)
				throw new IllegalStateException("plugin.properties not found in plugin JAR");
			props.load(in);
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to load plugin.properties", e);
		}
		CHECKSTYLE_VERSION = props.getProperty("checkstyle.version");
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

	@VisibleForTesting
	static String readMinSdkFromManifest(@Nonnull Project project) {
		final var manifest = new File(project.getProjectDir(), "src/main/AndroidManifest.xml");
		if (!manifest.exists())
			return null;

		try {
			final var content = Files.readString(manifest.toPath());
			final var matcher = MANIFEST_MIN_SDK.matcher(content);
			if (matcher.find())
				return matcher.group(1);
		}
		catch (IOException ignored) {
		}
		return null;
	}

	private static void registerTasks(@Nonnull Project project, @Nonnull String extractTaskName) {
		project.getTasks().register(
				"checkstyleMain",
				Checkstyle.class,
				task -> {
					task.dependsOn(extractTaskName);
					task.include("**/*.java");
					task.setClasspath(project.files());
					task.setSource(project.fileTree("src/main/java"));
				}
		);

		final var testDir = project.file("src/test/java");
		project.getTasks().register(
				"checkstyleTest",
				Checkstyle.class,
				task -> {
					task.dependsOn(extractTaskName);
					task.include("**/*.java");
					task.onlyIf(t -> testDir.exists());
					task.setClasspath(project.files());
					task.setSource(project.fileTree("src/test/java"));
				}
		);

		project.afterEvaluate(p -> {
			final var checkTask = p.getTasks().findByName("check");
			if (checkTask != null)
				checkTask.dependsOn("checkstyleMain", "checkstyleTest");
		});
	}

	@VisibleForTesting
	static String resolveMinSdk(@Nonnull Project project) {
		// try Android plugin's minSdk from build.gradle
		try {
			final var android = project.getExtensions().findByName("android");
			if (android != null) {
				final var defaultConfig = android.getClass().getMethod("getDefaultConfig").invoke(android);
				final var minSdkObj = defaultConfig.getClass().getMethod("getMinSdk").invoke(defaultConfig);
				if (minSdkObj != null)
					return minSdkObj.toString();
			}
		}
		catch (Exception ignored) {
		}

		// fallback: try AndroidManifest.xml
		final var manifestMinSdk = readMinSdkFromManifest(project);
		if (manifestMinSdk != null)
			return manifestMinSdk;

		// non-Android: assume latest Java (all APIs available)
		return String.valueOf(Integer.MAX_VALUE);
	}

	@Override
	public void apply(@Nonnull Project project) {
		project.getPluginManager().apply("checkstyle");

		final var configFile = new File(project.getLayout().getBuildDirectory().getAsFile().get(), "checkstyle/checkstyle.xml");

		final var extractTask = project.getTasks().register(
				"extractCheckstyleConfig",
				ExtractCheckstyleConfig.class,
				task -> task.getOutputFile().set(configFile)
		);

		final var ext = project.getExtensions().getByType(CheckstyleExtension.class);
		ext.getConfigDirectory().set(project.file("config/checkstyle"));
		ext.setConfigFile(configFile);
		ext.setToolVersion(CHECKSTYLE_VERSION);

		project.afterEvaluate(p -> ext.getConfigProperties().put("minSdk", resolveMinSdk(p)));

		final var checkstyleConfig = project.getConfigurations().getByName("checkstyle");
		addDependencies(project, checkstyleConfig);

		registerTasks(project, extractTask.getName());
	}
}