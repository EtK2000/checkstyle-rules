package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallThisViolation {
	void method(Object a, java.util.function.Consumer<Integer> c) {
	}

	void thisBracelessLambdaOnBodyLine() {
		method(this, v ->
				System.out.println(v)); // violation: closing paren on body line
	}

	void thisLambdaNotOnClosing() {
		method(this, x -> {
			System.out.println(x);
		}
		); // violation: closing brace not on closing paren line
	}

	void thisLambdaNotOnOpening() {
		method( // violation: args not on opening paren line
				this, x -> {
					System.out.println(x);
				}
		); // violation: closing brace not on closing paren line
	}
}