package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
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
 *       matter for the check (the fixer skips non-private since deletion may
 *       break external callers).</li>
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
public class PreferStaticImportConstantCheck extends AbstractCheck {
	private static final String MSG_KEY = "prefer.static.import.constant";
	private static final String SUPPRESS_KEY = "PreferStaticImportConstant";

	@CheckReturnValue
	@Nullable
	private static String classChainText(@Nonnull DetailAST chain) {
		final var text = FullIdent.createFullIdent(chain).getText();
		return text == null || text.isEmpty() ? null : text;
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST findStaticInitAssign(@Nonnull DetailAST objBlock, @Nonnull String fieldName) {
		final var enclosingTypeName = getEnclosingTypeName(objBlock);
		final var packageName = getPackageName(objBlock);
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
	@Nullable
	private static String getEnclosingTypeName(@Nonnull DetailAST objBlock) {
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
	private static String getPackageName(@Nonnull DetailAST node) {
		var root = node;
		while (root.getParent() != null)
			root = root.getParent();
		for (var child = root.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.PACKAGE_DEF)
				continue;
			final var dot = child.findFirstToken(TokenTypes.DOT);
			if (dot != null) {
				final var text = FullIdent.createFullIdent(dot).getText();
				return text == null || text.isEmpty() ? null : text;
			}
			final var ident = child.findFirstToken(TokenTypes.IDENT);
			if (ident != null) {
				final var text = ident.getText();
				return text.isEmpty() ? null : text;
			}
		}
		return null;
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
		if (!isPureDotChainOrIdent(lhs))
			return false;
		final var lhsText = FullIdent.createFullIdent(lhs).getText();
		if (lhsText.equals(enclosingTypeName + "." + fieldName))
			return true;
		return packageName != null
				&& lhsText.equals(packageName + "." + enclosingTypeName + "." + fieldName);
	}

	@CheckReturnValue
	private static boolean isLowerCaseFirst(@Nonnull String segment) {
		return !segment.isEmpty() && Character.isLowerCase(segment.charAt(0));
	}

	@CheckReturnValue
	private static boolean isPureDotChainOrIdent(@Nonnull DetailAST ast) {
		var cur = ast;
		while (true) {
			if (cur.getType() == TokenTypes.IDENT)
				return true;
			if (cur.getType() != TokenTypes.DOT)
				return false;
			final var left = cur.getFirstChild();
			if (left == null)
				return false;
			final var right = left.getNextSibling();
			if (right == null || right.getType() != TokenTypes.IDENT)
				return false;
			cur = left;
		}
	}

	@CheckReturnValue
	@Nullable
	private static DetailAST unwrapParens(@Nullable DetailAST node) {
		var cur = node;
		while (cur != null) {
			if (cur.getType() == TokenTypes.LPAREN) {
				cur = cur.getNextSibling();
				continue;
			}
			if (cur.getType() == TokenTypes.EXPR) {
				cur = cur.getFirstChild();
				continue;
			}
			break;
		}
		return cur;
	}

	private final Map<String, String> importedClasses = new HashMap<>();
	private final Set<String> wildcardPackages = new HashSet<>();

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		importedClasses.clear();
		wildcardPackages.clear();

		for (var child = rootAST.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() != TokenTypes.IMPORT)
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

	@Nonnull
	@Override
	public int[] getAcceptableTokens() {
		return getDefaultTokens();
	}

	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{TokenTypes.VARIABLE_DEF};
	}

	@Nonnull
	@Override
	public int[] getRequiredTokens() {
		return getDefaultTokens();
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
		return !wildcardPackages.isEmpty();
	}

	private void processAlias(@Nonnull DetailAST reportTarget, @Nonnull DetailAST initRoot) {
		final var init = unwrapParens(initRoot);
		if (init == null || init.getType() != TokenTypes.DOT)
			return;
		if (!isPureDotChainOrIdent(init))
			return;

		final var leftSubtree = init.getFirstChild();
		final var rightmostIdent = leftSubtree == null ? null : leftSubtree.getNextSibling();
		if (leftSubtree == null || rightmostIdent == null || rightmostIdent.getType() != TokenTypes.IDENT)
			return;

		final var classChain = classChainText(leftSubtree);
		if (classChain == null)
			return;

		final var firstDot = classChain.indexOf('.');
		final var leftmostSegment = firstDot < 0 ? classChain : classChain.substring(0, firstDot);
		if (leftmostSegment.isEmpty())
			return;
		if (!isLowerCaseFirst(leftmostSegment) && !isClassResolvable(leftmostSegment))
			return;

		log(reportTarget, MSG_KEY, reportTarget.getText(), classChain, rightmostIdent.getText());
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