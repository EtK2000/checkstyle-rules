package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags C-style array declarations ({@code int x[]}) and suggests Java-style
 * ({@code int[] x}). C-style splits the type information across two locations
 * and creates ambiguity in multi-variable declarations: in {@code int alpha[],
 * beta;}, alpha is {@code int[]} but beta is {@code int}.
 *
 * <p>Applies to fields, local variables, method/constructor parameters, and
 * method return types ({@code int method()[]}). Does not flag array creation
 * expressions ({@code new int[5]}) since those are a different construct.
 */
public class ArrayTypeStyleCheck extends AbstractAstCheck {
	private static final String MSG = "array.type.style";

	@CheckReturnValue
	@Nullable
	private static DetailAST findCStyleDeclarator(@Nonnull DetailAST decl) {
		final var ident = decl.findFirstToken(TokenTypes.IDENT);
		if (ident == null)
			return null;
		final var type = decl.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		final var identLine = ident.getLineNo();
		final var identCol = ident.getColumnNo();
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR && isAfter(child, identLine, identCol))
				return child;
		}
		return null;
	}

	@CheckReturnValue
	private static boolean isAfter(@Nonnull DetailAST node, int line, int col) {
		final var nodeLine = node.getLineNo();
		if (nodeLine != line)
			return nodeLine > line;
		return node.getColumnNo() > col;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.METHOD_DEF,
				TokenTypes.PARAMETER_DEF,
				TokenTypes.RECORD_COMPONENT_DEF,
				TokenTypes.VARIABLE_DEF
		};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var cStyle = findCStyleDeclarator(ast);
		if (cStyle == null)
			return;
		final var ident = ast.findFirstToken(TokenTypes.IDENT);
		log(cStyle, MSG, ident == null ? "" : ident.getText());
	}
}