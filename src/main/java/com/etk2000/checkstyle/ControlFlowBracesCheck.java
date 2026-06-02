package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
public class ControlFlowBracesCheck extends AbstractAstCheck {
	/**
	 * Where a control statement's body begins and ends, in the zero-based
	 * coordinates the fixer works in.
	 *
	 * @param block       whether the body is a braced block
	 * @param line        line the body starts on
	 * @param column      column the body starts at
	 * @param headerLine  line the keyword's header ends on (its {@code )}, or the
	 *                    keyword itself for {@code else})
	 * @param endLine     line the body stops on, or {@code -1} when nothing
	 *                    follows it there
	 * @param endColumn   where the body stops on that line: just past its
	 *                    terminating {@code ;}, or where the next construct
	 *                    starts when the body has no such token
	 * @param lastLine    last line the body statement occupies
	 */
	public record ControlBody(
			boolean block,
			int line,
			int column,
			int headerLine,
			int endLine,
			int endColumn,
			int lastLine
	) {}

	/**
	 * A {@code do-while}'s formatting tier and the position of the {@code while}
	 * that closes it, in the zero-based coordinates the fixer works in.
	 *
	 * @param tier             the body's formatting tier
	 * @param whileLine        line the closing {@code while} sits on
	 * @param whileColumn      column it starts at
	 * @param whileOnBodyLine  whether it shares the last line the body statement
	 *                         occupies, i.e. is cuddled onto the body rather than
	 *                         starting a line of its own
	 */
	public record DoWhileShape(int tier, int whileLine, int whileColumn, boolean whileOnBodyLine) {}

	/**
	 * Source span of a one-liner control-flow body, in the zero-based
	 * coordinates the fixer works in.
	 *
	 * @param column      where the body starts on the keyword line
	 * @param endLine     line the body stops on, or {@code -1} when nothing
	 *                    follows it there
	 * @param endColumn   where the body stops on that line: just past its
	 *                    terminating {@code ;}, or where the next construct
	 *                    starts when the body has no such token
	 * @param elseColumn  where the statement's {@code else} continues on the
	 *                    keyword line, or {@code -1} when it does not
	 * @param lastLine    last line the body statement occupies
	 */
	public record OneLinerBody(int column, int endLine, int endColumn, int elseColumn, int lastLine) {}

	/**
	 * Where a control-flow body stops, shared by the two body descriptors, which
	 * differ only in what else they carry.
	 *
	 * @param endLine    line the body stops on, or {@code -1} when nothing follows
	 *                   it there
	 * @param endColumn  where it stops on that line, or {@code -1} likewise
	 * @param lastLine   last line the body statement occupies
	 */
	private record BodyEnd(int endLine, int endColumn, int lastLine) {}

	static final int TIER_2 = 2;
	static final int TIER_3 = 3;

	private static final String MSG_DO_WHILE_BODY_ON_DO_LINE = "control.flow.do.while.body.on.do.line";
	private static final String MSG_DO_WHILE_WHILE_NEXT_LINE = "control.flow.do.while.while.next.line";
	private static final String MSG_MISSING_BRACES = "control.flow.missing.braces";
	private static final String MSG_ONE_LINER = "control.flow.one.liner";
	private static final String MSG_UNNECESSARY_BRACES = "control.flow.unnecessary.braces";

	/**
	 * Locates the non-do-while control keyword reported at {@code (line, column)}
	 * and returns where its body begins, or {@code null} when the position holds
	 * no such keyword or the body is an empty statement.
	 */
	@CheckReturnValue
	@Nullable
	public static ControlBody bodyAt(@Nonnull DetailAST root, int line, int column) {
		final var keyword = bodyKeywordAt(root, line, column);
		if (keyword == null)
			return null;
		final var body = getBody(keyword);
		final var closeParen = keyword.findFirstToken(TokenTypes.RPAREN);
		final var end = bodyEnd(keyword, body);
		return new ControlBody(
				body.getType() == TokenTypes.SLIST,
				AstUtil.firstLine(body) - 1,
				AstUtil.firstColumn(body),
				(closeParen == null ? keyword.getLineNo() : closeParen.getLineNo()) - 1,
				end.endLine(),
				end.endColumn(),
				end.lastLine()
		);
	}

	@CheckReturnValue
	@Nonnull
	private static BodyEnd bodyEnd(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		final var semi = terminatingSemi(body);
		final var lastLine = statementLastLine(body);
		final var trailer = semi == null ? trailingConstruct(keyword, elseOf(keyword), lastLine) : null;
		return new BodyEnd(endLineOf(semi, trailer, lastLine), endColumnOf(semi, trailer), lastLine - 1);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST bodyKeywordAt(@Nonnull DetailAST root, int line, int column) {
		final var keyword = AstUtil.findNodeAt(root, line, column, ControlFlowBracesCheck::isControlFlowKeyword);
		if (keyword == null)
			return null;
		final var body = getBody(keyword);
		return body == null || body.getType() == TokenTypes.EMPTY_STAT ? null : keyword;
	}

	/**
	 * Lines a braceless body occupies. Measured from the body's first token
	 * rather than its own position, since an imaginary node carries its
	 * operator's line ({@code EXPR} for {@code list\n.add(1)} sits on the
	 * {@code .} line, not on {@code list}'s).
	 */
	@CheckReturnValue
	private static int bodyLineCount(@Nonnull DetailAST body) {
		return statementLastLine(body) - AstUtil.firstLine(body) + 1;
	}

	@CheckReturnValue
	private static boolean containsBinaryOp(@Nonnull DetailAST node) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (isBinaryOpToken(current.getType()))
				return true;
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * Checks whether the subtree below {@code node} contains a METHOD_CALL,
	 * indicating method chaining (e.g. {@code a.b().c()}).
	 */
	@CheckReturnValue
	private static boolean containsMethodCall(@Nonnull DetailAST node) {
		final var stack = new ArrayDeque<DetailAST>();
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
			stack.push(child);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (current.getType() == TokenTypes.METHOD_CALL)
				return true;
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	public static int determineTier(@Nonnull DetailAST body) {
		if (!isDoWhileLineEligible(body))
			return TIER_3;
		return TIER_2;
	}

	/**
	 * The statement's own {@code else}, which continues it rather than following
	 * it. Only an {@code if} has one.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST elseOf(@Nonnull DetailAST keyword) {
		return keyword.getType() == TokenTypes.LITERAL_IF ? keyword.findFirstToken(TokenTypes.LITERAL_ELSE) : null;
	}

	@CheckReturnValue
	private static int endColumnOf(@Nullable DetailAST semi, @Nullable DetailAST trailer) {
		if (semi != null)
			return semi.getColumnNo() + 1;
		return trailer == null ? -1 : AstUtil.firstColumn(trailer);
	}

	@CheckReturnValue
	private static int endLineOf(@Nullable DetailAST semi, @Nullable DetailAST trailer, int lastLine) {
		if (semi != null)
			return semi.getLineNo() - 1;
		return trailer == null ? -1 : lastLine - 1;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getBody(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE -> ast.getFirstChild();
			// this check runs outside any fixer firewall, so a missing RPAREN must yield
			// no body rather than an NPE that fails the whole check task for the file
			case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF, TokenTypes.LITERAL_WHILE -> {
				final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
				yield rparen == null ? null : rparen.getNextSibling();
			}
			default -> throw new IllegalArgumentException("Unexpected token: " + ast);
		};
	}

	/**
	 * Returns whether the RHS of an assignment/compound-assignment is too
	 * complex for tier 2.
	 */
	@CheckReturnValue
	private static boolean hasComplexRhs(@Nonnull DetailAST expr) {
		return switch (expr.getType()) {
			case TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN,
			     TokenTypes.BSR_ASSIGN, TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN,
			     TokenTypes.MINUS_ASSIGN, TokenTypes.MOD_ASSIGN, TokenTypes.PLUS_ASSIGN,
			     TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN, TokenTypes.STAR_ASSIGN -> {
				final var rhs = expr.getLastChild();
				yield rhs != null && (containsBinaryOp(rhs) || isComplexRhsValue(rhs));
			}
			default -> false;
		};
	}

	/**
	 * Whether the block wraps a body that would read the same without its braces:
	 * one statement, occupying one line. The brace-to-brace distance covers the
	 * lines between them (comments included, which the AST cannot see), plus one
	 * for a statement sharing the {@code &#123;} line.
	 *
	 * <p>A statement sharing the {@code &#125;} line is deliberately not counted: it
	 * would make {@code &#123;\n\tstmt; &#125;} look unwrappable-in-place to the fixer.
	 */
	@CheckReturnValue
	private static boolean hasUnnecessaryBraces(@Nonnull DetailAST slist) {
		final var close = slist.findFirstToken(TokenTypes.RCURLY);
		final var first = slist.getFirstChild();
		// a `case` group's SLIST carries no RCURLY; without a close brace there is
		// nothing to unwrap, and the line arithmetic below would dereference null
		if (close == null)
			return false;
		// an empty block has no statement to unwrap, whatever comments fill the gap
		if (first == close)
			return false;

		if (isDeclaration(first))
			return false;

		var lines = close.getLineNo() - slist.getLineNo() - 1;
		if (close.getLineNo() > slist.getLineNo() && AstUtil.firstLine(first) == slist.getLineNo())
			++lines;
		return lines == 1 && statementCount(slist) == 1;
	}

	@CheckReturnValue
	private static boolean isBinaryOpToken(int type) {
		return switch (type) {
			case TokenTypes.BAND, TokenTypes.BOR, TokenTypes.BSR, TokenTypes.BXOR,
			     TokenTypes.DIV, TokenTypes.EQUAL, TokenTypes.GE, TokenTypes.GT,
			     TokenTypes.LAND, TokenTypes.LE, TokenTypes.LOR, TokenTypes.LT,
			     TokenTypes.MINUS, TokenTypes.MOD, TokenTypes.NOT_EQUAL, TokenTypes.PLUS,
			     TokenTypes.QUESTION, TokenTypes.SL, TokenTypes.SR, TokenTypes.STAR -> true;
			default -> false;
		};
	}

	/**
	 * Returns whether an assignment RHS is itself a chained method call or a
	 * {@code new} expression. Such a value is tier 3, mirroring how a bare
	 * chained call or {@code new} body is tier 3 (a single non-chained call
	 * RHS stays tier 2).
	 */
	@CheckReturnValue
	private static boolean isComplexRhsValue(@Nonnull DetailAST rhs) {
		if (rhs.getType() == TokenTypes.LITERAL_NEW)
			return true;
		if (rhs.getType() == TokenTypes.METHOD_CALL) {
			final var dot = rhs.findFirstToken(TokenTypes.DOT);
			return dot != null && containsMethodCall(dot);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isControlFlowKeyword(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF,
			     TokenTypes.LITERAL_WHILE -> true;
			default -> false;
		};
	}

	/**
	 * Whether the statement declares a local variable or a local type. Such a
	 * declaration is a block statement rather than a statement, so it can only
	 * appear inside a block: unwrapping its braces would not compile.
	 */
	@CheckReturnValue
	private static boolean isDeclaration(@Nonnull DetailAST statement) {
		return switch (statement.getType()) {
			case TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF,
			     TokenTypes.RECORD_DEF, TokenTypes.VARIABLE_DEF -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	static boolean isDoWhileLineEligible(@Nonnull DetailAST body) {
		if (!isSimpleExpression(body))
			return false;

		final var expr = body.getFirstChild();
		return !hasComplexRhs(expr);
	}

	@CheckReturnValue
	private static boolean isOneLiner(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		return body.getType() != TokenTypes.SLIST && AstUtil.firstLine(body) == keyword.getLineNo();
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

	/**
	 * Locates the non-do-while control keyword reported at {@code (line, column)}
	 * and returns the span of its one-liner body, or {@code null} when the
	 * position holds no such keyword or its body is not a one-liner.
	 */
	@CheckReturnValue
	@Nullable
	public static OneLinerBody oneLinerBodyAt(@Nonnull DetailAST root, int line, int column) {
		final var keyword = bodyKeywordAt(root, line, column);
		if (keyword == null)
			return null;

		final var body = getBody(keyword);
		if (!isOneLiner(keyword, body))
			return null;

		if (keyword.getType() == TokenTypes.LITERAL_ELSE && body.getType() == TokenTypes.LITERAL_IF)
			return null;

		final var elseKeyword = elseOf(keyword);
		final var end = bodyEnd(keyword, body);
		return new OneLinerBody(
				AstUtil.firstColumn(body),
				end.endLine(),
				end.endColumn(),
				elseKeyword != null && elseKeyword.getLineNo() == keyword.getLineNo()
						? elseKeyword.getColumnNo()
						: -1,
				end.lastLine()
		);
	}

	/**
	 * Locates the {@code do-while} keyword reported at {@code (line, column)}
	 * and returns its body's formatting tier together with the position of the
	 * {@code while} that closes it, or {@code null} when no such keyword is found.
	 * For a braced body the inner statement's tier is returned (collapsing the
	 * braces preserves it).
	 */
	@CheckReturnValue
	@Nullable
	public static DoWhileShape shapeAt(@Nonnull DetailAST root, int line, int column) {
		final var doNode = AstUtil.findNodeAt(root, line, column, n -> n.getType() == TokenTypes.LITERAL_DO);
		if (doNode == null)
			return null;
		final var body = doNode.getFirstChild();
		final var whileAst = doNode.findFirstToken(TokenTypes.DO_WHILE);
		if (body == null || whileAst == null)
			return null;
		final var tierBody = body.getType() == TokenTypes.SLIST ? body.getFirstChild() : body;
		return tierBody == null
				? null
				: new DoWhileShape(
						determineTier(tierBody),
						whileAst.getLineNo() - 1,
						whileAst.getColumnNo(),
						whileOnBodyLine(doNode, body)
				);
	}

	@CheckReturnValue
	private static int statementCount(@Nonnull DetailAST slist) {
		var count = 0;
		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			if (type != TokenTypes.RCURLY && type != TokenTypes.SEMI)
				++count;
		}
		return count;
	}

	/**
	 * Last line the body statement occupies. The terminating {@code ;} can sit on
	 * a later line than the body's own last token, and a span that stops short of
	 * it describes a statement that is not all there.
	 */
	@CheckReturnValue
	private static int statementLastLine(@Nonnull DetailAST body) {
		final var semi = terminatingSemi(body);
		return semi == null ? AstUtil.lastLine(body) : Math.max(AstUtil.lastLine(body), semi.getLineNo());
	}

	/**
	 * The body statement's terminating {@code ;}. It is a sibling of the body
	 * node rather than part of its subtree, so every span computed over a body
	 * has to fold it back in; a body that is itself a control statement has none.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST terminatingSemi(@Nonnull DetailAST body) {
		final var sibling = body.getNextSibling();
		return sibling != null && sibling.getType() == TokenTypes.SEMI ? sibling : null;
	}

	/**
	 * The construct that resumes after a body with no terminating {@code ;} (one
	 * that is itself a control statement), when it starts on the body's own last
	 * line: the statement's {@code else}, or the next statement in the enclosing
	 * block. Without it the body would be taken to own the rest of that line, and
	 * a sibling statement sharing it would be swallowed by the added braces.
	 *
	 * <p>An {@code else} keyword is a child of its {@code if} rather than a
	 * statement in the enclosing block, so what follows it is the {@code if}'s
	 * own sibling.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST trailingConstruct(
			@Nonnull DetailAST keyword,
			@Nullable DetailAST elseKeyword,
			int lastLine
	) {
		final var statement = keyword.getType() == TokenTypes.LITERAL_ELSE ? keyword.getParent() : keyword;
		final var trailer = elseKeyword != null ? elseKeyword : statement.getNextSibling();
		// the enclosing block's own `}` is not something that resumes after the body
		return trailer != null && trailer.getType() != TokenTypes.RCURLY && AstUtil.firstLine(trailer) == lastLine
				? trailer
				: null;
	}

	@CheckReturnValue
	private static boolean whileOnBodyLine(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		final var whileAst = keyword.findFirstToken(TokenTypes.DO_WHILE);
		return whileAst != null && whileAst.getLineNo() == statementLastLine(body);
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

		if (body.getType() == TokenTypes.SLIST) {
			if (hasUnnecessaryBraces(body))
				log(keyword, MSG_UNNECESSARY_BRACES);
		}
		else if (bodyLineCount(body) > 1)
			log(keyword, MSG_MISSING_BRACES);
	}

	private void checkDoWhile(@Nonnull DetailAST keyword, @Nonnull DetailAST body) {
		final var tier = determineTier(body);
		final var bodyOnDoLine = isOneLiner(keyword, body);
		final var whileOnBodyLine = whileOnBodyLine(keyword, body);

		if (body.getType() == TokenTypes.SLIST) {
			if (hasUnnecessaryBraces(body))
				log(keyword, MSG_UNNECESSARY_BRACES);
			return;
		}

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
				else if (whileOnBodyLine)
					log(keyword, MSG_DO_WHILE_WHILE_NEXT_LINE);
			}
			default -> { }
		}
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

		final var elseBody = getBody(elseKeyword);
		if (elseBody == null)
			return;

		if (elseBody.getType() == TokenTypes.LITERAL_IF)
			return;

		checkBody(elseKeyword, elseBody);
	}
}