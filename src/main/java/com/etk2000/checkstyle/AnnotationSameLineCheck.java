package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces inline annotation formatting: all annotations
 * on the same line as the declaration and in alphabetical order. Applies to
 * method parameters, constructor parameters, catch parameters, lambda
 * parameters, record components, and for-each iteration variables.
 */
public class AnnotationSameLineCheck extends AbstractCheck {
	private static final String MSG_KEY = "annotation.same.line";
	private static final String MSG_ORDER = "annotation.alphabetical.order";

	@CheckReturnValue
	private static int declarationLine(@Nonnull DetailAST modifiersOrAnnotations) {
		// for ANNOTATIONS (record components), find the sibling type element
		if (modifiersOrAnnotations.getType() == TokenTypes.ANNOTATIONS) {
			final var nextSibling = modifiersOrAnnotations.getNextSibling();
			if (nextSibling != null)
				return nextSibling.getLineNo();
			return modifiersOrAnnotations.getParent().getLineNo();
		}

		final var parent = modifiersOrAnnotations.getParent();
		final var type = parent.findFirstToken(TokenTypes.TYPE);
		if (type != null)
			return type.getLineNo();

		final var ident = parent.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getLineNo();

		return parent.getLineNo();
	}

	@CheckReturnValue
	private static boolean isSameLineContext(@Nonnull DetailAST ast) {
		final var parent = ast.getParent();
		if (parent == null)
			return false;

		// ANNOTATIONS token is used by RECORD_COMPONENT_DEF instead of MODIFIERS
		if (ast.getType() == TokenTypes.ANNOTATIONS)
			return parent.getType() == TokenTypes.RECORD_COMPONENT_DEF;

		return switch (parent.getType()) {
			case TokenTypes.PARAMETER_DEF -> true;
			case TokenTypes.VARIABLE_DEF -> {
				final var grandparent = parent.getParent();
				yield grandparent != null
						&& (grandparent.getType() == TokenTypes.FOR_EACH_CLAUSE
						|| grandparent.getType() == TokenTypes.FOR_INIT);
			}
			default -> false;
		};
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.ANNOTATIONS, TokenTypes.MODIFIERS};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (!isSameLineContext(ast))
			return;

		final var annotations = AstUtil.collectAnnotations(ast);
		if (annotations.isEmpty())
			return;

		final var declLine = declarationLine(ast);

		// check same-line violations
		for (final var annotation : annotations) {
			if (annotation.getLineNo() != declLine) {
				log(annotation, MSG_KEY, AstUtil.annotationName(annotation));
				return;
			}
		}

		// check alphabetical order (only when all annotations are inline)
		String previousName = null;
		for (final var annotation : annotations) {
			final var name = AstUtil.annotationName(annotation);
			if (previousName != null && name.compareTo(previousName) < 0)
				log(annotation, MSG_ORDER, name, previousName);
			previousName = name;
		}
	}
}