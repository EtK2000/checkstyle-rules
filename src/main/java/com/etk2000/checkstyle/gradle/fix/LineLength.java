package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared project-wide tab width and line-length budget. {@link #TAB_WIDTH} is the
 * single source of truth for tab-expansion in this project: it is configured on
 * Checkstyle's TreeWalker so violation columns are reported with this width, used
 * by fixers to map those columns back to character indices, and used here to
 * compute visual line length for wrap decisions (e.g. field consolidation, field
 * sorting, record component layout).
 */
public final class LineLength {
	public static final int MAX_LINE_LENGTH = 120;
	public static final int TAB_WIDTH = 4;

	@CheckReturnValue
	public static int tabExpandedLength(@Nonnull String line) {
		var len = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (line.charAt(i) == '\t')
				len += TAB_WIDTH - (len % TAB_WIDTH);
			else
				++len;
		}
		return len;
	}

	/**
	 * Renders a comma-separated declarator list as one line ({@code prefix +
	 * names joined by ", " + suffix}) when it fits within {@link #MAX_LINE_LENGTH},
	 * otherwise wraps overflowing names onto continuation lines indented by
	 * {@code contIndent} (each non-final wrapped line ends in a comma).
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> wrapFieldList(
			@Nonnull String prefix, @Nonnull List<String> names, @Nonnull String suffix, @Nonnull String contIndent
	) {
		final var result = new ArrayList<String>();
		if (names.isEmpty()) {
			result.add(prefix + suffix);
			return result;
		}
		var line = new StringBuilder(prefix);
		for (var i = 0; i < names.size(); ++i) {
			final var name = names.get(i);
			final var isLast = i == names.size() - 1;
			if (i == 0)
				line.append(name);
			else {
				final var withName = line + ", " + name;
				if (tabExpandedLength(withName + (isLast ? suffix : ",")) > MAX_LINE_LENGTH) {
					result.add(line + ",");
					line = new StringBuilder(contIndent + name);
				}
				else
					line = new StringBuilder(withName);
			}
			if (isLast)
				result.add(line + suffix);
		}
		return result;
	}
}