package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Collapses a {@code while} loop and the statement it duplicates into a
 * {@code do-while}.
 *
 * <p>Which of the two do-while forms to emit is
 * {@link ControlFlowBracesCheck}'s rule, so the tier is read from
 * {@link ControlFlowBracesCheck#determineTier} off the loop's own body rather
 * than assumed: a body whose assignment has a binary right-hand side, a
 * {@code new} expression, or a chained call belongs on its own line, and
 * emitting it on the {@code do} line produces output that check then flags.
 */
class PreferDoWhileFixer implements CheckstyleFixer {
	/** The tier {@link ControlFlowBracesCheck#determineTier} returns for a body that stays on the {@code do} line. */
	private static final int TIER_2 = 2;

	private static final Pattern WHILE_LINE = Pattern.compile(
			"^(\\s*)while\\s*\\((.+)\\)\\s*(\\{)?\\s*$"
	);

	/**
	 * The formatting tier of the loop body reported at {@code (lineIndex, column)},
	 * or {@code null} when the buffer does not parse or no {@code while} sits
	 * there. The body is unwrapped out of its {@code SLIST} the same way
	 * {@code ControlFlowBracesCheck.shapeAt} does, because collapsing the braces
	 * preserves the inner statement's tier.
	 */
	@CheckReturnValue
	@Nullable
	private static Integer bodyTier(@Nonnull List<String> lines, int lineIndex, int column) {
		return FixerAst.withAst(
				lines,
				root -> {
				final var whileAst = AstQuery.findNodeAt(
						root, lineIndex, column, n -> n.getType() == TokenTypes.LITERAL_WHILE
				);
				if (whileAst == null)
					return null;
				final var rparen = whileAst.findFirstToken(TokenTypes.RPAREN);
				final var body = rparen == null ? null : rparen.getNextSibling();
				if (body == null)
					return null;
				final var tierBody = body.getType() == TokenTypes.SLIST ? body.getFirstChild() : body;
				return tierBody == null ? null : ControlFlowBracesCheck.determineTier(tierBody);
				}
		);
	}

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

		final var tier = bodyTier(lines, lineIndex, column);
		if (tier == null)
			return new SkipResult(SkipMessages.CONTROL_FLOW_SKIP_NO_TIER);

		final var whileLine = indent + "while (" + cond + ");";
		return new FixResult(
				lineIndex - 1,
				endLine,
				tier == TIER_2
						? List.of(indent + "do " + bodyStripped, whileLine)
						: List.of(indent + "do", indent + "\t" + bodyStripped, whileLine)
		);
	}
}