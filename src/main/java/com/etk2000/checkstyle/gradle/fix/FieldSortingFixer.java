package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for FieldSortingCheck enum constant violations only.
 * Sorts enum constants alphabetically and splits same-line constants.
 * Returns null for field ordering violations (type, name, chunk, dependency, anon.class).
 */
class FieldSortingFixer implements CheckstyleFixer {
	private record EnumEntry(@Nonnull String name, @Nonnull List<String> leadingLines, @Nonnull List<String> contentLines) {}

	private record ParseResult(@Nonnull List<EnumEntry> entries, int blockStart, int blockEnd, @Nonnull String terminal) {}

	@CheckReturnValue
	@Nonnull
	private static List<String> buildReplacement(@Nonnull List<EnumEntry> sorted, @Nonnull String terminal) {
		final var result = new ArrayList<String>();
		for (var i = 0; i < sorted.size(); ++i) {
			final var entry = sorted.get(i);
			result.addAll(entry.leadingLines());
			for (var j = 0; j < entry.contentLines().size(); ++j) {
				var line = entry.contentLines().get(j);
				if (j == entry.contentLines().size() - 1) {
					final var sep = i < sorted.size() - 1 ? "," : terminal;
					line = insertBeforeTrailingComment(line, sep);
				}
				result.add(line);
			}
		}
		return result;
	}

	@CheckReturnValue
	private static boolean containsEnumKeyword(@Nonnull List<String> lines, int braceLineIndex) {
		for (var i = braceLineIndex; i >= 0 && i >= braceLineIndex - 5; --i) {
			final var line = lines.get(i);
			var idx = line.indexOf("enum");
			while (idx >= 0) {
				final var before = idx == 0 || !Character.isJavaIdentifierPart(line.charAt(idx - 1));
				final var after = idx + 4 >= line.length() || !Character.isJavaIdentifierPart(line.charAt(idx + 4));
				if (before && after)
					return true;
				idx = line.indexOf("enum", idx + 1);
			}
			if (i < braceLineIndex) {
				final var trimmed = line.stripLeading();
				if (trimmed.contains("{") || trimmed.contains("}") || trimmed.contains(";"))
					break;
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String detectTerminal(@Nonnull String line) {
		var lastSep = -1;
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else if (c == ',' || c == ';')
				lastSep = i;
		}
		if (lastSep >= 0)
			return String.valueOf(line.charAt(lastSep));
		return "";
	}

	/**
	 * Detects the terminal from the last segment of a same-line constant line.
	 * For "ALPHA, BETA;" returns ";". For "ALPHA, BETA" returns "".
	 */
	@CheckReturnValue
	@Nonnull
	private static String detectTerminalFromLastSegment(@Nonnull String trimmed) {
		var lastSplitComma = -1;
		var parenDepth = 0;
		var inString = false;
		var inChar = false;

		for (var i = 0; i < trimmed.length(); ++i) {
			final var c = trimmed.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(trimmed, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(trimmed, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '/')
				break;
			else {
				if (c == '(')
					++parenDepth;
				else if (c == ')')
					--parenDepth;
				else if (c == ',' && parenDepth == 0) {
					var j = i + 1;
					while (j < trimmed.length() && trimmed.charAt(j) <= ' ')
						++j;
					if (j < trimmed.length() && Character.isJavaIdentifierStart(trimmed.charAt(j)))
						lastSplitComma = i;
				}
			}
		}

		if (lastSplitComma < 0)
			return detectTerminal(trimmed);

		final var lastSegment = trimmed.substring(lastSplitComma + 1).strip();
		return detectTerminal(lastSegment);
	}

	@CheckReturnValue
	@Nullable
	private static String extractConstantName(@Nonnull String trimmed) {
		if (trimmed.isEmpty() || !Character.isJavaIdentifierStart(trimmed.charAt(0)))
			return null;

		var end = 1;
		while (end < trimmed.length() && Character.isJavaIdentifierPart(trimmed.charAt(end)))
			++end;

		final var name = trimmed.substring(0, end);
		if (isJavaKeyword(name))
			return null;

		var i = end;
		while (i < trimmed.length() && trimmed.charAt(i) <= ' ')
			++i;

		if (i >= trimmed.length())
			return name;

		final var next = trimmed.charAt(i);
		if (next == '(' || next == '{' || next == ',' || next == ';')
			return name;
		if (next == '/' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '/')
			return name;

		return null;
	}

	@CheckReturnValue
	private static int findEnumOpen(@Nonnull List<String> lines, int lineIndex) {
		var depth = 0;
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var line = lines.get(i);
			final var trimmed = line.stripLeading();
			if (trimmed.startsWith("*") && !trimmed.startsWith("*/"))
				continue;

			final var braces = structuralBraces(line);
			for (var j = braces.size() - 1; j >= 0; --j) {
				if (braces.get(j) == '}')
					++depth;
				else {
					if (depth == 0)
						return containsEnumKeyword(lines, i) ? i : -1;
					--depth;
				}
			}
		}
		return -1;
	}

	/**
	 * Finds the start index of a trailing {@code //} comment on a line, skipping
	 * occurrences inside string/char literals. Returns -1 if no trailing comment.
	 */
	@CheckReturnValue
	private static int findTrailingCommentStart(@Nonnull String line) {
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return i;
		}
		return -1;
	}

	/**
	 * Inserts a separator (comma or semicolon) before any trailing line comment.
	 * For "ALPHA // comment" with sep "," returns "ALPHA, // comment".
	 * For "ALPHA" with sep "," returns "ALPHA,".
	 */
	@CheckReturnValue
	@Nonnull
	private static String insertBeforeTrailingComment(@Nonnull String line, @Nonnull String sep) {
		if (sep.isEmpty())
			return line;
		final var commentStart = findTrailingCommentStart(line);
		if (commentStart < 0)
			return line + sep;
		// insert separator before the comment, preserving whitespace
		final var beforeComment = line.substring(0, commentStart).stripTrailing();
		final var commentPart = line.substring(commentStart);
		return beforeComment + sep + " " + commentPart;
	}

	@CheckReturnValue
	private static boolean isEscaped(@Nonnull String line, int pos) {
		var backslashes = 0;
		for (var i = pos - 1; i >= 0 && line.charAt(i) == '\\'; --i)
			++backslashes;
		return backslashes % 2 != 0;
	}

	@CheckReturnValue
	private static boolean isJavaKeyword(@Nonnull String word) {
		return switch (word) {
			case "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
			     "class", "const", "continue", "default", "do", "double", "else", "enum",
			     "extends", "final", "finally", "float", "for", "goto", "if", "implements",
			     "import", "instanceof", "int", "interface", "long", "native", "new", "package",
			     "private", "protected", "public", "record", "return", "short", "static",
			     "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
			     "transient", "try", "var", "void", "volatile", "while", "yield" -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	@Nullable
	private static ParseResult parseBlock(@Nonnull List<String> lines, int afterBrace) {
		final var entries = new ArrayList<EnumEntry>();
		var leading = new ArrayList<String>();
		var blockStart = -1;
		var blockEnd = -1;
		var terminal = "";
		var i = afterBrace;

		while (i < lines.size()) {
			final var line = lines.get(i);
			final var trimmed = line.stripLeading();
			final var indent = line.substring(0, line.length() - trimmed.length());

			// skip initial blank lines before any constant-related content
			if (entries.isEmpty() && leading.isEmpty() && trimmed.isEmpty()) {
				++i;
				continue;
			}

			// end of enum body
			if (trimmed.startsWith("}"))
				break;

			// blank line
			if (trimmed.isEmpty()) {
				if (blockStart < 0)
					blockStart = i;
				leading.add(line);
				++i;
				continue;
			}

			// comment line
			if (trimmed.startsWith("//") || trimmed.startsWith("*")) {
				if (blockStart < 0)
					blockStart = i;
				leading.add(line);
				++i;
				continue;
			}

			// block comment start
			if (trimmed.startsWith("/*")) {
				if (blockStart < 0)
					blockStart = i;
				leading.add(line);
				if (!trimmed.contains("*/")) {
					++i;
					while (i < lines.size() && !lines.get(i).contains("*/")) {
						leading.add(lines.get(i));
						++i;
					}
					if (i < lines.size()) {
						leading.add(lines.get(i));
						++i;
					}
				}
				else
					++i;
				continue;
			}

			// annotation
			if (trimmed.startsWith("@")) {
				if (blockStart < 0)
					blockStart = i;
				leading.add(line);
				// use string/char/comment-aware paren tracking
				var annotParenDepth = structuralParenDelta(line);
				++i;
				while (annotParenDepth > 0 && i < lines.size()) {
					leading.add(lines.get(i));
					annotParenDepth += structuralParenDelta(lines.get(i));
					++i;
				}
				continue;
			}

			// try to extract a constant name
			final var name = extractConstantName(trimmed);
			if (name == null)
				break;

			if (blockStart < 0)
				blockStart = i;

			// check for same-line constants: comma at depth 0 followed by an identifier
			final var sameLine = parseSameLine(trimmed, indent);
			if (sameLine != null && sameLine.size() > 1) {
				for (var j = 0; j < sameLine.size(); ++j) {
					final var entry = sameLine.get(j);
					final var entryLeading = j == 0 ? new ArrayList<>(leading) : List.<String>of();
					entries.add(new EnumEntry(entry.name(), entryLeading, entry.contentLines()));
				}
				leading = new ArrayList<>();
				blockEnd = i;
				terminal = detectTerminalFromLastSegment(trimmed);
				++i;
				if (terminal.equals(";"))
					break;
				continue;
			}

			// single constant, possibly multi-line
			final var content = new ArrayList<String>();
			var parenDepth = 0;
			var braceDepth = 0;

			do {
				final var currentLine = lines.get(i);
				content.add(currentLine);
				final var depths = updateDepths(currentLine, parenDepth, braceDepth);
				parenDepth = depths[0];
				braceDepth = depths[1];
				blockEnd = i;
				++i;
			} while ((parenDepth > 0 || braceDepth > 0) && i < lines.size());

			// detect and strip terminal from last content line
			final var lastRaw = content.getLast();
			terminal = detectTerminal(lastRaw);
			content.set(content.size() - 1, stripTerminal(lastRaw));

			entries.add(new EnumEntry(name, new ArrayList<>(leading), content));
			leading = new ArrayList<>();

			if (terminal.equals(";"))
				break;
		}

		if (entries.isEmpty() || blockStart < 0 || blockEnd < 0)
			return null;

		return new ParseResult(entries, blockStart, blockEnd, terminal);
	}

	/**
	 * Parses same-line constants from a trimmed line like "ALPHA, BETA, GAMMA".
	 * Returns entries with trailing separators stripped, or null if not same-line.
	 */
	@CheckReturnValue
	@Nullable
	private static List<EnumEntry> parseSameLine(@Nonnull String trimmed, @Nonnull String indent) {
		final var splits = new ArrayList<Integer>();
		var parenDepth = 0;
		var braceDepth = 0;
		var inString = false;
		var inChar = false;

		for (var i = 0; i < trimmed.length(); ++i) {
			final var c = trimmed.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(trimmed, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(trimmed, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < trimmed.length() && trimmed.charAt(i + 1) == '*') {
				final var end = trimmed.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else {
				switch (c) {
					case '(' -> ++parenDepth;
					case ')' -> --parenDepth;
					case ',' -> {
						if (parenDepth == 0 && braceDepth == 0) {
							var j = i + 1;
							while (j < trimmed.length() && trimmed.charAt(j) <= ' ')
								++j;
							if (j < trimmed.length() && Character.isJavaIdentifierStart(trimmed.charAt(j))) {
								var nameEnd = j;
								while (nameEnd < trimmed.length() && Character.isJavaIdentifierPart(trimmed.charAt(nameEnd)))
									++nameEnd;
								if (!isJavaKeyword(trimmed.substring(j, nameEnd)))
									splits.add(i);
							}
						}
					}
					case '{' -> ++braceDepth;
					case '}' -> --braceDepth;
				}
			}
		}

		if (splits.isEmpty())
			return null;

		final var entries = new ArrayList<EnumEntry>();
		var start = 0;
		for (var splitIdx : splits) {
			final var segment = trimmed.substring(start, splitIdx).strip();
			final var name = extractConstantName(segment);
			if (name == null)
				return null;
			entries.add(new EnumEntry(name, List.of(), List.of(indent + segment)));
			start = splitIdx + 1;
			while (start < trimmed.length() && trimmed.charAt(start) <= ' ')
				++start;
		}

		// last segment
		final var lastRaw = trimmed.substring(start).strip();
		if (lastRaw.isEmpty())
			return null;
		final var lastName = extractConstantName(lastRaw);
		if (lastName == null)
			return null;
		final var lastStripped = stripTerminal(indent + lastRaw);
		entries.add(new EnumEntry(lastName, List.of(), List.of(lastStripped)));

		return entries.size() > 1 ? entries : null;
	}

	@CheckReturnValue
	@Nonnull
	private static String stripTerminal(@Nonnull String line) {
		var lastSep = -1;
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else if (c == ',' || c == ';')
				lastSep = i;
		}
		if (lastSep >= 0)
			return line.substring(0, lastSep) + line.substring(lastSep + 1);
		return line;
	}

	@CheckReturnValue
	@Nonnull
	private static List<Character> structuralBraces(@Nonnull String line) {
		final var result = new ArrayList<Character>();
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else if (c == '{' || c == '}')
				result.add(c);
		}
		return result;
	}

	/**
	 * Counts the net paren delta on a line, skipping parens inside string/char
	 * literals and comments. Returns positive for unmatched '(', negative for ')'.
	 */
	@CheckReturnValue
	private static int structuralParenDelta(@Nonnull String line) {
		var delta = 0;
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else if (c == '(')
				++delta;
			else if (c == ')')
				--delta;
		}
		return delta;
	}

	@CheckReturnValue
	@Nonnull
	private static int[] updateDepths(@Nonnull String line, int parenDepth, int braceDepth) {
		var inString = false;
		var inChar = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(line, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(line, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				final var end = line.indexOf("*/", i + 2);
				if (end >= 0)
					i = end + 1;
				else
					break;
			}
			else {
				switch (c) {
					case '(' -> ++parenDepth;
					case ')' -> --parenDepth;
					case '{' -> ++braceDepth;
					case '}' -> --braceDepth;
				}
			}
		}
		return new int[]{parenDepth, braceDepth};
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var enumOpenLine = findEnumOpen(lines, lineIndex);
		if (enumOpenLine < 0)
			return new SkipResult(SkipMessages.FIELD_SORT_SKIP);

		final var result = parseBlock(lines, enumOpenLine + 1);
		if (result == null || result.entries().size() < 2)
			return null;

		// sort entries alphabetically by name
		final var sorted = new ArrayList<>(result.entries());
		sorted.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));

		// strip leading blank lines from the first entry to avoid NoBlankLineAfterClassBrace
		final var first = sorted.getFirst();
		final var strippedLeading = new ArrayList<>(first.leadingLines());
		while (!strippedLeading.isEmpty() && strippedLeading.getFirst().isBlank())
			strippedLeading.removeFirst();
		sorted.set(0, new EnumEntry(first.name(), strippedLeading, first.contentLines()));

		final var replacement = buildReplacement(sorted, result.terminal());

		// idempotence check
		final var original = new ArrayList<>(lines.subList(result.blockStart(), result.blockEnd() + 1));
		if (replacement.equals(original))
			return null;

		return new FixResult(result.blockStart(), result.blockEnd(), replacement);
	}
}