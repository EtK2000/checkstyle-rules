package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.Nonnull;

record FixResult(int startLine, int endLine, @Nonnull List<String> replacement) {}