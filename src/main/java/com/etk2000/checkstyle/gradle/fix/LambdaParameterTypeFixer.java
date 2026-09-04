package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LambdaParameterTypeCheck;

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
		final var params = FixerAst.locate(
				lines,
				lineIndex,
				charColumn,
				(root, line, column) -> LambdaParameterTypeCheck.paramsAt(root, lines, line, column)
		);
		if (params == null)
			return null;

		// the check gated this too, but on checkstyle's own file text; this buffer is the one earlier
		// fixes in the same pass have been editing, so the decision is taken again against it
		final var emitted = LambdaParameterTypeCheck.rewrittenList(lines, params);
		if (emitted == null || !LambdaParameterTypeCheck.collapsible(lines, FixerAst.maskAll(lines), params))
			return null;
		return SpanRewrite.replacing(lines, params.listSpan(), emitted);
	}
}