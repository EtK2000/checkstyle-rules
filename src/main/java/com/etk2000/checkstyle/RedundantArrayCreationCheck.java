package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags redundant explicit array creation when calling varargs methods
 * or constructors. For example, {@code String.join(",", new CharSequence[]{"a", "b"})}
 * should be {@code String.join(",", "a", "b")}.
 *
 * <p>Uses reflection to confirm the target method/constructor is actually
 * varargs before flagging. Primitive arrays passed to reference-type varargs
 * are skipped because removing the array wrapper would change autoboxing
 * behavior.
 */
public class RedundantArrayCreationCheck extends AbstractCheck {
	private static final String MSG = "redundant.array.creation";

	@CheckReturnValue
	private static int countArgs(@Nonnull DetailAST elist) {
		var count = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				++count;
		}
		return count;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findLastExpr(@Nonnull DetailAST elist) {
		DetailAST last = null;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR)
				last = child;
		}
		return last;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findRedundantArrayArg(@Nonnull DetailAST elist) {
		final var lastExpr = findLastExpr(elist);
		if (lastExpr == null)
			return null;

		final var inner = lastExpr.getFirstChild();
		if (inner == null || inner.getType() != TokenTypes.LITERAL_NEW)
			return null;

		if (inner.findFirstToken(TokenTypes.ARRAY_INIT) == null)
			return null;

		return inner;
	}

	@CheckReturnValue
	@Nullable
	private static String getMethodName(@Nonnull DetailAST methodCall) {
		final var firstChild = methodCall.getFirstChild();
		if (firstChild == null)
			return null;
		if (firstChild.getType() == TokenTypes.IDENT)
			return firstChild.getText();
		if (firstChild.getType() == TokenTypes.DOT) {
			var last = firstChild.getFirstChild();
			if (last == null)
				return null;
			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getType() == TokenTypes.IDENT ? last.getText() : null;
		}
		return null;
	}

	@CheckReturnValue
	private static boolean isPrimitiveArray(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
				     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE,
				     TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
				     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT ->
					{ return true; }
			}
		}
		return false;
	}

	private final Set<String> imports = new HashSet<>();

	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
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
				TokenTypes.IMPORT,
				TokenTypes.LITERAL_NEW,
				TokenTypes.METHOD_CALL,
				TokenTypes.PACKAGE_DEF
		};
	}

	@CheckReturnValue
	@Nullable
	private String getReceiverClassName(@Nonnull DetailAST methodCall) {
		final var receiverTypeName = AstUtil.getReceiverTypeName(methodCall, packageName, imports);
		if (receiverTypeName == null)
			return null;
		return ReflectionUtil.resolveClassName(receiverTypeName, packageName, imports);
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	private void visitConstructorCall(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		final var arrayNew = findRedundantArrayArg(elist);
		if (arrayNew == null)
			return;

		final var className = AstUtil.findNewClassName(ast);
		if (className == null)
			return;

		final var fqcn = ReflectionUtil.resolveClassName(className, packageName, imports);
		if (fqcn == null)
			return;

		final var argCount = countArgs(elist);
		final var componentType = ReflectionUtil.getVarArgsComponentType(fqcn, "new", argCount);
		if (componentType == null)
			return;

		if (isPrimitiveArray(arrayNew) && !componentType.isPrimitive())
			return;

		log(arrayNew, MSG, className);
	}

	private void visitMethodCall(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		final var arrayNew = findRedundantArrayArg(elist);
		if (arrayNew == null)
			return;

		final var methodName = getMethodName(ast);
		if (methodName == null)
			return;

		final var fqcn = getReceiverClassName(ast);
		if (fqcn == null)
			return;

		final var argCount = countArgs(elist);
		final var componentType = ReflectionUtil.getVarArgsComponentType(fqcn, methodName, argCount);
		if (componentType == null)
			return;

		if (isPrimitiveArray(arrayNew) && !componentType.isPrimitive())
			return;

		log(arrayNew, MSG, methodName);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.LITERAL_NEW -> visitConstructorCall(ast);
			case TokenTypes.METHOD_CALL -> visitMethodCall(ast);
			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
		}
	}
}