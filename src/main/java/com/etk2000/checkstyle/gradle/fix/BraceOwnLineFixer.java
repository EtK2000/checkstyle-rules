package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.BraceOwnLineCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.ast.AstQuery;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code BraceOwnLineCheck}: pulls the opening brace up onto the header's last line.
 *
 * <p>A comment trailing the header stays at the end of the joined line, after the brace. Every
 * other comment between the header and the brace, including one ahead of the brace on its own line,
 * becomes the block's first line. What follows the brace on its line rides up with it, unless the
 * header's comment is already there or a comment moved into the block; it then follows them as the
 * block's next line, splitting a block that closes on the brace line.
 *
 * <p>A brace indented differently from its statement has the block re-indented to the statement's
 * indent, which is refused when a text block inside would change value.
 */
final class BraceOwnLineFixer implements CheckstyleFixer {
	/**
	 * {@code line} with its {@code from} indent prefix replaced by {@code to}, or {@code null} when
	 * it does not start with that prefix.
	 */
	@CheckReturnValue
	@Nullable
	private static String reindented(@Nonnull String line, @Nonnull String from, @Nonnull String to) {
		return line.startsWith(from) ? to + line.substring(from.length()) : null;
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var root = FixerAst.parseOrNull(lines);
		if (root == null)
			return new SkipResult(SkipMessages.BRACE_OWN_LINE_SKIP_NO_PARSE);
		final var site = BraceOwnLineCheck.siteAt(root, lineIndex, LineText.columnOfCharIndex(lines.get(lineIndex), column));
		if (site == null || !site.ownLine())
			return new SkipResult(SkipMessages.BRACE_OWN_LINE_SKIP_STALE);

		final var headerLine = site.headerEndLine() - 1;
		final var braceLine = site.brace().getLineNo() - 1;
		final var closeLine = site.close().getLineNo() - 1;
		final var braceText = lines.get(braceLine);
		final var braceIndex = LineText.charIndexOfColumn(braceText, site.brace().getColumnNo());
		final var statementIndent = LineText.extractIndent(lines.get(AstQuery.firstLine(site.owner()) - 1));
		final var bodyIndent = statementIndent + '\t';
		final var masked = FixerAst.maskAll(lines);

		final var headerText = lines.get(headerLine);
		final var codeEnd = masked.get(headerLine).stripTrailing().length();
		final var headerComment = headerText.substring(codeEnd).strip();

		// lines between the header and the brace are comments or blank. A block comment the header
		// line opens keeps its continuation lines verbatim right below it; any other comment moves
		// into the block at the body indent, a multi-line one shifted as a whole
		final var moved = new ArrayList<String>();
		var state = FixerAst.entryState(lines, headerLine + 1);
		var continuesHeaderComment = state.inBlockComment();
		var openerIndent = "";
		for (var i = headerLine + 1; i <= braceLine; ++i) {
			final var text = i == braceLine ? braceText.substring(0, braceIndex) : lines.get(i);
			if (state.inBlockComment()) {
				if (continuesHeaderComment)
					moved.add(text.stripTrailing());
				else {
					final var shifted = reindented(text, openerIndent, bodyIndent);
					moved.add((shifted == null ? text : shifted).stripTrailing());
				}
			}
			else if (!text.isBlank()) {
				openerIndent = LineText.extractIndent(text);
				moved.add(bodyIndent + text.strip());
			}
			state = JavaLineScanner.stateAfter(text, state);
			if (!state.inBlockComment())
				continuesHeaderComment = false;
		}

		final var afterBrace = braceText.substring(braceIndex + 1).stripTrailing();
		final var result = new ArrayList<String>();
		final var head = headerText.substring(0, codeEnd) + " {";
		// riding up ahead of moved comments would put the brace line's text out of source order, and a
		// block comment it leaves open would take the moved ones in, the first `*/` among them ending it
		if (headerComment.isEmpty() && moved.isEmpty())
			result.add(head + afterBrace);
		else {
			result.add(headerComment.isEmpty() ? head : head + ' ' + headerComment);
			result.addAll(moved);
			if (closeLine == braceLine) {
				final var closeIndex = LineText.charIndexOfColumn(braceText, site.close().getColumnNo());
				final var inner = braceText.substring(braceIndex + 1, closeIndex).strip();
				if (!inner.isEmpty())
					result.add(bodyIndent + inner);
				result.add(statementIndent + braceText.substring(closeIndex).stripTrailing());
			}
			else if (!afterBrace.isBlank())
				result.add(bodyIndent + afterBrace.strip());
		}

		final var braceEntry = FixerAst.entryState(lines, braceLine);
		// a brace line that begins inside a block comment is indented for the comment, not the brace
		final var braceIndent = braceEntry.inBlockComment() ? statementIndent : LineText.extractIndent(braceText);
		if (closeLine == braceLine || braceIndent.equals(statementIndent))
			return new FixResult(headerLine, braceLine, result);

		// shifting every line of a text block by the same prefix keeps its incidental indentation,
		// and with it the value; a content line that lacks the prefix cannot shift with the rest
		state = JavaLineScanner.stateAfter(braceText, braceEntry);
		for (var i = braceLine + 1; i <= closeLine; ++i) {
			final var text = lines.get(i);
			final var shifted = text.isBlank() ? text : reindented(text, braceIndent, statementIndent);
			if (shifted == null && state.inTextBlock())
				return new SkipResult(SkipMessages.BRACE_OWN_LINE_SKIP_TEXT_BLOCK);
			result.add(shifted == null ? text : shifted);
			state = JavaLineScanner.stateAfter(text, state);
		}
		return new FixResult(headerLine, closeLine, result);
	}
}