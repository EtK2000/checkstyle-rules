package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.ReflectionUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferCollectionInterfaceFixer implements CheckstyleFixer {
	private static final String IMPORT_PREFIX = "import ";
	private static final String PACKAGE_PREFIX = "package ";
	private static final String STATIC_IMPORT_PREFIX = "import static ";

	@CheckReturnValue
	@Nonnull
	private static Set<String> collectImports(@Nonnull List<String> lines) {
		final var imports = new HashSet<String>();
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var trimmed = JavaLineScanner.stripCommentsAndStrings(line, state).strip();
			state = JavaLineScanner.stateAfter(line, state);
			if (trimmed.startsWith(IMPORT_PREFIX) && !trimmed.startsWith(STATIC_IMPORT_PREFIX) && trimmed.endsWith(";"))
				imports.add(trimmed.substring(IMPORT_PREFIX.length(), trimmed.length() - 1).strip());
		}
		return imports;
	}

	@CheckReturnValue
	@Nullable
	private static String findPackageName(@Nonnull List<String> lines) {
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var trimmed = JavaLineScanner.stripCommentsAndStrings(line, state).strip();
			state = JavaLineScanner.stateAfter(line, state);
			if (trimmed.startsWith(PACKAGE_PREFIX) && trimmed.endsWith(";"))
				return trimmed.substring(PACKAGE_PREFIX.length(), trimmed.length() - 1).strip();
		}
		return null;
	}

	/**
	 * Returns the type name ending at {@code end}, including any dotted qualifier
	 * preceding the simple name at {@code column}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String qualifiedNameEndingAt(@Nonnull String line, int column, int end) {
		var start = column;
		while (start > 0) {
			final var c = line.charAt(start - 1);
			if (c != '.' && !Character.isJavaIdentifierPart(c))
				break;
			--start;
		}
		return line.substring(start, end);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var end = LineText.identEnd(line, column);
		if (end <= column)
			return null;

		final var fullName = qualifiedNameEndingAt(line, column, end);
		final String iface;
		if (fullName.indexOf('.') >= 0)
			iface = ReflectionUtil.findCollectionInterface(fullName);
		else {
			final var fqcn = ReflectionUtil.resolveClassName(fullName, findPackageName(lines), collectImports(lines));
			iface = fqcn == null ? null : ReflectionUtil.findCollectionInterface(fqcn);
		}
		if (iface == null)
			return new SkipResult(SkipMessages.COLLECTION_INTERFACE_SKIP);

		final var replaceStart = end - fullName.length();
		final var fixed = line.substring(0, replaceStart) + iface + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(fixed), Set.of("java.util." + iface));
	}
}