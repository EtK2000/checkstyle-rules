package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags unnecessary {@code this.} usage.
 * {@code this.} is only allowed when:
 * <ul>
 *     <li>Assigning an instance field: {@code this.field = value}</li>
 *     <li>A name bound in the same member shares its name (a parameter, local, pattern variable,
 *     try resource, or a lambda parameter in a field initializer)</li>
 *     <li>A field or instance initializer reads a field declared later in the same type</li>
 * </ul>
 */
public class NoUnnecessaryThisCheck extends AbstractAstCheck {
	/**
	 * The {@code this} keyword and the member name it qualifies.
	 */
	public record ThisTarget(@Nonnull DetailAST literalThis, @Nonnull DetailAST ident) {}

	private static final Set<Integer> TYPE_DEFINITIONS = Set.of(
			TokenTypes.ANNOTATION_DEF,
			TokenTypes.CLASS_DEF,
			TokenTypes.ENUM_DEF,
			TokenTypes.INTERFACE_DEF,
			TokenTypes.RECORD_DEF
	);
	private static final String MSG_KEY = "no.unnecessary.this";

	/**
	 * Adds every name {@code ast}'s subtree binds that can shadow a field: parameters (method,
	 * constructor, lambda, catch), locals, for and for-each variables, pattern variables
	 * (instanceof, case and record patterns) and try resources. Nested type declarations are left
	 * out, as their bindings are never in scope outside them; anonymous class bodies are not, which
	 * only ever keeps a {@code this.} that could go.
	 */
	private static void collectBindings(@Nonnull DetailAST ast, @Nonnull Set<String> names) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				// a lone untyped lambda parameter is an IDENT directly under LAMBDA
				case TokenTypes.IDENT -> {
					if (ast.getType() == TokenTypes.LAMBDA)
						names.add(child.getText());
				}
				case TokenTypes.PARAMETER_DEF, TokenTypes.PATTERN_VARIABLE_DEF, TokenTypes.RESOURCE, TokenTypes.VARIABLE_DEF -> {
					final var ident = child.findFirstToken(TokenTypes.IDENT);
					if (ident != null)
						names.add(ident.getText());
				}
				default -> {}
			}
			if (!TYPE_DEFINITIONS.contains(child.getType()))
				collectBindings(child, names);
		}
	}

	/**
	 * True when {@code dot} reads an instance field of its innermost type from one of that type's
	 * field initializers or instance initializers, and the field is declared by that member or a
	 * later one. JLS 8.3.3 rejects such a read as a simple name, so {@code this.} keeps it legal.
	 */
	@CheckReturnValue
	private static boolean isForwardFieldRead(@Nonnull DetailAST dot, @Nonnull DetailAST member, @Nonnull String memberName) {
		if (dot.getParent().getType() == TokenTypes.METHOD_CALL
				|| member.getType() != TokenTypes.INSTANCE_INIT && member.getType() != TokenTypes.VARIABLE_DEF)
			return false;
		for (var sibling = member; sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.VARIABLE_DEF
					&& !AstQuery.hasModifier(sibling, TokenTypes.LITERAL_STATIC)
					&& memberName.equals(sibling.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return false;
	}

	/**
	 * The member of the innermost type body holding {@code dot} (a method, constructor,
	 * initializer or field declarator), or the compilation unit's root when no type body holds it.
	 */
	@CheckReturnValue
	@Nonnull
	private static DetailAST memberOf(@Nonnull DetailAST dot) {
		var member = dot;
		while (member.getParent() != null && member.getParent().getType() != TokenTypes.OBJBLOCK)
			member = member.getParent();
		return member;
	}

	/**
	 * The target {@code dot} names, or null when its {@code this.} is one of the three the check
	 * leaves alone.
	 */
	@CheckReturnValue
	@Nullable
	private static ThisTarget targetFor(@Nonnull DetailAST dot, @Nonnull Map<DetailAST, Set<String>> bindingsByMember) {
		final var firstChild = dot.getFirstChild();
		if (firstChild == null || firstChild.getType() != TokenTypes.LITERAL_THIS)
			return null;

		// a type witness puts TYPE_ARGUMENTS here, so `this.<String>m()` is left alone
		final var memberIdent = firstChild.getNextSibling();
		if (memberIdent == null || memberIdent.getType() != TokenTypes.IDENT)
			return null;

		final var assignParent = dot.getParent();
		if (assignParent != null && assignParent.getType() == TokenTypes.ASSIGN
				&& assignParent.getFirstChild() == dot)
			return null;

		final var memberName = memberIdent.getText();
		final var member = memberOf(dot);
		final var bindings = bindingsByMember.computeIfAbsent(member, key -> {
			final var names = new HashSet<String>();
			collectBindings(key, names);
			return names;
		});
		return bindings.contains(memberName) || isForwardFieldRead(dot, member, memberName)
				? null
				: new ThisTarget(firstChild, memberIdent);
	}

	/**
	 * The {@code this.} qualifier whose {@code .} stands at {@code (line, column)}, or null when
	 * nothing there is one the check reports. {@code line} is 0-based and {@code column} is a
	 * 0-based code-point column, both matching what the fixer is handed.
	 */
	@CheckReturnValue
	@Nullable
	public static ThisTarget thisAt(@Nonnull DetailAST root, int line, int column) {
		final var dot = AstQuery.findNodeAt(root, line, column, ast -> ast.getType() == TokenTypes.DOT);
		return dot == null ? null : targetFor(dot, new HashMap<>());
	}

	private final Map<DetailAST, Set<String>> bindingsByMember = new HashMap<>();

	@Override
	public void beginTree(@Nullable DetailAST rootAst) {
		bindingsByMember.clear();
	}

	/**
	 * Clearing here as well as in {@link #beginTree} is what releases the file on the normal path:
	 * the map is keyed by member nodes, each of which reaches the compilation unit through its
	 * parent chain, so one entry surviving the last file pins that whole tree for as long as the
	 * pooled thread lives. A walk aborted by a throw skips this hook, so the aborted file's tree
	 * stays held until the next {@code beginTree} on this instance.
	 */
	@Override
	public void finishTree(@Nullable DetailAST rootAst) {
		bindingsByMember.clear();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.DOT};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var target = targetFor(ast, bindingsByMember);
		if (target != null)
			log(ast, MSG_KEY, target.ident().getText());
	}
}