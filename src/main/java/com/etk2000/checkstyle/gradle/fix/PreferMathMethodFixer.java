package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.PreferMathMethodCheck;
import com.etk2000.checkstyle.PreferMathMethodCheck.IfTarget;
import com.etk2000.checkstyle.PreferMathMethodCheck.MathTarget;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferMathMethodFixer implements CheckstyleFixer {
	/**
	 * One rewrite's emitted text, and the source regions it copies verbatim. The regions are what
	 * tells a comment that travels with copied text apart from one the rewrite would otherwise drop.
	 */
	private record Rendering(@Nonnull String text, @Nonnull List<TextSpan> kept) {}

	/**
	 * The token an argument's region runs up to: its own separating comma, or the closing paren of
	 * the call it belongs to when it is the last argument. Null when the argument sits somewhere
	 * that has no such delimiter, which is every operand of a ternary condition.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST argumentDelimiter(@Nonnull DetailAST argument) {
		final var next = argument.getNextSibling();
		// a non-comma sibling between two arguments is not the last-argument shape, and widening to
		// the RPAREN would swallow every later argument into this one's region. Reachable only under
		// a WITH_COMMENTS parse, where a comment node lands between an argument and its comma
		if (next != null)
			return next.getType() == TokenTypes.COMMA ? next : null;
		final var elist = argument.getParent();
		if (elist == null || elist.getType() != TokenTypes.ELIST)
			return null;
		final var close = elist.getNextSibling();
		return close != null && close.getType() == TokenTypes.RPAREN ? close : null;
	}

	/**
	 * The source region re-emitted for one argument: its own span, widened to the delimiter that
	 * ends it so a comment written between the two travels with the argument instead of being
	 * dropped. A block comment sitting between an argument and its comma is the case that needs it;
	 * the {@code clamp_comment_paren_in_arg} fixture pins it.
	 */
	@CheckReturnValue
	@Nullable
	private static TextSpan argumentRegion(@Nonnull List<String> lines, @Nonnull DetailAST argument) {
		final var start = AstSpan.spanStart(lines, argument);
		var end = AstSpan.spanEnd(lines, argument);
		if (start == null || end == null)
			return null;

		final var delimiter = argumentDelimiter(argument);
		if (delimiter != null) {
			final var at = AstSpan.tokenStart(lines, delimiter);
			if (at != null && !AstSpan.outOfBounds(lines, end, at))
				end = at;
		}
		return new TextSpan(start, end);
	}

	/**
	 * Whether a comment falls in {@code [from, to)}, text the rewrite discards rather than
	 * relocates. A literal there is not refused: a ternary's discarded tail holds its own branches,
	 * and those are written again as the call's arguments, so {@code c > 'a' ? c : 'a'} drops a
	 * copy of {@code 'a'} and loses nothing.
	 */
	@CheckReturnValue
	private static boolean carriesComment(@Nonnull List<String> lines, @Nonnull TextPos from, @Nonnull TextPos to) {
		if (AstSpan.outOfBounds(lines, from, to))
			return false;
		// a gap running across lines is handed to sliceSpan, which already refuses a comment inside
		// one; doing it here would have to thread the lexer state over the same lines again
		if (from.line() != to.line())
			return AstSpan.sliceSpan(lines, from, to) == null;
		// the gap begins just past a code token, so the state at its start is NONE
		final var text = lines.get(from.line()).substring(from.index(), to.index());
		return JavaLineScanner.firstCommentMarker(text, LexerState.NONE) >= 0;
	}

	/**
	 * The {@code return <name>;} written directly after {@code ifAst}, or null when the statement
	 * after it is something else. The rewrite folds such a return into itself, since the value it
	 * returns is the one the if-else had just finished assigning.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST collapseReturn(@Nonnull DetailAST ifAst, @Nonnull String name) {
		final var next = ifAst.getNextSibling();
		if (next == null || next.getType() != TokenTypes.LITERAL_RETURN)
			return null;
		final var expr = next.findFirstToken(TokenTypes.EXPR);
		final var value = expr == null ? null : expr.getFirstChild();
		return value != null && value.getType() == TokenTypes.IDENT && name.equals(value.getText())
				? next
				: null;
	}

	/**
	 * Whether rewriting {@code span} down to {@code regions} would discard a comment. The rewrite
	 * keeps only the argument regions and writes fresh punctuation around them, so every gap between
	 * them, and the two ends, are dropped. A comment in any of them means refusing the fix, since
	 * there is nowhere left to put it.
	 */
	@CheckReturnValue
	private static boolean dropsComment(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull List<TextSpan> regions
	) {
		final var ordered = new ArrayList<>(regions);
		ordered.sort(
				Comparator.<TextSpan>comparingInt(region -> region.start().line())
						.thenComparingInt(region -> region.start().index())
		);

		var cursor = span.start();
		for (var region : ordered) {
			// a region starting behind the cursor means the regions overlap. carriesComment answers
			// false for a backwards pair, so falling through would drop a comment written there
			if (AstSpan.outOfBounds(lines, cursor, region.start())
					|| carriesComment(lines, cursor, region.start()))
				return true;
			cursor = region.end();
		}
		return AstSpan.outOfBounds(lines, cursor, span.end())
				|| carriesComment(lines, cursor, span.end());
	}

	/**
	 * The edit writing {@code rendering} over {@code span}, or null when it cannot be written there.
	 *
	 * <p>Everything the span held is discarded, so every comment inside it that does not travel with
	 * a copied region is merged into one comment trailing the statement. Only an {@code else} may
	 * precede the span on its own line, and it keeps that line with the statement indented under it;
	 * anything else there (a label, an enclosing one-line {@code if}) would end up cuddled with a
	 * statement this fixer wrote. A line over the project's budget is refused rather than emitted,
	 * since nothing downstream wraps it and the fold is what made it long.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult emitStatement(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull Rendering rendering
	) {
		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var head = edges.prefix().strip();
		if (!head.isEmpty() && !"else".equals(head) && !"} else".equals(head))
			return null;

		final var relocated = SpanComments.relocate(lines, span, rendering.kept());
		final var comment = edges.appendableComment(relocated);
		// the suffix opens a literal nothing may follow, so the comment the fold displaced has
		// nowhere to go; dropping it is the one way this rewrite could lose text silently
		if (comment.isEmpty() && !relocated.isEmpty())
			return null;
		final var body = rendering.text() + edges.suffix() + (comment.isEmpty() ? "" : ' ' + comment);
		final var replacement = head.isEmpty()
				? List.of(edges.prefix() + body)
				: List.of(edges.prefix().stripTrailing(), edges.indent() + '\t' + body);
		for (var line : replacement) {
			if (LineLength.tabExpandedLength(line) > LineLength.MAX_LINE_LENGTH)
				return null;
		}
		return new FixResult(span.start().line(), span.end().line(), replacement);
	}

	/**
	 * The edit replacing the {@code Math} construct the check classified at the violation's
	 * position, or null when there is none there or it cannot be rewritten without losing text.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixExpression(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var target = FixerAst.locate(lines, lineIndex, charColumn, PreferMathMethodCheck::mathTargetAt);
		if (target == null)
			return null;
		// Math.abs and the ternary it replaces disagree on -0.0, and the check's zero test accepts
		// every numeric spelling. Rewriting only an integral zero keeps the unsound case reported
		// but unapplied; docs/todo/float-nan-math-methods.md carries the rest of the argument
		if ("abs".equals(target.method()) && !isIntegralZero(target.zeroLiteral()))
			return new SkipResult(SkipMessages.MATH_METHOD_SKIP_FLOAT_ZERO);

		final var span = nodeSpan(lines, target.node());
		if (span == null)
			return null;

		final var call = mathCall(lines, target);
		if (call == null || dropsComment(lines, span, call.kept()))
			return null;

		return SpanRewrite.replacing(lines, span, call.text());
	}

	/**
	 * The edit replacing the if-else statement the check classified at the violation's position, or
	 * null when nothing it classifies sits there. Once something does, this owns the outcome: no
	 * other shape is reported at an {@code if}, so a refusal is a skip rather than a fall-through.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixIfShape(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var target = FixerAst.locate(lines, lineIndex, charColumn, PreferMathMethodCheck::ifTargetAt);
		if (target == null)
			return null;
		// the same -0.0 divergence the expression path refuses, narrowed against the same wider zero
		// test on the check; docs/todo/float-nan-math-methods.md carries the argument
		if ("abs".equals(target.math().method()) && !isIntegralZero(target.math().zeroLiteral()))
			return new SkipResult(SkipMessages.MATH_METHOD_SKIP_FLOAT_ZERO);

		final var result = rewriteIfShape(lines, target);
		return result != null ? result : new SkipResult(SkipMessages.MATH_METHOD_SKIP_IF);
	}

	/**
	 * The statement replacing the if-else, and the regions of the source it copies into it.
	 * {@code collapsed} says the rewrite has absorbed both the declaration above and the return
	 * below, so the assignment it was built around disappears into that return.
	 */
	@CheckReturnValue
	@Nullable
	private static Rendering ifStatement(
			@Nonnull List<String> lines,
			@Nonnull IfTarget target,
			@Nullable DetailAST declaration,
			boolean collapsed,
			@Nonnull Rendering call
	) {
		final var assign = target.assign();
		if (assign == null || collapsed)
			return new Rendering("return " + call.text() + ";", call.kept());

		final var kept = new ArrayList<>(call.kept());
		if (declaration != null) {
			final var ident = declaration.findFirstToken(TokenTypes.IDENT);
			final var start = AstSpan.spanStart(lines, declaration);
			final var end = ident == null ? null : AstSpan.tokenEnd(lines, ident);
			final var head = AstSpan.sliceSpan(lines, start, end);
			if (head == null || head.isBlank())
				return null;
			kept.add(new TextSpan(start, end));
			return new Rendering(head.strip() + " = " + call.text() + ";", kept);
		}

		final var lvalue = assign.getFirstChild();
		final var region = lvalue == null ? null : nodeSpan(lines, lvalue);
		final var text = region == null ? null : AstSpan.sliceSpan(lines, region.start(), region.end());
		if (text == null || text.isBlank())
			return null;
		kept.add(region);
		return new Rendering(text.strip() + ' ' + assign.getText() + ' ' + call.text() + ";", kept);
	}

	/**
	 * Whether {@code ifAst} stands in an old-style {@code switch} label's block. Such a block is not
	 * a scope of its own: a local declared under one label is in scope for the whole switch body, so
	 * a later label can still name one that the {@code return} below this statement makes
	 * unreachable here. The collapse's premise, that such a declaration is dead, does not hold there.
	 */
	@CheckReturnValue
	private static boolean isCaseScoped(@Nonnull DetailAST ifAst) {
		final var block = ifAst.getParent();
		if (block == null)
			return false;
		return block.getType() == TokenTypes.CASE_GROUP
				|| block.getParent() != null && block.getParent().getType() == TokenTypes.CASE_GROUP;
	}

	@CheckReturnValue
	private static boolean isIntegralZero(@Nullable DetailAST zeroLiteral) {
		return zeroLiteral != null
				&& (zeroLiteral.getType() == TokenTypes.NUM_INT || zeroLiteral.getType() == TokenTypes.NUM_LONG);
	}

	/**
	 * Whether {@code def} is the only declarator of its statement. A multi-declarator statement puts
	 * every name under its own {@code VARIABLE_DEF}, all of them sharing the one type's position, so
	 * the span of any but the first starts at text the others own and rewriting it would delete them.
	 */
	@CheckReturnValue
	private static boolean isSoleDeclarator(@Nonnull DetailAST def) {
		final var previous = def.getPreviousSibling();
		final var next = def.getNextSibling();
		return (previous == null || previous.getType() != TokenTypes.COMMA)
				&& (next == null || next.getType() != TokenTypes.COMMA);
	}

	/** The {@code Math} call {@code target} describes, written from the source its arguments came from. */
	@CheckReturnValue
	@Nullable
	private static Rendering mathCall(@Nonnull List<String> lines, @Nonnull MathTarget target) {
		final var arguments = target.arguments();
		// the arity invariant lives in the check's classifier, which the fixer consumes across a
		// class boundary, so a later edit there would write an uncompilable call to disk rather than
		// skip the fix
		final var arity = switch (target.method()) {
			case "abs" -> 1;
			case "clamp" -> 3;
			default -> 2;
		};
		if (arguments.size() != arity)
			return null;

		final var kept = new ArrayList<TextSpan>(arguments.size());
		final var texts = new ArrayList<String>(arguments.size());
		for (var argument : arguments) {
			final var region = argumentRegion(lines, argument);
			if (region == null)
				return null;
			final var text = AstSpan.sliceSpan(lines, region.start(), region.end());
			if (text == null || text.isBlank())
				return null;
			kept.add(region);
			texts.add(text.strip());
		}
		return new Rendering("Math." + target.method() + "(" + String.join(", ", texts) + ")", kept);
	}

	/** The span {@code node}'s subtree occupies, or null when either end does not resolve. */
	@CheckReturnValue
	@Nullable
	private static TextSpan nodeSpan(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var start = AstSpan.spanStart(lines, node);
		final var end = AstSpan.spanEnd(lines, node);
		return start == null || end == null ? null : new TextSpan(start, end);
	}

	/**
	 * The uninitialized declaration of {@code name} written directly above {@code ifAst}, or null
	 * when the statement before it is something else. A declaration the if-else goes on to assign in
	 * every branch is folded into the rewrite rather than left behind holding nothing.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST precedingDeclaration(@Nonnull DetailAST ifAst, @Nonnull String name) {
		final var parent = ifAst.getParent();
		if (parent == null)
			return null;
		DetailAST previous = null;
		for (var child = parent.getFirstChild(); child != null && child != ifAst; child = child.getNextSibling()) {
			// a statement's own SEMI is a sibling of it, not a child
			if (child.getType() != TokenTypes.SEMI)
				previous = child;
		}
		if (previous == null || previous.getType() != TokenTypes.VARIABLE_DEF
				|| previous.findFirstToken(TokenTypes.ASSIGN) != null)
			return null;
		final var ident = previous.findFirstToken(TokenTypes.IDENT);
		return ident != null && name.equals(ident.getText()) ? previous : null;
	}

	/**
	 * The edit replacing the whole if-else statement, or null when it cannot be written as one.
	 *
	 * <p>The extent is the {@code if} itself, widened over whichever neighbours the rewrite consumes:
	 * the declaration whose initializer stood in for the missing {@code else}, the {@code return}
	 * that did, and the declaration/return pair a plain assignment collapses into a {@code return}.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult rewriteIfShape(@Nonnull List<String> lines, @Nonnull IfTarget target) {
		final var ifAst = target.math().node();
		final var assign = target.assign();
		final var lvalue = assign == null ? null : assign.getFirstChild();

		var head = target.declaration();
		var tail = target.trailingReturn();
		var collapsed = false;
		// only a bare name can be both declared above and returned below; a field or an array
		// element is assigned through something the declaration does not name
		if (lvalue != null && lvalue.getType() == TokenTypes.IDENT && !isCaseScoped(ifAst)) {
			final var returned = collapseReturn(ifAst, lvalue.getText());
			final var declaration = head != null ? head : precedingDeclaration(ifAst, lvalue.getText());
			if (returned != null && declaration != null) {
				head = declaration;
				tail = returned;
				collapsed = true;
			}
		}
		if (head != null && !isSoleDeclarator(head))
			return null;

		final var start = AstSpan.spanStart(lines, head != null ? head : ifAst);
		final var end = AstSpan.spanEnd(lines, tail != null ? tail : ifAst);
		if (AstSpan.outOfBounds(lines, start, end))
			return null;
		final var span = AstSpan.extendedOverTrailingComment(lines, start, end);
		if (SpanRewrite.leavesALiteralOpen(lines, span.start(), span.end()))
			return null;

		final var call = mathCall(lines, target.math());
		if (call == null)
			return null;
		final var rendering = ifStatement(lines, target, head, collapsed, call);
		return rendering == null ? null : emitStatement(lines, span, rendering);
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		// The if-else form is reported under its own key at the `if`, which the expression
		// classifier does not recognise, so the two shapes cannot answer for each other.
		if (!PreferMathMethodCheck.MSG_METHOD_IF.equals(FixContext.getViolationKey())) {
			final var expressionResult = fixExpression(lines, lineIndex, charColumn);
			if (expressionResult != null)
				return expressionResult;
		}

		final var ifShapeResult = fixIfShape(lines, lineIndex, charColumn);
		if (ifShapeResult != null)
			return ifShapeResult;

		return new SkipResult(SkipMessages.MATH_METHOD_SKIP);
	}
}