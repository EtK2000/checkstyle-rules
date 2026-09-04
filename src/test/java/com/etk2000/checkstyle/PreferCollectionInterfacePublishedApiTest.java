package com.etk2000.checkstyle;

import static com.etk2000.checkstyle.BaseCheckTest.assertCheckMatchesMarkers;
import static com.etk2000.checkstyle.BaseCheckTest.assertNoViolations;

import org.junit.jupiter.api.Test;

/**
 * A published signature is reported but never auto-fixed: another file may override it or call it,
 * and this run cannot see that file. Every member here is therefore a warning, which the fix
 * pipeline skips. This lives outside {@code cases.clean.java} because the shapes it carries need a
 * public top-level type, and Java allows only one of those per file, named after the file.
 */
public class PreferCollectionInterfacePublishedApiTest {
	@Test
	public void testASignatureIsLeftAloneWhenAnImportBindsTheReplacementName() throws Exception {
		assertNoViolations(
				PreferCollectionInterfaceCheck.class,
				"prefercollectioninterface/InputCollectionInterfaceBoundReplacementName.java"
		);
	}

	@Test
	public void testPublishedApiSignaturesAreReportedButNotFixable() throws Exception {
		assertCheckMatchesMarkers(
				PreferCollectionInterfaceCheck.class,
				"prefercollectioninterface/InputCollectionInterfacePublishedApi.java"
		);
	}
}