package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChainField {
	static class C0<T> {
		String label;
	}

	static class C1<T> extends C0<T> {
	}

	static C1<String> create() {
		return new C1<>();
	}

	void m() {
		final var chained = create();
		System.out.println(chained.label);
	}
}