package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstDisplay;
import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags if-else chains whose condition is an equality
 * comparison ({@code X == Y} or {@code X != Y}) and whose branches each use
 * one of the equality operands. Inside the true branch of {@code ==} (or false
 * branch of {@code !=}), X and Y are interchangeable, so the if-else collapses
 * to a single statement.
 *
 * <p>For {@code ==}, the surviving value is the else-branch value; for
 * {@code !=}, the surviving value is the then-branch value.
 *
 * <p>Examples that fire:
 * <ul>
 *     <li>{@code if (a == b) r = a; else r = b;} -> {@code r = b;}</li>
 *     <li>{@code if (a == b) return a; else return b;} -> {@code return b;}</li>
 *     <li>{@code if (a == b) return a; return b;} -> {@code return b;}</li>
 *     <li>{@code if (a != b) r = a; else r = b;} -> {@code r = a;}</li>
 *     <li>{@code if (a != b) return a; return b;} -> {@code return a;}</li>
 * </ul>
 * Operands and branch values must all be pure (no side effects).
 */
public class RedundantEqualityBranchCheck extends AbstractAstCheck {
	private record BranchInfo(@Nonnull BranchKind kind, @Nullable DetailAST target, @Nonnull DetailAST value) {}

	public enum BranchKind {
		ASSIGN,
		RETURN
	}

	/**
	 * The classification of a redundant equality if-else. Lines are 0-based and inclusive.
	 * {@code target} is the assignment target text for {@link BranchKind#ASSIGN} and
	 * {@code null} for a return. When an uninitialized {@code T target;} declaration sits immediately
	 * above the assign form and a bare {@code return target;} immediately below, and the declaration
	 * can go with them ({@link #isDeletable}), {@code declLine} and {@code collapseReturnLine} carry
	 * their 0-based lines (both {@code -1} otherwise). {@code firstColumn} is the code-point column the
	 * collapsed statements start at on the first line of the collapse, and {@code endColumn} the one
	 * just past their last token on its last line. {@code leadLine} is the first line of the statement
	 * holding the code ahead of the collapse on its first line, whose indent that code is laid out at.
	 */
	public record Redundancy(
			@Nonnull BranchKind kind,
			@Nullable String target,
			@Nonnull String hint,
			int startLine,
			int endLine,
			int declLine,
			int collapseReturnLine,
			int firstColumn,
			int endColumn,
			int leadLine
	) {}

	// the nodes whose children are statements or members, each laid out at its own first line's indent
	private static final Set<Integer> STATEMENT_LISTS = Set.of(TokenTypes.OBJBLOCK, TokenTypes.SLIST);
	// the bodies between a statement and its method that give a return no declared type
	private static final Set<Integer> UNTYPED_BODIES = Set.of(
			TokenTypes.COMPACT_CTOR_DEF, TokenTypes.CTOR_DEF, TokenTypes.INSTANCE_INIT, TokenTypes.LAMBDA,
			TokenTypes.OBJBLOCK, TokenTypes.STATIC_INIT
	);
	private static final String MSG_KEY = "redundant.equality.branch";

	/**
	 * Classifies {@code ifAst} as a redundant equality if-else, returning the collapse or {@code null}
	 * when it does not fire.
	 */
	@CheckReturnValue
	@Nullable
	public static Redundancy classify(@Nonnull DetailAST ifAst) {
		if (ifAst.getType() != TokenTypes.LITERAL_IF)
			return null;
		final var lparen = ifAst.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return null;
		final var condExpr = lparen.getNextSibling();
		if (condExpr == null || condExpr.getType() != TokenTypes.EXPR)
			return null;
		final var condition = condExpr.getFirstChild();
		if (condition == null
				|| (condition.getType() != TokenTypes.EQUAL && condition.getType() != TokenTypes.NOT_EQUAL))
			return null;

		final var leftOp = condition.getFirstChild();
		final var rightOp = leftOp != null ? leftOp.getNextSibling() : null;
		if (leftOp == null || rightOp == null)
			return null;
		if (!AstQuery.isPureExpression(leftOp) || !AstQuery.isPureExpression(rightOp))
			return null;

		final var leftText = AstDisplay.displayText(leftOp);
		final var rightText = AstDisplay.displayText(rightOp);

		final var rparen = ifAst.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var thenBody = rparen.getNextSibling();
		if (thenBody == null)
			return null;

		final var thenBranch = extractBranch(thenBody);
		if (thenBranch == null)
			return null;

		final BranchInfo elseBranch;
		final DetailAST last;
		final var elseAst = ifAst.findFirstToken(TokenTypes.LITERAL_ELSE);
		if (elseAst != null) {
			final var elseBody = elseAst.getFirstChild();
			if (elseBody == null)
				return null;
			elseBranch = extractBranch(elseBody);
			if (elseBranch == null)
				return null;
			last = ifAst;
		}
		else {
			if (thenBranch.kind() != BranchKind.RETURN)
				return null;
			final var nextStmt = ifAst.getNextSibling();
			if (nextStmt == null || nextStmt.getType() != TokenTypes.LITERAL_RETURN)
				return null;
			final var nextExpr = nextStmt.findFirstToken(TokenTypes.EXPR);
			if (nextExpr == null)
				return null;
			final var nextValue = nextExpr.getFirstChild();
			if (nextValue == null)
				return null;
			elseBranch = new BranchInfo(BranchKind.RETURN, null, nextValue);
			last = nextStmt;
		}

		if (thenBranch.kind() != elseBranch.kind())
			return null;
		if (thenBranch.kind() == BranchKind.ASSIGN
				&& !AstDisplay.displayText(thenBranch.target()).equals(AstDisplay.displayText(elseBranch.target())))
			return null;

		if (!AstQuery.isPureExpression(thenBranch.value()) || !AstQuery.isPureExpression(elseBranch.value()))
			return null;

		final var thenValueText = AstDisplay.displayText(thenBranch.value());
		final var elseValueText = AstDisplay.displayText(elseBranch.value());
		if (!leftText.equals(thenValueText) && !rightText.equals(thenValueText))
			return null;
		if (!leftText.equals(elseValueText) && !rightText.equals(elseValueText))
			return null;

		final var hint = condition.getType() == TokenTypes.EQUAL ? elseValueText : thenValueText;
		final var kind = thenBranch.kind();
		final var target = kind == BranchKind.ASSIGN ? AstDisplay.displayText(thenBranch.target()) : null;
		final var startLine = ifAst.getLineNo() - 1;
		final var endLine = AstQuery.lastLine(last) - 1;

		if (kind == BranchKind.ASSIGN && elseAst != null) {
			final var decl = previousStatement(ifAst);
			final var next = ifAst.getNextSibling();
			if (decl != null && decl.getType() == TokenTypes.VARIABLE_DEF
					&& next != null && next.getType() == TokenTypes.LITERAL_RETURN
					&& isUninitializedDeclOf(decl, target)
					&& returnsBareName(next, target)
					&& isDeletable(decl)) {
				return new Redundancy(
						kind,
						target,
						hint,
						startLine,
						endLine,
						AstQuery.firstLine(decl) - 1,
						AstQuery.lastLine(next) - 1,
						AstQuery.firstColumn(decl),
						endColumn(next),
						leadLine(decl)
				);
			}
		}

		return new Redundancy(kind, target, hint, startLine, endLine, -1, -1, ifAst.getColumnNo(), endColumn(last), leadLine(ifAst));
	}

	/**
	 * Locates the {@code LITERAL_IF} the check reported at {@code (line, column)} (0-based) in
	 * {@code root} and classifies it, or {@code null} when no such if-node exists.
	 */
	@CheckReturnValue
	@Nullable
	public static Redundancy classifyAt(@Nonnull DetailAST root, int line, int column) {
		final var ifAst = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.LITERAL_IF);
		return ifAst == null ? null : classify(ifAst);
	}

	/**
	 * The method whose body holds {@code node}, or {@code null} when a lambda, constructor,
	 * initializer or type body comes first, whose {@code return} has no declared type to compare.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST enclosingMethod(@Nonnull DetailAST node) {
		for (var ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			if (ancestor.getType() == TokenTypes.METHOD_DEF)
				return ancestor;
			if (UNTYPED_BODIES.contains(ancestor.getType()))
				return null;
		}
		return null;
	}

	/** The code-point column just past the last token of {@code ast}. */
	@CheckReturnValue
	private static int endColumn(@Nonnull DetailAST ast) {
		var last = ast;
		for (var child = ast.getLastChild(); child != null; child = child.getLastChild())
			last = child;
		return last.getColumnNo() + last.getText().codePointCount(0, last.getText().length());
	}

	@CheckReturnValue
	@Nullable
	private static BranchInfo extractBranch(@Nonnull DetailAST body) {
		final var stmt = AstQuery.unwrapSingleStatementBlock(body);
		if (stmt == null)
			return null;

		if (stmt.getType() == TokenTypes.EXPR) {
			final var inner = stmt.getFirstChild();
			if (inner == null || inner.getType() != TokenTypes.ASSIGN)
				return null;
			final var lhs = inner.getFirstChild();
			final var rhs = lhs != null ? lhs.getNextSibling() : null;
			if (lhs == null || rhs == null)
				return null;
			return new BranchInfo(BranchKind.ASSIGN, lhs, rhs);
		}

		if (stmt.getType() == TokenTypes.LITERAL_RETURN) {
			final var expr = stmt.findFirstToken(TokenTypes.EXPR);
			if (expr == null)
				return null;
			final var value = expr.getFirstChild();
			if (value == null)
				return null;
			return new BranchInfo(BranchKind.RETURN, null, value);
		}

		return null;
	}

	/**
	 * Whether {@code decl}, the uninitialized declaration above an assign-form if-else that a
	 * {@code return} of its name follows, can be deleted with them so that {@code return value;}
	 * replaces all three. Only then does nothing else depend on it: it declares that one name (a
	 * declarator beside it, {@code int s = compute(), r;}, would go too); it sits in a block of its
	 * own, not straight in a {@code case} group, whose later cases share its scope
	 * ({@code case 2: r = 5;}); it carries no annotation, which would be dropped; and its type is the
	 * enclosing method's return type, so returning the value converts it exactly as assigning it did
	 * ({@code double r = intValue; return r;} in a method returning {@code Object} returns a
	 * {@code Double}, which {@code return intValue;} would not).
	 */
	@CheckReturnValue
	private static boolean isDeletable(@Nonnull DetailAST decl) {
		// the declaration ahead of the if is the last declarator of its statement, so only a COMMA can precede it
		final var previous = decl.getPreviousSibling();
		if (previous != null && previous.getType() == TokenTypes.COMMA)
			return false;
		final var block = decl.getParent();
		if (block == null || block.getParent() == null || block.getParent().getType() == TokenTypes.CASE_GROUP)
			return false;
		final var modifiers = decl.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null && modifiers.findFirstToken(TokenTypes.ANNOTATION) != null)
			return false;
		final var method = enclosingMethod(decl);
		final var declaredType = decl.findFirstToken(TokenTypes.TYPE);
		return method != null && declaredType != null
				&& AstDisplay.displayText(declaredType).equals(AstDisplay.displayText(method.findFirstToken(TokenTypes.TYPE)));
	}

	@CheckReturnValue
	private static boolean isUninitializedDeclOf(@Nonnull DetailAST varDef, @Nonnull String name) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		return ident != null && ident.getText().equals(name) && varDef.findFirstToken(TokenTypes.ASSIGN) == null;
	}

	/**
	 * The 0-based first line of the statement or member holding the token right before
	 * {@code node}: the code ahead of {@code node} on its line ends that statement, so that line's
	 * indent is the one the code is laid out at, even when the line itself starts elsewhere (a text
	 * block's closing line, a wrapped argument list).
	 */
	@CheckReturnValue
	private static int leadLine(@Nonnull DetailAST node) {
		var statement = tokenBefore(node);
		if (statement == null)
			return AstQuery.firstLine(node) - 1;
		while (statement.getParent() != null && !STATEMENT_LISTS.contains(statement.getParent().getType()))
			statement = statement.getParent();
		// an expression statement or a declaration hangs its `;` beside it rather than under it
		if (statement.getType() == TokenTypes.SEMI && statement.getPreviousSibling() != null)
			statement = statement.getPreviousSibling();
		return AstQuery.firstLine(statement) - 1;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST previousStatement(@Nonnull DetailAST node) {
		var prev = node.getPreviousSibling();
		while (prev != null && prev.getType() == TokenTypes.SEMI)
			prev = prev.getPreviousSibling();
		return prev;
	}

	@CheckReturnValue
	private static boolean returnsBareName(@Nonnull DetailAST returnStmt, @Nonnull String name) {
		final var expr = returnStmt.findFirstToken(TokenTypes.EXPR);
		if (expr == null)
			return false;
		final var value = expr.getFirstChild();
		return value != null && value.getType() == TokenTypes.IDENT
				&& value.getNextSibling() == null && value.getText().equals(name);
	}

	/** The last token ahead of {@code node} in its parent, or the parent itself when none is. */
	@CheckReturnValue
	@Nullable
	private static DetailAST tokenBefore(@Nonnull DetailAST node) {
		var before = node.getPreviousSibling();
		if (before == null)
			return node.getParent();
		while (before.getLastChild() != null)
			before = before.getLastChild();
		return before;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_IF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var redundancy = classify(ast);
		if (redundancy != null)
			log(ast, MSG_KEY, redundancy.hint());
	}
}