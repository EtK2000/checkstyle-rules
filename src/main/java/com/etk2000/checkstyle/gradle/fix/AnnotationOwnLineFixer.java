package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationOwnLineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);
		final var stripped = line.stripLeading();
		final var indent = line.substring(0, line.length() - stripped.length());

		// case 1: multiple annotations or annotation + declaration on same line - split and sort
		final var parsed = AnnotationFixerUtil.parseAnnotations(stripped);
		if (parsed.annotations().size() > 1 || !parsed.remaining().isEmpty()) {
			AnnotationFixerUtil.sortAnnotations(parsed.annotations());
			final var replacement = new ArrayList<String>();
			for (final var annotation : parsed.annotations())
				replacement.add(indent + annotation);
			if (!parsed.remaining().isEmpty())
				replacement.add(indent + parsed.remaining());
			return new FixResult(lineIndex, lineIndex, replacement);
		}

		// single annotation on its own line

		// case 2: blank line below - remove blank lines (violation is on annotation before the blank)
		if (lineIndex + 1 < lines.size() && lines.get(lineIndex + 1).isBlank()) {
			var lastBlank = lineIndex + 1;
			while (lastBlank + 1 < lines.size() && lines.get(lastBlank + 1).isBlank())
				++lastBlank;
			return new FixResult(lineIndex + 1, lastBlank, List.of());
		}

		// case 3: alphabetical order - collect block, sort, replace
		var start = lineIndex;
		while (start > 0 && AnnotationFixerUtil.isAnnotationOnlyLine(lines.get(start - 1).stripLeading()))
			--start;

		var end = lineIndex;
		while (end + 1 < lines.size() && AnnotationFixerUtil.isAnnotationOnlyLine(lines.get(end + 1).stripLeading()))
			++end;

		final var allAnnotations = new ArrayList<String>();
		for (var i = start; i <= end; ++i) {
			final var p = AnnotationFixerUtil.parseAnnotations(lines.get(i).stripLeading());
			allAnnotations.addAll(p.annotations());
		}

		final var sorted = new ArrayList<>(allAnnotations);
		AnnotationFixerUtil.sortAnnotations(sorted);

		if (sorted.equals(allAnnotations))
			return null;

		final var replacement = new ArrayList<String>();
		for (final var annotation : sorted)
			replacement.add(indent + annotation);
		return new FixResult(start, end, replacement);
	}
}