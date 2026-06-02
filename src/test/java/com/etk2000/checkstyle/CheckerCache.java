package com.etk2000.checkstyle;

import com.etk2000.checkstyle.gradle.fix.ViolationCollectingListener;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Reuses configured {@link Checker} instances across test calls, keyed by the
 * configuration's content (each module's name, properties, and custom
 * messages, plus the child tree). Configuring a Checker reflectively
 * instantiates the whole module tree, so for a suite that runs the same
 * configuration thousands of times (e.g. {@code FullPipelineRegressionTest})
 * rebuilding it per call dominates the runtime. Identical configurations map
 * to the same key and share a Checker; a different configuration (e.g. a
 * minimal import-cleanup ruleset) gets its own.
 *
 * <p>This only changes Checker object lifecycle, not behaviour: callers still
 * pass the exact same {@link Configuration} they would have built anyway, and
 * each {@code process} runs with a fresh {@link ViolationCollectingListener}
 * that is removed afterwards, so a reused Checker reports exactly what a
 * freshly configured one would.
 *
 * <p>Thread-safety: the cache is a {@link ThreadLocal}, so a Checker is never
 * shared between threads. This keeps it correct under JUnit parallel execution
 * (each thread configures and reuses its own Checkers) with no locking and no
 * cross-thread listener interleaving, and it is trivially correct under the
 * current single-threaded execution. Checkers are not destroyed between runs
 * (a configured Checker is reused for the life of the test JVM).
 */
public final class CheckerCache {
	private static final ThreadLocal<Map<String, Checker>> CHECKERS = ThreadLocal.withInitial(HashMap::new);

	private static void appendKey(@Nonnull StringBuilder sb, @Nonnull Configuration config) {
		sb.append(config.getName()).append('{');
		final var props = new TreeMap<String, String>();
		for (var name : config.getPropertyNames()) {
			try {
				props.put(name, config.getProperty(name));
			}
			catch (CheckstyleException e) {
				throw new IllegalStateException("Cannot read property '" + name + "' of module " + config.getName(), e);
			}
		}
		for (var prop : props.entrySet())
			sb.append(prop.getKey()).append('=').append(prop.getValue()).append(';');
		sb.append("}msg").append(new TreeMap<>(config.getMessages())).append('[');
		for (var child : config.getChildren())
			appendKey(sb, child);
		sb.append(']');
	}

	@CheckReturnValue
	@Nonnull
	private static Checker configure(@Nonnull Configuration config, @Nonnull ClassLoader moduleClassLoader) {
		final var checker = new Checker();
		try {
			checker.setModuleClassLoader(moduleClassLoader);
			checker.configure(config);
		}
		catch (CheckstyleException e) {
			throw new IllegalStateException("Failed to configure Checker for " + config.getName(), e);
		}
		return checker;
	}

	@CheckReturnValue
	@Nonnull
	private static String key(@Nonnull Configuration config) {
		final var sb = new StringBuilder();
		appendKey(sb, config);
		return sb.toString();
	}

	@CheckReturnValue
	@Nonnull
	public static List<AuditEvent> process(
			@Nonnull Configuration config,
			@Nonnull ClassLoader moduleClassLoader,
			@Nonnull File file
	) throws CheckstyleException {
		return process(config, moduleClassLoader, List.of(file));
	}

	@CheckReturnValue
	@Nonnull
	public static List<AuditEvent> process(
			@Nonnull Configuration config,
			@Nonnull ClassLoader moduleClassLoader,
			@Nonnull List<File> files
	) throws CheckstyleException {
		final var checker = CHECKERS.get().computeIfAbsent(key(config), k -> configure(config, moduleClassLoader));
		final var listener = new ViolationCollectingListener();
		checker.addListener(listener);
		try {
			checker.process(files);
		}
		finally {
			checker.removeListener(listener);
		}
		return listener.getViolations();
	}

	private CheckerCache() {
	}
}