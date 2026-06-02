package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;

interface CheckstyleFixer {
	/**
	 * Returns the fix for the violation at {@code (lineIndex, column)}, or a
	 * {@link SkipResult} with a non-null reason when the fix cannot be applied.
	 * Must never return {@code null}; every can't-fix branch must produce a
	 * {@code SkipResult} so {@code // skip-reason:} directives in test slices
	 * can verify the exact reason text.
	 */
	@Nonnull
	FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column);
}