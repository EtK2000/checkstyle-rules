package com.etk2000.checkstyle.gradle.fix;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.gradle.workers.WorkerExecutor;

import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * Thin Gradle task that delegates to {@link CheckstyleFixAction} via the
 * Worker API with classloader isolation.  This class intentionally has no
 * checkstyle API imports so Gradle can decorate it on the buildscript
 * classpath without requiring checkstyle.
 */
@DisableCachingByDefault(because = "Modifies source files in place")
public abstract class CheckstyleFixTask extends DefaultTask {
	@TaskAction
	public void fix() {
		getWorkerExecutor().classLoaderIsolation(spec ->
				spec.getClasspath().from(getCheckstyleClasspath())
		).submit(CheckstyleFixAction.class, this::populate);
	}

	@Classpath
	public abstract ConfigurableFileCollection getCheckstyleClasspath();

	/** Where the user's {@code suppressions.xml} lives, as the checkstyle tasks read it. */
	@Internal
	public abstract DirectoryProperty getConfigDirectory();

	@Internal
	public abstract Property<Boolean> getDryRun();

	@Internal
	public abstract Property<String> getDryRunTaskName();

	@Internal
	public abstract Property<Integer> getDryRunTotal();

	@Input
	public abstract Property<String> getMinSdk();

	@Internal
	public abstract DirectoryProperty getSource();

	@Internal
	public abstract DirectoryProperty getTestSource();

	@Inject
	public abstract WorkerExecutor getWorkerExecutor();

	/** Hands the worker this task's inputs, an optional one only when it is set. */
	void populate(@Nonnull CheckstyleFixAction.Params params) {
		if (getConfigDirectory().isPresent())
			params.getConfigDirectory().set(getConfigDirectory());
		params.getMinSdk().set(getMinSdk());
		params.getSource().set(getSource());
		if (getDryRun().isPresent()) {
			params.getDryRun().set(getDryRun());
			params.getDryRunTaskName().set(getDryRunTaskName());
			params.getDryRunTotal().set(getDryRunTotal());
		}
		if (getTestSource().isPresent())
			params.getTestSource().set(getTestSource());
	}
}