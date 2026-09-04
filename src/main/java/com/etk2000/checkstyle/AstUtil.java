package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Predicate;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class AstUtil {
	@CheckReturnValue
	@Nonnull
	static String annotationName(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		final var dot = annotation.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			var last = dot.getFirstChild();
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getText();
		}
		return "";
	}

	/**
	 * Returns true if two AST subtrees are structurally identical: same
	 * token type, same text, same number of children, and recursively
	 * equal children in order. Uses a parallel iterative walk to avoid
	 * StackOverflowError on deeply nested expressions.
	 */
	@CheckReturnValue
	static boolean astStructuralEquals(@Nonnull DetailAST a, @Nonnull DetailAST b) {
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
			if (na.getChildCount() != nb.getChildCount())
				return false;
			var ca = na.getFirstChild();
			var cb = nb.getFirstChild();
			while (ca != null && cb != null) {
				stack.push(ca);
				stack.push(cb);
				ca = ca.getNextSibling();
				cb = cb.getNextSibling();
			}
		}
		return true;
	}

	/**
	 * Returns a canonical string for an ANNOTATION AST node, including its
	 * name and normalized parameters. Parameter names are sorted alphabetically
	 * so that {@code @A(b=1, a=2)} and {@code @A(a=2, b=1)} produce the same
	 * string. Positional values are stored under key "value".
	 *
	 * <p>Examples:
	 * <ul>
	 *   <li>{@code @Deprecated} and {@code @Deprecated()} both produce {@code "Deprecated"}</li>
	 *   <li>{@code @A(123)} and {@code @A(value=123)} both produce {@code "A(value=123)"}</li>
	 * </ul>
	 */
	@CheckReturnValue
	@Nonnull
	static String canonicalAnnotation(@Nonnull DetailAST annotation, int maxDepth) {
		if (maxDepth <= 0)
			return "";
		final var sb = new StringBuilder(annotationName(annotation));
		final var params = new TreeMap<String, String>();
		for (var child = annotation.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
				final var key = child.findFirstToken(TokenTypes.IDENT).getText();
				var value = child.findFirstToken(TokenTypes.EXPR);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION);
				if (value != null) {
					params.put(
							key,
							value.getType() == TokenTypes.ANNOTATION
									? canonicalAnnotation(value, maxDepth - 1)
									: exprText(value)
					);
				}
			}
			else if (child.getType() == TokenTypes.ANNOTATION)
				params.put("value", canonicalAnnotation(child, maxDepth - 1));
			else if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT)
				params.put("value", exprText(child));
		}
		if (!params.isEmpty()) {
			sb.append('(');
			var first = true;
			for (var entry : params.entrySet()) {
				if (!first)
					sb.append(',');
				sb.append(entry.getKey()).append('=').append(entry.getValue());
				first = false;
			}
			sb.append(')');
		}
		return sb.toString();
	}

	@CheckReturnValue
	@Nonnull
	static String canonicalType(@Nonnull DetailAST typeNode) {
		final var sb = new StringBuilder();
		for (var child = typeNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.ARRAY_DECLARATOR -> sb.append("[]");
				case TokenTypes.DOT -> sb.append(dottedName(child));
				case TokenTypes.IDENT -> sb.append(child.getText());
				case TokenTypes.LITERAL_BOOLEAN -> sb.append("boolean");
				case TokenTypes.LITERAL_BYTE -> sb.append("byte");
				case TokenTypes.LITERAL_CHAR -> sb.append("char");
				case TokenTypes.LITERAL_DOUBLE -> sb.append("double");
				case TokenTypes.LITERAL_FLOAT -> sb.append("float");
				case TokenTypes.LITERAL_INT -> sb.append("int");
				case TokenTypes.LITERAL_LONG -> sb.append("long");
				case TokenTypes.LITERAL_SHORT -> sb.append("short");
				case TokenTypes.LITERAL_VOID -> sb.append("void");
			}
		}
		return sb.toString();
	}

	@CheckReturnValue
	@Nonnull
	static List<DetailAST> collectAnnotations(@Nonnull DetailAST modifiersOrAnnotations) {
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
	static List<String> collectInstanceFieldTypes(@Nonnull DetailAST objBlock) {
		final var types = new ArrayList<String>();
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var modifiers = child.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null)
				continue;
			final var type = child.findFirstToken(TokenTypes.TYPE);
			if (type != null)
				types.add(canonicalType(type));
		}
		types.sort(null);
		return types;
	}

	@CheckReturnValue
	@Nonnull
	static List<DetailAST> collectMatching(@Nonnull DetailAST root, @Nonnull Predicate<DetailAST> predicate) {
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
	static Set<String> collectParameterNames(@Nonnull DetailAST defNode) {
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
	static List<String> collectParameterTypes(@Nonnull DetailAST defNode) {
		final var types = new ArrayList<String>();
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var child = params.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.PARAMETER_DEF)
					continue;
				final var type = child.findFirstToken(TokenTypes.TYPE);
				if (type != null)
					types.add(canonicalType(type));
			}
		}
		types.sort(null);
		return types;
	}

	@CheckReturnValue
	static boolean containsCastTo(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprText) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.TYPECAST) {
				final var castType = node.findFirstToken(TokenTypes.TYPE);
				final var rparen = node.findFirstToken(TokenTypes.RPAREN);
				final var castExpr = rparen != null ? rparen.getNextSibling() : null;
				if (castType != null && castExpr != null
						&& typeName.equals(typeText(castType))
						&& exprText.equals(exprText(castExpr)))
					return true;
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
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
	static int countArguments(@Nonnull DetailAST elist) {
		// a lambda argument is a bare ELIST child rather than an EXPR, so counting EXPR alone
		// reads `f(() -> x)` as a no-argument call
		var count = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++count;
		}
		return count;
	}

	@CheckReturnValue
	private static int countParameters(@Nonnull DetailAST parameters) {
		var count = 0;
		for (var child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.PARAMETER_DEF)
				++count;
		}
		return count;
	}

	/** The body of a type {@code name} names by being declared in an enclosing scope of {@code scope}. */
	@CheckReturnValue
	@Nullable
	private static DetailAST declaredTypeBody(@Nonnull DetailAST scope, @Nonnull String name) {
		for (var frame = scope; frame != null; frame = frame.getParent()) {
			for (var child = frame.getFirstChild(); child != null; child = child.getNextSibling()) {
				final var type = child.getType();
				final var isTypeDef = type == TokenTypes.CLASS_DEF || type == TokenTypes.ENUM_DEF
						|| type == TokenTypes.INTERFACE_DEF || type == TokenTypes.RECORD_DEF
						|| type == TokenTypes.ANNOTATION_DEF;
				final var ident = isTypeDef ? child.findFirstToken(TokenTypes.IDENT) : null;
				if (ident != null && name.equals(ident.getText()))
					return child.findFirstToken(TokenTypes.OBJBLOCK);
			}
		}

		final var def = sameFileClassDef(scope, name);
		return def == null ? null : def.findFirstToken(TokenTypes.OBJBLOCK);
	}

	/**
	 * Builds human-readable text for an expression AST.
	 * Unlike {@link #exprText} which is designed for equality comparison,
	 * this includes operators, dots, and brackets for display in messages.
	 * Uses an iterative stack to avoid StackOverflowError on deeply nested
	 * expressions.
	 */
	@CheckReturnValue
	@Nonnull
	static String displayText(@Nonnull DetailAST ast) {
		final var sb = new StringBuilder();
		final var stack = new ArrayDeque<>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var task = stack.pop();
			if (task instanceof String text) {
				sb.append(text);
				continue;
			}
			final var node = (DetailAST) task;
			final var first = node.getFirstChild();
			final var second = first != null ? first.getNextSibling() : null;
			switch (node.getType()) {
				case TokenTypes.BAND -> {
					stack.push(second);
					stack.push(" & ");
					stack.push(first);
				}
				case TokenTypes.BNOT -> {
					stack.push(first);
					stack.push("~");
				}
				case TokenTypes.BOR -> {
					stack.push(second);
					stack.push(" | ");
					stack.push(first);
				}
				case TokenTypes.BSR -> {
					stack.push(second);
					stack.push(" >>> ");
					stack.push(first);
				}
				case TokenTypes.BXOR -> {
					stack.push(second);
					stack.push(" ^ ");
					stack.push(first);
				}
				case TokenTypes.DEC -> {
					stack.push(first);
					stack.push("--");
				}
				case TokenTypes.DIV -> {
					stack.push(second);
					stack.push(" / ");
					stack.push(first);
				}
				case TokenTypes.DOT -> {
					stack.push(second);
					stack.push(".");
					stack.push(first);
				}
				case TokenTypes.EQUAL -> {
					stack.push(second);
					stack.push(" == ");
					stack.push(first);
				}
				case TokenTypes.EXPR -> {
					if (node.getChildCount() == 1)
						stack.push(first);
					else
						sb.append(exprText(node));
				}
				case TokenTypes.GE -> {
					stack.push(second);
					stack.push(" >= ");
					stack.push(first);
				}
				case TokenTypes.GT -> {
					stack.push(second);
					stack.push(" > ");
					stack.push(first);
				}
				case TokenTypes.INC -> {
					stack.push(first);
					stack.push("++");
				}
				case TokenTypes.INDEX_OP -> {
					stack.push("]");
					stack.push(second);
					stack.push("[");
					stack.push(first);
				}
				case TokenTypes.LAND -> {
					stack.push(second);
					stack.push(" && ");
					stack.push(first);
				}
				case TokenTypes.LE -> {
					stack.push(second);
					stack.push(" <= ");
					stack.push(first);
				}
				case TokenTypes.LNOT -> {
					stack.push(first);
					stack.push("!");
				}
				case TokenTypes.LOR -> {
					stack.push(second);
					stack.push(" || ");
					stack.push(first);
				}
				case TokenTypes.LT -> {
					stack.push(second);
					stack.push(" < ");
					stack.push(first);
				}
				case TokenTypes.METHOD_CALL -> {
					final var elist = node.findFirstToken(TokenTypes.ELIST);
					final var callArgs = new ArrayList<DetailAST>();
					for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
						if (child.getType() == TokenTypes.EXPR)
							callArgs.add(child);
					}
					stack.push(")");
					for (var k = callArgs.size() - 1; k >= 0; --k) {
						stack.push(callArgs.get(k));
						if (k > 0)
							stack.push(", ");
					}
					stack.push("(");
					stack.push(first);
				}
				case TokenTypes.MINUS -> {
					stack.push(second);
					stack.push(" - ");
					stack.push(first);
				}
				case TokenTypes.MOD -> {
					stack.push(second);
					stack.push(" % ");
					stack.push(first);
				}
				case TokenTypes.NOT_EQUAL -> {
					stack.push(second);
					stack.push(" != ");
					stack.push(first);
				}
				case TokenTypes.PLUS -> {
					stack.push(second);
					stack.push(" + ");
					stack.push(first);
				}
				case TokenTypes.POST_DEC -> {
					stack.push("--");
					stack.push(first);
				}
				case TokenTypes.POST_INC -> {
					stack.push("++");
					stack.push(first);
				}
				case TokenTypes.QUESTION -> {
					final var colon = second.getNextSibling();
					stack.push(colon.getNextSibling());
					stack.push(" : ");
					stack.push(second);
					stack.push(" ? ");
					stack.push(first);
				}
				case TokenTypes.SL -> {
					stack.push(second);
					stack.push(" << ");
					stack.push(first);
				}
				case TokenTypes.SR -> {
					stack.push(second);
					stack.push(" >> ");
					stack.push(first);
				}
				case TokenTypes.STAR -> {
					stack.push(second);
					stack.push(" * ");
					stack.push(first);
				}
				case TokenTypes.TYPECAST -> {
					final var operandParts = new ArrayList<DetailAST>();
					for (var part = node.findFirstToken(TokenTypes.RPAREN).getNextSibling(); part != null; part = part.getNextSibling())
						operandParts.add(part);
					for (var k = operandParts.size() - 1; k >= 0; --k)
						stack.push(operandParts.get(k));
					stack.push(") ");
					stack.push(exprText(first));
					stack.push("(");
				}
				case TokenTypes.UNARY_MINUS -> {
					stack.push(first);
					stack.push("-");
				}
				case TokenTypes.UNARY_PLUS -> {
					stack.push(first);
					stack.push("+");
				}
				// A compound node type the switch does not format (e.g. LITERAL_NEW, a method
				// reference, a lambda) falls back to leaf-text concatenation so its operands
				// are rendered instead of dropped; a leaf node keeps its own text.
				default -> sb.append(node.getChildCount() == 0 ? node.getText() : exprText(node));
			}
		}
		return sb.toString();
	}

	/**
	 * Returns the dotted name from a DOT AST node (e.g. "java.util.List"),
	 * without consuming sibling nodes like ARRAY_DECLARATOR or TYPE_ARGUMENTS.
	 */
	@CheckReturnValue
	@Nonnull
	static String dottedName(@Nonnull DetailAST dot) {
		final var segments = new ArrayList<String>();
		var current = dot;
		while (current.getType() == TokenTypes.DOT) {
			final var first = current.getFirstChild();
			if (first == null)
				break;

			// a generic segment carries its TYPE_ARGUMENTS as a sibling of its IDENT, so taking
			// the sibling blind reads `Outer<String>.Inner` back as `Outer.TYPE_ARGUMENTS`
			for (var sibling = first.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
				if (sibling.getType() == TokenTypes.IDENT)
					segments.add(sibling.getText());
			}
			current = first;
		}
		segments.add(current.getText());
		final var sb = new StringBuilder(segments.getLast());
		for (var i = segments.size() - 2; i >= 0; --i)
			sb.append('.').append(segments.get(i));
		return sb.toString();
	}

	/**
	 * Returns the concatenated leaf text of an AST subtree. Uses an iterative
	 * stack to avoid StackOverflowError on deeply nested expressions.
	 */
	@CheckReturnValue
	@Nonnull
	static String exprText(@Nonnull DetailAST ast) {
		if (ast.getChildCount() == 0)
			return ast.getText();

		final var sb = new StringBuilder();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getChildCount() == 0) {
				sb.append(node.getText());
				continue;
			}
			final var children = new ArrayList<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				children.add(child);
			for (var i = children.size() - 1; i >= 0; --i)
				stack.push(children.get(i));
		}
		return sb.toString();
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findEnclosingClassDef(@Nonnull DetailAST node) {
		for (var p = node.getParent(); p != null; p = p.getParent()) {
			final var t = p.getType();
			if (t == TokenTypes.CLASS_DEF || t == TokenTypes.INTERFACE_DEF
					|| t == TokenTypes.ENUM_DEF || t == TokenTypes.RECORD_DEF)
				return p;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findInnerClassDef(@Nonnull DetailAST classDef, @Nonnull String className) {
		final var objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var t = child.getType();
			if (t != TokenTypes.CLASS_DEF && t != TokenTypes.INTERFACE_DEF
					&& t != TokenTypes.ENUM_DEF && t != TokenTypes.RECORD_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null && className.equals(ident.getText()))
				return child;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String findMethodReturnTypeInClass(@Nonnull DetailAST classDef, @Nonnull String methodName, int arity) {
		final var objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		String matched = null;
		var matchCount = 0;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident == null || !methodName.equals(ident.getText()))
				continue;
			final var params = child.findFirstToken(TokenTypes.PARAMETERS);
			final var paramCount = params == null ? 0 : countParameters(params);
			if (paramCount != arity)
				continue;
			final var typeNode = child.findFirstToken(TokenTypes.TYPE);
			if (typeNode == null)
				continue;
			final var typeName = getTypeName(typeNode);
			if (typeName == null)
				continue;
			if (matched != null && !matched.equals(typeName))
				return null;
			matched = typeName;
			++matchCount;
		}
		return matchCount > 0 ? matched : null;
	}

	/**
	 * Extracts the class name from a LITERAL_NEW node, handling both
	 * simple names ({@code new Foo()}) and qualified names
	 * ({@code new pkg.Foo()}). Constructor-level type arguments
	 * ({@code new <T>Foo()}) are skipped.
	 *
	 * @return the class name, or {@code null} for primitive arrays
	 */
	@CheckReturnValue
	@Nullable
	static String findNewClassName(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.DOT)
				return dottedName(child);
			if (child.getType() == TokenTypes.IDENT)
				return child.getText();
		}
		return null;
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
	static DetailAST findNewClassTypeArguments(@Nonnull DetailAST literalNew) {
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
	static DetailAST findNodeAt(@Nonnull DetailAST root, int line, int column, @Nonnull Predicate<DetailAST> predicate) {
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

	@CheckReturnValue
	@Nullable
	private static DetailAST findSameFileClassDef(@Nonnull DetailAST node, @Nonnull String className) {
		for (var enclosing = findEnclosingClassDef(node); enclosing != null; enclosing = findEnclosingClassDef(enclosing)) {
			final var found = findInnerClassDef(enclosing, className);
			if (found != null)
				return found;
		}
		var root = node;
		while (root.getParent() != null)
			root = root.getParent();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var t = child.getType();
			if (t == TokenTypes.CLASS_DEF || t == TokenTypes.INTERFACE_DEF
					|| t == TokenTypes.ENUM_DEF || t == TokenTypes.RECORD_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null && className.equals(ident.getText()))
					return child;
			}
		}
		return null;
	}

	/**
	 * Column of the earliest token in the subtree: the smallest column among the
	 * nodes sitting on {@link #firstLine(DetailAST)}. The subtree has to be
	 * walked because an imaginary node carries its operator's position rather
	 * than its first operand's ({@code EXPR} for {@code x = 5} sits at the
	 * {@code =}, not at the {@code x}).
	 */
	@CheckReturnValue
	static int firstColumn(@Nonnull DetailAST ast) {
		final var first = firstLine(ast);
		var column = Integer.MAX_VALUE;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() == first && node.getColumnNo() < column)
				column = node.getColumnNo();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return column;
	}

	@CheckReturnValue
	static int firstLine(@Nonnull DetailAST ast) {
		var first = ast.getLineNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var line = node.getLineNo();
			if (line < first)
				first = line;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return first;
	}

	@CheckReturnValue
	@Nullable
	public static String getEnclosingTypeName(@Nonnull DetailAST objBlock) {
		final var parent = objBlock.getParent();
		if (parent == null)
			return null;
		final var type = parent.getType();
		if (type != TokenTypes.CLASS_DEF && type != TokenTypes.INTERFACE_DEF
				&& type != TokenTypes.ENUM_DEF && type != TokenTypes.RECORD_DEF
				&& type != TokenTypes.ANNOTATION_DEF)
			return null;
		final var ident = parent.findFirstToken(TokenTypes.IDENT);
		return ident == null ? null : ident.getText();
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
			return lastIdent(firstChild);
		return null;
	}

	@CheckReturnValue
	@Nullable
	public static String getPackageName(@Nonnull DetailAST node) {
		var root = node;
		while (root.getParent() != null)
			root = root.getParent();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.PACKAGE_DEF)
				continue;
			final var dot = child.findFirstToken(TokenTypes.DOT);
			if (dot != null) {
				final var text = FullIdent.createFullIdent(dot).getText();
				return text == null || text.isEmpty() ? null : text;
			}
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null) {
				final var text = ident.getText();
				return text.isEmpty() ? null : text;
			}
		}
		return null;
	}

	/**
	 * Finds the type name of the receiver in a dotted method call.
	 * For {@code obj.method()}, finds the declared type of {@code obj}
	 * (field, parameter, or local variable).
	 * For {@code Type.method()}, returns {@code Type} directly (static call).
	 * For chained calls, returns {@code null};
	 * use {@link #getReceiverTypeName(DetailAST, String, Set)} for chain resolution.
	 */
	@CheckReturnValue
	@Nullable
	static String getReceiverTypeName(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return null;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return null;

		final var receiverName = receiver.getText();
		if (Character.isUpperCase(receiverName.charAt(0)))
			return receiverName;

		return resolveVariableType(methodCall, receiverName);
	}

	/**
	 * Like {@link #getReceiverTypeName(DetailAST)} but also resolves
	 * chained method calls (e.g. {@code fragment.requireView().findViewById()})
	 * by walking the chain and using reflection to resolve intermediate return types.
	 */
	@CheckReturnValue
	@Nullable
	static String getReceiverTypeName(
			@Nonnull DetailAST methodCall,
			@Nullable String packageName,
			@Nonnull Set<String> imports
	) {
		// Descend the receiver chain (base.m1().m2()...): each call's receiver is either an
		// IDENT the simple rule resolves or an inner METHOD_CALL. Collect the inner calls'
		// method names while descending, stop at the first receiver the simple rule resolves
		// (a bare same-class call like requireView().m() has an IDENT receiver, so the simple
		// rule returns null and the DOT guard below bails), then fold the method return types
		// back outward. Iterative (not recursive) so a very long chain cannot overflow the
		// stack, matching the other subtree walks in this file.
		final var methodNames = new ArrayDeque<String>();
		var current = methodCall;
		String baseType;
		while (true) {
			final var simple = getReceiverTypeName(current);
			if (simple != null) {
				baseType = simple;
				break;
			}
			final var firstChild = current.getFirstChild();
			if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
				return null;
			final var receiver = firstChild.getFirstChild();
			if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
				return null;
			final var innerDot = receiver.getFirstChild();
			if (innerDot == null || innerDot.getType() != TokenTypes.DOT)
				return null;
			final var innerMethodName = lastIdent(innerDot);
			if (innerMethodName == null)
				return null;
			methodNames.push(innerMethodName);
			current = receiver;
		}

		var type = baseType;
		while (!methodNames.isEmpty()) {
			final var fqcn = ReflectionUtil.resolveClassName(type, packageName, imports);
			if (fqcn == null)
				return null;
			type = ReflectionUtil.getMethodReturnTypeName(fqcn, methodNames.pop());
			if (type == null)
				return null;
		}
		return type;
	}

	@CheckReturnValue
	@Nullable
	private static String getTypeName(@Nonnull DetailAST typeNode) {
		// descend through nested ARRAY_DECLARATORs; some AST shapes nest them
		// around the base type, others leave them as siblings of the base.
		var dimensions = 0;
		var inner = typeNode.getFirstChild();
		while (inner != null && inner.getType() == TokenTypes.ARRAY_DECLARATOR) {
			++dimensions;
			inner = inner.getFirstChild();
		}
		if (inner == null)
			return null;

		for (var sib = inner.getNextSibling(); sib != null; sib = sib.getNextSibling()) {
			if (sib.getType() == TokenTypes.ARRAY_DECLARATOR)
				++dimensions;
		}

		final var arraySuffix = "[]".repeat(dimensions);
		return switch (inner.getType()) {
			// dottedName walks the DOT chain only; FullIdent would also consume
			// sibling ARRAY_DECLARATOR/TYPE_ARGUMENTS and double-count brackets.
			case TokenTypes.DOT -> dottedName(inner) + arraySuffix;
			case TokenTypes.IDENT -> "var".equals(inner.getText()) ? null : inner.getText() + arraySuffix;
			// primitives have no methods, but primitive arrays are objects
			case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
			     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
			     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
			     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT ->
					dimensions == 0 ? null : inner.getText() + arraySuffix;
			default -> null;
		};
	}

	@CheckReturnValue
	public static boolean hasModifier(@Nonnull DetailAST ast, int modifierType) {
		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(modifierType) != null;
	}

	@CheckReturnValue
	static boolean hasSuppressWarnings(@Nonnull DetailAST modifiers, @Nonnull String key) {
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ANNOTATION)
				continue;
			if (!"SuppressWarnings".equals(annotationName(child)))
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

	/**
	 * The body of a member type named {@code name} that {@code objBlock} inherits. Supertype names
	 * are resolved with {@link #declaredTypeBody} rather than {@link #supertypeBodies}, because the
	 * latter resolves through {@link #sameFileTypeBody} and would re-enter this walk with a fresh
	 * visited set, which a same-file inheritance cycle turns into unbounded recursion.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST inheritedMemberType(
			@Nonnull DetailAST objBlock,
			@Nonnull String name,
			@Nonnull Set<DetailAST> visited
	) {
		final var typeDef = objBlock.getParent();
		for (var clause = typeDef == null ? null : typeDef.getFirstChild();
				clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var superName = clause.getFirstChild(); superName != null; superName = superName.getNextSibling()) {
				final var written = typeName(superName);
				final var superBody = written == null ? null : declaredTypeBody(typeDef, written);
				if (superBody == null || !visited.add(superBody))
					continue;

				for (var member = superBody.getFirstChild(); member != null; member = member.getNextSibling()) {
					final var ident = member.findFirstToken(TokenTypes.IDENT);
					if (ident != null && name.equals(ident.getText())) {
						final var body = member.findFirstToken(TokenTypes.OBJBLOCK);
						if (body != null)
							return body;
					}
				}

				final var deeper = inheritedMemberType(superBody, name, visited);
				if (deeper != null)
					return deeper;
			}
		}
		return null;
	}

	@CheckReturnValue
	static boolean isAssignmentOperator(int tokenType) {
		return switch (tokenType) {
			case TokenTypes.ASSIGN, TokenTypes.BAND_ASSIGN, TokenTypes.BOR_ASSIGN, TokenTypes.BSR_ASSIGN,
			     TokenTypes.BXOR_ASSIGN, TokenTypes.DIV_ASSIGN, TokenTypes.MINUS_ASSIGN, TokenTypes.MOD_ASSIGN,
			     TokenTypes.PLUS_ASSIGN, TokenTypes.SL_ASSIGN, TokenTypes.SR_ASSIGN, TokenTypes.STAR_ASSIGN -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	static boolean isEmptyBody(@Nonnull DetailAST body) {
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
			final var left = cur.getFirstChild();
			if (left == null)
				return false;
			final var right = left.getNextSibling();
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
	static boolean isPureExpression(@Nonnull DetailAST ast) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
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
	static boolean isZeroLiteral(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.NUM_DOUBLE, TokenTypes.NUM_FLOAT,
			     TokenTypes.NUM_INT, TokenTypes.NUM_LONG -> isNumericZero(ast.getText());
			default -> false;
		};
	}

	@CheckReturnValue
	@Nullable
	private static String lastIdent(@Nonnull DetailAST dot) {
		var last = dot.getFirstChild();
		if (last == null)
			return null;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return last.getType() == TokenTypes.IDENT ? last.getText() : null;
	}

	@CheckReturnValue
	static int lastLine(@Nonnull DetailAST ast) {
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
	 * Resolve the type of a field declared on a same-file class definition.
	 * If {@code className} is null, walks up to the enclosing CLASS_DEF /
	 * INTERFACE_DEF / ENUM_DEF / RECORD_DEF and looks for a field named
	 * {@code fieldName}; otherwise locates the named type within the same
	 * compilation unit and resolves the field there.
	 */
	@CheckReturnValue
	@Nullable
	static String resolveSameFileFieldType(@Nonnull DetailAST scope, @Nullable String className, @Nonnull String fieldName) {
		final var classDef = className == null ? findEnclosingClassDef(scope) : findSameFileClassDef(scope, className);
		if (classDef == null)
			return null;
		final var objBlock = classDef.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var typeName = variableTypeName(child, fieldName);
			if (typeName != null)
				return typeName;
		}
		return null;
	}

	/**
	 * Resolve the declared return type of a method named {@code methodName}
	 * with parameter count {@code arity} on any enclosing same-file class.
	 * Returns null when no overload at that arity exists, or when the overloads
	 * at that arity return different types, to avoid corrupting downstream type
	 * inference.
	 */
	@CheckReturnValue
	@Nullable
	static String resolveSameFileMethodReturnType(@Nonnull DetailAST scope, @Nonnull String methodName, int arity) {
		for (var ancestor = scope.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			final var t = ancestor.getType();
			if (t == TokenTypes.CLASS_DEF || t == TokenTypes.INTERFACE_DEF
					|| t == TokenTypes.ENUM_DEF || t == TokenTypes.RECORD_DEF) {
				final var found = findMethodReturnTypeInClass(ancestor, methodName, arity);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	static String resolveVariableType(@Nonnull DetailAST from, @Nonnull String varName) {
		for (var scope = from.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.SLIST || scope.getType() == TokenTypes.OBJBLOCK) {
				for (var sibling = scope.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
					final var typeName = variableTypeName(sibling, varName);
					if (typeName != null)
						return typeName;
				}
			}

			if (scope.getType() == TokenTypes.METHOD_DEF || scope.getType() == TokenTypes.CTOR_DEF) {
				final var params = scope.findFirstToken(TokenTypes.PARAMETERS);
				if (params != null) {
					for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
						final var typeName = variableTypeName(param, varName);
						if (typeName != null)
							return typeName;
					}
				}
			}
		}
		return null;
	}

	/**
	 * The same-file type {@code className} resolves to from {@code scope}, or null when the
	 * compilation unit declares no such type. The name may be qualified ({@code Outer.Box}), in
	 * which case each segment after the first is looked up inside the previous one.
	 */
	@CheckReturnValue
	@Nullable
	static DetailAST sameFileClassDef(@Nonnull DetailAST scope, @Nonnull String className) {
		final var segments = className.split("\\.");
		if (segments.length == 0)
			return null;

		var classDef = findSameFileClassDef(scope, segments[0]);
		for (var i = 1; i < segments.length && classDef != null; ++i)
			classDef = findInnerClassDef(classDef, segments[i]);
		return classDef;
	}

	/**
	 * The body of the same-file type {@code name} names from {@code scope}, resolved the way Java
	 * scoping does: the innermost enclosing scope that declares the name wins, so a local class
	 * shadows a same-named type declared further out. A name no enclosing scope declares may still
	 * be a member type inherited from a supertype, which is in scope without appearing in any frame.
	 *
	 * <p>This is the single answer to "does this file declare that type", so a caller deciding
	 * whether to fall back to the classpath cannot disagree with one walking the inheritance graph.
	 */
	@CheckReturnValue
	@Nullable
	static DetailAST sameFileTypeBody(@Nonnull DetailAST scope, @Nullable String name) {
		if (name == null)
			return null;

		final var declared = declaredTypeBody(scope, name);
		if (declared != null)
			return declared;

		for (var frame = scope; frame != null; frame = frame.getParent()) {
			final var inherited = frame.getType() == TokenTypes.OBJBLOCK
					? inheritedMemberType(frame, name, new HashSet<>())
					: null;
			if (inherited != null)
				return inherited;
		}
		return null;
	}

	@CheckReturnValue
	@Nonnull
	public static String simpleName(@Nonnull String fqcn) {
		return fqcn.substring(fqcn.lastIndexOf('.') + 1);
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
	 * The bodies of {@code objBlock}'s direct supertypes that are declared in the same file.
	 * Supertypes resolved from the classpath have no AST here and are skipped.
	 */
	@CheckReturnValue
	@Nonnull
	static List<DetailAST> supertypeBodies(@Nonnull DetailAST objBlock) {
		final var typeDef = objBlock.getParent();
		if (typeDef == null)
			return List.of();

		final var bodies = new ArrayList<DetailAST>();
		for (var clause = typeDef.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var name = clause.getFirstChild(); name != null; name = name.getNextSibling()) {
				final var superBlock = sameFileTypeBody(typeDef, typeName(name));
				if (superBlock != null)
					bodies.add(superBlock);
			}
		}
		return bodies;
	}

	/** The name written at {@code nameNode}, or null when it is neither an identifier nor a dotted name. */
	@CheckReturnValue
	@Nullable
	static String typeName(@Nullable DetailAST nameNode) {
		if (nameNode == null)
			return null;
		if (nameNode.getType() == TokenTypes.IDENT)
			return nameNode.getText();
		return nameNode.getType() == TokenTypes.DOT ? dottedName(nameNode) : null;
	}

	@CheckReturnValue
	static int typeParameterCount(@Nonnull DetailAST classDef) {
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

	@CheckReturnValue
	@Nonnull
	static String typeText(@Nonnull DetailAST type) {
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null)
			return exprText(dot);
		return "";
	}

	/**
	 * Strips wrapping {@code LPAREN} and {@code EXPR} nodes from a value node,
	 * descending into the parenthesized/expression-wrapped inner node. Returns
	 * the first non-wrapper node, or {@code null} if the chain terminates.
	 */
	@CheckReturnValue
	@Nullable
	static DetailAST unwrapParensAndExpr(@Nullable DetailAST node) {
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
	static DetailAST unwrapParensAndExprFromEnd(@Nullable DetailAST node) {
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
	 * {@code SEMI}/{@code RCURLY}) or {@code null} when the block holds zero or
	 * more than one statement.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST unwrapSingleStatementBlock(@Nonnull DetailAST body) {
		if (body.getType() != TokenTypes.SLIST)
			return body;
		DetailAST single = null;
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.SEMI || child.getType() == TokenTypes.RCURLY)
				continue;
			if (single != null)
				return null;
			single = child;
		}
		return single;
	}

	@CheckReturnValue
	@Nullable
	private static String variableTypeName(@Nonnull DetailAST node, @Nonnull String varName) {
		if (node.getType() != TokenTypes.VARIABLE_DEF && node.getType() != TokenTypes.PARAMETER_DEF)
			return null;

		final var ident = node.findFirstToken(TokenTypes.IDENT);
		if (ident == null || !varName.equals(ident.getText()))
			return null;

		final var type = node.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		final var typeName = getTypeName(type);
		if (typeName != null)
			return typeName;

		// `var` type: infer from the initializer so checks can resolve the real
		// type instead of giving up.
		if (node.getType() == TokenTypes.VARIABLE_DEF) {
			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				return null;
			final var assignChild = assign.getFirstChild();
			if (assignChild == null)
				return null;
			final var init = assignChild.getType() == TokenTypes.EXPR ? assignChild.getFirstChild() : assignChild;
			if (init == null)
				return null;
			if (init.getType() == TokenTypes.STRING_LITERAL)
				return "String";
			if (init.getType() == TokenTypes.METHOD_CALL) {
				final var receiver = init.getFirstChild();
				if (receiver != null && receiver.getType() == TokenTypes.IDENT) {
					final var elist = init.findFirstToken(TokenTypes.ELIST);
					var arity = 0;
					if (elist != null) {
						for (var c = elist.getFirstChild(); c != null; c = c.getNextSibling()) {
							if (c.getType() != TokenTypes.COMMA)
								++arity;
						}
					}
					return resolveSameFileMethodReturnType(node, receiver.getText(), arity);
				}
				return null;
			}
			if (init.getType() != TokenTypes.LITERAL_NEW)
				return null;

			var dimensions = 0;
			for (var child = init.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
					++dimensions;
			}

			final var className = findNewClassName(init);
			if (className != null)
				return className + "[]".repeat(dimensions);
			for (var child = init.getFirstChild(); child != null; child = child.getNextSibling()) {
				switch (child.getType()) {
					case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
					     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
					     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
					     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT -> {
						return child.getText() + "[]".repeat(dimensions);
					}
					default -> {
					}
				}
			}
			return null;
		}
		return null;
	}
}