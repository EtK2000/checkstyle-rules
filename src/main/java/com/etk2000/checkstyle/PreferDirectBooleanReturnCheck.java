package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags {@code if (cond) return BOOL_LIT;} paired with an opposite-literal
 * {@code return} (either as the immediately following sibling statement or as
 * the {@code else} branch). Such pairs collapse to a single
 * {@code return cond;} (or {@code return !cond;}) without any change in
 * behavior.
 *
 * <p>Examples that fire:
 * <ul>
 *     <li>{@code if (x) return true; return false;} -> {@code return x;}</li>
 *     <li>{@code if (x) return false; return true;} -> {@code return !x;}</li>
 *     <li>{@code if (x) return true; else return false;} -> {@code return x;}</li>
 *     <li>{@code if (!x) return false; return true;} -> {@code return x;} (double-negation)</li>
 * </ul>
 *
 * <p>Both the {@code if} body and the paired return must be exactly a single
 * {@code return true;} or {@code return false;}.
 */
public class PreferDirectBooleanReturnCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.direct.boolean.return";

	@CheckReturnValue
	@Nullable
	private static Boolean returnLiteralValue(@Nonnull DetailAST stmt) {
		if (stmt.getType() != TokenTypes.LITERAL_RETURN)
			return null;
		final var expr = stmt.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return null;
		final var value = expr.getFirstChild();
		if (value == null)
			return null;
		return switch (value.getType()) {
			case TokenTypes.LITERAL_FALSE -> Boolean.FALSE;
			case TokenTypes.LITERAL_TRUE -> Boolean.TRUE;
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapSingleStatementBlock(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.SLIST)
			return body;
		DetailAST single = null;
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.SEMI || child.getType() == TokenTypes.RCURLY)
				continue;
			if (single != null)
				return null;
			single = child;
		}
		return single;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
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
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return;

		final var thenStmt = unwrapSingleStatementBlock(thenBody);
		if (thenStmt == null)
			return;
		final var thenValue = returnLiteralValue(thenStmt);
		if (thenValue == null)
			return;

		final Boolean elseValue;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return;
			final var elseStmt = unwrapSingleStatementBlock(elseBody);
			if (elseStmt == null)
				return;
			elseValue = returnLiteralValue(elseStmt);
		}
		else {
			final var nextSibling = ast.getNextSibling();
			if (nextSibling == null)
				return;
			elseValue = returnLiteralValue(nextSibling);
		}
		if (elseValue == null || elseValue.equals(thenValue))
			return;

		log(ast, MSG_KEY);
	}
}