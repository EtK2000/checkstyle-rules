package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferDirectBooleanReturnCheck;
import com.etk2000.checkstyle.format.SpanEdges;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferDirectBooleanReturnFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		final var target = FixerAst.withAst(
				lines,
				root -> PreferDirectBooleanReturnCheck.locateAt(root, lines, lineIndex, column)
		);
		// a buffer an earlier fix in the same pass left unparseable, or a position no collapsible `if`
		// starts at any more. Neither is a recognized violation, so there is no skip reason to name
		if (target == null)
			return null;

		final var span = PreferDirectBooleanReturnCheck.spanOf(lines, target);
		final var collapse = PreferDirectBooleanReturnCheck.collapseOf(lines, target);
		if (span == null || collapse == null) {
			return new SkipResult(PreferDirectBooleanReturnCheck.foldsAClassBody(lines, target)
					? SkipMessages.DIRECT_BOOLEAN_SKIP_CLASS_BODY
					: SkipMessages.DIRECT_BOOLEAN_SKIP_TEXT_BLOCK
			);
		}

		final var edges = SpanEdges.of(
				lines, span.start().line(), span.start().index(), span.end().line(), span.end().index()
		);
		final var comment = edges.appendableComment(collapse.comment());

		final var statements = collapse.statements();
		final var replacement = new ArrayList<String>(statements.size());
		for (var i = 0; i < statements.size(); ++i) {
			// whatever precedes the `if` on its line stays where it is; only the first emitted line can
			// carry it, so a hoisted statement after it falls back to the line's own indentation
			final var head = i == 0 ? edges.prefix() : edges.indent();
			// the relocated comment goes last, after any code the span left on the closing line, so it
			// comments out nothing that has to survive
			replacement.add(i == statements.size() - 1
					? head + statements.get(i) + edges.suffix() + (comment.isEmpty() ? "" : " " + comment)
					: head + statements.get(i)
			);
		}
		return new FixResult(span.start().line(), span.end().line(), replacement);
	}
}