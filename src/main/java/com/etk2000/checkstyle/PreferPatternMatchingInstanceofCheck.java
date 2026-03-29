package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags {@code instanceof} checks where the checked type
 * is subsequently cast to, preferring pattern matching
 * ({@code x instanceof Foo f}) instead.
 */
public class PreferPatternMatchingInstanceofCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.pattern.instanceof";

	@CheckReturnValue
	private static boolean containsCastTo(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprText) {
		if (ast.getType() == TokenTypes.TYPECAST) {
			final var castType = ast.findFirstToken(TokenTypes.TYPE);
			final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
			final var castExpr = rparen != null ? rparen.getNextSibling() : null;
			if (castType != null && castExpr != null
					&& typeName.equals(typeText(castType))
					&& exprText.equals(exprText(castExpr)))
				return true;
		}
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (containsCastTo(child, typeName, exprText))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String exprText(@Nonnull DetailAST ast) {
		if (ast.getChildCount() == 0)
			return ast.getText();

		final var sb = new StringBuilder();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			sb.append(exprText(child));
		return sb.toString();
	}

	@CheckReturnValue
	private static DetailAST findIfBody(@Nonnull DetailAST instanceofAst) {
		// walk up to find the enclosing LITERAL_IF
		var parent = instanceofAst.getParent();
		while (parent != null && parent.getType() != TokenTypes.LITERAL_IF)
			parent = parent.getParent();
		if (parent == null)
			return null;

		// the "then" body is the SLIST or single statement after RPAREN
		return parent.findFirstToken(TokenTypes.SLIST);
	}

	@CheckReturnValue
	@Nonnull
	private static String typeText(@Nonnull DetailAST type) {
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null)
			return exprText(dot);
		return "";
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_INSTANCEOF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// already using pattern matching
		if (ast.findFirstToken(TokenTypes.PATTERN_VARIABLE_DEF) != null)
			return;

		final var expr = ast.getFirstChild();
		final var type = ast.findFirstToken(TokenTypes.TYPE);
		if (expr == null || type == null)
			return;

		final var typeName = typeText(type);
		final var exprStr = exprText(expr);
		if (typeName.isEmpty())
			return;

		final var body = findIfBody(ast);
		if (body == null)
			return;

		if (containsCastTo(body, typeName, exprStr))
			log(ast, MSG_KEY, typeName);
	}
}