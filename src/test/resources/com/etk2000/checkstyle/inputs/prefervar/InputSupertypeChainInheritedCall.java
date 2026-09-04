package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChainInheritedCall {
	static class C0<T> {
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		void m() {
			final var chained = make();
			System.out.println(chained);
		}
	}

	static String make() {
		return "";
	}
}