package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Base for checks that resolve simple type names against the file's imports and
 * package declaration. Owns that per-file scope so a subclass neither declares
 * nor resets it, and exposes the two resolutions that consume it.
 *
 * <p>The scope is read off the compilation unit's own children, so it is complete
 * before the first token arrives and stays independent of which tokens a subclass
 * registers and of the order they are visited in.
 *
 * <p>{@link TokenTypes#STATIC_IMPORT} is deliberately left alone: it is a distinct
 * token that a subclass may want for its own rule.
 */
abstract class AbstractResolvingCheck extends AbstractMinSdkCheck {
	private final Map<String, String> resolved = new HashMap<>();
	private final Set<String> imports = new HashSet<>();

	private String packageName;

	/**
	 * Per-file setup hook, called once the resolution scope is populated, so
	 * {@link #resolve} and {@link #receiverTypeName} already answer here.
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
		resolved.clear();
		if (rootAST != null) {
			for (var child = rootAST.getFirstChild(); child != null; child = child.getNextSibling()) {
				switch (child.getType()) {
					case TokenTypes.IMPORT -> {
						final var imported = FullIdent.createFullIdentBelow(child).getText();
						if (!imported.isEmpty())
							imports.add(imported);
					}
					case TokenTypes.PACKAGE_DEF -> packageName = AstText.getPackageName(child);
				}
			}
		}
		beginFile(rootAST);
	}

	/**
	 * Per-file teardown hook, called while the resolution scope is still populated
	 * so a subclass can resolve names while emitting whatever it deferred.
	 */
	protected void finishFile(@Nullable DetailAST rootAST) {
	}

	@Override
	public final void finishTree(@Nullable DetailAST rootAST) {
		finishFile(rootAST);
		imports.clear();
		packageName = null;
		resolved.clear();
	}

	/**
	 * The fully qualified name of {@code methodCall}'s receiver type resolved
	 * through this file's scope, or null when it cannot be determined.
	 */
	@CheckReturnValue
	@Nullable
	protected final String receiverTypeName(@Nonnull DetailAST methodCall) {
		return AstResolve.getReceiverTypeName(methodCall, packageName, imports);
	}

	/**
	 * The fully qualified name {@code simpleName} refers to in this file, or null
	 * when it resolves to nothing. Memoized for the file, including the misses:
	 * every type variable in a signature reaches this and resolves to nothing.
	 */
	@CheckReturnValue
	@Nullable
	protected final String resolve(@Nonnull String simpleName) {
		if (resolved.containsKey(simpleName))
			return resolved.get(simpleName);

		final var fqcn = ReflectionUtil.resolveClassName(simpleName, packageName, imports);
		resolved.put(simpleName, fqcn);
		return fqcn;
	}

	protected abstract void visitScopedToken(@Nonnull DetailAST ast);

	@Override
	public final void visitToken(@Nonnull DetailAST ast) {
		visitScopedToken(ast);
	}
}