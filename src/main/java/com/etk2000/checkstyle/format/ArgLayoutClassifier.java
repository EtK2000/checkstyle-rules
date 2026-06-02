package com.etk2000.checkstyle.format;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Classifies a call/definition argument by the layout rule it carries, shared by the multiline-call
 * check and the reformatting helpers (and reusable by any future formatting check/fixer). Holds the
 * single source of truth for the static special-inline method set ({@code List.of} etc.) and the
 * call-level "inline-block configuration" recognition. The context-dependent
 * {@code getString}/{@code getQuantityString} recognition stays in the check (which primes the
 * {@code Context} receiver set) and is threaded in via a {@code contextSpecial} predicate.
 */
public final class ArgLayoutClassifier {
	private static final Map<String, Set<String>> SPECIAL_INLINE_METHODS = Map.of(
			"asList", Set.of("Arrays"),
			"copyOf", Set.of("List", "Map", "Set"),
			"of", Set.of("List", "Map", "Set")
	);

	@CheckReturnValue
	public static boolean containsBracedLambda(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LAMBDA) {
				if (node.findFirstToken(TokenTypes.SLIST) != null)
					return true;
				// a lambda's own body is never our concern, braced or not
				continue;
			}
			// don't recurse into nested method call args; a lambda inside another call is not our concern
			if (node.getType() == TokenTypes.ELIST)
				continue;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * Whether {@code ast} carries an inline-block argument shape: a lambda or {@code new} expression;
	 * a static special-inline call ({@code List.of} etc.) when {@code matchStaticSpecial}; or a
	 * context-special call the {@code contextSpecial} predicate recognizes ({@code getString}/
	 * {@code getQuantityString}). The walk stops at a nested {@code ELIST} because a
	 * lambda/constructor/special call inside another call's arguments is that inner call's concern.
	 */
	@CheckReturnValue
	public static boolean containsInlineBlockArg(@Nonnull DetailAST ast, boolean matchStaticSpecial, @Nonnull Predicate<DetailAST> contextSpecial) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LAMBDA || node.getType() == TokenTypes.LITERAL_NEW)
				return true;
			if (node.getType() == TokenTypes.METHOD_CALL
					&& ((matchStaticSpecial && isStaticSpecialInlineMethodCall(node)) || contextSpecial.test(node)))
				return true;
			if (node.getType() == TokenTypes.ELIST)
				continue;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * Whether {@code arg}'s subtree contains a brace-delimited block whose braces span more than one
	 * source line: a lambda/statement block ({@code SLIST}), an anonymous-class body ({@code OBJBLOCK}),
	 * or a switch body ({@code LITERAL_SWITCH}). Such an argument is re-emitted verbatim by the
	 * reformatters rather than tight-collapsed, since joining a multi-line braced body onto one line
	 * produces a crammed (and usually over-long) result; a block already written on one line collapses
	 * as normal. Unlike {@link #containsBracedLambda}, this descends into nested calls, because a
	 * multi-line block anywhere in the argument (even inside a nested call) must be preserved. The
	 * one-line span test also skips a braceless case-group {@code SLIST} (no {@code RCURLY} child).
	 */
	@CheckReturnValue
	public static boolean containsMultilineBracedBlock(@Nonnull DetailAST arg) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(arg);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var type = node.getType();
			if (type == TokenTypes.SLIST || type == TokenTypes.OBJBLOCK || type == TokenTypes.LITERAL_SWITCH) {
				final var rcurly = node.findFirstToken(TokenTypes.RCURLY);
				if (rcurly != null && rcurly.getLineNo() != node.getLineNo())
					return true;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * The braced ({@code SLIST}-bodied) lambda that is {@code arg} directly (or its immediate child, when
	 * {@code arg} is an {@code EXPR} wrapper), or {@code null}. A lambda buried deeper (inside a ternary,
	 * cast, or nested call) is not returned, so a caller that reshapes {@code arg} as a lambda argument
	 * never acts on a shape it cannot navigate.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST directBracedLambda(@Nonnull DetailAST arg) {
		final var node = arg.getType() == TokenTypes.LAMBDA ? arg : arg.findFirstToken(TokenTypes.LAMBDA);
		return node != null && node.getType() == TokenTypes.LAMBDA && node.findFirstToken(TokenTypes.SLIST) != null
				? node : null;
	}

	/**
	 * The direct braced inline block that is {@code arg} (or its immediate child when {@code arg} is an
	 * {@code EXPR} wrapper): a braced ({@code SLIST}-bodied) lambda, or an anonymous class
	 * ({@code LITERAL_NEW} with an {@code OBJBLOCK} body). Returns the {@code LAMBDA}/{@code LITERAL_NEW}
	 * node, or {@code null} for anything else (braceless lambda, non-anonymous {@code new}, or a block
	 * buried deeper). Callers reshape this as an inline-block argument, so a shape they cannot navigate
	 * is never returned.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST directInlineBlock(@Nonnull DetailAST arg) {
		final var lambda = directBracedLambda(arg);
		if (lambda != null)
			return lambda;
		final var node = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		return node != null && node.getType() == TokenTypes.LITERAL_NEW && node.findFirstToken(TokenTypes.OBJBLOCK) != null
				? node : null;
	}

	@CheckReturnValue
	private static boolean isAndroidResourceId(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (node == null || node.getType() != TokenTypes.DOT)
			return false;

		var leftmost = node;
		while (leftmost.getType() == TokenTypes.DOT && leftmost.getFirstChild() != null)
			leftmost = leftmost.getFirstChild();

		if (leftmost.getType() != TokenTypes.IDENT)
			return false;

		if ("R".equals(leftmost.getText()))
			return true;

		if ("android".equals(leftmost.getText())) {
			final var next = leftmost.getNextSibling();
			return next != null && next.getType() == TokenTypes.IDENT && "R".equals(next.getText());
		}
		return false;
	}

	/**
	 * Whether {@code ast} is a "compact" first argument that a two-argument inline-block/ternary/
	 * method-call configuration may keep on the opening line: {@code this} or an Android resource id
	 * ({@code R.xxx.yyy} / {@code android.R.xxx.yyy}).
	 */
	@CheckReturnValue
	public static boolean isCompactFirstArg(@Nonnull DetailAST ast) {
		return isAndroidResourceId(ast) || isLiteralThis(ast);
	}

	@CheckReturnValue
	public static boolean isComputeIfAbsentWithBracedLambda(@Nonnull DetailAST ast) {
		if (ast.getType() != TokenTypes.METHOD_CALL || !isMethodCallNamed(ast, "computeIfAbsent"))
			return false;
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		return args.size() == 2 && containsBracedLambda(args.get(1));
	}

	/**
	 * Whether {@code owner} is a call whose whole argument list is an "inline-block configuration" that
	 * must keep its opening-line shape: a single inline-block arg, a {@code this}/resource-id first arg
	 * followed by an inline block, a {@code postDelayed} braced-lambda-first call, a
	 * {@code computeIfAbsent} braced-lambda-second call, or a {@code put} whose value is an inline
	 * block. Mirrors the multiline-call check's inline-block classification condition.
	 */
	@CheckReturnValue
	public static boolean isInlineBlockConfiguration(@Nonnull DetailAST owner, @Nonnull Predicate<DetailAST> contextSpecial) {
		return isSingleInlineBlockArg(owner, contextSpecial) || isThisAndInlineBlockArgs(owner, contextSpecial)
				|| isPostDelayedWithInlineBlock(owner) || isComputeIfAbsentWithBracedLambda(owner)
				|| isPutWithInlineBlockValue(owner, contextSpecial);
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
	public static boolean isMethodCallNamed(@Nonnull DetailAST methodCall, @Nonnull String name) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return false;

		if (firstChild.getType() == TokenTypes.IDENT)
			return name.equals(firstChild.getText());

		if (firstChild.getType() == TokenTypes.DOT) {
			// getLastChild, not getNextSibling: an explicit type witness (obj.<T>method())
			// inserts a TYPE_ARGUMENTS node between the receiver and the method name
			final var methodName = firstChild.getLastChild();
			return methodName != null && methodName.getType() == TokenTypes.IDENT && name.equals(methodName.getText());
		}
		return false;
	}

	@CheckReturnValue
	public static boolean isPostDelayedWithInlineBlock(@Nonnull DetailAST ast) {
		if (ast.getType() != TokenTypes.METHOD_CALL || !isMethodCallNamed(ast, "postDelayed"))
			return false;
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		return args.size() == 2 && directInlineBlock(args.getFirst()) != null;
	}

	@CheckReturnValue
	public static boolean isPutWithInlineBlockValue(@Nonnull DetailAST ast, @Nonnull Predicate<DetailAST> contextSpecial) {
		if (ast.getType() != TokenTypes.METHOD_CALL || !isMethodCallNamed(ast, "put"))
			return false;
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		return args.size() == 2 && containsInlineBlockArg(args.get(1), true, contextSpecial);
	}

	@CheckReturnValue
	public static boolean isSingleInlineBlockArg(@Nonnull DetailAST ast, @Nonnull Predicate<DetailAST> contextSpecial) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		if (args.size() != 1)
			return false;
		// don't match special methods inside args when this call is itself a special method
		final var matchStaticSpecial = ast.getType() != TokenTypes.METHOD_CALL || !isStaticSpecialInlineMethodCall(ast);
		return containsInlineBlockArg(args.getFirst(), matchStaticSpecial, contextSpecial);
	}

	@CheckReturnValue
	public static boolean isSingleTernaryArg(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		return args.size() == 1 && isTernary(args.getFirst());
	}

	/**
	 * Whether {@code owner}'s whole argument list must keep its opening-line shape and so cannot be
	 * re-laid-out as a plain flat list: an inline-block configuration (see
	 * {@link #isInlineBlockConfiguration}) or a ternary configuration (a sole ternary argument, or a
	 * {@code this}/resource-id first argument followed by a ternary).
	 */
	@CheckReturnValue
	public static boolean isSpecialLayoutConfiguration(@Nonnull DetailAST owner, @Nonnull Predicate<DetailAST> contextSpecial) {
		return isInlineBlockConfiguration(owner, contextSpecial) || isSingleTernaryArg(owner) || isThisAndTernaryArgs(owner);
	}

	/**
	 * Whether {@code methodCall} is a statically-recognizable special inline call ({@code List.of},
	 * {@code Map.of}, {@code Set.of}/{@code copyOf}, {@code Arrays.asList}), including through an
	 * explicit type witness or a fully-qualified receiver. Context-independent.
	 */
	@CheckReturnValue
	public static boolean isStaticSpecialInlineMethodCall(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return false;

		if (firstChild.getType() == TokenTypes.IDENT) {
			final var entry = SPECIAL_INLINE_METHODS.get(firstChild.getText());
			return entry != null && entry.isEmpty();
		}

		if (firstChild.getType() == TokenTypes.DOT) {
			final var receiver = firstChild.getFirstChild();
			// getLastChild, not getNextSibling: an explicit type witness (Receiver.<T>method())
			// inserts a TYPE_ARGUMENTS node between the receiver and the method name
			final var methodName = firstChild.getLastChild();
			if (receiver == null || methodName == null || methodName.getType() != TokenTypes.IDENT)
				return false;

			final var entry = SPECIAL_INLINE_METHODS.get(methodName.getText());
			if (entry != null) {
				if (receiver.getType() == TokenTypes.IDENT)
					return entry.contains(receiver.getText());

				// FQN, e.g. java.util.Arrays.asList(...): extract last segment
				if (receiver.getType() == TokenTypes.DOT) {
					var last = receiver.getFirstChild();
					while (last.getNextSibling() != null)
						last = last.getNextSibling();
					if (last.getType() == TokenTypes.IDENT)
						return entry.contains(last.getText());
				}
			}
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
	public static boolean isThisAndInlineBlockArgs(@Nonnull DetailAST ast, @Nonnull Predicate<DetailAST> contextSpecial) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		if (args.size() != 2 || !isCompactFirstArg(args.getFirst()))
			return false;
		final var matchStaticSpecial = ast.getType() != TokenTypes.METHOD_CALL || !isStaticSpecialInlineMethodCall(ast);
		return containsInlineBlockArg(args.get(1), matchStaticSpecial, contextSpecial);
	}

	@CheckReturnValue
	public static boolean isThisAndTernaryArgs(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		final var args = topLevelArgs(elist);
		return args.size() == 2 && isCompactFirstArg(args.getFirst()) && isTernary(args.get(1));
	}

	/**
	 * The top-level arguments of {@code elist} (its non-{@code COMMA} children) in source order. A
	 * shared helper for the arg-count/arg-shape classifiers so each does not re-walk the child list.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> topLevelArgs(@Nonnull DetailAST elist) {
		final var args = new ArrayList<DetailAST>();
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				args.add(child);
		}
		return args;
	}

	private ArgLayoutClassifier() {
	}
}