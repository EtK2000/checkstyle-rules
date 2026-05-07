package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
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

	private record LoopInfo(int topLineIdx, int endLineIdx, @Nonnull LoopKind kind, boolean braced) {}

	private static final Set<String> BOXED_PRIMITIVE_TYPES = Set.of(
			"Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short"
	);
	private static final Set<String> SAFE_STRING_METHODS_ON_BUILDER = Set.of(
			"charAt", "chars", "codePointAt", "codePoints", "isEmpty",
			"length", "subSequence"
	);

	@CheckReturnValue
	@Nonnull
	private static String buildAppendBody(@Nonnull String indent, @Nonnull AssignInfo assign) {
		final var sb = new StringBuilder(indent);
		final var prepends = assign.prepends().stream().map(op -> rewriteSafeMethodCalls(op, assign.lhsText())).toList();
		final var appends = assign.appends().stream().map(op -> rewriteSafeMethodCalls(op, assign.lhsText())).toList();
		if (prepends.isEmpty()) {
			sb.append("sb");
			for (var op : appends)
				sb.append(".append(").append(op).append(')');
		}
		else {
			sb.append("sb.insert(0, ");
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

	@CheckReturnValue
	@Nonnull
	private static FixResult buildStringConcatReplacement(
			@Nonnull List<String> lines,
			@Nonnull DeclInfo decl,
			@Nonnull LoopInfo loop,
			@Nonnull AssignInfo assign,
			int bodyLineIdx
	) {
		final var declIndent = decl.isField() ? extractIndent(lines.get(loop.topLineIdx())) : extractIndent(lines.get(decl.lineIdx()));
		final var newBody = buildAppendBody(assign.indent(), assign);

		final var replacement = new ArrayList<String>();
		final int spanStart;
		if (decl.isField()) {
			spanStart = loop.topLineIdx();
			replacement.add(declIndent + "final var sb = new StringBuilder();");
			replacement.add(declIndent + "sb.append(" + assign.lhsText() + ");");
		}
		else {
			spanStart = decl.lineIdx();
			replacement.add(declIndent + "final var sb = new StringBuilder();");
			if (!"\"\"".equals(decl.initExpr()))
				replacement.add(declIndent + "sb.append(" + decl.initExpr() + ");");
			for (var i = decl.lineIdx() + 1; i < loop.topLineIdx(); ++i)
				replacement.add(lines.get(i));
		}

		replacement.add(lines.get(loop.topLineIdx()));
		// Pass-through any pre-body lines inside the loop (e.g. when body is buried
		// inside a multi-statement block). Safe-method calls on the variable get
		// rewritten to the StringBuilder receiver.
		for (var i = loop.topLineIdx() + 1; i < bodyLineIdx; ++i)
			replacement.add(rewriteSafeMethodCalls(lines.get(i), assign.lhsText()));
		replacement.add(newBody);
		// Pass-through any post-body lines inside the loop and the close brace / while.
		for (var i = bodyLineIdx + 1; i <= loop.endLineIdx(); ++i)
			replacement.add(rewriteSafeMethodCalls(lines.get(i), assign.lhsText()));

		final String postLine;
		if (decl.isField())
			postLine = declIndent + assign.lhsText() + " = sb.toString();";
		else
			postLine = declIndent + "final var " + decl.varName() + " = sb.toString();";
		replacement.add(postLine);

		return new FixResult(spanStart, loop.endLineIdx(), replacement);
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
	private static boolean containsChainAssignment(@Nonnull String line, @Nonnull String chain) {
		if (chain.isEmpty())
			return false;
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			if (i + chain.length() <= line.length() && line.regionMatches(i, chain, 0, chain.length())) {
				final var prev = i == 0 ? '\0' : line.charAt(i - 1);
				final var afterChain = i + chain.length();
				final var leftOk = prev != '.' && !Character.isJavaIdentifierPart(prev);
				final var rightOk = afterChain >= line.length()
						|| !Character.isJavaIdentifierPart(line.charAt(afterChain));
				if (leftOk && rightOk) {
					var j = afterChain;
					while (j < line.length() && Character.isWhitespace(line.charAt(j)))
						++j;
					if (j < line.length()) {
						final var op = line.charAt(j);
						if (op == '=' && (j + 1 >= line.length() || line.charAt(j + 1) != '='))
							return true;
						if ((op == '+' || op == '-' || op == '*' || op == '/' || op == '%'
								|| op == '&' || op == '|' || op == '^')
								&& j + 1 < line.length() && line.charAt(j + 1) == '='
								&& (j + 2 >= line.length() || line.charAt(j + 2) != '='))
							return true;
					}
					i = afterChain;
					continue;
				}
			}
			if (Character.isJavaIdentifierStart(ch)) {
				while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
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
	private static boolean containsReceiverChain(@Nonnull String line, @Nonnull String chain) {
		if (chain.isEmpty())
			return false;
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			if (i + chain.length() <= line.length() && line.regionMatches(i, chain, 0, chain.length())) {
				final var leftOk = i == 0
						|| (!Character.isJavaIdentifierPart(line.charAt(i - 1)) && line.charAt(i - 1) != '.');
				final var afterEnd = i + chain.length();
				final var rightOk = afterEnd >= line.length()
						|| !Character.isJavaIdentifierPart(line.charAt(afterEnd));
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
		var depth = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
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
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= s.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/')
				return false;
			if (ch == '(' || ch == '[' || ch == '<' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '>' || ch == '}') {
				if (depth > 0)
					--depth;
			}
			else if (depth == 0 && ch == ',')
				return true;
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsTopLevelPlus(@Nonnull String s) {
		var depth = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
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
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= s.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/')
				return false;
			if (ch == '(' || ch == '[')
				++depth;
			else if (ch == ')' || ch == ']') {
				if (depth > 0)
					--depth;
			}
			else if (ch == '+' && depth == 0)
				return true;
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static int countParensIgnoringLiterals(@Nonnull String s) {
		var count = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
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
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= s.length())
					return count;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/')
				return count;
			if (ch == '(')
				++count;
			++i;
		}
		return count;
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
	private static int expandedIndentWidth(@Nonnull String indent) {
		var w = 0;
		for (var i = 0; i < indent.length(); ++i) {
			final var ch = indent.charAt(i);
			if (ch == '\t')
				w = (w / 8 + 1) * 8;
			else
				++w;
		}
		return w;
	}

	@CheckReturnValue
	@Nonnull
	private static String extractIndent(@Nonnull String line) {
		var i = 0;
		while (i < line.length() && Character.isWhitespace(line.charAt(i)))
			++i;
		return line.substring(0, i);
	}

	@CheckReturnValue
	private static int findClosingBracket(@Nonnull String line, int openBracket) {
		var depth = 0;
		var i = openBracket;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				final var quote = ch;
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
						i += 2;
						continue;
					}
					if (c == quote) {
						++i;
						break;
					}
					++i;
				}
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length()
						&& !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return -1;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return -1;
			if (ch == '[')
				++depth;
			else if (ch == ']') {
				--depth;
				if (depth == 0)
					return i;
			}
			++i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findClosingParen(@Nonnull String line, int openParen) {
		var depth = 0;
		var i = openParen;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length()
						&& !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return -1;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return -1;
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					return i;
			}
			++i;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static DeclInfo findDeclarationAbove(@Nonnull List<String> lines, int searchFromIdx, @Nonnull String varName) {
		for (var i = searchFromIdx; i >= 0; --i) {
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
	private static int findDoWhileLine(@Nonnull List<String> lines, int from, int doIndent) {
		for (var i = from + 1; i < lines.size(); ++i) {
			final var line = lines.get(i);
			final var stripped = line.strip();
			if (stripped.isEmpty())
				continue;
			final var indent = expandedIndentWidth(extractIndent(line));
			if (indent == doIndent && (stripped.startsWith("while ") || stripped.startsWith("while(")))
				return i;
			if (indent < doIndent)
				return -1;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static LoopInfo findEnclosingLoop(@Nonnull List<String> lines, int bodyLineIdx) {
		if (bodyLineIdx <= 0 || bodyLineIdx >= lines.size())
			return null;
		var currentIdx = bodyLineIdx;
		var currentIndent = expandedIndentWidth(extractIndent(lines.get(currentIdx)));
		while (true) {
			final var parent = findParentAtLowerIndent(lines, currentIdx, currentIndent);
			if (parent < 0)
				return null;
			final var stripped = lines.get(parent).strip();
			if (matchesLoopTop(stripped)) {
				final var braced = stripped.endsWith("{");
				final var kind = stripped.startsWith("for") ? LoopKind.FOR : LoopKind.WHILE;
				if (braced) {
					final var endIdx = findMatchingClose(lines, parent);
					if (endIdx < 0)
						return null;
					return new LoopInfo(parent, endIdx, kind, true);
				}
				return new LoopInfo(parent, bodyLineIdx, kind, false);
			}
			if ("do".equals(stripped) || "do {".equals(stripped)) {
				final var braced = "do {".equals(stripped);
				final int whileIdx;
				if (braced) {
					final var endIdx = findMatchingClose(lines, parent);
					if (endIdx < 0 || endIdx + 1 >= lines.size())
						return null;
					whileIdx = endIdx + 1;
				}
				else {
					whileIdx = findDoWhileLine(lines, bodyLineIdx, expandedIndentWidth(extractIndent(lines.get(parent))));
					if (whileIdx < 0)
						return null;
				}
				final var whileStripped = lines.get(whileIdx).strip();
				if (!whileStripped.startsWith("while ") && !whileStripped.startsWith("while("))
					return null;
				if (!whileStripped.endsWith(";"))
					return null;
				return new LoopInfo(parent, whileIdx, LoopKind.DO_WHILE, braced);
			}
			if (matchesIfTop(stripped)) {
				currentIdx = parent;
				currentIndent = expandedIndentWidth(extractIndent(lines.get(parent)));
				continue;
			}
			return null;
		}
	}

	@CheckReturnValue
	private static int findExprEnd(@Nonnull String line, int from) {
		var depth = 0;
		var i = from;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return i;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return i;
			// Generic type witness `obj.<Type>method()`: treat `<` after `.` as
			// depth-bumping (paired with matching `>`) rather than a comparison.
			if (ch == '<' && depth == 0) {
				var prev = i - 1;
				while (prev >= from && line.charAt(prev) == ' ')
					--prev;
				if (prev >= from && line.charAt(prev) == '.') {
					++depth;
					++i;
					continue;
				}
			}
			if (ch == '>' && depth > 0) {
				var prev = i - 1;
				while (prev >= from && line.charAt(prev) == ' ')
					--prev;
				if (prev >= from && (Character.isJavaIdentifierPart(line.charAt(prev))
						|| line.charAt(prev) == ',' || line.charAt(prev) == '?'
						|| line.charAt(prev) == '>' || line.charAt(prev) == ']')) {
					--depth;
					++i;
					continue;
				}
			}
			if (ch == '(' || ch == '[')
				++depth;
			else if (ch == ')' || ch == ']') {
				if (depth == 0)
					return i;
				--depth;
			}
			else if (depth == 0) {
				if (ch == ',' || ch == ';')
					return i;
				// stop at lower-precedence operators that bind weaker than `+`:
				// `==`, `!=`, `<=`, `>=`, `<`, `>`, `&&`, `||`, `?`, `:`
				if (ch == '?' || ch == ':')
					return i;
				if ((ch == '=' || ch == '!') && i + 1 < line.length() && line.charAt(i + 1) == '=')
					return i;
				if ((ch == '<' || ch == '>') && (i + 1 >= line.length() || line.charAt(i + 1) != ch))
					return i;
				if ((ch == '&' || ch == '|') && i + 1 < line.length() && line.charAt(i + 1) == ch)
					return i;
			}
			++i;
		}
		return i;
	}

	/**
	 * Returns the line index where the matching `)` of a for-loop header
	 * closes, or {@code -1} if the loop top isn't a for-loop. The header may
	 * span multiple lines (e.g. multi-line for-each `for (int i :\n indices)`).
	 * Skips strings, char literals, and comments while tracking paren depth.
	 */
	@CheckReturnValue
	private static int findForHeaderEnd(@Nonnull List<String> lines, int loopTopIdx) {
		final var topLine = lines.get(loopTopIdx);
		final var stripped = topLine.stripLeading();
		if (!stripped.startsWith("for ") && !stripped.startsWith("for("))
			return -1;
		final var openParen = topLine.indexOf('(');
		if (openParen < 0)
			return -1;
		var depth = 0;
		for (var lineIdx = loopTopIdx; lineIdx < lines.size(); ++lineIdx) {
			final var line = lines.get(lineIdx);
			var i = lineIdx == loopTopIdx ? openParen : 0;
			while (i < line.length()) {
				final var ch = line.charAt(i);
				if (ch == '"' || ch == '\'') {
					++i;
					while (i < line.length()) {
						final var c = line.charAt(i);
						if (c == '\\' && i + 1 < line.length()) {
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
				if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
					break;
				if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
					i += 2;
					while (i + 1 < line.length()
							&& !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
						++i;
					if (i + 1 >= line.length())
						return -1;
					i += 2;
					continue;
				}
				if (ch == '(')
					++depth;
				else if (ch == ')') {
					--depth;
					if (depth == 0)
						return lineIdx;
				}
				++i;
			}
		}
		return -1;
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
	private static int findMatchingClose(@Nonnull List<String> lines, int openLineIdx) {
		final var openIndent = extractIndent(lines.get(openLineIdx));
		var inBlockComment = false;
		for (var i = openLineIdx + 1; i < lines.size(); ++i) {
			final var line = lines.get(i);
			final var stripped = line.stripLeading();
			if (!inBlockComment) {
				if (stripped.isEmpty())
					continue;
				final var indent = extractIndent(line);
				if (indent.equals(openIndent) && stripped.startsWith("}"))
					return i;
			}
			inBlockComment = scanLineForBlockComment(line, inBlockComment);
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
			final var indent = expandedIndentWidth(extractIndent(line));
			if (indent < childIndent)
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findTopLevelAssignEquals(@Nonnull String s) {
		var depth = 0;
		var i = 0;
		while (i < s.length()) {
			final var ch = s.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < s.length()) {
					final var c = s.charAt(i);
					if (c == '\\' && i + 1 < s.length()) {
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
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < s.length() && !(s.charAt(i) == '*' && s.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= s.length())
					return -1;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < s.length() && s.charAt(i + 1) == '/')
				return -1;
			if (ch == '(' || ch == '[' || ch == '<' || ch == '{')
				++depth;
			else if (ch == ')' || ch == ']' || ch == '>' || ch == '}') {
				if (depth > 0)
					--depth;
			}
			else if (depth == 0 && ch == '=') {
				final var prev = i > 0 ? s.charAt(i - 1) : ' ';
				final var next = i + 1 < s.length() ? s.charAt(i + 1) : ' ';
				if (prev != '!' && prev != '<' && prev != '>' && prev != '=' && next != '=')
					return i;
			}
			++i;
		}
		return -1;
	}

	@CheckReturnValue
	@Nullable
	private static String fixAppendConcat(@Nonnull String line, int column) {
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		final var appendIdx = line.lastIndexOf(".append(", column);
		if (appendIdx < 0)
			return null;
		final var openParen = appendIdx + ".append".length();
		final var closeParen = findClosingParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argsStart = openParen + 1;
		final var arg = line.substring(argsStart, closeParen);
		final var parts = splitTopLevelPlus(arg);
		if (parts == null || parts.size() < 2)
			return null;
		// the FIRST operand must contain a String literal. Otherwise the leading
		// operands may be a numeric-add chain (e.g. `1 + 2 + "x"` evaluates as `"3x"`,
		// but splitting yields `.append(1).append(2).append("x")` = `"12x"`).
		if (!parts.getFirst().contains("\""))
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
		while (typeEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(typeEnd)))
			++typeEnd;
		final var typeName = line.substring(typeStart, typeEnd);
		if (!BOXED_PRIMITIVE_TYPES.contains(typeName))
			return null;
		if (typeEnd >= line.length() || line.charAt(typeEnd) != '(')
			return null;
		final var openParen = typeEnd;
		final var closeParen = findClosingParen(line, openParen);
		if (closeParen < 0)
			return null;
		final var argText = line.substring(openParen + 1, closeParen).strip();
		if ("Boolean".equals(typeName)) {
			if ("true".equals(argText))
				return line.substring(0, column) + "Boolean.TRUE" + line.substring(closeParen + 1);
			if ("false".equals(argText))
				return line.substring(0, column) + "Boolean.FALSE" + line.substring(closeParen + 1);
		}
		return line.substring(0, column) + typeName + ".valueOf(" + argText + ")"
				+ line.substring(closeParen + 1);
	}

	@CheckReturnValue
	@Nullable
	private static String fixEmptyStringConcat(@Nonnull String line, int column) {
		// bail on text blocks (line-based fixer can't reason about multi-line literal regions)
		if (line.contains("\"\"\""))
			return null;
		// `"" + expr` form
		final var leftIdx = line.indexOf("\"\" + ");
		if (leftIdx >= 0) {
			final var rhsStart = leftIdx + 5;
			final var rhsEnd = findExprEnd(line, rhsStart);
			// bail on multiline: if findExprEnd consumed all the way to end-of-line,
			// the expression continues on the next line and we can't safely capture it.
			if (rhsEnd < 0 || rhsEnd >= line.length())
				return null;
			final var rhs = line.substring(rhsStart, rhsEnd);
			// reject if rhs has a top-level `+` (chain like `"" + a + b` would change semantics)
			if (containsTopLevelPlus(rhs))
				return null;
			return line.substring(0, leftIdx) + "String.valueOf(" + rhs + ")" + line.substring(rhsEnd);
		}
		// `expr + ""` form
		final var rightIdx = line.indexOf(" + \"\"");
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
		final var closeParen = findClosingParen(line, openParen);
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
		if (loop.braced() || loop.kind() == LoopKind.DO_WHILE) {
			if (lineIndex <= loop.topLineIdx() || lineIndex >= loop.endLineIdx())
				return null;
		}
		else if (lineIndex != loop.endLineIdx())
			return null;
		// Bail on if-with-else around the assignment. Scan forward beyond loopEnd for
		// unbraced loops, since the body can syntactically extend through else clauses
		// at indents greater than the loop top's.
		final var loopTopIndent = expandedIndentWidth(extractIndent(lines.get(loop.topLineIdx())));
		final var scanLimit = loop.braced() ? loop.endLineIdx() : lines.size();
		for (var i = lineIndex + 1; i < scanLimit; ++i) {
			final var raw = lines.get(i);
			final var stripped = raw.stripLeading();
			if (stripped.isEmpty())
				continue;
			final var indent = expandedIndentWidth(extractIndent(raw));
			if (indent <= loopTopIndent)
				break;
			if (stripped.startsWith("else ") || stripped.equals("else") || stripped.startsWith("else{")
					|| stripped.startsWith("} else") || stripped.startsWith("}else"))
				return null;
		}
		// For array-element LHS, the index must be a simple IDENT or integer
		// literal that's stable across iterations (not for-each iter var, not
		// mutated in the loop scope, array variable not reassigned).
		final var isArrayLhs = assign.lhsText().contains("[");
		if (isArrayLhs && !validateArrayLhsLoopStable(lines, loop, lineIndex, assign.lhsText()))
			return null;
		final DeclInfo decl;
		if (isFieldLhs) {
			// Synthetic decl for the field accessor: span starts at the loop top.
			decl = new DeclInfo(loop.topLineIdx(), "String", assign.varName(), "", false, true);
		}
		else {
			// Decl can be anywhere above the loop within the same scope, as long as no
			// statement between the decl and the loop top references the variable.
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
			decl = found;
		}
		// No other reference to the var inside the loop body region.
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
		// Build a synthetic body line by stripping the `do<ws>` prefix; use any extra
		// indent (`\t`) for the rewritten statement. parseConcatAssignment expects
		// the line to end with `;`.
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
		// Find while line.
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
		if (expandedIndentWidth(extractIndent(whileLine)) != expandedIndentWidth(indent))
			return null;
		if (!referencesAreAllSafeMethodCalls(whileLine, assign.lhsText()))
			return null;
		final var loop = new LoopInfo(lineIndex, lineIndex + 1, LoopKind.DO_WHILE, false);
		// Array-element LHS: validate index loop-stability before proceeding.
		if (assign.lhsText().contains("[")
				&& !validateArrayLhsLoopStable(lines, loop, lineIndex, assign.lhsText()))
			return null;
		// Find declaration (or synthesize for field LHS).
		final DeclInfo decl;
		if (isFieldLhs)
			decl = new DeclInfo(lineIndex, "String", assign.varName(), "", false, true);
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
			decl = found;
		}
		// Build replacement: always emit tier-3 form (do / body / while).
		final var newBody = buildAppendBody(indent + "\t", assign);
		final var declIndent = decl.isField() ? indent : extractIndent(lines.get(decl.lineIdx()));
		final var replacement = new ArrayList<String>();
		final int spanStart;
		if (decl.isField()) {
			spanStart = lineIndex;
			replacement.add(declIndent + "final var sb = new StringBuilder();");
			replacement.add(declIndent + "sb.append(" + assign.lhsText() + ");");
		}
		else {
			spanStart = decl.lineIdx();
			replacement.add(declIndent + "final var sb = new StringBuilder();");
			if (!"\"\"".equals(decl.initExpr()))
				replacement.add(declIndent + "sb.append(" + decl.initExpr() + ");");
			for (var i = decl.lineIdx() + 1; i < lineIndex; ++i)
				replacement.add(lines.get(i));
		}
		// If the rewritten body is a single non-chained call (one `(` outside
		// strings/chars), keep tier-2 (`do <body>; while(...);`); otherwise
		// expand to tier-3.
		final var bodyStripped = newBody.stripLeading();
		if (countParensIgnoringLiterals(bodyStripped) == 1)
			replacement.add(indent + "do " + bodyStripped);
		else {
			replacement.add(indent + "do");
			replacement.add(newBody);
		}
		replacement.add(rewriteSafeMethodCalls(whileLine, assign.lhsText()));
		final String postLine;
		if (decl.isField())
			postLine = declIndent + assign.lhsText() + " = sb.toString();";
		else
			postLine = declIndent + "final var " + decl.varName() + " = sb.toString();";
		replacement.add(postLine);
		return new FixResult(spanStart, loop.endLineIdx(), replacement);
	}

	@CheckReturnValue
	@Nullable
	private static String fixToArraySized(@Nonnull String line, int column) {
		// the violation column may point at the receiver or the LPAREN; the prefix
		// `.toArray(new ` may lie before or after `column`. Try lastIndexOf first
		// (matches LPAREN-anchored case) and fall back to indexOf (receiver-anchored).
		final var prefix = ".toArray(new ";
		var idx = line.lastIndexOf(prefix, Math.max(0, column));
		if (idx < 0)
			idx = line.indexOf(prefix, Math.max(0, column));
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

	@CheckReturnValue
	private static boolean isInSameScope(@Nonnull List<String> lines, int declLineIdx, int targetLineIdx) {
		// Walk forward from decl to target, tracking brace balance with proper
		// string/char/comment skipping (incl. multi-line `/* */` and `"""`).
		// The decl and target must be in the same brace scope: depth must
		// return to 0 by the time we reach `targetLineIdx`. If the depth ever
		// drops below 0 or ends non-zero, we crossed a scope boundary. Bail
		// also if we encounter a text block (line-based scanner can't reason
		// across `"""`).
		var depth = 0;
		var inBlockComment = false;
		// Walk lines strictly between decl and target; the target line itself is the
		// loop top (or do-line) whose braces belong to the loop body, not the
		// enclosing scope. Including it would falsely raise depth.
		for (var lineIdx = declLineIdx + 1; lineIdx < targetLineIdx; ++lineIdx) {
			final var line = lines.get(lineIdx);
			if (line.contains("\"\"\""))
				return false;
			var i = 0;
			while (i < line.length()) {
				if (inBlockComment) {
					if (i + 1 < line.length() && line.charAt(i) == '*' && line.charAt(i + 1) == '/') {
						inBlockComment = false;
						i += 2;
						continue;
					}
					++i;
					continue;
				}
				final var ch = line.charAt(i);
				if (ch == '"' || ch == '\'') {
					++i;
					while (i < line.length()) {
						final var c = line.charAt(i);
						if (c == '\\' && i + 1 < line.length()) {
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
				if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
					inBlockComment = true;
					i += 2;
					continue;
				}
				if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
					break;
				if (ch == '{')
					++depth;
				else if (ch == '}') {
					--depth;
					if (depth < 0)
						return false;
				}
				++i;
			}
		}
		// Decl and target must be in the same scope: net brace balance is 0.
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
	 * Returns true if the given line contains a reference to {@code receiverText}
	 * that is NOT followed by exactly {@code bracketPortion} (the `[idx]` /
	 * `[idx][jdx]` suffix from the LHS expression). Used by
	 * {@link #validateArrayLhsLoopStable} to detect dangerous uses of the
	 * array variable on non-body lines: bare references (`Arrays.fill(arr, ...)`),
	 * sibling-element references (`arr[j]` for `arr[k]` LHS), reads of other
	 * properties (`arr.length`), and assignments (`arr = newArr()`). References
	 * that match exactly `<receiver><bracketPortion>` are allowed (they're
	 * either the LHS itself or a safe-method-call receiver, separately
	 * validated by {@link #verifyNoOtherVarUseInLoop}).
	 * Skips strings, char literals, and comments.
	 */
	@CheckReturnValue
	private static boolean lineHasUnsafeArrayReference(
			@Nonnull String line, @Nonnull String receiverText, @Nonnull String bracketPortion
	) {
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return false;
				i += 2;
				continue;
			}
			if (i + receiverText.length() <= line.length()
					&& line.regionMatches(i, receiverText, 0, receiverText.length())) {
				final var prev = i == 0 ? '\0' : line.charAt(i - 1);
				final var afterRecv = i + receiverText.length();
				final var leftOk = prev != '.' && !Character.isJavaIdentifierPart(prev);
				final var rightOk = afterRecv >= line.length()
						|| !Character.isJavaIdentifierPart(line.charAt(afterRecv));
				if (leftOk && rightOk) {
					if (afterRecv + bracketPortion.length() <= line.length()
							&& line.regionMatches(afterRecv, bracketPortion, 0, bracketPortion.length())) {
						i = afterRecv + bracketPortion.length();
						continue;
					}
					return true;
				}
			}
			if (Character.isJavaIdentifierStart(ch)) {
				while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
					++i;
				continue;
			}
			++i;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean matchesIfTop(@Nonnull String stripped) {
		return (stripped.startsWith("if (") || stripped.startsWith("if("))
				&& !stripped.contains("else");
	}

	@CheckReturnValue
	private static boolean matchesLoopTop(@Nonnull String stripped) {
		return stripped.startsWith("for ") || stripped.startsWith("for(")
				|| stripped.startsWith("while ") || stripped.startsWith("while(");
	}

	@CheckReturnValue
	private static boolean mentionsIdentifier(@Nonnull String line, @Nonnull String name) {
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			if (Character.isJavaIdentifierStart(ch)) {
				final var start = i;
				while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
					++i;
				final var word = line.substring(start, i);
				if (word.equals(name))
					return true;
				continue;
			}
			++i;
		}
		return false;
	}

	/**
	 * Returns true if the given identifier is mutated anywhere on the line
	 * (assignment with `=`, compound assignment `<op>=`, or pre/post inc/dec).
	 * Skips strings, char literals, and comments. Used to detect whether the
	 * index of an `arr[i]` LHS is stable across loop iterations.
	 */
	@CheckReturnValue
	private static boolean mutatesIdentifier(@Nonnull String line, @Nonnull String name) {
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return false;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			// Pre-increment/decrement: ++name or --name.
			if ((ch == '+' || ch == '-') && i + 1 < line.length() && line.charAt(i + 1) == ch) {
				final var afterOp = i + 2;
				if (afterOp + name.length() <= line.length()
						&& line.regionMatches(afterOp, name, 0, name.length())
						&& (afterOp + name.length() >= line.length()
								|| !Character.isJavaIdentifierPart(line.charAt(afterOp + name.length()))))
					return true;
			}
			if (Character.isJavaIdentifierStart(ch) || ch == '_') {
				final var prev = i == 0 ? '\0' : line.charAt(i - 1);
				final var start = i;
				while (i < line.length() && Character.isJavaIdentifierPart(line.charAt(i)))
					++i;
				final var word = line.substring(start, i);
				if (!word.equals(name))
					continue;
				// Skip member access: `obj.name = ...` is a write to obj.name, not name.
				if (prev == '.')
					continue;
				// Post-increment/decrement: name++ or name--.
				if (i + 1 < line.length()) {
					final var c1 = line.charAt(i);
					final var c2 = line.charAt(i + 1);
					if ((c1 == '+' && c2 == '+') || (c1 == '-' && c2 == '-'))
						return true;
				}
				// Assignment / compound assignment: name [ws]* (= or op=)
				var j = i;
				while (j < line.length() && Character.isWhitespace(line.charAt(j)))
					++j;
				if (j < line.length()) {
					final var op = line.charAt(j);
					if (op == '=' && (j + 1 >= line.length() || line.charAt(j + 1) != '='))
						return true;
					if ((op == '+' || op == '-' || op == '*' || op == '/' || op == '%'
							|| op == '&' || op == '|' || op == '^')
							&& j + 1 < line.length() && line.charAt(j + 1) == '='
							&& (j + 2 >= line.length() || line.charAt(j + 2) != '='))
						return true;
					// Shift compound assignment: <<=, >>=, >>>=.
					if ((op == '<' || op == '>') && j + 1 < line.length() && line.charAt(j + 1) == op) {
						var k = j + 2;
						if (op == '>' && k < line.length() && line.charAt(k) == '>')
							++k;
						if (k < line.length() && line.charAt(k) == '='
								&& (k + 1 >= line.length() || line.charAt(k + 1) != '='))
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
				final var closeIdx = findClosingBracket(trimmed, bracketEnd);
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
		if (rhs.isEmpty())
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
		return new AssignInfo(indent, lhsText, varName, prepends, appends);
	}

	@CheckReturnValue
	private static boolean referencesAreAllSafeMethodCalls(@Nonnull String line, @Nonnull String lhsText) {
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/'))
					++i;
				if (i + 1 >= line.length())
					return true;
				i += 2;
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return true;
			if (line.startsWith(lhsText, i)) {
				if (i > 0) {
					final var prev = line.charAt(i - 1);
					if (prev == '.' || Character.isJavaIdentifierPart(prev)) {
						++i;
						continue;
					}
				}
				final var afterLhs = i + lhsText.length();
				if (afterLhs < line.length() && Character.isJavaIdentifierPart(line.charAt(afterLhs))) {
					++i;
					continue;
				}
				if (afterLhs >= line.length() || line.charAt(afterLhs) != '.')
					return false;
				final var methodStart = afterLhs + 1;
				var methodEnd = methodStart;
				while (methodEnd < line.length() && Character.isJavaIdentifierPart(line.charAt(methodEnd)))
					++methodEnd;
				final var method = line.substring(methodStart, methodEnd);
				if (!SAFE_STRING_METHODS_ON_BUILDER.contains(method))
					return false;
				if (methodEnd >= line.length() || line.charAt(methodEnd) != '(')
					return false;
				i = methodEnd;
				continue;
			}
			++i;
		}
		return true;
	}

	@CheckReturnValue
	@Nonnull
	private static String rewriteSafeMethodCalls(@Nonnull String line, @Nonnull String lhsText) {
		final var out = new StringBuilder();
		var i = 0;
		while (i < line.length()) {
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				out.append(ch);
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					out.append(c);
					if (c == '\\' && i + 1 < line.length()) {
						out.append(line.charAt(i + 1));
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				out.append("/*");
				i += 2;
				while (i + 1 < line.length() && !(line.charAt(i) == '*' && line.charAt(i + 1) == '/')) {
					out.append(line.charAt(i));
					++i;
				}
				if (i + 1 < line.length()) {
					out.append("*/");
					i += 2;
				}
				continue;
			}
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
				out.append(line, i, line.length());
				return out.toString();
			}
			if (line.startsWith(lhsText, i)) {
				final var leftBoundaryOk = i == 0
						|| (line.charAt(i - 1) != '.' && !Character.isJavaIdentifierPart(line.charAt(i - 1)));
				final var afterLhs = i + lhsText.length();
				final var rightBoundaryOk = afterLhs >= line.length()
						|| !Character.isJavaIdentifierPart(line.charAt(afterLhs));
				if (leftBoundaryOk && rightBoundaryOk) {
					out.append("sb");
					i = afterLhs;
					continue;
				}
			}
			out.append(ch);
			++i;
		}
		return out.toString();
	}

	@CheckReturnValue
	private static boolean scanLineForBlockComment(@Nonnull String line, boolean startInBlockComment) {
		var inBlockComment = startInBlockComment;
		var i = 0;
		while (i < line.length()) {
			if (inBlockComment) {
				if (i + 1 < line.length() && line.charAt(i) == '*' && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					i += 2;
					continue;
				}
				++i;
				continue;
			}
			final var ch = line.charAt(i);
			if (ch == '"' || ch == '\'') {
				++i;
				while (i < line.length()) {
					final var c = line.charAt(i);
					if (c == '\\' && i + 1 < line.length()) {
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
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return false;
			if (ch == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				inBlockComment = true;
				i += 2;
				continue;
			}
			++i;
		}
		return inBlockComment;
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
			if (ch == '(')
				++depth;
			else if (ch == ')') {
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
		return parts;
	}

	/**
	 * Verifies that an array-element LHS like `arr[i]` is loop-stable: the
	 * array variable and the index expression's identifier(s) are not mutated
	 * anywhere in the loop scope, and the index identifier is not a for-each
	 * iteration variable. The body line itself is excluded since it always
	 * "writes" to the LHS by definition.
	 *
	 * @return null if not loop-stable, otherwise a non-null marker
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
			for (var headerIdx = loop.topLineIdx(); headerIdx <= forHeaderEnd; ++headerIdx) {
				final var headerLine = lines.get(headerIdx);
				for (var idx : identIndexes) {
					if (mentionsIdentifier(headerLine, idx))
						return false;
				}
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
		for (var i = scanFrom; i <= scanTo; ++i) {
			final var line = lines.get(i);
			final var isBodyLine = i == bodyLineIdx;
			final var isForHeaderLine = forHeaderEnd >= 0 && i <= forHeaderEnd;
			if (isBodyLine) {
				// Body line legitimately contains arr[idx] = arr[idx] + ... so
				// `mentionsIdentifier` would always match. Only flag actual
				// reassignments / inc-dec on the array variable, the index
				// identifiers, or any chain-prefix assignment (catches
				// `obj.f[i] += "x"; obj = newObj();` and
				// `this.matrix.cells[i] += "y"; this.matrix = newMatrix();`
				// when the body line packs multiple statements).
				if (mutatesIdentifier(line, arrayName))
					return false;
				for (var idx : identIndexes) {
					if (mutatesIdentifier(line, idx))
						return false;
				}
				for (var prefix : intermediatePrefixes) {
					if (containsChainAssignment(line, prefix))
						return false;
				}
				continue;
			}
			// For-header line: index-binding is already checked above; the
			// header legitimately reads loop-bound expressions like `i < n`,
			// `j < arr.length`, etc., so don't run the strict
			// `lineHasUnsafeArrayReference` check on it. Index-mutation
			// checks (e.g. `++i`) are still meaningful, though they're already
			// implied by the binding check.
			if (isForHeaderLine) {
				// The for-header is exempt from `lineHasUnsafeArrayReference`
				// (allowing `for (...; i < arr.length; ++i)`), but the array
				// variable's reassignment IS still unsafe: a multi-statement
				// init clause like `for (arr = newArr(), j = 0; ...)` would
				// otherwise slip past, producing a silent semantic break.
				if (mutatesIdentifier(line, arrayName))
					return false;
				for (var prefix : intermediatePrefixes) {
					if (containsReceiverChain(line, prefix))
						return false;
				}
				continue;
			}
			// Non-body, non-for-header line: any reference to the receiver
			// chain that is NOT followed by the exact lhs-bracket suffix is
			// rejected. Catches `Arrays.fill(arr, ...)`, `arr[j].method()`
			// (sibling element), `arr.length`, `arr = newArr()`, etc.
			if (lineHasUnsafeArrayReference(line, receiverPart, bracketPortion))
				return false;
			// For dotted receivers like `this.matrix.cells`, the receiver
			// check above only detects references to the FULL chain. Mutation
			// of an intermediate prefix (`this.matrix = pickNew()`) is missed.
			// Scan each intermediate prefix.
			for (var prefix : intermediatePrefixes) {
				if (containsReceiverChain(line, prefix))
					return false;
			}
			for (var idx : identIndexes) {
				if (mutatesIdentifier(line, idx))
					return false;
			}
		}
		return true;
	}

	@CheckReturnValue
	private static boolean verifyNoOtherVarUseInLoop(@Nonnull List<String> lines, @Nonnull LoopInfo loop, int bodyLineIdx, @Nonnull String lhsText) {
		// Outside the body line, the only allowed mentions of the variable are
		// `<varName>.<safeMethod>(...)` calls (per the SAFE_STRING_METHODS_ON_BUILDER
		// allowlist). Anything else (bare reference, write, unsafe-method call) bails.
		final var scanFrom = loop.topLineIdx() + 1;
		final var scanTo = loop.endLineIdx();
		for (var i = scanFrom; i < scanTo; ++i) {
			if (i == bodyLineIdx)
				continue;
			if (!referencesAreAllSafeMethodCalls(lines.get(i), lhsText))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);

		var result = fixBoxedConstructor(line, column);
		if (result == null)
			result = fixNewString(line, column);
		if (result == null)
			result = fixStringBuffer(line, column);
		if (result == null)
			result = fixToArraySized(line, column);
		if (result == null)
			result = fixEmptyStringConcat(line, column);
		if (result == null)
			result = fixAppendConcat(line, column);

		if (result != null)
			return new FixResult(lineIndex, lineIndex, List.of(result));

		return fixStringConcatInLoop(lines, lineIndex);
	}
}