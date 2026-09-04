package com.etk2000.checkstyle.format;

import static com.etk2000.checkstyle.ScalingAssertions.assertScalesLinearly;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;

import java.util.List;

@Isolated
public class JavaSpanReindenterScalingTest {
	@Test
	public void testBraceAfterLongCommentBeforeConstructorArgsScalesLinearly() throws Exception {
		assertScalesLinearly(
				2000,
				32000,
				size -> List.of("x(new Outer /*" + "a".repeat(size) + "*/ () {", "}", ")"),
				(size, lines) -> {
					final var start = System.nanoTime();
					assertEquals("}", JavaSpanReindenter.reindent(lines, 0).get(1));
					return System.nanoTime() - start;
				}
		);
	}

	@Test
	public void testBraceAfterManyGenericNewsScalesLinearly() throws Exception {
		assertScalesLinearly(
				2000,
				32000,
				size -> List.of("x(" + "new A<".repeat(size) + "B" + ">".repeat(size) + "() {", "}", ")"),
				(size, lines) -> {
					final var start = System.nanoTime();
					assertEquals("}", JavaSpanReindenter.reindent(lines, 0).get(1));
					return System.nanoTime() - start;
				}
		);
	}
}