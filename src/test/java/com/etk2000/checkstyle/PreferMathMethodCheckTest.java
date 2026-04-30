package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

public class PreferMathMethodCheckTest {
	private static final String DIR = "mathmethod/";

	private static Stream<Arguments> ifViolationProvider() {
		return Stream.of(
				Arguments.of(9, "Use 'Math.abs(a)' here."),
				Arguments.of(18, "Use 'Math.abs(a)' here."),
				Arguments.of(27, "Use 'Math.abs(a)' here."),
				Arguments.of(36, "Use 'Math.abs(a)' here."),
				Arguments.of(45, "Use 'Math.abs(a)' here."),
				Arguments.of(54, "Use 'Math.abs(a)' here."),
				Arguments.of(63, "Use 'Math.abs(a)' here."),
				Arguments.of(72, "Use 'Math.abs(a)' here."),
				Arguments.of(81, "Use 'Math.abs(a)' here."),
				Arguments.of(89, "Use 'Math.abs(a)' here."),
				Arguments.of(97, "Use 'Math.abs(a)' here."),
				Arguments.of(104, "Use 'Math.abs(a)' here."),
				Arguments.of(111, "Use 'Math.abs(a)' here."),
				Arguments.of(118, "Use 'Math.abs(a)' here."),
				Arguments.of(125, "Use 'Math.abs(a)' here."),
				Arguments.of(132, "Use 'Math.abs(a)' here."),
				Arguments.of(139, "Use 'Math.abs(a)' here."),
				Arguments.of(146, "Use 'Math.abs(a)' here."),
				Arguments.of(153, "Use 'Math.abs(a)' here."),
				Arguments.of(159, "Use 'Math.abs(a)' here."),
				Arguments.of(165, "Use 'Math.abs(a)' here."),
				Arguments.of(171, "Use 'Math.abs(a)' here."),
				Arguments.of(177, "Use 'Math.abs(a)' here."),
				Arguments.of(183, "Use 'Math.abs(a)' here."),
				Arguments.of(189, "Use 'Math.abs(a)' here."),
				Arguments.of(195, "Use 'Math.abs(a)' here."),
				Arguments.of(202, "Use 'Math.min(a, b)' here."),
				Arguments.of(210, "Use 'Math.max(a, b)' here."),
				Arguments.of(218, "Use 'Math.max(a, b)' here."),
				Arguments.of(226, "Use 'Math.max(a, b)' here."),
				Arguments.of(234, "Use 'Math.max(a, b)' here."),
				Arguments.of(243, "Use 'Math.max(a, b)' here."),
				Arguments.of(252, "Use 'Math.max(a, b)' here."),
				Arguments.of(261, "Use 'Math.max(a, b)' here."),
				Arguments.of(270, "Use 'Math.max(a, b)' here."),
				Arguments.of(279, "Use 'Math.max(--a, b)' here."),
				Arguments.of(288, "Use 'Math.max(++a, b)' here."),
				Arguments.of(297, "Use 'Math.max(a, ++b)' here."),
				Arguments.of(305, "Use 'Math.max(a, b)' here."),
				Arguments.of(313, "Use 'Math.max(a, b)' here."),
				Arguments.of(320, "Use 'Math.max(a, b)' here."),
				Arguments.of(327, "Use 'Math.max(a, b)' here."),
				Arguments.of(334, "Use 'Math.max(a, b)' here."),
				Arguments.of(341, "Use 'Math.max(++a, b)' here."),
				Arguments.of(349, "Use 'Math.max(a, b)' here."),
				Arguments.of(356, "Use 'Math.max(a, b)' here."),
				Arguments.of(367, "Use 'Math.max(a, b)' here."),
				Arguments.of(374, "Use 'Math.max(a, b)' here."),
				Arguments.of(382, "Use 'Math.max(a, b)' here."),
				Arguments.of(388, "Use 'Math.max(a, b)' here."),
				Arguments.of(394, "Use 'Math.max(a, b)' here."),
				Arguments.of(400, "Use 'Math.max(a, b)' here."),
				Arguments.of(407, "Use 'Math.min(a, b)' here."),
				Arguments.of(416, "Use 'Math.min(a, b)' here."),
				Arguments.of(425, "Use 'Math.min(a, b)' here."),
				Arguments.of(434, "Use 'Math.min(a, b)' here."),
				Arguments.of(442, "Use 'Math.min(a, b)' here."),
				Arguments.of(449, "Use 'Math.min(a, b)' here."),
				Arguments.of(456, "Use 'Math.min(a, b)' here."),
				Arguments.of(463, "Use 'Math.min(a, b)' here."),
				Arguments.of(470, "Use 'Math.min(a, b)' here."),
				Arguments.of(478, "Use 'Math.min(a, b)' here."),
				Arguments.of(484, "Use 'Math.min(a, b)' here."),
				Arguments.of(490, "Use 'Math.min(a, b)' here."),
				Arguments.of(496, "Use 'Math.min(a, b)' here.")
		);
	}

	@Test
	public void testClampGatedByMinSdk() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodClampViolation.java",
				"minSdk",
				"34"
		).isEmpty());
	}

	@Test
	public void testClampViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodClampViolation.java",
				"minSdk",
				"35"
		);
		assertEquals(4, violations.size());

		assertEquals(5, violations.get(0).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(0).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(lo, Math.min(hi, value))'.", violations.get(0).getMessage());

		assertEquals(9, violations.get(1).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(1).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.max(Math.min(hi, value), lo)'.", violations.get(1).getMessage());

		assertEquals(13, violations.get(2).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(2).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(hi, Math.max(lo, value))'.", violations.get(2).getMessage());

		assertEquals(17, violations.get(3).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(3).getSeverityLevel());
		assertEquals("Use 'Math.clamp(value, lo, hi)' instead of 'Math.min(Math.max(lo, value), hi)'.", violations.get(3).getMessage());
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(PreferMathMethodCheck.class, DIR + "InputPreferMathMethodClean.java").isEmpty());
	}

	@MethodSource("ifViolationProvider")
	@ParameterizedTest
	public void testIfViolations(int expectedLine, String expectedMessage) throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodIfViolation.java"
		);
		final var match = violations.stream()
				.filter(v -> v.getLine() == expectedLine)
				.findFirst()
				.orElseThrow(() -> new AssertionError("No violation at line " + expectedLine));
		assertEquals(SeverityLevel.ERROR, match.getSeverityLevel());
		assertEquals(expectedMessage, match.getMessage());
	}

	@Test
	public void testIfViolationsCount() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodIfViolation.java"
		);
		assertEquals(65, violations.size());
	}

	@Test
	public void testIfWithBracedSingleStatementSlistFires() throws Exception {
		final var source = "class T {\n"
				+ "\tint f(int a, int b) {\n"
				+ "\t\tif (a > b) {\n"
				+ "\t\t\treturn a;\n"
				+ "\t\t}\n"
				+ "\t\telse {\n"
				+ "\t\t\treturn b;\n"
				+ "\t\t}\n"
				+ "\t}\n"
				+ "}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferMathMethodCheck.class, source);
		assertEquals(1, violations.size());
		assertEquals(3, violations.getFirst().getLine());
		assertEquals(SeverityLevel.ERROR, violations.getFirst().getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' here.", violations.getFirst().getMessage());
	}

	@Test
	public void testIfWithEmptySlistDoesNotFire() throws Exception {
		final var source = "class T {\n"
				+ "\tint f(int a, int b) {\n"
				+ "\t\tif (a > b) {}\n"
				+ "\t\telse {\n"
				+ "\t\t\treturn a;\n"
				+ "\t\t}\n"
				+ "\t\treturn b;\n"
				+ "\t}\n"
				+ "}\n";
		final var violations = BaseCheckTest.runCheckInline(PreferMathMethodCheck.class, source);
		assertTrue(violations.isEmpty());
	}

	@Test
	public void testRedundantEqualityBranchCheckDoesNotFireOnMathResources() throws Exception {
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferMathMethodIfViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferMathMethodTernaryViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferMathMethodClampViolation.java"
		).isEmpty());
		assertTrue(BaseCheckTest.runCheck(
				RedundantEqualityBranchCheck.class,
				DIR + "InputPreferMathMethodClean.java"
		).isEmpty());
	}

	@Test
	public void testTernaryViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(
				PreferMathMethodCheck.class,
				DIR + "InputPreferMathMethodTernaryViolation.java"
		);
		assertEquals(23, violations.size());
		var i = 0;

		assertEquals(7, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a >= 0 ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(11, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 >= a ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(15, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a > 0 ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(19, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 > a ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(23, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a <= 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(27, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 <= a ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(31, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(35, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of '0 < a ? a : -a'.", violations.get(i++).getMessage());

		assertEquals(39, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(arr[0])' instead of 'arr[0] < 0 ? -arr[0] : arr[0]'.", violations.get(i++).getMessage());

		assertEquals(43, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.", violations.get(i++).getMessage());

		assertEquals(47, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a >= b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(51, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(55, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a <= b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(59, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, b)' instead of 'a < b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(63, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(arr[0], arr[1])' instead of 'arr[0] > arr[1] ? arr[0] : arr[1]'.", violations.get(i++).getMessage());

		assertEquals(67, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a.x, b.x)' instead of 'a.x > b.x ? a.x : b.x'.", violations.get(i++).getMessage());

		assertEquals(71, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(a, 5)' instead of 'a > 5 ? a : 5'.", violations.get(i++).getMessage());

		assertEquals(75, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(--a, b)' instead of '--a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(79, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.max(++a, b)' instead of '++a > b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(83, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a >= b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(87, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a > b ? b : a'.", violations.get(i++).getMessage());

		assertEquals(91, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a <= b ? a : b'.", violations.get(i++).getMessage());

		assertEquals(95, violations.get(i).getLine());
		assertEquals(SeverityLevel.ERROR, violations.get(i).getSeverityLevel());
		assertEquals("Use 'Math.min(a, b)' instead of 'a < b ? a : b'.", violations.get(i++).getMessage());
	}
}