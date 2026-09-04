package com.etk2000.checkstyle.inputs.overloadmethodorder;

// === case: param_count ===
class InputOverloadViolation {
	void method(int a, int b) {}

	void method(int a) {}
}
// === end ===

// === case: type ===
class InputOverloadTypeViolation {
	void arr(int[] a) {}

	void arr(int a) {}

	void dim(int[][] a) {}

	void dim(int[] a) {}

	void mix(String a) {}

	void mix(int[] a) {}

	void method(String a) {}

	void method(int a) {}

	void obj(String a) {}

	void obj(List a) {}

	void prim(int a) {}

	void prim(char a) {}

	void secondParam(int a, int b) {}

	void secondParam(int a, char b) {}

	void vararg(int... a) {}

	void vararg(int a) {}
}
// === end ===

// === case: type_qualified_generic ===
// imports: java.util.Map
class InputOverloadQualifiedGenericViolation {
	// a qualified generic hangs its TYPE_ARGUMENTS off the DOT as the last child,
	// so the base name must be read from the last IDENT, not the last child
	void nested(Map<String, String> a) {}

	void nested(Outer.Inner<String> a) {}

	void nestedArray(Map<String, String> a) {}

	void nestedArray(Outer.Inner<String>[] a) {}
}

class Outer {
	static class Inner<T> {
	}
}
// === end ===