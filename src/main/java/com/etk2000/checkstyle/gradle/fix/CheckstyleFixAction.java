package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationOwnLineCheck;
import com.etk2000.checkstyle.AnnotationSameLineCheck;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.LambdaParameterTypeCheck;
import com.etk2000.checkstyle.NoArrayTrailingCommaCheck;
import com.etk2000.checkstyle.NoBlankLineBetweenSingleCasesCheck;
import com.etk2000.checkstyle.NoFinalParametersCheck;
import com.etk2000.checkstyle.NoUnnecessaryThisCheck;
import com.etk2000.checkstyle.PreferCollectionInterfaceCheck;
import com.etk2000.checkstyle.PreferBulkOperationCheck;
import com.etk2000.checkstyle.PreferMathMethodCheck;
import com.etk2000.checkstyle.PreferPrefixIncrementCheck;
import com.etk2000.checkstyle.PreferSpecificApiCheck;
import com.etk2000.checkstyle.PreferStandardCharsetsCheck;
import com.etk2000.checkstyle.PreferStaticImportCheck;
import com.etk2000.checkstyle.PreferVarCheck;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck;
import com.etk2000.checkstyle.RedundantNumericSuffixCheck;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.checks.UpperEllCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.AvoidNoArgumentSuperConstructorCallCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.ExplicitInitializationCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.FinalLocalVariableCheck;
import com.puppycrawl.tools.checkstyle.checks.coding.NoEnumTrailingCommaCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.RedundantImportCheck;
import com.puppycrawl.tools.checkstyle.checks.imports.UnusedImportsCheck;
import com.puppycrawl.tools.checkstyle.checks.modifier.RedundantModifierCheck;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkAction;
import org.gradle.workers.WorkParameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CheckstyleFixAction implements WorkAction<CheckstyleFixAction.Params> {
	record ApplyFixesResult(int fixCount, boolean needsSecondPass) {
	}

	interface Params extends WorkParameters {
		Property<String> getMinSdk();

		DirectoryProperty getSource();
	}

	private static final int TAB_WIDTH = 8;
	private static final String ALLOWED_METHODS = "findViewById,findViewWithTag,getArgument,getSystemService,requireViewById";
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
				Map.entry(FieldSortingCheck.class.getName(), new FieldSortingFixer()),
				Map.entry(FinalLocalVariableCheck.class.getName(), new FinalLocalVariableFixer()),
				Map.entry(LambdaParameterTypeCheck.class.getName(), new LambdaParameterTypeFixer()),
				Map.entry(NoArrayTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoFinalParametersCheck.class.getName(), modifierFixer),
				Map.entry(NoBlankLineBetweenSingleCasesCheck.class.getName(), new NoBlankLineBetweenSingleCasesFixer()),
				Map.entry(NoEnumTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoUnnecessaryThisCheck.class.getName(), new NoUnnecessaryThisFixer()),
				Map.entry(PreferCollectionInterfaceCheck.class.getName(), new PreferCollectionInterfaceFixer()),
				Map.entry(PreferBulkOperationCheck.class.getName(), new PreferBulkOperationFixer()),
				Map.entry(PreferMathMethodCheck.class.getName(), new PreferMathMethodFixer()),
				Map.entry(PreferPrefixIncrementCheck.class.getName(), new PreferPrefixIncrementFixer()),
				Map.entry(PreferSpecificApiCheck.class.getName(), new PreferSpecificApiFixer()),
				Map.entry(PreferStandardCharsetsCheck.class.getName(), new PreferStandardCharsetsFixer()),
				Map.entry(PreferStaticImportCheck.class.getName(), new PreferStaticImportFixer()),
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
	 */
	@CheckReturnValue
	@Nonnull
	static ApplyFixesResult applyFixes(
			@Nonnull List<String> lines,
			@Nonnull List<AuditEvent> violations,
			@Nonnull Map<String, CheckstyleFixer> fixers
	) {
		return applyFixes(lines, violations, fixers, Map.of());
	}

	@CheckReturnValue
	@Nonnull
	static ApplyFixesResult applyFixes(
			@Nonnull List<String> lines,
			@Nonnull List<AuditEvent> violations,
			@Nonnull Map<String, CheckstyleFixer> fixers,
			@Nonnull Map<String, CheckstyleFixer> moduleIdFixers
	) {
		violations.sort(
				Comparator.comparingInt(AuditEvent::getLine).reversed()
						.thenComparing(Comparator.comparingInt(AuditEvent::getColumn).reversed())
		);

		final var importsToAdd = new TreeSet<String>();
		var fixed = 0;
		var suppressedLine = -1;
		var passedThrough = false;
		for (var event : violations) {
			final var fixer = resolveFixer(event, fixers, moduleIdFixers);
			if (fixer == null)
				continue;
			if (event.getSeverityLevel() != SeverityLevel.ERROR)
				continue;
			final var lineIndex = event.getLine() - 1;
			if (lineIndex == suppressedLine) {
				// after a prior delete, a blank line may shift into this position;
				// allow deletion only once and only for DeleteLineFixer (e.g.
				// RedundantImport + UnusedImports double-fire: first removes
				// import, second removes leftover blank)
				if (!passedThrough && lineIndex >= 0 && lineIndex < lines.size()
						&& lines.get(lineIndex).isEmpty()
						&& fixer instanceof DeleteLineFixer)
					passedThrough = true;
				else
					continue;
			}
			else
				passedThrough = false;
			if (lineIndex < 0 || lineIndex >= lines.size())
				continue;
			final var charColumn = tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
			final var result = fixer.fix(lines, lineIndex, charColumn);
			if (result == null)
				continue;
			if (result.endLine() >= result.startLine())
				lines.subList(result.startLine(), result.endLine() + 1).clear();
			lines.addAll(result.startLine(), result.replacement());
			importsToAdd.addAll(result.importsToAdd());
			// suppress next same-line violation when this fix removed content,
			// since the line that shifted into this position is unrelated
			suppressedLine = result.replacement().size() < result.endLine() - result.startLine() + 1
					? lineIndex : -1;
			++fixed;
		}

		var needsSecondPass = false;
		if (!importsToAdd.isEmpty())
			needsSecondPass = insertMissingImports(lines, importsToAdd) > 0;

		return new ApplyFixesResult(fixed, needsSecondPass);
	}

	@CheckReturnValue
	@Nonnull
	private static DefaultConfiguration createCheckerConfig(@Nonnull String minSdk) {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", String.valueOf(TAB_WIDTH));
		for (var checkName : FIXERS.keySet()) {
			final var checkConfig = new DefaultConfiguration(checkName);
			if (checkName.equals(FinalLocalVariableCheck.class.getName()))
				checkConfig.addProperty("validateEnhancedForLoopVariable", "false");
			if (checkName.equals(PreferMathMethodCheck.class.getName())
					|| checkName.equals(PreferSpecificApiCheck.class.getName())
					|| checkName.equals(PreferStandardCharsetsCheck.class.getName())
					|| checkName.equals(PreferStaticImportCheck.class.getName()))
				checkConfig.addProperty("minSdk", minSdk);
			if (checkName.equals(PreferVarCheck.class.getName()))
				checkConfig.addProperty("allowedMethods", ALLOWED_METHODS);
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

		return checkerConfig;
	}

	private static boolean doExecute(
			@Nonnull DefaultConfiguration checkerConfig,
			@Nonnull List<File> files
	) throws CheckstyleException, IOException {
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

		checker.process(files);
		checker.destroy();

		var needsSecondPass = false;
		var filesFixed = 0;
		var totalFixed = 0;
		var totalSkipped = 0;

		for (var entry : violationsByFile.entrySet()) {
			final var filePath = Path.of(entry.getKey());
			final var violations = entry.getValue();
			final var totalViolations = violations.size();
			final var lines = new ArrayList<>(Files.readAllLines(filePath));
			final var result = applyFixes(lines, violations, FIXERS, MODULE_ID_FIXERS);

			if (result.fixCount() > 0) {
				Files.writeString(filePath, String.join("\n", lines));
				++filesFixed;
				totalFixed += result.fixCount();
			}
			if (result.needsSecondPass())
				needsSecondPass = true;
			totalSkipped += totalViolations - result.fixCount();
		}

		if (totalFixed > 0 || totalSkipped > 0)
			System.out.println("Fixed " + totalFixed + " violations in " + filesFixed + " files (" + totalSkipped + " skipped)");
		return needsSecondPass;
	}

	/**
	 * Returns the set of check source names and module IDs that have fixers.
	 * Used by tests to verify consistency with {@code FixableCheckNames}.
	 */
	@CheckReturnValue
	@Nonnull
	public static Set<String> fixableSourceNames() {
		final var names = new HashSet<>(FIXERS.keySet());
		names.addAll(MODULE_ID_FIXERS.keySet());
		return names;
	}

	/**
	 * Returns the {@code allowedMethods} value used by the fixer for PreferVarCheck.
	 * Used by tests to verify consistency with {@code checkstyle.xml}.
	 */
	@CheckReturnValue
	@Nonnull
	public static String fixerAllowedMethods() {
		return ALLOWED_METHODS;
	}

	/**
	 * Inserts missing import statements into the file lines in sorted position.
	 * Returns the number of imports added.
	 */
	static int insertMissingImports(@Nonnull List<String> lines, @Nonnull Set<String> fqns) {
		// separate static and regular imports
		final var regularToAdd = new TreeSet<String>();
		final var staticToAdd = new TreeSet<String>();
		for (var fqn : fqns) {
			if (fqn.startsWith("static "))
				staticToAdd.add(fqn.substring(7));
			else
				regularToAdd.add(fqn);
		}

		// remove already-present imports (exact matches and wildcard coverage)
		var lastImportIdx = -1;
		var lastStaticImportIdx = -1;
		var packageIdx = -1;
		for (var i = 0; i < lines.size(); ++i) {
			final var line = lines.get(i);
			if (line.startsWith("package "))
				packageIdx = i;
			else if (line.startsWith("import static ")) {
				lastStaticImportIdx = i;
				if (line.endsWith(";")) {
					final var fqn = line.substring(14, line.length() - 1);
					staticToAdd.remove(fqn);
					if (fqn.endsWith(".*"))
						staticToAdd.removeIf(s -> s.startsWith(fqn.substring(0, fqn.length() - 1)));
				}
			}
			else if (line.startsWith("import ")) {
				lastImportIdx = i;
				if (line.endsWith(";")) {
					final var fqn = line.substring(7, line.length() - 1);
					regularToAdd.remove(fqn);
					if (fqn.endsWith(".*"))
						regularToAdd.removeIf(s -> s.startsWith(fqn.substring(0, fqn.length() - 1)));
				}
			}
		}

		var addedStatic = 0;

		// insert static imports
		if (!staticToAdd.isEmpty()) {
			if (lastStaticImportIdx >= 0) {
				// existing static group: insert within it (blank-line separator already present)
				for (var fqn : staticToAdd) {
					final var importLine = "import static " + fqn + ";";
					var insertIdx = lastStaticImportIdx + 1;
					for (var i = 0; i <= lastStaticImportIdx; ++i) {
						if (lines.get(i).startsWith("import static ") && lines.get(i).compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
					lines.add(insertIdx, importLine);
					++lastStaticImportIdx;
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
					++addedStatic;
				}
			}
			else {
				// no existing static group: insert a new block with a trailing blank line
				var insertIdx = packageIdx + 1;
				if (packageIdx >= 0) {
					if (insertIdx >= lines.size() || !lines.get(insertIdx).isEmpty()) {
						lines.add(insertIdx, "");
						if (lastImportIdx >= insertIdx)
							++lastImportIdx;
					}
					++insertIdx;
				}
				for (var fqn : staticToAdd) {
					lines.add(insertIdx, "import static " + fqn + ";");
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
					++insertIdx;
					++addedStatic;
				}
				// ensure blank line after the new static group
				if (insertIdx >= lines.size() || !lines.get(insertIdx).isEmpty()) {
					lines.add(insertIdx, "");
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
				}
			}
		}

		// if statics exist but no regular imports yet, position lastImportIdx
		// after the static group (with blank separator) so regulars go below statics
		if (!regularToAdd.isEmpty() && lastImportIdx < 0) {
			for (var i = lines.size() - 1; i >= 0; --i) {
				if (lines.get(i).startsWith("import static ")) {
					if (i + 1 >= lines.size() || !lines.get(i + 1).isEmpty())
						lines.add(i + 1, "");
					lastImportIdx = i + 1;
					break;
				}
			}
		}

		if (regularToAdd.isEmpty())
			return addedStatic;

		if (lastImportIdx >= 0) {
			for (var fqn : regularToAdd) {
				final var importLine = "import " + fqn + ";";
				final var targetPrefix = "import " + fqn.substring(0, fqn.lastIndexOf('.') + 1);

				// find the same-package group boundaries
				var groupStart = -1;
				var groupEnd = -1;
				for (var i = 0; i <= lastImportIdx; ++i) {
					if (lines.get(i).startsWith(targetPrefix)) {
						if (groupStart < 0)
							groupStart = i;
						groupEnd = i;
					}
				}

				int insertIdx;
				if (groupStart >= 0) {
					// insert within the same-package group in sorted order
					insertIdx = groupEnd + 1;
					for (var i = groupStart; i <= groupEnd; ++i) {
						if (lines.get(i).compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}
				else {
					// no same-package group, fall back to global alphabetical
					insertIdx = lastImportIdx + 1;
					for (var i = 0; i <= lastImportIdx; ++i) {
						final var line = lines.get(i);
						if (line.startsWith("import ") && !line.startsWith("import static ")
								&& line.compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}

				lines.add(insertIdx, importLine);
				++lastImportIdx;
			}
		}
		else if (packageIdx >= 0) {
			// no existing imports, insert after package with blank line
			var insertIdx = packageIdx + 1;
			if (insertIdx < lines.size() && lines.get(insertIdx).isEmpty())
				++insertIdx;
			else
				lines.add(insertIdx++, "");
			for (var fqn : regularToAdd)
				lines.add(insertIdx++, "import " + fqn + ";");
		}
		else {
			// no package, no imports
			var insertIdx = 0;
			for (var fqn : regularToAdd)
				lines.add(insertIdx++, "import " + fqn + ";");
		}

		return addedStatic + regularToAdd.size();
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

	@Override
	public void execute() {
		try {
			final var checkerConfig = createCheckerConfig(getParameters().getMinSdk().get());

			final var sourceDir = getParameters().getSource().get().getAsFile().toPath();
			final List<File> files;
			try (var stream = Files.walk(sourceDir)) {
				files = stream
						.filter(p -> p.toString().endsWith(".java"))
						.map(Path::toFile)
						.toList();
			}

			// Pass 1: apply all fixes. Pass 2: clean up cascading violations
			// (e.g. imports that became unused after a fixer replaced their usage).
			if (doExecute(checkerConfig, files))
				doExecute(checkerConfig, files);
		}
		catch (CheckstyleException | IOException e) {
			throw new RuntimeException("Checkstyle fix failed", e);
		}
	}
}