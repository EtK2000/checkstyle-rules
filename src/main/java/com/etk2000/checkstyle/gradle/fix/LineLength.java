package com.etk2000.checkstyle.gradle.fix;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared line-length budget used by fixers that decide between single-line and multi-line
 * output (e.g. field consolidation, field sorting, record component layout).
 * <p>
 * The tab-width used here ({@value #WRAP_TAB_WIDTH}) is the per-project wrap convention,
 * not Checkstyle's tab-expansion width — wrap decisions count tabs as four columns.
 */
final class LineLength {
	static final int MAX_LINE_LENGTH = 120;
	static final int WRAP_TAB_WIDTH = 4;

	@CheckReturnValue
	static int tabExpandedLength(@Nonnull String line) {
		var len = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (line.charAt(i) == '\t')
				len += WRAP_TAB_WIDTH - (len % WRAP_TAB_WIDTH);
			else
				++len;
		}
		return len;
	}
}