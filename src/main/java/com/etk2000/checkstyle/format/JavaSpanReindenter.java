package com.etk2000.checkstyle.format;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Re-indents a contiguous, already-line-broken Java span to the project's canonical indentation.
 * Callers (the auto-fixers) decide the line breaks (which content sits on which physical line) and
 * hand the resulting lines here to fix only the leading indentation of each.
 *
 * <p>The rule, reverse-engineered from the project's canonical multiline oracles:
 * {@code indent(line) = indent(openerLineOf(enclosingBracket)) + step}, where the enclosing bracket
 * is the innermost bracket still open at the start of the line after that line's own leading closing
 * brackets are applied, and {@code step} is one tab for {@code &#123;} (a block body) or two tabs for
 * {@code (}/{@code [} (a continuation/argument/chain). Multiple brackets opened on one line form a
 * single level (so {@code method(new ArrayList<>(other(} indents its content one level and
 * {@code )))} dedents one level). A method-chain link ({@code .foo()} beginning its own line) instead
 * indents one level in from the line it chains onto (the preceding content line, i.e. the receiver),
 * so a chain on a receiver that sits on its own continuation line steps in from that line rather than
 * from the enclosing bracket; consecutive links at the same depth align with each other.
 *
 * <p>Bracket counting runs on {@link JavaLineScanner#stripCommentsAndStrings}-masked text with the
 * {@link JavaLineScanner.LexerState} threaded across lines, so brackets inside string/char literals,
 * {@code //} and {@code /* *}{@code /} comments, and text blocks are ignored. A line that begins
 * inside a text block or a multi-line block comment is emitted verbatim, since its leading whitespace
 * is significant. Output is tabs-only; the first line is forced to {@code baseTabs}; over-closed input
 * clamps at {@code baseTabs} rather than going negative; the transform is idempotent.
 */
public final class JavaSpanReindenter {
	private record OpenBracket(boolean brace, int openerLine) {}

	@CheckReturnValue
	private static boolean isCloser(char c) {
		return c == ')' || c == ']' || c == '}';
	}

	@CheckReturnValue
	private static boolean isOpener(char c) {
		return c == '(' || c == '[' || c == '{';
	}

	@CheckReturnValue
	private static int leadingTabs(@Nonnull String line) {
		var n = 0;
		while (n < line.length() && line.charAt(n) == '\t')
			++n;
		return n;
	}

	/**
	 * Returns a copy of {@code lines} re-indented per the class contract. {@code baseTabs} is the
	 * indentation (in tabs) of the span's first line; every other line is indented relative to it. A
	 * span of zero or one line is returned unchanged.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> reindent(@Nonnull List<String> lines, int baseTabs) {
		if (lines.size() <= 1)
			return lines;

		final var expanded = splitTrailingClosersAfterLiteral(lines);
		final var result = new ArrayList<String>(expanded.size());
		final var indentByLine = new int[expanded.size()];
		final var open = new ArrayDeque<OpenBracket>();
		var state = JavaLineScanner.LexerState.NONE;
		// receiver of the previous emitted content line, so a method-chain continuation (`.foo()` on its
		// own line) can indent one level in from the line it chains onto rather than from the enclosing
		// bracket: prevDepth is the bracket depth at that line's start, prevWasChain whether it too was a
		// chain link (consecutive links at the same depth align instead of stepping in again)
		var prevIndent = baseTabs;
		var prevDepth = 0;
		var prevWasChain = false;
		for (var i = 0; i < expanded.size(); ++i) {
			final var raw = expanded.get(i);
			final var beganInBlock = state.inTextBlock() || state.inBlockComment();
			final var masked = JavaLineScanner.stripCommentsAndStrings(raw, state);
			final var content = raw.strip();
			final var depthAtStart = open.size();

			if (beganInBlock) {
				// leading whitespace is text-block content / block-comment body: preserve verbatim
				result.add(raw);
				indentByLine[i] = leadingTabs(raw);
				prevIndent = indentByLine[i];
				prevDepth = depthAtStart;
				prevWasChain = false;
			}
			else if (i == 0) {
				indentByLine[i] = baseTabs;
				result.add("\t".repeat(baseTabs) + content);
				prevIndent = baseTabs;
				prevDepth = depthAtStart;
				prevWasChain = false;
			}
			else if (content.isEmpty()) {
				result.add("");
				indentByLine[i] = baseTabs;
			}
			else {
				final var maskedContent = masked.strip();
				var leadingClosers = 0;
				while (leadingClosers < maskedContent.length() && isCloser(maskedContent.charAt(leadingClosers)))
					++leadingClosers;
				final var isChain = leadingClosers == 0 && !maskedContent.isEmpty() && maskedContent.charAt(0) == '.';
				final int indent;
				if (leadingClosers > 0 && !open.isEmpty()) {
					// a line that starts by closing bracket(s) aligns with the line that opened the
					// outermost of them (so a } lands under its opener, not one continuation level in).
					// Read that opener off the stack without copying it: it is the min(closers, depth)-th
					// entry from the top (iteration is top-first, since opens are pushed onto the head).
					final var pops = Math.min(leadingClosers, open.size());
					final var it = open.iterator();
					var outermostClosed = it.next();
					for (var k = 1; k < pops; ++k)
						outermostClosed = it.next();
					indent = indentByLine[outermostClosed.openerLine()];
				}
				else if (open.isEmpty())
					indent = baseTabs;
				else if (isChain)
					indent = prevWasChain && prevDepth == depthAtStart ? prevIndent : prevIndent + 2;
				else {
					// a continuation line sits one step in from the line opening its enclosing bracket
					final var top = open.peek();
					indent = indentByLine[top.openerLine()] + (top.brace() ? 1 : 2);
				}
				indentByLine[i] = indent;
				result.add("\t".repeat(indent) + content);
				prevIndent = indent;
				prevDepth = depthAtStart;
				prevWasChain = isChain;
			}

			for (var c = 0; c < masked.length(); ++c) {
				final var ch = masked.charAt(c);
				if (isOpener(ch))
					open.push(new OpenBracket(ch == '{', i));
				else if (isCloser(ch) && !open.isEmpty())
					open.pop();
			}
			state = JavaLineScanner.stateAfter(raw, state);
		}
		return result;
	}

	/**
	 * Splits a line that begins inside a text block / block comment, closes that literal, and carries a
	 * trailing run of closing brackets (e.g. {@code text"""; });}) into two: the literal-closing part
	 * (kept verbatim, with any statement terminator) and the closing-bracket run, so the latter lands on
	 * its own line at its canonical depth. Lines without that exact shape are returned unchanged, so a
	 * verbatim argument ending in {@code ,} or a bare closing delimiter is never touched.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> splitTrailingClosersAfterLiteral(@Nonnull List<String> lines) {
		final var expanded = new ArrayList<String>(lines.size());
		var state = JavaLineScanner.LexerState.NONE;
		for (var line : lines) {
			final var beganInBlock = state.inTextBlock() || state.inBlockComment();
			final var next = JavaLineScanner.stateAfter(line, state);
			if (beganInBlock) {
				final var closeAt = JavaLineScanner.multilineLiteralCloseIndex(line, state);
				if (closeAt >= 0) {
					final var trailing = line.substring(closeAt);
					final var splitAt = trailingCloserSplit(trailing);
					if (splitAt >= 0) {
						expanded.add((line.substring(0, closeAt) + trailing.substring(0, splitAt)).stripTrailing());
						expanded.add(trailing.substring(splitAt));
						state = next;
						continue;
					}
				}
			}
			expanded.add(line);
			state = next;
		}
		return expanded;
	}

	/**
	 * The index in {@code trailing} at which a pure closing-bracket run begins, or -1 if {@code trailing}
	 * is not {@code [statement terminators]} followed by a run of only {@code }})];,} and whitespace that
	 * contains at least one {@code &#125;}. Requiring a brace keeps this to a block/lambda close (e.g. a
	 * value lambda's {@code });}); a bare call-closing {@code )} after a text-block argument is left inline.
	 */
	@CheckReturnValue
	private static int trailingCloserSplit(@Nonnull String trailing) {
		var closerStart = -1;
		for (var i = 0; i < trailing.length(); ++i) {
			final var c = trailing.charAt(i);
			if (c == '}' || c == ')') {
				closerStart = i;
				break;
			}
			if (c != ';' && c != ',' && !Character.isWhitespace(c))
				return -1;
		}
		if (closerStart < 0)
			return -1;
		var hasBrace = false;
		for (var i = closerStart; i < trailing.length(); ++i) {
			final var c = trailing.charAt(i);
			if (c != '}' && c != ')' && c != ']' && c != ';' && c != ',' && !Character.isWhitespace(c))
				return -1;
			if (c == '}')
				hasBrace = true;
		}
		return hasBrace ? closerStart : -1;
	}

	private JavaSpanReindenter() {
	}
}