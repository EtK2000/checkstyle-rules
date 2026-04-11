package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantAnnotationSyntaxFixer implements CheckstyleFixer {
	private static final Pattern EMPTY_PARENS = Pattern.compile("@\\w[\\w.]*\\(\\)");
	private static final Pattern EXPLICIT_VALUE = Pattern.compile("\\(\\s*value\\s*=\\s*");
	private static final Pattern EXPLICIT_VALUE_LINE = Pattern.compile("\\s*value\\s*=\\s*");

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);

		// rule 1: single-line empty parens @A() -> @A
		final var emptyMatcher = EMPTY_PARENS.matcher(line);
		if (emptyMatcher.find(column)) {
			final var fixed = line.substring(0, emptyMatcher.end() - 2) + line.substring(emptyMatcher.end());
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}

		// rule 1: multiline empty parens @A(\n) -> @A
		final var trimmed = line.stripTrailing();
		if (trimmed.endsWith("(") && trimmed.contains("@")) {
			for (var i = lineIndex + 1; i < lines.size(); ++i) {
				final var next = lines.get(i).stripLeading();
				if (next.startsWith(")")) {
					final var fixed = trimmed.substring(0, trimmed.length() - 1).stripTrailing();
					final var afterParen = next.substring(1).stripLeading();
					if (afterParen.isEmpty())
						return new FixResult(lineIndex, i, List.of(fixed));
					return new FixResult(lineIndex, i, List.of(fixed + " " + afterParen));
				}
				if (!next.isEmpty())
					break;
			}
		}

		// rule 2: single-line explicit value key @A(value = x) -> @A(x)
		final var valueMatcher = EXPLICIT_VALUE.matcher(line);
		if (valueMatcher.find(column)) {
			final var fixed = line.substring(0, valueMatcher.start()) + "(" + line.substring(valueMatcher.end());
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}

		// rule 2: multiline explicit value key - find and fix the value= line
		for (var i = lineIndex + 1; i < lines.size(); ++i) {
			final var nextLine = lines.get(i);
			final var valueLineMatcher = EXPLICIT_VALUE_LINE.matcher(nextLine);
			if (valueLineMatcher.lookingAt()) {
				final var indent = nextLine.substring(0, nextLine.length() - nextLine.stripLeading().length());
				final var fixed = indent + nextLine.substring(valueLineMatcher.end());
				return new FixResult(i, i, List.of(fixed));
			}
			if (!nextLine.isBlank())
				break;
		}

		return null;
	}
}