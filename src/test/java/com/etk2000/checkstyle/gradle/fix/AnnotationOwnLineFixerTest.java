package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

public class AnnotationOwnLineFixerTest {
	private static final String TOPIC = "annotationownline";

	private final CheckstyleFixer fixer = new AnnotationOwnLineFixer();

	@Test
	public void testModifierTailWithoutADeclarationDeclines() {
		assertNull(fixer.fix(List.of("\t@B final @A"), 0, 0));
	}

	@Test
	public void testUnparseableBuffer() throws Exception {
		assertSkip(fixer, TOPIC, "unparseable_buffer");
	}
}