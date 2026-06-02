package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags consecutive same-type uninitialized fields on
 * separate lines that should be combined into a single declaration
 * (e.g. {@code int height, width;}).
 */
public class FieldConsolidationCheck extends AbstractAstCheck {
	private static final int MAX_ANNOTATION_DEPTH = 50;
	// the type serializers recurse through nested generics, and this check runs outside
	// any fixer firewall, so a machine-generated `List<List<...<String>...>>` would fail
	// the whole check task with a StackOverflowError. Past this depth the pair is simply
	// not reported, which cannot merge two types that differ below the cap.
	private static final int MAX_TYPE_DEPTH = 64;
	private static final String MSG_KEY = "field.consolidate.same.type";

	@CheckReturnValue
	@Nonnull
	private static List<String> annotationKeys(@Nonnull DetailAST varDef) {
		final var result = new ArrayList<String>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null) {
			for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.ANNOTATION)
					result.add(AstUtil.canonicalAnnotation(child, MAX_ANNOTATION_DEPTH));
			}
		}
		result.sort(String::compareTo);
		return result;
	}

	/**
	 * True when the declaration opened by {@code currGroup} can be folded into the
	 * one opened by {@code prevGroup}. The pairing is between the declarations'
	 * adjacent declarators (the previous declaration's last and the current one's
	 * first), but the answer is about the whole declarations: the fix rewrites both
	 * into a single declaration rendered as one base type followed by bare names
	 * (see {@code docs/c-style-array-fixer.md}), so every declarator either side
	 * has to end up with that one type.
	 */
	@CheckReturnValue
	private static boolean canCombine(@Nonnull List<DetailAST> prevGroup, @Nonnull List<DetailAST> currGroup) {
		final var prev = prevGroup.getLast();
		final var curr = currGroup.getFirst();
		final var prevIdentLine = prev.findFirstToken(TokenTypes.IDENT).getLineNo();
		final var currIdentLine = curr.findFirstToken(TokenTypes.IDENT).getLineNo();
		if (prevIdentLine == currIdentLine)
			return false;
		if (curr.getLineNo() > prevIdentLine + 1)
			return false;
		if (prev.findFirstToken(TokenTypes.ASSIGN) != null || curr.findFirstToken(TokenTypes.ASSIGN) != null)
			return false;
		if (!modifierKeywords(prev).equals(modifierKeywords(curr)))
			return false;
		if (!annotationKeys(prev).equals(annotationKeys(curr)))
			return false;
		if (exceedsTypeDepth(prev) || exceedsTypeDepth(curr))
			return false;
		final var type = typeName(prev);
		return type.equals(typeName(curr)) && sharesType(prevGroup, type) && sharesType(currGroup, type);
	}

	/**
	 * The declarators of the declaration {@code first} opens: itself plus every
	 * {@code VARIABLE_DEF} reachable through a chain of {@code COMMA} siblings,
	 * since {@code int a, b;} is two sibling {@code VARIABLE_DEF}s under the
	 * {@code OBJBLOCK} rather than one node with two names.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> declarationGroup(@Nonnull DetailAST first) {
		final var group = new ArrayList<DetailAST>();
		group.add(first);
		for (var sibling = first.getNextSibling(); sibling != null && sibling.getType() == TokenTypes.COMMA; ) {
			final var declarator = sibling.getNextSibling();
			// defensive: on parseable input a COMMA in an OBJBLOCK is always followed by
			// the next declarator
			if (declarator == null || declarator.getType() != TokenTypes.VARIABLE_DEF)
				break;
			group.add(declarator);
			sibling = declarator.getNextSibling();
		}
		return group;
	}

	/**
	 * True when the declaration's type nests deeper than {@link #MAX_TYPE_DEPTH}.
	 * Walks iteratively so measuring the depth cannot itself overflow the stack.
	 */
	@CheckReturnValue
	private static boolean exceedsTypeDepth(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return false;
		final var nodes = new ArrayDeque<DetailAST>();
		final var depths = new ArrayDeque<Integer>();
		nodes.push(type);
		depths.push(0);
		while (!nodes.isEmpty()) {
			final var node = nodes.pop();
			final var depth = depths.pop();
			if (depth > MAX_TYPE_DEPTH)
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				nodes.push(child);
				depths.push(depth + 1);
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String fieldName(@Nonnull DetailAST varDef) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : "";
	}

	@CheckReturnValue
	@Nonnull
	private static Set<Integer> modifierKeywords(@Nonnull DetailAST varDef) {
		final var result = new TreeSet<Integer>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null) {
			for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() != TokenTypes.ANNOTATION)
					result.add(child.getType());
			}
		}
		return result;
	}

	private static void serializeBoundChildren(@Nonnull DetailAST bounds, @Nonnull StringBuilder sb) {
		for (var bc = bounds.getFirstChild(); bc != null; bc = bc.getNextSibling()) {
			if (bc.getType() == TokenTypes.ANNOTATIONS) {
				for (var ann = bc.getFirstChild(); ann != null; ann = ann.getNextSibling()) {
					if (ann.getType() == TokenTypes.ANNOTATION) {
						sb.append('@');
						sb.append(AstUtil.canonicalAnnotation(ann, MAX_ANNOTATION_DEPTH));
						sb.append(' ');
					}
				}
			}
			else
				serializeTypeName(bc, sb);
		}
	}

	private static void serializeDot(@Nonnull DetailAST dot, @Nonnull StringBuilder sb) {
		final var first = dot.getFirstChild();
		if (first.getType() == TokenTypes.DOT)
			serializeDot(first, sb);
		else
			sb.append(first.getText());
		sb.append('.');
		// find the IDENT child (skip TYPE_ARGUMENTS which may follow)
		var ident = first.getNextSibling();
		while (ident != null && ident.getType() != TokenTypes.IDENT)
			ident = ident.getNextSibling();
		if (ident != null)
			sb.append(ident.getText());
	}

	private static void serializeTypeArguments(@Nonnull DetailAST typeArgs, @Nonnull StringBuilder sb) {
		sb.append('<');
		var first = true;
		for (var child = typeArgs.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.TYPE_ARGUMENT) {
				if (!first)
					sb.append(',');
				first = false;
				for (var tc = child.getFirstChild(); tc != null; tc = tc.getNextSibling()) {
					switch (tc.getType()) {
						case TokenTypes.ANNOTATIONS -> {
							for (var ann = tc.getFirstChild(); ann != null; ann = ann.getNextSibling()) {
								if (ann.getType() == TokenTypes.ANNOTATION) {
									sb.append('@');
									sb.append(AstUtil.canonicalAnnotation(ann, MAX_ANNOTATION_DEPTH));
									sb.append(' ');
								}
							}
						}
						case TokenTypes.TYPE_LOWER_BOUNDS -> {
							sb.append(" super ");
							serializeBoundChildren(tc, sb);
						}
						case TokenTypes.TYPE_UPPER_BOUNDS -> {
							sb.append(" extends ");
							serializeBoundChildren(tc, sb);
						}
						case TokenTypes.WILDCARD_TYPE -> sb.append('?');
						default -> serializeTypeName(tc, sb);
					}
				}
			}
		}
		sb.append('>');
	}

	private static void serializeTypeName(@Nonnull DetailAST ast, @Nonnull StringBuilder sb) {
		switch (ast.getType()) {
			case TokenTypes.ARRAY_DECLARATOR -> {
				serializeTypeName(ast.getFirstChild(), sb);
				sb.append("[]");
			}
			case TokenTypes.DOT -> {
				serializeDot(ast, sb);
				// TYPE_ARGUMENTS may be a child of DOT (FQN generics) or a sibling
				var typeArgs = ast.findFirstToken(TokenTypes.TYPE_ARGUMENTS);
				if (typeArgs == null) {
					final var next = ast.getNextSibling();
					if (next != null && next.getType() == TokenTypes.TYPE_ARGUMENTS)
						typeArgs = next;
				}
				if (typeArgs != null)
					serializeTypeArguments(typeArgs, sb);
			}
			case TokenTypes.IDENT -> {
				sb.append(ast.getText());
				final var next = ast.getNextSibling();
				if (next != null && next.getType() == TokenTypes.TYPE_ARGUMENTS)
					serializeTypeArguments(next, sb);
			}
			case TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE, TokenTypes.LITERAL_CHAR,
			     TokenTypes.LITERAL_DOUBLE, TokenTypes.LITERAL_FLOAT, TokenTypes.LITERAL_INT,
			     TokenTypes.LITERAL_LONG, TokenTypes.LITERAL_SHORT -> sb.append(ast.getText());
		}
	}

	/**
	 * True when every declarator of a declaration is itself of {@code type}. A
	 * declarator may carry its own C-style brackets, so one declaration can hold
	 * several types ({@code int a, b[];} declares an {@code int} and an
	 * {@code int[]}) and only the pair the check compares is guaranteed to match.
	 */
	@CheckReturnValue
	private static boolean sharesType(@Nonnull List<DetailAST> group, @Nonnull String type) {
		for (var declarator : group) {
			if (!type.equals(typeName(declarator)))
				return false;
		}
		return true;
	}

	@CheckReturnValue
	@Nonnull
	private static String typeName(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";
		final var sb = new StringBuilder();
		serializeTypeName(type.getFirstChild(), sb);
		// count array declarators (brackets are siblings of the base type under TYPE)
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				sb.append("[]");
		}
		return sb.toString();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		List<DetailAST> prevGroup = null;
		var prevSeparatedByComma = false;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.COMMA -> prevSeparatedByComma = true;
				case TokenTypes.SEMI -> {}
				case TokenTypes.VARIABLE_DEF -> {
					if (!prevSeparatedByComma) {
						final var group = declarationGroup(child);
						if (prevGroup != null && canCombine(prevGroup, group))
							log(child.findFirstToken(TokenTypes.IDENT), MSG_KEY, fieldName(child), fieldName(prevGroup.getLast()), typeName(child));
						prevGroup = group;
					}
					prevSeparatedByComma = false;
				}
				default -> {
					prevGroup = null;
					prevSeparatedByComma = false;
				}
			}
		}
	}
}