package com.etk2000.checkstyle.inputs.classstructure;

class InputClassStructureInstanceInitViolation {
	void method() {}

	{ } // violation: instance initializer after instance method
}