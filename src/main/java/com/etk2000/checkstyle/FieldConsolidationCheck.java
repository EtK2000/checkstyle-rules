package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags consecutive same-type uninitialized fields on
 * separate lines that should be combined into a single declaration
 * (e.g. {@code int height, width;}).
 */
public class FieldConsolidationCheck extends AbstractCheck {
	private static final String MSG_KEY = "field.consolidate.same.type";

	@CheckReturnValue
	@Nonnull
	private static List<String> annotationKeys(@Nonnull DetailAST varDef) {
		final var result = new ArrayList<String>();
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers != null) {
			for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.ANNOTATION)
					result.add(canonicalAnnotation(child));
			}
		}
		result.sort(String::compareTo);
		return result;
	}

	@CheckReturnValue
	private static boolean canCombine(@Nonnull DetailAST prev, @Nonnull DetailAST curr) {
		final var prevIdentLine = prev.findFirstToken(TokenTypes.IDENT).getLineNo();
		final var currIdentLine = curr.findFirstToken(TokenTypes.IDENT).getLineNo();
		// already on the same line (already combined)
		if (prevIdentLine == currIdentLine)
			return false;
		// gap between fields (comment, blank line, or javadoc)
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
	private static String canonicalAnnotation(@Nonnull DetailAST annotation) {
		final var sb = new StringBuilder();
		for (var child = annotation.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IDENT) {
				sb.append(child.getText());
				break;
			}
			if (child.getType() == TokenTypes.DOT) {
				serializeDot(child, sb);
				break;
			}
		}
		final var params = new TreeMap<String, String>();
		for (var child = annotation.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
				final var key = child.findFirstToken(TokenTypes.IDENT).getText();
				// value can be EXPR (single value) or ANNOTATION_ARRAY_INIT (array value)
				var value = child.findFirstToken(TokenTypes.EXPR);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
				params.put(key, serializeAst(value));
			}
			else if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT)
				params.put("value", serializeAst(child));
		}
		if (!params.isEmpty()) {
			sb.append('(');
			var first = true;
			for (var entry : params.entrySet()) {
				if (!first)
					sb.append(',');
				sb.append(entry.getKey()).append('=').append(entry.getValue());
				first = false;
			}
			sb.append(')');
		}
		return sb.toString();
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

	@CheckReturnValue
	@Nonnull
	private static String serializeAst(@Nonnull DetailAST ast) {
		if (ast.getChildCount() == 0)
			return ast.getText();
		final var sb = new StringBuilder();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			sb.append(serializeAst(child));
		return sb.toString();
	}

	private static void serializeDot(@Nonnull DetailAST dot, @Nonnull StringBuilder sb) {
		final var first = dot.getFirstChild();
		if (first.getType() == TokenTypes.DOT)
			serializeDot(first, sb);
		else
			sb.append(first.getText());
		sb.append('.');
		var last = first;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		sb.append(last.getText());
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
					if (tc.getType() == TokenTypes.WILDCARD_TYPE)
						serializeWildcard(tc, sb);
					else
						serializeTypeName(tc, sb);
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
				final var next = ast.getNextSibling();
				if (next != null && next.getType() == TokenTypes.TYPE_ARGUMENTS)
					serializeTypeArguments(next, sb);
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

	private static void serializeWildcard(@Nonnull DetailAST wildcard, @Nonnull StringBuilder sb) {
		sb.append('?');
		for (var child = wildcard.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.TYPE_UPPER_BOUNDS) {
				sb.append(" extends ");
				serializeTypeName(child.getFirstChild(), sb);
			}
			else if (child.getType() == TokenTypes.TYPE_LOWER_BOUNDS) {
				sb.append(" super ");
				serializeTypeName(child.getFirstChild(), sb);
			}
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

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.COMMA, TokenTypes.SEMI -> {}
				case TokenTypes.VARIABLE_DEF -> {
					if (prev != null && canCombine(prev, child))
						log(child.findFirstToken(TokenTypes.IDENT), MSG_KEY, fieldName(child), fieldName(prev), typeName(child));
					prev = child;
				}
				default -> prev = null;
			}
		}
	}
}