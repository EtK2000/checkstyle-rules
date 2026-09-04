package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.LineText;
import com.etk2000.checkstyle.PreferStandardCharsetsCheck;
import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Dispatches on the violation key, since only the check can tell a charset literal a
 * {@code Charset} overload accepts from one passed to a plain {@code String} parameter. Any other
 * key, or none, is refused, because rewriting a literal no overload is proven to accept as a
 * {@code Charset} produces code that does not compile.
 */
class PreferStandardCharsetsFixer implements CheckstyleFixer {
	private static final String STANDARD_CHARSETS_IMPORT = "java.nio.charset.StandardCharsets";

	/**
	 * Replaces the {@code forName} call whose argument is the literal at the violation with the
	 * {@code StandardCharsets} constant, receiver through closing paren. Refuses a call standing
	 * alone as a statement or lambda body, where a bare constant may not compile, and one spanning
	 * more than one line or holding a comment, which the one-line replacement would drop.
	 */
	@CheckReturnValue
	@Nonnull
	private static FixAttempt fixForName(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		// locate clamps the column onto the line, so an out-of-range one could still land on a literal
		if (lineIndex < 0 || lineIndex >= lines.size() || charColumn < 0 || charColumn >= lines.get(lineIndex).length())
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
		final var call = FixerAst.locate(lines, lineIndex, charColumn, PreferStandardCharsetsCheck::forNameCallAt);
		if (call == null)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
		final var literalText = call.findFirstToken(TokenTypes.ELIST).getFirstChild().getFirstChild().getText();
		final var constant = PreferStandardCharsetsCheck.standardCharsetConstant(literalText.substring(1, literalText.length() - 1));
		if (constant == null)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
		if (PreferStandardCharsetsCheck.isStatementExpression(call))
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_FOR_NAME_STATEMENT);
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		if (AstQuery.firstLine(call) != lineIndex + 1 || rparen.getLineNo() != lineIndex + 1)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_FOR_NAME_MULTILINE);

		final var line = lines.get(lineIndex);
		final var start = LineText.charIndexOfColumn(line, AstQuery.firstColumn(call));
		final var end = LineText.charIndexOfColumn(line, rparen.getColumnNo()) + 1;
		final var withoutLiteral = line.substring(start, charColumn) + line.substring(charColumn + literalText.length(), end);
		if (withoutLiteral.contains("/*") || withoutLiteral.contains("//"))
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_FOR_NAME_COMMENT);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(line.substring(0, start) + "StandardCharsets." + constant + line.substring(end)),
				Set.of(STANDARD_CHARSETS_IMPORT)
		);
	}

	/**
	 * Replaces the charset literal at the violation with the {@code StandardCharsets} constant.
	 * Refuses when no literal sits at the column or it is a {@code forName} argument (the event is
	 * stale, and only the whole call may be replaced), and when a catch around it would stop
	 * compiling once the {@code Charset} overload no longer throws.
	 */
	@CheckReturnValue
	@Nullable
	private static FixAttempt fixLiteral(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var line = lines.get(lineIndex);
		if (charColumn < 0 || charColumn >= line.length())
			return null;

		if (line.charAt(charColumn) != '"')
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
		final var closeQuote = line.indexOf('"', charColumn + 1);
		if (closeQuote < 0)
			return null;

		final var charsetName = line.substring(charColumn + 1, closeQuote);
		final var constant = PreferStandardCharsetsCheck.standardCharsetConstant(charsetName);
		if (constant == null)
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP);
		final var literal = FixerAst.locate(
				lines,
				lineIndex,
				charColumn,
				(root, row, column) -> AstQuery.findNodeAt(root, row, column, node -> node.getType() == TokenTypes.STRING_LITERAL)
		);
		if (literal == null || PreferStandardCharsetsCheck.isForNameArgument(literal))
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
		if (PreferStandardCharsetsCheck.isCaughtAsEncodingFailure(literal))
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_CAUGHT);

		final var newLine = line.substring(0, charColumn) + "StandardCharsets." + constant
				+ line.substring(closeQuote + 1);
		return new FixResult(
				lineIndex,
				lineIndex,
				List.of(newLine),
				Set.of(STANDARD_CHARSETS_IMPORT)
		);
	}

	@CheckReturnValue
	@Nullable
	@Override
	public FixAttempt fix(@Nonnull List<String> lines, int lineIndex, int charColumn) {
		final var key = FixContext.getViolationKey();
		if (PreferStandardCharsetsCheck.MSG_SPECIFIC.equals(key))
			return fixLiteral(lines, lineIndex, charColumn);
		if (PreferStandardCharsetsCheck.MSG_FOR_NAME.equals(key))
			return fixForName(lines, lineIndex, charColumn);
		if (PreferStandardCharsetsCheck.MSG_GENERIC.equals(key))
			return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_VARIABLE);
		return new SkipResult(SkipMessages.PREFER_STANDARD_CHARSETS_SKIP_UNPROVEN);
	}
}