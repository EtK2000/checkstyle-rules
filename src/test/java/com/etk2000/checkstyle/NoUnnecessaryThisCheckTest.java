package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

public class NoUnnecessaryThisCheckTest {
	@ParameterizedTest
	@ValueSource(strings = {
			"class T { int alpha = this.beta; int beta; }",
			"class T { int alpha = this.beta, beta = 2; }",
			"class T { int alpha = this.alpha + 1; }",
			"class T { { System.out.println(this.beta); } int beta; }",
			"class T { { { System.out.println(this.beta); } } int beta; }",
			"class T { { this.beta += 1; } int beta; }",
			"class T { { this.beta++; } int beta; }",
			"class T { { ++this.beta; } int beta; }",
			"class T { Runnable task = () -> System.out.println(this.beta); int beta; }",
			"class T { int size = this.items.length; int[] items = {}; }",
			"class T { Object holder = new Object() { int alpha = this.beta; int beta; }; }",
			"enum T { A; int alpha = this.beta; int beta; }",
			"class T { int alpha = this.beta; void m() {} int beta; }",
			"class T { void m() { class Local { int alpha = this.beta; int beta; } } }"
	})
	public void testForwardFieldReadKeepsThis(String source) throws Exception {
		assertEquals(List.of(), BaseCheckTest.runCheckInline(NoUnnecessaryThisCheck.class, source));
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"@SuppressWarnings(this.beta) class T { int beta; }",
			"class T { int alpha = this.BETA; static int BETA = 1; }",
			"class T { void m() { System.out.println(this.beta); } int beta; }",
			"class T { T() { System.out.println(this.beta); } int beta; }",
			"class T { int alpha = this.beta(); int beta; int beta() { return 0; } }",
			"class T { Object holder = new Object() { @Override public String toString() { return \"\" + this.beta; } int beta; }; }",
			"class T { int beta; int alpha = this.beta; class Inner { int beta; } }",
			"class Base { int beta; } class T extends Base { int alpha = this.beta; }",
			"class T { Object holder = new Object() { int beta; int alpha = this.beta; }; int beta; }"
	})
	public void testReadThatIsNotAForwardFieldReadReported(String source) throws Exception {
		final var events = BaseCheckTest.runCheckInline(NoUnnecessaryThisCheck.class, source);
		assertEquals(1, events.size(), events::toString);
		assertEquals(1, events.getFirst().getLine());
		assertEquals(source.indexOf("this.") + "this.".length(), events.getFirst().getColumn());
		assertEquals(SeverityLevel.ERROR, events.getFirst().getSeverityLevel());
		final var member = source.substring(source.indexOf("this.") + "this.".length()).split("\\W", 2)[0];
		assertEquals("Remove the unnecessary 'this.' from 'this." + member + "' (only needed when shadowing or assigning a field).", events.getFirst().getMessage());
	}
}