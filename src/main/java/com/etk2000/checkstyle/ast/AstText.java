package com.etk2000.checkstyle.ast;

import static java.util.Objects.requireNonNull;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.TokenUtil;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Reconstructs source text from AST nodes: names, types, annotations and expressions.
 */
public final class AstText {
	/** How deep {@link #canonicalAnnotation} and {@link #annotationIdentity} follow nested annotations. */
	public static final int MAX_ANNOTATION_DEPTH = 50;
	private static final String OPERATOR_CHARS = "!%&*+-/:<=>?^|~";

	/**
	 * The {@link #annotationIdentity} of each annotation directly under {@code modifiers}, sorted
	 * by simple name with a stable sort: the order of distinct annotation types carries no meaning,
	 * but a repeated annotation's instances keep their source order, which its container
	 * preserves. The key is the simple name because {@code @Tag} and {@code @a.Tag} may be one
	 * repeatable type. Null when any identity is.
	 */
	@CheckReturnValue
	@Nullable
	public static List<String> annotationIdentities(@Nonnull DetailAST modifiers, int maxDepth) {
		final var annotations = new ArrayList<DetailAST>();
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION)
				annotations.add(child);
		}
		annotations.sort(Comparator.comparing(AstText::annotationName));
		final var identities = new ArrayList<String>(annotations.size());
		for (var annotation : annotations) {
			final var identity = annotationIdentity(annotation, maxDepth);
			if (identity == null)
				return null;
			identities.add(identity);
		}
		return identities;
	}

	/**
	 * The identity of an {@code ANNOTATION}: {@link #canonicalAnnotation}'s form, but with every
	 * annotation's name as written, at any nesting level. Two annotation types that share a simple
	 * name ({@code javax.annotation.Nullable}, {@code org.jetbrains.annotations.Nullable}) never
	 * compare equal; the price is that two spellings of one type ({@code @Nullable} under an import
	 * and {@code @javax.annotation.Nullable}) do not either.
	 *
	 * <p>Null when the nesting runs past {@code maxDepth}, since whatever sits below the cap is
	 * unknown.
	 */
	@CheckReturnValue
	@Nullable
	public static String annotationIdentity(@Nonnull DetailAST annotation, int maxDepth) {
		return canonical(annotation, maxDepth, true);
	}

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
	 * {@link #canonicalAnnotation} when {@code asWritten} is false, {@link #annotationIdentity}
	 * when it is true. Only the identity can come out null: past {@code maxDepth} the ordering key
	 * settles for an empty string, which is enough for a sort, while equality has to stay unknown.
	 */
	@CheckReturnValue
	@Nullable
	private static String canonical(@Nonnull DetailAST annotation, int maxDepth, boolean asWritten) {
		if (maxDepth <= 0)
			return asWritten ? null : "";
		final var params = new TreeMap<String, String>();
		for (var child = annotation.getFirstChild(); child != null; child = child.getNextSibling()) {
			final String key;
			DetailAST value;
			if (child.getType() == TokenTypes.ANNOTATION_MEMBER_VALUE_PAIR) {
				final var keyIdent = child.findFirstToken(TokenTypes.IDENT);
				if (keyIdent == null)
					continue;

				key = keyIdent.getText();
				value = child.findFirstToken(TokenTypes.EXPR);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION_ARRAY_INIT);
				if (value == null)
					value = child.findFirstToken(TokenTypes.ANNOTATION);
				if (value == null)
					continue;
			}
			else if (child.getType() == TokenTypes.ANNOTATION || child.getType() == TokenTypes.EXPR
					|| child.getType() == TokenTypes.ANNOTATION_ARRAY_INIT) {
				key = "value";
				value = child;
			}
			else
				continue;

			final var text = value.getType() == TokenTypes.ANNOTATION
					? canonical(value, maxDepth - 1, asWritten)
					: valueText(value, maxDepth - 1, asWritten);
			if (text == null)
				return null;
			// the identity keeps the `@` a nested annotation value is written with, so it cannot
			// read the same as a constant of that name (`@W(@C)` against `@W(C)`)
			params.put(key, asWritten && value.getType() == TokenTypes.ANNOTATION ? "@" + text : text);
		}
		final var sb = new StringBuilder(asWritten ? writtenAnnotationName(annotation) : annotationName(annotation));
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
	 *
	 * <p>The name is the simple one, which makes this an ordering key, not an identity: see
	 * {@link #annotationIdentity} for comparing two annotations for equality.
	 */
	@CheckReturnValue
	@Nonnull
	public static String canonicalAnnotation(@Nonnull DetailAST annotation, int maxDepth) {
		return requireNonNull(canonical(annotation, maxDepth, false));
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
	 * The source tokens of {@code node}'s subtree in source order, leaving out comments and the
	 * imaginary grouping nodes ({@code EXPR}, {@code TYPE_ARGUMENTS}, ...) whose text is their
	 * token name. Each {@code ANNOTATION} becomes the one token {@code @} plus its
	 * {@link #annotationIdentity}, so annotations that differ only in parameter order compare
	 * equal while two types that share a simple name do not. Null when an annotation's identity
	 * is.
	 *
	 * <p>Ordered by position rather than read off the tree, because a node's own token does not
	 * sit at a fixed place among its children: a {@code DOT} spanning two generic segments holds
	 * {@code [Outer, <A>, Inner, <B>]} with its {@code .} after the second child, and an operator's
	 * text sits between its operands. Iterative, so a deep expression cannot overflow the stack.
	 */
	@CheckReturnValue
	@Nullable
	public static List<String> identityTokens(@Nonnull DetailAST node, int maxAnnotationDepth) {
		return tokens(node, maxAnnotationDepth, true);
	}

	/**
	 * Whether {@code type} is one of the four comment tokens a {@code WITH_COMMENTS} parse weaves
	 * into the tree: {@code SINGLE_LINE_COMMENT} and {@code BLOCK_COMMENT_BEGIN} head a comment
	 * subtree, {@code COMMENT_CONTENT} and {@code BLOCK_COMMENT_END} sit inside one.
	 */
	@CheckReturnValue
	public static boolean isCommentToken(int type) {
		return type == TokenTypes.SINGLE_LINE_COMMENT || type == TokenTypes.BLOCK_COMMENT_BEGIN
				|| type == TokenTypes.BLOCK_COMMENT_END || type == TokenTypes.COMMENT_CONTENT;
	}

	/**
	 * Whether {@code node} is a grouping node the parser made up rather than a source token. Its
	 * text is then its own token name ({@code EXPR}, {@code ELIST}); an identifier spelled
	 * {@code IDENT} is the one real token that can match it.
	 */
	@CheckReturnValue
	public static boolean isImaginaryToken(@Nonnull DetailAST node) {
		return node.getType() != TokenTypes.IDENT && node.getText().equals(TokenUtil.getTokenName(node.getType()));
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

	/**
	 * {@link #identityTokens} when {@code asWritten} is true; with each annotation as its
	 * {@link #canonicalAnnotation} ordering key otherwise, which never comes out null.
	 */
	@CheckReturnValue
	@Nullable
	private static List<String> tokens(@Nonnull DetailAST node, int maxAnnotationDepth, boolean asWritten) {
		final var nodes = new ArrayList<DetailAST>();
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(node);
		while (!stack.isEmpty()) {
			final var current = stack.pop();
			if (isCommentToken(current.getType()))
				continue;
			if (current.getType() == TokenTypes.ANNOTATION) {
				nodes.add(current);
				continue;
			}
			if (!isImaginaryToken(current))
				nodes.add(current);
			for (var child = current.getFirstChild(); child != null; child = child.getNextSibling())
				stack.push(child);
		}
		nodes.sort(Comparator.comparingInt(DetailAST::getLineNo).thenComparingInt(DetailAST::getColumnNo));
		final var tokens = new ArrayList<String>(nodes.size());
		for (var token : nodes) {
			if (token.getType() != TokenTypes.ANNOTATION) {
				tokens.add(token.getText());
				continue;
			}
			final var annotation = canonical(token, maxAnnotationDepth, asWritten);
			if (annotation == null)
				return null;
			tokens.add("@" + annotation);
		}
		return tokens;
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

	/**
	 * An annotation value's tokens joined into one comparable string, operators included. A space
	 * is kept only where two tokens would otherwise fuse into different ones ({@code - -1} against
	 * {@code --1}). Null when {@code asWritten} and a nested annotation's identity is.
	 */
	@CheckReturnValue
	@Nullable
	private static String valueText(@Nonnull DetailAST value, int maxDepth, boolean asWritten) {
		final var tokens = tokens(value, maxDepth, asWritten);
		if (tokens == null)
			return null;
		final var sb = new StringBuilder();
		// no token is empty: even a text block's content opens with its line terminator
		for (var token : tokens) {
			if (!sb.isEmpty() && wouldFuse(sb.charAt(sb.length() - 1), token.charAt(0)))
				sb.append(' ');
			sb.append(token);
		}
		return sb.toString();
	}

	@CheckReturnValue
	private static boolean wouldFuse(char last, char next) {
		if (Character.isJavaIdentifierPart(last))
			return Character.isJavaIdentifierPart(next);
		return OPERATOR_CHARS.indexOf(last) >= 0 && OPERATOR_CHARS.indexOf(next) >= 0;
	}

	/** The annotation's name as written, qualified when it was written qualified. */
	@CheckReturnValue
	@Nonnull
	private static String writtenAnnotationName(@Nonnull DetailAST annotation) {
		final var dot = annotation.findFirstToken(TokenTypes.DOT);
		return dot != null ? dottedName(dot) : annotationName(annotation);
	}

	private AstText() {
	}
}