package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class LambdaParameterTypeFixerTest {
	private static final String TOPIC = "lambdaparametertype";

	private final CheckstyleFixer fixer = new LambdaParameterTypeFixer();

	@Test
	public void testNoArrow() throws Exception {
		final var fx = TestResources.loadSnippet(TOPIC, "no_arrow_skip");
		final var t = fx.firstTarget();
		final var result = assertInstanceOf(
				SkipResult.class,
				fixer.fix(new ArrayList<>(fx.inputLines()), t.line(), t.column())
		);
		assertEquals(SkipMessages.LAMBDA_PARAM_SKIP, result.reason());
	}
}