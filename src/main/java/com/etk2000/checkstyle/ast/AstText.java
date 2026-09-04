package com.etk2000.checkstyle.ast;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Reconstructs source text from AST nodes: names, types, annotations and expressions.
 *
 * <p>Also the home of the comment-node skip primitives ({@link #isCommentToken},
 * {@link #firstRealChild}, {@link #nextRealSibling}). They are sited here deliberately: this class
 * is the bottom of the package's one-way dependency chain, so {@code AstQuery}, {@code AstDisplay}
 * and {@code AstResolve} all reach them without a back-edge, and {@code AstDisplay} does not have
 * to drag in {@code AstQuery} to get them.
 */
public final class AstText {
	@CheckReturnValue
	@Nonnull
	public static String annotationName(@Nonnull DetailAST annotation) {
		final var ident = annotation.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		final var dot = annotation.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			var last = dot.getFirstChild();
			if (last == null)
				return "";

			while (last.getNextSibling() != null)
				last = last.getNextSibling();
			return last.getText();
		}
		return "";
	}

	/**
	 * Returns a canonical string for an ANNOTATION AST node, including its
	 * name and normalized parameters. Parameter names are sorted alphabetically
	 * so that {@code @A(b=1, a=2)} and {@code @A(a=2, b=1)} produce the same
	 * string. Positional values are stored under key "value".
	 *
	 * <p>Examples:
	 * <ul>
	 *   <li>{@code @Deprecated} and {@code @Deprecated()} both produce {@code "Deprecated"}</li>
	 *   <li>{@code @A(123)} and {@code @A(value=123)} both produce {@code "A(value=123)"}</li>
	 * </ul>
	 */
	@CheckReturnValue
	@Nonnull
	public static String canonicalAnnotation(@Nonnull DetailAST annotation, int maxDepth) {
		if (maxDepth <= 0)
			return "";
		final var sb = new StringBuilder(annotationName(annotation));
		final var params = new TreeMap<String, String>();
		for (var child = annotation.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
				final var keyIdent = child.findFirstToken(TokenTypes.IDENT);
				if (keyIdent == null)
					continue;

				final var key = keyIdent.getText();
				var value = child.findFirstToken(TokenTypes.EXPR);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION);
				if (value != null) {
					params.put(
							key,
							value.getType() == TokenTypes.ANNOTATION
									? canonicalAnnotation(value, maxDepth - 1)
									: exprText(value)
					);
				}
			}
			else if (child.getType() == TokenTypes.ANNOTATION)
				params.put("value", canonicalAnnotation(child, maxDepth - 1));
			else if (child.getType() == TokenTypes.EXPR || child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT)
				params.put("value", exprText(child));
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
	public static String canonicalType(@Nonnull DetailAST typeNode) {
		final var sb = new StringBuilder();
		for (var child = typeNode.getFirstChild(); child != null; child = child.getNextSibling()) {
			switch (child.getType()) {
				case TokenTypes.ARRAY_DECLARATOR -> sb.append("[]");
				case TokenTypes.DOT -> sb.append(dottedName(child));
				case TokenTypes.IDENT -> sb.append(child.getText());
				case TokenTypes.LITERAL_BOOLEAN -> sb.append("boolean");
				case TokenTypes.LITERAL_BYTE -> sb.append("byte");
				case TokenTypes.LITERAL_CHAR -> sb.append("char");
				case TokenTypes.LITERAL_DOUBLE -> sb.append("double");
				case TokenTypes.LITERAL_FLOAT -> sb.append("float");
				case TokenTypes.LITERAL_INT -> sb.append("int");
				case TokenTypes.LITERAL_LONG -> sb.append("long");
				case TokenTypes.LITERAL_SHORT -> sb.append("short");
				case TokenTypes.LITERAL_VOID -> sb.append("void");
			}
		}
		return sb.toString();
	}

	/** The name a {@code PACKAGE_DEF} declares, or null when it carries none. */
	@CheckReturnValue
	@Nullable
	private static String declaredPackageName(@Nonnull DetailAST packageDef) {
		// searches direct children only, so a qualified annotation type's own dots
		// cannot be mistaken for the package name
		final var dot = packageDef.findFirstToken(TokenTypes.DOT);
		if (dot != null) {
			// dottedName, not FullIdent.createFullIdent: FullIdent skips SINGLE_LINE_COMMENT and
			// nothing else, so a BLOCK_COMMENT_BEGIN reaches appendToFull and is appended as the
			// literal text `/*` (probed: `package /*p*/ a.b;` -> `/*.a`)
			final var dotted = dottedName(dot);
			return dotted.isEmpty() ? null : dotted;
		}

		final var ident = packageDef.findFirstToken(TokenTypes.IDENT);
		if (ident == null)
			return null;

		final var single = ident.getText();
		return single.isEmpty() ? null : single;
	}

	/**
	 * Returns the dotted name from a DOT AST node (e.g. "java.util.List"),
	 * without consuming sibling nodes like ARRAY_DECLARATOR or TYPE_ARGUMENTS.
	 */
	@CheckReturnValue
	@Nonnull
	public static String dottedName(@Nonnull DetailAST dot) {
		final var segments = new ArrayList<String>();
		var current = dot;
		while (current.getType() == TokenTypes.DOT) {
			final var first = firstRealChild(current);
			if (first == null)
				break;

			// a generic segment carries its TYPE_ARGUMENTS as a sibling of its IDENT, so taking
			// the sibling blind reads `Outer<String>.Inner` back as `Outer.TYPE_ARGUMENTS`
			for (var sibling = nextRealSibling(first); sibling != null; sibling = nextRealSibling(sibling)) {
				if (sibling.getType() == TokenTypes.IDENT)
					segments.add(sibling.getText());
			}
			current = first;
		}
		segments.add(current.getText());
		final var sb = new StringBuilder(segments.getLast());
		for (var i = segments.size() - 2; i >= 0; --i)
			sb.append('.').append(segments.get(i));
		return sb.toString();
	}

	/**
	 * Returns the concatenated leaf text of an AST subtree, contributing nothing for the comment
	 * nodes a {@code WITH_COMMENTS} parse weaves in. Uses an iterative stack to avoid
	 * StackOverflowError on deeply nested expressions.
	 *
	 * <p>"Leaf" means no non-comment child, not {@code getChildCount() == 0}: a comment lands among
	 * the children of the node whose next real token it precedes, so the blind test would append
	 * the comment's own text and, on an operand-selecting caller, drop a real name.
	 */
	@CheckReturnValue
	@Nonnull
	public static String exprText(@Nonnull DetailAST ast) {
		if (isCommentToken(ast.getType()))
			return "";
		if (firstRealChild(ast) == null)
			return ast.getText();

		final var sb = new StringBuilder();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(ast);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			final var first = firstRealChild(node);
			if (first == null) {
				sb.append(node.getText());
				continue;
			}
			final var children = new ArrayList<DetailAST>();
			for (var child = first; child != null; child = nextRealSibling(child))
				children.add(child);
			for (var i = children.size() - 1; i >= 0; --i)
				stack.push(children.get(i));
		}
		return sb.toString();
	}

	/**
	 * Extracts the class name from a LITERAL_NEW node, handling both
	 * simple names ({@code new Foo()}) and qualified names
	 * ({@code new pkg.Foo()}). Constructor-level type arguments
	 * ({@code new <T>Foo()}) are skipped.
	 *
	 * @return the class name, or {@code null} for primitive arrays
	 */
	@CheckReturnValue
	@Nullable
	public static String findNewClassName(@Nonnull DetailAST literalNew) {
		for (var child = literalNew.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.DOT)
				return dottedName(child);
			if (child.getType() == TokenTypes.IDENT)
				return child.getText();
		}
		return null;
	}

	/**
	 * The first child of {@code node} that is not a comment, or {@code null} when it has none.
	 * Returns exactly what {@link DetailAST#getFirstChild()} returns under a
	 * {@code WITHOUT_COMMENTS} parse, where no comment node exists to skip.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST firstRealChild(@Nonnull DetailAST node) {
		var child = node.getFirstChild();
		while (child != null && isCommentToken(child.getType()))
			child = child.getNextSibling();
		return child;
	}

	@CheckReturnValue
	@Nullable
	public static String getEnclosingTypeName(@Nonnull DetailAST objBlock) {
		final var parent = objBlock.getParent();
		if (parent == null)
			return null;
		final var type = parent.getType();
		if (type != TokenTypes.CLASS_DEF && type != TokenTypes.INTERFACE_DEF
				&& type != TokenTypes.ENUM_DEF && type != TokenTypes.RECORD_DEF
				&& type != TokenTypes.ANNOTATION_DEF)
			return null;
		final var ident = parent.findFirstToken(TokenTypes.IDENT);
		return ident == null ? null : ident.getText();
	}

	@CheckReturnValue
	@Nullable
	public static String getPackageName(@Nonnull DetailAST node) {
		if (node.getType() == TokenTypes.PACKAGE_DEF)
			return declaredPackageName(node);

		var root = node;
		while (root.getParent() != null)
			root = root.getParent();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.PACKAGE_DEF)
				continue;

			final var declared = declaredPackageName(child);
			if (declared != null)
				return declared;
		}
		return null;
	}

	/**
	 * Whether {@code type} is one of the four comment tokens a {@code WITH_COMMENTS} parse weaves
	 * into the tree: {@code SINGLE_LINE_COMMENT} and {@code BLOCK_COMMENT_BEGIN} head a comment
	 * subtree, {@code COMMENT_CONTENT} and {@code BLOCK_COMMENT_END} sit inside one.
	 *
	 * <p>A comment is always inserted as a previous sibling of the node built from the next real
	 * token, so it lands among the children of that node's <em>parent</em>. That is why the hazard
	 * is not only appended comment text: a blind {@code getFirstChild}/{@code getNextSibling} picks
	 * the comment as an operand and the real one is dropped.
	 */
	@CheckReturnValue
	public static boolean isCommentToken(int type) {
		return type == TokenTypes.SINGLE_LINE_COMMENT || type == TokenTypes.BLOCK_COMMENT_BEGIN
				|| type == TokenTypes.BLOCK_COMMENT_END || type == TokenTypes.COMMENT_CONTENT;
	}

	@CheckReturnValue
	@Nullable
	static String lastIdent(@Nonnull DetailAST dot) {
		var last = dot.getFirstChild();
		if (last == null)
			return null;
		while (last.getNextSibling() != null)
			last = last.getNextSibling();
		return last.getType() == TokenTypes.IDENT ? last.getText() : null;
	}

	/**
	 * The next sibling of {@code node} that is not a comment, or {@code null} when it has none.
	 * Returns exactly what {@link DetailAST#getNextSibling()} returns under a
	 * {@code WITHOUT_COMMENTS} parse. Accepts null so a caller can chain it to step over several
	 * operands without a null check between each.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST nextRealSibling(@Nullable DetailAST node) {
		var sibling = node == null ? null : node.getNextSibling();
		while (sibling != null && isCommentToken(sibling.getType()))
			sibling = sibling.getNextSibling();
		return sibling;
	}

	@CheckReturnValue
	@Nonnull
	public static String simpleName(@Nonnull String fqcn) {
		return fqcn.substring(fqcn.lastIndexOf('.') + 1);
	}

	/** The name written at {@code nameNode}, or null when it is neither an identifier nor a dotted name. */
	@CheckReturnValue
	@Nullable
	public static String typeName(@Nullable DetailAST nameNode) {
		if (nameNode == null)
			return null;
		if (nameNode.getType() == TokenTypes.IDENT)
			return nameNode.getText();
		return nameNode.getType() == TokenTypes.DOT ? dottedName(nameNode) : null;
	}

	@CheckReturnValue
	@Nonnull
	public static String typeText(@Nonnull DetailAST type) {
		final var ident = type.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();

		final var dot = type.findFirstToken(TokenTypes.DOT);
		if (dot != null)
			return exprText(dot);
		return "";
	}

	private AstText() {
	}
}