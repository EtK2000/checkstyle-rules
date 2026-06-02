package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;

/**
 * Edge-case coverage for {@link PreferImportCheck} that cannot live in the
 * data-driven {@code StandardCheckTests} slices: a pathologically deep generated
 * name, the segment ceiling verified by a reflection-call count, a
 * generic-outer-nested type whose reported name drops the type arguments (the
 * fixer's line scan cannot handle the mid-name {@code <...>}), and the
 * {@code prefer.import.nested} classifier paths that need scope a temp-file slice
 * lacks (a same-package classpath type, an uncompiled sibling source file, and
 * the segment ceiling applied to a type-bound root). The routine
 * self-reference, expression-receiver, class-literal, enclosing-name collision,
 * and every-declaration-kind-as-a-same-file-root cases live in
 * {@code cases.clean.java} / {@code cases.in.java}.
 */
public class PreferImportCheckTest {
	@Test
	public void testCompletesOnDeeplyNestedName() throws Exception {
		final var chain = new StringBuilder("a0");
		for (var i = 1; i < 2000; ++i)
			chain.append(".a").append(i);
		final var source = "class T {\n\t" + chain + " field;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(2, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name '" + chain + "'.", violations.getFirst().getMessage());
	}

	@Test
	public void testDeepSelfQualifiedNameSuppressedWithoutReflection() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 0; i < 20; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertEquals(0, ReflectionUtil.classForNameCallCount.get(), "segment ceiling must suppress without a reflection scan");
	}

	@Test
	public void testDepthCapWithTypeRootForcesOldMessage() throws Exception {
		final var name = new StringBuilder("Map");
		for (var i = 1; i <= 17; ++i)
			name.append(".a").append(i);
		final var source = "import java.util.Map;\nclass T {\n\t" + name + " f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name '" + name + "'.", violations.getFirst().getMessage());
	}

	@Test
	public void testEnclosingSelfQualAtCeilingScans() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 1; i <= 16; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertNotEquals(0, ReflectionUtil.classForNameCallCount.get(), "16-dot self-qualified name must be resolved by a reflection scan");
	}

	@Test
	public void testEnclosingSelfQualOverCeilingSuppressesWithoutReflection() throws Exception {
		final var name = new StringBuilder("a");
		for (var i = 1; i <= 17; ++i)
			name.append(".x").append(i);
		final var source = "class a {\n\t" + name + " f;\n}\n";
		ReflectionUtil.clearCache();
		ReflectionUtil.classForNameCallCount.set(0);
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(0, violations.size());
		assertEquals(0, ReflectionUtil.classForNameCallCount.get(), "17-dot self-qualified name must suppress without a reflection scan");
	}

	@Test
	public void testGenericOuterNestedTypeDropsTypeArgumentsFromName() throws Exception {
		final var source = "class T {\n\tjava.util.Map<String, Integer>.Entry e;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(2, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use an import instead of fully qualified name 'java.util.Map.Entry'.", violations.getFirst().getMessage());
	}

	@Test
	public void testJustUnderDepthCapWithTypeRootIsNested() throws Exception {
		final var name = new StringBuilder("Map");
		for (var i = 1; i <= 16; ++i)
			name.append(".a").append(i);
		final var full = name.toString();
		final var source = "import java.util.Map;\nclass T {\n\t" + full + " f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		final var qualifier = full.substring(0, full.lastIndexOf('.'));
		assertEquals("Import the nested type 'a16' instead of qualifying it through '" + qualifier + "'.", violations.getFirst().getMessage());
	}

	@Test
	public void testSamePackageClasspathRootIsNested() throws Exception {
		final var source = "package com.etk2000.checkstyle;\nclass T {\n\tLineText.Foo f;\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferImportCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
		assertEquals("Import the nested type 'Foo' instead of qualifying it through 'LineText'.", violations.getFirst().getMessage());
	}

	@Test
	public void testSiblingSourceFileLowercaseRootSkipsProbe() throws Exception {
		final var dir = Files.createTempDirectory("preferimport-sibling-lower");
		try {
			Files.writeString(dir.resolve("foo.java"), "class foo {\n\tstatic class Nested {}\n}\n");
			final var main = dir.resolve("Main.java");
			Files.writeString(main, "class Main {\n\tfoo.Nested field;\n}\n");
			final var violations = BaseCheckTest.runCheckOnDiskFile(PreferImportCheck.class, main.toFile());
			assertEquals(1, violations.size());
			assertEquals(2, violations.getFirst().getLine());
			assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
			assertEquals("Use an import instead of fully qualified name 'foo.Nested'.", violations.getFirst().getMessage());
		}
		finally {
			Files.deleteIfExists(dir.resolve("foo.java"));
			Files.deleteIfExists(dir.resolve("Main.java"));
			Files.deleteIfExists(dir);
		}
	}

	@Test
	public void testSiblingSourceFileRootIsNested() throws Exception {
		final var dir = Files.createTempDirectory("preferimport-sibling");
		try {
			Files.writeString(dir.resolve("Sibling.java"), "class Sibling {\n\tstatic class Nested {}\n}\n");
			final var main = dir.resolve("Main.java");
			Files.writeString(main, "class Main {\n\tSibling.Nested field;\n}\n");
			final var violations = BaseCheckTest.runCheckOnDiskFile(PreferImportCheck.class, main.toFile());
			assertEquals(1, violations.size());
			assertEquals(2, violations.getFirst().getLine());
			assertEquals(SeverityLevel.WARNING, violations.getFirst().getSeverityLevel());
			assertEquals("Import the nested type 'Nested' instead of qualifying it through 'Sibling'.", violations.getFirst().getMessage());
		}
		finally {
			Files.deleteIfExists(dir.resolve("Sibling.java"));
			Files.deleteIfExists(dir.resolve("Main.java"));
			Files.deleteIfExists(dir);
		}
	}
}