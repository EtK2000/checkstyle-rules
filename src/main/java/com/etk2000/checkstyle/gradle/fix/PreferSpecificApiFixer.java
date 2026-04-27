package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferSpecificApiFixer implements CheckstyleFixer {
	private static final String[][] ASSERT_RULES = {
			// {methodName, literal, replacement}
			{"assertEquals", "false", "assertFalse"},
			{"assertEquals", "null", "assertNull"},
			{"assertEquals", "true", "assertTrue"},
			{"assertNotEquals", "false", "assertTrue"},
			{"assertNotEquals", "null", "assertNotNull"},
			{"assertNotEquals", "true", "assertFalse"},
			{"assertNotSame", "null", "assertNotNull"},
			{"assertSame", "null", "assertNull"}
	};

	/**
	 * Finds the assertion class prefix from existing static imports
	 * (e.g. {@code org.junit.Assert} or {@code org.junit.jupiter.api.Assertions})
	 * and adds a static import for the replacement method. Skips if a wildcard
	 * import already covers the class.
	 */
	private static void addAssertImport(
			@Nonnull List<String> lines,
			@Nonnull String replacementMethod,
			@Nonnull Set<String> imports
	) {
		for (var existing : lines) {
			if (!existing.startsWith("import static "))
				continue;
			final var fqn = existing.substring(14, existing.length() - 1);

			if (fqn.endsWith(".*"))
				return;

			final var lastDot = fqn.lastIndexOf('.');
			if (lastDot < 0)
				continue;
			final var methodName = fqn.substring(lastDot + 1);
			if (methodName.startsWith("assert") || methodName.startsWith("fail")) {
				imports.add("static " + fqn.substring(0, lastDot) + "." + replacementMethod);
				return;
			}
		}
	}

	/**
	 * Scans backwards from {@code dotIdx} to find the start of a simple receiver
	 * expression (identifiers and dots). Returns -1 if the receiver contains
	 * complex syntax (parens, brackets, etc.) that would make insertion unsafe.
	 */
	@CheckReturnValue
	private static int findReceiverStart(@Nonnull String line, int dotIdx) {
		var pos = dotIdx - 1;
		while (pos >= 0) {
			final var ch = line.charAt(pos);
			if (Character.isJavaIdentifierPart(ch) || ch == '.')
				--pos;
			else
				break;
		}
		final var start = pos + 1;
		if (start >= dotIdx || line.charAt(start) == '.')
			return -1;
		return start;
	}

	/**
	 * Finds an occurrence of {@code prefix} (a literal like {@code "0 == "}) followed by
	 * {@code suffix} (a method chain like {@code ".strip().length()"}) where:
	 * (a) the character before {@code prefix} is not a number/identifier continuation
	 *     (so the leading digit is standalone, not part of {@code 100} or similar), and
	 * (b) the text between {@code prefix} and {@code suffix} is a simple receiver
	 *     (identifier characters and dots only).
	 * Iterates through occurrences until a valid one is found. Returns
	 * {@code [prefixIdx, suffixIdx]} or {@code null}.
	 */
	@CheckReturnValue
	@Nullable
	private static int[] findReversedMatch(@Nonnull String line, @Nonnull String prefix, @Nonnull String suffix) {
		var searchFrom = 0;
		while (searchFrom < line.length()) {
			final var idx = line.indexOf(prefix, searchFrom);
			if (idx < 0)
				return null;
			if (idx > 0) {
				final var prev = line.charAt(idx - 1);
				if (Character.isJavaIdentifierPart(prev) || prev == '.') {
					searchFrom = idx + 1;
					continue;
				}
			}
			final var methodIdx = line.indexOf(suffix, idx + prefix.length());
			if (methodIdx < 0)
				return null;
			final var between = line.substring(idx + prefix.length(), methodIdx);
			if (!isSimpleReceiver(between)) {
				searchFrom = idx + 1;
				continue;
			}
			return new int[]{idx, methodIdx};
		}
		return null;
	}

	/**
	 * Like {@code line.indexOf(pattern, from)} but, when {@code pattern} ends with a digit,
	 * skips matches where the next character could continue the trailing digit as a numeric
	 * literal, i.e. any Java identifier part (digits, underscore, hex/binary prefix letters,
	 * type suffix letters, exponent letters) or a decimal point. This prevents matching
	 * {@code "< 1"} inside {@code "< 10"}, {@code "< 1L"}, or {@code "< 1.5"}.
	 * Iterates through occurrences until a valid one is found.
	 */
	@CheckReturnValue
	private static int findStandalonePattern(@Nonnull String line, @Nonnull String pattern, int from) {
		if (pattern.isEmpty() || !Character.isDigit(pattern.charAt(pattern.length() - 1)))
			return line.indexOf(pattern, from);
		var idx = line.indexOf(pattern, from);
		while (idx >= 0) {
			final var endPos = idx + pattern.length();
			if (endPos >= line.length())
				return idx;
			final var ch = line.charAt(endPos);
			if (!Character.isJavaIdentifierPart(ch) && ch != '.')
				return idx;
			idx = line.indexOf(pattern, idx + 1);
		}
		return -1;
	}

	/**
	 * {@code Arrays.asList(...)} -> {@code List.of(...)}.
	 * Adds the {@code java.util.List} import.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixArraysAsList(@Nonnull String line, @Nonnull Set<String> imports) {
		final var pattern = "Arrays.asList(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		imports.add("java.util.List");
		return line.substring(0, idx) + "List.of(" + line.substring(idx + pattern.length());
	}

	/**
	 * Simplifies assertion calls with literal arguments.
	 * Handles both literal-first ({@code assertEquals(true, x)})
	 * and literal-last ({@code assertEquals(x, true)}) 2-arg forms.
	 * Adds the static import for the replacement method if the original
	 * uses specific (non-wildcard) static imports.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertion(@Nonnull List<String> lines, @Nonnull String line, @Nonnull Set<String> imports) {
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralFirst(line, rule[0], rule[1], rule[2]);
			if (result != null) {
				addAssertImport(lines, rule[2], imports);
				return result;
			}
		}
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralLast(line, rule[0], rule[1], rule[2]);
			if (result != null) {
				addAssertImport(lines, rule[2], imports);
				return result;
			}
		}
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralMiddle(line, rule[0], rule[1], rule[2]);
			if (result != null) {
				addAssertImport(lines, rule[2], imports);
				return result;
			}
		}
		return null;
	}

	/**
	 * Handles {@code assertEquals(true, x)} -> {@code assertTrue(x)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralFirst(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var pattern = methodName + "(" + literal + ", ";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + replacement + "(" + line.substring(idx + pattern.length());
	}

	/**
	 * Handles {@code assertEquals(x, true)} -> {@code assertTrue(x)}.
	 * Finds the method call and removes the trailing literal argument.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralLast(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var methodStart = line.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var suffix = ", " + literal + ")";
		final var suffixIdx = line.indexOf(suffix, methodStart);
		if (suffixIdx < 0)
			return null;

		return line.substring(0, methodStart) + replacement + "("
				+ line.substring(methodStart + methodName.length() + 1, suffixIdx)
				+ ")" + line.substring(suffixIdx + suffix.length());
	}

	/**
	 * Handles 3-arg forms where the literal is in the middle position:
	 * {@code assertEquals("msg", null, x)} -> {@code assertNull("msg", x)} (JUnit 4),
	 * {@code assertEquals(x, null, "msg")} -> {@code assertNull(x, "msg")} (JUnit 5).
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralMiddle(
			@Nonnull String line,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement
	) {
		final var methodStart = line.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var pattern = ", " + literal + ", ";
		final var patternIdx = line.indexOf(pattern, methodStart);
		if (patternIdx < 0)
			return null;

		return line.substring(0, methodStart) + replacement + "("
				+ line.substring(methodStart + methodName.length() + 1, patternIdx)
				+ ", " + line.substring(patternIdx + pattern.length());
	}

	/**
	 * {@code Collections.emptyList()} -> {@code List.of()},
	 * {@code Collections.singletonList(x)} -> {@code List.of(x)}, etc.
	 * Adds the required import (e.g. {@code java.util.List}) to the given set.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectionsFactory(@Nonnull String line, @Nonnull Set<String> imports) {
		final String[][] replacements = {
				{"Collections.emptyList()", "List.of()"},
				{"Collections.emptyMap()", "Map.of()"},
				{"Collections.emptySet()", "Set.of()"},
				{"Collections.singleton(", "Set.of("},
				{"Collections.singletonList(", "List.of("},
				{"Collections.singletonMap(", "Map.of("},
				{"Collections.unmodifiableList(", "List.copyOf("},
				{"Collections.unmodifiableMap(", "Map.copyOf("},
				{"Collections.unmodifiableSet(", "Set.copyOf("}
		};
		for (var r : replacements) {
			final var idx = line.indexOf(r[0]);
			if (idx >= 0) {
				imports.add("java.util." + r[1].substring(0, r[1].indexOf('.')));
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
			}
		}
		return null;
	}

	/**
	 * {@code Collections.sort(list)} -> {@code list.sort(null)},
	 * {@code Collections.sort(list, cmp)} -> {@code list.sort(cmp)}.
	 * Uses paren-balanced parsing to extract the first argument.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectionsSort(@Nonnull String line) {
		final var pattern = "Collections.sort(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;

		final var argsStart = idx + pattern.length();

		// find the comma separating args (at depth 0) or the closing paren
		var depth = 1;
		var pos = argsStart;
		var commaIdx = -1;
		while (pos < line.length() && depth > 0) {
			final var ch = line.charAt(pos);
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					break;
			}
			else if (ch == ',' && depth == 1 && commaIdx < 0)
				commaIdx = pos;
			else if (ch == '"') {
				++pos;
				while (pos < line.length() && line.charAt(pos) != '"') {
					if (line.charAt(pos) == '\\')
						++pos;
					++pos;
				}
			}
			++pos;
		}
		if (depth != 0)
			return null;

		final var closeParen = pos;
		if (commaIdx >= 0) {
			// 2-arg form: Collections.sort(list, cmp) -> list.sort(cmp)
			final var listArg = line.substring(argsStart, commaIdx).strip();
			final var cmpArg = line.substring(commaIdx + 1, closeParen).strip();
			return line.substring(0, idx) + listArg + ".sort(" + cmpArg + ")"
					+ line.substring(closeParen + 1);
		}

		// 1-arg form: Collections.sort(list) -> list.sort(null)
		final var listArg = line.substring(argsStart, closeParen).strip();
		return line.substring(0, idx) + listArg + ".sort(null)" + line.substring(closeParen + 1);
	}

	/**
	 * {@code .collect(Collectors.toList())} -> {@code .toList()}, and
	 * {@code .collect(Collectors.toUnmodifiableList())} -> {@code .toList()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectToList(@Nonnull String line) {
		for (var collector : new String[]{"toList", "toUnmodifiableList"}) {
			final var pattern = ".collect(Collectors." + collector + "())";
			final var idx = line.indexOf(pattern);
			if (idx >= 0)
				return line.substring(0, idx) + ".toList()" + line.substring(idx + pattern.length());
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixComparisonIsEmpty(@Nonnull String line, @Nonnull String method) {
		// positive simple forms: .length() == 0, .length() <= 0, .length() < 1
		final String[] positivePatterns = {
				method + " == 0",
				method + " <= 0",
				method + " < 1"
		};
		for (var pattern : positivePatterns) {
			final var idx = findStandalonePattern(line, pattern, 0);
			if (idx >= 0)
				return line.substring(0, idx) + ".isEmpty()" + line.substring(idx + pattern.length());
		}

		// positive reversed forms: 0 == expr.length(), 0 >= expr.length(), 1 > expr.length()
		final String[][] reversedPositive = {
				{"0 == ", method},
				{"0 >= ", method},
				{"1 > ", method}
		};
		for (var rev : reversedPositive) {
			final var match = findReversedMatch(line, rev[0], rev[1]);
			if (match != null) {
				final var idx = match[0];
				final var methodIdx = match[1];
				return line.substring(0, idx) + line.substring(idx + rev[0].length(), methodIdx)
						+ ".isEmpty()" + line.substring(methodIdx + rev[1].length());
			}
		}

		// negated simple forms: .length() != 0, .length() > 0, .length() >= 1
		final String[] negatedPatterns = {
				method + " != 0",
				method + " > 0",
				method + " >= 1"
		};
		for (var neg : negatedPatterns) {
			final var idx = findStandalonePattern(line, neg, 0);
			if (idx >= 0) {
				final var receiverStart = findReceiverStart(line, idx);
				if (receiverStart < 0)
					return null;
				if (receiverStart > 0 && line.charAt(receiverStart - 1) == '!') {
					return line.substring(0, receiverStart - 1) + line.substring(receiverStart, idx)
							+ ".isEmpty()" + line.substring(idx + neg.length());
				}
				return line.substring(0, receiverStart) + "!" + line.substring(receiverStart, idx)
						+ ".isEmpty()" + line.substring(idx + neg.length());
			}
		}

		// negated reversed forms: 0 != expr.length(), 0 < expr.length(), 1 <= expr.length()
		final String[][] reversedNegated = {
				{"0 != ", method},
				{"0 < ", method},
				{"1 <= ", method}
		};
		for (var rev : reversedNegated) {
			final var match = findReversedMatch(line, rev[0], rev[1]);
			if (match != null) {
				final var idx = match[0];
				final var methodIdx = match[1];
				final var receiver = line.substring(idx + rev[0].length(), methodIdx);
				return line.substring(0, idx) + "!" + receiver
						+ ".isEmpty()" + line.substring(methodIdx + rev[1].length());
			}
		}

		return null;
	}

	/**
	 * {@code .equals("")} -> {@code .isEmpty()}.
	 * Only matches when the argument is literally {@code ""}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixEqualsEmpty(@Nonnull String line) {
		final var pattern = ".equals(\"\")";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".isEmpty()" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .get(0)} -> {@code .getFirst()},
	 * {@code .remove(0)} -> {@code .removeFirst()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixGetOrRemoveFirst(@Nonnull String line) {
		final String[][] replacements = {
				{".get(0)", ".getFirst()"},
				{".remove(0)", ".removeFirst()"}
		};
		for (var r : replacements) {
			final var idx = line.indexOf(r[0]);
			if (idx >= 0)
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
		}
		return null;
	}

	/**
	 * {@code .length() == 0} -> {@code .isEmpty()},
	 * {@code .size() > 0} -> {@code !receiver.isEmpty()}, etc.
	 * Handles all 6 comparison operators in both normal and reversed form,
	 * for both {@code .length()} and {@code .size()}.
	 * Negated forms scan backwards to find the receiver start.
	 * Returns null for complex receivers (method calls, array access).
	 */
	@CheckReturnValue
	@Nullable
	private static String fixLengthOrSizeIsEmpty(@Nonnull String line) {
		for (var method : new String[]{".length()", ".size()"}) {
			final var result = fixComparisonIsEmpty(line, method);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * {@code .keySet().contains(k)} -> {@code .containsKey(k)},
	 * {@code .values().contains(v)} -> {@code .containsValue(v)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixMapChain(@Nonnull String line) {
		final String[][] replacements = {
				{".keySet().contains(", ".containsKey("},
				{".values().contains(", ".containsValue("}
		};
		for (var r : replacements) {
			final var idx = line.indexOf(r[0]);
			if (idx >= 0)
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
		}
		return null;
	}

	/**
	 * {@code .replaceAll("literal", x)} -> {@code .replace("literal", x)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixReplaceAll(@Nonnull String line) {
		final var pattern = ".replaceAll(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".replace(" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().count()} -> {@code .size()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamCount(@Nonnull String line) {
		final var pattern = ".stream().count()";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".size()" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().findFirst().isPresent()} -> {@code !receiver.isEmpty()}.
	 * If already negated ({@code !receiver.stream()...}), removes the existing
	 * {@code !} to produce {@code receiver.isEmpty()} instead of {@code !!receiver.isEmpty()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamFindFirstIsPresent(@Nonnull String line) {
		final var pattern = ".stream().findFirst().isPresent()";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;

		final var receiverStart = findReceiverStart(line, idx);
		if (receiverStart < 0)
			return null;

		// if already negated, remove the ! instead of adding another
		if (receiverStart > 0 && line.charAt(receiverStart - 1) == '!') {
			return line.substring(0, receiverStart - 1) + line.substring(receiverStart, idx)
					+ ".isEmpty()" + line.substring(idx + pattern.length());
		}
		return line.substring(0, receiverStart) + "!" + line.substring(receiverStart, idx)
				+ ".isEmpty()" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().forEach(} -> {@code .forEach(}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamForEach(@Nonnull String line) {
		final var pattern = ".stream().forEach(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".forEach(" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code String.format("literal", args)} -> {@code "literal".formatted(args)},
	 * {@code String.format(singleArg)} -> {@code singleArg} (strip the call).
	 * Uses paren-balanced parsing to extract arguments.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStringFormat(@Nonnull String line) {
		final var pattern = "String.format(";
		final var idx = line.indexOf(pattern);
		if (idx < 0)
			return null;

		final var argsStart = idx + pattern.length();

		// find the matching closing paren using depth tracking
		var depth = 1;
		var closeParen = argsStart;
		var commaAtDepthOne = -1;
		while (closeParen < line.length() && depth > 0) {
			final var ch = line.charAt(closeParen);
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					break;
			}
			else if (ch == ',' && depth == 1 && commaAtDepthOne < 0)
				commaAtDepthOne = closeParen;
			else if (ch == '"') {
				++closeParen;
				while (closeParen < line.length()) {
					if (line.charAt(closeParen) == '\\')
						++closeParen;
					else if (line.charAt(closeParen) == '"')
						break;
					++closeParen;
				}
			}
			else if (ch == '\'') {
				++closeParen;
				if (closeParen < line.length() && line.charAt(closeParen) == '\\')
					++closeParen;
				if (closeParen < line.length())
					++closeParen;
			}
			if (depth > 0)
				++closeParen;
		}
		if (depth != 0)
			return null;

		// single-arg: String.format(expr) -> expr
		if (commaAtDepthOne < 0) {
			final var singleArg = line.substring(argsStart, closeParen).strip();
			return line.substring(0, idx) + singleArg + line.substring(closeParen + 1);
		}

		// multi-arg: first arg must be a string literal for .formatted() rewrite
		if (line.charAt(argsStart) != '"')
			return null;

		// find end of string literal
		var literalEnd = argsStart + 1;
		while (literalEnd < line.length()) {
			final var ch = line.charAt(literalEnd);
			if (ch == '\\')
				++literalEnd;
			else if (ch == '"')
				break;
			++literalEnd;
		}
		if (literalEnd >= line.length())
			return null;

		final var literal = line.substring(argsStart, literalEnd + 1);
		final var remainingArgs = line.substring(commaAtDepthOne + 1, closeParen).strip();
		return line.substring(0, idx) + literal + ".formatted(" + remainingArgs + ")"
				+ line.substring(closeParen + 1);
	}

	/**
	 * {@code .toArray(new Type[0])} -> {@code .toArray(Type[]::new)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixToArrayNewZero(@Nonnull String line) {
		final var prefix = ".toArray(new ";
		final var idx = line.indexOf(prefix);
		if (idx < 0)
			return null;

		final var typeStart = idx + prefix.length();
		final var bracketIdx = line.indexOf('[', typeStart);
		if (bracketIdx < 0)
			return null;

		final var typeName = line.substring(typeStart, bracketIdx);
		final var expectedEnd = "[0])";
		if (!line.startsWith(expectedEnd, bracketIdx))
			return null;

		return line.substring(0, idx) + ".toArray(" + typeName + "[]::new)"
				+ line.substring(bracketIdx + expectedEnd.length());
	}

	/**
	 * {@code .trim().isEmpty()} / {@code .strip().isEmpty()} -> {@code .isBlank()},
	 * {@code .trim().length() == 0} / {@code .strip().length() == 0} -> {@code .isBlank()},
	 * {@code 0 == .trim().length()} / {@code 0 == .strip().length()} -> {@code .isBlank()},
	 * {@code .trim().length() != 0} / {@code .strip().length() != 0} -> {@code !receiver.isBlank()}.
	 * Negated forms scan backwards from {@code .trim()} / {@code .strip()} to find the receiver
	 * start (identifiers and dotted names only). Returns null for complex
	 * receivers (method calls, array access, casts).
	 */
	@CheckReturnValue
	@Nullable
	private static String fixTrimIsBlank(@Nonnull String line) {
		for (var method : new String[]{".strip()", ".trim()"}) {
			final var result = fixTrimOrStripIsBlank(line, method);
			if (result != null)
				return result;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixTrimOrStripIsBlank(@Nonnull String line, @Nonnull String method) {
		var pattern = method + ".isEmpty()";
		var idx = line.indexOf(pattern);
		if (idx >= 0)
			return line.substring(0, idx) + ".isBlank()" + line.substring(idx + pattern.length());

		pattern = method + ".length() == 0";
		idx = findStandalonePattern(line, pattern, 0);
		if (idx >= 0)
			return line.substring(0, idx) + ".isBlank()" + line.substring(idx + pattern.length());

		final var suffix = method + ".length()";
		final String[][] reversedPositive = {
				{"0 == ", suffix},
				{"0 >= ", suffix},
				{"1 > ", suffix}
		};
		for (var rev : reversedPositive) {
			final var match = findReversedMatch(line, rev[0], rev[1]);
			if (match != null) {
				final var matchIdx = match[0];
				final var methodIdx = match[1];
				return line.substring(0, matchIdx) + line.substring(matchIdx + rev[0].length(), methodIdx)
						+ ".isBlank()" + line.substring(methodIdx + rev[1].length());
			}
		}

		pattern = method + ".length() < 1";
		idx = findStandalonePattern(line, pattern, 0);
		if (idx >= 0)
			return line.substring(0, idx) + ".isBlank()" + line.substring(idx + pattern.length());

		pattern = method + ".length() <= 0";
		idx = findStandalonePattern(line, pattern, 0);
		if (idx >= 0)
			return line.substring(0, idx) + ".isBlank()" + line.substring(idx + pattern.length());

		final String[] negPatterns = {
				method + ".length() != 0",
				method + ".length() > 0",
				method + ".length() >= 1"
		};
		for (var neg : negPatterns) {
			idx = findStandalonePattern(line, neg, 0);
			if (idx >= 0) {
				final var receiverStart = findReceiverStart(line, idx);
				if (receiverStart < 0)
					return null;
				if (receiverStart > 0 && line.charAt(receiverStart - 1) == '!') {
					return line.substring(0, receiverStart - 1) + line.substring(receiverStart, idx)
							+ ".isBlank()" + line.substring(idx + neg.length());
				}
				return line.substring(0, receiverStart) + "!" + line.substring(receiverStart, idx)
						+ ".isBlank()" + line.substring(idx + neg.length());
			}
		}

		final String[][] reversedNegated = {
				{"0 != ", suffix},
				{"0 < ", suffix},
				{"1 <= ", suffix}
		};
		for (var rev : reversedNegated) {
			final var match = findReversedMatch(line, rev[0], rev[1]);
			if (match != null) {
				final var matchIdx = match[0];
				final var methodIdx = match[1];
				final var receiver = line.substring(matchIdx + rev[0].length(), methodIdx);
				return line.substring(0, matchIdx) + "!" + receiver
						+ ".isBlank()" + line.substring(methodIdx + rev[1].length());
			}
		}

		return null;
	}

	@CheckReturnValue
	private static boolean isSimpleReceiver(@Nonnull String s) {
		if (s.isEmpty())
			return false;
		for (var i = 0; i < s.length(); ++i) {
			final var ch = s.charAt(i);
			if (!Character.isJavaIdentifierPart(ch) && ch != '.')
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var imports = new TreeSet<String>();

		// try each fixable pattern in turn
		var result = fixAssertion(lines, line, imports);
		if (result == null)
			result = fixCollectionsFactory(line, imports);
		if (result == null)
			result = fixCollectionsSort(line);
		if (result == null)
			result = fixArraysAsList(line, imports);
		if (result == null)
			result = fixCollectToList(line);
		if (result == null)
			result = fixEqualsEmpty(line);
		if (result == null)
			result = fixGetOrRemoveFirst(line);
		if (result == null)
			result = fixMapChain(line);
		if (result == null)
			result = fixReplaceAll(line);
		if (result == null)
			result = fixStreamCount(line);
		if (result == null)
			result = fixStreamFindFirstIsPresent(line);
		if (result == null)
			result = fixStreamForEach(line);
		if (result == null)
			result = fixStringFormat(line);
		if (result == null)
			result = fixToArrayNewZero(line);
		if (result == null)
			result = fixTrimIsBlank(line);
		if (result == null)
			result = fixLengthOrSizeIsEmpty(line);

		if (result == null)
			return new SkipResult(SkipMessages.PREFER_API_SKIP);
		if (imports.isEmpty())
			return new FixResult(lineIndex, lineIndex, List.of(result));
		return new FixResult(lineIndex, lineIndex, List.of(result), imports);
	}
}