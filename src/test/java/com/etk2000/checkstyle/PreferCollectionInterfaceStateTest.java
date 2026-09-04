package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

/**
 * The check caches a {@code TypeGraph} of the file it is walking, so a stale one would answer the
 * next file's overload-collapse question with the previous file's inheritance. One checker driving
 * two files is the only arrangement that exercises the reset; every slice test builds a fresh one.
 */
public class PreferCollectionInterfaceStateTest {
	private static final String LEAK_A = "prefercollectioninterface/InputCollectionInterfaceTypeGraphLeakA.java";
	private static final String LEAK_B = "prefercollectioninterface/InputCollectionInterfaceTypeGraphLeakB.java";
	private static final String USE_SITE_LEAK_A = "prefercollectioninterface/InputCollectionInterfaceUseSiteLeakA.java";
	private static final String USE_SITE_LEAK_B = "prefercollectioninterface/InputCollectionInterfaceUseSiteLeakB.java";

	@Test
	public void testTheCallSiteIndexDoesNotSurviveIntoTheNextFile() throws Exception {
		final var violations = BaseCheckTest.runCheckOnFiles(
				PreferCollectionInterfaceCheck.class, USE_SITE_LEAK_A, USE_SITE_LEAK_B
		);
		assertEquals(2, violations.size());

		final var discarded = violations.getFirst();
		assertTrue(discarded.getFileName().endsWith("InputCollectionInterfaceUseSiteLeakA.java"));
		assertEquals(6, discarded.getLine());
		assertEquals(
				SeverityLevel.ERROR,
				discarded.getSeverityLevel(),
				"the first file only discards the result, so widening its return type is contained"
		);
		assertEquals("Use 'List' instead of 'ArrayList'.", discarded.getMessage());

		final var pinned = violations.getLast();
		assertTrue(pinned.getFileName().endsWith("InputCollectionInterfaceUseSiteLeakB.java"));
		assertEquals(8, pinned.getLine());
		assertEquals(
				SeverityLevel.WARNING,
				pinned.getSeverityLevel(),
				"the second file assigns the result to a concrete-typed field, which a stale index cannot see"
		);
		assertEquals("Use 'List' instead of 'ArrayList'.", pinned.getMessage());
	}

	@Test
	public void testTheTypeGraphDoesNotSurviveIntoTheNextFile() throws Exception {
		final var violations = BaseCheckTest.runCheckOnFiles(PreferCollectionInterfaceCheck.class, LEAK_A, LEAK_B);
		assertEquals(
				1,
				violations.size(),
				"the second file's override pair collapses, so only the first file's signature is reported"
		);
		final var violation = violations.getFirst();
		assertTrue(
				violation.getFileName().endsWith("InputCollectionInterfaceTypeGraphLeakA.java"),
				"the reported violation must be the first file's"
		);
		assertEquals(6, violation.getLine());
		assertEquals(SeverityLevel.WARNING, violation.getSeverityLevel());
		assertEquals("Use 'List' instead of 'ArrayList'.", violation.getMessage());
	}
}