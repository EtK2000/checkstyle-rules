package com.etk2000.checkstyle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.puppycrawl.tools.checkstyle.JavaParser;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileText;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.File;
import java.util.List;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

public class NoUnnecessaryThisCheckTest {
	@CheckReturnValue
	@Nonnull
	private static DetailAST parseInline(@Nonnull String source) throws Exception {
		return JavaParser.parseFileText(
				new FileText(new File("Inline.java"), List.of(source)), JavaParser.Options.WITHOUT_COMMENTS
		);
	}

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

	/**
	 * The last two rows are the only drivers of {@code isForwardFieldRead}'s {@code METHOD_CALL} and
	 * later-static exits <em>through the seam</em>. The fixer never calls {@code visitToken}, so a
	 * misclassification there would leave the check reporting a violation the fixer then refuses,
	 * which no slice can catch.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"class T { int beta; int alpha = this.beta; }",
			"class T { int beta; { System.out.println(this.beta); } }",
			"class T { int alpha = this.beta(); int beta; int beta() { return 0; } }",
			"class T { int alpha = this.beta; static int beta = 1; }"
	})
	public void testThisAtClassifiesAnEarlierFieldRead(String source) throws Exception {
		final var target = NoUnnecessaryThisCheck.thisAt(
				parseInline(source), 0, source.indexOf("this.") + "this".length()
		);
		assertNotNull(target);
		assertEquals("beta", target.ident().getText());
	}

	/**
	 * The fixer reaches the forward-read refusal through {@code thisAt} rather than through the
	 * visitor, and misclassifying one emits a simple name reading a field declared below it, which
	 * does not compile. The shape cannot live in a resource fixture, because {@code FieldSorting}
	 * reorders the declarators and turns the forward read into a backward one.
	 */
	@ParameterizedTest
	@ValueSource(strings = {
			"class T { int alpha = this.beta; int beta; }",
			"class T { { System.out.println(this.beta); } int beta; }"
	})
	public void testThisAtRefusesAForwardFieldRead(String source) throws Exception {
		assertNull(NoUnnecessaryThisCheck.thisAt(
				parseInline(source), 0, source.indexOf("this.") + "this".length()
		));
	}
}