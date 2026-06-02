package com.etk2000.checkstyle;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * The kinds of inefficiency {@link JitInefficiencyCheck} detects. Shared between
 * the check (which resolves {@link #checkMessageKey()} for its violation message)
 * and {@code JitInefficiencyFixer} (which resolves {@link #skipReasonKey()} for the
 * {@code SkipResult} it returns when it recognizes a violation it cannot rewrite).
 */
public enum JitInefficiencyCategory {
	APPEND_CONCAT("jit.append.concat", "jit.append.concat.skip"),
	BOXED_ACCUMULATOR("jit.boxed.accumulator", "jit.boxed.accumulator.skip"),
	BOXED_CONSTRUCTOR("jit.boxed.constructor", "jit.boxed.constructor.skip"),
	DOUBLE_BRACE("jit.double.brace", "jit.double.brace.skip"),
	EMPTY_STRING_CONCAT("jit.empty.string.concat", "jit.empty.string.concat.skip"),
	ENUM_VALUES_IN_LOOP("jit.enum.values.in.loop", "jit.enum.values.in.loop.skip"),
	ITERATOR_LOOP("jit.iterator.loop", "jit.iterator.loop.skip"),
	MAP_KEYSET_GET("jit.map.keyset.get", "jit.map.keyset.get.skip"),
	NEW_STRING("jit.new.string", "jit.new.string.skip"),
	REUSABLE_OBJECT("jit.reusable.object", "jit.reusable.object.skip"),
	STRING_BUFFER("jit.string.buffer", "jit.string.buffer.skip"),
	STRING_CONCAT_IN_LOOP("jit.string.concat.in.loop", "jit.string.concat.in.loop.skip"),
	STRING_REGEX_IN_LOOP("jit.string.regex.in.loop", "jit.string.regex.in.loop.skip"),
	TOARRAY_SIZED("jit.toarray.sized", "jit.toarray.sized.skip");

	@Nonnull
	private final String checkMessageKey;

	@Nonnull
	private final String skipReasonKey;

	JitInefficiencyCategory(@Nonnull String checkMessageKey, @Nonnull String skipReasonKey) {
		this.checkMessageKey = checkMessageKey;
		this.skipReasonKey = skipReasonKey;
	}

	@CheckReturnValue
	@Nonnull
	public String checkMessageKey() {
		return checkMessageKey;
	}

	@CheckReturnValue
	@Nonnull
	public String skipReasonKey() {
		return skipReasonKey;
	}
}