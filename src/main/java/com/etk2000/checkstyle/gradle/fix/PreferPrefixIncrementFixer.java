package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferPrefixIncrementCheck;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferPrefixIncrementFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var column = LineText.columnOfCharIndex(lines.get(lineIndex), charColumn);
		final var root = FixerAst.parseOrNull(lines);
		if (root == null)
			return null;

		final var span = PreferPrefixIncrementCheck.postfixSpanAt(root, lineIndex, column);
		if (span == null)
			return null;

		if (span.operandLine() != span.operatorLine())
			return new SkipResult(SkipMessages.PREFER_PREFIX_SKIP_MULTILINE_OPERAND);

		final var line = lines.get(lineIndex);
		final var operandStart = LineText.charIndexOfColumn(line, span.operandColumn());
		final var operatorStart = LineText.charIndexOfColumn(line, span.operatorColumn());
		final var operator = span.increment() ? "++" : "--";
		// the span comes from parsing this very text, and checkstyle's parser rejects an escaped
		// operator, so a mismatch is a bug; splicing on would drop two chars that are not the operator
		if (operandStart < 0 || operandStart >= operatorStart || !line.startsWith(operator, operatorStart))
			throw new IllegalStateException("postfix span does not match line " + lineIndex + ": " + line);

		// whitespace separating the operand from the operator would trail the
		// rebuilt line when the operator was its last token
		final var operand = line.substring(operandStart, operatorStart).stripTrailing();
		final var fixed = line.substring(0, operandStart) + operator + operand + line.substring(operatorStart + 2);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}