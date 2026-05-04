package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces consistent brace usage for control flow statements.
 * <ul>
 *     <li>No one-liners: body must be on its own line</li>
 *     <li>No unnecessary braces: single-line body must not be wrapped in braces</li>
 *     <li>Missing braces: braceless body spanning multiple lines must have braces</li>
 * </ul>
 * Do-while has tier-based formatting:
 * <ul>
 *     <li>Tier 2 (simple body): body on do line, while on next</li>
 *     <li>Tier 3 (non-simple body): body on own line (standard rules)</li>
 * </ul>
 * Each nesting level is evaluated independently.
 */
public class ControlFlowBracesCheck extends AbstractCheck {
	static final int TIER_2 = 2;
	static final int TIER_3 = 3;

	private static final String MSG_DO_WHILE_BODY_ON_DO_LINE = "control.flow.do.while.body.on.do.line";
	private static final String MSG_DO_WHILE_WHILE_NEXT_LINE = "control.flow.do.while.while.next.line";
	private static final String MSG_MISSING_BRACES = "control.flow.missing.braces";
	private static final String MSG_ONE_LINER = "control.flow.one.liner";
	private static final String MSG_UNNECESSARY_BRACES = "control.flow.unnecessary.braces";

	@CheckReturnValue
	private static int bodyLineCount(@Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.SLIST)
			return bodyLineCountOfBlock(body);
		return AstUtil.lastLine(body) - body.getLineNo() + 1;
	}

	@CheckReturnValue
	private static int bodyLineCountOfBlock(@Nonnull DetailAST slist) {
		final var open = slist.getLineNo();
		final var close = slist.findFirstToken(TokenTypes.RCURLY).getLineNo();
		return close - open - 1;
	}

	/**
	 * Checks whether any node in the subtree is a binary operator
	 * (arithmetic, comparison, logical, bitwise, shift).
	 */
	@CheckReturnValue
	private static boolean containsBinaryOp(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BSR, TokenTypes.BXOR,
			     TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE, TokenTypes.GT,
			     TokenTypes.LAND, TokenTypes.LE, TokenTypes.LOR, TokenTypes.LT,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL, TokenTypes.PLUS,
			     TokenTypes.QUESTION, TokenTypes.SL, TokenTypes.SR, TokenTypes.STAR -> true;
			default -> {
				for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (containsBinaryOp(child))
						yield true;
				}
				yield false;
			}
		};
	}

	/**
	 * Checks whether the DOT subtree contains a METHOD_CALL node,
	 * indicating method chaining (e.g. {@code a.b().c()}).
	 */
	@CheckReturnValue
	private static boolean containsMethodCall(@Nonnull DetailAST node) {
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.METHOD_CALL)
				return true;
			if (containsMethodCall(child))
				return true;
		}
		return false;
	}

	/**
	 * Determines the formatting tier for a do-while body.
	 * <ul>
	 *     <li>Tier 2: body on do line, while on next (simple body)</li>
	 *     <li>Tier 3: body on own line (non-simple body)</li>
	 * </ul>
	 */
	@CheckReturnValue
	static int determineTier(@Nonnull DetailAST body) {
		if (!isDoWhileLineEligible(body))
			return TIER_3;
		return TIER_2;
	}

	@CheckReturnValue
	private static DetailAST getBody(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.LITERAL_DO -> ast.getFirstChild();
			case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE ->
					ast.findFirstToken(TokenTypes.RPAREN).getNextSibling();
			default -> throw new IllegalArgumentException("Unexpected token: " + ast);
		};
	}

	/**
	 * Returns whether the RHS of an assignment/compound-assignment contains
	 * a binary operator, making the expression too complex for tier 2.
	 */
	@CheckReturnValue
	private static boolean hasComplexRhs(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			     TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN,
			     TokenTypes.MINUS_ASSIGN, TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN,
			     TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN, TokenTypes.STAR_ASSIGN -> {
				final var rhs = expr.getLastChild();
				yield rhs != null && containsBinaryOp(rhs);
			}
			default -> false;
		};
	}

	/**
	 * Returns whether the body qualifies to be on the {@code do} line
	 * (tier 2). The body must be a simple expression without chained
	 * method calls or complex RHS.
	 */
	@CheckReturnValue
	static boolean isDoWhileLineEligible(@Nonnull DetailAST body) {
		if (!isSimpleExpression(body))
			return false;

		final var expr = body.getFirstChild();

		// assignments with complex RHS are tier 3
		return !hasComplexRhs(expr);
	}

	@CheckReturnValue
	private static boolean isOneLiner(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.SLIST)
			return false;
		return body.getLineNo() == keyword.getLineNo();
	}

	/**
	 * Returns whether the body is a simple expression: increment/decrement,
	 * assignment, compound assignment, or a single non-chained method call.
	 */
	@CheckReturnValue
	static boolean isSimpleExpression(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.EXPR)
			return false;

		final var expr = body.getFirstChild();
		return switch (expr.getType()) {
			case TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			     TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN, TokenTypes.DEC,
			     TokenTypes.DIV_ASSIGN, TokenTypes.INC, TokenTypes.MINUS_ASSIGN,
			     TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN, TokenTypes.POST_DEC,
			     TokenTypes.POST_INC, TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN,
			     TokenTypes.STAR_ASSIGN -> true;
			case TokenTypes.METHOD_CALL -> {
				final var dot = expr.findFirstToken(TokenTypes.DOT);
				yield dot == null || !containsMethodCall(dot);
			}
			default -> false;
		};
	}

	private void checkBody(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		if (body.getType() == TokenTypes.EMPTY_STAT)
			return;

		if (keyword.getType() == TokenTypes.LITERAL_DO) {
			checkDoWhile(keyword, body);
			return;
		}

		if (isOneLiner(keyword, body)) {
			log(keyword, MSG_ONE_LINER);
			return;
		}

		final var lines = bodyLineCount(body);

		if (body.getType() == TokenTypes.SLIST) {
			if (lines == 1)
				log(keyword, MSG_UNNECESSARY_BRACES);
		}
		else if (lines > 1)
			log(keyword, MSG_MISSING_BRACES);
	}

	private void checkDoWhile(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		final var tier = determineTier(body);
		final var bodyOnDoLine = isOneLiner(keyword, body);
		final var whileAst = keyword.findFirstToken(TokenTypes.DO_WHILE);
		final var bodyLastLine = AstUtil.lastLine(body);
		final var whileOnBodyLine = whileAst != null && whileAst.getLineNo() == bodyLastLine;

		// braced body — always unnecessary for single-line do-while bodies
		if (body.getType() == TokenTypes.SLIST) {
			if (bodyLineCountOfBlock(body) == 1)
				log(keyword, MSG_UNNECESSARY_BRACES);
			return;
		}

		// multi-line braceless body — always needs braces regardless of tier
		if (bodyLineCount(body) > 1) {
			log(keyword, MSG_MISSING_BRACES);
			return;
		}

		switch (tier) {
			case TIER_2 -> {
				if (!bodyOnDoLine)
					log(keyword, MSG_DO_WHILE_BODY_ON_DO_LINE);
				else if (whileOnBodyLine)
					log(keyword, MSG_DO_WHILE_WHILE_NEXT_LINE);
			}
			case TIER_3 -> {
				if (bodyOnDoLine)
					log(keyword, MSG_ONE_LINER);
			}
			default -> { }
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
				TokenTypes.LITERAL_DO,
				TokenTypes.LITERAL_FOR,
				TokenTypes.LITERAL_IF,
				TokenTypes.LITERAL_WHILE
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var body = getBody(ast);
		if (body == null)
			return;

		checkBody(ast, body);

		if (ast.getType() != TokenTypes.LITERAL_IF)
			return;

		final var elseKeyword = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseKeyword == null)
			return;

		final var elseBody = elseKeyword.getFirstChild();
		if (elseBody == null)
			return;

		if (elseBody.getType() == TokenTypes.LITERAL_IF)
			return;

		checkBody(elseKeyword, elseBody);
	}
}