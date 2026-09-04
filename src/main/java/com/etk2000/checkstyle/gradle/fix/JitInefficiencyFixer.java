package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AstSpan;
import com.etk2000.checkstyle.AstSpan.TextPos;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.JitInefficiencyCheck;
import com.etk2000.checkstyle.JitInefficiencyCheck.JitTarget;
import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.TopLevelScan;
import com.etk2000.checkstyle.TopLevelScan.Brackets;
import com.etk2000.checkstyle.TopLevelScan.Target;
import com.etk2000.checkstyle.TopLevelScan.Unbalanced;
import com.etk2000.checkstyle.TopLevelScan.Underflow;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.SpanReformat;
import com.etk2000.checkstyle.gradle.fix.AstSplice.Rewrite;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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

	static final Target ASSIGNMENT_EQUALS = (scan, i) -> {
		if (scan.charAt(i) != '=')
			return false;
		final var prev = i > 0 ? scan.charAt(i - 1) : ' ';
		final var next = i + 1 < scan.length() ? scan.charAt(i + 1) : ' ';
		return prev != '!' && prev != '<' && prev != '>' && prev != '=' && next != '=';
	};

	private static final TopLevelScan ALL_BRACKET_SCAN = new TopLevelScan(Brackets.ALL, Underflow.CLAMP);

	/**
	 * The other way to write the same storage: {@code this.f} for {@code f} and {@code f} for
	 * {@code this.f}, with any trailing chain or index carried along, so {@code this.a.b} pairs with
	 * {@code a.b} and {@code this.arr[i]} with {@code arr[i]}.
	 */
	@CheckReturnValue
	@Nonnull
	private static String aliasSpellingOf(@Nonnull String chain) {
		return chain.startsWith("this.") ? chain.substring(5) : "this." + chain;
	}

	/**
	 * Whether the assignment the line scanner found is the one the check reported on. The loop
	 * rewrite still locates its own statement in the text, so this is the seam where the two could
	 * disagree. Compared with whitespace removed, because only the scanner's spelling is normalized.
	 *
	 * <p>The shape that disagrees is an assignment nested inside the outer chain,
	 * {@code a = a + (s = s + x);}: both are reported, and without this the inner one is handed the
	 * rewrite built for the outer. Measured by removing the comparison, which left the whole suite
	 * green and that one shape mis-attributed.
	 */
	@CheckReturnValue
	private static boolean assignsTheReportedTarget(
			@Nonnull List<String> lines,
			@Nonnull JitTarget target,
			@Nonnull AssignInfo assign
	) {
		final var lhs = target.argument();
		final var reported = lhs == null ? null : AstSpan.sliceNode(lines, lhs);
		return reported != null && reported.replaceAll("\\s", "").equals(assign.lhsText().replaceAll("\\s", ""));
	}

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
		var lineState = SpanReformat.lexerStateAt(lines, loop.topLineIdx());
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
	 * Whether {@code line} assigns {@code chain}, or its {@code this.}-qualified form when
	 * {@code chain} is bare. The full-fix pipeline's NoUnnecessaryThis fixer
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
	 * line comments, and block comments.
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
	 * Returns true if the given line contains {@code chain} as a substring with
	 * identifier-style boundaries. Skips strings, char literals, and comments.
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
		return ALL_BRACKET_SCAN.contains(s, ',', Unbalanced.FOUND);
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
		return ALL_BRACKET_SCAN.contains(s, ';', Unbalanced.NOT_FOUND);
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
	 * Whether the local {@code literalNew} initializes was declared {@code var}.
	 *
	 * <p>The {@code StringBuffer} rewrite replaces the class name inside the {@code new} and
	 * nothing else, so an explicitly typed local keeps declaring the type the initializer no
	 * longer produces: {@code StringBuffer sb = new StringBuilder();} does not compile. This
	 * repo's own fixtures all use {@code var} because {@code PreferVarCheck} requires it, which is
	 * why the shape never appeared; a consumer project without that check writes it routinely.
	 */
	@CheckReturnValue
	private static boolean declaredTypeIsInferred(@Nonnull DetailAST literalNew) {
		for (var parent = literalNew.getParent(); parent != null; parent = parent.getParent()) {
			if (parent.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var declared = parent.findFirstToken(TokenTypes.TYPE);
			final var name = declared == null ? null : declared.findFirstToken(TokenTypes.IDENT);
			return name != null && "var".equals(name.getText());
		}
		return false;
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
		final var eqIdx = ALL_BRACKET_SCAN.indexOf(withoutSemi, ASSIGNMENT_EQUALS);
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

	/**
	 * Returns {@code {line, index}} of the {@code )} closing the loop header that opens
	 * on {@code loopTopIdx}, or {@code {-1, -1}} when it never closes.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] findLoopHeaderEnd(@Nonnull List<String> lines, int loopTopIdx) {
		final var topState = SpanReformat.lexerStateAt(lines, loopTopIdx);
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

	/**
	 * Splits {@code .append(a + b + c)} into {@code .append(a).append(b).append(c)}, rewriting from
	 * the {@code .} that introduces the call's own name so the receiver survives exactly as written,
	 * whatever shape it has.
	 */
	@CheckReturnValue
	@Nullable
	private static Rewrite fixAppendConcat(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var call = target.node();
		final var plus = target.argument();
		if (plus == null || !JitInefficiencyCheck.splitsIntoAppendsSafely(plus))
			return null;
		final var operands = operandNodesOf(plus);
		final var regions = operandRegionsOf(lines, plus);
		if (operands == null || regions == null || operands.size() != regions.size())
			return null;

		// the chain writes each operand into the receiver before evaluating the next, so an operand
		// that reads the receiver would observe a half-built value instead of its pre-call state.
		// Compared on identifiers rather than on the receiver's text: a cast or parenthesized
		// receiver has no root name to match, which is why those two shapes used to be refused
		final var receiver = AstQuery.unwrapParensAndExpr(AstSplice.receiverOf(call));
		if (receiver == null)
			return null;
		final var receiverNames = rootIdentifiersIn(receiver);
		for (var operand : operands) {
			if (!Collections.disjoint(rootIdentifiersIn(operand), receiverNames))
				return null;
		}

		final var chain = new StringBuilder();
		for (var region : regions)
			chain.append(".append(").append(region.strip()).append(')');
		return AstSplice.spliceTail(lines, call, call, chain.toString());
	}

	/**
	 * {@code new Integer(x)} becomes {@code Integer.valueOf(x)}, and the two {@code Boolean}
	 * literals become the cached constants. The emitted name is always unqualified, which is valid
	 * even where the source wrote the FQN because {@code java.lang} is auto-imported.
	 */
	@CheckReturnValue
	@Nullable
	private static Rewrite fixBoxedConstructor(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var literalNew = target.node();
		final var type = target.replacement();
		if (type == null)
			return null;
		if ("Float".equals(type) && !floatValueOfAccepts(target.argument()))
			return null;
		final var argument = AstSplice.parenthesizedArgumentsOf(lines, literalNew);
		if (argument == null)
			return null;
		final var value = argument.strip();
		if (value.isEmpty())
			return null;
		if ("Boolean".equals(type) && ("true".equals(value) || "false".equals(value)))
			return AstSplice.spliceNode(lines, literalNew, "Boolean." + value.toUpperCase(Locale.ROOT));
		return AstSplice.spliceNode(lines, literalNew, type + ".valueOf(" + value + ")");
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite fixEmptyStringConcat(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var plus = target.node();
		final var operand = target.argument();
		if (operand == null || !JitInefficiencyCheck.concatenationSurvivesValueOf(operand))
			return null;
		final var plusPos = AstSplice.positionOf(lines, plus);
		final var start = AstSpan.spanStart(lines, plus);
		final var end = AstSpan.spanEnd(lines, plus);
		final var operandStart = AstSpan.spanStart(lines, operand);
		if (plusPos == null || start == null || end == null || operandStart == null)
			return null;

		// the empty literal sits on whichever side the surviving operand does not. Taking the
		// operand as the text on the far side of the `+` rather than as a slice of its own node:
		// grouping parens are siblings of the operand under the PLUS, so `"" + (a + b)` would
		// otherwise slice to the single character `(`
		final var emptyIsOnTheLeft = operandStart.line() > plusPos.line()
				|| (operandStart.line() == plusPos.line() && operandStart.index() > plusPos.index());
		final var afterPlus = new TextPos(plusPos.line(), plusPos.index() + 1);
		final var text = emptyIsOnTheLeft
				? AstSplice.textBetween(lines, afterPlus, end)
				: AstSplice.textBetween(lines, start, plusPos);
		if (text == null || text.isBlank())
			return null;

		// the side being dropped has to be the empty literal and nothing else. A parenthesized
		// empty literal (`("") + value`) makes the check hand back that literal as the surviving
		// operand, because the `(` takes the first-child slot the side test reads; slicing the far
		// side then deletes `value` and the result still compiles
		final var discarded = emptyIsOnTheLeft
				? AstSplice.textBetween(lines, start, plusPos)
				: AstSplice.textBetween(lines, afterPlus, end);
		if (discarded == null || !"\"\"".equals(discarded.strip()))
			return null;
		return AstSplice.spliceNode(lines, plus, "String.valueOf(" + text.strip() + ")");
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite fixNewString(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var literalNew = target.node();
		if (target.replacement() == null)
			return null;
		final var argument = AstSplice.parenthesizedArgumentsOf(lines, literalNew);
		if (argument == null)
			return null;
		final var value = argument.strip();
		return value.isEmpty() ? null : AstSplice.spliceNode(lines, literalNew, value);
	}

	@CheckReturnValue
	@Nullable
	private static Rewrite fixStringBuffer(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var className = target.argument();
		if (target.replacement() == null || className == null || !declaredTypeIsInferred(target.node()))
			return null;
		// only the class name is replaced, so a qualifier, a type-use annotation and the
		// constructor's own arguments all survive as written
		return AstSplice.spliceNode(lines, className, "StringBuilder");
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixStringConcatInLoop(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var lineIndex = target.node().getLineNo() - 1;
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;
		final var bodyLine = lines.get(lineIndex);
		// Bail on text blocks and block comments anywhere on the body line; the
		// line-text scanners don't track multi-line literal/comment state.
		if (bodyLine.contains("\"\"\"") || bodyLine.contains("/*"))
			return null;
		// splice point: this line becomes an `sb.append(...)` and the assignment it looks
		// like is hoisted out of the loop
		if (SpanReformat.lexerStateAt(lines, lineIndex).inMultilineLiteral())
			return null;
		// Tier-2 do-while: `do <stmt>; while (cond);` (body shares line with `do`).
		final var bodyStripped = bodyLine.stripLeading();
		if (bodyStripped.length() > 2
				&& bodyStripped.charAt(0) == 'd' && bodyStripped.charAt(1) == 'o'
				&& Character.isWhitespace(bodyStripped.charAt(2)))
			return fixTier2DoWhile(lines, target, lineIndex);
		final var assign = parseConcatAssignment(bodyLine);
		if (assign == null || !assignsTheReportedTarget(lines, target, assign)
				|| !loopOperandsSurviveTheRewrite(target.node()))
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
		if (SpanReformat.lexerStateAt(lines, loop.topLineIdx()).inMultilineLiteral())
			return null;
		// splice point: the write-back follows the loop's last line. Swallowed, a local's
		// `final var s = ...` goes missing and a field's assignment silently drops the
		// whole loop's result
		if (SpanReformat.lexerStateAt(lines, loop.endLineIdx() + 1).inMultilineLiteral())
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
			if (found == null || !initializerSurvivesAnAppend(found))
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
	private static FixResult fixTier2DoWhile(@Nonnull List<String> lines, @Nonnull JitTarget target, int lineIndex) {
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
		if (assign == null || !assignsTheReportedTarget(lines, target, assign)
				|| !loopOperandsSurviveTheRewrite(target.node()))
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
		final var loop = new LoopInfo(lineIndex, lineIndex + 1, LoopKind.DO_WHILE, false);
		if (!verifyNoOtherVarUseInLoop(lines, loop, lineIndex, assign.lhsText()))
			return null;
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
			if (found == null || !initializerSurvivesAnAppend(found))
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

	/**
	 * Replaces only the size expression with {@code 0}, so the element type, a type-use annotation
	 * on it and any whitespace around the brackets are all left exactly as written.
	 */
	@CheckReturnValue
	@Nullable
	private static Rewrite fixToArraySized(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		final var size = target.argument();
		if (size == null || !sizeEqualsTheCollection(target.node(), size))
			return null;
		return AstSplice.spliceNode(lines, size, "0");
	}

	/**
	 * Whether {@code Float.valueOf} has an overload for this argument.
	 *
	 * <p>{@code Float} is the one boxed type whose factory is narrower than its constructor:
	 * {@code new Float(double)} exists and {@code Float.valueOf(double)} does not, so
	 * {@code new Float(1.5)} rewrites to code that fails with "no suitable method found for
	 * valueOf(double)". Verified against javac: {@code 1.5f}, {@code 1} and {@code "1.5"} compile,
	 * {@code 1.5}, {@code 1.5d} and a {@code double} variable do not.
	 *
	 * <p>The suffix is what decides it, not the token type: checkstyle tokenizes {@code 1.5} as
	 * {@code NUM_FLOAT} too. An identifier is refused rather than resolved, which also turns away
	 * the {@code float} variable that would have been safe.
	 */
	@CheckReturnValue
	private static boolean floatValueOfAccepts(@Nullable DetailAST argument) {
		if (argument == null)
			return false;
		if (argument.getType() == TokenTypes.NUM_INT || argument.getType() == TokenTypes.STRING_LITERAL)
			return true;
		if (argument.getType() != TokenTypes.NUM_FLOAT)
			return false;
		final var literal = argument.getText();
		final var suffix = literal.isEmpty() ? ' ' : literal.charAt(literal.length() - 1);
		return suffix == 'F' || suffix == 'f';
	}

	@CheckReturnValue
	private static int indexOfOperand(@Nonnull List<DetailAST> operands, @Nonnull DetailAST target) {
		for (var i = 0; i < operands.size(); ++i) {
			if (AstQuery.astStructuralEquals(operands.get(i), target))
				return i;
		}
		return -1;
	}

	/**
	 * Whether the accumulator's initializer can be spliced into an {@code append(...)}. Only an
	 * unadorned {@code null} cannot: it picks no overload, so the emitted source would not compile.
	 * A cast one ({@code (String) null}) is fine and must keep being fixed. Nothing else is at risk
	 * here, because the initializer is spliced whole and a {@code String} variable cannot be
	 * initialized from a {@code char[]} in the first place.
	 */
	@CheckReturnValue
	private static boolean initializerSurvivesAnAppend(@Nonnull DeclInfo decl) {
		// grouping parens do not change which overload `null` picks, so `(null)` is the same hazard
		var initializer = decl.initExpr().strip();
		while (initializer.startsWith("(") && initializer.endsWith(")"))
			initializer = initializer.substring(1, initializer.length() - 1).strip();
		return !"null".equals(initializer);
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

	/** Whether {@code ident} is the name after a {@code .}, which denotes a member and not a variable. */
	@CheckReturnValue
	private static boolean isDotSuffix(@Nonnull DetailAST ident) {
		final var parent = ident.getParent();
		return parent != null && parent.getType() == TokenTypes.DOT && parent.getFirstChild() != ident;
	}

	@CheckReturnValue
	private static boolean isInSameScope(@Nonnull List<String> lines, int declLineIdx, int targetLineIdx) {
		var depth = 0;
		var state = SpanReformat.lexerStateAt(lines, declLineIdx + 1);
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

	/**
	 * Whether the loop rewrite renders every operand the way the concatenation it replaces did.
	 *
	 * <p>Only an operand emitted <em>alone</em> is at risk. {@link #buildAppendBody} gives each
	 * append its own {@code append(...)} and a lone prepend its own {@code insert(0, ...)}, where
	 * a {@code char[]} binds the array overload and writes the characters the concatenation would
	 * have rendered as {@code [C@1b6d}, and a bare {@code null} picks no overload at all. Several
	 * prepends are joined into one {@code insert(0, a + b)} whose argument is a String however the
	 * parts render, so that form is safe and must keep being fixed.
	 */
	@CheckReturnValue
	private static boolean loopOperandsSurviveTheRewrite(@Nonnull DetailAST assign) {
		final var lhs = assign.getFirstChild();
		final var rhs = lhs == null ? null : lhs.getNextSibling();
		if (rhs == null)
			return false;

		final var chain = AstQuery.unwrapParensAndExpr(rhs);
		if (chain == null)
			return false;

		// `s += X` splices X whole into one append, so the chain is never taken apart
		if (assign.getType() != TokenTypes.ASSIGN || chain.getType() != TokenTypes.PLUS)
			return JitInefficiencyCheck.concatenationSurvivesValueOf(chain);

		final var operands = operandNodesOf(chain);
		if (operands == null)
			return false;

		// the accumulator need not be a bare name: `arr[i] = arr[i] + x` reads it through an index
		final var accumulator = indexOfOperand(operands, lhs);
		if (accumulator < 0)
			return false;

		for (var i = accumulator + 1; i < operands.size(); ++i) {
			if (!JitInefficiencyCheck.concatenationSurvivesValueOf(operands.get(i)))
				return false;
		}
		return accumulator != 1 || JitInefficiencyCheck.concatenationSurvivesValueOf(operands.getFirst());
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
		var state = SpanReformat.lexerStateAt(lines, loopEndIdx + 1);
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
	private static boolean namesTheEnclosingInstance(@Nonnull DetailAST qualifier) {
		final var type = qualifier.getType();
		if (type == TokenTypes.LITERAL_SUPER || type == TokenTypes.LITERAL_THIS)
			return true;

		final var last = type == TokenTypes.DOT ? qualifier.getLastChild() : null;
		return last != null && last.getType() == TokenTypes.LITERAL_THIS;
	}

	/**
	 * The top-level operands of a {@code +} chain, as nodes. {@code A + B + C} parses as
	 * {@code PLUS(PLUS(A, B), C)}, so the chain's spine is its left edge; a parenthesized left
	 * operand ends the spine, which is right, because the group is one operand.
	 */
	@CheckReturnValue
	@Nullable
	private static List<DetailAST> operandNodesOf(@Nonnull DetailAST plus) {
		final var spine = spineOf(plus);
		final var operands = new ArrayList<DetailAST>();
		operands.add(AstQuery.unwrapParensAndExpr(spine.getLast().getFirstChild()));
		for (var i = spine.size() - 1; i >= 0; --i)
			operands.add(AstQuery.unwrapParensAndExprFromEnd(spine.get(i).getLastChild()));
		return operands.contains(null) ? null : operands;
	}

	/**
	 * The same operands as {@link #operandNodesOf}, but as the verbatim text between the chain's
	 * {@code +} tokens, so grouping parens and any comment inside an operand ride along.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> operandRegionsOf(@Nonnull List<String> lines, @Nonnull DetailAST plus) {
		final var start = AstSpan.spanStart(lines, plus);
		final var end = AstSpan.spanEnd(lines, plus);
		if (start == null || end == null)
			return null;
		final var spine = spineOf(plus);
		final var regions = new ArrayList<String>();
		var from = start;
		// the spine runs outermost first, so its operators are in right-to-left source order
		for (var i = spine.size() - 1; i >= 0; --i) {
			final var operator = AstSplice.positionOf(lines, spine.get(i));
			final var region = operator == null ? null : AstSplice.textBetween(lines, from, operator);
			if (region == null)
				return null;
			regions.add(region);
			from = new TextPos(operator.line(), operator.index() + 1);
		}
		final var last = AstSplice.textBetween(lines, from, end);
		if (last == null)
			return null;
		regions.add(last);
		return regions;
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
				|| containsReceiverChain(line, aliasSpellingOf(chain), entryState);
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

	/**
	 * The one rewrite each category has, or null when this category has none and when the one it
	 * has refuses the shape. Dispatching on the category rather than trying every rewrite against
	 * the line is what keeps a rewrite off a violation that is not its own: four of the seven used
	 * a positional anchor that could match left of the reported column or ignore it outright, and a
	 * foreign category reaching the loop rewriter emitted {@code this.total = sb.toString();} for an
	 * {@code int} field, which does not compile.
	 */
	@CheckReturnValue
	@Nullable
	private static FixResult rewrittenFor(@Nonnull List<String> lines, @Nonnull JitTarget target) {
		return switch (target.category()) {
			case APPEND_CONCAT -> singleLine(fixAppendConcat(lines, target));
			// the seven the fixer recognizes and leaves to the developer: each needs a declaration
			// moved, a type changed or a loop restructured, none of which is a splice
			case BOXED_ACCUMULATOR, DOUBLE_BRACE, ENUM_VALUES_IN_LOOP, ITERATOR_LOOP, MAP_KEYSET_GET,
					REUSABLE_OBJECT, STRING_REGEX_IN_LOOP -> null;
			case BOXED_CONSTRUCTOR -> singleLine(fixBoxedConstructor(lines, target));
			case EMPTY_STRING_CONCAT -> singleLine(fixEmptyStringConcat(lines, target));
			case NEW_STRING -> singleLine(fixNewString(lines, target));
			case STRING_BUFFER -> singleLine(fixStringBuffer(lines, target));
			case STRING_CONCAT_IN_LOOP -> fixStringConcatInLoop(lines, target);
			case TOARRAY_SIZED -> singleLine(fixToArraySized(lines, target));
		};
	}

	/**
	 * The variables {@code node} reads, which is every identifier in its subtree except the ones
	 * naming a member. {@code foo.sb} reads {@code foo}, not a variable called {@code sb}, so a
	 * receiver named {@code sb} does not collide with it.
	 *
	 * <p>{@code this} and {@code super} count as names of their own, except in a {@code this.f},
	 * {@code super.f} or {@code Outer.this.f} qualified field, which contributes the field name
	 * {@code f}.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> rootIdentifiersIn(@Nonnull DetailAST node) {
		final var names = new HashSet<String>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			final var type = current.getType();

			// NoUnnecessaryThisFixer may strip one side's `this.` earlier in the same pass, so the
			// qualified and bare spellings of one field have to collect the same name
			if (type == TokenTypes.DOT) {
				final var qualifier = current.getFirstChild();
				final var field = current.getLastChild();
				if (qualifier != null && field != null && field.getType() == TokenTypes.IDENT
						&& namesTheEnclosingInstance(qualifier)) {
					names.add(field.getText());
					continue;
				}
			}
			if (type == TokenTypes.LITERAL_SUPER || type == TokenTypes.LITERAL_THIS
					|| (type == TokenTypes.IDENT && !isDotSuffix(current)))
				names.add(current.getText());
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return names;
	}

	/** A one-line rewrite as the fix result that replaces that line, or null when there was none. */
	@CheckReturnValue
	@Nullable
	private static FixResult singleLine(@Nullable Rewrite rewrite) {
		return rewrite == null ? null : new FixResult(rewrite.line(), rewrite.line(), List.of(rewrite.text()));
	}

	/**
	 * Whether the array size is the collection's own count, which is the only size the rewrite can
	 * drop without changing what the call returns.
	 *
	 * <p>{@code Collection.toArray(T[] a)} hands back {@code a} itself whenever
	 * {@code a.length >= size}, padding the tail with nulls. Measured on a three-element list,
	 * {@code toArray(new String[16])} has length 16 with {@code [3] == null} while
	 * {@code toArray(new String[0])} has length 3. So a literal, a bare identifier, and a
	 * {@code size()} on some <em>other</em> receiver can each change the result; only a count read
	 * from the same receiver the call is made on is provably equal to it.
	 *
	 * <p>The check still reports the rest. The performance advice holds either way, and whether
	 * the padding is load-bearing is the developer's to know.
	 */
	@CheckReturnValue
	private static boolean sizeEqualsTheCollection(@Nonnull DetailAST call, @Nonnull DetailAST size) {
		if (size.getType() != TokenTypes.METHOD_CALL)
			return false;
		final var elist = size.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 0)
			return false;
		final var dot = size.findFirstToken(TokenTypes.DOT);
		final var name = dot == null ? null : dot.getLastChild();
		if (name == null || name.getType() != TokenTypes.IDENT
				|| (!"length".equals(name.getText()) && !"size".equals(name.getText())))
			return false;
		final var collection = AstQuery.unwrapParensAndExpr(AstSplice.receiverOf(call));
		final var counted = AstQuery.unwrapParensAndExpr(dot.getFirstChild());
		return collection != null && counted != null
				&& AstQuery.isSideEffectFree(counted)
				&& AstQuery.astStructuralEquals(counted, collection);
	}

	/** The {@code PLUS} nodes forming a concatenation chain's left spine, outermost first. */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> spineOf(@Nonnull DetailAST plus) {
		final var spine = new ArrayList<DetailAST>();
		var node = plus;
		while (true) {
			spine.add(node);
			final var first = node.getFirstChild();
			if (first == null || first.getType() != TokenTypes.PLUS)
				return spine;
			node = first;
		}
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
	 * anywhere in the loop scope. The body line itself is checked for
	 * mutation only, since it always "writes" to the LHS by definition.
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
			var headerState = SpanReformat.lexerStateAt(lines, loop.topLineIdx());
			for (var headerIdx = loop.topLineIdx(); headerIdx <= forHeaderEnd; ++headerIdx) {
				final var headerLine = lines.get(headerIdx);
				for (var idx : identIndexes) {
					if (mentionsIdentifier(headerLine, idx, headerState))
						return false;
				}
				headerState = JavaLineScanner.stateAfter(headerLine, headerState);
			}
		}
		// Mutation of any prefix in the loop scope (e.g. `this.matrix = ...`
		// or `this = ...`, the latter illegal Java but harmlessly conservative)
		// invalidates the post-loop write, so the body line refuses an assignment to a
		// prefix and every other line refuses any mention of one. The full chain is
		// allowed to appear as part of `<lhsText>.<safe>()` reads, validated separately.
		final var dottedPrefixes = enumerateDottedPrefixes(receiverPart);
		final var intermediatePrefixes = dottedPrefixes.size() <= 1
				? List.<String>of()
				: dottedPrefixes.subList(0, dottedPrefixes.size() - 1);
		final var bracketPortion = lhsText.substring(firstBracket);
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
		// code: a quote in the comment's tail would blank the rest of the line and
		// hide a real mutation sitting after the `*/`
		var scanState = SpanReformat.lexerStateAt(lines, scanFrom);
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
		// the scan starts at the header line, not after it: a pre-test `while`/`for`
		// condition is evaluated every iteration, so a reference there is as live as
		// one in the body
		final var scanFrom = loop.topLineIdx();
		final var scanTo = Math.min(loop.endLineIdx(), lines.size() - 1);
		var lineState = SpanReformat.lexerStateAt(lines, scanFrom);
		// the terminator line is included: buildStringConcatReplacement rewrites through
		// loop.endLineIdx(), so a reference packed onto it (a statement cuddled after
		// `}`, or a do-while's `while (s.equals(t))`) has to be validated like any other
		for (var i = scanFrom; i <= scanTo; ++i) {
			final var entryState = lineState;
			lineState = JavaLineScanner.stateAfter(lines.get(i), lineState);
			// the body line legitimately spells the accumulator, on both sides of its own
			// assignment, so only the other lines are checked for a reference to it
			if (i != bodyLineIdx && !referencesAreAllSafeMethodCalls(lines.get(i), lhsText, entryState))
				return false;

			// the write-back lands after the loop, so a read through the spelling the rewrite does
			// not redirect sees the pre-loop value on every iteration, and an operand is as live
			// as any
			if (containsReceiverChain(lines.get(i), aliasSpellingOf(lhsText), entryState))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var target = FixerAst.withAst(lines, root -> JitInefficiencyCheck.locateAt(root, lineIndex, column));
		// a buffer an earlier fix in the same pass left unparseable, or a position nothing starts
		// at. Neither is a recognized violation, so there is no category to name a skip reason for
		if (target == null)
			return null;
		final var rewritten = rewrittenFor(lines, target);
		return rewritten != null ? rewritten : new SkipResult(SkipMessages.get(target.category().skipReasonKey()));
	}
}