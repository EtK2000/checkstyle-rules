package com.etk2000.checkstyle.gradle.fix;

import javax.annotation.Nonnull;

/**
 * A fix left a file that parsed before the pass unparseable. Every fix must keep the file
 * parseable, so this is a fixer bug and fails the run rather than skipping the file. An
 * {@link Error} so no fixer's or the pipeline's firewall, which all catch runtime failures to
 * skip one violation, can swallow it.
 */
final class FixBrokeParseError extends Error {
	FixBrokeParseError(@Nonnull String message) {
		super(message);
	}
}