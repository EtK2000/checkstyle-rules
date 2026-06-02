package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

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
}