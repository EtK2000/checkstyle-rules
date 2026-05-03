package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

class AstUtil {
	@CheckReturnValue
	@Nonnull
	static String annotationName(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		// qualified name like @androidx.annotation.NonNull — use last segment
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
									: serializeAst(value)
					);
				}
			}
			else if (child.getType() == TokenTypes.ANNOTATION)
				params.put("value", canonicalAnnotation(child, maxDepth - 1));
			else if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT)
				params.put("value", serializeAst(child));
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

	/**
	 * Returns a canonical string for a TYPE AST node, including primitives,
	 * reference types, qualified names, and arrays.
	 */
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

	/**
	 * Collects the names of all parameters in the given constructor or
	 * method definition.
	 */
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
				case TokenTypes.UNARY_MINUS -> {
					stack.push(first);
					stack.push("-");
				}
				case TokenTypes.UNARY_PLUS -> {
					stack.push(first);
					stack.push("+");
				}
				default -> sb.append(node.getText());
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
		// Walk left-child DOT chain iteratively to collect segments.
		final var segments = new ArrayList<String>();
		var current = dot;
		while (current.getType() == TokenTypes.DOT) {
			final var first = current.getFirstChild();
			if (first == null)
				break;
			final var second = first.getNextSibling();
			if (second != null)
				segments.add(second.getText());
			current = first;
		}
		segments.add(current.getText());
		// Segments were collected right-to-left; reverse for dotted order.
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
			// Collect children, then push in reverse so left-to-right order is preserved.
			final var children = new ArrayList<DetailAST>();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				children.add(child);
			for (var i = children.size() - 1; i >= 0; --i)
				stack.push(children.get(i));
		}
		return sb.toString();
	}

	/**
	 * Extracts the class name from a LITERAL_NEW node, handling both
	 * simple names ({@code new Foo()}) and qualified names
	 * ({@code new pkg.Foo()}). Constructor-level type arguments
	 * ({@code new <T>Foo()}) are correctly skipped by iterating direct
	 * children until the first IDENT or DOT is found.
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
	 * Extracts the method name from the last child of a METHOD_CALL's DOT.
	 */
	@CheckReturnValue
	@Nullable
	private static String getMethodName(@Nonnull DetailAST dot) {
		var last = dot.getFirstChild();
		if (last == null)
			return null;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return last.getType() == TokenTypes.IDENT ? last.getText() : null;
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

		// check if receiver starts with uppercase (likely a class name for static calls)
		if (Character.isUpperCase(receiverName.charAt(0)))
			return receiverName;

		// look up the variable declaration to find its type
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
		final var simple = getReceiverTypeName(methodCall);
		if (simple != null)
			return simple;

		// try resolving chained method calls: receiver is itself a METHOD_CALL
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return null;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
			return null;

		// recursively resolve the inner call's receiver type
		final var innerReceiverType = getReceiverTypeName(receiver, packageName, imports);
		if (innerReceiverType == null) {
			// bare call in the same class (e.g. requireView().method())
			// can't resolve without knowing the enclosing class's own type
			return null;
		}

		final var innerFqcn = ReflectionUtil.resolveClassName(innerReceiverType, packageName, imports);
		if (innerFqcn == null)
			return null;

		// get the inner method's name from the DOT of the inner METHOD_CALL
		final var innerDot = receiver.getFirstChild();
		if (innerDot == null || innerDot.getType() != TokenTypes.DOT)
			return null;

		final var innerMethodName = getMethodName(innerDot);
		if (innerMethodName == null)
			return null;

		// resolve the return type of the inner method
		return ReflectionUtil.getMethodReturnTypeName(innerFqcn, innerMethodName);
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

	@CheckReturnValue
	static boolean isEmptyBody(@Nonnull DetailAST body) {
		return switch (body.getType()) {
			// empty statement: if (x);
			case TokenTypes.EMPTY_STAT -> true;
			// empty block: if (x) {}
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

		// strip trailing type suffix (D/F/L/d/f/l)
		final var lastChar = s.charAt(s.length() - 1);
		if (lastChar == 'D' || lastChar == 'F' || lastChar == 'L'
				|| lastChar == 'd' || lastChar == 'f' || lastChar == 'l')
			s = s.substring(0, s.length() - 1);

		// strip underscores
		s = s.replace("_", "");
		if (s.isEmpty())
			return false;

		// strip hex/binary prefix
		if (s.startsWith("0x") || s.startsWith("0X")
				|| s.startsWith("0b") || s.startsWith("0B"))
			s = s.substring(2);

		// all remaining chars must be zeros, dots, and exponent parts that evaluate to zero
		var hasDigit = false;
		for (var i = 0; i < s.length(); ++i) {
			final var c = s.charAt(i);
			if (c == '0' || c == '.') {
				if (c == '0')
					hasDigit = true;
			}
			else if (c == 'E' || c == 'P' || c == 'e' || c == 'p') {
				// exponent: skip optional sign, remaining must be zeros
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

			// check method/constructor parameters
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

	@CheckReturnValue
	@Nonnull
	private static String serializeAst(@Nonnull DetailAST ast) {
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

		// `var` type: infer from `new X(...)` initializer so checks can resolve
		// the real type instead of giving up.
		if (node.getType() == TokenTypes.VARIABLE_DEF) {
			final var assign = node.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				return null;
			final var assignChild = assign.getFirstChild();
			if (assignChild == null)
				return null;
			final var init = assignChild.getType() == TokenTypes.EXPR ? assignChild.getFirstChild() : assignChild;
			if (init == null || init.getType() != TokenTypes.LITERAL_NEW)
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
						 TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT ->
						{ return child.getText() + "[]".repeat(dimensions); }
					default -> {}
				}
			}
			return null;
		}
		return null;
	}
}