package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallResourceIdViolation {
	void resourceIdLambdaNotOnOpening() {
		method( // violation: lambda not on opening paren line
				R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation: closing brace not on closing paren line
	}

	void resourceIdLambdaNotOnClosing() {
		method(R.string.ok, x -> {
			System.out.println(x);
		}
		); // violation: closing brace not on closing paren line
	}

	void resourceIdBracelessLambdaOnBodyLine() {
		method(R.string.ok, v ->
				System.out.println(v)); // violation: closing paren on body line
	}

	void androidResourceIdLambdaNotOnOpening() {
		method( // violation: lambda not on opening paren line
				android.R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation: closing brace not on closing paren line
	}

	void method(Object a, java.util.function.Consumer<Integer> c) {
	}
}