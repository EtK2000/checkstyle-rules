package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags classes eligible for conversion to records.
 * A class is eligible when all instance fields are final without inline
 * initializers, it does not extend another class, has no instance initializers,
 * no custom equals/hashCode/toString with {@code @Override}, and constructors
 * only do simple field assignments.
 */
public class PreferRecordCheck extends AbstractAstCheck {
	private static final Set<String> RECORD_OVERRIDE_METHODS = Set.of("equals", "hashCode", "toString");
	private static final String MSG_KEY = "prefer.record";
	private static final String MSG_KEY_WARNING = "prefer.record.warning";

	@CheckReturnValue
	private static boolean hasAnnotation(@Nonnull DetailAST modifiers, @Nonnull String name) {
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION && name.equals(AstUtil.annotationName(child)))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	private static boolean hasInstanceInitializer(@Nonnull DetailAST objBlock) {
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.INSTANCE_INIT)
				return true;
		}
		return false;
	}

	/**
	 * Checks that every {@code this.field = ...} assignment in a constructor
	 * uses a simple parameter identifier as the RHS. Other statements
	 * (validation, side effects) are allowed since they can go in a compact
	 * record constructor.
	 */
	@CheckReturnValue
	private static boolean hasOnlySimpleFieldAssignments(@Nonnull DetailAST ctorDef) {
		final var slist = ctorDef.findFirstToken(TokenTypes.SLIST);
		if (slist == null)
			return true;

		final var paramNames = AstUtil.collectParameterNames(ctorDef);
		for (var child = slist.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.EXPR)
				continue;

			final var assign = child.getFirstChild();
			if (assign == null || assign.getType() != TokenTypes.ASSIGN)
				continue;

			final var lhs = assign.getFirstChild();
			if (lhs == null || lhs.getType() != TokenTypes.DOT)
				continue;
			final var thisToken = lhs.getFirstChild();
			if (thisToken == null || thisToken.getType() != TokenTypes.LITERAL_THIS)
				continue;

			final var fieldIdent = thisToken.getNextSibling();
			final var rhs = lhs.getNextSibling();
			if (rhs == null || rhs.getType() != TokenTypes.IDENT
					|| !paramNames.contains(rhs.getText())
					|| (fieldIdent != null && !fieldIdent.getText().equals(rhs.getText())))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	private static boolean hasOverriddenRecordMethod(@Nonnull DetailAST objBlock) {
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.METHOD_DEF)
				continue;
			final var modifiers = child.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers == null || !hasAnnotation(modifiers, "Override"))
				continue;
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null && RECORD_OVERRIDE_METHODS.contains(ident.getText()))
				return true;
		}
		return false;
	}

	/**
	 * Checks that all instance fields are final and none have inline initializers.
	 * Returns {@code false} if there are no instance fields, any are non-final,
	 * or any have inline initializers (which can't map to record components).
	 */
	@CheckReturnValue
	private static boolean hasRecordEligibleFields(@Nonnull DetailAST objBlock) {
		var hasInstanceField = false;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.VARIABLE_DEF)
				continue;
			final var modifiers = child.findFirstToken(TokenTypes.MODIFIERS);
			if (modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null)
				continue;

			hasInstanceField = true;
			if (modifiers == null || modifiers.findFirstToken(TokenTypes.FINAL) == null)
				return false;

			if (child.findFirstToken(TokenTypes.ASSIGN) != null)
				return false;
		}
		return hasInstanceField;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.CLASS_DEF};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		// skip abstract classes (records can't be abstract)
		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null && modifiers.findFirstToken(TokenTypes.ABSTRACT) != null)
			return;
		if (modifiers != null && AstUtil.hasSuppressWarnings(modifiers, "PreferRecord"))
			return;

		final var extendsClause = ast.findFirstToken(TokenTypes.EXTENDS_CLAUSE);
		if (extendsClause != null && extendsClause.getChildCount() > 0)
			return;

		final var objBlock = ast.findFirstToken(TokenTypes.OBJBLOCK);
		if (objBlock == null)
			return;

		if (!hasRecordEligibleFields(objBlock))
			return;

		if (hasInstanceInitializer(objBlock))
			return;

		if (hasOverriddenRecordMethod(objBlock))
			return;

		// a constructor must exist whose parameters match all instance
		// fields exactly (same types as a multiset, order doesn't matter)
		// and whose body has only simple this.field = param assignments
		final var fieldTypes = AstUtil.collectInstanceFieldTypes(objBlock);
		var hasMatchingConstructor = false;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.CTOR_DEF)
				continue;
			if (!hasOnlySimpleFieldAssignments(child))
				continue;
			if (AstUtil.collectParameterTypes(child).equals(fieldTypes)) {
				hasMatchingConstructor = true;
				break;
			}
		}
		if (!hasMatchingConstructor)
			return;

		final var ident = ast.findFirstToken(TokenTypes.IDENT);
		final var name = ident != null ? ident.getText() : "<unknown>";
		final var implementsClause = ast.findFirstToken(TokenTypes.IMPLEMENTS_CLAUSE);
		if (implementsClause != null && implementsClause.getChildCount() > 0)
			logWarning(ast, MSG_KEY_WARNING, name);
		else
			log(ast, MSG_KEY, name);
	}
}