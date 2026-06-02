package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

public class ViolationCollectingListenerTest {
	@Nonnull
	private static AuditEvent event(@Nonnull String fileName, @Nonnull String message) {
		final var violation = new Violation(
				1, 0, "", "", null, SeverityLevel.ERROR, null, Object.class, message
		);
		return new AuditEvent(new Object(), fileName, violation);
	}

	private ByteArrayOutputStream capturedErr;
	private PrintStream originalErr;

	@Test
	public void addErrorAccumulatesInOrder() {
		final var listener = new ViolationCollectingListener();
		final var e1 = event("A.java", "first");
		final var e2 = event("B.java", "second");
		listener.addError(e1);
		listener.addError(e2);
		assertEquals(List.of(e1, e2), listener.getViolations());
	}

	@Test
	public void addErrorPreservesEventIdentity() {
		final var listener = new ViolationCollectingListener();
		final var e = event("A.java", "msg");
		listener.addError(e);
		assertSame(e, listener.getViolations().getFirst());
	}

	@Test
	public void addExceptionWritesToStderrEvenWhenViolationsEmpty() {
		final var listener = new ViolationCollectingListener();
		listener.addException(event("X.java", "ignored"), new RuntimeException("boom"));
		assertTrue(listener.getViolations().isEmpty());
		final var out = capturedErr.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains("X.java"), "stderr should mention file: " + out);
		assertTrue(out.contains("boom"), "stderr should include throwable message: " + out);
	}

	@Test
	public void addExceptionWritesToStderrWithFileNameAndMessage() {
		final var listener = new ViolationCollectingListener();
		listener.addException(event("Foo.java", "ignored"), new RuntimeException("kaboom"));
		final var out = capturedErr.toString(StandardCharsets.UTF_8);
		assertTrue(out.contains("Foo.java"), "stderr should mention file: " + out);
		assertTrue(out.contains("kaboom"), "stderr should include throwable message: " + out);
	}

	@Test
	public void getViolationsEmptyInitially() {
		final var listener = new ViolationCollectingListener();
		assertTrue(listener.getViolations().isEmpty());
	}

	@Test
	public void noOpLifecycleMethodsDoNotThrow() {
		final var listener = new ViolationCollectingListener();
		final var e = event("A.java", "msg");
		listener.auditStarted(e);
		listener.fileStarted(e);
		listener.fileFinished(e);
		listener.auditFinished(e);
		assertTrue(listener.getViolations().isEmpty());
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