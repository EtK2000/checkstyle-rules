package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.ArrayList;
import java.util.List;

/**
 * Positions for {@code JitInefficiencyLocatorTest}, which addresses them directly rather than
 * through a reported violation. One statement per method so a test names a method instead of a
 * line, and nothing here is a slice: the file carries no markers and is never fix-run.
 */
public class InputLocatorPositions {
	static class Foreign {
		static class Integer {
			Integer(int value) {
				System.out.println(value);
			}
		}

		static class String {
			String(java.lang.String value) {
				System.out.println(value);
			}
		}

		static class StringBuffer {
		}
	}

	int count;

	String name;

	void appendConcat(StringBuilder sb, String x) {
		sb.append("a" + x);
	}

	void boxedConstructor() {
		System.out.println(new Integer(42));
	}

	void boxedConstructorForeignQualifier() {
		System.out.println(new Foreign.Integer(42));
	}

	void boxedConstructorQualified() {
		System.out.println(new java.lang.Integer(42));
	}

	void charArrayOperand(char[] chars) {
		System.out.println("" + chars);
	}

	void emptyConcatChain(int a, int b) {
		System.out.println("" + a + b);
	}

	void emptyConcatLeft(int x) {
		System.out.println("" + x);
	}

	void emptyConcatParenthesizedOperand(int a, int b) {
		System.out.println("" + (a + b));
	}

	void emptyConcatRight(int x) {
		System.out.println(x + "");
	}

	void newString() {
		System.out.println(new String("hi"));
	}

	void newStringForeignQualifier() {
		System.out.println(new Foreign.String("hi"));
	}

	void nullOperand() {
		System.out.println("" + null);
	}

	void stringBuffer() {
		final var b = new StringBuffer();
		System.out.println(b);
	}

	void stringBufferForeignQualifier() {
		final var b = new Foreign.StringBuffer();
		System.out.println(b);
	}

	void stringBufferQualified() {
		final var b = new java.lang.StringBuffer();
		System.out.println(b);
	}

	void stringConcatInLoop(String x) {
		for (var i = 0; i < 2; ++i)
			name = name + x;
	}

	void supplementaryBeforeBoxed() {
		System.out.println("𝕏" + new Integer(42));
	}

	void toArraySized(List<String> list, int n) {
		System.out.println(list.toArray(new String[n]).length);
	}

	void unmodifiableCount() {
		final var values = new ArrayList<String>();
		count = values.size();
	}
}