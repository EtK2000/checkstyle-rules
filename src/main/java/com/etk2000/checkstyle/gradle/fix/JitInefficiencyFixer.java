package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.JitInefficiencyCheck;
import com.etk2000.checkstyle.LineText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class JitInefficiencyFixer implements CheckstyleFixer {
	private enum LoopKind {
		DO_WHILE,
		FOR,
		WHILE
	}

	private record AssignInfo(
			@Nonnull String indent,
			@Nonnull String lhsText,
			@Nonnull String varName,
			@Nonnull List<String> prepends,
			@Nonnull List<String> appends
	) {}

	private record DeclInfo(
			int lineIdx,
			@Nonnull String typeText,
			@Nonnull String varName,
			@Nonnull String initExpr,
			boolean isVar,
			boolean isField
	) {}

	private record LoopInfo(
			int topLineIdx,
			int endLineIdx,
			@Nonnull LoopKind kind,
			boolean braced
	) {}

	private static final Set<String> SAFE_STRING_METHODS_ON_BUILDER = Set.of(
			"charAt", "chars", "codePointAt", "codePoints", "isEmpty",
			"length", "subSequence"
	);

	/**
	 * Every identifier bound anywhere in {@code masked}, as whole tokens. A run that
	 * does not begin with an identifier start character (so it cannot be an
	 * identifier) contributes nothing: the {@code sb} in a malformed {@code 2sb} is
	 * not a binding.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> boundIdentifiers(@Nonnull List<String> masked) {
		final var names = new HashSet<String>();
		for (var line : masked) {
			var i = 0;
			while (i < line.length()) {
				final var end = LineText.identEnd(line, i);
				if (end == i) {
					i += Character.charCount(line.codePointAt(i));
					continue;
				}
				if (Character.isJavaIdentifierStart(line.codePointAt(i)))
					names.add(line.substring(i, end));
				i = end;
			}
		}
		return names;
	}

	@CheckReturnValue
	@Nonnull
	private static String buildAppendBody(@Nonnull String indent, @Nonnull AssignInfo assign, @Nonnull String builder) {
		final var sb = new StringBuilder(indent);
		final var prepends = assign.prepends().stream().map(op -> rewriteSafeMethodCalls(op, assign.lhsText(), builder)).toList();
		final var appends = assign.appends().stream().map(op -> rewriteSafeMethodCalls(op, assign.lhsText(), builder)).toList();
		if (prepends.isEmpty()) {
			sb.append(builder);
			for (var op : appends)
				sb.append(".append(").append(op).append(')');
		}
		else {
			sb.append(builder).append(".insert(0, ");
			if (prepends.size() == 1)
				sb.append(prepends.getFirst());
			else
				sb.append(String.join(" + ", prepends));
			sb.append(')');
			for (var op : appends)
				sb.append(".append(").append(op).append(')');
		}
		sb.append(';');
		return sb.toString();
	}

	/**
	 * A name for the emitted {@code StringBuilder} local that is not already bound
	 * anywhere in {@code lines}: {@code sb}, then {@code stringBuilder}, then
	 * {@code sb2}, {@code sb3} and so on.
	 *
	 * <p>"Bound" is judged conservatively: any whole-token occurrence of the name
	 * in code counts, wherever it sits. A local in a closed sibling block cannot
	 * actually collide, so this sometimes picks a longer name than it had to; the
	 * inverse mistake is worse than a cosmetic one. Reusing a name bound by a field
	 * or a nested type compiles but silently rebinds the later reference to the new
	 * local, and reusing one bound by a visible local is a duplicate-local error.
	 */
	@CheckReturnValue
	@Nonnull
	private static String builderName(@Nonnull List<String> lines) {
		final var bound = boundIdentifiers(FixerAst.maskAll(lines));
		// the candidates are all distinct, so every rejected one is a distinct member of
		// `bound`: after bound.size() rejections the set is exhausted and the next
		// candidate is free. That makes the search bounded without assuming anything
		// about the buffer.
		for (var i = 0; i <= bound.size(); ++i) {
			final var candidate = switch (i) {
				case 0 -> "sb";
				case 1 -> "stringBuilder";
				default -> "sb" + i;
			};
			if (!bound.contains(candidate))
				return candidate;
		}
		throw new IllegalStateException("no free builder name among " + (bound.size() + 1) + " distinct candidates");
	}

	@CheckReturnValue
	@Nonnull
	private static FixResult buildStringConcatReplacement(
			@Nonnull List<String> lines,
			@Nonnull DeclInfo decl,
			@Nonnull LoopInfo loop,
			@Nonnull AssignInfo assign,
			int bodyLineIdx
	) {
		final var declIndent = decl.isField() ? LineText.extractIndent(lines.get(loop.topLineIdx())) : LineText.extractIndent(lines.get(decl.lineIdx()));
		final var builder = builderName(lines);
		final var newBody = buildAppendBody(assign.indent(), assign, builder);

		final var replacement = new ArrayList<String>();
		final int spanStart;
		if (decl.isField()) {
			spanStart = loop.topLineIdx();
			replacement.add(declIndent + "final var " + builder + " = new StringBuilder();");
			replacement.add(declIndent + builder + ".append(" + assign.lhsText() + ");");
		}
		else {
			spanStart = decl.lineIdx();
			replacement.add(declIndent + "final var " + builder + " = new StringBuilder();");
			if (!"\"\"".equals(decl.initExpr()))
				replacement.add(declIndent + builder + ".append(" + decl.initExpr() + ");");
			for (var i = decl.lineIdx() + 1; i < loop.topLineIdx(); ++i)
				replacement.add(lines.get(i));
		}

		final var entryStates = new ArrayList<LexerState>();
		var lineState = entryStateAt(lines, loop.topLineIdx());
		for (var i = loop.topLineIdx(); i <= loop.endLineIdx(); ++i) {
			entryStates.add(lineState);
			lineState = JavaLineScanner.stateAfter(lines.get(i), lineState);
		}

		replacement.add(rewriteSafeMethodCalls(lines.get(loop.topLineIdx()), assign.lhsText(), builder, entryStates.getFirst()));
		for (var i = loop.topLineIdx() + 1; i < bodyLineIdx; ++i)
			replacement.add(rewriteSafeMethodCalls(lines.get(i), assign.lhsText(), builder, entryStates.get(i - loop.topLineIdx())));
		replacement.add(newBody);
		for (var i = bodyLineIdx + 1; i <= loop.endLineIdx(); ++i)
			replacement.add(rewriteSafeMethodCalls(lines.get(i), assign.lhsText(), builder, entryStates.get(i - loop.topLineIdx())));

		final String postLine;
		if (decl.isField())
			postLine = declIndent + assign.lhsText() + " = " + builder + ".toString();";
		else
			postLine = declIndent + "final var " + decl.varName() + " = " + builder + ".toString();";
		replacement.add(postLine);

		return new FixResult(spanStart, loop.endLineIdx(), replacement);
	}

	/**
	 * Parses {@code lines} to an AST and reuses {@link JitInefficiencyCheck}'s
	 * detector to recover the inefficiency category at the violation site. When
	 * the detector reports a category, returns a {@link SkipResult} carrying its
	 * skip reason; when there is no category (non-violation) or the input is
	 * unparseable (such as a bare fragment), returns {@code null}.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt categorizeSkip(@Nonnull List<String> lines, int lineIndex, int column) {
		final var category = FixerAst.withAst(lines, root -> new JitInefficiencyCheck().categorizeAt(root, lineIndex, column));
		if (category == null)
			return null;
		return new SkipResult(SkipMessages.get(category.skipReasonKey()));
	}

	/**
	 * Whether {@code line} assigns {@code chain} in either its bare form or its
	 * {@code this.}-qualified form. The full-fix pipeline's NoUnnecessaryThis fixer
	 * strips {@code this.} from array-element reads (so the receiver prefix derived
	 * from the LHS is bare, e.g. {@code matrix}) but keeps it on a direct
	 * instance-field assignment ({@code this.matrix = ...}, per the "this. on field
	 * assignment" convention). A bare-prefix scan alone would miss that mutation and
	 * hoist a read of the stale field, so both forms are checked.
	 */
	@CheckReturnValue
	private static boolean chainOrThisFormAssigned(@Nonnull String line, @Nonnull String chain, @Nonnull LexerState entryState) {
		return containsChainAssignment(line, chain, entryState)
				|| (!chain.startsWith("this.") && containsChainAssignment(line, "this." + chain, entryState));
	}

	/**
	 * Returns true if the given line contains an assignment whose LHS is
	 * exactly {@code chain}, i.e. {@code <chain> [ws]* (= or op=)} where the
	 * chain has identifier-style boundaries. Skips strings, char literals,
	 * line comments, and block comments. Used on body lines to detect
	 * intermediate-prefix mutations like `this.matrix = newMatrix();` packed
	 * onto the same line as `this.matrix.cells[i] += "x";`.
	 */
	@CheckReturnValue
	static boolean containsChainAssignment(@Nonnull String line, @Nonnull String chain, @Nonnull LexerState entryState) {
		if (chain.isEmpty())
			return false;
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < scan.length()) {
			final var ch = scan.charAt(i);
			if (i + chain.length() <= scan.length() && scan.regionMatches(i, chain, 0, chain.length())) {
				final var afterChain = i + chain.length();
				final var leftOk = i == 0
						|| (scan.charAt(i - 1) != '.' && !Character.isJavaIdentifierPart(scan.charAt(i - 1)));
				final var rightOk = afterChain >= scan.length()
						|| !Character.isJavaIdentifierPart(scan.charAt(afterChain));
				if (leftOk && rightOk) {
					var j = afterChain;
					while (j < scan.length() && Character.isWhitespace(scan.charAt(j)))
						++j;
					if (j < scan.length()) {
						final var op = scan.charAt(j);
						if (op == '=' && (j + 1 >= scan.length() || scan.charAt(j + 1) != '='))
							return true;
						if ((op == '+' || op == '-' || op == '*' || op == '/' || op == '%'
								|| op == '&' || op == '|' || op == '^')
								&& j + 1 < scan.length() && scan.charAt(j + 1) == '='
								&& (j + 2 >= scan.length() || scan.charAt(j + 2) != '='))
							return true;
					}
					i = afterChain;
					continue;
				}
			}
			if (Character.isJavaIdentifierStart(ch)) {
				while (i < scan.length() && Character.isJavaIdentifierPart(scan.charAt(i)))
					++i;
				continue;
			}
			++i;
		}
		return false;
	}

	/**
	 * Returns true if the given line contains the dotted receiver chain (e.g.
	 * `this.a.b`) as a substring with identifier-style boundaries. Skips
	 * strings, char literals, and comments. Used to detect chain-level
	 * mutations like `this.a.b = newArr;` for an `arr[i]` LHS where the
	 * array variable is qualified.
	 */
	@CheckReturnValue
	private static boolean containsReceiverChain(@Nonnull String line, @Nonnull String chain, @Nonnull LexerState entryState) {
		if (chain.isEmpty())
			return false;
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < scan.length()) {
			if (i + chain.length() <= scan.length() && scan.regionMatches(i, chain, 0, chain.length())) {
				final var leftOk = i == 0
						|| (!Character.isJavaIdentifierPart(scan.charAt(i - 1)) && scan.charAt(i - 1) != '.');
				final var afterEnd = i + chain.length();
				final var rightOk = afterEnd >= scan.length()
						|| !Character.isJavaIdentifierPart(scan.charAt(afterEnd));
				if (leftOk && rightOk)
					return true;
				i += chain.length();
				continue;
			}
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsTopLevelComma(@Nonnull String s) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < scan.length(); ++i) {
			final var ch = scan.charAt(i);
			// `<`/`>` are deliberately not a pair here: a relational `<` opens a group that
			// hides every later comma, and a relational `>`, a lambda `->` or a shift `>>`
			// closes it again, so the scan ends balanced with the separator undetected
			if (ch == '(' || ch == '[' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '}') {
				if (depth > 0)
					--depth;
			}
			else if (depth == 0 && ch == ',')
				return true;
		}
		// an unbalanced scan cannot be trusted, so report a separator rather than deny one
		return depth != 0;
	}

	@CheckReturnValue
	private static boolean containsTopLevelPlus(@Nonnull String s) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < scan.length(); ++i) {
			final var ch = scan.charAt(i);
			if (ch == '(' || ch == '[')
				++depth;
			else if (ch == ')' || ch == ']') {
				if (depth > 0)
					--depth;
			}
			else if (ch == '+' && depth == 0)
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code s} carries a {@code ;} outside every bracket group, i.e. a second
	 * statement is packed onto the assignment's line. A statement RHS can never hold
	 * one at top level, so a hit means the text past the assignment is not part of the
	 * expression and splicing it into {@code sb.append(...)} would emit unparseable
	 * Java and destroy that statement.
	 */
	@CheckReturnValue
	private static boolean containsTopLevelSemicolon(@Nonnull String s) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < scan.length(); ++i) {
			final var ch = scan.charAt(i);
			if (ch == '(' || ch == '[' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '}') {
				if (depth > 0)
					--depth;
			}
			else if (depth == 0 && ch == ';')
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static int countParensIgnoringLiterals(@Nonnull String s) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var count = 0;
		for (var i = 0; i < scan.length(); ++i) {
			if (scan.charAt(i) == '(')
				++count;
		}
		return count;
	}

	/**
	 * The 0-based index of the line carrying the {@code while} that closes the
	 * {@code do} at {@code doLineIdx}, or {@code -1} when the buffer does not
	 * parse or the parse reports no {@code do} keyword there.
	 */
	@CheckReturnValue
	private static int doWhileTerminatorLine(@Nonnull List<String> lines, int doLineIdx) {
		final var doLine = lines.get(doLineIdx);
		final var column = doLine.length() - doLine.stripLeading().length();
		final var shape = FixerAst.withAst(lines, root -> ControlFlowBracesCheck.shapeAt(root, doLineIdx, column));
		return shape == null ? -1 : shape.whileLine();
	}

	/**
	 * Whether {@code bodyText} is a do-while body the check would classify as tier 2,
	 * decided by {@link ControlFlowBracesCheck#shapeAt} on a synthetic do-while rather
	 * than re-derived from the text. Falls back to the single-top-level-paren
	 * approximation only when the synthetic buffer does not parse.
	 */
	@CheckReturnValue
	private static boolean emittedBodyIsTier2(@Nonnull String bodyText) {
		final var probe = List.of(
				"class T {",
				"\tvoid m(boolean c) {",
				"\t\tdo " + bodyText,
				"\t\twhile (c);",
				"\t}",
				"}"
		);
		final var shape = FixerAst.withAst(probe, root -> ControlFlowBracesCheck.shapeAt(root, 2, 2));
		if (shape == null)
			return countParensIgnoringLiterals(bodyText) == 1;
		return shape.tier() == 2;
	}

	/**
	 * The lexer state {@code lineIndex} begins in, folded from the top of the buffer.
	 *
	 * <p>Callers use this to refuse a splice at a line whose leading text is the content
	 * of a block comment or text block opened above it. Every line-level scanner here
	 * masks from a cold state, which reads that carried content as live code, so a guard
	 * is needed wherever the fixer would emit or rewrite text at such a line.
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
	 * Returns every dotted prefix of a dotted receiver chain, including the
	 * leftmost segment (so mutation of the chain root is also detectable). For
	 * {@code "this.matrix.cells"} returns
	 * {@code ["this", "this.matrix", "this.matrix.cells"]}. For {@code "obj.f"}
	 * returns {@code ["obj", "obj.f"]}. For an undotted receiver (e.g.
	 * {@code "arr"}) returns the empty list.
	 */
	@CheckReturnValue
	private static List<String> enumerateDottedPrefixes(@Nonnull String receiverPart) {
		if (!receiverPart.contains("."))
			return List.of();
		final var prefixes = new ArrayList<String>();
		var pos = receiverPart.indexOf('.');
		while (pos >= 0) {
			prefixes.add(receiverPart.substring(0, pos));
			pos = receiverPart.indexOf('.', pos + 1);
		}
		prefixes.add(receiverPart);
		return prefixes;
	}

	@CheckReturnValue
	@Nullable
	private static DeclInfo findDeclarationAbove(@Nonnull List<String> lines, int searchFromIdx, @Nonnull String varName) {
		// a line whose text continues a block comment or text block opened above is that
		// literal's content however much it reads like a declaration, and anchoring the
		// rewrite there splices live code into the comment and drops its opener, since
		// the replacement range starts at the matched line
		final var entryStates = new ArrayList<LexerState>();
		var state = LexerState.NONE;
		for (var i = 0; i <= searchFromIdx && i < lines.size(); ++i) {
			entryStates.add(state);
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		}
		for (var i = entryStates.size() - 1; i >= 0; --i) {
			if (entryStates.get(i).inMultilineLiteral())
				continue;
			// a line that leaves a literal open carries the rest of its text into it, so a
			// `;` that only looks like a terminator is comment content and the initializer
			// lifted from the line would splice a dangling opener into the emitted append
			if (JavaLineScanner.stateAfter(lines.get(i), entryStates.get(i)).inMultilineLiteral())
				continue;
			final var decl = findDeclarationLine(lines, i, varName);
			if (decl != null)
				return decl;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DeclInfo findDeclarationLine(@Nonnull List<String> lines, int searchFromIdx, @Nonnull String varName) {
		if (searchFromIdx < 0 || searchFromIdx >= lines.size())
			return null;
		final var stripped = lines.get(searchFromIdx).strip();
		if (!stripped.endsWith(";"))
			return null;
		final var withoutSemi = stripped.substring(0, stripped.length() - 1);
		final var eqIdx = findTopLevelAssignEquals(withoutSemi);
		if (eqIdx < 0)
			return null;
		final var lhs = withoutSemi.substring(0, eqIdx).strip();
		final var initExpr = withoutSemi.substring(eqIdx + 1).strip();
		if (initExpr.isEmpty())
			return null;
		// Reject multi-variable declarations (`String s = "", t = "x";`): a top-level
		// comma in the init region means more than one variable.
		if (containsTopLevelComma(initExpr))
			return null;
		if (containsTopLevelSemicolon(initExpr))
			return null;
		final var parts = lhs.split("\\s+");
		if (parts.length < 2)
			return null;
		if (!parts[parts.length - 1].equals(varName))
			return null;
		final var sbType = new StringBuilder();
		for (var i = 0; i < parts.length - 1; ++i) {
			if (!sbType.isEmpty())
				sbType.append(' ');
			sbType.append(parts[i]);
		}
		final var typeText = sbType.toString();
		final var withoutFinal = typeText.startsWith("final ") ? typeText.substring(6).strip() : typeText;
		if (!"String".equals(withoutFinal) && !"java.lang.String".equals(withoutFinal) && !"var".equals(withoutFinal))
			return null;
		final var isVar = "var".equals(withoutFinal);
		// For `var`, require the initializer to visibly contain a string literal.
		// Without this, a false-positive String type resolution upstream (e.g.
		// from method-overload mismatch or shadowed nested-class field) could
		// rewrite a non-String var into a StringBuilder and corrupt semantics.
		if (isVar && !initExpr.contains("\""))
			return null;
		return new DeclInfo(searchFromIdx, typeText, varName, initExpr, isVar, false);
	}

	@CheckReturnValue
	@Nullable
	private static LoopInfo findEnclosingLoop(@Nonnull List<String> lines, int bodyLineIdx) {
		if (bodyLineIdx <= 0 || bodyLineIdx >= lines.size())
			return null;
		// classify header lines off the masked view: a `for (...)`/`while (...)` sitting
		// inside a text block or block comment is not a loop
		final var masked = FixerAst.maskAll(lines);
		var currentIdx = bodyLineIdx;
		var currentIndent = LineLength.tabExpandedLength(LineText.extractIndent(lines.get(currentIdx)));
		while (true) {
			final var parent = findParentAtLowerIndent(lines, currentIdx, currentIndent);
			if (parent < 0)
				return null;
			final var stripped = masked.get(parent).strip();
			if (stripped.startsWith("for ") || stripped.startsWith("for(")
					|| stripped.startsWith("while ") || stripped.startsWith("while(")) {
				// bracedness is read from the first code character after the header's `)`,
				// not from the line ending in `{`, which a statement packed after the brace
				// (`while (c) { var e = it.next();`) hides. Getting it wrong calls the loop
				// unbraced, which ends the span at the body line and splices the write-back
				// inside the loop.
				final var headerEnd = findLoopHeaderEnd(lines, parent);
				if (headerEnd[0] < 0)
					return null;
				final var open = firstCodeCharFrom(masked, headerEnd[0], headerEnd[1] + 1);
				final var kind = stripped.startsWith("for") ? LoopKind.FOR : LoopKind.WHILE;
				if (open[0] >= 0 && masked.get(open[0]).charAt(open[1]) == '{') {
					final var endIdx = findMatchingClose(masked, open[0], open[1]);
					if (endIdx < 0)
						return null;
					return new LoopInfo(parent, endIdx, kind, true);
				}
				return new LoopInfo(parent, bodyLineIdx, kind, false);
			}
			if ("do".equals(stripped) || "do {".equals(stripped)) {
				final var whileIdx = doWhileTerminatorLine(lines, parent);
				if (whileIdx < 0)
					return null;
				final var whileStripped = lines.get(whileIdx).strip();
				// the condition has to close on this line: a multi-line condition puts the
				// `;` further down, and ending the span here would splice the write-back
				// into the middle of it
				if (!whileStripped.endsWith(";"))
					return null;
				// the terminator may be cuddled onto the body's closing brace
				// (`} while (c);`); anything else ahead of it is refused, because the
				// in-loop reference scan reads the line from a cold lexer state and would
				// misread comment or literal text sitting there as code
				final var afterBrace = whileStripped.startsWith("}")
						? whileStripped.substring(1).stripLeading()
						: whileStripped;
				if (!afterBrace.startsWith("while ") && !afterBrace.startsWith("while("))
					return null;
				return new LoopInfo(parent, whileIdx, LoopKind.DO_WHILE, "do {".equals(stripped));
			}
			if ((stripped.startsWith("if (") || stripped.startsWith("if("))
					&& !stripped.contains("else")) {
				currentIdx = parent;
				currentIndent = LineLength.tabExpandedLength(LineText.extractIndent(lines.get(parent)));
				continue;
			}
			return null;
		}
	}

	@CheckReturnValue
	private static int findExprEnd(@Nonnull String line, int from) {
		// Mask strings/chars/comments to spaces so the bespoke depth/operator scan
		// below only sees structural code. A `//` or unterminated `/*` masks to
		// end-of-line, so the scan runs off the end and the caller bails rather
		// than truncating the RHS at a comment or splicing an open comment.
		final var mask = JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE);
		// Paren/bracket nesting and generic-witness nesting are counted separately
		// so a comparison `>` inside a call argument can't be mistaken for closing a
		// `(`, and a witness `<...>` can't be mistaken for a comparison.
		var parenDepth = 0;
		var genericDepth = 0;
		var i = from;
		while (i < mask.length()) {
			final var ch = mask.charAt(i);
			// A `<` opens a generic witness when it directly follows `.` (`obj.<T>m()`)
			// or when one is already open (nested type args like `<List<String>>`); the
			// paired `>` closes it. Any other `<`/`>` falls through to comparison handling.
			if (ch == '<') {
				if (genericDepth > 0) {
					++genericDepth;
					++i;
					continue;
				}
				var prev = i - 1;
				while (prev >= from && mask.charAt(prev) == ' ')
					--prev;
				if (prev >= from && mask.charAt(prev) == '.') {
					++genericDepth;
					++i;
					continue;
				}
			}
			if (ch == '>' && genericDepth > 0) {
				--genericDepth;
				++i;
				continue;
			}
			if (ch == '(' || ch == '[' || ch == '{')
				++parenDepth;
			else if (ch == ')' || ch == ']' || ch == '}') {
				if (parenDepth == 0)
					return i;
				--parenDepth;
			}
			else if (parenDepth == 0 && genericDepth == 0) {
				if (ch == ',' || ch == ';')
					return i;
				// stop at operators that bind weaker than `+`
				if (ch == '?' || ch == ':')
					return i;
				if ((ch == '=' || ch == '!') && i + 1 < mask.length() && mask.charAt(i + 1) == '=')
					return i;
				if ((ch == '<' || ch == '>') && (i + 1 >= mask.length() || mask.charAt(i + 1) != ch))
					return i;
				if ((ch == '&' || ch == '|') && i + 1 < mask.length() && mask.charAt(i + 1) == ch)
					return i;
			}
			++i;
		}
		return i;
	}

	/**
	 * Returns the line index where the matching `)` of a for-loop header
	 * closes, or {@code -1} if the loop top isn't a for-loop or the header is
	 * never closed.
	 */
	@CheckReturnValue
	private static int findForHeaderEnd(@Nonnull List<String> lines, int loopTopIdx) {
		final var stripped = lines.get(loopTopIdx).stripLeading();
		if (!stripped.startsWith("for ") && !stripped.startsWith("for("))
			return -1;
		return findLoopHeaderEnd(lines, loopTopIdx)[0];
	}

	@CheckReturnValue
	private static int findIdentifierStart(@Nonnull String line, int from) {
		var pos = from - 1;
		while (pos >= 0) {
			final var ch = line.charAt(pos);
			if (Character.isJavaIdentifierPart(ch) || ch == '.')
				--pos;
			else
				break;
		}
		return pos + 1;
	}

	@CheckReturnValue
	private static int findLastAppendBefore(@Nonnull String line, int column) {
		return JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE)
				.lastIndexOf(".append(", Math.max(0, column));
	}

	/**
	 * Returns {@code {line, index}} of the {@code )} closing the loop header that opens
	 * on {@code loopTopIdx}, or {@code {-1, -1}} when it never closes.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] findLoopHeaderEnd(@Nonnull List<String> lines, int loopTopIdx) {
		final var topState = entryStateAt(lines, loopTopIdx);
		final var openParen = JavaLineScanner
				.stripCommentsAndStrings(lines.get(loopTopIdx), topState)
				.indexOf('(');
		if (openParen < 0)
			return new int[]{-1, -1};
		var depth = 0;
		var state = topState;
		for (var lineIdx = loopTopIdx; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			final var mask = JavaLineScanner.stripCommentsAndStrings(line, state);
			for (var i = lineIdx == loopTopIdx ? openParen : 0; i < mask.length(); ++i) {
				final var ch = mask.charAt(i);
				if (ch == '(')
					++depth;
				else if (ch == ')') {
					--depth;
					if (depth == 0)
						return new int[]{lineIdx, i};
				}
			}
			state = JavaLineScanner.stateAfter(line, state);
		}
		return new int[]{-1, -1};
	}

	/**
	 * Returns the line closing the block that opens after {@code fromIndex} on
	 * {@code fromLine}, or {@code -1} when it never closes. The scan starts past the
	 * header's {@code )} so a brace group inside the header (an inline array
	 * initializer, a block lambda) cannot be mistaken for the loop's own body.
	 */
	@CheckReturnValue
	private static int findMatchingClose(@Nonnull List<String> masked, int fromLine, int fromIndex) {
		// Brace depth over the masked source, not indentation: an over-indented `}` made
		// the indent scan step past the loop's real close and return an enclosing one,
		// which pulled every statement in between into the rewritten span.
		var depth = 0;
		for (var i = fromLine; i < masked.size(); ++i) {
			final var line = masked.get(i);
			for (var c = i == fromLine ? fromIndex : 0; c < line.length(); ++c) {
				if (line.charAt(c) == '{')
					++depth;
				else if (line.charAt(c) == '}') {
					--depth;
					if (depth == 0)
						return i;
					if (depth < 0)
						return -1;
				}
			}
		}
		return -1;
	}

	@CheckReturnValue
	private static int findParentAtLowerIndent(@Nonnull List<String> lines, int from, int childIndent) {
		for (var i = from - 1; i >= 0; --i) {
			final var line = lines.get(i);
			final var stripped = line.strip();
			if (stripped.isEmpty())
				continue;
			final var indent = LineLength.tabExpandedLength(LineText.extractIndent(line));
			if (indent < childIndent)
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findTopLevelAssignEquals(@Nonnull String s) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(s, JavaLineScanner.LexerState.NONE);
		var depth = 0;
		for (var i = 0; i < scan.length(); ++i) {
			final var ch = scan.charAt(i);
			// `<`/`>` are deliberately not a pair here, matching containsTopLevelComma
			if (ch == '(' || ch == '[' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '}') {
				if (depth > 0)
					--depth;
			}
			else if (depth == 0 && ch == '=') {
				final var prev = i > 0 ? scan.charAt(i - 1) : ' ';
				final var next = i + 1 < scan.length() ? scan.charAt(i + 1) : ' ';
				if (prev != '!' && prev != '<' && prev != '>' && prev != '=' && next != '=')
					return i;
			}
		}
		return -1;
	}

	/**
	 * Returns {@code {line, index}} of the first non-whitespace character at or after
	 * {@code fromIndex} on {@code fromLine} in {@code masked}, or {@code {-1, -1}}
	 * when the rest of the buffer holds nothing but whitespace.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] firstCodeCharFrom(@Nonnull List<String> masked, int fromLine, int fromIndex) {
		for (var i = Math.max(0, fromLine); i < masked.size(); ++i) {
			final var line = masked.get(i);
			for (var c = i == fromLine ? Math.max(0, fromIndex) : 0; c < line.length(); ++c) {
				if (!Character.isWhitespace(line.charAt(c)))
					return new int[]{i, c};
			}
		}
		return new int[]{-1, -1};
	}

	@CheckReturnValue
	private static int firstStringContainingPart(@Nonnull List<String> parts) {
		for (var i = 0; i < parts.size(); ++i) {
			if (parts.get(i).contains("\""))
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static String fixAppendConcat(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		// splice point: the rewritten `.append(...)` chain replaces this line
		if (entryStateAt(lines, lineIndex).inMultilineLiteral())
			return null;
		final var appendIdx = findLastAppendBefore(line, column);
		if (appendIdx < 0)
			return null;
		final var openParen = appendIdx + ".append".length();
		final var closeParen = JavaLineScanner.matchingCloseParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argsStart = openParen + 1;
		final var arg = line.substring(argsStart, closeParen);
		final var parts = splitTopLevelPlus(arg);
		if (parts == null || parts.size() < 2)
			return null;
		// At least one operand must contain a String literal: otherwise the chain
		// is pure numeric/non-String and splitting changes semantics (e.g. `1+2+x`
		// evaluating to `3 + x.toString()` versus `.append(1).append(2).append(x)`).
		final var firstStringPartIdx = firstStringContainingPart(parts);
		if (firstStringPartIdx < 0)
			return null;
		if (firstStringPartIdx > 0 && !leadingOperandsArePromotableToString(parts, firstStringPartIdx))
			return null;
		// Self-reference guard: the chained-append rewrite evaluates each operand
		// in sequence and writes intermediate state into the receiver. If any
		// operand textually references the receiver expression (e.g. `sb.length()`),
		// the rewritten chain would observe values from PARTIAL state rather than
		// the original receiver snapshot, silently changing semantics.
		final var receiverStart = findIdentifierStart(line, appendIdx);
		final var receiverText = line.substring(receiverStart, appendIdx);
		// nothing found even at the chain's root means we cannot analyse the receiver at
		// all, so refuse rather than split blind
		final var receiver = receiverText.isEmpty() ? receiverRoot(line, appendIdx) : receiverText;
		if (receiver.isEmpty() || partsReferenceReceiver(parts, receiver))
			return null;
		final var sb = new StringBuilder();
		sb.append(line, 0, appendIdx);
		for (var p : parts)
			sb.append(".append(").append(p.strip()).append(')');
		sb.append(line, closeParen + 1, line.length());
		return sb.toString();
	}

	@CheckReturnValue
	@Nullable
	private static String fixBoxedConstructor(@Nonnull String line, int column) {
		if (column >= line.length() || !line.startsWith("new ", column))
			return null;
		final var typeStart = column + "new ".length();
		var typeEnd = typeStart;
		while (typeEnd < line.length()) {
			final var ch = line.charAt(typeEnd);
			if (Character.isJavaIdentifierPart(ch)) {
				++typeEnd;
				continue;
			}
			if (ch == '.' && typeEnd + 1 < line.length()
					&& Character.isJavaIdentifierStart(line.charAt(typeEnd + 1))) {
				++typeEnd;
				continue;
			}
			break;
		}
		final var qualifiedTypeName = line.substring(typeStart, typeEnd);
		// Accept either the unqualified boxed-primitive name (e.g. `Integer`) or
		// its `java.lang.` FQN (e.g. `java.lang.Integer`). Other qualifiers are
		// rejected because `Foo.Integer.valueOf` doesn't generally exist.
		final String simpleTypeName;
		if (JitInefficiencyCheck.BOXED_PRIMITIVE_TYPES.contains(qualifiedTypeName))
			simpleTypeName = qualifiedTypeName;
		else if (qualifiedTypeName.startsWith("java.lang.")
				&& JitInefficiencyCheck.BOXED_PRIMITIVE_TYPES.contains(qualifiedTypeName.substring("java.lang.".length())))
			simpleTypeName = qualifiedTypeName.substring("java.lang.".length());
		else
			return null;
		if (typeEnd >= line.length() || line.charAt(typeEnd) != '(')
			return null;
		final var openParen = typeEnd;
		final var closeParen = JavaLineScanner.matchingCloseParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argText = line.substring(openParen + 1, closeParen).strip();
		if ("Boolean".equals(simpleTypeName)) {
			if ("true".equals(argText))
				return line.substring(0, column) + "Boolean.TRUE" + line.substring(closeParen + 1);
			if ("false".equals(argText))
				return line.substring(0, column) + "Boolean.FALSE" + line.substring(closeParen + 1);
		}
		// Emit the unqualified `T.valueOf(...)`; `java.lang.*` types are auto-imported
		// so the result is always valid even when the original used the FQN.
		return line.substring(0, column) + simpleTypeName + ".valueOf(" + argText + ")"
				+ line.substring(closeParen + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String fixEmptyStringConcat(@Nonnull List<String> lines, int lineIndex) {
		final var line = lines.get(lineIndex);
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		// splice point: this line is replaced by the `String.valueOf(...)` rewrite
		if (entryStateAt(lines, lineIndex).inMultilineLiteral())
			return null;
		final var leftIdx = JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE).indexOf("\"\" + ");
		if (leftIdx >= 0) {
			final var rhsStart = leftIdx + 5;
			final var rhsEnd = findExprEnd(line, rhsStart);
			// bail on multiline: if findExprEnd consumed all the way to end-of-line,
			// the expression continues on the next line and we can't safely capture it.
			if (rhsEnd < 0 || rhsEnd >= line.length())
				return null;
			// a `<` or `>` cannot terminate a String-concat operand in valid Java, so
			// stopping on one means a type-argument list (`new ArrayList<>()`) was read as
			// a comparison and the operand truncated mid-expression
			if (line.charAt(rhsEnd) == '<' || line.charAt(rhsEnd) == '>')
				return null;
			final var rhs = line.substring(rhsStart, rhsEnd);
			// reject if rhs has a top-level `+` (chain like `"" + a + b` would change semantics)
			if (containsTopLevelPlus(rhs))
				return null;
			return line.substring(0, leftIdx) + "String.valueOf(" + rhs + ")" + line.substring(rhsEnd);
		}
		final var rightIdx = JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE).indexOf(" + \"\"");
		if (rightIdx >= 0) {
			final var lhsStart = findIdentifierStart(line, rightIdx);
			if (lhsStart < 0 || lhsStart == rightIdx)
				return null;
			final var lhs = line.substring(lhsStart, rightIdx);
			// reject chain: scan everything before the LHS for a top-level `+` (symmetric
			// with the rhs branch). Any `+` to the left at depth 0 means we're in the
			// middle of a longer concat chain.
			if (containsTopLevelPlus(line.substring(0, lhsStart)))
				return null;
			final var afterEmpty = rightIdx + " + \"\"".length();
			return line.substring(0, lhsStart) + "String.valueOf(" + lhs + ")"
					+ line.substring(afterEmpty);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixNewString(@Nonnull String line, int column) {
		if (column >= line.length() || !line.startsWith("new String(", column))
			return null;
		final var openParen = column + "new String".length();
		final var closeParen = JavaLineScanner.matchingCloseParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argText = line.substring(openParen + 1, closeParen).strip();
		if (argText.isEmpty())
			return null;
		if (!isSimpleIdentifier(argText) && !isSingleStringLiteral(argText))
			return null;
		return line.substring(0, column) + argText + line.substring(closeParen + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String fixStringBuffer(@Nonnull String line, int column) {
		final var prefix = "new StringBuffer";
		if (!line.startsWith(prefix, column))
			return null;
		// guard against `new StringBufferInputStream` (legacy java.io class): the
		// next char must be `(`, `<`, or whitespace, not an identifier continuation.
		final var afterPrefix = column + prefix.length();
		if (afterPrefix < line.length()) {
			final var nextChar = line.charAt(afterPrefix);
			if (Character.isJavaIdentifierPart(nextChar))
				return null;
		}
		return line.substring(0, column) + "new StringBuilder" + line.substring(afterPrefix);
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixStringConcatInLoop(@Nonnull List<String> lines, int lineIndex) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var bodyLine = lines.get(lineIndex);
		// Bail on text blocks and block comments anywhere on the body line; the
		// line-text scanners don't track multi-line literal/comment state.
		if (bodyLine.contains("\"\"\"") || bodyLine.contains("/*"))
			return null;
		// splice point: this line becomes an `sb.append(...)` and the assignment it looks
		// like is hoisted out of the loop
		if (entryStateAt(lines, lineIndex).inMultilineLiteral())
			return null;
		// Tier-2 do-while: `do <stmt>; while (cond);` (body shares line with `do`).
		// Accept any whitespace separator after `do` (space, tab, etc.).
		final var bodyStripped = bodyLine.stripLeading();
		if (bodyStripped.length() > 2
				&& bodyStripped.charAt(0) == 'd' && bodyStripped.charAt(1) == 'o'
				&& Character.isWhitespace(bodyStripped.charAt(2)))
			return fixTier2DoWhile(lines, lineIndex);
		final var assign = parseConcatAssignment(bodyLine);
		if (assign == null)
			return null;
		// Any qualified LHS (`this.f`, `obj.f`, `this.a.b`, ...) or array-element
		// LHS (`arr[i]`, `this.arr[i]`) takes the "field-like" code path: we can't
		// replace a decl line, so we synthesize the SB construction directly above
		// the loop and reassign after.
		final var isFieldLhs = assign.lhsText().contains(".") || assign.lhsText().contains("[");
		final var loop = findEnclosingLoop(lines, lineIndex);
		if (loop == null)
			return null;
		// splice point: a field LHS emits the StringBuilder construction above this line.
		// A local LHS emits at the declaration instead, which findDeclarationAbove already
		// proves sits outside a literal
		if (entryStateAt(lines, loop.topLineIdx()).inMultilineLiteral())
			return null;
		// splice point: the write-back follows the loop's last line. Swallowed, a local's
		// `final var s = ...` goes missing and a field's assignment silently drops the
		// whole loop's result
		if (entryStateAt(lines, loop.endLineIdx() + 1).inMultilineLiteral())
			return null;
		// an unbraced loop's span ends on the body line the caller passed in, so only the
		// braced and do-while forms can put the assignment outside the rewritten range
		if ((loop.braced() || loop.kind() == LoopKind.DO_WHILE)
				&& (lineIndex <= loop.topLineIdx() || lineIndex >= loop.endLineIdx()))
			return null;
		// Bail on if-with-else around the assignment. Scan forward beyond loopEnd for
		// unbraced loops, since the body can syntactically extend through else clauses
		// at indents greater than the loop top's. Bail if a text block appears in
		// the scanned region: the `else` test reads the MASKED line, so `else` inside a
		// comment or literal is invisible to it while a live `/* note */ else` still
		// matches. Testing the raw line got both directions wrong: it let an else-branch
		// slip through and it aborted fixable loops on comment content.
		final var loopTopIndent = LineLength.tabExpandedLength(LineText.extractIndent(lines.get(loop.topLineIdx())));
		final var scanLimit = loop.braced() ? loop.endLineIdx() : lines.size();
		final var maskedScan = FixerAst.maskAll(lines);
		for (var i = lineIndex + 1; i < scanLimit; ++i) {
			final var raw = lines.get(i);
			if (raw.contains("\"\"\""))
				return null;
			final var stripped = maskedScan.get(i).stripLeading();
			if (stripped.isEmpty())
				continue;
			final var indent = LineLength.tabExpandedLength(LineText.extractIndent(raw));
			if (indent <= loopTopIndent)
				break;
			if (stripped.startsWith("else ") || stripped.equals("else") || stripped.startsWith("else{")
					|| stripped.startsWith("} else") || stripped.startsWith("}else"))
				return null;
		}
		final var isArrayLhs = assign.lhsText().contains("[");
		if (isArrayLhs && !validateArrayLhsLoopStable(lines, loop, lineIndex, assign.lhsText()))
			return null;
		final DeclInfo decl;
		if (isFieldLhs) {
			// splice point: the construction goes above the loop header, so the header has to
			// be a block statement and not some controller's unbraced body
			if (!isBlockStatement(lines, loop.topLineIdx()))
				return null;
			decl = new DeclInfo(loop.topLineIdx(), "String", assign.varName(), "", false, true);
		}
		else {
			final var found = findDeclarationAbove(lines, loop.topLineIdx() - 1, assign.varName());
			if (found == null)
				return null;
			if (!isInSameScope(lines, found.lineIdx(), loop.topLineIdx()))
				return null;
			for (var i = found.lineIdx() + 1; i < loop.topLineIdx(); ++i) {
				final var gapLine = lines.get(i);
				if (gapLine.contains("\"\"\"") || gapLine.contains("/*"))
					return null;
				if (mentionsIdentifier(gapLine, assign.varName()))
					return null;
			}
			if (mutatedAfterLoop(lines, loop.endLineIdx(), assign.varName()))
				return null;
			decl = found;
		}
		if (!verifyNoOtherVarUseInLoop(lines, loop, lineIndex, assign.lhsText()))
			return null;
		return buildStringConcatReplacement(lines, decl, loop, assign, lineIndex);
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixTier2DoWhile(@Nonnull List<String> lines, int lineIndex) {
		final var doLine = lines.get(lineIndex);
		if (doLine.contains("\"\"\"") || doLine.contains("/*"))
			return null;
		final var doStripped = doLine.stripLeading();
		final var indent = doLine.substring(0, doLine.length() - doStripped.length());
		if (doStripped.length() <= 2 || !Character.isWhitespace(doStripped.charAt(2)))
			return null;
		var bodySkip = 3;
		while (bodySkip < doStripped.length() && Character.isWhitespace(doStripped.charAt(bodySkip)))
			++bodySkip;
		final var bodyText = doStripped.substring(bodySkip);
		final var virtualBody = indent + "\t" + bodyText;
		final var assign = parseConcatAssignment(virtualBody);
		if (assign == null)
			return null;
		final var isFieldLhs = assign.lhsText().contains(".") || assign.lhsText().contains("[");
		if (lineIndex + 1 >= lines.size())
			return null;
		final var whileLine = lines.get(lineIndex + 1);
		if (whileLine.contains("\"\"\"") || whileLine.contains("/*"))
			return null;
		final var whileStripped = whileLine.strip();
		if (!whileStripped.startsWith("while ") && !whileStripped.startsWith("while("))
			return null;
		if (!whileStripped.endsWith(";"))
			return null;
		if (LineLength.tabExpandedLength(LineText.extractIndent(whileLine)) != LineLength.tabExpandedLength(indent))
			return null;
		if (!referencesAreAllSafeMethodCalls(whileLine, assign.lhsText()))
			return null;
		final var loop = new LoopInfo(lineIndex, lineIndex + 1, LoopKind.DO_WHILE, false);
		if (assign.lhsText().contains("[")
				&& !validateArrayLhsLoopStable(lines, loop, lineIndex, assign.lhsText()))
			return null;
		final DeclInfo decl;
		if (isFieldLhs) {
			if (!isBlockStatement(lines, lineIndex))
				return null;
			decl = new DeclInfo(lineIndex, "String", assign.varName(), "", false, true);
		}
		else {
			final var found = findDeclarationAbove(lines, lineIndex - 1, assign.varName());
			if (found == null)
				return null;
			if (!isInSameScope(lines, found.lineIdx(), lineIndex))
				return null;
			for (var i = found.lineIdx() + 1; i < lineIndex; ++i) {
				final var gapLine = lines.get(i);
				if (gapLine.contains("\"\"\"") || gapLine.contains("/*"))
					return null;
				if (mentionsIdentifier(gapLine, assign.varName()))
					return null;
			}
			if (mutatedAfterLoop(lines, loop.endLineIdx(), assign.varName()))
				return null;
			decl = found;
		}
		final var builder = builderName(lines);
		final var newBody = buildAppendBody(indent + "\t", assign, builder);
		final var declIndent = decl.isField() ? indent : LineText.extractIndent(lines.get(decl.lineIdx()));
		final var replacement = new ArrayList<String>();
		final int spanStart;
		if (decl.isField()) {
			spanStart = lineIndex;
			replacement.add(declIndent + "final var " + builder + " = new StringBuilder();");
			replacement.add(declIndent + builder + ".append(" + assign.lhsText() + ");");
		}
		else {
			spanStart = decl.lineIdx();
			replacement.add(declIndent + "final var " + builder + " = new StringBuilder();");
			if (!"\"\"".equals(decl.initExpr()))
				replacement.add(declIndent + builder + ".append(" + decl.initExpr() + ");");
			for (var i = decl.lineIdx() + 1; i < lineIndex; ++i)
				replacement.add(lines.get(i));
		}
		// counting parens instead disagreed with the check whenever the single call carried
		// an argument that itself had parens (`sb.append(list.get(i))`), so the fixer emitted
		// tier 3 where the check wants tier 2 and left behind a violation this pass never
		// revisits
		final var bodyStripped = newBody.stripLeading();
		if (emittedBodyIsTier2(bodyStripped))
			replacement.add(indent + "do " + bodyStripped);
		else {
			replacement.add(indent + "do");
			replacement.add(newBody);
		}
		replacement.add(rewriteSafeMethodCalls(whileLine, assign.lhsText(), builder));
		final String postLine;
		if (decl.isField())
			postLine = declIndent + assign.lhsText() + " = " + builder + ".toString();";
		else
			postLine = declIndent + "final var " + decl.varName() + " = " + builder + ".toString();";
		replacement.add(postLine);
		return new FixResult(spanStart, loop.endLineIdx(), replacement);
	}

	@CheckReturnValue
	@Nullable
	private static String fixToArraySized(@Nonnull String line, int column) {
		// the violation column points at the LPAREN of the outer `.toArray(...)` call,
		// so `.toArray(new ` is exactly 8 chars to the left. lastIndexOf is anchored
		// and cannot drift forward into following text.
		final var prefix = ".toArray(new ";
		final var idx = line.lastIndexOf(prefix, Math.max(0, column));
		if (idx < 0)
			return null;
		final var typeStart = idx + prefix.length();
		final var bracketIdx = line.indexOf('[', typeStart);
		if (bracketIdx < 0)
			return null;
		final var typeText = line.substring(typeStart, bracketIdx);
		if (typeText.contains("@"))
			return null;
		// require a single-dim closing ']' before ')'. Multi-dim has another '[' before ')'.
		final var sizeStart = bracketIdx + 1;
		final var sizeEnd = line.indexOf(']', sizeStart);
		if (sizeEnd < 0)
			return null;
		final var afterClose = sizeEnd + 1;
		if (afterClose >= line.length())
			return null;
		if (line.charAt(afterClose) != ')')
			return null;
		// refuse unless the size expression is known-pure (whitelist), otherwise dropping
		// it would silently lose side effects.
		final var sizeText = line.substring(sizeStart, sizeEnd);
		if (!sizeExpressionIsKnownPure(sizeText))
			return null;
		return line.substring(0, bracketIdx + 1) + "0" + line.substring(sizeEnd);
	}

	/**
	 * Whether the statement starting at {@code lineIdx} sits directly inside a block, so
	 * statements may be spliced in above it. A loop that is the unbraced body of an
	 * {@code if}/{@code else}/outer loop, or that follows a statement label, cannot take a
	 * hoisted declaration: the declaration would become the controller's body and the loop
	 * plus the write-back would escape the guard entirely.
	 */
	@CheckReturnValue
	private static boolean isBlockStatement(@Nonnull List<String> lines, int lineIdx) {
		final var masked = FixerAst.maskAll(lines);
		for (var i = lineIdx - 1; i >= 0; --i) {
			final var stripped = masked.get(i).strip();
			if (stripped.isEmpty())
				continue;
			final var last = stripped.charAt(stripped.length() - 1);
			// A trailing `:` is refused either way. A statement label binds the single
			// statement after it, so a spliced declaration would take the label and free the
			// loop from it; a bare `case`/`default` label does accept the extra statements,
			// but the spliced `final var sb` then needs braces to limit its scope, which is
			// a second fixer's edit rather than this one's output.
			return last == '{' || last == '}' || last == ';';
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isInSameScope(@Nonnull List<String> lines, int declLineIdx, int targetLineIdx) {
		var depth = 0;
		var state = entryStateAt(lines, declLineIdx + 1);
		// Walk lines strictly between decl and target; the target line itself is the
		// loop top (or do-line) whose braces belong to the loop body, not the
		// enclosing scope. Including it would falsely raise depth.
		for (var lineIdx = declLineIdx + 1; lineIdx < targetLineIdx; ++lineIdx) {
			final var line = lines.get(lineIdx);
			if (line.contains("\"\"\""))
				return false;
			final var masked = JavaLineScanner.stripCommentsAndStrings(line, state);
			for (var i = 0; i < masked.length(); ++i) {
				final var ch = masked.charAt(i);
				if (ch == '{')
					++depth;
				else if (ch == '}') {
					--depth;
					if (depth < 0)
						return false;
				}
			}
			state = JavaLineScanner.stateAfter(line, state);
		}
		return depth == 0;
	}

	@CheckReturnValue
	private static boolean isSimpleIdentifier(@Nonnull String s) {
		if (s.isEmpty())
			return false;
		if (!Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (var i = 1; i < s.length(); ++i) {
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isSingleStringLiteral(@Nonnull String s) {
		if (s.length() < 2 || s.charAt(0) != '"' || s.charAt(s.length() - 1) != '"')
			return false;
		var i = 1;
		while (i < s.length() - 1) {
			final var ch = s.charAt(i);
			if (ch == '\\' && i + 1 < s.length()) {
				i += 2;
				continue;
			}
			if (ch == '"')
				return false;
			++i;
		}
		return true;
	}

	/**
	 * Returns true if every operand in {@code parts} before {@code firstStringIdx}
	 * is safe to append individually without changing concat semantics. Pure
	 * numeric and char literals are rejected because they would otherwise be
	 * summed arithmetically before promotion to String (e.g. {@code 1 + 2 + "x"}
	 * evaluates to {@code "3x"} but chained appends produce {@code "12x"}).
	 * Identifier-style operands and method calls are accepted: their type is
	 * unknowable from text but Java's {@code +} promotes the operand to String
	 * regardless of its numeric/reference type when paired with a String, and
	 * {@code StringBuilder.append} has matching overloads for every primitive
	 * and Object type.
	 */
	@CheckReturnValue
	private static boolean leadingOperandsArePromotableToString(@Nonnull List<String> parts, int firstStringIdx) {
		for (var i = 0; i < firstStringIdx; ++i) {
			final var part = parts.get(i).strip();
			if (part.isEmpty())
				return false;
			final var firstCh = part.charAt(0);
			// numeric literal (decimal/hex/binary/float) starts with a digit or `.`
			if (Character.isDigit(firstCh))
				return false;
			if (firstCh == '.' && part.length() > 1 && Character.isDigit(part.charAt(1)))
				return false;
			// char literal, would also fail with reordering since it implicit-promotes to int
			if (firstCh == '\'')
				return false;
			// leading sign followed by a numeric literal (e.g. `-1`, `+0xFF`)
			if ((firstCh == '-' || firstCh == '+') && part.length() > 1) {
				final var next = part.charAt(1);
				if (Character.isDigit(next))
					return false;
				if (next == '.' && part.length() > 2 && Character.isDigit(part.charAt(2)))
					return false;
			}
		}
		return true;
	}

	/**
	 * Returns true if the given line contains a reference to {@code receiverText}
	 * that is NOT followed by exactly {@code bracketPortion} (the `[idx]` /
	 * `[idx][jdx]` suffix from the LHS expression). References that match exactly
	 * `<receiver><bracketPortion>` are allowed (they're either the LHS itself or a
	 * safe-method-call receiver, separately validated by
	 * {@link #verifyNoOtherVarUseInLoop}). Skips strings, char literals, and
	 * comments.
	 */
	@CheckReturnValue
	private static boolean lineHasUnsafeArrayReference(
			@Nonnull String line, @Nonnull String receiverText, @Nonnull String bracketPortion, @Nonnull LexerState entryState
	) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < scan.length()) {
			final var ch = scan.charAt(i);
			if (i + receiverText.length() <= scan.length()
					&& scan.regionMatches(i, receiverText, 0, receiverText.length())) {
				final var afterRecv = i + receiverText.length();
				final var leftOk = i == 0
						|| (scan.charAt(i - 1) != '.' && !Character.isJavaIdentifierPart(scan.charAt(i - 1)));
				final var rightOk = afterRecv >= scan.length()
						|| !Character.isJavaIdentifierPart(scan.charAt(afterRecv));
				if (leftOk && rightOk) {
					if (afterRecv + bracketPortion.length() <= scan.length()
							&& scan.regionMatches(afterRecv, bracketPortion, 0, bracketPortion.length())) {
						i = afterRecv + bracketPortion.length();
						continue;
					}
					return true;
				}
			}
			if (Character.isJavaIdentifierStart(ch)) {
				while (i < scan.length() && Character.isJavaIdentifierPart(scan.charAt(i)))
					++i;
				continue;
			}
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean mentionsIdentifier(@Nonnull String line, @Nonnull String name) {
		return mentionsIdentifier(line, name, LexerState.NONE);
	}

	@CheckReturnValue
	private static boolean mentionsIdentifier(@Nonnull String line, @Nonnull String name, @Nonnull LexerState entryState) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < scan.length()) {
			if (Character.isJavaIdentifierStart(scan.charAt(i))) {
				final var start = i;
				while (i < scan.length() && Character.isJavaIdentifierPart(scan.charAt(i)))
					++i;
				if (scan.substring(start, i).equals(name))
					return true;
				continue;
			}
			++i;
		}
		return false;
	}

	/**
	 * Returns true if {@code name} is written again after the loop ends, anywhere
	 * in the scope that encloses the loop. The rewrite replaces the variable's
	 * declaration with {@code final var <name> = sb.toString();}, so a later write
	 * would target a final variable and no longer compile.
	 */
	@CheckReturnValue
	private static boolean mutatedAfterLoop(@Nonnull List<String> lines, int loopEndIdx, @Nonnull String name) {
		var depth = 0;
		var state = entryStateAt(lines, loopEndIdx + 1);
		for (var lineIdx = loopEndIdx + 1; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			// the scanner cannot reason across `"""`, and a text block below the loop
			// may hide a write; refuse rather than guess
			if (line.contains("\"\"\""))
				return true;
			if (mutatesIdentifier(line, name, state))
				return true;
			final var masked = JavaLineScanner.stripCommentsAndStrings(line, state);
			for (var i = 0; i < masked.length(); ++i) {
				final var ch = masked.charAt(i);
				if (ch == '{')
					++depth;
				else if (ch == '}') {
					--depth;
					// the enclosing scope closed, so the declaration is out of scope
					// from here on and any later write names a different variable
					if (depth < 0)
						return false;
				}
			}
			state = JavaLineScanner.stateAfter(line, state);
		}
		return false;
	}

	/**
	 * Returns true if the given identifier is mutated anywhere on the line
	 * (assignment with `=`, compound assignment `<op>=`, or pre/post inc/dec).
	 * Skips strings, char literals, and comments.
	 */
	@CheckReturnValue
	static boolean mutatesIdentifier(@Nonnull String line, @Nonnull String name, @Nonnull LexerState entryState) {
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < scan.length()) {
			final var ch = scan.charAt(i);
			if ((ch == '+' || ch == '-') && i + 1 < scan.length() && scan.charAt(i + 1) == ch) {
				final var afterOp = i + 2;
				if (afterOp + name.length() <= scan.length()
						&& scan.regionMatches(afterOp, name, 0, name.length())
						&& (afterOp + name.length() >= scan.length()
						|| !Character.isJavaIdentifierPart(scan.charAt(afterOp + name.length()))))
					return true;
			}
			if (Character.isJavaIdentifierStart(ch) || ch == '_') {
				// `\0` is a safe stand-in for "nothing precedes this" only here, because
				// prev is compared against `.` alone and never fed to isJavaIdentifierPart
				final var prev = i == 0 ? '\0' : scan.charAt(i - 1);
				final var start = i;
				while (i < scan.length() && Character.isJavaIdentifierPart(scan.charAt(i)))
					++i;
				if (!scan.substring(start, i).equals(name))
					continue;
				// Skip member access: `obj.name = ...` is a write to obj.name, not name.
				if (prev == '.')
					continue;
				if (i + 1 < scan.length()) {
					final var c1 = scan.charAt(i);
					final var c2 = scan.charAt(i + 1);
					if ((c1 == '+' && c2 == '+') || (c1 == '-' && c2 == '-'))
						return true;
				}
				var j = i;
				while (j < scan.length() && Character.isWhitespace(scan.charAt(j)))
					++j;
				if (j < scan.length()) {
					final var op = scan.charAt(j);
					if (op == '=' && (j + 1 >= scan.length() || scan.charAt(j + 1) != '='))
						return true;
					if ((op == '+' || op == '-' || op == '*' || op == '/' || op == '%'
							|| op == '&' || op == '|' || op == '^')
							&& j + 1 < scan.length() && scan.charAt(j + 1) == '='
							&& (j + 2 >= scan.length() || scan.charAt(j + 2) != '='))
						return true;
					if ((op == '<' || op == '>') && j + 1 < scan.length() && scan.charAt(j + 1) == op) {
						var k = j + 2;
						if (op == '>' && k < scan.length() && scan.charAt(k) == '>')
							++k;
						if (k < scan.length() && scan.charAt(k) == '='
								&& (k + 1 >= scan.length() || scan.charAt(k + 1) != '='))
							return true;
					}
				}
				continue;
			}
			++i;
		}
		return false;
	}

	@CheckReturnValue
	@Nullable
	private static AssignInfo parseConcatAssignment(@Nonnull String line) {
		var trimmed = line.stripTrailing();
		if (!trimmed.endsWith(";"))
			return null;
		trimmed = trimmed.substring(0, trimmed.length() - 1);
		var i = 0;
		while (i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i)))
			++i;
		final var indent = trimmed.substring(0, i);
		final var lhsStart = i;
		while (i < trimmed.length()) {
			final var ch = trimmed.charAt(i);
			if (Character.isJavaIdentifierPart(ch) || ch == '.')
				++i;
			else
				break;
		}
		final var receiverText = trimmed.substring(lhsStart, i);
		if (receiverText.isEmpty() || receiverText.endsWith(".") || receiverText.startsWith(".")
				|| receiverText.contains("..")
				|| !Character.isJavaIdentifierStart(receiverText.charAt(0)))
			return null;
		// Optional `[index]` suffix(es) for array element LHS. Supports chained
		// indexing like `arr[i][j]`: consume bracketed regions until the
		// receiver+suffix sequence ends.
		final String lhsText;
		if (i < trimmed.length() && trimmed.charAt(i) == '[') {
			var bracketEnd = i;
			while (bracketEnd < trimmed.length() && trimmed.charAt(bracketEnd) == '[') {
				final var closeIdx = JavaLineScanner.matchingClose(trimmed, bracketEnd);
				if (closeIdx < 0)
					return null;
				bracketEnd = closeIdx + 1;
			}
			lhsText = trimmed.substring(lhsStart, bracketEnd);
			i = bracketEnd;
		}
		else
			lhsText = receiverText;
		while (i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i)))
			++i;
		if (i >= trimmed.length())
			return null;
		final boolean isPlusAssign;
		if (i + 1 < trimmed.length() && trimmed.charAt(i) == '+' && trimmed.charAt(i + 1) == '=') {
			isPlusAssign = true;
			i += 2;
		}
		else if (trimmed.charAt(i) == '=' && (i + 1 >= trimmed.length() || trimmed.charAt(i + 1) != '=')) {
			isPlusAssign = false;
			++i;
		}
		else
			return null;
		while (i < trimmed.length() && Character.isWhitespace(trimmed.charAt(i)))
			++i;
		final var rhs = trimmed.substring(i);
		if (rhs.isEmpty() || containsTopLevelSemicolon(rhs))
			return null;
		final var bracketIdx = lhsText.indexOf('[');
		final var receiverPart = bracketIdx >= 0 ? lhsText.substring(0, bracketIdx) : lhsText;
		final var lastDot = receiverPart.lastIndexOf('.');
		final var varName = lastDot >= 0 ? receiverPart.substring(lastDot + 1) : receiverPart;
		// For qualified LHS (`this.f`, `obj.f`, `this.a.b`, etc.) the receiver
		// must be a simple dotted ident chain (no method calls, casts, etc.).
		// `containsTopLevelComma` would reject parens; here we only need to
		// reject anything other than identifier characters and dots.
		if (lastDot >= 0) {
			for (var k = 0; k < receiverPart.length(); ++k) {
				final var ch = receiverPart.charAt(k);
				if (!Character.isJavaIdentifierPart(ch) && ch != '.')
					return null;
			}
		}
		if (isPlusAssign) {
			if (!referencesAreAllSafeMethodCalls(rhs, lhsText))
				return null;
			return new AssignInfo(indent, lhsText, varName, List.of(), List.of(rhs));
		}
		final var parts = splitTopLevelPlus(rhs);
		if (parts == null || parts.size() < 2)
			return null;
		final var prepends = new ArrayList<String>();
		final var appends = new ArrayList<String>();
		var foundLhs = false;
		var foundLhsCount = 0;
		for (var part : parts) {
			final var stripped = part.strip();
			if (stripped.equals(lhsText)) {
				foundLhs = true;
				++foundLhsCount;
			}
			else if (foundLhs)
				appends.add(stripped);
			else
				prepends.add(stripped);
		}
		if (!foundLhs || foundLhsCount > 1)
			return null;
		if (prepends.isEmpty() && appends.isEmpty())
			return null;
		for (var op : prepends) {
			if (!referencesAreAllSafeMethodCalls(op, lhsText))
				return null;
		}
		for (var op : appends) {
			if (!referencesAreAllSafeMethodCalls(op, lhsText))
				return null;
		}
		// every emitted op but the first runs against a partially built builder, so a
		// whitelisted read (`length()`, `charAt(...)`) in a later one observes the
		// accumulator mid-rewrite rather than its pre-statement value: `s = s + "-" +
		// s.length()` would become `sb.append("-").append(sb.length())`, which counts the
		// `-` it just added. Prepends are all evaluated before the `insert`, so only
		// appends past the first are exposed, unless a prepend exists, which runs first
		// and exposes every append.
		final var laterAppends = prepends.isEmpty() ? appends.subList(1, appends.size()) : appends;
		if (partsReferenceReceiver(laterAppends, lhsText))
			return null;
		return new AssignInfo(indent, lhsText, varName, prepends, appends);
	}

	/**
	 * Returns true if any operand in {@code parts} textually references the
	 * {@code receiverText} expression with identifier-style boundaries (not a
	 * substring inside a longer identifier). Skips string and char literals so
	 * a literal mentioning the receiver name is not a real reference.
	 */
	@CheckReturnValue
	private static boolean partsReferenceReceiver(@Nonnull List<String> parts, @Nonnull String receiverText) {
		for (var part : parts) {
			var i = 0;
			while (i < part.length()) {
				final var ch = part.charAt(i);
				if (ch == '"' || ch == '\'') {
					++i;
					while (i < part.length()) {
						final var c = part.charAt(i);
						if (c == '\\' && i + 1 < part.length()) {
							i += 2;
							continue;
						}
						if (c == ch) {
							++i;
							break;
						}
						++i;
					}
					continue;
				}
				if (i + receiverText.length() <= part.length()
						&& part.regionMatches(i, receiverText, 0, receiverText.length())) {
					final var afterRecv = i + receiverText.length();
					final var leftOk = i == 0
							|| (part.charAt(i - 1) != '.' && !Character.isJavaIdentifierPart(part.charAt(i - 1)));
					final var rightOk = afterRecv >= part.length()
							|| !Character.isJavaIdentifierPart(part.charAt(afterRecv));
					if (leftOk && rightOk)
						return true;
				}
				++i;
			}
		}
		return false;
	}

	/**
	 * The identifier the receiver expression ending at {@code from} is rooted in, or
	 * {@code ""} when there is none. {@link #findIdentifierStart} stops at the first
	 * non-name character, so for a chained receiver (`sb.append(x).`) it yields nothing
	 * and the self-reference guard it feeds would silently not run. Walking back over
	 * balanced call and index groups reaches the root (`sb`), which is what an operand
	 * has to avoid referencing.
	 */
	@CheckReturnValue
	@Nonnull
	private static String receiverRoot(@Nonnull String line, int from) {
		var pos = from;
		while (pos > 0) {
			final var ch = line.charAt(pos - 1);
			if (Character.isJavaIdentifierPart(ch) || ch == '.') {
				--pos;
				continue;
			}
			if (ch != ')' && ch != ']')
				break;
			final var open = ch == ')' ? '(' : '[';
			var depth = 0;
			var i = pos - 1;
			while (i >= 0) {
				final var c = line.charAt(i);
				if (c == ch)
					++depth;
				else if (c == open && --depth == 0)
					break;
				--i;
			}
			if (i < 0)
				return "";
			pos = i;
		}
		final var end = LineText.identEnd(line, pos);
		return end > pos ? line.substring(pos, end) : "";
	}

	@CheckReturnValue
	private static boolean referencesAreAllSafeMethodCalls(@Nonnull String line, @Nonnull String lhsText) {
		return referencesAreAllSafeMethodCalls(line, lhsText, LexerState.NONE);
	}

	// masks through the same scanner `rewriteSafeMethodCalls` uses, from the same entry
	// state: a validator that lexed from cold read an apostrophe in a carried block
	// comment as an open char literal, skipped the rest of the line, and reported "all
	// safe" for a line the rewriter then went on to rewrite
	@CheckReturnValue
	private static boolean referencesAreAllSafeMethodCalls(
			@Nonnull String line,
			@Nonnull String lhsText,
			@Nonnull LexerState entryState
	) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		var i = 0;
		while (i < masked.length()) {
			if (!masked.startsWith(lhsText, i)) {
				++i;
				continue;
			}
			if (i > 0) {
				final var prev = masked.charAt(i - 1);
				if (prev == '.' || Character.isJavaIdentifierPart(prev)) {
					++i;
					continue;
				}
			}
			final var afterLhs = i + lhsText.length();
			if (afterLhs < masked.length() && Character.isJavaIdentifierPart(masked.charAt(afterLhs))) {
				++i;
				continue;
			}
			if (afterLhs >= masked.length() || masked.charAt(afterLhs) != '.')
				return false;
			final var methodStart = afterLhs + 1;
			var methodEnd = methodStart;
			while (methodEnd < masked.length() && Character.isJavaIdentifierPart(masked.charAt(methodEnd)))
				++methodEnd;
			if (!SAFE_STRING_METHODS_ON_BUILDER.contains(masked.substring(methodStart, methodEnd)))
				return false;
			if (methodEnd >= masked.length() || masked.charAt(methodEnd) != '(')
				return false;
			i = methodEnd;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean referencesChainOrThisForm(@Nonnull String line, @Nonnull String chain, @Nonnull LexerState entryState) {
		return containsReceiverChain(line, chain, entryState)
				|| (!chain.startsWith("this.") && containsReceiverChain(line, "this." + chain, entryState));
	}

	@CheckReturnValue
	@Nonnull
	private static String rewriteSafeMethodCalls(@Nonnull String line, @Nonnull String lhsText, @Nonnull String builder) {
		return rewriteSafeMethodCalls(line, lhsText, builder, LexerState.NONE);
	}

	/**
	 * Rewrites whole-token occurrences of {@code lhsText} on {@code line} to the
	 * StringBuilder receiver {@code builder}, leaving string/char/comment content
	 * untouched. {@code entryState} threads the lexer state from prior lines, so
	 * a {@code lhsText} appearing as text inside a block comment opened on an
	 * earlier line (or inside a string/comment on this line) is not rewritten.
	 * Matches and token boundaries are tested on the masked line (code only);
	 * output chars are spliced from the original.
	 */
	@CheckReturnValue
	@Nonnull
	private static String rewriteSafeMethodCalls(
			@Nonnull String line,
			@Nonnull String lhsText,
			@Nonnull String builder,
			@Nonnull LexerState entryState
	) {
		if (lhsText.isEmpty())
			return line;
		final var mask = JavaLineScanner.stripCommentsAndStrings(line, entryState);
		final var out = new StringBuilder();
		var i = 0;
		while (i < line.length()) {
			if (mask.startsWith(lhsText, i)) {
				final var afterLhs = i + lhsText.length();
				// code points, not chars: a supplementary identifier character is a
				// surrogate pair and neither half is an identifier part on its own, so a
				// char-wise test reports a word boundary in the middle of an identifier
				final var leftBoundaryOk = i == 0
						|| (mask.codePointBefore(i) != '.' && !Character.isJavaIdentifierPart(mask.codePointBefore(i)));
				final var rightBoundaryOk = afterLhs >= mask.length()
						|| !Character.isJavaIdentifierPart(mask.codePointAt(afterLhs));
				if (leftBoundaryOk && rightBoundaryOk) {
					out.append(builder);
					i = afterLhs;
					continue;
				}
			}
			out.append(line.charAt(i));
			++i;
		}
		return out.toString();
	}

	@CheckReturnValue
	private static boolean sizeExpressionIsKnownPure(@Nonnull String size) {
		// Whitelist of known-pure size shapes (values can be safely dropped):
		// - integer literal (decimal, hex, binary, optionally underscore-separated)
		// - simple identifier or dotted-name (e.g. `n`, `THIS.field`)
		// - dotted access ending in `.size()` or `.length()` (idempotent on collections/arrays/strings)
		final var trimmed = size.strip();
		if (trimmed.isEmpty())
			return false;
		if (Character.isDigit(trimmed.charAt(0))) {
			for (var i = 0; i < trimmed.length(); ++i) {
				final var ch = trimmed.charAt(i);
				if (!Character.isLetterOrDigit(ch) && ch != '_')
					return false;
			}
			return true;
		}
		final var sizeSuffix = ".size()";
		final var lengthSuffix = ".length()";
		var prefix = trimmed;
		if (trimmed.endsWith(sizeSuffix))
			prefix = trimmed.substring(0, trimmed.length() - sizeSuffix.length());
		else if (trimmed.endsWith(lengthSuffix))
			prefix = trimmed.substring(0, trimmed.length() - lengthSuffix.length());
		if (prefix.isEmpty())
			return false;
		for (var i = 0; i < prefix.length(); ++i) {
			final var ch = prefix.charAt(i);
			if (!Character.isJavaIdentifierPart(ch) && ch != '.')
				return false;
		}
		return Character.isJavaIdentifierStart(prefix.charAt(0));
	}

	@CheckReturnValue
	@Nullable
	private static List<String> splitTopLevelPlus(@Nonnull String s) {
		final var parts = new ArrayList<String>();
		var depth = 0;
		var lastSplit = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if (c == '"') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
						i += 2;
						continue;
					}
					if (c == '\'') {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= s.length())
					return null;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/')
				return null;
			if (ch == '(' || ch == '[' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '}') {
				if (depth == 0)
					return null;
				--depth;
			}
			else if (ch == '+' && depth == 0) {
				parts.add(s.substring(lastSplit, i));
				lastSplit = i + 1;
			}
			++i;
		}
		if (depth != 0)
			return null;
		parts.add(s.substring(lastSplit));
		// a blank operand means the `+` was unary or an increment (`s + ++i`, `s + +b`),
		// not a concat separator; splicing it would emit an argument-less `.append()`
		for (var part : parts) {
			if (part.isBlank())
				return null;
		}
		return parts;
	}

	/**
	 * Verifies that an array-element LHS like `arr[i]` is loop-stable: the
	 * array variable and the index expression's identifier(s) are not mutated
	 * anywhere in the loop scope, and the index identifier is not a for-each
	 * iteration variable. The body line itself is excluded since it always
	 * "writes" to the LHS by definition.
	 */
	@CheckReturnValue
	private static boolean validateArrayLhsLoopStable(
			@Nonnull List<String> lines, @Nonnull LoopInfo loop, int bodyLineIdx, @Nonnull String lhsText
	) {
		final var firstBracket = lhsText.indexOf('[');
		if (firstBracket < 0)
			return false;
		final var receiverPart = lhsText.substring(0, firstBracket);
		final var lastDot = receiverPart.lastIndexOf('.');
		final var arrayName = lastDot >= 0 ? receiverPart.substring(lastDot + 1) : receiverPart;
		// Extract every `[index]` expression. Supports chained indexing like
		// `arr[i][j]` and `this.arr[k][m]`; each index expression is validated
		// independently for loop-stability.
		final var indexExpressions = new ArrayList<String>();
		var pos = firstBracket;
		while (pos < lhsText.length()) {
			if (lhsText.charAt(pos) != '[')
				break;
			var depth = 0;
			var closeIdx = -1;
			for (var k = pos; k < lhsText.length(); ++k) {
				final var ch = lhsText.charAt(k);
				if (ch == '[')
					++depth;
				else if (ch == ']') {
					--depth;
					if (depth == 0) {
						closeIdx = k;
						break;
					}
				}
			}
			if (closeIdx < 0)
				return false;
			indexExpressions.add(lhsText.substring(pos + 1, closeIdx).strip());
			pos = closeIdx + 1;
		}
		if (indexExpressions.isEmpty() || pos != lhsText.length())
			return false;
		// Each index must be a simple IDENT or pure integer literal; anything
		// more complex (method calls, arithmetic, member access) we don't analyze.
		final var identIndexes = new ArrayList<String>();
		for (var indexText : indexExpressions) {
			if (indexText.isEmpty())
				return false;
			if (Character.isJavaIdentifierStart(indexText.charAt(0))) {
				for (var k = 0; k < indexText.length(); ++k) {
					if (!Character.isJavaIdentifierPart(indexText.charAt(k)))
						return false;
				}
				identIndexes.add(indexText);
			}
			else if (Character.isDigit(indexText.charAt(0))) {
				for (var k = 0; k < indexText.length(); ++k) {
					final var ch = indexText.charAt(k);
					if (!Character.isDigit(ch) && ch != 'L' && ch != 'l')
						return false;
				}
			}
			else
				return false;
		}
		// Reject when any index identifier appears on ANY line of the for-header.
		// Covers classic-for `int i = 0; ...; ++i` (binding on top line),
		// for-each `for (T x : ...)`, and multi-line for-headers where the
		// binding is on a continuation line. We bound the scan to the actual
		// for-header (paren-depth tracked across lines) so body-sibling
		// statements like `obj.k = 5;` aren't incorrectly treated as a binding.
		// For non-for loops, no for-header binding semantics apply, so skip.
		final var forHeaderEnd = findForHeaderEnd(lines, loop.topLineIdx());
		if (loop.kind() == LoopKind.FOR && forHeaderEnd < 0)
			return false;
		if (forHeaderEnd >= 0) {
			var headerState = entryStateAt(lines, loop.topLineIdx());
			for (var headerIdx = loop.topLineIdx(); headerIdx <= forHeaderEnd; ++headerIdx) {
				final var headerLine = lines.get(headerIdx);
				for (var idx : identIndexes) {
					if (mentionsIdentifier(headerLine, idx, headerState))
						return false;
				}
				headerState = JavaLineScanner.stateAfter(headerLine, headerState);
			}
		}
		// Enumerate every dotted prefix of the receiver chain (including the
		// leftmost segment) EXCLUDING the full chain itself. For
		// `this.matrix.cells`, this gives `["this", "this.matrix"]`.
		// Mutation of any prefix in the loop scope (e.g. `this.matrix = ...`
		// or `this = ...`, the latter illegal Java but harmlessly conservative)
		// invalidates the post-loop write. The full chain is allowed to appear
		// as part of `<lhsText>.<safe>()` reads, validated separately.
		final var dottedPrefixes = enumerateDottedPrefixes(receiverPart);
		final var intermediatePrefixes = dottedPrefixes.size() <= 1
				? List.<String>of()
				: dottedPrefixes.subList(0, dottedPrefixes.size() - 1);
		final var bracketPortion = lhsText.substring(firstBracket);
		// Scan loop scope: top line through end. The body line legitimately
		// contains the LHS (`arr[idx] = arr[idx] + ...`); only check it for
		// actual MUTATION patterns. Non-body lines must reference the array
		// only as `<receiver>[<exact-lhs-indices>]` (followed by anything;
		// safe-method validation is done by `verifyNoOtherVarUseInLoop`).
		// Anything else (bare receiver, sibling-element index, method-call
		// arg) is rejected.
		final var scanFrom = loop.topLineIdx();
		final var scanTo = Math.min(loop.endLineIdx(), lines.size() - 1);
		// Conservative bail on text blocks anywhere in the loop scope: the
		// line-by-line scanners can't track `"""` content across lines, and a
		// text block whose content textually matches the array LHS would pass
		// validation only to be corrupted by the subsequent `rewriteSafeMethodCalls`.
		for (var i = scanFrom; i <= scanTo; ++i) {
			if (lines.get(i).contains("\"\"\""))
				return false;
		}
		// threaded rather than restarted per line: a scanned line can continue a block
		// comment opened above the loop, whose carried content a cold lexer reads as
		// code -- a quote in the comment's tail would blank the rest of the line and
		// hide a real mutation sitting after the `*/`
		var scanState = entryStateAt(lines, scanFrom);
		for (var i = scanFrom; i <= scanTo; ++i) {
			final var line = lines.get(i);
			final var lineState = scanState;
			scanState = JavaLineScanner.stateAfter(line, scanState);
			final var isBodyLine = i == bodyLineIdx;
			final var isForHeaderLine = forHeaderEnd >= 0 && i <= forHeaderEnd;
			if (isBodyLine) {
				// Body line legitimately contains arr[idx] = arr[idx] + ... so
				// `mentionsIdentifier` would always match. Only flag actual
				// reassignments / inc-dec on the array variable, the index
				// identifiers, or any chain assignment (catches
				// `obj.f[i] += "x"; obj = newObj();` and
				// `this.matrix.cells[i] += "y"; this.matrix.cells = newCells();`
				// when the body line packs multiple statements). The FULL receiver
				// chain (e.g. `this.matrix.cells`) is also checked: `mutatesIdentifier`
				// on the leaf name correctly skips member-access writes, so without
				// the chain-assignment scan a same-line reassignment of the full
				// chain would slip through.
				if (mutatesIdentifier(line, arrayName, lineState))
					return false;
				for (var idx : identIndexes) {
					if (mutatesIdentifier(line, idx, lineState))
						return false;
				}
				for (var prefix : intermediatePrefixes) {
					if (chainOrThisFormAssigned(line, prefix, lineState))
						return false;
				}
				if (receiverPart.contains(".") && chainOrThisFormAssigned(line, receiverPart, lineState))
					return false;
				continue;
			}
			if (isForHeaderLine) {
				// The for-header is exempt from `lineHasUnsafeArrayReference`
				// (allowing `for (...; i < arr.length; ++i)`), but the array
				// variable's reassignment IS still unsafe: a multi-statement
				// init clause like `for (arr = newArr(), j = 0; ...)` would
				// otherwise slip past, producing a silent semantic break.
				if (mutatesIdentifier(line, arrayName, lineState))
					return false;
				for (var prefix : intermediatePrefixes) {
					if (referencesChainOrThisForm(line, prefix, lineState))
						return false;
				}
				continue;
			}
			// Non-body, non-for-header line: any reference to the receiver
			// chain that is NOT followed by the exact lhs-bracket suffix is
			// rejected. Catches `Arrays.fill(arr, ...)`, `arr[j].method()`
			// (sibling element), `arr.length`, `arr = newArr()`, etc.
			if (lineHasUnsafeArrayReference(line, receiverPart, bracketPortion, lineState))
				return false;
			// For dotted receivers like `this.matrix.cells`, the receiver
			// check above only detects references to the FULL chain. Mutation
			// of an intermediate prefix (`this.matrix = pickNew()`) is missed.
			// Scan each intermediate prefix.
			for (var prefix : intermediatePrefixes) {
				if (referencesChainOrThisForm(line, prefix, lineState))
					return false;
			}
			for (var idx : identIndexes) {
				if (mutatesIdentifier(line, idx, lineState))
					return false;
			}
		}
		return true;
	}

	@CheckReturnValue
	private static boolean verifyNoOtherVarUseInLoop(@Nonnull List<String> lines, @Nonnull LoopInfo loop, int bodyLineIdx, @Nonnull String lhsText) {
		// Conservative bail on text blocks anywhere in the loop scope: the
		// line-by-line scanner doesn't track `"""` content, and a text block
		// mentioning the variable name would pass safe-method validation only to
		// be rewritten as if it were live code by `rewriteSafeMethodCalls`.
		// The scan starts at the header line, not after it: a pre-test `while`/`for`
		// condition is evaluated every iteration, so a reference there is as live as
		// one in the body.
		final var scanFrom = loop.topLineIdx();
		final var scanTo = Math.min(loop.endLineIdx(), lines.size() - 1);
		var lineState = entryStateAt(lines, scanFrom);
		// the terminator line is included: buildStringConcatReplacement rewrites through
		// loop.endLineIdx(), so a reference packed onto it -- a statement cuddled after
		// `}`, or a do-while's `while (s.equals(t))` -- has to be validated like any other
		for (var i = scanFrom; i <= scanTo; ++i) {
			final var entryState = lineState;
			lineState = JavaLineScanner.stateAfter(lines.get(i), lineState);
			if (i == bodyLineIdx)
				continue;
			if (!referencesAreAllSafeMethodCalls(lines.get(i), lhsText, entryState))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		// the helpers below index the line's chars, while categorizeSkip compares against
		// AST positions and needs the reported code-point column unchanged
		final var charColumn = LineText.charIndexOfColumn(line, column);
		if (charColumn < 0)
			return categorizeSkip(lines, lineIndex, column);

		var fixedLine = fixBoxedConstructor(line, charColumn);
		if (fixedLine == null)
			fixedLine = fixNewString(line, charColumn);
		if (fixedLine == null)
			fixedLine = fixStringBuffer(line, charColumn);
		if (fixedLine == null)
			fixedLine = fixToArraySized(line, charColumn);
		if (fixedLine == null)
			fixedLine = fixEmptyStringConcat(lines, lineIndex);
		if (fixedLine == null)
			fixedLine = fixAppendConcat(lines, lineIndex, charColumn);

		if (fixedLine != null)
			return new FixResult(lineIndex, lineIndex, List.of(fixedLine));

		final var loopResult = fixStringConcatInLoop(lines, lineIndex);
		if (loopResult != null)
			return loopResult;

		return categorizeSkip(lines, lineIndex, column);
	}
}