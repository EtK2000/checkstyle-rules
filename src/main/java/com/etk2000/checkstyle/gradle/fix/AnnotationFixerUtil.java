package com.etk2000.checkstyle.gradle.fix;

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
		// extract simple name: skip @ and package prefix, stop at ( if present
		final var start = annotation.lastIndexOf('.') + 1;
		final var parenIdx = annotation.indexOf('(');
		final var end = parenIdx >= 0 ? parenIdx : annotation.length();
		return annotation.substring(Math.max(1, start), end);
	}

	@CheckReturnValue
	static boolean isAnnotationOnlyLine(@Nonnull String stripped) {
		if (stripped.isEmpty() || stripped.charAt(0) != '@')
			return false;

		var pos = 0;
		while (pos < stripped.length() && stripped.charAt(pos) == '@') {
			++pos;
			while (pos < stripped.length()
					&& (Character.isJavaIdentifierPart(stripped.charAt(pos)) || stripped.charAt(pos) == '.'))
				++pos;

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
					++pos;
				}
			}

			while (pos < stripped.length() && stripped.charAt(pos) == ' ')
				++pos;
		}
		return pos >= stripped.length();
	}

	/**
	 * Parses all annotations from a content string (leading whitespace already stripped).
	 * Returns the list of annotation strings and the remaining non-annotation content.
	 */
	@CheckReturnValue
	@Nonnull
	static ParseResult parseAnnotations(@Nonnull String content) {
		final var annotations = new ArrayList<String>();
		var pos = 0;

		while (pos < content.length() && content.charAt(pos) == '@') {
			final var start = pos;
			++pos;

			// read annotation name (possibly qualified with dots)
			while (pos < content.length()
					&& (Character.isJavaIdentifierPart(content.charAt(pos)) || content.charAt(pos) == '.'))
				++pos;

			// read params if present (balanced parens)
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
						// skip string/char literal
						++pos;
						while (pos < content.length() && content.charAt(pos) != ch) {
							if (content.charAt(pos) == '\\')
								++pos;
							++pos;
						}
					}
					++pos;
				}
			}

			annotations.add(content.substring(start, pos));

			// skip whitespace between annotations
			while (pos < content.length() && content.charAt(pos) == ' ')
				++pos;
		}

		final var remaining = content.substring(pos).stripLeading();
		return new ParseResult(annotations, remaining);
	}

	static void sortAnnotations(@Nonnull List<String> annotations) {
		annotations.sort(Comparator.comparing(AnnotationFixerUtil::annotationSortKey));
	}
}