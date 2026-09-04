package com.etk2000.checkstyle.inputs.preferimport;

import com.etk2000.checkstyle.testhelpers.ClasspathScopeBase;

class InputSupertypeChainClasspathField {
	static class C0<T> extends ClasspathScopeBase {
	}

	static class C1<T> extends C0<T> {
	}

	static class Sub extends C1<String> {
		int max() {
			return java.lang.Integer.MAX_VALUE;
		}

		Thread thread() {
			return java.lang.Thread.currentThread();
		}
	}
}