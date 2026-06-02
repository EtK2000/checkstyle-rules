package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
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

	@CheckReturnValue
	private static boolean isHandledDotParent(int parentType) {
		// DOT chains in these positions are either type references (handled by
		// checkType/the clause cases) or the qualifier of a construct handled by
		// its own case (method call/ref, new, annotation type), or not an
		// expression at all (import/package). Only expression-position DOTs
		// (field access, class literal, annotation-argument values) fall through.
		// Type-argument and wildcard-bound DOTs are type positions owned by the
		// TYPE_ARGUMENTS case, so they are excluded here too.
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
	 * shortened, or {@code null} if none resolves. Shared with the fixer so it
	 * bounds the strip identically. Returns the longest dotted prefix of
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
	 * The DOT node naming the qualified type of a single {@code TYPE_ARGUMENT},
	 * or {@code null} when the argument is a simple name, an unbounded wildcard,
	 * or a wildcard bound to a simple name. Handles a direct dotted type
	 * ({@code java.util.Map}) and a wildcard bound ({@code ? extends java.util.Map}).
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

	private final Set<String> fileTypeNames = new HashSet<>();
	private final Set<String> imports = new HashSet<>();
	private String packageName;

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		fileTypeNames.clear();
		imports.clear();
		packageName = null;

		// a comments-only or empty file has no compilation unit, so checkstyle passes a null root
		if (rootAST == null)
			return;

		packageName = AstUtil.getPackageName(rootAST);
		for (var child = rootAST.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.IMPORT) {
				final var fqn = FullIdent.createFullIdentBelow(child).getText();
				if (!fqn.isEmpty())
					imports.add(fqn);
			}
		}
		collectFileTypeNames(rootAST, fileTypeNames);
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
		if (typeChain == null || !AstUtil.isPureDotChainOrIdent(typeChain))
			return;
		final var prefix = resolvableTypePrefix(FullIdent.createFullIdent(typeChain).getText());
		if (prefix != null)
			log(qualifier, MSG_KEY, prefix);
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

	/**
	 * Whether {@code root} binds to a type visible in this file: a type declared
	 * in the file, a name resolvable via the file's imports / same package /
	 * {@code java.lang} (per {@link ReflectionUtil#resolveClassName}), or an
	 * uncompiled sibling source file of the same simple name.
	 */
	@CheckReturnValue
	private boolean rootNamesTypeInScope(@Nonnull String root) {
		return fileTypeNames.contains(root)
				|| ReflectionUtil.resolveClassName(root, packageName, imports) != null
				|| siblingSourceFileExists(root);
	}

	@CheckReturnValue
	private boolean siblingSourceFileExists(@Nonnull String simpleName) {
		// a same-package sibling names a type, whose source file starts uppercase
		// by convention; skip the filesystem probe for lowercase package roots
		if (simpleName.isEmpty() || !Character.isUpperCase(simpleName.charAt(0)))
			return false;
		final var filePath = getFilePath();
		if (filePath == null)
			return false;
		try {
			final var parent = Path.of(filePath).getParent();
			return parent != null && Files.exists(parent.resolve(simpleName + ".java"));
		}
		catch (InvalidPathException | SecurityException ignored) {
			return false;
		}
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