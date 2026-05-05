package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferExactAssertionFixer implements CheckstyleFixer {
	private record FixOutput(@Nonnull String line, @Nonnull String replacementMethod, boolean qualified) {
	}

	private record ImportLine(@Nonnull String fqn, boolean staticImport, boolean wildcard) {
	}

	private record NegationResult(@Nonnull String content, boolean negated) {
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
	 * {@code Assertions} class only — JUnit 4's {@code Assert} doesn't have
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
	 * Finds the index of the {@code (} that opens a call to {@code methodName} on
	 * {@code line}, requiring the character before {@code methodName} to NOT be a Java
	 * identifier part (so {@code customAssertTrue(} is not matched), and skipping
	 * occurrences inside string/char literals. Returns -1 if no call is found.
	 */
	@CheckReturnValue
	private static int findMethodCallOpen(@Nonnull String line, @Nonnull String methodName) {
		final var pattern = methodName + "(";
		var inString = false;
		var inChar = false;
		for (var i = 0; i + pattern.length() <= line.length(); ++i) {
			final var c = line.charAt(i);
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
	 * Returns true if any line in {@code lines} imports an {@code Assertions} class —
	 * static or non-static, single or wildcard. The fixer's output references
	 * {@code assertInstanceOf} / {@code assertNotInstanceOf}, which only exist on
	 * JUnit 5's {@code Assertions} (or a user-defined subclass with the same simple
	 * name). When no such import is present, the fixer must skip the rewrite or it
	 * would produce non-compiling code.
	 */
	@CheckReturnValue
	private static boolean hasAssertionsImport(@Nonnull List<String> lines) {
		return hasImportedClassNamed(lines, PreferExactAssertionFixer::isJunit5AssertionsClass);
	}

	/**
	 * Generic helper: walks {@code lines}, parses each as an {@code import} statement
	 * (whitespace-tolerant), and applies {@code classMatcher} to the imported class FQN
	 * (the method-name suffix is stripped for static single imports; wildcard exports are
	 * stripped). Returns true on first match.
	 */
	@CheckReturnValue
	private static boolean hasImportedClassNamed(
			@Nonnull List<String> lines,
			@Nonnull java.util.function.Predicate<String> classMatcher
	) {
		for (var existing : lines) {
			final var parsed = parseImport(existing);
			if (parsed == null)
				continue;
			// for non-wildcard static imports, strip the trailing method name to get the class FQN.
			// non-static imports and wildcard imports already name the class directly.
			var classFqn = parsed.fqn;
			if (!parsed.wildcard && parsed.staticImport) {
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
	 * Returns true if any line in {@code lines} imports a JUnit 4 {@code Assert} class
	 * (simple-name match). Used to detect JUnit 4 + JUnit 5 mixed-import files where
	 * an unqualified {@code assertTrue} call would resolve via JUnit 4 today.
	 */
	@CheckReturnValue
	private static boolean hasJunit4AssertImport(@Nonnull List<String> lines) {
		return hasImportedClassNamed(
				lines,
				fqn -> {
					final var dot = fqn.lastIndexOf('.');
					final var simple = dot < 0 ? fqn : fqn.substring(dot + 1);
					return "Assert".equals(simple);
				}
		);
	}

	/**
	 * Returns true if {@code fqn} names the JUnit 5 {@code Assertions} class. Matches
	 * by class name only (last segment) — accepts both {@code org.junit.jupiter.api.Assertions}
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
	 * Parses an {@code import [static] FQN[.*];} line, tolerantly handling whitespace
	 * around tokens and the trailing semicolon. Returns null if the line isn't an
	 * import statement. The returned {@code fqn} has any trailing {@code .*} stripped;
	 * {@code wildcard} indicates whether it was present.
	 */
	@CheckReturnValue
	@Nullable
	private static ImportLine parseImport(@Nonnull String line) {
		final var matcher = IMPORT_PATTERN.matcher(line);
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
	 * Attempts to convert {@code methodName(... x instanceof Y ...)} to
	 * {@code replacement(Y.class, x, ...)}. Also handles single-negation wrapping
	 * ({@code !(x instanceof Y)}); when present, the polarity flips: an
	 * {@code assertTrue(!(x instanceof Y))} becomes {@code assertNotInstanceOf(Y.class, x)}.
	 * Returns the rewritten line or null if the expected shape isn't found. Skips
	 * pattern-binding ({@code instanceof Y y}) and generic types ({@code instanceof List<X>}).
	 */
	@CheckReturnValue
	@Nullable
	private static FixOutput tryFix(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String positive,
			@Nonnull String negative
	) {
		final var openIdx = findMethodCallOpen(line, methodName);
		if (openIdx < 0)
			return null;

		final var closeIdx = LambdaCallParser.findClosingParenInLine(line, openIdx);
		if (closeIdx < 0)
			return null;

		final var argsText = line.substring(openIdx + 1, closeIdx);
		// text blocks contain commas/quotes that the simple splitter can't track
		if (argsText.contains("\"\"\""))
			return null;

		final var args = splitTopLevelArgs(argsText);
		if (args.size() < 1 || args.size() > 2)
			return null;

		// find the arg containing top-level " instanceof ", possibly through a `!(...)` wrap
		var instArgIdx = -1;
		String content = null;
		var negated = false;
		for (var i = 0; i < args.size(); ++i) {
			final var arg = args.get(i);
			final var unwrapped = unwrapNegation(arg.strip());
			final var candidate = unwrapped != null ? unwrapped.content() : arg;
			if (findTopLevelInstanceof(candidate) >= 0) {
				instArgIdx = i;
				content = candidate;
				negated = unwrapped != null && unwrapped.negated();
				break;
			}
		}
		if (instArgIdx < 0 || content == null)
			return null;

		final var instOffset = findTopLevelInstanceof(content);
		final var lhs = content.substring(0, instOffset).strip();
		final var typePart = content.substring(instOffset + " instanceof ".length()).strip();

		// pattern-binding (`instanceof Y y`) — skip; the binding semantics aren't preserved.
		// generic types (`instanceof List<X>`) — skip; generics can't appear in a .class literal.
		if (typePart.contains(" ") || typePart.contains("<"))
			return null;
		if (lhs.isEmpty() || typePart.isEmpty())
			return null;

		final var replacement = negated ? negative : positive;

		final var newArgs = new StringBuilder();
		newArgs.append(typePart).append(".class, ").append(lhs);
		for (var i = 0; i < args.size(); ++i) {
			if (i != instArgIdx)
				newArgs.append(", ").append(args.get(i));
		}

		final var methodStart = openIdx - methodName.length();
		// qualified iff the method name is preceded by `.` (e.g. `Foo.assertTrue(...)`).
		// Computed from the original-line context to avoid string-literal spoofs in the
		// rewritten output that can occur with `String.contains` heuristics.
		final var qualified = methodStart > 0 && line.charAt(methodStart - 1) == '.';
		final var newLine = line.substring(0, methodStart)
				+ replacement
				+ "(" + newArgs + ")"
				+ line.substring(closeIdx + 1);
		return new FixOutput(newLine, replacement, qualified);
	}

	/**
	 * Iteratively strips wrapping {@code !(...)} negations from a stripped arg,
	 * returning the innermost content along with the parity (odd parity = effectively
	 * negated). Returns null if no leading negation is present or if the wrap shape
	 * isn't matched (e.g. {@code !x} with no parens — invalid for instanceof anyway).
	 * Multi-level cancellation: {@code !!(x instanceof Y)} returns
	 * ({@code "x instanceof Y"}, false).
	 */
	@CheckReturnValue
	@Nullable
	private static NegationResult unwrapNegation(@Nonnull String stripped) {
		// strip leading `!`s — they're left-associative, so `!!x` parses as `!(!x)`. The
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

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		var output = tryFix(line, "assertTrue", "assertInstanceOf", "assertNotInstanceOf");
		if (output == null)
			output = tryFix(line, "assertFalse", "assertNotInstanceOf", "assertInstanceOf");
		if (output == null)
			return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);

		// guard: rewriting to assertInstanceOf only makes sense when an Assertions class
		// is reachable. Qualified calls (`Foo.assertTrue(...)`) already name the source
		// class on the rewritten line, so the existing qualifier suffices. Unqualified
		// calls require either:
		//   (a) a JUnit 5 Assertions class to be imported (so the rewritten unqualified
		//       call resolves), AND
		//   (b) no JUnit 4 Assert class also imported (otherwise the original `assertTrue`
		//       resolves through JUnit 4 and a rewrite would silently swap frameworks).
		if (!output.qualified) {
			if (!hasAssertionsImport(lines))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
			if (hasJunit4AssertImport(lines))
				return new SkipResult(SkipMessages.PREFER_ASSERT_SKIP);
		}

		final var imports = new TreeSet<String>();
		addAssertImport(lines, output.replacementMethod, imports);
		if (imports.isEmpty())
			return new FixResult(lineIndex, lineIndex, List.of(output.line));
		return new FixResult(lineIndex, lineIndex, List.of(output.line), imports);
	}
}