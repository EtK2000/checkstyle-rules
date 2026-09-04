package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

abstract class AbstractAstCheck extends AbstractCheck {
	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	/**
	 * Logs a violation at {@code warning} severity regardless of the configured
	 * default, restoring the prior severity afterward so a later {@link #log}
	 * from the same tree walk keeps its own (typically error) severity. Lets a
	 * check own its per-violation severity in code rather than relying on
	 * per-module {@code severity} config.
	 */
	protected void logWarning(@Nonnull DetailAST ast, @Nonnull String msgKey, @Nonnull Object... args) {
		final var savedSeverity = getSeverity();
		try {
			setSeverity(SeverityLevel.WARNING.getName());
			log(ast, msgKey, args);
		}
		finally {
			setSeverity(savedSeverity);
		}
	}

	/**
	 * Whether a source file for a type named {@code simpleName} sits next to the checked file, so a
	 * same-package class the checkstyle classpath cannot load may declare that name.
	 */
	@CheckReturnValue
	protected boolean siblingSourceFileExists(@Nonnull String simpleName) {
		// a same-package sibling names a type, whose source file starts uppercase
		// by convention; skip the filesystem probe for lowercase package roots
		if (simpleName.isEmpty() || !Character.isUpperCase(simpleName.charAt(0)))
			return false;
		final var filePath = getFilePath();
		if (filePath == null)
			return false;
		try {
			final var parent = Path.of(filePath).getParent();
			return parent != null && Files.exists(parent.resolve(simpleName + ".java"));
		}
		catch (InvalidPathException | SecurityException ignored) {
			return false;
		}
	}
}