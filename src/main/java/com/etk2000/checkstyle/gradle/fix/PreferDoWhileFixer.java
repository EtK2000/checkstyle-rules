package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferDoWhileFixer implements CheckstyleFixer {
	private static final Pattern WHILE_LINE = Pattern.compile(
			"^(\\s*)while\\s*\\((.+)\\)\\s*(\\{)?\\s*$"
	);

	@CheckReturnValue
	private static boolean hasComment(@Nonnull String line) {
		return line.contains("//") || line.contains("/*");
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 1 || lineIndex + 1 >= lines.size())
			return null;

		final var whileMatch = WHILE_LINE.matcher(lines.get(lineIndex));
		if (!whileMatch.matches())
			return new SkipResult("while line not in expected format (multi-line cond, trailing content, or comment)");

		final var indent = whileMatch.group(1);
		final var cond = whileMatch.group(2);
		final var braced = whileMatch.group(3) != null;

		final var preLine = lines.get(lineIndex - 1);
		if (hasComment(preLine))
			return new SkipResult("comment on pre-statement line");
		if (!preLine.startsWith(indent))
			return new SkipResult("pre-statement indent mismatch");
		final var preContent = preLine.substring(indent.length());
		if (preContent.isEmpty() || Character.isWhitespace(preContent.charAt(0)) || !preContent.endsWith(";"))
			return new SkipResult("pre-statement formatting");

		final var bodyLine = lines.get(lineIndex + 1);
		if (hasComment(bodyLine))
			return new SkipResult("comment on body line");
		final var bodyStripped = bodyLine.strip();
		if (!bodyStripped.endsWith(";"))
			return new SkipResult("body formatting");
		if (!bodyStripped.equals(preContent))
			return new SkipResult("textual mismatch between pre-statement and body");

		final int endLine;
		if (braced) {
			if (lineIndex + 2 >= lines.size())
				return null;
			final var closingLine = lines.get(lineIndex + 2);
			if (!"}".equals(closingLine.strip()) || !closingLine.startsWith(indent))
				return new SkipResult("braced body multi-statement or unusual closing");
			endLine = lineIndex + 2;
		}
		else
			endLine = lineIndex + 1;

		return new FixResult(
				lineIndex - 1,
				endLine,
				List.of(
						indent + "do " + bodyStripped,
						indent + "while (" + cond + ");"
				)
		);
	}
}