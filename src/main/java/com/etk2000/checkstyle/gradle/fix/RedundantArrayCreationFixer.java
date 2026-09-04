package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.AstSpan.TextSpan;
import com.etk2000.checkstyle.RedundantArrayCreationCheck;
import com.etk2000.checkstyle.RedundantArrayCreationCheck.ArrayArgTarget;
import com.etk2000.checkstyle.SpanComments;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Unwraps a varargs argument written as an explicit array creation, leaving the initializer's
 * elements as the arguments.
 *
 * <p>Every boundary comes from a node position: the {@code new}, both braces, the initializer's
 * trailing comma, and the argument-list comma in front of an empty initializer. Reading those off
 * the line text instead made the trailing comma a {@code endsWith(",")} test on raw source, which
 * a comment written after that comma defeated, and the dropped-argument branch a test for empty
 * raw text, which a comment inside the braces defeated. Both emitted a dangling comma.
 */
class RedundantArrayCreationFixer implements CheckstyleFixer {
	/**
	 * {@code body} placed where the deleted argument stood. Deleting an empty initializer takes
	 * its separator with it, so what stands in front is the preceding argument rather than the
	 * {@code (} opening the list, and a comment re-emitted against it needs a space of its own.
	 */
	@CheckReturnValue
	@Nonnull
	private static String againstPrecedingArgument(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull String body
	) {
		if (body.isEmpty())
			return body;

		final var prefix = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		).prefix();
		return prefix.isEmpty() || prefix.endsWith("(") || Character.isWhitespace(prefix.charAt(prefix.length() - 1))
				? body
				: ' ' + body;
	}

	/**
	 * The edit for an initializer holding no elements, which drops the whole argument along with
	 * the comma separating it from the one before. Nothing but comments can be written inside such
	 * braces, and they are re-emitted where the argument stood.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt emptyInitializerFix(
			@Nonnull List<String> lines,
			@Nonnull ArrayArgTarget target,
			@Nonnull TextPos newStart,
			@Nonnull TextPos end
	) {
		final var precedingComma = target.literalNew().getParent().getPreviousSibling();
		final var start = precedingComma == null || precedingComma.getType() != TokenTypes.COMMA
				? newStart
				: AstSpan.tokenStart(lines, precedingComma);
		if (start == null || AstSpan.outOfBounds(lines, start, end))
			return null;

		final var span = new TextSpan(start, end);
		final var body = String.join(" ", SpanComments.inside(lines, span, List.of()));
		final var fix = SpanRewrite.replacing(lines, span, againstPrecedingArgument(lines, span, body));
		if (fix == null)
			return refusal(lines, span);
		return lengthensPastBudget(lines, span, fix)
				? new SkipResult(SkipMessages.REDUNDANT_ARRAY_CREATION_SKIP_LINE_BUDGET)
				: fix;
	}

	/**
	 * Whether {@code fix} pushes a line past the budget that was not already past it. A span whose
	 * own source is already over the limit cannot be brought under it by this fixer, and refusing
	 * those would keep the creation for good.
	 */
	@CheckReturnValue
	private static boolean lengthensPastBudget(
			@Nonnull List<String> lines, @Nonnull TextSpan span, @Nonnull FixResult fix
	) {
		if (fix.replacement().stream().noneMatch(RedundantArrayCreationFixer::overBudget))
			return false;

		for (var line = span.start().line(); line <= span.end().line(); ++line) {
			if (overBudget(lines.get(line)))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean overBudget(@Nonnull String line) {
		return LineLength.tabExpandedLength(line) > LineLength.MAX_LINE_LENGTH;
	}

	/**
	 * Which refusal a span that will not fold onto one line earns. A comment inside it would
	 * comment out everything the fold appended after it; a text block's opening delimiter has to
	 * be the last thing on its line, so folding deletes the line break the block depends on.
	 */
	@CheckReturnValue
	@Nonnull
	private static SkipResult refusal(@Nonnull List<String> lines, @Nonnull TextSpan span) {
		return new SkipResult(SpanComments.inside(lines, span, List.of()).isEmpty()
				? SkipMessages.REDUNDANT_ARRAY_CREATION_SKIP_TEXT_BLOCK
				: SkipMessages.REDUNDANT_ARRAY_CREATION_SKIP_COMMENT
		);
	}

	/**
	 * The edit replacing {@code target}'s array creation with its elements, or the reason there is
	 * none.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt rewrite(@Nonnull List<String> lines, @Nonnull ArrayArgTarget target) {
		final var arrayInit = target.arrayInit();
		// a nested initializer is only legal inside an array creation, so spliced into an argument
		// list it would not compile
		if (arrayInit.findFirstToken(TokenTypes.ARRAY_INIT) != null)
			return new SkipResult(SkipMessages.REDUNDANT_ARRAY_CREATION_SKIP_NESTED_INITIALIZER);

		final var rcurly = arrayInit.findFirstToken(TokenTypes.RCURLY);
		final var newStart = AstSpan.spanStart(lines, target.literalNew());
		final var innerStart = AstSpan.tokenEnd(lines, arrayInit);
		final var innerEnd = rcurly == null ? null : AstSpan.tokenStart(lines, rcurly);
		final var end = rcurly == null ? null : AstSpan.tokenEnd(lines, rcurly);
		if (newStart == null || innerStart == null || innerEnd == null || end == null)
			return null;

		// the closing brace being the only child is what makes the initializer empty. Testing for an
		// EXPR child instead reads `{() -> {}}` as empty, because a lambda element is a bare LAMBDA
		// node with no EXPR wrapping it, and deletes the arguments
		if (arrayInit.getFirstChild() == rcurly)
			return emptyInitializerFix(lines, target, newStart, end);

		final var trailingComma = trailingCommaOf(arrayInit);
		final var elementsEnd = trailingComma == null ? innerEnd : AstSpan.tokenStart(lines, trailingComma);
		final var afterComma = trailingComma == null ? innerEnd : AstSpan.tokenEnd(lines, trailingComma);
		if (elementsEnd == null || afterComma == null || AstSpan.outOfBounds(lines, newStart, end))
			return null;

		final var span = new TextSpan(newStart, end);
		final var elements = AstSpan.sliceSpan(lines, innerStart, elementsEnd);
		if (elements == null)
			return refusal(lines, span);

		// the head holds whatever was written between `new` and `{`, the tail whatever followed a
		// trailing comma: both are deleted, so any comment in them is re-emitted around the elements
		final var carried = SpanCarry.carried(lines, new TextSpan(newStart, innerStart), List.of());
		final var trailing = SpanComments.inside(lines, new TextSpan(afterComma, innerEnd), List.of());
		final var body = carried
				+ elements.strip()
				+ (trailing.isEmpty() ? "" : ' ' + String.join(" ", trailing));
		final var fix = SpanRewrite.replacing(lines, span, SpanCarry.separated(lines, span, body));
		if (fix == null)
			return refusal(lines, span);
		return lengthensPastBudget(lines, span, fix)
				? new SkipResult(SkipMessages.REDUNDANT_ARRAY_CREATION_SKIP_LINE_BUDGET)
				: fix;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST trailingCommaOf(@Nonnull DetailAST arrayInit) {
		final var rcurly = arrayInit.findFirstToken(TokenTypes.RCURLY);
		final var previous = rcurly == null ? null : rcurly.getPreviousSibling();
		return previous != null && previous.getType() == TokenTypes.COMMA ? previous : null;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.locate(lines, lineIndex, column, RedundantArrayCreationCheck::redundantArrayAt);
		return target == null ? null : rewrite(lines, target);
	}
}