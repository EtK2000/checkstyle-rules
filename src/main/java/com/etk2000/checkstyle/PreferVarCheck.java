package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that requires {@code var} instead of explicit types
 * in for-each loops, try-with-resources, and local variable declarations
 * (where the type is inferrable from the initializer).
 */
public class PreferVarCheck extends AbstractCheck {
	private static final String MSG_FOREACH = "prefer.var.foreach";
	private static final String MSG_LOCAL = "prefer.var.local";
	private static final String MSG_TRY = "prefer.var.try.resource";

	@CheckReturnValue
	private static boolean isInitializerNull(@Nonnull DetailAST assign) {
		var value = assign.getFirstChild();
		// unwrap EXPR wrapper
		if (value != null && value.getType() == TokenTypes.EXPR)
			value = value.getFirstChild();
		return value != null && value.getType() == TokenTypes.LITERAL_NULL;
	}

	@CheckReturnValue
	private static boolean isLocalVariable(@Nonnull DetailAST varDef) {
		final var parent = varDef.getParent();
		// local variables live in SLIST (block), not OBJBLOCK (class body)
		return parent != null && parent.getType() == TokenTypes.SLIST;
	}

	private void checkVarType(@Nonnull DetailAST varDef, @Nonnull String msgKey) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident == null || !"var".equals(ident.getText()))
			log(varDef, msgKey);
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
				TokenTypes.FOR_EACH_CLAUSE,
				TokenTypes.RESOURCE,
				TokenTypes.VARIABLE_DEF
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
			case TokenTypes.FOR_EACH_CLAUSE -> {
				final var varDef = ast.findFirstToken(TokenTypes.VARIABLE_DEF);
				if (varDef != null)
					checkVarType(varDef, MSG_FOREACH);
			}
			case TokenTypes.RESOURCE -> checkVarType(ast, MSG_TRY);
			case TokenTypes.VARIABLE_DEF -> {
				if (!isLocalVariable(ast))
					return;

				// must have an initializer
				final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
				if (assign == null)
					return;

				// skip null initializers (type can't be inferred)
				if (isInitializerNull(assign))
					return;

				checkVarType(ast, MSG_LOCAL);
			}
		}
	}
}