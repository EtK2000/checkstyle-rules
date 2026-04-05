package com.etk2000.checkstyle.gradle.fix;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

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
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
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
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNonNullCharSkipped() {
		final var lines = new ArrayList<>(List.of("\tchar c = 'a';"));
		assertNull(fixer.fix(lines, 0, 6));
	}

	@Test
	public void testNonNullCharUnicodeSkipped() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\u0001';"));
		assertNull(fixer.fix(lines, 0, 6));
	}

	@Test
	public void testNonZeroBinarySkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 0b1;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNonZeroDoubleSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 1.0;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testNonZeroExponentSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e1;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testNonZeroExponentWithSignSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e+1;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testNonZeroFloatSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 1.0f;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testNonZeroFloatWithoutDecimalSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 1F;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testNonZeroHexFloatSkipped() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0x1.0p0f;"));
		assertNull(fixer.fix(lines, 0, 7));
	}

	@Test
	public void testNonZeroHexSkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 0x1;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNonZeroLeadingDotSkipped() {
		final var lines = new ArrayList<>(List.of("\tdouble d = .1;"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testNonZeroLongSkipped() {
		final var lines = new ArrayList<>(List.of("\tlong x = 1L;"));
		assertNull(fixer.fix(lines, 0, 6));
	}

	@Test
	public void testNonZeroOctalSkipped() {
		final var lines = new ArrayList<>(List.of("\tint x = 01;"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testNoSemicolon() {
		final var lines = new ArrayList<>(List.of("\tint x = 0"));
		assertNull(fixer.fix(lines, 0, 5));
	}

	@Test
	public void testRemoveBinaryZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0b0;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveBinaryZeroUppercasePrefix() {
		final var lines = new ArrayList<>(List.of("\tint x = 0B0;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveBooleanFalse() {
		final var lines = new ArrayList<>(List.of("\tboolean b = false;"));
		final var result = fixer.fix(lines, 0, 9);
		assertNotNull(result);
		assertEquals("\tboolean b;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveCharNull() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\0';"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals("\tchar c;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveCharUnicodeNull() {
		final var lines = new ArrayList<>(List.of("\tchar c = '\\u0000';"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals("\tchar c;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZero() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponent() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponentWithMinusSign() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e-0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroExponentWithPlusSign() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0e+0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroLeadingDot() {
		final var lines = new ArrayList<>(List.of("\tdouble d = .0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroMultipleDecimals() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.000;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroUppercaseExponent() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0E0;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveDoubleZeroWithSuffix() {
		final var lines = new ArrayList<>(List.of("\tdouble d = 0.0d;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tdouble d;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZero() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0.0f;"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZeroWithoutDecimal() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0F;"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveFloatZeroWithUnderscores() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0_0.0_0f;"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0x0;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroUppercasePrefix() {
		final var lines = new ArrayList<>(List.of("\tint x = 0X0;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroWithLongSuffix() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0x0L;"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveHexZeroWithPExponent() {
		final var lines = new ArrayList<>(List.of("\tfloat f = 0x0.0p0f;"));
		final var result = fixer.fix(lines, 0, 7);
		assertNotNull(result);
		assertEquals("\tfloat f;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveIntZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 0;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLongZero() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0L;"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveLongZeroLowercaseSuffix() {
		final var lines = new ArrayList<>(List.of("\tlong x = 0l;"));
		final var result = fixer.fix(lines, 0, 6);
		assertNotNull(result);
		assertEquals("\tlong x;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveNull() {
		final var lines = new ArrayList<>(List.of("\tObject o = null;"));
		final var result = fixer.fix(lines, 0, 8);
		assertNotNull(result);
		assertEquals("\tObject o;", result.replacement().getFirst());
	}

	@Test
	public void testRemoveOctalZero() {
		final var lines = new ArrayList<>(List.of("\tint x = 00;"));
		final var result = fixer.fix(lines, 0, 5);
		assertNotNull(result);
		assertEquals("\tint x;", result.replacement().getFirst());
	}

	@Test
	public void testStringValueSkipped() {
		final var lines = new ArrayList<>(List.of("\tString s = \"hello\";"));
		assertNull(fixer.fix(lines, 0, 8));
	}

	@Test
	public void testTrueSkipped() {
		final var lines = new ArrayList<>(List.of("\tboolean b = true;"));
		assertNull(fixer.fix(lines, 0, 9));
	}
}