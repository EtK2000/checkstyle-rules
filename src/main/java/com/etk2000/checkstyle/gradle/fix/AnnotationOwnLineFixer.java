package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationOwnLineFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static boolean isInsideOrStartsComment(@Nonnull String line, boolean inBlockComment) {
		if (inBlockComment)
			return true;
		final var stripped = line.stripLeading();
		return stripped.startsWith("//") || stripped.startsWith("/*") || stripped.startsWith("*");
	}

	@CheckReturnValue
	private static boolean startsBlockComment(@Nonnull String line) {
		final var stripped = line.stripLeading();
		return stripped.startsWith("/*") && !stripped.contains("*/");
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);

		// case 0: blank line inside a multi-line annotation - remove it
		if (line.isBlank()) {
			var lastBlank = lineIndex;
			while (lastBlank + 1 < lines.size() && lines.get(lastBlank + 1).isBlank())
				++lastBlank;
			return new FixResult(lineIndex, lastBlank, List.of());
		}

		final var stripped = line.stripLeading();
		final var indent = line.substring(0, line.length() - stripped.length());

		// case 1: multiple annotations or annotation + declaration on same line - split and sort
		final var parsed = AnnotationFixerUtil.parseAnnotations(stripped);
		if (parsed.annotations().size() > 1
				|| (!parsed.annotations().isEmpty() && !parsed.remaining().isEmpty())) {
			AnnotationFixerUtil.sortAnnotations(parsed.annotations());
			final var replacement = new ArrayList<String>();
			for (var annotation : parsed.annotations())
				replacement.add(indent + annotation);
			if (!parsed.remaining().isEmpty())
				replacement.add(indent + parsed.remaining());
			return new FixResult(lineIndex, lineIndex, replacement);
		}

		// single annotation on its own line

		// case 2: blank line below (possibly after comment lines) - remove blank lines
		{
			var scan = lineIndex + 1;
			var inBlock = false;
			while (scan < lines.size() && isInsideOrStartsComment(lines.get(scan), inBlock)) {
				if (inBlock) {
					if (lines.get(scan).stripLeading().contains("*/"))
						inBlock = false;
				}
				else if (startsBlockComment(lines.get(scan)))
					inBlock = true;
				++scan;
			}
			if (scan < lines.size() && lines.get(scan).isBlank()) {
				var lastBlank = scan;
				while (lastBlank + 1 < lines.size() && lines.get(lastBlank + 1).isBlank())
					++lastBlank;
				return new FixResult(scan, lastBlank, List.of());
			}
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
		for (var annotation : sorted)
			replacement.add(indent + annotation);
		return new FixResult(start, end, replacement);
	}
}