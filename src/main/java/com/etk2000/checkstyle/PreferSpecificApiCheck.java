package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags generic API calls where a more specific
 * method is available. Currently detects:
 * <ul>
 *     <li>{@code .get(0)} → use {@code .getFirst()}</li>
 *     <li>{@code .get(size() - 1)} → use {@code .getLast()}</li>
 * </ul>
 * Suppresses {@code .get(0)} when the same receiver also calls
 * {@code .get(N)} with other indices in the same method scope
 * (sequential access pattern).
 */
public class PreferSpecificApiCheck extends AbstractCheck {
	private static final String MSG_GET_FIRST = "prefer.api.getFirst";
	private static final String MSG_GET_LAST = "prefer.api.getLast";

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

	private static void collectGetCalls(@Nonnull DetailAST ast, @Nonnull ArrayList<DetailAST> results) {
		if (ast.getType() == TokenTypes.METHOD_CALL && isGetCall(ast))
			results.add(ast);
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			collectGetCalls(child, results);
	}

	@CheckReturnValue
	@Nonnull
	private static String getArgText(@Nonnull DetailAST methodCall) {
		final var elist = methodCall.findFirstToken(TokenTypes.ELIST);
		if (elist == null || elist.getChildCount() != 1)
			return "";
		return childText(elist.getFirstChild());
	}

	@CheckReturnValue
	private static boolean isGetCall(@Nonnull DetailAST methodCall) {
		final var dot = methodCall.findFirstToken(TokenTypes.DOT);
		if (dot == null)
			return false;

		// last child of DOT is the method name
		var last = dot.getFirstChild();
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return "get".equals(last.getText());
	}

	@CheckReturnValue
	private static boolean isLiteralZero(@Nonnull DetailAST expr) {
		// handle EXPR wrapper
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		return inner != null
				&& inner.getType() == TokenTypes.NUM_INT
				&& "0".equals(inner.getText());
	}

	@CheckReturnValue
	private static boolean isSizeMinusOne(@Nonnull DetailAST expr, @Nonnull DetailAST dot) {
		// handle EXPR wrapper
		final var inner = expr.getType() == TokenTypes.EXPR ? expr.getFirstChild() : expr;
		if (inner == null || inner.getType() != TokenTypes.MINUS)
			return false;

		final var left = inner.getFirstChild();
		final var right = left != null ? left.getNextSibling() : null;
		if (left == null || right == null)
			return false;

		// right side must be literal 1
		if (right.getType() != TokenTypes.NUM_INT || !"1".equals(right.getText()))
			return false;

		// left side must be a .size() call on the same receiver
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

		// check that the receiver of .size() matches the receiver of .get()
		final var getReceiver = receiverText(dot);
		final var sizeReceiver = receiverText(sizeDot);
		return !getReceiver.isEmpty() && getReceiver.equals(sizeReceiver);
	}

	@CheckReturnValue
	@Nonnull
	private static String receiverText(@Nonnull DetailAST dot) {
		// the receiver is everything in the DOT except the last child (the method name)
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

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.COMPACT_CTOR_DEF,
				TokenTypes.CTOR_DEF,
				TokenTypes.INSTANCE_INIT,
				TokenTypes.METHOD_DEF,
				TokenTypes.STATIC_INIT
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var getCalls = new ArrayList<DetailAST>();
		collectGetCalls(ast, getCalls);
		if (getCalls.isEmpty())
			return;

		// group by receiver, track whether each receiver has non-zero-index .get() calls
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
			if (!receiversWithOtherIndices.containsKey(receiver))
				log(call, MSG_GET_FIRST);
		}

		// always flag .get(size() - 1)
		for (var call : lastGets)
			log(call, MSG_GET_LAST);
	}
}