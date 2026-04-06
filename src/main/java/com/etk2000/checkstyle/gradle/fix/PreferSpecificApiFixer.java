package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferSpecificApiFixer implements CheckstyleFixer {
	private static final String[][] ASSERT_RULES = {
			// {methodName, literal, replacement}
			{"assertEquals", "false", "assertFalse"},
			{"assertEquals", "null", "assertNull"},
			{"assertEquals", "true", "assertTrue"},
			{"assertNotEquals", "false", "assertTrue"},
			{"assertNotEquals", "null", "assertNotNull"},
			{"assertNotEquals", "true", "assertFalse"},
			{"assertNotSame", "null", "assertNotNull"},
			{"assertSame", "null", "assertNull"}
	};

	/**
	 * Simplifies assertion calls with literal arguments.
	 * Handles both literal-first ({@code assertEquals(true, x)})
	 * and literal-last ({@code assertEquals(x, true)}) 2-arg forms.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertion(@Nonnull String line) {
		for (final var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralFirst(line, rule[0], rule[1], rule[2]);
			if (result != null)
				return result;
		}
		for (final var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralLast(line, rule[0], rule[1], rule[2]);
			if (result != null)
				return result;
		}
		for (final var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralMiddle(line, rule[0], rule[1], rule[2]);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Handles {@code assertEquals(true, x)} -> {@code assertTrue(x)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralFirst(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var pattern = methodName + "(" + literal + ", ";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + replacement + "(" + line.substring(idx + pattern.length());
	}

	/**
	 * Handles {@code assertEquals(x, true)} -> {@code assertTrue(x)}.
	 * Finds the method call and removes the trailing literal argument.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralLast(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var methodStart = line.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var suffix = ", " + literal + ")";
		final var suffixIdx = line.indexOf(suffix, methodStart);
		if (suffixIdx < 0)
			return null;

		return line.substring(0, methodStart) + replacement + "("
				+ line.substring(methodStart + methodName.length() + 1, suffixIdx)
				+ ")" + line.substring(suffixIdx + suffix.length());
	}

	/**
	 * Handles 3-arg forms where the literal is in the middle position:
	 * {@code assertEquals("msg", null, x)} -> {@code assertNull("msg", x)} (JUnit 4),
	 * {@code assertEquals(x, null, "msg")} -> {@code assertNull(x, "msg")} (JUnit 5).
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralMiddle(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var methodStart = line.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var pattern = ", " + literal + ", ";
		final var patternIdx = line.indexOf(pattern, methodStart);
		if (patternIdx < 0)
			return null;

		return line.substring(0, methodStart) + replacement + "("
				+ line.substring(methodStart + methodName.length() + 1, patternIdx)
				+ ", " + line.substring(patternIdx + pattern.length());
	}

	/**
	 * {@code Collections.emptyList()} -> {@code List.of()},
	 * {@code Collections.singletonList(x)} -> {@code List.of(x)}, etc.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectionsFactory(@Nonnull String line) {
		final String[][] replacements = {
				{"Collections.emptyList()", "List.of()"},
				{"Collections.emptyMap()", "Map.of()"},
				{"Collections.emptySet()", "Set.of()"},
				{"Collections.singleton(", "Set.of("},
				{"Collections.singletonList(", "List.of("},
				{"Collections.singletonMap(", "Map.of("},
				{"Collections.unmodifiableList(", "List.copyOf("},
				{"Collections.unmodifiableMap(", "Map.copyOf("},
				{"Collections.unmodifiableSet(", "Set.copyOf("}
		};
		for (final var r : replacements) {
			final var idx = line.indexOf(r[0]);
			if (idx >= 0)
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
		}
		return null;
	}

	/**
	 * {@code .collect(Collectors.toList())} -> {@code .toList()}, and
	 * {@code .collect(Collectors.toUnmodifiableList())} -> {@code .toList()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectToList(@Nonnull String line) {
		for (final var collector : new String[]{"toList", "toUnmodifiableList"}) {
			final var pattern = ".collect(Collectors." + collector + "())";
			final var idx = line.indexOf(pattern);
			if (idx >= 0)
				return line.substring(0, idx) + ".toList()" + line.substring(idx + pattern.length());
		}
		return null;
	}

	/**
	 * {@code .equals("")} -> {@code .isEmpty()}.
	 * Only matches when the argument is literally {@code ""}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixEqualsEmpty(@Nonnull String line) {
		final var pattern = ".equals(\"\")";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".isEmpty()" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .keySet().contains(k)} -> {@code .containsKey(k)},
	 * {@code .values().contains(v)} -> {@code .containsValue(v)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixMapChain(@Nonnull String line) {
		final String[][] replacements = {
				{".keySet().contains(", ".containsKey("},
				{".values().contains(", ".containsValue("}
		};
		for (final var r : replacements) {
			final var idx = line.indexOf(r[0]);
			if (idx >= 0)
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
		}
		return null;
	}

	/**
	 * {@code .replaceAll("literal", x)} -> {@code .replace("literal", x)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixReplaceAll(@Nonnull String line) {
		final var pattern = ".replaceAll(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".replace(" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().count()} -> {@code .size()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamCount(@Nonnull String line) {
		final var pattern = ".stream().count()";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".size()" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().forEach(} -> {@code .forEach(}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamForEach(@Nonnull String line) {
		final var pattern = ".stream().forEach(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".forEach(" + line.substring(idx + pattern.length());
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// try each fixable pattern in turn
		var result = fixAssertion(line);
		if (result == null)
			result = fixCollectionsFactory(line);
		if (result == null)
			result = fixCollectToList(line);
		if (result == null)
			result = fixEqualsEmpty(line);
		if (result == null)
			result = fixMapChain(line);
		if (result == null)
			result = fixReplaceAll(line);
		if (result == null)
			result = fixStreamCount(line);
		if (result == null)
			result = fixStreamForEach(line);

		if (result == null)
			return null;
		return new FixResult(lineIndex, lineIndex, List.of(result));
	}
}