package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationOwnLineCheck;
import com.etk2000.checkstyle.AnnotationSameLineCheck;
import com.etk2000.checkstyle.ArrayTypeStyleCheck;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.FieldConsolidationCheck;
import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.JitInefficiencyCheck;
import com.etk2000.checkstyle.LambdaParameterTypeCheck;
import com.etk2000.checkstyle.MultilineCallFormattingCheck;
import com.etk2000.checkstyle.NoArrayTrailingCommaCheck;
import com.etk2000.checkstyle.NoBlankLineBetweenSingleCasesCheck;
import com.etk2000.checkstyle.NoEnumTrailingSemicolonCheck;
import com.etk2000.checkstyle.NoFinalParametersCheck;
import com.etk2000.checkstyle.NoUnnecessaryThisCheck;
import com.etk2000.checkstyle.PreferBulkOperationCheck;
import com.etk2000.checkstyle.PreferCollectionInterfaceCheck;
import com.etk2000.checkstyle.PreferDirectBooleanReturnCheck;
import com.etk2000.checkstyle.PreferDoWhileCheck;
import com.etk2000.checkstyle.PreferExactAssertionCheck;
import com.etk2000.checkstyle.PreferImportCheck;
import com.etk2000.checkstyle.PreferLiteralSuffixCheck;
import com.etk2000.checkstyle.PreferMathMethodCheck;
import com.etk2000.checkstyle.PreferPrefixIncrementCheck;
import com.etk2000.checkstyle.PreferSpecificApiCheck;
import com.etk2000.checkstyle.PreferStandardCharsetsCheck;
import com.etk2000.checkstyle.PreferStaticImportCheck;
import com.etk2000.checkstyle.PreferStaticImportConstantCheck;
import com.etk2000.checkstyle.PreferVarCheck;
import com.etk2000.checkstyle.RecordFormattingCheck;
import com.etk2000.checkstyle.RedundantAnnotationSyntaxCheck;
import com.etk2000.checkstyle.RedundantArrayCreationCheck;
import com.etk2000.checkstyle.RedundantCastCheck;
import com.etk2000.checkstyle.RedundantEqualityBranchCheck;
import com.etk2000.checkstyle.RedundantNumericSuffixCheck;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
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
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CheckstyleFixAction implements WorkAction<CheckstyleFixAction.Params> {
	record ApplyFixesResult(
			int fixCount,
			boolean needsSecondPass,
			@Nonnull Map<String, List<String>> skippedReasons
	) {}

	interface Params extends WorkParameters {
		Property<Boolean> getDryRun();

		Property<String> getDryRunTaskName();

		Property<Integer> getDryRunTotal();

		Property<String> getMinSdk();

		DirectoryProperty getSource();

		DirectoryProperty getTestSource();
	}

	private static final String ALLOWED_METHODS = "findViewById,findViewWithTag,getArgument,getSystemService,requireViewById";
	private static final String BLANK_LINE_AFTER_BREAK_ID = "BlankLineAfterBreak";
	private static final String BLANK_LINE_AFTER_CLASS_BRACE_ID = "NoBlankLineAfterClassBrace";
	private static final String BLANK_LINE_BEFORE_CLOSING_BRACE_ID = "NoBlankLineBeforeClosingBrace";
	private static final String DOUBLE_BLANK_LINES_ID = "NoDoubleBlankLines";
	private static final String TRAILING_NEWLINE_ID = "NoTrailingNewline";
	private static final String TRAILING_WHITESPACE_ID = "NoTrailingWhitespace";

	static final Map<String, CheckstyleFixer> MODULE_ID_FIXERS = Map.of(
			BLANK_LINE_AFTER_BREAK_ID, new BlankLineAfterBreakFixer(),
			BLANK_LINE_AFTER_CLASS_BRACE_ID, new BlankLineAfterClassBraceFixer(),
			BLANK_LINE_BEFORE_CLOSING_BRACE_ID, new BlankLineBeforeClosingBraceFixer(),
			DOUBLE_BLANK_LINES_ID, new DoubleBlankLineFixer(),
			TRAILING_NEWLINE_ID, new TrailingNewlineFixer(),
			TRAILING_WHITESPACE_ID, new TrailingWhitespaceFixer()
	);

	static final Map<String, CheckstyleFixer> FIXERS;

	static {
		final var commaFixer = new NoArrayTrailingCommaFixer();
		final var deleteLineFixer = new DeleteLineFixer();
		final var modifierFixer = new RedundantModifierFixer();
		FIXERS = Map.ofEntries(
				Map.entry(AnnotationOwnLineCheck.class.getName(), new AnnotationOwnLineFixer()),
				Map.entry(AnnotationSameLineCheck.class.getName(), new AnnotationSameLineFixer()),
				Map.entry(ArrayTypeStyleCheck.class.getName(), new ArrayTypeStyleFixer()),
				Map.entry(AvoidNoArgumentSuperConstructorCallCheck.class.getName(), new AvoidNoArgumentSuperCallFixer()),
				Map.entry(ConstructorAssignmentOrderCheck.class.getName(), new ConstructorAssignmentOrderFixer()),
				Map.entry(ControlFlowBracesCheck.class.getName(), new ControlFlowBracesFixer()),
				Map.entry(ExplicitInitializationCheck.class.getName(), new ExplicitInitializationFixer()),
				Map.entry(FieldConsolidationCheck.class.getName(), new FieldConsolidationFixer()),
				Map.entry(FieldSortingCheck.class.getName(), new FieldSortingFixer()),
				Map.entry(FinalLocalVariableCheck.class.getName(), new FinalLocalVariableFixer()),
				Map.entry(JitInefficiencyCheck.class.getName(), new JitInefficiencyFixer()),
				Map.entry(LambdaParameterTypeCheck.class.getName(), new LambdaParameterTypeFixer()),
				Map.entry(MultilineCallFormattingCheck.class.getName(), new MultilineCallFormattingFixer()),
				Map.entry(NoArrayTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoFinalParametersCheck.class.getName(), modifierFixer),
				Map.entry(NoBlankLineBetweenSingleCasesCheck.class.getName(), new NoBlankLineBetweenSingleCasesFixer()),
				Map.entry(NoEnumTrailingCommaCheck.class.getName(), commaFixer),
				Map.entry(NoEnumTrailingSemicolonCheck.class.getName(), new NoEnumTrailingSemicolonFixer()),
				Map.entry(NoUnnecessaryThisCheck.class.getName(), new NoUnnecessaryThisFixer()),
				Map.entry(PreferBulkOperationCheck.class.getName(), new PreferBulkOperationFixer()),
				Map.entry(PreferCollectionInterfaceCheck.class.getName(), new PreferCollectionInterfaceFixer()),
				Map.entry(PreferDirectBooleanReturnCheck.class.getName(), new PreferDirectBooleanReturnFixer()),
				Map.entry(PreferDoWhileCheck.class.getName(), new PreferDoWhileFixer()),
				Map.entry(PreferExactAssertionCheck.class.getName(), new PreferExactAssertionFixer()),
				Map.entry(PreferImportCheck.class.getName(), new PreferImportFixer()),
				Map.entry(PreferLiteralSuffixCheck.class.getName(), new PreferLiteralSuffixFixer()),
				Map.entry(PreferMathMethodCheck.class.getName(), new PreferMathMethodFixer()),
				Map.entry(PreferPrefixIncrementCheck.class.getName(), new PreferPrefixIncrementFixer()),
				Map.entry(PreferSpecificApiCheck.class.getName(), new PreferSpecificApiFixer()),
				Map.entry(PreferStandardCharsetsCheck.class.getName(), new PreferStandardCharsetsFixer()),
				Map.entry(PreferStaticImportCheck.class.getName(), new PreferStaticImportFixer()),
				Map.entry(PreferStaticImportConstantCheck.class.getName(), new PreferStaticImportConstantFixer()),
				Map.entry(PreferVarCheck.class.getName(), new PreferVarFixer()),
				Map.entry(RecordFormattingCheck.class.getName(), new RecordFormattingFixer()),
				Map.entry(RedundantAnnotationSyntaxCheck.class.getName(), new RedundantAnnotationSyntaxFixer()),
				Map.entry(RedundantArrayCreationCheck.class.getName(), new RedundantArrayCreationFixer()),
				Map.entry(RedundantCastCheck.class.getName(), new RedundantCastFixer()),
				Map.entry(RedundantEqualityBranchCheck.class.getName(), new RedundantEqualityBranchFixer()),
				Map.entry(RedundantImportCheck.class.getName(), deleteLineFixer),
				Map.entry(RedundantModifierCheck.class.getName(), modifierFixer),
				Map.entry(RedundantNumericSuffixCheck.class.getName(), new RedundantNumericSuffixFixer()),
				Map.entry(UnusedImportsCheck.class.getName(), new UnusedImportsFixer()),
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

		final var preFixUsedImports = collectUsedImports(lines);
		final var importsToAdd = new TreeSet<String>();
		final var skippedReasons = new LinkedHashMap<String, List<String>>();
		var fixed = 0;
		var suppressedLine = -1;
		var passedThrough = false;
		try {
			if (!violations.isEmpty() && violations.getFirst().getFileName() != null)
				FixContext.setFilePath(violations.getFirst().getFileName());
			for (var event : violations) {
				final var checkName = extractCheckShortName(event);
				final var fixer = resolveFixer(event, fixers, moduleIdFixers);
				if (fixer == null) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_NO_FIXER);
					continue;
				}
				if (event.getSeverityLevel() != SeverityLevel.ERROR) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_SEVERITY);
					continue;
				}
				final var lineIndex = event.getLine() - 1;
				if (lineIndex == suppressedLine) {
					// after a prior delete, a blank line may shift into this position;
					// allow deletion only once and only for a line-deleting fixer (e.g.
					// RedundantImport + UnusedImports double-fire: first removes
					// import, second removes leftover blank)
					if (!passedThrough && lineIndex >= 0 && lineIndex < lines.size()
							&& lines.get(lineIndex).isEmpty()
							&& fixer instanceof LineDeleter)
						passedThrough = true;
					else {
						trackSkip(skippedReasons, checkName, SkipMessages.FIX_SUPPRESSED);
						continue;
					}
				}
				else
					passedThrough = false;
				if (lineIndex < 0 || lineIndex >= lines.size()) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_BOUNDS);
					continue;
				}
				final var charColumn = tabColumnToCharIndex(lines.get(lineIndex), event.getColumn() - 1);
				FixContext.setViolation(event.getViolation());
				final FixAttempt attempt;
				try {
					attempt = fixer.fix(lines, lineIndex, charColumn);
				}
				// one violation the fixer cannot survive must not abandon the run: files
				// already rewritten stay written, and the rest still get fixed. A
				// VirtualMachineError other than a blown stack is not survivable, so it
				// propagates and leaves the current file untouched rather than writing a
				// half-fixed one.
				catch (RuntimeException | AssertionError | LinkageError | StackOverflowError e) {
					System.err.println(checkName + " fixer failed at " + event.getFileName() + ":" + event.getLine());
					e.printStackTrace();
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_ERROR);
					continue;
				}
				if (attempt == null) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_NOT_FIXABLE);
					continue;
				}
				if (attempt instanceof SkipResult(String reason)) {
					trackSkip(skippedReasons, checkName, reason);
					continue;
				}
				final var result = (FixResult) attempt;
				if (result.startLine() < 0 || result.startLine() > lines.size()
						|| result.endLine() >= lines.size()) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_BOUNDS);
					continue;
				}
				if (result.endLine() >= result.startLine())
					lines.subList(result.startLine(), result.endLine() + 1).clear();
				lines.addAll(result.startLine(), result.replacement());
				importsToAdd.addAll(result.importsToAdd());
				// suppress the next violation whose lineIndex falls past the replacement range
				// after an actual deletion, since that line belongs to content that shifted up
				suppressedLine = result.endLine() >= result.startLine()
						&& lineIndex >= result.startLine() + result.replacement().size()
						? lineIndex : -1;
				++fixed;
			}
		}
		finally {
			FixerAst.clearCache();
			FixContext.clearFilePath();
			FixContext.clearViolation();
		}

		var needsSecondPass = false;
		if (!importsToAdd.isEmpty())
			needsSecondPass = insertMissingImports(lines, importsToAdd) > 0;
		if (!needsSecondPass && fixed > 0 && !preFixUsedImports.isEmpty()) {
			final var postFixImportLines = new HashSet<String>();
			for (var line : lines) {
				final var stripped = line.strip();
				if (stripped.startsWith("import "))
					postFixImportLines.add(stripped);
			}
			final var postFixBody = collectBodyLines(lines);
			for (var entry : preFixUsedImports.entrySet()) {
				if (postFixImportLines.contains(entry.getKey())
						&& !JavaSourceScanner.containsIdentifier(postFixBody, entry.getValue())) {
					needsSecondPass = true;
					break;
				}
			}
		}

		final var immutableReasons = new LinkedHashMap<String, List<String>>();
		for (var entry : skippedReasons.entrySet())
			immutableReasons.put(entry.getKey(), List.copyOf(entry.getValue()));
		return new ApplyFixesResult(fixed, needsSecondPass, Map.copyOf(immutableReasons));
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> collectBodyLines(@Nonnull List<String> lines) {
		final var result = new ArrayList<String>();
		for (var line : lines) {
			final var trimmed = line.strip();
			if (trimmed.startsWith("import ") || trimmed.startsWith("package "))
				continue;
			result.add(line);
		}
		return result;
	}

	@VisibleForTesting
	static void collectJavaFiles(@Nonnull Path dir, @Nonnull List<File> out) throws IOException {
		if (!Files.isDirectory(dir))
			return;
		try (var stream = Files.walk(dir)) {
			stream.filter(p -> p.toString().endsWith(".java"))
					.map(Path::toFile)
					.forEach(out::add);
		}
	}

	@CheckReturnValue
	@Nonnull
	private static Map<String, String> collectUsedImports(@Nonnull List<String> lines) {
		final var imports = new LinkedHashMap<String, String>();
		for (var line : lines) {
			final var parsed = ImportLine.parse(line);
			if (parsed == null || parsed.wildcard())
				continue;
			final var fqn = parsed.fqn();
			final var dotIdx = fqn.lastIndexOf('.');
			if (dotIdx < 0)
				continue;
			imports.put(line.strip(), fqn.substring(dotIdx + 1));
		}
		if (imports.isEmpty())
			return Map.of();
		final var bodyLines = collectBodyLines(lines);
		final var result = new LinkedHashMap<String, String>();
		for (var entry : imports.entrySet()) {
			if (JavaSourceScanner.containsIdentifier(bodyLines, entry.getValue()))
				result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/**
	 * Computes the dry-run hint string. Clamps {@code dryRunTotal} up to
	 * {@code fixable} so the denominator is never less than the numerator
	 * (a stale {@code dryRunTotal} from an earlier {@code check} run can
	 * legitimately be lower than the current fix count).
	 */
	@CheckReturnValue
	@Nullable
	@VisibleForTesting
	static String computeHint(int fixable, int dryRunTotal, @Nonnull String taskName) {
		return formatHintMessage(fixable, Math.max(dryRunTotal, fixable), taskName);
	}

	@CheckReturnValue
	@Nonnull
	@VisibleForTesting
	static DefaultConfiguration createCheckerConfig(@Nonnull String minSdk) {
		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", String.valueOf(LineLength.TAB_WIDTH));
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

	/**
	 * @return {needsSecondPass ? 1 : 0, totalFixed}
	 */
	@VisibleForTesting
	static int[] doExecute(
			@Nonnull DefaultConfiguration checkerConfig,
			boolean dryRun,
			@Nonnull List<File> files
	) throws CheckstyleException, IOException {
		final var checker = new Checker();
		final var listener = new ViolationsByFileListener();
		try {
			checker.setModuleClassLoader(NoArrayTrailingCommaCheck.class.getClassLoader());
			checker.configure(checkerConfig);
			checker.addListener(listener);
			checker.process(files);
		}
		finally {
			checker.destroy();
		}

		var needsSecondPass = false;
		var filesFixed = 0;
		var totalFixed = 0;
		var totalSkipped = 0;
		final var allSkippedReasons = new LinkedHashMap<String, List<String>>();

		for (var entry : listener.getViolationsByFile().entrySet()) {
			final var filePath = Path.of(entry.getKey());
			final var violations = entry.getValue();
			final var totalViolations = violations.size();

			final List<String> sourceLines;
			try {
				sourceLines = readSourceLines(filePath);
			}
			// one unreadable file (not UTF-8, permissions, a dangling symlink) must not
			// abandon the run and leave the files already rewritten without a summary
			catch (IOException e) {
				System.err.println("Skipping " + filePath + ": " + e);
				continue;
			}
			final var lines = new ArrayList<>(sourceLines);
			final var result = applyFixes(lines, violations, FIXERS, MODULE_ID_FIXERS);

			if (result.fixCount() > 0) {
				if (!dryRun) {
					try {
						Files.writeString(filePath, String.join("\n", lines));
					}
					catch (IOException e) {
						System.err.println("Could not write " + filePath + ": " + e);
						continue;
					}
				}
				++filesFixed;
				totalFixed += result.fixCount();
			}
			if (result.needsSecondPass())
				needsSecondPass = true;
			totalSkipped += totalViolations - result.fixCount();
			for (var skipEntry : result.skippedReasons().entrySet())
				allSkippedReasons.computeIfAbsent(skipEntry.getKey(), k -> new ArrayList<>()).addAll(skipEntry.getValue());
		}

		if (!dryRun && (totalFixed > 0 || totalSkipped > 0)) {
			System.out.println("Fixed " + totalFixed + " violations in " + filesFixed + " files (" + totalSkipped + " skipped)");
			printSkipSummary(allSkippedReasons);
		}
		return new int[]{needsSecondPass ? 1 : 0, totalFixed};
	}

	@CheckReturnValue
	@Nonnull
	private static String extractCheckShortName(@Nonnull AuditEvent event) {
		final var moduleId = event.getModuleId();
		if (moduleId != null && !moduleId.isEmpty())
			return moduleId;
		final var source = event.getSourceName();
		if (source != null && !source.isEmpty()) {
			final var dot = source.lastIndexOf('.');
			return dot >= 0 ? source.substring(dot + 1) : source;
		}
		return "unknown";
	}

	/**
	 * Returns the set of check source names and module IDs that have fixers.
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
	 */
	@CheckReturnValue
	@Nonnull
	public static String fixerAllowedMethods() {
		return ALLOWED_METHODS;
	}

	@CheckReturnValue
	@Nullable
	@VisibleForTesting
	static String formatHintMessage(int fixable, int total, @Nonnull String taskName) {
		if (fixable <= 0)
			return null;
		if (fixable == total)
			return "Run ./gradlew " + taskName + " to auto-fix all " + fixable + " violations.";
		return "Run ./gradlew " + taskName + " to auto-fix " + fixable + " of " + total + " violations.";
	}

	/**
	 * Inserts missing import statements into the file lines in sorted position.
	 * Returns the number of imports added.
	 */
	static int insertMissingImports(@Nonnull List<String> lines, @Nonnull Set<String> fqns) {
		final var regularToAdd = new TreeSet<String>();
		final var staticToAdd = new TreeSet<String>();
		for (var fqn : fqns) {
			if (fqn.startsWith("static "))
				staticToAdd.add(fqn.substring(7));
			else
				regularToAdd.add(fqn);
		}

		// A line inside a text block or block comment can textually begin with
		// `import `/`package ` (e.g. a text block whose content is Java source, or a
		// commented-out import group). Treating it as a real directive would insert the
		// new import inside that literal. `masked` marks lines inside such a literal and
		// is kept in sync with every insertion below (an added import/blank line is never
		// masked), so every directive scan can skip masked lines even after mutation.
		final var masks = FqnResolver.computeLineMasks(lines);
		final var masked = new ArrayList<Boolean>(lines.size());
		for (var i = 0; i < lines.size(); ++i)
			masked.add(masks.inBlockComment()[i] || masks.inTextBlock()[i]);
		var lastImportIdx = -1;
		var lastStaticImportIdx = -1;
		var packageIdx = -1;
		for (var i = 0; i < lines.size(); ++i) {
			if (masked.get(i))
				continue;
			// leading whitespace (and a BOM on line 0) is legal ahead of `package`/`import`;
			// matching the raw line missed those and dropped the new import at index 0,
			// above the package declaration
			final var line = lines.get(i).strip();
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

		if (!staticToAdd.isEmpty()) {
			if (lastStaticImportIdx >= 0) {
				for (var fqn : staticToAdd) {
					final var importLine = "import static " + fqn + ";";
					var insertIdx = lastStaticImportIdx + 1;
					for (var i = 0; i <= lastStaticImportIdx; ++i) {
						if (!masked.get(i) && lines.get(i).strip().startsWith("import static ") && lines.get(i).strip().compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
					insertIdx = skipMaskedRun(masked, insertIdx);
					lines.add(insertIdx, importLine);
					masked.add(insertIdx, false);
					lastStaticImportIdx = Math.max(lastStaticImportIdx + 1, insertIdx);
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
					++addedStatic;
				}
			}
			else {
				var insertIdx = packageIdx + 1;
				if (packageIdx >= 0) {
					insertIdx = skipMaskedRun(masked, insertIdx);
					if (insertIdx >= lines.size() || !lines.get(insertIdx).isEmpty()) {
						lines.add(insertIdx, "");
						masked.add(insertIdx, false);
						if (lastImportIdx >= insertIdx)
							++lastImportIdx;
					}
					++insertIdx;
				}
				for (var fqn : staticToAdd) {
					lines.add(insertIdx, "import static " + fqn + ";");
					masked.add(insertIdx, false);
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
					++insertIdx;
					++addedStatic;
				}
				if (insertIdx >= lines.size() || !lines.get(insertIdx).isEmpty()) {
					lines.add(insertIdx, "");
					masked.add(insertIdx, false);
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
				}
			}
		}

		// if statics exist but no regular imports yet, position lastImportIdx
		// after the static group (with blank separator) so regulars go below statics
		if (!regularToAdd.isEmpty() && lastImportIdx < 0) {
			for (var i = lines.size() - 1; i >= 0; --i) {
				if (!masked.get(i) && lines.get(i).strip().startsWith("import static ")) {
					final var after = skipMaskedRun(masked, i + 1);
					if (after >= lines.size() || !lines.get(after).isEmpty()) {
						lines.add(after, "");
						masked.add(after, false);
					}
					lastImportIdx = after;
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

				var groupStart = -1;
				var groupEnd = -1;
				for (var i = 0; i <= lastImportIdx; ++i) {
					if (!masked.get(i) && lines.get(i).startsWith(targetPrefix)) {
						if (groupStart < 0)
							groupStart = i;
						groupEnd = i;
					}
				}

				int insertIdx;
				if (groupStart >= 0) {
					insertIdx = groupEnd + 1;
					for (var i = groupStart; i <= groupEnd; ++i) {
						if (!masked.get(i) && lines.get(i).compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}
				else {
					insertIdx = lastImportIdx + 1;
					for (var i = 0; i <= lastImportIdx; ++i) {
						final var line = lines.get(i);
						if (!masked.get(i) && line.startsWith("import ") && !line.startsWith("import static ")
								&& line.compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}

				insertIdx = skipMaskedRun(masked, insertIdx);
				lines.add(insertIdx, importLine);
				masked.add(insertIdx, false);
				lastImportIdx = Math.max(lastImportIdx + 1, insertIdx);
			}
		}
		else if (packageIdx >= 0) {
			var insertIdx = skipMaskedRun(masked, packageIdx + 1);
			if (insertIdx < lines.size() && lines.get(insertIdx).isEmpty())
				++insertIdx;
			else {
				lines.add(insertIdx, "");
				masked.add(insertIdx, false);
				++insertIdx;
			}
			for (var fqn : regularToAdd) {
				lines.add(insertIdx, "import " + fqn + ";");
				masked.add(insertIdx, false);
				++insertIdx;
			}
		}
		else {
			var insertIdx = 0;
			for (var fqn : regularToAdd) {
				lines.add(insertIdx, "import " + fqn + ";");
				masked.add(insertIdx, false);
				++insertIdx;
			}
		}

		return addedStatic + regularToAdd.size();
	}

	private static void printSkipSummary(@Nonnull Map<String, List<String>> allSkippedReasons) {
		if (allSkippedReasons.isEmpty())
			return;

		record ReasonCount(@Nonnull String check, @Nonnull String reason, int count) {}

		final var entries = new ArrayList<ReasonCount>();
		for (var entry : allSkippedReasons.entrySet()) {
			final var reasonCounts = new LinkedHashMap<String, Integer>();
			for (var reason : entry.getValue())
				reasonCounts.merge(reason, 1, Integer::sum);
			for (var rc : reasonCounts.entrySet())
				entries.add(new ReasonCount(entry.getKey(), rc.getKey(), rc.getValue()));
		}

		entries.sort(Comparator.comparingInt(ReasonCount::count).reversed()
				.thenComparing(ReasonCount::check));

		System.out.println("Skipped violations:");
		for (var entry : entries)
			System.out.println("  " + entry.check() + ": " + entry.reason() + " (x" + entry.count() + ")");
	}

	/**
	 * Reads a source file into a mutable line list that faithfully models the
	 * file's line structure, preserving a final empty line when the file ends
	 * with a line terminator. {@link Files#readAllLines} drops that terminator,
	 * which hides an end-of-file newline from line-based fixers; preserving it
	 * lets {@link TrailingNewlineFixer} treat it as an ordinary trailing blank
	 * line and lets {@code String.join("\n", lines)} round-trip LF-terminated
	 * content (CR and CRLF terminators are normalized to LF on write).
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> readSourceLines(@Nonnull Path path) throws IOException {
		return splitPreservingTrailingNewline(Files.readString(path));
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
	 * Advances {@code idx} past a contiguous run of masked lines (inside a block
	 * comment or text block). Used so a new import chosen to sit right after an
	 * anchor directive that itself opens an unterminated comment lands after the
	 * comment closes rather than inside it.
	 */
	@CheckReturnValue
	private static int skipMaskedRun(@Nonnull List<Boolean> masked, int idx) {
		while (idx < masked.size() && masked.get(idx))
			++idx;
		return idx;
	}

	/**
	 * Splits {@code content} into lines like {@link Files#readAllLines} (handles
	 * {@code \n}, {@code \r\n}, and {@code \r}; no terminators in the result),
	 * but appends a trailing empty line when {@code content} ends with a line
	 * terminator so an end-of-file newline is represented. Returns a mutable list.
	 */
	@CheckReturnValue
	@Nonnull
	static List<String> splitPreservingTrailingNewline(@Nonnull String content) {
		final var lines = new ArrayList<>(content.lines().toList());
		if (content.endsWith("\n") || content.endsWith("\r"))
			lines.add("");
		return lines;
	}

	/**
	 * Converts a tab-expanded column (as reported by Checkstyle) back to the
	 * code-point column the AST node carries; Checkstyle expands TABs to
	 * {@link LineLength#TAB_WIDTH} when calculating column numbers. The result is
	 * not a char index: it may be compared against an AST position, but must be
	 * converted before it indexes a line.
	 */
	@CheckReturnValue
	static int tabColumnToCharIndex(@Nonnull String line, int tabExpandedCol) {
		var visualCol = 0;
		for (var i = 0; i < line.length(); ++i) {
			if (visualCol >= tabExpandedCol)
				return i;
			if (line.charAt(i) == '\t')
				visualCol += LineLength.TAB_WIDTH - (visualCol % LineLength.TAB_WIDTH);
			else
				++visualCol;
		}
		return line.length();
	}

	private static void trackSkip(
			@Nonnull Map<String, List<String>> skippedReasons,
			@Nonnull String checkName,
			@Nonnull String reason
	) {
		skippedReasons.computeIfAbsent(checkName, k -> new ArrayList<>()).add(reason);
	}

	@Override
	public void execute() {
		try {
			final var params = getParameters();
			final var checkerConfig = createCheckerConfig(params.getMinSdk().get());
			final var dryRun = params.getDryRun().getOrElse(false).booleanValue();

			final var files = new ArrayList<File>();
			collectJavaFiles(params.getSource().get().getAsFile().toPath(), files);
			if (params.getTestSource().isPresent())
				collectJavaFiles(params.getTestSource().get().getAsFile().toPath(), files);

			final var result = doExecute(checkerConfig, dryRun, files);
			if (dryRun) {
				final var fixable = result[1];
				final var hint = computeHint(
						fixable,
						params.getDryRunTotal().getOrElse(fixable),
						params.getDryRunTaskName().getOrElse("checkstyleFix")
				);
				if (hint != null)
					System.out.println(hint);
			}
			// Pass 2: clean up cascading violations
			// (e.g. imports that became unused after a fixer replaced their usage).
			else if (result[0] != 0)
				doExecute(checkerConfig, false, files);
		}
		catch (CheckstyleException | IOException e) {
			throw new RuntimeException("Checkstyle fix failed", e);
		}
	}
}