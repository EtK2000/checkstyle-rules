package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.etk2000.checkstyle.TestResources;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * The fix pipeline honours the {@code suppressions.xml} in the config directory the way the
 * checkstyle task does. These runs leave a suppressed violation in place on purpose, so they live
 * apart from {@link CheckstyleFixIntegrationTest}, whose every test verifies a violation-free result.
 */
public class CheckstyleFixSuppressionsTest {
	record SuppressedRun(int fixed, @Nonnull String content) {}

	private static final String UPPER_ELL = "do_execute_suppressed_upper_ell";

	@Nonnull
	private static String fixed(@Nonnull String caseName) {
		return String.join("\n", TestResources.loadCase("integration", caseName).fixedLines());
	}

	@Nonnull
	private static String input(@Nonnull String caseName) {
		return String.join("\n", TestResources.loadCase("integration", caseName).inputLines());
	}

	/** A {@code suppressions.xml} holding the one {@code suppress} element. */
	@Nonnull
	private static String suppressions(@Nonnull String suppress) {
		return String.join(
				"\n",
				"<?xml version=\"1.0\"?>",
				"<!DOCTYPE suppressions PUBLIC \"-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN\" \"https://checkstyle.org/dtds/suppressions_1_2.dtd\">",
				"<suppressions>",
				suppress,
				"</suppressions>"
		);
	}

	@TempDir
	Path tempDir;

	@Test
	public void doExecuteFixesAFileSuppressionsXmlLeavesOut() throws Exception {
		final var xml = suppressions("<suppress checks=\"UpperEllCheck\" files=\"Other\\.java\" />");
		assertEquals(new SuppressedRun(1, fixed(UPPER_ELL)), fixUnderSuppressions(UPPER_ELL, xml));
	}

	@Test
	public void doExecuteFixesWithoutASuppressionsXml() throws Exception {
		assertEquals(new SuppressedRun(1, fixed(UPPER_ELL)), fixUnderSuppressions(UPPER_ELL, null));
	}

	/**
	 * FieldConsolidation keeps the order written where the run never sorts fields, whether a
	 * {@code @SuppressWarnings} or the {@code suppressions.xml} turns FieldSorting off.
	 */
	@Test
	public void doExecuteMergesInTheOrderWrittenWhereSuppressionsXmlSuppressesFieldSorting() throws Exception {
		final var xml = suppressions("<suppress checks=\"FieldSortingCheck\" files=\"Suppressed\\.java\" />");
		final var run = fixUnderSuppressions("field_consolidation_names_out_of_order", xml);
		assertEquals(1, run.fixed());
		assertEquals(List.of("\tint zeta, alpha;"), run.content().lines().filter(line -> line.startsWith("\t")).toList());
	}

	@Test
	public void doExecuteRefusesAMalformedSuppressionsXml() {
		assertThrows(CheckstyleException.class, () -> fixUnderSuppressions(UPPER_ELL, "<suppressions><suppress"));
	}

	@Test
	public void doExecuteSkipsACheckerModuleSuppressionsXmlSuppressesById() throws Exception {
		final var xml = suppressions("<suppress id=\"NoTrailingWhitespace\" files=\"Suppressed\\.java\" />");
		assertEquals(new SuppressedRun(0, input("trailing_whitespace")), fixUnderSuppressions("trailing_whitespace", xml));
	}

	@Test
	public void doExecuteSkipsAFileSuppressionsXmlSuppresses() throws Exception {
		final var xml = suppressions("<suppress checks=\"UpperEllCheck\" files=\"Suppressed\\.java\" />");
		assertEquals(new SuppressedRun(0, input(UPPER_ELL)), fixUnderSuppressions(UPPER_ELL, xml));
	}

	/**
	 * Runs one fix pass over the integration case {@code caseName}, saved as {@code Suppressed.java},
	 * with a config directory holding {@code suppressionsXml} as its {@code suppressions.xml}, or no
	 * such file when it is null.
	 */
	@Nonnull
	private SuppressedRun fixUnderSuppressions(@Nonnull String caseName, @Nullable String suppressionsXml) throws Exception {
		final var file = tempDir.resolve("Suppressed.java").toFile();
		Files.writeString(file.toPath(), input(caseName));
		final var configDirectory = Files.createDirectories(tempDir.resolve("config"));
		if (suppressionsXml != null)
			Files.writeString(configDirectory.resolve("suppressions.xml"), suppressionsXml);
		final var config = CheckstyleFixAction.createCheckerConfig(String.valueOf(Integer.MAX_VALUE), configDirectory.toFile());
		final var result = CheckstyleFixAction.doExecute(config, false, List.of(file));
		return new SuppressedRun(result[1], Files.readString(file.toPath()));
	}
}