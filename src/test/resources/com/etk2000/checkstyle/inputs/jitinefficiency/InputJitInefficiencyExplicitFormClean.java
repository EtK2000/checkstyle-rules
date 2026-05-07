package com.etk2000.checkstyle.inputs.jitinefficiency;

import java.util.List;

class InputJitInefficiencyExplicitFormClean {
	static class SomeUnknownType {
		Object field;
	}

	int n;
	String f;

	void explicitArrayElementNonString(int n) {
		final var arr = new int[3];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i] + 1;
	}

	void explicitArrayLhsNotBare(int n) {
		final var arr = new String[3];
		for (var i = 0; i < n; ++i)
			arr[i] = arr[i].trim() + "!";
	}

	void explicitDifferentVar(int n) {
		String s = "";
		final var t = "x";
		for (var i = 0; i < n; ++i)
			s = t + i;
		System.out.println(s);
	}

	void explicitFieldNonString(int times) {
		this.n = 0;
		for (var i = 0; i < times; ++i)
			this.n = this.n + 1;
		System.out.println(this.n);
	}

	void explicitNonString(int times) {
		int n = 0;
		for (var i = 0; i < times; ++i)
			n = n + 1;
		System.out.println(n);
	}

	void explicitNotBare(List<String> list) {
		String s = "abc";
		for (var x : list)
			s = s.trim() + x;
		System.out.println(s);
	}

	void explicitOutsideLoop() {
		var s = "a";
		s = s + "b";
		System.out.println(s);
	}

	void explicitRhsMethodCall(int n) {
		String s = "";
		for (var i = 0; i < n; ++i)
			s = compute();
		System.out.println(s);
	}

	void explicitTernaryRhs(int n, boolean flag) {
		String s = "";
		for (var i = 0; i < n; ++i)
			s = flag ? s : "x";
		System.out.println(s);
	}

	void explicitLambdaInLoop(List<String> list) {
		for (var x : list) {
			final Runnable r = () -> {
				String t = "";
				t = t + x;
				System.out.println(t);
			};
			r.run();
		}
	}

	void explicitMethodCallArrayReceiverLhs(List<String> list) {
		for (var x : list)
			getArr()[0] = getArr()[0] + x;
	}

	void explicitMethodCallReceiverLhs(List<String> list) {
		for (var x : list)
			getSelf().f = getSelf().f + x;
	}

	void explicitUnresolvableMiddleSegment(SomeUnknownType obj, List<String> list) {
		// `obj`'s type is unknown so the middle segment can't be resolved.
		for (var x : list)
			obj.field.value = obj.field.value + x;
	}

	void explicitVarFromUnknownMethod(List<String> list) {
		// Documented limitation: receiver-type inference would require resolving
		// `list.get(0)`'s element type, which the same-file resolver doesn't do.
		var result = list.get(0).toUpperCase();
		for (var x : list)
			result = result + x;
		System.out.println(result);
	}

	private String compute() {
		return "x";
	}

	private String[] getArr() {
		return new String[]{"a"};
	}

	private InputJitInefficiencyExplicitFormClean getSelf() {
		return this;
	}
}