package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallLambdaViolation {
	void lambdaNotOnOpeningLine() {
		method( // violation line 5 — lambda not on opening paren line
				x -> {
					System.out.println(x);
				}
		); // violation line 9 — closing brace not on closing paren line
	}

	void lambdaNotOnClosingLine() {
		method(x -> {
			System.out.println(x);
		}
		); // violation line 16 — closing brace not on closing paren line
	}

	void bracelessLambdaClosingOnBodyLine() {
		method(v ->
				System.out.println(v)); // violation line 21 — closing paren on body line
	}

	void method(java.util.function.Consumer<Integer> c) {
	}
}