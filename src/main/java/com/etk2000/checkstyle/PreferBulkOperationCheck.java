package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags loops that add/put/copy elements one at a time when a bulk operation exists
 * ({@code addAll}, {@code putAll}, {@code System.arraycopy}, {@code Arrays.fill}).
 */
public class PreferBulkOperationCheck extends AbstractAstCheck {
	public enum BulkKind {
		ADD_ALL,
		ARRAY_COPY,
		FILL,
		PUT_ALL
	}

	/**
	 * A bulk-operation opportunity the check reports. {@code first}/{@code second} are the display
	 * texts the message and fixer use ({@code target}/{@code source} for add/put; {@code src}/
	 * {@code dst} for arraycopy; {@code arr}/{@code value} for fill), sliced verbatim from the source
	 * so any receiver/type shape (qualified name, generics, cast) survives. The 0-based span
	 * {@code [startLine:startCol, endLine:endCol)} covers the whole {@code for} statement when
	 * {@link #statementForm}, otherwise the {@code forEach} call expression.
	 */
	public record BulkOp(
			@Nonnull BulkKind kind,
			@Nonnull String first,
			@Nonnull String second,
			int startLine,
			int startCol,
			int endLine,
			int endCol,
			boolean statementForm
	) {}

	private record KindArgs(@Nonnull BulkKind kind, @Nonnull String first, @Nonnull String second) {}

	static final String MSG_ADDALL = "prefer.bulk.addall";
	static final String MSG_ARRAYCOPY = "prefer.bulk.arraycopy";
	static final String MSG_FILL = "prefer.bulk.fill";
	static final String MSG_PUTALL = "prefer.bulk.putall";

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

		return new KindArgs(BulkKind.ADD_ALL, target, source);
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

		return new KindArgs(BulkKind.PUT_ALL, target, source);
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
				sourceExpr = child.getFirstChild();
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
			final var source = sliceNode(lines, sourceExpr);
			if (arg != null && source != null && isIdent(arg.getFirstChild(), iterVarName))
				return new KindArgs(BulkKind.ADD_ALL, target, source);
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

			final var map = sliceNode(lines, mapReceiver);
			if (map == null)
				return null;
			return new KindArgs(BulkKind.PUT_ALL, target, map);
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

		if ("put".equals(methodName))
			return new KindArgs(BulkKind.PUT_ALL, target, source);
		if ("add".equals(methodName))
			return new KindArgs(BulkKind.ADD_ALL, target, source);
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
		if (!AstUtil.exprText(getReceiverNode).equals(AstUtil.exprText(boundReceiver)))
			return null;

		final var target = sliceReceiver(lines, bodyStmt);
		final var source = sliceNode(lines, boundReceiver);
		if (target == null || source == null)
			return null;

		return new KindArgs(BulkKind.ADD_ALL, target, source);
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
			if (!AstUtil.exprText(rhsArray).equals(AstUtil.exprText(boundArrayExpr)))
				return null;

			final var src = sliceNode(lines, rhsArray);
			final var dst = sliceNode(lines, lhsArray);
			if (src == null || dst == null)
				return null;
			return new KindArgs(BulkKind.ARRAY_COPY, src, dst);
		}
		if (AstUtil.isPureExpression(rhs) && !referencesVar(rhs, loopVar)) {
			if (!AstUtil.exprText(lhsArray).equals(AstUtil.exprText(boundArrayExpr)))
				return null;

			final var arr = sliceNode(lines, lhsArray);
			final var value = sliceNode(lines, rhs);
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
		if (initValue == null || !AstUtil.isZeroLiteral(initValue))
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
		final var kindArgs = switch (ast.getType()) {
			case TokenTypes.LITERAL_FOR -> ast.findFirstToken(TokenTypes.FOR_EACH_CLAUSE) != null
					? checkForEachLoop(ast, lines)
					: checkIndexedForLoop(ast, lines);
			case TokenTypes.METHOD_CALL -> checkForEachCall(ast, lines);
			default -> null;
		};
		if (kindArgs == null)
			return null;
		final var start = spanStart(ast);
		final var end = spanEnd(ast);
		return new BulkOp(
				kindArgs.kind(),
				kindArgs.first(),
				kindArgs.second(),
				start[0],
				start[1],
				end[0],
				end[1],
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
		final var node = AstUtil.findNodeAt(
				root,
				line,
				column,
				n -> n.getType() == TokenTypes.METHOD_CALL || n.getType() == TokenTypes.LITERAL_FOR
		);
		return node == null ? null : classify(node, lines);
	}

	@CheckReturnValue
	private static int getArgCount(@Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		return elist == null ? 0 : AstUtil.countArguments(elist);
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
		return dot != null ? dot.getFirstChild() : null;
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
	@Nonnull
	private static String messageKey(@Nonnull BulkKind kind) {
		return switch (kind) {
			case ADD_ALL -> MSG_ADDALL;
			case ARRAY_COPY -> MSG_ARRAYCOPY;
			case FILL -> MSG_FILL;
			case PUT_ALL -> MSG_PUTALL;
		};
	}

	/**
	 * Whether a space is needed when joining two source fragments of a multi-line operand, so a
	 * chain break (a line ending {@code x}, the next starting {@code .y}) or a bracketed break
	 * rejoins without a stray space, while an operator/operand break keeps its separating space.
	 */
	@CheckReturnValue
	private static boolean needsSpaceBetween(char prevLast, char nextFirst) {
		return prevLast != '(' && prevLast != '['
				&& nextFirst != ')' && nextFirst != ']' && nextFirst != ',' && nextFirst != '.' && nextFirst != ';';
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

	/**
	 * Slices the source text of {@code methodRef}'s qualifier: from the qualifier's start up to the
	 * {@code ::} (the {@code METHOD_REF} node's own position), so a type witness ({@code x::<T>m})
	 * and the method name are excluded. Returns {@code null} for an empty qualifier or one that does
	 * not start on the {@code ::} line.
	 */
	@CheckReturnValue
	@Nullable
	private static String sliceMethodRefQualifier(@Nonnull List<String> lines, @Nonnull DetailAST methodRef) {
		final var first = methodRef.getFirstChild();
		if (first == null)
			return null;
		final var start = spanStart(first);
		if (start[0] != methodRef.getLineNo() - 1)
			return null;
		final var qualifier = lines.get(start[0]).substring(start[1], methodRef.getColumnNo());
		return qualifier.isEmpty() ? null : qualifier;
	}

	/**
	 * Slices the source text of {@code node}'s full span, or {@code null} when the span is
	 * multi-line and carries a comment (see {@link #sliceSpan}).
	 */
	@CheckReturnValue
	@Nullable
	private static String sliceNode(@Nonnull List<String> lines, @Nonnull DetailAST node) {
		return sliceSpan(lines, spanStart(node), spanEnd(node));
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
		return sliceSpan(lines, spanStart(first), spanEnd(lastReceiverChild));
	}

	/**
	 * Slices the source text of the span {@code [start, end)}. A single-line span is returned
	 * verbatim. A multi-line span is refused ({@code null}) when a comment falls inside it (slicing
	 * it verbatim and collapsing to one line would comment out the trailing code); otherwise its
	 * lines are joined, each stripped, with a single space where one is needed to keep adjacent
	 * tokens apart (see {@link #needsSpaceBetween}).
	 */
	@CheckReturnValue
	@Nullable
	private static String sliceSpan(@Nonnull List<String> lines, @Nonnull int[] start, @Nonnull int[] end) {
		if (start[0] == end[0])
			return lines.get(start[0]).substring(start[1], end[1]);
		// The operand starts on a code token, so the lexer state at its start column is NONE. Scan
		// only the operand region [from, to) of each line (not the whole line) so a comment BEFORE
		// the operand cannot mask one inside it, and carry the state over that region alone.
		var state = JavaLineScanner.LexerState.NONE;
		final var joined = new StringBuilder();
		for (var i = start[0]; i <= end[0]; ++i) {
			final var line = lines.get(i);
			final var from = i == start[0] ? start[1] : 0;
			final var to = i == end[0] ? end[1] : line.length();
			final var region = line.substring(from, to);
			if (state.inBlockComment())
				return null;
			if (JavaLineScanner.firstCommentMarker(region, state) >= 0)
				return null;
			final var fragment = region.strip();
			if (!fragment.isEmpty()) {
				if (!joined.isEmpty() && needsSpaceBetween(joined.charAt(joined.length() - 1), fragment.charAt(0)))
					joined.append(' ');
				joined.append(fragment);
			}
			state = JavaLineScanner.stateAfter(region, state);
		}
		return joined.isEmpty() ? null : joined.toString();
	}

	/**
	 * The 0-based {@code [line, columnAfter]} of the last token in {@code ast}'s subtree, where
	 * {@code columnAfter} is the char index immediately past that token. Tokens do not overlap, so
	 * the node whose start position is furthest right is the last token; its length gives the end.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] spanEnd(@Nonnull DetailAST ast) {
		var best = ast;
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() > best.getLineNo()
					|| (node.getLineNo() == best.getLineNo() && node.getColumnNo() > best.getColumnNo()))
				best = node;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return new int[]{best.getLineNo() - 1, best.getColumnNo() + best.getText().length()};
	}

	/**
	 * The 0-based {@code [line, column]} of the first token in {@code ast}'s subtree (its leftmost,
	 * topmost position).
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] spanStart(@Nonnull DetailAST ast) {
		var line = ast.getLineNo();
		var col = ast.getColumnNo();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getLineNo() < line || (node.getLineNo() == line && node.getColumnNo() < col)) {
				line = node.getLineNo();
				col = node.getColumnNo();
			}
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		return new int[]{line - 1, col};
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapExpr(@Nullable DetailAST ast) {
		return ast != null && ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
	}

	// held per file rather than rebuilt per token: `getDefaultTokens` registers two very common
	// tokens, so copying the whole file at each visit is quadratic in file size
	private List<String> sourceLines = List.of();

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		sourceLines = List.of(getLines());
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_FOR, TokenTypes.METHOD_CALL};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var op = classify(ast, sourceLines);
		if (op != null)
			log(ast, messageKey(op.kind()), op.first(), op.second());
	}
}