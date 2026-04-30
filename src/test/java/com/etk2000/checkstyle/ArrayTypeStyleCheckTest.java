package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

import javax.annotation.Nonnull;

public class ArrayTypeStyleCheckTest {
	private static final String DIR = "arraytypestyle/";

	private static void assertViolation(@Nonnull AuditEvent event, int line, @Nonnull String name) {
		assertEquals(line, event.getLine());
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel());
		assertEquals("Array brackets must be on the type, not after '" + name + "'.", event.getMessage());
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(ArrayTypeStyleCheck.class, DIR + "InputArrayTypeStyleClean.java").isEmpty());
	}

	@Test
	public void testViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(ArrayTypeStyleCheck.class, DIR + "InputArrayTypeStyleViolation.java");
		assertEquals(20, violations.size());
		var i = 0;
		assertViolation(violations.get(i++), 7, "comp");
		assertViolation(violations.get(i++), 9, "sb");
		assertViolation(violations.get(i++), 12, "annotatedField");
		assertViolation(violations.get(i++), 14, "ib");
		assertViolation(violations.get(i++), 15, "ic");
		assertViolation(violations.get(i++), 16, "gs");
		assertViolation(violations.get(i++), 18, "ctorParam");
		assertViolation(violations.get(i++), 20, "p");
		assertViolation(violations.get(i++), 25, "lc");
		assertViolation(violations.get(i++), 29, "c");
		assertViolation(violations.get(i++), 33, "methodMixedReturn");
		assertViolation(violations.get(i++), 37, "methodReturnCStyle");
		assertViolation(violations.get(i++), 41, "methodReturnWithThrows");
		assertViolation(violations.get(i++), 45, "b");
		assertViolation(violations.get(i++), 50, "mx");
		assertViolation(violations.get(i++), 55, "gamma");
		assertViolation(violations.get(i++), 60, "zeta");
		assertViolation(violations.get(i++), 65, "alpha");
		assertViolation(violations.get(i++), 65, "beta");
		assertViolation(violations.get(i++), 70, "lb");
	}
}