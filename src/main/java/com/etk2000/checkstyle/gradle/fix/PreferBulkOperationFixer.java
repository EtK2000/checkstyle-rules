package com.etk2000.checkstyle.gradle.fix;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class PreferBulkOperationFixer implements CheckstyleFixer {
	/**
	 * Extracts the source expression from a for-each loop's joined text using
	 * paren-balanced scanning. Handles nested parentheses in method calls
	 * like {@code map.values()} or {@code getList(a, b)}.
	 */
	@CheckReturnValue
	@Nullable
	private static String extractForEachSource(@Nonnull String loopText) {
		final var colonIdx = LambdaCallParser.indexOfStructural(loopText, ": ");
		if (colonIdx < 0)
			return null;
		// The for-each source spans from just past `: ` to the `)` that closes the
		// for-header. We already know depth was 1 at the start of the source; find the
		// first structural `)` using the for-header's `(` as the anchor.
		final var forOpenParen = loopText.indexOf('(');
		if (forOpenParen < 0 || forOpenParen > colonIdx)
			return null;
		final var forCloseParen = LambdaCallParser.findClosingParenInLine(loopText, forOpenParen);
		if (forCloseParen < 0)
			return null;
		final var start = colonIdx + 2;
		if (start > forCloseParen)
			return null;
		return loopText.substring(start, forCloseParen).strip();
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
	@Nullable
	private static String extractLambdaTarget(@Nonnull String raw) {
		var target = raw.strip();
		if (target.startsWith("{"))
			target = target.substring(1).strip();
		if (target.isEmpty())
			return null;
		// Must be a simple receiver: identifier or dotted chain (x, x.y, x.y.z)
		// Rejects nested lambdas, parenthesized expressions, operators, etc.
		for (var i = 0; i < target.length(); ++i) {
			final var c = target.charAt(i);
			if (!Character.isJavaIdentifierPart(c) && c != '.')
				return null;
		}
		return target;
	}

	@CheckReturnValue
	@Nullable
	private static String extractTargetName(@Nonnull String loopText, @Nonnull String methodCall) {
		final var methodIdx = loopText.lastIndexOf(methodCall);
		if (methodIdx < 0)
			return null;
		var name = loopText.substring(0, methodIdx);
		final var lastSep = Math.max(name.lastIndexOf('{'), name.lastIndexOf(')'));
		if (lastSep >= 0)
			name = name.substring(lastSep + 1);
		name = name.strip();
		return name.isEmpty() ? null : name;
	}

	/**
	 * Extracts the value expression from text after {@code ] = }, stopping at the
	 * first structural semicolon (outside any literal, comment, or text block).
	 */
	@CheckReturnValue
	@Nonnull
	private static String extractValueUpToSemicolon(@Nonnull String text) {
		final var semiIdx = LambdaCallParser.indexOfStructuralChar(text, 0, ';');
		return semiIdx < 0 ? text.strip() : text.substring(0, semiIdx).strip();
	}

	@CheckReturnValue
	private static int findLoopEndLine(@Nonnull List<String> lines, int startLine) {
		if (LambdaCallParser.hasStructuralOpenBrace(lines.get(startLine)))
			return LambdaCallParser.findClosingBraceLine(lines, startLine);
		return LambdaCallParser.findEndOfBracelessStatement(lines, startLine);
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixArrayCopy(int startLine, int endLine, @Nonnull String indent, @Nonnull String loopText) {
		final var src = substringBetweenIdentBoundary(loopText, "< ", ".length");
		if (src == null)
			return null;

		final var bodyStart = LambdaCallParser.findClosingParenInLine(loopText, loopText.indexOf('('));
		if (bodyStart < 0)
			return null;
		final var bodyText = loopText.substring(bodyStart + 1).strip();
		var body = bodyText;
		if (body.startsWith("{"))
			body = body.substring(1).strip();
		final var bracketIdx = body.indexOf('[');
		if (bracketIdx < 0)
			return null;
		final var dst = body.substring(0, bracketIdx).strip();

		// Validate RHS pattern: the value after `] = ` must be exactly `src[lhsIndex]`
		// where lhsIndex is the same index expression as the LHS. This rejects both
		// non-arraycopy patterns (e.g. `arr[i] = -a[b[0]]`) and same-array-fill patterns
		// (e.g. `arr[i] = arr[0]` which is a fill, not a copy).
		final var assignIdx = body.indexOf("] = ");
		if (assignIdx < 0)
			return null;
		final var lhsIndex = body.substring(bracketIdx + 1, assignIdx).strip();
		if (lhsIndex.isEmpty())
			return null;
		final var value = body.substring(assignIdx + 4).stripLeading();
		final var expectedRhs = src + "[" + lhsIndex + "]";
		if (!value.startsWith(expectedRhs))
			return null;
		// Require that what immediately follows the matched RHS is not identifier-like
		// (i.e. not another `[`, `.`, or Java identifier char). Rejects `src[i].clone()`
		// or similar suffix pollution where the textual prefix matches but the
		// expression is not a bare indexed read.
		if (value.length() > expectedRhs.length()) {
			final var after = value.charAt(expectedRhs.length());
			if (after == '.' || after == '[' || Character.isJavaIdentifierPart(after))
				return null;
		}

		return new FixResult(startLine, endLine, List.of(indent + "System.arraycopy(" + src + ", 0, " + dst + ", 0, " + src + ".length);"));
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixArrayFill(int startLine, int endLine, @Nonnull String indent, @Nonnull String loopText) {
		final var arr = substringBetweenIdentBoundary(loopText, "< ", ".length");
		if (arr == null)
			return null;
		final var assignIdx = loopText.indexOf("] = ");
		if (assignIdx < 0)
			return null;
		final var value = extractValueUpToSemicolon(loopText.substring(assignIdx + 4));
		if (value.isEmpty())
			return null;
		return new FixResult(startLine, endLine, List.of(indent + "Arrays.fill(" + arr + ", " + value + ");"), Set.of("java.util.Arrays"));
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixEntrySetPutAll(int startLine, int endLine, @Nonnull String indent, @Nonnull String loopText) {
		final var map = substringBetween(loopText, ": ", ".entrySet()");
		if (map == null)
			return null;
		final var targetName = extractTargetName(loopText, ".put(");
		if (targetName == null)
			return null;
		return new FixResult(startLine, endLine, List.of(indent + targetName + ".putAll(" + map + ");"));
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixForEachAddAll(int startLine, int endLine, @Nonnull String indent, @Nonnull String loopText) {
		final var source = extractForEachSource(loopText);
		if (source == null)
			return null;
		final var targetName = extractTargetName(loopText, ".add(");
		if (targetName == null)
			return null;
		return new FixResult(startLine, endLine, List.of(indent + targetName + ".addAll(" + source + ");"));
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixForEachLambda(@Nonnull List<String> lines, int lineIndex, int column) {
		final var rawLine = lines.get(lineIndex);

		// Locate the `(` of `.forEach(` in the RAW line so all downstream
		// consumers (findClosingParen, stripCommentsJoined) use consistent
		// coordinates. Prefer `column` (AST-derived, raw-source coordinate);
		// fall back to structural search which skips literals/comments.
		final int openParenCol;
		if (column >= ".forEach".length()
				&& column < rawLine.length()
				&& rawLine.charAt(column) == '('
				&& rawLine.startsWith(".forEach", column - ".forEach".length()))
			openParenCol = column;
		else {
			final var found = LambdaCallParser.indexOfStructural(rawLine, ".forEach(");
			if (found < 0)
				return null;
			openParenCol = found + ".forEach".length();
		}
		final var forEachCol = openParenCol - ".forEach".length();

		// Extract source via backward scan for a simple dotted identifier.
		final var receiverStart = LambdaCallParser.extractReceiverStart(rawLine, forEachCol);
		final var source = rawLine.substring(receiverStart, forEachCol);
		if (source.isEmpty() || source.startsWith(".") || source.endsWith("."))
			return null;

		// Leading content before the receiver (includes indent and any statement prefix like
		// `if (flag) `). Bail for nested cases: unclosed parens (arg list), unclosed braces
		// (block start), or `->` (inside a lambda body). All checks use structural scanning
		// to avoid false matches inside string literals.
		final var leading = rawLine.substring(0, receiverStart);
		if (LambdaCallParser.indexOfStructural(leading, "->") >= 0
				|| LambdaCallParser.hasUnclosedParen(leading)
				|| LambdaCallParser.hasUnclosedBrace(leading))
			return null;

		// Find the matching `)` of the forEach call (may span multiple lines).
		final var closeLoc = LambdaCallParser.findClosingParen(lines, lineIndex, openParenCol);
		if (closeLoc == null)
			return null;
		final var endLine = closeLoc.line();

		// Preserve trailing content on the closing line (everything after `)`, typically `;`
		// plus any following statements or comments).
		final var trailing = lines.get(endLine).substring(closeLoc.col() + 1);

		// Build the inner text (everything between `(` and the matching `)`, with
		// lines joined by single spaces). Uses a shared scanner state so multi-line
		// block comments are correctly stripped across line boundaries.
		final var inner = LambdaCallParser.stripCommentsJoined(lines, lineIndex, openParenCol + 1, endLine);

		// Method reference: target::put or other::add. Uses structural scanning
		// to skip `::` inside string literals in the forEach body.
		final var doubleColonIdx = LambdaCallParser.indexOfStructural(inner, "::");
		if (doubleColonIdx >= 0) {
			final var refTarget = inner.substring(0, doubleColonIdx).strip();
			if (refTarget.isEmpty())
				return null;
			final var closeParen = LambdaCallParser.indexOfStructuralChar(inner, doubleColonIdx, ')');
			if (closeParen < 0)
				return null;
			final var methodName = inner.substring(doubleColonIdx + 2, closeParen).strip();
			if ("put".equals(methodName))
				return new FixResult(lineIndex, endLine, List.of(leading + refTarget + ".putAll(" + source + ")" + trailing));
			if ("add".equals(methodName))
				return new FixResult(lineIndex, endLine, List.of(leading + refTarget + ".addAll(" + source + ")" + trailing));
			return null;
		}

		// Lambda: (k, v) -> target.put(k, v) / item -> other.add(item) / block-body variants.
		// All searches use structural scanning to skip tokens inside string literals.
		final var arrowIdx = LambdaCallParser.indexOfStructural(inner, "->");
		if (arrowIdx < 0)
			return null;

		final var putIdx = LambdaCallParser.indexOfStructural(inner.substring(arrowIdx + 2), ".put(");
		if (putIdx >= 0) {
			final var target = extractLambdaTarget(inner.substring(arrowIdx + 2, arrowIdx + 2 + putIdx));
			if (target == null)
				return null;
			return new FixResult(lineIndex, endLine, List.of(leading + target + ".putAll(" + source + ")" + trailing));
		}

		final var addIdx = LambdaCallParser.indexOfStructural(inner.substring(arrowIdx + 2), ".add(");
		if (addIdx >= 0) {
			final var target = extractLambdaTarget(inner.substring(arrowIdx + 2, arrowIdx + 2 + addIdx));
			if (target == null)
				return null;
			return new FixResult(lineIndex, endLine, List.of(leading + target + ".addAll(" + source + ")" + trailing));
		}

		return null;
	}

	@CheckReturnValue
	@Nullable
	private static FixResult fixIndexedAddAll(int startLine, int endLine, @Nonnull String indent, @Nonnull String loopText) {
		final var source = substringBetween(loopText, "< ", ".size()");
		if (source == null)
			return null;
		final var targetName = extractTargetName(loopText, ".add(");
		if (targetName == null)
			return null;
		return new FixResult(startLine, endLine, List.of(indent + targetName + ".addAll(" + source + ");"));
	}

	@CheckReturnValue
	@Nullable
	private static String substringBetween(@Nonnull String text, @Nonnull String start, @Nonnull String end) {
		final var startIdx = text.indexOf(start);
		if (startIdx < 0)
			return null;
		final var afterStart = startIdx + start.length();
		final var endIdx = text.indexOf(end, afterStart);
		if (endIdx < 0)
			return null;
		return text.substring(afterStart, endIdx).strip();
	}

	/**
	 * Like {@link #substringBetween}, but requires the character immediately after
	 * {@code end} to NOT be a Java identifier part. Prevents matching when {@code end}
	 * is a prefix of a longer identifier (e.g. {@code .length} matching {@code .lengthArray}).
	 */
	@CheckReturnValue
	@Nullable
	private static String substringBetweenIdentBoundary(@Nonnull String text, @Nonnull String start, @Nonnull String end) {
		final var startIdx = LambdaCallParser.indexOfStructural(text, start);
		if (startIdx < 0)
			return null;
		final var afterStart = startIdx + start.length();
		var endIdx = text.indexOf(end, afterStart);
		while (endIdx >= 0) {
			final var afterEnd = endIdx + end.length();
			if (afterEnd >= text.length() || !Character.isJavaIdentifierPart(text.charAt(afterEnd)))
				return text.substring(afterStart, endIdx).strip();
			endIdx = text.indexOf(end, afterEnd);
		}
		return null;
	}

	@Nullable
	@Override
	public FixResult fix(@Nonnull List<String> lines, int lineIndex, int column) {
		final var line = lines.get(lineIndex);
		final var indent = extractIndent(line);
		final var stripped = LambdaCallParser.stripComment(line).stripLeading();

		if (stripped.contains(".forEach("))
			return fixForEachLambda(lines, lineIndex, column);

		if (!stripped.startsWith("for"))
			return null;

		final var endLine = findLoopEndLine(lines, lineIndex);
		if (endLine < 0)
			return null;

		final var loopText = LambdaCallParser.stripCommentsJoined(lines, lineIndex, 0, endLine);

		if (loopText.contains(".entrySet()") && loopText.contains(".put("))
			return fixEntrySetPutAll(lineIndex, endLine, indent, loopText);

		if (loopText.contains(": ") && loopText.contains(".add("))
			return fixForEachAddAll(lineIndex, endLine, indent, loopText);

		if (loopText.contains(".size()") && loopText.contains(".add(") && loopText.contains(".get("))
			return fixIndexedAddAll(lineIndex, endLine, indent, loopText);

		if (loopText.contains(".length") && loopText.contains("] = ")) {
			// Try arraycopy first; if the RHS doesn't match the `src[i]` pattern, fall back
			// to fill. This correctly routes fill values that happen to contain `[` (e.g.
			// `arr[i] = -a[b[0]]`) to `fixArrayFill` instead of producing a self-copy.
			final var copyResult = fixArrayCopy(lineIndex, endLine, indent, loopText);
			if (copyResult != null)
				return copyResult;
			return fixArrayFill(lineIndex, endLine, indent, loopText);
		}

		return null;
	}
}