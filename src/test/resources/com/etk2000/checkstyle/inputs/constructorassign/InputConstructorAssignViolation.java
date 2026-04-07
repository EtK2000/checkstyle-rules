package com.etk2000.checkstyle.inputs.constructorassign;

class InputConstructorAssignViolation {
	int alpha, beta;

	InputConstructorAssignViolation(int alpha, int beta) {
		this.beta = beta; // violation: beta before alpha
		this.alpha = alpha; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}

class InputConstructorAssignMultiBeforeSimple {
	int alpha;
	Object beta;

	InputConstructorAssignMultiBeforeSimple(int alpha, Object beta) {
		this.beta = new Object() { // multi-line first
			@Override
			public String toString() {
				return beta.toString();
			}
		};
		this.alpha = alpha; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}