package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.Violation;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Per-fix context exposed to fixers that need information beyond the
 * {@code (lines, lineIndex, column)} the {@link CheckstyleFixer} interface
 * provides. The file path is set by {@code applyFixes} before invoking any
 * fixer for a file; the violation is set before each individual fixer call so
 * a fixer can read the check's own decision (the message key and resolved
 * text) instead of re-deriving it. Both are cleared after the file's fix loop.
 */
final class FixContext {
	private static final ThreadLocal<String> CURRENT_FILE_PATH = new ThreadLocal<>();
	private static final ThreadLocal<Violation> CURRENT_VIOLATION = new ThreadLocal<>();

	static void clearFilePath() {
		CURRENT_FILE_PATH.remove();
	}

	static void clearViolation() {
		CURRENT_VIOLATION.remove();
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

	static void setFilePath(@Nonnull String path) {
		CURRENT_FILE_PATH.set(path);
	}

	static void setViolation(@Nonnull Violation violation) {
		CURRENT_VIOLATION.set(violation);
	}
}