package com.etk2000.checkstyle.gradle.fix;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;
import org.gradle.workers.WorkerExecutor;

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
		).submit(
				CheckstyleFixAction.class,
				params -> params.getSource().set(getSource())
		);
	}

	@Classpath
	public abstract ConfigurableFileCollection getCheckstyleClasspath();

	@Internal
	public abstract DirectoryProperty getSource();

	@Inject
	public abstract WorkerExecutor getWorkerExecutor();
}