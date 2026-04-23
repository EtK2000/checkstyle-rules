package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
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
public class FieldSortingCheck extends AbstractCheck {
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

	@CheckReturnValue
	@Nonnull
	private static String annotationDescription(@Nonnull List<String> sortedKeys) {
		if (sortedKeys.isEmpty())
			return "unannotated";
		final var key = sortedKeys.getFirst();
		final var paren = key.indexOf('(');
		var name = paren >= 0 ? key.substring(0, paren) : key;
		final var dot = name.lastIndexOf('.');
		if (dot >= 0)
			name = name.substring(dot + 1);
		return "annotated @" + name;
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
			case TokenTypes.DOT -> {
				buildTypeName(ast.getFirstChild(), sb);
				sb.append('.');
				var last = ast.getFirstChild();
				while (last.getNextSibling() != null)
					last = last.getNextSibling();
				sb.append(last.getText());
			}
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
			return 2; // non-final

		return varDef.findFirstToken(TokenTypes.ASSIGN) != null ? 0 : 1;
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
	private static int compareAnnotations(@Nonnull List<String> a, @Nonnull List<String> b) {
		for (var i = 0; i < Math.min(a.size(), b.size()); ++i) {
			final var cmp = a.get(i).compareToIgnoreCase(b.get(i));
			if (cmp != 0)
				return cmp;
		}
		return Integer.compare(a.size(), b.size());
	}

	@CheckReturnValue
	private static int compareTypes(@Nonnull String a, @Nonnull String b) {
		final var aBase = baseType(a);
		final var bBase = baseType(b);
		final var aPrim = PRIMITIVES.contains(aBase);
		final var bPrim = PRIMITIVES.contains(bBase);

		// primitives before reference types
		if (aPrim != bPrim)
			return aPrim ? -1 : 1;

		// same base type: sort by array depth (int before int[] before int[][])
		if (aBase.equals(bBase))
			return Integer.compare(arrayDepth(a), arrayDepth(b));

		// alphabetical by base type name
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
	private static Map<String, Set<String>> fieldDependencies(@Nonnull List<DetailAST> fields) {
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
	private static String fieldName(@Nonnull DetailAST varDef) {
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

	@CheckReturnValue
	@Nonnull
	private static String typeName(@Nonnull DetailAST varDef) {
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (type == null)
			return "";

		final var sb = new StringBuilder();
		buildTypeName(type.getFirstChild(), sb);

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

			// same chunk: check dependencies
			final var currName = fieldName(curr);
			final var prevName = fieldName(prev);
			final var currDeps = deps.get(currName);
			final var prevDeps = deps.get(prevName);

			// curr depends on prev: ordering is justified
			if (currDeps != null && currDeps.contains(prevName))
				continue;

			// prev depends on curr: curr should be declared first
			if (prevDeps != null && prevDeps.contains(currName)) {
				log(prev, MSG_DEPENDENCY, prevName, currName);
				continue;
			}

			// anonymous class initializers sort before non-anonymous
			final var prevAnon = hasAnonymousClassInit(prev);
			final var currAnon = hasAnonymousClassInit(curr);
			if (prevAnon != currAnon) {
				if (currAnon)
					log(curr, MSG_ANON_CLASS, fieldName(curr), fieldName(prev));
				continue;
			}

			// compare types
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

			if (currName.compareToIgnoreCase(prevName) < 0)
				log(curr, MSG_NAME, currName, prevName);
		}
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