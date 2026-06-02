package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ViolationsByFileListenerTest {
	@Nonnull
	private static AuditEvent event(@Nullable String fileName, @Nonnull String message) {
		final var violation = new Violation(
				1, 0, "", "", null, SeverityLevel.ERROR, null, Object.class, message
		);
		return new AuditEvent(new Object(), fileName, violation);
	}

	private ByteArrayOutputStream capturedErr;
	private PrintStream originalErr;

	@Test
	public void addErrorDoesNotPopulateParentFlatList() {
		final var listener = new ViolationsByFileListener();
		listener.addError(event("A.java", "msg"));
		assertEquals(1, listener.getViolationsByFile().get("A.java").size());
		assertTrue(
				listener.getViolations().isEmpty(),
				"Subclass overrides addError; parent's flat list must stay empty"
		);
	}

	@Test
	public void addErrorGroupsByFileName() {
		final var listener = new ViolationsByFileListener();
		final var a1 = event("A.java", "a1");
		final var a2 = event("A.java", "a2");
		final var b1 = event("B.java", "b1");
		listener.addError(a1);
		listener.addError(a2);
		listener.addError(b1);
		assertEquals(List.of(a1, a2), listener.getViolationsByFile().get("A.java"));
		assertEquals(List.of(b1), listener.getViolationsByFile().get("B.java"));
	}

	@Test
	public void addErrorHandlesNullFileName() {
		final var listener = new ViolationsByFileListener();
		final var e = event(null, "msg");
		listener.addError(e);
		assertEquals(List.of(e), listener.getViolationsByFile().get(null));
	}

	@Test
	public void addErrorPreservesPerFileOrderUnderInterleaving() {
		final var listener = new ViolationsByFileListener();
		final var a1 = event("A.java", "a1");
		final var b1 = event("B.java", "b1");
		final var a2 = event("A.java", "a2");
		listener.addError(a1);
		listener.addError(b1);
		listener.addError(a2);
		assertEquals(List.of(a1, a2), listener.getViolationsByFile().get("A.java"));
		assertEquals(List.of(b1), listener.getViolationsByFile().get("B.java"));
	}

	@Test
	public void addExceptionInheritsStderrLogging() {
		final var listener = new ViolationsByFileListener();
		listener.addException(event("X.java", "ignored"), new RuntimeException("boom"));
		final var out = capturedErr.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains("X.java"));
		assertTrue(out.contains("boom"));
	}

	@Test
	public void getViolationsByFileEmptyInitially() {
		final var listener = new ViolationsByFileListener();
		assertTrue(listener.getViolationsByFile().isEmpty());
	}

	@BeforeEach
	public void redirectStderr() {
		originalErr = System.err;
		capturedErr = new ByteArrayOutputStream();
		System.setErr(new PrintStream(capturedErr, true, StandardCharsets.UTF_8));
	}

	@AfterEach
	public void restoreStderr() {
		System.setErr(originalErr);
	}
}