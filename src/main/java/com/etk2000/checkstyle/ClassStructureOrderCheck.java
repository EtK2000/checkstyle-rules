package com.etk2000.checkstyle;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Checkstyle check that enforces class structure ordering:
 * 1. Inner types, 2. Static fields, 3. Static initializers,
 * 4. Static methods, 5. Instance fields, 6. Constructors/instance initializers,
 * 7. Instance methods.
 */
public class ClassStructureOrderCheck extends AbstractCheck {
	private static final String MSG_KEY = "class.structure.order";
	private static final String[] SECTION_NAMES = {
			"",
			"inner type",
			"static field",
			"static initializer",
			"static method",
			"instance field",
			"constructor/instance initializer",
			"instance method"
	};

	@CheckReturnValue
	@Nonnull
	private static String getName(@Nonnull DetailAST ast) {
		final var ident = ast.findFirstToken(TokenTypes.IDENT);
		if (ident != null)
			return ident.getText();
		return switch (ast.getType()) {
			case TokenTypes.COMPACT_CTOR_DEF -> "<compact ctor>";
			case TokenTypes.INSTANCE_INIT -> "<instance init>";
			case TokenTypes.STATIC_INIT -> "<static init>";
			default -> "<unknown>";
		};
	}

	@CheckReturnValue
	private static boolean hasModifier(@Nonnull DetailAST ast, int modifierType) {
		final var modifiers = ast.findFirstToken(TokenTypes.MODIFIERS);
		return modifiers != null && modifiers.findFirstToken(modifierType) != null;
	}

	@CheckReturnValue
	private static int sectionOf(@Nonnull DetailAST ast) {
		return switch (ast.getType()) {
			case TokenTypes.CLASS_DEF, TokenTypes.ENUM_DEF, TokenTypes.INTERFACE_DEF,
			     TokenTypes.RECORD_DEF -> 1;
			case TokenTypes.COMPACT_CTOR_DEF, TokenTypes.CTOR_DEF, TokenTypes.INSTANCE_INIT -> 6;
			case TokenTypes.METHOD_DEF -> hasModifier(ast, TokenTypes.LITERAL_STATIC) ? 4 : 7;
			case TokenTypes.STATIC_INIT -> 3;
			case TokenTypes.VARIABLE_DEF -> hasModifier(ast, TokenTypes.LITERAL_STATIC) ? 2 : 5;
			default -> 0;
		};
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
		var highestSection = 0;

		for (var child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
			final var section = sectionOf(child);
			if (section == 0)
				continue;

			if (section < highestSection) {
				final var name = getName(child);
				log(
						child,
						MSG_KEY,
						name,
						SECTION_NAMES[section],
						SECTION_NAMES[highestSection]
				);
			}
			else
				highestSection = section;
		}
	}
}