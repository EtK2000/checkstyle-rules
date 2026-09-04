package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags loops that add/put/copy elements one at a time when a bulk operation exists
 * ({@code addAll}, {@code Collections.addAll}, {@code putAll}, {@code System.arraycopy},
 * {@code Arrays.fill}).
 */
public class PreferBulkOperationCheck extends AbstractResolvingCheck {
	public enum BulkKind {
		ADD_ALL,
		ARRAY_COPY,
		COLLECTIONS_ADD_ALL,
		FILL,
		PUT_ALL
	}

	/**
	 * A bulk-operation opportunity the check reports. {@code first}/{@code second} are the display
	 * texts the message and fixer use ({@code target}/{@code source} for add/put; {@code src}/
	 * {@code dst} for arraycopy; {@code arr}/{@code value} for fill), sliced verbatim from the source
	 * so any receiver/type shape (qualified name, generics, cast) survives. The 0-based span
	 * {@code [startLine:startIndex, endLine:endIndex)} covers the whole {@code for} statement when
	 * {@link #statementForm}, otherwise the {@code forEach} call expression.
	 *
	 * <p>{@code startIndex}/{@code endIndex} are <em>char</em> indices into their own line, already
	 * converted from the code-point columns {@link DetailAST} reports, so they may be handed to
	 * {@link String#substring} directly and must not be compared against an AST column. Contrast
	 * {@link #classifyAt}, whose {@code column} parameter is a code-point column because it is
	 * matched against AST positions.
	 *
	 * @see LineText#charIndexOfColumn
	 */
	public record BulkOp(
			@Nonnull BulkKind kind,
			@Nonnull String first,
			@Nonnull String second,
			int startLine,
			int startIndex,
			int endLine,
			int endIndex,
			boolean statementForm
	) {}

	/**
	 * A classified opportunity plus the operand nodes the type gate needs. {@code sourceExpr} and
	 * {@code targetExpr} are null for the array kinds, which carry no collection operands.
	 */
	private record KindArgs(
			@Nonnull BulkKind kind,
			@Nonnull String first,
			@Nonnull String second,
			@Nullable DetailAST sourceExpr,
			@Nullable DetailAST targetExpr
	) {
		KindArgs(@Nonnull BulkKind kind, @Nonnull String first, @Nonnull String second) {
			this(kind, first, second, null, null);
		}

		@CheckReturnValue
		@Nonnull
		KindArgs withKind(@Nonnull BulkKind replacement) {
			return new KindArgs(replacement, first, second, sourceExpr, targetExpr);
		}
	}

	private static final String COLLECTION_FQCN = "java.util.Collection";
	private static final String MAP_FQCN = "java.util.Map";

	static final String MSG_ADDALL = "prefer.bulk.addall";
	static final String MSG_ARRAYCOPY = "prefer.bulk.arraycopy";
	static final String MSG_COLLECTIONSADDALL = "prefer.bulk.collectionsaddall";
	static final String MSG_FILL = "prefer.bulk.fill";
	static final String MSG_PUTALL = "prefer.bulk.putall";

	/**
	 * Whether the bulk method this opportunity names actually applies to its operands' types: the
	 * source assignable to {@code Collection}/{@code Map}, and the target declaring
	 * {@code addAll}/{@code putAll}. A type the file's scope cannot resolve answers no, so a
	 * rewrite is refused rather than guessed.
	 *
	 * <p>Applied once here instead of at each classifier, so no path can be left ungated.
	 */
	@CheckReturnValue
	@Nullable
	private static KindArgs applyTypeGate(@Nonnull KindArgs kindArgs, @Nonnull ResolutionScope scope) {
		// `arr.length` and `arr[i]` only parse against an array, which establishes array-ness but
		// NOT component-type compatibility: known wrong for a byte/short/char fill and for an
		// arraycopy between mismatched component types
		if (kindArgs.kind() == BulkKind.ARRAY_COPY || kindArgs.kind() == BulkKind.FILL)
			return kindArgs;

		if (kindArgs.sourceExpr() == null || kindArgs.targetExpr() == null)
			return null;

		final var isMap = kindArgs.kind() == BulkKind.PUT_ALL;
		final var elementsFqcn = isMap ? MAP_FQCN : COLLECTION_FQCN;

		final var targetFqcn = resolvedTypeOf(kindArgs.targetExpr(), scope);
		if (targetFqcn == null)
			return null;
		if (!ReflectionUtil.declaresMethodErasure(targetFqcn, isMap ? "putAll" : "addAll", List.of(elementsFqcn)))
			return null;

		// an object array has no addAll overload but does have Collections.addAll, so it
		// reclassifies rather than refusing; a primitive array has no boxing-free bulk API.
		// Collections.addAll takes a Collection, which declaring addAll(Collection) does not imply
		if (!isMap && isObjectArraySource(kindArgs.sourceExpr(), scope)) {
			return ReflectionUtil.acceptsValueOfType(COLLECTION_FQCN, targetFqcn)
					? kindArgs.withKind(BulkKind.COLLECTIONS_ADD_ALL)
					: null;
		}

		final var sourceFqcn = resolvedTypeOf(kindArgs.sourceExpr(), scope);
		return sourceFqcn != null && ReflectionUtil.acceptsValueOfType(elementsFqcn, sourceFqcn) ? kindArgs : null;
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachCall(@Nonnull DetailAST ast, @Nonnull List<String> lines) {
		if (!"forEach".equals(getCallMethodName(ast)))
			return null;

		final var sourceReceiver = getReceiver(ast);
		if (sourceReceiver == null || sourceReceiver.getType() == TokenTypes.METHOD_CALL)
			return null;

		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;

		DetailAST arg = null;
		var argCount = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			if (type == TokenTypes.LAMBDA || type == TokenTypes.METHOD_REF) {
				arg = child;
				++argCount;
			}
			else if (type == TokenTypes.EXPR) {
				final var inner = child.getFirstChild();
				if (inner != null) {
					final var innerType = inner.getType();
					if (innerType == TokenTypes.LAMBDA || innerType == TokenTypes.METHOD_REF)
						arg = inner;
				}
				++argCount;
			}
		}
		if (argCount != 1 || arg == null)
			return null;

		return arg.getType() == TokenTypes.METHOD_REF
				? checkForEachMethodRef(ast, lines, arg)
				: checkForEachLambdaBody(ast, lines, arg);
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachLambdaAdd(
			@Nonnull DetailAST ast,
			@Nonnull List<String> lines,
			@Nonnull DetailAST lambda,
			@Nonnull String paramName
	) {
		final var bodyStmt = getLambdaBodyStatement(lambda);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return null;
		if (!"add".equals(getCallMethodName(bodyStmt)))
			return null;
		if (getArgCount(bodyStmt) != 1)
			return null;

		final var arg = getNthArg(bodyStmt, 0);
		if (arg == null || !isIdent(arg.getFirstChild(), paramName))
			return null;

		final var target = sliceReceiver(lines, bodyStmt);
		final var source = sliceReceiver(lines, ast);
		if (target == null || source == null)
			return null;

		return new KindArgs(BulkKind.ADD_ALL, target, source, getReceiver(ast), getReceiver(bodyStmt));
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachLambdaBody(@Nonnull DetailAST ast, @Nonnull List<String> lines, @Nonnull DetailAST lambda) {
		final var paramNames = new String[2];
		var paramCount = 0;
		final var params = lambda.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var child = params.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.PARAMETER_DEF) {
					final var ident = child.findFirstToken(TokenTypes.IDENT);
					if (ident == null)
						return null;
					if (paramCount < paramNames.length)
						paramNames[paramCount] = ident.getText();
					++paramCount;
				}
			}
		}
		else {
			final var firstChild = lambda.getFirstChild();
			if (firstChild != null && firstChild.getType() == TokenTypes.IDENT) {
				paramNames[0] = firstChild.getText();
				paramCount = 1;
			}
		}

		if (paramCount == 2 && paramNames[0] != null && paramNames[1] != null)
			return checkForEachLambdaPut(ast, lines, lambda, paramNames);
		if (paramCount == 1 && paramNames[0] != null)
			return checkForEachLambdaAdd(ast, lines, lambda, paramNames[0]);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachLambdaPut(
			@Nonnull DetailAST ast,
			@Nonnull List<String> lines,
			@Nonnull DetailAST lambda,
			@Nonnull String[] paramNames
	) {
		final var bodyStmt = getLambdaBodyStatement(lambda);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return null;
		if (!"put".equals(getCallMethodName(bodyStmt)))
			return null;
		if (getArgCount(bodyStmt) != 2)
			return null;

		final var firstArg = getNthArg(bodyStmt, 0);
		if (firstArg == null || !isIdent(firstArg.getFirstChild(), paramNames[0]))
			return null;
		final var secondArg = getNthArg(bodyStmt, 1);
		if (secondArg == null || !isIdent(secondArg.getFirstChild(), paramNames[1]))
			return null;

		final var target = sliceReceiver(lines, bodyStmt);
		final var source = sliceReceiver(lines, ast);
		if (target == null || source == null)
			return null;

		return new KindArgs(BulkKind.PUT_ALL, target, source, getReceiver(ast), getReceiver(bodyStmt));
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachLoop(@Nonnull DetailAST ast, @Nonnull List<String> lines) {
		final var forEachClause = ast.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
		if (forEachClause == null)
			return null;

		final var varDef = forEachClause.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef == null)
			return null;
		final var iterVarIdent = varDef.findFirstToken(TokenTypes.IDENT);
		if (iterVarIdent == null)
			return null;
		final var iterVarName = iterVarIdent.getText();

		DetailAST sourceExpr = null;
		for (var child = forEachClause.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR) {
				sourceExpr = AstQuery.unwrapParensAndExpr(child);
				break;
			}
		}
		if (sourceExpr == null)
			return null;

		final var bodyStmt = getSingleBodyStatement(ast);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return null;

		final var methodName = getCallMethodName(bodyStmt);
		final var target = sliceReceiver(lines, bodyStmt);
		if (methodName == null || target == null)
			return null;

		if ("add".equals(methodName) && getArgCount(bodyStmt) == 1) {
			final var arg = getNthArg(bodyStmt, 0);
			final var source = AstSpan.sliceNode(lines, sourceExpr);
			if (arg != null && source != null && isIdent(arg.getFirstChild(), iterVarName))
				return new KindArgs(BulkKind.ADD_ALL, target, source, sourceExpr, getReceiver(bodyStmt));
		}
		else if ("put".equals(methodName) && getArgCount(bodyStmt) == 2) {
			if (sourceExpr.getType() != TokenTypes.METHOD_CALL)
				return null;
			if (!"entrySet".equals(getCallMethodName(sourceExpr)))
				return null;
			if (getArgCount(sourceExpr) != 0)
				return null;

			final var firstArg = getNthArg(bodyStmt, 0);
			final var secondArg = getNthArg(bodyStmt, 1);
			if (firstArg == null || secondArg == null)
				return null;
			if (!isNoArgCallOnVar(firstArg.getFirstChild(), iterVarName, "getKey"))
				return null;
			if (!isNoArgCallOnVar(secondArg.getFirstChild(), iterVarName, "getValue"))
				return null;

			final var mapReceiver = getReceiver(sourceExpr);
			if (mapReceiver == null)
				return null;

			final var map = AstSpan.sliceNode(lines, mapReceiver);
			if (map == null)
				return null;
			return new KindArgs(BulkKind.PUT_ALL, target, map, mapReceiver, getReceiver(bodyStmt));
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkForEachMethodRef(@Nonnull DetailAST ast, @Nonnull List<String> lines, @Nonnull DetailAST methodRef) {
		final var method = methodRef.getLastChild();
		if (method == null || method.getType() != TokenTypes.IDENT || method == methodRef.getFirstChild())
			return null;
		final var methodName = method.getText();

		final var target = sliceMethodRefQualifier(lines, methodRef);
		final var source = sliceReceiver(lines, ast);
		if (target == null || source == null)
			return null;

		final var sourceExpr = getReceiver(ast);
		final var targetExpr = methodRef.getFirstChild();
		if ("put".equals(methodName))
			return new KindArgs(BulkKind.PUT_ALL, target, source, sourceExpr, targetExpr);
		if ("add".equals(methodName))
			return new KindArgs(BulkKind.ADD_ALL, target, source, sourceExpr, targetExpr);
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkIndexedAddAll(
			@Nonnull DetailAST bodyStmt,
			@Nonnull List<String> lines,
			@Nonnull String loopVar,
			@Nonnull DetailAST boundReceiver
	) {
		if (bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return null;
		if (!"add".equals(getCallMethodName(bodyStmt)))
			return null;
		if (getArgCount(bodyStmt) != 1)
			return null;

		final var arg = getNthArg(bodyStmt, 0);
		if (arg == null)
			return null;
		final var argInner = arg.getFirstChild();
		if (argInner == null || argInner.getType() != TokenTypes.METHOD_CALL)
			return null;
		if (!"get".equals(getCallMethodName(argInner)))
			return null;
		if (getArgCount(argInner) != 1)
			return null;

		final var getArg = getNthArg(argInner, 0);
		if (getArg == null || !isIdent(getArg.getFirstChild(), loopVar))
			return null;

		final var getReceiverNode = getReceiver(argInner);
		if (getReceiverNode == null)
			return null;
		if (!AstText.exprText(getReceiverNode).equals(AstText.exprText(boundReceiver)))
			return null;

		final var target = sliceReceiver(lines, bodyStmt);
		final var source = AstSpan.sliceNode(lines, boundReceiver);
		if (target == null || source == null)
			return null;

		return new KindArgs(BulkKind.ADD_ALL, target, source, boundReceiver, getReceiver(bodyStmt));
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkIndexedArrayOp(
			@Nonnull DetailAST bodyStmt,
			@Nonnull List<String> lines,
			@Nonnull String loopVar,
			@Nonnull DetailAST boundArrayExpr
	) {
		if (bodyStmt.getType() != TokenTypes.ASSIGN)
			return null;

		final var lhs = bodyStmt.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.INDEX_OP)
			return null;

		final var lhsArray = lhs.getFirstChild();
		if (lhsArray == null)
			return null;
		final var lhsIndex = unwrapExpr(lhsArray.getNextSibling());
		if (!isIdent(lhsIndex, loopVar))
			return null;

		final var rhs = lhs.getNextSibling();
		if (rhs == null)
			return null;

		if (rhs.getType() == TokenTypes.INDEX_OP) {
			final var rhsArray = rhs.getFirstChild();
			if (rhsArray == null)
				return null;
			final var rhsIndex = unwrapExpr(rhsArray.getNextSibling());
			if (!isIdent(rhsIndex, loopVar))
				return null;
			if (!AstText.exprText(rhsArray).equals(AstText.exprText(boundArrayExpr)))
				return null;

			final var src = AstSpan.sliceNode(lines, rhsArray);
			final var dst = AstSpan.sliceNode(lines, lhsArray);
			if (src == null || dst == null)
				return null;
			return new KindArgs(BulkKind.ARRAY_COPY, src, dst);
		}
		if (AstQuery.isPureExpression(rhs) && !referencesVar(rhs, loopVar)) {
			if (!AstText.exprText(lhsArray).equals(AstText.exprText(boundArrayExpr)))
				return null;

			final var arr = AstSpan.sliceNode(lines, lhsArray);
			final var value = AstSpan.sliceNode(lines, rhs);
			if (arr == null || value == null)
				return null;
			return new KindArgs(BulkKind.FILL, arr, value);
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static KindArgs checkIndexedForLoop(@Nonnull DetailAST ast, @Nonnull List<String> lines) {
		final var forInit = ast.findFirstToken(TokenTypes.FOR_INIT);
		if (forInit == null)
			return null;
		final var varDef = forInit.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef == null)
			return null;

		var varDefCount = 0;
		for (var child = forInit.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				++varDefCount;
		}
		if (varDefCount != 1)
			return null;

		final var loopVarIdent = varDef.findFirstToken(TokenTypes.IDENT);
		if (loopVarIdent == null)
			return null;
		final var loopVar = loopVarIdent.getText();

		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return null;
		final var initExpr = assign.findFirstToken(TokenTypes.EXPR);
		if (initExpr == null)
			return null;
		final var initValue = initExpr.getFirstChild();
		if (initValue == null || !AstQuery.isZeroLiteral(initValue))
			return null;

		final var forCond = ast.findFirstToken(TokenTypes.FOR_CONDITION);
		if (forCond == null)
			return null;
		final var condExpr = forCond.findFirstToken(TokenTypes.EXPR);
		if (condExpr == null)
			return null;
		final var comparison = condExpr.getFirstChild();
		if (comparison == null || comparison.getType() != TokenTypes.LT)
			return null;
		final var lhs = comparison.getFirstChild();
		if (lhs == null)
			return null;
		final var rhs = lhs.getNextSibling();
		if (rhs == null || !isIdent(lhs, loopVar))
			return null;

		final var forIter = ast.findFirstToken(TokenTypes.FOR_ITERATOR);
		if (forIter == null || !isSimpleIncrement(forIter, loopVar))
			return null;

		final var bodyStmt = getSingleBodyStatement(ast);
		if (bodyStmt == null)
			return null;

		if (rhs.getType() == TokenTypes.METHOD_CALL) {
			if ("size".equals(getCallMethodName(rhs)) && getArgCount(rhs) == 0) {
				final var boundReceiver = getReceiver(rhs);
				if (boundReceiver != null)
					return checkIndexedAddAll(bodyStmt, lines, loopVar, boundReceiver);
			}
		}
		else if (rhs.getType() == TokenTypes.DOT && "length".equals(getLastIdentInDot(rhs))) {
			final var arrayExpr = rhs.getFirstChild();
			if (arrayExpr != null)
				return checkIndexedArrayOp(bodyStmt, lines, loopVar, arrayExpr);
		}
		return null;
	}

	/**
	 * Classifies {@code ast} (a {@code METHOD_CALL} or {@code LITERAL_FOR}) as a bulk-operation
	 * opportunity, returning the {@link BulkOp} or {@code null} when it does not fire. {@code lines}
	 * is the source the AST was parsed from, used to slice receiver/operand display text verbatim.
	 */
	@CheckReturnValue
	@Nullable
	public static BulkOp classify(@Nonnull DetailAST ast, @Nonnull List<String> lines) {
		return classify(ast, lines, ResolutionScope.of(ast));
	}

	/** As {@link #classify(DetailAST, List)}, against the scope the caller already holds. */
	@CheckReturnValue
	@Nullable
	private static BulkOp classify(
			@Nonnull DetailAST ast,
			@Nonnull List<String> lines,
			@Nonnull ResolutionScope scope
	) {
		final var kindArgs = switch (ast.getType()) {
			case TokenTypes.LITERAL_FOR -> ast.findFirstToken(TokenTypes.FOR_EACH_CLAUSE) != null
					? checkForEachLoop(ast, lines)
					: checkIndexedForLoop(ast, lines);
			case TokenTypes.METHOD_CALL -> checkForEachCall(ast, lines);
			default -> null;
		};
		final var gated = kindArgs == null ? null : applyTypeGate(kindArgs, scope);
		if (gated == null)
			return null;
		final var start = AstSpan.spanStart(lines, ast);
		final var end = AstSpan.spanEnd(lines, ast);
		if (start == null || end == null)
			return null;
		return new BulkOp(
				gated.kind(),
				gated.first(),
				gated.second(),
				start.line(),
				start.index(),
				end.line(),
				end.index(),
				ast.getType() == TokenTypes.LITERAL_FOR
		);
	}

	/**
	 * Locates the {@code METHOD_CALL} or {@code LITERAL_FOR} the check reported at {@code (line,
	 * column)} (0-based) in {@code root} and classifies it, or {@code null} when no such node
	 * exists. {@code lines} is the source the AST was parsed from.
	 */
	@CheckReturnValue
	@Nullable
	public static BulkOp classifyAt(@Nonnull DetailAST root, @Nonnull List<String> lines, int line, int column) {
		final var node = AstQuery.findNodeAt(
				root,
				line,
				column,
				n -> n.getType() == TokenTypes.METHOD_CALL || n.getType() == TokenTypes.LITERAL_FOR
		);
		return node == null ? null : classify(node, lines, ResolutionScope.of(root));
	}

	/**
	 * The type of a {@code forEach} lambda parameter, which carries no declared type of its own:
	 * {@code v} in {@code map.forEach((k, v) -> ...)} takes its type from {@code map}. Every
	 * {@code forEach} in the JDK hands the receiver's type arguments to the lambda in declaration
	 * order ({@code Consumer<T>}, {@code BiConsumer<K, V>}), so the n-th parameter reads the n-th
	 * argument. Restricted to {@code forEach} precisely because that ordering is not general:
	 * {@code Map.merge}'s {@code BiFunction<V, V, V>} would map its first parameter to {@code V}.
	 */
	@CheckReturnValue
	@Nullable
	private static String forEachLambdaParamType(@Nonnull DetailAST ident, @Nonnull ResolutionScope scope) {
		for (var scopeNode = ident.getParent(); scopeNode != null; scopeNode = scopeNode.getParent()) {
			if (scopeNode.getType() != TokenTypes.LAMBDA)
				continue;

			final var index = lambdaParamIndex(scopeNode, ident.getText());
			if (index < 0)
				return null;

			final var elist = scopeNode.getParent();
			final var call = elist == null ? null : elist.getParent();
			if (call == null || call.getType() != TokenTypes.METHOD_CALL)
				return null;
			if (!"forEach".equals(getCallMethodName(call)))
				return null;

			final var receiver = getReceiver(call);
			if (receiver == null || receiver.getType() != TokenTypes.IDENT)
				return null;

			return AstResolve.variableTypeArgumentName(receiver, receiver.getText(), index);
		}
		return null;
	}

	@CheckReturnValue
	private static int getArgCount(@Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		return elist == null ? 0 : AstQuery.countArguments(elist);
	}

	@CheckReturnValue
	@Nullable
	private static String getCallMethodName(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		return dot != null ? getLastIdentInDot(dot) : null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getLambdaBodyStatement(@Nonnull DetailAST lambda) {
		final var hasParens = lambda.findFirstToken(TokenTypes.RPAREN) != null;
		var pastParams = false;
		for (var child = lambda.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (hasParens) {
				if (child.getType() == TokenTypes.RPAREN) {
					pastParams = true;
					continue;
				}
			}
			else if (!pastParams && child.getType() == TokenTypes.IDENT) {
				pastParams = true;
				continue;
			}
			if (!pastParams)
				continue;
			if (child.getType() == TokenTypes.EXPR)
				return child.getFirstChild();
			if (child.getType() == TokenTypes.SLIST) {
				final var first = child.getFirstChild();
				if (first == null || first.getType() != TokenTypes.EXPR)
					return null;
				var next = first.getNextSibling();
				if (next != null && next.getType() == TokenTypes.SEMI)
					next = next.getNextSibling();
				if (next == null || next.getType() != TokenTypes.RCURLY || next.getNextSibling() != null)
					return null;
				return first.getFirstChild();
			}
			return child;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static String getLastIdentInDot(@Nonnull DetailAST dot) {
		var last = dot.getFirstChild();
		if (last == null)
			return null;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return last.getType() == TokenTypes.IDENT ? last.getText() : null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getNthArg(@Nonnull DetailAST methodCall, int n) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return null;
		var idx = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.COMMA)
				continue;
			// indexed over the same children getArgCount counts, so a bare argument node (a
			// lambda or method reference is not EXPR-wrapped) reads as unsupported here rather
			// than shifting every later argument one position left
			if (idx == n)
				return child.getType() == TokenTypes.EXPR ? child : null;
			++idx;
		}
		return null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getReceiver(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		// grouping parens are siblings under the DOT, so the first child of `(src).f()` is the LPAREN
		return dot != null ? AstQuery.unwrapParensAndExpr(dot.getFirstChild()) : null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getSingleBodyStatement(@Nonnull DetailAST forAst) {
		final var rparen = forAst.findFirstToken(TokenTypes.RPAREN);
		if (rparen == null)
			return null;
		final var body = rparen.getNextSibling();
		if (body == null)
			return null;
		if (body.getType() == TokenTypes.SLIST) {
			final var first = body.getFirstChild();
			if (first == null || first.getType() != TokenTypes.EXPR)
				return null;
			var next = first.getNextSibling();
			if (next != null && next.getType() == TokenTypes.SEMI)
				next = next.getNextSibling();
			if (next == null || next.getType() != TokenTypes.RCURLY || next.getNextSibling() != null)
				return null;
			return first.getFirstChild();
		}
		return body.getType() == TokenTypes.EXPR ? body.getFirstChild() : null;
	}

	@CheckReturnValue
	private static boolean isIdent(@Nullable DetailAST ast, @Nonnull String name) {
		return ast != null && ast.getType() == TokenTypes.IDENT && name.equals(ast.getText());
	}

	@CheckReturnValue
	private static boolean isNoArgCallOnVar(
			@Nullable DetailAST ast,
			@Nonnull String varName,
			@Nonnull String methodName
	) {
		if (ast == null || ast.getType() != TokenTypes.METHOD_CALL)
			return false;
		if (!methodName.equals(getCallMethodName(ast)))
			return false;
		return getArgCount(ast) == 0 && isIdent(getReceiver(ast), varName);
	}

	/**
	 * Whether {@code expr} is an array whose component is a reference type, the only array shape
	 * {@code Collections.addAll} accepts. A declaration spells an array {@code Foo[]} while
	 * reflection spells it {@code [LFoo;}, so both are read here.
	 */
	@CheckReturnValue
	private static boolean isObjectArraySource(@Nonnull DetailAST expr, @Nonnull ResolutionScope scope) {
		// expressionTypeName's pattern-variable fallback over-answers, which its own contract says is
		// safe only for callers that REFUSE on the answer; this one rewrites on it
		if (expr.getType() == TokenTypes.IDENT && !AstResolve.variableIsBound(expr, expr.getText()))
			return false;

		final var name = AstResolve.expressionTypeName(expr, scope.packageName(), scope.imports());
		if (name == null)
			return false;

		// `[[I` and `[Ljava.lang.String;` are both arrays of a reference component; only a
		// single-bracket primitive descriptor such as `[I` is not
		if (name.startsWith("["))
			return name.startsWith("[[") || name.startsWith("[L");

		if (!name.endsWith("[]"))
			return false;

		final var component = name.substring(0, name.length() - 2);
		return component.endsWith("[]")
				|| ReflectionUtil.resolveClassName(component, scope.packageName(), scope.imports()) != null;
	}

	@CheckReturnValue
	private static boolean isSimpleIncrement(@Nonnull DetailAST forIter, @Nonnull String varName) {
		final var elist = forIter.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return false;
		var exprCount = 0;
		DetailAST expr = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR) {
				expr = child;
				++exprCount;
			}
		}
		if (exprCount != 1 || expr == null)
			return false;
		final var incr = expr.getFirstChild();
		if (incr == null)
			return false;
		if (incr.getType() != TokenTypes.INC && incr.getType() != TokenTypes.POST_INC)
			return false;
		return isIdent(incr.getFirstChild(), varName);
	}

	@CheckReturnValue
	private static int lambdaParamIndex(@Nonnull DetailAST lambda, @Nonnull String name) {
		final var params = lambda.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null) {
			final var naked = lambda.getFirstChild();
			return naked != null && naked.getType() == TokenTypes.IDENT && name.equals(naked.getText()) ? 0 : -1;
		}

		var index = 0;
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;
			final var ident = param.findFirstToken(TokenTypes.IDENT);
			if (ident != null && name.equals(ident.getText()))
				return index;
			++index;
		}
		return -1;
	}

	@CheckReturnValue
	@Nonnull
	private static String messageKey(@Nonnull BulkKind kind) {
		return switch (kind) {
			case ADD_ALL -> MSG_ADDALL;
			case ARRAY_COPY -> MSG_ARRAYCOPY;
			case COLLECTIONS_ADD_ALL -> MSG_COLLECTIONSADDALL;
			case FILL -> MSG_FILL;
			case PUT_ALL -> MSG_PUTALL;
		};
	}

	@CheckReturnValue
	private static boolean referencesVar(@Nonnull DetailAST ast, @Nonnull String varName) {
		// avoids the unbounded recursion depth that the equivalent recursive walk would
		// incur on deeply-nested generated code
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.IDENT && varName.equals(node.getText()))
				return true;
			for (var child = node.getLastChild(); child != null; child = child.getPreviousSibling())
				stack.push(child);
		}
		return false;
	}

	/** The fully qualified name of {@code expr}'s type in {@code scope}, or null when unknown. */
	@CheckReturnValue
	@Nullable
	private static String resolvedTypeOf(@Nonnull DetailAST expr, @Nonnull ResolutionScope scope) {
		var name = AstResolve.expressionTypeName(expr, scope.packageName(), scope.imports());
		if (name == null && expr.getType() == TokenTypes.IDENT)
			name = forEachLambdaParamType(expr, scope);
		return name == null ? null : ReflectionUtil.resolveClassName(name, scope.packageName(), scope.imports());
	}

	/**
	 * Slices the source text of {@code methodRef}'s qualifier: from the qualifier's start up to the
	 * {@code ::} (the {@code METHOD_REF} node's own position), so a type witness ({@code x::<T>m})
	 * and the method name are excluded. Returns {@code null} for an empty qualifier, one that does
	 * not start on the {@code ::} line, one whose ends do not convert to char indices, and one whose
	 * start lands after the {@code ::}.
	 */
	@CheckReturnValue
	@Nullable
	private static String sliceMethodRefQualifier(@Nonnull List<String> lines, @Nonnull DetailAST methodRef) {
		final var first = methodRef.getFirstChild();
		if (first == null)
			return null;
		final var start = AstSpan.spanStart(lines, first);
		if (start == null || start.line() != methodRef.getLineNo() - 1)
			return null;
		final var lineText = lines.get(start.line());
		final var end = LineText.charIndexOfColumn(lineText, methodRef.getColumnNo());
		if (end < 0 || start.index() > end)
			return null;
		final var qualifier = lineText.substring(start.index(), end);
		return qualifier.isEmpty() ? null : qualifier;
	}

	/**
	 * Slices the source text of a dotted call's receiver: every child of the call's {@code DOT}
	 * except the trailing method-name identifier, taken verbatim from source (so a qualified name,
	 * generics, or a cast survives). Returns {@code null} when the call has no dotted receiver.
	 */
	@CheckReturnValue
	@Nullable
	private static String sliceReceiver(@Nonnull List<String> lines, @Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return null;
		final var method = dot.getLastChild();
		final var first = dot.getFirstChild();
		if (method == null || first == null || first == method)
			return null;
		final var lastReceiverChild = method.getPreviousSibling();
		if (lastReceiverChild == null)
			return null;
		return AstSpan.sliceSpan(lines, AstSpan.spanStart(lines, first), AstSpan.spanEnd(lines, lastReceiverChild));
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapExpr(@Nullable DetailAST ast) {
		return ast != null && ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
	}

	// both held per file rather than rebuilt per token: `getDefaultTokens` registers two very
	// common tokens, so redoing either at each visit is quadratic in file size
	private List<String> sourceLines = List.of();

	private ResolutionScope scope = ResolutionScope.EMPTY;

	@Override
	protected void beginFile(@Nullable DetailAST rootAST) {
		// a bare instance has no file contents, which a null root is the only signal of
		sourceLines = rootAST == null ? List.of() : List.of(getLines());
		scope = ResolutionScope.of(rootAST);
	}

	@Override
	protected void finishFile(@Nullable DetailAST rootAST) {
		scope = ResolutionScope.EMPTY;
		sourceLines = List.of();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_FOR, TokenTypes.METHOD_CALL};
	}

	@Override
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		final var op = classify(ast, sourceLines, scope);
		if (op != null)
			log(ast, messageKey(op.kind()), op.first(), op.second());
	}
}