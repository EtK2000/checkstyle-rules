package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.ArgLayoutClassifier;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces multiline call/signature formatting:
 * no arguments on the opening paren line, and no arguments on the closing paren line.
 * <p>
 * Exception ("ternary"): calls with exactly one argument that is a ternary expression. The
 * condition stays on the opening paren line, and the closing paren goes on its own line (not on
 * the same line as the last ternary branch).
 * <p>
 * Exception ("inline block"): calls with exactly one argument that is a lambda, anonymous class,
 * constructor call, or special processing method ({@code List.of}, {@code Map.of},
 * {@code Arrays.asList}, {@code Context.getString},
 * {@code Context.getResources().getQuantityString}) are exempt: the argument stays on the
 * opening paren line and its closing brace/paren on the closing paren line. For braceless
 * (expression) lambdas that extend past the opening line, the closing paren goes on its own line
 * instead. For constructor calls followed by method chaining (e.g. {@code new Foo().bar()}), the
 * constructor starts on the opening paren line and the closing paren goes on its own line (same
 * rule as braceless lambdas that extend past the opening line).
 * <p>
 * Exception ("method call arg"): calls with exactly one argument that is a plain method call.
 * The inner call stays on the opening paren line and closing parens are stacked on the same line.
 * This also applies when there are exactly two arguments and the first is {@code this} or an
 * Android resource identifier.
 * <p>
 * Both ternary and inline block exceptions also apply when there are exactly two arguments and
 * the first is {@code this} or an Android resource identifier ({@code R.xxx.yyy} or
 * {@code android.R.xxx.yyy}).
 * <p>
 * The inline block exception also applies to {@code Handler.postDelayed} with a braced lambda
 * as the first argument and the delay as the second.
 * <p>
 * The inline block exception also applies to {@code computeIfAbsent} with a braced lambda as the
 * second argument (the key stays on the opening paren line and the lambda's closing brace/paren on
 * the closing paren line). This is gated on the method name specifically, not on any two-argument
 * call with a lambda, because a {@code computeIfAbsent} key is short whereas an arbitrary method's
 * first argument may be long and should not be forced onto the opening line.
 * <p>
 * The inline block exception also applies to a two-argument {@code put} whose second argument is an
 * inline block (a chained constructor such as {@code new JSONObject().put(...)}, a lambda, or an
 * anonymous class): the key stays on the opening paren line and the value's closing brace/paren on
 * the closing paren line. Like {@code computeIfAbsent} this is gated on the method name
 * specifically, because a {@code put} key is short.
 * <p>
 * Separately, a {@code new JSONObject().put(k, v)} expression (a fresh {@code new JSONObject()} with
 * exactly one {@code .put}, not chained further) whose value is simple (a literal, variable, or
 * single non-chained method call, i.e. not a nested {@code new JSONObject()}, chained call, ternary,
 * or lambda) must be written on one line when its collapsed form fits within
 * {@link LineLength#MAX_LINE_LENGTH} columns; splitting the {@code new JSONObject()} from its
 * {@code .put} across lines is a violation. Multi-{@code put} builders and non-simple values stay
 * multiline.
 * <p>
 * {@code getString} is recognized with a known Context receiver: a variable assigned from
 * {@code requireContext()}/{@code getContext()}/{@code requireActivity()}/{@code getActivity()},
 * a parameter typed as {@code Context}, or calling directly on one of those methods.
 */
public class MultilineCallFormattingCheck extends AbstractAstCheck {
	record LayoutViolation(@Nonnull DetailAST node, @Nonnull String messageKey) {}

	static final String MSG_CLOSING = "multiline.args.on.closing.paren";
	static final String MSG_LAMBDA_NOT_ON_CLOSING = "multiline.lambda.not.on.closing.paren";
	static final String MSG_LAMBDA_NOT_ON_OPENING = "multiline.lambda.not.on.opening.paren";
	static final String MSG_OPENING = "multiline.args.on.opening.paren";
	private static final String MSG_POSTDELAYED_ONE_LINE = "multiline.postdelayed.one.line";
	private static final String MSG_PUT_COLLAPSIBLE = "multiline.put.collapsible";
	static final String MSG_SHARED_LINE = "multiline.args.shared.line";
	static final String MSG_TERNARY_COLON_LINE = "multiline.ternary.colon.wrong.line";
	static final String MSG_TERNARY_NOT_ON_CLOSING = "multiline.ternary.not.on.closing.paren";
	static final String MSG_TERNARY_NOT_ON_OPENING = "multiline.ternary.not.on.opening.paren";
	static final String MSG_TERNARY_QUESTION_LINE = "multiline.ternary.question.wrong.line";

	@CheckReturnValue
	static boolean containsChainedConstructor(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LITERAL_NEW && node.getParent() != null
					&& node.getParent().getType() == TokenTypes.DOT)
				return true;
			if (node.getType() == TokenTypes.ELIST || node.getType() == TokenTypes.OBJBLOCK
					|| node.getType() == TokenTypes.SLIST)
				continue;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	static DetailAST findFirstArg(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				yield params == null ? null : params.findFirstToken(TokenTypes.PARAMETER_DEF);
			}
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL -> {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				yield elist == null ? null : elist.getFirstChild();
			}
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	static DetailAST findLastArg(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				if (params == null)
					yield null;
				yield lastChildOfType(params, TokenTypes.PARAMETER_DEF);
			}
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL -> {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				if (elist == null)
					yield null;
				yield lastNonCommaChild(elist);
			}
			default -> null;
		};
	}

	@CheckReturnValue
	static boolean isDirectBracelessLambda(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		return node != null && node.getType() == TokenTypes.LAMBDA
				&& node.findFirstToken(TokenTypes.SLIST) == null;
	}

	@CheckReturnValue
	private static boolean isDirectMethodCall(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		return node != null && node.getType() == TokenTypes.METHOD_CALL;
	}

	@CheckReturnValue
	private static boolean isSingleMethodCallArg(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		DetailAST onlyArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (onlyArg != null)
					return false;
				onlyArg = child;
			}
		}
		return onlyArg != null && isDirectMethodCall(onlyArg);
	}

	@CheckReturnValue
	private static boolean isThisAndMethodCallArgs(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		DetailAST firstArg = null, secondArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (firstArg == null)
					firstArg = child;
				else if (secondArg == null)
					secondArg = child;
				else
					return false;
			}
		}
		return firstArg != null && secondArg != null
				&& ArgLayoutClassifier.isCompactFirstArg(firstArg) && isDirectMethodCall(secondArg);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST lastChildOfType(@Nonnull DetailAST parent, int type) {
		DetailAST last = null;
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == type)
				last = child;
		}
		return last;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST lastNonCommaChild(@Nonnull DetailAST parent) {
		DetailAST last = null;
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				last = child;
		}
		return last;
	}

	final ContextReceiverIndex contextReceivers = new ContextReceiverIndex();
	@Nullable
	private String[] cachedLines, primedLines;

	private void addSharedLineViolations(@Nonnull DetailAST ast, @Nonnull List<LayoutViolation> out) {
		final var firstArg = findFirstArg(ast);
		final var lastArg = findLastArg(ast);
		if (firstArg == null || lastArg == null || firstArg.getLineNo() == lastArg.getLineNo())
			return;

		final var argList = switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF ->
					ast.findFirstToken(TokenTypes.PARAMETERS);
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL ->
					ast.findFirstToken(TokenTypes.ELIST);
			default -> null;
		};
		if (argList == null)
			return;

		final var isParams = argList.getType() == TokenTypes.PARAMETERS;
		DetailAST prev = null;
		for (var child = argList.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (isParams) {
				if (child.getType() != TokenTypes.PARAMETER_DEF)
					continue;
			}
			else if (child.getType() == TokenTypes.COMMA)
				continue;

			if (prev != null && child.getLineNo() <= AstQuery.lastLine(prev))
				out.add(new LayoutViolation(child, MSG_SHARED_LINE));
			prev = child;
		}
	}

	/**
	 * The single layout-analysis pass shared by the check and the fixer. Returns every layout
	 * violation the given call/signature would emit.
	 */
	@CheckReturnValue
	@Nonnull
	List<LayoutViolation> analyzeLayout(@Nonnull DetailAST ast) {
		final var out = new ArrayList<LayoutViolation>();

		// for METHOD_CALL and SUPER_CTOR_CALL the token itself is the '(', no LPAREN child
		final int openLine;
		final DetailAST openToken;
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen != null) {
			openLine = lparen.getLineNo();
			openToken = lparen;
		}
		else {
			openLine = ast.getLineNo();
			openToken = ast;
		}

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return out;

		final var closeLine = rparen.getLineNo();

		// the `.put` is single-line, so this must run before the same-line early return below
		if (ast.getType() == TokenTypes.METHOD_CALL && JsonObjectPutCollapse.isViolation(ast, sourceLines())) {
			out.add(new LayoutViolation(openToken, MSG_PUT_COLLAPSIBLE));
			return out;
		}

		if (openLine == closeLine)
			return out;

		final var firstArg = findFirstArg(ast);
		if (firstArg == null)
			return out;

		if (ArgLayoutClassifier.isSingleTernaryArg(ast) || ArgLayoutClassifier.isThisAndTernaryArgs(ast)) {
			final var ternaryArg = ArgLayoutClassifier.isThisAndTernaryArgs(ast) ? findLastArg(ast) : firstArg;
			final var question = ternaryArg != null && ternaryArg.getType() == TokenTypes.EXPR
					? ternaryArg.getFirstChild() : ternaryArg;
			final var condition = question != null ? question.getFirstChild() : null;
			if (condition != null && condition.getLineNo() != openLine)
				out.add(new LayoutViolation(openToken, MSG_TERNARY_NOT_ON_OPENING));

			if (condition != null) {
				final var colon = question.findFirstToken(TokenTypes.COLON);
				if (question.getLineNo() != AstQuery.lastLine(condition)
						|| (colon != null && colon.getLineNo() != question.getLineNo())) {
					if (question.getLineNo() != AstQuery.lastLine(condition) + 1)
						out.add(new LayoutViolation(question, MSG_TERNARY_QUESTION_LINE));
					final var trueExpr = condition.getNextSibling();
					if (colon != null && trueExpr != null && colon.getLineNo() != AstQuery.lastLine(trueExpr) + 1)
						out.add(new LayoutViolation(colon, MSG_TERNARY_COLON_LINE));

					if (AstQuery.lastLine(ternaryArg) == closeLine)
						out.add(new LayoutViolation(rparen, MSG_CLOSING));
				}
				else if (AstQuery.lastLine(ternaryArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_TERNARY_NOT_ON_CLOSING));
			}
			return out;
		}

		if (ArgLayoutClassifier.isPostDelayedWithInlineBlock(ast)) {
			// we are past the openLine == closeLine early return, so a should-be-one-line call here is by
			// definition still multi-line and always flagged
			final var lambda = ArgLayoutClassifier.directBracedLambda(firstArg);
			final var slist = lambda != null ? lambda.findFirstToken(TokenTypes.SLIST) : null;
			if (slist != null && AstQuery.singleExpressionStatementBody(slist) != null
					&& !crossesMultilineLiteral(openLine, closeLine)
					&& LineCollapse.fitsOnOneLine(sourceLines(), openLine, closeLine))
				out.add(new LayoutViolation(openToken, MSG_POSTDELAYED_ONE_LINE));
			else {
				if (AstQuery.firstLine(firstArg) != openLine)
					out.add(new LayoutViolation(openToken, MSG_LAMBDA_NOT_ON_OPENING));
				if (AstQuery.lastLine(firstArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
			}
			return out;
		}

		if (ArgLayoutClassifier.isInlineBlockConfiguration(ast, contextReceivers::isContextSpecial)) {
			final var lastArg = findLastArg(ast);
			if (AstQuery.firstLine(firstArg) != openLine && inlineBlockOpeningPullUpFeasible(lastArg, openLine, closeLine))
				out.add(new LayoutViolation(openToken, MSG_LAMBDA_NOT_ON_OPENING));

			if (lastArg != null) {
				if ((isDirectBracelessLambda(lastArg) || containsChainedConstructor(lastArg)) && AstQuery.lastLine(lastArg) != openLine) {
					if (AstQuery.lastLine(lastArg) == closeLine)
						out.add(new LayoutViolation(rparen, MSG_CLOSING));
				}
				else if (AstQuery.lastLine(lastArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
			}
			return out;
		}

		if (isSingleMethodCallArg(ast) || isThisAndMethodCallArgs(ast)) {
			final var effectiveArg = isThisAndMethodCallArgs(ast) ? findLastArg(ast) : firstArg;
			if (effectiveArg != null && AstQuery.firstLine(effectiveArg) == openLine) {
				if (AstQuery.lastLine(effectiveArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
				return out;
			}
		}

		if (firstArg.getLineNo() == openLine)
			out.add(new LayoutViolation(openToken, MSG_OPENING));

		final var lastArg = findLastArg(ast);
		if (lastArg != null && AstQuery.lastLine(lastArg) == closeLine)
			out.add(new LayoutViolation(rparen, MSG_CLOSING));

		if (ast.getType() != TokenTypes.METHOD_CALL || !ArgLayoutClassifier.isStaticSpecialInlineMethodCall(ast))
			addSharedLineViolations(ast, out);

		return out;
	}

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		cachedLines = null;
		contextReceivers.clear();
		primedLines = null;
	}

	/**
	 * Whether {@code [openLine .. closeLine)} crosses a text block or multi-line block comment. The
	 * width gate refuses only text blocks, because the joiners it answers for join whole lines and so
	 * keep a block comment intact; {@code JavaPostDelayedReformatter} instead slices within lines at
	 * AST columns, where a block comment cannot survive, so that caller needs the wider refusal or it
	 * reports a violation its own reformatter will always decline.
	 */
	@CheckReturnValue
	private boolean crossesMultilineLiteral(int fromLine, int toLine) {
		final var lines = sourceLines();
		var state = JavaLineScanner.LexerState.NONE;
		for (var line = fromLine; line < toLine; ++line) {
			if (line < 1 || line > lines.length)
				continue;
			state = JavaLineScanner.stateAfter(lines[line - 1], state);
			if (state.inMultilineLiteral())
				return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.CTOR_DEF,
				TokenTypes.LITERAL_NEW,
				TokenTypes.METHOD_CALL,
				TokenTypes.METHOD_DEF,
				TokenTypes.SUPER_CTOR_CALL,
				TokenTypes.VARIABLE_DEF
		};
	}

	/**
	 * Whether the fixer could pull the first argument of an inline-block configuration onto the {@code (}
	 * line. A single-physical-line value ({@code lastArg} on one line) has only one clean shape, the whole
	 * call collapsed onto one line, so the pull-up is feasible only when that line fits within
	 * {@link LineLength#MAX_LINE_LENGTH}. A multi-line value keeps its tail, so the pull-up joins the
	 * {@code (} line through the value's first line: a {@code //} comment on any joined line but that last
	 * (kept) one would swallow the value, making the pull-up infeasible.
	 */
	@CheckReturnValue
	private boolean inlineBlockOpeningPullUpFeasible(@Nullable DetailAST lastArg, int openLine, int closeLine) {
		if (lastArg != null && AstQuery.firstLine(lastArg) == AstQuery.lastLine(lastArg))
			return LineCollapse.fitsOnOneLine(sourceLines(), openLine, closeLine, true);

		// scan the argument lines the pull-up would join (the key line(s) between the ( line and the value's
		// first line), not the ( line itself: joining the ( line keeps its code and appends the argument, so a
		// // comment there is a separate concern the fixer skips on, not a swallow this rule suppresses. The
		// lexer state is threaded from the ( line so a // inside a text block / block comment on a key line is
		// masked, not mistaken for a real trailing comment; a line that begins inside such a literal carries no
		// code-level comment and is skipped
		final var lines = sourceLines();
		final var headEnd = lastArg != null ? AstQuery.firstLine(lastArg) : closeLine;
		var state = JavaLineScanner.LexerState.NONE;
		for (var line = openLine; line < headEnd; ++line) {
			if (line < 1 || line > lines.length)
				continue;
			final var raw = lines[line - 1];
			final var beganInLiteral = state.inTextBlock() || state.inBlockComment();
			state = JavaLineScanner.stateAfter(raw, state);
			if (line == openLine || beganInLiteral)
				continue;
			if (JavaLineScanner.firstLineComment(raw, JavaLineScanner.LexerState.NONE) >= 0)
				return false;
		}
		return true;
	}

	/**
	 * Redirects the layout-feasibility gates to the source text the fixer parsed its AST from, so
	 * they read real lines when a static resolver runs {@link #analyzeLayout} on an otherwise
	 * uninitialized check instance. A real check run leaves this unset and reads
	 * {@link #getLines()}.
	 */
	void primeLines(@Nonnull List<String> lines) {
		primedLines = lines.toArray(String[]::new);
	}

	/**
	 * The file's lines, memoized because {@code getLines()} hands back a fresh copy of the whole file
	 * on every call and the width gates ask for it once per candidate call.
	 */
	@CheckReturnValue
	@Nonnull
	private String[] sourceLines() {
		if (primedLines != null)
			return primedLines;
		if (cachedLines == null)
			cachedLines = getLines();
		return cachedLines;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.VARIABLE_DEF) {
			contextReceivers.collectVariable(ast);
			return;
		}

		if (ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF)
			contextReceivers.collectParameters(ast);

		for (var violation : analyzeLayout(ast))
			log(violation.node(), violation.messageKey());
	}
}