package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

public class PropertiesUtilTest {
	@Test
	public void propertiesAsArrayEmptyMapReturnsEmptyArray() {
		assertArrayEquals(new String[0], PropertiesUtil.propertiesAsArray(Map.of()));
	}

	@Test
	public void propertiesAsArrayMultipleEntriesSortedByKey() {
		final var insertionOrdered = new LinkedHashMap<String, String>();
		insertionOrdered.put("zKey", "zVal");
		insertionOrdered.put("aKey", "aVal");
		insertionOrdered.put("mKey", "mVal");
		assertArrayEquals(
				new String[]{"aKey", "aVal", "mKey", "mVal", "zKey", "zVal"},
				PropertiesUtil.propertiesAsArray(insertionOrdered)
		);
	}

	@Test
	public void propertiesAsArraySingleEntry() {
		assertArrayEquals(
				new String[]{"minSdk", "19"},
				PropertiesUtil.propertiesAsArray(Map.of("minSdk", "19"))
		);
	}

	@Test
	public void variantSuffixEmptyMapReturnsEmptyString() {
		assertEquals("", PropertiesUtil.variantSuffix(Map.of()));
	}

	@Test
	public void variantSuffixFromArrayEmptyReturnsEmptyString() {
		assertEquals("", PropertiesUtil.variantSuffixFromArray(new String[0]));
	}

	@Test
	public void variantSuffixFromArrayMatchesMapBasedVariantSuffix() {
		assertEquals(
				PropertiesUtil.variantSuffix(Map.of("minSdk", "19")),
				PropertiesUtil.variantSuffixFromArray(new String[]{"minSdk", "19"})
		);
	}

	@Test
	public void variantSuffixFromArrayMultipleKeysSortedByKey() {
		assertEquals(
				"aKey-aVal.zKey-zVal",
				PropertiesUtil.variantSuffixFromArray(new String[]{"zKey", "zVal", "aKey", "aVal"})
		);
	}

	@Test
	public void variantSuffixFromArrayOddLengthThrows() {
		final var ex = assertThrows(
				IllegalArgumentException.class,
				() -> PropertiesUtil.variantSuffixFromArray(new String[]{"minSdk", "19", "orphan"})
		);
		assertEquals(
				"checkProperties must be an even-length key/value sequence, got length 3",
				ex.getMessage()
		);
	}

	@Test
	public void variantSuffixFromArraySingleKeyValueReturnsKDashV() {
		assertEquals("minSdk-19", PropertiesUtil.variantSuffixFromArray(new String[]{"minSdk", "19"}));
	}

	@Test
	public void variantSuffixMultipleKeysSortedByKey() {
		final var insertionOrdered = new LinkedHashMap<String, String>();
		insertionOrdered.put("zKey", "zVal");
		insertionOrdered.put("aKey", "aVal");
		assertEquals("aKey-aVal.zKey-zVal", PropertiesUtil.variantSuffix(insertionOrdered));
	}

	@Test
	public void variantSuffixSingleKeyValueReturnsKDashV() {
		assertEquals("minSdk-19", PropertiesUtil.variantSuffix(Map.of("minSdk", "19")));
	}
}