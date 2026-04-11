package com.etk2000.checkstyle.gradle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

public class CheckstylePluginMinSdkTest {
	@TempDir
	Path tempDir;

	@Test
	public void testManifestMinSdkParsed() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("project")).toFile();
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
		final var projectDir = Files.createDirectory(tempDir.resolve("project2")).toFile();
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
		final var projectDir = Files.createDirectory(tempDir.resolve("project4")).toFile();
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
		final var projectDir = Files.createDirectory(tempDir.resolve("project3")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertNull(CheckstylePlugin.readMinSdkFromManifest(project));
	}

	@Test
	public void testResolveMinSdkDefaultsToMaxForNonAndroid() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("project6")).toFile();
		final var project = ProjectBuilder.builder().withProjectDir(projectDir).build();
		assertEquals(String.valueOf(Integer.MAX_VALUE), CheckstylePlugin.resolveMinSdk(project));
	}

	@Test
	public void testResolveMinSdkFallsBackToManifest() throws Exception {
		final var projectDir = Files.createDirectory(tempDir.resolve("project5")).toFile();
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