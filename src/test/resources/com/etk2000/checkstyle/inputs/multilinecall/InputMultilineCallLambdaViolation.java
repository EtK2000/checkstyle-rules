package com.etk2000.checkstyle.inputs.multilinecall;

import java.util.function.Consumer;

class InputMultilineCallLambdaViolation {
	void bracelessLambdaClosingOnBodyLine() {
		method(v ->
				System.out.println(v)); // violation: In multiline calls/signatures, no arguments on the closing paren line.
	}

	void lambdaNotOnClosingLine() {
		method(x -> {
			System.out.println(x);
		}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void lambdaNotOnOpeningLine() {
		method( // violation: Inline block argument: must be on the opening paren line.
				x -> {
					System.out.println(x);
				}
		); // violation: Inline block argument: closing brace/paren must be on the closing paren line.
	}

	void method(Consumer<Integer> c) {
	}
}