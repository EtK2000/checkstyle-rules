package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class ExplicitInitializationFixer implements CheckstyleFixer {
	/**
	 * Returns true if the value string represents a Java default value:
	 * zero for numeric types, false for boolean, '\0' for char, null for references.
	 */
	@CheckReturnValue
	private static boolean isDefaultValue(@Nonnull String value) {
		if ("false".equals(value) || "null".equals(value))
			return true;

		if (value.startsWith("'") && value.endsWith("'"))
			return "'\\0'".equals(value) || "'\\u0000'".equals(value);

		return isNumericZero(value);
	}

	@CheckReturnValue
	private static boolean isNumericZero(@Nonnull String value) {
		var s = value.replace("_", "");
		if (s.isEmpty())
			return false;

		// 'L'/'l' is the long-integer suffix; strip it because Double.parseDouble
		// (used below to mirror the check's own CheckUtil.parseDouble) rejects it,
		// unlike the 'f'/'F'/'d'/'D' float suffixes it accepts natively.
		final var lastChar = s.charAt(s.length() - 1);
		if (lastChar == 'L' || lastChar == 'l')
			s = s.substring(0, s.length() - 1);
		if (s.isEmpty())
			return false;

		// A hex literal is a float only when it carries a 'p'/'P' binary exponent;
		// hex and binary integers aren't parseable by Double.parseDouble, so a zero
		// one is simply all-'0' digits. Everything else (decimal int/float, octal,
		// hex float) parses via Double.parseDouble, matching the check exactly.
		final var isHex = s.startsWith("0x") || s.startsWith("0X");
		if (s.startsWith("0b") || s.startsWith("0B")
				|| (isHex && s.indexOf('p') < 0 && s.indexOf('P') < 0)) {
			final var digits = s.substring(2);
			if (digits.isEmpty())
				return false;
			for (var i = 0; i < digits.length(); ++i) {
				if (digits.charAt(i) != '0')
					return false;
			}
			return true;
		}

		try {
			return Double.compare(Double.parseDouble(s), 0.0) == 0;
		}
		catch (NumberFormatException e) {
			return false;
		}
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		// Fold the lexer state over preceding lines so a declaration that continues a
		// multi-line comment or text block (its closer sits before the code on this
		// line) is masked with the correct entry state; a NONE mask would mis-lex a
		// stray quote in the carried comment tail and blank the real assignment.
		var entryState = JavaLineScanner.LexerState.NONE;
		for (var i = 0; i < lineIndex; ++i)
			entryState = JavaLineScanner.stateAfter(lines.get(i), entryState);
		// Locate the assignment and initializer terminator on the mask so a '=', ';',
		// or ',' inside a comment/string/char literal can't hijack them. The comment
		// between the name and '=' is why the '=' locate itself must be masked.
		final var scan = JavaLineScanner.stripCommentsAndStrings(line, entryState);

		final var eqIdx = scan.indexOf('=', column);
		if (eqIdx < 0)
			return null;

		final var semiIdx = scan.indexOf(';', eqIdx);
		if (semiIdx < 0)
			return null;

		final var commaIdx = scan.indexOf(',', eqIdx);
		final var endIdx = (commaIdx >= 0 && commaIdx < semiIdx) ? commaIdx : semiIdx;

		// The value token is the whitespace-trimmed span of the masked initializer
		// region; classify it from the ORIGINAL text, since a char literal like '\0'
		// masks its interior to spaces and would misclassify if read from the mask.
		var valStart = eqIdx + 1;
		while (valStart < endIdx && Character.isWhitespace(scan.charAt(valStart)))
			++valStart;
		var valEnd = endIdx;
		while (valEnd > valStart && Character.isWhitespace(scan.charAt(valEnd - 1)))
			--valEnd;
		if (valStart >= valEnd)
			return null;

		final var value = line.substring(valStart, valEnd);
		if (!isDefaultValue(value))
			return new SkipResult(SkipMessages.EXPLICIT_INIT_SKIP);

		// Preserve any comment sitting before or after the removed value: each side's
		// masked region is all blanks, so stripping the original text of that region
		// yields the comment (or "" when it is only whitespace).
		final var before = line.substring(eqIdx + 1, valStart).strip();
		final var after = line.substring(valEnd, endIdx).strip();
		final var comments = before.isEmpty() ? after
				: after.isEmpty() ? before
				: before + " " + after;

		final var head = line.substring(0, eqIdx).stripTrailing();
		final var tail = line.substring(endIdx);
		final var fixed = comments.isEmpty() ? head + tail : head + " " + comments + tail;
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}