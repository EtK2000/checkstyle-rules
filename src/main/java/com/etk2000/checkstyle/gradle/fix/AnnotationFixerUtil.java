package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

class AnnotationFixerUtil {
	record ParseResult(@Nonnull List<String> annotations, @Nonnull String remaining) {}

	@CheckReturnValue
	@Nonnull
	static String annotationSortKey(@Nonnull String annotation) {
		final var parenIdx = annotation.indexOf('(');
		final var end = parenIdx >= 0 ? parenIdx : annotation.length();
		if (end == 0)
			return "";

		// searched within the name only: an argument's own qualified constant
		// (`@Target(ElementType.TYPE)`) otherwise holds the last '.', putting start past end
		final var start = annotation.lastIndexOf('.', end - 1) + 1;
		return annotation.substring(Math.clamp(start, 1, end), end);
	}

	@CheckReturnValue
	static boolean isAnnotationOnlyLine(@Nonnull String stripped) {
		if (stripped.isEmpty() || stripped.charAt(0) != '@')
			return false;

		var pos = 0;
		while (pos < stripped.length() && stripped.charAt(pos) == '@') {
			pos = LineText.qualifiedNameEnd(stripped, pos + 1);

			if (pos < stripped.length() && stripped.charAt(pos) == '(') {
				var depth = 1;
				++pos;
				while (pos < stripped.length() && depth > 0) {
					final var ch = stripped.charAt(pos);
					if (ch == '(')
						++depth;
					else if (ch == ')')
						--depth;
					else if (ch == '"' || ch == '\'') {
						++pos;
						while (pos < stripped.length() && stripped.charAt(pos) != ch) {
							if (stripped.charAt(pos) == '\\')
								++pos;
							++pos;
						}
					}
					if (pos < stripped.length())
						++pos;
				}
			}

			while (pos < stripped.length()
						&& (stripped.charAt(pos) == ' ' || stripped.charAt(pos) == '\t'))
				++pos;
		}
		return pos >= stripped.length();
	}

	/** Parses all annotations from a content string whose leading whitespace is already stripped. */
	@CheckReturnValue
	@Nonnull
	static ParseResult parseAnnotations(@Nonnull String content) {
		final var annotations = new ArrayList<String>();
		var pos = 0;

		while (pos < content.length() && content.charAt(pos) == '@') {
			final var start = pos;

			pos = LineText.qualifiedNameEnd(content, pos + 1);

			// `@` with no name after it is not an annotation this can hand back intact:
			// emitting it as one splits `@ Deprecated` (legal Java) onto a line of its own
			if (pos == start + 1)
				return new ParseResult(annotations, content.substring(start).stripLeading());

			if (pos < content.length() && content.charAt(pos) == '(') {
				var depth = 1;
				++pos;
				while (pos < content.length() && depth > 0) {
					final var ch = content.charAt(pos);
					if (ch == '(')
						++depth;
					else if (ch == ')')
						--depth;
					else if (ch == '"' || ch == '\'') {
						++pos;
						while (pos < content.length() && content.charAt(pos) != ch) {
							if (content.charAt(pos) == '\\')
								++pos;
							++pos;
						}
					}
					if (pos < content.length())
						++pos;
				}
				if (depth != 0)
					return new ParseResult(annotations, content.substring(start).stripLeading());
			}

			annotations.add(content.substring(start, Math.min(pos, content.length())));

			while (pos < content.length()
					&& (content.charAt(pos) == ' ' || content.charAt(pos) == '\t'))
				++pos;
		}

		final var remaining = content.substring(Math.min(pos, content.length())).stripLeading();
		return new ParseResult(annotations, remaining);
	}

	static void sortAnnotations(@Nonnull List<String> annotations) {
		annotations.sort(Comparator.comparing(AnnotationFixerUtil::annotationSortKey));
	}
}