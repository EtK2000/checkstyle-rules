package com.etk2000.checkstyle.gradle.fix;

import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Parsed form of a single Java {@code import} statement: the imported name
 * ({@link #fqn}, with any trailing {@code .*} removed), whether it is a
 * {@code static} import, and whether it was a {@code .*} wildcard.
 *
 * <p>{@link #parse} operates on a line that has ALREADY been stripped of
 * comments — each fixer strips with its own comment-awareness (single-line
 * {@link LambdaCallParser#stripComment} vs. text-block-aware
 * {@link FqnResolver#stripCommentsForClassification}), so the shared parser
 * stays out of that decision. It accepts any single {@code import}/{@code import
 * static} statement terminated by {@code ;}, tolerating the whitespace the JLS
 * permits inside a qualified name ({@code import java . util . List ;}) by
 * collapsing it, and non-ASCII identifiers. A missing {@code ;}, an internal
 * {@code ;}, trailing tokens after the {@code ;}, or an empty name yields
 * {@code null}. Segment-level validity is left to callers (they split on the
 * last {@code .} and guard degenerate names themselves).
 *
 * <p>The name group is anchored to start and end with a non-whitespace
 * character ({@code \S(?:[^;]*\S)?}) so the trailing {@code \s*;} owns the
 * whitespace unambiguously; a lazy {@code [^;]+?} there would backtrack
 * quadratically on an {@code import} line that is all whitespace with no
 * {@code ;} (reachable, since some callers parse every line of a file).
 */
record ImportLine(@Nonnull String fqn, boolean staticImport, boolean wildcard) {
	private static final Pattern IMPORT_PATTERN = Pattern.compile(
			"^\\s*import\\s+(static\\s+)?(\\S(?:[^;]*\\S)?)\\s*;\\s*$"
	);
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	@CheckReturnValue
	@Nullable
	static ImportLine parse(@Nonnull String strippedLine) {
		final var matcher = IMPORT_PATTERN.matcher(strippedLine);
		if (!matcher.matches())
			return null;
		final var staticImport = matcher.group(1) != null;
		var fqn = WHITESPACE.matcher(matcher.group(2)).replaceAll("");
		final var wildcard = fqn.endsWith(".*");
		if (wildcard)
			fqn = fqn.substring(0, fqn.length() - 2);
		return new ImportLine(fqn, staticImport, wildcard);
	}
}