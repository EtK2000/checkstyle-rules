package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Shared vocabulary and text primitives for the source-span reformatters in this package: the outcome
 * types below and the pure-text helpers here, so no reformatter re-derives the join/comment rules.
 */
public final class SpanReformat {
	public sealed interface Result permits Reformatted, CannotReformat {}

	/** The span {@code lines[fromIndex..toIndex]} (0-based, inclusive) is to be replaced with {@code lines}. */
	public record Reformatted(int fromIndex, int toIndex, @Nonnull List<String> lines) implements Result {}

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
	 * Whether any of {@code lines[fromIdx + 1 .. toIdx]} begins inside a text block or multi-line block
	 * comment, meaning a collapse across that boundary would corrupt significant whitespace. The state is
	 * advanced before each test, so {@code fromIdx}'s own entry state is never asked about: that line is
	 * the base a join appends onto rather than an appended line, and a literal still open at its end
	 * necessarily reopens on the next line, which is covered. Contrast
	 * {@link #beginsInMultilineLiteralByLine}, which tests before advancing and so really does report
	 * whether each line begins inside a literal.
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
	 * {@link JavaLineScanner.LexerState} from {@code fromIdx}.
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
	 * Whether any of {@code lines[fromIdx + 1 .. toIdx]} begins inside a text block, on the same
	 * advance-then-test basis as {@link #beginsInMultilineLiteral}. Narrower than that method, for a
	 * caller that joins whole lines in order: a block comment survives that intact, because its closing
	 * delimiter is inside the joined range, but a text block cannot, because nothing may follow its
	 * opening delimiter on the same line.
	 */
	@CheckReturnValue
	public static boolean beginsInTextBlock(@Nonnull List<String> lines, int fromIdx, int toIdx) {
		var lexer = JavaLineScanner.LexerState.NONE;
		for (var i = fromIdx; i < toIdx; ++i) {
			lexer = JavaLineScanner.stateAfter(lines.get(i), lexer);
			if (lexer.inTextBlock())
				return true;
		}
		return false;
	}

	/**
	 * Whether the first non-blank fragment of {@code fragments} is entirely a {@code //} line comment.
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
		return hasTrailingLineComment(line, JavaLineScanner.LexerState.NONE);
	}

	/** Whether {@code line} carries a {@code //} comment, given the lexer state it begins in. */
	@CheckReturnValue
	public static boolean hasTrailingLineComment(@Nonnull String line, @Nonnull LexerState state) {
		return JavaLineScanner.firstLineComment(line, state) >= 0;
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
	 * The lexer state a span beginning at {@code lines[idx]} starts in, threaded from the first line of the
	 * file. Seeding {@link JavaLineScanner.LexerState#NONE} at the span itself is wrong whenever that line
	 * begins inside a text block or block comment: the line is then lexed as code, so an apostrophe in
	 * comment prose opens a char literal that masks the rest of it, hiding a real trailing {@code //}.
	 *
	 * <p>Total in {@code idx}: a negative one folds nothing and one past the end folds every line, so a
	 * caller asking about the position after a span's last line ({@code endLineIdx + 1}, which reaches
	 * exactly {@code lines.size()}) gets the end-of-buffer state rather than an exception.
	 */
	@CheckReturnValue
	@Nonnull
	public static LexerState lexerStateAt(@Nonnull List<String> lines, int idx) {
		var state = LexerState.NONE;
		for (var i = 0; i < idx && i < lines.size(); ++i)
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		return state;
	}

	/**
	 * Whether {@code (idx, col)} is an in-range position in {@code lines} whose character equals
	 * {@code expected}.
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
	 *
	 * <p>{@code state} is the lexer state the first fragment begins in, and is threaded across the rest.
	 * A caller whose first fragment starts at a code boundary (just past a {@code (}, {@code ,},
	 * {@code ?} or {@code :}) passes {@link JavaLineScanner.LexerState#NONE}; one whose slice starts at
	 * column 0 must pass {@link #lexerStateAt}, or a first line that begins inside a block comment is
	 * lexed as code and an apostrophe in the prose masks a real trailing {@code //}.
	 */
	@CheckReturnValue
	static boolean swallowsComment(@Nonnull List<String> fragments, @Nonnull LexerState state) {
		var lastNonBlank = -1;
		for (var i = 0; i < fragments.size(); ++i) {
			if (!fragments.get(i).isBlank())
				lastNonBlank = i;
		}
		var lexer = state;
		for (var i = 0; i < lastNonBlank; ++i) {
			if (!fragments.get(i).isBlank() && hasTrailingLineComment(fragments.get(i), lexer))
				return true;
			lexer = JavaLineScanner.stateAfter(fragments.get(i), lexer);
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