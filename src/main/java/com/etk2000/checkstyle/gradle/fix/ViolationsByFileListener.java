package com.etk2000.checkstyle.gradle.fix;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Variant of {@link ViolationCollectingListener} that groups events by the
 * source file name {@code event.getFileName()}. Used by
 * {@code CheckstyleFixAction.doExecute} where a single Checker run produces
 * violations for many files and each file's events need to be applied
 * independently.
 *
 * <p>Overrides {@code addError} completely; the inherited flat list stays
 * empty so callers always read via {@link #getViolationsByFile()}.
 */
class ViolationsByFileListener extends ViolationCollectingListener {
	@Nonnull
	private final Map<String, List<AuditEvent>> violationsByFile = new HashMap<>();

	@Override
	public void addError(@Nonnull AuditEvent event) {
		violationsByFile.computeIfAbsent(event.getFileName(), k -> new ArrayList<>()).add(event);
	}

	@CheckReturnValue
	@Nonnull
	public Map<String, List<AuditEvent>> getViolationsByFile() {
		return violationsByFile;
	}
}