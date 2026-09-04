package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags JIT-unfriendly patterns and unnecessary
 * allocations detectable via AST. Currently detects:
 * <ul>
 *     <li>Empty-string concatenation ({@code "" + x}) -> {@code String.valueOf(x)}</li>
 *     <li>{@code new String(literal)} / {@code new String(stringVar)} -> use the value directly</li>
 *     <li>{@code new StringBuffer()} (local) -> {@code new StringBuilder()}</li>
 *     <li>Boxed primitive constructors ({@code new Integer(42)}) -> {@code valueOf(...)} / {@code Boolean.TRUE}</li>
 *     <li>{@code .toArray(new T[size])} where {@code size != 0} -> {@code new T[0]}</li>
 *     <li>String concatenation inside {@code StringBuilder.append(...)} -> chained {@code .append()}</li>
 *     <li>String {@code +=} inside a loop -> use {@code StringBuilder}</li>
 *     <li>{@code .matches(...)} / {@code .replaceAll(...)} / {@code .split(...)} inside a loop -> hoist {@code Pattern.compile(...)}</li>
 *     <li>{@code Map.keySet()} + {@code .get(key)} inside for-each -> iterate {@code .entrySet()}</li>
 *     <li>{@code Enum.values()} inside a loop -> cache to a static final array</li>
 *     <li>Double-brace initialization -> use {@code List.of(...)} or constructor</li>
 *     <li>Repeated reusable-object creation ({@code Pattern.compile}, {@code DateTimeFormatter.ofPattern}, {@code new SimpleDateFormat}, etc.) inside a method body -> hoist to a static final field</li>
 *     <li>Boxed numeric accumulator modified inside a loop -> use the primitive type</li>
 *     <li>Explicit iterator {@code while (it.hasNext())} loop -> enhanced {@code for}</li>
 * </ul>
 */
public class JitInefficiencyCheck extends AbstractAstCheck {
	/** What {@link #visitToken} logs, plus the two payload slots {@link JitTarget} carries forward. */
	private record Detection(
			@Nonnull JitInefficiencyCategory category,
			@Nullable String replacement,
			@Nullable DetailAST argument,
			@Nonnull Object... logArgs
	) {}

	private record ScopedNode(@Nonnull DetailAST node, boolean inLoop) {}

	/**
	 * The inefficiency the check reported at a position, the node it reported on, and whatever that
	 * category's rewrite needs beyond the node itself.
	 *
	 * <p>For the three {@code LITERAL_NEW} categories {@code replacement} is the {@code java.lang}
	 * class the {@code new} resolves to, and null when it resolves to anything else.
	 *
	 * @see #locateAt
	 */
	public record JitTarget(
			@Nonnull JitInefficiencyCategory category,
			@Nonnull DetailAST node,
			@Nullable String replacement,
			@Nullable DetailAST argument
	) {}

	private static final Set<String> BOXED_NUMERIC_TYPES = Set.of(
			"Byte", "Double", "Float", "Integer", "Long", "Short"
	);
	public static final Set<String> BOXED_PRIMITIVE_TYPES = Set.of(
			"Boolean", "Byte", "Character", "Double", "Float", "Integer", "Long", "Short"
	);
	private static final Set<String> COLLECTION_OR_MAP_TYPES = Set.of(
			"AbstractList", "AbstractMap", "AbstractSet",
			"ArrayList", "Collection", "EnumMap", "EnumSet", "HashMap", "HashSet",
			"LinkedHashMap", "LinkedHashSet", "LinkedList", "List", "Map",
			"Set", "TreeMap", "TreeSet", "Vector"
	);
	private static final Set<String> REGEX_STRING_METHODS = Set.of(
			"matches", "replaceAll", "split"
	);
	private static final Set<String> REUSABLE_FACTORY_NEW_TYPES = Set.of(
			"DecimalFormat", "Gson", "ObjectMapper", "SimpleDateFormat"
	);

	@CheckReturnValue
	private static boolean accumulatesInsideALoop(@Nonnull DetailAST scope, @Nonnull String varName) {
		final var stack = new ArrayDeque<ScopedNode>();
		stack.push(new ScopedNode(scope, false));
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			final var node = current.node();
			final var type = node.getType();
			final var nowInLoop = current.inLoop() || type == TokenTypes.LITERAL_FOR
					|| type == TokenTypes.LITERAL_WHILE || type == TokenTypes.LITERAL_DO;
			if (nowInLoop && accumulatesInto(node, varName))
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(new ScopedNode(child, nowInLoop));
		}
		return false;
	}

	/**
	 * Whether {@code node} updates {@code varName} <em>from its own value</em>: a compound
	 * assignment, or a plain one whose right-hand side reads it.
	 *
	 * <p>Not "any write". A plain {@code total = 5} is excluded deliberately, because what makes a
	 * boxed accumulator worth reporting is the repeated unbox-add-rebox, and an assignment that
	 * ignores the previous value does none of that.
	 */
	@CheckReturnValue
	private static boolean accumulatesInto(@Nonnull DetailAST node, @Nonnull String varName) {
		final var type = node.getType();
		final var compound = type == TokenTypes.PLUS_ASSIGN || type == TokenTypes.MINUS_ASSIGN
				|| type == TokenTypes.STAR_ASSIGN || type == TokenTypes.DIV_ASSIGN
				|| type == TokenTypes.MOD_ASSIGN;
		if (!compound && type != TokenTypes.ASSIGN)
			return false;
		final var lhs = node.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.IDENT || !varName.equals(lhs.getText()))
			return false;
		return compound || isAssignReadingSelf(node, varName);
	}

	@CheckReturnValue
	private static boolean ancestorIsLoop(@Nonnull DetailAST ast) {
		var prev = ast;
		for (var parent = ast.getParent(); parent != null; parent = parent.getParent()) {
			final var type = parent.getType();
			if (type == TokenTypes.LITERAL_FOR) {
				// body is the child after RPAREN; exclude FOR_INIT and the
				// FOR_EACH_CLAUSE iterable (both run once, not per iteration).
				final var rparen = parent.findFirstToken(TokenTypes.RPAREN);
				if (rparen != null && rparen.getNextSibling() == prev)
					return true;
				// FOR_CONDITION and FOR_ITERATOR run each iteration
				final var prevType = prev.getType();
				if (prevType == TokenTypes.FOR_CONDITION || prevType == TokenTypes.FOR_ITERATOR)
					return true;
			}
			else if (type == TokenTypes.LITERAL_WHILE || type == TokenTypes.LITERAL_DO)
				return true;
			if (type == TokenTypes.METHOD_DEF || type == TokenTypes.CTOR_DEF
					|| type == TokenTypes.LAMBDA || type == TokenTypes.OBJBLOCK)
				return false;
			prev = parent;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean ancestorIsMethodBody(@Nonnull DetailAST ast) {
		for (var parent = ast.getParent(); parent != null; parent = parent.getParent()) {
			final var type = parent.getType();
			if (type == TokenTypes.METHOD_DEF || type == TokenTypes.CTOR_DEF)
				return true;
			if (type == TokenTypes.STATIC_INIT || type == TokenTypes.INSTANCE_INIT)
				return false;
			if (type == TokenTypes.VARIABLE_DEF) {
				final var grand = parent.getParent();
				if (grand != null && grand.getType() == TokenTypes.OBJBLOCK)
					return false;
			}
		}
		return false;
	}

	@CheckReturnValue
	private static boolean bodyHasMapGet(@Nonnull DetailAST body, @Nonnull String mapVar, @Nonnull String loopVar) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(body);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_CALL && isMapGetOfLoopVar(node, mapVar, loopVar))
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	/**
	 * The inefficiency category of the node the check would log at {@code (line, column)}, which is
	 * {@link #locateAt}'s answer with the rewrite payload dropped.
	 */
	@CheckReturnValue
	@Nullable
	public static JitInefficiencyCategory categorizeAt(@Nonnull DetailAST root, int line, int column) {
		final var target = locateAt(root, line, column);
		return target == null ? null : target.category();
	}

	/** The {@code IDENT} or {@code DOT} naming the class a {@code new} instantiates. */
	@CheckReturnValue
	@Nullable
	private static DetailAST classNameNode(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT || child.getType() == TokenTypes.DOT)
				return child;
		}
		return null;
	}

	/**
	 * Whether {@code String.valueOf(operand)} produces what {@code "" + operand} produces. Two
	 * operands diverge, and both spellings compile either way, so neither shows up as a build
	 * failure:
	 *
	 * <ul>
	 *     <li>{@code null}, where the concatenation is the text {@code "null"} but the call binds
	 *     to {@code valueOf(char[])} and throws a {@code NullPointerException} at run time;</li>
	 *     <li>a {@code char[]}, where the concatenation is the array's {@code toString()}
	 *     ({@code [C@1b6d}) but the call returns its characters.</li>
	 * </ul>
	 *
	 * <p>An operand this file's scope cannot type at all is reported equivalent, so the rewrite
	 * still happens for, say, a call into a class the classpath does not carry. The check reports
	 * the concatenation either way; this says only whether the one rewrite preserves it.
	 */
	@CheckReturnValue
	public static boolean concatenationSurvivesValueOf(@Nonnull DetailAST operand) {
		if (operand.getType() == TokenTypes.LITERAL_NULL)
			return false;

		final var type = operandTypeName(operand);
		// reflection spells an array return in JVM form, so one type arrives under two names
		return !"char[]".equals(type) && !"[C".equals(type);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectAppendConcat(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var method = dot.getLastChild();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !"append".equals(method.getText()))
			return null;
		final var arg = singleArgInner(methodCall);
		if (arg == null || arg.getType() != TokenTypes.PLUS)
			return null;
		if (!isStringConcat(arg))
			return null;
		if (!isBuilderReceiver(dot.getFirstChild()))
			return null;
		return new Detection(JitInefficiencyCategory.APPEND_CONCAT, null, arg);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectAssignPlusStringInLoop(@Nonnull DetailAST assign) {
		final var lhs = assign.getFirstChild();
		if (lhs == null)
			return null;
		if (!isAssignableLhsShape(lhs))
			return null;
		final var rhs = lhs.getNextSibling();
		if (rhs == null || rhs.getType() != TokenTypes.PLUS)
			return null;
		if (!plusChainContainsBareLhs(rhs, lhs))
			return null;
		final var typeName = resolveLhsType(lhs);
		if (!isStringTypeName(typeName))
			return null;
		if (!ancestorIsLoop(assign))
			return null;
		return new Detection(JitInefficiencyCategory.STRING_CONCAT_IN_LOOP, null, lhs);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectBoxedAccumulator(@Nonnull DetailAST variableDef) {
		final var parent = variableDef.getParent();
		if (parent == null)
			return null;
		final var modifiers = variableDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null && modifiers.findFirstToken(TokenTypes.FINAL) != null)
			return null;
		final var typeName = typeNameForVariableDef(variableDef);
		if (typeName == null || !BOXED_NUMERIC_TYPES.contains(typeName))
			return null;
		final var ident = variableDef.findFirstToken(TokenTypes.IDENT);
		if (ident == null)
			return null;
		final var varName = ident.getText();
		var scope = parent;
		while (scope != null && scope.getType() != TokenTypes.METHOD_DEF
				&& scope.getType() != TokenTypes.CTOR_DEF)
			scope = scope.getParent();
		if (scope == null)
			return null;
		if (accumulatesInsideALoop(scope, varName))
			return new Detection(JitInefficiencyCategory.BOXED_ACCUMULATOR, null, null, varName, typeName);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectDoubleBrace(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!COLLECTION_OR_MAP_TYPES.contains(className))
			return null;
		final var objBlock = literalNew.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.INSTANCE_INIT)
				return new Detection(JitInefficiencyCategory.DOUBLE_BRACE, null, null);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectEnumValuesInLoop(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var first = dot.getFirstChild();
		if (first == null || first.getType() != TokenTypes.IDENT)
			return null;
		final var second = first.getNextSibling();
		if (second == null || second.getType() != TokenTypes.IDENT
				|| !"values".equals(second.getText()))
			return null;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 0)
			return null;
		final var receiverName = first.getText();
		if (receiverName.isEmpty() || !Character.isUpperCase(receiverName.charAt(0)))
			return null;
		if (!ancestorIsLoop(methodCall))
			return null;
		return new Detection(JitInefficiencyCategory.ENUM_VALUES_IN_LOOP, null, null, receiverName);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectFor(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.ASSIGN -> detectAssignPlusStringInLoop(ast);
			case TokenTypes.LITERAL_FOR -> detectForEachKeySetGet(ast);
			case TokenTypes.LITERAL_NEW -> detectLiteralNew(ast);
			case TokenTypes.LITERAL_WHILE -> detectIteratorWhile(ast);
			case TokenTypes.METHOD_CALL -> detectMethodCall(ast);
			case TokenTypes.PLUS -> detectPlusForEmptyStringConcat(ast);
			case TokenTypes.PLUS_ASSIGN -> detectPlusAssignStringInLoop(ast);
			case TokenTypes.VARIABLE_DEF -> detectBoxedAccumulator(ast);
			default -> null;
		};
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectForEachKeySetGet(@Nonnull DetailAST literalFor) {
		final var foreach = literalFor.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
		if (foreach == null)
			return null;
		final var iterableExpr = foreach.findFirstToken(TokenTypes.EXPR);
		if (iterableExpr == null)
			return null;
		final var iterableInner = iterableExpr.getFirstChild();
		if (iterableInner == null || iterableInner.getType() != TokenTypes.METHOD_CALL)
			return null;
		final var dot = iterableInner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return null;
		final var method = receiver.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !"keySet".equals(method.getText()))
			return null;
		final var mapVar = receiver.getText();
		final var loopVarDef = foreach.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (loopVarDef == null)
			return null;
		final var loopVarIdent = loopVarDef.findFirstToken(TokenTypes.IDENT);
		if (loopVarIdent == null)
			return null;
		final var loopVar = loopVarIdent.getText();
		final var rparen = literalFor.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen != null ? rparen.getNextSibling() : null;
		if (body != null && bodyHasMapGet(body, mapVar, loopVar))
			return new Detection(JitInefficiencyCategory.MAP_KEYSET_GET, null, null);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectIteratorWhile(@Nonnull DetailAST literalWhile) {
		final var cond = literalWhile.findFirstToken(TokenTypes.EXPR);
		if (cond == null)
			return null;
		final var inner = cond.getFirstChild();
		if (inner == null || inner.getType() != TokenTypes.METHOD_CALL)
			return null;
		final var dot = inner.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return null;
		final var method = receiver.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !"hasNext".equals(method.getText()))
			return null;
		final var elist = inner.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 0)
			return null;
		final var iterName = receiver.getText();
		final var rparen = literalWhile.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen != null ? rparen.getNextSibling() : null;
		if (body == null)
			return null;
		if (iteratorRefsAreNextOnly(body, iterName))
			return new Detection(JitInefficiencyCategory.ITERATOR_LOOP, null, null);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectLiteralNew(@Nonnull DetailAST literalNew) {
		final var className = literalNewClassName(literalNew);
		if (className == null)
			return null;
		if (BOXED_PRIMITIVE_TYPES.contains(className)) {
			final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
			if (elist != null && AstQuery.countArguments(elist) == 1) {
				return new Detection(
						JitInefficiencyCategory.BOXED_CONSTRUCTOR,
						jdkSimpleTypeName(literalNew),
						findArgInner(elist, 0),
						className
				);
			}
		}
		final var newString = detectNewString(literalNew);
		if (newString != null)
			return newString;
		final var stringBuffer = detectStringBuffer(literalNew, className);
		if (stringBuffer != null)
			return stringBuffer;
		final var doubleBrace = detectDoubleBrace(literalNew, className);
		if (doubleBrace != null)
			return doubleBrace;
		return detectNewLiteralForReusableFactory(literalNew, className);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectMethodCall(@Nonnull DetailAST methodCall) {
		final var toArray = detectToArraySized(methodCall);
		if (toArray != null)
			return toArray;
		final var reusable = detectReusableFactoryCall(methodCall);
		if (reusable != null)
			return reusable;
		final var regex = detectStringRegexCallInLoop(methodCall);
		if (regex != null)
			return regex;
		final var enumValues = detectEnumValuesInLoop(methodCall);
		if (enumValues != null)
			return enumValues;
		return detectAppendConcat(methodCall);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectNewLiteralForReusableFactory(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!REUSABLE_FACTORY_NEW_TYPES.contains(className))
			return null;
		final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) == 0)
			return null;
		final var firstArg = findArgInner(elist, 0);
		if (firstArg == null || firstArg.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsMethodBody(literalNew))
			return null;
		return new Detection(JitInefficiencyCategory.REUSABLE_OBJECT, null, null, "new " + className + "(...)");
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectNewString(@Nonnull DetailAST literalNew) {
		final var className = literalNewClassName(literalNew);
		if (!"String".equals(className))
			return null;
		final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 1)
			return null;
		final var arg = findArgInner(elist, 0);
		if (arg == null)
			return null;
		final var jdkName = jdkSimpleTypeName(literalNew);
		if (arg.getType() == TokenTypes.STRING_LITERAL)
			return new Detection(JitInefficiencyCategory.NEW_STRING, jdkName, arg, "string literal");
		if (arg.getType() == TokenTypes.IDENT) {
			final var typeName = AstResolve.resolveVariableType(literalNew, arg.getText());
			if (isStringTypeName(typeName))
				return new Detection(JitInefficiencyCategory.NEW_STRING, jdkName, arg, "String variable");
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectPlusAssignStringInLoop(@Nonnull DetailAST plusAssign) {
		final var lhs = plusAssign.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.IDENT)
			return null;
		final var typeName = AstResolve.resolveVariableType(plusAssign, lhs.getText());
		if (!isStringTypeName(typeName))
			return null;
		if (!ancestorIsLoop(plusAssign))
			return null;
		return new Detection(JitInefficiencyCategory.STRING_CONCAT_IN_LOOP, null, lhs);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectPlusForEmptyStringConcat(@Nonnull DetailAST plus) {
		final var left = plus.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		// a parenthesized left operand puts an LPAREN in the first child slot, so neither side reads
		// as the empty literal and the check stays silent on `(a + b) + ""` altogether
		if (isEmptyStringLiteral(left))
			return new Detection(JitInefficiencyCategory.EMPTY_STRING_CONCAT, null, AstQuery.unwrapParensAndExpr(right));
		if (isEmptyStringLiteral(right))
			return new Detection(JitInefficiencyCategory.EMPTY_STRING_CONCAT, null, AstQuery.unwrapParensAndExpr(left));
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectReusableFactoryCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT)
			return null;
		final var method = receiver.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT)
			return null;
		final var receiverName = receiver.getText();
		final var methodName = method.getText();
		final var matches = ("Pattern".equals(receiverName) && "compile".equals(methodName))
				|| ("DateTimeFormatter".equals(receiverName) && "ofPattern".equals(methodName));
		if (!matches)
			return null;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) == 0)
			return null;
		final var firstArg = findArgInner(elist, 0);
		if (firstArg == null || firstArg.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsMethodBody(methodCall))
			return null;
		return new Detection(
				JitInefficiencyCategory.REUSABLE_OBJECT, null, null, receiverName + "." + methodName + "(...)"
		);
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectStringBuffer(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!"StringBuffer".equals(className))
			return null;
		for (var parent = literalNew.getParent(); parent != null; parent = parent.getParent()) {
			final var t = parent.getType();
			if (t == TokenTypes.VARIABLE_DEF) {
				final var grand = parent.getParent();
				if (grand != null && grand.getType() == TokenTypes.SLIST) {
					return new Detection(
							JitInefficiencyCategory.STRING_BUFFER,
							jdkSimpleTypeName(literalNew),
							classNameNode(literalNew)
					);
				}
				return null;
			}
			if (t == TokenTypes.SLIST || t == TokenTypes.OBJBLOCK
					|| t == TokenTypes.METHOD_DEF || t == TokenTypes.CTOR_DEF
					|| t == TokenTypes.LAMBDA)
				return null;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectStringRegexCallInLoop(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var receiver = dot.getFirstChild();
		if (receiver == null)
			return null;
		final var method = receiver.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !REGEX_STRING_METHODS.contains(method.getText()))
			return null;
		if (receiver.getType() == TokenTypes.IDENT) {
			final var receiverName = receiver.getText();
			if (receiverName.isEmpty() || Character.isUpperCase(receiverName.charAt(0)))
				return null;
			final var receiverType = AstResolve.resolveVariableType(methodCall, receiverName);
			if (receiverType != null && !isStringTypeName(receiverType))
				return null;
		}
		else if (receiver.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsLoop(methodCall))
			return null;
		return new Detection(JitInefficiencyCategory.STRING_REGEX_IN_LOOP, null, null, method.getText());
	}

	@CheckReturnValue
	@Nullable
	private static Detection detectToArraySized(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var method = dot.getLastChild();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !"toArray".equals(method.getText()))
			return null;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 1)
			return null;
		final var arg = findArgInner(elist, 0);
		if (arg == null || arg.getType() != TokenTypes.LITERAL_NEW)
			return null;
		// skip multi-dimensional arrays
		var arrayDeclCount = 0;
		for (var c = arg.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ARRAY_DECLARATOR)
				++arrayDeclCount;
		}
		if (arrayDeclCount != 1)
			return null;
		final var arrayDecl = arg.findFirstToken(TokenTypes.ARRAY_DECLARATOR);
		if (arrayDecl == null)
			return null;
		for (var c = arrayDecl.getFirstChild(); c != null; c = c.getNextSibling()) {
			if (c.getType() == TokenTypes.ARRAY_DECLARATOR)
				return null;
		}
		final var sizeExpr = arrayDecl.findFirstToken(TokenTypes.EXPR);
		if (sizeExpr == null)
			return null;
		final var sizeInner = sizeExpr.getFirstChild();
		if (sizeInner == null)
			return null;
		if (AstQuery.isZeroLiteral(sizeInner))
			return null;
		// a qualified type nests its segments under a DOT, so the direct-child IDENT
		// lookup finds nothing and the message would render the placeholder
		final var typeIdent = arg.findFirstToken(TokenTypes.IDENT);
		final var qualified = arg.findFirstToken(TokenTypes.DOT);
		final String typeName;
		if (typeIdent != null)
			typeName = typeIdent.getText();
		else if (qualified != null)
			typeName = AstText.dottedName(qualified);
		else
			typeName = "?";
		return new Detection(JitInefficiencyCategory.TOARRAY_SIZED, null, sizeInner, typeName);
	}

	/**
	 * The name of the type {@code node}'s innermost enclosing class extends, or null when it
	 * extends nothing nameable here.
	 *
	 * <p>An anonymous class ends the ascent even though it has no {@code CLASS_DEF}: the type it
	 * extends is the one its {@code new} names. Walking past it would read the lexically enclosing
	 * class's supertype instead, and the two are unrelated, so a {@code super.f} inside the
	 * anonymous body would be typed from a class it does not inherit from.
	 */
	@CheckReturnValue
	@Nullable
	private static String enclosingSupertypeName(@Nonnull DetailAST node) {
		for (var scope = node.getParent(); scope != null; scope = scope.getParent()) {
			final var type = scope.getType();
			final var owner = type == TokenTypes.OBJBLOCK ? scope.getParent() : null;
			if (owner != null) {
				if (owner.getType() == TokenTypes.LITERAL_NEW)
					return literalNewClassName(owner);
			}
			// only a class names a supertype this can read
			if (type == TokenTypes.ANNOTATION_DEF || type == TokenTypes.ENUM_DEF
					|| type == TokenTypes.INTERFACE_DEF || type == TokenTypes.RECORD_DEF)
				return null;
			if (type != TokenTypes.CLASS_DEF)
				continue;

			final var extendsClause = scope.findFirstToken(TokenTypes.EXTENDS_CLAUSE);
			final var superType = extendsClause == null ? null : extendsClause.getFirstChild();
			return superType == null ? null : AstText.typeName(superType);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findArgInner(@Nonnull DetailAST elist, int index) {
		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;
			if (i++ == index)
				return child.getType() == TokenTypes.EXPR ? child.getFirstChild() : child;
		}
		return null;
	}

	/**
	 * Returns true if {@code lhs} is a valid assignment target shape for the
	 * concat-in-loop check: a bare IDENT, a DOT chain ending in an IDENT
	 * (including {@code this.f}, {@code obj.f}, {@code this.a.b}, etc.), or
	 * an array element {@code arr[i]} / {@code this.arr[i]} / chained
	 * {@code arr[i][j]} where the array receiver is itself a valid
	 * IDENT/DOT shape.
	 */
	@CheckReturnValue
	private static boolean isAssignableLhsShape(@Nonnull DetailAST lhs) {
		var node = lhs;
		// For `arr[i][j]`, the AST is INDEX_OP(INDEX_OP(arr, i), j).
		while (node.getType() == TokenTypes.INDEX_OP) {
			final var array = node.getFirstChild();
			if (array == null)
				return false;
			node = array;
		}
		return isReceiverChainShape(node);
	}

	@CheckReturnValue
	private static boolean isAssignReadingSelf(@Nonnull DetailAST assign, @Nonnull String varName) {
		final var first = assign.getFirstChild();
		if (first == null)
			return false;
		final var rhs = first.getNextSibling();
		return rhs != null && mentionsIdent(rhs, varName);
	}

	/**
	 * Whether {@code receiver} is provably a {@code StringBuilder} or {@code StringBuffer}.
	 *
	 * <p>Affirmative rather than "not known to be something else". The earlier form resolved only
	 * a bare {@code IDENT} and let every other shape through, so a {@code void append(String)} on
	 * a user class was reported and rewritten to {@code out.append(a).append(b)}, which fails with
	 * "void cannot be dereferenced". A chained receiver recurses through its own {@code append},
	 * because that is what the builder's own method returns.
	 */
	@CheckReturnValue
	private static boolean isBuilderReceiver(@Nullable DetailAST receiver) {
		final var node = AstQuery.unwrapParensAndExpr(receiver);
		if (node == null)
			return false;
		return switch (node.getType()) {
			case TokenTypes.DOT -> isBuilderTypeName(resolveReceiverChainType(node));
			case TokenTypes.IDENT -> isBuilderTypeName(AstResolve.resolveVariableType(node, node.getText()));
			case TokenTypes.LITERAL_NEW -> isBuilderTypeName(literalNewClassName(node));
			case TokenTypes.METHOD_CALL -> {
				final var chained = node.findFirstToken(TokenTypes.DOT);
				final var name = chained == null ? null : chained.getLastChild();
				yield name != null && "append".equals(name.getText())
						&& isBuilderReceiver(chained.getFirstChild());
			}
			case TokenTypes.TYPECAST -> isBuilderTypeName(AstText.typeText(node.findFirstToken(TokenTypes.TYPE)));
			default -> false;
		};
	}

	@CheckReturnValue
	private static boolean isBuilderTypeName(@Nullable String typeName) {
		return "StringBuffer".equals(typeName) || "StringBuilder".equals(typeName);
	}

	@CheckReturnValue
	private static boolean isEmptyStringLiteral(@Nullable DetailAST node) {
		if (node == null)
			return false;
		final var inner = node.getType() == TokenTypes.EXPR ? node.getFirstChild() : node;
		return inner != null && inner.getType() == TokenTypes.STRING_LITERAL
				&& "\"\"".equals(inner.getText());
	}

	@CheckReturnValue
	private static boolean isMapGetOfLoopVar(@Nonnull DetailAST methodCall, @Nonnull String mapVar, @Nonnull String loopVar) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		final var receiver = dot == null ? null : dot.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.IDENT || !mapVar.equals(receiver.getText()))
			return false;
		final var methodIdent = receiver.getNextSibling();
		if (methodIdent == null || methodIdent.getType() != TokenTypes.IDENT
				|| !"get".equals(methodIdent.getText()))
			return false;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 1)
			return false;
		final var arg = findArgInner(elist, 0);
		return arg != null && arg.getType() == TokenTypes.IDENT && loopVar.equals(arg.getText());
	}

	@CheckReturnValue
	private static boolean isNextOrHasNextCall(@Nonnull DetailAST ident) {
		final var parent = ident.getParent();
		if (parent == null || parent.getType() != TokenTypes.DOT)
			return false;
		final var method = ident.getNextSibling();
		if (method == null || method.getType() != TokenTypes.IDENT)
			return false;
		return "hasNext".equals(method.getText()) || "next".equals(method.getText());
	}

	@CheckReturnValue
	private static boolean isReceiverChainShape(@Nonnull DetailAST node) {
		var n = node;
		while (n.getType() == TokenTypes.DOT) {
			final var first = n.getFirstChild();
			final var last = first != null ? first.getNextSibling() : null;
			if (last == null || last.getType() != TokenTypes.IDENT || last.getNextSibling() != null)
				return false;
			n = first;
		}
		return n.getType() == TokenTypes.IDENT || n.getType() == TokenTypes.LITERAL_THIS;
	}

	@CheckReturnValue
	private static boolean isStringConcat(@Nonnull DetailAST plus) {
		final var left = plus.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		return operandIsStringTyped(left) || operandIsStringTyped(right);
	}

	@CheckReturnValue
	private static boolean isStringTypeName(@Nullable String typeName) {
		return "String".equals(typeName) || "java.lang.String".equals(typeName);
	}

	@CheckReturnValue
	private static boolean iteratorRefsAreNextOnly(@Nonnull DetailAST body, @Nonnull String iterName) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(body);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.IDENT && iterName.equals(node.getText())
					&& !isNextOrHasNextCall(node))
				return false;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return true;
	}

	/**
	 * The {@code java.lang} class {@code literalNew} instantiates, or null when its spelling names
	 * anything else. An unqualified name is taken at face value, as the language does: a nested
	 * {@code String} in scope would shadow it, and the check's own detectors already resolve names
	 * no further than this.
	 */
	@CheckReturnValue
	@Nullable
	private static String jdkSimpleTypeName(@Nonnull DetailAST literalNew) {
		final var name = classNameNode(literalNew);
		if (name == null)
			return null;
		if (name.getType() == TokenTypes.IDENT)
			return name.getText();
		final var qualified = AstText.dottedName(name);
		final var prefix = "java.lang.";
		if (qualified == null || !qualified.startsWith(prefix))
			return null;
		final var simple = qualified.substring(prefix.length());
		// `java.lang.invoke.MethodHandle` also starts with the prefix, and its simple name is not
		// what survives the dot
		return simple.indexOf('.') < 0 ? simple : null;
	}

	@CheckReturnValue
	@Nullable
	private static String literalNewClassName(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT)
				return child.getText();
			if (child.getType() == TokenTypes.DOT) {
				// for a qualified name like `java.util.ArrayList`, the last IDENT in the
				// DOT's children sequence is the class name (TYPE_ARGUMENTS may follow).
				String last = null;
				for (var c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
					if (c.getType() == TokenTypes.IDENT)
						last = c.getText();
				}
				return last;
			}
		}
		return null;
	}

	/**
	 * Walks the parsed tree and returns what the check would log at {@code (line, column)} (0-based
	 * line index, 0-based code-point column, matching {@code AbstractCheck.log}'s
	 * {@code getLineNo()}/{@code getColumnNo()}), together with the node it would log on.
	 */
	@CheckReturnValue
	@Nullable
	public static JitTarget locateAt(@Nonnull DetailAST root, int line, int column) {
		final var node = AstQuery.findNodeAt(root, line, column, n -> detectFor(n) != null);
		if (node == null)
			return null;
		final var detection = detectFor(node);
		return detection == null
				? null
				: new JitTarget(detection.category(), node, detection.replacement(), detection.argument());
	}

	@CheckReturnValue
	private static boolean mentionsIdent(@Nonnull DetailAST root, @Nonnull String name) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.IDENT && name.equals(node.getText()))
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return false;
	}

	@CheckReturnValue
	private static boolean operandIsStringTyped(@Nullable DetailAST node) {
		if (node == null)
			return false;
		if (node.getType() == TokenTypes.STRING_LITERAL)
			return true;
		if (node.getType() == TokenTypes.PLUS)
			return isStringConcat(node);
		return node.getType() == TokenTypes.IDENT
				&& isStringTypeName(AstResolve.resolveVariableType(node, node.getText()));
	}

	/** The type of a concatenation operand, or null when this file's scope cannot name it. */
	@CheckReturnValue
	@Nullable
	private static String operandTypeName(@Nonnull DetailAST operand) {
		final var scope = ResolutionScope.of(operand);
		return AstResolve.expressionTypeName(operand, scope.packageName(), scope.imports());
	}

	@CheckReturnValue
	private static boolean plusChainContainsBareLhs(@Nonnull DetailAST plus, @Nonnull DetailAST lhs) {
		// Walk the chain spine: PLUS(PLUS(a, b), c) -> operands [a, b, c].
		var node = plus;
		while (node != null && node.getType() == TokenTypes.PLUS) {
			final var left = node.getFirstChild();
			final var right = left != null ? left.getNextSibling() : null;
			if (right != null && AstQuery.astStructuralEquals(right, lhs))
				return true;
			node = left;
		}
		return node != null && AstQuery.astStructuralEquals(node, lhs);
	}

	/**
	 * Resolve the static type of an LHS node (IDENT, DOT chain, or INDEX_OP,
	 * possibly chained for multi-dim arrays). For each INDEX_OP nesting
	 * level, one `[]` is stripped from the receiver's type.
	 */
	@CheckReturnValue
	@Nullable
	private static String resolveLhsType(@Nonnull DetailAST lhs) {
		var node = lhs;
		var indexDepth = 0;
		while (node.getType() == TokenTypes.INDEX_OP) {
			++indexDepth;
			final var array = node.getFirstChild();
			if (array == null)
				return null;
			node = array;
		}
		if (indexDepth == 0)
			return resolveReceiverChainType(node);
		final var receiverType = resolveReceiverChainType(node);
		if (receiverType == null)
			return null;
		var stripped = receiverType;
		for (var k = 0; k < indexDepth; ++k) {
			if (!stripped.endsWith("[]"))
				return null;
			stripped = stripped.substring(0, stripped.length() - 2);
		}
		return stripped;
	}

	@CheckReturnValue
	@Nullable
	private static String resolveReceiverChainType(@Nonnull DetailAST chain) {
		// Collect the chain bottom-up: dots' field-IDENTs get pushed, then resolved left-to-right.
		final var fieldNames = new ArrayDeque<String>();
		var node = chain;
		String enclosingInstanceType = null;
		while (node.getType() == TokenTypes.DOT) {
			final var first = node.getFirstChild();
			final var fieldIdent = first != null ? first.getNextSibling() : null;
			if (fieldIdent == null)
				return null;

			// `Outer.this.f`: this DOT names an enclosing instance, not a field, so it ends the walk
			if (fieldIdent.getType() == TokenTypes.LITERAL_THIS) {
				if (first.getType() != TokenTypes.IDENT)
					return null;
				enclosingInstanceType = first.getText();
				break;
			}
			if (fieldIdent.getType() != TokenTypes.IDENT)
				return null;
			fieldNames.push(fieldIdent.getText());
			node = first;
		}
		final String startType;
		// a name that resolves to nothing also leaves startType null, and letting it share this
		// path typed `holder.sb` from a same-named field of the enclosing class, which is a
		// different object entirely
		final var readsEnclosingFields = node.getType() == TokenTypes.LITERAL_THIS;
		if (enclosingInstanceType != null)
			startType = enclosingInstanceType;
		else if (readsEnclosingFields)
			startType = null;
		else if (node.getType() == TokenTypes.LITERAL_SUPER) {
			// resolved on the supertype rather than here, because a field this class redeclares
			// shadows the one `super.` reads, and the two may not share a type
			startType = enclosingSupertypeName(chain);
			if (startType == null)
				return null;
		}
		else if (node.getType() == TokenTypes.IDENT)
			startType = AstResolve.resolveVariableType(node, node.getText());
		else
			return null;
		var currentType = startType;
		var first = true;
		for (var fieldName : fieldNames) {
			if (first && readsEnclosingFields)
				currentType = AstResolve.resolveSameFileFieldType(chain, null, fieldName);
			else {
				if (currentType == null)
					return null;
				currentType = AstResolve.resolveSameFileFieldType(chain, currentType, fieldName);
			}
			first = false;
		}
		if (fieldNames.isEmpty())
			return startType;
		return currentType;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST singleArgInner(@Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstQuery.countArguments(elist) != 1)
			return null;
		return findArgInner(elist, 0);
	}

	/**
	 * Whether rewriting {@code plus} as one {@code .append(...)} per top-level operand preserves the
	 * value it appends.
	 *
	 * <p>Two conditions, both required. The first is that the chain is a concatenation throughout:
	 * {@code A + B + C} evaluates as {@code ((A + B) + C)}, so once the first pair produces a
	 * String every operand after it is concatenated rather than added, and that first pair has to
	 * be a String concatenation already. {@code 1 + 2 + "x"} is {@code "3x"} where the split would
	 * append {@code "12x"}, while {@code -1 + "x"} and {@code 'a' + "x"} split cleanly because
	 * {@code StringBuilder.append} renders a number and a char the same way the concatenation does.
	 *
	 * <p>The second is that each operand survives being appended on its own, which
	 * {@link #concatenationSurvivesValueOf} answers. A chain can satisfy the first and fail this:
	 * {@code "a" + chars} is a String concatenation with a {@code char[]} operand.
	 *
	 * <p>A parenthesized left operand ends the spine early, which is correct: the group is one
	 * operand and is appended whole.
	 */
	@CheckReturnValue
	public static boolean splitsIntoAppendsSafely(@Nonnull DetailAST plus) {
		var innermost = plus;
		while (true) {
			final var first = innermost.getFirstChild();
			if (first == null || first.getType() != TokenTypes.PLUS)
				break;
			innermost = first;
		}
		if (innermost.getType() != TokenTypes.PLUS || !isStringConcat(innermost))
			return false;

		for (var node = plus; node != null && node.getType() == TokenTypes.PLUS; node = node.getFirstChild()) {
			final var right = AstQuery.unwrapParensAndExprFromEnd(node.getLastChild());
			if (right == null || !concatenationSurvivesValueOf(right))
				return false;
		}
		final var leftmost = AstQuery.unwrapParensAndExpr(innermost.getFirstChild());
		return leftmost != null && concatenationSurvivesValueOf(leftmost);
	}

	@CheckReturnValue
	@Nullable
	private static String typeNameForVariableDef(@Nonnull DetailAST variableDef) {
		final var type = variableDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : null;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.ASSIGN,
				TokenTypes.LITERAL_FOR,
				TokenTypes.LITERAL_NEW,
				TokenTypes.LITERAL_WHILE,
				TokenTypes.METHOD_CALL,
				TokenTypes.PLUS,
				TokenTypes.PLUS_ASSIGN,
				TokenTypes.VARIABLE_DEF
		};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var detection = detectFor(ast);
		if (detection != null)
			log(ast, detection.category().checkMessageKey(), detection.logArgs());
	}
}