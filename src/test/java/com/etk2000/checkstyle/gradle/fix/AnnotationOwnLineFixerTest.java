package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AnnotationOwnLineFixerTest {
	private static final String TOPIC = "annotationownline";

	private final CheckstyleFixer fixer = new AnnotationOwnLineFixer();

	@Test
	public void testBlockUnterminatedAnnotationSkips() throws Exception {
		assertSkip(fixer, TOPIC, "block_unterminated_annotation_skips");
	}

	@Test
	public void testEmbeddedAnnotationsWithEmptyRemaining() {
		// A slice always has a member after the annotation, so only a direct call
		// reaches the empty-remaining branch.
		final var result = assertInstanceOf(FixResult.class, fixer.fix(List.of("\t@B final @A"), 0, 0));
		assertEquals(0, result.startLine());
		assertEquals(0, result.endLine());
		assertEquals(List.of("\t@A", "\t@B", "\tfinal"), result.replacement());
		assertTrue(result.importsToAdd().isEmpty());
	}

	@Test
	public void testEmbeddedUnterminatedAnnotationSkips() throws Exception {
		assertSkip(fixer, TOPIC, "embedded_unterminated_annotation_skips");
	}

	@Test
	public void testJavadocContinuationAtViolationWithBlankBelow() throws Exception {
		assertSkip(fixer, TOPIC, "javadoc_continuation_at_violation_with_blank_below");
	}
}