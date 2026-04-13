package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import javax.annotation.Nonnull;

public class PreferStaticImportCheckTest {
	private static final String DIR = "preferstaticimport/";
	private static final String CHAINED_FILE = DIR + "InputPreferStaticImportChainedViolation.java";
	private static final String CLEAN_FILE = DIR + "InputPreferStaticImportClean.java";
	private static final String COLLECTORS_FILE = DIR + "InputPreferStaticImportCollectorsViolation.java";
	private static final String CONFLICTS_FILE = DIR + "InputPreferStaticImportConflictsClean.java";
	private static final String EXPLICIT_SHADOW_FILE = DIR + "InputPreferStaticImportExplicitShadowClean.java";
	private static final String IMPORT_CONFLICT_FILE = DIR + "InputPreferStaticImportImportConflictClean.java";
	private static final String NESTED_TYPE_SHADOWS_FILE = DIR + "InputPreferStaticImportNestedTypeShadowsClean.java";
	private static final String OBJECTS_FILE = DIR + "InputPreferStaticImportObjectsViolation.java";
	private static final String PREDICATE_FILE = DIR + "InputPreferStaticImportPredicateViolation.java";
	private static final String SAME_FILE_SHADOW_FILE = DIR + "InputPreferStaticImportSameFileShadowClean.java";
	private static final String SDK = String.valueOf(Integer.MAX_VALUE);
	private static final String SIBLING_SHADOW_FILE = DIR + "siblingshadow/InputPreferStaticImportSiblingShadowClean.java";
	private static final String WILDCARD_FILE = DIR + "InputPreferStaticImportWildcardViolation.java";

	private static void assertEveryViolation(@Nonnull List<AuditEvent> violations, @Nonnull String expectedMessage) {
		for (var v : violations) {
			assertEquals(SeverityLevel.ERROR, v.getSeverityLevel());
			assertEquals(expectedMessage, v.getMessage());
		}
	}

	@Nonnull
	private static List<AuditEvent> filterByMessageContains(@Nonnull List<AuditEvent> violations, @Nonnull String needle) {
		return violations.stream().filter(v -> v.getMessage().contains(needle)).toList();
	}

	@Test
	public void testChainedCallsAreEachCounted() throws Exception {
		// `Predicate.not(Objects.requireNonNull(x))` x2 -> 2 of each candidate -> 4 violations.
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, CHAINED_FILE, "minSdk", SDK);
		assertEquals(4, violations.size());
		assertEquals(2, filterByMessageContains(violations, "Predicate.not").size());
		assertEveryViolation(filterByMessageContains(violations, "Predicate.not"), "Replace 'Predicate.not' with a static import of 'not'.");
		assertEquals(2, filterByMessageContains(violations, "Objects.requireNonNull").size());
		assertEveryViolation(filterByMessageContains(violations, "Objects.requireNonNull"), "Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.");
	}

	@Test
	public void testCleanFileHasNoViolations() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, CLEAN_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testCollectorsCarvedOutToListNotFlaggedEvenAtMinOccurrencesOne() throws Exception {
		// the clean file uses Collectors.toList twice and Collectors.toUnmodifiableList once.
		// even with minOccurrences=1, those carved-out methods must not fire.
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, CLEAN_FILE, "minSdk", SDK, "minOccurrences", "1");
		assertTrue(filterByMessageContains(violations, "Collectors").isEmpty());
	}

	@Test
	public void testCollectorsViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, COLLECTORS_FILE, "minSdk", SDK);
		assertEquals(6, violations.size());
		final var joining = filterByMessageContains(violations, "joining");
		assertEquals(2, joining.size());
		assertEveryViolation(joining, "Replace 'Collectors.joining' with a static import of 'joining'.");
		final var grouping = filterByMessageContains(violations, "groupingBy");
		assertEquals(2, grouping.size());
		assertEveryViolation(grouping, "Replace 'Collectors.groupingBy' with a static import of 'groupingBy'.");
		final var toSet = filterByMessageContains(violations, "toSet");
		assertEquals(2, toSet.size());
		assertEveryViolation(toSet, "Replace 'Collectors.toSet' with a static import of 'toSet'.");
	}

	@Test
	public void testConflictsCleanLocalMethodShadows() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, CONFLICTS_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testConflictsCleanStaticImportFromDifferentOwnerShadows() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, IMPORT_CONFLICT_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testExplicitImportShadowCleanViaWildcard() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, EXPLICIT_SHADOW_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testMinOccurrencesOneFlagsSingleUse() throws Exception {
		// the clean file has 1 Predicate.not + 1 Objects.requireNonNull. At minOccurrences=1 both fire.
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, CLEAN_FILE, "minSdk", SDK, "minOccurrences", "1");
		assertEquals(2, violations.size());
		final var pred = filterByMessageContains(violations, "Predicate.not");
		assertEquals(1, pred.size());
		assertEquals("Replace 'Predicate.not' with a static import of 'not'.", pred.getFirst().getMessage());
		final var obj = filterByMessageContains(violations, "Objects.requireNonNull");
		assertEquals(1, obj.size());
		assertEquals("Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.", obj.getFirst().getMessage());
	}

	@CsvSource({
			"32, Predicate.not",
			"23, Collectors.toSet",
			"23, Collectors.groupingBy",
			"23, Collectors.joining"
	})
	@ParameterizedTest
	public void testMinSdkBelowSuppresses(String minSdk, String forbidden) throws Exception {
		final var file = forbidden.startsWith("Predicate") ? PREDICATE_FILE : COLLECTORS_FILE;
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, file, "minSdk", minSdk);
		assertTrue(filterByMessageContains(violations, forbidden).isEmpty());
	}

	@Test
	public void testNestedTypeShadowsCleanCoverAllTokenTypes() throws Exception {
		// One file, four top-level classes each hosting a nested Predicate of a different
		// token type (annotation, enum, interface, record). Verifies walkForLocalShadows
		// treats each TokenType as a shadow candidate.
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, NESTED_TYPE_SHADOWS_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testObjectsBoundaryAtMinSdk29SuppressesElseButNotRequireNonNull() throws Exception {
		// requireNonNullElse[Get] gated at API 30; isNull/nonNull/requireNonNull gated at API 19.
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, OBJECTS_FILE, "minSdk", "29");
		assertTrue(filterByMessageContains(violations, "requireNonNullElse").isEmpty());
		assertEquals(2, filterByMessageContains(violations, "Objects.requireNonNull'").size());
		assertEquals(2, filterByMessageContains(violations, "Objects.isNull").size());
		assertEquals(2, filterByMessageContains(violations, "Objects.nonNull").size());
	}

	@Test
	public void testObjectsBoundaryAtMinSdk30FiresAll() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, OBJECTS_FILE, "minSdk", "30");
		assertEquals(10, violations.size());
	}

	@Test
	public void testObjectsViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, OBJECTS_FILE, "minSdk", SDK);
		assertEquals(10, violations.size());
		final var isNull = filterByMessageContains(violations, "Objects.isNull");
		assertEquals(2, isNull.size());
		assertEveryViolation(isNull, "Replace 'Objects.isNull' with a static import of 'isNull'.");
		final var nonNull = filterByMessageContains(violations, "Objects.nonNull");
		assertEquals(2, nonNull.size());
		assertEveryViolation(nonNull, "Replace 'Objects.nonNull' with a static import of 'nonNull'.");
		final var require = filterByMessageContains(violations, "Objects.requireNonNull'");
		assertEquals(2, require.size());
		assertEveryViolation(require, "Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.");
		final var requireElse = filterByMessageContains(violations, "Objects.requireNonNullElse'");
		assertEquals(2, requireElse.size());
		assertEveryViolation(requireElse, "Replace 'Objects.requireNonNullElse' with a static import of 'requireNonNullElse'.");
		final var requireElseGet = filterByMessageContains(violations, "Objects.requireNonNullElseGet");
		assertEquals(2, requireElseGet.size());
		assertEveryViolation(requireElseGet, "Replace 'Objects.requireNonNullElseGet' with a static import of 'requireNonNullElseGet'.");
	}

	@Test
	public void testPredicateViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, PREDICATE_FILE, "minSdk", SDK);
		assertEquals(2, violations.size());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(9, violations.get(0).getLine());
		assertEquals("Replace 'Predicate.not' with a static import of 'not'.", violations.get(0).getMessage());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(10, violations.get(1).getLine());
		assertEquals("Replace 'Predicate.not' with a static import of 'not'.", violations.get(1).getMessage());
	}

	@Test
	public void testSameFileShadowCleanViaWildcard() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, SAME_FILE_SHADOW_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testSiblingShadowCleanViaWildcard() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportCheck.class, SIBLING_SHADOW_FILE, "minSdk", SDK).isEmpty());
	}

	@Test
	public void testWildcardFiresTwoUsesForEachCandidate() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportCheck.class, WILDCARD_FILE, "minSdk", SDK);
		assertEquals(6, violations.size());
		final var pred = filterByMessageContains(violations, "Predicate.not");
		assertEquals(2, pred.size());
		assertEveryViolation(pred, "Replace 'Predicate.not' with a static import of 'not'.");
		final var objs = filterByMessageContains(violations, "Objects.requireNonNull");
		assertEquals(2, objs.size());
		assertEveryViolation(objs, "Replace 'Objects.requireNonNull' with a static import of 'requireNonNull'.");
		final var coll = filterByMessageContains(violations, "Collectors.toSet");
		assertEquals(2, coll.size());
		assertEveryViolation(coll, "Replace 'Collectors.toSet' with a static import of 'toSet'.");
	}
}