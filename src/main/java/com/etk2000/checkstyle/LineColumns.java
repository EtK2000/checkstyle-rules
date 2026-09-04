package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;

import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * A file's lines, converting a code-point column to a char index like
 * {@link LineText#charIndexOfColumn} does, but reading each line once: a line's columns are mapped
 * the first time one of them is asked for, so the many columns of one long line cost a single pass
 * over it rather than one pass each.
 */
public final class LineColumns {
	/** Marks a line without supplementary characters, whose columns are its char indexes. */
	private static final int[] SAME_AS_CHARS = new int[0];

	/** Per line, the char index of each column up to its end, mapped on first use. */
	private final int[][] offsets;
	private final List<String> lines;

	public LineColumns(@Nonnull List<String> lines) {
		this.lines = lines;
		offsets = new int[lines.size()][];
	}

	/** The char index of {@code node}'s column in its line, or {@code -1} when it does not name a position in the file. */
	@CheckReturnValue
	public int charIndex(@Nonnull DetailAST node) {
		return charIndex(node.getLineNo(), node.getColumnNo());
	}

	/** The char index of {@code column} in line {@code lineNo} (1-based), or {@code -1} when it does not name a position in the file. */
	@CheckReturnValue
	public int charIndex(int lineNo, int column) {
		final var lineIndex = lineNo - 1;
		if (lineIndex < 0 || lineIndex >= lines.size() || column < 0)
			return -1;
		final var line = lines.get(lineIndex);
		var lineOffsets = offsets[lineIndex];
		if (lineOffsets == null) {
			final var codePoints = line.codePointCount(0, line.length());
			if (codePoints == line.length())
				lineOffsets = SAME_AS_CHARS;
			else {
				lineOffsets = new int[codePoints + 1];
				var index = 0;
				for (var i = 0; i <= codePoints; ++i) {
					lineOffsets[i] = index;
					if (index < line.length())
						index += Character.charCount(line.codePointAt(index));
				}
			}
			offsets[lineIndex] = lineOffsets;
		}
		if (lineOffsets == SAME_AS_CHARS)
			return column <= line.length() ? column : -1;
		return column < lineOffsets.length ? lineOffsets[column] : -1;
	}

	@CheckReturnValue
	@Nonnull
	public List<String> lines() {
		return lines;
	}
}