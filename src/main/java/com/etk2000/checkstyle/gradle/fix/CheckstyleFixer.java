package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;

interface CheckstyleFixer {
	/**
	 * Returns the fix for the violation at {@code (lineIndex, column)}, or a
	 * {@link SkipResult} with a non-null reason when the fix cannot be applied.
	 * {@code column} is an index into the line's chars, converted once by the pipeline
	 * ({@link CheckstyleFixAction#eventCharIndex}), so it indexes the line directly; a fixer
	 * matching it against an AST position, which counts code points, converts it back with
	 * {@code LineText.columnOfCharIndex}.
	 * Must never return {@code null}; every can't-fix branch must produce a
	 * {@code SkipResult} so {@code // skip-reason:} directives in test slices
	 * can verify the exact reason text.
	 */
	@Nonnull
	FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column);
}