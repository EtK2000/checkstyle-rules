package com.etk2000.checkstyle.gradle.fix;

import com.etk2000.checkstyle.AnnotationOwnLineCheck;
import com.etk2000.checkstyle.AnnotationSameLineCheck;
import com.etk2000.checkstyle.ArrayTypeStyleCheck;
import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck;
import com.etk2000.checkstyle.ControlFlowBracesCheck;
import com.etk2000.checkstyle.EmptyBodyCheck;
import com.etk2000.checkstyle.FieldConsolidationCheck;
import com.etk2000.checkstyle.FieldSortingCheck;
import com.etk2000.checkstyle.JavaLineScanner;
import com.etk2000.checkstyle.JitInefficiencyCheck;
import com.etk2000.checkstyle.LambdaParameterTypeCheck;
import com.etk2000.checkstyle.LineText;
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
import com.etk2000.checkstyle.gradle.FixableCheckNames;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.PackageObjectFactory;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import com.puppycrawl.tools.checkstyle.api.Configurable;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.Filter;
import com.puppycrawl.tools.checkstyle.api.FilterSet;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.api.Violation;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CheckstyleFixAction implements WorkAction<CheckstyleFixAction.Params> {
	record ApplyFixesResult(
			int fixCount,
			boolean needsAnotherPass,
			@Nonnull Map<String, List<String>> skippedReasons
	) {}

	interface Params extends WorkParameters {
		DirectoryProperty getConfigDirectory();

		Property<Boolean> getDryRun();

		Property<String> getDryRunTaskName();

		Property<Integer> getDryRunTotal();

		Property<String> getMinSdk();

		DirectoryProperty getSource();

		DirectoryProperty getTestSource();
	}

	private static final char BYTE_ORDER_MARK = 0xFEFF;
	// passes a fix run makes, counting the last one that finds nothing left to fix. Every
	// integration case and every topic's cases.in.java settles within 5; one spare pass stops a
	// file that never settles without cutting a slow one short
	static final int MAX_FIX_PASSES = 6;
	private static final String ALLOWED_METHODS = "findViewById,findViewWithTag,getArgument,getSystemService,requireViewById";
	private static final String BLANK_LINE_AFTER_BREAK_ID = "BlankLineAfterBreak";
	private static final String BLANK_LINE_AFTER_CLASS_BRACE_ID = "NoBlankLineAfterClassBrace";
	private static final String BLANK_LINE_BEFORE_CLOSING_BRACE_ID = "NoBlankLineBeforeClosingBrace";
	private static final String DOUBLE_BLANK_LINES_ID = "NoDoubleBlankLines";
	private static final String SUPPRESSIONS_FILE = "suppressions.xml";
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
				Map.entry(EmptyBodyCheck.class.getName(), new EmptyBodyFixer()),
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
		var deferred = false;
		var fixed = 0;
		var staleFrom = 0;
		var staleLine = -1;
		var suppressedLine = -1;
		var passedThrough = false;
		String openCheck = null;
		// the original lines each applied fix rewrote, from its start down to the line above its
		// own event's: a later event there points into text that is no longer what it reported.
		// Events run bottom-up and a fix only shifts lines from its own start on, so a line above
		// every rewritten span still means what it did. The fix's own line (staleLine) has changed
		// once a fix reaches it, and its later events (in falling column order) are put off: another
		// check's event may hang on text past its column (the initializer a cast removal changed),
		// and a rewrite spanning lines may land a different declaration there. Only while every fix
		// on the line was the same check (openCheck) editing just that line in place does that
		// check's next event stay open, if it points ahead of staleFrom, the first char any of
		// those fixes changed: a check's events on one line are separate constructs
		final var rewritten = new TreeMap<Integer, Integer>();
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
					// after a prior delete, a blank line may shift into this position
					// (RedundantImport + UnusedImports double-fire: the first removes the
					// import, the second the leftover blank)
					if (!passedThrough && lineIndex >= 0 && lineIndex < lines.size()
							&& lines.get(lineIndex).isEmpty()
							&& fixer instanceof LineDeleter)
						passedThrough = true;
					else {
						deferred = true;
						trackSkip(skippedReasons, checkName, SkipMessages.FIX_SUPPRESSED);
						continue;
					}
				}
				else
					passedThrough = false;
				if (staleLine >= 0 && lineIndex == staleLine
						&& (!checkName.equals(openCheck) || eventCharIndex(lines.get(lineIndex), event) >= staleFrom)
						|| insideAny(rewritten, lineIndex)) {
					deferred = true;
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_SUPPRESSED);
					continue;
				}
				// whether an import is unused was judged on the text before this pass, and an event
				// put off to the next one may be what starts using it (PreferImport stripping a
				// qualified name back to the import's simple name)
				if (deferred && fixer instanceof UnusedImportsFixer) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_SUPPRESSED);
					continue;
				}
				if (lineIndex < 0 || lineIndex >= lines.size()) {
					trackSkip(skippedReasons, checkName, SkipMessages.FIX_BOUNDS);
					continue;
				}
				final var charColumn = eventCharIndex(lines.get(lineIndex), event);
				final var eventLine = lines.get(lineIndex);
				FixContext.setViolation(event.getViolation());
				// every applied fix edits lines in place and is counted in fixed, so the count
				// identifies an unchanged buffer
				FixContext.setBufferVersion(fixed);
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
				final var rewrittenEnd = Math.min(result.endLine(), lineIndex - 1);
				if (rewrittenEnd >= result.startLine())
					rewritten.merge(result.startLine(), rewrittenEnd, Math::max);
				// suppress the next violation whose lineIndex falls past the replacement range
				// after an actual deletion, since that line belongs to content that shifted up
				suppressedLine = result.endLine() >= result.startLine()
						&& lineIndex >= result.startLine() + result.replacement().size()
						? lineIndex : -1;
				if (suppressedLine == lineIndex)
					staleLine = -1;
				else if (result.startLine() <= lineIndex) {
					final var inPlace = result.startLine() == lineIndex && result.endLine() == lineIndex && result.replacement().size() == 1;
					// a later fix on the line only ever comes from openCheck at a column ahead of
					// staleFrom, and the events after it are further left still, so its own change
					// point is the only one they can reach
					staleFrom = inPlace ? commonPrefixLength(eventLine, result.replacement().getFirst()) : 0;
					if (staleLine != lineIndex) {
						staleLine = lineIndex;
						openCheck = inPlace ? checkName : null;
					}
					else if (!inPlace)
						openCheck = null;
				}
				++fixed;
			}
		}
		finally {
			FieldConsolidationFixer.clearCache();
			FieldRewriteVerifier.clearCache();
			FixerAst.clearCache();
			FixContext.clearBufferVersion();
			FixContext.clearFilePath();
			FixContext.clearViolation();
		}

		var needsAnotherPass = deferred;
		if (!importsToAdd.isEmpty() && insertMissingImports(lines, importsToAdd) > 0)
			needsAnotherPass = true;
		if (!needsAnotherPass && fixed > 0 && !preFixUsedImports.isEmpty()) {
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
					needsAnotherPass = true;
					break;
				}
			}
		}

		final var immutableReasons = new LinkedHashMap<String, List<String>>();
		for (var entry : skippedReasons.entrySet())
			immutableReasons.put(entry.getKey(), List.copyOf(entry.getValue()));
		return new ApplyFixesResult(fixed, needsAnotherPass, Map.copyOf(immutableReasons));
	}

	/**
	 * Whether a line spliced in after {@code idx} lands outside every comment and text block. The walk
	 * passes the lines a multi-line comment or text block covers entirely, which carry no code and so
	 * are never an insertion point anyway, then asks whether the first line that does carry code still
	 * begins inside one. When it does, the directive on {@code idx} opened a comment that the next
	 * code line closes and there is no whole line between the two: splicing before that line writes the
	 * import into the comment, and splicing after it writes it past the type declaration.
	 */
	@CheckReturnValue
	private static boolean canAnchorAfter(
			@Nonnull List<String> maskedLines, @Nonnull List<Boolean> masked, int idx
	) {
		var next = idx + 1;
		while (next < maskedLines.size() && masked.get(next) && maskedLines.get(next).isBlank())
			++next;
		return next >= masked.size() || !masked.get(next);
	}

	/**
	 * The filters {@code checkerConfig} hangs on the Checker itself (a {@code SuppressionFilter}),
	 * configured as the Checker configures them, so a fixer whose rewrite answers to another check
	 * can ask whether they drop that check's events where it would act.
	 */
	@CheckReturnValue
	@Nonnull
	static FilterSet checkerFilters(@Nonnull DefaultConfiguration checkerConfig) throws CheckstyleException {
		final var factory = new PackageObjectFactory(Checker.class.getPackage().getName(), NoArrayTrailingCommaCheck.class.getClassLoader());
		final var filters = new FilterSet();
		for (var child : checkerConfig.getChildren()) {
			if (factory.createModule(child.getName()) instanceof Filter filter) {
				if (filter instanceof Configurable configurable)
					configurable.configure(child);
				filters.addFilter(filter);
			}
		}
		return filters;
	}

	@CheckReturnValue
	@Nonnull
	private static List<String> collectBodyLines(@Nonnull List<String> lines) {
		final var bodyLines = new ArrayList<String>();
		for (var line : lines) {
			final var trimmed = line.strip();
			if (trimmed.startsWith("import ") || trimmed.startsWith("package "))
				continue;
			bodyLines.add(line);
		}
		return bodyLines;
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
		final var usedImports = new LinkedHashMap<String, String>();
		for (var entry : imports.entrySet()) {
			if (JavaSourceScanner.containsIdentifier(bodyLines, entry.getValue()))
				usedImports.put(entry.getKey(), entry.getValue());
		}
		return usedImports;
	}

	@CheckReturnValue
	private static int commonPrefixLength(@Nonnull String a, @Nonnull String b) {
		final var mismatch = Arrays.mismatch(a.toCharArray(), b.toCharArray());
		return mismatch < 0 ? a.length() : mismatch;
	}

	/**
	 * Clamps {@code dryRunTotal} up to
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
		return createCheckerConfig(minSdk, null);
	}

	/**
	 * The Checker the fix pipeline runs: every fixable check, with the user's
	 * {@code suppressions.xml} from {@code configDirectory} applied when there is one, as the
	 * bundled config applies it, so a file {@code checkstyle} reports clean is never rewritten.
	 */
	@CheckReturnValue
	@Nonnull
	@VisibleForTesting
	static DefaultConfiguration createCheckerConfig(@Nonnull String minSdk, @Nullable File configDirectory) {
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
		// the fixers read and write UTF-8, so the reported columns must come from the same decoding
		checkerConfig.addProperty("charset", StandardCharsets.UTF_8.name());
		// the Checker-level regex modules report columns too, at the Checker's own tab width
		checkerConfig.addProperty("tabWidth", String.valueOf(LineLength.TAB_WIDTH));
		if (configDirectory != null) {
			final var suppressionConfig = new DefaultConfiguration("SuppressionFilter");
			suppressionConfig.addProperty("file", new File(configDirectory, SUPPRESSIONS_FILE).getPath());
			suppressionConfig.addProperty("optional", "true");
			checkerConfig.addChild(suppressionConfig);
		}
		checkerConfig.addChild(treeWalkerConfig);

		final var blankAfterBreakConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterBreakConfig.addProperty("id", BLANK_LINE_AFTER_BREAK_ID);
		blankAfterBreakConfig.addProperty("format", "break\\s*;\\n[^\\S\\n]*(case |default[\\s:])");
		blankAfterBreakConfig.addProperty("message", FixableCheckNames.BLANK_LINE_AFTER_BREAK_MESSAGE);
		checkerConfig.addChild(blankAfterBreakConfig);

		final var blankAfterClassBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankAfterClassBraceConfig.addProperty("id", BLANK_LINE_AFTER_CLASS_BRACE_ID);
		blankAfterClassBraceConfig.addProperty("format", "(class|interface|enum|record)\\s+\\w[^{]*\\{\\s*\\n\\s*\\n");
		blankAfterClassBraceConfig.addProperty("message", FixableCheckNames.BLANK_LINE_AFTER_CLASS_BRACE_MESSAGE);
		checkerConfig.addChild(blankAfterClassBraceConfig);

		final var blankBeforeCloseBraceConfig = new DefaultConfiguration("RegexpMultiline");
		blankBeforeCloseBraceConfig.addProperty("id", BLANK_LINE_BEFORE_CLOSING_BRACE_ID);
		blankBeforeCloseBraceConfig.addProperty("format", "\\n[^\\S\\n]*\\n[^\\S\\n]*\\}");
		blankBeforeCloseBraceConfig.addProperty("message", FixableCheckNames.BLANK_LINE_BEFORE_CLOSING_BRACE_MESSAGE);
		checkerConfig.addChild(blankBeforeCloseBraceConfig);

		final var doubleBlankConfig = new DefaultConfiguration("RegexpMultiline");
		doubleBlankConfig.addProperty("id", DOUBLE_BLANK_LINES_ID);
		doubleBlankConfig.addProperty("format", "\\n\\s*\\n\\s*\\n");
		doubleBlankConfig.addProperty("message", FixableCheckNames.DOUBLE_BLANK_LINES_MESSAGE);
		checkerConfig.addChild(doubleBlankConfig);

		final var trailingNewlineConfig = new DefaultConfiguration("RegexpMultiline");
		trailingNewlineConfig.addProperty("id", TRAILING_NEWLINE_ID);
		trailingNewlineConfig.addProperty("format", "\\n\\z");
		trailingNewlineConfig.addProperty("message", FixableCheckNames.TRAILING_NEWLINE_MESSAGE);
		checkerConfig.addChild(trailingNewlineConfig);

		final var trailingWsConfig = new DefaultConfiguration("RegexpSingleline");
		trailingWsConfig.addProperty("id", TRAILING_WHITESPACE_ID);
		trailingWsConfig.addProperty("format", "[ \\t]+$");
		trailingWsConfig.addProperty("message", FixableCheckNames.TRAILING_WHITESPACE_MESSAGE);
		checkerConfig.addChild(trailingWsConfig);

		return checkerConfig;
	}

	/**
	 * @return {needsAnotherPass ? 1 : 0, totalFixed}
	 */
	@VisibleForTesting
	static int[] doExecute(
			@Nonnull DefaultConfiguration checkerConfig,
			boolean dryRun,
			@Nonnull List<File> files
	) throws CheckstyleException, IOException {
		return doExecute(checkerConfig, dryRun, files, new ArrayList<>());
	}

	/**
	 * Adds to {@code changed} every file this pass rewrote.
	 *
	 * @return {needsAnotherPass ? 1 : 0, totalFixed}
	 */
	private static int[] doExecute(
			@Nonnull DefaultConfiguration checkerConfig,
			boolean dryRun,
			@Nonnull List<File> files,
			@Nonnull List<File> changed
	) throws CheckstyleException, IOException {
		return doExecute(checkerConfig, dryRun, files, changed, FIXERS, MODULE_ID_FIXERS);
	}

	/**
	 * {@link #doExecute(DefaultConfiguration, boolean, List, List)} with the given fixers. A file
	 * the pass's fixes would leave unparseable, though it parsed before, is not written: it is named
	 * on stderr with the first parse error and counts as unchanged, since the next pass could not
	 * parse it and the run would stop with it already rewritten.
	 *
	 * @return {needsAnotherPass ? 1 : 0, totalFixed}
	 */
	@VisibleForTesting
	static int[] doExecute(
			@Nonnull DefaultConfiguration checkerConfig,
			boolean dryRun,
			@Nonnull List<File> files,
			@Nonnull List<File> changed,
			@Nonnull Map<String, CheckstyleFixer> fixers,
			@Nonnull Map<String, CheckstyleFixer> moduleIdFixers
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
		final var filters = checkerFilters(checkerConfig);

		var needsAnotherPass = false;
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
			final var fileName = entry.getKey();
			FixContext.setSuppressed((check, line) ->
					!filters.accept(new AuditEvent(checker, fileName, new Violation(line, 0, null, "", null, SeverityLevel.ERROR, null, check, "")))
			);
			final ApplyFixesResult result;
			try {
				result = applyFixes(lines, violations, fixers, moduleIdFixers);
			}
			finally {
				FixContext.clearSuppressed();
			}

			if (result.fixCount() > 0) {
				final var parseError = parseBreak(sourceLines, lines);
				if (parseError != null) {
					System.err.println("Not writing " + filePath + ": its fixes would leave it unparseable (" + parseError + ")");
					continue;
				}
				if (!dryRun) {
					try {
						writeAtomically(filePath, String.join("\n", lines));
					}
					catch (IOException e) {
						System.err.println("Could not write " + filePath + ": " + e);
						continue;
					}
				}
				changed.add(filePath.toFile());
				++filesFixed;
				totalFixed += result.fixCount();
			}
			if (result.needsAnotherPass())
				needsAnotherPass = true;
			totalSkipped += totalViolations - result.fixCount();
			for (var skipEntry : result.skippedReasons().entrySet())
				allSkippedReasons.computeIfAbsent(skipEntry.getKey(), k -> new ArrayList<>()).addAll(skipEntry.getValue());
		}

		if (!dryRun && (totalFixed > 0 || totalSkipped > 0)) {
			System.out.println("Fixed " + totalFixed + " violations in " + filesFixed + " files (" + totalSkipped + " skipped)");
			printSkipSummary(allSkippedReasons);
		}
		return new int[]{needsAnotherPass ? 1 : 0, totalFixed};
	}

	/**
	 * The index into {@code line}'s chars of the column {@code event} reports, which checkstyle
	 * counts in code points with tabs expanded.
	 */
	@CheckReturnValue
	static int eventCharIndex(@Nonnull String line, @Nonnull AuditEvent event) {
		final var codePointColumn = tabColumnToCodePointColumn(line, event.getColumn() - 1);
		return LineText.charIndexOfColumn(line, Math.clamp(codePointColumn, 0, line.codePointCount(0, line.length())));
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

	/**
	 * Fixes {@code files} pass after pass, re-checking only the files the last pass rewrote, since a
	 * fix can leave a violation only a fresh check run reports. Returns how many passes ran.
	 */
	@VisibleForTesting
	static int fixUntilSettled(
			@Nonnull DefaultConfiguration checkerConfig,
			@Nonnull List<File> files,
			int maxPasses
	) throws CheckstyleException, IOException {
		var passes = 0;
		var pending = files;
		do {
			final var changed = new ArrayList<File>();
			doExecute(checkerConfig, false, pending, changed);
			pending = changed;
			++passes;
		}
		while (!pending.isEmpty() && passes < maxPasses);
		if (!pending.isEmpty())
			System.err.println("Stopped after " + passes + " fix passes with files still changing, run the task again to finish them: " + pending);
		return passes;
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
		// a byte-order mark belongs to the file rather than to whichever line is first, and String.strip
		// does not treat it as whitespace. Carried through the scans it hides a `package` line; left in
		// place it rides on a line an insertion at index 0 pushes down, putting the mark in the middle of
		// the file
		final var byteOrderMark = !lines.isEmpty() && !lines.getFirst().isEmpty()
				&& lines.getFirst().charAt(0) == BYTE_ORDER_MARK;
		if (byteOrderMark)
			lines.set(0, lines.getFirst().substring(1));
		final var added = insertMissingImportsInto(lines, fqns);
		if (byteOrderMark)
			lines.set(0, BYTE_ORDER_MARK + lines.getFirst());
		return added;
	}

	/** {@link #insertMissingImports} with any byte-order mark already held aside. */
	private static int insertMissingImportsInto(@Nonnull List<String> lines, @Nonnull Set<String> fqns) {
		final var regularToAdd = new TreeSet<String>();
		final var staticToAdd = new TreeSet<String>();
		for (var fqn : fqns) {
			if (fqn.startsWith("static "))
				staticToAdd.add(fqn.substring(7));
			else
				regularToAdd.add(fqn);
		}

		// `masked` marks the lines that BEGIN inside a block comment or text block, which is what
		// decides where a new line may be spliced. `maskedLines` is the same source with every comment
		// and literal blanked, which is what decides whether a line carries a directive at all: a line
		// a multi-line literal covers blanks to nothing and falls out of the scans below on its own,
		// while `*/ import a.B;` keeps the import it really declares. Both views are spliced alongside
		// `lines` so all three stay addressable by the same index after an insertion.
		final var masks = FqnResolver.computeLineMasks(lines);
		final var masked = new ArrayList<Boolean>(lines.size());
		for (var i = 0; i < lines.size(); ++i)
			masked.add(masks.inBlockComment()[i] || masks.inTextBlock()[i]);
		final var maskedLines = JavaLineScanner.maskAll(lines);

		var lastImportIdx = -1;
		var lastStaticImportIdx = -1;
		var packageIdx = -1;
		for (var i = 0; i < lines.size(); ++i) {
			// a `package` line written with a tab would otherwise leave packageIdx at -1 and send the
			// whole import block to the top of the file, above the package statement, where it does not
			// parse
			final var line = maskedLines.get(i).strip();
			if (LineText.startsWithSeparatedWord(line, "package")) {
				packageIdx = i;
				continue;
			}
			if (!LineText.startsWithSeparatedWord(line, "import"))
				continue;

			final var declared = ImportLine.parse(line);
			final var isStatic = declared == null ? line.startsWith("import static ") : declared.staticImport();
			removeImported(isStatic ? staticToAdd : regularToAdd, declared);
			// an import wrapped across lines ends no physical line of its own with the `;`, so no line
			// of it may anchor: splicing after its first would land the new import between its halves
			// and the file would stop parsing
			if (!line.endsWith(";") || !canAnchorAfter(maskedLines, masked, i))
				continue;
			if (isStatic)
				lastStaticImportIdx = i;
			else
				lastImportIdx = i;
		}

		var addedStatic = 0;

		if (!staticToAdd.isEmpty()) {
			if (lastStaticImportIdx >= 0) {
				for (var fqn : staticToAdd) {
					final var importLine = "import static " + fqn + ";";
					var insertIdx = lastStaticImportIdx + 1;
					for (var i = 0; i <= lastStaticImportIdx; ++i) {
						final var existing = maskedLines.get(i).strip();
						if (existing.startsWith("import static ") && existing.compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
					insertIdx = skipMaskedRun(masked, insertIdx);
					spliceLine(lines, maskedLines, masked, insertIdx, importLine);
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
						spliceLine(lines, maskedLines, masked, insertIdx, "");
						if (lastImportIdx >= insertIdx)
							++lastImportIdx;
					}
					++insertIdx;
				}
				for (var fqn : staticToAdd) {
					spliceLine(lines, maskedLines, masked, insertIdx, "import static " + fqn + ";");
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
					++insertIdx;
					++addedStatic;
				}
				if (insertIdx >= lines.size() || !lines.get(insertIdx).isEmpty()) {
					spliceLine(lines, maskedLines, masked, insertIdx, "");
					if (lastImportIdx >= insertIdx)
						++lastImportIdx;
				}
			}
		}

		if (!regularToAdd.isEmpty() && lastImportIdx < 0) {
			for (var i = lines.size() - 1; i >= 0; --i) {
				if (!maskedLines.get(i).strip().startsWith("import static ")
						|| !canAnchorAfter(maskedLines, masked, i))
					continue;
				final var after = skipMaskedRun(masked, i + 1);
				if (after >= lines.size() || !lines.get(after).isEmpty())
					spliceLine(lines, maskedLines, masked, after, "");
				lastImportIdx = after;
				break;
			}
		}

		if (regularToAdd.isEmpty())
			return addedStatic;

		if (lastImportIdx >= 0) {
			for (var fqn : regularToAdd) {
				final var importLine = "import " + fqn + ";";
				final var lastDot = fqn.lastIndexOf('.');
				// null rather than a bare "import ", which would match every import line including the
				// static ones and sort a default-package class into the static group
				final var targetPrefix = lastDot < 0 ? null : "import " + fqn.substring(0, lastDot + 1);

				var groupStart = -1;
				var groupEnd = -1;
				for (var i = 0; i <= lastImportIdx; ++i) {
					if (targetPrefix != null && maskedLines.get(i).strip().startsWith(targetPrefix)) {
						if (groupStart < 0)
							groupStart = i;
						groupEnd = i;
					}
				}

				int insertIdx;
				if (groupStart >= 0) {
					insertIdx = groupEnd + 1;
					for (var i = groupStart; i <= groupEnd; ++i) {
						if (maskedLines.get(i).strip().compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}
				else {
					insertIdx = lastImportIdx + 1;
					for (var i = 0; i <= lastImportIdx; ++i) {
						final var line = maskedLines.get(i).strip();
						if (line.startsWith("import ") && !line.startsWith("import static ")
								&& line.compareTo(importLine) > 0) {
							insertIdx = i;
							break;
						}
					}
				}

				insertIdx = skipMaskedRun(masked, insertIdx);
				spliceLine(lines, maskedLines, masked, insertIdx, importLine);
				lastImportIdx = Math.max(lastImportIdx + 1, insertIdx);
			}
		}
		else if (packageIdx >= 0) {
			var insertIdx = skipMaskedRun(masked, packageIdx + 1);
			if (insertIdx < lines.size() && lines.get(insertIdx).isEmpty())
				++insertIdx;
			else {
				spliceLine(lines, maskedLines, masked, insertIdx, "");
				++insertIdx;
			}
			for (var fqn : regularToAdd) {
				spliceLine(lines, maskedLines, masked, insertIdx, "import " + fqn + ";");
				++insertIdx;
			}
		}
		else {
			var insertIdx = 0;
			for (var fqn : regularToAdd) {
				spliceLine(lines, maskedLines, masked, insertIdx, "import " + fqn + ";");
				++insertIdx;
			}
		}

		return addedStatic + regularToAdd.size();
	}

	/** True when {@code line} falls in one of {@code spans}, each a first line mapped to its last. */
	@CheckReturnValue
	private static boolean insideAny(@Nonnull NavigableMap<Integer, Integer> spans, int line) {
		for (var end : spans.headMap(line, true).values()) {
			if (end >= line)
				return true;
		}
		return false;
	}

	/**
	 * The first parse error of {@code after} when {@code before} parses and {@code after} does not,
	 * else null.
	 */
	@CheckReturnValue
	@Nullable
	@VisibleForTesting
	static String parseBreak(@Nonnull List<String> before, @Nonnull List<String> after) {
		final var error = parseError(after);
		return error == null || parseError(before) != null ? null : error;
	}

	/**
	 * Null when {@code lines} parse, else the message of the innermost parse error that has one.
	 * A byte-order mark, which the parse refuses and which carries no code, is left out.
	 */
	@CheckReturnValue
	@Nullable
	private static String parseError(@Nonnull List<String> lines) {
		final var text = new ArrayList<>(lines);
		if (!text.isEmpty() && !text.getFirst().isEmpty() && text.getFirst().charAt(0) == BYTE_ORDER_MARK)
			text.set(0, text.getFirst().substring(1));
		try {
			JavaParser.parse(new FileContents(new FileText(new File("Fixed.java"), text)));
			return null;
		}
		catch (CheckstyleException e) {
			var message = e.getMessage();
			for (var cause = e.getCause(); cause != null; cause = cause.getCause()) {
				if (cause.getMessage() != null)
					message = cause.getMessage();
			}
			return message;
		}
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

	@CheckReturnValue
	@Nonnull
	static List<String> readSourceLines(@Nonnull Path path) throws IOException {
		return splitPreservingTrailingNewline(Files.readString(path));
	}

	/**
	 * Drops from {@code toAdd} every name {@code declared} already brings into scope, and nothing when
	 * it is null, which is what {@link ImportLine#parse} returns for a line carrying more than one
	 * statement or a token past the {@code ;}: keying off such a line would match text spanning both
	 * statements. A wildcard covers what the element it names declares itself, not what a nested class
	 * or a sub-package does, so {@code a.B.*} leaves {@code a.B.C.m} out of scope and matching on the
	 * prefix alone would drop an import the file still needs.
	 */
	private static void removeImported(@Nonnull Set<String> toAdd, @Nullable ImportLine declared) {
		if (declared == null)
			return;
		if (!declared.wildcard()) {
			toAdd.remove(declared.fqn());
			return;
		}
		toAdd.remove(declared.fqn() + ".*");
		final var prefix = declared.fqn() + '.';
		toAdd.removeIf(s -> s.startsWith(prefix) && s.indexOf('.', prefix.length()) < 0);
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
	 * Splices {@code line} into {@code lines} at {@code idx} and into the two mask views beside it, so
	 * all three stay addressable by the same index. The text is code the fix wrote, so it masks to
	 * itself and opens no comment or literal for the lines below it.
	 */
	private static void spliceLine(
			@Nonnull List<String> lines,
			@Nonnull List<String> maskedLines,
			@Nonnull List<Boolean> masked,
			int idx,
			@Nonnull String line
	) {
		lines.add(idx, line);
		maskedLines.add(idx, line);
		masked.add(idx, false);
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
	static int tabColumnToCodePointColumn(@Nonnull String line, int tabExpandedCol) {
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

	/**
	 * Replaces the file {@code path} leads to (through any symlinks) with {@code content} in one
	 * step: the text goes to a temporary file beside it, which takes over its POSIX permissions
	 * and is then moved over it, so a write that fails or is interrupted part way leaves the
	 * original untouched. A file its owner cannot write is refused, as a plain write would be. In a
	 * directory that takes no new files the file is written in place instead.
	 */
	@VisibleForTesting
	static void writeAtomically(@Nonnull Path path, @Nonnull String content) throws IOException {
		final var target = path.toRealPath();
		if (!Files.isWritable(target))
			throw new AccessDeniedException(target.toString());
		final Path temp;
		try {
			temp = Files.createTempFile(target.getParent(), "." + target.getFileName(), ".tmp");
		}
		catch (AccessDeniedException e) {
			Files.writeString(target, content);
			return;
		}
		try {
			Files.writeString(temp, content);
			if (target.getFileSystem().supportedFileAttributeViews().contains("posix"))
				Files.setPosixFilePermissions(temp, Files.getPosixFilePermissions(target));
			Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		}
		finally {
			Files.deleteIfExists(temp);
		}
	}

	@Override
	public void execute() {
		try {
			final var params = getParameters();
			final var checkerConfig = createCheckerConfig(
					params.getMinSdk().get(),
					params.getConfigDirectory().isPresent() ? params.getConfigDirectory().get().getAsFile() : null
			);
			final var dryRun = params.getDryRun().getOrElse(false).booleanValue();

			final var files = new ArrayList<File>();
			collectJavaFiles(params.getSource().get().getAsFile().toPath(), files);
			if (params.getTestSource().isPresent())
				collectJavaFiles(params.getTestSource().get().getAsFile().toPath(), files);

			if (dryRun) {
				final var fixable = doExecute(checkerConfig, true, files)[1];
				final var hint = computeHint(
						fixable,
						params.getDryRunTotal().getOrElse(fixable),
						params.getDryRunTaskName().getOrElse("checkstyleFix")
				);
				if (hint != null)
					System.out.println(hint);
			}
			else
				fixUntilSettled(checkerConfig, files, MAX_FIX_PASSES);
		}
		catch (CheckstyleException | IOException e) {
			throw new RuntimeException("Checkstyle fix failed", e);
		}
	}
}