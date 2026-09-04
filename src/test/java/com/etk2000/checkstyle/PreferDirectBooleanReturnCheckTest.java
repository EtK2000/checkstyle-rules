package com.etk2000.checkstyle;

import org.junit.jupiter.api.Test;

/**
 * Cross-check sweeps for {@code PreferDirectBooleanReturnCheck}: the topics that write the same
 * {@code if}-with-boolean-return shapes this check collapses, in both directions.
 */
public class PreferDirectBooleanReturnCheckTest {
	private static final String TOPIC_CLEAN = "preferdirectbooleanreturn/cases.clean.java";
	private static final String TOPIC_IN = "preferdirectbooleanreturn/cases.in.java";

	@Test
	public void testControlFlowBracesResourcesAreNotCollapsible() throws Exception {
		BaseCheckTest.assertNoViolations(
				PreferDirectBooleanReturnCheck.class,
				"controlflowbraces/cases.clean.java",
				"controlflowbraces/cases.in.java"
		);
	}

	@Test
	public void testEmptyBodyResourcesAreNotCollapsible() throws Exception {
		BaseCheckTest.assertNoViolations(
				PreferDirectBooleanReturnCheck.class,
				"emptybody/cases.clean.java",
				"emptybody/cases.in.java"
		);
	}

	@Test
	public void testPreferDoWhileResourcesAreNotCollapsible() throws Exception {
		BaseCheckTest.assertNoViolations(
				PreferDirectBooleanReturnCheck.class,
				"preferdowhile/cases.clean.java",
				"preferdowhile/cases.in.java"
		);
	}

	@Test
	public void testPreferLambdaResourcesAreNotCollapsible() throws Exception {
		BaseCheckTest.assertNoViolations(PreferDirectBooleanReturnCheck.class, "preferlambda/cases.clean.java");
	}

	@Test
	public void testRedundantEqualityBranchCheckDoesNotFireOnDirectBooleanReturnResources() throws Exception {
		BaseCheckTest.assertNoViolations(RedundantEqualityBranchCheck.class, TOPIC_CLEAN, TOPIC_IN);
	}

	@Test
	public void testRedundantEqualityBranchResourcesAreNotCollapsible() throws Exception {
		BaseCheckTest.assertNoViolations(
				PreferDirectBooleanReturnCheck.class,
				"redundantequalitybranch/cases.clean.java",
				"redundantequalitybranch/cases.in.java"
		);
	}
}