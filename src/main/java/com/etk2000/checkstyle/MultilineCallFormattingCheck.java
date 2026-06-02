package com.etk2000.checkstyle;

import com.etk2000.checkstyle.format.ArgLayoutClassifier;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces multiline call/signature formatting:
 * no arguments on the opening paren line, and no arguments on the closing paren line.
 * <p>
 * Exception ("ternary"): calls with exactly one argument that is a ternary expression. The
 * condition stays on the opening paren line, and the closing paren goes on its own line (not on
 * the same line as the last ternary branch).
 * <p>
 * Exception ("inline block"): calls with exactly one argument that is a lambda, anonymous class,
 * constructor call, or special processing method ({@code List.of}, {@code Map.of},
 * {@code Arrays.asList}, {@code Context.getString},
 * {@code Context.getResources().getQuantityString}) are exempt: the argument stays on the
 * opening paren line and its closing brace/paren on the closing paren line. For braceless
 * (expression) lambdas that extend past the opening line, the closing paren goes on its own line
 * instead. For constructor calls followed by method chaining (e.g. {@code new Foo().bar()}), the
 * constructor starts on the opening paren line and the closing paren goes on its own line (same
 * rule as braceless lambdas that extend past the opening line).
 * <p>
 * Exception ("method call arg"): calls with exactly one argument that is a plain method call.
 * The inner call stays on the opening paren line and closing parens are stacked on the same line.
 * This also applies when there are exactly two arguments and the first is {@code this} or an
 * Android resource identifier.
 * <p>
 * Both ternary and inline block exceptions also apply when there are exactly two arguments and
 * the first is {@code this} or an Android resource identifier ({@code R.xxx.yyy} or
 * {@code android.R.xxx.yyy}).
 * <p>
 * The inline block exception also applies to {@code Handler.postDelayed} with a braced lambda
 * as the first argument and the delay as the second.
 * <p>
 * The inline block exception also applies to {@code computeIfAbsent} with a braced lambda as the
 * second argument (the key stays on the opening paren line and the lambda's closing brace/paren on
 * the closing paren line). This is gated on the method name specifically, not on any two-argument
 * call with a lambda, because a {@code computeIfAbsent} key is short whereas an arbitrary method's
 * first argument may be long and should not be forced onto the opening line.
 * <p>
 * The inline block exception also applies to a two-argument {@code put} whose second argument is an
 * inline block (a chained constructor such as {@code new JSONObject().put(...)}, a lambda, or an
 * anonymous class): the key stays on the opening paren line and the value's closing brace/paren on
 * the closing paren line. Like {@code computeIfAbsent} this is gated on the method name
 * specifically, because a {@code put} key is short.
 * <p>
 * Separately, a {@code new JSONObject().put(k, v)} expression (a fresh {@code new JSONObject()} with
 * exactly one {@code .put}, not chained further) whose value is simple (a literal, variable, or
 * single non-chained method call, i.e. not a nested {@code new JSONObject()}, chained call, ternary,
 * or lambda) must be written on one line when its collapsed form fits within
 * {@link LineLength#MAX_LINE_LENGTH} columns; splitting the {@code new JSONObject()} from its
 * {@code .put} across lines is a violation. Multi-{@code put} builders and non-simple values stay
 * multiline.
 * <p>
 * {@code getString} is recognized with a known Context receiver: a variable assigned from
 * {@code requireContext()}/{@code getContext()}/{@code requireActivity()}/{@code getActivity()},
 * a parameter typed as {@code Context}, or calling directly on one of those methods.
 */
public class MultilineCallFormattingCheck extends AbstractAstCheck {
	/**
	 * A single layout violation the analyzer would log: the AST node the violation attaches to and
	 * its message key. Returned by {@link #analyzeLayout} so the check and the fixer share one
	 * analysis pass.
	 */
	private record LayoutViolation(@Nonnull DetailAST node, @Nonnull String messageKey) {}

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
	 * collapses to one line. For an inline-block value the opening violation is only emitted when that line
	 * fits within {@link LineLength#MAX_LINE_LENGTH}, so its pull-up always fits; a ternary is not gated, so
	 * when its one-line form exceeds the width the fixer re-lays it out multi-line via {@code ternaryQuestion}
	 * (the ternary's {@code QUESTION} node, non-null only for a ternary opening). {@code tailJoinStart ==
	 * closeLine} means the {@code )} stays on its own line (chained constructor / braceless lambda); otherwise
	 * it stacks with the value's last brace/paren.</li>
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

	private static final Set<String> CONTEXT_RETURNING_METHODS = Set.of(
			"getActivity", "getContext", "requireActivity", "requireContext"
	);

	private static final String MSG_CLOSING = "multiline.args.on.closing.paren";
	private static final String MSG_LAMBDA_NOT_ON_CLOSING = "multiline.lambda.not.on.closing.paren";
	private static final String MSG_LAMBDA_NOT_ON_OPENING = "multiline.lambda.not.on.opening.paren";
	private static final String MSG_OPENING = "multiline.args.on.opening.paren";
	private static final String MSG_POSTDELAYED_ONE_LINE = "multiline.postdelayed.one.line";
	private static final String MSG_PUT_COLLAPSIBLE = "multiline.put.collapsible";
	private static final String MSG_SHARED_LINE = "multiline.args.shared.line";
	private static final String MSG_TERNARY_COLON_LINE = "multiline.ternary.colon.wrong.line";
	private static final String MSG_TERNARY_NOT_ON_CLOSING = "multiline.ternary.not.on.closing.paren";
	private static final String MSG_TERNARY_NOT_ON_OPENING = "multiline.ternary.not.on.opening.paren";
	private static final String MSG_TERNARY_QUESTION_LINE = "multiline.ternary.question.wrong.line";

	/**
	 * Computes how to move a call's closing paren to satisfy a closing-paren violation at the given
	 * 0-based {@code line}/{@code column} (the reported {@code )} position), or {@code null} when the
	 * coordinates are not a closing-paren violation OR the call also has another (opening/shared/
	 * ternary-internal) violation, in which case the pipeline cannot converge by moving only the
	 * paren and the fixer must defer. Runs the same {@link #analyzeLayout} the check uses, so the
	 * two never disagree.
	 */
	@CheckReturnValue
	@Nullable
	public static ClosingParenMove closingParenMove(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var rparen = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var call = rparen.getParent();
		if (call == null)
			return null;

		final var check = new MultilineCallFormattingCheck();
		check.primeContextVars(root);
		check.primeLines(lines);
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

		return new ClosingParenMove(pullUp, AstUtil.lastLine(lastArg), rparen.getLineNo(), openLine, rparen.getColumnNo());
	}

	/**
	 * Fixer entry point: for the {@code new JSONObject().put(...)} collapse violation at the given
	 * coordinates, returns the {@code {fromLine, toLine}} (0-based, inclusive) of the enclosing
	 * statement the fixer must join onto one line, or {@code null} when the coordinates are not a
	 * collapsible split put. Reuses {@link #isCollapsibleJsonObjectPutShape} and
	 * {@link #enclosingStatement} so the fixer never re-derives the rule from text. The line-length
	 * gate is not re-applied here (the check gated it before emitting the violation; the fixer
	 * re-measures its own joined output).
	 */
	@CheckReturnValue
	@Nullable
	public static int[] collapsibleJsonObjectPutLineSpan(@Nonnull DetailAST root, int line, int column) {
		final var putCall = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.METHOD_CALL);
		if (putCall == null || !isCollapsibleJsonObjectPutShape(putCall))
			return null;
		final var receiver = putCall.getFirstChild().getFirstChild();
		if (receiver.getLineNo() == putCall.getLineNo())
			return null;
		final var statement = enclosingStatement(putCall);
		return new int[]{AstUtil.firstLine(statement) - 1, AstUtil.lastLine(statement) - 1};
	}

	@CheckReturnValue
	private static boolean containsChainedConstructor(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LITERAL_NEW && node.getParent() != null
					&& node.getParent().getType() == TokenTypes.DOT)
				return true;
			// don't recurse into code blocks or nested method call args
			if (node.getType() == TokenTypes.ELIST || node.getType() == TokenTypes.OBJBLOCK
					|| node.getType() == TokenTypes.SLIST)
				continue;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsLiteralNew(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.LITERAL_NEW)
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsMethodCall(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_CALL)
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * Walks up from an expression node to the statement (or member declaration) that contains it:
	 * the ancestor whose parent is a block ({@code SLIST}/{@code OBJBLOCK}), i.e. the whole
	 * {@code ...;} statement the collapse fixer joins onto one line.
	 */
	@CheckReturnValue
	@Nonnull
	public static DetailAST enclosingStatement(@Nonnull DetailAST node) {
		var current = node;
		while (current.getParent() != null) {
			final var parentType = current.getParent().getType();
			if (parentType == TokenTypes.SLIST || parentType == TokenTypes.OBJBLOCK)
				return current;
			current = current.getParent();
		}
		return current;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findFirstArg(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				yield params == null ? null : params.findFirstToken(TokenTypes.PARAMETER_DEF);
			}
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL -> {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				yield elist == null ? null : elist.getFirstChild();
			}
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findLastArg(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF -> {
				final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
				if (params == null)
					yield null;
				yield lastChildOfType(params, TokenTypes.PARAMETER_DEF);
			}
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL -> {
				final var elist = ast.findFirstToken(TokenTypes.ELIST);
				if (elist == null)
					yield null;
				yield lastNonCommaChild(elist);
			}
			default -> null;
		};
	}

	/**
	 * Whether {@code node} is an argument/parameter (a non-comma direct child of an {@code ELIST} or
	 * {@code PARAMETERS}), the node type {@link #resolvableSharedLineArgs} matches at a shared-line
	 * violation's reported coordinate before walking up to the owning call/definition.
	 */
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
	 * AST-shape test shared by the check and the fixer: a {@code put} call on a fresh
	 * {@code new JSONObject()} (not chained further), with exactly two arguments and a simple
	 * value. Does not check the split-across-lines or line-length conditions.
	 */
	@CheckReturnValue
	private static boolean isCollapsibleJsonObjectPutShape(@Nonnull DetailAST ast) {
		if (ast.getType() != TokenTypes.METHOD_CALL || !ArgLayoutClassifier.isMethodCallNamed(ast, "put"))
			return false;
		final var dot = ast.getFirstChild();
		final var parent = ast.getParent();
		if (dot == null || dot.getType() != TokenTypes.DOT || (parent != null && parent.getType() == TokenTypes.DOT))
			return false;
		final var receiver = dot.getFirstChild();
		if (receiver == null || !isNewJsonObject(receiver))
			return false;
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;
		DetailAST key = null, value = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (key == null)
					key = child;
				else if (value == null)
					value = child;
				else
					return false;
			}
		}
		return key != null && value != null && isSimpleValue(value);
	}

	@CheckReturnValue
	private static boolean isContextType(@Nonnull DetailAST type) {
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null && "Context".equals(ident.getText()))
			return true;

		// fully qualified: android.content.Context (DOT tree, last IDENT is "Context")
		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			for (var child = dot.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.IDENT && "Context".equals(child.getText()) && child.getNextSibling() == null)
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean isDirectBracelessLambda(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		return node != null && node.getType() == TokenTypes.LAMBDA
				&& node.findFirstToken(TokenTypes.SLIST) == null;
	}

	@CheckReturnValue
	private static boolean isDirectMethodCall(@Nonnull DetailAST ast) {
		final var node = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		return node != null && node.getType() == TokenTypes.METHOD_CALL;
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
	private static boolean isNewJsonObject(@Nonnull DetailAST node) {
		if (node.getType() != TokenTypes.LITERAL_NEW)
			return false;
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && "JSONObject".equals(ident.getText());
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
	private static boolean isSimpleValue(@Nonnull DetailAST arg) {
		var node = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		if (node == null || containsLiteralNew(node))
			return false;
		while (node.getType() == TokenTypes.UNARY_MINUS || node.getType() == TokenTypes.UNARY_PLUS) {
			node = node.getFirstChild();
			if (node == null)
				return false;
		}
		return switch (node.getType()) {
			case TokenTypes.CHAR_LITERAL, TokenTypes.IDENT, TokenTypes.LITERAL_FALSE,
			     TokenTypes.LITERAL_NULL, TokenTypes.LITERAL_TRUE, TokenTypes.NUM_DOUBLE,
			     TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG,
			     TokenTypes.STRING_LITERAL -> true;
			case TokenTypes.DOT -> !containsMethodCall(node);
			case TokenTypes.METHOD_CALL -> {
				final var dot = node.findFirstToken(TokenTypes.DOT);
				yield dot == null || !containsMethodCall(dot);
			}
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isSingleMethodCallArg(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		DetailAST onlyArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (onlyArg != null)
					return false;
				onlyArg = child;
			}
		}
		return onlyArg != null && isDirectMethodCall(onlyArg);
	}

	@CheckReturnValue
	private static boolean isTernaryOperator(@Nonnull DetailAST ast) {
		return ast.getType() == TokenTypes.QUESTION || ast.getType() == TokenTypes.COLON;
	}

	@CheckReturnValue
	private static boolean isThisAndMethodCallArgs(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;

		DetailAST firstArg = null, secondArg = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA) {
				if (firstArg == null)
					firstArg = child;
				else if (secondArg == null)
					secondArg = child;
				else
					return false;
			}
		}
		return firstArg != null && secondArg != null
				&& ArgLayoutClassifier.isCompactFirstArg(firstArg) && isDirectMethodCall(secondArg);
	}

	@CheckReturnValue
	private static boolean joinsTight(@Nonnull StringBuilder joined, @Nonnull String continuation) {
		if (joined.isEmpty() || continuation.isEmpty())
			return false;
		final var first = continuation.charAt(0);
		if (first == '.' || first == ')' || first == ',' || first == ';' || first == ']')
			return true;
		final var last = joined.charAt(joined.length() - 1);
		return last == '(' || last == '[';
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST lastChildOfType(@Nonnull DetailAST parent, int type) {
		DetailAST last = null;
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == type)
				last = child;
		}
		return last;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST lastNonCommaChild(@Nonnull DetailAST parent) {
		DetailAST last = null;
		for (var child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				last = child;
		}
		return last;
	}

	/**
	 * Computes how to move a call's argument onto (or off) its opening paren line for an
	 * opening-paren violation ({@code multiline.args.on.opening.paren},
	 * {@code multiline.lambda.not.on.opening.paren}, {@code multiline.ternary.not.on.opening.paren}) at
	 * the given 0-based {@code line}/{@code column}. Returns {@code null} when the coordinates do not
	 * resolve to a call with an opening-paren violation, or when the call also carries a violation this
	 * move would not resolve (so the pipeline could not converge). Locates the call from either its
	 * opening or its closing token, so the fixer reaches the same whole-span plan whether invoked at
	 * the opening or the (Group B) closing violation. Runs the same {@link #analyzeLayout} the check
	 * uses, so the two never disagree.
	 */
	@CheckReturnValue
	@Nullable
	public static OpeningParenMove openingParenMove(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstUtil.findNodeAt(root, line, column, MultilineCallFormattingCheck::isCallOrParen);
		if (node == null)
			return null;
		final var call = switch (node.getType()) {
			case TokenTypes.LPAREN, TokenTypes.RPAREN -> node.getParent();
			default -> node;
		};
		if (call == null)
			return null;

		final var check = new MultilineCallFormattingCheck();
		check.primeContextVars(root);
		check.primeLines(lines);
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
		final var argFirstLine = AstUtil.firstLine(blockArg);
		final var argLastLine = AstUtil.lastLine(blockArg);
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
	 * Fixer classification entry point: returns the call/definition owner (a {@code METHOD_CALL},
	 * {@code LITERAL_NEW}, {@code SUPER_CTOR_CALL}, {@code METHOD_DEF} or {@code CTOR_DEF}) of a plain
	 * opening/closing-paren violation ({@code multiline.args.on.opening.paren} /
	 * {@code .on.closing.paren}) at the given 0-based {@code line}/{@code column}, or {@code null} when
	 * the coordinates are not such a violation or the call also carries a violation a whole-list
	 * re-emission would not resolve (a shared-line, ternary, or inline-block move). Runs the same
	 * {@link #analyzeLayout} the check uses. The caller re-lays-out the source through
	 * {@code JavaArgListReformatter}, falling back to the push-down move when the reformatter declines.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableArgListOwner(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstUtil.findNodeAt(root, line, column, MultilineCallFormattingCheck::isArgListChildOrOwnerToken);
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

		final var check = new MultilineCallFormattingCheck();
		check.primeContextVars(root);
		check.primeLines(lines);

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
	 * Fixer classification entry point: returns the {@code postDelayed} {@code METHOD_CALL} (a braced-lambda
	 * first argument + delay second argument) whose layout violation sits at the given 0-based
	 * {@code line}/{@code column} (the reported {@code (} or {@code )} position), or {@code null} when the
	 * coordinates are not such a call or it is already a single line. The {@code postDelayed} branch of
	 * {@link #analyzeLayout} produces only reshape-resolvable violations, so this structural match needs no
	 * re-run of the analyzer (which would reach {@code getLines()} on the uninitialized instance). The
	 * caller re-lays-out the source (see {@code JavaPostDelayedReformatter}).
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvablePostDelayed(@Nonnull DetailAST root, int line, int column) {
		final var node = AstUtil.findNodeAt(root, line, column, MultilineCallFormattingCheck::isPostDelayedTargetToken);
		if (node == null)
			return null;
		final var call = node.getType() == TokenTypes.METHOD_CALL ? node : node.getParent();
		final var rparen = call.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null || rparen.getLineNo() == call.getLineNo())
			return null;
		return call;
	}

	/**
	 * Fixer classification entry point: returns the call/definition owner (a {@code METHOD_CALL},
	 * {@code LITERAL_NEW}, {@code SUPER_CTOR_CALL}, {@code METHOD_DEF} or {@code CTOR_DEF}) of a
	 * shared-line argument violation ({@code multiline.args.shared.line}) at the given 0-based
	 * {@code line}/{@code column} (the reported argument position), or {@code null} when the coordinates
	 * are not such a violation or the call also carries a violation a whole-list re-emission would not
	 * resolve. Runs the same {@link #analyzeLayout} the check uses. The reformatting is not a check
	 * concern: the caller re-lays-out the source (see {@code JavaArgListReformatter}).
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableSharedLineArgs(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var arg = AstUtil.findNodeAt(root, line, column, MultilineCallFormattingCheck::isArgListChild);
		if (arg == null)
			return null;
		final var owner = arg.getParent().getParent();
		if (owner == null)
			return null;

		final var check = new MultilineCallFormattingCheck();
		check.primeContextVars(root);
		check.primeLines(lines);

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
	 * Fixer classification entry point: returns the {@code QUESTION} node of a ternary argument whose
	 * internal layout is wrong ({@code multiline.ternary.question.wrong.line} or
	 * {@code multiline.ternary.colon.wrong.line}) at the given 0-based {@code line}/{@code column} (the
	 * reported {@code ?}/{@code :} position), or {@code null} when the coordinates are not such a
	 * violation or the ternary also carries a violation a whole-ternary re-emission would not resolve.
	 * Runs the same {@link #analyzeLayout} the check uses, so the two never disagree. The reformatting
	 * of the ternary is not a check concern: the caller reads the geometry off the returned node and
	 * re-lays-out the source (see {@code JavaTernaryReformatter}).
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST resolvableTernaryLayoutQuestion(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstUtil.findNodeAt(root, line, column, MultilineCallFormattingCheck::isTernaryOperator);
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

		final var check = new MultilineCallFormattingCheck();
		check.primeContextVars(root);
		check.primeLines(lines);

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

	private final Set<String> contextVarNames = new HashSet<>();
	@Nullable
	private String[] primedLines;

	private void addSharedLineViolations(@Nonnull DetailAST ast, @Nonnull List<LayoutViolation> out) {
		final var firstArg = findFirstArg(ast);
		final var lastArg = findLastArg(ast);
		if (firstArg == null || lastArg == null || firstArg.getLineNo() == lastArg.getLineNo())
			return;

		final var argList = switch (ast.getType()) {
			case TokenTypes.CTOR_DEF, TokenTypes.METHOD_DEF ->
					ast.findFirstToken(TokenTypes.PARAMETERS);
			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.SUPER_CTOR_CALL ->
					ast.findFirstToken(TokenTypes.ELIST);
			default -> null;
		};
		if (argList == null)
			return;

		final var isParams = argList.getType() == TokenTypes.PARAMETERS;
		DetailAST prev = null;
		for (var child = argList.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (isParams) {
				if (child.getType() != TokenTypes.PARAMETER_DEF)
					continue;
			}
			else if (child.getType() == TokenTypes.COMMA)
				continue;

			if (prev != null && child.getLineNo() <= AstUtil.lastLine(prev))
				out.add(new LayoutViolation(child, MSG_SHARED_LINE));
			prev = child;
		}
	}

	/**
	 * The single layout-analysis pass shared by the check (which logs the result) and the fixer
	 * (which plans a fix from it). Returns every layout violation the given call/signature would emit.
	 * Reads {@link #contextVarNames}, which the check populates incrementally during the tree walk and
	 * the fixer primes via {@link #primeContextVars} before calling this.
	 */
	@CheckReturnValue
	@Nonnull
	private List<LayoutViolation> analyzeLayout(@Nonnull DetailAST ast) {
		final var out = new ArrayList<LayoutViolation>();

		// for METHOD_CALL and SUPER_CTOR_CALL the token itself is the '(', no LPAREN child
		final int openLine;
		final DetailAST openToken;
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen != null) {
			openLine = lparen.getLineNo();
			openToken = lparen;
		}
		else {
			openLine = ast.getLineNo();
			openToken = ast;
		}

		final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return out;

		final var closeLine = rparen.getLineNo();

		// the `.put` is single-line, so this must run before the same-line early return below
		if (ast.getType() == TokenTypes.METHOD_CALL && isCollapsibleJsonObjectPut(ast)) {
			out.add(new LayoutViolation(openToken, MSG_PUT_COLLAPSIBLE));
			return out;
		}

		if (openLine == closeLine)
			return out;

		final var firstArg = findFirstArg(ast);
		if (firstArg == null)
			return out;

		if (ArgLayoutClassifier.isSingleTernaryArg(ast) || ArgLayoutClassifier.isThisAndTernaryArgs(ast)) {
			final var ternaryArg = ArgLayoutClassifier.isThisAndTernaryArgs(ast) ? findLastArg(ast) : firstArg;
			final var question = ternaryArg != null && ternaryArg.getType() == TokenTypes.EXPR
					? ternaryArg.getFirstChild() : ternaryArg;
			final var condition = question != null ? question.getFirstChild() : null;
			if (condition != null && condition.getLineNo() != openLine)
				out.add(new LayoutViolation(openToken, MSG_TERNARY_NOT_ON_OPENING));

			if (condition != null) {
				final var colon = question.findFirstToken(TokenTypes.COLON);
				if (question.getLineNo() != AstUtil.lastLine(condition)
						|| (colon != null && colon.getLineNo() != question.getLineNo())) {
					if (question.getLineNo() != AstUtil.lastLine(condition) + 1)
						out.add(new LayoutViolation(question, MSG_TERNARY_QUESTION_LINE));
					final var trueExpr = condition.getNextSibling();
					if (colon != null && trueExpr != null && colon.getLineNo() != AstUtil.lastLine(trueExpr) + 1)
						out.add(new LayoutViolation(colon, MSG_TERNARY_COLON_LINE));

					if (AstUtil.lastLine(ternaryArg) == closeLine)
						out.add(new LayoutViolation(rparen, MSG_CLOSING));
				}
				else if (AstUtil.lastLine(ternaryArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_TERNARY_NOT_ON_CLOSING));
			}
			return out;
		}

		if (ArgLayoutClassifier.isPostDelayedWithInlineBlock(ast)) {
			// canonical: one line when the single-statement body collapses within the limit (the fixer
			// unwraps the braces), otherwise the lambda opens on the ( line and `}, delay);` stacks after
			// the body. We are past the openLine == closeLine early return, so a should-be-one-line call
			// here is by definition still multi-line and always flagged
			final var lambda = ArgLayoutClassifier.directBracedLambda(firstArg);
			final var slist = lambda != null ? lambda.findFirstToken(TokenTypes.SLIST) : null;
			if (slist != null && AstUtil.singleExpressionStatementBody(slist) != null && collapsesToSingleLine(openLine, closeLine))
				out.add(new LayoutViolation(openToken, MSG_POSTDELAYED_ONE_LINE));
			else {
				if (AstUtil.firstLine(firstArg) != openLine)
					out.add(new LayoutViolation(openToken, MSG_LAMBDA_NOT_ON_OPENING));
				if (AstUtil.lastLine(firstArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
			}
			return out;
		}

		if (ArgLayoutClassifier.isInlineBlockConfiguration(ast, this::isContextSpecial)) {
			final var lastArg = findLastArg(ast);
			// the opening rule is suppressed when the fixer could not pull the first argument onto the ( line:
			// the collapsed single-line form would exceed the max width, or joining the head lines would move a
			// // comment onto the joined line and swallow the value. The split is then the only valid shape
			if (AstUtil.firstLine(firstArg) != openLine && inlineBlockOpeningPullUpFeasible(lastArg, openLine, closeLine))
				out.add(new LayoutViolation(openToken, MSG_LAMBDA_NOT_ON_OPENING));

			if (lastArg != null) {
				if ((isDirectBracelessLambda(lastArg) || containsChainedConstructor(lastArg)) && AstUtil.lastLine(lastArg) != openLine) {
					if (AstUtil.lastLine(lastArg) == closeLine)
						out.add(new LayoutViolation(rparen, MSG_CLOSING));
				}
				else if (AstUtil.lastLine(lastArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
			}
			return out;
		}

		if (isSingleMethodCallArg(ast) || isThisAndMethodCallArgs(ast)) {
			final var effectiveArg = isThisAndMethodCallArgs(ast) ? findLastArg(ast) : firstArg;
			if (effectiveArg != null && AstUtil.firstLine(effectiveArg) == openLine) {
				if (AstUtil.lastLine(effectiveArg) != closeLine)
					out.add(new LayoutViolation(rparen, MSG_LAMBDA_NOT_ON_CLOSING));
				return out;
			}
		}

		if (firstArg.getLineNo() == openLine)
			out.add(new LayoutViolation(openToken, MSG_OPENING));

		final var lastArg = findLastArg(ast);
		if (lastArg != null && AstUtil.lastLine(lastArg) == closeLine)
			out.add(new LayoutViolation(rparen, MSG_CLOSING));

		if (ast.getType() != TokenTypes.METHOD_CALL || !ArgLayoutClassifier.isStaticSpecialInlineMethodCall(ast))
			addSharedLineViolations(ast, out);

		return out;
	}

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		contextVarNames.clear();
	}

	@CheckReturnValue
	private boolean collapsesToSingleLine(int fromLine, int toLine) {
		return collapsesToSingleLine(fromLine, toLine, false);
	}

	@CheckReturnValue
	private boolean collapsesToSingleLine(int fromLine, int toLine, boolean tight) {
		final var lines = sourceLines();
		if (fromLine < 1 || toLine > lines.length || fromLine > toLine)
			return false;
		var state = JavaLineScanner.LexerState.NONE;
		final var collapsed = new StringBuilder();
		for (var line = fromLine; line <= toLine; ++line) {
			final var raw = lines[line - 1];
			final var masked = JavaLineScanner.stripCommentsAndStrings(raw, state);
			state = JavaLineScanner.stateAfter(raw, state);
			if (line == fromLine) {
				collapsed.append(masked.stripTrailing());
				continue;
			}
			final var piece = masked.strip();
			// tight joins around brackets/punctuation exactly as the fixer's collapse does, so the measured
			// width equals the fixer's one-line output at the max-width boundary; loose keeps the historical
			// space-join the postDelayed / collapsible-put gates were tuned against
			if (tight) {
				if (piece.isEmpty())
					continue;
				if (!collapsed.isEmpty() && !joinsTight(collapsed, piece))
					collapsed.append(' ');
				collapsed.append(piece);
			}
			else {
				collapsed.append(' ');
				collapsed.append(piece);
			}
		}
		return LineLength.tabExpandedLength(collapsed.toString()) <= LineLength.MAX_LINE_LENGTH;
	}

	private void collectContextVars(@Nonnull DetailAST methodOrCtor) {
		final var params = methodOrCtor.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;
			final var type = param.findFirstToken(TokenTypes.TYPE);
			if (type != null && isContextType(type)) {
				final var ident = param.findFirstToken(TokenTypes.IDENT);
				if (ident != null)
					contextVarNames.add(ident.getText());
			}
		}
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.CTOR_DEF,
				TokenTypes.LITERAL_NEW,
				TokenTypes.METHOD_CALL,
				TokenTypes.METHOD_DEF,
				TokenTypes.SUPER_CTOR_CALL,
				TokenTypes.VARIABLE_DEF
		};
	}

	@CheckReturnValue
	private boolean hasKnownContextReceiver(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null)
			return false;

		if (receiver.getType() == TokenTypes.IDENT && contextVarNames.contains(receiver.getText()))
			return true;

		if (receiver.getType() == TokenTypes.METHOD_CALL) {
			final var callName = receiver.getFirstChild();
			if (callName != null && callName.getType() == TokenTypes.IDENT
					&& CONTEXT_RETURNING_METHODS.contains(callName.getText()))
				return true;

			if (callName != null && callName.getType() == TokenTypes.DOT) {
				// getLastChild, not getNextSibling: an explicit type witness
				// (receiver.<T>getContext()) inserts a TYPE_ARGUMENTS node before the name
				final var innerMethod = callName.getLastChild();
				if (innerMethod != null && CONTEXT_RETURNING_METHODS.contains(innerMethod.getText()))
					return true;
			}
		}
		return false;
	}

	/**
	 * Whether the fixer could pull the first argument of an inline-block configuration onto the {@code (}
	 * line. A single-physical-line value ({@code lastArg} on one line) has only one clean shape, the whole
	 * call collapsed onto one line, so the pull-up is feasible only when that line fits within
	 * {@link LineLength#MAX_LINE_LENGTH}. A multi-line value keeps its tail, so the pull-up joins the
	 * {@code (} line through the value's first line: a {@code //} comment on any joined line but that last
	 * (kept) one would swallow the value, making the pull-up infeasible.
	 */
	@CheckReturnValue
	private boolean inlineBlockOpeningPullUpFeasible(@Nullable DetailAST lastArg, int openLine, int closeLine) {
		if (lastArg != null && AstUtil.firstLine(lastArg) == AstUtil.lastLine(lastArg))
			return collapsesToSingleLine(openLine, closeLine, true);

		// scan the argument lines the pull-up would join (the key line(s) between the ( line and the value's
		// first line), not the ( line itself: joining the ( line keeps its code and appends the argument, so a
		// // comment there is a separate concern the fixer skips on, not a swallow this rule suppresses. The
		// lexer state is threaded from the ( line so a // inside a text block / block comment on a key line is
		// masked, not mistaken for a real trailing comment; a line that begins inside such a literal carries no
		// code-level comment and is skipped
		final var lines = sourceLines();
		final var headEnd = lastArg != null ? AstUtil.firstLine(lastArg) : closeLine;
		var state = JavaLineScanner.LexerState.NONE;
		for (var line = openLine; line < headEnd; ++line) {
			if (line < 1 || line > lines.length)
				continue;
			final var raw = lines[line - 1];
			final var beganInLiteral = state.inTextBlock() || state.inBlockComment();
			state = JavaLineScanner.stateAfter(raw, state);
			if (line == openLine || beganInLiteral)
				continue;
			if (JavaLineScanner.firstLineComment(raw, JavaLineScanner.LexerState.NONE) >= 0)
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private boolean isCollapsibleJsonObjectPut(@Nonnull DetailAST ast) {
		if (!isCollapsibleJsonObjectPutShape(ast))
			return false;
		final var receiver = ast.getFirstChild().getFirstChild();
		if (receiver.getLineNo() == ast.getLineNo())
			return false;
		final var statement = enclosingStatement(ast);
		return collapsesToSingleLine(AstUtil.firstLine(statement), AstUtil.lastLine(statement));
	}

	@CheckReturnValue
	private boolean isContextSpecial(@Nonnull DetailAST methodCall) {
		return isGetStringCall(methodCall) || isGetQuantityStringCall(methodCall);
	}

	@CheckReturnValue
	private boolean isGetQuantityStringCall(@Nonnull DetailAST methodCall) {
		if (!ArgLayoutClassifier.isMethodCallNamed(methodCall, "getQuantityString"))
			return false;

		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL
				|| !ArgLayoutClassifier.isMethodCallNamed(receiver, "getResources"))
			return false;

		return hasKnownContextReceiver(receiver);
	}

	@CheckReturnValue
	private boolean isGetStringCall(@Nonnull DetailAST methodCall) {
		if (!ArgLayoutClassifier.isMethodCallNamed(methodCall, "getString"))
			return false;

		final var firstChild = methodCall.getFirstChild();

		// bare getString(...): can't know if the receiver is a Context
		return firstChild.getType() != TokenTypes.IDENT && hasKnownContextReceiver(methodCall);
	}

	private void primeContextVars(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			// mirror visitToken's two contextVarNames sources exactly, so the fixer's analysis
			// matches the check's: Context-typed params and Context-assigned locals
			if (node.getType() == TokenTypes.METHOD_DEF || node.getType() == TokenTypes.CTOR_DEF)
				collectContextVars(node);
			else if (node.getType() == TokenTypes.VARIABLE_DEF)
				visitVariableDef(node);
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
	}

	/**
	 * Supplies the source lines the fixer parsed its AST from, so the layout-feasibility gates
	 * ({@link #collapsesToSingleLine}, the inline-block opening pull-up check) read real text when the
	 * fixer's static resolvers run {@link #analyzeLayout} on this otherwise-uninitialized check instance.
	 * The real check run leaves this unset and falls back to {@link #getLines()}.
	 */
	private void primeLines(@Nonnull List<String> lines) {
		primedLines = lines.toArray(String[]::new);
	}

	@CheckReturnValue
	@Nonnull
	private String[] sourceLines() {
		return primedLines != null ? primedLines : getLines();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (ast.getType() == TokenTypes.VARIABLE_DEF) {
			visitVariableDef(ast);
			return;
		}

		if (ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF)
			collectContextVars(ast);

		for (var violation : analyzeLayout(ast))
			log(violation.node(), violation.messageKey());
	}

	private void visitVariableDef(@Nonnull DetailAST varDef) {
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return;

		final var expr = assign.getFirstChild();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return;

		final var methodCall = expr.getFirstChild();
		if (methodCall == null || methodCall.getType() != TokenTypes.METHOD_CALL)
			return;

		final var callFirst = methodCall.getFirstChild();
		if (callFirst == null)
			return;

		String methodName = null;
		if (callFirst.getType() == TokenTypes.IDENT)
			methodName = callFirst.getText();
		else if (callFirst.getType() == TokenTypes.DOT) {
			// getLastChild, not getNextSibling: an explicit type witness
			// (receiver.<T>getContext()) inserts a TYPE_ARGUMENTS node before the name
			final var dotMethod = callFirst.getLastChild();
			if (dotMethod != null)
				methodName = dotMethod.getText();
		}

		if (methodName != null && CONTEXT_RETURNING_METHODS.contains(methodName)) {
			final var ident = varDef.findFirstToken(TokenTypes.IDENT);
			if (ident != null)
				contextVarNames.add(ident.getText());
		}
	}
}