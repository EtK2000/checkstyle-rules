package com.etk2000.checkstyle.gradle.fix;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Shared utilities for fixers that parse Java method calls with lambda or method-reference
 * arguments across single and multi-line source text. All scans respect string literals,
 * char literals, line comments ({@code //}), and block comments ({@code /* ... *}{@code /}).
 */
class LambdaCallParser {
	/**
	 * Position within a list of source lines.
	 */
	record Location(int line, int col) {}

	/**
	 * Result of a single {@link ScanState#advance} step.
	 *
	 * @param consumed         how many source characters were processed (1 normally, 2 for
	 *                         escape sequences or {@code /*}/{@code *}{@code /} markers)
	 * @param inside           true if this position is inside a literal or comment
	 *                         (callers should not count braces/parens here)
	 * @param keep             true if the character(s) should be retained by a
	 *                         comment-stripping copy
	 * @param lineCommentStart true if this is the start of a {@code //} line comment;
	 *                         callers should stop scanning the current line
	 */
	private record Step(int consumed, boolean inside, boolean keep, boolean lineCommentStart) {
		@CheckReturnValue
		@Nonnull
		static Step inside(boolean keep) {
			return new Step(1, true, keep, false);
		}

		@CheckReturnValue
		@Nonnull
		static Step lineComment() {
			return new Step(0, true, false, true);
		}

		@CheckReturnValue
		@Nonnull
		static Step outside() {
			return new Step(1, false, true, false);
		}

		@CheckReturnValue
		@Nonnull
		static Step skipThree(boolean keep) {
			return new Step(3, true, keep, false);
		}

		@CheckReturnValue
		@Nonnull
		static Step skipTwo(boolean keep) {
			return new Step(2, true, keep, false);
		}
	}

	/**
	 * Tracks literal and comment state across a scan. A single instance may be reused
	 * across multiple lines by calling {@link #endOfLine()} between them; this preserves
	 * block-comment state while resetting single-line states.
	 */
	private static final class ScanState {
		@CheckReturnValue
		private static boolean isTripleQuote(@Nonnull String text, int pos) {
			return pos + 2 < text.length() && text.charAt(pos) == '"'
					&& text.charAt(pos + 1) == '"' && text.charAt(pos + 2) == '"';
		}

		private boolean inBlockComment, inChar, inString, inTextBlock;

		/**
		 * Advances over the character at {@code pos}, updating state. Returns a {@link Step}
		 * describing how many characters were consumed and whether the caller should treat
		 * this position as "inside" a literal/comment (skip structural counting).
		 */
		@CheckReturnValue
		@Nonnull
		Step advance(@Nonnull String text, int pos) {
			final var c = text.charAt(pos);
			if (inBlockComment) {
				if (c == '*' && pos + 1 < text.length() && text.charAt(pos + 1) == '/') {
					inBlockComment = false;
					return Step.skipTwo( false);
				}
				return Step.inside(false);
			}
			if (inTextBlock) {
				if (isTripleQuote(text, pos)) {
					inTextBlock = false;
					return Step.skipThree(true);
				}
				if (c == '\\' && pos + 1 < text.length())
					return Step.skipTwo(true);
				return Step.inside(true);
			}
			if (inChar) {
				if (c == '\\' && pos + 1 < text.length())
					return Step.skipTwo( true);
				if (c == '\'')
					inChar = false;
				return Step.inside(true);
			}
			if (inString) {
				if (c == '\\' && pos + 1 < text.length())
					return Step.skipTwo( true);
				if (c == '"')
					inString = false;
				return Step.inside(true);
			}
			if (c == '/' && pos + 1 < text.length()) {
				final var next = text.charAt(pos + 1);
				if (next == '/')
					return Step.lineComment();
				if (next == '*') {
					inBlockComment = true;
					return Step.skipTwo( false);
				}
			}
			if (c == '\'')
				inChar = true;
			else if (c == '"') {
				if (isTripleQuote(text, pos)) {
					inTextBlock = true;
					return Step.skipThree(true);
				}
				inString = true;
			}
			return Step.outside();
		}

		void endOfLine() {
			// Line comments, strings, and char literals don't cross lines in normal Java.
			// Block comments and text blocks do, so inBlockComment and inTextBlock are preserved.
			inChar = false;
			inString = false;
		}
	}

	/**
	 * Whether {@code line} ends while still inside a block comment it opened but
	 * did not close (a {@code /*} with no matching close before end-of-line). A
	 * trailing {@code //} line comment does not count, since it is fully contained
	 * on the line. Lets a caller refuse to edit a line whose block comment
	 * continues onto later lines.
	 */
	@CheckReturnValue
	static boolean endsInBlockComment(@Nonnull String line) {
		final var state = new ScanState();
		for (var j = 0; j < line.length(); ++j) {
			final var step = state.advance(line, j);
			if (step.lineCommentStart)
				return false;
			if (step.consumed > 1)
				j += step.consumed - 1;
		}
		return state.inBlockComment;
	}

	/**
	 * Scans backward from {@code endPos} for a simple dotted identifier chain
	 * (e.g. {@code x}, {@code x.y}, {@code x.y.z}) and returns its start index.
	 * Returns {@code endPos} if no identifier character precedes that position.
	 */
	@CheckReturnValue
	static int extractReceiverStart(@Nonnull String text, int endPos) {
		var start = endPos;
		while (start > 0) {
			final var c = text.charAt(start - 1);
			if (Character.isJavaIdentifierPart(c) || c == '.')
				--start;
			else
				break;
		}
		return start;
	}

	/**
	 * Scans forward from {@code startLine} for the first {@code {} outside string/char/comment
	 * contexts, then returns the line index of the matching {@code }}. State is preserved
	 * across line boundaries, so multi-line block comments containing {@code {} or {@code }}
	 * characters are correctly skipped. Returns -1 if no matching brace is found.
	 */
	@CheckReturnValue
	static int findClosingBraceLine(@Nonnull List<String> lines, int startLine) {
		final var state = new ScanState();
		var depth = 0;
		var sawOpen = false;
		for (var i = startLine; i < lines.size(); ++i) {
			final var text = lines.get(i);
			for (var j = 0; j < text.length(); ++j) {
				final var step = state.advance(text, j);
				if (step.lineCommentStart)
					break;
				if (step.consumed > 1) {
					j += step.consumed - 1;
					continue;
				}
				if (step.inside)
					continue;
				final var c = text.charAt(j);
				if (c == '{') {
					++depth;
					sawOpen = true;
				}
				else if (c == '}') {
					--depth;
					if (sawOpen && depth == 0)
						return i;
				}
			}
			state.endOfLine();
		}
		return -1;
	}

	/**
	 * Finds the {@link Location} of the closing {@code )} that matches the opening paren at
	 * {@code (startLine, openParenCol)}. Returns {@code null} if not found.
	 */
	@CheckReturnValue
	@Nullable
	static Location findClosingParen(@Nonnull List<String> lines, int startLine, int openParenCol) {
		final var state = new ScanState();
		var depth = 0;
		for (var i = startLine; i < lines.size(); ++i) {
			final var text = lines.get(i);
			final var from = i == startLine ? openParenCol : 0;
			for (var j = from; j < text.length(); ++j) {
				final var step = state.advance(text, j);
				if (step.lineCommentStart)
					break;
				if (step.consumed > 1) {
					j += step.consumed - 1;
					continue;
				}
				if (step.inside)
					continue;
				final var c = text.charAt(j);
				if (c == '(')
					++depth;
				else if (c == ')') {
					--depth;
					if (depth == 0)
						return new Location(i, j);
				}
			}
			state.endOfLine();
		}
		return null;
	}

	/**
	 * Single-string variant of {@link #findClosingParen(List, int, int)}. Starts scanning
	 * at {@code openIdx} (which should point to an open {@code (}) and returns the index
	 * of the matching {@code )}. Literals, comments, and text blocks are respected.
	 * Returns -1 if not found. If {@code openIdx < 0}, returns -1.
	 */
	@CheckReturnValue
	static int findClosingParenInLine(@Nonnull String text, int openIdx) {
		if (openIdx < 0)
			return -1;
		final var state = new ScanState();
		var depth = 0;
		for (var j = openIdx; j < text.length(); ++j) {
			final var step = state.advance(text, j);
			if (step.lineCommentStart)
				break;
			if (step.consumed > 1) {
				j += step.consumed - 1;
				continue;
			}
			if (step.inside)
				continue;
			final var c = text.charAt(j);
			if (c == '(')
				++depth;
			else if (c == ')') {
				--depth;
				if (depth == 0)
					return j;
			}
		}
		return -1;
	}

	/**
	 * Scans forward starting at {@code startLine} for the first line whose last structural
	 * (outside any string/char/comment/text-block) non-whitespace character is {@code ;}.
	 * Handles both single-line forms ({@code for (...) body;}) and multi-line forms. A
	 * single-line form is only claimed when the terminating {@code ;} comes after the
	 * for-header's matching {@code )}. Block-comment and text-block state is preserved
	 * across line boundaries. Returns -1 if no terminating line is found.
	 */
	@CheckReturnValue
	static int findEndOfBracelessStatement(@Nonnull List<String> lines, int startLine) {
		final var state = new ScanState();
		// Paren depth and headerCloseIdx are tracked ACROSS lines so that a statement
		// spanning multiple lines (e.g. a call with a text-block argument) correctly
		// identifies the final `;` as being at top-level.
		var depth = 0;
		var headerCloseIdx = -1;
		for (var i = startLine; i < lines.size(); ++i) {
			final var text = lines.get(i);
			var lastStructuralSemi = -1;
			var lastStructuralNonWs = -1;
			var topLevelSemiCount = 0;
			for (var j = 0; j < text.length(); ++j) {
				final var step = state.advance(text, j);
				if (step.lineCommentStart)
					break;
				if (step.consumed > 1) {
					j += step.consumed - 1;
					continue;
				}
				if (step.inside)
					continue;
				final var c = text.charAt(j);
				if (Character.isWhitespace(c))
					continue;
				lastStructuralNonWs = j;
				if (c == '(')
					++depth;
				else if (c == ')') {
					--depth;
					if (i == startLine && depth == 0 && headerCloseIdx < 0)
						headerCloseIdx = j;
				}
				else if (c == ';') {
					lastStructuralSemi = j;
					// A `;` at paren-depth 0 is a top-level statement terminator.
					// `;` inside a for-header (depth > 0) or other parens do not count.
					if (depth == 0)
						++topLevelSemiCount;
				}
			}
			state.endOfLine();

			// Accept only if there is exactly ONE top-level `;` on the line (ruling out
			// multi-statement lines) AND that `;` is the last structural non-ws char.
			if (topLevelSemiCount != 1 || lastStructuralSemi != lastStructuralNonWs)
				continue;
			if (i == startLine) {
				// Single-line braceless form: `;` must come AFTER the for-header's `)`.
				if (headerCloseIdx >= 0 && lastStructuralSemi > headerCloseIdx)
					return i;
			}
			else
				return i;
		}
		return -1;
	}

	/**
	 * Returns true if {@code line} contains a structural open-brace character
	 * outside of string/char literals and comments. Useful for disambiguating a
	 * real block-opener from one that appears inside a string or comment.
	 */
	@CheckReturnValue
	static boolean hasStructuralOpenBrace(@Nonnull String line) {
		final var state = new ScanState();
		for (var j = 0; j < line.length(); ++j) {
			final var step = state.advance(line, j);
			if (step.lineCommentStart)
				break;
			if (step.consumed > 1) {
				j += step.consumed - 1;
				continue;
			}
			if (step.inside)
				continue;
			if (line.charAt(j) == '{')
				return true;
		}
		return false;
	}

	/**
	 * Returns true if {@code text} has an unbalanced count of {@code {} and {@code }},
	 * respecting string/char literals, line comments, and block comments.
	 */
	@CheckReturnValue
	static boolean hasUnclosedBrace(@Nonnull String text) {
		return hasUnclosedDelimiter(text, '{', '}');
	}

	@CheckReturnValue
	private static boolean hasUnclosedDelimiter(@Nonnull String text, char open, char close) {
		final var state = new ScanState();
		var depth = 0;
		for (var j = 0; j < text.length(); ++j) {
			final var step = state.advance(text, j);
			if (step.lineCommentStart)
				break;
			if (step.consumed > 1) {
				j += step.consumed - 1;
				continue;
			}
			if (step.inside)
				continue;
			final var c = text.charAt(j);
			if (c == open)
				++depth;
			else if (c == close)
				--depth;
		}
		return depth != 0;
	}

	/**
	 * Returns true if {@code text} has an unbalanced count of {@code (} and {@code )},
	 * respecting string/char literals, line comments, and block comments.
	 */
	@CheckReturnValue
	static boolean hasUnclosedParen(@Nonnull String text) {
		return hasUnclosedDelimiter(text, '(', ')');
	}

	/**
	 * Returns the index of the first occurrence of {@code needle} in {@code text} at a
	 * position outside of string/char literals and comments. Returns -1 if not found.
	 * The needle itself is matched literally against the raw text at candidate positions;
	 * only the START of the needle is required to be structural.
	 */
	@CheckReturnValue
	static int indexOfStructural(@Nonnull String text, @Nonnull String needle) {
		if (needle.isEmpty())
			return 0;
		final var state = new ScanState();
		for (var j = 0; j < text.length(); ++j) {
			final var step = state.advance(text, j);
			if (step.lineCommentStart)
				return -1;
			if (step.consumed > 1) {
				j += step.consumed - 1;
				continue;
			}
			if (step.inside)
				continue;
			if (text.startsWith(needle, j))
				return j;
		}
		return -1;
	}

	/**
	 * Returns the index of the first occurrence of {@code target} in {@code text} at a
	 * structural position (outside string/char literals, text blocks, and comments), at
	 * or after {@code fromIndex}. Returns -1 if not found.
	 */
	@CheckReturnValue
	static int indexOfStructuralChar(@Nonnull String text, int fromIndex, char target) {
		// Single-pass scan: advance state from position 0 so character-class tracking is
		// consistent, but only check for `target` matches once `j >= fromIndex`. This
		// avoids double-advance bugs that a split warm-up/main-loop design can cause when
		// `fromIndex` lands mid-escape-sequence.
		final var state = new ScanState();
		for (var j = 0; j < text.length(); ++j) {
			final var step = state.advance(text, j);
			if (step.lineCommentStart)
				return -1;
			if (step.consumed > 1) {
				j += step.consumed - 1;
				continue;
			}
			if (step.inside || j < fromIndex)
				continue;
			if (text.charAt(j) == target)
				return j;
		}
		return -1;
	}

	/**
	 * Strips line comments ({@code //}) and same-line block comments ({@code /*...*}{@code /})
	 * from a line while respecting string and char literals. Block comments that start on
	 * the line but do not end on it are stripped to end-of-line.
	 */
	@CheckReturnValue
	@Nonnull
	static String stripComment(@Nonnull String line) {
		final var state = new ScanState();
		final var sb = new StringBuilder(line.length());
		for (var j = 0; j < line.length(); ++j) {
			final var step = state.advance(line, j);
			if (step.lineCommentStart)
				return sb.toString();
			if (step.consumed > 1) {
				if (step.keep)
					sb.append(line, j, j + step.consumed);
				j += step.consumed - 1;
				continue;
			}
			if (step.keep)
				sb.append(line.charAt(j));
		}
		return sb.toString();
	}

	/**
	 * Returns the comment-stripped text of {@code lines[startLine]} from {@code startCol}
	 * through {@code lines[endLine]} (inclusive), with lines joined by a single space.
	 * Block-comment state is preserved across line boundaries, so multi-line
	 * {@code /* ... *}{@code /} comments are correctly stripped.
	 */
	@CheckReturnValue
	@Nonnull
	static String stripCommentsJoined(@Nonnull List<String> lines, int startLine, int startCol, int endLine) {
		final var state = new ScanState();
		final var sb = new StringBuilder();
		for (var i = startLine; i <= endLine; ++i) {
			final var text = lines.get(i);
			final var from = i == startLine ? startCol : 0;
			for (var j = from; j < text.length(); ++j) {
				final var step = state.advance(text, j);
				if (step.lineCommentStart)
					break;
				if (step.consumed > 1) {
					if (step.keep)
						sb.append(text, j, j + step.consumed);
					j += step.consumed - 1;
					continue;
				}
				if (step.keep)
					sb.append(text.charAt(j));
			}
			state.endOfLine();
			if (i < endLine)
				sb.append(' ');
		}
		return sb.toString();
	}
}