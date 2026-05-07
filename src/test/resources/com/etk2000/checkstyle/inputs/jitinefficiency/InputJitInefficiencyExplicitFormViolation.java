package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.List;

class InputJitInefficiencyExplicitFormViolation {
	static class Holder {
		Holder next;
		String value;
	}

	final Holder holder = new Holder();
	String f;
	String[] arr;
	String[][] grid;

	void stringConcatExplicitArrayLhsChainedIndex(int k, int j, List<String> list) {
		final var matrix = new String[10][10];
		matrix[k][j] = "";
		for (var x : list)
			matrix[k][j] = matrix[k][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitArrayLhsClassicFor(int n) {
		final var arr = new String[n];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i] + "!"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitArrayLhsExternalIndex(int k, List<String> list) {
		final var local = new String[10];
		local[k] = "";
		for (var x : list)
			local[k] = local[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitArrayLhsThisArray(int k, List<String> list) {
		this.arr[k] = "";
		for (var x : list)
			this.arr[k] = this.arr[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitArrayLhsThisChainedIndex(int k, int j, List<String> list) {
		this.grid[k][j] = "";
		for (var x : list)
			this.grid[k][j] = this.grid[k][j] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitChained(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + ", " + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitClassicFor(int n) {
		String result = "";
		for (var i = 0; i < n; ++i)
			result = result + i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitDoWhileTier3() {
		String s = "";
		do
			s = s + "x"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}

	void stringConcatExplicitFieldThis(List<String> list) {
		this.f = "";
		for (var x : list)
			this.f = this.f + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(this.f);
	}

	void stringConcatExplicitForEach(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitMiddle(List<String> list) {
		String result = "";
		for (var x : list)
			result = ">>" + result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitReversed(List<String> list) {
		String result = "";
		for (var x : list)
			result = x + result; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitWhile(boolean cond) {
		String result = "";
		while (cond) {
			result = result + getNext(); // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			cond = result.length() < 5;
		}
		System.out.println(result);
	}

	void stringConcatExplicitDeepChain(List<String> list) {
		String result = "";
		for (var x : list)
			result = result + x + "," + " " + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitFqnType(List<String> list) {
		java.lang.String result = "";
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitInForIterator(int n) {
		String s = "";
		for (var i = 0; i < n; s = s + i) // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
			System.out.println(i);
	}

	void stringConcatExplicitDoWhileTier2() {
		String s = "";
		do s = s + "y"; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		while (s.length() < 5);
		System.out.println(s);
	}

	void stringConcatExplicitFieldOnObj(InputJitInefficiencyExplicitFormViolation obj, List<String> list) {
		obj.f = "";
		for (var x : list)
			obj.f = obj.f + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitThisDotNested(List<String> list) {
		this.holder.value = "";
		for (var x : list)
			this.holder.value = this.holder.value + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitVarFromMethod(List<String> list) {
		var result = compute();
		for (var x : list)
			result = result + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		System.out.println(result);
	}

	void stringConcatExplicitDeepNestedField(List<String> list) {
		this.holder.next.value = "";
		for (var x : list)
			this.holder.next.value = this.holder.next.value + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitFqnArrayLhs(int k, List<String> list) {
		final java.lang.String[] local = new java.lang.String[10];
		local[k] = "";
		for (var x : list)
			local[k] = local[k] + x; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
	}

	void stringConcatExplicitNestedLoopOuterIndex(int m, int n) {
		final var local = new String[m];
		for (var k = 0; k < m; ++k) {
			local[k] = "";
			for (var i = 0; i < n; ++i)
				local[k] = local[k] + i; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	void stringConcatExplicitNestedLoopInnerIndex(int m, int n) {
		final var local = new String[n];
		for (var k = 0; k < m; ++k) {
			for (var i = 0; i < n; ++i)
				local[i] = local[i] + k; // violation: String concatenation inside a loop allocates a new String per iteration; use a 'StringBuilder'.
		}
	}

	private String compute() {
		return "x";
	}

	private String getNext() {
		return "x";
	}
}