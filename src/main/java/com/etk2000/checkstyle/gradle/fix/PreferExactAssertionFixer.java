package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.PreferExactAssertionCheck.ASSERT_CLASS;
import static com.etk2000.checkstyle.PreferExactAssertionCheck.ASSERTIONS_CLASS;
import static com.etk2000.checkstyle.PreferExactAssertionCheck.isJunitAssertClass;

import com.etk2000.checkstyle.AstUtil;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferExactAssertionFixer implements CheckstyleFixer {
	private record CallFix(
			int startLine,
			int endLine,
			@Nonnull List<String> replacement,
			@Nonnull String replacementMethod,
			boolean qualified,
			@Nullable String qualifierSimpleName
	) {}

	private record CallSpan(
			int methodNameEnd,
			int semiLine,
			int semiCol,
			@Nonnull String argsText
	) {}

	private record NegationResult(@Nonnull String content, boolean negated) {}

	private record RewrittenArgs(@Nonnull String replacementMethod, @Nonnull String newArgs) {}

	/**
	 * Upper bound on the number of line boundaries the cross-line walks in
	 * {@link #buildCallFix} and {@link #findImmediateQualifierName} will cross
	 * before bailing. Bounds the work even on pathological inputs (very long
	 * whitespace/comment runs preceding the call). Sixteen is well above any
	 * reasonable hand-formatted chained-call layout.
	 */
	private static final int MAX_QUALIFIER_LOOKBACK_LINES = 16;

	/**
	 * Canonical JUnit assertion method names. Used to match the FQN prefix in static
	 * imports so the replacement import goes to the correct framework. Excluding
	 * AssertJ ({@code assertThat}) and Truth ({@code assertWithMessage}) which would
	 * otherwise match a generic {@code startsWith("assert")} prefix.
	 */
	private static final Set<String> JUNIT_ASSERT_METHODS = Set.of(
			"assertAll", "assertArrayEquals", "assertDoesNotThrow", "assertEquals",
			"assertFalse", "assertInstanceOf", "assertIterableEquals", "assertLinesMatch",
			"assertNotEquals", "assertNotInstanceOf", "assertNotNull", "assertNotSame",
			"assertNull", "assertSame", "assertThrows", "assertTimeout",
			"assertTimeoutPreemptively", "assertTrue", "fail"
	);

	/**
	 * Scans existing static imports for a JUnit 5 {@code Assertions} prefix and adds the
	 * replacement method's static import to {@code imports}. Restricts to the JUnit 5
	 * {@code Assertions} class only. JUnit 4's {@code Assert} doesn't have
	 * {@code assertInstanceOf}, so adding such an import there would produce a non-
	 * compiling result. AssertJ ({@code assertThat}) and Truth ({@code assertWithMessage})
	 * are also rejected by the method-name whitelist.
	 * <p>
	 * Wildcard imports for the {@code Assertions} class short-circuit (no add needed).
	 * Wildcards for unrelated classes don't block scanning of subsequent imports.
	 */
	private static void addAssertImport(
			@Nonnull List<String> lines,
			@Nonnull String replacementMethod,
			@Nonnull Set<String> imports
	) {
		for (var existing : lines) {
			final var parsed = parseImport(existing);
			if (parsed == null || !parsed.staticImport())
				continue;

			if (parsed.wildcard()) {
				if (isJunit5AssertionsClass(parsed.fqn()))
					return;
				continue;
			}

			final var lastDot = parsed.fqn().lastIndexOf('.');
			if (lastDot < 0)
				continue;
			final var methodName = parsed.fqn().substring(lastDot + 1);
			if (!JUNIT_ASSERT_METHODS.contains(methodName))
				continue;
			final var classFqn = parsed.fqn().substring(0, lastDot);
			if (!isJunit5AssertionsClass(classFqn))
				continue;
			imports.add("static " + classFqn + "." + replacementMethod);
			return;
		}
	}

	/**
	 * Companion to {@link #addAssertImport} for the negation rewrite. Adds a static import
	 * for {@code oppositeMethod} (either {@code assertTrue} or {@code assertFalse}) drawn
	 * from the same JUnit-style class as the existing {@code assertTrue}/{@code assertFalse}
	 * static import. Unlike {@link #addAssertImport}, this accepts both {@code Assert}
	 * (JUnit 4) and {@code Assertions} (JUnit 5) because both classes have both methods.
	 * Skips adding when a wildcard for the same class is already present or
	 * {@code oppositeMethod} is already explicitly imported.
	 */
	private static void addOppositeAssertImport(
			@Nonnull List<String> lines,
			@Nonnull String oppositeMethod,
			@Nonnull Set<String> imports
	) {
		String inferredClassFqn = null;
		for (var existing : lines) {
			final var parsed = parseImport(existing);
			if (parsed == null || !parsed.staticImport())
				continue;

			if (parsed.wildcard()) {
				if (isJunitAssertClass(AstUtil.simpleName(parsed.fqn())))
					return;
				continue;
			}

			final var lastDot = parsed.fqn().lastIndexOf('.');
			if (lastDot < 0)
				continue;
			final var methodName = parsed.fqn().substring(lastDot + 1);
			final var classFqn = parsed.fqn().substring(0, lastDot);
			if (!isJunitAssertClass(AstUtil.simpleName(classFqn)))
				continue;
			if (oppositeMethod.equals(methodName))
				return;
			if (!JUNIT_ASSERT_METHODS.contains(methodName))
				continue;
			if (inferredClassFqn == null)
				inferredClassFqn = classFqn;
		}
		if (inferredClassFqn != null)
			imports.add("static " + inferredClassFqn + "." + oppositeMethod);
	}

	/**
	 * Returns true if {@code argsText} contains a structural token the rewrite logic
	 * can't handle safely:
	 * <ul>
	 *     <li>Line comment ({@code //}) outside strings/chars/block-comments: would
	 *         consume the rewritten {@code );} after the arg is flattened.</li>
	 *     <li>Explicit-type-argument prefix ({@code .<}): contains commas that confuse
	 *         {@link #splitTopLevelArgs}.</li>
	 *     <li>Source-level Unicode escape (backslash-u) outside literals: Java's
	 *         compiler preprocesses these into real characters BEFORE tokenization
	 *         (JLS 3.3), but our text-based scanners don't, so a Unicode-escaped
	 *         {@code //} in the file bytes looks like 12 plain characters here while
	 *         the compiler sees a line comment. Bail to avoid emitting code the
	 *         compiler will interpret differently after the rewrite.</li>
	 * </ul>
	 */
	@CheckReturnValue
	private static boolean argsTextHasUnsafeStructuralToken(@Nonnull String argsText) {
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
		var inTextBlock = false;
		for (var i = 0; i < argsText.length(); ++i) {
			final var c = argsText.charAt(i);
			if (c == '\\' && i + 1 < argsText.length() && argsText.charAt(i + 1) == 'u')
				return true;
			if (inBlockComment) {
				if (c == '*' && i + 1 < argsText.length() && argsText.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inTextBlock) {
				if (c == '"' && i + 2 < argsText.length()
						&& argsText.charAt(i + 1) == '"' && argsText.charAt(i + 2) == '"') {
					inTextBlock = false;
					i += 2;
				}
				else if (c == '\\' && i + 1 < argsText.length())
					++i;
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < argsText.length())
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < argsText.length())
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < argsText.length()) {
				final var next = argsText.charAt(i + 1);
				if (next == '/')
					return true;
				if (next == '*') {
					inBlockComment = true;
					++i;
					continue;
				}
			}
			if (c == '.' && i + 1 < argsText.length() && argsText.charAt(i + 1) == '<')
				return true;
			if (c == '"' && i + 2 < argsText.length()
					&& argsText.charAt(i + 1) == '"' && argsText.charAt(i + 2) == '"') {
				inTextBlock = true;
				i += 2;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static CallFix buildCallFix(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull CallSpan span,
			@Nonnull String methodName,
			@Nonnull RewrittenArgs rewritten
	) {
		final var firstLine = lines.get(lineIndex);
		final var methodStart = span.methodNameEnd() - methodName.length();
		var dotLineIdx = lineIndex;
		var dotCol = methodStart - 1;
		var dotLine = stripLineComment(firstLine);
		var crossings = 0;
		while (true) {
			while (dotCol >= 0 && dotCol < dotLine.length() && Character.isWhitespace(dotLine.charAt(dotCol)))
				--dotCol;
			if (dotCol >= 0)
				break;
			++crossings;
			if (crossings > MAX_QUALIFIER_LOOKBACK_LINES || --dotLineIdx < 0) {
				dotLineIdx = -1;
				break;
			}
			dotLine = stripLineComment(lines.get(dotLineIdx));
			dotCol = dotLine.length() - 1;
		}
		final var qualified = dotLineIdx >= 0 && dotCol < dotLine.length() && dotLine.charAt(dotCol) == '.';
		final var qualifierSimpleName = qualified
				? findImmediateQualifierName(lines, dotLineIdx, dotCol)
				: null;
		final var suffix = lines.get(span.semiLine()).substring(span.semiCol() + 1);

		var callStart = methodStart;
		if (qualified && dotLineIdx == lineIndex) {
			var i = dotCol;
			while (i >= 0 && (Character.isJavaIdentifierPart(firstLine.charAt(i)) || firstLine.charAt(i) == '.'))
				--i;
			callStart = i + 1;
		}
		var indentLen = 0;
		while (indentLen < firstLine.length() && Character.isWhitespace(firstLine.charAt(indentLen)))
			++indentLen;
		final var indent = firstLine.substring(0, indentLen);
		final var leading = firstLine.substring(indentLen, callStart).stripTrailing();
		final var call = firstLine.substring(callStart, methodStart).stripTrailing()
				+ rewritten.replacementMethod + "(" + rewritten.newArgs + ");" + suffix;
		final var combined = leading.isEmpty()
				? indent + call
				: indent + leading + "\n" + indent + call;
		final var resultLines = List.of(combined.split("\n", -1));
		return new CallFix(lineIndex, span.semiLine(), resultLines, rewritten.replacementMethod, qualified, qualifierSimpleName);
	}

	/**
	 * Collapses runs of whitespace (including newlines) to single spaces. Used to
	 * normalize an LHS that crossed lines in the original source.
	 */
	@CheckReturnValue
	@Nonnull
	private static String collapseWhitespace(@Nonnull String s) {
		return s.replaceAll("\\s+", " ");
	}

	/**
	 * Counts standalone occurrences of {@code methodName} on {@code line}, applying the
	 * same identifier-boundary and literal/block-comment skipping as
	 * {@link #findMethodNameEndFrom}. Used to detect ambiguous slow-path inputs where
	 * the caller can't be sure which occurrence the violation refers to.
	 */
	@CheckReturnValue
	private static int countMethodNameOccurrences(@Nonnull String line, @Nonnull String methodName) {
		var count = 0;
		var from = 0;
		while (true) {
			final var nameEnd = findMethodNameEndFrom(line, methodName, from);
			if (nameEnd < 0)
				return count;
			++count;
			from = nameEnd;
		}
	}

	/**
	 * Finds the call span for {@code methodName} starting at {@code lines[lineIndex]}.
	 * Handles both single-line and multi-line calls (open paren on its own line, args
	 * across multiple lines, closing paren and/or semicolon on separate lines). When
	 * {@code column} points to a {@code (} immediately preceded by {@code methodName},
	 * the search uses that call site directly (so multi-call lines disambiguate via
	 * the violation column). Returns the method-name end column, the semicolon location,
	 * and the args text between the open and close parens. Returns null if the call
	 * shape isn't recognized.
	 */
	@CheckReturnValue
	@Nullable
	private static CallSpan findCallSpan(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String methodName
	) {
		final var firstLine = lines.get(lineIndex);

		var openLine = lineIndex;
		int openCol;
		final int methodNameEnd;
		final var nameLen = methodName.length();
		if (column >= 0 && column < firstLine.length() && firstLine.charAt(column) == '('
				&& column >= nameLen
				&& firstLine.startsWith(methodName, column - nameLen)
				&& (column - nameLen == 0
				|| !Character.isJavaIdentifierPart(firstLine.charAt(column - nameLen - 1)))) {
			openCol = column;
			methodNameEnd = column;
		}
		else {
			openCol = findMethodCallOpen(firstLine, methodName);
			if (openCol >= 0) {
				// `name(` adjacency is already verified by findMethodCallOpen; the name
				// ends exactly at openCol. Using openCol directly here (rather than the
				// first match from findMethodNameEndFrom) avoids corrupting the prefix
				// when the line contains an earlier non-call identifier with the same
				// name (e.g. `String msg = assertTrue + " x"; Assertions.assertTrue(o
				// iof Y);`).
				methodNameEnd = openCol;
			}
			else {
				// `(` may be preceded by whitespace or a comment, or on a later line.
				// Bail when the first line has more than one standalone occurrence of
				// the method name: a `name<ws>(` shape earlier on the line plus a later
				// `name<newline>(` shape would make the slow path target the wrong call.
				if (countMethodNameOccurrences(firstLine, methodName) > 1)
					return null;
				final var nameEnd = findMethodNameEndFrom(firstLine, methodName, 0);
				if (nameEnd < 0)
					return null;
				final var paren = scanForwardForChar(lines, lineIndex, nameEnd, '(', true);
				if (paren == null)
					return null;
				methodNameEnd = nameEnd;
				openLine = paren[0];
				openCol = paren[1];
			}
		}

		final var closeLoc = LambdaCallParser.findClosingParen(lines, openLine, openCol);
		if (closeLoc == null)
			return null;
		final var closeLine = closeLoc.line();
		final var closeCol = closeLoc.col();

		// `;` must be whitespace-adjacent to `)`; comments between would be silently
		// dropped on rewrite, so bail.
		final var semiLoc = scanForwardForChar(lines, closeLine, closeCol + 1, ';', false);
		if (semiLoc == null)
			return null;
		return new CallSpan(
				methodNameEnd,
				semiLoc[0],
				semiLoc[1],
				joinArgs(lines, openLine, openCol, closeLine, closeCol)
		);
	}

	/**
	 * Returns the identifier immediately preceding the qualifying dot, walking
	 * backward across whitespace and line boundaries. For
	 * {@code Assert.assertTrue(...)} returns {@code "Assert"}; for
	 * {@code org.junit.Assert.assertTrue(...)} returns {@code "Assert"} (the
	 * rightmost segment of the qualifier chain); for the multi-line shape
	 * {@code Assert\n.assertTrue(...)} also returns {@code "Assert"}. Returns
	 * an empty string when the character preceding the dot (after whitespace)
	 * is not a Java identifier part - e.g. {@code getHelper().assertTrue(...)}
	 * (preceded by {@code )}), {@code (this).assertTrue(...)}, or a block
	 * comment trailer. Empty is used (not null) so callers can compare against
	 * the {@code "Assert"}/{@code "Assertions"} whitelist uniformly.
	 */
	@CheckReturnValue
	@Nonnull
	private static String findImmediateQualifierName(
			@Nonnull List<String> lines,
			int dotLineIdx,
			int dotCol
	) {
		var lineIdx = dotLineIdx;
		var line = stripLineComment(lines.get(lineIdx));
		var col = dotCol - 1;
		var crossings = 0;
		while (true) {
			while (col >= 0 && col < line.length() && Character.isWhitespace(line.charAt(col)))
				--col;
			if (col >= 0 && col < line.length())
				break;
			++crossings;
			if (crossings > MAX_QUALIFIER_LOOKBACK_LINES || --lineIdx < 0)
				return "";
			line = stripLineComment(lines.get(lineIdx));
			col = line.length() - 1;
		}
		// Codepoint-aware: a supplementary char (e.g. mathematical alphanumeric)
		// is encoded as a surrogate pair. Stepping by char would split it and
		// either prematurely terminate the walk or yield a false identifier.
		final var trailingCp = Character.codePointBefore(line, col + 1);
		if (!Character.isJavaIdentifierPart(trailingCp))
			return "";
		final var end = col + 1;
		while (col >= 0) {
			final var cp = Character.codePointBefore(line, col + 1);
			if (!Character.isJavaIdentifierPart(cp))
				break;
			col -= Character.charCount(cp);
		}
		return line.substring(col + 1, end);
	}

	/**
	 * Finds the index of the {@code (} that opens a call to {@code methodName} on
	 * {@code line}, requiring the character before {@code methodName} to NOT be a Java
	 * identifier part (so {@code customAssertTrue(} is not matched), and skipping
	 * occurrences inside string/char/text-block literals and comments. Returns -1 if no
	 * call is found. The match is located on the masked line so literal/comment content
	 * can't spoof a call; the returned index aligns with {@code line}.
	 */
	@CheckReturnValue
	private static int findMethodCallOpen(@Nonnull String line, @Nonnull String methodName) {
		final var pattern = methodName + "(";
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, LexerState.NONE);
		for (var i = 0; i + pattern.length() <= masked.length(); ++i) {
			if (masked.startsWith(pattern, i)
					&& (i == 0 || !Character.isJavaIdentifierPart(masked.charAt(i - 1))))
				return i + methodName.length();
		}
		return -1;
	}

	/**
	 * Finds the end-of-name index for {@code methodName} in {@code line}, scanning from
	 * {@code fromIndex}, without requiring a {@code (} to follow. Returns the index just
	 * past the last char of the name, or -1 if not found. Skips occurrences inside
	 * string/char/text-block literals and comments. Rejects identifier-part neighbors (so
	 * {@code customAssertTrue} / {@code assertTrueX} aren't matched). The match is located on
	 * the masked line so literal/comment content can't spoof a name; indices align with {@code line}.
	 */
	@CheckReturnValue
	private static int findMethodNameEndFrom(@Nonnull String line, @Nonnull String methodName, int fromIndex) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, LexerState.NONE);
		for (var i = fromIndex; i + methodName.length() <= masked.length(); ++i) {
			if (masked.startsWith(methodName, i)
					&& (i == 0 || !Character.isJavaIdentifierPart(masked.charAt(i - 1)))
					&& (i + methodName.length() == masked.length()
					|| !Character.isJavaIdentifierPart(masked.charAt(i + methodName.length()))))
				return i + methodName.length();
		}
		return -1;
	}

	/**
	 * Returns the index of {@code " instanceof "} in {@code text} that sits at
	 * paren/bracket/brace depth 0, outside any string/char/text-block or comment content.
	 * Returns -1 if no top-level occurrence exists. Unlike a plain
	 * {@link LambdaCallParser#indexOfStructural} this also respects paren depth so a
	 * sub-expression like {@code "...(x instanceof Y)..."} is correctly skipped. The scan runs
	 * over the masked text so literal/comment content can't contribute brackets to the depth
	 * count or a spurious {@code instanceof}; the returned index aligns with {@code text}.
	 */
	@CheckReturnValue
	private static int findTopLevelInstanceof(@Nonnull String text) {
		final var needle = " instanceof ";
		final var masked = JavaLineScanner.stripCommentsAndStrings(text, LexerState.NONE);
		var depth = 0;
		for (var i = 0; i + needle.length() <= masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (depth == 0 && masked.startsWith(needle, i))
				return i;
		}
		return -1;
	}

	/**
	 * Returns true if any line in {@code lines} has a STATIC import of an {@code Assertions}
	 * class (single or wildcard). The fixer's output references {@code assertInstanceOf} /
	 * {@code assertNotInstanceOf}, which only exist on JUnit 5's {@code Assertions} (or a
	 * user-defined subclass with the same simple name). Non-static imports don't enable
	 * unqualified method resolution, so they're irrelevant for this gate. When no qualifying
	 * static import is present, the fixer must skip the rewrite or it would produce
	 * non-compiling code.
	 */
	@CheckReturnValue
	private static boolean hasAssertionsImport(@Nonnull List<String> lines) {
		return hasStaticImportOfClass(lines, PreferExactAssertionFixer::isJunit5AssertionsClass);
	}

	/**
	 * Returns true if any line in {@code lines} has a STATIC import of a JUnit 4
	 * {@code Assert} class (simple-name match). Used to detect JUnit 4 + JUnit 5
	 * mixed-import files where an unqualified {@code assertTrue} call would resolve
	 * via JUnit 4 today. Non-static imports are irrelevant (they bring the type into
	 * scope but don't enable unqualified method calls).
	 */
	@CheckReturnValue
	private static boolean hasJunit4AssertImport(@Nonnull List<String> lines) {
		return hasStaticImportOfClass(
				lines,
				fqn -> ASSERT_CLASS.equals(AstUtil.simpleName(fqn))
		);
	}

	/**
	 * Generic helper: walks {@code lines}, parses each as a STATIC {@code import} statement
	 * (whitespace-tolerant), and applies {@code classMatcher} to the imported class FQN
	 * (the method-name suffix is stripped for static single imports; wildcard exports are
	 * stripped). Non-static imports are skipped because they don't enable unqualified method
	 * resolution. Returns true on first match.
	 */
	@CheckReturnValue
	private static boolean hasStaticImportOfClass(
			@Nonnull List<String> lines,
			@Nonnull Predicate<String> classMatcher
	) {
		for (var existing : lines) {
			final var parsed = parseImport(existing);
			if (parsed == null || !parsed.staticImport())
				continue;
			var classFqn = parsed.fqn();
			if (!parsed.wildcard()) {
				final var lastDot = classFqn.lastIndexOf('.');
				if (lastDot < 0)
					continue;
				classFqn = classFqn.substring(0, lastDot);
			}
			if (classMatcher.test(classFqn))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isJavaIdentifier(@Nonnull String s) {
		if (s.isEmpty() || !Character.isJavaIdentifierStart(s.charAt(0)))
			return false;
		for (var i = 1; i < s.length(); ++i) {
			if (!Character.isJavaIdentifierPart(s.charAt(i)))
				return false;
		}
		return true;
	}

	/**
	 * Returns true if {@code fqn} names the JUnit 5 {@code Assertions} class. Matches
	 * by class name only (last segment). Accepts both {@code org.junit.jupiter.api.Assertions}
	 * and any custom subclass shadowing the same simple name. JUnit 4's {@code Assert}
	 * (singular) does not match.
	 */
	@CheckReturnValue
	private static boolean isJunit5AssertionsClass(@Nonnull String fqn) {
		return ASSERTIONS_CLASS.equals(AstUtil.simpleName(fqn));
	}

	/**
	 * Builds the argsText between {@code (openLine, openCol)} (an open paren) and
	 * {@code (closeLine, closeCol)} (its matching close paren). Newlines are kept
	 * between lines so text-block content keeps its structure; outside text blocks
	 * the splitter treats {@code \n} as ordinary whitespace.
	 */
	@CheckReturnValue
	@Nonnull
	private static String joinArgs(
			@Nonnull List<String> lines,
			int openLine,
			int openCol,
			int closeLine,
			int closeCol
	) {
		if (openLine == closeLine)
			return lines.get(openLine).substring(openCol + 1, closeCol);
		final var sb = new StringBuilder();
		for (var i = openLine; i <= closeLine; ++i) {
			final var text = lines.get(i);
			final var from = i == openLine ? openCol + 1 : 0;
			final var to = i == closeLine ? closeCol : text.length();
			if (from < to)
				sb.append(text, from, to);
			if (i < closeLine)
				sb.append('\n');
		}
		return sb.toString();
	}

	/**
	 * Strips any trailing line comment (single-line, literal-aware) and delegates to
	 * {@link ImportLine#parse}; the comment-strip is this fixer's contribution, since
	 * it scans raw source lines that may carry a {@code // ...} suffix.
	 */
	@CheckReturnValue
	@Nullable
	private static ImportLine parseImport(@Nonnull String line) {
		return ImportLine.parse(LambdaCallParser.stripComment(line));
	}

	/**
	 * Applies the {@code assertTrue/assertFalse(... x instanceof Y ...)} rewrite to a
	 * comma-separated args string (possibly multi-line). Returns the rewritten method
	 * name and args, or null if the shape isn't matched.
	 */
	@CheckReturnValue
	@Nullable
	private static RewrittenArgs rewriteArgs(
			@Nonnull String argsText,
			@Nonnull String methodName,
			@Nonnull String positive,
			@Nonnull String negative
	) {
		if (argsTextHasUnsafeStructuralToken(argsText))
			return null;
		final var args = splitTopLevelArgs(argsText);
		if (args.isEmpty() || args.size() > 2)
			return null;

		var instArgIdx = -1;
		String content = null;
		var negated = false;
		for (var i = 0; i < args.size(); ++i) {
			final var arg = args.get(i);
			final var deparen = unwrapOuterParens(arg);
			final var unwrapped = unwrapNegation(deparen);
			final var candidate = unwrapped != null ? unwrapOuterParens(unwrapped.content()) : deparen;
			if (findTopLevelInstanceof(candidate) >= 0) {
				instArgIdx = i;
				content = candidate;
				negated = unwrapped != null && unwrapped.negated();
				break;
			}
		}
		if (instArgIdx < 0)
			return null;

		final var instOffset = findTopLevelInstanceof(content);
		final var lhs = collapseWhitespace(content.substring(0, instOffset).strip());
		var typePart = collapseWhitespace(content.substring(instOffset + " instanceof ".length()).strip());

		final var replacement = negated ? negative : positive;
		// pattern binding ("Y y") is only reachable when the assertion's effective polarity
		// is TRUE: the runtime path that kept the binding ran the body of `instanceof Y`. When
		// the assertion effectively asserts the instanceof is FALSE, the binding never binds,
		// so it's safe to drop.
		if (typePart.contains(" ")) {
			if ("assertInstanceOf".equals(replacement))
				return null;
			final var firstSpace = typePart.indexOf(' ');
			final var typeOnly = typePart.substring(0, firstSpace).strip();
			final var binding = typePart.substring(firstSpace + 1).strip();
			if (typeOnly.isEmpty() || typeOnly.contains(" ") || typeOnly.contains("<"))
				return null;
			if (!isJavaIdentifier(binding))
				return null;
			typePart = typeOnly;
		}
		if (typePart.contains("<"))
			return null;
		if (lhs.isEmpty() || typePart.isEmpty())
			return null;

		final var newArgs = new StringBuilder();
		newArgs.append(typePart).append(".class, ").append(lhs);
		for (var i = 0; i < args.size(); ++i) {
			if (i != instArgIdx)
				newArgs.append(", ").append(args.get(i).strip());
		}
		return new RewrittenArgs(replacement, newArgs.toString());
	}

	/**
	 * Applies the {@code assertTrue/assertFalse(!x)} -> {@code opposite(x)} rewrite to a
	 * comma-separated args string. Finds the single argument whose stripped form starts
	 * with {@code !} (the boolean condition, which may be the first or second arg
	 * depending on JUnit-4 message-first vs JUnit-5 message-last conventions), strips
	 * one leading {@code !} and any outer parens that wrap the entire result, and
	 * returns the rewritten args. Returns null if no argument starts with {@code !} or
	 * if the call has more than two args.
	 */
	@CheckReturnValue
	@Nullable
	private static RewrittenArgs rewriteNegationArgs(@Nonnull String argsText, @Nonnull String opposite) {
		if (argsTextHasUnsafeStructuralToken(argsText))
			return null;
		final var args = splitTopLevelArgs(argsText);
		if (args.isEmpty() || args.size() > 2)
			return null;

		var negArgIdx = -1;
		String strippedInner = null;
		for (var i = 0; i < args.size(); ++i) {
			final var deparen = unwrapOuterParens(args.get(i));
			if (!deparen.startsWith("!"))
				continue;
			// `!=` can't start an expression in valid Java, but guard against malformed input.
			if (deparen.length() < 2 || deparen.charAt(1) == '=')
				continue;
			final var inner = unwrapOuterParens(deparen.substring(1).strip());
			if (inner.isEmpty())
				continue;
			negArgIdx = i;
			strippedInner = inner;
			break;
		}
		if (negArgIdx < 0)
			return null;

		final var newArgs = new StringBuilder();
		for (var i = 0; i < args.size(); ++i) {
			if (i > 0)
				newArgs.append(", ");
			newArgs.append(i == negArgIdx ? strippedInner : args.get(i).strip());
		}
		return new RewrittenArgs(opposite, newArgs.toString());
	}

	/**
	 * Scans {@code lines} starting at {@code (startLine, startCol)} for the first occurrence
	 * of {@code target}. Whitespace is always skipped. Comments are skipped when
	 * {@code allowComments} is true; otherwise hitting any comment returns null
	 * (preserving the source content the fixer would otherwise drop on rewrite).
	 * Returns {@code (line, col)} as a two-element int array, or null if not found.
	 */
	@CheckReturnValue
	@Nullable
	private static int[] scanForwardForChar(
			@Nonnull List<String> lines,
			int startLine,
			int startCol,
			char target,
			boolean allowComments
	) {
		var inBlockComment = false;
		for (var i = startLine; i < lines.size(); ++i) {
			final var text = lines.get(i);
			final var from = i == startLine ? startCol : 0;
			for (var j = from; j < text.length(); ++j) {
				final var c = text.charAt(j);
				if (inBlockComment) {
					if (c == '*' && j + 1 < text.length() && text.charAt(j + 1) == '/') {
						inBlockComment = false;
						++j;
					}
					continue;
				}
				if (Character.isWhitespace(c))
					continue;
				if (c == '/' && j + 1 < text.length()) {
					final var next = text.charAt(j + 1);
					if (next == '/') {
						if (!allowComments)
							return null;
						break;
					}
					if (next == '*') {
						if (!allowComments)
							return null;
						inBlockComment = true;
						++j;
						continue;
					}
				}
				if (c == target)
					return new int[]{i, j};
				return null;
			}
		}
		return null;
	}

	/**
	 * Splits {@code argsText} at top-level commas, respecting parens, brackets, braces, and
	 * string/char/text-block/comment content (commas inside those don't split). Commas are located
	 * on the masked text so literal and comment content is ignored, and the arg substrings are
	 * sliced from the original so that content is preserved verbatim. Returns trimmed arg substrings.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> splitTopLevelArgs(@Nonnull String argsText) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(argsText, LexerState.NONE);
		final var result = new ArrayList<String>();
		var depth = 0;
		var start = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '[' || c == '{')
				++depth;
			else if (c == ')' || c == ']' || c == '}')
				--depth;
			else if (c == ',' && depth == 0) {
				result.add(argsText.substring(start, i).strip());
				start = i + 1;
			}
		}
		result.add(argsText.substring(start).strip());
		return result;
	}

	/**
	 * Returns {@code line} with {@code //} line comments and same-line
	 * {@code /* ... *}{@code /} block comments replaced with spaces, preserving
	 * every character's column index. Respects string and char-literal state so
	 * a {@code //} inside a string literal is not treated as a comment.
	 *
	 * <p>Returns an empty string (signalling "treat this line as having no
	 * usable code") when the line contains any of the following constructs
	 * whose semantics our line-local scanner cannot reason about safely:</p>
	 * <ul>
	 *     <li>A source-level Unicode escape ({@code}-style), which the
	 *         Java compiler preprocesses before tokenization (JLS 3.3) but we
	 *         don't, so our literal/comment boundaries can diverge from the
	 *         compiler's view.</li>
	 *     <li>A text-block delimiter ({@code """}), which can span multiple
	 *         lines and whose interior is not tracked by this single-line
	 *         scanner.</li>
	 *     <li>A stray closing {@code *}{@code /} (one that appears before any
	 *         opening {@code /*} on the same line), indicating the line is the
	 *         middle or end of a multi-line block comment that began earlier.
	 *         Block-comment state is not tracked across lines, so the safest
	 *         response is to treat the whole line as unusable.</li>
	 * </ul>
	 * <p>An unterminated same-line block comment (opens with {@code /*} but
	 * doesn't close on the same line) is treated as comment-to-end-of-line.</p>
	 */
	@CheckReturnValue
	@Nonnull
	private static String stripLineComment(@Nonnull String line) {
		for (var i = 0; i < line.length() - 1; ++i) {
			if (line.charAt(i) == '\\' && line.charAt(i + 1) == 'u')
				return "";
		}
		if (line.contains("\"\"\""))
			return "";
		// Detect a stray closing `*/` (one that appears before any opening `/*` on
		// the same line, outside string/char literals) by scanning forward with
		// literal state tracking. `indexOf` would false-positive on `*/` inside a
		// string literal like `"*/"`, producing spurious bails.
		{
			var inString = false;
			var inChar = false;
			for (var i = 0; i + 1 < line.length(); ++i) {
				final var c = line.charAt(i);
				if (inString) {
					if (c == '\\' && i + 1 < line.length()) {
						++i;
						continue;
					}
					if (c == '"')
						inString = false;
					continue;
				}
				if (inChar) {
					if (c == '\\' && i + 1 < line.length()) {
						++i;
						continue;
					}
					if (c == '\'')
						inChar = false;
					continue;
				}
				if (c == '*' && line.charAt(i + 1) == '/')
					return "";
				if (c == '/' && line.charAt(i + 1) == '*')
					break;
				if (c == '"')
					inString = true;
				else if (c == '\'')
					inChar = true;
			}
		}

		final var sb = new StringBuilder(line.length());
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					sb.append("  ");
					++i;
				}
				else
					sb.append(' ');
				continue;
			}
			if (inString) {
				sb.append(c);
				if (c == '\\' && i + 1 < line.length()) {
					sb.append(line.charAt(i + 1));
					++i;
				}
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				sb.append(c);
				if (c == '\\' && i + 1 < line.length()) {
					sb.append(line.charAt(i + 1));
					++i;
				}
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < line.length()) {
				final var next = line.charAt(i + 1);
				if (next == '/') {
					while (sb.length() < line.length())
						sb.append(' ');
					return sb.toString();
				}
				if (next == '*') {
					inBlockComment = true;
					sb.append("  ");
					++i;
					continue;
				}
			}
			sb.append(c);
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
		}
		return sb.toString();
	}

	/**
	 * Tries the {@code assertTrue/assertFalse(!x)} -> {@code assertFalse/assertTrue(x)}
	 * rewrite at {@code lines[lineIndex]}. Returns null if the call shape isn't
	 * recognized or no argument has a leading {@code !}.
	 */
	@CheckReturnValue
	@Nullable
	private static CallFix tryFixNegationSpan(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String methodName,
			@Nonnull String opposite
	) {
		final var span = findCallSpan(lines, lineIndex, column, methodName);
		if (span == null)
			return null;
		final var rewritten = rewriteNegationArgs(span.argsText(), opposite);
		if (rewritten == null)
			return null;
		return buildCallFix(lines, lineIndex, span, methodName, rewritten);
	}

	/**
	 * Tries the {@code assertTrue/assertFalse(... x instanceof Y ...)} rewrite at
	 * {@code lines[lineIndex]}. Returns null if the call shape isn't recognized or the
	 * rewrite isn't applicable (e.g. the inner expression doesn't contain instanceof,
	 * has a generic type argument, or a pattern binding the rewrite can't preserve).
	 */
	@CheckReturnValue
	@Nullable
	private static CallFix tryFixSpan(
			@Nonnull List<String> lines,
			int lineIndex,
			int column,
			@Nonnull String methodName,
			@Nonnull String positive,
			@Nonnull String negative
	) {
		final var span = findCallSpan(lines, lineIndex, column, methodName);
		if (span == null)
			return null;
		final var rewritten = rewriteArgs(span.argsText(), methodName, positive, negative);
		if (rewritten == null)
			return null;
		return buildCallFix(lines, lineIndex, span, methodName, rewritten);
	}

	/**
	 * Iteratively strips wrapping {@code !(...)} negations from a stripped arg,
	 * returning the innermost content along with the parity (odd parity = effectively
	 * negated). Returns null if no leading negation is present or if the wrap shape
	 * isn't matched (e.g. {@code !x} with no parens, invalid for instanceof anyway).
	 * Multi-level cancellation: {@code !!(x instanceof Y)} returns
	 * ({@code "x instanceof Y"}, false).
	 */
	@CheckReturnValue
	@Nullable
	private static NegationResult unwrapNegation(@Nonnull String stripped) {
		var current = stripped;
		var levels = 0;
		while (current.startsWith("!")) {
			++levels;
			current = current.substring(1).strip();
		}
		if (levels == 0)
			return null;
		if (!current.startsWith("(") || !current.endsWith(")"))
			return null;
		// findClosingParenInLine treats `//` as a line comment that runs to end-of-string,
		// which is wrong for multi-line input (the comment really ends at the next \n). Bail
		// rather than risk a mismatched-paren match.
		if (current.indexOf('\n') >= 0)
			return null;
		final var closeIdx = LambdaCallParser.findClosingParenInLine(current, 0);
		if (closeIdx != current.length() - 1)
			return null;
		return new NegationResult(current.substring(1, closeIdx).strip(), levels % 2 == 1);
	}

	/**
	 * Recursively strips matched outer parens from a stripped expression. {@code (x)}
	 * becomes {@code x}; {@code ((x))} becomes {@code x}. Parens that are NOT a
	 * matched outer wrap (e.g. {@code (a) && (b)} where the first {@code (} doesn't
	 * pair with the last {@code )}) are left intact.
	 */
	@CheckReturnValue
	@Nonnull
	private static String unwrapOuterParens(@Nonnull String s) {
		var current = s.strip();
		// findClosingParenInLine breaks permanently on `//` even in multi-line strings, so
		// bail out for multi-line input to avoid mis-matched-paren stripping.
		if (current.indexOf('\n') >= 0)
			return current;
		while (current.startsWith("(") && current.endsWith(")")) {
			final var closeIdx = LambdaCallParser.findClosingParenInLine(current, 0);
			if (closeIdx != current.length() - 1)
				return current;
			current = current.substring(1, closeIdx).strip();
		}
		return current;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		// try the instanceof rewrite first because it's more specific (one assertion replaces
		// what would otherwise be two rounds of negation stripping). When it matches but the
		// target method doesn't exist under the call's framework, drop it and fall back to
		// negation so the user still gets a partial improvement (e.g.
		// `Assert.assertTrue(!(x instanceof Y))` becomes `Assert.assertFalse(x instanceof Y)`
		// under JUnit 4, where `assertInstanceOf` is not available).
		var attempt = tryFixSpan(lines, lineIndex, column, "assertTrue", "assertInstanceOf", "assertNotInstanceOf");
		if (attempt == null)
			attempt = tryFixSpan(lines, lineIndex, column, "assertFalse", "assertNotInstanceOf", "assertInstanceOf");
		if (attempt != null) {
			if (!attempt.qualified) {
				if (!hasAssertionsImport(lines) || hasJunit4AssertImport(lines))
					attempt = null;
			}
			else if (!ASSERTIONS_CLASS.equals(attempt.qualifierSimpleName)) {
				// Only JUnit 5 `Assertions` has `assertInstanceOf` / `assertNotInstanceOf`.
				// JUnit 4 `Assert` doesn't, so fall back to the negation rewrite (assertTrue
				// <-> assertFalse with the instanceof retained). That rewrite is safe because
				// both Assert and Assertions have assertTrue/assertFalse. The defensive skip
				// below rejects any qualifier that isn't one of those two classes.
				attempt = null;
			}
		}

		var isNegation = false;
		if (attempt == null) {
			attempt = tryFixNegationSpan(lines, lineIndex, column, "assertTrue", "assertFalse");
			if (attempt == null)
				attempt = tryFixNegationSpan(lines, lineIndex, column, "assertFalse", "assertTrue");
			if (attempt != null)
				isNegation = true;
		}
		if (attempt == null)
			return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);

		if (attempt.qualified && !isJunitAssertClass(attempt.qualifierSimpleName)) {
			// Qualified call to an unknown receiver (custom helper, chained expression,
			// parenthesized receiver). We can't prove the replacement method exists on
			// the same receiver, so skip rather than guess.
			return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
		}

		if (isNegation && !attempt.qualified) {
			// Both Assert (JUnit 4) and Assertions (JUnit 5) have assertTrue/assertFalse, so
			// the negation rewrite works under either framework. Skip only when we can't infer
			// a class to import from, or when both frameworks are imported simultaneously
			// (the swap would change which class resolves the unqualified call).
			if (!hasStaticImportOfClass(lines, fqn -> isJunitAssertClass(AstUtil.simpleName(fqn))))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
			if (hasAssertionsImport(lines) && hasJunit4AssertImport(lines))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
		}

		final var imports = new TreeSet<String>();
		// qualified calls carry the source class on the rewritten line, so no static import
		// is needed for the rewrite to resolve.
		if (!attempt.qualified) {
			if (isNegation)
				addOppositeAssertImport(lines, attempt.replacementMethod, imports);
			else
				addAssertImport(lines, attempt.replacementMethod, imports);
		}
		if (imports.isEmpty())
			return new FixResult(attempt.startLine, attempt.endLine, attempt.replacement);
		return new FixResult(attempt.startLine, attempt.endLine, attempt.replacement, imports);
	}
}