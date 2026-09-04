package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.BraceOwnLineCheck;
import com.etk2000.checkstyle.PreferDirectBooleanReturnCheck;
import com.etk2000.checkstyle.format.SpanEdges;
import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferDirectBooleanReturnFixer implements CheckstyleFixer {
	/**
	 * Emits a block that {@link PreferDirectBooleanReturnCheck#joinsParentHeader} as the body of the
	 * unbraced statement {@code ifAst} sits under. A comment trailing that statement's header stays
	 * after the brace; lines between the header and the {@code if} stay inside the block.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt blockOnParentHeader(
			@Nonnull List<String> lines,
			@Nonnull DetailAST ifAst,
			@Nonnull List<String> block,
			int endLine,
			@Nonnull String suffix,
			@Nonnull String comment
	) {
		final var headerLine = BraceOwnLineCheck.headerEnd(ifAst.getParent(), ifAst).getLineNo() - 1;
		final var parentIndent = PreferDirectBooleanReturnCheck.parentIndent(lines, ifAst);
		final var headerText = lines.get(headerLine);
		final var codeEnd = FixerAst.maskAll(lines).get(headerLine).stripTrailing().length();
		final var headerComment = headerText.substring(codeEnd).strip();

		final var replacement = new ArrayList<String>(block.size() + endLine - headerLine);
		replacement.add(headerText.substring(0, codeEnd) + " {" + (headerComment.isEmpty() ? "" : " " + headerComment));
		replacement.addAll(lines.subList(headerLine + 1, ifAst.getLineNo() - 1));
		for (var statement : block.subList(1, block.size() - 1))
			replacement.add(parentIndent + statement);
		replacement.add(parentIndent + block.getLast() + suffix + (comment.isEmpty() ? "" : " " + comment));
		return new FixResult(headerLine, endLine, replacement);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var target = FixerAst.locate(
				lines,
				lineIndex,
				charColumn,
				(root, line, column) -> PreferDirectBooleanReturnCheck.locateAt(root, lines, line, column)
		);
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
		if (PreferDirectBooleanReturnCheck.joinsParentHeader(statements, edges))
			return blockOnParentHeader(lines, target.ifAst(), statements, span.end().line(), edges.suffix(), comment);

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