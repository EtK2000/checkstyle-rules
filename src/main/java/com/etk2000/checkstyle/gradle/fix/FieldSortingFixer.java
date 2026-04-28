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
			@Nonnull List<String> annotations,
			@Nonnull String name,
			@Nonnull String typeName,
			@Nonnull List<List<String>> typeArgAnnotations,
			int chunk,
			boolean isStatic,
			@Nonnull List<String> lines,
			@Nonnull Set<String> dependencies
	) {}

	private record ParseResult(@Nonnull List<EnumEntry> entries, int blockStart, int blockEnd, @Nonnull String terminal) {}

	private static final int MAX_LINE_LENGTH = 120;
	private static final Pattern ANNOTATION_PREFIX_PATTERN = Pattern.compile(
			"^(?:@\\w+(?:\\([^()]*(?:\\([^()]*\\)[^()]*)*\\))?\\s*)+"
	);
	private static final Pattern FIELD_PATTERN = Pattern.compile(
			"^\\s*(?:(?:@\\w+(?:\\([^()]*(?:\\([^()]*\\)[^()]*)*\\))?\\s+)*)"
					+ "(?:(?:public|private|protected|static|final|transient|volatile)\\s+)*"
					+ "((?:boolean|byte|char|double|float|int|long|short|(?:\\w+\\.)*[A-Z]\\w*)(?:<[^>]*>)?(?:\\[\\])*)"
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
	private static boolean canConsolidate(@Nonnull FieldEntry entry) {
		final var declLine = entry.lines.getLast();
		final var stripped = declLine.stripLeading();
		return !stripped.contains("=") && entry.lines.size() <= 2
				&& findTrailingCommentStart(declLine) < 0;
	}

	@CheckReturnValue
	private static boolean canMergeWith(@Nonnull FieldEntry base, @Nonnull FieldEntry candidate) {
		return candidate.chunk == base.chunk
				&& candidate.typeName.equals(base.typeName)
				&& candidate.annotations.equals(base.annotations)
				&& candidate.typeArgAnnotations.equals(base.typeArgAnnotations)
				&& canConsolidate(candidate)
				&& candidate.dependencies.isEmpty()
				&& base.dependencies.isEmpty();
	}

	@CheckReturnValue
	private static int compareAnnotations(@Nonnull List<String> a, @Nonnull List<String> b) {
		for (var i = 0; i < Math.min(a.size(), b.size()); ++i) {
			final var cmp = a.get(i).compareToIgnoreCase(b.get(i));
			if (cmp != 0)
				return cmp;
		}
		return Integer.compare(a.size(), b.size());
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
	private static int compareTypeArgAnnotations(
			@Nonnull List<List<String>> a, @Nonnull List<List<String>> b
	) {
		for (var i = 0; i < Math.min(a.size(), b.size()); ++i) {
			final var cmp = compareAnnotations(a.get(i), b.get(i));
			if (cmp != 0)
				return cmp;
		}
		return Integer.compare(a.size(), b.size());
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
	@Nonnull
	private static List<String> extractAllFieldNames(@Nonnull FieldEntry entry) {
		final var declLine = entry.lines.getLast();
		final var matcher = FIELD_PATTERN.matcher(declLine);
		if (!matcher.find())
			return List.of(entry.name);
		final var afterType = matcher.end(1);
		final var names = new ArrayList<String>();
		var pos = afterType;
		while (pos < declLine.length()) {
			while (pos < declLine.length() && !Character.isJavaIdentifierStart(declLine.charAt(pos)))
				++pos;
			if (pos >= declLine.length())
				break;
			final var start = pos;
			while (pos < declLine.length() && Character.isJavaIdentifierPart(declLine.charAt(pos)))
				++pos;
			names.add(declLine.substring(start, pos));
			while (pos < declLine.length() && declLine.charAt(pos) != ',' && declLine.charAt(pos) != ';')
				++pos;
			if (pos >= declLine.length() || declLine.charAt(pos) == ';')
				break;
			++pos;
		}
		return names.isEmpty() ? List.of(entry.name) : names;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> extractAnnotationKeys(@Nonnull List<String> fieldLines) {
		final var keys = new ArrayList<String>();
		var angleDepth = 0;
		var inTextBlock = false;
		for (var line : fieldLines) {
			final var stripped = line.stripLeading();
			var pos = 0;
			var inString = false;
			var inChar = false;
			while (pos < stripped.length()) {
				final var c = stripped.charAt(pos);
				if (inTextBlock) {
					if (c == '"' && !isEscaped(stripped, pos) && pos + 2 < stripped.length()
							&& stripped.charAt(pos + 1) == '"' && stripped.charAt(pos + 2) == '"') {
						inTextBlock = false;
						pos += 3;
					}
					else
						++pos;
					continue;
				}
				if (inString) {
					if (c == '"' && !isEscaped(stripped, pos))
						inString = false;
					++pos;
					continue;
				}
				if (inChar) {
					if (c == '\'' && !isEscaped(stripped, pos))
						inChar = false;
					++pos;
					continue;
				}
				if (c == '"') {
					if (pos + 2 < stripped.length()
							&& stripped.charAt(pos + 1) == '"' && stripped.charAt(pos + 2) == '"') {
						inTextBlock = true;
						pos += 3;
					}
					else {
						inString = true;
						++pos;
					}
					continue;
				}
				if (c == '\'') {
					inChar = true;
					++pos;
					continue;
				}
				if (c == '/' && pos + 1 < stripped.length() && stripped.charAt(pos + 1) == '/')
					break;
				if (c == '/' && pos + 1 < stripped.length() && stripped.charAt(pos + 1) == '*') {
					final var end = stripped.indexOf("*/", pos + 2);
					pos = end >= 0 ? end + 2 : stripped.length();
					continue;
				}
				if (c == '<') {
					++angleDepth;
					++pos;
					continue;
				}
				if (c == '>') {
					--angleDepth;
					++pos;
					continue;
				}
				if (c == '=' && angleDepth <= 0)
					break;
				if (c != '@' || angleDepth > 0) {
					++pos;
					continue;
				}
				final var atIdx = pos;
				var nameEnd = atIdx + 1;
				while (nameEnd < stripped.length()
						&& (Character.isJavaIdentifierPart(stripped.charAt(nameEnd)) || stripped.charAt(nameEnd) == '.'))
					++nameEnd;
				if (nameEnd <= atIdx + 1) {
					pos = atIdx + 1;
					continue;
				}
				final var fullName = stripped.substring(atIdx + 1, nameEnd);
				final var dotIdx = fullName.lastIndexOf('.');
				final var simpleName = dotIdx >= 0 ? fullName.substring(dotIdx + 1) : fullName;
				if (simpleName.isEmpty() || !Character.isUpperCase(simpleName.charAt(0))) {
					pos = nameEnd;
					continue;
				}
				final var sb = new StringBuilder(simpleName);
				var afterName = nameEnd;
				while (afterName < stripped.length() && stripped.charAt(afterName) <= ' ')
					++afterName;
				if (afterName < stripped.length() && stripped.charAt(afterName) == '(') {
					final var paramsText = extractParenBalanced(stripped, afterName);
					if (paramsText != null && !paramsText.equals("()")) {
						final var inner = paramsText.substring(1, paramsText.length() - 1).strip();
						if (!inner.contains("="))
							sb.append("(value=").append(inner).append(')');
						else
							sb.append(paramsText);
					}
					pos = afterName + (paramsText != null ? paramsText.length() : 1);
				}
				else
					pos = nameEnd;
				keys.add(sb.toString());
			}
		}
		keys.sort(String.CASE_INSENSITIVE_ORDER);
		return keys;
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
	@Nonnull
	private static String extractIndent(@Nonnull String line) {
		var idx = 0;
		while (idx < line.length() && (line.charAt(idx) == '\t' || line.charAt(idx) == ' '))
			++idx;
		return line.substring(0, idx);
	}

	@CheckReturnValue
	@Nullable
	private static String extractParenBalanced(@Nonnull String text, int openParen) {
		var depth = 0;
		var inString = false;
		var inChar = false;
		for (var i = openParen; i < text.length(); ++i) {
			final var c = text.charAt(i);
			if (inString) {
				if (c == '"' && !isEscaped(text, i))
					inString = false;
			}
			else if (inChar) {
				if (c == '\'' && !isEscaped(text, i))
					inChar = false;
			}
			else if (c == '"')
				inString = true;
			else if (c == '\'')
				inChar = true;
			else if (c == '(')
				++depth;
			else if (c == ')') {
				--depth;
				if (depth == 0)
					return text.substring(openParen, i + 1);
			}
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static List<List<String>> extractTypeArgAnnotationKeys(@Nonnull String typeName) {
		final var angleStart = typeName.indexOf('<');
		final var angleEnd = typeName.lastIndexOf('>');
		if (angleStart < 0 || angleEnd <= angleStart)
			return List.of();

		final var inner = typeName.substring(angleStart + 1, angleEnd);

		final var args = new ArrayList<String>();
		var depth = 0;
		var start = 0;
		for (var i = 0; i < inner.length(); ++i) {
			final var c = inner.charAt(i);
			if (c == '(' || c == '<')
				++depth;
			else if (c == ')' || c == '>')
				--depth;
			else if (c == ',' && depth == 0) {
				args.add(inner.substring(start, i).strip());
				start = i + 1;
			}
		}
		args.add(inner.substring(start).strip());

		final var result = new ArrayList<List<String>>();
		for (var arg : args) {
			final var keys = new ArrayList<String>();
			var pos = 0;
			while (pos < arg.length()) {
				if (arg.charAt(pos) != '@') {
					++pos;
					continue;
				}
				var nameEnd = pos + 1;
				while (nameEnd < arg.length()
						&& (Character.isJavaIdentifierPart(arg.charAt(nameEnd)) || arg.charAt(nameEnd) == '.'))
					++nameEnd;
				if (nameEnd <= pos + 1) {
					++pos;
					continue;
				}
				final var fullName = arg.substring(pos + 1, nameEnd);
				final var dotIdx = fullName.lastIndexOf('.');
				final var simpleName = dotIdx >= 0 ? fullName.substring(dotIdx + 1) : fullName;
				if (simpleName.isEmpty() || !Character.isUpperCase(simpleName.charAt(0))) {
					pos = nameEnd;
					continue;
				}
				final var sb = new StringBuilder(simpleName);
				var afterName = nameEnd;
				while (afterName < arg.length() && arg.charAt(afterName) <= ' ')
					++afterName;
				if (afterName < arg.length() && arg.charAt(afterName) == '(') {
					final var paramsText = extractParenBalanced(arg, afterName);
					if (paramsText != null && !paramsText.equals("()")) {
						final var paramInner = paramsText.substring(1, paramsText.length() - 1).strip();
						if (!paramInner.contains("="))
							sb.append("(value=").append(paramInner).append(')');
						else
							sb.append(paramsText);
					}
					pos = afterName + (paramsText != null ? paramsText.length() : 1);
				}
				else
					pos = nameEnd;
				keys.add(sb.toString());
			}
			keys.sort(String.CASE_INSENSITIVE_ORDER);
			result.add(keys);
		}
		return result;
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
		var inTextBlock = false;
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
				if (inTextBlock) {
					if (c == '"' && !isEscaped(line, j) && j + 2 < line.length()
							&& line.charAt(j + 1) == '"' && line.charAt(j + 2) == '"') {
						inTextBlock = false;
						j += 2;
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
				else if (c == '"') {
					if (j + 2 < line.length() && line.charAt(j + 1) == '"' && line.charAt(j + 2) == '"') {
						inTextBlock = true;
						j += 2;
					}
					else
						inString = true;
				}
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
				.thenComparing(FieldEntry::annotations, FieldSortingFixer::compareAnnotations)
				.thenComparing(FieldEntry::typeArgAnnotations, FieldSortingFixer::compareTypeArgAnnotations)
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

		// build replacement with consolidation of same-type same-annotation uninitialized fields
		final var replacement = new ArrayList<String>();
		var prevChunk = -1;
		var i2 = 0;
		while (i2 < sorted.size()) {
			final var entry = sorted.get(i2);
			if (!replacement.isEmpty() && entry.chunk != prevChunk)
				replacement.add("");
			prevChunk = entry.chunk;

			if (!canConsolidate(entry)) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}

			final var groupNames = new ArrayList<>(extractAllFieldNames(entry));
			var j2 = i2 + 1;
			while (j2 < sorted.size() && canMergeWith(entry, sorted.get(j2))) {
				groupNames.addAll(extractAllFieldNames(sorted.get(j2)));
				++j2;
			}
			groupNames.sort(String.CASE_INSENSITIVE_ORDER);

			if (groupNames.size() == 1) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}

			final var declLine = entry.lines.getLast();
			final var nameStart = declLine.indexOf(entry.name);
			if (nameStart < 0) {
				replacement.addAll(entry.lines);
				++i2;
				continue;
			}
			final var prefix = declLine.substring(0, nameStart);
			final var suffix = ";";
			for (var k = 0; k < entry.lines.size() - 1; ++k)
				replacement.add(entry.lines.get(k));

			final var merged = prefix + String.join(", ", groupNames) + suffix;
			if (tabExpandedLength(merged) <= MAX_LINE_LENGTH)
				replacement.add(merged);
			else {
				final var baseIndent = extractIndent(declLine);
				final var contIndent = baseIndent + "\t\t";
				var line = new StringBuilder(prefix + groupNames.getFirst());
				for (var k = 1; k < groupNames.size(); ++k) {
					final var isLast = k == groupNames.size() - 1;
					final var withName = line + ", " + groupNames.get(k);
					if (tabExpandedLength(withName + (isLast ? suffix : ",")) > MAX_LINE_LENGTH) {
						replacement.add(line + ",");
						line = new StringBuilder(contIndent + groupNames.get(k));
					}
					else
						line = new StringBuilder(withName);
					if (isLast)
						replacement.add(line + suffix);
				}
			}
			i2 = j2;
		}

		// idempotence check
		final var original = new ArrayList<>(lines.subList(startIdx, endIdx + 1));
		if (replacement.equals(original))
			return null;

		// safety: verify no structural lines were lost (e.g. fields with nested generics
		// that FIELD_PATTERN couldn't parse)
		if (hasUnaccountedLines(original, replacement))
			return null;

		return new FixResult(startIdx, endIdx, replacement);
	}

	@CheckReturnValue
	private static boolean hasUnaccountedLines(@Nonnull List<String> original, @Nonnull List<String> replacement) {
		final var replacementSet = new HashSet<>(replacement);
		for (var line : original) {
			final var stripped = line.stripLeading();
			if (stripped.isEmpty() || stripped.startsWith("//") || stripped.startsWith("/*")
					|| stripped.startsWith("*") || stripped.startsWith("@"))
				continue;
			if (!replacementSet.contains(line) && !isSubsumedByConsolidation(stripped, replacement))
				return true;
		}
		return false;
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

	private static boolean isSubsumedByConsolidation(@Nonnull String stripped, @Nonnull List<String> replacement) {
		final var matcher = FIELD_PATTERN.matcher(stripped);
		if (!matcher.find())
			return false;
		final var fieldName = matcher.group(2);
		for (var rLine : replacement) {
			if (containsFieldWord(rLine, fieldName))
				return true;
		}
		return false;
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
			var afterAnnotations = stripped.startsWith("@")
					? ANNOTATION_PREFIX_PATTERN.matcher(stripped).replaceAll("")
					: stripped;
			final var angleIdx = afterAnnotations.indexOf('<');
			if (angleIdx >= 0) {
				final var closeIdx = afterAnnotations.indexOf('>', angleIdx);
				if (closeIdx >= 0)
					afterAnnotations = afterAnnotations.substring(0, angleIdx) + afterAnnotations.substring(closeIdx + 1);
			}
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

			final var annotations = extractAnnotationKeys(fieldLines);
			final var typeArgAnnotations = extractTypeArgAnnotationKeys(typeName);
			fields.add(new FieldEntry(annotations, fieldName, typeName, typeArgAnnotations, chunk, isStatic, fieldLines, deps));
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
		var inTextBlock = false;
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
				if (inTextBlock) {
					if (c == '"' && !isEscaped(line, j) && j + 2 < line.length()
							&& line.charAt(j + 1) == '"' && line.charAt(j + 2) == '"') {
						inTextBlock = false;
						j += 2;
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
				else if (c == '"') {
					if (j + 2 < line.length() && line.charAt(j + 1) == '"' && line.charAt(j + 2) == '"') {
						inTextBlock = true;
						j += 2;
					}
					else
						inString = true;
				}
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
	private static int tabExpandedLength(@Nonnull String line) {
		var len = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (line.charAt(i) == '\t')
				len += 4 - (len % 4);
			else
				++len;
		}
		return len;
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