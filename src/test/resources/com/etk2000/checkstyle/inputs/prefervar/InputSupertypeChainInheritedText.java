package com.etk2000.checkstyle.inputs.prefervar;

class InputSupertypeChainInheritedText {
	static class C0<T> {
		String text() {
			return "";
		}
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		String m() {
			return text().replaceAll("a", "b");
		}
	}
}