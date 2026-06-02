package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class FragmentCaseReferencesTest {
	private static final Path INPUTS_ROOT = Path.of("src/test/resources/com/etk2000/checkstyle/inputs");
	private static final Path TEST_SOURCES_ROOT = Path.of("src/test/java");
	private static final String FRAGMENTS_IN = "fragments.in.java";

	private static void addCommaSeparatedTokens(@Nonnull String lit, @Nonnull Set<String> out) {
		var start = 0;
		for (var i = 0; i < lit.length(); ++i) {
			if (lit.charAt(i) == ',') {
				out.add(lit.substring(start, i).strip());
				start = i + 1;
			}
		}
		out.add(lit.substring(start).strip());
	}

	@CheckReturnValue
	@Nonnull
	private static Map<String, List<String>> collectTopicCases() throws IOException {
		final var topicCases = new LinkedHashMap<String, List<String>>();
		try (var paths = Files.walk(INPUTS_ROOT)) {
			for (var path : paths.filter(p -> p.getFileName().toString().equals(FRAGMENTS_IN)).sorted().toList()) {
				final var topic = path.getParent().getFileName().toString();
				topicCases.put(topic, TestResources.caseNames(topic));
			}
		}
		if (topicCases.isEmpty())
			throw new IllegalStateException("no fragments.in.java found under " + INPUTS_ROOT);
		return topicCases;
	}

	@CheckReturnValue
	@Nonnull
	static List<String> extractStringLiterals(@Nonnull String strippedSource) {
		final var result = new ArrayList<String>();
		final var len = strippedSource.length();
		var i = 0;
		while (i < len) {
			final var c = strippedSource.charAt(i);
			if (c == '"' && i + 2 < len && strippedSource.charAt(i + 1) == '"' && strippedSource.charAt(i + 2) == '"') {
				i += 3;
				final var sb = new StringBuilder();
				while (i < len) {
					if (i + 2 < len && strippedSource.charAt(i) == '"' && strippedSource.charAt(i + 1) == '"' && strippedSource.charAt(i + 2) == '"') {
						i += 3;
						break;
					}
					sb.append(strippedSource.charAt(i));
					++i;
				}
				result.add(sb.toString());
				continue;
			}
			if (c == '"') {
				++i;
				final var sb = new StringBuilder();
				while (i < len && strippedSource.charAt(i) != '"' && strippedSource.charAt(i) != '\n') {
					if (strippedSource.charAt(i) == '\\' && i + 1 < len) {
						sb.append(strippedSource.charAt(i + 1));
						i += 2;
					}
					else {
						sb.append(strippedSource.charAt(i));
						++i;
					}
				}
				if (i < len && strippedSource.charAt(i) == '"')
					++i;
				result.add(sb.toString());
				continue;
			}
			if (c == '\'') {
				++i;
				while (i < len && strippedSource.charAt(i) != '\'' && strippedSource.charAt(i) != '\n') {
					if (strippedSource.charAt(i) == '\\' && i + 1 < len)
						i += 2;
					else
						++i;
				}
				if (i < len && strippedSource.charAt(i) == '\'')
					++i;
				continue;
			}
			++i;
		}
		return result;
	}

	@CheckReturnValue
	@Nonnull
	static Map<String, List<String>> findUnreferencedCases(
			@Nonnull Map<String, List<String>> topicCases,
			@Nonnull List<String> strippedSources
	) {
		final var referenced = referencedTokens(strippedSources);
		final var result = new TreeMap<String, List<String>>();
		for (var entry : topicCases.entrySet()) {
			final var unused = new ArrayList<String>();
			for (var name : entry.getValue()) {
				if (!referenced.contains(name))
					unused.add(name);
			}
			if (!unused.isEmpty())
				result.put(entry.getKey(), unused);
		}
		return result;
	}

	@CheckReturnValue
	@Nonnull
	private static Set<String> referencedTokens(@Nonnull List<String> strippedSources) {
		final var tokens = new HashSet<String>();
		for (var src : strippedSources) {
			for (var lit : extractStringLiterals(src))
				addCommaSeparatedTokens(lit, tokens);
		}
		return tokens;
	}

	@Test
	public void testEveryDeclaredCaseIsReferenced() throws IOException {
		final var stripped = JavaSourceUtil.walkJavaSources(TEST_SOURCES_ROOT)
				.stream().map(JavaSourceUtil::stripJavaComments).toList();
		final var topicCases = collectTopicCases();
		// CheckstyleFixIntegrationCasesTest's @TestFactory enumerates every integration
		// case via TestResources.caseNames, so they are referenced dynamically rather
		// than by a per-case string literal the scan below can see.
		topicCases.remove("integration");
		final var unreferenced = findUnreferencedCases(topicCases, stripped);
		if (!unreferenced.isEmpty())
			fail("Unreferenced cases in fragments.in.java (declared but no test references them): " + unreferenced);
	}

	@Test
	public void testExtractStringLiteralsHandlesEmptyString() {
		assertEquals(
				List.of(""),
				extractStringLiterals("var s = \"\";")
		);
	}

	@Test
	public void testExtractStringLiteralsHandlesEscapeAtEof() {
		assertEquals(
				List.of("a\\"),
				extractStringLiterals("\"a\\")
		);
	}

	@Test
	public void testExtractStringLiteralsHandlesEscapedQuote() {
		assertEquals(
				List.of("a\"b"),
				extractStringLiterals("var s = \"a\\\"b\";")
		);
	}

	@Test
	public void testExtractStringLiteralsHandlesTextBlock() {
		assertEquals(
				List.of("\nfoo, bar\n"),
				extractStringLiterals("var s = \"\"\"\nfoo, bar\n\"\"\";")
		);
	}

	@Test
	public void testExtractStringLiteralsHandlesUnterminatedTextBlock() {
		assertEquals(
				List.of("abc"),
				extractStringLiterals("\"\"\"abc")
		);
	}

	@Test
	public void testExtractStringLiteralsPreservesContentAndSkipsCharLiterals() {
		assertEquals(
				List.of("hello, world", "x"),
				extractStringLiterals("var a = \"hello, world\"; char c = ','; var b = \"x\";")
		);
	}

	@Test
	public void testFindUnreferencedCasesAggregatesAcrossTopics() {
		assertEquals(
				Map.of(
						"topic_a", List.of("orphan_a"),
						"topic_b", List.of("orphan_b1", "orphan_b2")
				),
				findUnreferencedCases(
						Map.of(
								"topic_a", List.of("orphan_a"),
								"topic_b", List.of("orphan_b1", "orphan_b2")
						),
						List.of("class T {}")
				)
		);
	}

	@Test
	public void testFindUnreferencedCasesEmptyTopicNotInResult() {
		assertTrue(findUnreferencedCases(
				Map.of("topic_a", List.of()),
				List.of("class T {}")
		).isEmpty());
	}

	@Test
	public void testFindUnreferencedCasesIgnoresSubstringMatch() {
		assertEquals(
				Map.of("t", List.of("foo")),
				findUnreferencedCases(
						Map.of("t", List.of("foo")),
						List.of("class T { String s = \"food\"; }")
				)
		);
	}

	@Test
	public void testFindUnreferencedCasesIgnoresUnquotedMention() {
		assertEquals(
				Map.of("t", List.of("foo")),
				findUnreferencedCases(
						Map.of("t", List.of("foo")),
						List.of("class T { int foo; }")
				)
		);
	}

	@Test
	public void testFindUnreferencedCasesMatchesCsvFirstPosition() {
		assertTrue(findUnreferencedCases(
				Map.of("t", List.of("foo")),
				List.of("class T { @CsvSource({\"foo, java.util.Foo\"}) void m() {} }")
		).isEmpty());
	}

	@Test
	public void testFindUnreferencedCasesMatchesCsvLastPosition() {
		assertTrue(findUnreferencedCases(
				Map.of("t", List.of("foo")),
				List.of("class T { String s = \"alpha, beta, foo\"; }")
		).isEmpty());
	}

	@Test
	public void testFindUnreferencedCasesMatchesCsvMiddlePosition() {
		assertTrue(findUnreferencedCases(
				Map.of("t", List.of("foo")),
				List.of("class T { String s = \"alpha, foo, beta\"; }")
		).isEmpty());
	}

	@Test
	public void testFindUnreferencedCasesMatchesQuotedLiteral() {
		assertTrue(findUnreferencedCases(
				Map.of("t", List.of("foo")),
				List.of("class T { String s = \"foo\"; }")
		).isEmpty());
	}

	@Test
	public void testFindUnreferencedCasesReportsOrphanWhenNoMatch() {
		assertEquals(
				Map.of("t", List.of("orphan")),
				findUnreferencedCases(
						Map.of("t", List.of("orphan")),
						List.of("class T {}")
				)
		);
	}
}