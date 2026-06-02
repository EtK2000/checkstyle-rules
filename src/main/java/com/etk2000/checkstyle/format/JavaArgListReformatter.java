package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.format.SpanReformat.CannotReformat;
import com.etk2000.checkstyle.format.SpanReformat.Reason;
import com.etk2000.checkstyle.format.SpanReformat.Reformatted;
import com.etk2000.checkstyle.format.SpanReformat.Result;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Re-lays-out a multi-line call or definition argument list: the whole list is collapsed onto one line
 * when it fits within the caller's max line width, otherwise the opening {@code (} keeps its head, each
 * argument (itself collapsed onto one line) sits on its own line at the continuation indent, and the
 * closing {@code )} lands on its own line. Indentation is delegated to {@link JavaSpanReindenter};
 * slice/collapse/comment primitives and the result vocabulary are shared via {@link SpanReformat}.
 *
 * <p>The list is sliced at the {@code (}, each top-level {@code ,} (an {@code ELIST}/{@code PARAMETERS}
 * comma read off the AST, so a comma inside a generic type, string, or nested call is carried verbatim
 * within its argument), and the {@code )}. Because the whole list is re-emitted, this also satisfies
 * the opening/closing layout rules on the same call. An argument that cannot (or should not) be collapsed
 * onto one line (it spans a text block or multi-line block comment, a mid-argument {@code //} comment would
 * swallow the rest of it, or it contains a multi-line braced block, a lambda, anonymous-class, or switch
 * body, that a collapse would cram) is emitted verbatim while its siblings still collapse/split around it. A {@code //}
 * comment that leaked past a {@code ,} (it leads the next argument's slice, being a trailing comment on
 * the previous line) is lifted back onto the previous argument's line (or the {@code (} head), so it is
 * preserved rather than blocking the re-layout. Re-layout is refused entirely (the caller keeps the
 * source as-is or falls back to a layout-preserving move) only when an argument needs its own
 * opening-line shape (an inline-block or ternary configuration, per
 * {@link ArgLayoutClassifier#isSpecialLayoutConfiguration}).
 *
 * <p>Reformatting utility that depends only on the shared {@link ArgLayoutClassifier} /
 * {@link SpanReformat} / {@link JavaSpanReindenter} utilities, never on a check or fixer.
 */
public final class JavaArgListReformatter {
	/**
	 * Re-lays-out the argument/parameter list of {@code owner} (a {@code METHOD_CALL},
	 * {@code LITERAL_NEW}, {@code SUPER_CTOR_CALL}, {@code METHOD_DEF} or {@code CTOR_DEF}, as returned by
	 * {@code MultilineCallFormattingCheck.resolvableSharedLineArgs}). Collapses the whole list onto one
	 * line when it fits within {@code maxLineWidth} (tabs expanded to {@code tabWidth}); otherwise puts
	 * each argument on its own line. {@code lines} are the current source lines the {@code owner} AST was
	 * parsed from.
	 */
	@CheckReturnValue
	@Nonnull
	public static Result reformat(@Nonnull List<String> lines, @Nonnull DetailAST owner, int maxLineWidth, int tabWidth) {
		final var argList = switch (owner.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> owner.findFirstToken(TokenTypes.PARAMETERS);
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL ->
					owner.findFirstToken(TokenTypes.ELIST);
			default -> null;
		};
		final var rparen = owner.findFirstToken(TokenTypes.RPAREN);
		if (argList == null || rparen == null)
			return new CannotReformat(Reason.STALE);

		// an inline-block or ternary configuration must keep its opening-line shape: re-laying its
		// argument list out here would produce a shape the check re-flags. getString/computeIfAbsent/put
		// configs never reach the reformatter (they are classified inline-block and routed elsewhere), so
		// only the structural/name-based configs matter here, hence the empty contextSpecial predicate
		if (ArgLayoutClassifier.isSpecialLayoutConfiguration(owner, node -> false))
			return new CannotReformat(Reason.SPECIAL_ARG);

		final var lparen = owner.findFirstToken(TokenTypes.LPAREN);
		final var openIdx = (lparen != null ? lparen.getLineNo() : owner.getLineNo()) - 1;
		final var openCol = lparen != null ? lparen.getColumnNo() : owner.getColumnNo();
		final var closeIdx = rparen.getLineNo() - 1;
		final var closeCol = rparen.getColumnNo();

		final var boundaries = new ArrayList<int[]>();
		boundaries.add(new int[]{openIdx, openCol});
		for (var child = argList.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				boundaries.add(new int[]{child.getLineNo() - 1, child.getColumnNo()});
		}
		boundaries.add(new int[]{closeIdx, closeCol});

		// the boundaries are always at least the ( and the ) (a single argument, no comma); fewer is a stale coord
		if (boundaries.size() < 2 || openIdx < 0 || closeIdx >= lines.size())
			return new CannotReformat(Reason.STALE);
		for (var i = 0; i < boundaries.size(); ++i) {
			final var b = boundaries.get(i);
			final var expected = i == 0 ? '(' : i == boundaries.size() - 1 ? ')' : ',';
			if (!SpanReformat.pointsAt(lines, b[0], b[1], expected))
				return new CannotReformat(Reason.STALE);
			if (i > 0) {
				final var prev = boundaries.get(i - 1);
				if (b[0] < prev[0] || (b[0] == prev[0] && b[1] <= prev[1]))
					return new CannotReformat(Reason.STALE);
			}
		}

		// which lines begin inside a text block / multi-line block comment: a collapse across them would
		// corrupt the literal, so an argument spanning one is re-emitted verbatim instead of joined
		final var beginsInLiteral = SpanReformat.beginsInMultilineLiteralByLine(lines, openIdx, closeIdx);

		// the argument nodes in order (segment i corresponds to argNodes[i]), so a segment can be tested for
		// a multi-line braced block that must not be tight-collapsed
		final var argNodes = new ArrayList<DetailAST>();
		for (var child = argList.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				argNodes.add(child);
		}

		final var verbatim = new boolean[boundaries.size() - 1];
		var anyVerbatim = false;
		for (var i = 0; i + 1 < boundaries.size(); ++i) {
			for (var ln = boundaries.get(i)[0] + 1; ln <= boundaries.get(i + 1)[0]; ++ln) {
				if (beginsInLiteral[ln - openIdx]) {
					verbatim[i] = true;
					anyVerbatim = true;
					break;
				}
			}
			// a multi-line braced block (lambda / anonymous-class / switch body) would be crammed onto one
			// line by a collapse, so its argument is emitted verbatim instead, like the text-block handling
			if (!verbatim[i] && i < argNodes.size() && ArgLayoutClassifier.containsMultilineBracedBlock(argNodes.get(i))) {
				verbatim[i] = true;
				anyVerbatim = true;
			}
		}

		final var segments = new ArrayList<List<String>>();
		for (var i = 0; i + 1 < boundaries.size(); ++i) {
			final var a = boundaries.get(i);
			final var b = boundaries.get(i + 1);
			segments.add(SpanReformat.slice(lines, a[0], a[1] + 1, b[0], b[1]));
		}
		// a `//` comment that leads a segment leaked past the previous separator (it is a trailing comment on
		// that line, not this argument's own code): lift it out so it re-attaches to the previous argument's
		// line (or the `(` head), then let the argument itself collapse/split. A `//` comment that stays
		// mid-argument (code before it, more of the argument after) would be swallowed by a one-line collapse,
		// so that argument is emitted verbatim while its siblings collapse/split around it
		final var leadingComment = new String[segments.size()];
		// physical line of an own-line leaked comment (fragment index > 0), so a verbatim re-emission can drop
		// exactly that line rather than the first interior line whose text matches (which could be a distinct,
		// identical comment the argument legitimately contains); -1 when no comment or it trailed the previous `,`
		final var leadingCommentLine = new int[segments.size()];
		Arrays.fill(leadingCommentLine, -1);
		var anyLeadingComment = false;
		for (var i = 0; i < segments.size(); ++i) {
			final var segment = segments.get(i);
			// lift the leaked comment even when the segment is already verbatim (a text block / braced block),
			// so it re-attaches to the previous argument's line rather than being re-emitted inside the block
			if (SpanReformat.beginsWithLineComment(segment)) {
				for (var f = 0; f < segment.size(); ++f) {
					if (!segment.get(f).isBlank()) {
						leadingComment[i] = segment.get(f).strip();
						// fragment f maps to physical line boundaries[i][0] + f; f == 0 trails the previous
						// separator on its own line and is dropped by the firstLine guard, so only an own-line
						// comment (f > 0) needs an interior line skipped in the verbatim re-emission
						if (f > 0)
							leadingCommentLine[i] = boundaries.get(i)[0] + f;
						segment.set(f, "");
						anyLeadingComment = true;
						break;
					}
				}
			}
			if (!verbatim[i] && SpanReformat.swallowsComment(segment)) {
				verbatim[i] = true;
				anyVerbatim = true;
			}
		}

		final var arguments = new ArrayList<String>(segments.size());
		for (var segment : segments)
			arguments.add(SpanReformat.collapse(segment));
		// a trailing `//` on any argument blocks the one-line collapse (it would swallow the following
		// separator or the closing `)`), but the multi-line shape keeps it: the `,` is hoisted ahead of the
		// comment so each argument still lands on its own line with its comment intact
		var anyTrailingComment = false;
		for (var i = 0; i < arguments.size(); ++i) {
			if (!verbatim[i] && SpanReformat.hasTrailingLineComment(arguments.get(i))) {
				anyTrailingComment = true;
				break;
			}
		}

		final var baseTabs = SpanReformat.leadingTabs(lines.get(openIdx));
		final var suffix = lines.get(closeIdx).substring(closeCol);

		// the whole call collapses onto one line only when no argument must stay multi-line (a text block /
		// block comment cannot be one-lined) and no argument carries a trailing or leaked `//` comment
		if (!anyVerbatim && !anyTrailingComment && !anyLeadingComment) {
			final var head = lines.get(openIdx).substring(0, openCol + 1).strip();
			final var oneLine = "\t".repeat(baseTabs) + head + String.join(", ", arguments) + suffix;
			if (SpanReformat.tabExpandedWidth(oneLine, tabWidth) <= maxLineWidth)
				return new Reformatted(openIdx, closeIdx, List.of(oneLine));
		}

		final var broken = new ArrayList<String>();
		// a `//` comment that led the first segment sits on the `(` line; keep it there (args on later lines
		// never join back onto it, so it cannot swallow)
		final var head = lines.get(openIdx).substring(0, openCol + 1);
		broken.add(leadingComment[0] != null ? head + " " + leadingComment[0] : head);
		for (var i = 0; i < arguments.size(); ++i) {
			final var comma = i + 1 < arguments.size() ? "," : "";
			// a `//` comment that leaked to the NEXT segment belongs on this argument's line, after its `,`
			final var trailing = i + 1 < leadingComment.length && leadingComment[i + 1] != null
					? " " + leadingComment[i + 1] : "";
			if (verbatim[i]) {
				// re-emit the argument's raw source lines; JavaSpanReindenter keeps text-block / block-comment
				// interiors verbatim, so the literal's value is preserved. The trailing separator attaches to
				// the argument's last real line (the `,`/`)` boundary may sit on a later, whitespace-only line)
				final var a = boundaries.get(i);
				final var b = boundaries.get(i + 1);
				final var segLines = new ArrayList<String>();
				final var firstLine = lines.get(a[0]).substring(a[1] + 1);
				// skip the first line when it is only the leaked comment already lifted to the previous line
				if (leadingComment[i] == null && !firstLine.isBlank())
					segLines.add(firstLine);
				var interiorStart = a[0] + 1;
				// an own-line leaked `//` comment was lifted to the previous line; the interior lines before it are
				// all blank (it is the segment's first non-blank fragment), so resume just past it and drop any
				// blank lines it was separated from the argument by, leaving the argument's real content
				if (leadingCommentLine[i] >= 0) {
					interiorStart = leadingCommentLine[i] + 1;
					while (interiorStart < b[0] && lines.get(interiorStart).isBlank())
						++interiorStart;
				}
				for (var ln = interiorStart; ln < b[0]; ++ln)
					segLines.add(lines.get(ln));
				// the literal has closed before the ,/) boundary; drop any non-significant trailing whitespace
				final var lastPart = lines.get(b[0]).substring(0, b[1]).stripTrailing();
				if (!lastPart.isBlank())
					segLines.add(lastPart);
				// blank lines between the argument's last real line and the ,/) boundary must not steal the
				// separator (a text block's closing delimiter is its last line, so this only drops layout blanks)
				while (!segLines.isEmpty() && segLines.getLast().isBlank())
					segLines.removeLast();
				if (segLines.isEmpty())
					return new CannotReformat(Reason.STALE);
				segLines.set(segLines.size() - 1, segLines.getLast() + comma + trailing);
				broken.addAll(segLines);
			}
			else
				broken.add(SpanReformat.insertBeforeTrailingComment(arguments.get(i), comma) + trailing);
		}
		broken.add(suffix);
		return new Reformatted(openIdx, closeIdx, JavaSpanReindenter.reindent(broken, baseTabs));
	}

	private JavaArgListReformatter() {
	}
}