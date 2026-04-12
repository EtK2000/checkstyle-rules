package com.etk2000.checkstyle.gradle;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Lightweight holder for fixable check source names and module IDs.
 * This class intentionally has no checkstyle API imports so it can be
 * loaded on the buildscript classpath without requiring checkstyle.
 */
final class FixableCheckNames {
	static final Set<String> MODULE_IDS = Set.of(
			"BlankLineAfterBreak",
			"NoBlankLineAfterClassBrace",
			"NoBlankLineBeforeClosingBrace",
			"NoDoubleBlankLines",
			"NoTrailingNewline",
			"NoTrailingWhitespace"
	);

	static final Set<String> SOURCE_NAMES = Set.of(
			"com.etk2000.checkstyle.AnnotationOwnLineCheck",
			"com.etk2000.checkstyle.AnnotationSameLineCheck",
			"com.etk2000.checkstyle.ControlFlowBracesCheck",
			"com.etk2000.checkstyle.LambdaParameterTypeCheck",
			"com.etk2000.checkstyle.NoArrayTrailingCommaCheck",
			"com.etk2000.checkstyle.NoBlankLineBetweenSingleCasesCheck",
			"com.etk2000.checkstyle.NoFinalParametersCheck",
			"com.etk2000.checkstyle.NoUnnecessaryThisCheck",
			"com.etk2000.checkstyle.PreferCollectionInterfaceCheck",
			"com.etk2000.checkstyle.PreferMathMethodCheck",
			"com.etk2000.checkstyle.PreferPrefixIncrementCheck",
			"com.etk2000.checkstyle.PreferSpecificApiCheck",
			"com.etk2000.checkstyle.PreferVarCheck",
			"com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck",
			"com.etk2000.checkstyle.RedundantNumericSuffixCheck",
			"com.puppycrawl.tools.checkstyle.checks.UpperEllCheck",
			"com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck",
			"com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck",
			"com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck",
			"com.puppycrawl.tools.checkstyle.checks.coding.NoEnumTrailingCommaCheck",
			"com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck",
			"com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck",
			"com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck"
	);

	@CheckReturnValue
	@Nonnull
	static Set<String> all() {
		final var names = new HashSet<>(SOURCE_NAMES);
		names.addAll(MODULE_IDS);
		return names;
	}
}