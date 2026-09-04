package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChainInheritedField {
	static class C0<T> {
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		void m() {
			for (var chained : NUMBERS)
				System.out.println(chained);
		}
	}

	static final Integer[] NUMBERS = {};
}