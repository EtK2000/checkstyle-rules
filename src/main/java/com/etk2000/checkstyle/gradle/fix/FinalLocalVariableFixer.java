package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.LineText;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

class FinalLocalVariableFixer implements CheckstyleFixer {
	private static final Set<String> NON_DECL_KEYWORDS = Set.of(
			"break", "case", "catch", "continue", "default", "do", "else", "finally",
			"for", "if", "return", "switch", "throw", "try", "while", "yield"
	);

	/**
	 * Returns whether {@code line}'s code (ignoring a trailing line/block comment
	 * and the contents of string/char literals) ends in a {@code ,}. The
	 * multi-variable-continuation guard uses this rather than a plain
	 * {@code stripTrailing().endsWith(",")} so a sibling variable whose comma is
	 * followed by a trailing comment (e.g. {@code s = compute(), // note}) is still
	 * recognized, and a comma that only appears inside a comment or literal is not.
	 */
	@CheckReturnValue
	private static boolean codeEndsWithComma(@Nonnull String line) {
		return JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE).stripTrailing().endsWith(",");
	}

	/**
	 * The char index where the declaration containing the variable at char index
	 * {@code charColumn} begins on {@code line}: the first non-whitespace char after
	 * the nearest preceding statement boundary ({@code ;}, {@code {}, or {@code }}),
	 * or the end of the indentation when the declaration is the first statement on the
	 * line. The boundary scan runs over the comment/string-masked line so a {@code ;}
	 * inside a literal or trailing comment isn't mistaken for a statement separator.
	 * This is what lets a declaration that shares a line with a preceding statement
	 * ({@code X = Foo.X; int y = 0;}) take its {@code final} before its own type
	 * rather than at the start of the line. {@code charColumn} is a char index (the
	 * caller has already converted the pipeline's code-point column), so it maps
	 * directly onto the mask.
	 */
	@CheckReturnValue
	private static int declStart(@Nonnull String line, int charColumn) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, JavaLineScanner.LexerState.NONE);
		final var limit = Math.min(charColumn, masked.length());
		var boundary = -1;
		for (var i = 0; i < limit; ++i) {
			final var c = masked.charAt(i);
			if (c == ';' || c == '{' || c == '}')
				boundary = i;
		}
		var pos = boundary + 1;
		while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
			++pos;
		return pos;
	}

	/**
	 * Inserts {@code final } at {@code insertPos} (the start of the declaration on
	 * {@code targetIndex}). Skips when that position isn't a declaration start: a
	 * type, modifier, or annotation begins with a Java identifier or {@code @}, so
	 * a position starting with anything else (e.g. a comment-only line walked back to)
	 * can't legally take a {@code final} prefix. Also skips when {@code final} is
	 * already present.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt insertFinal(@Nonnull List<String> lines, int targetIndex, int insertPos) {
		final var line = lines.get(targetIndex);
		if (line.isBlank() || insertPos >= line.length())
			return new SkipResult(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE);

		final var first = line.charAt(insertPos);
		if (!Character.isJavaIdentifierStart(first) && first != '@')
			return new SkipResult(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE);
		if (line.startsWith("final ", insertPos))
			return new SkipResult(SkipMessages.FINAL_LOCAL_ALREADY_FINAL);
		// the check fires on a VARIABLE_DEF token; if a sibling fixer collapsed the
		// decl line into a different construct (e.g. PreferMathMethod merging
		// `int r;` + branched assign + trailing return into `return Math.max(...);`),
		// the original line index now points at content that cannot legally take a
		// `final` prefix, refuse to corrupt it
		final var tokenEnd = LineText.identEnd(line, insertPos);
		if (NON_DECL_KEYWORDS.contains(line.substring(insertPos, tokenEnd)))
			return new SkipResult(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE);

		final var fixed = line.substring(0, insertPos) + "final " + line.substring(insertPos);
		return new FixResult(targetIndex, targetIndex, List.of(fixed));
	}

	@Nonnull
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		// column is a code-point column from the pipeline; convert to a char index
		// once here so declStart's mask scan and the blank-prefix test both index
		// correctly on supplementary-char lines. A negative, past-end, or at-end
		// column can only be a synthetic position the check never reports (a real
		// variable name always has a `;`/`=`/`,` after it, so its start is strictly
		// inside the line), so refuse it rather than fabricating a fix at an
		// arbitrary spot
		final var charColumn = LineText.charIndexOfColumn(line, column);
		if (charColumn < 0 || charColumn >= line.length())
			return new SkipResult(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE);

		// the check reports the variable-name IDENT; when everything before it on
		// this line is whitespace, the type lives on a previous line (split
		// declaration), so 'final' belongs on the type line, not here
		if (line.substring(0, charColumn).isBlank()) {
			var typeLineIndex = lineIndex - 1;
			while (typeLineIndex >= 0 && lines.get(typeLineIndex).isBlank())
				--typeLineIndex;
			if (typeLineIndex < 0)
				return new SkipResult(SkipMessages.FINAL_LOCAL_NO_TYPE_LINE);
			// a multi-variable continuation: the line above the name is a sibling
			// variable (its declaration ends in a top-level ','), not the type
			// header; 'final' applies to the whole declaration, so a non-first
			// variable can't be made final on its own
			if (codeEndsWithComma(lines.get(typeLineIndex)))
				return new SkipResult(SkipMessages.FINAL_LOCAL_MULTI_VAR);
			final var typeLine = lines.get(typeLineIndex);
			return insertFinal(lines, typeLineIndex, LineText.extractIndent(typeLine).length());
		}

		return insertFinal(lines, lineIndex, declStart(line, charColumn));
	}
}