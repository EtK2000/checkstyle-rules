package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.puppycrawl.tools.checkstyle.JavaParser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Verifies that every {@code .java} file under {@code src/test/resources} parses
 * with checkstyle's {@link JavaParser}. Without this test, a syntax error in a
 * fixture causes {@code checkstyleTestResources} to fail with a giant
 * "Unable to process files: [...]" message that buries the actual file:line:col
 * deep in the stacktrace.
 *
 * <p>{@code fragments.in.java} / {@code fragments.out.java} are line fragments,
 * not full compilation units (matching the exclusion in {@code build.gradle}'s
 * {@code checkstyleTestResources} task), so they are skipped.
 */
public class TestResourcesParseTest {
	private static final Path TEST_RESOURCES_ROOT = Path.of("src", "test", "resources");
	private static final String BLANK_SUFFIX = ": blank compilation unit (comments or whitespace only); delete it";

	@CheckReturnValue
	@Nonnull
	private static Optional<String> classify(@Nonnull String content) throws IOException {
		final var tempFile = File.createTempFile("parse-test", ".java");
		try {
			Files.writeString(tempFile.toPath(), content);
			return classifyResource(tempFile.toPath());
		}
		finally {
			tempFile.delete();
		}
	}

	@CheckReturnValue
	@Nonnull
	private static Optional<String> classifyResource(@Nonnull Path file) {
		try {
			final var root = JavaParser.parseFile(file.toFile(), JavaParser.Options.WITHOUT_COMMENTS);
			return root == null ? Optional.of(file + BLANK_SUFFIX) : Optional.empty();
		}
		catch (Exception e) {
			final var cause = mostSpecificCause(e);
			return Optional.of(file + ": " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
		}
	}

	@CheckReturnValue
	private static boolean isParseChecked(@Nonnull Path p) {
		if (!p.toString().endsWith(".java"))
			return false;
		final var name = p.getFileName().toString();
		return !"fragments.in.java".equals(name) && !"fragments.out.java".equals(name);
	}

	@CheckReturnValue
	@Nonnull
	static Throwable mostSpecificCause(@Nonnull Throwable t) {
		final var seen = Collections.newSetFromMap(new IdentityHashMap<Throwable, Boolean>());
		var best = t;
		for (var c = t; c != null && seen.add(c); c = c.getCause()) {
			final var msg = c.getMessage();
			if (msg != null && !msg.isBlank())
				best = c;
		}
		return best;
	}

	@Test
	public void allTestResourcesParse() throws IOException {
		final var failures = new ArrayList<String>();
		try (var stream = Files.walk(TEST_RESOURCES_ROOT)) {
			final var files = stream
					.filter(TestResourcesParseTest::isParseChecked)
					.sorted()
					.toList();
			for (var file : files)
				classifyResource(file).ifPresent(failures::add);
		}
		if (!failures.isEmpty())
			fail("Test resources failed to parse:\n  " + String.join("\n  ", failures));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "    ", "// a comment", "/* a comment */", "/** a comment */"})
	public void blankInputsAreFlaggedAsNullRoot(@Nonnull String content) throws Exception {
		assertTrue(classify(content).orElse("").endsWith(BLANK_SUFFIX), "blank content must be flagged");
	}

	@ParameterizedTest
	@ValueSource(strings = {"/* c */ ;", "/** c */ ;"})
	public void commentFollowedByCodeIsRealRoot(@Nonnull String content) throws Exception {
		assertTrue(classify(content).isEmpty(), "a comment before real code must not be flagged");
	}

	@Test
	public void fragmentsFilesAreNotParseChecked() {
		assertFalse(isParseChecked(Path.of("topic", "fragments.in.java")));
		assertFalse(isParseChecked(Path.of("topic", "fragments.out.java")));
		assertTrue(isParseChecked(Path.of("topic", "cases.in.java")));
	}

	@Test
	public void mostSpecificCausePrefersInnermostNonBlankMessage() {
		final var inner = new IllegalStateException("the real cause");
		final var outer = new RuntimeException("", inner);
		assertSame(inner, mostSpecificCause(outer));
	}

	@Test
	public void mostSpecificCauseReturnsRootWhenNoCause() {
		final var only = new RuntimeException("solo");
		assertSame(only, mostSpecificCause(only));
	}

	@Test
	public void mostSpecificCauseTerminatesOnCyclicCause() {
		// an A->B->A cause cycle must stop the walk, not spin forever
		final var a = new RuntimeException("A");
		final var b = new RuntimeException("B");
		a.initCause(b);
		b.initCause(a);
		assertTimeoutPreemptively(Duration.ofSeconds(2), () -> mostSpecificCause(a));
	}

	@Test
	public void parseErrorIsReportedWithCause() throws Exception {
		// a leading BOM is rejected by checkstyle's parser; the failure must surface the cause
		final var failure = classify((char) 0xFEFF + "x");
		assertTrue(failure.isPresent(), "a parse error must be reported");
		assertTrue(failure.get().contains("Exception"), failure.get());
	}

	@Test
	public void strayTopLevelSemicolonIsNotFlagged() throws Exception {
		// a lone top-level ';' is a valid (empty) type declaration, so it parses to a real root
		assertTrue(classify(";").isEmpty());
	}
}