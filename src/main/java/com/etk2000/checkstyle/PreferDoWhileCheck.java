package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
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
public class PreferDoWhileCheck extends AbstractAstCheck {
	/**
	 * The {@code while} loop found at a located position, and the nodes a rewrite has to consume
	 * around it.
	 *
	 * <p>{@code bodyStatement} and {@code precedingStatement} are the {@code EXPR} nodes themselves,
	 * not the operator nodes beneath them. {@code condition} is the {@code EXPR} between the loop's
	 * parentheses.
	 */
	public record DoWhileTarget(
			@Nonnull DetailAST whileAst,
			@Nonnull DetailAST bodyStatement,
			@Nonnull DetailAST precedingStatement,
			@Nonnull DetailAST condition
	) {}

	private static final String MSG_KEY = "prefer.do.while";

	@CheckReturnValue
	@Nullable
	private static DetailAST previousExpressionStatement(@Nonnull DetailAST whileAst) {
		for (var prev = whileAst.getPreviousSibling(); prev != null; prev = prev.getPreviousSibling()) {
			// visitToken only ever sees the comment-free tree, since TreeWalker notifies an ordinary
			// check on its ORDINARY pass alone. The skip is for targetAt, which is public and whose
			// caller chooses the parse: a WITH_COMMENTS tree weaves comment nodes in as siblings
			if (prev.getType() == TokenTypes.SEMI || AstText.isCommentToken(prev.getType()))
				continue;
			if (prev.getType() == TokenTypes.EXPR)
				return prev;
			return null;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST singleExpressionStatement(@Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.EXPR)
			return body;
		if (body.getType() != TokenTypes.SLIST)
			return null;
		DetailAST stmt = null;
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			if (type == TokenTypes.SEMI || AstText.isCommentToken(type))
				continue;
			if (type == TokenTypes.RCURLY)
				break;
			if (stmt != null)
				return null;
			stmt = child;
		}
		return stmt != null && stmt.getType() == TokenTypes.EXPR ? stmt : null;
	}

	/**
	 * Returns the loop to rewrite at {@code line} (0-based) and {@code column} (a 0-based code-point
	 * column), or null when no reportable {@code while} starts exactly there.
	 */
	@CheckReturnValue
	@Nullable
	public static DoWhileTarget targetAt(@Nonnull DetailAST root, int line, int column) {
		final var whileAst = AstQuery.findNodeAt(root, line, column, ast -> ast.getType() == TokenTypes.LITERAL_WHILE);
		return whileAst == null ? null : targetFor(whileAst);
	}

	@CheckReturnValue
	@Nullable
	private static DoWhileTarget targetFor(@Nonnull DetailAST whileAst) {
		final var rparen = whileAst.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var body = rparen.getNextSibling();
		if (body == null)
			return null;
		final var bodyStatement = singleExpressionStatement(body);
		if (bodyStatement == null)
			return null;
		final var precedingStatement = previousExpressionStatement(whileAst);
		if (precedingStatement == null)
			return null;
		return AstQuery.astStructuralEquals(precedingStatement.getFirstChild(), bodyStatement.getFirstChild())
				? new DoWhileTarget(whileAst, bodyStatement, precedingStatement, rparen.getPreviousSibling())
				: null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_WHILE};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (targetFor(ast) != null)
			log(ast, MSG_KEY);
	}
}