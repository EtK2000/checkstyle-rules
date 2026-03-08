package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags unnecessary {@code this.} usage.
 * {@code this.} is only allowed when:
 * <ul>
 *     <li>Assigning an instance field: {@code this.field = value}</li>
 *     <li>A parameter or local variable with the same name exists (shadowing)</li>
 * </ul>
 */
public class NoUnnecessaryThisCheck extends AbstractCheck {
	private static final String MSG_KEY = "no.unnecessary.this";

	@CheckReturnValue
	@Nonnull
	private static Set<String> collectLocalNames(@Nonnull DetailAST enclosingDef) {
		final var names = new HashSet<String>();

		// collect parameter names
		final var params = enclosingDef.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
				if (param.getType() == TokenTypes.PARAMETER_DEF) {
					final var ident = param.findFirstToken(TokenTypes.IDENT);
					if (ident != null)
						names.add(ident.getText());
				}
			}
		}

		// collect local variable names from the method/constructor body
		final var slist = enclosingDef.findFirstToken(TokenTypes.SLIST);
		if (slist != null)
			collectVariableNames(slist, names);

		return names;
	}

	private static void collectVariableNames(@Nonnull DetailAST ast, @Nonnull Set<String> names) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null)
					names.add(ident.getText());
			}
			// recurse into blocks but NOT into inner classes/lambdas
			else if (child.getType() != TokenTypes.CLASS_DEF
					&& child.getType() != TokenTypes.ENUM_DEF
					&& child.getType() != TokenTypes.LAMBDA)
				collectVariableNames(child, names);
		}
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findEnclosingMethodOrCtor(@Nonnull DetailAST ast) {
		for (var parent = ast.getParent(); parent != null; parent = parent.getParent()) {
			switch (parent.getType()) {
				case TokenTypes.METHOD_DEF, TokenTypes.CTOR_DEF, TokenTypes.COMPACT_CTOR_DEF:
					return parent;

				// stop at class boundaries
				case TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				     TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF:
					return null;
			}
		}
		return null;
	}

	@CheckReturnValue
	private static boolean isFieldAssignment(@Nonnull DetailAST dot) {
		final var parent = dot.getParent();
		return parent != null && parent.getType() == TokenTypes.ASSIGN
				&& parent.getFirstChild() == dot;
	}

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.DOT};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var firstChild = ast.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.LITERAL_THIS)
			return;

		// get the member name after this.
		final var memberIdent = firstChild.getNextSibling();
		if (memberIdent == null || memberIdent.getType() != TokenTypes.IDENT)
			return;

		final var memberName = memberIdent.getText();

		// allowed: this.field = value (assignment)
		if (isFieldAssignment(ast))
			return;

		// allowed: shadowing — a parameter or local variable with the same name
		final var enclosing = findEnclosingMethodOrCtor(ast);
		if (enclosing != null && collectLocalNames(enclosing).contains(memberName))
			return;

		log(ast, MSG_KEY, memberName);
	}
}