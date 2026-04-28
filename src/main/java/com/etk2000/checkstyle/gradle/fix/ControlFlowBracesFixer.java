package com.etk2000.checkstyle.gradle.fix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Fixer for do-while violations of {@code ControlFlowBracesCheck}.
 * Determines the formatting tier from line content and produces the
 * correct form:
 * <ul>
 *     <li>Tier 2: body on do line, while on next line</li>
 *     <li>Tier 3: body on own line, while after</li>
 * </ul>
 * Non-do-while violations are skipped (returns {@code null}).
 */
class ControlFlowBracesFixer implements CheckstyleFixer {
	private static final Pattern SIMPLE_BODY_PATTERN = Pattern.compile(
			"^(\\+\\+\\w+|--\\w+|\\w+\\+\\+|\\w+--|" +
					"\\w+\\s*(?:[+\\-*/%&|^]|<<|>>>?)=\\s*[^+\\-*/%&|^<>]+|" +
					"\\w+\\s*=\\s*[^+\\-*/%&|^<>]+|" +
					"[\\w.]+\\([^)]*\\))$"
	);

	@Nullable
	private static FixResult buildTierResult(
			@Nonnull String bodyText,
			@Nonnull String whileClause,
			int startLine,
			int endLine,
			@Nonnull String indent
	) {
		// body with no real statement (comment-only, possibly with empty `;`); refuse to fix
		if (isCommentOnly(bodyText))
			return null;

		// no-comment view to decide if `;` is needed; isCommentOnly already filtered
		// out unterminated cases, but be explicit defensively in case that invariant changes
		final var withoutBlocks = stripBlockComments(bodyText);
		if (withoutBlocks == null)
			return null;
		final var bodyNoComment = stripTrailingLineComment(withoutBlocks).strip();

		// non-empty body without a real `;` is malformed (or disjoint source); refuse to fix
		if (!bodyNoComment.isEmpty() && !bodyNoComment.endsWith(";"))
			return null;

		final var tier = determineTierFromText(bodyText);
		final var result = new ArrayList<String>();
		switch (tier) {
			case 2 -> {
				result.add(indent + "do " + bodyText);
				result.add(indent + whileClause);
			}
			default -> {
				// tier-3 needs an explicit `;` if the body's no-comment view doesn't supply one
				final var body = bodyNoComment.endsWith(";") ? bodyText : bodyText + ";";
				result.add(indent + "do");
				result.add(indent + "\t" + body);
				result.add(indent + whileClause);
			}
		}
		return new FixResult(startLine, endLine, result);
	}

	@CheckReturnValue
	private static int determineTierFromText(@Nonnull String bodyText) {
		final var text = bodyText.endsWith(";")
				? bodyText.substring(0, bodyText.length() - 1).strip()
				: bodyText.strip();

		if (text.isEmpty())
			return 3;

		if (text.contains(")."))
			return 3;

		if (!SIMPLE_BODY_PATTERN.matcher(text).matches())
			return 3;

		return 2;
	}

	@CheckReturnValue
	@Nonnull
	private static String extractIndent(@Nonnull String line) {
		var i = 0;
		while (i < line.length() && (line.charAt(i) == '\t' || line.charAt(i) == ' '))
			++i;
		return line.substring(0, i);
	}

	/**
	 * Finds the line where the body statement ends by tracking paren/brace depth
	 * until a semicolon is found at depth 0.
	 */
	@CheckReturnValue
	private static int findStatementEnd(@Nonnull List<String> lines, int startIdx) {
		var parenDepth = 0;
		var braceDepth = 0;
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
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
		return -1;
	}

	@CheckReturnValue
	private static int findTrailingComment(@Nonnull String line) {
		var inString = false;
		var inChar = false;
		var inBlockComment = false;
		for (var i = 0; i < line.length(); ++i) {
			final var c = line.charAt(i);
			if (inBlockComment) {
				if (c == '*' && i + 1 < line.length() && line.charAt(i + 1) == '/') {
					inBlockComment = false;
					++i;
				}
				continue;
			}
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
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '*') {
				inBlockComment = true;
				++i;
			}
			else if (c == '/' && i + 1 < line.length() && line.charAt(i + 1) == '/')
				return i;
		}
		return -1;
	}

	@CheckReturnValue
	private static int findWhileInText(@Nonnull String text) {
		var idx = text.lastIndexOf("while");
		while (idx > 0) {
			var before = idx - 1;
			while (before >= 0 && (text.charAt(before) == ' ' || text.charAt(before) == '\t'))
				--before;
			if (before >= 0 && text.charAt(before) == ';')
				return idx;
			idx = text.lastIndexOf("while", idx - 1);
		}
		return -1;
	}

	@CheckReturnValue
	private static int findWhileLine(@Nonnull List<String> lines, int doLine) {
		for (var i = doLine + 1; i < lines.size(); ++i) {
			if (lines.get(i).stripLeading().startsWith("while"))
				return i;
		}
		return -1;
	}

	@Nullable
	private static FixResult fixBracedBody(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent
	) {
		var closeBraceLine = -1;
		for (var i = lineIndex + 1; i < lines.size(); ++i) {
			if (lines.get(i).stripLeading().startsWith("}")) {
				closeBraceLine = i;
				break;
			}
		}
		if (closeBraceLine < 0)
			return null;

		final var bodyLines = new ArrayList<String>();
		for (var i = lineIndex + 1; i < closeBraceLine; ++i)
			bodyLines.add(lines.get(i));

		// empty body has no statement to keep; emitting `do\n\twhile(...)` would be invalid Java
		if (bodyLines.isEmpty())
			return null;

		final var closeLineStripped = lines.get(closeBraceLine).stripLeading();
		final var whileIdx = closeLineStripped.indexOf("while");
		if (whileIdx < 0) {
			final var nextWhile = findWhileLine(lines, closeBraceLine);
			if (nextWhile < 0)
				return null;
			final var whileClauseFromNext = lines.get(nextWhile).stripLeading();
			if (bodyLines.size() == 1) {
				final var bodyStripped = bodyLines.getFirst().stripLeading();
				if (isVariableDeclaration(bodyStripped))
					return null;
				return buildTierResult(bodyStripped, whileClauseFromNext, lineIndex, nextWhile, indent);
			}
			final var r = new ArrayList<String>();
			r.add(indent + "do");
			r.addAll(bodyLines);
			r.add(indent + whileClauseFromNext);
			return new FixResult(lineIndex, nextWhile, r);
		}
		final var whileClause = closeLineStripped.substring(whileIdx);

		if (bodyLines.size() == 1) {
			final var bodyStripped = bodyLines.getFirst().stripLeading();
			if (isVariableDeclaration(bodyStripped))
				return null;
			return buildTierResult(bodyStripped, whileClause, lineIndex, closeBraceLine, indent);
		}

		final var result = new ArrayList<String>();
		result.add(indent + "do");
		result.addAll(bodyLines);
		result.add(indent + whileClause);
		return new FixResult(lineIndex, closeBraceLine, result);
	}

	@Nullable
	private static FixResult fixMissingBraces(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent
	) {
		final var whileLine = findWhileLine(lines, lineIndex);
		if (whileLine < 0)
			return null;

		final var result = new ArrayList<String>();
		result.add(indent + "do {");
		for (var i = lineIndex + 1; i < whileLine; ++i)
			result.add(lines.get(i));
		result.add(indent + "} " + lines.get(whileLine).stripLeading());
		return new FixResult(lineIndex, whileLine, result);
	}

	/**
	 * Fixes a non-do-while control flow where the opening brace is on its own line.
	 * Removes the brace lines and keeps keyword + body (same as unnecessary braces).
	 */
	@Nullable
	private static FixResult fixNonDoWhileBraceOnOwnLine(
			@Nonnull List<String> lines,
			int lineIndex,
			int braceLine,
			@Nonnull String indent
	) {
		// find body line after the { line
		var bodyLine = braceLine + 1;
		while (bodyLine < lines.size() && lines.get(bodyLine).isBlank())
			++bodyLine;
		if (bodyLine >= lines.size())
			return null;

		// check if body is a variable declaration (braces needed for scope)
		if (isVariableDeclaration(lines.get(bodyLine).stripLeading()))
			return null;

		// if the brace line has a comment, refuse to fix (would lose the comment)
		if (findTrailingComment(lines.get(braceLine)) >= 0)
			return null;

		// find the closing } line
		var closeBrace = bodyLine + 1;
		while (closeBrace < lines.size() && lines.get(closeBrace).isBlank())
			++closeBrace;
		if (closeBrace >= lines.size())
			return null;

		final var closeStripped = lines.get(closeBrace).stripLeading();
		if (!closeStripped.startsWith("}"))
			return null;

		final var result = new ArrayList<String>();
		result.add(lines.get(lineIndex));
		result.add(lines.get(bodyLine));

		if (closeStripped.length() > 1) {
			final var afterBrace = closeStripped.substring(1).stripLeading();
			if (!afterBrace.isEmpty())
				result.add(indent + afterBrace);
		}

		return new FixResult(lineIndex, closeBrace, result);
	}

	/**
	 * Fixes a non-do-while control flow statement with a multi-line braceless body
	 * by wrapping the body in braces.
	 */
	@Nullable
	private static FixResult fixNonDoWhileMissingBraces(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent
	) {
		// find the body start: the next non-blank line after the keyword
		var bodyStart = lineIndex + 1;
		while (bodyStart < lines.size() && lines.get(bodyStart).isBlank())
			++bodyStart;
		if (bodyStart >= lines.size())
			return null;

		// find the body end using semicolon tracking
		final var bodyEnd = findStatementEnd(lines, bodyStart);
		if (bodyEnd < 0 || bodyEnd == bodyStart)
			return null;

		// build replacement: keyword line + { + body lines + }
		// if keyword line has a trailing comment, insert { before the comment
		final var keywordLine = lines.get(lineIndex);
		final var commentIdx = findTrailingComment(keywordLine);
		final var result = new ArrayList<String>();
		if (commentIdx >= 0)
			result.add(keywordLine.substring(0, commentIdx).stripTrailing() + " { " + keywordLine.substring(commentIdx));
		else
			result.add(keywordLine + " {");
		for (var i = bodyStart; i <= bodyEnd; ++i)
			result.add(lines.get(i));
		result.add(indent + "}");
		return new FixResult(lineIndex, bodyEnd, result);
	}

	/**
	 * Fixes a non-do-while control flow statement with unnecessary braces on a
	 * single-line body by removing the braces.
	 */
	@Nullable
	private static FixResult fixNonDoWhileUnnecessaryBraces(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent
	) {
		final var line = lines.get(lineIndex);
		final var braceIdx = line.lastIndexOf('{');
		if (braceIdx < 0)
			return null;

		final var keywordLine = line.substring(0, braceIdx).stripTrailing();

		// find the single body line
		var bodyLine = lineIndex + 1;
		while (bodyLine < lines.size() && lines.get(bodyLine).isBlank())
			++bodyLine;
		if (bodyLine >= lines.size())
			return null;

		// if the body declares a variable, braces are needed for scope
		if (isVariableDeclaration(lines.get(bodyLine).stripLeading()))
			return null;

		// find the closing brace line
		var closeBrace = bodyLine + 1;
		while (closeBrace < lines.size() && lines.get(closeBrace).isBlank())
			++closeBrace;
		if (closeBrace >= lines.size())
			return null;

		final var closeStripped = lines.get(closeBrace).stripLeading();
		if (!closeStripped.startsWith("}"))
			return null;

		final var result = new ArrayList<String>();
		result.add(keywordLine);
		result.add(lines.get(bodyLine));

		// if close brace has "else" after it, keep the else part on its own line
		if (closeStripped.length() > 1) {
			final var afterBrace = closeStripped.substring(1).stripLeading();
			if (!afterBrace.isEmpty())
				result.add(indent + afterBrace);
		}

		return new FixResult(lineIndex, closeBrace, result);
	}

	@Nullable
	private static FixResult fixOnDoLine(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String line,
			@Nonnull String indent
	) {
		final var afterDo = line.substring(line.indexOf("do") + 2).stripLeading();
		final var whileIdx = findWhileInText(afterDo);

		if (whileIdx >= 0)
			return fixOnDoLineWhileSameLine(afterDo, whileIdx, lineIndex, indent);

		return fixOnDoLineWhileNextLine(lines, afterDo, lineIndex, indent);
	}

	@Nullable
	private static FixResult fixOnDoLineWhileNextLine(
			@Nonnull List<String> lines,
			@Nonnull String afterDo,
			int lineIndex,
			@Nonnull String indent
	) {
		final var bodyText = afterDo.stripTrailing();
		if (bodyText.isEmpty())
			return null;
		final var whileLine = findWhileLine(lines, lineIndex);
		if (whileLine < 0)
			return null;
		final var whileClause = lines.get(whileLine).stripLeading();
		return buildTierResult(bodyText, whileClause, lineIndex, whileLine, indent);
	}

	@Nullable
	private static FixResult fixOnDoLineWhileSameLine(
			@Nonnull String afterDo,
			int whileIdx,
			int lineIndex,
			@Nonnull String indent
	) {
		final var bodyText = afterDo.substring(0, whileIdx).stripTrailing();
		final var whileClause = afterDo.substring(whileIdx);
		return buildTierResult(bodyText, whileClause, lineIndex, lineIndex, indent);
	}

	@Nullable
	private static FixResult fixOwnLine(
			@Nonnull List<String> lines,
			int lineIndex,
			@Nonnull String indent
	) {
		if (lineIndex + 1 >= lines.size())
			return null;

		final var bodyText = lines.get(lineIndex + 1).stripLeading();
		if (bodyText.isEmpty())
			return null;

		final var whileLine = findWhileLine(lines, lineIndex);
		if (whileLine < 0)
			return null;

		if (whileLine > lineIndex + 2)
			return fixMissingBraces(lines, lineIndex, indent);

		final var whileClause = lines.get(whileLine).stripLeading();
		return buildTierResult(bodyText, whileClause, lineIndex, whileLine, indent);
	}

	/**
	 * Returns whether the body has no real statement (comment-only, possibly with a
	 * trailing empty {@code ;}). Catches line-comment-only, block-comment-only, mixed
	 * comments, block-comment-with-empty-statement, and unterminated block comments
	 * (whether at the start or mid-line).
	 */
	@CheckReturnValue
	private static boolean isCommentOnly(@Nonnull String bodyText) {
		final var stripped = bodyText.strip();
		if (stripped.isEmpty())
			return false;

		// strip completed block comments; null means an unterminated /* was found,
		// which the fixer cannot safely fix from a single body line
		final var withoutBlocks = stripBlockComments(stripped);
		if (withoutBlocks == null)
			return true;

		// strip trailing line comment; if nothing real remains (or only an empty `;`),
		// it's effectively comment-only
		final var withoutLine = stripTrailingLineComment(withoutBlocks).strip();
		return withoutLine.isEmpty() || withoutLine.equals(";");
	}

	@CheckReturnValue
	private static boolean isEscaped(@Nonnull String line, int pos) {
		var backslashes = 0;
		for (var i = pos - 1; i >= 0 && line.charAt(i) == '\\'; --i)
			++backslashes;
		return backslashes % 2 != 0;
	}

	@CheckReturnValue
	private static boolean isNonDoWhileKeyword(@Nonnull String stripped) {
		return stripped.startsWith("if ") || stripped.startsWith("if(")
				|| stripped.equals("else") || stripped.startsWith("else ")
				|| stripped.startsWith("for ") || stripped.startsWith("for(")
				|| stripped.startsWith("while ") || stripped.startsWith("while(");
	}

	@CheckReturnValue
	private static boolean isVariableDeclaration(@Nonnull String stripped) {
		// skip leading annotations (e.g., @SuppressWarnings("unused") int x = 5;)
		var s = stripped;
		while (s.startsWith("@")) {
			var j = 1;
			while (j < s.length() && Character.isJavaIdentifierPart(s.charAt(j)))
				++j;
			if (j < s.length() && s.charAt(j) == '(') {
				var depth = 1;
				++j;
				while (j < s.length() && depth > 0) {
					if (s.charAt(j) == '(')
						++depth;
					else if (s.charAt(j) == ')')
						--depth;
					++j;
				}
			}
			while (j < s.length() && s.charAt(j) == ' ')
				++j;
			s = s.substring(j);
		}

		if (s.startsWith("final ") || s.startsWith("var "))
			return true;

		// extract the first word
		var end = 0;
		while (end < s.length() && Character.isJavaIdentifierPart(s.charAt(end)))
			++end;
		if (end == 0 || end >= s.length())
			return false;

		final var firstWord = s.substring(0, end);

		// statement keywords are not variable declarations
		return switch (firstWord) {
			case "assert", "break", "case", "continue", "default", "do", "else", "for",
			     "if", "new", "return", "super", "switch", "synchronized", "this",
			     "throw", "try", "while", "yield" -> false;
			default -> {
				// after the type name, skip dot-separated qualifiers (e.g., Map.Entry)
				var i = end;
				while (i < s.length() && s.charAt(i) == '.') {
					++i;
					while (i < s.length() && Character.isJavaIdentifierPart(s.charAt(i)))
						++i;
				}
				// skip optional generics and array brackets, then check for identifier
				if (i < s.length() && s.charAt(i) == '<') {
					var depth = 1;
					++i;
					while (i < s.length() && depth > 0) {
						if (s.charAt(i) == '<')
							++depth;
						else if (s.charAt(i) == '>')
							--depth;
						++i;
					}
				}
				while (i + 1 < s.length() && s.charAt(i) == '[' && s.charAt(i + 1) == ']')
					i += 2;
				// must be followed by space then identifier
				yield i < s.length() && s.charAt(i) == ' '
						&& i + 1 < s.length() && Character.isJavaIdentifierStart(s.charAt(i + 1));
			}
		};
	}

	/**
	 * Removes all completed {@code /* ... *}{@code /} block comments, tracking string,
	 * char literal, and {@code //} line-comment state so comment delimiters inside
	 * literals or inside line comments are not mistaken for real block comments.
	 * Returns {@code null} if an unterminated block comment is found — callers should
	 * treat that as "refuse to fix".
	 */
	@CheckReturnValue
	@Nullable
	private static String stripBlockComments(@Nonnull String s) {
		final var sb = new StringBuilder();
		var inString = false;
		var inChar = false;
		var i = 0;
		while (i < s.length()) {
			final var c = s.charAt(i);
			if (inString) {
				sb.append(c);
				if (c == '"' && !isEscaped(s, i))
					inString = false;
				++i;
			}
			else if (inChar) {
				sb.append(c);
				if (c == '\'' && !isEscaped(s, i))
					inChar = false;
				++i;
			}
			else if (c == '"') {
				inString = true;
				sb.append(c);
				++i;
			}
			else if (c == '\'') {
				inChar = true;
				sb.append(c);
				++i;
			}
			else if (i + 1 < s.length() && c == '/' && s.charAt(i + 1) == '/') {
				// line comment runs to end-of-string; preserve it untouched and stop scanning
				sb.append(s, i, s.length());
				break;
			}
			else if (i + 1 < s.length() && c == '/' && s.charAt(i + 1) == '*') {
				final var end = s.indexOf("*/", i + 2);
				if (end < 0)
					return null;
				i = end + 2;
			}
			else {
				sb.append(c);
				++i;
			}
		}
		return sb.toString();
	}

	@CheckReturnValue
	@Nonnull
	private static String stripTrailingLineComment(@Nonnull String s) {
		final var idx = findTrailingComment(s);
		return idx < 0 ? s : s.substring(0, idx);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);
		final var stripped = line.stripLeading();
		final var indent = extractIndent(line);

		// handle non-do-while keywords
		if (isNonDoWhileKeyword(stripped)) {
			// one-liner (body on same line as keyword)
			if (stripped.endsWith(";"))
				return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP);

			// unnecessary braces: keyword line ends with {
			if (stripped.endsWith("{")) {
				// skip for-loops: PreferBulkOperation may also fire on them
				if (stripped.startsWith("for ") || stripped.startsWith("for("))
					return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP);
				return fixNonDoWhileUnnecessaryBraces(lines, lineIndex, indent);
			}

			// brace on its own line: treat as unnecessary braces
			var next = lineIndex + 1;
			while (next < lines.size() && lines.get(next).isBlank())
				++next;
			if (next < lines.size() && lines.get(next).stripLeading().startsWith("{")) {
				if (stripped.startsWith("for ") || stripped.startsWith("for("))
					return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP);
				return fixNonDoWhileBraceOnOwnLine(lines, lineIndex, next, indent);
			}

			return fixNonDoWhileMissingBraces(lines, lineIndex, indent);
		}

		if (!stripped.startsWith("do ") && !stripped.equals("do") && !stripped.startsWith("do\t"))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP);

		if (stripped.startsWith("do {") || stripped.startsWith("do\t{"))
			return fixBracedBody(lines, lineIndex, indent);

		if (stripped.equals("do"))
			return fixOwnLine(lines, lineIndex, indent);

		return fixOnDoLine(lines, lineIndex, line, indent);
	}
}