package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.format.SpanReformat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AnnotationSameLineFixer implements CheckstyleFixer {
	/**
	 * Index on {@code mask} where the annotation group containing {@code column}
	 * starts: just past the {@code (} or {@code ,} that separates it from what
	 * precedes it, with any leading gap skipped.
	 *
	 * <p>The scan is depth-aware, so an annotation's own argument list
	 * ({@code @B(1) @C @A}) is stepped over rather than mistaken for the enclosing
	 * parameter paren, and it runs on the mask, so a {@code (} or {@code ,} inside
	 * an argument's string literal ({@code @B("a, @Y @X") @C @A}) is not a
	 * separator either. Reading either as the group's start put the rewrite inside
	 * the literal, sorting the text found there and leaving the annotations the
	 * check reported untouched.
	 */
	@CheckReturnValue
	private static int groupStartOn(@Nonnull String mask, int column) {
		var start = column;
		var depth = 0;
		while (start > 0) {
			final var ch = mask.charAt(start - 1);
			if (ch == ')')
				++depth;
			else if (ch == '(') {
				if (depth == 0)
					break;
				--depth;
			}
			else if (ch == ',' && depth == 0)
				break;
			--start;
		}
		while (start < mask.length() && Character.isWhitespace(mask.charAt(start)))
			++start;
		return start;
	}

	/**
	 * Line {@code lineIndex} with string, char and comment content blanked,
	 * indexable at the same columns as the original.
	 */
	@CheckReturnValue
	@Nonnull
	private static String maskedLine(@Nonnull List<String> lines, int lineIndex) {
		return JavaLineScanner.stripCommentsAndStrings(lines.get(lineIndex), SpanReformat.lexerStateAt(lines, lineIndex));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var stripped = line.stripLeading();

		if (AnnotationFixerUtil.isAnnotationOnlyLine(stripped)) {
			final var annotationTexts = new ArrayList<String>();
			var idx = lineIndex;
			while (idx < lines.size()) {
				final var s = lines.get(idx).strip();
				if (!AnnotationFixerUtil.isAnnotationOnlyLine(s))
					break;
				final var parsed = AnnotationFixerUtil.parseAnnotations(s);
				if (parsed.annotations().isEmpty())
					return null;
				annotationTexts.addAll(parsed.annotations());
				++idx;
			}

			if (idx >= lines.size())
				return null;

			if (annotationTexts.isEmpty())
				return null;

			AnnotationFixerUtil.sortAnnotations(annotationTexts);
			final var declLine = lines.get(idx);
			final var declStripped = declLine.stripLeading();
			final var declIndent = declLine.substring(0, declLine.length() - declStripped.length());
			final var joined = declIndent + String.join(" ", annotationTexts) + " " + declStripped;
			return new FixResult(lineIndex, idx, List.of(joined));
		}

		final var groupStart = groupStartOn(maskedLine(lines, lineIndex), column);

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