package com.etk2000.checkstyle.inputs.preferimport;

class InputSupertypeChainUnknownRoot {
	static class C0<T> extends MissingSupertype {
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		java.lang.Thread.State state;

		int max() {
			return java.lang.Integer.MAX_VALUE;
		}
	}
}