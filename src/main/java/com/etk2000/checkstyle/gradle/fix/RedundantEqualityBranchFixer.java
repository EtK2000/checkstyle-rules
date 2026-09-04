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
 * one place) and rewrites the reported line span, keeping the code that shares its first line ahead
 * of the collapse (an {@code else} or label it is the body of, a statement before it) on a line of
 * its own and the code that shares its last line after it.
 */
class RedundantEqualityBranchFixer implements CheckstyleFixer {
	@CheckReturnValue
	private static boolean endsWithWord(@Nonnull String text, @Nonnull String word) {
		return text.endsWith(word) && LineText.isWordAt(text, text.length() - word.length(), word);
	}

	/**
	 * Whether the statement collapsed after {@code lead} is the body of what {@code lead} ends with
	 * ({@code else}, {@code do}, a condition's closing parenthesis, a label's or case's colon, a
	 * block's opening brace), rather than one more statement after it.
	 */
	@CheckReturnValue
	private static boolean opensBody(@Nonnull String lead) {
		return switch (lead.charAt(lead.length() - 1)) {
			case ')', ':', '{' -> true;
			default -> endsWithWord(lead, "do") || endsWithWord(lead, "else");
		};
	}

	/**
	 * Scans the collapse span {@code [start, end]} for code-level comments. Returns the text to append
	 * to the collapsed statement: empty when the span has no comment, or a leading space plus the
	 * if-line's trailing comment when exactly one comment occupies the rest of the if-line. Returns
	 * {@code null} (refuse) when a comment sits on any other line, a second comment appears, code
	 * follows the comment on the if-line, or a block comment spans lines: none of those can be
	 * relocated without losing or misplacing the comment. The scan starts in the state the lines above
	 * leave, so a span opening on a text block's closing line ({@code """; if (a == b) r = a; // keep})
	 * is read as code and comment rather than as text.
	 */
	@CheckReturnValue
	@Nullable
	private static String relocatableComment(@Nonnull List<String> lines, int start, int end, int ifLine) {
		var state = LexerState.NONE;
		for (var i = 0; i < start; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
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
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
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

		final var firstLine = lines.get(start);
		final var lastLine = lines.get(end);
		final var lead = firstLine.substring(0, LineText.charIndexOfColumn(firstLine, redundancy.firstColumn())).stripTrailing();
		// an if-line comment is the tail itself when the collapse ends on that line
		final var tail = lastLine.substring(LineText.charIndexOfColumn(lastLine, redundancy.endColumn())).stripTrailing();
		final var collapsed = statement + (tail.isEmpty() ? comment : end == redundancy.startLine() ? tail : tail + comment);
		if (lead.isBlank())
			return new FixResult(start, end, List.of(LineText.extractIndent(firstLine) + collapsed));
		final var indent = LineText.extractIndent(lines.get(redundancy.leadLine()));
		return new FixResult(start, end, List.of(lead, indent + (opensBody(lead) ? "\t" : "") + collapsed));
	}
}