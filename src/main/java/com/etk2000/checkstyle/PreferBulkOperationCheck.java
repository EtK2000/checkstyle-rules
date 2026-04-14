package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags loops that add/put/copy elements one at a time when a bulk operation exists
 * ({@code addAll}, {@code putAll}, {@code System.arraycopy}, {@code Arrays.fill}).
 */
public class PreferBulkOperationCheck extends AbstractCheck {
	static final String MSG_ADDALL = "prefer.bulk.addall";
	static final String MSG_ARRAYCOPY = "prefer.bulk.arraycopy";
	static final String MSG_FILL = "prefer.bulk.fill";
	static final String MSG_PUTALL = "prefer.bulk.putall";

	@CheckReturnValue
	private static int getArgCount(@Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return 0;
		var count = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				++count;
		}
		return count;
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
		// For parenthesized params: skip until after RPAREN
		// For single param without parens (IDENT -> body): skip the IDENT
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
			if (child.getType() == TokenTypes.EXPR) {
				if (idx == n)
					return child;
				++idx;
			}
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
		if (getArgCount(ast) != 0)
			return false;
		return isIdent(getReceiver(ast), varName);
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
	private static boolean referencesVar(@Nonnull DetailAST ast, @Nonnull String varName) {
		// Iterative pre-order walk over the AST subtree rooted at `ast`. Avoids the
		// unbounded recursion depth that the equivalent recursive walk would incur on
		// deeply-nested generated code.
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

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapExpr(@Nullable DetailAST ast) {
		return ast != null && ast.getType() == TokenTypes.EXPR ? ast.getFirstChild() : ast;
	}

	private void checkForEachCall(@Nonnull DetailAST ast) {
		if (!"forEach".equals(getCallMethodName(ast)))
			return;

		final var sourceReceiver = getReceiver(ast);
		if (sourceReceiver == null || sourceReceiver.getType() == TokenTypes.METHOD_CALL)
			return;

		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		// Find the single argument (LAMBDA, METHOD_REF, or EXPR wrapping either)
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
			return;

		if (arg.getType() == TokenTypes.METHOD_REF)
			checkForEachMethodRef(ast, sourceReceiver, arg);
		else
			checkForEachLambdaBody(ast, sourceReceiver, arg);
	}

	private void checkForEachLambdaAdd(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST sourceReceiver,
			@Nonnull DetailAST lambda,
			@Nonnull String paramName
	) {
		final var bodyStmt = getLambdaBodyStatement(lambda);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return;
		if (!"add".equals(getCallMethodName(bodyStmt)))
			return;
		if (getArgCount(bodyStmt) != 1)
			return;

		final var arg = getNthArg(bodyStmt, 0);
		if (arg == null || !isIdent(arg.getFirstChild(), paramName))
			return;

		final var targetReceiver = getReceiver(bodyStmt);
		if (targetReceiver == null)
			return;

		log(ast, MSG_ADDALL, AstUtil.displayText(targetReceiver), AstUtil.displayText(sourceReceiver));
	}

	private void checkForEachLambdaBody(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST sourceReceiver,
			@Nonnull DetailAST lambda
	) {
		final var paramNames = new String[2];
		var paramCount = 0;
		final var params = lambda.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var child = params.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.PARAMETER_DEF) {
					final var ident = child.findFirstToken(TokenTypes.IDENT);
					if (ident == null)
						return;
					if (paramCount < paramNames.length)
						paramNames[paramCount] = ident.getText();
					++paramCount;
				}
			}
		}
		else {
			// Single param without parens: item -> ...
			final var firstChild = lambda.getFirstChild();
			if (firstChild != null && firstChild.getType() == TokenTypes.IDENT) {
				paramNames[0] = firstChild.getText();
				paramCount = 1;
			}
		}

		if (paramCount == 2 && paramNames[0] != null && paramNames[1] != null)
			checkForEachLambdaPut(ast, sourceReceiver, lambda, paramNames);
		else if (paramCount == 1 && paramNames[0] != null)
			checkForEachLambdaAdd(ast, sourceReceiver, lambda, paramNames[0]);
	}

	private void checkForEachLambdaPut(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST sourceReceiver,
			@Nonnull DetailAST lambda,
			@Nonnull String[] paramNames
	) {
		final var bodyStmt = getLambdaBodyStatement(lambda);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return;
		if (!"put".equals(getCallMethodName(bodyStmt)))
			return;
		if (getArgCount(bodyStmt) != 2)
			return;

		final var firstArg = getNthArg(bodyStmt, 0);
		if (firstArg == null || !isIdent(firstArg.getFirstChild(), paramNames[0]))
			return;
		final var secondArg = getNthArg(bodyStmt, 1);
		if (secondArg == null || !isIdent(secondArg.getFirstChild(), paramNames[1]))
			return;

		final var targetReceiver = getReceiver(bodyStmt);
		if (targetReceiver == null)
			return;

		log(ast, MSG_PUTALL, AstUtil.displayText(targetReceiver), AstUtil.displayText(sourceReceiver));
	}

	private void checkForEachLoop(@Nonnull DetailAST ast) {
		final var forEachClause = ast.findFirstToken(TokenTypes.FOR_EACH_CLAUSE);
		if (forEachClause == null)
			return;

		final var varDef = forEachClause.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef == null)
			return;
		final var iterVarIdent = varDef.findFirstToken(TokenTypes.IDENT);
		if (iterVarIdent == null)
			return;
		final var iterVarName = iterVarIdent.getText();

		DetailAST sourceExpr = null;
		for (var child = forEachClause.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR) {
				sourceExpr = child.getFirstChild();
				break;
			}
		}
		if (sourceExpr == null)
			return;

		final var bodyStmt = getSingleBodyStatement(ast);
		if (bodyStmt == null || bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return;

		final var methodName = getCallMethodName(bodyStmt);
		final var targetReceiver = getReceiver(bodyStmt);
		if (methodName == null || targetReceiver == null)
			return;

		if ("add".equals(methodName) && getArgCount(bodyStmt) == 1) {
			final var arg = getNthArg(bodyStmt, 0);
			if (arg != null && isIdent(arg.getFirstChild(), iterVarName))
				log(ast, MSG_ADDALL, AstUtil.displayText(targetReceiver), AstUtil.displayText(sourceExpr));
		}
		else if ("put".equals(methodName) && getArgCount(bodyStmt) == 2) {
			if (sourceExpr.getType() != TokenTypes.METHOD_CALL)
				return;
			if (!"entrySet".equals(getCallMethodName(sourceExpr)))
				return;
			if (getArgCount(sourceExpr) != 0)
				return;

			final var firstArg = getNthArg(bodyStmt, 0);
			final var secondArg = getNthArg(bodyStmt, 1);
			if (firstArg == null || secondArg == null)
				return;
			if (!isNoArgCallOnVar(firstArg.getFirstChild(), iterVarName, "getKey"))
				return;
			if (!isNoArgCallOnVar(secondArg.getFirstChild(), iterVarName, "getValue"))
				return;

			final var mapReceiver = getReceiver(sourceExpr);
			if (mapReceiver == null)
				return;

			log(ast, MSG_PUTALL, AstUtil.displayText(targetReceiver), AstUtil.displayText(mapReceiver));
		}
	}

	private void checkForEachMethodRef(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST sourceReceiver,
			@Nonnull DetailAST methodRef
	) {
		final var refReceiver = methodRef.getFirstChild();
		if (refReceiver == null)
			return;

		var last = refReceiver;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		if (last.getType() != TokenTypes.IDENT || last == refReceiver)
			return;
		final var methodName = last.getText();

		if ("put".equals(methodName))
			log(ast, MSG_PUTALL, AstUtil.displayText(refReceiver), AstUtil.displayText(sourceReceiver));
		else if ("add".equals(methodName))
			log(ast, MSG_ADDALL, AstUtil.displayText(refReceiver), AstUtil.displayText(sourceReceiver));
	}

	private void checkIndexedAddAll(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST bodyStmt,
			@Nonnull String loopVar,
			@Nonnull DetailAST boundReceiver
	) {
		if (bodyStmt.getType() != TokenTypes.METHOD_CALL)
			return;
		if (!"add".equals(getCallMethodName(bodyStmt)))
			return;
		if (getArgCount(bodyStmt) != 1)
			return;

		final var arg = getNthArg(bodyStmt, 0);
		if (arg == null)
			return;
		final var argInner = arg.getFirstChild();
		if (argInner == null || argInner.getType() != TokenTypes.METHOD_CALL)
			return;
		if (!"get".equals(getCallMethodName(argInner)))
			return;
		if (getArgCount(argInner) != 1)
			return;

		final var getArg = getNthArg(argInner, 0);
		if (getArg == null || !isIdent(getArg.getFirstChild(), loopVar))
			return;

		final var getReceiverNode = getReceiver(argInner);
		if (getReceiverNode == null)
			return;
		if (!AstUtil.exprText(getReceiverNode).equals(AstUtil.exprText(boundReceiver)))
			return;

		final var targetReceiver = getReceiver(bodyStmt);
		if (targetReceiver == null)
			return;

		log(ast, MSG_ADDALL, AstUtil.displayText(targetReceiver), AstUtil.displayText(boundReceiver));
	}

	private void checkIndexedArrayOp(
			@Nonnull DetailAST ast,
			@Nonnull DetailAST bodyStmt,
			@Nonnull String loopVar,
			@Nonnull DetailAST boundArrayExpr
	) {
		if (bodyStmt.getType() != TokenTypes.ASSIGN)
			return;

		final var lhs = bodyStmt.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.INDEX_OP)
			return;

		final var lhsArray = lhs.getFirstChild();
		if (lhsArray == null)
			return;
		final var lhsIndex = unwrapExpr(lhsArray.getNextSibling());
		if (!isIdent(lhsIndex, loopVar))
			return;

		final var rhs = lhs.getNextSibling();
		if (rhs == null)
			return;

		if (rhs.getType() == TokenTypes.INDEX_OP) {
			final var rhsArray = rhs.getFirstChild();
			if (rhsArray == null)
				return;
			final var rhsIndex = unwrapExpr(rhsArray.getNextSibling());
			if (!isIdent(rhsIndex, loopVar))
				return;
			if (!AstUtil.exprText(rhsArray).equals(AstUtil.exprText(boundArrayExpr)))
				return;

			log(ast, MSG_ARRAYCOPY, AstUtil.displayText(rhsArray), AstUtil.displayText(lhsArray));
		}
		else if (AstUtil.isPureExpression(rhs) && !referencesVar(rhs, loopVar)) {
			if (!AstUtil.exprText(lhsArray).equals(AstUtil.exprText(boundArrayExpr)))
				return;

			log(ast, MSG_FILL, AstUtil.displayText(lhsArray), AstUtil.displayText(rhs));
		}
	}

	private void checkIndexedForLoop(@Nonnull DetailAST ast) {
		final var forInit = ast.findFirstToken(TokenTypes.FOR_INIT);
		if (forInit == null)
			return;
		final var varDef = forInit.findFirstToken(TokenTypes.VARIABLE_DEF);
		if (varDef == null)
			return;

		var varDefCount = 0;
		for (var child = forInit.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF)
				++varDefCount;
		}
		if (varDefCount != 1)
			return;

		final var loopVarIdent = varDef.findFirstToken(TokenTypes.IDENT);
		if (loopVarIdent == null)
			return;
		final var loopVar = loopVarIdent.getText();

		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return;
		final var initExpr = assign.findFirstToken(TokenTypes.EXPR);
		if (initExpr == null)
			return;
		final var initValue = initExpr.getFirstChild();
		if (initValue == null || !AstUtil.isZeroLiteral(initValue))
			return;

		final var forCond = ast.findFirstToken(TokenTypes.FOR_CONDITION);
		if (forCond == null)
			return;
		final var condExpr = forCond.findFirstToken(TokenTypes.EXPR);
		if (condExpr == null)
			return;
		final var comparison = condExpr.getFirstChild();
		if (comparison == null || comparison.getType() != TokenTypes.LT)
			return;
		final var lhs = comparison.getFirstChild();
		if (lhs == null)
			return;
		final var rhs = lhs.getNextSibling();
		if (rhs == null || !isIdent(lhs, loopVar))
			return;

		final var forIter = ast.findFirstToken(TokenTypes.FOR_ITERATOR);
		if (forIter == null || !isSimpleIncrement(forIter, loopVar))
			return;

		final var bodyStmt = getSingleBodyStatement(ast);
		if (bodyStmt == null)
			return;

		if (rhs.getType() == TokenTypes.METHOD_CALL) {
			if ("size".equals(getCallMethodName(rhs)) && getArgCount(rhs) == 0) {
				final var boundReceiver = getReceiver(rhs);
				if (boundReceiver != null)
					checkIndexedAddAll(ast, bodyStmt, loopVar, boundReceiver);
			}
		}
		else if (rhs.getType() == TokenTypes.DOT && "length".equals(getLastIdentInDot(rhs))) {
			final var arrayExpr = rhs.getFirstChild();
			if (arrayExpr != null)
				checkIndexedArrayOp(ast, bodyStmt, loopVar, arrayExpr);
		}
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LITERAL_FOR, TokenTypes.METHOD_CALL};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.LITERAL_FOR -> {
				if (ast.findFirstToken(TokenTypes.FOR_EACH_CLAUSE) != null)
					checkForEachLoop(ast);
				else
					checkIndexedForLoop(ast);
			}
			case TokenTypes.METHOD_CALL -> checkForEachCall(ast);
		}
	}
}