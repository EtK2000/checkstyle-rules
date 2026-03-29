package com.etk2000.checkstyle.gradle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;

public class CheckstylePluginMinSdkTest {
	@Rule
	public final TemporaryFolder tempDir = new TemporaryFolder();

	@Test
	public void testManifestMinSdkParsed() throws Exception {
		final var projectDir = tempDir.newFolder("project");
		final var manifestDir = new File(projectDir, "src/main");
		manifestDir.mkdirs();

		Files.writeString(
				new File(manifestDir, "AndroidManifest.xml").toPath(),
				"<manifest><uses-sdk android:minSdkVersion=\"28\" /></manifest>"
		);

		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertEquals("28", CheckstylePlugin.readMinSdkFromManifest(project));
	}

	@Test
	public void testManifestMinSdkWithSpaces() throws Exception {
		final var projectDir = tempDir.newFolder("project2");
		final var manifestDir = new File(projectDir, "src/main");
		manifestDir.mkdirs();

		Files.writeString(
				new File(manifestDir, "AndroidManifest.xml").toPath(),
				"<manifest>\n\t<uses-sdk android:minSdkVersion = \"34\" />\n</manifest>"
		);

		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertEquals("34", CheckstylePlugin.readMinSdkFromManifest(project));
	}

	@Test
	public void testManifestWithoutMinSdkReturnsNull() throws Exception {
		final var projectDir = tempDir.newFolder("project4");
		final var manifestDir = new File(projectDir, "src/main");
		manifestDir.mkdirs();

		Files.writeString(
				new File(manifestDir, "AndroidManifest.xml").toPath(),
				"<manifest><application /></manifest>"
		);

		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertNull(CheckstylePlugin.readMinSdkFromManifest(project));
	}

	@Test
	public void testNoManifestReturnsNull() throws Exception {
		final var projectDir = tempDir.newFolder("project3");
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertNull(CheckstylePlugin.readMinSdkFromManifest(project));
	}

	@Test
	public void testResolveMinSdkDefaultsToMaxForNonAndroid() throws Exception {
		final var projectDir = tempDir.newFolder("project6");
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertEquals(String.valueOf(Integer.MAX_VALUE), CheckstylePlugin.resolveMinSdk(project));
	}

	@Test
	public void testResolveMinSdkFallsBackToManifest() throws Exception {
		final var projectDir = tempDir.newFolder("project5");
		final var manifestDir = new File(projectDir, "src/main");
		manifestDir.mkdirs();

		Files.writeString(
				new File(manifestDir, "AndroidManifest.xml").toPath(),
				"<manifest><uses-sdk android:minSdkVersion=\"21\" /></manifest>"
		);

		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertEquals("21", CheckstylePlugin.resolveMinSdk(project));
	}
}