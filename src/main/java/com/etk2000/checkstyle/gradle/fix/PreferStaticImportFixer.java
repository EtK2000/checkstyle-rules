package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.PreferStaticImportCheck;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferStaticImportFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var line = lines.get(lineIndex);
		if (charColumn < 0 || charColumn >= line.length())
			return null;

		// the check reports the receiver's own first character, so anything to the left of it means
		// the column is stale (a sibling fix rewrote this line earlier in the pass) or the receiver
		// is qualified; splicing there glues two identifiers together into one that compiles
		if (charColumn > 0) {
			final var before = line.codePointBefore(charColumn);
			if (before == '.' || Character.isJavaIdentifierPart(before))
				return null;
		}

		var end = charColumn;
		while (end < line.length() && Character.isJavaIdentifierPart(line.charAt(end)))
			++end;
		final var simpleClass = line.substring(charColumn, end);
		final var fqcn = PreferStaticImportCheck.SIMPLE_TO_FQCN.get(simpleClass);
		if (fqcn == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_SKIP);

		if (end >= line.length() || line.charAt(end) != '.')
			return null;
		final var dotPos = end;

		var methodEnd = dotPos + 1;
		while (methodEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(methodEnd)))
			++methodEnd;
		final var simpleMethod = line.substring(dotPos + 1, methodEnd);
		if (simpleMethod.isEmpty())
			return null;

		final var newLine = line.substring(0, charColumn) + line.substring(dotPos + 1);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(newLine),
				Set.of("static " + fqcn + "." + simpleMethod)
		);
	}
}