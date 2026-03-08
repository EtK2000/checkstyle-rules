package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisViolation {
	int field;

	void method() {
		System.out.println(this.field);
	}
}