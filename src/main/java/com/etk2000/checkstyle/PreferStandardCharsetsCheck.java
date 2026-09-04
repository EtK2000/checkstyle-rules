package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
public class PreferStandardCharsetsCheck extends AbstractResolvingCheck {
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
					map.put(charset.name().toLowerCase(Locale.ROOT), fieldName);
					for (var alias : charset.aliases())
						map.put(alias.toLowerCase(Locale.ROOT), fieldName);
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
		return standardCharsetConstant(text.substring(1, text.length() - 1));
	}

	/**
	 * Maps a charset name or alias to the matching {@code StandardCharsets} field name, or null
	 * when no constant covers it. Matching is case-insensitive under {@link Locale#ROOT}, since
	 * charset names are ASCII protocol identifiers: a locale-sensitive lowercase would map the
	 * {@code I} in {@code ISO-8859-1} and {@code US-ASCII} to a dotless {@code \u0131} under a
	 * Turkish default locale and stop matching them.
	 *
	 * <p>This is the single source of truth for the mapping; {@code PreferStandardCharsetsFixer}
	 * calls it rather than deriving its own.</p>
	 *
	 * @param charsetName the unquoted charset name
	 */
	@CheckReturnValue
	@Nullable
	public static String standardCharsetConstant(@Nonnull String charsetName) {
		return CHARSET_MAP.get(charsetName.toLowerCase(Locale.ROOT));
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

		final var fqcn = resolve(className);
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
				receiverTypeName = receiverTypeName(ast);
		}
		else if (firstChild.getType() == TokenTypes.IDENT)
			methodName = firstChild.getText();
		else
			return -1;

		if (receiverTypeName == null)
			return -1;

		final var fqcn = resolve(receiverTypeName);
		if (fqcn == null)
			return -1;

		return ReflectionUtil.findCharsetStringArgIndex(fqcn, methodName, argCount);
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.IMPORT, TokenTypes.LITERAL_NEW, TokenTypes.METHOD_CALL, TokenTypes.PACKAGE_DEF};
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
	protected void visitScopedToken(@Nonnull DetailAST ast) {
		if (minSdkAtLeast(MIN_SDK_STANDARD_CHARSETS))
			visitCall(ast);
	}
}