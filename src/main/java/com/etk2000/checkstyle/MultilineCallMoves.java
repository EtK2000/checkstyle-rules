package com.etk2000.checkstyle;

import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_CLOSING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_LAMBDA_NOT_ON_CLOSING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_LAMBDA_NOT_ON_OPENING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_OPENING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_SHARED_LINE;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_TERNARY_COLON_LINE;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_TERNARY_NOT_ON_CLOSING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_TERNARY_NOT_ON_OPENING;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.MSG_TERNARY_QUESTION_LINE;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.containsChainedConstructor;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.findFirstArg;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.findLastArg;
import static com.etk2000.checkstyle.MultilineCallFormattingCheck.isDirectBracelessLambda;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.ArgLayoutClassifier;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Answers, for one reported {@link MultilineCallFormattingCheck} violation coordinate, what the
 * fixer is allowed to do about it: the plan for moving a call's opening or closing paren, or the
 * call/definition/ternary node whose whole argument list a reformatter should re-emit.
 * <p>
 * Every entry point but {@link #resolvablePostDelayed} re-runs
 * {@link MultilineCallFormattingCheck#analyzeLayout}, the same pass the check logs from, so the two
 * can never disagree about what is a violation. Each returns {@code null} when the coordinate is not
 * the violation it handles, or when the call carries a second violation the move would leave behind:
 * the fix pipeline reruns to convergence, so a partial fix that cannot converge must be deferred
 * rather than applied.
 */
public final class MultilineCallMoves {
	/**
	 * A plan for moving a call's closing paren to satisfy a closing-paren violation, computed by
	 * {@link #closingParenMove}. When {@code pullUp}, the {@code )} on {@code rparenLine} joins onto
	 * {@code argLastLine} (stacking with an inline block's brace or rejoining a single-line ternary).
	 * Otherwise the {@code )} (and any trailing content) on {@code rparenLine} splits onto its own
	 * line, indented to match {@code openLine}. All line numbers are 1-based; {@code rparenColumn} is
	 * the 0-based char column of the {@code )} on its line.
	 */
	public record ClosingParenMove(boolean pullUp, int argLastLine, int rparenLine, int openLine, int rparenColumn) {}

	/**
	 * A plan for moving a call's argument onto (or off) its opening paren line, computed by
	 * {@link #openingParenMove}. Two shapes:
	 * <ul>
	 * <li>{@code pushDown}: a plain call whose first argument shares the {@code (} line. The fixer
	 * splits the {@code (} line at {@code openParenColumn} (0-based char index of {@code (} on
	 * {@code openLine}) and moves the trailing arguments onto their own line.</li>
	 * <li>pull-up (when {@code !pushDown}): an inline-block or ternary argument that must start on the
	 * {@code (} line. The fixer joins source lines {@code [openLine .. headJoinEnd]} onto the {@code (}
	 * line, keeps the interior lines (shifted left), and joins {@code [tailJoinStart .. closeLine]} into
	 * the trailing line. When {@code valueSingleLine} the whole {@code [openLine .. closeLine]} span
	 * collapses to one line. {@code ternaryQuestion} is the ternary's {@code QUESTION} node, non-null only
	 * for a ternary opening. {@code tailJoinStart == closeLine} means the {@code )} stays on its own line
	 * (chained constructor / braceless lambda); otherwise it stacks with the value's last brace/paren.</li>
	 * </ul>
	 * All line numbers are 1-based.
	 */
	public record OpeningParenMove(
			boolean pushDown,
			boolean valueSingleLine,
			int openLine,
			int openParenColumn,
			int headJoinEnd,
			int tailJoinStart,
			int closeLine,
			@Nullable DetailAST ternaryQuestion
	) {}

	/**
	 * Computes how to move a call's closing paren to satisfy a closing-paren violation at the given
	 * 0-based {@code line}/{@code column} (the reported {@code )} position), or {@code null} when the
	 * coordinates are not a closing-paren violation OR the call also has another (opening/shared/
	 * ternary-internal) violation.
	 */
	@CheckReturnValue
	@Nullable
	public static ClosingParenMove closingParenMove(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var rparen = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var call = rparen.getParent();
		if (call == null)
			return null;

		final var check = primed(root, lines);
		final var violations = check.analyzeLayout(call);
		if (violations.size() != 1)
			return null;

		final var pullUp = switch (violations.getFirst().messageKey()) {
			case MSG_CLOSING -> false;
			case MSG_LAMBDA_NOT_ON_CLOSING, MSG_TERNARY_NOT_ON_CLOSING -> true;
			default -> null;
		};
		if (pullUp == null)
			return null;

		final var lparen = call.findFirstToken(TokenTypes.LPAREN);
		final var openLine = lparen != null ? lparen.getLineNo() : call.getLineNo();

		final var lastArg = findLastArg(call);
		if (lastArg == null)
			return null;

		return new ClosingParenMove(pullUp, AstQuery.lastLine(lastArg), rparen.getLineNo(), openLine, rparen.getColumnNo());
	}

	@CheckReturnValue
	private static boolean isArgListChild(@Nonnull DetailAST node) {
		final var parent = node.getParent();
		if (parent == null)
			return false;
		final var parentType = parent.getType();
		return (parentType == TokenTypes.ELIST || parentType == TokenTypes.PARAMETERS)
				&& node.getType() != TokenTypes.COMMA;
	}

	@CheckReturnValue
	private static boolean isArgListChildOrOwnerToken(@Nonnull DetailAST node) {
		return isArgListChild(node) || switch (node.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.LITERAL_NEW, TokenTypes.LPAREN, TokenTypes.METHOD_CALL,
					TokenTypes.METHOD_DEF, TokenTypes.RPAREN, TokenTypes.SUPER_CTOR_CALL -> true;
			default -> false;
		};
	}

	/**
	 * The AST node types {@link #openingParenMove} accepts at the reported coordinate to locate the
	 * enclosing call: a call's opening/closing paren, or a paren-less call token.
	 */
	@CheckReturnValue
	private static boolean isCallOrParen(@Nonnull DetailAST node) {
		return switch (node.getType()) {
			case TokenTypes.LPAREN, TokenTypes.METHOD_CALL, TokenTypes.RPAREN, TokenTypes.SUPER_CTOR_CALL -> true;
			default -> false;
		};
	}

	/**
	 * A special inline method call ({@code List.of} etc.) with a fully-qualified (dotted) receiver
	 * such as {@code java.util.List.of}. The opening pull-up is deferred (skipped) while the receiver
	 * is still fully qualified: pulling it up in the same pipeline pass would consume the receiver's
	 * line before {@code PreferImportFixer} shortens the FQN, and {@code UnusedImports} would then strip
	 * the (still FQN-unused) import, stranding the FQN. Skipping lets {@code PreferImportFixer} shorten
	 * {@code java.util.List.of} to {@code List.of} first; on a later pass the receiver is a plain
	 * {@code List} and the pull-up proceeds normally.
	 */
	@CheckReturnValue
	private static boolean isFqnSpecialInlineCall(@Nonnull DetailAST arg) {
		final var node = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		if (node == null || node.getType() != TokenTypes.METHOD_CALL || !ArgLayoutClassifier.isStaticSpecialInlineMethodCall(node))
			return false;
		final var dot = node.getFirstChild();
		return dot != null && dot.getType() == TokenTypes.DOT
				&& dot.getFirstChild() != null && dot.getFirstChild().getType() == TokenTypes.DOT;
	}

	@CheckReturnValue
	private static boolean isPostDelayedTargetToken(@Nonnull DetailAST node) {
		if (node.getType() == TokenTypes.METHOD_CALL)
			return ArgLayoutClassifier.isPostDelayedWithInlineBlock(node);
		final var parent = node.getParent();
		return node.getType() == TokenTypes.RPAREN && parent != null && parent.getType() == TokenTypes.METHOD_CALL
				&& ArgLayoutClassifier.isPostDelayedWithInlineBlock(parent);
	}

	@CheckReturnValue
	private static boolean isTernaryOperator(@Nonnull DetailAST ast) {
		return ast.getType() == TokenTypes.QUESTION || ast.getType() == TokenTypes.COLON;
	}

	/**
	 * Computes how to move a call's argument onto (or off) its opening paren line for an
	 * opening-paren violation ({@code multiline.args.on.opening.paren},
	 * {@code multiline.lambda.not.on.opening.paren}, {@code multiline.ternary.not.on.opening.paren}) at
	 * the given 0-based {@code line}/{@code column}. Returns {@code null} when the coordinates do not
	 * resolve to a call with an opening-paren violation, or when the call also carries a violation this
	 * move would not resolve. Locates the call from either its opening or its closing token, so the
	 * fixer reaches the same whole-span plan whether invoked at the opening or the closing violation.
	 */
	@CheckReturnValue
	@Nullable
	public static OpeningParenMove openingParenMove(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, MultilineCallMoves::isCallOrParen);
		if (node == null)
			return null;
		final var call = switch (node.getType()) {
			case TokenTypes.LPAREN, TokenTypes.RPAREN -> node.getParent();
			default -> node;
		};
		if (call == null)
			return null;

		final var check = primed(root, lines);
		final var violations = check.analyzeLayout(call);

		String openKey = null;
		for (var v : violations) {
			switch (v.messageKey()) {
				case MSG_LAMBDA_NOT_ON_OPENING, MSG_OPENING, MSG_TERNARY_NOT_ON_OPENING -> openKey = v.messageKey();
				default -> {}
			}
		}
		if (openKey == null)
			return null;

		final var lparen = call.findFirstToken(TokenTypes.LPAREN);
		final int openLine, openParenColumn;
		if (lparen != null) {
			openLine = lparen.getLineNo();
			openParenColumn = lparen.getColumnNo();
		}
		else {
			openLine = call.getLineNo();
			openParenColumn = call.getColumnNo();
		}
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;

		if (openKey.equals(MSG_OPENING)) {
			// plain push-down resolves only the opening violation, so require it to be the sole one
			if (violations.size() != 1)
				return null;
			return new OpeningParenMove(true, false, openLine, openParenColumn, 0, 0, rparen.getLineNo(), null);
		}

		// pull-up: the whole-span re-emission also fixes an accompanying closing violation, but any
		// shared-line or ternary-internal violation would survive, so refuse those
		final var allowedOther = openKey.equals(MSG_LAMBDA_NOT_ON_OPENING)
				? Set.of(MSG_CLOSING, MSG_LAMBDA_NOT_ON_CLOSING)
				: Set.<String>of();
		for (var v : violations) {
			final var key = v.messageKey();
			if (key.equals(openKey) || allowedOther.contains(key))
				continue;
			return null;
		}

		// the argument that must land on the ( line is the inline block / ternary: for postDelayed the
		// braced lambda is the FIRST arg (delay follows it), everywhere else it is the last arg
		final var blockArg = ArgLayoutClassifier.isPostDelayedWithInlineBlock(call) ? findFirstArg(call) : findLastArg(call);
		if (blockArg == null || isFqnSpecialInlineCall(blockArg))
			return null;
		final var argFirstLine = AstQuery.firstLine(blockArg);
		final var argLastLine = AstQuery.lastLine(blockArg);
		final var valueSingleLine = argFirstLine == argLastLine;
		// the ) stays on its own line for a multi-line ternary (condition on the ( line, ?/: and )
		// each on their own line), a chained constructor, or a braceless lambda; otherwise it stacks
		// with the value's last brace/paren (braced lambda, anonymous class, special/method-call arg).
		final var closingOnOwnLine = openKey.equals(MSG_TERNARY_NOT_ON_OPENING)
				|| containsChainedConstructor(blockArg) || isDirectBracelessLambda(blockArg);
		final var tailJoinStart = !valueSingleLine && closingOnOwnLine ? rparen.getLineNo() : argLastLine;
		final var ternaryArg = openKey.equals(MSG_TERNARY_NOT_ON_OPENING)
				? (blockArg.getType() == TokenTypes.EXPR ? blockArg.getFirstChild() : blockArg)
				: null;
		final var ternaryQuestion = ternaryArg != null && ternaryArg.getType() == TokenTypes.QUESTION ? ternaryArg : null;
		return new OpeningParenMove(
				false,
				valueSingleLine,
				openLine,
				openParenColumn,
				argFirstLine,
				tailJoinStart,
				rparen.getLineNo(),
				ternaryQuestion
		);
	}

	/**
	 * A check instance wired to the source the caller parsed its AST from, so
	 * {@link MultilineCallFormattingCheck#analyzeLayout} sees the same context receivers and the same
	 * line text the running check would have seen for this file.
	 */
	@CheckReturnValue
	@Nonnull
	private static MultilineCallFormattingCheck primed(@Nonnull DetailAST root, @Nonnull List<String> lines) {
		final var check = new MultilineCallFormattingCheck();
		check.contextReceivers.prime(root);
		check.primeLines(lines);
		return check;
	}

	/**
	 * Returns the call/definition owner (a {@code METHOD_CALL}, {@code LITERAL_NEW},
	 * {@code SUPER_CTOR_CALL}, {@code METHOD_DEF} or {@code CTOR_DEF}) of a plain opening/closing-paren
	 * violation ({@code multiline.args.on.opening.paren} / {@code .on.closing.paren}) at the given
	 * 0-based {@code line}/{@code column}, or {@code null} when the coordinates are not such a violation
	 * or the call also carries a violation a whole-list re-emission would not resolve (a shared-line,
	 * ternary, or inline-block move).
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableArgListOwner(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, MultilineCallMoves::isArgListChildOrOwnerToken);
		if (node == null)
			return null;
		final DetailAST owner;
		if (node.getType() == TokenTypes.LPAREN || node.getType() == TokenTypes.RPAREN)
			owner = node.getParent();
		else if (isArgListChild(node))
			owner = node.getParent() != null ? node.getParent().getParent() : null;
		else
			owner = node;
		if (owner == null)
			return null;

		final var check = primed(root, lines);

		var hasOpeningOrClosing = false;
		for (var v : check.analyzeLayout(owner)) {
			switch (v.messageKey()) {
				case MSG_CLOSING, MSG_OPENING -> hasOpeningOrClosing = true;
				// a shared-line, ternary, or inline-block move would survive the plain re-emission, so defer
				default -> {
					return null;
				}
			}
		}
		return hasOpeningOrClosing ? owner : null;
	}

	/**
	 * Returns the {@code postDelayed} {@code METHOD_CALL} (a braced-lambda first argument + delay second
	 * argument) whose layout violation sits at the given 0-based {@code line}/{@code column} (the
	 * reported {@code (} or {@code )} position), or {@code null} when the coordinates are not such a
	 * call or it is already a single line. The {@code postDelayed} branch of
	 * {@link MultilineCallFormattingCheck#analyzeLayout} produces only reshape-resolvable violations,
	 * so this structural match needs no re-run of the analyzer, which is why it is the one entry point
	 * that takes no lines.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvablePostDelayed(@Nonnull DetailAST root, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, MultilineCallMoves::isPostDelayedTargetToken);
		if (node == null)
			return null;
		final var call = node.getType() == TokenTypes.METHOD_CALL ? node : node.getParent();
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null || rparen.getLineNo() == call.getLineNo())
			return null;
		return call;
	}

	/**
	 * Returns the call/definition owner (a {@code METHOD_CALL}, {@code LITERAL_NEW},
	 * {@code SUPER_CTOR_CALL}, {@code METHOD_DEF} or {@code CTOR_DEF}) of a shared-line argument
	 * violation ({@code multiline.args.shared.line}) at the given 0-based {@code line}/{@code column}
	 * (the reported argument position), or {@code null} when the coordinates are not such a violation or
	 * the call also carries a violation a whole-list re-emission would not resolve.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableSharedLineArgs(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var arg = AstQuery.findNodeAt(root, line, column, MultilineCallMoves::isArgListChild);
		if (arg == null)
			return null;
		final var owner = arg.getParent().getParent();
		if (owner == null)
			return null;

		final var check = primed(root, lines);

		var hasShared = false;
		for (var v : check.analyzeLayout(owner)) {
			switch (v.messageKey()) {
				// a whole-list re-emission also resolves the opening/closing rules on the same call
				case MSG_CLOSING, MSG_OPENING -> {}
				case MSG_SHARED_LINE -> hasShared = true;
				// any other violation (ternary/lambda) means this is not a general shared-line owner
				default -> {
					return null;
				}
			}
		}
		return hasShared ? owner : null;
	}

	/**
	 * Returns the {@code QUESTION} node of a ternary argument whose internal layout is wrong
	 * ({@code multiline.ternary.question.wrong.line} or {@code multiline.ternary.colon.wrong.line}) at
	 * the given 0-based {@code line}/{@code column} (the reported {@code ?}/{@code :} position), or
	 * {@code null} when the coordinates are not such a violation or the ternary also carries a violation
	 * a whole-ternary re-emission would not resolve.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableTernaryLayoutQuestion(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, MultilineCallMoves::isTernaryOperator);
		if (node == null)
			return null;
		final var question = node.getType() == TokenTypes.QUESTION ? node : node.getParent();
		if (question == null || question.getType() != TokenTypes.QUESTION)
			return null;

		// the ternary must be a bare call argument (EXPR -> ELIST -> call) for analyzeLayout to classify it
		final var expr = question.getParent();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return null;
		final var elist = expr.getParent();
		if (elist == null)
			return null;
		final var call = elist.getParent();
		if (call == null)
			return null;

		final var check = primed(root, lines);

		var hasInternal = false;
		for (var v : check.analyzeLayout(call)) {
			switch (v.messageKey()) {
				// a whole-ternary re-emission also resolves the opening/closing rules on the same ternary
				case MSG_CLOSING, MSG_TERNARY_NOT_ON_CLOSING, MSG_TERNARY_NOT_ON_OPENING -> {}
				case MSG_TERNARY_COLON_LINE, MSG_TERNARY_QUESTION_LINE -> hasInternal = true;
				// any other violation (e.g. shared-line) would survive the re-emission, so defer
				default -> {
					return null;
				}
			}
		}
		return hasInternal ? question : null;
	}

	private MultilineCallMoves() {
	}
}