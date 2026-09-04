package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChain {
	static class C0<T> {
		String put(T value) {
			return "C0";
		}
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
	}

	static C1<String> create() {
		return new C1<>();
	}

	void m() {
		final var chained = create();
		System.out.println(chained.put("a"));
	}
}