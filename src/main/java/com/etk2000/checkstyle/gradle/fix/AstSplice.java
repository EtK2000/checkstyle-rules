package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.SpanReformat;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Turns a located AST node back into a single-line text edit: where the node's tokens sit, what
 * the source reads between two of them, and the line that results from replacing one span.
 *
 * <p>Shared by every fixer that dispatches on a check's locator rather than on the line's text.
 */
final class AstSplice {
	/**
	 * A single-line edit: the 0-based line it replaces and the text that line becomes.
	 */
	record Rewrite(int line, @Nonnull String text) {}

	/**
	 * The position just past the call's own {@code (}, which is where its arguments begin.
	 */
	@CheckReturnValue
	@Nullable
	static TextPos afterLparenOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var lparen = positionOf(lines, call);
		return lparen == null ? null : new TextPos(lparen.line(), lparen.index() + 1);
	}

	/**
	 * The {@code ELIST} children of {@code call} that are arguments rather than separators.
	 */
	@CheckReturnValue
	@Nonnull
	static List<DetailAST> argumentsIn(@Nonnull DetailAST call) {
		final var arguments = new ArrayList<DetailAST>();
		final var elist = call.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return arguments;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				arguments.add(child);
		}
		return arguments;
	}

	/**
	 * The verbatim argument text of {@code call}, between its {@code (} and its {@code )}, or null
	 * when the span does not slice. Taken off the parens rather than the {@code ELIST} because an
	 * empty argument list produces a childless {@code ELIST} that borrows its position from the
	 * {@code )} and so describes no span of its own.
	 */
	@CheckReturnValue
	@Nullable
	static String argumentsOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		return textBetween(lines, afterLparenOf(lines, call), rparenOf(lines, call));
	}

	/**
	 * {@code text} made safe to use as a receiver. {@code Collections.sort(x)} moves its argument
	 * in front of the {@code .}, where only a primary expression is legal: a cast argument would
	 * become {@code (List<String>) raw.sort(null)}, which binds the call to {@code raw}.
	 */
	@CheckReturnValue
	@Nonnull
	static String asReceiver(@Nonnull DetailAST argument, @Nonnull String text) {
		final var inner = argument.getType() == TokenTypes.EXPR ? argument.getFirstChild() : argument;
		return inner != null && isPrimary(inner) ? text : "(" + text + ")";
	}

	/**
	 * The index just past the {@code *&#47;} closing a block comment opened at {@code at}.
	 */
	@CheckReturnValue
	static int blockCommentEnd(@Nonnull String line, int at) {
		final var close = line.indexOf("*/", at + 2);
		return close < 0 ? line.length() : close + 2;
	}

	/**
	 * Whether replacing {@code [start, end)} with {@code text} would drop a comment written inside
	 * that span. A rewrite is free to move a comment, and several do (the arguments it copies
	 * verbatim carry theirs along), so the test is whether the comment's source survives into the
	 * replacement rather than whether the span contained one.
	 *
	 * <p>Without this, a rewrite that collapses a chain deletes any comment in it:
	 * {@code list.stream()/*c*}{@code /.count()} became {@code list.size()}, silently losing the
	 * comment. Measured across twelve comment positions, seven were being dropped.
	 */
	@CheckReturnValue
	static boolean discardsAComment(
			@Nonnull List<String> lines,
			@Nonnull TextPos start,
			@Nonnull TextPos end,
			@Nonnull String text
	) {
		final var line = lines.get(start.line());
		var state = SpanReformat.lexerStateAt(lines, start.line());
		var from = 0;
		while (from < line.length()) {
			final var relative = JavaLineScanner.firstCommentMarker(line.substring(from), state);
			if (relative < 0)
				return false;
			final var at = from + relative;
			final int close;
			if (state.inBlockComment()) {
				// the marker reports index 0 for a comment carried in from an earlier line, where
				// this line holds the closing `*/` rather than an opener
				final var carried = JavaLineScanner.multilineLiteralCloseIndex(line, state);
				close = carried < 0 ? line.length() : carried;
			}
			else
				close = line.startsWith("//", at) ? line.length() : blockCommentEnd(line, at);
			if (at < end.index() && close > start.index() && !text.contains(line.substring(at, close)))
				return true;
			if (close >= line.length())
				return false;
			// a closed comment leaves the lexer where it started, and `from` never lands in a literal
			state = JavaLineScanner.LexerState.NONE;
			// the carried-comment arm derives close from index 0 rather than from `from`, so the
			// advance is made explicit rather than resting on that arm only running first
			from = Math.max(close, from + 1);
		}
		return false;
	}

	/**
	 * The position of the {@code .} introducing {@code call}'s own method name, or null if unqualified.
	 */
	@CheckReturnValue
	@Nullable
	static TextPos dotOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var dot = call.findFirstToken(TokenTypes.DOT);
		return dot == null ? null : positionOf(lines, dot);
	}

	/**
	 * The position of the first argument separator of {@code call}, or null when it has fewer than two.
	 */
	@CheckReturnValue
	@Nullable
	static TextPos firstCommaOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var elist = call.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				return positionOf(lines, child);
		}
		return null;
	}

	/**
	 * Whether {@code node} is an expression no surrounding operator can split.
	 */
	@CheckReturnValue
	static boolean isPrimary(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.CHAR_LITERAL, TokenTypes.DOT, TokenTypes.IDENT, TokenTypes.INDEX_OP,
			     TokenTypes.LITERAL_FALSE, TokenTypes.LITERAL_NEW, TokenTypes.LITERAL_NULL,
			     TokenTypes.LITERAL_THIS, TokenTypes.LITERAL_TRUE, TokenTypes.METHOD_CALL,
			     TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT,
			     TokenTypes.NUM_LONG, TokenTypes.STRING_LITERAL, TokenTypes.TEXT_BLOCK_LITERAL_BEGIN -> true;
			default -> false;
		};
	}

	/**
	 * Whether {@code parent} is a position binding looser than any operator a lifted expression can
	 * contain, so the expression needs no parens there. An {@code EXPR} covers a statement, an
	 * argument and a variable initializer; an assignment is tested separately because
	 * {@code msg = x} parents the call with the {@code ASSIGN} rather than an {@code EXPR}, which
	 * a plain {@code EXPR} test mistook for a tighter context and parenthesized.
	 */
	@CheckReturnValue
	static boolean isValueContext(@Nonnull DetailAST parent) {
		return parent.getType() == TokenTypes.EXPR || AstQuery.isAssignmentOperator(parent.getType());
	}

	/**
	 * The verbatim text between the {@code (} and {@code )} tokens <em>inside</em> {@code node}.
	 *
	 * <p>{@link #argumentsOf} cannot answer for a {@code LITERAL_NEW}: it reads the opening paren off
	 * the node's own position, which is right for a {@code METHOD_CALL} but points at the
	 * {@code new} keyword here.
	 */
	@CheckReturnValue
	@Nullable
	static String parenthesizedArgumentsOf(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var lparen = node.findFirstToken(TokenTypes.LPAREN);
		final var lparenPos = lparen == null ? null : positionOf(lines, lparen);
		return lparenPos == null
				? null
				: textBetween(lines, new TextPos(lparenPos.line(), lparenPos.index() + 1), rparenOf(lines, node));
	}

	/**
	 * {@code text}, which is {@code call}'s single argument lifted into {@code call}'s place, wrapped in
	 * parens when it needs them to keep its original grouping. The removed call's own parens were
	 * doing that work: {@code String.format(a + b).trim()} became {@code a + b.trim()}, which
	 * compiles and trims only {@code b}. A primary expression binds tighter than anything around it
	 * and is always safe; anything else is safe only in a value context, where nothing binds
	 * tighter.
	 */
	@CheckReturnValue
	@Nonnull
	static String parenthesizeIfNeeded(@Nonnull DetailAST call, @Nonnull DetailAST argument, @Nonnull String text) {
		final var inner = argument.getType() == TokenTypes.EXPR ? argument.getFirstChild() : argument;
		final var parent = call.getParent();
		if (inner == null || parent == null || isValueContext(parent) || isPrimary(inner))
			return text;
		return "(" + text + ")";
	}

	/**
	 * The source position of {@code node}'s own token, converted from its code-point column.
	 */
	@CheckReturnValue
	@Nullable
	static TextPos positionOf(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var line = node.getLineNo() - 1;
		if (line < 0 || line >= lines.size())
			return null;
		final var index = LineText.charIndexOfColumn(lines.get(line), node.getColumnNo());
		return index < 0 ? null : new TextPos(line, index);
	}

	/**
	 * The receiver of a dotted call, or null for an unqualified call with no {@code DOT} at all.
	 */
	@CheckReturnValue
	@Nullable
	static DetailAST receiverOf(@Nullable DetailAST call) {
		if (call == null)
			return null;
		final var dot = call.findFirstToken(TokenTypes.DOT);
		return dot == null ? null : dot.getFirstChild();
	}

	/**
	 * The receiver's verbatim text, but only when re-emitting it is safe. A receiver written inside
	 * grouping parens is not: the parens are siblings under the {@code DOT} rather than part of the
	 * receiver's own subtree, so {@code getFirstChild()} hands back the {@code (} itself and slicing
	 * it yields the single character {@code "("}. Measured on {@code (a + b).trim().length() == 0},
	 * which produced {@code return (.isBlank();}.
	 */
	@CheckReturnValue
	@Nullable
	static String reemittableReceiverOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var receiver = receiverOf(call);
		if (receiver == null)
			return null;
		final var end = AstSpan.spanEnd(lines, receiver);
		final var dot = dotOf(lines, call);
		if (end == null || dot == null || end.line() != dot.line() || end.index() > dot.index())
			return null;
		// only whitespace may sit between the two. A paren shape leaves real code there (the
		// receiver's span ends just past its own `(`), and a comment is not blank, so both stay
		// refused; `list .size()` no longer does
		if (!lines.get(end.line()).substring(end.index(), dot.index()).isBlank())
			return null;
		// a receiver starting on an earlier line makes sliceNode join and reflow it, rewriting
		// source the fixer was not asked to touch. The callers refuse it downstream today, but
		// this method promises its own result is safe to re-emit
		final var start = AstSpan.spanStart(lines, receiver);
		if (start == null || start.line() != dot.line())
			return null;
		return AstSpan.sliceNode(lines, receiver);
	}

	@CheckReturnValue
	@Nullable
	static TextPos rparenOf(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		return rparen == null ? null : positionOf(lines, rparen);
	}

	/**
	 * Replaces the whole call with {@code text} logically negated: a {@code !} already written
	 * immediately before it is removed rather than a second one added, so the output is never
	 * {@code !!x}.
	 */
	@CheckReturnValue
	@Nullable
	static Rewrite spliceNegated(@Nonnull List<String> lines, @Nonnull DetailAST node, @Nonnull String text) {
		final var start = AstSpan.spanStart(lines, node);
		final var end = AstSpan.spanEnd(lines, node);
		if (start == null || end == null)
			return null;
		if (start.index() > 0 && lines.get(start.line()).charAt(start.index() - 1) == '!')
			return spliceSpan(lines, new TextPos(start.line(), start.index() - 1), end, text);
		return spliceSpan(lines, start, end, "!" + text);
	}

	/**
	 * Replaces {@code node}'s whole span with {@code text}, or null when it is not on one line.
	 */
	@CheckReturnValue
	@Nullable
	static Rewrite spliceNode(@Nonnull List<String> lines, @Nonnull DetailAST node, @Nonnull String text) {
		return spliceSpan(lines, AstSpan.spanStart(lines, node), AstSpan.spanEnd(lines, node), text);
	}

	/**
	 * The line {@code [start, end)} would become with {@code text} in place of it, or null when
	 * the span is absent, runs backwards, or crosses a line boundary. A cross-line span is refused
	 * rather than collapsed: joining the lines would move code the fixer was not asked to touch.
	 */
	@CheckReturnValue
	@Nullable
	static Rewrite spliceSpan(
			@Nonnull List<String> lines,
			@Nullable TextPos start,
			@Nullable TextPos end,
			@Nonnull String text
	) {
		if (start == null || end == null || start.line() != end.line() || start.index() > end.index())
			return null;
		final var line = lines.get(start.line());
		if (end.index() > line.length() || discardsAComment(lines, start, end, text))
			return null;
		return new Rewrite(start.line(), line.substring(0, start.index()) + text + line.substring(end.index()));
	}

	/**
	 * Replaces {@code .name(args)}, from the {@code .} that introduces {@code anchor}'s method
	 * name through the end of {@code call}, with {@code text}, leaving the receiver chain in
	 * front of it exactly as written. {@code anchor} is the innermost call of the chain being
	 * collapsed.
	 */
	@CheckReturnValue
	@Nullable
	static Rewrite spliceTail(
			@Nonnull List<String> lines,
			@Nullable DetailAST anchor,
			@Nonnull DetailAST call,
			@Nonnull String text
	) {
		return anchor == null ? null : spliceSpan(lines, dotOf(lines, anchor), AstSpan.spanEnd(lines, call), text);
	}

	/**
	 * Like {@link #spliceTail} but stopping just past {@code call}'s {@code (}, so the arguments
	 * and the closing paren survive untouched even when they run onto later lines.
	 */
	@CheckReturnValue
	@Nullable
	static Rewrite spliceTailToLparen(
			@Nonnull List<String> lines,
			@Nullable DetailAST anchor,
			@Nonnull DetailAST call,
			@Nonnull String text
	) {
		return anchor == null ? null : spliceSpan(lines, dotOf(lines, anchor), afterLparenOf(lines, call), text);
	}

	/**
	 * The verbatim source between two positions, which must lie on one line.
	 */
	@CheckReturnValue
	@Nullable
	static String textBetween(@Nonnull List<String> lines, @Nullable TextPos from, @Nullable TextPos to) {
		return AstSpan.sliceSpan(lines, from, to);
	}

	private AstSplice() {
	}
}