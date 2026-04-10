package com.etk2000.checkstyle.inputs.mathmethod;

class InputPreferMathMethodTernaryViolation {
	int x;

	int absGe(int a) {
		return a >= 0 ? a : -a; // violation: Use 'Math.abs(a)' instead of 'a >= 0 ? a : -a'.
	}

	int absGeZeroLeft(int a) {
		return 0 >= a ? -a : a; // violation: Use 'Math.abs(a)' instead of '0 >= a ? -a : a'.
	}

	int absGt(int a) {
		return a > 0 ? a : -a; // violation: Use 'Math.abs(a)' instead of 'a > 0 ? a : -a'.
	}

	int absGtZeroLeft(int a) {
		return 0 > a ? -a : a; // violation: Use 'Math.abs(a)' instead of '0 > a ? -a : a'.
	}

	int absLe(int a) {
		return a <= 0 ? -a : a; // violation: Use 'Math.abs(a)' instead of 'a <= 0 ? -a : a'.
	}

	int absLeZeroLeft(int a) {
		return 0 <= a ? a : -a; // violation: Use 'Math.abs(a)' instead of '0 <= a ? a : -a'.
	}

	int absLt(int a) {
		return a < 0 ? -a : a; // violation: Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.
	}

	int absLtZeroLeft(int a) {
		return 0 < a ? a : -a; // violation: Use 'Math.abs(a)' instead of '0 < a ? a : -a'.
	}

	int absWithArrayAccess(int[] arr) {
		return arr[0] < 0 ? -arr[0] : arr[0]; // violation: Use 'Math.abs(arr[0])' instead of 'arr[0] < 0 ? -arr[0] : arr[0]'.
	}

	long absWithLongParam(long a) {
		return a < 0 ? -a : a; // violation: Use 'Math.abs(a)' instead of 'a < 0 ? -a : a'.
	}

	int maxGe(int a, int b) {
		return a >= b ? a : b; // violation: Use 'Math.max(a, b)' instead of 'a >= b ? a : b'.
	}

	int maxGt(int a, int b) {
		return a > b ? a : b; // violation: Use 'Math.max(a, b)' instead of 'a > b ? a : b'.
	}

	int maxLe(int a, int b) {
		return a <= b ? b : a; // violation: Use 'Math.max(a, b)' instead of 'a <= b ? b : a'.
	}

	int maxLt(int a, int b) {
		return a < b ? b : a; // violation: Use 'Math.max(a, b)' instead of 'a < b ? b : a'.
	}

	int maxWithArrayAccess(int[] arr) {
		return arr[0] > arr[1] ? arr[0] : arr[1]; // violation: Use 'Math.max(arr[0], arr[1])' instead of 'arr[0] > arr[1] ? arr[0] : arr[1]'.
	}

	int maxWithFieldAccess(InputPreferMathMethodTernaryViolation a, InputPreferMathMethodTernaryViolation b) {
		return a.x > b.x ? a.x : b.x; // violation: Use 'Math.max(a.x, b.x)' instead of 'a.x > b.x ? a.x : b.x'.
	}

	int maxWithLiteral(int a) {
		return a > 5 ? a : 5; // violation: Use 'Math.max(a, 5)' instead of 'a > 5 ? a : 5'.
	}

	int maxWithPreDecrement(int a, int b) {
		return --a > b ? a : b; // violation: Use 'Math.max(--a, b)' instead of '--a > b ? a : b'.
	}

	int maxWithPreIncrement(int a, int b) {
		return ++a > b ? a : b; // violation: Use 'Math.max(++a, b)' instead of '++a > b ? a : b'.
	}

	int minGe(int a, int b) {
		return a >= b ? b : a; // violation: Use 'Math.min(a, b)' instead of 'a >= b ? b : a'.
	}

	int minGt(int a, int b) {
		return a > b ? b : a; // violation: Use 'Math.min(a, b)' instead of 'a > b ? b : a'.
	}

	int minLe(int a, int b) {
		return a <= b ? a : b; // violation: Use 'Math.min(a, b)' instead of 'a <= b ? a : b'.
	}

	int minLt(int a, int b) {
		return a < b ? a : b; // violation: Use 'Math.min(a, b)' instead of 'a < b ? a : b'.
	}
}