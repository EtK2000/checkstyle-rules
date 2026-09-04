package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base for checks that resolve simple type names against the file's imports and
 * package declaration. Owns that per-file scope so a subclass neither declares
 * nor resets it, and exposes the two resolutions that consume it.
 *
 * <p>A subclass MUST list {@link TokenTypes#IMPORT} and
 * {@link TokenTypes#PACKAGE_DEF} in its {@code getDefaultTokens()}, or the scope
 * stays empty and every simple name silently resolves to nothing.
 *
 * <p>{@link TokenTypes#STATIC_IMPORT} is deliberately not consumed here: it is a
 * distinct token that a subclass may want for its own rule.
 */
abstract class AbstractResolvingCheck extends AbstractMinSdkCheck {
	private final Set<String> imports = new HashSet<>();

	private String packageName;

	/**
	 * Per-file setup hook, called once the resolution scope has been reset.
	 *
	 * @param rootAST null for a file with no compilation unit, i.e. one that is
	 *                empty or holds nothing but comments
	 */
	protected void beginFile(@Nullable DetailAST rootAST) {
	}

	@Override
	public final void beginTree(@Nullable DetailAST rootAST) {
		imports.clear();
		packageName = null;
		beginFile(rootAST);
	}

	/**
	 * The fully qualified name of {@code methodCall}'s receiver type resolved
	 * through this file's scope, or null when it cannot be determined.
	 */
	@CheckReturnValue
	@Nullable
	protected final String receiverTypeName(@Nonnull DetailAST methodCall) {
		return AstUtil.getReceiverTypeName(methodCall, packageName, imports);
	}

	/**
	 * The fully qualified name {@code simpleName} refers to in this file, or null
	 * when it resolves to nothing.
	 */
	@CheckReturnValue
	@Nullable
	protected final String resolve(@Nonnull String simpleName) {
		return ReflectionUtil.resolveClassName(simpleName, packageName, imports);
	}

	protected abstract void visitScopedToken(@Nonnull DetailAST ast);

	@Override
	public final void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.PACKAGE_DEF -> packageName = AstUtil.getPackageName(ast);
			default -> visitScopedToken(ast);
		}
	}
}