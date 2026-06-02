package com.etk2000.checkstyle.gradle.fix;

import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Test-only helper that serializes a {@code Map} of check properties
 * into the two representations the {@code StandardCheckTests}
 * pipeline needs: a flat key/value {@code String[]} for {@code
 * runCheck}-style varargs, and a dotted variant-filename suffix (e.g.
 * {@code minSdk-19} or {@code minSdk-19.tokens-FOR_INIT}). Both forms
 * sort by key so downstream consumers (variant filename lookup,
 * property array comparison) see deterministic output.
 */
public final class PropertiesUtil {
	@CheckReturnValue
	@Nonnull
	public static Map<String, String> arrayToMap(@Nonnull String[] properties) {
		if (properties.length == 0)
			return Map.of();
		if (properties.length % 2 != 0)
			throw new IllegalArgumentException("properties must be an even-length key/value sequence, got length " + properties.length);
		final var map = new HashMap<String, String>();
		for (var i = 0; i + 1 < properties.length; i += 2)
			map.put(properties[i], properties[i + 1]);
		return Map.copyOf(map);
	}

	@CheckReturnValue
	@Nonnull
	public static String[] propertiesAsArray(@Nonnull Map<String, String> properties) {
		final var arr = new String[properties.size() * 2];
		var i = 0;
		for (var entry : new TreeMap<>(properties).entrySet()) {
			arr[i++] = entry.getKey();
			arr[i++] = entry.getValue();
		}
		return arr;
	}

	@CheckReturnValue
	@Nonnull
	public static String variantSuffix(@Nonnull Map<String, String> properties) {
		if (properties.isEmpty())
			return "";
		return new TreeMap<>(properties).entrySet().stream()
				.map(e -> e.getKey() + "-" + e.getValue())
				.collect(joining("."));
	}

	@CheckReturnValue
	@Nonnull
	public static String variantSuffixFromArray(@Nonnull String[] checkProperties) {
		if (checkProperties.length == 0)
			return "";
		if (checkProperties.length % 2 != 0) {
			throw new IllegalArgumentException(
					"checkProperties must be an even-length key/value sequence, got length " + checkProperties.length
			);
		}
		final var sorted = new TreeMap<String, String>();
		for (var i = 0; i + 1 < checkProperties.length; i += 2)
			sorted.put(checkProperties[i], checkProperties[i + 1]);
		return sorted.entrySet().stream()
				.map(e -> e.getKey() + "-" + e.getValue())
				.collect(joining("."));
	}

	private PropertiesUtil() {
	}
}