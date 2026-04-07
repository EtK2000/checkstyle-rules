package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisInstanceInitViolation {
	int field;

	{
		System.out.println(this.field); // violation: Unnecessary 'this.field', only use when shadowing or in field assignment.
	}
}