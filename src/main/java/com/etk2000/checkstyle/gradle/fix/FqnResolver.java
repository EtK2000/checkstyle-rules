package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLangClasses;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shared resolver that maps a Java type/class name (a simple name or a dotted
 * chain) to the fully-qualified name the compiler would bind it to from a
 * single source file, using only information visible in that file: explicit
 * single-type imports, sibling source files in the same package directory, and
 * the implicit {@code java.lang} import. Also owns the source-line
 * classification primitives (text-block masking and comment stripping) the
 * resolution relies on, so callers don't duplicate them.
 *
 * <p>{@link #resolve} returns a {@link Resolution} carrying both the resolved
 * FQN and the {@link ResolutionSource} that bound it, so callers can apply
 * their own precedence policy (e.g. treating a wildcard-fallback resolution as
 * unconfirmed). {@link #resolveFqcn} is the legacy string-only entry preserving
 * the exact behavior {@code PreferStaticImportConstantFixer} relied on.
 */
final class FqnResolver {
	enum ResolutionSource {
		EXPLICIT_IMPORT,
		JAVA_LANG,
		NONE,
		SAME_PACKAGE_SIBLING,
		WILDCARD
	}

	record Resolution(@Nullable String fqn, @Nonnull ResolutionSource source) {
		static final Resolution NONE = new Resolution(null, ResolutionSource.NONE);
	}

	/**
	 * Per-line "starts inside" masks computed in a single source scan: for each
	 * line index, whether that line begins inside an unterminated block comment
	 * and/or text block. Both are produced together because they share the same
	 * scan.
	 */
	record LineMasks(@Nonnull boolean[] inBlockComment, @Nonnull boolean[] inTextBlock) {
		/**
		 * Returns a per-line mask that is true where the line begins inside a
		 * block comment or a text block, i.e. lines whose content must not be
		 * scanned as source (imports, package declarations, type references).
		 */
		@CheckReturnValue
		@Nonnull
		boolean[] maskedLines() {
			final var masked = new boolean[inTextBlock.length];
			for (var i = 0; i < masked.length; ++i)
				masked[i] = inBlockComment[i] || inTextBlock[i];
			return masked;
		}
	}

	private static final char BOM = '﻿';

	@CheckReturnValue
	@Nonnull
	static LineMasks computeLineMasks(@Nonnull List<String> lines) {
		final var inBlockCommentMask = new boolean[lines.size()];
		final var inTextBlockMask = new boolean[lines.size()];
		var state = LexerState.NONE;
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			inBlockCommentMask[lineIdx] = state.inBlockComment();
			inTextBlockMask[lineIdx] = state.inTextBlock();
			state = JavaLineScanner.stateAfter(lines.get(lineIdx), state);
		}
		return new LineMasks(inBlockCommentMask, inTextBlockMask);
	}

	@CheckReturnValue
	@Nonnull
	static Resolution resolve(
			@Nonnull List<String> lines,
			@Nonnull boolean[] skipMask,
			@Nonnull String simpleClass,
			@Nullable String filePath
	) {
		String packageName = null;
		String wildcardCandidate = null;
		var wildcardCount = 0;
		for (var lineIdx = 0; lineIdx < lines.size(); ++lineIdx) {
			if (skipMask[lineIdx])
				continue;
			final var stripped = stripCommentsAndBom(lines.get(lineIdx), lineIdx);
			if (stripped.startsWith("package ") && stripped.endsWith(";")) {
				packageName = stripped.substring("package ".length(), stripped.length() - 1)
						.replaceAll("\\s+", "");
				continue;
			}
			if (!stripped.startsWith("import "))
				continue;
			if (stripped.startsWith("import static "))
				continue;
			if (!stripped.endsWith(";"))
				continue;
			final var fqn = stripped.substring("import ".length(), stripped.length() - 1)
					.replaceAll("\\s+", "");
			if (fqn.endsWith(".*")) {
				final var wildcardPrefix = fqn.substring(0, fqn.length() - 2);
				if (wildcardPrefix.isEmpty() || wildcardPrefix.startsWith(".") || wildcardPrefix.endsWith("."))
					continue;
				++wildcardCount;
				if (wildcardCandidate == null)
					wildcardCandidate = wildcardPrefix + "." + simpleClass;
				continue;
			}
			final var lastDot = fqn.lastIndexOf('.');
			if (lastDot <= 0 || lastDot == fqn.length() - 1)
				continue;
			final var simple = fqn.substring(lastDot + 1);
			if (simple.equals(simpleClass))
				return new Resolution(fqn, ResolutionSource.EXPLICIT_IMPORT);
		}

		if (filePath != null) {
			try {
				final var parentDir = Path.of(filePath).getParent();
				if (parentDir != null && Files.exists(parentDir.resolve(simpleClass + ".java"))) {
					if (packageName != null && !packageName.isEmpty())
						return new Resolution(packageName + "." + simpleClass, ResolutionSource.SAME_PACKAGE_SIBLING);
					return new Resolution(simpleClass, ResolutionSource.SAME_PACKAGE_SIBLING);
				}
			}
			catch (InvalidPathException ignored) {
			}
		}

		// java.lang is implicitly imported; if no explicit import already
		// named this simple name (handled above), resolve to java.lang. The
		// wildcard fallback below would otherwise misroute e.g. `Math.PI`
		// through a wildcard-imported sibling package.
		if (JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(simpleClass))
			return new Resolution("java.lang." + simpleClass, ResolutionSource.JAVA_LANG);

		// Non-java.lang fallback: when a single wildcard import is in scope
		// we assume the class lives there. This may misroute when the class
		// actually lives elsewhere (sibling package or another wildcard),
		// but without sibling-package class lookup we have no better signal.
		if (wildcardCount == 1)
			return new Resolution(wildcardCandidate, ResolutionSource.WILDCARD);
		return Resolution.NONE;
	}

	@CheckReturnValue
	@Nullable
	static String resolveFqcn(
			@Nonnull List<String> lines,
			@Nonnull boolean[] inTextBlockMask,
			@Nonnull String classChain,
			@Nullable String filePath
	) {
		final var firstDot = classChain.indexOf('.');
		if (firstDot >= 0) {
			final var first = classChain.substring(0, firstDot);
			if (!first.isEmpty() && Character.isLowerCase(first.charAt(0)))
				return classChain;
			final var leftmostFqn = resolve(lines, inTextBlockMask, first, filePath).fqn();
			return leftmostFqn == null ? null : leftmostFqn + classChain.substring(firstDot);
		}
		return resolve(lines, inTextBlockMask, classChain, filePath).fqn();
	}

	@CheckReturnValue
	@Nonnull
	static String stripCommentsAndBom(@Nonnull String line, int lineIdx) {
		final var stripped = stripCommentsForClassification(line);
		return lineIdx == 0 && !stripped.isEmpty() && stripped.charAt(0) == BOM
				? stripped.substring(1)
				: stripped;
	}

	@CheckReturnValue
	@Nonnull
	static String stripCommentsForClassification(@Nonnull String line) {
		return stripCommentsForClassification(line, false);
	}

	@CheckReturnValue
	@Nonnull
	static String stripCommentsForClassification(@Nonnull String line, boolean startsInTextBlock) {
		return JavaLineScanner.stripCommentsAndStrings(line, new LexerState(false, startsInTextBlock)).strip();
	}
}