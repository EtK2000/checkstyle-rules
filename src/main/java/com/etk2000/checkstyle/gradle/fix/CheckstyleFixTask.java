package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationOwnLineCheck;
import com.etk2000.checkstyle.AnnotationSameLineCheck;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.LambdaParameterTypeCheck;
import com.etk2000.checkstyle.NoArrayTrailingCommaCheck;
import com.etk2000.checkstyle.NoFinalParametersCheck;
import com.etk2000.checkstyle.NoBlankLineBetweenSingleCasesCheck;
import com.etk2000.checkstyle.NoUnnecessaryThisCheck;
import com.etk2000.checkstyle.PreferCollectionInterfaceCheck;
import com.etk2000.checkstyle.PreferMathMethodCheck;
import com.etk2000.checkstyle.PreferPrefixIncrementCheck;
import com.etk2000.checkstyle.PreferSpecificApiCheck;
import com.etk2000.checkstyle.PreferVarCheck;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck;
import com.etk2000.checkstyle.RedundantNumericSuffixCheck;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.NoEnumTrailingCommaCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;
import com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@DisableCachingByDefault(because = "Modifies source files in place")
public abstract class CheckstyleFixTask extends DefaultTask {
	private static final int TAB_WIDTH = 8;
	private static final String BLANK_LINE_AFTER_BREAK_ID = "BlankLineAfterBreak";
	private static final String BLANK_LINE_AFTER_CLASS_BRACE_ID = "NoBlankLineAfterClassBrace";
	private static final String BLANK_LINE_BEFORE_CLOSING_BRACE_ID = "NoBlankLineBeforeClosingBrace";
	private static final String DOUBLE_BLANK_LINES_ID = "NoDoubleBlankLines";
	private static final String TRAILING_NEWLINE_ID = "NoTrailingNewline";
	private static final String TRAILING_WHITESPACE_ID = "NoTrailingWhitespace";

	// keyed by module id (Checker-level modules like RegexpSingleline/RegexpMultiline)
	static final Map<String, CheckstyleFixer> MODULE_ID_FIXERS = Map.of(
			BLANK_LINE_AFTER_BREAK_ID, new BlankLineAfterBreakFixer(),
			BLANK_LINE_AFTER_CLASS_BRACE_ID, new BlankLineAfterClassBraceFixer(),
			BLANK_LINE_BEFORE_CLOSING_BRACE_ID, new BlankLineBeforeClosingBraceFixer(),
			DOUBLE_BLANK_LINES_ID, new DoubleBlankLineFixer(),
			TRAILING_NEWLINE_ID, new TrailingNewlineFixer(),
			TRAILING_WHITESPACE_ID, new TrailingWhitespaceFixer()
	);

	// keyed by check class name (TreeWalker modules)
	static final Map<String, CheckstyleFixer> FIXERS;

	static {
		final var commaFixer = new NoArrayTrailingCommaFixer();
		final var deleteLineFixer = new DeleteLineFixer();
		final var modifierFixer = new RedundantModifierFixer();
		FIXERS = Map.ofEntries(
				Map.entry(AnnotationOwnLineCheck.class.getName(), new AnnotationOwnLineFixer()),
				Map.entry(AnnotationSameLineCheck.class.getName(), new AnnotationSameLineFixer()),
				Map.entry(AvoidNoArgumentSuperConstructorCallCheck.class.getName(), new AvoidNoArgumentSuperCallFixer()),
				Map.entry(ControlFlowBracesCheck.class.getName(), new ControlFlowBracesFixer()),
				Map.entry(ExplicitInitializationCheck.class.getName(), new ExplicitInitializationFixer()),
				Map.entry(FinalLocalVariableCheck.class.getName(), new FinalLocalVariableFixer()),
				Map.entry(LambdaParameterTypeCheck.class.getName(), new LambdaParameterTypeFixer()),
				Map.entry(NoArrayTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoFinalParametersCheck.class.getName(), modifierFixer),
				Map.entry(NoBlankLineBetweenSingleCasesCheck.class.getName(), new NoBlankLineBetweenSingleCasesFixer()),
				Map.entry(NoEnumTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoUnnecessaryThisCheck.class.getName(), new NoUnnecessaryThisFixer()),
				Map.entry(PreferCollectionInterfaceCheck.class.getName(), new PreferCollectionInterfaceFixer()),
				Map.entry(PreferMathMethodCheck.class.getName(), new PreferMathMethodFixer()),
				Map.entry(PreferPrefixIncrementCheck.class.getName(), new PreferPrefixIncrementFixer()),
				Map.entry(PreferSpecificApiCheck.class.getName(), new PreferSpecificApiFixer()),
				Map.entry(PreferVarCheck.class.getName(), new PreferVarFixer()),
				Map.entry(RedundantAnnotationSyntaxCheck.class.getName(), new RedundantAnnotationSyntaxFixer()),
				Map.entry(RedundantImportCheck.class.getName(), deleteLineFixer),
				Map.entry(RedundantModifierCheck.class.getName(), modifierFixer),
				Map.entry(RedundantNumericSuffixCheck.class.getName(), new RedundantNumericSuffixFixer()),
				Map.entry(UnusedImportsCheck.class.getName(), deleteLineFixer),
				Map.entry(UpperEllCheck.class.getName(), new UpperEllFixer())
		);
	}

	/**
	 * Applies fixes to lines based on violations, processing bottom-to-top.
	 * Returns the number of fixes applied.
	 */
	static int applyFixes(
			@Nonnull List<String> lines,
			@Nonnull List<AuditEvent> violations,
			@Nonnull Map<String, CheckstyleFixer> fixers
	) {
		return applyFixes(lines, violations, fixers, Map.of());
	}

	static int applyFixes(
			@Nonnull List<String> lines,
			@Nonnull List<AuditEvent> violations,
			@Nonnull Map<String, CheckstyleFixer> fixers,
			@Nonnull Map<String, CheckstyleFixer> moduleIdFixers
	) {
		violations.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);

		var fixed = 0;
		for (var event : violations) {
			final var fixer = resolveFixer(event, fixers, moduleIdFixers);
			if (fixer == null)
				continue;
			if (event.getSeverityLevel() != SeverityLevel.ERROR)
				continue;
			final var lineIndex = event.getLine() - 1;
			final var charColumn = tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
			final var result = fixer.fix(lines, lineIndex, charColumn);
			if (result == null)
				continue;
			if (result.endLine() >= result.startLine())
				lines.subList(result.startLine(), result.endLine() + 1).clear();
			lines.addAll(result.startLine(), result.replacement());
			++fixed;
		}
		return fixed;
	}

	/**
	 * Returns the set of check source names and module IDs that have fixers.
	 * Used by the plugin to determine which violations in a Checkstyle XML
	 * report are auto-fixable.
	 */
	@CheckReturnValue
	@Nonnull
	public static Set<String> fixableSourceNames() {
		final var names = new HashSet<>(FIXERS.keySet());
		names.addAll(MODULE_ID_FIXERS.keySet());
		return names;
	}

	@CheckReturnValue
	@Nullable
	private static CheckstyleFixer resolveFixer(
			@Nonnull AuditEvent event,
			@Nonnull Map<String, CheckstyleFixer> fixers,
			@Nonnull Map<String, CheckstyleFixer> moduleIdFixers
	) {
		final var fixer = fixers.get(event.getSourceName());
		if (fixer != null)
			return fixer;
		final var moduleId = event.getModuleId();
		if (moduleId != null)
			return moduleIdFixers.get(moduleId);
		return null;
	}

	/**
	 * Converts a tab-expanded column (as reported by Checkstyle) to a
	 * character index in the line. Checkstyle expands TABs to
	 * {@link #TAB_WIDTH} when calculating column numbers.
	 */
	@CheckReturnValue
	static int tabColumnToCharIndex(@Nonnull String line, int tabExpandedCol) {
		var visualCol = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (visualCol >= tabExpandedCol)
				return i;
			if (line.charAt(i) == '\t')
				visualCol += TAB_WIDTH - (visualCol % TAB_WIDTH);
			else
				++visualCol;
		}
		return line.length();
	}

	@TaskAction
	public void fix() throws Exception {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", String.valueOf(TAB_WIDTH));
		for (var checkName : FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.equals(FinalLocalVariableCheck.class.getName()))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
			treeWalkerConfig.addChild(checkConfig);
		}

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		// Checker-level regex modules
		final var blankAfterBreakConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterBreakConfig.addProperty("id", BLANK_LINE_AFTER_BREAK_ID);
		blankAfterBreakConfig.addProperty("format", "break\\s*;\\n[^\\S\\n]*(case |default[\\s:])");
		blankAfterBreakConfig.addProperty("message", "Add a blank line after break; before the next case/default.");
		checkerConfig.addChild(blankAfterBreakConfig);

		final var blankAfterClassBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterClassBraceConfig.addProperty("id", BLANK_LINE_AFTER_CLASS_BRACE_ID);
		blankAfterClassBraceConfig.addProperty("format", "(class|interface|enum|record)\\s+\\w[^{]*\\{\\s*\\n\\s*\\n");
		blankAfterClassBraceConfig.addProperty("message", "No blank line at start of a class/interface/enum/record.");
		checkerConfig.addChild(blankAfterClassBraceConfig);

		final var blankBeforeCloseBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankBeforeCloseBraceConfig.addProperty("id", BLANK_LINE_BEFORE_CLOSING_BRACE_ID);
		blankBeforeCloseBraceConfig.addProperty("format", "\\n[^\\S\\n]*\\n[^\\S\\n]*\\}");
		blankBeforeCloseBraceConfig.addProperty("message", "No blank line before closing brace.");
		checkerConfig.addChild(blankBeforeCloseBraceConfig);

		final var doubleBlankConfig = new DefaultConfiguration("RegexpMultiline");
		doubleBlankConfig.addProperty("id", DOUBLE_BLANK_LINES_ID);
		doubleBlankConfig.addProperty("format", "\\n\\s*\\n\\s*\\n");
		doubleBlankConfig.addProperty("message", "No double blank lines.");
		checkerConfig.addChild(doubleBlankConfig);

		final var trailingNewlineConfig = new DefaultConfiguration("RegexpMultiline");
		trailingNewlineConfig.addProperty("id", TRAILING_NEWLINE_ID);
		trailingNewlineConfig.addProperty("format", "\\n\\z");
		trailingNewlineConfig.addProperty("message", "File must not end with a trailing newline.");
		checkerConfig.addChild(trailingNewlineConfig);

		final var trailingWsConfig = new DefaultConfiguration("RegexpSingleline");
		trailingWsConfig.addProperty("id", TRAILING_WHITESPACE_ID);
		trailingWsConfig.addProperty("format", "[ \\t]+$");
		trailingWsConfig.addProperty("message", "No trailing whitespace.");
		checkerConfig.addChild(trailingWsConfig);

		final var checker = new Checker();
		checker.setModuleClassLoader(NoArrayTrailingCommaCheck.class.getClassLoader());
		checker.configure(checkerConfig);

		final var violationsByFile = new HashMap<String, List<AuditEvent>>();
		checker.addListener(new AuditListener() {
			@Override
			public void addError(@Nonnull AuditEvent event) {
				violationsByFile.computeIfAbsent(event.getFileName(), k -> new ArrayList<>()).add(event);
			}

			@Override
			public void addException(@Nonnull AuditEvent event, @Nonnull Throwable throwable) {
			}

			@Override
			public void auditFinished(@Nonnull AuditEvent event) {
			}

			@Override
			public void auditStarted(@Nonnull AuditEvent event) {
			}

			@Override
			public void fileFinished(@Nonnull AuditEvent event) {
			}

			@Override
			public void fileStarted(@Nonnull AuditEvent event) {
			}
		});

		final var sourceDir = getSource().get().getAsFile().toPath();
		final List<File> files;
		try (var stream = Files.walk(sourceDir)) {
			files = stream
					.filter(p -> p.toString().endsWith(".java"))
					.map(Path::toFile)
					.toList();
		}

		checker.process(files);
		checker.destroy();

		var filesFixed = 0;
		var totalFixed = 0;
		var totalSkipped = 0;

		for (var entry : violationsByFile.entrySet()) {
			final var filePath = Path.of(entry.getKey());
			final var violations = entry.getValue();
			final var totalViolations = violations.size();
			final var lines = new ArrayList<>(Files.readAllLines(filePath));
			final var fileFixed = applyFixes(lines, violations, FIXERS, MODULE_ID_FIXERS);

			if (fileFixed > 0) {
				Files.writeString(filePath, String.join("\n", lines));
				++filesFixed;
				totalFixed += fileFixed;
			}
			totalSkipped += totalViolations - fileFixed;
		}

		getLogger().lifecycle("Fixed {} violations in {} files ({} skipped)", totalFixed, filesFixed, totalSkipped);
	}

	@Internal
	public abstract DirectoryProperty getSource();
}