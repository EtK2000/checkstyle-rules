package com.etk2000.checkstyle.inputs.emptybody;

class InputEmptyBodyClean {
	void bracedIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
	}

	void bracedIfElse(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else {
			System.out.println("non-positive");
		}
	}

	void bracedIfElseIf(int x) {
		if (x > 0) {
			System.out.println("positive");
		}
		else if (x < 0) {
			System.out.println("negative");
		}
		else {
			System.out.println("zero");
		}
	}

	void normalIf(int x) {
		if (x > 0)
			System.out.println("positive");
	}

	void normalIfElse(int x) {
		if (x > 0)
			System.out.println("positive");
		else
			System.out.println("non-positive");
	}

	void normalIfElseIf(int x) {
		if (x > 0)
			System.out.println("positive");
		else if (x < 0)
			System.out.println("negative");
		else
			System.out.println("zero");
	}
}