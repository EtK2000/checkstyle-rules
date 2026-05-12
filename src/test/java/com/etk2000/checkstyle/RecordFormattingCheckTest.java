package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

public class RecordFormattingCheckTest {
	private static final String DIR = "recordformatting/";
	private static final String MSG_BRACES_EMPTY_BODY_SPLIT = "Empty record body must be '{}' on one line.";
	private static final String MSG_BRACES_NON_EMPTY_SAME_LINE = "Non-empty record body must place '}' on its own line.";
	private static final String MSG_COMPONENT_MULTI_PER_LINE = "Each record component must be on its own line.";
	private static final String MSG_COMPONENT_ON_CLOSING_PAREN = "Last record component must not share the line with the closing paren.";
	private static final String MSG_COMPONENT_ON_OPENING_PAREN = "First record component must not share the line with the opening paren.";
	private static final String MSG_OPEN_BRACE_BAD_SPACING = "Record opening brace must have exactly one space before it.";
	private static final String MSG_OPEN_BRACE_NOT_ON_ANCHOR = "Record opening brace must be on the same line as the closing paren (or implements clause).";

	@Test
	public void testBracesViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				RecordFormattingCheck.class,
				DIR + "InputRecordFormattingBracesViolation.java"
		);
		assertEquals(11, violations.size());

		assertEquals(6, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_BAD_SPACING, violations.get(0).getMessage());

		assertEquals(8, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_BAD_SPACING, violations.get(1).getMessage());

		assertEquals(10, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_BAD_SPACING, violations.get(2).getMessage());

		assertEquals(12, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_BAD_SPACING, violations.get(3).getMessage());

		assertEquals(17, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_NOT_ON_ANCHOR, violations.get(4).getMessage());

		assertEquals(20, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_NOT_ON_ANCHOR, violations.get(5).getMessage());

		assertEquals(25, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals(MSG_BRACES_EMPTY_BODY_SPLIT, violations.get(6).getMessage());

		assertEquals(27, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals(MSG_BRACES_NON_EMPTY_SAME_LINE, violations.get(7).getMessage());

		assertEquals(29, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_BAD_SPACING, violations.get(8).getMessage());

		assertEquals(32, violations.get(9).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(9).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_NOT_ON_ANCHOR, violations.get(9).getMessage());

		assertEquals(36, violations.get(10).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(10).getSeverityLevel());
		assertEquals(MSG_OPEN_BRACE_NOT_ON_ANCHOR, violations.get(10).getMessage());
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(RecordFormattingCheck.class, DIR + "InputRecordFormattingClean.java").isEmpty());
	}

	@Test
	public void testComponentsViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				RecordFormattingCheck.class,
				DIR + "InputRecordFormattingComponentsViolation.java"
		);
		assertEquals(9, violations.size());

		assertEquals(4, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_OPENING_PAREN, violations.get(0).getMessage());

		assertEquals(5, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_CLOSING_PAREN, violations.get(1).getMessage());

		assertEquals(7, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_OPENING_PAREN, violations.get(2).getMessage());

		assertEquals(15, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_CLOSING_PAREN, violations.get(3).getMessage());

		assertEquals(19, violations.get(4).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(4).getSeverityLevel());
		assertEquals(MSG_COMPONENT_MULTI_PER_LINE, violations.get(4).getMessage());

		assertEquals(24, violations.get(5).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(5).getSeverityLevel());
		assertEquals(MSG_COMPONENT_MULTI_PER_LINE, violations.get(5).getMessage());

		assertEquals(24, violations.get(6).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(6).getSeverityLevel());
		assertEquals(MSG_COMPONENT_MULTI_PER_LINE, violations.get(6).getMessage());

		assertEquals(27, violations.get(7).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(7).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_OPENING_PAREN, violations.get(7).getMessage());

		assertEquals(28, violations.get(8).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(8).getSeverityLevel());
		assertEquals(MSG_COMPONENT_ON_CLOSING_PAREN, violations.get(8).getMessage());
	}
}