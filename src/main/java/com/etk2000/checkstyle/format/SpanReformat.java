package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared vocabulary and text primitives for the source-span reformatters
 * ({@link JavaTernaryReformatter}, {@link JavaArgListReformatter}). Both slice a contiguous source span
 * at AST token boundaries, collapse each sliced segment onto one line (tight-joining around
 * brackets/punctuation), and hand the result to {@link JavaSpanReindenter}. They share the outcome
 * types below and the pure-text helpers here so neither re-derives the join/comment rules.
 *
 * <p>All helpers are pure text operations with no dependency on any check or fixer.
 */
public final class SpanReformat {
	/** Outcome of a reformat: either the re-laid-out span or a reason it was left untouched. */
	public sealed interface Result permits Reformatted, CannotReformat {}

	/** The span {@code lines[fromIndex..toIndex]} (0-based, inclusive) is to be replaced with {@code lines}. */
	public record Reformatted(int fromIndex, int toIndex, @Nonnull List<String> lines) implements Result {}

	/** The span could not be re-laid-out; {@code reason} says why. */
	public record CannotReformat(@Nonnull Reason reason) implements Result {}

	public enum Reason {
		/** A {@code //} comment sits on a line the re-layout would join, and would swallow the rest. */
		COMMENT_ON_JOINED_LINE,
		/** A segment spans a text block or multi-line block comment, which cannot be collapsed. */
		MULTILINE_LITERAL,
		/** An argument needs its own opening-line layout (lambda/ternary/new/special call); re-laying the list out would move it off the {@code (} line. */
		SPECIAL_ARG,
		/** The reported token positions no longer line up with the source (a prior same-pass edit). */
		STALE
	}

	/**
	 * Whether any line in {@code lines[fromIdx..toIdx)} begins inside a text block or multi-line block
	 * comment, meaning a collapse across that boundary would corrupt significant whitespace.
	 */
	@CheckReturnValue
	public static boolean beginsInMultilineLiteral(@Nonnull List<String> lines, int fromIdx, int toIdx) {
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var i = fromIdx; i < toIdx; ++i) {
			lexer = JavaLineScanner.stateAfter(lines.get(i), lexer);
			if (lexer.inTextBlock() || lexer.inBlockComment())
				return true;
		}
		return false;
	}

	/**
	 * Per-line flags for {@code lines[fromIdx..toIdx]} (inclusive): element {@code i} is true when
	 * {@code lines.get(fromIdx + i)} begins inside a text block or multi-line block comment, threading
	 * {@link JavaLineScanner.LexerState} from {@code fromIdx}. Unlike {@link #beginsInMultilineLiteral},
	 * which collapses the range to a single flag, this keeps a flag per line so a caller can re-emit only
	 * the spanning segments verbatim while collapsing the rest.
	 */
	@CheckReturnValue
	@Nonnull
	static boolean[] beginsInMultilineLiteralByLine(@Nonnull List<String> lines, int fromIdx, int toIdx) {
		final var flags = new boolean[toIdx - fromIdx + 1];
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var i = fromIdx; i <= toIdx; ++i) {
			flags[i - fromIdx] = lexer.inTextBlock() || lexer.inBlockComment();
			lexer = JavaLineScanner.stateAfter(lines.get(i), lexer);
		}
		return flags;
	}

	/**
	 * Whether the first non-blank fragment of {@code fragments} is entirely a {@code //} line comment. Such
	 * a comment leaked past the previous {@code ,} (it is a trailing comment on the previous line, not this
	 * argument's own code), so the argument cannot be re-laid-out verbatim while keeping the comment attached.
	 */
	@CheckReturnValue
	static boolean beginsWithLineComment(@Nonnull List<String> fragments) {
		for (var fragment : fragments) {
			if (!fragment.isBlank())
				return fragment.stripLeading().startsWith("//");
		}
		return false;
	}

	/** Joins {@code fragments} onto one line, tight around brackets/punctuation, dropping blank fragments. */
	@CheckReturnValue
	@Nonnull
	public static String collapse(@Nonnull List<String> fragments) {
		final var joined = new StringBuilder();
		for (var fragment : fragments) {
			final var piece = fragment.strip();
			if (piece.isEmpty())
				continue;
			if (!joined.isEmpty() && !joinsTight(joined, piece))
				joined.append(' ');
			joined.append(piece);
		}
		return joined.toString();
	}

	@CheckReturnValue
	public static boolean hasTrailingLineComment(@Nonnull String line) {
		return JavaLineScanner.firstLineComment(line, JavaLineScanner.LexerState.NONE) >= 0;
	}

	/**
	 * Inserts {@code separator} at the end of {@code line}'s code, before any trailing {@code //} comment,
	 * so a required {@code ,}/{@code ;} lands on the code rather than inside the comment. Appends at the end
	 * when there is no trailing comment.
	 */
	@CheckReturnValue
	@Nonnull
	static String insertBeforeTrailingComment(@Nonnull String line, @Nonnull String separator) {
		final var comment = JavaLineScanner.firstLineComment(line, JavaLineScanner.LexerState.NONE);
		if (comment < 0)
			return line + separator;
		final var codeEnd = line.substring(0, comment).stripTrailing().length();
		return line.substring(0, codeEnd) + separator + line.substring(codeEnd);
	}

	@CheckReturnValue
	public static boolean joinsTight(@Nonnull StringBuilder joined, @Nonnull String continuation) {
		if (joined.isEmpty() || continuation.isEmpty())
			return false;
		final var first = continuation.charAt(0);
		if (first == '.' || first == ')' || first == ',' || first == ';' || first == ']')
			return true;
		final var last = joined.charAt(joined.length() - 1);
		return last == '(' || last == '[';
	}

	@CheckReturnValue
	public static int leadingTabs(@Nonnull String line) {
		var n = 0;
		while (n < line.length() && line.charAt(n) == '\t')
			++n;
		return n;
	}

	/**
	 * Whether {@code (idx, col)} is an in-range position in {@code lines} whose character equals
	 * {@code expected}. The reformatters validate every AST-reported token coordinate this way before
	 * slicing at it, so a stale coordinate (a prior same-pass edit shifted the text) is refused rather
	 * than slicing at the wrong character.
	 */
	@CheckReturnValue
	static boolean pointsAt(@Nonnull List<String> lines, int idx, int col, char expected) {
		return idx >= 0 && idx < lines.size() && col >= 0 && col < lines.get(idx).length()
				&& lines.get(idx).charAt(col) == expected;
	}

	/**
	 * The source fragments from {@code (startIdx, startCol)} inclusive to {@code (endIdx, endCol)}
	 * exclusive: a single substring when both ends are on one line, otherwise the tail of the start
	 * line, the whole interior lines, and the head of the end line.
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> slice(@Nonnull List<String> lines, int startIdx, int startCol, int endIdx, int endCol) {
		if (startIdx == endIdx) {
			final var single = new ArrayList<String>(1);
			single.add(lines.get(startIdx).substring(startCol, endCol));
			return single;
		}

		final var fragments = new ArrayList<String>();
		fragments.add(lines.get(startIdx).substring(startCol));
		for (var i = startIdx + 1; i < endIdx; ++i)
			fragments.add(lines.get(i));
		fragments.add(lines.get(endIdx).substring(0, endCol));
		return fragments;
	}

	/**
	 * Whether collapsing {@code fragments} would pull a {@code //} comment inline ahead of later content
	 * (swallowing it). A comment on the last non-blank fragment ends a canonical line and is preserved.
	 */
	@CheckReturnValue
	static boolean swallowsComment(@Nonnull List<String> fragments) {
		var lastNonBlank = -1;
		for (var i = 0; i < fragments.size(); ++i) {
			if (!fragments.get(i).isBlank())
				lastNonBlank = i;
		}
		for (var i = 0; i < lastNonBlank; ++i) {
			if (!fragments.get(i).isBlank() && hasTrailingLineComment(fragments.get(i)))
				return true;
		}
		return false;
	}

	/** Visual width of {@code line} with tabs expanded to {@code tabWidth} stops. */
	@CheckReturnValue
	static int tabExpandedWidth(@Nonnull String line, int tabWidth) {
		var width = 0;
		for (var i = 0; i < line.length(); ++i)
			width += line.charAt(i) == '\t' ? tabWidth - (width % tabWidth) : 1;
		return width;
	}

	private SpanReformat() {
	}
}