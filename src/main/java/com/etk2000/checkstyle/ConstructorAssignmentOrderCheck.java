package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashSet;

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
	private static final String MSG_DEPENDENCY = "constructor.assign.dependency";
	private static final String MSG_MULTI_BEFORE_SIMPLE = "constructor.assign.multi.before.simple";
	private static final String MSG_ORDER = "constructor.assign.order";

	private static void collectFieldReferences(@Nonnull DetailAST ast, @Nonnull HashSet<String> result) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			// look for this.field references
			if (child.getType() == TokenTypes.DOT) {
				final var thisToken = child.getFirstChild();
				if (thisToken != null && thisToken.getType() == TokenTypes.LITERAL_THIS) {
					final var fieldIdent = thisToken.getNextSibling();
					if (fieldIdent != null && fieldIdent.getType() == TokenTypes.IDENT)
						result.add(fieldIdent.getText());
				}
			}
			collectFieldReferences(child, result);
		}
	}

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
		if (assignments.size() < 2)
			return;

		// collect assigned field names and their RHS dependencies
		final var assignedFields = new HashSet<String>();
		for (var assignment : assignments) {
			final var name = extractFieldName(assignment);
			if (name != null)
				assignedFields.add(name);
		}

		for (var i = 1; i < assignments.size(); ++i) {
			final var prev = assignments.get(i - 1);
			final var curr = assignments.get(i);
			final var prevName = extractFieldName(prev);
			final var currName = extractFieldName(curr);
			if (prevName == null || currName == null)
				continue;

			// collect this.field references on the RHS of each assignment
			final var currRefs = new HashSet<String>();
			final var assign = curr.getFirstChild();
			if (assign != null && assign.getChildCount() > 1)
				collectFieldReferences(assign.getLastChild(), currRefs);
			currRefs.retainAll(assignedFields);

			final var prevRefs = new HashSet<String>();
			final var prevAssign = prev.getFirstChild();
			if (prevAssign != null && prevAssign.getChildCount() > 1)
				collectFieldReferences(prevAssign.getLastChild(), prevRefs);
			prevRefs.retainAll(assignedFields);

			// curr depends on prev: ordering is justified
			if (currRefs.contains(prevName))
				continue;

			// prev depends on curr: curr should be assigned first
			if (prevRefs.contains(currName)) {
				log(prev, MSG_DEPENDENCY, prevName, currName);
				continue;
			}

			if (currName.compareToIgnoreCase(prevName) < 0)
				log(curr, MSG_ORDER, currName, prevName);
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