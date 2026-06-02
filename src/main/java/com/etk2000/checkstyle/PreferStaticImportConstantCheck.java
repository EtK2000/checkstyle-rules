package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Flags {@code static final} fields whose initializer aliases another class's
 * static constant, e.g.
 * {@code private static final int MAX_LINE_LENGTH = LineLength.MAX_LINE_LENGTH;}.
 * The recommended replacement is to delete the field and add
 * {@code import static <FQCN>.<CONST>;}.
 *
 * <p>The check fires when ALL the following hold:</p>
 * <ul>
 *   <li>The declaration is a field (parent is {@code OBJBLOCK}).</li>
 *   <li>Modifiers include {@code static} and {@code final}. Visibility doesn't
 *       matter for the check.</li>
 *   <li>The initializer, after stripping enclosing parens, is a pure
 *       {@code DOT}-chain ending in an {@code IDENT}: e.g. {@code Foo.X},
 *       {@code Outer.Inner.X}, {@code com.foo.Bar.X}, or {@code (Foo.X)}. No
 *       arithmetic, casts, method calls, ternaries, array access, etc.</li>
 *   <li>The leftmost segment of the chain is reachable as a class, either via
 *       an explicit {@code import}, a wildcard import, or a sibling
 *       {@code SimpleClass.java}. Fully-qualified chains (leading lowercase
 *       package segment) are trusted as-is.</li>
 *   <li>The field is not annotated with
 *       {@code @SuppressWarnings("PreferStaticImportConstant")} and neither
 *       is any enclosing type.</li>
 * </ul>
 *
 * <p>Also fires on the split form where a {@code static final} field is
 * declared without an inline initializer and the assignment lives in a sibling
 * {@code static { ... }} block, as long as the assignment is the only one in a
 * single static initializer (multiple branches/static blocks are skipped to
 * avoid mis-flagging conditional assignments).</p>
 */
public class PreferStaticImportConstantCheck extends AbstractAstCheck {
	private static final String MSG_KEY = "prefer.static.import.constant";
	private static final String SUPPRESS_KEY = "PreferStaticImportConstant";

	/**
	 * Locates the unique {@code static { ... }} assignment to {@code fieldName}
	 * within {@code objBlock} (matching either bare {@code FIELD = ...} or
	 * {@code EnclosingType.FIELD = ...}/{@code pkg.EnclosingType.FIELD = ...}),
	 * returning its {@code ASSIGN} node, or {@code null} when there is no such
	 * assignment or more than one. Shared with {@code PreferStaticImportConstantFixer}
	 * so the fixer matches cinit assignments by the same rule the check enforces.
	 */
	@CheckReturnValue
	@Nullable
	public static DetailAST findStaticInitAssign(@Nonnull DetailAST objBlock, @Nonnull String fieldName) {
		final var enclosingTypeName = AstUtil.getEnclosingTypeName(objBlock);
		final var packageName = AstUtil.getPackageName(objBlock);
		DetailAST found = null;
		for (var child = objBlock.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.STATIC_INIT)
				continue;
			final var slist = child.findFirstToken(TokenTypes.SLIST);
			if (slist == null)
				continue;
			for (var stmt = slist.getFirstChild(); stmt != null; stmt = stmt.getNextSibling()) {
				if (stmt.getType() != TokenTypes.EXPR)
					continue;
				final var inner = stmt.getFirstChild();
				if (inner == null || inner.getType() != TokenTypes.ASSIGN)
					continue;
				if (!isAssignToField(inner.getFirstChild(), fieldName, enclosingTypeName, packageName))
					continue;
				if (found != null)
					return null;
				found = inner;
			}
		}
		return found;
	}

	@CheckReturnValue
	private static boolean isAssignToField(
			@Nullable DetailAST lhs,
			@Nonnull String fieldName,
			@Nullable String enclosingTypeName,
			@Nullable String packageName
	) {
		if (lhs == null)
			return false;
		if (lhs.getType() == TokenTypes.IDENT)
			return fieldName.equals(lhs.getText());
		if (lhs.getType() != TokenTypes.DOT || enclosingTypeName == null)
			return false;
		if (!AstUtil.isPureDotChainOrIdent(lhs))
			return false;
		final var lhsText = FullIdent.createFullIdent(lhs).getText();
		if (lhsText.equals(enclosingTypeName + "." + fieldName))
			return true;
		return packageName != null
				&& lhsText.equals(packageName + "." + enclosingTypeName + "." + fieldName);
	}

	/**
	 * Extracts the declared package name from a {@code PACKAGE_DEF} node (the
	 * qualified name before the terminating {@code ;}), or {@code null} for the
	 * default package. Used to build a same-package sibling class's FQCN.
	 */
	@CheckReturnValue
	@Nullable
	private static String packageNameOf(@Nonnull DetailAST packageDef) {
		final var semi = packageDef.getLastChild();
		final var nameNode = semi == null ? null : semi.getPreviousSibling();
		if (nameNode == null)
			return null;
		final var name = FullIdent.createFullIdent(nameNode).getText();
		return name == null || name.isEmpty() ? null : name;
	}

	private final Map<String, String> importedClasses = new HashMap<>();
	private final Map<String, Set<String>> staticImportsByMember = new HashMap<>();
	private final Set<String> wildcardPackages = new HashSet<>();
	@Nullable
	private String packageName;

	@Override
	public void beginTree(@Nullable DetailAST rootAST) {
		importedClasses.clear();
		staticImportsByMember.clear();
		wildcardPackages.clear();
		packageName = null;

		// a comments-only or empty file has no compilation unit, so checkstyle passes a null root
		if (rootAST == null)
			return;

		collectImports(rootAST);
	}

	void collectImports(@Nonnull DetailAST root) {
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var type = child.getType();
			if (type == TokenTypes.PACKAGE_DEF) {
				packageName = packageNameOf(child);
				continue;
			}
			if (type == TokenTypes.STATIC_IMPORT) {
				collectStaticImport(child);
				continue;
			}
			if (type != TokenTypes.IMPORT)
				continue;
			final var fqn = FullIdent.createFullIdentBelow(child).getText();
			if (fqn.endsWith(".*")) {
				final var prefix = fqn.substring(0, fqn.length() - 2);
				if (!prefix.isEmpty() && !prefix.startsWith(".") && !prefix.endsWith("."))
					wildcardPackages.add(prefix);
			}
			else if (!fqn.isEmpty()) {
				final var lastDot = fqn.lastIndexOf('.');
				if (lastDot > 0 && lastDot < fqn.length() - 1)
					importedClasses.put(fqn.substring(lastDot + 1), fqn);
			}
		}
	}

	/**
	 * Records a single {@code import static <prefix>.<member>;} into
	 * {@link #staticImportsByMember} keyed by member name. Static wildcard imports
	 * ({@code import static <prefix>.*;}) and malformed forms name no specific
	 * member, so they are ignored (they can never create a same-member conflict).
	 */
	private void collectStaticImport(@Nonnull DetailAST staticImport) {
		final var nameNode = staticImport.getFirstChild() == null ? null : staticImport.getFirstChild().getNextSibling();
		if (nameNode == null)
			return;
		final var fqn = FullIdent.createFullIdent(nameNode).getText();
		if (fqn == null || fqn.endsWith(".*"))
			return;
		final var lastDot = fqn.lastIndexOf('.');
		if (lastDot <= 0 || lastDot == fqn.length() - 1)
			return;
		staticImportsByMember.computeIfAbsent(fqn.substring(lastDot + 1), k -> new HashSet<>()).add(fqn.substring(0, lastDot));
	}

	/**
	 * @return {@code true} when the file already declares
	 * {@code import static <prefix>.<member>;} with a {@code <prefix>} other than
	 * {@code classFqcn}. Adding our own static import of {@code member} from
	 * {@code classFqcn} would then be a duplicate-member compile error, so the
	 * alias cannot be replaced with a static import and the check must stay silent.
	 * Shares the conflict rule the fixer applies in
	 * {@code PreferStaticImportConstantFixer.conflictsWithExistingStaticImport}.
	 */
	@CheckReturnValue
	private boolean conflictsWithExistingStaticImport(@Nonnull String classFqcn, @Nonnull String member) {
		final var prefixes = staticImportsByMember.get(member);
		if (prefixes == null)
			return false;
		for (var prefix : prefixes) {
			if (!prefix.equals(classFqcn))
				return true;
		}
		return false;
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.VARIABLE_DEF};
	}

	@CheckReturnValue
	private boolean isClassResolvable(@Nonnull String simpleClass) {
		if (importedClasses.containsKey(simpleClass))
			return true;
		final var filePath = getFilePath();
		if (filePath != null) {
			try {
				final var parentDir = Path.of(filePath).getParent();
				if (parentDir != null && Files.exists(parentDir.resolve(simpleClass + ".java")))
					return true;
			}
			catch (InvalidPathException ignored) {
			}
		}
		// java.lang is implicitly imported; an unqualified reference to one of
		// those types resolves there in the absence of an explicit shadowing
		// import (already checked above). The wildcard fallback must not be
		// allowed to misroute these to an unrelated wildcard-imported package.
		return JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(simpleClass) || !wildcardPackages.isEmpty();
	}

	private void processAlias(@Nonnull DetailAST reportTarget, @Nonnull DetailAST initRoot) {
		final var init = AstUtil.unwrapParensAndExpr(initRoot);
		if (init == null || init.getType() != TokenTypes.DOT)
			return;
		if (!AstUtil.isPureDotChainOrIdent(init))
			return;

		final var leftSubtree = init.getFirstChild();
		final var rightmostIdent = leftSubtree == null ? null : leftSubtree.getNextSibling();
		if (leftSubtree == null || rightmostIdent == null || rightmostIdent.getType() != TokenTypes.IDENT)
			return;

		final var classChainFull = FullIdent.createFullIdent(leftSubtree).getText();
		final var classChain = classChainFull == null || classChainFull.isEmpty() ? null : classChainFull;
		if (classChain == null)
			return;

		final var firstDot = classChain.indexOf('.');
		final var leftmostSegment = firstDot < 0 ? classChain : classChain.substring(0, firstDot);
		if (leftmostSegment.isEmpty())
			return;
		if (!Character.isLowerCase(leftmostSegment.charAt(0)) && !isClassResolvable(leftmostSegment))
			return;

		// An alias whose static-import replacement would collide with an existing
		// `import static <other>.<member>;` (different class, same member) can't be
		// auto-converted, so flagging it would suggest an impossible fix; stay silent.
		// When the class FQCN can't be determined (e.g. two wildcard imports) no
		// conflict can be proven, so fire conservatively.
		final var member = rightmostIdent.getText();
		final var classFqcn = resolveClassFqcn(classChain);
		if (classFqcn != null && conflictsWithExistingStaticImport(classFqcn, member))
			return;

		log(reportTarget, MSG_KEY, reportTarget.getText(), classChain, member);
	}

	/**
	 * Resolves the fully-qualified name of the class named by {@code classChain}
	 * (a simple name or a dotted chain), mirroring the fixer's
	 * {@code FqnResolver.resolveFqcn}: a leading-lowercase chain is already
	 * fully-qualified; otherwise the leftmost segment is resolved via an explicit
	 * import, a same-package sibling file, the implicit {@code java.lang}, or a
	 * lone wildcard import, and the remainder appended. Returns {@code null} when
	 * the FQCN can't be determined (no resolving import, or ambiguous wildcards).
	 */
	@CheckReturnValue
	@Nullable
	private String resolveClassFqcn(@Nonnull String classChain) {
		final var firstDot = classChain.indexOf('.');
		if (firstDot >= 0) {
			final var first = classChain.substring(0, firstDot);
			if (!first.isEmpty() && Character.isLowerCase(first.charAt(0)))
				return classChain;
			final var leftmost = resolveSimpleClassFqcn(first);
			return leftmost == null ? null : leftmost + classChain.substring(firstDot);
		}
		return resolveSimpleClassFqcn(classChain);
	}

	@CheckReturnValue
	@Nullable
	private String resolveSimpleClassFqcn(@Nonnull String simpleClass) {
		final var imported = importedClasses.get(simpleClass);
		if (imported != null)
			return imported;
		final var filePath = getFilePath();
		if (filePath != null) {
			try {
				final var parentDir = Path.of(filePath).getParent();
				if (parentDir != null && Files.exists(parentDir.resolve(simpleClass + ".java")))
					return packageName == null ? simpleClass : packageName + "." + simpleClass;
			}
			catch (InvalidPathException ignored) {
			}
		}
		if (JavaLangClasses.forJavaTarget(Integer.MAX_VALUE).contains(simpleClass))
			return "java.lang." + simpleClass;
		if (wildcardPackages.size() == 1)
			return wildcardPackages.iterator().next() + "." + simpleClass;
		return null;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		final var parent = ast.getParent();
		if (parent == null || parent.getType() != TokenTypes.OBJBLOCK)
			return;

		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		if (modifiers == null)
			return;
		if (modifiers.findFirstToken(TokenTypes.LITERAL_STATIC) == null
				|| modifiers.findFirstToken(TokenTypes.FINAL) == null)
			return;

		if (AstUtil.hasSuppressWarnings(modifiers, SUPPRESS_KEY))
			return;
		for (var enclosing = parent.getParent(); enclosing != null; enclosing = enclosing.getParent()) {
			final var type = enclosing.getType();
			if (type != TokenTypes.CLASS_DEF && type != TokenTypes.INTERFACE_DEF
					&& type != TokenTypes.ENUM_DEF && type != TokenTypes.RECORD_DEF
					&& type != TokenTypes.ANNOTATION_DEF)
				continue;
			final var typeModifiers = enclosing.findFirstToken(TokenTypes.MODIFIERS);
			if (typeModifiers != null && AstUtil.hasSuppressWarnings(typeModifiers, SUPPRESS_KEY))
				return;
		}

		final var fieldIdent = ast.findFirstToken(TokenTypes.IDENT);
		if (fieldIdent == null)
			return;

		final var assign = ast.findFirstToken(TokenTypes.ASSIGN);
		if (assign != null) {
			final var assignChild = assign.getFirstChild();
			if (assignChild == null)
				return;
			processAlias(fieldIdent, assignChild);
			return;
		}

		final var cinitAssign = findStaticInitAssign(parent, fieldIdent.getText());
		if (cinitAssign == null)
			return;
		final var rhs = cinitAssign.getFirstChild() == null ? null
				: cinitAssign.getFirstChild().getNextSibling();
		if (rhs == null)
			return;
		processAlias(fieldIdent, rhs);
	}
}