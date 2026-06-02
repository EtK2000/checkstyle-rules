package com.etk2000.checkstyle.inputs.overloadmethodorder;

// === case: param_count ===
class InputOverloadViolation {
	void method(int a, int b) {}

	void method(int a) {} // violation: Overload 'method' with 1 parameters must appear before overload with 2 parameters.
}
// === end ===

// === case: type ===
class InputOverloadTypeViolation {
	void arr(int[] a) {}

	void arr(int a) {} // violation: Overload 'arr(int)' must appear before 'arr(int[])'.

	void dim(int[][] a) {}

	void dim(int[] a) {} // violation: Overload 'dim(int[])' must appear before 'dim(int[][])'.

	void mix(String a) {}

	void mix(int[] a) {} // violation: Overload 'mix(int[])' must appear before 'mix(String)'.

	void method(String a) {}

	void method(int a) {} // violation: Overload 'method(int)' must appear before 'method(String)'.

	void obj(String a) {}

	void obj(List a) {} // violation: Overload 'obj(List)' must appear before 'obj(String)'.

	void prim(int a) {}

	void prim(char a) {} // violation: Overload 'prim(char)' must appear before 'prim(int)'.

	void secondParam(int a, int b) {}

	void secondParam(int a, char b) {} // violation: Overload 'secondParam(int, char)' must appear before 'secondParam(int, int)'.

	void vararg(int... a) {}

	void vararg(int a) {} // violation: Overload 'vararg(int)' must appear before 'vararg(int...)'.
}
// === end ===