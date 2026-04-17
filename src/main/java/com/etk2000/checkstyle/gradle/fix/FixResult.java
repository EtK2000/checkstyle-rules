package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

record FixResult(int startLine, int endLine, @Nonnull List<String> replacement, @Nonnull Set<String> importsToAdd) implements FixAttempt {
	FixResult(int startLine, int endLine, @Nonnull List<String> replacement) {
		this(startLine, endLine, replacement, Set.of());
	}
}