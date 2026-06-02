package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.format.SpanReformat;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferDirectBooleanReturnFixer implements CheckstyleFixer {
	/** A parsed {@code return <expr>;}: {@code literal} is the boolean when it is {@code true}/{@code false},
	 *  else {@code null} and {@code expr} carries the verbatim expression text; {@code comment} is a
	 *  relocated trailing {@code //} comment (or empty). */
	private record BodyInfo(int startLine, int endLine, @Nullable Boolean literal, @Nonnull String expr, @Nonnull String comment) {}

	/** The condition collapsed onto one line, plus any {@code //} comments relocated to a trailing comment. */
	private record CondInfo(@Nonnull String code, @Nonnull String trailingComment, boolean hasUnicodeEscape) {}

	private record ParenLoc(int line, int column) {}

	private record ReturnParse(@Nonnull String expr, @Nonnull String comment) {}

	private static final String SKIP_AMBIGUOUS_OPERATOR = "ambiguous comparison operator";
	private static final String SKIP_ELSE_BODY = "no simple collapsible else or trailing return";
	private static final String SKIP_MULTILINE = "multi-line if condition";
	private static final String SKIP_THEN_BODY = "if body is not a simple collapsible return";
	private static final String SKIP_TRAILING_COMMENT = "comment between condition and body";
	private static final String SKIP_UNICODE = "Unicode escape in condition";

	/**
	 * Collects the condition source spanning {@code (ifLineIndex, openParen)} to the matching close paren at
	 * {@code (endLineIndex, closeColumn)}: each physical line is split into code and a code-level {@code //}
	 * line comment, the code parts are tight-joined onto one line, and the line comments are relocated to a
	 * single trailing comment. Returns {@code null} when the span crosses a multi-line literal (a block
	 * comment that spans lines, or a text block), which cannot be collapsed without corrupting significant
	 * whitespace.
	 */
	@CheckReturnValue
	@Nullable
	private static CondInfo collectCondition(
			@Nonnull List<String> lines,
			int ifLineIndex,
			int openParen,
			int endLineIndex,
			int closeColumn
	) {
		final var codeParts = new ArrayList<String>();
		final var comments = new ArrayList<String>();
		var hasUnicodeEscape = false;
		var state = LexerState.NONE;
		for (var i = ifLineIndex; i <= endLineIndex; ++i) {
			if (i > ifLineIndex && (state.inBlockComment() || state.inTextBlock()))
				return null;
			final var line = lines.get(i);
			final var startColumn = i == ifLineIndex ? openParen + 1 : 0;
			final var endColumn = i == endLineIndex ? closeColumn : line.length();
			final var lineComment = JavaLineScanner.firstLineComment(line, state);
			final String codePart;
			if (lineComment >= startColumn && lineComment < endColumn) {
				codePart = line.substring(startColumn, lineComment);
				comments.add(line.substring(lineComment, endColumn));
			}
			else
				codePart = line.substring(startColumn, endColumn);
			if (codePart.contains("\\u"))
				hasUnicodeEscape = true;
			codeParts.add(codePart);
			state = JavaLineScanner.stateAfter(line, state);
		}
		return new CondInfo(SpanReformat.collapse(codeParts), mergeComments(comments), hasUnicodeEscape);
	}

	@CheckReturnValue
	@Nonnull
	private static String combineAnd(@Nonnull String left, @Nonnull String right) {
		return wrapForAnd(left) + " && " + wrapForAnd(right);
	}

	@CheckReturnValue
	@Nonnull
	private static String combineOr(@Nonnull String left, @Nonnull String right) {
		return wrapForOr(left) + " || " + wrapForOr(right);
	}

	/**
	 * For a same-literal collapse whose condition has a side effect, returns the statement line(s) that
	 * preserve that effect (emitted before {@code return LIT;}), or {@code null} when the effect cannot be
	 * hoisted. Mirrors the check's three extractable shapes: a whole statement-expression -> {@code C;}; a
	 * comparison wrapping one -> that operand as {@code <expr>;} ({@code ++i > 0} -> {@code ++i;}); and
	 * {@code pure && sideEffect()} -> {@code if (pure)} then {@code sideEffect();}. A leading cast is stripped
	 * first ({@code (boolean) box()} -> {@code box();}), mirroring the check's cast transparency.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> extractSideEffect(@Nonnull String cond, @Nonnull String indent) {
		// Strip parens wrapping the whole condition, then any leading casts, before the shape scan: the check
		// classifies the unwrapped operand, so a wholly-parenthesized side-effecting condition (e.g.
		// (list.add(x)), (size <= refresh())) or a cast over one ((boolean) box()) must be unwrapped here too,
		// otherwise every operator sits at depth >= 1 and the shape-1 fallback would emit a non-statement
		// expression verbatim. Looping (not recursing) keeps a pathological cast chain off the call stack.
		var expr = JavaLineScanner.stripOuterParens(cond);
		for (var afterCast = stripLeadingCast(expr); afterCast != null; afterCast = stripLeadingCast(expr))
			expr = JavaLineScanner.stripOuterParens(afterCast);
		final var masked = JavaLineScanner.stripCommentsAndStrings(expr, LexerState.NONE);
		// Shape 1a: a depth-0 assignment is the whole condition (assignment binds looser than every operator
		// in its right-hand side), so emit it verbatim rather than splitting at an operator inside the RHS.
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (depth == 0 && isAssignEq(masked, i))
				return List.of(indent + expr + ";");
		}
		// Shape 3: last depth-0 '&&'. If the right operand carries the side effect it must run only when the
		// left is truthy -> if (left) right;. Otherwise the right is pure (its short-circuited evaluation is
		// unobservable) and only the left's side effect survives -> left;.
		depth = 0;
		var lastAnd = -1;
		for (var i = 0; i + 1 < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (depth == 0 && c == '&' && masked.charAt(i + 1) == '&')
				lastAnd = i;
		}
		if (lastAnd >= 0) {
			final var left = expr.substring(0, lastAnd).strip();
			final var right = expr.substring(lastAnd + 2).strip();
			if (hasSideEffect(right)) {
				return List.of(
						indent + "if (" + JavaLineScanner.stripOuterParens(left) + ")",
						indent + "\t" + JavaLineScanner.stripOuterParens(right) + ";"
				);
			}
			if (hasSideEffect(left))
				return List.of(indent + JavaLineScanner.stripOuterParens(left) + ";");
			return null;
		}
		// Shape 2: a single clean depth-0 comparison wrapping exactly one side-effecting operand -> that
		// operand;. A comparison is a two-char ==/!=/<=/>= or a whitespace-surrounded lone </>; never a shift
		// (<<, >>, >>>) or a generic type argument, which text cannot otherwise distinguish from a comparison.
		// More than one depth-0 comparison (a chained a < b == c) can't be split at the top-level operator
		// without precedence parsing, so refuse rather than split at the wrong one.
		depth = 0;
		var relPos = -1;
		var relLen = 0;
		var relCount = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (depth == 0) {
				final var next = i + 1 < masked.length() ? masked.charAt(i + 1) : '\0';
				final var prev = i > 0 ? masked.charAt(i - 1) : '\0';
				final int len;
				if ((c == '=' || c == '!' || c == '<' || c == '>') && next == '=')
					len = 2;
				else if ((c == '<' || c == '>') && prev != c && next != c && !(c == '>' && prev == '-')
						&& Character.isWhitespace(prev) && Character.isWhitespace(next))
					len = 1;
				else
					len = 0;
				if (len > 0) {
					++relCount;
					relPos = i;
					relLen = len;
				}
			}
		}
		if (relCount > 1)
			return null;
		if (relCount == 1) {
			final var left = expr.substring(0, relPos).strip();
			final var right = expr.substring(relPos + relLen).strip();
			if (hasSideEffect(left) && !hasSideEffect(right))
				return List.of(indent + normalizePostfix(JavaLineScanner.stripOuterParens(left)) + ";");
			if (hasSideEffect(right) && !hasSideEffect(left))
				return List.of(indent + normalizePostfix(JavaLineScanner.stripOuterParens(right)) + ";");
			return null;
		}
		// Shape 1 (primary): the whole condition is a statement-expression (method/constructor call, inc/dec).
		// A tight (unspaced) lone '<'/'>' comparison was skipped by shape 2 (text cannot tell it from a generic
		// type argument), and a relational expression is not a statement-expression, so refuse rather than emit
		// non-compiling code. Balanced depth-0 '<'/'>' (generics inside a statement-expression) are fine.
		if (hasUnbalancedAngleBrackets(masked))
			return null;
		return List.of(indent + expr + ";");
	}

	/**
	 * Locates the {@code )} matching the {@code (} at {@code (ifLineIndex, openParen)}, threading
	 * {@link JavaLineScanner} lexer state forward across physical lines so parentheses inside literals and
	 * comments (including a block comment or text block that spans lines) do not count. Returns {@code null}
	 * when the group never closes (unterminated block comment / truncated source).
	 */
	@CheckReturnValue
	@Nullable
	private static ParenLoc findCloseParen(@Nonnull List<String> lines, int ifLineIndex, int openParen) {
		var depth = 0;
		var state = LexerState.NONE;
		for (var i = ifLineIndex; i < lines.size(); ++i) {
			final var line = lines.get(i);
			final var masked = JavaLineScanner.stripCommentsAndStrings(line, state);
			for (var c = i == ifLineIndex ? openParen : 0; c < masked.length(); ++c) {
				final var ch = masked.charAt(c);
				if (ch == '(')
					++depth;
				else if (ch == ')') {
					--depth;
					if (depth == 0)
						return new ParenLoc(i, c);
				}
			}
			state = JavaLineScanner.stateAfter(line, state);
		}
		return null;
	}

	/**
	 * Whether {@code expr} has a top-level (paren/bracket/brace depth 0) operator that binds looser than the
	 * operator it is about to be joined under, so it needs wrapping: a ternary {@code ?} or an assignment
	 * always, plus {@code ||} when {@code includeOr} (i.e. the join operator is {@code &&}).
	 */
	@CheckReturnValue
	private static boolean hasLooseDepth0Op(@Nonnull String expr, boolean includeOr) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(expr, LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			switch (c) {
				case '(', '[', '{' -> ++depth;
				case ')', ']', '}' -> --depth;
				case '=' -> {
					if (depth == 0 && isAssignEq(masked, i))
						return true;
				}
				case '?' -> {
					if (depth == 0)
						return true;
				}
				case '|' -> {
					if (includeOr && depth == 0 && i + 1 < masked.length() && masked.charAt(i + 1) == '|')
						return true;
				}
				default -> {
				}
			}
		}
		return false;
	}

	/**
	 * True if the (masked) condition contains a state-mutating construct: a method/constructor call, an
	 * increment/decrement, or an assignment. Conservative (a pure-looking method call still counts), so a
	 * same-literal collapse never silently drops a possible side effect.
	 */
	@CheckReturnValue
	private static boolean hasSideEffect(@Nonnull String expr) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(expr, LexerState.NONE);
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(') {
				var j = i - 1;
				while (j >= 0 && Character.isWhitespace(masked.charAt(j)))
					--j;
				if (j >= 0 && Character.isJavaIdentifierPart(masked.charAt(j)))
					return true;
			}
			else if ((c == '+' || c == '-') && i + 1 < masked.length() && masked.charAt(i + 1) == c)
				return true;
			else if (isAssignEq(masked, i))
				return true;
		}
		return false;
	}

	/**
	 * True if the masked expression has an unequal count of depth-0 lone {@code <} and {@code >}, a tight
	 * (unspaced) comparison operator that shape 2's whitespace-gated scan could not classify (text cannot tell
	 * it from a generic type argument). Shifts ({@code <<}/{@code >>}/{@code >>>}), two-char comparisons
	 * ({@code <=}/{@code >=}), and {@code ->} are excluded; generic type arguments contribute balanced pairs
	 * and so do not trip this. Depth-0 means outside {@code ()}/{@code []}/{@code {}}.
	 */
	@CheckReturnValue
	private static boolean hasUnbalancedAngleBrackets(@Nonnull String masked) {
		var depth = 0;
		var angles = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (depth == 0 && (c == '<' || c == '>')) {
				final var next = i + 1 < masked.length() ? masked.charAt(i + 1) : '\0';
				final var prev = i > 0 ? masked.charAt(i - 1) : '\0';
				if (c == prev || c == next || next == '=' || (c == '>' && prev == '-'))
					continue;
				angles += c == '<' ? 1 : -1;
			}
		}
		return angles != 0;
	}

	/**
	 * True if the {@code =} at index {@code i} in a masked expression is an assignment operator (plain
	 * {@code =} or a compound {@code +=}/{@code &=}/{@code <<=}/… ), not part of a comparison
	 * ({@code ==}/{@code !=}/{@code <=}/{@code >=}). The tricky case is {@code <<=}/{@code >>=}/{@code >>>=}:
	 * the {@code =} is preceded by {@code <}/{@code >} like {@code <=}/{@code >=}, but a doubled preceding
	 * {@code <}/{@code >} marks a shift-assign rather than a comparison.
	 */
	@CheckReturnValue
	private static boolean isAssignEq(@Nonnull String masked, int i) {
		if (masked.charAt(i) != '=' || (i + 1 < masked.length() && masked.charAt(i + 1) == '='))
			return false;
		if (i == 0)
			return true;
		final var prev = masked.charAt(i - 1);
		if (prev == '=' || prev == '!')
			return false;
		if (prev == '<' || prev == '>')
			return i >= 2 && masked.charAt(i - 2) == prev;
		return true;
	}

	@CheckReturnValue
	private static boolean isAtomicCond(@Nonnull String s) {
		var depth = 0;
		final var len = s.length();
		var i = 0;
		while (i < len) {
			final var cp = s.codePointAt(i);
			if (cp == '(')
				++depth;
			else if (cp == ')')
				--depth;
			else if (depth == 0
					&& !Character.isLetterOrDigit(cp)
					&& cp != '_'
					&& cp != '.'
					&& cp != '$')
				return false;
			i += Character.charCount(cp);
		}
		return depth == 0;
	}

	/**
	 * Joins the {@code //} comments relocated out of the collapsed statement into a single trailing comment.
	 * Each comment's {@code //} prefix and surrounding whitespace is stripped and the non-empty remainders are
	 * joined with a single space under one leading {@code // }. Content-less comments are dropped; an empty
	 * string is returned when nothing survives.
	 */
	@CheckReturnValue
	@Nonnull
	private static String mergeComments(@Nonnull List<String> comments) {
		final var texts = new ArrayList<String>();
		for (var comment : comments) {
			final var text = comment.substring(2).strip();
			if (!text.isEmpty())
				texts.add(text);
		}
		return texts.isEmpty() ? "" : "// " + String.join(" ", texts);
	}

	/**
	 * Rewrites a bare postfix increment/decrement operand to its prefix form ({@code i++} -> {@code ++i}) when it is
	 * extracted as a standalone statement, where the value is discarded and the project prefers prefix.
	 * Only a plain lvalue (identifier with optional {@code .} member access or {@code [ ]} indexing) is rewritten; an
	 * operand whose postfix value is consumed (an assignment RHS, a call argument) has a depth-0 operator or
	 * whitespace and is returned unchanged.
	 */
	@CheckReturnValue
	private static String normalizePostfix(@Nonnull String operand) {
		if (operand.length() < 3 || !(operand.endsWith("++") || operand.endsWith("--")))
			return operand;
		final var base = operand.substring(0, operand.length() - 2);
		var depth = 0;
		for (var i = 0; i < base.length(); ++i) {
			final var c = base.charAt(i);
			if (c == '(' || c == '[')
				++depth;
			else if (c == ')' || c == ']')
				--depth;
			else if (depth == 0 && c != '.' && !Character.isJavaIdentifierPart(c))
				return operand;
		}
		return operand.substring(operand.length() - 2) + base;
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseBody(
			@Nonnull List<String> lines,
			int condEndLineIndex,
			@Nonnull String indent,
			@Nonnull String afterCond
	) {
		if (afterCond.isEmpty())
			return parseSingleReturnLine(lines, condEndLineIndex + 1, indent + "\t", condEndLineIndex + 1);
		if (afterCond.equals("{")) {
			if (condEndLineIndex + 2 >= lines.size())
				return null;
			if (!(indent + "}").equals(lines.get(condEndLineIndex + 2)))
				return null;
			return parseSingleReturnLine(lines, condEndLineIndex + 1, indent + "\t", condEndLineIndex + 2);
		}
		return returnBodyInfo(parseReturnExpr(afterCond), condEndLineIndex, condEndLineIndex);
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseElse(@Nonnull List<String> lines, int candidateLineIndex, @Nonnull String indent) {
		if (candidateLineIndex >= lines.size())
			return null;
		final var firstLine = lines.get(candidateLineIndex);
		if ((indent + "else").equals(firstLine))
			return parseSingleReturnLine(lines, candidateLineIndex + 1, indent + "\t", candidateLineIndex + 1);
		if ((indent + "else {").equals(firstLine)) {
			if (candidateLineIndex + 2 >= lines.size())
				return null;
			if (!(indent + "}").equals(lines.get(candidateLineIndex + 2)))
				return null;
			return parseSingleReturnLine(lines, candidateLineIndex + 1, indent + "\t", candidateLineIndex + 2);
		}
		return null;
	}

	/**
	 * Parses a single-line {@code return <expr>;} (optionally trailed by a {@code //} comment) from {@code s},
	 * returning the verbatim expression text and the relocated comment, or {@code null} when {@code s} is not a
	 * complete single-line valued return (e.g. a return whose expression spans physical lines lacks the
	 * terminating {@code ;} on this text and is refused).
	 */
	@CheckReturnValue
	@Nullable
	private static ReturnParse parseReturnExpr(@Nonnull String s) {
		final var lineComment = JavaLineScanner.firstLineComment(s, LexerState.NONE);
		final var codePart = (lineComment < 0 ? s : s.substring(0, lineComment)).stripTrailing();
		final var comment = lineComment < 0 ? "" : s.substring(lineComment).strip();
		if (!codePart.startsWith("return ") || !codePart.endsWith(";"))
			return null;
		final var expr = codePart.substring("return ".length(), codePart.length() - 1).strip();
		if (expr.isEmpty())
			return null;
		return new ReturnParse(expr, comment);
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo parseSingleReturnLine(
			@Nonnull List<String> lines,
			int returnLineIndex,
			@Nonnull String returnIndent,
			int rangeEndLineIndex
	) {
		if (returnLineIndex >= lines.size())
			return null;
		final var line = lines.get(returnLineIndex);
		if (!line.startsWith(returnIndent))
			return null;
		return returnBodyInfo(parseReturnExpr(line.substring(returnIndent.length())), returnLineIndex, rangeEndLineIndex);
	}

	@CheckReturnValue
	@Nullable
	private static BodyInfo returnBodyInfo(@Nullable ReturnParse parsed, int startLine, int endLine) {
		if (parsed == null)
			return null;
		final var unwrapped = JavaLineScanner.stripOuterParens(parsed.expr());
		final var literal = unwrapped.equals("true")
				? Boolean.TRUE
				: unwrapped.equals("false") ? Boolean.FALSE : null;
		return new BodyInfo(startLine, endLine, literal, parsed.expr(), parsed.comment());
	}

	@CheckReturnValue
	@Nonnull
	private static String simplifyNegation(@Nonnull String cond) {
		final var stripped = cond.strip();
		if (stripped.startsWith("!") && !stripped.startsWith("!=")) {
			final var rest = stripped.substring(1).strip();
			if (!rest.isEmpty() && isAtomicCond(rest))
				return rest;
		}
		if (isAtomicCond(stripped))
			return "!" + stripped;
		return "!(" + stripped + ")";
	}

	/**
	 * If {@code expr} begins with a cast ({@code (Type) operand}), returns the operand text with the leading
	 * cast removed; otherwise {@code null}. A leading parenthesized group whose {@code )} is not the last
	 * character (so it does not wrap the whole expression) and is followed directly by an operand start (a Java
	 * identifier start or a nested {@code (}) is a cast, since two adjacent primaries are valid Java only as a
	 * cast. A group followed by {@code .}/{@code [}/an operator is a parenthesized receiver or operand and is
	 * left alone. Callers pass an already outer-stripped expression, so a whole-wrapping group cannot appear.
	 */
	@CheckReturnValue
	@Nullable
	private static String stripLeadingCast(@Nonnull String expr) {
		if (expr.isEmpty() || expr.charAt(0) != '(')
			return null;
		final var masked = JavaLineScanner.stripCommentsAndStrings(expr, LexerState.NONE);
		var depth = 0;
		var close = -1;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(')
				++depth;
			else if (c == ')' && --depth == 0) {
				close = i;
				break;
			}
		}
		if (close < 0 || close == masked.length() - 1)
			return null;
		final var operand = expr.substring(close + 1).stripLeading();
		if (operand.isEmpty())
			return null;
		final var first = operand.charAt(0);
		if (!(Character.isJavaIdentifierStart(first) || first == '('))
			return null;
		// `instanceof` is the only infix keyword operator that can follow a parenthesized primary, so
		// `(recv) instanceof T` is a relational expression, not a cast; refusing here avoids stripping the
		// receiver as a phantom cast and emitting `if (instanceof T)`.
		if (operand.startsWith("instanceof")
				&& (operand.length() == "instanceof".length()
						|| !Character.isJavaIdentifierPart(operand.charAt("instanceof".length()))))
			return null;
		return operand;
	}

	@CheckReturnValue
	@Nonnull
	private static String wrapForAnd(@Nonnull String expr) {
		return hasLooseDepth0Op(expr, true) ? "(" + expr + ")" : expr;
	}

	@CheckReturnValue
	@Nonnull
	private static String wrapForOr(@Nonnull String expr) {
		return hasLooseDepth0Op(expr, false) ? "(" + expr + ")" : expr;
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var ifLine = lines.get(lineIndex);
		if (column < 0 || column + 4 > ifLine.length())
			return null;
		if (!ifLine.startsWith("if ", column))
			return null;
		final var openParen = column + 3;
		if (openParen >= ifLine.length() || ifLine.charAt(openParen) != '(')
			return null;

		final var close = findCloseParen(lines, lineIndex, openParen);
		if (close == null)
			return new SkipResult(SKIP_MULTILINE);

		final var indent = ifLine.substring(0, column);
		final var condInfo = collectCondition(lines, lineIndex, openParen, close.line(), close.column());
		if (condInfo == null)
			return new SkipResult(SKIP_MULTILINE);
		if (condInfo.hasUnicodeEscape())
			return new SkipResult(SKIP_UNICODE);
		final var cond = condInfo.code();
		if (cond.isBlank())
			return null;
		final var afterCond = lines.get(close.line()).substring(close.column() + 1).stripLeading();

		final var thenInfo = parseBody(lines, close.line(), indent, afterCond);
		if (thenInfo == null)
			return new SkipResult(afterCond.startsWith("//") ? SKIP_TRAILING_COMMENT : SKIP_THEN_BODY);

		final var afterThen = thenInfo.endLine() + 1;
		var elseInfo = parseElse(lines, afterThen, indent);
		if (elseInfo == null)
			elseInfo = parseSingleReturnLine(lines, afterThen, indent, afterThen);
		if (elseInfo == null)
			return new SkipResult(SKIP_ELSE_BODY);

		final var comments = new ArrayList<String>();
		if (!condInfo.trailingComment().isEmpty())
			comments.add(condInfo.trailingComment());
		if (!thenInfo.comment().isEmpty())
			comments.add(thenInfo.comment());
		if (!elseInfo.comment().isEmpty())
			comments.add(elseInfo.comment());
		final var trailingComment = mergeComments(comments);
		final var commentSuffix = trailingComment.isEmpty() ? "" : " " + trailingComment;

		final var thenLiteral = thenInfo.literal();
		final var elseLiteral = elseInfo.literal();

		// Same literal on both branches: the condition's value is irrelevant. Drop a pure condition;
		// otherwise hoist its side effect into a statement ahead of the return so it is preserved.
		if (thenLiteral != null && thenLiteral.equals(elseLiteral)) {
			final var returnLine = indent + "return " + (thenLiteral ? "true" : "false") + ";" + commentSuffix;
			if (!hasSideEffect(cond))
				return new FixResult(lineIndex, elseInfo.endLine(), List.of(returnLine));
			final var extraction = extractSideEffect(cond, indent);
			if (extraction == null)
				return new SkipResult(SKIP_AMBIGUOUS_OPERATOR);
			final var replacement = new ArrayList<>(extraction);
			replacement.add(returnLine);
			return new FixResult(lineIndex, elseInfo.endLine(), replacement);
		}

		final String returnExpr;
		if (thenLiteral != null && elseLiteral != null)
			returnExpr = thenLiteral ? cond : simplifyNegation(cond);
		else if (thenLiteral == null && elseLiteral != null) {
			returnExpr = elseLiteral
					? combineOr(simplifyNegation(cond), thenInfo.expr())
					: combineAnd(cond, thenInfo.expr());
		}
		else if (thenLiteral != null && elseLiteral == null) {
			returnExpr = thenLiteral
					? combineOr(cond, elseInfo.expr())
					: combineAnd(simplifyNegation(cond), elseInfo.expr());
		}
		else
			return null;

		final var returnLine = indent + "return " + returnExpr + ";" + commentSuffix;
		return new FixResult(lineIndex, elseInfo.endLine(), List.of(returnLine));
	}
}