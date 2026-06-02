package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

public class FixContextTest {
	@Nonnull
	private static Violation violation(@Nonnull String key, @Nonnull String message) {
		return new Violation(1, "bundle", key, new Object[0], null, FixContextTest.class, message);
	}

	@AfterEach
	public void cleanup() {
		FixContext.clearFilePath();
		FixContext.clearViolation();
	}

	@Test
	public void testClearRemovesValue() {
		FixContext.setFilePath("/some/path.java");
		FixContext.clearFilePath();
		assertNull(FixContext.getFilePath());
	}

	@Test
	public void testClearViolationRemovesValue() {
		FixContext.setViolation(violation("k", "m"));
		FixContext.clearViolation();
		assertNull(FixContext.getViolationKey());
		assertNull(FixContext.getViolationMessage());
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
	public void testGetViolationKeyReturnsNullByDefault() {
		assertNull(FixContext.getViolationKey());
	}

	@Test
	public void testGetViolationMessageReturnsNullByDefault() {
		assertNull(FixContext.getViolationMessage());
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
	public void testSetViolationExposesKeyAndMessage() {
		FixContext.setViolation(violation("field.sort.anon.class", "Field a must appear before b."));
		assertEquals("field.sort.anon.class", FixContext.getViolationKey());
		assertEquals("Field a must appear before b.", FixContext.getViolationMessage());
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