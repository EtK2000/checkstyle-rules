package com.etk2000.checkstyle;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared literal/comment-aware lexer for the text-based fixers. A single physical
 * line is scanned tracking Java string, character, block-comment, and text-block
 * state, with {@link LexerState} carrying the block-comment / text-block spans that
 * cross physical lines.
 *
 * <p>This is the one place that knows how Java lexes strings, char literals,
 * {@code //} and {@code /* *}{@code /} comments, and {@code """} text blocks; the
 * fixers build their structural scans (paren depth, terminator search, comment
 * stripping) on top of it instead of re-deriving the escape and text-block rules.
 */
public final class JavaLineScanner {
	/**
	 * Lexer state that carries across physical lines: whether the next line begins
	 * inside an unterminated block comment or text block. String and char literals
	 * never span lines in valid Java, so they are not part of the carried state.
	 */
	public record LexerState(boolean inBlockComment, boolean inTextBlock) {
		public static final LexerState NONE = new LexerState(false, false);

		/**
		 * Whether a literal that spans physical lines is still open, i.e. the next
		 * line continues inside a block comment or text block.
		 */
		@CheckReturnValue
		public boolean inMultilineLiteral() {
			return inBlockComment || inTextBlock;
		}
	}

	/**
	 * Returns the column of the first code-level comment marker ({@code //} or {@code /*}) on
	 * {@code line} given the incoming {@code state}, or {@code -1} when the line carries no comment.
	 * Markers inside string/char literals and text blocks are ignored. When {@code line} begins
	 * already inside a block comment (per {@code state}), the comment covers the line from its start,
	 * so {@code 0} is returned. When it begins inside a text block, scanning resumes only after the
	 * block closes on this line.
	 *
	 * <p>Unlike {@link #stripCommentsAndStrings}, which blanks the marker itself, this preserves the
	 * marker's location, so callers can detect or split at a comment they need to keep.
	 */
	@CheckReturnValue
	public static int firstCommentMarker(@Nonnull String line, @Nonnull LexerState state) {
		if (state.inBlockComment())
			return 0;
		var inTextBlock = state.inTextBlock();
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inTextBlock) {
				if (c == '"' && !LineText.isEscaped(line, i) && i + 2 < line.length()
						&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = false;
					i += 2;
				}
				continue;
			}
			if (inString) {
				if (c == '"' && !LineText.isEscaped(line, i))
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\'' && !LineText.isEscaped(line, i))
					inChar = false;
				continue;
			}
			if (c == '"') {
				if (i + 2 < line.length() && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = true;
					i += 2;
				}
				else
					inString = true;
			}
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && (line.charAt(i + 1) == '/' || line.charAt(i + 1) == '*'))
				return i;
		}
		return -1;
	}

	/**
	 * Returns the column of the first code-level {@code //} line-comment marker on {@code line} given the
	 * incoming {@code state}, or {@code -1} when the line carries no line comment. Unlike
	 * {@link #firstCommentMarker}, a complete {@code /* *}{@code /} block comment is skipped over rather than
	 * reported, so this finds a {@code //} that follows an inline block comment on the same line. Markers
	 * inside string/char literals, text blocks, and block comments are ignored. When {@code line} begins
	 * inside an unterminated block comment or text block (per {@code state}) that never closes on this line,
	 * there is no code-level {@code //}, so {@code -1} is returned.
	 */
	@CheckReturnValue
	public static int firstLineComment(@Nonnull String line, @Nonnull LexerState state) {
		var inBlockComment = state.inBlockComment();
		var inTextBlock = state.inTextBlock();
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inTextBlock) {
				if (c == '"' && !LineText.isEscaped(line, i) && i + 2 < line.length()
						&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = false;
					i += 2;
				}
				continue;
			}
			if (inString) {
				if (c == '"' && !LineText.isEscaped(line, i))
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\'' && !LineText.isEscaped(line, i))
					inChar = false;
				continue;
			}
			if (c == '"') {
				if (i + 2 < line.length() && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = true;
					i += 2;
				}
				else
					inString = true;
			}
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return i;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				inBlockComment = true;
				++i;
			}
		}
		return -1;
	}

	/**
	 * Masks every line of {@code lines} with {@link #stripCommentsAndStrings}, threading the
	 * {@link LexerState} across lines in a single forward pass so a block comment or text block that
	 * spans multiple physical lines is masked correctly on every line it covers. The returned list is
	 * the same size as {@code lines}; element {@code i} is {@code lines.get(i)} with string/char/comment
	 * content blanked, indexable at the same columns as the original. Callers that need random or
	 * backward access to masked source build it once here instead of re-threading state per lookup.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> maskAll(@Nonnull List<String> lines) {
		final var masked = new ArrayList<String>(lines.size());
		var state = LexerState.NONE;
		for (var line : lines) {
			masked.add(stripCommentsAndStrings(line, state));
			state = stateAfter(line, state);
		}
		return masked;
	}

	/**
	 * Returns the index of the bracket matching the opener at {@code openIndex}
	 * within a single physical {@code line}, ignoring brackets inside string/char
	 * literals and comments. The opener may be any bracket family — {@code (},
	 * {@code [}, or a curly brace — and only its own family is counted (a
	 * {@code )} does not affect a {@code [} scan). Returns {@code -1} when
	 * {@code openIndex} is out of range, does not point at an opener, or the group
	 * does not close on this line.
	 *
	 * <p>Like {@link #matchingCloseParen}, masking ({@link #stripCommentsAndStrings})
	 * starts at {@code openIndex}: the char there is a real opener, so the lexer
	 * state at that point is normal code and anything earlier on the line is
	 * irrelevant. Passing an already-masked line is safe (re-masking is idempotent).
	 */
	@CheckReturnValue
	public static int matchingClose(@Nonnull String line, int openIndex) {
		if (openIndex < 0 || openIndex >= line.length())
			return -1;
		final var open = line.charAt(openIndex);
		final char close;
		switch (open) {
			case '(' -> close = ')';
			case '[' -> close = ']';
			case '{' -> close = '}';
			default -> {
				return -1;
			}
		}
		final var scan = stripCommentsAndStrings(line.substring(openIndex), LexerState.NONE);
		var depth = 1;
		for (var i = 1; i < scan.length(); ++i) {
			final var c = scan.charAt(i);
			if (c == open)
				++depth;
			else if (c == close) {
				--depth;
				if (depth == 0)
					return openIndex + i;
			}
		}
		return -1;
	}

	/**
	 * Returns the index of the {@code )} matching the {@code (} at
	 * {@code openParenIndex} within a single physical {@code line}, ignoring
	 * parentheses inside string/char literals and comments. Returns {@code -1}
	 * when {@code openParenIndex} is out of range or there is no matching close
	 * paren on the line, i.e. the group spans multiple physical lines or a
	 * {@code //} comment, unterminated block comment, or text-block opener
	 * swallows the rest of the line.
	 *
	 * <p>Masking ({@link #stripCommentsAndStrings}) starts at {@code openParenIndex}
	 * rather than the start of the line: the char there is a real {@code (}, so the
	 * lexer state at that point is always normal code, and anything earlier on the
	 * line is irrelevant. This is what lets it work on a continuation line that
	 * begins inside a text block or block comment (e.g. a leading {@code """} that
	 * closes a text block before the paren), where masking from the line start
	 * would misread the leading delimiter.
	 */
	@CheckReturnValue
	public static int matchingCloseParen(@Nonnull String line, int openParenIndex) {
		if (openParenIndex < 0 || openParenIndex >= line.length())
			return -1;
		final var scan = stripCommentsAndStrings(line.substring(openParenIndex), LexerState.NONE);
		var depth = 1;
		for (var i = 1; i < scan.length(); ++i) {
			final var c = scan.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')') {
				--depth;
				if (depth == 0)
					return openParenIndex + i;
			}
		}
		return -1;
	}

	/**
	 * If {@code line} begins inside a text block or block comment (per {@code state}) and that literal
	 * closes on this line, returns the index just past its closing delimiter ({@code """} or {@code *}{@code /});
	 * returns -1 if {@code state} is not inside a literal or the literal stays open past this line.
	 */
	@CheckReturnValue
	public static int multilineLiteralCloseIndex(@Nonnull String line, @Nonnull LexerState state) {
		if (state.inBlockComment()) {
			for (var i = 0; i + 1 < line.length(); ++i) {
				if (line.charAt(i) == '*' && line.charAt(i + 1) == '/')
					return i + 2;
			}
			return -1;
		}
		if (state.inTextBlock()) {
			for (var i = 0; i + 2 < line.length(); ++i) {
				if (line.charAt(i) == '"' && !LineText.isEscaped(line, i)
						&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"')
					return i + 3;
			}
			return -1;
		}
		return -1;
	}

	/**
	 * Index of the first line at or after {@code from} that carries code, or
	 * {@code -1} when none does. {@code masked} must come from {@link #maskAll}:
	 * a line masks to blank when it is empty, whitespace, a comment, or content of
	 * a comment or text block opened earlier, so one blank test covers every form
	 * of "nothing to see here".
	 */
	@CheckReturnValue
	public static int nextCodeLine(@Nonnull List<String> masked, int from) {
		for (var i = Math.max(from, 0); i < masked.size(); ++i) {
			if (!masked.get(i).isBlank())
				return i;
		}
		return -1;
	}

	/**
	 * Whether the first code character on the line is {@code token}. Checked
	 * against {@code masked} so a character that is comment or literal content is
	 * not mistaken for code, and the leading-whitespace runs must agree so text the
	 * mask blanked cannot hide ahead of it. Masking preserves length and writes
	 * only spaces, so masked whitespace is a superset of raw's: equal run lengths
	 * prove the surviving token sits at raw's own first non-whitespace index.
	 */
	@CheckReturnValue
	public static boolean opensWith(@Nonnull String raw, @Nonnull String masked, char token) {
		final var maskedBody = masked.stripLeading();
		return !maskedBody.isEmpty() && maskedBody.charAt(0) == token
				&& raw.length() - raw.stripLeading().length() == masked.length() - maskedBody.length();
	}

	/**
	 * Returns the {@link LexerState} in effect after scanning {@code line} that
	 * began in {@code state}, for threading multi-line block-comment / text-block
	 * spans onto the next physical line.
	 */
	@CheckReturnValue
	@Nonnull
	public static LexerState stateAfter(@Nonnull String line, @Nonnull LexerState state) {
		var inBlockComment = state.inBlockComment();
		var inTextBlock = state.inTextBlock();
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inTextBlock) {
				if (c == '"' && !LineText.isEscaped(line, i) && i + 2 < line.length()
						&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = false;
					i += 2;
				}
				continue;
			}
			if (inString) {
				if (c == '"' && !LineText.isEscaped(line, i))
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\'' && !LineText.isEscaped(line, i))
					inChar = false;
				continue;
			}
			if (c == '"') {
				if (i + 2 < line.length() && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					inTextBlock = true;
					i += 2;
				}
				else
					inString = true;
			}
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				inBlockComment = true;
				++i;
			}
		}
		return new LexerState(inBlockComment, inTextBlock);
	}

	/**
	 * Returns {@code line} with the contents of string and character literals and
	 * all comment text replaced by spaces, starting from {@code state}. Length is
	 * preserved and structural punctuation outside literals/comments is left
	 * intact, so callers can index/search the result with the same column offsets
	 * as the original line. The literal delimiter quotes themselves are kept.
	 */
	@CheckReturnValue
	@Nonnull
	public static String stripCommentsAndStrings(@Nonnull String line, @Nonnull LexerState state) {
		final var out = new StringBuilder(line);
		var inBlockComment = state.inBlockComment();
		var inTextBlock = state.inTextBlock();
		var inString = false;
		var inChar = false;
		// lookahead reads must come from the immutable `line`, not the in-place
		// `out` buffer (overwrites would otherwise mask marker chars); writes
		// still target `out`
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					out.setCharAt(i, ' ');
					out.setCharAt(i + 1, ' ');
					inBlockComment = false;
					++i;
				}
				else
					out.setCharAt(i, ' ');
				continue;
			}
			if (inTextBlock) {
				if (c == '"' && !LineText.isEscaped(line, i) && i + 2 < line.length()
						&& line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					out.setCharAt(i, ' ');
					out.setCharAt(i + 1, ' ');
					out.setCharAt(i + 2, ' ');
					inTextBlock = false;
					i += 2;
				}
				else
					out.setCharAt(i, ' ');
				continue;
			}
			if (inString) {
				if (c == '"' && !LineText.isEscaped(line, i))
					inString = false;
				else
					out.setCharAt(i, ' ');
				continue;
			}
			if (inChar) {
				if (c == '\'' && !LineText.isEscaped(line, i))
					inChar = false;
				else
					out.setCharAt(i, ' ');
				continue;
			}
			if (c == '"') {
				if (i + 2 < line.length() && line.charAt(i + 1) == '"' && line.charAt(i + 2) == '"') {
					out.setCharAt(i, ' ');
					out.setCharAt(i + 1, ' ');
					out.setCharAt(i + 2, ' ');
					inTextBlock = true;
					i += 2;
				}
				else
					inString = true;
			}
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
				for (var k = i; k < out.length(); ++k)
					out.setCharAt(k, ' ');
				break;
			}
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				out.setCharAt(i, ' ');
				out.setCharAt(i + 1, ' ');
				inBlockComment = true;
				++i;
			}
		}
		return out.toString();
	}

	/**
	 * Strips balanced parentheses that wrap the entire expression, repeatedly (so {@code ((a))} -> {@code a}),
	 * returning the inner expression trimmed. Parens are counted on a {@link #stripCommentsAndStrings} mask so
	 * a paren inside a string or char literal (e.g. {@code f(")")}) is not miscounted; a non-wrapping pair
	 * (e.g. {@code (a) + (b)}) is left intact.
	 */
	@CheckReturnValue
	public static String stripOuterParens(@Nonnull String expr) {
		var result = expr.strip();
		while (result.length() >= 2 && result.charAt(0) == '(' && result.charAt(result.length() - 1) == ')') {
			final var masked = stripCommentsAndStrings(result, LexerState.NONE);
			var depth = 0;
			var wraps = true;
			for (var i = 0; i < masked.length(); ++i) {
				final var c = masked.charAt(i);
				if (c == '(')
					++depth;
				else if (c == ')' && --depth == 0 && i != masked.length() - 1) {
					wraps = false;
					break;
				}
			}
			if (!wraps)
				return result;
			result = result.substring(1, result.length() - 1).strip();
		}
		return result;
	}

	private JavaLineScanner() {
	}
}