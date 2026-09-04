package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.format.ArgLayoutClassifier;
import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The rule that a {@code new JSONObject().put(k, v)} expression, a fresh {@code new JSONObject()}
 * with exactly one {@code .put} and a simple value, must be written on one line when its collapsed
 * form fits within {@link LineLength#MAX_LINE_LENGTH}: splitting the {@code new JSONObject()} from
 * its {@code .put} across lines is a violation. Multi-{@code put} builders and non-simple values
 * (a nested {@code new JSONObject()}, a chained call, a ternary, a lambda) stay multiline.
 * <p>
 * A simple value is a literal, a variable, or a single non-chained method call.
 */
public final class JsonObjectPutCollapse {
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
	 * Walks up from an expression node to the statement (or member declaration) that contains it, the
	 * whole {@code ...;} statement the collapse fixer joins onto one line. A braceless control-flow
	 * body counts as that statement, so collapsing the body of a braceless {@code if} leaves the
	 * {@code if} on its own line rather than dragging it (and any {@code else}) onto the joined one;
	 * while such a body is still multi-line it owes braces, which is {@code ControlFlowBracesCheck}'s
	 * rule to enforce, and once collapsed to one line it no longer does.
	 *
	 * @return {@code null} when the chain reaches a {@code for} header instead of a statement, where
	 *         joining would splice the header rather than collapse a value
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST enclosingStatement(@Nonnull DetailAST node) {
		var current = node;
		while (current.getParent() != null) {
			switch (current.getParent().getType()) {
				case TokenTypes.ARRAY_INIT, TokenTypes.LABELED_STAT, TokenTypes.LITERAL_DO,
						TokenTypes.LITERAL_ELSE, TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_IF,
						TokenTypes.LITERAL_WHILE, TokenTypes.OBJBLOCK, TokenTypes.SLIST,
						TokenTypes.SWITCH_RULE -> {
					return current;
				}
				case TokenTypes.FOR_CONDITION, TokenTypes.FOR_EACH_CLAUSE, TokenTypes.FOR_INIT,
						TokenTypes.FOR_ITERATOR -> {
					return null;
				}
				default -> current = current.getParent();
			}
		}
		return current;
	}

	@CheckReturnValue
	private static boolean isNewJsonObject(@Nonnull DetailAST node) {
		if (node.getType() != TokenTypes.LITERAL_NEW)
			return false;
		final var ident = node.findFirstToken(TokenTypes.IDENT);
		return ident != null && "JSONObject".equals(ident.getText());
	}

	/**
	 * A {@code put} call on a fresh {@code new JSONObject()} (not chained further), with exactly two
	 * arguments and a simple value. Does not check the split-across-lines or line-length conditions.
	 */
	@CheckReturnValue
	private static boolean isShape(@Nonnull DetailAST ast) {
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

	/**
	 * Whether the {@code new JSONObject().put(k, v)} at {@code ast} must be collapsed onto one line:
	 * its receiver is split off above the {@code .put}, and the whole statement fits once joined.
	 */
	@CheckReturnValue
	static boolean isViolation(@Nonnull DetailAST ast, @Nonnull String[] lines) {
		final var statement = splitStatement(ast);
		return statement != null
				&& LineCollapse.fitsOnOneLine(lines, statementFirstLine(statement), AstQuery.lastLine(statement));
	}

	/**
	 * Fixer entry point: the {@code {fromLine, toLine}} (0-based, inclusive) of the statement the
	 * fixer must join onto one line for the collapse violation at the given 0-based
	 * {@code line}/{@code column}, or {@code null} when the coordinates are not a split
	 * {@code new JSONObject().put(...)}, or when the joined form would not fit.
	 * <p>
	 * The width gate is re-applied here rather than trusted from the check, because the fixer is
	 * registered per check class and so runs for every message this check emits: without it, a call
	 * carrying some other violation reaches this entry point and gets collapsed past the limit the
	 * check refused it on. Nothing downstream re-measures, {@code joinSpan} included.
	 */
	@CheckReturnValue
	@Nullable
	public static int[] lineSpan(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var putCall = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.METHOD_CALL);
		final var statement = putCall == null ? null : splitStatement(putCall);
		if (statement == null)
			return null;
		final var fromLine = statementFirstLine(statement);
		final var toLine = AstQuery.lastLine(statement);
		return LineCollapse.fitsOnOneLine(lines.toArray(String[]::new), fromLine, toLine)
				? new int[]{fromLine - 1, toLine - 1}
				: null;
	}

	/**
	 * The enclosing statement of a {@code new JSONObject().put(...)} whose {@code new JSONObject()}
	 * sits on an earlier line than its {@code .put}, or {@code null} when {@code ast} is not such a
	 * split call.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST splitStatement(@Nonnull DetailAST ast) {
		if (!isShape(ast))
			return null;
		final var receiver = ast.getFirstChild().getFirstChild();
		if (receiver.getLineNo() == ast.getLineNo())
			return null;

		// backstop for a shape the climb does not name: joining a span that covers a brace-delimited
		// body would flatten that body onto one line instead of collapsing a value
		final var statement = enclosingStatement(ast);
		return statement == null || ArgLayoutClassifier.containsMultilineBracedBlock(statement) ? null : statement;
	}

	/**
	 * The first line of {@code statement}'s own code, skipping an {@code ANNOTATIONS} or
	 * {@code MODIFIERS} subtree: for an annotated declaration {@link AstQuery#firstLine} returns the
	 * annotation's line, and joining from there would pull the annotation inline and count its width
	 * against the collapse gate.
	 */
	@CheckReturnValue
	private static int statementFirstLine(@Nonnull DetailAST statement) {
		var first = Integer.MAX_VALUE;
		for (var child = statement.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATIONS && child.getType() != TokenTypes.MODIFIERS)
				first = Math.min(first, AstQuery.firstLine(child));
		}
		return first == Integer.MAX_VALUE ? AstQuery.firstLine(statement) : first;
	}

	private JsonObjectPutCollapse() {
	}
}