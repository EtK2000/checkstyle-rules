package com.etk2000.checkstyle;

import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Finds or splits on a token that sits outside every bracket group, over text whose
 * literal and comment content has been masked away by
 * {@link JavaLineScanner#stripCommentsAndStrings}. This is the "top-level separator"
 * idiom the text-based fixers use to answer questions like "does this initializer
 * declare a second variable" or "where does this assignment's {@code =} sit".
 *
 * <p>Masking runs first, so a bracket or a separator inside a string, char literal,
 * text block or comment contributes nothing. The mask preserves every column, so an
 * index this class returns is valid in the text that was passed in, and the parts
 * {@link #split} produces are sliced from that original text rather than from the mask.</p>
 *
 * <p>Masked content is rewritten to spaces rather than removed, so a separator or a
 * {@link Target} that can match a space matches literal and comment content too, and the
 * index reported for such a match points inside a construct the original text still carries.
 * Pass a separator that masking cannot produce, or re-test the match against the unmasked
 * text.</p>
 *
 * <p>The two configuration axes are the ones the call sites genuinely disagree on, and
 * the disagreements are deliberate rather than accidental, so there is no default:
 * {@link Brackets} because a scan for a concatenation operator must stay blind to the
 * braces of a lambda body, and {@link Underflow} because a scan over a fragment that
 * begins inside a group sees closers it never opened. {@code <} and {@code >} are never
 * a pair on any axis: a relational {@code <} would open a group that hides every later
 * separator, and a relational {@code >}, a lambda arrow or a shift would close it again,
 * leaving the scan balanced with the separator undetected.</p>
 */
public record TopLevelScan(@Nonnull Brackets brackets, @Nonnull Underflow underflow) {
	/** Which bracket pairs open and close a group. */
	public enum Brackets {
		/** {@code ()}, {@code []} and <code>{}</code>. */
		ALL,

		/**
		 * {@code ()} and {@code []} only. A brace is ordinary text, so a token inside a
		 * lambda body, an array initializer or an anonymous class body still counts as
		 * top level.
		 */
		NO_BRACES
	}

	/** What {@link TopLevelScan#contains} reports when the scan ends at a non-zero depth. */
	public enum Unbalanced {
		/**
		 * Report a hit. For a caller that rejects the text on a hit, this turns an
		 * untrustworthy scan into a refusal rather than a silent acceptance.
		 */
		FOUND,

		/** Report no hit, the same as a balanced scan that found nothing. */
		NOT_FOUND
	}

	/** What a closing bracket with no matching opener does to the depth counter. */
	public enum Underflow {
		/**
		 * Hold the depth at 0, so the text following the stray closer is still scanned as
		 * top level.
		 */
		CLAMP,

		/**
		 * Let the depth go negative, so the text following the stray closer counts as
		 * nested until an opener balances it out again.
		 */
		SIGNED
	}

	/**
	 * Tests whether the depth-0 position {@code index} of {@code masked} is a match.
	 * {@code index} is always in {@code [0, masked.length())}, so {@code charAt(index)} is
	 * safe, but an implementation that looks at a neighbouring position must bound-check
	 * that itself.
	 */
	@FunctionalInterface
	public interface Target {
		@CheckReturnValue
		boolean matchesAt(@Nonnull String masked, int index);
	}

	@CheckReturnValue
	private int afterClose(int depth) {
		return underflow == Underflow.CLAMP ? Math.max(0, depth - 1) : depth - 1;
	}

	/**
	 * Whether {@code s} carries {@code target} outside every bracket group. A scan that
	 * ends at a non-zero depth reports whatever {@code unbalanced} prescribes, but only
	 * when no genuine top-level hit was seen first.
	 */
	@CheckReturnValue
	public boolean contains(@Nonnull String s, char target, @Nonnull Unbalanced unbalanced) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(s, LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var ch = masked.charAt(i);
			if (isOpen(ch))
				++depth;
			else if (isClose(ch))
				depth = afterClose(depth);
			else if (depth == 0 && ch == target)
				return true;
		}
		return unbalanced == Unbalanced.FOUND && depth != 0;
	}

	/**
	 * The index in {@code s} of the first position outside every bracket group that
	 * {@code target} accepts, or -1 when it accepts none. A rejected position does not
	 * end the scan, so a target may veto a candidate on its surroundings and still see
	 * the ones after it. A bracket position is consumed as structure and is never
	 * offered to {@code target}.
	 */
	@CheckReturnValue
	public int indexOf(@Nonnull String s, @Nonnull Target target) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(s, LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var ch = masked.charAt(i);
			if (isOpen(ch))
				++depth;
			else if (isClose(ch))
				depth = afterClose(depth);
			else if (depth == 0 && target.matchesAt(masked, i))
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private boolean isClose(char ch) {
		return ch == ')' || ch == ']' || brackets == Brackets.ALL && ch == '}';
	}

	@CheckReturnValue
	private boolean isOpen(char ch) {
		return ch == '(' || ch == '[' || brackets == Brackets.ALL && ch == '{';
	}

	/**
	 * Splits {@code s} at every {@code separator} outside a bracket group. Parts are
	 * sliced from {@code s} verbatim, so literal and comment content survives and
	 * surrounding whitespace is left on; a caller that wants them trimmed does that
	 * itself. The result always holds one more part than there were separators, so an
	 * empty string yields one empty part, and it is a fresh mutable list the caller owns.
	 *
	 * <p>Slicing the original relies on {@link JavaLineScanner#stripCommentsAndStrings}
	 * preserving length exactly; a masker that ever changed length would make this
	 * mis-slice rather than throw.</p>
	 */
	@CheckReturnValue
	@Nonnull
	public List<String> split(@Nonnull String s, char separator) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(s, LexerState.NONE);
		final var parts = new ArrayList<String>();
		var depth = 0;
		var start = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var ch = masked.charAt(i);
			if (isOpen(ch))
				++depth;
			else if (isClose(ch))
				depth = afterClose(depth);
			else if (depth == 0 && ch == separator) {
				parts.add(s.substring(start, i));
				start = i + 1;
			}
		}
		parts.add(s.substring(start));
		return parts;
	}
}