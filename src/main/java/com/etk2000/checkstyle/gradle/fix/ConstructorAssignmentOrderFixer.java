package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for {@code ConstructorAssignmentOrderCheck}. Sorts {@code this.xxx = ...}
 * assignments within constructor and initializer bodies by group (simple, multi-line,
 * variable-dependent) then alphabetically within each group.
 */
class ConstructorAssignmentOrderFixer implements CheckstyleFixer {
	private record AssignmentEntry(
			@Nonnull String fieldName,
			@Nonnull List<String> lines,
			int group,
			int subGroup,
			@Nonnull Set<String> referencedFields
	) {}

	private record LocalVar(@Nonnull String name, @Nonnull String line, int declOrder) {}

	private static final int GROUP_MULTI = 1;
	private static final int GROUP_SIMPLE = 0;
	private static final int GROUP_VAR = 2;
	private static final Pattern LOCAL_VAR_PATTERN = Pattern.compile(
			"^\\s*(?:final\\s+)?(?:var|boolean|byte|char|double|float|int|long|short"
					+ "|[A-Z]\\w*(?:<[^>]*>)?)(?:\\[])*\\s+(\\w+)\\s*="
	);
	private static final Pattern THIS_ASSIGN_PATTERN = Pattern.compile(
			"^\\s*this\\.(\\w+)\\s*="
	);

	@CheckReturnValue
	private static int assignmentGroup(
			@Nonnull List<String> assignLines,
			@Nonnull String rhsText,
			@Nonnull Map<String, Integer> localVarOrder
	) {
		for (var varName : localVarOrder.keySet()) {
			if (containsWord(rhsText, varName))
				return GROUP_VAR;
		}
		return assignLines.size() > 1 ? GROUP_MULTI : GROUP_SIMPLE;
	}

	@CheckReturnValue
	private static int assignmentSubGroup(
			@Nonnull String rhsText,
			@Nonnull Map<String, Integer> localVarOrder
	) {
		var max = -1;
		for (var entry : localVarOrder.entrySet()) {
			if (containsWord(rhsText, entry.getKey()))
				max = Math.max(max, entry.getValue());
		}
		return max;
	}

	@CheckReturnValue
	private static boolean containsWord(@Nonnull String text, @Nonnull String word) {
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
	private static Set<String> extractFieldRefs(
			@Nonnull String rhsText,
			@Nonnull Set<String> assignedFieldNames
	) {
		final var refs = new LinkedHashSet<String>();
		for (var fieldName : assignedFieldNames) {
			if (containsWord(rhsText, "this." + fieldName))
				refs.add(fieldName);
		}
		return refs;
	}

	@CheckReturnValue
	@Nullable
	private static String extractLocalVarName(@Nonnull String line) {
		final var matcher = LOCAL_VAR_PATTERN.matcher(line);
		return matcher.find() ? matcher.group(1) : null;
	}

	@CheckReturnValue
	@Nullable
	private static String extractThisFieldName(@Nonnull String line) {
		final var matcher = THIS_ASSIGN_PATTERN.matcher(line);
		return matcher.find() ? matcher.group(1) : null;
	}

	@CheckReturnValue
	private static int findBodyEnd(@Nonnull List<String> lines, int afterOpen) {
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

	/**
	 * Scans backward from the line before {@code fromLine} to find the enclosing
	 * body opening brace. Skips the violation line itself because it may contain
	 * braces from anonymous classes or lambdas within an assignment. Also skips
	 * braces inside string literals by rejecting lines where the opening brace appears
	 * after an {@code =} (field/variable initializers).
	 */
	@CheckReturnValue
	private static int findBodyStart(@Nonnull List<String> lines, int fromLine) {
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
	private static boolean isEscaped(@Nonnull String line, int pos) {
		var backslashes = 0;
		for (var i = pos - 1; i >= 0 && line.charAt(i) == '\\'; --i)
			++backslashes;
		return backslashes % 2 != 0;
	}

	/**
	 * Checks if the character at {@code pos} is inside a string literal by
	 * counting unescaped double-quote characters before it on the same line.
	 * An odd count means the position is inside a string.
	 */
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

	/**
	 * Reads a (possibly multi-line) statement starting at {@code startIdx},
	 * tracking brace and paren depth. Returns the end index (inclusive).
	 */
	@CheckReturnValue
	private static int readStatementEnd(@Nonnull List<String> lines, int startIdx) {
		var parenDepth = 0;
		var braceDepth = 0;
		var inBlockComment = false;
		var inString = false;
		var inChar = false;
		for (var i = startIdx; i < lines.size(); ++i) {
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
	private static String rhsText(@Nonnull List<String> assignLines) {
		final var first = assignLines.getFirst();
		final var eqIdx = first.indexOf('=');
		if (eqIdx < 0)
			return String.join("\n", assignLines);
		final var sb = new StringBuilder(first.substring(eqIdx + 1));
		for (var i = 1; i < assignLines.size(); ++i)
			sb.append('\n').append(assignLines.get(i));
		return sb.toString();
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var bodyStartLine = findBodyStart(lines, lineIndex);
		if (bodyStartLine < 0)
			return null;

		final var bodyEndLine = findBodyEnd(lines, bodyStartLine + 1);
		if (bodyEndLine < 0)
			return null;

		// first pass: collect local var names and field names
		final var localVarOrder = new LinkedHashMap<String, Integer>();
		final var localVars = new ArrayList<LocalVar>();
		final var assignedFieldNames = new LinkedHashSet<String>();
		var varDeclCount = 0;

		for (var i = bodyStartLine + 1; i < bodyEndLine; ++i) {
			final var line = lines.get(i);
			final var varName = extractLocalVarName(line);
			if (varName != null) {
				localVarOrder.put(varName, varDeclCount);
				localVars.add(new LocalVar(varName, line, varDeclCount));
				++varDeclCount;
				continue;
			}
			final var fieldName = extractThisFieldName(line);
			if (fieldName != null) {
				assignedFieldNames.add(fieldName);
				i = readStatementEnd(lines, i);
			}
		}

		if (assignedFieldNames.size() < 2)
			return null;

		// second pass: build assignment entries
		final var assignments = new ArrayList<AssignmentEntry>();
		var firstAssignLine = -1;
		var lastAssignLine = -1;

		for (var i = bodyStartLine + 1; i < bodyEndLine; ++i) {
			final var fieldName = extractThisFieldName(lines.get(i));
			if (fieldName == null)
				continue;

			final var endIdx = readStatementEnd(lines, i);
			final var assignLines = new ArrayList<>(lines.subList(i, endIdx + 1));
			final var rhs = rhsText(assignLines);
			final var group = assignmentGroup(assignLines, rhs, localVarOrder);
			final var subGroup = group == GROUP_VAR ? assignmentSubGroup(rhs, localVarOrder) : -1;
			final var fieldRefs = extractFieldRefs(rhs, assignedFieldNames);

			assignments.add(new AssignmentEntry(fieldName, assignLines, group, subGroup, fieldRefs));

			if (firstAssignLine < 0)
				firstAssignLine = i;
			lastAssignLine = endIdx;
			i = endIdx;
		}

		if (assignments.size() < 2)
			return null;

		// also expand range to include local vars that are between first and last assignment
		for (var lv : localVars) {
			for (var i = bodyStartLine + 1; i < bodyEndLine; ++i) {
				if (lines.get(i).equals(lv.line) && i >= firstAssignLine && i <= lastAssignLine) {
					firstAssignLine = Math.min(firstAssignLine, i);
					lastAssignLine = Math.max(lastAssignLine, i);
				}
			}
		}

		// also include blank lines within the range
		// find the actual first non-blank content before first assignment (local vars)
		for (var i = firstAssignLine - 1; i > bodyStartLine; --i) {
			final var line = lines.get(i);
			if (line.isBlank())
				continue;
			final var vn = extractLocalVarName(line);
			if (vn != null && localVarOrder.containsKey(vn))
				firstAssignLine = i;
			else
				break;
		}

		// sort assignments
		final var sorted = new ArrayList<>(assignments);
		sorted.sort(Comparator
				.comparingInt(AssignmentEntry::group)
				.thenComparingInt(AssignmentEntry::subGroup)
				.thenComparing(AssignmentEntry::fieldName, String.CASE_INSENSITIVE_ORDER));

		// apply dependency adjustments: if A references this.B, A must come after B
		// guard against circular dependencies with max iterations
		var maxIter = sorted.size() * sorted.size();
		for (var changed = true; changed && --maxIter >= 0; ) {
			changed = false;
			for (var i = 0; i < sorted.size(); ++i) {
				final var entry = sorted.get(i);
				for (var j = i + 1; j < sorted.size(); ++j) {
					if (entry.referencedFields.contains(sorted.get(j).fieldName)) {
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

		// build replacement
		final var replacement = new ArrayList<String>();
		final var placedVars = new LinkedHashSet<String>();
		var prevGroup = -1;
		var prevSubGroup = -1;

		for (var entry : sorted) {
			// blank line between groups or between var sub-groups
			if (!replacement.isEmpty()
					&& (entry.group != prevGroup
					|| (entry.group == GROUP_VAR && entry.subGroup != prevSubGroup)))
				replacement.add("");

			// place local vars needed by this var-group entry
			if (entry.group == GROUP_VAR) {
				final var rhs = rhsText(entry.lines);
				for (var lv : localVars) {
					if (!placedVars.contains(lv.name) && containsWord(rhs, lv.name)) {
						placedVars.add(lv.name);
						replacement.add(lv.line);
					}
				}
			}

			replacement.addAll(entry.lines);
			prevGroup = entry.group;
			prevSubGroup = entry.subGroup;
		}

		// place any remaining local vars that weren't used by any assignment
		for (var lv : localVars) {
			if (!placedVars.contains(lv.name))
				replacement.add(lv.line);
		}

		// idempotence check
		final var original = new ArrayList<>(lines.subList(firstAssignLine, lastAssignLine + 1));
		if (replacement.equals(original))
			return null;

		return new FixResult(firstAssignLine, lastAssignLine, replacement);
	}
}