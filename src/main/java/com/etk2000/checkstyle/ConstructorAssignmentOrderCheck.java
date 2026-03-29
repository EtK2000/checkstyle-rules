package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces field assignments in constructors and
 * instance initializers are ordered alphabetically in two chunks:
 * (1) simple one-line assignments, then (2) multi-line assignments.
 * Within each chunk, assignments must be alphabetical by field name.
 */
public class ConstructorAssignmentOrderCheck extends AbstractCheck {
	private static final String MSG_MULTI_BEFORE_SIMPLE = "constructor.assign.multi.before.simple";
	private static final String MSG_ORDER = "constructor.assign.order";

	@CheckReturnValue
	@Nullable
	private static String extractFieldName(@Nonnull DetailAST exprStatement) {
		// look for: EXPR -> ASSIGN -> DOT(LITERAL_THIS, IDENT)
		final var expr = exprStatement.getFirstChild();
		if (expr == null || expr.getType() != TokenTypes.ASSIGN)
			return null;

		final var lhs = expr.getFirstChild();
		if (lhs == null || lhs.getType() != TokenTypes.DOT)
			return null;

		final var thisToken = lhs.getFirstChild();
		if (thisToken == null || thisToken.getType() != TokenTypes.LITERAL_THIS)
			return null;

		final var fieldIdent = thisToken.getNextSibling();
		if (fieldIdent == null || fieldIdent.getType() != TokenTypes.IDENT)
			return null;

		return fieldIdent.getText();
	}

	@CheckReturnValue
	private static boolean isMultiLine(@Nonnull DetailAST statement) {
		return AstUtil.lastLine(statement) > statement.getLineNo();
	}

	private void checkAssignmentOrder(@Nonnull DetailAST body) {
		final var simpleAssignments = new ArrayList<DetailAST>();
		final var multiAssignments = new ArrayList<DetailAST>();
		var seenMulti = false;

		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.EXPR)
				continue;

			final var fieldName = extractFieldName(child);
			if (fieldName == null)
				continue;

			if (isMultiLine(child)) {
				seenMulti = true;
				multiAssignments.add(child);
			}
			else {
				if (seenMulti)
					log(child, MSG_MULTI_BEFORE_SIMPLE, fieldName);
				simpleAssignments.add(child);
			}
		}

		checkChunkOrder(simpleAssignments);
		checkChunkOrder(multiAssignments);
	}

	private void checkChunkOrder(@Nonnull ArrayList<DetailAST> assignments) {
		String prevName = null;
		for (var assignment : assignments) {
			final var name = extractFieldName(assignment);
			if (prevName != null && name != null && name.compareToIgnoreCase(prevName) < 0)
				log(assignment, MSG_ORDER, name, prevName);
			prevName = name;
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
				TokenTypes.COMPACT_CTOR_DEF,
				TokenTypes.CTOR_DEF,
				TokenTypes.INSTANCE_INIT
		};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var body = ast.findFirstToken(TokenTypes.SLIST);
		if (body != null)
			checkAssignmentOrder(body);
	}
}