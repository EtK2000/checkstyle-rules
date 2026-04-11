package com.etk2000.checkstyle.inputs.constructorassign;

class InputConstructorAssignDependencyViolation {
	int alpha, beta;

	InputConstructorAssignDependencyViolation(int alpha) {
		this.beta = this.alpha + 1; // violation: Assignment 'this.beta' references 'this.alpha' which should be assigned before it.
		this.alpha = alpha;
	}
}

// dependency violation within a var sub-group (same code path as above but in GROUP_VAR context)
class InputConstructorAssignVarDependencyViolation {
	int alpha, beta;

	InputConstructorAssignVarDependencyViolation(int beta, int x) {
		final var computed = x * 2;
		this.alpha = computed + this.beta; // violation: Assignment 'this.alpha' references 'this.beta' which should be assigned before it.
		this.beta = computed;
	}
}