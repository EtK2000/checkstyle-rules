package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class PreferVarFixerTest {
	private final CheckstyleFixer fixer = new PreferVarFixer();

	@Test
	public void testAlreadyFinalVar() {
		final var lines = new ArrayList<>(List.of("\tfinal var x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testAlreadyVar() {
		final var lines = new ArrayList<>(List.of("\tvar x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testAnnotation() {
		final var lines = new ArrayList<>(List.of("for (@Nonnull String i : l)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (@Nonnull var i : l)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationMultiple() {
		final var lines = new ArrayList<>(List.of("for (@Nonnull @Deprecated String i : l)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (@Nonnull @Deprecated var i : l)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationPlusFinal() {
		final var lines = new ArrayList<>(List.of("for (@Nonnull final String i : l)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (@Nonnull final var i : l)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testAnnotationWithArgs() {
		final var lines = new ArrayList<>(List.of("\t@SuppressWarnings(\"x\") String s = \"\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\t@SuppressWarnings(\"x\") var s = \"\";", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testArrayType() {
		final var lines = new ArrayList<>(List.of("\tString[] a = new String[5];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar a = new String[5];", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testColumnAtExactEnd() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 11));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testColumnAtNonIdentifier() {
		// column points at '=' which is not a Java identifier start
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 7));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 999));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testCommaInCharLiteralNotMultiVar() {
		final var lines = new ArrayList<>(List.of("\tchar c = ',';"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar c = ',';", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaInMethodCallNotMultiVar() {
		final var lines = new ArrayList<>(List.of("\tString x = m(a, b);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar x = m(a, b);", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testCommaInStringNotMultiVar() {
		final var lines = new ArrayList<>(List.of("\tString x = \"a,b\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar x = \"a,b\";", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondAlreadyDiamond() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList<>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondAnnotationEndingInVar() {
		final var lines = new ArrayList<>(List.of("\t@Autovar var x = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\t@Autovar var x = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondAnnotationEqualSign() {
		final var lines = new ArrayList<>(List.of("\t@SuppressWarnings(value = \"unchecked\") var x = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\t@SuppressWarnings(value = \"unchecked\") var x = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondAnonymousClass() {
		final var lines = new ArrayList<>(List.of("\tvar cmp = new Comparator<Object>() {"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar cmp = new Comparator<>() {", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondAtLineStart() {
		final var lines = new ArrayList<>(List.of("var x = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 0));
		assertEquals("var x = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondMixedObjectAndNonObjectArgs() {
		final var lines = new ArrayList<>(List.of("\tvar m = new HashMap<Object, String>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondMixedQualifiedAndBareObjectArgs() {
		final var lines = new ArrayList<>(List.of("\tvar m = new HashMap<Object, java.lang.Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar m = new HashMap<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondMultipleObjectArgs() {
		final var lines = new ArrayList<>(List.of("\tvar m = new HashMap<Object, Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar m = new HashMap<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondNestedGenericTypeArg() {
		final var lines = new ArrayList<>(List.of("\tvar m = new Foo<Map<Object, Object>>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondNoEqualsSign() {
		final var lines = new ArrayList<>(List.of("\tnew ArrayList<Object>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, -1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondNoNewKeyword() {
		final var lines = new ArrayList<>(List.of("\tvar x = factory<Object>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondNonObjectTypeArg() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList<String>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondNonObjectTypeArgAnnotated() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList<@Ann Object>();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondNotVarType() {
		final var lines = new ArrayList<>(List.of("\tArrayList<Object> a = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		// type-to-var path handles this, not diamond path
		assertEquals("\tvar a = new ArrayList<Object>();", result.replacement().getFirst());
	}

	@Test
	public void testDiamondQualifiedObjectTypeArg() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList<java.lang.Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar a = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondQualifiedTypeName() {
		final var lines = new ArrayList<>(List.of("\tvar a = new java.util.ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar a = new java.util.ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondSingleObjectArg() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar a = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondSingleObjectArgWithWhitespace() {
		final var lines = new ArrayList<>(List.of("\tvar a = new ArrayList< Object >();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar a = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondThreeObjectArgs() {
		final var lines = new ArrayList<>(List.of("\tvar t = new Foo<Object, Object, Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar t = new Foo<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondTypePrefixContainingVar() {
		final var lines = new ArrayList<>(List.of("\tfoovar x = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		// diamond path rejects "foovar" (identifier-part guard), type-to-var path handles it
		assertEquals("\tvar x = new ArrayList<Object>();", result.replacement().getFirst());
	}

	@Test
	public void testDiamondUnbalancedAngleBrackets() {
		final var lines = new ArrayList<>(List.of("\tvar x = new ArrayList<Object();"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testDiamondVarInVariableName() {
		final var lines = new ArrayList<>(List.of("\tvar myvar = new ArrayList<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar myvar = new ArrayList<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondWithConstructorArgs() {
		final var lines = new ArrayList<>(List.of("\tvar list = new ArrayList<Object>(16);"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar list = new ArrayList<>(16);", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDiamondWithFinal() {
		final var lines = new ArrayList<>(List.of("\tfinal var s = new LinkedHashSet<Object>();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal var s = new LinkedHashSet<>();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testDoubleEscapedBackslashNotMultiVar() {
		final var lines = new ArrayList<>(List.of("\tString x = \"\\\\\", y = \"z\";"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testExplicitArrayInitConstructorNotArray() {
		// "new Type(...)" without [] should not be treated as array init
		final var lines = new ArrayList<>(List.of("\tfinal var x = new String(\"x\");"));
		// array path returns null (no []), falls through to type-to-var which sees "final var" -> SkipResult
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testExplicitArrayInitGenericType() {
		final var lines = new ArrayList<>(List.of("\tfinal var a = new List<String>[]{list};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal List<String>[] a = {list};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitArrayInitMultiDim() {
		final var lines = new ArrayList<>(List.of("\tfinal int[][] m = new int[][]{{1}};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal int[][] m = {{1}};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitArrayInitTypedMatching() {
		final var lines = new ArrayList<>(List.of("\tfinal String[] a = new String[]{\"a\"};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal String[] a = {\"a\"};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitArrayInitVarToPrimitive() {
		final var lines = new ArrayList<>(List.of("\tfinal var a = new int[]{1, 2};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal int[] a = {1, 2};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testExplicitArrayInitVarToString() {
		final var lines = new ArrayList<>(List.of("\tfinal var a = new String[]{\"a\"};"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal String[] a = {\"a\"};", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFinalColumnAtFinal() {
		final var lines = new ArrayList<>(List.of("\tfinal int x = 5;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tfinal var x = 5;", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testFinalColumnAtType() {
		final var lines = new ArrayList<>(List.of("\tfinal int x = 5;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals("\tfinal var x = 5;", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEach() {
		final var lines = new ArrayList<>(List.of("for (String item : list)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (var item : list)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForEachFinal() {
		final var lines = new ArrayList<>(List.of("for (final String i : l)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (final var i : l)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testForInit() {
		final var lines = new ArrayList<>(List.of("for (int i = 0; i < 10; ++i)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("for (var i = 0; i < 10; ++i)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testGenericType() {
		final var lines = new ArrayList<>(List.of("\tList<String> l = List.of();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar l = List.of();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiDimArray() {
		final var lines = new ArrayList<>(List.of("\tint[][] m = new int[3][3];"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar m = new int[3][3];", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testMultiVar() {
		final var lines = new ArrayList<>(List.of("\tint x = 1, y = 2;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, 1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(SkipResult.class, fixer.fix(lines, 0, -1));
		assertEquals(SkipMessages.PREFER_VAR_SKIP, result.reason());
	}

	@Test
	public void testNestedGeneric() {
		final var lines = new ArrayList<>(List.of("\tMap<String, List<Integer>> m = Map.of();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar m = Map.of();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testQualifiedType() {
		final var lines = new ArrayList<>(List.of("\tjava.util.List<String> l = x;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar l = x;", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSimpleInt() {
		final var lines = new ArrayList<>(List.of("\tint x = 5;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar x = 5;", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testSimpleString() {
		final var lines = new ArrayList<>(List.of("\tString s = \"hi\";"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar s = \"hi\";", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTryWithResources() {
		final var lines = new ArrayList<>(List.of("try (InputStream in = x)"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals("try (var in = x)", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testTwoTabIndent() {
		final var lines = new ArrayList<>(List.of("\t\tint x = 5;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 2));
		assertEquals("\t\tvar x = 5;", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testWildcardGeneric() {
		final var lines = new ArrayList<>(List.of("\tList<?> l = List.of();"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 1));
		assertEquals("\tvar l = List.of();", result.replacement().getFirst());
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
	}
}