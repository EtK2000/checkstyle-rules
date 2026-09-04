package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.List;

public class PreferSpecificApiCheckTest {
	/**
	 * This lives outside the topic's own fixtures because the project style forbids an
	 * explicitly typed for-each variable.
	 */
	@Test
	public void loopVariableReceiverTypeIsResolved() throws Exception {
		final var events = BaseCheckTest.runCheck(
				PreferSpecificApiCheck.class,
				"prefervar/InputForEachReceiverScope.java",
				"minSdk",
				"35"
		);
		assertEquals(List.of(), events.stream().map(e -> e.getLine() + ":" + e.getMessage()).toList());
	}
}