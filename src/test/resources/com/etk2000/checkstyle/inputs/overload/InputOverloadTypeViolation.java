package com.etk2000.checkstyle.inputs.overload;

class InputOverloadTypeViolation {
	// array before base type — wrong
	void arr(int[] a) {}

	void arr(int a) {} // violation: int must appear before int[]

	// multi-dimensional before single-dimensional — wrong
	void dim(int[][] a) {}

	void dim(int[] a) {} // violation: int[] must appear before int[][]

	// reference type before primitive array — wrong
	void mix(String a) {}

	void mix(int[] a) {} // violation: int[] sorts as primitive, before String

	// reference type before primitive — wrong
	void method(String a) {}

	void method(int a) {} // violation: int must appear before String

	// alphabetical violation within reference types
	void obj(String a) {}

	void obj(List a) {} // violation: List must appear before String

	// alphabetical violation within primitives
	void prim(int a) {}

	void prim(char a) {} // violation: char must appear before int
}