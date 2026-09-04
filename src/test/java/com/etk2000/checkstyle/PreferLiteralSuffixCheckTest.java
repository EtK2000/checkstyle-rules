package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class PreferLiteralSuffixCheckTest {
	@Test
	public void hasNegativeIntValueWhenWidened_decimalNeverFlagged() {
		assertFalse(PreferLiteralSuffixCheck.hasNegativeIntValueWhenWidened("4294967295"));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"0x80000000",
			"0X80000000",
			"0xFFFFFFFF",
			"0XFFFFFFFF",
			"0x80_00_00_00",
			"0b10000000_00000000_00000000_00000000",
			"0B10000000_00000000_00000000_00000000"
	})
	public void hasNegativeIntValueWhenWidened_highBitSet_returnsTrue(String literal) {
		assertTrue(PreferLiteralSuffixCheck.hasNegativeIntValueWhenWidened(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"0x",
			"0X",
			"0b",
			"0B",
			"0xZZ",
			"0bZ",
			"0xFFFFFFFFFF",
			"0b1111111111111111111111111111111111111111"
	})
	public void hasNegativeIntValueWhenWidened_malformedOrOversized_returnsFalseViaCatch(String literal) {
		assertFalse(PreferLiteralSuffixCheck.hasNegativeIntValueWhenWidened(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"100",
			"0",
			"2147483647",
			"0xFF",
			"0x7FFFFFFF",
			"0X7FFFFFFF",
			"0b1010",
			"0B1010"
	})
	public void hasNegativeIntValueWhenWidened_positiveAndDecimal_returnsFalse(String literal) {
		assertFalse(PreferLiteralSuffixCheck.hasNegativeIntValueWhenWidened(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "_", "00", "0_0", "010", "0xFF", "0XFF", "0b1010", "0B1010", "1a", "1 0"})
	public void isPlainDecimal_notPlainDecimal_returnsFalse(String literal) {
		assertFalse(PreferLiteralSuffixCheck.isPlainDecimal(literal));
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "1", "100", "1_000", "2147483647"})
	public void isPlainDecimal_plainDecimal_returnsTrue(String literal) {
		assertTrue(PreferLiteralSuffixCheck.isPlainDecimal(literal));
	}
}