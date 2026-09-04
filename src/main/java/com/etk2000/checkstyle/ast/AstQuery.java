package com.etk2000.checkstyle.ast;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Structural navigation over an AST, plus the predicates that classify a node without
 * resolving any type.
 */
public final class AstQuery {
	/**
	 * Returns true if two AST subtrees are structurally identical: same
	 * token type, same text, same number of children, and recursively
	 * equal children in order. Uses a parallel iterative walk to avoid
	 * StackOverflowError on deeply nested expressions.
	 */
	@CheckReturnValue
	public static boolean astStructuralEquals(@Nonnull DetailAST a, @Nonnull DetailAST b) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(a);
		stack.push(b);
		while (!stack.isEmpty()) {
			final var nb = stack.pop();
			final var na = stack.pop();
			if (na.getType() != nb.getType())
				return false;
			if (!na.getText().equals(nb.getText()))
				return false;
			var ca = AstText.firstRealChild(na);
			var cb = AstText.firstRealChild(nb);
			while (ca != null && cb != null) {
				stack.push(ca);
				stack.push(cb);
				ca = AstText.nextRealSibling(ca);
				cb = AstText.nextRealSibling(cb);
			}
			// a differing child count shows up as one walk outliving the other. Comparing
			// getChildCount() instead counts the comment nodes a WITH_COMMENTS parse wove in, so
			// `i = i + 1` and `i = /*c*/ i + 1` come out unequal
			if (ca != null || cb != null)
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nonnull
	public static List<DetailAST> collectAnnotations(@Nonnull DetailAST modifiersOrAnnotations) {
		final var annotations = new ArrayList<DetailAST>();
		for (var child = modifiersOrAnnotations.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION)
				annotations.add(child);
		}
		return annotations;
	}

	/**
	 * Collects the canonical type strings of all instance (non-static)
	 * fields in the given OBJBLOCK, returned sorted for multiset comparison.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> collectInstanceFieldTypes(@Nonnull DetailAST objBlock) {
		final var types = new ArrayList<String>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var modifiers = child.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null)
				continue;
			final var type = child.findFirstToken(TokenTypes.TYPE);
			if (type != null)
				types.add(AstText.canonicalType(type));
		}
		types.sort(null);
		return types;
	}

	@CheckReturnValue
	@Nonnull
	public static List<DetailAST> collectMatching(@Nonnull DetailAST root, @Nonnull Predicate<DetailAST> predicate) {
		final var results = new ArrayList<DetailAST>();
		collectMatchingInto(root, predicate, results);
		return results;
	}

	/**
	 * Iterative for the same reason as the other walks here: a deeply nested
	 * generated expression must not overflow the stack. Children are pushed in
	 * reverse so results stay in pre-order, which callers index positionally.
	 */
	private static void collectMatchingInto(@Nonnull DetailAST node, @Nonnull Predicate<DetailAST> predicate, @Nonnull List<DetailAST> results) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (predicate.test(current))
				results.add(current);
			final var children = new ArrayDeque<DetailAST>();
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				children.push(child);
			for (var child : children)
				stack.push(child);
		}
	}

	@CheckReturnValue
	@Nonnull
	public static Set<String> collectParameterNames(@Nonnull DetailAST defNode) {
		final var names = new HashSet<String>();
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var child = params.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.PARAMETER_DEF)
					continue;
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null)
					names.add(ident.getText());
			}
		}
		return names;
	}

	/**
	 * Collects the canonical type strings of all parameters in the given
	 * constructor or method definition, returned sorted for multiset comparison.
	 */
	@CheckReturnValue
	@Nonnull
	public static List<String> collectParameterTypes(@Nonnull DetailAST defNode) {
		final var types = new ArrayList<String>();
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var child = params.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.PARAMETER_DEF)
					continue;
				final var type = child.findFirstToken(TokenTypes.TYPE);
				if (type != null)
					types.add(AstText.canonicalType(type));
			}
		}
		types.sort(null);
		return types;
	}

	@CheckReturnValue
	public static boolean containsCastTo(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprText) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.TYPECAST) {
				final var castType = node.findFirstToken(TokenTypes.TYPE);
				// nextRealSibling: `(String) /*c*/ obj` puts the comment where the operand belongs,
				// so a blind getNextSibling compares the comment's text against the wanted one
				final var castExpr = AstText.nextRealSibling(node.findFirstToken(TokenTypes.RPAREN));
				if (castType != null && castExpr != null
						&& typeName.equals(AstText.typeText(castType))
						&& exprText.equals(AstText.exprText(castExpr)))
					return true;
			}
			for (var child = AstText.firstRealChild(node); child != null; child = AstText.nextRealSibling(child))
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean containsStringValue(@Nonnull DetailAST ast, @Nonnull String value) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.STRING_LITERAL) {
					final var text = child.getText();
					if (text.length() >= 2 && value.equals(text.substring(1, text.length() - 1)))
						return true;
				}
				stack.push(child);
			}
		}
		return false;
	}

	@CheckReturnValue
	public static int countArguments(@Nonnull DetailAST elist) {
		// a lambda argument is a bare ELIST child rather than an EXPR, so counting EXPR alone
		// reads `f(() -> x)` as a no-argument call
		var count = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++count;
		}
		return count;
	}

	/**
	 * Whether {@code statement} ends in an {@code if} that has no {@code else} of its own. A
	 * control statement's trailing substatement is what a later {@code else} would attach to, so
	 * the chain is followed down: an {@code if} hands off to its {@code else} when it has one and
	 * is otherwise the terminus, while {@code else}, {@code while}, {@code for} and a label hand
	 * off to their body. Anything else ends the chain without a dangling {@code if}.
	 */
	@CheckReturnValue
	public static boolean endsWithDanglingIf(@Nonnull DetailAST statement) {
		for (var node = statement; ; ) {
			if (node.getType() == TokenTypes.LITERAL_IF) {
				final var elseAst = node.findFirstToken(TokenTypes.LITERAL_ELSE);
				if (elseAst == null)
					return true;
				node = elseAst;
				continue;
			}

			if (node.getType() != TokenTypes.LITERAL_ELSE
					&& node.getType() != TokenTypes.LITERAL_FOR
					&& node.getType() != TokenTypes.LITERAL_WHILE
					&& node.getType() != TokenTypes.LABELED_STAT)
				return false;

			final var body = substatementOf(node);
			if (body == null)
				return false;
			node = body;
		}
	}

	/**
	 * Finds the class-level TYPE_ARGUMENTS on a LITERAL_NEW node,
	 * handling both simple names ({@code new Foo<T>()}) and qualified
	 * names ({@code new pkg.Foo<T>()}). Constructor-level type arguments
	 * ({@code new <T>Foo()}) are skipped. Returns {@code null} if no
	 * class-level type arguments exist (including diamond {@code <>}).
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST findNewClassTypeArguments(@Nonnull DetailAST literalNew) {
		// simple name: LITERAL_NEW > IDENT > TYPE_ARGUMENTS (as siblings)
		var pastClassName = false;
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT || child.getType() == TokenTypes.DOT)
				pastClassName = true;
			else if (pastClassName && child.getType() == TokenTypes.TYPE_ARGUMENTS)
				return child;
		}

		// qualified name: TYPE_ARGUMENTS may be nested inside the DOT subtree
		final var dot = literalNew.findFirstToken(TokenTypes.DOT);
		if (dot != null)
			return dot.findFirstToken(TokenTypes.TYPE_ARGUMENTS);

		return null;
	}

	/**
	 * Ascends to the compilation-unit root, then pre-order DFS for the first node
	 * located at the given (zero-based) {@code line} and {@code column} that
	 * satisfies {@code predicate}. The root carries no siblings (pinned by
	 * {@code PreferPrefixIncrementCheckTest.testSpanFoundInSecondTopLevelClass}),
	 * so one subtree covers the whole file.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST findNodeAt(@Nonnull DetailAST root, int line, int column, @Nonnull Predicate<DetailAST> predicate) {
		var top = root;
		while (top.getParent() != null)
			top = top.getParent();
		return findNodeAtInternal(top, line, column, predicate);
	}

	/**
	 * Iterative so deeply nested generated expressions cannot overflow the stack:
	 * children are pushed in reverse so the walk still visits them in source
	 * order, matching the pre-order the callers rely on.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST findNodeAtInternal(@Nonnull DetailAST node, int line, int column, @Nonnull Predicate<DetailAST> predicate) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (current.getLineNo() == line + 1 && current.getColumnNo() == column && predicate.test(current))
				return current;
			final var children = new ArrayDeque<DetailAST>();
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				children.push(child);
			for (var child : children)
				stack.push(child);
		}
		return null;
	}

	/**
	 * Column of the earliest token in the subtree: the smallest column among the
	 * nodes sitting on {@link #firstLine(DetailAST)}. The subtree has to be
	 * walked because an imaginary node carries its operator's position rather
	 * than its first operand's ({@code EXPR} for {@code x = 5} sits at the
	 * {@code =}, not at the {@code x}).
	 *
	 * <p>Comment descendants are excluded, for the reason given on {@link #firstLine(DetailAST)}.
	 */
	@CheckReturnValue
	public static int firstColumn(@Nonnull DetailAST ast) {
		final var first = firstLine(ast);
		var column = Integer.MAX_VALUE;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() == first && node.getColumnNo() < column)
				column = node.getColumnNo();
			for (var child = AstText.firstRealChild(node); child != null; child = AstText.nextRealSibling(child))
				stack.push(child);
		}
		return column;
	}

	/**
	 * Line of the earliest token in the subtree, excluding comment descendants: a leading comment
	 * attaches <em>forward</em>, into the subtree of the declaration that follows it, so counting
	 * it drags the span start back onto the comment's own line. {@code DetailAstImpl.findLineNo}
	 * already skips comment tokens, which is why a node's reported {@code getLineNo()} disagrees
	 * with a hand-rolled min-over-descendants walk that does not.
	 */
	@CheckReturnValue
	public static int firstLine(@Nonnull DetailAST ast) {
		var first = ast.getLineNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var line = node.getLineNo();
			if (line < first)
				first = line;
			for (var child = AstText.firstRealChild(node); child != null; child = AstText.nextRealSibling(child))
				stack.push(child);
		}
		return first;
	}

	@CheckReturnValue
	@Nullable
	public static String getMethodName(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return null;
		if (firstChild.getType() == TokenTypes.IDENT)
			return firstChild.getText();
		if (firstChild.getType() == TokenTypes.DOT)
			return AstText.lastIdent(firstChild);
		return null;
	}

	@CheckReturnValue
	public static boolean hasModifier(@Nonnull DetailAST ast, int modifierType) {
		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(modifierType) != null;
	}

	@CheckReturnValue
	public static boolean hasSuppressWarnings(@Nonnull DetailAST modifiers, @Nonnull String key) {
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				continue;
			if (!"SuppressWarnings".equals(AstText.annotationName(child)))
				continue;
			if (containsStringValue(child, key))
				return true;
		}
		return false;
	}

	/**
	 * True if {@code incDec} (an {@code INC}/{@code DEC}/{@code POST_INC}/{@code POST_DEC}) mutates an element
	 * of a freshly-created array, e.g. {@code (new int[]{a})[0]++}. Such a mutation targets a throwaway array
	 * whose backing store is never referenced again, so it has no observable side effect (the primitive was
	 * copied into the array by value). An increment of a named array element ({@code arr[i]++}) or a variable
	 * IS observable and is not matched.
	 */
	@CheckReturnValue
	private static boolean incrementsFreshArrayElement(@Nonnull DetailAST incDec) {
		var target = incDec.getFirstChild();
		while (target != null) {
			switch (target.getType()) {
				case TokenTypes.INDEX_OP -> target = target.getFirstChild();
				case TokenTypes.LITERAL_NEW -> {
					return target.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null;
				}
				case TokenTypes.LPAREN, TokenTypes.RPAREN -> target = target.getNextSibling();
				default -> {
					return false;
				}
			}
		}
		return false;
	}

	@CheckReturnValue
	public static boolean isAssignmentOperator(int tokenType) {
		return switch (tokenType) {
			case TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN, TokenTypes.BSR_ASSIGN,
			     TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN, TokenTypes.MOD_ASSIGN,
			     TokenTypes.PLUS_ASSIGN, TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN, TokenTypes.STAR_ASSIGN -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	public static boolean isEmptyBody(@Nonnull DetailAST body) {
		return switch (body.getType()) {
			case TokenTypes.EMPTY_STAT -> true;
			case TokenTypes.SLIST -> body.getChildCount() == 1
					&& body.getFirstChild().getType() == TokenTypes.RCURLY;
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isNumericZero(@Nonnull String value) {
		if (value.isEmpty())
			return false;

		var s = value;

		final var lastChar = s.charAt(s.length() - 1);
		if (lastChar == 'D' || lastChar == 'F' || lastChar == 'L'
				|| lastChar == 'd' || lastChar == 'f' || lastChar == 'l')
			s = s.substring(0, s.length() - 1);

		s = s.replace("_", "");
		if (s.isEmpty())
			return false;

		if (s.startsWith("0x") || s.startsWith("0X")
				|| s.startsWith("0b") || s.startsWith("0B"))
			s = s.substring(2);

		var hasDigit = false;
		for (var i = 0; i < s.length(); ++i) {
			final var c = s.charAt(i);
			if (c == '0' || c == '.') {
				if (c == '0')
					hasDigit = true;
			}
			else if (c == 'E' || c == 'P' || c == 'e' || c == 'p') {
				var j = i + 1;
				if (j < s.length() && (s.charAt(j) == '+' || s.charAt(j) == '-'))
					++j;
				if (j >= s.length())
					return false;
				for (; j < s.length(); ++j) {
					if (s.charAt(j) != '0')
						return false;
				}
				return hasDigit;
			}
			else
				return false;
		}
		return hasDigit;
	}

	@CheckReturnValue
	public static boolean isPureDotChainOrIdent(@Nonnull DetailAST ast) {
		var cur = ast;
		while (true) {
			if (cur.getType() == TokenTypes.IDENT)
				return true;
			if (cur.getType() != TokenTypes.DOT)
				return false;
			final var left = AstText.firstRealChild(cur);
			if (left == null)
				return false;
			final var right = AstText.nextRealSibling(left);
			if (right == null || right.getType() != TokenTypes.IDENT)
				return false;
			cur = left;
		}
	}

	/**
	 * Returns true if the expression has no side effects.
	 * Pure: identifiers, field accesses, literals, array accesses,
	 * unary plus/minus.
	 * Not pure: method calls, constructors, increment/decrement, assignments.
	 * Uses an iterative stack to avoid StackOverflowError on deeply nested
	 * expressions.
	 */
	@CheckReturnValue
	public static boolean isPureExpression(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			// a comment neither is nor contains an expression, so it must not reach the
			// default arm, which would report the whole subtree as impure
			if (AstText.isCommentToken(node.getType()))
				continue;

			switch (node.getType()) {
				case TokenTypes.CHAR_LITERAL, TokenTypes.IDENT, TokenTypes.LITERAL_FALSE,
				     TokenTypes.LITERAL_NULL, TokenTypes.LITERAL_THIS, TokenTypes.LITERAL_TRUE,
				     TokenTypes.NUM_DOUBLE,
				     TokenTypes.NUM_FLOAT, TokenTypes.NUM_INT, TokenTypes.NUM_LONG,
				     TokenTypes.RBRACK, TokenTypes.STRING_LITERAL -> {
				}
				case TokenTypes.DOT, TokenTypes.EXPR, TokenTypes.INDEX_OP,
				     TokenTypes.UNARY_MINUS, TokenTypes.UNARY_PLUS -> {
					for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
						stack.push(child);
				}
				default -> {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Returns true if evaluating {@code ast} cannot mutate program state: the subtree contains no
	 * method call, constructor invocation, increment/decrement, or assignment. Array creation
	 * ({@code new T[]{...}}) is permitted, since allocating an array runs no user code; its element and
	 * dimension expressions are still checked (a call inside them is a side effect). Unlike
	 * {@link #isPureExpression} (a strict whitelist that also rejects operators), this permits all operators,
	 * comparisons, casts, and {@code instanceof}/pattern tests, blacklisting only the constructs that can have
	 * side effects.
	 */
	@CheckReturnValue
	public static boolean isSideEffectFree(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var type = node.getType();
			switch (type) {
				case TokenTypes.DEC, TokenTypes.INC, TokenTypes.POST_DEC, TokenTypes.POST_INC -> {
					if (!incrementsFreshArrayElement(node))
						return false;
				}
				case TokenTypes.LITERAL_NEW -> {
					if (node.findFirstToken(TokenTypes.ARRAY_DECLARATOR) == null)
						return false;
				}
				case TokenTypes.METHOD_CALL -> {
					return false;
				}
				default -> {
					if (isAssignmentOperator(type))
						return false;
				}
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return true;
	}

	/**
	 * Returns true if the AST node is a numeric literal whose value is zero.
	 * Handles all Java numeric literal forms: decimal, hex, binary, octal,
	 * underscores, exponent notation, and type suffixes.
	 */
	@CheckReturnValue
	public static boolean isZeroLiteral(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT,
			     TokenTypes.NUM_INT, TokenTypes.NUM_LONG -> isNumericZero(ast.getText());
			default -> false;
		};
	}

	@CheckReturnValue
	public static int lastLine(@Nonnull DetailAST ast) {
		var last = ast.getLineNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var line = node.getLineNo();
			if (line > last)
				last = line;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return last;
	}

	/**
	 * Whether an {@code else} further out would re-bind if the statement chain holding {@code node}
	 * were opened up. Java attaches an {@code else} to the closest preceding {@code if} that lacks
	 * one, so a node reached from the then-branch of an {@code if} that owns an {@code else} is one
	 * whose own dangling {@code if} would capture that {@code else}. The climb passes through the
	 * constructs whose body is a trailing substatement, since those keep the enclosing {@code else}
	 * reachable; anything else ends it. The re-bound result still compiles, which is why callers
	 * have to refuse up front rather than rely on a downstream failure.
	 *
	 * <p>{@code node} must be a control keyword's body or the keyword itself. The first step asks
	 * whether the node arrived from a then-branch, which only a legal substatement can answer; a
	 * condition or paren child of an {@code if} makes that test vacuously true and reports a hazard
	 * that is not there.
	 */
	@CheckReturnValue
	public static boolean rebindsAFollowingElse(@Nonnull DetailAST node) {
		for (var current = node; ; ) {
			final var parent = current.getParent();
			if (parent == null)
				return false;

			if (parent.getType() == TokenTypes.LITERAL_IF) {
				final var elseAst = parent.findFirstToken(TokenTypes.LITERAL_ELSE);
				if (current != elseAst && elseAst != null)
					return true;
			}
			else if (parent.getType() != TokenTypes.LITERAL_ELSE
					&& parent.getType() != TokenTypes.LITERAL_DO
					&& parent.getType() != TokenTypes.LITERAL_FOR
					&& parent.getType() != TokenTypes.LITERAL_WHILE
					&& parent.getType() != TokenTypes.LABELED_STAT)
				return false;

			current = parent;
		}
	}

	/**
	 * If {@code body} is a single-statement block (per {@link #unwrapSingleStatementBlock}) whose sole
	 * statement is an expression statement ({@code EXPR}), returns that {@code EXPR}; otherwise
	 * {@code null}. Only an expression statement is legal as a braceless lambda body, so a block holding
	 * a {@code return}/{@code if}/{@code throw}/local-variable/... statement is NOT unwrappable.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST singleExpressionStatementBody(@Nonnull DetailAST body) {
		final var single = unwrapSingleStatementBlock(body);
		return single != null && single.getType() == TokenTypes.EXPR ? single : null;
	}

	/**
	 * The statement a control keyword trails, which is what a later {@code else} would bind into.
	 * A {@code while}/{@code for} body follows the header's {@code )}, an {@code else} body is its
	 * only child, and a labelled statement's follows the label.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST substatementOf(@Nonnull DetailAST keyword) {
		return switch (keyword.getType()) {
			case TokenTypes.LABELED_STAT -> AstText.nextRealSibling(AstText.firstRealChild(keyword));
			case TokenTypes.LITERAL_ELSE -> AstText.firstRealChild(keyword);
			case TokenTypes.LITERAL_FOR, TokenTypes.LITERAL_WHILE ->
					AstText.nextRealSibling(keyword.findFirstToken(TokenTypes.RPAREN));
			default -> null;
		};
	}

	@CheckReturnValue
	public static int typeParameterCount(@Nonnull DetailAST classDef) {
		final var typeParams = classDef.findFirstToken(TokenTypes.TYPE_PARAMETERS);
		if (typeParams == null)
			return 0;

		var count = 0;
		for (var child = typeParams.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.TYPE_PARAMETER)
				++count;
		}
		return count;
	}

	/**
	 * Strips wrapping {@code LPAREN} and {@code EXPR} nodes from a value node,
	 * descending into the parenthesized/expression-wrapped inner node. Returns
	 * the first non-wrapper node, or {@code null} if the chain terminates.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST unwrapParensAndExpr(@Nullable DetailAST node) {
		var cur = node;
		while (cur != null) {
			if (cur.getType() == TokenTypes.LPAREN)
				cur = cur.getNextSibling();
			else if (cur.getType() == TokenTypes.EXPR)
				cur = cur.getFirstChild();
			else
				return cur;
		}
		return null;
	}

	/**
	 * The reverse of {@link #unwrapParensAndExpr}: walks backward from a binary operator's last child to
	 * its real right operand, stepping over a trailing {@code )} (and its inner) so a parenthesized right
	 * operand ({@code a && (b)}) resolves to {@code b}. Needed because a binary node's children are the
	 * operand tokens in source order, so the last child of {@code a && (b)} is the {@code RPAREN}, not the
	 * operand.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST unwrapParensAndExprFromEnd(@Nullable DetailAST node) {
		var cur = node;
		while (cur != null) {
			if (cur.getType() == TokenTypes.RPAREN)
				cur = cur.getPreviousSibling();
			else if (cur.getType() == TokenTypes.EXPR)
				cur = cur.getFirstChild();
			else
				return cur;
		}
		return null;
	}

	/**
	 * Unwraps a single-statement block. For a non-{@code SLIST} body, returns it
	 * unchanged; for an {@code SLIST}, returns its sole statement (ignoring
	 * {@code SEMI}/{@code RCURLY}, and any comment) or {@code null} when the block holds zero or
	 * more than one statement. A comment before the {@code RCURLY} would otherwise read as a
	 * second statement, and a comment-only block as a first one.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST unwrapSingleStatementBlock(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.SLIST)
			return body;
		DetailAST single = null;
		for (var child = AstText.firstRealChild(body); child != null; child = AstText.nextRealSibling(child)) {
			if (child.getType() == TokenTypes.SEMI || child.getType() == TokenTypes.RCURLY)
				continue;
			if (single != null)
				return null;
			single = child;
		}
		return single;
	}

	private AstQuery() {
	}
}