package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces multiline call/signature formatting:
 * no arguments on the opening paren line, and no arguments on the closing paren line.
 * <p>
 * Exception ("ternary"): calls with exactly one argument that is a ternary expression — the
 * condition stays on the opening paren line, and the closing paren goes on its own line (not on
 * the same line as the last ternary branch).
 * <p>
 * Exception ("inline block"): calls with exactly one argument that is a lambda, anonymous class,
 * constructor call, or special processing method ({@code List.of}, {@code Map.of},
 * {@code Arrays.asList}, {@code Context.getString},
 * {@code Context.getResources().getQuantityString}) are exempt — the argument stays on the
 * opening paren line and its closing brace/paren on the closing paren line. For braceless
 * (expression) lambdas that extend past the opening line, the closing paren goes on its own line
 * instead. For constructor calls followed by method chaining (e.g. {@code new Foo().bar()}), the
 * constructor starts on the opening paren line and the closing paren goes on its own line (same
 * rule as braceless lambdas that extend past the opening line).
 * <p>
 * Both ternary and inline block exceptions also apply when there are exactly two arguments and
 * the first is {@code this} or an Android resource identifier ({@code R.xxx.yyy} or
 * {@code android.R.xxx.yyy}).
 * <p>
 * The inline block exception also applies to {@code Handler.postDelayed} with a braced lambda
 * as the first argument and the delay as the second.
 * <p>
 * {@code getString} is recognized with a known Context receiver: a variable assigned from
 * {@code requireContext()}/{@code getContext()}/{@code requireActivity()}/{@code getActivity()},
 * a parameter typed as {@code Context}, or calling directly on one of those methods.
 */
public class MultilineCallFormattingCheck extends AbstractCheck {
	// method names that qualify as inline block args; value = allowed receiver class names (empty = any receiver)
	private static final Map<String, Set<String>> SPECIAL_INLINE_METHODS = Map.of(
			"asList", Set.of("Arrays"),
			"of", Set.of("List", "Map")
	);

	// methods whose return value is always a Context (used for getString tracking)
	private static final Set<String> CONTEXT_RETURNING_METHODS = Set.of(
			"getActivity", "getContext", "requireActivity", "requireContext"
	);

	private static final String MSG_CLOSING = "multiline.args.on.closing.paren";
	private static final String MSG_LAMBDA_NOT_ON_CLOSING = "multiline.lambda.not.on.closing.paren";
	private static final String MSG_LAMBDA_NOT_ON_OPENING = "multiline.lambda.not.on.opening.paren";
	private static final String MSG_OPENING = "multiline.args.on.opening.paren";
	private static final String MSG_SHARED_LINE = "multiline.args.shared.line";
	private static final String MSG_TERNARY_COLON_LINE = "multiline.ternary.colon.wrong.line";
	private static final String MSG_TERNARY_NOT_ON_CLOSING = "multiline.ternary.not.on.closing.paren";
	private static final String MSG_TERNARY_NOT_ON_OPENING = "multiline.ternary.not.on.opening.paren";
	private static final String MSG_TERNARY_QUESTION_LINE = "multiline.ternary.question.wrong.line";

	@CheckReturnValue
	private static boolean containsBracedLambda(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LAMBDA)
			return ast.findFirstToken(TokenTypes.SLIST) != null;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (containsBracedLambda(child))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsChainedConstructor(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_NEW && ast.getParent() != null
				&& ast.getParent().getType() == TokenTypes.DOT)
			return true;
		// don't recurse into code blocks (lambda bodies, anonymous class bodies)
		if (ast.getType() == TokenTypes.OBJBLOCK || ast.getType() == TokenTypes.SLIST)
			return false;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (containsChainedConstructor(child))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findFirstArg(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.METHOD_CALL, TokenTypes.LITERAL_NEW, TokenTypes.SUPER_CTOR_CALL -> {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				yield elist == null ? null : elist.getFirstChild();
			}
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				yield params == null ? null : params.findFirstToken(TokenTypes.PARAMETER_DEF);
			}
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findLastArg(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.METHOD_CALL, TokenTypes.LITERAL_NEW, TokenTypes.SUPER_CTOR_CALL: {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				if (elist == null)
					return null;
				return lastNonCommaChild(elist);
			}

			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF: {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				if (params == null)
					return null;
				return lastChildOfType(params, TokenTypes.PARAMETER_DEF);
			}

			default:
				return null;
		}
	}

	@CheckReturnValue
	private static int firstLine(@Nonnull DetailAST ast) {
		var first = ast.getLineNo();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childFirst = firstLine(child);
			if (childFirst < first)
				first = childFirst;
		}
		return first;
	}

	@CheckReturnValue
	private static boolean isAndroidResourceId(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (node == null || node.getType() != TokenTypes.DOT)
			return false;

		// walk DOT chain to find leftmost identifier
		var leftmost = node;
		while (leftmost.getType() == TokenTypes.DOT && leftmost.getFirstChild() != null)
			leftmost = leftmost.getFirstChild();

		if (leftmost.getType() != TokenTypes.IDENT)
			return false;

		// R.xxx.yyy
		if ("R".equals(leftmost.getText()))
			return true;

		// android.R.xxx.yyy
		if ("android".equals(leftmost.getText())) {
			final var next = leftmost.getNextSibling();
			return next != null && next.getType() == TokenTypes.IDENT && "R".equals(next.getText());
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isCompactFirstArg(@Nonnull DetailAST ast) {
		return isAndroidResourceId(ast) || isLiteralThis(ast);
	}

	@CheckReturnValue
	private static boolean isContextType(@Nonnull DetailAST type) {
		// simple: Context
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null && "Context".equals(ident.getText()))
			return true;

		// fully qualified: android.content.Context (DOT tree, last IDENT is "Context")
		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			for (var child = dot.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.IDENT && "Context".equals(child.getText()) && child.getNextSibling() == null)
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isDirectBracelessLambda(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		return node != null && node.getType() == TokenTypes.LAMBDA
				&& node.findFirstToken(TokenTypes.SLIST) == null;
	}

	@CheckReturnValue
	private static boolean isLiteralThis(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LITERAL_THIS)
			return true;
		if (ast.getType() == TokenTypes.EXPR)
			return ast.getFirstChild() != null && ast.getFirstChild().getType() == TokenTypes.LITERAL_THIS;
		return false;
	}

	@CheckReturnValue
	private static boolean isMethodCallNamed(@Nonnull DetailAST methodCall, @Nonnull String name) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return false;

		// bare call: getString(...)
		if (firstChild.getType() == TokenTypes.IDENT)
			return name.equals(firstChild.getText());

		// dotted call: receiver.getString(...)
		if (firstChild.getType() == TokenTypes.DOT) {
			final var methodName = firstChild.getFirstChild() == null ? null : firstChild.getFirstChild().getNextSibling();
			return methodName != null && name.equals(methodName.getText());
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isPostDelayedWithBracedLambda(@Nonnull DetailAST ast) {
		if (ast.getType() != TokenTypes.METHOD_CALL || !isMethodCallNamed(ast, "postDelayed"))
			return false;
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
		return firstArg != null && secondArg != null && containsBracedLambda(firstArg);
	}

	@CheckReturnValue
	private static boolean isSingleTernaryArg(@Nonnull DetailAST ast) {
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
		return onlyArg != null && isTernary(onlyArg);
	}

	@CheckReturnValue
	private static boolean isStaticSpecialInlineMethodCall(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return false;

		// bare call: asList(...)
		if (firstChild.getType() == TokenTypes.IDENT) {
			final var entry = SPECIAL_INLINE_METHODS.get(firstChild.getText());
			return entry != null && entry.isEmpty();
		}

		// dotted call: Arrays.asList(...), List.of(...)
		if (firstChild.getType() == TokenTypes.DOT) {
			final var receiver = firstChild.getFirstChild();
			final var methodName = receiver == null ? null : receiver.getNextSibling();
			if (receiver == null || methodName == null)
				return false;

			final var entry = SPECIAL_INLINE_METHODS.get(methodName.getText());
			if (entry != null && receiver.getType() == TokenTypes.IDENT && entry.contains(receiver.getText()))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isTernary(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.QUESTION)
			return true;
		if (ast.getType() == TokenTypes.EXPR)
			return ast.getFirstChild() != null && ast.getFirstChild().getType() == TokenTypes.QUESTION;
		return false;
	}

	@CheckReturnValue
	private static boolean isThisAndTernaryArgs(@Nonnull DetailAST ast) {
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
				&& isCompactFirstArg(firstArg) && isTernary(secondArg);
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

	private final Set<String> contextVarNames = new HashSet<>();

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		contextVarNames.clear();
	}

	private void checkArgsOnSeparateLines(@Nonnull DetailAST ast) {
		final var firstArg = findFirstArg(ast);
		final var lastArg = findLastArg(ast);
		if (firstArg == null || lastArg == null || firstArg.getLineNo() == lastArg.getLineNo())
			return;

		// args span multiple lines — each must be on its own line
		final var argList = switch (ast.getType()) {
			case TokenTypes.METHOD_CALL, TokenTypes.LITERAL_NEW, TokenTypes.SUPER_CTOR_CALL ->
					ast.findFirstToken(TokenTypes.ELIST);
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF ->
					ast.findFirstToken(TokenTypes.PARAMETERS);
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

			if (prev != null && child.getLineNo() <= AstUtil.lastLine(prev))
				log(child, MSG_SHARED_LINE);
			prev = child;
		}
	}

	@CheckReturnValue
	private boolean containsInlineBlockArg(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.LAMBDA || ast.getType() == TokenTypes.LITERAL_NEW)
			return true;
		if (ast.getType() == TokenTypes.METHOD_CALL && isSpecialInlineMethodCall(ast))
			return true;
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (containsInlineBlockArg(child))
				return true;
		}
		return false;
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
				TokenTypes.CTOR_DEF,
				TokenTypes.LITERAL_NEW,
				TokenTypes.METHOD_CALL,
				TokenTypes.METHOD_DEF,
				TokenTypes.SUPER_CTOR_CALL,
				TokenTypes.VARIABLE_DEF
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@CheckReturnValue
	private boolean hasKnownContextReceiver(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null)
			return false;

		// receiver is a tracked context variable
		if (receiver.getType() == TokenTypes.IDENT && contextVarNames.contains(receiver.getText()))
			return true;

		// receiver is a direct call to a context-returning method: requireContext().XXX(...)
		if (receiver.getType() == TokenTypes.METHOD_CALL) {
			final var callName = receiver.getFirstChild();
			if (callName != null && callName.getType() == TokenTypes.IDENT
					&& CONTEXT_RETURNING_METHODS.contains(callName.getText()))
				return true;

			// dotted context call: something.getContext().XXX(...)
			if (callName != null && callName.getType() == TokenTypes.DOT) {
				final var innerMethod = callName.getFirstChild() == null ? null : callName.getFirstChild().getNextSibling();
				if (innerMethod != null && CONTEXT_RETURNING_METHODS.contains(innerMethod.getText()))
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	private boolean isGetQuantityStringCall(@Nonnull DetailAST methodCall) {
		if (!isMethodCallNamed(methodCall, "getQuantityString"))
			return false;

		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		// receiver must be getResources()
		final var receiver = firstChild.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL
				|| !isMethodCallNamed(receiver, "getResources"))
			return false;

		// getResources()'s receiver must be a known Context
		return hasKnownContextReceiver(receiver);
	}

	@CheckReturnValue
	private boolean isGetStringCall(@Nonnull DetailAST methodCall) {
		if (!isMethodCallNamed(methodCall, "getString"))
			return false;

		final var firstChild = methodCall.getFirstChild();

		// bare getString(...) — can't know if this is a Context
		if (firstChild.getType() == TokenTypes.IDENT)
			return false;

		// dotted: receiver.getString(...)
		return hasKnownContextReceiver(methodCall);
	}

	@CheckReturnValue
	private boolean isSingleInlineBlockArg(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		// count non-comma children — must be exactly one
		DetailAST onlyArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (onlyArg != null)
					return false;
				onlyArg = child;
			}
		}
		return onlyArg != null && containsInlineBlockArg(onlyArg);
	}

	@CheckReturnValue
	private boolean isSpecialInlineMethodCall(@Nonnull DetailAST methodCall) {
		return isStaticSpecialInlineMethodCall(methodCall) || isGetQuantityStringCall(methodCall) || isGetStringCall(methodCall);
	}

	@CheckReturnValue
	private boolean isThisAndInlineBlockArgs(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		// collect non-comma children — must be exactly two
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
				&& isCompactFirstArg(firstArg) && containsInlineBlockArg(secondArg);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.VARIABLE_DEF) {
			visitVariableDef(ast);
			return;
		}

		// scan parameters for Context-typed ones (before the multiline check)
		if (ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF) {
			final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
			if (params != null) {
				for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
					if (param.getType() != TokenTypes.PARAMETER_DEF)
						continue;
					final var type = param.findFirstToken(TokenTypes.TYPE);
					if (type != null && isContextType(type)) {
						final var ident = param.findFirstToken(TokenTypes.IDENT);
						if (ident != null)
							contextVarNames.add(ident.getText());
					}
				}
			}
		}

		// for METHOD_CALL and SUPER_CTOR_CALL the token itself is the '(' — no LPAREN child
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
			return;

		final var closeLine = rparen.getLineNo();

		// single-line call — skip
		if (openLine == closeLine)
			return;

		final var firstArg = findFirstArg(ast);
		if (firstArg == null)
			return;

		// ternary arg: condition on opening paren line, closing paren on its own line
		if (isSingleTernaryArg(ast) || isThisAndTernaryArgs(ast)) {
			final var ternaryArg = isThisAndTernaryArgs(ast) ? findLastArg(ast) : firstArg;
			final var question = ternaryArg != null && ternaryArg.getType() == TokenTypes.EXPR
					? ternaryArg.getFirstChild() : ternaryArg;
			final var condition = question != null ? question.getFirstChild() : null;
			if (condition != null && condition.getLineNo() != openLine)
				log(openToken, MSG_TERNARY_NOT_ON_OPENING);

			// check ? and : positioning when ternary body spans multiple lines
			if (condition != null) {
				final var colon = question.findFirstToken(TokenTypes.COLON);
				if (question.getLineNo() != AstUtil.lastLine(condition)
						|| (colon != null && colon.getLineNo() != question.getLineNo())) {
					if (question.getLineNo() != AstUtil.lastLine(condition) + 1)
						log(question, MSG_TERNARY_QUESTION_LINE);
					final var trueExpr = condition.getNextSibling();
					if (colon != null && trueExpr != null && colon.getLineNo() != AstUtil.lastLine(trueExpr) + 1)
						log(colon, MSG_TERNARY_COLON_LINE);

					// multiline ternary: closing paren on its own line
					if (ternaryArg != null && AstUtil.lastLine(ternaryArg) == closeLine)
						log(rparen, MSG_CLOSING);
				}
				else if (ternaryArg != null && AstUtil.lastLine(ternaryArg) != closeLine) {
					// single-line ternary: closing paren on the same line
					log(rparen, MSG_TERNARY_NOT_ON_CLOSING);
				}
			}
			return;
		}

		// inline block arg: enforce the OPPOSITE — must be on paren lines
		if (isSingleInlineBlockArg(ast) || isThisAndInlineBlockArgs(ast) || isPostDelayedWithBracedLambda(ast)) {
			if (firstLine(firstArg) != openLine)
				log(openToken, MSG_LAMBDA_NOT_ON_OPENING);

			final var lastArg = findLastArg(ast);
			if (lastArg != null) {
				if ((isDirectBracelessLambda(lastArg) || containsChainedConstructor(lastArg)) && AstUtil.lastLine(lastArg) != openLine) {
					// braceless lambda spanning past opening line: closing paren on its own line
					if (AstUtil.lastLine(lastArg) == closeLine)
						log(rparen, MSG_CLOSING);
				}
				else if (AstUtil.lastLine(lastArg) != closeLine)
					log(rparen, MSG_LAMBDA_NOT_ON_CLOSING);
			}
			return;
		}

		if (firstArg.getLineNo() == openLine)
			log(openToken, MSG_OPENING);

		final var lastArg = findLastArg(ast);
		if (lastArg != null && AstUtil.lastLine(lastArg) == closeLine)
			log(rparen, MSG_CLOSING);

		if (ast.getType() != TokenTypes.METHOD_CALL || !isStaticSpecialInlineMethodCall(ast))
			checkArgsOnSeparateLines(ast);
	}

	private void visitVariableDef(@Nonnull DetailAST varDef) {
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return;

		final var expr = assign.getFirstChild();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return;

		final var methodCall = expr.getFirstChild();
		if (methodCall == null || methodCall.getType() != TokenTypes.METHOD_CALL)
			return;

		final var callFirst = methodCall.getFirstChild();
		if (callFirst == null)
			return;

		String methodName = null;
		if (callFirst.getType() == TokenTypes.IDENT)
			methodName = callFirst.getText();
		else if (callFirst.getType() == TokenTypes.DOT) {
			final var dotMethod = callFirst.getFirstChild() == null ? null : callFirst.getFirstChild().getNextSibling();
			if (dotMethod != null)
				methodName = dotMethod.getText();
		}

		if (methodName != null && CONTEXT_RETURNING_METHODS.contains(methodName)) {
			final var ident = varDef.findFirstToken(TokenTypes.IDENT);
			if (ident != null)
				contextVarNames.add(ident.getText());
		}
	}
}