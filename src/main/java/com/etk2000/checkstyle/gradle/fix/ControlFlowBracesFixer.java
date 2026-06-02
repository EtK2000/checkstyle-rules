package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.ControlFlowBracesCheck.ControlBody;
import com.etk2000.checkstyle.ControlFlowBracesCheck.DoWhileShape;
import com.etk2000.checkstyle.ControlFlowBracesCheck.OneLinerBody;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code ControlFlowBracesCheck}. Non-do-while statements get their
 * one-liner body moved to its own line, unnecessary braces removed, or missing
 * braces added. Do-while violations are reshaped to their formatting tier.
 */
class ControlFlowBracesFixer implements CheckstyleFixer {
	/**
	 * Text the reshaped do-while has to carry over from lines its replacement
	 * range covers but the two/three-line tier form would otherwise drop.
	 *
	 * @param doLineTrailer  what followed the {@code do} (or its {@code &#123;}),
	 *                       appended to the emitted {@code do} line
	 * @param beforeWhile    whole lines sitting between the body and the
	 *                       {@code while}, re-emitted ahead of it
	 */
	private record CarriedText(@Nonnull String doLineTrailer, @Nonnull List<String> beforeWhile) {
		static final CarriedText NONE = new CarriedText("", List.of());
	}

	/**
	 * A do-while's terminator as the reshaped statement re-emits it.
	 *
	 * @param line    line the closing {@code while} sits on
	 * @param index   index of the keyword on that line
	 * @param before  what shares that line ahead of the clause and cannot ride
	 *                with it, i.e. the body's own tail when the {@code while} is
	 *                cuddled onto it. Empty when only whitespace or a comment
	 *                precedes the keyword, which the clause keeps instead
	 * @param clause  the text re-emitted as the statement's terminator
	 */
	private record Terminator(int line, int index, @Nonnull String before, @Nonnull String clause) {}

	/**
	 * Where the one-liner body stops on the keyword line: at the {@code else}
	 * that continues the statement, else where the body's span ends, else at the
	 * end of the line (nothing follows the body there).
	 */
	@CheckReturnValue
	private static int bodyEnd(@Nonnull String line, int bodyStart, @Nonnull OneLinerBody body) {
		final var elseStart = LineText.charIndexOfColumn(line, body.elseColumn());
		if (elseStart > bodyStart)
			return elseStart;
		final var semiEnd = LineText.charIndexOfColumn(line, body.endColumn());
		return semiEnd > bodyStart ? semiEnd : line.length();
	}

	/**
	 * Where a multi-line one-liner body stops on its last line. The line's own
	 * length means nothing follows the body there.
	 */
	@CheckReturnValue
	private static int bodyEndOnLastLine(@Nonnull String endText, @Nonnull OneLinerBody body, int lastLine) {
		if (body.endLine() != lastLine)
			return endText.length();
		final var end = LineText.charIndexOfColumn(endText, body.endColumn());
		return end < 0 ? endText.length() : end;
	}

	/**
	 * Whether every bracket opened in {@code masked} between {@code openLine} and
	 * {@code closeLine} also closes there. The brace-removal paths locate the
	 * block's {@code &#125;} by taking the next line that starts with one, which is
	 * only the block's own when the body in between is self-contained: an
	 * anonymous class or array initializer split across lines puts its own
	 * {@code &#125;} first, and dropping that line would destroy it. The check's
	 * one-statement-one-line invariant rules this out for the buffer it saw, but a
	 * sibling fixer may have reshaped the body since.
	 */
	@CheckReturnValue
	private static boolean bodyIsBalanced(@Nonnull List<String> masked, int openLine, int closeLine) {
		var depth = 0;
		for (var i = openLine + 1; i < closeLine; ++i) {
			for (var j = 0; j < masked.get(i).length(); ++j) {
				switch (masked.get(i).charAt(j)) {
					case '(', '[', '{' -> ++depth;
					case ')', ']', '}' -> --depth;
					default -> { }
				}
				if (depth < 0)
					return false;
			}
		}
		return depth == 0;
	}

	/**
	 * Appends {@code &#123;} to the keyword line and closes the block after the body
	 * lines {@code bodyStart..bodyEnd}. {@code endIndex} is where the body stops on
	 * its last line: code past it is a sibling of the keyword and is re-emitted
	 * after the closing brace, while a comment there annotates the body and rides
	 * along inside the braces.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt braceBody(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			int bodyStart,
			int bodyEnd,
			int endIndex
	) {
		// a literal can open after the body's terminating `;`, which would put the
		// emitted `}` inside it
		if (leavesLiteralOpen(lines, lineIndex, bodyEnd))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);

		final var lastLine = lines.get(bodyEnd);
		final var trailing = endIndex <= lastLine.length() ? lastLine.substring(endIndex) : "";
		final var trailingIsCode = !trailing.isBlank() && !isCommentOnly(trailing);

		final var keywordLine = lines.get(lineIndex);
		final var commentIdx = findTrailingComment(keywordLine, entryStateAt(lines, lineIndex));
		final var result = new ArrayList<String>();
		if (commentIdx >= 0)
			result.add(keywordLine.substring(0, commentIdx).stripTrailing() + " { " + keywordLine.substring(commentIdx));
		else
			result.add(keywordLine + " {");
		for (var i = bodyStart; i < bodyEnd; ++i)
			result.add(lines.get(i));
		result.add(trailingIsCode ? lastLine.substring(0, endIndex).stripTrailing() : lastLine);
		result.add(indent + "}");
		if (trailingIsCode)
			result.add(indent + trailing.strip());
		return new FixResult(lineIndex, bodyEnd, result);
	}

	@Nullable
	private static FixAttempt buildTierResult(
			int tier,
			@Nonnull String bodyText,
			@Nonnull String whileClause,
			int startLine,
			int endLine,
			@Nonnull String indent,
			@Nonnull CarriedText carried
	) {
		if (bodyText.isBlank() || isCommentOnly(bodyText))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_COMMENT_ONLY);

		// a text block spans lines the reshaped body cannot carry
		if (JavaLineScanner.stateAfter(bodyText, JavaLineScanner.LexerState.NONE).inTextBlock())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_TEXT_BLOCK);

		final var bodyCode = maskLiteralsAndComments(bodyText).strip();

		if (!bodyCode.endsWith(";") && !bodyCode.endsWith("}"))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_NO_SEMICOLON);

		final var trailer = carried.doLineTrailer();
		final var result = new ArrayList<String>();
		switch (tier) {
			case 2 -> {
				// tier 2 puts the body on the do line, so a trailer would land behind
				// the body's own line comment and be swallowed by it
				if (!trailer.isEmpty() && findTrailingComment(bodyText) >= 0)
					return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
				result.add(indent + "do " + bodyText + (trailer.isEmpty() ? "" : " " + trailer));
			}
			default -> {
				result.add(indent + "do" + (trailer.isEmpty() ? "" : " " + trailer));
				result.add(indent + "\t" + bodyText);
			}
		}
		result.addAll(carried.beforeWhile());
		result.add(indent + whileClause);
		return new FixResult(startLine, endLine, result);
	}

	/**
	 * The lexer state {@code lineIndex} begins in. Threaded from the top rather
	 * than read off the line above alone, since a line reading {@code """;} in
	 * isolation looks like an opener when it is really a closer.
	 */
	@CheckReturnValue
	@Nonnull
	private static LexerState entryStateAt(@Nonnull List<String> lines, int lineIndex) {
		var state = LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		return state;
	}

	@CheckReturnValue
	private static int findTrailingComment(@Nonnull String line) {
		return JavaLineScanner.firstLineComment(line, JavaLineScanner.LexerState.NONE);
	}

	/**
	 * As {@link #findTrailingComment(String)}, for a whole line whose entry state
	 * may carry a block comment or text block from above. A cold scan would read
	 * that carried content as code and miss the comment the line really opens.
	 */
	@CheckReturnValue
	private static int findTrailingComment(@Nonnull String line, @Nonnull LexerState state) {
		return JavaLineScanner.firstLineComment(line, state);
	}

	/**
	 * First line after the keyword that carries anything. Scanned rather than taken
	 * from the AST so a comment sitting between the header and the statement is
	 * wrapped with the body instead of stranded.
	 */
	@CheckReturnValue
	private static int firstBodyLine(@Nonnull List<String> lines, int lineIndex) {
		var bodyStart = lineIndex + 1;
		while (bodyStart < lines.size() && lines.get(bodyStart).isBlank())
			++bodyStart;
		return bodyStart < lines.size() ? bodyStart : -1;
	}

	@Nullable
	private static FixAttempt fixBracedBody(
			@Nonnull List<String> lines,
			int lineIndex,
			int braceLine,
			@Nonnull String indent,
			@Nonnull DoWhileShape shape,
			@Nonnull String doLineTrailer
	) {
		// searched on a masked buffer so a `}` inside a string, comment, or text block
		// is not mistaken for the block's own brace
		final var masked = JavaLineScanner.maskAll(lines);
		var closeBraceLine = -1;
		for (var i = braceLine + 1; i < lines.size(); ++i) {
			if (masked.get(i).stripLeading().startsWith("}")) {
				closeBraceLine = i;
				break;
			}
		}
		if (closeBraceLine < 0)
			return null;

		// whatever follows the `{` goes with the line when it is rebuilt: a comment
		// travels to the new `do` line, but a statement there has nowhere to land
		final var openLine = lines.get(braceLine);
		final var afterBrace = openLine.substring(openLine.indexOf('{') + 1).strip();
		// the block comment or text block runs on into the body below, so the text
		// there is its content and carrying the opener up would swallow the rest of
		// the file
		if (opensUnterminatedLiteral(afterBrace))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);
		if (!afterBrace.isEmpty() && !isCommentOnly(afterBrace))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);

		// the `do` line's own comment and the brace line's cannot both ride the single
		// reshaped `do` line: a `//` trailer would swallow whatever follows it
		if (!doLineTrailer.isEmpty() && !afterBrace.isEmpty())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
		final var trailer = doLineTrailer.isEmpty() ? afterBrace : doLineTrailer;

		final var bodyLines = new ArrayList<String>();
		for (var i = braceLine + 1; i < closeBraceLine; ++i)
			bodyLines.add(lines.get(i));

		// empty body has no statement to keep; emitting `do\n\twhile(...)` would be invalid Java
		if (bodyLines.isEmpty())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_EMPTY_BODY);

		// The check flags unnecessary braces only on a single-line braced body; a multi-line braced
		// body is check-clean. Removing its braces would leave a multi-line braceless body the check
		// would then flag as missing braces, so refuse rather than corrupt it.
		if (bodyLines.size() > 1)
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_MULTILINE_BRACED);

		final var bodyStripped = bodyLines.getFirst().stripLeading();
		if (isVariableDeclaration(bodyStripped))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_DECLARATION_BODY);

		final var closeLineStripped = lines.get(closeBraceLine).stripLeading();
		// the `}` was found on the masked buffer, so a close line that begins inside a
		// multi-line literal carries raw text ahead of it that every index below,
		// which assumes char 0 is that brace, would misread
		if (!JavaLineScanner.opensWith(lines.get(closeBraceLine), masked.get(closeBraceLine), '}'))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
		if (!bodyIsBalanced(masked, braceLine, closeBraceLine))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_CLOSE_BRACE);
		// searched on a masked copy so a `while` inside a comment or literal on the
		// close-brace line is not mistaken for the condition; the mask preserves
		// columns, so the index addresses `closeLineStripped` itself. The opensWith
		// guard above put the `}` at index 0, so a hit is never 0 and the substring
		// ranges below are always valid
		final var whileIdx = LineText.indexOfWord(maskLiteralsAndComments(closeLineStripped), "while");
		// the close-brace line is dropped whole, so whatever shares it with the `}`
		// has to be re-emitted; only a comment can be, code there would have no home
		final var afterClose = (whileIdx < 0
				? closeLineStripped.substring(1)
				: closeLineStripped.substring(1, whileIdx)).strip();
		// the block comment or text block runs on past the close-brace line, so
		// re-emitting just its opener ahead of the `while` would strand the text it
		// covers
		if (opensUnterminatedLiteral(afterClose))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);
		if (!afterClose.isEmpty() && !isCommentOnly(afterClose))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);

		final var carriedLines = new ArrayList<String>();
		// a comment between the keyword and its own-line `{` sits inside the replaced
		// range, so it has to travel too
		carriedLines.addAll(keptLines(lines, lineIndex, braceLine));
		if (!afterClose.isEmpty())
			carriedLines.add(indent + afterClose);

		if (whileIdx < 0) {
			final var nextWhile = shape.whileLine();
			if (nextWhile <= closeBraceLine)
				return null;
			carriedLines.addAll(keptLines(lines, closeBraceLine, nextWhile));
			return buildTierResult(
					shape.tier(),
					bodyStripped,
					lines.get(nextWhile).stripLeading(),
					lineIndex,
					nextWhile,
					indent,
					new CarriedText(trailer, carriedLines)
			);
		}
		return buildTierResult(
				shape.tier(),
				bodyStripped,
				closeLineStripped.substring(whileIdx),
				lineIndex,
				closeBraceLine,
				indent,
				new CarriedText(trailer, carriedLines)
		);
	}

	/**
	 * Wraps a multi-line braceless do-while body in braces. The {@code while} can
	 * be cuddled onto the body's last line, in which case that line splits: the
	 * body part stays where it is and the terminator moves down to the added
	 * closing brace.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixMissingBraces(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull String doLineTrailer,
			@Nullable Terminator terminator
	) {
		if (terminator == null || terminator.line() <= lineIndex)
			return null;

		final var result = new ArrayList<String>();
		result.add(indent + "do {" + (doLineTrailer.isEmpty() ? "" : " " + doLineTrailer));
		for (var i = lineIndex + 1; i < terminator.line(); ++i)
			result.add(lines.get(i));
		if (!terminator.before().isBlank())
			result.add(terminator.before());
		result.add(indent + "} " + terminator.clause());
		return new FixResult(lineIndex, terminator.line(), result);
	}

	@Nullable
	private static FixAttempt fixNonDoWhileBraceOnOwnLine(
			@Nonnull List<String> lines,
			int lineIndex,
			int braceLine,
			@Nonnull String indent
	) {
		// the pipeline bounds-checks the violation's own line, not one
		// derived from the AST
		if (braceLine < 0 || braceLine >= lines.size())
			return null;

		final var masked = JavaLineScanner.maskAll(lines);
		final var bodyLine = JavaLineScanner.nextCodeLine(masked, braceLine + 1);
		if (bodyLine < 0)
			return null;

		if (isVariableDeclaration(lines.get(bodyLine).stripLeading()))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_DECLARATION_BODY);

		// anything sharing the brace line goes with it when the line is dropped,
		// including a comment the line-comment scan does not see. A line the AST
		// placed the block on but whose text does not open with `{` cannot be read
		// that way at all
		if (!JavaLineScanner.opensWith(lines.get(braceLine), masked.get(braceLine), '{')
				|| !lines.get(braceLine).stripLeading().substring(1).isBlank())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);

		final var closeBrace = JavaLineScanner.nextCodeLine(masked, bodyLine + 1);
		if (closeBrace < 0)
			return null;

		final var closeMasked = masked.get(closeBrace);
		final var closeStripped = lines.get(closeBrace).stripLeading();
		if (!closeMasked.stripLeading().startsWith("}"))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_CLOSE_BRACE);
		if (!JavaLineScanner.opensWith(lines.get(closeBrace), closeMasked, '}'))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
		if (!bodyIsBalanced(masked, bodyLine - 1, closeBrace))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_CLOSE_BRACE);

		final var result = new ArrayList<String>();
		result.add(lines.get(lineIndex));
		// the replaced range covers the lines around the body, and the masked scan
		// steps over comment-only ones, so they have to be re-emitted
		result.addAll(keptLines(lines, lineIndex, braceLine));
		result.addAll(keptLines(lines, braceLine, bodyLine));
		result.add(lines.get(bodyLine));
		result.addAll(keptLines(lines, bodyLine, closeBrace));

		final var afterBrace = closeStripped.substring(1).stripLeading();
		if (!afterBrace.isEmpty())
			result.add(indent + afterBrace);

		return new FixResult(lineIndex, closeBrace, result);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixNonDoWhileFromAst(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull ControlBody body
	) {
		// every handler below rewrites the keyword line and takes the body from the
		// lines after it, so a header that has not closed by then would be spliced
		// mid-condition
		if (body.headerLine() != lineIndex)
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_MULTILINE_HEADER);
		if (!body.block())
			return fixNonDoWhileMissingBracesFromAst(lines, lineIndex, indent, body);
		return body.line() == lineIndex
				? fixNonDoWhileUnnecessaryBraces(lines, lineIndex, LineText.charIndexOfColumn(lines.get(lineIndex), body.column()), indent)
				: fixNonDoWhileBraceOnOwnLine(lines, lineIndex, body.line(), indent);
	}

	/**
	 * Wraps a braceless multi-line body in braces, taking its extent from the
	 * check's AST. The parser decides which {@code else} belongs inside the body
	 * and which binds to an enclosing {@code if}, so no text rule has to.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixNonDoWhileMissingBracesFromAst(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull ControlBody body
	) {
		// the appended `{` would land inside a block comment or text block still open
		// after the keyword line, leaving the emitted `}` unmatched
		if (leavesLiteralOpen(lines, lineIndex, lineIndex))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);

		final var bodyStart = firstBodyLine(lines, lineIndex);
		final var bodyEnd = body.lastLine();
		if (bodyStart < 0 || bodyEnd <= bodyStart || bodyEnd >= lines.size())
			return null;

		final var lastLine = lines.get(bodyEnd);
		// with no terminating `;` and nothing resuming on the body's last line, the
		// body owns the rest of it
		final var endIndex = body.endLine() == bodyEnd
				? LineText.charIndexOfColumn(lastLine, body.endColumn())
				: lastLine.length();
		return endIndex < 0 ? null : braceBody(lines, lineIndex, indent, bodyStart, bodyEnd, endIndex);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixNonDoWhileOneLiner(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull OneLinerBody body
	) {
		final var line = lines.get(lineIndex);
		final var bodyStart = LineText.charIndexOfColumn(line, body.column());
		final var lastLine = Math.max(body.lastLine(), lineIndex);
		if (bodyStart <= 0 || bodyStart >= line.length() || lastLine >= lines.size())
			return null;

		final var multiLine = lastLine > lineIndex;
		// only the multi-line shape emits a closing brace, which a literal still open
		// after the body's last line would swallow
		if (multiLine && leavesLiteralOpen(lines, lineIndex, lastLine))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);

		final var endLine = multiLine ? lastLine : lineIndex;
		final var endText = lines.get(endLine);
		final var bodyEnd = multiLine ? bodyEndOnLastLine(endText, body, lastLine) : bodyEnd(line, bodyStart, body);

		final var trailing = endText.substring(bodyEnd);
		// a comment after the body annotates it, so it travels with the body; real
		// code after it (a second statement, or the `else` of an if written on one
		// line) is a sibling of the keyword and belongs at the keyword's indent
		final var trailingIsCode = !trailing.isBlank() && !isCommentOnly(trailing);
		final var bodyText = (!multiLine && trailingIsCode
				? line.substring(bodyStart, bodyEnd)
				: line.substring(bodyStart)).stripTrailing();
		final var head = line.substring(0, bodyStart).stripTrailing();
		final var result = new ArrayList<String>();
		result.add(multiLine ? head + " {" : head);
		result.add(indent + '\t' + bodyText);
		for (var i = lineIndex + 1; i <= lastLine; ++i) {
			// indenting a blank line would leave whitespace-only content behind
			final var continuation = i == lastLine && trailingIsCode
					? endText.substring(0, bodyEnd).stripTrailing()
					: lines.get(i);
			result.add(continuation.isBlank() ? continuation : '\t' + continuation);
		}
		if (multiLine)
			result.add(indent + '}');
		if (trailingIsCode)
			result.add(indent + trailing.strip());

		return new FixResult(lineIndex, lastLine, result);
	}

	@Nullable
	private static FixAttempt fixNonDoWhileUnnecessaryBraces(
			@Nonnull List<String> lines,
			int lineIndex,
			int braceIdx,
			@Nonnull String indent
	) {
		final var line = lines.get(lineIndex);
		if (braceIdx < 0 || braceIdx >= line.length() || line.charAt(braceIdx) != '{')
			return null;

		// a line comment after the `{` documents the statement, not the block, so it
		// stays on the keyword line rather than being dropped with the brace
		final var commentIdx = findTrailingComment(line, entryStateAt(lines, lineIndex));
		// anything else after the `{` is code (or a block comment the line-comment
		// scan does not see), and rebuilding the keyword line would drop it
		if (!(commentIdx > braceIdx ? line.substring(braceIdx + 1, commentIdx) : line.substring(braceIdx + 1)).isBlank())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);

		final var keywordLine = commentIdx > braceIdx
				? line.substring(0, braceIdx).stripTrailing() + " " + line.substring(commentIdx).stripTrailing()
				: line.substring(0, braceIdx).stripTrailing();

		final var masked = JavaLineScanner.maskAll(lines);
		final var bodyLine = JavaLineScanner.nextCodeLine(masked, lineIndex + 1);
		if (bodyLine < 0)
			return null;

		if (isVariableDeclaration(lines.get(bodyLine).stripLeading()))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_DECLARATION_BODY);

		final var closeBrace = JavaLineScanner.nextCodeLine(masked, bodyLine + 1);
		if (closeBrace < 0)
			return null;

		final var closeMasked = masked.get(closeBrace);
		final var closeStripped = lines.get(closeBrace).stripLeading();
		if (!closeMasked.stripLeading().startsWith("}"))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_CLOSE_BRACE);
		if (!JavaLineScanner.opensWith(lines.get(closeBrace), closeMasked, '}'))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
		if (!bodyIsBalanced(masked, bodyLine - 1, closeBrace))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_CLOSE_BRACE);

		final var result = new ArrayList<String>();
		result.add(keywordLine);
		// the brace lines go away but anything between them does not: the masked scan
		// steps over comment-only lines, and the replacement range covers them
		result.addAll(keptLines(lines, lineIndex, bodyLine));
		result.add(lines.get(bodyLine));
		result.addAll(keptLines(lines, bodyLine, closeBrace));

		final var afterBrace = closeStripped.substring(1).stripLeading();
		if (!afterBrace.isEmpty())
			result.add(indent + afterBrace);

		return new FixResult(lineIndex, closeBrace, result);
	}

	@Nullable
	private static FixAttempt fixOnDoLine(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String line,
			@Nonnull String indent,
			@Nonnull DoWhileShape shape
	) {
		final var doIdx = LineText.charIndexOfColumn(line, column);
		if (doIdx < 0 || !LineText.isWordAt(line, doIdx, "do"))
			return null;
		// the statement is rebuilt from the keyword's indent alone, so a do-while
		// written ahead of this one, enclosing or sibling, would be dropped
		if (!line.substring(0, doIdx).isBlank())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_NESTED_DO);

		final var afterDo = line.substring(doIdx + 2).stripLeading();
		final var terminator = terminatorAt(lines, shape);
		if (terminator != null && terminator.line() == lineIndex)
			return fixOnDoLineWhileSameLine(line, line.length() - afterDo.length(), terminator, lineIndex, indent, shape.tier());

		return fixOnDoLineWhileNextLine(lines, afterDo, lineIndex, indent, shape, terminator);
	}

	@Nullable
	private static FixAttempt fixOnDoLineWhileNextLine(
			@Nonnull List<String> lines,
			@Nonnull String afterDo,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull DoWhileShape shape,
			@Nullable Terminator terminator
	) {
		final var bodyText = afterDo.stripTrailing();
		final var whileLine = shape.whileLine();
		if (whileLine < 0)
			return null;
		// the body opened on the `do` line, so code sharing the terminator's line is
		// the tail of a body that spans lines: the tier forms put the body on one
		// line, which cannot carry it
		if (terminator != null && !terminator.before().isBlank())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_WHILE_LINE_BODY);
		return buildTierResult(
				shape.tier(),
				bodyText,
				lines.get(whileLine).stripLeading(),
				lineIndex,
				whileLine,
				indent,
				new CarriedText("", keptLines(lines, lineIndex, whileLine))
		);
	}

	@Nullable
	private static FixAttempt fixOnDoLineWhileSameLine(
			@Nonnull String line,
			int bodyIdx,
			@Nonnull Terminator terminator,
			int lineIndex,
			@Nonnull String indent,
			int tier
	) {
		if (terminator.index() < bodyIdx)
			return null;
		return buildTierResult(
				tier,
				line.substring(bodyIdx, terminator.index()).stripTrailing(),
				terminator.clause(),
				lineIndex,
				lineIndex,
				indent,
				CarriedText.NONE
		);
	}

	@Nullable
	private static FixAttempt fixOwnLine(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent,
			@Nonnull DoWhileShape shape,
			@Nonnull String doLineTrailer
	) {
		final var bodyStart = firstBodyLine(lines, lineIndex);
		if (bodyStart < 0)
			return null;

		// a comment between the `do` and its own-line `{` still leaves a braced body:
		// treating the comment as the body would brace the existing block a second time
		final var masked = JavaLineScanner.maskAll(lines);
		final var braceLine = JavaLineScanner.nextCodeLine(masked, bodyStart);
		if (braceLine >= 0 && masked.get(braceLine).stripLeading().startsWith("{")) {
			// the `{` shares its line with a comment ahead of it, so every index below
			// (which assumes the brace is the line's first char) would misread it
			if (!JavaLineScanner.opensWith(lines.get(braceLine), masked.get(braceLine), '{'))
				return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_BRACE_LINE_CONTENT);
			return fixBracedBody(lines, lineIndex, braceLine, indent, shape, doLineTrailer);
		}

		final var terminator = terminatorAt(lines, shape);
		final var whileLine = shape.whileLine();
		// a terminator cuddled onto the body's last line leaves that line part of the
		// body, so the body runs one line further than the `while` line alone says
		final var lastBodyLine = shape.whileOnBodyLine() ? whileLine : whileLine - 1;
		if (lastBodyLine < bodyStart)
			return null;

		// a blank line before the `while` does not make the body multi-line
		var bodyEnd = lastBodyLine;
		while (bodyEnd > bodyStart && lines.get(bodyEnd).isBlank())
			--bodyEnd;
		if (bodyEnd > bodyStart)
			return fixMissingBraces(lines, lineIndex, indent, doLineTrailer, terminator);

		return buildTierResult(
				shape.tier(),
				lastBodyLine == whileLine && terminator != null
						? terminator.before().stripLeading()
						: lines.get(bodyStart).stripLeading(),
				terminator != null ? terminator.clause() : lines.get(whileLine).stripLeading(),
				lineIndex,
				whileLine,
				indent,
				new CarriedText(doLineTrailer, keptLines(lines, bodyStart, whileLine))
		);
	}

	/**
	 * Returns whether the body has no real statement: comment-only, possibly with a
	 * trailing empty {@code ;}. An unterminated block comment blanks the rest of the
	 * text, so it reads as comment-only too.
	 */
	@CheckReturnValue
	private static boolean isCommentOnly(@Nonnull String bodyText) {
		final var code = maskLiteralsAndComments(bodyText).strip();
		return code.isEmpty() || code.equals(";");
	}

	/**
	 * Whether the whitespace at {@code i} sits inside a qualified name, i.e. a
	 * {@code .} follows or precedes it across the run of spaces.
	 */
	@CheckReturnValue
	private static boolean isDotSpacing(@Nonnull String s, int i) {
		if (s.charAt(i) != ' ' && s.charAt(i) != '\t')
			return false;
		var next = i + 1;
		while (next < s.length() && (s.charAt(next) == ' ' || s.charAt(next) == '\t'))
			++next;
		if (next < s.length() && s.charAt(next) == '.')
			return true;
		var prev = i;
		do --prev;
		while (prev >= 0 && (s.charAt(prev) == ' ' || s.charAt(prev) == '\t'));
		return prev >= 0 && s.charAt(prev) == '.';
	}

	/**
	 * Whether {@code masked} carries a non-do-while control-flow keyword as a whole
	 * token. Searched across the line rather than anchored at its start: the check
	 * reports at the keyword's own column, so a cuddled {@code } else --x;} has its
	 * keyword mid-line and would otherwise read as keywordless.
	 */
	@CheckReturnValue
	private static boolean isNonDoWhileKeyword(@Nonnull String masked) {
		return LineText.indexOfWord(masked, "else") >= 0
				|| LineText.indexOfWord(masked, "for") >= 0
				|| LineText.indexOfWord(masked, "if") >= 0
				|| LineText.indexOfWord(masked, "while") >= 0;
	}

	@CheckReturnValue
	private static boolean isVariableDeclaration(@Nonnull String stripped) {
		// masking blanks comment and literal content first: a leading `/* note */`
		// would otherwise hide the declaration behind it, an apostrophe inside a
		// comment would open a char literal that swallows the rest of the line, and
		// a paren inside a literal would be counted as nesting
		var s = maskLiteralsAndComments(stripped).strip();
		// skip leading annotations (e.g., @SuppressWarnings("unused") int x = 5;),
		// including qualified names, whose dots would otherwise end the name scan
		// and leave a leading `.` that no longer parses as a declaration
		while (s.startsWith("@")) {
			var j = 1;
			while (j < s.length() && (s.charAt(j) == '.' || isDotSpacing(s, j) || LineText.identEnd(s, j) > j))
				j = s.charAt(j) == '.' || isDotSpacing(s, j) ? j + 1 : LineText.identEnd(s, j);
			j = pastGroup(s, j, '(', ')');
			s = s.substring(j).strip();
		}

		if (LineText.startsWithSeparatedWord(s, "final") || LineText.startsWithSeparatedWord(s, "var"))
			return true;

		final var end = LineText.identEnd(s, 0);
		if (end == 0 || end >= s.length())
			return false;

		final var firstWord = s.substring(0, end);

		return switch (firstWord) {
			case "assert", "break", "case", "continue", "default", "do", "else", "for",
			     "if", "new", "return", "super", "switch", "synchronized", "this",
			     "throw", "try", "while", "yield" -> false;
			default -> {
				var i = end;
				while (i < s.length() && s.charAt(i) == '.')
					i = LineText.identEnd(s, i + 1);
				i = pastGroup(s, i, '<', '>');
				while (i + 1 < s.length() && s.charAt(i) == '[' && s.charAt(i + 1) == ']')
					i += 2;
				var afterType = i;
				while (afterType < s.length() && Character.isWhitespace(s.charAt(afterType)))
					++afterType;
				yield afterType > i && afterType < s.length()
						&& Character.isJavaIdentifierStart(s.codePointAt(afterType));
			}
		};
	}

	/**
	 * The lines strictly between {@code from} and {@code to} that carry content.
	 * Blank ones are dropped: the reshaped statement supplies its own line breaks,
	 * so keeping them would leave stray gaps inside it. A blank line reached while a
	 * block comment or text block is open is that literal's own content rather than a
	 * gap, so it is kept.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> keptLines(@Nonnull List<String> lines, int from, int to) {
		var state = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i <= from; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);

		final var kept = new ArrayList<String>();
		for (var i = from + 1; i < to; ++i) {
			final var line = lines.get(i);
			if (!line.isBlank() || state.inMultilineLiteral())
				kept.add(line);
			state = JavaLineScanner.stateAfter(line, state);
		}
		return kept;
	}

	/**
	 * Whether a block comment or text block is still open after the lines
	 * {@code from..to}. State is threaded from the top of the buffer rather than
	 * started cold at {@code from}: a line reading {@code """;} in isolation looks
	 * like an opener when it is really a closer, and {@code from} itself may sit
	 * inside a literal opened above, whose carried content a cold lexer reads as
	 * code.
	 */
	@CheckReturnValue
	private static boolean leavesLiteralOpen(@Nonnull List<String> lines, int from, int to) {
		var state = entryStateAt(lines, from);
		for (var i = from; i <= to; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		return state.inMultilineLiteral();
	}

	/**
	 * Blanks every literal and comment in {@code s}, keeping length and column
	 * alignment.
	 */
	@CheckReturnValue
	@Nonnull
	private static String maskLiteralsAndComments(@Nonnull String s) {
		return JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
	}

	/**
	 * Whether a block comment or text block is still open where {@code lineIndex}
	 * begins, so the text there is that literal's content rather than code.
	 */
	@CheckReturnValue
	private static boolean opensInsideLiteral(@Nonnull List<String> lines, int lineIndex) {
		return entryStateAt(lines, lineIndex).inMultilineLiteral();
	}

	/**
	 * Whether {@code s} opens a block comment or text block that does not close
	 * within it, so the text below continues inside that literal.
	 */
	@CheckReturnValue
	private static boolean opensUnterminatedLiteral(@Nonnull String s) {
		final var state = JavaLineScanner.stateAfter(s, JavaLineScanner.LexerState.NONE);
		return state.inMultilineLiteral();
	}

	/**
	 * Index just past the group {@code open} opens at {@code start}, or {@code start}
	 * itself when the character there is not {@code open}. A group left unclosed runs
	 * to the end of {@code s}, so a truncated annotation or type argument list
	 * consumes the rest of the line rather than being re-scanned as declaration text.
	 */
	@CheckReturnValue
	private static int pastGroup(@Nonnull String s, int start, char open, char close) {
		if (start >= s.length() || s.charAt(start) != open)
			return start;
		var depth = 1;
		var i = start + 1;
		while (i < s.length() && depth > 0) {
			if (s.charAt(i) == open)
				++depth;
			else if (s.charAt(i) == close)
				--depth;
			++i;
		}
		return i;
	}

	/**
	 * Splits the terminator's line around the {@code while} the check's AST
	 * located, or {@code null} when a sibling fix in this pass has since moved that
	 * position out of the buffer.
	 */
	@CheckReturnValue
	@Nullable
	private static Terminator terminatorAt(@Nonnull List<String> lines, @Nonnull DoWhileShape shape) {
		if (shape.whileLine() < 0 || shape.whileLine() >= lines.size())
			return null;
		final var text = lines.get(shape.whileLine());
		final var index = LineText.charIndexOfColumn(text, shape.whileColumn());
		if (index < 0)
			return null;
		final var before = text.substring(0, index).stripTrailing();
		// a comment ahead of the keyword annotates the statement, so it rides on the
		// re-emitted clause rather than being stranded on a line of its own
		return isCommentOnly(before)
				? new Terminator(shape.whileLine(), index, "", text.stripLeading())
				: new Terminator(shape.whileLine(), index, before, text.substring(index));
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var stripped = line.stripLeading();
		final var indent = LineText.extractIndent(line);

		final var root = FixerAst.parseOrNull(lines);
		final var entryState = entryStateAt(lines, lineIndex);

		// a line whose carried comment content opens with `do ` is not a do-while: the
		// keyword text is literal content, and routing there drops a fixable violation
		if (entryState.inMultilineLiteral() || !LineText.startsWithWord(stripped, "do")) {
			if (root != null) {
				final var oneLiner = ControlFlowBracesCheck.oneLinerBodyAt(root, lineIndex, column);
				if (oneLiner != null)
					return fixNonDoWhileOneLiner(lines, lineIndex, indent, oneLiner);

				final var body = ControlFlowBracesCheck.bodyAt(root, lineIndex, column);
				if (body != null)
					return fixNonDoWhileFromAst(lines, lineIndex, indent, body);
			}
			// Every body span comes from the check's own AST classifier, so a buffer that
			// does not parse cannot have produced a violation in the first place, and a
			// parsed buffer with no node at the position means an earlier fix in this pass
			// moved it; they are reported apart because only the second is a stale coordinate.
			if (!isNonDoWhileKeyword(JavaLineScanner.stripCommentsAndStrings(line, entryState)))
				return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_NO_KEYWORD);
			return new SkipResult(root == null
					? SkipMessages.CONTROL_FLOW_SKIP_NO_BODY
					: SkipMessages.CONTROL_FLOW_SKIP_STALE_POSITION
			);
		}

		final var shape = root == null ? null : ControlFlowBracesCheck.shapeAt(root, lineIndex, column);
		// the do-while route reads every span off the shape; without one it can only
		// refuse. Split for the same reason the non-do-while side is: a parsed buffer with
		// no shape at the position is a stale coordinate, not a missing parse
		if (shape == null) {
			return new SkipResult(root == null
					? SkipMessages.CONTROL_FLOW_SKIP_NO_TIER
					: SkipMessages.CONTROL_FLOW_SKIP_STALE_POSITION
			);
		}
		// every reshape re-emits the terminator's line at the keyword's indent, so a
		// line that begins inside a block comment or text block would have its
		// content moved away from the opener it belongs to
		if (opensInsideLiteral(lines, shape.whileLine()))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);

		// the separator after `do` is optional (`do{` is legal), so the shape is
		// decided by what follows the keyword rather than by a fixed prefix
		final var afterKeyword = stripped.substring(2).stripLeading();
		if (afterKeyword.startsWith("{"))
			return fixBracedBody(lines, lineIndex, lineIndex, indent, shape, "");

		// a text block gets the more specific reason: it cannot be carried onto the
		// reshaped line at all, not merely "it opened here"
		final var afterState = JavaLineScanner.stateAfter(afterKeyword, JavaLineScanner.LexerState.NONE);
		if (afterState.inTextBlock())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_TEXT_BLOCK);
		if (afterState.inBlockComment())
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_UNTERMINATED_LITERAL);

		if (afterKeyword.isEmpty() || isCommentOnly(afterKeyword))
			return fixOwnLine(lines, lineIndex, indent, shape, afterKeyword);

		return fixOnDoLine(lines, lineIndex, column, line, indent, shape);
	}
}