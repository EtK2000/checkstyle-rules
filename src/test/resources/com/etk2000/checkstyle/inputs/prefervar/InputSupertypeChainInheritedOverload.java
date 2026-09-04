package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChainInheritedOverload {
	static class C0<T> {
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		void m() {
			final var chained = create();
			take(chained);
		}
	}

	static C1<String> create() {
		return new C1<>();
	}

	static void take(C0<String> value) {
		System.out.println(value);
	}

	static void take(Runnable task) {
		System.out.println(task);
	}
}