package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags empty if/else-if/else bodies (error),
 * empty while/for/do-while bodies (warning, since spin-waits are a valid use case),
 * empty static/instance initializer blocks (error, no valid use case), and stray
 * empty statements (error).
 *
 * <p>A clause body written as a bare {@code ;} is accepted when a comment sits on the
 * semicolon's own line: the semicolon is load-bearing (the clause cannot be written
 * without it) and the comment is what marks the no-op deliberate. A braced empty body
 * gets no such escape, since {@code ;} plus a comment is the sanctioned form; neither
 * does a stray {@code ;}, which is not a clause body, so it can simply go while any
 * comment on its line stays.
 *
 * @see InfiniteEmptyLoopCheck for infinite empty loops at error severity
 */
public class EmptyBodyCheck extends AbstractAstCheck {
	private static final String MSG_DO = "empty.do";
	private static final String MSG_ELSE = "empty.else";
	private static final String MSG_FOR = "empty.for";
	private static final String MSG_IF = "empty.if";
	private static final String MSG_INSTANCE_INIT = "empty.instance.init";
	private static final String MSG_SEMI = "empty.semi";
	private static final String MSG_STATIC_INIT = "empty.static.init";
	private static final String MSG_WHILE = "empty.while";

	/**
	 * Whether {@code emptyStat}'s parent needs a statement there, so the {@code ;}
	 * cannot simply be deleted. {@code LABELED_STAT} qualifies on the same test
	 * ({@code label:} alone does not parse) but is not one of the clause forms the
	 * style rule covers, so it is left alone entirely rather than reported.
	 */
	@CheckReturnValue
	public static boolean isRequiredClauseBody(@Nonnull DetailAST emptyStat) {
		final var parent = emptyStat.getParent();
		return parent != null && switch (parent.getType()) {
			case TokenTypes.LABELED_STAT, TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE,
			     TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE -> true;
			default -> false;
		};
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.EMPTY_STAT,
				TokenTypes.INSTANCE_INIT,
				TokenTypes.LITERAL_DO,
				TokenTypes.LITERAL_FOR,
				TokenTypes.LITERAL_IF,
				TokenTypes.LITERAL_WHILE,
				TokenTypes.STATIC_INIT
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return new int[0];
	}

	/**
	 * Whether {@code body} is a bare {@code ;} sharing its physical line with a comment.
	 * Read off {@link com.puppycrawl.tools.checkstyle.api.FileContents}, which the lexer
	 * populates for every check: a trailing comment attaches forward in the AST, becoming
	 * a sibling of whatever follows the statement, so no comment-bearing tree can answer
	 * this.
	 */
	@CheckReturnValue
	private boolean isAnnotatedEmptyStatement(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.EMPTY_STAT)
			return false;
		final var line = body.getLineNo();
		// the end column is the line's own length rather than an open bound: the
		// intersection test folds (line, column) into one number keyed on
		// Integer.MAX_VALUE, so an open bound would reach column 0 of the next line
		// and match a comment that merely follows the statement
		return getFileContents().hasIntersectionWithComment(line, 0, line, getLine(line - 1).length());
	}

	private void visitDo(@Nonnull DetailAST ast) {
		final var body = ast.getFirstChild();
		if (body != null && AstQuery.isEmptyBody(body) && !isAnnotatedEmptyStatement(body))
			logWarning(ast, MSG_DO);
	}

	private void visitEmptyStat(@Nonnull DetailAST ast) {
		if (!isRequiredClauseBody(ast))
			log(ast, MSG_SEMI);
	}

	private void visitIf(@Nonnull DetailAST ast) {
		final var thenBody = ast.findFirstToken(TokenTypes.SLIST);
		if (thenBody != null && AstQuery.isEmptyBody(thenBody))
			log(ast, MSG_IF);
		else if (thenBody == null) {
			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.EMPTY_STAT) {
					if (!isAnnotatedEmptyStatement(child))
						log(ast, MSG_IF);
					break;
				}
			}
		}

		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst == null)
			return;

		final var elseBody = elseAst.getFirstChild();
		if (elseBody != null && AstQuery.isEmptyBody(elseBody) && !isAnnotatedEmptyStatement(elseBody))
			log(elseAst, MSG_ELSE);
	}

	private void visitInitializer(@Nonnull DetailAST ast, @Nonnull String msgKey) {
		final var slist = ast.findFirstToken(TokenTypes.SLIST);
		if (slist != null && AstQuery.isEmptyBody(slist))
			log(ast, msgKey);
	}

	private void visitLoop(@Nonnull DetailAST ast, @Nonnull String msgKey) {
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return;
		final var body = rparen.getNextSibling();
		if (body != null && AstQuery.isEmptyBody(body) && !isAnnotatedEmptyStatement(body))
			logWarning(ast, msgKey);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.EMPTY_STAT -> visitEmptyStat(ast);
			case TokenTypes.INSTANCE_INIT -> visitInitializer(ast, MSG_INSTANCE_INIT);
			case TokenTypes.LITERAL_DO -> visitDo(ast);
			case TokenTypes.LITERAL_FOR -> visitLoop(ast, MSG_FOR);
			case TokenTypes.LITERAL_IF -> visitIf(ast);
			case TokenTypes.LITERAL_WHILE -> visitLoop(ast, MSG_WHILE);
			case TokenTypes.STATIC_INIT -> visitInitializer(ast, MSG_STATIC_INIT);
		}
	}
}