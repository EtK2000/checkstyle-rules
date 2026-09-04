package com.etk2000.checkstyle;

import com.etk2000.checkstyle.FieldInitializers.Kind;
import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

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
	 * The rule-relevant classification of one field {@code VARIABLE_DEF}.
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

	/**
	 * The names one body of an initializer reads ({@link #walkInitializer}): the initializer itself,
	 * whose {@code enclosing} is null, or an anonymous class body in it, whose own member names are
	 * dropped from what it reads before they join the {@code enclosing} body's.
	 */
	private record InitializerBody(@Nonnull Set<String> reads, @Nonnull Set<String> members, @Nullable InitializerBody enclosing) {}

	// tokens that keep a primitive or String initializer from being a constant expression of
	// literals alone: any name (a call, a field, a type), a switch expression, `this`, a created
	// array or a class literal of a primitive, which a String concatenation turns into text, and
	// `null`, which no constant expression holds
	private static final Set<Integer> NON_CONSTANT_TOKENS = Set.of(
			TokenTypes.IDENT,
			TokenTypes.LITERAL_CLASS,
			TokenTypes.LITERAL_NEW,
			TokenTypes.LITERAL_NULL,
			TokenTypes.LITERAL_SWITCH,
			TokenTypes.LITERAL_THIS
	);
	private static final Set<String> PRIMITIVES = Set.of(
			"boolean", "byte", "char", "double", "float", "int", "long", "short"
	);
	private static final String MSG_ANNOTATION = "field.sort.annotation";
	private static final String MSG_ANON_CLASS = "field.sort.anon.class";
	private static final String MSG_CHUNK = "field.sort.chunk";
	private static final String MSG_DEPENDENCY = "field.sort.dependency";
	private static final String MSG_ENUM_CONSTANT = "field.sort.enum.constant";
	private static final String MSG_ENUM_DEPENDENCY = "field.sort.enum.dependency";
	private static final String MSG_ENUM_SAME_LINE = "field.sort.enum.same.line";
	private static final String MSG_NAME = "field.sort.name";
	private static final String MSG_TYPE = "field.sort.type";

	private static void addAnnotationsFrom(@Nonnull DetailAST parent, @Nonnull List<String> keys) {
		final var annotations = parent.findFirstToken(TokenTypes.ANNOTATIONS);
		if (annotations == null)
			return;
		for (var ann = annotations.getFirstChild(); ann != null; ann = ann.getNextSibling()) {
			if (ann.getType() == TokenTypes.ANNOTATION)
				keys.add(AstText.canonicalAnnotation(ann, AstText.MAX_ANNOTATION_DEPTH));
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
				keys.add(AstText.canonicalAnnotation(child, AstText.MAX_ANNOTATION_DEPTH));
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

	/**
	 * The names among {@code fields}, one static or instance group, whose place the
	 * anonymous-class rule decides: each field with an anonymous-class initializer, and each that
	 * reads one, directly or through other fields. A free plain field has to precede an
	 * anonymous-class one of its chunk and a reader to follow it, so one declaration holding both
	 * kinds has no place this check accepts.
	 */
	@CheckReturnValue
	@Nonnull
	public static Set<String> anonymousClassBound(@Nonnull List<DetailAST> fields) {
		final var anonNames = new HashSet<String>();
		for (var field : fields) {
			if (hasAnonymousClassInit(field))
				anonNames.add(fieldName(field));
		}
		final var bound = new HashSet<String>();
		if (anonNames.isEmpty())
			return bound;
		final var readers = new HashMap<String, Set<String>>();
		for (var entry : fieldDependencies(fields).entrySet()) {
			for (var read : entry.getValue())
				readers.computeIfAbsent(read, name -> new HashSet<>()).add(entry.getKey());
		}
		final var pending = new ArrayDeque<>(anonNames);
		bound.addAll(anonNames);
		while (!pending.isEmpty()) {
			for (var reader : readers.getOrDefault(pending.pop(), Set.of())) {
				if (bound.add(reader))
					pending.push(reader);
			}
		}
		return bound;
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
			case TokenTypes.DOT -> sb.append(AstText.dottedName(ast));
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

	/**
	 * Adds to {@code result} the names the initializer {@code assign}, of a field of the class body
	 * {@code ownBody}, reads: bare, or selected through {@code this} or the class's own name
	 * ({@link FieldInitializers#isOwnName}), and to {@code outerResult} the ones it reads through
	 * {@code Outer.this}, {@code Outer} being that class's own name too. A member of any other object
	 * or type ({@code other.count}, {@code super.count}, {@code System.out}, {@code Other.this.count})
	 * is no read of a field of the class, and neither is a name that reads no field at all
	 * ({@link FieldInitializers#nonReads}) nor a {@code case} label naming none of the class's
	 * {@code constants}, its constant variables, since it names a constant of the selector's enum
	 * ({@link FieldInitializers#isCaseLabel}). Inside an anonymous class body only its member
	 * initializers and initializer blocks count, since they run while the field is initialized,
	 * and a name the body declares as one of its own fields refers to that member, so it is
	 * dropped; a name reached through {@code Outer.this} bypasses that, since an anonymous class
	 * has no name to qualify.
	 */
	private static void collectIdents(
			@Nonnull DetailAST assign,
			@Nonnull DetailAST ownBody,
			@Nonnull Set<String> constants,
			@Nonnull Set<String> result,
			@Nonnull Set<String> outerResult
	) {
		final var nonReads = FieldInitializers.nonReads(assign);
		walkInitializer(
				assign,
				result,
				(ident, body) -> {
					if (nonReads.contains(ident) || FieldInitializers.isCaseLabel(ident) && !constants.contains(ident.getText()))
						return;
					final var parent = ident.getParent();
					final var selected = parent.getType() == TokenTypes.DOT && parent.getLastChild() == ident;
					if (isQualifiedThisMember(ident, ownBody))
						outerResult.add(ident.getText());
					else if (!selected || isSelfQualifiedField(parent, ident, ownBody))
						body.reads().add(ident.getText());
				}
		);
	}

	/**
	 * Adds the names {@code assign}, the initializer of a field of the class body {@code ownBody},
	 * reads while it runs in a form that compiles ahead of the field's declaration: qualified
	 * through {@code this}, or through {@code Outer.this} or the type's own name
	 * ({@link FieldInitializers#isOwnName}), assigned ({@code b = 5}), or any form inside an
	 * anonymous class body, less the body's own members. A bare name read directly by a field
	 * initializer is left out: ahead of its declaration it does not compile (JLS 8.3.3). A lambda
	 * body counts, since one called while initializing (a stream's {@code map}) runs then. A name
	 * that reads no field ({@link FieldInitializers#nonReads}) is left out too, and so is a
	 * {@code case} label ({@link FieldInitializers#isCaseLabel}), which names a constant variable,
	 * whose value javac inlines, or a constant of the selector's enum. Anonymous class
	 * methods are skipped: what an initializer runs through a call is not traced, it makes the
	 * field {@link FieldInitializers.Kind#RUNS_CODE} instead.
	 */
	private static void collectInitTimeReads(@Nonnull DetailAST assign, @Nonnull Set<String> result, @Nonnull Set<String> outerResult, @Nonnull DetailAST ownBody) {
		final var nonReads = FieldInitializers.nonReads(assign);
		walkInitializer(
				assign,
				result,
				(ident, body) -> {
					if (nonReads.contains(ident) || FieldInitializers.isCaseLabel(ident))
						return;
					final var parent = ident.getParent();
					if (isQualifiedThisMember(ident, ownBody))
						outerResult.add(ident.getText());
					else if (body.enclosing() != null || isSelfQualifiedField(parent, ident, ownBody) || isWrittenTo(parent, ident))
						body.reads().add(ident.getText());
				}
		);
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

	/** True when the enum constant {@code constant} sorts ahead of {@code previous}, the one declared right before it. */
	@CheckReturnValue
	private static boolean constantSortsAhead(@Nonnull DetailAST previous, @Nonnull DetailAST constant) {
		return fieldName(constant).compareToIgnoreCase(fieldName(previous)) < 0;
	}

	@CheckReturnValue
	private static boolean containsAnonymousClass(@Nonnull DetailAST ast) {
		final var pending = new ArrayDeque<DetailAST>();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			pending.push(child);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (node.getType() == TokenTypes.LITERAL_NEW && node.findFirstToken(TokenTypes.OBJBLOCK) != null)
				return true;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
		return false;
	}

	/**
	 * The name at {@code index} of {@code fields}, alone when {@code single}, else with the names
	 * of the declaration it opens.
	 */
	@CheckReturnValue
	@Nonnull
	private static Set<String> declarationNames(@Nonnull List<DetailAST> fields, int index, boolean single) {
		final var names = new HashSet<String>();
		names.add(fieldName(fields.get(index)));
		for (var i = index + 1; !single && i < fields.size() && isLaterDeclarator(fields.get(i)); ++i)
			names.add(fieldName(fields.get(i)));
		return names;
	}

	/** True when a name of {@code first} reads one of {@code second}, or the other way round. */
	@CheckReturnValue
	private static boolean dependsAcross(@Nonnull Map<String, Set<String>> deps, @Nonnull Set<String> first, @Nonnull Set<String> second) {
		for (var name : first) {
			for (var read : deps.getOrDefault(name, Set.of())) {
				if (second.contains(read))
					return true;
			}
		}
		for (var name : second) {
			for (var read : deps.getOrDefault(name, Set.of())) {
				if (first.contains(read))
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	public static Map<String, Set<String>> fieldDependencies(@Nonnull List<DetailAST> fields) {
		final var constants = new HashSet<String>();
		final var fieldNames = new HashSet<String>();
		for (var field : fields) {
			fieldNames.add(fieldName(field));
			if (field.findFirstToken(TokenTypes.ASSIGN) != null && !isInitializedAtRuntime(field))
				constants.add(fieldName(field));
		}

		final var deps = new HashMap<String, Set<String>>();
		for (var field : fields) {
			final var assign = field.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				continue;

			final var refs = new HashSet<String>();
			collectIdents(assign, field.getParent(), constants, refs, refs);
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

	/**
	 * For each of {@code fields}, given in source order, the fields declared after it, static alike,
	 * that its initializer reads while it runs, in a form that compiles there
	 * ({@link #collectInitTimeReads}). Such a read sees the later field's default value, so any
	 * reorder that puts the later field first changes what the initializer computes. A later field
	 * that {@link #isInitializedAtRuntime} rejects reads the same wherever it is declared, so it is
	 * left out.
	 */
	@CheckReturnValue
	@Nonnull
	public static Map<String, Set<String>> initTimeForwardReads(@Nonnull List<DetailAST> fields) {
		final var positions = new HashMap<String, Integer>();
		for (var i = 0; i < fields.size(); ++i)
			positions.putIfAbsent(fieldName(fields.get(i)), i);
		final var reads = new HashMap<String, Set<String>>();
		for (var i = 0; i < fields.size(); ++i) {
			final var field = fields.get(i);
			final var assign = field.findFirstToken(TokenTypes.ASSIGN);
			if (assign == null)
				continue;
			final var idents = new HashSet<String>();
			collectInitTimeReads(assign, idents, idents, field.getParent());
			for (var ident : idents) {
				final var at = positions.get(ident);
				if (at != null && at > i && isStatic(fields.get(at)) == isStatic(field) && isInitializedAtRuntime(fields.get(at)))
					reads.computeIfAbsent(fieldName(field), name -> new HashSet<>()).add(ident);
			}
		}
		return reads;
	}

	/**
	 * True when the declaration of {@code varDef} gives it a value as it runs: it has an
	 * initializer, and is not a constant variable (JLS 4.12.4), whose value javac inlines wherever
	 * it is read. Only a {@code final} primitive or {@code String} initialized from literals alone
	 * counts as a constant here, an interface's or annotation type's field being final without the
	 * keyword; one naming another constant does not, which only ever keeps a reorder from happening.
	 */
	@CheckReturnValue
	static boolean isInitializedAtRuntime(@Nonnull DetailAST varDef) {
		final var assign = varDef.findFirstToken(TokenTypes.ASSIGN);
		if (assign == null)
			return false;
		final var type = varDef.findFirstToken(TokenTypes.TYPE);
		if (!AstQuery.hasModifier(varDef, TokenTypes.FINAL) && !isInterfaceMember(varDef) || type.findFirstToken(TokenTypes.ARRAY_DECLARATOR) != null)
			return true;
		final var typeNode = type.getFirstChild();
		final var typeName = typeNode.getType() == TokenTypes.DOT ? FullIdent.createFullIdent(typeNode).getText() : typeNode.getText();
		final var isString = "String".equals(typeName) || "java.lang.String".equals(typeName);
		return !PRIMITIVES.contains(typeName) && !isString || !isLiteralOnly(assign);
	}

	/** True when {@code varDef} is a member of an interface or annotation type, implicitly static and final. */
	@CheckReturnValue
	public static boolean isInterfaceMember(@Nonnull DetailAST varDef) {
		final var body = varDef.getParent();
		final var type = body == null ? null : body.getParent();
		return type != null && (type.getType() == TokenTypes.INTERFACE_DEF || type.getType() == TokenTypes.ANNOTATION_DEF);
	}

	/** True when {@code varDef} is a declaration's second name or later ({@code b} in {@code int a, b;}). */
	@CheckReturnValue
	private static boolean isLaterDeclarator(@Nonnull DetailAST varDef) {
		final var previous = varDef.getPreviousSibling();
		return previous != null && previous.getType() == TokenTypes.COMMA;
	}

	/**
	 * True when everything under {@code ast} is a literal or an operator: no name, switch,
	 * {@code this}, created array, class literal or {@code null} ({@link #NON_CONSTANT_TOKENS}), and
	 * no division or remainder that may be integral and by zero
	 * ({@link FieldInitializers#mayDivideByZero}), which would throw.
	 */
	@CheckReturnValue
	private static boolean isLiteralOnly(@Nonnull DetailAST ast) {
		final var pending = new ArrayDeque<DetailAST>();
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling())
			pending.push(child);
		while (!pending.isEmpty()) {
			final var node = pending.pop();
			if (NON_CONSTANT_TOKENS.contains(node.getType()) || FieldInitializers.mayDivideByZero(node))
				return false;
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling())
				pending.push(child);
		}
		return true;
	}

	/**
	 * True when {@code ident} is the member {@code Outer.this} selects, {@code Outer} naming the
	 * class whose body {@code ownBody} is ({@link FieldInitializers#isOwnName}).
	 */
	@CheckReturnValue
	private static boolean isQualifiedThisMember(@Nonnull DetailAST ident, @Nonnull DetailAST ownBody) {
		final var qualifier = ident.getPreviousSibling();
		return ident.getParent().getType() == TokenTypes.DOT
				&& qualifier != null
				&& qualifier.getType() == TokenTypes.DOT
				&& qualifier.getLastChild().getType() == TokenTypes.LITERAL_THIS
				&& FieldInitializers.isOwnName(qualifier.getFirstChild(), ownBody);
	}

	/**
	 * True when {@code ident}, the member {@code dot} selects, is a field reached through
	 * {@code this} or the name of the class whose body {@code ownBody} is
	 * ({@link FieldInitializers#isOwnName}): not a method being called, and not a member of some
	 * other object or type ({@code other.count}, {@code super.count}, {@code Other.count}).
	 */
	@CheckReturnValue
	private static boolean isSelfQualifiedField(@Nonnull DetailAST dot, @Nonnull DetailAST ident, @Nonnull DetailAST ownBody) {
		final var qualifier = ident.getPreviousSibling();
		final var parent = dot.getParent();
		if (dot.getType() != TokenTypes.DOT || qualifier == null
				|| parent.getType() == TokenTypes.METHOD_CALL && parent.getFirstChild() == dot)
			return false;
		return qualifier.getType() == TokenTypes.LITERAL_THIS || FieldInitializers.isOwnName(qualifier, ownBody);
	}

	@CheckReturnValue
	private static boolean isStatic(@Nonnull DetailAST varDef) {
		final var modifiers = varDef.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null || isInterfaceMember(varDef);
	}

	/**
	 * True when the type whose body {@code objBlock} is carries
	 * {@code @SuppressWarnings("FieldSorting")}, which leaves its fields in the order written.
	 */
	@CheckReturnValue
	public static boolean isSuppressed(@Nonnull DetailAST objBlock) {
		final var parent = objBlock.getParent();
		if (parent == null)
			return false;
		var parentModifiers = parent.findFirstToken(TokenTypes.MODIFIERS);
		if (parentModifiers == null)
			parentModifiers = parent.findFirstToken(TokenTypes.ANNOTATIONS);
		return parentModifiers != null && AstQuery.hasSuppressWarnings(parentModifiers, "FieldSorting");
	}

	/**
	 * True when {@code ident} is what {@code parent} assigns ({@code b = 5}). A compound assignment
	 * or an increment of a bare name is not checked: ahead of the declaration javac rejects it as an
	 * illegal forward reference, lambda bodies included.
	 */
	@CheckReturnValue
	private static boolean isWrittenTo(@Nonnull DetailAST parent, @Nonnull DetailAST ident) {
		return parent.getType() == TokenTypes.ASSIGN && parent.getFirstChild() == ident;
	}

	/**
	 * Returns the {@code OBJBLOCK} whose {@code VARIABLE_DEF} or {@code ENUM_CONSTANT_DEF}
	 * child the check reported at {@code (line, column)} (0-based), or {@code null} when no
	 * such member sits there.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST objblockAt(@Nonnull DetailAST root, int line, int column) {
		final var member = AstQuery.findNodeAt(
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

	/**
	 * True when a name of {@code first} and one of {@code second} are
	 * {@linkplain FieldInitializers#pinned pinned}, given their {@code kinds}.
	 */
	@CheckReturnValue
	private static boolean pinnedAcross(@Nonnull Map<String, Kind> kinds, @Nonnull Set<String> first, @Nonnull Set<String> second) {
		for (var a : first) {
			for (var b : second) {
				if (FieldInitializers.pinned(kinds.get(a), kinds.get(b)))
					return true;
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	public static Set<String> transitiveDependencies(@Nonnull String name, @Nonnull Map<String, Set<String>> deps) {
		final var result = new HashSet<String>();
		final var pending = new ArrayDeque<String>();
		pending.push(name);
		while (!pending.isEmpty()) {
			final var direct = deps.get(pending.pop());
			if (direct == null)
				continue;
			for (var dep : direct) {
				if (result.add(dep))
					pending.push(dep);
			}
		}
		return result;
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

		for (var child = type.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.ARRAY_DECLARATOR)
				sb.append("[]");
		}
		return sb.toString();
	}

	/**
	 * Hands {@code visitor} every identifier of the initializer {@code assign} that runs while the
	 * field is initialized, with the {@link InitializerBody} it sits in: the initializer's own, which
	 * gathers into {@code result}, or an anonymous class body's, of whose members only the
	 * initializers and initializer blocks are walked. Each body's reads, less its own member names,
	 * then join the body around it.
	 */
	private static void walkInitializer(@Nonnull DetailAST assign, @Nonnull Set<String> result, @Nonnull BiConsumer<DetailAST, InitializerBody> visitor) {
		final var bodies = new ArrayList<InitializerBody>();
		final var nodes = new ArrayDeque<DetailAST>();
		final var owners = new ArrayDeque<InitializerBody>();
		nodes.push(assign);
		owners.push(new InitializerBody(result, Set.of(), null));
		while (!nodes.isEmpty()) {
			final var node = nodes.pop();
			final var owner = owners.pop();
			if (node.getType() == TokenTypes.OBJBLOCK && node.getParent().getType() == TokenTypes.LITERAL_NEW) {
				final var body = new InitializerBody(new HashSet<>(), new HashSet<>(), owner);
				bodies.add(body);
				for (var member = node.getFirstChild(); member != null; member = member.getNextSibling()) {
					final var assigned = member.findFirstToken(TokenTypes.ASSIGN);
					if (member.getType() == TokenTypes.VARIABLE_DEF)
						body.members().add(fieldName(member));
					final var walked = member.getType() == TokenTypes.INSTANCE_INIT || member.getType() == TokenTypes.STATIC_INIT
							? member
							: member.getType() == TokenTypes.VARIABLE_DEF ? assigned : null;
					if (walked != null) {
						nodes.push(walked);
						owners.push(body);
					}
				}
				continue;
			}
			if (node.getType() == TokenTypes.IDENT)
				visitor.accept(node, owner);
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				nodes.push(child);
				owners.push(owner);
			}
		}
		for (var i = bodies.size() - 1; i >= 0; --i) {
			final var body = bodies.get(i);
			body.reads().removeAll(body.members());
			body.enclosing().reads().addAll(body.reads());
		}
	}

	/**
	 * Reports each anonymous-class field followed, in its chunk, by a plain field that
	 * could precede it: one that reads no anonymous-class field of the chunk (directly or
	 * through other fields) and that the anonymous-class field does not read either. A
	 * plain field reading an anonymous-class field only has to follow what it reads, which
	 * the dependency rule enforces; requiring more would leave {@code a; ra; b; rb} (each
	 * plain reading one anonymous-class field) with no order satisfying the rule. A plain field
	 * {@linkplain FieldInitializers#pinned pinned} to the anonymous-class one ({@code kinds})
	 * keeps its place after it.
	 */
	private void checkAnonClassOrder(@Nonnull List<DetailAST> fields, @Nonnull Map<String, Set<String>> deps, @Nonnull Map<String, Kind> kinds) {
		final var anonNamesByChunk = new HashMap<Integer, Set<String>>();
		final var chunks = new int[fields.size()];
		final var isAnon = new boolean[fields.size()];
		for (var i = 0; i < fields.size(); ++i) {
			final var field = fields.get(i);
			chunks[i] = chunkOf(field);
			isAnon[i] = hasAnonymousClassInit(field);
			if (isAnon[i])
				anonNamesByChunk.computeIfAbsent(chunks[i], chunk -> new HashSet<>()).add(fieldName(field));
		}

		final var plainAnonReads = new HashMap<Integer, Set<String>>();
		for (var i = 0; i < fields.size(); ++i) {
			if (!isAnon[i])
				continue;
			final var anon = fields.get(i);
			final var chunk = chunks[i];
			final var anonName = fieldName(anon);
			final var anonDeps = transitiveDependencies(anonName, deps);
			for (var j = i + 1; j < fields.size(); ++j) {
				if (chunks[j] != chunk || isAnon[j])
					continue;
				final var plain = fields.get(j);
				final var plainName = fieldName(plain);
				if (anonDeps.contains(plainName) || FieldInitializers.pinned(kinds.get(anonName), kinds.get(plainName)))
					continue;
				final var plainDeps = plainAnonReads.computeIfAbsent(j, index -> {
					final var reads = transitiveDependencies(plainName, deps);
					reads.retainAll(anonNamesByChunk.get(chunk));
					return reads;
				});
				if (plainDeps.isEmpty()) {
					log(anon, MSG_ANON_CLASS, anonName, plainName);
					break;
				}
			}
		}
	}

	/**
	 * Reports constants out of alphabetical order, constants sharing a line, and a constant reading
	 * a later one by bare name among its arguments ({@link FieldInitializers#bareForwardReads}),
	 * which does not compile. The order is reported whatever creating the constants runs or reads:
	 * code anywhere may depend on the declared order, so only a person can decide to change it, and
	 * both order reports are warnings, which the fix pipeline leaves alone.
	 */
	private void checkEnumConstants(@Nonnull List<DetailAST> constants) {
		final var bareForward = FieldInitializers.bareForwardReads(constants);
		for (var constant : constants) {
			final var read = bareForward.get(fieldName(constant));
			if (read != null)
				logWarning(constant, MSG_ENUM_DEPENDENCY, fieldName(constant), read);
		}
		for (var i = 1; i < constants.size(); ++i) {
			final var prevIdent = constants.get(i - 1).findFirstToken(TokenTypes.IDENT);
			final var currIdent = constants.get(i).findFirstToken(TokenTypes.IDENT);
			final var prevName = prevIdent.getText();
			final var currName = currIdent.getText();

			if (currIdent.getLineNo() == prevIdent.getLineNo())
				log(constants.get(i), MSG_ENUM_SAME_LINE, currName);

			if (constantSortsAhead(constants.get(i - 1), constants.get(i)))
				logWarning(constants.get(i), MSG_ENUM_CONSTANT, currName, prevName);
		}
	}

	private void checkFieldGroup(@Nonnull List<DetailAST> fields) {
		if (fields.size() < 2)
			return;

		final var deps = fieldDependencies(fields);
		final var kinds = new HashMap<String, Kind>();
		for (var field : fields)
			kinds.put(fieldName(field), FieldInitializers.kindOf(field));
		checkAnonClassOrder(fields, deps, kinds);
		checkForwardDependencies(fields, deps);

		for (var i = 1; i < fields.size(); ++i) {
			final var curr = fields.get(i);
			// a declaration sits among its neighbours by its first name, while its own names keep
			// order among themselves
			var p = i - 1;
			if (!isLaterDeclarator(curr)) {
				while (p > 0 && isLaterDeclarator(fields.get(p)))
					--p;
			}
			final var prev = fields.get(p);
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

			// a dependency or an anonymous/plain pair overrides type and name order;
			// checkForwardDependencies and checkAnonClassOrder report those orders,
			// since they also span fields that are not neighbours. A pinned pair keeps the order
			// it has, as a dependency would
			final var prevNames = declarationNames(fields, p, isLaterDeclarator(curr));
			final var currNames = declarationNames(fields, i, isLaterDeclarator(curr));
			if (dependsAcross(deps, prevNames, currNames)
					|| pinnedAcross(kinds, prevNames, currNames)
					|| hasAnonymousClassInit(prev) != hasAnonymousClassInit(curr))
				continue;

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

	/**
	 * A forward read by simple name is an illegal forward reference (JLS 8.3.3), but a qualified read
	 * such as {@code this.x} and an anonymous class's member initializers compile and silently read
	 * the default value.
	 */
	private void checkForwardDependencies(@Nonnull List<DetailAST> fields, @Nonnull Map<String, Set<String>> deps) {
		for (var i = 0; i < fields.size(); ++i) {
			final var field = fields.get(i);
			final var name = fieldName(field);
			final var fieldDeps = deps.get(name);
			if (fieldDeps == null)
				continue;
			for (var j = i + 1; j < fields.size(); ++j) {
				final var laterName = fieldName(fields.get(j));
				if (fieldDeps.contains(laterName)) {
					log(field, MSG_DEPENDENCY, name, laterName);
					break;
				}
			}
		}
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.OBJBLOCK};
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		if (isSuppressed(ast))
			return;

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