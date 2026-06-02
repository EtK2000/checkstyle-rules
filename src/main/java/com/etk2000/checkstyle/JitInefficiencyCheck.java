package com.etk2000.checkstyle;

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
	private record Detection(
			@Nonnull JitInefficiencyCategory category,
			@Nonnull Object... logArgs
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
		if (body.getType() == TokenTypes.METHOD_CALL) {
			final var dot = body.findFirstToken(TokenTypes.DOT);
			if (dot != null) {
				final var receiver = dot.getFirstChild();
				if (receiver != null && receiver.getType() == TokenTypes.IDENT
						&& mapVar.equals(receiver.getText())) {
					final var methodIdent = receiver.getNextSibling();
					if (methodIdent != null && methodIdent.getType() == TokenTypes.IDENT
							&& "get".equals(methodIdent.getText())) {
						final var elist = body.findFirstToken(TokenTypes.ELIST);
						if (elist != null && AstUtil.countArguments(elist) == 1) {
							final var arg = findArgInner(elist, 0);
							if (arg != null && arg.getType() == TokenTypes.IDENT
									&& loopVar.equals(arg.getText()))
								return true;
						}
					}
				}
			}
		}
		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (bodyHasMapGet(child, mapVar, loopVar))
				return true;
		}
		return false;
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

	@CheckReturnValue
	private static boolean isEmptyStringLiteral(@Nullable DetailAST node) {
		if (node == null)
			return false;
		final var inner = node.getType() == TokenTypes.EXPR ? node.getFirstChild() : node;
		return inner != null && inner.getType() == TokenTypes.STRING_LITERAL
				&& "\"\"".equals(inner.getText());
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
	private static boolean iteratorRefsAreNextOnly(@Nonnull DetailAST node, @Nonnull String iterName) {
		if (node.getType() == TokenTypes.IDENT && iterName.equals(node.getText())) {
			final var parent = node.getParent();
			if (parent == null || parent.getType() != TokenTypes.DOT)
				return false;
			final var method = node.getNextSibling();
			if (method == null || method.getType() != TokenTypes.IDENT)
				return false;
			final var name = method.getText();
			if (!"next".equals(name) && !"hasNext".equals(name))
				return false;
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (!iteratorRefsAreNextOnly(child, iterName))
				return false;
		}
		return true;
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

	@CheckReturnValue
	private static boolean mentionsIdent(@Nonnull DetailAST root, @Nonnull String name) {
		if (root.getType() == TokenTypes.IDENT && name.equals(root.getText()))
			return true;
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (mentionsIdent(child, name))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean modifiedInLoopInternal(@Nonnull DetailAST node, @Nonnull String varName, boolean inLoop) {
		final var t = node.getType();
		final var nowInLoop = inLoop || t == TokenTypes.LITERAL_FOR
				|| t == TokenTypes.LITERAL_WHILE || t == TokenTypes.LITERAL_DO;
		if (nowInLoop) {
			if (t == TokenTypes.PLUS_ASSIGN || t == TokenTypes.MINUS_ASSIGN
					|| t == TokenTypes.STAR_ASSIGN || t == TokenTypes.DIV_ASSIGN
					|| t == TokenTypes.MOD_ASSIGN) {
				final var lhs = node.getFirstChild();
				if (lhs != null && lhs.getType() == TokenTypes.IDENT
						&& varName.equals(lhs.getText()))
					return true;
			}
			else if (t == TokenTypes.ASSIGN) {
				final var lhs = node.getFirstChild();
				if (lhs != null && lhs.getType() == TokenTypes.IDENT
						&& varName.equals(lhs.getText()) && isAssignReadingSelf(node, varName))
					return true;
			}
		}
		for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (modifiedInLoopInternal(child, varName, nowInLoop))
				return true;
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
				&& isStringTypeName(AstUtil.resolveVariableType(node, node.getText()));
	}

	@CheckReturnValue
	private static boolean plusChainContainsBareLhs(@Nonnull DetailAST plus, @Nonnull DetailAST lhs) {
		// Walk the chain spine: PLUS(PLUS(a, b), c) -> operands [a, b, c].
		var node = plus;
		while (node != null && node.getType() == TokenTypes.PLUS) {
			final var left = node.getFirstChild();
			final var right = left != null ? left.getNextSibling() : null;
			if (right != null && AstUtil.astStructuralEquals(right, lhs))
				return true;
			node = left;
		}
		return node != null && AstUtil.astStructuralEquals(node, lhs);
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
		// Collect the chain bottom-up: dots' field-IDENTs get pushed, then the
		// leftmost receiver. We then resolve left-to-right.
		final var fieldNames = new ArrayDeque<String>();
		var node = chain;
		while (node.getType() == TokenTypes.DOT) {
			final var first = node.getFirstChild();
			final var fieldIdent = first != null ? first.getNextSibling() : null;
			if (fieldIdent == null || fieldIdent.getType() != TokenTypes.IDENT)
				return null;
			fieldNames.push(fieldIdent.getText());
			node = first;
		}
		final String startType;
		if (node.getType() == TokenTypes.LITERAL_THIS)
			startType = null;
		else if (node.getType() == TokenTypes.IDENT)
			startType = AstUtil.resolveVariableType(node, node.getText());
		else
			return null;
		var currentType = startType;
		var first = true;
		for (var fieldName : fieldNames) {
			if (first && startType == null) {
				// receiver is `this`; resolve in enclosing class.
				currentType = AstUtil.resolveSameFileFieldType(chain, null, fieldName);
			}
			else {
				if (currentType == null)
					return null;
				currentType = AstUtil.resolveSameFileFieldType(chain, currentType, fieldName);
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
		if (elist == null || AstUtil.countArguments(elist) != 1)
			return null;
		return findArgInner(elist, 0);
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

	/**
	 * Walks the parsed tree and returns the inefficiency category of the node
	 * the check would log at {@code (line, column)} (0-based line index,
	 * 0-based code-point column, matching {@code AbstractCheck.log}'s
	 * {@code getLineNo()}/{@code getColumnNo()}).
	 */
	@CheckReturnValue
	@Nullable
	public JitInefficiencyCategory categorizeAt(@Nonnull DetailAST root, int line, int column) {
		final var node = AstUtil.findNodeAt(root, line, column, n -> detectFor(n) != null);
		if (node == null)
			return null;
		final var detection = detectFor(node);
		return detection != null ? detection.category() : null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectAppendConcat(@Nonnull DetailAST methodCall) {
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
		final var receiver = dot.getFirstChild();
		String receiverType = null;
		if (receiver != null && receiver.getType() == TokenTypes.IDENT)
			receiverType = AstUtil.resolveVariableType(methodCall, receiver.getText());
		if (receiverType != null
				&& !"StringBuilder".equals(receiverType)
				&& !"StringBuffer".equals(receiverType))
			return null;
		return new Detection(JitInefficiencyCategory.APPEND_CONCAT);
	}

	@CheckReturnValue
	@Nullable
	private Detection detectAssignPlusStringInLoop(@Nonnull DetailAST assign) {
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
		return new Detection(JitInefficiencyCategory.STRING_CONCAT_IN_LOOP);
	}

	@CheckReturnValue
	@Nullable
	private Detection detectBoxedAccumulator(@Nonnull DetailAST variableDef) {
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
		if (modifiedInLoopInternal(scope, varName, false))
			return new Detection(JitInefficiencyCategory.BOXED_ACCUMULATOR, varName, typeName);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectDoubleBrace(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!COLLECTION_OR_MAP_TYPES.contains(className))
			return null;
		final var objBlock = literalNew.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.INSTANCE_INIT)
				return new Detection(JitInefficiencyCategory.DOUBLE_BRACE);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectEnumValuesInLoop(@Nonnull DetailAST methodCall) {
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
		if (elist == null || AstUtil.countArguments(elist) != 0)
			return null;
		final var receiverName = first.getText();
		if (receiverName.isEmpty() || !Character.isUpperCase(receiverName.charAt(0)))
			return null;
		if (!ancestorIsLoop(methodCall))
			return null;
		return new Detection(JitInefficiencyCategory.ENUM_VALUES_IN_LOOP, receiverName);
	}

	@CheckReturnValue
	@Nullable
	private Detection detectFor(@Nonnull DetailAST ast) {
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
	private Detection detectForEachKeySetGet(@Nonnull DetailAST literalFor) {
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
			return new Detection(JitInefficiencyCategory.MAP_KEYSET_GET);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectIteratorWhile(@Nonnull DetailAST literalWhile) {
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
		if (elist == null || AstUtil.countArguments(elist) != 0)
			return null;
		final var iterName = receiver.getText();
		final var rparen = literalWhile.findFirstToken(TokenTypes.RPAREN);
		final var body = rparen != null ? rparen.getNextSibling() : null;
		if (body == null)
			return null;
		if (iteratorRefsAreNextOnly(body, iterName))
			return new Detection(JitInefficiencyCategory.ITERATOR_LOOP);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectLiteralNew(@Nonnull DetailAST literalNew) {
		final var className = literalNewClassName(literalNew);
		if (className == null)
			return null;
		if (BOXED_PRIMITIVE_TYPES.contains(className)) {
			final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
			if (elist != null && AstUtil.countArguments(elist) == 1)
				return new Detection(JitInefficiencyCategory.BOXED_CONSTRUCTOR, className);
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
	private Detection detectMethodCall(@Nonnull DetailAST methodCall) {
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
	private Detection detectNewLiteralForReusableFactory(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!REUSABLE_FACTORY_NEW_TYPES.contains(className))
			return null;
		final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstUtil.countArguments(elist) == 0)
			return null;
		final var firstArg = findArgInner(elist, 0);
		if (firstArg == null || firstArg.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsMethodBody(literalNew))
			return null;
		return new Detection(JitInefficiencyCategory.REUSABLE_OBJECT, "new " + className + "(...)");
	}

	@CheckReturnValue
	@Nullable
	private Detection detectNewString(@Nonnull DetailAST literalNew) {
		final var className = literalNewClassName(literalNew);
		if (!"String".equals(className))
			return null;
		final var elist = literalNew.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstUtil.countArguments(elist) != 1)
			return null;
		final var arg = findArgInner(elist, 0);
		if (arg == null)
			return null;
		if (arg.getType() == TokenTypes.STRING_LITERAL)
			return new Detection(JitInefficiencyCategory.NEW_STRING, "string literal");
		if (arg.getType() == TokenTypes.IDENT) {
			final var typeName = AstUtil.resolveVariableType(literalNew, arg.getText());
			if (isStringTypeName(typeName))
				return new Detection(JitInefficiencyCategory.NEW_STRING, "String variable");
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectPlusAssignStringInLoop(@Nonnull DetailAST plusAssign) {
		final var lhs = plusAssign.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.IDENT)
			return null;
		final var typeName = AstUtil.resolveVariableType(plusAssign, lhs.getText());
		if (!isStringTypeName(typeName))
			return null;
		if (!ancestorIsLoop(plusAssign))
			return null;
		return new Detection(JitInefficiencyCategory.STRING_CONCAT_IN_LOOP);
	}

	@CheckReturnValue
	@Nullable
	private Detection detectPlusForEmptyStringConcat(@Nonnull DetailAST plus) {
		final var left = plus.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (isEmptyStringLiteral(left) || isEmptyStringLiteral(right))
			return new Detection(JitInefficiencyCategory.EMPTY_STRING_CONCAT);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private Detection detectReusableFactoryCall(@Nonnull DetailAST methodCall) {
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
		if (elist == null || AstUtil.countArguments(elist) == 0)
			return null;
		final var firstArg = findArgInner(elist, 0);
		if (firstArg == null || firstArg.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsMethodBody(methodCall))
			return null;
		return new Detection(JitInefficiencyCategory.REUSABLE_OBJECT, receiverName + "." + methodName + "(...)");
	}

	@CheckReturnValue
	@Nullable
	private Detection detectStringBuffer(@Nonnull DetailAST literalNew, @Nonnull String className) {
		if (!"StringBuffer".equals(className))
			return null;
		for (var parent = literalNew.getParent(); parent != null; parent = parent.getParent()) {
			final var t = parent.getType();
			if (t == TokenTypes.VARIABLE_DEF) {
				final var grand = parent.getParent();
				if (grand != null && grand.getType() == TokenTypes.SLIST)
					return new Detection(JitInefficiencyCategory.STRING_BUFFER);
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
	private Detection detectStringRegexCallInLoop(@Nonnull DetailAST methodCall) {
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
			final var receiverType = AstUtil.resolveVariableType(methodCall, receiverName);
			if (receiverType != null && !isStringTypeName(receiverType))
				return null;
		}
		else if (receiver.getType() != TokenTypes.STRING_LITERAL)
			return null;
		if (!ancestorIsLoop(methodCall))
			return null;
		return new Detection(JitInefficiencyCategory.STRING_REGEX_IN_LOOP, method.getText());
	}

	@CheckReturnValue
	@Nullable
	private Detection detectToArraySized(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var method = dot.getLastChild();
		if (method == null || method.getType() != TokenTypes.IDENT
				|| !"toArray".equals(method.getText()))
			return null;
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || AstUtil.countArguments(elist) != 1)
			return null;
		final var arg = findArgInner(elist, 0);
		if (arg == null || arg.getType() != TokenTypes.LITERAL_NEW)
			return null;
		// skip multi-dimensional arrays: count direct ARRAY_DECLARATOR children of LITERAL_NEW
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
		if (AstUtil.isZeroLiteral(sizeInner))
			return null;
		// a qualified type nests its segments under a DOT, so the direct-child IDENT
		// lookup finds nothing and the message would render the placeholder
		final var typeIdent = arg.findFirstToken(TokenTypes.IDENT);
		final var qualified = arg.findFirstToken(TokenTypes.DOT);
		final String typeName;
		if (typeIdent != null)
			typeName = typeIdent.getText();
		else if (qualified != null)
			typeName = AstUtil.dottedName(qualified);
		else
			typeName = "?";
		return new Detection(JitInefficiencyCategory.TOARRAY_SIZED, typeName);
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