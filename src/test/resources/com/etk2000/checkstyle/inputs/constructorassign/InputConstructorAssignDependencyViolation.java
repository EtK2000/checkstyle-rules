package com.etk2000.checkstyle.inputs.constructorassign;

class InputConstructorAssignDependencyViolation {
	int alpha, beta;

	InputConstructorAssignDependencyViolation(int alpha) {
		this.beta = this.alpha + 1; // violation: Assignment 'this.beta' references 'this.alpha' which should be assigned before it.
		this.alpha = alpha;
	}
}