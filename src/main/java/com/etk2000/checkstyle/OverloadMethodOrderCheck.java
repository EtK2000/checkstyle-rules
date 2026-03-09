package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces overloaded methods are ordered
 * with fewer parameters first. When parameter counts are equal,
 * compares parameter types left-to-right: primitives sort before
 * reference types, alphabetical within each group.
 */
public class OverloadMethodOrderCheck extends AbstractCheck {
	private static final String MSG_KEY = "overload.method.order";
	private static final String MSG_TYPE_KEY = "overload.method.type.order";

	@CheckReturnValue
	private static int compareParamTypes(@Nonnull DetailAST a, @Nonnull DetailAST b) {
		final var aParams = getParamTypes(a);
		final var bParams = getParamTypes(b);
		final var count = aParams.size();

		for (var i = 0; i < count; ++i) {
			final var aType = aParams.get(i);
			final var bType = bParams.get(i);
			final var aPrimitive = isPrimitive(aType);
			final var bPrimitive = isPrimitive(bType);

			// primitives sort before reference types
			if (aPrimitive != bPrimitive)
				return aPrimitive ? -1 : 1;

			final var cmp = aType.compareToIgnoreCase(bType);
			if (cmp != 0)
				return cmp;
		}
		return 0;
	}

	@CheckReturnValue
	@Nonnull
	private static String formatParamTypes(@Nonnull DetailAST methodDef) {
		final var types = getParamTypes(methodDef);
		return String.join(", ", types);
	}

	@CheckReturnValue
	@Nonnull
	private static String getBaseTypeName(@Nonnull DetailAST firstChild) {
		return switch (firstChild.getType()) {
			case TokenTypes.DOT -> {
				// qualified type like java.util.List — use last segment
				var last = firstChild.getFirstChild();
				while (last.getNextSibling() != null)
					last = last.getNextSibling();
				yield last.getText();
			}
			case TokenTypes.IDENT -> firstChild.getText();
			case TokenTypes.LITERAL_BOOLEAN -> "boolean";
			case TokenTypes.LITERAL_BYTE -> "byte";
			case TokenTypes.LITERAL_CHAR -> "char";
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			case TokenTypes.LITERAL_SHORT -> "short";
			default -> firstChild.getText();
		};
	}

	@CheckReturnValue
	@Nonnull
	private static ArrayList<String> getParamTypes(@Nonnull DetailAST methodDef) {
		final var types = new ArrayList<String>();
		final var params = methodDef.findFirstToken(TokenTypes.PARAMETERS);
		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			final var type = param.findFirstToken(TokenTypes.TYPE);
			if (type != null)
				types.add(getTypeName(type));
		}
		return types;
	}

	@CheckReturnValue
	@Nonnull
	private static String getTypeName(@Nonnull DetailAST type) {
		final var first = type.getFirstChild();
		if (first == null)
			return "";

		final var base = getBaseTypeName(first);

		// count ARRAY_DECLARATOR siblings to determine array dimensions
		var arrayDimensions = 0;
		for (var sibling = first.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.ARRAY_DECLARATOR)
				++arrayDimensions;
		}

		if (arrayDimensions == 0)
			return base;

		final var sb = new StringBuilder(base);
		for (var i = 0; i < arrayDimensions; ++i)
			sb.append("[]");
		return sb.toString();
	}

	@CheckReturnValue
	private static boolean isPrimitive(@Nonnull String typeName) {
		// strip array suffixes — int[] is still a primitive type for sorting purposes
		final var base = typeName.endsWith("[]")
				? typeName.substring(0, typeName.indexOf('['))
				: typeName;
		return switch (base) {
			case "boolean", "byte", "char", "double", "float", "int", "long", "short" -> true;
			default -> false;
		};
	}

	@CheckReturnValue
	private static int paramCount(@Nonnull DetailAST methodDef) {
		return methodDef.findFirstToken(TokenTypes.PARAMETERS).getChildCount(TokenTypes.PARAMETER_DEF);
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		DetailAST prevMethod = null;
		String prevName = null;
		var prevParams = 0;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;

			final var name = child.findFirstToken(TokenTypes.IDENT).getText();
			final var params = paramCount(child);

			if (name.equals(prevName)) {
				if (params < prevParams)
					log(child, MSG_KEY, name, params, prevParams);
				else if (params == prevParams && compareParamTypes(prevMethod, child) > 0)
					log(child, MSG_TYPE_KEY, name, formatParamTypes(child), formatParamTypes(prevMethod));
			}

			prevMethod = child;
			prevName = name;
			prevParams = params;
		}
	}
}