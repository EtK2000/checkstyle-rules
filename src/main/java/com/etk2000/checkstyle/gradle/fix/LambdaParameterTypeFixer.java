package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.LambdaParameterTypeCheck;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Rewrites a lambda's parameter list to the form {@link LambdaParameterTypeCheck} asked for. The
 * list, its parameters and the spans to slice all come from the check, so neither side re-derives
 * the decision from text.
 *
 * <p>The written type is never re-emitted, only dropped or replaced by {@code var}. That is what
 * keeps a C-style {@code int a[]} and a varargs {@code String... a} from leaving brackets or an
 * ellipsis behind: there is no type span to get the extent of wrong.
 */
class LambdaParameterTypeFixer implements CheckstyleFixer {
	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		final var params = FixerAst.withAst(
				lines, root -> LambdaParameterTypeCheck.paramsAt(root, lines, lineIndex + 1, column)
		);
		// a buffer an earlier fix in the same pass left unparseable, or a position no reported lambda
		// starts at any more. Neither is a recognized violation, so there is no skip reason to name
		if (params == null)
			return null;

		// the check gated this too, but on checkstyle's own file text; this buffer is the one earlier
		// fixes in the same pass have been editing, so the decision is taken again against it
		final var emitted = LambdaParameterTypeCheck.rewrittenList(lines, params);
		if (emitted == null || !LambdaParameterTypeCheck.collapsible(lines, FixerAst.maskAll(lines), params))
			return null;

		final var span = params.listSpan();
		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var rewritten = edges.prefix() + emitted + edges.suffix();
		// a position that resolved to a lambda already in this form would otherwise count as a fix and
		// send the file round the pipeline again, up to the pass limit, without ever changing anything
		final var current = AstSpan.sliceVerbatim(lines, span.start(), span.end());
		if (rewritten.equals(edges.prefix() + current + edges.suffix()))
			return null;
		return new FixResult(span.start().line(), span.end().line(), List.of(rewritten));
	}
}