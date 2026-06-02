package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkipResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Direct {@code fix()} tests for {@code PreferImportFixer} paths the
 * {@code StandardCheckTests} pipeline cannot drive.
 */
public class PreferImportFixerTest {
	private static final String FIX_CONTEXT_TOPIC = "preferimportfixcontext";
	private static final String TOPIC = "preferimport";

	private static void writeSiblingFile(Path dir) throws Exception {
		final var dep = TestResources.loadCase(FIX_CONTEXT_TOPIC, "same_package_sibling_dep");
		Files.writeString(dir.resolve("Sibling.java"), String.join("\n", dep.inputLines()));
	}

	private final PreferImportFixer fixer = new PreferImportFixer();

	@Test
	public void testDefaultPackageSiblingCollisionSkips(@TempDir Path dir) throws Exception {
		writeSiblingFile(dir);
		final var main = TestResources.loadSnippet(FIX_CONTEXT_TOPIC, "default_package_sibling_collision_main");
		final var mainFile = dir.resolve("Main.java");
		final var lines = new ArrayList<>(main.inputLines());
		Files.writeString(mainFile, String.join("\n", lines));
		final var target = main.firstTarget();
		final var maskedLines = FqnResolver.computeLineMasks(lines).maskedLines();
		final var resolution = FqnResolver.resolve(lines, maskedLines, "Sibling", mainFile.toString());
		assertEquals("Sibling", resolution.fqn());
		assertEquals(FqnResolver.ResolutionSource.SAME_PACKAGE_SIBLING, resolution.source());
		FixContext.setFilePath(mainFile.toString());
		try {
			final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, target.line(), target.column()));
			assertEquals(SkipMessages.IMPORT_SKIP_NAME_COLLISION, result.reason());
		}
		finally {
			FixContext.clearFilePath();
		}
	}

	@Test
	public void testLeadingBomSkipsUnparseable() throws Exception {
		assertSkipResult(fixer, TOPIC, "leading_bom_unparseable", SkipMessages.IMPORT_SKIP_UNPARSEABLE);
	}

	@Test
	public void testSamePackageSiblingCollisionSkips(@TempDir Path dir) throws Exception {
		writeSiblingFile(dir);
		final var main = TestResources.loadSnippet(FIX_CONTEXT_TOPIC, "same_package_sibling_collision_main");
		final var mainFile = dir.resolve("Main.java");
		final var lines = new ArrayList<>(main.inputLines());
		Files.writeString(mainFile, String.join("\n", lines));
		final var target = main.firstTarget();
		final var maskedLines = FqnResolver.computeLineMasks(lines).maskedLines();
		final var resolution = FqnResolver.resolve(lines, maskedLines, "Sibling", mainFile.toString());
		assertEquals("com.example.Sibling", resolution.fqn());
		assertEquals(FqnResolver.ResolutionSource.SAME_PACKAGE_SIBLING, resolution.source());
		FixContext.setFilePath(mainFile.toString());
		try {
			final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, target.line(), target.column()));
			assertEquals(SkipMessages.IMPORT_SKIP_NAME_COLLISION, result.reason());
		}
		finally {
			FixContext.clearFilePath();
		}
	}

	@Test
	public void testSamePackageSiblingStrips(@TempDir Path dir) throws Exception {
		writeSiblingFile(dir);
		final var main = TestResources.loadSnippet(FIX_CONTEXT_TOPIC, "same_package_sibling_main");
		final var mainFile = dir.resolve("Main.java");
		final var lines = new ArrayList<>(main.inputLines());
		Files.writeString(mainFile, String.join("\n", lines));
		final var target = main.firstTarget();
		FixContext.setFilePath(mainFile.toString());
		try {
			final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, target.line(), target.column()));
			assertEquals(List.of("\tSibling field;"), result.replacement());
			assertTrue(result.importsToAdd().isEmpty(), "same-package strip must not add an import");
		}
		finally {
			FixContext.clearFilePath();
		}
	}

	@Test
	public void testTextBlockMaskResetsAfterClose() throws Exception {
		final var lines = TestResources.readResourceLines("/com/etk2000/checkstyle/inputs/" + TOPIC + "/cases.clean.java");
		final var mask = FqnResolver.computeLineMasks(lines).inTextBlock();
		var opener = -1;
		for (var i = 0; i < lines.size(); ++i) {
			if (lines.get(i).stripTrailing().endsWith("\"\"\"")) {
				opener = i;
				break;
			}
		}
		assertNotEquals(-1, opener, "text-block opener not found in cases.clean.java");
		assertFalse(mask[opener], "text-block opener line begins outside the block");
		assertTrue(mask[opener + 1], "body line begins inside the text block");
		assertTrue(mask[opener + 2], "closing line begins inside the text block");
		assertFalse(mask[opener + 3], "line after the closing triple-quote must not stay masked");
	}

	@Test
	public void testUnparseableFileSkips() throws Exception {
		assertSkipResult(fixer, TOPIC, "unparseable", SkipMessages.IMPORT_SKIP_UNPARSEABLE);
	}
}