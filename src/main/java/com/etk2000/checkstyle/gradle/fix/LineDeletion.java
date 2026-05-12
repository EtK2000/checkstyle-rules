package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shared helper for fixers that delete a range of lines. When the deletion
 * would leave a double blank line (blanks both above the start and below the
 * end), one of the adjacent blanks is dropped with the range so the resulting
 * file collapses cleanly.
 */
final class LineDeletion {
	@CheckReturnValue
	@Nullable
	static FixResult deleteRange(@Nonnull List<String> lines, int startLine, int endLine) {
		return deleteRange(lines, startLine, endLine, Set.of());
	}

	@CheckReturnValue
	@Nullable
	static FixResult deleteRange(
			@Nonnull List<String> lines,
			int startLine,
			int endLine,
			@Nonnull Set<String> importsToAdd
	) {
		if (startLine < 0 || endLine < startLine || endLine >= lines.size())
			return null;
		if (startLine > 0 && endLine + 1 < lines.size()
				&& lines.get(startLine - 1).isBlank()
				&& lines.get(endLine + 1).isBlank())
			return new FixResult(startLine, endLine + 1, List.of(), importsToAdd);
		return new FixResult(startLine, endLine, List.of(), importsToAdd);
	}
}