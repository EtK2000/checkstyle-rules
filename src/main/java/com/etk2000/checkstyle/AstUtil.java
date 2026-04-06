package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

	@CheckReturnValue
	static boolean containsCastTo(@Nonnull DetailAST ast, @Nonnull String typeName, @Nonnull String exprText) {
		if (ast.getType() == TokenTypes.TYPECAST) {
			final var castType = ast.findFirstToken(TokenTypes.TYPE);
			final var rparen = ast.findFirstToken(TokenTypes.RPAREN);
			final var castExpr = rparen != null ? rparen.getNextSibling() : null;
			if (castType != null && castExpr != null
					&& typeName.equals(typeText(castType))
					&& exprText.equals(exprText(castExpr)))
				return true;
		}
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (containsCastTo(child, typeName, exprText))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	static String exprText(@Nonnull DetailAST ast) {
		if (ast.getChildCount() == 0)
			return ast.getText();

		final var sb = new StringBuilder();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			sb.append(exprText(child));
		return sb.toString();
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
		// simple type: IDENT (exclude "var" since it's not a real type name)
		final var ident = typeNode.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return "var".equals(ident.getText()) ? null : ident.getText();

		// primitive types
		for (var child = typeNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
				     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
				     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
				     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT -> {
					return null; // primitives can't have methods
				}
			}
		}

		// qualified type: DOT
		final var dot = typeNode.findFirstToken(TokenTypes.DOT);
		if (dot != null)
			return FullIdent.createFullIdent(dot).getText();

		return null;
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
	static int lastLine(@Nonnull DetailAST ast) {
		var last = ast.getLineNo();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var childLast = lastLine(child);
			if (childLast > last)
				last = childLast;
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

		return getTypeName(type);
	}
}