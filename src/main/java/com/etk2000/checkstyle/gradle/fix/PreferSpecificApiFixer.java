package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

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
			final var parsed = ImportLine.parse(existing);
			if (parsed == null || !parsed.staticImport())
				continue;
			if (parsed.wildcard())
				return;
			final var fqn = parsed.fqn();
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
	 * {@code Collections.unmodifiableList(Arrays.asList(args))} -> {@code List.of(args)}.
	 * Mirrors the check's combined suggestion for the nested form; the plain
	 * {@code unmodifiableList(x)} form is left to the generic copyOf replacement.
	 */
	@CheckReturnValue
	@Nullable
	private static String collapseUnmodifiableListAsList(
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull Set<String> imports
	) {
		final var pattern = "Collections.unmodifiableList(Arrays.asList(";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;

		final var argsStart = idx + pattern.length();
		var depth = 2;
		var pos = argsStart;
		var innerClose = -1;
		while (pos < scan.length() && depth > 0) {
			final var ch = scan.charAt(pos);
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 1)
					innerClose = pos;
				else if (depth == 0)
					break;
			}
			++pos;
		}
		if (depth != 0 || innerClose < 0)
			return null;

		imports.add("java.util.List");
		return line.substring(0, idx) + "List.of(" + line.substring(argsStart, innerClose)
				+ ")" + line.substring(pos + 1);
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
	 * (so the leading digit is standalone, not part of {@code 100} or similar), and
	 * (b) the text between {@code prefix} and {@code suffix} is a simple receiver
	 * (identifier characters and dots only).
	 * Iterates through occurrences until a valid one is found. Returns
	 * {@code [prefixIdx, suffixIdx]} or {@code null}.
	 */
	@CheckReturnValue
	@Nullable
	private static int[] findReversedMatch(@Nonnull String scan, @Nonnull String prefix, @Nonnull String suffix) {
		var searchFrom = 0;
		while (searchFrom < scan.length()) {
			final var idx = scan.indexOf(prefix, searchFrom);
			if (idx < 0)
				return null;
			if (idx > 0) {
				final var prev = scan.charAt(idx - 1);
				if (Character.isJavaIdentifierPart(prev) || prev == '.') {
					searchFrom = idx + 1;
					continue;
				}
			}
			final var methodIdx = scan.indexOf(suffix, idx + prefix.length());
			if (methodIdx < 0)
				return null;
			final var between = scan.substring(idx + prefix.length(), methodIdx);
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
	private static int findStandalonePattern(@Nonnull String scan, @Nonnull String pattern, int from) {
		if (pattern.isEmpty() || !Character.isDigit(pattern.charAt(pattern.length() - 1)))
			return scan.indexOf(pattern, from);
		var idx = scan.indexOf(pattern, from);
		while (idx >= 0) {
			final var endPos = idx + pattern.length();
			if (endPos >= scan.length())
				return idx;
			final var ch = scan.charAt(endPos);
			if (!Character.isJavaIdentifierPart(ch) && ch != '.')
				return idx;
			idx = scan.indexOf(pattern, idx + 1);
		}
		return -1;
	}

	/**
	 * {@code Arrays.asList(...)} -> {@code List.of(...)}.
	 * Adds the {@code java.util.List} import.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixArraysAsList(@Nonnull String line, @Nonnull String scan, @Nonnull Set<String> imports) {
		final var pattern = "Arrays.asList(";
		final var idx = scan.indexOf(pattern);
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
	private static String fixAssertion(
			@Nonnull List<String> lines,
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull Set<String> imports
	) {
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralFirst(lines, line, scan, rule[0], rule[1], rule[2], imports);
			if (result != null)
				return result;
		}
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralLast(lines, line, scan, rule[0], rule[1], rule[2], imports);
			if (result != null)
				return result;
		}
		for (var rule : ASSERT_RULES) {
			final var result = fixAssertionLiteralMiddle(lines, line, scan, rule[0], rule[1], rule[2], imports);
			if (result != null)
				return result;
		}
		return null;
	}

	/**
	 * Handles {@code assertEquals(true, x)} -> {@code assertTrue(x)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralFirst(
			@Nonnull List<String> lines,
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement,
			@Nonnull Set<String> imports
	) {
		final var pattern = methodName + "(" + literal + ", ";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;
		if (!isQualifiedCallAt(line, idx))
			addAssertImport(lines, replacement, imports);
		return line.substring(0, idx) + replacement + "(" + line.substring(idx + pattern.length());
	}

	/**
	 * Handles {@code assertEquals(x, true)} -> {@code assertTrue(x)}.
	 * Finds the method call and removes the trailing literal argument.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixAssertionLiteralLast(
			@Nonnull List<String> lines,
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement,
			@Nonnull Set<String> imports
	) {
		final var methodStart = scan.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var suffix = ", " + literal + ")";
		final var suffixIdx = scan.indexOf(suffix, methodStart);
		if (suffixIdx < 0)
			return null;

		if (!isQualifiedCallAt(line, methodStart))
			addAssertImport(lines, replacement, imports);
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
			@Nonnull List<String> lines,
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull String methodName,
			@Nonnull String literal,
			@Nonnull String replacement,
			@Nonnull Set<String> imports
	) {
		final var methodStart = scan.indexOf(methodName + "(");
		if (methodStart < 0)
			return null;

		final var pattern = ", " + literal + ", ";
		final var patternIdx = scan.indexOf(pattern, methodStart);
		if (patternIdx < 0)
			return null;

		if (!isQualifiedCallAt(line, methodStart))
			addAssertImport(lines, replacement, imports);
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
	private static String fixCollectionsFactory(@Nonnull String line, @Nonnull String scan, @Nonnull Set<String> imports) {
		final var collapsed = collapseUnmodifiableListAsList(line, scan, imports);
		if (collapsed != null)
			return collapsed;

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
			final var idx = scan.indexOf(r[0]);
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
	private static String fixCollectionsSort(@Nonnull String line, @Nonnull String scan) {
		final var pattern = "Collections.sort(";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;

		final var argsStart = idx + pattern.length();

		var depth = 1;
		var pos = argsStart;
		var commaIdx = -1;
		while (pos < scan.length() && depth > 0) {
			final var ch = scan.charAt(pos);
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					break;
			}
			else if (ch == ',' && depth == 1 && commaIdx < 0)
				commaIdx = pos;
			++pos;
		}
		if (depth != 0)
			return null;

		final var closeParen = pos;
		if (commaIdx >= 0) {
			final var listArg = line.substring(argsStart, commaIdx).strip();
			final var cmpArg = line.substring(commaIdx + 1, closeParen).strip();
			return line.substring(0, idx) + listArg + ".sort(" + cmpArg + ")"
					+ line.substring(closeParen + 1);
		}

		final var listArg = line.substring(argsStart, closeParen).strip();
		return line.substring(0, idx) + listArg + ".sort(null)" + line.substring(closeParen + 1);
	}

	/**
	 * {@code .collect(Collectors.toList())} -> {@code .toList()}, and
	 * {@code .collect(Collectors.toUnmodifiableList())} -> {@code .toList()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixCollectToList(@Nonnull String line, @Nonnull String scan) {
		for (var collector : new String[]{"toList", "toUnmodifiableList"}) {
			final var pattern = ".collect(Collectors." + collector + "())";
			final var idx = scan.indexOf(pattern);
			if (idx >= 0)
				return line.substring(0, idx) + ".toList()" + line.substring(idx + pattern.length());
		}
		return null;
	}

	/**
	 * {@code .equals("")} -> {@code .isEmpty()}.
	 * Only matches when the argument is literally {@code ""}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixEqualsEmpty(@Nonnull String line, @Nonnull String scan) {
		final var pattern = ".equals(\"\")";
		final var idx = scan.indexOf(pattern);
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
	private static String fixGetOrRemoveFirst(@Nonnull String line, @Nonnull String scan) {
		final String[][] replacements = {
				{".get(0)", ".getFirst()"},
				{".remove(0)", ".removeFirst()"}
		};
		for (var r : replacements) {
			final var idx = scan.indexOf(r[0]);
			if (idx >= 0)
				return line.substring(0, idx) + r[1] + line.substring(idx + r[0].length());
		}
		return null;
	}

	/**
	 * {@code receiver.get(receiver.size() - 1)} -> {@code receiver.getLast()},
	 * {@code receiver.remove(receiver.size() - 1)} -> {@code receiver.removeLast()}.
	 * The check guarantees both receivers textually match; the fixer also requires
	 * the receiver to be a simple identifier or dotted name so we can locate it
	 * unambiguously on the line.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixGetOrRemoveLast(@Nonnull String line, @Nonnull String scan) {
		final String[][] replacements = {
				{".get(", ".getLast()"},
				{".remove(", ".removeLast()"}
		};
		for (var r : replacements) {
			final var pattern = r[0];
			var searchFrom = 0;
			while (searchFrom < scan.length()) {
				final var openIdx = scan.indexOf(pattern, searchFrom);
				if (openIdx < 0)
					break;
				final var receiverStart = findReceiverStart(line, openIdx);
				if (receiverStart < 0) {
					searchFrom = openIdx + 1;
					continue;
				}
				final var receiverText = line.substring(receiverStart, openIdx);
				final var argStart = openIdx + pattern.length();
				final var expectedArg = receiverText + ".size() - 1)";
				if (!line.startsWith(expectedArg, argStart)) {
					searchFrom = openIdx + 1;
					continue;
				}
				final var endPos = argStart + expectedArg.length();
				return line.substring(0, openIdx) + r[1] + line.substring(endPos);
			}
		}
		return null;
	}

	/**
	 * {@code .indexOf("x")} / {@code .lastIndexOf("x")} (single-char string literal)
	 * -> {@code .indexOf('x')} / {@code .lastIndexOf('x')}. Also handles 2-arg overloads.
	 * Returns null when the string content cannot be safely re-emitted as a char literal
	 * (e.g. unrecognized escape, multi-line construct).
	 */
	@CheckReturnValue
	@Nullable
	private static String fixIndexOfChar(@Nonnull String line, @Nonnull String scan, int column) {
		// the violation column points at the LPAREN of the METHOD_CALL when emitted
		// from log(call, ...). Try lastIndexOf (LPAREN-anchored) first; if the match's
		// LPAREN does NOT line up with `column`, fall back to indexOf (receiver-anchored)
		// which matches when the column points at the receiver chain instead.
		final var safeColumn = Math.max(0, column);
		for (var name : new String[]{".indexOf(", ".lastIndexOf("}) {
			var idx = scan.lastIndexOf(name, safeColumn);
			if (idx < 0 || idx + name.length() - 1 < safeColumn - 1)
				idx = scan.indexOf(name, safeColumn);
			if (idx < 0)
				continue;
			final var openParen = idx + name.length() - 1;
			final var argStart = openParen + 1;
			if (argStart >= scan.length() || scan.charAt(argStart) != '"')
				continue;
			// The mask blanks the string interior (including any escaped quote) but
			// keeps the delimiter quotes, so the next '"' is the closing quote.
			final var end = scan.indexOf('"', argStart + 1);
			if (end < 0)
				return null;
			final var content = line.substring(argStart + 1, end);
			final var charLiteral = stringContentToCharLiteralContent(content);
			if (charLiteral == null)
				continue;
			return line.substring(0, argStart) + "'" + charLiteral + "'" + line.substring(end + 1);
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
	private static String fixLengthOrSizeIsEmpty(@Nonnull String line, @Nonnull String scan) {
		for (var method : new String[]{".length()", ".size()"}) {
			final var result = rewriteSizeComparison(line, scan, method, ".isEmpty()");
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
	private static String fixMapChain(@Nonnull String line, @Nonnull String scan) {
		final String[][] replacements = {
				{".keySet().contains(", ".containsKey("},
				{".values().contains(", ".containsValue("}
		};
		for (var r : replacements) {
			final var idx = scan.indexOf(r[0]);
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
	private static String fixReplaceAll(@Nonnull String line, @Nonnull String scan) {
		final var pattern = ".replaceAll(";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;
		return line.substring(0, idx) + ".replace(" + line.substring(idx + pattern.length());
	}

	/**
	 * {@code .stream().count()} -> {@code .size()}.
	 */
	@CheckReturnValue
	@Nullable
	private static String fixStreamCount(@Nonnull String line, @Nonnull String scan) {
		final var pattern = ".stream().count()";
		final var idx = scan.indexOf(pattern);
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
	private static String fixStreamFindFirstIsPresent(@Nonnull String line, @Nonnull String scan) {
		final var pattern = ".stream().findFirst().isPresent()";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;

		final var receiverStart = findReceiverStart(line, idx);
		if (receiverStart < 0)
			return null;

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
	private static String fixStreamForEach(@Nonnull String line, @Nonnull String scan) {
		final var pattern = ".stream().forEach(";
		final var idx = scan.indexOf(pattern);
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
	private static String fixStringFormat(@Nonnull String line, @Nonnull String scan) {
		final var pattern = "String.format(";
		final var idx = scan.indexOf(pattern);
		if (idx < 0)
			return null;

		final var argsStart = idx + pattern.length();

		var depth = 1;
		var closeParen = argsStart;
		var commaAtDepthOne = -1;
		while (closeParen < scan.length() && depth > 0) {
			final var ch = scan.charAt(closeParen);
			if (ch == '(')
				++depth;
			else if (ch == ')') {
				--depth;
				if (depth == 0)
					break;
			}
			else if (ch == ',' && depth == 1 && commaAtDepthOne < 0)
				commaAtDepthOne = closeParen;
			++closeParen;
		}
		if (depth != 0)
			return null;

		if (commaAtDepthOne < 0) {
			final var singleArg = line.substring(argsStart, closeParen).strip();
			return line.substring(0, idx) + singleArg + line.substring(closeParen + 1);
		}

		if (scan.charAt(argsStart) != '"')
			return null;

		// The masked scan blanks string interiors but keeps the delimiter quotes,
		// so the first '"' after the opener is the literal's closing quote.
		final var literalEnd = scan.indexOf('"', argsStart + 1);
		if (literalEnd < 0 || literalEnd > commaAtDepthOne)
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
	private static String fixToArrayNewZero(@Nonnull String line, @Nonnull String scan) {
		final var prefix = ".toArray(new ";
		final var idx = scan.indexOf(prefix);
		if (idx < 0)
			return null;

		final var typeStart = idx + prefix.length();
		final var bracketIdx = scan.indexOf('[', typeStart);
		if (bracketIdx < 0)
			return null;

		final var typeName = line.substring(typeStart, bracketIdx);
		final var expectedEnd = "[0])";
		if (!scan.startsWith(expectedEnd, bracketIdx))
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
	private static String fixTrimIsBlank(@Nonnull String line, @Nonnull String scan) {
		for (var method : new String[]{".strip()", ".trim()"}) {
			final var result = fixTrimOrStripIsBlank(line, scan, method);
			if (result != null)
				return result;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String fixTrimOrStripIsBlank(@Nonnull String line, @Nonnull String scan, @Nonnull String method) {
		final var emptyPattern = method + ".isEmpty()";
		final var emptyIdx = scan.indexOf(emptyPattern);
		if (emptyIdx >= 0)
			return line.substring(0, emptyIdx) + ".isBlank()" + line.substring(emptyIdx + emptyPattern.length());

		return rewriteSizeComparison(line, scan, method + ".length()", ".isBlank()");
	}

	/**
	 * Whether the call whose name starts at {@code idx} has a dotted receiver
	 * (e.g. {@code org.junit.Assert.assertEquals(...)}). A qualified call keeps
	 * its receiver after the rewrite, so it needs no static import. Takes the
	 * matched index (not a name) so the check targets the exact rewritten call.
	 */
	@CheckReturnValue
	private static boolean isQualifiedCallAt(@Nonnull String line, int idx) {
		if (idx <= 0)
			return false;
		var pos = idx - 1;
		while (pos >= 0 && Character.isWhitespace(line.charAt(pos)))
			--pos;
		return pos >= 0 && line.charAt(pos) == '.';
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

	/**
	 * Rewrites a size/length comparison against zero into a boolean-returning call.
	 * {@code subject} is the size/length expression suffix (e.g. {@code .size()} or
	 * {@code .trim().length()}); {@code replacement} is the call it collapses to
	 * (e.g. {@code .isEmpty()} or {@code .isBlank()}). Handles positive
	 * ({@code == 0}, {@code <= 0}, {@code < 1}) and negated ({@code != 0},
	 * {@code > 0}, {@code >= 1}) forms, each in forward and operand-reversed order.
	 */
	@CheckReturnValue
	@Nullable
	private static String rewriteSizeComparison(
			@Nonnull String line,
			@Nonnull String scan,
			@Nonnull String subject,
			@Nonnull String replacement
	) {
		final String[] positivePatterns = {subject + " == 0", subject + " <= 0", subject + " < 1"};
		for (var pattern : positivePatterns) {
			final var idx = findStandalonePattern(scan, pattern, 0);
			if (idx >= 0)
				return line.substring(0, idx) + replacement + line.substring(idx + pattern.length());
		}

		final String[][] reversedPositive = {{"0 == ", subject}, {"0 >= ", subject}, {"1 > ", subject}};
		for (var rev : reversedPositive) {
			final var match = findReversedMatch(scan, rev[0], rev[1]);
			if (match != null) {
				final var idx = match[0];
				final var methodIdx = match[1];
				return line.substring(0, idx) + line.substring(idx + rev[0].length(), methodIdx)
						+ replacement + line.substring(methodIdx + rev[1].length());
			}
		}

		final String[] negatedPatterns = {subject + " != 0", subject + " > 0", subject + " >= 1"};
		for (var neg : negatedPatterns) {
			final var idx = findStandalonePattern(scan, neg, 0);
			if (idx >= 0) {
				final var receiverStart = findReceiverStart(line, idx);
				if (receiverStart < 0)
					return null;
				if (receiverStart > 0 && line.charAt(receiverStart - 1) == '!') {
					return line.substring(0, receiverStart - 1) + line.substring(receiverStart, idx)
							+ replacement + line.substring(idx + neg.length());
				}
				return line.substring(0, receiverStart) + "!" + line.substring(receiverStart, idx)
						+ replacement + line.substring(idx + neg.length());
			}
		}

		final String[][] reversedNegated = {{"0 != ", subject}, {"0 < ", subject}, {"1 <= ", subject}};
		for (var rev : reversedNegated) {
			final var match = findReversedMatch(scan, rev[0], rev[1]);
			if (match != null) {
				final var idx = match[0];
				final var methodIdx = match[1];
				final var receiver = line.substring(idx + rev[0].length(), methodIdx);
				return line.substring(0, idx) + "!" + receiver + replacement + line.substring(methodIdx + rev[1].length());
			}
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String stringContentToCharLiteralContent(@Nonnull String content) {
		if (content.isEmpty())
			return null;
		// `'` in a string becomes `\'` in a char literal
		if ("'".equals(content))
			return "\\'";
		// `\"` escape in a string becomes plain `"` in a char literal
		if ("\\\"".equals(content))
			return "\"";
		if (content.length() == 1 && content.charAt(0) != '\\' && content.charAt(0) != '\'')
			return content;
		if (content.length() >= 2 && content.charAt(0) == '\\') {
			if (content.length() == 2) {
				final var n = content.charAt(1);
				if (n == '"' || n == '\'' || n == '0' || n == '\\' || n == 'b' || n == 'f'
						|| n == 'n' || n == 'r' || n == 's' || n == 't')
					return content;
			}
			if (content.length() == 6 && content.charAt(1) == 'u') {
				for (var i = 2; i < 6; ++i) {
					final var c = content.charAt(i);
					if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F')))
						return null;
				}
				return content;
			}
			if (content.length() <= 4) {
				var allOctal = true;
				for (var i = 1; i < content.length(); ++i) {
					if (content.charAt(i) < '0' || content.charAt(i) > '7') {
						allOctal = false;
						break;
					}
				}
				// per JLS: 3-digit octal escape requires first digit 0..3 (max value \377)
				if (allOctal && content.length() == 4 && content.charAt(1) > '3')
					return null;
				if (allOctal && content.length() >= 2)
					return content;
			}
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var imports = new TreeSet<String>();
		// Fold the lexer state over preceding lines so a line that continues a
		// multi-line construct (e.g. a text block whose closing """ sits on this
		// line before real code) is masked with the correct entry state.
		var entryState = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			entryState = JavaLineScanner.stateAfter(lines.get(i), entryState);
		// Mask string/char/comment content once (positions preserved) and locate
		// every pattern on the mask, splicing output from the original line, so a
		// pattern appearing inside a literal or comment can't hijack the match.
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);

		var result = fixAssertion(lines, line, scan, imports);
		if (result == null)
			result = fixCollectionsFactory(line, scan, imports);
		if (result == null)
			result = fixCollectionsSort(line, scan);
		if (result == null)
			result = fixArraysAsList(line, scan, imports);
		if (result == null)
			result = fixCollectToList(line, scan);
		if (result == null)
			result = fixEqualsEmpty(line, scan);
		if (result == null)
			result = fixGetOrRemoveFirst(line, scan);
		if (result == null)
			result = fixGetOrRemoveLast(line, scan);
		if (result == null)
			result = fixIndexOfChar(line, scan, column);
		if (result == null)
			result = fixMapChain(line, scan);
		if (result == null)
			result = fixReplaceAll(line, scan);
		if (result == null)
			result = fixStreamCount(line, scan);
		if (result == null)
			result = fixStreamFindFirstIsPresent(line, scan);
		if (result == null)
			result = fixStreamForEach(line, scan);
		if (result == null)
			result = fixStringFormat(line, scan);
		if (result == null)
			result = fixToArrayNewZero(line, scan);
		if (result == null)
			result = fixTrimIsBlank(line, scan);
		if (result == null)
			result = fixLengthOrSizeIsEmpty(line, scan);

		if (result == null)
			return new SkipResult(SkipMessages.PREFER_API_SKIP);
		if (imports.isEmpty())
			return new FixResult(lineIndex, lineIndex, List.of(result));
		return new FixResult(lineIndex, lineIndex, List.of(result), imports);
	}
}