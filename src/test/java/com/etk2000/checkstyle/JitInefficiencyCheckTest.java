package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.api.Test;

import java.util.List;

import javax.annotation.Nonnull;

public class JitInefficiencyCheckTest {
	private static final String DIR = "jitinefficiency/";

	private static void assertViolation(@Nonnull List<AuditEvent> violations, int index, int line, @Nonnull String message) {
		final var event = violations.get(index);
		assertEquals(line, event.getLine(), "violation #" + index + " line");
		assertEquals(SeverityLevel.ERROR, event.getSeverityLevel(), "violation #" + index + " severity");
		assertEquals(message, event.getMessage(), "violation #" + index + " message");
	}

	@Test
	public void testAllocationViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyAllocationViolation.java");
		assertEquals(9, violations.size());
		var i = 0;
		assertViolation(violations, i++, 7, "Use chained '.append()' instead of string concatenation inside '.append(...)'.");
		assertViolation(violations, i++, 8, "Use chained '.append()' instead of string concatenation inside '.append(...)'.");
		assertViolation(violations, i++, 12, "Use 'String.valueOf(...)' instead of concatenating with the empty string.");
		assertViolation(violations, i++, 13, "Use 'String.valueOf(...)' instead of concatenating with the empty string.");
		assertViolation(violations, i++, 18, "Use the string literal directly instead of wrapping in 'new String(...)'.");
		assertViolation(violations, i++, 19, "Use the String variable directly instead of wrapping in 'new String(...)'.");
		assertViolation(violations, i++, 24, "Use 'StringBuilder' instead of 'StringBuffer' for non-shared local builders.");
		assertViolation(violations, i++, 30, "'toArray(new String[non-zero])' is slower than 'toArray(new String[0])' on modern JVMs.");
		assertViolation(violations, i++, 31, "'toArray(new String[non-zero])' is slower than 'toArray(new String[0])' on modern JVMs.");
	}

	@Test
	public void testBoxedConstructorViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyBoxedConstructorViolation.java");
		assertEquals(9, violations.size());
		final String[] types = {"Integer", "Long", "Boolean", "Boolean", "Double", "Float", "Short", "Byte", "Character"};
		final int[] lines = {5, 6, 7, 8, 9, 10, 11, 12, 13};
		for (var i = 0; i < types.length; ++i)
			assertViolation(violations, i, lines[i], "Use '" + types[i] + ".valueOf(...)' instead of 'new " + types[i] + "(...)'.");
	}

	@Test
	public void testClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyClean.java").isEmpty());
	}

	@Test
	public void testExplicitFormClean() throws Exception {
		assertTrue(BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyExplicitFormClean.java").isEmpty());
	}

	@Test
	public void testExplicitFormViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyExplicitFormViolation.java");
		assertEquals(24, violations.size());
		final var msg = "String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.";
		var i = 0;
		assertViolation(violations, i++, 20, msg);
		assertViolation(violations, i++, 26, msg);
		assertViolation(violations, i++, 33, msg);
		assertViolation(violations, i++, 39, msg);
		assertViolation(violations, i++, 45, msg);
		assertViolation(violations, i++, 51, msg);
		assertViolation(violations, i++, 58, msg);
		assertViolation(violations, i++, 65, msg);
		assertViolation(violations, i++, 73, msg);
		assertViolation(violations, i++, 80, msg);
		assertViolation(violations, i++, 87, msg);
		assertViolation(violations, i++, 94, msg);
		assertViolation(violations, i++, 101, msg);
		assertViolation(violations, i++, 110, msg);
		assertViolation(violations, i++, 117, msg);
		assertViolation(violations, i++, 123, msg);
		assertViolation(violations, i++, 129, msg);
		assertViolation(violations, i++, 137, msg);
		assertViolation(violations, i++, 143, msg);
		assertViolation(violations, i++, 149, msg);
		assertViolation(violations, i++, 156, msg);
		assertViolation(violations, i++, 163, msg);
		assertViolation(violations, i++, 171, msg);
		assertViolation(violations, i++, 179, msg);
	}

	@Test
	public void testLoopViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyLoopViolation.java");
		assertEquals(27, violations.size());
		var i = 0;
		assertViolation(violations, i++, 15, "Boxed accumulator 'sum' (type 'Long') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 22, "Boxed accumulator 'count' (type 'Long') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 29, "Boxed accumulator 'prod' (type 'Integer') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 36, "Boxed accumulator 'n' (type 'Long') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 43, "Boxed accumulator 'm' (type 'Long') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 50, "Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 57, "Use 'Integer.valueOf(...)' instead of 'new Integer(...)'.");
		assertViolation(violations, i++, 62, "'.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 67, "'.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 72, "Boxed accumulator 'count' (type 'Integer') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 81, "Boxed accumulator 'total' (type 'Double') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 88, "Boxed accumulator 'fSum' (type 'Float') is autoboxed in a loop, prefer the primitive type.");
		assertViolation(violations, i++, 97, "'Color.values()' allocates a new array each call; cache to a static final field outside the loop.");
		assertViolation(violations, i++, 105, "'Color.values()' allocates a new array each call; cache to a static final field outside the loop.");
		assertViolation(violations, i++, 113, "'.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 121, "'.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 129, "String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.");
		assertViolation(violations, i++, 135, "String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.");
		assertViolation(violations, i++, 142, "'Color.values()' allocates a new array each call; cache to a static final field outside the loop.");
		assertViolation(violations, i++, 149, "Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.");
		assertViolation(violations, i++, 156, "Iterate '.entrySet()' instead of '.keySet()' + '.get(...)' (avoids double lookup).");
		assertViolation(violations, i++, 164, "'.matches(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 172, "'.replaceAll(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 178, "Use an enhanced 'for' loop instead of an explicit 'Iterator.hasNext()/next()' loop.");
		assertViolation(violations, i++, 180, "'.split(...)' compiles the regex on every call; hoist a 'Pattern.compile(...)' outside the loop.");
		assertViolation(violations, i++, 188, "String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.");
		assertViolation(violations, i++, 195, "String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.");
	}

	@Test
	public void testStructuralViolations() throws Exception {
		final var violations = BaseCheckTest.runCheck(JitInefficiencyCheck.class, DIR + "InputJitInefficiencyStructuralViolation.java");
		assertEquals(8, violations.size());
		var i = 0;
		assertViolation(violations, i++, 15, "'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.");
		assertViolation(violations, i++, 21, "Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.");
		assertViolation(violations, i++, 29, "Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.");
		assertViolation(violations, i++, 36, "'DateTimeFormatter.ofPattern(...)' creates a reusable object on every call; hoist to a static final field.");
		assertViolation(violations, i++, 41, "'Pattern.compile(...)' creates a reusable object on every call; hoist to a static final field.");
		assertViolation(violations, i++, 46, "'new SimpleDateFormat(...)' creates a reusable object on every call; hoist to a static final field.");
		assertViolation(violations, i++, 51, "'new DecimalFormat(...)' creates a reusable object on every call; hoist to a static final field.");
		assertViolation(violations, i++, 56, "Avoid double-brace initialization, use a constructor or 'List.of()'/'Map.of()'/'Set.of()' instead.");
	}
}