package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces field assignments in constructors and
 * instance initializers are ordered in three groups:
 * (1) simple one-line assignments without local variables,
 * (2) multi-line assignments without local variables,
 * (3) assignments that use local variables (sub-grouped by variable
 * declaration order).
 * Within each group/sub-group, assignments must be alphabetical by field
 * name, with field-to-field dependency exceptions.
 */
public class ConstructorAssignmentOrderCheck extends AbstractCheck {
	private record AssignmentInfo(@Nonnull DetailAST ast, @Nonnull String fieldName, int group, int subGroup) {}

	private static final int GROUP_MULTI = 1;
	private static final int GROUP_SIMPLE = 0;
	private static final int GROUP_VAR = 2;
	private static final String MSG_DEPENDENCY = "constructor.assign.dependency";
	private static final String MSG_MULTI_BEFORE_SIMPLE = "constructor.assign.multi.before.simple";
	private static final String MSG_NON_VAR_BEFORE_VAR = "constructor.assign.non.var.before.var";
	private static final String MSG_ORDER = "constructor.assign.order";
	private static final String MSG_VAR_GROUP_ORDER = "constructor.assign.var.group.order";

	private static void collectFieldReferences(@Nonnull DetailAST ast, @Nonnull Set<String> result) {
		if (ast.getType() == TokenTypes.DOT) {
			final var thisToken = ast.getFirstChild();
			if (thisToken != null && thisToken.getType() == TokenTypes.LITERAL_THIS) {
				final var fieldIdent = thisToken.getNextSibling();
				if (fieldIdent != null && fieldIdent.getType() == TokenTypes.IDENT)
					result.add(fieldIdent.getText());
			}
		}
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			collectFieldReferences(child, result);
	}

	private static void collectLocalVarReferences(
			@Nonnull DetailAST ast,
			@Nonnull Set<String> localVarNames,
			@Nonnull Set<String> result
	) {
		// skip this.field patterns entirely (the IDENT child is a field name, not a local var)
		if (ast.getType() == TokenTypes.DOT
				&& ast.getFirstChild() != null
				&& ast.getFirstChild().getType() == TokenTypes.LITERAL_THIS)
			return;

		if (ast.getType() == TokenTypes.IDENT && localVarNames.contains(ast.getText()))
			result.add(ast.getText());

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			collectLocalVarReferences(child, localVarNames, result);
	}

	@CheckReturnValue
	@Nullable
	private static String extractFieldName(@Nonnull DetailAST exprStatement) {
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
		// phase 1: scan for local variable declarations and field assignments
		final var localVarNames = new HashSet<String>();
		final var localVarOrder = new HashMap<String, Integer>();
		var varDeclCount = 0;
		final var assignments = new ArrayList<AssignmentInfo>();

		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null) {
					localVarNames.add(ident.getText());
					localVarOrder.put(ident.getText(), varDeclCount++);
				}
				continue;
			}

			if (child.getType() != TokenTypes.EXPR)
				continue;

			final var fieldName = extractFieldName(child);
			if (fieldName == null)
				continue;

			// determine which local variables are referenced in the RHS
			final var usedVars = new HashSet<String>();
			final var assign = child.getFirstChild();
			if (assign != null && assign.getChildCount() > 1)
				collectLocalVarReferences(assign.getLastChild(), localVarNames, usedVars);

			final int group;
			final int subGroup;
			if (usedVars.isEmpty()) {
				group = isMultiLine(child) ? GROUP_MULTI : GROUP_SIMPLE;
				subGroup = -1;
			}
			else {
				group = GROUP_VAR;
				subGroup = usedVars.stream()
						.mapToInt(v -> localVarOrder.getOrDefault(v, -1))
						.max()
						.orElse(-1);
			}

			assignments.add(new AssignmentInfo(child, fieldName, group, subGroup));
		}

		// phase 2: collect all assigned field names for dependency checking
		final var assignedFields = new HashSet<String>();
		for (var info : assignments)
			assignedFields.add(info.fieldName);

		// phase 3: pairwise comparison
		for (var i = 1; i < assignments.size(); ++i) {
			final var prev = assignments.get(i - 1);
			final var curr = assignments.get(i);

			// different major group: check group ordering
			if (prev.group > curr.group) {
				if (prev.group == GROUP_VAR)
					log(curr.ast, MSG_NON_VAR_BEFORE_VAR, curr.fieldName);
				else
					log(curr.ast, MSG_MULTI_BEFORE_SIMPLE, curr.fieldName);
				continue;
			}
			if (prev.group < curr.group)
				continue;

			// same major group VAR: check sub-group ordering
			if (prev.group == GROUP_VAR) {
				if (prev.subGroup > curr.subGroup) {
					log(curr.ast, MSG_VAR_GROUP_ORDER, curr.fieldName, prev.fieldName);
					continue;
				}
				if (prev.subGroup < curr.subGroup)
					continue;
			}

			// same group + same sub-group: check field deps and alphabetical
			final var currFieldRefs = new HashSet<String>();
			final var currAssign = curr.ast.getFirstChild();
			if (currAssign != null && currAssign.getChildCount() > 1)
				collectFieldReferences(currAssign.getLastChild(), currFieldRefs);
			currFieldRefs.retainAll(assignedFields);

			final var prevFieldRefs = new HashSet<String>();
			final var prevAssign = prev.ast.getFirstChild();
			if (prevAssign != null && prevAssign.getChildCount() > 1)
				collectFieldReferences(prevAssign.getLastChild(), prevFieldRefs);
			prevFieldRefs.retainAll(assignedFields);

			if (currFieldRefs.contains(prev.fieldName))
				continue;

			if (prevFieldRefs.contains(curr.fieldName)) {
				log(prev.ast, MSG_DEPENDENCY, prev.fieldName, curr.fieldName);
				continue;
			}

			if (curr.fieldName.compareToIgnoreCase(prev.fieldName) < 0)
				log(curr.ast, MSG_ORDER, curr.fieldName, prev.fieldName);
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