package com.etk2000.checkstyle.gradle;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Lightweight holder for fixable check source names, module IDs and regexp messages.
 * This class intentionally has no checkstyle API imports so it can be
 * loaded on the buildscript classpath without requiring checkstyle.
 */
public final class FixableCheckNames {
	public static final String BLANK_LINE_AFTER_BREAK_MESSAGE = "Add a blank line after break; before the next case/default.";
	public static final String BLANK_LINE_AFTER_CLASS_BRACE_MESSAGE = "Class/interface/enum/record body must not start with a blank line.";
	public static final String BLANK_LINE_BEFORE_CLOSING_BRACE_MESSAGE = "Closing brace must not be preceded by a blank line.";
	public static final String DOUBLE_BLANK_LINES_MESSAGE = "File must not contain consecutive blank lines.";
	public static final String TRAILING_NEWLINE_MESSAGE = "File must not end with a trailing newline.";
	public static final String TRAILING_WHITESPACE_MESSAGE = "Line must not end with whitespace.";

	/**
	 * Messages from fixable regexp-based checks (RegexpMultiline/RegexpSingleline).
	 * The XML report's {@code source} attribute for these is the generic regexp class
	 * name, not the module ID, so matching by message is the only way to identify them.
	 */
	static final Set<String> FIXABLE_MESSAGES = Set.of(
			BLANK_LINE_AFTER_BREAK_MESSAGE,
			BLANK_LINE_AFTER_CLASS_BRACE_MESSAGE,
			BLANK_LINE_BEFORE_CLOSING_BRACE_MESSAGE,
			DOUBLE_BLANK_LINES_MESSAGE,
			TRAILING_NEWLINE_MESSAGE,
			TRAILING_WHITESPACE_MESSAGE
	);

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
			"com.etk2000.checkstyle.ArrayTypeStyleCheck",
			"com.etk2000.checkstyle.ConstructorAssignmentOrderCheck",
			"com.etk2000.checkstyle.ControlFlowBracesCheck",
			"com.etk2000.checkstyle.EmptyBodyCheck",
			"com.etk2000.checkstyle.FieldConsolidationCheck",
			"com.etk2000.checkstyle.FieldSortingCheck",
			"com.etk2000.checkstyle.JitInefficiencyCheck",
			"com.etk2000.checkstyle.LambdaParameterTypeCheck",
			"com.etk2000.checkstyle.MultilineCallFormattingCheck",
			"com.etk2000.checkstyle.NoArrayTrailingCommaCheck",
			"com.etk2000.checkstyle.NoBlankLineBetweenSingleCasesCheck",
			"com.etk2000.checkstyle.NoEnumTrailingSemicolonCheck",
			"com.etk2000.checkstyle.NoFinalParametersCheck",
			"com.etk2000.checkstyle.NoUnnecessaryThisCheck",
			"com.etk2000.checkstyle.PreferBulkOperationCheck",
			"com.etk2000.checkstyle.PreferCollectionInterfaceCheck",
			"com.etk2000.checkstyle.PreferDirectBooleanReturnCheck",
			"com.etk2000.checkstyle.PreferDoWhileCheck",
			"com.etk2000.checkstyle.PreferExactAssertionCheck",
			"com.etk2000.checkstyle.PreferImportCheck",
			"com.etk2000.checkstyle.PreferLiteralSuffixCheck",
			"com.etk2000.checkstyle.PreferMathMethodCheck",
			"com.etk2000.checkstyle.PreferPrefixIncrementCheck",
			"com.etk2000.checkstyle.PreferSpecificApiCheck",
			"com.etk2000.checkstyle.PreferStandardCharsetsCheck",
			"com.etk2000.checkstyle.PreferStaticImportCheck",
			"com.etk2000.checkstyle.PreferStaticImportConstantCheck",
			"com.etk2000.checkstyle.PreferVarCheck",
			"com.etk2000.checkstyle.RecordFormattingCheck",
			"com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck",
			"com.etk2000.checkstyle.RedundantArrayCreationCheck",
			"com.etk2000.checkstyle.RedundantCastCheck",
			"com.etk2000.checkstyle.RedundantEqualityBranchCheck",
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