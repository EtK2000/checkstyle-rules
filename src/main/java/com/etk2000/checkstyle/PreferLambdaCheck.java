package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;

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
		// iterative: a deeply nested expression must not overflow the stack here, where the Error
		// would abort the run rather than one file
		final var pending = new ArrayDeque<DetailAST>();
		pending.push(ast);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
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
					// a nested anonymous class has its own this/super
					case TokenTypes.OBJBLOCK -> {}
					default -> pending.push(child);
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

		final var typeName = AstText.findNewClassName(literalNew);
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
		return new int[]{TokenTypes.LITERAL_NEW};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_NEW)
			checkAnonymousClass(ast);
	}
}