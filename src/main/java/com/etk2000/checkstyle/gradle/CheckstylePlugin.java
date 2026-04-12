package com.etk2000.checkstyle.gradle;

import com.etk2000.checkstyle.gradle.fix.CheckstyleFixTask;

import org.gradle.api.DefaultTask;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

public class CheckstylePlugin implements Plugin<Project> {
	@DisableCachingByDefault(because = "Extracts a static resource from the plugin JAR; no trackable inputs to cache against")
	public abstract static class ExtractCheckstyleConfig extends DefaultTask {
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
	private static final Pattern XML_ATTR_MESSAGE = Pattern.compile("message=\"([^\"]+)\"");
	private static final Pattern XML_ATTR_SEVERITY = Pattern.compile("severity=\"([^\"]+)\"");
	private static final Pattern XML_ATTR_SOURCE = Pattern.compile("source=\"([^\"]+)\"");
	private static final Pattern XML_ERROR = Pattern.compile("<error\\b[^>]*/>");
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

	/**
	 * Counts total and fixable violations in a Checkstyle XML report file.
	 * Returns {total, fixable}. TreeWalker violations are matched by source
	 * name; regexp-based violations are matched by message (since their XML
	 * source is the generic RegexpMultiline/RegexpSingleline class name).
	 */
	@Nonnull
	@VisibleForTesting
	static int[] countViolations(
			@Nonnull File xmlReport,
			@Nonnull Set<String> fixableNames,
			@Nonnull Set<String> fixableMessages
	) {
		if (!xmlReport.exists())
			return new int[]{0, 0};
		try {
			final var content = Files.readString(xmlReport.toPath());
			final var errorMatcher = XML_ERROR.matcher(content);
			var total = 0;
			var fixable = 0;
			while (errorMatcher.find()) {
				final var element = errorMatcher.group();
				++total;

				// the fixer only fixes error-severity violations, skip warnings
				final var severityMatcher = XML_ATTR_SEVERITY.matcher(element);
				if (severityMatcher.find() && !"error".equals(severityMatcher.group(1)))
					continue;

				final var sourceMatcher = XML_ATTR_SOURCE.matcher(element);
				if (sourceMatcher.find() && fixableNames.contains(sourceMatcher.group(1)))
					++fixable;
				else {
					final var messageMatcher = XML_ATTR_MESSAGE.matcher(element);
					if (messageMatcher.find() && fixableMessages.contains(messageMatcher.group(1)))
						++fixable;
				}
			}
			return new int[]{total, fixable};
		}
		catch (IOException e) {
			return new int[]{0, 0};
		}
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

	private static void registerTasks(
			@Nonnull Project project,
			@Nonnull String extractTaskName,
			@Nonnull Configuration checkstyleConfig
	) {
		project.getTasks().register(
				"checkstyleFix",
				CheckstyleFixTask.class,
				task -> {
					task.setDescription("Auto-fix simple checkstyle violations in main sources.");
					task.setGroup("verification");
					task.getCheckstyleClasspath().from(checkstyleConfig);
					task.getMinSdk().set(project.provider(() -> resolveMinSdk(project)));
					task.getSource().set(project.file("src/main/java"));
				}
		);

		final var testDir = project.file("src/test/java");
		project.getTasks().register(
				"checkstyleFixAll",
				DefaultTask.class,
				task -> {
					task.dependsOn("checkstyleFix", "checkstyleFixTest");
					task.setDescription("Auto-fix simple checkstyle violations in all sources.");
					task.setGroup("verification");
				}
		);

		project.getTasks().register(
				"checkstyleFixTest",
				CheckstyleFixTask.class,
				task -> {
					task.setDescription("Auto-fix simple checkstyle violations in test sources.");
					task.setGroup("verification");
					task.getCheckstyleClasspath().from(checkstyleConfig);
					task.getMinSdk().set(project.provider(() -> resolveMinSdk(project)));
					task.onlyIf(t -> testDir.exists());
					task.getSource().set(testDir);
				}
		);

		// hint task: after checkstyle runs, show how many violations are auto-fixable
		final var reportsDir = new File(project.getLayout().getBuildDirectory().getAsFile().get(), "reports/checkstyle");
		project.getTasks().register(
				"checkstyleFixHint",
				DefaultTask.class,
				task -> {
					task.mustRunAfter("checkstyleMain", "checkstyleTest");
					task.getOutputs().upToDateWhen(t -> false);
					task.doLast(t -> {
						final var fixableMessages = FixableCheckNames.FIXABLE_MESSAGES;
						final var fixableNames = FixableCheckNames.all();
						final var mainCounts = countViolations(new File(reportsDir, "main.xml"), fixableNames, fixableMessages);
						final var testCounts = countViolations(new File(reportsDir, "test.xml"), fixableNames, fixableMessages);
						final var fixable = mainCounts[1] + testCounts[1];
						final var total = mainCounts[0] + testCounts[0];
						if (fixable <= 0)
							return;
						final var taskName = fixable == mainCounts[1]
								? "checkstyleFix"
								: fixable == testCounts[1] ? "checkstyleFixTest" : "checkstyleFixAll";
						if (fixable == total)
							t.getLogger().lifecycle("Run ./gradlew {} to auto-fix all {} violations.", taskName, fixable);
						else
							t.getLogger().lifecycle("Run ./gradlew {} to auto-fix {} of {} violations.", taskName, fixable, total);
					});
				}
		);

		project.getTasks().register(
				"checkstyleMain",
				Checkstyle.class,
				task -> {
					task.dependsOn(extractTaskName);
					task.finalizedBy("checkstyleFixHint");
					task.include("**/*.java");
					task.setClasspath(project.files());
					task.setSource(project.fileTree("src/main/java"));
				}
		);

		project.getTasks().register(
				"checkstyleTest",
				Checkstyle.class,
				task -> {
					task.dependsOn(extractTaskName);
					task.finalizedBy("checkstyleFixHint");
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

		final var checkstyleConfig = project.getConfigurations().getByName("checkstyle");
		addDependencies(project, checkstyleConfig);

		project.afterEvaluate(p -> {
			ext.getConfigProperties().put("minSdk", resolveMinSdk(p));

			// add compile and test classpaths so reflection-based checks can resolve project types
			for (var name : new String[]{"compileClasspath", "testCompileClasspath"}) {
				final var classpath = p.getConfigurations().findByName(name);
				if (classpath != null)
					checkstyleConfig.getDependencies().add(p.getDependencies().create(p.files(classpath)));
			}
		});

		registerTasks(project, extractTask.getName(), checkstyleConfig);
	}
}