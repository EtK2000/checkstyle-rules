package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import org.junit.jupiter.api.Test;

public class AnnotationSameLineFixerTest {
	private static final String TOPIC = "annotationsameline";

	private final CheckstyleFixer fixer = new AnnotationSameLineFixer();

	@Test
	public void testAnnotationUnterminatedParenSkipsJoin() throws Exception {
		assertSkip(fixer, TOPIC, "annotation_unterminated_paren_skips_join");
	}
}