package com.etk2000.checkstyle.inputs.prefervar;

class InputNestedInnerSubclasses<T> {
	class Inner {
	}

	class C1 extends Inner {
		void m() {
			final String chained = "x";
			System.out.println(chained);
		}
	}
}