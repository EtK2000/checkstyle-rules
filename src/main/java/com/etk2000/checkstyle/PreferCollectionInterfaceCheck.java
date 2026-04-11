package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags concrete collection types in method and
 * constructor signatures (return types and parameter types). Suggests
 * the corresponding interface type instead. Uses reflection to
 * dynamically determine the interface rather than a hardcoded map.
 * Recursively checks nested generic type arguments.
 */
public class PreferCollectionInterfaceCheck extends AbstractCheck {
	private static final String MSG = "prefer.collection.interface";

	@CheckReturnValue
	@Nullable
	private static DetailAST findType(@Nonnull DetailAST ast) {
		return ast.findFirstToken(TokenTypes.TYPE);
	}

	private final Set<String> imports = new HashSet<>();

	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
	}

	private void checkParameters(@Nonnull DetailAST ast) {
		final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;

		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() == TokenTypes.PARAMETER_DEF) {
				final var paramType = findType(param);
				if (paramType != null)
					checkTypeTree(paramType);
			}
		}
	}

	private void checkTypeTree(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.DOT) {
				final var fullName = FullIdent.createFullIdent(child).getText();
				final var iface = ReflectionUtil.findCollectionInterface(fullName);
				if (iface != null) {
					final var simpleName = fullName.substring(fullName.lastIndexOf('.') + 1);
					// log at the last IDENT child so the fixer column targets the simple name
					DetailAST lastIdent = null;
					for (var c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
						if (c.getType() == TokenTypes.IDENT)
							lastIdent = c;
					}
					if (lastIdent != null)
						log(lastIdent, MSG, iface, simpleName);
					continue;
				}
			}
			else if (child.getType() == TokenTypes.IDENT) {
				final var fqcn = ReflectionUtil.resolveClassName(child.getText(), packageName, imports);
				if (fqcn != null) {
					final var iface = ReflectionUtil.findCollectionInterface(fqcn);
					if (iface != null)
						log(child, MSG, iface, child.getText());
				}
			}
			checkTypeTree(child);
		}
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.CTOR_DEF,
				TokenTypes.IMPORT,
				TokenTypes.METHOD_DEF,
				TokenTypes.PACKAGE_DEF
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.CTOR_DEF -> checkParameters(ast);
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.METHOD_DEF -> {
				final var returnType = findType(ast);
				if (returnType != null)
					checkTypeTree(returnType);
				checkParameters(ast);
			}
			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
		}
	}
}