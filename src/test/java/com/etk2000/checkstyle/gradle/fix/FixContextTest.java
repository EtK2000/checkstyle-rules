package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

public class FixContextTest {
	@AfterEach
	public void cleanup() {
		FixContext.clearFilePath();
	}

	@Test
	public void testClearRemovesValue() {
		FixContext.setFilePath("/some/path.java");
		FixContext.clearFilePath();
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testClearWhenNothingSetIsNoOp() {
		FixContext.clearFilePath();
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testGetFilePathReturnsNullByDefault() {
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testOverwriteReplacesValue() {
		FixContext.setFilePath("/first.java");
		FixContext.setFilePath("/second.java");
		assertEquals("/second.java", FixContext.getFilePath());
	}

	@Test
	public void testSetThenGetReturnsValue() {
		FixContext.setFilePath("/some/path.java");
		assertEquals("/some/path.java", FixContext.getFilePath());
	}

	@Test
	public void testThreadLocalIsolation() throws Exception {
		FixContext.setFilePath("/main-thread.java");
		final var fromOtherThread = new AtomicReference<String>("not-set");
		final var thread = new Thread(() -> fromOtherThread.set(FixContext.getFilePath()));
		thread.start();
		thread.join();
		assertNull(fromOtherThread.get());
		assertEquals("/main-thread.java", FixContext.getFilePath());
	}
}