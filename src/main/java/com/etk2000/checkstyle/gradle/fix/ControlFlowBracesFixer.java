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
 *     <li>Tier 1: {@code do body; while (cond);} (all one line)</li>
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

	@Nonnull
	private static FixResult buildTierResult(
			@Nonnull String bodyText,
			@Nonnull String whileClause,
			int startLine,
			int endLine,
			@Nonnull String indent
	) {
		final var tier = determineTierFromText(bodyText, extractWhileCondition(whileClause));
		final var result = new ArrayList<String>();
		switch (tier) {
			case 1 -> result.add(indent + "do " + bodyText + " " + whileClause);
			case 2 -> {
				result.add(indent + "do " + bodyText);
				result.add(indent + whileClause);
			}
			default -> {
				final var body = bodyText.endsWith(";") ? bodyText : bodyText + ";";
				result.add(indent + "do");
				result.add(indent + "\t" + body);
				result.add(indent + whileClause);
			}
		}
		return new FixResult(startLine, endLine, result);
	}

	@CheckReturnValue
	private static int determineTierFromText(
			@Nonnull String bodyText,
			@Nonnull String whileCondition
	) {
		final var text = bodyText.endsWith(";")
				? bodyText.substring(0, bodyText.length() - 1).strip()
				: bodyText.strip();

		if (text.isEmpty())
			return 3;

		if (text.contains(")."))
			return 3;

		if (!SIMPLE_BODY_PATTERN.matcher(text).matches())
			return 3;

		if (text.contains("."))
			return 2;
		if (whileCondition.contains("&&") || whileCondition.contains("||"))
			return 2;

		return 1;
	}

	@CheckReturnValue
	@Nonnull
	private static String extractIndent(@Nonnull String line) {
		var i = 0;
		while (i < line.length() && (line.charAt(i) == '\t' || line.charAt(i) == ' '))
			++i;
		return line.substring(0, i);
	}

	@CheckReturnValue
	@Nonnull
	private static String extractWhileCondition(@Nonnull String whileClause) {
		final var open = whileClause.indexOf('(');
		final var close = whileClause.lastIndexOf(')');
		if (open >= 0 && close > open)
			return whileClause.substring(open + 1, close);
		return whileClause;
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

		final var closeLineStripped = lines.get(closeBraceLine).stripLeading();
		final var whileClause = closeLineStripped.substring(closeLineStripped.indexOf("while"));

		if (bodyLines.size() == 1)
			return buildTierResult(bodyLines.getFirst().stripLeading(), whileClause, lineIndex, closeBraceLine, indent);

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

	@Nonnull
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
		final var whileLine = findWhileLine(lines, lineIndex);
		if (whileLine < 0)
			return null;

		if (whileLine > lineIndex + 2)
			return fixMissingBraces(lines, lineIndex, indent);

		final var whileClause = lines.get(whileLine).stripLeading();
		return buildTierResult(bodyText, whileClause, lineIndex, whileLine, indent);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 0 || lineIndex >= lines.size())
			return null;

		final var line = lines.get(lineIndex);
		final var stripped = line.stripLeading();

		if (!stripped.startsWith("do ") && !stripped.equals("do") && !stripped.startsWith("do\t"))
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP);

		final var indent = extractIndent(line);

		if (stripped.startsWith("do {") || stripped.startsWith("do\t{"))
			return fixBracedBody(lines, lineIndex, indent);

		if (stripped.equals("do"))
			return fixOwnLine(lines, lineIndex, indent);

		return fixOnDoLine(lines, lineIndex, line, indent);
	}
}