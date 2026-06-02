package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferPrefixIncrementCheck;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferPrefixIncrementFixer implements CheckstyleFixer {
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
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
		if (operandStart < 0 || operatorStart < 0 || operandStart >= operatorStart
				|| !line.startsWith(operator, operatorStart))
			return null;

		// whitespace separating the operand from the operator would trail the
		// rebuilt line when the operator was its last token
		final var operand = line.substring(operandStart, operatorStart).stripTrailing();
		final var fixed = line.substring(0, operandStart) + operator + operand + line.substring(operatorStart + 2);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}