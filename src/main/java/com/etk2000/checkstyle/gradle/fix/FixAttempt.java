package com.etk2000.checkstyle.gradle.fix;

sealed interface FixAttempt permits FixResult, SkipResult {
}