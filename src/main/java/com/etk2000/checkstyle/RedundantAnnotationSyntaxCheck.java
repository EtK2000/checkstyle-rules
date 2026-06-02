package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags redundant annotation syntax:
 * <ul>
 *   <li>{@code @A()} should be {@code @A} (empty parentheses)</li>
 *   <li>{@code @A(value = x)} should be {@code @A(x)} (explicit {@code value} key)</li>
 * </ul>
 */
public class RedundantAnnotationSyntaxCheck extends AbstractAstCheck {
	private static final String MSG_EMPTY_PARENS = "annotation.syntax.empty.parens";
	private static final String MSG_EXPLICIT_VALUE = "annotation.syntax.explicit.value";

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATION};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var lparen = ast.findFirstToken(TokenTypes.LPAREN);
		if (lparen == null)
			return;

		final var name = AstUtil.annotationName(ast);

		if (ast.findFirstToken(TokenTypes.RPAREN) != null
				&& ast.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) == null
				&& ast.findFirstToken(TokenTypes.EXPR) == null
				&& ast.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT) == null
				&& ast.findFirstToken(TokenTypes.ANNOTATION) == null) {
			log(ast, MSG_EMPTY_PARENS, name);
			return;
		}

		final var pair = ast.findFirstToken(TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR);
		if (pair == null)
			return;

		if (ast.findFirstToken(TokenTypes.COMMA) != null)
			return;

		final var key = pair.findFirstToken(TokenTypes.IDENT);
		if (key != null && "value".equals(key.getText()))
			log(ast, MSG_EXPLICIT_VALUE, name);
	}
}