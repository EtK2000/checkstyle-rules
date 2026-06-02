package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class PreferStaticImportCheckTest {
	private static final String OBJECTS_FILE = "preferstaticimport/cases.objects.in.java";
	private static final String SDK = String.valueOf(Integer.MAX_VALUE);
	private static final String SIBLING_SHADOW_FILE = "preferstaticimport/siblingshadow/cases.clean.java";

	@Test
	public void testDefaultMinSdkFiresAllRules() throws Exception {
		assertEquals(10, BaseCheckTest.runCheck(PreferStaticImportCheck.class, OBJECTS_FILE).size());
	}

	@Test
	public void testSiblingShadowCleanViaWildcard() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, SIBLING_SHADOW_FILE, "minSdk", SDK).isEmpty());
	}
}