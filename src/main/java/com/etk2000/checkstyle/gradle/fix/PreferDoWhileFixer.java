package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.PreferDoWhileCheck;
import com.etk2000.checkstyle.PreferDoWhileCheck.DoWhileTarget;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.ast.AstIndent;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collapses a {@code while} loop and the statement it duplicates into a {@code do-while}, deleting
 * the duplicate.
 */
class PreferDoWhileFixer implements CheckstyleFixer {
	/** The tier {@link ControlFlowBracesCheck#determineTier} returns for a body that stays on the {@code do} line. */
	private static final int TIER_2 = 2;

	/**
	 * The emitted replacement, or null when a line would exceed the project's budget. Anything ahead
	 * of the span on its first line (a {@code case} label, an earlier statement) keeps that line and
	 * the do-while starts below it, since the AST-derived indent places the statement correctly
	 * whether or not something precedes it.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> emitted(
			@Nonnull SpanEdges edges, @Nonnull String indent, @Nonnull List<String> statement
	) {
		final var lines = new ArrayList<String>(statement.size() + 1);
		final var head = edges.prefix().stripTrailing();
		if (head.isBlank())
			lines.add(indent + statement.getFirst().stripLeading());
		else {
			lines.add(head);
			lines.add(statement.getFirst());
		}
		lines.addAll(statement.subList(1, statement.size()));
		// `head` is source the fold merely shortened, so refusing on its length would reject a fix
		// over a line already past the budget
		for (var line : statement) {
			if (LineLength.tabExpandedLength(line) > LineLength.MAX_LINE_LENGTH)
				return null;
		}
		return lines;
	}

	/** The region a copied node's text comes from, so a comment written inside it travels with it. */
	@CheckReturnValue
	@Nullable
	private static TextSpan regionOf(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		final var start = AstSpan.spanStart(lines, node);
		final var end = AstSpan.spanEnd(lines, node);
		return start == null || end == null ? null : new TextSpan(start, end);
	}

	/**
	 * The edit collapsing {@code target} into a do-while, or a skip when the fold would lose text.
	 * The body's terminating semicolon is its sibling rather than its child, braced or not, so it is
	 * picked up explicitly to keep the emitted statement complete.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull DoWhileTarget target) {
		final var semi = target.bodyStatement().getNextSibling();
		if (semi == null || semi.getType() != TokenTypes.SEMI)
			return null;

		final var start = AstSpan.spanStart(lines, target.precedingStatement());
		final var end = AstSpan.spanEnd(lines, target.whileAst());
		if (AstSpan.outOfBounds(lines, start, end))
			return null;
		final var span = AstSpan.extendedOverTrailingComment(lines, start, end);
		if (SpanRewrite.leavesALiteralOpen(lines, span.start(), span.end()))
			return new SkipResult(SkipMessages.PREFER_DO_WHILE_SKIP_UNSLICEABLE);

		final var bodyRegion = regionOf(lines, target.bodyStatement());
		final var conditionRegion = regionOf(lines, target.condition());
		final var bodyEnd = AstSpan.spanEnd(lines, semi);
		if (bodyRegion == null || conditionRegion == null || bodyEnd == null)
			return null;
		// the copy runs past the terminator, so the region it is copied from has to reach there too:
		// a comment between the body's last token and its `;` travels with the copy, and relocating
		// it as well would emit it twice
		final var copiedBody = new TextSpan(bodyRegion.start(), bodyEnd);
		final var body = AstSpan.sliceSpan(lines, copiedBody.start(), copiedBody.end());
		final var condition = AstSpan.sliceSpan(lines, conditionRegion.start(), conditionRegion.end());
		if (body == null || condition == null)
			return new SkipResult(SkipMessages.PREFER_DO_WHILE_SKIP_UNSLICEABLE);

		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var relocated = SpanComments.relocate(lines, span, List.of(conditionRegion, copiedBody));
		final var comment = edges.appendableComment(relocated);
		// the suffix opens a literal nothing may follow, so the comment the fold displaced has
		// nowhere to go; dropping it is the one way this rewrite could lose text silently
		if (comment.isEmpty() && !relocated.isEmpty())
			return new SkipResult(SkipMessages.PREFER_DO_WHILE_SKIP_UNSLICEABLE);

		final var indent = "\t".repeat(AstIndent.tabsFor(target.whileAst()));
		final var tail = indent + "while (" + condition + ");"
				+ edges.suffix() + (comment.isEmpty() ? "" : ' ' + comment);
		final var statement = ControlFlowBracesCheck.determineTier(target.bodyStatement()) == TIER_2
				? List.of(indent + "do " + body, tail)
				: List.of(indent + "do", indent + '\t' + body, tail);

		final var replacement = emitted(edges, indent, statement);
		return replacement == null
				? new SkipResult(SkipMessages.PREFER_DO_WHILE_SKIP_LINE_BUDGET)
				: new FixResult(span.start().line(), span.end().line(), replacement);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, PreferDoWhileCheck::targetAt);
		return target == null ? null : rewrite(lines, target);
	}
}