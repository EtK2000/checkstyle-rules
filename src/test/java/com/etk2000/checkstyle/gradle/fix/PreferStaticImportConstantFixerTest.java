package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PreferStaticImportConstantFixerTest {
	private final CheckstyleFixer fixer = new PreferStaticImportConstantFixer();

	@TempDir
	Path tempDir;

	@AfterEach
	public void cleanup() {
		FixContext.clearFilePath();
	}

	@Test
	public void testAnnotationArgContainingBarePrivateWordDoesNotMisidentifyVisibility() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@MyAnnotation( private ) static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testAnnotationArgContainingEqualsBeforeRealEqualsHonorsPrivate() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@SuppressWarnings(value = \"rawtypes\") private static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testAnnotationArgContainingPrivateStringDoesNotMisidentifyVisibility() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@SuppressWarnings(\"private-key\") static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testAnnotationArgUnbalancedParenInsideStringDoesNotMisidentifyVisibility() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@Description(\"foo) private bar(\") static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testCanonicalAliasBlankAboveOnlyDeletesLineOnly() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate int a;",
				"",
				"\tprivate static final int X = Foo.X;",
				"\tprivate int b;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCanonicalAliasNoBlanksDeletesLineOnly() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate int a;",
				"\tprivate static final int X = Foo.X;",
				"\tprivate int b;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCanonicalAliasWithSurroundingBlanksCollapsesPair() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate int a;",
				"",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tprivate int b;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(6, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCharLiteralInMethodBodyIsPreservedDuringRewrite() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\tchar q = '\\''; return Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\tchar q = '\\''; return X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitAssignmentSharingLineWithTrailingStatementReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X; int y = 0;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitAssignmentSpanningMultipleLinesReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX =",
				"\t\t\t\tFoo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitAssignmentWithMismatchedQualifierReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tOther.X = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitBlankFinalIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithCommentOnStaticCloserLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t/* close note */ }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithCommentOnStaticOpenerLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic { // open note",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic { // open note"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithDeclAfterStaticBlockOnSameLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t} private static final int Y = 0;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithDeclBeforeStaticBlockOnSameOpenerLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tprivate int Z = 7; static {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tprivate int Z = 7; static {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithLeadingCommentInStaticBlockKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\t// important context: do not drop",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(
				List.of("", "\tstatic {", "\t\t// important context: do not drop"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithLeadingCommentOnCinitLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\t/* lead */ X = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithNoMatchingAssignmentReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"\tprivate static int Y;",
				"",
				"\tstatic {",
				"\t\tY = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitBlankFinalWithQualifiedAssignmentIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tT.X = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithStraySemicolonsInStaticBlockIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\t;",
				"\t\tX = Foo.X;",
				"\t\t;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(9, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithTrailingCommentInStaticBlockKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t\t// trailing note",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitBlankFinalWithTrailingCommentOnCinitLineKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X; // trailing",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of("", "\tstatic {"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitDefaultPackageBareLhsIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 26));
		assertEquals(2, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitDefaultPackageFqnLhsAttemptReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tx.T.X = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitFindFieldDefDisambiguatesByColumnOnSameLineNestedClasses() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { static class A { private static final int X; static { X = Foo.X; } } static class B { private static final int X; static { X = Foo.X; } } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 52));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
		assertEquals(
				List.of("class T { static class A {   } static class B { private static final int X; static { X = Foo.X; } } }"),
				result.replacement()
		);
	}

	@Test
	public void testCinitFixerWithColumnOffIdentReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 0));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitFqnLhsAssignmentIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tx.T.X = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitFqnLhsWithLeadingNonAssignStatementIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tSystem.out.println();",
				"\t\tx.T.X = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of("", "\tstatic {", "\t\tSystem.out.println();"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitFqnLhsWithWrongPackageReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\ty.T.X = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitMultiLineAnnotatedBlankFinalWithEqualsInAnnotationArgIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@SuppressWarnings(value = \"unused\")",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(3, result.startLine());
		assertEquals(8, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitMultiLineAnnotatedFieldWithBlockCommentBeforeAnnotationIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t/* note */ @SuppressWarnings(\"u\")",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(3, result.startLine());
		assertEquals(8, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitMultiLineAnnotatedFieldWithContentBeforeAnnotationReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tint z = 0; @SuppressWarnings(\"u\")",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitMultiLineAnnotatedFieldWithZeroIndentAnnotationIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"@SuppressWarnings(\"unused\")",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(3, result.startLine());
		assertEquals(8, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitMultiLineBlankFinalWithTrailingLineCommentIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X; // legacy alias",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitMultipleAssignmentsToSameFieldReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t\tX = Foo.Y;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitNonPrivateBlankFinalReturnsVisibilitySkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tpublic static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 25));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testCinitQualifiedLhsWithInternalWhitespaceIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tT . X = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of(""), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndCinitIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { private static final int X; static { X = Foo.X; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 35));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {   }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndCinitWithAnnotatedFieldIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { @SuppressWarnings(\"unused\") private static final int X; static { X = Foo.X; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 63));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {   }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndCinitWithAnnotationOnPriorLineReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@SuppressWarnings(\"unused\")",
				"\tprivate static final int X; static { X = Foo.X; }",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitSameLineDeclAndCinitWithCommentInsideStaticBlockKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { private static final int X; static { /* note */ X = Foo.X; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 35));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {  static { /* note */  } }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndCinitWithExtraStatementInStaticBlockKeepsBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { private static final int X; static { X = Foo.X; int y = 0; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 35));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {  static {  int y = 0; } }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndFqnCinitIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { private static final int X; static { x.T.X = Foo.X; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 35));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {   }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitSameLineDeclAndQualifiedCinitIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T { private static final int X; static { T.X = Foo.X; } }"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 35));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(List.of("class T {   }"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitUnresolvableClassReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = NotImported.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP, skip.reason());
	}

	@Test
	public void testCinitWithConflictingStaticImportReturnsConflictSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import static other.Bar.X;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT, skip.reason());
	}

	@Test
	public void testCinitWithLeadingNonAssignStatementIsAutoFixed() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tSystem.out.println();",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(List.of("", "\tstatic {", "\t\tSystem.out.println();"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitWithMethodCallLhsReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tfoo().X = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitWithNestedSameNameFieldAndNoMatchingCinitReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"\tstatic class Inner {",
				"\t\tprivate static final int X;",
				"\t}",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 5, 27));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitWithNonAliasRhsReturnsCinitSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"",
				"\tstatic {",
				"\t\tX = 42;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CINIT, skip.reason());
	}

	@Test
	public void testCinitWithSiblingFieldsBetweenFieldAndCinitPreservesThem() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"\tprivate int other1;",
				"\tprivate int other2;",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(9, result.endLine());
		assertEquals(
				List.of("\tprivate int other1;", "\tprivate int other2;", ""),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testCinitWithTextBlockSiblingFieldPreservesContent() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X;",
				"\tprivate static final String DOC = \"\"\"",
				"\t\t\tX = Bar.X",
				"\t\t\t\"\"\";",
				"",
				"\tstatic {",
				"\t\tX = Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(10, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final String DOC = \"\"\"",
						"\t\t\tX = Bar.X",
						"\t\t\t\"\"\";",
						""
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testColumnNegativeReturnsNull() {
		final var lines = new ArrayList<>(List.of("class T {", "\tint x;", "}"));
		assertNull(fixer.fix(lines, 1, -1));
	}

	@Test
	public void testColumnPastEolReturnsNull() {
		final var lines = new ArrayList<>(List.of("class T {", "\tint x;", "}"));
		assertNull(fixer.fix(lines, 1, 999));
	}

	@Test
	public void testConflictingStaticImportReturnsConflictSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import static other.Bar.X;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT, skip.reason());
	}

	@Test
	public void testConflictScanIgnoresMalformedStaticImport() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import static .X;",
				"import static foo.X.;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testDefaultPackageEmitsSimpleNameOnly() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Foo.java"), "class Foo {}");
		final var lines = new ArrayList<>(List.of(
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 1, 1));
		assertEquals(1, result.startLine());
		assertEquals(1, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static Foo.X"), result.importsToAdd());
	}

	@Test
	public void testEqualsInsideCommentBeforeRealEqualsResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X /* = note */ = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testExplicitImportWinsOverSibling() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Foo.java"), "class Foo {}");
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import other.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static other.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testExplicitImportWinsOverWildcard() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import other.Foo;",
				"import wild.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static other.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFindShadowKindBailsOnAssertionErrorFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				0,
				0,
				ls -> {
					throw new AssertionError("parser bug");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnCheckstyleExceptionFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				0,
				0,
				ls -> {
					throw new CheckstyleException("bad input");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnRuntimeExceptionFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				0,
				0,
				ls -> {
					throw new IllegalStateException("parser internal NPE");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindShadowKindBailsOnStackOverflowErrorFromParser() {
		final var result = PreferStaticImportConstantFixer.findShadowKindUsing(
				List.of(),
				"X",
				0,
				0,
				ls -> {
					throw new StackOverflowError("deep recursion");
				}
		);
		assertEquals("potential shadow (file does not parse cleanly)", result);
	}

	@Test
	public void testFindStatementEndAcceptsTrailingBlockComment() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X; /* tail */",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFindStatementEndHandlesEscapedQuoteInStringLiteral() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tString A = \"a\\\";b\"; private static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testFindStatementEndSkipsBlockCommentSpanningLines() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = /* multi-line",
				"\t\tcomment ; ignore */ Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFindStatementEndSkipsEscapedQuoteInCharLiteral() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tchar Q = '\\''; private static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testFindStatementEndSkipsLineCommentOnSameLine() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X; // a trailing line comment",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFindStatementEndSkipsSemicolonInCharLiteral() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tchar SEMI = ';'; private static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testFindStatementEndSkipsSemicolonInStringLiteral() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tString A = \"a;b;c\"; private static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testFullyQualifiedChainUsageInMethodBodyRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = pkg.Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn pkg.Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static pkg.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testFullyQualifiedRhsResolvesAsIs() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = pkg.Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static pkg.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testImportInsideTextBlockIgnoredForFqcnResolution() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tString doc = \"\"\"",
				"\t\timport com.fake.Foo;",
				"\t\t\"\"\";",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 5, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP, skip.reason());
	}

	@Test
	public void testImportLineWithLeadingBlockCommentResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"/* legacy */ import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testImportLineWithTrailingCommentResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo; // historical note",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testImportLineWithUrlInBlockCommentResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo; /* see https://example.com */",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testLeadingAnnotationAboveReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@Deprecated",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 1));
	}

	@Test
	public void testLeadingJavadocAboveReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t/** Important note. */",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 1));
	}

	@Test
	public void testLeadingJavadocContinuationAboveReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t/**",
				"\t * doc.",
				"\t */",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 6, 1));
	}

	@Test
	public void testLeadingLineCommentAboveReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t// explains why",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 1));
	}

	@Test
	public void testLeadingStarWithEqualsDoesNotTriggerSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tString a = \"x\"",
				"\t\t* /* assigned */ \"y=\";",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testLeadingStarWithSemicolonDoesNotTriggerSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tint z = 1",
				"\t\t* 2;",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testLineIndexNegativeReturnsNull() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		assertNull(fixer.fix(lines, -1, 0));
	}

	@Test
	public void testLineIndexOutOfRangeReturnsNull() {
		final var lines = new ArrayList<>(List.of("class T {}"));
		assertNull(fixer.fix(lines, 99, 0));
	}

	@Test
	public void testLowercaseLocalFieldNameRewrittenToUppercaseConstantName() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int max = Foo.MAX;",
				"",
				"\tint use() {",
				"\t\treturn max;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn MAX;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.MAX"), result.importsToAdd());
	}

	@Test
	public void testMalformedImportsAreIgnoredDuringResolution() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import .Foo;",
				"import foo.;",
				"import wild.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 1));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static wild.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMalformedWildcardImportSkipped() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import .*;",
				"import foo.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMarkerAnnotationOnSameLineAsPrivateFixerSucceeds() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@Deprecated private static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiLineAliasDeletesAllLines() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X =",
				"\t\t\tFoo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiLineAliasWithWhitespaceAroundDot() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo",
				"\t\t\t.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultipleAnnotationsOnSameLineFixerSucceeds() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@Deprecated @SuppressWarnings(\"unused\") private static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultipleQualifiedUsagesOnSameLineAllRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn Foo.X + Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X + X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultipleUsageLinesWithIntermediateLinePreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint a() {",
				"\t\treturn Foo.X;",
				"\t}",
				"",
				"\tint b() {",
				"\t\treturn Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(10, result.endLine());
		assertEquals(
				List.of("", "\tint a() {", "\t\treturn X;", "\t}", "", "\tint b() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultipleWildcardsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.*;",
				"import bar.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 1));
	}

	@Test
	public void testMultiStatementOnAliasLineReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X; int leftover = 7;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testMultiStatementWithMidLineBlockCommentReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class Holder { private static final int X = Foo.X; /* note */ }"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 16));
	}

	@Test
	public void testMultiVarAnnotationOnDeclLineIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t@Deprecated private static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 38));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\t@Deprecated private static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarAnnotationOnDeclLineIsPreservedInRebuildLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t@Deprecated private static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 49));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\t@Deprecated private static final int X = Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarAnnotationOnFirstLineMultiLineIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t@Deprecated private static final int X = Foo.X,",
				"\t\t\tY = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 3));
		assertEquals(4, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\t@Deprecated private static final int X = Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarAnnotationWithBodyOnDeclLineIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t@SuppressWarnings(\"unused\") private static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 54));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\t@SuppressWarnings(\"unused\") private static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarArrayInitializerInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int[] X = Foo.X, Y = {1, 2, 3};",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 28));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int[] Y = {1, 2, 3};"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarArrayTypeRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int[] X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 28));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(List.of("\tprivate static final int[] Y = Bar.Y;"), result.replacement());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarBlockCommentInPrefixIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate /* note */ static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 37));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate /* note */ static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarBlockCommentInsideKeptRhsIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X, Y = Bar /* note */ . Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = Bar /* note */ . Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarCharLiteralInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = ',';",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = ',';"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarCharLiteralWithEscapeInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = '\\n';",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = '\\n';"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarCommentOnDeclLineReturnsMultiVarSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X, Y = Bar.Y; // important note",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR, skip.reason());
	}

	@Test
	public void testMultiVarConflictingStaticImportReturnsConflictSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"import static other.Other.X;",
				"class T {",
				"\tprivate static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 5, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_CONFLICT, skip.reason());
	}

	@Test
	public void testMultiVarFourVariablesRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C, D = Foo.D;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int B = Foo.B, C = Foo.C, D = Foo.D;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.A"), result.importsToAdd());
	}

	@Test
	public void testMultiVarFourVariablesRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C, D = Foo.D;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 59));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.D"), result.importsToAdd());
	}

	@Test
	public void testMultiVarFqcnRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = com.foo.Foo.X, Y = com.foo.Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 26));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = com.foo.Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static com.foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarFqcnRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = com.foo.Foo.X, Y = com.foo.Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 45));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = com.foo.Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static com.foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarGenericTypeRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"import java.util.Map;",
				"class T {",
				"\tprivate static final Map<String, Integer> X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 43));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final Map<String, Integer> Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarGenericTypeRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"import java.util.Map;",
				"class T {",
				"\tprivate static final Map<String, Integer> X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 54));
		assertEquals(5, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final Map<String, Integer> X = Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarMethodCallRhsInNonAliasSegmentDoesNotConfuseDetection() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = compute(1, 2), Y = Foo.Y;",
				"\tstatic int compute(int a, int b) { return a + b; }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 45));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = compute(1, 2);"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarMixedAliasRemovesAliasVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = 0, Y = Foo.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 33));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = 0;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarMultiLineBlockCommentAboveDeclDoesNotConfuseMask() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t/* block",
				"\t   comment with \"\"\" markers */",
				"\tprivate static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 6, 26));
		assertEquals(6, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarNonPrivateReturnsVisibilitySkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tstatic final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 18));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testMultiVarOnContinuationLineRemovesContinuationVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X,",
				"\t\t\tY = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 3));
		assertEquals(4, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarOnFirstLineSpanningSecondRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X,",
				"\t\t\tY = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(4, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarQualifiedAnnotationOnDeclLineIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\t@java.lang.SuppressWarnings(\"unused\") private static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 64));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\t@java.lang.SuppressWarnings(\"unused\") private static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarRenamedAliasOnLastVariableRewritesUsages() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X, RENAMED = Bar.Y;",
				"",
				"\tint use() {",
				"\t\treturn X + RENAMED;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 37));
		assertEquals(4, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final int X = Foo.X;",
						"",
						"\tint use() {",
						"\t\treturn X + Y;"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarRenamedAliasRewritesUsages() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X, Y = Bar.Y;",
				"",
				"\tint use() {",
				"\t\treturn RENAMED + Y;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(4, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final int Y = Bar.Y;",
						"",
						"\tint use() {",
						"\t\treturn X + Y;"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarRenamedAliasShadowedByLocalReturnsShadowSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X, Y = Bar.Y;",
				"",
				"\tint use() {",
				"\t\tint RENAMED = 5;",
				"\t\treturn RENAMED + Y;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 4, 26));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("local variable"),
				skip.reason()
		);
	}

	@Test
	public void testMultiVarSingleLineRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = Bar.Y;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarSingleLineRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = Foo.X, Y = Bar.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 37));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarSingleLineTripleQuoteSequenceNotTreatedAsTextBlock() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"\"\"oneliner\"\"\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = \"\"\"oneliner\"\"\";"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarStringLiteralInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"hello\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = \"hello\";"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarStringLiteralWithCommaInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"hello, world\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = \"hello, world\";"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarStringLiteralWithEscapedQuoteInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"a\\\"b\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = \"a\\\"b\";"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarStringLiteralWithSemicolonInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"a;b\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final Object Y = \"a;b\";"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockClosesAndContinuesOnSameLine() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final String S = \"\"\"",
				"\t\thello",
				"\t\t\"\"\", Y = Foo.Y;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 7));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final String S = \"\"\"",
						"\t\thello",
						"\t\t\"\"\";"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.Y"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"\"\"",
				"\t\t\ta;b,c",
				"\t\t\t\"\"\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final Object Y = \"\"\"",
						"\t\t\ta;b,c",
						"\t\t\t\"\"\";"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockWithBackslashEscapeInContentIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"\"\"",
				"\t\ta\\\\nb",
				"\t\t\"\"\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final Object Y = \"\"\"",
						"\t\ta\\\\nb",
						"\t\t\"\"\";"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockWithFinalKeywordInContentDoesNotConfuseFindDeclarationStart() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final String DOC = \"\"\"",
				"\t\tpublic static final int Z = 0;",
				"\t\t\"\"\";",
				"\tprivate static final int A = Foo.A, B = Foo.B;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 6, 26));
		assertEquals(6, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("\tprivate static final int B = Foo.B;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.A"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockWithInternalQuotesInSiblingIsPreservedInRebuild() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"\"\"",
				"\t\t\the said \"hello\".",
				"\t\t\t\"\"\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final Object Y = \"\"\"",
						"\t\t\the said \"hello\".",
						"\t\t\t\"\"\";"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarTextBlockWithLineCommentMarkerInContentDoesNotBail() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"\"\"",
				"\t\tcode with // marker",
				"\t\t\"\"\";",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 29));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of(
						"\tprivate static final Object Y = \"\"\"",
						"\t\tcode with // marker",
						"\t\t\"\"\";"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarThreeVariablesOnLastContinuationLineRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A,",
				"\t\t\tB = Foo.B,",
				"\t\t\tC = Foo.C;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 5, 3));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final int A = Foo.A, B = Foo.B;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.C"), result.importsToAdd());
	}

	@Test
	public void testMultiVarThreeVariablesOnMiddleContinuationLineRemovesMiddleVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A,",
				"\t\t\tB = Foo.B,",
				"\t\t\tC = Foo.C;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 3));
		assertEquals(3, result.startLine());
		assertEquals(5, result.endLine());
		assertEquals(
				List.of("\tprivate static final int A = Foo.A, C = Foo.C;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.B"), result.importsToAdd());
	}

	@Test
	public void testMultiVarThreeVariablesRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 26));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int B = Foo.B, C = Foo.C;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.A"), result.importsToAdd());
	}

	@Test
	public void testMultiVarThreeVariablesRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 48));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int A = Foo.A, B = Foo.B;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.C"), result.importsToAdd());
	}

	@Test
	public void testMultiVarThreeVariablesRemovesMiddleVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int A = Foo.A, B = Foo.B, C = Foo.C;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 37));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertEquals(
				List.of("\tprivate static final int A = Foo.A, C = Foo.C;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.B"), result.importsToAdd());
	}

	@Test
	public void testMultiVarUnresolvableClassReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = NotImported.X, Y = Foo.Y;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 26));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP, skip.reason());
	}

	@Test
	public void testMultiVarUrlInStringLiteralBailsConservatively() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final Object X = Foo.X, Y = \"https://example.com\";",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 29));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_MULTI_VAR, skip.reason());
	}

	@Test
	public void testMultiVarWithParensRemovesFirstVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = (Foo.X), Y = (Bar.Y);",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 26));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate static final int Y = (Bar.Y);"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testMultiVarWithParensRemovesLastVariable() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import foo.Bar;",
				"class T {",
				"\tprivate static final int X = (Foo.X), Y = (Bar.Y);",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 39));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertEquals(
				List.of("\tprivate static final int X = (Foo.X);"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Bar.Y"), result.importsToAdd());
	}

	@Test
	public void testNestedAnnotationArgDoesNotMisidentifyVisibility() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@MyAnno(@Other(\"private\")) static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testNestedClassChainUsageInMethodBodyRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Outer;",
				"class T {",
				"\tprivate static final int X = Outer.Inner.X;",
				"",
				"\tint use() {",
				"\t\treturn Outer.Inner.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Outer.Inner.X"), result.importsToAdd());
	}

	@Test
	public void testNestedClassRhsResolvesViaSimpleClass() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Outer;",
				"class T {",
				"\tprivate static final int X = Outer.Inner.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Outer.Inner.X"), result.importsToAdd());
	}

	@Test
	public void testNestedClassRhsUnresolvedOuterReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Outer.Inner.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testNestedTypeImportResolvesToFullPath() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Bar.Inner;",
				"class T {",
				"\tprivate static final int X = Inner.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Bar.Inner.X"), result.importsToAdd());
	}

	@Test
	public void testNoMethodBodyUsageDeletesFieldOnly() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn 0;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testNonConflictingStaticImportSucceeds() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import static other.Bar.Y;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testNoStatementTerminatorAcrossMultipleLinesSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X =",
				"\t\t\tFoo.X"
		));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testNoStatementTerminatorSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.X"
		));
		assertNull(fixer.fix(lines, 2, 1));
	}

	@Test
	public void testPackageLineWithTrailingCommentRecognized() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Foo.java"), "class Foo {}");
		final var lines = new ArrayList<>(List.of(
				"package foo.bar; // a trailing comment",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.bar.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testPackagePrivateAliasReturnsVisibilitySkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tstatic final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testPackageWithInternalWhitespaceIsSanitized() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Foo.java"), "class Foo {}");
		final var lines = new ArrayList<>(List.of(
				"package foo . bar;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.bar.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testParentlessFilePathFallsThroughGracefully() {
		FixContext.setFilePath("T.java");
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testParseAliasDotAtEofSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testParseAliasEqualsAtEndOfLineSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X =;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testParseAliasNoEqualsReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X /* no = */ Foo.X;"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testPrivateInsideCommentDoesNotMisidentifyVisibility() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tpublic /* private */ static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testProtectedAliasReturnsVisibilitySkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprotected static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testPublicAliasReturnsVisibilitySkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tpublic static final int X = Foo.X;",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_VISIBILITY, skip.reason());
	}

	@Test
	public void testQualifiedUsageBeforeFieldDeclIsRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tint use() {",
				"\t\treturn Foo.X;",
				"\t}",
				"",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 7, 1));
		assertEquals(4, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(
				List.of("\t\treturn X;", "\t}", ""),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideBlockCommentIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\t/** See {@link Foo#X} for details. */",
				"\tint use() { return 0; }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideBlockCommentSpanningLinesIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\t/*",
				"\t * mentions Foo.X here",
				"\t */",
				"\tint use() { return 0; }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideLineCommentIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn 0; // see Foo.X above",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideStringLiteralIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tString use() {",
				"\t\treturn \"Foo.X\";",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideTextBlockIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tString use() {",
				"\t\treturn \"\"\"",
				"\t\t\tdoes Foo.X stuff",
				"\t\t\t\"\"\";",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageInsideTextBlockWithBackslashEscapeIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tString use() {",
				"\t\treturn \"\"\"",
				"\t\t\tRENAMED \\\"escaped\\\" and Foo.X stuff",
				"\t\t\t\"\"\";",
				"\t}",
				"\tint val() { return RENAMED; }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(10, result.endLine());
		assertEquals(
				List.of(
						"",
						"\tString use() {",
						"\t\treturn \"\"\"",
						"\t\t\tRENAMED \\\"escaped\\\" and Foo.X stuff",
						"\t\t\t\"\"\";",
						"\t}",
						"\tint val() { return X; }"
				),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsagePrecededByDotOnDifferentObjectNotRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use(Other other) {",
				"\t\treturn Foo.X + other.Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use(Other other) {", "\t\treturn X + other.Foo.X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testQualifiedUsageWithLongerSuffixIsNotRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn Foo.X + Foo.XLong;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X + Foo.XLong;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRenamedAliasOwnDeclarationIsNotItsOwnShadow() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED =",
				"\t\t\tFoo.X;",
				"",
				"\tint use() {",
				"\t\treturn RENAMED;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(7, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRenamedAliasReferencedInMethodBodyRewrittenToConstantName() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn RENAMED;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRenamedAliasReferencedInStringLiteralIsPreserved() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tString use() {",
				"\t\treturn \"RENAMED\";",
				"\t}",
				"\tint val() { return RENAMED; }",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(8, result.endLine());
		assertEquals(
				List.of("", "\tString use() {", "\t\treturn \"RENAMED\";", "\t}", "\tint val() { return X; }"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRenamedAliasWithBothQualifiedAndLocalUsagesRewritten() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn RENAMED + Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X + X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRenamedAliasWithUnparseableFileBailsConservatively() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn ((;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted(
						"potential shadow (file does not parse cleanly)"
				),
				skip.reason()
		);
	}

	@Test
	public void testRhsArithmeticSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X + 1;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testRhsBareIdentReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testRhsCommentBetweenClassAndDotResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo /* mid */ . X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRhsDotFollowedByLiteralReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.42;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testRhsExcessClosingParensReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X);",
				"}"
		));
		assertNull(fixer.fix(lines, 3, 1));
	}

	@Test
	public void testRhsFivefoldNestedParensResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = (((((Foo.X)))));",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRhsLiteralReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = 42;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testRhsMismatchedParensReturnsNull() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = (Foo.X;",
				"}"
		));
		assertNull(fixer.fix(lines, 3, 1));
	}

	@Test
	public void testRhsNestedParensResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = ((Foo.X));",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRhsParenthesizedResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = (Foo.X);",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testRhsParenWrappedBareIdentSkips() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = (Foo);",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testRhsTripleNestedParensResolves() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = (((Foo.X)));",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSameClassStaticImportAlreadyPresentDoesNotConflict() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import static foo.Foo.X;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSameClassUsedInFieldAndQualifiedMethodBodyRewritesUsage() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"",
				"\tint use() {",
				"\t\treturn Foo.X;",
				"\t}",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(6, result.endLine());
		assertEquals(
				List.of("", "\tint use() {", "\t\treturn X;"),
				result.replacement()
		);
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSamePackageResolutionUsesSiblingFile() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Helper.java"), "package x.y; class Helper { static final int MAX = 100; }");
		final var lines = new ArrayList<>(List.of(
				"package x.y;",
				"class T {",
				"\tprivate static final int MAX = Helper.MAX;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 2, 1));
		assertEquals(2, result.startLine());
		assertEquals(2, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static x.y.Helper.MAX"), result.importsToAdd());
	}

	@Test
	public void testSamePackageResolutionWithoutSiblingReturnsSkip() {
		final var lines = new ArrayList<>(List.of(
				"package x.y;",
				"class T {",
				"\tprivate static final int X = SiblingClass.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testShadowingArrayTypeLocalSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\tint[] RENAMED = new int[5];",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("local variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingCatchParameterSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\ttry {} catch (Exception RENAMED) {}",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("catch parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingConstructorParameterSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tT(int RENAMED) {}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("constructor parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingForEachVarSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use(int[] arr) {",
				"\t\tfor (var RENAMED : arr) {}",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("for-each variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingForInitSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\tfor (int RENAMED = 0; RENAMED < 10; ++RENAMED) {}",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("for-loop variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingGenericReferenceTypeLocalSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\tMap<String, Integer> RENAMED = null;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("local variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingLambdaMultiParamWithoutTypesSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\tdo2((a, RENAMED) -> RENAMED + 1);",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("lambda parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingLambdaSingleParamSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() {",
				"\t\tjava.util.stream.Stream.of(1).map(RENAMED -> RENAMED + 1);",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("lambda parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingLocalDeclarationSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use() {",
				"\t\tint RENAMED = 5;",
				"\t\treturn RENAMED;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("local variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingNestedFieldWithSameNameSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tstatic class Inner {",
				"\t\tint RENAMED;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("another field with the same name"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingNestedGenericReferenceTypeLocalSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use(Map<String, List<Integer>> RENAMED) {",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("method parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingParameterSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use(int RENAMED) {",
				"\t\treturn RENAMED;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("method parameter"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingReferenceTypeLocalSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tint use() {",
				"\t\tFoo RENAMED = null;",
				"\t\treturn 0;",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("local variable"),
				skip.reason()
		);
	}

	@Test
	public void testShadowingTryWithResourcesVariableSkipsFix() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int RENAMED = Foo.X;",
				"",
				"\tvoid use() throws Exception {",
				"\t\ttry (java.io.Closeable RENAMED = null) {}",
				"\t}",
				"}"
		));
		final var skip = assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
		assertEquals(
				SkipMessages.PREFER_STATIC_IMPORT_CONSTANT_SKIP_SHADOW.formatted("try-with-resources variable"),
				skip.reason()
		);
	}

	@Test
	public void testSiblingWinsOverWildcard() throws Exception {
		final var file = tempDir.resolve("T.java").toFile();
		Files.writeString(tempDir.resolve("Foo.java"), "class Foo {}");
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import wild.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		FixContext.setFilePath(file.getAbsolutePath());
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static x.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSingleVarWithAnnotationArgContainingCommaIsNotMisidentifiedAsMultiVar() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\t@SuppressWarnings({\"a\", \"b\"}) private static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSingleVarWithCommentContainingCommaIsNotMisidentifiedAsMultiVar() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tprivate static final int /* note, see also Foo */ X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSingleVarWithGenericTypeIsNotMisidentifiedAsMultiVar() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import java.util.Map;",
				"class T {",
				"\tprivate static final Map<String, Integer> X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testSingleVarWithThreeArgGenericTypeIsNotMisidentifiedAsMultiVar() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"import java.util.function.BiFunction;",
				"class T {",
				"\tprivate static final BiFunction<Integer, Integer, Integer> X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testStaticImportInsideTextBlockIgnoredForConflictDetection() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.Foo;",
				"class T {",
				"\tString doc = \"\"\"",
				"\t\timport static other.X;",
				"\t\t\"\"\";",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 6, 26));
		assertEquals(6, result.startLine());
		assertEquals(6, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testStaticImportLineNotUsedForResolution() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import static foo.Foo.OTHER;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 3, 1));
	}

	@Test
	public void testUnparseableFilePathRejectedByPathOfFallsThroughGracefully() {
		FixContext.setFilePath("foo\0bar/T.java");
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testUnusableFilePathFallsThroughGracefully() {
		FixContext.setFilePath("no/such/dir/T.java");
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		assertInstanceOf(SkipResult.class, fixer.fix(lines, 2, 1));
	}

	@Test
	public void testWildcardBeforeExplicitStillPicksExplicit() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import wild.*;",
				"import other.Foo;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 4, 1));
		assertEquals(4, result.startLine());
		assertEquals(4, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static other.Foo.X"), result.importsToAdd());
	}

	@Test
	public void testWildcardImportFallback() {
		final var lines = new ArrayList<>(List.of(
				"package x;",
				"import foo.*;",
				"class T {",
				"\tprivate static final int X = Foo.X;",
				"}"
		));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 3, 1));
		assertEquals(3, result.startLine());
		assertEquals(3, result.endLine());
		assertTrue(result.replacement().isEmpty());
		assertEquals(Set.of("static foo.Foo.X"), result.importsToAdd());
	}
}