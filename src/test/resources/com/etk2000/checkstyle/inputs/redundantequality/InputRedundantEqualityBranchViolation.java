package com.etk2000.checkstyle.inputs.redundantequality;

class InputRedundantEqualityBranchViolation {
	int assignReturn(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			r = a;
		else
			r = b;
		return r;
	}

	int assignSwapped(int a, int b) {
		final int r;
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			r = b;
		else
			r = a;
		return r;
	}

	int ifElseReturn(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		else
			return b;
	}

	int ifElseReturnSwapped(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			return b;
		else
			return a;
	}

	int notEqual(int a, int b) {
		final int r;
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			r = a;
		else
			r = b;
		return r;
	}

	int notEqualIfElseReturn(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			return a;
		else
			return b;
	}

	int notEqualSwapped(int a, int b) {
		final int r;
		if (a != b) // violation: Redundant equality if-else, use 'b' directly.
			r = b;
		else
			r = a;
		return r;
	}

	int notEqualTrailingReturn(int a, int b) {
		if (a != b) // violation: Redundant equality if-else, use 'a' directly.
			return a;
		return b;
	}

	int trailingReturn(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'b' directly.
			return a;
		return b;
	}

	int trailingReturnSwapped(int a, int b) {
		if (a == b) // violation: Redundant equality if-else, use 'a' directly.
			return b;
		return a;
	}
}