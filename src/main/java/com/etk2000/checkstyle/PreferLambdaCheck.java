package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags anonymous class implementations of
 * functional interfaces that could be replaced with lambda expressions.
 * Only flags anonymous classes with a single method and no extra members
 * (fields, inner types, etc.).
 */
public class PreferLambdaCheck extends AbstractResolvingCheck {
	private static final String MSG = "prefer.lambda";

	@CheckReturnValue
	private static boolean containsThisOrSuperReference(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_SUPER -> {
					return true;
				}

				case TokenTypes.LITERAL_THIS -> {
					// Qualified this (e.g. Outer.this) is fine in a lambda
					final var parent = child.getParent();
					if (parent.getType() != TokenTypes.DOT || parent.getFirstChild() == child)
						return true;
				}

				// Don't recurse into nested anonymous classes, they have their own this/super
				case TokenTypes.OBJBLOCK -> {}

				default -> {
					if (containsThisOrSuperReference(child))
						return true;
				}
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isSimpleAnonymousClass(@Nonnull DetailAST objBlock) {
		var methodCount = 0;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LCURLY, TokenTypes.RCURLY -> {}
				case TokenTypes.METHOD_DEF -> {
					if (++methodCount > 1)
						return false;
				}
				default -> {
					return false;
				}
			}
		}
		return methodCount == 1;
	}

	private void checkAnonymousClass(@Nonnull DetailAST literalNew) {
		final var objBlock = literalNew.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return;

		if (!isSimpleAnonymousClass(objBlock))
			return;

		if (containsThisOrSuperReference(objBlock))
			return;

		final var typeName = AstUtil.findNewClassName(literalNew);
		if (typeName == null)
			return;

		final var fqcn = resolve(typeName);
		if (fqcn == null)
			return;

		if (ReflectionUtil.isFunctionalInterface(fqcn))
			log(literalNew, MSG, typeName);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.IMPORT,
				TokenTypes.LITERAL_NEW,
				TokenTypes.PACKAGE_DEF
		};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_NEW)
			checkAnonymousClass(ast);
	}
}