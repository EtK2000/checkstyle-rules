package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.format.SpanReformat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class RedundantAnnotationSyntaxFixer implements CheckstyleFixer {
	/**
	 * A run of whitespace and comments with no code: the block comments (and an
	 * optional trailing {@code //} line comment) in order, plus whether the run
	 * ends with a line comment. When folded onto one line, a line comment must be
	 * the last content (it swallows the rest of the physical line), so callers
	 * inspect {@link #endsWithLineComment} to reject a swallow.
	 */
	private record CommentRun(@Nonnull List<String> comments, boolean endsWithLineComment) {}

	private static final Pattern EMPTY_PARENS = Pattern.compile("(@\\w[\\w.]*)\\s*\\(\\s*\\)");
	private static final Pattern EXPLICIT_VALUE = Pattern.compile("\\(\\s*value\\s*=\\s*");
	private static final Pattern EXPLICIT_VALUE_LINE = Pattern.compile("\\s*value\\s*=\\s*");

	/**
	 * Parses {@code text} as a {@link CommentRun}: whitespace, block comments, and
	 * at most one trailing {@code //} line comment, with no code. Returns
	 * {@code null} when any code (a string, identifier, ...) is present. The check
	 * fires on comment-only parentheses too (comments aren't AST nodes), so this
	 * both decides whether the parentheses are truly empty and yields any comment
	 * to re-emit after the annotation name when the parentheses are removed. A
	 * {@code /*} without a closing {@code *}{@code /} on {@code text} is captured
	 * verbatim.
	 */
	@CheckReturnValue
	@Nullable
	private static CommentRun commentRun(@Nonnull String text) {
		final var comments = new ArrayList<String>();
		var i = 0;
		while (i < text.length()) {
			final var c = text.charAt(i);
			if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '*') {
				final var start = i;
				i += 2;
				while (i + 1 < text.length() && !(text.charAt(i) == '*' && text.charAt(i + 1) == '/'))
					++i;
				i = Math.min(i + 2, text.length());
				comments.add(text.substring(start, i));
			}
			else if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '/') {
				comments.add(text.substring(i));
				return new CommentRun(List.copyOf(comments), true);
			}
			else if (Character.isWhitespace(c))
				++i;
			else
				return null;
		}
		return new CommentRun(List.copyOf(comments), false);
	}

	/**
	 * The lexer state entering {@code lines.get(lineIndex)}, folded over the
	 * preceding lines so a line that continues a multi-line comment or text block
	 * is masked correctly.
	 */
	@CheckReturnValue
	@Nonnull
	private static LexerState entryStateAt(@Nonnull List<String> lines, int lineIndex) {
		var state = LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		return state;
	}

	/**
	 * Net paren depth (opens minus closes) over a masked line.
	 */
	@CheckReturnValue
	private static int parenDepth(@Nonnull String mask) {
		var depth = 0;
		for (var i = 0; i < mask.length(); ++i) {
			final var c = mask.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')')
				--depth;
		}
		return depth;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;
		// Match every syntax pattern on a literal/comment/text-block-aware mask
		// (positions preserved, output spliced from the original line) so a
		// decoy @A() / (value = / value = inside a string or comment is never
		// matched, and continuation lines inside a multi-line comment/text block
		// are masked with the correct entry state.
		final var entryState = entryStateAt(lines, lineIndex);
		final var mask = JavaLineScanner.stripCommentsAndStrings(line, entryState);

		// rule 1: single-line empty parens @A() / @A ( ) -> @A. A comment inside
		// the parens (or between the name and the parens) is preserved by moving it
		// after the name (@A(/* c */) -> @A /* c */), matching the multiline rule's
		// spacing. The mask keeps string/char delimiter quotes, so a real argument
		// like @A("") leaves a non-blank interior that never matches EMPTY_PARENS;
		// commentRun then pulls the comments out of the blank interior (only
		// whitespace and block comments can reach it single-line) to re-emit them.
		final var emptyMatcher = EMPTY_PARENS.matcher(mask);
		if (emptyMatcher.find(column)) {
			final var nameEnd = emptyMatcher.end(1);
			final var openParenIdx = mask.indexOf('(', nameEnd);
			final var closeParenIdx = emptyMatcher.end() - 1;
			final var interiorRun = commentRun(line.substring(openParenIdx + 1, closeParenIdx));
			final var preRun = commentRun(line.substring(nameEnd, openParenIdx));
			if (interiorRun != null && preRun != null) {
				final var comments = new ArrayList<>(preRun.comments());
				comments.addAll(interiorRun.comments());
				final var glue = comments.isEmpty() ? "" : " " + String.join(" ", comments);
				final var tail = line.substring(closeParenIdx + 1);
				// removing the parens would fuse the name (or a folded comment) into the
				// next token: keep a separator when the tail begins with an identifier
				// (@Override()void -> @Override void, @B(/* c */)String -> @B /* c */ String)
				// or another annotation (@A()@B -> @A @B). A tail already starting with
				// whitespace (or a ')' / ',' ...) needs no added space.
				final var needsSpace = !tail.isEmpty()
						&& (Character.isJavaIdentifierPart(tail.charAt(0)) || tail.charAt(0) == '@');
				final var fixed = line.substring(0, nameEnd) + glue + (needsSpace ? " " : "") + tail;
				return new FixResult(lineIndex, lineIndex, List.of(fixed));
			}
		}

		// rule 1: multiline empty parens @A(\n) -> @A. Comment-only lines between the
		// parens are stepped over and their comments folded after the name in source
		// order (open tail, middle lines, close prefix), then any code after the ')'.
		final var maskTrimmed = mask.stripTrailing();
		if (maskTrimmed.endsWith("(") && maskTrimmed.contains("@")) {
			final var openParenIdx = maskTrimmed.length() - 1;
			final var midComments = new ArrayList<String>();
			var spanHasMultilineComment = false;
			var state = JavaLineScanner.stateAfter(line, entryState);
			for (var i = lineIndex + 1; i < lines.size(); ++i) {
				final var raw = lines.get(i);
				final var entryInComment = state.inBlockComment();
				final var exitState = JavaLineScanner.stateAfter(raw, state);
				final var nextMask = JavaLineScanner.stripCommentsAndStrings(raw, state);
				if (!entryInComment && nextMask.stripLeading().startsWith(")")) {
					// A block comment that spanned line boundaries can't be re-emitted on
					// one line, so the (now confirmed) empty-parens annotation is left as-is.
					if (spanHasMultilineComment)
						return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP);
					// closeParenIdx comes from the mask (comments blanked), so the pre-')'
					// text is read from the ORIGINAL line: a leading block comment there
					// (@B(\n/* c */)) is folded in, not sliced off as if ')' were first.
					final var closeParenIdx = nextMask.indexOf(')');
					final var openRun = commentRun(line.substring(openParenIdx + 1));
					final var closeRun = commentRun(raw.substring(0, closeParenIdx));
					if (openRun == null || closeRun == null)
						return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP);
					final var afterParen = raw.substring(closeParenIdx + 1).strip();
					final var comments = new ArrayList<>(openRun.comments());
					comments.addAll(midComments);
					comments.addAll(closeRun.comments());
					// A // line comment folds only as the last content on the merged line:
					// a later comment, or code after the ')', would be swallowed by it.
					for (var k = 0; k < comments.size(); ++k) {
						if (comments.get(k).startsWith("//") && (k < comments.size() - 1 || !afterParen.isEmpty()))
							return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP);
					}
					var fixed = line.substring(0, openParenIdx).stripTrailing();
					if (!comments.isEmpty())
						fixed += " " + String.join(" ", comments);
					if (!afterParen.isEmpty())
						fixed += " " + afterParen;
					return new FixResult(lineIndex, i, List.of(fixed));
				}
				// A line inside a multi-line block comment: record it and keep scanning
				// (the parens may still hold a value, which rule 2 handles); only bail
				// once the ')' confirms the parens are actually empty.
				if (entryInComment || exitState.inBlockComment()) {
					spanHasMultilineComment = true;
					state = exitState;
					continue;
				}
				// A code line between the parens: not empty parens -> break (rule 2 next).
				if (!nextMask.isBlank())
					break;
				// A blank or single-line-comment-only line: collect its comments, step over.
				final var run = commentRun(raw);
				if (run != null)
					midComments.addAll(run.comments());
				state = exitState;
			}
		}

		// rule 2: single-line explicit value key @A(value = x) -> @A(x). A block
		// comment in any gap of the removed key (before 'value', between 'value' and
		// '=', or after '=') is folded in after the '(' rather than dropped. Each gap
		// holds only whitespace and block comments once the mask-based match succeeds.
		final var valueMatcher = EXPLICIT_VALUE.matcher(mask);
		if (valueMatcher.find(column)) {
			final var openParenIdx = valueMatcher.start();
			final var valueKeyIdx = mask.indexOf("value", openParenIdx + 1);
			final var eqIdx = mask.indexOf('=', valueKeyIdx);
			final var preRun = commentRun(line.substring(openParenIdx + 1, valueKeyIdx));
			final var midRun = commentRun(line.substring(valueKeyIdx + "value".length(), eqIdx));
			final var postRun = commentRun(line.substring(eqIdx + 1, valueMatcher.end()));
			final var comments = new ArrayList<String>();
			if (preRun != null)
				comments.addAll(preRun.comments());
			if (midRun != null)
				comments.addAll(midRun.comments());
			if (postRun != null)
				comments.addAll(postRun.comments());
			final var tail = line.substring(valueMatcher.end());
			final var glue = comments.isEmpty() ? "" : String.join(" ", comments) + (tail.isEmpty() ? "" : " ");
			final var fixed = line.substring(0, openParenIdx + 1) + glue + tail;
			return new FixResult(lineIndex, lineIndex, List.of(fixed));
		}

		// rule 2: multiline explicit value key. Collapse the whole annotation onto one
		// line (@A(\n value = x \n) -> @A(x)) when the span has no // comment and no
		// block comment spanning line boundaries, and the collapsed line fits the
		// max width; otherwise fall back to the minimal edit that strips only
		// 'value = ' on its line. Only scan when this annotation's '(' is still open
		// (parenDepth > 0); a closed annotation would corrupt an unrelated following
		// `value = ...` statement.
		if (parenDepth(mask) > 0) {
			final var openTrim = mask.stripTrailing();
			var valueLineIdx = -1;
			var valueMatchEnd = -1;
			var closeLineIdx = -1;
			var closeCol = -1;
			var canCollapse = openTrim.endsWith("(");
			// a // line comment in the open tail (after the '(') would be pulled inline by
			// the collapse and swallow the value, so refuse to collapse (block comments in
			// the open tail are fine to fold). The subsequent-line scan below only sees
			// lines after the open line, so this open-line check is separate.
			if (canCollapse) {
				final var openTail = commentRun(line.substring(openTrim.length()));
				if (openTail == null || openTail.endsWithLineComment())
					canCollapse = false;
			}
			var depth = parenDepth(mask);
			var state = JavaLineScanner.stateAfter(line, entryState);
			for (var i = lineIndex + 1; i < lines.size() && closeLineIdx < 0; ++i) {
				final var raw = lines.get(i);
				// a // comment or a block comment spanning line boundaries in the span
				// can't be one-lined, so the collapse is refused for it (a text block is
				// unreachable here: checkstyle can't parse one as an annotation value)
				final var marker = JavaLineScanner.firstCommentMarker(raw, state);
				if (state.inBlockComment()
						|| (marker >= 0 && marker + 1 < raw.length() && raw.charAt(marker + 1) == '/'))
					canCollapse = false;
				final var exitState = JavaLineScanner.stateAfter(raw, state);
				if (exitState.inBlockComment())
					canCollapse = false;
				final var nextMask = JavaLineScanner.stripCommentsAndStrings(raw, state);
				if (valueLineIdx < 0) {
					final var valueLineMatcher = EXPLICIT_VALUE_LINE.matcher(nextMask);
					if (valueLineMatcher.lookingAt()) {
						valueLineIdx = i;
						valueMatchEnd = valueLineMatcher.end();
					}
					// a non-blank line that isn't the value line means the parens don't
					// hold a lone `value = ...` (e.g. `notValue = 1`) -> nothing to fix
					else if (!nextMask.isBlank())
						break;
				}
				for (var c = 0; c < nextMask.length(); ++c) {
					final var ch = nextMask.charAt(c);
					if (ch == '(')
						++depth;
					else if (ch == ')' && --depth == 0) {
						closeLineIdx = i;
						closeCol = c;
						break;
					}
				}
				state = exitState;
			}
			if (valueLineIdx >= 0) {
				if (canCollapse && closeLineIdx >= 0) {
					final var indent = LineText.extractIndent(line);
					final var openParenIdx = openTrim.length() - 1;
					final var pieces = new ArrayList<String>();
					pieces.add(line.substring(indent.length(), openParenIdx + 1));
					for (var k = lineIndex; k <= closeLineIdx; ++k) {
						final var kRaw = lines.get(k);
						final var start = k == lineIndex ? openParenIdx + 1 : 0;
						final var end = k == closeLineIdx ? closeCol : kRaw.length();
						pieces.add(kRaw.substring(k == valueLineIdx ? valueMatchEnd : start, end));
					}
					pieces.add(lines.get(closeLineIdx).substring(closeCol));
					final var collapsed = indent + SpanReformat.collapse(pieces);
					if (LineLength.tabExpandedLength(collapsed) <= LineLength.MAX_LINE_LENGTH)
						return new FixResult(lineIndex, closeLineIdx, List.of(collapsed));
				}
				final var raw = lines.get(valueLineIdx);
				final var fixed = LineText.extractIndent(raw) + raw.substring(valueMatchEnd);
				return new FixResult(valueLineIdx, valueLineIdx, List.of(fixed));
			}
		}

		return new SkipResult(SkipMessages.ANNOTATION_SYNTAX_SKIP);
	}
}