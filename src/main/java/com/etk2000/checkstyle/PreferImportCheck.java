package com.etk2000.checkstyle;

import com.etk2000.checkstyle.ast.AstQuery;
import com.etk2000.checkstyle.ast.AstText;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Checkstyle check that flags fully qualified type names, preferring
 * an import statement instead. Covers annotations, type references
 * (fields, parameters, return types, locals, generics, casts),
 * extends/implements clauses, throws clauses, and {@code new} expressions
 * (all syntactic), plus expression-position qualifiers: static method-call
 * receivers ({@code java.util.List.of(...)}), method references
 * ({@code java.util.Collections::sort}), static field/constant access
 * ({@code java.lang.Integer.MAX_VALUE}), class literals
 * ({@code java.util.List.class}), and FQNs in annotation argument values
 * ({@code @Cap(java.lang.Integer.MAX_VALUE)}). Expression positions are
 * confirmed by reflection (the qualifier must resolve to a loadable class), so a
 * bare {@code SimpleClass.method()} is left to {@link PreferStaticImportCheck}.
 * Such a qualifier is not flagged where a variable named like its simple name
 * may be in scope ({@link VariableScope#lookupVariable}), since the variable
 * would take over the shortened name, and is a warning, which the fixer leaves
 * alone, when a class that answer depends on cannot be read; a
 * class literal and a constructor reference name a type, so they still are.
 *
 * <p>Generic type arguments are flagged uniformly in every enclosing context
 * (declared types, {@code new} expressions, method-call/reference type
 * witnesses, casts), including wildcard bounds ({@code ? extends java.util.Map}).
 * As type positions they are flagged unconditionally, without the reflection
 * gate the expression-position qualifiers use.
 *
 * <p>A type-position qualifier whose leading segment is not a package but a type
 * already in scope ({@code Map.Entry} when {@code java.util.Map} is imported, a
 * {@code java.lang} type, a same-package type, or a non-enclosing type declared
 * in this file) is not fully qualified, so it is flagged with the distinct
 * {@code prefer.import.nested} message advising the nested type be imported
 * directly. A dotted name whose leading {@code pkg.Class} prefix loads from the
 * classpath is a genuine FQN and keeps the {@code prefer.import} message even
 * when its leading segment collides with an in-file type name.
 */
public class PreferImportCheck extends AbstractAstCheck {
	private static final int MAX_RESOLVABLE_DOTS = 16;
	private static final String MSG_KEY = "prefer.import";
	private static final String MSG_KEY_NESTED = "prefer.import.nested";
	private static final String MSG_KEY_WARNING = "prefer.import.warning";

	private static void buildQualifiedName(@Nonnull DetailAST ast, @Nonnull StringBuilder sb) {
		if (ast.getType() == TokenTypes.DOT) {
			for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
				if (child.getType() == TokenTypes.TYPE_ARGUMENTS)
					continue;
				if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '.')
					sb.append('.');
				buildQualifiedName(child, sb);
			}
		}
		else if (ast.getType() != TokenTypes.TYPE_ARGUMENTS)
			sb.append(ast.getText());
	}

	private static void collectFileTypeNames(@Nonnull DetailAST root, @Nonnull Set<String> out) {
		final var stack = new ArrayDeque<DetailAST>();
		stack.push(root);
		while (!stack.isEmpty()) {
			final var node = stack.pop();
			for (var child = node.getFirstChild(); child != null; child = child.getNextSibling()) {
				final var isTypeDef = switch (child.getType()) {
					case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
					     TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF -> true;
					default -> false;
				};
				if (isTypeDef) {
					final var ident = child.findFirstToken(TokenTypes.IDENT);
					if (ident != null)
						out.add(ident.getText());
				}
				stack.push(child);
			}
		}
	}

	/** The names the single-type and on-demand imports of the file {@code root} heads import. */
	@CheckReturnValue
	@Nonnull
	private static Set<String> importsOf(@Nonnull DetailAST root) {
		final var imports = new HashSet<String>();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IMPORT) {
				final var fqn = FullIdent.createFullIdentBelow(child).getText();
				if (!fqn.isEmpty())
					imports.add(fqn);
			}
		}
		return imports;
	}

	/**
	 * Whether shortening {@code qualifiedName}, written at the 0-based {@code line} and code-point
	 * {@code column} of the file {@code root} heads, to its simple name would make it read as another
	 * member type outside the file, the one the nearest enclosing body with a member type of that name
	 * declares or inherits ({@link VariableScope#lookupInheritedMemberType}). Null when no node sits at
	 * that position.
	 * Shared with the fixer, which refuses such a site.
	 */
	@CheckReturnValue
	@Nullable
	public static ScopeLookup inheritedMemberTypeAt(@Nonnull DetailAST root, int line, int column, @Nonnull String qualifiedName) {
		final var site = AstQuery.findNodeAt(root, line, column, node -> true);
		if (site == null)
			return null;
		final var packageName = AstText.getPackageName(root);
		final var imports = importsOf(root);
		return new VariableScope(root, packageName, name -> ReflectionUtil.resolveClassName(name, packageName, imports))
				.lookupInheritedMemberType(site, qualifiedName);
	}

	@CheckReturnValue
	private static boolean isConstructorReference(@Nonnull DetailAST qualifier) {
		final var parent = qualifier.getParent();
		return parent.getType() == TokenTypes.METHOD_REF && parent.getLastChild().getType() == TokenTypes.LITERAL_NEW;
	}

	@CheckReturnValue
	private static boolean isHandledDotParent(int parentType) {
		return switch (parentType) {
			case TokenTypes.ANNOTATION, TokenTypes.DOT, TokenTypes.EXTENDS_CLAUSE,
			     TokenTypes.IMPLEMENTS_CLAUSE, TokenTypes.IMPORT, TokenTypes.LITERAL_NEW,
			     TokenTypes.LITERAL_THROWS, TokenTypes.METHOD_CALL, TokenTypes.METHOD_REF,
			     TokenTypes.PACKAGE_DEF, TokenTypes.STATIC_IMPORT, TokenTypes.TYPE,
			     TokenTypes.TYPE_ARGUMENT, TokenTypes.TYPE_LOWER_BOUNDS,
			     TokenTypes.TYPE_UPPER_BOUNDS -> true;
			default -> false;
		};
	}

	/**
	 * Whether {@code qualifiedName}'s leading segment names a type declaration
	 * lexically enclosing {@code node}. Such a self-qualification
	 * ({@code Enclosing.Nested} inside {@code Enclosing}) is never an import
	 * candidate: the simple name suffices in the body and the qualifier is
	 * compiler-required in the enclosing type's own header, so it is not flagged.
	 *
	 * <p>A genuine external FQN that merely shares its leading segment with an
	 * enclosing type name (e.g. a class named {@code com} referencing
	 * {@code com.google.X}) still resolves to a real class via reflection, so it
	 * is not treated as a self-qualification and is flagged normally.
	 */
	@CheckReturnValue
	private static boolean namesEnclosingType(@Nonnull DetailAST node, @Nonnull String qualifiedName) {
		final var firstDot = qualifiedName.indexOf('.');
		if (firstDot < 0)
			return false;
		final var head = qualifiedName.substring(0, firstDot);
		for (var ancestor = node.getParent(); ancestor != null; ancestor = ancestor.getParent()) {
			final var isTypeDef = switch (ancestor.getType()) {
				case TokenTypes.ANNOTATION_DEF, TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF,
				     TokenTypes.INTERFACE_DEF, TokenTypes.RECORD_DEF -> true;
				default -> false;
			};
			if (isTypeDef) {
				final var ident = ancestor.findFirstToken(TokenTypes.IDENT);
				if (ident != null && ident.getText().equals(head)) {
					// a very deep dotted name at a head-match is a self-qualified
					// reference, never a real importable FQN; skip the O(segments)
					// reflection scan
					if (qualifiedName.chars().filter(c -> c == '.').count() > MAX_RESOLVABLE_DOTS)
						return true;
					return resolvableTypePrefix(qualifiedName) == null;
				}
			}
		}
		return false;
	}

	@CheckReturnValue
	@Nonnull
	private static String qualifiedName(@Nonnull DetailAST dot) {
		final var sb = new StringBuilder();
		buildQualifiedName(dot, sb);
		return sb.toString();
	}

	/**
	 * The fully-qualified type prefix of a dotted expression that should be
	 * shortened, or {@code null} if none resolves. Returns the longest dotted prefix of
	 * {@code dottedName} (including the whole string) that loads as a class via
	 * {@link ReflectionUtil#isResolvableClass}; a single-segment name never
	 * matches, which keeps bare {@code SimpleClass.member} out of scope.
	 */
	@CheckReturnValue
	@Nullable
	public static String resolvableTypePrefix(@Nonnull String dottedName) {
		for (var name = dottedName; name.indexOf('.') >= 0; name = name.substring(0, name.lastIndexOf('.'))) {
			if (ReflectionUtil.isResolvableClass(name))
				return name;
		}
		return null;
	}

	/**
	 * Whether a static import of the file {@code root} heads brings in a member type of
	 * {@code qualifiedName}'s simple name other than that type: a static on-demand import with
	 * {@code onDemand}, a single static import of the name without
	 * ({@link VariableScope#lookupStaticImportedMemberType}). Shared with the fixer, which refuses a
	 * shortening the import would take over.
	 */
	@CheckReturnValue
	@Nonnull
	public static ScopeLookup staticImportedMemberType(@Nonnull DetailAST root, @Nonnull String qualifiedName, boolean onDemand) {
		final var packageName = AstText.getPackageName(root);
		final var imports = importsOf(root);
		return new VariableScope(root, packageName, name -> ReflectionUtil.resolveClassName(name, packageName, imports))
				.lookupStaticImportedMemberType(qualifiedName, onDemand);
	}

	/**
	 * The DOT node naming the qualified type of a single {@code TYPE_ARGUMENT},
	 * or {@code null} when the argument is a simple name, an unbounded wildcard,
	 * or a wildcard bound to a simple name.
	 */
	@CheckReturnValue
	@Nullable
	private static DetailAST typeArgumentQualifier(@Nonnull DetailAST typeArgument) {
		for (var child = typeArgument.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.DOT)
				return child;
			if (child.getType() == TokenTypes.TYPE_LOWER_BOUNDS || child.getType() == TokenTypes.TYPE_UPPER_BOUNDS) {
				for (var bound = child.getFirstChild(); bound != null; bound = bound.getNextSibling()) {
					if (bound.getType() == TokenTypes.DOT)
						return bound;
				}
			}
		}
		return null;
	}

	private final Set<String> fileTypeNames = new HashSet<>(), imports = new HashSet<>();
	private String packageName;
	private VariableScope variableScope;

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		fileTypeNames.clear();
		imports.clear();
		packageName = null;

		// a comments-only or empty file has no compilation unit, so checkstyle passes a null root
		if (rootAST == null)
			return;

		packageName = AstText.getPackageName(rootAST);
		imports.addAll(importsOf(rootAST));
		collectFileTypeNames(rootAST, fileTypeNames);
		variableScope = new VariableScope(rootAST, packageName, name -> ReflectionUtil.resolveClassName(name, packageName, imports));
	}

	private void checkType(@Nonnull DetailAST type) {
		final var firstChild = type.getFirstChild();
		if (firstChild != null && firstChild.getType() == TokenTypes.DOT)
			logQualifiedType(type, qualifiedName(firstChild));
	}

	private void flagExpressionQualifier(@Nullable DetailAST qualifier) {
		if (qualifier == null)
			return;
		// a class literal is DOT(typeChain, LITERAL_CLASS); the type qualifier is
		// the chain before `.class`
		var typeChain = qualifier;
		if (typeChain.getType() == TokenTypes.DOT) {
			final var last = typeChain.getLastChild();
			if (last != null && last.getType() == TokenTypes.LITERAL_CLASS)
				typeChain = typeChain.getFirstChild();
		}
		// only a pure dotted-identifier receiver can be a type qualifier; an
		// impure chain (array index, cast, nested call) is never an FQN, and
		// feeding it to FullIdent could yield a misleading partial name
		if (typeChain == null || !AstQuery.isPureDotChainOrIdent(typeChain))
			return;
		final var prefix = resolvableTypePrefix(FullIdent.createFullIdent(typeChain).getText());
		if (prefix == null)
			return;
		// a class literal and a constructor reference name a type outright, but any other qualifier
		// is an ambiguous name, which a variable of the shortened name would take over (JLS 6.5.2)
		final var namesType = typeChain != qualifier || isConstructorReference(qualifier);
		final var lookup = namesType
				? ScopeLookup.ABSENT
				: variableScope.lookupVariable(qualifier, prefix.substring(prefix.lastIndexOf('.') + 1));
		if (lookup == ScopeLookup.ABSENT)
			log(qualifier, MSG_KEY, prefix);
		else if (lookup == ScopeLookup.UNKNOWN)
			logWarning(qualifier, MSG_KEY_WARNING, prefix);
	}

	private void flagTypeArgumentQualifiers(@Nonnull DetailAST typeArguments) {
		for (var arg = typeArguments.getFirstChild(); arg != null; arg = arg.getNextSibling()) {
			if (arg.getType() != TokenTypes.TYPE_ARGUMENT)
				continue;
			final var dot = typeArgumentQualifier(arg);
			if (dot != null)
				logQualifiedType(dot, qualifiedName(dot));
		}
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.ANNOTATION,
				TokenTypes.DOT,
				TokenTypes.EXTENDS_CLAUSE,
				TokenTypes.IMPLEMENTS_CLAUSE,
				TokenTypes.LITERAL_NEW,
				TokenTypes.LITERAL_THROWS,
				TokenTypes.METHOD_CALL,
				TokenTypes.METHOD_REF,
				TokenTypes.TYPE,
				TokenTypes.TYPE_ARGUMENTS
		};
	}

	/**
	 * Whether {@code qualifiedName}'s leading segment names a type already in
	 * scope rather than a package, making the whole a nested-type reference
	 * ({@code Map.Entry} with {@code Map} imported) rather than a fully-qualified
	 * name. A dotted name whose leading {@code pkg.Class} prefix loads from the
	 * classpath is a genuine FQN and returns {@code false} even when its leading
	 * segment collides with an in-scope type name (reflection wins). A name deeper
	 * than {@link #MAX_RESOLVABLE_DOTS} is treated as an FQN without a reflection
	 * scan, mirroring {@link #namesEnclosingType}'s ceiling.
	 */
	@CheckReturnValue
	private boolean isNestedThroughType(@Nonnull String qualifiedName) {
		final var firstDot = qualifiedName.indexOf('.');
		if (firstDot < 0)
			return false;
		if (qualifiedName.chars().filter(c -> c == '.').count() > MAX_RESOLVABLE_DOTS)
			return false;
		if (!rootNamesTypeInScope(qualifiedName.substring(0, firstDot)))
			return false;
		return resolvableTypePrefix(qualifiedName) == null;
	}

	private void logQualifiedType(@Nonnull DetailAST anchor, @Nonnull String qualifiedName) {
		if (namesEnclosingType(anchor, qualifiedName))
			return;
		if (isNestedThroughType(qualifiedName)) {
			final var lastDot = qualifiedName.lastIndexOf('.');
			logWarning(anchor, MSG_KEY_NESTED, qualifiedName.substring(0, lastDot), qualifiedName.substring(lastDot + 1));
		}
		else
			log(anchor, MSG_KEY, qualifiedName);
	}

	@CheckReturnValue
	private boolean rootNamesTypeInScope(@Nonnull String root) {
		return fileTypeNames.contains(root)
				|| ReflectionUtil.resolveClassName(root, packageName, imports) != null
				|| siblingSourceFileExists(root);
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.ANNOTATION -> {
				for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getType() == TokenTypes.DOT) {
						logQualifiedType(ast, qualifiedName(child));
						break;
					}
				}
			}
			case TokenTypes.DOT -> {
				final var parent = ast.getParent();
				if (parent != null && !isHandledDotParent(parent.getType()))
					flagExpressionQualifier(ast);
			}
			case TokenTypes.EXTENDS_CLAUSE, TokenTypes.IMPLEMENTS_CLAUSE,
			     TokenTypes.LITERAL_THROWS -> {
				for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
					if (child.getType() == TokenTypes.DOT)
						logQualifiedType(child, qualifiedName(child));
				}
			}
			case TokenTypes.LITERAL_NEW -> {
				final var firstChild = ast.getFirstChild();
				if (firstChild != null && firstChild.getType() == TokenTypes.DOT)
					logQualifiedType(ast, qualifiedName(firstChild));
			}
			case TokenTypes.METHOD_CALL -> {
				// static call: METHOD_CALL > DOT(qualifier, methodName); the qualifier
				// is the DOT's first child (everything left of the method name)
				final var dot = ast.getFirstChild();
				if (dot != null && dot.getType() == TokenTypes.DOT)
					flagExpressionQualifier(dot.getFirstChild());
			}
			case TokenTypes.METHOD_REF -> flagExpressionQualifier(ast.getFirstChild());
			case TokenTypes.TYPE -> checkType(ast);
			case TokenTypes.TYPE_ARGUMENTS -> flagTypeArgumentQualifiers(ast);
		}
	}
}