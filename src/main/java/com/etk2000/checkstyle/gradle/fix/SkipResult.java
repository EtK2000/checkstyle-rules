package com.etk2000.checkstyle.gradle.fix;

import javax.annotation.Nonnull;

record SkipResult(@Nonnull String reason) implements FixAttempt {}