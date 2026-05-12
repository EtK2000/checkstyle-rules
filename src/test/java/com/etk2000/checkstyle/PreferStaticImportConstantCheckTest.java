package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

public class PreferStaticImportConstantCheckTest {
	private static final String CINIT_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantCinitViolation.java";
	private static final String CLEAN_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantClean.java";
	private static final String CONFLICT_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantConflictViolation.java";
	private static final String MULTI_VAR_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantMultiVarViolation.java";
	private static final String MULTI_WILDCARD_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantMultiWildcardViolation.java";
	private static final String SAME_PACKAGE_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantSamePackageViolation.java";
	private static final String TYPE_SUPPRESSED_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantTypeSuppressedClean.java";
	private static final String VIOLATION_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantViolation.java";
	private static final String VISIBILITY_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantVisibilityViolation.java";
	private static final String WILDCARD_FILE = "preferstaticimportconstant/InputPreferStaticImportConstantWildcardViolation.java";

	private static void assertViolation(@Nonnull AuditEvent event, int line, @Nonnull String message) {
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals(line, event.getLine());
		assertEquals(message, event.getMessage());
	}

	@Test
	public void testCinitSplitAssignmentFires() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, CINIT_FILE);
		assertEquals(6, violations.size());
		assertViolation(violations.get(0), 6, "Replace 'FQ_CINIT' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X25' with a static import.");
		assertViolation(violations.get(1), 7, "Replace 'FQ_LHS_CINIT' alias of 'AnchorClass.X27' with a static import.");
		assertViolation(violations.get(2), 8, "Replace 'NESTED_CINIT' alias of 'AnchorClass.Inner.X16' with a static import.");
		assertViolation(violations.get(3), 9, "Replace 'PAREN_CINIT' alias of 'AnchorClass.X23' with a static import.");
		assertViolation(violations.get(4), 10, "Replace 'QUALIFIED_CINIT' alias of 'AnchorClass.X26' with a static import.");
		assertViolation(violations.get(5), 11, "Replace 'SPLIT_ALIAS' alias of 'AnchorClass.X24' with a static import.");
	}

	@Test
	public void testCleanFileHasNoViolations() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, CLEAN_FILE).isEmpty());
	}

	@Test
	public void testCompletesOnDeeplyNestedDotChain() throws Exception {
		final var chain = new StringBuilder("a0");
		for (var i = 1; i < 2000; ++i)
			chain.append(".a").append(i);
		final var source = "class T {\n\tprivate static final int X = " + chain + ";\n}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferStaticImportConstantCheck.class, source);
		assertEquals(1, violations.size());
		assertViolation(violations.getFirst(), 2, "Replace 'X' alias of '" + chain + "' with a static import.");
	}

	@Test
	public void testConflictingStaticImportStillFires() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, CONFLICT_FILE);
		assertEquals(1, violations.size());
		assertViolation(violations.getFirst(), 9, "Replace 'X23_FROM_ANCHOR' alias of 'AnchorClass.X23' with a static import.");
	}

	@Test
	public void testMultipleWildcardImportsFireFromCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, MULTI_WILDCARD_FILE);
		assertEquals(1, violations.size());
		assertViolation(violations.getFirst(), 7, "Replace 'MULTI_WILDCARD_X' alias of 'AnchorClass.MULTI_WILDCARD_X' with a static import.");
	}

	@Test
	public void testMultiVariableDeclarationFiresPerAliasVariable() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, MULTI_VAR_FILE);
		assertEquals(14, violations.size());
		assertViolation(violations.get(0), 8, "Replace 'FQ_A' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X11' with a static import.");
		assertViolation(violations.get(1), 9, "Replace 'FQ_B' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X12' with a static import.");
		assertViolation(violations.get(2), 11, "Replace 'MIXED_B' alias of 'AnchorClass.X5' with a static import.");
		assertViolation(violations.get(3), 12, "Replace 'MULTI_A' alias of 'AnchorClass.X3' with a static import.");
		assertViolation(violations.get(4), 13, "Replace 'MULTI_B' alias of 'AnchorClass.X4' with a static import.");
		assertViolation(violations.get(5), 14, "Replace 'PAREN_A' alias of 'AnchorClass.X9' with a static import.");
		assertViolation(violations.get(6), 15, "Replace 'PAREN_B' alias of 'AnchorClass.X10' with a static import.");
		assertViolation(violations.get(7), 16, "Replace 'SINGLE_A' alias of 'AnchorClass.X1' with a static import.");
		assertViolation(violations.get(8), 16, "Replace 'SINGLE_B' alias of 'AnchorClass.X2' with a static import.");
		assertViolation(violations.get(9), 17, "Replace 'TRI_A' alias of 'AnchorClass.X6' with a static import.");
		assertViolation(violations.get(10), 18, "Replace 'TRI_B' alias of 'AnchorClass.X7' with a static import.");
		assertViolation(violations.get(11), 19, "Replace 'TRI_C' alias of 'AnchorClass.X8' with a static import.");
		assertViolation(violations.get(12), 20, "Replace 'MAP_A' alias of 'AnchorClass.X13' with a static import.");
		assertViolation(violations.get(13), 21, "Replace 'MAP_B' alias of 'AnchorClass.X14' with a static import.");
	}

	@Test
	public void testSamePackageResolvableFires() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, SAME_PACKAGE_FILE);
		assertEquals(1, violations.size());
		assertViolation(violations.getFirst(), 6, "Replace 'MAX' alias of 'InputPreferStaticImportConstantSamePackageHelper.MAX' with a static import.");
	}

	@Test
	public void testTypeLevelSuppressionPropagatesToNestedTypes() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, TYPE_SUPPRESSED_FILE).isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, VIOLATION_FILE);
		assertEquals(15, violations.size());

		assertViolation(violations.get(0), 11, "Replace 'ENUM_FIELD' alias of 'AnchorClass.ENUM_FIELD' with a static import.");
		assertViolation(violations.get(1), 15, "Replace 'RECORD_FIELD' alias of 'AnchorClass.RECORD_FIELD' with a static import.");
		assertViolation(violations.get(2), 19, "Replace 'NESTED' alias of 'AnchorClass.NESTED' with a static import.");
		assertViolation(violations.get(3), 22, "Replace 'DEEPLY_NESTED_PARENS_ALIAS' alias of 'AnchorClass.X23' with a static import.");
		assertViolation(violations.get(4), 23, "Replace 'FQ_ALIAS' alias of 'com.etk2000.checkstyle.inputs.preferstaticimportconstant.support.AnchorClass.X15' with a static import.");
		assertViolation(violations.get(5), 24, "Replace 'INT_ALIAS' alias of 'AnchorClass.INT_ALIAS' with a static import.");
		assertViolation(violations.get(6), 25, "Replace 'lowercase_alias' alias of 'AnchorClass.X7' with a static import.");
		assertViolation(violations.get(7), 26, "Replace 'NESTED_CLASS_ALIAS' alias of 'AnchorClass.Inner.X16' with a static import.");
		assertViolation(violations.get(8), 27, "Replace 'PARENTHESIZED_ALIAS' alias of 'AnchorClass.X11' with a static import.");
		assertViolation(violations.get(9), 28, "Replace 'RENAMED' alias of 'AnchorClass.X6' with a static import.");
		assertViolation(violations.get(10), 30, "Replace 'ANNOTATED_ALIAS' alias of 'AnchorClass.ANNOTATED_ALIAS' with a static import.");
		assertViolation(violations.get(11), 32, "Replace 'OTHER_SUPPRESS_KEY' alias of 'AnchorClass.OTHER_SUPPRESS_KEY' with a static import.");
		assertViolation(violations.get(12), 33, "Replace 'ARRAY_ALIAS' alias of 'AnchorClass.ARRAY_ALIAS' with a static import.");
		assertViolation(violations.get(13), 34, "Replace 'GENERIC_ALIAS' alias of 'AnchorClass.GENERIC_ALIAS' with a static import.");
		assertViolation(violations.get(14), 35, "Replace 'STRING_ALIAS' alias of 'AnchorClass.STRING_ALIAS' with a static import.");
	}

	@Test
	public void testVisibilityVariantsFireRegardlessOfVisibility() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, VISIBILITY_FILE);
		assertEquals(3, violations.size());
		assertViolation(violations.get(0), 6, "Replace 'PACKAGE_PRIVATE_ALIAS' alias of 'AnchorClass.X1' with a static import.");
		assertViolation(violations.get(1), 7, "Replace 'PROTECTED_ALIAS' alias of 'AnchorClass.X2' with a static import.");
		assertViolation(violations.get(2), 8, "Replace 'PUBLIC_ALIAS' alias of 'AnchorClass.X3' with a static import.");
	}

	@Test
	public void testWildcardImportResolvesAndFires() throws Exception {
		final var violations = BaseCheckTest.runCheck(PreferStaticImportConstantCheck.class, WILDCARD_FILE);
		assertEquals(1, violations.size());
		assertViolation(violations.getFirst(), 6, "Replace 'WILDCARD_X' alias of 'AnchorClass.WILDCARD_X' with a static import.");
	}
}