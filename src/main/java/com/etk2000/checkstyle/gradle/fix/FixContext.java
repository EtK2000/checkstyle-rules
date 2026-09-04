package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;

import java.util.List;
import java.util.function.BiPredicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Per-fix context exposed to fixers that need information beyond the
 * {@code (lines, lineIndex, column)} the {@link CheckstyleFixer} interface
 * provides. The file path is set by {@code applyFixes} before invoking any
 * fixer for a file; the violation is set before each individual fixer call so
 * a fixer can read the check's own decision (the message key and resolved
 * text) instead of re-deriving it. Both are cleared after the file's fix loop. A run also sets which
 * events its configured filters drop in the file, for a fixer whose rewrite answers to another check.
 */
final class FixContext {
	// how many fixes applyFixes has applied to the buffer it hands every fixer of this file, so an
	// unchanged buffer can be told apart from an edited one without comparing every line
	private static final ThreadLocal<Integer> BUFFER_VERSION = new ThreadLocal<>();
	private static final ThreadLocal<String> CURRENT_FILE_PATH = new ThreadLocal<>();
	private static final ThreadLocal<Violation> CURRENT_VIOLATION = new ThreadLocal<>();
	// the buffer applyFixes edits in place and hands every fixer, as distinct from a candidate a
	// fixer builds to test a rewrite, and that buffer as it stood before the pass
	private static final ThreadLocal<List<String>> LIVE_BUFFER = new ThreadLocal<>();
	private static final ThreadLocal<List<String>> LIVE_SOURCE = new ThreadLocal<>();
	// whether the run's configured filters drop an event of a check at a 1-based line of the file
	private static final ThreadLocal<BiPredicate<Class<?>, Integer>> SUPPRESSED = new ThreadLocal<>();

	static void clearBufferVersion() {
		BUFFER_VERSION.remove();
	}

	static void clearFilePath() {
		CURRENT_FILE_PATH.remove();
	}

	static void clearLiveBuffer() {
		LIVE_BUFFER.remove();
		LIVE_SOURCE.remove();
	}

	static void clearSuppressed() {
		SUPPRESSED.remove();
	}

	static void clearViolation() {
		CURRENT_VIOLATION.remove();
	}

	/** The {@link #BUFFER_VERSION} of the buffer the current fixer call was handed, or null outside {@code applyFixes}. */
	@CheckReturnValue
	@Nullable
	static Integer getBufferVersion() {
		return BUFFER_VERSION.get();
	}

	@CheckReturnValue
	@Nullable
	static String getFilePath() {
		return CURRENT_FILE_PATH.get();
	}

	@CheckReturnValue
	@Nullable
	static String getViolationKey() {
		final var violation = CURRENT_VIOLATION.get();
		return violation == null ? null : violation.getKey();
	}

	@CheckReturnValue
	@Nullable
	static String getViolationMessage() {
		final var violation = CURRENT_VIOLATION.get();
		return violation == null ? null : violation.getViolation();
	}

	@CheckReturnValue
	@Nullable
	static SeverityLevel getViolationSeverity() {
		final var violation = CURRENT_VIOLATION.get();
		return violation == null ? null : violation.getSeverityLevel();
	}

	/**
	 * Whether the filters the run is configured with (a {@code suppressions.xml}) drop an event of
	 * {@code check} at 1-based {@code line} of the file being fixed, so the run never acts on that
	 * check there; false when no run set them.
	 */
	@CheckReturnValue
	static boolean isSuppressed(@Nonnull Class<?> check, int line) {
		final var suppressed = SUPPRESSED.get();
		return suppressed != null && suppressed.test(check, line);
	}

	/**
	 * What {@code lines} held before the pass when it is the very list {@code applyFixes} is editing,
	 * else null, a copy of it included.
	 */
	@CheckReturnValue
	@Nullable
	static List<String> liveSourceOf(@Nonnull List<String> lines) {
		return LIVE_BUFFER.get() == lines ? LIVE_SOURCE.get() : null;
	}

	static void setBufferVersion(int version) {
		BUFFER_VERSION.set(version);
	}

	static void setFilePath(@Nonnull String path) {
		CURRENT_FILE_PATH.set(path);
	}

	static void setLiveBuffer(@Nonnull List<String> live, @Nonnull List<String> source) {
		LIVE_BUFFER.set(live);
		LIVE_SOURCE.set(source);
	}

	static void setSuppressed(@Nonnull BiPredicate<Class<?>, Integer> suppressed) {
		SUPPRESSED.set(suppressed);
	}

	static void setViolation(@Nonnull Violation violation) {
		CURRENT_VIOLATION.set(violation);
	}
}