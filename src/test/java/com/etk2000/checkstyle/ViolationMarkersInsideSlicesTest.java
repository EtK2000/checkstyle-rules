package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Enforces that every {@code // violation: ...} marker in a
 * {@code cases.*.in.java} fixture lives inside a
 * {@code // === case: NAME === / // === end ===} block.
 *
 * <p>A marker outside any case slice is a violation pattern that can only be
 * reached by the check unit test and the E2E regression test. Fixer unit
 * tests ({@code assertCaseFix}) and integration tests ({@code assertFullFix})
 * can't exercise it. To get true per-pattern sharing across all four test
 * layers, every scenario needs to be a slice.
 */
public class ViolationMarkersInsideSlicesTest {
	private static final Path INPUTS_ROOT = Path.of("src", "test", "resources", "com", "etk2000", "checkstyle", "inputs");
	private static final String CASE_MARKER = "// === case:";
	private static final String END_MARKER = "// === end ===";
	private static final String VIOLATION_MARKER = "// violation:";

	@CheckReturnValue
	private static int scanOutsideMarkers(@Nonnull Path file) throws IOException {
		final var lines = Files.readAllLines(file);
		var inSlice = false;
		var outsideCount = 0;
		for (var line : lines) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(CASE_MARKER))
				inSlice = true;
			else if (trimmed.equals(END_MARKER))
				inSlice = false;
			else if (!inSlice && line.contains(VIOLATION_MARKER))
				++outsideCount;
		}
		return outsideCount;
	}

	@Test
	public void everyViolationMarkerLivesInsideACaseSlice() throws IOException {
		final var offenders = new TreeSet<String>();
		try (var stream = Files.walk(INPUTS_ROOT)) {
			final var inputs = stream
					.filter(Files::isRegularFile)
					.filter(p -> {
						final var name = p.getFileName().toString();
						return name.startsWith("cases.") && name.endsWith(".in.java");
					})
					.sorted()
					.toList();
			for (var input : inputs) {
				final var relative = INPUTS_ROOT.relativize(input).toString().replace('\\', '/');
				final var outsideCount = scanOutsideMarkers(input);
				if (outsideCount > 0)
					offenders.add(relative + " (" + outsideCount + " marker(s) outside any case slice)");
			}
		}
		if (!offenders.isEmpty()) {
			fail(
					"Found `// violation:` markers OUTSIDE any `// === case: ... ===` / `// === end ===` block."
							+ " Move the violation pattern into a case slice:\n  "
							+ String.join("\n  ", offenders)
			);
		}
	}
}