package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

/**
 * The one annotatable target the slice corpus cannot reach. A package annotation is legal only in a
 * {@code package-info.java}, and a slice's {@code // package:} directive emits a bare package line,
 * so this drives the fixture directly instead.
 */
public class RedundantAnnotationSyntaxPackageInfoTest {
	private static final String FIXTURE = "redundantannotationsyntax/packageannotation/package-info.java";

	@Test
	public void testEmptyParensOnPackageAnnotation() throws Exception {
		final var events = BaseCheckTest.runCheck(RedundantAnnotationSyntaxCheck.class, FIXTURE);
		assertEquals(1, events.size());
		assertEquals(4, events.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel());
		assertEquals(
				"Remove the empty parentheses from annotation '@Deprecated'.",
				events.getFirst().getMessage()
		);
	}
}