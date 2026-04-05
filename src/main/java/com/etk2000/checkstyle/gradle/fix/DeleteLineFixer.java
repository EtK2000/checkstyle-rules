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
		return new FixResult(lineIndex, lineIndex, List.of());
	}
}