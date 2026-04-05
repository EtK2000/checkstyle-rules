package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferPrefixIncrementFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		// Case 1: column points to the identifier (ident++ / ident--)
		if (Character.isJavaIdentifierStart(line.charAt(column))) {
			var identEnd = column;
			while (identEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(identEnd)))
				++identEnd;
			if (identEnd + 1 < line.length()) {
				final var op = line.substring(identEnd, identEnd + 2);
				if ("++".equals(op) || "--".equals(op)) {
					final var ident = line.substring(column, identEnd);
					final var fixed = line.substring(0, column) + op + ident + line.substring(identEnd + 2);
					return new FixResult(lineIndex, lineIndex, List.of(fixed));
				}
			}
		}

		// Case 2: column points to the ++ or -- operator
		if (column + 1 < line.length()) {
			final var op = line.substring(column, column + 2);
			if ("++".equals(op) || "--".equals(op)) {
				var identStart = column - 1;
				while (identStart >= 0 && Character.isJavaIdentifierPart(line.charAt(identStart)))
					--identStart;
				++identStart;
				if (identStart < column && Character.isJavaIdentifierStart(line.charAt(identStart))) {
					final var ident = line.substring(identStart, column);
					final var fixed = line.substring(0, identStart) + op + ident + line.substring(column + 2);
					return new FixResult(lineIndex, lineIndex, List.of(fixed));
				}
			}
		}

		return null;
	}
}