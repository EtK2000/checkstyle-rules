package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

public class BaseCheckTestTest {
	private static class Check extends StubCheck {}

	private static class MissingSuffix extends StubCheck {}

	private abstract static class StubCheck extends AbstractCheck {
		@Nonnull
		@Override
		public int[] getAcceptableTokens() {
			return new int[0];
		}

		@Nonnull
		@Override
		public int[] getDefaultTokens() {
			return new int[0];
		}

		@Nonnull
		@Override
		public int[] getRequiredTokens() {
			return new int[0];
		}
	}

	private static class XMLParserCheck extends StubCheck {}

	@Nonnull
	private static Stream<Arguments> deriveTopicCases() {
		return Stream.of(
				Arguments.of(InstanceofBeforeCastCheck.class, "instanceofbeforecast"),
				Arguments.of(NoUnnecessaryThisCheck.class, "nounnecessarythis"),
				Arguments.of(PreferExactAssertionCheck.class, "preferexactassertion"),
				Arguments.of(PreferSpecificApiCheck.class, "preferspecificapi"),
				Arguments.of(RedundantNumericSuffixCheck.class, "redundantnumericsuffix"),
				Arguments.of(XMLParserCheck.class, "xmlparser")
		);
	}

	@Nonnull
	private static List<String> toEventKeys(@Nonnull List<AuditEvent> events) {
		final var keys = new ArrayList<String>();
		for (var e : events)
			keys.add(e.getFileName() + ":" + e.getLine() + ":" + e.getMessage());
		return keys;
	}

	@Test
	public void assertCheckMatchesMarkersInlineExtraMarkerFails() {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.\n\t\t// violation: extra unrelated marker\n\t}\n}";
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src)
		);
		assertTrue(ex.getMessage().contains("Expected 2 violation(s)"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersInlineMatches() throws Exception {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.\n\t}\n}";
		BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src);
	}

	@Test
	public void assertCheckMatchesMarkersInlineMissingMarkerFails() {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x);\n\t}\n}";
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src)
		);
		assertTrue(ex.getMessage().contains("Expected 0 violation(s)"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersInlineTrailingNewlinePasses() throws Exception {
		BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, "class T {}\n");
	}

	@Test
	public void assertCheckMatchesMarkersInlineWrongLineFails() {
		final var src = "class T {\n\tint x; // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.\n\tvoid m() {\n\t\tSystem.out.println(this.x);\n\t}\n}";
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src)
		);
		assertTrue(ex.getMessage().contains("line mismatch"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersInlineWrongMessageFails() {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x); // violation: completely wrong message\n\t}\n}";
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src)
		);
		assertTrue(ex.getMessage().contains("message mismatch"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersInlineWrongSeverityFails() {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.\n\t}\n}";
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src, "severity", "warning")
		);
		assertTrue(ex.getMessage().contains("severity mismatch"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersInlineZeroViolationsPasses() throws Exception {
		BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, "class T {}");
	}

	@Test
	public void assertCheckMatchesMarkersLinesContextAppearsInFailureMessage() {
		final var lines = List.of(
				"class T {",
				"\tvoid m() {} // violation: never fires here",
				"}"
		);
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "my-topic/my-case")
		);
		assertTrue(ex.getMessage().contains("my-topic/my-case"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersLinesEmptyListPasses() throws Exception {
		BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, List.of(), "<test>");
	}

	@Test
	public void assertCheckMatchesMarkersLinesExtraMarkerFails() {
		final var lines = List.of(
				"class T {",
				"\tint x;",
				"\tvoid m() {",
				"\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.",
				"\t\t// violation: extra unrelated marker",
				"\t}",
				"}"
		);
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>")
		);
		assertTrue(ex.getMessage().contains("Expected 2 violation(s)"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersLinesMarkerOnLastLineMatches() throws Exception {
		final var lines = List.of(
				"class T {",
				"\tint x;",
				"\tvoid m() { System.out.println(this.x); }} // violation: Unnecessary 'this.x', only use when shadowing or in field assignment."
		);
		BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>");
	}

	@Test
	public void assertCheckMatchesMarkersLinesMatches() throws Exception {
		final var lines = List.of(
				"class T {",
				"\tint x;",
				"\tvoid m() {",
				"\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.",
				"\t}",
				"}"
		);
		BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>");
	}

	@Test
	public void assertCheckMatchesMarkersLinesMatchesInlineForSameContent() throws Exception {
		final var src = "class T {\n\tint x;\n\tvoid m() {\n\t\tSystem.out.println(this.x); // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.\n\t}\n}";
		BaseCheckTest.assertCheckMatchesMarkersInline(NoUnnecessaryThisCheck.class, src);
		BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, List.of(src.split("\n", -1)), "<test>");
	}

	@Test
	public void assertCheckMatchesMarkersLinesMissingMarkerFails() {
		final var lines = List.of(
				"class T {",
				"\tint x;",
				"\tvoid m() {",
				"\t\tSystem.out.println(this.x);",
				"\t}",
				"}"
		);
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>")
		);
		assertTrue(ex.getMessage().contains("Expected 0 violation(s)"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersLinesStripBlankLineMarkerIsLoadBearing() throws Exception {
		final var lines = List.of(
				"class T {",
				"\t@V({",
				"// violation: No blank line inside annotation 'V'.",
				"\t\t\"a\"",
				"\t})",
				"\tvoid f() {}",
				"}"
		);
		BaseCheckTest.assertCheckMatchesMarkers(AnnotationOwnLineCheck.class, lines, "<test>");

		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkersInline(AnnotationOwnLineCheck.class, String.join("\n", lines))
		);
		assertTrue(ex.getMessage().contains("Expected 1 violation(s)"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersLinesWrongLineFails() {
		final var lines = List.of(
				"class T {",
				"\tint x; // violation: Unnecessary 'this.x', only use when shadowing or in field assignment.",
				"\tvoid m() {",
				"\t\tSystem.out.println(this.x);",
				"\t}",
				"}"
		);
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>")
		);
		assertTrue(ex.getMessage().contains("line mismatch"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("<test>"), "context missing: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersLinesWrongMessageFails() {
		final var lines = List.of(
				"class T {",
				"\tint x;",
				"\tvoid m() {",
				"\t\tSystem.out.println(this.x); // violation: completely wrong message",
				"\t}",
				"}"
		);
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, lines, "<test>")
		);
		assertTrue(ex.getMessage().contains("message mismatch"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("<test>"), "context missing: " + ex.getMessage());
	}

	@Test
	public void assertCheckMatchesMarkersMatchesRealFixture() throws Exception {
		BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, "nounnecessarythis/cases.in.java");
	}

	@Test
	public void assertCheckMatchesMarkersMissingResourceThrows() {
		final var ex = assertThrows(
				NullPointerException.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, "does/not/exist.java")
		);
		assertTrue(
				ex.getMessage().endsWith("Test input file not found: does/not/exist.java"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@Test
	public void assertCheckMatchesMarkersNoSlashMissingResourceMessageContainsResolvedPath() {
		final var ex = assertThrows(
				NullPointerException.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, "does-not-exist.java")
		);
		assertTrue(
				ex.getMessage().endsWith("Test input file not found: nounnecessarythis/does-not-exist.java"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@Test
	public void assertCheckMatchesMarkersWrongMessageFails() {
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertCheckMatchesMarkers(NoUnnecessaryThisCheck.class, "markermatcher/cases.in.java")
		);
		assertTrue(ex.getMessage().contains("message mismatch"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void assertNoViolationsEmptyPathsNoOps() throws Exception {
		BaseCheckTest.assertNoViolations(NoUnnecessaryThisCheck.class);
	}

	@Test
	public void assertNoViolationsThrowsWithFileLineMessageWhenCheckFires() {
		final var ex = assertThrows(
				AssertionError.class,
				() -> BaseCheckTest.assertNoViolations(NoUnnecessaryThisCheck.class, "nounnecessarythis/cases.in.java")
		);
		assertTrue(ex.getMessage().contains("NoUnnecessaryThisCheck"), "missing check name: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("nounnecessarythis/cases.in.java"), "missing path: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("first at line 8"), "missing line number: " + ex.getMessage());
		assertTrue(
				ex.getMessage().contains("Unnecessary 'this.field'"),
				"missing first violation message text: " + ex.getMessage()
		);
		assertTrue(ex.getMessage().contains("9 violation(s)"), "missing violation count: " + ex.getMessage());
	}

	@Test
	public void deriveTopicJustCheckSuffixReturnsEmpty() {
		assertEquals("", BaseCheckTest.deriveTopic(Check.class));
	}

	@Test
	public void deriveTopicNonCheckSuffixedClassThrows() {
		final var ex = assertThrows(
				IllegalArgumentException.class,
				() -> BaseCheckTest.deriveTopic(MissingSuffix.class)
		);
		assertTrue(
				ex.getMessage().contains("MissingSuffix"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@MethodSource("deriveTopicCases")
	@ParameterizedTest
	public void deriveTopicStripsCheckSuffixAndLowercases(
			@Nonnull Class<? extends AbstractCheck> checkClass,
			@Nonnull String expectedTopic
	) {
		assertEquals(expectedTopic, BaseCheckTest.deriveTopic(checkClass));
	}

	@Test
	public void hoistImportsDirectivesDedupesRepeatedDirective() {
		final var input = List.of(
				"package x;",
				"// imports: java.util.List",
				"// imports: java.util.List",
				"class T {}"
		);
		assertEquals(
				List.of("package x;", "", "import java.util.List;", "// imports: java.util.List", "// imports: java.util.List", "class T {}"),
				BaseCheckTest.hoistImportsDirectives(input)
		);
	}

	@Test
	public void hoistImportsDirectivesIgnoresEmptyFqn() {
		final var input = List.of("package x;", "// imports:", "// imports: java.util.List", "class T {}");
		assertEquals(
				List.of("package x;", "", "import java.util.List;", "// imports:", "// imports: java.util.List", "class T {}"),
				BaseCheckTest.hoistImportsDirectives(input)
		);
	}

	@Test
	public void hoistImportsDirectivesNoDirectiveReturnsRawLines() {
		final var input = List.of("package x;", "", "class T {}");
		assertEquals(input, BaseCheckTest.hoistImportsDirectives(input));
	}

	@Test
	public void hoistImportsDirectivesNoPackagePrependsImports() {
		final var input = List.of("// imports: java.util.List", "class T {}");
		assertEquals(
				List.of("import java.util.List;", "", "// imports: java.util.List", "class T {}"),
				BaseCheckTest.hoistImportsDirectives(input)
		);
	}

	@Test
	public void hoistImportsDirectivesSplicesAfterPackage() {
		final var input = List.of(
				"package x;",
				"",
				"// imports: java.util.List",
				"// imports: java.util.Map",
				"class T {}"
		);
		assertEquals(
				List.of(
						"package x;",
						"",
						"import java.util.List;",
						"import java.util.Map;",
						"",
						"// imports: java.util.List",
						"// imports: java.util.Map",
						"class T {}"
				),
				BaseCheckTest.hoistImportsDirectives(input)
		);
	}

	@Test
	public void parseViolationMarkersAdjacentMarkersFirstHasEmptyMessage() {
		final var lines = List.of("x; // violation:// violation: second");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(2, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("", result.getFirst().message());
		assertEquals(1, result.getLast().line());
		assertEquals(SeverityLevel.ERROR, result.getLast().severity());
		assertEquals("second", result.getLast().message());
	}

	@Test
	public void parseViolationMarkersCaseVariantDoesNotMatch() {
		final var lines = List.of(
				"x; // VIOLATION: shouted",
				"y; // Violation: titled",
				"z; // vIoLaTion: mixed"
		);
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void parseViolationMarkersEmptyListReturnsEmpty() {
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(List.of()));
	}

	@Test
	public void parseViolationMarkersEmptyMessage() {
		final var lines = List.of("x; // violation:");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersExtractsLineAndMessage() {
		final var lines = List.of("class T {", "\tint x; // violation: x is forbidden", "}");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(2, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("x is forbidden", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersInsideStringLiteralStillMatches() {
		final var lines = List.of("String s = \"// violation: not a real marker\";");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("not a real marker\";", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersMixedSeverityOnSameLine() {
		final var lines = List.of("x; // violation: err // violation (warning): warn");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).line());
		assertEquals(SeverityLevel.ERROR, result.get(0).severity());
		assertEquals("err", result.get(0).message());
		assertEquals(1, result.get(1).line());
		assertEquals(SeverityLevel.WARNING, result.get(1).severity());
		assertEquals("warn", result.get(1).message());
	}

	@Test
	public void parseViolationMarkersMultipleOnSameLine() {
		final var lines = List.of("x; // violation: first  // violation: second");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(2, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("first", result.getFirst().message());
		assertEquals(1, result.getLast().line());
		assertEquals(SeverityLevel.ERROR, result.getLast().severity());
		assertEquals("second", result.getLast().message());
	}

	@Test
	public void parseViolationMarkersNoMarkersReturnsEmpty() {
		final var lines = List.of("class T {", "\tint x;", "}");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void parseViolationMarkersOpenerAttributesToTextBlockOpenerLine() {
		final var lines = List.of(
				"class T {",
				"\tString s = \"\"\"",
				"\t\ttext",
				"\t\t\"\"\"; // violation@opener: msg",
				"}"
		);
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(2, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersOpenerMapsToNearestPrecedingTextBlockOpener() {
		final var lines = List.of(
				"class T {",
				"\tString a = \"\"\"",
				"\t\tone",
				"\t\t\"\"\";",
				"\tString b = \"\"\"",
				"\t\ttwo",
				"\t\t\"\"\"; // violation@opener: msg",
				"}"
		);
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(5, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersOpenerOutsideTextBlockMapsToOwnLine() {
		final var lines = List.of("class T {", "\tint x; // violation@opener: msg", "}");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(2, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersOpenerTextBlockOpenerOnFirstLine() {
		final var lines = List.of(
				"TB = \"\"\"",
				"\ttext",
				"\t\"\"\"; // violation@opener: msg"
		);
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersOpenerWithWarningQualifier() {
		final var lines = List.of(
				"class T {",
				"\tString s = \"\"\"",
				"\t\ttext",
				"\t\t\"\"\"; // violation (warning) @opener: msg",
				"}"
		);
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(2, result.getFirst().line());
		assertEquals(SeverityLevel.WARNING, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPlainMarkerOnTextBlockCloseMapsToOwnLine() {
		final var lines = List.of(
				"class T {",
				"\tString s = \"\"\"",
				"\t\ttext",
				"\t\t\"\"\"; // violation: msg",
				"}"
		);
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(4, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateBracketsBeforeWarningDoesNotMatch() {
		final var lines = List.of("x; // violation [minSdk>=35] (warning): msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35")));
	}

	@Test
	public void parseViolationMarkersPredicateDoubleBracketsDoesNotMatch() {
		final var lines = List.of("x; // violation [minSdk>=35][delta>=0]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35", "delta", "0")));
	}

	@Test
	public void parseViolationMarkersPredicateEmptyBracketsDoesNotMatch() {
		final var lines = List.of("x; // violation []: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35")));
	}

	@Test
	public void parseViolationMarkersPredicateEqFalse() {
		final var lines = List.of("x; // violation [minSdk==5]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "6")));
	}

	@Test
	public void parseViolationMarkersPredicateEqJustBelow() {
		final var lines = List.of("x; // violation [minSdk==5]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "4")));
	}

	@Test
	public void parseViolationMarkersPredicateEqTrue() {
		final var lines = List.of("x; // violation [minSdk==5]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "5"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateGeFalse() {
		final var lines = List.of("x; // violation [minSdk>=35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34")));
	}

	@Test
	public void parseViolationMarkersPredicateGeTrue() {
		final var lines = List.of("x; // violation [minSdk>=35]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateGtBoundaryAtEqualValue() {
		final var lines = List.of("x; // violation [minSdk>35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35")));
	}

	@Test
	public void parseViolationMarkersPredicateGtTrue() {
		final var lines = List.of("x; // violation [minSdk>35]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "36"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateInvalidOperatorThrows() {
		final var lines = List.of("x; // violation [minSdk=35]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate 'minSdk=35'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateLeBoundaryAtEqualValue() {
		final var lines = List.of("x; // violation [minSdk<=10]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "10"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateLeFalse() {
		final var lines = List.of("x; // violation [minSdk<=10]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "11")));
	}

	@Test
	public void parseViolationMarkersPredicateLhsLeadingMinusParsesAsNumeric() {
		final var lines = List.of("x; // violation [key==-42]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("key", "-42"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateLhsLeadingPlusParsesAsNumeric() {
		final var lines = List.of("x; // violation [key==42]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("key", "+42"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateLtBoundaryAtEqualValue() {
		final var lines = List.of("x; // violation [minSdk<35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35")));
	}

	@Test
	public void parseViolationMarkersPredicateLtTrue() {
		final var lines = List.of("x; // violation [minSdk<35]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateMalformedThrows() {
		final var lines = List.of("x; // violation [garbage]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate 'garbage'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateMissingRhsThrows() {
		final var lines = List.of("x; // violation [minSdk>=]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate 'minSdk>='"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateMixedGatingBothSurviveWhenBound() {
		final var lines = List.of("x; // violation [minSdk>=35]: gated // violation: ungated");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"));
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).line());
		assertEquals(SeverityLevel.ERROR, result.get(0).severity());
		assertEquals("gated", result.get(0).message());
		assertEquals(1, result.get(1).line());
		assertEquals(SeverityLevel.ERROR, result.get(1).severity());
		assertEquals("ungated", result.get(1).message());
	}

	@Test
	public void parseViolationMarkersPredicateMixedGatingDropsGatedOnly() {
		final var lines = List.of("x; // violation [minSdk>=35]: gated // violation: ungated");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("ungated", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateMixedGatingReversedOrder() {
		final var lines = List.of("x; // violation: ungated // violation [minSdk>=35]: gated");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("ungated", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateMixedTypeEqLhsNonIntRhsInt() {
		final var lines = List.of("x; // violation [key==42]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("key", "42abc")));
	}

	@Test
	public void parseViolationMarkersPredicateMixedTypeNeLhsNonIntRhsInt() {
		final var lines = List.of("x; // violation [key!=42]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("key", "42abc"));
		assertEquals(1, result.size());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateNeFalse() {
		final var lines = List.of("x; // violation [minSdk!=5]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "5")));
	}

	@Test
	public void parseViolationMarkersPredicateNegativeRhsAccepted() {
		final var lines = List.of("x; // violation [delta>=-1]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("delta", "0"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateNeJustBelow() {
		final var lines = List.of("x; // violation [minSdk!=5]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "4"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateNeTrue() {
		final var lines = List.of("x; // violation [minSdk!=5]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "6"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicateNonIntegerRhsThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "34.5]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("rhs='34.5'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "34.5'"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicatePropertyValueEmptyStringThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "35]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", ""))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("lhs=''"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "35'"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicatePropertyValueNotIntegerThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "35]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "abc"))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("lhs='abc'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "35'"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicatePropertyValueOverflowThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "35]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "9999999999"))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("lhs='9999999999'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "35'"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicatePropertyValueWithWhitespaceThrows(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "35]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", " 35 "))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("lhs=' 35 '"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "35'"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateRhsAtIntMaxAccepted() {
		final var lines = List.of("x; // violation [delta<=2147483647]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("delta", "0"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateRhsAtIntMinAccepted() {
		final var lines = List.of("x; // violation [delta>=-2147483648]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("delta", "0"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateRhsLeadingEqualsThrows() {
		final var lines = List.of("x; // violation [minSdk>= =5]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "5"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate 'minSdk>= =5'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicateRhsNegativeOverflowThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [delta" + op + "-9999999999]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("delta", "0"))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("rhs='-9999999999'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'delta" + op + "-9999999999'"), "unexpected message: " + ex.getMessage());
	}

	@ParameterizedTest
	@ValueSource(strings = {"==", "!="})
	public void parseViolationMarkersPredicateRhsOverflowDoesNotThrowForEqualityOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "9999999999]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"));
		if ("==".equals(op))
			assertEquals(List.of(), result);
		else {
			assertEquals(1, result.size());
			assertEquals(1, result.getFirst().line());
			assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
			assertEquals("msg", result.getFirst().message());
		}
	}

	@ParameterizedTest
	@ValueSource(strings = {">=", ">", "<=", "<"})
	public void parseViolationMarkersPredicateRhsOverflowThrowsForOrderingOp(String op) {
		final var lines = List.of("x; // violation [minSdk" + op + "9999999999]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Ordering op '" + op + "'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("requires integer operands"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("rhs='9999999999'"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("predicate 'minSdk" + op + "9999999999'"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateStringEqFalse() {
		final var lines = List.of("x; // violation [allowedMethods==genericMethod]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("allowedMethods", "otherMethod")));
	}

	@Test
	public void parseViolationMarkersPredicateStringEqFalseEmptyLhs() {
		final var lines = List.of("x; // violation [key==value]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("key", "")));
	}

	@Test
	public void parseViolationMarkersPredicateStringEqTrue() {
		final var lines = List.of("x; // violation [allowedMethods==genericMethod]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("allowedMethods", "genericMethod"));
		assertEquals(1, result.size());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateStringNeFalse() {
		final var lines = List.of("x; // violation [allowedMethods!=genericMethod]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("allowedMethods", "genericMethod")));
	}

	@Test
	public void parseViolationMarkersPredicateStringNeTrue() {
		final var lines = List.of("x; // violation [allowedMethods!=genericMethod]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("allowedMethods", "otherMethod"));
		assertEquals(1, result.size());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateThreeMarkersKeptDroppedKept() {
		final var lines = List.of("x; // violation: a // violation [minSdk>=35]: b // violation: c");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34"));
		assertEquals(2, result.size());
		assertEquals(1, result.get(0).line());
		assertEquals(SeverityLevel.ERROR, result.get(0).severity());
		assertEquals("a", result.get(0).message());
		assertEquals(1, result.get(1).line());
		assertEquals(SeverityLevel.ERROR, result.get(1).severity());
		assertEquals("c", result.get(1).message());
	}

	@Test
	public void parseViolationMarkersPredicateUnboundPropertyDropsMarker() {
		final var lines = List.of("x; // violation [minSdk>=35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of()));
	}

	@Test
	public void parseViolationMarkersPredicateUnboundPropertyDropsMarkerForEquality() {
		final var lines = List.of("x; // violation [allowedMethods==genericMethod]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of()));
	}

	@Test
	public void parseViolationMarkersPredicateUnboundPropertyDropsMarkerOtherKeyPresent() {
		final var lines = List.of("x; // violation [minSdk>=35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("otherKey", "5")));
	}

	@Test
	public void parseViolationMarkersPredicateWarningQualifierBothPresentFalse() {
		final var lines = List.of("x; // violation (warning) [minSdk>=35]: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "34")));
	}

	@Test
	public void parseViolationMarkersPredicateWarningQualifierBothPresentTrue() {
		final var lines = List.of("x; // violation (warning) [minSdk>=35]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.WARNING, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersPredicateWhitespaceOnlyBracketsThrows() {
		final var lines = List.of("x; // violation [ ]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateWhitespaceOnlyRhsThrows() {
		final var lines = List.of("x; // violation [minSdk>=   ]: msg");
		final var ex = assertThrows(
				IllegalStateException.class,
				() -> BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"))
		);
		assertTrue(ex.getMessage().contains("Malformed marker predicate"), "unexpected message: " + ex.getMessage());
		assertTrue(ex.getMessage().contains("expected <key><op><value>"), "unexpected message: " + ex.getMessage());
	}

	@Test
	public void parseViolationMarkersPredicateWhitespaceTolerated() {
		final var lines = List.of("x; // violation [ minSdk >= 35 ]: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "35"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersRequiresSpaceAfterSlashSlash() {
		final var lines = List.of("x; //violation: msg");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void parseViolationMarkersThreeMarkersOnSameLine() {
		final var lines = List.of("x; // violation: a // violation: b // violation: c");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(3, result.size());
		assertEquals("a", result.get(0).message());
		assertEquals("b", result.get(1).message());
		assertEquals("c", result.get(2).message());
		for (var v : result) {
			assertEquals(1, v.line());
			assertEquals(SeverityLevel.ERROR, v.severity());
		}
	}

	@Test
	public void parseViolationMarkersUngatedMarkerSurvivesWithProperties() {
		final var lines = List.of("x; // violation: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of("minSdk", "30"));
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersUngatedMarkerWithEmptyPropertiesPasses() {
		final var lines = List.of("x; // violation: msg");
		final var result = BaseCheckTest.parseViolationMarkers(lines, Map.of());
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.ERROR, result.getFirst().severity());
		assertEquals("msg", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersUnknownQualifierDoesNotMatch() {
		final var lines = List.of("x; // violation (other): unexpected");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void parseViolationMarkersWarningQualifierCanonicalSpacing() {
		final var lines = List.of("x; // violation (warning): warn message");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.WARNING, result.getFirst().severity());
		assertEquals("warn message", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersWarningQualifierCaseVariantDoesNotMatch() {
		final var lines = List.of(
				"x; // violation (WARNING): shouted",
				"y; // violation (Warning): titled",
				"z; // violation (wArNiNg): mixed"
		);
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void parseViolationMarkersWarningQualifierNoSpaceBeforeParen() {
		final var lines = List.of("x; // violation(warning): warn message");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.WARNING, result.getFirst().severity());
		assertEquals("warn message", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersWarningQualifierSpaceBeforeColon() {
		final var lines = List.of("x; // violation (warning) : warn message");
		final var result = BaseCheckTest.parseViolationMarkers(lines);
		assertEquals(1, result.size());
		assertEquals(1, result.getFirst().line());
		assertEquals(SeverityLevel.WARNING, result.getFirst().severity());
		assertEquals("warn message", result.getFirst().message());
	}

	@Test
	public void parseViolationMarkersWarningQualifierWhitespaceInsideParensDoesNotMatch() {
		final var lines = List.of("x; // violation ( warning ): inner spaces");
		assertEquals(List.of(), BaseCheckTest.parseViolationMarkers(lines));
	}

	@Test
	public void runCheckInlineForwardsProperties() throws Exception {
		final var source = "class T {\n\tvoid f(String s) {\n\t\tif (s.trim().isEmpty()) {\n\t\t\treturn;\n\t\t}\n\t}\n}";
		final var below = BaseCheckTest.runCheckInline(PreferSpecificApiCheck.class, source, "minSdk", "32");
		assertTrue(below.isEmpty(), "minSdk=32 should gate off trim-isBlank, got: " + below);
		final var above = BaseCheckTest.runCheckInline(PreferSpecificApiCheck.class, source, "minSdk", "33");
		assertEquals(1, above.size());
		assertEquals("Use '.isBlank()' instead of '.trim().isEmpty()'.", above.getFirst().getMessage());
	}

	@Test
	public void runCheckInlineMatchesRunCheckForSameContent() throws Exception {
		final var path = "nounnecessarythis/cases.in.java";
		final var url = BaseCheckTest.class.getResource("/com/etk2000/checkstyle/inputs/" + path);
		final var content = Files.readString(Path.of(url.toURI()));
		final var viaInline = BaseCheckTest.runCheckInline(NoUnnecessaryThisCheck.class, content);
		final var viaFile = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, path);
		assertEquals(viaFile.size(), viaInline.size());
		for (var i = 0; i < viaFile.size(); ++i) {
			assertEquals(viaFile.get(i).getLine(), viaInline.get(i).getLine());
			assertEquals(viaFile.get(i).getMessage(), viaInline.get(i).getMessage());
			assertEquals(viaFile.get(i).getSeverityLevel(), viaInline.get(i).getSeverityLevel());
		}
	}

	@Test
	public void runCheckInlineOddLengthPropertiesThrows() {
		final var ex = assertThrows(
				IllegalArgumentException.class,
				() -> BaseCheckTest.runCheckInline(PreferSpecificApiCheck.class, "class T {}", "minSdk")
		);
		assertEquals(
				"properties must be an even-length key/value sequence, got length 1",
				ex.getMessage()
		);
	}

	@Test
	public void runCheckMissingResourceThrows() {
		final var ex = assertThrows(
				NullPointerException.class,
				() -> BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, "does/not/exist.java")
		);
		assertTrue(
				ex.getMessage().endsWith("Test input file not found: does/not/exist.java"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@Test
	public void runCheckNoSlashForwardsProperties() throws Exception {
		final var below = BaseCheckTest.runCheck(PreferStaticImportCheck.class, "cases.objects.in.java", "minSdk", "29");
		final var above = BaseCheckTest.runCheck(PreferStaticImportCheck.class, "cases.objects.in.java", "minSdk", "30");
		assertEquals(6, below.size());
		assertEquals(10, above.size());
	}

	@Test
	public void runCheckNoSlashMissingResourceMessageContainsResolvedPath() {
		final var ex = assertThrows(
				NullPointerException.class,
				() -> BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, "does-not-exist.java")
		);
		assertTrue(
				ex.getMessage().endsWith("Test input file not found: nounnecessarythis/does-not-exist.java"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@Test
	public void runCheckNoSlashPathDerivesTopicPrefix() throws Exception {
		final var viaNoSlash = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, "cases.clean.java");
		final var viaSlash = BaseCheckTest.runCheck(NoUnnecessaryThisCheck.class, "nounnecessarythis/cases.clean.java");
		assertTrue(viaNoSlash.isEmpty(), "clean fixture must produce no violations");
		assertEquals(viaSlash.size(), viaNoSlash.size());
	}

	@Test
	public void runCheckOddLengthPropertiesThrows() {
		final var ex = assertThrows(
				IllegalArgumentException.class,
				() -> BaseCheckTest.runCheck(
						PreferSpecificApiCheck.class,
						"preferspecificapi/cases.in.java",
						"minSdk"
				)
		);
		assertEquals(
				"properties must be an even-length key/value sequence, got length 1",
				ex.getMessage()
		);
	}

	@Test
	public void runCheckOnFilesEmptyPathsReturnsEmpty() throws Exception {
		assertTrue(BaseCheckTest.runCheckOnFiles(PreferSpecificApiCheck.class).isEmpty());
	}

	@Test
	public void runCheckOnFilesMissingResourceThrows() {
		final var ex = assertThrows(
				NullPointerException.class,
				() -> BaseCheckTest.runCheckOnFiles(PreferSpecificApiCheck.class, "does/not/exist.java")
		);
		assertTrue(
				ex.getMessage().contains("Test input file not found: does/not/exist.java"),
				"unexpected message: " + ex.getMessage()
		);
	}

	@Test
	public void runCheckOnFilesMultiplePathsAggregates() throws Exception {
		final var path1 = "preferexactassertion/cases.junit4wildcard.in.java";
		final var path2 = "preferexactassertion/cases.junit5wildcard.in.java";
		final var first = BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, path1);
		final var second = BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, path2);
		final var combined = BaseCheckTest.runCheckOnFiles(PreferExactAssertionCheck.class, path1, path2);
		assertTrue(!first.isEmpty() && !second.isEmpty(), "fixtures must produce violations to make the aggregation assertion meaningful");
		final var expected = new ArrayList<String>();
		expected.addAll(toEventKeys(first));
		expected.addAll(toEventKeys(second));
		expected.sort(null);
		final var actual = new ArrayList<>(toEventKeys(combined));
		actual.sort(null);
		assertEquals(expected, actual);
	}

	@Test
	public void runCheckOnFilesSinglePathMatchesRunCheck() throws Exception {
		final var path = "redundantnumericsuffix/cases.in.java";
		final var viaRunCheck = BaseCheckTest.runCheck(RedundantNumericSuffixCheck.class, path);
		final var viaRunCheckOnFiles = BaseCheckTest.runCheckOnFiles(RedundantNumericSuffixCheck.class, path);
		assertEquals(viaRunCheck.size(), viaRunCheckOnFiles.size());
		for (var i = 0; i < viaRunCheck.size(); ++i) {
			assertEquals(viaRunCheck.get(i).getLine(), viaRunCheckOnFiles.get(i).getLine());
			assertEquals(viaRunCheck.get(i).getMessage(), viaRunCheckOnFiles.get(i).getMessage());
		}
	}

	@Test
	public void stripViolationMarkersOpenerMarkerStripped() {
		assertEquals("\t\t\"\"\";", BaseCheckTest.stripViolationMarkers("\t\t\"\"\"; // violation@opener: msg"));
	}
}