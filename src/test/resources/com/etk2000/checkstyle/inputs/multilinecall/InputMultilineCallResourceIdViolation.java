package com.etk2000.checkstyle.inputs.multilinecall;

class InputMultilineCallResourceIdViolation {
	void resourceIdLambdaNotOnOpening() {
		method( // violation line 5 — lambda not on opening paren line
				R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation line 9 — closing brace not on closing paren line
	}

	void resourceIdLambdaNotOnClosing() {
		method(R.string.ok, x -> {
			System.out.println(x);
		}
		); // violation line 16 — closing brace not on closing paren line
	}

	void resourceIdBracelessLambdaOnBodyLine() {
		method(R.string.ok, v ->
				System.out.println(v)); // violation line 21 — closing paren on body line
	}

	void androidResourceIdLambdaNotOnOpening() {
		method( // violation line 25 — lambda not on opening paren line
				android.R.string.ok, x -> {
					System.out.println(x);
				}
		); // violation line 29 — closing brace not on closing paren line
	}

	void method(Object a, java.util.function.Consumer<Integer> c) {
	}
}