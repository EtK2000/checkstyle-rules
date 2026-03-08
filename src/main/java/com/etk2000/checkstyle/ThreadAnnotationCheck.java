package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces all classes, interfaces, enums, and records
 * have a thread annotation (@AnyThread, @BinderThread, @MainThread, @UiThread, @WorkerThread).
 */
public class ThreadAnnotationCheck extends AbstractCheck {
	private static final Set<String> THREAD_ANNOTATIONS = Set.of(
			"AnyThread", "BinderThread", "MainThread", "UiThread", "WorkerThread"
	);
	private static final String MSG_KEY = "thread.annotation.missing";

	@CheckReturnValue
	@Nonnull
	private static String annotationName(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

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
	private static boolean hasThreadAnnotation(@Nonnull DetailAST modifiers) {
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION && THREAD_ANNOTATIONS.contains(annotationName(child)))
				return true;
		}
		return false;
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
				TokenTypes.CLASS_DEF,
				TokenTypes.ENUM_DEF,
				TokenTypes.INTERFACE_DEF,
				TokenTypes.RECORD_DEF
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// skip inner types — only check top-level and nested-top (parent is OBJBLOCK of a file or compilation unit)
		if (ast.getParent() != null && ast.getParent().getType() == TokenTypes.OBJBLOCK)
			return;

		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null || !hasThreadAnnotation(modifiers))
			log(ast, MSG_KEY, ast.findFirstToken(TokenTypes.IDENT).getText());
	}
}