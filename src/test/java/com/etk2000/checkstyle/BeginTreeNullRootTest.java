package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.puppycrawl.tools.checkstyle.DetailAstImpl;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Proves every check that overrides {@code beginTree} tolerates the null root
 * checkstyle hands it for a comments-only or empty file (which has no
 * compilation unit), both via a direct {@code beginTree(null)} call and
 * end-to-end through the checker. {@code PreferStaticImportCheck} and
 * {@code PreferStaticImportConstantCheck} dereference the root, so they also get
 * a guard-regression test asserting their {@code collectImports} throws on a
 * null root, proving the {@code beginTree} guard is load-bearing rather than
 * decorative.
 */
public class BeginTreeNullRootTest {
	private static final String COMMENTS_ONLY = "// just a comment";

	/**
	 * The end-to-end roster: {@link #beginTreeChecks} plus every check whose
	 * {@code beginTree} ignores the root but reads the file contents, which only a
	 * checker-driven run supplies. Such a check is null-root-safe where it matters
	 * and would fail a bare-instance call for an unrelated reason.
	 */
	@CheckReturnValue
	@Nonnull
	static Stream<Class<? extends AbstractCheck>> beginTreeCheckerDrivenChecks() {
		return Stream.concat(beginTreeChecks(), Stream.of(PreferBulkOperationCheck.class));
	}

	@CheckReturnValue
	@Nonnull
	static Stream<Class<? extends AbstractCheck>> beginTreeChecks() {
		return Stream.of(
				MultilineCallFormattingCheck.class,
				PreferCollectionInterfaceCheck.class,
				PreferExactAssertionCheck.class,
				PreferLambdaCheck.class,
				PreferSpecificApiCheck.class,
				PreferStandardCharsetsCheck.class,
				PreferStaticImportCheck.class,
				PreferStaticImportConstantCheck.class,
				PreferVarCheck.class,
				RedundantArrayCreationCheck.class
		);
	}

	@MethodSource("beginTreeChecks")
	@ParameterizedTest
	public void beginTreeNullRootDoesNotThrow(@Nonnull Class<? extends AbstractCheck> checkClass) throws Exception {
		final var check = checkClass.getDeclaredConstructor().newInstance();
		assertDoesNotThrow(() -> check.beginTree(null));
	}

	@Test
	public void commentsOnlyContentHasNullRoot() throws Exception {
		// pins the premise of commentsOnlyFileYieldsNoViolations: checkstyle hands beginTree a
		// null root for comments-only content, so that test exercises the guard rather than a
		// vacuous empty-root path
		final var tempFile = File.createTempFile("begintree-null-root", ".java");
		try {
			Files.writeString(tempFile.toPath(), COMMENTS_ONLY);
			assertNull(JavaParser.parseFile(tempFile, JavaParser.Options.WITHOUT_COMMENTS));
		}
		finally {
			tempFile.delete();
		}
	}

	@MethodSource("beginTreeCheckerDrivenChecks")
	@ParameterizedTest
	public void commentsOnlyFileYieldsNoViolations(@Nonnull Class<? extends AbstractCheck> checkClass) throws Exception {
		assertEquals(List.of(), BaseCheckTest.runCheckInline(checkClass, COMMENTS_ONLY));
	}

	@Test
	public void preferStaticImportCollectImportsSkipsChildlessStaticImport() {
		// checkstyle never emits a childless STATIC_IMPORT, but the guard must hold if one is built
		final var root = new DetailAstImpl();
		final var staticImport = new DetailAstImpl();
		staticImport.setType(TokenTypes.STATIC_IMPORT);
		root.addChild(staticImport);
		assertDoesNotThrow(() -> new PreferStaticImportCheck().collectImports(root));
	}

	@Test
	public void preferStaticImportCollectImportsSkipsPathlessStaticImport() {
		// a STATIC_IMPORT carrying only LITERAL_STATIC with no path sibling
		final var root = new DetailAstImpl();
		final var staticImport = new DetailAstImpl();
		staticImport.setType(TokenTypes.STATIC_IMPORT);
		final var literalStatic = new DetailAstImpl();
		literalStatic.setType(TokenTypes.LITERAL_STATIC);
		staticImport.addChild(literalStatic);
		root.addChild(staticImport);
		assertDoesNotThrow(() -> new PreferStaticImportCheck().collectImports(root));
	}

	@Test
	public void preferStaticImportCollectImportsThrowsOnNullRoot() {
		assertThrows(NullPointerException.class, () -> new PreferStaticImportCheck().collectImports(null));
	}

	@Test
	public void preferStaticImportConstantCollectImportsThrowsOnNullRoot() {
		assertThrows(NullPointerException.class, () -> new PreferStaticImportConstantCheck().collectImports(null));
	}
}