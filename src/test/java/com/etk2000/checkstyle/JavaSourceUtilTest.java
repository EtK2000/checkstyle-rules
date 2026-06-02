package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class JavaSourceUtilTest {
	@Nonnull
	private static Stream<Arguments> stripJavaCommentsCases() {
		return Stream.of(
				Arguments.of("foo // line\nbar", "foo \nbar"),
				Arguments.of("a /* x */ b", "a   b"),
				Arguments.of("\"// inside string\"", "\"// inside string\""),
				Arguments.of("\"/* inside string */\"", "\"/* inside string */\""),
				Arguments.of("'/'", "'/'"),
				Arguments.of("// only comment", ""),
				Arguments.of("/* unterminated", " "),
				Arguments.of("a // b\nc /* d */ e", "a \nc   e"),
				Arguments.of("\"\"\"\n// in text block\n\"\"\"", "\"\"\"\n// in text block\n\"\"\""),
				Arguments.of("\"a\\\"b\"", "\"a\\\"b\""),
				Arguments.of("\"a\\\\\"", "\"a\\\\\""),
				Arguments.of("\"a\\", "\"a\\"),
				Arguments.of("\"\"\"abc\"\"\"", "\"\"\"abc\"\"\""),
				Arguments.of("'\\''", "'\\''"),
				Arguments.of("'\\n'", "'\\n'"),
				Arguments.of("/*/", " "),
				Arguments.of("/* * */", " ")
		);
	}

	@MethodSource("stripJavaCommentsCases")
	@ParameterizedTest
	public void testStripJavaComments(String input, String expected) {
		assertEquals(expected, JavaSourceUtil.stripJavaComments(input));
	}

	@Test
	public void testWalkJavaSourcesFailsWhenEmpty() throws Exception {
		final var tempDir = Files.createTempDirectory("empty-sources");
		try {
			assertThrows(IllegalStateException.class, () -> JavaSourceUtil.walkJavaSources(tempDir));
		}
		finally {
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void testWalkJavaSourcesIgnoresNonJavaFiles() throws Exception {
		final var tempDir = Files.createTempDirectory("walk-mixed");
		final var javaFile = tempDir.resolve("Foo.java");
		final var txtFile = tempDir.resolve("Bar.txt");
		final var bakFile = tempDir.resolve("Baz.java.bak");
		try {
			Files.writeString(javaFile, "class Foo {}", StandardCharsets.UTF_8);
			Files.writeString(txtFile, "ignored", StandardCharsets.UTF_8);
			Files.writeString(bakFile, "ignored", StandardCharsets.UTF_8);
			final var sources = JavaSourceUtil.walkJavaSources(tempDir);
			assertEquals(1, sources.size());
		}
		finally {
			Files.deleteIfExists(javaFile);
			Files.deleteIfExists(txtFile);
			Files.deleteIfExists(bakFile);
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void testWalkJavaSourcesReadsFileContents() throws Exception {
		final var tempDir = Files.createTempDirectory("walk-content");
		final var javaFile = tempDir.resolve("Foo.java");
		try {
			Files.writeString(javaFile, "class Foo {}", StandardCharsets.UTF_8);
			final var sources = JavaSourceUtil.walkJavaSources(tempDir);
			assertEquals(1, sources.size());
			assertEquals("class Foo {}", sources.getFirst());
		}
		finally {
			Files.deleteIfExists(javaFile);
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void testWalkJavaSourcesReadsMultipleFiles() throws Exception {
		final var tempDir = Files.createTempDirectory("walk-multi");
		final var fileA = tempDir.resolve("A.java");
		final var fileB = tempDir.resolve("B.java");
		try {
			Files.writeString(fileA, "class A {}", StandardCharsets.UTF_8);
			Files.writeString(fileB, "class B {}", StandardCharsets.UTF_8);
			final var sources = JavaSourceUtil.walkJavaSources(tempDir);
			assertEquals(2, sources.size());
			assertEquals(Set.of("class A {}", "class B {}"), Set.copyOf(sources));
		}
		finally {
			Files.deleteIfExists(fileA);
			Files.deleteIfExists(fileB);
			Files.deleteIfExists(tempDir);
		}
	}

	@Test
	public void testWalkJavaSourcesRecursesIntoSubdirectories() throws Exception {
		final var tempDir = Files.createTempDirectory("walk-nested");
		final var subDir = Files.createDirectory(tempDir.resolve("sub"));
		final var javaFile = subDir.resolve("Foo.java");
		try {
			Files.writeString(javaFile, "class Foo {}", StandardCharsets.UTF_8);
			final var sources = JavaSourceUtil.walkJavaSources(tempDir);
			assertEquals(1, sources.size());
			assertEquals("class Foo {}", sources.getFirst());
		}
		finally {
			Files.deleteIfExists(javaFile);
			Files.deleteIfExists(subDir);
			Files.deleteIfExists(tempDir);
		}
	}
}