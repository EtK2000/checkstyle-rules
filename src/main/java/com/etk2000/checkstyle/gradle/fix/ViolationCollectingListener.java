package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared {@link AuditListener} that accumulates violations into a list and
 * logs Checkstyle exceptions to stderr. Used by single-file callers (most
 * tests). For the multi-file map shape that {@code doExecute} needs, see
 * {@link ViolationsByFileListener}.
 *
 * <p>Centralizing this listener prevents silent-swallow regressions: the
 * earlier inline anonymous listeners had empty {@code addException} bodies,
 * so a Checkstyle parser error or check throw produced zero violations and
 * passed silently.
 */
public class ViolationCollectingListener implements AuditListener {
	@Nonnull
	private final List<AuditEvent> violations = new ArrayList<>();

	@Override
	public void addError(@Nonnull AuditEvent event) {
		violations.add(event);
	}

	@Override
	public void addException(@Nonnull AuditEvent event, @Nonnull Throwable throwable) {
		System.err.println("Checkstyle exception on " + event.getFileName() + ": " + throwable.getMessage());
	}

	@Override
	public void auditFinished(@Nonnull AuditEvent event) {
	}

	@Override
	public void auditStarted(@Nonnull AuditEvent event) {
	}

	@Override
	public void fileFinished(@Nonnull AuditEvent event) {
	}

	@Override
	public void fileStarted(@Nonnull AuditEvent event) {
	}

	@CheckReturnValue
	@Nonnull
	public List<AuditEvent> getViolations() {
		return violations;
	}
}