package com.etk2000.checkstyle.gradle.fix;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import java.util.List;

public class JitInefficiencyFixerTest {
	@Test
	public void appendConcatLiteralAndIdent() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"key=\" + value);";
		final var attempt = fixer.fix(List.of(line), 0, line.indexOf('+'));
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tsb.append(\"key=\").append(value);", result.replacement().getFirst());
	}

	@Test
	public void appendConcatRefusesNonStringConcat() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(a + b);";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatRefusesNumericLeadingChain() {
		// `1 + 2 + "x"` evaluates as `"3x"`; naive `.append(1).append(2).append("x")` splitting yields `"12x"`
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(1 + 2 + \"x\");";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatTextBlockBail() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"\"\"prefix\"\"\" + value);";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void appendConcatThreeOperandsStringLeading() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tsb.append(\"a\" + b + c);";
		final var attempt = fixer.fix(List.of(line), 0, line.indexOf('+'));
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tsb.append(\"a\").append(b).append(c);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanFalseLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(false);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.FALSE;", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanTrueLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(true);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.TRUE;", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorBooleanVariable() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var b = new Boolean(flag);";
		final var col = line.indexOf("new Boolean");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var b = Boolean.valueOf(flag);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorInteger() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Integer(42);";
		final var col = line.indexOf("new Integer");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var x = Integer.valueOf(42);", result.replacement().getFirst());
	}

	@Test
	public void boxedConstructorRefusesNonBoxedType() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Foo(42);";
		final var col = line.indexOf("new Foo");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void boxedConstructorRefusesUnclosedParen() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = new Integer(42";
		final var col = line.indexOf("new Integer");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void emptyStringConcatCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + 'x';";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf('x');", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatEscapedQuoteCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + f('\\'');";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(f('\\''));", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatLeft() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + x;";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(x);", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatPlusInsideCharLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + f('+');";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(f('+'));", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatRefusesChain() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\" + a + b;";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void emptyStringConcatRefusesChainReversed() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = a + b + \"\";";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf('+')));
	}

	@Test
	public void emptyStringConcatRight() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = name + \"\";";
		final var col = line.indexOf('+');
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = String.valueOf(name);", result.replacement().getFirst());
	}

	@Test
	public void emptyStringConcatTextBlockBail() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = \"\"\"text\"\"\" + \"\" + x;";
		assertNull(fixer.fix(List.of(line), 0, line.indexOf("\"\" +")));
	}

	@Test
	public void newStringLiteral() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String(\"hello\");";
		final var col = line.indexOf("new String");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var s = \"hello\";", result.replacement().getFirst());
	}

	@Test
	public void newStringRefusesComplexArg() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String(getValue());";
		final var col = line.indexOf("new String");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void newStringRefusesEmptyArg() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new String();";
		final var col = line.indexOf("new String");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void newStringVariable() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var copy = new String(existing);";
		final var col = line.indexOf("new String");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var copy = existing;", result.replacement().getFirst());
	}

	@Test
	public void returnsNullWhenNoPattern() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var x = 42;";
		assertNull(fixer.fix(List.of(line), 0, 0));
	}

	@Test
	public void stringBufferRefusesInputStreamSubstring() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var s = new StringBufferInputStream(bytes);";
		final var col = line.indexOf("new StringBuffer");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void stringBufferToBuilder() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var sb = new StringBuffer(\"hi\");";
		final var col = line.indexOf("new StringBuffer");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var sb = new StringBuilder(\"hi\");", result.replacement().getFirst());
	}

	@Test
	public void toArraySizedRefusesAnnotatedType() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new @Nullable String[5]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedRefusesMultiDim() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new String[5][3]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedRefusesSideEffectingSize() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var a = list.toArray(new String[mutate(x)]);";
		final var col = line.indexOf("list");
		assertNull(fixer.fix(List.of(line), 0, col));
	}

	@Test
	public void toArraySizedToZero() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var arr = list.toArray(new String[5]);";
		final var col = line.indexOf("list");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var arr = list.toArray(new String[0]);", result.replacement().getFirst());
	}

	@Test
	public void toArraySizedWithSizeExpression() {
		final var fixer = new JitInefficiencyFixer();
		final var line = "\t\tfinal var arr = list.toArray(new String[list.size()]);";
		final var col = line.indexOf("list");
		final var attempt = fixer.fix(List.of(line), 0, col);
		final var result = (FixResult) attempt;
		assertNotNull(result);
		assertEquals("\t\tfinal var arr = list.toArray(new String[0]);", result.replacement().getFirst());
	}
}