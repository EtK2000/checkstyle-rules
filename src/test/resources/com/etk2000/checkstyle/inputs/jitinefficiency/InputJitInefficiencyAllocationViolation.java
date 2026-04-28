package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.List;

class InputJitInefficiencyAllocationViolation {
	void appendConcat(StringBuilder sb, String value) {
		sb.append("key=" + value); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
		sb.append(value + "=tail"); // violation: Use chained '.append()' instead of string concatenation inside '.append(...)'.
	}

	void emptyStringConcat(int x, String name) {
		final var s = "" + x; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		final var t = name + ""; // violation: Use 'String.valueOf(...)' instead of concatenating with the empty string.
		System.out.println(s + t);
	}

	void newStringLiteralAndVar(String existing) {
		final var a = new String("hello"); // violation: Use the string literal directly instead of wrapping in 'new String(...)'.
		final var b = new String(existing); // violation: Use the String variable directly instead of wrapping in 'new String(...)'.
		System.out.println(a + b);
	}

	void stringBufferLocal() {
		final var sb = new StringBuffer(); // violation: Use 'StringBuilder' instead of 'StringBuffer' for non-shared local builders.
		sb.append("x");
		System.out.println(sb);
	}

	void toArraySized(List<String> list) {
		final var a = list.toArray(new String[5]); // violation: 'toArray(new String[non-zero])' is slower than 'toArray(new String[0])' on modern JVMs.
		final var b = list.toArray(new String[list.size()]); // violation: 'toArray(new String[non-zero])' is slower than 'toArray(new String[0])' on modern JVMs.
		System.out.println(a.length + b.length);
	}
}