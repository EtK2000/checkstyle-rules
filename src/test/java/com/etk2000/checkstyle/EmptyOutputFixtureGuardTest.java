package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Fails when an expected-output fixture ({@code cases.*.out.java},
 * {@code cases.*.fixed.java}, {@code fragments.out.java}, ...) is blank (no
 * non-whitespace content). The loaders treat a missing and a blank output file
 * identically, so a blank one is dead weight that should be deleted, not
 * retained as a placeholder.
 */
public class EmptyOutputFixtureGuardTest {
	private static final Path TEST_RESOURCES_ROOT = Path.of("src", "test", "resources");

	@CheckReturnValue
	private static boolean isEmptyOutputFixture(@Nonnull List<String> lines) {
		return lines.stream().allMatch(String::isBlank);
	}

	@CheckReturnValue
	private static boolean isOutputFixture(@Nonnull String fileName) {
		if (!fileName.endsWith(".java"))
			return false;
		final var components = fileName.split("\\.");
		if (!"cases".equals(components[0]) && !"fragments".equals(components[0]))
			return false;
		for (var component : components) {
			if ("out".equals(component) || "fixed".equals(component))
				return true;
		}
		return false;
	}

	@Test
	public void isEmptyOutputFixtureFlagsBlankContent() {
		assertTrue(isEmptyOutputFixture(List.of()));
		assertTrue(isEmptyOutputFixture(List.of("", "\t", "   ")));
	}

	@Test
	public void isEmptyOutputFixtureRejectsNonBlankContent() {
		assertFalse(isEmptyOutputFixture(List.of("// a comment")));
		assertFalse(isEmptyOutputFixture(List.of("// === case: foo ===", "\tint x;", "// === end ===")));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"cases.out.java",
			"cases.fixed.java",
			"fragments.out.java",
			"cases.junit4.out.java",
			"cases.out.minSdk-18.java",
			"cases.minOccurrences-1.fixed.java"
	})
	public void isOutputFixtureAcceptsOutputNames(@Nonnull String fileName) {
		assertTrue(isOutputFixture(fileName), fileName);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"cases.in.java",
			"cases.clean.java",
			"fragments.in.java",
			"cases.layout.in.java",
			"InputAstUtil.java",
			"cases.out.txt"
	})
	public void isOutputFixtureRejectsNonOutputNames(@Nonnull String fileName) {
		assertFalse(isOutputFixture(fileName), fileName);
	}

	@Test
	public void noEmptyOutputFixtures() throws IOException {
		final var offenders = new ArrayList<String>();
		try (var stream = Files.walk(TEST_RESOURCES_ROOT)) {
			final var files = stream
					.filter(p -> isOutputFixture(p.getFileName().toString()))
					.sorted()
					.toList();
			for (var file : files) {
				if (isEmptyOutputFixture(Files.readAllLines(file)))
					offenders.add(file.toString());
			}
		}
		if (!offenders.isEmpty()) {
			fail(
					"Blank expected-output fixture(s) with no non-whitespace content; delete each"
							+ " (a missing output file loads identically to a blank one):\n  "
							+ String.join("\n  ", offenders)
			);
		}
	}
}