package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferExactAssertionFixer implements CheckstyleFixer {
	private record CallFix(int startLine, int endLine, @Nonnull List<String> replacement,
	                       @Nonnull String replacementMethod, boolean qualified) {
	}

	private record ImportLine(@Nonnull String fqn, boolean staticImport, boolean wildcard) {
	}

	private record NegationResult(@Nonnull String content, boolean negated) {
	}

	private record RewrittenArgs(@Nonnull String replacementMethod, @Nonnull String newArgs) {
	}

	/**
	 * Matches an {@code import [static] FQN[.*];} line tolerantly: accepts arbitrary
	 * whitespace (including tabs) between tokens and around the trailing semicolon.
	 * Group 1 is non-null when the import is static. Group 2 is the FQN, possibly
	 * ending in {@code .*} for wildcard imports.
	 */
	private static final Pattern IMPORT_PATTERN = Pattern.compile(
			"^\\s*import\\s+(static\\s+)?([\\w.$*]+)\\s*;\\s*$"
	);

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
	 * are also rejected by the method-name allowlist.
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
			if (parsed == null || !parsed.staticImport)
				continue;

			if (parsed.wildcard) {
				if (isJunit5AssertionsClass(parsed.fqn))
					return;
				continue;
			}

			final var lastDot = parsed.fqn.lastIndexOf('.');
			if (lastDot < 0)
				continue;
			final var methodName = parsed.fqn.substring(lastDot + 1);
			if (!JUNIT_ASSERT_METHODS.contains(methodName))
				continue;
			final var classFqn = parsed.fqn.substring(0, lastDot);
			if (!isJunit5AssertionsClass(classFqn))
				continue;
			imports.add("static " + classFqn + "." + replacementMethod);
			return;
		}
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
	 * {@link #findMethodNameEnd}. Used to detect ambiguous slow-path inputs where
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
	 * Finds the index of the {@code (} that opens a call to {@code methodName} on
	 * {@code line}, requiring the character before {@code methodName} to NOT be a Java
	 * identifier part (so {@code customAssertTrue(} is not matched), and skipping
	 * occurrences inside string/char literals, line comments, and block comments.
	 * Returns -1 if no call is found.
	 */
	@CheckReturnValue
	private static int findMethodCallOpen(@Nonnull String line, @Nonnull String methodName) {
		final var pattern = methodName + "(";
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
		for (var i = 0; i + pattern.length() <= line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < line.length()) {
				final var next = line.charAt(i + 1);
				if (next == '/')
					return -1;
				if (next == '*') {
					inBlockComment = true;
					++i;
					continue;
				}
			}
			if (c == '"') {
				inString = true;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				continue;
			}
			if (line.startsWith(pattern, i)
					&& (i == 0 || !Character.isJavaIdentifierPart(line.charAt(i - 1))))
				return i + methodName.length();
		}
		return -1;
	}

	/**
	 * Finds the end-of-name index for {@code methodName} in {@code line}, without
	 * requiring a {@code (} to follow. Returns the index just past the last char of
	 * the name, or -1 if not found. Skips occurrences inside string/char literals,
	 * line comments, and block comments. Rejects identifier-part neighbors (so
	 * {@code customAssertTrue} / {@code assertTrueX} aren't matched).
	 */
	@CheckReturnValue
	private static int findMethodNameEnd(@Nonnull String line, @Nonnull String methodName) {
		return findMethodNameEndFrom(line, methodName, 0);
	}

	/**
	 * Same as {@link #findMethodNameEnd} but starts scanning from {@code fromIndex}.
	 * Used to count multiple occurrences for ambiguity detection.
	 */
	@CheckReturnValue
	private static int findMethodNameEndFrom(@Nonnull String line, @Nonnull String methodName, int fromIndex) {
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
		for (var i = fromIndex; i + methodName.length() <= line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
			if (inString) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < line.length())
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '/' && i + 1 < line.length()) {
				final var next = line.charAt(i + 1);
				if (next == '/')
					return -1;
				if (next == '*') {
					inBlockComment = true;
					++i;
					continue;
				}
			}
			if (c == '"') {
				inString = true;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				continue;
			}
			if (line.startsWith(methodName, i)
					&& (i == 0 || !Character.isJavaIdentifierPart(line.charAt(i - 1)))
					&& (i + methodName.length() == line.length()
							|| !Character.isJavaIdentifierPart(line.charAt(i + methodName.length()))))
				return i + methodName.length();
		}
		return -1;
	}

	/**
	 * Returns the index of {@code " instanceof "} in {@code text} that sits at
	 * paren/bracket/brace depth 0, outside any string or char literal. Returns -1
	 * if no top-level occurrence exists. Unlike a plain
	 * {@link LambdaCallParser#indexOfStructural} this also respects paren depth so a
	 * sub-expression like {@code "...(x instanceof Y)..."} is correctly skipped.
	 */
	@CheckReturnValue
	private static int findTopLevelInstanceof(@Nonnull String text) {
		final var needle = " instanceof ";
		var depth = 0;
		var inString = false;
		var inChar = false;
		for (var i = 0; i + needle.length() <= text.length(); ++i) {
			final var c = text.charAt(i);
			if (inString) {
				if (c == '\\' && i + 1 < text.length())
					++i;
				else if (c == '"')
					inString = false;
				continue;
			}
			if (inChar) {
				if (c == '\\' && i + 1 < text.length())
					++i;
				else if (c == '\'')
					inChar = false;
				continue;
			}
			if (c == '"') {
				inString = true;
				continue;
			}
			if (c == '\'') {
				inChar = true;
				continue;
			}
			if (c == '(' || c == '[' || c == '{') {
				++depth;
				continue;
			}
			if (c == ')' || c == ']' || c == '}') {
				--depth;
				continue;
			}
			if (depth == 0 && text.startsWith(needle, i))
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
				fqn -> {
					final var dot = fqn.lastIndexOf('.');
					final var simple = dot < 0 ? fqn : fqn.substring(dot + 1);
					return "Assert".equals(simple);
				}
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
			if (parsed == null || !parsed.staticImport)
				continue;
			var classFqn = parsed.fqn;
			if (!parsed.wildcard) {
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

	/**
	 * Returns true if {@code fqn} names the JUnit 5 {@code Assertions} class. Matches
	 * by class name only (last segment). Accepts both {@code org.junit.jupiter.api.Assertions}
	 * and any custom subclass shadowing the same simple name. JUnit 4's {@code Assert}
	 * (singular) does not match.
	 */
	@CheckReturnValue
	private static boolean isJunit5AssertionsClass(@Nonnull String fqn) {
		final var dot = fqn.lastIndexOf('.');
		final var simple = dot < 0 ? fqn : fqn.substring(dot + 1);
		return "Assertions".equals(simple);
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
	 * Parses an {@code import [static] FQN[.*];} line, tolerantly handling whitespace
	 * around tokens and the trailing semicolon. Returns null if the line isn't an
	 * import statement. The returned {@code fqn} has any trailing {@code .*} stripped;
	 * {@code wildcard} indicates whether it was present.
	 */
	@CheckReturnValue
	@Nullable
	private static ImportLine parseImport(@Nonnull String line) {
		final var matcher = IMPORT_PATTERN.matcher(LambdaCallParser.stripComment(line));
		if (!matcher.matches())
			return null;
		final var staticImport = matcher.group(1) != null;
		var fqn = matcher.group(2);
		final var wildcard = fqn.endsWith(".*");
		if (wildcard)
			fqn = fqn.substring(0, fqn.length() - 2);
		return new ImportLine(fqn, staticImport, wildcard);
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
		final var typePart = content.substring(instOffset + " instanceof ".length()).strip();

		if (typePart.contains(" ") || typePart.contains("<"))
			return null;
		if (lhs.isEmpty() || typePart.isEmpty())
			return null;

		final var replacement = negated ? negative : positive;
		final var newArgs = new StringBuilder();
		newArgs.append(typePart).append(".class, ").append(lhs);
		for (var i = 0; i < args.size(); ++i) {
			if (i != instArgIdx)
				newArgs.append(", ").append(args.get(i).strip());
		}
		return new RewrittenArgs(replacement, newArgs.toString());
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
	 * Splits {@code argsText} at top-level commas, respecting parens, brackets, braces,
	 * and string/char literals. Returns trimmed arg substrings.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> splitTopLevelArgs(@Nonnull String argsText) {
		final var result = new ArrayList<String>();
		var depth = 0;
		var inString = false;
		var inChar = false;
		var start = 0;
		for (var i = 0; i < argsText.length(); ++i) {
			final var c = argsText.charAt(i);
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
			if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(' || c == '[' || c == '{')
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
	 * Tries to fix a call to {@code methodName} starting at {@code lines[lineIndex]}.
	 * Handles both single-line and multi-line calls (open paren on its own line, args
	 * across multiple lines, closing paren and/or semicolon on separate lines).
	 * Returns null if the call shape isn't recognized or the rewrite isn't applicable.
	 */
	@CheckReturnValue
	@Nullable
	private static CallFix tryFixSpan(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String methodName,
			@Nonnull String positive,
			@Nonnull String negative
	) {
		final var firstLine = lines.get(lineIndex);

		// fast path for single-line calls: `(` immediately after the method name.
		// fall back to a name-only search for multi-line / paren-on-own-line shapes.
		var openLine = lineIndex;
		var openCol = findMethodCallOpen(firstLine, methodName);
		final int methodNameEnd;
		if (openCol >= 0) {
			// `name(` adjacency is already verified by findMethodCallOpen; the name
			// ends exactly at openCol. Using openCol directly here (rather than the
			// first match from findMethodNameEnd) avoids corrupting the prefix when
			// the line contains an earlier non-call identifier with the same name
			// (e.g. `String msg = assertTrue + " x"; Assertions.assertTrue(o iof Y);`).
			methodNameEnd = openCol;
		}
		else {
			// `(` may be preceded by whitespace or a comment, or on a later line.
			// Bail when the first line has more than one standalone occurrence of the
			// method name: a `name<ws>(` shape earlier on the line plus a later
			// `name<newline>(` shape would make the slow path target the wrong call.
			if (countMethodNameOccurrences(firstLine, methodName) > 1)
				return null;
			final var nameEnd = findMethodNameEnd(firstLine, methodName);
			if (nameEnd < 0)
				return null;
			final var paren = scanForwardForChar(lines, lineIndex, nameEnd, '(', true);
			if (paren == null)
				return null;
			methodNameEnd = nameEnd;
			openLine = paren[0];
			openCol = paren[1];
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
		final var semiLine = semiLoc[0];
		final var semiCol = semiLoc[1];

		final var argsText = joinArgs(lines, openLine, openCol, closeLine, closeCol);
		final var rewritten = rewriteArgs(argsText, methodName, positive, negative);
		if (rewritten == null)
			return null;

		final var methodStart = methodNameEnd - methodName.length();
		final var qualified = methodStart > 0 && firstLine.charAt(methodStart - 1) == '.';
		final var prefix = firstLine.substring(0, methodStart);
		final var suffix = lines.get(semiLine).substring(semiCol + 1);
		final var combined = prefix + rewritten.replacementMethod + "(" + rewritten.newArgs + ");" + suffix;
		// `\n` inside the rewritten body indicates a text-block arg crossed lines.
		final var resultLines = List.of(combined.split("\n", -1));
		return new CallFix(lineIndex, semiLine, resultLines, rewritten.replacementMethod, qualified);
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
		// strip leading `!`s. They're left-associative, so `!!x` parses as `!(!x)`. The
		// parens wrap the operand only at the innermost level (e.g. `!!(x instanceof Y)`).
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
		var attempt = tryFixSpan(lines, lineIndex, "assertTrue", "assertInstanceOf", "assertNotInstanceOf");
		if (attempt == null)
			attempt = tryFixSpan(lines, lineIndex, "assertFalse", "assertNotInstanceOf", "assertInstanceOf");
		if (attempt == null)
			return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);

		// guard: rewriting to assertInstanceOf only makes sense when an Assertions class
		// is reachable. Qualified calls (`Foo.assertTrue(...)`) already name the source
		// class on the rewritten line, so the existing qualifier suffices. Unqualified
		// calls require either:
		//   (a) a JUnit 5 Assertions class to be imported (so the rewritten unqualified
		//       call resolves), AND
		//   (b) no JUnit 4 Assert class also imported (otherwise the original `assertTrue`
		//       resolves through JUnit 4 and a rewrite would silently swap frameworks).
		if (!attempt.qualified) {
			if (!hasAssertionsImport(lines))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
			if (hasJunit4AssertImport(lines))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
		}

		final var imports = new TreeSet<String>();
		addAssertImport(lines, attempt.replacementMethod, imports);
		if (imports.isEmpty())
			return new FixResult(attempt.startLine, attempt.endLine, attempt.replacement);
		return new FixResult(attempt.startLine, attempt.endLine, attempt.replacement, imports);
	}
}