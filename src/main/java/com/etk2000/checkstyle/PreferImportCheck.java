package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags fully qualified type names, preferring
 * an import statement instead. Covers annotations, type references
 * (fields, parameters, return types, locals, generics, casts),
 * extends/implements clauses, and throws clauses.
 */
public class PreferImportCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.import";

	private static void buildQualifiedName(@Nonnull DetailAST ast, @Nonnull StringBuilder sb) {
		if (ast.getType() == TokenTypes.DOT) {
			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.TYPE_ARGUMENTS)
					continue;
				if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '.')
					sb.append('.');
				buildQualifiedName(child, sb);
			}
		}
		else if (ast.getType() != TokenTypes.TYPE_ARGUMENTS)
			sb.append(ast.getText());
	}

	@CheckReturnValue
	@Nonnull
	private static String qualifiedName(@Nonnull DetailAST dot) {
		final var sb = new StringBuilder();
		buildQualifiedName(dot, sb);
		return sb.toString();
	}

	private void checkType(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild == null)
			return;

		if (firstChild.getType() == TokenTypes.DOT)
			log(type, MSG_KEY, qualifiedName(firstChild));

		// recurse into generic type arguments (e.g. List<java.util.Map<...>>)
		findQualifiedTypesInSubtree(type);
	}

	private void findQualifiedTypesInSubtree(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.TYPE || child.getType() == TokenTypes.TYPE_ARGUMENT) {
				final var fc = child.getFirstChild();
				if (fc != null && fc.getType() == TokenTypes.DOT)
					log(child, MSG_KEY, qualifiedName(fc));
			}
			findQualifiedTypesInSubtree(child);
		}
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
				TokenTypes.ANNOTATION,
				TokenTypes.EXTENDS_CLAUSE,
				TokenTypes.IMPLEMENTS_CLAUSE,
				TokenTypes.LITERAL_THROWS,
				TokenTypes.TYPE
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.ANNOTATION -> {
				// annotation uses DOT when fully qualified (e.g. @javax.annotation.Nonnull)
				final var dot = ast.findFirstToken(TokenTypes.DOT);
				if (dot != null)
					log(ast, MSG_KEY, qualifiedName(dot));
			}
			case TokenTypes.EXTENDS_CLAUSE, TokenTypes.IMPLEMENTS_CLAUSE,
			     TokenTypes.LITERAL_THROWS -> {
				// these contain type names directly as children
				for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getType() == TokenTypes.DOT)
						log(child, MSG_KEY, qualifiedName(child));
				}
			}
			case TokenTypes.TYPE -> checkType(ast);
		}
	}
}