package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * The same-file inheritance edges of one compilation unit, indexed in both directions. A check that
 * asks about every declaration builds this once per file, rather than rewalking the unit per
 * declaration.
 *
 * <p>Unlike {@link AstUtil#supertypeBodies}, this also links the two body kinds that name their
 * supertype somewhere other than an {@code extends}/{@code implements} clause: an anonymous class
 * names it on the {@code new}, and an enum constant body inherits its own enum. Those edges live
 * here rather than in the shared helper because widening the shared one would also change what
 * {@code PreferVarCheck} considers an inherited declaration.
 */
final class TypeGraph {
	/**
	 * The bodies related to one type body, split by direction: {@code descendants} is the body
	 * itself plus its same-file subtypes, {@code ancestors} the same-file supertypes of each of
	 * those. A member declared in an ancestor is only inherited when it is visible, whereas one
	 * declared in a descendant is always the descendant's own, so callers weighing a signature
	 * rewrite have to treat the two groups differently.
	 */
	record Related(@Nonnull List<DetailAST> descendants, @Nonnull List<DetailAST> ancestors) {}

	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> supertypesOf(@Nonnull DetailAST objBlock) {
		final var parent = objBlock.getParent();
		if (parent == null)
			return List.of();

		// an anonymous body names its supertype on the `new`, past any constructor type arguments
		if (parent.getType() == TokenTypes.LITERAL_NEW) {
			final var body = AstUtil.sameFileTypeBody(parent, AstUtil.findNewClassName(parent));
			return body == null ? List.of() : List.of(body);
		}
		if (parent.getType() == TokenTypes.ENUM_CONSTANT_DEF) {
			final var body = parent.getParent();
			return body == null ? List.of() : List.of(body);
		}

		final var bodies = new ArrayList<DetailAST>();
		for (var clause = parent.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;

			for (var name = clause.getFirstChild(); name != null; name = name.getNextSibling()) {
				final var body = AstUtil.sameFileTypeBody(parent, AstUtil.typeName(name));
				if (body != null)
					bodies.add(body);
			}
		}
		return bodies;
	}

	private final Map<DetailAST, List<DetailAST>> subtypes = new HashMap<>();
	private final Map<DetailAST, List<DetailAST>> supertypes = new HashMap<>();

	TypeGraph(@Nonnull DetailAST node) {
		var root = node;
		while (root.getParent() != null)
			root = root.getParent();

		final var pending = new ArrayDeque<DetailAST>();
		pending.push(root);
		while (!pending.isEmpty()) {
			final var current = pending.pop();
			if (current.getType() == TokenTypes.OBJBLOCK) {
				final var supers = supertypesOf(current);
				supertypes.put(current, supers);
				for (var body : supers)
					subtypes.computeIfAbsent(body, key -> new ArrayList<>()).add(current);
			}
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
	}

	private void collectAncestors(
			@Nonnull List<DetailAST> from,
			@Nonnull Set<DetailAST> seen,
			@Nonnull List<DetailAST> into
	) {
		for (var i = 0; i < from.size(); ++i) {
			for (var body : supertypes.getOrDefault(from.get(i), List.of())) {
				if (seen.add(body))
					into.add(body);
			}
		}
	}

	@CheckReturnValue
	@Nonnull
	Related related(@Nonnull DetailAST objBlock) {
		final var descendants = new ArrayList<DetailAST>();
		final var seen = new HashSet<DetailAST>();
		descendants.add(objBlock);
		seen.add(objBlock);
		for (var i = 0; i < descendants.size(); ++i) {
			for (var body : subtypes.getOrDefault(descendants.get(i), List.of())) {
				if (seen.add(body))
					descendants.add(body);
			}
		}

		// seeded from every descendant, so a subtype's other supertype is reached too; only
		// supertypes are ever appended, which keeps the walk off sibling subtypes
		final var ancestors = new ArrayList<DetailAST>();
		collectAncestors(descendants, seen, ancestors);
		collectAncestors(ancestors, seen, ancestors);
		return new Related(descendants, ancestors);
	}
}