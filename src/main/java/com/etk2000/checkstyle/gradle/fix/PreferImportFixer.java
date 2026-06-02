package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FqnResolver.stripCommentsAndBom;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferImportCheck;
import com.etk2000.checkstyle.ReflectionUtil;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Narrow fixer for {@link com.etk2000.checkstyle.PreferImportCheck}: replaces a
 * fully-qualified type name with its simple name, but only when the simple name
 * provably re-binds to exactly that FQN from this single file. It never inserts
 * a new import (that is the general fixer's job); it only strips a qualifier
 * that is already resolvable via a single-type import, a same-package type, or
 * the implicit {@code java.lang} import.
 *
 * <p>It strips only to a single simple name. A nested-type access such as
 * {@code Map.Entry} (from {@code java.util.Map.Entry} when only {@code Map} is
 * imported) is itself a qualified name the check re-flags, so producing it
 * would not resolve the violation; those cases are skipped, leaving the import
 * decision to the developer.
 *
 * <p>Resolution delegates to {@link FqnResolver} (shared with
 * {@code PreferStaticImportConstantFixer}); this fixer layers on the extra
 * safety gates the strip needs: an in-file shadow guard (a nested type or type
 * parameter with the same simple name), a wildcard gate on {@code java.lang}
 * names, and a reflection check for a same-package type contributed by a
 * dependency. Every case it cannot prove safe returns a {@link SkipResult}.
 */
final class PreferImportFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	private static Set<String> collectShadowingTypeNames(@Nonnull List<String> lines) {
		return FixerAst.withAst(
				lines,
				root -> {
				final var names = new HashSet<String>();
				collectTypeNames(root, names);
				return names;
				}
		);
	}

	private static void collectTypeNames(@Nonnull DetailAST node, @Nonnull Set<String> out) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				     TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF, TokenTypes.TYPE_PARAMETER -> {
					final var ident = child.findFirstToken(TokenTypes.IDENT);
					if (ident != null)
						out.add(ident.getText());
				}
				default -> {
				}
			}
			collectTypeNames(child, out);
		}
	}

	@CheckReturnValue
	@Nullable
	private static String findPackageName(@Nonnull List<String> lines, @Nonnull boolean[] maskedLines) {
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (maskedLines[lineIdx])
				continue;
			final var stripped = stripCommentsAndBom(lines.get(lineIdx), lineIdx);
			if (stripped.startsWith("package ") && stripped.endsWith(";"))
				return stripped.substring("package ".length(), stripped.length() - 1).replaceAll("\\s+", "");
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static int[] findQualifiedName(@Nonnull String line, int fromColumn) {
		// the reported column counts code points; used raw it starts the scan early on
		// a line with a supplementary character and can latch onto an earlier dotted run
		var i = LineText.charIndexOfColumn(line, Math.clamp(fromColumn, 0, line.codePointCount(0, line.length())));
		// the check reports the column at the dotted run's first token for a
		// plain type, but at a preceding keyword/operator (`new`, `@`) for
		// others; if it lands inside a dotted run, rewind to that run's start
		if (i < line.length() && (Character.isJavaIdentifierPart(line.charAt(i)) || line.charAt(i) == '.')) {
			while (i > 0 && (Character.isJavaIdentifierPart(line.charAt(i - 1)) || line.charAt(i - 1) == '.'))
				--i;
		}
		while (i < line.length()) {
			if (!Character.isJavaIdentifierStart(line.charAt(i))) {
				++i;
				continue;
			}
			final var runStart = i;
			while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
				++i;
			var runEnd = i;
			var hasDot = false;
			var j = i;
			while (j < line.length() && line.charAt(j) == '.'
					&& j + 1 < line.length() && Character.isJavaIdentifierStart(line.charAt(j + 1))) {
				hasDot = true;
				j += 2;
				while (j < line.length() && Character.isJavaIdentifierPart(line.charAt(j)))
					++j;
				runEnd = j;
			}
			if (hasDot)
				return new int[]{runStart, runEnd};
			// a dotless identifier (e.g. `new`, `extends`, `throws`); skip it and
			// keep scanning for the qualified type name that follows
			i = runEnd;
		}
		return null;
	}

	@CheckReturnValue
	private static boolean hasWildcardImport(@Nonnull List<String> lines, @Nonnull boolean[] maskedLines) {
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (maskedLines[lineIdx])
				continue;
			final var stripped = stripCommentsAndBom(lines.get(lineIdx), lineIdx);
			if (stripped.startsWith("import ") && !stripped.startsWith("import static ") && stripped.endsWith(".*;"))
				return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var bounds = findQualifiedName(line, column);
		if (bounds == null)
			return new SkipResult(SkipMessages.IMPORT_SKIP_NON_CONTIGUOUS);
		final var start = bounds[0];
		final var rawRun = line.substring(start, bounds[1]);
		// bound the FQN to the longest dotted prefix that resolves to a class, so
		// an expression qualifier like `java.util.List.of` strips only the type
		// `java.util.List` and leaves `.of`; fall back to the whole run for an
		// unresolvable / off-classpath type, preserving TYPE-position behavior
		final var resolved = PreferImportCheck.resolvableTypePrefix(rawRun);
		final var fqn = resolved != null ? resolved : rawRun;
		final var end = start + fqn.length();
		final var lastDot = fqn.lastIndexOf('.');
		if (lastDot < 0)
			return new SkipResult(SkipMessages.IMPORT_SKIP_NON_CONTIGUOUS);
		final var simpleName = fqn.substring(lastDot + 1);

		final var shadowing = collectShadowingTypeNames(lines);
		if (shadowing == null)
			return new SkipResult(SkipMessages.IMPORT_SKIP_UNPARSEABLE);
		if (shadowing.contains(simpleName))
			return new SkipResult(SkipMessages.IMPORT_SKIP_SHADOW);

		final var maskedLines = FqnResolver.computeLineMasks(lines).maskedLines();
		final var packageName = findPackageName(lines, maskedLines);
		final var resolution = FqnResolver.resolve(lines, maskedLines, simpleName, FixContext.getFilePath());
		final var dependencyShadow = packageName != null
				&& ReflectionUtil.isResolvableClass(packageName + "." + simpleName);
		return switch (resolution.source()) {
			case EXPLICIT_IMPORT -> fqn.equals(resolution.fqn())
					? strip(line, lineIndex, start, end, simpleName, Set.of(resolution.fqn()))
					: new SkipResult(SkipMessages.IMPORT_SKIP_NAME_COLLISION);
			case JAVA_LANG -> {
				if (!fqn.equals(resolution.fqn()) || dependencyShadow)
					yield new SkipResult(SkipMessages.IMPORT_SKIP_NAME_COLLISION);
				if (hasWildcardImport(lines, maskedLines))
					yield new SkipResult(SkipMessages.IMPORT_SKIP_WILDCARD_AMBIGUITY);
				yield strip(line, lineIndex, start, end, simpleName, Set.of());
			}
			case NONE -> {
				if (!dependencyShadow)
					yield new SkipResult(SkipMessages.IMPORT_SKIP_UNRESOLVABLE);
				yield fqn.equals(packageName + "." + simpleName)
						? strip(line, lineIndex, start, end, simpleName, Set.of())
						: new SkipResult(SkipMessages.IMPORT_SKIP_NAME_COLLISION);
			}
			case SAME_PACKAGE_SIBLING -> fqn.equals(resolution.fqn())
					? strip(line, lineIndex, start, end, simpleName, Set.of())
					: new SkipResult(SkipMessages.IMPORT_SKIP_NAME_COLLISION);
			case WILDCARD -> new SkipResult(SkipMessages.IMPORT_SKIP_WILDCARD);
		};
	}

	@CheckReturnValue
	@Nonnull
	private FixAttempt strip(
			@Nonnull String line,
			int lineIndex,
			int start,
			int end,
			@Nonnull String keptName,
			@Nonnull Set<String> importsToAdd
	) {
		final var newLine = line.substring(0, start) + keptName + line.substring(end);
		return new FixResult(lineIndex, lineIndex, List.of(newLine), importsToAdd);
	}
}