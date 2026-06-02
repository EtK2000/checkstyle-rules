package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that enforces field ordering within static and instance
 * groups. Fields must be sorted in three chunks:
 * <ol>
 *     <li>finals with inline values</li>
 *     <li>finals without inline values</li>
 *     <li>non-finals</li>
 * </ol>
 * Within each chunk, primitives sort before reference types, then
 * alphabetical by type name. Array types sort right after their base type.
 * Fields of the same type sort alphabetically by name.
 */
public class FieldSortingCheck extends AbstractAstCheck {
	/**
	 * The rule-relevant classification of one field {@code VARIABLE_DEF}, exposed so the
	 * fixer reorders fields with the check's rule rather than re-deriving it via regex.
	 */
	public record FieldInfo(
			@Nonnull DetailAST varDef,
			@Nonnull String name,
			int chunk,
			boolean isStatic,
			@Nonnull String sortType,
			@Nonnull List<String> annotationKeys,
			@Nonnull List<List<String>> typeArgAnnotationKeys,
			boolean anonInit
	) {}

	private static final int MAX_ANNOTATION_DEPTH = 50;
	private static final Set<String> PRIMITIVES = Set.of(
			"boolean", "byte", "char", "double", "float", "int", "long", "short"
	);
	private static final String MSG_ANNOTATION = "field.sort.annotation";
	private static final String MSG_ANON_CLASS = "field.sort.anon.class";
	private static final String MSG_CHUNK = "field.sort.chunk";
	private static final String MSG_DEPENDENCY = "field.sort.dependency";
	private static final String MSG_ENUM_CONSTANT = "field.sort.enum.constant";
	private static final String MSG_ENUM_SAME_LINE = "field.sort.enum.same.line";
	private static final String MSG_NAME = "field.sort.name";
	private static final String MSG_TYPE = "field.sort.type";

	private static void addAnnotationsFrom(@Nonnull DetailAST parent, @Nonnull List<String> keys) {
		final var annotations = parent.findFirstToken(TokenTypes.ANNOTATIONS);
		if (annotations == null)
			return;
		for (var ann = annotations.getFirstChild(); ann != null; ann = ann.getNextSibling()) {
			if (ann.getType() == TokenTypes.ANNOTATION)
				keys.add(AstUtil.canonicalAnnotation(ann, MAX_ANNOTATION_DEPTH));
		}
	}

	@CheckReturnValue
	@Nonnull
	private static String annotationDescription(@Nonnull List<String> sortedKeys) {
		if (sortedKeys.isEmpty())
			return "unannotated";
		return "annotated @" + annotationSimpleName(sortedKeys.getFirst());
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> annotationKeys(@Nonnull DetailAST varDef) {
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null)
			return List.of();
		final var keys = new ArrayList<String>();
		for (var child = modifiers.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ANNOTATION)
				keys.add(AstUtil.canonicalAnnotation(child, MAX_ANNOTATION_DEPTH));
		}
		if (keys.isEmpty())
			return List.of();
		keys.sort(String.CASE_INSENSITIVE_ORDER);
		return keys;
	}

	@CheckReturnValue
	@Nonnull
	private static String annotationSimpleName(@Nonnull String canonicalKey) {
		final var paren = canonicalKey.indexOf('(');
		var name = paren >= 0 ? canonicalKey.substring(0, paren) : canonicalKey;
		final var dot = name.lastIndexOf('.');
		if (dot >= 0)
			name = name.substring(dot + 1);
		return name;
	}

	@CheckReturnValue
	private static int arrayDepth(@Nonnull String typeName) {
		var depth = 0;
		var idx = typeName.indexOf('[');
		while (idx >= 0) {
			++depth;
			idx = typeName.indexOf('[', idx + 1);
		}
		return depth;
	}

	@CheckReturnValue
	@Nonnull
	private static String baseType(@Nonnull String typeName) {
		final var bracket = typeName.indexOf('[');
		return bracket < 0 ? typeName : typeName.substring(0, bracket);
	}

	private static void buildTypeName(@Nonnull DetailAST ast, @Nonnull StringBuilder sb) {
		switch (ast.getType()) {
			case TokenTypes.ARRAY_DECLARATOR -> {
				buildTypeName(ast.getFirstChild(), sb);
				sb.append("[]");
			}
			// walking to the last child would pick up a TYPE_ARGUMENTS sibling and render
			// its token name; dottedName stops at the qualified segments
			case TokenTypes.DOT -> sb.append(AstUtil.dottedName(ast));
			case TokenTypes.IDENT, TokenTypes.LITERAL_BOOLEAN, TokenTypes.LITERAL_BYTE,
			     TokenTypes.LITERAL_CHAR, TokenTypes.LITERAL_DOUBLE, TokenTypes.LITERAL_FLOAT,
			     TokenTypes.LITERAL_INT, TokenTypes.LITERAL_LONG,
			     TokenTypes.LITERAL_SHORT -> sb.append(ast.getText());
		}
	}

	@CheckReturnValue
	@Nonnull
	private static String chunkName(int chunk) {
		return switch (chunk) {
			case 0 -> "final with inline value";
			case 1 -> "final without inline value";
			default -> "non-final";
		};
	}

	@CheckReturnValue
	private static int chunkOf(@Nonnull DetailAST varDef) {
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null || modifiers.findFirstToken(TokenTypes.FINAL) == null)
			return 2;

		return varDef.findFirstToken(TokenTypes.ASSIGN) != null ? 0 : 1;
	}

	@CheckReturnValue
	@Nonnull
	public static FieldInfo classifyField(@Nonnull DetailAST varDef) {
		return new FieldInfo(
				varDef,
				fieldName(varDef),
				chunkOf(varDef),
				isStatic(varDef),
				typeName(varDef),
				annotationKeys(varDef),
				typeArgAnnotationKeys(varDef),
				hasAnonymousClassInit(varDef)
		);
	}

	private static void collectIdents(@Nonnull DetailAST ast, @Nonnull Set<String> result) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			// skip anonymous class bodies: references inside methods are deferred, not init-time
			if (child.getType() == TokenTypes.OBJBLOCK && ast.getType() == TokenTypes.LITERAL_NEW)
				continue;
			if (child.getType() == TokenTypes.IDENT)
				result.add(child.getText());
			collectIdents(child, result);
		}
	}

	@CheckReturnValue
	public static int compareAnnotations(@Nonnull List<String> a, @Nonnull List<String> b) {
		for (var i = 0; i < Math.min(a.size(), b.size()); ++i) {
			final var cmp = a.get(i).compareToIgnoreCase(b.get(i));
			if (cmp != 0)
				return cmp;
		}
		return Integer.compare(a.size(), b.size());
	}

	@CheckReturnValue
	public static int compareTypeArgAnnotations(
			@Nonnull List<List<String>> a, @Nonnull List<List<String>> b
	) {
		for (var i = 0; i < Math.min(a.size(), b.size()); ++i) {
			final var cmp = compareAnnotations(a.get(i), b.get(i));
			if (cmp != 0)
				return cmp;
		}
		return Integer.compare(a.size(), b.size());
	}

	@CheckReturnValue
	public static int compareTypes(@Nonnull String a, @Nonnull String b) {
		final var aBase = baseType(a);
		final var bBase = baseType(b);
		final var aPrim = PRIMITIVES.contains(aBase);
		final var bPrim = PRIMITIVES.contains(bBase);

		if (aPrim != bPrim)
			return aPrim ? -1 : 1;

		if (aBase.equals(bBase))
			return Integer.compare(arrayDepth(a), arrayDepth(b));

		return aBase.compareToIgnoreCase(bBase);
	}

	@CheckReturnValue
	private static boolean containsAnonymousClass(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.LITERAL_NEW && child.findFirstToken(TokenTypes.OBJBLOCK) != null)
				return true;
			if (containsAnonymousClass(child))
				return true;
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	public static Map<String, Set<String>> fieldDependencies(@Nonnull List<DetailAST> fields) {
		final var fieldNames = new HashSet<String>();
		for (var field : fields)
			fieldNames.add(fieldName(field));

		final var deps = new HashMap<String, Set<String>>();
		for (var field : fields) {
			final var assign = field.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				continue;

			final var refs = new HashSet<String>();
			collectIdents(assign, refs);
			refs.retainAll(fieldNames);
			refs.remove(fieldName(field));
			if (!refs.isEmpty())
				deps.put(fieldName(field), refs);
		}
		return deps;
	}

	@CheckReturnValue
	@Nonnull
	public static String fieldName(@Nonnull DetailAST varDef) {
		final var ident = varDef.findFirstToken(TokenTypes.IDENT);
		return ident != null ? ident.getText() : "";
	}

	@CheckReturnValue
	private static boolean hasAnonymousClassInit(@Nonnull DetailAST varDef) {
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		return assign != null && containsAnonymousClass(assign);
	}

	@CheckReturnValue
	private static boolean isStatic(@Nonnull DetailAST varDef) {
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null;
	}

	/**
	 * Returns the {@code OBJBLOCK} whose {@code VARIABLE_DEF} or {@code ENUM_CONSTANT_DEF}
	 * child the check reported at {@code (line, column)} (0-based), or {@code null} when no
	 * such member sits there.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST objblockAt(@Nonnull DetailAST root, int line, int column) {
		final var member = AstUtil.findNodeAt(
				root,
				line,
				column,
				node -> node.getType() == TokenTypes.VARIABLE_DEF || node.getType() == TokenTypes.ENUM_CONSTANT_DEF
		);
		if (member == null)
			return null;
		final var parent = member.getParent();
		return parent != null && parent.getType() == TokenTypes.OBJBLOCK ? parent : null;
	}

	@CheckReturnValue
	@Nonnull
	private static String typeArgAnnotationDescription(@Nonnull List<String> sortedKeys) {
		if (sortedKeys.isEmpty())
			return "type argument unannotated";
		return "type argument annotated @" + annotationSimpleName(sortedKeys.getFirst());
	}

	@CheckReturnValue
	@Nonnull
	private static List<List<String>> typeArgAnnotationKeys(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return List.of();

		var baseAst = type.getFirstChild();
		while (baseAst != null && baseAst.getType() == TokenTypes.ARRAY_DECLARATOR)
			baseAst = baseAst.getFirstChild();
		if (baseAst == null)
			return List.of();

		DetailAST typeArgs = null;
		for (var sibling = baseAst.getNextSibling(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling.getType() == TokenTypes.TYPE_ARGUMENTS) {
				typeArgs = sibling;
				break;
			}
		}
		if (typeArgs == null && baseAst.getType() == TokenTypes.DOT)
			typeArgs = baseAst.findFirstToken(TokenTypes.TYPE_ARGUMENTS);

		if (typeArgs == null)
			return List.of();

		final var result = new ArrayList<List<String>>();
		for (var child = typeArgs.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.TYPE_ARGUMENT)
				continue;
			final var keys = new ArrayList<String>();
			addAnnotationsFrom(child, keys);
			for (var tc = child.getFirstChild(); tc != null; tc = tc.getNextSibling()) {
				if (tc.getType() == TokenTypes.TYPE_UPPER_BOUNDS
						|| tc.getType() == TokenTypes.TYPE_LOWER_BOUNDS)
					addAnnotationsFrom(tc, keys);
			}
			if (keys.isEmpty())
				result.add(List.of());
			else {
				keys.sort(String.CASE_INSENSITIVE_ORDER);
				result.add(keys);
			}
		}
		return result;
	}

	@CheckReturnValue
	@Nonnull
	private static String typeName(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";

		final var baseType = type.getFirstChild();
		if (baseType == null)
			return "";

		final var sb = new StringBuilder();
		buildTypeName(baseType, sb);

		// count array declarators (can be on the type or after the ident)
		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				sb.append("[]");
		}
		return sb.toString();
	}

	private void checkEnumConstants(@Nonnull List<DetailAST> constants) {
		for (var i = 1; i < constants.size(); ++i) {
			final var prevIdent = constants.get(i - 1).findFirstToken(TokenTypes.IDENT);
			final var currIdent = constants.get(i).findFirstToken(TokenTypes.IDENT);
			final var prevName = prevIdent.getText();
			final var currName = currIdent.getText();

			if (currIdent.getLineNo() == prevIdent.getLineNo())
				log(constants.get(i), MSG_ENUM_SAME_LINE, currName);

			if (currName.compareToIgnoreCase(prevName) < 0)
				log(constants.get(i), MSG_ENUM_CONSTANT, currName, prevName);
		}
	}

	private void checkFieldGroup(@Nonnull List<DetailAST> fields) {
		if (fields.size() < 2)
			return;

		final var deps = fieldDependencies(fields);

		for (var i = 1; i < fields.size(); ++i) {
			final var prev = fields.get(i - 1);
			final var curr = fields.get(i);
			final var prevChunk = chunkOf(prev);
			final var currChunk = chunkOf(curr);

			if (currChunk < prevChunk) {
				log(curr, MSG_CHUNK, fieldName(curr), chunkName(currChunk), chunkName(prevChunk));
				continue;
			}
			if (currChunk > prevChunk)
				continue;

			final var currName = fieldName(curr);
			final var prevName = fieldName(prev);
			final var currDeps = deps.get(currName);
			final var prevDeps = deps.get(prevName);

			if (currDeps != null && currDeps.contains(prevName))
				continue;

			if (prevDeps != null && prevDeps.contains(currName)) {
				log(prev, MSG_DEPENDENCY, prevName, currName);
				continue;
			}

			final var prevAnon = hasAnonymousClassInit(prev);
			final var currAnon = hasAnonymousClassInit(curr);
			if (prevAnon != currAnon) {
				if (currAnon)
					log(curr, MSG_ANON_CLASS, fieldName(curr), fieldName(prev));
				continue;
			}

			final var prevType = typeName(prev);
			final var currType = typeName(curr);
			final var typeCmp = compareTypes(currType, prevType);

			if (typeCmp < 0) {
				log(curr, MSG_TYPE, fieldName(curr), currType, fieldName(prev), prevType);
				continue;
			}
			if (typeCmp > 0)
				continue;

			final var prevAnnotations = annotationKeys(prev);
			final var currAnnotations = annotationKeys(curr);
			final var annotCmp = compareAnnotations(currAnnotations, prevAnnotations);

			if (annotCmp < 0) {
				log(
						curr,
						MSG_ANNOTATION,
						currName,
						annotationDescription(currAnnotations),
						prevName,
						annotationDescription(prevAnnotations)
				);
				continue;
			}
			if (annotCmp > 0)
				continue;

			final var prevTypeArgAnns = typeArgAnnotationKeys(prev);
			final var currTypeArgAnns = typeArgAnnotationKeys(curr);
			final var typeArgAnnotCmp = compareTypeArgAnnotations(currTypeArgAnns, prevTypeArgAnns);

			if (typeArgAnnotCmp < 0) {
				var diffPos = Math.min(currTypeArgAnns.size(), prevTypeArgAnns.size());
				for (var j = 0; j < diffPos; ++j) {
					if (compareAnnotations(currTypeArgAnns.get(j), prevTypeArgAnns.get(j)) != 0) {
						diffPos = j;
						break;
					}
				}
				final var currAnnsAtPos = diffPos < currTypeArgAnns.size()
						? currTypeArgAnns.get(diffPos) : List.<String>of();
				final var prevAnnsAtPos = diffPos < prevTypeArgAnns.size()
						? prevTypeArgAnns.get(diffPos) : List.<String>of();
				log(
						curr,
						MSG_ANNOTATION,
						currName,
						typeArgAnnotationDescription(currAnnsAtPos),
						prevName,
						typeArgAnnotationDescription(prevAnnsAtPos)
				);
				continue;
			}
			if (typeArgAnnotCmp > 0)
				continue;

			if (currName.compareToIgnoreCase(prevName) < 0)
				log(curr, MSG_NAME, currName, prevName);
		}
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var parent = ast.getParent();
		if (parent != null) {
			var parentModifiers = parent.findFirstToken(TokenTypes.MODIFIERS);
			if (parentModifiers == null)
				parentModifiers = parent.findFirstToken(TokenTypes.ANNOTATIONS);
			if (parentModifiers != null && AstUtil.hasSuppressWarnings(parentModifiers, "FieldSorting"))
				return;
		}

		final var enumConstants = new ArrayList<DetailAST>();
		final var staticFields = new ArrayList<DetailAST>();
		final var instanceFields = new ArrayList<DetailAST>();

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ENUM_CONSTANT_DEF)
				enumConstants.add(child);
			else if (child.getType() == TokenTypes.VARIABLE_DEF) {
				if (isStatic(child))
					staticFields.add(child);
				else
					instanceFields.add(child);
			}
		}

		checkEnumConstants(enumConstants);
		checkFieldGroup(staticFields);
		checkFieldGroup(instanceFields);
	}
}