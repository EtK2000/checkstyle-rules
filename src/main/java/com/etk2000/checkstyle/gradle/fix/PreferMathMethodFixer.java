package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferMathMethodCheck;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferMathMethodFixer implements CheckstyleFixer {
	/**
	 * Resolved positions of a single-statement if/else (or if-without-else)
	 * after accounting for optional braces on either side. {@code thenBodyIndex}
	 * is the line containing the then-branch statement; {@code elseBodyIndex}
	 * is the line containing the else-branch statement (or {@code -1} if no
	 * else); {@code blockEndIndex} is the last physical line of the if-else
	 * block (the closing {@code \}} after the else body when braced, or the
	 * else-body line when unbraced; for no-else it's the closing {@code \}}
	 * of the then-block when braced, or the then-body line when unbraced).
	 */
	private record IfElseLayout(
			int thenBodyIndex,
			int elseBodyIndex,
			int blockEndIndex,
			boolean hasElse
	) {}

	/**
	 * One {@code left OP right ? trueBranch : falseBranch} occurrence, located by
	 * {@link #ternarySpanCovering}. {@code start} is inclusive and {@code end} exclusive,
	 * both indices into the line the span was found in.
	 */
	private record TernarySpan(
			int start,
			int end,
			@Nonnull String left,
			@Nonnull String op,
			@Nonnull String right,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {}

	private static final Pattern ASSIGN_BODY_PATTERN = Pattern.compile(
			"^\\s*([\\w.\\[\\]]+)\\s*=\\s*(.+?)\\s*;\\s*$"
	);
	private static final Pattern COMPOUND_ASSIGN_BODY_PATTERN = Pattern.compile(
			"^\\s*([\\w.\\[\\]]+)\\s*([+\\-*/%&|^]=|<<=|>>>?=)\\s*(.+?)\\s*;\\s*$"
	);
	private static final Pattern DECL_LINE_PATTERN = Pattern.compile(
			"^\\s*(?:final\\s+)?(?!(?:assert|break|continue|return|throw|yield)\\b)\\S+\\s+(\\w+)\\s*;\\s*$"
	);
	private static final Pattern IF_COMPARISON_PATTERN = Pattern.compile(
			"^(\\s*)if\\s*\\(\\s*(.+?)\\s*(>=?|<=?)\\s*(.+?)\\s*\\)\\s*(\\{)?\\s*$"
	);
	private static final Pattern IF_LINE_PATTERN = Pattern.compile(
			"^\\s*(?:\\}\\s*)?(?:else\\s+)?if\\s*\\("
	);
	private static final Pattern RETURN_BODY_PATTERN = Pattern.compile("^\\s*return\\s+(.+?)\\s*;\\s*$");
	private static final Pattern RETURN_VAR_PATTERN = Pattern.compile("^\\s*return\\s+(\\w+)\\s*;\\s*$");
	// init capture excludes ',' to reject multi-decls like `int r = a, s = b;`
	private static final Pattern VAR_DECL_INIT_PATTERN = Pattern.compile(
			"^(\\s*)(?:final\\s+)?(?:\\w+(?:\\s*\\[\\s*\\])*\\s+)(\\w+)\\s*=\\s*([^,]+?)\\s*;\\s*$"
	);

	@CheckReturnValue
	@Nonnull
	private static String buildClamp(
			@Nonnull String line,
			int outerStart,
			int outerClose,
			@Nonnull String outerArg,
			@Nonnull String innerArg1,
			@Nonnull String innerArg2,
			boolean isOuterMax
	) {
		final String replacement;
		if (isOuterMax)
			replacement = "Math.clamp(" + innerArg2 + ", " + outerArg + ", " + innerArg1 + ")";
		else
			replacement = "Math.clamp(" + innerArg2 + ", " + innerArg1 + ", " + outerArg + ")";
		return line.substring(0, outerStart) + replacement + line.substring(outerClose + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String computeMathExpr(
			@Nonnull String left,
			@Nonnull String op,
			@Nonnull String right,
			@Nonnull String thenValue,
			@Nonnull String elseValue
	) {
		if (isZero(right)) {
			final var abs = tryFixAbs(left, op, thenValue, elseValue);
			if (abs != null)
				return abs;
		}
		if (isZero(left)) {
			final var abs = tryFixAbsZeroLeft(right, op, thenValue, elseValue);
			if (abs != null)
				return abs;
		}
		return tryFixMaxMin(left, op, right, thenValue, elseValue);
	}

	/**
	 * Whether the text starting at {@code start} is the tail of a longer name:
	 * the character before it continues an identifier or is the dot of a
	 * qualified receiver.
	 */
	@CheckReturnValue
	private static boolean continuesAName(@Nonnull String line, int start) {
		if (start == 0)
			return false;
		final var prev = line.charAt(start - 1);
		return Character.isJavaIdentifierPart(prev) || prev == '.';
	}

	/**
	 * Whether {@code line}, ignoring trailing whitespace, ends in one of {@code enders}.
	 *
	 * <p>A linear pre-gate for the line-shape patterns, every one of which anchors on {@code $}
	 * after a required closing character. Each carries two lazy {@code (.+?)} groups, so on a line
	 * that cannot match they re-expand the second group from every position the first can end at,
	 * which is quadratic in the line's length. Rejecting those here costs one backward scan. This
	 * only narrows what reaches the matcher, since a line the pattern would have accepted ends in
	 * the same character the gate requires.
	 */
	@CheckReturnValue
	private static boolean endsWithAny(@Nonnull String line, char... enders) {
		var end = line.length();
		while (end > 0 && Character.isWhitespace(line.charAt(end - 1)))
			--end;
		if (end == 0)
			return false;
		final var last = line.charAt(end - 1);
		for (var ender : enders) {
			if (last == ender)
				return true;
		}
		return false;
	}

	/**
	 * Finds the index of {@code target} at paren nesting depth 0, starting from {@code from}.
	 * Returns -1 if not found before the end of the string or before depth goes negative.
	 */
	@CheckReturnValue
	private static int findAtDepthZero(@Nonnull String s, int from, char target) {
		// Mask literal/comment content (positions preserved) so a paren or the
		// target char inside a string/char literal or comment is not counted.
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var depth = 0;
		for (var i = from; i < scan.length(); ++i) {
			final var c = scan.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')') {
				if (depth == 0)
					return -1;
				--depth;
			}
			else if (c == target && depth == 0)
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static String fixClamp(@Nonnull String line, int charColumn) {
		final var result = tryFixClampOuter(line, charColumn, "Math.max(", "Math.min(", true);
		if (result != null)
			return result;

		return tryFixClampOuter(line, charColumn, "Math.min(", "Math.max(", false);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt fixIfShape(@Nonnull List<String> lines, int lineIndex) {
		final var ifLine = lines.get(lineIndex);
		if (!endsWithAny(ifLine, ')', '{'))
			return null;
		final var ifMatch = IF_COMPARISON_PATTERN.matcher(ifLine);
		if (!ifMatch.matches())
			return null;
		final var indent = ifMatch.group(1);
		final var leftOp = ifMatch.group(2).strip();
		final var op = ifMatch.group(3);
		final var rightOp = ifMatch.group(4).strip();
		final var ifBraced = ifMatch.group(5) != null;

		final var layout = layoutIfElse(lines, lineIndex, ifBraced);
		if (layout == null)
			return null;
		final var thenLine = lines.get(layout.thenBodyIndex());
		if (!endsWithAny(thenLine, ';'))
			return null;

		final var thenCompound = COMPOUND_ASSIGN_BODY_PATTERN.matcher(thenLine);
		if (thenCompound.matches()) {
			return tryCompoundAssignShape(
					lines,
					lineIndex,
					layout,
					indent,
					leftOp,
					op,
					rightOp,
					thenCompound.group(1),
					thenCompound.group(2),
					thenCompound.group(3).strip()
			);
		}

		final var thenAssign = ASSIGN_BODY_PATTERN.matcher(thenLine);
		if (thenAssign.matches()) {
			final var plainResult = tryPlainAssignShape(
					lines,
					lineIndex,
					layout,
					indent,
					leftOp,
					op,
					rightOp,
					thenAssign.group(1),
					thenAssign.group(2).strip()
			);
			if (plainResult != null)
				return plainResult;
			if (lineIndex >= 1) {
				return tryInitOverwriteShape(
						lines,
						lineIndex,
						layout,
						indent,
						leftOp,
						op,
						rightOp,
						thenAssign.group(1),
						thenAssign.group(2).strip()
				);
			}
		}

		final var thenReturn = RETURN_BODY_PATTERN.matcher(thenLine);
		if (thenReturn.matches()) {
			return tryReturnShape(
					lines,
					lineIndex,
					layout,
					indent,
					leftOp,
					op,
					rightOp,
					thenReturn.group(1).strip()
			);
		}

		return null;
	}

	/**
	 * Rewrites the ternary covering {@code column}, or {@code null} when none does.
	 *
	 * <p>{@code maskedLine} is {@code line} with comment and literal content blanked at
	 * the same columns, so a comment or string cannot supply the match: matching the raw
	 * line rewrote {@code // mirrors c > d ? c : d} into {@code // mirrors Math.max(c, d)}
	 * and left the real violation unfixed.
	 *
	 * <p>Column 0 is a real column, not a "no column" sentinel: the check reports a ternary
	 * at its {@code ?} and an if-else at its {@code if}, so either opening a zero-indented
	 * line yields it. A column no match covers means the construct is not locatable, so the
	 * search stays anchored rather than falling back to an unanchored {@code find()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixTernary(@Nonnull String line, @Nonnull String maskedLine, int column) {
		final var span = ternarySpanCovering(maskedLine, column);
		if (span == null)
			return null;

		// Masking blanks comments to spaces and the gaps between operands are optional, so a
		// comment *inside* the expression (`a /* n */ > b ? a : b`) becomes matchable when the
		// raw line was not. The replacement splices the original line, so rewriting would
		// delete it.
		if (!maskedLine.regionMatches(span.start(), line, span.start(), span.end() - span.start()))
			return null;

		if (isZero(span.right())) {
			final var absResult = tryFixAbs(span.left(), span.op(), span.trueBranch(), span.falseBranch());
			if (absResult != null)
				return spliceTernary(line, span, absResult);
		}
		if (isZero(span.left())) {
			final var absResult = tryFixAbsZeroLeft(span.right(), span.op(), span.trueBranch(), span.falseBranch());
			if (absResult != null)
				return spliceTernary(line, span, absResult);
		}

		final var maxMinResult = tryFixMaxMin(
				span.left(), span.op(), span.right(), span.trueBranch(), span.falseBranch()
		);
		if (maxMinResult != null)
			return spliceTernary(line, span, maxMinResult);

		return null;
	}

	@CheckReturnValue
	private static boolean hasMutationBefore(@Nonnull String s, int i) {
		if (i < 2)
			return false;
		final var c = s.charAt(i - 1);
		return (c == '+' || c == '-') && s.charAt(i - 2) == c;
	}

	@CheckReturnValue
	private static boolean isNegation(@Nonnull String expr, @Nonnull String variable) {
		return expr.startsWith("-") && expr.substring(1).strip().equals(variable);
	}

	/**
	 * Whether {@code c} is one of the six characters Java's {@code \s} accepts. Deliberately
	 * not {@link Character#isWhitespace}, which also accepts Unicode separators and so would
	 * widen what counts as a gap inside an expression.
	 *
	 * <p>The vertical tab is written as an escape: raw, it is invisible in diffs and review
	 * tools, and anything that normalized it to a space would drop it from the class while
	 * still compiling.
	 */
	@CheckReturnValue
	private static boolean isTernarySpace(char c) {
		return c == ' ' || c == '\t' || c == '\n' || c == '\u000B' || c == '\f' || c == '\r';
	}

	/**
	 * Whether {@code c} can appear in an operand. ASCII only, matching Java's {@code \w}:
	 * {@link Character#isLetterOrDigit} would admit letters from other scripts and pull a
	 * supplementary character into an operand.
	 */
	@CheckReturnValue
	private static boolean isTernaryWord(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9'
				|| c == '_' || c == '.' || c == '[' || c == ']';
	}

	@CheckReturnValue
	private static boolean isZero(@Nonnull String text) {
		return "0".equals(text);
	}

	/**
	 * Resolves the physical line layout of a single-statement if[/else] block
	 * starting at the if-condition line, handling all four brace combinations
	 * (UU, BU, UB, BB) plus the cuddled {@code } else {} variant. Returns
	 * {@code null} if the layout cannot be parsed (e.g. the closing brace is
	 * missing, the else-body line is absent, or the shape is malformed). When
	 * the if has no matching {@code else} keyword on the next eligible line,
	 * {@code hasElse} is {@code false} and {@code elseBodyIndex} is {@code -1};
	 * callers needing trailing-return semantics should look at
	 * {@code blockEndIndex + 1}.
	 */
	@CheckReturnValue
	@Nullable
	private static IfElseLayout layoutIfElse(@Nonnull List<String> lines, int lineIndex, boolean ifBraced) {
		final var thenBodyIndex = lineIndex + 1;
		if (thenBodyIndex >= lines.size())
			return null;
		if (!ifBraced)
			return resolveElsePart(lines, thenBodyIndex, thenBodyIndex, thenBodyIndex + 1);
		final var afterThenIndex = thenBodyIndex + 1;
		if (afterThenIndex >= lines.size())
			return null;
		final var closeLine = lines.get(afterThenIndex).strip();
		if (closeLine.equals("}"))
			return resolveElsePart(lines, thenBodyIndex, afterThenIndex, afterThenIndex + 1);
		if (closeLine.equals("} else") || closeLine.equals("} else {"))
			return resolveElseBody(lines, thenBodyIndex, afterThenIndex, closeLine.endsWith("{"));
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static IfElseLayout resolveElseBody(
			@Nonnull List<String> lines,
			int thenBodyIndex,
			int elseKeywordIndex,
			boolean elseBraced
	) {
		final var elseBodyIndex = elseKeywordIndex + 1;
		if (elseBodyIndex >= lines.size())
			return null;
		if (elseBraced) {
			final var elseCloseIndex = elseBodyIndex + 1;
			if (elseCloseIndex >= lines.size() || !lines.get(elseCloseIndex).strip().equals("}"))
				return null;
			return new IfElseLayout(thenBodyIndex, elseBodyIndex, elseCloseIndex, true);
		}
		return new IfElseLayout(thenBodyIndex, elseBodyIndex, elseBodyIndex, true);
	}

	@CheckReturnValue
	@Nullable
	private static IfElseLayout resolveElsePart(
			@Nonnull List<String> lines,
			int thenBodyIndex,
			int thenBlockEndIndex,
			int elseSearchIndex
	) {
		// Skip blank-only lines between the then-block end and the else keyword.
		// Java allows arbitrary whitespace there; without this, a `}` + blank line
		// + `else { ... }` would fall through to `hasElse=false`, and
		// `tryInitOverwriteShape` would rewrite the if-block while leaving the
		// orphan else as uncompilable code.
		var idx = elseSearchIndex;
		while (idx < lines.size() && lines.get(idx).isBlank())
			++idx;
		if (idx >= lines.size())
			return new IfElseLayout(thenBodyIndex, -1, thenBlockEndIndex, false);
		final var elseLine = lines.get(idx).strip();
		if (elseLine.equals("else") || elseLine.equals("else {")) {
			final var resolved = resolveElseBody(lines, thenBodyIndex, idx, elseLine.endsWith("{"));
			if (resolved != null)
				return resolved;
			return new IfElseLayout(thenBodyIndex, -1, thenBlockEndIndex, false);
		}
		// If the first non-blank line could mask an else (comment, or `else if`
		// chain), refuse to classify the layout. Returning hasElse=false here
		// would let tryInitOverwriteShape rewrite the if-block while leaving an
		// orphan else block as uncompilable code. Genuine no-else shapes (a
		// trailing statement, the method's closing brace) start with neither a
		// comment marker nor the else keyword and fall through to hasElse=false.
		if (elseLine.startsWith("else") || elseLine.startsWith("//")
				|| elseLine.startsWith("/*") || elseLine.startsWith("*"))
			return null;
		return new IfElseLayout(thenBodyIndex, -1, thenBlockEndIndex, false);
	}

	@CheckReturnValue
	private static int skipTernarySpaces(@Nonnull String s, int i) {
		var j = i;
		while (j < s.length() && isTernarySpace(s.charAt(j)))
			++j;
		return j;
	}

	@CheckReturnValue
	private static int skipTernarySpacesBack(@Nonnull String s, int i) {
		var j = i;
		while (j > 0 && isTernarySpace(s.charAt(j - 1)))
			--j;
		return j;
	}

	@CheckReturnValue
	@Nonnull
	private static String spliceTernary(@Nonnull String line, @Nonnull TernarySpan span, @Nonnull String replacement) {
		return line.substring(0, span.start()) + replacement + line.substring(span.end());
	}

	/**
	 * Splits a call like "Math.min(arg1, arg2)" into its two arguments,
	 * using paren-balancing to find the correct comma.
	 * Returns [arg1, arg2] or null if the structure doesn't match.
	 */
	@CheckReturnValue
	@Nullable
	private static String[] splitInnerArgs(@Nonnull String call, @Nonnull String prefix) {
		if (!call.endsWith(")"))
			return null;

		final var argsStart = prefix.length();
		final var argsEnd = call.length() - 1;

		final var commaIdx = findAtDepthZero(call, argsStart, ',');
		if (commaIdx < 0 || commaIdx >= argsEnd)
			return null;

		return new String[]{
				call.substring(argsStart, commaIdx).strip(),
				call.substring(commaIdx + 1, argsEnd).strip()
		};
	}

	@CheckReturnValue
	@Nonnull
	private static String stripPrefixMutation(@Nonnull String operand) {
		if (operand.startsWith("++") || operand.startsWith("--"))
			return operand.substring(2);
		return operand;
	}

	/**
	 * End of the operand starting at {@code start}, or -1 when none does. Forward counterpart
	 * of {@link #ternaryOperandStart}; the branches after {@code ?} take no mutation prefix.
	 */
	@CheckReturnValue
	private static int ternaryOperandEnd(@Nonnull String s, int start) {
		var i = start;
		if (i < s.length() && (s.charAt(i) == '+' || s.charAt(i) == '-'))
			i = skipTernarySpaces(s, i + 1);
		final var wordStart = i;
		while (i < s.length() && isTernaryWord(s.charAt(i)))
			++i;
		return i == wordStart ? -1 : i;
	}

	/**
	 * Start of the operand ending at {@code end}, or -1 when no operand ends there.
	 *
	 * <p>{@code ++a} reads two ways backwards, as a mutation on the word or as a sign with a
	 * stray {@code +} before it, and only the first starts where the leftmost match does. Both
	 * readings are built and the leftmost wins, which is also what settles {@code +++a} as
	 * {@code ++} then {@code +}. A mutation prefix never reaches across whitespace.
	 */
	@CheckReturnValue
	private static int ternaryOperandStart(@Nonnull String s, int end, boolean allowMutation) {
		var wordStart = end;
		while (wordStart > 0 && isTernaryWord(s.charAt(wordStart - 1)))
			--wordStart;
		if (wordStart == end)
			return -1;

		var start = allowMutation && hasMutationBefore(s, wordStart) ? wordStart - 2 : wordStart;

		final var afterSign = skipTernarySpacesBack(s, wordStart);
		if (afterSign > 0) {
			final var sign = s.charAt(afterSign - 1);
			if (sign == '+' || sign == '-') {
				final var signed = afterSign - 1;
				start = Math.min(start, allowMutation && hasMutationBefore(s, signed) ? signed - 2 : signed);
			}
		}
		return start;
	}

	/**
	 * Parses outward from the {@code ?} at {@code questionIndex}, or {@code null} when the
	 * surrounding text is not {@code left OP right ? trueBranch : falseBranch}.
	 */
	@CheckReturnValue
	@Nullable
	private static TernarySpan ternarySpanAt(@Nonnull String s, int questionIndex) {
		final var rightEnd = skipTernarySpacesBack(s, questionIndex);
		final var rightStart = ternaryOperandStart(s, rightEnd, true);
		if (rightStart < 0)
			return null;

		final var opEnd = skipTernarySpacesBack(s, rightStart);
		if (opEnd == 0)
			return null;
		var opStart = opEnd - 1;
		if (s.charAt(opStart) == '=') {
			if (opStart == 0)
				return null;
			--opStart;
		}
		final var comparison = s.charAt(opStart);
		if (comparison != '>' && comparison != '<')
			return null;

		final var leftEnd = skipTernarySpacesBack(s, opStart);
		final var leftStart = ternaryOperandStart(s, leftEnd, true);
		if (leftStart < 0)
			return null;

		final var trueStart = skipTernarySpaces(s, questionIndex + 1);
		final var trueEnd = ternaryOperandEnd(s, trueStart);
		if (trueEnd < 0)
			return null;

		final var colon = skipTernarySpaces(s, trueEnd);
		if (colon >= s.length() || s.charAt(colon) != ':')
			return null;

		final var falseStart = skipTernarySpaces(s, colon + 1);
		final var falseEnd = ternaryOperandEnd(s, falseStart);
		if (falseEnd < 0)
			return null;

		return new TernarySpan(
				leftStart,
				falseEnd,
				s.substring(leftStart, leftEnd).strip(),
				s.substring(opStart, opEnd),
				s.substring(rightStart, rightEnd).strip(),
				s.substring(trueStart, trueEnd).strip(),
				s.substring(falseStart, falseEnd).strip()
		);
	}

	/**
	 * The ternary span covering {@code column}, or {@code null} when none does.
	 *
	 * <p>Replaces a regex whose {@code [\w.\[\]]+} runs backtracked quadratically on a long
	 * identifier run with no match. Here each character is visited a bounded number of times
	 * per candidate {@code ?}.
	 *
	 * <p>Spans are produced left to right and non-overlapping, reproducing
	 * {@code Matcher.find()}: one whose operand reaches back into an earlier span is skipped
	 * rather than reported, so a chained {@code a > b ? a : b > c ? b : c} still yields only
	 * its first ternary.
	 */
	@CheckReturnValue
	@Nullable
	private static TernarySpan ternarySpanCovering(@Nonnull String s, int column) {
		var from = 0;
		for (var q = s.indexOf('?'); q >= 0; q = s.indexOf('?', q + 1)) {
			final var span = ternarySpanAt(s, q);
			if (span == null || span.start() < from)
				continue;
			if (span.start() > column)
				return null;
			if (column < span.end())
				return span;
			from = span.end();
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryCompoundAssignShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull IfElseLayout layout,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenAssignOp,
			@Nonnull String thenValue
	) {
		if (!layout.hasElse())
			return null;
		final var elseMatch = COMPOUND_ASSIGN_BODY_PATTERN.matcher(lines.get(layout.elseBodyIndex()));
		if (!elseMatch.matches())
			return null;
		if (!thenTarget.equals(elseMatch.group(1)) || !thenAssignOp.equals(elseMatch.group(2)))
			return null;
		final var elseValue = elseMatch.group(3).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		return new FixResult(
				lineIndex,
				layout.blockEndIndex(),
				List.of(indent + thenTarget + " " + thenAssignOp + " " + math + ";")
		);
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixAbs(
			@Nonnull String variable,
			@Nonnull String op,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var stripped = stripPrefixMutation(variable);
		if (">".equals(op) || ">=".equals(op)) {
			if (trueBranch.equals(stripped) && isNegation(falseBranch, stripped))
				return "Math.abs(" + variable + ")";
		}
		if ("<".equals(op) || "<=".equals(op)) {
			if (isNegation(trueBranch, stripped) && falseBranch.equals(stripped))
				return "Math.abs(" + variable + ")";
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixAbsZeroLeft(
			@Nonnull String variable,
			@Nonnull String op,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var stripped = stripPrefixMutation(variable);
		if (">".equals(op) || ">=".equals(op)) {
			if (isNegation(trueBranch, stripped) && falseBranch.equals(stripped))
				return "Math.abs(" + variable + ")";
		}
		if ("<".equals(op) || "<=".equals(op)) {
			if (trueBranch.equals(stripped) && isNegation(falseBranch, stripped))
				return "Math.abs(" + variable + ")";
		}
		return null;
	}

	/**
	 * Tries to parse and fix a clamp pattern whose {@code outerPrefix} (e.g.
	 * "Math.max(") ends at {@code charColumn} and that contains
	 * {@code innerPrefix} (e.g. "Math.min("). Uses paren-balancing to correctly
	 * split arguments even when they contain nested calls, casts, or other
	 * parenthesized expressions.
	 *
	 * <p>The check logs a clamp at its {@code METHOD_CALL}, whose position is the
	 * call's {@code (}, so the prefix must end exactly at {@code charColumn}
	 * rather than be searched for from the start of the line. Without the anchor
	 * the rewrite lands on whichever call comes first, including one the check
	 * deliberately refused. The character before the match is checked too: a
	 * qualified receiver such as {@code MyMath.max(} carries {@code Math.max(} as
	 * a suffix and would otherwise satisfy the anchor by accident, yielding a call
	 * to a {@code clamp} that does not exist.
	 */
	@CheckReturnValue
	@Nullable
	private static String tryFixClampOuter(
			@Nonnull String line,
			int charColumn,
			@Nonnull String outerPrefix,
			@Nonnull String innerPrefix,
			boolean isOuterMax
	) {
		final var outerStart = charColumn - (outerPrefix.length() - 1);
		if (outerStart < 0 || !line.startsWith(outerPrefix, outerStart) || continuesAName(line, outerStart))
			return null;

		final var argsStart = outerStart + outerPrefix.length();
		final var outerClose = JavaLineScanner.matchingCloseParen(line, argsStart - 1);
		if (outerClose < 0)
			return null;

		final var commaIdx = findAtDepthZero(line, argsStart, ',');
		if (commaIdx < 0 || commaIdx >= outerClose)
			return null;

		final var firstArg = line.substring(argsStart, commaIdx).strip();
		final var secondArg = line.substring(commaIdx + 1, outerClose).strip();

		if (secondArg.startsWith(innerPrefix)) {
			final var innerArgs = splitInnerArgs(secondArg, innerPrefix);
			if (innerArgs != null)
				return buildClamp(line, outerStart, outerClose, firstArg, innerArgs[0], innerArgs[1], isOuterMax);
		}

		if (firstArg.startsWith(innerPrefix)) {
			final var innerArgs = splitInnerArgs(firstArg, innerPrefix);
			if (innerArgs != null)
				return buildClamp(line, outerStart, outerClose, secondArg, innerArgs[0], innerArgs[1], isOuterMax);
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String tryFixMaxMin(
			@Nonnull String left,
			@Nonnull String op,
			@Nonnull String right,
			@Nonnull String trueBranch,
			@Nonnull String falseBranch
	) {
		final var strippedLeft = stripPrefixMutation(left);
		final var strippedRight = stripPrefixMutation(right);
		final var trueIsLeft = trueBranch.equals(strippedLeft) && falseBranch.equals(strippedRight);
		final var trueIsRight = trueBranch.equals(strippedRight) && falseBranch.equals(strippedLeft);
		if (!trueIsLeft && !trueIsRight)
			return null;

		final boolean isMax;
		if (">".equals(op) || ">=".equals(op))
			isMax = trueIsLeft;
		else
			isMax = trueIsRight;

		return (isMax ? "Math.max(" : "Math.min(") + left + ", " + right + ")";
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryInitOverwriteShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull IfElseLayout layout,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenValue
	) {
		if (layout.hasElse())
			return null;
		// Defense-in-depth: layoutIfElse should already have returned null when
		// an else block sits past blockEndIndex (separated by blanks or any
		// non-statement content), but verify directly here too. This fixer
		// replaces lines through blockEndIndex and a misjudged hasElse=false
		// would orphan the else, producing uncompilable code.
		final var pastBlockIndex = layout.blockEndIndex() + 1;
		if (pastBlockIndex < lines.size()) {
			final var pastBlockLine = lines.get(pastBlockIndex).strip();
			if (pastBlockLine.equals("else") || pastBlockLine.equals("else {")
					|| pastBlockLine.startsWith("} else"))
				return null;
		}
		final var declMatch = VAR_DECL_INIT_PATTERN.matcher(lines.get(lineIndex - 1));
		if (!declMatch.matches() || !thenTarget.equals(declMatch.group(2)))
			return null;
		final var elseValue = declMatch.group(3).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		final var trailingReturnIndex = layout.blockEndIndex() + 1;
		if (trailingReturnIndex < lines.size()) {
			final var trailingReturn = RETURN_VAR_PATTERN.matcher(lines.get(trailingReturnIndex));
			if (trailingReturn.matches() && thenTarget.equals(trailingReturn.group(1))) {
				return new FixResult(
						lineIndex - 1,
						trailingReturnIndex,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		final var declLine = lines.get(lineIndex - 1);
		final var newDecl = declLine.substring(0, declLine.indexOf('=')) + "= " + math + ";";
		return new FixResult(lineIndex - 1, layout.blockEndIndex(), List.of(newDecl));
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryPlainAssignShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull IfElseLayout layout,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenTarget,
			@Nonnull String thenValue
	) {
		if (!layout.hasElse())
			return null;
		final var elseMatch = ASSIGN_BODY_PATTERN.matcher(lines.get(layout.elseBodyIndex()));
		if (!elseMatch.matches())
			return null;
		if (!thenTarget.equals(elseMatch.group(1)))
			return null;
		final var elseValue = elseMatch.group(2).strip();

		final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
		if (math == null)
			return null;

		final var declIndex = lineIndex - 1;
		final var trailingReturnIndex = layout.blockEndIndex() + 1;
		if (declIndex >= 0 && trailingReturnIndex < lines.size()) {
			final var declMatch = DECL_LINE_PATTERN.matcher(lines.get(declIndex));
			final var returnVarMatch = RETURN_VAR_PATTERN.matcher(lines.get(trailingReturnIndex));
			if (declMatch.matches()
					&& returnVarMatch.matches()
					&& thenTarget.equals(declMatch.group(1))
					&& thenTarget.equals(returnVarMatch.group(1))) {
				return new FixResult(
						declIndex,
						trailingReturnIndex,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		return new FixResult(
				lineIndex,
				layout.blockEndIndex(),
				List.of(indent + thenTarget + " = " + math + ";")
		);
	}

	@CheckReturnValue
	@Nullable
	private static FixAttempt tryReturnShape(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull IfElseLayout layout,
			@Nonnull String indent,
			@Nonnull String leftOp,
			@Nonnull String op,
			@Nonnull String rightOp,
			@Nonnull String thenValue
	) {
		if (layout.hasElse()) {
			final var elseReturnMatch = RETURN_BODY_PATTERN.matcher(lines.get(layout.elseBodyIndex()));
			if (elseReturnMatch.matches()) {
				final var elseValue = elseReturnMatch.group(1).strip();
				final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
				if (math == null)
					return null;
				return new FixResult(
						lineIndex,
						layout.blockEndIndex(),
						List.of(indent + "return " + math + ";")
				);
			}
			return null;
		}

		final var trailingReturnIndex = layout.blockEndIndex() + 1;
		if (trailingReturnIndex < lines.size()) {
			final var trailingReturnMatch = RETURN_BODY_PATTERN.matcher(lines.get(trailingReturnIndex));
			if (trailingReturnMatch.matches()) {
				final var elseValue = trailingReturnMatch.group(1).strip();
				final var math = computeMathExpr(leftOp, op, rightOp, thenValue, elseValue);
				if (math == null)
					return null;
				return new FixResult(
						lineIndex,
						trailingReturnIndex,
						List.of(indent + "return " + math + ";")
				);
			}
		}

		return null;
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// The if-else form is reported under its own key at the `if`, which is not
		// a call position, so the expression rewrites can only mis-target there.
		if (!PreferMathMethodCheck.MSG_METHOD_IF.equals(FixContext.getViolationKey())) {
			// both rewrites index the line, so both need the char index rather than the
			// code-point column the pipeline reports
			final var charColumn = LineText.charIndexOfColumn(line, column);
			var result = fixClamp(line, charColumn);
			if (result == null) {
				// masked through FixerAst so the lexer state is threaded from line 0: a line
				// that merely *closes* a text block starts with """, which per-line masking
				// would read as an opener and blank the real expression after it
				result = fixTernary(line, FixerAst.maskAll(lines).get(lineIndex), charColumn);
			}
			if (result != null)
				return new FixResult(lineIndex, lineIndex, List.of(result));
		}

		final var ifShapeResult = fixIfShape(lines, lineIndex);
		if (ifShapeResult != null)
			return ifShapeResult;

		if (IF_LINE_PATTERN.matcher(line).find())
			return new SkipResult(SkipMessages.MATH_METHOD_SKIP_IF);
		return new SkipResult(SkipMessages.MATH_METHOD_SKIP);
	}
}