package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.format.SpanReformat.CannotReformat;
import com.etk2000.checkstyle.format.SpanReformat.Reason;
import com.etk2000.checkstyle.format.SpanReformat.Reformatted;
import com.etk2000.checkstyle.format.SpanReformat.Result;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Re-lays-out a multi-line ternary that is a call argument into the project's canonical shape: the
 * condition on the opening {@code (} line, {@code ? <trueBranch>} on the next line,
 * {@code : <falseBranch>} on the line after, and the closing {@code )} on its own line, with each
 * part collapsed onto a single line. Indentation is delegated to {@link JavaSpanReindenter}; the
 * slice/collapse/comment primitives and the result vocabulary are shared via {@link SpanReformat}.
 *
 * <p>The source is sliced at the {@code (}, {@code ?}, {@code :} and {@code )} tokens (read off the
 * AST, so a nested ternary, string, or comment inside a branch is carried verbatim within its
 * segment). Because the whole ternary is re-emitted, this also satisfies the opening/closing layout
 * rules on the same ternary. Collapsing is refused when a {@code //} comment would be swallowed by a
 * joined segment or a branch spans a text block or multi-line block comment.
 *
 * <p>This is a reformatting utility with no dependency on any particular check or fixer, so a future
 * formatting fixer can reuse it. It applies no line-length policy: callers measure the returned lines
 * and decide whether the result is acceptable.
 */
public final class JavaTernaryReformatter {
	/**
	 * Appends a ternary segment to {@code out}: {@code prefix + <collapsed segment>} for a plain segment,
	 * or {@code prefix} then the segment's raw source lines when it spans a text block / multi-line comment
	 * (which cannot be joined onto one line). The segment is {@code lines[startIdx..endIdx]} bounded by
	 * {@code startCol} (exclusive on {@code startIdx}) and {@code endCol} (exclusive on {@code endIdx}).
	 */
	private static void emitSegment(@Nonnull List<String> out, @Nonnull List<String> lines, int startIdx, int startCol, int endIdx, int endCol, @Nonnull String prefix, boolean verbatim, @Nonnull List<String> fragments) {
		if (!verbatim) {
			out.add(prefix + SpanReformat.collapse(fragments));
			return;
		}
		final var raw = new ArrayList<String>();
		final var first = lines.get(startIdx).substring(startCol);
		if (!first.isBlank())
			raw.add(first.strip());
		for (var ln = startIdx + 1; ln < endIdx; ++ln)
			raw.add(lines.get(ln));
		// the literal has closed before the ?/:/) boundary; anything between it and the boundary is
		// non-significant whitespace, so drop it to avoid a trailing space on the emitted line
		final var last = lines.get(endIdx).substring(0, endCol).stripTrailing();
		if (!last.isBlank())
			raw.add(last);
		if (raw.isEmpty())
			out.add(prefix + SpanReformat.collapse(fragments));
		else {
			raw.set(0, prefix + raw.getFirst());
			out.addAll(raw);
		}
	}

	@CheckReturnValue
	private static DetailAST enclosingCall(@Nonnull DetailAST node) {
		for (var p = node.getParent(); p != null; p = p.getParent()) {
			final var t = p.getType();
			if (t == TokenTypes.METHOD_CALL || t == TokenTypes.LITERAL_NEW || t == TokenTypes.SUPER_CTOR_CALL)
				return p;
		}
		return null;
	}

	/**
	 * Re-lays-out the ternary whose {@code QUESTION} node is {@code question} (as returned by
	 * {@code MultilineCallFormattingCheck.resolvableTernaryLayoutQuestion}). The ternary must be a call
	 * argument; {@code lines} are the current source lines the {@code question} AST was parsed from.
	 */
	@CheckReturnValue
	@Nonnull
	public static Result reformat(@Nonnull List<String> lines, @Nonnull DetailAST question) {
		final var colon = question.findFirstToken(TokenTypes.COLON);
		final var call = enclosingCall(question);
		if (colon == null || call == null)
			return new CannotReformat(Reason.STALE);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return new CannotReformat(Reason.STALE);
		final var lparen = call.findFirstToken(TokenTypes.LPAREN);
		final var openIdx = (lparen != null ? lparen.getLineNo() : call.getLineNo()) - 1;
		final var qIdx = question.getLineNo() - 1;
		final var qCol = question.getColumnNo();
		final var cIdx = colon.getLineNo() - 1;
		final var cCol = colon.getColumnNo();
		final var closeIdx = rparen.getLineNo() - 1;
		final var closeCol = rparen.getColumnNo();

		if (openIdx < 0 || closeIdx >= lines.size() || openIdx > qIdx || qIdx > cIdx || cIdx > closeIdx)
			return new CannotReformat(Reason.STALE);
		if (!SpanReformat.pointsAt(lines, qIdx, qCol, '?')
				|| !SpanReformat.pointsAt(lines, cIdx, cCol, ':')
				|| !SpanReformat.pointsAt(lines, closeIdx, closeCol, ')'))
			return new CannotReformat(Reason.STALE);

		// which lines begin inside a text block / multi-line comment: a segment spanning one cannot be
		// joined onto its canonical line, so it is re-emitted verbatim instead
		final var beginsInLiteral = SpanReformat.beginsInMultilineLiteralByLine(lines, openIdx, closeIdx);
		final var headVerbatim = spansLiteral(beginsInLiteral, openIdx, openIdx, qIdx);
		final var trueVerbatim = spansLiteral(beginsInLiteral, openIdx, qIdx, cIdx);
		final var falseVerbatim = spansLiteral(beginsInLiteral, openIdx, cIdx, closeIdx);

		final var headFragments = SpanReformat.slice(lines, openIdx, 0, qIdx, qCol);
		final var trueFragments = SpanReformat.slice(lines, qIdx, qCol + 1, cIdx, cCol);
		final var falseFragments = SpanReformat.slice(lines, cIdx, cCol + 1, closeIdx, closeCol);

		// a `//` comment on a joined line would swallow the rest, but only for a segment being COLLAPSED
		if ((!headVerbatim && SpanReformat.swallowsComment(headFragments))
				|| (!trueVerbatim && SpanReformat.swallowsComment(trueFragments))
				|| (!falseVerbatim && SpanReformat.swallowsComment(falseFragments)))
			return new CannotReformat(Reason.COMMENT_ON_JOINED_LINE);

		final var broken = new ArrayList<String>();
		emitSegment(broken, lines, openIdx, 0, qIdx, qCol, "", headVerbatim, headFragments);
		emitSegment(broken, lines, qIdx, qCol + 1, cIdx, cCol, "? ", trueVerbatim, trueFragments);
		emitSegment(broken, lines, cIdx, cCol + 1, closeIdx, closeCol, ": ", falseVerbatim, falseFragments);
		broken.add(lines.get(closeIdx).substring(closeCol));
		return new Reformatted(openIdx, closeIdx, JavaSpanReindenter.reindent(broken, SpanReformat.leadingTabs(lines.get(openIdx))));
	}

	@CheckReturnValue
	private static boolean spansLiteral(@Nonnull boolean[] beginsInLiteral, int openIdx, int fromIdx, int toIdx) {
		for (var ln = fromIdx + 1; ln <= toIdx; ++ln) {
			if (beginsInLiteral[ln - openIdx])
				return true;
		}
		return false;
	}

	private JavaTernaryReformatter() {
	}
}