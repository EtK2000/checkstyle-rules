package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferStaticImportFixer implements CheckstyleFixer {
	private static final Map<String, String> SIMPLE_TO_FQCN = Map.of(
			"Collectors", "java.util.stream.Collectors",
			"Objects", "java.util.Objects",
			"Predicate", "java.util.function.Predicate"
	);

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		// read the receiver IDENT at column
		var end = column;
		while (end < line.length() && Character.isJavaIdentifierPart(line.charAt(end)))
			++end;
		final var simpleClass = line.substring(column, end);
		final var fqcn = SIMPLE_TO_FQCN.get(simpleClass);
		if (fqcn == null)
			return new SkipResult(SkipMessages.PREFER_STATIC_IMPORT_SKIP);

		// require a dot immediately after the receiver
		if (end >= line.length() || line.charAt(end) != '.')
			return null;
		final var dotPos = end;

		// read the method IDENT after the dot
		var methodEnd = dotPos + 1;
		while (methodEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(methodEnd)))
			++methodEnd;
		final var simpleMethod = line.substring(dotPos + 1, methodEnd);
		if (simpleMethod.isEmpty())
			return null;

		// strip "ClassName." in place; the call becomes "method(...)"
		final var newLine = line.substring(0, column) + line.substring(dotPos + 1);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(newLine),
				Set.of("static " + fqcn + "." + simpleMethod)
		);
	}
}