package com.etk2000.checkstyle.inputs.prefervar;

import java.util.Map;

class InputSupertypeChainDoubling {
	static class C0<T> {
		Label label() {
			return new Label();
		}
	}

	static class Label {
	}

	static class C1<T> extends C0<Map<T, T>> {
	}

	static class Sub extends C1<String> {
		void m() {
			final var chained = label();
			System.out.println(chained);
		}
	}
}