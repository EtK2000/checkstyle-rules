package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.etk2000.checkstyle.gradle.CheckstylePluginTest;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.ResourceLock;

import java.io.File;

import javax.annotation.Nonnull;

/** What {@link CheckstyleFixTask} hands its worker, read off the parameters it fills. */
@ResourceLock(CheckstylePluginTest.PROJECT_BUILDER)
public class CheckstyleFixTaskTest {
	@TempDir
	File projectDir;

	@Nonnull
	private Project project() {
		return ProjectBuilder.builder().withProjectDir(projectDir).build();
	}

	@Test
	public void testPopulateLeavesTheConfigDirectoryUnsetWhenTheTaskHasNone() {
		final var project = project();
		final var task = project.getTasks().register("fix", CheckstyleFixTask.class).get();
		final var params = project.getObjects().newInstance(CheckstyleFixAction.Params.class);
		task.populate(params);
		assertFalse(params.getConfigDirectory().isPresent());
	}

	@Test
	public void testPopulatePassesTheConfigDirectoryTheTaskHas() {
		final var project = project();
		final var task = project.getTasks().register("fix", CheckstyleFixTask.class).get();
		final var configDirectory = new File(projectDir, "config/checkstyle");
		task.getConfigDirectory().set(configDirectory);
		final var params = project.getObjects().newInstance(CheckstyleFixAction.Params.class);
		task.populate(params);
		assertEquals(configDirectory, params.getConfigDirectory().get().getAsFile());
	}
}