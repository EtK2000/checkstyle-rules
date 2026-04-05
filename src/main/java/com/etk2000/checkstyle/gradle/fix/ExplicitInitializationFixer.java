package com.etk2000.checkstyle.gradle.fix;

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

		// char: '\0', '\u0000'
		if (value.startsWith("'") && value.endsWith("'"))
			return "'\\0'".equals(value) || "'\\u0000'".equals(value);

		// numeric zero: strip suffix (L/l/f/F/d/D), underscores, and hex/binary/octal prefixes,
		// then check if it evaluates to zero
		return isNumericZero(value);
	}

	@CheckReturnValue
	private static boolean isNumericZero(@Nonnull String value) {
		if (value.isEmpty())
			return false;

		// strip trailing type suffix
		var s = value;
		final var lastChar = s.charAt(s.length() - 1);
		if (lastChar == 'D' || lastChar == 'F' || lastChar == 'L'
				|| lastChar == 'd' || lastChar == 'f' || lastChar == 'l')
			s = s.substring(0, s.length() - 1);

		// strip underscores (Java numeric separator)
		s = s.replace("_", "");
		if (s.isEmpty())
			return false;

		// strip hex/binary/octal prefix
		if (s.startsWith("0x") || s.startsWith("0X")
				|| s.startsWith("0b") || s.startsWith("0B"))
			s = s.substring(2);

		// all remaining chars must be zeros, dots, and exponent parts that evaluate to zero
		// e.g. "0", "0.0", "0.000", "0e0", "0.0e+0", "00"
		var hasDigit = false;
		for (var i = 0; i < s.length(); ++i) {
			final var c = s.charAt(i);
			if (c == '0' || c == '.') {
				if (c == '0')
					hasDigit = true;
			}
			else if (c == 'e' || c == 'E' || c == 'p' || c == 'P') {
				// exponent part: everything after must also be zero
				// skip optional sign
				var j = i + 1;
				if (j < s.length() && (s.charAt(j) == '+' || s.charAt(j) == '-'))
					++j;
				// remaining must all be '0'
				if (j >= s.length())
					return false;
				for (; j < s.length(); ++j) {
					if (s.charAt(j) != '0')
						return false;
				}
				return hasDigit;
			}
			else
				return false;
		}
		return hasDigit;
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		if (column < 0 || column >= line.length())
			return null;

		final var eqIdx = line.indexOf('=', column);
		if (eqIdx < 0)
			return null;

		// find the end of this initializer: next ',' or ';' after '='
		final var semiIdx = line.indexOf(';', eqIdx);
		if (semiIdx < 0)
			return null;

		final var commaIdx = line.indexOf(',', eqIdx);
		final var endIdx = (commaIdx >= 0 && commaIdx < semiIdx) ? commaIdx : semiIdx;

		final var value = line.substring(eqIdx + 1, endIdx).strip();
		if (!isDefaultValue(value))
			return null;

		final var fixed = line.substring(0, eqIdx).stripTrailing() + line.substring(endIdx);
		return new FixResult(lineIndex, lineIndex, List.of(fixed));
	}
}