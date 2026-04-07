package com.etk2000.checkstyle.inputs.fieldsorting;

class InputFieldSortingDependencyViolation {
	// dependency ordering is correct, but ALPHA has no dependency and should come first
	static final int BASE = 10;
	static final int DERIVED = BASE + 1;
	static final int ALPHA = 5; // violation: Field 'ALPHA' must appear before 'DERIVED' (alphabetical order, same type).
}

class InputFieldSortingDependencyForwardRef {
	// forward reference via this: beta depends on alpha, but alpha is declared after
	int beta = this.alpha + 1; // violation: Field 'beta' references 'alpha' which should be declared before it.
	int alpha = 10;
}