package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Enforces that Java fixture source (the kind of code that gets fed to a check
 * or fixer under test) lives in {@code src/test/resources}, not inline in
 * {@code *Test.java}. Files listed in
 * {@code config/test-inline-source-whitelist.txt} are exempt during migration.
 *
 * <p>See {@code docs/testing.md} ("Resource-only fixtures") for the rationale
 * and migration story.
 */
public class NoInlineJavaSourceTest {
	private static final Path TEST_ROOT = Path.of("src", "test", "java", "com", "etk2000", "checkstyle");
	private static final Path WHITELIST_FILE = Path.of("config", "test-inline-source-whitelist.txt");
	private static final Pattern CLASS_LIKE_DECL = Pattern.compile("\\b(class|interface|enum|record)\\s+[A-Z]\\w*\\s*[<({]");
	private static final Pattern METHOD_LIKE_DECL = Pattern.compile("\\b(void|int|long|String|boolean)\\s+\\w+\\s*\\([^)]*\\)\\s*[{;]");
	private static final Pattern STRING_LITERAL = Pattern.compile("\"(?:[^\"\\\\\\n]|\\\\.)*\"");
	private static final Pattern TEXT_BLOCK = Pattern.compile("\"\"\"[\\s\\S]*?\"\"\"");

	@CheckReturnValue
	@Nonnull
	private static String inferClassName(@Nonnull Path file) {
		return file.getFileName().toString().replace(".java", "");
	}

	@CheckReturnValue
	private static int lineAt(@Nonnull String source, int offset) {
		var line = 1;
		for (var i = 0; i < offset; ++i) {
			if (source.charAt(i) == '\n')
				++line;
		}
		return line;
	}

	@CheckReturnValue
	@Nonnull
	private static Set<String> loadWhitelist() throws IOException {
		if (!Files.exists(WHITELIST_FILE))
			return Set.of();
		final var entries = new HashSet<String>();
		for (var raw : Files.readAllLines(WHITELIST_FILE)) {
			final var hashIdx = raw.indexOf('#');
			final var line = (hashIdx < 0 ? raw : raw.substring(0, hashIdx)).trim();
			if (line.isEmpty())
				continue;
			entries.add(line);
		}
		return entries;
	}

	@CheckReturnValue
	private static boolean looksLikeJavaFixture(@Nonnull String literal) {
		if (literal.length() < 4)
			return false;
		return literal.contains("\n")
				|| (literal.contains("\\n") && (literal.contains("{") || literal.contains("}")))
				|| literal.contains("\\n\\t")
				|| literal.contains("\\t\\t")
				|| literal.contains("\t\t")
				|| literal.contains("// violation:")
				|| literal.contains("// target:")
				|| CLASS_LIKE_DECL.matcher(literal).find()
				|| METHOD_LIKE_DECL.matcher(literal).find()
				|| ((literal.startsWith("import ") || literal.startsWith("package ")) && literal.contains(";"));
	}

	private static void recordOffender(
			@Nonnull Map<String, List<String>> offenders,
			@Nonnull String className,
			int line,
			@Nonnull String literal
	) {
		final var summary = literal.length() > 80 ? literal.substring(0, 77) + "..." : literal;
		offenders.computeIfAbsent(className, k -> new ArrayList<>())
				.add("line " + line + ": " + summary.replace("\n", "\\n").replace("\t", "\\t"));
	}

	private static void scan(
			@Nonnull Path file,
			@Nonnull String className,
			@Nonnull Map<String, List<String>> offenders
	) throws IOException {
		final var source = Files.readString(file);

		final var textMatcher = TEXT_BLOCK.matcher(source);
		while (textMatcher.find()) {
			final var raw = textMatcher.group();
			final var content = raw.substring(3, raw.length() - 3);
			if (looksLikeJavaFixture(content))
				recordOffender(offenders, className, lineAt(source, textMatcher.start()), content);
		}

		final var masked = TEXT_BLOCK.matcher(source).replaceAll("\"\"");

		final var strMatcher = STRING_LITERAL.matcher(masked);
		while (strMatcher.find()) {
			final var raw = strMatcher.group();
			final var content = raw.substring(1, raw.length() - 1);
			if (looksLikeJavaFixture(content))
				recordOffender(offenders, className, lineAt(masked, strMatcher.start()), content);
		}
	}

	@Test
	public void noInlineJavaFixtureSource() throws IOException {
		final var whitelist = loadWhitelist();
		final var encountered = new HashSet<String>();
		final var offenders = new LinkedHashMap<String, List<String>>();

		try (var stream = Files.walk(TEST_ROOT)) {
			final var files = stream.filter(p -> p.toString().endsWith(".java")).sorted().toList();
			for (var file : files) {
				final var className = inferClassName(file);
				encountered.add(className);
				if (whitelist.contains(className))
					continue;
				scan(file, className, offenders);
			}
		}

		final var staleWhitelist = new TreeSet<>(whitelist);
		staleWhitelist.removeAll(encountered);

		final var errors = new ArrayList<String>();
		if (!offenders.isEmpty()) {
			final var msg = new StringBuilder("Inline Java fixture source found in non-whitelisted files. ");
			msg.append("Move the fixture to src/test/resources or add the file to ")
					.append(WHITELIST_FILE)
					.append(" during migration.\n\n");
			offenders.forEach((cls, occurrences) -> {
				msg.append(cls).append(":\n");
				for (var occ : occurrences)
					msg.append("  ").append(occ).append('\n');
			});
			errors.add(msg.toString());
		}
		if (!staleWhitelist.isEmpty()) {
			errors.add(
					"Whitelist entries refer to nonexistent test classes (remove them from "
							+ WHITELIST_FILE + "): " + staleWhitelist
			);
		}
		if (!errors.isEmpty())
			fail(String.join("\n\n", errors));
	}
}