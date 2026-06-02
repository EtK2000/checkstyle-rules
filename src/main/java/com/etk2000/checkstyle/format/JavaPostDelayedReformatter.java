package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.AstUtil;
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
 * Re-lays-out a {@code Handler.postDelayed(inlineBlock, delay)} call (as classified by
 * {@link ArgLayoutClassifier#isPostDelayedWithInlineBlock}), where the first argument is a braced lambda
 * or an anonymous class, into its canonical shape: when the first argument is a braced lambda whose body
 * is a single expression statement and the whole call fits within the caller's max line width, the braces
 * are unwrapped and the call is collapsed onto one line ({@code handler.postDelayed(() -> stmt, delay);});
 * otherwise (a multi-statement / non-expression lambda body, an anonymous class, or a collapsed form that
 * exceeds the width) the block opens on the {@code (} line, its body stays multi-line, and the closing
 * {@code }, delay);} lands on one line after the body. Indentation of the multi-line shape is delegated
 * to {@link JavaSpanReindenter}; slice/collapse/width primitives are shared via {@link SpanReformat}.
 *
 * <p>Reformatting utility that depends only on the shared {@link AstUtil} / {@link SpanReformat} /
 * {@link JavaSpanReindenter} utilities, never on a check or fixer.
 */
public final class JavaPostDelayedReformatter {
	/**
	 * Re-lays-out {@code call} (a {@code postDelayed} {@code METHOD_CALL} with a braced-lambda first
	 * argument and a delay second argument). {@code lines} are the current source lines the {@code call}
	 * AST was parsed from; the one-line form is produced only when it fits within {@code maxLineWidth}
	 * (tabs expanded to {@code tabWidth}).
	 */
	@CheckReturnValue
	@Nonnull
	public static Result reformat(@Nonnull List<String> lines, @Nonnull DetailAST call, int maxLineWidth, int tabWidth) {
		// a METHOD_CALL has no LPAREN child: the call token itself sits at the opening `(`
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		final var elist = call.findFirstToken(TokenTypes.ELIST);
		if (rparen == null || elist == null)
			return new CannotReformat(Reason.STALE);

		DetailAST lambdaArg = null, delayArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;
			if (lambdaArg == null)
				lambdaArg = child;
			else if (delayArg == null)
				delayArg = child;
			else
				return new CannotReformat(Reason.STALE);
		}
		if (lambdaArg == null || delayArg == null)
			return new CannotReformat(Reason.STALE);

		// the first arg is a direct braced inline block: a braced lambda (SLIST body) or an anonymous class
		// (OBJBLOCK body). A lambda's SLIST node IS its `{`; an anon class's OBJBLOCK has an LCURLY child
		final var block = ArgLayoutClassifier.directInlineBlock(lambdaArg);
		if (block == null)
			return new CannotReformat(Reason.STALE);
		final var braceBody = block.getType() == TokenTypes.LAMBDA
				? block.findFirstToken(TokenTypes.SLIST) : block.findFirstToken(TokenTypes.OBJBLOCK);
		if (braceBody == null)
			return new CannotReformat(Reason.STALE);
		final var lcurly = braceBody.getType() == TokenTypes.SLIST ? braceBody : braceBody.findFirstToken(TokenTypes.LCURLY);
		final var rcurly = braceBody.findFirstToken(TokenTypes.RCURLY);
		if (lcurly == null || rcurly == null)
			return new CannotReformat(Reason.STALE);

		final var openIdx = call.getLineNo() - 1;
		final var lcurlyIdx = lcurly.getLineNo() - 1;
		final var lcurlyCol = lcurly.getColumnNo();
		final var rcurlyIdx = rcurly.getLineNo() - 1;
		final var rcurlyCol = rcurly.getColumnNo();
		final var closeIdx = rparen.getLineNo() - 1;
		final var rparenCol = rparen.getColumnNo();
		if (openIdx < 0 || closeIdx >= lines.size() || openIdx > lcurlyIdx || lcurlyIdx > rcurlyIdx || rcurlyIdx > closeIdx)
			return new CannotReformat(Reason.STALE);
		// the reported columns must still point at the block's `{`/`}` and the call's `)`; a prior same-pass
		// edit that shifted them without shifting line numbers would otherwise slice at the wrong character
		if (!SpanReformat.pointsAt(lines, lcurlyIdx, lcurlyCol, '{')
				|| !SpanReformat.pointsAt(lines, rcurlyIdx, rcurlyCol, '}')
				|| !SpanReformat.pointsAt(lines, closeIdx, rparenCol, ')'))
			return new CannotReformat(Reason.STALE);

		// any // comment on a joined line would swallow the rest, and a text block / block comment cannot be
		// collapsed onto one line; either way keep the source as-is
		for (var i = openIdx; i < closeIdx; ++i) {
			if (SpanReformat.hasTrailingLineComment(lines.get(i)))
				return new CannotReformat(Reason.COMMENT_ON_JOINED_LINE);
		}
		if (SpanReformat.beginsInMultilineLiteral(lines, openIdx, closeIdx + 1))
			return new CannotReformat(Reason.MULTILINE_LITERAL);

		final var baseTabs = SpanReformat.leadingTabs(lines.get(openIdx));
		final var suffix = lines.get(closeIdx).substring(rparenCol);

		// only a braced lambda with a single EXPRESSION-statement body can be unwrapped onto one line; an anon
		// class (OBJBLOCK) or a return/if/var-def/throw body keeps its braces via the multi-line fallback,
		// else the unwrap would emit uncompilable source
		if (AstUtil.singleExpressionStatementBody(braceBody) != null) {
			final var head = SpanReformat.collapse(SpanReformat.slice(lines, openIdx, 0, lcurlyIdx, lcurlyCol));
			final var bodyRaw = SpanReformat.collapse(SpanReformat.slice(lines, lcurlyIdx, lcurlyCol + 1, rcurlyIdx, rcurlyCol));
			final var body = bodyRaw.endsWith(";") ? bodyRaw.substring(0, bodyRaw.length() - 1).stripTrailing() : bodyRaw;
			final var afterBrace = SpanReformat.collapse(SpanReformat.slice(lines, rcurlyIdx, rcurlyCol + 1, closeIdx, rparenCol));
			final var oneLine = "\t".repeat(baseTabs) + head + " " + body + afterBrace + suffix;
			if (SpanReformat.tabExpandedWidth(oneLine, tabWidth) <= maxLineWidth)
				return new Reformatted(openIdx, closeIdx, List.of(oneLine));
		}

		final var broken = new ArrayList<String>();
		broken.add(SpanReformat.collapse(SpanReformat.slice(lines, openIdx, 0, lcurlyIdx, lcurlyCol + 1)));
		for (var i = lcurlyIdx + 1; i < rcurlyIdx; ++i)
			broken.add(lines.get(i));
		broken.add(SpanReformat.collapse(SpanReformat.slice(lines, rcurlyIdx, rcurlyCol, closeIdx, rparenCol + 1)) + suffix.substring(1));
		return new Reformatted(openIdx, closeIdx, JavaSpanReindenter.reindent(broken, baseTabs));
	}

	private JavaPostDelayedReformatter() {
	}
}