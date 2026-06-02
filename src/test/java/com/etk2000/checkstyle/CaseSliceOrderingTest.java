package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

public class CaseSliceOrderingTest {
	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");
	private static final String CASE_MARKER_PREFIX = "// === case: ";
	private static final String CASE_MARKER_SUFFIX = " ===";

	private static List<String> extractCaseNames(@Nonnull Path path) throws IOException {
		final var names = new ArrayList<String>();
		for (var line : Files.readAllLines(path)) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(CASE_MARKER_PREFIX) && trimmed.endsWith(CASE_MARKER_SUFFIX))
				names.add(trimmed.substring(CASE_MARKER_PREFIX.length(), trimmed.length() - CASE_MARKER_SUFFIX.length()));
		}
		return names;
	}

	/**
	 * Fragment files carry the same {@code // === case: ===} markers as case slices
	 * and are loaded by name, so they need the same ordering and uniqueness
	 * guarantees. They were outside this guard until a duplicate name in one of them
	 * made two tests load the same body.
	 */
	private static boolean isCaseSliceFile(@Nonnull String filename) {
		return (filename.startsWith("cases.") || filename.startsWith("fragments.")) && filename.endsWith(".java");
	}

	@Test
	public void allCaseSliceFiles_haveAlphabeticallyOrderedCases() throws IOException {
		final var violations = new ArrayList<String>();
		try (var paths = Files.walk(INPUTS_ROOT)) {
			for (var path : paths.filter(p -> isCaseSliceFile(p.getFileName().toString())).sorted().toList()) {
				final var names = extractCaseNames(path);
				for (var i = 1; i < names.size(); ++i) {
					if (names.get(i).compareTo(names.get(i - 1)) < 0)
						violations.add(path + ": case '" + names.get(i) + "' must come before '" + names.get(i - 1) + "'");
				}
			}
		}
		if (!violations.isEmpty())
			fail("Case slices must be alphabetically ordered:\n  " + String.join("\n  ", violations));
	}

	@Test
	public void allCaseSliceFiles_haveUniqueCaseNames() throws IOException {
		final var violations = new ArrayList<String>();
		try (var paths = Files.walk(INPUTS_ROOT)) {
			for (var path : paths.filter(p -> isCaseSliceFile(p.getFileName().toString())).sorted().toList()) {
				final var seen = new HashSet<String>();
				for (var name : extractCaseNames(path)) {
					if (!seen.add(name))
						violations.add(path + ": duplicate case '" + name + "' (only one body is ever loaded, so the other is never tested)");
				}
			}
		}
		if (!violations.isEmpty())
			fail("Case slice names must be unique within a file:\n  " + String.join("\n  ", violations));
	}
}