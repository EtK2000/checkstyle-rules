package com.etk2000.checkstyle.gradle.fix;

/**
 * Marker for fixers whose effect is "delete the violation line". Used by
 * {@code CheckstyleFixAction.applyFixes} to allow a second same-line
 * violation (e.g. {@code RedundantImportCheck} firing on a line that
 * {@code UnusedImportsCheck}'s fixer just deleted) to pass through and
 * clean up the leftover blank instead of being suppressed.
 */
interface LineDeleter {
}