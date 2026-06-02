package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.annotation.Nonnull;

public class JavaLangClassesTest {
	@Test
	public void forJavaTargetAboveLatestReturnsSameAsMaxValue() {
		assertEquals(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE), JavaLangClasses.forJavaTarget(99));
		assertEquals(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE), JavaLangClasses.forJavaTarget(26));
	}

	@ParameterizedTest
	@ValueSource(ints = {7, 0, -1, Integer.MIN_VALUE})
	public void forJavaTargetBelowEightReturnsBaseSetOnly(int target) {
		final var types = JavaLangClasses.forJavaTarget(target);
		assertEquals(96, types.size());
		assertFalse(types.contains("Compiler"), "Compiler's range starts at 8");
	}

	@Test
	public void forJavaTargetCachesInstancePerTarget() {
		assertSame(JavaLangClasses.forJavaTarget(17), JavaLangClasses.forJavaTarget(17));
		assertSame(JavaLangClasses.forJavaTarget(11), JavaLangClasses.forJavaTarget(11));
	}

	@ParameterizedTest
	@ValueSource(strings = {"AbstractStringBuilder", "NamedPackage", "ThreadBuilders"})
	public void forJavaTargetExcludesNonPublicTypes(@Nonnull String type) {
		assertFalse(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(type));
	}

	@ParameterizedTest
	@ValueSource(strings = {"StringTemplate", "StableValue", "LazyConstant"})
	public void forJavaTargetExcludesPreviewAndWithdrawnTypes(@Nonnull String type) {
		for (var v = 8; v <= 26; ++v)
			assertFalse(JavaLangClasses.forJavaTarget(v).contains(type), type + " is preview/withdrawn, never standard");
	}

	@ParameterizedTest
	@ValueSource(strings = {"Method", "MethodHandle", "MemorySegment", "AccessibleObject"})
	public void forJavaTargetExcludesSubpackageTypes(@Nonnull String type) {
		assertFalse(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(type), type + " lives in a java.lang subpackage, not java.lang");
	}

	@Test
	public void forJavaTargetGatesCompilerToRemovalRange() {
		assertTrue(JavaLangClasses.forJavaTarget(8).contains("Compiler"));
		assertTrue(JavaLangClasses.forJavaTarget(20).contains("Compiler"));
		assertFalse(JavaLangClasses.forJavaTarget(21).contains("Compiler"), "java.lang.Compiler was removed in Java 21");
		assertFalse(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains("Compiler"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"IllegalCallerException", "LayerInstantiationException", "Module", "ModuleLayer", "ProcessHandle", "StackWalker"})
	public void forJavaTargetGatesJava9Additions(@Nonnull String type) {
		assertFalse(JavaLangClasses.forJavaTarget(8).contains(type));
		assertTrue(JavaLangClasses.forJavaTarget(9).contains(type));
	}

	@Test
	public void forJavaTargetGatesMatchExceptionAtFinalization() {
		assertFalse(JavaLangClasses.forJavaTarget(19).contains("MatchException"), "ct.sym lists MatchException at 19 but pattern switch was preview through 20");
		assertFalse(JavaLangClasses.forJavaTarget(20).contains("MatchException"));
		assertTrue(JavaLangClasses.forJavaTarget(21).contains("MatchException"));
	}

	@Test
	public void forJavaTargetGatesRecordAtFinalization() {
		assertFalse(JavaLangClasses.forJavaTarget(14).contains("Record"), "ct.sym lists Record at 14 but records were preview in 14-15");
		assertFalse(JavaLangClasses.forJavaTarget(15).contains("Record"));
		assertTrue(JavaLangClasses.forJavaTarget(16).contains("Record"));
	}

	@Test
	public void forJavaTargetGatesScopedValueAndIoAtFinalization() {
		assertFalse(JavaLangClasses.forJavaTarget(24).contains("ScopedValue"));
		assertFalse(JavaLangClasses.forJavaTarget(24).contains("IO"));
		assertTrue(JavaLangClasses.forJavaTarget(25).contains("ScopedValue"));
		assertTrue(JavaLangClasses.forJavaTarget(25).contains("IO"));
	}

	@Test
	public void forJavaTargetGatesWrongThreadExceptionAtFinalization() {
		assertFalse(JavaLangClasses.forJavaTarget(18).contains("WrongThreadException"));
		assertTrue(JavaLangClasses.forJavaTarget(19).contains("WrongThreadException"));
	}

	@ParameterizedTest
	@ValueSource(ints = {8, 11, 17, 21, 25, Integer.MAX_VALUE})
	public void forJavaTargetIncludesBaseTypesAtAllVersions(int target) {
		final var types = JavaLangClasses.forJavaTarget(target);
		assertTrue(types.contains("Object"));
		assertTrue(types.contains("String"));
		assertTrue(types.contains("Math"));
		assertTrue(types.contains("StrictMath"));
		assertTrue(types.contains("Integer"));
	}

	@Test
	public void forJavaTargetReturnsImmutableSet() {
		final var types = JavaLangClasses.forJavaTarget(17);
		assertThrows(UnsupportedOperationException.class, () -> types.add("Foo"));
	}

	@CsvSource({
			"8, 97",
			"9, 103",
			"15, 103",
			"16, 104",
			"18, 104",
			"19, 105",
			"20, 105",
			"21, 105",
			"24, 105",
			"25, 107",
			"26, 107"
	})
	@ParameterizedTest
	public void forJavaTargetSizesPerVersion(int target, int expectedSize) {
		assertEquals(expectedSize, JavaLangClasses.forJavaTarget(target).size());
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"Boolean", "Byte", "Character", "Class", "ClassLoader", "Cloneable", "Comparable",
			"Deprecated", "Double", "Enum", "Float", "FunctionalInterface", "Integer", "Iterable",
			"Long", "Math", "Number", "Object", "Override", "Process", "Runnable", "Runtime",
			"SafeVarargs", "Short", "StrictMath", "String", "StringBuffer", "StringBuilder",
			"SuppressWarnings", "System", "Thread", "Throwable", "Void"
	})
	public void latestTargetContainsFormerCuratedNames(@Nonnull String type) {
		assertTrue(JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(type));
	}
}