package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code UnusedImportsCheck}. Deletes the import line at the
 * violation site, after validating that the line parses as a single
 * {@code import [static] X[.Y]+;} statement. Re-verifies that the simple name
 * is still absent from the file body before deleting non-redundant imports.
 * This protects against the same-pass cross-fixer race where another fixer
 * rewrites the body to USE the simple name between the check phase
 * (pre-fix snapshot) and this fixer's run.
 *
 * <p>{@code java.lang.*} imports are deleted without body re-verify: the check
 * fires on them because they're inherently redundant (the class is
 * auto-imported), not because the simple name is unused. The same-line
 * {@code RedundantImport} + {@code UnusedImports} cascade depends on the
 * unconditional delete here. Wildcard imports also delete unconditionally,
 * since the check doesn't fire on a wildcard whose members are referenced.
 *
 * <p>Implements {@link LineDeleter} so a same-line cross-fixer delete cascade
 * can sweep the residual blank line via the {@code suppressedLine}
 * pass-through.
 */
class UnusedImportsFixer implements CheckstyleFixer, LineDeleter {
	private static final String JAVA_LANG_PREFIX = "java.lang.";

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var rawLine = lines.get(lineIndex);
		if (rawLine.isEmpty())
			return LineDeletion.deleteRange(lines, lineIndex, lineIndex);
		// a block comment that opens on this line but closes on a later one would
		// be orphaned by deleting only this line, so leave the import in place
		if (LambdaCallParser.endsInBlockComment(rawLine))
			return new SkipResult(SkipMessages.UNUSED_IMPORTS_MALFORMED);
		final var parsed = ImportLine.parse(LambdaCallParser.stripComment(rawLine));
		if (parsed == null)
			return new SkipResult(SkipMessages.UNUSED_IMPORTS_MALFORMED);
		if (parsed.wildcard())
			return LineDeletion.deleteRange(lines, lineIndex, lineIndex);
		final var fqn = parsed.fqn();
		final var dotIdx = fqn.lastIndexOf('.');
		if (dotIdx < 0 || dotIdx == fqn.length() - 1)
			return new SkipResult(SkipMessages.UNUSED_IMPORTS_MALFORMED);
		if (fqn.startsWith(JAVA_LANG_PREFIX) && fqn.indexOf('.', JAVA_LANG_PREFIX.length()) < 0)
			return LineDeletion.deleteRange(lines, lineIndex, lineIndex);
		final var simpleName = fqn.substring(dotIdx + 1);
		// Scan ALL non-violation lines; the scanner's unqualified-only check
		// rejects matches preceded by '.', so other import lines and package
		// qualifiers cannot false-positive. Avoid filtering by line prefix
		// since that breaks the scanner's text-block state if a text block
		// happens to contain a line starting with "import " or "package ".
		final var bodyLines = new ArrayList<String>(lines.size());
		for (var i = 0; i < lines.size(); ++i) {
			if (i == lineIndex)
				continue;
			bodyLines.add(lines.get(i));
		}
		if (JavaSourceScanner.containsUnqualifiedIdentifier(bodyLines, simpleName))
			return new SkipResult(SkipMessages.UNUSED_IMPORTS_NOW_USED);
		return LineDeletion.deleteRange(lines, lineIndex, lineIndex);
	}
}