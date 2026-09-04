package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstResolve;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Which simple names a variable may take over at a point of one file. A variable obscures a type
 * of the same name (JLS 6.4.2), so a check about to make the code read a simple name as a type asks
 * here first. Built once per file, since it reads the file's static imports and caches per-member
 * pattern names.
 */
public final class VariableScope {
	/**
	 * A type a name reads as: one declared in this file ({@code declaration}, a type declaration or an
	 * anonymous class's {@code new}), a class outside it ({@code fqcn}), or neither, when it cannot be
	 * read.
	 */
	private record TypeRef(@Nullable DetailAST declaration, @Nullable String fqcn) {}

	/** What {@link #walkSupertypes} answers when a type declared in this file declares the field. */
	private static final String SAME_FILE_FIND = "#same-file";
	/** Two different types the name may read as, which javac rejects. */
	private static final TypeRef AMBIGUOUS_TYPE = new TypeRef(null, ReflectionUtil.AMBIGUOUS_TYPE_KEY);
	/** A type a class that cannot be read decides. */
	private static final TypeRef UNKNOWN_TYPE = new TypeRef(null, null);

	/**
	 * The type declaration named {@code name} among {@code scope}'s children, only those before
	 * {@code before} when it is one of them.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST declaredType(@Nonnull DetailAST scope, @Nonnull String name, @Nullable DetailAST before) {
		for (var child = scope.getFirstChild(); child != null && child != before; child = child.getNextSibling()) {
			if (isTypeDeclaration(child.getType()) && name.equals(child.findFirstToken(TokenTypes.IDENT).getText()))
				return child;
		}
		return null;
	}

	/** Whether the type body {@code objBlock} declares an enum constant named {@code name}. */
	@CheckReturnValue
	private static boolean declaresEnumConstant(@Nonnull DetailAST objBlock, @Nonnull String name) {
		for (var member = objBlock.getFirstChild(); member != null; member = member.getNextSibling()) {
			if (member.getType() == TokenTypes.ENUM_CONSTANT_DEF && name.equals(member.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return false;
	}

	/**
	 * Whether {@code declaration}, a type declaration or an anonymous class's {@code new}, itself
	 * declares a field or enum constant named {@code name} in its body.
	 */
	@CheckReturnValue
	private static boolean declaresField(@Nonnull DetailAST declaration, @Nonnull String name) {
		final var body = declaration.findFirstToken(TokenTypes.OBJBLOCK);
		if (body == null)
			return false;
		for (var member = body.getFirstChild(); member != null; member = member.getNextSibling()) {
			if (member.getType() == TokenTypes.VARIABLE_DEF && name.equals(member.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return declaresEnumConstant(body, name);
	}

	/** Whether an earlier case group of a switch enclosing {@code site} declares a local named {@code name}. */
	@CheckReturnValue
	private static boolean earlierCaseGroupDeclares(@Nonnull DetailAST site, @Nonnull String name) {
		for (var group = site.getParent(); group != null; group = group.getParent()) {
			if (group.getType() != TokenTypes.CASE_GROUP)
				continue;
			// a switch block is one scope, so such a local reaches every later group; only the last
			// group can be empty, so every earlier one has a statement list
			for (var earlier = group.getPreviousSibling(); earlier != null; earlier = earlier.getPreviousSibling()) {
				if (earlier.getType() != TokenTypes.CASE_GROUP)
					continue;
				for (var statement = earlier.findFirstToken(TokenTypes.SLIST).getFirstChild(); statement != null; statement = statement.getNextSibling()) {
					if (statement.getType() == TokenTypes.VARIABLE_DEF && name.equals(statement.findFirstToken(TokenTypes.IDENT).getText()))
						return true;
				}
			}
		}
		return false;
	}

	/** Whether an enum enclosing {@code site} declares a constant named {@code name}. */
	@CheckReturnValue
	private static boolean enclosingEnumDeclaresConstant(@Nonnull DetailAST site, @Nonnull String name) {
		for (var scope = site.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.OBJBLOCK && scope.getParent().getType() == TokenTypes.ENUM_DEF
					&& declaresEnumConstant(scope, name))
				return true;
		}
		return false;
	}

	/** The owners of the type bodies enclosing {@code node}, innermost first: declarations and anonymous classes' {@code new}. */
	@CheckReturnValue
	@Nonnull
	private static List<DetailAST> enclosingOwners(@Nonnull DetailAST node) {
		final var owners = new ArrayList<DetailAST>();
		for (var scope = node.getParent(); scope != null; scope = scope.getParent()) {
			if (scope.getType() == TokenTypes.OBJBLOCK)
				owners.add(scope.getParent());
		}
		return owners;
	}

	/**
	 * Whether {@code site} lies inside the declaration of a variable named {@code name}. javac reads a
	 * local's own annotations in that local's scope, while {@link AstResolve#variableIsBound} only
	 * binds the name from the name onwards.
	 */
	@CheckReturnValue
	private static boolean insideDeclarationOf(@Nonnull DetailAST site, @Nonnull String name) {
		for (var node = site.getParent(); node != null; node = node.getParent()) {
			if (node.getType() == TokenTypes.VARIABLE_DEF && name.equals(node.findFirstToken(TokenTypes.IDENT).getText()))
				return true;
		}
		return false;
	}

	/** Whether the type declaration {@code declaration} is private. */
	@CheckReturnValue
	private static boolean isPrivate(@Nonnull DetailAST declaration) {
		final var modifiers = declaration.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_PRIVATE) != null;
	}

	/** Whether {@code type} names one type that can be read: one declared in this file, or a single class outside it. */
	@CheckReturnValue
	private static boolean isReadable(@Nullable TypeRef type) {
		return type != null && (type.declaration() != null || type.fqcn() != null && !type.equals(AMBIGUOUS_TYPE));
	}

	/**
	 * Whether the member type declaration {@code declaration} is static, as a static import needs:
	 * declared so, a nested interface, enum, record or annotation, or a member of an interface.
	 */
	@CheckReturnValue
	private static boolean isStaticMember(@Nonnull DetailAST declaration) {
		final var modifiers = declaration.findFirstToken(TokenTypes.MODIFIERS);
		if (declaration.getType() != TokenTypes.CLASS_DEF || modifiers != null && modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) != null)
			return true;
		final var enclosing = declaration.getParent().getParent().getType();
		return enclosing == TokenTypes.INTERFACE_DEF || enclosing == TokenTypes.ANNOTATION_DEF;
	}

	@CheckReturnValue
	private static boolean isTypeDeclaration(int type) {
		return switch (type) {
			case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
			     TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF -> true;
			default -> false;
		};
	}

	/** What a probe answer means: nothing found, a class that cannot be read, or a find. */
	@CheckReturnValue
	@Nonnull
	private static ScopeLookup lookupOf(@Nullable String answer) {
		if (answer == null)
			return ScopeLookup.ABSENT;
		return ReflectionUtil.UNKNOWN_TYPE_KEY.equals(answer) ? ScopeLookup.UNKNOWN : ScopeLookup.FOUND;
	}

	/**
	 * Whether the member type {@code member} a simple name reads as is a class outside this file other
	 * than {@code qualifiedName}: FOUND for another or two of them, ABSENT for that class itself or a
	 * type declared in this file, UNKNOWN when a class it depends on cannot be read.
	 */
	@CheckReturnValue
	@Nonnull
	private static ScopeLookup otherThan(@Nonnull TypeRef member, @Nonnull String qualifiedName) {
		if (member.equals(UNKNOWN_TYPE))
			return ScopeLookup.UNKNOWN;
		return member.declaration() != null || member.fqcn().equals(ReflectionUtil.binaryName(qualifiedName))
				? ScopeLookup.ABSENT
				: ScopeLookup.FOUND;
	}

	/**
	 * The supertype names {@code owner}, a type declaration or an anonymous class's {@code new},
	 * writes. A name the text does not spell as a plain or dotted name is a null entry.
	 */
	@CheckReturnValue
	@Nonnull
	private static List<String> supertypeNames(@Nonnull DetailAST owner) {
		final var names = new ArrayList<String>();
		if (owner.getType() == TokenTypes.LITERAL_NEW) {
			names.add(AstText.findNewClassName(owner));
			return names;
		}
		for (var clause = owner.getFirstChild(); clause != null; clause = clause.getNextSibling()) {
			if (clause.getType() != TokenTypes.EXTENDS_CLAUSE && clause.getType() != TokenTypes.IMPLEMENTS_CLAUSE)
				continue;
			for (var name = clause.getFirstChild(); name != null; name = name.getNextSibling()) {
				if (name.getType() == TokenTypes.IDENT || name.getType() == TokenTypes.DOT)
					names.add(AstText.typeName(name));
			}
		}
		return names;
	}

	private final List<String> onDemandOwners = new ArrayList<>();
	private final Map<DetailAST, List<TypeRef>> supertypes = new HashMap<>();
	private final Map<DetailAST, Map<String, Optional<TypeRef>>> memberTypes = new HashMap<>();
	private final Map<DetailAST, Set<String>> memberPatterns = new HashMap<>();
	private final Map<String, Optional<String>> probes = new HashMap<>();
	private final Map<String, List<String>> staticImports = new HashMap<>();
	private final Set<String> singleTypeImports = new HashSet<>();
	@Nullable
	private final DetailAST root;
	@Nonnull
	private final Function<String, String> resolver;
	@Nullable
	private final String packageName;

	/**
	 * The scope of the file {@code root} heads, in {@code packageName}, with {@code resolver} giving
	 * the class a type name written in the file resolves to (null when it does not).
	 */
	public VariableScope(@Nullable DetailAST root, @Nullable String packageName, @Nonnull Function<String, String> resolver) {
		this.packageName = packageName;
		this.resolver = resolver;
		this.root = root;
		for (var child = root == null ? null : root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IMPORT) {
				final var imported = FullIdent.createFullIdentBelow(child).getText();
				if (!imported.endsWith(".*"))
					singleTypeImports.add(imported.substring(imported.lastIndexOf('.') + 1));
				continue;
			}
			if (child.getType() != TokenTypes.STATIC_IMPORT)
				continue;
			final var imported = child.findFirstToken(TokenTypes.DOT);
			if (imported.getLastChild().getType() == TokenTypes.STAR)
				onDemandOwners.add(FullIdent.createFullIdent(imported.getFirstChild()).getText());
			else
				staticImports.computeIfAbsent(imported.getLastChild().getText(), name -> new ArrayList<>()).add(FullIdent.createFullIdent(imported).getText());
		}
	}

	/**
	 * A field named {@code name} that {@code fqcn} shows this file's package, by inheritance or, with
	 * {@code staticImport}, through a static on-demand import: {@code fqcn} itself when it does, null
	 * when it does not, {@link ReflectionUtil#UNKNOWN_TYPE_KEY} when its fields cannot be read.
	 */
	@CheckReturnValue
	@Nullable
	private String fieldProbe(@Nonnull String fqcn, @Nonnull String name, boolean staticImport) {
		final Supplier<String> read = () -> {
			final var seen = ReflectionUtil.seesField(fqcn, name, packageName, staticImport);
			if (seen == null)
				return ReflectionUtil.UNKNOWN_TYPE_KEY;
			return seen ? fqcn : null;
		};
		return probe(fqcn + (staticImport ? "#static#" : "#field#") + name, read);
	}

	/**
	 * The member type named {@code name} that {@code declaration} (a type declaration or an anonymous
	 * class's {@code new}) declares or inherits (JLS 8.5) along every path through its supertypes,
	 * in this file or outside it; with {@code staticOnly} only a static one, which a static import
	 * brings in. A declaration of the name hides its class's supertypes' ones, and one a supertype
	 * declares privately is not passed down. A worklist, since a generated file can chain thousands
	 * of supertypes.
	 */
	@CheckReturnValue
	@Nullable
	private TypeRef findMemberType(@Nonnull DetailAST declaration, @Nonnull String name, boolean staticOnly) {
		final var found = new LinkedHashSet<TypeRef>();
		final var pending = new ArrayDeque<DetailAST>();
		final var seen = new HashSet<DetailAST>();
		var unknown = false;
		pending.add(declaration);
		while (!pending.isEmpty()) {
			final var current = pending.poll();
			if (!seen.add(current))
				continue;
			final var body = current.findFirstToken(TokenTypes.OBJBLOCK);
			final var declared = body == null ? null : declaredType(body, name, null);
			if (declared != null) {
				if ((current == declaration || !isPrivate(declared)) && (!staticOnly || isStaticMember(declared)))
					found.add(new TypeRef(declared, null));
				continue;
			}
			for (var supertype : supertypesOf(current)) {
				if (supertype.declaration() != null)
					pending.add(supertype.declaration());
				else {
					final var member = supertype.fqcn() == null ? ReflectionUtil.UNKNOWN_TYPE_KEY : memberTypeProbe(supertype.fqcn(), name, staticOnly);
					if (ReflectionUtil.UNKNOWN_TYPE_KEY.equals(member))
						unknown = true;
					else if (member != null)
						found.add(new TypeRef(null, member));
				}
			}
		}
		if (unknown)
			return UNKNOWN_TYPE;
		if (found.size() > 1 || found.contains(AMBIGUOUS_TYPE))
			return AMBIGUOUS_TYPE;
		return found.isEmpty() ? null : found.getFirst();
	}

	/**
	 * Whether {@code qualifiedName}'s simple name, written at {@code site}, would read as a member type
	 * other than that type itself: the one the nearest enclosing body that has a member type of the
	 * name declares or inherits ({@link #memberType}). A type declared in this file counts as absent,
	 * being left to the caller. UNKNOWN when that body's answer depends on a class that cannot be read.
	 */
	@CheckReturnValue
	@Nonnull
	public ScopeLookup lookupInheritedMemberType(@Nonnull DetailAST site, @Nonnull String qualifiedName) {
		final var simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
		for (var frame = site.getParent(); frame != null; frame = frame.getParent()) {
			if (frame.getType() != TokenTypes.OBJBLOCK)
				continue;
			final var member = memberType(frame.getParent(), simpleName, false);
			if (member != null)
				return otherThan(member, qualifiedName);
		}
		return ScopeLookup.ABSENT;
	}

	/**
	 * Whether a static import of the file brings in a member type of {@code qualifiedName}'s simple
	 * name other than that type itself: a static on-demand import with {@code onDemand}, a single
	 * static import of the name without. A class declared in this file is read through
	 * {@link #memberType}, a type declared in this file being left to the caller. UNKNOWN when none
	 * does but a class the answer depends on cannot be read.
	 */
	@CheckReturnValue
	@Nonnull
	public ScopeLookup lookupStaticImportedMemberType(@Nonnull String qualifiedName, boolean onDemand) {
		final var simpleName = qualifiedName.substring(qualifiedName.lastIndexOf('.') + 1);
		final var owners = new ArrayList<String>();
		if (onDemand)
			owners.addAll(onDemandOwners);
		else {
			for (var imported : staticImports.getOrDefault(simpleName, List.of()))
				owners.add(imported.substring(0, imported.lastIndexOf('.')));
		}
		var unknown = false;
		for (var owner : owners) {
			final var member = staticMemberTypeOf(owner, simpleName);
			final var lookup = member == null ? ScopeLookup.ABSENT : otherThan(member, qualifiedName);
			if (lookup == ScopeLookup.FOUND)
				return lookup;
			unknown |= lookup == ScopeLookup.UNKNOWN;
		}
		return unknown ? ScopeLookup.UNKNOWN : ScopeLookup.ABSENT;
	}

	/**
	 * Whether a variable named {@code name} may be in scope at {@code site}, so a simple name written
	 * there reads as that variable rather than a type. FOUND for a declarator
	 * {@link AstResolve#variableIsDeclaredInScope} finds, a local of an earlier case group, an enum
	 * constant, the declaration {@code site} sits in, any pattern of the name in an enclosing member
	 * ({@link ProvenTypes#mayBindPattern}, pattern scoping not being modelled past an if-then branch
	 * and an {@code &&}), a name a single static import brings in (whatever member it is: the file
	 * cannot tell a field from a method or a member type), a static field or enum constant a static
	 * on-demand import brings in, or a field a supertype of an enclosing body passes down, its name
	 * read nearest scope first ({@link #resolveSimpleTypeName}). Classes outside the file are read by
	 * reflection, so UNKNOWN when none of those binds but a class they depend on cannot be read: a
	 * supertype whose name does not resolve, one that does not load or whose fields cannot be read,
	 * or such a static on-demand import owner.
	 */
	@CheckReturnValue
	@Nonnull
	public ScopeLookup lookupVariable(@Nonnull DetailAST site, @Nonnull String name) {
		if (staticImports.containsKey(name)
				|| AstResolve.variableIsDeclaredInScope(site, name)
				|| earlierCaseGroupDeclares(site, name)
				|| enclosingEnumDeclaresConstant(site, name)
				|| insideDeclarationOf(site, name)
				|| ProvenTypes.mayBindPattern(site, name, member -> memberPatterns.computeIfAbsent(member, ProvenTypes::patternNames)))
			return ScopeLookup.FOUND;
		final var onDemand = onDemandLookup(name);
		if (onDemand == ScopeLookup.FOUND)
			return onDemand;
		final var inherited = lookupOf(walkSupertypes(enclosingOwners(site), owner -> declaresField(owner, name), fqcn -> fieldProbe(fqcn, name, false)));
		return inherited == ScopeLookup.ABSENT ? onDemand : inherited;
	}

	/**
	 * The member type named {@code name} that {@code declaration} declares or inherits
	 * ({@link #findMemberType}), worked out once per file. Null when there is none,
	 * {@link #UNKNOWN_TYPE} when a class the answer depends on cannot be read or the lookup comes back
	 * to itself, which only a cyclic hierarchy does, and {@link #AMBIGUOUS_TYPE} for two different ones.
	 */
	@CheckReturnValue
	@Nullable
	private TypeRef memberType(@Nonnull DetailAST declaration, @Nonnull String name, boolean staticOnly) {
		final var cache = memberTypes.computeIfAbsent(declaration, ignored -> new HashMap<>());
		final var key = staticOnly ? name + "#static" : name;
		final var cached = cache.get(key);
		if (cached != null)
			return cached.orElse(null);
		cache.put(key, Optional.of(UNKNOWN_TYPE));
		final var found = findMemberType(declaration, name, staticOnly);
		cache.put(key, Optional.ofNullable(found));
		return found;
	}

	/**
	 * The binary name of the member type named {@code simpleName} that a subclass of {@code fqcn} in
	 * this file's package inherits, or with {@code staticOnly} that a static import of it there brings
	 * in ({@link ReflectionUtil#inheritedMemberClass}), memoized for the file.
	 */
	@CheckReturnValue
	@Nullable
	private String memberTypeProbe(@Nonnull String fqcn, @Nonnull String simpleName, boolean staticOnly) {
		return probe(fqcn + (staticOnly ? "#static-member#" : "#member#") + simpleName, () -> ReflectionUtil.inheritedMemberClass(fqcn, simpleName, staticOnly, packageName));
	}

	/**
	 * Whether a static on-demand import of the file brings in a field named {@code name}: a static
	 * field the imported class lets this file's package see, or, for a class declared in this file,
	 * any field or enum constant it declares of the name or that its supertypes pass down.
	 */
	@CheckReturnValue
	@Nonnull
	private ScopeLookup onDemandLookup(@Nonnull String name) {
		var unknown = false;
		for (var owner : onDemandOwners) {
			final var sameFile = sameFileOwner(owner);
			final var lookup = sameFile == null
					? lookupOf(fieldProbe(owner, name, true))
					: lookupOf(walkSupertypes(List.of(sameFile), declaration -> declaresField(declaration, name), fqcn -> fieldProbe(fqcn, name, true)));
			if (lookup == ScopeLookup.FOUND)
				return lookup;
			unknown |= lookup == ScopeLookup.UNKNOWN;
		}
		return unknown ? ScopeLookup.UNKNOWN : ScopeLookup.ABSENT;
	}

	/** What {@code compute} answers for {@code key}, worked out once per file. */
	@CheckReturnValue
	@Nullable
	private String probe(@Nonnull String key, @Nonnull Supplier<String> compute) {
		return probes.computeIfAbsent(key, ignored -> Optional.ofNullable(compute.get())).orElse(null);
	}

	/**
	 * The type the simple type name {@code simpleName} names where {@code node} is written, nearest
	 * scope first (JLS 6.4.1, 6.5.5.1): a local class declared earlier in an enclosing block, then each
	 * enclosing body from the innermost out with the member types it declares or inherits
	 * ({@link #memberType}), then the compilation unit (a single-type import, a top-level type of the
	 * file, a static member type a single static import of the name brings in), then the package, and
	 * last the on-demand imports, {@code java.lang} and the static member types a static on-demand
	 * import brings in, together. Null when none does, {@link #UNKNOWN_TYPE} when the scope that
	 * decides depends on a class that cannot be read, and {@link #AMBIGUOUS_TYPE} for two answers.
	 */
	@CheckReturnValue
	@Nullable
	private TypeRef resolveSimpleTypeName(@Nonnull DetailAST node, @Nonnull String simpleName) {
		var child = node;
		for (var frame = node.getParent(); frame != null; frame = frame.getParent()) {
			if (frame.getType() == TokenTypes.SLIST) {
				final var local = declaredType(frame, simpleName, child);
				if (local != null)
					return new TypeRef(local, null);
			}
			else if (frame.getType() == TokenTypes.OBJBLOCK) {
				final var member = memberType(frame.getParent(), simpleName, false);
				if (member != null)
					return member;
			}
			child = frame;
		}
		if (singleTypeImports.contains(simpleName))
			return new TypeRef(null, resolver.apply(simpleName));
		final var topLevel = root == null ? null : declaredType(root, simpleName, null);
		if (topLevel != null)
			return new TypeRef(topLevel, null);
		final var staticallyImported = staticImportedType(simpleName);
		if (staticallyImported != null)
			return staticallyImported;
		final var resolved = resolver.apply(simpleName);
		if (resolved != null && resolved.equals(packageName == null ? simpleName : packageName + '.' + simpleName))
			return new TypeRef(null, resolved);
		final var answers = new LinkedHashSet<TypeRef>();
		if (resolved != null)
			answers.add(new TypeRef(null, resolved));
		for (var owner : onDemandOwners) {
			final var member = staticMemberTypeOf(owner, simpleName);
			if (UNKNOWN_TYPE.equals(member))
				return UNKNOWN_TYPE;
			if (member != null)
				answers.add(member);
		}
		if (answers.size() > 1)
			return AMBIGUOUS_TYPE;
		return answers.isEmpty() ? null : answers.getFirst();
	}

	/**
	 * The type the type name {@code written} names in {@code owner}'s header, read in the scope
	 * enclosing {@code owner} ({@link #resolveSimpleTypeName}); {@link #UNKNOWN_TYPE} when it does not
	 * resolve. A dotted name is read through its head: a type of this file by its member types, a class
	 * outside it by appending the rest, and the whole name as fully qualified when the head does not
	 * resolve.
	 */
	@CheckReturnValue
	@Nonnull
	private TypeRef resolveTypeName(@Nonnull DetailAST owner, @Nonnull String written) {
		final var segments = written.split("\\.");
		var current = resolveSimpleTypeName(owner, segments[0]);
		if (segments.length > 1 && !isReadable(current))
			return new TypeRef(null, written);
		for (var i = 1; i < segments.length && isReadable(current); ++i) {
			if (current.declaration() == null)
				return new TypeRef(null, current.fqcn() + '.' + String.join(".", List.of(segments).subList(i, segments.length)));
			current = memberType(current.declaration(), segments[i], false);
		}
		return isReadable(current) ? current : UNKNOWN_TYPE;
	}

	/** The declaration of {@code owner}, a class name a static import writes, when this file declares it. */
	@CheckReturnValue
	@Nullable
	private DetailAST sameFileOwner(@Nonnull String owner) {
		return root == null || packageName == null || !owner.startsWith(packageName + '.')
				? null
				: AstResolve.sameFileClassDef(root, owner.substring(packageName.length() + 1));
	}

	/**
	 * The static member type named {@code simpleName} a single static import of the name brings in
	 * ({@link #staticMemberTypeOf}); one of a field or method is passed over. Null when none does,
	 * {@link #UNKNOWN_TYPE} when none does but an imported class cannot be read.
	 */
	@CheckReturnValue
	@Nullable
	private TypeRef staticImportedType(@Nonnull String simpleName) {
		var unknown = false;
		for (var imported : staticImports.getOrDefault(simpleName, List.of())) {
			final var member = staticMemberTypeOf(imported.substring(0, imported.lastIndexOf('.')), simpleName);
			if (UNKNOWN_TYPE.equals(member))
				unknown = true;
			else if (member != null)
				return member;
		}
		return unknown ? UNKNOWN_TYPE : null;
	}

	/**
	 * The static member type named {@code simpleName} a static import of {@code owner} brings in: read
	 * by {@link #memberType} when this file declares {@code owner}, by reflection otherwise.
	 */
	@CheckReturnValue
	@Nullable
	private TypeRef staticMemberTypeOf(@Nonnull String owner, @Nonnull String simpleName) {
		final var sameFile = sameFileOwner(owner);
		if (sameFile != null)
			return memberType(sameFile, simpleName, true);
		final var member = memberTypeProbe(owner, simpleName, true);
		if (member == null)
			return null;
		return ReflectionUtil.UNKNOWN_TYPE_KEY.equals(member) ? UNKNOWN_TYPE : new TypeRef(null, member);
	}

	/**
	 * The supertypes {@code owner}'s header names, resolved once per file. An inheritance cycle, which
	 * only invalid code has, meets the owner again while it is being read and sees no supertypes.
	 */
	@CheckReturnValue
	@Nonnull
	private List<TypeRef> supertypesOf(@Nonnull DetailAST owner) {
		final var cached = supertypes.get(owner);
		if (cached != null)
			return cached;
		supertypes.put(owner, List.of());
		final var resolved = new ArrayList<TypeRef>();
		for (var written : supertypeNames(owner))
			resolved.add(written == null ? UNKNOWN_TYPE : resolveTypeName(owner, written));
		supertypes.put(owner, resolved);
		return resolved;
	}

	/**
	 * The first find on a supertype of the bodies {@code start} owns: a same-file one {@code declares}
	 * answers for and that is followed to its own supertypes, or one declared outside the file
	 * {@code probe} answers for; a worklist rather than recursion, since a generated file can chain
	 * thousands of them. {@code probe} answers null for nothing found and
	 * {@link ReflectionUtil#UNKNOWN_TYPE_KEY} for a class it cannot read; the walk answers null when
	 * nothing is found anywhere, and that key when a supertype it met does not resolve or cannot be
	 * read.
	 */
	@CheckReturnValue
	@Nullable
	private String walkSupertypes(@Nonnull List<DetailAST> start, @Nonnull Predicate<DetailAST> declares, @Nonnull Function<String, String> probe) {
		final var pending = new ArrayDeque<>(start);
		final var seen = new HashSet<DetailAST>();
		var unknown = false;
		while (!pending.isEmpty()) {
			final var owner = pending.poll();
			if (!seen.add(owner))
				continue;
			if (declares.test(owner))
				return SAME_FILE_FIND;
			for (var supertype : supertypesOf(owner)) {
				if (supertype.declaration() != null) {
					pending.add(supertype.declaration());
					continue;
				}
				final var answer = supertype.fqcn() == null ? ReflectionUtil.UNKNOWN_TYPE_KEY : probe.apply(supertype.fqcn());
				if (lookupOf(answer) == ScopeLookup.FOUND)
					return answer;
				unknown |= answer != null;
			}
		}
		return unknown ? ReflectionUtil.UNKNOWN_TYPE_KEY : null;
	}
}