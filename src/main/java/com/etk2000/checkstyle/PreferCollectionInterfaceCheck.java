package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FullIdent;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that flags concrete collection types in method and
 * constructor signatures (return types and parameter types). Suggests
 * the corresponding interface type instead. Uses reflection to
 * dynamically determine the interface rather than a hardcoded map.
 * Recursively checks nested generic type arguments.
 */
public class PreferCollectionInterfaceCheck extends AbstractAstCheck {
	private static final String MSG = "prefer.replacement";

	private final Set<String> imports = new HashSet<>();

	private String packageName;

	@Override
	public void beginTree(@Nonnull DetailAST rootAST) {
		imports.clear();
		packageName = null;
	}

	private void checkParameters(@Nonnull DetailAST ast) {
		final var params = ast.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null || collapsesOntoAnOverload(ast))
			return;

		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() == TokenTypes.PARAMETER_DEF) {
				final var paramType = param.findFirstToken(TokenTypes.TYPE);
				if (paramType != null)
					checkTypeTree(paramType);
			}
		}
	}

	private void checkTypeTree(@Nonnull DetailAST ast) {
		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child.getType() == TokenTypes.DOT) {
				final var fullName = FullIdent.createFullIdent(child).getText();
				final var iface = ReflectionUtil.findCollectionInterface(fullName);
				if (iface != null) {
					final var simpleName = AstUtil.simpleName(fullName);
					// log at the last IDENT child so the fixer column targets the simple name
					DetailAST lastIdent = null;
					for (var c = child.getFirstChild(); c != null; c = c.getNextSibling()) {
						if (c.getType() == TokenTypes.IDENT)
							lastIdent = c;
					}
					if (lastIdent != null)
						log(lastIdent, MSG, iface, simpleName);
					continue;
				}
			}
			else if (child.getType() == TokenTypes.IDENT) {
				final var fqcn = ReflectionUtil.resolveClassName(child.getText(), packageName, imports);
				if (fqcn != null) {
					final var iface = ReflectionUtil.findCollectionInterface(fqcn);
					if (iface != null)
						log(child, MSG, iface, child.getText());
				}
			}
			checkTypeTree(child);
		}
	}

	/**
	 * Whether replacing this signature's concrete collections with their interfaces would make it
	 * identical to another overload's, which would stop compiling. Erasure already forbids two
	 * overloads differing only in type arguments, so comparing the erased parameter types is
	 * exactly the identity the compiler uses.
	 */
	@CheckReturnValue
	private boolean collapsesOntoAnOverload(@Nonnull DetailAST defNode) {
		final var name = defNode.findFirstToken(TokenTypes.IDENT);
		final var objBlock = defNode.getParent();
		if (name == null || objBlock == null)
			return false;

		final var rewritten = rewrittenParameterTypes(defNode);
		for (var sibling = objBlock.getFirstChild(); sibling != null; sibling = sibling.getNextSibling()) {
			if (sibling == defNode || sibling.getType() != defNode.getType())
				continue;

			final var siblingName = sibling.findFirstToken(TokenTypes.IDENT);
			if (defNode.getType() == TokenTypes.METHOD_DEF
					&& (siblingName == null || !name.getText().equals(siblingName.getText())))
				continue;

			if (rewritten.equals(rewrittenParameterTypes(sibling)))
				return true;
		}
		return false;
	}

	/** {@code canonical} with its base name replaced by its collection interface, when it has one. */
	@Nonnull
	@Override
	public int[] getDefaultTokens() {
		return new int[]{
				TokenTypes.CTOR_DEF,
				TokenTypes.IMPORT,
				TokenTypes.METHOD_DEF,
				TokenTypes.PACKAGE_DEF
		};
	}

	/**
	 * The erased parameter types of {@code defNode}, in order, with every concrete collection
	 * replaced by the interface this check would suggest for it.
	 */
	@CheckReturnValue
	@Nonnull
	private List<String> rewrittenParameterTypes(@Nonnull DetailAST defNode) {
		final var types = new ArrayList<String>();
		final var params = defNode.findFirstToken(TokenTypes.PARAMETERS);
		if (params == null)
			return types;

		for (var param = params.getFirstChild(); param != null; param = param.getNextSibling()) {
			if (param.getType() != TokenTypes.PARAMETER_DEF)
				continue;

			final var type = param.findFirstToken(TokenTypes.TYPE);
			types.add(type == null ? "" : rewrittenTypeName(AstUtil.canonicalType(type)));
		}
		return types;
	}

	@CheckReturnValue
	@Nonnull
	private String rewrittenTypeName(@Nonnull String canonical) {
		final var brackets = canonical.indexOf('[');
		final var base = brackets < 0 ? canonical : canonical.substring(0, brackets);
		final var suffix = brackets < 0 ? "" : canonical.substring(brackets);
		final var fqcn = base.indexOf('.') >= 0 ? base : ReflectionUtil.resolveClassName(base, packageName, imports);
		if (fqcn == null)
			return canonical;

		// compared fully qualified: `List` and `java.util.List` name one type but differ as strings,
		// so comparing the written spellings would miss the collapse they produce together
		final var iface = ReflectionUtil.collectionInterfaceFqcn(fqcn);
		return (iface == null ? fqcn : iface) + suffix;
	}

	@Override
	public void visitToken(@Nonnull DetailAST ast) {
		switch (ast.getType()) {
			case TokenTypes.CTOR_DEF -> checkParameters(ast);
			case TokenTypes.IMPORT -> imports.add(FullIdent.createFullIdentBelow(ast).getText());
			case TokenTypes.METHOD_DEF -> {
				final var returnType = ast.findFirstToken(TokenTypes.TYPE);
				if (returnType != null)
					checkTypeTree(returnType);
				checkParameters(ast);
			}
			case TokenTypes.PACKAGE_DEF -> {
				final var ident = ast.getLastChild().getPreviousSibling();
				packageName = FullIdent.createFullIdent(ident).getText();
			}
		}
	}
}