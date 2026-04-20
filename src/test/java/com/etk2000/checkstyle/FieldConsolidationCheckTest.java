package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class FieldConsolidationCheckTest {
	private static final String DIR = "fieldconsolidation/";
	private static final String SORT_DIR = "fieldsorting/";

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, DIR + "InputFieldConsolidationClean.java").isEmpty());
	}

	@Test
	public void testCrossCheckFieldSortingCleanWithOurCheck() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingClean.java");
		assertEquals(1, violations.size());
		assertEquals(122, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Fields 'indices' and 'elements' (type 'int[]') should be declared on one line.", violations.getFirst().getMessage());
	}

	@Test
	public void testCrossCheckFieldSortingViolationsWithOurCheck() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingAnonClassViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingArrayViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingChunkViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingDependencyViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingEnumConstantViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingEnumSameLineViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingNameViolation.java").isEmpty());
		assertTrue(BaseCheckTest.runCheck(FieldConsolidationCheck.class, SORT_DIR + "InputFieldSortingTypeViolation.java").isEmpty());
	}

	@Test
	public void testCrossCheckOurCleanWithFieldSorting() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldConsolidationClean.java");
		assertTrue(
				violations.isEmpty(),
				() -> violations.stream().map(v -> v.getLine() + ": " + v.getMessage()).toList().toString()
		);
	}

	@Test
	public void testCrossCheckOurViolationWithFieldSorting() throws Exception {
		assertTrue(BaseCheckTest.runCheck(FieldSortingCheck.class, DIR + "InputFieldConsolidationViolation.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(FieldConsolidationCheck.class, DIR + "InputFieldConsolidationViolation.java");
		assertEquals(42, violations.size());
		var i = 0;

		assertEquals(14, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(20, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'second' and 'first' (type 'String') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(26, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'b' and 'a' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'c' and 'b' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(43, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(51, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(59, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(67, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(73, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'shared' and 'global' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(79, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(85, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'String[][]') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(91, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'words' and 'names' (type 'List<String>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(97, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(108, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(114, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'visible' and 'active' (type 'boolean') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(120, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'remaining' and 'elapsed' (type 'long') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(126, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaMap' and 'alphaMap' (type 'Map<String,List<Integer>>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(132, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaList' and 'alphaList' (type 'List<? extends Number>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(141, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(147, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaSuper' and 'alphaSuper' (type 'List<? super Integer>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(153, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaUnbounded' and 'alphaUnbounded' (type 'List<?>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(159, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int[]') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(165, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaByte' and 'alphaByte' (type 'byte') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(167, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaChar' and 'alphaChar' (type 'char') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(169, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaDouble' and 'alphaDouble' (type 'double') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(171, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaFloat' and 'alphaFloat' (type 'float') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(173, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'betaShort' and 'alphaShort' (type 'short') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(180, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(189, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(195, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'second' and 'first' (type 'String') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(201, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'java.util.List<String>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(211, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(225, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(237, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(245, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'List<? extends @ViolationTypeAnn Number>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(253, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(261, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(267, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'List<String[]>') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(275, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(283, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'beta' and 'alpha' (type 'int') should be declared on one line.", violations.get(i++).getMessage());

		assertEquals(289, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Fields 'c' and 'b' (type 'int') should be declared on one line.", violations.get(i++).getMessage());
	}
}