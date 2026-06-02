package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces optimal lambda parameter types:
 * <ul>
 *   <li>Prefer implicit types over explicit types or {@code var}
 *       when no annotations are present</li>
 *   <li>Prefer {@code var} over explicit types when annotations are present
 *       (annotations require {@code var} or explicit types)</li>
 *   <li>Prefer naked single parameter ({@code x ->}) over
 *       parenthesized ({@code (x) ->})</li>
 * </ul>
 */
public class LambdaParameterTypeCheck extends AbstractAstCheck {
	static final String MSG_IMPLICIT = "lambda.param.use.implicit";
	static final String MSG_PARENS = "lambda.param.unnecessary.parens";
	static final String MSG_VAR = "lambda.param.use.var";

	@CheckReturnValue
	private static List<DetailAST> collectParamDefs(@Nonnull DetailAST parameters) {
		final var paramDefs = new ArrayList<DetailAST>();
		for (var child = parameters.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.PARAMETER_DEF)
				paramDefs.add(child);
		}
		return paramDefs;
	}

	@CheckReturnValue
	@Nonnull
	private static String getTypeName(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";
		final var child = type.getFirstChild();
		if (child == null)
			return "";
		final var primitiveName = switch (child.getType()) {
			case TokenTypes.LITERAL_BOOLEAN -> "boolean";
			case TokenTypes.LITERAL_BYTE -> "byte";
			case TokenTypes.LITERAL_CHAR -> "char";
			case TokenTypes.LITERAL_DOUBLE -> "double";
			case TokenTypes.LITERAL_FLOAT -> "float";
			case TokenTypes.LITERAL_INT -> "int";
			case TokenTypes.LITERAL_LONG -> "long";
			case TokenTypes.LITERAL_SHORT -> "short";
			default -> null;
		};
		if (primitiveName != null)
			return primitiveName;
		return AstUtil.typeText(type);
	}

	@CheckReturnValue
	private static boolean hasAnnotations(@Nonnull DetailAST paramDef) {
		final var mods = paramDef.findFirstToken(TokenTypes.MODIFIERS);
		return mods != null && mods.findFirstToken(TokenTypes.ANNOTATION) != null;
	}

	@CheckReturnValue
	private static boolean isImplicitType(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		return type == null || type.getFirstChild() == null;
	}

	@CheckReturnValue
	private static boolean isVarType(@Nonnull DetailAST paramDef) {
		final var type = paramDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		return ident != null && "var".equals(ident.getText());
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.LAMBDA};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return;

		final var paramDefs = collectParamDefs(params);
		if (paramDefs.isEmpty())
			return;

		// check for unnecessary parentheses on single implicit parameter
		if (paramDefs.size() == 1 && isImplicitType(paramDefs.getFirst()) && !hasAnnotations(paramDefs.getFirst())) {
			log(ast, MSG_PARENS);
			return;
		}

		// determine if any parameter has annotations
		var anyAnnotated = false;
		for (var param : paramDefs) {
			if (hasAnnotations(param)) {
				anyAnnotated = true;
				break;
			}
		}

		for (var param : paramDefs) {
			if (isImplicitType(param))
				continue;

			if (anyAnnotated) {
				if (!isVarType(param))
					log(param, MSG_VAR, getTypeName(param));
			}
			else
				log(param, MSG_IMPLICIT, getTypeName(param));
		}
	}
}