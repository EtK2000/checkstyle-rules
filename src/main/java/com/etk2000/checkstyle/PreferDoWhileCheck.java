package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags a {@code while} loop whose body is structurally
 * identical to the immediately preceding statement. Such code can be collapsed
 * into a {@code do-while}, which removes the duplicated statement.
 *
 * <p>Only fires for single-statement loop bodies (with or without braces) where
 * the previous sibling is a plain expression statement matching the body.
 * Multi-statement bodies are excluded: matching them would require the entire
 * body sequence to also appear as a contiguous pre-loop sequence, which the
 * check does not attempt.</p>
 */
public class PreferDoWhileCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.do.while";

	@CheckReturnValue
	@Nullable
	private static DetailAST previousExpressionStatement(@Nonnull DetailAST whileAst) {
		for (var prev = whileAst.getPreviousSibling(); prev != null; prev = prev.getPreviousSibling()) {
			if (prev.getType() == TokenTypes.SEMI)
				continue;
			if (prev.getType() == TokenTypes.EXPR)
				return prev.getFirstChild();
			return null;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST singleExpressionStatement(@Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.EXPR)
			return body.getFirstChild();
		if (body.getType() != TokenTypes.SLIST)
			return null;
		DetailAST stmt = null;
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			if (type == TokenTypes.SEMI)
				continue;
			if (type == TokenTypes.RCURLY)
				break;
			if (stmt != null)
				return null;
			stmt = child;
		}
		return stmt != null && stmt.getType() == TokenTypes.EXPR ? stmt.getFirstChild() : null;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_WHILE};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var body = rparen.getNextSibling();
		if (body == null)
			return;
		final var bodyExpr = singleExpressionStatement(body);
		if (bodyExpr == null)
			return;
		final var prevExpr = previousExpressionStatement(ast);
		if (prevExpr == null)
			return;
		if (AstUtil.astStructuralEquals(prevExpr, bodyExpr))
			log(ast, MSG_KEY);
	}
}