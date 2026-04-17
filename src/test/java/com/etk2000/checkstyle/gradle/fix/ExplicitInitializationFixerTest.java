package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ExplicitInitializationFixerTest {
	private final CheckstyleFixer fixer = new ExplicitInitializationFixer();

	@Test
	public void testColumnOutOfBounds() {
		final var lines = new ArrayList<>(List.of("\tint x = 0;"));
		assertNull(fixer.fix(lines, 0, 50));
	}

	@Test
	public void testMultiDeclarationFirstVar() {
		final var lines = new ArrayList<>(List.of("\tint x = 0, y;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x, y;", result.replacement().getFirst());
	}

	@Test
	public void testNegativeColumn() {
		final var lines = new ArrayList<>(List.of("\tint x = 0;"));
		assertNull(fixer.fix(lines, 0, -1));
	}

	@Test
	public void testNoEqualsSign() {
		final var lines = new ArrayList<>(List.of("\tint x;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNonDefaultValueSkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 42;"));
		final var attempt = fixer.fix(lines, 0, 5);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonNullCharSkipped() {
		final var lines = new ArrayList<>(List.of("\tchar c = 'a';"));
		final var attempt = fixer.fix(lines, 0, 6);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonNullCharUnicodeSkipped() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\u0001';"));
		final var attempt = fixer.fix(lines, 0, 6);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroBinarySkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 0b1;"));
		final var attempt = fixer.fix(lines, 0, 5);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroDoubleSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 1.0;"));
		final var attempt = fixer.fix(lines, 0, 8);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroExponentSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e1;"));
		final var attempt = fixer.fix(lines, 0, 8);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroExponentWithSignSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e+1;"));
		final var attempt = fixer.fix(lines, 0, 8);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroFloatSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 1.0f;"));
		final var attempt = fixer.fix(lines, 0, 7);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroFloatWithoutDecimalSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 1F;"));
		final var attempt = fixer.fix(lines, 0, 7);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroHexFloatSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0x1.0p0f;"));
		final var attempt = fixer.fix(lines, 0, 7);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroHexSkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 0x1;"));
		final var attempt = fixer.fix(lines, 0, 5);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroLeadingDotSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = .1;"));
		final var attempt = fixer.fix(lines, 0, 8);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroLongSkipped() {
		final var lines = new ArrayList<>(List.of("\tlong x = 1L;"));
		final var attempt = fixer.fix(lines, 0, 6);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNonZeroOctalSkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 01;"));
		final var attempt = fixer.fix(lines, 0, 5);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testNoSemicolon() {
		final var lines = new ArrayList<>(List.of("\tint x = 0"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testRemoveBinaryZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0b0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveBinaryZeroUppercasePrefix() {
		final var lines = new ArrayList<>(List.of("\tint x = 0B0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveBooleanFalse() {
		final var lines = new ArrayList<>(List.of("\tboolean b = false;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 9));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tboolean b;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveCharNull() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\0';"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tchar c;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveCharUnicodeNull() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\u0000';"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tchar c;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZero() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponent() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponentWithMinusSign() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e-0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponentWithPlusSign() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e+0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroLeadingDot() {
		final var lines = new ArrayList<>(List.of("\tdouble d = .0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroMultipleDecimals() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.000;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroUppercaseExponent() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0E0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroWithSuffix() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0d;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZero() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0.0f;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZeroWithoutDecimal() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0F;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZeroWithUnderscores() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0_0.0_0f;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0x0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroUppercasePrefix() {
		final var lines = new ArrayList<>(List.of("\tint x = 0X0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroWithLongSuffix() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0x0L;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroWithPExponent() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0x0.0p0f;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 7));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveIntZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLongZero() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0L;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLongZeroLowercaseSuffix() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0l;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 6));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveNull() {
		final var lines = new ArrayList<>(List.of("\tObject o = null;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 8));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tObject o;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveOctalZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 00;"));
		final var result = assertInstanceOf(FixResult.class, fixer.fix(lines, 0, 5));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertTrue(result.importsToAdd().isEmpty());
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testStringValueSkipped() {
		final var lines = new ArrayList<>(List.of("\tString s = \"hello\";"));
		final var attempt = fixer.fix(lines, 0, 8);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}

	@Test
	public void testTrueSkipped() {
		final var lines = new ArrayList<>(List.of("\tboolean b = true;"));
		final var attempt = fixer.fix(lines, 0, 9);
		assertInstanceOf(SkipResult.class, attempt);
		assertEquals(SkipMessages.EXPLICIT_INIT_SKIP, ((SkipResult) attempt).reason());
	}
}