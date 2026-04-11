package com.etk2000.checkstyle.inputs.constructorassign;

// V1: alphabetical violation in group 1
class InputConstructorAssignViolation {
	int alpha, beta;

	InputConstructorAssignViolation(int alpha, int beta) {
		this.beta = beta;
		this.alpha = alpha; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}

// V2: group 2 before group 1
class InputConstructorAssignMultiBeforeSimple {
	int alpha;
	Object beta;

	InputConstructorAssignMultiBeforeSimple(int alpha, Object beta) {
		this.beta = new Object() {
			@Override
			public String toString() {
				return beta.toString();
			}
		};
		this.alpha = alpha; // violation: Simple assignment 'this.alpha' must appear before multi-line assignments.
	}
}

// V3: alphabetical violation in group 2
class InputConstructorAssignMultiAlphabetical {
	Object alpha, beta;

	InputConstructorAssignMultiAlphabetical(Object a, Object b) {
		this.beta = new Object() {
			@Override
			public String toString() {
				return b.toString();
			}
		};
		this.alpha = new Object() { // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
			@Override
			public String toString() {
				return a.toString();
			}
		};
	}
}

// V4: group 3 before group 1
class InputConstructorAssignVarBeforeSimple {
	int alpha, beta;

	InputConstructorAssignVarBeforeSimple(int x) {
		final var computed = x * 2;
		this.alpha = computed;
		this.beta = x; // violation: Assignment 'this.beta' must appear before variable-dependent assignments.
	}
}

// V5: group 3 before group 2
class InputConstructorAssignVarBeforeMulti {
	int beta;
	Object alpha;

	InputConstructorAssignVarBeforeMulti(int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = new Object() { // violation: Assignment 'this.alpha' must appear before variable-dependent assignments.
			@Override
			public String toString() {
				return "test";
			}
		};
	}
}

// V6: alphabetical violation within var sub-group
class InputConstructorAssignVarAlphabetical {
	int alpha, beta;

	InputConstructorAssignVarAlphabetical(int x) {
		final var computed = x * 2;
		this.beta = computed;
		this.alpha = computed + 1; // violation: Assignment 'this.alpha' must appear before 'this.beta' (alphabetical order).
	}
}

// V7: later var sub-group before earlier sub-group
class InputConstructorAssignVarGroupOrder {
	int alpha, beta;

	InputConstructorAssignVarGroupOrder(int x) {
		final var first = x + 1;
		final var second = x + 2;
		this.beta = second;
		this.alpha = first; // violation: Assignment 'this.alpha' must appear before 'this.beta' (variable declaration order).
	}
}