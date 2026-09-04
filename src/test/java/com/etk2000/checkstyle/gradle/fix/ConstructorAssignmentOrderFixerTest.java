package com.etk2000.checkstyle.gradle.fix;

import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertCaseSkip;
import static com.etk2000.checkstyle.gradle.fix.FixerTestUtil.assertSkip;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.etk2000.checkstyle.ConstructorAssignmentOrderCheck;
import com.etk2000.checkstyle.TestResources;

import org.junit.jupiter.api.Test;

import java.util.List;

public class ConstructorAssignmentOrderFixerTest {
	private static final String TOPIC = "constructorassignmentorder";

	private final CheckstyleFixer fixer = new ConstructorAssignmentOrderFixer();

	/** The check never reports a body already in order, so only a direct caller reaches this. */
	@Test
	public void testAlreadyOrderedBodyIsNoChangeResult() {
		final var lines = List.of(
				"class A {",
				"\tprivate final int a, b;",
				"",
				"\tA(int a, int b) {",
				"\t\tthis.a = a;",
				"\t\tthis.b = b;",
				"\t}",
				"}"
		);
		assertEquals(new FixResult(4, 5, lines.subList(4, 6)), fixer.fix(lines, 5, 9));
	}

	@Test
	public void testBodyWithoutAssignmentsSkips() {
		final var lines = List.of("class A {", "\tA() {", "\t\tfoo();", "\t}", "", "\tvoid foo() {", "\t}", "}");
		assertEquals(new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_TOO_FEW), fixer.fix(lines, 2, 5));
	}

	/**
	 * A field-dependency cycle needs at least three fields, which fires the check on two lines, so
	 * the standard case-slice runner (single-violation only) never drives its skip.
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

	@Test
	public void testSingleAssignmentBodySkips() {
		final var lines = List.of("class A {", "\tint a;", "\tA() {", "\t\tthis.a = 1;", "\t}", "}");
		assertEquals(new SkipResult(SkipMessages.CONSTRUCTOR_ASSIGN_SKIP_TOO_FEW), fixer.fix(lines, 3, 9));
	}
}