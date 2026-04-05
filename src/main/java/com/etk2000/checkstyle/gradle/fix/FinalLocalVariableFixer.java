package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FinalLocalVariableFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// find end of leading whitespace (indentation)
		var insertPos = 0;
		while (insertPos < line.length() && Character.isWhitespace(line.charAt(insertPos)))
			++insertPos;

		// guard: don't insert if final is already present
		if (line.startsWith("final ", insertPos))
			return null;

		final var fixed = line.substring(0, insertPos) + "final " + line.substring(insertPos);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}