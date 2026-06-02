package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags charset string usage and suggests
 * {@code StandardCharsets} constants instead. Detects:
 * <ul>
 *   <li>Known charset-accepting methods/constructors (discovered via
 *       reflection) called with any non-{@code StandardCharsets} argument
 *       (string literals, String-typed variables, fields, parameters)</li>
 *   <li>String literals matching charset names passed as arguments to
 *       any method/constructor</li>
 * </ul>
 *
 * <p>Respects {@code minSdk}: {@code StandardCharsets} requires Android
 * API 19+, so the check is suppressed when {@code minSdk < 19}.</p>
 */
public class PreferStandardCharsetsCheck extends AbstractAstCheck {
	private static final int MIN_SDK_STANDARD_CHARSETS = 19;
	private static final Map<String, String> CHARSET_MAP = buildCharsetMap();
	private static final String MSG_GENERIC = "prefer.standard.charsets.string";
	private static final String MSG_SPECIFIC = "prefer.standard.charsets";

	@Nonnull
	private static Map<String, String> buildCharsetMap() {
		final var map = new HashMap<String, String>();
		for (var field : StandardCharsets.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType() == Charset.class) {
				try {
					final var charset = (Charset) field.get(null);
					final var fieldName = field.getName();
					map.put(charset.name().toLowerCase(), fieldName);
					for (var alias : charset.aliases())
						map.put(alias.toLowerCase(), fieldName);
				}
				catch (IllegalAccessException ignored) {
				}
			}
		}
		return Map.copyOf(map);
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST getArgExpr(@Nonnull DetailAST elist, int index) {
		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.EXPR) {
				if (i == index)
					return child.getFirstChild();
				++i;
			}
		}
		return null;
	}

	@CheckReturnValue
	private static boolean isStandardCharsetsRef(@Nonnull DetailAST expr) {
		if (expr.getType() == TokenTypes.DOT) {
			final var left = expr.getFirstChild();
			return left != null
					&& left.getType() == TokenTypes.IDENT
					&& "StandardCharsets".equals(left.getText());
		}
		return false;
	}

	/**
	 * Returns whether the IDENT expression resolves to a {@code String}-typed
	 * variable, field, or parameter. Returns {@code false} for {@code var}
	 * declarations or when the type cannot be determined.
	 */
	@CheckReturnValue
	private static boolean isStringIdent(@Nonnull DetailAST ident) {
		if (ident.getType() != TokenTypes.IDENT)
			return false;
		final var typeName = AstUtil.resolveVariableType(ident, ident.getText());
		return "String".equals(typeName) || "java.lang.String".equals(typeName);
	}

	/**
	 * If the expression is a string literal matching a known charset
	 * name, returns the {@code StandardCharsets} field name; otherwise null.
	 */
	@CheckReturnValue
	@Nullable
	private static String matchCharsetLiteral(@Nonnull DetailAST expr) {
		if (expr.getType() != TokenTypes.STRING_LITERAL)
			return null;
		final var text = expr.getText();
		return CHARSET_MAP.get(text.substring(1, text.length() - 1).toLowerCase());
	}

	private final Set<String> imports = new HashSet<>();

	private int minSdk = Integer.MAX_VALUE;
	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
	}

	/**
	 * Uses reflection to find the index of a {@code String} charset parameter
	 * in a constructor call, or -1 if no such parameter exists.
	 */
	@CheckReturnValue
	private int findConstructorCharsetArgIndex(@Nonnull DetailAST ast, int argCount) {
		final var className = AstUtil.findNewClassName(ast);
		if (className == null)
			return -1;

		final var fqcn = ReflectionUtil.resolveClassName(className, packageName, imports);
		if (fqcn == null)
			return -1;

		return ReflectionUtil.findCharsetStringArgIndex(fqcn, "new", argCount);
	}

	/**
	 * Uses reflection to find the index of a {@code String} charset parameter
	 * in a method call, or -1 if no such parameter exists.
	 */
	@CheckReturnValue
	private int findMethodCharsetArgIndex(@Nonnull DetailAST ast, int argCount) {
		final var firstChild = ast.getFirstChild();
		if (firstChild == null)
			return -1;

		final String methodName;
		String receiverTypeName = null;
		if (firstChild.getType() == TokenTypes.DOT) {
			methodName = firstChild.getLastChild().getText();
			final var receiver = firstChild.getFirstChild();

			if (receiver != null && receiver.getType() == TokenTypes.STRING_LITERAL)
				receiverTypeName = "String";
			else if (receiver != null && receiver.getType() == TokenTypes.LITERAL_NEW)
				receiverTypeName = AstUtil.findNewClassName(receiver);
			else
				receiverTypeName = AstUtil.getReceiverTypeName(ast, packageName, imports);
		}
		else if (firstChild.getType() == TokenTypes.IDENT)
			methodName = firstChild.getText();
		else
			return -1;

		if (receiverTypeName == null)
			return -1;

		final var fqcn = ReflectionUtil.resolveClassName(receiverTypeName, packageName, imports);
		if (fqcn == null)
			return -1;

		return ReflectionUtil.findCharsetStringArgIndex(fqcn, methodName, argCount);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.IMPORT, TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.PACKAGE_DEF};
	}

	/**
	 * Sets the minimum SDK version for the target platform.
	 * {@code StandardCharsets} requires Android API 19+.
	 * <p>Called by Checkstyle via reflection when {@code minSdk} is set in the config.</p>
	 */
	@SuppressWarnings("unused")
	public void setMinSdk(int minSdk) {
		this.minSdk = minSdk;
	}

	private void visitCall(@Nonnull DetailAST ast) {
		final var elist = ast.findFirstToken(TokenTypes.ELIST);
		if (elist == null)
			return;

		final var argCount = AstUtil.countArguments(elist);
		final var charsetArgIndex = ast.getType() == TokenTypes.LITERAL_NEW
				? findConstructorCharsetArgIndex(ast, argCount)
				: findMethodCharsetArgIndex(ast, argCount);

		if (charsetArgIndex >= 0) {
			final var arg = getArgExpr(elist, charsetArgIndex);
			if (arg != null && !isStandardCharsetsRef(arg)) {
				final var constant = matchCharsetLiteral(arg);
				if (constant != null)
					log(arg, MSG_SPECIFIC, arg.getText().substring(1, arg.getText().length() - 1), constant);
				else if (isStringIdent(arg))
					log(arg, MSG_GENERIC, arg.getText());
			}
		}

		var i = 0;
		for (var child = elist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.EXPR)
				continue;
			if (i != charsetArgIndex) {
				final var expr = child.getFirstChild();
				if (expr != null) {
					final var constant = matchCharsetLiteral(expr);
					if (constant != null)
						log(expr, MSG_SPECIFIC, expr.getText().substring(1, expr.getText().length() - 1), constant);
				}
			}
			++i;
		}
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());

			case TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL -> {
				if (minSdk >= MIN_SDK_STANDARD_CHARSETS)
					visitCall(ast);
			}

			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
		}
	}
}