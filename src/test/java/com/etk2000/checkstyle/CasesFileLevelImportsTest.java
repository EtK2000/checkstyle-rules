package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Guards the per-slice imports convention: every {@code cases.*.java} fixture
 * file with slices must declare imports via per-slice {@code // imports:}
 * directives, not file-level {@code import} lines at the top.
 *
 * <p>{@code cases.*.clean.*.java} files are exempted: they are full-file
 * fixtures with no slices to attach per-slice directives to.
 */
public class CasesFileLevelImportsTest {
	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");
	private static final String CASE_MARKER_PREFIX = "// === case:";

	@CheckReturnValue
	private static boolean hasFileLevelImports(@Nonnull Path path) throws IOException {
		for (var line : Files.readAllLines(path)) {
			final var trimmed = line.trim();
			if (trimmed.startsWith(CASE_MARKER_PREFIX))
				return false;
			if (trimmed.startsWith("import "))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isCasesFile(@Nonnull String filename) {
		// `cases.*.clean.*.java` files are full-file clean fixtures (not sliced);
		// file-level imports are legitimate there since the file has no slices to attach to.
		return filename.startsWith("cases.") && filename.endsWith(".java")
				&& !filename.contains(".clean.");
	}

	@Test
	public void noUnmigratedFileLevelImports() throws IOException {
		final var offenders = new ArrayList<String>();
		try (var paths = Files.walk(INPUTS_ROOT)) {
			for (var path : paths.filter(p -> isCasesFile(p.getFileName().toString())).sorted().toList()) {
				if (hasFileLevelImports(path))
					offenders.add(INPUTS_ROOT.relativize(path).toString().replace('\\', '/'));
			}
		}
		if (!offenders.isEmpty()) {
			fail(
					"cases.*.java file(s) carry file-level `import` statements. "
							+ "Use per-slice `// imports:` directives instead (see docs/testing.md). Offenders:\n  "
							+ String.join("\n  ", offenders)
			);
		}
	}
}