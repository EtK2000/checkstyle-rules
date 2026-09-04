package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.EmptyBodyCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Removes the empty construct reported by {@link EmptyBodyCheck}, keeping any side effect its
 * condition would have had. An {@code if} condition runs exactly once, so its side effects are
 * hoisted out as statements in the construct's place; a loop header runs once per iteration, so
 * nothing can be hoisted there and a header that mutates anything outliving the loop refuses.
 */
class EmptyBodyFixer implements CheckstyleFixer {
	private sealed interface Plan permits Refusal, Removal {}

	private record Refusal(@Nonnull String reason) implements Plan {}

	private record Removal(@Nonnull DetailAST start, @Nonnull DetailAST end, @Nonnull List<String> hoisted) implements Plan {}

	/** Expression forms whose evaluation can mutate program state and that are legal as statements. */
	private static final Set<Integer> HOISTABLE = Set.of(
			TokenTypes.DEC,
			TokenTypes.INC,
			TokenTypes.METHOD_CALL,
			TokenTypes.POST_DEC,
			TokenTypes.POST_INC
	);

	/** Forms that evaluate only some of their operands, so a nested side effect may never run. */
	private static final Set<Integer> UNPREDICTABLE = Set.of(
			TokenTypes.LAND,
			TokenTypes.LITERAL_SWITCH,
			TokenTypes.LOR,
			TokenTypes.QUESTION
	);

	@CheckReturnValue
	@Nullable
	private static DetailAST bodyEnd(@Nonnull DetailAST body) {
		return body.getType() == TokenTypes.EMPTY_STAT ? body : body.findFirstToken(TokenTypes.RCURLY);
	}

	/**
	 * The outermost mutating sub-expressions under {@code root}, in evaluation order. Outermost so a
	 * call's arguments ride along inside it rather than being hoisted a second time.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> collectSideEffects(@Nonnull DetailAST root) {
		final var found = new ArrayList<DetailAST>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node != root && isHoistable(node)) {
				found.add(node);
				continue;
			}
			final var children = new ArrayDeque<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				children.push(child);
			for (var child : children)
				stack.push(child);
		}
		return found;
	}

	/** {@code construct}'s empty body, or {@code null} when it is no longer empty at that position. */
	@CheckReturnValue
	@Nullable
	private static DetailAST emptyBodyOf(@Nonnull DetailAST construct) {
		final var body = switch (construct.getType()) {
			case TokenTypes.INSTANCE_INIT, TokenTypes.STATIC_INIT -> construct.findFirstToken(TokenTypes.SLIST);
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_ELSE -> construct.getFirstChild();
			case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_WHILE -> {
				final var rparen = construct.findFirstToken(TokenTypes.RPAREN);
				yield rparen == null ? null : rparen.getNextSibling();
			}
			case TokenTypes.LITERAL_IF -> {
				final var slist = construct.findFirstToken(TokenTypes.SLIST);
				yield slist == null ? construct.findFirstToken(TokenTypes.EMPTY_STAT) : slist;
			}
			default -> null;
		};
		return body != null && AstQuery.isEmptyBody(body) ? body : null;
	}

	/**
	 * The exact source text of {@code node}, or {@code null} when it does not fit on one line.
	 * The extent is read back off the source rather than summed from token lengths: an imaginary
	 * node carries its token-type name as text and occupies no source at all, so only a node whose
	 * text really does appear at its own column contributes an end.
	 */
	@CheckReturnValue
	@Nullable
	private static String exactSourceText(@Nonnull DetailAST node, @Nonnull List<String> lines) {
		final var lineNo = node.getLineNo();
		if (lineNo < 1 || lineNo > lines.size())
			return null;
		final var line = lines.get(lineNo - 1);
		var start = Integer.MAX_VALUE;
		var end = -1;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (current.getLineNo() != lineNo)
				return null;
			final var index = LineText.charIndexOfColumn(line, current.getColumnNo());
			if (index < 0)
				return null;
			if (index < start)
				start = index;
			final var text = current.getText();
			if (line.startsWith(text, index) && index + text.length() > end)
				end = index + text.length();
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return start > end ? null : line.substring(start, end);
	}

	/**
	 * Rewrites the one line a stray {@code ;} sits on. The semicolon is the whole span, so anything
	 * else on that line stays, such as a trailing comment or an enclosing brace.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt fixStrayStatement(@Nonnull List<String> lines, int lineIndex, int index) {
		final var line = lines.get(lineIndex);
		final var head = line.substring(0, index).stripTrailing();
		final var tail = line.substring(index + 1).strip();
		if (head.isEmpty() && tail.isEmpty())
			return new FixResult(lineIndex, lineIndex, List.of());
		if (head.isEmpty())
			return new FixResult(lineIndex, lineIndex, List.of(LineText.extractIndent(line) + tail));
		return new FixResult(lineIndex, lineIndex, List.of(tail.isEmpty() ? head : head + " " + tail));
	}

	/**
	 * Whether removing {@code forAst} preserves every side effect its header would have had.
	 * The variables the init clause declares die with the loop, so a condition or update touching
	 * only those is free to go; anything else outlives the loop.
	 */
	@CheckReturnValue
	private static boolean forHeaderIsRemovable(@Nonnull DetailAST forAst) {
		final var each = forAst.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
		if (each != null)
			return AstQuery.isSideEffectFree(each);

		final var init = forAst.findFirstToken(TokenTypes.FOR_INIT);
		final var cond = forAst.findFirstToken(TokenTypes.FOR_CONDITION);
		final var iter = forAst.findFirstToken(TokenTypes.FOR_ITERATOR);
		if (init == null || cond == null || iter == null)
			return false;

		final var declared = new HashSet<String>();
		for (var child = init.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;
			// an init clause of expressions (`for (i = 0; ...)`) assigns something declared
			// elsewhere, which the loop's removal would have to keep
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				return false;
			// only the initializer is inspected: a declaration's own `=` is an ASSIGN token, so
			// testing the whole VARIABLE_DEF would report every initialized loop variable as a
			// side effect
			final var assign = child.findFirstToken(TokenTypes.ASSIGN);
			final var initializer = assign == null ? null : assign.getFirstChild();
			if (initializer != null && !AstQuery.isSideEffectFree(initializer))
				return false;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident == null)
				return false;
			declared.add(ident.getText());
		}
		return AstQuery.isSideEffectFree(cond) && mutatesOnly(iter, declared);
	}

	/**
	 * The statements standing in for {@code condition}'s side effects, or {@code null} when nothing
	 * in it has a statement form or their source text cannot be recovered exactly. Empty when the
	 * condition mutates nothing.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> hoistedStatements(@Nonnull DetailAST condition, @Nonnull List<String> lines) {
		if (AstQuery.isSideEffectFree(condition))
			return List.of();

		// the collector recognises exactly the forms that make a condition not side-effect-free, so
		// an empty result would mean the two have drifted apart and a side effect is about to be
		// deleted rather than hoisted
		final var nodes = collectSideEffects(condition);
		if (nodes.isEmpty())
			return null;

		final var statements = new ArrayList<String>(nodes.size());
		for (var node : nodes) {
			final var text = exactSourceText(node, lines);
			if (text == null)
				return null;
			statements.add(text + ";");
		}
		return statements;
	}

	/**
	 * The plan for an empty-bodied {@code if}. An {@code else} branch would have to be re-attached
	 * to a negated condition, which is a rewrite rather than a removal, so it refuses instead; an
	 * {@code else if} hands back the enclosing {@code else}, since dropping only the inner
	 * {@code if} would leave a dangling {@code else}.
	 */
	@CheckReturnValue
	@Nonnull
	private static Plan ifRemoval(@Nonnull DetailAST ifAst, @Nonnull DetailAST end, @Nonnull List<String> lines) {
		if (ifAst.findFirstToken(TokenTypes.LITERAL_ELSE) != null)
			return new Refusal(SkipMessages.EMPTY_SKIP_ELSE_BRANCH);
		final var condition = ifAst.findFirstToken(TokenTypes.EXPR);
		if (condition == null)
			return new Refusal(SkipMessages.EMPTY_SKIP_NO_NODE);
		if (picksOperands(condition))
			return new Refusal(SkipMessages.EMPTY_SKIP_HOIST_SHORT_CIRCUIT);
		final var hoisted = hoistedStatements(condition, lines);
		if (hoisted == null)
			return new Refusal(SkipMessages.EMPTY_SKIP_HOIST_MULTILINE);
		final var parent = ifAst.getParent();
		if (parent == null || parent.getType() != TokenTypes.LITERAL_ELSE)
			return new Removal(ifAst, end, hoisted);
		// the span starts at the `else`, so a hoisted statement lands outside the chain and would
		// run even on the paths an earlier branch already took
		return hoisted.isEmpty()
				? new Removal(parent, end, hoisted)
				: new Refusal(SkipMessages.EMPTY_SKIP_HOIST_OUT_OF_ELSE);
	}

	@CheckReturnValue
	private static boolean isHoistable(@Nonnull DetailAST node) {
		final var type = node.getType();
		// `new T[n]` is not a statement, and allocating an array runs no user code anyway;
		// a side effect in its dimensions is reached by descending past it
		if (type == TokenTypes.LITERAL_NEW)
			return node.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null;
		return HOISTABLE.contains(type) || AstQuery.isAssignmentOperator(type);
	}

	@CheckReturnValue
	private static boolean isRemovableConstruct(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.EMPTY_STAT, TokenTypes.INSTANCE_INIT, TokenTypes.LITERAL_DO,
			     TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF,
			     TokenTypes.LITERAL_WHILE, TokenTypes.STATIC_INIT -> true;
			default -> false;
		};
	}

	/**
	 * Whether {@code root}'s subtree mutates nothing outside {@code names}. Targets are compared by
	 * simple name, so a field access or an array element is rejected, since an init clause can
	 * declare neither.
	 */
	@CheckReturnValue
	private static boolean mutatesOnly(@Nonnull DetailAST root, @Nonnull Set<String> names) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node != root && isHoistable(node)) {
				final var type = node.getType();
				if (type == TokenTypes.METHOD_CALL || type == TokenTypes.LITERAL_NEW)
					return false;
				final var target = node.getFirstChild();
				if (target == null || target.getType() != TokenTypes.IDENT || !names.contains(target.getText()))
					return false;
				final var value = target.getNextSibling();
				if (value != null && !AstQuery.isSideEffectFree(value))
					return false;
				continue;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return true;
	}

	@CheckReturnValue
	private static boolean picksOperands(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (UNPREDICTABLE.contains(node.getType()))
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * The span to delete for the construct at the reported position, or a refusal. A {@code do}'s
	 * span runs past its body to the loop's own terminating {@code ;}.
	 */
	@CheckReturnValue
	@Nonnull
	private static Plan planRemoval(@Nonnull DetailAST construct, @Nonnull List<String> lines) {
		final var body = emptyBodyOf(construct);
		if (body == null)
			return new Refusal(SkipMessages.EMPTY_SKIP_NO_NODE);
		final var end = construct.getType() == TokenTypes.LITERAL_DO
				? construct.getLastChild()
				: bodyEnd(body);
		if (end == null || (construct.getType() == TokenTypes.LITERAL_DO && end.getType() != TokenTypes.SEMI))
			return new Refusal(SkipMessages.EMPTY_SKIP_NO_NODE);

		return switch (construct.getType()) {
			case TokenTypes.LITERAL_DO, TokenTypes.LITERAL_WHILE -> {
				final var condition = construct.findFirstToken(TokenTypes.EXPR);
				yield condition != null && AstQuery.isSideEffectFree(condition)
						? new Removal(construct, end, List.of())
						: new Refusal(SkipMessages.EMPTY_SKIP_LOOP_SIDE_EFFECT);
			}
			case TokenTypes.LITERAL_FOR -> forHeaderIsRemovable(construct)
					? new Removal(construct, end, List.of())
					: new Refusal(SkipMessages.EMPTY_SKIP_LOOP_SIDE_EFFECT);
			case TokenTypes.LITERAL_IF -> ifRemoval(construct, end, lines);
			default -> new Removal(construct, end, List.of());
		};
	}

	/**
	 * Whether a comment occupies the span. The caller has already established that nothing precedes
	 * the construct on its first line, so the scan can start there in the default lexer state.
	 */
	@CheckReturnValue
	private static boolean spanHasComment(@Nonnull List<String> lines, int startLine, int endLine, int endIndex) {
		var state = LexerState.NONE;
		for (var i = startLine; i <= endLine; ++i) {
			final var line = lines.get(i);
			final var marker = JavaLineScanner.firstCommentMarker(line, state);
			if (marker >= 0 && (i < endLine || marker < endIndex))
				return true;
			state = JavaLineScanner.stateAfter(line, state);
		}
		return false;
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var construct = FixerAst.withAst(
				lines,
				root -> AstQuery.findNodeAt(root, lineIndex, column, EmptyBodyFixer::isRemovableConstruct)
		);
		if (construct == null)
			return new SkipResult(SkipMessages.EMPTY_SKIP_NO_NODE);

		if (construct.getType() == TokenTypes.EMPTY_STAT) {
			final var index = LineText.charIndexOfColumn(lines.get(lineIndex), construct.getColumnNo());
			if (EmptyBodyCheck.isRequiredClauseBody(construct) || index < 0 || index >= lines.get(lineIndex).length())
				return new SkipResult(SkipMessages.EMPTY_SKIP_NO_NODE);
			return fixStrayStatement(lines, lineIndex, index);
		}

		final var plan = planRemoval(construct, lines);
		if (plan instanceof Refusal(String reason))
			return new SkipResult(reason);
		final var removal = (Removal) plan;

		final var start = removal.start();
		final var end = removal.end();
		// a statement that is itself a clause's whole body cannot simply go, or the clause is left
		// without one. An `else` is a clause rather than a statement, so removing it is fine
		if (start.getType() != TokenTypes.LITERAL_ELSE && EmptyBodyCheck.isRequiredClauseBody(start))
			return new SkipResult(SkipMessages.EMPTY_SKIP_REQUIRED_CLAUSE_BODY);
		if (start.getType() == TokenTypes.LITERAL_ELSE && AstQuery.rebindsAFollowingElse(start))
			return new SkipResult(SkipMessages.EMPTY_SKIP_ELSE_REBIND);
		final var startLine = start.getLineNo() - 1;
		final var endLine = end.getLineNo() - 1;
		if (startLine < 0 || startLine > endLine || endLine >= lines.size())
			return new SkipResult(SkipMessages.EMPTY_SKIP_NO_NODE);
		final var firstLine = lines.get(startLine);
		final var startIndex = LineText.charIndexOfColumn(firstLine, start.getColumnNo());
		final var lastLine = lines.get(endLine);
		final var endColumn = LineText.charIndexOfColumn(lastLine, end.getColumnNo());
		if (startIndex < 0 || endColumn < 0)
			return new SkipResult(SkipMessages.EMPTY_SKIP_NO_NODE);
		final var endIndex = Math.min(endColumn + end.getText().length(), lastLine.length());
		if (!firstLine.substring(0, startIndex).isBlank())
			return new SkipResult(SkipMessages.EMPTY_SKIP_SHARED_LINE);
		if (spanHasComment(lines, startLine, endLine, endIndex))
			return new SkipResult(SkipMessages.EMPTY_SKIP_COMMENT_IN_SPAN);

		final var hoisted = removal.hoisted();
		final var indent = LineText.extractIndent(firstLine);
		final var replacement = new ArrayList<String>(hoisted.size() + 1);
		for (var statement : hoisted)
			replacement.add(indent + statement);
		final var trailing = lastLine.substring(endIndex).strip();
		if (!trailing.isEmpty())
			replacement.add(indent + trailing);
		return new FixResult(startLine, endLine, replacement);
	}
}