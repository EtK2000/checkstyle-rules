package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The names a compilation unit resolves simple type names through: its package and its imports.
 * {@link com.etk2000.checkstyle.ast.AstResolve#expressionTypeName} needs both, and a check holding
 * no state has nowhere to cache them, so this reads them off the tree on demand.
 */
record ResolutionScope(@Nullable String packageName, @Nonnull Set<String> imports) {
	static final ResolutionScope EMPTY = new ResolutionScope(null, Set.of());

	/**
	 * The scope of the compilation unit containing {@code node}, which may be the unit itself.
	 * Imports and the package declaration are direct children of the unit, so any node in the tree
	 * reaches them by walking to the root.
	 */
	@CheckReturnValue
	@Nonnull
	static ResolutionScope of(@Nullable DetailAST node) {
		if (node == null)
			return EMPTY;

		var unit = node;
		while (unit.getParent() != null)
			unit = unit.getParent();

		String packageName = null;
		final var imports = new HashSet<String>();
		for (var child = unit.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.IMPORT -> {
					final var imported = FullIdent.createFullIdentBelow(child).getText();
					if (!imported.isEmpty())
						imports.add(imported);
				}
				case TokenTypes.PACKAGE_DEF -> packageName = AstText.getPackageName(child);
			}
		}
		return new ResolutionScope(packageName, imports);
	}
}