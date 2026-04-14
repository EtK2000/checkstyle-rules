package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class DeleteLineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		// if the deleted line (typically an orphaned import) has blank lines both above
		// and below, also delete the blank below so we don't leave a double blank
		if (lineIndex > 0 && lineIndex + 1 < lines.size()
				&& lines.get(lineIndex - 1).isEmpty()
				&& lines.get(lineIndex + 1).isEmpty())
			return new FixResult(lineIndex, lineIndex + 1, List.of());
		return new FixResult(lineIndex, lineIndex, List.of());
	}
}