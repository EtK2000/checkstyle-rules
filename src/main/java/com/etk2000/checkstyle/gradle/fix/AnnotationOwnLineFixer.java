package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationOwnLineFixer implements CheckstyleFixer {
	private record EmbeddedResult(@Nonnull List<String> annotations, @Nonnull String prefix,
			@Nonnull String remaining) {}

	/**
	 * Extracts annotations that appear after modifier keywords (e.g. {@code final @A var x}).
	 * Returns {@code null} if no embedded annotations are found.
	 */
	@CheckReturnValue
	@Nullable
	private static EmbeddedResult extractEmbeddedAnnotations(@Nonnull String content) {
		var pos = 0;

		while (pos < content.length()) {
			while (pos < content.length()
					&& (content.charAt(pos) == ' ' || content.charAt(pos) == '\t'))
				++pos;

			if (pos >= content.length())
				return null;

			if (content.charAt(pos) == '@') {
				if (pos == 0)
					return null;

				final var prefix = content.substring(0, pos).stripTrailing();
				final var parsed = AnnotationFixerUtil.parseAnnotations(content.substring(pos));
				if (parsed.annotations().isEmpty())
					return null;

				return new EmbeddedResult(parsed.annotations(), prefix, parsed.remaining());
			}

			if (!Character.isJavaIdentifierStart(content.charAt(pos)))
				return null;

			while (pos < content.length() && Character.isJavaIdentifierPart(content.charAt(pos)))
				++pos;
		}

		return null;
	}

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

		// case 1: annotations on same line as declaration - split and sort
		// handles leading annotations (@A @B void f), embedded annotations (final @A var x),
		// and mixed (@ C final @A var x)
		final var parsed = AnnotationFixerUtil.parseAnnotations(stripped);
		final var allAnnotations = new ArrayList<>(parsed.annotations());
		var declarationPart = parsed.remaining();

		while (!declarationPart.isEmpty()) {
			final var embedded = extractEmbeddedAnnotations(declarationPart);
			if (embedded == null)
				break;
			allAnnotations.addAll(embedded.annotations());
			declarationPart = embedded.remaining().isEmpty()
					? embedded.prefix()
					: embedded.prefix() + " " + embedded.remaining();
		}

		if (allAnnotations.size() > 1
				|| (!allAnnotations.isEmpty() && !declarationPart.isEmpty())) {
			AnnotationFixerUtil.sortAnnotations(allAnnotations);
			final var replacement = new ArrayList<String>();
			for (var annotation : allAnnotations)
				replacement.add(indent + annotation);
			if (!declarationPart.isEmpty())
				replacement.add(indent + declarationPart);
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

		final var blockAnnotations = new ArrayList<String>();
		for (var i = start; i <= end; ++i) {
			final var p = AnnotationFixerUtil.parseAnnotations(lines.get(i).stripLeading());
			blockAnnotations.addAll(p.annotations());
		}

		final var sorted = new ArrayList<>(blockAnnotations);
		AnnotationFixerUtil.sortAnnotations(sorted);

		if (sorted.equals(blockAnnotations))
			return null;

		final var replacement = new ArrayList<String>();
		for (var annotation : sorted)
			replacement.add(indent + annotation);
		return new FixResult(start, end, replacement);
	}
}