package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.function.Consumer;

class InputMultilineCallThisViolation {
	void method(Object a, Consumer<Integer> c) {
	}

	void thisBracelessLambdaOnBodyLine() {
		method(this, v ->
				System.out.println(v)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void thisLambdaNotOnClosing() {
		method(this, x -> {
			System.out.println(x);
		}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void thisLambdaNotOnOpening() {
		method( // violation: Inline block argument: must be on the opening paren line.
				this, x -> {
					System.out.println(x);
				}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}
}