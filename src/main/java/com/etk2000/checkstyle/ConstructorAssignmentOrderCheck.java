package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
public class ConstructorAssignmentOrderCheck extends AbstractAstCheck {
	/**
	 * One {@code this.field = ...} assignment in a constructor/initializer body: its {@code EXPR}
	 * node, the assigned field name, its {@link #GROUP_SIMPLE}/{@link #GROUP_MULTI}/{@link #GROUP_VAR}
	 * group, the {@code subGroup} (max local-var declaration order it references, {@code -1} outside
	 * {@code GROUP_VAR}), {@code fieldRefs} (the other assigned fields its RHS reads, for dependency
	 * ordering), {@code usedVars} (the local variables its RHS reads), and the 0-based
	 * {@code startLine}/{@code endLine} span of
	 * its source text (a multi-line assignment, e.g. an anonymous class or text block, spans more
	 * than one line).
	 */
	public record Assignment(
			@Nonnull DetailAST ast,
			@Nonnull String fieldName,
			int group,
			int subGroup,
			@Nonnull Set<String> fieldRefs,
			@Nonnull Set<String> usedVars,
			int startLine,
			int endLine
	) {}

	/**
	 * The classified body: its {@code this.field} assignments and local-var declarations (both in
	 * source order), plus the 0-based lines occupied by any other top-level statement (a bare call,
	 * a non-{@code this} assignment).
	 */
	public record BodyClassification(
			@Nonnull List<Assignment> assignments,
			@Nonnull List<LocalVar> localVars,
			@Nonnull Set<Integer> statementLines
	) {}

	/**
	 * A local variable declared in the body: its {@code VARIABLE_DEF} node, name, 0-based
	 * declaration order, the 0-based {@code startLine}/{@code endLine} span of its source text (a
	 * declaration whose initializer wraps spans more than one line), and {@code usedVars} (the
	 * earlier local variables its initializer reads).
	 */
	public record LocalVar(
			@Nonnull DetailAST ast,
			@Nonnull String name,
			int order,
			int startLine,
			int endLine,
			@Nonnull Set<String> usedVars
	) {}

	public static final int GROUP_MULTI = 1;
	public static final int GROUP_SIMPLE = 0;
	public static final int GROUP_VAR = 2;
	private static final String MSG_DEPENDENCY = "constructor.assign.dependency";
	private static final String MSG_MULTI_BEFORE_SIMPLE = "constructor.assign.multi.before.simple";
	private static final String MSG_NON_VAR_BEFORE_VAR = "constructor.assign.non.var.before.var";
	private static final String MSG_ORDER = "constructor.assign.order";
	private static final String MSG_VAR_GROUP_ORDER = "constructor.assign.var.group.order";

	/**
	 * Locates the constructor/instance-initializer body {@code SLIST} that owns the assignment the
	 * check reported at {@code (line, column)} (0-based), or {@code null} when no {@code EXPR} sits
	 * there or the enclosing block is not a constructor/initializer body.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST bodyAt(@Nonnull DetailAST root, int line, int column) {
		final var expr = AstUtil.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.EXPR);
		if (expr == null)
			return null;
		final var body = expr.getParent();
		if (body == null || body.getType() != TokenTypes.SLIST)
			return null;
		final var owner = body.getParent();
		if (owner == null)
			return null;
		return switch (owner.getType()) {
			case TokenTypes.COMPACT_CTOR_DEF, TokenTypes.CTOR_DEF, TokenTypes.INSTANCE_INIT -> body;
			default -> null;
		};
	}

	/**
	 * Classifies a constructor/initializer {@code SLIST} body into its {@code this.field}
	 * assignments and local-var declarations, computing each assignment's group, sub-group,
	 * field-dependency set, referenced local variables, and source-line span, and collecting the
	 * lines of any other top-level statement.
	 */
	@CheckReturnValue
	@Nonnull
	public static BodyClassification classify(@Nonnull DetailAST body) {
		final var shadowedNames = new HashSet<String>();
		final var parent = body.getParent();
		final var params = parent == null ? null : parent.findFirstToken(TokenTypes.PARAMETERS);
		if (params != null) {
			for (var p = params.getFirstChild(); p != null; p = p.getNextSibling()) {
				if (p.getType() == TokenTypes.PARAMETER_DEF) {
					final var pid = p.findFirstToken(TokenTypes.IDENT);
					if (pid != null)
						shadowedNames.add(pid.getText());
				}
			}
		}

		final var localVarNames = new HashSet<String>();
		final var localVarOrder = new HashMap<String, Integer>();
		final var localVars = new ArrayList<LocalVar>();
		final var statementLines = new HashSet<Integer>();
		var varDeclCount = 0;
		final var scanned = new ArrayList<Assignment>();

		for (var child = body.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.VARIABLE_DEF) {
				final var ident = child.findFirstToken(TokenTypes.IDENT);
				if (ident != null) {
					final var varUsedVars = new HashSet<String>();
					final var init = child.findFirstToken(TokenTypes.ASSIGN);
					if (init != null)
						collectLocalVarReferences(init, localVarNames, varUsedVars);
					localVars.add(new LocalVar(
							child,
							ident.getText(),
							varDeclCount,
							child.getLineNo() - 1,
							AstUtil.lastLine(child) - 1,
							Set.copyOf(varUsedVars)
					));
					localVarNames.add(ident.getText());
					localVarOrder.put(ident.getText(), varDeclCount);
					++varDeclCount;
				}
				continue;
			}

			if (child.getType() == TokenTypes.EXPR) {
				final var fieldName = extractFieldName(child);
				if (fieldName != null) {
					final var usedVars = new HashSet<String>();
					final var assign = child.getFirstChild();
					if (assign != null && assign.getChildCount() > 1)
						collectLocalVarReferences(assign.getLastChild(), localVarNames, usedVars);

					final int group;
					final int subGroup;
					if (usedVars.isEmpty()) {
						group = AstUtil.lastLine(child) > child.getLineNo() ? GROUP_MULTI : GROUP_SIMPLE;
						subGroup = -1;
					}
					else {
						group = GROUP_VAR;
						subGroup = usedVars.stream()
								.mapToInt(v -> localVarOrder.getOrDefault(v, -1))
								.max()
								.orElse(-1);
					}

					scanned.add(new Assignment(
							child,
							fieldName,
							group,
							subGroup,
							Set.of(),
							Set.copyOf(usedVars),
							child.getLineNo() - 1,
							AstUtil.lastLine(child) - 1
					));
					continue;
				}
			}

			if (child.getType() != TokenTypes.RCURLY && child.getType() != TokenTypes.SEMI) {
				for (var ln = child.getLineNo(); ln <= AstUtil.lastLine(child); ++ln)
					statementLines.add(ln - 1);
			}
		}

		shadowedNames.addAll(localVarNames);
		final var assignedFields = new HashSet<String>();
		for (var info : scanned)
			assignedFields.add(info.fieldName());

		final var assignments = new ArrayList<Assignment>(scanned.size());
		for (var info : scanned) {
			final var fieldRefs = fieldReferences(info.ast().getFirstChild(), shadowedNames, assignedFields);
			assignments.add(new Assignment(
					info.ast(),
					info.fieldName(),
					info.group(),
					info.subGroup(),
					fieldRefs,
					info.usedVars(),
					info.startLine(),
					info.endLine()
			));
		}

		return new BodyClassification(assignments, localVars, statementLines);
	}

	/**
	 * Collects field references from an RHS expression into two sets: {@code qualified}
	 * for {@code this.field} accesses (always a field regardless of shadowing) and
	 * {@code bare} for lone identifiers (a field only when not shadowed by a local or
	 * parameter of the same name). Member accesses on another receiver ({@code other.field})
	 * and method-call names ({@code field()}) are not treated as bare field references.
	 */
	private static void collectFieldReferences(
			@Nonnull DetailAST root,
			@Nonnull Set<String> qualified,
			@Nonnull Set<String> bare
	) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var ast = stack.pop();
			if (ast.getType() == TokenTypes.DOT) {
				final var receiver = ast.getFirstChild();
				if (receiver == null)
					continue;
				if (receiver.getType() == TokenTypes.LITERAL_THIS) {
					final var fieldIdent = receiver.getNextSibling();
					if (fieldIdent != null && fieldIdent.getType() == TokenTypes.IDENT)
						qualified.add(fieldIdent.getText());
				}
				stack.push(receiver);
				continue;
			}
			if (ast.getType() == TokenTypes.IDENT) {
				final var parent = ast.getParent();
				if (parent == null || parent.getType() != TokenTypes.METHOD_CALL || parent.getFirstChild() != ast)
					bare.add(ast.getText());
				continue;
			}
			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
	}

	private static void collectLocalVarReferences(
			@Nonnull DetailAST root,
			@Nonnull Set<String> localVarNames,
			@Nonnull Set<String> result
	) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var ast = stack.pop();
			// skip this.field patterns entirely (the IDENT child is a field name, not a local var)
			if (ast.getType() == TokenTypes.DOT
					&& ast.getFirstChild() != null
					&& ast.getFirstChild().getType() == TokenTypes.LITERAL_THIS)
				continue;

			if (ast.getType() == TokenTypes.IDENT && localVarNames.contains(ast.getText()))
				result.add(ast.getText());

			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
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

	/**
	 * Returns the assigned fields referenced by an assignment's RHS, combining
	 * {@code this.field} accesses with bare identifiers that are not shadowed by a
	 * local variable or parameter, then restricting to fields assigned in this body.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> fieldReferences(
			@Nullable DetailAST assign,
			@Nonnull Set<String> shadowedNames,
			@Nonnull Set<String> assignedFields
	) {
		final var qualified = new HashSet<String>();
		final var bare = new HashSet<String>();
		if (assign != null && assign.getChildCount() > 1)
			collectFieldReferences(assign.getLastChild(), qualified, bare);
		bare.removeAll(shadowedNames);
		qualified.addAll(bare);
		qualified.retainAll(assignedFields);
		return qualified;
	}

	private void checkAssignmentOrder(@Nonnull DetailAST body) {
		final var assignments = classify(body).assignments();

		for (var i = 1; i < assignments.size(); ++i) {
			final var prev = assignments.get(i - 1);
			final var curr = assignments.get(i);

			if (prev.group() > curr.group()) {
				// a data dependency (curr reads a field prev assigns) forces curr after prev, so the
				// group order cannot be satisfied; don't report an unfixable violation
				if (!curr.fieldRefs().contains(prev.fieldName())) {
					if (prev.group() == GROUP_VAR)
						log(curr.ast(), MSG_NON_VAR_BEFORE_VAR, curr.fieldName());
					else
						log(curr.ast(), MSG_MULTI_BEFORE_SIMPLE, curr.fieldName());
				}
				continue;
			}
			if (prev.group() < curr.group())
				continue;

			if (prev.group() == GROUP_VAR) {
				if (prev.subGroup() > curr.subGroup()) {
					log(curr.ast(), MSG_VAR_GROUP_ORDER, curr.fieldName(), prev.fieldName());
					continue;
				}
				if (prev.subGroup() < curr.subGroup())
					continue;
			}

			if (curr.fieldRefs().contains(prev.fieldName()))
				continue;

			if (prev.fieldRefs().contains(curr.fieldName())) {
				log(prev.ast(), MSG_DEPENDENCY, prev.fieldName(), curr.fieldName());
				continue;
			}

			if (curr.fieldName().compareToIgnoreCase(prev.fieldName()) < 0)
				log(curr.ast(), MSG_ORDER, curr.fieldName(), prev.fieldName());
		}
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

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var body = ast.findFirstToken(TokenTypes.SLIST);
		if (body != null)
			checkAssignmentOrder(body);
	}
}