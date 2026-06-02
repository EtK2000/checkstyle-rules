package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

final class JavaSourceScanner {
	private record ScanResult(boolean inBlockComment, boolean inTextBlock, boolean found) {}

	@CheckReturnValue
	static boolean containsIdentifier(@Nonnull List<String> lines, @Nonnull String identifier) {
		return scan(lines, identifier, false);
	}

	/**
	 * Like {@link #containsIdentifier} but additionally rejects matches where
	 * the preceding character is {@code '.'} — i.e. only counts occurrences that
	 * would resolve via an unqualified reference, not part of a fully-qualified
	 * name like {@code org.junit.Assert.assertTrue}.
	 */
	@CheckReturnValue
	static boolean containsUnqualifiedIdentifier(@Nonnull List<String> lines, @Nonnull String identifier) {
		return scan(lines, identifier, true);
	}

	@CheckReturnValue
	private static boolean scan(@Nonnull List<String> lines, @Nonnull String identifier, boolean unqualifiedOnly) {
		if (identifier.isEmpty())
			return false;
		for (var i = 0; i < identifier.length(); ++i) {
			if (!Character.isJavaIdentifierPart(identifier.charAt(i)))
				return false;
		}
		var inBlockComment = false;
		var inTextBlock = false;
		for (var line : lines) {
			final var result = scanLine(line, identifier, inBlockComment, inTextBlock, unqualifiedOnly);
			if (result.found())
				return true;
			inBlockComment = result.inBlockComment();
			inTextBlock = result.inTextBlock();
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static ScanResult scanLine(
			@Nonnull String line,
			@Nonnull String identifier,
			boolean inBlockCommentStart,
			boolean inTextBlockStart,
			boolean unqualifiedOnly
	) {
		var inBlockComment = inBlockCommentStart;
		var inTextBlock = inTextBlockStart;
		var inString = false;
		var inChar = false;
		var found = false;
		final var len = line.length();
		final var idLen = identifier.length();
		for (var i = 0; i < len; ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < len && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inTextBlock) {
				if (c == '\\' && i + 1 < len) {
					++i;
					continue;
				}
				if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = false;
					i += 2;
				}
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < len)
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < len)
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < len) {
				final var next = line.charAt(i + 1);
				if (next == '/')
					break;
				if (next == '*') {
					inBlockComment = true;
					++i;
					continue;
				}
			}
			if (c == '"' && i + 2 < len && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
				inTextBlock = true;
				i += 2;
				continue;
			}
			if (c == '"') {
				inString = true;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				continue;
			}
			if (!found && c == identifier.charAt(0) && i + idLen <= len
					&& line.regionMatches(i, identifier, 0, idLen)) {
				final var before = i > 0 ? line.charAt(i - 1) : ' ';
				final var afterIdx = i + idLen;
				final var after = afterIdx < len ? line.charAt(afterIdx) : ' ';
				if (!Character.isJavaIdentifierPart(before) && !Character.isJavaIdentifierPart(after)
						&& (!unqualifiedOnly || before != '.'))
					found = true;
			}
		}
		return new ScanResult(inBlockComment, inTextBlock, found);
	}

	private JavaSourceScanner() {
	}
}