package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags C-style array declarations ({@code int x[]}) and suggests Java-style
 * ({@code int[] x}). C-style splits the type information across two locations
 * and creates ambiguity in multi-variable declarations: in {@code int alpha[],
 * beta;}, alpha is {@code int[]} but beta is {@code int}.
 *
 * <p>Applies to fields, local variables, method/constructor parameters, and
 * method return types ({@code int method()[]}). Does not flag array creation
 * expressions ({@code new int[5]}) since those are a different construct.
 */
public class ArrayTypeStyleCheck extends AbstractAstCheck {
	/**
	 * One declarator of a multi-variable declaration: its type (the base type followed by the
	 * dimensions, which the AST keeps side by side), its name, and the dimensions written after it.
	 */
	public record CStyleDeclarator(@Nonnull DetailAST type, @Nonnull DetailAST ident, @Nonnull List<DetailAST> dimensions) {}

	/**
	 * The declaration a reported dimension belongs to: its name, and whether a type-use annotation
	 * sits on a dimension of its base type ({@code int @A [] x[]}).
	 */
	public record ReportedDeclarator(@Nonnull DetailAST name, boolean annotatedBaseDimension) {}

	private static final int[] DECLARATION_TOKENS = {
			TokenTypes.METHOD_DEF,
			TokenTypes.PARAMETER_DEF,
			TokenTypes.RECORD_COMPONENT_DEF,
			TokenTypes.VARIABLE_DEF
	};
	private static final String MSG = "array.type.style";

	/**
	 * {@code variableDef} with the dimensions written after its name, in source order, or null when
	 * one is also written before it: a base type of {@code int[]} would keep its own brackets through
	 * the move.
	 */
	@CheckReturnValue
	@Nullable
	private static CStyleDeclarator cStyleDeclarator(@Nonnull DetailAST variableDef) {
		final var ident = variableDef.findFirstToken(TokenTypes.IDENT);
		final var type = variableDef.findFirstToken(TokenTypes.TYPE);
		final var dimensions = new ArrayList<DetailAST>();
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.ARRAY_DECLARATOR)
				continue;
			if (!isAfter(child, ident.getLineNo(), ident.getColumnNo()))
				return null;
			dimensions.add(child);
		}
		return new CStyleDeclarator(type, ident, dimensions);
	}

	/** The variable whose type holds the dimension at the 0-based {@code line} and code-point {@code column}, or null. */
	@CheckReturnValue
	@Nullable
	private static DetailAST declarationOfDimensionAt(@Nonnull DetailAST root, int line, int column) {
		final var dimension = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.ARRAY_DECLARATOR);
		if (dimension == null || dimension.getParent().getType() != TokenTypes.TYPE)
			return null;
		final var decl = dimension.getParent().getParent();
		return decl.getType() == TokenTypes.VARIABLE_DEF ? decl : null;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findCStyleDeclarator(@Nonnull DetailAST decl) {
		final var ident = decl.findFirstToken(TokenTypes.IDENT);
		if (ident == null)
			return null;
		final var type = decl.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return null;

		final var identLine = ident.getLineNo();
		final var identCol = ident.getColumnNo();
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR && isAfter(child, identLine, identCol))
				return child;
		}
		return null;
	}

	/**
	 * Whether the dimension at the 0-based {@code line} and code-point {@code column} belongs to a
	 * declaration of more than one variable, whatever their depths.
	 */
	@CheckReturnValue
	public static boolean inMultiVariableDeclarationAt(@Nonnull DetailAST root, int line, int column) {
		final var decl = declarationOfDimensionAt(root, line, column);
		return decl != null && (isCommaSeparatedDeclarator(decl.getPreviousSibling()) || nextDeclarator(decl) != null);
	}

	@CheckReturnValue
	private static boolean isAfter(@Nonnull DetailAST node, int line, int col) {
		final var nodeLine = node.getLineNo();
		if (nodeLine != line)
			return nodeLine > line;
		return node.getColumnNo() > col;
	}

	@CheckReturnValue
	private static boolean isCommaSeparatedDeclarator(@Nullable DetailAST comma) {
		return comma != null && comma.getType() == TokenTypes.COMMA
				&& comma.getPreviousSibling() != null && comma.getPreviousSibling().getType() == TokenTypes.VARIABLE_DEF;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST nextDeclarator(@Nonnull DetailAST decl) {
		final var comma = decl.getNextSibling();
		if (comma == null || comma.getType() != TokenTypes.COMMA)
			return null;
		final var next = comma.getNextSibling();
		return next != null && next.getType() == TokenTypes.VARIABLE_DEF ? next : null;
	}

	/**
	 * The declaration whose reported dimension sits at the 0-based {@code line} and code-point
	 * {@code column}: the first dimension written after the name of a method, parameter, record
	 * component or variable, where {@link #visitToken} logs. Null for any other bracket there, such as
	 * a cast's, an {@code instanceof} type's, a base type's or a later dimension of the same name.
	 */
	@CheckReturnValue
	@Nullable
	public static ReportedDeclarator reportedDeclaratorAt(@Nonnull DetailAST root, int line, int column) {
		final var dimension = AstQuery.findNodeAt(root, line, column, node -> node.getType() == TokenTypes.ARRAY_DECLARATOR);
		if (dimension == null || dimension.getParent().getType() != TokenTypes.TYPE)
			return null;
		final var owner = dimension.getParent().getParent();
		if (Arrays.stream(DECLARATION_TOKENS).noneMatch(token -> token == owner.getType()) || findCStyleDeclarator(owner) != dimension)
			return null;

		final var name = owner.findFirstToken(TokenTypes.IDENT);
		var annotatedBaseDimension = false;
		for (var child = dimension.getParent().getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATIONS && !isAfter(child, name.getLineNo(), name.getColumnNo()))
				annotatedBaseDimension = true;
		}
		return new ReportedDeclarator(name, annotatedBaseDimension);
	}

	/**
	 * The declarators of the multi-variable declaration whose C-style dimension sits at the 0-based
	 * {@code line} and code-point {@code column}, when every declarator writes the same number of
	 * dimensions after its name and none before it, so moving them onto the shared base type retypes
	 * no declarator. Null for a single declarator, a parameter, a method, or a group mixing depths.
	 */
	@CheckReturnValue
	@Nullable
	public static List<CStyleDeclarator> uniformCStyleGroupAt(@Nonnull DetailAST root, int line, int column) {
		final var decl = declarationOfDimensionAt(root, line, column);
		if (decl == null)
			return null;

		var first = decl;
		while (isCommaSeparatedDeclarator(first.getPreviousSibling()))
			first = first.getPreviousSibling().getPreviousSibling();
		final var group = new ArrayList<CStyleDeclarator>();
		for (var current = first; current != null; current = nextDeclarator(current)) {
			final var declarator = cStyleDeclarator(current);
			if (declarator == null || declarator.dimensions().isEmpty())
				return null;
			if (!group.isEmpty() && declarator.dimensions().size() != group.getFirst().dimensions().size())
				return null;
			group.add(declarator);
		}
		return group.size() < 2 ? null : group;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return DECLARATION_TOKENS.clone();
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var cStyle = findCStyleDeclarator(ast);
		if (cStyle == null)
			return;
		final var ident = ast.findFirstToken(TokenTypes.IDENT);
		log(cStyle, MSG, ident == null ? "" : ident.getText());
	}
}