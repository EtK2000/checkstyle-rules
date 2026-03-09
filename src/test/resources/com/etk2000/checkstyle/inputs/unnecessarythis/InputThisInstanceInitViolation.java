package com.etk2000.checkstyle.inputs.unnecessarythis;

class InputThisInstanceInitViolation {
	int field;

	{
		System.out.println(this.field); // violation: unnecessary this
	}
}