package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.function.Consumer;

class InputMultilineCallResourceIdViolation {
	void androidResourceIdLambdaNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				android.R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void method(Object a, Consumer<Integer> c) {
	}

	void resourceIdBracelessLambdaOnBodyLine() {
		method(R.string.ok, v ->
				System.out.println(v)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void resourceIdLambdaNotOnClosing() {
		method(R.string.ok, x -> {
			System.out.println(x);
		}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void resourceIdLambdaNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}
}