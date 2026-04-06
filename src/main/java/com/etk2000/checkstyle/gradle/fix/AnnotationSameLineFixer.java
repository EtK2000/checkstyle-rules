package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationSameLineFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);
		final var stripped = line.stripLeading();

		// case 1: annotation on its own line - join with subsequent lines
		if (AnnotationFixerUtil.isAnnotationOnlyLine(stripped)) {
			final var annotationTexts = new ArrayList<String>();
			var idx = lineIndex;
			while (idx < lines.size()) {
				final var s = lines.get(idx).strip();
				if (!AnnotationFixerUtil.isAnnotationOnlyLine(s))
					break;
				final var parsed = AnnotationFixerUtil.parseAnnotations(s);
				annotationTexts.addAll(parsed.annotations());
				++idx;
			}

			if (idx >= lines.size())
				return null;

			AnnotationFixerUtil.sortAnnotations(annotationTexts);
			final var declLine = lines.get(idx);
			final var declStripped = declLine.stripLeading();
			final var declIndent = declLine.substring(0, declLine.length() - declStripped.length());
			final var joined = declIndent + String.join(" ", annotationTexts) + " " + declStripped;
			return new FixResult(lineIndex, idx, List.of(joined));
		}

		// case 2: inline annotations out of alphabetical order - reorder on the same line
		// find the start of the annotation group by scanning backward from column
		var groupStart = column;
		while (groupStart > 0
				&& line.charAt(groupStart - 1) != '('
				&& line.charAt(groupStart - 1) != ',')
			--groupStart;

		// skip whitespace after the boundary
		while (groupStart < line.length() && Character.isWhitespace(line.charAt(groupStart)))
			++groupStart;

		final var prefix = line.substring(0, groupStart);
		final var parsed = AnnotationFixerUtil.parseAnnotations(line.substring(groupStart));
		if (parsed.annotations().size() < 2)
			return null;

		final var sorted = new ArrayList<>(parsed.annotations());
		AnnotationFixerUtil.sortAnnotations(sorted);
		if (sorted.equals(parsed.annotations()))
			return null;

		var result = prefix + String.join(" ", sorted);
		if (!parsed.remaining().isEmpty())
			result += " " + parsed.remaining();
		return new FixResult(lineIndex, lineIndex, List.of(result));
	}
}