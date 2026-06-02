package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static java.util.Objects.requireNonNull;

import com.etk2000.checkstyle.gradle.fix.LineLength;
import com.etk2000.checkstyle.gradle.fix.PropertiesUtil;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.function.Executable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BaseCheckTest {
	public record ExpectedViolation(
			int line,
			@Nonnull SeverityLevel severity,
			@Nonnull String message
	) {}

	private static final Pattern PREDICATE_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*(>=|<=|==|!=|>|<)\\s*([^=\\s]\\S*?)\\s*$");
	private static final Pattern VIOLATION_MARKER = Pattern.compile("// violation(?:\\s*\\((warning)\\))?(?:\\s*\\[([^\\]]+)\\])?(?:\\s*@(opener))?\\s*:");
	private static final String CHECK_SUFFIX = "Check";

	public static void assertCheckMatchesMarkers(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String inputPath,
			@Nonnull String... properties
	) throws Exception {
		final var resolvedPath = resolveInputPath(checkClass, inputPath);
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + resolvedPath);
		requireNonNull(url, "Test input file not found: " + resolvedPath);
		final var fileLines = Files.readAllLines(Path.of(url.toURI()));
		final var expected = parseViolationMarkers(fileLines, PropertiesUtil.arrayToMap(properties));
		final var violations = runCheck(checkClass, inputPath, properties);
		assertExpectedViolationsMatch(expected, violations, resolvedPath);
	}

	public static void assertCheckMatchesMarkers(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull List<String> lines,
			@Nonnull String context,
			@Nonnull String... properties
	) throws Exception {
		final var expected = parseViolationMarkers(lines, PropertiesUtil.arrayToMap(properties));
		// A trailing '// violation:' comment can otherwise change the check's own
		// input (e.g. it makes a blank line non-blank, hiding an internal-blank
		// violation).
		final var violations = runCheckInline(checkClass, stripViolationMarkers(String.join("\n", lines)), properties);
		assertExpectedViolationsMatch(expected, violations, context);
	}

	public static void assertCheckMatchesMarkersInline(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String content,
			@Nonnull String... properties
	) throws Exception {
		final var lines = List.of(content.split("\n", -1));
		final var expected = parseViolationMarkers(lines, PropertiesUtil.arrayToMap(properties));
		final var violations = runCheckInline(checkClass, content, properties);
		assertExpectedViolationsMatch(expected, violations, "<inline>");
	}

	private static void assertExpectedViolationsMatch(
			@Nonnull List<ExpectedViolation> expected,
			@Nonnull List<AuditEvent> actual,
			@Nonnull String context
	) {
		final var assertions = new ArrayList<Executable>();
		assertions.add(() -> {
			assertEquals(
					expected.size(),
					actual.size(),
					"Expected " + expected.size() + " violation(s) from `// violation:` markers in " + context
							+ ", got " + actual.size() + " from check"
			);
		});
		final var limit = Math.min(expected.size(), actual.size());
		for (var i = 0; i < limit; ++i) {
			final var idx = i;
			final var exp = expected.get(i);
			final var act = actual.get(i);
			assertions.add(() -> {
				assertEquals(
						exp.line(),
						act.getLine(),
						"violation[" + idx + "] line mismatch in " + context
				);
			});
			assertions.add(() -> {
				assertEquals(
						exp.severity(),
						act.getSeverityLevel(),
						"violation[" + idx + "] severity mismatch in " + context
				);
			});
			assertions.add(() -> {
				assertEquals(
						exp.message(),
						act.getMessage(),
						"violation[" + idx + "] message mismatch in " + context
				);
			});
		}
		assertAll(assertions);
	}

	public static void assertNoViolations(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String... inputPaths
	) throws Exception {
		for (var path : inputPaths) {
			final var violations = runCheck(checkClass, path);
			if (!violations.isEmpty()) {
				final var first = violations.getFirst();
				throw new AssertionError(
						checkClass.getSimpleName() + " unexpectedly fired on " + path
								+ " (" + violations.size() + " violation(s); first at line "
								+ first.getLine() + ": " + first.getMessage() + ")"
				);
			}
		}
	}

	@CheckReturnValue
	@Nonnull
	public static String deriveTopic(@Nonnull Class<? extends AbstractCheck> checkClass) {
		final var name = checkClass.getSimpleName();
		if (!name.endsWith(CHECK_SUFFIX))
			throw new IllegalArgumentException("Check class name must end in 'Check': " + name);
		return name.substring(0, name.length() - CHECK_SUFFIX.length()).toLowerCase();
	}

	@CheckReturnValue
	private static boolean evaluatePredicate(@Nullable String predicate, @Nonnull Map<String, String> properties) {
		if (predicate == null)
			return true;
		final var m = PREDICATE_PATTERN.matcher(predicate);
		if (!m.matches())
			throw new IllegalStateException("Malformed marker predicate '" + predicate + "': expected <key><op><value>");
		final var key = m.group(1);
		final var op = m.group(2);
		final var rhsRaw = m.group(3);
		final var lhsRaw = properties.get(key);
		if (lhsRaw == null)
			return false;
		final var lhsInt = tryParseInt(lhsRaw);
		final var rhsInt = tryParseInt(rhsRaw);
		if (lhsInt != null && rhsInt != null) {
			return switch (op) {
				case "!=" -> lhsInt != rhsInt;
				case "<" -> lhsInt < rhsInt;
				case "<=" -> lhsInt <= rhsInt;
				case "==" -> lhsInt.intValue() == rhsInt.intValue();
				case ">" -> lhsInt > rhsInt;
				case ">=" -> lhsInt >= rhsInt;
				default -> throw new IllegalStateException("Unreachable: unknown op " + op);
			};
		}
		return switch (op) {
			case "!=" -> !lhsRaw.equals(rhsRaw);
			case "==" -> lhsRaw.equals(rhsRaw);
			default -> throw new IllegalStateException(
					"Ordering op '" + op + "' requires integer operands; predicate '" + predicate
							+ "' has non-integer value (lhs='" + lhsRaw + "', rhs='" + rhsRaw + "')"
			);
		};
	}

	@CheckReturnValue
	@Nonnull
	static List<String> hoistImportsDirectives(@Nonnull List<String> rawLines) {
		final var prefix = "// imports:";
		final var collected = new LinkedHashSet<String>();
		for (var line : rawLines) {
			final var trimmed = line.strip();
			if (!trimmed.startsWith(prefix))
				continue;
			final var fqn = trimmed.substring(prefix.length()).strip();
			if (!fqn.isEmpty())
				collected.add("import " + fqn + ";");
		}
		if (collected.isEmpty())
			return rawLines;
		var packageLineIdx = -1;
		for (var i = 0; i < rawLines.size(); ++i) {
			if (rawLines.get(i).strip().startsWith("package ")) {
				packageLineIdx = i;
				break;
			}
		}
		final var out = new ArrayList<String>(rawLines.size() + collected.size() + 1);
		if (packageLineIdx < 0) {
			out.addAll(collected);
			out.add("");
			out.addAll(rawLines);
		}
		else {
			out.addAll(rawLines.subList(0, packageLineIdx + 1));
			out.add("");
			out.addAll(collected);
			out.addAll(rawLines.subList(packageLineIdx + 1, rawLines.size()));
		}
		return out;
	}

	@CheckReturnValue
	@Nonnull
	public static List<ExpectedViolation> parseViolationMarkers(@Nonnull List<String> lines) {
		return parseViolationMarkers(lines, Map.of());
	}

	@CheckReturnValue
	@Nonnull
	public static List<ExpectedViolation> parseViolationMarkers(@Nonnull List<String> lines, @Nonnull Map<String, String> properties) {
		final var result = new ArrayList<ExpectedViolation>();
		final var openerLine = textBlockOpenerLine(lines);
		for (var i = 0; i < lines.size(); ++i) {
			final var line = lines.get(i);
			final var matches = VIOLATION_MARKER.matcher(line).results().toList();
			for (var j = 0; j < matches.size(); ++j) {
				final var curr = matches.get(j);
				final var msgStart = curr.end();
				final var msgEnd = j + 1 < matches.size() ? matches.get(j + 1).start() : line.length();
				if (!evaluatePredicate(curr.group(2), properties))
					continue;
				final var severity = curr.group(1) != null ? SeverityLevel.WARNING : SeverityLevel.ERROR;
				final var violationLine = "opener".equals(curr.group(3)) ? openerLine[i] : i;
				result.add(new ExpectedViolation(violationLine + 1, severity, line.substring(msgStart, msgEnd).trim()));
			}
		}
		return result;
	}

	@CheckReturnValue
	@Nonnull
	private static String resolveInputPath(@Nonnull Class<? extends AbstractCheck> checkClass, @Nonnull String inputPath) {
		return inputPath.indexOf('/') < 0 ? deriveTopic(checkClass) + "/" + inputPath : inputPath;
	}

	@Nonnull
	public static List<AuditEvent> runCheck(@Nonnull Class<? extends AbstractCheck> checkClass, @Nonnull String inputPath) throws Exception {
		return runCheck(checkClass, inputPath, new String[0]);
	}

	@Nonnull
	public static List<AuditEvent> runCheck(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String inputPath,
			@Nonnull String... properties
	) throws Exception {
		final var resolvedPath = resolveInputPath(checkClass, inputPath);
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + resolvedPath);
		requireNonNull(url, "Test input file not found: " + resolvedPath);
		return runCheckOnFiles(checkClass, List.of(new File(url.toURI())), properties);
	}

	@Nonnull
	public static List<AuditEvent> runCheckInline(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String content,
			@Nonnull String... properties
	) throws Exception {
		final var tempFile = File.createTempFile("checkstyle-inline-test", ".java");
		try {
			Files.writeString(tempFile.toPath(), content);
			return runCheckOnFiles(checkClass, List.of(tempFile), properties);
		}
		finally {
			tempFile.delete();
		}
	}

	@Nonnull
	public static List<AuditEvent> runCheckOnDiskFile(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull File file,
			@Nonnull String... properties
	) throws Exception {
		return runCheckOnFiles(checkClass, List.of(file), properties);
	}

	@Nonnull
	public static List<AuditEvent> runCheckOnFiles(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String... inputPaths
	) throws Exception {
		final var files = new ArrayList<File>();
		for (var path : inputPaths) {
			final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path);
			requireNonNull(url, "Test input file not found: " + path);
			files.add(new File(url.toURI()));
		}
		return runCheckOnFiles(checkClass, files);
	}

	@Nonnull
	private static List<AuditEvent> runCheckOnFiles(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull List<File> files,
			@Nonnull String... properties
	) throws Exception {
		if (properties.length % 2 != 0) {
			throw new IllegalArgumentException(
					"properties must be an even-length key/value sequence, got length " + properties.length
			);
		}
		final var checkConfig = new DefaultConfiguration(checkClass.getName());
		for (var i = 0; i < properties.length; i += 2)
			checkConfig.addProperty(properties[i], properties[i + 1]);

		final var treeWalkerConfig = new DefaultConfiguration(TreeWalker.class.getName());
		treeWalkerConfig.addProperty("tabWidth", String.valueOf(LineLength.TAB_WIDTH));
		treeWalkerConfig.addChild(checkConfig);

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(treeWalkerConfig);

		final var translated = new ArrayList<File>(files.size());
		for (var file : files) {
			final var rawLines = Files.readAllLines(file.toPath());
			final var hasDirective = rawLines.stream().anyMatch(l -> l.strip().startsWith("// imports:"));
			if (!hasDirective) {
				translated.add(file);
				continue;
			}
			final var translatedLines = hoistImportsDirectives(rawLines);
			final var tempDir = Path.of(System.getProperty("java.io.tmpdir"), "checkstyle-translated");
			Files.createDirectories(tempDir);
			final var sourceKey = Integer.toHexString(file.getAbsolutePath().hashCode());
			final var temp = tempDir.resolve(sourceKey + "-" + file.getName()).toFile();
			Files.writeString(temp.toPath(), String.join("\n", translatedLines));
			translated.add(temp);
		}
		return CheckerCache.process(checkerConfig, checkClass.getClassLoader(), translated);
	}

	@Nonnull
	static List<AuditEvent> runRegexCheck(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull String inputPath
	) throws Exception {
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + inputPath);
		requireNonNull(url, "Test input file not found: " + inputPath);
		return runRegexCheckOnFile(moduleName, format, new File(url.toURI()));
	}

	@Nonnull
	static List<AuditEvent> runRegexCheckInline(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull String content
	) throws Exception {
		final var tempFile = File.createTempFile("checkstyle-regex-test", ".java");
		try {
			Files.writeString(tempFile.toPath(), content);
			return runRegexCheckOnFile(moduleName, format, tempFile);
		}
		finally {
			tempFile.delete();
		}
	}

	@Nonnull
	private static List<AuditEvent> runRegexCheckOnFile(
			@Nonnull String moduleName,
			@Nonnull String format,
			@Nonnull File file
	) throws Exception {
		final var moduleConfig = new DefaultConfiguration(moduleName);
		moduleConfig.addProperty("format", format);
		moduleConfig.addProperty("message", "test violation");

		final var checkerConfig = new DefaultConfiguration("Checker");
		checkerConfig.addChild(moduleConfig);

		return CheckerCache.process(checkerConfig, Checker.class.getClassLoader(), file);
	}

	/**
	 * Removes trailing {@code // violation:} markers (and the whitespace before
	 * them) so the check runs on the same source production and the fixer see.
	 * Line count is preserved, so reported line numbers still line up with the
	 * markers parsed from the original content.
	 */
	@CheckReturnValue
	@Nonnull
	static String stripViolationMarkers(@Nonnull String content) {
		final var lines = content.split("\n", -1);
		for (var i = 0; i < lines.length; ++i) {
			final var matcher = VIOLATION_MARKER.matcher(lines[i]);
			if (!matcher.find())
				continue;
			var end = matcher.start();
			while (end > 0 && (lines[i].charAt(end - 1) == ' ' || lines[i].charAt(end - 1) == '\t'))
				--end;
			lines[i] = lines[i].substring(0, end);
		}
		return String.join("\n", lines);
	}

	/**
	 * For each line, the index of the line where the text block it begins inside opened, or the line
	 * itself when it does not begin inside a text block. Resolves a {@code // violation@opener:}
	 * marker (placed on a text block's closing line, after the closing {@code """}) back to the
	 * opener line the check reports on. The opener line itself cannot carry a trailing comment
	 * because only whitespace may follow a text-block-opening {@code """}. Plain {@code // violation:}
	 * markers are unaffected and stay on their own line.
	 */
	@CheckReturnValue
	@Nonnull
	private static int[] textBlockOpenerLine(@Nonnull List<String> lines) {
		final var opener = new int[lines.size()];
		var state = JavaLineScanner.LexerState.NONE;
		var currentOpener = 0;
		for (var i = 0; i < lines.size(); ++i) {
			if (state.inTextBlock())
				opener[i] = currentOpener;
			else {
				opener[i] = i;
				currentOpener = i;
			}
			state = JavaLineScanner.stateAfter(lines.get(i), state);
		}
		return opener;
	}

	@CheckReturnValue
	@Nullable
	private static Integer tryParseInt(@Nonnull String s) {
		try {
			return Integer.parseInt(s);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}
}