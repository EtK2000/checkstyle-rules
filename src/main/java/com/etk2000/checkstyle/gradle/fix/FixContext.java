package com.etk2000.checkstyle.gradle.fix;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Per-file context exposed to fixers that need information beyond the
 * {@code (lines, lineIndex, column)} the {@link CheckstyleFixer} interface
 * provides. The path is set by {@code applyFixes} before invoking any fixer
 * for a file and cleared after the loop.
 */
final class FixContext {
	private static final ThreadLocal<String> CURRENT_FILE_PATH = new ThreadLocal<>();

	static void clearFilePath() {
		CURRENT_FILE_PATH.remove();
	}

	@CheckReturnValue
	@Nullable
	static String getFilePath() {
		return CURRENT_FILE_PATH.get();
	}

	static void setFilePath(@Nonnull String path) {
		CURRENT_FILE_PATH.set(path);
	}
}