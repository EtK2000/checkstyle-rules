package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.MultilineCallFormattingCheck;
import com.etk2000.checkstyle.format.JavaArgListReformatter;
import com.etk2000.checkstyle.format.JavaPostDelayedReformatter;
import com.etk2000.checkstyle.format.JavaSpanReindenter;
import com.etk2000.checkstyle.format.JavaTernaryReformatter;
import com.etk2000.checkstyle.format.SpanReformat;
import com.etk2000.checkstyle.format.SpanReformat.CannotReformat;
import com.etk2000.checkstyle.format.SpanReformat.Reformatted;
import com.etk2000.checkstyle.MultilineCallFormattingCheck.ClosingParenMove;
import com.etk2000.checkstyle.MultilineCallFormattingCheck.OpeningParenMove;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixes a subset of {@code MultilineCallFormattingCheck} messages:
 * <ul>
 * <li>{@code multiline.put.collapsible}: joins a split {@code new JSONObject().put(k, v)} (simple
 * value) onto one line (the whole enclosing statement).</li>
 * <li>closing-paren moves ({@code multiline.args.on.closing.paren},
 * {@code multiline.lambda.not.on.closing.paren}, {@code multiline.ternary.not.on.closing.paren}):
 * for a plain call, collapses the whole call onto one line when it fits within
 * {@link LineLength#MAX_LINE_LENGTH}, else pushes the closing {@code )} onto its own line; or pulls it
 * up to stack with an inline block's brace / rejoin a single-line ternary, but only when the closing
 * move is the call's sole violation (otherwise the pipeline could not converge).</li>
 * <li>opening-paren moves ({@code multiline.args.on.opening.paren},
 * {@code multiline.lambda.not.on.opening.paren}, {@code multiline.ternary.not.on.opening.paren}):
 * for a plain call, collapses the whole call onto one line when it fits within
 * {@link LineLength#MAX_LINE_LENGTH}, else splits the first argument off the {@code (} line onto its
 * own line; for an inline-block or ternary argument, pulls the argument up onto the {@code (} line and
 * re-stacks the closing {@code )} (collapsing the whole call to one line when its single-line value
 * fits), so a single whole-span rewrite also resolves any accompanying closing violation.</li>
 * <li>ternary-internal moves ({@code multiline.ternary.question.wrong.line},
 * {@code multiline.ternary.colon.wrong.line}): re-lays-out the whole ternary argument into its
 * canonical shape via {@link JavaTernaryReformatter} (condition on the {@code (} line, {@code ?}- and
 * {@code :}-branches each on their own line, {@code )} on its own line), which also resolves any
 * accompanying opening/closing violation on the same ternary. Skipped only when a {@code //} comment
 * would be swallowed by a collapsed segment, or a branch spans a text block / multi-line comment.</li>
 * <li>shared-line moves ({@code multiline.args.shared.line}): re-lays-out the whole argument/parameter
 * list via {@link JavaArgListReformatter}, collapsed onto one line when the call/def fits within
 * {@link LineLength#MAX_LINE_LENGTH}, otherwise each argument on its own line with the {@code (}/{@code )}
 * on their own lines. Either way this also resolves any accompanying opening/closing violation on the
 * same call. Skipped only for a swallowed comment or a text-block / multi-line-comment argument.</li>
 * </ul>
 * Except for the single-line collapse, every move re-emits the whole call span and re-indents it via
 * {@link JavaSpanReindenter}, so the fixed output follows the project's canonical indentation
 * regardless of how the input was indented. The shape decisions and geometry are delegated to the
 * check's {@code public static} classifiers
 * ({@link MultilineCallFormattingCheck#collapsibleJsonObjectPutLineSpan},
 * {@link MultilineCallFormattingCheck#closingParenMove},
 * {@link MultilineCallFormattingCheck#openingParenMove},
 * {@link MultilineCallFormattingCheck#resolvableTernaryLayoutQuestion},
 * {@link MultilineCallFormattingCheck#resolvableSharedLineArgs}) so the fixer never re-derives a
 * rule from text and can never disagree with the check.
 */
final class MultilineCallFormattingFixer implements CheckstyleFixer {
	/**
	 * Re-lays-out the argument list of {@code owner} via {@link JavaArgListReformatter} (collapsed onto
	 * one line when it fits within {@link LineLength#MAX_LINE_LENGTH}, else each argument on its own
	 * line), mapping a refusal to the matching skip reason.
	 */
	@CheckReturnValue
	@Nonnull
	static FixAttempt applyArgListReformat(@Nonnull List<String> lines, @Nonnull DetailAST owner) {
		return switch (JavaArgListReformatter.reformat(lines, owner, LineLength.MAX_LINE_LENGTH, LineLength.TAB_WIDTH)) {
			case Reformatted r -> new FixResult(r.fromIndex(), r.toIndex(), r.lines());
			case CannotReformat c -> new SkipResult(switch (c.reason()) {
				// COMMENT_ON_JOINED_LINE and SPECIAL_ARG are unreachable from this shared-line entry: the
				// arg-list reformatter lifts a leaked `//` comment onto the previous line rather than bailing,
				// and a special layout config never produces a shared-line violation. Both are defensive maps
				// shared with the reformatter, reachable only via the opening/closing entry (reformatPlainOrPushDown)
				case COMMENT_ON_JOINED_LINE, MULTILINE_LITERAL, SPECIAL_ARG -> SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED;
				case STALE -> SkipMessages.MULTILINE_PUT_SKIP_STALE;
			});
		};
	}

	@CheckReturnValue
	@Nonnull
	private static FixAttempt applyOpeningMove(@Nonnull List<String> lines, @Nonnull OpeningParenMove move) {
		if (move.valueSingleLine()) {
			// a single-physical-line value's only check-clean shape is the whole call on one line (leaving the
			// ) on its own line would re-flag the closing rule), so collapse it. A ternary has a second clean
			// shape when that one line is too wide: the multi-line ternary (condition on the ( line, ?/: on
			// their own lines), so re-lay it out instead. An inline-block single-line value cannot re-flow,
			// but the check only emits its opening violation when the one-liner fits, so it never lands here wide
			final var collapsed = joinSpan(lines, move.openLine() - 1, move.closeLine() - 1);
			if (collapsed instanceof FixResult result
					&& LineLength.tabExpandedLength(result.replacement().getFirst()) > LineLength.MAX_LINE_LENGTH
					&& move.ternaryQuestion() != null)
				return applyTernaryReformat(lines, move.ternaryQuestion());
			return collapsed;
		}
		return pullUpOpening(lines, move);
	}

	/**
	 * Re-lays-out a {@code Handler.postDelayed(bracedLambda, delay)} call via
	 * {@link JavaPostDelayedReformatter} (one line when the single-statement body fits, else the lambda on
	 * the {@code (} line with {@code }, delay);} stacked after the body), mapping a refusal to the matching
	 * skip reason.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt applyPostDelayedReshape(@Nonnull List<String> lines, @Nonnull DetailAST call) {
		return switch (JavaPostDelayedReformatter.reformat(lines, call, LineLength.MAX_LINE_LENGTH, LineLength.TAB_WIDTH)) {
			case Reformatted r -> new FixResult(r.fromIndex(), r.toIndex(), r.lines());
			case CannotReformat c -> new SkipResult(switch (c.reason()) {
				case COMMENT_ON_JOINED_LINE -> SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN;
				case MULTILINE_LITERAL, SPECIAL_ARG -> SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED;
				case STALE -> SkipMessages.MULTILINE_PUT_SKIP_STALE;
			});
		};
	}

	/**
	 * Re-lays-out the ternary whose {@code QUESTION} node is {@code question} via
	 * {@link JavaTernaryReformatter}, mapping a refusal to the matching skip reason. The canonical
	 * multi-line shape is emitted even when a long branch pushes a line past
	 * {@link LineLength#MAX_LINE_LENGTH}: no fixer in the pipeline re-wraps an over-long branch, so the
	 * correct shape is strictly better than leaving the pre-existing broken layout.
	 */
	@CheckReturnValue
	@Nonnull
	static FixAttempt applyTernaryReformat(@Nonnull List<String> lines, @Nonnull DetailAST question) {
		return switch (JavaTernaryReformatter.reformat(lines, question)) {
			case Reformatted r -> new FixResult(r.fromIndex(), r.toIndex(), r.lines());
			case CannotReformat c -> new SkipResult(switch (c.reason()) {
				case COMMENT_ON_JOINED_LINE -> SkipMessages.MULTILINE_TERNARY_SKIP_COMMENT_JOIN;
				case MULTILINE_LITERAL, SPECIAL_ARG -> SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED;
				case STALE -> SkipMessages.MULTILINE_PUT_SKIP_STALE;
			});
		};
	}

	/**
	 * Tries to collapse the call span {@code lines[fromIdx..toIdx]} onto one physical line, tight-joined
	 * at the opening line's indent (per CLAUDE.md's single-line preference). Returns that one-line
	 * {@link FixResult} when the join is safe (no {@code //} comment on a non-final joined line would be
	 * swallowed, no text block / multi-line comment is crossed) and the result fits within
	 * {@link LineLength#MAX_LINE_LENGTH}; otherwise {@code null} so the caller falls back to its
	 * multi-line split (which keeps interior lines verbatim and is therefore comment-safe).
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult collapseIfFits(@Nonnull List<String> lines, int fromIdx, int toIdx) {
		if (fromIdx >= toIdx || SpanReformat.beginsInMultilineLiteral(lines, fromIdx, toIdx))
			return null;
		for (var i = fromIdx; i < toIdx; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return null;
		}

		final var oneLine = joinRange(lines, fromIdx, toIdx);
		if (LineLength.tabExpandedLength(oneLine) > LineLength.MAX_LINE_LENGTH)
			return null;
		return new FixResult(fromIdx, toIdx, List.of(oneLine));
	}

	/**
	 * Joins {@code lines[fromLine..toLine]} into one string, tightening around
	 * {@code (}/{@code [}/{@code .}/{@code )}/{@code ,}/{@code ;}/{@code ]} boundaries the same way
	 * {@link #joinSpan} does. Keeps the first line's leading whitespace and does no bounds or comment
	 * checks (callers guard those), so it is reusable for the head and tail groups of a move.
	 */
	@CheckReturnValue
	@Nonnull
	private static String joinRange(@Nonnull List<String> lines, int fromLine, int toLine) {
		final var joined = new StringBuilder(lines.get(fromLine).stripTrailing());
		for (var i = fromLine + 1; i <= toLine; ++i) {
			final var continuation = lines.get(i).strip();
			if (continuation.isEmpty())
				continue;
			if (!SpanReformat.joinsTight(joined, continuation))
				joined.append(' ');
			joined.append(continuation);
		}
		return joined.toString();
	}

	@CheckReturnValue
	@Nonnull
	private static FixAttempt joinSpan(@Nonnull List<String> lines, int fromLine, int toLine) {
		if (fromLine < 0 || toLine >= lines.size() || fromLine >= toLine)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		// a `//` comment on any line before the last would be pulled inline and swallow the rest
		for (var i = fromLine; i < toLine; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_COMMENT);
		}

		return new FixResult(fromLine, toLine, List.of(joinRange(lines, fromLine, toLine)));
	}

	/**
	 * Pulls a call's closing {@code )} up to stack with an inline block's {@code }}/{@code )} or to
	 * rejoin a single-line ternary: joins {@code [argLastLine .. rparenLine]} into one trailing line,
	 * then re-indents the whole call span. Bails (skip) if a {@code //} comment sits on a joined line.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt pullUpClosingParen(@Nonnull List<String> lines, @Nonnull ClosingParenMove move) {
		final var openIdx = move.openLine() - 1;
		final var argLastIdx = move.argLastLine() - 1;
		final var rparenIdx = move.rparenLine() - 1;
		if (openIdx < 0 || rparenIdx >= lines.size() || openIdx > argLastIdx || argLastIdx > rparenIdx)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		for (var i = argLastIdx; i < rparenIdx; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_COMMENT);
		}

		final var broken = new ArrayList<>(lines.subList(openIdx, argLastIdx));
		broken.add(joinRange(lines, argLastIdx, rparenIdx));
		return reindented(lines, openIdx, rparenIdx, broken);
	}

	/**
	 * Pulls an inline-block or ternary argument up onto the {@code (} line: joins
	 * {@code [openLine .. headJoinEnd]} into the opening line, keeps the interior lines, and joins
	 * {@code [tailJoinStart .. closeLine]} into the trailing line, then re-indents the whole span. Bails
	 * (skip) if a {@code //} comment sits on a joined line, or if the head-join or tail-join range crosses
	 * a text block / multi-line block comment (a joined literal would be corrupted). A literal in the kept
	 * interior is fine: {@link JavaSpanReindenter} preserves text-block / comment content verbatim.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt pullUpOpening(@Nonnull List<String> lines, @Nonnull OpeningParenMove move) {
		final var openIdx = move.openLine() - 1;
		final var headEndIdx = move.headJoinEnd() - 1;
		final var tailStartIdx = move.tailJoinStart() - 1;
		final var closeIdx = move.closeLine() - 1;
		if (openIdx < 0 || closeIdx >= lines.size()
				|| openIdx > headEndIdx || headEndIdx >= tailStartIdx || tailStartIdx > closeIdx)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		// a `//` comment on a line that gets joined (into the head or the tail) would swallow the rest
		for (var i = openIdx; i < headEndIdx; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN);
		}
		for (var i = tailStartIdx; i < closeIdx; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_COMMENT_JOIN);
		}

		// the head-join and tail-join ranges are collapsed onto one line, so they must not cross a text
		// block / multi-line comment; a literal in the kept interior is re-indented value-safe by
		// JavaSpanReindenter (content and closing delimiter stay verbatim)
		final var beginsInLiteral = new boolean[closeIdx - openIdx + 1];
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var i = openIdx; i <= closeIdx; ++i) {
			beginsInLiteral[i - openIdx] = lexer.inTextBlock() || lexer.inBlockComment();
			lexer = JavaLineScanner.stateAfter(lines.get(i), lexer);
		}
		for (var i = openIdx + 1; i <= headEndIdx; ++i) {
			if (beginsInLiteral[i - openIdx])
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED);
		}
		for (var i = tailStartIdx + 1; i <= closeIdx; ++i) {
			if (beginsInLiteral[i - openIdx])
				return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED);
		}

		final var broken = new ArrayList<String>();
		broken.add(joinRange(lines, openIdx, headEndIdx));
		for (var i = headEndIdx + 1; i < tailStartIdx; ++i)
			broken.add(lines.get(i));
		broken.add(joinRange(lines, tailStartIdx, closeIdx));
		return reindented(lines, openIdx, closeIdx, broken);
	}

	/**
	 * Pushes a call's closing {@code )} (and any trailing {@code ;}/{@code &#123;}/chain) off the last
	 * argument's line onto its own line: splits the reported line at the {@code )} column, then
	 * re-indents the whole call span so the {@code )} and every kept line land at their canonical depth.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt pushDownClosingParen(@Nonnull List<String> lines, @Nonnull ClosingParenMove move) {
		final var rparenIdx = move.rparenLine() - 1;
		final var openIdx = move.openLine() - 1;
		if (rparenIdx < 0 || rparenIdx >= lines.size() || openIdx < 0 || openIdx > rparenIdx)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var content = lines.get(rparenIdx);
		final var column = move.rparenColumn();
		if (column < 0 || column >= content.length())
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var left = content.substring(0, column).stripTrailing();
		if (left.isEmpty())
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var collapsed = collapseIfFits(lines, openIdx, rparenIdx);
		if (collapsed != null)
			return collapsed;

		final var broken = new ArrayList<>(lines.subList(openIdx, rparenIdx));
		broken.add(left);
		broken.add(content.substring(column));
		return reindented(lines, openIdx, rparenIdx, broken);
	}

	/**
	 * Splits a plain call's first argument off the {@code (} line: keeps everything through the
	 * {@code (} on the opening line, moves the trailing argument text onto its own line, and re-indents
	 * the whole call span so the moved arguments and the closing {@code )} land at their canonical depth.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt pushDownOpeningArgs(@Nonnull List<String> lines, @Nonnull OpeningParenMove move) {
		final var openIdx = move.openLine() - 1;
		final var closeIdx = move.closeLine() - 1;
		if (openIdx < 0 || closeIdx >= lines.size() || openIdx > closeIdx)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var content = lines.get(openIdx);
		final var parenCol = move.openParenColumn();
		if (parenCol < 0 || parenCol >= content.length() || content.charAt(parenCol) != '(')
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var trailing = content.substring(parenCol + 1).strip();
		if (trailing.isEmpty())
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_STALE);

		final var collapsed = collapseIfFits(lines, openIdx, closeIdx);
		if (collapsed != null)
			return collapsed;

		final var broken = new ArrayList<String>();
		broken.add(content.substring(0, parenCol + 1));
		broken.add(trailing);
		for (var i = openIdx + 1; i <= closeIdx; ++i)
			broken.add(lines.get(i));
		return reindented(lines, openIdx, closeIdx, broken);
	}

	/**
	 * Re-lays-out a plain opening/closing violation's whole argument list via {@link JavaArgListReformatter}
	 * (collapsing the call when it fits, else each argument on its own line, collapsing any nested multi-line
	 * plain argument). Falls back to {@code pushDown} (the layout-preserving split) when there is no plain
	 * arg-list owner or the reformatter declines (an argument needs its own opening-line layout, a {@code //}
	 * comment would be swallowed, a text block is crossed, or the coordinates are stale).
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt reformatPlainOrPushDown(@Nonnull List<String> lines, @Nonnull DetailAST root, int lineIndex, int column, @Nonnull Supplier<FixAttempt> pushDown) {
		final var owner = MultilineCallFormattingCheck.resolvableArgListOwner(root, lines, lineIndex, column);
		if (owner != null
				&& JavaArgListReformatter.reformat(lines, owner, LineLength.MAX_LINE_LENGTH, LineLength.TAB_WIDTH) instanceof Reformatted r)
			return new FixResult(r.fromIndex(), r.toIndex(), r.lines());
		return pushDown.get();
	}

	/**
	 * Re-indents {@code brokenLines} (the correctly line-broken content for a call span) via
	 * {@link JavaSpanReindenter}, seeded from the indentation of the span's opening line, and returns a
	 * {@link FixResult} replacing {@code lines[fromIdx..toIdx]} with it.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixResult reindented(@Nonnull List<String> lines, int fromIdx, int toIdx, @Nonnull List<String> brokenLines) {
		return new FixResult(fromIdx, toIdx, JavaSpanReindenter.reindent(brokenLines, SpanReformat.leadingTabs(lines.get(fromIdx))));
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var root = FixerAst.parseOrNull(lines);
		if (root == null)
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED);

		// classification AND application share one firewall: a throw from any check classifier or from a
		// move's re-emit/re-indent degrades to a skip rather than aborting the whole fix pass
		try {
			final var collapseSpan = MultilineCallFormattingCheck.collapsibleJsonObjectPutLineSpan(root, lineIndex, column);
			if (collapseSpan != null)
				return joinSpan(lines, collapseSpan[0], collapseSpan[1]);
			// postDelayed reshaping owns its own canonical (one-line unwrap or `}, delay);`), so it must
			// intercept before the generic opening/closing moves would try a layout-preserving push/pull
			final var postDelayed = MultilineCallFormattingCheck.resolvablePostDelayed(root, lineIndex, column);
			if (postDelayed != null)
				return applyPostDelayedReshape(lines, postDelayed);
			final var move = MultilineCallFormattingCheck.closingParenMove(root, lines, lineIndex, column);
			if (move != null) {
				if (move.pullUp())
					return pullUpClosingParen(lines, move);
				return reformatPlainOrPushDown(lines, root, lineIndex, column, () -> pushDownClosingParen(lines, move));
			}
			final var openMove = MultilineCallFormattingCheck.openingParenMove(root, lines, lineIndex, column);
			if (openMove != null) {
				if (openMove.pushDown())
					return reformatPlainOrPushDown(lines, root, lineIndex, column, () -> pushDownOpeningArgs(lines, openMove));
				return applyOpeningMove(lines, openMove);
			}
			final var ternaryQuestion = MultilineCallFormattingCheck.resolvableTernaryLayoutQuestion(root, lines, lineIndex, column);
			if (ternaryQuestion != null)
				return applyTernaryReformat(lines, ternaryQuestion);
			final var sharedLineOwner = MultilineCallFormattingCheck.resolvableSharedLineArgs(root, lines, lineIndex, column);
			if (sharedLineOwner != null)
				return applyArgListReformat(lines, sharedLineOwner);
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED);
		}
		catch (RuntimeException | StackOverflowError | AssertionError e) {
			return new SkipResult(SkipMessages.MULTILINE_PUT_SKIP_UNSUPPORTED);
		}
	}
}