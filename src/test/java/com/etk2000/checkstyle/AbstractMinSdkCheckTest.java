package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class AbstractMinSdkCheckTest {
	@Test
	public void maxIntMinSdkIsAccepted() {
		final var check = new PreferMathMethodCheck();
		assertDoesNotThrow(() -> check.setMinSdk(Integer.MAX_VALUE));
	}

	@Test
	public void minSdkAtLeastComparesAgainstTheStoredLevel() {
		final var check = new PreferMathMethodCheck();
		check.setMinSdk(24);
		assertTrue(check.minSdkAtLeast(24));
		assertTrue(check.minSdkAtLeast(23));
		assertFalse(check.minSdkAtLeast(25));
	}

	@Test
	public void minSdkOneIsAccepted() {
		final var check = new PreferMathMethodCheck();
		assertDoesNotThrow(() -> check.setMinSdk(1));
	}

	@ParameterizedTest
	@ValueSource(ints = {0, -1, Integer.MIN_VALUE})
	public void nonPositiveMinSdkIsRejected(int minSdk) {
		final var check = new PreferMathMethodCheck();
		final var thrown = assertThrows(IllegalArgumentException.class, () -> check.setMinSdk(minSdk));
		assertEquals("minSdk must be a positive API level, got " + minSdk, thrown.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"0", "-1"})
	public void nonPositiveMinSdkIsRejectedThroughConfig(String minSdk) {
		final var thrown = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.runCheck(PreferMathMethodCheck.class, "cases.in.java", "minSdk", minSdk)
		);

		// walk the chain rather than pin a depth: what matters is that the rejection is the
		// cause, not that some unrelated misconfiguration also fails to start
		var cause = thrown.getCause();
		while (cause != null && !(cause instanceof IllegalArgumentException))
			cause = cause.getCause();
		assertNotNull(cause, "rejection did not surface as the cause: " + thrown);
		assertTrue(cause.getMessage().contains("minSdk must be a positive API level"), cause.getMessage());
	}
}