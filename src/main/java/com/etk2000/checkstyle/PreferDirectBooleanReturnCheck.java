package com.etk2000.checkstyle;

import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.SpanEdges;
import com.etk2000.checkstyle.format.SpanReformat;
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
 * Flags an {@code if} whose body and paired {@code return} (the {@code else}
 * branch or the immediately following sibling statement) are each a single
 * {@code return <expr>;}, where the pair collapses to one {@code return} without
 * changing behavior:
 *
 * <ul>
 *     <li><b>Opposite literals</b> {@code if (C) return true; return false;} -> {@code return C;}
 *         (and {@code return false;}/{@code return true;} -> {@code return !C;}).</li>
 *     <li><b>Same literal</b> {@code if (C) return true; return true;} -> {@code return true;}:
 *         {@code C} is dropped when it is side-effect-free (a pointless {@code if}, dropped even if the
 *         condition could throw); when {@code C} has a hoistable side effect that effect is extracted ahead
 *         of the return (e.g. {@code if (compute()) ...} -> {@code compute();} then {@code return true;}).
 *         Otherwise not flagged.</li>
 *     <li><b>One literal (mixed)</b> collapses via short-circuit: {@code if (C) return X; return false;}
 *         -> {@code return C && X;}; {@code return true;} -> {@code return !C || X;};
 *         {@code if (C) return true; return X;} -> {@code return C || X;};
 *         {@code return false;} -> {@code return !C && X;}.</li>
 * </ul>
 *
 * <p>When both returns are non-literal expressions the pair is a ternary
 * ({@code C ? X : Y}) and is left alone. A mixed collapse is also left alone when the non-literal return
 * spans more than one line (a genuine guarded body), or when it would mix {@code &&} with {@code ||} or
 * use three or more boolean operators. Any collapse is left alone when a line it would emit exceeds the
 * line-length budget.
 *
 * <p>The collapse itself lives here rather than in the fixer because the budget gate has to measure the
 * text that will actually be emitted: {@link #collapseOf} renders it from the source span, and
 * {@link #locateAt} hands the fixer the same decision this check logged.
 */
public class PreferDirectBooleanReturnCheck extends AbstractAstCheck {
	/**
	 * The statement lines a collapse emits, without indentation (a nested line carries its own leading
	 * tab), and the comments relocated out of the replaced span merged into one trailing {@code //}
	 * comment (empty when none had to move).
	 */
	public record DirectBooleanCollapse(@Nonnull List<String> statements, @Nonnull String comment) {}

	/** Which collapse an {@code if} qualifies for. */
	public enum DirectBooleanRule {
		COMBINE("prefer.direct.boolean.return.combine"),
		OPPOSITE("prefer.direct.boolean.return"),
		REDUNDANT("prefer.direct.boolean.return.redundant");

		private final String messageKey;

		DirectBooleanRule(@Nonnull String messageKey) {
			this.messageKey = messageKey;
		}

		@CheckReturnValue
		@Nonnull
		public String messageKey() {
			return messageKey;
		}
	}

	/**
	 * The collapse the check reported at a position. {@code thenLiteral} and {@code pairedLiteral} are
	 * the boolean each branch returns; exactly one of them is null for a
	 * {@link DirectBooleanRule#COMBINE}, and neither is for the other two rules.
	 *
	 * @see #locateAt
	 */
	public record DirectBooleanTarget(
			@Nonnull DirectBooleanRule rule,
			@Nonnull DetailAST ifAst,
			@Nonnull DetailAST conditionExpr,
			@Nonnull DetailAST thenReturn,
			@Nonnull DetailAST pairedReturn,
			@Nullable Boolean thenLiteral,
			@Nullable Boolean pairedLiteral
	) {}

	/**
	 * One rule's rewrite: the statement lines it emits, and the source regions it re-emits verbatim.
	 */
	private record Rendering(@Nonnull List<String> statements, @Nonnull List<TextSpan> kept) {}

	/**
	 * A hoistable side effect in a same-literal condition: {@code effect} is the statement expression to
	 * emit, and {@code guard} the pure left operand it must stay short-circuited behind, or null when it
	 * runs unconditionally.
	 */
	private record SideEffectHoist(@Nullable DetailAST guard, @Nonnull DetailAST effect) {}

	/**
	 * {@code statements} wrapped in a block when more than one of them has to land where {@code ifAst}
	 * stands. Only an {@code SLIST} holds a list of statements; under an unbraced {@code if},
	 * {@code else}, {@code for}, {@code while}, {@code do} or labelled statement the slot holds exactly
	 * one, so everything after the first would fall outside the enclosing statement: silently changing
	 * behavior where the result still compiles, and breaking the parse where it does not (a hoist under
	 * a {@code do}, or ahead of an {@code else}, leaves a clause with nothing to attach to).
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> blockIfNeeded(@Nonnull List<String> statements, @Nonnull DetailAST ifAst) {
		final var parent = ifAst.getParent();
		if (statements.size() < 2 || parent != null && parent.getType() == TokenTypes.SLIST)
			return statements;
		final var block = new ArrayList<String>(statements.size() + 2);
		block.add("{");
		for (var statement : statements)
			block.add('\t' + statement);
		block.add("}");
		return block;
	}

	/**
	 * Whether {@code node} carries a class body: an anonymous or local class declared inside the
	 * expression. Folded onto the collapsed line its members, annotations and statements all land there
	 * too, and the result, though it compiles, breaks formatting rules no re-indentation recovers from,
	 * so a collapse that would do it is refused instead.
	 */
	@CheckReturnValue
	private static boolean carriesAClassBody(@Nonnull DetailAST node) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (current.getType() == TokenTypes.OBJBLOCK)
				return true;
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * The replacement for {@code target} as rendered from {@code lines}, or null when the span cannot be
	 * collapsed onto one line: a text block inside it, whose significant whitespace a collapse would
	 * destroy, or a class body a collapse would fold onto the emitted line.
	 */
	@CheckReturnValue
	@Nullable
	public static DirectBooleanCollapse collapseOf(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var span = spanOf(lines, target);
		final var rendering = switch (target.rule()) {
			case COMBINE -> renderCombine(lines, target);
			case OPPOSITE -> renderOpposite(lines, target);
			case REDUNDANT -> renderRedundant(lines, target);
		};
		if (span == null || rendering == null)
			return null;
		return new DirectBooleanCollapse(
				blockIfNeeded(rendering.statements(), target.ifAst()),
				SpanComments.relocate(lines, span, rendering.kept())
		);
	}

	@CheckReturnValue
	@Nonnull
	private static String combineAnd(@Nonnull String left, @Nonnull String right) {
		return wrapForAnd(left) + " && " + wrapForAnd(right);
	}

	@CheckReturnValue
	@Nonnull
	private static String combineOr(@Nonnull String left, @Nonnull String right) {
		return wrapForOr(left) + " || " + wrapForOr(right);
	}

	/**
	 * The region between the {@code if}'s parentheses, which is wider than the condition {@code EXPR}'s
	 * own span: a comment written just inside either paren belongs to the condition's text, not to the
	 * dropped structure around it.
	 */
	@CheckReturnValue
	@Nullable
	private static TextSpan conditionRegion(@Nonnull List<String> lines, @Nonnull DetailAST ifAst) {
		final var lparen = ifAst.findFirstToken(TokenTypes.LPAREN);
		final var rparen = ifAst.findFirstToken(TokenTypes.RPAREN);
		if (lparen == null || rparen == null)
			return null;
		final var open = AstSpan.tokenEnd(lines, lparen);
		final var close = AstSpan.tokenStart(lines, rparen);
		return open == null || close == null ? null : new TextSpan(open, close);
	}

	@CheckReturnValue
	private static int countType(@Nonnull DetailAST root, int tokenType) {
		var count = 0;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == tokenType)
				++count;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return count;
	}

	/**
	 * Whether every line the collapse would emit stays within the line-length budget. Measured on the
	 * rendered text rather than on a normalized rendering of the condition, so interior whitespace and
	 * any comment kept inline count the same here as they will on disk, and assembled exactly as the
	 * fixer assembles it.
	 *
	 * <p>The relocated trailing comment is the one thing deliberately excluded, because a long comment
	 * would otherwise suppress the violation outright.
	 */
	@CheckReturnValue
	private static boolean fitsLineBudget(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var collapse = collapseOf(lines, target);
		final var span = spanOf(lines, target);
		// a span that does not render has no projected line to measure; the check still reports it and
		// the fixer skips it
		if (collapse == null || span == null)
			return true;
		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var statements = collapse.statements();
		for (var i = 0; i < statements.size(); ++i) {
			final var last = i == statements.size() - 1;
			final var line = (i == 0 ? edges.prefix() : edges.indent())
					+ statements.get(i) + (last ? edges.suffix() : "");
			if (LineLength.tabExpandedLength(line) > LineLength.MAX_LINE_LENGTH)
				return false;
		}
		return true;
	}

	/**
	 * Whether the collapse {@code target} describes would fold a class body onto one line, which is
	 * the one refusal {@link #collapseOf} makes that a text block does not explain.
	 */
	@CheckReturnValue
	public static boolean foldsAClassBody(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		if (foldsAClassBody(conditionRegion(lines, target.ifAst()), target.conditionExpr()))
			return true;
		if (target.rule() != DirectBooleanRule.COMBINE)
			return false;
		final var valueExpr = (target.thenLiteral() == null ? target.thenReturn() : target.pairedReturn())
				.findFirstToken(TokenTypes.EXPR);
		return valueExpr != null && foldsAClassBody(regionOf(lines, valueExpr), valueExpr);
	}

	/**
	 * Whether re-emitting {@code node} from {@code region} would fold a class body onto one line. A body
	 * already written on one line survives the collapse unchanged, so only a region that spans lines is
	 * refused.
	 *
	 * @see #carriesAClassBody
	 */
	@CheckReturnValue
	public static boolean foldsAClassBody(@Nullable TextSpan region, @Nonnull DetailAST node) {
		return region != null && region.start().line() != region.end().line() && carriesAClassBody(node);
	}

	/**
	 * Whether {@code expr} has a top-level (paren/bracket/brace depth 0) operator that binds looser than
	 * the operator it is about to be joined under, so it needs wrapping: a ternary {@code ?} or an
	 * assignment always, plus {@code ||} when {@code includeOr} (i.e. the join operator is {@code &&}).
	 */
	@CheckReturnValue
	private static boolean hasLooseDepth0Op(@Nonnull String expr, boolean includeOr) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(expr, LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			switch (c) {
				case '(', '[', '{' -> ++depth;
				case ')', ']', '}' -> --depth;
				case '=' -> {
					if (depth == 0 && isAssignEq(masked, i))
						return true;
				}
				case '?' -> {
					if (depth == 0)
						return true;
				}
				case '|' -> {
					if (includeOr && depth == 0 && i + 1 < masked.length() && masked.charAt(i + 1) == '|')
						return true;
				}
				default -> {
				}
			}
		}
		return false;
	}

	/**
	 * A hoisted side effect written as a statement. A postfix increment or decrement whose value is now
	 * discarded takes the project's preferred prefix form ({@code i++} -> {@code ++i}).
	 */
	@CheckReturnValue
	@Nullable
	private static String hoistedStatement(@Nonnull List<String> lines, @Nonnull DetailAST effect) {
		final var type = effect.getType();
		if (type == TokenTypes.POST_DEC || type == TokenTypes.POST_INC) {
			final var operand = effect.getFirstChild();
			final var operandText = operand == null ? null : spanCodeOf(lines, regionOf(lines, operand));
			if (operandText != null && !operandText.isBlank())
				return (type == TokenTypes.POST_INC ? "++" : "--") + operandText + ";";
		}
		final var text = spanCodeOf(lines, regionOf(lines, effect));
		return text == null || text.isBlank() ? null : text + ";";
	}

	/**
	 * True if the {@code =} at index {@code i} in a masked expression is an assignment operator (plain
	 * {@code =} or a compound {@code +=}/{@code &=}/{@code <<=}/... ), not part of a comparison
	 * ({@code ==}/{@code !=}/{@code <=}/{@code >=}). The tricky case is {@code <<=}/{@code >>=}/{@code >>>=}:
	 * the {@code =} is preceded by {@code <}/{@code >} like {@code <=}/{@code >=}, but a doubled preceding
	 * {@code <}/{@code >} marks a shift-assign rather than a comparison.
	 */
	@CheckReturnValue
	private static boolean isAssignEq(@Nonnull String masked, int i) {
		if (masked.charAt(i) != '=' || (i + 1 < masked.length() && masked.charAt(i + 1) == '='))
			return false;
		if (i == 0)
			return true;
		final var prev = masked.charAt(i - 1);
		if (prev == '=' || prev == '!')
			return false;
		if (prev == '<' || prev == '>')
			return i >= 2 && masked.charAt(i - 2) == prev;
		return true;
	}

	/**
	 * Whether {@code cond} is one token a {@code !} can be written in front of, rather than an
	 * expression that has to be parenthesised first. Measured on the masked text: a parenthesis inside
	 * a string literal, a char literal or a comment is content, not nesting, and a balanced pair of
	 * them hides whatever operator sits between.
	 */
	@CheckReturnValue
	private static boolean isAtomicCond(@Nonnull String cond) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(cond, LexerState.NONE);
		var depth = 0;
		final var len = masked.length();
		var i = 0;
		while (i < len) {
			final var cp = masked.codePointAt(i);
			if (cp == '(')
				++depth;
			else if (cp == ')') {
				// without a floor a leading `)` would drive the depth negative and a later `(` bring it
				// back to zero, and every character in between would be skipped as nested
				if (--depth < 0)
					return false;
			}
			else if (depth == 0
					&& !Character.isLetterOrDigit(cp)
					&& cp != '_'
					&& cp != '.'
					&& cp != '$')
				return false;
			i += Character.charCount(cp);
		}
		return depth == 0;
	}

	@CheckReturnValue
	private static boolean isStatementExpr(@Nonnull DetailAST node) {
		final var type = node.getType();
		return AstQuery.isAssignmentOperator(type)
				|| type == TokenTypes.DEC || type == TokenTypes.INC
				|| type == TokenTypes.METHOD_CALL
				|| type == TokenTypes.POST_DEC || type == TokenTypes.POST_INC;
	}

	@CheckReturnValue
	private static boolean isValuedReturn(@Nonnull DetailAST stmt) {
		return stmt.getType() == TokenTypes.LITERAL_RETURN && stmt.findFirstToken(TokenTypes.EXPR) != null;
	}

	/**
	 * Walks the parsed tree and returns what the check would log at {@code (line, column)} (0-based line
	 * index, 0-based code-point column, matching {@code AbstractCheck.log}'s {@code getLineNo()} and
	 * {@code getColumnNo()}), together with the nodes the rewrite needs.
	 *
	 * <p>{@code lines} is the buffer to resolve spans against, which for a fixer is the live one an
	 * earlier fix in the same pass may already have edited. Every refusal {@link #visitToken} applies is
	 * re-applied here, the budget gate included, because that gate reads the buffer: a fixer that
	 * answered where the check would now refuse would emit the over-long line the check declined to ask for.
	 */
	@CheckReturnValue
	@Nullable
	public static DirectBooleanTarget locateAt(
			@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column
	) {
		final var node = AstQuery.findNodeAt(
				root,
				line,
				column,
				n -> n.getType() == TokenTypes.LITERAL_IF && targetFor(n, lines) != null
		);
		return node == null ? null : targetFor(node, lines);
	}

	@CheckReturnValue
	@Nullable
	private static TextSpan regionOf(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var start = AstSpan.spanStart(lines, node);
		final var end = AstSpan.spanEnd(lines, node);
		return start == null || end == null ? null : new TextSpan(start, end);
	}

	@CheckReturnValue
	@Nullable
	private static Rendering renderCombine(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var conditionRegion = conditionRegion(lines, target.ifAst());
		final var condition = spanCodeOf(lines, conditionRegion);
		final var valueExpr = (target.thenLiteral() == null ? target.thenReturn() : target.pairedReturn())
				.findFirstToken(TokenTypes.EXPR);
		final var valueRegion = valueExpr == null ? null : regionOf(lines, valueExpr);
		final var value = spanCodeOf(lines, valueRegion);
		if (conditionRegion == null || condition == null || condition.isBlank()
				|| valueRegion == null || value == null || value.isBlank()
				|| foldsAClassBody(conditionRegion, target.conditionExpr())
				|| foldsAClassBody(valueRegion, valueExpr))
			return null;
		final var literalTrue = Boolean.TRUE.equals(target.thenLiteral() == null
				? target.pairedLiteral()
				: target.thenLiteral()
		);
		final String joined;
		if (target.thenLiteral() == null) {
			joined = literalTrue
					? combineOr(simplifyNegation(condition), value)
					: combineAnd(condition, value);
		}
		else {
			joined = literalTrue
					? combineOr(condition, value)
					: combineAnd(simplifyNegation(condition), value);
		}
		return new Rendering(List.of("return " + joined + ";"), List.of(conditionRegion, valueRegion));
	}

	@CheckReturnValue
	@Nullable
	private static Rendering renderOpposite(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var region = conditionRegion(lines, target.ifAst());
		final var condition = spanCodeOf(lines, region);
		if (region == null || condition == null || condition.isBlank()
				|| foldsAClassBody(region, target.conditionExpr()))
			return null;
		final var text = Boolean.TRUE.equals(target.thenLiteral()) ? condition : simplifyNegation(condition);
		return new Rendering(List.of("return " + text + ";"), List.of(region));
	}

	@CheckReturnValue
	@Nullable
	private static Rendering renderRedundant(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var literal = Boolean.TRUE.equals(target.thenLiteral()) ? "return true;" : "return false;";
		// the condition is dropped whole, so nothing of it is re-emitted and every comment in it relocates
		if (AstQuery.isSideEffectFree(target.conditionExpr()))
			return new Rendering(List.of(literal), List.of());
		final var hoist = sameLiteralHoist(target.conditionExpr());
		if (hoist == null)
			return null;
		final var effectRegion = regionOf(lines, hoist.effect());
		final var effect = hoistedStatement(lines, hoist.effect());
		if (effectRegion == null || effect == null || foldsAClassBody(effectRegion, hoist.effect()))
			return null;
		if (hoist.guard() == null)
			return new Rendering(List.of(effect, literal), List.of(effectRegion));
		final var guardRegion = regionOf(lines, hoist.guard());
		final var guard = spanCodeOf(lines, guardRegion);
		if (guardRegion == null || guard == null || guard.isBlank()
				|| foldsAClassBody(guardRegion, hoist.guard()))
			return null;
		return new Rendering(
				List.of("if (" + guard + ")", "\t" + effect, literal),
				List.of(guardRegion, effectRegion)
		);
	}

	/**
	 * The boolean literal value of a {@code return true;}/{@code return false;} (unwrapping redundant
	 * parentheses, so {@code return (true);} counts), or {@code null} when the return value is a
	 * non-literal expression. Assumes {@code stmt} is a valued return (see {@link #isValuedReturn}).
	 */
	@CheckReturnValue
	@Nullable
	private static Boolean returnLiteralValue(@Nonnull DetailAST stmt) {
		final var expr = stmt.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return null;
		final var value = AstQuery.unwrapParensAndExpr(expr);
		if (value == null)
			return null;
		return switch (value.getType()) {
			case TokenTypes.LITERAL_FALSE -> Boolean.FALSE;
			case TokenTypes.LITERAL_TRUE -> Boolean.TRUE;
			default -> null;
		};
	}

	/**
	 * The side effect a same-literal condition can be collapsed around, or null when it cannot. Bounded
	 * to three shapes: the whole condition is a statement-expression ({@code compute()},
	 * {@code flag = other}, {@code ++i}); a comparison wrapping one statement-expression
	 * ({@code ++i > 0} -> {@code ++i;}); or {@code pure && sideEffect()}, where the effect keeps its
	 * guard ({@code if (pure) sideEffect();}). Anything else is left alone. A leading cast is
	 * transparent: the operand it wraps is classified instead ({@code (boolean) box()} -> {@code box()}),
	 * so the cast is dropped and its side effect hoisted, discarding a pure cast's
	 * {@code ClassCastException} risk.
	 */
	@CheckReturnValue
	@Nullable
	private static SideEffectHoist sameLiteralHoist(@Nonnull DetailAST conditionExpr) {
		var top = AstQuery.unwrapParensAndExpr(conditionExpr);
		while (top != null && top.getType() == TokenTypes.TYPECAST) {
			final var rparen = top.findFirstToken(TokenTypes.RPAREN);
			top = rparen == null ? null : AstQuery.unwrapParensAndExpr(rparen.getNextSibling());
		}
		if (top == null)
			return null;
		if (isStatementExpr(top))
			return new SideEffectHoist(null, top);
		final var left = AstQuery.unwrapParensAndExpr(top.getFirstChild());
		final var right = AstQuery.unwrapParensAndExprFromEnd(top.getLastChild());
		if (left == null || right == null)
			return null;
		return switch (top.getType()) {
			case TokenTypes.EQUAL, TokenTypes.GE, TokenTypes.GT, TokenTypes.LE, TokenTypes.LT, TokenTypes.NOT_EQUAL -> {
				if (isStatementExpr(left) && AstQuery.isSideEffectFree(right))
					yield new SideEffectHoist(null, left);
				yield isStatementExpr(right) && AstQuery.isSideEffectFree(left)
						? new SideEffectHoist(null, right)
						: null;
			}
			// the right operand runs only when the left is truthy, so it keeps that guard; a pure right
			// operand contributes nothing observable and only the left's effect survives
			case TokenTypes.LAND -> {
				if (isStatementExpr(right))
					yield new SideEffectHoist(left, right);
				yield isStatementExpr(left) && AstQuery.isSideEffectFree(right)
						? new SideEffectHoist(null, left)
						: null;
			}
			default -> null;
		};
	}

	@CheckReturnValue
	@Nonnull
	private static String simplifyNegation(@Nonnull String cond) {
		final var stripped = cond.strip();
		if (stripped.startsWith("!")) {
			final var rest = stripped.substring(1).strip();
			if (!rest.isEmpty() && isAtomicCond(rest))
				return rest;
		}
		if (isAtomicCond(stripped))
			return "!" + stripped;
		return "!(" + stripped + ")";
	}

	/**
	 * The source of {@code region} collapsed onto one line, with any {@code //} comment dropped (the
	 * caller relocates those). A block comment is carried along inline, its own line breaks collapsed
	 * with the code's. Null when the region crosses a text block, whose significant whitespace a
	 * one-line collapse would destroy.
	 */
	@CheckReturnValue
	@Nullable
	private static String spanCodeOf(@Nonnull List<String> lines, @Nullable TextSpan region) {
		if (region == null || region.start().line() > region.end().line()
				|| (region.start().line() == region.end().line() && region.start().index() > region.end().index()))
			return null;
		final var parts = new ArrayList<String>();
		var state = LexerState.NONE;
		for (var i = region.start().line(); i <= region.end().line(); ++i) {
			if (state.inTextBlock())
				return null;
			final var line = lines.get(i);
			final var from = i == region.start().line() ? region.start().index() : 0;
			final var to = i == region.end().line() ? region.end().index() : line.length();
			final var text = line.substring(from, to);
			final var lineComment = JavaLineScanner.firstLineComment(text, state);
			parts.add(lineComment < 0 ? text : text.substring(0, lineComment));
			state = JavaLineScanner.stateAfter(text, state);
		}
		return SpanReformat.collapse(parts);
	}

	/**
	 * The source span the collapse replaces: from the {@code if} keyword to just past the paired return,
	 * which for an {@code else} form is the end of the {@code if} statement itself.
	 */
	@CheckReturnValue
	@Nullable
	public static TextSpan spanOf(@Nonnull List<String> lines, @Nonnull DirectBooleanTarget target) {
		final var start = AstSpan.spanStart(lines, target.ifAst());
		final var elseAst = target.ifAst().findFirstToken(TokenTypes.LITERAL_ELSE);
		final var end = AstSpan.spanEnd(lines, elseAst != null ? target.ifAst() : target.pairedReturn());
		if (start == null || end == null)
			return null;
		return AstSpan.extendedOverTrailingComment(lines, start, end);
	}

	/**
	 * The collapse {@code ast} qualifies for, or null when it qualifies for none. {@code lines} is the
	 * buffer its spans resolve against, which the line-length gate measures.
	 */
	@CheckReturnValue
	@Nullable
	public static DirectBooleanTarget targetFor(@Nonnull DetailAST ast, @Nonnull List<String> lines) {
		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return null;

		final var thenStmt = AstQuery.unwrapSingleStatementBlock(thenBody);
		if (thenStmt == null || !isValuedReturn(thenStmt))
			return null;

		final DetailAST elseStmt;
		final var elseAst = ast.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return null;
			elseStmt = AstQuery.unwrapSingleStatementBlock(elseBody);
		}
		else
			elseStmt = ast.getNextSibling();
		if (elseStmt == null || !isValuedReturn(elseStmt))
			return null;

		final var conditionExpr = ast.findFirstToken(TokenTypes.EXPR);
		if (conditionExpr == null)
			return null;
		final var thenValue = returnLiteralValue(thenStmt);
		final var elseValue = returnLiteralValue(elseStmt);

		if (thenValue == null && elseValue == null)
			return null;

		final DirectBooleanRule rule;
		if (thenValue == null || elseValue == null) {
			final var valueExpr = (thenValue == null ? thenStmt : elseStmt).findFirstToken(TokenTypes.EXPR);
			if (valueExpr == null || AstQuery.lastLine(valueExpr) > AstQuery.firstLine(valueExpr))
				return null;
			final var literalTrue = thenValue == null ? Boolean.TRUE.equals(elseValue) : thenValue;
			final var ands = countType(conditionExpr, TokenTypes.LAND)
					+ countType(valueExpr, TokenTypes.LAND) + (literalTrue ? 0 : 1);
			final var ors = countType(conditionExpr, TokenTypes.LOR)
					+ countType(valueExpr, TokenTypes.LOR) + (literalTrue ? 1 : 0);
			if ((ands > 0 && ors > 0) || ands + ors >= 3)
				return null;
			rule = DirectBooleanRule.COMBINE;
		}
		else if (thenValue.equals(elseValue)) {
			if (!AstQuery.isSideEffectFree(conditionExpr) && sameLiteralHoist(conditionExpr) == null)
				return null;
			rule = DirectBooleanRule.REDUNDANT;
		}
		else
			rule = DirectBooleanRule.OPPOSITE;

		final var target = new DirectBooleanTarget(
				rule, ast, conditionExpr, thenStmt, elseStmt, thenValue, elseValue
		);
		return fitsLineBudget(lines, target) ? target : null;
	}

	@CheckReturnValue
	@Nonnull
	private static String wrapForAnd(@Nonnull String expr) {
		return hasLooseDepth0Op(expr, true) ? "(" + expr + ")" : expr;
	}

	@CheckReturnValue
	@Nonnull
	private static String wrapForOr(@Nonnull String expr) {
		return hasLooseDepth0Op(expr, false) ? "(" + expr + ")" : expr;
	}

	@Nonnull
	private List<String> sourceLines = List.of();

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		sourceLines = List.of(getLines());
	}

	@Override
	public void finishTree(@Nonnull DetailAST rootAST) {
		// a TreeWalker instance outlives the file, so holding the buffer here would pin every line of
		// the last file checked for as long as the check lives
		sourceLines = List.of();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast, sourceLines);
		if (target != null)
			log(ast, target.rule().messageKey());
	}
}