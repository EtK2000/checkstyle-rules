package com.etk2000.checkstyle;

import com.etk2000.checkstyle.format.ArgLayoutClassifier;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Remembers which identifiers in the file currently being walked hold an Android {@code Context},
 * so that a {@code getString} / {@code getResources().getQuantityString} call on one of them can be
 * recognized as a special inline-block argument by the layout rules.
 * <p>
 * A name qualifies two ways: a parameter declared as {@code Context}, or a local assigned from
 * {@code requireContext()}/{@code getContext()}/{@code requireActivity()}/{@code getActivity()}.
 * <p>
 * The two ways of filling it differ in timing, not in what qualifies: a running check feeds names in
 * as its walk reaches them, so it matches a call only against names declared before it, while
 * {@link #prime} collects the whole tree up front. A call that precedes its receiver's declaration is
 * therefore classified one way by the check and the other by a fixer-side caller.
 */
final class ContextReceiverIndex {
	private static final Set<String> CONTEXT_RETURNING_METHODS = Set.of(
			"getActivity", "getContext", "requireActivity", "requireContext"
	);

	@CheckReturnValue
	private static boolean isContextType(@Nonnull DetailAST type) {
		// the base type's IDENT is a direct child of TYPE alongside the ARRAY_DECLARATOR rather than
		// under it, so both branches below would accept Context[]
		if (type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null)
			return false;

		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null && "Context".equals(ident.getText()))
			return true;

		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			for (var child = dot.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.IDENT && "Context".equals(child.getText()) && child.getNextSibling() == null)
					return true;
			}
		}
		return false;
	}

	private final Set<String> names = new HashSet<>();

	void clear() {
		names.clear();
	}

	void collectParameters(@Nonnull DetailAST methodOrCtor) {
		final var params = methodOrCtor.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			// a varargs parameter's type is an array, and its ELLIPSIS is a sibling of TYPE rather than
			// part of it, so the type node alone cannot rule it out
			if (param.findFirstToken(TokenTypes.ELLIPSIS) != null)
				continue;

			final var type = param.findFirstToken(TokenTypes.TYPE);
			if (type != null && isContextType(type)) {
				final var ident = param.findFirstToken(TokenTypes.IDENT);
				if (ident != null)
					names.add(ident.getText());
			}
		}
	}

	void collectVariable(@Nonnull DetailAST varDef) {
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return;

		final var expr = assign.getFirstChild();
		if (expr == null || expr.getType() != TokenTypes.EXPR)
			return;

		final var methodCall = expr.getFirstChild();
		if (methodCall == null || methodCall.getType() != TokenTypes.METHOD_CALL)
			return;

		final var callFirst = methodCall.getFirstChild();
		if (callFirst == null)
			return;

		String methodName = null;
		if (callFirst.getType() == TokenTypes.IDENT)
			methodName = callFirst.getText();
		else if (callFirst.getType() == TokenTypes.DOT) {
			// getLastChild, not getNextSibling: an explicit type witness
			// (receiver.<T>getContext()) inserts a TYPE_ARGUMENTS node before the name
			final var dotMethod = callFirst.getLastChild();
			if (dotMethod != null)
				methodName = dotMethod.getText();
		}

		if (methodName != null && CONTEXT_RETURNING_METHODS.contains(methodName)) {
			final var ident = varDef.findFirstToken(TokenTypes.IDENT);
			if (ident != null)
				names.add(ident.getText());
		}
	}

	@CheckReturnValue
	private boolean hasKnownReceiver(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null)
			return false;

		if (receiver.getType() == TokenTypes.IDENT && names.contains(receiver.getText()))
			return true;

		if (receiver.getType() == TokenTypes.METHOD_CALL) {
			final var callName = receiver.getFirstChild();
			if (callName != null && callName.getType() == TokenTypes.IDENT
					&& CONTEXT_RETURNING_METHODS.contains(callName.getText()))
				return true;

			if (callName != null && callName.getType() == TokenTypes.DOT) {
				// getLastChild, not getNextSibling: an explicit type witness
				// (receiver.<T>getContext()) inserts a TYPE_ARGUMENTS node before the name
				final var innerMethod = callName.getLastChild();
				if (innerMethod != null && CONTEXT_RETURNING_METHODS.contains(innerMethod.getText()))
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	boolean isContextSpecial(@Nonnull DetailAST methodCall) {
		return isGetStringCall(methodCall) || isGetQuantityStringCall(methodCall);
	}

	@CheckReturnValue
	private boolean isGetQuantityStringCall(@Nonnull DetailAST methodCall) {
		if (!ArgLayoutClassifier.isMethodCallNamed(methodCall, "getQuantityString"))
			return false;

		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.DOT)
			return false;

		final var receiver = firstChild.getFirstChild();
		if (receiver == null || receiver.getType() != TokenTypes.METHOD_CALL
				|| !ArgLayoutClassifier.isMethodCallNamed(receiver, "getResources"))
			return false;

		return hasKnownReceiver(receiver);
	}

	@CheckReturnValue
	private boolean isGetStringCall(@Nonnull DetailAST methodCall) {
		if (!ArgLayoutClassifier.isMethodCallNamed(methodCall, "getString"))
			return false;

		final var firstChild = methodCall.getFirstChild();

		// bare getString(...): can't know if the receiver is a Context
		return firstChild.getType() != TokenTypes.IDENT && hasKnownReceiver(methodCall);
	}

	/**
	 * Collects every qualifying name at or below {@code root}, which must be the compilation unit's
	 * root: this descends only, where the coordinate lookups that consume the result
	 * ({@link com.etk2000.checkstyle.ast.AstQuery#findNodeAt}) ascend to the root first, so a subtree
	 * would index fewer names than those lookups can reach. Adds to whatever is already collected.
	 */
	void prime(@Nonnull DetailAST root) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			if (node.getType() == TokenTypes.METHOD_DEF || node.getType() == TokenTypes.CTOR_DEF)
				collectParameters(node);
			else if (node.getType() == TokenTypes.VARIABLE_DEF)
				collectVariable(node);
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
	}
}