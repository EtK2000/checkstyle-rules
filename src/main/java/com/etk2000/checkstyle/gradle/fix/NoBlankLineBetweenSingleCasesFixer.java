package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class NoBlankLineBetweenSingleCasesFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		var firstBlank = lineIndex - 1;
		while (firstBlank >= 0 && lines.get(firstBlank).isBlank())
			--firstBlank;
		++firstBlank;
		if (firstBlank >= lineIndex)
			return null;
		return new FixResult(firstBlank, lineIndex - 1, List.of());
	}
}