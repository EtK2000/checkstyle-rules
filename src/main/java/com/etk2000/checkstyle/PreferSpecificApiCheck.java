package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags generic API calls where a more specific
 * method is available. Currently detects:
 * <ul>
 *     <li>{@code assertEquals(true/false, x)} -> use {@code assertTrue(x)} / {@code assertFalse(x)}</li>
 *     <li>{@code assertEquals(null, x)} -> use {@code assertNull(x)}</li>
 *     <li>{@code assertNotEquals(true/false, x)} -> use {@code assertFalse(x)} / {@code assertTrue(x)}</li>
 *     <li>{@code assertNotEquals(null, x)} -> use {@code assertNotNull(x)}</li>
 *     <li>{@code assertSame(null, x)} -> use {@code assertNull(x)}</li>
 *     <li>{@code assertNotSame(null, x)} -> use {@code assertNotNull(x)}</li>
 *     <li>{@code Collections.sort(list)} -> use {@code list.sort(...)} (API 24+)</li>
 *     <li>{@code Collections.emptyList()} -> use {@code List.of()} (API 30+)</li>
 *     <li>{@code Collections.emptyMap()} -> use {@code Map.of()} (API 30+)</li>
 *     <li>{@code Collections.emptySet()} -> use {@code Set.of()} (API 30+)</li>
 *     <li>{@code Collections.singleton(x)} -> use {@code Set.of(x)} (API 30+)</li>
 *     <li>{@code Collections.singletonList(x)} -> use {@code List.of(x)} (API 30+)</li>
 *     <li>{@code Collections.singletonMap(k, v)} -> use {@code Map.of(k, v)} (API 30+)</li>
 *     <li>{@code Collections.unmodifiableList(x)} -> use {@code List.copyOf(x)} (API 31+)</li>
 *     <li>{@code Collections.unmodifiableMap(x)} -> use {@code Map.copyOf(x)} (API 31+)</li>
 *     <li>{@code Collections.unmodifiableSet(x)} -> use {@code Set.copyOf(x)} (API 31+)</li>
 *     <li>{@code .collect(Collectors.toList())} -> use {@code .toList()}</li>
 *     <li>{@code .collect(Collectors.toUnmodifiableList())} -> use {@code .toList()}</li>
 *     <li>{@code .equals("")} -> use {@code .isEmpty()}</li>
 *     <li>{@code .get(0)} -> use {@code .getFirst()}</li>
 *     <li>{@code .get(size() - 1)} -> use {@code .getLast()}</li>
 *     <li>{@code .indexOf(str) != -1} / {@code >= 0} -> use {@code .contains(str)}</li>
 *     <li>{@code .indexOf(str) == -1} / {@code < 0} -> use {@code !.contains(str)}</li>
 *     <li>{@code .keySet().contains(k)} -> use {@code .containsKey(k)}</li>
 *     <li>{@code .replaceAll("literal", x)} -> use {@code .replace("literal", x)} (when no regex chars)</li>
 *     <li>{@code .size() == 0} / {@code .size() != 0} / {@code .length() == 0} / {@code .length() != 0} -> use {@code .isEmpty()} / {@code !.isEmpty()}</li>
 *     <li>{@code .stream().count()} -> use {@code .size()}</li>
 *     <li>{@code .stream().findFirst().isPresent()} -> use {@code !.isEmpty()}</li>
 *     <li>{@code .stream().forEach(...)} -> use {@code .forEach(...)} (API 24+)</li>
 *     <li>{@code .toArray(new Type[0])} -> use {@code .toArray(Type[]::new)} (API 33+)</li>
 *     <li>{@code .strip().isEmpty()} -> use {@code .isBlank()} (API 33+)</li>
 *     <li>{@code .strip().length() == 0} -> use {@code .isBlank()} (API 33+)</li>
 *     <li>{@code .trim().isEmpty()} -> use {@code .isBlank()} (API 33+)</li>
 *     <li>{@code .trim().length() == 0} -> use {@code .isBlank()} (API 33+)</li>
 *     <li>{@code .values().contains(v)} -> use {@code .containsValue(v)}</li>
 *     <li>{@code Collections.unmodifiableList(Arrays.asList(...))} -> use {@code List.of(...)}</li>
 *     <li>{@code Arrays.asList(...)} -> use {@code List.of(...)} (API 30+)</li>
 *     <li>{@code String.format("...", args)} -> use {@code "...".formatted(args)} (API 34+)</li>
 * </ul>
 * Suppresses {@code .get(0)} when the same receiver also calls
 * {@code .get(N)} with other indices in the same method scope
 * (sequential access pattern).
 * <p>
 * Uses reflection to verify the receiver type actually has
 * the suggested method before flagging.
 */
public class PreferSpecificApiCheck extends AbstractAstCheck {
	private static final int MIN_SDK_COLLECTION_FACTORY = 30;
	private static final int MIN_SDK_COPY_OF = 31;
	private static final int MIN_SDK_FOR_EACH = 24;
	private static final int MIN_SDK_FORMATTED = 34;
	private static final int MIN_SDK_GET_FIRST_LAST = 35;
	private static final int MIN_SDK_IS_BLANK = 33;
	private static final int MIN_SDK_TO_ARRAY_GENERATOR = 33;
	private static final String MSG_ASSERT = "prefer.api.assert";
	private static final String MSG_METHOD = "prefer.replacement";

	/**
	 * Returns a two-element array {@code [replacement, literal]} for a call like
	 * {@code assertEquals(true, x)} or {@code assertNotEquals(null, x)},
	 * or {@code null} if the call is not a simplifiable assertion.
	 * Handles both static-import ({@code assertEquals}) and qualified
	 * ({@code Assert.assertEquals}) forms, as well as
	 * {@code assertSame}/{@code assertNotSame} with {@code null}.
	 */
	@CheckReturnValue
	@Nullable
	private static String[] assertionSimplification(@Nonnull DetailAST methodCall) {
		final var methodName = AstUtil.getMethodName(methodCall);
		if (methodName == null)
			return null;

		final var isEquals = "assertEquals".equals(methodName) || "assertSame".equals(methodName);
		final var isNotEquals = "assertNotEquals".equals(methodName) || "assertNotSame".equals(methodName);
		if (!isEquals && !isNotEquals)
			return null;

		final var isSame = "assertSame".equals(methodName) || "assertNotSame".equals(methodName);

		// find the two-argument form (expected, actual), skip if more than 3 args
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;

		var argCount = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++argCount;
		}

		// for 3-arg form (message, expected, actual), check the second arg
		// for 2-arg form (expected, actual), check the first arg
		final DetailAST expectedExpr;
		if (argCount == 2)
			expectedExpr = elist.getFirstChild();
		else if (argCount == 3)
			expectedExpr = elist.getFirstChild().getNextSibling().getNextSibling();
		else
			return null;

		final var expected = expectedExpr.getType() == TokenTypes.EXPR
				? expectedExpr.getFirstChild()
				: expectedExpr;
		if (expected == null)
			return null;

		// also check the last argument (actual) for null/true/false in reversed form
		final var lastArg = elist.getLastChild();
		final var actual = lastArg.getType() == TokenTypes.EXPR
				? lastArg.getFirstChild()
				: lastArg;

		// for 3-arg form, also check the first arg (JUnit 5 message-last: expected, actual, message)
		final DetailAST firstArg;
		if (argCount == 3) {
			final var raw = elist.getFirstChild();
			firstArg = raw.getType() == TokenTypes.EXPR ? raw.getFirstChild() : raw;
		}
		else
			firstArg = null;

		if (expected.getType() == TokenTypes.LITERAL_NULL || actual.getType() == TokenTypes.LITERAL_NULL
				|| (firstArg != null && firstArg.getType() == TokenTypes.LITERAL_NULL))
			return new String[]{isEquals ? "assertNull" : "assertNotNull", "null"};

		// assertSame/assertNotSame only applies to null, not true/false
		if (isSame)
			return null;

		if (expected.getType() == TokenTypes.LITERAL_TRUE || actual.getType() == TokenTypes.LITERAL_TRUE
				|| (firstArg != null && firstArg.getType() == TokenTypes.LITERAL_TRUE))
			return new String[]{isEquals ? "assertTrue" : "assertFalse", "true"};
		if (expected.getType() == TokenTypes.LITERAL_FALSE || actual.getType() == TokenTypes.LITERAL_FALSE
				|| (firstArg != null && firstArg.getType() == TokenTypes.LITERAL_FALSE))
			return new String[]{isEquals ? "assertFalse" : "assertTrue", "false"};
		return null;
	}

	@CheckReturnValue
	@Nonnull
	private static String childText(@Nonnull DetailAST ast) {
		if (ast.getChildCount() == 0)
			return ast.getText();

		final var sb = new StringBuilder();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			sb.append(childText(child));
		return sb.toString();
	}

	/**
	 * Returns the replacement for a {@code Collections.unmodifiableXxx(x)} call,
	 * e.g. {@code "List.copyOf"} for {@code Collections.unmodifiableList(x)},
	 * or {@code null} if the call is not a match.
	 */
	@CheckReturnValue
	@Nullable
	private static String collectionsCopyOfReplacement(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;

		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		if (receiver == null || method == null)
			return null;

		if (receiver.getType() != TokenTypes.IDENT || !"Collections".equals(receiver.getText()))
			return null;
		if (method.getType() != TokenTypes.IDENT)
			return null;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return null;

		if ("unmodifiableList".equals(method.getText()) && isArraysAsListCall(elist.getFirstChild()))
			return "List.of";

		return switch (method.getText()) {
			case "unmodifiableList" -> "List.copyOf";
			case "unmodifiableMap" -> "Map.copyOf";
			case "unmodifiableSet" -> "Set.copyOf";
			default -> null;
		};
	}

	/**
	 * Returns the replacement prefix for a {@code Collections} factory call,
	 * e.g. {@code "List.of"} for {@code Collections.emptyList()} or
	 * {@code Collections.singletonList(x)}, or {@code null} if not a match.
	 */
	@CheckReturnValue
	@Nullable
	private static String collectionsFactoryReplacement(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;

		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		if (receiver == null || method == null)
			return null;

		if (receiver.getType() != TokenTypes.IDENT || !"Collections".equals(receiver.getText()))
			return null;
		if (method.getType() != TokenTypes.IDENT)
			return null;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		final var hasArgs = elist != null && elist.getChildCount() > 0;

		return switch (method.getText()) {
			case "emptyList" -> hasArgs ? null : "List.of";
			case "emptyMap" -> hasArgs ? null : "Map.of";
			case "emptySet" -> hasArgs ? null : "Set.of";
			case "singleton" -> hasArgs ? "Set.of" : null;
			case "singletonList" -> hasArgs ? "List.of" : null;
			case "singletonMap" -> hasArgs ? "Map.of" : null;
			default -> null;
		};
	}

	@CheckReturnValue
	private static int decodedJavaStringLength(@Nonnull String literalText) {
		if (literalText.length() < 2 || literalText.charAt(0) != '"'
				|| literalText.charAt(literalText.length() - 1) != '"')
			return -1;
		final var content = literalText.substring(1, literalText.length() - 1);
		var len = 0;
		var i = 0;
		while (i < content.length()) {
			final var c = content.charAt(i);
			if (c == '\\') {
				if (i + 1 >= content.length())
					return -1;
				final var next = content.charAt(i + 1);
				if (next == '"' || next == '\'' || next == '\\' || next == 'b' || next == 'f'
						|| next == 'n' || next == 'r' || next == 's' || next == 't')
					i += 2;
				else if (next == 'u') {
					if (i + 6 > content.length())
						return -1;
					for (var j = i + 2; j < i + 6; ++j) {
						final var hex = content.charAt(j);
						if (!isHexDigit(hex))
							return -1;
					}
					i += 6;
				}
				else if (next >= '0' && next <= '7') {
					// JLS octal escape: \0-\377, so 3-digit form requires first digit in 0-3
					final var maxDigits = next <= '3' ? 3 : 2;
					var j = i + 2;
					while (j < content.length() && j - i - 1 < maxDigits
							&& content.charAt(j) >= '0' && content.charAt(j) <= '7')
						++j;
					i = j;
				}
				else
					return -1;
			}
			else
				++i;
			++len;
		}
		return len;
	}

	/**
	 * Returns the METHOD_CALL node for the .indexOf() call inside a comparison,
	 * checking both operand positions.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST indexOfCallFromComparison(@Nonnull DetailAST comparison) {
		final var left = comparison.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left != null && isIndexOfStringCall(left))
			return left.getType() == TokenTypes.EXPR ? left.getFirstChild() : left;
		return right != null && right.getType() == TokenTypes.EXPR ? right.getFirstChild() : right;
	}

	/**
	 * Detects comparisons equivalent to contains/!contains using indexOf
	 * and returns {@code ".contains(...)"} or {@code "!.contains(...)"}
	 * accordingly, or {@code null} if the AST is not an indexOf comparison.
	 * Handles: {@code != -1}, {@code >= 0}, {@code == -1}, {@code < 0},
	 * and their reversed-operand forms.
	 */
	@CheckReturnValue
	@Nullable
	private static String indexOfContainsReplacement(@Nonnull DetailAST ast) {
		final var left = ast.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left == null || right == null)
			return null;

		final boolean indexOfOnLeft;
		if (isIndexOfStringCall(left) && (isLiteralNegativeOne(right) || isLiteralZero(right)))
			indexOfOnLeft = true;
		else if (isIndexOfStringCall(right) && (isLiteralNegativeOne(left) || isLiteralZero(left)))
			indexOfOnLeft = false;
		else
			return null;

		final var literal = indexOfOnLeft ? right : left;
		final var isNegOne = isLiteralNegativeOne(literal);

		// normalize operator so indexOf is conceptually on the left
		final var normalizedOp = indexOfOnLeft
				? ast.getType()
				: switch (ast.getType()) {
			case TokenTypes.GE -> TokenTypes.LE;
			case TokenTypes.GT -> TokenTypes.LT;
			case TokenTypes.LE -> TokenTypes.GE;
			case TokenTypes.LT -> TokenTypes.GT;
			default -> ast.getType();
		};

		// with indexOf on the left: != -1 and >= 0 mean "contains", == -1 and < 0 mean "not contains"
		return switch (normalizedOp) {
			case TokenTypes.EQUAL -> isNegOne ? "!.contains(...)" : null;
			case TokenTypes.GE -> !isNegOne ? ".contains(...)" : null;
			case TokenTypes.GT -> isNegOne ? ".contains(...)" : null;
			case TokenTypes.LE -> isNegOne ? "!.contains(...)" : null;
			case TokenTypes.LT -> !isNegOne ? "!.contains(...)" : null;
			case TokenTypes.NOT_EQUAL -> isNegOne ? ".contains(...)" : null;
			default -> null;
		};
	}

	@CheckReturnValue
	private static boolean isArraysAsListCall(@Nonnull DetailAST ast) {
		final var inner = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return false;
		final var dot = inner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		return receiver != null && method != null
				&& receiver.getType() == TokenTypes.IDENT && "Arrays".equals(receiver.getText())
				&& method.getType() == TokenTypes.IDENT && "asList".equals(method.getText());
	}

	@CheckReturnValue
	private static boolean isCollectionsSortCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		if (receiver == null || method == null)
			return false;
		if (receiver.getType() != TokenTypes.IDENT || !"Collections".equals(receiver.getText()))
			return false;
		if (method.getType() != TokenTypes.IDENT || !"sort".equals(method.getText()))
			return false;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;
		var argCount = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++argCount;
		}
		return argCount == 1 || argCount == 2;
	}

	private static boolean isCollectToListCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"collect".equals(last.getText()))
			return false;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return false;

		final var arg = elist.getFirstChild();
		final var inner = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var innerDot = inner.findFirstToken(TokenTypes.DOT);
		if (innerDot == null)
			return false;

		final var receiver = innerDot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		return receiver != null && method != null
				&& receiver.getType() == TokenTypes.IDENT && "Collectors".equals(receiver.getText())
				&& method.getType() == TokenTypes.IDENT
				&& ("toList".equals(method.getText()) || "toUnmodifiableList".equals(method.getText()));
	}

	/**
	 * Detects comparisons equivalent to isEmpty/!isEmpty and returns
	 * {@code "isEmpty()"} or {@code "!isEmpty()"} accordingly,
	 * or {@code null} if the AST is not a size/length-vs-zero comparison.
	 * Handles: {@code ==0}, {@code !=0}, {@code >0}, {@code >=1},
	 * {@code <1}, {@code <=0}, and their reversed-operand forms.
	 */
	@CheckReturnValue
	@Nullable
	private static String isEmptyReplacement(@Nonnull DetailAST ast) {
		final var left = ast.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left == null || right == null)
			return null;

		return switch (ast.getType()) {
			case TokenTypes.EQUAL -> {
				if ((isSizeCall(left) && isLiteralZero(right))
						|| (isLiteralZero(left) && isSizeCall(right)))
					yield "isEmpty()";
				yield null;
			}
			case TokenTypes.GE -> {
				if (isSizeCall(left) && isLiteralOne(right))
					yield "!isEmpty()";
				if (isLiteralZero(left) && isSizeCall(right))
					yield "isEmpty()";
				yield null;
			}
			case TokenTypes.GT -> {
				if (isSizeCall(left) && isLiteralZero(right))
					yield "!isEmpty()";
				if (isLiteralOne(left) && isSizeCall(right))
					yield "isEmpty()";
				yield null;
			}
			case TokenTypes.LE -> {
				if (isSizeCall(left) && isLiteralZero(right))
					yield "isEmpty()";
				if (isLiteralOne(left) && isSizeCall(right))
					yield "!isEmpty()";
				yield null;
			}
			case TokenTypes.LT -> {
				if (isSizeCall(left) && isLiteralOne(right))
					yield "isEmpty()";
				if (isLiteralZero(left) && isSizeCall(right))
					yield "!isEmpty()";
				yield null;
			}
			case TokenTypes.NOT_EQUAL -> {
				if ((isSizeCall(left) && isLiteralZero(right))
						|| (isLiteralZero(left) && isSizeCall(right)))
					yield "!isEmpty()";
				yield null;
			}
			default -> null;
		};
	}

	/**
	 * Checks whether the METHOD_CALL is {@code receiver.equals("")}
	 * where the argument is an empty string literal. Does NOT match
	 * the {@code "".equals(receiver)} form (null-safe idiom).
	 */
	@CheckReturnValue
	private static boolean isEqualsEmptyString(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"equals".equals(last.getText()))
			return false;

		final var receiver = dot.getFirstChild();
		if (receiver != null && receiver.getType() == TokenTypes.STRING_LITERAL)
			return false;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return false;

		final var arg = elist.getFirstChild();
		final var inner = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		return inner != null
				&& inner.getType() == TokenTypes.STRING_LITERAL
				&& "\"\"".equals(inner.getText());
	}

	@CheckReturnValue
	private static boolean isGetCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return "get".equals(last.getText());
	}

	@CheckReturnValue
	private static boolean isHexDigit(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	/**
	 * Checks whether the call is {@code .indexOf(str)} or {@code .lastIndexOf(str)}
	 * (also 2-arg overloads) where the first argument is a STRING_LITERAL whose
	 * raw textual length is exactly one character. The check uses Java escape
	 * decoding to ensure {@code "\n"} (length-1 escape sequence) is recognised.
	 */
	@CheckReturnValue
	private static boolean isIndexOfSingleCharStringCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		final var name = last.getText();
		if (!"indexOf".equals(name) && !"lastIndexOf".equals(name))
			return false;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;
		final var firstArg = elist.getFirstChild();
		if (firstArg == null)
			return false;
		final var inner = firstArg.getType() == TokenTypes.EXPR ? firstArg.getFirstChild() : firstArg;
		if (inner == null || inner.getType() != TokenTypes.STRING_LITERAL)
			return false;
		return decodedJavaStringLength(inner.getText()) == 1;
	}

	/**
	 * Checks whether the AST is a {@code .indexOf(str)} call with a
	 * single String argument (not char). Handles EXPR wrapper.
	 */
	@CheckReturnValue
	private static boolean isIndexOfStringCall(@Nonnull DetailAST ast) {
		final var inner = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var dot = inner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"indexOf".equals(last.getText()))
			return false;

		final var elist = inner.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return false;

		final var arg = elist.getFirstChild();
		final var argInner = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		return argInner != null && argInner.getType() == TokenTypes.STRING_LITERAL;
	}

	@CheckReturnValue
	private static boolean isLiteralNegativeOne(@Nonnull DetailAST expr) {
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		if (inner == null || inner.getType() != TokenTypes.UNARY_MINUS)
			return false;
		final var num = inner.getFirstChild();
		return num != null && num.getType() == TokenTypes.NUM_INT && "1".equals(num.getText());
	}

	@CheckReturnValue
	private static boolean isLiteralOne(@Nonnull DetailAST expr) {
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		return inner != null
				&& inner.getType() == TokenTypes.NUM_INT
				&& "1".equals(inner.getText());
	}

	@CheckReturnValue
	private static boolean isLiteralZero(@Nonnull DetailAST expr) {
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		return inner != null
				&& inner.getType() == TokenTypes.NUM_INT
				&& "0".equals(inner.getText());
	}

	@CheckReturnValue
	private static boolean isRemoveCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return "remove".equals(last.getText());
	}

	@CheckReturnValue
	private static boolean isReplaceAllWithLiteral(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"replaceAll".equals(last.getText()))
			return false;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;
		var argCount = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++argCount;
		}
		if (argCount != 2)
			return false;
		final var firstArg = elist.getFirstChild();
		final var inner = firstArg.getType() == TokenTypes.EXPR ? firstArg.getFirstChild() : firstArg;
		if (inner == null || inner.getType() != TokenTypes.STRING_LITERAL)
			return false;
		final var text = inner.getText();
		final var content = text.substring(1, text.length() - 1);
		for (var i = 0; i < content.length(); ++i) {
			if (".*()+?[]{}|^$\\".indexOf(content.charAt(i)) >= 0)
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isSizeCall(@Nonnull DetailAST ast) {
		final var inner = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var dot = inner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"length".equals(last.getText()) && !"size".equals(last.getText()))
			return false;

		final var elist = inner.findFirstToken(TokenTypes.ELIST);
		if (elist != null && elist.getChildCount() > 0)
			return false;

		// skip .trim/.strip().length(), handled by isBlank detection
		if ("length".equals(last.getText())) {
			final var receiver = dot.getFirstChild();
			if (receiver != null && receiver.getType() == TokenTypes.METHOD_CALL && isTrimOrStripCall(receiver))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isSizeMinusOne(@Nonnull DetailAST expr, @Nonnull DetailAST dot) {
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		if (inner == null || inner.getType() != TokenTypes.MINUS)
			return false;

		final var left = inner.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left == null || right == null)
			return false;

		if (right.getType() != TokenTypes.NUM_INT || !"1".equals(right.getText()))
			return false;

		if (left.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var sizeDot = left.findFirstToken(TokenTypes.DOT);
		if (sizeDot == null)
			return false;

		var sizeName = sizeDot.getFirstChild();
		while (sizeName.getNextSibling() != null)
			sizeName = sizeName.getNextSibling();
		if (!"size".equals(sizeName.getText()))
			return false;

		final var getReceiver = receiverText(dot);
		final var sizeReceiver = receiverText(sizeDot);
		return !getReceiver.isEmpty() && getReceiver.equals(sizeReceiver);
	}

	/**
	 * Checks whether the METHOD_CALL is a standalone {@code Arrays.asList(...)} call
	 * that is NOT nested inside {@code Collections.unmodifiableList()} (which is
	 * already handled by the copyOf detection).
	 */
	@CheckReturnValue
	private static boolean isStandaloneArraysAsListCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		if (receiver == null || method == null)
			return false;

		if (receiver.getType() != TokenTypes.IDENT || !"Arrays".equals(receiver.getText()))
			return false;
		if (method.getType() != TokenTypes.IDENT || !"asList".equals(method.getText()))
			return false;

		final var parent = methodCall.getParent();
		if (parent != null && parent.getType() == TokenTypes.EXPR) {
			final var grandparent = parent.getParent();
			if (grandparent != null && grandparent.getType() == TokenTypes.ELIST) {
				final var outerCall = grandparent.getParent();
				if (outerCall != null && outerCall.getType() == TokenTypes.METHOD_CALL) {
					final var outerDot = outerCall.findFirstToken(TokenTypes.DOT);
					if (outerDot != null) {
						final var outerReceiver = outerDot.getFirstChild();
						final var outerMethod = outerReceiver != null ? outerReceiver.getNextSibling() : null;
						if (outerReceiver != null && outerMethod != null
								&& "Collections".equals(outerReceiver.getText())
								&& "unmodifiableList".equals(outerMethod.getText()))
							return false;
					}
				}
			}
		}
		return true;
	}

	@CheckReturnValue
	private static boolean isStreamFindFirstIsPresentCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"isPresent".equals(last.getText()))
			return false;
		final var isPresentElist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (isPresentElist != null && isPresentElist.getChildCount() > 0)
			return false;
		final var findFirstCall = dot.getFirstChild();
		if (findFirstCall == null || findFirstCall.getType() != TokenTypes.METHOD_CALL)
			return false;
		return isStreamTerminalCall(findFirstCall, "findFirst");
	}

	/**
	 * Checks whether the METHOD_CALL is {@code .stream().forEach(...)},
	 * i.e. a forEach terminal directly chained on a stream() call
	 * with no intermediate operations.
	 */
	private static boolean isStreamForEachCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"forEach".equals(last.getText()))
			return false;

		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var streamDot = receiver.findFirstToken(TokenTypes.DOT);
		if (streamDot == null)
			return false;

		var streamMethodName = streamDot.getFirstChild();
		while (streamMethodName.getNextSibling() != null)
			streamMethodName = streamMethodName.getNextSibling();
		if (!"stream".equals(streamMethodName.getText()))
			return false;

		final var streamElist = receiver.findFirstToken(TokenTypes.ELIST);
		return streamElist == null || streamElist.getChildCount() == 0;
	}

	@CheckReturnValue
	private static boolean isStreamTerminalCall(@Nonnull DetailAST methodCall, @Nonnull String terminalName) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!terminalName.equals(last.getText()))
			return false;
		final var terminalElist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (terminalElist != null && terminalElist.getChildCount() > 0)
			return false;
		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
			return false;
		final var streamDot = receiver.findFirstToken(TokenTypes.DOT);
		if (streamDot == null)
			return false;
		var streamMethodName = streamDot.getFirstChild();
		while (streamMethodName.getNextSibling() != null)
			streamMethodName = streamMethodName.getNextSibling();
		if (!"stream".equals(streamMethodName.getText()))
			return false;
		final var streamElist = receiver.findFirstToken(TokenTypes.ELIST);
		return streamElist == null || streamElist.getChildCount() == 0;
	}

	/**
	 * Detects {@code String.format(...)} calls that can be simplified:
	 * single-arg calls (any arg type) and multi-arg calls where the first
	 * arg is a string literal.
	 */
	@CheckReturnValue
	private static boolean isStringFormatCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		final var receiver = dot.getFirstChild();
		final var method = receiver != null ? receiver.getNextSibling() : null;
		if (receiver == null || method == null)
			return false;

		if (receiver.getType() != TokenTypes.IDENT || !"String".equals(receiver.getText()))
			return false;
		if (method.getType() != TokenTypes.IDENT || !"format".equals(method.getText()))
			return false;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() == 0)
			return false;

		var argCount = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.COMMA)
				++argCount;
		}

		if (argCount == 1)
			return true;

		final var firstArg = elist.getFirstChild();
		final var inner = firstArg.getType() == TokenTypes.EXPR ? firstArg.getFirstChild() : firstArg;
		return inner != null && inner.getType() == TokenTypes.STRING_LITERAL;
	}

	/**
	 * Checks whether the METHOD_CALL is {@code receiver.trim().isEmpty()} or
	 * {@code receiver.strip().isEmpty()}, i.e. an {@code isEmpty()} call whose
	 * receiver is a {@code .trim()} or {@code .strip()} call.
	 */
	@CheckReturnValue
	private static boolean isTrimIsEmptyCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"isEmpty".equals(last.getText()))
			return false;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist != null && elist.getChildCount() > 0)
			return false;

		final var receiver = dot.getFirstChild();
		return receiver != null && receiver.getType() == TokenTypes.METHOD_CALL && isTrimOrStripCall(receiver);
	}

	/**
	 * Like {@link #isSizeCall(DetailAST)} but checks for {@code .trim().length()} or
	 * {@code .strip().length()} specifically. Used by the isBlank detection.
	 */
	@CheckReturnValue
	private static boolean isTrimLengthCall(@Nonnull DetailAST ast) {
		final var inner = ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return false;

		final var dot = inner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"length".equals(last.getText()))
			return false;

		final var elist = inner.findFirstToken(TokenTypes.ELIST);
		if (elist != null && elist.getChildCount() > 0)
			return false;

		final var receiver = dot.getFirstChild();
		return receiver != null && receiver.getType() == TokenTypes.METHOD_CALL && isTrimOrStripCall(receiver);
	}

	/**
	 * Checks whether the METHOD_CALL is a {@code .trim()} or {@code .strip()} call
	 * with no arguments.
	 */
	@CheckReturnValue
	private static boolean isTrimOrStripCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"strip".equals(last.getText()) && !"trim".equals(last.getText()))
			return false;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		return elist == null || elist.getChildCount() == 0;
	}

	@CheckReturnValue
	@Nullable
	private static String mapChainReplacement(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"contains".equals(last.getText()))
			return null;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL)
			return null;
		final var receiverDot = receiver.findFirstToken(TokenTypes.DOT);
		if (receiverDot == null)
			return null;
		var receiverMethod = receiverDot.getFirstChild();
		while (receiverMethod.getNextSibling() != null)
			receiverMethod = receiverMethod.getNextSibling();
		final var receiverElist = receiver.findFirstToken(TokenTypes.ELIST);
		if (receiverElist != null && receiverElist.getChildCount() > 0)
			return null;
		return switch (receiverMethod.getText()) {
			case "keySet" -> ".containsKey(...)";
			case "values" -> ".containsValue(...)";
			default -> null;
		};
	}

	@CheckReturnValue
	@Nonnull
	private static String receiverText(@Nonnull DetailAST dot) {
		final var sb = new StringBuilder();
		for (var child = dot.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getNextSibling() == null)
				break;
			if (!sb.isEmpty())
				sb.append('.');
			sb.append(childText(child));
		}
		return sb.toString();
	}

	/**
	 * Returns the METHOD_CALL node for the .size() call inside a comparison,
	 * checking both operand positions.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST sizeCallFromComparison(@Nonnull DetailAST comparison) {
		final var left = comparison.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left != null && isSizeCall(left))
			return left.getType() == TokenTypes.EXPR ? left.getFirstChild() : left;
		return right != null && right.getType() == TokenTypes.EXPR ? right.getFirstChild() : right;
	}

	@CheckReturnValue
	@Nullable
	private static String stringLiteralToCharLiteral(@Nonnull String stringLiteral) {
		if (stringLiteral.length() < 3)
			return null;
		final var content = stringLiteral.substring(1, stringLiteral.length() - 1);
		// special cases: single quote needs escaping in char; \" escape in string -> plain " in char
		if ("'".equals(content))
			return "'\\''";
		if ("\\\"".equals(content))
			return "'\"'";
		// any other escape sequence works as-is in a char literal
		return "'" + content + "'";
	}

	/**
	 * Returns the array element type name from a {@code .toArray(new Type[0])} call,
	 * or {@code null} if the METHOD_CALL is not a matching toArray call.
	 */
	@CheckReturnValue
	@Nullable
	private static String toArrayNewZeroType(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;

		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (!"toArray".equals(last.getText()))
			return null;

		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return null;

		final var arg = elist.getFirstChild();
		final var inner = arg.getType() == TokenTypes.EXPR ? arg.getFirstChild() : arg;
		if (inner == null || inner.getType() != TokenTypes.LITERAL_NEW)
			return null;

		final var typeName = AstUtil.findNewClassName(inner);
		if (typeName == null)
			return null;

		// skip multi-dimensional arrays (e.g. new String[0][])
		var arrayDeclCount = 0;
		for (var child = inner.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				++arrayDeclCount;
		}
		if (arrayDeclCount != 1)
			return null;

		final var arrayDecl = inner.findFirstToken(TokenTypes.ARRAY_DECLARATOR);
		if (arrayDecl == null)
			return null;

		final var sizeExpr = arrayDecl.findFirstToken(TokenTypes.EXPR);
		if (sizeExpr == null)
			return null;

		final var sizeNum = sizeExpr.getFirstChild();
		if (sizeNum == null || sizeNum.getType() != TokenTypes.NUM_INT || !"0".equals(sizeNum.getText()))
			return null;

		// skip if annotations are present on the type (e.g. new @NonNull String[0])
		if (inner.findFirstToken(TokenTypes.ANNOTATIONS) != null)
			return null;

		return typeName;
	}

	/**
	 * Detects comparisons equivalent to isBlank/!isBlank using trim().length()
	 * or strip().length() and returns {@code ".isBlank()"} or {@code "!.isBlank()"}
	 * accordingly, or {@code null} if the AST is not a trim/strip().length() comparison.
	 */
	@CheckReturnValue
	@Nullable
	private static String trimLengthZeroReplacement(@Nonnull DetailAST ast) {
		final var left = ast.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left == null || right == null)
			return null;

		return switch (ast.getType()) {
			case TokenTypes.EQUAL -> {
				if ((isTrimLengthCall(left) && isLiteralZero(right))
						|| (isLiteralZero(left) && isTrimLengthCall(right)))
					yield ".isBlank()";
				yield null;
			}
			case TokenTypes.GE -> {
				if (isTrimLengthCall(left) && isLiteralOne(right))
					yield "!.isBlank()";
				if (isLiteralZero(left) && isTrimLengthCall(right))
					yield ".isBlank()";
				yield null;
			}
			case TokenTypes.GT -> {
				if (isTrimLengthCall(left) && isLiteralZero(right))
					yield "!.isBlank()";
				if (isLiteralOne(left) && isTrimLengthCall(right))
					yield ".isBlank()";
				yield null;
			}
			case TokenTypes.LE -> {
				if (isTrimLengthCall(left) && isLiteralZero(right))
					yield ".isBlank()";
				if (isLiteralOne(left) && isTrimLengthCall(right))
					yield "!.isBlank()";
				yield null;
			}
			case TokenTypes.LT -> {
				if (isTrimLengthCall(left) && isLiteralOne(right))
					yield ".isBlank()";
				if (isLiteralZero(left) && isTrimLengthCall(right))
					yield "!.isBlank()";
				yield null;
			}
			case TokenTypes.NOT_EQUAL -> {
				if ((isTrimLengthCall(left) && isLiteralZero(right))
						|| (isLiteralZero(left) && isTrimLengthCall(right)))
					yield "!.isBlank()";
				yield null;
			}
			default -> null;
		};
	}

	/**
	 * Given a node matched by {@link #isTrimIsEmptyCall} or
	 * {@link #trimLengthZeroReplacement}, returns the actual method name
	 * ({@code "trim"} or {@code "strip"}) for use in violation messages.
	 */
	@CheckReturnValue
	@Nonnull
	private static String trimOrStripName(@Nonnull DetailAST detectedNode) {
		final DetailAST trimOrStripCall;
		if (detectedNode.getType() == TokenTypes.METHOD_CALL)
			trimOrStripCall = detectedNode.findFirstToken(TokenTypes.DOT).getFirstChild();
		else {
			final var left = detectedNode.getFirstChild();
			final var side = isTrimLengthCall(left) ? left : left.getNextSibling();
			final var inner = side.getType() == TokenTypes.EXPR ? side.getFirstChild() : side;
			trimOrStripCall = inner.findFirstToken(TokenTypes.DOT).getFirstChild();
		}
		final var dot = trimOrStripCall.findFirstToken(TokenTypes.DOT);
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return last.getText();
	}

	private final Set<String> imports = new HashSet<>();

	private int minSdk = Integer.MAX_VALUE;
	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.COMPACT_CTOR_DEF,
				TokenTypes.CTOR_DEF,
				TokenTypes.IMPORT,
				TokenTypes.INSTANCE_INIT,
				TokenTypes.METHOD_DEF,
				TokenTypes.PACKAGE_DEF,
				TokenTypes.STATIC_INIT
		};
	}

	/**
	 * Checks whether the receiver of a .get() call has the specified
	 * method available, using reflection to resolve the receiver type.
	 * Returns {@code true} if the type can't be resolved (best-effort:
	 * flag it and let the user decide).
	 */
	@CheckReturnValue
	private boolean receiverHasMethod(@Nonnull DetailAST methodCall, @Nonnull String methodName) {
		final var receiverTypeName = AstUtil.getReceiverTypeName(methodCall, packageName, imports);
		if (receiverTypeName == null)
			return true;

		final var fqcn = ReflectionUtil.resolveClassName(receiverTypeName, packageName, imports);
		return fqcn == null || ReflectionUtil.hasMethod(fqcn, methodName);
	}

	/**
	 * Like {@link #receiverHasMethod} but returns {@code false} when the
	 * type can't be resolved. Used for isEmpty replacements where a wrong
	 * suggestion would break compilation (e.g. {@code File.length() > 0}
	 * has no {@code isEmpty()}).
	 */
	@CheckReturnValue
	private boolean receiverHasMethodStrict(@Nonnull DetailAST methodCall, @Nonnull String methodName) {
		final var receiverTypeName = AstUtil.getReceiverTypeName(methodCall, packageName, imports);
		if (receiverTypeName == null)
			return false;

		final var fqcn = ReflectionUtil.resolveClassName(receiverTypeName, packageName, imports);
		return fqcn != null && ReflectionUtil.hasMethod(fqcn, methodName);
	}

	/**
	 * Checks whether the receiver's {@code isEmpty()} comes from
	 * {@link CharSequence} (API 35) rather than {@link String} (API 1).
	 * Returns {@code false} if the receiver type can't be resolved.
	 */
	@CheckReturnValue
	private boolean receiverIsCharSequenceNotString(@Nonnull DetailAST methodCall) {
		final var typeName = AstUtil.getReceiverTypeName(methodCall, packageName, imports);
		if (typeName == null)
			return false;
		final var fqcn = ReflectionUtil.resolveClassName(typeName, packageName, imports);
		return fqcn != null && ReflectionUtil.isCharSequenceNotString(fqcn);
	}

	/**
	 * Sets the minimum SDK version for the target platform.
	 * APIs not available below this SDK level will not be suggested.
	 * For example, {@code .getFirst()}/{@code .getLast()} require Android API 35+.
	 * <p>Called by Checkstyle via reflection when {@code minSdk} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}

	private void visitArraysAsList(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isStandaloneArraysAsListCall(n));
		for (var call : calls) {
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			final var hasArgs = elist != null && elist.getChildCount() > 0;
			final var argText = hasArgs ? "(...)" : "()";
			log(call, MSG_METHOD, "List.of" + argText, "Arrays.asList" + argText);
		}
	}

	private void visitAssertions(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && assertionSimplification(n) != null);
		for (var call : calls) {
			final var result = assertionSimplification(call);
			log(call, MSG_ASSERT, result[0], AstUtil.getMethodName(call), result[1]);
		}
	}

	private void visitCollectionsCopyOf(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && collectionsCopyOfReplacement(n) != null);
		for (var call : calls) {
			final var prefix = collectionsCopyOfReplacement(call);
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var method = dot.getFirstChild().getNextSibling();
			log(call, MSG_METHOD, prefix + "(...)", "Collections." + method.getText() + "(...)");
		}
	}

	private void visitCollectionsFactory(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && collectionsFactoryReplacement(n) != null);
		for (var call : calls) {
			final var prefix = collectionsFactoryReplacement(call);
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var method = dot.getFirstChild().getNextSibling();
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			final var hasArgs = elist != null && elist.getChildCount() > 0;
			final var argText = hasArgs ? "(...)" : "()";
			log(call, MSG_METHOD, prefix + argText, "Collections." + method.getText() + argText);
		}
	}

	private void visitCollectionsSort(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isCollectionsSortCall(n));
		for (var call : calls)
			log(call, MSG_METHOD, ".sort(...)", "Collections.sort(...)");
	}

	private void visitCollectToList(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isCollectToListCall(n));
		for (var call : calls) {
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			final var argExpr = elist.getFirstChild();
			final var innerCall = argExpr.getType() == TokenTypes.EXPR ? argExpr.getFirstChild() : argExpr;
			final var innerDot = innerCall.findFirstToken(TokenTypes.DOT);
			final var collectorMethod = innerDot.getFirstChild().getNextSibling();
			log(call, MSG_METHOD, ".toList()", ".collect(Collectors." + collectorMethod.getText() + "())");
		}
	}

	private void visitEqualsEmptyString(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isEqualsEmptyString(n));
		for (var call : calls) {
			if (!receiverHasMethodStrict(call, "isEmpty"))
				continue;
			if (minSdk < 35 && receiverIsCharSequenceNotString(call))
				continue;
			log(call, MSG_METHOD, ".isEmpty()", ".equals(\"\")");
		}
	}

	private void visitIndexOfChar(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isIndexOfSingleCharStringCall(n));
		for (var call : calls) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			var methodIdent = dot.getFirstChild();
			while (methodIdent.getNextSibling() != null)
				methodIdent = methodIdent.getNextSibling();
			final var methodName = methodIdent.getText();
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			final var firstArg = elist.getFirstChild();
			final var inner = firstArg.getType() == TokenTypes.EXPR ? firstArg.getFirstChild() : firstArg;
			final var literalText = inner.getText();
			final var charLiteral = stringLiteralToCharLiteral(literalText);
			if (charLiteral == null)
				continue;
			log(call, MSG_METHOD, methodName + "(" + charLiteral + ")", methodName + "(" + literalText + ")");
		}
	}

	private void visitIndexOfContains(@Nonnull DetailAST ast) {
		final var comparisons = AstUtil.collectMatching(ast, n -> indexOfContainsReplacement(n) != null);
		for (var comparison : comparisons) {
			final var indexOfCall = indexOfCallFromComparison(comparison);
			if (!receiverHasMethod(indexOfCall, "contains"))
				continue;

			final var replacement = indexOfContainsReplacement(comparison);
			final var left = comparison.getFirstChild();
			final var right = left.getNextSibling();
			final var op = switch (comparison.getType()) {
				case TokenTypes.EQUAL -> "==";
				case TokenTypes.GE -> ">=";
				case TokenTypes.GT -> ">";
				case TokenTypes.LE -> "<=";
				case TokenTypes.LT -> "<";
				case TokenTypes.NOT_EQUAL -> "!=";
				default -> "?";
			};
			final var indexOfText = ".indexOf(...)";
			final var indexOfOnLeft = isIndexOfStringCall(left);
			final var literalSide = indexOfOnLeft ? right : left;
			final var literalText = isLiteralNegativeOne(literalSide) ? "-1" : childText(literalSide);
			final var actual = indexOfOnLeft
					? indexOfText + " " + op + " " + literalText
					: literalText + " " + op + " " + indexOfText;
			log(comparison, MSG_METHOD, replacement, actual);
		}
	}

	private void visitMapChain(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && mapChainReplacement(n) != null);
		for (var call : calls) {
			final var replacement = mapChainReplacement(call);
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var receiver = dot.getFirstChild();
			final var receiverDot = receiver.findFirstToken(TokenTypes.DOT);
			var receiverMethod = receiverDot.getFirstChild();
			while (receiverMethod.getNextSibling() != null)
				receiverMethod = receiverMethod.getNextSibling();
			log(call, MSG_METHOD, replacement, "." + receiverMethod.getText() + "().contains(...)");
		}
	}

	private void visitMethodScope(@Nonnull DetailAST ast) {
		visitAssertions(ast);
		visitCollectToList(ast);
		visitEqualsEmptyString(ast);
		visitIndexOfChar(ast);
		visitIndexOfContains(ast);
		visitMapChain(ast);
		visitReplaceAllLiteral(ast);
		visitSizeEqualsZero(ast);
		visitStreamCount(ast);
		visitStreamFindFirstIsPresent(ast);

		if (minSdk >= MIN_SDK_FOR_EACH) {
			visitCollectionsSort(ast);
			visitStreamForEach(ast);
		}

		if (minSdk >= MIN_SDK_COLLECTION_FACTORY) {
			visitArraysAsList(ast);
			visitCollectionsFactory(ast);
		}

		if (minSdk >= MIN_SDK_COPY_OF)
			visitCollectionsCopyOf(ast);

		if (minSdk >= MIN_SDK_IS_BLANK)
			visitTrimIsBlank(ast);

		if (minSdk >= MIN_SDK_TO_ARRAY_GENERATOR)
			visitToArrayNewZero(ast);

		if (minSdk >= MIN_SDK_FORMATTED)
			visitStringFormat(ast);

		if (minSdk < MIN_SDK_GET_FIRST_LAST)
			return;

		final var getCalls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isGetCall(n));

		final var zeroGets = new ArrayList<DetailAST>();
		final var lastGets = new ArrayList<DetailAST>();
		final var receiversWithOtherIndices = new HashMap<String, Boolean>();

		for (var call : getCalls) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			if (dot == null)
				continue;

			final var receiver = receiverText(dot);
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			if (elist == null || elist.getChildCount() != 1)
				continue;

			final var arg = elist.getFirstChild();
			if (isLiteralZero(arg))
				zeroGets.add(call);
			else if (isSizeMinusOne(arg, dot))
				lastGets.add(call);
			else
				receiversWithOtherIndices.put(receiver, Boolean.TRUE);
		}

		// flag .get(0) only if receiver doesn't also use .get(N) with other indices
		for (var call : zeroGets) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var receiver = receiverText(dot);
			if (!receiversWithOtherIndices.containsKey(receiver) && receiverHasMethod(call, "getFirst"))
				log(call, MSG_METHOD, ".getFirst()", ".get(0)");
		}

		// flag .get(size() - 1) only if receiver doesn't also use .get(N) with other indices and has getLast()
		for (var call : lastGets) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var receiver = receiverText(dot);
			if (!receiversWithOtherIndices.containsKey(receiver) && receiverHasMethod(call, "getLast"))
				log(call, MSG_METHOD, ".getLast()", ".get(size() - 1)");
		}

		// same logic for .remove(0) -> .removeFirst(), .remove(size()-1) -> .removeLast()
		final var removeCalls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isRemoveCall(n));
		if (removeCalls.isEmpty())
			return;

		final var zeroRemoves = new ArrayList<DetailAST>();
		final var lastRemoves = new ArrayList<DetailAST>();
		final var removeReceiversWithOtherIndices = new HashMap<String, Boolean>();

		for (var call : removeCalls) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			if (dot == null)
				continue;

			final var receiver = receiverText(dot);
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			if (elist == null || elist.getChildCount() != 1)
				continue;

			final var arg = elist.getFirstChild();
			if (isLiteralZero(arg))
				zeroRemoves.add(call);
			else if (isSizeMinusOne(arg, dot))
				lastRemoves.add(call);
			else
				removeReceiversWithOtherIndices.put(receiver, Boolean.TRUE);
		}

		for (var call : zeroRemoves) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var receiver = receiverText(dot);
			if (!removeReceiversWithOtherIndices.containsKey(receiver) && receiverHasMethod(call, "removeFirst"))
				log(call, MSG_METHOD, ".removeFirst()", ".remove(0)");
		}

		for (var call : lastRemoves) {
			final var dot = call.findFirstToken(TokenTypes.DOT);
			final var receiver = receiverText(dot);
			if (!removeReceiversWithOtherIndices.containsKey(receiver) && receiverHasMethod(call, "removeLast"))
				log(call, MSG_METHOD, ".removeLast()", ".remove(size() - 1)");
		}
	}

	private void visitReplaceAllLiteral(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isReplaceAllWithLiteral(n));
		for (var call : calls)
			log(call, MSG_METHOD, ".replace(...)", ".replaceAll(...)");
	}

	private void visitSizeEqualsZero(@Nonnull DetailAST ast) {
		final var comparisons = AstUtil.collectMatching(ast, n -> isEmptyReplacement(n) != null);
		for (var comparison : comparisons) {
			final var sizeCall = sizeCallFromComparison(comparison);
			if (sizeCall == null || !receiverHasMethodStrict(sizeCall, "isEmpty"))
				continue;

			if (minSdk < 35 && receiverIsCharSequenceNotString(sizeCall))
				continue;

			final var replacement = isEmptyReplacement(comparison);
			final var dot = sizeCall.findFirstToken(TokenTypes.DOT);
			if (dot == null)
				continue;
			var methodName = dot.getFirstChild();
			while (methodName.getNextSibling() != null)
				methodName = methodName.getNextSibling();

			// build the actual comparison text, e.g. ".size() > 0" or "0 < .size()"
			final var left = comparison.getFirstChild();
			final var right = left.getNextSibling();
			final var op = switch (comparison.getType()) {
				case TokenTypes.EQUAL -> "==";
				case TokenTypes.GE -> ">=";
				case TokenTypes.GT -> ">";
				case TokenTypes.LE -> "<=";
				case TokenTypes.LT -> "<";
				case TokenTypes.NOT_EQUAL -> "!=";
				default -> "?";
			};
			final var sizeText = "." + methodName.getText() + "()";
			final var actual = isSizeCall(left)
					? sizeText + " " + op + " " + childText(right)
					: childText(left) + " " + op + " " + sizeText;
			log(comparison, MSG_METHOD, "." + replacement, actual);
		}
	}

	private void visitStreamCount(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isStreamTerminalCall(n, "count"));
		for (var call : calls)
			log(call, MSG_METHOD, ".size()", ".stream().count()");
	}

	private void visitStreamFindFirstIsPresent(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isStreamFindFirstIsPresentCall(n));
		for (var call : calls)
			log(call, MSG_METHOD, "!.isEmpty()", ".stream().findFirst().isPresent()");
	}

	private void visitStreamForEach(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isStreamForEachCall(n));
		for (var call : calls)
			log(call, MSG_METHOD, ".forEach(...)", ".stream().forEach(...)");
	}

	private void visitStringFormat(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && isStringFormatCall(n));
		for (var call : calls) {
			final var elist = call.findFirstToken(TokenTypes.ELIST);
			var argCount = 0;
			for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.COMMA)
					++argCount;
			}
			if (argCount == 1)
				log(call, MSG_METHOD, "the value directly", "String.format(value)");
			else
				log(call, MSG_METHOD, ".formatted(...)", "String.format(...)");
		}
	}

	private void visitToArrayNewZero(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> n.getType() == TokenTypes.METHOD_CALL && toArrayNewZeroType(n) != null);
		for (var call : calls) {
			final var typeName = toArrayNewZeroType(call);
			log(call, MSG_METHOD, typeName + "[]::new", "new " + typeName + "[0]");
		}
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
			default -> visitMethodScope(ast);
		}
	}

	private void visitTrimIsBlank(@Nonnull DetailAST ast) {
		final var calls = AstUtil.collectMatching(ast, n -> (n.getType() == TokenTypes.METHOD_CALL && isTrimIsEmptyCall(n)) || trimLengthZeroReplacement(n) != null);
		for (var node : calls) {
			final var methodName = trimOrStripName(node);
			if (node.getType() == TokenTypes.METHOD_CALL)
				log(node, MSG_METHOD, ".isBlank()", "." + methodName + "().isEmpty()");
			else {
				final var replacement = trimLengthZeroReplacement(node);
				final var left = node.getFirstChild();
				final var right = left.getNextSibling();
				final var op = switch (node.getType()) {
					case TokenTypes.EQUAL -> "==";
					case TokenTypes.GE -> ">=";
					case TokenTypes.GT -> ">";
					case TokenTypes.LE -> "<=";
					case TokenTypes.LT -> "<";
					case TokenTypes.NOT_EQUAL -> "!=";
					default -> "?";
				};
				final var trimLengthOnLeft = isTrimLengthCall(left);
				final var literalSide = trimLengthOnLeft ? right : left;
				final var literalText = childText(literalSide);
				final var lengthSuffix = "." + methodName + "().length()";
				final var actual = trimLengthOnLeft
						? lengthSuffix + " " + op + " " + literalText
						: literalText + " " + op + " " + lengthSuffix;
				log(node, MSG_METHOD, replacement, actual);
			}
		}
	}
}