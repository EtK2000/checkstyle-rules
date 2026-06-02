package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

public class FqnResolverTest {
	@Test
	public void testResolveParentlessFilePathFallsThroughToNone() {
		final var result = FqnResolver.resolve(List.of(), new boolean[0], "Helper", "Helper.java");
		assertEquals(FqnResolver.ResolutionSource.NONE, result.source());
		assertNull(result.fqn());
	}

	@Test
	public void testResolveUnparseableFilePathIsCaughtAndFallsThroughToNone() {
		// a NUL character (built at runtime to keep it out of the source) is not a legal
		// path element, so Path.of rejects it with InvalidPathException; resolve must catch
		// it rather than let it propagate
		final var invalidPath = "x" + (char) 0 + "y.java";
		final var result = FqnResolver.resolve(List.of(), new boolean[0], "Helper", invalidPath);
		assertEquals(FqnResolver.ResolutionSource.NONE, result.source());
		assertNull(result.fqn());
	}
}