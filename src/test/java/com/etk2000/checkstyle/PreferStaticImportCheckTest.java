package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

public class PreferStaticImportCheckTest {
	private static final int NESTING_DEPTH = 100_000;
	private static final String OBJECTS_FILE = "preferstaticimport/cases.objects.in.java";
	private static final String SDK = String.valueOf(Integer.MAX_VALUE);
	private static final String SIBLING_SHADOW_FILE = "preferstaticimport/siblingshadow/cases.clean.java";

	/**
	 * The real candidate table cannot collide, but the builder takes its table as a parameter, so a
	 * synthetic one reaches the guard without touching production state.
	 */
	@Test
	public void testCollidingSimpleNamesAreRejected() {
		final var colliding = Map.of("a.b.Objects", Map.<String, Integer>of(), "c.d.Objects", Map.<String, Integer>of());
		final var thrown = assertThrows(
				IllegalStateException.class,
				() -> PreferStaticImportCheck.buildSimpleToFqcn(colliding)
		);
		assertTrue(thrown.getMessage().startsWith("candidate classes share a simple name: "));
		assertTrue(thrown.getMessage().contains("a.b.Objects"));
		assertTrue(thrown.getMessage().contains("c.d.Objects"));
	}

	@Test
	public void testDeeplyNestedTreeDoesNotOverflow() throws Exception {
		var deepest = new DetailAstImpl();
		deepest.setType(TokenTypes.IDENT);
		deepest.setText("x");
		for (var i = 0; i < NESTING_DEPTH; ++i) {
			final var wrapper = new DetailAstImpl();
			wrapper.setType(TokenTypes.LNOT);
			wrapper.setText("!");
			wrapper.addChild(deepest);
			deepest = wrapper;
		}

		final var root = new DetailAstImpl();
		root.setType(TokenTypes.COMPILATION_UNIT);
		root.setText("COMPILATION_UNIT");
		root.addChild(deepest);

		final var tempFile = File.createTempFile("checkstyle-deep-walk", ".java");
		try {
			Files.writeString(tempFile.toPath(), "// contents are irrelevant; only the path is read");
			final var check = new PreferStaticImportCheck();
			check.setFileContents(new FileContents(new FileText(tempFile, StandardCharsets.UTF_8.name())));
			assertDoesNotThrow(() -> check.beginTree(root));
		}
		finally {
			tempFile.delete();
		}
	}

	@Test
	public void testDefaultMinSdkFiresAllRules() throws Exception {
		assertEquals(12, BaseCheckTest.runCheck(PreferStaticImportCheck.class, OBJECTS_FILE).size());
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
	public void testNonPositiveMinOccurrencesIsRejected(int minOccurrences) {
		final var check = new PreferStaticImportCheck();
		final var thrown = assertThrows(IllegalArgumentException.class, () -> check.setMinOccurrences(minOccurrences));
		assertEquals("minOccurrences must be positive, got " + minOccurrences, thrown.getMessage());
	}

	@Test
	public void testOneMinOccurrencesIsAccepted() {
		final var check = new PreferStaticImportCheck();
		assertDoesNotThrow(() -> check.setMinOccurrences(1));
	}

	@Test
	public void testSiblingShadowCleanViaWildcard() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, SIBLING_SHADOW_FILE, "minSdk", SDK).isEmpty());
	}
}