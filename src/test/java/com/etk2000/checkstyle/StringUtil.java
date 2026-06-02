package com.etk2000.checkstyle;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public final class StringUtil {
	/**
	 * Replaces {@code suffix} with {@code replacement} only when {@code text}
	 * ends with it; otherwise returns {@code text} unchanged. Equivalent to a
	 * {@code $}-anchored regex replace, but without compiling a pattern or
	 * running the regex engine.
	 */
	@CheckReturnValue
	@Nonnull
	public static String replaceSuffix(@Nonnull String text, @Nonnull String suffix, @Nonnull String replacement) {
		if (!text.endsWith(suffix))
			return text;
		return text.substring(0, text.length() - suffix.length()) + replacement;
	}

	private StringUtil() {
	}
}