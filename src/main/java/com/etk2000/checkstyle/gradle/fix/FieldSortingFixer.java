package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for FieldSortingCheck violations. Sorts enum constants alphabetically
 * and splits same-line constants. Also sorts field declarations by chunk
 * (final+value, final-no-value, non-final), type (primitives first), and
 * name (alphabetical).
 */
class FieldSortingFixer implements CheckstyleFixer {
	private record EnumEntry(@Nonnull String name, @Nonnull List<String> leadingLines, @Nonnull List<String> contentLines) {}

	private record FieldEntry(
			@Nonnull String name,
			@Nonnull String typeName,
			int chunk,
			boolean isStatic,
			@Nonnull List<String> lines,
			@Nonnull Set<String> dependencies
	) {}

	private record ParseResult(@Nonnull List<EnumEntry> entries, int blockStart, int blockEnd, @Nonnull String terminal) {}

	private static final Pattern FIELD_PATTERN = Pattern.compile(
			"^\\s*(?:(?:@\\w+(?:\\([^)]*\\))?\\s+)*)"
					+ "(?:(?:public|private|protected|static|final|transient|volatile)\\s+)*"
					+ "((?:boolean|byte|char|double|float|int|long|short|[A-Z]\\w*)(?:<[^>]*>)?(?:\\[\\])*)"
					+ "\\s+(\\w+)"
	);
	private static final Set<String> PRIMITIVES = Set.of(
			"boolean", "byte", "char", "double", "float", "int", "long", "short"
	);

	@CheckReturnValue
	private static int arrayDepthOf(@Nonnull String typeName) {
		var depth = 0;
		var idx = typeName.indexOf('[');
		while (idx >= 0) {
			++depth;
			idx = typeName.indexOf('[', idx + 1);
		}
		return depth;
	}

	@CheckReturnValue
	@Nonnull
	private static String baseTypeName(@Nonnull String typeName) {
		final var bracket = typeName.indexOf('[');
		final var angle = typeName.indexOf('<');
		var end = typeName.length();
		if (bracket >= 0)
			end = Math.min(end, bracket);
		if (angle >= 0)
			end = Math.min(end, angle);
		return typeName.substring(0, end);
	}

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
	private static int compareFieldTypes(@Nonnull String a, @Nonnull String b) {
		final var aBase = baseTypeName(a);
		final var bBase = baseTypeName(b);
		final var aPrim = PRIMITIVES.contains(aBase);
		final var bPrim = PRIMITIVES.contains(bBase);
		if (aPrim != bPrim)
			return aPrim ? -1 : 1;
		if (aBase.equals(bBase))
			return Integer.compare(arrayDepthOf(a), arrayDepthOf(b));
		return aBase.compareToIgnoreCase(bBase);
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
	private static boolean containsFieldWord(@Nonnull String text, @Nonnull String word) {
		var idx = text.indexOf(word);
		while (idx >= 0) {
			final var before = idx == 0 || !Character.isJavaIdentifierPart(text.charAt(idx - 1));
			final var after = idx + word.length() >= text.length()
					|| !Character.isJavaIdentifierPart(text.charAt(idx + word.length()));
			if (before && after)
				return true;
			idx = text.indexOf(word, idx + 1);
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String declPrefix(@Nonnull String stripped) {
		final var eqIdx = stripped.indexOf('=');
		return eqIdx >= 0 ? stripped.substring(0, eqIdx) : stripped;
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
	private static int fieldChunk(@Nonnull String declLine) {
		final var prefix = declPrefix(declLine.stripLeading());
		final var hasFinal = containsFieldWord(prefix, "final");
		if (!hasFinal)
			return 2;
		return declLine.contains("=") ? 0 : 1;
	}

	@CheckReturnValue
	private static boolean fieldIsStatic(@Nonnull String declLine) {
		return containsFieldWord(declPrefix(declLine.stripLeading()), "static");
	}

	@CheckReturnValue
	private static int findClassBodyEnd(@Nonnull List<String> lines, int afterOpen) {
		var depth = 1;
		var inBlockComment = false;
		var inString = false;
		var inChar = false;
		for (var i = afterOpen; i < lines.size(); ++i) {
			final var line = lines.get(i);
			for (var j = 0; j < line.length(); ++j) {
				final var c = line.charAt(j);
				if (inBlockComment) {
					if (c == '*' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
						inBlockComment = false;
						++j;
					}
					continue;
				}
				if (inString) {
					if (c == '"' && !isEscaped(line, j))
						inString = false;
				}
				else if (inChar) {
					if (c == '\'' && !isEscaped(line, j))
						inChar = false;
				}
				else if (c == '"')
					inString = true;
				else if (c == '\'')
					inChar = true;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/')
					break;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
					inBlockComment = true;
					++j;
				}
				else if (c == '{')
					++depth;
				else if (c == '}') {
					--depth;
					if (depth == 0)
						return i;
				}
			}
		}
		return -1;
	}

	@CheckReturnValue
	private static int findClassBodyStart(@Nonnull List<String> lines, int fromLine) {
		var depth = 0;
		for (var i = fromLine - 1; i >= 0; --i) {
			final var line = lines.get(i);
			for (var j = line.length() - 1; j >= 0; --j) {
				final var c = line.charAt(j);
				if (c == '{' || c == '}') {
					if (isInsideString(line, j))
						continue;
					if (c == '}')
						++depth;
					else if (depth == 0)
						return i;
					else
						--depth;
				}
			}
		}
		return -1;
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

	@Nullable
	private static FixResult fixFieldOrder(@Nonnull List<String> lines, int lineIndex) {
		final var bodyStart = findClassBodyStart(lines, lineIndex);
		if (bodyStart < 0)
			return null;

		final var bodyEnd = findClassBodyEnd(lines, bodyStart + 1);
		if (bodyEnd < 0)
			return null;

		// parse fields
		final var fields = parseFields(lines, bodyStart + 1, bodyEnd);
		if (fields.size() < 2)
			return null;

		// determine if the violation is in static or instance fields
		final var violationStatic = fieldIsStatic(lines.get(lineIndex));
		final var group = new ArrayList<FieldEntry>();
		for (var f : fields) {
			if (f.isStatic == violationStatic)
				group.add(f);
		}

		if (group.size() < 2)
			return null;

		// sort by chunk, type, name, with dependency exceptions
		final var fieldNames = new HashSet<String>();
		for (var f : group)
			fieldNames.add(f.name);

		final var sorted = new ArrayList<>(group);
		sorted.sort(Comparator
				.comparingInt(FieldEntry::chunk)
				.thenComparing(FieldEntry::typeName, FieldSortingFixer::compareFieldTypes)
				.thenComparing(FieldEntry::name, String.CASE_INSENSITIVE_ORDER));

		// apply dependency adjustments, with cycle guard
		var maxIter = sorted.size() * sorted.size();
		for (var changed = true; changed && --maxIter >= 0; ) {
			changed = false;
			for (var i = 0; i < sorted.size(); ++i) {
				final var entry = sorted.get(i);
				for (var j = i + 1; j < sorted.size(); ++j) {
					if (entry.dependencies.contains(sorted.get(j).name)) {
						sorted.remove(i);
						sorted.add(j, entry);
						changed = true;
						break;
					}
				}
				if (changed)
					break;
			}
		}

		// find the range of lines to replace (first to last field in this group)
		final var firstLine = group.getFirst().lines.getFirst();
		final var lastLine = group.getLast().lines.getLast();
		var startIdx = -1;
		var endIdx = -1;
		for (var i = bodyStart + 1; i < bodyEnd; ++i) {
			if (lines.get(i).equals(firstLine) && startIdx < 0)
				startIdx = i;
			if (lines.get(i).equals(lastLine))
				endIdx = i;
		}
		if (startIdx < 0 || endIdx < 0)
			return null;

		// expand startIdx backward to include annotations of the first field
		for (var f : group) {
			if (!f.lines.isEmpty() && f.lines.getFirst().equals(lines.get(startIdx)))
				break;
			for (var fl : f.lines) {
				for (var i = bodyStart + 1; i < startIdx; ++i) {
					if (lines.get(i).equals(fl))
						startIdx = i;
				}
			}
		}

		// build replacement
		final var replacement = new ArrayList<String>();
		var prevChunk = -1;
		for (var entry : sorted) {
			if (!replacement.isEmpty() && entry.chunk != prevChunk)
				replacement.add("");
			replacement.addAll(entry.lines);
			prevChunk = entry.chunk;
		}

		// idempotence check
		final var original = new ArrayList<>(lines.subList(startIdx, endIdx + 1));
		if (replacement.equals(original))
			return null;

		return new FixResult(startIdx, endIdx, replacement);
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
	private static boolean isInsideString(@Nonnull String line, int pos) {
		var quotes = 0;
		for (var i = 0; i < pos; ++i) {
			final var c = line.charAt(i);
			if (c == '"' && !isEscaped(line, i))
				++quotes;
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				break;
		}
		return quotes % 2 != 0;
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

	@CheckReturnValue
	@Nonnull
	private static List<FieldEntry> parseFields(
			@Nonnull List<String> lines,
			int bodyContentStart,
			int bodyContentEnd
	) {
		final var fields = new ArrayList<FieldEntry>();
		final var allFieldNames = new HashSet<String>();

		// first pass: collect field names
		for (var i = bodyContentStart; i < bodyContentEnd; ++i) {
			final var matcher = FIELD_PATTERN.matcher(lines.get(i));
			if (matcher.find())
				allFieldNames.add(matcher.group(2));
		}

		// second pass: build field entries with dependencies
		var i = bodyContentStart;
		while (i < bodyContentEnd) {
			final var line = lines.get(i);
			final var stripped = line.stripLeading();

			// skip blank lines, comments, and non-field lines
			if (stripped.isEmpty() || stripped.startsWith("//") || stripped.startsWith("/*")
					|| stripped.startsWith("*") || stripped.startsWith("{") || stripped.startsWith("}")) {
				++i;
				continue;
			}

			// skip enum constants, methods, constructors, inner classes, static/instance init blocks
			// check for ( only in the non-annotation part of the line to avoid
			// skipping fields with inline annotations like @SuppressWarnings("unchecked") int x;
			final var afterAnnotations = stripped.startsWith("@")
					? stripped.replaceAll("^(?:@\\w+(?:\\([^)]*\\))?\\s*)+", "")
					: stripped;
			if (stripped.startsWith("enum ") || stripped.startsWith("class ")
					|| stripped.startsWith("interface ") || stripped.startsWith("record ")
					|| afterAnnotations.contains("(") && !stripped.contains("=")
					|| stripped.equals("static {") || stripped.equals("{")) {
				++i;
				continue;
			}

			// try to match a field declaration
			final var matcher = FIELD_PATTERN.matcher(line);
			if (!matcher.find()) {
				// could be an annotation line -- look ahead
				if (stripped.startsWith("@")) {
					++i;
					continue;
				}
				++i;
				continue;
			}

			final var typeName = matcher.group(1);
			final var fieldName = matcher.group(2);
			final var chunk = fieldChunk(line);
			final var isStatic = fieldIsStatic(line);

			// collect all lines for this field (including annotations above)
			final var fieldLines = new ArrayList<String>();

			// look backward for annotation lines immediately above
			var annotStart = i;
			for (var j = i - 1; j >= bodyContentStart; --j) {
				final var prevStripped = lines.get(j).stripLeading();
				if (prevStripped.startsWith("@") || prevStripped.startsWith("//"))
					annotStart = j;
				else if (!prevStripped.isEmpty())
					break;
				else
					break;
			}
			for (var j = annotStart; j < i; ++j)
				fieldLines.add(lines.get(j));

			// find the end of the field declaration (tracking depth for multi-line initializers)
			final var fieldEnd = readFieldEnd(lines, i, bodyContentEnd);
			for (var j = i; j <= fieldEnd; ++j)
				fieldLines.add(lines.get(j));

			// extract dependencies: field names referenced in the initializer
			final var deps = new HashSet<String>();
			if (line.contains("=")) {
				final var initText = String.join(" ", fieldLines);
				for (var fn : allFieldNames) {
					if (!fn.equals(fieldName) && containsFieldWord(initText, fn))
						deps.add(fn);
				}
			}

			fields.add(new FieldEntry(fieldName, typeName, chunk, isStatic, fieldLines, deps));
			i = fieldEnd + 1;
		}
		return fields;
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
	private static int readFieldEnd(@Nonnull List<String> lines, int startIdx, int limit) {
		var parenDepth = 0;
		var braceDepth = 0;
		var inBlockComment = false;
		var inString = false;
		var inChar = false;
		for (var i = startIdx; i < limit; ++i) {
			final var line = lines.get(i);
			for (var j = 0; j < line.length(); ++j) {
				final var c = line.charAt(j);
				if (inBlockComment) {
					if (c == '*' && j + 1 < line.length() && line.charAt(j + 1) == '/') {
						inBlockComment = false;
						++j;
					}
					continue;
				}
				if (inString) {
					if (c == '"' && !isEscaped(line, j))
						inString = false;
				}
				else if (inChar) {
					if (c == '\'' && !isEscaped(line, j))
						inChar = false;
				}
				else if (c == '"')
					inString = true;
				else if (c == '\'')
					inChar = true;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '/')
					break;
				else if (c == '/' && j + 1 < line.length() && line.charAt(j + 1) == '*') {
					inBlockComment = true;
					++j;
				}
				else {
					switch (c) {
						case '(' -> ++parenDepth;
						case ')' -> --parenDepth;
						case '{' -> ++braceDepth;
						case '}' -> --braceDepth;
					}
					if (c == ';' && parenDepth == 0 && braceDepth == 0)
						return i;
				}
			}
		}
		return startIdx;
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
		if (enumOpenLine >= 0) {
			final var result = parseBlock(lines, enumOpenLine + 1);
			if (result == null || result.entries().size() < 2)
				return null;

			final var sorted = new ArrayList<>(result.entries());
			sorted.sort((a, b) -> a.name().compareToIgnoreCase(b.name()));

			final var first = sorted.getFirst();
			final var strippedLeading = new ArrayList<>(first.leadingLines());
			while (!strippedLeading.isEmpty() && strippedLeading.getFirst().isBlank())
				strippedLeading.removeFirst();
			sorted.set(0, new EnumEntry(first.name(), strippedLeading, first.contentLines()));

			final var replacement = buildReplacement(sorted, result.terminal());

			final var original = new ArrayList<>(lines.subList(result.blockStart(), result.blockEnd() + 1));
			if (replacement.equals(original))
				return null;

			return new FixResult(result.blockStart(), result.blockEnd(), replacement);
		}

		// try field ordering fix
		return fixFieldOrder(lines, lineIndex);
	}
}