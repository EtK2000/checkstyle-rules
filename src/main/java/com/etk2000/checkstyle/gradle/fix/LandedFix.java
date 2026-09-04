package com.etk2000.checkstyle.gradle.fix;

import javax.annotation.Nonnull;

/** A fix {@code applyFixes} applied, with the check and 1-based event line it answered. */
record LandedFix(@Nonnull String checkName, int line, @Nonnull FixResult result) {}