package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;

import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck;
import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

public class ConstructorAssignmentOrderFixerTest {
	private static final String TOPIC = "constructorassignmentorder";

	private final CheckstyleFixer fixer = new ConstructorAssignmentOrderFixer();

	/**
	 * A field-dependency cycle needs at least three fields, which fires the check on two lines, so
	 * the standard case-slice runner (single-violation only) never drives its skip; this pins the
	 * cycle SkipResult reason on the {@code dependency_cycle} slice directly.
	 */
	@Test
	public void testDependencyCycleSkips() throws Exception {
		final var slice = TestResources.loadCaseSlice(TOPIC, "dependency_cycle");
		assertCaseSkip(
				ConstructorAssignmentOrderCheck.class,
				fixer,
				TOPIC,
				"dependency_cycle",
				slice.inputLines(),
				SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_CYCLE
		);
	}

	@Test
	public void testNoBodyEnd() throws Exception {
		assertSkip(fixer, TOPIC, "no_body_end");
	}
}