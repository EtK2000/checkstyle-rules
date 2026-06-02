package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.LineLength.MAX_LINE_LENGTH;

import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JavaLineScanner.LexerState;
import com.etk2000.checkstyle.LineText;

import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class FieldConsolidationFixer implements CheckstyleFixer {
	private static final String SKIP_BLOCK_COMMENT = "block comment on the field declaration line";
	private static final String SKIP_MIXED_ARRAY_BRACKETS = SkipMessages.get("field.consolidate.skip.mixed.array.brackets");
	private static final String SKIP_MULTILINE_DECLARATION = SkipMessages.get("field.consolidate.skip.multiline");
	private static final String SKIP_PREV_FIELD = "could not locate the preceding field declaration";
	private static final String SKIP_TRAILING_CONTENT = SkipMessages.get("field.consolidate.skip.trailing.content");

	/**
	 * The index just past the last bracket of the C-style array suffix the
	 * declarator name ending at {@code from} carries, or {@code from} itself when
	 * it carries none. Whitespace before and inside the suffix is tolerated
	 * ({@code int alpha [];} is a legal declaration), but whitespace after the last
	 * bracket is left to the caller, which reads it as the separator leading to the
	 * next declarator or to the terminator.
	 */
	@CheckReturnValue
	private static int declaratorBracketsEnd(@Nonnull String line, int from, int end) {
		var bracketsEnd = from;
		for (var pos = from; pos < end; ++pos) {
			final var c = line.charAt(pos);
			if (c == '[' || c == ']')
				bracketsEnd = pos + 1;
			else if (!Character.isWhitespace(c))
				break;
		}
		return bracketsEnd;
	}

	@CheckReturnValue
	@Nonnull
	@VisibleForTesting
	static List<LexerState> entryStates(@Nonnull List<String> lines) {
		final var states = new ArrayList<LexerState>(lines.size());
		var state = LexerState.NONE;
		for (var line : lines) {
			states.add(state);
			state = JavaLineScanner.stateAfter(line, state);
		}
		return states;
	}

	@Nullable
	private static String extractFieldNames(@Nonnull String line, int column) {
		if (column < 0 || column >= line.length())
			return null;
		if (!Character.isJavaIdentifierStart(line.charAt(column)))
			return null;

		final var sb = new StringBuilder();
		var pos = column;
		while (pos < line.length()) {
			if (!Character.isJavaIdentifierStart(line.charAt(pos)))
				break;
			final var nameStart = pos;
			while (pos < line.length() && Character.isJavaIdentifierPart(line.charAt(pos)))
				++pos;
			sb.append(line, nameStart, pos);

			pos = declaratorBracketsEnd(line, pos, line.length());

			while (pos < line.length() && Character.isWhitespace(line.charAt(pos)))
				++pos;

			if (pos < line.length() && line.charAt(pos) == ',') {
				sb.append(", ");
				do ++pos;
				while (pos < line.length() && Character.isWhitespace(line.charAt(pos)));
			}
			else
				break;
		}
		var result = sb.toString();
		if (result.endsWith(", "))
			result = result.substring(0, result.length() - 2);
		return result.isEmpty() ? null : result;
	}

	@CheckReturnValue
	private static int findFieldNamesStart(@Nonnull String line, int semiIdx) {
		var pos = semiIdx - 1;
		while (true) {
			// whitespace as well as brackets: a declarator's C-style suffix may sit
			// away from its name (`int alpha [];`) as well as after the terminator
			while (pos >= 0 && (line.charAt(pos) == '[' || line.charAt(pos) == ']' || Character.isWhitespace(line.charAt(pos))))
				--pos;
			if (pos < 0 || !Character.isJavaIdentifierPart(line.charAt(pos)))
				break;
			while (pos >= 0 && Character.isJavaIdentifierPart(line.charAt(pos)))
				--pos;
			var tempPos = pos;
			while (tempPos >= 0 && Character.isWhitespace(line.charAt(tempPos)))
				--tempPos;
			if (tempPos >= 0 && line.charAt(tempPos) == ',') {
				pos = tempPos - 1;
				continue;
			}
			break;
		}
		return pos + 1;
	}

	@CheckReturnValue
	@VisibleForTesting
	static int findFieldSemicolon(@Nonnull String line, @Nonnull LexerState state) {
		return JavaLineScanner.stripCommentsAndStrings(line, state).indexOf(';');
	}

	@CheckReturnValue
	private static int findFirstIdentStart(@Nonnull String line) {
		for (var i = 0; i < line.length(); ++i) {
			if (Character.isJavaIdentifierStart(line.charAt(i)))
				return i;
		}
		return -1;
	}

	private static int findPrevFieldLine(@Nonnull List<String> lines, int lineIndex, @Nonnull List<LexerState> entryStates) {
		for (var i = lineIndex - 1; i >= 0; --i) {
			final var line = lines.get(i);
			if (findFieldSemicolon(line, entryStates.get(i)) >= 0 || findTrailingComma(line, entryStates.get(i)) >= 0)
				return i;
			final var trimmed = line.trim();
			if (!trimmed.startsWith("@") && !trimmed.isEmpty())
				return -1;
		}
		return -1;
	}

	@CheckReturnValue
	@VisibleForTesting
	static int findTrailingComma(@Nonnull String line, @Nonnull LexerState state) {
		final var masked = JavaLineScanner.stripCommentsAndStrings(line, state);
		var lastComma = -1;
		var depth = 0;
		for (var i = 0; i < masked.length(); ++i) {
			final var c = masked.charAt(i);
			if (c == '(' || c == '{' || c == '[')
				++depth;
			else if (c == ')' || c == '}' || c == ']')
				--depth;
			else if (c == ',' && depth == 0)
				lastComma = i;
		}
		return lastComma;
	}

	/**
	 * Net generic-angle-bracket balance ({@code <} minus {@code >}) over
	 * {@code masked[start, end)}. A nonzero result means the region's type
	 * prefix has a generic that opened or closed on a different physical line,
	 * i.e. the declaration spans multiple lines. {@code masked} must already be
	 * run through {@link JavaLineScanner#stripCommentsAndStrings} so brackets
	 * inside literals and comments are not counted.
	 */
	@CheckReturnValue
	private static int genericBracketDepth(@Nonnull String masked, int start, int end) {
		var depth = 0;
		for (var i = start; i < end; ++i) {
			final var c = masked.charAt(i);
			if (c == '<')
				++depth;
			else if (c == '>')
				--depth;
		}
		return depth;
	}

	// firstCommentMarker rather than the mask, which erases the markers we need to inspect
	@CheckReturnValue
	@VisibleForTesting
	static boolean hasBlockCommentBefore(@Nonnull String line, int end, @Nonnull LexerState state) {
		final var marker = JavaLineScanner.firstCommentMarker(line, state);
		return marker >= 0 && marker < end && marker + 1 < line.length() && line.charAt(marker + 1) == '*';
	}

	private static boolean hasCStyleArrayBrackets(@Nonnull String line, int semiIdx) {
		var i = semiIdx - 1;
		while (i >= 0 && Character.isWhitespace(line.charAt(i)))
			--i;
		return i >= 0 && line.charAt(i) == ']';
	}

	/**
	 * Returns true if {@code tail} is nothing but a comment that closes on its own
	 * line, so the merge can re-emit it after the surviving terminator. A tail that
	 * leaves a block comment open would carry the following lines into the comment.
	 */
	@CheckReturnValue
	private static boolean isCarryableComment(@Nonnull String tail) {
		// a terminator is only ever located in code context, so the text after it
		// starts in normal lexer state
		final var state = JavaLineScanner.stateAfter(tail, LexerState.NONE);
		return !state.inBlockComment() && !state.inTextBlock()
				&& JavaLineScanner.stripCommentsAndStrings(tail, LexerState.NONE).isBlank();
	}

	private static boolean isCommentLine(@Nonnull String line) {
		final var trimmed = line.trim();
		return trimmed.startsWith("//") || trimmed.startsWith("/*");
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> parseFieldNames(@Nonnull String names) {
		final var result = new ArrayList<String>();
		var pos = 0;
		while (pos < names.length()) {
			while (pos < names.length() && Character.isWhitespace(names.charAt(pos)))
				++pos;
			if (pos >= names.length() || !Character.isJavaIdentifierStart(names.charAt(pos)))
				break;
			final var start = pos;
			while (pos < names.length() && Character.isJavaIdentifierPart(names.charAt(pos)))
				++pos;
			while (pos < names.length() && (names.charAt(pos) == '[' || names.charAt(pos) == ']'))
				++pos;
			result.add(names.substring(start, pos));
			while (pos < names.length() && (names.charAt(pos) == ',' || Character.isWhitespace(names.charAt(pos))))
				++pos;
		}
		return result;
	}

	/**
	 * The previous field's declaration prefix (everything before
	 * {@code terminatorIdx}) with its C-style declarator brackets hoisted onto the
	 * base type: {@code int alpha[], beta[]} becomes {@code int[] alpha, beta}.
	 * The merged declaration renders one base type followed by bare names, so the
	 * brackets have to move for the merge to keep the previous field's type.
	 *
	 * <p>Returns {@code null} rather than a prefix that would change a type: when
	 * the declarators do not all carry the same bracket suffix
	 * ({@code int alpha, beta[]}), one shared base type cannot express them and
	 * hoisting the odd one out would retype its siblings; when the declaration's
	 * type sits on an earlier line, there is no base type here to hoist onto.
	 */
	@CheckReturnValue
	@Nullable
	private static String rewritePrevPrefixToJavaStyle(@Nonnull String prevLine, int terminatorIdx) {
		final var namesStart = findFieldNamesStart(prevLine, terminatorIdx);
		final var bareNames = new StringBuilder();
		String brackets = null;
		var pos = namesStart;
		while (pos < terminatorIdx) {
			if (!Character.isJavaIdentifierStart(prevLine.charAt(pos)))
				return null;
			final var nameStart = pos;
			while (pos < terminatorIdx && Character.isJavaIdentifierPart(prevLine.charAt(pos)))
				++pos;
			bareNames.append(prevLine, nameStart, pos);

			final var bracketsStart = pos;
			pos = declaratorBracketsEnd(prevLine, pos, terminatorIdx);
			final var declaratorBrackets = prevLine.substring(bracketsStart, pos).strip();
			if (brackets == null)
				brackets = declaratorBrackets;
			else if (!brackets.equals(declaratorBrackets))
				return null;

			final var separatorStart = pos;
			while (pos < terminatorIdx && (prevLine.charAt(pos) == ',' || Character.isWhitespace(prevLine.charAt(pos))))
				++pos;
			bareNames.append(prevLine, separatorStart, pos);
		}
		if (brackets == null || brackets.isEmpty())
			return prevLine.substring(0, terminatorIdx);

		var typeEnd = namesStart;
		while (typeEnd > 0 && Character.isWhitespace(prevLine.charAt(typeEnd - 1)))
			--typeEnd;
		if (typeEnd == 0)
			return null;
		return prevLine.substring(0, typeEnd) + brackets + prevLine.substring(typeEnd, namesStart) + bareNames;
	}

	/**
	 * Returns the index of the declaration terminator on {@code line} (its {@code ;},
	 * or the trailing {@code ,} of a declaration continued on the next line), or
	 * {@code -1} when the line carries neither.
	 */
	@CheckReturnValue
	private static int terminatorIndexOf(@Nonnull String line, @Nonnull LexerState state) {
		final var semiIdx = findFieldSemicolon(line, state);
		return semiIdx >= 0 ? semiIdx : findTrailingComma(line, state);
	}

	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int column) {
		if (lineIndex < 1 || lineIndex >= lines.size())
			return null;

		final var entryStates = entryStates(lines);
		final var prev = findPrevFieldLine(lines, lineIndex, entryStates);
		if (prev < 0)
			return new SkipResult(SKIP_PREV_FIELD);

		final var prevLine = lines.get(prev);
		var terminatorIdx = findFieldSemicolon(prevLine, entryStates.get(prev));
		final var prevEndsWithComma = terminatorIdx < 0;
		if (prevEndsWithComma)
			terminatorIdx = findTrailingComma(prevLine, entryStates.get(prev));
		if (terminatorIdx < 0)
			return null;

		// The previous field's type prefix must be complete on prevLine. An
		// unbalanced generic there means either the type wraps onto prevLine
		// from above, or prevLine is itself a wrapped continuation of the
		// violation field (whose internal generic comma findTrailingComma
		// mistook for a declarator separator). Both would drive a corrupt
		// single-line rebuild, so skip instead.
		final var prevMasked = JavaLineScanner.stripCommentsAndStrings(prevLine, entryStates.get(prev));
		if (genericBracketDepth(prevMasked, 0, terminatorIdx) != 0)
			return new SkipResult(SKIP_MULTILINE_DECLARATION);

		final var prevCStyle = hasCStyleArrayBrackets(prevLine, terminatorIdx);
		final var violationLine = lines.get(lineIndex);
		final var violationState = entryStates.get(lineIndex);
		final var violationSemiIdx = findFieldSemicolon(violationLine, violationState);

		// every use below indexes chars, but the reported column counts code points
		column = LineText.charIndexOfColumn(violationLine, column);
		if (column < 0)
			return null;

		// A column mid-identifier (rather than at a name's start) means another
		// fixer (e.g. FieldSortingFixer) reflowed this group into a wrapped
		// declaration earlier in the same pass, so the violation is now stale;
		// acting on it would truncate the name. Defer to the next pass.
		if (column >= 1 && column < violationLine.length()
				&& Character.isJavaIdentifierPart(violationLine.charAt(column - 1))
				&& Character.isJavaIdentifierPart(violationLine.charAt(column)))
			return null;

		if (column >= 0 && column < violationLine.length()
				&& !Character.isJavaIdentifierStart(violationLine.charAt(column))) {
			var newColumn = column;
			while (newColumn < violationLine.length()
					&& !Character.isJavaIdentifierStart(violationLine.charAt(newColumn)))
				++newColumn;
			if (newColumn >= violationLine.length())
				return null;
			column = newColumn;
		}

		final var names = extractFieldNames(violationLine, column);
		if (names == null || names.isEmpty())
			return null;

		// The violation field's type must be complete before its name. An
		// unbalanced generic in the prefix means the type opened on a line
		// above, so merging into prevLine would drop the wrapped type segment.
		final var violationMasked = JavaLineScanner.stripCommentsAndStrings(violationLine, violationState);
		if (genericBracketDepth(violationMasked, 0, column) != 0)
			return new SkipResult(SKIP_MULTILINE_DECLARATION);

		if (hasBlockCommentBefore(violationLine, column, violationState))
			return new SkipResult(SKIP_BLOCK_COMMENT);
		final var violationEnd = violationSemiIdx >= 0 ? violationSemiIdx : violationLine.length();
		if (violationEnd < column)
			return null;
		final var afterColumnState = JavaLineScanner.stateAfter(violationLine.substring(0, column), violationState);
		if (hasBlockCommentBefore(violationLine.substring(column), violationEnd - column, afterColumnState))
			return new SkipResult(SKIP_BLOCK_COMMENT);

		var endLine = lineIndex;
		var allNames = names;
		var stoppedAtComment = false;
		if (violationSemiIdx < 0) {
			final var violationIndentWidth = LineLength.tabExpandedLength(LineText.extractIndent(violationLine));
			for (var i = lineIndex + 1; i < lines.size(); ++i) {
				final var contLine = lines.get(i);
				if (LineLength.tabExpandedLength(LineText.extractIndent(contLine)) <= violationIndentWidth)
					break;
				if (isCommentLine(contLine)) {
					stoppedAtComment = true;
					break;
				}
				final var contIdentStart = findFirstIdentStart(contLine);
				if (contIdentStart < 0)
					break;
				final var contNames = extractFieldNames(contLine, contIdentStart);
				// unreachable for well-formed input: findFirstIdentStart guarantees an identifier start at
				// contIdentStart, so extractFieldNames always yields at least that name; guards a null splice
				if (contNames == null || contNames.isEmpty())
					return null;
				allNames = allNames + ", " + contNames;
				endLine = i;
				if (findFieldSemicolon(contLine, entryStates.get(i)) >= 0)
					break;
			}
		}

		final String suffix;
		if (prevEndsWithComma) {
			final var lastLine = lines.get(endLine);
			final var lastSemiIdx = findFieldSemicolon(lastLine, entryStates.get(endLine));
			if (lastSemiIdx >= 0)
				suffix = lastLine.substring(lastSemiIdx);
			else {
				final var lastCommaIdx = findTrailingComma(lastLine, entryStates.get(endLine));
				suffix = lastCommaIdx >= 0 ? lastLine.substring(lastCommaIdx) : ",";
			}
		}
		else if (stoppedAtComment) {
			final var lastLine = lines.get(endLine);
			final var lastCommaIdx = findTrailingComma(lastLine, entryStates.get(endLine));
			suffix = lastCommaIdx >= 0 ? lastLine.substring(lastCommaIdx) : prevLine.substring(terminatorIdx);
		}
		else if (endLine > lineIndex) {
			final var lastLine = lines.get(endLine);
			final var lastSemiIdx = findFieldSemicolon(lastLine, entryStates.get(endLine));
			suffix = lastSemiIdx >= 0 ? lastLine.substring(lastSemiIdx) : prevLine.substring(terminatorIdx);
		}
		else
			suffix = prevLine.substring(terminatorIdx);

		// The merge keeps exactly one terminator and one trailing tail, so whatever
		// follows the other consumed line's terminator is dropped. A dropped comment
		// silently reattributes the surviving one to both fields, and dropped code is
		// deleted outright. Carry a lone comment across; refuse anything else.
		final var lastTerminatorIdx = terminatorIndexOf(lines.get(endLine), entryStates.get(endLine));
		final var keptTail = suffix.substring(1);
		final var prevTail = prevLine.substring(terminatorIdx + 1);
		final var lastTail = lastTerminatorIdx >= 0 ? lines.get(endLine).substring(lastTerminatorIdx + 1) : "";
		final var droppedTail = keptTail.equals(prevTail) ? lastTail : prevTail;
		var keptSuffix = suffix;
		if (!droppedTail.isBlank() && !droppedTail.equals(keptTail)) {
			if (!keptTail.isBlank() || !isCarryableComment(droppedTail))
				return new SkipResult(SKIP_TRAILING_CONTENT);
			keptSuffix = suffix.charAt(0) + droppedTail;
		}
		// the reconciliation above compares only prevLine's and the last consumed line's
		// tails, but every line in [prev, endLine] is replaced. A comment on any line
		// between them sits inside that span and would be deleted: that includes the
		// second field's stacked annotation lines, which sit above `lineIndex`.
		for (var i = prev + 1; i < endLine; ++i) {
			if (JavaLineScanner.firstCommentMarker(lines.get(i), entryStates.get(i)) >= 0)
				return new SkipResult(SKIP_TRAILING_CONTENT);
		}

		final var prevPrefix = prevCStyle ? rewritePrevPrefixToJavaStyle(prevLine, terminatorIdx) : prevLine.substring(0, terminatorIdx);
		if (prevPrefix == null)
			return new SkipResult(SKIP_MIXED_ARRAY_BRACKETS);
		final var merged = prevPrefix + ", " + allNames + keptSuffix;

		if (LineLength.tabExpandedLength(merged) <= MAX_LINE_LENGTH)
			return new FixResult(prev, endLine, List.of(merged));

		final var newTerminatorIdx = prevPrefix.length() + 2 + allNames.length();
		final var fieldNamesStart = findFieldNamesStart(merged, newTerminatorIdx);
		final var prefix = merged.substring(0, fieldNamesStart);
		final var namesRegion = merged.substring(fieldNamesStart, newTerminatorIdx);
		final var wrapSuffix = merged.substring(newTerminatorIdx);

		final var namesList = parseFieldNames(namesRegion);
		// unreachable for well-formed input; guards against findFieldNamesStart mis-locating the names
		// region, which would otherwise let wrapFieldList drop fields on an empty split
		if (namesList.size() < 2)
			return new FixResult(prev, endLine, List.of(merged));

		final var baseIndent = LineText.extractIndent(prevLine);
		final var contIndent = baseIndent + "\t\t";
		return new FixResult(prev, endLine, LineLength.wrapFieldList(prefix, namesList, wrapSuffix, contIndent));
	}
}