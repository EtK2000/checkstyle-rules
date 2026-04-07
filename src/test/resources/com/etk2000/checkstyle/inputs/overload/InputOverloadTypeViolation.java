package com.etk2000.checkstyle.inputs.overload;

class InputOverloadTypeViolation {
	void arr(int[] a) {}

	void arr(int a) {} // violation: int must appear before int[]

	void dim(int[][] a) {}

	void dim(int[] a) {} // violation: int[] must appear before int[][]

	void mix(String a) {}

	void mix(int[] a) {} // violation: int[] sorts as primitive, before String

	void method(String a) {}

	void method(int a) {} // violation: int must appear before String

	void obj(String a) {}

	void obj(List a) {} // violation: List must appear before String

	void prim(int a) {}

	void prim(char a) {} // violation: char must appear before int

	void secondParam(int a, int b) {}

	void secondParam(int a, char b) {} // violation: char must appear before int at position 2

	void vararg(int... a) {}

	void vararg(int a) {} // violation: int must appear before int...
}