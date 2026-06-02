package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.RedundantEqualityBranchCheck;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collapses a redundant equality if-else reported by {@code RedundantEqualityBranchCheck}. Reuses the
 * check's {@link RedundantEqualityBranchCheck#classifyAt AST classifier} (so the collapse rule lives in
 * one place) and rewrites the reported line span.
 */
class RedundantEqualityBranchFixer implements CheckstyleFixer {
	/**
	 * Scans the collapse span {@code [start, end]} for code-level comments. Returns the text to append
	 * to the collapsed statement: empty when the span has no comment, or a leading space plus the
	 * if-line's trailing comment when exactly one comment occupies the rest of the if-line. Returns
	 * {@code null} (refuse) when a comment sits on any other line, a second comment appears, code
	 * follows the comment on the if-line, or a block comment spans lines: none of those can be
	 * relocated without losing or misplacing the comment.
	 */
	@CheckReturnValue
	@Nullable
	private static String relocatableComment(@Nonnull List<String> lines, int start, int end, int ifLine) {
		var state = LexerState.NONE;
		var relocated = "";
		for (var i = start; i <= end; ++i) {
			final var line = lines.get(i);
			if (state.inBlockComment())
				return null;
			final var marker = JavaLineScanner.firstCommentMarker(line, state);
			if (marker >= 0) {
				if (JavaLineScanner.stateAfter(line, state).inBlockComment())
					return null;
				if (i != ifLine || !relocated.isEmpty())
					return null;
				if (!JavaLineScanner.stripCommentsAndStrings(line, state).substring(marker).isBlank())
					return null;
				relocated = " " + line.substring(marker).stripTrailing();
			}
			state = JavaLineScanner.stateAfter(line, state);
		}
		return relocated;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var redundancy = FixerAst.withAst(lines, root -> RedundantEqualityBranchCheck.classifyAt(root, lineIndex, column));
		if (redundancy == null)
			return null;

		final int start;
		final int end;
		final String statement;
		if (redundancy.declLine() >= 0) {
			start = redundancy.declLine();
			end = redundancy.collapseReturnLine();
			statement = "return " + redundancy.hint() + ";";
		}
		else {
			start = redundancy.startLine();
			end = redundancy.endLine();
			statement = redundancy.kind() == RedundantEqualityBranchCheck.BranchKind.ASSIGN
					? redundancy.target() + " = " + redundancy.hint() + ";"
					: "return " + redundancy.hint() + ";";
		}
		if (start < 0 || start > end || end >= lines.size())
			return null;

		final var comment = relocatableComment(lines, start, end, redundancy.startLine());
		if (comment == null)
			return new SkipResult(SkipMessages.REDUNDANT_EQUALITY_SKIP_COMMENT);

		final var indent = LineText.extractIndent(lines.get(start));
		return new FixResult(start, end, List.of(indent + statement + comment));
	}
}