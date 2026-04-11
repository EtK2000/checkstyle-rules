package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;

public class MessagesFileSortedTest {
	@Test
	public void testMessagesAreSortedAlphabetically() throws Exception {
		final var keys = new ArrayList<String>();
		try (var reader = new BufferedReader(new InputStreamReader(
				getClass().getResourceAsStream("/com/etk2000/checkstyle/messages.properties"),
				StandardCharsets.UTF_8
		))) {
			String line;
			while ((line = reader.readLine()) != null) {
				if (line.isBlank() || line.startsWith("#"))
					continue;

				final var eqIndex = line.indexOf('=');
				if (eqIndex > 0)
					keys.add(line.substring(0, eqIndex));
			}
		}

		for (var i = 1; i < keys.size(); ++i) {
			if (keys.get(i).compareTo(keys.get(i - 1)) < 0)
				fail("messages.properties is not sorted: '" + keys.get(i) + "' must appear before '" + keys.get(i - 1) + "'");
		}
	}
}