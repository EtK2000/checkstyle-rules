package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class LineLengthTest {
	@Test
	public void testAlignedTab() {
		assertEquals(8, LineLength.tabExpandedLength("xxxx\t"));
	}

	@Test
	public void testEmpty() {
		assertEquals(0, LineLength.tabExpandedLength(""));
	}

	@Test
	public void testMixedSpacesAndTab() {
		assertEquals(4, LineLength.tabExpandedLength("  \t"));
	}

	@Test
	public void testNoTabs() {
		assertEquals(5, LineLength.tabExpandedLength("hello"));
	}

	@Test
	public void testSingleTab() {
		assertEquals(4, LineLength.tabExpandedLength("\t"));
	}

	@Test
	public void testTabAfterOneChar() {
		assertEquals(4, LineLength.tabExpandedLength("x\t"));
	}

	@Test
	public void testTabAfterThreeChars() {
		assertEquals(4, LineLength.tabExpandedLength("xxx\t"));
	}

	@Test
	public void testTabAfterTwoChars() {
		assertEquals(4, LineLength.tabExpandedLength("xx\t"));
	}

	@Test
	public void testTabAtStartFollowedByChar() {
		assertEquals(5, LineLength.tabExpandedLength("\tx"));
	}

	@Test
	public void testTwoTabs() {
		assertEquals(8, LineLength.tabExpandedLength("\t\t"));
	}
}