package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

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
public class FieldConsolidationCheck extends AbstractCheck {
	private static final int MAX_ANNOTATION_DEPTH = 50;
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

	@CheckReturnValue
	private static boolean canCombine(@Nonnull DetailAST prev, @Nonnull DetailAST curr) {
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
		return typeName(prev).equals(typeName(curr));
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
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		DetailAST prev = null;
		var prevSeparatedByComma = false;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.COMMA -> prevSeparatedByComma = true;
				case TokenTypes.SEMI -> {}
				case TokenTypes.VARIABLE_DEF -> {
					if (prev != null && !prevSeparatedByComma && canCombine(prev, child))
						log(child.findFirstToken(TokenTypes.IDENT), MSG_KEY, fieldName(child), fieldName(prev), typeName(child));
					prev = child;
					prevSeparatedByComma = false;
				}
				default -> {
					prev = null;
					prevSeparatedByComma = false;
				}
			}
		}
	}
}