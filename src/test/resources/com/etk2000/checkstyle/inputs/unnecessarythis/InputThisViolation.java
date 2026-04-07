package com.etk2000.checkstyle.inputs.unnecessarythis;

record InputThisViolationRecord(int field) {
	InputThisViolationRecord {
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}

class InputThisViolation {
	int field;

	void method() {
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}